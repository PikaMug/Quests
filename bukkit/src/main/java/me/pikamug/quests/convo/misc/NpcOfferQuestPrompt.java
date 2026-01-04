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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.events.misc.BukkitMiscPostNpcOfferQuestEvent;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.browsit.conversations.api.Conversations;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.UUID;

public class NpcOfferQuestPrompt extends MiscStringPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private final LinkedList<Quest> npcQuests;
    private final String npcName;

    public NpcOfferQuestPrompt(final @NotNull UUID uuid, final BukkitQuestsPlugin plugin,
                               LinkedList<Quest> npcQuests, String npcName) {
        super();
        this.uuid = uuid;
        this.plugin = plugin;
        this.npcQuests = npcQuests;
        this.npcName = npcName;
    }

    private int size = 3;

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public String getTitle() {
        return BukkitLang.get("questNPCListTitle").replace("<npc>", npcName != null ? npcName : "NPC");
    }

    public ChatColor getNumberColor(final int number) {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin != null) {
            final Quester quester = plugin.getQuester(uuid);
            if (quests != null && number > 0) {
                if (number < (quests.size() + 1)) {
                    final Quest quest = quests.get(number - 1);
                    if (quester.getCompletedQuests().contains(quest)) {
                        return ChatColor.GREEN;
                    } else {
                        return ChatColor.GOLD;
                    }
                } else if (number == (quests.size() + 1)) {
                    return ChatColor.GOLD;
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

    public String getAdditionalText(final int number) {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin != null) {
            final Quester quester = plugin.getQuester(uuid);
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

    @Override
    public String getQueryText() {
        return BukkitLang.get("enterAnOption");
    }

    public @NotNull String getPromptText() {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin == null || quests == null || npcName == null) {
            return ChatColor.YELLOW + BukkitLang.get("itemCreateCriticalError");
        }
        quests.sort(Comparator.comparing(Quest::getName));


        final BukkitMiscPostNpcOfferQuestEvent event = new BukkitMiscPostNpcOfferQuestEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (Bukkit.getPlayer(uuid) == null || !plugin.getConfigSettings().canClickablePrompts()) {
            final StringBuilder text = new StringBuilder(ChatColor.WHITE + getTitle());
            size = quests.size();
            for (int i = 1; i <= size + 1; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i).append(". ")
                        .append(ChatColor.RESET).append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            text.append("\n").append(ChatColor.WHITE).append(getQueryText());
            return text.toString();
        }
        final TextComponent component = new TextComponent(getTitle());
        component.setColor(net.md_5.bungee.api.ChatColor.WHITE);
        size = quests.size();
        final TextComponent line = new TextComponent("");
        for (int i = 1; i <= size + 1; i++) {
            final TextComponent choice = new TextComponent("\n" + getNumberColor(i) + ChatColor.BOLD + i + ". "
                    + ChatColor.RESET + getSelectionText(i));
            choice.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + i));
            if (plugin.getConfigSettings().canShowQuestReqs() && i <= size) {
                choice.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(quests.get(i - 1).getDescription()).create()));
            }
            line.addExtra(choice);
            line.addExtra(getAdditionalText(i));
        }
        component.addExtra(line);
        component.addExtra("\n" + ChatColor.WHITE + getQueryText());
        Bukkit.getPlayer(uuid).spigot().sendMessage(component);
        return "";
    }

    public void acceptInput(final String input) {
        final LinkedList<Quest> quests = npcQuests;
        if (plugin == null || quests == null) {
            return;
        }
        final CommandSender sender = Bukkit.getEntity(uuid);
        final BukkitQuester quester = plugin.getQuester(uuid);
        int numInput = -1;
        try {
            numInput = Integer.parseInt(input);
        } catch (final NumberFormatException e) {
            // Continue
        }
        if (input.equalsIgnoreCase(BukkitLang.get("cancel")) || numInput == (quests.size() + 1)) {
            sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("cancelled"));
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
                sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidOption"));
                new NpcOfferQuestPrompt(uuid, plugin, npcQuests, npcName).start();
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
                        new QuestAcceptPrompt(player.getUniqueId(), plugin).start();
                    }
                }
            }
        }
    }

    private String extracted(final BukkitQuestsPlugin plugin, final Quester quester) {
        final Quest quest = plugin.getQuestById(quester.getQuestIdToTake());
        return MessageFormat.format("{0}- {1}{2}{3} -\n\n{4}{5}\n", ChatColor.GOLD, ChatColor.DARK_PURPLE, 
                quest.getName(), ChatColor.GOLD, ChatColor.RESET, quest.getDescription());
    }

    public void start() {
        Conversations.create(uuid)
                .prompt(getPromptText(), String.class, prompt -> prompt
                        .converter(String::valueOf)
                        .conversionFailText(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"))
                        .fetch((input, sender) -> acceptInput(input)))
                //.endWhen(new TimeClause(plugin.getConfigSettings().getAcceptTimeout() * 20L,
                // ChatColor.YELLOW + BukkitLang.get("questTimeout")))                              needs String support
                .start();
    }
}
