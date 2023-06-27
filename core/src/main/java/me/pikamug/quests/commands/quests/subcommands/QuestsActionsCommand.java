/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.pikamug.quests.commands.quests.subcommands;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.commands.QuestsSubCommand;
import me.pikamug.quests.util.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;

public class QuestsActionsCommand extends QuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public QuestsActionsCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "actions";
    }

    @Override
    public String getNameI18N() {
        return Language.get("COMMAND_EVENTS_EDITOR");
    }

    @Override
    public String getDescription() {
        return Language.get("COMMAND_EVENTS_EDITOR_HELP");
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
        if (cs.hasPermission("quests.events.*") || cs.hasPermission("quests.actions.*")
                || cs.hasPermission("quests.actions.editor") || cs.hasPermission("quests.events.editor")
                || cs.hasPermission("quests.mode.trial")) {
            final Conversable c = (Conversable) cs;
            if (!c.isConversing()) {
                plugin.getActionFactory().getConversationFactory().buildConversation(c).begin();
            } else {
                cs.sendMessage(ChatColor.RED + Language.get(cs, "duplicateEditor"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Language.get(cs, "noPermission"));
        }
    }
}
