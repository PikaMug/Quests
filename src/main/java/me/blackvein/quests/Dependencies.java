package me.blackvein.quests;

import org.bukkit.plugin.RegisteredServiceProvider;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.CitizensPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import ro.nicuch.citizensbooks.CitizensBooksAPI;
import ro.nicuch.citizensbooks.CitizensBooksPlugin;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.gmail.nossr50.mcMMO;
import com.herocraftonline.heroes.Heroes;
import com.live.bemmamin.gps.Vars;
import com.live.bemmamin.gps.api.GPSAPI;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Dependencies {
	
	private Quests plugin;
	private static Economy economy = null;
	private static Permission permission = null;
	private static WorldGuardPlugin worldGuard = null;
	private static mcMMO mcmmo = null;
	private static GPSAPI gpsapi = null;
	private static Heroes heroes = null;
	private static PhatLoots phatLoots = null;
	private static PlaceholderAPIPlugin placeholder = null;
	private static CitizensPlugin citizens;
	private static Denizen denizen = null;
	private static CitizensBooksAPI citizensBooks = null;
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
	
	public WorldGuardPlugin getWorldGuard() {
		return worldGuard;
	}
	
	public mcMMO getMcmmo() {
		return mcmmo;
	}
	
	public GPSAPI getGpsApi() {
		return gpsapi;
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
		return citizens;
	}
	
	public Denizen getDenizen() {
		return denizen;
	}
	
	public CitizensBooksAPI getCitizensBooksApi() {
		return citizensBooks;
	}
	
	public PartiesAPI getPartiesApi() {
		return parties;
	}
	
	public boolean isPluginAvailable(String pluginName) {
		if (plugin.getServer().getPluginManager().getPlugin(pluginName) != null ) {
			if (!plugin.getServer().getPluginManager().getPlugin(pluginName).isEnabled()) {
				plugin.getLogger().warning(pluginName + " was detected, but is not enabled! Fix "+ pluginName + " to allow linkage.");
			} else {
				return true;
			}
		}
		return false;
	}
	
	void init() {
		try {
			if (isPluginAvailable("Citizens")) {
				citizens = (CitizensPlugin) plugin.getServer().getPluginManager().getPlugin("Citizens");
			}
		} catch (Exception e) {
			plugin.getLogger().warning("Legacy version of Citizens found. Citizens in Quests not enabled.");
		}
		if (isPluginAvailable("WorldGuard")) {
			worldGuard = (WorldGuardPlugin) plugin.getServer().getPluginManager().getPlugin("WorldGuard");
		}
		if (isPluginAvailable("Denizen")) {
			denizen = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");
		}
		if (isPluginAvailable("mcMMO")) {
			mcmmo = (mcMMO) plugin.getServer().getPluginManager().getPlugin("mcMMO");
		}
		if (isPluginAvailable("GPS") && plugin.getSettings().canUseGPS()) {
			gpsapi = new GPSAPI(plugin);
			Vars.getInstance().setMaxDistanceToEntry(9999.0);
		}
		if (isPluginAvailable("Heroes")) {
			heroes = (Heroes) plugin.getServer().getPluginManager().getPlugin("Heroes");
		}
		if (isPluginAvailable("PhatLoots")) {
			phatLoots = (PhatLoots) plugin.getServer().getPluginManager().getPlugin("PhatLoots");
		}
		if (isPluginAvailable("PlaceholderAPI")) {
			placeholder = (PlaceholderAPIPlugin) plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
		}
		if (isPluginAvailable("CitizensBooks")) {
		    citizensBooks = ((CitizensBooksPlugin) plugin.getServer().getPluginManager().getPlugin("CitizensBooks")).getAPI();
        }
		if (isPluginAvailable("Parties")) {
		    parties = Parties.getApi();
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
			RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
			return (economy != null);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
		return (permission != null);
	}
	
	public boolean runDenizenScript(String scriptName, Quester quester) {
		return plugin.getDenizenTrigger().runDenizenScript(scriptName, quester);
	}
}
