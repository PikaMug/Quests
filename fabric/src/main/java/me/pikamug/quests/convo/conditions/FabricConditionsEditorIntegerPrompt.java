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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.conditions.ConditionFactory;
import me.pikamug.quests.convo.FabricQuestsIntegerPrompt;
import org.browsit.conversations.api.Conversations;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class FabricConditionsEditorIntegerPrompt extends FabricQuestsIntegerPrompt {
    private final UUID uuid;
    private final ConditionFactory factory;

    public FabricConditionsEditorIntegerPrompt(final UUID uuid) {
        this.uuid = uuid;
        this.factory = FabricQuestsPlugin.getInstance().getConditionFactory();
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

    public abstract net.minecraft.ChatFormatting getNumberColor(int number);

    public abstract String getSelectionText(int number);

    public abstract String getAdditionalText(int number);

    public abstract @NotNull String getPromptText();

    public abstract void acceptInput(final Number input);

    public void start() {
        Conversations.create(uuid).title(getName()).prompt(getPromptText(), Number.class, prompt -> prompt
                .converter(Integer::parseInt).fetch((input, sender) -> acceptInput(input))).start();
    }
}
