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
            return Lang.get("itemCreateCriticalError");
        }
        if (!(context.getForWhom() instanceof Player) || !((QuestsAPI)context.getPlugin()).getSettings().canClickablePrompts()) {
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
