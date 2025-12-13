/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.actions.ActionFactory;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import org.bukkit.ChatColor;

import java.util.UUID;

public abstract class ActionsEditorNumericPrompt extends QuestsNumericPrompt {
    private final UUID uuid;
    private final ActionFactory factory;
    
    public ActionsEditorNumericPrompt(final UUID uuid) {
        this.uuid = uuid;
        this.factory = BukkitQuestsPlugin.getInstance().getActionFactory();
    }
    
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
    
    public UUID getUniqueId() {
        return uuid;
    }
    
    public ActionFactory getActionFactory() {
        return factory;
    }
    
    public abstract int getSize();
    
    public abstract String getTitle();
    
    public abstract ChatColor getNumberColor(int number);
    
    public abstract String getSelectionText(int number);
    
    public abstract String getAdditionalText(int number);
}
