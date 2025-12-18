/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.actions;

import java.util.List;
import java.util.UUID;

public interface ActionFactory {

    //ConversationFactory getConversationFactory();

    List<String> getNamesOfActionsBeingEdited();

    void setNamesOfActionsBeingEdited(final List<String> actionNames);

    void returnToMenu(final UUID uuid);

    void loadData(final UUID uuid, final Action action);

    void clearData(final UUID uuid);

    void deleteAction(final UUID uuid);

    void saveAction(final UUID uuid);
}
