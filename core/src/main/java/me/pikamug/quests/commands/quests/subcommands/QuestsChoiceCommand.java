package me.pikamug.quests.commands.quests.subcommands;

import me.pikamug.quests.commands.QuestsSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.pikamug.quests.util.Language;

public class QuestsChoiceCommand extends QuestsSubCommand {

    @Override
    public String getName() {
        return "choice";
    }

    @Override
    public String getNameI18N() {
        return Language.get("COMMAND_CHOICE");
    }

    @Override
    public String getDescription() {
        return Language.get("COMMAND_CHOICE_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.choice";
    }

    @Override
    public String getSyntax() {
        return "/quests choice";
    }

    @Override
    public int getMaxArguments() {
        return 2;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (assertNonPlayer(cs)) {
            return;
        }
        final Player player = (Player) cs;
        if (!cs.hasPermission(getPermission())) {
            Language.send(player, ChatColor.RED + Language.get(player, "noPermission"));
            return;
        }
        if (!player.isConversing()) {
            Language.send(player, ChatColor.RED + Language.get(player, "notConversing"));
            return;
        }
        if (args.length == 1) {
            return;
        }
        final String input = concatArgArray(args, 1, args.length - 1, ' ');
        if (input != null) {
            player.acceptConversationInput(input);
        }
    }
}
