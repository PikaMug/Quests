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
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class FabricQuestadminReloadCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestadminReloadCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_QUESTADMIN_RELOAD");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_QUESTADMIN_RELOAD_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.admin.reload";
    }

    @Override
    public String getSyntax() {
        return "/questadmin reload";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questsReloading")), false);
        plugin.reload();
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "questsReloaded")), false);
        reply(source,() -> Component.literal(ChatFormatting.GOLD + lang(source, "numQuestsLoaded")
                .replace("<number>", String.valueOf(plugin.getLoadedQuests().size()))), false);
        return Command.SINGLE_SUCCESS;
    }
}