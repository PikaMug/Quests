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
import me.pikamug.quests.util.BukkitLang;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BukkitQuestsInfoCommand extends BukkitQuestsSubCommand {

    private final BukkitQuestsPlugin plugin;

    public BukkitQuestsInfoCommand(BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getNameI18N() {
        return BukkitLang.get("COMMAND_INFO");
    }

    public String getDescription() {
        return BukkitLang.get("COMMAND_INFO_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.info";
    }

    @Override
    public String getSyntax() {
        return "/quests info";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if (cs.hasPermission("quests.info")) {
            cs.sendMessage(ChatColor.YELLOW + "Quests " + ChatColor.GOLD + plugin.getDescription().getVersion());
            cs.sendMessage(ChatColor.GOLD + BukkitLang.get(cs, "developedBy") + " " + "PikaMug & contributors");
            cs.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.UNDERLINE + "https://github.com/PikaMug/Quests");
        }
    }
}
