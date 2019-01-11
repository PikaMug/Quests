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

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.blackvein.quests.nms.v1_10_R1.Eff_1_10_R1;
import me.blackvein.quests.nms.v1_11_R1.Eff_1_11_R1;
import me.blackvein.quests.nms.v1_12_R1.Eff_1_12_R1;
import me.blackvein.quests.nms.v1_13_R1.Eff_1_13_R1;
import me.blackvein.quests.nms.v1_13_R2.Eff_1_13_R2;
import me.blackvein.quests.nms.v1_8_R1.Eff_1_8_R1;
import me.blackvein.quests.nms.v1_8_R2.Eff_1_8_R2;
import me.blackvein.quests.nms.v1_8_R3.Eff_1_8_R3;
import me.blackvein.quests.nms.v1_9_R1.Eff_1_9_R1;
import me.blackvein.quests.nms.v1_9_R2.Eff_1_9_R2;
import net.citizensnpcs.api.npc.NPC;

public class NpcEffectThread implements Runnable {

	final Quests plugin;

	public NpcEffectThread(Quests quests) {
		plugin = quests;
	}

	@Override
	public void run() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			Quester quester = plugin.getQuester(player.getUniqueId());
			List<Entity> nearby = player.getNearbyEntities(32.0, 32.0, 32.0);
			if (nearby.isEmpty() == false) {
				for (Entity e : nearby) {
					if (plugin.getDependencies().getCitizens() != null) {
						if (plugin.getDependencies().getCitizens().getNPCRegistry().isNPC(e)) {
							NPC npc = plugin.getDependencies().getCitizens().getNPCRegistry().getNPC(e);
							if (plugin.hasQuest(npc, quester)) {
								showEffect(player, npc, plugin.getSettings().getEffect());
							} else if (plugin.hasCompletedRedoableQuest(npc, quester)) {
								showEffect(player, npc, plugin.getSettings().getRedoEffect());
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Display a particle effect above an NPC one time
	 * @param player Target player to let view the effect
	 * @param npc Target NPC to place the effect above
	 * @param effectType Value of EnumParticle such as NOTE or SMOKE
	 */
	public void showEffect(Player player, NPC npc, String effectType) {
		try {
			if (plugin.getDetectedBukkitVersion().contains("1.13.2")
					|| plugin.getDetectedBukkitVersion().contains("1.13.1")) {
				showEffect_1_13_R2(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.13")) {
				showEffect_1_13_R1(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.12")) {
				showEffect_1_12_R1(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.11")) {
				showEffect_1_11_R1(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.10")) {
				showEffect_1_10_R1(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.9.4")) {
				showEffect_1_9_R2(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.9")) {
				showEffect_1_9_R1(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.8.9")
					|| plugin.getDetectedBukkitVersion().contains("1.8.8")
					|| plugin.getDetectedBukkitVersion().contains("1.8.7")
					|| plugin.getDetectedBukkitVersion().contains("1.8.6")
					|| plugin.getDetectedBukkitVersion().contains("1.8.5")
					|| plugin.getDetectedBukkitVersion().contains("1.8.4")) {
				showEffect_1_8_R3(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.8.3")) {
				showEffect_1_8_R2(player, npc, effectType);
			} else if (plugin.getDetectedBukkitVersion().contains("1.8")) {
				showEffect_1_8_R1(player, npc, effectType);
			}
		} catch (Exception e) {
			plugin.getLogger().severe("Bukkit version detected as " + plugin.getDetectedBukkitVersion());
			e.printStackTrace();
		}
	}
	
	private void showEffect_1_13_R2(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_13_R2.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_13_R2.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_13_R2.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_13_R2.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_13_R2.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_13_R2.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_13_R2.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_13_R2.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_13_R2.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_13_R2.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_13_R2.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_13_R2.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_13_R2.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}
	
	private void showEffect_1_13_R1(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_13_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_13_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_13_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_13_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_13_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_13_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_13_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_13_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_13_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_13_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_13_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_13_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_13_R1.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_12_R1(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_12_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_12_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_12_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_12_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_12_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_12_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_12_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_12_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_12_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_12_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_12_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_12_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_12_R1.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_11_R1(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_11_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_11_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_11_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_11_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_11_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_11_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_11_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_11_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_11_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_11_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_11_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_11_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_11_R1.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_10_R1(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_10_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_10_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_10_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_10_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_10_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_10_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_10_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_10_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_10_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_10_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_10_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_10_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_10_R1.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_9_R2(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_9_R2.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_9_R2.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_9_R2.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_9_R2.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_9_R2.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_9_R2.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_9_R2.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_9_R2.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_9_R2.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_9_R2.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_9_R2.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_9_R2.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_9_R2.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_9_R1(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_9_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_9_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_9_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_9_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_9_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_9_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_9_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_9_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_9_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_9_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_9_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_9_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_9_R1.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_8_R3(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_8_R3.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_8_R3.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_8_R3.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_8_R3.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_8_R3.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R3.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_8_R3.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_8_R3.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_8_R3.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R3.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R3.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_8_R3.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_8_R3.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_8_R2(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_8_R2.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_8_R2.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_8_R2.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_8_R2.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_8_R2.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R2.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_8_R2.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_8_R2.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_8_R2.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R2.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R2.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_8_R2.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_8_R2.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}

	private void showEffect_1_8_R1(Player player, NPC npc, String effectType) {
		Location eyeLoc = npc.getEntity().getLocation();
		eyeLoc.setY(eyeLoc.getY() + 1.5);
		if (effectType.equalsIgnoreCase("enchant")) {
			try {
				Eff_1_8_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("crit")) {
			try {
				Eff_1_8_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("spell")) {
			try {
				Eff_1_8_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("magiccrit")) {
			try {
				Eff_1_8_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("mobspell")) {
			try {
				Eff_1_8_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("note")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("portal")) {
			try {
				Eff_1_8_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("dust")) {
			try {
				Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
				Eff_1_8_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("witch")) {
			try {
				Eff_1_8_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("snowball")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("splash")) {
			try {
				Location old = eyeLoc;
				Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
				Eff_1_8_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (effectType.equalsIgnoreCase("smoke")) {
			try {
				Eff_1_8_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				Eff_1_8_R1.valueOf(effectType.toUpperCase()).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
			} catch (Exception e) {
				plugin.getLogger().info(effectType + " is not a valid effect name!");
			}
		}
	}
}
