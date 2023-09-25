/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.module;

import me.pikamug.quests.quests.Quest;

import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public interface CustomObjective {

    String getModuleName();

    @SuppressWarnings("unused")
    Map.Entry<String, Short> getModuleItem();

    String getName();

    void setName(final String name);

    String getAuthor();

    void setAuthor(final String author);

    String getDisplay();

    void setDisplay(final String display);

    Map.Entry<String, Short> getItem();

    void setItem(final String type, final short durability);

    LinkedList<Map.Entry<String, Object>> getData();

    /**
     * Add a new prompt<p>
     *
     * Note that the "defaultValue" Object will be cast to a String internally
     *
     * @param title Prompt name
     * @param description Description of expected input
     * @param defaultValue Value to be used if input is not received
     */
    void addStringPrompt(final String title, final String description, final Object defaultValue);

    Map<String, String> getDescriptions();

    int getCount();

    void setCount(final int count);

    String getCountPrompt();

    void setCountPrompt(final String countPrompt);

    /**
     * Check whether to let user set required amount for objective
     */
    boolean canShowCount();

    /**
     * Set whether to let user set required amount for objective
     *
     * @param showCount Whether to show the count
     */
    void setShowCount(final boolean showCount);

    /**
     * Get custom objective data for applicable player
     *
     * @param uuid UUID of player attempting this objective
     * @param customObj The objective being attempted
     * @param quest Current me.pikamug.quests.Quest which includes this objective
     * @return data Map of custom objective data
     */
    Map<String, Object> getDataForPlayer(final UUID uuid, final CustomObjective customObj, final Quest quest);

    /**
     * Increment objective count for applicable player
     *
     * @param uuid UUID of player attempting this objective
     * @param customObj The objective being attempted
     * @param quest Current me.pikamug.quests.Quest which includes this objective
     * @param count Amount to increase objective count by
     */
    void incrementObjective(final UUID uuid, final CustomObjective customObj, final Quest quest, final int count);
}
