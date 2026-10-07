/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests;

import me.pikamug.quests.actions.Action;
import me.pikamug.quests.actions.FabricActionFactory;
import me.pikamug.quests.listeners.FabricCommandManager;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.conditions.FabricConditionFactory;
import me.pikamug.quests.config.ConfigSettings;
import me.pikamug.quests.config.FabricConfigSettings;
import me.pikamug.quests.dependencies.FabricDependencies;
import me.pikamug.quests.listeners.FabricBlockListener;
import me.pikamug.quests.listeners.FabricChatListener;
import me.pikamug.quests.listeners.FabricEntityListener;
import me.pikamug.quests.listeners.FabricItemListener;
import me.pikamug.quests.listeners.FabricPlayerListener;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.QuestProgress;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.FabricQuestFactory;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.statistics.FabricMetrics;
import me.pikamug.quests.storage.FabricStorageFactory;
import me.pikamug.quests.storage.QuesterStorage;
import me.pikamug.quests.storage.implementation.file.FabricActionJsonStorage;
import me.pikamug.quests.storage.implementation.file.FabricConditionJsonStorage;
import me.pikamug.quests.storage.implementation.file.FabricQuestJsonStorage;
import me.pikamug.quests.storage.implementation.jar.FabricModuleJarStorage;
import me.pikamug.quests.tasks.FabricNpcEffectThread;
import me.pikamug.quests.tasks.FabricPlayerMoveThread;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.ConsoleVt;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.FabricQuestsLogger;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FabricQuestsPlugin implements DedicatedServerModInitializer, Quests {

    public static final String MOD_ID = "quests";
    public static final FabricQuestsLogger LOGGER = new FabricQuestsLogger();

    private boolean loading = true;
    private static FabricQuestsPlugin instance;
    private MinecraftServer server;
    private FabricDependencies depends;
    private FabricActionJsonStorage actionLoader;
    private FabricConditionJsonStorage conditionLoader;
    private ConfigSettings configSettings;
    private FabricModuleJarStorage customLoader;
    private FabricQuestJsonStorage questLoader;
    private List<CustomObjective> customObjectives = new LinkedList<>();
    private List<CustomRequirement> customRequirements = new LinkedList<>();
    private List<CustomReward> customRewards = new LinkedList<>();
    private volatile Map<UUID, Quester> questers = new ConcurrentHashMap<>();
    private Collection<Quest> quests = ConcurrentHashMap.newKeySet();
    private Collection<Action> actions = ConcurrentHashMap.newKeySet();
    private Collection<Condition> conditions = ConcurrentHashMap.newKeySet();
    private Collection<UUID> questNpcUuids = ConcurrentHashMap.newKeySet();
    private final Map<UUID, net.minecraft.core.BlockPos> tempBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, net.minecraft.core.BlockPos> selectedBlockStarts = new ConcurrentHashMap<>();
    private FabricQuestFactory questFactory;
    private FabricActionFactory actionFactory;
    private FabricConditionFactory conditionFactory;
    private FabricNpcEffectThread effectThread;
    private FabricPlayerMoveThread moveThread;
    private QuesterStorage storage;

    @Override
    public void onInitializeServer() {
        instance = this;

        ConsoleVt.enable();

        // Register commands before the server constructs its CommandDispatcher,
        // otherwise the Commands-construction event fires before any listener exists.
        new FabricCommandManager(this);

        FabricMixinEvents.registerServerStarted(server -> {
            this.server = server;
            onEnable();
        });

        FabricMixinEvents.registerServerStopping(server -> {
            onDisable();
        });
    }

    private void onEnable() {
        /*----> WARNING: ORDER OF STEPS MATTERS <----*/

        // 0 - Conversations are initialized by the embedded conversation-fabric mod itself
        // (SERVER_STARTED -> FabricConversations.init), so Quests only consumes the API.

        // 1 - Initialize variables
        actionLoader = new FabricActionJsonStorage(this);
        conditionLoader = new FabricConditionJsonStorage(this);
        configSettings = new FabricConfigSettings(this);
        customLoader = new FabricModuleJarStorage(this);
        questLoader = new FabricQuestJsonStorage(this);
        effectThread = new FabricNpcEffectThread(this);
        moveThread = new FabricPlayerMoveThread(this);
        questFactory = new FabricQuestFactory(this);
        actionFactory = new FabricActionFactory(this);
        conditionFactory = new FabricConditionFactory(this);
        depends = new FabricDependencies(this);

        // 2 - Load main config
        configSettings.init();
        if (configSettings.getLanguage().contains("-")) {
            final FabricMetrics metrics = new FabricMetrics(this);
            metrics.addCustomChart(new FabricMetrics.SimplePie("language", () -> configSettings.getLanguage()));
        }

        // 3 - Setup language files
        try {
            FabricLang.init(this);
        } catch (final IOException e) {
            LOGGER.error("Failed to initialize language files", e);
        }

        // 4 - Load soft-depends
        depends.init();

        // 5 - Transfer resources from jar
        moveStorageResource("quests.json");
        moveStorageResource("actions.json");
        moveStorageResource("conditions.json");
        saveResourceAs("quests.json", "storage/quests.json", false);
        saveResourceAs("actions.json", "storage/actions.json", false);
        saveResourceAs("conditions.json", "storage/conditions.json", false);

        // 6 - Load storage
        final FabricStorageFactory storageFactory = new FabricStorageFactory(this);
        storage = storageFactory.getInstance();

        // 7 - Register listeners
        new FabricBlockListener(this);
        new FabricItemListener(this);
        new FabricPlayerListener(this);
        new FabricEntityListener(this);
        new FabricChatListener(this);
        if (depends.hasEasyNpc()) {
            new me.pikamug.quests.listeners.npc.FabricEasyNpcListener(this);
        }
        if (depends.hasTaterzens()) {
            new me.pikamug.quests.listeners.npc.FabricTaterzensListener(this);
        }

        // 9 - Register tick events
        FabricMixinEvents.registerServerTick(this::onTick);

        if (configSettings.getStrictPlayerMovement() > 0) {
            final long ticks = configSettings.getStrictPlayerMovement();
            moveThread.setInterval(ticks);
        }

        // 8 - Delay loading of quests, actions and modules
        delayLoadQuestInfo();

        LOGGER.info("Quests plugin enabled.");
    }

    private void onDisable() {
        LOGGER.info("Saving Quester data...");
        if (server != null) {
            for (final net.minecraft.server.level.ServerPlayer p : server.getPlayerList().getPlayers()) {
                getQuester(p.getUUID()).saveData();
            }
        }
        LOGGER.info("Closing storage...");
        if (storage != null) {
            storage.close();
        }
    }

    private void onTick(MinecraftServer server) {
        // Tick scheduled tasks
        FabricScheduler.tick(server);

        // Tick player move thread
        if (configSettings != null && configSettings.getStrictPlayerMovement() > 0 && moveThread != null) {
            moveThread.run();
        }
    }

    public boolean isLoading() {
        return loading;
    }

    public static FabricQuestsPlugin getInstance() {
        return instance;
    }

    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public File getPluginDataFolder() {
        final Path path = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        final File dir = path.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Override
    public FabricQuestsLogger getPluginLogger() {
        return LOGGER;
    }

    @Override
    public InputStream getPluginResource(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    @Override
    public String getDetectedServerSoftwareVersion() {
        return "Fabric " + net.minecraft.SharedConstants.getCurrentVersion().name();
    }

    @Override
    public FabricDependencies getDependencies() {
        return depends;
    }

    @Override
    public ConfigSettings getConfigSettings() {
        return configSettings;
    }

    @Override
    public List<CustomObjective> getCustomObjectives() {
        return customObjectives;
    }

    @Override
    public List<CustomReward> getCustomRewards() {
        return customRewards;
    }

    @Override
    public List<CustomRequirement> getCustomRequirements() {
        return customRequirements;
    }

    @Override
    public Collection<Quest> getLoadedQuests() {
        return quests;
    }

    @Override
    public Collection<Action> getLoadedActions() {
        return actions;
    }

    public Action getAction(final String name) {
        if (name == null) {
            return null;
        }
        for (final Action a : actions) {
            if (a.getName().equalsIgnoreCase(name)) {
                return a;
            }
        }
        for (final Action a : actions) {
            if (a.getName().toLowerCase().startsWith(name.toLowerCase())) {
                return a;
            }
        }
        for (final Action a : actions) {
            if (a.getName().toLowerCase().contains(name.toLowerCase())) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Collection<Condition> getLoadedConditions() {
        return conditions;
    }

    public Condition getCondition(final String name) {
        if (name == null) {
            return null;
        }
        for (final Condition c : conditions) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        for (final Condition c : conditions) {
            if (c.getName().toLowerCase().startsWith(name.toLowerCase())) {
                return c;
            }
        }
        for (final Condition c : conditions) {
            if (c.getName().toLowerCase().contains(name.toLowerCase())) {
                return c;
            }
        }
        return null;
    }

    @Override
    public FabricQuester getQuester(final UUID id) {
        if (depends.isNpc(id)) {
            return new FabricQuester(this, id);
        }
        return (FabricQuester) questers.computeIfAbsent(id, uuid -> new FabricQuester(this, uuid));
    }

    /**
     * Loads a player's stored data from the active storage implementation and
     * applies it to the live {@link Quester} so no progress is lost across
     * restarts or reloads.
     *
     * @param uuid the player to load data for
     */
    public void applyLoadedQuester(final UUID uuid) {
        if (uuid == null) {
            return;
        }
        try {
            final Quester loaded = getStorage().loadQuester(uuid).get();
            if (loaded == null) {
                return;
            }
            final FabricQuester live = getQuester(uuid);
            if (live == loaded) {
                // Storage implementation (e.g. SQL) already mutated the live quester
                return;
            }
            live.setLastKnownName(loaded.getLastKnownName());
            live.setQuestPoints(loaded.getQuestPoints());
            live.setCurrentQuests(loaded.getCurrentQuests());
            live.setCompletedQuests(loaded.getCompletedQuests());
            live.setCompletedTimes(loaded.getCompletedTimes());
            live.setAmountsCompleted(loaded.getAmountsCompleted());
            if (loaded instanceof FabricQuester fabricLoaded) {
                for (final Map.Entry<Quest, QuestProgress> e : fabricLoaded.getProgressData().entrySet()) {
                    live.setQuestProgress(e.getKey(), e.getValue());
                }
            }
            live.setHasData(fabricLoadedHasData(loaded));
        } catch (final Exception e) {
            LOGGER.error("Failed to load quester data for {}", uuid, e);
        }
    }

    private static boolean fabricLoadedHasData(final Quester loaded) {
        return loaded instanceof FabricQuester fabricLoaded && fabricLoaded.hasData();
    }

    @Override
    public Collection<Quester> getOnlineQuesters() {
        final Set<Quester> online = ConcurrentHashMap.newKeySet();
        if (server == null) {
            return online;
        }
        for (final Quester q : getOfflineQuesters()) {
            if (server.getPlayerList().getPlayer(q.getUUID()) != null) {
                online.add(q);
            }
        }
        return online;
    }

    @Override
    public Collection<Quester> getOfflineQuesters() {
        return Collections.unmodifiableCollection(questers.values());
    }

    @Override
    public void addQuester(final Quester q) {
        questers.put(q.getUUID(), q);
    }

    @Override
    public void removeQuester(final Quester q) {
        questers.remove(q.getUUID());
    }

    /**
     * Checks if user is non-op player in Trial Mode
     *
     * @param uuid the editor user to be checked
     * @return {@code true} if user is an online non-operator, {@code false} otherwise
     */
    public boolean hasLimitedAccess(final UUID uuid) {
        final net.minecraft.server.level.ServerPlayer player = FabricMiscUtil.getPlayer(uuid, this);
        if (player == null) {
            return false;
        }
        return !me.pikamug.quests.util.FabricMiscUtil.hasPermission(player, net.minecraft.server.permissions.PermissionLevel.GAMEMASTERS);
    }

    /**
     * Get a Quest by Name
     *
     * @param name Name of the quest
     * @return Closest match or null if not found
     */
    public Quest getQuest(final String name) {
        if (name == null) {
            return null;
        }
        for (final Quest q : quests) {
            if (q.getName().equalsIgnoreCase(name)) {
                return q;
            }
        }
        for (final Quest q : quests) {
            if (q.getName().toLowerCase().startsWith(name.toLowerCase())) {
                return q;
            }
        }
        for (final Quest q : quests) {
            if (q.getName().toLowerCase().contains(name.toLowerCase())) {
                return q;
            }
        }
        return null;
    }

    public Quest getQuestById(final String id) {
        if (id == null) {
            return null;
        }
        for (final Quest q : quests) {
            if (q.getId().equals(id)) {
                return q;
            }
        }
        return null;
    }

    public Collection<UUID> getQuestNpcUuids() {
        return questNpcUuids;
    }

    public void setQuestNpcUuids(final Collection<UUID> questNpcUuids) {
        final Set<UUID> newQuestNpcUuids = ConcurrentHashMap.newKeySet();
        newQuestNpcUuids.addAll(questNpcUuids);
        this.questNpcUuids = newQuestNpcUuids;
    }

    @Override
    public FabricQuestFactory getQuestFactory() {
        return questFactory;
    }

    public Map<UUID, net.minecraft.core.BlockPos> getTempBlocks() {
        return tempBlocks;
    }

    public Map<UUID, net.minecraft.core.BlockPos> getSelectedBlockStarts() {
        return selectedBlockStarts;
    }

    public boolean isSelectingBlockStart(final UUID uuid) {
        return selectedBlockStarts.containsKey(uuid);
    }

    @Override
    public FabricActionFactory getActionFactory() {
        return actionFactory;
    }

    @Override
    public FabricConditionFactory getConditionFactory() {
        return conditionFactory;
    }

    public FabricNpcEffectThread getNpcEffectThread() {
        return effectThread;
    }

    public FabricPlayerMoveThread getPlayerMoveThread() {
        return moveThread;
    }

    public QuesterStorage getStorage() {
        return storage;
    }

    public FabricQuestJsonStorage getQuestLoader() {
        return questLoader;
    }

    public boolean isEnabled() {
        return server != null && !loading;
    }

    private void moveStorageResource(String fileName) {
        final File storageDir = new File(getPluginDataFolder(), "storage");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        final File storageFile = new File(getPluginDataFolder(), fileName);
        if (!storageFile.isFile()) {
            return;
        }
        final File outFile = new File(storageDir, fileName);
        final boolean moved = storageFile.renameTo(outFile);
        if (!moved) {
            LOGGER.error("Unable to move {} file. Check folder permissions and restart server.", fileName);
        }
    }

    @Override
    public void saveResourceAs(String resourcePath, final String outputPath, final boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        final InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath
                    + "' cannot be found in Quests jar");
        }

        final String outPath = outputPath.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        final File outFile = new File(getPluginDataFolder(), outPath);
        final File outDir = new File(outFile.getPath().replace(outFile.getName(), ""));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                final OutputStream out = new FileOutputStream(outFile);
                final byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (final IOException ex) {
            LOGGER.error("Could not save {} to {}", outFile.getName(), outFile, ex);
        }
    }

    private void delayLoadQuestInfo() {
        // Load conditions, actions, quests after a short delay to let soft-depends initialize
        FabricScheduler.runLater(() -> {
            try {
                conditionLoader.init();
                actionLoader.init();
                questLoader.init();
                LOGGER.info("Loaded {} Quest(s), {} Action(s), {} Condition(s) and {} Phrase(s)",
                        quests.size(), actions.size(), conditions.size(), FabricLang.size());
                customLoader.init();
                questLoader.importQuests();
            } catch (final Exception e) {
                LOGGER.error("Failed to load quests, actions or conditions", e);
            }
        }, 5L);

        FabricScheduler.runLater(() -> {
            if (server != null) {
                for (final net.minecraft.server.level.ServerPlayer p : server.getPlayerList().getPlayers()) {
                    applyLoadedQuester(p.getUUID());
                }
            }
            loading = false;
        }, 60L);
    }

    public void reload() {
        if (loading) {
            LOGGER.warn("Still loading, cannot reload yet");
            return;
        }
        loading = true;
        FabricScheduler.runAsync(() -> {
            try {
                if (configSettings.getConsoleLogging() > 3) {
                    LOGGER.info("Starting save of all questers (may take a while)");
                }
                getStorage().saveOfflineQuesters().get();
                FabricLang.clear();
                configSettings.init();
                FabricLang.load(FabricQuestsPlugin.this, configSettings.getLanguage());
                quests.clear();
                actions.clear();
                conditions.clear();
                conditionLoader.init();
                actionLoader.init();
                questLoader.init();
                for (final Quester quester : questers.values()) {
                    applyLoadedQuester(quester.getUUID());
                    for (final Quest quest : quester.getCurrentQuests().keySet()) {
                        quester.checkQuest(quest);
                    }
                }
                customLoader.init();
                questLoader.importQuests();
            } catch (final Exception e) {
                LOGGER.error("Error during reload", e);
            }
            loading = false;
        });
    }
}
