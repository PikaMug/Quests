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

import me.pikamug.quests.Quests;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class QuestsNumericPrompt extends NumericPrompt {
    private static final HandlerList HANDLERS = new HandlerList();
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^(\\d+) - ");

    public QuestsNumericPrompt() {
    }
    
    public String getName() {
        return getClass().getSimpleName();
    }
    
    public HandlerList getHandlers() {
        return HANDLERS;
    }
     
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
    
    @Override
    public @NotNull String getPromptText(@NotNull final ConversationContext cc) {
        return sendClickableSelection(getBasicPromptText(cc), cc);
    }
    
    public abstract String getBasicPromptText(ConversationContext cc);
    
    /**
     * Takes a Quests-styled conversation interface and decides how to send it
     * to the target. Players receive clickable text, others (i.e. console)
     * receive plain text, which is returned to be delivered through the
     * Conversations API.
     * 
     * @param input   the Quests-styled conversation interface
     * @param context the conversation context
     * @return        plain text to deliver
     */
    public static String sendClickableSelection(final String input, final ConversationContext context) {
        if (context.getPlugin() == null) {
            return "ERROR";
        }
        if (!(context.getForWhom() instanceof Player) || !((Quests)context.getPlugin()).getConfigSettings().canClickablePrompts()) {
            return input;
        }
        final String[] basicText = input.split("\n");
        final TextComponent component = new TextComponent("");
        boolean first = true;
        for (final String line : basicText) {
            final Matcher matcher = NUMBER_PATTERN.matcher(ChatColor.stripColor(line));
            final TextComponent lineComponent = new TextComponent(TextComponent.fromLegacyText(line));
            if (matcher.find()) {
                lineComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + matcher.group(1)));
            }
            if (first) {
                first = false;
            } else {
                component.addExtra("\n");
            }
            component.addExtra(lineComponent);
        }
        ((Player)context.getForWhom()).spigot().sendMessage(component);
        return "";
    }
}
