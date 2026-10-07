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

public class FabricQuestadminSetStageCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestadminSetStageCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setstage";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_QUESTADMIN_SETSTAGE");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_QUESTADMIN_SETSTAGE_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.setstage";
    }

    @Override
    public String getSyntax() {
        return "/questadmin setstage";
    }

    @Override
    public int getMaxArguments() {
        return 4;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (args.length < 4) {
            return 0;
        }
        final String rest = concatArgArray(args, 2, args.length - 1, ' ');
        if (rest == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "inputNum")), false);
            return 0;
        }
        final String[] parts = rest.split(" ");
        if (parts.length < 2) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "inputNum")), false);
            return 0;
        }
        int stage;
        try {
            stage = Integer.parseInt(parts[parts.length - 1]);
        } catch (final NumberFormatException e) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "inputNum")), false);
            return 0;
        }
        final String questName = String.join(" ", java.util.Arrays.copyOf(parts, parts.length - 1));
        final FabricQuester quester = resolveTarget(plugin, source, args[1]);
        if (quester == null) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "playerNotFound")), false);
            return 0;
        }
        final String targetName = quester.getLastKnownName() != null ? quester.getLastKnownName() : args[1];
        if (quester.getCurrentQuests().isEmpty()) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(source, "noCurrentQuest")
                    .replace("<player>", targetName)), false);
            return 0;
        }
        final Quest quest = findQuest(plugin, questName);
        if (quest == null) {
            reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "questNotFound")
                    .replace("<input>", questName)), false);
            return 0;
        }
        if (!quester.getCurrentQuests().containsKey(quest)) {
            final String msg1 = lang(source, "questForceTake")
                    .replace("<player>", targetName)
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
        }
        final int index = stage - 1;
        if (index < 0 || index >= quest.getStages().size()) {
            reply(source,() -> Component.literal(ChatFormatting.RED + lang(source, "invalidRange")
                    .replace("<least>", "1")
                    .replace("<greatest>", String.valueOf(quest.getStages().size()))), false);
            return 0;
        }
        quest.setStage(quester, index);
        quester.saveData();
        return Command.SINGLE_SUCCESS;
    }

    @Override
    public List<String> tabComplete(final CommandSourceStack source, final String[] args) {
        if (args.length == 2) {
            return null;
        }
        if (args.length == 3) {
            final List<String> results = new ArrayList<>();
            final FabricQuester quester = resolveTarget(plugin, source, args[1]);
            if (quester != null && !quester.getCurrentQuests().isEmpty()) {
                for (final Quest quest : quester.getCurrentQuests().keySet()) {
                    if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        results.add(ChatFormatting.stripFormatting(quest.getName()));
                    }
                }
            } else {
                for (final Quest quest : plugin.getLoadedQuests()) {
                    if (quest.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        results.add(ChatFormatting.stripFormatting(quest.getName()));
                    }
                }
            }
            return results;
        }
        if (args.length > 3) {
            final String questName = concatArgArray(args, 2, args.length - 2, ' ');
            final Quest quest = findQuest(plugin, questName);
            if (quest != null) {
                final List<String> results = new ArrayList<>();
                for (int i = 1; i <= quest.getStages().size(); i++) {
                    results.add(questName + " " + i);
                }
                return results;
            }
        }
        return Collections.emptyList();
    }
}