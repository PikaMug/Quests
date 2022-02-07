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

package me.blackvein.quests.quests;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface QuestFactory {
    Map<UUID, Block> getSelectedBlockStarts();

    void setSelectedBlockStarts(final Map<UUID, Block> selectedBlockStarts);

    Map<UUID, Block> getSelectedKillLocations();

    void setSelectedKillLocations(final Map<UUID, Block> selectedKillLocations);

    Map<UUID, Block> getSelectedReachLocations();

    void setSelectedReachLocations(final Map<UUID, Block> selectedReachLocations);

    Set<UUID> getSelectingNpcs();

    void setSelectingNpcs(final Set<UUID> selectingNpcs);

    List<String> getNamesOfQuestsBeingEdited();

    void setNamesOfQuestsBeingEdited(final List<String> questNames);

    ConversationFactory getConversationFactory();

    Prompt returnToMenu(final ConversationContext context);

    void loadQuest(final ConversationContext context, final IQuest q);

    void deleteQuest(final ConversationContext context);

    void saveQuest(final ConversationContext context, final ConfigurationSection section);

    /*void saveRequirements(final ConversationContext context, final ConfigurationSection section);

    void saveStages(final ConversationContext context, final ConfigurationSection section);

    void saveRewards(final ConversationContext context, final ConfigurationSection section);

    void savePlanner(final ConversationContext context, final ConfigurationSection section);

    void saveOptions(final ConversationContext context, final ConfigurationSection section);*/
}
