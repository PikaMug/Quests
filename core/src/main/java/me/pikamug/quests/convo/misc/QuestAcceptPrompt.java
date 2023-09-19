package me.pikamug.quests.convo.misc;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.events.misc.MiscPostQuestAcceptEvent;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.BukkitLang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuestAcceptPrompt extends MiscStringPrompt {

    private ConversationContext context;
    private final BukkitQuestsPlugin plugin;

    public QuestAcceptPrompt(BukkitQuestsPlugin plugin) {
        super(null);
        this.plugin = plugin;
    }

    public QuestAcceptPrompt(final ConversationContext context) {
        super(context);
        plugin = (BukkitQuestsPlugin)context.getPlugin();
    }

    @Override
    public ConversationContext getConversationContext() {
        return context;
    }

    public int getSize() {
        return 2;
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
        return BukkitLang.get("acceptQuest");
    }

    @Override
    public @NotNull String getPromptText(final @NotNull ConversationContext context) {
        this.context = context;
        if (plugin == null) {
            return ChatColor.YELLOW + BukkitLang.get("itemCreateCriticalError");
        }

        final MiscPostQuestAcceptEvent event = new MiscPostQuestAcceptEvent(context, this);
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
                || input.equalsIgnoreCase(BukkitLang.get("yesWord"))
                || input.equalsIgnoreCase(BukkitLang.get(player, "yesWord"))) {
            final Quester quester = plugin.getQuester(player.getUniqueId());
            if (quester == null) {
                plugin.getLogger().info("Ended conversation because quester for " + getName() + "was null");
                return Prompt.END_OF_CONVERSATION;
            }
            final String questIdToTake = quester.getQuestIdToTake();
            if (plugin.getQuestById(questIdToTake) == null) {
                plugin.getLogger().warning(player.getName() + " attempted to take quest ID \"" + questIdToTake
                        + "\" but something went wrong");
                player.sendMessage(ChatColor.RED
                        + "Something went wrong! Please report issue to an administrator.");
            } else {
                quester.takeQuest(plugin.getQuestById(questIdToTake), false);
            }
            return Prompt.END_OF_CONVERSATION;
        } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("n")
                || input.equalsIgnoreCase(BukkitLang.get("noWord"))
                || input.equalsIgnoreCase(BukkitLang.get(player, "noWord"))) {
            BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get("cancelled"));
            return Prompt.END_OF_CONVERSATION;
        } else {
            final String msg = BukkitLang.get(player, "questInvalidChoice")
                    .replace("<yes>", BukkitLang.get(player, "yesWord"))
                    .replace("<no>", BukkitLang.get(player, "noWord"));
            BukkitLang.send(player, ChatColor.RED + msg);
            return new QuestAcceptPrompt(context);
        }
    }
}
