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
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricLocation;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FabricAction implements Action {

    private String name;
    private String message;
    private boolean clearInv = false;
    private boolean failQuest = false;
    protected LinkedList<FabricLocation> explosions = new LinkedList<>();
    protected Map<FabricLocation, SoundEvent> effects = new HashMap<>();
    protected LinkedList<ItemStack> items = new LinkedList<>();
    protected ServerLevel stormWorld = null;
    private int stormDuration = 0;
    protected ServerLevel thunderWorld = null;
    private int thunderDuration = 0;
    private int timer = 0;
    private boolean cancelTimer = false;
    private LinkedList<QuestMob> mobSpawns = new LinkedList<>();
    private LinkedList<FabricLocation> lightningStrikes = new LinkedList<>();
    private LinkedList<String> commands = new LinkedList<>();
    private LinkedList<MobEffect> potionEffects = new LinkedList<>();
    private int hunger = -1;
    private int saturation = -1;
    private float health = -1;
    private FabricLocation teleport;
    private String book = "";

    @Override
    public int compareTo(final Action action) {
        return name.compareTo(action.getName());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public boolean isClearInv() {
        return clearInv;
    }

    @Override
    public void setClearInv(final boolean clearInv) {
        this.clearInv = clearInv;
    }

    @Override
    public boolean isFailQuest() {
        return failQuest;
    }

    @Override
    public void setFailQuest(final boolean failQuest) {
        this.failQuest = failQuest;
    }

    public LinkedList<FabricLocation> getExplosions() {
        return explosions;
    }

    public void setExplosions(final LinkedList<FabricLocation> explosions) {
        this.explosions = explosions;
    }

    public Map<FabricLocation, SoundEvent> getEffects() {
        return effects;
    }

    public void setEffects(final Map<FabricLocation, SoundEvent> effects) {
        this.effects = effects;
    }

    public LinkedList<ItemStack> getItems() {
        return items;
    }

    public void setItems(final LinkedList<ItemStack> items) {
        this.items = items;
    }

    public ServerLevel getStormWorld() {
        return stormWorld;
    }

    public void setStormWorld(final ServerLevel stormWorld) {
        this.stormWorld = stormWorld;
    }

    @Override
    public int getStormDuration() {
        return stormDuration;
    }

    @Override
    public void setStormDuration(final int stormDuration) {
        this.stormDuration = stormDuration;
    }

    public ServerLevel getThunderWorld() {
        return thunderWorld;
    }

    public void setThunderWorld(final ServerLevel thunderWorld) {
        this.thunderWorld = thunderWorld;
    }

    @Override
    public int getThunderDuration() {
        return thunderDuration;
    }

    @Override
    public void setThunderDuration(final int thunderDuration) {
        this.thunderDuration = thunderDuration;
    }

    @Override
    public int getTimer() {
        return timer;
    }

    @Override
    public void setTimer(final int timer) {
        this.timer = timer;
    }

    @Override
    public boolean isCancelTimer() {
        return cancelTimer;
    }

    @Override
    public void setCancelTimer(final boolean cancelTimer) {
        this.cancelTimer = cancelTimer;
    }

    @Override
    public LinkedList<QuestMob> getMobSpawns() {
        return mobSpawns;
    }

    @Override
    public void setMobSpawns(final LinkedList<QuestMob> mobSpawns) {
        this.mobSpawns = mobSpawns;
    }

    public LinkedList<FabricLocation> getLightningStrikes() {
        return lightningStrikes;
    }

    public void setLightningStrikes(final LinkedList<FabricLocation> lightningStrikes) {
        this.lightningStrikes = lightningStrikes;
    }

    @Override
    public LinkedList<String> getCommands() {
        return commands;
    }

    @Override
    public void setCommands(final LinkedList<String> commands) {
        this.commands = commands;
    }

    public LinkedList<MobEffect> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(final LinkedList<MobEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    @Override
    public int getHunger() {
        return hunger;
    }

    @Override
    public void setHunger(final int hunger) {
        this.hunger = hunger;
    }

    @Override
    public int getSaturation() {
        return saturation;
    }

    @Override
    public void setSaturation(final int saturation) {
        this.saturation = saturation;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public void setHealth(final float health) {
        this.health = health;
    }

    public FabricLocation getTeleport() {
        return teleport;
    }

    public void setTeleport(final FabricLocation teleport) {
        this.teleport = teleport;
    }

    @Override
    public String getBook() {
        return book;
    }

    @Override
    public void setBook(final String book) {
        this.book = book;
    }

    @Override
    public void fire(final Quester quester, final Quest quest) {
        final FabricQuester fabricQuester = (FabricQuester)quester;
        final ServerPlayer player = fabricQuester.getPlayer();
        if (player == null) return;
        if (message != null) {
            quester.sendMessage(FabricMiscUtil.parseString(message));
        }
        if (clearInv) {
            player.getInventory().clearContent();
        }

        // Explosions
        if (explosions != null && !explosions.isEmpty()) {
            for (final FabricLocation loc : explosions) {
                if (loc != null && loc.getLevel() != null) {
                    loc.getLevel().explode(null, loc.getX(), loc.getY(), loc.getZ(), 4.0F, Level.ExplosionInteraction.BLOCK);
                }
            }
        }

        // Effects (particles)
        if (effects != null && !effects.isEmpty()) {
            for (final Map.Entry<FabricLocation, SoundEvent> entry : effects.entrySet()) {
                final FabricLocation loc = entry.getKey();
                if (loc == null || loc.getLevel() == null) continue;
                Object effectObj = entry.getValue();
                if (effectObj instanceof String) {
                    String effectStr = (String) effectObj;
                    final ParticleType<?> particle = BuiltInRegistries.PARTICLE_TYPE
                            .getValue(Identifier.tryBuild("minecraft", effectStr.toLowerCase()));
                    if (particle instanceof ParticleOptions) {
                        ParticleOptions particles = (ParticleOptions) particle;
                        loc.getLevel().sendParticles(particles, loc.getX(), loc.getY(), loc.getZ(), 1, 0, 0, 0, 0);
                    }
                } else if (effectObj instanceof SoundEvent) {
                    SoundEvent eff = (SoundEvent) effectObj;
                    loc.getLevel().playSound(null, loc.getX(), loc.getY(), loc.getZ(), eff, SoundSource.MASTER);
                    loc.getLevel().levelEvent(2002, BlockPos.containing(loc.getX(), loc.getY(), loc.getZ()), eff.getId());
                }
            }
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

        // Teleport
        if (teleport != null) {
            if (teleport.getLevel() != null && player.isAlive()) {
                player.teleportTo(teleport.getLevel(), teleport.getX(), teleport.getY(), teleport.getZ(),
                        java.util.Collections.emptySet(), teleport.getYaw(), teleport.getPitch(), false);
            }
        }

        // Potion effects
        if (potionEffects != null && !potionEffects.isEmpty()) {
            for (final MobEffect effect : potionEffects) {
                Holder<MobEffect> effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
                player.addEffect(new MobEffectInstance(effectHolder));
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

        // Lightning strikes
        if (lightningStrikes != null && !lightningStrikes.isEmpty()) {
            for (final FabricLocation loc : lightningStrikes) {
                if (loc == null || loc.getLevel() == null) continue;
                final LightningBolt bolt = FabricMiscUtil.LIGHTNING_BOLT.create(loc.getLevel(), EntitySpawnReason.TRIGGERED);
                if (bolt != null) {
                    bolt.setPos(loc.getX(), loc.getY(), loc.getZ());
                    loc.getLevel().addFreshEntity(bolt);
                }
            }
        }

        // Timer
        if (timer > 0) {
            final FabricQuester fq = (FabricQuester) quester;
            quester.sendMessage(FabricLang.get(player, "timerStart")
                    .replace("<time>", FabricMiscUtil.formatTime(timer * 1000L))
                    .replace("<quest>", quest != null ? quest.getName() : ""));
            final List<Integer> toNotify = Arrays.asList(60, 30, 10, 5, 4, 3, 2, 1);
            for (final int seconds : toNotify) {
                if (timer > seconds) {
                    final FabricActionTimer notifyTimer = new FabricActionTimer(fq, quest, seconds);
                    FabricScheduler.runLater(notifyTimer, (timer - seconds) * 20L);
                    fq.getActionTimers().put(notifyTimer, quest);
                }
            }
            final FabricActionTimer failTimer = new FabricActionTimer(fq, quest, 0);
            FabricScheduler.runLater(failTimer, timer * 20L);
            fq.getActionTimers().put(failTimer, quest);
        }
        if (cancelTimer) {
            final Iterator<Map.Entry<FabricActionTimer, Quest>> it =
                    ((FabricQuester) quester).getActionTimers().entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<FabricActionTimer, Quest> entry = it.next();
                if (quest != null && entry.getValue().getId().equals(quest.getId())) {
                    entry.getKey().cancel();
                    it.remove();
                }
            }
        }
    }

    private void spawnMob(ServerPlayer player, ServerLevel level, QuestMob mob) {
        if (!(mob instanceof FabricQuestMob))
            return;
        FabricQuestMob fabricMob = (FabricQuestMob) mob;
        if (fabricMob.getEntityType() == null) return;
        final EntityType<?> entityType = fabricMob.getEntityType();
        final Integer amount = fabricMob.getSpawnAmounts();
        final int max = amount != null ? amount : 1;
        for (int i = 0; i < max; i++) {
            final EntityType<?> entity = entityType.create(level, EntitySpawnReason.COMMAND);
            if (entity instanceof Mob) {
                Mob mobEntity = (Mob) entity;
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

    private FabricLocation parseLocation(MinecraftServer server, String locStr) {
        if (server == null || locStr == null || locStr.isEmpty()) return null;
        final String[] parts = locStr.split(":");
        if (parts.length < 2) return null;
        final String worldName = parts[0];
        final String[] coords = parts[1].split(",");
        if (coords.length < 3) return null;
        ServerLevel level = null;
        for (final ServerLevel sl : server.getAllLevels()) {
            final String path = sl.dimension().identifier().getPath();
            final String full = sl.dimension().identifier().toString();
            if (path.equalsIgnoreCase(worldName) || full.equalsIgnoreCase(worldName)) {
                level = sl;
                break;
            }
        }
        if (level == null) return null;
        try {
            final double x = Double.parseDouble(coords[0]);
            final double y = Double.parseDouble(coords[1]);
            final double z = Double.parseDouble(coords[2]);
            float yaw = 0.0f;
            float pitch = 0.0f;
            if (coords.length > 3) {
                try { yaw = Float.parseFloat(coords[3]); } catch (Exception ignored) {}
            }
            if (coords.length > 4) {
                try { pitch = Float.parseFloat(coords[4]); } catch (Exception ignored) {}
            }
            return new FabricLocation(level, x, y, z, yaw, pitch);
        } catch (final NumberFormatException e) {
            return null;
        }
    }
}
