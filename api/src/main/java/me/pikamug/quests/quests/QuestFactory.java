/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.quests;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

public interface QuestFactory {

    ConcurrentSkipListSet<UUID> getSelectingNpcs();

    void setSelectingNpcs(final ConcurrentSkipListSet<UUID> selectingNpcs);

    List<String> getNamesOfQuestsBeingEdited();

    void setNamesOfQuestsBeingEdited(final List<String> questNames);

    //ConversationFactory getConversationFactory();

    void returnToMenu(final UUID uuid);

    void loadQuest(final UUID uuid, final Quest q);

    void deleteQuest(final UUID uuid);

    void saveQuest(final UUID uuid, final ConfigurationSection section);

    /*void saveRequirements(final ConversationContext context, final ConfigurationSection section);

    void saveStages(final ConversationContext context, final ConfigurationSection section);

    void saveRewards(final ConversationContext context, final ConfigurationSection section);

    void savePlanner(final ConversationContext context, final ConfigurationSection section);

    void saveOptions(final ConversationContext context, final ConfigurationSection section);*/
}
