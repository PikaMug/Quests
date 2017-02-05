package me.blackvein.quests.prompts;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.blackvein.quests.CustomObjective;
import me.blackvein.quests.util.ColorUtil;
import me.blackvein.quests.Event;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.citizensnpcs.api.CitizensPlugin;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateStagePrompt extends FixedSetPrompt implements ColorUtil {

    private final int stageNum;
    private final String pref;
    private final CitizensPlugin citizens;
    private final QuestFactory questFactory;

    public CreateStagePrompt(int stageNum, QuestFactory qf, CitizensPlugin cit) {

        super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26");
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
        this.citizens = cit;
        this.questFactory = qf;

    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {

        try {

            context.setSessionData(pref, Boolean.TRUE);

            String text = PINK + "- " + AQUA + (String) context.getSessionData(CK.Q_NAME) + PINK + " | " + Lang.get("stageEditorStage") + " " + PURPLE + stageNum + PINK + " -\n";

            if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                text += PINK + "" + BOLD + "1 " + RESET + PURPLE + "- " + Lang.get("stageEditorBreakBlocks") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "1 " + RESET + PURPLE + "- " + Lang.get("stageEditorBreakBlocks") + "\n";

                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_BREAK_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);

                for (int i = 0; i < names.size(); i++) {
                    text += GRAY + "    - " + BLUE + Quester.prettyItemString(names.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }

            }

            if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                text += PINK + "" + BOLD + "2 " + RESET + PURPLE + "- " + Lang.get("stageEditorDamageBlocks") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "2 " + RESET + PURPLE + "- " + Lang.get("stageEditorDamageBlocks") + "\n";

                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);

                for (int i = 0; i < names.size(); i++) {
                    text += GRAY + "    - " + BLUE + Quester.prettyItemString(names.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }

            }

            if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                text += PINK + "" + BOLD + "3 " + RESET + PURPLE + "- " + Lang.get("stageEditorPlaceBlocks") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "3 " + RESET + PURPLE + "- " + Lang.get("stageEditorPlaceBlocks") + "\n";

                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_PLACE_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);

                for (int i = 0; i < names.size(); i++) {
                    text += GRAY + "    - " + BLUE + Quester.prettyItemString(names.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }

            }

            if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                text += PINK + "" + BOLD + "4 " + RESET + PURPLE + "- " + Lang.get("stageEditorUseBlocks") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "4 " + RESET + PURPLE + "- " + Lang.get("stageEditorUseBlocks") + "\n";

                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_USE_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);

                for (int i = 0; i < names.size(); i++) {
                    text += GRAY + "    - " + BLUE + Quester.prettyItemString(names.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }
            }

            if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                text += PINK + "" + BOLD + "5 " + RESET + PURPLE + "- " + Lang.get("stageEditorCutBlocks") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "5 " + RESET + PURPLE + "- " + Lang.get("stageEditorCutBlocks") + "\n";

                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_CUT_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS);

                for (int i = 0; i < names.size(); i++) {
                    text += GRAY + "    - " + BLUE + Quester.prettyItemString(names.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }

            }

            if (context.getSessionData(pref + CK.S_FISH) == null) {
                text += PINK + "" + BOLD + "6 " + RESET + PURPLE + "- " + Lang.get("stageEditorCatchFish") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                Integer fish = (Integer) context.getSessionData(pref + CK.S_FISH);
                text += PINK + "" + BOLD + "6 " + RESET + PURPLE + "- " + Lang.get("stageEditorCatchFish") + " " + GRAY + "(" + AQUA + fish + " " + Lang.get("stageEditorFish") + GRAY + ")\n";
            }

            if (context.getSessionData(pref + CK.S_PLAYER_KILL) == null) {
                text += PINK + "" + BOLD + "7 " + RESET + PURPLE + "- " + Lang.get("stageEditorKillPlayers") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                Integer players = (Integer) context.getSessionData(pref + CK.S_PLAYER_KILL);
                text += PINK + "" + BOLD + "7 " + RESET + PURPLE + "- " + Lang.get("stageEditorKillPlayers") + GRAY + " (" + AQUA + players + " " + Lang.get("stageEditorPlayers") + GRAY + ")\n";
            }

            if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                text += PINK + "" + BOLD + "8 " + RESET + PURPLE + "- " + Lang.get("stageEditorEnchantItems") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "8 " + RESET + PURPLE + "- " + Lang.get("stageEditorEnchantItems") + "\n";

                LinkedList<String> enchants = (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES);
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS);

                for (int i = 0; i < enchants.size(); i++) {
                    text += GRAY + "    - " + BLUE + Quester.prettyItemString(names.get(i)) + GRAY + " " + Lang.get("with") + " " + AQUA + Quester.prettyString(enchants.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }

            }

            if (questFactory.quests.citizens != null) {

                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                    text += PINK + "" + BOLD + "9 " + RESET + PURPLE + "- " + Lang.get("stageEditorDeliverItems") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += PINK + "" + BOLD + "9 " + RESET + PURPLE + "- " + Lang.get("stageEditorDeliverItems") + "\n";

                    LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
                    LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);

                    for (int i = 0; i < npcs.size(); i++) {
                        text += GRAY + "    - " + BLUE + ItemUtil.getName(items.get(i)) + GRAY + " x " + AQUA + items.get(i).getAmount() + GRAY + " " + Lang.get("to") + " " + DARKAQUA + citizens.getNPCRegistry().getById(npcs.get(i)).getName() + "\n";
                    }

                }

            } else {
                text += GRAY + "" + BOLD + "9 " + RESET + GRAY + "- " + Lang.get("stageEditorDeliverItems") + GRAY + " (" + Lang.get("questCitNotInstalled") + ")\n";
            }

            if (questFactory.quests.citizens != null) {

                if (context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO) == null) {
                    text += PINK + "" + BOLD + "10 " + RESET + PURPLE + "- " + Lang.get("stageEditorTalkToNPCs") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += PINK + "" + BOLD + "10 " + RESET + PURPLE + "- " + Lang.get("stageEditorTalkToNPCs") + "\n";

                    LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_TALK_TO);

                    for (int i = 0; i < npcs.size(); i++) {
                        text += GRAY + "    - " + BLUE + citizens.getNPCRegistry().getById(npcs.get(i)).getName() + "\n";
                    }

                }

            } else {
                text += GRAY + "" + BOLD + "10 " + RESET + GRAY + "- " + Lang.get("stageEditorTalkToNPCs") + GRAY + " (" + Lang.get("questCitNotInstalled") + ")\n";
            }

            if (questFactory.quests.citizens != null) {

                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                    text += PINK + "" + BOLD + "11 " + RESET + PURPLE + "- " + Lang.get("stageEditorKillNPCs") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += PINK + "" + BOLD + "11 " + RESET + PURPLE + "- " + Lang.get("stageEditorKillNPCs") + "\n";

                    LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
                    LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);

                    for (int i = 0; i < npcs.size(); i++) {
                        text += GRAY + "    - " + BLUE + citizens.getNPCRegistry().getById(npcs.get(i)).getName() + GRAY + " x " + AQUA + amounts.get(i) + "\n";
                    }

                }

            } else {
                text += GRAY + "" + BOLD + "11 " + RESET + GRAY + "- " + Lang.get("stageEditorKillNPCs") + GRAY + " (" + Lang.get("questCitNotInstalled") + ")\n";
            }

            if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                text += PINK + "" + BOLD + "12 " + RESET + PURPLE + "- " + Lang.get("stageEditorKillMobs") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "12 " + RESET + PURPLE + "- " + Lang.get("stageEditorKillMobs") + "\n";

                LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_TYPES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS);

                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {

                    for (int i = 0; i < mobs.size(); i++) {
                        text += GRAY + "    - " + AQUA + Quester.prettyString(mobs.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                    }

                } else {

                    LinkedList<String> locs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                    LinkedList<Integer> radii = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
                    LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);

                    for (int i = 0; i < mobs.size(); i++) {
                        String msg = Lang.get("blocksWithin");
                        msg = msg.replaceAll("<amount>", PURPLE + "" + radii.get(i) + GRAY);
                        text += GRAY + "    - " + BLUE + Quester.prettyString(mobs.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + GRAY + msg + YELLOW + names.get(i) + " (" + locs.get(i) + ")\n";
                    }

                }

            }

            if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
                text += PINK + "" + BOLD + "13 " + RESET + PURPLE + "- " + Lang.get("stageEditorReachLocs") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "13 " + RESET + PURPLE + "- " + Lang.get("stageEditorReachLocs") + "\n";

                LinkedList<String> locations = (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS);
                LinkedList<Integer> radii = (LinkedList<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS);
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES);

                for (int i = 0; i < locations.size(); i++) {
                    text += GRAY + "    - " + Lang.get("stageEditorReachRadii1") + " " + BLUE + radii.get(i) + GRAY + " " + Lang.get("stageEditorReachRadii2") + " " + AQUA + names.get(i) + GRAY + " (" + DARKAQUA + locations.get(i) + GRAY + ")\n";
                }

            }

            if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                text += PINK + "" + BOLD + "14 " + RESET + PURPLE + "- " + Lang.get("stageEditorTameMobs") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {

                text += PINK + "" + BOLD + "14 " + RESET + PURPLE + "- " + Lang.get("stageEditorTameMobs") + "\n";

                LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_TAME_TYPES);
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS);

                for (int i = 0; i < mobs.size(); i++) {
                    text += GRAY + "    - " + BLUE + mobs.get(i) + GRAY + " x " + AQUA + amounts.get(i) + "\n";
                }

            }

            if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                text += PINK + "" + BOLD + "15 " + RESET + PURPLE + "- " + Lang.get("stageEditorShearSheep") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "15 " + RESET + PURPLE + "- " + Lang.get("stageEditorShearSheep") + "\n";

                LinkedList<String> colors = (LinkedList<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS);
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS);

                for (int i = 0; i < colors.size(); i++) {
                    text += GRAY + "    - " + BLUE + colors.get(i) + GRAY + " x " + AQUA + amounts.get(i) + "\n";
                }

            }

            text += PINK + "" + BOLD + "16 " + RESET + PURPLE + "- " + Lang.get("stageEditorEvents") + "\n";

            if (context.getSessionData(pref + CK.S_DELAY) == null) {
                text += PINK + "" + BOLD + "17 " + RESET + PURPLE + "- " + Lang.get("delay") + GRAY + "  (" + Lang.get("noneSet") + ")\n";
            } else {
                long time = (Long) context.getSessionData(pref + CK.S_DELAY);
                text += PINK + "" + BOLD + "17 " + RESET + PURPLE + "- " + Lang.get("delay") + GRAY + "(" + AQUA + Quests.getTime(time) + GRAY + ")\n";
            }

            if (context.getSessionData(pref + CK.S_DELAY) == null) {
                text += GRAY + "" + BOLD + "18 " + RESET + GRAY + "- " + Lang.get("stageEditorDelayMessage") + GRAY + " (" + Lang.get("noDelaySet") + ")\n";
            } else if (context.getSessionData(pref + CK.S_DELAY_MESSAGE) == null) {
                text += PINK + "" + BOLD + "18 " + RESET + PURPLE + "- " + Lang.get("stageEditorDelayMessage") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "18 " + RESET + PURPLE + "- " + Lang.get("stageEditorDelayMessage") + GRAY + " (" + AQUA + "\"" + context.getSessionData(pref + CK.S_DELAY_MESSAGE) + "\"" + GRAY + ")\n";
            }

            if (questFactory.quests.denizen == null) {
                text += GRAY + "" + BOLD + "19 " + RESET + GRAY + "- " + Lang.get("stageEditorDenizenScript") + GRAY + " (" + Lang.get("questDenNotInstalled") + ")\n";
            } else {

                if (context.getSessionData(pref + CK.S_DENIZEN) == null) {
                    text += PINK + "" + BOLD + "19 " + RESET + PURPLE + "- " + Lang.get("stageEditorDenizenScript") + GRAY + " (" + Lang.get("noneSet") + ")\n";
                } else {
                    text += PINK + "" + BOLD + "19 " + RESET + PURPLE + "- " + Lang.get("stageEditorDenizenScript") + GRAY + " (" + AQUA + context.getSessionData(pref + CK.S_DENIZEN) + GRAY + "\n";
                }

            }

            if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) == null) {
                text += PINK + "" + BOLD + "20 " + RESET + PURPLE + "- " + Lang.get("stageEditorPassword") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                LinkedList<LinkedList<String>> passPhrases = (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
                LinkedList<String> passDisplays = (LinkedList<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS);
                text += PINK + "" + BOLD + "20 " + RESET + PURPLE + "- " + Lang.get("stageEditorPassword") + "\n";
                for (int i = 0; i < passPhrases.size(); i++) {
                    text += AQUA + "    - \"" + passDisplays.get(i) + "\"\n";
                    LinkedList<String> phrases = passPhrases.get(i);
                    for (String phrase : phrases) {
                        text += DARKAQUA + "      - " + phrase + "\n";
                    }
                }
            }

            if (context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) == null) {
                text += PINK + "" + BOLD + "21 " + RESET + PINK + "- " + Lang.get("stageEditorCustom") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                LinkedList<String> customObjs = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
                text += PINK + "" + BOLD + "21 " + RESET + PINK + "- " + Lang.get("stageEditorCustom") + "\n";
                for (String s : customObjs) {
                    text += PINK + "    - " + GOLD + s + "\n";
                }
            }

            if (context.getSessionData(pref + CK.S_START_MESSAGE) == null) {
                text += PINK + "" + BOLD + "22 " + RESET + PURPLE + "- " + Lang.get("stageEditorStartMessage") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "22 " + RESET + PURPLE + "- " + Lang.get("stageEditorStartMessage") + GRAY + "(" + AQUA + "\"" + context.getSessionData(pref + CK.S_START_MESSAGE) + "\"" + GRAY + ")\n";
            }

            if (context.getSessionData(pref + CK.S_COMPLETE_MESSAGE) == null) {
                text += PINK + "" + BOLD + "23 " + RESET + PURPLE + "- " + Lang.get("stageEditorCompleteMessage") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "23 " + RESET + PURPLE + "- " + Lang.get("stageEditorCompleteMessage") + GRAY + "(" + AQUA + "\"" + context.getSessionData(pref + CK.S_COMPLETE_MESSAGE) + "\"" + GRAY + ")\n";
            }

            if (context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) == null) {
                text += PINK + "" + BOLD + "24 " + RESET + PURPLE + "- " + Lang.get("stageObjectiveOverride") + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "24 " + RESET + PURPLE + "- " + Lang.get("stageObjectiveOverride") + GRAY + "(" + DARKAQUA + "\"" + context.getSessionData(pref + CK.S_OVERRIDE_DISPLAY) + "\"" + GRAY + ")\n";
            }

            text += RED + "" + BOLD + "25 " + RESET + PURPLE + "- " + Lang.get("stageEditorDelete") + "\n";
            text += GREEN + "" + BOLD + "26 " + RESET + PURPLE + "- " + Lang.get("done") + "\n";

            return text;

        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {

        if (input.equalsIgnoreCase("1")) {
            return new BreakBlockListPrompt();
        } else if (input.equalsIgnoreCase("2")) {
            return new DamageBlockListPrompt();
        } else if (input.equalsIgnoreCase("3")) {
            return new PlaceBlockListPrompt();
        } else if (input.equalsIgnoreCase("4")) {
            return new UseBlockListPrompt();
        } else if (input.equalsIgnoreCase("5")) {
            return new CutBlockListPrompt();
        } else if (input.equalsIgnoreCase("6")) {
            return new FishPrompt();
        } else if (input.equalsIgnoreCase("7")) {
            return new KillPlayerPrompt();
        } else if (input.equalsIgnoreCase("8")) {
            return new EnchantmentListPrompt();
        } else if (input.equalsIgnoreCase("9")) {
            if (questFactory.quests.citizens != null) {
                return new DeliveryListPrompt();
            } else {
                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoCitizens"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        } else if (input.equalsIgnoreCase("10")) {
            if (questFactory.quests.citizens != null) {
                return new NPCIDsToTalkToPrompt();
            } else {
                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoCitizens"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        } else if (input.equalsIgnoreCase("11")) {
            if (questFactory.quests.citizens != null) {
                return new NPCKillListPrompt();
            } else {
                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoCitizens"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        } else if (input.equalsIgnoreCase("12")) {
            return new MobListPrompt();
        } else if (input.equalsIgnoreCase("13")) {
            return new ReachListPrompt();
        } else if (input.equalsIgnoreCase("14")) {
            return new TameListPrompt();
        } else if (input.equalsIgnoreCase("15")) {
            return new ShearListPrompt();
        } else if (input.equalsIgnoreCase("16")) {
            return new EventListPrompt();
        } else if (input.equalsIgnoreCase("17")) {
            return new DelayPrompt();
        } else if (input.equalsIgnoreCase("18")) {
            if (context.getSessionData(pref + CK.S_DELAY) == null) {
                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoDelaySet"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new DelayMessagePrompt();
            }
        } else if (input.equalsIgnoreCase("19")) {
            if (questFactory.quests.denizen == null) {
                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoDenizen"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new DenizenPrompt();
            }
        } else if (input.equalsIgnoreCase("20")) {
            return new PasswordListPrompt();
        } else if (input.equalsIgnoreCase("21")) {
            return new CustomObjectivesPrompt();
        } else if (input.equalsIgnoreCase("22")) {
            return new StartMessagePrompt();
        } else if (input.equalsIgnoreCase("23")) {
            return new CompleteMessagePrompt();
        } else if (input.equalsIgnoreCase("24")) {
            return new OverrideDisplayPrompt();
        } else if (input.equalsIgnoreCase("25")) {
            return new DeletePrompt();
        } else if (input.equalsIgnoreCase("26")) {
            return new StagesPrompt(questFactory);
        } else {
            return new CreateStagePrompt(stageNum, questFactory, citizens);
        }

    }

    private class PasswordListPrompt extends FixedSetPrompt {

        public PasswordListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorPassword") + "-\n";
            if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorAddPasswordDisplay") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorAddPasswordPhrases") + " (" + Lang.get("stageEditorNoPasswordDisplays") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorAddPasswordDisplay") + "\n";
                for (String display : getPasswordDisplays(context)) {

                    text += GRAY + "    - " + AQUA + display + "\n";

                }

                if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) == null) {
                    text += YELLOW + "2 - " + Lang.get("stageEditorAddPasswordPhrases") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += YELLOW + "2 - " + Lang.get("stageEditorAddPasswordPhrases") + "\n";
                    for (LinkedList<String> phraseList : getPasswordPhrases(context)) {

                        text += GRAY + "    - ";
                        for (String s : phraseList) {
                            if (phraseList.getLast().equals(s) == false) {
                                text += DARKAQUA + s + GRAY + "|";
                            } else {
                                text += DARKAQUA + s + "\n";
                            }
                        }

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new PasswordDisplayPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorMustSetPasswordDisplays"));
                    return new PasswordListPrompt();
                } else {
                    return new PasswordPhrasePrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorAddPasswordCleared"));
                context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, null);
                context.setSessionData(pref + CK.S_PASSWORD_PHRASES, null);
                return new PasswordListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) != null) {
                    two = ((LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorPasswordNotSameSize"));
                    return new PasswordListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getPasswordDisplays(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS);
        }

        @SuppressWarnings("unchecked")
		private LinkedList<LinkedList<String>> getPasswordPhrases(ConversationContext context) {
            return (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
        }

    }

    private class PasswordDisplayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = YELLOW + Lang.get("stageEditorPasswordDisplayPrompt") + "\n";
            text += ITALIC + "" + GOLD + Lang.get("stageEditorPasswordDisplayHint");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS) != null) {

                    @SuppressWarnings("unchecked")
					List<String> displays = (List<String>) context.getSessionData(pref + CK.S_PASSWORD_DISPLAYS);
                    displays.add(input);
                    context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, displays);

                } else {

                    List<String> displays = new LinkedList<String>();
                    displays.add(input);
                    context.setSessionData(pref + CK.S_PASSWORD_DISPLAYS, displays);

                }

            }

            return new PasswordListPrompt();

        }

    }

    private class PasswordPhrasePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = YELLOW + Lang.get("stageEditorPasswordPhrasePrompt") + "\n";
            text += ITALIC + "" + GOLD + Lang.get("stageEditorPasswordPhraseHint1") + "\n";
            text += RESET + "" + YELLOW + Lang.get("stageEditorPasswordPhraseHint2");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (context.getSessionData(pref + CK.S_PASSWORD_PHRASES) != null) {

                    @SuppressWarnings("unchecked")
					LinkedList<LinkedList<String>> phrases = (LinkedList<LinkedList<String>>) context.getSessionData(pref + CK.S_PASSWORD_PHRASES);
                    LinkedList<String> newPhrases = new LinkedList<String>();
                    newPhrases.addAll(Arrays.asList(input.split("\\|")));
                    phrases.add(newPhrases);
                    context.setSessionData(pref + CK.S_PASSWORD_PHRASES, phrases);

                } else {

                    LinkedList<LinkedList<String>> phrases = new LinkedList<LinkedList<String>>();
                    LinkedList<String> newPhrases = new LinkedList<String>();
                    newPhrases.addAll(Arrays.asList(input.split("\\|")));
                    phrases.add(newPhrases);
                    context.setSessionData(pref + CK.S_PASSWORD_PHRASES, phrases);

                }

            }

            return new PasswordListPrompt();

        }

    }

    private class OverrideDisplayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = YELLOW + Lang.get("stageEditorObjectiveOverridePrompt") + "\n";
            text += ITALIC + "" + GOLD + Lang.get("stageEditorObjectiveOverrideHint");

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                context.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, input);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData(pref + CK.S_OVERRIDE_DISPLAY, null);
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorObjectiveOverrideCleared"));

            }

            return new CreateStagePrompt(stageNum, questFactory, citizens);

        }

    }

    private class BreakBlockListPrompt extends FixedSetPrompt {

        public BreakBlockListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorBreakBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noNamesSet") + ")\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noNamesSet") + ")\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(s) + "\n";

                }

                if (context.getSessionData(pref + CK.S_BREAK_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }
                
                if (context.getSessionData(pref + CK.S_BREAK_DURABILITY) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Integer i : getBlockDurability(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new BreakBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new BreakBlockListPrompt();
                } else {
                    return new BreakBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_BREAK_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new BreakBlockListPrompt();
                } else {
                    return new BreakBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorBreakBlocksCleared"));
                context.setSessionData(pref + CK.S_BREAK_NAMES, null);
                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, null);
                context.setSessionData(pref + CK.S_BREAK_DURABILITY, null);
                return new BreakBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_BREAK_NAMES) != null) {
                    one = ((List<Integer>) context.getSessionData(pref + CK.S_BREAK_NAMES)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_BREAK_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorListNotSameSize"));
                    return new BreakBlockListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_BREAK_NAMES);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_BREAK_AMOUNTS);
        }
        
        @SuppressWarnings("unchecked")
		private List<Integer> getBlockDurability(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_BREAK_DURABILITY);
        }
    }

    private class BreakBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {

                    try {

                        if (Material.matchMaterial(s) != null) {

                            if (names.contains(s) == false) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorContainsDuplicates"));
                                return new BreakBlockNamesPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorInvalidBlockName"));
                            return new BreakBlockNamesPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNotListofNumbers") + "\n" + PINK + s);
                        return new BreakBlockNamesPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_BREAK_NAMES, names);

            }

            return new BreakBlockListPrompt();

        }
    }

    private class BreakBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorBreakBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditortNotGreaterThanZero"));
                            return new BreakBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_BREAK_AMOUNTS, amounts);

            }

            return new BreakBlockListPrompt();

        }
    }
    
    private class BreakBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorBreakBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> durability = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                        	durability.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditortNotGreaterThanZero"));
                            return new BreakBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new BreakBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_BREAK_DURABILITY, durability);

            }

            return new BreakBlockListPrompt();

        }
    }

    private class DamageBlockListPrompt extends FixedSetPrompt {

        public DamageBlockListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorDamageBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetDamageAmounts") + " (" + Lang.get("noNamesSet") + ")\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noNamesSet") + ")\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(s) + "\n";

                }

                if (context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetDamageAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetDamageAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                if (context.getSessionData(pref + CK.S_DAMAGE_DURABILITY) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Integer i : getBlockDurability(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new DamageBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new DamageBlockListPrompt();
                } else {
                    return new DamageBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new DamageBlockListPrompt();
                } else {
                    return new DamageBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorDamageBlocksCleared"));
                context.setSessionData(pref + CK.S_DAMAGE_NAMES, null);
                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, null);
                return new DamageBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_DAMAGE_NAMES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorListNotSameSize"));
                    return new DamageBlockListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_DAMAGE_NAMES);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_AMOUNTS);
        }
        
        @SuppressWarnings("unchecked")
		private List<Integer> getBlockDurability(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_DAMAGE_DURABILITY);
        }
    }

    private class DamageBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {

                    try {

                        if (Material.matchMaterial(s) != null) {

                            if (names.contains(s) == false) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorListContainsDuplicates"));
                                return new DamageBlockNamesPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorInvalidBlockName"));
                            return new DamageBlockNamesPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockNamesPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_DAMAGE_NAMES, names);

            }

            return new DamageBlockListPrompt();

        }
    }

    private class DamageBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorDamageBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorNotGreaterThanZero"));
                            return new DamageBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_DAMAGE_AMOUNTS, amounts);

            }

            return new DamageBlockListPrompt();

        }
    }
    
    private class DamageBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorDamageBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> durability = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                        	durability.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditortNotGreaterThanZero"));
                            return new DamageBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new DamageBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_DAMAGE_DURABILITY, durability);

            }

            return new DamageBlockListPrompt();

        }
    }

    private class PlaceBlockListPrompt extends FixedSetPrompt {

        public PlaceBlockListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorPlaceBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetPlaceAmounts") + " (" + Lang.get("noNamesSet") + ")\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noNamesSet") + ")\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(s) + "\n";

                }

                if (context.getSessionData(pref + CK.S_PLACE_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetPlaceAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + "stageEditorSetPlaceAmounts" + "\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }
                
                if (context.getSessionData(pref + CK.S_PLACE_DURABILITY) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Integer i : getBlockDurability(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new PlaceBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new PlaceBlockListPrompt();
                } else {
                    return new PlaceBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_PLACE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new PlaceBlockListPrompt();
                } else {
                    return new PlaceBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorPlaceBlocksCleared"));
                context.setSessionData(pref + CK.S_PLACE_NAMES, null);
                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_PLACE_DURABILITY, null);
                return new PlaceBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_PLACE_NAMES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_PLACE_NAMES)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_PLACE_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorListNotSameSize"));
                    return new PlaceBlockListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_PLACE_NAMES);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_PLACE_AMOUNTS);
        }
        
        @SuppressWarnings("unchecked")
		private List<Integer> getBlockDurability(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_PLACE_DURABILITY);
        }
    }

    private class PlaceBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {

                    try {

                        if (Material.matchMaterial(s) != null) {

                            if (names.contains(s) == false) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorListContainsDuplicates"));
                                return new PlaceBlockNamesPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorInvalidBlockName"));
                            return new PlaceBlockNamesPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockNamesPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_PLACE_NAMES, names);

            }

            return new PlaceBlockListPrompt();

        }
    }

    private class PlaceBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorPlaceBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorNotGreaterThanZero"));
                            return new PlaceBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_PLACE_AMOUNTS, amounts);

            }

            return new PlaceBlockListPrompt();

        }
    }
    
    private class PlaceBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorPlaceBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> durability = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                        	durability.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditortNotGreaterThanZero"));
                            return new PlaceBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new PlaceBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_PLACE_DURABILITY, durability);

            }

            return new PlaceBlockListPrompt();

        }
    }

    private class UseBlockListPrompt extends FixedSetPrompt {

        public UseBlockListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorUseBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetUseAmounts") + " (" + Lang.get("noNamesSet") + ")\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noNamesSet") + ")\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(s) + "\n";

                }

                if (context.getSessionData(pref + CK.S_USE_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetUseAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetUseAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }
                
                if (context.getSessionData(pref + CK.S_USE_DURABILITY) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Integer i : getBlockDurability(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new UseBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new UseBlockListPrompt();
                } else {
                    return new UseBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_USE_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new UseBlockListPrompt();
                } else {
                    return new UseBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorUseBlocksCleared"));
                context.setSessionData(pref + CK.S_USE_NAMES, null);
                context.setSessionData(pref + CK.S_USE_AMOUNTS, null);
                context.setSessionData(pref + CK.S_USE_DURABILITY, null);
                return new UseBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_USE_NAMES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_USE_NAMES)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_USE_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorListNotSameSize"));
                    return new UseBlockListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_USE_NAMES);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_USE_AMOUNTS);
        }
        
        @SuppressWarnings("unchecked")
		private List<Integer> getBlockDurability(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_USE_DURABILITY);
        }
    }

    private class UseBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {

                    try {

                        if (Material.matchMaterial(s) != null) {

                            if (names.contains(s) == false) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(RED + " " + Lang.get("stageEditorContainsDuplicates"));
                                return new UseBlockNamesPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorInvalidBlockName"));
                            return new UseBlockNamesPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockNamesPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_USE_NAMES, names);

            }

            return new UseBlockListPrompt();

        }
    }

    private class UseBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorUseBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorNotGreaterThanZero"));
                            return new UseBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_USE_AMOUNTS, amounts);

            }

            return new UseBlockListPrompt();

        }
    }
    
    private class UseBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorUseBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> durability = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                        	durability.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditortNotGreaterThanZero"));
                            return new UseBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new UseBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_USE_DURABILITY, durability);

            }

            return new UseBlockListPrompt();

        }
    }

    private class CutBlockListPrompt extends FixedSetPrompt {

        public CutBlockListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorCutBlocks") + " -\n";
            if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetCutAmounts") + " (" + Lang.get("noNamesSet") + ")\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noNamesSet") + ")\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockNames") + "\n";
                for (String s : getBlockNames(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(s) + "\n";

                }

                if (context.getSessionData(pref + CK.S_CUT_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetCutAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetCutAmounts") + "\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                if (context.getSessionData(pref + CK.S_CUT_DURABILITY) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetBlockDurability") + "\n";
                    for (Integer i : getBlockDurability(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new CutBlockNamesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new CutBlockListPrompt();
                } else {
                    return new CutBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_CUT_NAMES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoBlockNames"));
                    return new CutBlockListPrompt();
                } else {
                    return new CutBlockDurabilityPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorCutBlocksCleared"));
                context.setSessionData(pref + CK.S_CUT_NAMES, null);
                context.setSessionData(pref + CK.S_CUT_AMOUNTS, null);
                context.setSessionData(pref + CK.S_CUT_DURABILITY, null);
                return new CutBlockListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_CUT_NAMES) != null) {
                    one = ((List<Integer>) context.getSessionData(pref + CK.S_CUT_NAMES)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_CUT_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorListNotSameSize"));
                    return new CutBlockListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getBlockNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_CUT_NAMES);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_CUT_AMOUNTS);
        }
        
        @SuppressWarnings("unchecked")
		private List<Integer> getBlockDurability(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_CUT_DURABILITY);
        }
    }

    private class CutBlockNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorEnterBlockNames");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {

                    try {

                        if (Material.matchMaterial(s) != null) {

                            if (names.contains(s) == false) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(RED + " " + Lang.get("stageEditorListContainsDuplicates"));
                                return new CutBlockNamesPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorInvalidBlockName"));
                            return new CutBlockNamesPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockNamesPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_CUT_NAMES, names);

            }

            return new CutBlockListPrompt();

        }
    }

    private class CutBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorCutBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorNotGreaterThanZero"));
                            return new CutBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_CUT_AMOUNTS, amounts);

            }

            return new CutBlockListPrompt();

        }
    }
    
    private class CutBlockDurabilityPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorCutBlocksPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> durability = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                        	durability.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditortNotGreaterThanZero"));
                            return new CutBlockAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new CutBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_CUT_DURABILITY, durability);

            }

            return new CutBlockListPrompt();

        }
    }

    private class FishPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorCatchFishPrompt");
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number number) {

            int num = number.intValue();
            Player player = (Player) context.getForWhom();

            if (num < -1) {
                player.sendMessage(RED + Lang.get("stageEditorPositiveAmount"));
                return new FishPrompt();
            } else if (num == 0) {
                context.setSessionData(pref + CK.S_FISH, null);
            } else if (num > 0) {
                context.setSessionData(pref + CK.S_FISH, num);
            }

            return new CreateStagePrompt(stageNum, questFactory, citizens);

        }
    }

    private class KillPlayerPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorKillPlayerPrompt");
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number number) {

            int num = number.intValue();
            Player player = (Player) context.getForWhom();

            if (num < -1) {
                player.sendMessage(RED + Lang.get("stageEditorPositiveAmount"));
                return new KillPlayerPrompt();
            } else if (num == 0) {
                context.setSessionData(pref + CK.S_PLAYER_KILL, null);
            } else if (num > 0) {
                context.setSessionData(pref + CK.S_PLAYER_KILL, num);
            }

            return new CreateStagePrompt(stageNum, questFactory, citizens);

        }
    }

    private class EnchantmentListPrompt extends FixedSetPrompt {

        public EnchantmentListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorEnchantItems") + " -\n";
            if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetEnchantments") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetItemNames") + " (" + Lang.get("stageEditorNoEnchantmentsSet") + ")\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetEnchantAmounts") + " (" + Lang.get("stageEditorNoEnchantmentsSet") + ")\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetEnchantments") + "\n";
                for (String s : getEnchantTypes(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + CK.S_ENCHANT_NAMES) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetItemNames") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetItemNames") + "\n";
                    for (String s : getEnchantItems(context)) {

                        text += GRAY + "    - " + AQUA + Quester.prettyItemString(s) + "\n";

                    }

                }

                if (context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetEnchantAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetEnchantAmounts") + "\n";
                    for (int i : getEnchantAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new EnchantTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoEnchantments"));
                    return new EnchantmentListPrompt();
                } else {
                    return new EnchantItemsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoEnchantments"));
                    return new EnchantmentListPrompt();
                } else {
                    return new EnchantAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorEnchantmentsCleared"));
                context.setSessionData(pref + CK.S_ENCHANT_TYPES, null);
                context.setSessionData(pref + CK.S_ENCHANT_NAMES, null);
                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, null);
                return new EnchantmentListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;
                int three;

                if (context.getSessionData(pref + CK.S_ENCHANT_TYPES) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_ENCHANT_NAMES) != null) {
                    two = ((List<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES)).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS) != null) {
                    three = ((List<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS)).size();
                } else {
                    three = 0;
                }

                if (one == two && two == three) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorEnchantmentNotSameSize"));
                    return new EnchantmentListPrompt();
                }
            }

            return null;

        }

        @SuppressWarnings("unchecked")
		private List<String> getEnchantTypes(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_ENCHANT_TYPES);
        }

        @SuppressWarnings("unchecked")
		private List<String> getEnchantItems(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_ENCHANT_NAMES);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getEnchantAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_ENCHANT_AMOUNTS);
        }
    }

    private class EnchantTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = PINK + "- " + PURPLE + Lang.get("stageEditorEnchantments") + PINK + " -\n";
            for (Enchantment e : Enchantment.values()) {

                text += GREEN + Quester.prettyEnchantmentString(e) + ", ";

            }
            text = text.substring(0, text.length() - 1);

            return text + "\n" + YELLOW + Lang.get("stageEditorEnchantTypePrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(",");
                LinkedList<String> enchs = new LinkedList<String>();
                boolean valid;
                for (String s : args) {

                    s = s.trim();
                    valid = false;
                    for (Enchantment e : Enchantment.values()) {

                        if (Quester.prettyEnchantmentString(e).equalsIgnoreCase(s)) {

                            if (enchs.contains(s) == false) {
                                enchs.add(Quester.prettyEnchantmentString(e));
                                valid = true;
                                break;
                            } else {
                                context.getForWhom().sendRawMessage(RED + " " + Lang.get("stageEditorListContainsDuplicates"));
                                return new EnchantTypesPrompt();
                            }

                        }

                    }
                    if (valid == false) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorInvalidEnchantment"));
                        return new EnchantTypesPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_ENCHANT_TYPES, enchs);

            }

            return new EnchantmentListPrompt();

        }
    }

    private class EnchantItemsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorItemNamesPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<String> names = new LinkedList<String>();
                for (String s : args) {

                    try {

                        if (Material.matchMaterial(s) != null) {

                            if (names.contains(s) == false) {
                                names.add(s);
                            } else {
                                context.getForWhom().sendRawMessage(RED + " " + Lang.get("stageEditorListContainsDuplicates"));
                                return new EnchantItemsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorInvalidItemName"));
                            return new EnchantItemsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new EnchantItemsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_ENCHANT_NAMES, names);

            }

            return new EnchantmentListPrompt();

        }
    }

    private class EnchantAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorEnchantAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorNotGreaterThanZero"));
                            return new EnchantAmountsPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new EnchantAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_ENCHANT_AMOUNTS, amounts);

            }

            return new EnchantmentListPrompt();

        }
    }

    private class DeliveryListPrompt extends FixedSetPrompt {

        public DeliveryListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null) {
                    List<ItemStack> itemRews = getItems(context);
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_DELIVERY_ITEMS, itemRews);
                } else {
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + CK.S_DELIVERY_ITEMS, itemRews);
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }

            String text = GOLD + "- " + Lang.get("stageEditorDeliverItems") + " -\n";
            if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                text += GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorDeliveryAddItem") + "\n";
                text += GRAY + "2 - " + Lang.get("stageEditorDeliveryNPCs") + " (" + Lang.get("stageEditorNoItemsSet") + ")\n";
                if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null) {
                    text += BLUE + "3 - " + Lang.get("stageEditorDeliveryMessages") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "3 - " + Lang.get("stageEditorDeliveryMessages") + "\n";
                    for (String s : getDeliveryMessages(context)) {

                        text += GRAY + "    - " + AQUA + "\"" + s + "\"";

                    }

                }

            } else {

                for (ItemStack is : getItems(context)) {

                    text += GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";

                }

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorDeliveryAddItem") + "\n";

                if (context.getSessionData(pref + CK.S_DELIVERY_NPCS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorDeliveryNPCs") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorDeliveryNPCs") + "\n";
                    for (int i : getDeliveryNPCs(context)) {

                        text += GRAY + "    - " + AQUA + i + " (" + citizens.getNPCRegistry().getById(i).getName() + ")\n";

                    }

                }

                if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null) {
                    text += BLUE + "3 - " + Lang.get("stageEditorDeliveryMessages") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "3 - " + Lang.get("stageEditorDeliveryMessages") + "\n";
                    for (String s : getDeliveryMessages(context)) {

                        text += GRAY + "    - " + AQUA + "\"" + s + "\"\n";

                    }

                }

            }

            text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
            text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(DeliveryListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoItems"));
                    return new DeliveryListPrompt();
                } else {
                    return new DeliveryNPCsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                return new DeliveryMessagesPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorDeliveriesCleared"));
                context.setSessionData(pref + CK.S_DELIVERY_ITEMS, null);
                context.setSessionData(pref + CK.S_DELIVERY_NPCS, null);
                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, null);
                return new DeliveryListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_DELIVERY_ITEMS) != null) {
                    one = ((List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_DELIVERY_NPCS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS)).size();
                } else {
                    two = 0;
                }

                if (one == two) {

                    if (context.getSessionData(pref + CK.S_DELIVERY_MESSAGES) == null && one != 0) {
                        context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoDeliveryMessage"));
                        return new DeliveryListPrompt();
                    } else {
                        return new CreateStagePrompt(stageNum, questFactory, citizens);
                    }

                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorDeliveriesNotSameSize"));
                    return new DeliveryListPrompt();
                }
            }

            return null;

        }

        @SuppressWarnings("unchecked")
		private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(pref + CK.S_DELIVERY_ITEMS);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getDeliveryNPCs(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_DELIVERY_NPCS);
        }

        @SuppressWarnings("unchecked")
		private List<String> getDeliveryMessages(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_DELIVERY_MESSAGES);
        }
    }

    private class DeliveryNPCsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            questFactory.selectingNPCs.add((Player) context.getForWhom());
            return YELLOW + Lang.get("stageEditorNPCPrompt") + "\n" + GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        Integer i = Integer.parseInt(s);

                        if (citizens.getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(PINK + "" + i + RED + " " + Lang.get("stageEditorInvalidNPC"));
                            return new DeliveryNPCsPrompt();
                        }

                    } catch (NumberFormatException e) {

                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new DeliveryNPCsPrompt();

                    }

                }

                context.setSessionData(pref + CK.S_DELIVERY_NPCS, npcs);

            }

            questFactory.selectingNPCs.remove((Player) context.getForWhom());
            return new DeliveryListPrompt();

        }
    }

    private class DeliveryMessagesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String note = GOLD + Lang.get("stageEditorNPCNote");
            return YELLOW + Lang.get("stageEditorDeliveryMessagesPrompt") + "\n" + note;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(";");
                LinkedList<String> messages = new LinkedList<String>();
                messages.addAll(Arrays.asList(args));

                context.setSessionData(pref + CK.S_DELIVERY_MESSAGES, messages);

            }

            return new DeliveryListPrompt();

        }
    }

    private class NPCIDsToTalkToPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            questFactory.selectingNPCs.add((Player) context.getForWhom());
            return YELLOW + Lang.get("stageEditorNPCToTalkToPrompt") + "\n" + GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        Integer i = Integer.parseInt(s);

                        if (citizens.getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(PINK + "" + i + RED + " " + Lang.get("stageEditorInvalidNPC"));
                            return new NPCIDsToTalkToPrompt();
                        }

                    } catch (NumberFormatException e) {

                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new NPCIDsToTalkToPrompt();

                    }

                }

                questFactory.selectingNPCs.remove((Player) context.getForWhom());
                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, npcs);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {

                context.setSessionData(pref + CK.S_NPCS_TO_TALK_TO, null);

            }

            return new CreateStagePrompt(stageNum, questFactory, citizens);

        }
    }

    private class NPCKillListPrompt extends FixedSetPrompt {

        public NPCKillListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorKillNPCs") + " -\n";
            if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetKillIds") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetKillAmounts") + " (" + Lang.get("noIdsSet") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("cancel") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetKillIds") + "\n";
                for (Integer i : getNPCIds(context)) {

                    text += GRAY + "    - " + AQUA + citizens.getNPCRegistry().getById(i).getName() + DARKAQUA + " (" + i + ")\n";

                }

                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetKillAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetKillAmounts") + "\n";
                    for (Integer i : getKillAmounts(context)) {

                        text += GRAY + "    - " + BLUE + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("cancel") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new NpcIdsToKillPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoNPCs"));
                    return new NPCKillListPrompt();
                } else {
                    return new NpcAmountsToKillPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorKillNPCsCleared"));
                context.setSessionData(pref + CK.S_NPCS_TO_KILL, null);
                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, null);
                return new NPCKillListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL) != null) {
                    one = ((List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS)).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNPCKillsNotSameSize"));
                    return new NPCKillListPrompt();
                }
            }
            return null;

        }

        @SuppressWarnings("unchecked")
		private List<Integer> getNPCIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getKillAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS);
        }
    }

    private class NpcIdsToKillPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            questFactory.selectingNPCs.add((Player) context.getForWhom());
            return YELLOW + Lang.get("stageEditorNPCPrompt") + "\n" + GOLD + Lang.get("npcHint");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        Integer i = Integer.parseInt(s);

                        if (citizens.getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(PINK + "" + i + RED + " " + Lang.get("stageEditorInvalidNPC"));
                            return new NpcIdsToKillPrompt();
                        }

                    } catch (NumberFormatException e) {

                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcIdsToKillPrompt();

                    }

                }

                context.setSessionData(pref + CK.S_NPCS_TO_KILL, npcs);

            }

            questFactory.selectingNPCs.remove((Player) context.getForWhom());
            return new NPCKillListPrompt();

        }
    }

    private class NpcAmountsToKillPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + Lang.get("stageEditorKillNPCsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " " + Lang.get("stageEditorNotGreaterThanZero"));
                            return new NpcAmountsToKillPrompt();
                        }

                    } catch (NumberFormatException e) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + Lang.get("stageEditorNotListofNumbers"));
                        return new NpcAmountsToKillPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_NPCS_TO_KILL_AMOUNTS, amounts);

            }

            return new NPCKillListPrompt();

        }
    }

    private class MobListPrompt extends FixedSetPrompt {

        public MobListPrompt() {

            super("1", "2", "3", "4", "5", "6", "7");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorKillMobs") + " -\n";
            if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetMobTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetMobAmounts") + " (" + Lang.get("stageEditorNoMobTypesSet") + ")\n";
                text += DARKGRAY + "|---------" + Lang.get("stageEditorOptional") + "---------|\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetKillLocations") + " (" + Lang.get("stageEditorNoMobTypesSet") + ")\n";
                text += GRAY + "4 - " + Lang.get("stageEditorSetKillLocationRadii") + " (" + Lang.get("stageEditorNoMobTypesSet") + ")\n";
                text += GRAY + "5 - " + Lang.get("stageEditorSetKillLocationNames") + " (" + Lang.get("stageEditorNoMobTypesSet") + ")\n";
                text += DARKGRAY + "|--------------------------|\n";
                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetMobTypes") + "\n";
                for (String s : getMobTypes(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + CK.S_MOB_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetMobAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetMobAmounts") + "\n";
                    for (Integer i : getMobAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += DARKGRAY + "|---------" + Lang.get("stageEditorOptional") + "---------|\n";

                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetKillLocations") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetKillLocations") + "\n";
                    for (String s : getKillLocations(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) == null) {
                    text += BLUE + "4 - " + Lang.get("stageEditorSetKillLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "4 - " + Lang.get("stageEditorSetKillLocationRadii") + "\n";
                    for (int i : getKillRadii(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) == null) {
                    text += BLUE + "5 - " + Lang.get("stageEditorSetKillLocationNames") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "5 - " + Lang.get("stageEditorSetKillLocationNames") + "\n";
                    for (String s : getKillLocationNames(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                text += DARKGRAY + "|--------------------------|\n";

                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - " + Lang.get("done");

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
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoMobTypes"));
                    return new MobListPrompt();
                } else {
                    return new MobAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoMobTypes"));
                    return new MobListPrompt();
                } else {
                    questFactory.selectedKillLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                    return new MobLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoKillLocations"));
                    return new MobListPrompt();
                } else {
                    return new MobRadiiPrompt();
                }
            } else if (input.equalsIgnoreCase("5")) {
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoKillLocations"));
                    return new MobListPrompt();
                } else {
                    return new MobLocationNamesPrompt();
                }
            } else if (input.equalsIgnoreCase("6")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorKillMobsCleared"));
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
                            return new CreateStagePrompt(stageNum, questFactory, citizens);
                        } else {
                            context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorAllListsNotSameSize"));
                            return new MobListPrompt();
                        }

                    } else {
                        return new CreateStagePrompt(stageNum, questFactory, citizens);
                    }

                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorMobTypesNotSameSize"));
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

            String mobs = PINK + Lang.get("eventEditorMobsTitle") + "\n";
            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {

                final EntityType type = mobArr[i];
                if (type.isAlive() == false) {
                    continue;
                }

                if (i < (mobArr.length - 1)) {
                    mobs += MiscUtil.getProperMobName(mobArr[i]) + ", ";
                } else {
                    mobs += MiscUtil.getProperMobName(mobArr[i]) + "\n";
                }
            }

            return mobs + YELLOW + Lang.get("stageEditorMobsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getMobType(s) != null) {

                        mobTypes.add(s);
                        context.setSessionData(pref + CK.S_MOB_TYPES, mobTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("stageEditorInvalidMob"));
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

            return YELLOW + Lang.get("stageEditorMobAmountsPrompt");

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
                            player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorNotGreaterThanZero"));
                            return new MobAmountsPrompt();
                        }

                        mobAmounts.add(i);

                    } catch (NumberFormatException e) {
                        player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorInvalidNumber"));
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

            return YELLOW + Lang.get("stageEditorMobLocationPrompt");

        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = questFactory.selectedKillLocations.get(player.getUniqueId());
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, locs);
                    questFactory.selectedKillLocations.remove(player.getUniqueId());

                } else {
                    player.sendMessage(RED + Lang.get("stageEditorNoBlock"));
                    return new MobLocationPrompt();
                }

                return new MobListPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                questFactory.selectedKillLocations.remove(player.getUniqueId());
                return new MobListPrompt();

            } else {
                return new MobLocationPrompt();
            }

        }
    }

    private class MobRadiiPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorMobLocationRadiiPrompt");

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
                            player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorNotGreaterThanZero"));
                            return new MobRadiiPrompt();
                        }

                        radii.add(i);

                    } catch (NumberFormatException e) {
                        player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorInvalidItemName"));
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

            return YELLOW + Lang.get("stageEditorMobLocationNamesPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> locNames = new LinkedList<String>();
                locNames.addAll(Arrays.asList(input.split(",")));

                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, locNames);

            }

            return new MobListPrompt();

        }
    }

    private class ReachListPrompt extends FixedSetPrompt {

        public ReachListPrompt() {

            super("1", "2", "3", "4", "5");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorReachLocs") + " -\n";
            if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetLocations") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetLocationRadii") + " (" + Lang.get("stageEditorNoLocationsSet") + ")\n";
                text += GRAY + "3 - " + Lang.get("stageEditorSetLocationNames") + " (" + Lang.get("stageEditorNoLocationsSet") + ")\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetLocations") + "\n";
                for (String s : getLocations(context)) {

                    text += GRAY + "    - " + DARKAQUA + s + "\n";

                }

                if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetLocationRadii") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetLocationRadii") + "\n";
                    for (Integer i : getLocationRadii(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetLocationNames") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorSetLocationNames") + "\n";
                    for (String s : getLocationNames(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("done");

            }

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                questFactory.selectedReachLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                return new ReachLocationPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoLocations"));
                    return new ReachListPrompt();
                } else {
                    return new ReachRadiiPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) == null) {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoLocations"));
                    return new ReachListPrompt();
                } else {
                    return new ReachNamesPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorReachLocationsCleared"));
                context.setSessionData(pref + CK.S_REACH_LOCATIONS, null);
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, null);
                context.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, null);
                return new ReachListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;
                int three;

                if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) != null) {
                    one = ((List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS)).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS) != null) {
                    two = ((List<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS)).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES) != null) {
                    three = ((List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES)).size();
                } else {
                    three = 0;
                }

                if (one == two && two == three) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("allListsNotSameSize"));
                    return new ReachListPrompt();
                }

            } else {
                return new ReachListPrompt();
            }

        }

        @SuppressWarnings("unchecked")
		private List<String> getLocations(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS);
        }

        @SuppressWarnings("unchecked")
		private List<Integer> getLocationRadii(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS);
        }

        @SuppressWarnings("unchecked")
		private List<String> getLocationNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS_NAMES);
        }
    }

    private class ReachLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorReachLocationPrompt");

        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {

                Block block = questFactory.selectedReachLocations.get(player.getUniqueId());
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData(pref + CK.S_REACH_LOCATIONS) != null) {
                        locs = (LinkedList<String>) context.getSessionData(pref + CK.S_REACH_LOCATIONS);
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData(pref + CK.S_REACH_LOCATIONS, locs);
                    questFactory.selectedReachLocations.remove(player.getUniqueId());

                } else {
                    player.sendMessage(RED + Lang.get("stageEditorNoBlockSelected"));
                    return new ReachLocationPrompt();
                }

                return new ReachListPrompt();

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {

                questFactory.selectedReachLocations.remove(player.getUniqueId());
                return new ReachListPrompt();

            } else {
                return new ReachLocationPrompt();
            }

        }
    }

    private class ReachRadiiPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorReachLocationRadiiPrompt");

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
                            player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorNotGreaterThanZero"));
                            return new ReachRadiiPrompt();
                        }

                        radii.add(i);

                    } catch (NumberFormatException e) {
                        player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorInvalidNumber"));
                        return new ReachRadiiPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_REACH_LOCATIONS_RADIUS, radii);

            }

            return new ReachListPrompt();

        }
    }

    private class ReachNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorReachLocationNamesPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> locNames = new LinkedList<String>();
                locNames.addAll(Arrays.asList(input.split(",")));

                context.setSessionData(pref + CK.S_REACH_LOCATIONS_NAMES, locNames);

            }

            return new ReachListPrompt();

        }
    }

    private class TameListPrompt extends FixedSetPrompt {

        public TameListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- " + Lang.get("stageEditorTameMobs") + " -\n";
            if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetMobTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetTameAmounts") + " (" + Lang.get("stageEditorNoMobTypesSet") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetMobTypes") + "\n";
                for (String s : getTameTypes(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + CK.S_TAME_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetTameAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetTameAmounts") + "\n";
                    for (Integer i : getTameAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

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
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoMobTypes"));
                    return new TameListPrompt();
                } else {
                    return new TameAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorTameCleared"));
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
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorTameMobsNotSameSize"));
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

            String mobs = PINK + Lang.get("eventEditorMobsTitle") + "\n";

            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {

                final EntityType type = mobArr[i];
                if (type.isAlive() == false) {
                    continue;
                }

                if (i < (mobArr.length - 1)) {
                    mobs += MiscUtil.getProperMobName(mobArr[i]) + ", ";
                } else {
                    mobs += MiscUtil.getProperMobName(mobArr[i]) + "\n";
                }
            }

            return mobs + YELLOW + Lang.get("stageEditorMobsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getMobType(s) != null) {

                        mobTypes.add(Quester.prettyMobString(Quests.getMobType(s)));
                        context.setSessionData(pref + CK.S_TAME_TYPES, mobTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("stageEditorInvalidMob"));
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

            return YELLOW + Lang.get("stageEditorTameAmountsPrompt");

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
                            player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorNotGreaterThanZero"));
                            return new TameAmountsPrompt();
                        }

                        mobAmounts.add(i);

                    } catch (NumberFormatException e) {
                        player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorInvalidNumber"));
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

            String text = GOLD + "- " + Lang.get("stageEditorShearSheep") + " -\n";
            if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetShearColors") + " (" + Lang.get("noneSet") + ")\n";
                text += GRAY + "2 - " + Lang.get("stageEditorSetShearAmounts") + " (" + Lang.get("stageEditorNoColorsSet") + ")\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorSetShearColors") + "\n";
                for (String s : getShearColors(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetShearAmounts") + " (" + Lang.get("noneSet") + ")\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorSetShearAmounts") + "\n";
                    for (Integer i : getShearAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("clear") + "\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("done");

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
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoColors"));
                    return new ShearListPrompt();
                } else {
                    return new ShearAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorShearCleared"));
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
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorShearNotSameSize"));
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

            String cols = PINK + "- " + Lang.get("stageEditorColors") + " - \n";
            final DyeColor[] colArr = DyeColor.values();
            for (int i = 0; i < colArr.length; i++) {

                if (i < (colArr.length - 1)) {
                    cols += Quests.getDyeString(colArr[i]) + ", ";
                } else {
                    cols += Quests.getDyeString(colArr[i]) + "\n";
                }
            }

            return cols + YELLOW + Lang.get("stageEditorShearColorsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                LinkedList<String> colors = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getDyeColor(s) != null) {

                        colors.add(Quests.getDyeString(Quests.getDyeColor(s)));
                        context.setSessionData(pref + CK.S_SHEAR_COLORS, colors);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + Lang.get("stageEditorInvalidDye"));
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

            return YELLOW + Lang.get("stageEditorShearAmountsPrompt");

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
                            player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorNotGreaterThanZero"));
                            return new ShearAmountsPrompt();
                        }

                        shearAmounts.add(i);

                    } catch (NumberFormatException e) {
                        player.sendMessage(PINK + input + " " + RED + Lang.get("stageEditorInvalidNumber"));
                        return new ShearAmountsPrompt();
                    }

                }

                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, shearAmounts);

            }

            return new ShearListPrompt();

        }
    }

    private class EventListPrompt extends FixedSetPrompt {

        public EventListPrompt() {

            super("1", "2", "3", "4", "5", "6");

        }

        @SuppressWarnings("unchecked")
		@Override
        public String getPromptText(ConversationContext context) {

            String text = GREEN + "- " + Lang.get("stageEditorStageEvents") + " -\n";

            if (context.getSessionData(pref + CK.S_START_EVENT) == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorStartEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - " + Lang.get("stageEditorStartEvent") + " (" + AQUA + ((String) context.getSessionData(pref + CK.S_START_EVENT)) + YELLOW + ")\n";
            }

            if (context.getSessionData(pref + CK.S_FINISH_EVENT) == null) {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorFinishEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - " + Lang.get("stageEditorFinishEvent") + " (" + AQUA + ((String) context.getSessionData(pref + CK.S_FINISH_EVENT)) + YELLOW + ")\n";
            }

            if (context.getSessionData(pref + CK.S_DEATH_EVENT) == null) {
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorDeathEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - " + Lang.get("stageEditorDeathEvent") + " (" + AQUA + ((String) context.getSessionData(pref + CK.S_DEATH_EVENT)) + YELLOW + ")\n";
            }

            if (context.getSessionData(pref + CK.S_DISCONNECT_EVENT) == null) {
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("stageEditorDisconnectEvent") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - " + Lang.get("stageEditorDisconnectEvent") + " (" + AQUA + ((String) context.getSessionData(pref + CK.S_DISCONNECT_EVENT)) + YELLOW + ")\n";
            }

            if (context.getSessionData(pref + CK.S_CHAT_EVENTS) == null) {
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("stageEditorChatEvents") + " (" + Lang.get("noneSet") + ")\n";
            } else {

                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - " + Lang.get("stageEditorChatEvents") + "\n";
                LinkedList<String> chatEvents = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENTS);
                LinkedList<String> chatEventTriggers = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS);

                for (String event : chatEvents) {
                    text += AQUA + "    - " + event + BLUE + " (" + Lang.get("stageEditorTriggeredBy") + ": \"" + chatEventTriggers.get(chatEvents.indexOf(event)) + "\")\n";
                }

            }

            text += BLUE + "" + BOLD + "6" + RESET + BLUE + " - " + Lang.get("back");

            return text;
        }

        /*
         en.put("stageEditorStageEvents", "Stage Events");
         en.put("stageEditorStartEvent", "Start Event");
         en.put("stageEditorFinishEvent", "Finish Event");
         en.put("stageEditorChatEvents", "Chat Events");
         en.put("stageEditorDeathEvent", "Death Event");
         en.put("stageEditorDisconnectEvent", "Disconnect Event");
         */
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new StartEventPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                return new FinishEventPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new DeathEventPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                return new DisconnectEventPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                return new ChatEventPrompt();
            } else if (input.equalsIgnoreCase("6")) {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new EventListPrompt();
            }

        }

    }

    private class StartEventPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = DARKGREEN + "- " + Lang.get("stageEditorStartEvent") + " -\n";
            if (questFactory.quests.events.isEmpty()) {
                text += RED + "- None";
            } else {
                for (Event e : questFactory.quests.events) {
                    text += GREEN + "- " + e.getName() + "\n";
                }
            }

            return text + YELLOW + Lang.get("stageEditorEventsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                Event found = null;

                for (Event e : questFactory.quests.events) {

                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }

                }

                if (found == null) {
                    player.sendMessage(RED + input + YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
                    return new StartEventPrompt();
                } else {
                    context.setSessionData(pref + CK.S_START_EVENT, found.getName());
                    return new EventListPrompt();
                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_START_EVENT, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorStartEventCleared"));
                return new EventListPrompt();
            } else {
                return new StartEventPrompt();
            }

        }
    }

    private class FinishEventPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = DARKGREEN + "- " + Lang.get("stageEditorFinishEvent") + " -\n";
            if (questFactory.quests.events.isEmpty()) {
                text += RED + "- " + Lang.get("none");
            } else {
                for (Event e : questFactory.quests.events) {
                    text += GREEN + "- " + e.getName() + "\n";
                }
            }

            return text + YELLOW + Lang.get("stageEditorEventsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                Event found = null;

                for (Event e : questFactory.quests.events) {

                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }

                }

                if (found == null) {
                    player.sendMessage(RED + input + YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
                    return new FinishEventPrompt();
                } else {
                    context.setSessionData(pref + CK.S_FINISH_EVENT, found.getName());
                    return new EventListPrompt();
                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_FINISH_EVENT, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorFinishEventCleared"));
                return new EventListPrompt();
            } else {
                return new FinishEventPrompt();
            }

        }
    }

    private class DeathEventPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = DARKGREEN + "- " + Lang.get("stageEditorDeathEvent") + " -\n";
            if (questFactory.quests.events.isEmpty()) {
                text += RED + "- None";
            } else {
                for (Event e : questFactory.quests.events) {
                    text += GREEN + "- " + e.getName() + "\n";
                }
            }

            return text + YELLOW + Lang.get("stageEditorEventsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                Event found = null;

                for (Event e : questFactory.quests.events) {

                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }

                }

                if (found == null) {
                    player.sendMessage(RED + input + YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
                    return new DeathEventPrompt();
                } else {
                    context.setSessionData(pref + CK.S_DEATH_EVENT, found.getName());
                    return new EventListPrompt();
                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_DEATH_EVENT, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorDeathEventCleared"));
                return new EventListPrompt();
            } else {
                return new DeathEventPrompt();
            }

        }
    }

    private class DisconnectEventPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = DARKGREEN + "- " + Lang.get("stageEditorDisconnectEvent") + " -\n";
            if (questFactory.quests.events.isEmpty()) {
                text += RED + "- " + Lang.get("none");
            } else {
                for (Event e : questFactory.quests.events) {
                    text += GREEN + "- " + e.getName() + "\n";
                }
            }

            return text + YELLOW + Lang.get("stageEditorEventsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                Event found = null;

                for (Event e : questFactory.quests.events) {

                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }

                }

                if (found == null) {
                    player.sendMessage(RED + input + YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
                    return new DisconnectEventPrompt();
                } else {
                    context.setSessionData(pref + CK.S_DISCONNECT_EVENT, found.getName());
                    return new EventListPrompt();
                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_DISCONNECT_EVENT, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorDisconnectEventCleared"));
                return new EventListPrompt();
            } else {
                return new DisconnectEventPrompt();
            }

        }
    }

    private class ChatEventPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = DARKGREEN + "- " + Lang.get("stageEditorChatEvents") + " -\n";
            if (questFactory.quests.events.isEmpty()) {
                text += RED + "- " + Lang.get("none");
            } else {
                for (Event e : questFactory.quests.events) {
                    text += GREEN + "- " + e.getName() + "\n";
                }
            }

            return text + YELLOW + Lang.get("stageEditorChatEventsPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                Event found = null;

                for (Event e : questFactory.quests.events) {

                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }

                }

                if (found == null) {
                    player.sendMessage(RED + input + YELLOW + " " + Lang.get("stageEditorInvalidEvent"));
                    return new ChatEventPrompt();
                } else {
                    context.setSessionData(pref + CK.S_CHAT_TEMP_EVENT, found.getName());
                    return new ChatEventTriggerPrompt();
                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_CHAT_EVENTS, null);
                context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorChatEventsCleared"));
                return new EventListPrompt();
            } else {
                return new ChatEventPrompt();
            }

        }
    }

    private class ChatEventTriggerPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String tempEvent = (String) context.getSessionData(pref + CK.S_CHAT_TEMP_EVENT);

            String text = GOLD + "- " + Lang.get("stageEditorChatTrigger") + " -\n";
            text += YELLOW + Lang.get("stageEditorChatEventsTriggerPromptA") + " " + AQUA + tempEvent + " " + YELLOW + Lang.get("stageEditorChatEventsTriggerPromptB");

            return text;
        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {

                if (context.getSessionData(pref + CK.S_CHAT_EVENTS) == null) {

                    LinkedList<String> chatEvents = new LinkedList<String>();
                    LinkedList<String> chatEventTriggers = new LinkedList<String>();

                    String event = (String) context.getSessionData(pref + CK.S_CHAT_TEMP_EVENT);

                    chatEvents.add(event);
                    chatEventTriggers.add(input.trim());

                    context.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
                    context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);

                    return new EventListPrompt();

                } else {

                    LinkedList<String> chatEvents = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENTS);
                    LinkedList<String> chatEventTriggers = (LinkedList<String>) context.getSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS);

                    String event = (String) context.getSessionData(pref + CK.S_CHAT_TEMP_EVENT);

                    chatEvents.add(event);
                    chatEventTriggers.add(input.trim());

                    context.setSessionData(pref + CK.S_CHAT_EVENTS, chatEvents);
                    context.setSessionData(pref + CK.S_CHAT_EVENT_TRIGGERS, chatEventTriggers);

                    return new EventListPrompt();

                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new EventListPrompt();
            } else {
                return new ChatEventTriggerPrompt();
            }

        }
    }

    private class DelayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorDelayPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
            if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_DELAY, null);
                player.sendMessage(GREEN + Lang.get("stageEditorDelayCleared"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }

            long l;

            try {

                l = Long.parseLong(input);

            } catch (NumberFormatException e) {
                //returns -1 if incorrect input
                l = MiscUtil.getTimeFromString(input);

                if (l == -1) {
                    player.sendMessage(RED + Lang.get("stageEditorNoNumber"));
                    return new DelayPrompt();
                }
            }

            if (l < 1000) {
                player.sendMessage(RED + Lang.get("stageEditorInvalidDelay"));
                return new DelayPrompt();
            } else {
                context.setSessionData(pref + CK.S_DELAY, l);
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }

        }
    }

    private class DelayMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorDelayMessagePrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                context.setSessionData(pref + CK.S_DELAY_MESSAGE, input);
                return new CreateStagePrompt(stageNum, questFactory, citizens);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_DELAY_MESSAGE, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorDelayMessageCleared"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new DelayMessagePrompt();
            }

        }
    }

    private class DenizenPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

        	//TODO Figure out why Blackvein left this unused
            @SuppressWarnings("unused")
			String text = DARKAQUA + "- " + Lang.get("stageEditorDenizenScript") + " -";

            for (String s : ScriptRegistry._getScriptNames()) {
                text += AQUA + "- " + s + "\n";
            }
            
            return YELLOW + Lang.get("stageEditorScriptPrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                if (ScriptRegistry.containsScript(input)) {

                    context.setSessionData(pref + CK.S_DENIZEN, ScriptRegistry.getScriptContainer(input).getName());
                    return new CreateStagePrompt(stageNum, questFactory, citizens);

                } else {

                    player.sendMessage(RED + Lang.get("stageEditorInvalidScript"));
                    return new DenizenPrompt();

                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_DENIZEN, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorDenizenCleared"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }

        }
    }

    private class DeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GREEN + "" + BOLD + "1" + RESET + "" + GREEN + " - " + Lang.get("yesWord") + "\n";
            text += GREEN + "" + BOLD + "2" + RESET + "" + GREEN + " - " + Lang.get("noWord");
            return RED + Lang.get("stageEditorConfirmStageDelete") + "\n" + YELLOW + Lang.get("stageEditorStage") + " " + stageNum + ": " + context.getSessionData(CK.Q_NAME) + RED + "\n(" + Lang.get("stageEditorConfirmStageNote") + ")\n" + text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("Yes")) {
                StagesPrompt.deleteStage(context, stageNum);
                player.sendMessage(YELLOW + Lang.get("stageEditorDeleteSucces"));
                return new StagesPrompt(questFactory);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("No")) {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                player.sendMessage(RED + Lang.get("invalidOption"));
                return new DeletePrompt();
            }

        }
    }

    private class StartMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorStartMessagePrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                context.setSessionData(pref + CK.S_START_MESSAGE, input);
                return new CreateStagePrompt(stageNum, questFactory, citizens);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_START_MESSAGE, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorStartMessageCleared"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        }

    }

    private class CompleteMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + Lang.get("stageEditorCompleteMessagePrompt");

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                context.setSessionData(pref + CK.S_COMPLETE_MESSAGE, input);
                return new CreateStagePrompt(stageNum, questFactory, citizens);

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_COMPLETE_MESSAGE, null);
                player.sendMessage(YELLOW + Lang.get("stageEditorCompleteMessageCleared"));
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        }

    }

    private class CustomObjectivesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = PINK + "- " + Lang.get("stageEditorCustom") + " -\n";
            if (questFactory.quests.customObjectives.isEmpty()) {
                text += BOLD + "" + PURPLE + "(" + Lang.get("stageEditorNoModules") + ")";
            } else {
                for (CustomObjective co : questFactory.quests.customObjectives) {
                    text += PURPLE + " - " + co.getName() + "\n";
                }
            }

            return text + YELLOW + Lang.get("stageEditorCustomPrompt");
        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {

                CustomObjective found = null;
                for (CustomObjective co : questFactory.quests.customObjectives) {
                    if (co.getName().equalsIgnoreCase(input)) {
                        found = co;
                        break;
                    }
                }

                if (found == null) {
                    for (CustomObjective co : questFactory.quests.customObjectives) {
                        if (co.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = co;
                            break;
                        }
                    }
                }

                if (found != null) {

                    if (context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES) != null) {
                        LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
                        LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
                        LinkedList<Integer> countList = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
                        if (list.contains(found.getName()) == false) {
                            list.add(found.getName());
                            datamapList.add(found.datamap);
                            countList.add(-999);
                            context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
                            context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
                        } else {
                            context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorCustomAlreadyAdded"));
                            return new CustomObjectivesPrompt();
                        }
                    } else {
                        LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
                        LinkedList<Integer> countList = new LinkedList<Integer>();
                        datamapList.add(found.datamap);
                        countList.add(-999);
                        LinkedList<String> list = new LinkedList<String>();
                        list.add(found.getName());
                        context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, list);
                        context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, datamapList);
                        context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT, countList);
                    }

                    //Send user to the count prompt / custom data prompt if there is any needed
                    if (found.isEnableCount()) {
                        return new CustomObjectiveCountPrompt();
                    }

                    if (found.datamap.isEmpty() == false) {

                        context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.descriptions);
                        return new ObjectiveCustomDataListPrompt();

                    }
                    //

                } else {
                    context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorModuleNotFound"));
                    return new CustomObjectivesPrompt();
                }

            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES, null);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA, null);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(YELLOW + Lang.get("stageEditorCustomCleared"));
            }

            return new CreateStagePrompt(stageNum, questFactory, citizens);

        }

    }

    private class CustomObjectiveCountPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = BOLD + "" + AQUA + "- ";

            @SuppressWarnings("unchecked")
			LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);

            String objName = list.getLast();

            text += objName + " -\n";

            CustomObjective found = null;
            for (CustomObjective co : questFactory.quests.customObjectives) {

                if (co.getName().equals(objName)) {
                    found = co;
                    break;
                }

            }

            text += BLUE + found.getCountPrompt().toString() + "\n\n";

            return text;

        }

        @SuppressWarnings("unchecked")
		@Override
        public Prompt acceptInput(ConversationContext context, String input) {

            try {

                int num = Integer.parseInt(input);
                LinkedList<Integer> counts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_COUNT);
                counts.set(counts.size() - 1, num);

                LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
                String objName = list.getLast();

                CustomObjective found = null;
                for (CustomObjective co : questFactory.quests.customObjectives) {

                    if (co.getName().equals(objName)) {
                        found = co;
                        break;
                    }

                }

                if (found.datamap.isEmpty() == false) {
                    context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, found.descriptions);
                    return new ObjectiveCustomDataListPrompt();
                } else {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                }

            } catch (NumberFormatException e) {
                context.getForWhom().sendRawMessage(RED + Lang.get("stageEditorNoNumber"));
                return new CustomObjectiveCountPrompt();
            }

        }

    }

    private class ObjectiveCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
		@Override
        public String getPromptText(ConversationContext context) {

            String text = BOLD + "" + AQUA + "- ";

            LinkedList<String> list = (LinkedList<String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES);
            LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);

            String objName = list.getLast();
            Map<String, Object> datamap = datamapList.getLast();

            text += objName + " -\n";
            int index = 1;

            LinkedList<String> datamapKeys = new LinkedList<String>();
            for (String key : datamap.keySet()) {
                datamapKeys.add(key);
            }
            Collections.sort(datamapKeys);

            for (String dataKey : datamapKeys) {

                text += BOLD + "" + DARKBLUE + index + " - " + RESET + BLUE + dataKey;
                if (datamap.get(dataKey) != null) {
                    text += GREEN + " (" + (String) datamap.get(dataKey) + ")\n";
                } else {
                    text += RED + " (" + Lang.get("valRequired") + ")\n";
                }

                index++;

            }

            text += BOLD + "" + DARKBLUE + index + " - " + AQUA + Lang.get("finish");

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            @SuppressWarnings("unchecked")
			LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
            Map<String, Object> datamap = datamapList.getLast();

            int numInput;

            try {
                numInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                return new ObjectiveCustomDataListPrompt();
            }

            if (numInput < 1 || numInput > datamap.size() + 1) {
                return new ObjectiveCustomDataListPrompt();
            }

            if (numInput < datamap.size() + 1) {

                LinkedList<String> datamapKeys = new LinkedList<String>();
                for (String key : datamap.keySet()) {
                    datamapKeys.add(key);
                }
                Collections.sort(datamapKeys);

                String selectedKey = datamapKeys.get(numInput - 1);
                context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, selectedKey);
                return new ObjectiveCustomDataPrompt();

            } else {

                if (datamap.containsValue(null)) {
                    return new ObjectiveCustomDataListPrompt();
                } else {
                    context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS, null);
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                }

            }

        }

    }

    private class ObjectiveCustomDataPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = "";
            String temp = (String) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP);
            @SuppressWarnings("unchecked")
			Map<String, String> descriptions = (Map<String, String>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_DESCRIPTIONS);
            if (descriptions.get(temp) != null) {
                text += GOLD + descriptions.get(temp) + "\n";
            }

            String msg = Lang.get("stageEditorCustomDataPrompt");
            msg = msg.replaceAll("<data>", BOLD + temp + RESET + YELLOW);
            text += YELLOW + msg;
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            @SuppressWarnings("unchecked")
			LinkedList<Map<String, Object>> datamapList = (LinkedList<Map<String, Object>>) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            datamap.put((String) context.getSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP), input);
            context.setSessionData(pref + CK.S_CUSTOM_OBJECTIVES_DATA_TEMP, null);
            return new ObjectiveCustomDataListPrompt();
        }

    }

}
