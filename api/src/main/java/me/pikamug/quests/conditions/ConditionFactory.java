/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.conditions;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;

import java.util.List;

public interface ConditionFactory {
    ConversationFactory getConversationFactory();

    List<String> getNamesOfConditionsBeingEdited();

    void setNamesOfConditionsBeingEdited(final List<String> conditionNames);

    Prompt returnToMenu(final ConversationContext context);

    void loadData(final ConversationContext context, final Condition condition);

    void clearData(final ConversationContext context);

    void deleteCondition(final ConversationContext context);

    void saveCondition(final ConversationContext context);
}
