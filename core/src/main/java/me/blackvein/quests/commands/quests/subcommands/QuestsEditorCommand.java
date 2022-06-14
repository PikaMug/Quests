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

package me.blackvein.quests.commands.quests.subcommands;

import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.commands.QuestsSubCommand;
import me.blackvein.quests.events.command.QuestsCommandPreQuestsEditorEvent;
import me.blackvein.quests.util.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.entity.Player;

public class QuestsEditorCommand extends QuestsSubCommand {

    private final Quests plugin;

    public QuestsEditorCommand(Quests plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "editor";
    }

    @Override
    public String getNameI18N() {
        return Lang.get("COMMAND_EDITOR");
    }

    @Override
    public String getDescription() {
        return Lang.get("COMMAND_EDITOR_HELP");
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
            final Conversable c = (Conversable) cs;
            if (!c.isConversing()) {
                final Conversation cn = plugin.getQuestFactory().getConversationFactory().buildConversation(c);
                if (cs instanceof Player) {
                    final Quester quester = plugin.getQuester(((Player) cs).getUniqueId());
                    final QuestsCommandPreQuestsEditorEvent event
                            = new QuestsCommandPreQuestsEditorEvent(quester, cn.getContext());
                    plugin.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                }
                cn.begin();
            } else {
                cs.sendMessage(ChatColor.RED + Lang.get(cs, "duplicateEditor"));
            }
        } else {
            cs.sendMessage(ChatColor.RED + Lang.get(cs, "noPermission"));
        }
    }
}
