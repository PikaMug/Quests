/*******************************************************************************************************
 * Continued by PikaMug (formerly HappyPikachu) with permission from _Blackvein_. All rights reserved.
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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class CustomObjective implements Listener {

	private Quests plugin = Quests.getPlugin(Quests.class);
	private String name = null;
	private String author = null;
	private LinkedList<Entry<String, Object>> data = new LinkedList<Entry<String, Object>>();
	private Map<String, String> descriptions = new HashMap<String, String>();
	private String countPrompt = "Enter number";
	private String display = "Progress: %count%";
	private boolean showCount = true;
	private int count = 1;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
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
	public void addStringPrompt(String title, String description, Object defaultValue) {
		Entry<String, Object> prompt = new AbstractMap.SimpleEntry<String, Object>(title, defaultValue);
		data.add(prompt);
		descriptions.put(title, description);
	}
	
	/**
	 * Set the title of a prompt
	 * 
	 * @param name Prompt title
	 * @deprecated use addPrompt(name, description)
	 */
	public void addData(String name) {
		Entry<String, Object> prompt = new AbstractMap.SimpleEntry<String, Object>(name, null);
		data.add(prompt);
	}
	
	public Map<String, String> getDescriptions() {
		return descriptions;
	}

	/**
	 * Set the description for the specified prompt
	 * 
	 * @param name Prompt title
	 * @param description Description of expected input
	 * @deprecated use addTaskPrompt(name, description)
	 */
	public void addDescription(String name, String description) {
		descriptions.put(name, description);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

    public String getCountPrompt() {
		return countPrompt;
	}

	public void setCountPrompt(String countPrompt) {
		this.countPrompt = countPrompt;
	}
	
	/**
	 * Check whether to let user set required amount for objective
	 * 
	 * @param enableCount
	 */
	public boolean canShowCount() {
		return showCount;
	}

	/**
	 * Set whether to let user set required amount for objective
	 * 
	 * @param enableCount
	 */
	public void setShowCount(boolean showCount) {
		this.showCount = showCount;
	}

	/**
	 * Check whether to let user set required amount for objective
	 * 
	 * @param enableCount
	 * @deprecated use setShowCount(boolean)
	 */
	public void setEnableCount(boolean enableCount) {
		setShowCount(enableCount);
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}
	
	public Map<String, Object> getDataForPlayer(Player player, CustomObjective customObj, Quest quest) {
		return getDatamap(player, customObj, quest);
	}

	/**
	 * Get data for specified player's current stage
	 * 
	 * @param player The player to get data for
	 * @param obj The CustomObjective to get data for
	 * @param quest Quest to get player's current stage. Returns null if player is not on quest
	 * @return data map if everything matches, otherwise null
	 * @deprecated use getDataForPlayer()
	 */
	public Map<String, Object> getDatamap(Player player, CustomObjective obj, Quest quest) {
		Quester quester = plugin.getQuester(player.getUniqueId());
		if (quester != null) {
			Stage currentStage = quester.getCurrentStage(quest);
			if (currentStage == null) {
				return null;
			}
			CustomObjective found = null;
			for (me.blackvein.quests.CustomObjective co : currentStage.customObjectives) {
				if (co.getName().equals(obj.getName())) {
					found = co;
					break;
				}
			}
			if (found != null) {
				Map<String, Object> m = new HashMap<String, Object>();
				for (Entry<String, Object> datamap : found.getData()) {
					for (Entry<String, Object> e : currentStage.customObjectiveData) {
						if (e.getKey().equals(datamap.getKey())) {
							m.put(e.getKey(), e.getValue());
						}
					}
				}
				if (m != null && !m.isEmpty()) {
					return m;
				}
			}
		}
		return null;
	}

	public void incrementObjective(Player player, CustomObjective obj, int count, Quest quest) {
		Quester quester = plugin.getQuester(player.getUniqueId());
		if (quester != null) {
			// Check if the player has Quest with objective
			boolean hasQuest = false;
			for (CustomObjective co : quester.getCurrentStage(quest).customObjectives) {
				if (co.getName().equals(obj.getName())) {
					hasQuest = true;
					break;
				}
			}
			if (hasQuest && quester.hasCustomObjective(quest, obj.getName())) {
				if (quester.getQuestData(quest).customObjectiveCounts.containsKey(obj.getName())) {
					int old = quester.getQuestData(quest).customObjectiveCounts.get(obj.getName());
					plugin.getQuester(player.getUniqueId()).getQuestData(quest).customObjectiveCounts.put(obj.getName(), old + count);
				} else {
					plugin.getQuester(player.getUniqueId()).getQuestData(quest).customObjectiveCounts.put(obj.getName(), count);
				}
				int index = -1;
				for (int i = 0; i < quester.getCurrentStage(quest).customObjectives.size(); i++) {
					if (quester.getCurrentStage(quest).customObjectives.get(i).getName().equals(obj.getName())) {
						index = i;
						break;
					}
				}
				if (index > -1) {
					if (quester.getQuestData(quest).customObjectiveCounts.get(obj.getName()) >= quester.getCurrentStage(quest).customObjectiveCounts.get(index)) {
						quester.finishObjective(quest, "customObj", null, null, null, null, null, null, null, null, null, obj);
						
						// Multiplayer
						if (quest.getOptions().getShareProgressLevel() == 2) {
							List<Quester> mq = quester.getMultiplayerQuestersByQuest(quest);
							if (mq != null) {
								for (Quester q : mq) {
									if (q.getCurrentQuests().containsKey(quest)) {
										q.finishObjective(quest, "customObj", null, null, null, null, null, null, null, null, null, obj);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}