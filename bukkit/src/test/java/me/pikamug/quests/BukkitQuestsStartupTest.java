package me.pikamug.quests;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import me.pikamug.quests.dependencies.BukkitDependencies;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.storage.QuesterStorage;
import me.pikamug.quests.storage.implementation.file.BukkitQuesterYamlStorage;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Exercises the production delayed-startup callback and YAML storage without starting a Minecraft server.
 * Only the hosting server, player, scheduler and plugin environment are test doubles.
 */
public class BukkitQuestsStartupTest {
    private static final UUID PLAYER_ID = UUID.fromString("472fd431-e2ac-48e5-a363-253fb5e6b3c8");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private BukkitQuestsPlugin plugin;
    private QuesterStorage storage;
    private File dataFile;
    private final Map<Long, Runnable> delayedTasks = new HashMap<>();

    /**
     * Creates the isolated host fixture and real YAML-backed storage.
     *
     * @throws Exception if fixture files or plugin fields cannot be initialized
     */
    @Before
    public void setUp() throws Exception {
        final File pluginDirectory = temporaryFolder.newFolder("Quests");
        final File dataDirectory = new File(pluginDirectory, "data");
        assertTrue(dataDirectory.mkdir());
        dataFile = new File(dataDirectory, PLAYER_ID + ".yml");

        final Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(PLAYER_ID);
        when(player.getName()).thenReturn("StartupFixture");
        when(player.isOnline()).thenReturn(true);
        final PlayerInventory inventory = mock(PlayerInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[0]);
        when(player.getInventory()).thenReturn(inventory);

        final Server server = mock(Server.class);
        doReturn(Collections.singletonList(player)).when(server).getOnlinePlayers();
        when(server.getPlayer(PLAYER_ID)).thenReturn(player);
        when(server.getOfflinePlayer(PLAYER_ID)).thenReturn(player);

        // JavaPlugin normally needs a server PluginClassLoader. Keep its host methods stubbed, but execute
        // the actual Quests methods (including the private delayed callback) against their real state.
        plugin = mock(BukkitQuestsPlugin.class, CALLS_REAL_METHODS);
        doReturn(server).when(plugin).getServer();
        doReturn(pluginDirectory).when(plugin).getDataFolder();
        doReturn(Logger.getLogger(BukkitQuestsStartupTest.class.getName())).when(plugin).getLogger();
        setPluginField("questers", new ConcurrentHashMap<UUID, Quester>());
        setPluginField("quests", Collections.emptySet());
        setPluginField("depends", mock(BukkitDependencies.class));
        setPluginField("loading", true);

        final FoliaLib foliaLib = mock(FoliaLib.class, RETURNS_DEEP_STUBS);
        final PlatformScheduler scheduler = foliaLib.getScheduler();
        doAnswer(invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            final Long delay = invocation.getArgument(1);
            assertFalse("Unexpected duplicate delayed task", delayedTasks.containsKey(delay));
            delayedTasks.put(delay, runnable);
            return null;
        }).when(scheduler).runLater(any(Runnable.class), anyLong());
        setPluginField("foliaLib", foliaLib);

        final BukkitQuesterYamlStorage yamlStorage = new BukkitQuesterYamlStorage(plugin, dataDirectory.getPath());
        storage = new CompletedLoadStorage(plugin, yamlStorage);
        setPluginField("storage", storage);
    }

    /**
     * Checks that an early player's loaded state survives startup and the next real save.
     *
     * @throws Exception if loading, startup or persistence fails
     */
    @Test
    public void startupPreservesAlreadyLoadedPlayerAndSavedPoints() throws Exception {
        writeSavedPoints(777);
        final Quester loaded = storage.loadQuester(PLAYER_ID).get(5, TimeUnit.SECONDS);
        assertNotNull(loaded);
        assertTrue(loaded instanceof BukkitQuester);
        assertSame(loaded, plugin.getQuester(PLAYER_ID));
        assertEquals(777, loaded.getQuestPoints());
        assertEquals(777, readSavedPoints());

        runDelayedOnlinePlayerInitialization();
        final Quester afterStartup = plugin.getQuester(PLAYER_ID);
        final int livePoints = afterStartup.getQuestPoints();
        final int beforeSave = readSavedPoints();
        storage.saveOfflineQuesters().get(5, TimeUnit.SECONDS);
        final int savedPoints = readSavedPoints();

        System.out.println("early join: loaded=777, disk before save=" + beforeSave + ", live after startup="
                + livePoints + ", disk after save=" + savedPoints + ", same instance=" + (loaded == afterStartup));
        assertEquals("The startup callback must not alter the existing save file", 777, beforeSave);
        assertEquals("Saving after startup must preserve the loaded quest points", 777, savedPoints);
        assertSame("Startup must retain the already loaded player object", loaded, afterStartup);
        assertEquals(777, livePoints);
        assertFalse(plugin.isLoading());
    }

    /**
     * Checks initialization when the saved player has not yet entered the in-memory registry.
     *
     * @throws Exception if loading, startup or persistence fails
     */
    @Test
    public void startupLoadsSavedPlayerWhenNoQuesterIsRegistered() throws Exception {
        writeSavedPoints(777);
        assertTrue(plugin.getOfflineQuesters().isEmpty());

        runDelayedOnlinePlayerInitialization();
        final Quester initialized = plugin.getQuester(PLAYER_ID);
        final int livePoints = initialized.getQuestPoints();
        storage.saveOfflineQuesters().get(5, TimeUnit.SECONDS);
        final int savedPoints = readSavedPoints();

        System.out.println("saved player initialization: live=" + livePoints + ", persisted=" + savedPoints);
        assertEquals("Initialization must not replace the object populated by the YAML loader", 777, livePoints);
        assertEquals(777, savedPoints);
        assertEquals(1, plugin.getOfflineQuesters().size());
        assertSame(initialized, plugin.getQuester(PLAYER_ID));
    }

    /**
     * Checks that the same startup path still initializes a player without an existing save.
     *
     * @throws Exception if startup or persistence fails
     */
    @Test
    public void startupInitializesAndSavesNewPlayerWithoutExistingData() throws Exception {
        assertFalse(dataFile.exists());
        assertTrue(plugin.getOfflineQuesters().isEmpty());

        runDelayedOnlinePlayerInitialization();
        assertEquals(1, plugin.getOfflineQuesters().size());
        final Quester initialized = plugin.getOfflineQuesters().iterator().next();
        assertEquals(PLAYER_ID, initialized.getUUID());
        assertEquals(0, initialized.getQuestPoints());
        assertSame(initialized, plugin.getQuester(PLAYER_ID));

        storage.saveOfflineQuesters().get(5, TimeUnit.SECONDS);
        assertTrue(dataFile.isFile());
        assertEquals(0, readSavedPoints());
        assertEquals("StartupFixture", YamlConfiguration.loadConfiguration(dataFile).getString("lastKnownName"));
        assertFalse(plugin.isLoading());
    }

    private void runDelayedOnlinePlayerInitialization() throws Exception {
        final Method method = BukkitQuestsPlugin.class.getDeclaredMethod("delayLoadQuestInfo");
        method.setAccessible(true);
        method.invoke(plugin);
        assertEquals("Keep the production startup schedule", 2, delayedTasks.size());
        assertTrue(delayedTasks.containsKey(5L));
        assertTrue(delayedTasks.containsKey(60L));
        // The unrelated five-tick content loader is not executed. The test models a player available
        // before the original sixty-tick callback; it does not emulate Paper or the entire boot process.
        delayedTasks.get(60L).run();
    }

    private void writeSavedPoints(final int points) throws Exception {
        final YamlConfiguration data = new YamlConfiguration();
        data.set("lastKnownName", "StartupFixture");
        data.set("quest-points", points);
        data.save(dataFile);
    }

    private int readSavedPoints() throws Exception {
        final YamlConfiguration data = new YamlConfiguration();
        data.load(dataFile);
        assertTrue("The real YAML serializer must include quest-points", data.contains("quest-points"));
        return data.getInt("quest-points");
    }

    private void setPluginField(final String name, final Object value) throws Exception {
        final Field field = BukkitQuestsPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(plugin, value);
    }

    /**
     * Selects an ordering in which each real asynchronous YAML load completes before its caller continues.
     * No load/save implementation or returned player is replaced. This isolates the startup overwrite
     * from the separate load/save race and avoids timing-dependent sleeps in the regression.
     */
    private static final class CompletedLoadStorage extends QuesterStorage {
        private CompletedLoadStorage(final BukkitQuestsPlugin plugin, final BukkitQuesterYamlStorage implementation) {
            super(plugin, implementation);
        }

        @Override
        public CompletableFuture<Quester> loadQuester(final UUID uniqueId) {
            final CompletableFuture<Quester> future = super.loadQuester(uniqueId);
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException(e);
            } catch (final ExecutionException | TimeoutException e) {
                throw new CompletionException(e);
            }
            return future;
        }
    }
}
