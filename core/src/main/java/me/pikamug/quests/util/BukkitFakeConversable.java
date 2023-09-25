/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.util;

import org.bukkit.Server;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * For use when creating a new ConversationContext
 */
public class BukkitFakeConversable implements Conversable {
    public String lastSentMessage;
    public Conversation begunConversation;
    public Conversation abandonedConverstion;
    public ConversationAbandonedEvent abandonedConversationEvent;

    public boolean isConversing() {
        return false;
    }

    public void acceptConversationInput(@NotNull final String input) {
    }

    public boolean beginConversation(final Conversation conversation) {
        begunConversation = conversation;
        conversation.outputNextPrompt();
        return true;
    }

    public void abandonConversation(@NotNull final Conversation conversation) {
        abandonedConverstion = conversation;
    }

    public void abandonConversation(@NotNull final Conversation conversation,
                                    @NotNull final ConversationAbandonedEvent details) {
        abandonedConverstion = conversation;
        abandonedConversationEvent = details;
    }

    public void sendRawMessage(@NotNull final String message) {
        lastSentMessage = message;
    }

    public Server getServer() {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isPermissionSet(final String name) {
        return false;
    }

    public boolean isPermissionSet(final Permission perm) {
        return false;
    }

    public boolean hasPermission(final String name) {
        return false;
    }

    public boolean hasPermission(final Permission perm) {
        return false;
    }

    public PermissionAttachment addAttachment(final Plugin plugin, final String name, final boolean value) {
        return null;
    }

    public PermissionAttachment addAttachment(final Plugin plugin) {
        return null;
    }

    public PermissionAttachment addAttachment(final Plugin plugin, final String name, final boolean value,
                                              final int ticks) {
        return null;
    }

    public PermissionAttachment addAttachment(final Plugin plugin, final int ticks) {
        return null;
    }

    public void removeAttachment(final PermissionAttachment attachment) {
    }

    public void recalculatePermissions() {
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    public boolean isOp() {
        return false;
    }

    public void setOp(final boolean value) {
    }
}

