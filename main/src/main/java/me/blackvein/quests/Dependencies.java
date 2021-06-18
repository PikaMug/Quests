/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;

import de.erethon.dungeonsxl.DungeonsXL;
import me.blackvein.quests.listeners.NpcListener;
import me.blackvein.quests.reflect.denizen.DenizenAPI;
import me.blackvein.quests.reflect.worldguard.WorldGuardAPI;
import me.blackvein.quests.util.Lang;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.citizensnpcs.api.CitizensPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

public class Dependencies {
    
    private final Quests plugin;
    private static Economy economy = null;
    private static Permission permission = null;
    private static WorldGuardAPI worldGuardApi = null;
    private static mcMMO mcmmo = null;
    private static Heroes heroes = null;
    private static PhatLoots phatLoots = null;
    public static PlaceholderAPIPlugin placeholder = null;
    private static CitizensPlugin citizens = null;
    private static DenizenAPI denizenApi = null;
    private static CitizensBooksAPI citizensBooks = null;
    private static DungeonsXL dungeons = null;
    private static PartiesAPI parties = null;
    
    public Dependencies(final Quests plugin) {
        this.plugin = plugin;
    }
    
    public Economy getVaultEconomy() {
        if (economy == null && isPluginAvailable("Vault")) {
            if (!setupEconomy()) {
                plugin.getLogger().warning("Economy not found.");
            }
        }
        return economy;
    }
    
    public Permission getVaultPermission() {
        if (permission == null && isPluginAvailable("Vault")) {
            if (!setupPermissions()) {
                plugin.getLogger().warning("Permissions not found.");
            }
        }
        return permission;
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
    
    public PhatLoots getPhatLoots() {
        if (phatLoots == null && isPluginAvailable("PhatLoots")) {
            try {
                phatLoots = (PhatLoots) plugin.getServer().getPluginManager().getPlugin("PhatLoots");
                plugin.getLogger().info("Sucessfully linked Quests with PhatLoots " 
                        + phatLoots.getDescription().getVersion());
            } catch (final NoClassDefFoundError e) {
                plugin.getLogger().warning("Unofficial version of PhatLoots found. PhatLoots in Quests not enabled.");
            }
        }
        return phatLoots;
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
                    if (listener.getListener() instanceof NpcListener) {
                        found = true;
                    }
                }
                if (!found) {
                    plugin.getServer().getPluginManager().registerEvents(plugin.getNpcListener(), plugin);
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
                citizensBooks = ((CitizensBooksPlugin) plugin.getServer().getPluginManager().getPlugin("CitizensBooks"))
                        .getAPI();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return citizensBooks;
    }
    
    public DungeonsXL getDungeonsApi() {
        if (dungeons == null && isPluginAvailable("DungeonsXL")) {
            dungeons = DungeonsXL.getInstance();
        }
        return dungeons;
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
                if (!plugin.getServer().getPluginManager().getPlugin(pluginName).isEnabled()) {
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
    
    void init() {
        getCitizens();
        getWorldGuardApi();
        getDenizenApi();
        getMcmmoClassic();
        getHeroes();
        getPhatLoots();
        getPlaceholderApi();
        getCitizensBooksApi();
        getDungeonsApi();
        getPartiesApi();
        getVaultEconomy();
        getVaultPermission();
    }

    private boolean setupEconomy() {
        try {
            final RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
                    .getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
            return (economy != null);
        } catch (final Exception e) {
            return false;
        }
    }

    private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    /**
     * @deprecated Do not use. Will be removed in a future version.
     **/
    public String getCurrency(final boolean plural) {
        // TODO remove "money" from strings.yml
        if (getVaultEconomy() == null) {
            return Lang.get("money");
        }
        if (plural) {
            if (getVaultEconomy().currencyNamePlural().trim().isEmpty()) {
                return Lang.get("money");
            } else {
                return getVaultEconomy().currencyNamePlural();
            }
        } else {
            if (getVaultEconomy().currencyNameSingular().trim().isEmpty()) {
                return Lang.get("money");
            } else {
                return getVaultEconomy().currencyNameSingular();
            }
        }
    }
    
    public boolean runDenizenScript(final String scriptName, final Quester quester) {
        return plugin.getDenizenTrigger().runDenizenScript(scriptName, quester);
    }
    
    public Location getNPCLocation(final int id) {
        return citizens.getNPCRegistry().getById(id).getStoredLocation();
    }

    public String getNPCName(final int id) {
        return citizens.getNPCRegistry().getById(id).getName();
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
}
