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
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class FabricQuestsInfoCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsInfoCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_INFO");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_INFO_HELP");
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
    public int execute(final CommandSourceStack source, final String[] args) {
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + "Quests "
                + ChatFormatting.GOLD + FabricQuestsPlugin.VERSION
                + ChatFormatting.GRAY + " (Fabric)"), false);
        reply(source,() -> Component.literal(ChatFormatting.GOLD
                + lang(source, "developedBy") + " PikaMug & contributors"), false);
        reply(source,() -> Component.literal(ChatFormatting.DARK_AQUA + "" + ChatFormatting.UNDERLINE
                + "https://github.com/PikaMug/Quests"), false);
        return Command.SINGLE_SUCCESS;
    }
}