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

import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.Action;
import me.pikamug.quests.enums.ObjectiveType;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FabricChatListener {

    private final FabricQuestsPlugin plugin;

    public FabricChatListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        FabricMixinEvents.registerChatAllow((player, content) -> {
            if (plugin.isLoading() || player == null || content == null) return true;
            final FabricQuester quester = plugin.getQuester(player.getUUID());
            for (final Quest quest : plugin.getLoadedQuests()) {
                if (!quester.meetsCondition(quest, true)) continue;
                if (!quester.getCurrentQuests().containsKey(quest)) continue;
                final Stage stage = quester.getCurrentStage(quest);
                if (stage == null || stage.getPasswordPhrases().isEmpty()) continue;
                for (final String phrase : stage.getPasswordPhrases()) {
                    if (phrase != null && phrase.equalsIgnoreCase(content)) {
                        return false;
                    }
                }
            }
            return true;
        });

        FabricMixinEvents.registerChatMessage((player, content) -> {
            if (plugin.isLoading() || player == null || content == null) return;
            handleChat(player, content);
        });

        FabricMixinEvents.registerCommandMessage((player, content) -> {
            if (plugin.isLoading() || player == null || content == null) return;
            handleCommand(player, content);
        });
    }

    private void handleChat(ServerPlayer player, String content) {
        if (content == null) return;
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        final ObjectiveType type = ObjectiveType.PASSWORD;
        final Set<String> dispatchedQuestIDs = new HashSet<>();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.meetsCondition(quest, true)) continue;
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            if (!stage.getPasswordPhrases().isEmpty()) {
                for (int i = 0; i < stage.getPasswordPhrases().size(); i++) {
                    final String phrase = stage.getPasswordPhrases().get(i);
                    if (phrase != null && phrase.equalsIgnoreCase(content)) {
                        applyPasswordProgress(quester, quest, i);
                        final int finalIndex = i;
                        dispatchedQuestIDs.addAll(quester.dispatchMultiplayerEverything(quest, type, (q, cq) -> {
                            if (!dispatchedQuestIDs.contains(cq.getId())) {
                                applyPasswordProgress((FabricQuester) q, cq, finalIndex);
                            }
                            return null;
                        }));
                        break;
                    }
                }
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

    private void applyPasswordProgress(FabricQuester quester, Quest quest, int index) {
        final Stage stage = quester.getCurrentStage(quest);
        if (stage == null || index < 0 || index >= stage.getPasswordPhrases().size()) return;
        final me.pikamug.quests.player.QuestProgress progress = quester.getQuestProgressOrDefault(quest);
        if (progress == null || index >= progress.getPasswordsSaid().size()) return;
        if (!progress.getPasswordsSaid().get(index)) {
            progress.getPasswordsSaid().set(index, true);
            quester.finishObjective(quest, ObjectiveType.PASSWORD, index);
        }
        quester.checkQuest(quest);
    }

    private void handleCommand(ServerPlayer player, String content) {
        if (content == null) return;
        if (!content.startsWith("/")) content = "/" + content;
        final String lowerContent = content.toLowerCase(Locale.ENGLISH);
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;
            // Bukkit parity minus the blanket cancellation: warn the player and skip the
            // stage's command actions, but never block unrelated server commands.
            final boolean allowed = quest.getOptions().canAllowCommands()
                    || lowerContent.startsWith("/quest");
            if (!allowed) {
                quester.sendMessage(ChatFormatting.RED + FabricLang.get(quester.getPlayer(), "optCommandsDenied")
                        .replace("<quest>", quest.getName()));
                continue;
            }
            if (stage.getCommandActions().isEmpty()) continue;
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
