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
import me.pikamug.quests.entity.FabricQuestMob;
import me.pikamug.quests.entity.QuestMob;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.tasks.FabricActionTimer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.WeatherData;

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
    private LinkedList<MobEffectInstance> potionEffects = new LinkedList<>();
    private LinkedList<ItemStack> items = new LinkedList<>();
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

    public LinkedList<MobEffectInstance> getPotionEffects() { return potionEffects; }
    public void setPotionEffects(LinkedList<MobEffectInstance> v) { this.potionEffects = v; }

    public LinkedList<ItemStack> getItems() { return items; }
    public void setItems(LinkedList<ItemStack> v) { this.items = v; }

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
        final MinecraftServer server = player.level().getServer();
        final ServerLevel level = player.level();

        // Clear inventory
        if (clearInv) {
            player.getInventory().clearContent();
        }

        // Fail quest
        if (failQuest && quest != null) {
            quest.failQuest(quester);
        }

        // Weather
        final WeatherData weatherData = level.getWeatherData();
        if (stormDuration > 0) {
            weatherData.setClearWeatherTime(0);
            weatherData.setRaining(true);
            weatherData.setRainTime(stormDuration * 20);
            weatherData.setThundering(false);
        }
        if (thunderDuration > 0) {
            weatherData.setClearWeatherTime(0);
            weatherData.setRaining(false);
            weatherData.setRainTime(thunderDuration * 20);
            weatherData.setThundering(true);
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

        // Potion effects
        if (potionEffects != null && !potionEffects.isEmpty()) {
            for (final MobEffectInstance effect : potionEffects) {
                player.addEffect(effect);
            }
        }

        // Item rewards
        if (items != null && !items.isEmpty()) {
            for (final ItemStack stack : items) {
                try {
                    me.pikamug.quests.util.FabricInventoryUtil.addItem(player, stack);
                } catch (final Exception e) {
                    FabricQuestsPlugin.LOGGER.warn("Unable to add null item to inventory of {} during quest {} event {}",
                            player.getName().getString(),
                            quest != null ? quest.getName() : "?",
                            name, e);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "<red>Quests encountered a problem with an item. Please contact an administrator."));
                }
            }
        }

        // Commands
        if (commands != null && !commands.isEmpty()) {
            for (final String command : commands) {
                if (server != null) {
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command.replace("%player%", player.getName().getString()));
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
            new FabricActionTimer(FabricQuestsPlugin.getInstance(), (FabricQuester) quester, quest, timer);
        }
    }

    private void spawnMob(ServerPlayer player, ServerLevel level, QuestMob mob) {
        if (!(mob instanceof FabricQuestMob fabricMob)
                || fabricMob.getEntityType() == null) return;
        final EntityType<?> entityType = fabricMob.getEntityType();
        final Integer amount = fabricMob.getSpawnAmounts();
        final int max = amount != null ? amount : 1;
        for (int i = 0; i < max; i++) {
            final var entity = entityType.create(level, EntitySpawnReason.COMMAND);
            if (entity instanceof Mob mobEntity) {
                mobEntity.setPos(player.getX() + (Math.random() * 4) - 2,
                        player.getY() + (Math.random() * 4) - 2,
                        player.getZ() + (Math.random() * 4) - 2);
                mobEntity.setYRot((float) (Math.random() * 360));
                mobEntity.setXRot(0);
                mobEntity.finalizeSpawn(level, level.getCurrentDifficultyAt(mobEntity.blockPosition()),
                        EntitySpawnReason.COMMAND, null);
                level.addFreshEntity(mobEntity);
            }
        }
    }

    private ServerPlayer getPlayer(Quester quester) {
        final MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
        if (server == null) return null;
        return server.getPlayerList().getPlayer(quester.getUUID());
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
