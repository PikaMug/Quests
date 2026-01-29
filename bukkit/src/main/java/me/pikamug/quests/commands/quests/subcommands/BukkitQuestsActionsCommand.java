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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.BukkitQuestsSubCommand;
import me.pikamug.quests.convo.actions.menu.ActionMenuPrompt;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.SessionData;
import org.browsit.conversations.api.Conversations;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitQuestsActionsCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsActionsCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "actions";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_EVENTS_EDITOR");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_EVENTS_EDITOR_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.actions";
    }

    @Override
    public String getSyntax() {
        return "/quests actions";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.actions.*") || cs.hasPermission("quests.actions.editor")
                || cs.hasPermission("quests.mode.trial")) {
            if (cs instanceof Player) {
                final UUID uuid = ((Player)cs).getUniqueId();
                if (Conversations.getConversationOf(uuid).isPresent()) {
                    cs.sendMessage(ChatColor.RED + BukkitLang.get(cs, "duplicateEditor"));
                    return;
                }
                SessionData.clear(uuid);
                new ActionMenuPrompt(uuid).start();
            }
            // TODO - determine how to run from console


            /*final Conversable c = (Conversable) cs;
            if (!c.isConversing()) {
                plugin.getActionFactory().getConversationFactory().buildConversation(c).begin();
            } else {
                cs.sendMessage(ChatColor.RED + BukkitLang.get(cs, "duplicateEditor"));
            }*/
        } else {
            cs.sendMessage(ChatColor.RED + BukkitLang.get(cs, "noPermission"));
        }
    }
}
