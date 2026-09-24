/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.listeners.npc;

import me.pikamug.quests.FabricMixinEvents;
import me.pikamug.quests.FabricQuestsPlugin;
import net.minecraft.world.entity.Entity;

/**
 * NPC listener for BOs-Easy-NPC.
 * Detects interactions via Fabric's UseEntityCallback and checks for EasyNPC entities.
 */
public class FabricEasyNpcListener extends FabricNpcListener {

    public FabricEasyNpcListener(FabricQuestsPlugin plugin) {
        super(plugin);
        register();
    }

    @Override
    public void register() {
        FabricMixinEvents.registerUseEntity((player, entity) -> {
            if (isNpc(entity)) {
                handleNpcInteract(player, entity);
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean isNpc(Entity entity) {
        if (entity == null) return false;
        // Check via reflection to avoid hard compile-time dependency
        try {
            return Class.forName("de.markusbordihn.easynpc.entity.easy.EasyNPCEntity").isInstance(entity);
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }
}
