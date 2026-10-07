/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.questadmin;

import com.mojang.brigadier.Command;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.commands.FabricQuestsSubCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminFinishCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminGiveCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminGivePointsCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminNextStageCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminPointsCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminQuitCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminReloadCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminRemoveCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminResetCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminSetStageCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminStatsCommand;
import me.pikamug.quests.commands.questadmin.subcommands.FabricQuestadminTakePointsCommand;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.pikamug.quests.commands.FabricQuestsSubCommand.lang;
import static me.pikamug.quests.commands.FabricQuestsSubCommand.reply;

public class FabricQuestadminCommandHandler {

    private static final String ADMIN = "quests.admin";

    private final FabricQuestsPlugin plugin;

    private final FabricQuestsSubCommand stats;
    private final FabricQuestsSubCommand give;
    private final FabricQuestsSubCommand quit;
    private final FabricQuestsSubCommand points;
    private final FabricQuestsSubCommand takepoints;
    private final FabricQuestsSubCommand givepoints;
    private final FabricQuestsSubCommand finish;
    private final FabricQuestsSubCommand nextstage;
    private final FabricQuestsSubCommand setstage;
    private final FabricQuestsSubCommand reset;
    private final FabricQuestsSubCommand remove;
    private final FabricQuestsSubCommand reload;

    private final Map<String, FabricQuestsSubCommand> subCommands = new HashMap<>();

    public FabricQuestadminCommandHandler(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        stats = new FabricQuestadminStatsCommand(plugin);
        give = new FabricQuestadminGiveCommand(plugin);
        quit = new FabricQuestadminQuitCommand(plugin);
        points = new FabricQuestadminPointsCommand(plugin);
        takepoints = new FabricQuestadminTakePointsCommand(plugin);
        givepoints = new FabricQuestadminGivePointsCommand(plugin);
        finish = new FabricQuestadminFinishCommand(plugin);
        nextstage = new FabricQuestadminNextStageCommand(plugin);
        setstage = new FabricQuestadminSetStageCommand(plugin);
        reset = new FabricQuestadminResetCommand(plugin);
        remove = new FabricQuestadminRemoveCommand(plugin);
        reload = new FabricQuestadminReloadCommand(plugin);
        subCommands.put("stats", stats);
        subCommands.put("give", give);
        subCommands.put("quit", quit);
        subCommands.put("points", points);
        subCommands.put("takepoints", takepoints);
        subCommands.put("givepoints", givepoints);
        subCommands.put("finish", finish);
        subCommands.put("nextstage", nextstage);
        subCommands.put("setstage", setstage);
        subCommands.put("reset", reset);
        subCommands.put("remove", remove);
        subCommands.put("reload", reload);
    }

    public int check(final CommandSourceStack source, final String[] args) {
        if (args.length < 1) {
            return handleAdminHelp(source);
        }
        final FabricQuestsSubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            return printQuestAdminUnknownCommand(args[0], source);
        } else if (args.length < sub.getMaxArguments()) {
            return getQuestAdminCommandUsage(source, sub.getName());
        } else if (args.length > sub.getMaxArguments() + 1) {
            return getQuestAdminCommandUsage(source, sub.getName());
        } else {
            return sub.execute(source, args);
        }
    }

    public int handleAdminHelp(final CommandSourceStack source) {
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questAdminHelpTitle")), false);
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + "/questadmin" + ChatFormatting.RED + " "
                + lang(source, "COMMAND_QUESTADMIN_HELP")), false);
        final boolean translate = plugin.getConfigSettings().canTranslateSubCommands();
        printAdminHelpLine(source, translate, "stats", "COMMAND_QUESTADMIN_STATS");
        printAdminHelpLine(source, translate, "give", "COMMAND_QUESTADMIN_GIVE");
        printAdminHelpLine(source, translate, "quit", "COMMAND_QUESTADMIN_QUIT");
        printAdminHelpLine(source, translate, "points", "COMMAND_QUESTADMIN_POINTS");
        printAdminHelpLine(source, translate, "takepoints", "COMMAND_QUESTADMIN_TAKEPOINTS");
        printAdminHelpLine(source, translate, "givepoints", "COMMAND_QUESTADMIN_GIVEPOINTS");
        printAdminHelpLine(source, translate, "finish", "COMMAND_QUESTADMIN_FINISH");
        printAdminHelpLine(source, translate, "nextstage", "COMMAND_QUESTADMIN_NEXTSTAGE");
        printAdminHelpLine(source, translate, "setstage", "COMMAND_QUESTADMIN_SETSTAGE");
        printAdminHelpLine(source, translate, "reset", "COMMAND_QUESTADMIN_RESET");
        printAdminHelpLine(source, translate, "remove", "COMMAND_QUESTADMIN_REMOVE");
        printAdminHelpLine(source, translate, "reload", "COMMAND_QUESTADMIN_RELOAD");
        return Command.SINGLE_SUCCESS;
    }

    private void printAdminHelpLine(final CommandSourceStack source, final boolean translate,
                                    final String sub, final String key) {
        if (!hasAdminNode(source, sub)) return;
        final String name = translate ? lang(source, key) : sub;
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + "/questadmin " + ChatFormatting.RED
                + lang(source, key + "_HELP").replace("<command>", name)), false);
    }

    private int printQuestAdminUnknownCommand(final String sub, final CommandSourceStack source) {
        reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "questsUnknownAdminCommand")), false);
        return Command.SINGLE_SUCCESS;
    }

    private int getQuestAdminCommandUsage(final CommandSourceStack source, final String cmd) {
        reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "usage") + ": "
                + ChatFormatting.YELLOW + "/questadmin " + lang(source,
                FabricLang.getKeyFromPrefix("COMMAND_QUESTADMIN_", cmd) + "_HELP")
                .replace("<command>", cmd.toLowerCase())), false);
        return 0;
    }

    /**
     * Handles command tab completion
     *
     * @param source the source of this command
     * @param args the arguments for this command
     * @return the possible completions, or null to provide online player names
     */
    public List<String> suggest(final CommandSourceStack source, final String[] args) {
        if (args.length < 1) {
            return null;
        }
        final FabricQuestsSubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            final List<String> names = new ArrayList<>();
            for (final FabricQuestsSubCommand c : subCommands.values()) {
                names.add(plugin.getConfigSettings().canTranslateSubCommands() ? c.getNameI18N() : c.getName());
            }
            return names;
        }
        return sub.tabComplete(source, args);
    }

    public boolean hasAdminRoot(final CommandSourceStack source) {
        if (!source.isPlayer()) return true;
        final ServerPlayer player = source.getPlayer();
        if (FabricMiscUtil.hasPermission(player, PermissionLevel.GAMEMASTERS)) return true;
        return plugin.getDependencies().hasPermission(player.getUUID(), ADMIN)
                || plugin.getDependencies().hasPermission(player.getUUID(), ADMIN + ".*");
    }

    public boolean hasAdminNode(final CommandSourceStack source, final String sub) {
        if (!source.isPlayer()) return true;
        final ServerPlayer player = source.getPlayer();
        if (FabricMiscUtil.hasPermission(player, PermissionLevel.GAMEMASTERS)) return true;
        return plugin.getDependencies().hasPermission(player.getUUID(), ADMIN + ".*")
                || plugin.getDependencies().hasPermission(player.getUUID(), ADMIN + "." + sub);
    }
}