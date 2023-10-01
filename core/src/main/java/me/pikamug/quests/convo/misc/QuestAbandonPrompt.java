package me.pikamug.quests.convo.misc;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.events.misc.MiscPostQuestAbandonEvent;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.BukkitLang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestAbandonPrompt extends MiscStringPrompt {

    private ConversationContext context;
    private final BukkitQuestsPlugin plugin;

    public QuestAbandonPrompt(BukkitQuestsPlugin plugin) {
        super(null);
        this.plugin = plugin;
    }

    public QuestAbandonPrompt(final ConversationContext context) {
        super(context);
        plugin = (BukkitQuestsPlugin)context.getPlugin();
    }

    @Override
    public ConversationContext getConversationContext() {
        return context;
    }

    private final int size = 2;

    public int getSize() {
        return size;
    }

    public String getTitle(final ConversationContext context) {
        return null;
    }

    @SuppressWarnings("unused")
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
                return ChatColor.GREEN;
            case 2:
                return ChatColor.RED;
            default:
                return null;
        }
    }

    @SuppressWarnings("unused")
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLang.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLang.get("noWord");
            default:
                return null;
        }
    }

    public String getQueryText(final ConversationContext context) {
        return BukkitLang.get("abandonQuest");
    }

    @Override
    public @NotNull String getPromptText(final @NotNull ConversationContext context) {
        this.context = context;
        if (plugin == null) {
            return ChatColor.YELLOW + BukkitLang.get("itemCreateCriticalError");
        }

        final MiscPostQuestAbandonEvent event = new MiscPostQuestAbandonEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!plugin.getConfigSettings().canClickablePrompts()) {
            return ChatColor.YELLOW + getQueryText(context) + "  " + ChatColor.GREEN
                    + getSelectionText(context, 1) + ChatColor.RESET + " / " + getSelectionText(context, 2);
        }

        final TextComponent component = new TextComponent("");
        component.addExtra(ChatColor.YELLOW + getQueryText(context) + "  " + ChatColor.GREEN);
        final TextComponent yes = new TextComponent(getSelectionText(context, 1));
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + BukkitLang.get("yesWord")));
        component.addExtra(yes);
        component.addExtra(ChatColor.RESET + " / ");
        final TextComponent no = new TextComponent(getSelectionText(context, 2));
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + BukkitLang.get("noWord")));
        component.addExtra(no);

        ((Player)context.getForWhom()).spigot().sendMessage(component);
        return "";
    }

    @Override
    public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
        final BukkitQuestsPlugin plugin = (BukkitQuestsPlugin)context.getPlugin();
        if (plugin == null || input == null) {
            return Prompt.END_OF_CONVERSATION;
        }
        final Player player = (Player) context.getForWhom();
        if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("y")
                || input.equalsIgnoreCase(BukkitLang.get("yesWord")) || input.equalsIgnoreCase(BukkitLang.get(player, "yesWord"))) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            if (quester == null) {
                plugin.getLogger().info("Ended conversation because quester for " + getName() + "was null");
                return Prompt.END_OF_CONVERSATION;
            }
            final String questIdToQuit = quester.getQuestIdToQuit();
            try {
                Quest quest = plugin.getQuestById(questIdToQuit);
                if (quest == null) {
                    plugin.getLogger().info(player.getName() + " attempted to quit quest ID \"" + questIdToQuit
                            + "\" but something went wrong");
                    player.sendMessage(ChatColor.RED
                            + "Something went wrong! Please report issue to an administrator.");
                } else {
                    final String msg = ChatColor.YELLOW + BukkitLang.get("questQuit").replace("<quest>",
                            ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                    quester.quitQuest(plugin.getQuestById(questIdToQuit), msg);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return Prompt.END_OF_CONVERSATION;
        } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("n")
                || input.equalsIgnoreCase(BukkitLang.get("noWord")) || input.equalsIgnoreCase(BukkitLang.get(player, "noWord"))) {
            BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get("cancelled"));
            return Prompt.END_OF_CONVERSATION;
        } else {
            final String msg = BukkitLang.get(player, "questInvalidChoice")
                    .replace("<yes>", BukkitLang.get(player, "yesWord"))
                    .replace("<no>", BukkitLang.get(player, "noWord"));
            BukkitLang.send(player, ChatColor.RED + msg);
            return new QuestAbandonPrompt(context);
        }
    }
}
