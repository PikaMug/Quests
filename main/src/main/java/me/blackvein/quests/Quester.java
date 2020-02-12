/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.alessiodp.parties.api.interfaces.Party;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;

import de.erethon.dungeonsxl.player.DGroup;
import me.blackvein.quests.events.quest.QuestTakeEvent;
import me.blackvein.quests.events.quester.QuesterPostStartQuestEvent;
import me.blackvein.quests.events.quester.QuesterPreOpenGUIEvent;
import me.blackvein.quests.events.quester.QuesterPreStartQuestEvent;
import me.blackvein.quests.tasks.StageTimer;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.InventoryUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.LocaleQuery;
import me.blackvein.quests.util.MiscUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.npc.NPC;

public class Quester {

    private Quests plugin;
    public boolean hasJournal = false;
    private UUID id;
    protected String questToTake;
    protected int questPoints = 0;
    protected ConcurrentHashMap<Integer, Quest> timers = new ConcurrentHashMap<Integer, Quest>();
    protected ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<Quest, Integer>() {

        private static final long serialVersionUID = 6361484975823846780L;

        @Override
        public Integer put(Quest key, Integer val) {
            Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(Map<? extends Quest, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    protected LinkedList<String> completedQuests = new LinkedList<String>() {

        private static final long serialVersionUID = -269110128568487000L;

        @Override
        public boolean add(String e) {
            boolean b = super.add(e);
            updateJournal();
            return b;
        }

        @Override
        public void add(int index, String element) {
            super.add(index, element);
            updateJournal();
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            boolean b = super.addAll(c);
            updateJournal();
            return b;
        }

        @Override
        public boolean addAll(int index, Collection<? extends String> c) {
            boolean b = super.addAll(index, c);
            updateJournal();
            return b;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public boolean remove(Object o) {
            boolean b = super.remove(o);
            updateJournal();
            return b;
        }

        @Override
        public String remove(int index) {
            String s = super.remove(index);
            updateJournal();
            return s;
        }

        @Override
        public String set(int index, String element) {
            String s = super.set(index, element);
            updateJournal();
            return s;
        }
    };
    protected Map<String, Long> completedTimes = new HashMap<String, Long>();
    protected Map<String, Integer> amountsCompleted = new HashMap<String, Integer>() {

        private static final long serialVersionUID = 5475202358792520975L;

        @Override
        public Integer put(String key, Integer val) {
            Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(Object key) {
            Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(Map<? extends String, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    protected Map<Quest, QuestData> questData = new HashMap<Quest, QuestData>() {

        private static final long serialVersionUID = -4607112433003926066L;

        @Override
        public QuestData put(Quest key, QuestData val) {
            QuestData data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public QuestData remove(Object key) {
            QuestData data = super.remove(key);
            updateJournal();
            return data;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(Map<? extends Quest, ? extends QuestData> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    
    public Quester(Quests newPlugin) {
        plugin = newPlugin;
    }

    public UUID getUUID() {
        return id;
    }

    public void setUUID(UUID id) {
        this.id = id;
    }

    public String getQuestToTake() {
        return questToTake;
    }

    public void setQuestToTake(String questToTake) {
        this.questToTake = questToTake;
    }

    public int getQuestPoints() {
        return questPoints;
    }

    public void setQuestPoints(int questPoints) {
        this.questPoints = questPoints;
    }

    public ConcurrentHashMap<Integer, Quest> getTimers() {
        return timers;
    }

    public void setTimers(ConcurrentHashMap<Integer, Quest> timers) {
        this.timers = timers;
    }
    
    public void removeTimer(Integer timerId) {
        this.timers.remove(timerId);
    }

    public ConcurrentHashMap<Quest, Integer> getCurrentQuests() {
        return currentQuests;
    }

    public void setCurrentQuests(ConcurrentHashMap<Quest, Integer> currentQuests) {
        this.currentQuests = currentQuests;
    }

    public LinkedList<String> getCompletedQuests() {
        return completedQuests;
    }

    public void setCompletedQuests(LinkedList<String> completedQuests) {
        this.completedQuests = completedQuests;
    }

    public Map<String, Long> getCompletedTimes() {
        return completedTimes;
    }

    public void setCompletedTimes(Map<String, Long> completedTimes) {
        this.completedTimes = completedTimes;
    }

    public Map<String, Integer> getAmountsCompleted() {
        return amountsCompleted;
    }

    public void setAmountsCompleted(Map<String, Integer> amountsCompleted) {
        this.amountsCompleted = amountsCompleted;
    }

    public Map<Quest, QuestData> getQuestData() {
        return questData;
    }

    public void setQuestData(Map<Quest, QuestData> questData) {
        this.questData = questData;
    }

    public Player getPlayer() {
        return plugin.getServer().getPlayer(id);
    }

    public OfflinePlayer getOfflinePlayer() {
        return plugin.getServer().getOfflinePlayer(id);
    }
    
    public Stage getCurrentStage(Quest quest) {
        if (currentQuests.containsKey(quest)) {
            return quest.getStage(currentQuests.get(quest));
        }
        return null;
    }

    public QuestData getQuestData(Quest quest) {
        if (questData.containsKey(quest)) {
            return questData.get(quest);
        }
        return null;
    }

    public void updateJournal() {
        if (!hasJournal) {
            return;
        }  
        if (!getPlayer().isOnline()) {
            plugin.getLogger().info("Could not update Quests Journal for " + getPlayer().getName() + " while offline");
            return;
        }
        Inventory inv = getPlayer().getInventory();
        ItemStack[] arr = inv.getContents();
        int index = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                if (ItemUtil.isJournal(arr[i])) {
                    index = i;
                    break;
                }
            }
        }
        if (index != -1) {
            ItemStack stack = new ItemStack(Material.WRITTEN_BOOK, 1);
            ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + Lang.get(getPlayer(), "journalTitle"));
            BookMeta book = (BookMeta) meta;
            book.setTitle(ChatColor.LIGHT_PURPLE + Lang.get(getPlayer(), "journalTitle"));
            book.setAuthor(getPlayer().getName());
            if (currentQuests.isEmpty()) {
                book.addPage(ChatColor.DARK_RED + Lang.get(getPlayer(), "journalNoQuests"));
            } else {
                int currentLength = 0;
                int currentLines = 0;
                String page = "";
                List<Quest> sortedList = currentQuests.keySet().stream()
                        .sorted(Comparator.comparing(Quest::getName))
                        .collect(Collectors.toList());
                for (Quest quest : sortedList) {
                    if ((currentLength + quest.getName().length() > 240) || (currentLines 
                            + ((quest.getName().length() % 19) == 0 ? (quest.getName().length() / 19) 
                            : ((quest.getName().length() / 19) + 1))) > 13) {
                        book.addPage(page);
                        page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.getName() + "\n";
                        currentLength = quest.getName().length();
                        currentLines = (quest.getName().length() % 19) == 0 ? (quest.getName().length() / 19) 
                                : (quest.getName().length() + 1);
                    } else {
                        page += ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + quest.getName() + "\n";
                        currentLength += quest.getName().length();
                        currentLines += (quest.getName().length() / 19);
                    }
                    if (getCurrentObjectives(quest, false) != null) {
                        for (String obj : getCurrentObjectives(quest, false)) {
                            // Length/Line check
                            if ((currentLength + obj.length() > 240) || (currentLines + ((obj.length() % 19) 
                                    == 0 ? (obj.length() / 19) : ((obj.length() / 19) + 1))) > 13) {
                                book.addPage(page);
                                page = obj + "\n";
                                currentLength = obj.length();
                                currentLines = (obj.length() % 19) == 0 ? (obj.length() / 19) : (obj.length() + 1);
                            } else {
                                page += obj + "\n";
                                currentLength += obj.length();
                                currentLines += (obj.length() / 19);
                            }
                        }
                    } else {
                        plugin.getLogger().severe("Quest Journal: objectives were null for " + quest.getName());
                    }
                    if (currentLines < 13)
                        page += "\n";
                    book.addPage(page);
                    page = "";
                    currentLines = 0;
                    currentLength = 0;
                }
            }
            stack.setItemMeta(book);
            inv.setItem(index, stack);
        }
    }
    
    /**
     * Start a quest for this Quester
     * 
     * @param q The quest to start
     * @param ignoreReqs Whether to ignore Requirements
     */
    public void takeQuest(Quest q, boolean ignoreReqs) {
        if (q == null) {
            return;
        }
        QuesterPreStartQuestEvent preEvent = new QuesterPreStartQuestEvent(this, q);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        OfflinePlayer player = getOfflinePlayer();
        if (player.isOnline()) {
            Player p = getPlayer();
            Planner pln = q.getPlanner();
            long start = pln.getStartInMillis(); // Start time in milliseconds since UTC epoch
            long end = pln.getEndInMillis(); // End time in milliseconds since UTC epoch
            long duration = end - start; // How long the quest can be active for
            long repeat = pln.getRepeat(); // Length to wait in-between start times
            if (start != -1) {
                if (System.currentTimeMillis() < start) {
                    String early = Lang.get("plnTooEarly");
                    early = early.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
                    early = early.replace("<time>", ChatColor.DARK_PURPLE
                            + MiscUtil.getTime(start - System.currentTimeMillis()) + ChatColor.YELLOW);
                    p.sendMessage(ChatColor.YELLOW + early);
                    return;
                }
            }
            if (end != -1 && repeat == -1) {
                if (System.currentTimeMillis() > end) {
                    String late = Lang.get("plnTooLate");
                    late = late.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.RED);
                    late = late.replace("<time>", ChatColor.DARK_PURPLE
                            + MiscUtil.getTime(System.currentTimeMillis() - end) + ChatColor.RED);
                    p.sendMessage(ChatColor.RED + late);
                    return;
                }
            }
            if (repeat != -1 && start != -1 && end != -1) {
                // Ensure that we're past the initial duration
                if (System.currentTimeMillis() > end) {
                    final int maxSize = 2;
                    final LinkedHashMap<Long, Long> cache = new LinkedHashMap<Long, Long>() {
                        private static final long serialVersionUID = 1L;
                        
                        @Override
                        protected boolean removeEldestEntry(final Map.Entry<Long, Long> eldest) {
                            return size() > maxSize;
                        }
                    };
                    
                    // Store both the upcoming and most recent period of activity
                    long nextStart = start;
                    long nextEnd = end;
                    while (System.currentTimeMillis() >= nextStart) {
                        nextStart += repeat;
                        nextEnd = nextStart + duration;
                        cache.put(nextStart, nextEnd);
                    }
                    
                    // Check whether the quest is currently active
                    boolean active = false;
                    for (Entry<Long, Long> startEnd : cache.entrySet()) {
                        if (startEnd.getKey() <= System.currentTimeMillis() && System.currentTimeMillis() 
                                < startEnd.getValue()) {
                            active = true;
                        }
                    }
                    
                    // If quest is not active, inform user of wait time
                    if (!active) {
                        String early = Lang.get("plnTooEarly");
                        early = early.replace("<quest>", ChatColor.AQUA + q.getName() + ChatColor.YELLOW);
                        early = early.replace("<time>", ChatColor.DARK_PURPLE
                                + MiscUtil.getTime(nextStart - System.currentTimeMillis()) + ChatColor.YELLOW);
                        p.sendMessage(ChatColor.YELLOW + early);
                        return;
                    }
                }
            }
        }
        if (q.testRequirements(player) == true || ignoreReqs) {
            addEmptiesFor(q, 0);
            try {
                currentQuests.put(q, 0);
            } catch (NullPointerException npe) {
                plugin.getLogger().severe("Unable to add quest" + q.getName() + " for player " + player.getName()
                        + ". Consider resetting player data or report on Github");
            }
            Stage stage = q.getStage(0);
            if (!ignoreReqs) {
                Requirements reqs = q.getRequirements();
                if (reqs.getMoney() > 0) {
                    if (plugin.getDependencies().getVaultEconomy() != null) {
                        plugin.getDependencies().getVaultEconomy().withdrawPlayer(getOfflinePlayer(), reqs.getMoney());
                    }
                }
                if (player.isOnline()) {
                    Player p = getPlayer();
                    for (ItemStack is : reqs.getItems()) {
                        if (reqs.getRemoveItems().get(reqs.getItems().indexOf(is)) == true) {
                            InventoryUtil.removeItem(p.getInventory(), is);
                        }
                    }
                    String accepted = Lang.get(getPlayer(), "questAccepted");
                    accepted = accepted.replace("<quest>", q.getName());
                    p.sendMessage(ChatColor.GREEN + accepted);
                    p.sendMessage("");
                    if (plugin.getSettings().canShowQuestTitles()) {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "title " 
                                + player.getName() + " title " + "{\"text\":\"" + Lang.get(getPlayer(), "quest") + " " 
                                + Lang.get(getPlayer(), "accepted") +  "\",\"color\":\"gold\"}");
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "title " 
                                + player.getName() + " subtitle " + "{\"text\":\"" + q.getName() 
                                + "\",\"color\":\"yellow\"}");
                    }
                }
            }
            if (player.isOnline()) {
                Player p = getPlayer();
                String msg = Lang.get(p, "questObjectivesTitle");
                msg = msg.replace("<quest>", q.getName());
                p.sendMessage(ChatColor.GOLD + msg);
                plugin.showObjectives(q, this, false);
                String stageStartMessage = stage.startMessage;
                if (stageStartMessage != null) {
                    p.sendMessage(ConfigUtil
                            .parseStringWithPossibleLineBreaks(stageStartMessage, q, getPlayer()));
                }
            }
            if (stage.chatActions.isEmpty() == false) {
                for (String chatTrigger : stage.chatActions.keySet()) {
                    questData.get(q).actionFired.put(chatTrigger, false);
                }
            }
            if (stage.commandActions.isEmpty() == false) {
                for (String commandTrigger : stage.commandActions.keySet()) {
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
            if (player.isOnline()) {
                ((Player)player).sendMessage(ChatColor.DARK_AQUA + Lang.get("requirements"));
                for (String s : getCurrentRequirements(q, false)) {
                    ((Player)player).sendMessage(ChatColor.GRAY + "- " + s);
                }
            }
        }
        if (player.isOnline()) {
            QuesterPostStartQuestEvent postEvent = new QuesterPostStartQuestEvent(this, q);
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
    public void quitQuest(Quest quest, String message) {
        quitQuest(quest, new String[] {message});
    }
    
    /**
     * End a quest for this Quester
     * 
     * @param quest The quest to start
     * @param messages Messages to inform player, can be left null or empty
     * @since 3.8.6
     */
    public void quitQuest(Quest quest, String[] messages) {
        if (quest == null) {
            return;
        }
        hardQuit(quest);
        for (String message : messages) {
            if (message != null && !message.equals("") && getPlayer().isOnline()) {
                getPlayer().sendMessage(message);
            }
        }
        saveData();
        loadData();
        updateJournal();
    }
    
    public LinkedList<String> getCurrentRequirements(Quest quest, boolean ignoreOverrides) {
        if (quest == null) {
            return new LinkedList<String>();
        }
        Requirements reqs = quest.getRequirements();
        if (!ignoreOverrides) {
            if (reqs.getFailRequirements() != null) {
                LinkedList<String> requirements = new LinkedList<String>();
                String message = ChatColor.RED + ConfigUtil.parseString(
                        ChatColor.translateAlternateColorCodes('&', reqs.getFailRequirements()), 
                        quest, getPlayer());
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                }
                requirements.add(message);
                return requirements;
            }
        }
        LinkedList<String> unfinishedRequirements = new LinkedList<String>();
        LinkedList<String> finishedRequirements = new LinkedList<String>();
        LinkedList<String> requirements = new LinkedList<String>();
        OfflinePlayer player = getPlayer();
        if (reqs.getMoney() > 0) {
            String currency = plugin.getDependencies().getCurrency(reqs.getMoney() == 1 ? false : true);
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
        for (String name : reqs.getNeededQuests()) {
            if (getCompletedQuests().contains(name)) {
                finishedRequirements.add(ChatColor.GREEN + name);
            } else {
                unfinishedRequirements.add(ChatColor.GRAY + name);
            }
        }
        for (String name : reqs.getBlockQuests()) {
            Quest questObject = new Quest();
            questObject.setName(name);
            if (completedQuests.contains(name) || currentQuests.containsKey(questObject)) {
                requirements.add(ChatColor.RED + name);
            }
        }
        for (String s : reqs.getMcmmoSkills()) {
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
            PlayerInventory inventory = getPlayer().getInventory();
            int num = 0;
            for (ItemStack is : reqs.getItems()) {
                for (ItemStack stack : inventory.getContents()) {
                    if (stack != null) {
                        if (ItemUtil.compareItems(is, stack, true) == 0) {
                            num += stack.getAmount();
                        }
                    }
                }
                if (num >= is.getAmount()) {
                    finishedRequirements.add(ChatColor.GREEN + "" + is.getAmount() + " " + ItemUtil.getName(is));
                } else {
                    unfinishedRequirements.add(ChatColor.GRAY + "" + is.getAmount() + " " + ItemUtil.getName(is));
                }
                num = 0;
            }
            for (String perm :reqs.getPermissions()) {
                if (getPlayer().hasPermission(perm)) {
                    finishedRequirements.add(ChatColor.GREEN + Lang.get("permissionDisplay") + " " + perm);
                } else {
                    unfinishedRequirements.add(ChatColor.GRAY + Lang.get("permissionDisplay") + " " + perm);
                }
                
            }
            for (Entry<String, Map<String, Object>> m : reqs.getCustomRequirements().entrySet()) {
                for (CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getName().equalsIgnoreCase(m.getKey())) {
                        if (cr != null) {
                            if (cr.testRequirement(getPlayer(), m.getValue())) {
                                finishedRequirements.add(ChatColor.GREEN + "" + m.getKey());
                            } else {
                                unfinishedRequirements.add(ChatColor.GRAY + "" + m.getKey());
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
    public LinkedList<String> getCurrentObjectives(Quest quest, boolean ignoreOverrides) {
        if (!ignoreOverrides) {
            if (getCurrentStage(quest) != null) {
                if (getCurrentStage(quest).objectiveOverride != null) {
                    LinkedList<String> objectives = new LinkedList<String>();
                    String message = ChatColor.GREEN + ConfigUtil.parseString(
                            ChatColor.translateAlternateColorCodes('&', getCurrentStage(quest).objectiveOverride), 
                            quest, getPlayer());
                    if (plugin.getDependencies().getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(getPlayer(), message);
                    }
                    objectives.add(message);
                    return objectives;
                }
            }
        }
        if (getQuestData(quest) == null) {
            return new LinkedList<String>();
        }
        LinkedList<String> unfinishedObjectives = new LinkedList<String>();
        LinkedList<String> finishedObjectives = new LinkedList<String>();
        LinkedList<String> objectives = new LinkedList<String>();
        for (ItemStack e : getCurrentStage(quest).blocksToBreak) {
            for (ItemStack e2 : getQuestData(quest).blocksBroken) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "break") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "break") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
                    }
                }
            }
        }
        for (ItemStack e : getCurrentStage(quest).blocksToDamage) {
            for (ItemStack e2 : getQuestData(quest).blocksDamaged) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "damage") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "damage") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
                    }
                }
            }
        }
        for (ItemStack e : getCurrentStage(quest).blocksToPlace) {
            for (ItemStack e2 : getQuestData(quest).blocksPlaced) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "place") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "place") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
                    }
                }
            }
        }
        for (ItemStack e : getCurrentStage(quest).blocksToUse) {
            for (ItemStack e2 : getQuestData(quest).blocksUsed) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "use") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "use") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
                    }
                }
            }
        }
        for (ItemStack e : getCurrentStage(quest).blocksToCut) {
            for (ItemStack e2 : getQuestData(quest).blocksCut) {
                if (e2.getType().equals(e.getType()) && e2.getDurability() == e.getDurability()) {
                    if (e2.getAmount() < e.getAmount()) {
                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "cut") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GREEN + ": " + e2.getAmount() + "/" + e.getAmount());
                    } else {
                        finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "cut") + " " 
                                + ItemUtil.getName(e2) + ChatColor.GRAY + ": " + e2.getAmount() + "/" + e.getAmount());
                    }
                }
            }
        }
        for (ItemStack is : getCurrentStage(quest).itemsToCraft) {
            int crafted = 0;
            if (getQuestData(quest).itemsCrafted.containsKey(is)) {
                crafted = getQuestData(quest).itemsCrafted.get(is);
            }
            int amt = is.getAmount();
            if (crafted < amt) {
                String obj = Lang.get(getPlayer(), "craft") + " " + ItemUtil.getName(is);
                unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " + crafted + "/" + amt);
            } else {
                String obj = Lang.get(getPlayer(), "craft") + " " + ItemUtil.getName(is);
                finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " + crafted + "/" + amt);
            }
        }
        for (ItemStack is : getCurrentStage(quest).itemsToSmelt) {
            int smelted = 0;
            if (getQuestData(quest).itemsSmelted.containsKey(is)) {
                smelted = getQuestData(quest).itemsSmelted.get(is);
            }
            int amt = is.getAmount();
            if (smelted < amt) {
                String obj = Lang.get(getPlayer(), "smelt") + " " + ItemUtil.getName(is);
                unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " + smelted + "/" + amt);
            } else {
                String obj = Lang.get(getPlayer(), "smelt") + " " + ItemUtil.getName(is);
                finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " + smelted + "/" + amt);
            }
        }
        Map<Enchantment, Material> set;
        Map<Enchantment, Material> set2;
        Set<Enchantment> enchantSet;
        Set<Enchantment> enchantSet2;
        Collection<Material> matSet;
        Enchantment enchantment = null;
        Enchantment enchantment2 = null;
        Material mat = null;
        int num1;
        int num2;
        for (Entry<Map<Enchantment, Material>, Integer> e : getCurrentStage(quest).itemsToEnchant.entrySet()) {
            for (Entry<Map<Enchantment, Material>, Integer> e2 : getQuestData(quest).itemsEnchanted.entrySet()) {
                set = e2.getKey();
                set2 = e.getKey();
                enchantSet = set.keySet();
                enchantSet2 = set2.keySet();
                for (Object o : enchantSet.toArray()) {
                    enchantment = (Enchantment) o;
                }
                for (Object o : enchantSet2.toArray()) {
                    enchantment2 = (Enchantment) o;
                }
                num1 = e2.getValue();
                num2 = e.getValue();
                matSet = set.values();
                for (Object o : matSet.toArray()) {
                    mat = (Material) o;
                }
                if (enchantment2 == enchantment) {
                    if (num1 < num2) {
                        String obj = Lang.get(getPlayer(), "enchantItem");
                        obj = obj.replace("<item>", ItemUtil.getName(new ItemStack(mat)) + ChatColor.GREEN);
                        obj = obj.replace("<enchantment>", ChatColor.LIGHT_PURPLE 
                                + ItemUtil.getPrettyEnchantmentName(enchantment) + ChatColor.GREEN);
                        unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " + num1 + "/" + num2);
                    } else {
                        String obj = Lang.get(getPlayer(), "enchantItem");
                        obj = obj.replace("<item>", ItemUtil.getName(new ItemStack(mat)) + ChatColor.GRAY);
                        obj = obj.replace("<enchantment>", ChatColor.LIGHT_PURPLE 
                                + ItemUtil.getPrettyEnchantmentName(enchantment) + ChatColor.GRAY);
                        finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " + num1 + "/" + num2);
                    }
                }
            }
        }
        for (ItemStack is : getCurrentStage(quest).itemsToBrew) {
            int brewed = 0;
            if (getQuestData(quest).itemsBrewed.containsKey(is)) {
                brewed = getQuestData(quest).itemsBrewed.get(is);
            }
            int amt = is.getAmount();
            if (brewed < amt) {
                String obj = Lang.get(getPlayer(), "brew") + " " + ItemUtil.getName(is);
                unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " + brewed + "/" + amt);
            } else {
                String obj = Lang.get(getPlayer(), "brew") + " " + ItemUtil.getName(is);
                finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " + brewed + "/" + amt);
            }
        }
        if (getCurrentStage(quest).cowsToMilk != null) {
            if (getQuestData(quest).getCowsMilked() < getCurrentStage(quest).cowsToMilk) {
                unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "milkCow") + ChatColor.GREEN + ": " 
            + getQuestData(quest).getCowsMilked() + "/" + getCurrentStage(quest).cowsToMilk);
            } else {
                finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "milkCow") + ChatColor.GRAY + ": " 
            + getQuestData(quest).getCowsMilked() + "/" + getCurrentStage(quest).cowsToMilk);
            }
        }
        if (getCurrentStage(quest).fishToCatch != null) {
            if (getQuestData(quest).getFishCaught() < getCurrentStage(quest).fishToCatch) {
                unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "catchFish") + ChatColor.GREEN + ": " 
            + getQuestData(quest).getFishCaught() + "/" + getCurrentStage(quest).fishToCatch);
            } else {
                finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "catchFish") + ChatColor.GRAY + ": " 
            + getQuestData(quest).getFishCaught() + "/" + getCurrentStage(quest).fishToCatch);
            }
        }
        for (EntityType e : getCurrentStage(quest).mobsToKill) {
            for (EntityType e2 : getQuestData(quest).mobsKilled) {
                if (e == e2) {
                    if (getQuestData(quest).mobNumKilled.size() > getQuestData(quest).mobsKilled.indexOf(e2) 
                            && getCurrentStage(quest).mobNumToKill.size() > getCurrentStage(quest).mobsToKill
                            .indexOf(e)) {
                        if (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled.indexOf(e2)) 
                                < getCurrentStage(quest).mobNumToKill.get(getCurrentStage(quest).mobsToKill
                                .indexOf(e))) {
                            if (getCurrentStage(quest).locationsToKillWithin.isEmpty()) {
                                unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "kill") + " " 
                                        + ChatColor.AQUA + MiscUtil.getPrettyMobName(e) + ChatColor.GREEN + ": " 
                                        + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled
                                        .indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill
                                        .get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                            } else {
                                String obj = Lang.get(getPlayer(), "killAtLocation");
                                obj = obj.replace("<mob>", ChatColor.LIGHT_PURPLE + MiscUtil.getPrettyMobName(e));
                                obj = obj.replace("<location>", getCurrentStage(quest).killNames
                                        .get(getCurrentStage(quest).mobsToKill.indexOf(e)));
                                unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " 
                                        + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled
                                        .indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill
                                        .get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                            }
                        } else {
                            if (getCurrentStage(quest).locationsToKillWithin.isEmpty()) {
                                finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "kill") + " " 
                                        + ChatColor.AQUA + MiscUtil.getPrettyMobName(e) + ChatColor.GRAY + ": " 
                                        + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled
                                        .indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill
                                        .get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                            } else {
                                String obj = Lang.get(getPlayer(), "killAtLocation");
                                obj = obj.replace("<mob>", ChatColor.LIGHT_PURPLE + MiscUtil.getPrettyMobName(e));
                                obj = obj.replace("<location>", getCurrentStage(quest).killNames
                                        .get(getCurrentStage(quest).mobsToKill.indexOf(e)));
                                finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " 
                                        + (getQuestData(quest).mobNumKilled.get(getQuestData(quest).mobsKilled
                                        .indexOf(e2))) + "/" + (getCurrentStage(quest).mobNumToKill
                                        .get(getCurrentStage(quest).mobsToKill.indexOf(e))));
                            }
                        }
                    }
                }
            }
        }
        if (getCurrentStage(quest).playersToKill != null) {
            if (getQuestData(quest).getPlayersKilled() < getCurrentStage(quest).playersToKill) {
                unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "killPlayer") + ChatColor.GREEN + ": " 
            + getQuestData(quest).getPlayersKilled() + "/" + getCurrentStage(quest).playersToKill);
            } else {
                finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "killPlayer") + ChatColor.GRAY + ": " 
            + getQuestData(quest).getPlayersKilled() + "/" + getCurrentStage(quest).playersToKill);
            }
        }
        int index = 0;
        for (ItemStack is : getCurrentStage(quest).itemsToDeliver) {
            int delivered = getQuestData(quest).itemsDelivered.get(index).getAmount();
            int toDeliver = is.getAmount();
            Integer npc = getCurrentStage(quest).itemDeliveryTargets.get(index);
            index++;
            if (delivered < toDeliver) {
                String obj = Lang.get(getPlayer(), "deliver");
                obj = obj.replace("<item>", ItemUtil.getName(is) + ChatColor.GREEN);
                obj = obj.replace("<npc>", plugin.getDependencies().getNPCName(npc));
                unfinishedObjectives.add(ChatColor.GREEN + obj + ": " + delivered + "/" + toDeliver);
            } else {
                String obj = Lang.get(getPlayer(), "deliver");
                obj = obj.replace("<item>", ItemUtil.getName(is) + ChatColor.GRAY);
                obj = obj.replace("<npc>", plugin.getDependencies().getNPCName(npc));
                finishedObjectives.add(ChatColor.GRAY + obj + ": " + delivered + "/" + toDeliver);
            }
        }
        for (Integer n : getCurrentStage(quest).citizensToInteract) {
            for (Entry<Integer, Boolean> e : getQuestData(quest).citizensInteracted.entrySet()) {
                if (e.getKey().equals(n)) {
                    if (e.getValue() == false) {
                        String obj = Lang.get(getPlayer(), "talkTo");
                        obj = obj.replace("<npc>", plugin.getDependencies().getNPCName(n));
                        unfinishedObjectives.add(ChatColor.GREEN + obj);
                    } else {
                        String obj = Lang.get(getPlayer(), "talkTo");
                        obj = obj.replace("<npc>", plugin.getDependencies().getNPCName(n));
                        finishedObjectives.add(ChatColor.GRAY + obj);
                    }
                }
            }
        }
        for (Integer n : getCurrentStage(quest).citizensToKill) {
            for (Integer n2 : getQuestData(quest).citizensKilled) {
                if (n.equals(n2)) {
                    if (getQuestData(quest).citizenNumKilled.size() > getQuestData(quest).citizensKilled.indexOf(n2) 
                            && getCurrentStage(quest).citizenNumToKill.size() > getCurrentStage(quest).citizensToKill
                            .indexOf(n)) {
                        if (getQuestData(quest).citizenNumKilled.get(getQuestData(quest).citizensKilled.indexOf(n2)) 
                                < getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill
                                .indexOf(n))) {
                            unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "kill") + " " 
                                    + plugin.getDependencies().getNPCName(n) + ChatColor.GREEN + " " 
                                    + getQuestData(quest).citizenNumKilled.get(getCurrentStage(quest).citizensToKill
                                    .indexOf(n)) + "/" + getCurrentStage(quest).citizenNumToKill
                                    .get(getCurrentStage(quest).citizensToKill.indexOf(n)));
                        } else {
                            finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "kill") + " " 
                                    + plugin.getDependencies().getNPCName(n) + ChatColor.GRAY + " " 
                                    + getCurrentStage(quest).citizenNumToKill.get(getCurrentStage(quest).citizensToKill
                                    .indexOf(n)) + "/" + getCurrentStage(quest).citizenNumToKill
                                    .get(getCurrentStage(quest).citizensToKill.indexOf(n)));
                        }
                    }
                }
            }
        }
        for (Entry<EntityType, Integer> e : getCurrentStage(quest).mobsToTame.entrySet()) {
            for (Entry<EntityType, Integer> e2 : getQuestData(quest).mobsTamed.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    if (e2.getValue() < e.getValue()) {
                        unfinishedObjectives.add(ChatColor.GREEN + Lang.get(getPlayer(), "tame") + " " 
                                + MiscUtil.getCapitalized(e.getKey().name()) 
                                + ChatColor.GREEN + ": " + e2.getValue() + "/" + e.getValue());
                    } else {
                        finishedObjectives.add(ChatColor.GRAY + Lang.get(getPlayer(), "tame") + " " 
                                + MiscUtil.getCapitalized(e.getKey().name()) + ChatColor.GRAY + ": " + e2.getValue() 
                                + "/" + e.getValue());
                    }
                }
            }
        }
        for (Entry<DyeColor, Integer> e : getCurrentStage(quest).sheepToShear.entrySet()) {
            for (Entry<DyeColor, Integer> e2 : getQuestData(quest).sheepSheared.entrySet()) {
                if (e.getKey().equals(e2.getKey())) {
                    if (e2.getValue() < e.getValue()) {
                        String obj = Lang.get(getPlayer(), "shearSheep");
                        obj = obj.replace("<color>", e.getKey().name().toLowerCase());
                        unfinishedObjectives.add(ChatColor.GREEN + obj + ChatColor.GREEN + ": " + e2.getValue() + "/" 
                                + e.getValue());
                    } else {
                        String obj = Lang.get(getPlayer(), "shearSheep");
                        obj = obj.replace("<color>", e.getKey().name().toLowerCase());
                        finishedObjectives.add(ChatColor.GRAY + obj + ChatColor.GRAY + ": " + e2.getValue() + "/" 
                                + e.getValue());
                    }
                }
            }
        }
        for (Location l : getCurrentStage(quest).locationsToReach) {
            for (Location l2 : getQuestData(quest).locationsReached) {
                if (l.equals(l2)) {
                    if (!getQuestData(quest).hasReached.isEmpty()) {
                        if (getQuestData(quest).hasReached.get(getQuestData(quest).locationsReached.indexOf(l2)) 
                                == false) {
                            String obj = Lang.get(getPlayer(), "goTo");
                            obj = obj.replace("<location>", getCurrentStage(quest).locationNames
                                    .get(getCurrentStage(quest).locationsToReach.indexOf(l)));
                            unfinishedObjectives.add(ChatColor.GREEN + obj);
                        } else {
                            String obj = Lang.get(getPlayer(), "goTo");
                            obj = obj.replace("<location>", getCurrentStage(quest).locationNames
                                    .get(getCurrentStage(quest).locationsToReach.indexOf(l)));
                            finishedObjectives.add(ChatColor.GRAY + obj);
                        }
                    }
                }
            }
        }
        for (String s : getCurrentStage(quest).passwordDisplays) {
            if (getQuestData(quest).passwordsSaid.containsKey(s)) {
                Boolean b = getQuestData(quest).passwordsSaid.get(s);
                if (b != null && !b) {
                    unfinishedObjectives.add(ChatColor.GREEN + s);
                } else {
                    finishedObjectives.add(ChatColor.GRAY + s);
                }
            }
        }
        for (CustomObjective co : getCurrentStage(quest).customObjectives) {
            int countsIndex = 0;
            String display = co.getDisplay();
            boolean addUnfinished = false;
            boolean addFinished = false;
            for (Entry<String, Integer> entry : getQuestData(quest).customObjectiveCounts.entrySet()) {
                if (co.getName().equals(entry.getKey())) {
                    int dataIndex = 0;
                    for (Entry<String,Object> prompt : co.getData()) {
                        String replacement = "%" + prompt.getKey() + "%";
                        try {
                            if (display.contains(replacement)) {
                                if (dataIndex < getCurrentStage(quest).customObjectiveData.size()) {
                                    display = display.replace(replacement, ((String) getCurrentStage(quest)
                                            .customObjectiveData.get(dataIndex).getValue()));
                                }
                            }
                        } catch (NullPointerException ne) {
                            plugin.getLogger().severe("Unable to fetch display for " + co.getName() + " on " 
                                    + quest.getName());
                            ne.printStackTrace();
                        }
                        dataIndex++;
                    }
                    if (entry.getValue() < getCurrentStage(quest).customObjectiveCounts.get(countsIndex)) {
                        if (co.canShowCount()) {
                            display = display.replace("%count%", entry.getValue() + "/" + getCurrentStage(quest)
                                    .customObjectiveCounts.get(countsIndex));
                        }
                        addUnfinished = true;
                    } else {
                        if (co.canShowCount()) {
                            display = display.replace("%count%", getCurrentStage(quest).customObjectiveCounts
                                    .get(countsIndex) + "/" + getCurrentStage(quest).customObjectiveCounts
                                    .get(countsIndex));
                        }
                        addFinished = true;
                    }
                    countsIndex++;
                }
            }
            if (addUnfinished) {
                unfinishedObjectives.add(ChatColor.GREEN + display);
            }
            if (addFinished) {
                finishedObjectives.add(ChatColor.GRAY + display);
            }
        }
        objectives.addAll(unfinishedObjectives);
        objectives.addAll(finishedObjectives);
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
    public LinkedList<String> getObjectives(Quest quest, boolean ignoreOverrides) {
        return getCurrentObjectives(quest, ignoreOverrides);
    }
    
    /**
     * Check if player's current stage has the specified objective<p>
     * 
     * Accepted strings are: breakBlock, damageBlock, placeBlock, useBlock,
     * cutBlock, craftItem, smeltItem, enchantItem, brewItem, milkCow, catchFish,
     * killMob, deliverItem, killPlayer, talkToNPC, killNPC, tameMob,
     * shearSheep, password, reachLocation
     * 
     * @deprecated Use {@link Stage#containsObjective(String)}
     * @param quest The quest to check objectives of
     * @param s The type of objective to check for
     * @return true if quest contains specified objective
     */
    public boolean containsObjective(Quest quest, String s) {
        if (quest == null || getCurrentStage(quest) == null) {
            return false;
        }
        return getCurrentStage(quest).containsObjective(s);
    }

    public boolean hasCustomObjective(Quest quest, String s) {
        if (getQuestData(quest) == null) {
            return false;
        }
        if (getQuestData(quest).customObjectiveCounts.containsKey(s)) {
            int count = getQuestData(quest).customObjectiveCounts.get(s);
            int index = -1;
            for (int i = 0; i < getCurrentStage(quest).customObjectives.size(); i++) {
                if (getCurrentStage(quest).customObjectives.get(i).getName().equals(s)) {
                    index = i;
                    break;
                }
            }
            int count2 = getCurrentStage(quest).customObjectiveCounts.get(index);
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
    public void breakBlock(Quest quest, ItemStack m) {
        ItemStack temp = m;
        temp.setAmount(0);
        ItemStack broken = temp;
        ItemStack toBreak = temp;
        for (ItemStack is : getQuestData(quest).blocksBroken) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        broken = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        broken = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    broken = is;
                }
            }
        }
        for (ItemStack is : getCurrentStage(quest).blocksToBreak) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toBreak = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        toBreak = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    toBreak = is;
                }
            }
        }
        if (broken.getAmount() < toBreak.getAmount()) {
            final ItemStack newBroken = broken;
            newBroken.setAmount(broken.getAmount() + 1);
            if (getQuestData(quest).blocksBroken.contains(broken)) {
                getQuestData(quest).blocksBroken.set(getQuestData(quest).blocksBroken.indexOf(broken), newBroken);
                if (broken.getAmount() == toBreak.getAmount()) {
                    finishObjective(quest, "breakBlock", m, toBreak, null, null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalBroken = broken;
                    final ItemStack finalToBreak = toBreak;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).blocksBroken.set(getQuestData(quest).blocksBroken
                                .indexOf(finalBroken), newBroken);
                        q.finishObjective(quest, "breakBlock", m, finalToBreak, null, null, null, null, null, null, 
                                null, null);
                        return null;
                    });
                }
            }
        }
    }
    
    /**
     * Mark block as damaged if Quester has such an objective
     * 
     * @param quest The quest for which the block is being damaged
     * @param m The block being damaged
     */
    @SuppressWarnings("deprecation")
    public void damageBlock(Quest quest, ItemStack m) {
        ItemStack temp = m;
        temp.setAmount(0);
        ItemStack damaged = temp;
        ItemStack toDamage = temp;
        for (ItemStack is : getQuestData(quest).blocksDamaged) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        damaged = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        damaged = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    damaged = is;
                }
            }
        }
        for (ItemStack is : getCurrentStage(quest).blocksToDamage) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toDamage = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        toDamage = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    toDamage = is;
                }
            }
        }
        if (damaged.getAmount() < toDamage.getAmount()) {
            final ItemStack newDamaged = damaged;
            newDamaged.setAmount(damaged.getAmount() + 1);
            if (getQuestData(quest).blocksDamaged.contains(damaged)) {
                getQuestData(quest).blocksDamaged.set(getQuestData(quest).blocksDamaged.indexOf(damaged), newDamaged);
                if (damaged.getAmount() == toDamage.getAmount()) {
                    finishObjective(quest, "damageBlock", m, toDamage, null, null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalDamaged = damaged;
                    final ItemStack finalToDamage = toDamage;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).blocksDamaged.set(getQuestData(quest).blocksDamaged
                                .indexOf(finalDamaged), newDamaged);
                        q.finishObjective(quest, "damageBlock", m, finalToDamage, null, null, null, null, null, null, 
                                null, null);
                        return null;
                    });
                }
            }
        }
    }

    /**
     * Mark block as placed if Quester has such an objective
     * 
     * @param quest The quest for which the block is being placed
     * @param m The block being placed
     */
    @SuppressWarnings("deprecation")
    public void placeBlock(Quest quest, ItemStack m) {
        ItemStack temp = m;
        temp.setAmount(0);
        ItemStack placed = temp;
        ItemStack toPlace = temp;
        for (ItemStack is : getQuestData(quest).blocksPlaced) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        placed = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        placed = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    placed = is;
                }
            }
        }
        for (ItemStack is : getCurrentStage(quest).blocksToPlace) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toPlace = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        toPlace = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    toPlace = is;
                }
            }
        }
        if (placed.getAmount() < toPlace.getAmount()) {
            final ItemStack newplaced = placed;
            newplaced.setAmount(placed.getAmount() + 1);
            if (getQuestData(quest).blocksPlaced.contains(placed)) {
                getQuestData(quest).blocksPlaced.set(getQuestData(quest).blocksPlaced.indexOf(placed), newplaced);
                if (placed.getAmount() == toPlace.getAmount()) {
                    finishObjective(quest, "placeBlock", m, toPlace, null, null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalPlaced = placed;
                    final ItemStack finalToPlace = toPlace;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).blocksPlaced.set(getQuestData(quest).blocksPlaced
                                .indexOf(finalPlaced), newplaced);
                        q.finishObjective(quest, "damageBlock", m, finalToPlace, null, null, null, null, null, null, 
                                null, null);
                        return null;
                    });
                }
            }
        }
    }

    /**
     * Mark block as used if Quester has such an objective
     * 
     * @param quest The quest for which the block is being used
     * @param m The block being used
     */
    @SuppressWarnings("deprecation")
    public void useBlock(Quest quest, ItemStack m) {
        ItemStack temp = m;
        temp.setAmount(0);
        ItemStack used = temp;
        ItemStack toUse = temp;
        for (ItemStack is : getQuestData(quest).blocksUsed) {
            if (m.getType() == is.getType() ) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        used = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        used = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    used = is;
                }
            }
        }
        for (ItemStack is : getCurrentStage(quest).blocksToUse) {
            if (m.getType() == is.getType() ) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid, so check durability
                    if (m.getDurability() == is.getDurability()) {
                        toUse = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        toUse = is;
                    }
                } else {
                    //Blocks are not solid, so ignore durability
                    toUse = is;
                }
            }
        }
        if (used.getAmount() < toUse.getAmount()) {
            final ItemStack newUsed = used;
            newUsed.setAmount(used.getAmount() + 1);
            if (getQuestData(quest).blocksUsed.contains(used)) {
                getQuestData(quest).blocksUsed.set(getQuestData(quest).blocksUsed.indexOf(used), newUsed);
                if (used.getAmount() == toUse.getAmount()) {
                    finishObjective(quest, "useBlock", m, toUse, null, null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalUsed = used;
                    final ItemStack finalToUse = toUse;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).blocksUsed.set(getQuestData(quest).blocksUsed
                                .indexOf(finalUsed), newUsed);
                        q.finishObjective(quest, "useBlock", m, finalToUse, null, null, null, null, null, null, null, 
                                null);
                        return null;
                    });
                }
            }
        }
    }

    /**
     * Mark block as cut if Quester has such an objective
     * 
     * @param quest The quest for which the block is being cut
     * @param m The block being cut
     */
    @SuppressWarnings("deprecation")
    public void cutBlock(Quest quest, ItemStack m) {
        ItemStack temp = m;
        temp.setAmount(0);
        ItemStack cut = temp;
        ItemStack toCut = temp;
        for (ItemStack is : getQuestData(quest).blocksCut) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        cut = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        cut = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    cut = is;
                }
            }
        }
        for (ItemStack is : getCurrentStage(quest).blocksToCut) {
            if (m.getType() == is.getType()) {
                if (m.getType().isSolid() && is.getType().isSolid()) {
                    //Blocks are solid so check for durability
                    if (m.getDurability() == is.getDurability()) {
                        toCut = is;
                    } else if (!LocaleQuery.isBelow113(plugin.getDetectedBukkitVersion())) {
                        //Ignore durability for 1.13+
                        toCut = is;
                    }
                } else {
                    //Blocks are not solid so ignore durability
                    toCut = is;
                }
            }
        }
        if (cut.getAmount() < toCut.getAmount()) {
            final ItemStack newCut = cut;
            newCut.setAmount(cut.getAmount() + 1);
            if (getQuestData(quest).blocksCut.contains(cut)) {
                getQuestData(quest).blocksCut.set(getQuestData(quest).blocksCut.indexOf(cut), newCut);
                if (cut.getAmount() == toCut.getAmount()) {
                    finishObjective(quest, "cutBlock", m, toCut, null, null, null, null, null, null, null, null);
                    
                    // Multiplayer
                    final ItemStack finalCut = cut;
                    final ItemStack finalToCut = toCut;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).blocksCut.set(getQuestData(quest).blocksCut.indexOf(finalCut), newCut);
                        q.finishObjective(quest, "cutBlock", m, finalToCut, null, null, null, null, null, null, null, 
                                null);
                        return null;
                    });
                }
            }
        }
    }
    
    /**
     * Mark item as crafted if Quester has such an objective
     * 
     * @param quest The quest for which the item is being crafted
     * @param i The item being crafted
     */
    public void craftItem(Quest quest, ItemStack i) {
        Player player = getPlayer();
        ItemStack found = null;
        for (ItemStack is : getQuestData(quest).itemsCrafted.keySet()) {
            if (ItemUtil.compareItems(i, is, true) == 0) {
                found = is;
                break;
            }
        }
        if (found != null) {
            int amount = getQuestData(quest).itemsCrafted.get(found);
            if (getCurrentStage(quest).itemsToCraft.indexOf(found) < 0) {
                plugin.getLogger().severe("Index out of bounds while crafting " + found.getType() + " x " 
                        + found.getAmount() + " for quest " 
                        + quest.getName() + " with " + i.getType() + " x " + i.getAmount() 
                        + " already crafted. List amount reports value of " + amount
                        + ". Please report this error on Github!");
                player.sendMessage(ChatColor.RED 
                        + "Quests had a problem crafting your item, please contact an administrator!");
                return;
            }
            int req = getCurrentStage(quest).itemsToCraft.get(getCurrentStage(quest).itemsToCraft.indexOf(found))
                    .getAmount();
            Material m = i.getType();
            if (amount < req) {
                if ((i.getAmount() + amount) >= req) {
                    getQuestData(quest).itemsCrafted.put(found, req);
                    finishObjective(quest, "craftItem", new ItemStack(m, 1), found, null, null, null, null, null, null, 
                            null, null);
                    
                    // Multiplayer
                    final ItemStack finalFound = found;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).itemsCrafted.put(finalFound, req);
                        q.finishObjective(quest, "craftItem", new ItemStack(m, 1), finalFound, null, null, null, null, 
                                null, null, null, null);
                        return null;
                    });
                } else {
                    getQuestData(quest).itemsCrafted.put(found, (amount + i.getAmount()));
                }
            }
        }
    }
    
    /**
     * Mark item as smelted if Quester has such an objective
     * 
     * @param quest The quest for which the item is being smelted
     * @param i The item being smelted
     */
    public void smeltItem(Quest quest, ItemStack i) {
        Player player = getPlayer();
        ItemStack found = null;
        for (ItemStack is : getQuestData(quest).itemsSmelted.keySet()) {
            if (ItemUtil.compareItems(i, is, true) == 0) {
                found = is;
                break;
            }
        }
        if (found != null) {
            int amount = getQuestData(quest).itemsSmelted.get(found);
            if (getCurrentStage(quest).itemsToSmelt.indexOf(found) < 0) {
                plugin.getLogger().severe("Index out of bounds while smelting " + found.getType() + " x " 
                        + found.getAmount() + " for quest " + quest.getName() + " with " + i.getType() + " x " 
                        + i.getAmount() + " already smelted. List amount reports value of " + amount 
                        + ". Please report this error on Github!");
                player.sendMessage(ChatColor.RED 
                        + "Quests had a problem smelting your item, please contact an administrator!");
                return;
            }
            int req = getCurrentStage(quest).itemsToSmelt.get(getCurrentStage(quest).itemsToSmelt.indexOf(found))
                    .getAmount();
            Material m = i.getType();
            if (amount < req) {
                if ((i.getAmount() + amount) >= req) {
                    getQuestData(quest).itemsSmelted.put(found, req);
                    finishObjective(quest, "smeltItem", new ItemStack(m, 1), found, null, null, null, null, null, null, 
                            null, null);
                    
                    // Multiplayer
                    final ItemStack finalFound = found;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).itemsSmelted.put(finalFound, req);
                        q.finishObjective(quest, "smeltItem", new ItemStack(m, 1), finalFound, null, null, null, null, 
                                null, null, null, null);
                        return null;
                    });
                } else {
                    getQuestData(quest).itemsSmelted.put(found, (amount + i.getAmount()));
                }
            }
        }
    }

    /**
     * Mark item as enchanted if Quester has such an objective
     * 
     * @param quest The quest for which the item is being enchanted
     * @param e The enchantment to be applied
     * @param m The item being enchanted
     */
    public void enchantItem(Quest quest, Enchantment e, Material m) {
        for (Entry<Map<Enchantment, Material>, Integer> entry : getQuestData(quest).itemsEnchanted.entrySet()) {
            if (entry.getKey().containsKey(e) && entry.getKey().containsValue(m)) {
                for (Entry<Map<Enchantment, Material>, Integer> entry2 : getCurrentStage(quest).itemsToEnchant
                        .entrySet()) {
                    if (entry2.getKey().containsKey(e) && entry2.getKey().containsValue(m)) {
                        if (entry.getValue() < entry2.getValue()) {
                            Integer num = entry.getValue() + 1;
                            getQuestData(quest).itemsEnchanted.put(entry.getKey(), num);
                            if (num.equals(entry2.getValue())) {
                                final ItemStack finalToEnchant = new ItemStack(m, entry.getValue());
                                
                                finishObjective(quest, "enchantItem", new ItemStack(m, 1), finalToEnchant, e, null, 
                                        null, null, null, null, null, null);
                                
                                // Multiplayer
                                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                                    q.getQuestData(quest).itemsEnchanted.put(entry.getKey(), num);
                                    q.finishObjective(quest, "enchantItem", new ItemStack(m, 1), finalToEnchant, null, 
                                            null, null, null, null, null, null, null);
                                    return null;
                                });
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Mark item as brewed if Quester has such an objective
     * 
     * @param quest The quest for which the item is being brewed
     * @param i The item being brewed
     */
    public void brewItem(Quest quest, ItemStack i) {
        Player player = getPlayer();
        ItemStack found = null;
        for (ItemStack is : getQuestData(quest).itemsBrewed.keySet()) {
            if (ItemUtil.compareItems(i, is, true) == 0) {
                found = is;
                break;
            }
        }
        if (found != null) {
            int amount = getQuestData(quest).itemsBrewed.get(found);
            if (getCurrentStage(quest).itemsToBrew.indexOf(found) < 0) {
                plugin.getLogger().severe("Index out of bounds while brewing " + found.getType() + " x " 
                        + found.getAmount() + " for quest " + quest.getName() + " with " + i.getType() + " x " 
                        + i.getAmount() + " already smelted. List amount reports value of " +  amount 
                        + ". Please report this error on Github!");
                player.sendMessage(ChatColor.RED 
                        + "Quests had a problem brewing your item, please contact an administrator!");
                return;
            }
            int req = getCurrentStage(quest).itemsToBrew.get(getCurrentStage(quest).itemsToBrew.indexOf(found))
                    .getAmount();
            Material m = i.getType();
            if (amount < req) {
                if ((i.getAmount() + amount) >= req) {
                    getQuestData(quest).itemsBrewed.put(found, req);
                    finishObjective(quest, "brewItem", new ItemStack(m, 1), found, null, null, null, null, null, null, 
                            null, null);
                    
                    // Multiplayer
                    final ItemStack finalFound = found;
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).itemsBrewed.put(finalFound, req);
                        q.finishObjective(quest, "brewItem", new ItemStack(m, 1), finalFound, null, null, null, null, 
                                null, null, null, null);
                        return null;
                    });
                } else {
                    getQuestData(quest).itemsBrewed.put(found, (amount + i.getAmount()));
                }
            }
        }
    }
    
    /**
     * Mark cow as milked if Quester has such an objective
     * 
     * @param quest The quest for which the fish is being caught
     */
    public void milkCow(Quest quest) {
        final int cowsToMilk = getCurrentStage(quest).cowsToMilk;
        if (getQuestData(quest).getCowsMilked() < cowsToMilk) {
            getQuestData(quest).setCowsMilked(getQuestData(quest).getCowsMilked() + 1);
            
            if (getQuestData(quest).getCowsMilked() == cowsToMilk) {
                finishObjective(quest, "milkCow", new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, cowsToMilk), null, null, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                    q.getQuestData(quest).setCowsMilked(cowsToMilk);
                    q.finishObjective(quest, "milkCow", new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, cowsToMilk), null, null, null, null, null, null, null, null);
                    return null;
                });
            }
        }
    }
    
    /**
     * Mark fish as caught if Quester has such an objective
     * 
     * @param quest The quest for which the fish is being caught
     */
    public void catchFish(Quest quest) {
        final int fishToCatch = getCurrentStage(quest).fishToCatch;
        if (getQuestData(quest).getFishCaught() < fishToCatch) {
            getQuestData(quest).setFishCaught(getQuestData(quest).getFishCaught() + 1);
            
            if (getQuestData(quest).getFishCaught() == fishToCatch) {
                finishObjective(quest, "catchFish", new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, fishToCatch), null, null, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                    q.getQuestData(quest).setFishCaught(fishToCatch);
                    q.finishObjective(quest, "catchFish", new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, fishToCatch), null, null, null, null, null, null, null, null);
                    return null;
                });
            }
        }
    }

    /**
     * Mark mob as killed if Quester has such an objective
     * 
     * @param quest The quest for which the mob is being killed
     * @param killedLocation The optional location to kill at
     * @param e The mob to be killed
     */
    public void killMob(Quest quest, Location killedLocation, EntityType e) {
        QuestData questData = getQuestData(quest);
        if (e == null) {
            return;
        }
        Stage currentStage = getCurrentStage(quest);
        if (currentStage.mobsToKill.contains(e) == false) {
            return;
        }
        int indexOfMobKilled = questData.mobsKilled.indexOf(e);
        int numberOfSpecificMobKilled = questData.mobNumKilled.get(indexOfMobKilled);
        int numberOfSpecificMobNeedsToBeKilledInCurrentStage = currentStage.mobNumToKill.get(indexOfMobKilled);
        if (questData.locationsToKillWithin.isEmpty() == false) {
            Location locationToKillWithin = questData.locationsToKillWithin.get(indexOfMobKilled);
            double radius = questData.radiiToKillWithin.get(indexOfMobKilled);
            // Check world #name, not the object
            if ((killedLocation.getWorld().getName().equals(locationToKillWithin.getWorld().getName())) == false) {
                return;
            }
            // Radius check, it's a "circle", not cuboid
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
        if (numberOfSpecificMobKilled < numberOfSpecificMobNeedsToBeKilledInCurrentStage) {
            int newNumberOfSpecificMobKilled = numberOfSpecificMobKilled + 1;
            questData.mobNumKilled.set(indexOfMobKilled, newNumberOfSpecificMobKilled);
            if (newNumberOfSpecificMobKilled == numberOfSpecificMobNeedsToBeKilledInCurrentStage) {
                finishObjective(quest, "killMob", new ItemStack(Material.AIR, 1),
                        new ItemStack(Material.AIR, numberOfSpecificMobNeedsToBeKilledInCurrentStage), null, e, null, 
                        null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, currentStage, (Quester q) -> {
                    q.getQuestData(quest).mobNumKilled.set(indexOfMobKilled, newNumberOfSpecificMobKilled);
                    q.finishObjective(quest, "killMob", new ItemStack(Material.AIR, 1),
                            new ItemStack(Material.AIR, numberOfSpecificMobNeedsToBeKilledInCurrentStage), null, e, 
                            null, null, null, null, null, null);
                    return null;
                });
            }
        }
    }

    /**
     * Mark player as killed if Quester has such an objective
     * 
     * @param quest The quest for which the player is being killed
     * @param player The player to be killed
     */
    public void killPlayer(Quest quest, Player player) {
        final int playersToKill = getCurrentStage(quest).playersToKill;
        if (getQuestData(quest).getPlayersKilled() < playersToKill) {
            getQuestData(quest).setPlayersKilled(getQuestData(quest).getPlayersKilled() + 1);
            if (getQuestData(quest).getPlayersKilled() == playersToKill) {
                finishObjective(quest, "killPlayer", new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, playersToKill), null, null, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                    q.getQuestData(quest).setPlayersKilled(getQuestData(quest).getPlayersKilled());
                    q.finishObjective(quest, "killPlayer", new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, playersToKill), null, null, null, null, null, null, null, null);
                    return null;
                });
            }
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
    public void deliverToNPC(Quest quest, NPC n, ItemStack i) {
        if (n == null) {
            return;
        }
        int currentIndex = -1;
        LinkedList<Integer> matches = new LinkedList<Integer>();
        for (ItemStack is : getQuestData(quest).itemsDelivered) {
            currentIndex++;
            if (ItemUtil.compareItems(i, is, true) == 0) {
                matches.add(currentIndex);
            }
        }
        
        if (!matches.isEmpty()) {
            Player player = getPlayer();
            for (Integer match : matches) {
                LinkedList<ItemStack> items = new LinkedList<ItemStack>(getQuestData(quest).itemsDelivered);
                if (!getCurrentStage(quest).getItemDeliveryTargets().get(match).equals(n.getId())) {
                    continue;
                }
                ItemStack found = items.get(match);
                int amount = found.getAmount();
                int toDeliver = getCurrentStage(quest).itemsToDeliver.get(match).getAmount();
                
                Material m = i.getType();
                if (amount < toDeliver) {
                    int index = player.getInventory().first(i);
                    if (index == -1) {
                        // Already delivered in previous loop
                        return;
                    }
                    if ((i.getAmount() + amount) >= toDeliver) {
                        ItemStack newStack = found;
                        found.setAmount(toDeliver);
                        getQuestData(quest).itemsDelivered.set(items.indexOf(found), newStack);
                        if ((i.getAmount() + amount) >= toDeliver) {
                            // Take away remaining amount to be delivered
                            ItemStack clone = i.clone();
                            clone.setAmount(i.getAmount() - (toDeliver - amount));
                            player.getInventory().setItem(index, clone);
                        } else {
                            player.getInventory().setItem(index, null);
                        }
                        player.updateInventory();
                        finishObjective(quest, "deliverItem", new ItemStack(m, 1), found, null, null, null, null, null, 
                                null, null, null);
                        
                        // Multiplayer
                        dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                            q.getQuestData(quest).itemsDelivered.set(items.indexOf(found), newStack);
                            q.finishObjective(quest, "deliverItem", new ItemStack(m, 1), found, null, null, null, null, 
                                    null, null, null, null);
                            return null;
                        });
                    } else {
                        ItemStack newStack = found;
                        found.setAmount(amount + i.getAmount());
                        getQuestData(quest).itemsDelivered.set(items.indexOf(found), newStack);
                        player.getInventory().setItem(index, null);
                        player.updateInventory();
                        String[] message = ConfigUtil.parseStringWithPossibleLineBreaks(getCurrentStage(quest)
                                .deliverMessages.get(new Random().nextInt(getCurrentStage(quest).deliverMessages
                                .size())), plugin.getDependencies().getCitizens().getNPCRegistry()
                                .getById(getCurrentStage(quest).itemDeliveryTargets.get(items.indexOf(found))));
                        player.sendMessage(message);
                    }
                }
            }
        }
    }
    
    /**
     * Mark NPC as interacted with if Quester has such an objective
     * 
     * @param quest The quest for which the NPC is being interacted with
     * @param n The NPC being interacted with
     */
    public void interactWithNPC(Quest quest, NPC n) {
        if (getQuestData(quest).citizensInteracted.containsKey(n.getId())) {
            Boolean b = getQuestData(quest).citizensInteracted.get(n.getId());
            if (b != null && !b) {
                getQuestData(quest).citizensInteracted.put(n.getId(), true);
                finishObjective(quest, "talkToNPC", new ItemStack(Material.AIR, 1), new ItemStack(Material.AIR, 1), 
                        null, null, null, n, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                    q.getQuestData(quest).citizensInteracted.put(n.getId(), true);
                    q.finishObjective(quest, "talkToNPC", new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, 1), null, null, null, n, null, null, null, null);
                    return null;
                });
            }
        }
    }

    /**
     * Mark NPC as killed if the Quester has such an objective
     * 
     * @param quest The quest for which the NPC is being killed
     * @param n The NPC being killed
     */
    public void killNPC(Quest quest, NPC n) {
        if (getQuestData(quest).citizensKilled.contains(n.getId())) {
            int index = getQuestData(quest).citizensKilled.indexOf(n.getId());
            final int npcsToKill = getCurrentStage(quest).citizenNumToKill.get(index);
            if (getQuestData(quest).citizenNumKilled.get(index) < npcsToKill) {
                getQuestData(quest).citizenNumKilled.set(index, getQuestData(quest).citizenNumKilled.get(index) + 1);
                if (getQuestData(quest).citizenNumKilled.get(index).equals(npcsToKill)) {
                    finishObjective(quest, "killNPC", new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, npcsToKill), null, null, null, n, null, null, null, null);
                    
                    // Multiplayer
                    dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                        q.getQuestData(quest).citizenNumKilled.set(index, getQuestData(quest).citizenNumKilled
                                .get(index));
                        q.finishObjective(quest, "killNPC", new ItemStack(Material.AIR, 1), 
                                new ItemStack(Material.AIR, npcsToKill), null, null, null, n, null, null, null, null);
                        return null;
                    });
                }
            }
        }
    }

    /**
     * Mark location as reached if the Quester has such an objective
     * 
     * @param quest The quest for which the location is being reached
     * @param l The location being reached
     */
    public void reachLocation(Quest quest, Location l) {
        if (getQuestData(quest).locationsReached == null) {
            return;
        }
        if (getQuestData(quest).locationsReached.isEmpty()) {
            return;
        }
        int index = 0;
        for (Location location : getQuestData(quest).locationsReached) {
            try {
                if (getCurrentStage(quest).locationsToReach.size() <= index) {
                    return;
                }
                Location locationToReach = getCurrentStage(quest).locationsToReach.get(index);
                double radius = getQuestData(quest).radiiToReachWithin.get(index);
                if (l.getX() < (locationToReach.getX() + radius) && l.getX() > (locationToReach.getX() - radius)) {
                    if (l.getZ() < (locationToReach.getZ() + radius) && l.getZ() > (locationToReach.getZ() - radius)) {
                        if (l.getY() < (locationToReach.getY() + radius) && l.getY() 
                                > (locationToReach.getY() - radius)) {
                            if (l.getWorld().getName().equals(locationToReach.getWorld().getName())) {
                                // TODO - Find proper cause of Github issues #646 and #825
                                if (index >= getQuestData(quest).hasReached.size()) {
                                    getQuestData(quest).hasReached.add(true);
                                    finishObjective(quest, "reachLocation", new ItemStack(Material.AIR, 1), 
                                            new ItemStack(Material.AIR, 1), null, null, null, null, location, null, 
                                            null, null);
                                } else if (getQuestData(quest).hasReached.get(index) == false) {
                                    getQuestData(quest).hasReached.set(index, true);
                                    finishObjective(quest, "reachLocation", new ItemStack(Material.AIR, 1), 
                                            new ItemStack(Material.AIR, 1), null, null, null, null, location, null, 
                                            null, null);
                                }
                                
                                // Multiplayer
                                final int finalIndex = index;
                                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                                    if (finalIndex >= getQuestData(quest).hasReached.size()) {
                                        q.getQuestData(quest).hasReached.add(true);
                                        q.finishObjective(quest, "reachLocation", new ItemStack(Material.AIR, 1), 
                                                new ItemStack(Material.AIR, 1), null, null, null, null, location, null, 
                                                null, null);
                                    } else {
                                        q.getQuestData(quest).hasReached.set(finalIndex, true);
                                        q.finishObjective(quest, "reachLocation", new ItemStack(Material.AIR, 1), 
                                                new ItemStack(Material.AIR, 1), null, null, null, null, location, null, 
                                                null, null);
                                    }
                                    return null;
                                });
                            }
                        }
                    }
                }
                index++;
                
            } catch (IndexOutOfBoundsException e) {
                plugin.getLogger().severe("An error has occurred with Quests. Please report on Github with info below");
                plugin.getLogger().warning("index = " + index);
                plugin.getLogger().warning("currentLocation = " + location.toString());
                plugin.getLogger().warning("locationsReached = " + getQuestData(quest).locationsReached.size());
                plugin.getLogger().warning("hasReached = " + getQuestData(quest).hasReached.size());
                e.printStackTrace();
            }
        }
    }

    /**
     * Mark mob as tamed if the Quester has such an objective
     * 
     * @param quest The quest for which the mob is being tamed
     * @param entity The mob being tamed
     */
    public void tameMob(Quest quest, EntityType entity) {
        if (getQuestData(quest).mobsTamed.containsKey(entity)) {
            getQuestData(quest).mobsTamed.put(entity, (getQuestData(quest).mobsTamed.get(entity) + 1));
            final int mobsToTame = getCurrentStage(quest).mobsToTame.get(entity);
            if (getQuestData(quest).mobsTamed.get(entity).equals(mobsToTame)) {
                finishObjective(quest, "tameMob", new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, mobsToTame), null, entity, null, null, null, null, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                    q.getQuestData(quest).mobsTamed.put(entity, getQuestData(quest).mobsTamed.get(entity));
                    q.finishObjective(quest, "tameMob", new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, mobsToTame), null, entity, null, null, null, null, null, null);
                    return null;
                });
            }
        }
    }

    /**
     * Mark sheep as sheared if the Quester has such an objective
     * 
     * @param quest The quest for which the sheep is being sheared
     * @param color The wool color of the sheep being sheared
     */
    public void shearSheep(Quest quest, DyeColor color) {
        if (getQuestData(quest).sheepSheared.containsKey(color)) {
            getQuestData(quest).sheepSheared.put(color, (getQuestData(quest).sheepSheared.get(color) + 1));
            final int sheepToShear = getCurrentStage(quest).sheepToShear.get(color);
            if (getQuestData(quest).sheepSheared.get(color).equals(sheepToShear)) {
                finishObjective(quest, "shearSheep", new ItemStack(Material.AIR, 1), 
                        new ItemStack(Material.AIR, sheepToShear), null, null, null, null, null, color, null, null);
                
                // Multiplayer
                dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                    q.getQuestData(quest).sheepSheared.put(color, getQuestData(quest).sheepSheared.get(color));
                    q.finishObjective(quest, "shearSheep", new ItemStack(Material.AIR, 1), 
                            new ItemStack(Material.AIR, sheepToShear), null, null, null, null, null, color, null, null);
                    return null;
                });
            }
        }
    }

    /**
     * Mark password as entered if the Quester has such an objective
     * 
     * @param quest The quest for which the password is being entered
     * @param evt The event during which the password was entered
     */
    public void sayPassword(Quest quest, AsyncPlayerChatEvent evt) {
        boolean done;
        for (LinkedList<String> passes : getCurrentStage(quest).passwordPhrases) {
            done = false;
            for (String pass : passes) {
                if (pass.equalsIgnoreCase(evt.getMessage())) {
                    evt.setCancelled(true);
                    String display = getCurrentStage(quest).passwordDisplays.get(getCurrentStage(quest).passwordPhrases
                            .indexOf(passes));
                    getQuestData(quest).passwordsSaid.put(display, true);
                    done = true;
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        finishObjective(quest, "password", new ItemStack(Material.AIR, 1), 
                                new ItemStack(Material.AIR, 1), null, null, null, null, null, null, display, null);
                        
                        // Multiplayer
                        dispatchMultiplayerObjectives(quest, getCurrentStage(quest), (Quester q) -> {
                            q.getQuestData(quest).passwordsSaid.put(display, true);
                            q.finishObjective(quest, "password", new ItemStack(Material.AIR, 1), 
                                    new ItemStack(Material.AIR, 1), null, null, null, null, null, null, display, null);
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
    }

    /**
     * Complete quest objective
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
    @SuppressWarnings("deprecation")
    public void finishObjective(Quest quest, String objective, ItemStack increment, ItemStack goal, 
            Enchantment enchantment, EntityType mob, String extra, NPC npc, Location location, DyeColor color, 
            String pass, CustomObjective co) {
        Player p = getPlayer();
        if (getCurrentStage(quest).objectiveOverride != null) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " 
                    + ConfigUtil.parseString(ChatColor.translateAlternateColorCodes('&', 
                    getCurrentStage(quest).objectiveOverride), quest, p);
            if (plugin.getDependencies().getPlaceholderApi() != null) {
                message = PlaceholderAPI.setPlaceholders(p, message);
            }
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("password")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + pass;
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("breakBlock")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "break") + " <item>";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, increment.getType(), increment.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (objective.equalsIgnoreCase("damageBlock")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "damage") 
                    + " <item>";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, increment.getType(), increment.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (objective.equalsIgnoreCase("placeBlock")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "place") + " <item>";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, increment.getType(), increment.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (objective.equalsIgnoreCase("useBlock")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "use") + " <item>";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, increment.getType(), increment.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (objective.equalsIgnoreCase("cutBlock")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "cut") + " <item>";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, increment.getType(), increment.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(increment)));
            }
        } else if (objective.equalsIgnoreCase("craftItem")) {
            ItemStack is = getCurrentStage(quest).itemsToCraft.get(getCurrentStage(quest).itemsToCraft.indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "craft") + " <item> "
                    + is.getAmount() + "/" + is.getAmount();
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, goal.getType(), goal.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (objective.equalsIgnoreCase("smeltItem")) {
            ItemStack is = getCurrentStage(quest).itemsToSmelt.get(getCurrentStage(quest).itemsToSmelt.indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "smelt") + " <item> "
                    + is.getAmount() + "/" + is.getAmount();
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, goal.getType(), goal.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (objective.equalsIgnoreCase("enchantItem")) {
            String obj = Lang.get(p, "enchantItem");
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
            Map<Enchantment, Integer> ench = new HashMap<Enchantment, Integer>();
            ench.put(enchantment, enchantment.getStartLevel());
            for (Map<Enchantment, Material> map : getCurrentStage(quest).itemsToEnchant.keySet()) {
                if (map.containsKey(enchantment)) {
                    message = message + " " + goal.getAmount() + "/" + goal.getAmount();
                    break;
                }
            }
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, increment.getType(), increment.getDurability(), ench);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(increment))
                        .replace("<enchantment>", enchantment.getName()));
            }
        } else if (objective.equalsIgnoreCase("brewItem")) {
            ItemStack is = getCurrentStage(quest).itemsToBrew.get(getCurrentStage(quest).itemsToBrew.indexOf(goal));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "brew") + " <item> "
                    + is.getAmount() + "/" + is.getAmount();
            if (plugin.getSettings().canTranslateNames() && goal.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, goal.getType(), goal.getDurability(), null, 
                        goal.getItemMeta());
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (objective.equalsIgnoreCase("deliverItem")) {
            String obj = Lang.get(p, "deliver");
            obj = obj.replace("<npc>", plugin.getDependencies().getNPCName(getCurrentStage(quest).itemDeliveryTargets
                    .get(getCurrentStage(quest).itemsToDeliver.indexOf(goal))));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
            ItemStack is = getCurrentStage(quest).itemsToDeliver.get(getCurrentStage(quest).itemsToDeliver
                    .indexOf(goal));
            if (plugin.getSettings().canTranslateNames() && !increment.hasItemMeta() 
                    && !increment.getItemMeta().hasDisplayName()) {
                plugin.getLocaleQuery().sendMessage(p, message, is.getType(), is.getDurability(), null);
            } else {
                p.sendMessage(message.replace("<item>", ItemUtil.getName(is)));
            }
        } else if (objective.equalsIgnoreCase("milkCow")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "milkCow") + " ";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("catchFish")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "catchFish") + " ";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("killMob")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "kill") + " <mob>";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            if (plugin.getSettings().canTranslateNames()) {
                plugin.getLocaleQuery().sendMessage(p, message, mob, extra);
            } else {
                p.sendMessage(message.replace("<mob>", MiscUtil.getProperMobName(mob)));
            }
        } else if (objective.equalsIgnoreCase("killPlayer")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "killPlayer");
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("talkToNPC")) {
            String obj = Lang.get(p, "talkTo");
            obj = obj.replace("<npc>", plugin.getDependencies().getNPCName(npc.getId()));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("killNPC")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "kill") + " " 
                    + npc.getName();
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("tameMob")) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + Lang.get(p, "tame") + " <mob>";
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            if (plugin.getSettings().canTranslateNames()) {
                plugin.getLocaleQuery().sendMessage(p, message, mob, extra);
            } else {
                p.sendMessage(message.replace("<mob>", MiscUtil.getProperMobName(mob)));
            }
        } else if (objective.equalsIgnoreCase("shearSheep")) {
            String obj = Lang.get(p, "shearSheep");
            obj = obj.replace("<color>", color.name().toLowerCase());
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
            message = message + " " + goal.getAmount() + "/" + goal.getAmount();
            p.sendMessage(message);
        } else if (objective.equalsIgnoreCase("reachLocation")) {
            String obj = Lang.get(p, "goTo");
            obj = obj.replace("<location>", getCurrentStage(quest).locationNames.get(getCurrentStage(quest)
                    .locationsToReach.indexOf(location)));
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + obj;
            p.sendMessage(message);
        } else if (co != null) {
            String message = ChatColor.GREEN + "(" + Lang.get(p, "completed") + ") " + co.getDisplay();
            int index = -1;
            for (int i = 0; i < getCurrentStage(quest).customObjectives.size(); i++) {
                if (getCurrentStage(quest).customObjectives.get(i).getName().equals(co.getName())) {
                    index = i;
                    break;
                }
            }
            List<Entry<String, Object>> sub = new LinkedList<>();
            sub.addAll(getCurrentStage(quest).customObjectiveData.subList(index, getCurrentStage(quest)
                    .customObjectiveData.size()));
            List<Entry<String, Object>> end = new LinkedList<Entry<String, Object>>(sub);
            sub.clear(); // since sub is backed by end, this removes all sub-list items from end
            for (Entry<String, Object> datamap : end) {
                message = message.replace("%" + (String.valueOf(datamap.getKey())) + "%", String.valueOf(datamap
                        .getValue()));
            }
            
            if (co.canShowCount()) {
                message = message.replace("%count%", goal.getAmount() + "/" + goal.getAmount());
            }
            p.sendMessage(message);
        }
        if (testComplete(quest)) {
            quest.nextStage(this, true);
        }
    }
    
    /**
     * Check whether this Quester has completed all objectives for their current stage
     * 
     * @param quest The quest with the current stage being checked
     * @return true if all stage objectives are marked complete
     */
    public boolean testComplete(Quest quest) {
        for (String s : getObjectives(quest, true)) {
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
    public void addEmptiesFor(Quest quest, int stage) {
        QuestData data = new QuestData(this);
        data.setDoJournalUpdate(false);
        if (quest.getStage(stage).blocksToBreak.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToBreak) {
                ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksBroken.indexOf(i) != -1) {
                    data.blocksBroken.set(data.blocksBroken.indexOf(temp), temp);
                } else {
                    data.blocksBroken.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToDamage.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToDamage) {
                ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksDamaged.indexOf(i) != -1) {
                    data.blocksDamaged.set(data.blocksDamaged.indexOf(temp), temp);
                } else {
                    data.blocksDamaged.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToPlace.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToPlace) {
                ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksPlaced.indexOf(i) != -1) {
                    data.blocksPlaced.set(data.blocksPlaced.indexOf(temp), temp);
                } else {
                    data.blocksPlaced.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToUse.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToUse) {
                ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksUsed.indexOf(i) != -1) {
                    data.blocksUsed.set(data.blocksUsed.indexOf(temp), temp);
                } else {
                    data.blocksUsed.add(temp);
                }
            }
        }
        if (quest.getStage(stage).blocksToCut.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).blocksToCut) {
                ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                if (data.blocksCut.indexOf(i) != -1) {
                    data.blocksCut.set(data.blocksCut.indexOf(temp), temp);
                } else {
                    data.blocksCut.add(temp);
                }
            }
        }
        if (quest.getStage(stage).itemsToCraft.isEmpty() == false) {
            for (ItemStack is : quest.getStage(stage).itemsToCraft) {
                data.itemsCrafted.put(is, 0);
            }
        }
        if (quest.getStage(stage).itemsToSmelt.isEmpty() == false) {
            for (ItemStack is : quest.getStage(stage).itemsToSmelt) {
                data.itemsSmelted.put(is, 0);
            }
        }
        if (quest.getStage(stage).itemsToEnchant.isEmpty() == false) {
            for (Entry<Map<Enchantment, Material>, Integer> e : quest.getStage(stage).itemsToEnchant.entrySet()) {
                Map<Enchantment, Material> map = (Map<Enchantment, Material>) e.getKey();
                data.itemsEnchanted.put(map, 0);
            }
        }
        if (quest.getStage(stage).itemsToBrew.isEmpty() == false) {
            for (ItemStack is : quest.getStage(stage).itemsToBrew) {
                data.itemsBrewed.put(is, 0);
            }
        }
        if (quest.getStage(stage).mobsToKill.isEmpty() == false) {
            for (EntityType e : quest.getStage(stage).mobsToKill) {
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
        if (quest.getStage(stage).itemsToDeliver.isEmpty() == false) {
            for (ItemStack i : quest.getStage(stage).itemsToDeliver) {
                ItemStack temp = new ItemStack(i.getType(), 0, i.getDurability());
                try {
                    temp.addEnchantments(i.getEnchantments());
                } catch (Exception e) {
                    plugin.getLogger().warning("Unable to add enchantment(s) " + i.getEnchantments().toString()
                            + " to delivery item " + i.getType().name() + " x 0 for quest " + quest.getName());
                }
                temp.setItemMeta(i.getItemMeta());
                data.itemsDelivered.add(temp);
            }
        }
        if (quest.getStage(stage).citizensToInteract.isEmpty() == false) {
            for (Integer n : quest.getStage(stage).citizensToInteract) {
                data.citizensInteracted.put(n, false);
            }
        }
        if (quest.getStage(stage).citizensToKill.isEmpty() == false) {
            for (Integer n : quest.getStage(stage).citizensToKill) {
                data.citizensKilled.add(n);
                data.citizenNumKilled.add(0);
            }
        }
        if (quest.getStage(stage).locationsToReach.isEmpty() == false) {
            for (Location l : quest.getStage(stage).locationsToReach) {
                data.locationsReached.add(l);
                data.hasReached.add(false);
                data.radiiToReachWithin.add(quest.getStage(stage).radiiToReachWithin.get(data.locationsReached
                        .indexOf(l)));
            }
        }
        if (quest.getStage(stage).mobsToTame.isEmpty() == false) {
            for (EntityType e : quest.getStage(stage).mobsToTame.keySet()) {
                data.mobsTamed.put(e, 0);
            }
        }
        if (quest.getStage(stage).sheepToShear.isEmpty() == false) {
            for (DyeColor d : quest.getStage(stage).sheepToShear.keySet()) {
                data.sheepSheared.put(d, 0);
            }
        }
        if (quest.getStage(stage).passwordDisplays.isEmpty() == false) {
            for (String display : quest.getStage(stage).passwordDisplays) {
                data.passwordsSaid.put(display, false);
            }
        }
        if (quest.getStage(stage).customObjectives.isEmpty() == false) {
            for (CustomObjective co : quest.getStage(stage).customObjectives) {
                data.customObjectiveCounts.put(co.getName(), 0);
            }
        }
        data.setDoJournalUpdate(true);
        hardDataPut(quest, data);
    }
    
    /**
     * Save data of the Quester to file
     */
    public void saveData() {
        FileConfiguration data = getBaseData();
        try {
            data.save(new File(plugin.getDataFolder(), "data" + File.separator + id + ".yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get the difference between Sytem.currentTimeMillis() and the last completed time for a quest
     * 
     * @param q The quest to get the last completed time of
     * @return Difference between now and then in milliseconds
     */
    public long getCooldownDifference(Quest q) {
        long currentTime = System.currentTimeMillis();
        long lastTime;
        if (completedTimes.containsKey(q.getName()) == false) {
            lastTime = System.currentTimeMillis();
            completedTimes.put(q.getName(), System.currentTimeMillis());
        } else {
            lastTime = completedTimes.get(q.getName());
        }
        long comparator = q.getPlanner().getCooldown();
        long difference = (comparator - (currentTime - lastTime));
        return difference;
    }

    @SuppressWarnings("deprecation")
    public FileConfiguration getBaseData() {
        FileConfiguration data = new YamlConfiguration();
        if (currentQuests.isEmpty() == false) {
            ArrayList<String> questNames = new ArrayList<String>();
            ArrayList<Integer> questStages = new ArrayList<Integer>();
            for (Quest quest : currentQuests.keySet()) {
                questNames.add(quest.getName());
                questStages.add(currentQuests.get(quest));
            }
            data.set("currentQuests", questNames);
            data.set("currentStages", questStages);
            data.set("quest-points", questPoints);
            ConfigurationSection dataSec = data.createSection("questData");
            for (Quest quest : currentQuests.keySet()) {
                if (quest.getName() == null || quest.getName().isEmpty()) {
                    plugin.getLogger().severe("Quest name was null or empty while loading data");
                    return null;
                }
                ConfigurationSection questSec = dataSec.createSection(quest.getName());
                QuestData questData = getQuestData(quest);
                if (questData == null)
                    continue;
                if (questData.blocksBroken.isEmpty() == false) {
                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (ItemStack m : questData.blocksBroken) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-broken-names", blockNames);
                    questSec.set("blocks-broken-amounts", blockAmounts);
                    questSec.set("blocks-broken-durability", blockDurability);
                }
                if (questData.blocksDamaged.isEmpty() == false) {
                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (ItemStack m : questData.blocksDamaged) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-damaged-names", blockNames);
                    questSec.set("blocks-damaged-amounts", blockAmounts);
                    questSec.set("blocks-damaged-durability", blockDurability);
                }
                if (questData.blocksPlaced.isEmpty() == false) {
                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (ItemStack m : questData.blocksPlaced) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-placed-names", blockNames);
                    questSec.set("blocks-placed-amounts", blockAmounts);
                    questSec.set("blocks-placed-durability", blockDurability);
                }
                if (questData.blocksUsed.isEmpty() == false) {
                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (ItemStack m : questData.blocksUsed) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-used-names", blockNames);
                    questSec.set("blocks-used-amounts", blockAmounts);
                    questSec.set("blocks-used-durability", blockDurability);
                }
                if (questData.blocksCut.isEmpty() == false) {
                    LinkedList<String> blockNames = new LinkedList<String>();
                    LinkedList<Integer> blockAmounts = new LinkedList<Integer>();
                    LinkedList<Short> blockDurability = new LinkedList<Short>();
                    for (ItemStack m : questData.blocksCut) {
                        blockNames.add(m.getType().name());
                        blockAmounts.add(m.getAmount());
                        blockDurability.add(m.getDurability());
                    }
                    questSec.set("blocks-cut-names", blockNames);
                    questSec.set("blocks-cut-amounts", blockAmounts);
                    questSec.set("blocks-cut-durability", blockDurability);
                }
                if (questData.itemsCrafted.isEmpty() == false) {
                    LinkedList<Integer> craftAmounts = new LinkedList<Integer>();
                    for (Entry<ItemStack, Integer> e : questData.itemsCrafted.entrySet()) {
                        craftAmounts.add(e.getValue());
                    }
                    questSec.set("item-craft-amounts", craftAmounts);
                }
                if (questData.itemsSmelted.isEmpty() == false) {
                    LinkedList<Integer> smeltAmounts = new LinkedList<Integer>();
                    for (Entry<ItemStack, Integer> e : questData.itemsSmelted.entrySet()) {
                        smeltAmounts.add(e.getValue());
                    }
                    questSec.set("item-smelt-amounts", smeltAmounts);
                }
                if (questData.itemsEnchanted.isEmpty() == false) {
                    LinkedList<String> enchantments = new LinkedList<String>();
                    LinkedList<String> itemNames = new LinkedList<String>();
                    LinkedList<Integer> enchAmounts = new LinkedList<Integer>();
                    for (Entry<Map<Enchantment, Material>, Integer> e : questData.itemsEnchanted.entrySet()) {
                        Map<Enchantment, Material> enchMap = (Map<Enchantment, Material>) e.getKey();
                        enchAmounts.add(questData.itemsEnchanted.get(enchMap));
                        for (Entry<Enchantment, Material> e2 : enchMap.entrySet()) {
                            enchantments.add(ItemUtil.getPrettyEnchantmentName((Enchantment) e2.getKey()));
                            itemNames.add(((Material) e2.getValue()).name());
                        }
                    }
                    questSec.set("enchantments", enchantments);
                    questSec.set("enchantment-item-names", itemNames);
                    questSec.set("times-enchanted", enchAmounts);
                }
                if (questData.itemsBrewed.isEmpty() == false) {
                    LinkedList<Integer> brewAmounts = new LinkedList<Integer>();
                    for (Entry<ItemStack, Integer> e : questData.itemsBrewed.entrySet()) {
                        brewAmounts.add(e.getValue());
                    }
                    questSec.set("item-brew-amounts", brewAmounts);
                }
                if (getCurrentStage(quest).cowsToMilk != null) {
                    questSec.set("cows-milked", questData.getCowsMilked());
                }
                if (getCurrentStage(quest).fishToCatch != null) {
                    questSec.set("fish-caught", questData.getFishCaught());
                }
                if (getCurrentStage(quest).playersToKill != null) {
                    questSec.set("players-killed", questData.getPlayersKilled());
                }
                if (questData.mobsKilled.isEmpty() == false) {
                    LinkedList<String> mobNames = new LinkedList<String>();
                    LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                    LinkedList<String> locations = new LinkedList<String>();
                    LinkedList<Integer> radii = new LinkedList<Integer>();
                    for (EntityType e : questData.mobsKilled) {
                        mobNames.add(MiscUtil.getPrettyMobName(e));
                    }
                    for (int i : questData.mobNumKilled) {
                        mobAmounts.add(i);
                    }
                    questSec.set("mobs-killed", mobNames);
                    questSec.set("mobs-killed-amounts", mobAmounts);
                    if (questData.locationsToKillWithin.isEmpty() == false) {
                        for (Location l : questData.locationsToKillWithin) {
                            locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());
                        }
                        for (int i : questData.radiiToKillWithin) {
                            radii.add(i);
                        }
                        questSec.set("mob-kill-locations", locations);
                        questSec.set("mob-kill-location-radii", radii);
                    }
                }
                if (questData.itemsDelivered.isEmpty() == false) {
                    LinkedList<Integer> deliveryAmounts = new LinkedList<Integer>();
                    for (ItemStack m : questData.itemsDelivered) {
                        deliveryAmounts.add(m.getAmount());
                    }
                    questSec.set("item-delivery-amounts", deliveryAmounts);
                }
                if (questData.citizensInteracted.isEmpty() == false) {
                    LinkedList<Integer> npcIds = new LinkedList<Integer>();
                    LinkedList<Boolean> hasTalked = new LinkedList<Boolean>();
                    for (Integer n : questData.citizensInteracted.keySet()) {
                        npcIds.add(n);
                        hasTalked.add(questData.citizensInteracted.get(n));
                    }
                    questSec.set("citizen-ids-to-talk-to", npcIds);
                    questSec.set("has-talked-to", hasTalked);
                }
                if (questData.citizensKilled.isEmpty() == false) {
                    LinkedList<Integer> npcIds = new LinkedList<Integer>();
                    for (Integer n : questData.citizensKilled) {
                        npcIds.add(n);
                    }
                    questSec.set("citizen-ids-killed", npcIds);
                    questSec.set("citizen-amounts-killed", questData.citizenNumKilled);
                }
                if (questData.locationsReached.isEmpty() == false) {
                    LinkedList<String> locations = new LinkedList<String>();
                    LinkedList<Boolean> has = new LinkedList<Boolean>();
                    LinkedList<Integer> radii = new LinkedList<Integer>();
                    for (Location l : questData.locationsReached) {
                        locations.add(l.getWorld().getName() + " " + l.getX() + " " + l.getY() + " " + l.getZ());
                    }
                    for (boolean b : questData.hasReached) {
                        has.add(b);
                    }
                    for (int i : questData.radiiToReachWithin) {
                        radii.add(i);
                    }
                    questSec.set("locations-to-reach", locations);
                    questSec.set("has-reached-location", has);
                    questSec.set("radii-to-reach-within", radii);
                }
                if (questData.mobsTamed.isEmpty() == false) {
                    LinkedList<String> mobNames = new LinkedList<String>();
                    LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                    for (EntityType e : questData.mobsTamed.keySet()) {
                        mobNames.add(MiscUtil.getPrettyMobName(e));
                        mobAmounts.add(questData.mobsTamed.get(e));
                    }
                    questSec.set("mobs-to-tame", mobNames);
                    questSec.set("mob-tame-amounts", mobAmounts);
                }
                if (questData.sheepSheared.isEmpty() == false) {
                    LinkedList<String> colors = new LinkedList<String>();
                    LinkedList<Integer> shearAmounts = new LinkedList<Integer>();
                    for (DyeColor d : questData.sheepSheared.keySet()) {
                        colors.add(MiscUtil.getPrettyDyeColorName(d));
                        shearAmounts.add(questData.sheepSheared.get(d));
                    }
                    questSec.set("sheep-to-shear", colors);
                    questSec.set("sheep-sheared", shearAmounts);
                }
                if (questData.passwordsSaid.isEmpty() == false) {
                    LinkedList<String> passwords = new LinkedList<String>();
                    LinkedList<Boolean> said = new LinkedList<Boolean>();
                    for (Entry<String, Boolean> entry : questData.passwordsSaid.entrySet()) {
                        passwords.add(entry.getKey());
                        said.add(entry.getValue());
                    }
                    questSec.set("passwords", passwords);
                    questSec.set("passwords-said", said);
                }
                if (questData.customObjectiveCounts.isEmpty() == false) {
                    LinkedList<String> customObj = new LinkedList<String>();
                    LinkedList<Integer> customObjCounts = new LinkedList<Integer>();
                    for (Entry<String, Integer> entry : questData.customObjectiveCounts.entrySet()) {
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
                    LinkedList<String> chatTriggers = new LinkedList<String>();
                    for (String trigger : questData.actionFired.keySet()) {
                        if (questData.actionFired.get(trigger) == true) {
                            chatTriggers.add(trigger);
                        }
                    }
                    if (chatTriggers.isEmpty() == false) {
                        questSec.set("chat-triggers", chatTriggers);
                    }
                }
                if (questData.actionFired.isEmpty() == false) {
                    LinkedList<String> commandTriggers = new LinkedList<String>();
                    for (String commandTrigger : questData.actionFired.keySet()) {
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
            List<String> noDupe = new ArrayList<String>();
            for (String s : completedQuests)
                if (!noDupe.contains(s))
                    noDupe.add(s);
            String[] completed = new String[noDupe.size()];
            int index = 0;
            for (String s : noDupe) {
                completed[index] = s;
                index++;
            }
            data.set("completed-Quests", completed);
        }
        if (completedTimes.isEmpty() == false) {
            List<String> questTimeNames = new LinkedList<String>();
            List<Long> questTimes = new LinkedList<Long>();
            for (String s : completedTimes.keySet()) {
                questTimeNames.add(s);
                questTimes.add(completedTimes.get(s));
            }
            data.set("completedRedoableQuests", questTimeNames);
            data.set("completedQuestTimes", questTimes);
        }
        if (amountsCompleted.isEmpty() == false) {
            List<String> list1 = new LinkedList<String>();
            List<Integer> list2 = new LinkedList<Integer>();
            for (Entry<String, Integer> entry : amountsCompleted.entrySet()) {
                list1.add(entry.getKey());
                list2.add(entry.getValue());
            }
            data.set("amountsCompletedQuests", list1);
            data.set("amountsCompleted", list2);
        }
        // #getPlayer is faster
        OfflinePlayer representedPlayer = getPlayer();
        if (representedPlayer == null) {
            representedPlayer = getOfflinePlayer();
        }
        data.set("hasJournal", hasJournal);
        data.set("lastKnownName", representedPlayer.getName());
        return data;
    }

    @SuppressWarnings("deprecation")
    public boolean loadData() {
        FileConfiguration data = new YamlConfiguration();
        try {
            File dataFile = new File(plugin.getDataFolder(), "data" + File.separator + id.toString() + ".yml");
            if (dataFile.exists() == false) {
                OfflinePlayer p = getOfflinePlayer();
                dataFile = new File(plugin.getDataFolder(), "data" + File.separator + p.getName() + ".yml");
                if (dataFile.exists() == false) {
                    return false;
                }
            }
            data.load(dataFile);
        } catch (IOException e) {
            return false;
        } catch (InvalidConfigurationException e) {
            return false;
        }
        hardClear();
        if (data.contains("completedRedoableQuests")) {
            List<String> redoNames = data.getStringList("completedRedoableQuests");
            List<Long> redoTimes = data.getLongList("completedQuestTimes");
            for (String s : redoNames) {
                for (Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(s)) {
                        completedTimes.put(q.getName(), redoTimes.get(redoNames.indexOf(s)));
                        break;
                    }
                }
            }
        }
        if (data.contains("amountsCompletedQuests")) {
            List<String> list1 = data.getStringList("amountsCompletedQuests");
            List<Integer> list2 = data.getIntegerList("amountsCompleted");
            for (int i = 0; i < list1.size(); i++) {
                amountsCompleted.put(list1.get(i), list2.get(i));
            }
        }
        questPoints = data.getInt("quest-points");
        hasJournal = data.getBoolean("hasJournal");
        if (data.isList("completed-Quests")) {
            for (String s : data.getStringList("completed-Quests")) {
                for (Quest q : plugin.getQuests()) {
                    if (q.getName().equalsIgnoreCase(s)) {
                        if (!completedQuests.contains(q.getName())) {
                            completedQuests.add(q.getName());
                        }
                        break;
                    }
                }
            }
        } else {
            completedQuests.clear();
        }
        if (data.isString("currentQuests") == false) {
            List<String> questNames = data.getStringList("currentQuests");
            List<Integer> questStages = data.getIntegerList("currentStages");
            // These appear to differ sometimes? That seems bad.
            int maxSize = Math.min(questNames.size(), questStages.size());
            for (int i = 0; i < maxSize; i++) {
                if (plugin.getQuest(questNames.get(i)) != null) {
                    currentQuests.put(plugin.getQuest(questNames.get(i)), questStages.get(i));
                }
            }
            ConfigurationSection dataSec = data.getConfigurationSection("questData");
            if (dataSec == null || dataSec.getKeys(false).isEmpty()) {
                return false;
            }
            for (String key : dataSec.getKeys(false)) {
                ConfigurationSection questSec = dataSec.getConfigurationSection(key);
                Quest quest = plugin.getQuest(key);
                Stage stage;
                if (quest == null || currentQuests.containsKey(quest) == false) {
                    continue;
                }
                stage = getCurrentStage(quest);
                if (stage == null) {
                    quest.completeQuest(this);
                    plugin.getLogger().severe("[Quests] Invalid stage number for player: \"" + id + "\" on Quest \"" 
                            + quest.getName() + "\". Quest ended.");
                    continue;
                }
                addEmptiesFor(quest, currentQuests.get(quest));
                if (questSec == null)
                    continue;
                if (questSec.contains("blocks-broken-names")) {
                    List<String> names = questSec.getStringList("blocks-broken-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-broken-amounts");
                    List<Short> durability = questSec.getShortList("blocks-broken-durability");
                    int index = 0;
                    for (String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (getQuestData(quest).blocksBroken.size() > 0) {
                            getQuestData(quest).blocksBroken.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-damaged-names")) {
                    List<String> names = questSec.getStringList("blocks-damaged-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-damaged-amounts");
                    List<Short> durability = questSec.getShortList("blocks-damaged-durability");
                    int index = 0;
                    for (String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (getQuestData(quest).blocksDamaged.size() > 0) {
                            getQuestData(quest).blocksDamaged.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-placed-names")) {
                    List<String> names = questSec.getStringList("blocks-placed-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-placed-amounts");
                    List<Short> durability = questSec.getShortList("blocks-placed-durability");
                    int index = 0;
                    for (String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (getQuestData(quest).blocksPlaced.size() > 0) {
                            getQuestData(quest).blocksPlaced.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-used-names")) {
                    List<String> names = questSec.getStringList("blocks-used-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-used-amounts");
                    List<Short> durability = questSec.getShortList("blocks-used-durability");
                    int index = 0;
                    for (String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (getQuestData(quest).blocksUsed.size() > 0) {
                            getQuestData(quest).blocksUsed.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("blocks-cut-names")) {
                    List<String> names = questSec.getStringList("blocks-cut-names");
                    List<Integer> amounts = questSec.getIntegerList("blocks-cut-amounts");
                    List<Short> durability = questSec.getShortList("blocks-cut-durability");
                    int index = 0;
                    for (String s : names) {
                        ItemStack is;
                        try {
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), durability.get(index));
                        } catch (IndexOutOfBoundsException e) {
                            // Legacy
                            is = new ItemStack(Material.matchMaterial(s), amounts.get(index), (short) 0);
                        }
                        if (getQuestData(quest).blocksCut.size() > 0) {
                            getQuestData(quest).blocksCut.set(index, is);
                        }
                        index++;
                    }
                }
                if (questSec.contains("item-craft-amounts")) {
                    List<Integer> craftAmounts = questSec.getIntegerList("item-craft-amounts");
                    for (int i = 0; i < craftAmounts.size(); i++) {
                        if (i < getCurrentStage(quest).itemsToCraft.size()) {
                            getQuestData(quest).itemsCrafted.put(getCurrentStage(quest).itemsToCraft
                                    .get(i), craftAmounts.get(i));
                        }
                    }
                }
                if (questSec.contains("item-smelt-amounts")) {
                    List<Integer> smeltAmounts = questSec.getIntegerList("item-smelt-amounts");
                    for (int i = 0; i < smeltAmounts.size(); i++) {
                        if (i < getCurrentStage(quest).itemsToSmelt.size()) {
                            getQuestData(quest).itemsSmelted.put(getCurrentStage(quest).itemsToSmelt
                                    .get(i), smeltAmounts.get(i));
                        }
                    }
                }
                if (questSec.contains("enchantments")) {
                    LinkedList<Enchantment> enchantments = new LinkedList<Enchantment>();
                    LinkedList<Material> materials = new LinkedList<Material>();
                    LinkedList<Integer> amounts = new LinkedList<Integer>();
                    List<String> enchantNames = questSec.getStringList("enchantments");
                    List<String> names = questSec.getStringList("enchantment-item-names");
                    List<Integer> times = questSec.getIntegerList("times-enchanted");
                    for (String s : enchantNames) {
                        enchantments.add(ItemUtil.getEnchantmentFromProperName(s));
                        materials.add(Material.matchMaterial(names.get(enchantNames.indexOf(s))));
                        amounts.add(times.get(enchantNames.indexOf(s)));
                    }
                    for (Enchantment e : enchantments) {
                        Map<Enchantment, Material> map = new HashMap<Enchantment, Material>();
                        map.put(e, materials.get(enchantments.indexOf(e)));
                        getQuestData(quest).itemsEnchanted.put(map, amounts.get(enchantments.indexOf(e)));
                    }
                }
                if (questSec.contains("item-brew-amounts")) {
                    List<Integer> brewAmounts = questSec.getIntegerList("item-brew-amounts");
                    for (int i = 0; i < brewAmounts.size(); i++) {
                        if (i < getCurrentStage(quest).itemsToBrew.size()) {
                            getQuestData(quest).itemsBrewed.put(getCurrentStage(quest).itemsToBrew
                                    .get(i), brewAmounts.get(i));
                        }
                    }
                }
                if (questSec.contains("cows-milked")) {
                    getQuestData(quest).setCowsMilked(questSec.getInt("cows-milked"));
                }
                if (questSec.contains("fish-caught")) {
                    getQuestData(quest).setFishCaught(questSec.getInt("fish-caught"));
                }
                if (questSec.contains("players-killed")) {
                    getQuestData(quest).setPlayersKilled(questSec.getInt("players-killed"));
                }
                if (questSec.contains("mobs-killed")) {
                    LinkedList<EntityType> mobs = new LinkedList<EntityType>();
                    List<Integer> amounts = questSec.getIntegerList("mobs-killed-amounts");
                    for (String s : questSec.getStringList("mobs-killed")) {
                        EntityType mob = MiscUtil.getProperMobType(s);
                        if (mob != null) {
                            mobs.add(mob);
                        }
                        getQuestData(quest).mobsKilled.clear();
                        getQuestData(quest).mobNumKilled.clear();
                        for (EntityType e : mobs) {
                            getQuestData(quest).mobsKilled.add(e);
                            getQuestData(quest).mobNumKilled.add(amounts.get(mobs.indexOf(e)));
                        }
                        if (questSec.contains("mob-kill-locations")) {
                            LinkedList<Location> locations = new LinkedList<Location>();
                            List<Integer> radii = questSec.getIntegerList("mob-kill-location-radii");
                            for (String loc : questSec.getStringList("mob-kill-locations")) {
                                if (ConfigUtil.getLocation(loc) != null) {
                                    locations.add(ConfigUtil.getLocation(loc));
                                }
                            }
                            getQuestData(quest).locationsToKillWithin = locations;
                            getQuestData(quest).radiiToKillWithin.clear();
                            for (int i : radii) {
                                getQuestData(quest).radiiToKillWithin.add(i);
                            }
                        }
                    }
                }
                if (questSec.contains("item-delivery-amounts")) {
                    List<Integer> deliveryAmounts = questSec.getIntegerList("item-delivery-amounts");
                    int index = 0;
                    for (int amt : deliveryAmounts) {
                        ItemStack is = getCurrentStage(quest).itemsToDeliver.get(index);
                        ItemStack temp = new ItemStack(is.getType(), amt, is.getDurability());
                        try {
                            temp.addEnchantments(is.getEnchantments());
                        } catch (Exception e) {
                            plugin.getLogger().warning("Unable to add enchantment(s) " + is.getEnchantments().toString()
                                    + " to delivery item " + is.getType().name() + " x " + amt + " for quest " 
                                    + quest.getName());
                        }
                        temp.setItemMeta(is.getItemMeta());
                        if (getQuestData(quest).itemsDelivered.size() > 0) {
                            getQuestData(quest).itemsDelivered.set(index, temp);
                        }
                        index++;
                    }
                }
                if (questSec.contains("citizen-ids-to-talk-to")) {
                    List<Integer> ids = questSec.getIntegerList("citizen-ids-to-talk-to");
                    List<Boolean> has = questSec.getBooleanList("has-talked-to");
                    for (int i : ids) {
                        getQuestData(quest).citizensInteracted.put(i, has.get(ids.indexOf(i)));
                    }
                }
                if (questSec.contains("citizen-ids-killed")) {
                    List<Integer> ids = questSec.getIntegerList("citizen-ids-killed");
                    List<Integer> num = questSec.getIntegerList("citizen-amounts-killed");
                    getQuestData(quest).citizensKilled.clear();
                    getQuestData(quest).citizenNumKilled.clear();
                    for (int i : ids) {
                        getQuestData(quest).citizensKilled.add(i);
                        getQuestData(quest).citizenNumKilled.add(num.get(ids.indexOf(i)));
                    }
                }
                if (questSec.contains("locations-to-reach")) {
                    LinkedList<Location> locations = new LinkedList<Location>();
                    List<Boolean> has = questSec.getBooleanList("has-reached-location");
                    while (has.size() < locations.size()) {
                        // TODO - Find proper cause of Github issues #646 and #825
                        plugin.getLogger().info("Added missing has-reached-location data for Quester " + id);
                        has.add(false);
                    }
                    List<Integer> radii = questSec.getIntegerList("radii-to-reach-within");
                    for (String loc : questSec.getStringList("locations-to-reach")) {
                        if (ConfigUtil.getLocation(loc) != null) {
                            locations.add(ConfigUtil.getLocation(loc));
                        }
                    }
                    getQuestData(quest).locationsReached = locations;
                    getQuestData(quest).hasReached.clear();
                    getQuestData(quest).radiiToReachWithin.clear();
                    for (boolean b : has) {
                        getQuestData(quest).hasReached.add(b);
                    }
                    for (int i : radii) {
                        getQuestData(quest).radiiToReachWithin.add(i);
                    }
                }
                if (questSec.contains("mobs-to-tame")) {
                    List<String> mobs = questSec.getStringList("mobs-to-tame");
                    List<Integer> amounts = questSec.getIntegerList("mob-tame-amounts");
                    for (String mob : mobs) {
                        getQuestData(quest).mobsTamed.put(EntityType.valueOf(mob.toUpperCase()), amounts
                                .get(mobs.indexOf(mob)));
                    }
                }
                if (questSec.contains("sheep-to-shear")) {
                    List<String> colors = questSec.getStringList("sheep-to-shear");
                    List<Integer> amounts = questSec.getIntegerList("sheep-sheared");
                    for (String color : colors) {
                        getQuestData(quest).sheepSheared.put(MiscUtil.getProperDyeColor(color), amounts.get(colors
                                .indexOf(color)));
                    }
                }
                if (questSec.contains("passwords")) {
                    List<String> passwords = questSec.getStringList("passwords");
                    List<Boolean> said = questSec.getBooleanList("passwords-said");
                    for (int i = 0; i < passwords.size(); i++) {
                        getQuestData(quest).passwordsSaid.put(passwords.get(i), said.get(i));
                    }
                }
                if (questSec.contains("custom-objectives")) {
                    List<String> customObj = questSec.getStringList("custom-objectives");
                    List<Integer> customObjCount = questSec.getIntegerList("custom-objective-counts");
                    for (int i = 0; i < customObj.size(); i++) {
                        getQuestData(quest).customObjectiveCounts.put(customObj.get(i), customObjCount.get(i));
                    }
                }
                if (questSec.contains("stage-delay")) {
                    getQuestData(quest).setDelayTimeLeft(questSec.getLong("stage-delay"));
                }
                if (getCurrentStage(quest).chatActions.isEmpty() == false) {
                    for (String chatTrig : getCurrentStage(quest).chatActions.keySet()) {
                        getQuestData(quest).actionFired.put(chatTrig, false);
                    }
                }
                if (questSec.contains("chat-triggers")) {
                    List<String> chatTriggers = questSec.getStringList("chat-triggers");
                    for (String s : chatTriggers) {
                        getQuestData(quest).actionFired.put(s, true);
                    }
                }
                if (getCurrentStage(quest).commandActions.isEmpty() == false) {
                    for (String commandTrig : getCurrentStage(quest).commandActions.keySet()) {
                        getQuestData(quest).actionFired.put(commandTrig, false);
                    }
                }
                if (questSec.contains("command-triggers")) {
                    List<String> commandTriggers = questSec.getStringList("command-triggers");
                    for (String s : commandTriggers) {
                        getQuestData(quest).actionFired.put(s, true);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Initiate the stage timer
     * @param quest The quest of which the timer is for
     */
    public void startStageTimer(Quest quest) {
        if (getQuestData(quest).getDelayTimeLeft() > -1) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), 
                    (long) (getQuestData(quest).getDelayTimeLeft() * 0.02));
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new StageTimer(plugin, this, quest), 
                    (long) (getCurrentStage(quest).delay * 0.02));
            if (getCurrentStage(quest).delayMessage != null) {
                Player p = plugin.getServer().getPlayer(id);
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
    public void stopStageTimer(Quest quest) {
        if (getQuestData(quest).getDelayTimeLeft() > -1) {
            getQuestData(quest).setDelayTimeLeft(getQuestData(quest).getDelayTimeLeft() - (System.currentTimeMillis() 
                    - getQuestData(quest).getDelayStartTime()));
        } else {
            getQuestData(quest).setDelayTimeLeft(getCurrentStage(quest).delay - (System.currentTimeMillis() 
                    - getQuestData(quest).getDelayStartTime()));
        }
        getQuestData(quest).setDelayOver(false);
    }
    
    /**
     * Get remaining stage delay time
     * @param quest The quest of which the timer is for
     * @return Remaining time in milliseconds
     */
    public long getStageTime(Quest quest) {
        if (getQuestData(quest).getDelayTimeLeft() > -1) {
            return getQuestData(quest).getDelayTimeLeft() - (System.currentTimeMillis() 
                    - getQuestData(quest).getDelayStartTime());
        } else {
            return getCurrentStage(quest).delay - (System.currentTimeMillis() - getQuestData(quest).getDelayStartTime());
        }
    }

    public boolean hasData() {
        if (currentQuests.isEmpty() == false || questData.isEmpty() == false) {
            return true;
        }
        if (questPoints > 1) {
            return true;
        }
        return completedQuests.isEmpty() == false;
    }
    
    /**
     * Check whether the provided quest is valid and, if not, inform the Quester
     * 
     * @param quest The quest to check
     */
    public void checkQuest(Quest quest) {
        if (quest != null) {
            boolean exists = false;
            for (Quest q : plugin.getQuests()) {
                if (q.getName().equalsIgnoreCase(quest.getName())) {
                    Stage stage = getCurrentStage(quest);
                    quest.updateCompass(this, stage);
                    exists = true;
                    break;
                }
            }
            if (exists == false) {
                if (plugin.getServer().getPlayer(id) != null) {
                    String error = Lang.get("questNotExist");
                    error = error.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.RED);
                    plugin.getServer().getPlayer(id).sendMessage(ChatColor.GOLD + "[Quests] " + ChatColor.RED + error);
                }
            }
        }
    }

    /**
     * Show an inventory GUI with quest items to the specified player
     * 
     * @param npc The NPC from which the GUI is bound
     * @param quests List of quests to use for displaying items
     */
    public void showGUIDisplay(NPC npc, LinkedList<Quest> quests) {
        if (npc == null || quests == null) {
            return;
        }
        QuesterPreOpenGUIEvent preEvent = new QuesterPreOpenGUIEvent(this, npc, quests);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        Player player = getPlayer();
        Inventory inv = plugin.getServer().createInventory(player, ((quests.size() / 9) + 1) * 9, 
                Lang.get(player, "quests") + " | " + npc.getName());
        int i = 0;
        for (Quest quest : quests) {
            if (quest.guiDisplay != null) {
                if (i > 53) {
                    // Protocol-enforced size limit has been exceeded
                    break;
                }
                ItemStack display = quest.guiDisplay;
                ItemMeta meta = display.getItemMeta();
                if (completedQuests.contains(quest.getName())) {
                    meta.setDisplayName(ChatColor.DARK_PURPLE + ConfigUtil.parseString(quest.getName()
                            + " " + ChatColor.GREEN + Lang.get(player, "redoCompleted"), npc));
                } else {
                    meta.setDisplayName(ChatColor.DARK_PURPLE + ConfigUtil.parseString(quest.getName(), npc));
                }
                if (!meta.hasLore()) {
                    LinkedList<String> lines = new LinkedList<String>();
                    String desc = quest.description;
                    if (desc.equals(ChatColor.stripColor(desc))) {
                        lines = MiscUtil.makeLines(desc, " ", 40, ChatColor.DARK_GREEN);
                    } else {
                        lines = MiscUtil.makeLines(desc, " ", 40, null);
                    }
                    meta.setLore(lines);
                }
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
    public void hardQuit(Quest quest) {
        try {
            currentQuests.remove(quest);
            if (questData.containsKey(quest)) {
                questData.remove(quest);
            }
            if (!timers.isEmpty()) {
                for (Map.Entry<Integer, Quest> entry : timers.entrySet()) {
                    if (entry.getValue().getName().equals(quest.getName())) {
                        plugin.getServer().getScheduler().cancelTask(entry.getKey());
                        timers.remove(entry.getKey());
                    }
                }
            }
        } catch (Exception ex) {
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
    public void hardRemove(Quest quest) {
        try {
            completedQuests.remove(quest.getName());
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
    public void hardStagePut(Quest key, Integer val) {
        try {
            currentQuests.put(key, val);
        } catch (Exception ex) {
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
    public void hardDataPut(Quest key, QuestData val) {
        try {
            questData.put(key, val);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reset compass target to Quester's bed spawn location<p>
     * 
     * Will set to Quester's spawn location if bed spawn does not exist
     */
    public void resetCompass() {
        if (!plugin.getSettings().canUseCompass())
            return;
        Player player = getPlayer();
        if (player == null)
            return;
        Location defaultLocation = player.getBedSpawnLocation();
        if (defaultLocation == null) {
            defaultLocation = player.getWorld().getSpawnLocation();
        }
        player.setCompassTarget(defaultLocation);
    }

    /**
     * Gets first stage target from current quests, then updates compass accordingly
     */
    public void findCompassTarget() {
        if (!plugin.getSettings().canUseCompass())
            return;
        Player player = getPlayer();
        if (player == null)
            return;
        for (Quest quest : currentQuests.keySet()) {
            Stage stage = getCurrentStage(quest);
            if (stage != null && quest.updateCompass(this, stage))
                break;
        }
    }
    
    /**
     * Check whether the Quester's inventory contains the specified item
     * 
     * @param is The item with a specified amount to check
     * @return true if the inventory contains at least the amount of the specified stack 
     */
    public boolean hasItem(ItemStack is) {
        Inventory inv = getPlayer().getInventory();
        int playerAmount = 0;
        for (ItemStack stack : inv.getContents()) {
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
     * cutBlock, craftItem, smeltItem, enchantItem, brewItem, milkCow, catchFish,
     * killMob, deliverItem, killPlayer, talkToNPC, killNPC, tameMob,
     * shearSheep, password, reachLocation
     *
     * @param objectiveType The type of objective to progress
     * @param fun The function to execute, the event call
     */
    public void dispatchMultiplayerEverything(Quest quest, String objectiveType, Function<Quester, Void> fun) {
        if (quest.getOptions().getShareProgressLevel() == 1) {
            List<Quester> mq = getMultiplayerQuesters(quest);
            if (mq == null) {
                return;
            }
            for (Quester q : mq) {
                if (q.containsObjective(quest, objectiveType)) {
                    if (!quest.getOptions().getRequireSameQuest() || this.containsObjective(quest, objectiveType)) {
                        fun.apply(q);
                    }
                }
            }
        }
    }
    
    /**
     * Dispatch finish objective to fellow questers
     *
     * @param quest The current quest
     * @param currentStage The current stage of the quest
     * @param fun The function to execute, the event call
     */
    public void dispatchMultiplayerObjectives(Quest quest, Stage currentStage, Function<Quester, Void> fun) {
        if (quest.getOptions().getShareProgressLevel() == 2) {
            List<Quester> mq = getMultiplayerQuesters(quest);
            for (Quester q : mq) {
                if ((q.getCurrentQuests().containsKey(quest) && currentStage.equals(q.getCurrentStage(quest)))
                        || !quest.getOptions().getRequireSameQuest()) {
                    fun.apply(q);
                }
            }
        }
    }
    
    /**
     * Get a list of follow Questers in a party or group
     * 
     * @param quest The quest which uses a linked plugin, i.e. Parties or DungeonsXL
     * @return Potentially empty list of Questers or null for invalid quest
     */
    public List<Quester> getMultiplayerQuesters(Quest quest) {
        if (quest == null) {
            return null;
        }
        List<Quester> mq = new LinkedList<Quester>();
        if (plugin.getDependencies().getPartiesApi() != null) {
            if (quest.getOptions().getUsePartiesPlugin()) {
                Party party = plugin.getDependencies().getPartiesApi().getParty(plugin.getDependencies()
                        .getPartiesApi().getPartyPlayer(getUUID()).getPartyName());
                if (party != null) {
                    for (UUID id : party.getMembers()) {
                        if (!id.equals(getUUID())) {
                            mq.add(plugin.getQuester(id));
                        }
                    }
                    return mq;
                }
            }
        }
        if (plugin.getDependencies().getDungeonsApi() != null) {
            if (quest.getOptions().getUseDungeonsXLPlugin()) {
                DGroup group = DGroup.getByPlayer(getPlayer());
                if (group != null) {
                    for (UUID id : group.getPlayers()) {
                        if (!id.equals(getUUID())) {
                            mq.add(plugin.getQuester(id));
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
    public boolean offerQuest(Quest quest, boolean giveReason) {
        if (quest == null) {
            return false;
        }
        QuestTakeEvent event = new QuestTakeEvent(quest, this);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        if (canAcceptOffer(quest, giveReason)) {
            if (getPlayer() instanceof Conversable) {
                if (getPlayer().isConversing() == false) {
                    setQuestToTake(quest.getName());
                    String s = ChatColor.GOLD + "- " + ChatColor.DARK_PURPLE + getQuestToTake() + ChatColor.GOLD 
                            + " -\n" + "\n" + ChatColor.RESET + plugin.getQuest(getQuestToTake()).getDescription() 
                            + "\n";
                    for (String msg : s.split("<br>")) {
                        getPlayer().sendMessage(msg);
                    }
                    if (!plugin.getSettings().canAskConfirmation()) {
                        takeQuest(plugin.getQuest(getQuestToTake()), false);
                    } else {
                        plugin.getConversationFactory().buildConversation((Conversable) getPlayer()).begin();
                    }
                    return true;
                } else {
                    getPlayer().sendMessage(ChatColor.YELLOW + Lang.get(getPlayer(), "alreadyConversing"));
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
    public boolean canAcceptOffer(Quest quest, boolean giveReason) {
        if (quest == null) {
            return false;
        }
        if (getCurrentQuests().size() >= plugin.getSettings().getMaxQuests() && plugin.getSettings().getMaxQuests() 
                > 0) {
            if (giveReason) {
                String msg = Lang.get(getPlayer(), "questMaxAllowed");
                msg = msg.replace("<number>", String.valueOf(plugin.getSettings().getMaxQuests()));
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCurrentQuests().containsKey(quest)) {
            if (giveReason) {
                String msg = Lang.get(getPlayer(), "questAlreadyOn");
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCompletedQuests().contains(quest.getName()) && quest.getPlanner().getCooldown() < 0) {
            if (giveReason) {
                String msg = Lang.get(getPlayer(), "questAlreadyCompleted");
                msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (quest.getNpcStart() != null && plugin.getSettings().canAllowCommandsForNpcQuests() == false) {
            if (giveReason) {
                String msg = Lang.get(getPlayer(), "mustSpeakTo");
                msg = msg.replace("<npc>", ChatColor.DARK_PURPLE + quest.getNpcStart().getName() + ChatColor.YELLOW);
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (quest.getBlockStart() != null) {
            if (giveReason) {
                String msg = Lang.get(getPlayer(), "noCommandStart");
                msg = msg.replace("<quest>", ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (getCompletedQuests().contains(quest.getName()) && getCooldownDifference(quest) > 0) {
            if (giveReason) {
                String msg = Lang.get(getPlayer(), "questTooEarly");
                msg = msg.replace("<quest>", ChatColor.AQUA + quest.getName() + ChatColor.YELLOW);
                msg = msg.replace("<time>", ChatColor.DARK_PURPLE + MiscUtil.getTime(getCooldownDifference(quest)) 
                        + ChatColor.YELLOW);
                getPlayer().sendMessage(ChatColor.YELLOW + msg);
            }
            return false;
        } else if (quest.getRegionStart() != null) {
            if (!quest.isInRegion(this)) {
                if (giveReason) {
                    String msg = Lang.get(getPlayer(), "questInvalidLocation");
                    msg = msg.replace("<quest>", ChatColor.AQUA + quest.getName() + ChatColor.YELLOW);
                    getPlayer().sendMessage(ChatColor.YELLOW + msg);
                }
                return false;
            }
        }
        return true;
    }
}
