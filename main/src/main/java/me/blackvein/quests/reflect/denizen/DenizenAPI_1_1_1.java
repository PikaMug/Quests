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

package me.blackvein.quests.reflect.denizen;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;

import net.citizensnpcs.api.npc.NPC;

public class DenizenAPI_1_1_1 {
    
    @Nullable
    public static boolean containsScript(final String input) {
        return ScriptRegistry.containsScript(input);
    }
    
    @Nullable
    public static String getScriptContainerName(final String input) {
        return ScriptRegistry.getScriptContainer(input).getName();
    }
    
    @Nullable
    public static Set<String> getScriptNames() {
        return ScriptRegistry.scriptContainers.keySet();
    }
    
    @Nullable
    public static Object getScriptContainerAs(final String scriptName) {
        return ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
    }
    
    @Nullable
    public static Object mirrorBukkitPlayer(final Player player) {
        return PlayerTag.mirrorBukkitPlayer(player);
    }
    
    @Nullable
    public static Object mirrorCitizensNPC(final NPC npc) {
        return NPCTag.mirrorCitizensNPC(npc);
    }
    
    @Nullable
    public static void runTaskScript(final String scriptName, final Player player) {
        final TaskScriptContainer taskScript = ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
        final BukkitScriptEntryData entryData = new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(player), null);
        final ScriptQueue queue = new InstantQueue(taskScript.getName())
                .addEntries(taskScript.getBaseEntries(entryData.clone()));
        queue.start();
    }
}
