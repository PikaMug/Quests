/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import io.github.znetworkw.znpcservers.ServersNPC;
import io.github.znetworkw.znpcservers.npc.NPC;
import me.blackvein.quests.dependencies.IDependencies;
import me.blackvein.quests.listeners.CitizensListener;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.reflect.denizen.DenizenAPI;
import me.blackvein.quests.reflect.worldguard.WorldGuardAPI;
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
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Dependencies implements IDependencies {
    
    private final Quests plugin;
    private static Economy economy = null;
    private static Permission permission = null;
    private static PartyProvider partyProvider = null;
    private static final Set<PartyProvider> partyProviders = new HashSet<>();
    private static WorldGuardAPI worldGuardApi = null;
    private static mcMMO mcmmo = null;
    private static Heroes heroes = null;
    public static PlaceholderAPIPlugin placeholder = null;
    public static CitizensPlugin citizens = null;
    private static DenizenAPI denizenApi = null;
    private static CitizensBooksAPI citizensBooks = null;
    private static ServersNPC znpcs = null;
    private static PartiesAPI parties = null;
    
    public Dependencies(final Quests plugin) {
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
        if (worldGuardApi == null && isPluginAvailable("WorldGuard")) {
            worldGuardApi = new WorldGuardAPI(plugin.getServer().getPluginManager().getPlugin("WorldGuard"));
        }
        return worldGuardApi;
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
                    if (listener.getListener() instanceof CitizensListener) {
                        found = true;
                    }
                }
                if (!found) {
                    plugin.getServer().getPluginManager().registerEvents(plugin.getCitizensListener(), plugin);
                    if (plugin.getSettings().canNpcEffects()) {
                        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, plugin.getNpcEffectThread(),
                                20, 20);
                    }
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
        if (denizenApi == null && isPluginAvailable("Denizen")) {
            denizenApi = new DenizenAPI();
        }
        return denizenApi;
    }
    
    public CitizensBooksAPI getCitizensBooksApi() {
        if (citizensBooks == null && isPluginAvailable("CitizensBooks")) {
            try {
                citizensBooks = ((CitizensBooksPlugin) Objects.requireNonNull(plugin.getServer().getPluginManager()
                        .getPlugin("CitizensBooks"))).getAPI();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return citizensBooks;
    }

    public ServersNPC getZnpcs() {
        if (znpcs == null) {
            znpcs = (ServersNPC) plugin.getServer().getPluginManager().getPlugin("ServersNPC");
        }
        return znpcs;
    }

    public Set<UUID> getZnpcsUuids() {
        if (znpcs != null && isPluginAvailable("ServersNPC")) {
            // TODO - it seems ZNPCs UUIDs do not persist restart
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
    
    public boolean runDenizenScript(final String scriptName, final IQuester quester, final UUID uuid) {
        return plugin.getDenizenTrigger().runDenizenScript(scriptName, quester, uuid);
    }

    public @Nullable Location getNPCLocation(final UUID uuid) {
        if (citizens != null && citizens.getNPCRegistry().getByUniqueId(uuid) != null) {
            return citizens.getNPCRegistry().getByUniqueId(uuid).getStoredLocation();
        } else if (getZnpcsUuids().contains(uuid)) {
            final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(uuid)).findAny();
            if (opt.isPresent()) {
                return opt.get().getLocation();
            }
        }
        return null;
    }

    public @Nullable Entity getNPCEntity(final UUID uuid) {
        if (citizens != null && citizens.getNPCRegistry().getByUniqueId(uuid) != null) {
            return citizens.getNPCRegistry().getByUniqueId(uuid).getEntity();
        } else if (getZnpcsUuids().contains(uuid)) {
            final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(uuid)).findAny();
            if (opt.isPresent()) {
                return (Entity) opt.get().getBukkitEntity();
            }
        }
        return null;
    }

    public @NotNull String getNPCName(final UUID uuid) {
        Entity npc = null;
        if (citizens != null && citizens.getNPCRegistry().getByUniqueId(uuid) != null) {
            return citizens.getNPCRegistry().getByUniqueId(uuid).getName();
        } else if (getZnpcsUuids().contains(uuid)) {
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

    public @Nullable UUID getUUIDFromNPC(final Entity entity) {
        if (citizens != null && citizens.getNPCRegistry().isNPC(entity)) {
            return citizens.getNPCRegistry().getNPC(entity).getUniqueId();
        } else if (getZnpcsUuids().contains(entity.getUniqueId())) {
            final Optional<NPC> opt = NPC.all().stream().filter(npc1 -> npc1.getUUID().equals(entity.getUniqueId())).findAny();
            if (opt.isPresent()) {
                return opt.get().getUUID();
            }
        }
        return null;
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
        return heroes.getCharacterManager().getHero(p);
    }

    public boolean testPrimaryHeroesClass(final String primaryClass, final UUID uuid) {
        final Hero hero = getHero(uuid);
        return hero.getHeroClass().getName().equalsIgnoreCase(primaryClass);
    }

    @SuppressWarnings("deprecation")
    public boolean testSecondaryHeroesClass(final String secondaryClass, final UUID uuid) {
        final Hero hero = getHero(uuid);
        return hero.getSecondClass().getName().equalsIgnoreCase(secondaryClass);
    }

    public void init() {
        getCitizens();
        getWorldGuardApi();
        getDenizenApi();
        getMcmmoClassic();
        getHeroes();
        getPlaceholderApi();
        getCitizensBooksApi();
        getPartiesApi();
        getPartyProvider();
        getVaultEconomy();
        getVaultPermission();
    }
}
