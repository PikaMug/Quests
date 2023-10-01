/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.dependencies;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import io.github.znetworkw.znpcservers.npc.NPC;
import lol.pyr.znpcsplus.ZNPCsPlus;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.listeners.BukkitCitizensListener;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.dependencies.reflect.denizen.DenizenAPI;
import me.pikamug.quests.dependencies.reflect.worldguard.WorldGuardAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.pikamug.unite.api.objects.PartyProvider;
import net.citizensnpcs.api.CitizensPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.niconeko.astralbooks.api.AstralBooks;
import ro.niconeko.astralbooks.api.AstralBooksAPI;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitDependencies implements Dependencies {
    
    private final BukkitQuestsPlugin plugin;
    private static Economy economy = null;
    private static Permission permission = null;
    private static PartyProvider partyProvider = null;
    private static final Set<PartyProvider> partyProviders = new HashSet<>();
    private static WorldGuardAPI worldGuard = null;
    private static mcMMO mcmmo = null;
    private static Heroes heroes = null;
    public static PlaceholderAPIPlugin placeholder = null;
    public static CitizensPlugin citizens = null;
    private static DenizenAPI denizen = null;
    private static AstralBooksAPI astralBooks = null;
    public static ZNPCsPlus znpcsPlus = null;
    private static PartiesAPI parties = null;
    private int npcEffectThread = -1;
    
    public BukkitDependencies(final BukkitQuestsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public Economy getVaultEconomy() {
        if (economy == null && isPluginAvailable("Vault")) {
            if (!setupEconomy()) {
                plugin.getLogger().warning("Economy provider not found.");
            }
        }
        return economy;
    }
    
    public Permission getVaultPermission() {
        if (permission == null && isPluginAvailable("Vault")) {
            if (!setupPermissions()) {
                plugin.getLogger().warning("Permission provider not found.");
            }
        }
        return permission;
    }

    public PartyProvider getPartyProvider() {
        if (partyProvider == null && isPluginAvailable("Unite")) {
            if (!setupParty()) {
                plugin.getLogger().warning("Party provider not found.");
            }
        }
        return partyProvider;
    }

    public Set<PartyProvider> getPartyProviders() {
        if (partyProvider == null && isPluginAvailable("Unite")) {
            if (!setupParty()) {
                plugin.getLogger().warning("Party providers not found.");
            }
        }
        return partyProviders;
    }
    
    public WorldGuardAPI getWorldGuardApi() {
        if (worldGuard == null && isPluginAvailable("WorldGuard")) {
            worldGuard = new WorldGuardAPI(plugin.getServer().getPluginManager().getPlugin("WorldGuard"));
        }
        return worldGuard;
    }
    
    public mcMMO getMcmmoClassic() {
        if (mcmmo == null && isPluginAvailable("mcMMO")) {
            try {
                Class.forName("com.gmail.nossr50.datatypes.skills.SkillType");
                mcmmo = (mcMMO) plugin.getServer().getPluginManager().getPlugin("mcMMO");
            } catch (final Exception e) {
                // Unsupported version
            }
        }
        return mcmmo;
    }
    
    public Heroes getHeroes() {
        if (heroes == null && isPluginAvailable("Heroes")) {
            heroes = (Heroes) plugin.getServer().getPluginManager().getPlugin("Heroes");
        }
        return heroes;
    }
    
    public PlaceholderAPIPlugin getPlaceholderApi() {
        if (placeholder == null && isPluginAvailable("PlaceholderAPI")) {
            placeholder = (PlaceholderAPIPlugin) plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        }
        return placeholder;
    }
    
    public CitizensPlugin getCitizens() {
        if (citizens == null) {
            linkCitizens();
        }
        return citizens;
    }
    
    public void linkCitizens() {
        if (isPluginAvailable("Citizens")) {
            try {
                citizens = (CitizensPlugin) plugin.getServer().getPluginManager().getPlugin("Citizens");
                boolean found = false;
                for (final RegisteredListener listener : HandlerList.getRegisteredListeners(plugin)) {
                    if (listener.getListener() instanceof BukkitCitizensListener) {
                        found = true;
                    }
                }
                if (!found) {
                    plugin.getServer().getPluginManager().registerEvents(plugin.getCitizensListener(), plugin);
                    startNpcEffectThread();
                    plugin.getLogger().info("Successfully linked Quests with Citizens " 
                            + citizens.getDescription().getVersion());
                }
            } catch (final Exception e) {
                plugin.getLogger().warning("Legacy version of Citizens found. Citizens in Quests not enabled.");
            }
        }
    }
    
    public void unlinkCitizens() {
        citizens = null;
    }
    
    public DenizenAPI getDenizenApi() {
        if (denizen == null && isPluginAvailable("Denizen")) {
            denizen = new DenizenAPI();
        }
        return denizen;
    }
    
    public AstralBooksAPI getAstralBooksApi() {
        if (astralBooks == null && isPluginAvailable("AstralBooks")) {
            try {
                astralBooks = ((AstralBooks) Objects.requireNonNull(plugin.getServer().getPluginManager()
                        .getPlugin("AstralBooks"))).getAPI();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return astralBooks;
    }

    public ZNPCsPlus getZnpcsPlus() {
        if (znpcsPlus == null) {
            znpcsPlus = (ZNPCsPlus) plugin.getServer().getPluginManager().getPlugin("ZNPCsPlus");
            startNpcEffectThread();
        }
        return znpcsPlus;
    }

    public Set<UUID> getZnpcsPlusUuids() {
        if (znpcsPlus != null && isPluginAvailable("ZNPCsPlus")) {
            return io.github.znetworkw.znpcservers.npc.NPC.all().stream()
                    .map(io.github.znetworkw.znpcservers.npc.NPC::getUUID).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public PartiesAPI getPartiesApi() {
        if (parties == null && isPluginAvailable("Parties")) {
            try {
                Class.forName("com.alessiodp.parties.api.Parties");
                parties = Parties.getApi();
            } catch (final Exception e) {
                // Unsupported version
            }
        }
        return parties;
    }
    
    public boolean isPluginAvailable(final String pluginName) {
        if (plugin.getServer().getPluginManager().getPlugin(pluginName) != null ) {
            try {
                if (!Objects.requireNonNull(plugin.getServer().getPluginManager().getPlugin(pluginName)).isEnabled()) {
                    plugin.getLogger().warning(pluginName
                            + " was detected, but is not enabled! Fix "+ pluginName + " to allow linkage.");
                } else {
                    return true;
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean setupEconomy() {
        try {
            final RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager()
                    .getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
            return economy != null;
        } catch (final Exception e) {
            return false;
        }
    }

    private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager()
                .getRegistration(Permission.class);
        if (rsp != null) {
            permission = rsp.getProvider();
        }
        return permission != null;
    }

    private boolean setupParty() {
        final RegisteredServiceProvider<PartyProvider> rsp = plugin.getServer().getServicesManager()
                .getRegistration(PartyProvider.class);
        if (rsp != null) {
            partyProvider = rsp.getProvider();
            for (final RegisteredServiceProvider<PartyProvider> rsp2 : plugin.getServer().getServicesManager()
                    .getRegistrations(PartyProvider.class)) {
                if (rsp2 != null) {
                    partyProviders.add(rsp2.getProvider());
                }
            }
        }
        return partyProvider != null;
    }
    
    public boolean runDenizenScript(final String scriptName, final Quester quester, final UUID uuid) {
        return plugin.getDenizenTrigger().runDenizenScript(scriptName, quester, uuid);
    }

    public @Nullable Location getNpcLocation(final UUID uuid) {
        if (citizens != null && citizens.getNPCRegistry().getByUniqueId(uuid) != null) {
            return citizens.getNPCRegistry().getByUniqueId(uuid).getStoredLocation();
        } else if (znpcsPlus != null && getZnpcsPlusUuids().contains(uuid)) {
            final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(uuid)).findAny();
            if (opt.isPresent()) {
                return opt.get().getLocation();
            }
        }
        return null;
    }

    public @Nullable Entity getNpcEntity(final UUID uuid) {
        if (citizens != null && citizens.getNPCRegistry().getByUniqueId(uuid) != null) {
            return citizens.getNPCRegistry().getByUniqueId(uuid).getEntity();
        } else if (znpcsPlus != null && getZnpcsPlusUuids().contains(uuid)) {
            final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(uuid)).findAny();
            if (opt.isPresent()) {
                return (Entity) opt.get().getBukkitEntity();
            }
        }
        return null;
    }

    public @NotNull String getNpcName(final UUID uuid) {
        final Entity npc;
        if (citizens != null && citizens.getNPCRegistry().getByUniqueId(uuid) != null) {
            return citizens.getNPCRegistry().getByUniqueId(uuid).getName();
        } else if (znpcsPlus != null && getZnpcsPlusUuids().contains(uuid)) {
            final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(uuid)).findAny();
            if (opt.isPresent()) {
                npc = (Entity) opt.get().getBukkitEntity();
                if (npc.getCustomName() != null) {
                    return npc.getCustomName();
                } else {
                    return opt.get().getNpcPojo().getHologramLines().get(0);
                }
            }
        }
        return "NPC";
    }

    public @Nullable UUID getUuidFromNpc(final Entity entity) {
        if (citizens != null && citizens.getNPCRegistry().isNPC(entity)) {
            return citizens.getNPCRegistry().getNPC(entity).getUniqueId();
        } else if (znpcsPlus != null && getZnpcsPlusUuids().contains(entity.getUniqueId())) {
            final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(entity.getUniqueId())).findAny();
            if (opt.isPresent()) {
                return opt.get().getUUID();
            }
        }
        return null;
    }

    public void startNpcEffectThread() {
        if (npcEffectThread == -1 && plugin.getConfigSettings().canNpcEffects()) {
            npcEffectThread = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                    plugin.getNpcEffectThread(), 20, 20);
        }
    }

    @SuppressWarnings("unused")
    public void stopNpcEffectThread() {
        plugin.getServer().getScheduler().cancelTask(npcEffectThread);
    }

    public SkillType getMcMMOSkill(final String s) {
        return SkillType.getSkill(s);
    }
    
    public int getMcmmoSkillLevel(final SkillType st, final String player) {
        final McMMOPlayer mPlayer = UserManager.getPlayer(player);
        if (mPlayer == null) {
            return -1;
        }
        return mPlayer.getProfile().getSkillLevel(st);
    }

    public Hero getHero(final UUID uuid) {
        final Player p = plugin.getServer().getPlayer(uuid);
        if (p == null) {
            return null;
        }
        if (heroes != null) {
            return heroes.getCharacterManager().getHero(p);
        }
        return null;
    }

    public HeroClass getHeroClass(final String className) {
        if (heroes != null) {
            return getHeroes().getClassManager().getClass(className);
        }
        return null;
    }

    public boolean testPrimaryHeroesClass(final String primaryClass, final UUID uuid) {
        if (heroes != null) {
            return getHero(uuid).getHeroClass().getName().equalsIgnoreCase(primaryClass);
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean testSecondaryHeroesClass(final String secondaryClass, final UUID uuid) {
        if (heroes != null) {
            return getHero(uuid).getSecondClass().getName().equalsIgnoreCase(secondaryClass);
        }
        return false;
    }

    public void init() {
        getCitizens();
        getWorldGuardApi();
        getDenizenApi();
        getMcmmoClassic();
        getHeroes();
        getPlaceholderApi();
        getAstralBooksApi();
        getPartiesApi();
        getPartyProvider();
        getVaultEconomy();
        getVaultPermission();
        getZnpcsPlus();
    }
}
