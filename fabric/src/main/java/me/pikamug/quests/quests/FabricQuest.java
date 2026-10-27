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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.actions.FabricAction;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.FabricOptions;
import me.pikamug.quests.quests.components.FabricPlanner;
import me.pikamug.quests.quests.components.FabricRequirements;
import me.pikamug.quests.quests.components.FabricRewards;
import me.pikamug.quests.quests.components.Options;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Rewards;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricLocation;
import me.pikamug.quests.util.FabricOfflinePlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class FabricQuest implements Quest {

    protected FabricQuestsPlugin plugin;
    private String id;
    private String name;
    private String description;
    private String finished;
    private String regionStart;
    private ItemStack guiDisplay;
    private LinkedList<Stage> orderedStages = new LinkedList<>();
    private UUID npcStart;
    private FabricLocation blockStart;
    private Action initialAction;
    private Requirements requirements = new FabricRequirements();
    private Planner planner = new FabricPlanner();
    private Rewards rewards = new FabricRewards();
    private Options options = new FabricOptions();

    public FabricQuest(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int compareTo(final Quest quest) {
        return id.compareTo(quest.getId());
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

    public FabricLocation getBlockStart() {
        return blockStart;
    }

    public void setBlockStart(final FabricLocation blockStart) {
        this.blockStart = blockStart;
    }

    @Override
    public Action getInitialAction() {
        return initialAction;
    }

    @Override
    public void setInitialAction(final Action initialAction) {
        this.initialAction = (FabricAction) initialAction;
    }

    @Override
    public Requirements getRequirements() {
        return requirements;
    }

    @Override
    public void setRequirements(final Requirements requirements) {
        this.requirements = (FabricRequirements) requirements;
    }

    @Override
    public Planner getPlanner() {
        return planner;
    }

    @Override
    public void setPlanner(final Planner planner) {
        this.planner = (FabricPlanner) planner;
    }

    @Override
    public Rewards getRewards() {
        return rewards;
    }

    @Override
    public void setRewards(final Rewards rewards) {
        this.rewards = (FabricRewards) rewards;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void setOptions(final Options options) {
        this.options = (FabricOptions) options;
    }

    /**
     * Force player to proceed to the next ordered stage
     *
     * @param quester Quester to force progression
     * @param allowSharedProgress Whether to distribute progress to fellow questers
     */
    public void nextStage(final Quester quester, final boolean allowSharedProgress) {
        final FabricQuester q = (FabricQuester) quester;
        final Stage currentStage = q.getCurrentStage(this);
        if (currentStage == null) {
            FabricQuestsPlugin.LOGGER.severe("Current stage was null for quester " + q.getPlayer().getStringUUID());
            return;
        }
        final String stageCompleteMessage = currentStage.getCompleteMessage();
        if (stageCompleteMessage != null) {
            if (q.getOfflinePlayer().isOnline()) {
                for (final String s : FabricConfigUtil.parseStringWithPossibleLineBreaks(stageCompleteMessage,
                        this, q.getPlayer())) {
                    q.sendMessage(s);
                }
            }
        }
        if (currentStage.getDelay() < 0) {
            doNextStage(q, allowSharedProgress);
        } else {
            // Here we avoid FabricStageTimer as the stage objectives are incomplete
            FabricScheduler.runLater(() -> {
                doNextStage(q, allowSharedProgress);
            }, (long) (currentStage.getDelay() * 0.02));
        }
        q.updateJournal();
    }

    private void doNextStage(final Quester quester, final boolean allowSharedProgress) {
        final Stage currentStage = quester.getCurrentStage(this);
        if (currentStage == null) {
            return;
        }
        if (currentStage.getFinishAction() != null) {
            currentStage.getFinishAction().fire(quester, this);
        }
        if (quester.getCurrentQuests().get(this) == (orderedStages.size() - 1)) {
            completeQuest(quester);
        } else {
            setStage(quester, quester.getCurrentQuests().get(this) + 1);
        }
        if (quester.getQuestProgressOrDefault(this) != null) {
            quester.getQuestProgressOrDefault(this).setDelayStartTime(0);
            quester.getQuestProgressOrDefault(this).setDelayTimeLeft(-1);
        }

        // Multiplayer
        if (allowSharedProgress && options.getShareProgressLevel() == 3) {
            final Collection<Quester> mq = quester.getMultiplayerQuesters(this);
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
        final FabricQuester q = (FabricQuester) quester;
        final FabricOfflinePlayer player = q.getOfflinePlayer();
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
        /*if (player.isOnline()) {
            final BukkitQuesterPreChangeStageEvent preEvent
                    = new BukkitQuesterPreChangeStageEvent((BukkitQuester) quester, this, currentStage, nextStage);
            plugin.getServer().getPluginManager().callEvent(preEvent);
            if (preEvent.isCancelled()) {
                return;
            }
        }*/
        quester.hardQuit(this);
        quester.hardStagePut(this, stage);
        quester.addEmptiesFor(this, stage);
        if (nextStage.getStartAction() != null) {
            nextStage.getStartAction().fire(quester, this);
        }
        updateCompass(quester, nextStage);
        if (player.isOnline()) {
            final ServerPlayer p = q.getPlayer();
            final String title = FabricLang.get(p, "objectives").replace("<quest>", name);
            FabricLang.send(p, ChatFormatting.GOLD + title);
            quester.showCurrentObjectives(this, quester, false);
            if (quester.getCurrentStage(this) == null) {
                quester.sendMessage(ChatFormatting.RED + "itemCreateCriticalError");
                FabricQuestsPlugin.LOGGER.severe("Could not set stage for quest ID " + getId()
                        + " because the current stage for player " + quester.getLastKnownName() + " was null");
                return;
            }
            final String stageStartMessage = quester.getCurrentStage(this).getStartMessage();
            if (stageStartMessage != null) {
                for (final String s : FabricConfigUtil.parseStringWithPossibleLineBreaks(stageStartMessage, this, p)) {
                    q.sendMessage(s);
                }
            }
            quester.showCurrentConditions(this, quester);
        }
        quester.updateJournal();
        quester.saveData();
        /*if (player.isOnline()) {
            final BukkitQuesterPostChangeStageEvent postEvent
                    = new BukkitQuesterPostChangeStageEvent((BukkitQuester) quester, this, currentStage, nextStage);
            plugin.getServer().getPluginManager().callEvent(postEvent);
        }*/
    }

    @Override
    public boolean updateCompass(Quester quester, Stage stage) {
        if (quester == null || stage == null) return false;
        quester.setCompassTarget(this);
        return true;
    }

    @Override
    public boolean testRequirements(Quester quester) {
        if (quester == null || requirements == null) return false;

        // Check quest points
        if (requirements.getQuestPoints() > 0 && quester.getQuestPoints() < requirements.getQuestPoints()) {
            return false;
        }

        // Check experience (online players only)
        if (requirements.getExp() > 0) {
            final net.minecraft.server.MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
            final net.minecraft.server.level.ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());
            if (player == null) return false;
            if (player.totalExperience < requirements.getExp()) {
                return false;
            }
        }

        // Check needed quests
        if (requirements.getNeededQuestIds() != null) {
            for (final String neededId : requirements.getNeededQuestIds()) {
                boolean found = false;
                for (final Quest completed : quester.getCompletedQuests()) {
                    if (completed.getId() != null && completed.getId().equalsIgnoreCase(neededId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) return false;
            }
        }

        // Check blocked quests
        if (requirements.getBlockQuestIds() != null) {
            for (final String blockedId : requirements.getBlockQuestIds()) {
                for (final Quest completed : quester.getCompletedQuests()) {
                    if (completed.getId() != null && completed.getId().equalsIgnoreCase(blockedId)) {
                        return false;
                    }
                }
            }
        }

        // Check permissions (LuckPerms)
        if (requirements.getPermissions() != null && !requirements.getPermissions().isEmpty()) {
            for (final String perm : requirements.getPermissions()) {
                if (!FabricQuestsPlugin.getInstance().getDependencies().hasPermission(quester.getUUID(), perm)) {
                    return false;
                }
            }
        }

        // Check item requirements (player must have enough of each item)
        if (requirements.getItems() != null && !requirements.getItems().isEmpty()) {
            final net.minecraft.server.MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
            final net.minecraft.server.level.ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());
            for (int i = 0; i < requirements.getItems().size(); i++) {
                final Object itemObj = requirements.getItems().get(i);
                if (!(itemObj instanceof net.minecraft.world.item.ItemStack)) return false;
                net.minecraft.world.item.ItemStack item = (net.minecraft.world.item.ItemStack) itemObj;
                if (player == null) return false;
                final int have = me.pikamug.quests.util.FabricInventoryUtil.countItem(player, item);
                if (have < item.getCount()) return false;
            }
        }

        // Check custom requirements (from module jars)
        if (requirements.getCustomRequirements() != null && !requirements.getCustomRequirements().isEmpty()) {
            final java.util.Collection<me.pikamug.quests.module.CustomRequirement> customRequirements = FabricQuestsPlugin.getInstance().getCustomRequirements();
            for (final java.util.Map.Entry<String, java.util.Map<String, Object>> entry : requirements.getCustomRequirements().entrySet()) {
                boolean found = false;
                for (final me.pikamug.quests.module.CustomRequirement custom : customRequirements) {
                    if (custom.getModuleName().equalsIgnoreCase(entry.getKey())) {
                        found = true;
                        if (!custom.testRequirement(quester.getUUID(), entry.getValue())) {
                            return false;
                        }
                        break;
                    }
                }
                if (!found) {
                    FabricQuestsPlugin.LOGGER.warn("Custom requirement module '{}' not loaded for quest '{}'",
                            entry.getKey(), name);
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void completeQuest(Quester quester) {
        completeQuest(quester, true);
    }

    @Override
    public void completeQuest(Quester quester, boolean allowMultiplayer) {
        if (quester == null) return;
        quester.getCurrentQuests().remove(this);
        quester.getCompletedQuests().add(this);
        final int count = quester.getAmountsCompleted().getOrDefault(this, 0);
        quester.getAmountsCompleted().put(this, count + 1);
        quester.getCompletedTimes().put(this, System.currentTimeMillis());
        // Grant rewards
        if (rewards != null) {
            quester.setQuestPoints(quester.getQuestPoints() + rewards.getQuestPoints());
            final net.minecraft.server.MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
            final net.minecraft.server.level.ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());

            if (rewards.getExp() > 0 && player != null) {
                player.giveExperiencePoints(rewards.getExp());
            }

            if (rewards.getCommands() != null && !rewards.getCommands().isEmpty() && server != null) {
                final String playerName = player != null ? player.getName().getString()
                        : quester.getUUID().toString();
                for (final String command : rewards.getCommands()) {
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                            command.replace("<player>", playerName));
                }
            }

            if (rewards.getPermissions() != null && !rewards.getPermissions().isEmpty()) {
                final me.pikamug.quests.dependencies.FabricDependencies dependencies = FabricQuestsPlugin.getInstance().getDependencies();
                for (final String permission : rewards.getPermissions()) {
                    if (permission != null && !permission.isEmpty()) {
                        dependencies.grantPermission(quester.getUUID(), permission);
                    }
                }
            }

            if (rewards.getItems() != null && !rewards.getItems().isEmpty() && player != null) {
                for (final Object itemObj : rewards.getItems()) {
                    if (itemObj instanceof net.minecraft.world.item.ItemStack) {
                        net.minecraft.world.item.ItemStack item = (net.minecraft.world.item.ItemStack) itemObj;
                        me.pikamug.quests.util.FabricInventoryUtil.addItem(player, item);
                    }
                }
            }

        if (rewards.getCustomRewards() != null && !rewards.getCustomRewards().isEmpty()) {
            final java.util.Collection<me.pikamug.quests.module.CustomReward> customRewards = FabricQuestsPlugin.getInstance().getCustomRewards();
            for (final java.util.Map.Entry<String, java.util.Map<String, Object>> entry : rewards.getCustomRewards().entrySet()) {
                boolean found = false;
                for (final me.pikamug.quests.module.CustomReward custom : customRewards) {
                        if (custom.getModuleName().equalsIgnoreCase(entry.getKey())) {
                            found = true;
                            try {
                                custom.giveReward(quester.getUUID(), entry.getValue());
                            } catch (final Exception e) {
                                FabricQuestsPlugin.LOGGER.warn("Custom reward '{}' failed for player {}",
                                        entry.getKey(), quester.getUUID(), e);
                            }
                            break;
                        }
                    }
                    if (!found) {
                        FabricQuestsPlugin.LOGGER.warn("Custom reward module '{}' not loaded for quest '{}'",
                                entry.getKey(), name);
                    }
                }
            }
        }
        quester.stopStageTimer(this);
        sendCompletionFeedback(quester);
        quester.saveData();
        FabricQuestsPlugin.LOGGER.info("Quest '{}' completed by {}", name, quester.getUUID());
    }

    private void sendCompletionFeedback(Quester quester) {
        if (quester == null) return;
        final FabricQuestsPlugin plugin = FabricQuestsPlugin.getInstance();
        final net.minecraft.server.MinecraftServer server = plugin.getServer();
        final ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());
        if (player != null) {
            if (plugin.getConfigSettings().canShowQuestTitles()) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§e" + name)));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(
                        "ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§6" + FabricLang.get(player, "quest") + " " + FabricLang.get(player, "complete"))));
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            }
            quester.sendMessage(FabricLang.get(player, "questCompleteTitle").replace("<quest>", name));
        }
        quester.sendMessage("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§a" + FabricLang.get(player, "questRewardsTitle"));
        if (rewards == null) {
            quester.sendMessage("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§7- " + FabricLang.get(player, "none"));
            return;
        }
        final boolean issuedReward = rewards.getQuestPoints() > 0 || rewards.getExp() > 0
                || (rewards.getItems() != null && !rewards.getItems().isEmpty())
                || (rewards.getCommands() != null && !rewards.getCommands().isEmpty())
                || (rewards.getPermissions() != null && !rewards.getPermissions().isEmpty())
                || (rewards.getCustomRewards() != null && !rewards.getCustomRewards().isEmpty());
        if (!issuedReward) {
            quester.sendMessage("ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§7- " + FabricLang.get(player, "none"));
            return;
        }
        if (rewards.getDetailsOverride() != null && !rewards.getDetailsOverride().isEmpty()) {
            for (final String s : rewards.getDetailsOverride()) {
                quester.sendMessage("- ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§2" + s);
            }
            return;
        }
        if (rewards.getExp() > 0) {
            quester.sendMessage("- ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§2" + rewards.getExp() + " " + FabricLang.get(player, "experience"));
        }
        if (rewards.getQuestPoints() > 0) {
            quester.sendMessage("- ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§2" + rewards.getQuestPoints() + " " + FabricLang.get(player, "questPoints"));
        }
        if (rewards.getItems() != null) {
            for (final Object itemObj : rewards.getItems()) {
                if (itemObj instanceof net.minecraft.world.item.ItemStack) {
                    net.minecraft.world.item.ItemStack item = (net.minecraft.world.item.ItemStack) itemObj;
                    String name = me.pikamug.quests.util.FabricItemUtil.getName(item);
                    if (name == null || name.trim().isEmpty() || name.equals("Air")) {
                    }
                    quester.sendMessage("- " + name + " x " + item.getCount());
                }
            }
        }
        if (rewards.getCommands() != null && !rewards.getCommands().isEmpty()) {
            int index = 0;
            for (final String s : rewards.getCommands()) {
                final String override = rewards.getCommandsOverrideDisplay() != null
                        && rewards.getCommandsOverrideDisplay().size() > index
                        ? rewards.getCommandsOverrideDisplay().get(index) : null;
                quester.sendMessage("- ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§2" + (override != null && !override.trim().isEmpty() ? override : s));
                index++;
            }
        }
        if (rewards.getPermissions() != null && !rewards.getPermissions().isEmpty()) {
            int index = 0;
            for (final String s : rewards.getPermissions()) {
                if (rewards.getPermissionWorlds() != null && rewards.getPermissionWorlds().size() > index) {
                    quester.sendMessage("- ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§2" + s + " (" + rewards.getPermissionWorlds().get(index) + ")");
                } else {
                    quester.sendMessage("- ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§2" + s);
                }
                index++;
            }
        }
        final Map<String, Map<String, Object>> customRewardMap = rewards.getCustomRewards();
        if (customRewardMap != null && !customRewardMap.isEmpty()) {
            final java.util.Collection<me.pikamug.quests.module.CustomReward> customRewards = FabricQuestsPlugin.getInstance().getCustomRewards();
            for (final Map.Entry<String, Map<String, Object>> entry : customRewardMap.entrySet()) {
                for (final me.pikamug.quests.module.CustomReward custom : customRewards) {
                    if (custom.getModuleName().equalsIgnoreCase(entry.getKey())) {
                        String display = custom.getDisplay();
                        if (display != null) {
                            for (final Map.Entry<String, Object> dataEntry : entry.getValue().entrySet()) {
                                display = display.replace("%" + dataEntry.getKey() + "%",
                                        dataEntry.getValue().toString());
                            }
                            quester.sendMessage("- ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§6" + display);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void failQuest(Quester quester) {
        failQuest(quester, false);
    }

    @Override
    public void failQuest(Quester quester, boolean ignoreFailAction) {
        if (quester == null) return;
        if (!ignoreFailAction) {
            final int stageNum = quester.getCurrentQuests().getOrDefault(this, 0);
            final Stage stage = orderedStages.isEmpty() ? null : orderedStages.get(Math.min(stageNum, orderedStages.size() - 1));
            if (stage != null && stage.getFailAction() != null) {
                stage.getFailAction().fire(quester, this);
            }
        }
        final FabricQuestsPlugin failPlugin = FabricQuestsPlugin.getInstance();
        final ServerPlayer failPlayer = failPlugin == null || failPlugin.getServer() == null ? null
                : failPlugin.getServer().getPlayerList().getPlayer(quester.getUUID());
        final String[] messages = {"ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â§c" + FabricLang.get(failPlayer, "questFailed").replace("<quest>", name != null ? name : id)};
        quester.quitQuest(this, messages);
        FabricQuestsPlugin.LOGGER.info("Quest '{}' failed by {}", name, quester.getUUID());
    }

    @Override
    public boolean isInRegionStart(Quester quester) {
        if (quester == null || regionStart == null) {
            return false;
        }
        final FabricQuestsPlugin plugin = (FabricQuestsPlugin) quester.getPlugin();
        if (plugin.getServer() == null) {
            return false;
        }
        final ServerPlayer player = plugin.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (player == null) {
            return false;
        }
        return plugin.getDependencies().getRegionsAt(player).contains(regionStart);
    }
}
