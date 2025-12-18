/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.conditions;

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.conditions.ConditionFactory;
import me.pikamug.quests.convo.QuestsNumericPrompt;
import org.browsit.conversations.api.Conversations;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ConditionsEditorNumericPrompt extends QuestsNumericPrompt {
    private final UUID uuid;
    private final ConditionFactory factory;
    
    public ConditionsEditorNumericPrompt(final UUID uuid) {
        this.uuid = uuid;
        this.factory = BukkitQuestsPlugin.getInstance().getConditionFactory();
    }
    
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    public UUID getUniqueId() {
        return uuid;
    }
    
    public ConditionFactory getConditionFactory() {
        return factory;
    }
    
    public abstract int getSize();
    
    public abstract String getTitle();
    
    public abstract ChatColor getNumberColor(int number);
    
    public abstract String getSelectionText(int number);
    
    public abstract String getAdditionalText(int number);

    public abstract @NotNull String getPromptText();

    public abstract void acceptInput(final Number input);

    public void start() {
        Conversations.create(uuid).prompt(getPromptText(), Number.class, prompt -> prompt
                .converter(Number::intValue).fetch((input, sender) -> acceptInput(input))).start();
    }
}
