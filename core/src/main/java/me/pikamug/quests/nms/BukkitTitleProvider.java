/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class BukkitTitleProvider {

    private static BukkitTitleProvider loaded;

    static {
        final String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            final String packageName = BukkitTitleProvider.class.getPackage().getName();
            if (internalsName.startsWith("v1_8_R")) {
                loaded = (BukkitTitleProvider) Class.forName(packageName + ".BukkitTitleProvider_" + internalsName)
                        .newInstance();
            } else {
                // Should not be an issue because single thread, alternatives welcome!
                loaded = new BukkitTitleProvider_Modern();
            }
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                | ClassCastException exception) {
            Bukkit.getLogger().severe("[Quests] No valid title implementation for version " + internalsName);
        }
    }

    abstract void sendTitlePacket(Player player, String title, String subtitle);

    /**
     * Sends the title to the player.
     *
     * @param player
     *                   The player to send the title to.
     * @param title
     *                   The title.
     * @param subtitle
     *                   The subtitle.
     */
    public static void sendTitle(final Player player, final String title, final String subtitle) {
        loaded.sendTitlePacket(player, title, subtitle);
    }
}
