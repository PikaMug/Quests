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

package me.blackvein.quests.nms;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class TitleProvider {

    private static TitleProvider loaded;

    static {
        final String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            final String packageName = TitleProvider.class.getPackage().getName();
            if (internalsName.startsWith("v1_8_R")) {
                loaded = (TitleProvider) Class.forName(packageName + ".TitleProvider_" + internalsName)
                        .newInstance();
            } else {
                loaded = new TitleProvider_Bukkit();
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
