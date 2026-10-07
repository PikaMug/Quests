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
import me.pikamug.quests.item.FabricQuestJournal;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class FabricQuestsJournalCommand extends FabricQuestsSubCommand {

    private final FabricQuestsPlugin plugin;

    public FabricQuestsJournalCommand(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "journal";
    }

    @Override
    public String getNameI18N() {
        return FabricLang.get("COMMAND_JOURNAL");
    }

    @Override
    public String getDescription() {
        return FabricLang.get("COMMAND_JOURNAL_HELP");
    }

    @Override
    public String getPermission() {
        return "quests.journal";
    }

    @Override
    public String getSyntax() {
        return "/quests journal";
    }

    @Override
    public int getMaxArguments() {
        return 1;
    }

    @Override
    public int execute(final CommandSourceStack source, final String[] args) {
        if (assertNonPlayer(source)) {
            return 0;
        }
        final ServerPlayer player = source.getPlayer();
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final Inventory inv = player.getInventory();
        final int index = quester.getJournalIndex();
        if (index != -1) {
            inv.setItem(index, ItemStack.EMPTY);
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalPutAway")
                    .replace("<journal>", lang(player, "journalTitle"))));
        } else if (player.getMainHandItem().isEmpty()) {
            final FabricQuestJournal journal = new FabricQuestJournal(plugin, quester);
            player.getInventory().setItem(player.getInventory().getSelectedSlot(), journal.toItemStack());
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalTaken")
                    .replace("<journal>", lang(player, "journalTitle"))));
        } else if (inv.getFreeSlot() != -1) {
            final FabricQuestJournal journal = new FabricQuestJournal(plugin, quester);
            inv.add(journal.toItemStack());
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalTaken")
                    .replace("<journal>", lang(player, "journalTitle"))));
        } else {
            player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + lang(player, "journalNoRoom")
                    .replace("<journal>", lang(player, "journalTitle"))));
        }
        return Command.SINGLE_SUCCESS;
    }
}