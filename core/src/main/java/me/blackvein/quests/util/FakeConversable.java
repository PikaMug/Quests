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

package me.blackvein.quests.util;

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
public class FakeConversable implements Conversable {
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

