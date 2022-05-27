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

package me.blackvein.quests.reflect.denizen;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import me.blackvein.quests.QuestsAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class DenizenAPI_1_1_0 {
    
    private static final QuestsAPI quests = (QuestsAPI) Bukkit.getPluginManager().getPlugin("Quests");
    private static final DenizenAPI api = quests != null ? quests.getDependencies().getDenizenApi() : null;

    public static boolean containsScript(final String input) {
        if (quests == null || api.scriptRegistry == null || api.containsScriptMethod == null) return false;
        boolean script = false;
        try {
            script = (boolean)api.containsScriptMethod.invoke(api.scriptRegistry, input);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen ScriptRegistry#containsScript", e);
        }
        return script;
    }
    
    @Nullable
    public static String getScriptContainerName(final String input) {
        return ScriptRegistry.getScriptContainer(input).getName();
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public static Set<String> getScriptNames() {
        if (quests == null || api.scriptRegistry == null || api.getScriptNamesMethod == null) return null;
        Set<String> names = null;
        try {
            names = (Set<String>)api.getScriptNamesMethod.invoke(api.scriptRegistry);
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen ScriptRegistry#_getScriptNames", e);
        }
        return names;
    }
    
    @Nullable
    public static Object getScriptContainerAs(final String scriptName) {
        return ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
    }
    
    @Nullable
    public static Object mirrorBukkitPlayer(final Player player) {
        return PlayerTag.mirrorBukkitPlayer(player);
    }
    
    public static @NotNull Object mirrorCitizensNPC(final NPC npc) {
        return NPCTag.fromEntity(npc.getEntity());
    }
    
    public static void runTaskScript(final String scriptName, final Player player, final NPC npc) {
        if (quests == null) {
            return;
        }
        try {
            final Constructor<?> constructor = api.bukkitScriptEntryData.getConstructors()[0];
            final Object tsc = getScriptContainerAs(scriptName);
            if (tsc != null) {
                final Method runTaskScript = tsc.getClass().getMethod("runTaskScript", api.scriptEntryData, Map.class);
                runTaskScript.invoke(tsc, constructor.newInstance(mirrorBukkitPlayer(player), mirrorCitizensNPC(npc)), null);
            }
        } catch (final Exception e) {
            quests.getPluginLogger().log(Level.WARNING, "Error invoking Denizen TaskScriptContainer#runTaskScript", e);
        }
    }
}
