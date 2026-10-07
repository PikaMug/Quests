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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FabricQuestsQuitCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsQuitCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "quit";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_QUIT");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_QUIT_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.quit";
    }

    @Override
    public String getSyntax() {
        return "/quests quit";
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
        if (assertNonPlayer(source)) {
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        if (quester.getCurrentQuests().isEmpty()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "noActiveQuest")));
            return 0;
        }
        final String questName = concatArgArray(args, 1, args.length - 1, ' ');
        final Quest quest = plugin.getQuest(questName != null ? questName.toLowerCase() : null);
        if (quest == null) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")));
            return 0;
        }
        if (quest.getOptions().canAllowQuitting()) {
            final String msg = lang(player, "questQuit").replace("<quest>", quest.getName());
            quester.quitQuest(quest, msg);
        } else {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "questQuitDisabled")));
        }
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<String> tabComplete(final CommandSourceStack source, final String[] args) {
        if (args.length == 2) {
            final List<String> results = new ArrayList<>();
            if (source.isPlayer()) {
                final FabricQuester quester = plugin.getQuester(source.getPlayer().getUUID());
                if (quester != null) {
                    for (final Quest quest : quester.getCurrentQuests().keySet()) {
                        if (quest.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            results.add(ChatFormatting.stripFormatting(quest.getName()));
                        }
                    }
                }
            } else {
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (quest.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        results.add(ChatFormatting.stripFormatting(quest.getName()));
                    }
                }
            }
            return results;
        }
        return Collections.emptyList();
    }
}