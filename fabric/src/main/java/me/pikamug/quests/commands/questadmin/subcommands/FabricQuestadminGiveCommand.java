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
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FabricQuestadminGiveCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestadminGiveCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_QUESTADMIN_GIVE");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_QUESTADMIN_GIVE_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.give";
    }

    @Override
    public String getSyntax() {
        return "/questadmin give";
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
        final String questName = concatArgArray(args, 2, args.length - 1, ' ');
        final Quest quest = findQuest(plugin, questName);
        if (quest == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "questNotFound")
                    .replace("<input>", questName != null ? questName : "")), false);
            return 0;
        }
        for (final Quest q : quester.getCurrentQuests().keySet()) {
            if (q.getId() != null && q.getId().equals(quest.getId())) {
                final String msg = lang(source, "questsPlayerHasQuestAlready")
                        .replace("<player>", quester.getLastKnownName())
                        .replace("<quest>", quest.getName());
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + msg), false);
                return 0;
            }
        }
        quester.hardQuit(quest);
        final String msg1 = lang(source, "questForceTake")
                .replace("<player>", quester.getLastKnownName())
                .replace("<quest>", quest.getName());
        reply(source,() -> Component.literal(ChatFormatting.GOLD + msg1), false);
        final ServerPlayer onlineTarget = source.getServer().getPlayerList().getPlayer(quester.getUUID());
        if (onlineTarget != null) {
            final String msg2 = lang(onlineTarget, "questForcedTake")
                    .replace("<player>", source.isPlayer() ? source.getPlayer().getName().getString() : "Console")
                    .replace("<quest>", quest.getName());
            onlineTarget.sendSystemMessage(Component.literal(ChatFormatting.GREEN + msg2));
        }
        quester.takeQuest(quest, true);
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<String> tabComplete(final CommandSourceStack source, final String[] args) {
        if (args.length == 2) {
            return null;
        }
        if (args.length == 3) {
            final List<String> results = new ArrayList<>();
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    results.add(ChatFormatting.stripFormatting(quest.getName()));
                }
            }
            return results;
        }
        return Collections.emptyList();
    }
}