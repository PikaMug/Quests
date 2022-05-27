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

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Set;

public class DenizenAPI {
    private Class<?> denizen_1_0_9 = null;
    private Class<?> denizen_1_1_0 = null;
    private Class<?> denizen_1_1_1 = null;
    private Class<?> denizen_1_2_2 = null;
    protected Class<?> scriptRegistry = null;
    protected Method containsScriptMethod = null;
    protected Method getScriptNamesMethod = null;
    protected Class<?> scriptContainer = null;
    protected Class<?> taskScriptContainer = null;
    protected Method getScriptContainerAsMethod = null;
    protected Class<?> dPlayer = null;
    protected Class<?> dNPC = null;
    protected Method mirrorBukkitPlayerMethod = null;
    protected Method mirrorCitizensNPCMethod = null;
    protected Class<?> scriptEntryData = null;
    protected Class<?> bukkitScriptEntryData = null;

    public DenizenAPI() {
        try {
            Class.forName("com.denizenscript.denizencore.utilities.ScriptUtilities");
            denizen_1_2_2 = Class.forName("com.denizenscript.denizen.Denizen");
            return;
        } catch (final Exception e) {
            // Fail silently
        }
        try {
            bukkitScriptEntryData 
                    = Class.forName("com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData");
            denizen_1_1_1 = Class.forName("com.denizenscript.denizen.Denizen");

            containsScriptMethod = scriptRegistry.getMethod("containsScript", String.class);
            return;
        } catch (final Exception e) {
            // Fail silently
        }
        try {
            bukkitScriptEntryData = Class.forName("com.denizenscript.denizen.BukkitScriptEntryData");
            scriptRegistry = Class.forName("com.denizenscript.denizencore.scripts.ScriptRegistry");
            scriptContainer = Class.forName("com.denizenscript.denizencore.scripts.containers.ScriptContainer");
            taskScriptContainer 
                    = Class.forName("com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer");
            dPlayer = Class.forName("com.denizenscript.denizen.objects.PlayerTag");
            dNPC = Class.forName("com.denizenscript.denizen.objects.NPCTag");
            scriptEntryData = Class.forName("com.denizenscript.denizencore.scripts.ScriptEntryData");
            bukkitScriptEntryData = Class.forName("com.denizenscript.denizen.BukkitScriptEntryData");
            denizen_1_1_0 = Class.forName("com.denizenscript.denizen.Denizen");

            containsScriptMethod = scriptRegistry.getMethod("containsScript", String.class);
            getScriptNamesMethod = scriptRegistry.getMethod("_getScriptNames");
            return;
        } catch (final Exception e) {
            // Fail silently
        }
        try {
            scriptRegistry = Class.forName("net.aufdemrand.denizencore.scripts.ScriptRegistry");
            scriptContainer = Class.forName("net.aufdemrand.denizencore.scripts.containers.ScriptContainer");
            taskScriptContainer 
                    = Class.forName("net.aufdemrand.denizencore.scripts.containers.core.TaskScriptContainer");
            dPlayer = Class.forName("net.aufdemrand.denizen.objects.dPlayer");
            dNPC = Class.forName("net.aufdemrand.denizen.objects.dNPC");
            scriptEntryData = Class.forName("net.aufdemrand.denizencore.scripts.ScriptEntryData");
            bukkitScriptEntryData = Class.forName("net.aufdemrand.denizen.BukkitScriptEntryData");
            denizen_1_0_9 = Class.forName("net.aufdemrand.denizen.Denizen");
            
            containsScriptMethod = scriptRegistry.getMethod("containsScript", String.class);
            getScriptNamesMethod = scriptRegistry.getMethod("_getScriptNames");
            getScriptContainerAsMethod = scriptRegistry
                    .getMethod("getScriptContainerAs", String.class, taskScriptContainer);
            mirrorBukkitPlayerMethod = dPlayer.getMethod("mirrorBukkitPlayer", OfflinePlayer.class);
            mirrorCitizensNPCMethod = dNPC.getMethod("mirrorCitizensNPC", NPC.class);
        } catch (final Exception e) {
            // Fail silently
        }
    }
    
    public boolean isEnabled() {
        return denizen_1_2_2 != null || denizen_1_1_1 != null || denizen_1_1_0 != null || denizen_1_0_9 != null;
    }

    @Nullable
    public Class<?> getDenizenClass() {
        if (denizen_1_2_2 != null) {
            return denizen_1_2_2;
        } else if (denizen_1_1_1 != null) {
            return denizen_1_1_1;
        } else if (denizen_1_1_0 != null) {
            return denizen_1_1_0;
        } else if (denizen_1_0_9 != null) {
            return denizen_1_0_9;
        }
        return null;
    }
    
    public boolean containsScript(final String input) {
        if (denizen_1_2_2 != null) {
            return DenizenAPI_1_2_2.containsScript(input);
        } else if (denizen_1_1_1 != null) {
            return DenizenAPI_1_1_1.containsScript(input);
        } else if (denizen_1_1_0 != null) {
            return DenizenAPI_1_1_0.containsScript(input);
        } else if (denizen_1_0_9 != null) {
            return DenizenAPI_1_0_9.containsScript(input);
        }
        return false;
    }

    @Nullable
    public String getScriptContainerName(final String input) {
        if (denizen_1_2_2 != null) {
            return DenizenAPI_1_2_2.getScriptContainerName(input);
        } else if (denizen_1_1_1 != null) {
            return DenizenAPI_1_1_1.getScriptContainerName(input);
        } else if (denizen_1_1_0 != null) {
            return DenizenAPI_1_1_0.getScriptContainerName(input);
        } else if (denizen_1_0_9 != null) {
            return DenizenAPI_1_0_9.getScriptContainerName(input);
        }
        return null;
    }

    @Nullable
    public Set<String> getScriptNames() {
        if (denizen_1_2_2 != null) {
            return DenizenAPI_1_2_2.getScriptNames();
        } else if (denizen_1_1_1 != null) {
            return DenizenAPI_1_1_1.getScriptNames();
        } else if (denizen_1_1_0 != null) {
            return DenizenAPI_1_1_0.getScriptNames();
        } else if (denizen_1_0_9 != null) {
            return DenizenAPI_1_0_9.getScriptNames();
        }
        return null;
    }

    @Nullable
    public Object getScriptContainerAs(final String scriptName) {
        if (denizen_1_2_2 != null) {
            return DenizenAPI_1_2_2.getScriptContainerAs(scriptName);
        } else if (denizen_1_1_1 != null) {
            return DenizenAPI_1_1_1.getScriptContainerAs(scriptName);
        } else if (denizen_1_1_0 != null) {
            return DenizenAPI_1_1_0.getScriptContainerAs(scriptName);
        } else if (denizen_1_0_9 != null) {
            return DenizenAPI_1_0_9.getScriptContainerAs(scriptName);
        }
        return null;
    }

    @Nullable
    public Object mirrorBukkitPlayer(final Player player) {
        if (denizen_1_2_2 != null) {
            return DenizenAPI_1_2_2.mirrorBukkitPlayer(player);
        } else if (denizen_1_1_1 != null) {
            return DenizenAPI_1_1_1.mirrorBukkitPlayer(player);
        } else if (denizen_1_1_0 != null) {
            return DenizenAPI_1_1_0.mirrorBukkitPlayer(player);
        } else if (denizen_1_0_9 != null) {
            return DenizenAPI_1_0_9.mirrorBukkitPlayer(player);
        }
        return null;
    }

    @Nullable
    public Object mirrorCitizensNPC(final NPC npc) {
        if (npc == null) {
            return null;
        }
        if (denizen_1_2_2 != null) {
            return DenizenAPI_1_2_2.mirrorCitizensNPC(npc);
        } else if (denizen_1_1_1 != null) {
            return DenizenAPI_1_1_1.mirrorCitizensNPC(npc);
        } else if (denizen_1_1_0 != null) {
            return DenizenAPI_1_1_0.mirrorCitizensNPC(npc);
        } else if (denizen_1_0_9 != null) {
            return DenizenAPI_1_0_9.mirrorCitizensNPC(npc);
        }
        return null;
    }

    public void runTaskScript(final String scriptName, final Player player, NPC npc) {
        if (denizen_1_2_2 != null) {
            DenizenAPI_1_2_2.runTaskScript(scriptName, player, npc);
        } else if (denizen_1_1_1 != null) {
            DenizenAPI_1_1_1.runTaskScript(scriptName, player, npc);
        } else if (denizen_1_1_0 != null) {
            DenizenAPI_1_1_0.runTaskScript(scriptName, player, npc);
        } else if (denizen_1_0_9 != null) {
            DenizenAPI_1_0_9.runTaskScript(scriptName, player, npc);
        }
    }
}
