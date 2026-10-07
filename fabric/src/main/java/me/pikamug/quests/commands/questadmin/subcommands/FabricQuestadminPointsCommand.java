/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.questadmin.subcommands;

import com.mojang.brigadier.Command;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.commands.FabricQuestsSubCommand;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.List;

public class FabricQuestadminPointsCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestadminPointsCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "points";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_QUESTADMIN_POINTS");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_QUESTADMIN_POINTS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.points";
    }

    @Override
    public String getSyntax() {
        return "/questadmin points";
    }

    @Override
    public int getMaxArguments() {
        return 3;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (args.length == 1) {
            // Shows command usage
            return 0;
        }
        final FabricQuester quester = resolveTarget(plugin, source, args[1]);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        if (args.length == 2) {
            // View points (kept from fabric's original admin "points" behavior)
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + quester.getLastKnownName() + " - "
                    + ChatFormatting.DARK_PURPLE + quester.getQuestPoints() + ChatFormatting.YELLOW + " "
                    + lang(source, "questPoints")), false);
            return Command.SINGLE_SUCCESS;
        }
        final int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "inputNum")), false);
            return 0;
        }
        quester.setQuestPoints(amount);
        final String msg1 = lang(source, "setQuestPoints")
                .replace("<points>", lang(source, "questPoints"))
                .replace("<player>", quester.getLastKnownName())
                .replace("<number>", String.valueOf(amount));
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questPointsSet")
                    .replace("<points>", lang(onlineTarget, "questPoints"))
                    .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                    .replace("<number>", String.valueOf(amount));
            onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
        }
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<String> tabComplete(final CommandSourceStack source, final String[] args) {
        if (args.length == 2) {
            return null;
        }
        return Collections.emptyList();
    }
}