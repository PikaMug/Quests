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

    public void acceptConversationInput(@NotNull String input) {
    }

    public boolean beginConversation(Conversation conversation) {
        begunConversation = conversation;
        conversation.outputNextPrompt();
        return true;
    }

    public void abandonConversation(@NotNull Conversation conversation) {
        abandonedConverstion = conversation;
    }

    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent details) {
        abandonedConverstion = conversation;
        abandonedConversationEvent = details;
    }

    public void sendRawMessage(@NotNull String message) {
        lastSentMessage = message;
    }

    public Server getServer() {
        return null;
    }

    public String getName() {
        return null;
    }

    public boolean isPermissionSet(String name) {
        return false;
    }

    public boolean isPermissionSet(Permission perm) {
        return false;
    }

    public boolean hasPermission(String name) {
        return false;
    }

    public boolean hasPermission(Permission perm) {
        return false;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return null;
    }

    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return null;
    }

    public void removeAttachment(PermissionAttachment attachment) {
    }

    public void recalculatePermissions() {
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    public boolean isOp() {
        return false;
    }

    public void setOp(boolean value) {
    }
}

