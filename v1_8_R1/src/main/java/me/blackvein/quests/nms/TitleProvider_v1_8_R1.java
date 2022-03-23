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

package me.blackvein.quests.nms;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.EnumTitleAction;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TitleProvider_v1_8_R1 extends TitleProvider {

    @Override
    void sendTitlePacket(final Player player, final String title, final String subtitle) {
        final CraftPlayer craft = (CraftPlayer) player;
        final String titleString = ComponentSerializer.toString(TextComponent.fromLegacyText(title));
        final String titleSubtitle = ComponentSerializer.toString(TextComponent.fromLegacyText(subtitle));

        final IChatBaseComponent titleJson = ChatSerializer.a(titleString);
        final IChatBaseComponent subtitleJSON = ChatSerializer.a(titleSubtitle);

        final PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleJson);
        final PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, subtitleJSON);

        craft.getHandle().playerConnection.sendPacket(titlePacket);
        craft.getHandle().playerConnection.sendPacket(subtitlePacket);
    }
}
