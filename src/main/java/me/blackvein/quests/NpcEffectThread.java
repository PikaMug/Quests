package me.blackvein.quests;

import java.util.List;

import me.blackvein.particles.Eff_1_7_R1;
import me.blackvein.particles.Eff_1_7_R3;
import me.blackvein.particles.Eff_1_7_R4;
import me.blackvein.particles.Eff_1_8_R1;
import me.blackvein.particles.Eff_1_8_R2;
import me.blackvein.particles.Eff_1_8_R3;
import me.blackvein.particles.Eff_1_9_R1;
import me.blackvein.particles.Eff_1_9_R2;
import me.blackvein.particles.Eff_1_10_R1;
import me.blackvein.particles.Eff_1_11_R1;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
                    if (plugin.citizens != null) {
                        if (plugin.citizens.getNPCRegistry().isNPC(e)) {

                            NPC npc = plugin.citizens.getNPCRegistry().getNPC(e);
                            if (plugin.hasQuest(npc, quester)) {
                                showEffect(player, npc);

                            }

                        }
                    }

                }

            }

        }

    }

    private static void showEffect(Player player, NPC npc) {

        if (Bukkit.getBukkitVersion().contains("1.7.2")) {
            showEffect_R1(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.7.9")) {
            showEffect_R3(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.7.10")) {
            showEffect_R4(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.8.4")
        		|| Bukkit.getBukkitVersion().contains("1.8.5")
        		|| Bukkit.getBukkitVersion().contains("1.8.6")
        		|| Bukkit.getBukkitVersion().contains("1.8.7")
        		|| Bukkit.getBukkitVersion().contains("1.8.8")) {
                showEffect_1_8_R3(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.8.3")) {
            showEffect_1_8_R2(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.8")) {
            showEffect_1_8_R1(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.9.4")) {
            showEffect_1_9_R2(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.9")) {
            showEffect_1_9_R1(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.10")) {
        	showEffect_1_10_R1(player, npc);
        } else if (Bukkit.getBukkitVersion().contains("1.11")) {
        	showEffect_1_11_R1(player, npc);
        }
    }
    private static void showEffect_1_11_R1(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);
    	
        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_11_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
            	Eff_1_11_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
            	Eff_1_11_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
            	Eff_1_11_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
            	Eff_1_11_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_11_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_11_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                Eff_1_11_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
            	Eff_1_11_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_11_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_11_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
            	Eff_1_11_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
        	
        	try {
        		Eff_1_11_R1.valueOf(Quests.effect).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
        	} catch (Exception e) {
        		Quests.getInstance().getLogger().info(Quests.effect + " is not a valid effect name!");
        		e.printStackTrace();
        	}
        }

    }
    
        private static void showEffect_1_10_R1(Player player, NPC npc) {
        	
        	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
        	Location eyeLoc = npc.getEntity().getLocation();
        	eyeLoc.setY(eyeLoc.getY() + 1.5);
        	
            if (Quests.effect.equalsIgnoreCase("enchant")) {

                try {
                    Eff_1_10_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("crit")) {

                try {
                	Eff_1_10_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("spell")) {

                try {
                	Eff_1_10_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

                try {
                	Eff_1_10_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

                try {
                	Eff_1_10_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("note")) {

                try {
                    Location old = eyeLoc;
                    Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                    Eff_1_10_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("portal")) {

                try {
                    Eff_1_10_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("dust")) {

                try {
                    Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                    Eff_1_10_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("witch")) {

                try {
                	Eff_1_10_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("snowball")) {

                try {
                    Location old = eyeLoc;
                    Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                    Eff_1_10_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("splash")) {

                try {
                    Location old = eyeLoc;
                    Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                    Eff_1_10_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (Quests.effect.equalsIgnoreCase("smoke")) {

                try {
                	Eff_1_10_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
            	
            	try {
            		Eff_1_10_R1.valueOf(Quests.effect).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            	} catch (Exception e) {
            		Quests.getInstance().getLogger().info(Quests.effect + " is not a valid effect name!");
            		e.printStackTrace();
            	}
            }

        }
        
private static void showEffect_1_9_R2(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);
    	
        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_9_R2.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
            	Eff_1_9_R2.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
            	Eff_1_9_R2.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
            	Eff_1_9_R2.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
            	Eff_1_9_R2.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_9_R2.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_9_R2.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                Eff_1_9_R2.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
            	Eff_1_9_R2.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_9_R2.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_9_R2.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
            	Eff_1_9_R2.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
        	
        	try {
        		Eff_1_9_R2.valueOf(Quests.effect).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
        	} catch (Exception e) {
        		Quests.getInstance().getLogger().info(Quests.effect + " is not a valid effect name!");
        		e.printStackTrace();
        	}
        }

    }

    private static void showEffect_1_9_R1(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);
    	
        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_9_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
            	Eff_1_9_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
            	Eff_1_9_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
            	Eff_1_9_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
            	Eff_1_9_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_9_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_9_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                Eff_1_9_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
            	Eff_1_9_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_9_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_9_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
            	Eff_1_9_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
        	
        	try {
        		Eff_1_9_R1.valueOf(Quests.effect).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
        	} catch (Exception e) {
        		Quests.getInstance().getLogger().info(Quests.effect + " is not a valid effect name!");
        		e.printStackTrace();
        	}
        }

    }
    
    private static void showEffect_1_8_R3(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);
    	
        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_8_R3.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
                Eff_1_8_R3.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
                Eff_1_8_R3.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
                Eff_1_8_R3.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
                Eff_1_8_R3.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R3.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_8_R3.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                Eff_1_8_R3.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
                Eff_1_8_R3.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R3.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R3.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
                Eff_1_8_R3.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
        	
        	try {
        		Eff_1_8_R3.valueOf(Quests.effect).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
        	} catch (Exception e) {
        		Quests.getInstance().getLogger().info(Quests.effect + " is not a valid effect name!");
        		e.printStackTrace();
        	}
        }

    }
    
    private static void showEffect_1_8_R2(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);
    	
        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_8_R2.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
                Eff_1_8_R2.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
                Eff_1_8_R2.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
                Eff_1_8_R2.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
                Eff_1_8_R2.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R2.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_8_R2.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                Eff_1_8_R2.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
                Eff_1_8_R2.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R2.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R2.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
                Eff_1_8_R2.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
        	
        	try {
        		Eff_1_8_R2.valueOf(Quests.effect).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
        	} catch (Exception e) {
        		Quests.getInstance().getLogger().info(Quests.effect + " is not a valid effect name!");
        		e.printStackTrace();
        	}
        }

    }
    
    private static void showEffect_1_8_R1(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);
    	
        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_8_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
                Eff_1_8_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
                Eff_1_8_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
                Eff_1_8_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
                Eff_1_8_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_8_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                Eff_1_8_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
                Eff_1_8_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_8_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
                Eff_1_8_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
        	
        	try {
        		Eff_1_8_R1.valueOf(Quests.effect).sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3, null);
        	} catch (Exception e) {
        		Quests.getInstance().getLogger().info(Quests.effect + " is not a valid effect name!");
        		e.printStackTrace();
        	}
        }

    }

    private static void showEffect_R4(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);
    	
        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_7_R4.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
                Eff_1_7_R4.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
                Eff_1_7_R4.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
                Eff_1_7_R4.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
                Eff_1_7_R4.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R4.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_7_R4.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location newLoc = new Location(player.getWorld(), eyeLoc.getX(), eyeLoc.getY() + (float) 0.5, eyeLoc.getZ());
                Eff_1_7_R4.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
                Eff_1_7_R4.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R4.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R4.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
                Eff_1_7_R4.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void showEffect_R3(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);

        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_7_R3.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
                Eff_1_7_R3.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
                Eff_1_7_R3.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
                Eff_1_7_R3.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
                Eff_1_7_R3.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R3.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_7_R3.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R3.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
                Eff_1_7_R3.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R3.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R3.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
                Eff_1_7_R3.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void showEffect_R1(Player player, NPC npc) {
    	
    	//Get and set eye location, because npc.getBukkitEntity() is deprecated.
    	Location eyeLoc = npc.getEntity().getLocation();
    	eyeLoc.setY(eyeLoc.getY() + 1.5);

        if (Quests.effect.equalsIgnoreCase("enchant")) {

            try {
                Eff_1_7_R1.ENCHANTMENT_TABLE.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 10);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("crit")) {

            try {
                Eff_1_7_R1.CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("spell")) {

            try {
                Eff_1_7_R1.INSTANT_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("magiccrit")) {

            try {
                Eff_1_7_R1.MAGIC_CRIT.sendToPlayer(player, eyeLoc, 0, 0, 0, (float) 0.35, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("mobspell")) {

            try {
                Eff_1_7_R1.MOB_SPELL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("note")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R1.NOTE.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("portal")) {

            try {
                Eff_1_7_R1.PORTAL.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 5);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("dust")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R1.RED_DUST.sendToPlayer(player, newLoc, 0, 0, 0, 1, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("witch")) {

            try {
                Eff_1_7_R1.WITCH_MAGIC.sendToPlayer(player, eyeLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("snowball")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R1.SNOWBALL_POOF.sendToPlayer(player, newLoc, 0, 0, 0, 1, 3);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("splash")) {

            try {
                Location old = eyeLoc;
                Location newLoc = new Location(player.getWorld(), old.getX(), old.getY() + (float) 0.5, old.getZ());
                Eff_1_7_R1.SPLASH.sendToPlayer(player, newLoc, 0, 0, 0, 1, 4);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Quests.effect.equalsIgnoreCase("smoke")) {

            try {
                Eff_1_7_R1.TOWN_AURA.sendToPlayer(player, eyeLoc, 0, 1, 0, 1, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
