package me.pikamug.quests.convo.misc;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.events.misc.BukkitMiscPostQuestAcceptEvent;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.BukkitLang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.clause.TimeClause;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestAcceptPrompt extends MiscStringPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;

    public QuestAcceptPrompt(final @NotNull UUID uuid, BukkitQuestsPlugin plugin) {
        super();
        this.uuid = uuid;
        this.plugin = plugin;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @SuppressWarnings("unused")
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
        switch (number) {
            case 1:
                return ChatColor.GREEN + BukkitLang.get("yesWord");
            case 2:
                return ChatColor.RED + BukkitLang.get("noWord");
            default:
                return null;
        }
    }

    @Override
    public String getQueryText() {
        return BukkitLang.get("acceptQuest");
    }

    public @NotNull String getPromptText() {
        if (plugin == null) {
            return ChatColor.YELLOW + BukkitLang.get("itemCreateCriticalError");
        }

        final BukkitMiscPostQuestAcceptEvent event = new BukkitMiscPostQuestAcceptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);

        if (!plugin.getConfigSettings().canClickablePrompts()) {
            return ChatColor.YELLOW + getQueryText() + "  " + ChatColor.GREEN
                    + getSelectionText(1) + ChatColor.RESET + " / " + getSelectionText(2);
        }

        final TextComponent component = new TextComponent("");
        component.addExtra(ChatColor.YELLOW + getQueryText() + "  " + ChatColor.GREEN);
        final TextComponent yes = new TextComponent(getSelectionText(1));
        yes.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + BukkitLang.get("yesWord")));
        component.addExtra(yes);
        component.addExtra(ChatColor.RESET + " / ");
        final TextComponent no = new TextComponent(getSelectionText(2));
        no.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + BukkitLang.get("noWord")));
        component.addExtra(no);

        Bukkit.getPlayer(uuid).spigot().sendMessage(component);
        return "";
    }

    public void acceptInput(final String input) {
        if (plugin == null || input == null) {
            return;
        }
        final Player player = Bukkit.getPlayer(uuid);
        if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("y")
                || input.equalsIgnoreCase(BukkitLang.get("yesWord"))
                || input.equalsIgnoreCase(BukkitLang.get(player, "yesWord"))) {
            final Quester quester = plugin.getQuester(uuid);
            if (quester == null) {
                plugin.getLogger().info("Ended conversation because quester for " + player.getName() + "was null");
                return;
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
        } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("n")
                || input.equalsIgnoreCase(BukkitLang.get("noWord"))
                || input.equalsIgnoreCase(BukkitLang.get(player, "noWord"))) {
            BukkitLang.send(player, ChatColor.YELLOW + BukkitLang.get("cancelled"));
        } else {
            final String msg = BukkitLang.get(player, "questInvalidChoice")
                    .replace("<yes>", BukkitLang.get(player, "yesWord"))
                    .replace("<no>", BukkitLang.get(player, "noWord"));
            BukkitLang.send(player, ChatColor.RED + msg);
        }
    }

    public void start() {
        Conversations.create(uuid)
                .prompt(getPromptText(), String.class, prompt -> prompt
                .converter(String::valueOf)
                .conversionFailText(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"))
                .fetch((input, sender) -> acceptInput(input)))
                .endWhen(TimeClause.create(plugin.getConfigSettings().getAcceptTimeout() * 20L,
                ChatColor.YELLOW + BukkitLang.get("questTimeout")))
                .start();
    }
}
