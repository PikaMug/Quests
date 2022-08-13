/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.convo;

import me.blackvein.quests.QuestsAPI;
import me.blackvein.quests.util.Lang;
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
            return Lang.get("itemCreateCriticalError");
        }
        if (!(context.getForWhom() instanceof Player) || !((QuestsAPI)context.getPlugin()).getSettings().canClickablePrompts()) {
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
