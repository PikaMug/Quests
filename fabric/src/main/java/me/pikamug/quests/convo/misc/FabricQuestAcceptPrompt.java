/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.misc;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.config.FabricConfigSettings;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.browsit.conversations.api.Conversations;
import org.browsit.conversations.api.clause.TimeClause;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FabricQuestAcceptPrompt extends FabricMiscStringPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricQuestAcceptPrompt(final @NotNull UUID uuid, FabricQuestsPlugin plugin) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = plugin;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @SuppressWarnings("unused")
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
            case 1:
                return ChatFormatting.GREEN;
            case 2:
                return ChatFormatting.RED;
            default:
                return null;
        }
    }

    @SuppressWarnings("unused")
    public String getSelectionText(final int number) {
        switch (number) {
            case 1:
                return ChatFormatting.GREEN + FabricLang.get("yesWord");
            case 2:
                return ChatFormatting.RED + FabricLang.get("noWord");
            default:
                return null;
        }
    }

    @Override
    public String getQueryText() {
        return FabricLang.get("acceptQuest");
    }

public @NotNull String getPromptText() {
        if (plugin == null) {
            return ChatFormatting.YELLOW + FabricLang.get("itemCreateCriticalError");
        }
        final FabricConfigSettings configSettings = (FabricConfigSettings) plugin.getConfigSettings();
        if (!configSettings.canClickablePrompts()) {
            return ChatFormatting.YELLOW + getQueryText() + "  " + ChatFormatting.GREEN
                    + getSelectionText(1) + ChatFormatting.RESET + " / " + getSelectionText(2);
        }

        final MutableComponent component = Component.literal("");
        component.append(Component.literal(ChatFormatting.YELLOW + getQueryText() + "  " + ChatFormatting.GREEN)
                .withStyle(Style.EMPTY));
        final MutableComponent yes = Component.literal(getSelectionText(1))
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent.RunCommand("/quests choice " + FabricLang.get("yesWord"))));
        component.append(yes);
        component.append(Component.literal(ChatFormatting.RESET + " / ")
                .withStyle(Style.EMPTY));
        final MutableComponent no = Component.literal(getSelectionText(2))
                .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                        .withClickEvent(new ClickEvent.RunCommand("/quests choice " + FabricLang.get("noWord"))));
        component.append(no);

        final ServerPlayer player = plugin.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            player.sendSystemMessage(component);
        }
        return "";
    }

    public void acceptInput(final String input) {
        if (plugin == null || input == null) {
            return;
        }
        final ServerPlayer player = plugin.getServer().getPlayerList().getPlayer(uuid);
        if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("y")
                || input.equalsIgnoreCase(FabricLang.get("yesWord"))
                || (player != null && input.equalsIgnoreCase(FabricLang.get(player, "yesWord")))) {
            final me.pikamug.quests.player.Quester quester = plugin.getQuester(uuid);
            if (quester == null) {
                plugin.getPluginLogger().info("Ended conversation because quester for " +
                        (player != null ? player.getName().getString() : uuid) + " was null");
                return;
            }
            final String questIdToTake = quester.getQuestIdToTake();
            final Quest quest = getQuestById(questIdToTake);
            if (quest == null) {
                plugin.getPluginLogger().warning((player != null ? player.getName().getString() : uuid.toString())
                        + " attempted to take quest ID \"" + questIdToTake + "\" but something went wrong");
                if (player != null) {
                    player.sendSystemMessage(Component.literal(ChatFormatting.RED
                            + "Something went wrong! Please report issue to an administrator."));
                }
            } else {
                quester.takeQuest(quest, false);
            }
        } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("n")
                || input.equalsIgnoreCase(FabricLang.get("noWord"))
                || (player != null && input.equalsIgnoreCase(FabricLang.get(player, "noWord")))) {
            if (player != null) {
                player.sendSystemMessage(Component.literal(ChatFormatting.YELLOW + FabricLang.get("cancelled")));
            }
        } else {
            final String msg = FabricLang.get("questInvalidChoice")
                    .replace("<yes>", FabricLang.get("yesWord"))
                    .replace("<no>", FabricLang.get("noWord"));
            if (player != null) {
                player.sendSystemMessage(Component.literal(ChatFormatting.RED + msg));
            }
        }
    }

    private Quest getQuestById(final String id) {
        if (id == null) return null;
        for (final Quest q : plugin.getLoadedQuests()) {
            if (q.getId().equals(id)) {
                return q;
            }
        }
        return null;
    }

    public void start() {
        Conversations.create(uuid).title(getName())
                .prompt(getPromptText(), String.class, prompt -> prompt
                        .converter(String::valueOf)
                        .conversionFailText(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError"))
                        .fetch((input, sender) -> acceptInput(input)))
                .endWhen(TimeClause.create(
                        ((FabricConfigSettings) plugin.getConfigSettings()).getAcceptTimeout() * 1000L,
                        ChatFormatting.YELLOW + FabricLang.get("questTimeout")))
                .start();
    }
}
