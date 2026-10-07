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

public class FabricQuestadminStatsCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestadminStatsCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_QUESTADMIN_STATS");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_QUESTADMIN_STATS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.stats";
    }

    @Override
    public String getSyntax() {
        return "/questadmin stats";
    }

    @Override
    public int getMaxArguments() {
        return 2;
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
        displayStats(source, quester);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public java.util.List<String> tabComplete(final CommandSourceStack source, final String[] args) {
        if (args.length == 2) {
            return null;
        }
        return java.util.Collections.emptyList();
    }
}