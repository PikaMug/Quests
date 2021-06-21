/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;

import me.blackvein.quests.actions.Action;
import me.blackvein.quests.actions.ActionFactory;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.conditions.ConditionFactory;
import me.blackvein.quests.convo.npcs.NpcOfferQuestPrompt;
import me.blackvein.quests.exceptions.ActionFormatException;
import me.blackvein.quests.exceptions.ConditionFormatException;
import me.blackvein.quests.exceptions.QuestFormatException;
import me.blackvein.quests.exceptions.StageFormatException;
import me.blackvein.quests.interfaces.ReloadCallback;
import me.blackvein.quests.listeners.BlockListener;
import me.blackvein.quests.listeners.CmdExecutor;
import me.blackvein.quests.listeners.DungeonsListener;
import me.blackvein.quests.listeners.ItemListener;
import me.blackvein.quests.listeners.NpcListener;
import me.blackvein.quests.listeners.PartiesListener;
import me.blackvein.quests.listeners.PlayerListener;
import me.blackvein.quests.statistics.Metrics;
import me.blackvein.quests.storage.Storage;
import me.blackvein.quests.storage.StorageFactory;
import me.blackvein.quests.tasks.NpcEffectThread;
import me.blackvein.quests.tasks.PlayerMoveThread;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;
import me.clip.placeholderapi.PlaceholderAPI;
import me.pikamug.localelib.LocaleManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Quests extends JavaPlugin implements ConversationAbandonedListener {

    private boolean loading = true;
    private String bukkitVersion = "0";
    private Dependencies depends;
    private Settings settings;
    private final List<CustomRequirement> customRequirements = new LinkedList<CustomRequirement>();
    private final List<CustomReward> customRewards = new LinkedList<CustomReward>();
    private final List<CustomObjective> customObjectives = new LinkedList<CustomObjective>();
    private Collection<Quester> questers = new ConcurrentSkipListSet<Quester>();
    private final Collection<Quest> quests = new ConcurrentSkipListSet<Quest>();
    private Collection<Action> actions = new ConcurrentSkipListSet<Action>();
    private Collection<Condition> conditions = new ConcurrentSkipListSet<Condition>();
    private LinkedList<NPC> questNpcs = new LinkedList<NPC>();
    private CommandExecutor cmdExecutor;
    private ConversationFactory conversationFactory;
    private ConversationFactory npcConversationFactory;
    private QuestFactory questFactory;
    private ActionFactory actionFactory;
    private ConditionFactory conditionFactory;
    private BlockListener blockListener;
    private ItemListener itemListener;
    private NpcListener npcListener;
    private PlayerListener playerListener;
    private NpcEffectThread effectThread;
    private PlayerMoveThread moveThread;
    private DungeonsListener dungeonsListener;
    private PartiesListener partiesListener;
    private DenizenTrigger trigger;
    private LocaleManager localeManager;
    private Storage storage;

    @Override
    public void onEnable() {
        /****** WARNING: ORDER OF STEPS MATTERS ******/

        // 1 - Initialize variables
        bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];
        settings = new Settings(this);
        localeManager = new LocaleManager();
        blockListener = new BlockListener(this);
        itemListener = new ItemListener(this);
        npcListener = new NpcListener(this);
        playerListener = new PlayerListener(this);
        dungeonsListener = new DungeonsListener();
        partiesListener = new PartiesListener();
        effectThread = new NpcEffectThread(this);
        moveThread = new PlayerMoveThread(this);
        questFactory = new QuestFactory(this);
        actionFactory = new ActionFactory(this);
        conditionFactory = new ConditionFactory(this);
        depends = new Dependencies(this);
        trigger = new DenizenTrigger(this);
        final Metrics metrics = new Metrics(this);
        metrics.addCustomChart(new Metrics.SimplePie("language", () -> Lang.getISO()));

        // 2 - Load main config
        settings.init();
        
        // 3 - Setup language files
        try {
            setupLang();
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
        
        // 4 - Load command executor
        cmdExecutor = new CmdExecutor(this);
        
        // 5 - Load soft-depends
        depends.init();
        
        // 6 - Save resources from jar
        saveResourceAs("quests.yml", "quests.yml", false);
        saveResourceAs("actions.yml", "actions.yml", false);
        saveResourceAs("conditions.yml", "conditions.yml", false);
        
        // 7 - Save config with any new options
        getConfig().options().copyDefaults(true);
        saveConfig();
        final StorageFactory storageFactory = new StorageFactory(this);
        storage = storageFactory.getInstance();
        
        // 8 - Setup commands
        getCommand("quests").setExecutor(cmdExecutor);
        getCommand("questadmin").setExecutor(cmdExecutor);
        getCommand("quest").setExecutor(cmdExecutor);
        
        // 9 - Build conversation factories
        this.conversationFactory = new ConversationFactory(this).withModality(false)
                .withPrefix(new ConversationPrefix() {
                    @Override
                    public String getPrefix(final ConversationContext context) {
                        return ChatColor.GRAY.toString();
                    }
                })
                .withFirstPrompt(new QuestAcceptPrompt()).withTimeout(settings.getAcceptTimeout())
                .thatExcludesNonPlayersWithMessage("Console may not perform this conversation!")
                .addConversationAbandonedListener(this);
        this.npcConversationFactory = new ConversationFactory(this).withModality(false)
                .withFirstPrompt(new NpcOfferQuestPrompt()).withTimeout(settings.getAcceptTimeout())
                .withLocalEcho(false).addConversationAbandonedListener(this);
        
        // 10 - Register listeners
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(itemListener, this);
        depends.linkCitizens();
        getServer().getPluginManager().registerEvents(playerListener, this);
        if (settings.getStrictPlayerMovement() > 0) {
            final long ticks = settings.getStrictPlayerMovement() * 20;
            getServer().getScheduler().scheduleSyncRepeatingTask(this, moveThread, ticks, ticks);
        }
        if (depends.getDungeonsApi() != null) {
            getServer().getPluginManager().registerEvents(dungeonsListener, this);
        }
        if (depends.getPartiesApi() != null) {
            getServer().getPluginManager().registerEvents(partiesListener, this);
        }
        
        // 11 - Delay loading of Quests, Actions and modules
        delayLoadQuestInfo(5L);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Saving Quester data...");
        for (final Player p : getServer().getOnlinePlayers()) {
            final Quester quester = getQuester(p.getUniqueId());
            quester.saveData();
        }
        getLogger().info("Closing storage...");
        storage.close();
    }
    
    public boolean isLoading() {
        return loading;
    }
    
    public String getDetectedBukkitVersion() {
        return bukkitVersion;
    }
    
    public Dependencies getDependencies() {
        return depends;
    }
    
    public Settings getSettings() {
        return settings;
    }
    
    public List<CustomRequirement> getCustomRequirements() {
        return customRequirements;
    }

    public Optional<CustomRequirement> getCustomRequirement(final String className) {
        for (int i = 0; i < customRequirements.size(); i++) {
            final CustomRequirement cr = customRequirements.get(i);
            if (cr.getClass().getName().equals(className)) {
                return Optional.of(cr);
            }
        }
        return Optional.empty();
    }
    
    public List<CustomReward> getCustomRewards() {
        return customRewards;
    }
    
    public Optional<CustomReward> getCustomReward(final String className) {
        for (int i = 0; i < customRewards.size(); i++) {
            final CustomReward cr = customRewards.get(i);
            if (cr.getClass().getName().equals(className)) {
                return Optional.of(cr);
            }
        }
        return Optional.empty();
    }
    
    public List<CustomObjective> getCustomObjectives() {
        return customObjectives;
    }
    
    public Optional<CustomObjective> getCustomObjective(final String className) {
        for (int i = 0; i < customObjectives.size(); i++) {
            final CustomObjective co = customObjectives.get(i);
            if (co.getClass().getName().equals(className)) {
                return Optional.of(co);
            }
        }
        return Optional.empty();
    }
    
    /**
     * Get every Quest loaded in memory
     * 
     * @deprecated Use {@link #getLoadedQuests()}
     * @return a list of all Quests
     */
    @Deprecated
    public LinkedList<Quest> getQuests() {
        return new LinkedList<>(quests);
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
     * Get every Action loaded in memory
     * 
     * @deprecated Use {@link #getLoadedActions()}
     * @return a list of all Actions
     */
    @Deprecated
    public LinkedList<Action> getActions() {
        return new LinkedList<>(actions);
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
     * @deprecated Use {@link #setLoadedActions(Collection)}
     */
    @Deprecated
    public void setActions(final LinkedList<Action> actions) {
        this.actions = actions;
    }
    
    /**
     * Set every Action loaded in memory
     * 
     */
    public void setLoadedActions(final Collection<Action> actions) {
        this.actions = actions;
    }
    
    /**
     * Get every Action loaded in memory
     * 
     * @deprecated Use {@link #getLoadedConditions()}
     * @return a list of all Conditions
     */
    @Deprecated
    public LinkedList<Condition> getConditions() {
        return new LinkedList<>(conditions);
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
     * @deprecated Use {@link #setLoadedConditions(Collection)}
     */
    @Deprecated
    public void setConditions(final LinkedList<Condition> conditions) {
        this.conditions = conditions;
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
     * @return Quester, or null if UUID is null
     */
    public Quester getQuester(final UUID id) {
        if (id == null) {
            return null;
        }
        final ConcurrentSkipListSet<Quester> set = (ConcurrentSkipListSet<Quester>) questers;
        for (final Quester q: set) {
            if (q != null && q.getUUID().equals(id)) {
                return q;
            }
        }
        final Quester quester = new Quester(this, id);
        if (depends.getCitizens() != null) {
            if (depends.getCitizens().getNPCRegistry().getByUniqueId(id) != null) {
                return quester;
            }
        }
        /*if (!quester.loadData()) {
            set.add(quester);
        } else {
            set.remove(quester);
        }*/
        final Quester q = new Quester(this, id);
        questers.add(q);
        return q;
    }
    
    /**
     * Get every Quester that has ever played on this server
     * 
     * @deprecated Use {@link #getOfflineQuesters()}
     * @return a list of all Questers
     */
    @Deprecated
    public LinkedList<Quester> getQuesters() {
        return new LinkedList<>(questers);
    }
    
    /**
     * Set every Quester that has ever played on this server
     * 
     * @deprecated Use {@link #setOfflineQuesters(Collection)}
     * @param questers a list of Questers
     */
    @Deprecated
    public void setQuesters(final LinkedList<Quester> questers) {
        this.questers = new ConcurrentSkipListSet<>(questers);
    }

    /**
     * Get every online Quester playing on this server
     * 
     * @return a collection of all online Questers
     */
    public Collection<Quester> getOnlineQuesters() {
        final Collection<Quester> questers = new ConcurrentSkipListSet<Quester>();;
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
    
    public LinkedList<NPC> getQuestNpcs() {
        return questNpcs;
    }
    
    public void setQuestNpcs(final LinkedList<NPC> questNpcs) {
        this.questNpcs = questNpcs;
    }
    
    public ConversationFactory getConversationFactory() {
        return conversationFactory;
    }
    
    public ConversationFactory getNpcConversationFactory() {
        return npcConversationFactory;
    }
    
    public QuestFactory getQuestFactory() {
        return questFactory;
    }
    
    public ActionFactory getActionFactory() {
        return actionFactory;
    }
    
    public ConditionFactory getConditionFactory() {
        return conditionFactory;
    }
    
    public BlockListener getBlockListener() {
        return blockListener;
    }
    
    public ItemListener getItemListener() {
        return itemListener;
    }
    
    public NpcListener getNpcListener() {
        return npcListener;
    }
    
    public PlayerListener getPlayerListener() {
        return playerListener;
    }
    
    public DungeonsListener getDungeonsListener() {
        return dungeonsListener;
    }
    
    public PartiesListener getPartiesListener() {
        return partiesListener;
    }
    
    public NpcEffectThread getNpcEffectThread() {
        return effectThread;
    }
    
    public DenizenTrigger getDenizenTrigger() {
        return trigger;
    }
    
    public LocaleManager getLocaleManager() {
        return localeManager;
    }
    
    public Storage getStorage() {
        return storage;
    }

    @Override
    public void conversationAbandoned(final ConversationAbandonedEvent abandonedEvent) {
        if (abandonedEvent.gracefulExit() == false) {
            if (abandonedEvent.getContext().getForWhom() != null) {
                try {
                    abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.YELLOW 
                            + Lang.get((Player) abandonedEvent.getContext().getForWhom(), "questTimeout"));
                } catch (final Exception e) {
                    // Do nothing
                }
            }
        }
    }

    private class QuestAcceptPrompt extends StringPrompt {

        @Override
        public String getPromptText(final ConversationContext context) {
            return ChatColor.YELLOW + Lang.get((Player) context.getForWhom(), "acceptQuest") + "  " + ChatColor.GREEN 
                    + Lang.get("yesWord") + " / " + Lang.get("noWord");
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String s) {
            final Player player = (Player) context.getForWhom();
            if (s.equalsIgnoreCase(Lang.get(player, "yesWord"))) {
                Quester quester = getQuester(player.getUniqueId());
                if (quester == null) {
                    // Must be new player
                    quester = new Quester(Quests.this, player.getUniqueId());
                    if (quester.saveData()) {
                        getLogger().info("Created new data for player " + player.getName());
                    } else {
                        player.sendMessage(ChatColor.RED + Lang.get(player, "questSaveError"));
                    }
                }
                final String questIdToTake = quester.questIdToTake;
                try {
                    if (getQuestById(questIdToTake) == null) {
                        getLogger().info(player.getName() + " attempted to take quest ID \"" + questIdToTake 
                                + "\" but something went wrong");
                        player.sendMessage(ChatColor.RED 
                                + "Something went wrong! Please report issue to an administrator.");
                    } else {
                        getQuester(player.getUniqueId()).takeQuest(getQuestById(questIdToTake), false);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return Prompt.END_OF_CONVERSATION;
            } else if (s.equalsIgnoreCase(Lang.get("noWord"))) {
                player.sendMessage(ChatColor.YELLOW + Lang.get("cancelled"));
                return Prompt.END_OF_CONVERSATION;
            } else {
                final String msg = Lang.get(player, "questInvalidChoice")
                    .replace("<yes>", Lang.get(player, "yesWord"))
                    .replace("<no>", Lang.get(player, "noWord"));
                player.sendMessage(ChatColor.RED + msg);
                return new QuestAcceptPrompt();
            }
        }
    }
    
    /**
     * Transfer language files from jar to disk
     */
    private void setupLang() throws IOException, URISyntaxException {
        final String path = "lang";
        final File jarFile = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        if(jarFile.isFile()) {
            final JarFile jar = new JarFile(jarFile);
            final Enumeration<JarEntry> entries = jar.entries();
            final Set<String> results = new HashSet<String>();
            while(entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (name.startsWith(path + "/") && name.contains("strings.yml")) {
                    results.add(name);
                }
            }
            for (final String resourcePath : results) {
                saveResourceAs(resourcePath, resourcePath, false);
                saveResourceAs(resourcePath, resourcePath.replace(".yml", "_new.yml"), true);
            }
            jar.close();
        }
        try {
            Lang.init(this);
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Save a Quests plugin resource to a specific path in the filesystem
     * 
     * @param resourcePath jar file location starting from resource folder, i.e. "lang/el-GR/strings.yml"
     * @param outputPath file destination starting from Quests folder, i.e. "lang/el-GR/strings.yml"
     * @param replace whether or not to replace the destination file
     */
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
     * Load quests, actions, and modules after specified delay<p>
     * 
     * At startup, this lets soft-depends (namely Citizens) fully load first
     */
    private void delayLoadQuestInfo(final long ticks) {
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

            @Override
            public void run() {
                loadQuests();
                loadActions();
                loadConditions();
                getLogger().log(Level.INFO, "Loaded " + quests.size() + " Quest(s), " + actions.size() + " Action(s), "
                        + conditions.size() + " Condition(s) and " + Lang.size() + " Phrase(s)");
                for (final Player p : getServer().getOnlinePlayers()) {
                    final Quester quester =  new Quester(Quests.this, p.getUniqueId());
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
                loadModules();
                importQuests();
                if (getSettings().canDisableCommandFeedback()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule sendCommandFeedback false");
                }
                loading = false;
            }
        }, ticks);
    }
    
    private void importQuests() {
        final File f = new File(this.getDataFolder(), "import");
        if (f.exists() && f.isDirectory()) {
            final File[] imports = f.listFiles();
            if (imports != null) {
                for (final File file : imports) {
                    if (file.isDirectory() == false && file.getName().endsWith(".yml")) {
                        importQuest(file);
                    }
                }
            }
        } else {
            f.mkdir();
        }
    }
    
    private void importQuest(final File file) {
        FileConfiguration config = null;
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (config != null) {
            final ConfigurationSection questsSection = config.getConfigurationSection("quests");
            if (questsSection == null) {
                getLogger().severe("Missing \'quests\' section marker, canceled import of file " + file.getName());
                return;
            }
            int count = 0;
            for (final String questKey : questsSection.getKeys(false)) {
                try {
                    for (final Quest lq : getLoadedQuests()) {
                        if (lq.getId().equals(questKey)) {
                            throw new QuestFormatException("id already exists", questKey);
                        }
                    }
                    final Quest quest = loadQuest(config, questKey);
                    if (config.contains("quests." + questKey + ".requirements")) {
                        loadQuestRequirements(config, questsSection, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".planner")) {
                        loadQuestPlanner(config, questsSection, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".options")) {
                        loadQuestOptions(config, questsSection, quest, questKey);
                    }
                    quest.plugin = this;
                    loadQuestStages(quest, config, questKey);
                    loadQuestRewards(config, quest, questKey);
                    quests.add(quest);
                    count++;
                } catch (final QuestFormatException e) {
                    e.printStackTrace();
                    continue;
                } catch (final StageFormatException e) {
                    e.printStackTrace();
                    continue;
                } catch (final ActionFormatException e) {
                    e.printStackTrace();
                    continue;
                } catch (final ConditionFormatException e) {
                    e.printStackTrace();
                    continue;
                }
            }
            if (count > 0) {
                getLogger().info("Imported " + count + " Quests from " + file.getName());
            }
        } else {
            getLogger().severe("Unable to import quest file " + file.getName());
        }
    }
    
    /**
     * Load modules from file
     */
    public void loadModules() {
        final File f = new File(this.getDataFolder(), "modules");
        if (f.exists() && f.isDirectory()) {
            final File[] modules = f.listFiles();
            if (modules != null) {
                for (final File module : modules) {
                    if (module.isDirectory() == false && module.getName().endsWith(".jar")) {
                        loadModule(module);
                    }
                }
            }
        } else {
            f.mkdir();
        }
        boolean failedToLoad;
        FileConfiguration config = null;
        final File file = new File(this.getDataFolder(), "quests.yml");
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (config != null) {
            ConfigurationSection questsSection;
            if (config.contains("quests")) {
                questsSection = config.getConfigurationSection("quests");
                for (final String questKey : questsSection.getKeys(false)) {
                    try {
                        failedToLoad = false;
                        if (config.contains("quests." + questKey)) {
                            loadCustomSections(getQuestById(questKey), config, questKey);
                        } else {
                            throw new QuestFormatException("Quest block is missing", questKey);
                        }
                        if (failedToLoad) {
                            getLogger().log(Level.SEVERE, "Failed to load Quest \"" + questKey + "\". Skipping.");
                        }
                    } catch (final QuestFormatException ex) {
                        continue;
                    } catch (final StageFormatException ex) {
                        continue;
                    }
                }
            }
        } else {
            getLogger().severe("Unable to load module data from quests.yml");
        }
    }

    /**
     * Load the specified jar as a module
     * 
     * @param jar A custom reward/requirement/objective jar
     */
    public void loadModule(final File jar) {
        try {
            @SuppressWarnings("resource")
            final
            JarFile jarFile = new JarFile(jar);
            final Enumeration<JarEntry> e = jarFile.entries();
            final URL[] urls = { new URL("jar:file:" + jar.getPath() + "!/") };
            final ClassLoader cl = URLClassLoader.newInstance(urls, getClassLoader());
            int count = 0;
            while (e.hasMoreElements()) {
                final JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                final Class<?> c = Class.forName(className, true, cl);
                if (CustomRequirement.class.isAssignableFrom(c)) {
                    final Class<? extends CustomRequirement> requirementClass = c.asSubclass(CustomRequirement.class);
                    final Constructor<? extends CustomRequirement> cstrctr = requirementClass.getConstructor();
                    final CustomRequirement requirement = cstrctr.newInstance();
                    final Optional<CustomRequirement>oo=getCustomRequirement(requirement.getClass().getName());
                    if (oo.isPresent()) customRequirements.remove(oo.get());
                    customRequirements.add(requirement);
                    final String name = requirement.getName() == null ? "[" + jar.getName() + "]" : requirement.getName();
                    final String author = requirement.getAuthor() == null ? "[Unknown]" : requirement.getAuthor();
                    count++;
                    getLogger().info("Loaded Module: " + name + " by " + author);
                } else if (CustomReward.class.isAssignableFrom(c)) {
                    final Class<? extends CustomReward> rewardClass = c.asSubclass(CustomReward.class);
                    final Constructor<? extends CustomReward> cstrctr = rewardClass.getConstructor();
                    final CustomReward reward = cstrctr.newInstance();
                    final Optional<CustomReward>oo=getCustomReward(reward.getClass().getName());
                    if (oo.isPresent()) customRewards.remove(oo.get());
                    customRewards.add(reward);
                    final String name = reward.getName() == null ? "[" + jar.getName() + "]" : reward.getName();
                    final String author = reward.getAuthor() == null ? "[Unknown]" : reward.getAuthor();
                    count++;
                    getLogger().info("Loaded Module: " + name + " by " + author);
                } else if (CustomObjective.class.isAssignableFrom(c)) {
                    final Class<? extends CustomObjective> objectiveClass = c.asSubclass(CustomObjective.class);
                    final Constructor<? extends CustomObjective> cstrctr = objectiveClass.getConstructor();
                    final CustomObjective objective = cstrctr.newInstance();
                    final Optional<CustomObjective>oo=getCustomObjective(objective.getClass().getName());
                    if (oo.isPresent()) {
                        HandlerList.unregisterAll(oo.get());
                        customObjectives.remove(oo.get());
                    }
                    customObjectives.add(objective);
                    final String name = objective.getName() == null ? "[" + jar.getName() + "]" : objective.getName();
                    final String author = objective.getAuthor() == null ? "[Unknown]" : objective.getAuthor();
                    count++;
                    getLogger().info("Loaded Module: " + name + " by " + author);
                    try {
                        getServer().getPluginManager().registerEvents(objective, this);
                        getLogger().info("Registered events for custom objective \"" + name + "\"");
                    } catch (final Exception ex) {
                        getLogger().warning("Failed to register events for custom objective \"" + name 
                                + "\". Does the objective class listen for events?");
                        ex.printStackTrace();
                    }
                }
            }
            if (count == 0) {
                getLogger().severe("Unable to load module from file: " + jar.getName() 
                        + ", jar file is not a valid module!");
            }
        } catch (final Exception e) {
            getLogger().severe("Unable to load module from file: " + jar.getName());
            e.printStackTrace();
        }
    }

    /**
     * Show all of a player's objectives for the current stage of a quest.<p>
     * 
     * Respects PlaceholderAPI and translations, when enabled.
     * 
     * @param quest The quest to get current stage objectives of
     * @param quester The player to show current stage objectives to
     * @param ignoreOverrides Whether to ignore objective-overrides
     */
    @SuppressWarnings("deprecation")
    public void showObjectives(final Quest quest, final Quester quester, final boolean ignoreOverrides) {
        if (quest == null) {
            getLogger().severe("Quest was null when getting objectives for " + quester.getLastKnownName());
            return;
        }
        if (quester.getQuestData(quest) == null) {
            getLogger().warning("Quest data was null when showing objectives for " + quest.getName());
            return;
        }
        if (quester.getCurrentStage(quest) == null) {
            getLogger().warning("Current stage was null when showing objectives for " + quest.getName());
            return;
        }
        if (!ignoreOverrides && !quester.getCurrentStage(quest).objectiveOverrides.isEmpty()) {
            for (final String s: quester.getCurrentStage(quest).objectiveOverrides) {
                String message = ChatColor.GREEN + (s.trim().length() > 0 ? "- " : "") + ConfigUtil
                        .parseString(ChatColor.translateAlternateColorCodes('&', s), quest, quester.getPlayer());
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                quester.sendMessage(message);
            }
            return;
        }
        final QuestData data = quester.getQuestData(quest);
        final Stage stage = quester.getCurrentStage(quest);
        for (final ItemStack e : stage.blocksToBreak) {
            for (final ItemStack e2 : data.blocksBroken) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "break");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                    }
                    if (getSettings().canTranslateNames() && !e.hasItemMeta() && !e.getItemMeta().hasDisplayName()) {
                        localeManager.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                    } else {
                        quester.sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                    }
                }
            }
        }
        for (final ItemStack e : stage.blocksToDamage) {
            for (final ItemStack e2 : data.blocksDamaged) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "damage");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                    }
                    if (getSettings().canTranslateNames() && !e.hasItemMeta() && !e.getItemMeta().hasDisplayName()) {
                        localeManager.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                    } else {
                        quester.sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                    }
                }
            }
        }
        for (final ItemStack e : stage.blocksToPlace) {
            for (final ItemStack e2 : data.blocksPlaced) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "place");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                    }
                    if (getSettings().canTranslateNames() && !e.hasItemMeta() && !e.getItemMeta().hasDisplayName()) {
                        localeManager.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                    } else {
                        quester.sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                    }
                }
            }
        }
        for (final ItemStack e : stage.blocksToUse) {
            for (final ItemStack e2 : data.blocksUsed) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "use");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                    }
                    if (getSettings().canTranslateNames() && !e.hasItemMeta() && !e.getItemMeta().hasDisplayName()) {
                        localeManager.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                    } else {
                        quester.sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                    }
                }
            }
        }
        for (final ItemStack e : stage.blocksToCut) {
            for (final ItemStack e2 : data.blocksCut) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "cut");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                    }
                    if (getSettings().canTranslateNames() && !e.hasItemMeta() && !e.getItemMeta().hasDisplayName()) {
                        localeManager.sendMessage(quester.getPlayer(), message, e.getType(), e.getDurability(), null);
                    } else {
                        quester.sendMessage(message.replace("<item>", ItemUtil.getName(e)));
                    }
                }
            }
        }
        int craftIndex = 0;
        for (final ItemStack is : stage.itemsToCraft) {
            int crafted = 0;
            if (data.itemsCrafted.size() > craftIndex) {
                crafted = data.itemsCrafted.get(craftIndex).getAmount();
            }
            final int amt = is.getAmount();
            craftIndex++;
            final ChatColor color = crafted < amt ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "craftItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + crafted + "/" + is.getAmount());
            } else {
                // Legacy
                message += color + ": " + crafted + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            if (getSettings().canTranslateNames() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments());
            } else {
                quester.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        }
        int smeltIndex = 0;
        for (final ItemStack is : stage.itemsToSmelt) {
            int smelted = 0;
            if (data.itemsSmelted.size() > smeltIndex) {
                smelted = data.itemsSmelted.get(smeltIndex).getAmount();
            }
            final int amt = is.getAmount();
            smeltIndex++;
            final ChatColor color = smelted < amt ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "smeltItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + smelted + "/" + is.getAmount());
            } else {
                // Legacy
                message += color + ": " + smelted + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            if (getSettings().canTranslateNames() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments());
            } else {
                quester.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        }
        int enchantIndex = 0;
        for (final ItemStack is : stage.itemsToEnchant) {
            int enchanted = 0;
            if (data.itemsEnchanted.size() > enchantIndex) {
                enchanted = data.itemsEnchanted.get(enchantIndex).getAmount();
            }
            final int amt = is.getAmount();
            enchantIndex++;
            final ChatColor color = enchanted < amt ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "enchItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + enchanted + "/" + is.getAmount());
            } else {
                // Legacy
                message += color + ": " + enchanted + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            if (getSettings().canTranslateNames() && is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments(), is.getItemMeta());
            } else if (getSettings().canTranslateNames() && !is.hasItemMeta() 
                    && Material.getMaterial("LINGERING_POTION") == null) {
                // Bukkit version is below 1.9
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments());
            } else {
                if (is.getEnchantments().isEmpty()) {
                    quester.sendMessage(message.replace("<item>", ItemUtil.getName(is))
                            .replace("<enchantment>", "")
                            .replace("<level>", "")
                            .replaceAll("\\s+", " "));
                } else {
                    for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                        quester.sendMessage(message.replace("<item>", ItemUtil.getName(is))
                                .replace("<enchantment>", ItemUtil.getPrettyEnchantmentName(e.getKey()))
                                .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                    }
                }
            }
        }
        int brewIndex = 0;
        for (final ItemStack is : stage.itemsToBrew) {
            int brewed = 0;
            if (data.itemsBrewed.size() > brewIndex) {
                brewed = data.itemsBrewed.get(brewIndex).getAmount();
            }
            final int amt = is.getAmount();
            brewIndex++;
            final ChatColor color = brewed < amt ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "brewItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + brewed + "/" + is.getAmount());
            } else {
                // Legacy
                message += color + ": " + brewed + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            if (getSettings().canTranslateNames() && is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments(), is.getItemMeta());
            } else if (getSettings().canTranslateNames() && !is.hasItemMeta() 
                    && Material.getMaterial("LINGERING_POTION") == null) {
                // Bukkit version is below 1.9
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments());
            } else {
                quester.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        }
        int consumeIndex = 0;
        for (final ItemStack is : stage.itemsToConsume) {
            int consumed = 0;
            if (data.itemsConsumed.size() > consumeIndex) {
                consumed = data.itemsConsumed.get(consumeIndex).getAmount();
            }
            final int amt = is.getAmount();
            consumeIndex++;
            final ChatColor color = consumed < amt ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "consumeItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + consumed + "/" + is.getAmount());
            } else {
                // Legacy
                message += color + ": " + consumed + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            if (getSettings().canTranslateNames() && is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments(), is.getItemMeta());
            } else if (getSettings().canTranslateNames() && !is.hasItemMeta() 
                    && Material.getMaterial("LINGERING_POTION") == null) {
                // Bukkit version is below 1.9
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments());
            } else {
                quester.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        }
        
        int index = 0;
        for (final ItemStack is : stage.itemsToDeliver) {
            int delivered = 0;
            if (data.itemsDelivered.size() > index) {
                delivered = data.itemsDelivered.get(index).getAmount();
            }
            final int toDeliver = is.getAmount();
            final Integer npc = stage.itemDeliveryTargets.get(index);
            index++;
            final ChatColor color = delivered < toDeliver ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "deliver").replace("<npc>", depends.getNPCName(npc));
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + delivered + "/" + toDeliver);
            } else {
                // Legacy
                message += color + ": " + delivered + "/" + toDeliver;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            if (getSettings().canTranslateNames() && !is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                localeManager.sendMessage(quester.getPlayer(), message, is.getType(), is.getDurability(), 
                        is.getEnchantments());
            } else {
                quester.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        }
        for (final Integer n : stage.citizensToInteract) {
            for (final Entry<Integer, Boolean> e : data.citizensInteracted.entrySet()) {
                if (e.getKey().equals(n)) {
                    final ChatColor color = e.getValue() == false ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "talkTo")
                            .replace("<npc>", depends.getNPCName(n));
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                    }
                    quester.sendMessage(message);
                }
            }
        }
        for (final Integer n : stage.citizensToKill) {
            for (final Integer n2 : data.citizensKilled) {
                if (n.equals(n2)) {
                    if (data.citizenNumKilled.size() > data.citizensKilled.indexOf(n2) 
                            && stage.citizenNumToKill.size() > stage.citizensToKill.indexOf(n)) {
                        final ChatColor color = data.citizenNumKilled.get(data.citizensKilled.indexOf(n2)) 
                                < stage.citizenNumToKill.get(stage.citizensToKill.indexOf(n)) 
                                ? ChatColor.GREEN : ChatColor.GRAY;
                        String message = color + "- " + Lang.get(quester.getPlayer(), "kill");
                        if (message.contains("<mob>")) {
                            message = message.replace("<mob>", depends.getNPCName(n));
                        } else {
                            message += " " + depends.getNPCName(n);
                        }
                        if (message.contains("<count>")) {
                            message = message.replace("<count>", "" + color 
                                    + data.citizenNumKilled.get(stage.citizensToKill.indexOf(n)) + "/" 
                                    + stage.citizenNumToKill.get(stage.citizensToKill.indexOf(n)));
                        } else {
                            // Legacy
                            message += color + ": " + data.citizenNumKilled.get(stage.citizensToKill.indexOf(n)) + "/" 
                                    + stage.citizenNumToKill.get(stage.citizensToKill.indexOf(n));
                        }
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        quester.sendMessage(message);
                    }
                }
            }
        }
        if (stage.cowsToMilk != null) {
            final ChatColor color = data.getCowsMilked() < stage.cowsToMilk ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "milkCow");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + data.getCowsMilked() + "/" + stage.cowsToMilk);
            } else {
                // Legacy
                message += color + ": " + data.getCowsMilked() + "/" + stage.cowsToMilk;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            quester.sendMessage(message);
        }
        if (stage.fishToCatch != null) {
            final ChatColor color = data.getFishCaught() < stage.fishToCatch ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "catchFish");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + data.getFishCaught() + "/" + stage.fishToCatch);
            } else {
                // Legacy
                message += color + ": " + data.getFishCaught() + "/" + stage.fishToCatch;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            quester.sendMessage(message);
        }
        for (final EntityType e : stage.mobsToKill) {
            for (final EntityType e2 : data.mobsKilled) {
                if (e == e2) {
                    if (data.mobNumKilled.size() > data.mobsKilled.indexOf(e2) 
                            && stage.mobNumToKill.size() > stage.mobsToKill.indexOf(e)) {
                        final ChatColor color = data.mobNumKilled.get(data.mobsKilled.indexOf(e2)) 
                                < stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)) 
                                ? ChatColor.GREEN : ChatColor.GRAY;
                        String message = color + "- ";
                        if (stage.locationsToKillWithin.isEmpty()) {
                            message += Lang.get(quester.getPlayer(), "kill");
                            if (message.contains("<count>")) {
                                message = message.replace("<count>", "" + color 
                                        + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) + "/" 
                                        + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e))));
                            } else {
                                // Legacy
                                message += ChatColor.AQUA + " <mob>" + color + ": "
                                        + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) + "/" 
                                        + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)));
                            }
                        } else {
                            message += Lang.get(quester.getPlayer(), "killAtLocation").replace("<location>", 
                                    stage.killNames.get(stage.mobsToKill.indexOf(e)));
                            if (message.contains("<count>")) {
                                message = message.replace("<count>", "" + color 
                                        + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) + "/" 
                                        + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e))));
                            } else {
                                message += color + ": " + (data.mobNumKilled.get(data.mobsKilled.indexOf(e2))) + "/" 
                                        + (stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)));
                            }
                        }
                        if (depends.getPlaceholderApi() != null) {
                            message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                        }
                        if (getSettings().canTranslateNames()) {
                            localeManager.sendMessage(quester.getPlayer(), message, e, null);
                        } else {
                            quester.sendMessage(message.replace("<mob>", MiscUtil.getProperMobName(e)));
                        }
                    }
                }
            }
        }
        if (stage.playersToKill != null) {
            final ChatColor color = data.getPlayersKilled() < stage.playersToKill ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "- " + Lang.get(quester.getPlayer(), "killPlayer");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + data.getPlayersKilled() + "/" + stage.playersToKill);
            } else {
                // Legacy
                message += color + ": " + data.getPlayersKilled() + "/" + stage.playersToKill;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
            }
            quester.sendMessage(message);
        }
        for (final Entry<EntityType, Integer> e : stage.mobsToTame.entrySet()) {
            for (final Entry<EntityType, Integer> e2 : data.mobsTamed.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    final ChatColor color = e2.getValue() < e.getValue() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "tame");
                    if (!message.contains("<mob>")) {
                        message += " <mob>";
                    }
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getValue() + "/" + e.getValue());
                    } else {
                        // Legacy
                        message += color + ": " + e2.getValue() + "/" + e.getValue();
                    }
                    if (getSettings().canTranslateNames()) {
                        localeManager.sendMessage(quester.getPlayer(), message, e.getKey(), null);
                    } else {
                        quester.sendMessage(message.replace("<mob>", 
                                MiscUtil.getProperMobName(e.getKey())));
                    }
                }
            }
        }
        for (final Entry<DyeColor, Integer> e : stage.sheepToShear.entrySet()) {
            for (final Entry<DyeColor, Integer> e2 : data.sheepSheared.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    final ChatColor color = e2.getValue() < e.getValue() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + "- " + Lang.get(quester.getPlayer(), "shearSheep");
                    message = message.replace("<color>", MiscUtil.getPrettyDyeColorName(e.getKey()));
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getValue() + "/" + e.getValue());
                    } else {
                        // Legacy
                        message += color + ": " + e2.getValue() + "/" + e.getValue();
                    }
                    quester.sendMessage(message);
                }
            }
        }
        for (int i = 0 ; i < stage.locationsToReach.size(); i++) {
            if (i < data.hasReached.size()) {
                final ChatColor color = data.hasReached.get(i) == false ? ChatColor.GREEN : ChatColor.GRAY;
                String message = color + Lang.get(quester.getPlayer(), "goTo");
                message = message.replace("<location>", stage.locationNames.get(i));
                quester.sendMessage(message);
            }
        }
        for (final String s : stage.passwordDisplays) {
            if (data.passwordsSaid.containsKey(s)) {
                final Boolean b = data.passwordsSaid.get(s);
                final ChatColor color = b != null && b == false ? ChatColor.GREEN : ChatColor.GRAY;
                final String message = color + "- " + s;
                quester.sendMessage(message);
            }
        }
        for (final CustomObjective co : stage.customObjectives) {
            int countsIndex = 0;
            String display = co.getDisplay();
            final List<String> unfinished = new LinkedList<String>();
            final List<String> finished = new LinkedList<String>();
            for (final Entry<String, Integer> entry : data.customObjectiveCounts.entrySet()) {
                if (co.getName().equals(entry.getKey())) {
                    for (final Entry<String,Object> prompt : co.getData()) {
                        final String replacement = "%" + prompt.getKey() + "%";
                        try {
                            for (final Entry<String, Object> e : stage.customObjectiveData) {
                                if (e.getKey().equals(prompt.getKey())) {
                                    if (display.contains(replacement)) {
                                        display = display.replace(replacement, ((String) e.getValue()));
                                    }
                                }
                            }
                        } catch (final NullPointerException ne) {
                            getLogger().severe("Unable to fetch display for " + co.getName() + " on " 
                                    + quest.getName());
                            ne.printStackTrace();
                        }
                    }
                    if (entry.getValue() < stage.customObjectiveCounts.get(countsIndex)) {
                        if (co.canShowCount()) {
                            display = display.replace("%count%", entry.getValue() + "/" 
                                    + stage.customObjectiveCounts.get(countsIndex));
                        }
                        unfinished.add(ChatColor.translateAlternateColorCodes('&', display));
                    } else {
                        if (co.canShowCount()) {
                            display = display.replace("%count%", stage.customObjectiveCounts.get(countsIndex) 
                                    + "/" + stage.customObjectiveCounts.get(countsIndex));
                        }
                        finished.add(ChatColor.translateAlternateColorCodes('&', display));
                    }
                }
                countsIndex++;
            }
            for (final String s : unfinished) {
                quester.sendMessage(ChatColor.GREEN + "- " + s);
            }
            for (final String s : finished) {
                quester.sendMessage(ChatColor.GRAY + "- " + s);
            }
        }
    }
    
    /**
     * Show the player a list of their quests
     * 
     * @param quester Quester to show the list
     * @param page Page to display, with 7 quests per page
     */
    public void listQuests(final Quester quester, final int page) {
        // Although we could copy the quests list to a new object, we instead opt to
        // duplicate code to improve efficiency if ignore-locked-quests is set to 'false'
        final int rows = 7;
        final Player player = quester.getPlayer();
        if (getSettings().canIgnoreLockedQuests()) {
            final LinkedList<Quest> available = new LinkedList<Quest>();
            for (final Quest q : quests) {
                if (!quester.getCompletedQuests().contains(q)) {
                    if (q.testRequirements(player)) {
                        available.add(q);
                    }
                } else if (q.getPlanner().hasCooldown() && quester.getRemainingCooldown(q) < 0) {
                    if (q.testRequirements(player)) {
                        available.add(q);
                    }
                }
            }
            if ((available.size() + rows) <= (page * rows) || available.size() == 0) {
                quester.sendMessage(ChatColor.YELLOW + Lang.get(player, "pageNotExist"));
            } else {
                quester.sendMessage(ChatColor.GOLD + Lang.get(player, "questListTitle"));
                int fromOrder = (page - 1) * rows;
                List<Quest> subQuests;
                if (available.size() >= (fromOrder + rows)) {
                    subQuests = available.subList((fromOrder), (fromOrder + rows));
                } else {
                    subQuests = available.subList((fromOrder), available.size());
                }
                fromOrder++;
                for (final Quest q : subQuests) {
                    if (quester.canAcceptOffer(q, false)) {
                        quester.sendMessage(ChatColor.YELLOW + Integer.toString(fromOrder) + ". " + q.getName());
                    } else {
                        quester.sendMessage(ChatColor.GRAY + Integer.toString(fromOrder) + ". " + q.getName());
                    }
                    fromOrder++;
                }
                final int numPages = (int) Math.ceil(((double) available.size()) / ((double) rows));
                String msg = Lang.get(player, "pageFooter");
                msg = msg.replace("<current>", String.valueOf(page));
                msg = msg.replace("<all>", String.valueOf(numPages));
                quester.sendMessage(ChatColor.GOLD + msg);
            }
        } else {
            if ((quests.size() + rows) <= (page * rows) || quests.size() == 0) {
                quester.sendMessage(ChatColor.YELLOW + Lang.get(player, "pageNotExist"));
            } else {
                quester.sendMessage(ChatColor.GOLD + Lang.get(player, "questListTitle"));
                int fromOrder = (page - 1) * rows;
                List<Quest> subQuests;
                if (quests.size() >= (fromOrder + rows)) {
                    subQuests = getQuests().subList((fromOrder), (fromOrder + rows));
                } else {
                    subQuests = getQuests().subList((fromOrder), quests.size());
                }
                fromOrder++;
                for (final Quest q : subQuests) {
                    if (quester.canAcceptOffer(q, false)) {
                        quester.sendMessage(ChatColor.YELLOW + Integer.toString(fromOrder) + ". " + q.getName());
                    } else {
                        quester.sendMessage(ChatColor.GRAY + Integer.toString(fromOrder) + ". " + q.getName());
                    }
                    fromOrder++;
                }
                final int numPages = (int) Math.ceil(((double) quests.size()) / ((double) rows));
                String msg = Lang.get(player, "pageFooter");
                msg = msg.replace("<current>", String.valueOf(page));
                msg = msg.replace("<all>", String.valueOf(numPages));
                quester.sendMessage(ChatColor.GOLD + msg);
            }
        }
    }

    /**
     * Reload quests, actions, config settings, lang and modules, and player data
     */
    public void reload(final ReloadCallback<Boolean> callback) {
        loading = true;
        reloadConfig();
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {

            @Override
            public void run() {
                try {
                    for (final Quester quester : questers) {
                        quester.saveData();
                    }
                    Lang.clear();
                    settings.init();
                    Lang.init(Quests.this);
                    quests.clear();
                    actions.clear();
                    conditions.clear();
                    loadQuests();
                    loadActions();
                    loadConditions();
                    for (final Quester quester : questers) {
                        final CompletableFuture<Quester> cf = getStorage().loadQuesterData(quester.getUUID());
                        final Quester loaded = cf.get();
                        for (final Quest q : loaded.currentQuests.keySet()) {
                            loaded.checkQuest(q);
                        }
                    }
                    loadModules();
                    importQuests();
                    if (callback != null) {
                        Bukkit.getScheduler().runTask(Quests.this, new Runnable() {
                            
                            @Override
                            public void run() {
                                loading = false;
                                callback.execute(true);
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        Bukkit.getScheduler().runTask(Quests.this, new Runnable() {
                            
                            @Override
                            public void run() {
                                loading = false;
                                callback.execute(false);
                            }
                        });
                    }
                }
                loading = false;
            }
        });
    }

    /**
     * Load quests from file
     */
    public void loadQuests() {
        boolean needsSaving = false;
        FileConfiguration config = null;
        final File file = new File(this.getDataFolder(), "quests.yml");
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        if (config != null) {
            ConfigurationSection questsSection;
            if (config.contains("quests")) {
                questsSection = config.getConfigurationSection("quests");
            } else {
                questsSection = config.createSection("quests");
                needsSaving = true;
            }
            if (questsSection == null) {
                getLogger().severe("Missing \'quests\' section marker within quests.yml, canceled loading");
                return;
            }
            for (final String questKey : questsSection.getKeys(false)) {
                try {
                    final Quest quest = loadQuest(config, questKey);
                    if (config.contains("quests." + questKey + ".requirements")) {
                        loadQuestRequirements(config, questsSection, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".planner")) {
                        loadQuestPlanner(config, questsSection, quest, questKey);
                    }
                    if (config.contains("quests." + questKey + ".options")) {
                        loadQuestOptions(config, questsSection, quest, questKey);
                    }
                    quest.plugin = this;
                    loadQuestStages(quest, config, questKey);
                    loadQuestRewards(config, quest, questKey);
                    quests.add(quest);
                    if (needsSaving) {
                        try {
                            config.save(file);
                        } catch (final IOException e) {
                            getLogger().log(Level.SEVERE, "Failed to save Quest \"" + questKey + "\"");
                            e.printStackTrace();
                        }
                    }
                } catch (final QuestFormatException e) {
                    e.printStackTrace();
                    continue;
                } catch (final StageFormatException e) {
                    e.printStackTrace();
                    continue;
                } catch (final ActionFormatException e) {
                    e.printStackTrace();
                    continue;
                } catch (final ConditionFormatException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        } else {
            getLogger().severe("Unable to load quests.yml");
        }
    }

    private Quest loadQuest(final FileConfiguration config, final String questKey) throws QuestFormatException,
            ActionFormatException {
        final Quest quest = new Quest();
        quest.id = questKey;
        if (config.contains("quests." + questKey + ".name")) {
            quest.setName(ConfigUtil.parseString(config.getString("quests." + questKey + ".name"), quest));
        } else {
            throw new QuestFormatException("name is missing", questKey);
        }
        if (config.contains("quests." + questKey + ".ask-message")) {
            quest.description = ConfigUtil.parseString(config.getString("quests." + questKey 
                    + ".ask-message"), quest);
        } else {
            throw new QuestFormatException("ask-message is missing", questKey);
        }
        if (config.contains("quests." + questKey + ".finish-message")) {
            quest.finished = ConfigUtil.parseString(config.getString("quests." + questKey 
                    + ".finish-message"), quest);
        } else {
            throw new QuestFormatException("finish-message is missing", questKey);
        }
        if (depends.getCitizens() != null && config.contains("quests." + questKey + ".npc-giver-id")) {
            final int npcId = config.getInt("quests." + questKey + ".npc-giver-id");
            if (CitizensAPI.getNPCRegistry().getById(npcId) != null) {
                quest.npcStart = CitizensAPI.getNPCRegistry().getById(npcId);
                questNpcs.add(CitizensAPI.getNPCRegistry().getById(npcId));
            } else {
                throw new QuestFormatException("npc-giver-id has invalid NPC ID " + npcId, questKey);
            }
        }
        if (config.contains("quests." + questKey + ".block-start")) {
            final Location location = ConfigUtil.getLocation(config.getString("quests." + questKey 
                    + ".block-start"));
            if (location != null) {
                quest.blockStart = location;
            } else {
                throw new QuestFormatException("block-start has invalid location format", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".region")
                && getDependencies().getWorldGuardApi() != null) {
            final String region = config.getString("quests." + questKey + ".region");
            boolean exists = false;
            for (final World world : getServer().getWorlds()) {
                if (getDependencies().getWorldGuardApi().getRegionManager(world) != null) {
                    if (getDependencies().getWorldGuardApi().getRegionManager(world).hasRegion(region)) {
                        quest.regionStart = region;
                        exists = true;
                        break;
                    }
                }
            }
            if (!exists) {
                throw new QuestFormatException("region has invalid WorldGuard region name", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".gui-display")) {
            ItemStack stack = config.getItemStack("quests." + questKey + ".gui-display");
            if (stack == null) {
                // Legacy
                final String item = config.getString("quests." + questKey + ".gui-display");
                try {
                    stack = ItemUtil.readItemStack(item);
                } catch (final Exception e) {
                    throw new QuestFormatException("items has invalid formatting for " + item, questKey);
                }
            }
            if (stack != null) {
                quest.guiDisplay = stack;
            } else {
                throw new QuestFormatException("gui-display has invalid item format", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".redo-delay")) {
            // Legacy
            if (config.getInt("quests." + questKey + ".redo-delay", -999) != -999) {
                quest.getPlanner().setCooldown(config.getInt("quests." + questKey + ".redo-delay") * 1000);
            } else {
                throw new QuestFormatException("redo-delay is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".action")) {
            final Action action = loadAction(config.getString("quests." + questKey + ".action"));
            if (action != null) {
                quest.initialAction = action;
            } else {
                throw new QuestFormatException("action failed to load", questKey);
            }
        } else if (config.contains("quests." + questKey + ".event")) {
            final Action action = loadAction(config.getString("quests." + questKey + ".event"));
            if (action != null) {
                quest.initialAction = action;
            } else {
                throw new QuestFormatException("action failed to load", questKey);
            }
        }
        return quest;
    }
    
    @SuppressWarnings("unchecked")
    private void loadQuestRewards(final FileConfiguration config, final Quest quest, final String questKey) throws QuestFormatException {
        final Rewards rews = quest.getRewards();
        if (config.contains("quests." + questKey + ".rewards.items")) {
            final LinkedList<ItemStack> temp = new LinkedList<ItemStack>();
            final List<ItemStack> stackList = (List<ItemStack>) config.get("quests." + questKey + ".rewards.items");
            if (ConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                // Legacy
                if (ConfigUtil.checkList(stackList, String.class)) {
                    final List<String> items = config.getStringList("quests." + questKey + ".rewards.items");
                    for (final String item : items) {
                        try {
                            final ItemStack stack = ItemUtil.readItemStack(item);
                            if (stack != null) {
                                temp.add(stack);
                            }
                        } catch (final Exception e) {
                            throw new QuestFormatException("Reward items has invalid formatting for " + item, questKey);
                        }
                    }
                } else {
                    throw new QuestFormatException("Reward items has invalid formatting", questKey);
                }
            }
            rews.setItems(temp);
        }
        if (config.contains("quests." + questKey + ".rewards.money")) {
            if (config.getInt("quests." + questKey + ".rewards.money", -999) != -999) {
                rews.setMoney(config.getInt("quests." + questKey + ".rewards.money"));
            } else {
                throw new QuestFormatException("Reward money is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.exp")) {
            if (config.getInt("quests." + questKey + ".rewards.exp", -999) != -999) {
                rews.setExp(config.getInt("quests." + questKey + ".rewards.exp"));
            } else {
                throw new QuestFormatException("Reward exp is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.commands")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.commands"), String.class)) {
                rews.setCommands(config.getStringList("quests." + questKey + ".rewards.commands"));
            } else {
                throw new QuestFormatException("Reward commands is not a list of commands", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.commands-override-display")) {
            // Legacy
            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.commands-override-display"), 
                    String.class)) {
                rews.setCommandsOverrideDisplay(config.getStringList("quests." + questKey 
                        + ".rewards.commands-override-display"));
            } else {
                throw new QuestFormatException("Reward commands-override-display is not a list of strings", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.permissions")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.permissions"), String.class)) {
                rews.setPermissions(config.getStringList("quests." + questKey + ".rewards.permissions"));
            } else {
                throw new QuestFormatException("Reward permissions is not a list of permissions", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.permission-worlds")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey 
                    + ".rewards.permission-worlds"), String.class)) {
                rews.setPermissionWorlds(config.getStringList("quests." + questKey + ".rewards.permission-worlds"));
            } else {
                throw new QuestFormatException("Reward permissions is not a list of worlds", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".rewards.quest-points")) {
            if (config.getInt("quests." + questKey + ".rewards.quest-points", -999) != -999) {
                rews.setQuestPoints(config.getInt("quests." + questKey + ".rewards.quest-points"));
            } else {
                throw new QuestFormatException("Reward quest-points is not a number", questKey);
            }
        }
        if (depends.isPluginAvailable("mcMMO")) {
            if (config.contains("quests." + questKey + ".rewards.mcmmo-skills")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.mcmmo-skills"), 
                        String.class)) {
                    if (config.contains("quests." + questKey + ".rewards.mcmmo-levels")) {
                        if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.mcmmo-levels"), 
                                Integer.class)) {
                            for (final String skill : config.getStringList("quests." + questKey + ".rewards.mcmmo-skills")) {
                                if (depends.getMcmmoClassic() == null) {
                                    throw new QuestFormatException("Reward mcMMO not found for mcmmo-skills", questKey);
                                } else if (Quests.getMcMMOSkill(skill) == null) {
                                    throw new QuestFormatException("Reward mcmmo-skills has invalid skill name " + skill, questKey);
                                }
                            }
                            rews.setMcmmoSkills(config.getStringList("quests." + questKey + ".rewards.mcmmo-skills"));
                            rews.setMcmmoAmounts(config.getIntegerList("quests." + questKey + ".rewards.mcmmo-levels"));
                        } else {
                            throw new QuestFormatException("Reward mcmmo-levels is not a list of numbers", questKey);
                        }
                    } else {
                        throw new QuestFormatException("Reward mcmmo-levels is missing!", questKey);
                    }
                } else {
                    throw new QuestFormatException("Reward mcmmo-skills is not a list of mcMMO skill names", questKey);
                }
            }
        }
        if (depends.isPluginAvailable("Heroes")) {
            if (config.contains("quests." + questKey + ".rewards.heroes-exp-classes")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-classes"), 
                        String.class)) {
                    if (config.contains("quests." + questKey + ".rewards.heroes-exp-amounts")) {
                        if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.heroes-exp-amounts"), 
                                Double.class)) {
                            for (final String heroClass : config.getStringList("quests." + questKey 
                                    + ".rewards.heroes-exp-classes")) {
                                if (depends.getHeroes() == null) {
                                    throw new QuestFormatException("Heroes not found for heroes-exp-classes", questKey);
                                } else if (depends.getHeroes().getClassManager().getClass(heroClass) == null) {
                                    throw new QuestFormatException("Reward heroes-exp-classes has invalid class name " 
                                            + heroClass, questKey);
                                }
                            }
                            rews.setHeroesClasses(config.getStringList("quests." + questKey 
                                    + ".rewards.heroes-exp-classes"));
                            rews.setHeroesAmounts(config.getDoubleList("quests." + questKey 
                                    + ".rewards.heroes-exp-amounts"));
                        } else {
                            throw new QuestFormatException("Reward heroes-exp-amounts is not a list of decimal numbers", questKey);
                        }
                    } else {
                        throw new QuestFormatException("Reward heroes-exp-amounts is missing", questKey);
                    }
                } else {
                    throw new QuestFormatException("Reward heroes-exp-classes is not a list of Heroes classes", questKey);
                }
            }
        }
        if (depends.isPluginAvailable("Parties")) {
            if (config.contains("quests." + questKey + ".rewards.parties-experience")) {
                if (config.getInt("quests." + questKey + ".rewards.parties-experience", -999) != -999) {
                    rews.setPartiesExperience(config.getInt("quests." + questKey + ".rewards.parties-experience"));
                } else {
                    throw new QuestFormatException("Reward parties-experience is not a number", questKey);
                }
            }
        }
        if (depends.isPluginAvailable("PhatLoots")) {
            if (config.contains("quests." + questKey + ".rewards.phat-loots")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".rewards.phat-loots"), String.class)) {
                    for (final String loot : config.getStringList("quests." + questKey + ".rewards.phat-loots")) {
                        if (depends.getPhatLoots() == null) {
                            throw new QuestFormatException("PhatLoots not found for phat-loots", questKey);
                        } else if (PhatLootsAPI.getPhatLoot(loot) == null) {
                            throw new QuestFormatException("Reward phat-loots has invalid PhatLoot name " + loot, questKey);
                        }
                    }
                    rews.setPhatLoots(config.getStringList("quests." + questKey + ".rewards.phat-loots"));
                } else {
                    throw new QuestFormatException("Reward phat-loots is not a list of PhatLoots", questKey);
                }
            }
        }
        if (config.contains("quests." + questKey + ".rewards.details-override")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey 
                    + ".rewards.details-override"), String.class)) {
                rews.setDetailsOverride(config.getStringList("quests." + questKey + ".rewards.details-override"));
            }  else {
                throw new QuestFormatException("Reward details-override is not a list of strings", questKey);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadQuestRequirements(final FileConfiguration config, final ConfigurationSection questsSection, final Quest quest, 
            final String questKey) throws QuestFormatException {
        final Requirements reqs = quest.getRequirements();
        if (config.contains("quests." + questKey + ".requirements.fail-requirement-message")) {
            final Object o = config.get("quests." + questKey + ".requirements.fail-requirement-message");
            if (o instanceof List) {
                reqs.setDetailsOverride(config.getStringList("quests." + questKey 
                        + ".requirements.fail-requirement-message"));
            } else {
                // Legacy
                final List<String> override = new LinkedList<String>();
                override.add((String) o);
                reqs.setDetailsOverride(override);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.items")) {
            final List<ItemStack> temp = new LinkedList<ItemStack>();
            final List<ItemStack> stackList = (List<ItemStack>) config.get("quests." + questKey + ".requirements.items");
            if (ConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                // Legacy
                final List<String> items = config.getStringList("quests." + questKey + ".requirements.items");
                if (ConfigUtil.checkList(items, String.class)) {
                    for (final String item : items) {
                        try {
                            final ItemStack stack = ItemUtil.readItemStack(item);
                            if (stack != null) {
                                temp.add(stack);
                            }
                        } catch (final Exception e) {
                            throw new QuestFormatException("Requirement items has invalid formatting for " + item, questKey);
                        }
                    }
                } else {
                    throw new QuestFormatException("Requirement items has invalid formatting", questKey);
                }
            }
            reqs.setItems(temp);
            if (config.contains("quests." + questKey + ".requirements.remove-items")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.remove-items"), 
                        Boolean.class)) {
                    reqs.setRemoveItems(config.getBooleanList("quests." + questKey + ".requirements.remove-items"));
                } else {
                    throw new QuestFormatException("Requirement remove-items is not a list of true/false values", questKey);
                }
            } else {
                throw new QuestFormatException("Requirement remove-items is missing", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.money")) {
            if (config.getInt("quests." + questKey + ".requirements.money", -999) != -999) {
                reqs.setMoney(config.getInt("quests." + questKey + ".requirements.money"));
            } else {
                throw new QuestFormatException("Requirement money is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.quest-points")) {
            if (config.getInt("quests." + questKey + ".requirements.quest-points", -999) != -999) {
                reqs.setQuestPoints(config.getInt("quests." + questKey + ".requirements.quest-points"));
            } else {
                throw new QuestFormatException("Requirement quest-points is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.quest-blocks")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.quest-blocks"), 
                    String.class)) {
                final List<String> names = config.getStringList("quests." + questKey + ".requirements.quest-blocks");
                boolean failed = false;
                String failedQuest = "NULL";
                final List<Quest> temp = new LinkedList<Quest>();
                for (final String name : names) {
                    boolean done = false;
                    for (final String id : questsSection.getKeys(false)) {
                        final String name2 = config.getString("quests." + id + ".name");
                        if (name2.equalsIgnoreCase(name)
                                || ChatColor.stripColor(name2).equalsIgnoreCase(ChatColor.stripColor(name))) {
                            if (getQuest(name) != null) {
                                temp.add(getQuest(name));
                            } else {
                                throw new QuestFormatException("Requirement quest-blocks has unknown quest name " 
                                        + name + ", place it earlier in file so it loads first", questKey);
                            }
                            done = true;
                            break;
                        }
                    }
                    if (!done) {
                        failed = true;
                        failedQuest = name;
                        break;
                    }
                }
                reqs.setBlockQuests(temp);
                if (failed) {
                    throw new QuestFormatException("Requirement quest-blocks has invalid quest name " + failedQuest, questKey);
                }
            } else {
                throw new QuestFormatException("Requirement quest-blocks is not a list of quest names", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.quests")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.quests"), String.class)) {
                final List<String> names = config.getStringList("quests." + questKey + ".requirements.quests");
                boolean failed = false;
                String failedQuest = "NULL";
                final List<Quest> temp = new LinkedList<Quest>();
                for (final String name : names) {
                    boolean done = false;
                    for (final String id : questsSection.getKeys(false)) {
                        final String name2 = config.getString("quests." + id + ".name");
                        if (name2.equalsIgnoreCase(name)
                                || ChatColor.stripColor(name2).equalsIgnoreCase(ChatColor.stripColor(name))) {
                            if (getQuest(name) != null) {
                                temp.add(getQuest(name));
                            } else {
                                throw new QuestFormatException("Requirement quests has unknown quest name " 
                                        + name + ", place it earlier in file so it loads first", questKey);
                            }
                            done = true;
                            break;
                        }
                    }
                    if (!done) {
                        failed = true;
                        failedQuest = name;
                        break;
                    }
                }
                reqs.setNeededQuests(temp);
                if (failed) {
                    throw new QuestFormatException("Requirement quests has invalid quest name " 
                            + failedQuest, questKey);
                }
            } else {
                throw new QuestFormatException("Requirement quests is not a list of quest names", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.permissions")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.permissions"), 
                    String.class)) {
                reqs.setPermissions(config.getStringList("quests." + questKey + ".requirements.permissions"));
            } else {
                throw new QuestFormatException("Requirement permissions is not a list of permissions", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.mcmmo-skills")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-skills"), 
                    String.class)) {
                if (config.contains("quests." + questKey + ".requirements.mcmmo-amounts")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".requirements.mcmmo-amounts"), 
                            Integer.class)) {
                        final List<String> skills = config.getStringList("quests." + questKey + ".requirements.mcmmo-skills");
                        final List<Integer> amounts = config.getIntegerList("quests." + questKey 
                                + ".requirements.mcmmo-amounts");
                        if (skills.size() != amounts.size()) {
                            throw new QuestFormatException("Requirement mcmmo-skills: and mcmmo-amounts are not the same size", 
                                    questKey);
                        }
                        reqs.setMcmmoSkills(skills);
                        reqs.setMcmmoAmounts(amounts);
                    } else {
                        throw new QuestFormatException("Requirement mcmmo-amounts is not a list of numbers", questKey);
                    }
                } else {
                    throw new QuestFormatException("Requirement mcmmo-amounts is missing", questKey);
                }
            } else {
                throw new QuestFormatException("Requirement mcmmo-skills is not a list of skills", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.heroes-primary-class")) {
            final String className = config.getString("quests." + questKey + ".requirements.heroes-primary-class");
            final HeroClass hc = depends.getHeroes().getClassManager().getClass(className);
            if (hc != null && hc.isPrimary()) {
                reqs.setHeroesPrimaryClass(hc.getName());
            } else if (hc != null) {
                throw new QuestFormatException("Requirement heroes-primary-class is not a primary Heroes class", questKey);
            } else {
                throw new QuestFormatException("Requirement heroes-primary-class has invalid Heroes class", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.heroes-secondary-class")) {
            final String className = config.getString("quests." + questKey + ".requirements.heroes-secondary-class");
            final HeroClass hc = depends.getHeroes().getClassManager().getClass(className);
            if (hc != null && hc.isSecondary()) {
                reqs.setHeroesSecondaryClass(hc.getName());
            } else if (hc != null) {
                throw new QuestFormatException("Requirement heroes-secondary-class is not a secondary Heroes class", questKey);
            } else {
                throw new QuestFormatException("Requirement heroes-secondary-class has invalid Heroes class", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".requirements.details-override")) {
            if (ConfigUtil.checkList(config.getList("quests." + questKey 
                    + ".requirements.details-override"), String.class)) {
                reqs.setDetailsOverride(config.getStringList("quests." + questKey + ".requirements.details-override"));
            }  else {
                throw new QuestFormatException("Requirement details-override is not a list of strings", questKey);
            }
        }
    }
    
    private void loadQuestPlanner(final FileConfiguration config, final ConfigurationSection questsSection, final Quest quest, 
            final String questKey) throws QuestFormatException {
        final Planner pln = quest.getPlanner();
        if (config.contains("quests." + questKey + ".planner.start")) {
            pln.setStart(config.getString("quests." + questKey + ".planner.start"));
        }
        if (config.contains("quests." + questKey + ".planner.end")) {
            pln.setEnd(config.getString("quests." + questKey + ".planner.end"));
        }
        if (config.contains("quests." + questKey + ".planner.repeat")) {
            if (config.getInt("quests." + questKey + ".planner.repeat", -999) != -999) {
                pln.setRepeat(config.getInt("quests." + questKey + ".planner.repeat") * 1000);
            } else {
                throw new QuestFormatException("Requirement repeat is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".planner.cooldown")) {
            if (config.getInt("quests." + questKey + ".planner.cooldown", -999) != -999) {
                pln.setCooldown(config.getInt("quests." + questKey + ".planner.cooldown") * 1000);
            } else {
                throw new QuestFormatException("Requirement cooldown is not a number", questKey);
            }
        }
        if (config.contains("quests." + questKey + ".planner.override")) {
            pln.setOverride(config.getBoolean("quests." + questKey + ".planner.override"));
        }
    }
    
    private void loadQuestOptions(final FileConfiguration config, final ConfigurationSection questsSection, final Quest quest, 
            final String questKey) throws QuestFormatException {
        final Options opts = quest.getOptions();
        if (config.contains("quests." + questKey + ".options.allow-commands")) {
            opts.setAllowCommands(config.getBoolean("quests." + questKey + ".options.allow-commands"));
        }
        if (config.contains("quests." + questKey + ".options.allow-quitting")) {
            opts.setAllowQuitting(config.getBoolean("quests." + questKey + ".options.allow-quitting"));
        } else if (getConfig().contains("allow-quitting")) {
            // Legacy
            opts.setAllowQuitting(getConfig().getBoolean("allow-quitting"));
        }
        if (config.contains("quests." + questKey + ".options.ignore-silk-touch")) {
            opts.setIgnoreSilkTouch(config.getBoolean("quests." + questKey + ".options.ignore-silk-touch"));
        }
        if (config.contains("quests." + questKey + ".options.use-dungeonsxl-plugin")) {
            opts.setUseDungeonsXLPlugin(config.getBoolean("quests." + questKey + ".options.use-dungeonsxl-plugin"));
        }
        if (config.contains("quests." + questKey + ".options.use-parties-plugin")) {
            opts.setUsePartiesPlugin(config.getBoolean("quests." + questKey + ".options.use-parties-plugin"));
        }
        if (config.contains("quests." + questKey + ".options.share-progress-level")) {
            opts.setShareProgressLevel(config.getInt("quests." + questKey + ".options.share-progress-level"));
        }
        if (config.contains("quests." + questKey + ".options.same-quest-only")) {
            opts.setShareSameQuestOnly(config.getBoolean("quests." + questKey + ".options.same-quest-only"));
        }
        if (config.contains("quests." + questKey + ".options.share-distance")) {
            opts.setShareDistance(config.getDouble("quests." + questKey + ".options.share-distance"));
        }
        if (config.contains("quests." + questKey + ".options.handle-offline-players")) {
            opts.setHandleOfflinePlayers(config.getBoolean("quests." + questKey + ".options.handle-offline-players"));
        }
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private void loadQuestStages(final Quest quest, final FileConfiguration config, final String questKey)
            throws StageFormatException, ActionFormatException, ConditionFormatException {
        final ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey + ".stages.ordered");
        if (questStages == null) {
            getLogger().severe(ChatColor.RED + questKey + " must have at least one stage!");
            return;
        }
        for (final String stage : questStages.getKeys(false)) {
            int stageNum = 0;
            try {
                stageNum = Integer.valueOf(stage);
            } catch (final NumberFormatException e) {
                getLogger().severe("Stage key " + stage + "must be a number!");
                continue;
            }
            final Stage oStage = new Stage();
            List<String> breakNames = new LinkedList<String>();
            List<Integer> breakAmounts = new LinkedList<Integer>();
            List<Short> breakDurability = new LinkedList<Short>();
            List<String> damageNames = new LinkedList<String>();
            List<Integer> damageAmounts = new LinkedList<Integer>();
            List<Short> damageDurability = new LinkedList<Short>();
            List<String> placeNames = new LinkedList<String>();
            List<Integer> placeAmounts = new LinkedList<Integer>();
            List<Short> placeDurability = new LinkedList<Short>();
            List<String> useNames = new LinkedList<String>();
            List<Integer> useAmounts = new LinkedList<Integer>();
            List<Short> useDurability = new LinkedList<Short>();
            List<String> cutNames = new LinkedList<String>();
            List<Integer> cutAmounts = new LinkedList<Integer>();
            List<Short> cutDurability = new LinkedList<Short>();
            final List<EntityType> mobsToKill = new LinkedList<EntityType>();
            final List<Integer> mobNumToKill = new LinkedList<Integer>();
            final List<Location> locationsToKillWithin = new LinkedList<Location>();
            final List<Integer> radiiToKillWithin = new LinkedList<Integer>();
            final List<String> areaNames = new LinkedList<String>();
            List<ItemStack> itemsToCraft = new LinkedList<ItemStack>();
            List<ItemStack> itemsToSmelt = new LinkedList<ItemStack>();
            List<ItemStack> itemsToEnchant = new LinkedList<ItemStack>();
            List<ItemStack> itemsToBrew = new LinkedList<ItemStack>();
            List<ItemStack> itemsToConsume = new LinkedList<ItemStack>();
            List<Integer> npcIdsToTalkTo = new LinkedList<Integer>();
            List<ItemStack> itemsToDeliver= new LinkedList<ItemStack>();
            List<Integer> itemDeliveryTargetIds = new LinkedList<Integer>();
            List<String> deliveryMessages = new LinkedList<String>();
            List<Integer> npcIdsToKill = new LinkedList<Integer>();
            List<Integer> npcAmountsToKill = new LinkedList<Integer>();
            // Legacy Denizen script load
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".script-to-run")) {
                if (getDependencies().getDenizenApi().containsScript(config.getString("quests." + questKey 
                        + ".stages.ordered." + stageNum + ".script-to-run"))) {
                    oStage.script = config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".script-to-run");
                } else {
                    throw new StageFormatException("script-to-run is not a valid Denizen script", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".break-block-names")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".break-block-names"), String.class)) {
                    breakNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".break-block-names");
                } else {
                    throw new StageFormatException("break-block-names is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".break-block-amounts")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".break-block-amounts"), Integer.class)) {
                        breakAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".break-block-amounts");
                    } else {
                        throw new StageFormatException("break-block-amounts is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("break-block-amounts is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".break-block-durability")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".break-block-durability"), Integer.class)) {
                        breakDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".break-block-durability");
                    } else {
                        throw new StageFormatException("break-block-durability is not a list of numbers", quest, 
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("break-block-durability is missing", quest, stageNum);
                }
            }
            int breakIndex = 0;
            for (final String s : breakNames) {
                ItemStack is;
                if (breakIndex < breakDurability.size() && breakDurability.get(breakIndex) != -1) {
                    is = ItemUtil.processItemStack(s, breakAmounts.get(breakIndex), breakDurability.get(breakIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, breakAmounts.get(breakIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToBreak.add(is);
                } else {
                    throw new StageFormatException("break-block-names has invalid item name " + s, quest, stageNum);
                }
                breakIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".damage-block-names")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".damage-block-names"), String.class)) {
                    damageNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".damage-block-names");
                } else {
                    throw new StageFormatException("damage-block-names is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".damage-block-amounts")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".damage-block-amounts"), Integer.class)) {
                        damageAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".damage-block-amounts");
                    } else {
                        throw new StageFormatException("damage-block-amounts is not a list of numbers", quest, 
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("damage-block-amounts is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".damage-block-durability")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".damage-block-durability"), Integer.class)) {
                        damageDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".damage-block-durability");
                    } else {
                        throw new StageFormatException("damage-block-durability is not a list of numbers", quest, 
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("damage-block-durability is missing", quest, stageNum);
                }
            }
            int damageIndex = 0;
            for (final String s : damageNames) {
                ItemStack is;
                if (damageIndex < damageDurability.size() && damageDurability.get(damageIndex) != -1) {
                    is = ItemUtil.processItemStack(s, damageAmounts.get(damageIndex), 
                            damageDurability.get(damageIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, damageAmounts.get(damageIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToDamage.add(is);
                } else {
                    throw new StageFormatException("damage-block-names has invalid item name " + s, quest, stageNum);
                }
                damageIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".place-block-names")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".place-block-names"), String.class)) {
                    placeNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".place-block-names");
                } else {
                    throw new StageFormatException("place-block-names is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".place-block-amounts")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".place-block-amounts"), Integer.class)) {
                        placeAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".place-block-amounts");
                    } else {
                        throw new StageFormatException("place-block-amounts is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("place-block-amounts is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".place-block-durability")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".place-block-durability"), Integer.class)) {
                        placeDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".place-block-durability");
                    } else {
                        throw new StageFormatException("place-block-durability is not a list of numbers", quest, 
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("place-block-durability is missing", quest, stageNum);
                }
            }
            int placeIndex = 0;
            for (final String s : placeNames) {
                ItemStack is;
                if (placeIndex < placeDurability.size() && placeDurability.get(placeIndex) != -1) {
                    is = ItemUtil.processItemStack(s, placeAmounts.get(placeIndex), placeDurability.get(placeIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, placeAmounts.get(placeIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToPlace.add(is);
                } else {
                    throw new StageFormatException("place-block-names has invalid item name " + s, quest, stageNum);
                }
                placeIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".use-block-names")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".use-block-names"), String.class)) {
                    useNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".use-block-names");
                } else {
                    throw new StageFormatException("use-block-names is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".use-block-amounts")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".use-block-amounts"),Integer.class)) {
                        useAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".use-block-amounts");
                    } else {
                        throw new StageFormatException("use-block-amounts is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("use-block-amounts is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".use-block-durability")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".use-block-durability"), Integer.class)) {
                        useDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".use-block-durability");
                    } else {
                        throw new StageFormatException("use-block-durability is not a list of numbers", quest, 
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("use-block-durability is missing", quest, stageNum);
                }
            }
            int useIndex = 0;
            for (final String s : useNames) {
                ItemStack is;
                if (useIndex < useDurability.size() && useDurability.get(useIndex) != -1) {
                    is = ItemUtil.processItemStack(s, useAmounts.get(useIndex), useDurability.get(useIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, useAmounts.get(useIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToUse.add(is);
                } else {
                    throw new StageFormatException("use-block-names has invalid item name " + s, quest, stageNum);
                }
                useIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cut-block-names")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".cut-block-names"), String.class)) {
                    cutNames = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".cut-block-names");
                } else {
                    throw new StageFormatException("cut-block-names is not a list of strings", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cut-block-amounts")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".cut-block-amounts"), Integer.class)) {
                        cutAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".cut-block-amounts");
                    } else {
                        throw new StageFormatException("cut-block-amounts is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("cut-block-amounts is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cut-block-durability")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".cut-block-durability"), Integer.class)) {
                        cutDurability = config.getShortList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".cut-block-durability");
                    } else {
                        throw new StageFormatException("cut-block-durability is not a list of numbers", quest, 
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("cut-block-durability is missing", quest, stageNum);
                }
            }
            int cutIndex = 0;
            for (final String s : cutNames) {
                ItemStack is;
                if (cutIndex < cutDurability.size() && cutDurability.get(cutIndex) != -1) {
                    is = ItemUtil.processItemStack(s, cutAmounts.get(cutIndex), cutDurability.get(cutIndex));
                } else {
                    // Legacy
                    is = ItemUtil.processItemStack(s, cutAmounts.get(cutIndex), (short) 0);
                }
                if (Material.matchMaterial(s) != null) {
                    oStage.blocksToCut.add(is);
                } else {
                    throw new StageFormatException("cut-block-names has invalid item name " + s, quest, stageNum);
                }
                cutIndex++;
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-craft")) {
                itemsToCraft = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".items-to-craft");
                if (ConfigUtil.checkList(itemsToCraft, ItemStack.class)) {
                    for (final ItemStack stack : itemsToCraft) {
                        if (stack != null) {
                            oStage.itemsToCraft.add(stack);
                        } else {
                            throw new StageFormatException("items-to-craft has invalid formatting " 
                                    + stack, quest, stageNum);
                        }
                    }
                } else {
                    // Legacy
                    final List<String> items = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".items-to-craft");
                    if (ConfigUtil.checkList(items, String.class)) {
                        for (final String item : items) {
                            final ItemStack is = ItemUtil.readItemStack("" + item);
                            if (is != null) {
                                oStage.itemsToCraft.add(is);
                            } else {
                                throw new StageFormatException("Legacy items-to-craft has invalid formatting " 
                                        + item, quest, stageNum);
                            }
                        }
                    } else {
                        throw new StageFormatException("items-to-craft is not formatted properly", quest, stageNum);
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-smelt")) {
                itemsToSmelt = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".items-to-smelt");
                if (ConfigUtil.checkList(itemsToSmelt, ItemStack.class)) {
                    for (final ItemStack stack : itemsToSmelt) {
                        if (stack != null) {
                            oStage.itemsToSmelt.add(stack);
                        } else {
                            throw new StageFormatException("items-to-smelt has invalid formatting " 
                                    + stack, quest, stageNum);
                        }
                    }
                } else {
                    // Legacy
                    final List<String> items = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".items-to-smelt");
                    if (ConfigUtil.checkList(items, String.class)) {
                        for (final String item : items) {
                            final ItemStack is = ItemUtil.readItemStack("" + item);
                            if (is != null) {
                                oStage.itemsToSmelt.add(is);
                            } else {
                                throw new StageFormatException("Legacy items-to-smelt has invalid formatting " 
                                        + item, quest, stageNum);
                            }
                        }
                    } else {
                        throw new StageFormatException("items-to-smelt is not formatted properly", quest, stageNum);
                    }
                }
            }
            
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-enchant")) {
                itemsToEnchant = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".items-to-enchant");
                if (ConfigUtil.checkList(itemsToEnchant, ItemStack.class)) {
                    for (final ItemStack stack : itemsToEnchant) {
                        if (stack != null) {
                            oStage.itemsToEnchant.add(stack);
                        } else {
                            throw new StageFormatException("items-to-enchant has invalid formatting " 
                                    + stack, quest, stageNum);
                        }
                    }
                } else {
                    // Legacy
                    final LinkedList<Material> types = new LinkedList<Material>();
                    final LinkedList<Enchantment> enchs = new LinkedList<Enchantment>();
                    final LinkedList<Integer> amts = new LinkedList<Integer>();
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".enchantments")) {
                        if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".enchantments"), String.class)) {
                            for (final String enchant : config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                                    + ".enchantments")) {
                                final Enchantment e = ItemUtil.getEnchantmentFromProperName(enchant);
                                if (e != null) {
                                    enchs.add(e);
                                } else {
                                    throw new StageFormatException("enchantments has invalid enchantment " 
                                            + enchant, quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("enchantments is not a list of enchantment names", quest, stageNum);
                        }
                        if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".enchantment-item-names")) {
                            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                                    + ".enchantment-item-names"), String.class)) {
                                for (final String item : config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                                        + ".enchantment-item-names")) {
                                    if (Material.matchMaterial(item) != null) {
                                        types.add(Material.matchMaterial(item));
                                    } else {
                                        throw new StageFormatException("enchantment-item-names has invalid item name " 
                                                + item, quest, stageNum);
                                    }
                                }
                            } else {
                                throw new StageFormatException("enchantment-item-names has invalid item name", quest, stageNum);
                            }
                        } else {
                            throw new StageFormatException("enchantment-item-names is missing", quest, stageNum);
                        }
                        if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".enchantment-amounts")) {
                            if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                                    + ".enchantment-amounts"), Integer.class)) {
                                for (final int i : config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                        + ".enchantment-amounts")) {
                                    amts.add(i);
                                }
                            } else {
                                throw new StageFormatException("enchantment-amounts is not a list of numbers", quest, stageNum);
                            }
                        } else {
                            throw new StageFormatException("enchantment-amounts is missing", quest, stageNum);
                        }
                        if (!enchs.isEmpty() && !types.isEmpty() && !amts.isEmpty()) {
                            for (int i = 0; i < enchs.size(); i++) {
                                final ItemStack stack = new ItemStack(types.get(i), amts.get(i));
                                stack.addEnchantment(enchs.get(0), 1);
                                if (stack != null) {
                                    oStage.itemsToEnchant.add(stack);
                                } else {
                                    throw new StageFormatException("Legacy items-to-enchant has invalid formatting " 
                                            + stack, quest, stageNum);
                                }
                            }
                        }
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-brew")) {
                itemsToBrew = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".items-to-brew");
                if (ConfigUtil.checkList(itemsToBrew, ItemStack.class)) {
                    for (final ItemStack stack : itemsToBrew) {
                        if (stack != null) {
                            oStage.itemsToBrew.add(stack);
                        } else {
                            throw new StageFormatException("items-to-brew has invalid formatting " 
                                    + stack, quest, stageNum);
                        }
                    }
                } else {
                    // Legacy
                    final List<String> items = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".items-to-brew");
                    if (ConfigUtil.checkList(items, String.class)) {
                        for (final String item : items) {
                            final ItemStack is = ItemUtil.readItemStack("" + item);
                            if (is != null) {
                                oStage.itemsToBrew.add(is);
                            } else {
                                throw new StageFormatException("Legacy items-to-brew has invalid formatting " 
                                        + item, quest, stageNum);
                            }
                        }
                    } else {
                        throw new StageFormatException("items-to-brew has invalid formatting", quest, stageNum);
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-consume")) {
                itemsToConsume = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".items-to-consume");
                if (ConfigUtil.checkList(itemsToConsume, ItemStack.class)) {
                    for (final ItemStack stack : itemsToConsume) {
                        if (stack != null) {
                            oStage.itemsToConsume.add(stack);
                        } else {
                            throw new StageFormatException("items-to-consume has invalid formatting " 
                                    + stack, quest, stageNum);
                        }
                    }
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".cows-to-milk")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".cows-to-milk", -999) 
                        != -999) {
                    oStage.cowsToMilk = config.getInt("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".cows-to-milk");
                } else {
                    throw new StageFormatException("cows-to-milk is not a number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".fish-to-catch")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".fish-to-catch", -999) 
                        != -999) {
                    oStage.fishToCatch = config.getInt("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".fish-to-catch");
                } else {
                    throw new StageFormatException("fish-to-catch is not a number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".players-to-kill")) {
                if (config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".players-to-kill", -999) 
                        != -999) {
                    oStage.playersToKill = config.getInt("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".players-to-kill");
                } else {
                    throw new StageFormatException("players-to-kill is not a number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-ids-to-talk-to")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".npc-ids-to-talk-to"), Integer.class)) {
                    npcIdsToTalkTo = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".npc-ids-to-talk-to");
                    for (final int i : npcIdsToTalkTo) {
                        if (getDependencies().getCitizens() != null) {
                            if (CitizensAPI.getNPCRegistry().getById(i) != null) {
                                questNpcs.add(CitizensAPI.getNPCRegistry().getById(i));
                            } else {
                                throw new StageFormatException("npc-ids-to-talk-to has invalid NPC ID of " + i, quest, 
                                        stageNum);
                            }
                        } else {
                            throw new StageFormatException("Citizens not found for npc-ids-to-talk-to", quest, 
                                    stageNum);
                        }
                        
                    }
                } else {
                    throw new StageFormatException("npc-ids-to-talk-to is not a list of numbers", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".items-to-deliver")) {
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-delivery-ids")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".npc-delivery-ids"), Integer.class)) {
                        if (config.contains("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".delivery-messages")) {
                            itemsToDeliver = (List<ItemStack>) config.get("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".items-to-deliver");
                            itemDeliveryTargetIds = config.getIntegerList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".npc-delivery-ids");
                            deliveryMessages = config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".delivery-messages");
                            int index = 0;
                            if (ConfigUtil.checkList(itemsToDeliver, ItemStack.class)) {
                                for (final ItemStack stack : itemsToDeliver) {
                                    if (stack != null) {
                                        final int npcId = itemDeliveryTargetIds.get(index);
                                        final String msg = deliveryMessages.size() > index 
                                                ? deliveryMessages.get(index) 
                                                : deliveryMessages.get(deliveryMessages.size() - 1);
                                        index++;
                                        if (stack != null) {
                                            if (getDependencies().getCitizens() != null) {
                                                final NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
                                                if (npc != null) {
                                                    oStage.itemsToDeliver.add(stack);
                                                    oStage.itemDeliveryTargets.add(npcId);
                                                    oStage.deliverMessages.add(msg);
                                                } else {
                                                    throw new StageFormatException(
                                                            "Citizens not found for npc-delivery-ids", quest, stageNum);
                                                }
                                            } else {
                                                throw new StageFormatException("npc-delivery-ids has invalid NPC ID of " 
                                           + npcId, quest, stageNum);
                                            }
                                        } else {
                                            throw new StageFormatException("items-to-deliver has invalid formatting " 
                                        + stack, quest, stageNum);
                                        }
                                    }
                                }
                            } else {
                                final List<String> items = config.getStringList("quests." + questKey 
                                        + ".stages.ordered." + stageNum + ".items-to-deliver");
                                if (ConfigUtil.checkList(items, String.class)) {
                                    // Legacy
                                    for (final String item : items) {
                                        final ItemStack is = ItemUtil.readItemStack("" + item);
                                        if (index <= itemDeliveryTargetIds.size()) {
                                            if (itemDeliveryTargetIds.size() != deliveryMessages.size()) {
                                                throw new StageFormatException(
                                                        "delivery-messages must be same size as items-to-deliver",
                                                        quest, stageNum);
                                            }
                                            final int npcId = itemDeliveryTargetIds.get(index);
                                            final String msg = deliveryMessages.size() > index 
                                                    ? deliveryMessages.get(index) 
                                                    : deliveryMessages.get(deliveryMessages.size() - 1);
                                            index++;
                                            if (is != null) {
                                                if (getDependencies().getCitizens() != null) {
                                                    final NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
                                                    if (npc != null) {
                                                        oStage.itemsToDeliver.add(is);
                                                        oStage.itemDeliveryTargets.add(npcId);
                                                        oStage.deliverMessages.add(msg);
                                                    } else {
                                                        throw new StageFormatException(
                                                                "npc-delivery-ids has invalid NPC ID of " + npcId, 
                                                                quest, stageNum);
                                                    }
                                                } else {
                                                    throw new StageFormatException(
                                                            "Citizens was not found installed for npc-delivery-ids", 
                                                            quest, stageNum);
                                                }
                                            } else {
                                                throw new StageFormatException(
                                                        "items-to-deliver has invalid formatting " + item, quest, 
                                                        stageNum);
                                            }
                                        } else {
                                            throw new StageFormatException("items-to-deliver is missing target IDs"
                                                    , quest, stageNum);
                                        }
                                    }
                                } else {
                                    throw new StageFormatException("items-to-deliver has invalid formatting", quest, 
                                            stageNum);
                                }
                            }
                        }
                    } else {
                        throw new StageFormatException("npc-delivery-ids is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("npc-delivery-id is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-ids-to-kill")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".npc-ids-to-kill"), Integer.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".npc-kill-amounts")) {
                        if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".npc-kill-amounts"), Integer.class)) {
                            npcIdsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                    + ".npc-ids-to-kill");
                            npcAmountsToKill = config.getIntegerList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".npc-kill-amounts");
                            for (final int i : npcIdsToKill) {
                                if (CitizensAPI.getNPCRegistry().getById(i) != null) {
                                    if (npcAmountsToKill.get(npcIdsToKill.indexOf(i)) > 0) {
                                        oStage.citizensToKill.add(i);
                                        oStage.citizenNumToKill.add(npcAmountsToKill.get(npcIdsToKill.indexOf(i)));
                                        questNpcs.add(CitizensAPI.getNPCRegistry().getById(i));
                                    } else {
                                        throw new StageFormatException("npc-kill-amounts is not a positive number", 
                                                quest, stageNum);
                                    }
                                } else {
                                    throw new StageFormatException("npc-ids-to-kill has invalid NPC ID of " + i, quest,
                                            stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("npc-kill-amounts is not a list of numbers", quest, 
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("npc-kill-amounts is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("npc-ids-to-kill is not a list of numbers", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mobs-to-kill")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".mobs-to-kill"), String.class)) {
                    final List<String> mobNames = config.getStringList("quests." + questKey + ".stages.ordered." 
                            + stageNum + ".mobs-to-kill");
                    for (final String mob : mobNames) {
                        final EntityType type = MiscUtil.getProperMobType(mob);
                        if (type != null) {
                            mobsToKill.add(type);
                        } else {
                            throw new StageFormatException("mobs-to-kill has invalid mob name " + mob, quest, stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("mobs-to-kill is not a list of mob names", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mob-amounts")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".mob-amounts"), Integer.class)) {
                        for (final int i : config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".mob-amounts")) {
                            mobNumToKill.add(i);
                        }
                    } else {
                        throw new StageFormatException("mob-amounts is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("mob-amounts is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".locations-to-kill")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".locations-to-kill"), String.class)) {
                    final List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".locations-to-kill");
                    for (final String loc : locations) {
                        if (ConfigUtil.getLocation(loc) != null) {
                            locationsToKillWithin.add(ConfigUtil.getLocation(loc));
                        } else {
                            throw new StageFormatException("locations-to-kill has invalid formatting " + loc, quest, 
                                    stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("locations-to-kill is not a list of locations", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".kill-location-radii")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".kill-location-radii"), Integer.class)) {
                        final List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".kill-location-radii");
                        for (final int i : radii) {
                            radiiToKillWithin.add(i);
                        }
                    } else {
                        throw new StageFormatException("kill-location-radii is not a list of numbers", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("kill-location-radii is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".kill-location-names")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".kill-location-names"), String.class)) {
                        final List<String> locationNames = config.getStringList("quests." + questKey + ".stages.ordered." 
                            + stageNum + ".kill-location-names");
                        for (final String name : locationNames) {
                            areaNames.add(name);
                        }
                    } else {
                        throw new StageFormatException("kill-location-names is not a list of names", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("kill-location-names is missing", quest, stageNum);
                }
            }
            oStage.mobsToKill.addAll(mobsToKill);
            oStage.mobNumToKill.addAll(mobNumToKill);
            oStage.locationsToKillWithin.addAll(locationsToKillWithin);
            oStage.radiiToKillWithin.addAll(radiiToKillWithin);
            oStage.killNames.addAll(areaNames);
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".locations-to-reach")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".locations-to-reach"), String.class)) {
                    final List<String> locations = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".locations-to-reach");
                    for (final String loc : locations) {
                        if (ConfigUtil.getLocation(loc) != null) {
                            oStage.locationsToReach.add(ConfigUtil.getLocation(loc));
                        } else {
                            throw new StageFormatException("locations-to-reach has invalid formatting" + loc, quest, 
                                    stageNum);
                        }
                    }
                } else {
                    throw new StageFormatException("locations-to-reach is not a list of locations", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".reach-location-radii")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".reach-location-radii"), Integer.class)) {
                        final List<Integer> radii = config.getIntegerList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".reach-location-radii");
                        for (final int i : radii) {
                            oStage.radiiToReachWithin.add(i);
                        }
                    } else {
                        throw new StageFormatException("reach-location-radii is not a list of numbers", quest, 
                                stageNum);
                    }
                } else {
                    throw new StageFormatException("reach-location-radii is missing", quest, stageNum);
                }
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".reach-location-names")) {
                    if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".reach-location-names"), String.class)) {
                        final List<String> locationNames = config.getStringList("quests." + questKey + ".stages.ordered." 
                            + stageNum + ".reach-location-names");
                        for (final String name : locationNames) {
                            oStage.locationNames.add(name);
                        }
                    } else {
                        throw new StageFormatException("reach-location-names is not a list of names", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("reach-location-names is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mobs-to-tame")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".mobs-to-tame"), String.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".mob-tame-amounts")) {
                        if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".mob-tame-amounts"), Integer.class)) {
                            final List<String> mobs = config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".mobs-to-tame");
                            final List<Integer> mobAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".mob-tame-amounts");
                            for (final String mob : mobs) {
                                if (Tameable.class.isAssignableFrom(EntityType.valueOf(mob.toUpperCase())
                                        .getEntityClass())) {
                                    oStage.mobsToTame.put(EntityType.valueOf(mob.toUpperCase()), 
                                            mobAmounts.get(mobs.indexOf(mob)));
                                } else {
                                    throw new StageFormatException("mobs-to-tame has invalid tameable mob " + mob, 
                                            quest, stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("mob-tame-amounts is not a list of numbers", quest, 
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("mob-tame-amounts is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("mobs-to-tame is not a list of mob names", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".sheep-to-shear")) {
                if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".sheep-to-shear"), String.class)) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".sheep-amounts")) {
                        if (ConfigUtil.checkList(config.getList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".sheep-amounts"), Integer.class)) {
                            final List<String> sheep = config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".sheep-to-shear");
                            final List<Integer> shearAmounts = config.getIntegerList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".sheep-amounts");
                            for (final String color : sheep) {
                                DyeColor dc = null;
                                if (color.equalsIgnoreCase("NULL")) {
                                    dc = DyeColor.WHITE;
                                }
                                try {
                                    if (dc == null) {
                                        dc = DyeColor.valueOf(color.toUpperCase());
                                    }
                                } catch (final IllegalArgumentException e) {
                                    // Fail silently
                                }
                                if (dc != null) {
                                    oStage.sheepToShear.put(dc, shearAmounts.get(sheep.indexOf(color)));
                                // Legacy start -->
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_BLACK"))) {
                                    oStage.sheepToShear.put(DyeColor.BLACK, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_BLUE"))) {
                                    oStage.sheepToShear.put(DyeColor.BLUE, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_BROWN"))) {
                                    oStage.sheepToShear.put(DyeColor.BROWN, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_CYAN"))) {
                                    oStage.sheepToShear.put(DyeColor.CYAN, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_GRAY"))) {
                                    oStage.sheepToShear.put(DyeColor.GRAY, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_GREEN"))) {
                                    oStage.sheepToShear.put(DyeColor.GREEN, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_LIGHT_BLUE"))) {
                                    oStage.sheepToShear.put(DyeColor.LIGHT_BLUE, 
                                            shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_LIME"))) {
                                    oStage.sheepToShear.put(DyeColor.LIME, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_MAGENTA"))) {
                                    oStage.sheepToShear.put(DyeColor.MAGENTA, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_ORANGE"))) {
                                    oStage.sheepToShear.put(DyeColor.ORANGE, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_PINK"))) {
                                    oStage.sheepToShear.put(DyeColor.PINK, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_PURPLE"))) {
                                    oStage.sheepToShear.put(DyeColor.PURPLE, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_RED"))) {
                                    oStage.sheepToShear.put(DyeColor.RED, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_SILVER"))) {
                                    // 1.13 changed DyeColor.SILVER -> DyeColor.LIGHT_GRAY
                                    oStage.sheepToShear.put(DyeColor.getByColor(Color.SILVER), 
                                            shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_WHITE"))) {
                                    oStage.sheepToShear.put(DyeColor.WHITE, shearAmounts.get(sheep.indexOf(color)));
                                } else if (color.equalsIgnoreCase(Lang.get("COLOR_YELLOW"))) {
                                    oStage.sheepToShear.put(DyeColor.YELLOW, shearAmounts.get(sheep.indexOf(color)));
                                // <-- Legacy end
                                } else {
                                    throw new StageFormatException("sheep-to-shear has invalid color " + color, quest,
                                            stageNum);
                                }
                            }
                        } else {
                            throw new StageFormatException("sheep-amounts is not a list of numbers", quest, stageNum);
                        }
                    } else {
                        throw new StageFormatException("sheep-amounts is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("sheep-to-shear is not a list of colors", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".password-displays")) {
                final List<String> displays = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".password-displays");
                if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".password-phrases")) {
                    final List<String> phrases = config.getStringList("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".password-phrases");
                    if (displays.size() == phrases.size()) {
                        for (int passIndex = 0; passIndex < displays.size(); passIndex++) {
                            oStage.passwordDisplays.add(displays.get(passIndex));
                            final LinkedList<String> answers = new LinkedList<String>();
                            answers.addAll(Arrays.asList(phrases.get(passIndex).split("\\|")));
                            oStage.passwordPhrases.add(answers);
                        }
                    } else {
                        throw new StageFormatException("password-displays and password-phrases are not the same size",
                                quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("password-phrases is missing", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".objective-override")) {
                final Object o = config.get("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".objective-override");
                if (o instanceof List) {
                    oStage.objectiveOverrides.addAll(config.getStringList("quests." + questKey 
                            + ".stages.ordered." + stageNum + ".objective-override"));
                } else {
                    // Legacy
                    final String s = config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".objective-override");
                    oStage.objectiveOverrides.add(s);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".start-event")) {
                final Action action = loadAction(config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".start-event"));
                if (action != null) {
                    oStage.startAction = action;
                } else {
                    throw new StageFormatException("start-event failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".finish-event")) {
                final Action action = loadAction(config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".finish-event"));
                if (action != null) {
                    oStage.finishAction = action;
                } else {
                    throw new StageFormatException("finish-event failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".fail-event")) {
                final Action action = loadAction(config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".fail-event"));
                if (action != null) {
                    oStage.failAction = action;
                } else {
                    throw new StageFormatException("fail-event failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".death-event")) {
                final Action action = loadAction(config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".death-event"));
                if (action != null) {
                    oStage.deathAction = action;
                } else {
                    throw new StageFormatException("death-event failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".disconnect-event")) {
                final Action action = loadAction(config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".disconnect-event"));
                if (action != null) {
                    oStage.disconnectAction = action;
                } else {
                    throw new StageFormatException("disconnect-event failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".chat-events")) {
                if (config.isList("quests." + questKey + ".stages.ordered." + stageNum + ".chat-events")) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".chat-event-triggers")) {
                        if (config.isList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".chat-event-triggers")) {
                            final List<String> chatEvents = config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".chat-events");
                            final List<String> chatEventTriggers = config.getStringList("quests." + questKey 
                                    + ".stages.ordered." + stageNum + ".chat-event-triggers");
                            boolean loadEventFailed = false;
                            for (int i = 0; i < chatEvents.size(); i++) {
                                final Action action = loadAction(chatEvents.get(i));
                                if (action != null) {
                                    if (i < chatEventTriggers.size()) {
                                        oStage.chatActions.put(chatEventTriggers.get(i), action);
                                    } else {
                                        throw new StageFormatException("chat-event-triggers list is too small", 
                                                quest, stageNum);
                                    }
                                } else {
                                    loadEventFailed = true;
                                    throw new StageFormatException("chat-events failed to load " + chatEvents.get(i),
                                            quest, stageNum);
                                }
                            }
                            if (loadEventFailed) {
                                break;
                            }
                        } else {
                            throw new StageFormatException("chat-event-triggers is not in list format", quest,
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("chat-event-triggers is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("chat-events is not in list format", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".command-events")) {
                if (config.isList("quests." + questKey + ".stages.ordered." + stageNum + ".command-events")) {
                    if (config.contains("quests." + questKey + ".stages.ordered." + stageNum 
                            + ".command-event-triggers")) {
                        if (config.isList("quests." + questKey + ".stages.ordered." + stageNum 
                                + ".command-event-triggers")) {
                            final List<String> commandEvents = config.getStringList("quests." + questKey + ".stages.ordered." 
                                    + stageNum + ".command-events");
                            final List<String> commandEventTriggers = config.getStringList("quests." + questKey 
                                    + ".stages.ordered." + stageNum + ".command-event-triggers");
                            boolean loadEventFailed = false;
                            for (int i = 0; i < commandEvents.size(); i++) {
                                final Action action = loadAction(commandEvents.get(i));
                                if (action != null) {
                                    if (i < commandEventTriggers.size()) {
                                        oStage.commandActions.put(commandEventTriggers.get(i), action);
                                    } else {
                                        throw new StageFormatException("command-event-triggers list is too small", 
                                                quest, stageNum);
                                    }
                                } else {
                                    loadEventFailed = true;
                                    throw new StageFormatException("command-events failed to load " 
                                            + commandEvents.get(i), quest, stageNum);
                                }
                            }
                            if (loadEventFailed) {
                                break;
                            }
                        } else {
                            throw new StageFormatException("command-event-triggers is not in list format", quest,
                                    stageNum);
                        }
                    } else {
                        throw new StageFormatException("command-event-triggers is missing", quest, stageNum);
                    }
                } else {
                    throw new StageFormatException("command-events is not in list format", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".condition")) {
                final Condition condition = loadCondition(config.getString("quests." + questKey + ".stages.ordered." 
                        + stageNum + ".condition"));
                if (condition != null) {
                    oStage.condition = condition;
                } else {
                    throw new StageFormatException("condition failed to load", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".delay")) {
                if (config.getLong("quests." + questKey + ".stages.ordered." + stageNum + ".delay", -999) != -999) {
                    oStage.delay = config.getInt("quests." + questKey + ".stages.ordered." + stageNum + ".delay")
                            * 1000;
                } else {
                    throw new StageFormatException("delay is not a number", quest, stageNum);
                }
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".delay-message")) {
                oStage.delayMessage = config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".delay-message");
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".start-message")) {
                oStage.startMessage = config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".start-message");
            }
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".complete-message")) {
                oStage.completeMessage = config.getString("quests." + questKey + ".stages.ordered." + stageNum 
                        + ".complete-message");
            }
            final LinkedList<Integer> ids = new LinkedList<Integer>();
            if (npcIdsToTalkTo != null) {
                ids.addAll(npcIdsToTalkTo);
            }
            oStage.citizensToInteract = ids;
            quest.getStages().add(oStage);
        }
    }
    
    protected Action loadAction(final String name) throws ActionFormatException {
        if (name == null) {
            return null;
        }
        final File legacy = new File(getDataFolder(), "events.yml");
        final File actions = new File(getDataFolder(), "actions.yml");
        final FileConfiguration data = new YamlConfiguration();
        try {
            if (actions.isFile()) {
                data.load(actions);
            } else {
                data.load(legacy);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
        }
        final String legacyName = "events." + name;
        String actionKey = "actions." + name + ".";
        if (data.contains(legacyName)) {
            actionKey = legacyName + ".";
        }
        final Action action = new Action(this);
        action.setName(name);
        if (data.contains(actionKey + "message")) {
            action.setMessage(ConfigUtil.parseString(data.getString(actionKey + "message")));
        }
        if (data.contains(actionKey + "open-book")) {
            action.setBook(data.getString(actionKey + "open-book"));
        }
        if (data.contains(actionKey + "clear-inventory")) {
            if (data.isBoolean(actionKey + "clear-inventory")) {
                action.setClearInv(data.getBoolean(actionKey + "clear-inventory"));
            } else {
                throw new ActionFormatException("clear-inventory is not a true/false value", actionKey);
            }
        }
        if (data.contains(actionKey + "fail-quest")) {
            if (data.isBoolean(actionKey + "fail-quest")) {
                action.setFailQuest(data.getBoolean(actionKey + "fail-quest"));
            } else {
                throw new ActionFormatException("fail-quest is not a true/false value", actionKey);
            }
        }
        if (data.contains(actionKey + "explosions")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "explosions"), String.class)) {
                final LinkedList<Location> explosions = new LinkedList<Location>();
                for (final String s : data.getStringList(actionKey + "explosions")) {
                    final Location loc = ConfigUtil.getLocation(s);
                    if (loc == null) {
                        throw new ActionFormatException("explosions is not in proper \"WorldName x y z\" format",
                                actionKey);
                    }
                    explosions.add(loc);
                }
                action.setExplosions(explosions);
            } else {
                throw new ActionFormatException("explosions is not a list of locations", actionKey);
            }
        }
        if (data.contains(actionKey + "effects")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "effects"), String.class)) {
                if (data.contains(actionKey + "effect-locations")) {
                    if (ConfigUtil.checkList(data.getList(actionKey + "effect-locations"), String.class)) {
                        final List<String> effectList = data.getStringList(actionKey + "effects");
                        final List<String> effectLocs = data.getStringList(actionKey + "effect-locations");
                        final Map<Location, Effect> effects = new HashMap<Location, Effect>();
                        for (final String s : effectList) {
                            Effect effect = null;
                            try {
                                effect = Effect.valueOf(s.toUpperCase());
                            } catch (final IllegalArgumentException e) {
                                throw new ActionFormatException(s + " is not a valid effect name",
                                        actionKey);
                            }
                            final Location l = ConfigUtil.getLocation(effectLocs.get(effectList.indexOf(s)));
                            if (l == null) {
                                throw new ActionFormatException("effect-locations is not in proper \"WorldName x y z\""
                                        + "format", actionKey);
                            }
                            effects.put(l, effect);
                        }
                        action.setEffects(effects);
                    } else {
                        throw new ActionFormatException("effect-locations is not a list of locations", actionKey);
                    }
                } else {
                    throw new ActionFormatException("effect-locations is missing", actionKey);
                }
            } else {
                throw new ActionFormatException("effects is not a list of effects", actionKey);
            }
        }
        if (data.contains(actionKey + "items")) {
            final LinkedList<ItemStack> temp = new LinkedList<ItemStack>();
            @SuppressWarnings("unchecked")
            final
            List<ItemStack> stackList = (List<ItemStack>) data.get(actionKey + "items");
            if (ConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            } else {
                // Legacy
                if (ConfigUtil.checkList(stackList, String.class)) {
                    final List<String> items = data.getStringList(actionKey + "items");
                    for (final String item : items) {
                        try {
                            final ItemStack stack = ItemUtil.readItemStack(item);
                            if (stack != null) {
                                temp.add(stack);
                            }
                        } catch (final Exception e) {
                            throw new ActionFormatException("items is not formatted properly", actionKey);
                        }
                    }
                } else {
                    throw new ActionFormatException("items is not a list of items", actionKey);
                }
            }
            action.setItems(temp);
        }
        if (data.contains(actionKey + "storm-world")) {
            final World stormWorld = getServer().getWorld(data.getString(actionKey + "storm-world"));
            if (stormWorld == null) {
                throw new ActionFormatException("storm-world is not a valid world name", actionKey);
            }
            if (data.contains(actionKey + "storm-duration")) {
                if (data.getInt(actionKey + "storm-duration", -999) != -999) {
                    action.setStormDuration(data.getInt(actionKey + "storm-duration") * 1000);
                } else {
                    throw new ActionFormatException("storm-duration is not a number", actionKey);
                }
                action.setStormWorld(stormWorld);
            } else {
                throw new ActionFormatException("storm-duration is missing", actionKey);
            }
        }
        if (data.contains(actionKey + "thunder-world")) {
            final World thunderWorld = getServer().getWorld(data.getString(actionKey + "thunder-world"));
            if (thunderWorld == null) {
                throw new ActionFormatException("thunder-world is not a valid world name", actionKey);
            }
            if (data.contains(actionKey + "thunder-duration")) {
                if (data.getInt(actionKey + "thunder-duration", -999) != -999) {
                    action.setThunderDuration(data.getInt(actionKey + "thunder-duration"));
                } else {
                    throw new ActionFormatException("thunder-duration is not a number", actionKey);
                }
                action.setThunderWorld(thunderWorld);
            } else {
                throw new ActionFormatException("thunder-duration is missing", actionKey);
            }
        }
        if (data.contains(actionKey + "mob-spawns")) {
            final ConfigurationSection section = data.getConfigurationSection(actionKey + "mob-spawns");
            final LinkedList<QuestMob> mobSpawns = new LinkedList<QuestMob>();
            for (final String s : section.getKeys(false)) {
                final String mobName = section.getString(s + ".name");
                final Location spawnLocation = ConfigUtil.getLocation(section.getString(s + ".spawn-location"));
                final EntityType type = MiscUtil.getProperMobType(section.getString(s + ".mob-type"));
                final Integer mobAmount = section.getInt(s + ".spawn-amounts");
                if (spawnLocation == null) {
                    throw new ActionFormatException("mob-spawn-locations is not in proper \"WorldName x y z\" format",
                            actionKey);
                }
                if (type == null) {
                    throw new ActionFormatException("mob-spawn-types is not a list of mob types", actionKey);
                }
                final ItemStack[] inventory = new ItemStack[5];
                final Float[] dropChances = new Float[5];
                inventory[0] = ItemUtil.readItemStack(section.getString(s + ".held-item"));
                dropChances[0] = (float) section.getDouble(s + ".held-item-drop-chance");
                inventory[1] = ItemUtil.readItemStack(section.getString(s + ".boots"));
                dropChances[1] = (float) section.getDouble(s + ".boots-drop-chance");
                inventory[2] = ItemUtil.readItemStack(section.getString(s + ".leggings"));
                dropChances[2] = (float) section.getDouble(s + ".leggings-drop-chance");
                inventory[3] = ItemUtil.readItemStack(section.getString(s + ".chest-plate"));
                dropChances[3] = (float) section.getDouble(s + ".chest-plate-drop-chance");
                inventory[4] = ItemUtil.readItemStack(section.getString(s + ".helmet"));
                dropChances[4] = (float) section.getDouble(s + ".helmet-drop-chance");
                final QuestMob questMob = new QuestMob(type, spawnLocation, mobAmount);
                questMob.setInventory(inventory);
                questMob.setDropChances(dropChances);
                questMob.setName(mobName);
                mobSpawns.add(questMob);
            }
            action.setMobSpawns(mobSpawns);
        }
        if (data.contains(actionKey + "lightning-strikes")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "lightning-strikes"), String.class)) {
                final LinkedList<Location> lightningStrikes = new LinkedList<Location>();
                for (final String s : data.getStringList(actionKey + "lightning-strikes")) {
                    final Location loc = ConfigUtil.getLocation(s);
                    if (loc == null) {
                        throw new ActionFormatException("lightning-strikes is not in proper \"WorldName x y z\" format",
                                actionKey);
                    }
                    lightningStrikes.add(loc);
                }
                action.setLightningStrikes(lightningStrikes);
            } else {
                throw new ActionFormatException("lightning-strikes is not a list of locations", actionKey);
            }
        }
        if (data.contains(actionKey + "commands")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "commands"), String.class)) {
                final LinkedList<String> commands = new LinkedList<String>();
                for (String s : data.getStringList(actionKey + "commands")) {
                    if (s.startsWith("/")) {
                        s = s.replaceFirst("/", "");
                    }
                    commands.add(s);
                }
                action.setCommands(commands);
            } else {
                throw new ActionFormatException("commands is not a list of commands", actionKey);
            }
        }
        if (data.contains(actionKey + "potion-effect-types")) {
            if (ConfigUtil.checkList(data.getList(actionKey + "potion-effect-types"), String.class)) {
                if (data.contains(actionKey + "potion-effect-durations")) {
                    if (ConfigUtil.checkList(data.getList(actionKey + "potion-effect-durations"), Integer.class)) {
                        if (data.contains(actionKey + "potion-effect-amplifiers")) {
                            if (ConfigUtil.checkList(data.getList(actionKey + "potion-effect-amplifiers"), 
                                    Integer.class)) {
                                final List<String> types = data.getStringList(actionKey + "potion-effect-types");
                                final List<Integer> durations = data.getIntegerList(actionKey + "potion-effect-durations");
                                final List<Integer> amplifiers = data.getIntegerList(actionKey + "potion-effect-amplifiers");
                                final LinkedList<PotionEffect> potionEffects = new LinkedList<PotionEffect>();
                                for (final String s : types) {
                                    final PotionEffectType type = PotionEffectType.getByName(s);
                                    if (type == null) {
                                        throw new ActionFormatException("potion-effect-types is not a list of potion "
                                                + "effect types", actionKey);
                                    }
                                    final PotionEffect effect = new PotionEffect(type, durations
                                            .get(types.indexOf(s)), amplifiers.get(types.indexOf(s)));
                                    potionEffects.add(effect);
                                }
                                action.setPotionEffects(potionEffects);
                            } else {
                                throw new ActionFormatException("potion-effect-amplifiers is not a list of numbers",
                                        actionKey);
                            }
                        } else {
                            throw new ActionFormatException("potion-effect-amplifiers is missing", actionKey);
                        }
                    } else {
                        throw new ActionFormatException("potion-effect-durations is not a list of numbers", actionKey);
                    }
                } else {
                    throw new ActionFormatException("potion-effect-durations is missing", actionKey);
                }
            } else {
                throw new ActionFormatException("potion-effect-types is not a list of potion effects", actionKey);
            }
        }
        if (data.contains(actionKey + "hunger")) {
            if (data.getInt(actionKey + "hunger", -999) != -999) {
                action.setHunger(data.getInt(actionKey + "hunger"));
            } else {
                throw new ActionFormatException("hunger is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "saturation")) {
            if (data.getInt(actionKey + "saturation", -999) != -999) {
                action.setSaturation(data.getInt(actionKey + "saturation"));
            } else {
                throw new ActionFormatException("saturation is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "health")) {
            if (data.getInt(actionKey + "health", -999) != -999) {
                action.setHealth(data.getInt(actionKey + "health"));
            } else {
                throw new ActionFormatException("health is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "teleport-location")) {
            if (data.isString(actionKey + "teleport-location")) {
                final Location teleport = ConfigUtil.getLocation(data.getString(actionKey + "teleport-location"));
                if (teleport == null) {
                    throw new ActionFormatException("teleport-location is not in proper \"WorldName x y z\" format",
                            actionKey);
                }
                action.setTeleport(teleport);
            } else {
                throw new ActionFormatException("teleport-location is not a location", actionKey);
            }
        }
        if (data.contains(actionKey + "timer")) {
            if (data.isInt(actionKey + "timer")) {
                action.setTimer(data.getInt(actionKey + "timer"));
            } else {
                throw new ActionFormatException("timer is not a number", actionKey);
            }
        }
        if (data.contains(actionKey + "cancel-timer")) {
            if (data.isBoolean(actionKey + "cancel-timer")) {
                action.setCancelTimer(data.getBoolean(actionKey + "cancel-timer"));
            } else {
                throw new ActionFormatException("cancel-timer is not a true/false value", actionKey);
            }
        }
        if (data.contains(actionKey + "denizen-script")) {
            action.setDenizenScript(data.getString(actionKey + "denizen-script"));
        }
        return action;
    }
    
    protected Condition loadCondition(final String name) throws ConditionFormatException {
        if (name == null) {
            return null;
        }
        final File conditions = new File(getDataFolder(), "conditions.yml");
        final FileConfiguration data = new YamlConfiguration();
        try {
            if (conditions.isFile()) {
                data.load(conditions);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final InvalidConfigurationException e) {
            e.printStackTrace();
        }
        final String conditionKey = "conditions." + name + ".";
        final Condition condition = new Condition(this);
        condition.setName(name);
        if (data.contains(conditionKey + "fail-quest")) {
            if (data.isBoolean(conditionKey + "fail-quest")) {
                condition.setFailQuest(data.getBoolean(conditionKey + "fail-quest"));
            } else {
                throw new ConditionFormatException("fail-quest is not a true/false value", conditionKey);
            }
        }
        if (data.contains(conditionKey + "ride-entity")) {
            if (ConfigUtil.checkList(data.getList(conditionKey + "ride-entity"), String.class)) {
                final LinkedList<String> entities = new LinkedList<String>();
                for (final String s : data.getStringList(conditionKey + "ride-entity")) {
                    final EntityType e = MiscUtil.getProperMobType(s);
                    if (e == null) {
                        throw new ConditionFormatException("ride-entity is not a valid entity type",
                                conditionKey);
                    }
                    entities.add(s);
                }
                condition.setEntitiesWhileRiding(entities);
            } else {
                throw new ConditionFormatException("ride-entity is not a list of entity types", conditionKey);
            }
        }
        if (data.contains(conditionKey + "ride-npc")) {
            if (ConfigUtil.checkList(data.getList(conditionKey + "ride-npc"), Integer.class)) {
                final LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (final int i : data.getIntegerList(conditionKey + "ride-npc")) {
                    if (i < 0) {
                        throw new ConditionFormatException("ride-npc is not a valid NPC ID",
                                conditionKey);
                    }
                    npcs.add(i);
                }
                condition.setNpcsWhileRiding(npcs);
            } else {
                throw new ConditionFormatException("ride-npc is not a list of NPC IDs", conditionKey);
            }
        }
        if (data.contains(conditionKey + "permission")) {
            if (ConfigUtil.checkList(data.getList(conditionKey + "permission"), String.class)) {
                final LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(data.getStringList(conditionKey + "permission"));
                condition.setPermissions(permissions);
            } else {
                throw new ConditionFormatException("permission is not a list of permissions", conditionKey);
            }
        }
        if (data.contains(conditionKey + "hold-main-hand")) {
            final LinkedList<ItemStack> temp = new LinkedList<ItemStack>();
            @SuppressWarnings("unchecked")
            final
            List<ItemStack> stackList = (List<ItemStack>) data.get(conditionKey + "hold-main-hand");
            if (ConfigUtil.checkList(stackList, ItemStack.class)) {
                for (final ItemStack stack : stackList) {
                    if (stack != null) {
                        temp.add(stack);
                    }
                }
            }
            condition.setItemsWhileHoldingMainHand(temp);
        }
        if (data.contains(conditionKey + "stay-within-world")) {
            if (ConfigUtil.checkList(data.getList(conditionKey + "stay-within-world"), String.class)) {
                final LinkedList<String> worlds = new LinkedList<String>();
                for (final String s : data.getStringList(conditionKey + "stay-within-world")) {
                    final World w = getServer().getWorld(s);
                    if (w == null) {
                        throw new ConditionFormatException("stay-within-world is not a valid world",
                                conditionKey);
                    }
                    worlds.add(s);
                }
                condition.setWorldsWhileStayingWithin(worlds);
            } else {
                throw new ConditionFormatException("stay-within-world is not a list of worlds", conditionKey);
            }
        }
        if (data.contains(conditionKey + "stay-within-biome")) {
            if (ConfigUtil.checkList(data.getList(conditionKey + "stay-within-biome"), String.class)) {
                final LinkedList<String> biomes = new LinkedList<String>();
                for (final String s : data.getStringList(conditionKey + "stay-within-biome")) {
                    final Biome b = MiscUtil.getProperBiome(s);
                    if (b == null) {
                        throw new ConditionFormatException("stay-within-biome is not a valid biome",
                                conditionKey);
                    }
                    biomes.add(s);
                }
                condition.setBiomesWhileStayingWithin(biomes);
            } else {
                throw new ConditionFormatException("stay-within-biome is not a list of biomes", conditionKey);
            }
        }
        if (data.contains(conditionKey + "stay-within-region")) {
            if (ConfigUtil.checkList(data.getList(conditionKey + "stay-within-region"), String.class)) {
                final LinkedList<String> regions = new LinkedList<String>();
                for (final String s : data.getStringList(conditionKey + "stay-within-region")) {
                    boolean exists = false;
                    for (final World world : getServer().getWorlds()) {
                        if (getDependencies().getWorldGuardApi().getRegionManager(world) != null) {
                            if (getDependencies().getWorldGuardApi().getRegionManager(world).hasRegion(s)) {
                                regions.add(s);
                                exists = true;
                                break;
                            }
                        }
                    }
                    if (!exists) {
                        throw new ConditionFormatException("region has invalid WorldGuard region name", conditionKey);
                    }
                }
                condition.setRegionsWhileStayingWithin(regions);
            } else {
                throw new ConditionFormatException("stay-within-region is not a list of regions", conditionKey);
            }
        }
        if (data.contains(conditionKey + "check-placeholder-id")) {
            if (ConfigUtil.checkList(data.getList(conditionKey + "check-placeholder-id"), String.class)) {
                if (data.contains(conditionKey + "check-placeholder-value")) {
                    if (ConfigUtil.checkList(data.getList(conditionKey + "check-placeholder-value"), String.class)) {
                        final LinkedList<String> identifiers = new LinkedList<String>();
                        final LinkedList<String> values = new LinkedList<String>();
                        identifiers.addAll(data.getStringList(conditionKey + "check-placeholder-id"));
                        values.addAll(data.getStringList(conditionKey + "check-placeholder-value"));
                        condition.setPlaceholdersCheckIdentifier(identifiers);
                        condition.setPlaceholdersCheckValue(values);
                    } else {
                        throw new ConditionFormatException("check-placeholder-value is not a list of values", conditionKey);
                    }
                }
            } else {
                throw new ConditionFormatException("check-placeholder-id is not a list of identifiers", conditionKey);
            }
        }
        return condition;
    }
    
    private void loadCustomSections(final Quest quest, final FileConfiguration config, final String questKey)
            throws StageFormatException, QuestFormatException {
        final ConfigurationSection questStages = config.getConfigurationSection("quests." + questKey + ".stages.ordered");
        for (final String stageNum : questStages.getKeys(false)) {
            if (quest == null) {
                getLogger().warning("Unable to load custom objectives because quest for " + questKey + " was null");
                return;
            }
            if (quest.getStage(Integer.valueOf(stageNum) - 1) == null) {
                getLogger().severe("Unable to load custom objectives because stage" + (Integer.valueOf(stageNum) - 1) 
                        + " for " + quest.getName() + " was null");
                return;
            }
            final Stage oStage = quest.getStage(Integer.valueOf(stageNum) - 1);
            oStage.customObjectives = new LinkedList<>();
            oStage.customObjectiveCounts = new LinkedList<>();
            oStage.customObjectiveData = new LinkedList<>();
            oStage.customObjectiveDisplays = new LinkedList<>();
            if (config.contains("quests." + questKey + ".stages.ordered." + stageNum + ".custom-objectives")) {
                final ConfigurationSection sec = config.getConfigurationSection("quests." + questKey + ".stages.ordered." 
                        + stageNum + ".custom-objectives");
                for (final String path : sec.getKeys(false)) {
                    final String name = sec.getString(path + ".name");
                    final int count = sec.getInt(path + ".count");
                    Optional<CustomObjective> found = Optional.empty();
                    for (final CustomObjective cr : customObjectives) {
                        if (cr.getName().equalsIgnoreCase(name)) {
                            found = Optional.of(cr);
                            break;
                        }
                    }
                    if (!found.isPresent()) {
                        getLogger().warning("Custom objective \"" + name + "\" for Stage " + stageNum + " of Quest \"" 
                                + quest.getName() + "\" could not be found!");
                        continue;
                    } else {
                        final ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                        oStage.customObjectives.add(found.get());
                        if (count <= 0) {
                            oStage.customObjectiveCounts.add(0);
                        } else {
                            oStage.customObjectiveCounts.add(count);
                        }
                        for (final Entry<String,Object> prompt : found.get().getData()) {
                            final Entry<String, Object> data = populateCustoms(sec2, prompt);
                            oStage.customObjectiveData.add(data);
                        }
                    }
                }
            }
        }
        final Rewards rews = quest.getRewards();
        if (config.contains("quests." + questKey + ".rewards.custom-rewards")) {
            final ConfigurationSection sec = config.getConfigurationSection("quests." + questKey + ".rewards.custom-rewards");
            final Map<String, Map<String, Object>> temp = new HashMap<String, Map<String, Object>>();
            for (final String path : sec.getKeys(false)) {
                final String name = sec.getString(path + ".name");
                Optional<CustomReward>found = Optional.empty();
                for (final CustomReward cr : customRewards) {
                    if (cr.getName().equalsIgnoreCase(name)) {
                        found=Optional.of(cr);
                        break;
                    }
                }
                if (!found.isPresent()) {
                    getLogger().warning("Custom reward \"" + name + "\" for Quest \"" + quest.getName() 
                            + "\" could not be found!");
                    continue;
                } else {
                    final ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                    final Map<String, Object> data = populateCustoms(sec2, found.get().getData());
                    temp.put(name, data);
                }
            }
            rews.setCustomRewards(temp);
        }
        final Requirements reqs = quest.getRequirements();
        if (config.contains("quests." + questKey + ".requirements.custom-requirements")) {
            final ConfigurationSection sec = config.getConfigurationSection("quests." + questKey 
                    + ".requirements.custom-requirements");
            final Map<String, Map<String, Object>> temp = new HashMap<String, Map<String, Object>>();
            for (final String path : sec.getKeys(false)) {
                final String name = sec.getString(path + ".name");
                Optional<CustomRequirement>found=Optional.empty();
                for (final CustomRequirement cr : customRequirements) {
                    if (cr.getName().equalsIgnoreCase(name)) {
                        found=Optional.of(cr);
                        break;
                    }
                }
                if (!found.isPresent()) {
                    throw new QuestFormatException(name + " custom requirement not found", questKey);
                } else {
                    final ConfigurationSection sec2 = sec.getConfigurationSection(path + ".data");
                    final Map<String, Object> data = populateCustoms(sec2,found.get().getData());
                    temp.put(name, data);
                }
            }
            reqs.setCustomRequirements(temp);
        }
    }
    
    /**
     * Permits use of fallbacks for customs maps<p>
     * 
     * Avoid null objects in datamap by initializing the entry value with empty string if no fallback present.
     * 
     * @param section The section of configuration to check
     * @param datamap The map to process
     * @return Populated map
     */
    private static Map<String, Object> populateCustoms(final ConfigurationSection section, final Map<String, Object> datamap) {
        final Map<String,Object> data = new HashMap<String,Object>();
        if (section != null) {
            for (final String key : datamap.keySet()) {
                data.put(key, section.contains(key) ? section.get(key) : datamap.get(key) != null 
                        ? datamap.get(key) : new String());
            }
        }
        return data;
    }

    /**
     * Permits use of fallbacks for customs entries<p>
     * 
     * Avoid null objects in datamap by initializing the entry value with empty string if no fallback present.
     * 
     * @param section The section of configuration to check
     * @param datamap The entry to process
     * @return Populated entry
     */
    private static Entry<String, Object> populateCustoms(final ConfigurationSection section, final Entry<String, Object> datamap) {
        String key = null;;
        Object value = null;;
        if (section != null) {
            key = datamap.getKey();
            value = datamap.getValue();
        }
        return new AbstractMap.SimpleEntry<String, Object>(key, section.contains(key) ? section.get(key) : value != null 
                ? value : new String());
    }
    
    /**
     * Load actions from file
     */
    public void loadActions() {
        final YamlConfiguration config = new YamlConfiguration();
        final File legacyFile = new File(this.getDataFolder(), "events.yml");
        final File actionsFile = new File(this.getDataFolder(), "actions.yml");
        // Using isFile() because exists() and renameTo() can return false positives
        if (legacyFile.isFile()) {
            getLogger().log(Level.INFO, "Renaming legacy events.yml to actions.yml ...");
            try {
                legacyFile.renameTo(actionsFile);
                if (actionsFile.isFile()) {
                    getLogger().log(Level.INFO, "Success! Deleting legacy events.yml ...");
                    legacyFile.delete();
                    getLogger().log(Level.INFO, "Done!");
                }
            } catch (final Exception e) {
                getLogger().log(Level.WARNING, "Unable to rename events.yml to actions.yml");
                e.printStackTrace();
            }
        }
        if (actionsFile.length() != 0) {
            try {
                if (actionsFile.isFile()) {
                    config.load(actionsFile);
                } else {
                    config.load(legacyFile);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final InvalidConfigurationException e) {
                e.printStackTrace();
            }
            ConfigurationSection sec = config.getConfigurationSection("actions");
            if (sec == null) {
                getLogger().log(Level.INFO,
                        "Could not find section \"actions\" from actions.yml. Trying legacy \"events\"...");
                sec = config.getConfigurationSection("events");
            }
            if (sec != null) {
                for (final String s : sec.getKeys(false)) {
                    Action action = null;
                    try {
                        action = loadAction(s);
                    } catch (final ActionFormatException e) {
                        e.printStackTrace();
                    }
                    if (action != null) {
                        actions.add(action);
                    } else {
                        getLogger().log(Level.SEVERE, "Failed to load Action \"" + s + "\". Skipping.");
                    }
                }
            } else {
                getLogger().log(Level.SEVERE, "Could not find beginning section from actions.yml. Skipping.");
            }
        } else {
            getLogger().log(Level.WARNING, "Empty file actions.yml was not loaded.");
        }
    }
    
    /**
     * Load conditions from file
     */
    public void loadConditions() {
        final YamlConfiguration config = new YamlConfiguration();
        final File conditionsFile = new File(this.getDataFolder(), "conditions.yml");
        // Using isFile() because exists() and renameTo() can return false positives
        if (conditionsFile.length() != 0) {
            try {
                if (conditionsFile.isFile()) {
                    config.load(conditionsFile);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            } catch (final InvalidConfigurationException e) {
                e.printStackTrace();
            }
            final ConfigurationSection sec = config.getConfigurationSection("conditions");
            if (sec != null) {
                for (final String s : sec.getKeys(false)) {
                    Condition condition = null;
                    try {
                        condition = loadCondition(s);
                    } catch (final ConditionFormatException e) {
                        e.printStackTrace();
                    }
                    if (condition != null) {
                        conditions.add(condition);
                    } else {
                        getLogger().log(Level.SEVERE, "Failed to load Condition \"" + s + "\". Skipping.");
                    }
                }
            } else {
                getLogger().log(Level.SEVERE, "Could not find beginning section from conditions.yml. Skipping.");
            }
        } else {
            getLogger().log(Level.WARNING, "Empty file conditions.yml was not loaded.");
        }
    }

    public static SkillType getMcMMOSkill(final String s) {
        return SkillType.getSkill(s);
    }

    /**
     * @deprecated Use InventoryUtil.removeItem(Inventory, ItemStack)
     */
    @Deprecated
    public static boolean removeItem(final Inventory inventory, final ItemStack is) {
        final int amount = is.getAmount();
        final HashMap<Integer, ? extends ItemStack> allItems = inventory.all(is.getType());
        final HashMap<Integer, Integer> removeFrom = new HashMap<Integer, Integer>();
        int foundAmount = 0;
        for (final Map.Entry<Integer, ? extends ItemStack> item : allItems.entrySet()) {
            if (ItemUtil.compareItems(is, item.getValue(), true) == 0) {
                if (item.getValue().getAmount() >= amount - foundAmount) {
                    removeFrom.put(item.getKey(), amount - foundAmount);
                    foundAmount = amount;
                } else {
                    foundAmount += item.getValue().getAmount();
                    removeFrom.put(item.getKey(), item.getValue().getAmount());
                }
                if (foundAmount >= amount) {
                    break;
                }
            }
        }
        if (foundAmount == amount) {
            for (final Map.Entry<Integer, Integer> toRemove : removeFrom.entrySet()) {
                final ItemStack item = inventory.getItem(toRemove.getKey());
                if (item.getAmount() - toRemove.getValue() <= 0) {
                    inventory.clear(toRemove.getKey());
                } else {
                    item.setAmount(item.getAmount() - toRemove.getValue());
                    inventory.setItem(toRemove.getKey(), item);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if player can use Quests
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
     * Checks if conversable is player in trial mode
     *
     * @param conversable the editor user to be checked
     * @return {@code true} if user is a Player with quests.admin.trial permission
     */
    public boolean hasLimitedAccess(final Conversable conversable) {
        if (!(conversable instanceof Player)) {
            return false;
        }
        return ((Player)conversable).hasPermission("quests.admin.trial");
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
        return null;
    }
    
    /**
     * Checks whether a NPC has a quest that the player may accept
     * 
     * @param npc The giver NPC to check
     * @param quester The player to check
     * @return true if at least one available quest has not yet been completed
     */
    public boolean hasQuest(final NPC npc, final Quester quester) {
        for (final Quest q : quests) {
            if (q.npcStart != null && !quester.completedQuests.contains(q)) {
                if (q.npcStart.getId() == npc.getId()) {
                    final boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
                    if (!ignoreLockedQuests || ignoreLockedQuests && q.testRequirements(quester)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    // Unused internally, left for external use
    /**
     * Checks whether a NPC has a quest that the player has already completed
     * 
     * @param npc The giver NPC to check
     * @param quester The player to check
     * @return true if at least one available quest has been completed
     */
    public boolean hasCompletedQuest(final NPC npc, final Quester quester) {
        for (final Quest q : quests) {
            if (q.npcStart != null && quester.completedQuests.contains(q) == true) {
                if (q.npcStart.getId() == npc.getId()) {
                    final boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
                    if (ignoreLockedQuests == false || ignoreLockedQuests == true 
                            && q.testRequirements(quester) == true) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Checks whether a NPC has a repeatable quest that the player has already completed
     * 
     * @param npc The giver NPC to check
     * @param quester The player to check
     * @return true if at least one available, redoable quest has been completed
     */
    public boolean hasCompletedRedoableQuest(final NPC npc, final Quester quester) {
        for (final Quest q : quests) {
            if (q.npcStart != null && quester.completedQuests.contains(q) == true 
                    && q.getPlanner().getCooldown() > -1) {
                if (q.npcStart.getId() == npc.getId()) {
                    final boolean ignoreLockedQuests = settings.canIgnoreLockedQuests();
                    if (ignoreLockedQuests == false || ignoreLockedQuests == true 
                            && q.testRequirements(quester) == true) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
