/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests;

import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.codisimus.plugins.phatloots.loot.CommandLoot;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.characters.Hero;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.conditions.Condition;
import me.blackvein.quests.events.quest.QuestUpdateCompassEvent;
import me.blackvein.quests.events.quester.QuesterPostChangeStageEvent;
import me.blackvein.quests.events.quester.QuesterPostCompleteQuestEvent;
import me.blackvein.quests.events.quester.QuesterPostFailQuestEvent;
import me.blackvein.quests.events.quester.QuesterPreChangeStageEvent;
import me.blackvein.quests.events.quester.QuesterPreCompleteQuestEvent;
import me.blackvein.quests.events.quester.QuesterPreFailQuestEvent;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.InventoryUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;
import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;

public class Quest implements Comparable<Quest> {

    protected Quests plugin;
    protected String id;
    private String name;
    protected String description;
    protected String finished;
    protected ItemStack guiDisplay = null;
    private final LinkedList<Stage> orderedStages = new LinkedList<>();
    protected NPC npcStart;
    protected Location blockStart;
    protected String regionStart = null;
    protected Action initialAction;
    private final Requirements requirements = new Requirements();
    private final Planner planner = new Planner();
    private final Rewards rewards = new Rewards();
    private final Options options = new Options();

    @Override
    public int compareTo(final Quest quest) {
        return id.compareTo(quest.getId());
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(final String description) {
        this.description = description;
    }
    
    public String getFinished() {
        return finished;
    }
    
    public void setFinished(final String finished) {
        this.finished = finished;
    }
    
    public String getRegionStart() {
        return regionStart;
    }
    
    public void setRegionStart(final String regionStart) {
        this.regionStart = regionStart;
    }
    
    public ItemStack getGUIDisplay() {
        return guiDisplay;
    }
    
    public void setGUIDisplay(final ItemStack guiDisplay) {
        this.guiDisplay = guiDisplay;
    }
    
    public Stage getStage(final int index) {
        try {
            return orderedStages.get(index);
        } catch (final Exception e) {
            return null;
        }
    }
    
    public LinkedList<Stage> getStages() {
        return orderedStages;
    }
    
    public NPC getNpcStart() {
        return npcStart;
    }
    
    public void setNpcStart(final NPC npcStart) {
        this.npcStart = npcStart;
    }
    
    public Location getBlockStart() {
        return blockStart;
    }
    
    public void setBlockStart(final Location blockStart) {
        this.blockStart = blockStart;
    }
    
    public Action getInitialAction() {
        return initialAction;
    }
    
    public void setInitialAction(final Action initialAction) {
        this.initialAction = initialAction;
    }
    
    public Requirements getRequirements() {
        return requirements;
    }
    
    public Planner getPlanner() {
        return planner;
    }
    
    public Rewards getRewards() {
        return rewards;
    }
    
    public Options getOptions() {
        return options;
    }

    /**
     * Force player to proceed to the next ordered stage
     * 
     * @param quester Player to force
     * @param allowSharedProgress Whether to distribute progress to fellow questers
     */
    public void nextStage(final Quester quester, final boolean allowSharedProgress) {
        final Stage currentStage = quester.getCurrentStage(this);
        if (currentStage == null) {
            plugin.getLogger().severe("Current stage was null for quester " + quester.getPlayer().getUniqueId());
            return;
        }
        final String stageCompleteMessage = currentStage.completeMessage;
        if (stageCompleteMessage != null) {
            if (quester.getOfflinePlayer().isOnline()) {
                quester.getPlayer().sendMessage(ConfigUtil.parseStringWithPossibleLineBreaks(stageCompleteMessage, 
                        this, quester.getPlayer()));
            }
        }
        if (quester.getPlayer().hasPermission("quests.compass")) {
            quester.resetCompass();
            quester.findCompassTarget();
        }
        if (currentStage.delay < 0) {
            if (currentStage.finishAction != null) {
                currentStage.finishAction.fire(quester, this);
            }
            if (quester.currentQuests.get(this) == (orderedStages.size() - 1)) {
                if (currentStage.script != null) {
                    plugin.getDenizenTrigger().runDenizenScript(currentStage.script, quester);
                }
                completeQuest(quester);
            } else {
                setStage(quester, quester.currentQuests.get(this) + 1);
            }
            if (quester.getQuestData(this) != null) {
                quester.getQuestData(this).setDelayStartTime(0);
                quester.getQuestData(this).setDelayTimeLeft(-1);
            }
            
            // Multiplayer
            if (allowSharedProgress && options.getShareProgressLevel() == 3) {
                final List<Quester> mq = quester.getMultiplayerQuesters(this);
                for (final Quester qq : mq) {
                    if (currentStage.equals(qq.getCurrentStage(this))) {
                        nextStage(qq, true);
                    }
                }
            }
        } else {
            quester.startStageTimer(this);
        }
        quester.updateJournal();
    }

    /**
     * Force player to proceed to the specified stage
     * 
     * @param quester Player to force
     * @param stage Stage number to specify
     * @throws IndexOutOfBoundsException if stage does not exist
     */
    public void setStage(final Quester quester, final int stage) throws IndexOutOfBoundsException {
        final OfflinePlayer player = quester.getOfflinePlayer();
        if (orderedStages.size() - 1 < stage) {
            final String msg = "Tried to set invalid stage number of " + stage + " for quest " + getName() + " on " 
                    + player.getName();
            throw new IndexOutOfBoundsException(msg);
        }
        final Stage currentStage = quester.getCurrentStage(this);
        final Stage nextStage = getStage(stage);
        if (player.isOnline()) {
            final QuesterPreChangeStageEvent preEvent = new QuesterPreChangeStageEvent(quester, this, currentStage, nextStage);
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
        }
        quester.hardQuit(this);
        quester.hardStagePut(this, stage);
        quester.addEmptiesFor(this, stage);
        if (currentStage.script != null) {
            plugin.getDenizenTrigger().runDenizenScript(currentStage.script, quester);
        }
        if (nextStage.startAction != null) {
            nextStage.startAction.fire(quester, this);
        }
        updateCompass(quester, nextStage);
        if (player.isOnline()) {
            final Player p = quester.getPlayer();
            final String title = Lang.get(p, "objectives").replace("<quest>", name);
            quester.sendMessage(ChatColor.GOLD + title);
            plugin.showObjectives(this, quester, false);
            final String stageStartMessage = quester.getCurrentStage(this).startMessage;
            if (stageStartMessage != null) {
                p.sendMessage(ConfigUtil.parseStringWithPossibleLineBreaks(stageStartMessage, this, p));
            }
            final Condition c = nextStage.getCondition();
            if (c != null && nextStage.getObjectiveOverrides().isEmpty()) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + Lang.get("stageEditorConditions"));
                if (!c.getEntitiesWhileRiding().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorRideEntity"));
                    for (final String e : c.getEntitiesWhileRiding()) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(e);
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                } else if (!c.getNpcsWhileRiding().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorRideNPC"));
                    for (final int i : c.getNpcsWhileRiding()) {
                        if (plugin.getDependencies().getCitizens() != null) {
                            msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(CitizensAPI.getNPCRegistry()
                                    .getById(i).getName());
                        } else {
                            msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(i);
                        }
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                } else if (!c.getPermissions().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorPermissions"));
                    for (final String e : c.getPermissions()) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(e);
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                } else if (!c.getItemsWhileHoldingMainHand().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorItemsInMainHand"));
                    for (final ItemStack is : c.getItemsWhileHoldingMainHand()) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(ItemUtil.getPrettyItemName(is
                                .getType().name()));
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                } else if (!c.getWorldsWhileStayingWithin().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorStayWithinWorld"));
                    for (final String w : c.getWorldsWhileStayingWithin()) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(w);
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                } else if (!c.getBiomesWhileStayingWithin().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorStayWithinBiome"));
                    for (final String b : c.getBiomesWhileStayingWithin()) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(MiscUtil
                                .snakeCaseToUpperCamelCase(b));
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                } else if (!c.getRegionsWhileStayingWithin().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorStayWithinRegion"));
                    for (final String r : c.getRegionsWhileStayingWithin()) {
                        msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(r);
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                } else if (!c.getPlaceholdersCheckIdentifier().isEmpty()) {
                    final StringBuilder msg = new StringBuilder("- " + Lang.get("conditionEditorCheckPlaceholder"));
                    int index = 0;
                    for (final String r : c.getPlaceholdersCheckIdentifier()) {
                        if (c.getPlaceholdersCheckValue().size() > index) {
                            msg.append(ChatColor.AQUA).append("\n   \u2515 ").append(r).append(ChatColor.GRAY)
                                    .append(" = ").append(ChatColor.AQUA).append(c.getPlaceholdersCheckValue()
                                            .get(index));
                        }
                        index++;
                    }
                    p.sendMessage(ChatColor.YELLOW + msg.toString());
                }
            }
        }
        quester.updateJournal();
        if (player.isOnline()) {
            final QuesterPostChangeStageEvent postEvent = new QuesterPostChangeStageEvent(quester, this, currentStage, nextStage);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Set location-objective target for compass.<p>
     * 
     * Method may be called as often as needed.
     * 
     * @param quester The online quester to have their compass updated
     * @param stage The stage to process for targets
     * @return true if an attempt was made successfully
     */
    public boolean updateCompass(final Quester quester, final Stage stage) {
        if (quester == null) {
            return false;
        }
        if (stage == null) {
            return false;
        }
        if (!quester.getOfflinePlayer().isOnline()) {
            return false;
        }
        if (!quester.getPlayer().hasPermission("quests.compass")) {
            return false;
        }
        final Quest quest = this;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location targetLocation = null;
            if (stage.citizensToInteract != null && stage.citizensToInteract.size() > 0) {
                targetLocation = plugin.getDependencies().getNPCLocation(stage.citizensToInteract.getFirst());
            } else if (stage.citizensToKill != null && stage.citizensToKill.size() > 0) {
                targetLocation = plugin.getDependencies().getNPCLocation(stage.citizensToKill.getFirst());
            } else if (stage.locationsToReach != null && stage.locationsToReach.size() > 0) {
                targetLocation = stage.locationsToReach.getFirst();
            } else if (stage.itemDeliveryTargets != null && stage.itemDeliveryTargets.size() > 0) {
                final NPC npc = plugin.getDependencies().getCitizens().getNPCRegistry().getById(stage.itemDeliveryTargets
                        .getFirst());
                targetLocation = npc.getStoredLocation();
            } else if (stage.playersToKill != null && stage.playersToKill > 0) {
                final Location source = quester.getPlayer().getLocation();
                Location nearest = null;
                double old_distance = 30000000;
                if (source.getWorld() == null) {
                    return;
                }
                for (final Player p : source.getWorld().getPlayers()) {
                    if (p.getUniqueId().equals(quester.getUUID())) {
                        continue;
                    }
                    final double new_distance = p.getLocation().distanceSquared(source);
                    if (new_distance < old_distance) {
                        nearest = p.getLocation();
                        old_distance = new_distance;
                    }
                }
                if (nearest != null) {
                    targetLocation = nearest;
                }
            } else if (stage.mobsToKill != null && stage.mobsToKill.size() > 0) {
                final Location source = quester.getPlayer().getLocation();
                Location nearest = null;
                double old_distance = 30000000;
                final EntityType et = stage.mobsToKill.getFirst();
                if (source.getWorld() == null) {
                    return;
                }
                for (final Entity e : source.getWorld().getEntities()) {
                    if (!e.getType().equals(et)) {
                        continue;
                    }
                    final double new_distance = e.getLocation().distanceSquared(source);
                    if (new_distance < old_distance) {
                        nearest = e.getLocation();
                        old_distance = new_distance;
                    }
                }
                if (nearest != null) {
                    targetLocation = nearest;
                }
            } else if (stage.mobsToTame != null && stage.mobsToTame.size() > 0) {
                final Location source = quester.getPlayer().getLocation();
                Location nearest = null;
                double old_distance = 30000000;
                final EntityType et = stage.mobsToTame.getFirst();
                if (source.getWorld() == null) {
                    return;
                }
                for (final Entity e : source.getWorld().getEntities()) {
                    if (!e.getType().equals(et)) {
                        continue;
                    }
                    final double new_distance = e.getLocation().distanceSquared(source);
                    if (new_distance < old_distance) {
                        nearest = e.getLocation();
                        old_distance = new_distance;
                    }
                }
                if (nearest != null) {
                    targetLocation = nearest;
                }
            } else if (stage.sheepToShear != null && stage.sheepToShear.size() > 0) {
                final Location source = quester.getPlayer().getLocation();
                Location nearest = null;
                double old_distance = 30000000;
                final DyeColor dc = stage.sheepToShear.getFirst();
                if (source.getWorld() == null) {
                    return;
                }
                for (final Entity e : source.getWorld().getEntities()) {
                    if (!e.getType().equals(EntityType.SHEEP)) {
                        continue;
                    }
                    final Sheep s = (Sheep)e;
                    if (s.getColor()!= null && s.getColor().equals(dc)) {
                        continue;
                    }
                    final double new_distance = e.getLocation().distanceSquared(source);
                    if (new_distance < old_distance) {
                        nearest = e.getLocation();
                        old_distance = new_distance;
                    }
                }
                if (nearest != null) {
                    targetLocation = nearest;
                }
            }
            if (targetLocation != null && targetLocation.getWorld() != null) {
                if (targetLocation.getWorld().getName().equals(quester.getPlayer().getWorld().getName())) {
                    final Location lockedTarget = new Location(targetLocation.getWorld(), targetLocation.getX(),
                            targetLocation.getY(), targetLocation.getZ());
                    final QuestUpdateCompassEvent event = new QuestUpdateCompassEvent(quest, quester, lockedTarget);
                    plugin.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    quester.getPlayer().setCompassTarget(lockedTarget);
                }
            }
        });
        return true;
    }
    
    /**
     * Check that a quester has met all Requirements to accept this quest<p>
     * 
     * Item, permission and custom Requirements are only checked for online players
     * 
     * @param quester The quester to check
     * @return true if all Requirements have been met
     */
    public boolean testRequirements(final Quester quester) {
        return testRequirements(quester.getOfflinePlayer());
    }
    
    /**
     * Check that a player has met all Requirements to accept this quest<p>
     * 
     * Item, permission and custom Requirements are only checked for online players
     * 
     * @param player The player to check
     * @return true if all Requirements have been met
     */
    protected boolean testRequirements(final OfflinePlayer player) {
        final Quester quester = plugin.getQuester(player.getUniqueId());
        if (requirements.getMoney() != 0 && plugin.getDependencies().getVaultEconomy() != null) {
            if (plugin.getDependencies().getVaultEconomy().getBalance(player) < requirements.getMoney()) {
                return false;
            }
        }
        if (quester.questPoints < requirements.getQuestPoints()) {
            return false;
        }
        if (!quester.completedQuests.containsAll(requirements.getNeededQuests())) {
            return false;
        }
        for (final Quest q : requirements.getBlockQuests()) {
            if (quester.completedQuests.contains(q) || quester.currentQuests.containsKey(q)) {
                return false;
            }
        }
        for (final String s : requirements.getMcmmoSkills()) {
            final SkillType st = Quests.getMcMMOSkill(s);
            final int lvl = requirements.getMcmmoAmounts().get(requirements.getMcmmoSkills().indexOf(s));
            if (UserManager.getOfflinePlayer(player).getProfile().getSkillLevel(st) < lvl) {
                return false;
            }
        }
        if (requirements.getHeroesPrimaryClass() != null) {
            if (!plugin.getDependencies().testPrimaryHeroesClass(requirements.getHeroesPrimaryClass(), player.getUniqueId())) {
                return false;
            }
        }
        if (requirements.getHeroesSecondaryClass() != null) {
            if (!plugin.getDependencies().testSecondaryHeroesClass(requirements.getHeroesSecondaryClass(),
                    player.getUniqueId())) {
                return false;
            }
        }
        if (player.isOnline()) {
            final Player p = (Player)player;
            final Inventory fakeInv = Bukkit.createInventory(null, InventoryType.PLAYER);
            fakeInv.setContents(p.getInventory().getContents().clone());
            for (final ItemStack is : requirements.getItems()) {
                if (InventoryUtil.canRemoveItem(fakeInv, is)) {
                    InventoryUtil.removeItem(fakeInv, is);
                } else {
                    return false;
                }
            }
            for (final String s : requirements.getPermissions()) {
                if (!p.hasPermission(s)) {
                    return false;
                }
            }
            for (final String s : requirements.getCustomRequirements().keySet()) {
                CustomRequirement found = null;
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getName().equalsIgnoreCase(s)) {
                        found = cr;
                        break;
                    }
                }
                if (found != null) {
                    if (!found.testRequirement(p, requirements.getCustomRequirements().get(s))) {
                        return false;
                    }
                } else {
                    plugin.getLogger().warning("Quester \"" + p.getName() + "\" attempted to take Quest \"" + name 
                            + "\", but the Custom Requirement \"" + s + "\" could not be found. Does it still exist?");
                }
            }
        }
        return true;
    }
    
    /**
     * Proceed to finish this quest, issuing applicable rewards
     *
     * @param quester The quester finishing this quest
     */
    public void completeQuest(final Quester quester) {
        completeQuest(quester, true);
    }
    
    /**
     * Proceed to finish this quest, issuing applicable rewards
     * 
     * @param quester The quester finishing this quest
     * @param allowMultiplayer Allow multiplayer sharing
     */
    @SuppressWarnings("deprecation")
    public void completeQuest(final Quester quester, final boolean allowMultiplayer) {
        final OfflinePlayer player = quester.getOfflinePlayer();
        boolean cancelled = false;
        if (player.isOnline()) {
            if (Bukkit.isPrimaryThread()) {
                final QuesterPreCompleteQuestEvent preEvent
                        = new QuesterPreCompleteQuestEvent(quester, this, false);
                plugin.getServer().getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return;
                }
            } else {
                final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    final QuesterPreCompleteQuestEvent preEvent
                            = new QuesterPreCompleteQuestEvent(quester, Quest.this, true);
                    plugin.getServer().getPluginManager().callEvent(preEvent);
                    return preEvent.isCancelled();
                });

                try {
                    cancelled = future.get();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (cancelled) {
            return;
        }
        quester.hardQuit(this);
        quester.completedQuests.add(this);
        for (final Map.Entry<Integer, Quest> entry : quester.timers.entrySet()) {
            if (entry.getValue().getName().equals(getName())) {
                plugin.getServer().getScheduler().cancelTask(entry.getKey());
                quester.timers.remove(entry.getKey());
            }
        }
        if (player.isOnline()) {
            final Player p = (Player)player;
            final String[] ps = ConfigUtil.parseStringWithPossibleLineBreaks(ChatColor.AQUA
                    + finished, this, p);
            Bukkit.getScheduler().runTaskLater(plugin, () -> p.sendMessage(ps), 40);
        }
        if (planner.getCooldown() > -1) {
            quester.completedTimes.put(this, System.currentTimeMillis());
            if (quester.amountsCompleted.containsKey(this)) {
                quester.amountsCompleted.put(this, quester.amountsCompleted.get(this) + 1);
            } else {
                quester.amountsCompleted.put(this, 1);
            }
        }
        
        // Issue rewards
        final Dependencies depends = plugin.getDependencies();
        boolean issuedReward = false;
        if (rewards.getMoney() > 0 && depends.getVaultEconomy() != null) {
            depends.getVaultEconomy().depositPlayer(player, rewards.getMoney());
            issuedReward = true;
            if (plugin.getSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded "
                        + depends.getVaultEconomy().format(rewards.getMoney()));
            }
        }
        if (player.isOnline()) {
            for (final ItemStack i : rewards.getItems()) {
                try {
                    InventoryUtil.addItem(player.getPlayer(), i);
                } catch (final Exception e) {
                    plugin.getLogger().severe("Unable to add null reward item to inventory of " 
                            + player.getName() + " upon completion of quest " + name);
                    quester.sendMessage(ChatColor.RED + "Quests encountered a problem with an item. "
                            + "Please contact an administrator.");
                }
                issuedReward = true;
                if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info(player.getUniqueId() + " was rewarded " + i.getType().name() + " x " 
                            + i.getAmount());
                }
            }
        }
        for (final String s : rewards.getCommands()) {
            if (player.getName() == null) {
                continue;
            }
            String temp = s.replace("<player>", player.getName());
            if (depends.getPlaceholderApi() != null && player.isOnline()) {
                temp = PlaceholderAPI.setPlaceholders((Player)player, temp);
            }
            final String command = temp;
            if (Bukkit.isPrimaryThread()) {
                Bukkit.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                        Bukkit.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command));
            }
            issuedReward = true;
            if (plugin.getSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded command " + s);
            }
        }
        for (int i = 0; i < rewards.getPermissions().size(); i++) {
            if (depends.getVaultPermission() != null) {
                final String perm = rewards.getPermissions().get(i);
                String world = null;
                if (i < rewards.getPermissionWorlds().size()) {
                    world = rewards.getPermissionWorlds().get(i);
                }
                if (world == null || world.equals("null")) {
                    depends.getVaultPermission().playerAdd(null, player, perm);
                } else {
                    depends.getVaultPermission().playerAdd(world, player, perm);
                }
                if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info(player.getUniqueId() + " was rewarded permission " + perm);
                }
                issuedReward = true;
            }
        }
        for (final String s : rewards.getMcmmoSkills()) {
            final int levels = rewards.getMcmmoAmounts().get(rewards.getMcmmoSkills().indexOf(s));
            UserManager.getOfflinePlayer(player).getProfile().addLevels(Quests.getMcMMOSkill(s), levels);
            if (plugin.getSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded " + s + " x " + levels);
            }
            issuedReward = true;
        }
        if (player.isOnline()) {
            for (final String s : rewards.getHeroesClasses()) {
                final Hero hero = plugin.getDependencies().getHero(player.getUniqueId());
                final double expChange = rewards.getHeroesAmounts().get(rewards.getHeroesClasses().indexOf(s));
                hero.addExp(expChange, plugin.getDependencies().getHeroes().getClassManager().getClass(s), 
                        ((Player)player).getLocation());
                if (plugin.getSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info(player.getUniqueId() + " was rewarded " + s + " x " + expChange);
                }
                issuedReward = true;
            }
        }
        if (rewards.getPartiesExperience() > 0 && depends.getPartiesApi() != null) {
            final PartyPlayer partyPlayer = depends.getPartiesApi().getPartyPlayer(player.getUniqueId());
            if (partyPlayer != null && partyPlayer.getPartyId() != null) {
                final Party party = depends.getPartiesApi().getParty(partyPlayer.getPartyId());
                if (party != null) {
                    party.giveExperience(rewards.getPartiesExperience());
                    issuedReward = true;
                    if (plugin.getSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info(player.getUniqueId() + " was rewarded "
                                + rewards.getPartiesExperience() + " party experience");
                    }
                }
            }
        }
        final LinkedList<ItemStack> phatLootItems = new LinkedList<>();
        int phatLootExp = 0;
        final LinkedList<String> phatLootMessages = new LinkedList<>();
        for (final String s : rewards.getPhatLoots()) {
            final LootBundle lb = PhatLootsAPI.getPhatLoot(s).rollForLoot();
            if (lb.getExp() > 0) {
                phatLootExp += lb.getExp();
                if (player.isOnline()) {
                    ((Player)player).giveExp(lb.getExp());
                }
            }
            if (lb.getMoney() > 0) {
                if (depends.getVaultEconomy() != null) {
                    depends.getVaultEconomy().depositPlayer(player, lb.getMoney());
                }
            }
            if (!lb.getItemList().isEmpty()) {
                phatLootItems.addAll(lb.getItemList());
                if (player.isOnline()) {
                    for (final ItemStack is : lb.getItemList()) {
                        try {
                            InventoryUtil.addItem(player.getPlayer(), is);
                        } catch (final Exception e) {
                            plugin.getLogger().severe("Unable to add PhatLoots item to inventory of "
                                    + player.getName() + " upon completion of quest " + name);
                            quester.sendMessage(ChatColor.RED + "Quests encountered a problem with an item. "
                                    + "Please contact an administrator.");
                        }
                    }
                }
            }
            if (!lb.getCommandList().isEmpty() && player.isOnline()) {
                for (final CommandLoot cl : lb.getCommandList()) {
                    cl.execute((Player)player);
                }
            }
            if (!lb.getMessageList().isEmpty()) {
                phatLootMessages.addAll(lb.getMessageList());
            }
            if (plugin.getSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded loot " + s);
            }
            issuedReward = true;
        }
        if (rewards.getExp() > 0 && player.isOnline()) {
            ((Player)player).giveExp(rewards.getExp());
            if (plugin.getSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded exp " + rewards.getExp());
            }
            issuedReward = true;
        }
        if (rewards.getQuestPoints() > 0) {
            quester.questPoints += rewards.getQuestPoints();
            if (plugin.getSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded " + rewards.getQuestPoints() + " "
                        + Lang.get("questPoints"));
            }
            issuedReward = true;
        }
        if (!rewards.getCustomRewards().isEmpty()) {
            issuedReward = true;
            if (plugin.getSettings().getConsoleLogging() > 2) {
                for (final String s : rewards.getCustomRewards().keySet()) {
                    plugin.getLogger().info(player.getUniqueId() + " was custom rewarded " + s);
                }
            }
        }
        
        // Inform player
        if (player.isOnline()) {
            final Player p = (Player)player;
            Lang.send(p, ChatColor.GOLD + Lang.get(p, "questCompleteTitle").replace("<quest>",
                    ChatColor.YELLOW + name + ChatColor.GOLD));
            if (plugin.getSettings().canShowQuestTitles()) {
                p.sendTitle(ChatColor.GOLD + Lang.get(p, "quest") + " " + Lang.get(p, "complete"),
                        ChatColor.YELLOW + name);
            }
            Lang.send(p, ChatColor.GREEN + Lang.get(p, "questRewardsTitle"));
            if (!issuedReward) {
                p.sendMessage(ChatColor.GRAY + "- (" + Lang.get("none") + ")");
            } else if (!rewards.getDetailsOverride().isEmpty()) {
                for (final String s: rewards.getDetailsOverride()) {
                    String message = ChatColor.DARK_GREEN + ConfigUtil.parseString(
                            ChatColor.translateAlternateColorCodes('&', s));
                    if (plugin.getDependencies().getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(p, message);
                    }
                    quester.sendMessage("- " + message);
                }
            } else {
                if (rewards.getQuestPoints() > 0) {
                    quester.sendMessage("- " + ChatColor.DARK_GREEN + rewards.getQuestPoints() + " "
                            + Lang.get(p, "questPoints"));
                }
                for (final ItemStack i : rewards.getItems()) {
                    StringBuilder text;
                    if (i.getItemMeta() != null && i.getItemMeta().hasDisplayName()) {
                        if (i.getEnchantments().isEmpty()) {
                            text = new StringBuilder("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName()
                                    + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount());
                        } else {
                            text = new StringBuilder("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName()
                                    + ChatColor.RESET);
                            try {
                                if (!i.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
                                    text.append(ChatColor.GRAY).append(" ").append(Lang.get(p, "with")).append(ChatColor.DARK_PURPLE);
                                    for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
                                        text.append(" ").append(ItemUtil.getPrettyEnchantmentName(e.getKey())).append(":").append(e.getValue());
                                    }
                                }
                            } catch (final Throwable tr) {
                                // Do nothing, hasItemFlag() not introduced until 1.8.6
                            }
                            text.append(ChatColor.GRAY).append(" x ").append(i.getAmount());
                        }
                    } else if (i.getDurability() != 0) {
                        text = new StringBuilder("- " + ChatColor.DARK_GREEN + "<item>:" + i.getDurability());
                        if (!i.getEnchantments().isEmpty()) {
                            text.append(ChatColor.GRAY).append(" ").append(Lang.get(p, "with"));
                            for (int iz = 0; iz < i.getEnchantments().size(); iz++) {
                                text.append(" <enchantment> <level>");
                            }
                        }
                        text.append(ChatColor.GRAY).append(" x ").append(i.getAmount());
                    } else {
                        text = new StringBuilder("- " + ChatColor.DARK_GREEN + "<item>");
                        if (!i.getEnchantments().isEmpty()) {
                            try {
                                if (!i.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
                                    text.append(ChatColor.GRAY).append(" ").append(Lang.get(p, "with"));
                                    for (int iz = 0; iz < i.getEnchantments().size(); iz++) {
                                        text.append(" <enchantment> <level>");
                                    }
                                }
                            } catch (final Throwable tr) {
                                // Do nothing, hasItemFlag() not introduced until 1.8.6
                            }
                        }
                        text.append(ChatColor.GRAY).append(" x ").append(i.getAmount());
                    }
                    if (plugin.getSettings().canTranslateNames() && text.toString().contains("<item>")) {
                        if (!plugin.getLocaleManager().sendMessage(p, text.toString(), i.getType(), i.getDurability(),
                                i.getEnchantments())) {
                            for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
                                text = new StringBuilder(text.toString().replaceFirst("<enchantment>", ItemUtil.getPrettyEnchantmentName(
                                        e.getKey())));
                                text = new StringBuilder(text.toString().replaceFirst("<level>", RomanNumeral.getNumeral(e.getValue())));
                            }
                            quester.sendMessage(text.toString().replace("<item>", ItemUtil.getName(i)));
                        }
                    } else {
                        for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
                            text = new StringBuilder(text.toString().replaceFirst("<enchantment>", ItemUtil.getPrettyEnchantmentName(
                                    e.getKey())));
                            text = new StringBuilder(text.toString().replaceFirst("<level>", RomanNumeral.getNumeral(e.getValue())));
                        }
                        quester.sendMessage(text.toString().replace("<item>", ItemUtil.getName(i)));
                    }
                }
                for (final ItemStack i : phatLootItems) {
                    if (i.getItemMeta() != null && i.getItemMeta().hasDisplayName()) {
                        if (i.getEnchantments().isEmpty()) {
                            quester.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC
                                    + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x "
                                    + i.getAmount());
                        } else {
                            quester.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC
                                    + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x "
                                    + i.getAmount() + ChatColor.DARK_PURPLE + " " + Lang.get(p, "enchantedItem"));
                        }
                    } else if (i.getDurability() != 0) {
                        if (i.getEnchantments().isEmpty()) {
                            quester.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ":"
                                    + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount());
                        } else {
                            quester.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ":"
                                    + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount()
                                    + ChatColor.DARK_PURPLE + " " + Lang.get(p, "enchantedItem"));
                        }
                    } else {
                        if (i.getEnchantments().isEmpty()) {
                            quester.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ChatColor.GRAY
                                    + " x " + i.getAmount());
                        } else {
                            quester.sendMessage("- " + ChatColor.DARK_GREEN + ItemUtil.getName(i) + ChatColor.GRAY
                                    + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " "
                                    + Lang.get(p, "enchantedItem"));
                        }
                    }
                }
                if (rewards.getMoney() > 0 && depends.getVaultEconomy() != null) {
                    quester.sendMessage("- " + ChatColor.DARK_GREEN
                            + depends.getVaultEconomy().format(rewards.getMoney()));
                }
                if (rewards.getExp() > 0 || phatLootExp > 0) {
                    final int tot = rewards.getExp() + phatLootExp;
                    quester.sendMessage("- " + ChatColor.DARK_GREEN + tot + ChatColor.DARK_PURPLE + " "
                            + Lang.get(p, "experience"));
                }
                if (!rewards.getCommands().isEmpty()) {
                    int index = 0;
                    for (final String s : rewards.getCommands()) {
                        if (!rewards.getCommandsOverrideDisplay().isEmpty()
                                && rewards.getCommandsOverrideDisplay().size() > index) {
                            if (!rewards.getCommandsOverrideDisplay().get(index).trim().equals("")) {
                                quester.sendMessage("- " + ChatColor.DARK_GREEN
                                        + rewards.getCommandsOverrideDisplay().get(index));
                            }
                        } else {
                            quester.sendMessage("- " + ChatColor.DARK_GREEN + s);
                        }
                        index++;
                    }
                }
                if (!rewards.getPermissions().isEmpty()) {
                    int index = 0;
                    for (final String s : rewards.getPermissions()) {
                        if (rewards.getPermissionWorlds() != null && rewards.getPermissionWorlds().size() > index) {
                            quester.sendMessage("- " + ChatColor.DARK_GREEN + s + " ("
                                    + rewards.getPermissionWorlds().get(index) + ")");
                        } else {
                            quester.sendMessage("- " + ChatColor.DARK_GREEN + s);
                            
                        }
                        index++;
                    }
                }
                if (!rewards.getMcmmoSkills().isEmpty()) {
                    for (final String s : rewards.getMcmmoSkills()) {
                        quester.sendMessage("- " + ChatColor.DARK_GREEN
                                + rewards.getMcmmoAmounts().get(rewards.getMcmmoSkills().indexOf(s)) + " "
                                + ChatColor.DARK_PURPLE + s + " " + Lang.get(p, "experience"));
                    }
                }
                if (!rewards.getHeroesClasses().isEmpty()) {
                    for (final String s : rewards.getHeroesClasses()) {
                        quester.sendMessage("- " + ChatColor.AQUA
                                + rewards.getHeroesAmounts().get(rewards.getHeroesClasses().indexOf(s)) + " " + ChatColor.BLUE
                                + s + " " + Lang.get(p, "experience"));
                    }
                }
                if (rewards.getPartiesExperience() > 0) {
                    p.sendMessage("- " + ChatColor.DARK_GREEN + rewards.getPartiesExperience() + ChatColor.DARK_PURPLE
                            + " " + Lang.get(p, "partiesExperience"));
                }
                if (!phatLootMessages.isEmpty()) {
                    for (final String s : phatLootMessages) {
                        quester.sendMessage("- " + s);
                    }
                }
                for (final String s : rewards.getCustomRewards().keySet()) {
                    CustomReward found = null;
                    for (final CustomReward cr : plugin.getCustomRewards()) {
                        if (cr.getName().equalsIgnoreCase(s)) {
                            found = cr;
                            break;
                        }
                    }
                    if (found != null) {
                        final Map<String, Object> dataMap = rewards.getCustomRewards().get(found.getName());
                        String message = found.getDisplay();
                        if (message != null) {
                            for (final String key : dataMap.keySet()) {
                                message = message.replace("%" + key + "%", dataMap.get(key).toString());
                            }
                            quester.sendMessage("- " + ChatColor.GOLD + message);
                        } else {
                            plugin.getLogger().warning("Failed to notify player: " 
                                    + "Custom Reward does not have an assigned name");
                        }
                        found.giveReward(p, rewards.getCustomRewards().get(s));
                    } else {
                        plugin.getLogger().warning("Quester \"" + player.getName() + "\" completed the Quest \""
                                + name + "\", but the Custom Reward \"" + s
                                + "\" could not be found. Does it still exist?");
                    }
                }
            }
        }
        quester.saveData();
        if (player.isOnline()) {
            if (player.getPlayer() != null) {
                player.getPlayer().updateInventory();
            }
        }
        quester.updateJournal();
        quester.findCompassTarget();
        if (player.isOnline()) {
            final QuesterPostCompleteQuestEvent postEvent = new QuesterPostCompleteQuestEvent(quester, this);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
        
        // Multiplayer
        if (allowMultiplayer && options.getShareProgressLevel() == 4) {
            final List<Quester> mq = quester.getMultiplayerQuesters(this);
            for (final Quester qq : mq) {
                if (qq.getQuestData(this) != null) {
                    completeQuest(qq, false);
                }
            }
        }
    }
    
    /**
     * Force player to quit quest and inform them of their failure
     * 
     * @param quester The quester to be ejected
     */
    public void failQuest(final Quester quester) {
        failQuest(quester, false);
    }
    
    /**
     * Force player to quit quest and inform them of their failure
     * 
     * @param quester The quester to be ejected
     * @param ignoreFailAction Whether to ignore quest fail Action
     */
    @SuppressWarnings("deprecation")
    public void failQuest(final Quester quester, final boolean ignoreFailAction) {
        final QuesterPreFailQuestEvent preEvent = new QuesterPreFailQuestEvent(quester, this);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        final Player player = quester.getPlayer();
        if (!ignoreFailAction) {
            final Stage stage = quester.getCurrentStage(this);
            if (stage != null && stage.getFailAction() != null) {
                quester.getCurrentStage(this).getFailAction().fire(quester, this);
            }
        }
        final String[] messages = {
                ChatColor.GOLD + Lang.get(player, "questCommandTitle").replace("<quest>", name),
                ChatColor.RED + Lang.get(player, "questFailed")
        };
        quester.quitQuest(this, messages);
        if (player.isOnline()) {
            player.updateInventory();
        }
        final QuesterPostFailQuestEvent postEvent = new QuesterPostFailQuestEvent(quester, this);
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }
    
    /**
     * Checks if quester is in WorldGuard region start
     * 
     * @deprecated Use {@link #isInRegionStart(Quester)}
     * @param quester The quester to check
     * @return true if quester is in region
     */
    @Deprecated
    public boolean isInRegion(final Quester quester) {
        return isInRegionStart(quester);
    }

    /**
     * Checks if player is in WorldGuard region start
     * 
     * @deprecated Use {@link #isInRegionStart(Player)}
     * @param player The player to check
     * @return true if player is in region
     */
    @Deprecated
    @SuppressWarnings("unused")
    private boolean isInRegion(final Player player) {
        return isInRegionStart(player);
    }
    
    /**
     * Checks if quester is in WorldGuard region start
     * 
     * @param quester The quester to check
     * @return true if quester is in region
     */
    public boolean isInRegionStart(final Quester quester) {
        return isInRegionStart(quester.getPlayer());
    }

    /**
     * Checks if player is in WorldGuard region start
     * 
     * @param player The player to check
     * @return true if player is in region
     */
    private boolean isInRegionStart(final Player player) {
        if (regionStart == null) {
            return false;
        }
        return plugin.getDependencies().getWorldGuardApi()
                .getApplicableRegionsIDs(player.getWorld(), player.getLocation()).contains(regionStart);
    }
}
