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

package me.blackvein.quests.prompts;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class MobsPrompt extends FixedSetPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;
    private final QuestFactory questFactory;

    public MobsPrompt(Quests plugin, int stageNum, QuestFactory qf) {
        super("1", "2", "3", "4", "5");
        this.plugin = plugin;
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
        this.questFactory = qf;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        context.setSessionData(pref, Boolean.TRUE);
        String text = ChatColor.AQUA + "- " + Lang.get("stageEditorMobs") + " -\n";
        if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorKillMobs") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "1 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorKillMobs") + "\n";
            LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_TYPES);
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS);
            if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                for (int i = 0; i < mobs.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA 
                            + MiscUtil.getPrettyMobName(MiscUtil.getProperMobType(mobs.get(i))) + ChatColor.GRAY + " x " 
                            + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                }
            } else {
                LinkedList<String> locs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                LinkedList<Integer> radii 
                        = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
                LinkedList<String> names 
                        = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);
                for (int i = 0; i < mobs.size(); i++) {
                    String msg = Lang.get("blocksWithin");
                    msg = msg.replaceAll("<amount>", ChatColor.DARK_PURPLE + "" + radii.get(i) + ChatColor.GRAY);
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE 
                            + MiscUtil.getPrettyMobName(MiscUtil.getProperMobType(mobs.get(i))) + ChatColor.GRAY + " x " 
                            + ChatColor.DARK_AQUA + amnts.get(i) + ChatColor.GRAY + msg + ChatColor.YELLOW 
                            + names.get(i) + " (" + locs.get(i) + ")\n";
                }
            }
        }
        if (context.getSessionData(pref + CK.S_FISH) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorCatchFish") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            Integer fish = (Integer) context.getSessionData(pref + CK.S_FISH);
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorCatchFish") + " " + ChatColor.GRAY + "(" + ChatColor.AQUA + fish 
                    + " " + Lang.get("stageEditorFish") + ChatColor.GRAY + ")\n";
        }
        if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorTameMobs") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "3 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorTameMobs") + "\n";
            LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_TAME_TYPES);
            LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS);
            for (int i = 0; i < mobs.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + mobs.get(i) + ChatColor.GRAY + " x " 
                        + ChatColor.AQUA + amounts.get(i) + "\n";
            }
        }
        if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorShearSheep") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "4 " + ChatColor.RESET + ChatColor.LIGHT_PURPLE 
                    + "- " + Lang.get("stageEditorShearSheep") + "\n";
            LinkedList<String> colors = (LinkedList<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS);
            LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS);
            for (int i = 0; i < colors.size(); i++) {
                text += ChatColor.GRAY + "     - " + ChatColor.BLUE + colors.get(i) + ChatColor.GRAY + " x " 
                        + ChatColor.AQUA + amounts.get(i) + "\n";
            }
        }
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "5 " + ChatColor.RESET + ChatColor.DARK_PURPLE + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("1")) {
            return new MobListPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            return new FishPrompt();
        } else if (input.equalsIgnoreCase("3")) {
            return new TameListPrompt();
        } else if (input.equalsIgnoreCase("4")) {
            return new ShearListPrompt();
        }
        try {
            return new StageMainPrompt(plugin, stageNum, questFactory);
        } catch (Exception e) {
            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
            return Prompt.END_OF_CONVERSATION;
        }
    }
    

    private class MobListPrompt extends FixedSetPrompt {

        public MobListPrompt() {
            super("1", "2", "3", "4", "5", "6", "7");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorKillMobs") + " -\n";
            if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetMobTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetMobAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetKillLocations") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetKillLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.GRAY + " - " 
                        + Lang.get("stageEditorSetKillLocationNames") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetMobTypes") + "\n";
                for (String s : getMobTypes(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(pref + CK.S_MOB_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetMobAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetMobAmounts") + "\n";
                    for (Integer i : getMobAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetKillLocations") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetKillLocations") + "\n";
                    for (String s : getKillLocations(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD  + "4" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetKillLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD  + "4" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetKillLocationRadii") + "\n";
                    for (int i : getKillRadii(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD  + "5" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetKillLocationNames") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD  + "5" + ChatColor.RESET + ChatColor.BLUE + " - " 
                            + Lang.get("stageEditorSetKillLocationNames") + "\n";
                    for (String s : getKillLocationNames(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new MobTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoMobTypes"));
                    return new MobListPrompt();
                } else {
                    return new MobAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoMobTypes"));
                    return new MobListPrompt();
                } else {
                    Map<UUID, Block> temp = questFactory.getSelectedKillLocations();
                    temp.put(((Player) context.getForWhom()).getUniqueId(), null);
                    questFactory.setSelectedKillLocations(temp);
                    return new MobLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoKillLocations"));
                    return new MobListPrompt();
                } else {
                    return new MobRadiiPrompt();
                }
            } else if (input.equalsIgnoreCase("5")) {
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoKillLocations"));
                    return new MobListPrompt();
                } else {
                    return new MobLocationNamesPrompt();
                }
            } else if (input.equalsIgnoreCase("6")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_MOB_TYPES, null);
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, null);
                return new MobListPrompt();
            } else if (input.equalsIgnoreCase("7")) {
                int one;
                int two;
                int three;
                int four;
                int five;
                if (context.getSessionData(pref + CK.S_MOB_TYPES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_MOB_TYPES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_MOB_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null) {
                    three = ((List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS)).size();
                } else {
                    three = 0;
                }
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) != null) {
                    four = ((List<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS)).size();
                } else {
                    four = 0;
                }
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) != null) {
                    five = ((List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES)).size();
                } else {
                    five = 0;
                }
                if (one == two) {
                    if (three != 0 || four != 0 || five != 0) {
                        if (two == three && three == four && four == five) {
                            return new StageMainPrompt(plugin, stageNum, questFactory);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                            return new MobListPrompt();
                        }
                    } else {
                        return new StageMainPrompt(plugin, stageNum, questFactory);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new MobListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getMobTypes(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_MOB_TYPES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getMobAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS);
        }

        @SuppressWarnings("unchecked")
        private List<String> getKillLocations(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getKillRadii(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
        }

        @SuppressWarnings("unchecked")
        private List<String> getKillLocationNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);
        }
    }

    private class MobTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String mobs = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorMobsTitle") + "\n";
            LinkedList<EntityType> mobArr = new LinkedList<EntityType>(Arrays.asList(EntityType.values()));
            LinkedList<EntityType> toRemove = new LinkedList<EntityType>();
            for (int i = 0; i < mobArr.size(); i++) {
                final EntityType type = mobArr.get(i);
                if (type.isAlive() == false || type.name().equals("PLAYER")) {
                    toRemove.add(type);
                }
            }
            mobArr.removeAll(toRemove);
            for (int i = 0; i < mobArr.size(); i++) {
                if (i < (mobArr.size() - 1)) {
                    mobs += MiscUtil.snakeCaseToUpperCamelCase(mobArr.get(i).name()) + ", ";
                } else {
                    mobs += MiscUtil.snakeCaseToUpperCamelCase(mobArr.get(i).name()) + "\n";
                }
            }
            return mobs + ChatColor.YELLOW + Lang.get("stageEditorMobsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    if (MiscUtil.getProperMobType(s) != null) {
                        mobTypes.add(s);
                        context.setSessionData(pref + CK.S_MOB_TYPES, mobTypes);
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidMob"));
                        return new MobTypesPrompt();
                    }
                }
            }
            return new MobListPrompt();
        }
    }

    private class MobAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorMobAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {
                    try {
                        int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new MobAmountsPrompt();
                        }
                        mobAmounts.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                        return new MobAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, mobAmounts);
            }
            return new MobListPrompt();
        }
    }

    private class MobLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorMobLocationPrompt");
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                Block block = questFactory.getSelectedKillLocations().get(player.getUniqueId());
                if (block != null) {
                    Location loc = block.getLocation();
                    LinkedList<String> locs;
                    if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }
                    locs.add(ConfigUtil.getLocationInfo(loc));
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, locs);
                    Map<UUID, Block> temp = questFactory.getSelectedKillLocations();
                    temp.remove(player.getUniqueId());
                    questFactory.setSelectedKillLocations(temp);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("stageEditorNoBlock"));
                    return new MobLocationPrompt();
                }
                return new MobListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                Map<UUID, Block> temp = questFactory.getSelectedKillLocations();
                temp.remove(player.getUniqueId());
                questFactory.setSelectedKillLocations(temp);
                return new MobListPrompt();
            } else {
                return new MobLocationPrompt();
            }
        }
    }

    private class MobRadiiPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorMobLocationRadiiPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<Integer> radii = new LinkedList<Integer>();
                for (String s : input.split(" ")) {
                    try {
                        int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new MobRadiiPrompt();
                        }
                        radii.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidItemName"));
                        return new MobRadiiPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, radii);
            }
            return new MobListPrompt();
        }
    }

    private class MobLocationNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorMobLocationNamesPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> locNames = new LinkedList<String>();
                locNames.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, locNames);
            }
            return new MobListPrompt();
        }
    }
    
    private class FishPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorCatchFishPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
                        return new FishPrompt();
                    } else if (i > 0) {
                        context.setSessionData(pref + CK.S_FISH, i);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new FishPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_FISH, null);
            }
            return new StageMainPrompt(plugin, stageNum, questFactory);
        }
    }
    
    private class TameListPrompt extends FixedSetPrompt {

        public TameListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorTameMobs") + " -\n";
            if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetMobTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY 
                        + Lang.get("stageEditorSetTameAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetMobTypes") + "\n";
                for (String s : getTameTypes(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(pref + CK.S_TAME_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetTameAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetTameAmounts") + "\n";
                    for (Integer i : getTameAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new TameTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoMobTypes"));
                    return new TameListPrompt();
                } else {
                    return new TameAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_TAME_TYPES, null);
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, null);
                return new TameListPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_TAME_TYPES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_TAME_TYPES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_TAME_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new StageMainPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new TameListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getTameTypes(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_TAME_TYPES);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getTameAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS);
        }
    }

    private class TameTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String mobs = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorMobsTitle") + "\n";
            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {
                final EntityType type = mobArr[i];
                if (type.isAlive() == false || Tameable.class.isAssignableFrom(type.getEntityClass()) == false) {
                    continue;
                }
                mobs += MiscUtil.snakeCaseToUpperCamelCase(mobArr[i].name()) + ", ";
            }
            mobs = mobs.substring(0, mobs.length() - 2) + "\n";
            return mobs + ChatColor.YELLOW + Lang.get("stageEditorMobsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    if (MiscUtil.getProperMobType(s) != null) {
                        final EntityType type = MiscUtil.getProperMobType(s);
                        if (type.isAlive() || Tameable.class.isAssignableFrom(type.getEntityClass())) {
                            mobTypes.add(MiscUtil.getPrettyMobName(type));
                            context.setSessionData(pref + CK.S_TAME_TYPES, mobTypes);
                        } else {
                            player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                    + Lang.get("stageEditorInvalidMob"));
                            return new TameTypesPrompt();
                        }
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidMob"));
                        return new TameTypesPrompt();
                    }
                }
            }
            return new TameListPrompt();
        }
    }

    private class TameAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorTameAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {
                    try {
                        int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new TameAmountsPrompt();
                        }
                        mobAmounts.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new TameAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, mobAmounts);
            }
            return new TameListPrompt();
        }
    }

    private class ShearListPrompt extends FixedSetPrompt {

        public ShearListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "- " + Lang.get("stageEditorShearSheep") + " -\n";
            if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetShearColors") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.GRAY 
                        + Lang.get("stageEditorSetShearAmounts") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.RED + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorSetShearColors") + "\n";
                for (String s : getShearColors(context)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetShearAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                            + Lang.get("stageEditorSetShearAmounts") + "\n";
                    for (Integer i : getShearAmounts(context)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                }
                text += ChatColor.RED + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new ShearColorsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorNoColors"));
                    return new ShearListPrompt();
                } else {
                    return new ShearAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_SHEAR_COLORS, null);
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, null);
                return new ShearListPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                int one;
                int two;
                if (context.getSessionData(pref + CK.S_SHEAR_COLORS) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new StageMainPrompt(plugin, stageNum, questFactory);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new ShearListPrompt();
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<String> getShearColors(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS);
        }

        @SuppressWarnings("unchecked")
        private List<Integer> getShearAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS);
        }
    }

    private class ShearColorsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String cols = ChatColor.LIGHT_PURPLE + "- " + Lang.get("stageEditorColors") + " - \n";
            final DyeColor[] colArr = DyeColor.values();
            for (int i = 0; i < colArr.length; i++) {
                if (i < (colArr.length - 1)) {
                    cols += MiscUtil.getDyeString(colArr[i]) + ", ";
                } else {
                    cols += MiscUtil.getDyeString(colArr[i]) + "\n";
                }
            }
            return cols + ChatColor.YELLOW + Lang.get("stageEditorShearColorsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> colors = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    if (MiscUtil.getDyeColor(s) != null) {
                        colors.add(MiscUtil.getDyeString(MiscUtil.getDyeColor(s)));
                        context.setSessionData(pref + CK.S_SHEAR_COLORS, colors);
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidDye"));
                        return new ShearColorsPrompt();
                    }
                }
            }
            return new ShearListPrompt();
        }
    }

    private class ShearAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("stageEditorShearAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<Integer> shearAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {
                    try {
                        int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new ShearAmountsPrompt();
                        }
                        shearAmounts.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                        return new ShearAmountsPrompt();
                    }
                }
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, shearAmounts);
            }
            return new ShearListPrompt();
        }
    }
}
