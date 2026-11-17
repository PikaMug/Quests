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
import me.pikamug.quests.convo.misc.FabricQuestAbandonPrompt;
import me.pikamug.quests.convo.misc.FabricQuestAcceptPrompt;
import me.pikamug.quests.dependencies.FabricPlaceholderSupport;
import me.pikamug.quests.dependencies.npc.FabricNpcDependency;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.events.quest.FabricQuestQuitEvent;
import me.pikamug.quests.events.quest.FabricQuestTakeEvent;
import me.pikamug.quests.events.quester.FabricQuesterPostStartQuestEvent;
import me.pikamug.quests.events.quester.FabricQuesterPostUpdateObjectiveEvent;
import me.pikamug.quests.events.quester.FabricQuesterPreOpenGUIEvent;
import me.pikamug.quests.events.quester.FabricQuesterPreStartQuestEvent;
import me.pikamug.quests.events.quester.FabricQuesterPreUpdateObjectiveEvent;
import me.pikamug.quests.module.CustomObjective;
import me.pikamug.quests.module.CustomRequirement;
import me.pikamug.quests.module.FabricCustomObjective;
import me.pikamug.quests.quests.FabricQuest;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.FabricObjective;
import me.pikamug.quests.quests.components.FabricStage;
import me.pikamug.quests.quests.components.Objective;
import me.pikamug.quests.quests.components.Planner;
import me.pikamug.quests.quests.components.Requirements;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.tasks.FabricStageTimer;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricInventoryUtil;
import me.pikamug.quests.util.FabricItemUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricLocation;
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
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.browsit.conversations.api.Conversations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.AbstractSet;
import java.util.Collection;
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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FabricQuester implements Quester {

    private final FabricQuestsPlugin plugin;
    private final UUID id;
    protected String questIdToTake;
    protected String questIdToQuit;
    private String lastKnownName;
    protected int questPoints = 0;
    private String compassTargetQuestId;
    private long lastNotifiedCondition = 0L;
    protected ConcurrentHashMap<Runnable, Quest> timers = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Quest, Integer> currentQuests = new ConcurrentHashMap<Quest, Integer>() {

        private static final long serialVersionUID = 6361484975823846780L;

        @Override
        public Integer put(final @NotNull Quest key, final @NotNull Integer val) {
            final Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(final @NotNull Object key) {
            final Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Quest, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    private Collection<Quest> completedQuests = new CompletedQuestSet();
    private class CompletedQuestSet extends AbstractSet<Quest> {
        private final Set<Quest> delegate = ConcurrentHashMap.newKeySet();
        @Override
        public boolean add(final Quest e) {
            final boolean changed = delegate.add(e);
            if (changed) updateJournal();
            return changed;
        }
        @Override
        public boolean addAll(final Collection<? extends Quest> c) {
            final boolean changed = delegate.addAll(c);
            if (changed) updateJournal();
            return changed;
        }
        @Override
        public void clear() {
            if (!delegate.isEmpty()) {
                delegate.clear();
                updateJournal();
            }
        }
        @Override
        public boolean remove(final Object o) {
            final boolean changed = delegate.remove(o);
            if (changed) updateJournal();
            return changed;
        }
        @Override
        public boolean removeAll(final Collection<?> c) {
            final boolean changed = delegate.removeAll(c);
            if (changed) updateJournal();
            return changed;
        }
        @Override
        public @NonNull Iterator<Quest> iterator() {
            return delegate.iterator();
        }
        @Override
        public int size() {
            return delegate.size();
        }
    }
    protected ConcurrentHashMap<Quest, Long> completedTimes = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<Quest, Integer> amountsCompleted = new ConcurrentHashMap<Quest, Integer>() {

        private static final long serialVersionUID = 5475202358792520975L;

        @Override
        public Integer put(final @NotNull Quest key, final @NotNull Integer val) {
            final Integer data = super.put(key, val);
            updateJournal();
            return data;
        }

        @Override
        public Integer remove(final @NotNull Object key) {
            final Integer i = super.remove(key);
            updateJournal();
            return i;
        }

        @Override
        public void clear() {
            super.clear();
            updateJournal();
        }

        @Override
        public void putAll(final Map<? extends Quest, ? extends Integer> m) {
            super.putAll(m);
            updateJournal();
        }
    };
    private boolean hasData = false;
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

    public FabricQuester(final FabricQuestsPlugin plugin, final UUID uuid) {
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

    public ConcurrentHashMap<Runnable, Quest> getTimers() {
        return timers;
    }

    public void setTimers(final ConcurrentHashMap<Runnable, Quest> timers) {
        this.timers = timers;
    }

    public void removeTimer(final Runnable task) {
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

    public void setQuestProgress(final Quest quest, final QuestProgress progress) {
        questProgress.put(quest, progress);
    }

    public ConcurrentHashMap<Quest, QuestProgress> getProgressData() {
        return questProgress;
    }

    public void setHasData(final boolean hasData) {
        this.hasData = hasData;
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

    public QuestProgress getQuestProgress(final Quest quest) {
        return questProgress.get(quest);
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
    public boolean offerQuest(final Quest quest, final boolean giveReason) {
        if (quest == null) {
            return false;
        }
        final FabricQuestTakeEvent event = new FabricQuestTakeEvent(quest, this);
        FabricQuestTakeEvent.EVENT.invoker().onEvent(event);
        if (event.isCancelled()) {
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
    public boolean canAcceptOffer(final Quest quest, final boolean giveReason) {
        if (quest == null) {
            return false;
        }
        final FabricQuest fabricQuest = (FabricQuest) quest;
        if (!plugin.getConfigSettings().canAllowCommandsForNpcQuests() && fabricQuest.getNpcStart() != null) {
            final UUID uuid = fabricQuest.getNpcStart();
            for (final FabricNpcDependency npcDependency : plugin.getDependencies().getNpcDependencies()) {
                final FabricLocation npcLocation = npcDependency.getLocation(uuid);
                if (npcLocation == null) continue;

                if (npcLocation.getWorld() != null
                        && npcLocation.getWorld().dimension().identifier().equals(getPlayer().level().dimension().identifier())
                        && npcLocation.distance(new FabricLocation(getPlayer().level(), getPlayer().blockPosition())) > 6.0) {
                    if (giveReason) {
                        final String msg = FabricLang.get(getPlayer(), "mustSpeakTo").replace("<npc>", plugin.getDependencies().getNpcName(uuid));
                        sendMessage(ChatFormatting.YELLOW + msg);
                    }
                    return false;
                }
            }
        }
        if (quest.getOptions().canGiveGloballyAtLogin() && !quest.getOptions().canOverrideMaxQuests()
                && getCurrentQuests().size() >= plugin.getConfigSettings().getMaxQuests()) {
            if (giveReason) {
                final String msg = FabricLang.get(getPlayer(), "questMaxAllowed").replace("<number>",
                        String.valueOf(plugin.getConfigSettings().getMaxQuests()));
                sendMessage(ChatFormatting.RED + msg);
            }
            return false;
        } else if (!quest.getOptions().canGiveGloballyAtLogin() && plugin.getConfigSettings().getMaxQuests() > 0
                && getCurrentQuests().size() >= plugin.getConfigSettings().getMaxQuests()) {
            if (giveReason) {
                final String msg = FabricLang.get(getPlayer(), "questMaxAllowed").replace("<number>",
                        String.valueOf(plugin.getConfigSettings().getMaxQuests()));
                sendMessage(ChatFormatting.YELLOW + msg);
            }
            return false;
        } else if (getCurrentQuests().containsKey(fabricQuest)) {
            if (giveReason) {
                final String msg = FabricLang.get(getPlayer(), "questAlreadyOn");
                sendMessage(ChatFormatting.YELLOW + msg);
            }
            return false;
        } else if (fabricQuest.getPlanner().getCooldown() < 0 && getCompletedQuests().contains(fabricQuest)) {
            if (giveReason) {
                final String msg = FabricLang.get(getPlayer(), "questAlreadyCompleted")
                        .replace("<quest>", fabricQuest.getName());
                sendMessage(ChatFormatting.YELLOW + msg);
            }
            return false;
        } else if (fabricQuest.getBlockStart() != null) {
            if (giveReason) {
                final String msg = FabricLang.get(getPlayer(), "noCommandStart").replace("<quest>",
                        fabricQuest.getName());
                sendMessage(ChatFormatting.YELLOW + msg);
            }
            return false;
        } else if (getRemainingCooldown(fabricQuest) > 0 && getCompletedQuests().contains(fabricQuest)
                && !fabricQuest.getPlanner().getOverride()) {
            if (giveReason) {
                final String msg = FabricLang.get(getPlayer(), "questTooEarly").replace("<quest>",
                        fabricQuest.getName()).replace("<time>", FabricMiscUtil
                        .getTime(getRemainingCooldown(fabricQuest)));
                sendMessage(ChatFormatting.YELLOW + msg);
            }
            return false;
        } else if (fabricQuest.getRegionStart() != null) {
            if (!fabricQuest.isInRegionStart(this)) {
                if (giveReason) {
                    final String msg = FabricLang.get(getPlayer(), "questInvalidLocation").replace("<quest>",
                            fabricQuest.getName());
                    sendMessage(ChatFormatting.YELLOW + msg);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Check if Quester is not too early or late for a planned quest<p>
     *
     * For player cooldown, use {@link #canAcceptOffer(Quest, boolean)} instead
     *
     * @param quest The quest to check
     * @param giveReason Whether to inform Quester of unpunctuality
     * @return true if on time
     */
    public boolean isOnTime(final Quest quest, final boolean giveReason) {
        if (quest == null) return true;
        final Planner pln = quest.getPlanner();
        final long currentTime = System.currentTimeMillis();
        final long start = pln.getStartInMillis(); // Start time in milliseconds since UTC epoch
        final long end = pln.getEndInMillis(); // End time in milliseconds since UTC epoch
        final long duration = end - start; // How long the quest can be active for
        final long repeat = pln.getRepeat(); // Length to wait in-between start times
        if (start != -1) {
            if (currentTime < start) {
                if (giveReason) {
                    String early = FabricLang.get(getPlayer(), "plnTooEarly");
                    early = early.replace("<quest>", quest.getName());
                    early = early.replace("<time>", FabricMiscUtil.getTime(start - currentTime));
                    sendMessage(ChatFormatting.YELLOW + early);
                }
                return false;
            }
        }
        if (end != -1 && repeat == -1) {
            if (currentTime > end) {
                if (giveReason) {
                    String late = FabricLang.get(getPlayer(), "plnTooLate");
                    late = late.replace("<quest>", quest.getName());
                    late = late.replace("<time>", FabricMiscUtil.getTime(currentTime - end));
                    sendMessage(ChatFormatting.RED + late);
                }
                return false;
            }
        }
        if (repeat != -1 && start != -1 && end != -1) {
            // Repeatable quest
            if (currentTime <= end) {
                // Initial period where quest may be active
                if (getCompletedTimes().containsKey(quest) && quest.getPlanner().getCooldown() > -1
                        && getRemainingCooldown(quest) > 0) {
                    if (giveReason) {
                        final String early = FabricLang.get(getPlayer(), "plnTooEarly").replace("<quest>", quest.getName())
                                .replace("<time>", FabricMiscUtil.getTime(end - currentTime));
                        sendMessage(ChatFormatting.YELLOW + early);
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
                if (!active || (quest.getPlanner().getOverride() && completedEnd > 0L && currentTime < completedEnd)) {
                    if (giveReason) {
                        final String early = FabricLang.get(getPlayer(), "plnTooEarly").replace("<quest>", quest.getName())
                                .replace("<time>", FabricMiscUtil.getTime(completedEnd - currentTime));
                        sendMessage(ChatFormatting.YELLOW + early);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Start a quest for this Quester
     *
     * @param quest The quest to start
     * @param ignoreRequirements Whether to ignore Requirements
     */
    public void takeQuest(final Quest quest, final boolean ignoreRequirements) {
        if (quest == null) {
            return;
        }
        final FabricQuesterPreStartQuestEvent preEvent = new FabricQuesterPreStartQuestEvent(this, quest);
        FabricQuesterPreStartQuestEvent.EVENT.invoker().onEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
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
            final me.pikamug.quests.quests.components.Requirements requirements = quest.getRequirements();
            final net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
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
                        if (!FabricInventoryUtil.removeItem(player, stack)
                                && !FabricInventoryUtil.stripItem(player, stack)) {
                            ok = false;
                        }
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
            sendMessage(ChatFormatting.GREEN + accepted);
            sendMessage("");
            if (plugin.getConfigSettings().canShowQuestTitles()) {
                player.connection.send(new ClientboundSetSubtitleTextPacket(
                        Component.literal(ChatFormatting.YELLOW + quest.getName())));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.literal(
                        ChatFormatting.GOLD + FabricLang.get(getPlayer(), "quest") + " "
                                + FabricLang.get(getPlayer(), "accepted"))));
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
            }
        }
        final Stage stage = quest.getStage(0);
        if (player != null) {
            FabricLang.send(player, ChatFormatting.GOLD + FabricLang.get(player, "objectives")
                    .replace("<quest>", quest.getName()));
            showCurrentObjectives(quest, this, false);
            if (stage != null && stage.getStartMessage() != null) {
                for (final String line : FabricConfigUtil.parseStringWithPossibleLineBreaks(
                        stage.getStartMessage(), quest, getPlayer())) {
                    sendMessage(line);
                }
            }
            showCurrentConditions(quest, this);
        }
        if (quest.getInitialAction() != null) {
            quest.getInitialAction().fire(this, quest);
        }
        if (stage != null && stage.getStartAction() != null) {
            stage.getStartAction().fire(this, quest);
        }
        questIdToTake = null;
        saveData();
        setCompassTarget(quest);
        quest.updateCompass(this, stage);
        updateJournal();
        if (getPlayer() != null) {
            final FabricQuesterPostStartQuestEvent postEvent = new FabricQuesterPostStartQuestEvent(this, quest);
            FabricQuesterPostStartQuestEvent.EVENT.invoker().onEvent(postEvent);
        }
    }

    /**
     * End a quest for this Quester, but ask permission first if possible<p>
     *
     * @param quest The quest to check and then offer
     * @param message Message to send Quester upon quit, can be left null or empty
     * @return true if successful
     */
    public boolean abandonQuest(final Quest quest, final String message) {
        return abandonQuest(quest, message != null ? new String[]{message} : new String[0]);
    }

    /**
     * End a quest for this Quester, but ask permission first if possible<p>
     *
     * @param quest The quest to check and then offer
     * @param messages Messages to send Quester upon quit, can be left null or empty
     * @return true if successful
     */
    public boolean abandonQuest(final Quest quest, final String[] messages) {
        if (quest == null) {
            return false;
        }
        final FabricQuestQuitEvent quitEvent = new FabricQuestQuitEvent(quest, this);
        FabricQuestQuitEvent.EVENT.invoker().onEvent(quitEvent);
        if (quitEvent.isCancelled()) {
            return false;
        }
        if (!currentQuests.containsKey(quest)) {
            return false;
        }
        if (!quest.getOptions().canAllowQuitting()) {
            sendMessage(FabricLang.get(getPlayer(), "questQuitDisabled"));
            return false;
        }
        if (getPlayer() != null) {
            setQuestIdToQuit(quest.getId());
            if (plugin.getConfigSettings().canConfirmAbandon()) {
                new FabricQuestAbandonPrompt(id, plugin).start();
            } else {
                quitQuest(quest, messages);
            }
            return true;
        }
        return false;
    }

    /**
     * End a quest for this Quester
     *
     * @param quest The quest to start
     * @param message Message to inform player, can be left null or empty
     * @since 3.8.6
     */
    public void quitQuest(final Quest quest, final String message) {
        quitQuest(quest, new String[] {message});
    }

    /**
     * End a quest for this Quester
     *
     * @param quest The quest to start
     * @param messages Messages to inform player, can be left null or empty
     * @since 3.8.6
     */
    public void quitQuest(final Quest quest, final String[] messages) {
        if (quest == null) {
            return;
        }
        hardQuit(quest);
        if (plugin.getConfigSettings().getConsoleLogging() > 1) {
            plugin.getPluginLogger().info(getOfflinePlayer().getUniqueId() + " quit quest " + quest.getName());
        }
        for (final String message : messages) {
            if (getOfflinePlayer().isOnline()) {
                FabricLang.send(getPlayer(), message);
            }
        }
        saveData();
        updateJournal();
        if (compassTargetQuestId != null && compassTargetQuestId.equals(quest.getId())) {
            compassTargetQuestId = null;
        }
    }

    /**
     * Show the player a list of their available quests
     *
     * @param quester Quester to show the list
     * @param page Page to display, with 7 quests per page
     */
    public void listQuests(final Quester quester, final int page) {
        // Although we could copy the quests list to a new object, we instead opt to
        // duplicate code to improve efficiency if ignore-locked-quests is set to 'false'
        final int rows = 7;
        final ServerPlayer player = getPlayer();
        final Collection<Quest> quests = plugin.getLoadedQuests();
        if (plugin.getConfigSettings().canIgnoreLockedQuests()) {
            final LinkedList<Quest> available = new LinkedList<>();
            for (final Quest q : quests) {
                if (!quester.getCompletedQuests().contains(q)) {
                    if (q.testRequirements(quester)) {
                        available.add(q);
                    }
                } else if (q.getPlanner().hasCooldown() && quester.getRemainingCooldown(q) <= 0) {
                    if (q.testRequirements(quester)) {
                        available.add(q);
                    }
                }
            }
            if ((available.size() + rows) <= (page * rows) || available.isEmpty()) {
                FabricLang.send(player, ChatFormatting.YELLOW + FabricLang.get(player, "pageNotExist"));
            } else {
                FabricLang.send(player, ChatFormatting.GOLD + FabricLang.get(player, "questListTitle"));
                int fromOrder = (page - 1) * rows;
                final List<Quest> subQuests;
                if (available.size() >= (fromOrder + rows)) {
                    subQuests = available.subList((fromOrder), (fromOrder + rows));
                } else {
                    subQuests = available.subList((fromOrder), available.size());
                }
                fromOrder++;
                for (final Quest q : subQuests) {
                    if (quester.canAcceptOffer(q, false)) {
                        quester.sendMessage(ChatFormatting.YELLOW + Integer.toString(fromOrder) + ". "
                                + q.getName());
                    } else {
                        quester.sendMessage(ChatFormatting.GRAY + Integer.toString(fromOrder) + ". "
                                + q.getName());
                    }
                    fromOrder++;
                }
                final int numPages = (int) Math.ceil(((double) available.size()) / ((double) rows));
                String msg = FabricLang.get(player, "pageFooter");
                msg = msg.replace("<current>", String.valueOf(page));
                msg = msg.replace("<all>", String.valueOf(numPages));
                FabricLang.send(player, ChatFormatting.GOLD + msg);
            }
        } else {
            if ((quests.size() + rows) <= (page * rows) || quests.isEmpty()) {
                FabricLang.send(player, ChatFormatting.YELLOW + FabricLang.get(player, "pageNotExist"));
            } else {
                FabricLang.send(player, ChatFormatting.GOLD + FabricLang.get(player, "questListTitle"));
                int fromOrder = (page - 1) * rows;
                final List<Quest> subQuests;
                if (quests.size() >= (fromOrder + rows)) {
                    subQuests = new LinkedList<>(quests).subList((fromOrder), (fromOrder + rows));
                } else {
                    subQuests = new LinkedList<>(quests).subList((fromOrder), quests.size());
                }
                fromOrder++;
                for (final Quest q : subQuests) {
                    if (quester.canAcceptOffer(q, false)) {
                        FabricLang.send(player, ChatFormatting.YELLOW + Integer.toString(fromOrder) + ". "
                                + q.getName());
                    } else {
                        FabricLang.send(player, ChatFormatting.GRAY + Integer.toString(fromOrder) + ". "
                                + q.getName());
                    }
                    fromOrder++;
                }
                final int numPages = (int) Math.ceil(((double) quests.size()) / ((double) rows));
                String msg = FabricLang.get(player, "pageFooter");
                msg = msg.replace("<current>", String.valueOf(page));
                msg = msg.replace("<all>", String.valueOf(numPages));
                FabricLang.send(player, ChatFormatting.GOLD + msg);
            }
        }
    }

    /**
     * Get current requirements for a quest, both finished and unfinished
     *
     * @param quest The quest to get objectives of
     * @param ignoreOverrides Whether to ignore objective-overrides
     * @return Ordered list of detailed requirements
     */
    public LinkedList<String> getCurrentRequirements(final Quest quest, final boolean ignoreOverrides) {
        if (quest == null) {
            return new LinkedList<>();
        }
        final Requirements requirements = quest.getRequirements();
        if (!ignoreOverrides) {
            if (requirements.getDetailsOverride() != null && !requirements.getDetailsOverride().isEmpty()) {
                final LinkedList<String> current = new LinkedList<>();
                for (final String s : requirements.getDetailsOverride()) {
                    current.add(ChatFormatting.RED + FabricConfigUtil.parseString(s, quest, getPlayer()));
                }
                return current;
            }
        }
        final LinkedList<String> unfinishedRequirements = new LinkedList<>();
        final LinkedList<String> finishedRequirements = new LinkedList<>();
        final LinkedList<String> current = new LinkedList<>();
        final ServerPlayer player = getPlayer();
        if (player == null) {
            current.add("Player was null");
            return current;
        }
        if (requirements.getQuestPoints() > 0) {
            if (getQuestPoints() >= requirements.getQuestPoints()) {
                unfinishedRequirements.add(ChatFormatting.GREEN + "" + requirements.getQuestPoints() + " "
                        + FabricLang.get(player, "questPoints"));
            } else {
                finishedRequirements.add(ChatFormatting.GRAY + "" + requirements.getQuestPoints() + " "
                        + FabricLang.get(player, "questPoints"));
            }
        }
        final Map<String, String> completed = completedQuests.stream()
                .collect(Collectors.toMap(Quest::getId, Quest::getName, (a, b) -> a));
        for (final String questId : requirements.getNeededQuestIds()) {
            if (completed.containsKey(questId)) {
                String msg = FabricLang.get(player, "haveCompleted");
                msg = msg.replace("<quest>", completed.get(questId));
                finishedRequirements.add(ChatFormatting.GREEN + msg);
            } else {
                String msg = FabricLang.get(player, "mustComplete");
                final Quest required = plugin.getQuestById(questId);
                msg = msg.replace("<quest>", required != null ? required.getName() : questId);
                unfinishedRequirements.add(ChatFormatting.GRAY + msg);
            }
        }
        for (final String questId : requirements.getBlockQuestIds()) {
            if (completed.containsKey(questId)) {
                String msg = FabricLang.get(player, "cannotComplete");
                msg = msg.replace("<quest>", quest.getName());
                current.add(ChatFormatting.RED + msg);
            }
        }
        for (final Quest q : currentQuests.keySet()) {
            if (q != null) {
                if (requirements.getBlockQuestIds().contains(q.getId())) {
                    current.add(ChatFormatting.RED + quest.getName());
                }
            }
        }
        for (final Object rawItem : requirements.getItems()) {
            if (!(rawItem instanceof ItemStack)) {
                continue;
            }
            final ItemStack is = (ItemStack) rawItem;
            if (is.isEmpty()) {
                continue;
            }
            if (FabricInventoryUtil.countItem(player, is) >= is.getCount()) {
                finishedRequirements.add(ChatFormatting.GREEN + "" + is.getCount() + " " + FabricItemUtil.getName(is));
            } else {
                unfinishedRequirements.add(ChatFormatting.GRAY + "" + is.getCount() + " " + FabricItemUtil.getName(is));
            }
        }
        for (final String perm : requirements.getPermissions()) {
            if (plugin.getDependencies().hasPermission(id, perm)) {
                finishedRequirements.add(ChatFormatting.GREEN + FabricLang.get(player, "permissionDisplay") + " " + perm);
            } else {
                unfinishedRequirements.add(ChatFormatting.GRAY + FabricLang.get(player, "permissionDisplay") + " " + perm);
            }
        }
        for (final Map.Entry<String, Map<String, Object>> m : requirements.getCustomRequirements().entrySet()) {
            for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                if (!cr.getName().equalsIgnoreCase(m.getKey())) {
                    continue;
                }
                String message = cr.getDisplay() != null ? cr.getDisplay() : m.getKey();
                for (final Map.Entry<String, Object> prompt : cr.getData().entrySet()) {
                    final String replacement = "%" + prompt.getKey() + "%";
                    try {
                        if (message.contains(replacement)) {
                            message = message.replace(replacement, String.valueOf(m.getValue().get(prompt.getKey())));
                        }
                    } catch (final NullPointerException ne) {
                        FabricQuestsPlugin.LOGGER.error("Unable to gather display for " + cr.getName() + " on "
                                + quest.getName(), ne);
                    }
                }
                if (cr.testRequirement(id, m.getValue())) {
                    finishedRequirements.add(ChatFormatting.GREEN + "" + message);
                } else {
                    unfinishedRequirements.add(ChatFormatting.GRAY + "" + message);
                }
            }
        }
        current.addAll(unfinishedRequirements);
        current.addAll(finishedRequirements);
        return current;
    }

    /**
     * Get current objectives for a quest, both finished and unfinished
     *
     * @param quest The quest to get objectives of
     * @param ignoreOverrides Whether to ignore objective-overrides
     * @param formatNames Whether to format item/entity names, if applicable
     * @return Ordered list of detailed objectives
     */
    public LinkedList<Objective> getCurrentObjectives(final Quest quest, final boolean ignoreOverrides,
                                                      final boolean formatNames) {
        final LinkedList<Objective> objectives = new LinkedList<>();
        if (quest == null) {
            plugin.getPluginLogger().severe("Quest was null when getting objectives for " + getLastKnownName());
            return objectives;
        }
        if (getQuestProgressOrDefault(quest) == null) {
            plugin.getPluginLogger().warning("Quest data was null when getting objectives for " + quest.getName());
            return objectives;
        }
        final FabricStage stage = (FabricStage) getCurrentStage(quest);
        if (stage == null) {
            //plugin.getLogger().warning("Current stage was null when getting objectives for " + quest.getName());
            return objectives;
        }
        final QuestProgress data = getQuestProgressOrDefault(quest);
        final ServerPlayer player = getPlayer();
        final LinkedList<String> overrides = stage.getObjectiveOverrides();
        int objIndex = -1;

        // Break blocks
        for (int i = 0; i < data.getBlocksBroken().size(); i++) {
            objIndex++;
            final int progress = data.getBlocksBroken().get(i);
            if (i >= stage.getBlocksToBreak().size()) { break; }
            final ItemStack goal = stage.getBlocksToBreak().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "break"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            objectives.add(new FabricObjective(ObjectiveType.BREAK_BLOCK, message, progress, goal));
        }

        // Damage blocks
        for (int i = 0; i < data.getBlocksDamaged().size(); i++) {
            objIndex++;
            final int current = data.getBlocksDamaged().get(i);
            if (i >= stage.getBlocksToDamage().size()) { break; }
            final Object goalObj = stage.getBlocksToDamage().get(i);
            if (goalObj == null) continue;
            final int goal = getBlockAmount(stage.getBlocksToDamageAmounts(), i);
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "damage", current, goal)
                    .replace("<item>", blockName(goalObj));
            objectives.add(new FabricObjective(ObjectiveType.DAMAGE_BLOCK, message, current, goal));
        }

        // Place blocks
        for (int i = 0; i < data.getBlocksPlaced().size(); i++) {
            objIndex++;
            final int current = data.getBlocksPlaced().get(i);
            if (i >= stage.getBlocksToPlace().size()) { break; }
            final Object goalObj = stage.getBlocksToPlace().get(i);
            if (goalObj == null) continue;
            final int goal = getBlockAmount(stage.getBlocksToPlaceAmounts(), i);
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "place", current, goal)
                    .replace("<item>", blockName(goalObj));
            objectives.add(new FabricObjective(ObjectiveType.PLACE_BLOCK, message, current, goal));
        }

        // Use blocks
        for (int i = 0; i < data.getBlocksUsed().size(); i++) {
            objIndex++;
            final int current = data.getBlocksUsed().get(i);
            if (i >= stage.getBlocksToUse().size()) { break; }
            final Object goalObj = stage.getBlocksToUse().get(i);
            if (goalObj == null) continue;
            final int goal = getBlockAmount(stage.getBlocksToUseAmounts(), i);
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "use", current, goal)
                    .replace("<item>", blockName(goalObj));
            objectives.add(new FabricObjective(ObjectiveType.USE_BLOCK, message, current, goal));
        }

        // Items crafted
        for (int i = 0; i < data.getItemsCrafted().size(); i++) {
            objIndex++;
            final int current = data.getItemsCrafted().get(i);
            if (i >= stage.getItemsToCraft().size()) { break; }
            final Object goalObj = stage.getItemsToCraft().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            final ItemStack goal = (ItemStack) goalObj;
            final int goalAmount = Math.max(1, goal.getCount());
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "craftItem", current,
                    goalAmount)
                    .replace("<item>", FabricItemUtil.getName(goal));
            objectives.add(new FabricObjective(ObjectiveType.CRAFT_ITEM, message, current, goalAmount));
        }

        // Items smelted
        for (int i = 0; i < data.getItemsSmelted().size(); i++) {
            objIndex++;
            final int current = data.getItemsSmelted().get(i);
            if (i >= stage.getItemsToSmelt().size()) { break; }
            final Object goalObj = stage.getItemsToSmelt().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            final ItemStack goal = (ItemStack) goalObj;
            final int goalAmount = Math.max(1, goal.getCount());
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "smeltItem", current,
                    goalAmount)
                    .replace("<item>", FabricItemUtil.getName(goal));
            objectives.add(new FabricObjective(ObjectiveType.SMELT_ITEM, message, current, goalAmount));
        }

        // Items enchanted
        for (int i = 0; i < data.getItemsEnchanted().size(); i++) {
            objIndex++;
            final int current = data.getItemsEnchanted().get(i);
            if (i >= stage.getItemsToEnchant().size()) { break; }
            final Object goalObj = stage.getItemsToEnchant().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            final ItemStack goal = (ItemStack) goalObj;
            final int goalAmount = Math.max(1, goal.getCount());
            String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "enchItem", current,
                    goalAmount)
                    .replace("<item>", FabricItemUtil.getName(goal));
            final ItemEnchantments ench = goal.get(DataComponents.ENCHANTMENTS);
            if (ench == null || ench.isEmpty()) {
                objectives.add(new FabricObjective(ObjectiveType.ENCHANT_ITEM,
                        message.replace("<enchantment>", "").replace("<level>", ""), current, goalAmount));
            } else {
                for (final Map.Entry<Holder<Enchantment>, Integer> entry : ench.entrySet()) {
                    String enchName = entry.getKey().getRegisteredName();
                    if (enchName != null && enchName.indexOf(':') != -1) {
                        enchName = enchName.substring(enchName.indexOf(':') + 1);
                    }
                    objectives.add(new FabricObjective(ObjectiveType.ENCHANT_ITEM,
                            message.replace("<enchantment>", enchName != null
                                            ? FabricMiscUtil.snakeCaseToUpperCamelCase(enchName) : "")
                                    .replace("<level>", toRoman(entry.getValue())), current, goalAmount));
                }
            }
        }

        // Items brewed
        for (int i = 0; i < data.getItemsBrewed().size(); i++) {
            objIndex++;
            final int current = data.getItemsBrewed().get(i);
            if (i >= stage.getItemsToBrew().size()) { break; }
            final Object goalObj = stage.getItemsToBrew().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            final ItemStack goal = (ItemStack) goalObj;
            final int goalAmount = Math.max(1, goal.getCount());
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "brewItem", current,
                    goalAmount)
                    .replace("<item>", FabricItemUtil.getName(goal))
                    .replace(" <level>", "")
                    .replace("<level>", "");
            objectives.add(new FabricObjective(ObjectiveType.BREW_ITEM, message, current, goalAmount));
        }

        // Items consumed
        for (int i = 0; i < data.getItemsConsumed().size(); i++) {
            objIndex++;
            final int current = data.getItemsConsumed().get(i);
            if (i >= stage.getItemsToConsume().size()) { break; }
            final Object goalObj = stage.getItemsToConsume().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            final ItemStack goal = (ItemStack) goalObj;
            final int goalAmount = Math.max(1, goal.getCount());
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "consumeItem", current,
                    goalAmount)
                    .replace("<item>", FabricItemUtil.getName(goal))
                    .replace(" <level>", "")
                    .replace("<level>", "");
            objectives.add(new FabricObjective(ObjectiveType.CONSUME_ITEM, message, current, goalAmount));
        }

        // Items delivered
        for (int i = 0; i < data.getItemsDelivered().size(); i++) {
            objIndex++;
            final int current = data.getItemsDelivered().get(i);
            if (i >= stage.getItemsToDeliver().size()) { break; }
            final Object goalObj = stage.getItemsToDeliver().get(i);
            if (!(goalObj instanceof ItemStack)) continue;
            final ItemStack goal = (ItemStack) goalObj;
            final int goalAmount = Math.max(1, goal.getCount());
            final UUID npcUuid = (stage.getItemDeliveryTargets().size() > i)
                    ? stage.getItemDeliveryTargets().get(i) : null;
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "deliver", current,
                    goalAmount)
                    .replace("<item>", FabricItemUtil.getName(goal))
                    .replace("<npc>", npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?");
            objectives.add(new FabricObjective(ObjectiveType.DELIVER_ITEM, message, current, goalAmount));
        }

        // NPCs interacted
        for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
            objIndex++;
            final boolean said = (data.getNpcsInteracted().size() > i) && data.getNpcsInteracted().get(i);
            final UUID npcUuid = stage.getNpcsToInteract().get(i);
            final String npcName = npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?";
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "talkTo", said ? 1 : 0, 1)
                    .replace("<npc>", npcName);
            objectives.add(new FabricObjective(ObjectiveType.TALK_TO_NPC, message, said ? 1 : 0, 1));
        }

        // NPCs killed
        for (int i = 0; i < stage.getNpcsToKill().size(); i++) {
            objIndex++;
            final int goal = (stage.getNpcNumToKill() != null && stage.getNpcNumToKill().size() > i)
                    ? stage.getNpcNumToKill().get(i) : 1;
            final int current = (data.getNpcsNumKilled().size() > i) ? data.getNpcsNumKilled().get(i) : 0;
            String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "kill", current, goal);
            final UUID npcUuid = stage.getNpcsToKill().get(i);
            final String npcName = npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?";
            if (message.contains("<mob>")) {
                message = message.replace("<mob>", npcName);
            } else {
                message += npcName;
            }
            objectives.add(new FabricObjective(ObjectiveType.KILL_NPC, message, current, goal));
        }

        // Mobs killed
        for (int i = 0; i < stage.getMobsToKill().size(); i++) {
            objIndex++;
            final int goal = (stage.getMobNumToKill() != null && stage.getMobNumToKill().size() > i)
                    ? stage.getMobNumToKill().get(i) : 1;
            final int current = (data.getMobNumKilled().size() > i) ? data.getMobNumKilled().get(i) : 0;
            final String mobName = FabricMiscUtil.snakeCaseToUpperCamelCase(
                    stage.getMobsToKill().get(i).toString());
            String message;
            if (stage.getLocationsToKillWithin().isEmpty()) {
                message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "kill", current, goal);
            } else {
                final String killName = stage.getKillNames() != null && stage.getKillNames().size() > i
                        && stage.getKillNames().get(i) != null ? stage.getKillNames().get(i) : "?";
                message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "killAtLocation", current, goal)
                        .replace("<location>", killName);
            }
            message = message.replace("<mob>", mobName);
            objectives.add(new FabricObjective(ObjectiveType.KILL_MOB, message, current, goal));
        }

        // Mobs tamed
        for (int i = 0; i < stage.getMobsToTame().size(); i++) {
            objIndex++;
            final int goal = (stage.getMobNumToTame() != null && stage.getMobNumToTame().size() > i)
                    ? stage.getMobNumToTame().get(i) : 1;
            final int current = (data.getMobsTamed().size() > i) ? data.getMobsTamed().get(i) : 0;
            final Object tameObj = stage.getMobsToTame().get(i);
            final String mobName = tameObj != null
                    ? FabricMiscUtil.snakeCaseToUpperCamelCase(tameObj.toString()) : "?";
            String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "tame", current, goal)
                    .replace("<mob>", mobName);
            objectives.add(new FabricObjective(ObjectiveType.TAME_MOB, message, current, goal));
        }

        // Fish caught
        if (stage.getFishToCatch() != null && stage.getFishToCatch() > 0) {
            objIndex++;
            final int goal = stage.getFishToCatch();
            final int current = data.getFishCaught();
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "catchFish", current, goal);
            objectives.add(new FabricObjective(ObjectiveType.CATCH_FISH, message, current, goal));
        }

        // Cows milked
        if (stage.getCowsToMilk() != null && stage.getCowsToMilk() > 0) {
            objIndex++;
            final int goal = stage.getCowsToMilk();
            final int current = data.getCowsMilked();
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "milkCow", current, goal);
            objectives.add(new FabricObjective(ObjectiveType.MILK_COW, message, current, goal));
        }

        // Sheep sheared
        for (int i = 0; i < stage.getSheepToShear().size(); i++) {
            objIndex++;
            final int goal = (stage.getSheepNumToShear() != null && stage.getSheepNumToShear().size() > i)
                    ? stage.getSheepNumToShear().get(i) : 1;
            final int current = (data.getSheepSheared().size() > i) ? data.getSheepSheared().get(i) : 0;
            final Object dyeObj = stage.getSheepToShear().get(i);
            final String color = dyeObj != null ? FabricMiscUtil.snakeCaseToUpperCamelCase(dyeObj.toString()) : "?";
            String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "shearSheep", current, goal)
                    .replace("<color>", color);
            objectives.add(new FabricObjective(ObjectiveType.SHEAR_SHEEP, message, current, goal));
        }

        // Players killed
        if (stage.getPlayersToKill() != null && stage.getPlayersToKill() > 0) {
            objIndex++;
            final int goal = stage.getPlayersToKill();
            final int current = data.getPlayersKilled();
            final String message = objectiveLine(player, ignoreOverrides, overrides, objIndex, "killPlayer", current, goal);
            objectives.add(new FabricObjective(ObjectiveType.KILL_PLAYER, message, current, goal));
        }

        // Locations to reach
        for (int i = 0; i < stage.getLocationsToReach().size(); i++) {
            objIndex++;
            final boolean reached = (data.getLocationsReached().size() > i) && data.getLocationsReached().get(i);
            final String locationName = (stage.getLocationNames() != null && stage.getLocationNames().size() > i
                    && stage.getLocationNames().get(i) != null) ? stage.getLocationNames().get(i) : "?";
            final String msg = objectiveLine(player, ignoreOverrides, overrides, objIndex, "goTo", reached ? 1 : 0, 1)
                    .replace("<location>", locationName);
            objectives.add(new FabricObjective(ObjectiveType.REACH_LOCATION, msg, reached ? 1 : 0, 1));
        }

        // Passwords said
        for (int i = 0; i < stage.getPasswordDisplays().size(); i++) {
            objIndex++;
            final boolean said = (data.getPasswordsSaid().size() > i) && data.getPasswordsSaid().get(i);
            final String display = stage.getPasswordDisplays().get(i);
            final String message = display != null ? display : "";
            objectives.add(new FabricObjective(ObjectiveType.PASSWORD, message, said ? 1 : 0, 1));
        }

        // Custom objectives
        for (int i = 0; i < stage.getCustomObjectives().size(); i++) {
            objIndex++;
            final CustomObjective co = stage.getCustomObjectives().get(i);
            if (co == null) continue;
            final int goal = (stage.getCustomObjectiveCounts() != null && stage.getCustomObjectiveCounts().size() > i)
                    ? stage.getCustomObjectiveCounts().get(i) : 1;
            final int current = (data.getCustomObjectiveCounts().size() > i)
                    ? data.getCustomObjectiveCounts().get(i) : 0;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                final String s = overrides.get(objIndex);
                message = (s != null ? s : "")
                        .replace("<count>", current + "/" + goal)
                        .replace("%count%", current + "/" + goal);
            } else {
                message = co.getDisplay() != null ? co.getDisplay() : "";
            }
            if (co instanceof FabricCustomObjective) {
                FabricCustomObjective fab = (FabricCustomObjective) co;
                for (final Map.Entry<String, Object> prompt : fab.getData()) {
                    final String replacement = "%" + prompt.getKey() + "%";
                    if (message.contains(replacement)) {
                        for (final Map.Entry<String, Object> e : stage.getCustomObjectiveData()) {
                            if (e.getKey().equals(prompt.getKey())) {
                                message = message.replace(replacement, String.valueOf(e.getValue()));
                            }
                        }
                    }
                }
            }
            if (co.canShowCount()) {
                message = message.replace("%count%", current + "/" + goal);
            }
            objectives.add(new FabricObjective(ObjectiveType.CUSTOM, message, current, goal));
        }

        for (int i = 0; i < objectives.size(); i++) {
            final Objective objective = objectives.get(i);
            String message = objective.getMessage();
            if (message != null) {
                message = FabricConfigUtil.parseString(message.trim().replaceAll("\\s{2,}", ""));
                objectives.set(i, new FabricObjective(objective.getType(), message, objective.getProgress(),
                        objective.getGoal()));
            }
        }

        return objectives;
    }

    /**
     * Format current objective message with count and placeholders
     *
     * @param color Color for text
     * @param message Text to format
     * @param progress Objective progress
     * @param goal Objective goal
     * @return Formatted message
     */
    private String formatCurrentObjectiveMessage(ChatFormatting color, String message, int progress, int goal) {
        message = color + message;
        message = message.replace("<count>", "" + color + progress + "/" + goal);
        message = message.replace("%count%", "" + color + progress + "/" + goal);
        if (plugin.getDependencies().getPlaceholderApi() != null) {
            message = FabricPlaceholderSupport.setPlaceholders(getPlayer(), message);
        }
        return message;
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
        final FabricStage fs = (FabricStage) stage;
        switch (type) {
            case BREAK_BLOCK: return getBlockAmount(fs.getBlocksToBreakAmounts(), index);
            case DAMAGE_BLOCK: return getBlockAmount(fs.getBlocksToDamageAmounts(), index);
            case PLACE_BLOCK: return getBlockAmount(fs.getBlocksToPlaceAmounts(), index);
            case USE_BLOCK: return getBlockAmount(fs.getBlocksToUseAmounts(), index);
            case CRAFT_ITEM: return itemGoal(fs.getItemsToCraft(), index);
            case SMELT_ITEM: return itemGoal(fs.getItemsToSmelt(), index);
            case ENCHANT_ITEM: return itemGoal(fs.getItemsToEnchant(), index);
            case BREW_ITEM: return itemGoal(fs.getItemsToBrew(), index);
            case CONSUME_ITEM: return itemGoal(fs.getItemsToConsume(), index);
            case DELIVER_ITEM: return itemGoal(fs.getItemsToDeliver(), index);
            case KILL_MOB: return listGoal(fs.getMobNumToKill(), index);
            case TAME_MOB: return listGoal(fs.getMobNumToTame(), index);
            case SHEAR_SHEEP: return listGoal(fs.getSheepNumToShear(), index);
            case KILL_NPC: return listGoal(fs.getNpcNumToKill(), index);
            case KILL_PLAYER: return fs.getPlayersToKill() != null ? fs.getPlayersToKill() : 0;
            case CATCH_FISH: return fs.getFishToCatch() != null ? fs.getFishToCatch() : 0;
            case MILK_COW: return fs.getCowsToMilk() != null ? fs.getCowsToMilk() : 0;
            default: return 1;
        }
    }

    private Object itemGoalObject(final LinkedList<?> items, final int index) {
        if (items == null || items.size() <= index || !(items.get(index) instanceof ItemStack)) return 1;
        return items.get(index);
    }

    private Object objectiveGoalObject(final Stage stage, final ObjectiveType type, final int index) {
        final FabricStage fs = (FabricStage) stage;
        switch (type) {
            case CRAFT_ITEM: return itemGoalObject(fs.getItemsToCraft(), index);
            case SMELT_ITEM: return itemGoalObject(fs.getItemsToSmelt(), index);
            case ENCHANT_ITEM: return itemGoalObject(fs.getItemsToEnchant(), index);
            case BREW_ITEM: return itemGoalObject(fs.getItemsToBrew(), index);
            case CONSUME_ITEM: return itemGoalObject(fs.getItemsToConsume(), index);
            case DELIVER_ITEM: return itemGoalObject(fs.getItemsToDeliver(), index);
            default: return objectiveGoal(stage, type, index);
        }
    }

    public void firePreUpdateObjective(final Quest quest, final ObjectiveType type, final int index) {
        if (quest == null || type == null) return;
        final Stage stage = getCurrentStage(quest);
        final QuestProgress progress = getQuestProgress(quest);
        if (stage == null || progress == null) return;
        final FabricObjective objective = new FabricObjective(type, null,
                objectiveProgress(progress, type, index), objectiveGoalObject(stage, type, index));
        FabricQuesterPreUpdateObjectiveEvent.EVENT.invoker().onEvent(
                new FabricQuesterPreUpdateObjectiveEvent(this, quest, objective));
    }

    public void firePostUpdateObjective(final Quest quest, final ObjectiveType type, final int index) {
        if (quest == null || type == null) return;
        final Stage stage = getCurrentStage(quest);
        final QuestProgress progress = getQuestProgress(quest);
        if (stage == null || progress == null) return;
        final FabricObjective objective = new FabricObjective(type, null,
                objectiveProgress(progress, type, index), objectiveGoalObject(stage, type, index));
        FabricQuesterPostUpdateObjectiveEvent.EVENT.invoker().onEvent(
                new FabricQuesterPostUpdateObjectiveEvent(this, quest, objective));
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

    private String formatCompletedObjectiveMessage(final ServerPlayer player, final String langKey, final int goal) {
        final String line = FabricLang.get(player, langKey)
                .replace("<count>", goal + "/" + goal)
                .replace("%count%", goal + "/" + goal);
        return ChatFormatting.GREEN + "(" + FabricLang.get(player, "completed") + ") " + line;
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
                final String message = ChatFormatting.GREEN + "(" + FabricLang.get(player, "completed") + ") "
                        + FabricConfigUtil.parseString(s.replace("<count>", count).replace("%count%", count));
                sendMessage(message);
            }
            return;
        }

        switch (type) {
            case BREAK_BLOCK:
                if (index >= stage.getBlocksToBreak().size()) return;
                sendMessage(formatCompletedObjectiveMessage(player, "break", goal)
                        .replace("<item>", blockName(stage.getBlocksToBreak().get(index))));
                break;
            case PLACE_BLOCK:
                if (index >= stage.getBlocksToPlace().size()) return;
                sendMessage(formatCompletedObjectiveMessage(player, "place", goal)
                        .replace("<item>", blockName(stage.getBlocksToPlace().get(index))));
                break;
            case DAMAGE_BLOCK:
                if (index >= stage.getBlocksToDamage().size()) return;
                sendMessage(formatCompletedObjectiveMessage(player, "damage", goal)
                        .replace("<item>", blockName(stage.getBlocksToDamage().get(index))));
                break;
            case USE_BLOCK:
                if (index >= stage.getBlocksToUse().size()) return;
                sendMessage(formatCompletedObjectiveMessage(player, "use", goal)
                        .replace("<item>", blockName(stage.getBlocksToUse().get(index))));
                break;
            case CRAFT_ITEM:
                if (index >= stage.getItemsToCraft().size()
                        || !(stage.getItemsToCraft().get(index) instanceof ItemStack)) return;
                    ItemStack craftGoal = (ItemStack) stage.getItemsToCraft().get(index);
                sendMessage(formatCompletedObjectiveMessage(player, "craftItem", goal)
                        .replace("<item>", FabricItemUtil.getName(craftGoal)));
                break;
            case SMELT_ITEM:
                if (index >= stage.getItemsToSmelt().size()
                        || !(stage.getItemsToSmelt().get(index) instanceof ItemStack)) return;
                    ItemStack smeltGoal = (ItemStack) stage.getItemsToSmelt().get(index);
                sendMessage(formatCompletedObjectiveMessage(player, "smeltItem", goal)
                        .replace("<item>", FabricItemUtil.getName(smeltGoal)));
                break;
            case ENCHANT_ITEM:
                if (index >= stage.getItemsToEnchant().size()
                        || !(stage.getItemsToEnchant().get(index) instanceof ItemStack)) return;
                    ItemStack enchGoal = (ItemStack) stage.getItemsToEnchant().get(index);
                String enchMsg = formatCompletedObjectiveMessage(player, "enchItem", goal);
                final ItemEnchantments ench = enchGoal.get(DataComponents.ENCHANTMENTS);
                if (ench != null && !ench.isEmpty()) {
                    final java.util.Map.Entry<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> first = ench.entrySet().iterator().next();
                    final Holder<Enchantment> holder = first.getKey();
                    String enchName = holder.getRegisteredName();
                    if (enchName != null && enchName.indexOf(':') != -1) {
                        enchName = enchName.substring(enchName.indexOf(':') + 1);
                    }
                    enchMsg = enchMsg.replace("<enchantment>", FabricMiscUtil.snakeCaseToUpperCamelCase(enchName))
                        .replace("<level>", toRoman(first.getValue()));
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
                sendMessage(formatCompletedObjectiveMessage(player, "brewItem", goal)
                        .replace("<item>", FabricItemUtil.getName(brewGoal))
                        .replace(" <level>", "")
                        .replace("<level>", "")
                        .replaceAll("\\s{2,}", " ").trim());
                break;
            case CONSUME_ITEM:
                if (index >= stage.getItemsToConsume().size()
                        || !(stage.getItemsToConsume().get(index) instanceof ItemStack)) return;
                    ItemStack consumeGoal = (ItemStack) stage.getItemsToConsume().get(index);
                sendMessage(formatCompletedObjectiveMessage(player, "consumeItem", goal)
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
                sendMessage(formatCompletedObjectiveMessage(player, "deliver", goal)
                        .replace("<item>", FabricItemUtil.getName(deliverGoal))
                        .replace("<npc>", npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?"));
                break;
            case KILL_MOB:
                if (index >= stage.getMobsToKill().size()) return;
                sendMessage(formatCompletedObjectiveMessage(player, "kill", goal)
                        .replace("<mob>", FabricMiscUtil.snakeCaseToUpperCamelCase(
                                stage.getMobsToKill().get(index).toString())));
                break;
            case KILL_NPC:
                if (index >= stage.getNpcsToKill().size()) return;
                final UUID npcKillUuid = stage.getNpcsToKill().get(index);
                sendMessage(formatCompletedObjectiveMessage(player, "kill", goal)
                        .replace("<mob>", npcKillUuid != null
                                ? plugin.getDependencies().getNpcName(npcKillUuid) : "?"));
                break;
            case KILL_PLAYER:
                sendMessage(formatCompletedObjectiveMessage(player, "killPlayer", goal));
                break;
            case TALK_TO_NPC:
                if (index >= stage.getNpcsToInteract().size()) return;
                final UUID npcInteractUuid = stage.getNpcsToInteract().get(index);
                sendMessage(formatCompletedObjectiveMessage(player, "talkTo", goal)
                        .replace("<npc>", npcInteractUuid != null
                                ? plugin.getDependencies().getNpcName(npcInteractUuid) : "?"));
                break;
            case TAME_MOB:
                if (index >= stage.getMobsToTame().size()) return;
                sendMessage(formatCompletedObjectiveMessage(player, "tame", goal)
                        .replace("<mob>", FabricMiscUtil.snakeCaseToUpperCamelCase(
                                stage.getMobsToTame().get(index).toString())));
                break;
            case CATCH_FISH:
                sendMessage(formatCompletedObjectiveMessage(player, "catchFish", goal));
                break;
            case MILK_COW:
                sendMessage(formatCompletedObjectiveMessage(player, "milkCow", goal));
                break;
            case SHEAR_SHEEP:
                if (index >= stage.getSheepToShear().size()) return;
                sendMessage(formatCompletedObjectiveMessage(player, "shearSheep", goal)
                        .replace("<color>", FabricMiscUtil.snakeCaseToUpperCamelCase(
                                stage.getSheepToShear().get(index).toString())));
                break;
            case REACH_LOCATION:
                if (index >= stage.getLocationsToReach().size()) return;
                final String locationName = (stage.getLocationNames() != null && stage.getLocationNames().size() > index
                        && stage.getLocationNames().get(index) != null) ? stage.getLocationNames().get(index) : "?";
                sendMessage(formatCompletedObjectiveMessage(player, "goTo", goal)
                        .replace("<location>", locationName));
                break;
            case PASSWORD:
                if (index >= stage.getPasswordDisplays().size()) return;
                sendMessage(ChatFormatting.GREEN + "(" + FabricLang.get(player, "completed") + ") "
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
                sendMessage(FabricConfigUtil.parseString(ChatFormatting.GREEN + "("
                        + FabricLang.get(player, "completed") + ") " + custom.trim()));
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

    /**
     * Show current objectives for a quest, if applicable<p>
     *
     * Respects PlaceholderAPI and translations, when enabled.
     *
     * @param quest The quest to get current stage objectives of
     * @param quester The player to show current stage objectives to
     * @param ignoreOverrides Whether to ignore objective-overrides
     */
    public void showCurrentObjectives(final Quest quest, final Quester quester, final boolean ignoreOverrides) {
        final LinkedList<Objective> objectives = getCurrentObjectives(quest, ignoreOverrides, true);
        if (objectives.isEmpty()) return;
        for (final Objective obj : objectives) {
            final ChatFormatting color = obj.getProgress() < obj.getGoal()
                    ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            sendMessage("- " + color + obj.getMessage());
        }
    }

    /**
     * Check if player's current stage has the specified objective
     *
     * @param quest The quest to check objectives of
     * @param type The type of objective to check for
     * @return true if stage contains specified objective
     */
    public boolean hasObjective(final Quest quest, final ObjectiveType type) {
        if (quest == null || type == null) return false;
        final Stage stage = getCurrentStage(quest);
        return stage != null && stage.containsObjective(type);
    }

    /**
     * Check if player's current stage has the specified custom objective
     *
     * @param quest The quest to check custom objectives of
     * @param name The exact name of custom objective to check for
     * @return true if stage contains specified objective
     */
    public boolean hasCustomObjective(final Quest quest, final String name) {
        if (quest == null || name == null) return false;
        final Stage stage = getCurrentStage(quest);
        return stage != null && stage.getCustomObjectives().stream()
                .anyMatch(co -> co.getName().equals(name));
    }

    /**
     * Show all of a player's conditions for the current stage of a quest.<p>
     *
     * @param quest The quest to get current stage objectives of
     * @param quester The player to show current stage objectives to
     */
    public void showCurrentConditions(final Quest quest, final Quester quester) {
        if (quest == null || quester == null) return;
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null) return;
        final Condition condition = stage.getCondition();
        if (condition == null) return;
        sendMessage(ChatFormatting.GOLD + "--- Condition ---");
        final boolean met = condition.check(quester, quest);
        sendMessage((met ? ChatFormatting.GREEN : ChatFormatting.RED) + " "
                + (condition.getName() != null ? condition.getName() : "Unknown"));
    }

    /**
     * Check whether this Quester has completed all objectives for their current stage
     *
     * @param quest The quest with the current stage being checked
     * @return true if all stage objectives are marked complete
     */
    public boolean testComplete(final Quest quest) {
        if (quest == null || !currentQuests.containsKey(quest)) return false;
        final FabricStage stage = (FabricStage) getCurrentStage(quest);
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
            final int goal = getBlockAmount(stage.getBlocksToDamageAmounts(), i);
            if (progress.getBlocksDamaged().get(i) < goal) return false;
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

        // Locations reached
        for (int i = 0; i < stage.getLocationsToReach().size(); i++) {
            if (progress.getLocationsReached().size() <= i) return false;
            if (!progress.getLocationsReached().get(i)) return false;
        }

        // Passwords said
        for (int i = 0; i < stage.getPasswordPhrases().size(); i++) {
            if (progress.getPasswordsSaid().size() <= i) return false;
            if (!progress.getPasswordsSaid().get(i)) return false;
        }

        return true;
    }

    /**
     * Add empty map values per Quest stage
     *
     * @param quest Quest with at least one stage
     * @param stage Where first stage is '0'
     */
    public void addEmptiesFor(final Quest quest, final int stage) {
        final QuestProgress data = new FabricQuestProgress(this);
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

    /**
     * Save data of the Quester to file
     *
     * @return true if successful
     */
    public boolean saveData() {
        hasData = true;
        try {
            plugin.getStorage().saveQuester(this).get();
        } catch (final Exception e) {
            FabricQuestsPlugin.LOGGER.error("Failed to save quester data for " + id, e);
            return false;
        }
        return true;
    }

    /**
     * Get the difference between System.currentTimeMillis() and the last completed time for a quest
     *
     * @param quest The quest to get the last completed time of
     * @return Difference between now and then in milliseconds
     */
    public long getCompletionDifference(final Quest quest) {
        if (quest == null) return 0;
        final Long completedTime = completedTimes.get(quest);
        if (completedTime == null) return 0;
        return System.currentTimeMillis() - completedTime;
    }

    /**
     * Get the amount of time left before Quester may take a completed quest again
     *
     * @param quest The quest to calculate the cooldown for
     * @return Length of time in milliseconds
     */
    public long getRemainingCooldown(final Quest quest) {
        if (quest == null || !quest.getPlanner().hasCooldown()) return 0;
        final long diff = getCompletionDifference(quest);
        final long cooldown = quest.getPlanner().getCooldown();
        if (diff >= cooldown) return 0;
        return cooldown - diff;
    }

    /**
     * Load data of the Quester from storage
     *
     * @deprecated Use {@link #hasData()}
     * @return true if successful
     */
    @Deprecated
    public boolean loadData() {
        return plugin.getStorage().loadQuester(id) != null;
    }

    /**
     * Check whether the Quester has data saved to hard storage
     *
     * @return true if successful
     */
    public boolean hasData() {
        return plugin.getStorage().loadQuester(id) != null;
    }

    /**
     * Check whether the Quester has base data in memory, indicating they have participated in quests
     *
     * @return false if empty
     */
    public boolean hasBaseData() {
        return !currentQuests.isEmpty() || !questProgress.isEmpty() || !completedQuests.isEmpty();
    }

    /**
     * Initiate the stage timer
     * @param quest The quest of which the timer is for
     */
    public void startStageTimer(final Quest quest) {
        if (quest == null) return;
        final Stage stage = getCurrentStage(quest);
        if (stage == null || stage.getDelay() < 0) return;
        final QuestProgress progress = getQuestProgressOrDefault(quest);
        final long delay = progress.getDelayTimeLeft() > -1 ? progress.getDelayTimeLeft() : stage.getDelay();
        FabricScheduler.runLater(new FabricStageTimer(plugin, this, quest), (long) (delay * 0.02));
        if (progress.getDelayTimeLeft() <= -1 && stage.getDelayMessage() != null) {
            final ServerPlayer player = getPlayer();
            if (player != null) {
                for (final String line : FabricConfigUtil.parseStringWithPossibleLineBreaks(
                        stage.getDelayMessage(), quest, player)) {
                    sendMessage(line);
                }
            }
        }
        progress.setDelayStartTime(System.currentTimeMillis());
    }

    /**
     * Pause the stage timer. Useful when a player quits
     * @param quest The quest of which the timer is for
     */
    public void stopStageTimer(final Quest quest) {
        if (quest == null) return;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return;
        final QuestProgress progress = getQuestProgressOrDefault(quest);
        final long elapsed = System.currentTimeMillis() - progress.getDelayStartTime();
        if (progress.getDelayTimeLeft() > -1) {
            progress.setDelayTimeLeft(progress.getDelayTimeLeft() - elapsed);
        } else {
            progress.setDelayTimeLeft(stage.getDelay() - elapsed);
        }
    }

    /**
     * Get remaining stage delay time
     * @param quest The quest of which the timer is for
     * @return Remaining time in milliseconds
     */
    public long getStageTime(final Quest quest) {
        if (quest == null || !currentQuests.containsKey(quest)) return 0;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return 0;
        final QuestProgress progress = getQuestProgress(quest);
        if (progress == null || progress.getDelayStartTime() <= 0) return 0;
        final long elapsed = System.currentTimeMillis() - progress.getDelayStartTime();
        if (progress.getDelayTimeLeft() > -1) {
            return Math.max(0, progress.getDelayTimeLeft() - elapsed);
        }
        return Math.max(0, stage.getDelay() - elapsed);
    }

    /**
     * Check whether the provided quest is valid and, if not, inform the Quester
     *
     * @param quest The quest to check
     */
    public void checkQuest(final Quest quest) {
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
            quest.nextStage(this, true);
        }
    }

    /**
     * Show an inventory GUI with quest items to the specified player
     *
     * @param npc UUID of the NPC from which the GUI is bound
     * @param quests List of quests to use for displaying items
     */
    public void showGUIDisplay(final UUID npc, final LinkedList<Quest> quests) {
        if (npc == null || quests == null) {
            return;
        }
        final LinkedList<FabricQuest> guiQuests = new LinkedList<>();
        for (final Quest quest : quests) {
            guiQuests.add((FabricQuest) quest);
        }
        final FabricQuesterPreOpenGUIEvent preEvent = new FabricQuesterPreOpenGUIEvent(this, npc, guiQuests);
        FabricQuesterPreOpenGUIEvent.EVENT.invoker().onEvent(preEvent);
        if (preEvent.isCancelled()) {
            return;
        }
        if (quests.isEmpty()) {
            sendMessage(FabricLang.get(getPlayer(), "noCurrentQuest"));
            return;
        }
        final ServerPlayer player = getPlayer();
        if (player == null) {
            return;
        }
        final int rows = Math.min(6, quests.size() / 9 + 1);
        final QuestMenuContainer container = new QuestMenuContainer(rows * 9);
        int slot = 0;
        for (final Quest quest : quests) {
            final FabricQuest fabricQuest = (FabricQuest) quest;
            if (fabricQuest.getGUIDisplay() == null) {
                continue;
            }
            if (slot >= container.getContainerSize()) {
                break;
            }
            container.setItem(slot++, fabricQuest.prepareDisplay(this));
        }
        final String title = FabricLang.get(player, "quests") + " | "
                + plugin.getDependencies().getNpcName(npc);
        player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                createQuestMenu(rows, id, inventory, container), Component.literal(title)));
    }

    private static ChestMenu createQuestMenu(final int rows, final int id,
                                             final net.minecraft.world.entity.player.Inventory inventory,
                                             final Container container) {
        final MenuType<ChestMenu> type;
        switch (rows) {
            case 1:
                type = MenuType.GENERIC_9x1;
                break;
            case 2:
                type = MenuType.GENERIC_9x2;
                break;
            case 3:
                type = MenuType.GENERIC_9x3;
                break;
            case 4:
                type = MenuType.GENERIC_9x4;
                break;
            case 5:
                type = MenuType.GENERIC_9x5;
                break;
            default:
                type = MenuType.GENERIC_9x6;
                break;
        }
        return new ChestMenu(type, id, inventory, container, rows);
    }

    public static final class QuestMenuContainer extends SimpleContainer {
        public QuestMenuContainer(final int size) {
            super(size);
        }
    }

    /**
     * Force Quester to quit the specified quest (canceling any timers), then update Quest Journal<p>
     *
     * Does not save changes to disk. Consider {@link #quitQuest(Quest, String)} or {@link #quitQuest(Quest, String[])}
     *
     * @param quest The quest to quit
     */
    public void hardQuit(final Quest quest) {
        try {
            if (compassTargetQuestId != null && compassTargetQuestId.equals(quest.getId())) {
                compassTargetQuestId = null;
            }
            currentQuests.remove(quest);
            questProgress.remove(quest);
            if (!timers.isEmpty()) {
                for (final Map.Entry<Runnable, Quest> entry : timers.entrySet()) {
                    if (entry.getValue().getName().equals(quest.getName())) {
                        //plugin.getScheduler().cancelTask(entry.getKey());
                        timers.remove(entry.getKey());
                    }
                }
            }
        } catch (final Exception ex) {
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
    public void hardRemove(final Quest quest) {
        if (quest == null) return;
        hardQuit(quest);
        completedQuests.remove(quest);
        completedTimes.remove(quest);
        amountsCompleted.remove(quest);
    }

    /**
     * Forcibly clear Quester's list of current quests and data, then update Quest Journal<p>
     *
     * Does not save changes to disk. Consider calling {@link #saveData()} followed by {@link #loadData()}
     */
    public void hardClear() {
        currentQuests.clear();
        completedQuests.clear();
        completedTimes.clear();
        amountsCompleted.clear();
        questProgress.clear();
        questPoints = 0;
    }

    /**
     * Forcibly set Quester's current stage, then update Quest Journal<br>
     * Does not save changes to disk. Consider calling {@link #saveData()} followed by {@link #loadData()}
     *
     * @param key The quest to set stage of
     * @param val The stage number to set
     */
    public void hardStagePut(final Quest key, final Integer val) {
        if (key != null && val != null) currentQuests.put(key, val);
    }

    /**
     * Forcibly set Quester's quest data, then update Quest Journal<p>
     *
     * Does not save changes to disk. Consider calling {@link #saveData()} followed by {@link #loadData()}
     *
     * @param key The quest to set stage of
     * @param val The data to set
     */
    public void hardDataPut(final Quest key, final QuestProgress val) {
        if (key != null && val != null) questProgress.put(key, val);
    }

    public boolean canUseCompass() {
        return compassTargetQuestId != null;
    }

    /**
     * Reset compass target to Quester's bed spawn location<p>
     *
     * Will set to Quester's spawn location if bed spawn does not exist
     */
    public void resetCompass() {
        compassTargetQuestId = null;
        final ServerPlayer player = getPlayer();
        final net.minecraft.server.MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
        if (player == null || server == null) return;
        final net.minecraft.server.level.ServerLevel overworld = server.overworld();
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket(
                net.minecraft.world.level.storage.LevelData.RespawnData.of(
                        overworld.dimension(), overworld.getRespawnData().pos(), 0f, 0f)));
    }

    /**
     * Update compass target to current stage of first available current quest, if possible
     */
    public void findCompassTarget() {
        // Here we apply this method to OPs by not checking #canUseCompass
        final ServerPlayer player = getPlayer();
        if (player == null || !plugin.getDependencies().hasPermission(id, "quests.compass")) {
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

    /**
     * Update compass target to current stage of next available current quest, if possible
     *
     * @param notify Whether to notify this quester of result
     */
    public void findNextCompassTarget(final boolean notify) {
        // Here we apply this method to OPs by not checking #canUseCompass
        final ServerPlayer player = getPlayer();
        if (player == null || !plugin.getDependencies().hasPermission(id, "quests.compass")) {
            return;
        }
        final LinkedList<String> list = currentQuests.keySet().stream()
                .sorted(Comparator.comparing(Quest::getName)).map(Quest::getId)
                .collect(Collectors.toCollection(LinkedList::new));
        int index = 0;
        if (compassTargetQuestId != null) {
            if (!list.contains(compassTargetQuestId) && notify) {
                return;
            }
            index = list.indexOf(compassTargetQuestId) + 1;
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
                        sendMessage(ChatFormatting.YELLOW
                                + FabricLang.get(getPlayer(), "compassSet").replace("<quest>", quest.getName()));
                    }
                } else {
                    resetCompass();
                    setCompassTarget(quest);
                    if (notify) {
                        sendMessage(ChatFormatting.RED
                                + FabricLang.get(getPlayer(), "compassNone").replace("<quest>", quest.getName()));
                    }
                }
            }
        } else {
            sendMessage(ChatFormatting.RED + FabricLang.get(getPlayer(), "journalNoQuests")
                    .replace("<journal>", FabricLang.get(getPlayer(), "journalTitle")));
        }
    }

    /** Point the player's compass at the first location of the given stage that has not been reached yet. */
    private void pointCompass(final Quest quest, final Stage stage) {
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

    private void setPlayerCompass(final BlockPos pos) {
        if (pos == null) return;
        final ServerPlayer player = getPlayer();
        if (player == null) return;
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket(
                net.minecraft.world.level.storage.LevelData.RespawnData.of(
                        player.level().dimension(), pos, 0f, 0f)));
    }

    private static BlockPos parseLocationBlock(final Object locObj) {
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

    /**
     * Dispatch player event to fellow questers<p>
     *
     * @param type The type of objective to progress
     * @param fun The function to execute, the event call
     */
    public Collection<String> dispatchMultiplayerEverything(final Quest quest, final ObjectiveType type,
                                                            final BiFunction<Quester, Quest, Void> fun) {
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

    /**
     * Dispatch finish objective to fellow questers
     *
     * @param quest The current quest
     * @param currentStage The current stage of the quest
     * @param fun The function to execute, the event call
     * @return applied quest IDs
     */
    public Collection<String> dispatchMultiplayerObjectives(final Quest quest, final Stage currentStage,
                                                            final Function<Quester, Void> fun) {
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

    /**
     * Get a collection of fellow Questers in a party or group
     *
     * @param quest The quest which uses a linked plugin, i.e. Parties or DungeonsXL
     * @return Potentially empty list of Questers or null for invalid quest
     */
    public Collection<Quester> getMultiplayerQuesters(final Quest quest) {
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

    /**
     * Whether this Quester meets condition of given quest
     *
     * @param quest The quest to check
     * @return Whether to send Quester reason for failure
     */
    public boolean meetsCondition(final Quest quest, final boolean giveReason) {
        if (quest == null || !currentQuests.containsKey(quest)) return true;
        final Stage stage = getCurrentStage(quest);
        if (stage == null) return true;
        final Condition condition = stage.getCondition();
        if (condition != null && !condition.check(this, quest)) {
            if (condition.isFailQuest()) {
                if (giveReason) {
                    sendMessage(ChatFormatting.RED + FabricLang.get(getPlayer(), "conditionFailQuit")
                            .replace("<quest>", quest.getName()));
                }
                if (stage.getFailAction() != null) {
                    stage.getFailAction().fire(this, quest);
                }
                hardQuit(quest);
            } else if (giveReason && System.currentTimeMillis() - lastNotifiedCondition
                    > (plugin.getConfigSettings().getConditionInterval() * 1000L)) {
                sendMessage(ChatFormatting.YELLOW + FabricLang.get(getPlayer(), "conditionFailRetry")
                        .replace("<quest>", quest.getName()));
                lastNotifiedCondition = System.currentTimeMillis();
            }
            return false;
        }
        return true;
    }

    /**
     * Whether this Quester is currently selecting a block in editor
     *
     * @return true if selecting
     */
    public boolean isSelectingBlock() {
        return plugin.isSelectingBlockStart(id) || plugin.getTempBlocks().containsKey(id);
    }

    /**
     * Whether this Quester is in the specified WorldGuard region
     *
     * @return true if in specified WorldGuard region
     */
    public boolean isInRegion(final String regionID) {
        final ServerPlayer player = getPlayer();
        if (player == null) return false;
        return plugin.getDependencies().isInRegion(player, regionID);
    }

    /**
     * Checks whether an NPC can offer a quest that the player may accept
     *
     * @param npc the giver NPC UUID to check
     * @return true if at least one available quest has not yet been completed
     */
    public boolean canAcceptQuest(final UUID npc) {
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

    /**
     * Checks whether an NPC can offer a quest that the player has already completed
     *
     * @param npc The giver NPC UUID to check
     * @return true if at least one available quest has been completed
     */
    public boolean canAcceptCompletedQuest(final UUID npc) {
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

    /**
     * Checks whether an NPC can offer a repeatable quest that the player has already completed
     *
     * @param npc The giver NPC UUID to check
     * @return true if at least one available, redoable quest has been completed
     */
    public boolean canAcceptCompletedRedoableQuest(final UUID npc) {
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
}

