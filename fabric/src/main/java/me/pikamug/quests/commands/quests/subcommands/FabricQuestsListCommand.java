/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands.quests.subcommands;

import com.mojang.brigadier.Command;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.commands.FabricQuestsSubCommand;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FabricQuestsListCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsListCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_LIST");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_LIST_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.list";
    }

    @Override
    public String getSyntax() {
        return "/quests list";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (!source.isPlayer()) {
            int num = 1;
            reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questListTitle")), false);
            if (plugin.getLoadedQuests().isEmpty()) {
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "noQuests")), false);
                return Command.SINGLE_SUCCESS;
            }
            for (final Quest q : plugin.getLoadedQuests()) {
                final int lineNo = num;
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + String.valueOf(lineNo)
                        + ". " + q.getName()), false);
                num++;
            }
            return Command.SINGLE_SUCCESS;
        }
        final int page;
        if (args.length == 1) {
            page = 1;
        } else {
            try {
                page = Integer.parseInt(args[1]);
            } catch (final NumberFormatException e) {
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "pageSelectionNum")),
                        false);
                return 0;
            }
        }
        if (page < 1) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "pageSelectionPosNum")), false);
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        quester.listQuests(quester, page);
        return Command.SINGLE_SUCCESS;
    }
}