/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners.npc;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.quests.components.Stage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/**
 * Abstract base class for NPC interaction handling on Fabric.
 * Concrete implementations exist for BOs-Easy-NPC and Taterzens.
 */
public abstract class FabricNpcListener {

    protected final FabricQuestsPlugin plugin;

    protected FabricNpcListener(FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void register();

    public abstract boolean isNpc(Entity entity);

    protected void handleNpcInteract(ServerPlayer player, Entity npcEntity) {
        if (plugin.isLoading() || player == null || npcEntity == null) return;
        final UUID npcUuid = npcEntity.getUUID();
        final FabricQuester quester = plugin.getQuester(player.getUUID());

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            // TALK_TO_NPC
            if (!stage.getNpcsToInteract().isEmpty()) {
                for (int i = 0; i < stage.getNpcsToInteract().size(); i++) {
                    if (npcUuid.equals(stage.getNpcsToInteract().get(i))) {
                        quester.getQuestProgressOrDefault(quest).getNpcsInteracted().set(i, true);
                    }
                }
            }
        }

        // Check if this NPC is a quest giver
        if (plugin.getQuestNpcUuids().contains(npcUuid)) {
            handleNpcQuestOffer(player, npcUuid);
        }
    }

    protected void handleNpcKill(ServerPlayer killer, UUID npcUuid) {
        if (plugin.isLoading() || killer == null) return;
        final FabricQuester quester = plugin.getQuester(killer.getUUID());

        for (final Quest quest : plugin.getLoadedQuests()) {
            if (!quester.getCurrentQuests().containsKey(quest)) continue;
            final Stage stage = quester.getCurrentStage(quest);
            if (stage == null) continue;

            if (!stage.getNpcsToKill().isEmpty()) {
                for (int i = 0; i < stage.getNpcsToKill().size(); i++) {
                    if (npcUuid.equals(stage.getNpcsToKill().get(i))) {
                        quester.getQuestProgressOrDefault(quest).getNpcsNumKilled().set(i,
                                quester.getQuestProgressOrDefault(quest).getNpcsNumKilled().get(i) + 1);
                    }
                }
            }
        }
    }

    private void handleNpcQuestOffer(ServerPlayer player, UUID npcUuid) {
        final FabricQuester quester = plugin.getQuester(player.getUUID());
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quest.getNpcStart() != null && quest.getNpcStart().equals(npcUuid)) {
                if (quester.canAcceptOffer(quest, false)) {
                    quester.offerQuest(quest, true);
                    return;
                }
            }
        }
    }
}
