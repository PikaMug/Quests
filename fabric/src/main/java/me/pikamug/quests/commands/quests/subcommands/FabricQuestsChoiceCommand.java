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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.data.Conversation;

import java.util.Optional;

public class FabricQuestsChoiceCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsChoiceCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "choice";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_CHOICE");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_CHOICE_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.choice";
    }

    @Override
    public String getSyntax() {
        return "/quests choice";
    }

    @Override
    public int getMaxArguments() {
        return 2;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (assertNonPlayer(source)) {
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        final Optional<Conversation> conversation = Conversations.getConversationOf(player.getUUID());
        if (!conversation.isPresent()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "notConversing")));
            return Command.SINGLE_SUCCESS;
        }
        if (args.length == 1) {
            // Shows command usage
            return 0;
        }
        final String input = concatArgArray(args, 1, args.length - 1, ' ');
        if (input == null) {
            return Command.SINGLE_SUCCESS;
        }
        conversation.get().handleInput(input);
        return Command.SINGLE_SUCCESS;
    }
}