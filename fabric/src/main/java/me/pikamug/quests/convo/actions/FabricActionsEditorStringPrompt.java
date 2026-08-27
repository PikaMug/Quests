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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.actions.ActionFactory;
import me.pikamug.quests.convo.FabricQuestsStringPrompt;
import org.browsit.conversations.api.Conversations;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class FabricActionsEditorStringPrompt extends FabricQuestsStringPrompt {
    private final UUID uuid;
    private final ActionFactory factory;

    public FabricActionsEditorStringPrompt(final UUID uuid) {
        this.uuid = uuid;
        this.factory = FabricQuestsPlugin.getInstance().getActionFactory();
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

    public abstract String getTitle();

    public abstract String getQueryText();

    public abstract @NotNull String getPromptText();

    public abstract void acceptInput(String input);

    public void start() {
        Conversations.create(uuid).title(getName()).prompt(getPromptText(), String.class, prompt -> prompt
                .converter(String::valueOf).fetch((input, sender) -> acceptInput(input))).start();
    }
}
