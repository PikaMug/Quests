/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.generic;

import me.pikamug.quests.convo.FabricQuestsIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FabricOverridePrompt extends FabricQuestsEditorStringPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsIntegerPrompt oldPrompt;
    private final String promptText;
    private final String classPrefix;

    public FabricOverridePrompt(final @NotNull UUID uuid, final FabricQuestsIntegerPrompt old, final String promptText) {
        super(uuid);
        this.uuid = uuid;
        oldPrompt = old;
        classPrefix = old.getClass().getSimpleName();
        this.promptText = promptText;
    }

    private final int size = 1;

    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getQueryText() {
        return promptText;
    }

    @Override
    public @NotNull String getPromptText() {
        return ChatFormatting.YELLOW + getQueryText();
    }

    @Override
    public void acceptInput(final String input) {
        if (input == null) {
            return;
        }
        final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, me.pikamug.quests.FabricQuestsPlugin.getInstance());
        if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
            if (sender != null) {
                sender.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        ChatFormatting.YELLOW + FabricLang.get("stageEditorMessageCleared")));
            }
            SessionData.set(uuid, classPrefix + "-override", FabricLang.get("cmdClear"));
        } else if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel"))) {
            SessionData.set(uuid, classPrefix + "-override", input);
        }
        oldPrompt.start();
    }
}
