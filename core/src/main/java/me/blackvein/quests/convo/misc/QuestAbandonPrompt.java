package me.blackvein.quests.convo.misc;

import me.blackvein.quests.Quests;
import me.blackvein.quests.events.misc.MiscPostQuestAbandonEvent;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.quests.IQuest;
import me.blackvein.quests.util.Lang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestAbandonPrompt extends MiscStringPrompt {

    private ConversationContext cc;

    public QuestAbandonPrompt() {
        super(null);
    }

    public QuestAbandonPrompt(final ConversationContext context) {
        super(context);
    }

    private final int size = 2;

    public int getSize() {
        return size;
    }

    public String getTitle(final ConversationContext context) {
        return null;
    }

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

    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
                return ChatColor.GREEN + Lang.get("yesWord");
            case 2:
                return ChatColor.RED + Lang.get("noWord");
            default:
                return null;
        }
    }

    public String getQueryText(final ConversationContext context) {
        return Lang.get("abandonQuest");
    }

    @Override
    public @NotNull String getPromptText(final @NotNull ConversationContext context) {
        this.cc = context;
        final Quests plugin = (Quests)context.getPlugin();
        if (plugin == null) {
            return ChatColor.YELLOW + Lang.get("unknownError");
        }

        final MiscPostQuestAbandonEvent event = new MiscPostQuestAbandonEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!plugin.getSettings().canClickablePrompts()) {
            return ChatColor.YELLOW + getQueryText(context) + "  " + ChatColor.GREEN
                    + getSelectionText(context, 1) + ChatColor.RESET + " / " + getSelectionText(context, 2);
        }

        final TextComponent component = new TextComponent("");
        component.addExtra(ChatColor.YELLOW + getQueryText(context) + "  " + ChatColor.GREEN);
        final TextComponent yes = new TextComponent(getSelectionText(context, 1));
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, Lang.get("yesWord")));
        component.addExtra(yes);
        component.addExtra(ChatColor.RESET + " / ");
        final TextComponent no = new TextComponent(getSelectionText(context, 2));
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, Lang.get("noWord")));
        component.addExtra(no);

        ((Player)context.getForWhom()).spigot().sendMessage(component);
        return "";
    }

    @Override
    public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
        final Quests plugin = (Quests)context.getPlugin();
        if (plugin == null || input == null) {
            return Prompt.END_OF_CONVERSATION;
        }
        final Player player = (Player) context.getForWhom();
        if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("y")
                || input.equalsIgnoreCase(Lang.get(player, "yesWord"))) {
            final IQuester quester = plugin.getQuester(player.getUniqueId());
            if (quester == null) {
                plugin.getLogger().info("Ended conversation because quester for " + getName() + "was null");
                return Prompt.END_OF_CONVERSATION;
            }
            final String questIdToQuit = quester.getQuestIdToQuit();
            try {
                IQuest quest = plugin.getQuestByIdTemp(questIdToQuit);
                if (quest == null) {
                    plugin.getLogger().info(player.getName() + " attempted to quit quest ID \"" + questIdToQuit
                            + "\" but something went wrong");
                    player.sendMessage(ChatColor.RED
                            + "Something went wrong! Please report issue to an administrator.");
                } else {
                    final String msg = ChatColor.YELLOW + Lang.get("questQuit").replace("<quest>",
                            ChatColor.DARK_PURPLE + quest.getName() + ChatColor.YELLOW);
                    quester.quitQuest(plugin.getQuestByIdTemp(questIdToQuit), msg);
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return Prompt.END_OF_CONVERSATION;
        } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("n")
                || input.equalsIgnoreCase(Lang.get("noWord"))) {
            Lang.send(player, ChatColor.YELLOW + Lang.get("cancelled"));
            return Prompt.END_OF_CONVERSATION;
        } else {
            final String msg = Lang.get(player, "questInvalidChoice")
                    .replace("<yes>", Lang.get(player, "yesWord"))
                    .replace("<no>", Lang.get(player, "noWord"));
            Lang.send(player, ChatColor.RED + msg);
            return new QuestAbandonPrompt(context);
        }
    }
}
