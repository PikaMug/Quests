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

import me.pikamug.quests.quests.BukkitQuest;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.events.misc.MiscPostNpcOfferQuestEvent;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.LinkedList;

public class NpcOfferQuestPrompt extends MiscStringPrompt {

    private ConversationContext context;
    private final BukkitQuestsPlugin plugin;

    public NpcOfferQuestPrompt(final BukkitQuestsPlugin plugin) {
        super(null);
        this.plugin = plugin;
    }

    public NpcOfferQuestPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }

    @Override
    public ConversationContext getConversationContext() {
        return context;
    }

    private int size = 3;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle(final ConversationContext context) {
        final String npc = (String) context.getSessionData("npc");
        return BukkitLang.get("questNPCListTitle").replace("<npc>", npc != null ? npc : "NPC");
    }

    @SuppressWarnings("unchecked")
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        final LinkedList<Quest> quests = (LinkedList<Quest>) context.getSessionData("npcQuests");
        if (plugin != null) {
            final Quester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final Quest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatColor.GREEN;
                    } else {
                        return ChatColor.GOLD;
                    }
                } else if (number == (quests.size() + 1)) {
                    //return ChatColor.RED;
                    return ChatColor.GOLD;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String getSelectionText(final ConversationContext context, final int number) {
        final LinkedList<Quest> quests = (LinkedList<Quest>) context.getSessionData("npcQuests");
        if (plugin != null) {
            final Quester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final Quest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatColor.GREEN + "" + ChatColor.ITALIC + quest.getName();
                    } else {
                        return ChatColor.YELLOW + "" + ChatColor.ITALIC + quest.getName();
                    }
                } else if (number == (quests.size() + 1)) {
                    return ChatColor.GRAY + BukkitLang.get("cancel");
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        final LinkedList<Quest> quests = (LinkedList<Quest>) context.getSessionData("npcQuests");
        if (plugin != null) {
            final Quester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final Quest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatColor.GREEN + "" + BukkitLang.get("redoCompleted");
                    }
                }
            }
        }
        return "";
    }

    public String getQueryText(final ConversationContext context) {
        return BukkitLang.get("enterAnOption");
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull String getPromptText(final ConversationContext context) {
        this.context = context;
        final LinkedList<BukkitQuest> quests = (LinkedList<BukkitQuest>) context.getSessionData("npcQuests");
        final String npc = (String) context.getSessionData("npc");
        if (plugin == null || quests == null || npc == null) {
            return ChatColor.YELLOW + BukkitLang.get("itemCreateCriticalError");
        }
        quests.sort(Comparator.comparing(BukkitQuest::getName));

        final MiscPostNpcOfferQuestEvent event = new MiscPostNpcOfferQuestEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!(context.getForWhom() instanceof Player) || !plugin.getConfigSettings().canClickablePrompts()) {
            final StringBuilder text = new StringBuilder(ChatColor.WHITE + getTitle(context));
            size = quests.size();
            for (int i = 1; i <= size + 1; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i).append(". ")
                        .append(ChatColor.RESET).append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            text.append("\n").append(ChatColor.WHITE).append(getQueryText(context));
            return text.toString();
        }
        final TextComponent component = new TextComponent(getTitle(context));
        component.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        size = quests.size();
        final TextComponent line = new TextComponent("");
        for (int i = 1; i <= size + 1; i++) {
            final TextComponent choice = new TextComponent("\n" + getNumberColor(context, i) + ChatColor.BOLD + i + ". "
                    + ChatColor.RESET + getSelectionText(context, i));
            choice.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + i));
            if (plugin.getConfigSettings().canShowQuestReqs() && i <= size) {
                choice.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(quests.get(i - 1).getDescription()).create()));
            }
            line.addExtra(choice);
            line.addExtra(getAdditionalText(context, i));
        }
        component.addExtra(line);
        component.addExtra("\n" + ChatColor.WHITE + getQueryText(context));
        ((Player)context.getForWhom()).spigot().sendMessage(component);
        return "";
    }

    @SuppressWarnings("unchecked")
    @Override
    public Prompt acceptInput(final ConversationContext context, final String input) {
        final LinkedList<Quest> quests = (LinkedList<Quest>) context.getSessionData("npcQuests");
        if (plugin == null || quests == null) {
            return Prompt.END_OF_CONVERSATION;
        }
        final Quester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
        int numInput = -1;
        try {
            numInput = Integer.parseInt(input);
        } catch (final NumberFormatException e) {
            // Continue
        }
        if (input.equalsIgnoreCase(BukkitLang.get("cancel")) || numInput == (quests.size() + 1)) {
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + BukkitLang.get("cancelled"));
            return Prompt.END_OF_CONVERSATION;
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
                context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                return new NpcOfferQuestPrompt(context);
            } else {
                final Player player = quester.getPlayer();
                if (quester.canAcceptOffer(q, true)) {
                    quester.setQuestIdToTake(q.getId());
                    for (final String msg : extracted(plugin, quester).split("<br>")) {
                        player.sendMessage(msg);
                    }
                    if (!plugin.getConfigSettings().canConfirmAccept()) {
                        quester.takeQuest(q, false);
                    } else {
                        plugin.getConversationFactory().buildConversation(player).begin();
                    }
                }
                return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    private String extracted(final BukkitQuestsPlugin plugin, final Quester quester) {
        final Quest quest = plugin.getQuestById(quester.getQuestIdToTake());
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, 
                quest.getName(), ChatColor.GOLD, ChatColor.RESET, quest.getDescription());
    }
}
