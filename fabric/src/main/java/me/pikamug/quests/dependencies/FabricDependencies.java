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

import me.pikamug.quests.FabricQuestsPlugin;
import net.fabricmc.loader.api.FabricLoader;

import java.util.UUID;

public class FabricDependencies implements Dependencies {

    private final FabricQuestsPlugin plugin;
    private boolean hasEasyNpc = false;
    private boolean hasTaterzens = false;
    private boolean hasOpenParties = false;

    public FabricDependencies(final FabricQuestsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        hasEasyNpc = FabricLoader.getInstance().isModLoaded("easy_npc");
        hasTaterzens = FabricLoader.getInstance().isModLoaded("taterzens");
        hasOpenParties = FabricLoader.getInstance().isModLoaded("openpartiesandclaims");

        if (hasEasyNpc) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "BOs-Easy-NPC");
        }
        if (hasTaterzens) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "Taterzens");
        }
        if (hasOpenParties) {
            FabricQuestsPlugin.LOGGER.info("Detected {} support", "Open Parties and Claims");
        }
    }

    @Override
    public boolean isPluginAvailable(String pluginName) {
        return FabricLoader.getInstance().isModLoaded(pluginName.toLowerCase());
    }

    public boolean isNpc(UUID uuid) {
        return plugin.getQuestNpcUuids().contains(uuid);
    }

    public boolean hasEasyNpc() {
        return hasEasyNpc;
    }

    public boolean hasTaterzens() {
        return hasTaterzens;
    }

    public boolean hasAnyNpcDependencies() {
        return hasEasyNpc || hasTaterzens;
    }

    /**
     * Returns whether the Open Parties and Claims mod is installed, which
     * backs the "use parties plugin" quest option on Fabric.
     */
    public boolean hasOpenParties() {
        return hasOpenParties;
    }

    public String getNpcName(UUID uuid) {
        final net.minecraft.server.level.ServerPlayer player =
                plugin.getServer() != null ? plugin.getServer().getPlayerList().getPlayer(uuid) : null;
        return player != null ? player.getName().getString() : uuid.toString().substring(0, 8);
    }

    /**
     * Returns the TextPlaceholderAPI marker when the placeholder-api mod is
     * installed. Otherwise returns null so placeholder-based conditions are
     * reported as "not installed".
     */
    public Object getPlaceholderApi() {
        return FabricLoader.getInstance().isModLoaded("placeholder-api") ? Boolean.TRUE : null;
    }
}
