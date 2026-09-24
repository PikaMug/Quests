/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.dependencies;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Delegates party lookups to Open Parties and Claims. Only instantiated when
 * the mod is present, so its API classes are never linked otherwise.
 */
public class FabricOpenPartiesAccessor {

    public List<UUID> getPartyMemberUuids(MinecraftServer server, UUID memberId) {
        final List<UUID> uuids = new LinkedList<>();
        final IServerPartyAPI party = getParty(server, memberId);
        if (party == null) return uuids;
        party.getMemberInfoStream().forEach(member -> {
            if (member != null && !member.getUUID().equals(memberId)) {
                uuids.add(member.getUUID());
            }
        });
        return uuids;
    }

    public List<ServerPlayer> getOnlinePartyMembers(MinecraftServer server, UUID memberId) {
        final List<ServerPlayer> members = new LinkedList<>();
        final IServerPartyAPI party = getParty(server, memberId);
        if (party == null) return members;
        party.getOnlineMemberStream().forEach(member -> {
            if (member != null && !member.getUUID().equals(memberId)) {
                members.add(member);
            }
        });
        return members;
    }

    private IServerPartyAPI getParty(MinecraftServer server, UUID memberId) {
        final OpenPACServerAPI api = OpenPACServerAPI.get(server);
        if (api == null) return null;
        final IPartyManagerAPI partyManager = api.getPartyManager();
        if (partyManager == null) return null;
        return partyManager.getPartyByMember(memberId);
    }
}