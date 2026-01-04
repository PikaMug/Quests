/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.util;

import me.pikamug.quests.BukkitQuestsPlugin;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

public class BukkitUpdateChecker {

    private final BukkitQuestsPlugin plugin;
    private final int resourceId;

    public BukkitUpdateChecker(final BukkitQuestsPlugin plugin, final int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        if (plugin.getConfigSettings().canUpdateCheck()) {
            Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (final InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource="
                        + this.resourceId).openStream(); final Scanner scanner = new Scanner(inputStream)) {
                    if (scanner.hasNext()) {
                        consumer.accept(scanner.next());
                    }
                } catch (final IOException e) {
                    this.plugin.getLogger().info("Update check failed: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Compares two valid semantic versions, i.e. 5.0.3
     *
     * @param currentVersion Current resource version
     * @param compareVersion Resource version to compare against
     * @return true if compared version is higher
     */
    public static boolean compareVersions(final String currentVersion, final String compareVersion) {
        if (currentVersion == null || compareVersion == null) {
            return false;
        }
        final String[] currentParts = currentVersion.split("\\.");
        final String[] compareParts = compareVersion.split("\\.");
        final int length = Math.max(currentParts.length, compareParts.length);
        if (length > 3) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            final int currentPart = Integer.parseInt(currentParts[i]);
            final int comparePart = Integer.parseInt(compareParts[i]);
            if (comparePart > currentPart) {
                return true;
            }
        }
        return false;
    }
}
