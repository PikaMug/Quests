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
import me.pikamug.quests.convo.quests.menu.QuestMenuPrompt;
import me.pikamug.quests.events.command.BukkitQuestsCommandPreEditorEvent;
import me.pikamug.quests.player.BukkitQuester;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.SessionData;
import org.browsit.conversations.api.Conversations;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitQuestsEditorCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsEditorCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "editor";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_EDITOR");
    }

    @Override
    public String getDescription() {
        return BukkitLang.get("COMMAND_EDITOR_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.editor";
    }

    @Override
    public String getSyntax() {
        return "/quests editor";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.editor.*") || cs.hasPermission("quests.editor.editor")
                || cs.hasPermission("quests.mode.trial")) {
            if (cs instanceof Player) { // TODO - determine how to also run from console
                final UUID uuid = ((Player)cs).getUniqueId();
                if (Conversations.getConversationOf(uuid).isPresent()) {
                    cs.sendMessage(ChatColor.RED + BukkitLang.get(cs, "duplicateEditor"));
                    return;
                }
                final BukkitQuester quester = plugin.getQuester(uuid);
                final BukkitQuestsCommandPreEditorEvent event = new BukkitQuestsCommandPreEditorEvent(quester);
                plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }
                SessionData.clear(uuid);
                new QuestMenuPrompt(uuid).start();
            }
        } else {
            cs.sendMessage(ChatColor.RED + BukkitLang.get(cs, "noPermission"));
        }
    }
}
