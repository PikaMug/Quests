/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.convo.misc;

import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.events.misc.MiscPostNpcOfferQuestEvent;
import me.blackvein.quests.Quest;
import me.blackvein.quests.util.Lang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.LinkedList;

public class NpcOfferQuestPrompt extends MiscStringPrompt {

    private ConversationContext cc;

    public NpcOfferQuestPrompt() {
        super(null);
    }

    public NpcOfferQuestPrompt(final ConversationContext context) {
        super(context);
    }

    @Override
    public ConversationContext getConversationContext() {
        return cc;
    }

    private int size = 3;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle(final ConversationContext context) {
        final String npc = (String) context.getSessionData("npc");
        return Lang.get("questNPCListTitle").replace("<npc>", npc != null ? npc : "NPC");
    }

    @SuppressWarnings("unchecked")
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        final Quests plugin = (Quests)context.getPlugin();
        final LinkedList<IQuest> quests = (LinkedList<IQuest>) context.getSessionData("npcQuests");
        if (plugin != null) {
            final IQuester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final IQuest quest = quests.get(number - 1);
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
        final Quests plugin = (Quests)context.getPlugin();
        final LinkedList<IQuest> quests = (LinkedList<IQuest>) context.getSessionData("npcQuests");
        if (plugin != null) {
            final IQuester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final IQuest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatColor.GREEN + "" + ChatColor.ITALIC + quest.getName();
                    } else {
                        return ChatColor.YELLOW + "" + ChatColor.ITALIC + quest.getName();
                    }
                } else if (number == (quests.size() + 1)) {
                    return ChatColor.GRAY + Lang.get("cancel");
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        final Quests plugin = (Quests)context.getPlugin();
        final LinkedList<IQuest> quests = (LinkedList<IQuest>) context.getSessionData("npcQuests");
        if (plugin != null) {
            final IQuester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final IQuest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatColor.GREEN + "" + Lang.get("redoCompleted");
                    }
                }
            }
        }
        return "";
    }

    public String getQueryText(final ConversationContext context) {
        return Lang.get("enterAnOption");
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull String getPromptText(final ConversationContext context) {
        this.cc = context;
        final Quests plugin = (Quests)context.getPlugin();
        final LinkedList<Quest> quests = (LinkedList<Quest>) context.getSessionData("npcQuests");
        final String npc = (String) context.getSessionData("npc");
        if (plugin == null || quests == null || npc == null) {
            return ChatColor.YELLOW + Lang.get("unknownError");
        }
        quests.sort(Comparator.comparing(Quest::getName));

        final MiscPostNpcOfferQuestEvent event = new MiscPostNpcOfferQuestEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!(context.getForWhom() instanceof Player) || !plugin.getSettings().canClickablePrompts()) {
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
            choice.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.valueOf(i)));
            if (plugin.getSettings().canShowQuestReqs() && i <= size) {
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
        final Quests plugin = (Quests)context.getPlugin();
        final LinkedList<IQuest> quests = (LinkedList<IQuest>) context.getSessionData("npcQuests");
        if (plugin == null || quests == null) {
            return Prompt.END_OF_CONVERSATION;
        }
        final IQuester quester = plugin.getQuester(((Player) context.getForWhom()).getUniqueId());
        int numInput = -1;
        try {
            numInput = Integer.parseInt(input);
        } catch (final NumberFormatException e) {
            // Continue
        }
        if (input.equalsIgnoreCase(Lang.get("cancel")) || numInput == (quests.size() + 1)) {
            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("cancelled"));
            return Prompt.END_OF_CONVERSATION;
        } else {
            IQuest q = null;
            for (final IQuest quest : quests) {
                if (quest.getName().equalsIgnoreCase(input)) {
                    q = quest;
                    break;
                }
            }
            if (q == null) {
                for (final IQuest quest : quests) {
                    if (numInput == (quests.indexOf(quest) + 1)) {
                        q = quest;
                        break;
                    }
                }
            }
            if (q == null) {
                for (final IQuest quest : quests) {
                    if (quest.getName().toLowerCase().contains(input.toLowerCase())) {
                        q = quest;
                        break;
                    }
                }
            }
            if (q == null) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new NpcOfferQuestPrompt(context);
            } else {
                final Player player = quester.getPlayer();
                if (quester.canAcceptOffer(q, true)) {
                    quester.setQuestIdToTake(q.getId());
                    for (final String msg : extracted(plugin, quester).split("<br>")) {
                        player.sendMessage(msg);
                    }
                    if (!plugin.getSettings().canAskConfirmation()) {
                        quester.takeQuest(q, false);
                    } else {
                        plugin.getConversationFactory().buildConversation(player).begin();
                    }
                }
                return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    private String extracted(final Quests plugin, final IQuester quester) {
        final IQuest quest = plugin.getQuestById(quester.getQuestIdToTake());
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, 
                quest.getName(), ChatColor.GOLD, ChatColor.RESET, quest.getDescription());
    }
}
