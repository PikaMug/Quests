/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.misc.FabricQuestAcceptPrompt;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.QuestProgress;
import me.pikamug.quests.quests.FabricQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.FabricStage;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricLocation;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class FabricBlockListener {

    private final FabricQuestsPlugin plugin;

    public FabricBlockListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        // Block damaged (START_DESTROY_BLOCK). The mixin only fires for ServerPlayer.
        FabricMixinEvents.registerAttackBlock(this::onBlockDamage);

        // Block used (right-click). The mixin only fires for ServerPlayer.
        FabricMixinEvents.registerUseBlock(this::onBlockUse);

        // Blocks actually broken / placed. Both mixins only fire for ServerPlayer.
        FabricMixinEvents.registerBlockBroken(this::onBlockBroken);
        FabricMixinEvents.registerBlockPlaced(this::onBlockPlaced);
    }

    private void onBlockDamage(ServerPlayer player, BlockPos pos) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final BlockState state = player.level().getBlockState(pos);
        final ObjectiveType type = ObjectiveType.DAMAGE_BLOCK;
        final Set<String> dispatchedQuestIDs = new HashSet<>();

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applyBlockDamage(quester, quest, state);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                    (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applyBlockDamage((FabricQuester) q, cq, state);
                }
                return null;
            }));
        }
    }

    private void applyBlockDamage(FabricQuester quester, Quest quest, BlockState state) {
        final FabricStage stage = (FabricStage) quester.getCurrentStage(quest);
        if (stage == null || stage.getBlocksToDamage().isEmpty()) return;
        final QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        final LinkedList<Short> durability = stage.getBlocksToDamageDurability();
        for (int i = 0; i < stage.getBlocksToDamage().size(); i++) {
            if (matchesBlock(state, stage.getBlocksToDamage().get(i), getBlockDurability(durability, i))) {
                final LinkedList<Integer> amounts = stage.getBlocksToDamageAmounts();
                final int goal = (amounts != null && !amounts.isEmpty() && amounts.size() > i)
                        ? amounts.get(i) : 1;
                final int before = progress.getBlocksDamaged().get(i);
                progress.getBlocksDamaged().set(i, before + 1);
                if (before < goal && progress.getBlocksDamaged().get(i) >= goal) {
                    quester.finishObjective(quest, ObjectiveType.DAMAGE_BLOCK, i);
                }
                quester.checkQuest(quest);
            }
        }
    }

    private void onBlockBroken(ServerPlayer player, BlockPos pos, BlockState state) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ItemStack tool = player.getMainHandItem();
        final ObjectiveType breakType = ObjectiveType.BREAK_BLOCK;
        final ObjectiveType placeType = ObjectiveType.PLACE_BLOCK;
        final Set<String> dispatchedBreakQuestIDs = new HashSet<>();
        final Set<String> dispatchedPlaceQuestIDs = new HashSet<>();

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (!quester.getCurrentQuests().containsKey(quest) || stage == null) continue;

            // BREAK_BLOCK
            if (stage.containsObjective(breakType)) {
                if (quest.getOptions().canIgnoreSilkTouch() && hasSilkTouch(tool)) {
                    quester.sendMessage(FabricLang.get(quester.getPlayer(), "optionSilkTouchFail")
                            .replace("<quest>", quest.getName()));
                } else {
                    applyBlockBreak(quester, quest, state);
                    dispatchedBreakQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, breakType,
                            (q, cq) -> {
                        if (!dispatchedBreakQuestIDs.contains(cq.getId())) {
                            applyBlockBreak((FabricQuester) q, cq, state);
                        }
                        return null;
                    }));
                }
            }

            // Replacing a block that was a placement goal counts against it
            if (quest.getOptions().canIgnoreBlockReplace() && stage.containsObjective(placeType)) {
                applyPlaceRollback(quester, quest, state);
                dispatchedPlaceQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, placeType,
                        (q, cq) -> {
                    if (!dispatchedPlaceQuestIDs.contains(cq.getId())) {
                        applyPlaceRollback((FabricQuester) q, cq, state);
                    }
                    return null;
                }));
            }
        }
    }

    private void applyBlockBreak(FabricQuester quester, Quest quest, BlockState state) {
        final FabricStage stage = (FabricStage) quester.getCurrentStage(quest);
        if (stage == null || stage.getBlocksToBreak().isEmpty()) return;
        final QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        final LinkedList<Short> durability = stage.getBlocksToBreakDurability();
        for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
            if (matchesBlock(state, stage.getBlocksToBreak().get(i), getBlockDurability(durability, i))) {
                final LinkedList<Integer> amounts = stage.getBlocksToBreakAmounts();
                final int goal = (amounts != null && !amounts.isEmpty() && amounts.size() > i)
                        ? amounts.get(i) : 1;
                final int before = progress.getBlocksBroken().get(i);
                progress.getBlocksBroken().set(i, before + 1);
                if (before < goal && progress.getBlocksBroken().get(i) >= goal) {
                    quester.finishObjective(quest, ObjectiveType.BREAK_BLOCK, i);
                }
                quester.checkQuest(quest);
            }
        }
    }

    private void applyPlaceRollback(FabricQuester quester, Quest quest, BlockState state) {
        final FabricStage stage = (FabricStage) quester.getCurrentStage(quest);
        if (stage == null || !quest.getOptions().canIgnoreBlockReplace()
                || stage.getBlocksToPlace().isEmpty()) return;
        final QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        final LinkedList<Short> durability = stage.getBlocksToPlaceDurability();
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            final int placed = progress.getBlocksPlaced().get(i);
            if (matchesBlock(state, stage.getBlocksToPlace().get(i),
                    getBlockDurability(durability, i)) && placed > 0) {
                progress.getBlocksPlaced().set(i, placed - 1);
            }
        }
    }

    private void onBlockPlaced(ServerPlayer player, BlockPos pos, BlockState state) {
        if (plugin.isLoading()) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ObjectiveType placeType = ObjectiveType.PLACE_BLOCK;
        final ObjectiveType breakType = ObjectiveType.BREAK_BLOCK;
        final Set<String> dispatchedPlaceQuestIDs = new HashSet<>();
        final Set<String> dispatchedBreakQuestIDs = new HashSet<>();

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (quester.getCurrentQuests().containsKey(quest) && stage != null) {
                // PLACE_BLOCK
                if (stage.containsObjective(placeType)) {
                    applyBlockPlace(quester, quest, state);
                }

                // Replacing a block that counted toward a break goal rolls it back
                if (quest.getOptions().canIgnoreBlockReplace() && stage.containsObjective(breakType)) {
                    applyBreakRollback(quester, quest, state);
                    dispatchedBreakQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, breakType,
                            (q, cq) -> {
                        if (!dispatchedBreakQuestIDs.contains(cq.getId())) {
                            applyBreakRollback((FabricQuester) q, cq, state);
                        }
                        return null;
                    }));
                }
            }

            dispatchedPlaceQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, placeType,
                    (q, cq) -> {
                if (!dispatchedPlaceQuestIDs.contains(cq.getId())) {
                    applyBlockPlace((FabricQuester) q, cq, state);
                }
                return null;
            }));
        }
    }

    private void applyBlockPlace(FabricQuester quester, Quest quest, BlockState state) {
        final FabricStage stage = (FabricStage) quester.getCurrentStage(quest);
        if (stage == null || stage.getBlocksToPlace().isEmpty()) return;
        final QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        final LinkedList<Short> durability = stage.getBlocksToPlaceDurability();
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            if (matchesBlock(state, stage.getBlocksToPlace().get(i), getBlockDurability(durability, i))) {
                final LinkedList<Integer> amounts = stage.getBlocksToPlaceAmounts();
                final int goal = (amounts != null && !amounts.isEmpty() && amounts.size() > i)
                        ? amounts.get(i) : 1;
                final int before = progress.getBlocksPlaced().get(i);
                progress.getBlocksPlaced().set(i, before + 1);
                if (before < goal && progress.getBlocksPlaced().get(i) >= goal) {
                    quester.finishObjective(quest, ObjectiveType.PLACE_BLOCK, i);
                }
                quester.checkQuest(quest);
            }
        }
    }

    private void applyBreakRollback(FabricQuester quester, Quest quest, BlockState state) {
        final FabricStage stage = (FabricStage) quester.getCurrentStage(quest);
        if (stage == null || !quest.getOptions().canIgnoreBlockReplace()
                || stage.getBlocksToBreak().isEmpty()) return;
        final QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        final LinkedList<Short> durability = stage.getBlocksToBreakDurability();
        for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
            final int broken = progress.getBlocksBroken().get(i);
            if (matchesBlock(state, stage.getBlocksToBreak().get(i),
                    getBlockDurability(durability, i)) && broken > 0) {
                progress.getBlocksBroken().set(i, broken - 1);
            }
        }
    }

    private void onBlockUse(ServerPlayer player, BlockPos pos, InteractionHand hand) {
        if (plugin.isLoading() || hand != InteractionHand.MAIN_HAND) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final BlockState state = player.level().getBlockState(pos);

        if (updateActionSelection(player, pos)) {
            quester.sendMessage(ChatFormatting.GOLD + FabricLang.get(quester.getPlayer(), "questSelectedLocation")
                    + " " + ChatFormatting.AQUA + player.level().dimension().identifier() + ": " + pos.getX() + ", "
                    + pos.getY() + ", " + pos.getZ());
            return;
        }

        // Block-start selection: right-click a block while editing the "start block" option
        if (plugin.isSelectingBlockStart(player.getUUID())) {
            plugin.getSelectedBlockStarts().put(player.getUUID(), new FabricLocation(player.level(), pos));
            quester.sendMessage(ChatFormatting.GOLD + FabricLang.get(quester.getPlayer(), "questSelectedLocation")
                    + " " + ChatFormatting.AQUA + player.level().dimension().identifier() + ": " + pos.getX() + ", "
                    + pos.getY() + ", " + pos.getZ());
            return;
        }

        boolean hasObjective = false;
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(ObjectiveType.USE_BLOCK)) {
                hasObjective = true;
                break;
            }
        }

        if (!hasObjective) {
            // Right-clicking a quest's start block offers the quest
            for (final Quest quest : plugin.getLoadedQuests()) {
                final FabricQuest fabricQuest = (FabricQuest) quest;
                final FabricLocation startBlock = fabricQuest.getBlockStart();
                if (startBlock == null || startBlock.getLevel() != player.level()
                        || !startBlock.getBlockPos().equals(pos)) continue;
                if (plugin.getConfigSettings().getMaxQuests() > 0
                        && quester.getCurrentQuests().size() >= plugin.getConfigSettings().getMaxQuests()) {
                    quester.sendMessage(ChatFormatting.YELLOW + FabricLang.get(quester.getPlayer(), "questMaxAllowed")
                            .replace("<number>", String.valueOf(plugin.getConfigSettings().getMaxQuests())));
                } else {
                    if (quester.getCompletedQuests().contains(fabricQuest)) {
                        if (fabricQuest.getPlanner().getCooldown() > -1
                                && quester.getRemainingCooldown(fabricQuest) > 0) {
                            quester.sendMessage(ChatFormatting.YELLOW
                                    + FabricLang.get(quester.getPlayer(), "questTooEarly")
                                    .replace("<quest>", fabricQuest.getName())
                                    .replace("<time>", FabricMiscUtil.getTime(
                                            quester.getRemainingCooldown(fabricQuest))));
                            continue;
                        } else if (fabricQuest.getPlanner().getCooldown() < 0) {
                            quester.sendMessage(ChatFormatting.YELLOW
                                    + FabricLang.get(quester.getPlayer(), "questAlreadyCompleted")
                                    .replace("<quest>", fabricQuest.getName()));
                            continue;
                        }
                    }
                    for (final Quest currentQuest : quester.getCurrentQuests().keySet()) {
                        if (currentQuest.getId().equals(fabricQuest.getId())) {
                            quester.sendMessage(ChatFormatting.RED
                                    + FabricLang.get(quester.getPlayer(), "questAlreadyOn"));
                            return;
                        }
                    }
                    quester.setQuestIdToTake(fabricQuest.getId());
                    if (!plugin.getConfigSettings().canConfirmAccept()) {
                        quester.takeQuest(fabricQuest, false);
                    } else {
                        final Quest toTake = plugin.getQuestById(quester.getQuestIdToTake());
                        final String content = ChatFormatting.GOLD + "- " + ChatFormatting.DARK_PURPLE
                                + (toTake != null ? toTake.getName() : "?") + ChatFormatting.GOLD + " -\n\n"
                                + ChatFormatting.RESET + (toTake != null ? toTake.getDescription() : "");
                        for (final String msg : content.split("<br>")) {
                            quester.sendMessage(msg);
                        }
                        new FabricQuestAcceptPrompt(player.getUUID(), plugin).start();
                    }
                }
                break;
            }
        }

        final ObjectiveType type = ObjectiveType.USE_BLOCK;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (quester.getCurrentQuests().containsKey(quest)
                    && quester.getCurrentStage(quest).containsObjective(type)) {
                applyBlockUse(quester, quest, state);
            }

            dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type,
                    (q, cq) -> {
                if (!dispatchedQuestIDs.contains(cq.getId())) {
                    applyBlockUse((FabricQuester) q, cq, state);
                }
                return null;
            }));
        }
    }

    private void applyBlockUse(FabricQuester quester, Quest quest, BlockState state) {
        final FabricStage stage = (FabricStage) quester.getCurrentStage(quest);
        if (stage == null || stage.getBlocksToUse().isEmpty()) return;
        final QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        final LinkedList<Short> durability = stage.getBlocksToUseDurability();
        for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
            if (matchesBlock(state, stage.getBlocksToUse().get(i), getBlockDurability(durability, i))) {
                final LinkedList<Integer> amounts = stage.getBlocksToUseAmounts();
                final int goal = (amounts != null && !amounts.isEmpty() && amounts.size() > i)
                        ? amounts.get(i) : 1;
                final int before = progress.getBlocksUsed().get(i);
                progress.getBlocksUsed().set(i, before + 1);
                if (before < goal && progress.getBlocksUsed().get(i) >= goal) {
                    quester.finishObjective(quest, ObjectiveType.USE_BLOCK, i);
                }
                quester.checkQuest(quest);
            }
        }
    }

    private boolean updateActionSelection(ServerPlayer player, BlockPos pos) {
        final UUID uuid = player.getUUID();
        final FabricLocation loc = new FabricLocation(player.level(), pos);
        if (plugin.getActionFactory().getSelectedMobLocations().containsKey(uuid)) {
            plugin.getActionFactory().getSelectedMobLocations().put(uuid, loc);
            return true;
        }
        if (plugin.getActionFactory().getSelectedEffectLocations().containsKey(uuid)) {
            plugin.getActionFactory().getSelectedEffectLocations().put(uuid, loc);
            return true;
        }
        if (plugin.getActionFactory().getSelectedExplosionLocations().containsKey(uuid)) {
            plugin.getActionFactory().getSelectedExplosionLocations().put(uuid, loc);
            return true;
        }
        if (plugin.getActionFactory().getSelectedLightningLocations().containsKey(uuid)) {
            plugin.getActionFactory().getSelectedLightningLocations().put(uuid, loc);
            return true;
        }
        if (plugin.getActionFactory().getSelectedTeleportLocations().containsKey(uuid)) {
            plugin.getActionFactory().getSelectedTeleportLocations().put(uuid, loc);
            return true;
        }
        return false;
    }

    private boolean matchesBlock(BlockState state, Object goal, short durability) {
        if (state == null || goal == null) return false;
        final Block block = resolveGoalBlock(goal);
        return block != null && state.getBlock() == block && getBlockDurability(state) == durability;
    }

    private Block resolveGoalBlock(Object goal) {
        final String name = goal.toString();
        Block block = blockById(name);
        if (block != null) return block;
        if (name.startsWith("{")) {
            try {
                final JsonObject json = JsonParser.parseString(name).getAsJsonObject();
                if (json.has("id") && json.get("id").isJsonPrimitive()) {
                    block = blockById(json.get("id").getAsString());
                    if (block != null) return block;
                }
            } catch (final Exception ignored) {
            }
        }
        final ItemStack goalStack = FabricItemUtil.readItemStack(name);
        if (goalStack.isEmpty()) return null;
        final Block itemBlock = Block.byItem(goalStack.getItem());
        return itemBlock == Blocks.AIR ? null : itemBlock;
    }

    private Block blockById(String name) {
        final Identifier id = Identifier.tryParse(name.toLowerCase(Locale.ROOT));
        if (id == null) return null;
        final Block block = BuiltInRegistries.BLOCK.getValue(id);
        return block == Blocks.AIR ? null : block;
    }

    private short getBlockDurability(BlockState state) {
        for (final Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && property.getName().equals("age")) {
                return (short) (int) state.getValue(age);
            }
        }
        for (final Property<?> property : state.getProperties()) {
            if (property instanceof BooleanProperty powered && property.getName().equals("powered")) {
                return (short) (state.getValue(powered) ? 1 : 0);
            }
        }
        return 0;
    }

    private short getBlockDurability(LinkedList<Short> durability, int index) {
        if (durability == null || index >= durability.size() || durability.get(index) == null) {
            return 0;
        }
        final short value = durability.get(index);
        return value < 0 ? 0 : value;
    }

    private boolean hasSilkTouch(ItemStack tool) {
        if (tool == null || tool.isEmpty()) return false;
        for (final Object2IntMap.Entry<Holder<Enchantment>> entry : tool.getEnchantments().entrySet()) {
            if (entry.getKey().is(Enchantments.SILK_TOUCH)) return true;
        }
        return false;
    }
}
