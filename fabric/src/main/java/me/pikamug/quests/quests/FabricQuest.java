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
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.components.*;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class FabricQuest implements Quest {

    private String id;
    private String name;
    private String description;
    private String finished;
    private String regionStart;
    private net.minecraft.core.BlockPos blockStart;
    private LinkedList<Stage> stages = new LinkedList<>();
    private UUID npcStart;
    private String npcStartName;
    private Action initialAction;
    private Requirements requirements = new FabricRequirements();
    private Planner planner = new FabricPlanner();
    private Rewards rewards = new FabricRewards();
    private Options options = new FabricOptions();

    @Override public String getId() { return id; }
    @Override public void setId(String id) { this.id = id; }
    @Override public String getName() { return name; }
    @Override public void setName(String v) { this.name = v; }
    @Override public String getDescription() { return description; }
    @Override public void setDescription(String v) { this.description = v; }
    @Override public String getFinished() { return finished; }
    @Override public void setFinished(String v) { this.finished = v; }
    @Override public String getRegionStart() { return regionStart; }
    @Override public void setRegionStart(String v) { this.regionStart = v; }
    public net.minecraft.core.BlockPos getBlockStart() { return blockStart; }
    public void setBlockStart(net.minecraft.core.BlockPos v) { this.blockStart = v; }
    @Override public Stage getStage(int index) {
        if (index < 0 || index >= stages.size()) return null;
        return stages.get(index);
    }
    @Override public LinkedList<Stage> getStages() { return stages; }
    @Override public UUID getNpcStart() { return npcStart; }
    @Override public void setNpcStart(UUID v) { this.npcStart = v; }
    @Override public String getNpcStartName() { return npcStartName; }
    public void setNpcStartName(String v) { this.npcStartName = v; }
    @Override public Action getInitialAction() { return initialAction; }
    @Override public void setInitialAction(Action v) { this.initialAction = v; }
    @Override public Requirements getRequirements() { return requirements; }
    @Override public void setRequirements(Requirements v) { this.requirements = v; }
    @Override public Planner getPlanner() { return planner; }
    @Override public void setPlanner(Planner v) { this.planner = v; }
    @Override public Rewards getRewards() { return rewards; }
    @Override public void setRewards(Rewards v) { this.rewards = v; }
    @Override public Options getOptions() { return options; }
    @Override public void setOptions(Options v) { this.options = v; }

    public void setStages(LinkedList<Stage> stages) { this.stages = stages; }

    @Override
    public void nextStage(Quester quester, boolean allowSharedProgress) {
        if (quester == null) return;
        final int currentStage = quester.getCurrentQuests().getOrDefault(this, 0);
        final int nextStage = currentStage + 1;
        if (nextStage < stages.size()) {
            quester.getCurrentQuests().put(this, nextStage);
            final Stage stage = stages.get(nextStage);
            if (stage.getStartAction() != null) {
                stage.getStartAction().fire(quester, this);
            }
        } else {
            completeQuest(quester);
        }
    }

    @Override
    public void setStage(Quester quester, int stage) {
        if (quester == null || stage < 0 || stage >= stages.size()) return;
        quester.getCurrentQuests().put(this, stage);
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
            final var server = FabricQuestsPlugin.getInstance().getServer();
            final var player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());
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
            final var server = FabricQuestsPlugin.getInstance().getServer();
            final var player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());
            for (int i = 0; i < requirements.getItems().size(); i++) {
                final Object itemObj = requirements.getItems().get(i);
                if (!(itemObj instanceof net.minecraft.world.item.ItemStack item)) return false;
                if (player == null) return false;
                final int have = me.pikamug.quests.util.FabricInventoryUtil.countItem(player, item);
                if (have < item.getCount()) return false;
            }
        }

        // Check custom requirements (from module jars)
        if (requirements.getCustomRequirements() != null && !requirements.getCustomRequirements().isEmpty()) {
            final var customRequirements = FabricQuestsPlugin.getInstance().getCustomRequirements();
            for (final var entry : requirements.getCustomRequirements().entrySet()) {
                boolean found = false;
                for (final var custom : customRequirements) {
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
            final var server = FabricQuestsPlugin.getInstance().getServer();
            final var player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());

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
                final var dependencies = FabricQuestsPlugin.getInstance().getDependencies();
                for (final String permission : rewards.getPermissions()) {
                    if (permission != null && !permission.isEmpty()) {
                        dependencies.grantPermission(quester.getUUID(), permission);
                    }
                }
            }

            if (rewards.getItems() != null && !rewards.getItems().isEmpty() && player != null) {
                for (final Object itemObj : rewards.getItems()) {
                    if (itemObj instanceof net.minecraft.world.item.ItemStack item) {
                        me.pikamug.quests.util.FabricInventoryUtil.addItem(player, item);
                    }
                }
            }

            if (rewards.getCustomRewards() != null && !rewards.getCustomRewards().isEmpty()) {
                final var customRewards = FabricQuestsPlugin.getInstance().getCustomRewards();
                for (final var entry : rewards.getCustomRewards().entrySet()) {
                    boolean found = false;
                    for (final var custom : customRewards) {
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
        final var server = plugin.getServer();
        final ServerPlayer player = server == null ? null : server.getPlayerList().getPlayer(quester.getUUID());
        if (player != null) {
            if (plugin.getConfigSettings().canShowQuestTitles()) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§e" + name)));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(
                        "§6" + FabricLang.get(player, "quest") + " " + FabricLang.get(player, "complete"))));
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            }
            quester.sendMessage(FabricLang.get(player, "questCompleteTitle").replace("<quest>", name));
        }
        quester.sendMessage("§a" + FabricLang.get(player, "questRewardsTitle"));
        if (rewards == null) {
            quester.sendMessage("§7- " + FabricLang.get(player, "none"));
            return;
        }
        final boolean issuedReward = rewards.getQuestPoints() > 0 || rewards.getExp() > 0
                || (rewards.getItems() != null && !rewards.getItems().isEmpty())
                || (rewards.getCommands() != null && !rewards.getCommands().isEmpty())
                || (rewards.getPermissions() != null && !rewards.getPermissions().isEmpty())
                || (rewards.getCustomRewards() != null && !rewards.getCustomRewards().isEmpty());
        if (!issuedReward) {
            quester.sendMessage("§7- " + FabricLang.get(player, "none"));
            return;
        }
        if (rewards.getDetailsOverride() != null && !rewards.getDetailsOverride().isEmpty()) {
            for (final String s : rewards.getDetailsOverride()) {
                quester.sendMessage("- §2" + s);
            }
            return;
        }
        if (rewards.getExp() > 0) {
            quester.sendMessage("- §2" + rewards.getExp() + " " + FabricLang.get(player, "experience"));
        }
        if (rewards.getQuestPoints() > 0) {
            quester.sendMessage("- §2" + rewards.getQuestPoints() + " " + FabricLang.get(player, "questPoints"));
        }
        if (rewards.getItems() != null) {
            for (final Object itemObj : rewards.getItems()) {
                if (itemObj instanceof net.minecraft.world.item.ItemStack item) {
                    quester.sendMessage("- §3" + me.pikamug.quests.util.FabricItemUtil.getName(item)
                            + " §7x " + item.getCount());
                }
            }
        }
        if (rewards.getCommands() != null && !rewards.getCommands().isEmpty()) {
            int index = 0;
            for (final String s : rewards.getCommands()) {
                final String override = rewards.getCommandsOverrideDisplay() != null
                        && rewards.getCommandsOverrideDisplay().size() > index
                        ? rewards.getCommandsOverrideDisplay().get(index) : null;
                quester.sendMessage("- §2" + (override != null && !override.trim().isEmpty() ? override : s));
                index++;
            }
        }
        if (rewards.getPermissions() != null && !rewards.getPermissions().isEmpty()) {
            int index = 0;
            for (final String s : rewards.getPermissions()) {
                if (rewards.getPermissionWorlds() != null && rewards.getPermissionWorlds().size() > index) {
                    quester.sendMessage("- §2" + s + " (" + rewards.getPermissionWorlds().get(index) + ")");
                } else {
                    quester.sendMessage("- §2" + s);
                }
                index++;
            }
        }
        final Map<String, Map<String, Object>> customRewardMap = rewards.getCustomRewards();
        if (customRewardMap != null && !customRewardMap.isEmpty()) {
            final var customRewards = FabricQuestsPlugin.getInstance().getCustomRewards();
            for (final Map.Entry<String, Map<String, Object>> entry : customRewardMap.entrySet()) {
                for (final var custom : customRewards) {
                    if (custom.getModuleName().equalsIgnoreCase(entry.getKey())) {
                        String display = custom.getDisplay();
                        if (display != null) {
                            for (final Map.Entry<String, Object> dataEntry : entry.getValue().entrySet()) {
                                display = display.replace("%" + dataEntry.getKey() + "%",
                                        dataEntry.getValue().toString());
                            }
                            quester.sendMessage("- §6" + display);
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
            final Stage stage = stages.isEmpty() ? null : stages.get(Math.min(stageNum, stages.size() - 1));
            if (stage != null && stage.getFailAction() != null) {
                stage.getFailAction().fire(quester, this);
            }
        }
        final FabricQuestsPlugin failPlugin = FabricQuestsPlugin.getInstance();
        final ServerPlayer failPlayer = failPlugin == null || failPlugin.getServer() == null ? null
                : failPlugin.getServer().getPlayerList().getPlayer(quester.getUUID());
        final String[] messages = {"§c" + FabricLang.get(failPlayer, "questFailed").replace("<quest>", name != null ? name : id)};
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

    @Override
    public int compareTo(Quest other) {
        if (other == null) return 1;
        if (this.name != null && other.getName() != null) {
            return this.name.compareTo(other.getName());
        }
        return 0;
    }
}
