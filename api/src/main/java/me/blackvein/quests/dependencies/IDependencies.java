package me.blackvein.quests.dependencies;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import me.blackvein.quests.player.IQuester;
import me.blackvein.quests.reflect.denizen.DenizenAPI;
import me.blackvein.quests.reflect.worldguard.WorldGuardAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.pikamug.unite.api.objects.PartyProvider;
import net.citizensnpcs.api.CitizensPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import ro.nicuch.citizensbooks.CitizensBooksAPI;

import java.util.Set;
import java.util.UUID;

public interface IDependencies {
    Economy getVaultEconomy();

    Permission getVaultPermission();

    PartyProvider getPartyProvider();

    Set<PartyProvider> getPartyProviders();

    WorldGuardAPI getWorldGuardApi();

    mcMMO getMcmmoClassic();

    Heroes getHeroes();

    PhatLoots getPhatLoots();

    PlaceholderAPIPlugin getPlaceholderApi();

    CitizensPlugin getCitizens();

    void linkCitizens();

    void unlinkCitizens();

    DenizenAPI getDenizenApi();

    CitizensBooksAPI getCitizensBooksApi();

    PartiesAPI getPartiesApi();

    boolean isPluginAvailable(final String pluginName);

    boolean runDenizenScript(final String scriptName, final IQuester quester);

    Location getNPCLocation(final int id);

    String getNPCName(final int id);

    int getMcmmoSkillLevel(final SkillType st, final String player);

    Hero getHero(final UUID uuid);

    boolean testPrimaryHeroesClass(final String primaryClass, final UUID uuid);

    boolean testSecondaryHeroesClass(final String secondaryClass, final UUID uuid);

    void init();
}
