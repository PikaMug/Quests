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
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FabricQuestsTopCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsTopCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "top";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_TOP");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_TOP_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.top";
    }

    @Override
    public String getSyntax() {
        return "/quests top";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        final int limit = plugin.getConfigSettings().getTopLimit();
        final ServerPlayer player = source.isPlayer() ? source.getPlayer() : null;
        final int topNumber;
        if (args.length == 1) {
            topNumber = 5; // default
        } else {
            try {
                topNumber = Integer.parseInt(args[1]);
            } catch (final NumberFormatException e) {
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "inputNum")), false);
                return 0;
            }
        }
        if (topNumber < 1 || topNumber > limit) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW
                    + lang(player, "invalidRange").replace("<least>", "1")
                    .replace("<greatest>", String.valueOf(limit))), false);
            return Command.SINGLE_SUCCESS;
        }
        final Map<String, Integer> questPoints = new LinkedHashMap<>();
        for (final Quester quester : plugin.getOfflineQuesters()) {
            if (quester.getLastKnownName() != null) {
                questPoints.put(quester.getLastKnownName(), quester.getQuestPoints());
            }
        }
        final List<Map.Entry<String, Integer>> sorted = new ArrayList<>(questPoints.entrySet());
        sorted.sort(Map.Entry.comparingByValue(java.util.Collections.reverseOrder()));
        reply(source,() -> Component.literal(ChatFormatting.GOLD
                + lang(player, "topQuestersTitle").replace("<number>", String.valueOf(topNumber))), false);
        int printed = 0;
        for (final Map.Entry<String, Integer> entry : sorted) {
            printed++;
            final int lineNo = printed;
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + String.valueOf(lineNo) + ". "
                    + entry.getKey() + " - " + ChatFormatting.DARK_PURPLE + String.valueOf(entry.getValue())
                    + ChatFormatting.YELLOW + " " + lang(player, "questPoints")), false);
            if (printed >= topNumber) break;
        }
        return Command.SINGLE_SUCCESS;
    }
}