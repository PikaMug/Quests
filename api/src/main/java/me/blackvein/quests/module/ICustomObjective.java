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

package me.blackvein.quests.module;

import me.blackvein.quests.quests.IQuest;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.Map;

public interface ICustomObjective {

    String getModuleName();

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
     * @param player Player attempting this objective
     * @param customObj The objective being attempted
     * @param quest Current me.blackvein.quests.Quest which includes this objective
     * @return data
     */
    Map<String, Object> getDataForPlayerTemp(final Player player, final ICustomObjective customObj, final IQuest quest);

    void incrementObjectiveTemp(final Player player, final ICustomObjective obj, final int count, final IQuest quest);
}
