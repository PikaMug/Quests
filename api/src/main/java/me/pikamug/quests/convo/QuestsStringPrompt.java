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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.List;

public abstract class QuestsStringPrompt extends StringPrompt {
    private static final HandlerList HANDLERS = new HandlerList();
    
    public QuestsStringPrompt() {
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

    /**
     * Takes a header, footer, and a list of names, formats them in Quests
     * style, and decides how to deliver the result. Players are sent
     * clickable text, all others (i.e. console) are sent plain text,
     * which is returned to be delivered through the Conversations API.
     * 
     * @param header  the menu header
     * @param list    a list of strings to display
     * @param footer  the menu footer
     * @param context the conversation context
     * @return        plain text to deliver
     */
    protected String sendClickableMenu(final String header, final List<String> list, final String footer,
                                       final ConversationContext context) {
        if (context.getPlugin() == null) {
            return "ERROR";
        }
        if (!(context.getForWhom() instanceof Player) || !((Quests)context.getPlugin()).getConfigSettings().canClickablePrompts()) {
            return ChatColor.GOLD + header + "\n" + ChatColor.AQUA + String.join(ChatColor.GRAY + ", " + ChatColor.AQUA, list) + "\n" + ChatColor.YELLOW + footer;
        }
        final TextComponent component = new TextComponent(header + "\n");
        component.setColor(ChatColor.GOLD);
        final TextComponent footerComponent = new TextComponent("\n" + footer);
        footerComponent.setColor(ChatColor.YELLOW);
        final TextComponent separator = new TextComponent(", ");
        separator.setColor(ChatColor.GRAY);
        for (int i = 0; i < list.size(); i++) {
            final TextComponent questName = new TextComponent(list.get(i));
            questName.setColor(ChatColor.AQUA);
            questName.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests choice " + ChatColor.stripColor(list.get(i))));
            component.addExtra(questName);
            if (i < (list.size() - 1)) {
                component.addExtra(separator);
            }
        }
        component.addExtra(footerComponent);
        ((Player)context.getForWhom()).spigot().sendMessage(component);
        return "";
    }
}
