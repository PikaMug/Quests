/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.player;

import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import io.github.znetworkw.znpcservers.npc.NPC;
import lol.pyr.znpcsplus.api.npc.Npc;
import me.clip.placeholderapi.PlaceholderAPI;
import me.pikamug.localelib.LocaleManager;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.conditions.BukkitCondition;
import me.pikamug.quests.config.BukkitConfigSettings;
import me.pikamug.quests.config.ConfigSettings;
import me.pikamug.quests.convo.misc.QuestAbandonPrompt;
import me.pikamug.quests.dependencies.BukkitDependencies;
import me.pikamug.quests.entity.BukkitCountableMob;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.events.quest.QuestQuitEvent;
import me.pikamug.quests.events.quest.QuestTakeEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPostStartQuestEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPostUpdateObjectiveEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreOpenGUIEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreStartQuestEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreUpdateObjectiveEvent;
import me.pikamug.quests.item.BukkitQuestJournal;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.nms.BukkitActionBarProvider;
import me.pikamug.quests.nms.BukkitTitleProvider;
import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.BukkitObjective;
import me.pikamug.quests.quests.components.BukkitRequirements;
import me.pikamug.quests.quests.components.BukkitStage;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.BukkitStageTimer;
import me.pikamug.quests.util.stack.BlockItemStack;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitInventoryUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.RomanNumeral;
import me.pikamug.unite.api.objects.PartyProvider;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BukkitQuester implements Quester {

    private final BukkitQuestsPlugin plugin;
    private UUID id;
    protected String questIdToTake;
    protected String questIdToQuit;
    private String lastKnownName;
    protected int questPoints = 0;
    private String compassTargetQuestId;
    private long lastNotifiedCondition = 0L;
    protected ConcurrentHashMap<Integer, Quest> timers = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<Quest, Integer>() {

        private static final long serialVersionUID = 6361484975823846780L;

        @Override
        public Integer put(final @NotNull Quest key, final @NotNull Integer val) {
            final Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(final @NotNull Object key) {
            final Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Quest, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    protected ConcurrentSkipListSet<Quest> completedQuests = new ConcurrentSkipListSet<Quest>() {

        private static final long serialVersionUID = -269110128568487000L;

        @Override
        public boolean add(final Quest e) {
            final boolean b = super.add(e);
            updateJournal();
            return b;
        }

        @Override
        public boolean addAll(final @NotNull Collection<? extends Quest> c) {
            final boolean b = super.addAll(c);
            updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public boolean remove(final Object o) {
            final boolean b = super.remove(o);
            updateJournal();
            return b;
        }

        @Override
        public boolean removeAll(final Collection<?> c) {
            final boolean b = super.removeAll(c);
            updateJournal();
            return b;
        }
    };
    protected ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<Quest, Integer>() {

        private static final long serialVersionUID = 5475202358792520975L;

        @Override
        public Integer put(final @NotNull Quest key, final @NotNull Integer val) {
            final Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(final @NotNull Object key) {
            final Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Quest, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    protected ConcurrentHashMap<Quest, BukkitQuestProgress> questProgress
            = new ConcurrentHashMap<Quest, BukkitQuestProgress>() {

        private static final long serialVersionUID = -4607112433003926066L;

        @Override
        public BukkitQuestProgress put(final @NotNull Quest key, final @NotNull BukkitQuestProgress val) {
            final BukkitQuestProgress data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public BukkitQuestProgress remove(final @NotNull Object key) {
            final BukkitQuestProgress data = super.remove(key);
            updateJournal();
            return data;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Quest, ? extends BukkitQuestProgress> m) {
            super.putAll(m);
            updateJournal();
        }
    };

    public BukkitQuester(final BukkitQuestsPlugin plugin, final UUID uuid) {
        this.plugin = plugin;
        this.id = uuid;
        if (getPlayer() != null) {
            this.lastKnownName = getPlayer().getName();
        } else {
            this.lastKnownName = getOfflinePlayer().getName();
        }
    }

    @Override
    public int compareTo(final Quester quester) {
        return id.compareTo(quester.getUUID());
    }

    @Override
    public UUID getUUID() {
        return id;
    }

    @Override
    public void setUUID(final UUID id) {
        this.id = id;
    }

    @Override
    public String getQuestIdToTake() {
        return questIdToTake;
    }

    @Override
    public void setQuestIdToTake(final String questIdToTake) {
        this.questIdToTake = questIdToTake;
    }

    @Override
    public String getQuestIdToQuit() {
        return questIdToQuit;
    }

    @Override
    public void setQuestIdToQuit(final String questIdToQuit) {
        this.questIdToQuit = questIdToQuit;
    }

    @Override
    public String getLastKnownName() {
        return lastKnownName;
    }

    @Override
    public void setLastKnownName(final String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    @Override
    public int getQuestPoints() {
        return questPoints;
    }

    @Override
    public void setQuestPoints(final int questPoints) {
        this.questPoints = questPoints;
    }

    /**
     * Get compass target quest. Returns null if not set
     *
     * @return Quest or null
     */
    @Override
    public Quest getCompassTarget() {
        return compassTargetQuestId != null ? plugin.getQuestById(compassTargetQuestId) : null;
    }

    /**
     * Set compass target quest. Does not update in-game
     *
     * @param quest The target quest
     */
    @Override
    public void setCompassTarget(final Quest quest) {
        if (quest != null) {
            compassTargetQuestId = quest.getId();
        }
    }

    @Override
    public ConcurrentHashMap<Integer, Quest> getTimers() {
        return timers;
    }

    @Override
    public void setTimers(final ConcurrentHashMap<Integer, Quest> timers) {
        this.timers = timers;
    }

    @Override
    public void removeTimer(final Integer timerId) {
        this.timers.remove(timerId);
    }

    @Override
    public ConcurrentHashMap<Quest, Integer> getCurrentQuests() {
        final ConcurrentHashMap<Quest, Integer> map = new ConcurrentHashMap<>();
        for (final Entry<Quest, Integer> cq : currentQuests.entrySet()) {
            final BukkitQuest q = (BukkitQuest) cq.getKey();
            map.put(q, cq.getValue());
        }
        return map;
    }

    @Override
    public void setCurrentQuests(final ConcurrentHashMap<Quest, Integer> currentQuests) {
        this.currentQuests = currentQuests;
    }

    public ConcurrentSkipListSet<Quest> getCompletedQuests() {
        final ConcurrentSkipListSet<Quest> set = new ConcurrentSkipListSet<>();
        for (final Quest iq : completedQuests) {
            final BukkitQuest q = (BukkitQuest) iq;
            set.add(q);
        }
        return set;
    }

    @Override
    public void setCompletedQuests(final ConcurrentSkipListSet<Quest> completedQuests) {
        this.completedQuests = completedQuests;
    }

    @Override
    public ConcurrentHashMap<Quest, Long> getCompletedTimes() {
        return completedTimes;
    }

    @Override
    public void setCompletedTimes(final ConcurrentHashMap<Quest, Long> completedTimes) {
        this.completedTimes = completedTimes;
    }

    @Override
    public ConcurrentHashMap<Quest, Integer> getAmountsCompleted() {
        return amountsCompleted;
    }

    @Override
    public void setAmountsCompleted(final ConcurrentHashMap<Quest, Integer> amountsCompleted) {
        this.amountsCompleted = amountsCompleted;
    }

    public ConcurrentHashMap<Quest, BukkitQuestProgress> getQuestProgress() {
        return questProgress;
    }

    public void setQuestProgress(final ConcurrentHashMap<Quest, BukkitQuestProgress> questProgress) {
        this.questProgress = questProgress;
    }

    @Override
    public Player getPlayer() {
        return plugin.getServer().getPlayer(id);
    }

    @Override
    public OfflinePlayer getOfflinePlayer() {
        return plugin.getServer().getOfflinePlayer(id);
    }

    @Override
    public void sendMessage(final String message) {
        if (getPlayer() == null || !getPlayer().isOnline() || message.trim().isEmpty()) {
            return;
        }
        getPlayer().sendMessage(message);
    }

    @Override
    public Stage getCurrentStage(final Quest quest) {
        if (currentQuests.containsKey(quest)) {
            return quest.getStage(currentQuests.get(quest));
        }
        return null;
    }

    /**
     * Get quest progress for given quest, or default values if not found
     *
     * @deprecated Use {@link #getQuestProgressOrDefault(Quest)} instead
     * @param quest The quest to check
     * @return Existing or current progress, or default
     */
    public QuestProgress getQuestDataOrDefault(final Quest quest) {
        return getQuestProgressOrDefault(quest);
    }

    /**
     * Get quest progress for given quest, or default values if not found
     *
     * @param quest The quest to check
     * @return Existing or current progress, or default
     */
    @Override
    public QuestProgress getQuestProgressOrDefault(final Quest quest) {
        if (questProgress.get(quest) != null) {
            return questProgress.get(quest);
        }
        if (currentQuests.get(quest) != null) {
            addEmptiesFor(quest, currentQuests.get(quest));
        }
        return new BukkitQuestProgress(this);
    }

    @Override
    public boolean hasJournal() {
        return getJournal() != null;
    }

    @Override
    public ItemStack getJournal() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            return null;
        }
        for (final ItemStack is : getPlayer().getInventory().getContents()) {
            if (BukkitItemUtil.isJournal(is)) {
                return is;
            }
        }
        return null;
    }

    @Override
    public int getJournalIndex() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            return -1;
        }
        final ItemStack[] arr = getPlayer().getInventory().getContents();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                if (BukkitItemUtil.isJournal(arr[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void updateJournal() {
        if (getPlayer() == null) {
            return;
        }
        if (!getPlayer().isOnline()) {
            plugin.getLogger().info("Could not update Quests Journal for " + getPlayer().getName() + " while offline");
            return;
        }
        final int index = getJournalIndex();
        if (index != -1) {
            final BukkitQuestJournal journal = new BukkitQuestJournal(plugin, this);
            getPlayer().getInventory().setItem(index, journal.toItemStack());
        }
    }

    /**
     * Check if quest is available and, if so, ask Quester if they would like to start it<p>
     *
     * @param quest The quest to check and then offer
     * @param giveReason Whether to inform Quester of unavailability
     * @return true if successful
     */
    public boolean offerQuest(final Quest quest, final boolean giveReason) {
        if (quest == null) {
            return false;
        }
        final QuestTakeEvent event = new QuestTakeEvent(quest, this);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        if (canAcceptOffer(quest, giveReason)) {
            if (getPlayer() != null) {
                if (!getPlayer().isConversing()) {
                    setQuestIdToTake(quest.getId());
                    final String s = ChatColor.GOLD + BukkitConfigUtil.parseString(BukkitLang.get("questObjectivesTitle")
                            + "\n" + ChatColor.RESET + quest.getDescription(), quest, getPlayer());
                    for (final String msg : s.split("<br>")) {
                        sendMessage(msg);
                    }
                    if (!plugin.getConfigSettings().canConfirmAccept()) {
                        takeQuest(quest, false);
                    } else {
                        plugin.getConversationFactory().buildConversation(getPlayer()).begin();
                    }
                    return true;
                } else {
                    sendMessage(ChatColor.YELLOW + BukkitLang.get(getPlayer(), "alreadyConversing"));
                }
            }
        }
        return false;
    }

    /**
     * Check if quest is available to this Quester<p>
     *
     * @param quest The quest to check
     * @param giveReason Whether to inform Quester of unavailability
     * @return true if available
     */
    public boolean canAcceptOffer(final Quest quest, final boolean giveReason) {
        if (quest == null) {
            return false;
        }
        final BukkitQuest bukkitQuest = (BukkitQuest) quest;
        if (!plugin.getConfigSettings().canAllowCommandsForNpcQuests() && bukkitQuest.getNpcStart() != null
                && getPlayer().getLocation().getWorld() != null) {
            final UUID uuid = bukkitQuest.getNpcStart();
            Entity npc = null;
            if (plugin.getDependencies().getCitizens() != null
                    && plugin.getDependencies().getCitizens().getNPCRegistry().getByUniqueId(uuid) != null) {
                npc = plugin.getDependencies().getCitizens().getNPCRegistry().getByUniqueId(uuid).getEntity();
            } else if (plugin.getDependencies().getZnpcsPlus() != null
                    && plugin.getDependencies().getZnpcsPlusUuids().contains(uuid)) {
                final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(uuid)).findAny();
                if (opt.isPresent()) {
                    npc = (Entity) opt.get().getBukkitEntity();
                }
            }
            if (npc != null && npc.getLocation().getWorld() != null && npc.getLocation().getWorld().getName()
                    .equals(getPlayer().getLocation().getWorld().getName())
                    && npc.getLocation().distance(getPlayer().getLocation()) > 6.0) {
                if (giveReason) {
                    final String msg = BukkitLang.get(getPlayer(), "mustSpeakTo").replace("<npc>", npc.getName());
                    sendMessage(ChatColor.YELLOW + msg);
                }
                return false;
            }
            if (plugin.getDependencies().getZnpcsPlusApi() != null &&
                    plugin.getDependencies().getZnpcsPlusApi().getNpcRegistry().getByUuid(uuid) != null) {
                Npc znpc = plugin.getDependencies().getZnpcsPlusApi().getNpcRegistry().getByUuid(uuid).getNpc();
                if (znpc.getWorld() != null && znpc.getWorld().equals(getPlayer().getWorld()) &&
                        znpc.getLocation().toBukkitLocation(znpc.getWorld()).distance(getPlayer().getLocation()) > 6.0) {
                    if (giveReason) {
                        final String msg = BukkitLang.get(getPlayer(), "mustSpeakTo")
                                .replace("<npc>", plugin.getDependencies().getNpcName(znpc.getUuid()));
                        sendMessage(ChatColor.YELLOW + msg);
                    }
                    return false;
                }
            }
        }
        if (getCurrentQuests().size() >= plugin.getConfigSettings().getMaxQuests()
                && plugin.getConfigSettings().getMaxQuests() > 0) {
            if (giveReason) {
                final String msg = BukkitLang.get(getPlayer(), "questMaxAllowed").replace("<number>",
                        String.valueOf(plugin.getConfigSettings().getMaxQuests()));
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCurrentQuests().containsKey(bukkitQuest)) {
            if (giveReason) {
                final String msg = BukkitLang.get(getPlayer(), "questAlreadyOn");
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCompletedQuests().contains(bukkitQuest) && bukkitQuest.getPlanner().getCooldown() < 0) {
            if (giveReason) {
                final String msg = BukkitLang.get(getPlayer(), "questAlreadyCompleted")
                        .replace("<quest>", bukkitQuest.getName());
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (bukkitQuest.getBlockStart() != null) {
            if (giveReason) {
                final String msg = BukkitLang.get(getPlayer(), "noCommandStart").replace("<quest>",
                        bukkitQuest.getName());
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCompletedQuests().contains(bukkitQuest) && getRemainingCooldown(bukkitQuest) > 0
                && !bukkitQuest.getPlanner().getOverride()) {
            if (giveReason) {
                final String msg = BukkitLang.get(getPlayer(), "questTooEarly").replace("<quest>",
                        bukkitQuest.getName()).replace("<time>", BukkitMiscUtil
                        .getTime(getRemainingCooldown(bukkitQuest)));
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (bukkitQuest.getRegionStart() != null) {
            if (!bukkitQuest.isInRegionStart(this)) {
                if (giveReason) {
                    final String msg = BukkitLang.get(getPlayer(), "questInvalidLocation").replace("<quest>",
                            bukkitQuest.getName());
                    getPlayer().sendMessage(ChatColor.YELLOW + msg);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Check if Quester is too early or late for a planned quest<p>
     *
     * For player cooldown, use {@link #canAcceptOffer(Quest, boolean)} instead
     *
     * @param quest The quest to check
     * @param giveReason Whether to inform Quester of unpunctuality
     * @return true if on time
     */
    public boolean isOnTime(final Quest quest, final boolean giveReason) {
        final Planner pln = quest.getPlanner();
        final long currentTime = System.currentTimeMillis();
        final long start = pln.getStartInMillis(); // Start time in milliseconds since UTC epoch
        final long end = pln.getEndInMillis(); // End time in milliseconds since UTC epoch
        final long duration = end - start; // How long the quest can be active for
        final long repeat = pln.getRepeat(); // Length to wait in-between start times
        if (start != -1) {
            if (currentTime < start) {
                if (giveReason) {
                    String early = BukkitLang.get("plnTooEarly");
                    early = early.replace("<quest>", quest.getName());
                    early = early.replace("<time>", BukkitMiscUtil.getTime(start - currentTime));
                    sendMessage(ChatColor.YELLOW + early);
                }
                return false;
            }
        }
        if (end != -1 && repeat == -1) {
            if (currentTime > end) {
                if (giveReason) {
                    String late = BukkitLang.get("plnTooLate");
                    late = late.replace("<quest>", quest.getName());
                    late = late.replace("<time>", BukkitMiscUtil.getTime(currentTime - end));
                    sendMessage(ChatColor.RED + late);
                }
                return false;
            }
        }
        if (repeat != -1 && start != -1 && end != -1) {
            // Ensure that we're past the initial duration
            if (currentTime > end) {
                final int maxSize = 2;
                final LinkedHashMap<Long, Long> mostRecent = new LinkedHashMap<Long, Long>() {
                    private static final long serialVersionUID = 3046838061019897713L;

                    @Override
                    protected boolean removeEldestEntry(final Map.Entry<Long, Long> eldest) {
                        return size() > maxSize;
                    }
                };

                // Get last completed time
                long completedTime = 0L;
                if (getCompletedTimes().containsKey(quest)) {
                    completedTime = getCompletedTimes().get(quest);
                }
                long completedEnd = 0L;

                // Store last completed, upcoming, and most recent periods of activity
                long nextStart = start;
                long nextEnd = end;
                while (currentTime >= nextStart) {
                    if (nextStart < completedTime && completedTime < nextEnd) {
                        completedEnd = nextEnd;
                    }
                    nextStart += repeat;
                    nextEnd = nextStart + duration;
                    mostRecent.put(nextStart, nextEnd);
                }

                // Check whether the quest is currently active
                boolean active = false;
                for (final Entry<Long, Long> startEnd : mostRecent.entrySet()) {
                    if (startEnd.getKey() <= currentTime && currentTime < startEnd.getValue()) {
                        active = true;
                        break;
                    }
                }

                // If quest is not active, or new period of activity should override player cooldown
                if (!active || (quest.getPlanner().getOverride() && completedEnd > 0L && currentTime < completedEnd)) {
                    if (giveReason) {
                        final String early = BukkitLang.get("plnTooEarly").replace("<quest>", quest.getName())
                                .replace("<time>", BukkitMiscUtil.getTime((completedEnd) - currentTime));
                        sendMessage(ChatColor.YELLOW + early);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Start a quest for this Quester
     *
     * @param quest The quest to start
     * @param ignoreRequirements Whether to ignore Requirements
     */
    public void takeQuest(final Quest quest, final boolean ignoreRequirements) {
        if (quest == null) {
            return;
        }
        final BukkitQuest bukkitQuest = (BukkitQuest) quest;
        final BukkitQuesterPreStartQuestEvent preEvent = new BukkitQuesterPreStartQuestEvent(this, bukkitQuest);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        final OfflinePlayer offlinePlayer = getOfflinePlayer();
        if (offlinePlayer.isOnline()) {
            if (!isOnTime(bukkitQuest, true)) {
                return;
            }
        }
        if (bukkitQuest.testRequirements(offlinePlayer) || ignoreRequirements) {
            addEmptiesFor(bukkitQuest, 0);
            try {
                currentQuests.put(bukkitQuest, 0);
                if (plugin.getConfigSettings().getConsoleLogging() > 1) {
                    plugin.getLogger().info(getPlayer().getUniqueId() + " started quest " + bukkitQuest.getName());
                }
            } catch (final NullPointerException npe) {
                plugin.getLogger().severe("Unable to add quest" + bukkitQuest.getName() + " for player "
                        + offlinePlayer.getName() + ". Consider resetting player data or report on Github");
            }
            final Stage stage = bukkitQuest.getStage(0);
            if (!ignoreRequirements) {
                final BukkitRequirements requirements = (BukkitRequirements) bukkitQuest.getRequirements();
                if (requirements.getMoney() > 0) {
                    if (plugin.getDependencies().getVaultEconomy() != null) {
                        plugin.getDependencies().getVaultEconomy().withdrawPlayer(getOfflinePlayer(),
                                requirements.getMoney());
                    }
                }
                if (offlinePlayer.isOnline()) {
                    final Player p = getPlayer();
                    if (p != null) {
                        final ItemStack[] original = p.getInventory().getContents().clone();
                        for (final ItemStack is : requirements.getItems()) {
                            if (requirements.getRemoveItems().get(requirements.getItems().indexOf(is))) {
                                if (!BukkitInventoryUtil.removeItem(p.getInventory(), is)) {
                                    if (p.getEquipment() != null && !BukkitInventoryUtil.stripItem(p.getEquipment(),
                                            is)) {
                                        p.getInventory().setContents(original);
                                        p.updateInventory();
                                        sendMessage(BukkitLang.get(p, "requirementsItemFail"));
                                        hardQuit(bukkitQuest);
                                        return;
                                    }
                                }
                            }
                        }
                        String accepted = BukkitLang.get(p, "questAccepted");
                        accepted = accepted.replace("<quest>", bukkitQuest.getName());
                        sendMessage(ChatColor.GREEN + accepted);
                        p.sendMessage("");
                        if (plugin.getConfigSettings().canShowQuestTitles()) {
                            final String title = ChatColor.GOLD + BukkitLang.get(p, "quest") + " "
                                    + BukkitLang.get(p, "accepted");
                            final String subtitle = ChatColor.YELLOW + bukkitQuest.getName();
                            BukkitTitleProvider.sendTitle(p, title, subtitle);
                        }
                    }
                }
            }
            if (offlinePlayer.isOnline()) {
                final Player p = getPlayer();
                final String title = BukkitLang.get(p, "objectives").replace("<quest>", bukkitQuest.getName());
                BukkitLang.send(p, ChatColor.GOLD + title);
                showCurrentObjectives(bukkitQuest, this, false);
                final String stageStartMessage = stage.getStartMessage();
                if (stageStartMessage != null) {
                    p.sendMessage(BukkitConfigUtil.parseStringWithPossibleLineBreaks(stageStartMessage, bukkitQuest,
                            getPlayer()));
                }
                showCurrentConditions(bukkitQuest, this);
            }
            if (bukkitQuest.getInitialAction() != null) {
                bukkitQuest.getInitialAction().fire(this, bukkitQuest);
            }
            if (stage.getStartAction() != null) {
                stage.getStartAction().fire(this, bukkitQuest);
            }
            saveData();
            setCompassTarget(bukkitQuest);
            bukkitQuest.updateCompass(this, stage);
        } else {
            if (offlinePlayer.isOnline()) {
                sendMessage(ChatColor.DARK_AQUA + BukkitLang.get("requirements"));
                for (final String s : getCurrentRequirements(bukkitQuest, false)) {
                    sendMessage("- " + ChatColor.GRAY + s);
                }
            }
        }
        if (offlinePlayer.isOnline()) {
            final BukkitQuesterPostStartQuestEvent postEvent = new BukkitQuesterPostStartQuestEvent(this, bukkitQuest);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * End a quest for this Quester, but ask permission first if possible<p>
     *
     * @param quest The quest to check and then offer
     * @param message Messages to send Quester upon quit, can be left null or empty
     * @return true if successful
     */
    public boolean abandonQuest(final Quest quest, final String message) {
        return abandonQuest(quest, new String[] {message});
    }

    /**
     * End a quest for this Quester, but ask permission first if possible<p>
     *
     * @param quest The quest to check and then offer
     * @param messages Messages to send Quester upon quit, can be left null or empty
     * @return true if successful
     */
    public boolean abandonQuest(final Quest quest, final String[] messages) {
        if (quest == null) {
            return false;
        }
        final QuestQuitEvent event = new QuestQuitEvent(quest, this);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        final ConfigSettings settings = plugin.getConfigSettings();
        if (getPlayer() != null) {
            setQuestIdToQuit(quest.getId());
            if (settings.canConfirmAbandon()) {
                final ConversationFactory cf = new ConversationFactory(plugin).withModality(false)
                        .withPrefix(context -> ChatColor.GRAY.toString())
                        .withFirstPrompt(new QuestAbandonPrompt(plugin)).withTimeout(settings.getAcceptTimeout())
                        .thatExcludesNonPlayersWithMessage("Console may not perform this conversation!")
                        .addConversationAbandonedListener(plugin.getConvoListener());
                cf.buildConversation(getPlayer()).begin();
            } else {
                quitQuest(quest, messages);
            }
            return true;
        }
        return false;
    }

    /**
     * End a quest for this Quester
     *
     * @param quest The quest to start
     * @param message Message to inform player, can be left null or empty
     * @since 3.8.6
     */
    public void quitQuest(final Quest quest, final String message) {
        quitQuest(quest, new String[] {message});
    }

    /**
     * End a quest for this Quester
     *
     * @param quest The quest to start
     * @param messages Messages to inform player, can be left null or empty
     * @since 3.8.6
     */
    public void quitQuest(final Quest quest, final String[] messages) {
        if (quest == null) {
            return;
        }
        hardQuit(quest);
        if (plugin.getConfigSettings().getConsoleLogging() > 1) {
            plugin.getLogger().info(getOfflinePlayer().getUniqueId() + " quit quest " + quest.getName());
        }
        for (final String message : messages) {
            if (getOfflinePlayer().isOnline()) {
                BukkitLang.send(getPlayer(), message);
            }
        }
        saveData();
        updateJournal();
        if (compassTargetQuestId != null && compassTargetQuestId.equals(quest.getId())) {
            compassTargetQuestId = null;
        }
    }

    /**
     * Show the player a list of their available quests
     *
     * @param quester Quester to show the list
     * @param page Page to display, with 7 quests per page
     */
    public void listQuests(final Quester quester, final int page) {
        // Although we could copy the quests list to a new object, we instead opt to
        // duplicate code to improve efficiency if ignore-locked-quests is set to 'false'
        final int rows = 7;
        final Player player = quester.getPlayer();
        final Collection<Quest> quests = plugin.getLoadedQuests();
        if (plugin.getConfigSettings().canIgnoreLockedQuests()) {
            final LinkedList<Quest> available = new LinkedList<>();
            for (final Quest q : quests) {
                if (!quester.getCompletedQuests().contains(q)) {
                    if (q.testRequirements(quester)) {
                        available.add(q);
                    }
                } else if (q.getPlanner().hasCooldown() && quester.getRemainingCooldown(q) < 0) {
                    if (q.testRequirements(quester)) {
                        available.add(q);
                    }
                }
            }
            if ((available.size() + rows) <= (page * rows) || available.isEmpty()) {
                BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "pageNotExist"));
            } else {
                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questListTitle"));
                int fromOrder = (page - 1) * rows;
                final List<Quest> subQuests;
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
                String msg = BukkitLang.get(player, "pageFooter");
                msg = msg.replace("<current>", String.valueOf(page));
                msg = msg.replace("<all>", String.valueOf(numPages));
                BukkitLang.send(player, ChatColor.GOLD + msg);
            }
        } else {
            if ((quests.size() + rows) <= (page * rows) || quests.isEmpty()) {
                BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get(player, "pageNotExist"));
            } else {
                BukkitLang.send(player, ChatColor.GOLD + BukkitLang.get(player, "questListTitle"));
                int fromOrder = (page - 1) * rows;
                final List<Quest> subQuests;
                if (quests.size() >= (fromOrder + rows)) {
                    subQuests = new LinkedList<>(quests).subList((fromOrder), (fromOrder + rows));
                } else {
                    subQuests = new LinkedList<>(quests).subList((fromOrder), quests.size());
                }
                fromOrder++;
                for (final Quest q : subQuests) {
                    if (quester.canAcceptOffer(q, false)) {
                        BukkitLang.send(player, ChatColor.YELLOW + Integer.toString(fromOrder) + ". " + q.getName());
                    } else {
                        BukkitLang.send(player, ChatColor.GRAY + Integer.toString(fromOrder) + ". " + q.getName());
                    }
                    fromOrder++;
                }
                final int numPages = (int) Math.ceil(((double) quests.size()) / ((double) rows));
                String msg = BukkitLang.get(player, "pageFooter");
                msg = msg.replace("<current>", String.valueOf(page));
                msg = msg.replace("<all>", String.valueOf(numPages));
                BukkitLang.send(player, ChatColor.GOLD + msg);
            }
        }
    }

    /**
     * Get current requirements for a quest, both finished and unfinished
     *
     * @param quest The quest to get objectives of
     * @param ignoreOverrides Whether to ignore objective-overrides
     * @return List of detailed requirements
     */
    public LinkedList<String> getCurrentRequirements(final Quest quest, final boolean ignoreOverrides) {
        if (quest == null) {
            return new LinkedList<>();
        }
        final BukkitRequirements requirements = (BukkitRequirements) quest.getRequirements();
        if (!ignoreOverrides) {
            if (requirements.getDetailsOverride() != null && !requirements.getDetailsOverride().isEmpty()) {
                final LinkedList<String> current = new LinkedList<>();
                for (final String s : requirements.getDetailsOverride()) {
                    String message = ChatColor.RED + BukkitConfigUtil.parseString(
                            ChatColor.translateAlternateColorCodes('&', s), quest, getPlayer());
                    if (plugin.getDependencies().getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    current.add(message);

                }
                return current;
            }
        }
        final LinkedList<String> unfinishedRequirements = new LinkedList<>();
        final LinkedList<String> finishedRequirements = new LinkedList<>();
        final LinkedList<String> current = new LinkedList<>();
        final OfflinePlayer player = getPlayer();
        if (requirements.getMoney() > 0 && plugin.getDependencies().getVaultEconomy() != null) {
            final String currency = requirements.getMoney() > 1 ? plugin.getDependencies().getVaultEconomy()
                    .currencyNamePlural() : plugin.getDependencies().getVaultEconomy().currencyNameSingular();
            if (plugin.getDependencies().getVaultEconomy().getBalance(player) >= requirements.getMoney()) {
                unfinishedRequirements.add(ChatColor.GREEN + "" + requirements.getMoney() + " " + currency);
            } else {
                finishedRequirements.add(ChatColor.GRAY + "" + requirements.getMoney() + " " + currency);
            }
        }
        if (requirements.getQuestPoints() > 0) {
            if (getQuestPoints() >= requirements.getQuestPoints()) {
                unfinishedRequirements.add(ChatColor.GREEN + "" + requirements.getQuestPoints() + " "
                        + BukkitLang.get("questPoints"));
            } else {
                finishedRequirements.add(ChatColor.GRAY + "" + requirements.getQuestPoints() + " "
                        + BukkitLang.get("questPoints"));
            }
        }
        final Map<String, String> completed = completedQuests.stream()
                .collect(Collectors.toMap(Quest::getId, Quest::getName));
        for (final String questId : requirements.getNeededQuestIds()) {
            if (completed.containsKey(questId)) {
                String msg = BukkitLang.get("haveCompleted");
                msg = msg.replace("<quest>", completed.get(questId));
                finishedRequirements.add(ChatColor.GREEN + msg);
            } else {
                String msg = BukkitLang.get("mustComplete");
                msg = msg.replace("<quest>", plugin.getQuestById(questId).getName());
                unfinishedRequirements.add(ChatColor.GRAY + msg);
            }
        }
        for (final String questId : requirements.getBlockQuestIds()) {
            if (completed.containsKey(questId)) {
                String msg = BukkitLang.get("cannotComplete");
                msg = msg.replace("<quest>", quest.getName());
                current.add(ChatColor.RED + msg);
            }
        }
        for (final Quest q : currentQuests.keySet()) {
            if (q != null) {
                if (requirements.getBlockQuestIds().contains(q.getId())) {
                    current.add(ChatColor.RED + quest.getName());
                }
            }
        }
        for (final String s : requirements.getMcmmoSkills()) {
            final SkillType st = plugin.getDependencies().getMcMMOSkill(s);
            final int lvl = requirements.getMcmmoAmounts().get(requirements.getMcmmoSkills().indexOf(s));
            if (UserManager.getOfflinePlayer(player).getProfile().getSkillLevel(st) >= lvl) {
                finishedRequirements.add(ChatColor.GREEN + "" + lvl + " " + s);
            } else {
                unfinishedRequirements.add(ChatColor.GRAY + "" + lvl + " " + s);
            }
        }
        if (requirements.getHeroesPrimaryClass() != null) {
            if (plugin.getDependencies()
                    .testPrimaryHeroesClass(requirements.getHeroesPrimaryClass(), player.getUniqueId())) {
                finishedRequirements.add(ChatColor.GREEN + BukkitLang.get("reqHeroesPrimaryDisplay") + " "
                    + requirements.getHeroesPrimaryClass());
            } else {
                unfinishedRequirements.add(ChatColor.GRAY + BukkitLang.get("reqHeroesPrimaryDisplay") + " "
                        + requirements.getHeroesPrimaryClass());
            }
        }
        if (requirements.getHeroesSecondaryClass() != null) {
            if (plugin.getDependencies()
                    .testSecondaryHeroesClass(requirements.getHeroesSecondaryClass(), player.getUniqueId())) {
                finishedRequirements.add(ChatColor.GREEN + BukkitLang.get("reqHeroesSecondaryDisplay") + " "
                        + requirements.getHeroesSecondaryClass());
            } else {
                finishedRequirements.add(ChatColor.GRAY + BukkitLang.get("reqHeroesSecondaryDisplay") + " "
                        + requirements.getHeroesSecondaryClass());
            }
        }
        if (player.isOnline()) {
            final Inventory fakeInv = Bukkit.createInventory(null, InventoryType.PLAYER);
            fakeInv.setContents(getPlayer().getInventory().getContents().clone());

            int num = 0;
            for (final ItemStack is : requirements.getItems()) {
                if (BukkitInventoryUtil.canRemoveItem(fakeInv, is)) {
                    BukkitInventoryUtil.removeItem(fakeInv, is);
                    num += is.getAmount();
                }
                if (num >= is.getAmount()) {
                    finishedRequirements.add(ChatColor.GREEN + "" + is.getAmount() + " " + BukkitItemUtil.getName(is));
                } else {
                    unfinishedRequirements.add(ChatColor.GRAY + "" + is.getAmount() + " " + BukkitItemUtil.getName(is));
                }
                num = 0;
            }

            for (final String perm :requirements.getPermissions()) {
                if (getPlayer().hasPermission(perm)) {
                    finishedRequirements.add(ChatColor.GREEN + BukkitLang.get("permissionDisplay") + " " + perm);
                } else {
                    unfinishedRequirements.add(ChatColor.GRAY + BukkitLang.get("permissionDisplay") + " " + perm);
                }

            }
            for (final Entry<String, Map<String, Object>> m : requirements.getCustomRequirements().entrySet()) {
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getName().equalsIgnoreCase(m.getKey())) {
                        String message = cr.getDisplay() != null ? cr.getDisplay() : m.getKey();
                        for (Entry<String, Object> prompt : cr.getData().entrySet()) {
                            final String replacement = "%" + prompt.getKey() + "%";
                            try {
                                if (message.contains(replacement)) {
                                    message = message.replace(replacement, String.valueOf(m.getValue()
                                            .get(prompt.getKey())));
                                }
                            } catch (final NullPointerException ne) {
                                plugin.getLogger().severe("Unable to gather display for " + cr.getName() + " on "
                                        + quest.getName());
                                ne.printStackTrace();
                            }
                        }
                        if (cr.testRequirement(getPlayer().getUniqueId(), m.getValue())) {
                            finishedRequirements.add(ChatColor.GREEN + "" + message);
                        } else {
                            unfinishedRequirements.add(ChatColor.GRAY + "" + message);
                        }
                    }
                }
            }
        }
        current.addAll(unfinishedRequirements);
        current.addAll(finishedRequirements);
        return current;
    }

    /**
     * Get current objectives for a quest, both finished and unfinished
     *
     * @param quest The quest to get objectives of
     * @param ignoreOverrides Whether to ignore objective-overrides
     * @param formatNames Whether to format item/entity names, if applicable
     * @return List of detailed objectives
     */
    public LinkedList<Objective> getCurrentObjectives(final Quest quest, final boolean ignoreOverrides,
                                                            final boolean formatNames) {
        if (quest == null) {
            plugin.getLogger().severe("Quest was null when getting objectives for " + getLastKnownName());
            return new LinkedList<>();
        }
        if (getQuestProgressOrDefault(quest) == null) {
            plugin.getLogger().warning("Quest data was null when getting objectives for " + quest.getName());
            return new LinkedList<>();
        }
        final BukkitStage stage = (BukkitStage) getCurrentStage(quest);
        if (stage == null) {
            //plugin.getLogger().warning("Current stage was null when getting objectives for " + quest.getName());
            return new LinkedList<>();
        }
        final BukkitDependencies depends = plugin.getDependencies();
        if (!ignoreOverrides && !stage.getObjectiveOverrides().isEmpty()) {
            final LinkedList<Objective> objectives = new LinkedList<>();
            for (final String s: stage.getObjectiveOverrides()) {
                String message = ChatColor.GREEN + BukkitConfigUtil.parseString(s, quest, getPlayer());
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                }
                // TODO is this acceptable?
                objectives.add(new BukkitObjective(ObjectiveType.CUSTOM, message, 0, 1));
            }
            return objectives;
        }
        final BukkitQuestProgress data = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final LinkedList<Objective> objectives = new LinkedList<>();
        for (int i = 0; i < data.getBlocksBroken().size(); i++) {
            final int progress = data.getBlocksBroken().get(i);
            if (i >= stage.getBlocksToBreak().size()) { break; }
            final BlockItemStack goal = stage.getBlocksToBreak().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "break"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.BREAK_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getBlocksDamaged().size(); i++) {
            final int progress = data.getBlocksDamaged().get(i);
            if (i >= stage.getBlocksToDamage().size()) { break; }
            final BlockItemStack goal = stage.getBlocksToDamage().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "damage"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.DAMAGE_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getBlocksPlaced().size(); i++) {
            final int progress = data.getBlocksPlaced().get(i);
            if (i >= stage.getBlocksToPlace().size()) { break; }
            final BlockItemStack goal = stage.getBlocksToPlace().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "place"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.PLACE_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getBlocksUsed().size(); i++) {
            final int progress = data.getBlocksUsed().get(i);
            if (i >= stage.getBlocksToUse().size()) { break; }
            final BlockItemStack goal = stage.getBlocksToUse().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "use"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.USE_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getBlocksCut().size(); i++) {
            final int progress = data.getBlocksCut().get(i);
            if (i >= stage.getBlocksToCut().size()) { break; }
            final BlockItemStack goal = stage.getBlocksToCut().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "cut"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.CUT_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsCrafted().size(); i++) {
            final int progress = data.getItemsCrafted().get(i);
            if (i >= stage.getItemsToCraft().size()) { break; }
            final ItemStack goal = stage.getItemsToCraft().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "craftItem"),
                    progress, goal.getAmount());
            if (goal.getType().name().equals("TIPPED_ARROW")) {
                final String level = BukkitItemUtil.getPrettyPotionLevel(goal.getItemMeta());
                if (!level.isEmpty()) {
                    message = message.replace("<item>", "<item> " + level);
                }
            }
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.CRAFT_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsSmelted().size(); i++) {
            final int progress = data.getItemsSmelted().get(i);
            if (i >= stage.getItemsToSmelt().size()) { break; }
            final ItemStack goal = stage.getItemsToSmelt().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "smeltItem"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.SMELT_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsEnchanted().size(); i++) {
            final int progress = data.getItemsEnchanted().get(i);
            if (i >= stage.getItemsToEnchant().size()) { break; }
            final ItemStack goal = stage.getItemsToEnchant().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "enchItem"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            if (goal.getEnchantments().isEmpty()) {
                objectives.add(new BukkitObjective(ObjectiveType.ENCHANT_ITEM,
                        message.replace("<enchantment>", "")
                                .replace("<level>", "")
                                .replaceAll("\\s+", " "), progress, goal));
            } else {
                for (final Entry<Enchantment, Integer> e : goal.getEnchantments().entrySet()) {
                    objectives.add(new BukkitObjective(ObjectiveType.ENCHANT_ITEM,
                            message.replace("<enchantment>", BukkitItemUtil.getPrettyEnchantmentName(e.getKey()))
                                    .replace("<level>", RomanNumeral.getNumeral(e.getValue())), progress, goal));
                }
            }
        }
        for (int i = 0; i < data.getItemsBrewed().size(); i++) {
            final int progress = data.getItemsBrewed().get(i);
            if (i >= stage.getItemsToBrew().size()) { break; }
            final ItemStack goal = stage.getItemsToBrew().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "brewItem"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            final String level = BukkitItemUtil.getPrettyPotionLevel(goal.getItemMeta());
            if (level.isEmpty()) {
                message = message.replace(" <level>", level);
            } else {
                message = message.replace("<level>", level);
            }
            objectives.add(new BukkitObjective(ObjectiveType.BREW_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsConsumed().size(); i++) {
            final int progress = data.getItemsConsumed().get(i);
            if (i >= stage.getItemsToConsume().size()) { break; }
            final ItemStack goal = stage.getItemsToConsume().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "consumeItem"),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            final String level = BukkitItemUtil.getPrettyPotionLevel(goal.getItemMeta());
            if (level.isEmpty()) {
                message = message.replace(" <level>", level);
            } else {
                message = message.replace("<level>", level);
            }
            objectives.add(new BukkitObjective(ObjectiveType.CONSUME_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsDelivered().size(); i++) {
            final int progress = data.getItemsDelivered().get(i);
            if (i >= stage.getItemsToDeliver().size()) { break; }
            final ItemStack goal = stage.getItemsToDeliver().get(i);
            final UUID npc = stage.getItemDeliveryTargets().get(i);
            final ChatColor color = progress < goal.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color,
                    BukkitLang.get(getPlayer(), "deliver").replace("<npc>", depends.getNpcName(npc)),
                    progress, goal.getAmount());
            if (formatNames) {
                message = message.replace("<item>", BukkitItemUtil.getName(goal));
            }
            objectives.add(new BukkitObjective(ObjectiveType.DELIVER_ITEM, message, progress, goal));
        }
        int interactIndex = 0;
        for (final UUID n : stage.getNpcsToInteract()) {
            if (data.getNpcsInteracted().size() > interactIndex) {
                final boolean progress = data.getNpcsInteracted().get(interactIndex);
                final ChatColor color = !progress ? ChatColor.GREEN : ChatColor.GRAY;
                String message = color + BukkitLang.get(getPlayer(), "talkTo")
                        .replace("<npc>", depends.getNpcName(n));
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                }
                objectives.add(new BukkitObjective(ObjectiveType.TALK_TO_NPC, message, progress ? 1 : 0, 1));
            }
            interactIndex++;
        }
        int npcKillIndex = 0;
        for (final UUID n : stage.getNpcsToKill()) {
            int progress = 0;
            if (data.getNpcsNumKilled().size() > npcKillIndex) {
                progress = data.getNpcsNumKilled().get(npcKillIndex);
            }
            final int goal = stage.getNpcNumToKill().get(npcKillIndex);
            final ChatColor color = progress < goal ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "kill"),
                    progress, goal);
            if (message.contains("<mob>")) {
                message = message.replace("<mob>", depends.getNpcName(n));
            } else {
                message += " " + depends.getNpcName(n);
            }
            objectives.add(new BukkitObjective(ObjectiveType.KILL_NPC, message, progress, goal));
            npcKillIndex++;
        }
        int mobKillIndex = 0;
        for (final EntityType e : stage.getMobsToKill()) {
            int progress = 0;
            if (data.getMobNumKilled().size() > mobKillIndex) {
                progress = data.getMobNumKilled().get(mobKillIndex);
            }
            final int goal = stage.getMobNumToKill().get(mobKillIndex);
            final ChatColor color = progress < goal ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + "";
            if (stage.getLocationsToKillWithin().isEmpty()) {
                message += BukkitLang.get(getPlayer(), "kill");
                if (message.contains("<count>")) {
                    message = message.replace("<count>", "" + color + progress + "/" + goal);
                }
            } else {
                message += BukkitLang.get(getPlayer(), "killAtLocation").replace("<location>",
                        stage.getKillNames().get(stage.getMobsToKill().indexOf(e)));
                if (message.contains("<count>")) {
                    message = message.replace("<count>", "" + color + progress + "/" + goal);
                } else {
                    message += color + ": " + progress + "/" + goal;
                }
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            if (formatNames) {
                message = message.replace("<mob>", BukkitMiscUtil.snakeCaseToUpperCamelCase(e.name()));
            }
            objectives.add(new BukkitObjective(ObjectiveType.KILL_MOB, message,
                    new BukkitCountableMob(e, progress), new BukkitCountableMob(e, goal)));
            mobKillIndex++;
        }
        int tameIndex = 0;
        for (final EntityType e : stage.getMobsToTame()) {
            int progress = 0;
            if (data.getMobsTamed().size() > tameIndex) {
                progress = data.getMobsTamed().get(tameIndex);
            }
            final int goal = stage.getMobNumToTame().get(tameIndex);
            final ChatColor color = progress < goal ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "tame"),
                    progress, goal);
            if (!message.contains("<mob>")) {
                message += " <mob>";
            }
            if (formatNames) {
                message = message.replace("<mob>", BukkitMiscUtil.snakeCaseToUpperCamelCase(stage.getMobsToTame()
                        .get(tameIndex).name()));
            }
            objectives.add(new BukkitObjective(ObjectiveType.TAME_MOB, message,
                    new BukkitCountableMob(e, progress), new BukkitCountableMob(e, goal)));
            tameIndex++;
        }
        if (stage.getFishToCatch() != null) {
            final int progress = data.getFishCaught();
            final int goal = stage.getFishToCatch();
            final ChatColor color = progress < goal ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "catchFish"),
                    progress, goal);
            objectives.add(new BukkitObjective(ObjectiveType.CATCH_FISH, message, progress, goal));
        }
        if (stage.getCowsToMilk() != null) {
            final int progress = data.getCowsMilked();
            final int goal = stage.getCowsToMilk();
            final ChatColor color = progress < goal ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "milkCow"),
                    progress, goal);
            objectives.add(new BukkitObjective(ObjectiveType.MILK_COW, message, progress, goal));
        }
        int shearIndex = 0;
        for (final int goal : stage.getSheepNumToShear()) {
            int progress = 0;
            if (data.getSheepSheared().size() > shearIndex) {
                progress = data.getSheepSheared().get(shearIndex);
            }
            final ChatColor color = progress < goal ? ChatColor.GREEN : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "shearSheep"),
                    progress, goal);
            message = message.replace("<color>", BukkitMiscUtil.getPrettyDyeColorName(stage.getSheepToShear()
                    .get(shearIndex)));
            objectives.add(new BukkitObjective(ObjectiveType.SHEAR_SHEEP, message, progress, goal));
            shearIndex++;
        }
        if (stage.getPlayersToKill() != null) {
            final int progress = data.getPlayersKilled();
            final int goal = stage.getPlayersToKill();
            final ChatColor color = data.getPlayersKilled() < stage.getPlayersToKill() ? ChatColor.GREEN
                    : ChatColor.GRAY;
            String message = formatCurrentObjectiveMessage(color, BukkitLang.get(getPlayer(), "killPlayer"),
                    progress, goal);
            objectives.add(new BukkitObjective(ObjectiveType.KILL_PLAYER, message, progress, goal));
        }
        for (int i = 0 ; i < stage.getLocationsToReach().size(); i++) {
            if (i < data.getLocationsReached().size()) {
                final boolean progress = data.getLocationsReached().get(i);
                final ChatColor color = !progress ? ChatColor.GREEN : ChatColor.GRAY;
                String message = color + BukkitLang.get(getPlayer(), "goTo");
                message = message.replace("<location>", stage.getLocationNames().get(i));
                objectives.add(new BukkitObjective(ObjectiveType.REACH_LOCATION, message, progress ? 1 : 0, 1));
            }
        }
        int passIndex = 0;
        for (final String s : stage.getPasswordDisplays()) {
            boolean progress = false;
            if (data.getPasswordsSaid().size() > passIndex) {
                progress = data.getPasswordsSaid().get(passIndex);
            }
            final ChatColor color = !progress ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + s;
            objectives.add(new BukkitObjective(ObjectiveType.PASSWORD, message, progress ? 1 : 0, 1));
            passIndex++;
        }
        int customIndex = 0;
        for (final CustomObjective co : stage.getCustomObjectives()) {
            int progress = 0;
            if (data.getCustomObjectiveCounts().size() > customIndex) {
                progress = data.getCustomObjectiveCounts().get(customIndex);
            }
            final int goal = stage.getCustomObjectiveCounts().get(customIndex);
            final ChatColor color = progress < goal ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + co.getDisplay();
            for (final Entry<String,Object> prompt : co.getData()) {
                final String replacement = "%" + prompt.getKey() + "%";
                try {
                    for (final Entry<String, Object> e : stage.getCustomObjectiveData()) {
                        if (e.getKey().equals(prompt.getKey())) {
                            if (message.contains(replacement)) {
                                message = message.replace(replacement, String.valueOf(e.getValue()));
                            }
                        }
                    }
                } catch (final NullPointerException ne) {
                    plugin.getLogger().severe("Unable to gather display for " + co.getName() + " on "
                            + quest.getName());
                    ne.printStackTrace();
                }
            }
            if (co.canShowCount()) {
                message = message.replace("%count%", progress + "/" + goal);
            }
            message = BukkitConfigUtil.parseString(message.trim().replaceAll("\\s{2,}", ""));
            objectives.add(new BukkitObjective(ObjectiveType.CUSTOM, message, progress, goal));
            customIndex++;
        }
        return objectives;
    }

    /**
     * Format current objective message with count and placeholders
     *
     * @param color Color for text
     * @param message Text to format
     * @param progress Objective progress
     * @param goal Objective goal
     * @return Formatted message
     */
    private String formatCurrentObjectiveMessage(ChatColor color, String message, int progress, int goal) {
        message = color + message;
        if (message.contains("<count>")) {
            message = message.replace("<count>", "" + color + progress + "/" + goal);
        }
        if (plugin.getDependencies().getPlaceholderApi() != null) {
            message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
        }
        return message;
    }

    /**
     * Show current objectives for a quest, if applicable<p>
     *
     * Respects PlaceholderAPI and translations, when enabled.
     *
     * @param quest The quest to get current stage objectives of
     * @param quester The player to show current stage objectives to
     * @param ignoreOverrides Whether to ignore objective-overrides
     */
    public void showCurrentObjectives(final Quest quest, final Quester quester, final boolean ignoreOverrides) {
        if (quest == null) {
            plugin.getLogger().severe("Quest was null when showing objectives for " + quester.getLastKnownName());
            return;
        }
        final BukkitQuester q = (BukkitQuester)quester;
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null) {
            plugin.getLogger().warning("Current stage was null when showing objectives for " + quest.getName());
            return;
        }
        if (!ignoreOverrides && !stage.getObjectiveOverrides().isEmpty()) {
            for (final String s: stage.getObjectiveOverrides()) {
                String message = (!s.trim().isEmpty() ? "- " : "") + ChatColor.GREEN + BukkitConfigUtil
                        .parseString(s, quest, quester.getPlayer());
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(quester.getPlayer(), message);
                }
                quester.sendMessage(message);
            }
            return;
        }
        final LocaleManager localeManager = plugin.getLocaleManager();
        final BukkitConfigSettings settings = plugin.getConfigSettings();
        if (localeManager == null && settings.canTranslateNames()) {
            settings.setTranslateNames(false);
            plugin.getLogger().severe("Problem with locale manager! Item name translation disabled.");
        }
        for (final Objective obj : q.getCurrentObjectives(quest, false, false)) {
            final BukkitObjective objective = (BukkitObjective) obj;
            String message = "- " + BukkitLang.BukkitFormatToken.convertString(quester.getPlayer(),
                    objective.getMessage());
            if (objective.getGoalAsBlockItem() != null) {
                final int progress = objective.getProgress();
                final BlockItemStack goal = objective.getGoalAsBlockItem();
                if (!settings.canShowCompletedObjs() && progress >= goal.getAmount()) {
                    continue;
                }
                if (localeManager != null && settings.canTranslateNames()) {
                    localeManager.sendMessage(quester.getPlayer(), message, goal.getType(), goal.getDurability(), null);
                }
            } else if (objective.getGoalAsItem() != null) {
                final int progress = objective.getProgress();
                final ItemStack goal = objective.getGoalAsItem();
                if (!settings.canShowCompletedObjs() && progress >= goal.getAmount()) {
                    continue;
                }
                if (localeManager != null && settings.canTranslateNames() && !goal.hasItemMeta()
                        && Material.getMaterial("LINGERING_POTION") == null) {
                    // Bukkit version is below 1.9 and item has no metadata
                    localeManager.sendMessage(quester.getPlayer(), message, goal);
                } else if (localeManager != null && settings.canTranslateNames() && goal.getItemMeta() != null
                        && !goal.getItemMeta().hasDisplayName() && !goal.getType().equals(Material.WRITTEN_BOOK)) {
                    // Bukkit version is 1.9+ and item lacks custom name
                    if (goal.getType().name().contains("POTION") && localeManager.hasBasePotionData()) {
                        final String level = BukkitItemUtil.getPrettyPotionLevel(goal.getItemMeta());
                        if (level.isEmpty()) {
                            message = message.replace(" <level>", level);
                        } else {
                            message = message.replace("<level>", level);
                        }
                    }
                    localeManager.sendMessage(quester.getPlayer(), message, goal);
                } else {
                    if (goal.getEnchantments().isEmpty()) {
                        quester.sendMessage(message.replace("<item>", BukkitItemUtil.getName(goal))
                                .replace("<enchantment>", "")
                                .replace("<level>", "")
                                .replaceAll("\\s+", " "));
                    } else {
                        for (final Entry<Enchantment, Integer> e : goal.getEnchantments().entrySet()) {
                            quester.sendMessage(message.replace("<item>", BukkitItemUtil.getName(goal))
                                    .replace("<enchantment>", BukkitItemUtil.getPrettyEnchantmentName(e.getKey()))
                                    .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                        }
                    }
                }
            } else if (objective.getProgressAsMob() != null && objective.getGoalAsMob() != null) {
                final BukkitCountableMob progress = objective.getProgressAsMob();
                final BukkitCountableMob goal = objective.getGoalAsMob();
                if (!settings.canShowCompletedObjs() && progress.getCount() >= goal.getCount()) {
                    continue;
                }
                if (localeManager != null && settings.canTranslateNames()) {
                    localeManager.sendMessage(quester.getPlayer(), message, goal.getEntityType(), null);
                } else {
                    quester.sendMessage(message.replace("<mob>",
                            BukkitMiscUtil.snakeCaseToUpperCamelCase(goal.getEntityType().name())));
                }
            } else {
                if (!settings.canShowCompletedObjs() && objective.getProgress() >= objective.getGoal()) {
                    continue;
                }
                if (obj.getType().equals(ObjectiveType.CUSTOM)) {
                    quester.sendMessage(message.trim().replaceAll("\\s{2,}", ""));
                } else {
                    quester.sendMessage(message);
                }
            }
        }
    }

    /**
     * Check if player's current stage has the specified objective
     *
     * @param quest The quest to check objectives of
     * @param type The type of objective to check for
     * @return true if stage contains specified objective
     */
    public boolean hasObjective(final Quest quest, final ObjectiveType type) {
        if (quest == null || getCurrentStage(quest) == null || type == null) {
            return false;
        }
        return getCurrentStage(quest).containsObjective(type);
    }

    /**
     * Check if player's current stage has the specified custom objective
     *
     * @param quest The quest to check custom objectives of
     * @param name The exact name of custom objective to check for
     * @return true if stage contains specified objective
     */
    public boolean hasCustomObjective(final Quest quest, final String name) {
        if (quest == null || getCurrentStage(quest) == null || name == null) {
            return false;
        }
        for (final CustomObjective co : getCurrentStage(quest).getCustomObjectives()) {
            if (co.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show all of a player's conditions for the current stage of a quest.<p>
     *
     * @param quest The quest to get current stage objectives of
     * @param quester The player to show current stage objectives to
     */
    public void showCurrentConditions(final Quest quest, final Quester quester) {
        if (quest == null) {
            plugin.getLogger().severe("Quest was null when getting conditions for " + quester.getLastKnownName());
            return;
        }
        if (quester.getQuestProgressOrDefault(quest) == null) {
            plugin.getLogger().warning("Quest data was null when showing conditions for " + quest.getName());
            return;
        }
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null) {
            plugin.getLogger().warning("Current stage was null when showing conditions for " + quest.getName());
            return;
        }
        final BukkitCondition c = (BukkitCondition) stage.getCondition();
        if (c != null && stage.getObjectiveOverrides().isEmpty()) {
            quester.sendMessage(ChatColor.LIGHT_PURPLE + BukkitLang.get("stageEditorConditions"));
            final StringBuilder msg = new StringBuilder("- " + ChatColor.YELLOW);
            if (!c.getEntitiesWhileRiding().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorRideEntity"));
                for (final String e : c.getEntitiesWhileRiding()) {
                    msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(e);
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getNpcsWhileRiding().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorRideNPC"));
                for (final UUID u : c.getNpcsWhileRiding()) {
                    if (plugin.getDependencies().getCitizens() != null) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(CitizensAPI.getNPCRegistry()
                                .getByUniqueId(u).getName());
                    } else {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(u);
                    }
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getPermissions().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorPermissions"));
                for (final String e : c.getPermissions()) {
                    msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(e);
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getItemsWhileHoldingMainHand().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorItemsInMainHand"));
                for (final ItemStack is : c.getItemsWhileHoldingMainHand()) {
                    msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(BukkitItemUtil.getPrettyItemName(is
                            .getType().name()));
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getItemsWhileWearing().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorItemsWear"));
                for (final ItemStack is : c.getItemsWhileWearing()) {
                    msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(BukkitItemUtil.getPrettyItemName(is
                            .getType().name()));
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getWorldsWhileStayingWithin().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorStayWithinWorld"));
                for (final String w : c.getWorldsWhileStayingWithin()) {
                    msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(w);
                }
                quester.sendMessage(msg.toString());
            } else if (c.getTickStartWhileStayingWithin() > -1 && c.getTickEndWhileStayingWithin() > -1) {
                msg.append(BukkitLang.get("conditionEditorStayWithinTicks"));
                msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(c.getTickStartWhileStayingWithin())
                        .append(" - ").append(c.getTickEndWhileStayingWithin());
                quester.sendMessage(msg.toString());
            } else if (!c.getBiomesWhileStayingWithin().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorStayWithinBiome"));
                for (final String b : c.getBiomesWhileStayingWithin()) {
                    msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(BukkitMiscUtil
                            .snakeCaseToUpperCamelCase(b));
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getRegionsWhileStayingWithin().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorStayWithinRegion"));
                for (final String r : c.getRegionsWhileStayingWithin()) {
                    msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(r);
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getPlaceholdersCheckIdentifier().isEmpty()) {
                msg.append(BukkitLang.get("conditionEditorCheckPlaceholder"));
                int index = 0;
                for (final String r : c.getPlaceholdersCheckIdentifier()) {
                    if (c.getPlaceholdersCheckValue().size() > index) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(r).append(ChatColor.GRAY)
                                .append(" = ").append(ChatColor.AQUA).append(c.getPlaceholdersCheckValue()
                                        .get(index));
                    }
                    index++;
                }
                quester.sendMessage(msg.toString());
            }
        }
    }

    @Override
    public void breakBlock(Quest quest, ItemStack itemStack) {
        breakBlock(quest, BlockItemStack.of(itemStack));
    }

    /**
     * Marks block as broken if Quester has such an objective
     *
     * @param quest The quest for which the block is being broken
     * @param broken The block being broken
     */
    @SuppressWarnings("deprecation")
    public void breakBlock(final Quest quest, final BlockItemStack broken) {
        BlockItemStack goal = null;
        for (final BlockItemStack toBreak : ((BukkitStage) getCurrentStage(quest)).getBlocksToBreak()) {
            if (goal != null) {
                break;
            }

            if (broken.matches(toBreak)) {
                goal = toBreak;
            } else {
                continue; // TODO
            }

            if (broken.getType() == toBreak.getType()) {
                if (broken.getType().isSolid() && toBreak.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (broken.getDurability() == toBreak.getDurability()) {
                        goal = toBreak;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        goal = toBreak;
                    }
                } /*else if (broken.getBlockData() instanceof Ageable && toBreak.getBlockData() instanceof Ageable) {
                    if (toBreak.getDurability() > 0) {
                        // Age toBreak specified so check for durability
                        if (broken.getDurability() == toBreak.getDurability()) {
                            goal = toBreak;
                        }
                    } else {
                        // Age toBreak unspecified so ignore durability
                        goal = toBreak;
                    }
                } */ else if (Material.getMaterial("CRAFTER") != null && broken.getType().isEdible()) {
                    // Paper 1.21+ is special case
                    final short toBreakAge = /* NBT.get(toBreak, nbt -> (short) nbt.getShort("quests_age")); */ 0;
                    final short brokenAge = broken.getDurability();
                    if (toBreakAge > 0) {
                        // Age toBreak specified so check for durability
                        if (brokenAge == toBreakAge) {
                            goal = toBreak;
                        }
                    } else {
                        // Age toBreak unspecified so ignore durability
                        goal = toBreak;
                    }
                } else if (broken.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (broken.getDurability() == toBreak.getDurability()) {
                        goal = toBreak;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    goal = toBreak;
                }
            }
        }

        if (goal == null) {
            // No match found
            return;
        }

        final ObjectiveType type = ObjectiveType.BREAK_BLOCK;
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, broken.getAmount(), goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final int breakIndex = getCurrentStage(quest).getBlocksToBreak().indexOf(goal);
        if (bukkitQuestProgress.blocksBroken.get(breakIndex) > goal.getAmount()) {
            return;
        }
        final int progress = bukkitQuestProgress.blocksBroken.get(breakIndex) + 1;
        bukkitQuestProgress.blocksBroken.set(breakIndex, progress);
        if (progress >= goal.getAmount()) {
            finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                    null, null, null, null);

            // Multiplayer
            final BlockItemStack finalGoal = goal;
            dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).blocksBroken.set(breakIndex, progress);
                q.finishObjective(quest, new BukkitObjective(type, null, progress, finalGoal), null, null, null,
                        null, null, null, null);
                return null;
            });
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, progress, goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    @Override
    public void damageBlock(Quest quest, ItemStack itemStack) {
        damageBlock(quest, BlockItemStack.of(itemStack));
    }

    /**
     * Marks block as damaged if Quester has such an objective
     *
     * @param quest The quest for which the block is being damaged
     * @param damaged The block being damaged
     */
    @SuppressWarnings("deprecation")
    public void damageBlock(final Quest quest, final BlockItemStack damaged) {
        BlockItemStack goal = null;
        for (final BlockItemStack toDamage : ((BukkitStage) getCurrentStage(quest)).getBlocksToDamage()) {
            if (goal != null) {
                break;
            }
            if (damaged.getType() == toDamage.getType()) {
                if (damaged.getType().isSolid() && toDamage.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (damaged.getDurability() == toDamage.getDurability()) {
                        goal = toDamage;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        goal = toDamage;
                    }
                } else if (damaged.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (damaged.getDurability() == toDamage.getDurability()) {
                        goal = toDamage;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    goal = toDamage;
                }
            }
        }

        if (goal == null) {
            // No match found
            return;
        }

        final ObjectiveType type = ObjectiveType.DAMAGE_BLOCK;
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, damaged.getAmount(), goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final int damageIndex = getCurrentStage(quest).getBlocksToDamage().indexOf(goal);
        if (bukkitQuestProgress.blocksDamaged.get(damageIndex) > goal.getAmount()) {
            return;
        }
        final int progress = bukkitQuestProgress.blocksDamaged.get(damageIndex) + 1;
        bukkitQuestProgress.blocksDamaged.set(damageIndex, progress);
        if (progress >= goal.getAmount()) {
            finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                    null, null, null, null);

            // Multiplayer
            final BlockItemStack finalGoal = goal;
            dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).blocksDamaged.set(damageIndex, progress);
                q.finishObjective(quest, new BukkitObjective(type, null, progress, finalGoal), null, null, null,
                        null, null, null, null);
                return null;
            });
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, progress, goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    @Override
    public void placeBlock(Quest quest, ItemStack itemStack) {
        placeBlock(quest, BlockItemStack.of(itemStack));
    }

    /**
     * Marks block as placed if Quester has such an objective
     *
     * @param quest The quest for which the block is being placed
     * @param placed The block being placed
     */
    @SuppressWarnings("deprecation")
    public void placeBlock(final Quest quest, final BlockItemStack placed) {
        BlockItemStack goal = null;
        for (final BlockItemStack toPlace : ((BukkitStage) getCurrentStage(quest)).getBlocksToPlace()) {
            if (goal != null) {
                break;
            }
            if (placed.getType() == toPlace.getType()) {
                if (placed.getType().isSolid() && toPlace.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (placed.getDurability() == toPlace.getDurability()) {
                        goal = toPlace;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        goal = toPlace;
                    }
                } else if (placed.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (placed.getDurability() == toPlace.getDurability()) {
                        goal = toPlace;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    goal = toPlace;
                }
            }
        }

        if (goal == null) {
            // No match found
            return;
        }

        final ObjectiveType type = ObjectiveType.PLACE_BLOCK;
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, placed.getAmount(), goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final int placeIndex = getCurrentStage(quest).getBlocksToPlace().indexOf(goal);
        if (bukkitQuestProgress.blocksPlaced.get(placeIndex) > goal.getAmount()) {
            return;
        }
        final int progress = bukkitQuestProgress.blocksPlaced.get(placeIndex) + 1;
        bukkitQuestProgress.blocksPlaced.set(placeIndex, progress);
        if (progress >= goal.getAmount()) {
            finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                    null, null, null, null);

            // Multiplayer
            final BlockItemStack finalGoal = goal;
            dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).blocksPlaced.set(placeIndex, progress);
                q.finishObjective(quest, new BukkitObjective(type, null, progress, finalGoal), null, null, null,
                        null, null, null, null);
                return null;
            });
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, progress, goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    @Override
    public void useBlock(Quest quest, ItemStack itemStack) {
        useBlock(quest, BlockItemStack.of(itemStack));
    }

    /**
     * Marks block as used if Quester has such an objective
     *
     * @param quest The quest for which the block is being used
     * @param used The block being used
     */
    @SuppressWarnings("deprecation")
    public void useBlock(final Quest quest, final BlockItemStack used) {
        BlockItemStack goal = null;
        for (final BlockItemStack toUse : ((BukkitStage) getCurrentStage(quest)).getBlocksToUse()) {
            if (goal != null) {
                break;
            }
            if (used.getType() == toUse.getType() ) {
                if (used.getType().isSolid() && toUse.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (used.getDurability() == toUse.getDurability()) {
                        goal = toUse;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        goal = toUse;
                    }
                } else if (used.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (used.getDurability() == toUse.getDurability()) {
                        goal = toUse;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    goal = toUse;
                }
            }
        }

        if (goal == null) {
            // No match found
            return;
        }

        final ObjectiveType type = ObjectiveType.USE_BLOCK;
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, used.getAmount(), goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final int useIndex = getCurrentStage(quest).getBlocksToUse().indexOf(goal);
        if (bukkitQuestProgress.blocksUsed.get(useIndex) > goal.getAmount()) {
            return;
        }
        final int progress = bukkitQuestProgress.blocksUsed.get(useIndex) + 1;
        bukkitQuestProgress.blocksUsed.set(useIndex, progress);
        if (progress >= goal.getAmount()) {
            finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                    null, null, null, null);

            // Multiplayer
            final BlockItemStack finalGoal = goal;
            dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).blocksUsed.set(useIndex, progress);
                q.finishObjective(quest, new BukkitObjective(type, null, progress, finalGoal), null, null, null,
                        null, null, null, null);
                return null;
            });
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, progress, goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    @Override
    public void cutBlock(Quest quest, ItemStack itemStack) {
        cutBlock(quest, BlockItemStack.of(itemStack));
    }

    /**
     * Marks block as cut if Quester has such an objective
     *
     * @param quest The quest for which the block is being cut
     * @param cut The block being cut
     */
    @SuppressWarnings("deprecation")
    public void cutBlock(final Quest quest, final BlockItemStack cut) {
        BlockItemStack goal = null;
        for (final BlockItemStack toCut : ((BukkitStage) getCurrentStage(quest)).getBlocksToCut()) {
            if (goal != null) {
                break;
            }
            if (cut.getType() == toCut.getType()) {
                if (cut.getType().isSolid() && toCut.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (cut.getDurability() == toCut.getDurability()) {
                        goal = toCut;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        goal = toCut;
                    }
                } else if (cut.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (cut.getDurability() == toCut.getDurability()) {
                        goal = toCut;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    goal = toCut;
                }
            }
        }

        if (goal == null) {
            // No match found
            return;
        }

        final ObjectiveType type = ObjectiveType.CUT_BLOCK;
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, cut.getAmount(), goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final int cutIndex = getCurrentStage(quest).getBlocksToCut().indexOf(goal);
        if (bukkitQuestProgress.blocksCut.get(cutIndex) > goal.getAmount()) {
            return;
        }
        final int progress = bukkitQuestProgress.blocksCut.get(cutIndex) + 1;
        bukkitQuestProgress.blocksCut.set(cutIndex, progress);
        if (progress >= goal.getAmount()) {
            finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                    null, null, null, null);

            // Multiplayer
            final BlockItemStack finalGoal = goal;
            dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).blocksCut.set(cutIndex, progress);
                q.finishObjective(quest, new BukkitObjective(type, null, progress, finalGoal), null, null, null,
                        null, null, null, null);
                return null;
            });
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, progress, goal.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark item as crafted if Quester has such an objective
     *
     * @param quest The quest for which the item is being crafted
     * @param crafted The item being crafted
     */
    public void craftItem(final Quest quest, final ItemStack crafted) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<>();
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        for (final ItemStack toCraft : ((BukkitStage) getCurrentStage(quest)).getItemsToCraft()) {
            currentIndex++;
            if (BukkitItemUtil.compareItems(crafted, toCraft, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final int amount = bukkitQuestProgress.itemsCrafted.get(match);
            final ItemStack goal = ((BukkitStage) getCurrentStage(quest)).getItemsToCraft().get(match);

            final ObjectiveType type = ObjectiveType.CRAFT_ITEM;
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, amount, goal));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int progress = crafted.getAmount() + amount;
            bukkitQuestProgress.itemsCrafted.set(match, progress);
            if (progress >= goal.getAmount()) {
                finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null, null,
                        null, null, null);

                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).itemsCrafted.set(match, progress);
                    q.finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                            null, null, null, null);
                    return null;
                });
            }

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, progress, goal));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark item as smelted if Quester has such an objective
     *
     * @param quest The quest for which the item is being smelted
     * @param smelted The item being smelted
     */
    public void smeltItem(final Quest quest, final ItemStack smelted) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<>();
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        for (final ItemStack toSmelt : ((BukkitStage) getCurrentStage(quest)).getItemsToSmelt()) {
            currentIndex++;
            if (BukkitItemUtil.compareItems(smelted, toSmelt, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final int amount = bukkitQuestProgress.itemsSmelted.get(match);
            final ItemStack goal = ((BukkitStage) getCurrentStage(quest)).getItemsToSmelt().get(match);

            final ObjectiveType type = ObjectiveType.SMELT_ITEM;
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, amount, goal));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int progress = smelted.getAmount() + amount;
            bukkitQuestProgress.itemsSmelted.set(match, progress);
            if (progress >= goal.getAmount()) {
                finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null, null,
                        null, null, null);

                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).itemsSmelted.set(match, progress);
                    q.finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                            null, null, null, null);
                    return null;
                });
            }

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, progress, goal));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Marks book as enchanted if Quester has such an objective
     *
     * @param quest The quest for which the item is being enchanted
     * @param enchantedBook The book being enchanted
     */
    public void enchantBook(final Quest quest, final ItemStack enchantedBook,
                            final Map<Enchantment, Integer> enchantsToAdd) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<>();
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        for (final ItemStack toEnchant : ((BukkitStage) getCurrentStage(quest)).getItemsToEnchant()) {
            currentIndex++;
            if (toEnchant.getItemMeta() instanceof EnchantmentStorageMeta) {
                if (((EnchantmentStorageMeta)toEnchant.getItemMeta()).getStoredEnchants().equals(enchantsToAdd)) {
                    matches.add(currentIndex);
                }
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final int amount = bukkitQuestProgress.itemsEnchanted.get(match);
            final ItemStack goal = ((BukkitStage) getCurrentStage(quest)).getItemsToEnchant().get(match);

            final ObjectiveType type = ObjectiveType.ENCHANT_ITEM;
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, amount, goal));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int progress = enchantedBook.getAmount() + amount;
            bukkitQuestProgress.itemsEnchanted.set(match, progress);
            if (progress >= goal.getAmount()) {
                finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null, null,
                        null, null, null);

                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).itemsEnchanted.set(match, progress);
                    q.finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                            null, null, null, null);
                    return null;
                });
            }

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, enchantedBook.getAmount() + amount, goal));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark item as enchanted if Quester has such an objective
     *
     * @param quest The quest for which the item is being enchanted
     * @param enchanted The item being enchanted
     */
    public void enchantItem(final Quest quest, final ItemStack enchanted) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<>();
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (!enchanted.getType().equals(Material.BOOK)) {
            for (final ItemStack toEnchant : ((BukkitStage) getCurrentStage(quest)).getItemsToEnchant()) {
                currentIndex++;
                if (!toEnchant.getEnchantments().isEmpty()) {
                    if (BukkitItemUtil.compareItems(enchanted, toEnchant, true) == 0) {
                        matches.add(currentIndex);
                    }
                } else {
                    if (BukkitItemUtil.compareItems(enchanted, toEnchant, true) == -4) {
                        matches.add(currentIndex);
                    }
                }
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final int amount = bukkitQuestProgress.itemsEnchanted.get(match);
            final ItemStack goal = ((BukkitStage) getCurrentStage(quest)).getItemsToEnchant().get(match);

            final ObjectiveType type = ObjectiveType.ENCHANT_ITEM;
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, amount, goal));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int progress = enchanted.getAmount() + amount;
            bukkitQuestProgress.itemsEnchanted.set(match, progress);
            if (progress >= goal.getAmount()) {
                finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null, null,
                        null, null, null);

                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).itemsEnchanted.set(match, progress);
                    q.finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                            null, null, null, null);
                    return null;
                });
            }

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, enchanted.getAmount() + amount, goal));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark item as brewed if Quester has such an objective
     *
     * @param quest The quest for which the item is being brewed
     * @param brewed The item being brewed
     */
    public void brewItem(final Quest quest, final ItemStack brewed) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<>();
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        for (final ItemStack toBrew : ((BukkitStage) getCurrentStage(quest)).getItemsToBrew()) {
            currentIndex++;
            if (BukkitItemUtil.compareItems(brewed, toBrew, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final int amount = bukkitQuestProgress.itemsBrewed.get(match);
            final ItemStack goal = ((BukkitStage) getCurrentStage(quest)).getItemsToBrew().get(match);

            final ObjectiveType type = ObjectiveType.BREW_ITEM;
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, amount, goal));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int progress = brewed.getAmount() + amount;
            bukkitQuestProgress.itemsBrewed.set(match, progress);
            if (progress >= goal.getAmount()) {
                finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null, null,
                        null, null, null);

                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).itemsBrewed.set(match, progress);
                    q.finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                            null, null, null, null);
                    return null;
                });
            }

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, progress, goal));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark item as consumed if Quester has such an objective
     *
     * @param quest The quest for which the item is being consumed
     * @param consumed The item being consumed
     */
    public void consumeItem(final Quest quest, final ItemStack consumed) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<>();
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        for (final ItemStack toConsume : ((BukkitStage) getCurrentStage(quest)).getItemsToConsume()) {
            currentIndex++;
            if (BukkitItemUtil.compareItems(consumed, toConsume, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final int amount = bukkitQuestProgress.itemsConsumed.get(match);
            final ItemStack goal = ((BukkitStage) getCurrentStage(quest)).getItemsToConsume().get(match);

            final ObjectiveType type = ObjectiveType.CONSUME_ITEM;
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, amount, goal));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int progress = consumed.getAmount() + amount;
            bukkitQuestProgress.itemsConsumed.set(match, progress);
            if (progress >= goal.getAmount()) {
                finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null, null,
                        null, null, null);

                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).itemsConsumed.set(match, progress);
                    q.finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null,
                            null, null, null, null);
                    return null;
                });
            }

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, progress, goal));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark item as delivered to a NPC if Quester has such an objective
     *
     * @param quest The quest for which the item is being delivered
     * @param npc UUID of the NPC being delivered to
     * @param delivered The item being delivered
     */
    public void deliverToNPC(final Quest quest, final UUID npc, final ItemStack delivered) {
        if (npc == null) {
            return;
        }

        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<>();
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        for (final ItemStack toDeliver : ((BukkitStage) getCurrentStage(quest)).getItemsToDeliver()) {
            currentIndex++;
            if (BukkitItemUtil.compareItems(delivered, toDeliver, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        final Player player = getPlayer();
        for (final Integer match : matches) {
            if (!getCurrentStage(quest).getItemDeliveryTargets().get(match).equals(npc)) {
                continue;
            }
            final int amount = bukkitQuestProgress.itemsDelivered.get(match);
            final ItemStack goal = ((BukkitStage) getCurrentStage(quest)).getItemsToDeliver().get(match);

            final ObjectiveType type = ObjectiveType.DELIVER_ITEM;
            final Set<String> dispatchedQuestIDs = new HashSet<>();
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, amount, goal));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int progress = delivered.getAmount() + amount;
            final int index = player.getInventory().first(delivered);
            if (index == -1) {
                // Already delivered in previous loop
                return;
            }
            bukkitQuestProgress.itemsDelivered.set(match, progress);
            if (progress >= goal.getAmount()) {
                if ((delivered.getAmount() + amount) >= goal.getAmount()) {
                    // Take away remaining amount to be delivered
                    final ItemStack clone = delivered.clone();
                    clone.setAmount(delivered.getAmount() - (goal.getAmount() - amount));
                    player.getInventory().setItem(index, clone);
                } else {
                    player.getInventory().setItem(index, null);
                }
                player.updateInventory();
                finishObjective(quest, new BukkitObjective(type, null, progress, goal), null, null, null, null,
                        null, null, null);
            } else {
                player.getInventory().setItem(index, null);
                player.updateInventory();
                final String[] message = BukkitConfigUtil.parseStringWithPossibleLineBreaks(getCurrentStage(quest)
                                .getDeliverMessages().get(new Random().nextInt(getCurrentStage(quest)
                                .getDeliverMessages().size())), getCurrentStage(quest).getItemDeliveryTargets()
                                .get(match), goal.getAmount() - progress);
                player.sendMessage(message);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, ObjectiveType.DELIVER_ITEM,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).itemsDelivered.set(match, progress);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, progress, goal));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark NPC as interacted with if Quester has such an objective
     *
     * @param quest The quest for which the NPC is being interacted with
     * @param npc UUID of the NPC being interacted with
     */
    public void interactWithNPC(final Quest quest, final UUID npc) {
        if (!getCurrentStage(quest).getNpcsToInteract().contains(npc)) {
            return;
        }

        final int index = getCurrentStage(quest).getNpcsToInteract().indexOf(npc);
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final boolean npcsInteracted = bukkitQuestProgress.npcsInteracted.get(index);

        final ObjectiveType type = ObjectiveType.TALK_TO_NPC;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, 1, 1));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        if (!npcsInteracted) {
            bukkitQuestProgress.npcsInteracted.set(index, true);
            finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                            new ItemStack(Material.AIR, 1)), null, null, npc, null, null, null, null);

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).npcsInteracted.set(index, true);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, 1, 1));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark NPC as killed if the Quester has such an objective
     *
     * @param quest The quest for which the NPC is being killed
     * @param npc UUID of the NPC being killed
     */
    public void killNPC(final Quest quest, final UUID npc) {
        if (!getCurrentStage(quest).getNpcsToKill().contains(npc)) {
            return;
        }

        final int index = getCurrentStage(quest).getNpcsToKill().indexOf(npc);
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        final int npcsKilled = bukkitQuestProgress.npcsNumKilled.get(index);
        final int npcsToKill = getCurrentStage(quest).getNpcNumToKill().get(index);

        final ObjectiveType type = ObjectiveType.KILL_NPC;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, npcsKilled, npcsToKill));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final int newNpcsKilled = bukkitQuestProgress.npcsNumKilled.get(index) + 1;
        if (npcsKilled < npcsToKill) {
            bukkitQuestProgress.npcsNumKilled.set(index, newNpcsKilled);
            if (newNpcsKilled >= npcsToKill) {
                finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, npcsToKill)), null, null, npc, null, null, null, null);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).npcsNumKilled
                                    .set(index, newNpcsKilled);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, newNpcsKilled, npcsToKill));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Marks cow as milked if Quester has such an objective
     *
     * @param quest The quest for which the fish is being caught
     */
    public void milkCow(final Quest quest) {
        final BukkitQuestProgress questProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (questProgress == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage == null) {
            return;
        }
        if (currentStage.getCowsToMilk() == null) {
            return;
        }

        final int cowsMilked = questProgress.getCowsMilked();
        final int cowsToMilk = currentStage.getCowsToMilk();

        final ObjectiveType type = ObjectiveType.MILK_COW;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, cowsMilked, cowsToMilk));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final int newCowsMilked = cowsMilked + 1;
        if (cowsMilked < cowsToMilk) {
            questProgress.setCowsMilked(newCowsMilked);

            if (newCowsMilked >= cowsToMilk) {
                finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, cowsToMilk)), null, null, null, null, null, null, null);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.getQuestProgressOrDefault(quest).setCowsMilked(newCowsMilked);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, newCowsMilked, cowsToMilk));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Marks fish as caught if Quester has such an objective
     *
     * @param quest The quest for which the fish is being caught
     */
    public void catchFish(final Quest quest) {
        final BukkitQuestProgress questProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (questProgress == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage == null) {
            return;
        }
        if (currentStage.getFishToCatch() == null) {
            return;
        }

        final int fishCaught = questProgress.getFishCaught();
        final int fishToCatch = currentStage.getFishToCatch();

        final ObjectiveType type = ObjectiveType.CATCH_FISH;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, fishCaught, fishToCatch));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final int newFishCaught = fishCaught + 1;
        if (fishCaught < fishToCatch) {
            questProgress.setFishCaught(newFishCaught);

            if (newFishCaught >= fishToCatch) {
                finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, fishToCatch)), null, null, null, null, null, null, null);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            q.getQuestProgressOrDefault(quest).setFishCaught(newFishCaught);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, newFishCaught, fishToCatch));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark mob as killed if Quester has such an objective
     *
     * @param quest The quest for which the mob is being killed
     * @param killedLocation The optional location to kill at
     * @param entityType The mob to be killed
     */
    public void killMob(final Quest quest, final Location killedLocation, final EntityType entityType) {
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (entityType == null) {
            return;
        }
        final BukkitStage currentStage = ((BukkitStage) getCurrentStage(quest));
        if (currentStage.getMobsToKill() == null) {
            return;
        }
        final int index = currentStage.getMobsToKill().indexOf(entityType);
        if (index == -1) {
            return;
        }
        final int mobsKilled = bukkitQuestProgress.mobNumKilled.get(index);
        final int mobsToKill = currentStage.getMobNumToKill().get(index);
        if (!currentStage.getLocationsToKillWithin().isEmpty()) {
            final Location locationToKillWithin = currentStage.getLocationsToKillWithin().get(index);
            final double radius = currentStage.getRadiiToKillWithin().get(index);
            if (killedLocation.getWorld() == null || locationToKillWithin.getWorld() == null) {
                return;
            }
            if (!(killedLocation.getWorld().getName().equals(locationToKillWithin.getWorld().getName()))) {
                return;
            }
            if (!(killedLocation.getX() < (locationToKillWithin.getX() + radius) && killedLocation.getX()
                    > (locationToKillWithin.getX() - radius))) {
                return;
            }
            if (!(killedLocation.getZ() < (locationToKillWithin.getZ() + radius) && killedLocation.getZ()
                    > (locationToKillWithin.getZ() - radius))) {
                return;
            }
            if (!(killedLocation.getY() < (locationToKillWithin.getY() + radius) && killedLocation.getY()
                    > (locationToKillWithin.getY() - radius))) {
                return;
            }
        }
        final ObjectiveType type = ObjectiveType.KILL_MOB;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, mobsKilled, mobsToKill));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final int newMobsKilled = mobsKilled + 1;
        if (mobsKilled < mobsToKill) {
            bukkitQuestProgress.mobNumKilled.set(index, newMobsKilled);
            if (newMobsKilled >= mobsToKill) {
                finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, mobsToKill)), entityType, null, null, null, null, null, null);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            final int i = q.getCurrentStage(cq).getMobsToKill().indexOf(entityType);
                            if (i == -1) {
                                return null;
                            }
                            final int kills = q.getQuestProgressOrDefault(quest).getMobNumKilled().get(i);
                            ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).mobNumKilled.set(index, kills + 1);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, newMobsKilled, mobsToKill));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark player as killed if Quester has such an objective
     *
     * @param quest The quest for which the player is being killed
     * @param player The player to be killed
     */
    public void killPlayer(final Quest quest, final Player player) {
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (bukkitQuestProgress == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage == null) {
            return;
        }
        if (currentStage.getPlayersToKill() == null) {
            return;
        }

        final int playersKilled = bukkitQuestProgress.getPlayersKilled();
        final int playersToKill = currentStage.getPlayersToKill();

        final ObjectiveType type = ObjectiveType.KILL_PLAYER;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, playersKilled, playersToKill));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final int newPlayersKilled = playersKilled + 1;
        if (playersKilled < playersToKill) {
            bukkitQuestProgress.setPlayersKilled(newPlayersKilled);
            if (newPlayersKilled >= playersToKill) {
                finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, playersToKill)), null, null, null, null, null, null, null);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            final int kills = q.getQuestProgressOrDefault(quest).getPlayersKilled();
                            q.getQuestProgressOrDefault(quest).setPlayersKilled(kills + 1);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, newPlayersKilled, playersToKill));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark location as reached if the Quester has such an objective
     *
     * @param quest The quest for which the location is being reached
     * @param location The location being reached
     */
    public void reachLocation(final Quest quest, final Location location) {
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (bukkitQuestProgress == null || bukkitQuestProgress.locationsReached == null || getCurrentStage(quest) == null
                || getCurrentStage(quest).getLocationsToReach() == null) {
            return;
        }

        final LinkedList<Location> locationsToReach = ((BukkitStage) getCurrentStage(quest)).getLocationsToReach();
        final int goal = locationsToReach.size();
        for (int i = 0; i < goal; i++) {
            final Location toReach = locationsToReach.get(i);
            if (location.getWorld() == null || toReach.getWorld() == null
                    || !location.getWorld().getName().equals(toReach.getWorld().getName())) {
                continue;
            }
            final double radius = getCurrentStage(quest).getRadiiToReachWithin().get(i);
            if (toReach.distanceSquared(location) <= radius * radius) {
                if (!bukkitQuestProgress.locationsReached.get(i)) {
                    int progress = 0;
                    for (final Boolean b : bukkitQuestProgress.locationsReached) {
                        if (b) {
                            progress++;
                        }
                    }
                    final ObjectiveType type = ObjectiveType.REACH_LOCATION;
                    final Set<String> dispatchedQuestIDs = new HashSet<>();
                    final BukkitQuesterPreUpdateObjectiveEvent preEvent
                            = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                            new BukkitObjective(type, null, progress, goal));
                    plugin.getServer().getPluginManager().callEvent(preEvent);

                    bukkitQuestProgress.locationsReached.set(i, true);
                    finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                            new ItemStack(Material.AIR, 1)), null, null, null, toReach, null, null, null);

                    int finalIndex = i;
                    dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                            (final Quester q, final Quest cq) -> {
                                if (!dispatchedQuestIDs.contains(cq.getId())) {
                                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).locationsReached
                                            .set(finalIndex, true);
                                    if (q.testComplete(quest)) {
                                        quest.nextStage(q, false);
                                    }
                                }
                                return null;
                            }));

                    final BukkitQuesterPostUpdateObjectiveEvent postEvent
                            = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                            new BukkitObjective(type, null, progress + 1, goal));
                    plugin.getServer().getPluginManager().callEvent(postEvent);

                    break;
                }
            }
        }
    }

    /**
     * Mark mob as tamed if the Quester has such an objective
     *
     * @param quest The quest for which the mob is being tamed
     * @param entityType The type of mob being tamed
     */
    public void tameMob(final Quest quest, final EntityType entityType) {
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (entityType == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage.getMobsToTame() == null) {
            return;
        }

        final int index = currentStage.getMobsToTame().indexOf(entityType);
        if (index == -1) {
            return;
        }

        final int mobsToTame = currentStage.getMobNumToTame().get(index);
        final int mobsTamed = bukkitQuestProgress.mobsTamed.get(index);

        final ObjectiveType type = ObjectiveType.TAME_MOB;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, mobsToTame, mobsTamed));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final int newMobsToTame = mobsTamed + 1;
        if (mobsTamed < mobsToTame) {
            bukkitQuestProgress.mobsTamed.set(index, newMobsToTame);
            if (newMobsToTame >= mobsToTame) {
                finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, mobsToTame)), entityType, null, null, null, null, null, null);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).mobsTamed.set(index, newMobsToTame);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, newMobsToTame, mobsTamed));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark sheep as sheared if the Quester has such an objective
     *
     * @param quest The quest for which the sheep is being sheared
     * @param color The wool color of the sheep being sheared
     */
    public void shearSheep(final Quest quest, final DyeColor color) {
        final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
        if (color == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage.getSheepToShear() == null) {
            return;
        }

        final int index = currentStage.getSheepToShear().indexOf(color);
        if (index == -1) {
            return;
        }

        final int sheepToShear = getCurrentStage(quest).getSheepNumToShear().get(index);
        final int sheepSheared = bukkitQuestProgress.sheepSheared.get(index);

        final ObjectiveType type = ObjectiveType.SHEAR_SHEEP;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, sheepSheared, sheepToShear));
        plugin.getServer().getPluginManager().callEvent(preEvent);

        final int newSheepSheared = sheepSheared + 1;
        if (sheepSheared < sheepToShear) {
            bukkitQuestProgress.sheepSheared.set(index, newSheepSheared);
            if (newSheepSheared >= sheepToShear) {
                finishObjective(quest, new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, sheepToShear)), null, null, null, null, color, null, null);
            }

            dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                    (final Quester q, final Quest cq) -> {
                        if (!dispatchedQuestIDs.contains(cq.getId())) {
                            ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).sheepSheared
                                    .set(index, newSheepSheared);
                            if (q.testComplete(quest)) {
                                quest.nextStage(q, false);
                            }
                        }
                        return null;
                    }));
        }

        final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this, quest,
                new BukkitObjective(type, null, newSheepSheared, sheepToShear));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark password as entered if the Quester has such an objective
     *
     * @param quest The quest for which the password is being entered
     * @param evt The event during which the password was entered
     */
    public void sayPassword(final Quest quest, final AsyncPlayerChatEvent evt) {
        final ObjectiveType type = ObjectiveType.PASSWORD;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            final BukkitQuesterPreUpdateObjectiveEvent preEvent = new BukkitQuesterPreUpdateObjectiveEvent(this, quest,
                    new BukkitObjective(type, null, 1, 1));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            int index = 0;
            final BukkitQuestProgress bukkitQuestProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
            for (final String pass : getCurrentStage(quest).getPasswordPhrases()) {
                if (pass.equalsIgnoreCase(evt.getMessage())) {
                    final String display = getCurrentStage(quest).getPasswordDisplays().get(index);
                    bukkitQuestProgress.passwordsSaid.set(index, true);

                    plugin.getServer().getScheduler().runTask(plugin, () -> finishObjective(quest,
                            new BukkitObjective(type, null, new ItemStack(Material.AIR, 1),
                            new ItemStack(Material.AIR, 1)), null, null, null, null, null, display, null));

                    final int finalIndex = index;
                    dispatchedQuestIDs.addAll(dispatchMultiplayerEverything(quest, type,
                            (final Quester q, final Quest cq) -> {
                                if (!dispatchedQuestIDs.contains(cq.getId())) {
                                    ((BukkitQuestProgress) q.getQuestProgressOrDefault(quest)).passwordsSaid
                                            .set(finalIndex, true);
                                    if (q.testComplete(quest)) {
                                        quest.nextStage(q, false);
                                    }
                                }
                                return null;
                            }));
                    break;
                }
                index++;
            }

            final BukkitQuesterPostUpdateObjectiveEvent postEvent = new BukkitQuesterPostUpdateObjectiveEvent(this,
                    quest, new BukkitObjective(type, null, 1, 1));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        });
    }

    /**
     * Complete a quest objective
     *
     * @param quest
     *            Quest containing the objective
     * @param objective
     *            Objective for type, progress and goal
     * @param mob
     *            Mob being killed or tamed, if any
     * @param extra
     *            Extra mob enum like career or ocelot type, if any
     * @param npc
     *            UUID of NPC being talked to or killed, if any
     * @param location
     *            Location for user to reach, if any
     * @param color
     *            Shear color, if any
     * @param pass
     *            Password, if any
     * @param co
     *            Custom objective, if any. See {@link CustomObjective}
     */
    @SuppressWarnings("deprecation")
    public void finishObjective(final Quest quest, final Objective objective, final EntityType mob,
                                final String extra, final UUID npc, final Location location, final DyeColor color,
                                final String pass, final CustomObjective co) {
        if (objective == null) {
            return;
        }
        final Player p = getPlayer();
        final ObjectiveType type = objective.getType();
        final ItemStack goal = objective.getGoalObject() instanceof ItemStack ? (ItemStack) objective.getGoalObject()
                : new ItemStack(Material.AIR, objective.getGoal());
        final BlockItemStack goalBlock = objective.getGoalObject() instanceof BlockItemStack
                ? (BlockItemStack) objective.getGoalObject()
                : BlockItemStack.of(Material.AIR, objective.getGoal(), (short) 0);

        if (!getCurrentStage(quest).getObjectiveOverrides().isEmpty()) {
            for (final String s: getCurrentStage(quest).getObjectiveOverrides()) {
                String message = ChatColor.GREEN + "(" + BukkitLang.get(p, "completed") + ") "
                        + BukkitConfigUtil.parseString(ChatColor.translateAlternateColorCodes('&', s), quest, p);
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(p, message);
                }
                sendMessage(message);
            }
        } else if (type.equals(ObjectiveType.BREAK_BLOCK)) {
            final String message = formatCompletedObjectiveMessage("break", goalBlock.getAmount());
            if (plugin.getConfigSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message,
                        goalBlock.getType(), goalBlock.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
            }
        } else if (type.equals(ObjectiveType.DAMAGE_BLOCK)) {
            final String message = formatCompletedObjectiveMessage("damage", goalBlock.getAmount());
            if (plugin.getConfigSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message,
                        goalBlock.getType(), goalBlock.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
            }
        } else if (type.equals(ObjectiveType.PLACE_BLOCK)) {
            final String message = formatCompletedObjectiveMessage("place", goalBlock.getAmount());
            if (plugin.getConfigSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message,
                        goalBlock.getType(), goalBlock.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
            }
        } else if (type.equals(ObjectiveType.USE_BLOCK)) {
            final String message = formatCompletedObjectiveMessage("use", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message,
                        goalBlock.getType(), goalBlock.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
            }
        } else if (type.equals(ObjectiveType.CUT_BLOCK)) {
            final String message = formatCompletedObjectiveMessage("cut", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message,
                        goalBlock.getType(), goalBlock.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(goalBlock)));
            }
        } else if (type.equals(ObjectiveType.CRAFT_ITEM)) {
            final ItemStack is = ((BukkitStage) getCurrentStage(quest)).getItemsToCraft().get(getCurrentStage(quest)
                    .getItemsToCraft().indexOf(goal));
            String message = formatCompletedObjectiveMessage("craftItem", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames() && !goal.hasItemMeta()
                    && !goal.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                if (goal.getType().name().equals("TIPPED_ARROW")) {
                    final String level = BukkitItemUtil.getPrettyPotionLevel(goal.getItemMeta());
                    if (!level.isEmpty()) {
                        message = message.replace("<item>", "<item> " + level);
                    }
                }
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.SMELT_ITEM)) {
            final ItemStack is = ((BukkitStage) getCurrentStage(quest)).getItemsToSmelt().get(getCurrentStage(quest)
                    .getItemsToSmelt().indexOf(goal));
            final String message = formatCompletedObjectiveMessage("smeltItem", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames() && !goal.hasItemMeta()
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.ENCHANT_ITEM)) {
            final ItemStack is = ((BukkitStage) getCurrentStage(quest)).getItemsToEnchant().get(getCurrentStage(quest)
                    .getItemsToEnchant().indexOf(goal));
            final String message = formatCompletedObjectiveMessage("enchItem", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames() && is.hasItemMeta()
                    && !is.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments(), goal.getItemMeta())) {
                    for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                        sendMessage(message.replace("<item>", BukkitItemUtil.getName(is))
                                .replace("<enchantment>", BukkitItemUtil.getPrettyEnchantmentName(e.getKey()))
                                .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                    }
                }
            } else if (plugin.getConfigSettings().canTranslateNames() && !is.hasItemMeta()
                    && Material.getMaterial("LINGERING_POTION") == null) {
                // Bukkit version is below 1.9
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments())) {
                    for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                        sendMessage(message.replace("<item>", BukkitItemUtil.getName(is))
                                .replace("<enchantment>", BukkitItemUtil.getPrettyEnchantmentName(e.getKey()))
                                .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                    }
                }
            } else {
                for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(is))
                            .replace("<enchantment>", BukkitItemUtil.getPrettyEnchantmentName(e.getKey()))
                            .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                }
            }
        } else if (type.equals(ObjectiveType.BREW_ITEM)) {
            final ItemStack is = ((BukkitStage) getCurrentStage(quest)).getItemsToBrew().get(getCurrentStage(quest)
                    .getItemsToBrew().indexOf(goal));
            String message = formatCompletedObjectiveMessage("brewItem", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames() && is.hasItemMeta()
                    && !is.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                if (goal.getType().name().contains("POTION") && plugin.getLocaleManager().hasBasePotionData()) {
                    final String level = BukkitItemUtil.getPrettyPotionLevel(goal.getItemMeta());
                    if (level.isEmpty()) {
                        message = message.replace(" <level>", level);
                    } else {
                        message = message.replace("<level>", level);
                    }
                }
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments(), goal.getItemMeta())) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
                }
            } else if (plugin.getConfigSettings().canTranslateNames() && !is.hasItemMeta()
                    && Material.getMaterial("LINGERING_POTION") == null) {
                // Bukkit version is below 1.9
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments())) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.CONSUME_ITEM)) {
            final ItemStack is = ((BukkitStage) getCurrentStage(quest)).getItemsToConsume().get(getCurrentStage(quest)
                    .getItemsToConsume().indexOf(goal));
            String message = formatCompletedObjectiveMessage("consumeItem", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames() && !goal.hasItemMeta()
                    && !goal.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                if (goal.getType().name().contains("POTION") && plugin.getLocaleManager().hasBasePotionData()) {
                    final String level = BukkitItemUtil.getPrettyPotionLevel(goal.getItemMeta());
                    if (level.isEmpty()) {
                        message = message.replace(" <level>", level);
                    } else {
                        message = message.replace("<level>", level);
                    }
                }
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.DELIVER_ITEM)) {
            final ItemStack is = ((BukkitStage) getCurrentStage(quest)).getItemsToDeliver().get(getCurrentStage(quest)
                    .getItemsToDeliver().indexOf(goal));
            final String message = formatCompletedObjectiveMessage("deliver", goal.getAmount())
                    .replace("<npc>", plugin.getDependencies().getNpcName(getCurrentStage(quest)
                    .getItemDeliveryTargets().get(getCurrentStage(quest).getItemsToDeliver().indexOf(goal))));
            if (plugin.getConfigSettings().canTranslateNames() && !goal.hasItemMeta()
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, is.getType(), is.getDurability(), null)) {
                    sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", BukkitItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.MILK_COW)) {
            final String message = formatCompletedObjectiveMessage("milkCow", goal.getAmount());
            sendMessage(message);
        } else if (type.equals(ObjectiveType.CATCH_FISH)) {
            final String message = formatCompletedObjectiveMessage("catchFish", goal.getAmount());
            sendMessage(message);
        } else if (type.equals(ObjectiveType.KILL_MOB)) {
            final String message = formatCompletedObjectiveMessage("kill", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, mob, extra)) {
                    sendMessage(message.replace("<mob>", BukkitMiscUtil.snakeCaseToUpperCamelCase(mob.name())));
                }
            } else {
                sendMessage(message.replace("<mob>", BukkitMiscUtil.snakeCaseToUpperCamelCase(mob.name())));
            }
        } else if (type.equals(ObjectiveType.KILL_PLAYER)) {
            final String message = formatCompletedObjectiveMessage("killPlayer", goal.getAmount());
            sendMessage(message);
        } else if (type.equals(ObjectiveType.TALK_TO_NPC)) {
            final String message = formatCompletedObjectiveMessage("talkTo", goal.getAmount())
                    .replace("<npc>", plugin.getDependencies().getNpcName(npc));
            sendMessage(message);
        } else if (type.equals(ObjectiveType.KILL_NPC)) {
            final String message = formatCompletedObjectiveMessage("kill", goal.getAmount());
            sendMessage(message.replace("<mob>", plugin.getDependencies().getNpcName(npc)));
        } else if (type.equals(ObjectiveType.TAME_MOB)) {
            final String message = formatCompletedObjectiveMessage("tame", goal.getAmount());
            if (plugin.getConfigSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, mob, extra)) {
                    sendMessage(message.replace("<mob>", BukkitMiscUtil.snakeCaseToUpperCamelCase(mob.name())));
                }
            } else {
                sendMessage(message.replace("<mob>", BukkitMiscUtil.snakeCaseToUpperCamelCase(mob.name())));
            }
        } else if (type.equals(ObjectiveType.SHEAR_SHEEP)) {
            final String message = formatCompletedObjectiveMessage("shearSheep", goal.getAmount())
                    .replace("<color>", BukkitMiscUtil.getPrettyDyeColorName(color));
            sendMessage(message);
        } else if (type.equals(ObjectiveType.REACH_LOCATION)) {
            String obj = BukkitLang.get(p, "goTo");
            try {
                obj = obj.replace("<location>", getCurrentStage(quest).getLocationNames().get(getCurrentStage(quest)
                        .getLocationsToReach().indexOf(location)));
            } catch (final IndexOutOfBoundsException e) {
                plugin.getLogger().severe("Unable to get final location " + location + " for quest ID "
                        + quest.getId() + ", please report on Github");
                obj = obj.replace("<location>", "ERROR");
            }
            final String message = ChatColor.GREEN + "(" + BukkitLang.get(p, "completed") + ") " + obj;
            sendMessage(message);
        } else if (type.equals(ObjectiveType.PASSWORD)) {
            sendMessage(ChatColor.GREEN + "(" + BukkitLang.get(p, "completed") + ") " + pass);
        } else if (co != null) {
            String message = ChatColor.GREEN + "(" + BukkitLang.get(p, "completed") + ") " + co.getDisplay();
            int index = -1;
            for (int i = 0; i < getCurrentStage(quest).getCustomObjectives().size(); i++) {
                if (getCurrentStage(quest).getCustomObjectives().get(i).getName().equals(co.getName())) {
                    index = i;
                    break;
                }
            }
            final List<Entry<String, Object>> sub = new LinkedList<>(getCurrentStage(quest).getCustomObjectiveData()
                    .subList(index, getCurrentStage(quest).getCustomObjectiveData().size()));
            final List<Entry<String, Object>> end = new LinkedList<>(sub);
            sub.clear(); // Since sub is backed by end, this removes all sub-list items from end
            for (final Entry<String, Object> dataMap : end) {
                message = message.replace("%" + (dataMap.getKey()) + "%", String.valueOf(dataMap.getValue()));
            }

            if (co.canShowCount()) {
                message = message.replace("%count%", goal.getAmount() + "/" + goal.getAmount());
            }
            sendMessage(BukkitConfigUtil.parseString(ChatColor.translateAlternateColorCodes('&', message)));
        }
        dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
            q.finishObjective(quest, objective, mob, extra, npc, location, color, pass, co);
            return null;
        });
        if (testComplete(quest)) {
            quest.nextStage(this, true);
        }
    }

    /**
     * Format completed objective message with count and placeholders
     *
     * @param langKey Label as it appears in lang file
     * @param goal Objective goal
     * @return Formatted message
     */
    private String formatCompletedObjectiveMessage(final String langKey, final int goal) {
        String message = ChatColor.GREEN + "(" + BukkitLang.get(getPlayer(), "completed") + ") " + BukkitLang.get(langKey);
        if (message.contains("<count>")) {
            message = message.replace("<count>", goal + "/" + goal);
        }
        if (plugin.getDependencies().getPlaceholderApi() != null) {
            message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
        }
        return message;
    }

    /**
     * Check whether this Quester has completed all objectives for their current stage
     *
     * @param quest The quest with the current stage being checked
     * @return true if all stage objectives are marked complete
     */
    public boolean testComplete(final Quest quest) {
        for (final Objective objective : getCurrentObjectives(quest, true, false)) {
            if (objective.getProgress() < objective.getGoal()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add empty map values per Quest stage
     *
     * @param quest Quest with at least one stage
     * @param stage Where first stage is '0'
     */
    public void addEmptiesFor(final Quest quest, final int stage) {
        final BukkitQuestProgress data = new BukkitQuestProgress(this);
        data.setDoJournalUpdate(false);
        if (quest == null) {
            plugin.getLogger().warning("Unable to find quest for player " + this.lastKnownName);
            return;
        }
        if (quest.getStage(stage) == null) {
            plugin.getLogger().severe("Unable to find Stage " + stage + " of quest ID " + quest.getId());
            return;
        }
        final BukkitStage bukkitStage = (BukkitStage) quest.getStage(stage);
        if (!bukkitStage.getBlocksToBreak().isEmpty()) {
            for (final BlockItemStack ignored : bukkitStage.getBlocksToBreak()) {
                data.blocksBroken.add(0);
            }
        }
        if (!bukkitStage.getBlocksToDamage().isEmpty()) {
            for (final BlockItemStack ignored : bukkitStage.getBlocksToDamage()) {
                data.blocksDamaged.add(0);
            }
        }
        if (!bukkitStage.getBlocksToPlace().isEmpty()) {
            for (final BlockItemStack ignored : bukkitStage.getBlocksToPlace()) {
                data.blocksPlaced.add(0);
            }
        }
        if (!bukkitStage.getBlocksToUse().isEmpty()) {
            for (final BlockItemStack ignored : bukkitStage.getBlocksToUse()) {
                data.blocksUsed.add(0);
            }
        }
        if (!bukkitStage.getBlocksToCut().isEmpty()) {
            for (final BlockItemStack ignored : bukkitStage.getBlocksToCut()) {
                data.blocksCut.add(0);
            }
        }
        if (!bukkitStage.getItemsToCraft().isEmpty()) {
            for (final ItemStack ignored : bukkitStage.getItemsToCraft()) {
                data.itemsCrafted.add(0);
            }
        }
        if (!bukkitStage.getItemsToSmelt().isEmpty()) {
            for (final ItemStack ignored : bukkitStage.getItemsToSmelt()) {
                data.itemsSmelted.add(0);
            }
        }
        if (!bukkitStage.getItemsToEnchant().isEmpty()) {
            for (final ItemStack ignored : bukkitStage.getItemsToEnchant()) {
                data.itemsEnchanted.add(0);
            }
        }
        if (!bukkitStage.getItemsToBrew().isEmpty()) {
            for (final ItemStack ignored : bukkitStage.getItemsToBrew()) {
                data.itemsBrewed.add(0);
            }
        }
        if (!bukkitStage.getItemsToConsume().isEmpty()) {
            for (final ItemStack ignored : bukkitStage.getItemsToConsume()) {
                data.itemsConsumed.add(0);
            }
        }
        if (!bukkitStage.getItemsToDeliver().isEmpty()) {
            for (final ItemStack ignored : bukkitStage.getItemsToDeliver()) {
                data.itemsDelivered.add(0);
            }
        }
        if (!quest.getStage(stage).getNpcsToInteract().isEmpty()) {
            for (final UUID ignored : quest.getStage(stage).getNpcsToInteract()) {
                data.npcsInteracted.add(false);
            }
        }
        if (!quest.getStage(stage).getNpcsToKill().isEmpty()) {
            for (final UUID ignored : quest.getStage(stage).getNpcsToKill()) {
                data.npcsNumKilled.add(0);
            }
        }
        if (!quest.getStage(stage).getMobsToKill().isEmpty()) {
            for (final EntityType ignored : bukkitStage.getMobsToKill()) {
                data.mobNumKilled.add(0);
            }
        }
        data.setCowsMilked(0);
        data.setFishCaught(0);
        data.setPlayersKilled(0);
        if (!quest.getStage(stage).getLocationsToReach().isEmpty()) {
            for (final Location ignored : bukkitStage.getLocationsToReach()) {
                data.locationsReached.add(false);
            }
        }
        if (!quest.getStage(stage).getMobsToTame().isEmpty()) {
            for (final EntityType ignored : bukkitStage.getMobsToTame()) {
                data.mobsTamed.add(0);
            }
        }
        if (!quest.getStage(stage).getSheepToShear().isEmpty()) {
            for (final DyeColor ignored : bukkitStage.getSheepToShear()) {
                data.sheepSheared.add(0);
            }
        }
        if (!quest.getStage(stage).getPasswordDisplays().isEmpty()) {
            for (final String ignored : bukkitStage.getPasswordDisplays()) {
                data.passwordsSaid.add(false);
            }
        }
        if (!quest.getStage(stage).getCustomObjectives().isEmpty()) {
            for (final CustomObjective ignored : quest.getStage(stage).getCustomObjectives()) {
                data.customObjectiveCounts.add(0);
            }
        }
        data.setDoJournalUpdate(true);
        hardDataPut(quest, data);
    }


    /**
     * Save data of the Quester to file
     *
     * @return true if successful
     */
    public boolean saveData() {
        try {
            plugin.getStorage().saveQuester(this).get();
        } catch (final Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Get the difference between System.currentTimeMillis() and the last completed time for a quest
     *
     * @param quest The quest to get the last completed time of
     * @return Difference between now and then in milliseconds
     */
    public long getCompletionDifference(final Quest quest) {
        final long currentTime = System.currentTimeMillis();
        final long lastTime;
        if (!completedTimes.containsKey(quest)) {
            lastTime = System.currentTimeMillis();
            completedTimes.put(quest, System.currentTimeMillis());
        } else {
            lastTime = completedTimes.get(quest);
        }
        return currentTime - lastTime;
    }

    /**
     * Get the amount of time left before Quester may take a completed quest again
     *
     * @param quest The quest to calculate the cooldown for
     * @return Length of time in milliseconds
     */
    public long getRemainingCooldown(final Quest quest) {
        return quest.getPlanner().getCooldown() - getCompletionDifference(quest);
    }

    public FileConfiguration getBaseData() {
        final FileConfiguration data = new YamlConfiguration();
        final ArrayList<String> currentQuestIds = new ArrayList<>();
        final ArrayList<Integer> currentQuestStages = new ArrayList<>();
        if (!currentQuests.isEmpty()) {
            for (final Quest quest : currentQuests.keySet()) {
                currentQuestIds.add(quest.getId());
                currentQuestStages.add(currentQuests.get(quest));
            }
            final ConfigurationSection dataSec = data.createSection("questData");
            for (final Quest quest : currentQuests.keySet()) {
                if (quest.getName() == null || quest.getName().isEmpty()) {
                    plugin.getLogger().severe("Quest name was null or empty while loading data");
                    return null;
                }
                final ConfigurationSection questSec = dataSec.createSection(quest.getId());
                final BukkitQuestProgress questProgress = (BukkitQuestProgress) getQuestProgressOrDefault(quest);
                if (questProgress == null) {
                    continue;
                }
                if (!questProgress.blocksBroken.isEmpty()) {
                    questSec.set("blocks-broken-amounts", questProgress.blocksBroken);
                }
                if (!questProgress.blocksDamaged.isEmpty()) {
                    questSec.set("blocks-damaged-amounts", questProgress.blocksDamaged);
                }
                if (!questProgress.blocksPlaced.isEmpty()) {
                    questSec.set("blocks-placed-amounts", questProgress.blocksPlaced);
                }
                if (!questProgress.blocksUsed.isEmpty()) {
                    questSec.set("blocks-used-amounts", questProgress.blocksUsed);
                }
                if (!questProgress.blocksCut.isEmpty()) {
                    questSec.set("blocks-cut-amounts", questProgress.blocksCut);
                }
                if (!questProgress.itemsCrafted.isEmpty()) {
                    questSec.set("item-craft-amounts", questProgress.itemsCrafted);
                }
                if (!questProgress.itemsSmelted.isEmpty()) {
                    questSec.set("item-smelt-amounts", questProgress.itemsSmelted);
                }
                if (!questProgress.itemsEnchanted.isEmpty()) {
                    questSec.set("item-enchant-amounts", questProgress.itemsEnchanted);
                }
                if (!questProgress.itemsBrewed.isEmpty()) {
                    questSec.set("item-brew-amounts", questProgress.itemsBrewed);
                }
                if (!questProgress.itemsConsumed.isEmpty()) {
                    questSec.set("item-consume-amounts", questProgress.itemsConsumed);
                }
                if (!questProgress.itemsDelivered.isEmpty()) {
                    questSec.set("item-delivery-amounts", questProgress.itemsDelivered);
                }
                if (!questProgress.npcsInteracted.isEmpty()) {
                    questSec.set("has-talked-to", questProgress.npcsInteracted);
                }
                if (!questProgress.npcsNumKilled.isEmpty()) {
                    questSec.set("npc-killed-amounts", questProgress.npcsNumKilled);
                }
                if (!questProgress.mobNumKilled.isEmpty()) {
                    questSec.set("mobs-killed-amounts", questProgress.mobNumKilled);
                }
                if (!questProgress.mobsTamed.isEmpty()) {
                    questSec.set("mob-tame-amounts", questProgress.mobsTamed);
                }
                final Stage stage = getCurrentStage(quest);
                if (stage != null) {
                    if (stage.getFishToCatch() != null) {
                        questSec.set("fish-caught", questProgress.getFishCaught());
                    }
                    if (stage.getCowsToMilk() != null) {
                        questSec.set("cows-milked", questProgress.getCowsMilked());
                    }
                    if (stage.getPlayersToKill() != null) {
                        questSec.set("players-killed", questProgress.getPlayersKilled());
                    }
                }
                if (!questProgress.sheepSheared.isEmpty()) {
                    questSec.set("sheep-sheared", questProgress.sheepSheared);
                }
                if (!questProgress.locationsReached.isEmpty()) {
                    questSec.set("has-reached-location", questProgress.locationsReached);
                }
                if (!questProgress.passwordsSaid.isEmpty()) {
                    questSec.set("passwords-said", questProgress.passwordsSaid);
                }
                if (!questProgress.customObjectiveCounts.isEmpty()) {
                    questSec.set("custom-objective-counts", questProgress.customObjectiveCounts);
                }
                if (questProgress.getDelayTimeLeft() > 0) {
                    questSec.set("stage-delay", questProgress.getDelayTimeLeft());
                }
            }
        }
        data.set("currentQuests", currentQuestIds);
        data.set("currentStages", currentQuestStages);
        data.set("quest-points", questPoints);
        if (!completedQuests.isEmpty()) {
            final List<String> questIds = new LinkedList<>();
            for (final Quest quest : completedQuests) {
                questIds.add(quest.getId());
            }
            data.set("completed-Quests", questIds);
        }
        if (!completedTimes.isEmpty()) {
            final List<String> questIds = new LinkedList<>();
            final List<Long> questTimes = new LinkedList<>();
            for (final Entry<Quest, Long> entry : completedTimes.entrySet()) {
                questIds.add(entry.getKey().getId());
                questTimes.add(entry.getValue());
            }
            data.set("completedRedoableQuests", questIds);
            data.set("completedQuestTimes", questTimes);
        }
        if (!amountsCompleted.isEmpty()) {
            final List<String> questIds = new LinkedList<>();
            final List<Integer> questAmounts = new LinkedList<>();
            for (final Entry<Quest, Integer> entry : amountsCompleted.entrySet()) {
                questIds.add(entry.getKey().getId());
                questAmounts.add(entry.getValue());
            }
            data.set("amountsCompletedQuests", questIds);
            data.set("amountsCompleted", questAmounts);
        }
        // #getPlayer is faster
        OfflinePlayer representedPlayer = getPlayer();
        if (representedPlayer == null) {
            representedPlayer = getOfflinePlayer();
        }
        data.set("lastKnownName", representedPlayer.getName());
        return data;
    }

    /**
     * Load data of the Quester from storage
     *
     * @deprecated Use {@link #hasData()}
     * @return true if successful
     */
    @Deprecated
    public boolean loadData() {
        return plugin.getStorage().loadQuester(id) != null;
    }

    /**
     * Check whether the Quester has data saved to hard storage
     *
     * @return true if successful
     */
    public boolean hasData() {
        return plugin.getStorage().loadQuester(id) != null;
    }

    /**
     * Check whether the Quester has base data in memory, indicating they have participated in quests
     *
     * @return false if empty
     */
    public boolean hasBaseData() {
        return !currentQuests.isEmpty() || !questProgress.isEmpty() || !completedQuests.isEmpty();
    }

    /**
     * Initiate the stage timer
     * @param quest The quest of which the timer is for
     */
    public void startStageTimer(final Quest quest) {
        if (getQuestProgressOrDefault(quest).getDelayTimeLeft() > -1) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitStageTimer(plugin, this, quest),
                    (long) (getQuestProgressOrDefault(quest).getDelayTimeLeft() * 0.02));
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitStageTimer(plugin, this, quest),
                    (long) (getCurrentStage(quest).getDelay() * 0.02));
            if (getCurrentStage(quest).getDelayMessage() != null) {
                final Player p = plugin.getServer().getPlayer(id);
                if (p != null) {
                    p.sendMessage(BukkitConfigUtil.parseStringWithPossibleLineBreaks((getCurrentStage(quest)
                            .getDelayMessage()), quest, p));
                }
            }
        }
        getQuestProgressOrDefault(quest).setDelayStartTime(System.currentTimeMillis());
    }

    /**
     * Pause the stage timer. Useful when a player quits
     * @param quest The quest of which the timer is for
     */
    public void stopStageTimer(final Quest quest) {
        if (getQuestProgressOrDefault(quest).getDelayTimeLeft() > -1) {
            getQuestProgressOrDefault(quest).setDelayTimeLeft(getQuestProgressOrDefault(quest).getDelayTimeLeft()
                    - (System.currentTimeMillis() - getQuestProgressOrDefault(quest).getDelayStartTime()));
        } else {
            getQuestProgressOrDefault(quest).setDelayTimeLeft(getCurrentStage(quest).getDelay()
                    - (System.currentTimeMillis() - getQuestProgressOrDefault(quest).getDelayStartTime()));
        }
    }

    /**
     * Get remaining stage delay time
     * @param quest The quest of which the timer is for
     * @return Remaining time in milliseconds
     */
    public long getStageTime(final Quest quest) {
        if (getQuestProgressOrDefault(quest).getDelayTimeLeft() > -1) {
            return getQuestProgressOrDefault(quest).getDelayTimeLeft() - (System.currentTimeMillis()
                    - getQuestProgressOrDefault(quest).getDelayStartTime());
        } else {
            return getCurrentStage(quest).getDelay() - (System.currentTimeMillis()
                    - getQuestProgressOrDefault(quest).getDelayStartTime());
        }
    }

    /**
     * Check whether the provided quest is valid and, if not, inform the Quester
     *
     * @param quest The quest to check
     */
    public void checkQuest(final Quest quest) {
        if (quest != null) {
            boolean exists = false;
            for (final Quest q : plugin.getLoadedQuests()) {
                if (q.getId().equalsIgnoreCase(quest.getId())) {
                    final Stage stage = getCurrentStage(quest);
                    if (stage != null) {
                        quest.updateCompass(this, stage);
                        // TODO - decide whether to handle this
                        /*if (q.equals(quest) == false) {
                            if (getPlayer() != null && getPlayer().isOnline()) {
                                quitQuest(quest, ChatColor.GOLD + Lang.get("questModified")
                                        .replace("<quest>", quest.getName()));
                            }
                        }*/
                    }
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                sendMessage(ChatColor.RED + BukkitLang.get("questNotExist").replace("<quest>", quest.getName()));
            }
        }
    }

    /**
     * Show an inventory GUI with quest items to the specified player
     *
     * @param npc UUID of the NPC from which the GUI is bound
     * @param quests List of quests to use for displaying items
     */
    public void showGUIDisplay(final UUID npc, final LinkedList<Quest> quests) {
        if (npc == null || quests == null) {
            return;
        }
        if (plugin.getDependencies().getCitizens() == null) {
            return;
        }
        final String name = plugin.getDependencies().getNpcName(npc);
        final LinkedList<BukkitQuest> qs = new LinkedList<>();
        for (Quest q : quests) {
            qs.add((BukkitQuest) q);
        }
        final BukkitQuesterPreOpenGUIEvent preEvent = new BukkitQuesterPreOpenGUIEvent(this, npc, qs);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        final Player player = getPlayer();
        final Inventory inv = plugin.getServer().createInventory(player, ((quests.size() / 9) + 1) * 9,
                BukkitLang.get(player, "quests") + " | " + name);
        int i = 0;
        for (final Quest quest : quests) {
            final BukkitQuest bukkitQuest = (BukkitQuest)quest;
            if (bukkitQuest.getGUIDisplay() != null) {
                if (i > 53) {
                    // Protocol-enforced size limit has been exceeded
                    break;
                }
                inv.setItem(i, bukkitQuest.prepareDisplay(this));
                i++;
            }
        }
        player.openInventory(inv);
    }

    /**
     * Force Quester to quit the specified quest (canceling any timers), then update Quest Journal<p>
     *
     * Does not save changes to disk. Consider {@link #quitQuest(Quest, String)} or {@link #quitQuest(Quest, String[])}
     *
     * @param quest The quest to quit
     */
    public void hardQuit(final Quest quest) {
        try {
            if (compassTargetQuestId != null && compassTargetQuestId.equals(quest.getId())) {
                compassTargetQuestId = null;
            }
            currentQuests.remove(quest);
            questProgress.remove(quest);
            if (!timers.isEmpty()) {
                for (final Map.Entry<Integer, Quest> entry : timers.entrySet()) {
                    if (entry.getValue().getName().equals(quest.getName())) {
                        plugin.getServer().getScheduler().cancelTask(entry.getKey());
                        timers.remove(entry.getKey());
                    }
                }
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Forcibly remove quest from Quester's list of completed quests, then update Quest Journal<p>
     *
     * Does not save changes to disk. Consider calling {@link #saveData()} followed by {@link #loadData()}
     *
     * @param quest The quest to remove
     */
    public void hardRemove(final Quest quest) {
        try {
            completedQuests.remove(quest);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Forcibly clear Quester's list of current quests and data, then update Quest Journal<p>
     *
     * Does not save changes to disk. Consider calling {@link #saveData()} followed by {@link #loadData()}
     */
    public void hardClear() {
        try {
            currentQuests.clear();
            questProgress.clear();
            amountsCompleted.clear();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Forcibly set Quester's current stage, then update Quest Journal<br>
     * Does not save changes to disk. Consider calling {@link #saveData()} followed by {@link #loadData()}
     *
     * @param key The quest to set stage of
     * @param val The stage number to set
     */
    public void hardStagePut(final Quest key, final Integer val) {
        try {
            currentQuests.put(key, val);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Forcibly set Quester's quest data, then update Quest Journal<p>
     *
     * Does not save changes to disk. Consider calling {@link #saveData()} followed by {@link #loadData()}
     *
     * @param key The quest to set stage of
     * @param val The data to set
     */
    public void hardDataPut(final Quest key, final QuestProgress val) {
        try {
            questProgress.put(key, (BukkitQuestProgress) val);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean canUseCompass() {
        if (getPlayer() != null) {
            if (!getPlayer().hasPermission("worldedit.navigation.jumpto")) {
                return getPlayer().hasPermission("quests.compass");
            }
        }
        return false;
    }

    /**
     * Reset compass target to Quester's bed spawn location<p>
     *
     * Will set to Quester's spawn location if bed spawn does not exist
     */
    public void resetCompass() {
        compassTargetQuestId = null;
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        if (!canUseCompass()) {
            return;
        }

        Location defaultLocation = player.getBedSpawnLocation();
        if (defaultLocation == null) {
            defaultLocation = player.getWorld().getSpawnLocation();
        }
        player.setCompassTarget(defaultLocation);
    }

    /**
     * Update compass target to current stage of first available current quest, if possible
     */
    public void findCompassTarget() {
        // Here we apply this method to OPs by not checking #canUseCompass
        if (getPlayer() == null || !getPlayer().hasPermission("quests.compass")) {
            return;
        }
        for (final Quest quest : currentQuests.keySet()) {
            final Stage stage = getCurrentStage(quest);
            if (stage != null) {
                if (stage.hasLocatableObjective()) {
                    quest.updateCompass(this, stage);
                } else {
                    resetCompass();
                    setCompassTarget(quest);
                }
                break;
            }
        }
    }

    /**
     * Update compass target to current stage of next available current quest, if possible
     *
     * @param notify Whether to notify this quester of result
     */
    public void findNextCompassTarget(final boolean notify) {
        // Here we apply this method to OPs by not checking #canUseCompass
        if (getPlayer() == null || !getPlayer().hasPermission("quests.compass")) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final LinkedList<String> list = currentQuests.keySet().stream()
                    .sorted(Comparator.comparing(Quest::getName)).map(Quest::getId)
                    .collect(Collectors.toCollection(LinkedList::new));
            int index = 0;
            if (compassTargetQuestId != null) {
                if (!list.contains(compassTargetQuestId) && notify) {
                    return;
                }
                index = list.indexOf(compassTargetQuestId) + 1;
                if (index >= list.size()) {
                    index = 0;
                }
            }
            if (!list.isEmpty()) {
                final Quest quest = plugin.getQuestById(list.get(index));
                compassTargetQuestId = quest.getId();
                final Stage stage = getCurrentStage(quest);
                if (stage != null) {
                    if (stage.hasLocatableObjective()) {
                        quest.updateCompass(BukkitQuester.this, stage);
                        if (notify) {
                            sendMessage(ChatColor.YELLOW + BukkitLang.get(getPlayer(), "compassSet")
                                    .replace("<quest>", quest.getName()));
                        }
                    } else {
                        resetCompass();
                        setCompassTarget(quest);
                        if (notify) {
                            sendMessage(ChatColor.RED + BukkitLang.get(getPlayer(), "compassNone")
                                    .replace("<quest>", quest.getName()));
                        }
                    }
                }
            } else {
                sendMessage(ChatColor.RED + BukkitLang.get(getPlayer(), "journalNoQuests")
                        .replace("<journal>", BukkitLang.get(getPlayer(), "journalTitle")));
            }
        });
    }

    /**
     * Check whether the Quester's inventory contains the specified item
     *
     * @param is The item with a specified amount to check
     * @return true if the inventory contains at least the amount of the specified stack
     */
    public boolean hasItem(final ItemStack is) {
        final Inventory inv = getPlayer().getInventory();
        int playerAmount = 0;
        for (final ItemStack stack : inv.getContents()) {
            if (stack != null) {
                if (BukkitItemUtil.compareItems(is, stack, false) == 0) {
                    playerAmount += stack.getAmount();
                }
            }
        }
        return playerAmount >= is.getAmount();
    }

    /**
     * Dispatch player event to fellow questers<p>
     *
     * @param type The type of objective to progress
     * @param fun The function to execute, the event call
     */
    public Set<String> dispatchMultiplayerEverything(final Quest quest, final ObjectiveType type,
                                                     final BiFunction<Quester, Quest, Void> fun) {
        final Set<String> appliedQuestIDs = new HashSet<>();
        if (quest != null) {
            try {
                if (quest.getOptions().getShareProgressLevel() == 1) {
                    final List<Quester> mq = getMultiplayerQuesters(quest);
                    if (mq == null) {
                        return appliedQuestIDs;
                    }
                    for (final Quester q : mq) {
                        if (q == null) {
                            continue;
                        }
                        if (quest.getOptions().canShareSameQuestOnly()) {
                            if (q.getCurrentStage(quest) != null) {
                                fun.apply(q, quest);
                                appliedQuestIDs.add(quest.getId());
                            }
                        }
                        q.getCurrentQuests().forEach((otherQuest, i) -> {
                            if (otherQuest.getStage(i).containsObjective(type)) {
                                if (!otherQuest.getOptions().canShareSameQuestOnly()) {
                                    fun.apply(q, otherQuest);
                                    appliedQuestIDs.add(otherQuest.getId());
                                }
                            }
                        });
                    }
                }
            } catch (final Exception e) {
                plugin.getLogger().severe("Error occurred while dispatching " + type.name() + " for quest ID "
                        + quest.getId());
                e.printStackTrace();
            }
        }
        return appliedQuestIDs;
    }

    /**
     * Dispatch finish objective to fellow questers
     *
     * @param quest The current quest
     * @param currentStage The current stage of the quest
     * @param fun The function to execute, the event call
     */
    public Set<String> dispatchMultiplayerObjectives(final Quest quest, final Stage currentStage,
                                                     final Function<Quester, Void> fun) {
        final Set<String> appliedQuestIDs = new HashSet<>();
        if (quest.getOptions().getShareProgressLevel() == 2) {
            final List<Quester> mq = getMultiplayerQuesters(quest);
            if (mq == null) {
                return appliedQuestIDs;
            }
            for (final Quester q : mq) {
                if (q == null) {
                    continue;
                }
                if ((q.getCurrentQuests().containsKey(quest) && currentStage.equals(q.getCurrentStage(quest)))) {
                    fun.apply(q);
                }
            }
        }
        return appliedQuestIDs;
    }

    /**
     * Get a list of fellow Questers in a party or group
     *
     * @param quest The quest which uses a linked plugin, i.e. Parties or DungeonsXL
     * @return Potentially empty list of Questers or null for invalid quest
     */
    public List<Quester> getMultiplayerQuesters(final Quest quest) {
        if (quest == null) {
            return null;
        }
        final List<Quester> mq = new LinkedList<>();
        if (plugin.getDependencies().getPartyProvider() != null) {
            final PartyProvider partyProvider = plugin.getDependencies().getPartyProvider();
            if (partyProvider != null) {
                if (quest.getOptions().canUsePartiesPlugin() || quest.getOptions().getExternalPartyPlugin() != null) {
                    if (getUUID() != null && partyProvider.getPartyId(getUUID()) != null) {
                        final String partyId = partyProvider.getPartyId(getUUID());
                        final double distanceSquared = quest.getOptions().getShareDistance()
                                * quest.getOptions().getShareDistance();
                        final boolean offlinePlayers = quest.getOptions().canHandleOfflinePlayers();
                        if (offlinePlayers) {
                            for (final UUID id : partyProvider.getMembers(partyId)) {
                                if (!id.equals(getUUID())) {
                                    mq.add(plugin.getQuester(id));
                                }
                            }
                        } else {
                            for (final UUID id : partyProvider.getOnlineMembers(partyId)) {
                                if (!id.equals(getUUID())) {
                                    if (distanceSquared > 0) {
                                        final Player player = Bukkit.getPlayer(id);
                                        if (player != null) {
                                            final Location locationOne = getPlayer().getLocation();
                                            final Location locationTwo = player.getLocation();
                                            if (locationOne.getWorld() != null && locationTwo.getWorld() != null) {
                                                if (locationOne.getWorld().getName().equals(locationTwo.getWorld()
                                                        .getName())) {
                                                    if (distanceSquared >= getPlayer().getLocation()
                                                            .distanceSquared(player.getLocation())) {
                                                        mq.add(plugin.getQuester(id));
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        mq.add(plugin.getQuester(id));
                                    }
                                }
                            }
                        }
                        if (plugin.getConfigSettings().getConsoleLogging() > 3) {
                            plugin.getLogger().info("Found " + mq.size() + " party members for quest ID "
                                    + quest.getId() + " via Unite plugin");
                        }
                        return mq;
                    }
                }
            }
        } else if (plugin.getDependencies().getPartiesApi() != null) {
            if (quest.getOptions().canUsePartiesPlugin()) {
                final PartyPlayer partyPlayer = plugin.getDependencies().getPartiesApi().getPartyPlayer(getUUID());
                if (partyPlayer != null && partyPlayer.getPartyId() != null) {
                    final Party party = plugin.getDependencies().getPartiesApi().getParty(partyPlayer.getPartyId());
                    if (party != null) {
                        final double distanceSquared = quest.getOptions().getShareDistance()
                                * quest.getOptions().getShareDistance();
                        final boolean offlinePlayers = quest.getOptions().canHandleOfflinePlayers();
                        if (offlinePlayers) {
                            for (final UUID id : party.getMembers()) {
                                if (!id.equals(getUUID())) {
                                    mq.add(plugin.getQuester(id));
                                }
                            }
                        } else {
                            for (final PartyPlayer pp : party.getOnlineMembers(true)) {
                                if (!pp.getPlayerUUID().equals(getUUID())) {
                                    if (distanceSquared > 0) {
                                        final Player player = Bukkit.getPlayer(pp.getPlayerUUID());
                                        if (player != null) {
                                            final Location locationOne = getPlayer().getLocation();
                                            final Location locationTwo = player.getLocation();
                                            if (locationOne.getWorld() != null && locationTwo.getWorld() != null) {
                                                if (locationOne.getWorld().getName().equals(locationTwo.getWorld()
                                                        .getName())) {
                                                    if (distanceSquared >= getPlayer().getLocation()
                                                            .distanceSquared(player.getLocation())) {
                                                        mq.add(plugin.getQuester(pp.getPlayerUUID()));
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        mq.add(plugin.getQuester(pp.getPlayerUUID()));
                                    }
                                }
                            }
                        }
                        if (plugin.getConfigSettings().getConsoleLogging() > 3) {
                            plugin.getLogger().info("Found " + mq.size() + " party members for quest ID "
                                    + quest.getId() + " via Parties plugin");
                        }
                        return mq;
                    }
                }
            }
        }
        return mq;
    }

    /**
     * Whether this Quester meets condition of given quest
     *
     * @param quest The quest to check
     * @return Whether to send Quester reason for failure
     */
    public boolean meetsCondition(final Quest quest, final boolean giveReason) {
        final Stage stage = getCurrentStage(quest);
        if (stage != null && stage.getCondition() != null && !stage.getCondition().check(this, quest)) {
            if (stage.getCondition().isFailQuest()) {
                if (giveReason) {
                    getPlayer().sendMessage(ChatColor.RED + BukkitLang.get(getPlayer(), "conditionFailQuit")
                        .replace("<quest>", quest.getName()));
                }
                if (stage.getFailAction() != null) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                            getCurrentStage(quest).getFailAction().fire(this, quest));
                }
                hardQuit(quest);
            } else if (giveReason) {
                if (System.currentTimeMillis() - lastNotifiedCondition > (plugin.getConfigSettings()
                        .getConditionInterval() * 1000L)) {
                    BukkitActionBarProvider.sendActionBar(getPlayer(), ChatColor.YELLOW + BukkitLang.get(getPlayer(),
                            "conditionFailRetry").replace("<quest>", quest.getName()));
                    lastNotifiedCondition = System.currentTimeMillis();
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Whether this Quester is currently selecting a block in editor
     *
     * @return true if selecting
     */
    public boolean isSelectingBlock() {
        final UUID uuid = getPlayer().getUniqueId();
        return plugin.getQuestFactory().getSelectedBlockStarts().containsKey(uuid)
                || plugin.getQuestFactory().getSelectedKillLocations().containsKey(uuid)
                || plugin.getQuestFactory().getSelectedReachLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedExplosionLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedEffectLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedMobLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedLightningLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedTeleportLocations().containsKey(uuid);
    }

    /**
     * Whether this Quester is in the specified WorldGuard region
     *
     * @return true if in specified WorldGuard region
     */
    public boolean isInRegion(final String regionID) {
        if (getPlayer() == null) {
            return false;
        }
        return plugin.getDependencies().getWorldGuardApi().getApplicableRegionsIDs(getPlayer().getWorld(),
                getPlayer().getLocation()).contains(regionID);
    }

    /**
     * Checks whether an NPC can offer a quest that the player may accept
     *
     * @param npc the giver NPC UUID to check
     * @return true if at least one available quest has not yet been completed
     */
    public boolean canAcceptQuest(final UUID npc) {
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getNpcStart() != null && !getCompletedQuests().contains(q)) {
                if (q.getNpcStart().equals(npc)) {
                    final boolean ignoreLockedQuests = plugin.getConfigSettings().canIgnoreLockedQuests();
                    if (!ignoreLockedQuests || q.testRequirements(this)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether an NPC can offer a quest that the player has already completed
     *
     * @param npc The giver NPC UUID to check
     * @return true if at least one available quest has been completed
     */
    public boolean canAcceptCompletedQuest(final UUID npc) {
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getNpcStart() != null && getCompletedQuests().contains(q)) {
                if (q.getNpcStart().equals(npc)) {
                    final boolean ignoreLockedQuests = plugin.getConfigSettings().canIgnoreLockedQuests();
                    if (!ignoreLockedQuests || q.testRequirements(this)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether an NPC can offer a repeatable quest that the player has already completed
     *
     * @param npc The giver NPC UUID to check
     * @return true if at least one available, redoable quest has been completed
     */
    public boolean canAcceptCompletedRedoableQuest(final UUID npc) {
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getNpcStart() != null && getCompletedQuests().contains(q)
                    && q.getPlanner().getCooldown() > -1) {
                if (q.getNpcStart().equals(npc)) {
                    final boolean ignoreLockedQuests = plugin.getConfigSettings().canIgnoreLockedQuests();
                    if (!ignoreLockedQuests || q.testRequirements(this)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
