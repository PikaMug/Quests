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

package me.blackvein.quests.util;

import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.core.TaskScriptContainer;

import net.citizensnpcs.api.npc.NPC;

/**
 * This class's imports must exist separately from DenizenAPI.java in order to not crash from NoClassDefFoundError at
 * runtime
 */
public class DenizenAPI_1_1_0 {
    
    @Nullable
    public static boolean containsScript(String input) {
        return ScriptRegistry.containsScript(input);
    }
    
    @Nullable
    public static String getScriptContainerName(String input) {
        return ScriptRegistry.getScriptContainer(input).getName();
    }
    
    @Nullable
    public static Set<String> _getScriptNames() {
        return ScriptRegistry._getScriptNames();
    }
    
    @Nullable
    public static Object getScriptContainerAs(String scriptName) {
        return ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
    }
    
    @Nullable
    public static Object mirrorBukkitPlayer(Player player) {
        return PlayerTag.mirrorBukkitPlayer(player);
    }
    
    @Nullable
    public static Object mirrorCitizensNPC(NPC npc) {
        return NPCTag.mirrorCitizensNPC(npc);
    }
    
    @Nullable
    public static void runTaskScript(String scriptName, Player player) {
        TaskScriptContainer taskScript = ScriptRegistry.getScriptContainerAs(scriptName, TaskScriptContainer.class);
        BukkitScriptEntryData entryData = new BukkitScriptEntryData(PlayerTag.mirrorBukkitPlayer(player), null);
        taskScript.runTaskScript(entryData, null);
    }
}
