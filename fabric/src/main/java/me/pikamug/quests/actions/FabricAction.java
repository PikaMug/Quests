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
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.tasks.FabricActionTimer;
import me.pikamug.quests.tasks.FabricScheduler;
import me.pikamug.quests.util.FabricConfigUtil;
import me.pikamug.quests.util.FabricInventoryUtil;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricLocation;
import me.pikamug.quests.util.FabricMiscUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FabricAction implements Action {

    private final FabricQuestsPlugin plugin;
    private String name = "";
    protected String message = null;
    protected boolean clearInv = false;
    protected boolean failQuest = false;
    protected LinkedList<FabricLocation> explosions = new LinkedList<>();
    protected Map<FabricLocation, SoundEvent> effects = new HashMap<>();
    protected LinkedList<ItemStack> items = new LinkedList<>();
    protected ServerLevel stormWorld = null;
    protected int stormDuration = 0;
    protected ServerLevel thunderWorld = null;
    protected int thunderDuration = 0;
    protected int timer = 0;
    protected boolean cancelTimer = false;
    protected LinkedList<QuestMob> mobSpawns = new LinkedList<QuestMob>() {

        private static final long serialVersionUID = -761974607799449780L;

        @Override
        public boolean equals(final Object o) {
            if (o instanceof LinkedList) {
                @SuppressWarnings("unchecked")
                final
                LinkedList<QuestMob> other = (LinkedList<QuestMob>) o;
                if (size() != other.size()) {
                    return false;
                }
                for (int i = 0; i < size(); i++) {
                    if (!get(i).equals(other.get(i))) {
                        return false;
                    }
                }
            }
            return false;
        }
    };
    protected LinkedList<FabricLocation> lightningStrikes = new LinkedList<>();
    protected LinkedList<String> commands = new LinkedList<>();
    protected LinkedList<MobEffectInstance> potionEffects = new LinkedList<>();
    protected int hunger = -1;
    protected int saturation = -1;
    protected float health = -1;
    protected FabricLocation teleport;
    protected String book = "";

    public FabricAction(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

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

    public LinkedList<MobEffectInstance> getPotionEffects() {
        return potionEffects;
    }

    public void setPotionEffects(final LinkedList<MobEffectInstance> potionEffects) {
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
        final MinecraftServer server = FabricQuestsPlugin.getInstance().getServer();
        if (message != null) {
            for (final String s : FabricConfigUtil.parseStringWithPossibleLineBreaks(message, quest, player)) {
                quester.sendMessage(s);
            }
        }
        if (clearInv) {
            player.getInventory().clearContent();
        }
        if (explosions != null && !explosions.isEmpty()) {
            for (final FabricLocation loc : explosions) {
                if (loc != null && loc.getLevel() != null) {
                    loc.getLevel().explode(null, loc.getX(), loc.getY(), loc.getZ(), 4.0F, Level.ExplosionInteraction.BLOCK);
                }
            }
        }
        if (effects != null && !effects.isEmpty()) {
            for (final Map.Entry<FabricLocation, SoundEvent> entry : effects.entrySet()) {
                final FabricLocation loc = entry.getKey();
                final SoundEvent sound = entry.getValue();
                if (loc == null || loc.getLevel() == null || sound == null) continue;
                loc.getLevel().playSound(null, loc.getX(), loc.getY(), loc.getZ(), sound, SoundSource.MASTER);
            }
        }
        if (items != null && !items.isEmpty()) {
            for (final ItemStack stack : items) {
                try {
                    FabricInventoryUtil.addItem(player, stack);
                } catch (final Exception e) {
                    FabricQuestsPlugin.LOGGER.warn("Unable to add null item to inventory of {} during quest {} event {}",
                            player.getName().getString(),
                            quest != null ? quest.getName() : "?", name, e);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "<red>Quests encountered a problem with an item. Please contact an administrator."));
                }
            }
        }
        if (stormWorld != null) {
            final WeatherData weatherData = stormWorld.getWeatherData();
            weatherData.setClearWeatherTime(0);
            weatherData.setRaining(true);
            weatherData.setRainTime(stormDuration);
        }
        if (thunderWorld != null) {
            final WeatherData weatherData = thunderWorld.getWeatherData();
            weatherData.setClearWeatherTime(0);
            weatherData.setThundering(true);
            weatherData.setThunderTime(thunderDuration);
        }
        if (mobSpawns != null && !mobSpawns.isEmpty()) {
            for (final QuestMob questMob : mobSpawns) {
                questMob.spawn();
            }
        }
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
        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                if (server != null) {
                    command = command.replace("<player>", player.getName().getString());
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
                }
            }
        }
        if (potionEffects != null && !potionEffects.isEmpty()) {
            for (final MobEffectInstance effect : potionEffects) {
                player.addEffect(effect);
            }
        }
        if (hunger != -1) {
            player.getFoodData().setFoodLevel(hunger);
        }
        if (saturation != -1) {
            player.getFoodData().setSaturation(saturation);
        }
        if (health != -1) {
            player.setHealth(health);
        }
        if (teleport != null) {
            if (teleport.getLevel() != null /*&& player.isAlive()*/) {
                player.teleportTo(teleport.getLevel(), teleport.getX(), teleport.getY(), teleport.getZ(),
                        java.util.Collections.emptySet(), teleport.getYaw(), teleport.getPitch(), false);
            }
        }
        if (failQuest && quest != null) {
            quest.failQuest(quester, true);
        }
        if (timer > 0) {
            quester.sendMessage(FabricLang.get(player, "timerStart")
                    .replace("<time>", FabricMiscUtil.formatTime(timer * 1000L))
                    .replace("<quest>", quest != null ? quest.getName() : ""));
            final List<Integer> toNotify = Arrays.asList(60, 30, 10, 5, 4, 3, 2, 1);
            for (final int seconds : toNotify) {
                if (timer > seconds) {
                    final FabricActionTimer notifyTimer = new FabricActionTimer(fabricQuester, quest, seconds);
                    FabricScheduler.runLater(notifyTimer, (timer - seconds) * 20L);
                    fabricQuester.getTimers().put(notifyTimer, quest);
                }
            }
            final FabricActionTimer failTimer = new FabricActionTimer(fabricQuester, quest, 0);
            FabricScheduler.runLater(failTimer, timer * 20L);
            fabricQuester.getTimers().put(failTimer, quest);
        }
        if (cancelTimer) {
            for (final Map.Entry<Runnable, Quest> entry : fabricQuester.getTimers().entrySet()) {
                if (entry.getValue().getId().equals(quest.getId())) {
                    if (entry.getKey() instanceof FabricActionTimer) {
                        ((FabricActionTimer) entry.getKey()).cancel();
                    }
                    fabricQuester.getTimers().remove(entry.getKey());
                }
            }
        }
    }
}
