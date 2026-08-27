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
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.FabricObjective;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.FabricStageTimer;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    private boolean hasData = false;

    public FabricQuester(FabricQuestsPlugin plugin, UUID uuid) {
        this.plugin = plugin;
        this.uuid = uuid;
    }

    @Override public Quests getPlugin() { return plugin; }
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
        return false;
    }

    @Override
    public int getJournalIndex() {
        return 0;
    }

    @Override
    public void updateJournal() {
        // Journal not supported on Fabric; quests are tracked via commands
    }

    @Override
    public boolean offerQuest(Quest quest, boolean giveReason) {
        if (quest == null) return false;
        if (currentQuests.containsKey(quest)) {
            if (giveReason) sendMessage(FabricLang.get("alreadyOnQuest"));
            return false;
        }
        if (plugin.getConfigSettings().getMaxQuests() > 0
                && currentQuests.size() >= plugin.getConfigSettings().getMaxQuests()
                && !quest.getOptions().canOverrideMaxQuests()) {
            if (giveReason) sendMessage(FabricLang.get("tooManyQuests"));
            return false;
        }
        if (plugin.getConfigSettings().canConfirmAccept()) {
            questIdToTake = quest.getId();
            sendMessage(FabricLang.get("confirmQuestTake").replace("<quest>", quest.getName()));
        } else {
            takeQuest(quest, false);
        }
        return true;
    }

    @Override
    public boolean canAcceptOffer(Quest quest, boolean giveReason) {
        if (quest == null) return false;
        if (currentQuests.containsKey(quest)) {
            if (giveReason) sendMessage(FabricLang.get("alreadyOnQuest"));
            return false;
        }
        if (completedQuests.contains(quest) && !quest.getPlanner().hasRepeat()) {
            if (giveReason) sendMessage(FabricLang.get("alreadyCompleted"));
            return false;
        }
        return true;
    }

    @Override
    public boolean isOnTime(Quest quest, boolean giveReason) {
        if (quest == null) return true;
        final long now = System.currentTimeMillis();
        if (quest.getPlanner().hasStart()) {
            if (now < quest.getPlanner().getStartInMillis()) {
                if (giveReason) sendMessage(FabricLang.get("notStarted"));
                return false;
            }
        }
        if (quest.getPlanner().hasEnd()) {
            if (now > quest.getPlanner().getEndInMillis()) {
                if (giveReason) sendMessage(FabricLang.get("questExpired"));
                return false;
            }
        }
        return true;
    }

    @Override
    public void takeQuest(Quest quest, boolean ignoreRequirements) {
        if (quest == null) return;
        if (!ignoreRequirements && !quest.testRequirements(this)) {
            sendMessage(FabricLang.get("doesNotMeetReqs"));
            return;
        }
        currentQuests.put(quest, 0);
        addEmptiesFor(quest, 0);
        final Stage stage = quest.getStage(0);
        if (stage != null && stage.getStartMessage() != null) {
            sendMessage(stage.getStartMessage());
        }
        if (stage != null && stage.getStartAction() != null) {
            stage.getStartAction().fire(this, quest);
        }
        if (quest.getInitialAction() != null) {
            quest.getInitialAction().fire(this, quest);
        }
        questIdToTake = null;
        saveData();
    }

    @Override
    public boolean abandonQuest(Quest quest, String message) {
        return abandonQuest(quest, message != null ? new String[]{message} : new String[0]);
    }

    @Override
    public boolean abandonQuest(Quest quest, String[] messages) {
        if (quest == null || !currentQuests.containsKey(quest)) return false;
        if (!quest.getOptions().canAllowQuitting()) {
            sendMessage(FabricLang.get("cannotQuit"));
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
        currentQuests.remove(quest);
        progressData.remove(quest);
        for (final String msg : messages) {
            if (msg != null && !msg.isEmpty()) {
                sendMessage(msg);
            }
        }
        sendMessage(FabricLang.get("questAbandoned").replace("<quest>", quest.getName()));
        saveData();
    }

    @Override
    public void listQuests(Quester quester, int page) {
        if (quester == null) return;
        sendMessage("§6--- Your Quests ---");
        final Collection<Quest> quests = quester.getCurrentQuests().keySet();
        if (quests.isEmpty()) {
            sendMessage(FabricLang.get("noCurrentQuest"));
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
            reqs.add(FabricLang.get("reqBreakBlock"));
        }
        // Blocks to place
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            reqs.add(FabricLang.get("reqPlaceBlock"));
        }
        // Items to craft
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            reqs.add(FabricLang.get("reqCraftItem"));
        }
        // Mobs to kill
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            final int amt = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            reqs.add(FabricLang.get("reqKillMob").replace("<amount>", String.valueOf(amt)));
        }
        // NPCs to interact
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            reqs.add(FabricLang.get("reqTalkToNpc"));
        }
        // Players to kill
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            reqs.add(FabricLang.get("reqKillPlayer").replace("<amount>", String.valueOf(stage.getPlayersToKill())));
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
            final int current = (progress.getBlocksBroken().size() > i) ? progress.getBlocksBroken().get(i) : 0;
            final String msg = FabricLang.get("questBreakBlock").replace("<goal>", "1");
            objs.add(new FabricObjective(ObjectiveType.BREAK_BLOCK, formatNames ? msg : msg, current, 1));
        }

        // Place blocks
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            final int current = (progress.getBlocksPlaced().size() > i) ? progress.getBlocksPlaced().get(i) : 0;
            final String msg = FabricLang.get("questPlaceBlock").replace("<goal>", "1");
            objs.add(new FabricObjective(ObjectiveType.PLACE_BLOCK, formatNames ? msg : msg, current, 1));
        }

        // Items crafted
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            final int current = (progress.getItemsCrafted().size() > i) ? progress.getItemsCrafted().get(i) : 0;
            final String msg = FabricLang.get("questCraftItem").replace("<goal>", "1");
            objs.add(new FabricObjective(ObjectiveType.CRAFT_ITEM, formatNames ? msg : msg, current, 1));
        }

        // Mobs killed
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            final int goal = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            final int current = (progress.getMobNumKilled().size() > i) ? progress.getMobNumKilled().get(i) : 0;
            final String msg = FabricLang.get("questKillMob").replace("<goal>", String.valueOf(goal));
            objs.add(new FabricObjective(ObjectiveType.KILL_MOB, formatNames ? msg : msg, current, goal));
        }

        // NPCs interacted
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            final boolean current = (progress.getNpcsInteracted().size() > i) && progress.getNpcsInteracted().get(i);
            objs.add(new FabricObjective(ObjectiveType.TALK_TO_NPC, FabricLang.get("questTalkToNpc"), current ? 1 : 0, 1));
        }

        // NPCs killed
        for (int i = 0; i < stage.getNpcsToKill().size(); i++) {
            final int goal = (stage.getNpcNumToKill() != null && stage.getNpcNumToKill().size() > i)
                    ? stage.getNpcNumToKill().get(i) : 1;
            final int current = (progress.getNpcsNumKilled().size() > i) ? progress.getNpcsNumKilled().get(i) : 0;
            objs.add(new FabricObjective(ObjectiveType.KILL_NPC, FabricLang.get("questKillNpc"), current, goal));
        }

        // Players killed
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            objs.add(new FabricObjective(ObjectiveType.KILL_PLAYER, FabricLang.get("questKillPlayer"),
                    progress.getPlayersKilled(), stage.getPlayersToKill()));
        }

        // Consume items
        for (int i = 0; i < stage.getItemsToConsume().size(); i++) {
            final int goal = (stage.getItemConsumeAmounts() != null && stage.getItemConsumeAmounts().size() > i)
                    ? stage.getItemConsumeAmounts().get(i) : 1;
            final int current = (progress.getItemsConsumed().size() > i) ? progress.getItemsConsumed().get(i) : 0;
            objs.add(new FabricObjective(ObjectiveType.CONSUME_ITEM, FabricLang.get("questConsumeItem"), current, goal));
        }

        // Use blocks
        for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
            final int goal = (stage.getBlockUseAmounts() != null && stage.getBlockUseAmounts().size() > i)
                    ? stage.getBlockUseAmounts().get(i) : 1;
            final int current = (progress.getBlocksUsed().size() > i) ? progress.getBlocksUsed().get(i) : 0;
            objs.add(new FabricObjective(ObjectiveType.USE_BLOCK, FabricLang.get("questUseBlock"), current, goal));
        }

        return objs;
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
            if (progress.getBlocksBroken().get(i) < 1) return false;
        }

        // Place blocks
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            if (progress.getBlocksPlaced().size() <= i) return false;
            if (progress.getBlocksPlaced().get(i) < 1) return false;
        }

        // Items crafted
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            if (progress.getItemsCrafted().size() <= i) return false;
            if (progress.getItemsCrafted().get(i) < 1) return false;
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

        // Cut blocks
        for (int i = 0; i < stage.getBlocksToCut().size(); i++) {
            if (progress.getBlocksCut().size() <= i) return false;
            if (progress.getBlocksCut().get(i) < 1) return false;
        }

        // Use blocks
        for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
            if (progress.getBlocksUsed().size() <= i) return false;
            if (progress.getBlocksUsed().get(i) < 1) return false;
        }

        // Consume items
        for (int i = 0; i < stage.getItemsToConsume().size(); i++) {
            if (progress.getItemsConsumed().size() <= i) return false;
            if (progress.getItemsConsumed().get(i) < 1) return false;
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
        while (progress.getBlocksPlaced().size() < s.getBlocksToPlace().size()) {
            progress.getBlocksPlaced().add(0);
        }
        while (progress.getItemsCrafted().size() < s.getItemsToCraft().size()) {
            progress.getItemsCrafted().add(0);
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
    }

    @Override
    public boolean saveData() {
        hasData = true;
        try {
            plugin.getStorage().saveQuesterData(this);
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
        if (delay > 0) {
            FabricScheduler.runLater(() -> checkQuest(quest), delay * 20);
        }
    }

    @Override
    public void stopStageTimer(Quest quest) {
        // Stage timers are fire-and-forget; stopping is handled by the timer checking if quest is still active
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
            } else {
                quest.completeQuest(this);
            }
        }
    }

    @Override
    public void showGUIDisplay(UUID npc, LinkedList<Quest> quests) {
        if (quests == null || quests.isEmpty()) {
            sendMessage(FabricLang.get("noCurrentQuest"));
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
    }

    @Override
    public void findCompassTarget() {
        if (compassTarget == null) return;
        final Stage stage = getCurrentStage(compassTarget);
        if (stage == null) return;
        // Set compass to first location objective if available
        if (stage.hasLocatableObjective() && !stage.getLocationsToReach().isEmpty()) {
            final Object locObj = stage.getLocationsToReach().get(0);
            if (locObj != null) {
                sendMessage(FabricLang.get("questNowTracking").replace("<quest>", compassTarget.getName()));
            }
        }
    }

    @Override
    public void findNextCompassTarget(boolean notify) {
        if (compassTarget == null) return;
        final Stage stage = getCurrentStage(compassTarget);
        if (stage == null || stage.getLocationsToReach().isEmpty()) return;
        // Find the first unreached location
        final QuestProgress progress = getQuestProgressOrDefault(compassTarget);
        for (int i = 0; i < stage.getLocationsToReach().size(); i++) {
            final boolean reached = (progress.getLocationsReached().size() > i) && progress.getLocationsReached().get(i);
            if (!reached) {
                final Object locObj = stage.getLocationsToReach().get(i);
                if (locObj != null && notify) {
                    sendMessage(FabricLang.get("questTrackingLocation"));
                }
                return;
            }
        }
    }

    @Override
    public Collection<String> dispatchMultiplayerEverything(Quest quest, ObjectiveType type, BiFunction<Quester, Quest, Void> fun) {
        // Multiplayer share not yet implemented for Fabric
        final Collection<String> nearbyNames = new ArrayList<>();
        final ServerPlayer player = getServerPlayer();
        if (player == null) return nearbyNames;
        final double shareDistance = quest.getOptions().getShareDistance();
        if (shareDistance <= 0) return nearbyNames;
        for (final ServerPlayer other : player.serverLevel().players()) {
            if (other.equals(player)) continue;
            if (player.distanceTo(other) > shareDistance) continue;
            final FabricQuester otherQuester = plugin.getQuester(other.getUUID());
            if (otherQuester.getCurrentQuests().containsKey(quest)) {
                fun.apply(otherQuester, quest);
                nearbyNames.add(other.getName().getString());
            }
        }
        return nearbyNames;
    }

    @Override
    public Collection<String> dispatchMultiplayerObjectives(Quest quest, Stage currentStage, Function<Quester, Void> fun) {
        final Collection<String> nearbyNames = new ArrayList<>();
        final ServerPlayer player = getServerPlayer();
        if (player == null) return nearbyNames;
        final double shareDistance = quest.getOptions().getShareDistance();
        if (shareDistance <= 0) return nearbyNames;
        for (final ServerPlayer other : player.serverLevel().players()) {
            if (other.equals(player)) continue;
            if (player.distanceTo(other) > shareDistance) continue;
            final FabricQuester otherQuester = plugin.getQuester(other.getUUID());
            if (otherQuester.getCurrentQuests().containsKey(quest)) {
                fun.apply(otherQuester);
                nearbyNames.add(other.getName().getString());
            }
        }
        return nearbyNames;
    }

    @Override
    public Collection<Quester> getMultiplayerQuesters(Quest quest) {
        final Collection<Quester> multiplayerQuesters = new ArrayList<>();
        final ServerPlayer player = getServerPlayer();
        if (player == null) return multiplayerQuesters;
        final double shareDistance = quest.getOptions().getShareDistance();
        if (shareDistance <= 0) return multiplayerQuesters;
        for (final ServerPlayer other : player.serverLevel().players()) {
            if (other.equals(player)) continue;
            if (player.distanceTo(other) > shareDistance) continue;
            final FabricQuester otherQuester = plugin.getQuester(other.getUUID());
            if (otherQuester.getCurrentQuests().containsKey(quest)) {
                multiplayerQuesters.add(otherQuester);
            }
        }
        return multiplayerQuesters;
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
        return false;
    }

    @Override
    public boolean isInRegion(String regionID) {
        // No WorldGuard equivalent on Fabric; always pass
        return true;
    }

    @Override
    public boolean canAcceptQuest(UUID npc) {
        // Accept all quests from any NPC
        return true;
    }

    @Override
    public boolean canAcceptCompletedQuest(UUID npc) {
        return true;
    }

    @Override
    public boolean canAcceptCompletedRedoableQuest(UUID npc) {
        return true;
    }

    @Override
    public int compareTo(Quester other) {
        if (other == null) return 1;
        return Integer.compare(this.questPoints, other.getQuestPoints());
    }

    private ServerPlayer getServerPlayer() {
        return plugin.getServer() != null ? plugin.getServer().getPlayerList().getPlayer(uuid) : null;
    }
}
