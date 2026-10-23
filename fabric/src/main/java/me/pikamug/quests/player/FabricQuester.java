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
import me.pikamug.quests.convo.misc.FabricQuestAcceptPrompt;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.FabricCustomObjective;
import me.pikamug.quests.quests.FabricQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.FabricObjective;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.FabricActionTimer;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricInventoryUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.FabricOfflinePlayer;
import net.minecraft.ChatFormatting;
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
import org.browsit.conversations.api.Conversations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FabricQuester implements Quester {

    private final FabricQuestsPlugin plugin;
    private final UUID id;
    private String questIdToTake;
    private String questIdToQuit;
    private String lastKnownName;
    private int questPoints = 0;
    private String compassTargetQuestId;
    private long lastNotifiedCondition = 0L;
    private final ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<>();
    private final Collection<Quest> completedQuests = Collections.synchronizedCollection(new ArrayList<>());
    private final ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Quest, QuestProgress> questProgress
            = new ConcurrentHashMap<Quest, QuestProgress>() {

        private static final long serialVersionUID = -3607612433053926086L;

        @Override
        public QuestProgress put(final @NotNull Quest key, final @NotNull QuestProgress val) {
            final QuestProgress data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public QuestProgress remove(final @NotNull Object key) {
            final QuestProgress data = super.remove(key);
            updateJournal();
            return data;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Quest, ? extends QuestProgress> m) {
            super.putAll(m);
            updateJournal();
        }
    };

    public FabricQuester(FabricQuestsPlugin plugin, UUID uuid) {
        this.plugin = plugin;
        this.id = uuid;
        if (getPlayer() != null) {
            this.lastKnownName = getPlayer().getName().getString();
        } else {
            this.lastKnownName = getOfflinePlayer().getName();
        }
        if (lastKnownName == null) {
            lastKnownName = String.valueOf(System.currentTimeMillis());
        }
    }

    @Override
    public int compareTo(final Quester quester) {
        return id.compareTo(quester.getUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FabricQuester)) return false;
        FabricQuester that = (FabricQuester) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public Quests getPlugin() {
        return plugin;
    }

    @Override
    public UUID getUUID() {
        return id;
    }

    @Override
    public String getQuestIdToTake() {
        return questIdToTake;
    }

    @Override
    public void setQuestIdToTake(final String questIdToTake) {
        this.questIdToTake = questIdToTake;
    }

    @Override
    public String getQuestIdToQuit() {
        return questIdToQuit;
    }

    @Override
    public void setQuestIdToQuit(final String questIdToQuit) {
        this.questIdToQuit = questIdToQuit;
    }

    @Override
    public String getLastKnownName() {
        return lastKnownName;
    }

    @Override
    public void setLastKnownName(final String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    @Override
    public int getQuestPoints() {
        return questPoints;
    }

    @Override
    public void setQuestPoints(final int questPoints) {
        this.questPoints = questPoints;
    }

    /**
     * Get compass target quest. Returns null if not set
     *
     * @return Quest or null
     */
    @Override
    public Quest getCompassTarget() {
        return compassTargetQuestId != null ? plugin.getQuestById(compassTargetQuestId) : null;
    }

    /**
     * Set compass target quest. Does not update in-game
     *
     * @param quest The target quest
     */
    @Override
    public void setCompassTarget(final Quest quest) {
        if (quest != null) {
            compassTargetQuestId = quest.getId();
        }
    }

    public ConcurrentHashMap<WrappedTask, Quest> getTimers() {
        return timers;
    }

    public void setTimers(final ConcurrentHashMap<WrappedTask, Quest> timers) {
        this.timers = timers;
    }

    public void removeTimer(final WrappedTask task) {
        this.timers.remove(task);
    }

    @Override
    public ConcurrentHashMap<Quest, Integer> getCurrentQuests() {
        final ConcurrentHashMap<Quest, Integer> map = new ConcurrentHashMap<>();
        for (final Map.Entry<Quest, Integer> cq : currentQuests.entrySet()) {
            final FabricQuest q = (FabricQuest) cq.getKey();
            map.put(q, cq.getValue());
        }
        return map;
    }

    @Override
    public void setCurrentQuests(final ConcurrentHashMap<Quest, Integer> currentQuests) {
        this.currentQuests = currentQuests;
    }

    public Collection<Quest> getCompletedQuests() {
        final Collection<Quest> set = ConcurrentHashMap.newKeySet();
        for (final Quest iq : completedQuests) {
            final FabricQuest q = (FabricQuest) iq;
            set.add(q);
        }
        return set;
    }

    @Override
    public void setCompletedQuests(final Collection<Quest> completedQuests) {
        final Collection<Quest> newCompletedQuests = ConcurrentHashMap.newKeySet();
        newCompletedQuests.addAll(completedQuests);
        this.completedQuests = newCompletedQuests;
    }

    @Override
    public ConcurrentHashMap<Quest, Long> getCompletedTimes() {
        return completedTimes;
    }

    @Override
    public void setCompletedTimes(final ConcurrentHashMap<Quest, Long> completedTimes) {
        this.completedTimes = completedTimes;
    }

    @Override
    public ConcurrentHashMap<Quest, Integer> getAmountsCompleted() {
        return amountsCompleted;
    }

    @Override
    public void setAmountsCompleted(final ConcurrentHashMap<Quest, Integer> amountsCompleted) {
        this.amountsCompleted = amountsCompleted;
    }

    public ConcurrentHashMap<Quest, QuestProgress> getQuestProgress() {
        return questProgress;
    }

    public void setQuestProgress(final ConcurrentHashMap<Quest, QuestProgress> questProgress) {
        this.questProgress = questProgress;
    }

    public @Nullable ServerPlayer getPlayer() {
        return FabricQuestsPlugin.getInstance().getServer().getPlayerList().getPlayer(id);
    }

    public FabricOfflinePlayer getOfflinePlayer() {
        return new FabricOfflinePlayer(FabricQuestsPlugin.getInstance().getServer(), id);
    }

    @Override
    public void sendMessage(final String message) {
        if (message == null || message.isEmpty()) return;
        final ServerPlayer player = getPlayer();
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    @Override
    public Stage getCurrentStage(final Quest quest) {
        if (currentQuests.containsKey(quest)) {
            return quest.getStage(currentQuests.get(quest));
        }
        return null;
    }

    /**
     * Get quest progress for given quest, or default values if not found
     *
     * @param quest The quest to check
     * @return Existing or current progress, or default
     */
    @Override
    public QuestProgress getQuestProgressOrDefault(final Quest quest) {
        if (questProgress.get(quest) != null) {
            return questProgress.get(quest);
        }
        if (currentQuests.get(quest) != null) {
            addEmptiesFor(quest, currentQuests.get(quest));
        }
        return new FabricQuestProgress(this);
    }

    @Override
    public boolean hasJournal() {
        return getJournal() != null;
    }

    public ItemStack getJournal() {
        final ServerPlayer player = getPlayer();
        if (player == null) return null;
        for (final net.minecraft.world.item.ItemStack is : player.getInventory().getNonEquipmentItems()) {
            if (me.pikamug.quests.util.FabricItemUtil.isJournal(is)) {
                return is;
            }
        }
        return null;
    }

    @Override
    public int getJournalIndex() {
        final ServerPlayer player = getPlayer();
        if (player == null) {
            return -1;
        }
        final List<net.minecraft.world.item.ItemStack> items = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size(); i++) {
            if (FabricItemUtil.isJournal(items.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void updateJournal() {
        final ServerPlayer player = getPlayer();
        if (player == null) {
            return;
        }
        final int index = getJournalIndex();
        if (index != -1) {
            final me.pikamug.quests.item.FabricQuestJournal journal
                    = new me.pikamug.quests.item.FabricQuestJournal(plugin, this);
            player.getInventory().setItem(index, journal.toItemStack());
        }
    }

    /**
     * Check if quest is available and, if so, ask Quester if they would like to start it<p>
     *
     * @param quest The quest to check and then offer
     * @param giveReason Whether to inform Quester of unavailability
     * @return true if successful
     */
    @Override
    public boolean offerQuest(final Quest quest, final boolean giveReason) {
        if (quest == null) {
            return false;
        }
        if (plugin.getConfigSettings().getMaxQuests() > 0
                && currentQuests.size() >= plugin.getConfigSettings().getMaxQuests()
                && !quest.getOptions().canOverrideMaxQuests()) {
            if (giveReason) sendMessage(FabricLang.get(getPlayer(), "questMaxAllowed")
                    .replace("<number>", String.valueOf(plugin.getConfigSettings().getMaxQuests())));
            return false;
        }
        if (canAcceptOffer(quest, giveReason)) {
            final ServerPlayer player = getPlayer();
            if (player != null) {
                if (!Conversations.getConversationOf(player.getUUID()).isPresent()) {
                    setQuestIdToTake(quest.getId());
                    final String s = ChatFormatting.GOLD + FabricConfigUtil.parseString(FabricLang.get("questObjectivesTitle")
                            + "\n" + ChatFormatting.RESET + quest.getDescription(), quest, getPlayer());
                    for (final String msg : s.split("<br>")) {
                        sendMessage(msg);
                    }
                    if (!plugin.getConfigSettings().canConfirmAccept()) {
                        takeQuest(quest, false);
                    } else {
                        new FabricQuestAcceptPrompt(player.getUUID(), plugin).start();
                    }
                    return true;
                } else {
                    sendMessage(ChatFormatting.YELLOW + FabricLang.get(player, "alreadyConversing"));
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
        FabricQuest fabricQuest = (FabricQuest) quest;
        if (!plugin.getConfigSettings().canAllowCommandsForNpcQuests() && fabricQuest.getNpcStart() != null) {
            final UUID uuid = fabricQuest.getNpcStart();
            final ServerPlayer player = getPlayer();
            final ServerPlayer npc = plugin.getServer() == null ? null
                    : plugin.getServer().getPlayerList().getPlayer(uuid);
            if (player != null && npc != null && player.level().dimension()
                    .equals(npc.level().dimension())) {
                if (player.blockPosition().distSqr(npc.blockPosition()) > 36) {
                    if (giveReason) {
                        final String msg = FabricLang.get(getPlayer(), "mustSpeakTo").replace("<npc>",
                                plugin.getDependencies().getNpcName(uuid));
                        sendMessage("§e" + msg);
                    }
                    return false;
                }
            }
        }
        if (currentQuests.containsKey(quest)) {
            if (giveReason) sendMessage(FabricLang.get(getPlayer(), "questAlreadyOn"));
            return false;
        }
        if (completedQuests.contains(quest) && !quest.getPlanner().hasRepeat()) {
            if (giveReason) sendMessage(FabricLang.get(getPlayer(), "questAlreadyCompleted"));
            return false;
        }
        if (getRemainingCooldown(quest) > 0 && completedQuests.contains(quest)
                && !quest.getPlanner().getOverride()) {
            if (giveReason) {
                final String msg = FabricLang.get(getPlayer(), "questTooEarly").replace("<quest>",
                        quest.getName()).replace("<time>", FabricMiscUtil
                        .getTime(getRemainingCooldown(quest)));
                sendMessage(msg);
            }
            return false;
        }
        if (quest.getRegionStart() != null && !quest.isInRegionStart(this)) {
            if (giveReason) {
                sendMessage(FabricLang.get(getPlayer(), "questInvalidLocation").replace("<quest>", quest.getName()));
            }
            return false;
        }
        if (quest instanceof FabricQuest) {
            FabricQuest fabricQuest = (FabricQuest) quest;
            if (fabricQuest.getBlockStart() != null) {
            if (giveReason) {
                sendMessage(FabricLang.get(getPlayer(), "noCommandStart").replace("<quest>", quest.getName()));
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
                    String early = FabricLang.get(getPlayer(), "plnTooEarly");
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
                    String late = FabricLang.get(getPlayer(), "plnTooLate");
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
                        final String early = FabricLang.get(getPlayer(), "plnTooEarly").replace("<quest>", quest.getName())
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
                        final String early = FabricLang.get(getPlayer(), "plnTooEarly").replace("<quest>", quest.getName())
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
        if (getPlayer() != null) {
            if (!isOnTime(quest, true)) {
                return;
            }
        }
        if (!ignoreRequirements && !quest.testRequirements(this)) {
            sendMessage(FabricLang.get(getPlayer(), "doesNotMeetReqs"));
            return;
        }
        currentQuests.put(quest, 0);
        addEmptiesFor(quest, 0);
        final ServerPlayer player = getPlayer();
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
                    if (doRemove && requirements.getItems().get(i) instanceof ItemStack) {
                        ItemStack stack = (ItemStack) requirements.getItems().get(i);
                        ok &= FabricInventoryUtil.removeItem(player, stack);
                    }
                }
            }
            if (!ok) {
                for (int i = 0; i < size; i++) {
                    inventory.setItem(i, original[i]);
                }
                sendMessage(FabricLang.get(getPlayer(), "requirementsItemFail"));
                hardQuit(quest);
                return;
            }
        }
        final boolean isGlobalQuest = quest.getOptions().canGiveGloballyAtLogin();
        if (player != null && (!isGlobalQuest || quest.getOptions().canInformOnStart())) {
            final String accepted = FabricLang.get(getPlayer(), "questAccepted").replace("<quest>", quest.getName());
            sendMessage("§a" + accepted);
            sendMessage("");
            if (plugin.getConfigSettings().canShowQuestTitles()) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("§e" + quest.getName())));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(
                        "§6" + FabricLang.get(getPlayer(), "quest") + " " + FabricLang.get(getPlayer(), "accepted"))));
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            }
        }
        final Stage stage = quest.getStage(0);
        if (player != null) {
            sendMessage("§6" + FabricLang.get(getPlayer(), "objectives").replace("<quest>", quest.getName()));
            showCurrentObjectives(quest, this, false);
            if (stage != null && stage.getStartMessage() != null) {
                sendMessage(FabricMiscUtil.parseString(stage.getStartMessage()));
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
            sendMessage(FabricLang.get(getPlayer(), "questQuitDisabled"));
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
        sendMessage(FabricLang.get(getPlayer(), "questQuit").replace("<quest>", quest.getName()));
        saveData();
        updateJournal();
    }

    @Override
    public void listQuests(Quester quester, int page) {
        if (quester == null) return;
        sendMessage("§6--- Your Quests ---");
        final Collection<Quest> quests = quester.getCurrentQuests().keySet();
        if (quests.isEmpty()) {
            sendMessage(FabricLang.get(getPlayer(), "noCurrentQuest")
                    .replace("<player>", getPlayer() == null ? "" : getPlayer().getName().getString()));
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
            reqs.add(FabricLang.get(getPlayer(), "reqBreakBlock"));
        }
        // Blocks to place
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            reqs.add(FabricLang.get(getPlayer(), "reqPlaceBlock"));
        }
        // Items to craft
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            reqs.add(FabricLang.get(getPlayer(), "reqCraftItem"));
        }
        // Mobs to kill
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            final int amt = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            reqs.add(FabricLang.get(getPlayer(), "reqKillMob").replace("<amount>", String.valueOf(amt)));
        }
        // NPCs to interact
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            reqs.add(FabricLang.get(getPlayer(), "reqTalkToNpc"));
        }
        // Players to kill
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            reqs.add(FabricLang.get(getPlayer(), "reqKillPlayer").replace("<amount>", String.valueOf(stage.getPlayersToKill())));
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
        final ServerPlayer player = getPlayer();
        final LinkedList<String> overrides = stage.getObjectiveOverrides() != null
                ? stage.getObjectiveOverrides() : new LinkedList<>();
        int objIndex = -1;

        // Break blocks
        for (int i = 0; i < stage.getBlocksToBreak().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getBlocksToBreak().get(i);
            if (goalObj == null) continue;
            final int goal = getBlockAmount(stage.getBlocksToBreakAmounts(), i);
            final int current = (progress.getBlocksBroken().size() > i) ? progress.getBlocksBroken().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "break", current, goal)
                    .replace("<item>", blockName(goalObj))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.BREAK_BLOCK, msg, current, goal));
        }

        // Place blocks
        for (int i = 0; i < stage.getBlocksToPlace().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getBlocksToPlace().get(i);
            if (goalObj == null) continue;
            final int goal = getBlockAmount(stage.getBlocksToPlaceAmounts(), i);
            final int current = (progress.getBlocksPlaced().size() > i) ? progress.getBlocksPlaced().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "place", current, goal)
                    .replace("<item>", blockName(goalObj))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.PLACE_BLOCK, msg, current, goal));
        }

        // Damage blocks
        for (int i = 0; i < stage.getBlocksToDamage().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getBlocksToDamage().get(i);
            if (goalObj == null) continue;
            final int goal = 1;
            final int current = (progress.getBlocksDamaged().size() > i) ? progress.getBlocksDamaged().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "damage", current, goal)
                    .replace("<item>", blockName(goalObj))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.DAMAGE_BLOCK, msg, current, goal));
        }

        // Use blocks
        for (int i = 0; i < stage.getBlocksToUse().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getBlocksToUse().get(i);
            if (goalObj == null) continue;
            final int goal = getBlockAmount(stage.getBlocksToUseAmounts(), i);
            final int current = (progress.getBlocksUsed().size() > i) ? progress.getBlocksUsed().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "use", current, goal)
                    .replace("<item>", blockName(goalObj))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.USE_BLOCK, msg, current, goal));
        }

        // Items crafted
        for (int i = 0; i < stage.getItemsToCraft().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getItemsToCraft().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            ItemStack cGoal = (ItemStack) goalObj;
            final int goal = Math.max(1, cGoal.getCount());
            final int current = (progress.getItemsCrafted().size() > i) ? progress.getItemsCrafted().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "craftItem", current, goal)
                    .replace("<item>", FabricItemUtil.getName(cGoal))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.CRAFT_ITEM, msg, current, goal));
        }

        // Items smelted
        for (int i = 0; i < stage.getItemsToSmelt().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getItemsToSmelt().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            ItemStack sGoal = (ItemStack) goalObj;
            final int goal = Math.max(1, sGoal.getCount());
            final int current = (progress.getItemsSmelted().size() > i) ? progress.getItemsSmelted().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "smeltItem", current, goal)
                    .replace("<item>", FabricItemUtil.getName(sGoal))
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.SMELT_ITEM, msg, current, goal));
        }

        // Items enchanted
        for (int i = 0; i < stage.getItemsToEnchant().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getItemsToEnchant().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            ItemStack eGoal = (ItemStack) goalObj;
            final int goal = Math.max(1, eGoal.getCount());
            final int current = (progress.getItemsEnchanted().size() > i) ? progress.getItemsEnchanted().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "enchItem", current, goal);
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
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.ENCHANT_ITEM, msg, current, goal));
        }

        // Items brewed
        for (int i = 0; i < stage.getItemsToBrew().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getItemsToBrew().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            ItemStack bGoal = (ItemStack) goalObj;
            final int goal = Math.max(1, bGoal.getCount());
            final int current = (progress.getItemsBrewed().size() > i) ? progress.getItemsBrewed().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "brewItem", current, goal)
                    .replace("<item>", FabricItemUtil.getName(bGoal))
                    .replace(" <level>", "")
                    .replace("<level>", "")
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.BREW_ITEM, msg, current, goal));
        }

        // Items consumed
        for (int i = 0; i < stage.getItemsToConsume().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getItemsToConsume().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            ItemStack cnGoal = (ItemStack) goalObj;
            final int goal = Math.max(1, cnGoal.getCount());
            final int current = (progress.getItemsConsumed().size() > i) ? progress.getItemsConsumed().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "consumeItem", current, goal)
                    .replace("<item>", FabricItemUtil.getName(cnGoal))
                    .replace(" <level>", "")
                    .replace("<level>", "")
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.CONSUME_ITEM, msg, current, goal));
        }

        // Mobs killed
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            objIndex++;
            final int goal = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            final int current = (progress.getMobNumKilled().size() > i) ? progress.getMobNumKilled().get(i) : 0;
            final String mobName = FabricMiscUtil.snakeCaseToUpperCamelCase(
                    stage.getMobsToKill().get(i).toString());
            String msg;
            if (stage.getLocationsToKillWithin().isEmpty()) {
                msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "kill", current, goal);
            } else {
                final String killName = stage.getKillNames() != null && stage.getKillNames().size() > i
                        && stage.getKillNames().get(i) != null ? stage.getKillNames().get(i) : "?";
                msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "killAtLocation", current, goal)
                        .replace("<location>", killName);
            }
            msg = msg.replace("<mob>", mobName).replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.KILL_MOB, msg, current, goal));
        }

        // Mobs tamed
        for (int i = 0; i < stage.getMobsToTame().size(); i++) {
            objIndex++;
            final int goal = (stage.getMobNumToTame() != null && stage.getMobNumToTame().size() > i)
                    ? stage.getMobNumToTame().get(i) : 1;
            final int current = (progress.getMobsTamed().size() > i) ? progress.getMobsTamed().get(i) : 0;
            final Object tameObj = stage.getMobsToTame().get(i);
            final String mobName = tameObj != null
                    ? FabricMiscUtil.snakeCaseToUpperCamelCase(tameObj.toString()) : "?";
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "tame", current, goal)
                    .replace("<mob>", mobName)
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.TAME_MOB, msg, current, goal));
        }

        // Fish caught
        if (stage.getFishToCatch() != null && stage.getFishToCatch() > 0) {
            objIndex++;
            final int goal = stage.getFishToCatch();
            final int current = progress.getFishCaught();
            final String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "catchFish", current, goal);
            objs.add(new FabricObjective(ObjectiveType.CATCH_FISH, msg.trim(), current, goal));
        }

        // Cows milked
        if (stage.getCowsToMilk() != null && stage.getCowsToMilk() > 0) {
            objIndex++;
            final int goal = stage.getCowsToMilk();
            final int current = progress.getCowsMilked();
            final String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "milkCow", current, goal);
            objs.add(new FabricObjective(ObjectiveType.MILK_COW, msg.trim(), current, goal));
        }

        // Sheep sheared
        for (int i = 0; i < stage.getSheepToShear().size(); i++) {
            objIndex++;
            final int goal = (stage.getSheepNumToShear() != null && stage.getSheepNumToShear().size() > i)
                    ? stage.getSheepNumToShear().get(i) : 1;
            final int current = (progress.getSheepSheared().size() > i) ? progress.getSheepSheared().get(i) : 0;
            final Object dyeObj = stage.getSheepToShear().get(i);
            final String color = dyeObj != null ? FabricMiscUtil.snakeCaseToUpperCamelCase(dyeObj.toString()) : "?";
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "shearSheep", current, goal)
                    .replace("<color>", color)
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.SHEAR_SHEEP, msg, current, goal));
        }

        // NPCs interacted
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            objIndex++;
            final boolean said = (progress.getNpcsInteracted().size() > i) && progress.getNpcsInteracted().get(i);
            final UUID npcUuid = stage.getNpcsToInteract().get(i);
            final String npcName = npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?";
            final String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "talkTo", said ? 1 : 0, 1)
                    .replace("<npc>", npcName);
            objs.add(new FabricObjective(ObjectiveType.TALK_TO_NPC, msg, said ? 1 : 0, 1));
        }

        // NPCs killed
        for (int i = 0; i < stage.getNpcsToKill().size(); i++) {
            objIndex++;
            final int goal = (stage.getNpcNumToKill() != null && stage.getNpcNumToKill().size() > i)
                    ? stage.getNpcNumToKill().get(i) : 1;
            final int current = (progress.getNpcsNumKilled().size() > i) ? progress.getNpcsNumKilled().get(i) : 0;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "kill", current, goal);
            final UUID npcUuid = stage.getNpcsToKill().get(i);
            final String npcName = npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?";
            if (msg.contains("<mob>")) {
                msg = msg.replace("<mob>", npcName);
            } else {
                msg += npcName;
            }
            objs.add(new FabricObjective(ObjectiveType.KILL_NPC, msg, current, goal));
        }

        // Players killed
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            objIndex++;
            final int goal = stage.getPlayersToKill();
            final int current = progress.getPlayersKilled();
            final String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "killPlayer", current, goal);
            objs.add(new FabricObjective(ObjectiveType.KILL_PLAYER, msg, current, goal));
        }

        // Passwords said
        for (int i = 0; i < stage.getPasswordDisplays().size(); i++) {
            objIndex++;
            final boolean said = (progress.getPasswordsSaid().size() > i) && progress.getPasswordsSaid().get(i);
            final String display = stage.getPasswordDisplays().get(i);
            final String msg = display != null ? display : "";
            objs.add(new FabricObjective(ObjectiveType.PASSWORD, msg, said ? 1 : 0, 1));
        }

        // Items delivered
        for (int i = 0; i < stage.getItemsToDeliver().size(); i++) {
            objIndex++;
            final Object goalObj = stage.getItemsToDeliver().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            ItemStack goal = (ItemStack) goalObj;
            final int goalAmount = Math.max(1, goal.getCount());
            final int current = (progress.getItemsDelivered().size() > i) ? progress.getItemsDelivered().get(i) : 0;
            final UUID npcUuid = (stage.getItemDeliveryTargets().size() > i)
                    ? stage.getItemDeliveryTargets().get(i) : null;
            String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "deliver", current, goalAmount)
                    .replace("<item>", FabricItemUtil.getName(goal))
                    .replace("<npc>", npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?")
                    .replaceAll("\\s{2,}", " ").trim();
            objs.add(new FabricObjective(ObjectiveType.DELIVER_ITEM, msg, current, goalAmount));
        }

        // Custom objectives
        for (int i = 0; i < stage.getCustomObjectives().size(); i++) {
            objIndex++;
            final CustomObjective co = stage.getCustomObjectives().get(i);
            if (co == null) continue;
            final int goal = (stage.getCustomObjectiveCounts() != null && stage.getCustomObjectiveCounts().size() > i)
                    ? stage.getCustomObjectiveCounts().get(i) : 1;
            final int current = (progress.getCustomObjectiveCounts().size() > i)
                    ? progress.getCustomObjectiveCounts().get(i) : 0;
            String msg;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                final String s = overrides.get(objIndex);
                msg = FabricMiscUtil.parseString((s != null ? s : "")
                        .replace("<count>", current + "/" + goal)
                        .replace("%count%", current + "/" + goal));
            } else {
                msg = co.getDisplay() != null ? co.getDisplay() : "";
            }
            if (co instanceof FabricCustomObjective) {
                FabricCustomObjective fab = (FabricCustomObjective) co;
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

        // Locations to reach
        for (int i = 0; i < stage.getLocationsToReach().size(); i++) {
            objIndex++;
            final boolean reached = (progress.getLocationsReached().size() > i) && progress.getLocationsReached().get(i);
            final String locationName = (stage.getLocationNames() != null && stage.getLocationNames().size() > i
                    && stage.getLocationNames().get(i) != null) ? stage.getLocationNames().get(i) : "?";
            final String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "goTo", reached ? 1 : 0, 1)
                    .replace("<location>", locationName);
            objs.add(new FabricObjective(ObjectiveType.REACH_LOCATION, msg, reached ? 1 : 0, 1));
        }

        return objs;
    }

    private String blockName(final Object goalObj) {
        if (goalObj instanceof ItemStack) {
            ItemStack stack = (ItemStack) goalObj;
            return FabricItemUtil.getName(stack);
        }
        final String raw = goalObj.toString();
        return FabricMiscUtil.snakeCaseToUpperCamelCase(
                raw.indexOf(':') != -1 ? raw.substring(raw.indexOf(':') + 1) : raw);
    }

    private String objectiveLine(final ServerPlayer player, final boolean ignoreOverrides,
            final LinkedList<String> overrides, final int objIndex, final String langKey, final int current,
            final int goal) {
        if (!ignoreOverrides && overrides != null && objIndex < overrides.size()) {
            final String s = overrides.get(objIndex);
            return FabricMiscUtil.parseString((s != null ? s : "").replace("<count>", current + "/" + goal)
                    .replace("%count%", current + "/" + goal));
        }
        return FabricLang.get(player, langKey)
                .replace("<count>", current + "/" + goal)
                .replace("%count%", current + "/" + goal)
                .replace("<goal>", current + "/" + goal);
    }

    private int listProgress(final LinkedList<Integer> list, final int index) {
        if (list != null && list.size() > index) return list.get(index);
        return 0;
    }

    private int listGoal(final LinkedList<Integer> amounts, final int index) {
        if (amounts != null && amounts.size() > index) return amounts.get(index);
        return 1;
    }

    private int itemGoal(final LinkedList<?> items, final int index) {
        if (items == null || items.size() <= index || !(items.get(index) instanceof ItemStack)) return 1;
        ItemStack is = (ItemStack) items.get(index);
        return Math.max(1, is.getCount());
    }

    private int objectiveGoal(final Stage stage, final ObjectiveType type, final int index) {
        switch (type) {
            case BREAK_BLOCK: return getBlockAmount(stage.getBlocksToBreakAmounts(), index);
            case PLACE_BLOCK: return getBlockAmount(stage.getBlocksToPlaceAmounts(), index);
            case USE_BLOCK: return getBlockAmount(stage.getBlocksToUseAmounts(), index);
            case DAMAGE_BLOCK: return 1;
            case CRAFT_ITEM: return itemGoal(stage.getItemsToCraft(), index);
            case SMELT_ITEM: return itemGoal(stage.getItemsToSmelt(), index);
            case ENCHANT_ITEM: return itemGoal(stage.getItemsToEnchant(), index);
            case BREW_ITEM: return itemGoal(stage.getItemsToBrew(), index);
            case CONSUME_ITEM: return itemGoal(stage.getItemsToConsume(), index);
            case DELIVER_ITEM: return itemGoal(stage.getItemsToDeliver(), index);
            case KILL_MOB: return listGoal(stage.getMobNumToKill(), index);
            case TAME_MOB: return listGoal(stage.getMobNumToTame(), index);
            case SHEAR_SHEEP: return listGoal(stage.getSheepNumToShear(), index);
            case KILL_NPC: return listGoal(stage.getNpcNumToKill(), index);
            case KILL_PLAYER: return stage.getPlayersToKill() != null ? stage.getPlayersToKill() : 0;
            case CATCH_FISH: return stage.getFishToCatch() != null ? stage.getFishToCatch() : 0;
            case MILK_COW: return stage.getCowsToMilk() != null ? stage.getCowsToMilk() : 0;
            default: return 1;
        }
    }

    private int objectiveProgress(final QuestProgress progress, final ObjectiveType type, final int index) {
        switch (type) {
            case BREAK_BLOCK: return listProgress(progress.getBlocksBroken(), index);
            case PLACE_BLOCK: return listProgress(progress.getBlocksPlaced(), index);
            case USE_BLOCK: return listProgress(progress.getBlocksUsed(), index);
            case DAMAGE_BLOCK: return listProgress(progress.getBlocksDamaged(), index);
            case CRAFT_ITEM: return listProgress(progress.getItemsCrafted(), index);
            case SMELT_ITEM: return listProgress(progress.getItemsSmelted(), index);
            case ENCHANT_ITEM: return listProgress(progress.getItemsEnchanted(), index);
            case BREW_ITEM: return listProgress(progress.getItemsBrewed(), index);
            case CONSUME_ITEM: return listProgress(progress.getItemsConsumed(), index);
            case DELIVER_ITEM: return listProgress(progress.getItemsDelivered(), index);
            case KILL_MOB: return listProgress(progress.getMobNumKilled(), index);
            case TAME_MOB: return listProgress(progress.getMobsTamed(), index);
            case SHEAR_SHEEP: return listProgress(progress.getSheepSheared(), index);
            case KILL_NPC: return listProgress(progress.getNpcsNumKilled(), index);
            case KILL_PLAYER: return progress.getPlayersKilled();
            case CATCH_FISH: return progress.getFishCaught();
            case MILK_COW: return progress.getCowsMilked();
            case TALK_TO_NPC: return (progress.getNpcsInteracted().size() > index && progress.getNpcsInteracted().get(index)) ? 1 : 0;
            case PASSWORD: return (progress.getPasswordsSaid().size() > index && progress.getPasswordsSaid().get(index)) ? 1 : 0;
            case REACH_LOCATION: return (progress.getLocationsReached().size() > index && progress.getLocationsReached().get(index)) ? 1 : 0;
            case CUSTOM: return listProgress(progress.getCustomObjectiveCounts(), index);
            default: return 0;
        }
    }

    private String formatCompletedObjective(final ServerPlayer player, final String langKey, final int goal) {
        final String line = FabricLang.get(player, langKey)
                .replace("<count>", goal + "/" + goal)
                .replace("%count%", goal + "/" + goal);
        return "§a(" + FabricLang.get(player, "completed") + ") " + line;
    }

    /**
     * Announce that an objective has reached its goal, mirroring the Bukkit module. Fires only when the objective
     * is actually complete ({@code progress >= goal}) so callers may invoke it after every increment; the
     * crossing must have just occurred or the message would be re-sent on later increments.
     */
    public void finishObjective(final Quest quest, final ObjectiveType type, final int index) {
        if (quest == null || type == null || !currentQuests.containsKey(quest)) return;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return;
        final QuestProgress progress = getQuestProgress(quest);
        if (progress == null) return;
        final int goal = objectiveGoal(stage, type, index);
        if (goal <= 0) return;
        if (objectiveProgress(progress, type, index) < goal) return;
        final ServerPlayer player = getPlayer();

        final LinkedList<String> overrides = stage.getObjectiveOverrides() != null
                ? stage.getObjectiveOverrides() : new LinkedList<>();
        if (!overrides.isEmpty()) {
            for (final String s : overrides) {
                if (s == null) continue;
                final String count = goal + "/" + goal;
                final String message = "§a(" + FabricLang.get(player, "completed") + ") "
                        + FabricMiscUtil.parseString(s.replace("<count>", count).replace("%count%", count));
                sendMessage(message);
            }
            return;
        }

        switch (type) {
            case BREAK_BLOCK:
                if (index >= stage.getBlocksToBreak().size()) return;
                sendMessage(formatCompletedObjective(player, "break", goal)
                        .replace("<item>", blockName(stage.getBlocksToBreak().get(index))));
                break;
            case PLACE_BLOCK:
                if (index >= stage.getBlocksToPlace().size()) return;
                sendMessage(formatCompletedObjective(player, "place", goal)
                        .replace("<item>", blockName(stage.getBlocksToPlace().get(index))));
                break;
            case DAMAGE_BLOCK:
                if (index >= stage.getBlocksToDamage().size()) return;
                sendMessage(formatCompletedObjective(player, "damage", goal)
                        .replace("<item>", blockName(stage.getBlocksToDamage().get(index))));
                break;
            case USE_BLOCK:
                if (index >= stage.getBlocksToUse().size()) return;
                sendMessage(formatCompletedObjective(player, "use", goal)
                        .replace("<item>", blockName(stage.getBlocksToUse().get(index))));
                break;
            case CRAFT_ITEM:
                if (index >= stage.getItemsToCraft().size()
                        || !(stage.getItemsToCraft().get(index) instanceof ItemStack)) return;
                    ItemStack craftGoal = (ItemStack) stage.getItemsToCraft().get(index);
                sendMessage(formatCompletedObjective(player, "craftItem", goal)
                        .replace("<item>", FabricItemUtil.getName(craftGoal)));
                break;
            case SMELT_ITEM:
                if (index >= stage.getItemsToSmelt().size()
                        || !(stage.getItemsToSmelt().get(index) instanceof ItemStack)) return;
                    ItemStack smeltGoal = (ItemStack) stage.getItemsToSmelt().get(index);
                sendMessage(formatCompletedObjective(player, "smeltItem", goal)
                        .replace("<item>", FabricItemUtil.getName(smeltGoal)));
                break;
            case ENCHANT_ITEM:
                if (index >= stage.getItemsToEnchant().size()
                        || !(stage.getItemsToEnchant().get(index) instanceof ItemStack)) return;
                    ItemStack enchGoal = (ItemStack) stage.getItemsToEnchant().get(index);
                String enchMsg = formatCompletedObjective(player, "enchItem", goal);
                final ItemEnchantments ench = enchGoal.get(DataComponents.ENCHANTMENTS);
                if (ench != null && !ench.isEmpty()) {
                    final var first = ench.entrySet().iterator().next();
                    final Holder<Enchantment> holder = first.getKey();
                    String enchName = holder.getRegisteredName();
                    if (enchName != null && enchName.indexOf(':') != -1) {
                        enchName = enchName.substring(enchName.indexOf(':') + 1);
                    }
                    enchMsg = enchMsg.replace("<enchantment>", FabricMiscUtil.snakeCaseToUpperCamelCase(enchName))
                            .replace("<level>", toRoman(first.getIntValue()));
                } else {
                    enchMsg = enchMsg.replace("<enchantment>", "").replace("<level>", "");
                }
                sendMessage(enchMsg.replace("<item>", FabricItemUtil.getName(enchGoal))
                        .replaceAll("\\s{2,}", " ").trim());
                break;
            case BREW_ITEM:
                if (index >= stage.getItemsToBrew().size()
                        || !(stage.getItemsToBrew().get(index) instanceof ItemStack)) return;
                    ItemStack brewGoal = (ItemStack) stage.getItemsToBrew().get(index);
                sendMessage(formatCompletedObjective(player, "brewItem", goal)
                        .replace("<item>", FabricItemUtil.getName(brewGoal))
                        .replace(" <level>", "")
                        .replace("<level>", "")
                        .replaceAll("\\s{2,}", " ").trim());
                break;
            case CONSUME_ITEM:
                if (index >= stage.getItemsToConsume().size()
                        || !(stage.getItemsToConsume().get(index) instanceof ItemStack)) return;
                    ItemStack consumeGoal = (ItemStack) stage.getItemsToConsume().get(index);
                sendMessage(formatCompletedObjective(player, "consumeItem", goal)
                        .replace("<item>", FabricItemUtil.getName(consumeGoal))
                        .replace(" <level>", "")
                        .replace("<level>", "")
                        .replaceAll("\\s{2,}", " ").trim());
                break;
            case DELIVER_ITEM:
                if (index >= stage.getItemsToDeliver().size()
                        || !(stage.getItemsToDeliver().get(index) instanceof ItemStack)) return;
                    ItemStack deliverGoal = (ItemStack) stage.getItemsToDeliver().get(index);
                final UUID npcUuid = (stage.getItemDeliveryTargets().size() > index)
                        ? stage.getItemDeliveryTargets().get(index) : null;
                sendMessage(formatCompletedObjective(player, "deliver", goal)
                        .replace("<item>", FabricItemUtil.getName(deliverGoal))
                        .replace("<npc>", npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?"));
                break;
            case KILL_MOB:
                if (index >= stage.getMobsToKill().size()) return;
                sendMessage(formatCompletedObjective(player, "kill", goal)
                        .replace("<mob>", FabricMiscUtil.snakeCaseToUpperCamelCase(
                                stage.getMobsToKill().get(index).toString())));
                break;
            case KILL_NPC:
                if (index >= stage.getNpcsToKill().size()) return;
                final UUID npcKillUuid = stage.getNpcsToKill().get(index);
                sendMessage(formatCompletedObjective(player, "kill", goal)
                        .replace("<mob>", npcKillUuid != null
                                ? plugin.getDependencies().getNpcName(npcKillUuid) : "?"));
                break;
            case KILL_PLAYER:
                sendMessage(formatCompletedObjective(player, "killPlayer", goal));
                break;
            case TALK_TO_NPC:
                if (index >= stage.getNpcsToInteract().size()) return;
                final UUID npcInteractUuid = stage.getNpcsToInteract().get(index);
                sendMessage(formatCompletedObjective(player, "talkTo", goal)
                        .replace("<npc>", npcInteractUuid != null
                                ? plugin.getDependencies().getNpcName(npcInteractUuid) : "?"));
                break;
            case TAME_MOB:
                if (index >= stage.getMobsToTame().size()) return;
                sendMessage(formatCompletedObjective(player, "tame", goal)
                        .replace("<mob>", FabricMiscUtil.snakeCaseToUpperCamelCase(
                                stage.getMobsToTame().get(index).toString())));
                break;
            case CATCH_FISH:
                sendMessage(formatCompletedObjective(player, "catchFish", goal));
                break;
            case MILK_COW:
                sendMessage(formatCompletedObjective(player, "milkCow", goal));
                break;
            case SHEAR_SHEEP:
                if (index >= stage.getSheepToShear().size()) return;
                sendMessage(formatCompletedObjective(player, "shearSheep", goal)
                        .replace("<color>", FabricMiscUtil.snakeCaseToUpperCamelCase(
                                stage.getSheepToShear().get(index).toString())));
                break;
            case REACH_LOCATION:
                if (index >= stage.getLocationsToReach().size()) return;
                final String locationName = (stage.getLocationNames() != null && stage.getLocationNames().size() > index
                        && stage.getLocationNames().get(index) != null) ? stage.getLocationNames().get(index) : "?";
                sendMessage(formatCompletedObjective(player, "goTo", goal)
                        .replace("<location>", locationName));
                break;
            case PASSWORD:
                if (index >= stage.getPasswordDisplays().size()) return;
                sendMessage("§a(" + FabricLang.get(player, "completed") + ") "
                        + (stage.getPasswordDisplays().get(index) != null
                                ? stage.getPasswordDisplays().get(index) : ""));
                break;
            case CUSTOM:
                if (index >= stage.getCustomObjectives().size()) return;
                final CustomObjective co = stage.getCustomObjectives().get(index);
                if (co == null) return;
                String custom = co.getDisplay() != null ? co.getDisplay() : "";
            if (co instanceof FabricCustomObjective) {
                FabricCustomObjective fab = (FabricCustomObjective) co;
                    for (final Map.Entry<String, Object> prompt : fab.getData()) {
                        final String replacement = "%" + prompt.getKey() + "%";
                        if (custom.contains(replacement)) {
                            for (final Map.Entry<String, Object> e : stage.getCustomObjectiveData()) {
                                if (e.getKey().equals(prompt.getKey())) {
                                    custom = custom.replace(replacement, String.valueOf(e.getValue()));
                                }
                            }
                        }
                    }
                }
                if (co.canShowCount()) {
                    custom = custom.replace("%count%", goal + "/" + goal);
                }
                sendMessage(FabricMiscUtil.parseString("§a(" + FabricLang.get(player, "completed") + ") "
                        + custom.trim()));
                break;
            default:
                break;
        }
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
        for (final Objective obj : objectives) {
            final String color = obj.getProgress() < obj.getGoal() ? "§a" : "§7";
            sendMessage("- " + color + obj.getMessage());
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
            if (progress.getItemsCrafted().get(i) < itemGoal(stage.getItemsToCraft(), i)) return false;
        }

        // Items smelted
        for (int i = 0; i < stage.getItemsToSmelt().size(); i++) {
            if (progress.getItemsSmelted().size() <= i) return false;
            if (progress.getItemsSmelted().get(i) < itemGoal(stage.getItemsToSmelt(), i)) return false;
        }

        // Items enchanted
        for (int i = 0; i < stage.getItemsToEnchant().size(); i++) {
            if (progress.getItemsEnchanted().size() <= i) return false;
            if (progress.getItemsEnchanted().get(i) < itemGoal(stage.getItemsToEnchant(), i)) return false;
        }

        // Items brewed
        for (int i = 0; i < stage.getItemsToBrew().size(); i++) {
            if (progress.getItemsBrewed().size() <= i) return false;
            if (progress.getItemsBrewed().get(i) < itemGoal(stage.getItemsToBrew(), i)) return false;
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
            if (progress.getItemsConsumed().get(i) < itemGoal(stage.getItemsToConsume(), i)) return false;
        }

        // Items delivered
        for (int i = 0; i < stage.getItemsToDeliver().size(); i++) {
            if (progress.getItemsDelivered().size() <= i) return false;
            final Object goal = stage.getItemsToDeliver().get(i);
            final int goalAmount = (goal instanceof ItemStack) ? Math.max(1, ((ItemStack) goal).getCount()) : 1;
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
        final QuestProgress data = new FabricQuestProgress();
        data.setDoJournalUpdate(false);
        if (quest == null) {
            FabricQuestsPlugin.LOGGER.warn("Unable to find quest for player " + lastKnownName);
            return;
        }
        if (quest.getStage(stage) == null) {
            FabricQuestsPlugin.LOGGER.error("Unable to find Stage " + stage + " of quest ID " + quest.getId());
            return;
        }
        final Stage s = quest.getStage(stage);
        if (!s.getBlocksToBreak().isEmpty()) {
            for (final Object ignored : s.getBlocksToBreak()) {
                data.getBlocksBroken().add(0);
            }
        }
        if (!s.getBlocksToDamage().isEmpty()) {
            for (final Object ignored : s.getBlocksToDamage()) {
                data.getBlocksDamaged().add(0);
            }
        }
        if (!s.getBlocksToPlace().isEmpty()) {
            for (final Object ignored : s.getBlocksToPlace()) {
                data.getBlocksPlaced().add(0);
            }
        }
        if (!s.getBlocksToUse().isEmpty()) {
            for (final Object ignored : s.getBlocksToUse()) {
                data.getBlocksUsed().add(0);
            }
        }
        if (!s.getBlocksToCut().isEmpty()) {
            for (final Object ignored : s.getBlocksToCut()) {
                data.getBlocksCut().add(0);
            }
        }
        if (!s.getItemsToCraft().isEmpty()) {
            for (final Object ignored : s.getItemsToCraft()) {
                data.getItemsCrafted().add(0);
            }
        }
        if (!s.getItemsToSmelt().isEmpty()) {
            for (final Object ignored : s.getItemsToSmelt()) {
                data.getItemsSmelted().add(0);
            }
        }
        if (!s.getItemsToEnchant().isEmpty()) {
            for (final Object ignored : s.getItemsToEnchant()) {
                data.getItemsEnchanted().add(0);
            }
        }
        if (!s.getItemsToBrew().isEmpty()) {
            for (final Object ignored : s.getItemsToBrew()) {
                data.getItemsBrewed().add(0);
            }
        }
        if (!s.getItemsToConsume().isEmpty()) {
            for (final Object ignored : s.getItemsToConsume()) {
                data.getItemsConsumed().add(0);
            }
        }
        if (!s.getItemsToDeliver().isEmpty()) {
            for (final Object ignored : s.getItemsToDeliver()) {
                data.getItemsDelivered().add(0);
            }
        }
        if (!s.getNpcsToInteract().isEmpty()) {
            for (final Object ignored : s.getNpcsToInteract()) {
                data.getNpcsInteracted().add(false);
            }
        }
        if (!s.getNpcsToKill().isEmpty()) {
            for (final Object ignored : s.getNpcsToKill()) {
                data.getNpcsNumKilled().add(0);
            }
        }
        if (!s.getMobsToKill().isEmpty()) {
            for (final Object ignored : s.getMobsToKill()) {
                data.getMobNumKilled().add(0);
            }
        }
        data.setCowsMilked(0);
        data.setFishCaught(0);
        data.setPlayersKilled(0);
        if (!s.getLocationsToReach().isEmpty()) {
            for (final Object ignored : s.getLocationsToReach()) {
                data.getLocationsReached().add(false);
            }
        }
        if (!s.getMobsToTame().isEmpty()) {
            for (final Object ignored : s.getMobsToTame()) {
                data.getMobsTamed().add(0);
            }
        }
        if (!s.getSheepToShear().isEmpty()) {
            for (final Object ignored : s.getSheepToShear()) {
                data.getSheepSheared().add(0);
            }
        }
        if (!s.getPasswordDisplays().isEmpty()) {
            for (final String ignored : s.getPasswordDisplays()) {
                data.getPasswordsSaid().add(false);
            }
        }
        if (!s.getCustomObjectives().isEmpty()) {
            for (final CustomObjective ignored : s.getCustomObjectives()) {
                data.getCustomObjectiveCounts().add(0);
            }
        }
        data.setDoJournalUpdate(true);
        hardDataPut(quest, data);
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
                if (next.getStartAction() != null) {
                    next.getStartAction().fire(this, quest);
                }
                final ServerPlayer player = getPlayer();
                sendMessage("§6" + FabricLang.get(player, "objectives"));
                showCurrentObjectives(quest, this, false);
                if (next.getStartMessage() != null) {
                    sendMessage(FabricMiscUtil.parseString(next.getStartMessage()));
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
            sendMessage(FabricLang.get(getPlayer(), "noCurrentQuest"));
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
        final ServerPlayer player = getPlayer();
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
        final ServerPlayer player = getPlayer();
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
        final ServerPlayer player = getPlayer();
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
                        sendMessage("§e" + FabricLang.get(getPlayer(), "compassSet").replace("<quest>", quest.getName()));
                    }
                } else {
                    resetCompass();
                    setCompassTarget(quest);
                    if (notify) {
                        sendMessage("§c" + FabricLang.get(getPlayer(), "compassNone").replace("<quest>", quest.getName()));
                    }
                }
            }
        } else {
            sendMessage("§c" + FabricLang.get(getPlayer(), "journalNoQuests")
                    .replace("<journal>", FabricLang.get(getPlayer(), "journalTitle")));
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
        final ServerPlayer player = getPlayer();
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
                    final ServerPlayer player = getPlayer();
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
        final ServerPlayer player = getPlayer();
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
        final ServerPlayer player = getPlayer();
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



    public ServerPlayer getPlayer() {
        return plugin.getServer() != null ? plugin.getServer().getPlayerList().getPlayer(uuid) : null;
    }
}
