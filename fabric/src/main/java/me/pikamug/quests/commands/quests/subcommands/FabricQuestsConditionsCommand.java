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
import me.pikamug.quests.convo.conditions.menu.FabricConditionMenuPrompt;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.browsit.conversations.api.Conversations;

public class FabricQuestsConditionsCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsConditionsCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "conditions";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_CONDITIONS_EDITOR");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_CONDITIONS_EDITOR_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.conditions";
    }

    @Override
    public String getSyntax() {
        return "/quests conditions";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (!source.isPlayer()) {
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        if (Conversations.getConversationOf(player.getUUID()).isPresent()) {
            player.sendSystemMessage(Component.literal(ChatFormatting.RED + lang(player, "duplicateEditor")));
            return Command.SINGLE_SUCCESS;
        }
        SessionData.clear(player.getUUID());
        new FabricConditionMenuPrompt(player.getUUID()).start();
        return Command.SINGLE_SUCCESS;
    }
}