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
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.dependencies.npc.EntityNpcDependency;
import me.pikamug.quests.dependencies.npc.NpcDependency;
import me.pikamug.quests.dependencies.npc.citizens.CitizensDependency;
import me.pikamug.quests.dependencies.npc.znpcsplus.ZnpcsPlusDependency;
import me.pikamug.quests.dependencies.npc.znpcsplus.legacy.LegacyZnpcsPlusDependency;
import me.pikamug.quests.dependencies.reflect.denizen.DenizenAPI;
import me.pikamug.quests.dependencies.reflect.worldguard.WorldGuardAPI;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.unite.api.objects.PartyProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.niconeko.astralbooks.api.AstralBooks;
import ro.niconeko.astralbooks.api.AstralBooksAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

public class BukkitDependencies implements Dependencies {

    private static final Set<PartyProvider> partyProviders = new HashSet<>();
    private static final List<NpcDependency> npcDependencies = new ArrayList<>();
    public static PlaceholderAPIPlugin placeholder = null;
    private static Economy economy = null;
    private static Permission permission = null;
    private static PartyProvider partyProvider = null;
    private static WorldGuardAPI worldGuard = null;
    private static mcMMO mcmmo = null;
    private static Heroes heroes = null;
    private static DenizenAPI denizen = null;
    private static AstralBooksAPI astralBooks = null;
    private static PartiesAPI parties = null;
    private final BukkitQuestsPlugin plugin;
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

    public List<NpcDependency> getNpcDependencies() {
        return npcDependencies;
    }

    public boolean hasAnyNpcDependencies() {
        return !npcDependencies.isEmpty();
    }

    public boolean hasAnyEntityNpcDependencies() {
        for (final NpcDependency npcDependency : npcDependencies) {
            if (npcDependency instanceof EntityNpcDependency) {
                return true;
            }
        }
        return false;
    }

    public NpcDependency getNpcDependency(final String dependencyName) {
        for (final NpcDependency npcDependency : npcDependencies) {
            if (npcDependency.getDependencyName().equalsIgnoreCase(dependencyName)) {
                return npcDependency;
            }
        }
        return null;
    }

    public void addNpcDependency(final NpcDependency npcDependency) {
        npcDependencies.add(npcDependency);
        plugin.getLogger().info("Successfully linked Quests with " + npcDependency.getDependencyName());
        startNpcEffectThread();
    }

    private void initNpcDependency() {
        if (isPluginAvailable("Citizens")) {
            try {
                addNpcDependency(new CitizensDependency(plugin));
            } catch (final Exception e) {
                plugin.getLogger().warning("Legacy version of Citizens found. Citizens in Quests not enabled.");
            }
        }
        if (isPluginAvailable("ZNPCsPlus")) {
            try {
                Class.forName("lol.pyr.znpcsplus.ZNPCsPlus"); // Check for 1.x classes
                addNpcDependency(new LegacyZnpcsPlusDependency(plugin));
            } catch (final Exception ignored) {
            }
            try {
                Class.forName("lol.pyr.znpcsplus.ZNpcsPlus"); // Check for 2.x classes
                addNpcDependency(new ZnpcsPlusDependency(plugin));
            } catch (final Exception ignored) {
            }
        }
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
        if (plugin.getServer().getPluginManager().getPlugin(pluginName) != null) {
            try {
                if (!Objects.requireNonNull(plugin.getServer().getPluginManager().getPlugin(pluginName)).isEnabled()) {
                    plugin.getLogger().warning(pluginName
                            + " was detected, but is not enabled! Fix " + pluginName + " to allow linkage.");
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
        Location location = null;
        for (final NpcDependency npcDependency : npcDependencies) {
            location = npcDependency.getLocation(uuid);
            if (location != null) {
                break;
            }
        }
        return location;
    }

    public @Nullable Entity getNpcEntity(final UUID uuid) {
        for (final NpcDependency npcDependency : npcDependencies) {
            if (!(npcDependency instanceof EntityNpcDependency)) {
                continue;
            }
            final Entity entity = ((EntityNpcDependency) npcDependency).getEntity(uuid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    public @NotNull String getNpcName(final UUID uuid) {
        String name = "NPC";
        for (final NpcDependency npcDependency : npcDependencies) {
            final String npcName = npcDependency.getName(uuid);
            if (npcName != null) {
                name = npcName;
                break;
            }
        }
        return BukkitConfigUtil.parseString(name);
    }

    public @Nullable UUID getUuidFromNpc(final Entity entity) {
        for (final NpcDependency npcDependency : npcDependencies) {
            if (!(npcDependency instanceof EntityNpcDependency)) {
                continue;
            }
            final UUID uuid = ((EntityNpcDependency) npcDependency).getId(entity);
            if (uuid != null) {
                return uuid;
            }
        }
        return null;
    }

    public boolean isNpc(final UUID uuid) {
        for (final NpcDependency npcDependency : npcDependencies) {
            if (npcDependency.isNpc(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNpc(final Entity entity) {
        for (final NpcDependency npcDependency : npcDependencies) {
            if (!(npcDependency instanceof EntityNpcDependency)) {
                continue;
            }
            if (((EntityNpcDependency) npcDependency).isNpc(entity)) {
                return true;
            }
        }
        return false;
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
        initNpcDependency();
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
    }
}
