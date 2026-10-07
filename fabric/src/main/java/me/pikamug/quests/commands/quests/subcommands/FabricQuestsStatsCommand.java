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
import me.pikamug.quests.util.FabricLang;
import net.minecraft.commands.CommandSourceStack;

public class FabricQuestsStatsCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsStatsCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_STATS");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_STATS_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.stats";
    }

    @Override
    public String getSyntax() {
        return "/quests stats";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (assertNonPlayer(source)) {
            return 0;
        }
        final FabricQuester quester = plugin.getQuester(source.getPlayer().getUUID());
        displayStats(source, quester);
        return Command.SINGLE_SUCCESS;
    }
}