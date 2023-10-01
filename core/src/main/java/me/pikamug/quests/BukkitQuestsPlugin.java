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

import me.pikamug.localelib.LocaleManager;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.actions.BukkitActionFactory;
import me.pikamug.quests.conditions.BukkitConditionFactory;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.config.BukkitConfigSettings;
import me.pikamug.quests.config.ConfigSettings;
import me.pikamug.quests.convo.misc.NpcOfferQuestPrompt;
import me.pikamug.quests.convo.misc.QuestAcceptPrompt;
import me.pikamug.quests.dependencies.BukkitDenizenTrigger;
import me.pikamug.quests.dependencies.BukkitDependencies;
import me.pikamug.quests.interfaces.ReloadCallback;
import me.pikamug.quests.listeners.BukkitBlockListener;
import me.pikamug.quests.listeners.BukkitCitizensListener;
import me.pikamug.quests.listeners.BukkitCommandManager;
import me.pikamug.quests.listeners.BukkitConvoListener;
import me.pikamug.quests.listeners.BukkitItemListener;
import me.pikamug.quests.listeners.BukkitPartiesListener;
import me.pikamug.quests.listeners.BukkitPlayerListener;
import me.pikamug.quests.listeners.BukkitUniteListener;
import me.pikamug.quests.listeners.BukkitZnpcsListener;
import me.pikamug.quests.logging.BukkitQuestsLog4JFilter;
import me.pikamug.quests.storage.implementation.jar.BukkitModuleJarStorage;
import me.pikamug.quests.storage.implementation.ModuleStorageImpl;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.BukkitQuestFactory;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.statistics.BukkitMetrics;
import me.pikamug.quests.storage.BukkitStorageFactory;
import me.pikamug.quests.storage.QuesterStorage;
import me.pikamug.quests.storage.implementation.file.BukkitActionYamlStorage;
import me.pikamug.quests.storage.implementation.file.BukkitConditionYamlStorage;
import me.pikamug.quests.storage.implementation.file.BukkitQuestYamlStorage;
import me.pikamug.quests.tasks.BukkitNpcEffectThread;
import me.pikamug.quests.tasks.BukkitPlayerMoveThread;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitUpdateChecker;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitQuestsPlugin extends JavaPlugin implements Quests {

    private boolean loading = true;
    private String bukkitVersion = "0";
    private BukkitDependencies depends;
    private BukkitActionYamlStorage actionLoader;
    private BukkitConditionYamlStorage conditionLoader;
    private ConfigSettings configSettings;
    private ModuleStorageImpl customLoader;
    private BukkitQuestYamlStorage questLoader;
    private List<CustomObjective> customObjectives = new LinkedList<>();
    private List<CustomRequirement> customRequirements = new LinkedList<>();
    private List<CustomReward> customRewards = new LinkedList<>();
    private Collection<Quester> questers = new ConcurrentSkipListSet<>();
    private Collection<Quest> quests = new ConcurrentSkipListSet<>();
    private Collection<Action> actions = new ConcurrentSkipListSet<>();
    private Collection<Condition> conditions = new ConcurrentSkipListSet<>();
    private Collection<UUID> questNpcUuids = new ConcurrentSkipListSet<>();
    private TabExecutor cmdExecutor;
    private ConversationFactory conversationFactory;
    private ConversationFactory npcConversationFactory;
    private BukkitQuestFactory questFactory;
    private BukkitActionFactory actionFactory;
    private BukkitConditionFactory conditionFactory;
    private BukkitConvoListener convoListener;
    private BukkitBlockListener blockListener;
    private BukkitItemListener itemListener;
    private BukkitCitizensListener citizensListener;
    private BukkitZnpcsListener znpcsListener;
    private BukkitPlayerListener playerListener;
    private BukkitNpcEffectThread effectThread;
    private BukkitPlayerMoveThread moveThread;
    private BukkitUniteListener uniteListener;
    private BukkitPartiesListener partiesListener;
    private BukkitDenizenTrigger trigger;
    private LocaleManager localeManager;
    private QuesterStorage storage;

    @Override
    public void onEnable() {
        /*----> WARNING: ORDER OF STEPS MATTERS <----*/

        // 1 - Trigger server to initialize Legacy Material Support
        try {
            Material.matchMaterial("STONE", true);
        } catch (final NoSuchMethodError ignored) {
        }

        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new BukkitQuestsLog4JFilter());

        // 2 - Initialize variables
        bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        actionLoader = new BukkitActionYamlStorage(this);
        conditionLoader = new BukkitConditionYamlStorage(this);
        configSettings = new BukkitConfigSettings(this);
        customLoader = new BukkitModuleJarStorage(this);
        questLoader = new BukkitQuestYamlStorage(this);
        try {
            Class.forName("me.pikamug.quests.libs.localelib.LocaleManager");
            localeManager = new LocaleManager();
        } catch (final Exception ignored) {
            getLogger().warning("LocaleLib not present! Is this a debug environment?");
        }
        convoListener = new BukkitConvoListener();
        blockListener = new BukkitBlockListener(this);
        itemListener = new BukkitItemListener(this);
        citizensListener = new BukkitCitizensListener(this);
        znpcsListener = new BukkitZnpcsListener(this);
        playerListener = new BukkitPlayerListener(this);
        uniteListener = new BukkitUniteListener();
        partiesListener = new BukkitPartiesListener();
        effectThread = new BukkitNpcEffectThread(this);
        moveThread = new BukkitPlayerMoveThread(this);
        questFactory = new BukkitQuestFactory(this);
        actionFactory = new BukkitActionFactory(this);
        conditionFactory = new BukkitConditionFactory(this);
        depends = new BukkitDependencies(this);
        trigger = new BukkitDenizenTrigger(this);

        // 3 - Load main config
        configSettings.init();
        if (configSettings.getLanguage().contains("-")) {
            final BukkitMetrics metrics = new BukkitMetrics(this);
            metrics.addCustomChart(new BukkitMetrics.SimplePie("language", () -> configSettings.getLanguage()));
        }
        
        // 4 - Setup language files
        try {
            BukkitLang.init(this);
        } catch (final IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        // 5 - Load command executor
        cmdExecutor = new BukkitCommandManager(this);
        
        // 6 - Load soft-depends
        depends.init();
        
        // 7 - Save resources from jar
        saveResourceAs("quests.yml", "quests.yml", false);
        saveResourceAs("actions.yml", "actions.yml", false);
        saveResourceAs("conditions.yml", "conditions.yml", false);
        
        // 8 - Save config with any new options
        getConfig().options().copyDefaults(true);
        getConfig().options().header("See https://pikamug.gitbook.io/quests/setup/configuration");
        saveConfig();
        final BukkitStorageFactory storageFactory = new BukkitStorageFactory(this);
        storage = storageFactory.getInstance();
        
        // 9 - Setup commands
        if (getCommand("quests") != null) {
            Objects.requireNonNull(getCommand("quests")).setExecutor(getTabExecutor());
            Objects.requireNonNull(getCommand("quests")).setTabCompleter(getTabExecutor());
        }
        if (getCommand("questadmin") != null) {
            Objects.requireNonNull(getCommand("questadmin")).setExecutor(getTabExecutor());
            Objects.requireNonNull(getCommand("questadmin")).setTabCompleter(getTabExecutor());
        }
        if (getCommand("quest") != null) {
            Objects.requireNonNull(getCommand("quest")).setExecutor(getTabExecutor());
            Objects.requireNonNull(getCommand("quest")).setTabCompleter(getTabExecutor());
        }
        
        // 10 - Build conversation factories
        this.conversationFactory = new ConversationFactory(this).withModality(false)
                .withPrefix(context -> ChatColor.GRAY.toString())
                .withFirstPrompt(new QuestAcceptPrompt(this)).withTimeout(configSettings.getAcceptTimeout())
                .thatExcludesNonPlayersWithMessage("Console may not perform this conversation!")
                .addConversationAbandonedListener(convoListener);
        this.npcConversationFactory = new ConversationFactory(this).withModality(false)
                .withFirstPrompt(new NpcOfferQuestPrompt(this)).withTimeout(configSettings.getAcceptTimeout())
                .withLocalEcho(false).addConversationAbandonedListener(convoListener);

        // 11 - Register listeners
        getServer().getPluginManager().registerEvents(getBlockListener(), this);
        getServer().getPluginManager().registerEvents(getItemListener(), this);
        depends.linkCitizens();
        if (depends.getZnpcsPlus() != null) {
            getServer().getPluginManager().registerEvents(getZnpcsListener(), this);
        }
        getServer().getPluginManager().registerEvents(getPlayerListener(), this);
        if (configSettings.getStrictPlayerMovement() > 0) {
            final long ticks = configSettings.getStrictPlayerMovement() * 20L;
            getServer().getScheduler().scheduleSyncRepeatingTask(this, getPlayerMoveThread(), ticks, ticks);
        }
        if (depends.getPartyProvider() != null) {
            getServer().getPluginManager().registerEvents(getUniteListener(), this);
        } else if (depends.getPartiesApi() != null) {
            getServer().getPluginManager().registerEvents(getPartiesListener(), this);
        }

        // 12 - Attempt to check for updates
        new BukkitUpdateChecker(this, 3711).getVersion(version -> {
            if (!getDescription().getVersion().split("-")[0].equalsIgnoreCase(version)) {
                getLogger().info(ChatColor.DARK_GREEN + BukkitLang.get("updateTo").replace("<version>",
                        version).replace("<url>", ChatColor.AQUA + getDescription().getWebsite()));
            }
        });

        // 13 - Delay loading of quests, actions and modules
        delayLoadQuestInfo();
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving Quester data...");
        for (final Player p : getServer().getOnlinePlayers()) {
            getQuester(p.getUniqueId()).saveData();
        }
        Bukkit.getScheduler().cancelTasks(this);
        getLogger().info("Closing storage...");
        if (storage != null) {
            storage.close();
        }
    }

    public boolean isProVersion() {
        return false;
    }

    public boolean isLoading() {
        return loading;
    }

    public File getPluginDataFolder() {
        return getDataFolder();
    }

    public Logger getPluginLogger() {
        return getLogger();
    }

    public InputStream getPluginResource(String filename) {
        return getResource(filename);
    }

    public String getDetectedServerSoftwareVersion() {
        return bukkitVersion;
    }

    public BukkitDependencies getDependencies() {
        return depends;
    }

    public BukkitConfigSettings getConfigSettings() {
        return (BukkitConfigSettings) configSettings;
    }

    @Override
    public List<CustomObjective> getCustomObjectives() {
        return customObjectives;
    }

    public Optional<CustomObjective> getCustomObjective(final String className) {
        for (final CustomObjective co : customObjectives) {
            if (co.getClass().getName().equals(className)) {
                return Optional.of(co);
            }
        }
        return Optional.empty();
    }

    public void setCustomObjectives(List<CustomObjective> customObjectives) {
        this.customObjectives = customObjectives;
    }

    @Override
    public List<CustomRequirement> getCustomRequirements() {
        return customRequirements;
    }

    public Optional<CustomRequirement> getCustomRequirement(final String className) {
        for (final CustomRequirement cr : customRequirements) {
            if (cr.getClass().getName().equals(className)) {
                return Optional.of(cr);
            }
        }
        return Optional.empty();
    }

    public void setCustomRequirements(List<CustomRequirement> customRequirements) {
        this.customRequirements = customRequirements;
    }

    @Override
    public List<CustomReward> getCustomRewards() {
        return customRewards;
    }

    public Optional<CustomReward> getCustomReward(final String className) {
        for (final CustomReward cr : customRewards) {
            if (cr.getClass().getName().equals(className)) {
                return Optional.of(cr);
            }
        }
        return Optional.empty();
    }

    public void setCustomRewards(List<CustomReward> customRewards) {
        this.customRewards = customRewards;
    }

    /**
     * Get every Quest loaded in memory
     *
     * @return a collection of all Quests
     */
    public Collection<Quest> getLoadedQuests() {
        return quests;
    }

    /**
     * Set every Quest loaded in memory
     *
     */
    public void setLoadedQuests(final Collection<Quest> quests) {
        this.quests = quests;
    }

    /**
     * Get every Action loaded in memory
     *
     * @return a collection of all Actions
     */
    public Collection<Action> getLoadedActions() {
        return actions;
    }

    /**
     * Set every Action loaded in memory
     *
     */
    public void setLoadedActions(final Collection<Action> actions) {
        this.actions = actions;
    }

    /**
     * Get every Condition loaded in memory
     *
     * @return a collection of all Conditions
     */
    public Collection<Condition> getLoadedConditions() {
        return conditions;
    }

    /**
     * Set every Condition loaded in memory
     *
     */
    public void setLoadedConditions(final Collection<Condition> conditions) {
        this.conditions = conditions;
    }

    /**
     * Get Quester from player UUID
     *
     * @param id Player UUID
     * @return new or existing Quester
     */
    public BukkitQuester getQuester(final @NotNull UUID id) {
        final ConcurrentSkipListSet<Quester> set = (ConcurrentSkipListSet<Quester>) questers;
        for (final Quester q : set) {
            if (q != null && q.getUUID().equals(id)) {
                return (BukkitQuester) q;
            }
        }
        final BukkitQuester quester = new BukkitQuester(this, id);
        if (depends.getCitizens() != null) {
            if (depends.getCitizens().getNPCRegistry().getByUniqueId(id) != null) {
                return quester;
            }
        }
        final BukkitQuester q = new BukkitQuester(this, id);
        questers.add(q);
        return q;
    }

    /**
     * Get every online Quester playing on this server
     *
     * @return a collection of all online Questers
     */
    public Collection<Quester> getOnlineQuesters() {
        final Collection<Quester> questers = new ConcurrentSkipListSet<>();
        for (final Quester q : getOfflineQuesters()) {
            if (q.getOfflinePlayer().isOnline()) {
                // Workaround for issues with the compass on fast join
                q.findCompassTarget();
                questers.add(q);
            }
        }
        return questers;
    }

    /**
     * Get every Quester that has ever played on this server
     *
     * @return a collection of all Questers
     */
    public Collection<Quester> getOfflineQuesters() {
        return questers;
    }

    /**
     * Set every Quester that has ever played on this server
     *
     * @param questers a collection of Questers
     */
    public void setOfflineQuesters(final Collection<Quester> questers) {
        this.questers = new ConcurrentSkipListSet<>(questers);
    }

    /**
     * Get every NPC UUID which sees use a quest giver, talk target, or kill target
     *
     * @return a collection of all UUIDs
     */
    public Collection<UUID> getQuestNpcUuids() {
        return questNpcUuids;
    }

    /**
     * Set every NPC UUID which sees use a quest giver, talk target, or kill target
     *
     * @param questNpcUuids a collection of UUIDs
     */
    @SuppressWarnings("unused")
    public void setQuestNpcUuids(final Collection<UUID> questNpcUuids) {
        this.questNpcUuids = new ConcurrentSkipListSet<>(questNpcUuids);
    }

    @SuppressWarnings("unused")
    public CommandExecutor getCommandExecutor() {
        return cmdExecutor;
    }

    public TabExecutor getTabExecutor() {
        return cmdExecutor;
    }

    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }

    public ConversationFactory getNpcConversationFactory() {
        return npcConversationFactory;
    }

    public BukkitQuestFactory getQuestFactory() {
        return questFactory;
    }

    public BukkitActionFactory getActionFactory() {
        return actionFactory;
    }

    public BukkitConditionFactory getConditionFactory() {
        return conditionFactory;
    }

    public BukkitConvoListener getConvoListener() {
        return convoListener;
    }

    public BukkitBlockListener getBlockListener() {
        return blockListener;
    }

    public BukkitItemListener getItemListener() {
        return itemListener;
    }

    public BukkitCitizensListener getCitizensListener() {
        return citizensListener;
    }

    public BukkitZnpcsListener getZnpcsListener() {
        return znpcsListener;
    }

    public BukkitPlayerListener getPlayerListener() {
        return playerListener;
    }

    public BukkitUniteListener getUniteListener() {
        return uniteListener;
    }

    public BukkitNpcEffectThread getNpcEffectThread() {
        return effectThread;
    }

    public BukkitPlayerMoveThread getPlayerMoveThread() {
        return moveThread;
    }

    public BukkitPartiesListener getPartiesListener() {
        return partiesListener;
    }

    public BukkitDenizenTrigger getDenizenTrigger() {
        return trigger;
    }

    public LocaleManager getLocaleManager() {
        return localeManager;
    }

    public QuesterStorage getStorage() {
        return storage;
    }

    /**
     * Save a Quests plugin resource to a specific path in the filesystem
     *
     * @param resourcePath jar file location starting from resource folder, i.e. "lang/el-GR/strings.yml"
     * @param outputPath file destination starting from Quests folder, i.e. "lang/el-GR/strings.yml"
     * @param replace whether or not to replace the destination file
     */
    @Override
    public void saveResourceAs(String resourcePath, final String outputPath, final boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        final InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath
                    + "' cannot be found in Quests jar");
        }

        final String outPath = outputPath.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        final File outFile = new File(getDataFolder(), outPath);
        final File outDir = new File(outFile.getPath().replace(outFile.getName(), ""));

        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                getLogger().log(Level.SEVERE, "Failed to make directories for " + outFile.getName() + " (canWrite= "
                        + outDir.canWrite() + ")");
            }
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
                if (!outFile.exists()) {
                    getLogger().severe("Unable to copy " + outFile.getName() + " (canWrite= " + outFile.canWrite()
                            + ")");
                }
            }
        } catch (final IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    /**
     * Load quests, actions, conditions, and modules
     * 
     * At startup, this lets soft-depends (namely Citizens) fully load first
     */
    private void delayLoadQuestInfo() {
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            conditionLoader.init();
            actionLoader.init();
            questLoader.init();
            getLogger().log(Level.INFO, "Loaded " + quests.size() + " Quest(s), " + actions.size() + " Action(s), "
                    + conditions.size() + " Condition(s) and " + BukkitLang.size() + " Phrase(s)");
            for (final Player p : getServer().getOnlinePlayers()) {
                final Quester quester =  new BukkitQuester(BukkitQuestsPlugin.this, p.getUniqueId());
                if (!quester.hasData()) {
                    quester.saveData();
                }
                // Workaround for issues with the compass on fast join
                quester.findCompassTarget();
                questers.add(quester);
            }
            if (depends.getCitizens() != null) {
                if (depends.getCitizens().getNPCRegistry() == null) {
                    getLogger().log(Level.SEVERE,
                            "Citizens was enabled but NPCRegistry was null. Disabling linkage.");
                    depends.unlinkCitizens();
                }
            }
            customLoader.init();
            questLoader.importQuests();
            if (getConfigSettings().canDisableCommandFeedback()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule sendCommandFeedback false");
            }
            loading = false;
        }, 5L);
    }

    /**
     * Reload quests, actions, conditions, config settings, lang, modules, and player data
     */
    public void reload(final ReloadCallback<Boolean> callback) {
        if (loading) {
            getLogger().warning(ChatColor.YELLOW + BukkitLang.get("errorLoading"));
            return;
        }
        loading = true;
        reloadConfig();
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                getStorage().saveOfflineQuesters().get();
                BukkitLang.clear();
                configSettings.init();
                BukkitLang.load(BukkitQuestsPlugin.this, configSettings.getLanguage());
                quests.clear();
                actions.clear();
                conditions.clear();
                conditionLoader.init();
                actionLoader.init();
                questLoader.init();
                for (final Quester quester : questers) {
                    final Quester loaded = getStorage().loadQuester(quester.getUUID()).get();
                    for (final Quest quest : loaded.getCurrentQuests().keySet()) {
                        loaded.checkQuest(quest);
                    }
                }
                customLoader.init();
                questLoader.importQuests();
                finishLoading(callback, true, null);
            } catch (final Exception e) {
                finishLoading(callback, false, e);
            }
            loading = false;
        });
    }

    /**
     * Execute finishing task and print provided exception
     *
     * @param callback Callback to execute
     * @param result Result to pass through callback
     * @param exception Exception to print, or null
     */
    private void finishLoading(final ReloadCallback<Boolean> callback, boolean result, final Exception exception) {
        if (exception != null) {
            exception.printStackTrace();
        }
        if (callback != null) {
            Bukkit.getScheduler().runTask(BukkitQuestsPlugin.this, () -> {
                loading = false;
                callback.execute(result);
            });
        }
    }

    /**
     * Checks if player can use the Quests plugin
     * 
     * @param uuid the entity UUID to be checked
     * @return {@code true} if entity is a Player that has permission
     */
    public boolean canUseQuests(final UUID uuid) {
        final Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            for (final Permission perm : getDescription().getPermissions()) {
                if (p.hasPermission(perm.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if conversable is non-op, non-* player in Trial Mode
     *
     * @param conversable the editor user to be checked
     * @return {@code true} if user is a Player with quests.mode.trial permission
     */
    public boolean hasLimitedAccess(final Conversable conversable) {
        if (!(conversable instanceof Player)) {
            return false;
        }
        final Player player = ((Player)conversable);
        if (player.isOp() || player.hasPermission("*")) {
            return false;
        }
        return player.hasPermission("quests.mode.trial");
    }

    /**
     * Get a Quest by ID
     *
     * @param id ID of the quest
     * @return Exact match or null if not found
     * @since 3.8.6
     */
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
    
    /**
     * Get a Quest by name
     * 
     * @param name Name of the quest
     * @return Closest match or null if not found
     */
    public Quest getQuest(final String name) {
        if (name == null) {
            return null;
        }
        for (final Quest q : quests) {
            if (q.getName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', name))) {
                return q;
            }
        }
        for (final Quest q : quests) {
            if (q.getName().toLowerCase().startsWith(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return q;
            }
        }
        for (final Quest q : quests) {
            if (q.getName().toLowerCase().contains(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return q;
            }
        }
        for (final Quest q : quests) {
            // For tab completion
            if (ChatColor.stripColor(q.getName()).equals(ChatColor.stripColor(ChatColor
                    .translateAlternateColorCodes('&', name)))) {
                return q;
            }
        }
        return null;
    }
    
    /**
     * Get an Action by name
     * 
     * @param name Name of the action
     * @return Closest match or null if not found
     */
    public Action getAction(final String name) {
        if (name == null) {
            return null;
        }
        for (final Action a : actions) {
            if (a.getName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', name))) {
                return a;
            }
        }
        for (final Action a : actions) {
            if (a.getName().toLowerCase().startsWith(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return a;
            }
        }
        for (final Action a : actions) {
            if (a.getName().toLowerCase().contains(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return a;
            }
        }
        for (final Action a : actions) {
            // For tab completion
            if (ChatColor.stripColor(a.getName()).equals(ChatColor.stripColor(ChatColor.
                    translateAlternateColorCodes('&', name)))) {
                return a;
            }
        }
        return null;
    }
    
    /**
     * Get a Condition by name
     * 
     * @param name Name of the condition
     * @return Closest match or null if not found
     */
    public Condition getCondition(final String name) {
        if (name == null) {
            return null;
        }
        for (final Condition c : conditions) {
            if (c.getName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', name))) {
                return c;
            }
        }
        for (final Condition c : conditions) {
            if (c.getName().toLowerCase().startsWith(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return c;
            }
        }
        for (final Condition c : conditions) {
            if (c.getName().toLowerCase().contains(ChatColor.translateAlternateColorCodes('&', name).toLowerCase())) {
                return c;
            }
        }
        for (final Condition c : conditions) {
            // For tab completion
            if (ChatColor.stripColor(c.getName()).equals(ChatColor.stripColor(ChatColor
                    .translateAlternateColorCodes('&', name)))) {
                return c;
            }
        }
        return null;
    }
}
