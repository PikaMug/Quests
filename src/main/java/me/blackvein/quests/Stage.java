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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class Stage {

	LinkedList<ItemStack> blocksToBreak = new LinkedList<ItemStack>();
	LinkedList<ItemStack> blocksToDamage = new LinkedList<ItemStack>();
	LinkedList<ItemStack> blocksToPlace = new LinkedList<ItemStack>();
	LinkedList<ItemStack> blocksToUse = new LinkedList<ItemStack>();
	LinkedList<ItemStack> blocksToCut = new LinkedList<ItemStack>();
	Integer fishToCatch;
	Integer playersToKill;
	Map<Map<Enchantment, Material>, Integer> itemsToEnchant = new HashMap<Map<Enchantment, Material>, Integer>();
	LinkedList<EntityType> mobsToKill = new LinkedList<EntityType>();
	LinkedList<Integer> mobNumToKill = new LinkedList<Integer>();
	LinkedList<Location> locationsToKillWithin = new LinkedList<Location>();
	LinkedList<Integer> radiiToKillWithin = new LinkedList<Integer>();
	LinkedList<String> areaNames = new LinkedList<String>();
	LinkedList<ItemStack> itemsToDeliver = new LinkedList<ItemStack>();
	LinkedList<Integer> itemDeliveryTargets = new LinkedList<Integer>() {

		private static final long serialVersionUID = -2774443496142382127L;

		@Override
		public boolean equals(Object o) {
			if (o instanceof LinkedList) {
				@SuppressWarnings("unchecked")
				LinkedList<Integer> otherList = (LinkedList<Integer>) o;
				for (Integer i : this) {
					Integer other = otherList.get(this.indexOf(i));
					if (!other.equals(i)) {
						return false;
					}
				}
			}
			return true;
		}
	};
	public LinkedList<String> deliverMessages = new LinkedList<String>();
	public LinkedList<Integer> citizensToInteract = new LinkedList<Integer>() {

		private static final long serialVersionUID = -4086855121042524435L;

		@Override
		public boolean equals(Object o) {
			if (o instanceof LinkedList) {
				@SuppressWarnings("unchecked")
				LinkedList<Integer> otherList = (LinkedList<Integer>) o;
				for (Integer i : this) {
					Integer other = otherList.get(this.indexOf(i));
					if (!other.equals(i)) {
						return false;
					}
				}
			}
			return true;
		}
	};
	public LinkedList<Integer> citizensToKill = new LinkedList<Integer>() {

		private static final long serialVersionUID = 7705964814014176415L;

		@Override
		public boolean equals(Object o) {
			if (o instanceof LinkedList) {
				@SuppressWarnings("unchecked")
				LinkedList<Integer> otherList = (LinkedList<Integer>) o;
				for (Integer i : this) {
					Integer other = otherList.get(this.indexOf(i));
					if (!other.equals(i)) {
						return false;
					}
				}
			}
			return true;
		}
	};
	public LinkedList<Integer> citizenNumToKill = new LinkedList<Integer>();
	public LinkedList<Location> locationsToReach = new LinkedList<Location>();
	public LinkedList<Integer> radiiToReachWithin = new LinkedList<Integer>();
	public LinkedList<World> worldsToReachWithin = new LinkedList<World>();
	public LinkedList<String> locationNames = new LinkedList<String>();
	public Map<EntityType, Integer> mobsToTame = new EnumMap<EntityType, Integer>(EntityType.class);
	public Map<DyeColor, Integer> sheepToShear = new EnumMap<DyeColor, Integer>(DyeColor.class);
	public Map<EnumMap<Material, Integer>, Boolean> itemsToCraft = new HashMap<EnumMap<Material, Integer>, Boolean>();
	public LinkedList<CustomObjective> customObjectives = new LinkedList<CustomObjective>();
	public LinkedList<Integer> customObjectiveCounts = new LinkedList<Integer>();
	public LinkedList<String> customObjectiveDisplays = new LinkedList<String>();
	public LinkedList<Map<String, Object>> customObjectiveData = new LinkedList<Map<String, Object>>();
	public LinkedList<String> passwordDisplays = new LinkedList<String>();
	public LinkedList<LinkedList<String>> passwordPhrases = new LinkedList<LinkedList<String>>();
	public String script;
	public Event startEvent = null;
	public Event deathEvent = null;
	public Map<String, Event> chatEvents = new HashMap<String, Event>();
	public Map<String, Event> commandEvents = new HashMap<String, Event>();
	public Event disconnectEvent = null;
	public Event finishEvent = null;
	public long delay = -1;
	public String delayMessage = null;
	public String completeMessage = null;
	public String startMessage = null;
	public String objectiveOverride = null;
	
	/**
	 * Check if stage has at least one objective EXCLUDING start/complete message
	 * 
	 * @return true if stage contains an objective
	 */
	public boolean hasObjective() {
		if (blocksToBreak.isEmpty() == false) { return true; }
		if (blocksToDamage.isEmpty() == false) { return true; }
		if (blocksToPlace.isEmpty() == false) { return true; }
		if (blocksToUse.isEmpty() == false) { return true; }
		if (blocksToCut.isEmpty() == false) { return true; }
		if (fishToCatch != null) { return true; }
		if (playersToKill != null) { return true; }
		return false;
	}
}
