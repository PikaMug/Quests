/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo;

import me.pikamug.quests.player.FabricQuester;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FabricQuestsIntegerPrompt implements QuestsPrompt {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(\\d+) - ");

    public FabricQuestsIntegerPrompt() {
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public @NotNull String getPromptText(@NotNull final FabricQuester quester) {
        return sendClickableSelection(getPromptText(), quester);
    }

    public abstract String getPromptText();

    public abstract void start();

    /**
     * Takes a Quests-styled conversation interface and decides how to send it
     * to the target. Players receive clickable text, others (i.e. console)
     * receive plain text, which is returned to be delivered through the
     * Conversations API.
     *
     * @param input   the Quests-styled conversation interface
     * @param quester the quest player
     * @return        plain text to deliver
     */
    public static String sendClickableSelection(final String input, final FabricQuester quester) {
        if (quester == null || quester.getPlugin() == null || quester.getServerPlayer() == null) {
            return "ERROR";
        }
        if (!quester.getPlugin().getConfigSettings().canClickablePrompts()) {
            return input;
        }
        final String[] basicText = input.split("\n");
        final MutableComponent component = Component.literal("");
        boolean first = true;
        for (final String line : basicText) {
            final String stripped = ChatFormatting.stripFormatting(line);
            final Matcher matcher = NUMBER_PATTERN.matcher(stripped);
            final Component lineComponent;
            if (matcher.find()) {
                lineComponent = Component.literal(stripped)
                        .withStyle(Style.EMPTY.withClickEvent(
                                new ClickEvent.RunCommand("/quests choice " + matcher.group(1))));
            } else {
                lineComponent = Component.literal(stripped);
            }
            if (first) {
                first = false;
            } else {
                component.append(Component.literal("\n"));
            }
            component.append(lineComponent);
        }
        quester.getServerPlayer().sendSystemMessage(component);
        return "";
    }
}
