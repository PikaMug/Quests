/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;
import java.util.Map;

public class FabricChatListener {

    private final FabricQuestsPlugin plugin;

    public FabricChatListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, player, chatType) -> {
            if (plugin.isLoading() || message == null || player == null) return true;
            final FabricQuester quester = plugin.getQuester(player.getUUID());
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.getCurrentQuests().containsKey(quest)) continue;
                final Stage stage = quester.getCurrentStage(quest);
                if (stage == null || stage.getPasswordPhrases().isEmpty()) continue;
                for (final String phrase : stage.getPasswordPhrases()) {
                    if (phrase != null && phrase.equalsIgnoreCase(message.signedContent())) {
                        return false;
                    }
                }
            }
            return true;
        });

        ServerMessageEvents.CHAT_MESSAGE.register((message, player, chatType) -> {
            if (plugin.isLoading() || message == null || player == null) return;
            handleChat(player, message.signedContent());
        });

        ServerMessageEvents.COMMAND_MESSAGE.register((message, source, chatType) -> {
            if (plugin.isLoading() || source == null || source.getPlayer() == null) return;
            handleCommand(source.getPlayer(), message.signedContent());
        });
    }

    private void handleChat(ServerPlayer player, String content) {
        if (content == null) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            if (!stage.getPasswordPhrases().isEmpty()) {
                boolean matched = false;
                for (int i = 0; i < stage.getPasswordPhrases().size(); i++) {
                    final String phrase = stage.getPasswordPhrases().get(i);
                    if (phrase != null && phrase.equalsIgnoreCase(content)) {
                        quester.getQuestProgressOrDefault(quest).getPasswordsSaid().set(i, true);
                        matched = true;
                    }
                }
                if (matched) quester.checkQuest(quest);
            }

            if (!stage.getChatActions().isEmpty()) {
                for (final Map.Entry<String, Action> entry : stage.getChatActions().entrySet()) {
                    if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(content)) {
                        entry.getValue().fire(quester, quest);
                    }
                }
            }
        }
    }

    private void handleCommand(ServerPlayer player, String content) {
        if (content == null) return;
        if (!content.startsWith("/")) content = "/" + content;
        final String lowerContent = content.toLowerCase(Locale.ENGLISH);
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null || stage.getCommandActions().isEmpty()) continue;
            for (final Map.Entry<String, Action> entry : stage.getCommandActions().entrySet()) {
                final String key = entry.getKey();
                if (key == null) continue;
                final String command = key.startsWith("/") ? key : "/" + key;
                final String lowerCommand = command.toLowerCase(Locale.ENGLISH);
                if (lowerContent.equals(lowerCommand)
                        || lowerContent.startsWith(lowerCommand + " ")) {
                    entry.getValue().fire(quester, quest);
                }
            }
        }
    }
}