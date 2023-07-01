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

package me.pikamug.quests.dependencies;

import com.alessiodp.parties.api.interfaces.PartiesAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.mcMMO;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass;
import lol.pyr.znpcsplus.ZNPCsPlus;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.dependencies.reflect.denizen.DenizenAPI;
import me.pikamug.quests.dependencies.reflect.worldguard.WorldGuardAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.pikamug.unite.api.objects.PartyProvider;
import net.citizensnpcs.api.CitizensPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import ro.niconeko.astralbooks.api.AstralBooksAPI;

import java.util.Set;
import java.util.UUID;

public interface Dependencies {
    Economy getVaultEconomy();

    Permission getVaultPermission();

    PartyProvider getPartyProvider();

    Set<PartyProvider> getPartyProviders();

    WorldGuardAPI getWorldGuardApi();

    mcMMO getMcmmoClassic();

    Heroes getHeroes();

    PlaceholderAPIPlugin getPlaceholderApi();

    CitizensPlugin getCitizens();

    void linkCitizens();

    void unlinkCitizens();

    DenizenAPI getDenizenApi();

    AstralBooksAPI getAstralBooksApi();

    ZNPCsPlus getZnpcsPlus();

    PartiesAPI getPartiesApi();

    boolean isPluginAvailable(final String pluginName);

    boolean runDenizenScript(final String scriptName, final Quester quester, final UUID uuid);

    Location getNpcLocation(final UUID uuid);

    String getNpcName(final UUID uuid);

    SkillType getMcMMOSkill(final String s);

    int getMcmmoSkillLevel(final SkillType st, final String player);

    Hero getHero(final UUID uuid);

    HeroClass getHeroClass(final String className);

    boolean testPrimaryHeroesClass(final String primaryClass, final UUID uuid);

    boolean testSecondaryHeroesClass(final String secondaryClass, final UUID uuid);

    void init();
}
