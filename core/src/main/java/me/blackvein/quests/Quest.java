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
import me.blackvein.quests.actions.IAction;
import me.blackvein.quests.actions.Action;
import me.blackvein.quests.conditions.ICondition;
import me.blackvein.quests.dependencies.IDependencies;
import me.blackvein.quests.events.quest.QuestUpdateCompassEvent;
import me.blackvein.quests.events.quester.QuesterPostChangeStageEvent;
import me.blackvein.quests.events.quester.QuesterPostCompleteQuestEvent;
import me.blackvein.quests.events.quester.QuesterPostFailQuestEvent;
import me.blackvein.quests.events.quester.QuesterPreChangeStageEvent;
import me.blackvein.quests.events.quester.QuesterPreCompleteQuestEvent;
import me.blackvein.quests.events.quester.QuesterPreFailQuestEvent;
import me.blackvein.quests.nms.TitleProvider;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.BukkitOptions;
import me.blackvein.quests.quests.BukkitPlanner;
import me.blackvein.quests.quests.BukkitRequirements;
import me.blackvein.quests.quests.BukkitRewards;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.quests.IStage;
import me.blackvein.quests.quests.Options;
import me.blackvein.quests.quests.Planner;
import me.blackvein.quests.quests.Requirements;
import me.blackvein.quests.quests.Rewards;
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
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Quest implements IQuest {

    protected Quests plugin;
    protected String id;
    private String name;
    protected String description;
    protected String finished;
    protected ItemStack guiDisplay = null;
    private final LinkedList<IStage> orderedStages = new LinkedList<>();
    protected UUID npcStart;
    protected Location blockStart;
    protected String regionStart = null;
    protected Action initialAction;
    private final BukkitRequirements requirements = new BukkitRequirements();
    private final BukkitPlanner planner = new BukkitPlanner();
    private final BukkitRewards rewards = new BukkitRewards();
    private final BukkitOptions options = new BukkitOptions();

    public Quest(final Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public int compareTo(final IQuest quest) {
        return id.compareTo(quest.getId());
    }

    public Quests getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        if (plugin instanceof Quests) {
            this.plugin = (Quests)plugin;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        if (plugin != null) {
            this.id = id;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getFinished() {
        return finished;
    }

    @Override
    public void setFinished(final String finished) {
        this.finished = finished;
    }

    @Override
    public String getRegionStart() {
        return regionStart;
    }

    @Override
    public void setRegionStart(final String regionStart) {
        this.regionStart = regionStart;
    }

    @Override
    public ItemStack getGUIDisplay() {
        return guiDisplay;
    }

    @Override
    public void setGUIDisplay(final ItemStack guiDisplay) {
        this.guiDisplay = guiDisplay;
    }

    @Override
    public IStage getStage(final int index) {
        try {
            return orderedStages.get(index);
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public LinkedList<IStage> getStages() {
        return orderedStages;
    }

    @Override
    public NPC getNpcStart() {
        if (CitizensAPI.getNPCRegistry().getByUniqueId(npcStart) != null) {
            return CitizensAPI.getNPCRegistry().getByUniqueId(npcStart);
        }
        return null;
    }

    @Override
    public void setNpcStart(final NPC npcStart) {
        this.npcStart = npcStart.getUniqueId();
    }

    @Override
    public Location getBlockStart() {
        return blockStart;
    }

    @Override
    public void setBlockStart(final Location blockStart) {
        this.blockStart = blockStart;
    }

    @Override
    public IAction getInitialAction() {
        return initialAction;
    }

    @Override
    public void setInitialAction(final IAction initialAction) {
        this.initialAction = (Action) initialAction;
    }

    @Override
    public Requirements getRequirements() {
        return requirements;
    }

    @Override
    public Planner getPlanner() {
        return planner;
    }

    @Override
    public Rewards getRewards() {
        return rewards;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    /**
     * Force player to proceed to the next ordered stage
     * 
     * @param quester Player to force
     * @param allowSharedProgress Whether to distribute progress to fellow questers
     */
    public void nextStage(final IQuester quester, final boolean allowSharedProgress) {
        final IStage currentStage = quester.getCurrentStage(this);
        if (currentStage == null) {
            plugin.getLogger().severe("Current stage was null for quester " + quester.getPlayer().getUniqueId());
            return;
        }
        final String stageCompleteMessage = currentStage.getCompleteMessage();
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
        if (currentStage.getDelay() < 0) {
            if (currentStage.getFinishAction() != null) {
                currentStage.getFinishAction().fire(quester, this);
            }
            if (quester.getCurrentQuestsTemp().get(this) == (orderedStages.size() - 1)) {
                if (currentStage.getScript() != null) {
                    plugin.getDenizenTrigger().runDenizenScript(currentStage.getScript(), quester);
                }
                completeQuest(quester);
            } else {
                setStage(quester, quester.getCurrentQuestsTemp().get(this) + 1);
            }
            if (quester.getQuestData(this) != null) {
                quester.getQuestData(this).setDelayStartTime(0);
                quester.getQuestData(this).setDelayTimeLeft(-1);
            }
            
            // Multiplayer
            if (allowSharedProgress && options.getShareProgressLevel() == 3) {
                final List<IQuester> mq = quester.getMultiplayerQuesters(this);
                for (final IQuester qq : mq) {
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
     * @param stage IStage number to specify
     * @throws IndexOutOfBoundsException if stage does not exist
     */
    public void setStage(final IQuester quester, final int stage) throws IndexOutOfBoundsException {
        final OfflinePlayer player = quester.getOfflinePlayer();
        if (orderedStages.size() - 1 < stage) {
            final String msg = "Tried to set invalid stage number of " + stage + " for quest " + getName() + " on " 
                    + player.getName();
            throw new IndexOutOfBoundsException(msg);
        }
        final IStage currentStage = quester.getCurrentStage(this);
        final IStage nextStage = getStage(stage);
        if (currentStage == null || nextStage == null) {
            return;
        }
        if (player.isOnline()) {
            final QuesterPreChangeStageEvent preEvent
                    = new QuesterPreChangeStageEvent((Quester) quester, this, currentStage, nextStage);
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
        }
        quester.hardQuit(this);
        quester.hardStagePut(this, stage);
        quester.addEmptiesFor(this, stage);
        if (currentStage.getScript() != null) {
            plugin.getDenizenTrigger().runDenizenScript(currentStage.getScript(), quester);
        }
        if (nextStage.getStartAction() != null) {
            nextStage.getStartAction().fire(quester, this);
        }
        updateCompass(quester, nextStage);
        if (player.isOnline()) {
            final Player p = quester.getPlayer();
            final String title = Lang.get(p, "objectives").replace("<quest>", name);
            quester.sendMessage(ChatColor.GOLD + title);
            plugin.showObjectives(this, quester, false);
            if (quester.getCurrentStage(this) == null) {
                quester.sendMessage(ChatColor.RED + "itemCreateCriticalError");
                plugin.getLogger().severe("Could not set stage for quest ID " + getId()
                        + " because the current stage for player " + quester.getLastKnownName() + " was null");
                return;
            }
            final String stageStartMessage = quester.getCurrentStage(this).getStartMessage();
            if (stageStartMessage != null) {
                p.sendMessage(ConfigUtil.parseStringWithPossibleLineBreaks(stageStartMessage, this, p));
            }
            final ICondition c = nextStage.getCondition();
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
        quester.saveData();
        if (player.isOnline()) {
            final QuesterPostChangeStageEvent postEvent
                    = new QuesterPostChangeStageEvent((Quester) quester, this, currentStage, nextStage);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
    }

    /**
     * Attempt to set location-objective target for compass.<p>
     * 
     * Method may be called as often as needed.
     * 
     * @param quester The online quester to have their compass updated
     * @param stage The stage to process for targets
     * @return true if quester is online and has permission
     */
    public boolean updateCompass(final IQuester quester, final IStage stage) {
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
        final IQuest quest = this;
        Bukkit.getScheduler().runTask(plugin, () -> {
            Location targetLocation = null;
            if (stage.getNpcsToInteract() != null && stage.getNpcsToInteract().size() > 0) {
                targetLocation = plugin.getDependencies().getNPCLocation(stage.getNpcsToInteract().getFirst());
            } else if (stage.getNpcsToKill() != null && stage.getNpcsToKill().size() > 0) {
                targetLocation = plugin.getDependencies().getNPCLocation(stage.getNpcsToKill().getFirst());
            } else if (stage.getLocationsToReach() != null && stage.getLocationsToReach().size() > 0) {
                targetLocation = stage.getLocationsToReach().getFirst();
            } else if (stage.getItemDeliveryTargets() != null && stage.getItemDeliveryTargets().size() > 0) {
                final NPC npc = plugin.getDependencies().getCitizens().getNPCRegistry().getByUniqueId(stage
                        .getItemDeliveryTargets().getFirst());
                targetLocation = npc.getStoredLocation();
            } else if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
                if (quester.getPlayer() == null) {
                    return;
                }
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
            } else if (stage.getMobsToKill() != null && stage.getMobsToKill().size() > 0) {
                if (quester.getPlayer() == null) {
                    return;
                }
                final Location source = quester.getPlayer().getLocation();
                Location nearest = null;
                double old_distance = 30000000;
                final EntityType et = stage.getMobsToKill().getFirst();
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
            } else if (stage.getMobsToTame() != null && stage.getMobsToTame().size() > 0) {
                if (quester.getPlayer() == null) {
                    return;
                }
                final Location source = quester.getPlayer().getLocation();
                Location nearest = null;
                double old_distance = 30000000;
                final EntityType et = stage.getMobsToTame().getFirst();
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
            } else if (stage.getSheepToShear() != null && stage.getSheepToShear().size() > 0) {
                if (quester.getPlayer() == null) {
                    return;
                }
                final Location source = quester.getPlayer().getLocation();
                Location nearest = null;
                double old_distance = 30000000;
                final DyeColor dc = stage.getSheepToShear().getFirst();
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
                if (quester.getPlayer() == null) {
                    return;
                }
                if (targetLocation.getWorld().getName().equals(quester.getPlayer().getWorld().getName())) {
                    final Location lockedTarget = new Location(targetLocation.getWorld(), targetLocation.getX(),
                            targetLocation.getY(), targetLocation.getZ());
                    final QuestUpdateCompassEvent event = new QuestUpdateCompassEvent(quest, quester, lockedTarget);
                    plugin.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    quester.setCompassTarget(this);
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
    public boolean testRequirements(final IQuester quester) {
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
    public boolean testRequirements(final OfflinePlayer player) {
        final IQuester quester = plugin.getQuester(player.getUniqueId());
        if (requirements.getMoney() != 0 && plugin.getDependencies().getVaultEconomy() != null) {
            if (plugin.getDependencies().getVaultEconomy().getBalance(player) < requirements.getMoney()) {
                return false;
            }
        }
        if (quester.getQuestPoints() < requirements.getQuestPoints()) {
            return false;
        }
        if (!quester.getCompletedQuestsTemp().containsAll(requirements.getNeededQuests())) {
            return false;
        }
        for (final IQuest q : requirements.getBlockQuests()) {
            if (quester.getCompletedQuestsTemp().contains(q) || quester.getCurrentQuestsTemp().containsKey(q)) {
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
    public void completeQuest(final IQuester quester) {
        completeQuest(quester, true);
    }
    
    /**
     * Proceed to finish this quest, issuing applicable rewards
     * 
     * @param quester The quester finishing this quest
     * @param allowMultiplayer Allow multiplayer sharing
     */
    @SuppressWarnings("deprecation")
    public void completeQuest(final IQuester quester, final boolean allowMultiplayer) {
        final OfflinePlayer player = quester.getOfflinePlayer();
        boolean cancelled = false;
        if (player.isOnline()) {
            if (Bukkit.isPrimaryThread()) {
                final QuesterPreCompleteQuestEvent preEvent
                        = new QuesterPreCompleteQuestEvent((Quester) quester, this, false);
                plugin.getServer().getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return;
                }
            } else {
                final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    final QuesterPreCompleteQuestEvent preEvent
                            = new QuesterPreCompleteQuestEvent((Quester) quester, Quest.this, true);
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
        quester.getCompletedQuestsTemp().add(this);
        for (final Map.Entry<Integer, IQuest> entry : quester.getTimers().entrySet()) {
            if (entry.getValue().getName().equals(getName())) {
                plugin.getServer().getScheduler().cancelTask(entry.getKey());
                quester.getTimers().remove(entry.getKey());
            }
        }
        if (player.isOnline()) {
            final Player p = (Player)player;
            final String[] ps = ConfigUtil.parseStringWithPossibleLineBreaks(ChatColor.AQUA
                    + finished, this, p);
            Bukkit.getScheduler().runTaskLater(plugin, () -> p.sendMessage(ps), 40);
        }
        if (planner.getCooldown() > -1) {
            quester.getCompletedTimes().put(this, System.currentTimeMillis());
            if (quester.getAmountsCompleted().containsKey(this)) {
                quester.getAmountsCompleted().put(this, quester.getAmountsCompleted().get(this) + 1);
            } else {
                quester.getAmountsCompleted().put(this, 1);
            }
        }
        
        // Issue rewards
        final IDependencies depends = plugin.getDependencies();
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
            quester.setQuestPoints(quester.getQuestPoints() + rewards.getQuestPoints());
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
                final String title = ChatColor.GOLD + Lang.get(p, "quest") + " " + Lang.get(p, "complete");
                final String subtitle = ChatColor.YELLOW + name;
                TitleProvider.sendTitle(p, title, subtitle);
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
            final QuesterPostCompleteQuestEvent postEvent = new QuesterPostCompleteQuestEvent((Quester) quester, this);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
        
        // Multiplayer
        if (allowMultiplayer && options.getShareProgressLevel() == 4) {
            final List<IQuester> mq = quester.getMultiplayerQuesters(this);
            for (final IQuester qq : mq) {
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
    public void failQuest(final IQuester quester) {
        failQuest(quester, false);
    }
    
    /**
     * Force player to quit quest and inform them of their failure
     * 
     * @param quester The quester to be ejected
     * @param ignoreFailAction Whether to ignore quest fail IAction
     */
    public void failQuest(final IQuester quester, final boolean ignoreFailAction) {
        final QuesterPreFailQuestEvent preEvent = new QuesterPreFailQuestEvent((Quester) quester, this);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        final Player player = quester.getPlayer();
        if (!ignoreFailAction) {
            final IStage stage = quester.getCurrentStage(this);
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
        final QuesterPostFailQuestEvent postEvent = new QuesterPostFailQuestEvent((Quester) quester, this);
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }
    
    /**
     * Checks if quester is in WorldGuard region start
     * 
     * @deprecated Use {@link #isInRegionStart(IQuester)}
     * @param quester The quester to check
     * @return true if quester is in region
     */
    @Deprecated
    public boolean isInRegion(final IQuester quester) {
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
    public boolean isInRegionStart(final IQuester quester) {
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
