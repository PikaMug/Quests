/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests;

import me.blackvein.quests.enums.ObjectiveType;
import me.blackvein.quests.events.quester.QuesterPostUpdateObjectiveEvent;
import me.blackvein.quests.events.quester.QuesterPreUpdateObjectiveEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

public abstract class CustomObjective implements Listener {

    private final Quests plugin = Quests.getPlugin(Quests.class);
    private String name = null;
    private String author = null;
    private String display = "Progress: %count%";
    private final Map<String, Short> items = new HashMap<String, Short>();
    private final LinkedList<Entry<String, Object>> data = new LinkedList<Entry<String, Object>>();
    private final Map<String, String> descriptions = new HashMap<String, String>();
    private String countPrompt = "Enter number";
    private boolean showCount = true;
    private int count = 1;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(final String author) {
        this.author = author;
    }
    
    public String getDisplay() {
        return display;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }
    
    public Map<String, Short> getItems() {
        return items;
    }
    
    public void addItem(final String type, final short durability) {
        this.items.put(type, durability);
    }
        
    public LinkedList<Entry<String, Object>> getData() {
        return data;
    }
    
    /**
     * Add a new prompt<p>
     * 
     * Note that the "defaultValue" Object will be cast to a String internally
     * 
     * @param title Prompt name
     * @param description Description of expected input
     * @param defaultValue Value to be used if input is not received
     */
    public void addStringPrompt(final String title, final String description, final Object defaultValue) {
        final Entry<String, Object> prompt = new AbstractMap.SimpleEntry<String, Object>(title, defaultValue);
        data.add(prompt);
        descriptions.put(title, description);
    }
    
    public Map<String, String> getDescriptions() {
        return descriptions;
    }

    public int getCount() {
        return count;
    }

    public void setCount(final int count) {
        this.count = count;
    }

    public String getCountPrompt() {
        return countPrompt;
    }

    public void setCountPrompt(final String countPrompt) {
        this.countPrompt = countPrompt;
    }
    
    /**
     * Check whether to let user set required amount for objective
     */
    public boolean canShowCount() {
        return showCount;
    }

    /**
     * Set whether to let user set required amount for objective
     * 
     * @param showCount Whether or not to show the count
     */
    public void setShowCount(final boolean showCount) {
        this.showCount = showCount;
    }
    
    public Map<String, Object> getDataForPlayer(final Player player, final CustomObjective customObj, final Quest quest) {
        final Quester quester = plugin.getQuester(player.getUniqueId());
        if (quester != null) {
            final Stage currentStage = quester.getCurrentStage(quest);
            if (currentStage == null) {
                return null;
            }
            CustomObjective found = null;
            for (final me.blackvein.quests.CustomObjective co : currentStage.customObjectives) {
                if (co.getName().equals(customObj.getName())) {
                    found = co;
                    break;
                }
            }
            if (found != null) {
                final Map<String, Object> m = new HashMap<String, Object>();
                for (final Entry<String, Object> datamap : found.getData()) {
                    for (final Entry<String, Object> e : currentStage.customObjectiveData) {
                        if (e.getKey().equals(datamap.getKey())) {
                            m.put(e.getKey(), e.getValue());
                        }
                    }
                }
                if (!m.isEmpty()) {
                    return m;
                }
            }
        }
        return null;
    }

    public void incrementObjective(final Player player, final CustomObjective obj, final int count, final Quest quest) {
        final Quester quester = plugin.getQuester(player.getUniqueId());
        if (quester != null) {
            if (quester.hasCustomObjective(quest, obj.getName())) {
                int index = -1;
                for (CustomObjective co : quester.getCurrentStage(quest).customObjectives) {
                    index++;
                    if (co.getName().equals(this.getName())) {
                        final int old = quester.getQuestData(quest).customObjectiveCounts.get(index);
                        plugin.getQuester(player.getUniqueId()).getQuestData(quest).customObjectiveCounts
                                .set(index, old + count);
                        break;
                    }
                }
                if (index > -1) {
                    final int progress = quester.getQuestData(quest).customObjectiveCounts.get(index);
                    final int goal = quester.getCurrentStage(quest).customObjectiveCounts.get(index);
                    
                    final ObjectiveType type = ObjectiveType.CUSTOM;
                    final QuesterPreUpdateObjectiveEvent preEvent 
                            = new QuesterPreUpdateObjectiveEvent(quester, quest, new Objective(type, progress, goal));
                    plugin.getServer().getPluginManager().callEvent(preEvent);
                    
                    if (progress >= goal) {
                        quester.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                                new ItemStack(Material.AIR, goal)), null, null, null, null, null, null, obj);
                        
                        // Multiplayer
                        int finalIndex = index;
                        quester.dispatchMultiplayerObjectives(quest, quester.getCurrentStage(quest), (final Quester q) -> {
                            final int old = q.getQuestData(quest).customObjectiveCounts.get(finalIndex);
                            q.getQuestData(quest).customObjectiveCounts.set(finalIndex, old + count);
                            q.finishObjective(quest, new Objective(type, new ItemStack(Material.AIR, 1), 
                                    new ItemStack(Material.AIR, goal)), null, null, null, null, null, null, obj);
                            return null;
                        });
                    }
                    
                    final QuesterPostUpdateObjectiveEvent postEvent 
                            = new QuesterPostUpdateObjectiveEvent(quester, quest, new Objective(type, progress, goal));
                    plugin.getServer().getPluginManager().callEvent(postEvent);
                }
            }
        }
    }
}
