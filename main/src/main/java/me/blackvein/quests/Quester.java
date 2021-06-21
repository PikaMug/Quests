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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Crops;

import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;

import de.erethon.dungeonsxl.player.DGroup;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.events.quest.QuestTakeEvent;
import me.blackvein.quests.events.quester.QuesterPostStartQuestEvent;
import me.blackvein.quests.events.quester.QuesterPostUpdateObjectiveEvent;
import me.blackvein.quests.events.quester.QuesterPreOpenGUIEvent;
import me.blackvein.quests.events.quester.QuesterPreStartQuestEvent;
import me.blackvein.quests.events.quester.QuesterPreUpdateObjectiveEvent;
import me.blackvein.quests.item.QuestJournal;
import me.blackvein.quests.storage.Storage;
import me.blackvein.quests.tasks.StageTimer;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.InventoryUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Quester implements Comparable<Quester> {

    private final Quests plugin;
    private UUID id;
    protected String questIdToTake;
    private String lastKnownName;
    protected int questPoints = 0;
    private String compassTargetQuestId;
    private long lastNotifiedCondition = 0L;
    protected ConcurrentHashMap<Integer, Quest> timers = new ConcurrentHashMap<Integer, Quest>();
    protected ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<Quest, Integer>() {

        private static final long serialVersionUID = 6361484975823846780L;

        @Override
        public Integer put(final Quest key, final Integer val) {
            final Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(final Object key) {
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

        /*@Override
        public void add(final int index, final Quest element) {
            super.add(index, element);
            updateJournal();
        }*/
        
        @Override
        public boolean addAll(final Collection<? extends Quest> c) {
            final boolean b = super.addAll(c);
            updateJournal();
            return b;
        }

        /*@Override
        public boolean addAll(final int index, final Collection<? extends Quest> c) {
            final boolean b = super.addAll(index, c);
            updateJournal();
            return b;
        }*/

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

        /*@Override
        public Quest remove(final int index) {
            final Quest s = super.remove(index);
            updateJournal();
            return s;
        }

        @Override
        public Quest set(final int index, final Quest element) {
            final Quest s = super.set(index, element);
            updateJournal();
            return s;
        }*/
    };
    protected ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<Quest, Long>();
    protected ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<Quest, Integer>() {

        private static final long serialVersionUID = 5475202358792520975L;

        @Override
        public Integer put(final Quest key, final Integer val) {
            final Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(final Object key) {
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
    protected ConcurrentHashMap<Quest, QuestData> questData = new ConcurrentHashMap<Quest, QuestData>() {

        private static final long serialVersionUID = -4607112433003926066L;

        @Override
        public QuestData put(final Quest key, final QuestData val) {
            final QuestData data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public QuestData remove(final Object key) {
            final QuestData data = super.remove(key);
            updateJournal();
            return data;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Quest, ? extends QuestData> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    
    /**
     * @deprecated Use {@link #Quester(Quests, UUID)}
     */
    @Deprecated
    public Quester(final Quests plugin) {
        this.plugin = plugin;
    }
    
    public Quester(final Quests plugin, final UUID uuid) {
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

    public UUID getUUID() {
        return id;
    }

    public void setUUID(final UUID id) {
        this.id = id;
    }

    public String getQuestIdToTake() {
        return questIdToTake;
    }

    public void setQuestIdToTake(final String questIdToTake) {
        this.questIdToTake = questIdToTake;
    }
    
    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(final String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }
    
    public int getQuestPoints() {
        return questPoints;
    }

    public void setQuestPoints(final int questPoints) {
        this.questPoints = questPoints;
    }
    
    /**
     * Get compass target quest. Returns null if not set
     * 
     * @return Quest or null
     */
    public Quest getCompassTarget() {
        return compassTargetQuestId != null ? plugin.getQuestById(compassTargetQuestId) : null;
    }
    
    /**
     * Set compass target quest. Does not update in-game
     * 
     * @param quest The target quest
     */
    public void setCompassTarget(final Quest quest) {
        compassTargetQuestId = quest.getId();
    }

    public ConcurrentHashMap<Integer, Quest> getTimers() {
        return timers;
    }

    public void setTimers(final ConcurrentHashMap<Integer, Quest> timers) {
        this.timers = timers;
    }
    
    public void removeTimer(final Integer timerId) {
        this.timers.remove(timerId);
    }

    public ConcurrentHashMap<Quest, Integer> getCurrentQuests() {
        return currentQuests;
    }

    public void setCurrentQuests(final ConcurrentHashMap<Quest, Integer> currentQuests) {
        this.currentQuests = currentQuests;
    }

    public ConcurrentSkipListSet<Quest> getCompletedQuests() {
        return completedQuests;
    }

    public void setCompletedQuests(final ConcurrentSkipListSet<Quest> completedQuests) {
        this.completedQuests = completedQuests;
    }

    public ConcurrentHashMap<Quest, Long> getCompletedTimes() {
        return completedTimes;
    }

    public void setCompletedTimes(final ConcurrentHashMap<Quest, Long> completedTimes) {
        this.completedTimes = completedTimes;
    }

    public ConcurrentHashMap<Quest, Integer> getAmountsCompleted() {
        return amountsCompleted;
    }

    public void setAmountsCompleted(final ConcurrentHashMap<Quest, Integer> amountsCompleted) {
        this.amountsCompleted = amountsCompleted;
    }

    public ConcurrentHashMap<Quest, QuestData> getQuestData() {
        return questData;
    }

    public void setQuestData(final ConcurrentHashMap<Quest, QuestData> questData) {
        this.questData = questData;
    }

    public Player getPlayer() {
        return plugin.getServer().getPlayer(id);
    }

    public OfflinePlayer getOfflinePlayer() {
        return plugin.getServer().getOfflinePlayer(id);
    }
    
    public void sendMessage(final String message) {
        if (getPlayer() == null || !getPlayer().isOnline() || message.trim().isEmpty()) {
            return;
        }
        getPlayer().sendMessage(message);
    }
    
    public Stage getCurrentStage(final Quest quest) {
        if (currentQuests.containsKey(quest)) {
            return quest.getStage(currentQuests.get(quest));
        }
        return null;
    }

    public QuestData getQuestData(final Quest quest) {
        for (final Quest q : questData.keySet()) {
            if (q.getId().equals(quest.getId())) {
                return questData.get(q);
            }
        }
        if (currentQuests.get(quest) != null) {
            addEmptiesFor(quest, currentQuests.get(quest));
        }
        return new QuestData(this);
    }
    
    public boolean hasJournal() {
        return getJournal() != null;
    }
    
    public ItemStack getJournal() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            return null;
        }
        for (final ItemStack is : getPlayer().getInventory().getContents()) {
            if (ItemUtil.isJournal(is)) {
                return is;
            }
        }
        return null;
    }
    
    public int getJournalIndex() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            return -1;
        }
        final ItemStack[] arr = getPlayer().getInventory().getContents();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                if (ItemUtil.isJournal(arr[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

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
            final QuestJournal journal = new QuestJournal(this);
            getPlayer().getInventory().setItem(index, journal.toItemStack());
        }
    }
    
    /**
     * Start a quest for this Quester
     * 
     * @param q The quest to start
     * @param ignoreReqs Whether to ignore Requirements
     */
    @SuppressWarnings("deprecation")
    public void takeQuest(final Quest q, final boolean ignoreReqs) {
        if (q == null) {
            return;
        }
        final QuesterPreStartQuestEvent preEvent = new QuesterPreStartQuestEvent(this, q);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        final OfflinePlayer offlinePlayer = getOfflinePlayer();
        if (offlinePlayer.isOnline()) {
            final Planner pln = q.getPlanner();
            final long currentTime = System.currentTimeMillis();
            final long start = pln.getStartInMillis(); // Start time in milliseconds since UTC epoch
            final long end = pln.getEndInMillis(); // End time in milliseconds since UTC epoch
            final long duration = end - start; // How long the quest can be active for
            final long repeat = pln.getRepeat(); // Length to wait in-between start times
            if (start != -1) {
                if (currentTime < start) {
                    String early = Lang.get("plnTooEarly");
                    early = early.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
                    early = early.replace("<time>", ChatColor.RED
                            + MiscUtil.getTime(start - currentTime) + ChatColor.YELLOW);
                    sendMessage(ChatColor.YELLOW + early);
                    return;
                }
            }
            if (end != -1 && repeat == -1) {
                if (currentTime > end) {
                    String late = Lang.get("plnTooLate");
                    late = late.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.RED);
                    late = late.replace("<time>", ChatColor.DARK_PURPLE
                            + MiscUtil.getTime(currentTime - end) + ChatColor.RED);
                    sendMessage(ChatColor.RED + late);
                    return;
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
                    if (getCompletedTimes().containsKey(q)) {
                        completedTime = getCompletedTimes().get(q);
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
                        }
                    }
                    
                    // If quest is not active, or new period of activity should override player cooldown, inform user
                    if (!active || (q.getPlanner().getOverride() && completedEnd > 0L
                            && currentTime < (completedEnd /*+ repeat*/))) {
                        if (getPlayer().isOnline()) {
                            final String early = Lang.get("plnTooEarly")
                                .replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW)
                                .replace("<time>", ChatColor.DARK_PURPLE
                                + MiscUtil.getTime((completedEnd /*+ repeat*/) - currentTime) + ChatColor.YELLOW);
                            sendMessage(ChatColor.YELLOW + early);
                        }
                        return;
                    }
                }
            }
        }
        if (q.testRequirements(offlinePlayer) == true || ignoreReqs) {
            addEmptiesFor(q, 0);
            try {
                currentQuests.put(q, 0);
                if (plugin.getSettings().getConsoleLogging() > 1) {
                    plugin.getLogger().info(getPlayer().getUniqueId() + " started quest " + q.getName());
                }
            } catch (final NullPointerException npe) {
                plugin.getLogger().severe("Unable to add quest" + q.getName() + " for player " + offlinePlayer.getName()
                        + ". Consider resetting player data or report on Github");
            }
            final Stage stage = q.getStage(0);
            if (!ignoreReqs) {
                final Requirements reqs = q.getRequirements();
                if (reqs.getMoney() > 0) {
                    if (plugin.getDependencies().getVaultEconomy() != null) {
                        plugin.getDependencies().getVaultEconomy().withdrawPlayer(getOfflinePlayer(), reqs.getMoney());
                    }
                }
                if (offlinePlayer.isOnline()) {
                    final Player p = getPlayer();
                    final ItemStack[] original = p.getInventory().getContents().clone();
                    for (final ItemStack is : reqs.getItems()) {
                        if (reqs.getRemoveItems().get(reqs.getItems().indexOf(is)) == true) {
                            if (InventoryUtil.removeItem(p.getInventory(), is) == false) {
                                if (InventoryUtil.stripItem(p.getEquipment(), is) == false) {
                                    p.getInventory().setContents(original);
                                    p.updateInventory();
                                    sendMessage(Lang.get(p, "requirementsItemFail"));
                                    hardQuit(q);
                                    return;
                                }
                            }
                        }
                    }
                    String accepted = Lang.get(getPlayer(), "questAccepted");
                    accepted = accepted.replace("<quest>", q.getName());
                    sendMessage(ChatColor.GREEN + accepted);
                    p.sendMessage("");
                    if (plugin.getSettings().canShowQuestTitles()) {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "title " 
                                + offlinePlayer.getName() + " title " + "{\"text\":\"" + Lang.get(getPlayer(), "quest")
                                + " " + Lang.get(getPlayer(), "accepted") +  "\",\"color\":\"gold\"}");
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "title " 
                                + offlinePlayer.getName() + " subtitle " + "{\"text\":\"" + q.getName() 
                                + "\",\"color\":\"yellow\"}");
                    }
                }
            }
            if (offlinePlayer.isOnline()) {
                final Player p = getPlayer();
                final String title = Lang.get(p, "objectives").replace("<quest>", q.getName());
                sendMessage(ChatColor.GOLD + title);
                plugin.showObjectives(q, this, false);
                final String stageStartMessage = stage.startMessage;
                if (stageStartMessage != null) {
                    p.sendMessage(ConfigUtil.parseStringWithPossibleLineBreaks(stageStartMessage, q, getPlayer()));
                }
                final Condition c = stage.getCondition();
                if (c != null) {
                    sendMessage(ChatColor.LIGHT_PURPLE + Lang.get("stageEditorConditions"));
                    if (!c.getEntitiesWhileRiding().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorRideEntity");
                        for (final String e : c.getEntitiesWhileRiding()) {
                            msg += ChatColor.AQUA + "\n   \u2515 " + e;
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    } else if (!c.getNpcsWhileRiding().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorRideNPC");
                        for (final int i : c.getNpcsWhileRiding()) {
                            if (plugin.getDependencies().getCitizens() != null) {
                                msg += ChatColor.AQUA + "\n   \u2515 " + CitizensAPI.getNPCRegistry().getById(i)
                                        .getName();
                            } else {
                                msg += ChatColor.AQUA + "\n   \u2515 " + i;
                            }
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    } else if (!c.getPermissions().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorPermissions");
                        for (final String e : c.getPermissions()) {
                            msg += ChatColor.AQUA + "\n   \u2515 " + e;
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    } else if (!c.getItemsWhileHoldingMainHand().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorItemsInMainHand");
                        for (final ItemStack is : c.getItemsWhileHoldingMainHand()) {
                            msg += ChatColor.AQUA + "\n   \u2515 " + ItemUtil.getPrettyItemName(is.getType().name());
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    } else if (!c.getWorldsWhileStayingWithin().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorStayWithinWorld");
                        for (final String w : c.getWorldsWhileStayingWithin()) {
                            msg += ChatColor.AQUA + "\n   \u2515 " + w;
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    } else if (!c.getBiomesWhileStayingWithin().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorStayWithinBiome");
                        for (final String b : c.getBiomesWhileStayingWithin()) {
                            msg += ChatColor.AQUA + "\n   \u2515 " + MiscUtil.snakeCaseToUpperCamelCase(b);
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    } else if (!c.getRegionsWhileStayingWithin().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorStayWithinRegion");
                        for (final String r : c.getRegionsWhileStayingWithin()) {
                            msg += ChatColor.AQUA + "\n   \u2515 " + r;
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    } else if (!c.getPlaceholdersCheckIdentifier().isEmpty()) {
                        String msg = "- " + Lang.get("conditionEditorCheckPlaceholder");
                        int index = 0;
                        for (final String r : c.getPlaceholdersCheckIdentifier()) {
                            if (c.getPlaceholdersCheckValue().size() > index) {
                                msg += ChatColor.AQUA + "\n   \u2515 " + r + ChatColor.GRAY + " = " 
                                        + ChatColor.AQUA + c.getPlaceholdersCheckValue().get(index);
                            }
                            index++;
                        }
                        sendMessage(ChatColor.YELLOW + msg);
                    }
                }
            }
            if (stage.chatActions.isEmpty() == false) {
                for (final String chatTrigger : stage.chatActions.keySet()) {
                    questData.get(q).actionFired.put(chatTrigger, false);
                }
            }
            if (stage.commandActions.isEmpty() == false) {
                for (final String commandTrigger : stage.commandActions.keySet()) {
                    questData.get(q).actionFired.put(commandTrigger, false);
                }
            }
            if (q.initialAction != null) {
                q.initialAction.fire(this, q);
            }
            if (stage.startAction != null) {
                stage.startAction.fire(this, q);
            }
            q.updateCompass(this, stage);
            saveData();
        } else {
            if (offlinePlayer.isOnline()) {
                sendMessage(ChatColor.DARK_AQUA + Lang.get("requirements"));
                for (final String s : getCurrentRequirements(q, false)) {
                    sendMessage(ChatColor.GRAY + "- " + s);
                }
            }
        }
        if (offlinePlayer.isOnline()) {
            final QuesterPostStartQuestEvent postEvent = new QuesterPostStartQuestEvent(this, q);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
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
        if (plugin.getSettings().getConsoleLogging() > 1) {
            plugin.getLogger().info(getPlayer().getUniqueId() + " quit quest " + quest.getName());
        }
        for (final String message : messages) {
            if (message != null && !message.equals("") && getPlayer().isOnline()) {
                sendMessage(message);
            }
        }
        saveData();
        //loadData();
        updateJournal();
    }
    
    public LinkedList<String> getCurrentRequirements(final Quest quest, final boolean ignoreOverrides) {
        if (quest == null) {
            return new LinkedList<String>();
        }
        final Requirements reqs = quest.getRequirements();
        if (!ignoreOverrides) {
            if (reqs.getDetailsOverride() != null && !reqs.getDetailsOverride().isEmpty()) {
                final LinkedList<String> requirements = new LinkedList<String>();
                for (final String s : reqs.getDetailsOverride()) {
                    String message = ChatColor.RED + ConfigUtil.parseString(
                            ChatColor.translateAlternateColorCodes('&', s), quest, getPlayer());
                    if (plugin.getDependencies().getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    requirements.add(message);
                    
                }
                return requirements;
            }
        }
        final LinkedList<String> unfinishedRequirements = new LinkedList<String>();
        final LinkedList<String> finishedRequirements = new LinkedList<String>();
        final LinkedList<String> requirements = new LinkedList<String>();
        final OfflinePlayer player = getPlayer();
        if (reqs.getMoney() > 0) {
            final String currency = plugin.getDependencies().getCurrency(reqs.getMoney() == 1 ? false : true);
            if (plugin.getDependencies().getVaultEconomy() != null
                    && plugin.getDependencies().getVaultEconomy().getBalance(player) >= reqs.getMoney()) {
                unfinishedRequirements.add(ChatColor.GREEN + "" + reqs.getMoney() + " " + currency);
            } else {
                finishedRequirements.add(ChatColor.GRAY + "" + reqs.getMoney() + " " + currency);
            }
        }
        if (reqs.getQuestPoints() > 0) {
            if (getQuestPoints() >= reqs.getQuestPoints()) {
                unfinishedRequirements.add(ChatColor.GREEN + "" + reqs.getQuestPoints() + " " 
                        + Lang.get("questPoints"));
            } else {
                finishedRequirements.add(ChatColor.GRAY + "" + reqs.getQuestPoints() + " " + Lang.get("questPoints"));
            }
        }
        for (final Quest q : reqs.getNeededQuests()) {
            if (q != null) {
                if (getCompletedQuests().contains(q)) {
                    finishedRequirements.add(ChatColor.GREEN + q.getName());
                } else {
                    unfinishedRequirements.add(ChatColor.GRAY + q.getName());
                }
            }
        }
        for (final Quest q : reqs.getBlockQuests()) {
            if (q != null) {
                if (completedQuests.contains(q) || currentQuests.containsKey(q)) {
                    requirements.add(ChatColor.RED + quest.getName());
                }
            }
        }
        for (final String s : reqs.getMcmmoSkills()) {
            final SkillType st = Quests.getMcMMOSkill(s);
            final int lvl = reqs.getMcmmoAmounts().get(reqs.getMcmmoSkills().indexOf(s));
            if (UserManager.getOfflinePlayer(player).getProfile().getSkillLevel(st) >= lvl) {
                finishedRequirements.add(ChatColor.GREEN + "" + lvl + " " + s);
            } else {
                unfinishedRequirements.add(ChatColor.GRAY + "" + lvl + " " + s);
            }
        }
        if (reqs.getHeroesPrimaryClass() != null) {
            if (plugin.getDependencies()
                    .testPrimaryHeroesClass(reqs.getHeroesPrimaryClass(), player.getUniqueId())) {
                finishedRequirements.add(ChatColor.GREEN + Lang.get("reqHeroesPrimaryDisplay") + " " 
                    + reqs.getHeroesPrimaryClass());
            } else {
                unfinishedRequirements.add(ChatColor.GRAY + Lang.get("reqHeroesPrimaryDisplay") + " " 
                        + reqs.getHeroesPrimaryClass());
            }
        }
        if (reqs.getHeroesSecondaryClass() != null) {
            if (plugin.getDependencies()
                    .testSecondaryHeroesClass(reqs.getHeroesSecondaryClass(), player.getUniqueId())) {
                finishedRequirements.add(ChatColor.GREEN + Lang.get("reqHeroesSecondaryDisplay") + " " 
                        + reqs.getHeroesSecondaryClass());
            } else {
                finishedRequirements.add(ChatColor.GRAY + Lang.get("reqHeroesSecondaryDisplay") + " " 
                        + reqs.getHeroesSecondaryClass());
            }
        }
        if (player.isOnline()) {
            final Inventory fakeInv = Bukkit.createInventory(null, InventoryType.PLAYER);
            fakeInv.setContents(getPlayer().getInventory().getContents().clone());
            
            int num = 0;
            for (final ItemStack is : reqs.getItems()) {
                if (InventoryUtil.canRemoveItem(fakeInv, is)) {
                    InventoryUtil.removeItem(fakeInv, is);
                    num += is.getAmount();
                }
                if (num >= is.getAmount()) {
                    finishedRequirements.add(ChatColor.GREEN + "" + is.getAmount() + " " + ItemUtil.getName(is));
                } else {
                    unfinishedRequirements.add(ChatColor.GRAY + "" + is.getAmount() + " " + ItemUtil.getName(is));
                }
                num = 0;
            }
            
            for (final String perm :reqs.getPermissions()) {
                if (getPlayer().hasPermission(perm)) {
                    finishedRequirements.add(ChatColor.GREEN + Lang.get("permissionDisplay") + " " + perm);
                } else {
                    unfinishedRequirements.add(ChatColor.GRAY + Lang.get("permissionDisplay") + " " + perm);
                }
                
            }
            for (final Entry<String, Map<String, Object>> m : reqs.getCustomRequirements().entrySet()) {
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getName().equalsIgnoreCase(m.getKey())) {
                        if (cr != null) {
                            if (cr.testRequirement(getPlayer(), m.getValue())) {
                                finishedRequirements.add(ChatColor.GREEN + "" 
                                        + (cr.getDisplay() != null ? cr.getDisplay() : m.getKey()));
                            } else {
                                unfinishedRequirements.add(ChatColor.GRAY + "" 
                                        + (cr.getDisplay() != null ? cr.getDisplay() : m.getKey()));
                            }
                        }
                    }
                }
            } 
        }
        requirements.addAll(unfinishedRequirements);
        requirements.addAll(finishedRequirements);
        return requirements;
    }
    
    /**
     * Get current objectives for a quest, both finished and unfinished
     * 
     * @param quest The quest to get objectives of
     * @param ignoreOverrides Whether to ignore objective-overrides
     * @return List of detailed objectives
     */
    @SuppressWarnings("deprecation")
    public LinkedList<String> getCurrentObjectives(final Quest quest, final boolean ignoreOverrides) {
        if (quest == null) {
            plugin.getLogger().severe("Quest was null when getting objectives for " + getLastKnownName());
            return new LinkedList<String>();
        }
        if (getQuestData(quest) == null) {
            plugin.getLogger().warning("Quest data was null when getting objectives for " + quest.getName());
            return new LinkedList<String>();
        }
        if (getCurrentStage(quest) == null) {
            //plugin.getLogger().warning("Current stage was null when getting objectives for " + quest.getName());
            return new LinkedList<String>();
        }
        final Dependencies depends = plugin.getDependencies();
        if (!ignoreOverrides && !getCurrentStage(quest).objectiveOverrides.isEmpty()) {
            final LinkedList<String> objectives = new LinkedList<String>();
            for (final String s: getCurrentStage(quest).objectiveOverrides) {
                String message = ChatColor.GREEN + ConfigUtil.parseString(
                        ChatColor.translateAlternateColorCodes('&', s), quest, getPlayer());
                if (depends.getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                }
                objectives.add(message);
            }
            return objectives;
        }
        final QuestData data = getQuestData(quest);
        final Stage stage = getCurrentStage(quest);
        final LinkedList<String> objectives = new LinkedList<String>();
        for (final ItemStack e : stage.blocksToBreak) {
            for (final ItemStack e2 : data.blocksBroken) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + Lang.get(getPlayer(), "break");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    objectives.add(message.replace("<item>", ItemUtil.getName(e2)));
                }
            }
        }
        for (final ItemStack e : stage.blocksToDamage) {
            for (final ItemStack e2 : data.blocksDamaged) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + Lang.get(getPlayer(), "damage");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    objectives.add(message.replace("<item>", ItemUtil.getName(e2)));
                }
            }
        }
        for (final ItemStack e : stage.blocksToPlace) {
            for (final ItemStack e2 : data.blocksPlaced) {
                final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                String message = color + Lang.get(getPlayer(), "place");
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    objectives.add(message.replace("<item>", ItemUtil.getName(e2)));
                }
            }
        }
        for (final ItemStack e : stage.blocksToUse) {
            for (final ItemStack e2 : data.blocksUsed) {
                final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                String message = color + Lang.get(getPlayer(), "use");
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    objectives.add(message.replace("<item>", ItemUtil.getName(e2)));
                }
            }
        }
        for (final ItemStack e : stage.blocksToCut) {
            for (final ItemStack e2 : data.blocksCut) {
                final ChatColor color = e2.getAmount() < e.getAmount() ? ChatColor.GREEN : ChatColor.GRAY;
                String message = color + Lang.get(getPlayer(), "cut");
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        // Legacy
                        message += " <item>" + color + ": " + e2.getAmount() + "/" + e.getAmount();
                    }
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    objectives.add(message.replace("<item>", ItemUtil.getName(e2)));
                }
            }
        }
        int craftIndex = 0;
        for (final ItemStack is : stage.itemsToCraft) {
            int crafted = 0;
            if (data.itemsConsumed.size() > craftIndex) {
                crafted = data.itemsConsumed.get(craftIndex).getAmount();
            }
            final int toCraft = is.getAmount();
            craftIndex++;
            final ChatColor color = crafted < toCraft ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "craftItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + crafted + "/" + toCraft);
            } else {
                // Legacy
                message += color + ": " + crafted + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message.replace("<item>", ItemUtil.getName(is)));
        }
        int smeltIndex = 0;
        for (final ItemStack is : stage.itemsToSmelt) {
            int smelted = 0;
            if (data.itemsConsumed.size() > smeltIndex) {
                smelted = data.itemsConsumed.get(smeltIndex).getAmount();
            }
            final int toSmelt = is.getAmount();
            smeltIndex++;
            final ChatColor color = smelted < toSmelt ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "smeltItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + smelted + "/" + toSmelt);
            } else {
                // Legacy
                message += color + ": " + smelted + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message.replace("<item>", ItemUtil.getName(is)));
        }
        int enchantIndex = 0;
        for (final ItemStack is : stage.itemsToEnchant) {
            int enchanted = 0;
            if (data.itemsConsumed.size() > enchantIndex) {
                enchanted = data.itemsConsumed.get(enchantIndex).getAmount();
            }
            final int toEnchant = is.getAmount();
            enchantIndex++;
            final ChatColor color = enchanted < toEnchant ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "enchItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + enchanted + "/" + toEnchant);
            } else {
                // Legacy
                message += color + ": " + enchanted + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            if (is.getEnchantments().isEmpty()) {
                objectives.add(message.replace("<item>", ItemUtil.getName(is))
                        .replace("<enchantment>", "")
                        .replace("<level>", "")
                        .replaceAll("\\s+", " "));
            } else {
                for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                    objectives.add(message.replace("<item>", ItemUtil.getName(is))
                            .replace("<enchantment>", ItemUtil.getPrettyEnchantmentName(e.getKey()))
                            .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                }
            }
        }
        int brewIndex = 0;
        for (final ItemStack is : stage.itemsToBrew) {
            int brewed = 0;
            if (data.itemsConsumed.size() > brewIndex) {
                brewed = data.itemsConsumed.get(brewIndex).getAmount();
            }
            final int toBrew = is.getAmount();
            brewIndex++;
            final ChatColor color = brewed < toBrew ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "brewItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + brewed + "/" + toBrew);
            } else {
                // Legacy
                message += color + ": " + brewed + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message.replace("<item>", ItemUtil.getName(is)));
        }
        int consumeIndex = 0;
        for (final ItemStack is : stage.itemsToConsume) {
            int consumed = 0;
            if (data.itemsConsumed.size() > consumeIndex) {
                consumed = data.itemsConsumed.get(consumeIndex).getAmount();
            }
            final int toConsume = is.getAmount();
            consumeIndex++;
            final ChatColor color = consumed < toConsume ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "consumeItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + consumed + "/" + toConsume);
            } else {
                // Legacy
                message += color + ": " + consumed + "/" + is.getAmount();
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message.replace("<item>", ItemUtil.getName(is)));
        }
        int deliverIndex = 0;
        for (final ItemStack is : stage.itemsToDeliver) {
            int delivered = 0;
            if (data.itemsDelivered.size() > deliverIndex) {
                delivered = data.itemsDelivered.get(deliverIndex).getAmount();
            }
            final int toDeliver = is.getAmount();
            final Integer npc = stage.itemDeliveryTargets.get(deliverIndex);
            deliverIndex++;
            final ChatColor color = delivered < toDeliver ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "deliver").replace("<npc>", depends.getNPCName(npc));
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + delivered + "/" + toDeliver);
            } else {
                // Legacy
                message += color + ": " + delivered + "/" + toDeliver;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message.replace("<item>", ItemUtil.getName(is)));
        }
        for (final Integer n : stage.citizensToInteract) {
            for (final Entry<Integer, Boolean> e : getQuestData(quest).citizensInteracted.entrySet()) {
                if (e.getKey().equals(n)) {
                    final ChatColor color = e.getValue() == false ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + Lang.get(getPlayer(), "talkTo")
                            .replace("<npc>", depends.getNPCName(n));
                    if (depends.getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    objectives.add(message);
                }
            }
        }
        for (final Integer n : getCurrentStage(quest).citizensToKill) {
            for (final Integer n2 : getQuestData(quest).citizensKilled) {
                if (n.equals(n2)) {
                    if (data.citizenNumKilled.size() > data.citizensKilled.indexOf(n2) 
                            && stage.citizenNumToKill.size() > stage.citizensToKill.indexOf(n)) {
                        final ChatColor color = data.citizenNumKilled.get(data.citizensKilled.indexOf(n2)) 
                                < stage.citizenNumToKill.get(stage.citizensToKill.indexOf(n)) 
                                ? ChatColor.GREEN : ChatColor.GRAY;
                        String message = color + Lang.get(getPlayer(), "kill");
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
                            message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                        }
                        objectives.add(message);
                    }
                }
            }
        }
        for (final EntityType e : getCurrentStage(quest).mobsToKill) {
            for (final EntityType e2 : getQuestData(quest).mobsKilled) {
                if (e == e2) {
                    if (data.mobNumKilled.size() > data.mobsKilled.indexOf(e2) 
                            && stage.mobNumToKill.size() > stage.mobsToKill.indexOf(e)) {
                        final ChatColor color = data.mobNumKilled.get(data.mobsKilled.indexOf(e2)) 
                                < stage.mobNumToKill.get(stage.mobsToKill.indexOf(e)) 
                                ? ChatColor.GREEN : ChatColor.GRAY;
                        String message = color + "";
                        if (stage.locationsToKillWithin.isEmpty()) {
                            message += Lang.get(getPlayer(), "kill");
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
                            message += Lang.get(getPlayer(), "killAtLocation").replace("<location>", 
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
                            message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                        }
                        objectives.add(message.replace("<mob>", MiscUtil.getProperMobName(e)));
                    }
                }
            }
        }
        for (final Entry<EntityType, Integer> e : getCurrentStage(quest).mobsToTame.entrySet()) {
            for (final Entry<EntityType, Integer> e2 : getQuestData(quest).mobsTamed.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    final ChatColor color = e2.getValue() < e.getValue() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + Lang.get(getPlayer(), "tame");
                    if (!message.contains("<mob>")) {
                        message += " <mob>";
                    }
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getValue() + "/" + e.getValue());
                    } else {
                        // Legacy
                        message += color + ": " + e2.getValue() + "/" + e.getValue();
                    }
                    objectives.add(message.replace("<mob>", MiscUtil.getProperMobName(e.getKey())));
                }
            }
        }
        if (getCurrentStage(quest).fishToCatch != null) {
            final ChatColor color = data.getFishCaught() < stage.fishToCatch ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "catchFish");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + data.getFishCaught() + "/" + stage.fishToCatch);
            } else {
                // Legacy
                message += color + ": " + data.getFishCaught() + "/" + stage.fishToCatch;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message);
        }
        if (stage.cowsToMilk != null) {
            final ChatColor color = data.getCowsMilked() < stage.cowsToMilk ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "milkCow");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + data.getCowsMilked() + "/" + stage.cowsToMilk);
            } else {
                // Legacy
                message += color + ": " + data.getCowsMilked() + "/" + stage.cowsToMilk;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message);
        }
        for (final Entry<DyeColor, Integer> e : getCurrentStage(quest).sheepToShear.entrySet()) {
            for (final Entry<DyeColor, Integer> e2 : getQuestData(quest).sheepSheared.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    final ChatColor color = e2.getValue() < e.getValue() ? ChatColor.GREEN : ChatColor.GRAY;
                    String message = color + Lang.get(getPlayer(), "shearSheep");
                    message = message.replace("<color>", MiscUtil.getPrettyDyeColorName(e.getKey()));
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + e2.getValue() + "/" + e.getValue());
                    } else {
                        // Legacy
                        message += color + ": " + e2.getValue() + "/" + e.getValue();
                    }
                    objectives.add(message);
                }
            }
        }
        if (stage.playersToKill != null) {
            final ChatColor color = data.getPlayersKilled() < stage.playersToKill ? ChatColor.GREEN : ChatColor.GRAY;
            String message = color + Lang.get(getPlayer(), "killPlayer");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + color + data.getPlayersKilled() + "/" + stage.playersToKill);
            } else {
                // Legacy
                message += color + ": " + data.getPlayersKilled() + "/" + stage.playersToKill;
            }
            if (depends.getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
            }
            objectives.add(message);
        }
        for (int i = 0 ; i < getCurrentStage(quest).locationsToReach.size(); i++) {
            if (i < data.hasReached.size()) {
                final ChatColor color = data.hasReached.get(i) == false ? ChatColor.GREEN : ChatColor.GRAY;
                String message = color + Lang.get(getPlayer(), "goTo");
                message = message.replace("<location>", stage.locationNames.get(i));
                objectives.add(message);
            }
        }
        for (final String s : getCurrentStage(quest).passwordDisplays) {
            if (data.passwordsSaid.containsKey(s)) {
                final Boolean b = data.passwordsSaid.get(s);
                final ChatColor color = b != null && b == false ? ChatColor.GREEN : ChatColor.GRAY;
                objectives.add(color + s);
            }
        }
        for (final CustomObjective co : getCurrentStage(quest).customObjectives) {
            int countsIndex = 0;
            String display = co.getDisplay();
            final List<String> unfinished = new LinkedList<String>();
            final List<String> finished = new LinkedList<String>();
            for (final Entry<String, Integer> entry : getQuestData(quest).customObjectiveCounts.entrySet()) {
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
                            plugin.getLogger().severe("Unable to fetch display for " + co.getName() + " on " 
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
                objectives.add(ChatColor.GREEN + s);
            }
            for (final String s : finished) {
                objectives.add(ChatColor.GRAY + s);
            }
        }
        return objectives;
    }
    
    /**
     * Get current objectives for a quest, both finished and unfinished
     * 
     * @deprecated Use {@link #getCurrentObjectives(Quest, boolean)}
     * @param quest The quest to get objectives of
     * @param ignoreOverrides Whether to ignore objective-overrides
     * @return List of detailed objectives
     */
    @Deprecated
    public LinkedList<String> getObjectives(final Quest quest, final boolean ignoreOverrides) {
        return getCurrentObjectives(quest, ignoreOverrides);
    }
    
    /**
     * Check if player's current stage has the specified objective<p>
     * 
     * Accepted strings are: breakBlock, damageBlock, placeBlock, useBlock,
     * cutBlock, craftItem, smeltItem, enchantItem, brewItem, consumeItem,
     * milkCow, catchFish, killMob, deliverItem, killPlayer, talkToNPC,
     * killNPC, tameMob, shearSheep, password, reachLocation
     * 
     * @deprecated Use {@link Stage#containsObjective(ObjectiveType)}
     * @param quest The quest to check objectives of
     * @param s The type of objective to check for
     * @return true if quest contains specified objective
     */
    @Deprecated
    public boolean containsObjective(final Quest quest, final String s) {
        if (quest == null || getCurrentStage(quest) == null) {
            return false;
        }
        return getCurrentStage(quest).containsObjective(ObjectiveType.fromName(s));
    }

    public boolean hasCustomObjective(final Quest quest, final String s) {
        if (getQuestData(quest) == null) {
            return false;
        }
        if (getQuestData(quest).customObjectiveCounts.containsKey(s)) {
            final int count = getQuestData(quest).customObjectiveCounts.get(s);
            int index = -1;
            for (int i = 0; i < getCurrentStage(quest).customObjectives.size(); i++) {
                if (getCurrentStage(quest).customObjectives.get(i).getName().equals(s)) {
                    index = i;
                    break;
                }
            }
            final int count2 = getCurrentStage(quest).customObjectiveCounts.get(index);
            return count <= count2;
        }
        return false;
    }
    
    /**
     * Mark block as broken if Quester has such an objective
     * 
     * @param quest The quest for which the block is being broken
     * @param m The block being broken
     */
    @SuppressWarnings("deprecation")
    public void breakBlock(final Quest quest, final ItemStack m) {
        final ItemStack temp = m;
        temp.setAmount(0);
        ItemStack broken = temp;
        ItemStack toBreak = temp;
        for (final ItemStack is : getQuestData(quest).blocksBroken) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        broken = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        broken = is;
                    }
                } else if (m.getData() instanceof Crops && is.getData() instanceof Crops) {
                    if (is.getDurability() > 0) {
                        // Age is specified so check for durability
                        if (m.getDurability() == is.getDurability()) {
                            broken = is;
                        }
                    } else {
                        // Age is unspecified so ignore durability
                        broken = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        broken = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    broken = is;
                }
            }
        }
        for (final ItemStack is : getCurrentStage(quest).blocksToBreak) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toBreak = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        toBreak = is;
                    }
                } else if (m.getData() instanceof Crops && is.getData() instanceof Crops) {
                    if (is.getDurability() > 0) {
                        // Age is specified so check for durability
                        if (m.getDurability() == is.getDurability()) {
                            toBreak = is;
                        }
                    } else {
                        // Age is unspecified so ignore durability
                        toBreak = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toBreak = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    toBreak = is;
                }
            }
        }
        
        final ObjectiveType type = ObjectiveType.BREAK_BLOCK;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, broken.getAmount(), toBreak.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final ItemStack newBroken = broken;
        if (broken.getAmount() < toBreak.getAmount()) {
            newBroken.setAmount(broken.getAmount() + 1);
            if (getQuestData(quest).blocksBroken.contains(broken)) {
                getQuestData(quest).blocksBroken.set(getQuestData(quest).blocksBroken.indexOf(broken), newBroken);
                if (broken.getAmount() == toBreak.getAmount()) {
                    finishObjective(quest, new Objective(type, m, toBreak), null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalBroken = broken;
                    final ItemStack finalToBreak = toBreak;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).blocksBroken.set(getQuestData(quest).blocksBroken
                                .indexOf(finalBroken), newBroken);
                        q.finishObjective(quest, new Objective(type, m, finalToBreak), null, null, null, null, null,
                                null, null);
                        return null;
                    });
                }
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newBroken.getAmount(), toBreak.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }
    
    /**
     * Mark block as damaged if Quester has such an objective
     * 
     * @param quest The quest for which the block is being damaged
     * @param m The block being damaged
     */
    @SuppressWarnings("deprecation")
    public void damageBlock(final Quest quest, final ItemStack m) {
        final ItemStack temp = m;
        temp.setAmount(0);
        ItemStack damaged = temp;
        ItemStack toDamage = temp;
        for (final ItemStack is : getQuestData(quest).blocksDamaged) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        damaged = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        damaged = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        damaged = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    damaged = is;
                }
            }
        }
        for (final ItemStack is : getCurrentStage(quest).blocksToDamage) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toDamage = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        toDamage = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toDamage = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    toDamage = is;
                }
            }
        }
        
        final ObjectiveType type = ObjectiveType.DAMAGE_BLOCK;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, damaged.getAmount(), toDamage.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final ItemStack newDamaged = damaged;
        if (damaged.getAmount() < toDamage.getAmount()) {
            
            newDamaged.setAmount(damaged.getAmount() + 1);
            if (getQuestData(quest).blocksDamaged.contains(damaged)) {
                getQuestData(quest).blocksDamaged.set(getQuestData(quest).blocksDamaged.indexOf(damaged), newDamaged);
                if (damaged.getAmount() == toDamage.getAmount()) {
                    finishObjective(quest, new Objective(type, m, toDamage), null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalDamaged = damaged;
                    final ItemStack finalToDamage = toDamage;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).blocksDamaged.set(getQuestData(quest).blocksDamaged
                                .indexOf(finalDamaged), newDamaged);
                        q.finishObjective(quest, new Objective(type, m, finalToDamage), null, null, null, null, null,
                                null, null);
                        return null;
                    });
                }
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newDamaged.getAmount(), toDamage.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark block as placed if Quester has such an objective
     * 
     * @param quest The quest for which the block is being placed
     * @param m The block being placed
     */
    @SuppressWarnings("deprecation")
    public void placeBlock(final Quest quest, final ItemStack m) {
        final ItemStack temp = m;
        temp.setAmount(0);
        ItemStack placed = temp;
        ItemStack toPlace = temp;
        for (final ItemStack is : getQuestData(quest).blocksPlaced) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        placed = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        placed = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        placed = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    placed = is;
                }
            }
        }
        for (final ItemStack is : getCurrentStage(quest).blocksToPlace) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toPlace = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        toPlace = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toPlace = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    toPlace = is;
                }
            }
        }
        
        final ObjectiveType type = ObjectiveType.PLACE_BLOCK;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, placed.getAmount(), toPlace.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final ItemStack newPlaced = placed;
        if (placed.getAmount() < toPlace.getAmount()) {
            newPlaced.setAmount(placed.getAmount() + 1);
            if (getQuestData(quest).blocksPlaced.contains(placed)) {
                getQuestData(quest).blocksPlaced.set(getQuestData(quest).blocksPlaced.indexOf(placed), newPlaced);
                if (placed.getAmount() == toPlace.getAmount()) {
                    finishObjective(quest, new Objective(type, m, toPlace), null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalPlaced = placed;
                    final ItemStack finalToPlace = toPlace;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).blocksPlaced.set(getQuestData(quest).blocksPlaced
                                .indexOf(finalPlaced), newPlaced);
                        q.finishObjective(quest, new Objective(type, m, finalToPlace), null, null, null, null, null,
                                null, null);
                        return null;
                    });
                }
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newPlaced.getAmount(), toPlace.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark block as used if Quester has such an objective
     * 
     * @param quest The quest for which the block is being used
     * @param m The block being used
     */
    @SuppressWarnings("deprecation")
    public void useBlock(final Quest quest, final ItemStack m) {
        final ItemStack temp = m;
        temp.setAmount(0);
        ItemStack used = temp;
        ItemStack toUse = temp;
        for (final ItemStack is : getQuestData(quest).blocksUsed) {
            if (m.getType() == is.getType() ) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        used = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        used = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        used = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    used = is;
                }
            }
        }
        for (final ItemStack is : getCurrentStage(quest).blocksToUse) {
            if (m.getType() == is.getType() ) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid, so check durability
                    if (m.getDurability() == is.getDurability()) {
                        toUse = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        toUse = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toUse = is;
                    }
                } else {
                    // Blocks are not solid, so ignore durability
                    toUse = is;
                }
            }
        }
        
        final ObjectiveType type = ObjectiveType.USE_BLOCK;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, used.getAmount(), toUse.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final ItemStack newUsed = used;
        if (used.getAmount() < toUse.getAmount()) {
            newUsed.setAmount(used.getAmount() + 1);
            if (getQuestData(quest).blocksUsed.contains(used)) {
                getQuestData(quest).blocksUsed.set(getQuestData(quest).blocksUsed.indexOf(used), newUsed);
                if (used.getAmount() == toUse.getAmount()) {
                    finishObjective(quest, new Objective(type, m, toUse), null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalUsed = used;
                    final ItemStack finalToUse = toUse;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).blocksUsed.set(getQuestData(quest).blocksUsed
                                .indexOf(finalUsed), newUsed);
                        q.finishObjective(quest, new Objective(type, m, finalToUse), null, null, null, null, null, null,
                                null);
                        return null;
                    });
                }
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newUsed.getAmount(), toUse.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark block as cut if Quester has such an objective
     * 
     * @param quest The quest for which the block is being cut
     * @param m The block being cut
     */
    @SuppressWarnings("deprecation")
    public void cutBlock(final Quest quest, final ItemStack m) {
        final ItemStack temp = m;
        temp.setAmount(0);
        ItemStack cut = temp;
        ItemStack toCut = temp;
        for (final ItemStack is : getQuestData(quest).blocksCut) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        cut = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        cut = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        cut = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    cut = is;
                }
            }
        }
        for (final ItemStack is : getCurrentStage(quest).blocksToCut) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    // Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toCut = is;
                    } else if (!plugin.getLocaleManager().isBelow113()) {
                        // Ignore durability for 1.13+
                        toCut = is;
                    }
                } else if (m.getType().name().equals("RED_ROSE")) {
                    // Flowers are unique so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toCut = is;
                    }
                } else {
                    // Blocks are not solid so ignore durability
                    toCut = is;
                }
            }
        }
        
        final ObjectiveType type = ObjectiveType.CUT_BLOCK;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, cut.getAmount(), toCut.getAmount()));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final ItemStack newCut = cut;
        if (cut.getAmount() < toCut.getAmount()) {
            newCut.setAmount(cut.getAmount() + 1);
            if (getQuestData(quest).blocksCut.contains(cut)) {
                getQuestData(quest).blocksCut.set(getQuestData(quest).blocksCut.indexOf(cut), newCut);
                if (cut.getAmount() == toCut.getAmount()) {
                    finishObjective(quest, new Objective(type, m, toCut), null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalCut = cut;
                    final ItemStack finalToCut = toCut;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).blocksCut.set(getQuestData(quest).blocksCut.indexOf(finalCut), newCut);
                        q.finishObjective(quest, new Objective(type, m, finalToCut), null, null, null, null, null, null,
                                null);
                        return null;
                    });
                }
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newCut.getAmount(), toCut.getAmount()));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }
    
    /**
     * Mark item as crafted if Quester has such an objective
     * 
     * @param quest The quest for which the item is being crafted
     * @param i The item being crafted
     */
    public void craftItem(final Quest quest, final ItemStack i) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<Integer>();
        for (final ItemStack is : getQuestData(quest).itemsCrafted) {
            currentIndex++;
            if (ItemUtil.compareItems(i, is, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final LinkedList<ItemStack> items = new LinkedList<ItemStack>(getQuestData(quest).itemsCrafted);
            final ItemStack found = items.get(match);
            final int amount = found.getAmount();
            final int toCraft = getCurrentStage(quest).itemsToCraft.get(match).getAmount();

            final ObjectiveType type = ObjectiveType.CRAFT_ITEM;
            final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest,
                    new Objective(type, amount, toCraft));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int newAmount = i.getAmount() + amount;
            final Material m = i.getType();
            if (amount < toCraft) {
                if (newAmount >= toCraft) {
                    final ItemStack newStack = found;
                    found.setAmount(toCraft);
                    getQuestData(quest).itemsCrafted.set(items.indexOf(found), newStack);
                    finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null, null,
                            null, null, null);

                    // Multiplayer
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).itemsCrafted.set(items.indexOf(found), newStack);
                        q.finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null,
                                null, null, null, null);
                        return null;
                    });
                } else {
                    final ItemStack newStack = found;
                    found.setAmount(newAmount);
                    getQuestData(quest).itemsCrafted.set(items.indexOf(found), newStack);
                }
                return;
            }

            final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest,
                    new Objective(type, newAmount, toCraft));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }
    
    /**
     * Mark item as smelted if Quester has such an objective
     * 
     * @param quest The quest for which the item is being smelted
     * @param i The item being smelted
     */
    public void smeltItem(final Quest quest, final ItemStack i) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<Integer>();
        for (final ItemStack is : getQuestData(quest).itemsSmelted) {
            currentIndex++;
            if (ItemUtil.compareItems(i, is, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final LinkedList<ItemStack> items = new LinkedList<ItemStack>(getQuestData(quest).itemsSmelted);
            final ItemStack found = items.get(match);
            final int amount = found.getAmount();
            final int toSmelt = getCurrentStage(quest).itemsToSmelt.get(match).getAmount();

            final ObjectiveType type = ObjectiveType.SMELT_ITEM;
            final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest,
                    new Objective(type, amount, toSmelt));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int newAmount = i.getAmount() + amount;
            final Material m = i.getType();
            if (amount < toSmelt) {
                if (newAmount >= toSmelt) {
                    final ItemStack newStack = found;
                    found.setAmount(toSmelt);
                    getQuestData(quest).itemsSmelted.set(items.indexOf(found), newStack);
                    finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null, null,
                            null, null, null);

                    // Multiplayer
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).itemsSmelted.set(items.indexOf(found), newStack);
                        q.finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null,
                                null, null, null, null);
                        return null;
                    });
                } else {
                    final ItemStack newStack = found;
                    found.setAmount(newAmount);
                    getQuestData(quest).itemsSmelted.set(items.indexOf(found), newStack);
                }
                return;
            }

            final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest,
                    new Objective(type, newAmount, toSmelt));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark item as enchanted if Quester has such an objective
     * 
     * @param quest The quest for which the item is being enchanted
     * @param i The item being enchanted
     */
    public void enchantItem(final Quest quest, final ItemStack i) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<Integer>();
        for (final ItemStack is : getQuestData(quest).itemsEnchanted) {
            currentIndex++;
            if (!is.getEnchantments().isEmpty()) {
                if (ItemUtil.compareItems(i, is, true) == 0) {
                    matches.add(currentIndex);
                }
            } else {
                if (ItemUtil.compareItems(i, is, true) == -4) {
                    matches.add(currentIndex);
                }
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final LinkedList<ItemStack> items = new LinkedList<ItemStack>(getQuestData(quest).itemsEnchanted);
            final ItemStack found = items.get(match);
            final int amount = found.getAmount();
            final int toEnchant = getCurrentStage(quest).itemsToEnchant.get(match).getAmount();

            final ObjectiveType type = ObjectiveType.ENCHANT_ITEM;
            final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest,
                    new Objective(type, amount, toEnchant));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int newAmount = i.getAmount() + amount;
            final Material m = i.getType();
            if (amount < toEnchant) {
                if (newAmount >= toEnchant) {
                    final ItemStack newStack = found;
                    found.setAmount(toEnchant);
                    getQuestData(quest).itemsEnchanted.set(items.indexOf(found), newStack);
                    finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null, null,
                            null, null, null);

                    // Multiplayer
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).itemsEnchanted.set(items.indexOf(found), newStack);
                        q.finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null,
                                null, null, null, null);
                        return null;
                    });
                } else {
                    final ItemStack newStack = found;
                    found.setAmount(newAmount);
                    getQuestData(quest).itemsEnchanted.set(items.indexOf(found), newStack);
                }
                return;
            }

            final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest,
                    new Objective(type, i.getAmount() + amount, toEnchant));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }
    
    /**
     * Mark item as brewed if Quester has such an objective
     * 
     * @param quest The quest for which the item is being brewed
     * @param i The item being brewed
     */
    public void brewItem(final Quest quest, final ItemStack i) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<Integer>();
        for (final ItemStack is : getQuestData(quest).itemsBrewed) {
            currentIndex++;
            if (ItemUtil.compareItems(i, is, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final LinkedList<ItemStack> items = new LinkedList<ItemStack>(getQuestData(quest).itemsBrewed);
            final ItemStack found = items.get(match);
            final int amount = found.getAmount();
            final int toBrew = getCurrentStage(quest).itemsToBrew.get(match).getAmount();

            final ObjectiveType type = ObjectiveType.BREW_ITEM;
            final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest,
                    new Objective(type, amount, toBrew));
            plugin.getServer().getPluginManager().callEvent(preEvent);

            final int newAmount = i.getAmount() + amount;
            final Material m = i.getType();
            if (amount < toBrew) {
                if (newAmount >= toBrew) {
                    final ItemStack newStack = found;
                    found.setAmount(toBrew);
                    getQuestData(quest).itemsBrewed.set(items.indexOf(found), newStack);
                    finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null, null,
                            null, null, null);

                    // Multiplayer
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).itemsBrewed.set(items.indexOf(found), newStack);
                        q.finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null,
                                null, null, null, null);
                        return null;
                    });
                } else {
                    final ItemStack newStack = found;
                    found.setAmount(newAmount);
                    getQuestData(quest).itemsBrewed.set(items.indexOf(found), newStack);
                }
                return;
            }

            final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest,
                    new Objective(type, newAmount, toBrew));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }
    
    /**
     * Mark item as consumed if Quester has such an objective
     * 
     * @param quest The quest for which the item is being consumed
     * @param i The item being consumed
     */
    public void consumeItem(final Quest quest, final ItemStack i) {
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<Integer>();
        for (final ItemStack is : getQuestData(quest).itemsConsumed) {
            currentIndex++;
            if (ItemUtil.compareItems(i, is, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        for (final Integer match : matches) {
            final LinkedList<ItemStack> items = new LinkedList<ItemStack>(getQuestData(quest).itemsConsumed);
            final ItemStack found = items.get(match);
            final int amount = found.getAmount();
            final int toConsume = getCurrentStage(quest).itemsToConsume.get(match).getAmount();
            
            final ObjectiveType type = ObjectiveType.CONSUME_ITEM;
            final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                    new Objective(type, amount, toConsume));
            plugin.getServer().getPluginManager().callEvent(preEvent);
            
            final int newAmount = i.getAmount() + amount;
            final Material m = i.getType();
            if (amount < toConsume) {
                if (newAmount >= toConsume) {
                    final ItemStack newStack = found;
                    found.setAmount(toConsume);
                    getQuestData(quest).itemsConsumed.set(items.indexOf(found), newStack);
                    finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null, null,
                            null, null, null);
                    
                    // Multiplayer
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).itemsConsumed.set(items.indexOf(found), newStack);
                        q.finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null,
                                null, null, null, null);
                        return null;
                    });
                } else {
                    final ItemStack newStack = found;
                    found.setAmount(newAmount);
                    getQuestData(quest).itemsConsumed.set(items.indexOf(found), newStack);
                }
                return;
            }
            
            final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                    new Objective(type, newAmount, toConsume));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }
    
    /**
     * Mark item as delivered to a NPC if Quester has such an objective
     * 
     * @param quest The quest for which the item is being delivered
     * @param n The NPC being delivered to
     * @param i The item being delivered
     */
    @SuppressWarnings("deprecation")
    public void deliverToNPC(final Quest quest, final NPC n, final ItemStack i) {
        if (n == null) {
            return;
        }
        
        int currentIndex = -1;
        final LinkedList<Integer> matches = new LinkedList<Integer>();
        for (final ItemStack is : getQuestData(quest).itemsDelivered) {
            currentIndex++;
            if (ItemUtil.compareItems(i, is, true) == 0) {
                matches.add(currentIndex);
            }
        }
        if (matches.isEmpty()) {
            return;
        }
        final Player player = getPlayer();
        for (final Integer match : matches) {
            final LinkedList<ItemStack> items = new LinkedList<ItemStack>(getQuestData(quest).itemsDelivered);
            if (!getCurrentStage(quest).getItemDeliveryTargets().get(match).equals(n.getId())) {
                continue;
            }
            final ItemStack found = items.get(match);
            final int amount = found.getAmount();
            final int toDeliver = getCurrentStage(quest).itemsToDeliver.get(match).getAmount();
            
            final ObjectiveType type = ObjectiveType.DELIVER_ITEM;
            final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                    new Objective(type, amount, toDeliver));
            plugin.getServer().getPluginManager().callEvent(preEvent);
            
            final int newAmount = i.getAmount() + amount;
            final Material m = i.getType();
            if (amount < toDeliver) {
                final int index = player.getInventory().first(i);
                if (index == -1) {
                    // Already delivered in previous loop
                    return;
                }
                if (newAmount >= toDeliver) {
                    final ItemStack newStack = found;
                    found.setAmount(toDeliver);
                    getQuestData(quest).itemsDelivered.set(items.indexOf(found), newStack);
                    if ((i.getAmount() + amount) >= toDeliver) {
                        // Take away remaining amount to be delivered
                        final ItemStack clone = i.clone();
                        clone.setAmount(i.getAmount() - (toDeliver - amount));
                        player.getInventory().setItem(index, clone);
                    } else {
                        player.getInventory().setItem(index, null);
                    }
                    player.updateInventory();
                    finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null, null,
                            null, null, null);
                    
                    // Multiplayer
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                        q.getQuestData(quest).itemsDelivered.set(items.indexOf(found), newStack);
                        q.finishObjective(quest, new Objective(type, new ItemStack(m, 1), found), null, null, null,
                                null, null, null, null);
                        return null;
                    });
                } else {
                    final ItemStack newStack = found;
                    found.setAmount(newAmount);
                    getQuestData(quest).itemsDelivered.set(items.indexOf(found), newStack);
                    player.getInventory().setItem(index, null);
                    player.updateInventory();
                    final String[] message = ConfigUtil.parseStringWithPossibleLineBreaks(getCurrentStage(quest)
                            .deliverMessages.get(new Random().nextInt(getCurrentStage(quest).deliverMessages
                            .size())), plugin.getDependencies().getCitizens().getNPCRegistry()
                            .getById(getCurrentStage(quest).itemDeliveryTargets.get(items.indexOf(found))));
                    player.sendMessage(message);
                }
            }
            
            final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                    new Objective(type, newAmount, toDeliver));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }
    
    /**
     * Mark NPC as interacted with if Quester has such an objective
     * 
     * @param quest The quest for which the NPC is being interacted with
     * @param n The NPC being interacted with
     */
    public void interactWithNPC(final Quest quest, final NPC n) {
        final ObjectiveType type = ObjectiveType.TALK_TO_NPC;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, 1, 1));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        if (!getQuestData(quest).citizensInteracted.containsKey(n.getId())) {
            return;
        }
        
        final Boolean b = getQuestData(quest).citizensInteracted.get(n.getId());
        if (b != null && !b) {
            getQuestData(quest).citizensInteracted.put(n.getId(), true);
            finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), new ItemStack(Material.AIR, 1)), 
                    null, null, n, null, null, null, null);
            
            // Multiplayer
            dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                q.getQuestData(quest).citizensInteracted.put(n.getId(), true);
                q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, 1)), null, null, n, null, null, null, null);
                return null;
            });
            
            final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                    new Objective(type, 1, 1));
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Mark NPC as killed if the Quester has such an objective
     * 
     * @param quest The quest for which the NPC is being killed
     * @param n The NPC being killed
     */
    public void killNPC(final Quest quest, final NPC n) {
        if (!getQuestData(quest).citizensKilled.contains(n.getId())) {
            return;
        }
        
        final int index = getQuestData(quest).citizensKilled.indexOf(n.getId());
        final int npcsKilled = getQuestData(quest).citizenNumKilled.get(index);
        final int npcsToKill = getCurrentStage(quest).citizenNumToKill.get(index);
        
        final ObjectiveType type = ObjectiveType.KILL_NPC;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, npcsKilled, npcsToKill));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final int newNpcsKilled = getQuestData(quest).citizenNumKilled.get(index) + 1;
        if (npcsKilled < npcsToKill) {
            getQuestData(quest).citizenNumKilled.set(index, newNpcsKilled);
            if (newNpcsKilled >= npcsToKill) {
                finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, npcsToKill)), null, null, n, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    q.getQuestData(quest).citizenNumKilled.set(index, getQuestData(quest).citizenNumKilled
                            .get(index));
                    q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, npcsToKill)), null, null, n, null, null, null, null);
                    return null;
                });
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newNpcsKilled, npcsToKill));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }
    
    /**
     * Mark cow as milked if Quester has such an objective
     * 
     * @param quest The quest for which the fish is being caught
     */
    public void milkCow(final Quest quest) {
        final QuestData questData = getQuestData(quest);
        if (questData == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage == null) {
            return;
        }
        if (currentStage.cowsToMilk == null) {
            return;
        }
        
        final int cowsMilked = questData.getCowsMilked();
        final int cowsToMilk = currentStage.cowsToMilk;
        
        final ObjectiveType type = ObjectiveType.MILK_COW;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, cowsMilked, cowsToMilk));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final int newCowsMilked = cowsMilked + 1;
        if (cowsMilked < cowsToMilk) {
            questData.setCowsMilked(newCowsMilked);
            
            if (newCowsMilked >= cowsToMilk) {
                finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, cowsToMilk)), null, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    q.getQuestData(quest).setCowsMilked(cowsToMilk);
                    q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, cowsToMilk)), null, null, null, null, null, null, null);
                    return null;
                });
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newCowsMilked, cowsToMilk));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }
    
    /**
     * Mark fish as caught if Quester has such an objective
     * 
     * @param quest The quest for which the fish is being caught
     */
    public void catchFish(final Quest quest) {
        final QuestData questData = getQuestData(quest);
        if (questData == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage == null) {
            return;
        }
        if (currentStage.fishToCatch == null) {
            return;
        }
        
        final int fishCaught = questData.getFishCaught();
        final int fishToCatch = currentStage.fishToCatch;
        
        final ObjectiveType type = ObjectiveType.CATCH_FISH;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, fishCaught, fishToCatch));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final int newFishCaught = fishCaught + 1;
        if (fishCaught < fishToCatch) {
            questData.setFishCaught(newFishCaught);
            
            if (newFishCaught >= fishToCatch) {
                finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, fishToCatch)), null, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    q.getQuestData(quest).setFishCaught(fishToCatch);
                    q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, fishToCatch)), null, null, null, null, null, null, null);
                    return null;
                });
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newFishCaught, fishToCatch));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark mob as killed if Quester has such an objective
     * 
     * @param quest The quest for which the mob is being killed
     * @param killedLocation The optional location to kill at
     * @param e The mob to be killed
     */
    public void killMob(final Quest quest, final Location killedLocation, final EntityType e) {
        final QuestData questData = getQuestData(quest);
        if (e == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage.mobsToKill == null) {
            return;
        }
        if (currentStage.mobsToKill.contains(e) == false) {
            return;
        }
        
        final int index = questData.mobsKilled.indexOf(e);
        if (index == -1) {
            return;
        }
        final int mobsKilled = questData.mobNumKilled.get(index);
        final int mobsToKill = currentStage.mobNumToKill.get(index);
        if (questData.locationsToKillWithin.isEmpty() == false) {
            final Location locationToKillWithin = questData.locationsToKillWithin.get(index);
            final double radius = questData.radiiToKillWithin.get(index);
            if ((killedLocation.getWorld().getName().equals(locationToKillWithin.getWorld().getName())) == false) {
                return;
            }
            if ((killedLocation.getX() < (locationToKillWithin.getX() + radius) && killedLocation.getX() 
                    > (locationToKillWithin.getX() - radius)) == false) {
                return;
            }
            if ((killedLocation.getZ() < (locationToKillWithin.getZ() + radius) && killedLocation.getZ() 
                    > (locationToKillWithin.getZ() - radius)) == false) {
                return;
            }
            if ((killedLocation.getY() < (locationToKillWithin.getY() + radius) && killedLocation.getY() 
                    > (locationToKillWithin.getY() - radius)) == false) {
                return;
            }
        }
        
        final ObjectiveType type = ObjectiveType.KILL_MOB;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, mobsKilled, mobsToKill));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final int newMobsKilled = mobsKilled + 1;
        if (mobsKilled < mobsToKill) {
            questData.mobNumKilled.set(index, newMobsKilled);
            if (newMobsKilled >= mobsToKill) {
                finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, mobsToKill)), e, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, currentStage, (final Quester q) -> {
                    q.getQuestData(quest).mobNumKilled.set(index, newMobsKilled);
                    q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                            new ItemStack(Material.AIR, mobsToKill)), e, null, null, null, null, null, null);
                    return null;
                });
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newMobsKilled, mobsToKill));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark player as killed if Quester has such an objective
     * 
     * @param quest The quest for which the player is being killed
     * @param player The player to be killed
     */
    public void killPlayer(final Quest quest, final Player player) {
        final QuestData questData = getQuestData(quest);
        if (questData == null) {
            return;
        }
        final Stage currentStage = getCurrentStage(quest);
        if (currentStage == null) {
            return;
        }
        if (currentStage.playersToKill == null) {
            return;
        }
        
        final int playersKilled = questData.getPlayersKilled();
        final int playersToKill = currentStage.playersToKill;
        
        final ObjectiveType type = ObjectiveType.KILL_PLAYER;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, playersKilled, playersToKill));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final int newPlayersKilled = playersKilled + 1;
        if (playersKilled < playersToKill) {
            questData.setPlayersKilled(newPlayersKilled);
            if (newPlayersKilled >= playersToKill) {
                finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, playersToKill)), null, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    q.getQuestData(quest).setPlayersKilled(getQuestData(quest).getPlayersKilled());
                    q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, playersToKill)), null, null, null, null, null, null, null);
                    return null;
                });
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newPlayersKilled, playersToKill));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark location as reached if the Quester has such an objective
     * 
     * @param quest The quest for which the location is being reached
     * @param location The location being reached
     */
    public void reachLocation(final Quest quest, final Location location) {
        if (getQuestData(quest) == null || getCurrentStage(quest) == null
                || getCurrentStage(quest).locationsToReach == null
                || getQuestData(quest).locationsReached == null || getQuestData(quest).radiiToReachWithin == null
                || getQuestData(quest).hasReached == null) {
            return;
        }
        
        final int locsToReach = getCurrentStage(quest).locationsToReach.size();
        final int locsReached = getQuestData(quest).locationsReached.size();
        
        int index = 0;
        try {
            for (final Location toReach : getCurrentStage(quest).locationsToReach) {
                if (!location.getWorld().getName().equals(toReach.getWorld().getName())) {
                    index++;
                    continue;
                }
                final double radius = getQuestData(quest).radiiToReachWithin.get(index);
                if (toReach.distanceSquared(location) <= radius * radius) {
                    if (!getQuestData(quest).hasReached.get(index)) {
                        final ObjectiveType type = ObjectiveType.REACH_LOCATION;
                        final QuesterPreUpdateObjectiveEvent preEvent 
                                = new QuesterPreUpdateObjectiveEvent(this, quest, 
                                new Objective(type, locsReached, locsToReach));
                        plugin.getServer().getPluginManager().callEvent(preEvent);
                        
                        getQuestData(quest).hasReached.set(index, true);
                        finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                                new ItemStack(Material.AIR, 1)), null, null, null, toReach, null, null,
                                null);
                        
                        // Multiplayer
                        final int finalIndex = index;
                        dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                            q.getQuestData(quest).hasReached.set(finalIndex, true);
                            q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                                    new ItemStack(Material.AIR, 1)), null, null, null, toReach, null,
                                    null, null);
                            return null;
                        });
                        
                        final QuesterPostUpdateObjectiveEvent postEvent 
                                = new QuesterPostUpdateObjectiveEvent(this, quest, 
                                new Objective(type, locsReached + 1, locsToReach));
                        plugin.getServer().getPluginManager().callEvent(postEvent);
                        
                        break;
                    }
                }
                index++;
            }
        } catch (final Exception e) {
            plugin.getLogger().severe("An error has occurred with Quests. Please report on Github with info below");
            plugin.getLogger().warning("quest = " + quest.getId());
            plugin.getLogger().warning("index = " + index);
            plugin.getLogger().warning("location = " + location.toString());
            plugin.getLogger().warning("locationsToReach = " + getCurrentStage(quest).locationsToReach.size());
            plugin.getLogger().warning("locationsReached = " + getQuestData(quest).locationsReached.size());
            plugin.getLogger().warning("hasReached = " + getQuestData(quest).hasReached.size());
            e.printStackTrace();
        }
    }

    /**
     * Mark mob as tamed if the Quester has such an objective
     * 
     * @param quest The quest for which the mob is being tamed
     * @param entity The mob being tamed
     */
    public void tameMob(final Quest quest, final EntityType entity) {
        if (!getQuestData(quest).mobsTamed.containsKey(entity)) {
            return;
        }
        final int mobsToTame = getCurrentStage(quest).mobsToTame.get(entity);
        final int mobsTamed = getQuestData(quest).mobsTamed.get(entity);
        
        final ObjectiveType type = ObjectiveType.TAME_MOB;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest,
                new Objective(type, mobsToTame, mobsTamed));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final int newMobsToTame = getQuestData(quest).mobsTamed.get(entity) + 1;
        if (mobsTamed < mobsToTame) {
            getQuestData(quest).mobsTamed.put(entity, newMobsToTame);
            if (newMobsToTame >= mobsToTame) {
                finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, mobsToTame)), entity, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    q.getQuestData(quest).mobsTamed.put(entity, getQuestData(quest).mobsTamed.get(entity));
                    q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                            new ItemStack(Material.AIR, mobsToTame)), entity, null, null, null, null, null, null);
                    return null;
                });
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newMobsToTame, mobsTamed));
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }

    /**
     * Mark sheep as sheared if the Quester has such an objective
     * 
     * @param quest The quest for which the sheep is being sheared
     * @param color The wool color of the sheep being sheared
     */
    public void shearSheep(final Quest quest, final DyeColor color) {
        if (!getQuestData(quest).sheepSheared.containsKey(color)) {
            return;
        }
        final int sheepSheared = getQuestData(quest).sheepSheared.get(color);
        final int sheepToShear = getCurrentStage(quest).sheepToShear.get(color);
        
        final ObjectiveType type = ObjectiveType.SHEAR_SHEEP;
        final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest, 
                new Objective(type, sheepSheared, sheepToShear));
        plugin.getServer().getPluginManager().callEvent(preEvent);
        
        final int newSheepSheared = getQuestData(quest).sheepSheared.get(color) + 1;
        if (sheepSheared < sheepToShear) {
            getQuestData(quest).sheepSheared.put(color, newSheepSheared);
            if (newSheepSheared >= sheepToShear) {
                finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, sheepToShear)), null, null, null, null, color, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                    q.getQuestData(quest).sheepSheared.put(color, getQuestData(quest).sheepSheared.get(color));
                    q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                            new ItemStack(Material.AIR, sheepToShear)), null, null, null, null, color, null, null);
                    return null;
                });
            }
        }
        
        final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest, 
                new Objective(type, newSheepSheared, sheepToShear));
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
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            final QuesterPreUpdateObjectiveEvent preEvent = new QuesterPreUpdateObjectiveEvent(this, quest,
                    new Objective(type, 1, 1));
            plugin.getServer().getPluginManager().callEvent(preEvent);
            
            boolean done = false;
            for (final LinkedList<String> passes : getCurrentStage(quest).passwordPhrases) {
                done = false;
                for (final String pass : passes) {
                    if (pass.equalsIgnoreCase(evt.getMessage())) {
                        final String display = getCurrentStage(quest).passwordDisplays.get(getCurrentStage(quest)
                                .passwordPhrases.indexOf(passes));
                        getQuestData(quest).passwordsSaid.put(display, true);
                        done = true;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                                    new ItemStack(Material.AIR, 1)), null, null, null, null, null, display, null);
                            
                            // Multiplayer
                            dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (final Quester q) -> {
                                q.getQuestData(quest).passwordsSaid.put(display, true);
                                q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1),
                                        new ItemStack(Material.AIR, 1)), null, null, null, null, null, display, null);
                                return null;
                            });
                        });
                        break;
                    }
                }
                if (done) {
                    break;
                }
            }
            
            if (done) {
                final QuesterPostUpdateObjectiveEvent postEvent = new QuesterPostUpdateObjectiveEvent(this, quest,
                        new Objective(type, 1, 1));
                plugin.getServer().getPluginManager().callEvent(postEvent);
            }
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
     *            NPC being talked to or killed, if any
     * @param location
     *            Location for user to reach, if any
     * @param color
     *            Shear color, if any
     * @param pass
     *            Password, if any
     * @param co
     *            Custom objective, if any. See {@link me.blackvein.quests.CustomObjective}
     */
    @SuppressWarnings("deprecation")
    public void finishObjective(final Quest quest, final Objective objective, final EntityType mob, final String extra,
            final NPC npc, final Location location, final DyeColor color, final String pass, final CustomObjective co) {
        if (objective == null) {
            return;
        }
        final Player p = getPlayer();
        final ObjectiveType type = objective.getType();
        final ItemStack increment = objective.getItemProgress() != null ? objective.getItemProgress() 
                : new ItemStack(Material.AIR, objective.getProgress());
        final ItemStack goal = objective.getItemGoal() != null ? objective.getItemGoal() 
                : new ItemStack(Material.AIR, objective.getGoal());
        if (getCurrentStage(quest).objectiveOverrides.isEmpty() == false) {
            for (final String s: getCurrentStage(quest).objectiveOverrides) {
                String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " 
                        + ConfigUtil.parseString(ChatColor.translateAlternateColorCodes('&', s), quest, p);
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(p, message);
                }
                sendMessage(message);
            }
        } else if (type.equals(ObjectiveType.BREAK_BLOCK)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "break");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += " <item>" + ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, increment.getType(), increment.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (type.equals(ObjectiveType.DAMAGE_BLOCK)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "damage");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += " <item>" + ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, increment.getType(), increment.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (type.equals(ObjectiveType.PLACE_BLOCK)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "place");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += " <item>" + ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, increment.getType(), increment.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (type.equals(ObjectiveType.USE_BLOCK)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "use");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += " <item>" + ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, increment.getType(), increment.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (type.equals(ObjectiveType.CUT_BLOCK)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "cut");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += " <item>" + ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, increment.getType(), increment.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (type.equals(ObjectiveType.CRAFT_ITEM)) {
            final ItemStack is = getCurrentStage(quest).itemsToCraft.get(getCurrentStage(quest).itemsToCraft.indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "craftItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + is.getAmount() + "/" + is.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + is.getAmount() + "/" + is.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.SMELT_ITEM)) {
            final ItemStack is = getCurrentStage(quest).itemsToSmelt.get(getCurrentStage(quest).itemsToSmelt.indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "smeltItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + is.getAmount() + "/" + is.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + is.getAmount() + "/" + is.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.ENCHANT_ITEM)) {
            final ItemStack is = getCurrentStage(quest).itemsToEnchant.get(getCurrentStage(quest).itemsToEnchant.indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "enchItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + is.getAmount() + "/" + is.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + is.getAmount() + "/" + is.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments(), goal.getItemMeta())) {
                    for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                        sendMessage(message.replace("<item>", ItemUtil.getName(is))
                                .replace("<enchantment>", ItemUtil.getPrettyEnchantmentName(e.getKey()))
                                .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                    }
                }
            } else if (plugin.getSettings().canTranslateNames() && !is.hasItemMeta()
                    && Material.getMaterial("LINGERING_POTION") == null) {
                // Bukkit version is below 1.9
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments())) {
                    for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                        sendMessage(message.replace("<item>", ItemUtil.getName(is))
                                .replace("<enchantment>", ItemUtil.getPrettyEnchantmentName(e.getKey()))
                                .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                    }
                }
            } else {
                for (final Entry<Enchantment, Integer> e : is.getEnchantments().entrySet()) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(is))
                            .replace("<enchantment>", ItemUtil.getPrettyEnchantmentName(e.getKey()))
                            .replace("<level>", RomanNumeral.getNumeral(e.getValue())));
                }
            }
        } else if (type.equals(ObjectiveType.BREW_ITEM)) {
            final ItemStack is = getCurrentStage(quest).itemsToBrew.get(getCurrentStage(quest).itemsToBrew.indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "brewItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + is.getAmount() + "/" + is.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + is.getAmount() + "/" + is.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && is.hasItemMeta() && !is.getItemMeta().hasDisplayName()) {
                // Bukkit version is 1.9+
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments(), goal.getItemMeta())) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else if (plugin.getSettings().canTranslateNames() && !is.hasItemMeta()
                    && Material.getMaterial("LINGERING_POTION") == null) {
                // Bukkit version is below 1.9
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(),
                        goal.getEnchantments())) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.CONSUME_ITEM)) {
            final ItemStack is = getCurrentStage(quest).itemsToConsume.get(getCurrentStage(quest).itemsToConsume
                    .indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "consumeItem");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + is.getAmount() + "/" + is.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + is.getAmount() + "/" + is.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta() 
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, goal.getType(), goal.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.DELIVER_ITEM)) {
            final ItemStack is = getCurrentStage(quest).itemsToDeliver.get(getCurrentStage(quest).itemsToDeliver
                    .indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "deliver")
                    .replace("<npc>", plugin.getDependencies().getNPCName(getCurrentStage(quest).itemDeliveryTargets
                    .get(getCurrentStage(quest).itemsToDeliver.indexOf(goal))));
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + is.getAmount() + "/" + is.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + is.getAmount() + "/" + is.getAmount();
            }
            if (plugin.getSettings().canTranslateNames() && !goal.hasItemMeta()
                    && !goal.getItemMeta().hasDisplayName()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, is.getType(), is.getDurability(), null)) {
                    sendMessage(message.replace("<item>", ItemUtil.getName(is)));
                }
            } else {
                sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (type.equals(ObjectiveType.MILK_COW)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "milkCow");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            sendMessage(message);
        } else if (type.equals(ObjectiveType.CATCH_FISH)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "catchFish");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            sendMessage(message);
        } else if (type.equals(ObjectiveType.KILL_MOB)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "kill");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += ChatColor.AQUA + " <mob>" + ChatColor.GREEN + ": " + goal.getAmount() + "/"
                        + goal.getAmount();
            }
            if (plugin.getSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, mob, extra)) {
                    sendMessage(message.replace("<mob>", MiscUtil.snakeCaseToUpperCamelCase(mob.name())));
                }
            } else {
                sendMessage(message.replace("<mob>", MiscUtil.snakeCaseToUpperCamelCase(mob.name())));
            }
        } else if (type.equals(ObjectiveType.KILL_PLAYER)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "killPlayer");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            sendMessage(message);
        } else if (type.equals(ObjectiveType.TALK_TO_NPC)) {
            final String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "talkTo")
                    .replace("<npc>", plugin.getDependencies().getNPCName(npc.getId()));
            sendMessage(message);
        } else if (type.equals(ObjectiveType.KILL_NPC)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "kill");
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += ChatColor.AQUA + " <mob>" + ChatColor.GREEN + ": " + goal.getAmount() + "/"
                        + goal.getAmount();
            }
            sendMessage(message.replace("<mob>", plugin.getDependencies().getNPCName(npc.getId())));
        } else if (type.equals(ObjectiveType.TAME_MOB)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "tame");
            if (!message.contains("<mob>")) {
                message += " <mob>";
            }
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            if (plugin.getSettings().canTranslateNames()) {
                if (!plugin.getLocaleManager().sendMessage(p, message, mob, extra)) {
                    sendMessage(message.replace("<mob>", MiscUtil.snakeCaseToUpperCamelCase(mob.name())));
                }
            } else {
                sendMessage(message.replace("<mob>", MiscUtil.snakeCaseToUpperCamelCase(mob.name())));
            }
        } else if (type.equals(ObjectiveType.SHEAR_SHEEP)) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "shearSheep");
            message = message.replace("<color>", MiscUtil.getPrettyDyeColorName(color));
            if (message.contains("<count>")) {
                message = message.replace("<count>", "" + ChatColor.GREEN + goal.getAmount() + "/" + goal.getAmount());
            } else {
                // Legacy
                message += ChatColor.GREEN + ": " + goal.getAmount() + "/" + goal.getAmount();
            }
            sendMessage(message);
        } else if (type.equals(ObjectiveType.REACH_LOCATION)) {
            String obj = Lang.get(p, "goTo");
            try {
                obj = obj.replace("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest)
                        .locationsToReach.indexOf(location)));
            } catch(final IndexOutOfBoundsException e) {
                plugin.getLogger().severe("Unable to get final location " + location + " for quest ID " 
                        + quest.getId() + ", please report on Github");
                obj = obj.replace("<location>", "ERROR");
            }
            final String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
            sendMessage(message);
        } else if (type.equals(ObjectiveType.PASSWORD)) {
            sendMessage(ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + pass);
        } else if (co != null) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + co.getDisplay();
            int index = -1;
            for (int i = 0; i < getCurrentStage(quest).customObjectives.size(); i++) {
                if (getCurrentStage(quest).customObjectives.get(i).getName().equals(co.getName())) {
                    index = i;
                    break;
                }
            }
            final List<Entry<String, Object>> sub = new LinkedList<>();
            sub.addAll(getCurrentStage(quest).customObjectiveData.subList(index, getCurrentStage(quest)
                    .customObjectiveData.size()));
            final List<Entry<String, Object>> end = new LinkedList<Entry<String, Object>>(sub);
            sub.clear(); // since sub is backed by end, this removes all sub-list items from end
            for (final Entry<String, Object> datamap : end) {
                message = message.replace("%" + (String.valueOf(datamap.getKey())) + "%", String.valueOf(datamap
                        .getValue()));
            }
            
            if (co.canShowCount()) {
                message = message.replace("%count%", goal.getAmount() + "/" + goal.getAmount());
            }
            sendMessage(ConfigUtil.parseString(ChatColor.translateAlternateColorCodes('&', message)));
        }
        if (testComplete(quest)) {
            quest.nextStage(this, true);
        }
    }

    /**
     * Complete quest objective
     * 
     * @deprecated Use {@link #finishObjective(Quest, Objective, EntityType,
     * String, NPC, Location, DyeColor, String, CustomObjective)}
     * 
     * @param quest
     *            Quest containing the objective
     * @param objective
     *            Type of objective, e.g. "password" or "damageBlock"
     * @param increment
     *            Final amount material being applied
     * @param goal
     *            Total required amount of material
     * @param enchantment
     *            Enchantment being applied by user
     * @param mob
     *            Mob being killed or tamed
     * @param extra
     *            Extra mob enum like career or ocelot type
     * @param npc
     *            NPC being talked to or killed
     * @param location
     *            Location for user to reach
     * @param color
     *            Shear color
     * @param pass
     *            Password
     * @param co
     *            See CustomObjective class
     */
    @Deprecated
    public void finishObjective(final Quest quest, final String objective, final ItemStack increment, 
            final ItemStack goal, final Enchantment enchantment, final EntityType mob, final String extra, 
            final NPC npc, final Location location, final DyeColor color, final String pass, final CustomObjective co) {
        if (objective == null) {
            return;
        }
        if (increment == null || goal == null) {
            finishObjective(quest, new Objective(ObjectiveType.fromName(objective), 1, 1), mob, extra, npc,
                    location, color, pass, co);
        } else {
            finishObjective(quest, new Objective(ObjectiveType.fromName(objective), increment, goal), mob, extra, npc,
                    location, color, pass, co);
        }
    }
    
    /**
     * Check whether this Quester has completed all objectives for their current stage
     * 
     * @param quest The quest with the current stage being checked
     * @return true if all stage objectives are marked complete
     */
    public boolean testComplete(final Quest quest) {
        for (final String s : getCurrentObjectives(quest, true)) {
            if (s.startsWith(ChatColor.GREEN.toString())) {
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
    @SuppressWarnings("deprecation")
    public void addEmptiesFor(final Quest quest, final int stage) {
        final QuestData data = new QuestData(this);
        data.setDoJournalUpdate(false);
        if (quest == null) {
            plugin.getLogger().warning("Unable to find quest for player " + this.lastKnownName);
            return;
        }
        if (quest.getStage(stage) == null) {
            plugin.getLogger().severe("Unable to find Stage " + stage + " of quest ID " + quest.getId());
            return;
        }
        if (quest.getStage(stage).blocksToBreak.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).blocksToBreak) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksBroken.indexOf(i) != -1) {
                    data.blocksBroken.set(data.blocksBroken.indexOf(temp), temp);
                } else {
                    data.blocksBroken.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToDamage.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).blocksToDamage) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksDamaged.indexOf(i) != -1) {
                    data.blocksDamaged.set(data.blocksDamaged.indexOf(temp), temp);
                } else {
                    data.blocksDamaged.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToPlace.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).blocksToPlace) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksPlaced.indexOf(i) != -1) {
                    data.blocksPlaced.set(data.blocksPlaced.indexOf(temp), temp);
                } else {
                    data.blocksPlaced.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToUse.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).blocksToUse) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksUsed.indexOf(i) != -1) {
                    data.blocksUsed.set(data.blocksUsed.indexOf(temp), temp);
                } else {
                    data.blocksUsed.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToCut.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).blocksToCut) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksCut.indexOf(i) != -1) {
                    data.blocksCut.set(data.blocksCut.indexOf(temp), temp);
                } else {
                    data.blocksCut.add(temp);
                }
            }
        }
        if (quest.getStage(stage).itemsToCraft.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).itemsToCraft) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                temp.addUnsafeEnchantments(i.getEnchantments());
                temp.setItemMeta(i.getItemMeta());
                data.itemsCrafted.add(temp);
            }
        }
        if (quest.getStage(stage).itemsToSmelt.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).itemsToSmelt) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                temp.addUnsafeEnchantments(i.getEnchantments());
                temp.setItemMeta(i.getItemMeta());
                data.itemsSmelted.add(temp);
            }
        }
        if (quest.getStage(stage).itemsToEnchant.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).itemsToEnchant) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                temp.addUnsafeEnchantments(i.getEnchantments());
                temp.setItemMeta(i.getItemMeta());
                data.itemsEnchanted.add(temp);
            }
        }
        if (quest.getStage(stage).itemsToBrew.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).itemsToBrew) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                temp.addUnsafeEnchantments(i.getEnchantments());
                temp.setItemMeta(i.getItemMeta());
                data.itemsBrewed.add(temp);
            }
        }
        if (quest.getStage(stage).itemsToConsume.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).itemsToConsume) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                temp.addUnsafeEnchantments(i.getEnchantments());
                temp.setItemMeta(i.getItemMeta());
                data.itemsConsumed.add(temp);
            }
        }
        if (quest.getStage(stage).itemsToDeliver.isEmpty() == false) {
            for (final ItemStack i : quest.getStage(stage).itemsToDeliver) {
                final ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                temp.addUnsafeEnchantments(i.getEnchantments());
                temp.setItemMeta(i.getItemMeta());
                data.itemsDelivered.add(temp);
            }
        }
        if (quest.getStage(stage).citizensToInteract.isEmpty() == false) {
            for (final Integer n : quest.getStage(stage).citizensToInteract) {
                data.citizensInteracted.put(n, false);
            }
        }
        if (quest.getStage(stage).citizensToKill.isEmpty() == false) {
            for (final Integer n : quest.getStage(stage).citizensToKill) {
                data.citizensKilled.add(n);
                data.citizenNumKilled.add(0);
            }
        }
        if (quest.getStage(stage).mobsToKill.isEmpty() == false) {
            for (final EntityType e : quest.getStage(stage).mobsToKill) {
                data.mobsKilled.add(e);
                data.mobNumKilled.add(0);
                if (quest.getStage(stage).locationsToKillWithin.isEmpty() == false) {
                    data.locationsToKillWithin.add(quest.getStage(stage).locationsToKillWithin.get(data.mobsKilled
                            .indexOf(e)));
                }
                if (quest.getStage(stage).radiiToKillWithin.isEmpty() == false) {
                    data.radiiToKillWithin.add(quest.getStage(stage).radiiToKillWithin.get(data.mobsKilled.indexOf(e)));
                }
            }
        }
        data.setCowsMilked(0);
        data.setFishCaught(0);
        data.setPlayersKilled(0);
        if (quest.getStage(stage).locationsToReach.isEmpty() == false) {
            for (final Location l : quest.getStage(stage).locationsToReach) {
                data.locationsReached.add(l);
                data.hasReached.add(false);
                data.radiiToReachWithin.add(quest.getStage(stage).radiiToReachWithin.get(data.locationsReached
                        .indexOf(l)));
            }
        }
        if (quest.getStage(stage).mobsToTame.isEmpty() == false) {
            for (final EntityType e : quest.getStage(stage).mobsToTame.keySet()) {
                data.mobsTamed.put(e, 0);
            }
        }
        if (quest.getStage(stage).sheepToShear.isEmpty() == false) {
            for (final DyeColor d : quest.getStage(stage).sheepToShear.keySet()) {
                data.sheepSheared.put(d, 0);
            }
        }
        if (quest.getStage(stage).passwordDisplays.isEmpty() == false) {
            for (final String display : quest.getStage(stage).passwordDisplays) {
                data.passwordsSaid.put(display, false);
            }
        }
        if (quest.getStage(stage).customObjectives.isEmpty() == false) {
            for (final CustomObjective co : quest.getStage(stage).customObjectives) {
                data.customObjectiveCounts.put(co.getName(), 0);
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
            final Storage storage = plugin.getStorage();
            storage.saveQuesterData(this);
        } catch (final Exception e) {
            return false;
        }
        return true;
    }
    
    /**
     * Get the difference between Sytem.currentTimeMillis() and the last completed time for a quest
     * 
     * @param quest The quest to get the last completed time of
     * @return Difference between now and then in milliseconds
     */
    public long getCompletionDifference(final Quest quest) {
        final long currentTime = System.currentTimeMillis();
        long lastTime;
        if (completedTimes.containsKey(quest) == false) {
            lastTime = System.currentTimeMillis();
            completedTimes.put(quest, System.currentTimeMillis());
        } else {
            lastTime = completedTimes.get(quest);
        }
        return currentTime - lastTime;
    }
    
    /**
     * Get the difference between player cooldown and time since last completion of a quest
     * 
     * @deprecated Use {@link #getRemainingCooldown(Quest)}
     * @param quest The quest to get the last completed time of
     * @return Difference between now and then in milliseconds
     */
    @Deprecated
    public long getCooldownDifference(final Quest quest) {
        return quest.getPlanner().getCooldown() - getCompletionDifference(quest);
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

    @SuppressWarnings("deprecation")
    public FileConfiguration getBaseData() {
        final FileConfiguration data = new YamlConfiguration();
        if (currentQuests.isEmpty() == false) {
            final ArrayList<String> questIds = new ArrayList<String>();
            final ArrayList<Integer> questStages = new ArrayList<Integer>();
            for (final Quest quest : currentQuests.keySet()) {
                questIds.add(quest.getId());
                questStages.add(currentQuests.get(quest));
            }
            data.set("currentQuests", questIds);
            data.set("currentStages", questStages);
            data.set("quest-points", questPoints);
            final ConfigurationSection dataSec = data.createSection("questData");
            for (final Quest quest : currentQuests.keySet()) {
                if (quest.getName() == null || quest.getName().isEmpty()) {
                    plugin.getLogger().severe("Quest name was null or empty while loading data");
                    return null;
                }
                final ConfigurationSection questSec = dataSec.createSection(quest.getId());
                final QuestData questData = getQuestData(quest);
                if (questData == null) {
                    continue;
                }
                if (questData.blocksBroken.isEmpty() == false) {
                    final LinkedList<String> blockNames = new LinkedList<String>();
                    final LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    final LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (final ItemStack m : questData.blocksBroken) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-broken-names", blockNames);
                    questSec.set("blocks-broken-amounts", blockAmounts);
                    questSec.set("blocks-broken-durability", blockDurability);
                }
                if (questData.blocksDamaged.isEmpty() == false) {
                    final LinkedList<String> blockNames = new LinkedList<String>();
                    final LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    final LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (final ItemStack m : questData.blocksDamaged) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-damaged-names", blockNames);
                    questSec.set("blocks-damaged-amounts", blockAmounts);
                    questSec.set("blocks-damaged-durability", blockDurability);
                }
                if (questData.blocksPlaced.isEmpty() == false) {
                    final LinkedList<String> blockNames = new LinkedList<String>();
                    final LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    final LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (final ItemStack m : questData.blocksPlaced) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-placed-names", blockNames);
                    questSec.set("blocks-placed-amounts", blockAmounts);
                    questSec.set("blocks-placed-durability", blockDurability);
                }
                if (questData.blocksUsed.isEmpty() == false) {
                    final LinkedList<String> blockNames = new LinkedList<String>();
                    final LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    final LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (final ItemStack m : questData.blocksUsed) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-used-names", blockNames);
                    questSec.set("blocks-used-amounts", blockAmounts);
                    questSec.set("blocks-used-durability", blockDurability);
                }
                if (questData.blocksCut.isEmpty() == false) {
                    final LinkedList<String> blockNames = new LinkedList<String>();
                    final LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    final LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (final ItemStack m : questData.blocksCut) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-cut-names", blockNames);
                    questSec.set("blocks-cut-amounts", blockAmounts);
                    questSec.set("blocks-cut-durability", blockDurability);
                }
                if (questData.itemsCrafted.isEmpty() == false) {
                    final LinkedList<Integer> craftAmounts = new LinkedList<Integer>();
                    for (final ItemStack m : questData.itemsCrafted) {
                        craftAmounts.add(m.getAmount());
                    }
                    questSec.set("item-craft-amounts", craftAmounts);
                }
                if (questData.itemsSmelted.isEmpty() == false) {
                    final LinkedList<Integer> smeltAmounts = new LinkedList<Integer>();
                    for (final ItemStack m : questData.itemsSmelted) {
                        smeltAmounts.add(m.getAmount());
                    }
                    questSec.set("item-smelt-amounts", smeltAmounts);
                }
                if (questData.itemsEnchanted.isEmpty() == false) {
                    final LinkedList<Integer> enchantAmounts = new LinkedList<Integer>();
                    for (final ItemStack m : questData.itemsEnchanted) {
                        enchantAmounts.add(m.getAmount());
                    }
                    questSec.set("item-enchant-amounts", enchantAmounts);
                }
                if (questData.itemsBrewed.isEmpty() == false) {
                    final LinkedList<Integer> brewAmounts = new LinkedList<Integer>();
                    for (final ItemStack m : questData.itemsBrewed) {
                        brewAmounts.add(m.getAmount());
                    }
                    questSec.set("item-brew-amounts", brewAmounts);
                }
                if (questData.itemsConsumed.isEmpty() == false) {
                    final LinkedList<Integer> consumeAmounts = new LinkedList<Integer>();
                    for (final ItemStack m : questData.itemsConsumed) {
                        consumeAmounts.add(m.getAmount());
                    }
                    questSec.set("item-consume-amounts", consumeAmounts);
                }
                if (questData.itemsDelivered.isEmpty() == false) {
                    final LinkedList<Integer> deliveryAmounts = new LinkedList<Integer>();
                    for (final ItemStack m : questData.itemsDelivered) {
                        deliveryAmounts.add(m.getAmount());
                    }
                    questSec.set("item-delivery-amounts", deliveryAmounts);
                }
                if (questData.citizensInteracted.isEmpty() == false) {
                    final LinkedList<Integer> npcIds = new LinkedList<Integer>();
                    final LinkedList<Boolean> hasTalked = new LinkedList<Boolean>();
                    for (final Integer n : questData.citizensInteracted.keySet()) {
                        npcIds.add(n);
                        hasTalked.add(questData.citizensInteracted.get(n));
                    }
                    questSec.set("citizen-ids-to-talk-to", npcIds);
                    questSec.set("has-talked-to", hasTalked);
                }
                if (questData.citizensKilled.isEmpty() == false) {
                    final LinkedList<Integer> npcIds = new LinkedList<Integer>();
                    for (final Integer n : questData.citizensKilled) {
                        npcIds.add(n);
                    }
                    questSec.set("citizen-ids-killed", npcIds);
                    questSec.set("citizen-amounts-killed", questData.citizenNumKilled);
                }
                if (getCurrentStage(quest) != null) {
                    if (getCurrentStage(quest).cowsToMilk != null) {
                        questSec.set("cows-milked", questData.getCowsMilked());
                    }
                    if (getCurrentStage(quest).fishToCatch != null) {
                        questSec.set("fish-caught", questData.getFishCaught());
                    }
                    if (getCurrentStage(quest).playersToKill != null) {
                        questSec.set("players-killed", questData.getPlayersKilled());
                    }
                }
                if (questData.mobsKilled.isEmpty() == false) {
                    final LinkedList<String> mobNames = new LinkedList<String>();
                    final LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                    final LinkedList<String> locations = new LinkedList<String>();
                    final LinkedList<Integer> radii = new LinkedList<Integer>();
                    for (final EntityType e : questData.mobsKilled) {
                        mobNames.add(MiscUtil.getPrettyMobName(e));
                    }
                    for (final int i : questData.mobNumKilled) {
                        mobAmounts.add(i);
                    }
                    questSec.set("mobs-killed", mobNames);
                    questSec.set("mobs-killed-amounts", mobAmounts);
                    if (questData.locationsToKillWithin.isEmpty() == false) {
                        for (final Location l : questData.locationsToKillWithin) {
                            locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());
                        }
                        for (final int i : questData.radiiToKillWithin) {
                            radii.add(i);
                        }
                        questSec.set("mob-kill-locations", locations);
                        questSec.set("mob-kill-location-radii", radii);
                    }
                }
                if (questData.locationsReached.isEmpty() == false) {
                    final LinkedList<String> locations = new LinkedList<String>();
                    final LinkedList<Boolean> has = new LinkedList<Boolean>();
                    final LinkedList<Integer> radii = new LinkedList<Integer>();
                    for (final Location l : questData.locationsReached) {
                        locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());
                    }
                    for (final boolean b : questData.hasReached) {
                        has.add(b);
                    }
                    for (final int i : questData.radiiToReachWithin) {
                        radii.add(i);
                    }
                    questSec.set("locations-to-reach", locations);
                    questSec.set("has-reached-location", has);
                    questSec.set("radii-to-reach-within", radii);
                }
                if (questData.mobsTamed.isEmpty() == false) {
                    final LinkedList<String> mobNames = new LinkedList<String>();
                    final LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                    for (final EntityType e : questData.mobsTamed.keySet()) {
                        mobNames.add(MiscUtil.getPrettyMobName(e));
                        mobAmounts.add(questData.mobsTamed.get(e));
                    }
                    questSec.set("mobs-to-tame", mobNames);
                    questSec.set("mob-tame-amounts", mobAmounts);
                }
                if (questData.sheepSheared.isEmpty() == false) {
                    final LinkedList<String> colors = new LinkedList<String>();
                    final LinkedList<Integer> shearAmounts = new LinkedList<Integer>();
                    for (final DyeColor d : questData.sheepSheared.keySet()) {
                        colors.add(MiscUtil.getPrettyDyeColorName(d));
                        shearAmounts.add(questData.sheepSheared.get(d));
                    }
                    questSec.set("sheep-to-shear", colors);
                    questSec.set("sheep-sheared", shearAmounts);
                }
                if (questData.passwordsSaid.isEmpty() == false) {
                    final LinkedList<String> passwords = new LinkedList<String>();
                    final LinkedList<Boolean> said = new LinkedList<Boolean>();
                    for (final Entry<String, Boolean> entry : questData.passwordsSaid.entrySet()) {
                        passwords.add(entry.getKey());
                        said.add(entry.getValue());
                    }
                    questSec.set("passwords", passwords);
                    questSec.set("passwords-said", said);
                }
                if (questData.customObjectiveCounts.isEmpty() == false) {
                    final LinkedList<String> customObj = new LinkedList<String>();
                    final LinkedList<Integer> customObjCounts = new LinkedList<Integer>();
                    for (final Entry<String, Integer> entry : questData.customObjectiveCounts.entrySet()) {
                        customObj.add(entry.getKey());
                        customObjCounts.add(entry.getValue());
                    }
                    questSec.set("custom-objectives", customObj);
                    questSec.set("custom-objective-counts", customObjCounts);
                }
                if (questData.getDelayTimeLeft() > 0) {
                    questSec.set("stage-delay", questData.getDelayTimeLeft());
                }
                if (questData.actionFired.isEmpty() == false) {
                    final LinkedList<String> chatTriggers = new LinkedList<String>();
                    for (final String trigger : questData.actionFired.keySet()) {
                        if (questData.actionFired.get(trigger) == true) {
                            chatTriggers.add(trigger);
                        }
                    }
                    if (chatTriggers.isEmpty() == false) {
                        questSec.set("chat-triggers", chatTriggers);
                    }
                }
                if (questData.actionFired.isEmpty() == false) {
                    final LinkedList<String> commandTriggers = new LinkedList<String>();
                    for (final String commandTrigger : questData.actionFired.keySet()) {
                        if (questData.actionFired.get(commandTrigger) == true) {
                            commandTriggers.add(commandTrigger);
                        }
                    }
                    if (commandTriggers.isEmpty() == false) {
                        questSec.set("command-triggers", commandTriggers);
                    }
                }
            }
        } else {
            data.set("currentQuests", "none");
            data.set("currentStages", "none");
            data.set("quest-points", questPoints);
        }
        if (completedQuests.isEmpty()) {
            data.set("completed-Quests", "none");
        } else {
            //final List<String> questIds = completedQuests.stream().map(Quest::getId).collect(Collectors.toList());
            final List<String> questIds = new LinkedList<String>();
            for (final Quest quest : completedQuests) {
                questIds.add(quest.getId());
            }
            data.set("completed-Quests", questIds);
        }
        if (completedTimes.isEmpty() == false) {
            final List<String> questIds = new LinkedList<String>();
            final List<Long> questTimes = new LinkedList<Long>();
            for (final Entry<Quest, Long> entry : completedTimes.entrySet()) {
                questIds.add(entry.getKey().getId());
                questTimes.add(entry.getValue());
            }
            data.set("completedRedoableQuests", questIds);
            data.set("completedQuestTimes", questTimes);
        }
        if (amountsCompleted.isEmpty() == false) {
            final List<String> questIds = new LinkedList<String>();
            final List<Integer> questAmts = new LinkedList<Integer>();
            for (final Entry<Quest, Integer> entry : amountsCompleted.entrySet()) {
                questIds.add(entry.getKey().getId());
                questAmts.add(entry.getValue());
            }
            data.set("amountsCompletedQuests", questIds);
            data.set("amountsCompleted", questAmts);
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
        return plugin.getStorage().loadQuesterData(id) != null;
    }
    
    /**
     * Check whether the Quester has data saved to hard storage
     * 
     * @return true if successful
     */
    public boolean hasData() {
        return plugin.getStorage().loadQuesterData(id) != null;
    }
    
    /**
     * Check whether the Quester has base data in memory, indicating they have participated in quests
     * 
     * @return false if empty
     */
    public boolean hasBaseData() {
        if (!currentQuests.isEmpty() || !questData.isEmpty() || !completedQuests.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Initiate the stage timer
     * @param quest The quest of which the timer is for
     */
    public void startStageTimer(final Quest quest) {
        if (getQuestData(quest).getDelayTimeLeft() > -1) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), 
                    (long) (getQuestData(quest).getDelayTimeLeft() * 0.02));
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), 
                    (long) (getCurrentStage(quest).delay * 0.02));
            if (getCurrentStage(quest).delayMessage != null) {
                final Player p = plugin.getServer().getPlayer(id);
                p.sendMessage(ConfigUtil.parseStringWithPossibleLineBreaks((getCurrentStage(quest)
                        .delayMessage), quest, p));
            }
        }
        getQuestData(quest).setDelayStartTime(System.currentTimeMillis());
    }
    
    /**
     * Pause the stage timer. Useful when a player quits
     * @param quest The quest of which the timer is for
     */
    public void stopStageTimer(final Quest quest) {
        if (getQuestData(quest).getDelayTimeLeft() > -1) {
            getQuestData(quest).setDelayTimeLeft(getQuestData(quest).getDelayTimeLeft() - (System.currentTimeMillis() 
                    - getQuestData(quest).getDelayStartTime()));
        } else {
            getQuestData(quest).setDelayTimeLeft(getCurrentStage(quest).delay - (System.currentTimeMillis() 
                    - getQuestData(quest).getDelayStartTime()));
        }
        //getQuestData(quest).setDelayOver(false);
    }
    
    /**
     * Get remaining stage delay time
     * @param quest The quest of which the timer is for
     * @return Remaining time in milliseconds
     */
    public long getStageTime(final Quest quest) {
        if (getQuestData(quest).getDelayTimeLeft() > -1) {
            return getQuestData(quest).getDelayTimeLeft() - (System.currentTimeMillis() 
                    - getQuestData(quest).getDelayStartTime());
        } else {
            return getCurrentStage(quest).delay - (System.currentTimeMillis() - getQuestData(quest).getDelayStartTime());
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
                        // TODO - decide whether or not to handle this
                        /*if (q.equals(quest) == false) {
                            if (getPlayer() != null && getPlayer().isOnline()) {
                                quitQuest(quest, ChatColor.GOLD + Lang.get("questModified")
                                        .replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.GOLD));
                            }
                        }*/
                    }
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                sendMessage(ChatColor.RED + Lang.get("questNotExist").replace("<quest>", ChatColor.DARK_PURPLE 
                        + quest.getName() + ChatColor.RED));
            }
        }
    }

    /**
     * Show an inventory GUI with quest items to the specified player
     * 
     * @param npc The NPC from which the GUI is bound
     * @param quests List of quests to use for displaying items
     */
    public void showGUIDisplay(final NPC npc, final LinkedList<Quest> quests) {
        if (npc == null || quests == null) {
            return;
        }
        final QuesterPreOpenGUIEvent preEvent = new QuesterPreOpenGUIEvent(this, npc, quests);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        final Player player = getPlayer();
        final Inventory inv = plugin.getServer().createInventory(player, ((quests.size() / 9) + 1) * 9, 
                Lang.get(player, "quests") + " | " + npc.getName());
        int i = 0;
        for (final Quest quest : quests) {
            if (quest.guiDisplay != null) {
                if (i > 53) {
                    // Protocol-enforced size limit has been exceeded
                    break;
                }
                final ItemStack display = quest.guiDisplay;
                final ItemMeta meta = display.getItemMeta();
                if (completedQuests.contains(quest)) {
                    meta.setDisplayName(ChatColor.DARK_PURPLE + ConfigUtil.parseString(quest.getName()
                            + " " + ChatColor.GREEN + Lang.get(player, "redoCompleted"), npc));
                } else {
                    meta.setDisplayName(ChatColor.DARK_PURPLE + ConfigUtil.parseString(quest.getName(), npc));
                }
                if (!meta.hasLore()) {
                    LinkedList<String> lines = new LinkedList<String>();
                    String desc = quest.description;
                    if (plugin.getDependencies().getPlaceholderApi() != null) {
                        desc = PlaceholderAPI.setPlaceholders(player, desc);
                    }
                    if (desc.equals(ChatColor.stripColor(desc))) {
                        lines = MiscUtil.makeLines(desc, " ", 40, ChatColor.DARK_GREEN);
                    } else {
                        lines = MiscUtil.makeLines(desc, " ", 40, null);
                    }
                    meta.setLore(lines);
                }
                meta.addItemFlags(ItemFlag.values());
                display.setItemMeta(meta);
                inv.setItem(i, display);
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
            currentQuests.remove(quest);
            if (questData.containsKey(quest)) {
                questData.remove(quest);
            }
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
            questData.clear();
            amountsCompleted.clear();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Forcibly set Quester's current stage, then update Quest Journal
     * 
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
    public void hardDataPut(final Quest key, final QuestData val) {
        try {
            questData.put(key, val);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public boolean canUseCompass() {
        if (getPlayer() != null) {
            if (!getPlayer().hasPermission("worldedit.navigation.jumpto")) {
                if (getPlayer().hasPermission("quests.compass")) {
                    return true;
                }
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
        compassTargetQuestId = null;
        if (defaultLocation != null) {
            player.setCompassTarget(defaultLocation);
        }
    }

    /**
     * Update compass target to current stage of first available current quest, if possible
     */
    public void findCompassTarget() {
        if (!canUseCompass()) {
            return;
        }
        for (final Quest quest : currentQuests.keySet()) {
            final Stage stage = getCurrentStage(quest);
            if (stage != null && quest.updateCompass(this, stage)) {
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
        if (!canUseCompass()) {
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
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
                if (list.size() > 0) {
                    final Quest quest = plugin.getQuestById(list.get(index));
                    compassTargetQuestId = quest.getId();
                    final Stage stage = getCurrentStage(quest);
                    if (stage != null) {
                        quest.updateCompass(Quester.this, stage);
                        if (notify) {
                            sendMessage(ChatColor.YELLOW + Lang.get(getPlayer(), "compassSet")
                                    .replace("<quest>", ChatColor.GOLD + quest.getName() + ChatColor.YELLOW));
                        }
                    }
                } else {
                    sendMessage(ChatColor.RED + Lang.get(getPlayer(), "journalNoQuests")
                            .replace("<journal>", Lang.get(getPlayer(), "journalTitle")));
                }
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
                if (ItemUtil.compareItems(is, stack, false) == 0) {
                    playerAmount += stack.getAmount();
                }
            }
        }
        return playerAmount >= is.getAmount();
    }
    
    /**
     * Dispatch player event to fellow questers<p>
     * 
     * Accepted strings are: breakBlock, damageBlock, placeBlock, useBlock,
     * cutBlock, craftItem, smeltItem, enchantItem, brewItem, consumeItem,
     * milkCow, catchFish, killMob, deliverItem, killPlayer, talkToNPC,
     * killNPC, tameMob, shearSheep, password, reachLocation
     * 
     * @deprecated Use {@link #dispatchMultiplayerEverything(Quest, ObjectiveType, BiFunction)}
     *
     * @param objectiveType The type of objective to progress
     * @param fun The function to execute, the event call
     */
    @Deprecated
    public void dispatchMultiplayerEverything(final Quest quest, final String objectiveType,
            final BiFunction<Quester, Quest, Void> fun) {
        dispatchMultiplayerEverything(quest, ObjectiveType.fromName(objectiveType), fun);
    }
    
    /**
     * Dispatch player event to fellow questers<p>
     *
     * @param type The type of objective to progress
     * @param fun The function to execute, the event call
     */
    public Set<String> dispatchMultiplayerEverything(final Quest quest, final ObjectiveType type,
            final BiFunction<Quester, Quest, Void> fun) {
        final Set<String> appliedQuestIDs = new HashSet<String>();
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
        final Set<String> appliedQuestIDs = new HashSet<String>();
        if (quest.getOptions().getShareProgressLevel() == 2) {
            final List<Quester> mq = getMultiplayerQuesters(quest);
            if (mq == null) {
                return appliedQuestIDs;
            }
            for (final Quester q : mq) {
                if (q == null) {
                    continue;
                }
                // Share only same quest is not necessary here
                // The function must be applied to the same quest
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
        final List<Quester> mq = new LinkedList<Quester>();
        if (plugin.getDependencies().getPartiesApi() != null) {
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
                                        if (player != null && distanceSquared >= getPlayer().getLocation()
                                                .distanceSquared(player.getLocation())) {
                                            mq.add(plugin.getQuester(pp.getPlayerUUID()));
                                        }
                                    } else {
                                        mq.add(plugin.getQuester(pp.getPlayerUUID()));
                                    }
                                }
                            }
                        }
                        
                        return mq;
                    }
                }
            }
        }
        if (plugin.getDependencies().getDungeonsApi() != null) {
            if (quest.getOptions().canUseDungeonsXLPlugin()) {
                final DGroup group = (DGroup) plugin.getDependencies().getDungeonsApi().getPlayerGroup(getPlayer());
                if (group != null) {
                    final double distanceSquared = quest.getOptions().getShareDistance() 
                            * quest.getOptions().getShareDistance();
                    final boolean offlinePlayers = quest.getOptions().canHandleOfflinePlayers();
                    if (offlinePlayers) {
                        for (final UUID id : group.getMembers()) {
                            if (!id.equals(getUUID())) {
                                mq.add(plugin.getQuester(id));
                            }
                        }
                    } else {
                        for (final UUID id : group.getMembers()) {
                            if (!id.equals(getUUID())) {
                                if (distanceSquared > 0) {
                                    final Player player = Bukkit.getPlayer(id);
                                    if (player != null && distanceSquared >= getPlayer().getLocation()
                                            .distanceSquared(player.getLocation())) {
                                        mq.add(plugin.getQuester(id));
                                    }
                                } else {
                                    mq.add(plugin.getQuester(id));
                                }
                            }
                        }
                    }
                    return mq;
                }
            }
        }
        return mq;
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
            if (getPlayer() instanceof Conversable) {
                if (getPlayer().isConversing() == false) {
                    setQuestIdToTake(quest.getId());
                    final String s = ChatColor.GOLD + Lang.get("questObjectivesTitle")
                            .replace("<quest>", quest.getName()) + "\n" + ChatColor.RESET + quest.getDescription();
                    for (final String msg : s.split("<br>")) {
                        sendMessage(msg);
                    }
                    if (!plugin.getSettings().canAskConfirmation()) {
                        takeQuest(quest, false);
                    } else {
                        plugin.getConversationFactory().buildConversation(getPlayer()).begin();
                    }
                    return true;
                } else {
                    sendMessage(ChatColor.YELLOW + Lang.get(getPlayer(), "alreadyConversing"));
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
        if (getCurrentQuests().size() >= plugin.getSettings().getMaxQuests() && plugin.getSettings().getMaxQuests() 
                > 0) {
            if (giveReason) {
                final String msg = Lang.get(getPlayer(), "questMaxAllowed").replace("<number>", 
                        String.valueOf(plugin.getSettings().getMaxQuests()));
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCurrentQuests().containsKey(quest)) {
            if (giveReason) {
                final String msg = Lang.get(getPlayer(), "questAlreadyOn");
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCompletedQuests().contains(quest) && quest.getPlanner().getCooldown() < 0) {
            if (giveReason) {
                final String msg = Lang.get(getPlayer(), "questAlreadyCompleted")
                        .replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (plugin.getDependencies().getCitizens() != null
                && plugin.getSettings().canAllowCommandsForNpcQuests() == false
                && quest.getNpcStart() != null && quest.getNpcStart().getEntity() != null 
                && quest.getNpcStart().getEntity().getLocation().getWorld().getName().equals(
                getPlayer().getLocation().getWorld().getName())
                && quest.getNpcStart().getEntity().getLocation().distance(getPlayer().getLocation()) > 6.0) {
            if (giveReason) {
                final String msg = Lang.get(getPlayer(), "mustSpeakTo").replace("<npc>", ChatColor.DARK_PURPLE 
                        + quest.getNpcStart().getName() + ChatColor.YELLOW);
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (quest.getBlockStart() != null) {
            if (giveReason) {
                final String msg = Lang.get(getPlayer(), "noCommandStart").replace("<quest>", ChatColor.DARK_PURPLE 
                        + quest.getName() + ChatColor.YELLOW);
                sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCompletedQuests().contains(quest) && getRemainingCooldown(quest) > 0 
                && !quest.getPlanner().getOverride()) {
            if (giveReason) {
                final String msg = Lang.get(getPlayer(), "questTooEarly").replace("<quest>", ChatColor.AQUA 
                        + quest.getName()+ ChatColor.YELLOW).replace("<time>", ChatColor.DARK_PURPLE 
                        + MiscUtil.getTime(getRemainingCooldown(quest)) + ChatColor.YELLOW);
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (quest.getRegionStart() != null) {
            if (!quest.isInRegionStart(this)) {
                if (giveReason) {
                    final String msg = Lang.get(getPlayer(), "questInvalidLocation").replace("<quest>", ChatColor.AQUA 
                            + quest.getName() + ChatColor.YELLOW);
                    getPlayer().sendMessage(ChatColor.YELLOW + msg);
                }
                return false;
            }
        }
        return true;
    }
    
    public boolean meetsCondition(final Quest quest, final boolean giveReason) {
        final Stage stage = getCurrentStage(quest);
        if (stage != null && stage.getCondition() != null && !stage.getCondition().check(this, quest)) {
            if (stage.getCondition().isFailQuest()) {
                if (giveReason) {
                    getPlayer().sendMessage(ChatColor.RED + Lang.get(getPlayer(), "conditionFailQuit")
                        .replace("<quest>", quest.getName()));
                }
                hardQuit(quest);
            } else if (giveReason) {
                if (System.currentTimeMillis() - lastNotifiedCondition > 6000) {
                    getPlayer().sendMessage(ChatColor.YELLOW + Lang.get(getPlayer(), "conditionFailRetry")
                            .replace("<quest>", quest.getName()));
                    lastNotifiedCondition = System.currentTimeMillis();
                }
            }
            return false;
        }
        return true;
    }
    
    public boolean isSelectingBlock() {
        final UUID uuid = getPlayer().getUniqueId();
        if (plugin.getQuestFactory().getSelectedBlockStarts().containsKey(uuid)
                || plugin.getQuestFactory().getSelectedKillLocations().containsKey(uuid)
                || plugin.getQuestFactory().getSelectedReachLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedExplosionLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedEffectLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedMobLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedLightningLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedTeleportLocations().containsKey(uuid)) {
                    return true;
        }
        return false;
    }
    
    public boolean isInRegion(final String regionID) {
        if (getPlayer() == null) {
            return false;
        }
        if (plugin.getDependencies().getWorldGuardApi().getApplicableRegionsIDs(getPlayer().getWorld(),
                getPlayer().getLocation()).contains(regionID)) {
            return true;
        }
        return false;
    }
}
