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
import me.pikamug.quests.conditions.FabricCondition;
import me.pikamug.quests.convo.misc.FabricQuestAbandonPrompt;
import me.pikamug.quests.convo.misc.FabricQuestAcceptPrompt;
import me.pikamug.quests.dependencies.FabricDependencies;
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
import me.pikamug.quests.util.RomanNumeral;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.LevelData;
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

    public static final class QuestMenuContainer extends SimpleContainer {
        public QuestMenuContainer(final int size) {
            super(size);
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

    public QuestProgress getQuestProgress(final Quest quest) {
        return questProgress.get(quest);
    }

    public void setQuestProgress(final ConcurrentHashMap<Quest, QuestProgress> questProgress) {
        this.questProgress = questProgress;
    }

    public void setQuestProgress(final Quest quest, final QuestProgress progress) {
        questProgress.put(quest, progress);
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
        final FabricDependencies depends = plugin.getDependencies();
        final QuestProgress data = getQuestProgressOrDefault(quest);
        final LinkedList<String> overrides = stage.getObjectiveOverrides();
        int objIndex = -1;
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
        for (int i = 0; i < data.getBlocksDamaged().size(); i++) {
            objIndex++;
            final int progress = data.getBlocksDamaged().get(i);
            if (i >= stage.getBlocksToDamage().size()) { break; }
            final ItemStack goal = stage.getBlocksToDamage().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "damage"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            objectives.add(new FabricObjective(ObjectiveType.DAMAGE_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getBlocksPlaced().size(); i++) {
            objIndex++;
            final int progress = data.getBlocksPlaced().get(i);
            if (i >= stage.getBlocksToPlace().size()) { break; }
            final ItemStack goal = stage.getBlocksToPlace().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "place"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            objectives.add(new FabricObjective(ObjectiveType.PLACE_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getBlocksUsed().size(); i++) {
            objIndex++;
            final int progress = data.getBlocksUsed().get(i);
            if (i >= stage.getBlocksToUse().size()) { break; }
            final ItemStack goal = stage.getBlocksToUse().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "use"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            objectives.add(new FabricObjective(ObjectiveType.USE_BLOCK, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsCrafted().size(); i++) {
            objIndex++;
            final int progress = data.getItemsCrafted().get(i);
            if (i >= stage.getItemsToCraft().size()) { break; }
            final ItemStack goal = stage.getItemsToCraft().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "craftItem"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            objectives.add(new FabricObjective(ObjectiveType.CRAFT_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsSmelted().size(); i++) {
            objIndex++;
            final int progress = data.getItemsSmelted().get(i);
            if (i >= stage.getItemsToSmelt().size()) { break; }
            final ItemStack goal = stage.getItemsToSmelt().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "smeltItem"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            objectives.add(new FabricObjective(ObjectiveType.SMELT_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsEnchanted().size(); i++) {
            objIndex++;
            final int progress = data.getItemsEnchanted().get(i);
            if (i >= stage.getItemsToEnchant().size()) { break; }
            final ItemStack goal = stage.getItemsToEnchant().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "enchItem"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            final ItemEnchantments ench = goal.get(DataComponents.ENCHANTMENTS);
            if (ench == null || ench.isEmpty()) {
                objectives.add(new FabricObjective(ObjectiveType.ENCHANT_ITEM,
                        message.replace("<enchantment>", "")
                                .replace("<level>", "")
                                .replaceAll("\\s+", " "), progress, goal));
            } else {
                for (final Map.Entry<Holder<Enchantment>, Integer> e : ench.entrySet()) {
                    String enchName = e.getKey().getRegisteredName();
                    if (enchName != null && enchName.indexOf(':') != -1) {
                        enchName = enchName.substring(enchName.indexOf(':') + 1);
                    }
                    objectives.add(new FabricObjective(ObjectiveType.ENCHANT_ITEM,
                            message.replace("<enchantment>", enchName != null
                                            ? FabricMiscUtil.snakeCaseToUpperCamelCase(enchName) : "")
                                    .replace("<level>", RomanNumeral.getNumeral(e.getValue())), progress, goal));
                }
            }
        }
        for (int i = 0; i < data.getItemsBrewed().size(); i++) {
            objIndex++;
            final int progress = data.getItemsBrewed().get(i);
            if (i >= stage.getItemsToBrew().size()) { break; }
            final ItemStack goal = stage.getItemsToBrew().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "brewItem"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            final String level = FabricItemUtil.getPrettyPotionLevel(goal);
            if (level.isEmpty()) {
                message = message.replace(" <level>", level);
            } else {
                message = message.replace("<level>", level);
            }
            objectives.add(new FabricObjective(ObjectiveType.BREW_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsConsumed().size(); i++) {
            objIndex++;
            final int progress = data.getItemsConsumed().get(i);
            if (i >= stage.getItemsToConsume().size()) { break; }
            final ItemStack goal = stage.getItemsToConsume().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "consumeItem"),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            final String level = FabricItemUtil.getPrettyPotionLevel(goal);
            if (level.isEmpty()) {
                message = message.replace(" <level>", level);
            } else {
                message = message.replace("<level>", level);
            }
            objectives.add(new FabricObjective(ObjectiveType.CONSUME_ITEM, message, progress, goal));
        }
        for (int i = 0; i < data.getItemsDelivered().size(); i++) {
            objIndex++;
            final int progress = data.getItemsDelivered().get(i);
            if (i >= stage.getItemsToDeliver().size()) { break; }
            final ItemStack goal = stage.getItemsToDeliver().get(i);
            final UUID npc = stage.getItemDeliveryTargets().get(i);
            final ChatFormatting color = progress < goal.getCount() ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal.getCount());
            } else {
                message = formatCurrentObjectiveMessage(color,
                        FabricLang.get(getPlayer(), "deliver").replace("<npc>",
                                plugin.getDependencies().getNpcName(npc)),
                        progress, goal.getCount());
            }
            if (formatNames) {
                message = message.replace("<item>", FabricItemUtil.getName(goal));
            }
            objectives.add(new FabricObjective(ObjectiveType.DELIVER_ITEM, message, progress, goal));
        }
        int interactIndex = 0;
        for (final UUID n : stage.getNpcsToInteract()) {
            objIndex++;
            if (data.getNpcsInteracted().size() > interactIndex) {
                final boolean progress = data.getNpcsInteracted().get(interactIndex);
                final ChatFormatting color = !progress ? ChatFormatting.GREEN : ChatFormatting.GRAY;
                String message;
                if (!ignoreOverrides && objIndex < overrides.size()) {
                    message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress ? 1 : 0, 1);
                } else {
                    message = color + FabricLang.get(getPlayer(), "talkTo")
                            .replace("<npc>", depends.getNpcName(n));
                }
                if (depends.getPlaceholderApi() != null) {
                    message = FabricPlaceholderSupport.setPlaceholders(getPlayer(), message);
                }
                objectives.add(new FabricObjective(ObjectiveType.TALK_TO_NPC, message, progress ? 1 : 0, 1));
            }
            interactIndex++;
        }
        int npcKillIndex = 0;
        for (final UUID n : stage.getNpcsToKill()) {
            objIndex++;
            int progress = 0;
            if (data.getNpcsNumKilled().size() > npcKillIndex) {
                progress = data.getNpcsNumKilled().get(npcKillIndex);
            }
            final int goal = stage.getNpcNumToKill().get(npcKillIndex);
            final ChatFormatting color = progress < goal ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "kill"),
                        progress, goal);
            }
            if (message.contains("<mob>")) {
                message = message.replace("<mob>", depends.getNpcName(n));
            } else {
                message += " " + depends.getNpcName(n);
            }
            objectives.add(new FabricObjective(ObjectiveType.KILL_NPC, message, progress, goal));
            npcKillIndex++;
        }
        int mobKillIndex = 0;
        for (final EntityType<?> e : stage.getMobsToKill()) {
            objIndex++;
            int progress = 0;
            if (data.getMobNumKilled().size() > mobKillIndex) {
                progress = data.getMobNumKilled().get(mobKillIndex);
            }
            final int goal = stage.getMobNumToKill().get(mobKillIndex);
            final ChatFormatting color = progress < goal ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message = color + "";
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                if (stage.getLocationsToKillWithin().isEmpty()) {
                    message += FabricLang.get(getPlayer(), "kill");
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + progress + "/" + goal);
                    }
                } else {
                    message += FabricLang.get(getPlayer(), "killAtLocation").replace("<location>",
                            stage.getKillNames().get(stage.getMobsToKill().indexOf(e)));
                    if (message.contains("<count>")) {
                        message = message.replace("<count>", "" + color + progress + "/" + goal);
                    } else {
                        message += color + ": " + progress + "/" + goal;
                    }
                }
            }
            if (depends.getPlaceholderApi() != null) {
                message = FabricPlaceholderSupport.setPlaceholders(getPlayer(), message);
            }
            if (formatNames) {
                message = message.replace("<mob>", FabricMiscUtil.getPrettyMobName(e));
            }
            objectives.add(new FabricObjective(ObjectiveType.KILL_MOB, message, progress, goal, e));
            mobKillIndex++;
        }
        int tameIndex = 0;
        for (final EntityType<?> e : stage.getMobsToTame()) {
            objIndex++;
            int progress = 0;
            if (data.getMobsTamed().size() > tameIndex) {
                progress = data.getMobsTamed().get(tameIndex);
            }
            final int goal = stage.getMobNumToTame().get(tameIndex);
            final ChatFormatting color = progress < goal ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "tame"),
                        progress, goal);
            }
            if (!message.contains("<mob>")) {
                message += " <mob>";
            }
            if (formatNames) {
                message = message.replace("<mob>", FabricMiscUtil.getPrettyMobName(
                        stage.getMobsToTame().get(tameIndex)));
            }
            objectives.add(new FabricObjective(ObjectiveType.TAME_MOB, message, progress, goal, e));
            tameIndex++;
        }
        if (stage.getFishToCatch() != null) {
            objIndex++;
            final int progress = data.getFishCaught();
            final int goal = stage.getFishToCatch();
            final ChatFormatting color = progress < goal ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "catchFish"),
                        progress, goal);
            }
            objectives.add(new FabricObjective(ObjectiveType.CATCH_FISH, message, progress, goal));
        }
        if (stage.getCowsToMilk() != null) {
            objIndex++;
            final int progress = data.getCowsMilked();
            final int goal = stage.getCowsToMilk();
            final ChatFormatting color = progress < goal ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "milkCow"),
                        progress, goal);
            }
            objectives.add(new FabricObjective(ObjectiveType.MILK_COW, message, progress, goal));
        }
        int shearIndex = 0;
        for (final int goal : stage.getSheepNumToShear()) {
            objIndex++;
            int progress = 0;
            if (data.getSheepSheared().size() > shearIndex) {
                progress = data.getSheepSheared().get(shearIndex);
            }
            final ChatFormatting color = progress < goal ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "shearSheep"),
                        progress, goal);
            }
            message = message.replace("<color>", FabricMiscUtil.getPrettyDyeColorName(
                    stage.getSheepToShear().get(shearIndex)));
            objectives.add(new FabricObjective(ObjectiveType.SHEAR_SHEEP, message, progress, goal));
            shearIndex++;
        }
        if (stage.getPlayersToKill() != null) {
            objIndex++;
            final int progress = data.getPlayersKilled();
            final int goal = stage.getPlayersToKill();
            final ChatFormatting color = data.getPlayersKilled() < stage.getPlayersToKill() ? ChatFormatting.GREEN
                    : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "killPlayer"),
                        progress, goal);
            }
            objectives.add(new FabricObjective(ObjectiveType.KILL_PLAYER, message, progress, goal));
        }
        for (int i = 0; i < stage.getLocationsToReach().size(); i++) {
            objIndex++;
            if (i < data.getLocationsReached().size()) {
                final boolean progress = data.getLocationsReached().get(i);
                final ChatFormatting color = !progress ? ChatFormatting.GREEN : ChatFormatting.GRAY;
                String message;
                if (!ignoreOverrides && objIndex < overrides.size()) {
                    message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress ? 1 : 0, 1);
                } else {
                    message = formatCurrentObjectiveMessage(color, FabricLang.get(getPlayer(), "goTo"),
                            progress ? 1 : 0, 1);
                }
                message = message.replace("<location>", stage.getLocationNames().get(i));
                objectives.add(new FabricObjective(ObjectiveType.REACH_LOCATION, message, progress ? 1 : 0, 1));
            }
        }
        int passIndex = 0;
        for (final String s : stage.getPasswordDisplays()) {
            objIndex++;
            boolean progress = false;
            if (data.getPasswordsSaid().size() > passIndex) {
                progress = data.getPasswordsSaid().get(passIndex);
            }
            final ChatFormatting color = !progress ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress ? 1 : 0, 1);
            } else {
                message = formatCurrentObjectiveMessage(color, s, progress ? 1 : 0, 1);
            }
            objectives.add(new FabricObjective(ObjectiveType.PASSWORD, message, progress ? 1 : 0, 1));
            passIndex++;
        }
        int customIndex = 0;
        for (final CustomObjective co : stage.getCustomObjectives()) {
            objIndex++;
            int progress = 0;
            if (data.getCustomObjectiveCounts().size() > customIndex) {
                progress = data.getCustomObjectiveCounts().get(customIndex);
            }
            final int goal = stage.getCustomObjectiveCounts().get(customIndex);
            final ChatFormatting color = progress < goal ? ChatFormatting.GREEN : ChatFormatting.GRAY;
            String message;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                message = formatCurrentObjectiveMessage(color, overrides.get(objIndex), progress, goal);
            } else {
                message = formatCurrentObjectiveMessage(color, co.getDisplay(), progress, goal);
            }
            for (final Map.Entry<String, Object> prompt : co.getData()) {
                final String replacement = "%" + prompt.getKey() + "%";
                try {
                    for (final Map.Entry<String, Object> e : stage.getCustomObjectiveData()) {
                        if (e.getKey().equals(prompt.getKey())) {
                            if (message.contains(replacement)) {
                                message = message.replace(replacement, String.valueOf(e.getValue()));
                            }
                        }
                    }
                } catch (final NullPointerException ne) {
                    plugin.getPluginLogger().severe("Unable to gather display for " + co.getName() + " on "
                            + quest.getName());
                    ne.printStackTrace();
                }
            }
            if (co.canShowCount()) {
                message = message.replace("%count%", progress + "/" + goal);
            }
            message = FabricConfigUtil.parseString(message.trim().replaceAll("\\s{2,}", ""));
            objectives.add(new FabricObjective(ObjectiveType.CUSTOM, message, progress, goal));
            customIndex++;
        }
        /*for (int i = 0; i < objectives.size(); i++) {
            final Objective objective = objectives.get(i);
            String message = objective.getMessage();
            if (message != null) {
                message = FabricConfigUtil.parseString(message.trim().replaceAll("\\s{2,}", ""));
                objectives.set(i, new FabricObjective(objective.getType(), message, objective.getProgress(),
                        objective.getGoal()));
            }
        }*/
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
        if (quest == null) {
            plugin.getPluginLogger().severe("Quest was null when showing objectives for " + getLastKnownName());
            return;
        }
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null) {
            plugin.getPluginLogger().warning("Current stage was null when showing objectives for " + quest.getName());
            return;
        }
        final LinkedList<String> overrides = stage.getObjectiveOverrides();
        int objIndex = -1;
        for (final Objective obj : getCurrentObjectives(quest, false, false)) {
            objIndex++;
            if (!ignoreOverrides && objIndex < overrides.size()) {
                final String s = overrides.get(objIndex);
                String message = (!s.trim().isEmpty() ? "- " : "") + ChatFormatting.GREEN
                        + FabricConfigUtil.parseString(s, quest, getPlayer());
                message = message.replace("<count>", obj.getProgress() + "/" + obj.getGoal());
                message = message.replace("%count%", obj.getProgress() + "/" + obj.getGoal());
                if (plugin.getDependencies().getPlaceholderApi() != null) {
                    message = FabricPlaceholderSupport.setPlaceholders(getPlayer(), message);
                }
                quester.sendMessage(message);
                continue;
            }
            String message = "- " + FabricLang.convertString(obj.getMessage());
            if (obj.getGoalObject() instanceof ItemStack) {
                message = message.replace("<item>", FabricItemUtil.getName((ItemStack) obj.getGoalObject()));
            } else if (obj.getType() == ObjectiveType.KILL_MOB || obj.getType() == ObjectiveType.TAME_MOB) {
                message = message.replace("<mob>", FabricMiscUtil.getPrettyMobName(
                        (EntityType<?>) obj.getGoalObject()));
            }
            if (obj.getType() == ObjectiveType.CUSTOM) {
                message = message.trim().replaceAll("\\s{2,}", "");
            }
            quester.sendMessage(message);
            if (!ignoreOverrides && !overrides.isEmpty()) {
                for (int i = objIndex; i < overrides.size(); i++) {
                    String msg = ChatFormatting.GREEN + FabricConfigUtil.parseString(overrides.get(i), quest, getPlayer());
                    if (plugin.getDependencies().getPlaceholderApi() != null) {
                        msg = FabricPlaceholderSupport.setPlaceholders(getPlayer(), msg);
                    }
                    quester.sendMessage(msg);
                }
            }
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
        if (quest == null || getCurrentStage(quest) == null || type == null) {
            return false;
        }
        return getCurrentStage(quest).containsObjective(type);
    }

    /**
     * Check if player's current stage has the specified custom objective
     *
     * @param quest The quest to check custom objectives of
     * @param name The exact name of custom objective to check for
     * @return true if stage contains specified objective
     */
    public boolean hasCustomObjective(final Quest quest, final String name) {
        if (quest == null || getCurrentStage(quest) == null || name == null) {
            return false;
        }
        for (final CustomObjective co : getCurrentStage(quest).getCustomObjectives()) {
            if (co.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show all of a player's conditions for the current stage of a quest.<p>
     *
     * @param quest The quest to get current stage objectives of
     * @param quester The player to show current stage objectives to
     */
    public void showCurrentConditions(final Quest quest, final Quester quester) {
        if (quest == null) {
            plugin.getPluginLogger().severe("Quest was null when getting conditions for " + quester.getLastKnownName());
            return;
        }
        if (quester.getQuestProgressOrDefault(quest) == null) {
            plugin.getPluginLogger().warning("Quest data was null when showing conditions for " + quest.getName());
            return;
        }
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null) {
            plugin.getPluginLogger().warning("Current stage was null when showing conditions for " + quest.getName());
            return;
        }
        final FabricCondition c = (FabricCondition) stage.getCondition();
        if (c != null && stage.getObjectiveOverrides().isEmpty()) {
            quester.sendMessage(ChatFormatting.LIGHT_PURPLE + FabricLang.get("stageEditorConditions"));
            final StringBuilder msg = new StringBuilder("- " + ChatFormatting.YELLOW);
            if (!c.getEntitiesWhileRiding().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorRideEntity"));
                for (final String e : c.getEntitiesWhileRiding()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(e);
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getNpcsWhileRiding().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorRideNPC"));
                for (final UUID u : c.getNpcsWhileRiding()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ");
                    String name = u.toString();
                    for (final FabricNpcDependency npcDependency : plugin.getDependencies().getNpcDependencies()) {
                        final String npcName = npcDependency.getName(u);
                        if (npcName != null) {
                            name = npcName;
                            break;
                        }
                    }
                    msg.append(name);
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getPermissions().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorPermissions"));
                for (final String e : c.getPermissions()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(e);
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getItemsWhileHoldingMainHand().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorItemsInMainHand"));
                for (final ItemStack is : c.getItemsWhileHoldingMainHand()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(FabricItemUtil.getPrettyItemName(is
                            .getItemName().toString()));
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getItemsWhileWearing().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorItemsWear"));
                for (final ItemStack is : c.getItemsWhileWearing()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(FabricItemUtil.getPrettyItemName(is
                            .getItemName().toString()));
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getWorldsWhileStayingWithin().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorStayWithinWorld"));
                for (final String w : c.getWorldsWhileStayingWithin()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(w);
                }
                quester.sendMessage(msg.toString());
            } else if (c.getTickStartWhileStayingWithin() > -1 && c.getTickEndWhileStayingWithin() > -1) {
                msg.append(FabricLang.get("conditionEditorStayWithinTicks"));
                msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(c.getTickStartWhileStayingWithin())
                        .append(" - ").append(c.getTickEndWhileStayingWithin());
                quester.sendMessage(msg.toString());
            } else if (!c.getBiomesWhileStayingWithin().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorStayWithinBiome"));
                for (final String b : c.getBiomesWhileStayingWithin()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(FabricMiscUtil
                            .snakeCaseToUpperCamelCase(b));
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getRegionsWhileStayingWithin().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorStayWithinRegion"));
                for (final String r : c.getRegionsWhileStayingWithin()) {
                    msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(r);
                }
                quester.sendMessage(msg.toString());
            } else if (!c.getPlaceholdersCheckIdentifier().isEmpty()) {
                msg.append(FabricLang.get("conditionEditorCheckPlaceholder"));
                int index = 0;
                for (final String r : c.getPlaceholdersCheckIdentifier()) {
                    if (c.getPlaceholdersCheckValue().size() > index) {
                        msg.append(ChatFormatting.AQUA).append("\n   \u2515 ").append(r).append(ChatFormatting.GRAY)
                                .append(" = ").append(ChatFormatting.AQUA).append(c.getPlaceholdersCheckValue()
                                        .get(index));
                    }
                    index++;
                }
                quester.sendMessage(msg.toString());
            }
        }
    }

    /**
     * Complete a quest objective
     *
     * @param quest Quest containing the objective
     * @param objective Objective for type, progress and goal
     * @param index Current objective index
     */
    public void finishObjective(final Quest quest, final Objective objective, final int index) {
        if (objective == null) {
            return;
        }
        final ServerPlayer p = getPlayer();
        final ObjectiveType type = objective.getType();
        final ItemStack goal = objective.getGoalObject() instanceof ItemStack ? (ItemStack) objective.getGoalObject()
                : new ItemStack(Items.DIRT, objective.getGoal());
        final Stage stage = getCurrentStage(quest);

        if (!stage.getObjectiveOverrides().isEmpty()) {
            for (final String s : getCurrentStage(quest).getObjectiveOverrides()) {
                String message = ChatFormatting.GREEN + "(" + FabricLang.get(p, "completed") + ") "
                        + FabricConfigUtil.parseString(s, quest, p);
                message = message.replace("<count>", objective.getProgress() + "/" + objective.getGoal());
                message = message.replace("%count%", objective.getProgress() + "/" + objective.getGoal());
                sendMessage(message);
            }
            return;
        }

        switch (type) {
            case BREAK_BLOCK:
                sendMessage(formatCompletedObjectiveMessage("break", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal)));
                break;
            case PLACE_BLOCK:
                sendMessage(formatCompletedObjectiveMessage("place", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal)));
                break;
            case DAMAGE_BLOCK:
                sendMessage(formatCompletedObjectiveMessage("damage", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal)));
                break;
            case USE_BLOCK:
                sendMessage(formatCompletedObjectiveMessage("use", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal)));
                break;
            case CRAFT_ITEM:
                sendMessage(formatCompletedObjectiveMessage("craftItem", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal)));
                break;
            case SMELT_ITEM:
                sendMessage(formatCompletedObjectiveMessage("smeltItem", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal)));
                break;
            case ENCHANT_ITEM:
                String enchMsg = formatCompletedObjectiveMessage("enchItem", objective.getGoal());
                final ItemEnchantments ench = goal.get(DataComponents.ENCHANTMENTS);
                if (ench != null && !ench.isEmpty()) {
                    final java.util.Map.Entry<net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment>, Integer> first = ench.entrySet().iterator().next();
                    final Holder<Enchantment> holder = first.getKey();
                    String enchName = holder.getRegisteredName();
                    if (enchName != null && enchName.indexOf(':') != -1) {
                        enchName = enchName.substring(enchName.indexOf(':') + 1);
                    }
                    enchMsg = enchMsg.replace("<enchantment>", FabricMiscUtil.snakeCaseToUpperCamelCase(enchName))
                        .replace("<level>", RomanNumeral.getNumeral(first.getValue()));
                } else {
                    enchMsg = enchMsg.replace("<enchantment>", "").replace("<level>", "");
                }
                sendMessage(enchMsg.replace("<item>", FabricItemUtil.getName(goal))
                        .replaceAll("\\s{2,}", " ").trim());
                break;
            case BREW_ITEM:
                String brewMsg = formatCompletedObjectiveMessage("brewItem", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal));
                final String brewLevel = FabricItemUtil.getPrettyPotionLevel(goal);
                if (brewLevel.isEmpty()) {
                    brewMsg = brewMsg.replace(" <level>", brewLevel);
                } else {
                    brewMsg = brewMsg.replace("<level>", brewLevel);
                }
                sendMessage(brewMsg.replaceAll("\\s{2,}", " ").trim());
                break;
            case CONSUME_ITEM:
                String consumeMsg = formatCompletedObjectiveMessage("consumeItem", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal));
                final String consumeLevel = FabricItemUtil.getPrettyPotionLevel(goal);
                if (consumeLevel.isEmpty()) {
                    consumeMsg = consumeMsg.replace(" <level>", consumeLevel);
                } else {
                    consumeMsg = consumeMsg.replace("<level>", consumeLevel);
                }
                sendMessage(consumeMsg.replaceAll("\\s{2,}", " ").trim());
                break;
            case DELIVER_ITEM:
                final UUID npcUuid = (stage.getItemDeliveryTargets().size() > index)
                        ? stage.getItemDeliveryTargets().get(index) : null;
                sendMessage(formatCompletedObjectiveMessage("deliver", objective.getGoal())
                        .replace("<item>", FabricItemUtil.getName(goal))
                        .replace("<npc>", npcUuid != null ? plugin.getDependencies().getNpcName(npcUuid) : "?"));
                break;
            case KILL_MOB:
                sendMessage(formatCompletedObjectiveMessage("kill", objective.getGoal())
                        .replace("<mob>", FabricMiscUtil.getPrettyMobName(
                                (EntityType<?>) stage.getMobsToKill().get(index))));
                break;
            case KILL_NPC:
                final UUID npcKillUuid = stage.getNpcsToKill().get(index);
                sendMessage(formatCompletedObjectiveMessage("kill", objective.getGoal())
                        .replace("<mob>", npcKillUuid != null
                                ? plugin.getDependencies().getNpcName(npcKillUuid) : "?"));
                break;
            case KILL_PLAYER:
                sendMessage(formatCompletedObjectiveMessage("killPlayer", objective.getGoal()));
                break;
            case TALK_TO_NPC:
                if (index >= stage.getNpcsToInteract().size()) return;
                final UUID npcInteractUuid = stage.getNpcsToInteract().get(index);
                sendMessage(formatCompletedObjectiveMessage("talkTo", objective.getGoal())
                        .replace("<npc>", npcInteractUuid != null
                                ? plugin.getDependencies().getNpcName(npcInteractUuid) : "?"));
                break;
            case TAME_MOB:
                if (index >= stage.getMobsToTame().size()) return;
                sendMessage(formatCompletedObjectiveMessage("tame", objective.getGoal())
                        .replace("<mob>", FabricMiscUtil.getPrettyMobName(
                                (EntityType<?>) stage.getMobsToTame().get(index))));
                break;
            case CATCH_FISH:
                sendMessage(formatCompletedObjectiveMessage("catchFish", objective.getGoal()));
                break;
            case MILK_COW:
                sendMessage(formatCompletedObjectiveMessage("milkCow", objective.getGoal()));
                break;
            case SHEAR_SHEEP:
                if (index >= stage.getSheepToShear().size()) return;
                sendMessage(formatCompletedObjectiveMessage("shearSheep", objective.getGoal())
                        .replace("<color>", FabricMiscUtil.getPrettyDyeColorName(
                                (DyeColor) stage.getSheepToShear().get(index))));
                break;
            case REACH_LOCATION:
                if (index >= stage.getLocationsToReach().size()) return;
                final String locationName = (stage.getLocationNames() != null && stage.getLocationNames().size() > index
                        && stage.getLocationNames().get(index) != null) ? stage.getLocationNames().get(index) : "?";
                sendMessage(formatCompletedObjectiveMessage("goTo", objective.getGoal())
                        .replace("<location>", locationName));
                break;
            case PASSWORD:
                if (index >= stage.getPasswordDisplays().size()) return;
                sendMessage(ChatFormatting.GREEN + "(" + FabricLang.get(p, "completed") + ") "
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
                    custom = custom.replace("%count%", objective.getProgress() + "/" + objective.getGoal());
                }
                sendMessage(FabricConfigUtil.parseString(ChatFormatting.GREEN + "("
                        + FabricLang.get(p, "completed") + ") " + custom.trim()));
                break;
            default:
                break;
        }
    }

    /**
     * Format completed objective message with count and placeholders
     *
     * @param langKey Label as it appears in lang file
     * @param goal Objective goal
     * @return Formatted message
     */
    private String formatCompletedObjectiveMessage(final String langKey, final int goal) {
        String message = "(" + FabricLang.get(getPlayer(), "completed") + ") " + FabricLang.get(langKey);
        if (message.contains("<count>")) {
            message = message.replace("<count>", goal + "/" + goal);
        }
        if (plugin.getDependencies().getPlaceholderApi() != null) {
            message = FabricPlaceholderSupport.setPlaceholders(getPlayer(), message);
        }
        return ChatFormatting.GREEN + message;
    }

    /**
     * Check whether this Quester has completed all objectives for their current stage
     *
     * @param quest The quest with the current stage being checked
     * @return true if all stage objectives are marked complete
     */
    public boolean testComplete(final Quest quest) {
        for (final Objective objective : getCurrentObjectives(quest, true, false)) {
            if (objective.getProgress() < objective.getGoal()) {
                return false;
            }
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
        final FabricQuestProgress data = new FabricQuestProgress(this);
        data.setDoJournalUpdate(false);
        if (quest == null) {
            FabricQuestsPlugin.LOGGER.warn("Unable to find quest for player " + lastKnownName);
            return;
        }
        if (quest.getStage(stage) == null) {
            FabricQuestsPlugin.LOGGER.error("Unable to find Stage " + stage + " of quest ID " + quest.getId());
            return;
        }
        final FabricStage s = (FabricStage) quest.getStage(stage);
        if (!s.getBlocksToBreak().isEmpty()) {
            for (final ItemStack ignored : s.getBlocksToBreak()) {
                data.blocksBroken.add(0);
            }
        }
        if (!s.getBlocksToDamage().isEmpty()) {
            for (final ItemStack ignored : s.getBlocksToDamage()) {
                data.blocksDamaged.add(0);
            }
        }
        if (!s.getBlocksToPlace().isEmpty()) {
            for (final ItemStack ignored : s.getBlocksToPlace()) {
                data.blocksPlaced.add(0);
            }
        }
        if (!s.getBlocksToUse().isEmpty()) {
            for (final ItemStack ignored : s.getBlocksToUse()) {
                data.blocksUsed.add(0);
            }
        }
        if (!s.getItemsToCraft().isEmpty()) {
            for (final ItemStack ignored : s.getItemsToCraft()) {
                data.itemsCrafted.add(0);
            }
        }
        if (!s.getItemsToSmelt().isEmpty()) {
            for (final ItemStack ignored : s.getItemsToSmelt()) {
                data.itemsSmelted.add(0);
            }
        }
        if (!s.getItemsToEnchant().isEmpty()) {
            for (final ItemStack ignored : s.getItemsToEnchant()) {
                data.itemsEnchanted.add(0);
            }
        }
        if (!s.getItemsToBrew().isEmpty()) {
            for (final ItemStack ignored : s.getItemsToBrew()) {
                data.itemsBrewed.add(0);
            }
        }
        if (!s.getItemsToConsume().isEmpty()) {
            for (final ItemStack ignored : s.getItemsToConsume()) {
                data.itemsConsumed.add(0);
            }
        }
        if (!s.getItemsToDeliver().isEmpty()) {
            for (final ItemStack ignored : s.getItemsToDeliver()) {
                data.itemsDelivered.add(0);
            }
        }
        if (!s.getNpcsToInteract().isEmpty()) {
            for (final UUID ignored : s.getNpcsToInteract()) {
                data.npcsInteracted.add(false);
            }
        }
        if (!s.getNpcsToKill().isEmpty()) {
            for (final UUID ignored : s.getNpcsToKill()) {
                data.npcsNumKilled.add(0);
            }
        }
        if (!s.getMobsToKill().isEmpty()) {
            for (final EntityType ignored : s.getMobsToKill()) {
                data.mobNumKilled.add(0);
            }
        }
        data.setCowsMilked(0);
        data.setFishCaught(0);
        data.setPlayersKilled(0);
        if (!s.getLocationsToReach().isEmpty()) {
            for (final FabricLocation ignored : s.getLocationsToReach()) {
                data.locationsReached.add(false);
            }
        }
        if (!s.getMobsToTame().isEmpty()) {
            for (final EntityType ignored : s.getMobsToTame()) {
                data.mobsTamed.add(0);
            }
        }
        if (!s.getSheepToShear().isEmpty()) {
            for (final DyeColor ignored : s.getSheepToShear()) {
                data.sheepSheared.add(0);
            }
        }
        if (!s.getPasswordDisplays().isEmpty()) {
            for (final String ignored : s.getPasswordDisplays()) {
                data.passwordsSaid.add(false);
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
        return quest.getPlanner().getCooldown() - getCompletionDifference(quest);
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
     *
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
     *
     * @param quest The quest of which the timer is for
     */
    public void stopStageTimer(final Quest quest) {
        if (getQuestProgressOrDefault(quest).getDelayTimeLeft() > -1) {
            getQuestProgressOrDefault(quest).setDelayTimeLeft(getQuestProgressOrDefault(quest).getDelayTimeLeft()
                    - (System.currentTimeMillis() - getQuestProgressOrDefault(quest).getDelayStartTime()));
        } else {
            getQuestProgressOrDefault(quest).setDelayTimeLeft(getCurrentStage(quest).getDelay()
                    - (System.currentTimeMillis() - getQuestProgressOrDefault(quest).getDelayStartTime()));
        }
    }

    /**
     * Get remaining stage delay time
     *
     * @param quest The quest of which the timer is for
     * @return Remaining time in milliseconds
     */
    public long getStageTime(final Quest quest) {
        if (getQuestProgressOrDefault(quest).getDelayTimeLeft() > -1) {
            return getQuestProgressOrDefault(quest).getDelayTimeLeft() - (System.currentTimeMillis()
                    - getQuestProgressOrDefault(quest).getDelayStartTime());
        } else {
            return getCurrentStage(quest).getDelay() - (System.currentTimeMillis()
                    - getQuestProgressOrDefault(quest).getDelayStartTime());
        }
    }

    /**
     * Check whether the provided quest is valid and, if not, inform the Quester
     *
     * @param quest The quest to check
     */
    public void checkQuest(final Quest quest) {
        if (quest != null) {
            boolean exists = false;
            for (final Quest q : plugin.getLoadedQuests()) {
                if (q.getId().equalsIgnoreCase(quest.getId())) {
                    final Stage stage = getCurrentStage(quest);
                    if (stage != null) {
                        quest.updateCompass(this, stage);
                        // TODO - decide whether to handle this
                        /*if (q.equals(quest) == false) {
                            if (getPlayer() != null && getPlayer().isOnline()) {
                                quitQuest(quest, ChatColor.GOLD + Lang.get("questModified")
                                        .replace("<quest>", quest.getName()));
                            }
                        }*/
                    }
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                sendMessage(ChatFormatting.RED + FabricLang.get("questNotExist").replace("<quest>", quest.getName()));
            }
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
     * Does not save changes to disk. Consider calling {@link #saveData()}
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
     * Does not save changes to disk. Consider calling {@link #saveData()}
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
     * Does not save changes to disk. Consider calling {@link #saveData()}
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
     * Does not save changes to disk. Consider calling {@link #saveData()}
     *
     * @param key The quest to set stage of
     * @param val The data to set
     */
    public void hardDataPut(final Quest key, final QuestProgress val) {
        if (key != null && val != null) questProgress.put(key, val);
    }

    public boolean canUseCompass() {
        if (getPlayer() != null) {
            final FabricDependencies dependencies = plugin.getDependencies();
            if (!dependencies.hasPermission(id, "worldedit.navigation.jumpto")) {
                return dependencies.hasPermission(id, "quests.compass");
            }
        }
        return false;
    }

    /**
     * Reset compass target to Quester's bed spawn location<p>
     *
     * Will set to Quester's spawn location if bed spawn does not exist
     */
    public void resetCompass() {
        compassTargetQuestId = null;
        final ServerPlayer player = getPlayer();
        if (player == null) {
            return;
        }
        if (!canUseCompass()) {
            return;
        }

        LevelData.RespawnData defaultRespawnData = null;
        final ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
        if (respawnConfig != null) {
            defaultRespawnData = respawnConfig.respawnData();
        }
        if (defaultRespawnData == null) {
            defaultRespawnData = player.level().getRespawnData();
        }
        player.connection.send(new ClientboundSetDefaultSpawnPositionPacket(defaultRespawnData));
    }

    /**
     * Update compass target to current stage of first available current quest, if possible
     */
    public void findCompassTarget() {
        // Here we apply this method to OPs by not checking #canUseCompass
        if (getPlayer() == null || !plugin.getDependencies().hasPermission(id, "quests.compass")) {
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

    /**
     * Check whether the Quester's inventory contains the specified item
     *
     * @param is The item with a specified amount to check
     * @return true if the inventory contains at least the amount of the specified stack
     */
    public boolean hasItem(final ItemStack is) {
        if (getPlayer() == null) {
            return false;
        }
        int playerAmount = 0;
        for (final ItemStack stack : getPlayer().getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && FabricItemUtil.matches(stack, is)) {
                playerAmount += stack.getCount();
            }
        }
        return playerAmount >= is.getCount();
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
            try {
                if (quest.getOptions().canGiveGloballyAtLogin()) {
                    final List<Quester> mq = getGlobalQuesters(quest);
                    if (mq == null) {
                        return appliedQuestIDs;
                    }
                    for (final Quester q : mq) {
                        if (q == null) {
                            continue;
                        }
                        q.getCurrentQuests().forEach((otherQuest, i) -> {
                            if (otherQuest.getStage(i).containsObjective(type)) {
                                fun.apply(q, otherQuest);
                                appliedQuestIDs.add(otherQuest.getId());
                            }
                        });
                    }
                } else if (quest.getOptions().getShareProgressLevel() == 1) {
                    final Collection<Quester> mq = getMultiplayerQuesters(quest);
                    if (mq == null) {
                        return appliedQuestIDs;
                    }
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
            } catch (final Exception e) {
                plugin.getPluginLogger().severe("Error occurred while dispatching " + type.name() + " for quest ID "
                        + quest.getId());
                e.printStackTrace();
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
        return mq;
    }

    /**
     * Get a list of fellow Questers participating in a global quest
     *
     * @param quest The server-wide, global quest
     * @return Potentially empty list of Questers or null for invalid quest
     */
    public List<Quester> getGlobalQuesters(final Quest quest) {
        if (quest == null) {
            return null;
        }
        final List<Quester> mq = new LinkedList<>();
        if (quest.getOptions().canGiveGloballyAtLogin()) {
            for (final Quester oq : plugin.getOnlineQuesters()) {
                if (oq.getUUID().equals(getUUID())) {
                    continue;
                }
                if (oq.getCurrentQuests().containsKey(quest)) {
                    mq.add(oq);
                }
            }
            if (!mq.isEmpty() && plugin.getConfigSettings().getConsoleLogging() > 3) {
                plugin.getPluginLogger().info("Found " + mq.size() + " global members for quest ID "
                        + quest.getId());
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
        if (getPlayer() == null) {
            return false;
        }
        final UUID uuid = getPlayer().getUUID();
        return plugin.getQuestFactory().getSelectedBlockStarts().containsKey(uuid)
                || plugin.getQuestFactory().getSelectedKillLocations().containsKey(uuid)
                || plugin.getQuestFactory().getSelectedReachLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedExplosionLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedEffectLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedMobLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedLightningLocations().containsKey(uuid)
                || plugin.getActionFactory().getSelectedTeleportLocations().containsKey(uuid);
    }

    /**
     * Whether this Quester is in the specified WorldGuard region
     *
     * @return true if in specified WorldGuard region
     */
    public boolean isInRegion(final String regionID) {
        final ServerPlayer player = getPlayer();
        if (player == null) {
            return false;
        }
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
            if (q.getNpcStart() != null && !getCompletedQuests().contains(q)) {
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
            if (q.getNpcStart() != null && getCompletedQuests().contains(q)) {
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
            if (q.getNpcStart() != null && getCompletedQuests().contains(q)
                    && q.getPlanner().getCooldown() > -1) {
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

    public ConcurrentHashMap<Quest, QuestProgress> getProgressData() {
        return questProgress;
    }

    //TODO - this method should not exist
    public void setHasData(final boolean hasData) {
        this.hasData = hasData;
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
            case BREAK_BLOCK: return itemGoal(fs.getBlocksToBreak(), index);
            case DAMAGE_BLOCK: return itemGoal(fs.getBlocksToDamage(), index);
            case PLACE_BLOCK: return itemGoal(fs.getBlocksToPlace(), index);
            case USE_BLOCK: return itemGoal(fs.getBlocksToUse(), index);
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
            case CUSTOM: return listGoal(fs.getCustomObjectiveCounts(), index);
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
}

