/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
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
import org.bukkit.plugin.RegisteredServiceProvider;

import me.blackvein.quests.reflect.denizen.DenizenAPI;
import me.blackvein.quests.reflect.worldguard.WorldGuardAPI;
import me.blackvein.quests.util.Lang;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.citizensnpcs.api.CitizensPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

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

public class Dependencies {
    
    private Quests plugin;
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
    
    public Dependencies(Quests plugin) {
        this.plugin = plugin;
    }
    
    public Economy getVaultEconomy() {
        return economy;
    }
    
    public Permission getVaultPermission() {
        return permission;
    }
    
    public WorldGuardAPI getWorldGuardApi() {
        return worldGuardApi;
    }
    
    public mcMMO getMcmmoClassic() {
        return mcmmo;
    }
    
    public Heroes getHeroes() {
        return heroes;
    }
    
    public PhatLoots getPhatLoots() {
        return phatLoots;
    }
    
    public PlaceholderAPIPlugin getPlaceholderApi() {
        return placeholder;
    }
    
    public CitizensPlugin getCitizens() {
        if (citizens == null) {
            if (isPluginAvailable("Citizens")) {
                try {
                    citizens = (CitizensPlugin) plugin.getServer().getPluginManager().getPlugin("Citizens");
                } catch (Exception e) {
                    plugin.getLogger().warning("Legacy version of Citizens found. Citizens in Quests not enabled.");
                }
            }
        }
        return citizens;
    }
    
    public void disableCitizens() {
        citizens = null;
    }
    
    public DenizenAPI getDenizenAPI() {
        return denizenApi;
    }
    
    public CitizensBooksAPI getCitizensBooksApi() {
        return citizensBooks;
    }
    
    public DungeonsXL getDungeonsApi() {
        return dungeons;
    }
    
    public PartiesAPI getPartiesApi() {
        return parties;
    }
    
    public boolean isPluginAvailable(String pluginName) {
        if (plugin.getServer().getPluginManager().getPlugin(pluginName) != null ) {
            if (!plugin.getServer().getPluginManager().getPlugin(pluginName).isEnabled()) {
                plugin.getLogger().warning(pluginName 
                        + " was detected, but is not enabled! Fix "+ pluginName + " to allow linkage.");
            } else {
                return true;
            }
        }
        return false;
    }
    
    void init() {
        if (isPluginAvailable("Citizens")) {
            try {
                citizens = (CitizensPlugin) plugin.getServer().getPluginManager().getPlugin("Citizens");
            } catch (Exception e) {
                plugin.getLogger().warning("Legacy version of Citizens found. Citizens in Quests not enabled.");
            }
        }
        if (isPluginAvailable("WorldGuard")) {
            worldGuardApi = new WorldGuardAPI(plugin.getServer().getPluginManager().getPlugin("WorldGuard"));
        }
        if (isPluginAvailable("Denizen")) {
            denizenApi = new DenizenAPI();
        }
        if (isPluginAvailable("mcMMO")) {
            try {
                Class.forName("com.gmail.nossr50.datatypes.skills.SkillType");
                mcmmo = (mcMMO) plugin.getServer().getPluginManager().getPlugin("mcMMO");
            } catch (Exception e) {
                // Unsupported version
            }
        }
        if (isPluginAvailable("Heroes")) {
            heroes = (Heroes) plugin.getServer().getPluginManager().getPlugin("Heroes");
        }
        if (isPluginAvailable("PhatLoots")) {
            try {
                phatLoots = (PhatLoots) plugin.getServer().getPluginManager().getPlugin("PhatLoots");
            } catch (NoClassDefFoundError e) {
                plugin.getLogger().warning("Unofficial version of PhatLoots found. PhatLoots in Quests not enabled.");
            }
        }
        if (isPluginAvailable("PlaceholderAPI")) {
            placeholder = (PlaceholderAPIPlugin) plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        }
        if (isPluginAvailable("CitizensBooks")) {
            citizensBooks = ((CitizensBooksPlugin) plugin.getServer().getPluginManager().getPlugin("CitizensBooks"))
                    .getAPI();
        }
        if (isPluginAvailable("DungeonsXL")) {
            dungeons = DungeonsXL.getInstance();
        }
        if (isPluginAvailable("Parties")) {
            try {
                Class.forName("com.alessiodp.parties.api.Parties");
                parties = Parties.getApi();
            } catch (Exception e) {
                // Unsupported version
            }
        }
        if (isPluginAvailable("Vault")) {
            if (!setupEconomy()) {
                plugin.getLogger().warning("Economy not found.");
            }
            if (!setupPermissions()) {
                plugin.getLogger().warning("Permissions not found.");
            }
        }
    }

    private boolean setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager()
                    .getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
            return (economy != null);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager()
                .getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
    
    public String getCurrency(boolean plural) {
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
    
    public boolean runDenizenScript(String scriptName, Quester quester) {
        return plugin.getDenizenTrigger().runDenizenScript(scriptName, quester);
    }
    
    public Location getNPCLocation(int id) {
        return citizens.getNPCRegistry().getById(id).getStoredLocation();
    }

    public String getNPCName(int id) {
        return citizens.getNPCRegistry().getById(id).getName();
    }
    
    public int getMcmmoSkillLevel(SkillType st, String player) {
        McMMOPlayer mPlayer = UserManager.getPlayer(player);
        if (mPlayer == null) {
            return -1;
        }
        return mPlayer.getProfile().getSkillLevel(st);
    }

    public Hero getHero(UUID uuid) {
        Player p = plugin.getServer().getPlayer(uuid);
        if (p == null) {
            return null;
        }
        return heroes.getCharacterManager().getHero(p);
    }

    public boolean testPrimaryHeroesClass(String primaryClass, UUID uuid) {
        Hero hero = getHero(uuid);
        return hero.getHeroClass().getName().equalsIgnoreCase(primaryClass);
    }

    @SuppressWarnings("deprecation")
    public boolean testSecondaryHeroesClass(String secondaryClass, UUID uuid) {
        Hero hero = getHero(uuid);
        return hero.getSecondClass().getName().equalsIgnoreCase(secondaryClass);
    }
}
