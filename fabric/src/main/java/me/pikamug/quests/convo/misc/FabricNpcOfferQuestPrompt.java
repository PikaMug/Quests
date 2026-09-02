/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.misc;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.config.FabricConfigSettings;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.clause.TimeClause;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;

public class FabricNpcOfferQuestPrompt extends FabricMiscStringPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private final LinkedList<Quest> npcQuests;
    private final String npcName;

    public FabricNpcOfferQuestPrompt(final @NotNull UUID uuid, final FabricQuestsPlugin plugin,
                                     LinkedList<Quest> npcQuests, String npcName) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = plugin;
        this.npcQuests = npcQuests;
        this.npcName = npcName;
    }

    public LinkedList<Quest> getNpcQuests() {
        return npcQuests;
    }

    private int size = 3;

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("questNPCListTitle").replace("<npc>", npcName != null ? npcName : "NPC");
    }

    public ChatFormatting getNumberColor(final int number) {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin != null) {
            final Quester quester = plugin.getQuester(uuid);
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final Quest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatFormatting.GREEN;
                    } else {
                        return ChatFormatting.GOLD;
                    }
                } else if (number == (quests.size() + 1)) {
                    return ChatFormatting.GOLD;
                }
            }
        }
        return null;
    }

    public String getSelectionText(final int number) {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin != null) {
            final Quester quester = plugin.getQuester(uuid);
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final Quest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatFormatting.GREEN + "" + ChatFormatting.ITALIC + quest.getName();
                    } else {
                        return ChatFormatting.YELLOW + "" + ChatFormatting.ITALIC + quest.getName();
                    }
                } else if (number == (quests.size() + 1)) {
                    return ChatFormatting.GRAY + FabricLang.get("cancel");
                }
            }
        }
        return null;
    }

    public String getAdditionalText(final int number) {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin != null) {
            final Quester quester = plugin.getQuester(uuid);
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final Quest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatFormatting.GREEN + "" + FabricLang.get("redoCompleted");
                    }
                }
            }
        }
        return "";
    }

    @Override
    public String getQueryText() {
        return FabricLang.get("enterAnOption");
    }

    public @NotNull String getPromptText() {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin == null || quests == null || npcName == null) {
            return ChatFormatting.YELLOW + FabricLang.get("itemCreateCriticalError");
        }
        quests.sort(Comparator.comparing(Quest::getName));

        final FabricConfigSettings configSettings = (FabricConfigSettings) plugin.getConfigSettings();
        final ServerPlayer player = plugin.getServer().getPlayerList().getPlayer(uuid);

        if (player == null || !configSettings.canClickablePrompts()) {
            final StringBuilder text = new StringBuilder(ChatFormatting.WHITE + getTitle());
            size = quests.size();
            for (int i = 1; i <= size + 1; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i).append(". ")
                        .append(ChatFormatting.RESET).append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            text.append("\n").append(ChatFormatting.WHITE).append(getQueryText());
            return text.toString();
        }

        final MutableComponent component = Component.literal(getTitle())
                .withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE));
        size = quests.size();
        final MutableComponent line = Component.literal("");
        for (int i = 1; i <= size + 1; i++) {
            final MutableComponent choice = Component.literal(
                    "\n" + getNumberColor(i) + ChatFormatting.BOLD + i + ". "
                            + ChatFormatting.RESET + getSelectionText(i))
                    .withStyle(Style.EMPTY
                            .withClickEvent(new ClickEvent.RunCommand("/quests choice " + i)));
            if (configSettings.canShowQuestReqs() && i <= size) {
                choice.withStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(quests.get(i - 1).getDescription()))));
            }
            line.append(choice);
            line.append(Component.literal(getAdditionalText(i)));
        }
        component.append(line);
        component.append(Component.literal("\n" + ChatFormatting.WHITE + getQueryText()));
        player.sendSystemMessage(component);
        return "";
    }

    public void acceptInput(final String input) {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin == null || quests == null) {
            return;
        }
        final FabricConfigSettings configSettings = (FabricConfigSettings) plugin.getConfigSettings();
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
        final Quester quester = plugin.getQuester(uuid);
        int numInput = -1;
        try {
            numInput = Integer.parseInt(input);
        } catch (final NumberFormatException e) {
            // Continue
        }
        if (input.equalsIgnoreCase(FabricLang.get("cancel")) || numInput == (quests.size() + 1)) {
            if (sender != null) {
                sender.sendSystemMessage(Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("cancelled")));
            }
            return;
        } else {
            Quest q = null;
            for (final Quest quest : quests) {
                if (quest.getName().equalsIgnoreCase(input)) {
                    q = quest;
                    break;
                }
            }
            if (q == null) {
                for (final Quest quest : quests) {
                    if (numInput == (quests.indexOf(quest) + 1)) {
                        q = quest;
                        break;
                    }
                }
            }
            if (q == null) {
                for (final Quest quest : quests) {
                    if (quest.getName().toLowerCase().contains(input.toLowerCase())) {
                        q = quest;
                        break;
                    }
                }
            }
            if (q == null) {
                if (sender != null) {
                    sender.sendSystemMessage(Component.literal(
                            ChatFormatting.RED + FabricLang.get("invalidOption")));
                }
                new FabricNpcOfferQuestPrompt(uuid, plugin, npcQuests, npcName).start();
            } else {
                final ServerPlayer player = FabricMiscUtil.getPlayer(uuid, plugin);
                if (quester.canAcceptOffer(q, true)) {
                    quester.setQuestIdToTake(q.getId());
                    final Quest takeQuest = getQuestById(quester.getQuestIdToTake());
                    if (takeQuest != null && player != null) {
                        final String questInfo = MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n",
                                ChatFormatting.GOLD, ChatFormatting.DARK_PURPLE,
                                takeQuest.getName(), ChatFormatting.GOLD, ChatFormatting.RESET,
                                takeQuest.getDescription());
                        for (final String msg : questInfo.split("<br>")) {
                            player.sendSystemMessage(Component.literal(msg));
                        }
                    }
                    if (!configSettings.canConfirmAccept()) {
                        quester.takeQuest(q, false);
                    } else {
                        new FabricQuestAcceptPrompt(player != null ? player.getUUID() : uuid, plugin).start();
                    }
                }
            }
        }
    }

    private Quest getQuestById(final String id) {
        if (id == null) return null;
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getId().equals(id)) {
                return q;
            }
        }
        return null;
    }

    private String extracted(final FabricQuestsPlugin plugin, final Quester quester) {
        final Quest quest = getQuestById(quester.getQuestIdToTake());
        if (quest == null) return "";
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatFormatting.GOLD, ChatFormatting.DARK_PURPLE,
                quest.getName(), ChatFormatting.GOLD, ChatFormatting.RESET, quest.getDescription());
    }

    public void start() {
        Conversations.create(uuid).title(getName())
                .prompt(getPromptText(), String.class, prompt -> prompt
                        .converter(String::valueOf)
                        .conversionFailText(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError"))
                        .fetch((input, sender) -> acceptInput(input)))
                .endWhen(TimeClause.create(
                        ((FabricConfigSettings) plugin.getConfigSettings()).getAcceptTimeout() * 1000L,
                        ChatFormatting.YELLOW + FabricLang.get("questTimeout")))
                .start();
    }
}
