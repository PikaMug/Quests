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

package me.blackvein.quests.util;

import java.lang.reflect.InvocationTargetException;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class LocaleQuery {
	   private static Class<?> craftMagicNumbers = null;
	   private final Quests plugin;
	   
	   public LocaleQuery(Quests plugin){
		   this.plugin = plugin;
		   setup();
	   }
	   
	   public void sendMessage(Player player, String message, Material material) {
		   if (plugin.translateItems) {
			   String key = queryByType(material);
			   if (Quests.bukkitVersion <= 1122) {
				   if (key.startsWith("tile.") || key.startsWith("item.")) {
					   key = key + ".name";
				   }
			   }
			   if (key != null) {
				   String msg = message.replace("<item>", "\",{\"translate\":\"" + key + "\"},\"");
				   player.chat("/tellraw " + player.getName() + " [\"" + msg + "\"]");
				   return;
			   }
		   }
		   player.sendMessage(message.replace("<item>", Quester.prettyItemString(material.name())));
	   }
	   
	   public void sendMessage(Player player, String message, Material material, Enchantment enchantment) {
		   if (plugin.translateItems) {
			   String key = queryByType(material);
			   if (Quests.bukkitVersion <= 1122) {
				   if (key.startsWith("tile.") || key.startsWith("item.")) {
					   key = key + ".name";
				   }
			   }
			   String key2 = "";
			   if (Quests.bukkitVersion <= 1122) {
				   key2 = "enchantment." + MiscUtil.getProperEnchantmentName(enchantment).replace("_", ".")
						   .replace("environmental", "all").replace("protection", "protect");
			   } else {
				   key2 = "enchantment.minecraft." + enchantment.toString().toLowerCase();
			   }
			   if (key != null && !key.equals("")) {
				   String msg = message.replace("<item>", "\",{\"translate\":\"" + key + "\"},\"")
						   .replace("<enchantment>", "\",{\"translate\":\"" + key2 + "\"},\"");
				   player.chat("/tellraw " + player.getName() + " [\"" + msg + "\"]");
				   return;
			   }
		   }
		   player.sendMessage(message.replace("<item>", Quester.prettyItemString(material.name()))
				   .replace("<enchantment>", Quester.prettyEnchantmentString(enchantment)));
	   }
	   
	   public void sendMessage(Player player, String message, EntityType type) {
		   if (plugin.translateItems) {
			   String key = "";
			   if (Quests.bukkitVersion <= 1122) {
				   key = "entity." + MiscUtil.getProperMobName(type) + ".name";
			   } else {
				   key = "entity.minecraft." + type.toString().toLowerCase();
			   }
			   if (!key.equals("")) {
				   String msg = message.replace("<mob>", "\",{\"translate\":\"" + key + "\"},\"");
				   player.chat("/tellraw " + player.getName() + " [\"" + msg + "\"]");
				   return;
			   }
		   }
		   player.sendMessage(message.replace("<mob>", Quester.prettyMobString(type)));
	   }
	   
	   /**
	    * Creates a new LocaleQuery of the specified material
	    * @param material the item with the material
	    * @return the new LocaleQuery
	    * @throws IllegalArgumentException if an item with that material could not be found
	    */
	   public String queryByType(Material material) throws IllegalArgumentException{
	       try {
	    	   Object item = MethodUtils.invokeExactStaticMethod(craftMagicNumbers,"getItem", material);
	    	   if (item == null) {
	    		   throw new IllegalArgumentException("An item with that material could not be found! (Perhaps you have specified a block?)");
	    	   }
	        	
	    	   String name = (String) MethodUtils.invokeExactMethod(item, "getName");
	    	   return name;
	       } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
	    	   e.printStackTrace();
	       }
	       return null;
	   }
	   
	   public void setup() {
		   String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	       try {
	           craftMagicNumbers = Class.forName("org.bukkit.craftbukkit.{v}.util.CraftMagicNumbers".replace("{v}", version));
	       } catch (ClassNotFoundException e) {
	           e.printStackTrace();
	       }
	   }
}
