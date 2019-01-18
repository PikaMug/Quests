/*******************************************************************************************************
 * Continued by FlyingPikachu/HappyPikachu with permission from _Blackvein_. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class CustomObjective implements Listener {

	private Quests plugin = Quests.getPlugin(Quests.class);
	private String name = null;
	private String author = null;
	private Map<String, Object> data = new HashMap<String, Object>();
	private Map<String, String> descriptions = new HashMap<String, String>();
	private String countPrompt = "null";
	private String display = "null";
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
		
	public Map<String, Object> getData() {
		return data;
	}
	
	/**
	 * Add a detailed piece of datum to the data map
	 * 
	 * @param name
	 * @param o
	 */
	public void addDatum(String name, Object o) {
		if (o == null) {
			data.put(name, o);
		} else {
			data.put(name, null);
		}
	}
	/**
	 * Add a blank piece of datum to the data map
	 * 
	 * @param name
	 */
	public void addDatum(String name) {
		data.put(name, null);
	}
	
	/**
	 * Add a detailed piece of datum to the data map
	 * 
	 * @param name
	 * @deprecated use addDatum(name, o)
	 */
	public void addData(String name, Object o) {
		addDatum(name, o);
	}

	/**
	 * Add a blank piece of datum to the data map
	 * 
	 * @param name
	 * @deprecated use addDatum(name)
	 */
	public void addData(String name) {
		addDatum(name);
	}
	
	public Map<String, String> getDescriptions() {
		return descriptions;
	}

	public void addDescription(String data, String description) {
		descriptions.put(data, description);
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
			if (currentStage == null)
				return null;
			int index = -1;
			int tempIndex = 0;
			for (me.blackvein.quests.CustomObjective co : currentStage.customObjectives) {
				if (co.getName().equals(obj.getName())) {
					index = tempIndex;
					break;
				}
				tempIndex++;
			}
			if (index > -1) {
				return currentStage.customObjectiveData.get(index);
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
					}
				}
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CustomObjective) {
			CustomObjective other = (CustomObjective) o;
			if (other.name.equals(name) == false) {
				return false;
			}
			if (other.author.equals(name) == false) {
				return false;
			}
			for (String s : other.getData().keySet()) {
				if (getData().containsKey(s) == false) {
					return false;
				}
			}
			for (Object val : other.getData().values()) {
				if (getData().containsValue(val) == false) {
					return false;
				}
			}
			for (String s : other.descriptions.keySet()) {
				if (descriptions.containsKey(s) == false) {
					return false;
				}
			}
			for (String s : other.descriptions.values()) {
				if (descriptions.containsValue(s) == false) {
					return false;
				}
			}
			if (other.countPrompt.equals(countPrompt) == false) {
				return false;
			}
			if (other.display.equals(display) == false) {
				return false;
			}
			if (other.showCount != showCount) {
				return false;
			}
			if (other.count != count) {
				return false;
			}
			return true;
		}
		return false;
	}
}