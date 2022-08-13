package me.blackvein.quests.commands.quests.subcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.util.Lang;

public class QuestsChoiceCommand extends QuestsSubCommand {

    @Override
    public String getName() {
        return "choice";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_CHOICE");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_CHOICE_HELP");
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
        Player player = (Player) cs;
        if (!cs.hasPermission(getPermission())) {
            Lang.send(player, ChatColor.RED + Lang.get(player, "noPermission"));
            return;
        }
        if (!player.isConversing()) {
            Lang.send(player, ChatColor.RED + Lang.get(player, "notConversing"));
            return;
        }
        if (args.length == 1) {
            return;
        }
        player.acceptConversationInput(concatArgArray(args, 1, args.length - 1, ' '));
    }

}
