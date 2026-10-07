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

public class FabricQuestadminTakePointsCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestadminTakePointsCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "takepoints";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_QUESTADMIN_TAKEPOINTS");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_QUESTADMIN_TAKEPOINTS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.takepoints";
    }

    @Override
    public String getSyntax() {
        return "/questadmin takepoints";
    }

    @Override
    public int getMaxArguments() {
        return 3;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (args.length < 3) {
            return 0;
        }
        final FabricQuester quester = resolveTarget(plugin, source, args[1]);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        final int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (final NumberFormatException e) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "inputNum")), false);
            return 0;
        }
        quester.setQuestPoints(Math.max(0, quester.getQuestPoints() - amount));
        final String msg1 = lang(source, "takeQuestPoints")
                .replace("<points>", lang(source, "questPoints"))
                .replace("<player>", quester.getLastKnownName())
                .replace("<number>", String.valueOf(amount));
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questPointsTaken")
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