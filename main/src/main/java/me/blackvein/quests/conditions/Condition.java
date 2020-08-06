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

package me.blackvein.quests.conditions;

import java.util.LinkedList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.blackvein.quests.Quest;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.MiscUtil;

public class Condition {

    @SuppressWarnings("unused")
    private final Quests plugin;
    private String name = "";
    private boolean failQuest = false;
    private LinkedList<ItemStack> itemsWhileHoldingMainHand = new LinkedList<ItemStack>();
    private LinkedList<String> worldsWhileStayingWithin = new LinkedList<String>();
    private LinkedList<String> biomesWhileStayingWithin = new LinkedList<String>();

    public Condition(final Quests plugin) {
        this.plugin = plugin;
    }

    public String getName() {
        return name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public boolean isFailQuest() {
        return failQuest;
    }

    public void setFailQuest(final boolean failQuest) {
        this.failQuest = failQuest;
    }

    public LinkedList<ItemStack> getItemsWhileHoldingMainHand() {
        return itemsWhileHoldingMainHand;
    }

    public void setItemsWhileHoldingMainHand(final LinkedList<ItemStack> itemsWhileHoldingMainHand) {
        this.itemsWhileHoldingMainHand = itemsWhileHoldingMainHand;
    }
    
    public LinkedList<String> getWorldsWhileStayingWithin() {
        return worldsWhileStayingWithin;
    }
    
    public void setWorldsWhileStayingWithin(final LinkedList<String> worldsWhileStayingWithin) {
        this.worldsWhileStayingWithin = worldsWhileStayingWithin;
    }
    
    public LinkedList<String> getBiomesWhileStayingWithin() {
        return biomesWhileStayingWithin;
    }
    
    public void setBiomesWhileStayingWithin(final LinkedList<String> biomesWhileStayingWithin) {
        this.biomesWhileStayingWithin = biomesWhileStayingWithin;
    }

    @SuppressWarnings("deprecation")
    public boolean check(final Quester quester, final Quest quest) {
        final Player player = quester.getPlayer();
        if (itemsWhileHoldingMainHand.isEmpty() == false) {
            for (final ItemStack is : itemsWhileHoldingMainHand) {
                if (ItemUtil.compareItems(player.getItemInHand(), is, true, true) == 0) {
                    return true;
                } else {
                    System.out.println("DEBUG: condition item does not match with code= " 
                            + ItemUtil.compareItems(player.getItemInHand(), is, true, true));
                }
            }
        } else if (worldsWhileStayingWithin.isEmpty() == false) {
            for (final String w : worldsWhileStayingWithin) {
                if (player.getWorld().getName().equalsIgnoreCase(w)) {
                    return true;
                } else {
                    System.out.println("DEBUG: condition world does not match for= " + w);
                }
            }
        } else if (biomesWhileStayingWithin.isEmpty() == false) {
            for (final String b : biomesWhileStayingWithin) {
                if (player.getWorld().getBiome(player.getLocation().getBlockX(), player.getLocation().getBlockZ())
                        .name().equalsIgnoreCase(MiscUtil.getProperBiome(b).name())) {
                    return true;
                } else {
                    System.out.println("DEBUG: condition biome does not match for= " + MiscUtil.getProperBiome(b));
                }
            }
        }
        return false;
    }
}
    
