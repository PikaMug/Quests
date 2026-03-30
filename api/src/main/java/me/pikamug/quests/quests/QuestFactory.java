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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface QuestFactory {

    Set<UUID> getSelectingNpcs();

    void setSelectingNpcs(final Collection<UUID> selectingNpcs);

    List<String> getNamesOfQuestsBeingEdited();

    void setNamesOfQuestsBeingEdited(final Collection<String> questNames);

    void returnToMenu(final UUID uuid);

    void loadQuest(final UUID uuid, final Quest q);

    void deleteQuest(final UUID uuid);
}
