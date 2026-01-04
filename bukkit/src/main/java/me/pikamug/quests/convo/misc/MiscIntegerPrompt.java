/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.misc;

import me.pikamug.quests.convo.QuestsIntegerPrompt;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class MiscIntegerPrompt extends QuestsIntegerPrompt {
    private final UUID uuid;

    public MiscIntegerPrompt(final @NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public abstract int getSize();

    public abstract String getTitle();

    public abstract ChatColor getNumberColor(int number);

    public abstract String getSelectionText(int number);

    public abstract String getAdditionalText(int number);
}
