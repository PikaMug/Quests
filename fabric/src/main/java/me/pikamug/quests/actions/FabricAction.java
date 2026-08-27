/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.actions;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.tasks.FabricActionTimer;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedList;

public class FabricAction implements Action {

    private String name;
    private String message;
    private boolean clearInv = false;
    private boolean failQuest = false;
    private int stormDuration = 0;
    private int thunderDuration = 0;
    private int timer = 0;
    private boolean cancelTimer = false;
    private LinkedList<QuestMob> mobSpawns = new LinkedList<>();
    private LinkedList<String> commands = new LinkedList<>();
    private int hunger = 0;
    private int saturation = 0;
    private float health = 0;
    private String book;
    private String denizenScript;

    @Override public String getName() { return name; }
    @Override public void setName(String v) { this.name = v; }
    @Override public String getMessage() { return message; }
    @Override public void setMessage(String v) { this.message = v; }
    @Override public boolean isClearInv() { return clearInv; }
    @Override public void setClearInv(boolean v) { this.clearInv = v; }
    @Override public boolean isFailQuest() { return failQuest; }
    @Override public void setFailQuest(boolean v) { this.failQuest = v; }
    @Override public int getStormDuration() { return stormDuration; }
    @Override public void setStormDuration(int v) { this.stormDuration = v; }
    @Override public int getThunderDuration() { return thunderDuration; }
    @Override public void setThunderDuration(int v) { this.thunderDuration = v; }
    @Override public int getTimer() { return timer; }
    @Override public void setTimer(int v) { this.timer = v; }
    @Override public boolean isCancelTimer() { return cancelTimer; }
    @Override public void setCancelTimer(boolean v) { this.cancelTimer = v; }
    @Override public LinkedList<QuestMob> getMobSpawns() { return mobSpawns; }
    @Override public void setMobSpawns(LinkedList<QuestMob> v) { this.mobSpawns = v; }
    @Override public LinkedList<String> getCommands() { return commands; }
    @Override public void setCommands(LinkedList<String> v) { this.commands = v; }
    @Override public int getHunger() { return hunger; }
    @Override public void setHunger(int v) { this.hunger = v; }
    @Override public int getSaturation() { return saturation; }
    @Override public void setSaturation(int v) { this.saturation = v; }
    @Override public float getHealth() { return health; }
    @Override public void setHealth(float v) { this.health = v; }
    @Override public String getBook() { return book; }
    @Override public void setBook(String v) { this.book = v; }
    @Override public String getDenizenScript() { return denizenScript; }
    @Override public void setDenizenScript(String v) { this.denizenScript = v; }

    @Override
    public void fire(Quester quester, Quest quest) {
        if (quester == null) return;

        // Message
        if (message != null && !message.isEmpty()) {
            quester.sendMessage(message);
        }

        // Get player
        final ServerPlayer player = getPlayer(quester);
        if (player == null) return;
        final MinecraftServer server = player.getServer();
        final ServerLevel level = player.serverLevel();

        // Clear inventory
        if (clearInv) {
            player.getInventory().clearContent();
        }

        // Fail quest
        if (failQuest && quest != null) {
            quest.failQuest(quester);
        }

        // Weather
        if (stormDuration > 0) {
            level.setWeatherDuration(0, true, false, stormDuration * 20);
        }
        if (thunderDuration > 0) {
            level.setWeatherDuration(0, false, true, thunderDuration * 20);
        }

        // Hunger and saturation
        if (hunger != 0) {
            final FoodData foodData = player.getFoodData();
            foodData.setFoodLevel(Math.max(0, Math.min(20, foodData.getFoodLevel() + hunger)));
        }
        if (saturation != 0) {
            final FoodData foodData = player.getFoodData();
            foodData.setSaturation(Math.max(0, Math.min(foodData.getFoodLevel(), foodData.getSaturationLevel() + saturation)));
        }

        // Health
        if (health > 0) {
            player.setHealth(Math.max(1.0f, Math.min(player.getMaxHealth(), player.getHealth() + health)));
        }

        // Commands
        if (commands != null && !commands.isEmpty()) {
            for (final String command : commands) {
                if (server != null) {
                    server.getCommands().performCommand(server.createCommandSourceStack(), command.replace("%player%", player.getName().getString()));
                }
            }
        }

        // Mob spawns
        if (mobSpawns != null && !mobSpawns.isEmpty()) {
            for (final QuestMob mob : mobSpawns) {
                spawnMob(player, level, mob);
            }
        }

        // Timer
        if (timer > 0) {
            new FabricActionTimer(FabricQuestsPlugin.getInstance(), quester, quest, timer);
        }
    }

    private void spawnMob(ServerPlayer player, ServerLevel level, QuestMob mob) {
        if (mob == null || mob.getType() == null) return;
        final EntityType<?> entityType = mob.getType();
        for (int i = 0; i < mob.getAmountToSpawn(); i++) {
            final var entity = entityType.create(level);
            if (entity instanceof Mob mobEntity) {
                mobEntity.moveTo(player.getX() + (Math.random() * 4) - 2,
                        player.getY() + (Math.random() * 4) - 2,
                        player.getZ() + (Math.random() * 4) - 2,
                        (float) (Math.random() * 360), 0);
                mobEntity.finalizeSpawn(level, level.getCurrentDifficultyAt(mobEntity.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null, null);
                level.addFreshEntity(mobEntity);
            }
        }
    }

    private ServerPlayer getPlayer(Quester quester) {
        final MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
        if (server == null) return null;
        return server.getPlayerList().getPlayerByName(quester.getName());
    }

    @Override
    public int compareTo(Action other) {
        if (other == null) return 1;
        if (this.name != null && other.getName() != null) {
            return this.name.compareTo(other.getName());
        }
        return 0;
    }
}
