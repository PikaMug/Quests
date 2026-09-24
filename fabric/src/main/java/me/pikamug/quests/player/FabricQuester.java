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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.Quests;
import me.pikamug.quests.conditions.Condition;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.FabricCustomObjective;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.FabricQuest;
import me.pikamug.quests.quests.components.FabricObjective;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.FabricActionTimer;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.FabricInventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FabricQuester implements Quester {

    private final FabricQuestsPlugin plugin;
    private final UUID uuid;
    private String questIdToTake;
    private String questIdToQuit;
    private String lastKnownName;
    private int questPoints;
    private Quest compassTarget;
    private final ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<>();
    private final Collection<Quest> completedQuests = Collections.synchronizedCollection(new ArrayList<>());
    private final ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Quest, QuestProgress> progressData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<FabricActionTimer, Quest> actionTimers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Quest, FabricScheduler.ScheduledTask> stageTimers = new ConcurrentHashMap<>();
    private boolean hasData = false;

    public FabricQuester(FabricQuestsPlugin plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }
@Override
    public Quests getPlugin() { return plugin; }

    public void setHasData(boolean v) { this.hasData = v; }

    public ConcurrentHashMap<Quest, QuestProgress> getProgressData() { return progressData; }
    @Override public UUID getUUID() { return uuid; }
    @Override public String getQuestIdToTake() { return questIdToTake; }
    @Override public void setQuestIdToTake(String v) { this.questIdToTake = v; }
    @Override public String getQuestIdToQuit() { return questIdToQuit; }
    @Override public void setQuestIdToQuit(String v) { this.questIdToQuit = v; }
    @Override public String getLastKnownName() { return lastKnownName; }
    @Override public void setLastKnownName(String v) { this.lastKnownName = v; }
    @Override public int getQuestPoints() { return questPoints; }
    @Override public void setQuestPoints(int v) { this.questPoints = v; }
    @Override public Quest getCompassTarget() { return compassTarget; }
    @Override public void setCompassTarget(Quest v) { this.compassTarget = v; }
    @Override public ConcurrentHashMap<Quest, Integer> getCurrentQuests() { return currentQuests; }
    @Override public void setCurrentQuests(ConcurrentHashMap<Quest, Integer> v) { currentQuests.clear(); currentQuests.putAll(v); }
    @Override public Collection<Quest> getCompletedQuests() { return completedQuests; }
    @Override public void setCompletedQuests(Collection<Quest> v) { completedQuests.clear(); completedQuests.addAll(v); }
    @Override public ConcurrentHashMap<Quest, Long> getCompletedTimes() { return completedTimes; }
    @Override public void setCompletedTimes(ConcurrentHashMap<Quest, Long> v) { completedTimes.clear(); completedTimes.putAll(v); }
    @Override public ConcurrentHashMap<Quest, Integer> getAmountsCompleted() { return amountsCompleted; }
    @Override public void setAmountsCompleted(ConcurrentHashMap<Quest, Integer> v) { amountsCompleted.clear(); amountsCompleted.putAll(v); }

    public ConcurrentHashMap<FabricActionTimer, Quest> getActionTimers() { return actionTimers; }

    @Override
    public void sendMessage(String message) {
        if (message == null || message.isEmpty()) return;
        final ServerPlayer player = getServerPlayer();
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    @Override
    public Stage getCurrentStage(Quest quest) {
        if (quest == null) return null;
        final int stageIndex = currentQuests.getOrDefault(quest, 0);
        return quest.getStage(stageIndex);
    }

    @Override
    public QuestProgress getQuestProgressOrDefault(Quest quest) {
        if (quest == null) return null;
        return progressData.computeIfAbsent(quest, k -> new FabricQuestProgress());
    }

    public QuestProgress getQuestProgress(Quest quest) {
        return progressData.get(quest);
    }

    public void setQuestProgress(Quest quest, QuestProgress progress) {
        if (quest != null && progress != null) {
            progressData.put(quest, progress);
        }
    }

    @Override
    public boolean hasJournal() {
        final ServerPlayer player = getServerPlayer();
        if (player == null) return false;
        for (final net.minecraft.world.item.ItemStack is : player.getInventory().getNonEquipmentItems()) {
            if (me.pikamug.quests.util.FabricItemUtil.isJournal(is)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getJournalIndex() {
        final ServerPlayer player = getServerPlayer();
        if (player == null) return -1;
        final List<net.minecraft.world.item.ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            if (me.pikamug.quests.util.FabricItemUtil.isJournal(items.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void updateJournal() {
        final ServerPlayer player = getServerPlayer();
        if (player == null) return;
        final int index = getJournalIndex();
        if (index != -1) {
            final me.pikamug.quests.item.FabricQuestJournal journal
                    = new me.pikamug.quests.item.FabricQuestJournal(plugin, this);
            player.getInventory().setItem(index, journal.toItemStack());
        }
    }

    @Override
    public boolean offerQuest(Quest quest, boolean giveReason) {
        if (quest == null) return false;
        if (currentQuests.containsKey(quest)) {
            if (giveReason) sendMessage(FabricLang.get(getServerPlayer(), "questAlreadyOn"));
            return false;
        }
        if (plugin.getConfigSettings().getMaxQuests() > 0
                && currentQuests.size() >= plugin.getConfigSettings().getMaxQuests()
                && !quest.getOptions().canOverrideMaxQuests()) {
            if (giveReason) sendMessage(FabricLang.get(getServerPlayer(), "questMaxAllowed")
                    .replace("<number>", String.valueOf(plugin.getConfigSettings().getMaxQuests())));
            return false;
        }
        if (plugin.getConfigSettings().canConfirmAccept()) {
            questIdToTake = quest.getId();
            sendMessage(FabricLang.get(getServerPlayer(), "confirmQuestTake").replace("<quest>", quest.getName()));
        } else {
            takeQuest(quest, false);
        }
        return true;
    }

    @Override
    public boolean canAcceptOffer(Quest quest, boolean giveReason) {
        if (quest == null) return false;
        if (!plugin.getConfigSettings().canAllowCommandsForNpcQuests()
                && quest.getNpcStart() != null
                && quest instanceof FabricQuest fabricQuestStart && fabricQuestStart.getNpcStart() != null) {
            final UUID npcUuid = fabricQuestStart.getNpcStart();
            final ServerPlayer player = getServerPlayer();
            final ServerPlayer npc = plugin.getServer() == null ? null
                    : plugin.getServer().getPlayerList().getPlayer(npcUuid);
            if (player != null && npc != null && player.level().dimension()
                    .equals(npc.level().dimension())) {
                if (player.blockPosition().distSqr(npc.blockPosition()) > 36) {
                    if (giveReason) {
                        final String msg = FabricLang.get(getServerPlayer(), "mustSpeakTo").replace("<npc>",
                                plugin.getDependencies().getNpcName(npcUuid));
                        sendMessage("§e" + msg);
                    }
                    return false;
                }
            }
        }
        if (currentQuests.containsKey(quest)) {
            if (giveReason) sendMessage(FabricLang.get(getServerPlayer(), "questAlreadyOn"));
            return false;
        }
        if (completedQuests.contains(quest) && !quest.getPlanner().hasRepeat()) {
            if (giveReason) sendMessage(FabricLang.get(getServerPlayer(), "questAlreadyCompleted"));
            return false;
        }
        if (getRemainingCooldown(quest) > 0 && completedQuests.contains(quest)
                && !quest.getPlanner().getOverride()) {
            if (giveReason) {
                final String msg = FabricLang.get(getServerPlayer(), "questTooEarly").replace("<quest>",
                        quest.getName()).replace("<time>", FabricMiscUtil
                        .getTime(getRemainingCooldown(quest)));
                sendMessage(msg);
            }
            return false;
        }
        if (quest.getRegionStart() != null && !quest.isInRegionStart(this)) {
            if (giveReason) {
                sendMessage(FabricLang.get(getServerPlayer(), "questInvalidLocation").replace("<quest>", quest.getName()));
            }
            return false;
        }
        if (quest instanceof FabricQuest fabricQuest && fabricQuest.getBlockStart() != null) {
            if (giveReason) {
                sendMessage(FabricLang.get(getServerPlayer(), "noCommandStart").replace("<quest>", quest.getName()));
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isOnTime(Quest quest, boolean giveReason) {
        if (quest == null) return true;
        final Planner pln = quest.getPlanner();
        final long currentTime = System.currentTimeMillis();
        final long start = pln.getStartInMillis(); // Start time in milliseconds since UTC epoch
        final long end = pln.getEndInMillis(); // End time in milliseconds since UTC epoch
        final long duration = end - start; // How long the quest can be active for
        final long repeat = pln.getRepeat(); // Length to wait in-between start times
        if (pln.hasStart()) {
            if (currentTime < start) {
                if (giveReason) {
                    String early = FabricLang.get(getServerPlayer(), "plnTooEarly");
                    early = early.replace("<quest>", quest.getName());
                    early = early.replace("<time>", FabricMiscUtil.getTime(start - currentTime));
                    sendMessage(early);
                }
                return false;
            }
        }
        if (pln.hasEnd() && !pln.hasRepeat()) {
            if (currentTime > end) {
                if (giveReason) {
                    String late = FabricLang.get(getServerPlayer(), "plnTooLate");
                    late = late.replace("<quest>", quest.getName());
                    late = late.replace("<time>", FabricMiscUtil.getTime(currentTime - end));
                    sendMessage(late);
                }
                return false;
            }
        }
        if (pln.hasRepeat() && pln.hasStart() && pln.hasEnd()) {
            // Repeatable quest
            if (currentTime <= end) {
                // Initial period where quest may be active
                if (getCompletedTimes().containsKey(quest) && pln.hasCooldown()
                        && getRemainingCooldown(quest) > 0) {
                    if (giveReason) {
                        final String early = FabricLang.get(getServerPlayer(), "plnTooEarly").replace("<quest>", quest.getName())
                                .replace("<time>", FabricMiscUtil.getTime(end - currentTime));
                        sendMessage(early);
                        return false;
                    }
                }
            } else {
                // Subsequent period where quest may be active
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
                for (final Map.Entry<Long, Long> startEnd : mostRecent.entrySet()) {
                    if (startEnd.getKey() <= currentTime && currentTime < startEnd.getValue()) {
                        active = true;
                        break;
                    }
                }

                // If quest is not active, or new period of activity should override player cooldown
                if (!active || (pln.getOverride() && completedEnd > 0L && currentTime < completedEnd)) {
                    if (giveReason) {
                        final String early = FabricLang.get(getServerPlayer(), "plnTooEarly").replace("<quest>", quest.getName())
                                .replace("<time>", FabricMiscUtil.getTime(completedEnd - currentTime));
                        sendMessage(early);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void takeQuest(Quest quest, boolean ignoreRequirements) {
        if (quest == null) return;
        if (getServerPlayer() != null) {
            if (!isOnTime(quest, true)) {
                return;
            }
        }
        if (!ignoreRequirements && !quest.testRequirements(this)) {
            sendMessage(FabricLang.get(getServerPlayer(), "doesNotMeetReqs"));
            return;
        }
        currentQuests.put(quest, 0);
        addEmptiesFor(quest, 0);
        final ServerPlayer player = getServerPlayer();
        if (player != null && !ignoreRequirements) {
            final var requirements = quest.getRequirements();
            final var inventory = player.getInventory();
            final int size = inventory.getContainerSize();
            final ItemStack[] original = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                final ItemStack slot = inventory.getItem(i);
                original[i] = slot.isEmpty() ? ItemStack.EMPTY : slot.copy();
            }
            boolean ok = true;
            if (requirements.getItems() != null && requirements.getRemoveItems() != null
                    && requirements.getRemoveItems().size() == requirements.getItems().size()) {
                for (int i = 0; i < requirements.getItems().size(); i++) {
                    final boolean doRemove = Boolean.TRUE.equals(requirements.getRemoveItems().get(i));
                    if (doRemove && requirements.getItems().get(i) instanceof ItemStack stack) {
                        ok &= FabricInventoryUtil.removeItem(player, stack);
                    }
                }
            }
            if (!ok) {
                for (int i = 0; i < size; i++) {
                    inventory.setItem(i, original[i]);
                }
                sendMessage(FabricLang.get(getServerPlayer(), "requirementsItemFail"));
                hardQuit(quest);
                return;
            }
        }
        final boolean isGlobalQuest = quest.getOptions().canGiveGloballyAtLogin();
        if (player != null && (!isGlobalQuest || quest.getOptions().canInformOnStart())) {
            final String accepted = FabricLang.get(getServerPlayer(), "questAccepted").replace("<quest>", quest.getName());
            sendMessage("§a" + accepted);
            sendMessage("");
            if (plugin.getConfigSettings().canShowQuestTitles()) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§e" + quest.getName())));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(
                        "§6" + FabricLang.get(getServerPlayer(), "quest") + " " + FabricLang.get(getServerPlayer(), "accepted"))));
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            }
        }
        final Stage stage = quest.getStage(0);
        if (player != null) {
            sendMessage("§6" + FabricLang.get(getServerPlayer(), "objectives").replace("<quest>", quest.getName()));
            showCurrentObjectives(quest, this, false);
            if (stage != null && stage.getStartMessage() != null) {
                sendMessage(stage.getStartMessage());
            }
            showCurrentConditions(quest, this);
        }
        if (stage != null && stage.getStartAction() != null) {
            stage.getStartAction().fire(this, quest);
        }
        if (quest.getInitialAction() != null) {
            quest.getInitialAction().fire(this, quest);
        }
        questIdToTake = null;
        saveData();
        setCompassTarget(quest);
        quest.updateCompass(this, stage);
        updateJournal();
    }

    @Override
    public boolean abandonQuest(Quest quest, String message) {
        return abandonQuest(quest, message != null ? new String[]{message} : new String[0]);
    }

    @Override
    public boolean abandonQuest(Quest quest, String[] messages) {
        if (quest == null || !currentQuests.containsKey(quest)) return false;
        if (!quest.getOptions().canAllowQuitting()) {
            sendMessage(FabricLang.get(getServerPlayer(), "questQuitDisabled"));
            return false;
        }
        quitQuest(quest, messages);
        return true;
    }

    @Override
    public void quitQuest(Quest quest, String message) {
        quitQuest(quest, message != null ? new String[]{message} : new String[0]);
    }

    @Override
    public void quitQuest(Quest quest, String[] messages) {
        if (quest == null) return;
        hardQuit(quest);
        for (final String msg : messages) {
            if (msg != null && !msg.isEmpty()) {
                sendMessage(msg);
            }
        }
        sendMessage(FabricLang.get(getServerPlayer(), "questQuit").replace("<quest>", quest.getName()));
        saveData();
        updateJournal();
    }

    @Override
    public void listQuests(Quester quester, int page) {
        if (quester == null) return;
        sendMessage("§6--- Your Quests ---");
        final Collection<Quest> quests = quester.getCurrentQuests().keySet();
        if (quests.isEmpty()) {
            sendMessage(FabricLang.get(getServerPlayer(), "noCurrentQuest")
                    .replace("<player>", getServerPlayer() == null ? "" : getServerPlayer().getName().getString()));
            return;
        }
        int idx = 0;
        for (final Quest q : quests) {
            final Stage stage = quester.getCurrentStage(q);
            final String stageInfo = stage != null ? " (Stage " + (quester.getCurrentQuests().get(q) + 1) + ")" : "";
            sendMessage("§7- §f" + q.getName() + stageInfo);
            idx++;
        }
        sendMessage("§7Total: " + idx);
    }

    @Override
    public LinkedList<String> getCurrentRequirements(Quest quest, boolean ignoreOverrides) {
        final LinkedList<String> reqs = new LinkedList<>();
        if (quest == null) return reqs;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return reqs;

        // Blocks to break
        for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
            reqs.add(FabricLang.get(getServerPlayer(), "reqBreakBlock"));
        }
        // Blocks to place
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            reqs.add(FabricLang.get(getServerPlayer(), "reqPlaceBlock"));
        }
        // Items to craft
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            reqs.add(FabricLang.get(getServerPlayer(), "reqCraftItem"));
        }
        // Mobs to kill
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            final int amt = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            reqs.add(FabricLang.get(getServerPlayer(), "reqKillMob").replace("<amount>", String.valueOf(amt)));
        }
        // NPCs to interact
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            reqs.add(FabricLang.get(getServerPlayer(), "reqTalkToNpc"));
        }
        // Players to kill
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            reqs.add(FabricLang.get(getServerPlayer(), "reqKillPlayer").replace("<amount>", String.valueOf(stage.getPlayersToKill())));
        }

        return reqs;
    }

    @Override
    public LinkedList<Objective> getCurrentObjectives(Quest quest, boolean ignoreOverrides, boolean formatNames) {
        final LinkedList<Objective> objs = new LinkedList<>();
        if (quest == null) return objs;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return objs;
        final QuestProgress progress = getQuestProgressOrDefault(quest);

        // Break blocks
        for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
            final Object goalObj = stage.getBlocksToBreak().get(i);
            if (goalObj == null) continue;
            final int goal = getBlockAmount(stage.getBlocksToBreakAmounts(), i);
            final int current = (progress.getBlocksBroken().size() > i) ? progress.getBlocksBroken().get(i) : 0;
            final String msg = FabricLang.get(getServerPlayer(), "questBreakBlock").replace("<goal>", String.valueOf(goal));
            objs.add(new FabricObjective(ObjectiveType.BREAK_BLOCK, formatNames ? msg : msg, current, goal));
        }

        // Place blocks
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            final int goal = getBlockAmount(stage.getBlocksToPlaceAmounts(), i);
            final int current = (progress.getBlocksPlaced().size() > i) ? progress.getBlocksPlaced().get(i) : 0;
            final String msg = FabricLang.get(getServerPlayer(), "questPlaceBlock").replace("<goal>", String.valueOf(goal));
            objs.add(new FabricObjective(ObjectiveType.PLACE_BLOCK, formatNames ? msg : msg, current, goal));
        }

        // Damage blocks
        for (int i = 0; i < stage.getBlocksToDamage().size(); i++) {
            final Object goalObj = stage.getBlocksToDamage().get(i);
            if (goalObj == null) continue;
            final int goal = 1;
            final int current = (progress.getBlocksDamaged().size() > i) ? progress.getBlocksDamaged().get(i) : 0;
            final String blockName;
            if (goalObj instanceof ItemStack dGoal) {
                blockName = FabricItemUtil.getName(dGoal);
            } else {
                final String raw = goalObj.toString();
                blockName = FabricMiscUtil.snakeCaseToUpperCamelCase(
                        raw.indexOf(':') != -1 ? raw.substring(raw.indexOf(':') + 1) : raw);
            }
            final String msg = FabricLang.get(getServerPlayer(), "damage")
                    .replace("<item>", blockName)
                    .replace("<count>", String.valueOf(goal))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.DAMAGE_BLOCK, formatNames ? msg : msg, current, goal));
        }

        // Items crafted
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            final int current = (progress.getItemsCrafted().size() > i) ? progress.getItemsCrafted().get(i) : 0;
            final String msg = FabricLang.get(getServerPlayer(), "questCraftItem").replace("<goal>", "1");
            objs.add(new FabricObjective(ObjectiveType.CRAFT_ITEM, formatNames ? msg : msg, current, 1));
        }

        // Items smelted
        for (int i = 0; i < stage.getItemsToSmelt().size(); i++) {
            final Object goalObj = stage.getItemsToSmelt().get(i);
            if (!(goalObj instanceof ItemStack sGoal)) continue;
            final int goal = Math.max(1, sGoal.getCount());
            final int current = (progress.getItemsSmelted().size() > i) ? progress.getItemsSmelted().get(i) : 0;
            final String msg = FabricLang.get(getServerPlayer(), "smeltItem")
                    .replace("<item>", FabricItemUtil.getName(sGoal))
                    .replace("<count>", String.valueOf(goal));
            objs.add(new FabricObjective(ObjectiveType.SMELT_ITEM, formatNames ? msg : msg, current, goal));
        }

        // Items enchanted
        for (int i = 0; i < stage.getItemsToEnchant().size(); i++) {
            final Object goalObj = stage.getItemsToEnchant().get(i);
            if (!(goalObj instanceof ItemStack eGoal)) continue;
            final int goal = Math.max(1, eGoal.getCount());
            final int current = (progress.getItemsEnchanted().size() > i) ? progress.getItemsEnchanted().get(i) : 0;
            String msg = FabricLang.get(getServerPlayer(), "enchItem");
            final ItemEnchantments ench = eGoal.get(DataComponents.ENCHANTMENTS);
            if (ench != null && !ench.isEmpty()) {
                final var first = ench.entrySet().iterator().next();
                final Holder<Enchantment> holder = first.getKey();
                String enchName = holder.getRegisteredName();
                if (enchName != null && enchName.indexOf(':') != -1) {
                    enchName = enchName.substring(enchName.indexOf(':') + 1);
                }
                msg = msg.replace("<enchantment>", FabricMiscUtil.snakeCaseToUpperCamelCase(enchName))
                        .replace("<level>", toRoman(first.getIntValue()));
            } else {
                msg = msg.replace("<enchantment>", "").replace("<level>", "");
            }
            msg = msg.replace("<item>", FabricItemUtil.getName(eGoal))
                    .replace("<count>", String.valueOf(goal))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.ENCHANT_ITEM, formatNames ? msg : msg, current, goal));
        }

        // Items brewed
        for (int i = 0; i < stage.getItemsToBrew().size(); i++) {
            final Object goalObj = stage.getItemsToBrew().get(i);
            if (!(goalObj instanceof ItemStack bGoal)) continue;
            final int goal = Math.max(1, bGoal.getCount());
            final int current = (progress.getItemsBrewed().size() > i) ? progress.getItemsBrewed().get(i) : 0;
            final String msg = FabricLang.get(getServerPlayer(), "brewItem")
                    .replace("<item>", FabricItemUtil.getName(bGoal))
                    .replace(" <level>", "")
                    .replace("<level>", "")
                    .replace("<count>", String.valueOf(goal))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.BREW_ITEM, formatNames ? msg : msg, current, goal));
        }

        // Mobs killed
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            final int goal = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            final int current = (progress.getMobNumKilled().size() > i) ? progress.getMobNumKilled().get(i) : 0;
            String msg;
            if (stage.getLocationsToKillWithin().isEmpty()) {
                msg = FabricLang.get(getServerPlayer(), "questKillMob").replace("<goal>", String.valueOf(goal));
            } else {
                final String killName = stage.getKillNames() != null && stage.getKillNames().size() > i
                        && stage.getKillNames().get(i) != null ? stage.getKillNames().get(i) : "?";
                msg = FabricLang.get(getServerPlayer(), "killAtLocation").replace("<location>", killName)
                        .replace("<count>", current + "/" + goal);
            }
            msg = msg.replace("<mob>", FabricMiscUtil.snakeCaseToUpperCamelCase(
                    stage.getMobsToKill().get(i).toString()));
            objs.add(new FabricObjective(ObjectiveType.KILL_MOB, formatNames ? msg : msg, current, goal));
        }

        // Mobs tamed
        for (int i = 0; i < stage.getMobsToTame().size(); i++) {
            final int goal = (stage.getMobNumToTame() != null && stage.getMobNumToTame().size() > i)
                    ? stage.getMobNumToTame().get(i) : 1;
            final int current = (progress.getMobsTamed().size() > i) ? progress.getMobsTamed().get(i) : 0;
            final Object tameObj = stage.getMobsToTame().get(i);
            final String mobName = tameObj != null
                    ? FabricMiscUtil.snakeCaseToUpperCamelCase(tameObj.toString()) : "?";
            final String msg = FabricLang.get(getServerPlayer(), "tame")
                    .replace("<mob>", mobName)
                    .replace("<count>", String.valueOf(goal));
            objs.add(new FabricObjective(ObjectiveType.TAME_MOB, formatNames ? msg : msg, current, goal));
        }

        // Sheep sheared
        for (int i = 0; i < stage.getSheepToShear().size(); i++) {
            final int goal = (stage.getSheepNumToShear() != null && stage.getSheepNumToShear().size() > i)
                    ? stage.getSheepNumToShear().get(i) : 1;
            final int current = (progress.getSheepSheared().size() > i) ? progress.getSheepSheared().get(i) : 0;
            final Object dyeObj = stage.getSheepToShear().get(i);
            final String color = dyeObj != null ? FabricMiscUtil.snakeCaseToUpperCamelCase(dyeObj.toString()) : "?";
            final String msg = FabricLang.get(getServerPlayer(), "shearSheep")
                    .replace("<color>", color)
                    .replace("<count>", String.valueOf(goal));
            objs.add(new FabricObjective(ObjectiveType.SHEAR_SHEEP, formatNames ? msg : msg, current, goal));
        }

        // NPCs interacted
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            final boolean current = (progress.getNpcsInteracted().size() > i) && progress.getNpcsInteracted().get(i);
            objs.add(new FabricObjective(ObjectiveType.TALK_TO_NPC, FabricLang.get(getServerPlayer(), "questTalkToNpc"), current ? 1 : 0, 1));
        }

        // NPCs killed
        for (int i = 0; i < stage.getNpcsToKill().size(); i++) {
            final int goal = (stage.getNpcNumToKill() != null && stage.getNpcNumToKill().size() > i)
                    ? stage.getNpcNumToKill().get(i) : 1;
            final int current = (progress.getNpcsNumKilled().size() > i) ? progress.getNpcsNumKilled().get(i) : 0;
            objs.add(new FabricObjective(ObjectiveType.KILL_NPC, FabricLang.get(getServerPlayer(), "questKillNpc"), current, goal));
        }

        // Players killed
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            objs.add(new FabricObjective(ObjectiveType.KILL_PLAYER, FabricLang.get(getServerPlayer(), "questKillPlayer"),
                    progress.getPlayersKilled(), stage.getPlayersToKill()));
        }

        // Consume items
        for (int i = 0; i < stage.getItemsToConsume().size(); i++) {
            final int goal = 1;
            final int current = (progress.getItemsConsumed().size() > i) ? progress.getItemsConsumed().get(i) : 0;
            objs.add(new FabricObjective(ObjectiveType.CONSUME_ITEM, FabricLang.get(getServerPlayer(), "questConsumeItem"), current, goal));
        }

        // Use blocks
        for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
            final int goal = getBlockAmount(stage.getBlocksToUseAmounts(), i);
            final int current = (progress.getBlocksUsed().size() > i) ? progress.getBlocksUsed().get(i) : 0;
            objs.add(new FabricObjective(ObjectiveType.USE_BLOCK, FabricLang.get(getServerPlayer(), "questUseBlock"), current, goal));
        }

        // Passwords said
        for (int i = 0; i < stage.getPasswordDisplays().size(); i++) {
            final boolean said = (progress.getPasswordsSaid().size() > i) && progress.getPasswordsSaid().get(i);
            final String display = stage.getPasswordDisplays().get(i);
            final String msg = display != null ? display : "";
            objs.add(new FabricObjective(ObjectiveType.PASSWORD, msg, said ? 1 : 0, 1));
        }

        // Items delivered
        for (int i = 0; i < stage.getItemsToDeliver().size(); i++) {
            final Object goalObj = stage.getItemsToDeliver().get(i);
            if (!(goalObj instanceof ItemStack goal)) continue;
            final int goalAmount = Math.max(1, goal.getCount());
            final int current = (progress.getItemsDelivered().size() > i) ? progress.getItemsDelivered().get(i) : 0;
            final UUID npcUuid = (stage.getItemDeliveryTargets().size() > i)
                    ? stage.getItemDeliveryTargets().get(i) : null;
            final String msg = FabricLang.get(getServerPlayer(), "deliver")
                    .replace("<item>", FabricItemUtil.getName(goal))
                    .replace("<npc>", npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?")
                    .replace("<count>", String.valueOf(goalAmount));
            objs.add(new FabricObjective(ObjectiveType.DELIVER_ITEM, formatNames ? msg : msg, current, goalAmount));
        }

        // Custom objectives
        for (int i = 0; i < stage.getCustomObjectives().size(); i++) {
            final CustomObjective co = stage.getCustomObjectives().get(i);
            if (co == null) continue;
            final int goal = (stage.getCustomObjectiveCounts() != null && stage.getCustomObjectiveCounts().size() > i)
                    ? stage.getCustomObjectiveCounts().get(i) : 1;
            final int current = (progress.getCustomObjectiveCounts().size() > i)
                    ? progress.getCustomObjectiveCounts().get(i) : 0;
            String msg = co.getDisplay() != null ? co.getDisplay() : "";
            if (co instanceof FabricCustomObjective fab) {
                for (final Map.Entry<String, Object> prompt : fab.getData()) {
                    final String replacement = "%" + prompt.getKey() + "%";
                    if (msg.contains(replacement)) {
                        for (final Map.Entry<String, Object> e : stage.getCustomObjectiveData()) {
                            if (e.getKey().equals(prompt.getKey())) {
                                msg = msg.replace(replacement, String.valueOf(e.getValue()));
                            }
                        }
                    }
                }
            }
            if (co.canShowCount()) {
                msg = msg.replace("%count%", current + "/" + goal);
            }
            objs.add(new FabricObjective(ObjectiveType.CUSTOM, msg.trim(), current, goal));
        }

        return objs;
    }

    private int getBlockAmount(final LinkedList<Integer> amounts, final int index) {
        return amountPresent(amounts, index) ? amounts.get(index) : 1;
    }

    private static String toRoman(int value) {
        if (value < 1 || value > 3999) return String.valueOf(value);
        final int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (value >= values[i]) {
                sb.append(numerals[i]);
                value -= values[i];
            }
        }
        return sb.toString();
    }

    private boolean amountPresent(final LinkedList<Integer> amounts, final int index) {
        return amounts != null && !amounts.isEmpty() && amounts.size() > index;
    }

    @Override
    public void showCurrentObjectives(Quest quest, Quester quester, boolean ignoreOverrides) {
        final LinkedList<Objective> objectives = getCurrentObjectives(quest, ignoreOverrides, true);
        if (objectives.isEmpty()) return;
        sendMessage("§6--- " + quest.getName() + " ---");
        for (final Objective obj : objectives) {
            final String msg = obj.getMessage() + " §7(" + obj.getProgress() + "/" + obj.getGoal() + ")";
            sendMessage(msg);
        }
    }

    @Override
    public boolean hasObjective(Quest quest, ObjectiveType type) {
        if (quest == null || type == null) return false;
        final Stage stage = getCurrentStage(quest);
        return stage != null && stage.containsObjective(type);
    }

    @Override
    public boolean hasCustomObjective(Quest quest, String name) {
        if (quest == null || name == null) return false;
        final Stage stage = getCurrentStage(quest);
        return stage != null && stage.getCustomObjectives().stream()
                .anyMatch(co -> co.getName().equals(name));
    }

    @Override
    public void showCurrentConditions(Quest quest, Quester quester) {
        if (quest == null || quester == null) return;
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null) return;
        final Condition condition = stage.getCondition();
        if (condition == null) return;
        sendMessage("§6--- Condition ---");
        final boolean met = condition.check(quester, quest);
        sendMessage((met ? "§a" : "§c") + " " + (condition.getName() != null ? condition.getName() : "Unknown"));
    }

    @Override
    public boolean testComplete(Quest quest) {
        if (quest == null || !currentQuests.containsKey(quest)) return false;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return false;
        final QuestProgress progress = getQuestProgressOrDefault(quest);
        if (progress == null) return false;

        // Break blocks
        for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
            if (progress.getBlocksBroken().size() <= i) return false;
            final int goal = getBlockAmount(stage.getBlocksToBreakAmounts(), i);
            if (progress.getBlocksBroken().get(i) < goal) return false;
        }

        // Damage blocks
        for (int i = 0; i < stage.getBlocksToDamage().size(); i++) {
            if (progress.getBlocksDamaged().size() <= i) return false;
            if (progress.getBlocksDamaged().get(i) < 1) return false;
        }

        // Place blocks
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            if (progress.getBlocksPlaced().size() <= i) return false;
            final int goal = getBlockAmount(stage.getBlocksToPlaceAmounts(), i);
            if (progress.getBlocksPlaced().get(i) < goal) return false;
        }

        // Items crafted
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            if (progress.getItemsCrafted().size() <= i) return false;
            if (progress.getItemsCrafted().get(i) < 1) return false;
        }

        // Items smelted
        for (int i = 0; i < stage.getItemsToSmelt().size(); i++) {
            if (progress.getItemsSmelted().size() <= i) return false;
            if (progress.getItemsSmelted().get(i) < 1) return false;
        }

        // Items enchanted
        for (int i = 0; i < stage.getItemsToEnchant().size(); i++) {
            if (progress.getItemsEnchanted().size() <= i) return false;
            if (progress.getItemsEnchanted().get(i) < 1) return false;
        }

        // Items brewed
        for (int i = 0; i < stage.getItemsToBrew().size(); i++) {
            if (progress.getItemsBrewed().size() <= i) return false;
            if (progress.getItemsBrewed().get(i) < 1) return false;
        }

        // Mobs killed
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            if (progress.getMobNumKilled().size() <= i) return false;
            final int goal = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            if (progress.getMobNumKilled().get(i) < goal) return false;
        }

        // NPCs interacted
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            if (progress.getNpcsInteracted().size() <= i) return false;
            if (!progress.getNpcsInteracted().get(i)) return false;
        }

        // NPCs killed
        for (int i = 0; i < stage.getNpcsToKill().size(); i++) {
            if (progress.getNpcsNumKilled().size() <= i) return false;
            final int goal = (stage.getNpcNumToKill() != null && stage.getNpcNumToKill().size() > i)
                    ? stage.getNpcNumToKill().get(i) : 1;
            if (progress.getNpcsNumKilled().get(i) < goal) return false;
        }

        // Players killed
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            if (progress.getPlayersKilled() < stage.getPlayersToKill()) return false;
        }

        // Use blocks
        for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
            if (progress.getBlocksUsed().size() <= i) return false;
            final int goal = getBlockAmount(stage.getBlocksToUseAmounts(), i);
            if (progress.getBlocksUsed().get(i) < goal) return false;
        }

        // Consume items
        for (int i = 0; i < stage.getItemsToConsume().size(); i++) {
            if (progress.getItemsConsumed().size() <= i) return false;
            if (progress.getItemsConsumed().get(i) < 1) return false;
        }

        // Items delivered
        for (int i = 0; i < stage.getItemsToDeliver().size(); i++) {
            if (progress.getItemsDelivered().size() <= i) return false;
            final Object goal = stage.getItemsToDeliver().get(i);
            final int goalAmount = (goal instanceof ItemStack is) ? Math.max(1, is.getCount()) : 1;
            if (progress.getItemsDelivered().get(i) < goalAmount) return false;
        }

        // Custom objectives
        for (int i = 0; i < stage.getCustomObjectives().size(); i++) {
            if (progress.getCustomObjectiveCounts().size() <= i) return false;
            final int goal = stage.getCustomObjectiveCounts().size() > i
                    ? stage.getCustomObjectiveCounts().get(i) : 1;
            if (progress.getCustomObjectiveCounts().get(i) < goal) return false;
        }

        // Mobs tamed
        for (int i = 0; i < stage.getMobsToTame().size(); i++) {
            if (progress.getMobsTamed().size() <= i) return false;
            final int goal = (stage.getMobNumToTame() != null && stage.getMobNumToTame().size() > i)
                    ? stage.getMobNumToTame().get(i) : 1;
            if (progress.getMobsTamed().get(i) < goal) return false;
        }

        // Sheep sheared
        for (int i = 0; i < stage.getSheepToShear().size(); i++) {
            if (progress.getSheepSheared().size() <= i) return false;
            final int goal = (stage.getSheepNumToShear() != null && stage.getSheepNumToShear().size() > i)
                    ? stage.getSheepNumToShear().get(i) : 1;
            if (progress.getSheepSheared().get(i) < goal) return false;
        }

        // Cows milked
        if (stage.getCowsToMilk() != null && stage.getCowsToMilk() > 0) {
            if (progress.getCowsMilked() < stage.getCowsToMilk()) return false;
        }

        // Fish caught
        if (stage.getFishToCatch() != null && stage.getFishToCatch() > 0) {
            if (progress.getFishCaught() < stage.getFishToCatch()) return false;
        }

        // Passwords said
        for (int i = 0; i < stage.getPasswordPhrases().size(); i++) {
            if (progress.getPasswordsSaid().size() <= i) return false;
            if (!progress.getPasswordsSaid().get(i)) return false;
        }

        return true;
    }

    @Override
    public void addEmptiesFor(Quest quest, int stage) {
        if (quest == null) return;
        final QuestProgress progress = getQuestProgressOrDefault(quest);
        final Stage s = quest.getStage(stage);
        if (s == null || progress == null) return;
        // Ensure lists have correct sizes for objective tracking
        while (progress.getBlocksBroken().size() < s.getBlocksToBreak().size()) {
            progress.getBlocksBroken().add(0);
        }
        while (progress.getBlocksDamaged().size() < s.getBlocksToDamage().size()) {
            progress.getBlocksDamaged().add(0);
        }
        while (progress.getBlocksPlaced().size() < s.getBlocksToPlace().size()) {
            progress.getBlocksPlaced().add(0);
        }
        while (progress.getBlocksUsed().size() < s.getBlocksToUse().size()) {
            progress.getBlocksUsed().add(0);
        }
        while (progress.getItemsCrafted().size() < s.getItemsToCraft().size()) {
            progress.getItemsCrafted().add(0);
        }
        while (progress.getItemsSmelted().size() < s.getItemsToSmelt().size()) {
            progress.getItemsSmelted().add(0);
        }
        while (progress.getItemsEnchanted().size() < s.getItemsToEnchant().size()) {
            progress.getItemsEnchanted().add(0);
        }
        while (progress.getItemsBrewed().size() < s.getItemsToBrew().size()) {
            progress.getItemsBrewed().add(0);
        }
        while (progress.getItemsDelivered().size() < s.getItemsToDeliver().size()) {
            progress.getItemsDelivered().add(0);
        }
        while (progress.getMobNumKilled().size() < s.getMobsToKill().size()) {
            progress.getMobNumKilled().add(0);
        }
        while (progress.getNpcsInteracted().size() < s.getNpcsToInteract().size()) {
            progress.getNpcsInteracted().add(false);
        }
        while (progress.getNpcsNumKilled().size() < s.getNpcsToKill().size()) {
            progress.getNpcsNumKilled().add(0);
        }
        while (progress.getMobsTamed().size() < s.getMobsToTame().size()) {
            progress.getMobsTamed().add(0);
        }
        while (progress.getSheepSheared().size() < s.getSheepToShear().size()) {
            progress.getSheepSheared().add(0);
        }
        while (progress.getPasswordsSaid().size() < s.getPasswordPhrases().size()) {
            progress.getPasswordsSaid().add(false);
        }
        while (progress.getCustomObjectiveCounts().size() < s.getCustomObjectives().size()) {
            progress.getCustomObjectiveCounts().add(0);
        }
    }

    @Override
    public boolean saveData() {
        hasData = true;
        try {
            plugin.getStorage().saveQuester(this).get();
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to save quester data for " + uuid, e);
            return false;
        }
        return true;
    }

    @Override
    public long getCompletionDifference(Quest quest) {
        if (quest == null) return 0;
        final Long completedTime = completedTimes.get(quest);
        if (completedTime == null) return 0;
        return System.currentTimeMillis() - completedTime;
    }

    @Override
    public long getRemainingCooldown(Quest quest) {
        if (quest == null || !quest.getPlanner().hasCooldown()) return 0;
        final long diff = getCompletionDifference(quest);
        final long cooldown = quest.getPlanner().getCooldown();
        if (diff >= cooldown) return 0;
        return cooldown - diff;
    }

    @Override
    public boolean hasData() {
        return hasData;
    }

    @Override
    public boolean hasBaseData() {
        return hasData;
    }

    @Override
    public void startStageTimer(Quest quest) {
        if (quest == null) return;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return;
        final long delay = stage.getDelay();
        final FabricScheduler.ScheduledTask previous = stageTimers.remove(quest);
        if (previous != null) {
            previous.cancel();
        }
        if (delay > 0) {
            stageTimers.put(quest, FabricScheduler.runLater(() -> checkQuest(quest), delay * 20));
        }
    }

    @Override
    public void stopStageTimer(Quest quest) {
        if (quest == null) return;
        final FabricScheduler.ScheduledTask task = stageTimers.remove(quest);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public long getStageTime(Quest quest) {
        if (quest == null || !currentQuests.containsKey(quest)) return 0;
        final QuestProgress progress = getQuestProgress(quest);
        if (progress == null) return 0;
        final long startTime = progress.getDelayStartTime();
        if (startTime <= 0) return 0;
        final Stage stage = getCurrentStage(quest);
        if (stage == null || stage.getDelay() <= 0) return 0;
        final long elapsed = System.currentTimeMillis() - startTime;
        final long total = stage.getDelay() * 1000L;
        return Math.max(0, total - elapsed);
    }

    @Override
    public void checkQuest(Quest quest) {
        if (quest == null || !currentQuests.containsKey(quest)) return;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return;

        // Check conditions
        final Condition condition = stage.getCondition();
        if (condition != null && !condition.check(this, quest)) {
            if (condition.isFailQuest()) {
                quest.failQuest(this);
                return;
            }
        }

        // Test completion
        if (testComplete(quest)) {
            if (stage.getCompleteMessage() != null) {
                sendMessage(stage.getCompleteMessage());
            }
            if (stage.getFinishAction() != null) {
                stage.getFinishAction().fire(this, quest);
            }
            // Advance to next stage or complete quest
            final int nextStage = currentQuests.get(quest) + 1;
            if (quest.getStage(nextStage) != null) {
                currentQuests.put(quest, nextStage);
                addEmptiesFor(quest, nextStage);
                final Stage next = quest.getStage(nextStage);
                if (next.getStartMessage() != null) {
                    sendMessage(next.getStartMessage());
                }
                if (next.getStartAction() != null) {
                    next.getStartAction().fire(this, quest);
                }
                startStageTimer(quest);
                updateJournal();
            } else {
                quest.completeQuest(this);
                updateJournal();
            }
        }
    }

    @Override
    public void showGUIDisplay(UUID npc, LinkedList<Quest> quests) {
        if (quests == null || quests.isEmpty()) {
            sendMessage(FabricLang.get(getServerPlayer(), "noCurrentQuest"));
            return;
        }
        sendMessage("§6--- Available Quests ---");
        for (final Quest quest : quests) {
            final String info = "§7- §f" + quest.getName() + " §8("
                    + (quest.getDescription() != null ? quest.getDescription() : "No description") + ")";
            sendMessage(info);
        }
    }

    @Override
    public void hardQuit(Quest quest) {
        if (quest == null) return;
        stopStageTimer(quest);
        final Iterator<Map.Entry<FabricActionTimer, Quest>> it = actionTimers.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<FabricActionTimer, Quest> entry = it.next();
            if (entry.getValue() == quest || (entry.getValue() != null
                    && entry.getValue().getName() != null && entry.getValue().getName().equals(quest.getName()))) {
                entry.getKey().cancel();
                it.remove();
            }
        }
        if (compassTarget != null && quest.getId() != null && compassTarget.getId() != null
                && compassTarget.getId().equals(quest.getId())) {
            compassTarget = null;
        }
        currentQuests.remove(quest);
        progressData.remove(quest);
    }

    @Override
    public void hardRemove(Quest quest) {
        if (quest == null) return;
        hardQuit(quest);
        completedQuests.remove(quest);
        completedTimes.remove(quest);
        amountsCompleted.remove(quest);
    }

    @Override
    public void hardClear() {
        currentQuests.clear();
        completedQuests.clear();
        completedTimes.clear();
        amountsCompleted.clear();
        progressData.clear();
        questPoints = 0;
    }

    @Override
    public void hardStagePut(Quest key, Integer val) {
        if (key != null && val != null) currentQuests.put(key, val);
    }

    @Override
    public void hardDataPut(Quest key, QuestProgress val) {
        if (key != null && val != null) progressData.put(key, val);
    }

    @Override
    public boolean canUseCompass() {
        return compassTarget != null;
    }

    @Override
    public void resetCompass() {
        compassTarget = null;
        final ServerPlayer player = getServerPlayer();
        final net.minecraft.server.MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
        if (player == null || server == null) return;
        final net.minecraft.server.level.ServerLevel overworld = server.overworld();
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket(
                net.minecraft.world.level.storage.LevelData.RespawnData.of(
                        overworld.dimension(), overworld.getRespawnData().pos(), 0f, 0f)));
    }

    @Override
    public void findCompassTarget() {
        // Here we apply this method to OPs by not checking #canUseCompass
        final ServerPlayer player = getServerPlayer();
        if (player == null || !plugin.getDependencies().hasPermission(uuid, "quests.compass")) {
            return;
        }
        for (final Quest quest : currentQuests.keySet()) {
            final Stage stage = getCurrentStage(quest);
            if (stage == null) continue;
            if (stage.hasLocatableObjective()) {
                pointCompass(quest, stage);
                setCompassTarget(quest);
            } else {
                resetCompass();
                setCompassTarget(quest);
            }
            break;
        }
    }

    @Override
    public void findNextCompassTarget(final boolean notify) {
        // Here we apply this method to OPs by not checking #canUseCompass
        final ServerPlayer player = getServerPlayer();
        if (player == null || !plugin.getDependencies().hasPermission(uuid, "quests.compass")) {
            return;
        }
        final LinkedList<String> list = currentQuests.keySet().stream()
                .sorted(Comparator.comparing(Quest::getName)).map(Quest::getId)
                .collect(Collectors.toCollection(LinkedList::new));
        int index = 0;
        if (compassTarget != null) {
            if (!list.contains(compassTarget.getId()) && notify) {
                return;
            }
            index = list.indexOf(compassTarget.getId()) + 1;
            if (index >= list.size()) {
                index = 0;
            }
        }
        if (!list.isEmpty()) {
            final Quest quest = plugin.getQuestById(list.get(index));
            if (quest == null) return;
            setCompassTarget(quest);
            final Stage stage = getCurrentStage(quest);
            if (stage != null) {
                if (stage.hasLocatableObjective()) {
                    pointCompass(quest, stage);
                    if (notify) {
                        sendMessage("§e" + FabricLang.get(getServerPlayer(), "compassSet").replace("<quest>", quest.getName()));
                    }
                } else {
                    resetCompass();
                    setCompassTarget(quest);
                    if (notify) {
                        sendMessage("§c" + FabricLang.get(getServerPlayer(), "compassNone").replace("<quest>", quest.getName()));
                    }
                }
            }
        } else {
            sendMessage("§c" + FabricLang.get(getServerPlayer(), "journalNoQuests")
                    .replace("<journal>", FabricLang.get(getServerPlayer(), "journalTitle")));
        }
    }

    /** Point the player's compass at the first location of the given stage that has not been reached yet. */
    private void pointCompass(Quest quest, Stage stage) {
        final QuestProgress progress = getQuestProgressOrDefault(quest);
        for (int i = 0; i < stage.getLocationsToReach().size(); i++) {
            final boolean reached = progress.getLocationsReached().size() > i
                    && progress.getLocationsReached().get(i);
            if (!reached) {
                final BlockPos target = parseLocationBlock(stage.getLocationsToReach().get(i));
                if (target != null) {
                    setPlayerCompass(target);
                }
                return;
            }
        }
    }

    private void setPlayerCompass(BlockPos pos) {
        if (pos == null) return;
        final ServerPlayer player = getServerPlayer();
        if (player == null) return;
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket(
                net.minecraft.world.level.storage.LevelData.RespawnData.of(
                        player.level().dimension(), pos, 0f, 0f)));
    }

    private static BlockPos parseLocationBlock(Object locObj) {
        if (locObj == null) return null;
        final String[] parts = locObj.toString().split(" ");
        if (parts.length < 3) return null;
        try {
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Collection<String> dispatchMultiplayerEverything(Quest quest, ObjectiveType type, BiFunction<Quester, Quest, Void> fun) {
        final Set<String> appliedQuestIDs = new HashSet<>();
        if (quest != null) {
            final Collection<Quester> mq = getMultiplayerQuesters(quest);
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
        return appliedQuestIDs;
    }

    @Override
    public Collection<String> dispatchMultiplayerObjectives(Quest quest, Stage currentStage, Function<Quester, Void> fun) {
        final Set<String> appliedQuestIDs = new HashSet<>();
        final Collection<Quester> mq = getMultiplayerQuesters(quest);
        for (final Quester q : mq) {
            if (q == null) {
                continue;
            }
            if (q.getCurrentQuests().containsKey(quest) && currentStage.equals(q.getCurrentStage(quest))) {
                fun.apply(q);
                appliedQuestIDs.add(quest.getId());
            }
        }
        return appliedQuestIDs;
    }

    @Override
    public Collection<Quester> getMultiplayerQuesters(Quest quest) {
        final Set<Quester> mq = new HashSet<>();
        if (quest == null) {
            return mq;
        }
        // Party sharing via Open Parties and Claims (parties only)
        if (plugin.getDependencies().hasOpenParties() && quest.getOptions().canUsePartiesPlugin()) {
            try {
                final double distanceSquared = quest.getOptions().getShareDistance()
                        * quest.getOptions().getShareDistance();
                if (quest.getOptions().canHandleOfflinePlayers()) {
                    for (final UUID memberUUID : plugin.getDependencies().getPartyMemberUuids(getUUID())) {
                        final FabricQuester otherQuester = plugin.getQuester(memberUUID);
                        if (otherQuester != null) {
                            mq.add(otherQuester);
                        }
                    }
                } else {
                    final ServerPlayer player = getServerPlayer();
                    if (player != null) {
                        for (final ServerPlayer other : plugin.getDependencies().getOnlinePartyMembers(getUUID())) {
                            if (distanceSquared > 0) {
                                if (player.level() == other.level() && player.distanceToSqr(other) <= distanceSquared) {
                                    mq.add(plugin.getQuester(other.getUUID()));
                                }
                            } else {
                                mq.add(plugin.getQuester(other.getUUID()));
                            }
                        }
                    }
                }
                if (plugin.getConfigSettings().getConsoleLogging() > 3) {
                    FabricQuestsPlugin.LOGGER.info("Found {} party members for quest ID {}",
                            mq.size(), quest.getId());
                }
                return mq;
            } catch (final Exception e) {
                FabricQuestsPlugin.LOGGER.warn("Failed to resolve party members for quest ID {}", quest.getId(), e);
            }
        }
        // Fallback: proximity-based sharing
        final ServerPlayer player = getServerPlayer();
        if (player == null) return mq;
        final double shareDistance = quest.getOptions().getShareDistance();
        if (shareDistance <= 0) return mq;
        for (final ServerPlayer other : player.level().players()) {
            if (other.equals(player)) continue;
            if (player.distanceTo(other) > shareDistance) continue;
            final FabricQuester otherQuester = plugin.getQuester(other.getUUID());
            if (otherQuester.getCurrentQuests().containsKey(quest)) {
                mq.add(otherQuester);
            }
        }
        return mq;
    }

    @Override
    public boolean meetsCondition(Quest quest, boolean giveReason) {
        if (quest == null || !currentQuests.containsKey(quest)) return true;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return true;
        final Condition condition = stage.getCondition();
        if (condition != null && !condition.check(this, quest)) {
            if (giveReason) {
                sendMessage("You do not meet the conditions for this quest.");
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isSelectingBlock() {
        return plugin.isSelectingBlockStart(uuid) || plugin.getTempBlocks().containsKey(uuid);
    }

    @Override
    public boolean isInRegion(String regionID) {
        final ServerPlayer player = getServerPlayer();
        if (player == null) return false;
        return plugin.getDependencies().getRegionsAt(player).contains(regionID);
    }

    @Override
    public boolean canAcceptQuest(UUID npc) {
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getNpcStart() != null && !completedQuests.contains(q)) {
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

    @Override
    public boolean canAcceptCompletedQuest(UUID npc) {
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getNpcStart() != null && completedQuests.contains(q)) {
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

    @Override
    public boolean canAcceptCompletedRedoableQuest(UUID npc) {
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getNpcStart() != null && completedQuests.contains(q)
                    && q.getPlanner() != null && q.getPlanner().getCooldown() > -1) {
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

    @Override
    public int compareTo(Quester other) {
        if (other == null) return 1;
        return Integer.compare(this.questPoints, other.getQuestPoints());
    }

    public ServerPlayer getServerPlayer() {
        return plugin.getServer() != null ? plugin.getServer().getPlayerList().getPlayer(uuid) : null;
    }
}
