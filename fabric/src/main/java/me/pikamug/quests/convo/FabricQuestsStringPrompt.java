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

import java.util.List;

public abstract class FabricQuestsStringPrompt implements QuestsPrompt {

    public FabricQuestsStringPrompt() {
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Takes a header, footer, and a list of names, formats them in Quests
     * style, and decides how to deliver the result. Players are sent
     * clickable text which is returned in a format to be delivered
     * through the Conversations API.
     *
     * @param header  the menu header
     * @param list    a list of strings to display
     * @param footer  the menu footer
     * @param quester the quest player
     * @return        plain text to deliver
     */
    protected String sendClickableMenu(final String header, final List<String> list, final String footer,
                                       final FabricQuester quester) {
        if (quester == null || quester.getPlugin() == null) {
            return "ERROR";
        }
        if (!quester.getPlugin().getConfigSettings().canClickablePrompts()) {
            return ChatFormatting.GOLD + header + "\n" + ChatFormatting.AQUA
                    + String.join(ChatFormatting.GRAY + ", " + ChatFormatting.AQUA, list)
                    + "\n" + ChatFormatting.YELLOW + footer;
        }
        final MutableComponent component = Component.literal(header + "\n")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
        for (int i = 0; i < list.size(); i++) {
            final String stripped = ChatFormatting.stripFormatting(list.get(i));
            final Component questName = Component.literal(list.get(i))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent.RunCommand("/quests choice " + stripped)));
            component.append(questName);
            if (i < (list.size() - 1)) {
                component.append(Component.literal(", ")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
            }
        }
        component.append(Component.literal("\n" + footer)
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
        quester.getServerPlayer().sendSystemMessage(component);
        return "";
    }
}
