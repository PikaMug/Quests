/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests;

import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.characters.Hero;
import io.github.znetworkw.znpcservers.npc.NPC;
import lol.pyr.znpcsplus.api.npc.Npc;
import me.clip.placeholderapi.PlaceholderAPI;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.actions.BukkitAction;
import me.pikamug.quests.dependencies.BukkitDependencies;
import me.pikamug.quests.events.quest.QuestUpdateCompassEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPostChangeStageEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPostCompleteQuestEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPostFailQuestEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreChangeStageEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreCompleteQuestEvent;
import me.pikamug.quests.events.quester.BukkitQuesterPreFailQuestEvent;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.module.CustomReward;
import me.pikamug.quests.nms.BukkitTitleProvider;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.BukkitOptions;
import me.pikamug.quests.quests.components.BukkitPlanner;
import me.pikamug.quests.quests.components.BukkitRequirements;
import me.pikamug.quests.quests.components.BukkitRewards;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitInventoryUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.RomanNumeral;
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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class BukkitQuest implements Quest {

    protected BukkitQuestsPlugin plugin;
    protected String id;
    private String name;
    protected String description;
    protected String finished;
    protected ItemStack guiDisplay = null;
    private final LinkedList<Stage> orderedStages = new LinkedList<>();
    protected UUID npcStart;
    protected Location blockStart;
    protected String regionStart = null;
    protected BukkitAction initialAction;
    private BukkitRequirements requirements = new BukkitRequirements();
    private BukkitPlanner planner = new BukkitPlanner();
    private BukkitRewards rewards = new BukkitRewards();
    private BukkitOptions options = new BukkitOptions();

    public BukkitQuest(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int compareTo(final Quest quest) {
        return id.compareTo(quest.getId());
    }

    /*public BukkitQuestsPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        if (plugin instanceof BukkitQuestsPlugin) {
            this.plugin = (BukkitQuestsPlugin)plugin;
        }
    }*/

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

    public ItemStack getGUIDisplay() {
        return guiDisplay;
    }

    public void setGUIDisplay(final ItemStack guiDisplay) {
        this.guiDisplay = guiDisplay;
    }

    @Override
    public Stage getStage(final int index) {
        try {
            return orderedStages.get(index);
        } catch (final Exception e) {
            return null;
        }
    }

    @Override
    public LinkedList<Stage> getStages() {
        return orderedStages;
    }

    @Override
    public UUID getNpcStart() {
        return npcStart;
    }

    @Override
    public void setNpcStart(final UUID npcStart) {
        this.npcStart = npcStart;
    }

    @Override
    public String getNpcStartName() {
        return plugin.getDependencies().getNpcName(getNpcStart());
    }

    public Location getBlockStart() {
        return blockStart;
    }

    public void setBlockStart(final Location blockStart) {
        this.blockStart = blockStart;
    }

    @Override
    public Action getInitialAction() {
        return initialAction;
    }

    @Override
    public void setInitialAction(final Action initialAction) {
        this.initialAction = (BukkitAction) initialAction;
    }

    @Override
    public Requirements getRequirements() {
        return requirements;
    }

    @Override
    public void setRequirements(final Requirements requirements) {
        this.requirements = (BukkitRequirements) requirements;
    }

    @Override
    public Planner getPlanner() {
        return planner;
    }

    @Override
    public void setPlanner(final Planner planner) {
        this.planner = (BukkitPlanner) planner;
    }

    @Override
    public Rewards getRewards() {
        return rewards;
    }

    @Override
    public void setRewards(final Rewards rewards) {
        this.rewards = (BukkitRewards) rewards;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void setOptions(final Options options) {
        this.options = (BukkitOptions) options;
    }

    /**
     * Force player to proceed to the next ordered stage
     * 
     * @param quester Player to force progression
     * @param allowSharedProgress Whether to distribute progress to fellow questers
     */
    public void nextStage(final Quester quester, final boolean allowSharedProgress) {
        final Stage currentStage = quester.getCurrentStage(this);
        if (currentStage == null) {
            plugin.getLogger().severe("Current stage was null for quester " + quester.getPlayer().getUniqueId());
            return;
        }
        final String stageCompleteMessage = currentStage.getCompleteMessage();
        if (stageCompleteMessage != null) {
            if (quester.getOfflinePlayer().isOnline()) {
                quester.getPlayer().sendMessage(BukkitConfigUtil.parseStringWithPossibleLineBreaks(stageCompleteMessage,
                        this, quester.getPlayer()));
            }
        }
        if (currentStage.getDelay() < 0) {
            doNextStage(quester, allowSharedProgress);
        } else {
            // Here we avoid BukkitStageTimer as the stage objectives are incomplete
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                doNextStage(quester, allowSharedProgress);
            }, (long) (currentStage.getDelay() * 0.02));
        }
        quester.updateJournal();
    }

    private void doNextStage(final Quester quester, final boolean allowSharedProgress) {
        final Stage currentStage = quester.getCurrentStage(this);
        if (currentStage.getFinishAction() != null) {
            currentStage.getFinishAction().fire(quester, this);
        }
        if (quester.getCurrentQuests().get(this) == (orderedStages.size() - 1)) {
            if (currentStage.getScript() != null) {
                plugin.getDenizenTrigger().runDenizenScript(currentStage.getScript(), quester, null);
            }
            completeQuest(quester);
        } else {
            setStage(quester, quester.getCurrentQuests().get(this) + 1);
        }
        if (quester.getQuestDataOrDefault(this) != null) {
            quester.getQuestDataOrDefault(this).setDelayStartTime(0);
            quester.getQuestDataOrDefault(this).setDelayTimeLeft(-1);
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
    }

    /**
     * Force player to proceed to the specified stage
     * 
     * @param quester Player to force progression
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
        if (currentStage == null || nextStage == null) {
            return;
        }
        if (player.isOnline()) {
            final BukkitQuesterPreChangeStageEvent preEvent
                    = new BukkitQuesterPreChangeStageEvent((BukkitQuester) quester, this, currentStage, nextStage);
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
        }
        quester.hardQuit(this);
        quester.hardStagePut(this, stage);
        quester.addEmptiesFor(this, stage);
        if (currentStage.getScript() != null) {
            plugin.getDenizenTrigger().runDenizenScript(currentStage.getScript(), quester, null);
        }
        if (nextStage.getStartAction() != null) {
            nextStage.getStartAction().fire(quester, this);
        }
        updateCompass(quester, nextStage);
        if (player.isOnline()) {
            final Player p = quester.getPlayer();
            final String title = BukkitLang.get(p, "objectives").replace("<quest>", name);
            quester.sendMessage(ChatColor.GOLD + title);
            quester.showCurrentObjectives(this, quester, false);
            if (quester.getCurrentStage(this) == null) {
                quester.sendMessage(ChatColor.RED + "itemCreateCriticalError");
                plugin.getLogger().severe("Could not set stage for quest ID " + getId()
                        + " because the current stage for player " + quester.getLastKnownName() + " was null");
                return;
            }
            final String stageStartMessage = quester.getCurrentStage(this).getStartMessage();
            if (stageStartMessage != null) {
                p.sendMessage(BukkitConfigUtil.parseStringWithPossibleLineBreaks(stageStartMessage, this, p));
            }
            quester.showCurrentConditions(this, quester);
        }
        quester.updateJournal();
        quester.saveData();
        if (player.isOnline()) {
            final BukkitQuesterPostChangeStageEvent postEvent
                    = new BukkitQuesterPostChangeStageEvent((BukkitQuester) quester, this, currentStage, nextStage);
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
            if (stage.getNpcsToInteract() != null && stage.getNpcsToInteract().size() > 0) {
                targetLocation = plugin.getDependencies().getNpcLocation(stage.getNpcsToInteract().getFirst());
            } else if (stage.getNpcsToKill() != null && stage.getNpcsToKill().size() > 0) {
                targetLocation = plugin.getDependencies().getNpcLocation(stage.getNpcsToKill().getFirst());
            } else if (stage.getLocationsToReach() != null && stage.getLocationsToReach().size() > 0) {
                targetLocation = (Location) stage.getLocationsToReach().getFirst();
            } else if (stage.getItemDeliveryTargets() != null && stage.getItemDeliveryTargets().size() > 0) {
                final UUID uuid = stage.getItemDeliveryTargets().getFirst();
                if (plugin.getDependencies().getCitizens() != null
                        && plugin.getDependencies().getCitizens().getNPCRegistry().getByUniqueId(uuid) != null) {
                    targetLocation = plugin.getDependencies().getCitizens().getNPCRegistry().getByUniqueId(uuid)
                            .getStoredLocation();
                }
                if (plugin.getDependencies().getZnpcsPlus() != null
                        && plugin.getDependencies().getZnpcsPlusUuids().contains(uuid)) {
                    final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(uuid)).findAny();
                    if (opt.isPresent()) {
                        targetLocation = opt.get().getLocation();
                    }
                }
                if (plugin.getDependencies().getZnpcsPlusApi() != null
                        && plugin.getDependencies().getZnpcsPlusApi().getNpcRegistry().getByUuid(uuid) != null) {
                    Npc znpc = plugin.getDependencies().getZnpcsPlusApi().getNpcRegistry().getByUuid(uuid).getNpc();
                    targetLocation = znpc.getLocation().toBukkitLocation(znpc.getWorld());
                }
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
                final EntityType et = (EntityType) stage.getMobsToKill().getFirst();
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
                final EntityType et = (EntityType) stage.getMobsToTame().getFirst();
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
                final DyeColor dc = (DyeColor) stage.getSheepToShear().getFirst();
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
     * Format GUI display item with applicable display name, lore, and item flags
     *
     * @param quester The quester to prepare for
     * @return formatted item
     */
    public ItemStack prepareDisplay(final BukkitQuester quester) {
        final ItemStack display = getGUIDisplay().clone();
        final ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            final Player player = quester.getPlayer();
            if (quester.getCompletedQuests().contains(this)) {
                meta.setDisplayName(ChatColor.DARK_PURPLE + BukkitConfigUtil.parseString(getName()
                        + " " + ChatColor.GREEN + BukkitLang.get(player, "redoCompleted"), getNpcStart()));
            } else {
                meta.setDisplayName(ChatColor.DARK_PURPLE + BukkitConfigUtil.parseString(getName(), getNpcStart()));
            }
            if (!meta.hasLore()) {
                final LinkedList<String> lines;
                String desc = getDescription();
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    desc = PlaceholderAPI.setPlaceholders(player, desc);
                }
                if (desc.equals(ChatColor.stripColor(desc))) {
                    lines = BukkitMiscUtil.makeLines(desc, " ", 40, ChatColor.DARK_GREEN);
                } else {
                    lines = BukkitMiscUtil.makeLines(desc, " ", 40, null);
                }
                meta.setLore(lines);
            }
            meta.addItemFlags(ItemFlag.values());
            display.setItemMeta(meta);
        }
        return display;
    }
    
    /**
     * Check that a quester has met all Requirements to accept this quest<p>
     * 
     * Item, experience, permission and custom Requirements are only checked for online players
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
     * Item, experience, permission and custom Requirements are only checked for online players
     * 
     * @param player The player to check
     * @return true if all Requirements have been met
     */
    public boolean testRequirements(final OfflinePlayer player) {
        final Quester quester = plugin.getQuester(player.getUniqueId());
        if (requirements.getMoney() != 0 && plugin.getDependencies().getVaultEconomy() != null) {
            if (plugin.getDependencies().getVaultEconomy().getBalance(player) < requirements.getMoney()) {
                return false;
            }
        }
        if (quester.getQuestPoints() < requirements.getQuestPoints()) {
            return false;
        }
        final Set<String> completed = quester.getCompletedQuests().stream().map(Quest::getId)
                .collect(Collectors.toSet());
        if (!requirements.getNeededQuestIds().isEmpty()
                && !completed.containsAll(requirements.getNeededQuestIds())) {
            return false;
        }
        if (!requirements.getBlockQuestIds().isEmpty()) {
            for (final String questId : requirements.getBlockQuestIds()) {
                if (completed.contains(questId)) {
                    return false;
                }
            }
            for (final Quest q : quester.getCurrentQuests().keySet()) {
                if (!requirements.getBlockQuestIds().contains(q.getId())) {
                    return false;
                }
            }
        }
        for (final String s : requirements.getMcmmoSkills()) {
            final SkillType st = plugin.getDependencies().getMcMMOSkill(s);
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
            if (p.getTotalExperience() < requirements.getExp()) {
                return false;
            }
            final Inventory fakeInv = Bukkit.createInventory(null, InventoryType.PLAYER);
            fakeInv.setContents(p.getInventory().getContents().clone());
            for (final ItemStack is : requirements.getItems()) {
                if (BukkitInventoryUtil.canRemoveItem(fakeInv, is)) {
                    BukkitInventoryUtil.removeItem(fakeInv, is);
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
                    if (!found.testRequirement(p.getUniqueId(), requirements.getCustomRequirements().get(s))) {
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
                final BukkitQuesterPreCompleteQuestEvent preEvent
                        = new BukkitQuesterPreCompleteQuestEvent((BukkitQuester) quester, this, false);
                plugin.getServer().getPluginManager().callEvent(preEvent);
                if (preEvent.isCancelled()) {
                    return;
                }
            } else {
                final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    final BukkitQuesterPreCompleteQuestEvent preEvent
                            = new BukkitQuesterPreCompleteQuestEvent((BukkitQuester) quester, BukkitQuest.this, true);
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
        final ConcurrentSkipListSet<Quest> completedQuests = quester.getCompletedQuests();
        completedQuests.add(this);
        quester.setCompletedQuests(completedQuests);
        for (final Map.Entry<Integer, Quest> entry : quester.getTimers().entrySet()) {
            if (entry.getValue().getName().equals(getName())) {
                plugin.getServer().getScheduler().cancelTask(entry.getKey());
                quester.getTimers().remove(entry.getKey());
            }
        }
        if (player.isOnline()) {
            final Player p = (Player)player;
            final String[] ps = BukkitConfigUtil.parseStringWithPossibleLineBreaks(ChatColor.AQUA
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
        final BukkitDependencies depends = plugin.getDependencies();
        boolean issuedReward = false;
        if (rewards.getMoney() > 0 && depends.getVaultEconomy() != null) {
            depends.getVaultEconomy().depositPlayer(player, rewards.getMoney());
            issuedReward = true;
            if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded "
                        + depends.getVaultEconomy().format(rewards.getMoney()));
            }
        }
        if (player.isOnline()) {
            for (final ItemStack i : rewards.getItems()) {
                try {
                    BukkitInventoryUtil.addItem(player.getPlayer(), i);
                } catch (final Exception e) {
                    plugin.getLogger().severe("Unable to add null reward item to inventory of " 
                            + player.getName() + " upon completion of quest " + name);
                    quester.sendMessage(ChatColor.RED + "Quests encountered a problem with an item. "
                            + "Please contact an administrator.");
                }
                issuedReward = true;
                if (plugin.getConfigSettings().getConsoleLogging() > 2) {
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
            if (plugin.getConfigSettings().getConsoleLogging() > 2) {
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
                if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                    plugin.getLogger().info(player.getUniqueId() + " was rewarded permission " + perm);
                }
                issuedReward = true;
            }
        }
        for (final String s : rewards.getMcmmoSkills()) {
            final int levels = rewards.getMcmmoAmounts().get(rewards.getMcmmoSkills().indexOf(s));
            UserManager.getOfflinePlayer(player).getProfile().addLevels(plugin.getDependencies().getMcMMOSkill(s), levels);
            if (plugin.getConfigSettings().getConsoleLogging() > 2) {
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
                if (plugin.getConfigSettings().getConsoleLogging() > 2) {
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
                    if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                        plugin.getLogger().info(player.getUniqueId() + " was rewarded "
                                + rewards.getPartiesExperience() + " party experience");
                    }
                }
            }
        }
        if (rewards.getExp() > 0 && player.isOnline()) {
            ((Player)player).giveExp(rewards.getExp());
            if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded exp " + rewards.getExp());
            }
            issuedReward = true;
        }
        if (rewards.getQuestPoints() > 0) {
            quester.setQuestPoints(quester.getQuestPoints() + rewards.getQuestPoints());
            if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                plugin.getLogger().info(player.getUniqueId() + " was rewarded " + rewards.getQuestPoints() + " "
                        + BukkitLang.get("questPoints"));
            }
            issuedReward = true;
        }
        if (!rewards.getCustomRewards().isEmpty()) {
            issuedReward = true;
            if (plugin.getConfigSettings().getConsoleLogging() > 2) {
                for (final String s : rewards.getCustomRewards().keySet()) {
                    plugin.getLogger().info(player.getUniqueId() + " was custom rewarded " + s);
                }
            }
        }
        
        // Inform player
        if (player.isOnline()) {
            final Player p = (Player)player;
            BukkitLang.send(p, ChatColor.GOLD + BukkitLang.get(p, "questCompleteTitle").replace("<quest>",
                    ChatColor.YELLOW + name + ChatColor.GOLD));
            if (plugin.getConfigSettings().canShowQuestTitles()) {
                final String title = ChatColor.GOLD + BukkitLang.get(p, "quest") + " " + BukkitLang.get(p, "complete");
                final String subtitle = ChatColor.YELLOW + name;
                BukkitTitleProvider.sendTitle(p, title, subtitle);
            }
            BukkitLang.send(p, ChatColor.GREEN + BukkitLang.get(p, "questRewardsTitle"));
            if (!issuedReward) {
                p.sendMessage(ChatColor.GRAY + "- (" + BukkitLang.get("none") + ")");
            } else if (!rewards.getDetailsOverride().isEmpty()) {
                for (final String s: rewards.getDetailsOverride()) {
                    String message = ChatColor.DARK_GREEN + BukkitConfigUtil.parseString(
                            ChatColor.translateAlternateColorCodes('&', s));
                    if (plugin.getDependencies().getPlaceholderApi() != null) {
                        message = PlaceholderAPI.setPlaceholders(p, message);
                    }
                    quester.sendMessage("- " + message);
                }
            } else {
                if (rewards.getExp() > 0) {
                    quester.sendMessage("- " + ChatColor.DARK_GREEN + rewards.getExp() + " "
                            + BukkitLang.get(p, "experience"));
                }
                if (rewards.getQuestPoints() > 0) {
                    quester.sendMessage("- " + ChatColor.DARK_GREEN + rewards.getQuestPoints() + " "
                            + BukkitLang.get(p, "questPoints"));
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
                                    text.append(ChatColor.GRAY).append(" ").append(BukkitLang.get(p, "with")).append(ChatColor.DARK_PURPLE);
                                    for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
                                        text.append(" ").append(BukkitItemUtil.getPrettyEnchantmentName(e.getKey())).append(":").append(e.getValue());
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
                            text.append(ChatColor.GRAY).append(" ").append(BukkitLang.get(p, "with"));
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
                                    text.append(ChatColor.GRAY).append(" ").append(BukkitLang.get(p, "with"));
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
                    if (plugin.getConfigSettings().canTranslateNames() && text.toString().contains("<item>")) {
                        if (!plugin.getLocaleManager().sendMessage(p, text.toString(), i.getType(), i.getDurability(),
                                i.getEnchantments())) {
                            for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
                                text = new StringBuilder(text.toString().replaceFirst("<enchantment>", BukkitItemUtil.getPrettyEnchantmentName(
                                        e.getKey())));
                                text = new StringBuilder(text.toString().replaceFirst("<level>", RomanNumeral.getNumeral(e.getValue())));
                            }
                            quester.sendMessage(text.toString().replace("<item>", BukkitItemUtil.getName(i)));
                        }
                    } else {
                        for (final Entry<Enchantment, Integer> e : i.getEnchantments().entrySet()) {
                            text = new StringBuilder(text.toString().replaceFirst("<enchantment>", BukkitItemUtil.getPrettyEnchantmentName(
                                    e.getKey())));
                            text = new StringBuilder(text.toString().replaceFirst("<level>", RomanNumeral.getNumeral(e.getValue())));
                        }
                        quester.sendMessage(text.toString().replace("<item>", BukkitItemUtil.getName(i)));
                    }
                }
                if (rewards.getMoney() > 0 && depends.getVaultEconomy() != null) {
                    quester.sendMessage("- " + ChatColor.DARK_GREEN
                            + depends.getVaultEconomy().format(rewards.getMoney()));
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
                                + ChatColor.DARK_PURPLE + s + " " + BukkitLang.get(p, "experience"));
                    }
                }
                if (!rewards.getHeroesClasses().isEmpty()) {
                    for (final String s : rewards.getHeroesClasses()) {
                        quester.sendMessage("- " + ChatColor.AQUA
                                + rewards.getHeroesAmounts().get(rewards.getHeroesClasses().indexOf(s)) + " " + ChatColor.BLUE
                                + s + " " + BukkitLang.get(p, "experience"));
                    }
                }
                if (rewards.getPartiesExperience() > 0) {
                    p.sendMessage("- " + ChatColor.DARK_GREEN + rewards.getPartiesExperience() + ChatColor.DARK_PURPLE
                            + " " + BukkitLang.get(p, "partiesExperience"));
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
                        found.giveReward(p.getUniqueId(), rewards.getCustomRewards().get(s));
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
            final BukkitQuesterPostCompleteQuestEvent postEvent = new BukkitQuesterPostCompleteQuestEvent((BukkitQuester) quester, this);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }
        
        // Multiplayer
        if (allowMultiplayer && options.getShareProgressLevel() == 4) {
            final List<Quester> mq = quester.getMultiplayerQuesters(this);
            for (final Quester qq : mq) {
                if (qq.getQuestDataOrDefault(this) != null) {
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
    public void failQuest(final Quester quester, final boolean ignoreFailAction) {
        final BukkitQuesterPreFailQuestEvent preEvent = new BukkitQuesterPreFailQuestEvent((BukkitQuester) quester, this);
        plugin.getServer().getPluginManager().callEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        for (final Map.Entry<Integer, Quest> entry : quester.getTimers().entrySet()) {
            if (entry.getValue().getId().equals(getId())) {
                plugin.getServer().getScheduler().cancelTask(entry.getKey());
                quester.getTimers().remove(entry.getKey());
            }
        }
        final Player player = quester.getPlayer();
        if (!ignoreFailAction) {
            final Stage stage = quester.getCurrentStage(this);
            if (stage != null && stage.getFailAction() != null) {
                quester.getCurrentStage(this).getFailAction().fire(quester, this);
            }
        }
        final String[] messages = {
                ChatColor.RED + BukkitLang.get(player, "questFailed").replace("<quest>", name)
        };
        quester.quitQuest(this, messages);
        if (player.isOnline()) {
            player.updateInventory();
        }
        final BukkitQuesterPostFailQuestEvent postEvent = new BukkitQuesterPostFailQuestEvent((BukkitQuester) quester, this);
        plugin.getServer().getPluginManager().callEvent(postEvent);
    }
    
    /**
     * Checks if quester is in WorldGuard region start
     * 
     * @param quester The quester to check
     * @return true if quester is in region
     */
    public boolean isInRegionStart(final Quester quester) {
        if (regionStart == null) {
            return false;
        }
        final Player player = quester.getPlayer();
        return plugin.getDependencies().getWorldGuardApi()
                .getApplicableRegionsIDs(player.getWorld(), player.getLocation()).contains(regionStart);
    }
}
