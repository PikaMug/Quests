package me.blackvein.quests.prompts;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import me.ThaH3lper.com.LoadBosses.LoadBoss;
import me.blackvein.quests.ColorUtil;
import me.blackvein.quests.Event;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quester;
import me.blackvein.quests.Quests;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.citizensnpcs.api.CitizensPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateStagePrompt extends FixedSetPrompt implements ColorUtil {

    private final int stageNum;
    private final String pref;
    private final CitizensPlugin citizens;
    private final QuestFactory questFactory;

    public CreateStagePrompt(int stageNum, QuestFactory qf, CitizensPlugin cit) {

        super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22");
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
        this.citizens = cit;
        this.questFactory = qf;

    }

    @Override
    public String getPromptText(ConversationContext context) {

        context.setSessionData(pref, Boolean.TRUE);

        String text = PINK + "- " + AQUA + (String) context.getSessionData("questName") + PINK + " | Stage " + PURPLE + stageNum + PINK + " -\n";

        if (context.getSessionData(pref + "breakIds") == null) {
            text += PINK + "" + BOLD + "1 " + RESET + PURPLE + "- Break Blocks " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "1 " + RESET + PURPLE + "- Break Blocks\n";

            LinkedList<Integer> ids = (LinkedList<Integer>) context.getSessionData(pref + "breakIds");
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "breakAmounts");

            for (int i = 0; i < ids.size(); i++) {
                text += GRAY + "    - " + BLUE + Quester.prettyItemString(ids.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
            }

        }


        if (context.getSessionData(pref + "damageIds") == null) {
            text += PINK + "" + BOLD + "2 " + RESET + PURPLE + "- Damage Blocks " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "2 " + RESET + PURPLE + "- Damage Blocks\n";

            LinkedList<Integer> ids = (LinkedList<Integer>) context.getSessionData(pref + "damageIds");
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "damageAmounts");

            for (int i = 0; i < ids.size(); i++) {
                text += GRAY + "    - " + BLUE + Quester.prettyItemString(ids.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
            }

        }


        if (context.getSessionData(pref + "placeIds") == null) {
            text += PINK + "" + BOLD + "3 " + RESET + PURPLE + "- Place Blocks " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "3 " + RESET + PURPLE + "- Place Blocks\n";

            LinkedList<Integer> ids = (LinkedList<Integer>) context.getSessionData(pref + "placeIds");
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "placeAmounts");

            for (int i = 0; i < ids.size(); i++) {
                text += GRAY + "    - " + BLUE + Quester.prettyItemString(ids.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
            }

        }


        if (context.getSessionData(pref + "useIds") == null) {
            text += PINK + "" + BOLD + "4 " + RESET + PURPLE + "- Use Blocks " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "4 " + RESET + PURPLE + "- Use Blocks\n";

            LinkedList<Integer> ids = (LinkedList<Integer>) context.getSessionData(pref + "useIds");
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "useAmounts");

            for (int i = 0; i < ids.size(); i++) {
                text += GRAY + "    - " + BLUE + Quester.prettyItemString(ids.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
            }

        }


        if (context.getSessionData(pref + "cutIds") == null) {
            text += PINK + "" + BOLD + "5 " + RESET + PURPLE + "- Cut Blocks " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "5 " + RESET + PURPLE + "- Cut Blocks\n";

            LinkedList<Integer> ids = (LinkedList<Integer>) context.getSessionData(pref + "cutIds");
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "cutAmounts");

            for (int i = 0; i < ids.size(); i++) {
                text += GRAY + "    - " + BLUE + Quester.prettyItemString(ids.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
            }

        }

        if (context.getSessionData(pref + "fish") == null) {
            text += PINK + "" + BOLD + "6 " + RESET + PURPLE + "- Catch Fish " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            Integer fish = (Integer) context.getSessionData(pref + "fish");
            text += PINK + "" + BOLD + "6 " + RESET + PURPLE + "- Catch Fish " + GRAY + "(" + AQUA + fish + " fish" + GRAY + ")\n";
        }

        if (context.getSessionData(pref + "playerKill") == null) {
            text += PINK + "" + BOLD + "7 " + RESET + PURPLE + "- Kill Players " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            Integer fish = (Integer) context.getSessionData(pref + "playerKill");
            text += PINK + "" + BOLD + "7 " + RESET + PURPLE + "- Kill Players " + GRAY + "(" + AQUA + fish + " players" + GRAY + ")\n";
        }

        if (context.getSessionData(pref + "enchantTypes") == null) {
            text += PINK + "" + BOLD + "8 " + RESET + PURPLE + "- Enchant Items " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "8 " + RESET + PURPLE + "- Enchant Items\n";

            LinkedList<String> enchants = (LinkedList<String>) context.getSessionData(pref + "enchantTypes");
            LinkedList<Integer> items = (LinkedList<Integer>) context.getSessionData(pref + "enchantIds");
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "enchantAmounts");

            for (int i = 0; i < enchants.size(); i++) {
                text += GRAY + "    - " + BLUE + Quester.prettyItemString(items.get(i)) + GRAY + " with " + AQUA + Quester.prettyString(enchants.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
            }

        }

        if (questFactory.quests.citizens != null) {

            if (context.getSessionData(pref + "deliveryItems") == null) {
                text += PINK + "" + BOLD + "9 " + RESET + PURPLE + "- Deliver Items " + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "9 " + RESET + PURPLE + "- Deliver Items\n";

                LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + "deliveryNPCs");
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(pref + "deliveryItems");

                for (int i = 0; i < npcs.size(); i++) {
                    text += GRAY + "    - " + BLUE + ItemUtil.getName(items.get(i)) + GRAY + " x " + AQUA + items.get(i).getAmount() + GRAY + " to " + DARKAQUA + citizens.getNPCRegistry().getById(npcs.get(i)).getName() + "\n";
                }

            }

        } else {
            text += GRAY + "" + BOLD + "9 " + RESET + GRAY + "- Deliver Items " + GRAY + "(Citizens not installed)\n";
        }

        if (questFactory.quests.citizens != null) {

            if (context.getSessionData(pref + "npcIdsToTalkTo") == null) {
                text += PINK + "" + BOLD + "10 " + RESET + PURPLE + "- Talk to NPCs " + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "10 " + RESET + PURPLE + "- Talk to NPCs\n";

                LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + "npcIdsToTalkTo");

                for (int i = 0; i < npcs.size(); i++) {
                    text += GRAY + "    - " + BLUE + citizens.getNPCRegistry().getById(npcs.get(i)).getName() + "\n";
                }

            }

        } else {
            text += GRAY + "" + BOLD + "10 " + RESET + GRAY + "- Talk to NPCs " + GRAY + "(Citizens not installed)\n";
        }

        if (questFactory.quests.citizens != null) {

            if (context.getSessionData(pref + "npcIdsToKill") == null) {
                text += PINK + "" + BOLD + "11 " + RESET + PURPLE + "- Kill NPCs " + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "11 " + RESET + PURPLE + "- Kill NPCs\n";

                LinkedList<Integer> npcs = (LinkedList<Integer>) context.getSessionData(pref + "npcIdsToKill");
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + "npcAmountsToKill");

                for (int i = 0; i < npcs.size(); i++) {
                    text += GRAY + "    - " + BLUE + citizens.getNPCRegistry().getById(npcs.get(i)).getName() + GRAY + " x " + AQUA + amounts.get(i) + "\n";
                }

            }

        } else {
            text += GRAY + "" + BOLD + "11 " + RESET + GRAY + "- Kill NPCs " + GRAY + "(Citizens not installed)\n";
        }
        
        if(Quests.epicBoss != null){
        
            if (context.getSessionData(pref + "bossIds") == null) {
                text += PINK + "" + BOLD + "12 " + RESET + PURPLE + "- Kill Bosses " + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "12 " + RESET + PURPLE + "- Kill Bosses\n";

                LinkedList<String> bosses = (LinkedList<String>) context.getSessionData(pref + "bossIds");
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "bossAmounts");

                for (int i = 0; i < bosses.size(); i++) {
                    text += GRAY + "    - " + ITALIC + AQUA + bosses.get(i) + RESET + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }

            }
            
        }else{
            text += GRAY + "" + BOLD + "12 " + RESET + GRAY + "- Kill Bosses " + GRAY + "(EpicBoss not installed)\n";
        }

        if (context.getSessionData(pref + "mobTypes") == null) {
            text += PINK + "" + BOLD + "13 " + RESET + PURPLE + "- Kill Mobs " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "13 " + RESET + PURPLE + "- Kill Mobs\n";

            LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + "mobTypes");
            LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + "mobAmounts");

            if (context.getSessionData(pref + "killLocations") == null) {

                for (int i = 0; i < mobs.size(); i++) {
                    text += GRAY + "    - " + AQUA + Quester.prettyString(mobs.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + "\n";
                }

            } else {

                LinkedList<String> locs = (LinkedList<String>) context.getSessionData(pref + "killLocations");
                LinkedList<Integer> radii = (LinkedList<Integer>) context.getSessionData(pref + "killLocationRadii");
                LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + "killLocationNames");

                for (int i = 0; i < mobs.size(); i++) {
                    text += GRAY + "    - " + BLUE + Quester.prettyString(mobs.get(i)) + GRAY + " x " + DARKAQUA + amnts.get(i) + GRAY + " within " + PURPLE + radii.get(i) + GRAY + " blocks of " + YELLOW + names.get(i) + " (" + locs.get(i) + ")\n";
                }

            }

        }

        if (context.getSessionData(pref + "reachLocations") == null) {
            text += PINK + "" + BOLD + "14 " + RESET + PURPLE + "- Reach Locations " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "14 " + RESET + PURPLE + "- Reach Locations\n";

            LinkedList<String> locations = (LinkedList<String>) context.getSessionData(pref + "reachLocations");
            LinkedList<Integer> radii = (LinkedList<Integer>) context.getSessionData(pref + "reachLocationRadii");
            LinkedList<String> names = (LinkedList<String>) context.getSessionData(pref + "reachLocationNames");

            for (int i = 0; i < locations.size(); i++) {
                text += GRAY + "    - Reach within " + BLUE + radii.get(i) + GRAY + " blocks of " + AQUA + names.get(i) + GRAY + " (" + DARKAQUA + locations.get(i) + GRAY + ")\n";
            }



        }

        if (context.getSessionData(pref + "tameTypes") == null) {
            text += PINK + "" + BOLD + "15 " + RESET + PURPLE + "- Tame Mobs " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            
            text += PINK + "" + BOLD + "15 " + RESET + PURPLE + "- Tame Mobs\n";

            LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + "tameTypes");
            LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + "tameAmounts");

            for (int i = 0; i < mobs.size(); i++) {
                text += GRAY + "    - " + BLUE + mobs.get(i) + GRAY + " x " + AQUA + amounts.get(i) + "\n";
            }

        }

        if (context.getSessionData(pref + "shearColors") == null) {
            text += PINK + "" + BOLD + "16 " + RESET + PURPLE + "- Shear Sheep " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "16 " + RESET + PURPLE + "- Shear Sheep\n";

            LinkedList<String> colors = (LinkedList<String>) context.getSessionData(pref + "shearColors");
            LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + "shearAmounts");

            for (int i = 0; i < colors.size(); i++) {
                text += GRAY + "    - " + BLUE + colors.get(i) + GRAY + " x " + AQUA + amounts.get(i) + "\n";
            }

        }

        if (context.getSessionData(pref + "event") == null) {
            text += PINK + "" + BOLD + "17 " + RESET + PURPLE + "- Event " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "17 " + RESET + PURPLE + "- Event " + GRAY + "(" + AQUA + context.getSessionData(pref + "event") + GRAY + ")\n";
        }

        if (context.getSessionData(pref + "delay") == null) {
            text += PINK + "" + BOLD + "18 " + RESET + PURPLE + "- Delay " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            long time = (Long) context.getSessionData(pref + "delay");
            text += PINK + "" + BOLD + "18 " + RESET + PURPLE + "- Delay " + GRAY + "(" + AQUA + Quests.getTime(time) + GRAY + ")\n";
        }

        if (context.getSessionData(pref + "delay") == null) {
            text += GRAY + "" + BOLD + "19 " + RESET + GRAY + "- Delay Message " + GRAY + "(No delay set)\n";
        } else if (context.getSessionData(pref + "delayMessage") == null) {
            text += PINK + "" + BOLD + "19 " + RESET + PURPLE + "- Delay Message " + GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += PINK + "" + BOLD + "19 " + RESET + PURPLE + "- Delay Message " + GRAY + "(" + AQUA + "\"" + context.getSessionData(pref + "delayMessage") + "\"" + GRAY + ")\n";
        }


        if (questFactory.quests.denizen == null) {
            text += GRAY + "" + BOLD + "19 " + RESET + GRAY + "- Denizen Script " + GRAY + "(Denizen not installed)\n";
        } else {

            if (context.getSessionData(pref + "denizen") == null) {
                text += GRAY + "" + BOLD + "20 " + RESET + PURPLE + "- Denizen Script " + GRAY + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += PINK + "" + BOLD + "20 " + RESET + PURPLE + "- Denizen Script " + GRAY + "(" + AQUA + context.getSessionData(pref + "denizen") + GRAY + "\n";
            }

        }

        text += RED + "" + BOLD + "21 " + RESET + PURPLE + "- Delete Stage\n";
        text += GREEN + "" + BOLD + "22 " + RESET + PURPLE + "- Done\n";

        return text;

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
                context.getForWhom().sendRawMessage(RED + "Citizens is not installed!");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        } else if (input.equalsIgnoreCase("10")) {
            if (questFactory.quests.citizens != null) {
                return new NPCIDsToTalkToPrompt();
            } else {
                context.getForWhom().sendRawMessage(RED + "Citizens is not installed!");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        } else if (input.equalsIgnoreCase("11")) {
            if (questFactory.quests.citizens != null) {
                return new NPCKillListPrompt();
            } else {
                context.getForWhom().sendRawMessage(RED + "Citizens is not installed!");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        } else if (input.equalsIgnoreCase("12")) {
            if (Quests.epicBoss != null) {
                return new EpicBossListPrompt();
            } else {
                context.getForWhom().sendRawMessage(RED + "EpicBoss is not installed!");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }
        } else if (input.equalsIgnoreCase("13")) {
            return new MobListPrompt();
        } else if (input.equalsIgnoreCase("14")) {
            return new ReachListPrompt();
        } else if (input.equalsIgnoreCase("15")) {
            return new TameListPrompt();
        } else if (input.equalsIgnoreCase("16")) {
            return new ShearListPrompt();
        } else if (input.equalsIgnoreCase("17")) {
            return new EventPrompt();
        } else if (input.equalsIgnoreCase("18")) {
            return new DelayPrompt();
        } else if (input.equalsIgnoreCase("19")) {
            if (context.getSessionData(pref + "delay") == null) {
                context.getForWhom().sendRawMessage(RED + "You must set a delay first!");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new DelayMessagePrompt();
            }
        } else if (input.equalsIgnoreCase("20")) {
            if (questFactory.quests.denizen == null) {
                context.getForWhom().sendRawMessage(RED + "Denizen is not installed!");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new DenizenPrompt();
            }
        } else if (input.equalsIgnoreCase("21")) {
            return new DeletePrompt();
        } else if (input.equalsIgnoreCase("22")) {
            return new StagesPrompt(questFactory);
        } else {
            return new CreateStagePrompt(stageNum, questFactory, citizens);
        }

    }

    private class BreakBlockListPrompt extends FixedSetPrompt {

        public BreakBlockListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Break Blocks -\n";
            if (context.getSessionData(pref + "breakIds") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs (None set)\n";
                text += GRAY + "2 - Set block amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs\n";
                for (Integer i : getBlockIds(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                }

                if (context.getSessionData(pref + "breakAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set block amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set block amounts\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new BreakBlockIdsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "breakIds") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set block IDs first!");
                    return new BreakBlockListPrompt();
                } else {
                    return new BreakBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Break blocks objective cleared.");
                context.setSessionData(pref + "breakIds", null);
                context.setSessionData(pref + "breakAmounts", null);
                return new BreakBlockListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "breakIds") != null) {
                    one = ((List<Integer>) context.getSessionData(pref + "breakIds")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "breakAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "breakAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "block IDs list" + RED + " and " + GOLD + " block amounts list " + RED + "are not the same size!");
                    return new BreakBlockListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getBlockIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "breakIds");
        }

        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "breakAmounts");
        }
    }

    private class BreakBlockIdsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter block IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Material.getMaterial(Integer.parseInt(s)) != null) {

                            if (ids.contains(Integer.parseInt(s)) == false) {
                                ids.add(Integer.parseInt(s));
                            } else {
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new BreakBlockIdsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid block ID!");
                            return new BreakBlockIdsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new BreakBlockIdsPrompt();
                    }

                }

                context.setSessionData(pref + "breakIds", ids);

            }

            return new BreakBlockListPrompt();

        }
    }

    private class BreakBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter block amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new BreakBlockAmountsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new BreakBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "breakAmounts", amounts);

            }

            return new BreakBlockListPrompt();

        }
    }

    private class DamageBlockListPrompt extends FixedSetPrompt {

        public DamageBlockListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Damage Blocks -\n";
            if (context.getSessionData(pref + "damageIds") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs (None set)\n";
                text += GRAY + "2 - Set damage amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs\n";
                for (Integer i : getBlockIds(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                }

                if (context.getSessionData(pref + "damageAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set damage amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set damage amounts\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new DamageBlockIdsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "damageIds") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set block IDs first!");
                    return new DamageBlockListPrompt();
                } else {
                    return new DamageBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Damage blocks objective cleared.");
                context.setSessionData(pref + "damageIds", null);
                context.setSessionData(pref + "damageAmounts", null);
                return new DamageBlockListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "damageIds") != null) {
                    one = ((List<Integer>) context.getSessionData(pref + "damageIds")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "damageAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "damageAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "block IDs list" + RED + " and " + GOLD + " damage amounts list " + RED + "are not the same size!");
                    return new DamageBlockListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getBlockIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "damageIds");
        }

        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "damageAmounts");
        }
    }

    private class DamageBlockIdsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter block IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Material.getMaterial(Integer.parseInt(s)) != null) {

                            if (ids.contains(Integer.parseInt(s)) == false) {
                                ids.add(Integer.parseInt(s));
                            } else {
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new DamageBlockIdsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid block ID!");
                            return new DamageBlockIdsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new DamageBlockIdsPrompt();
                    }

                }

                context.setSessionData(pref + "damageIds", ids);

            }

            return new DamageBlockListPrompt();

        }
    }

    private class DamageBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter damage amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new DamageBlockAmountsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new DamageBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "damageAmounts", amounts);

            }

            return new DamageBlockListPrompt();

        }
    }

    private class PlaceBlockListPrompt extends FixedSetPrompt {

        public PlaceBlockListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Place Blocks -\n";
            if (context.getSessionData(pref + "placeIds") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs (None set)\n";
                text += GRAY + "2 - Set place amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs\n";
                for (Integer i : getBlockIds(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                }

                if (context.getSessionData(pref + "placeAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set place amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set place amounts\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new PlaceBlockIdsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "placeIds") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set block IDs first!");
                    return new PlaceBlockListPrompt();
                } else {
                    return new PlaceBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Place blocks objective cleared.");
                context.setSessionData(pref + "placeIds", null);
                context.setSessionData(pref + "placeAmounts", null);
                return new PlaceBlockListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "placeIds") != null) {
                    one = ((List<Integer>) context.getSessionData(pref + "placeIds")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "placeAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "placeAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "block IDs list" + RED + " and " + GOLD + " place amounts list " + RED + "are not the same size!");
                    return new PlaceBlockListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getBlockIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "placeIds");
        }

        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "placeAmounts");
        }
    }

    private class PlaceBlockIdsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter block IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Material.getMaterial(Integer.parseInt(s)) != null) {

                            if (ids.contains(Integer.parseInt(s)) == false) {
                                ids.add(Integer.parseInt(s));
                            } else {
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new PlaceBlockIdsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid block ID!");
                            return new PlaceBlockIdsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new PlaceBlockIdsPrompt();
                    }

                }

                context.setSessionData(pref + "placeIds", ids);

            }

            return new PlaceBlockListPrompt();

        }
    }

    private class PlaceBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter place amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new PlaceBlockAmountsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new PlaceBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "placeAmounts", amounts);

            }

            return new PlaceBlockListPrompt();

        }
    }

    private class UseBlockListPrompt extends FixedSetPrompt {

        public UseBlockListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Use Blocks -\n";
            if (context.getSessionData(pref + "useIds") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs (None set)\n";
                text += GRAY + "2 - Set use amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs\n";
                for (Integer i : getBlockIds(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                }

                if (context.getSessionData(pref + "useAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set use amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set use amounts\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new UseBlockIdsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "useIds") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set block IDs first!");
                    return new UseBlockListPrompt();
                } else {
                    return new UseBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Use blocks objective cleared.");
                context.setSessionData(pref + "useIds", null);
                context.setSessionData(pref + "useAmounts", null);
                return new UseBlockListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "useIds") != null) {
                    one = ((List<Integer>) context.getSessionData(pref + "useIds")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "useAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "useAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "block IDs list" + RED + " and " + GOLD + " use amounts list " + RED + "are not the same size!");
                    return new UseBlockListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getBlockIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "useIds");
        }

        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "useAmounts");
        }
    }

    private class UseBlockIdsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter block IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Material.getMaterial(Integer.parseInt(s)) != null) {

                            if (ids.contains(Integer.parseInt(s)) == false) {
                                ids.add(Integer.parseInt(s));
                            } else {
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new UseBlockIdsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid block ID!");
                            return new UseBlockIdsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new UseBlockIdsPrompt();
                    }

                }

                context.setSessionData(pref + "useIds", ids);

            }

            return new UseBlockListPrompt();

        }
    }

    private class UseBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter use amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new UseBlockAmountsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new UseBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "useAmounts", amounts);

            }

            return new UseBlockListPrompt();

        }
    }

    private class CutBlockListPrompt extends FixedSetPrompt {

        public CutBlockListPrompt() {

            super("1", "2", "3", "4");

        }

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GOLD + "- Cut Blocks -\n";
            if (context.getSessionData(pref + "cutIds") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs (None set)\n";
                text += GRAY + "2 - Set cut amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set block IDs\n";
                for (Integer i : getBlockIds(context)) {

                    text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                }

                if (context.getSessionData(pref + "cutAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set cut amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set cut amounts\n";
                    for (Integer i : getBlockAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new CutBlockIdsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "cutIds") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set block IDs first!");
                    return new CutBlockListPrompt();
                } else {
                    return new CutBlockAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Cut blocks objective cleared.");
                context.setSessionData(pref + "cutIds", null);
                context.setSessionData(pref + "cutAmounts", null);
                return new CutBlockListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "cutIds") != null) {
                    one = ((List<Integer>) context.getSessionData(pref + "cutIds")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "cutAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "cutAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "block IDs list" + RED + " and " + GOLD + " cut amounts list " + RED + "are not the same size!");
                    return new CutBlockListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getBlockIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "cutIds");
        }

        private List<Integer> getBlockAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "cutAmounts");
        }
    }

    private class CutBlockIdsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter block IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Material.getMaterial(Integer.parseInt(s)) != null) {

                            if (ids.contains(Integer.parseInt(s)) == false) {
                                ids.add(Integer.parseInt(s));
                            } else {
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new CutBlockIdsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid block ID!");
                            return new CutBlockIdsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new CutBlockIdsPrompt();
                    }

                }

                context.setSessionData(pref + "cutIds", ids);

            }

            return new CutBlockListPrompt();

        }
    }

    private class CutBlockAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter cut amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new CutBlockAmountsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new CutBlockAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "cutAmounts", amounts);

            }

            return new CutBlockListPrompt();

        }
    }

    private class FishPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter number of fish to catch, or 0 to clear the fish catch objective, or -1 to cancel";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number number) {

            int num = number.intValue();
            Player player = (Player) context.getForWhom();

            if (num < -1) {
                player.sendMessage(RED + "You must enter a positive number!");
                return new FishPrompt();
            } else if (num == 0) {
                context.setSessionData(pref + "fish", null);
            } else if (num > 0) {
                context.setSessionData(pref + "fish", num);
            }

            return new CreateStagePrompt(stageNum, questFactory, citizens);

        }
    }

    private class KillPlayerPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter number of players to kill, or 0 to clear the player kill objective, or -1 to cancel";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number number) {

            int num = number.intValue();
            Player player = (Player) context.getForWhom();

            if (num < -1) {
                player.sendMessage(RED + "You must enter a positive number!");
                return new KillPlayerPrompt();
            } else if (num == 0) {
                context.setSessionData(pref + "playerKill", null);
            } else if (num > 0) {
                context.setSessionData(pref + "playerKill", num);
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

            String text = GOLD + "- Enchant Items -\n";
            if (context.getSessionData(pref + "enchantTypes") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set enchantments (None set)\n";
                text += GRAY + "2 - Set item ids (No enchantments set)\n";
                text += GRAY + "3 - Set enchant amounts (No enchantments set)\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set enchantments\n";
                for (String s : getEnchantTypes(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + "enchantIds") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set item ids (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set item ids\n";
                    for (Integer i : getEnchantItems(context)) {

                        text += GRAY + "    - " + AQUA + Quester.prettyItemString(i) + "\n";

                    }

                }

                if (context.getSessionData(pref + "enchantAmounts") == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set enchant amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set enchant amounts\n";
                    for (int i : getEnchantAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new EnchantTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "enchantTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set enchantments first!");
                    return new EnchantmentListPrompt();
                } else {
                    return new EnchantItemsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + "enchantTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set enchantments first!");
                    return new EnchantmentListPrompt();
                } else {
                    return new EnchantAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + "Enchantment objective cleared.");
                context.setSessionData(pref + "enchantTypes", null);
                context.setSessionData(pref + "enchantIds", null);
                context.setSessionData(pref + "enchantAmounts", null);
                return new EnchantmentListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;
                int three;

                if (context.getSessionData(pref + "enchantTypes") != null) {
                    one = ((List<Integer>) context.getSessionData(pref + "enchantTypes")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "enchantIds") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "enchantIds")).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData(pref + "enchantAmounts") != null) {
                    three = ((List<Integer>) context.getSessionData(pref + "enchantAmounts")).size();
                } else {
                    three = 0;
                }

                if (one == two && two == three) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "enchantments list" + RED + ", " + GOLD + "item id list " + RED + "and " + GOLD + "enchant amount list " + RED + "are not the same size!");
                    return new EnchantmentListPrompt();
                }
            }

            return null;

        }

        private List<String> getEnchantTypes(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "enchantTypes");
        }

        private List<Integer> getEnchantItems(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "enchantIds");
        }

        private List<Integer> getEnchantAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "enchantAmounts");
        }
    }

    private class EnchantTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = PINK + "- " + PURPLE + "Enchantments" + PINK + " -\n";
            for (Enchantment e : Enchantment.values()) {

                text += GREEN + Quester.prettyEnchantmentString(e) + ", ";

            }
            text = text.substring(0, text.length() - 1);

            return text + "\n" + YELLOW + "Enter enchantment names, separating each one by a " + BOLD + "" + ITALIC + "comma" + RESET + "" + YELLOW + ", or enter \'cancel\' to return.";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

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
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new EnchantTypesPrompt();
                            }

                        }

                    }
                    if (valid == false) {
                        context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid enchantment name!");
                        return new EnchantTypesPrompt();
                    }

                }

                context.setSessionData(pref + "enchantTypes", enchs);

            }

            return new EnchantmentListPrompt();

        }
    }

    private class EnchantItemsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter item IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> ids = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Material.getMaterial(Integer.parseInt(s)) != null) {

                            if (ids.contains(Integer.parseInt(s)) == false) {
                                ids.add(Integer.parseInt(s));
                            } else {
                                context.getForWhom().sendRawMessage(RED + " List contains duplicates!");
                                return new EnchantItemsPrompt();
                            }

                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not a valid item ID!");
                            return new EnchantItemsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new EnchantItemsPrompt();
                    }

                }

                context.setSessionData(pref + "enchantIds", ids);

            }

            return new EnchantmentListPrompt();

        }
    }

    private class EnchantAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter enchant amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new EnchantAmountsPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new EnchantAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "enchantAmounts", amounts);

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
            if(context.getSessionData("newItem") != null){
                if(context.getSessionData(pref + "deliveryItems") != null){
                    List<ItemStack> itemRews = getItems(context);
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + "deliveryItems", itemRews);
                }else{
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(pref + "deliveryItems", itemRews);
                }

                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);

            }
            
            String text = GOLD + "- Deliver Items -\n";
            if (context.getSessionData(pref + "deliveryItems") == null) {
                text += GRAY + " (" + Lang.get("noneSet") + ")\n";
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";
                text += GRAY + "2 - Set NPC IDs (No items set)\n";
                if (context.getSessionData(pref + "deliveryMessages") == null) {
                    text += BLUE + "3 - Set delivery messages (None set)\n";
                } else {

                    text += BLUE + "3 - Set delivery messages\n";
                    for (String s : getDeliveryMessages(context)) {

                        text += GRAY + "    - " + AQUA + "\"" + s + "\"";

                    }

                }
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";
            } else {

                for (ItemStack is : getItems(context)) {

                    text += GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";

                }
                
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Add item\n";

                if (context.getSessionData(pref + "deliveryNPCs") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set NPC IDs (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set NPC IDs\n";
                    for (int i : getDeliveryNPCs(context)) {

                        text += GRAY + "    - " + AQUA + i + " (" + citizens.getNPCRegistry().getById(i).getName() + ")\n";

                    }

                }

                if (context.getSessionData(pref + "deliveryMessages") == null) {
                    text += BLUE + "3 - Set delivery messages (None set)\n";
                } else {

                    text += BLUE + "3 - Set delivery messages\n";
                    for (String s : getDeliveryMessages(context)) {

                        text += GRAY + "    - " + AQUA + "\"" + s + "\"\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(DeliveryListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "deliveryItems") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must add items first!");
                    return new DeliveryListPrompt();
                } else {
                    return new DeliveryNPCsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                return new DeliveryMessagesPrompt();
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + "Delivery objective cleared.");
                context.setSessionData(pref + "deliveryItems", null);
                context.setSessionData(pref + "deliveryNPCs", null);
                context.setSessionData(pref + "deliveryMessages", null);
                return new DeliveryListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;

                if (context.getSessionData(pref + "deliveryItems") != null) {
                    one = ((List<ItemStack>) context.getSessionData(pref + "deliveryItems")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "deliveryNPCs") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "deliveryNPCs")).size();
                } else {
                    two = 0;
                }

                if (one == two) {

                    if (context.getSessionData(pref + "deliveryMessages") == null && one != 0) {
                        context.getForWhom().sendRawMessage(RED + "You must set at least one delivery message!");
                        return new DeliveryListPrompt();
                    } else {
                        return new CreateStagePrompt(stageNum, questFactory, citizens);
                    }

                } else {
                    context.getForWhom().sendRawMessage(RED + "The item list and NPC list are not equal in size!");
                    return new DeliveryListPrompt();
                }
            }

            return null;

        }

        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(pref + "deliveryItems");
        }

        private List<Integer> getDeliveryNPCs(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "deliveryNPCs");
        }

        private List<String> getDeliveryMessages(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "deliveryMessages");
        }
    }

    private class DeliveryNPCsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter NPC ids, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        Integer i = Integer.parseInt(s);

                        if (citizens.getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(PINK + "" + i + RED + " is not a valid NPC ID!");
                            return new DeliveryNPCsPrompt();
                        }

                    } catch (Exception e) {

                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new DeliveryNPCsPrompt();

                    }

                }

                context.setSessionData(pref + "deliveryNPCs", npcs);

            }

            return new DeliveryListPrompt();

        }
    }

    private class DeliveryMessagesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String note = GOLD + "Note: You may specify the name of the NPC with <npc>";
            return YELLOW + "Enter delivery messages, separating each one by a " + BOLD + ITALIC + "semi-colon" + RESET + YELLOW + ", or enter \'cancel\' to return.\n" + note;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(";");
                LinkedList<String> messages = new LinkedList<String>();
                messages.addAll(Arrays.asList(args));

                context.setSessionData(pref + "deliveryMessages", messages);

            }

            return new DeliveryListPrompt();

        }
    }

    private class NPCIDsToTalkToPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter NPC IDs, separating each one by a space, or enter \'clear\' to clear the NPC ID list, or \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        Integer i = Integer.parseInt(s);

                        if (citizens.getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(PINK + "" + i + RED + " is not a valid NPC ID!");
                            return new NPCIDsToTalkToPrompt();
                        }

                    } catch (Exception e) {

                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new NPCIDsToTalkToPrompt();

                    }

                }

                context.setSessionData(pref + "npcIdsToTalkTo", npcs);

            } else if (input.equalsIgnoreCase("clear")) {

                context.setSessionData(pref + "npcIdsToTalkTo", null);

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

            String text = GOLD + "- Kill NPCs -\n";
            if (context.getSessionData(pref + "npcIdsToKill") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set NPC IDs (None set)\n";
                text += GRAY + "2 - Set kill amounts (No IDs set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set kill IDs\n";
                for (Integer i : getNPCIds(context)) {

                    text += GRAY + "    - " + AQUA + citizens.getNPCRegistry().getById(i).getName() + DARKAQUA + " (" + i + ")\n";

                }

                if (context.getSessionData(pref + "npcAmountsToKill") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set kill amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set kill amounts\n";
                    for (Integer i : getKillAmounts(context)) {

                        text += GRAY + "    - " + BLUE + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new npcIdsToKillPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "npcIdsToKill") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set NPC IDs first!");
                    return new NPCKillListPrompt();
                } else {
                    return new npcAmountsToKillPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Kill NPCs objective cleared.");
                context.setSessionData(pref + "npcIdsToKill", null);
                context.setSessionData(pref + "npcAmountsToKill", null);
                return new NPCKillListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "npcIdsToKill") != null) {
                    one = ((List<Integer>) context.getSessionData(pref + "npcIdsToKill")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "npcAmountsToKill") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "npcAmountsToKill")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "NPC IDs list" + RED + " and " + GOLD + " kill amounts list " + RED + "are not the same size!");
                    return new NPCKillListPrompt();
                }
            }
            return null;

        }

        private List<Integer> getNPCIds(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "npcIdsToKill");
        }

        private List<Integer> getKillAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "npcAmountsToKill");
        }
    }

    private class npcIdsToKillPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter NPC IDs, separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> npcs = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        Integer i = Integer.parseInt(s);

                        if (citizens.getNPCRegistry().getById(i) != null) {
                            npcs.add(i);
                        } else {
                            context.getForWhom().sendRawMessage(PINK + "" + i + RED + " is not a valid NPC ID!");
                            return new npcIdsToKillPrompt();
                        }

                    } catch (Exception e) {

                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new npcIdsToKillPrompt();

                    }

                }

                context.setSessionData(pref + "npcIdsToKill", npcs);

            }

            return new NPCKillListPrompt();

        }
    }

    private class npcAmountsToKillPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return YELLOW + "Enter kill amounts (numbers), separating each one by a space, or enter \'cancel\' to return.";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] args = input.split(" ");
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : args) {

                    try {

                        if (Integer.parseInt(s) > 0) {
                            amounts.add(Integer.parseInt(s));
                        } else {
                            context.getForWhom().sendRawMessage(PINK + s + RED + " is not greater than 0!");
                            return new npcAmountsToKillPrompt();
                        }

                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(RED + "Invalid entry " + PINK + s + RED + ". Input was not a list of numbers!");
                        return new npcAmountsToKillPrompt();
                    }

                }

                context.setSessionData(pref + "npcAmountsToKill", amounts);

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

            String text = GOLD + "- Kill Mobs -\n";
            if (context.getSessionData(pref + "mobTypes") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set mob types (None set)\n";
                text += GRAY + "2 - Set mob amounts (No mob types set)\n";
                text += DARKGRAY + "|---------Optional---------|";
                text += GRAY + "3 - Set kill locations (No mob types set)\n";
                text += GRAY + "4 - Set kill location radii (No mob types set)\n";
                text += GRAY + "5 - Set kill location names (No mob types set)\n";
                text += DARKGRAY + "|--------------------------|";
                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set mob types\n";
                for (String s : getMobTypes(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + "mobAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set mob amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set mob amounts\n";
                    for (Integer i : getMobAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += DARKGRAY + "|---------Optional---------|";

                if (context.getSessionData(pref + "killLocations") == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set kill locations (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set kill locations\n";
                    for (String s : getKillLocations(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                if (context.getSessionData(pref + "killLocationRadii") == null) {
                    text += BLUE + "4 - Set kill location radii (None set)\n";
                } else {

                    text += BLUE + "4 - Set kill location radii\n";
                    for (int i : getKillRadii(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                if (context.getSessionData(pref + "killLocationNames") == null) {
                    text += BLUE + "5 - Set kill location names (None set)\n";
                } else {

                    text += BLUE + "5 - Set kill location names\n";
                    for (String s : getKillLocationNames(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                text += DARKGRAY + "|--------------------------|";

                text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "7" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new MobTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "mobTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set mob types first!");
                    return new MobListPrompt();
                } else {
                    return new MobAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + "mobTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set mob types first!");
                    return new MobListPrompt();
                } else {
                    questFactory.selectedKillLocations.put((Player) context.getForWhom(), null);
                    return new MobLocationPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                if (context.getSessionData(pref + "killLocations") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set kill locations first!");
                    return new MobListPrompt();
                } else {
                    return new MobRadiiPrompt();
                }
            } else if (input.equalsIgnoreCase("5")) {
                if (context.getSessionData(pref + "killLocations") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set kill locations first!");
                    return new MobListPrompt();
                } else {
                    return new MobLocationNamesPrompt();
                }
            } else if (input.equalsIgnoreCase("6")) {
                context.getForWhom().sendRawMessage(YELLOW + "Kill Mobs objective cleared.");
                context.setSessionData(pref + "mobTypes", null);
                context.setSessionData(pref + "mobAmounts", null);
                context.setSessionData(pref + "killLocations", null);
                context.setSessionData(pref + "killLocationRadii", null);
                context.setSessionData(pref + "killLocationNames", null);
                return new MobListPrompt();
            } else if (input.equalsIgnoreCase("7")) {

                int one;
                int two;

                int three;
                int four;
                int five;

                if (context.getSessionData(pref + "mobTypes") != null) {
                    one = ((List<String>) context.getSessionData(pref + "mobTypes")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "mobAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "mobAmounts")).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData(pref + "killLocations") != null) {
                    three = ((List<String>) context.getSessionData(pref + "killLocations")).size();
                } else {
                    three = 0;
                }

                if (context.getSessionData(pref + "killLocationRadii") != null) {
                    four = ((List<Integer>) context.getSessionData(pref + "killLocationRadii")).size();
                } else {
                    four = 0;
                }

                if (context.getSessionData(pref + "killLocationNames") != null) {
                    five = ((List<String>) context.getSessionData(pref + "killLocationNames")).size();
                } else {
                    five = 0;
                }

                if (one == two) {

                    if (three != 0 || four != 0 || five != 0) {

                        if (two == three && three == four && four == five) {
                            return new CreateStagePrompt(stageNum, questFactory, citizens);
                        } else {
                            context.getForWhom().sendRawMessage(RED + "All of your lists are not the same size!");
                            return new MobListPrompt();
                        }


                    } else {
                        return new CreateStagePrompt(stageNum, questFactory, citizens);
                    }

                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "mob types list " + RED + "and " + GOLD + "mob amounts list " + RED + "are not the same size!");
                    return new MobListPrompt();
                }

            }

            return null;

        }

        private List<String> getMobTypes(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "mobTypes");
        }

        private List<Integer> getMobAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "mobAmounts");
        }

        private List<String> getKillLocations(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "killLocations");
        }

        private List<Integer> getKillRadii(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "killLocationRadii");
        }

        private List<String> getKillLocationNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "killLocationNames");
        }
    }

    private class MobTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String mobs = PINK + "- Mobs - \n";
            mobs += PURPLE + "Bat, ";
            mobs += PURPLE + "Blaze, ";
            mobs += PURPLE + "CaveSpider, ";
            mobs += PURPLE + "Chicken, ";
            mobs += PURPLE + "Cow, ";
            mobs += PURPLE + "Creeper, ";
            mobs += PURPLE + "Enderman, ";
            mobs += PURPLE + "EnderDragon, ";
            mobs += PURPLE + "Ghast, ";
            mobs += PURPLE + "Giant, ";
            mobs += PURPLE + "IronGolem, ";
            mobs += PURPLE + "MagmaCube, ";
            mobs += PURPLE + "MushroomCow, ";
            mobs += PURPLE + "Ocelot, ";
            mobs += PURPLE + "Pig, ";
            mobs += PURPLE + "PigZombie, ";
            mobs += PURPLE + "Sheep, ";
            mobs += PURPLE + "Silverfish, ";
            mobs += PURPLE + "Skeleton, ";
            mobs += PURPLE + "Slime, ";
            mobs += PURPLE + "Snowman, ";
            mobs += PURPLE + "Spider, ";
            mobs += PURPLE + "Squid, ";
            mobs += PURPLE + "Villager, ";
            mobs += PURPLE + "Witch, ";
            mobs += PURPLE + "Wither, ";
            mobs += PURPLE + "Wolf, ";
            mobs += PURPLE + "Zombie\n";

            return mobs + YELLOW + "Enter mob names separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getMobType(s) != null) {

                        mobTypes.add(Quester.prettyMobString(Quests.getMobType(s)));
                        context.setSessionData(pref + "mobTypes", mobTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + "is not a valid mob name!");
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

            return YELLOW + "Enter mob amounts separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + input + " " + RED + "is not greater than zero!");
                            return new MobAmountsPrompt();
                        }

                        mobAmounts.add(i);


                    } catch (Exception e) {
                        player.sendMessage(PINK + input + " " + RED + "is not a number!");
                        return new MobAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "mobAmounts", mobAmounts);

            }

            return new MobListPrompt();

        }
    }

    private class MobLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Right-click on a block to select it, then enter \"add\" to add it to the kill location list, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("add")) {

                Block block = questFactory.selectedKillLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData(pref + "killLocations") != null) {
                        locs = (LinkedList<String>) context.getSessionData(pref + "killLocations");
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData(pref + "killLocations", locs);
                    questFactory.selectedKillLocations.remove(player);

                } else {
                    player.sendMessage(RED + "You must select a block first.");
                    return new MobLocationPrompt();
                }

                return new MobListPrompt();

            } else if (input.equalsIgnoreCase("cancel")) {

                questFactory.selectedKillLocations.remove(player);
                return new MobListPrompt();

            } else {
                return new MobLocationPrompt();
            }

        }
    }

    private class MobRadiiPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter kill location radii (number of blocks) separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Integer> radii = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + input + " " + RED + "is not greater than zero!");
                            return new MobRadiiPrompt();
                        }

                        radii.add(i);


                    } catch (Exception e) {
                        player.sendMessage(PINK + input + " " + RED + "is not a number!");
                        return new MobRadiiPrompt();
                    }

                }

                context.setSessionData(pref + "killLocationRadii", radii);

            }

            return new MobListPrompt();

        }
    }

    private class MobLocationNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter location names separating each one by a " + BOLD + "" + ITALIC + "comma" + RESET + "" + YELLOW + ", or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<String> locNames = new LinkedList<String>();
                locNames.addAll(Arrays.asList(input.split(",")));

                context.setSessionData(pref + "killLocationNames", locNames);

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

            String text = GOLD + "- Reach Locations -\n";
            if (context.getSessionData(pref + "reachLocations") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set locations (None set)\n";
                text += GRAY + "2 - Set location radii (No locations set)\n";
                text += GRAY + "3 - Set location names (No locations set)\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set locations\n";
                for (String s : getLocations(context)) {

                    text += GRAY + "    - " + DARKAQUA + s + "\n";

                }

                if (context.getSessionData(pref + "reachLocationRadii") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set location radii (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set location radii\n";
                    for (Integer i : getLocationRadii(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                if (context.getSessionData(pref + "reachLocationNames") == null) {
                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set location names (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set location names\n";
                    for (String s : getLocationNames(context)) {

                        text += GRAY + "    - " + AQUA + s + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                questFactory.selectedReachLocations.put((Player) context.getForWhom(), null);
                return new ReachLocationPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "reachLocations") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set locations first!");
                    return new ReachListPrompt();
                } else {
                    return new ReachRadiiPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(pref + "reachLocations") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set locations first!");
                    return new ReachListPrompt();
                } else {
                    return new ReachNamesPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(YELLOW + "Reach Locations objective cleared.");
                context.setSessionData(pref + "reachLocations", null);
                context.setSessionData(pref + "reachLocationRadii", null);
                context.setSessionData(pref + "reachLocationNames", null);
                return new ReachListPrompt();
            } else if (input.equalsIgnoreCase("5")) {

                int one;
                int two;
                int three;

                if (context.getSessionData(pref + "reachLocations") != null) {
                    one = ((List<String>) context.getSessionData(pref + "reachLocations")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "reachLocationRadii") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "reachLocationRadii")).size();
                } else {
                    two = 0;
                }

                if (context.getSessionData(pref + "reachLocationNames") != null) {
                    three = ((List<String>) context.getSessionData(pref + "reachLocationNames")).size();
                } else {
                    three = 0;
                }

                if (one == two && two == three) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "All of your lists are not the same size!");
                    return new ReachListPrompt();
                }


            } else {
                return new ReachListPrompt();
            }

        }

        private List<String> getLocations(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "reachLocations");
        }

        private List<Integer> getLocationRadii(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "reachLocationRadii");
        }

        private List<String> getLocationNames(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "reachLocationNames");
        }
    }

    private class ReachLocationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Right-click on a block to select it, then enter \"add\" to add it to the reach location list, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("add")) {

                Block block = questFactory.selectedReachLocations.get(player);
                if (block != null) {

                    Location loc = block.getLocation();

                    LinkedList<String> locs;
                    if (context.getSessionData(pref + "reachLocations") != null) {
                        locs = (LinkedList<String>) context.getSessionData(pref + "reachLocations");
                    } else {
                        locs = new LinkedList<String>();
                    }

                    locs.add(Quests.getLocationInfo(loc));
                    context.setSessionData(pref + "reachLocations", locs);
                    questFactory.selectedReachLocations.remove(player);

                } else {
                    player.sendMessage(RED + "You must select a block first.");
                    return new ReachLocationPrompt();
                }

                return new ReachListPrompt();

            } else if (input.equalsIgnoreCase("cancel")) {

                questFactory.selectedReachLocations.remove(player);
                return new ReachListPrompt();

            } else {
                return new ReachLocationPrompt();
            }

        }
    }

    private class ReachRadiiPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter reach location radii (number of blocks) separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Integer> radii = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + input + " " + RED + "is not greater than zero!");
                            return new ReachRadiiPrompt();
                        }

                        radii.add(i);


                    } catch (Exception e) {
                        player.sendMessage(PINK + input + " " + RED + "is not a number!");
                        return new ReachRadiiPrompt();
                    }

                }

                context.setSessionData(pref + "reachLocationRadii", radii);

            }

            return new ReachListPrompt();

        }
    }

    private class ReachNamesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter location names separating each one by a " + BOLD + "" + ITALIC + "comma" + RESET + "" + YELLOW + ", or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<String> locNames = new LinkedList<String>();
                locNames.addAll(Arrays.asList(input.split(",")));

                context.setSessionData(pref + "reachLocationNames", locNames);

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

            String text = GOLD + "- Tame Mobs -\n";
            if (context.getSessionData(pref + "tameTypes") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set mob types (None set)\n";
                text += GRAY + "2 - Set tame amounts (No mob types set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set mob types\n";
                for (String s : getTameTypes(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + "tameAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set tame amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set tame amounts\n";
                    for (Integer i : getTameAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new TameTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "tameTypes") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set mob types first!");
                    return new TameListPrompt();
                } else {
                    return new TameAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Tame Mobs objective cleared.");
                context.setSessionData(pref + "tameTypes", null);
                context.setSessionData(pref + "tameAmounts", null);
                return new TameListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "tameTypes") != null) {
                    one = ((List<String>) context.getSessionData(pref + "tameTypes")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "tameAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "tameAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "mob types list " + RED + "and " + GOLD + "tame amounts list " + RED + "are not the same size!");
                    return new TameListPrompt();
                }

            }

            return null;

        }

        private List<String> getTameTypes(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "tameTypes");
        }

        private List<Integer> getTameAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "tameAmounts");
        }
    }

    private class TameTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String mobs = PINK + "- Mobs - \n";
            mobs += PURPLE + "Bat, ";
            mobs += PURPLE + "Blaze, ";
            mobs += PURPLE + "CaveSpider, ";
            mobs += PURPLE + "Chicken, ";
            mobs += PURPLE + "Cow, ";
            mobs += PURPLE + "Creeper, ";
            mobs += PURPLE + "Enderman, ";
            mobs += PURPLE + "EnderDragon, ";
            mobs += PURPLE + "Ghast, ";
            mobs += PURPLE + "Giant, ";
            mobs += PURPLE + "IronGolem, ";
            mobs += PURPLE + "MagmaCube, ";
            mobs += PURPLE + "MushroomCow, ";
            mobs += PURPLE + "Ocelot, ";
            mobs += PURPLE + "Pig, ";
            mobs += PURPLE + "PigZombie, ";
            mobs += PURPLE + "Sheep, ";
            mobs += PURPLE + "Silverfish, ";
            mobs += PURPLE + "Skeleton, ";
            mobs += PURPLE + "Slime, ";
            mobs += PURPLE + "Snowman, ";
            mobs += PURPLE + "Spider, ";
            mobs += PURPLE + "Squid, ";
            mobs += PURPLE + "Villager, ";
            mobs += PURPLE + "Witch, ";
            mobs += PURPLE + "Wither, ";
            mobs += PURPLE + "Wolf, ";
            mobs += PURPLE + "Zombie\n";

            return mobs + YELLOW + "Enter mob names separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getMobType(s) != null) {

                        mobTypes.add(Quester.prettyMobString(Quests.getMobType(s)));
                        context.setSessionData(pref + "tameTypes", mobTypes);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + "is not a valid mob name!");
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

            return YELLOW + "Enter tame amounts separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Integer> mobAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + input + " " + RED + "is not greater than zero!");
                            return new TameAmountsPrompt();
                        }

                        mobAmounts.add(i);

                    } catch (Exception e) {
                        player.sendMessage(PINK + input + " " + RED + "is not a number!");
                        return new TameAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "tameAmounts", mobAmounts);

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

            String text = GOLD + "- Shear Sheeps -\n";
            if (context.getSessionData(pref + "shearColors") == null) {
                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set sheep colors (None set)\n";
                text += GRAY + "2 - Set shear amounts (No colors set)\n";
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";
            } else {

                text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set sheep colors\n";
                for (String s : getShearColors(context)) {

                    text += GRAY + "    - " + AQUA + s + "\n";

                }

                if (context.getSessionData(pref + "shearAmounts") == null) {
                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set shear amounts (None set)\n";
                } else {

                    text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set shear amounts\n";
                    for (Integer i : getShearAmounts(context)) {

                        text += GRAY + "    - " + AQUA + i + "\n";

                    }

                }

                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Clear\n";
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Done";

            }

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new ShearColorsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(pref + "shearColors") == null) {
                    context.getForWhom().sendRawMessage(RED + "You must set colors first!");
                    return new ShearListPrompt();
                } else {
                    return new ShearAmountsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                context.getForWhom().sendRawMessage(YELLOW + "Shear Sheep objective cleared.");
                context.setSessionData(pref + "shearColors", null);
                context.setSessionData(pref + "shearAmounts", null);
                return new ShearListPrompt();
            } else if (input.equalsIgnoreCase("4")) {

                int one;
                int two;

                if (context.getSessionData(pref + "shearColors") != null) {
                    one = ((List<String>) context.getSessionData(pref + "shearColors")).size();
                } else {
                    one = 0;
                }

                if (context.getSessionData(pref + "shearAmounts") != null) {
                    two = ((List<Integer>) context.getSessionData(pref + "shearAmounts")).size();
                } else {
                    two = 0;
                }

                if (one == two) {
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                } else {
                    context.getForWhom().sendRawMessage(RED + "The " + GOLD + "sheep colors list " + RED + "and " + GOLD + "shear amounts list " + RED + "are not the same size!");
                    return new ShearListPrompt();
                }

            }

            return null;

        }

        private List<String> getShearColors(ConversationContext context) {
            return (List<String>) context.getSessionData(pref + "shearColors");
        }

        private List<Integer> getShearAmounts(ConversationContext context) {
            return (List<Integer>) context.getSessionData(pref + "shearAmounts");
        }
    }

    private class ShearColorsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String mobs = PINK + "- Sheep Colors - \n";
            mobs += PURPLE + "Black, ";
            mobs += PURPLE + "Blue, ";
            mobs += PURPLE + "Brown, ";
            mobs += PURPLE + "Cyan, ";
            mobs += PURPLE + "Gray, ";
            mobs += PURPLE + "Green, ";
            mobs += PURPLE + "LightBlue, ";
            mobs += PURPLE + "Lime, ";
            mobs += PURPLE + "Magenta, ";
            mobs += PURPLE + "Orange, ";
            mobs += PURPLE + "Pink, ";
            mobs += PURPLE + "Purple, ";
            mobs += PURPLE + "Red, ";
            mobs += PURPLE + "Silver, ";
            mobs += PURPLE + "White, ";
            mobs += PURPLE + "Yellow\n";

            return mobs + YELLOW + "Enter sheep colors separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<String> colors = new LinkedList<String>();
                for (String s : input.split(" ")) {

                    if (Quests.getDyeColor(s) != null) {

                        colors.add(Quests.getDyeString(Quests.getDyeColor(s)));
                        context.setSessionData(pref + "shearColors", colors);

                    } else {
                        player.sendMessage(PINK + s + " " + RED + "is not a valid dye color!");
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

            return YELLOW + "Enter shear amounts separating each one by a space, or enter \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false) {

                LinkedList<Integer> shearAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {

                    try {

                        int i = Integer.parseInt(s);

                        if (i < 1) {
                            player.sendMessage(PINK + input + " " + RED + "is not greater than zero!");
                            return new ShearAmountsPrompt();
                        }

                        shearAmounts.add(i);

                    } catch (Exception e) {
                        player.sendMessage(PINK + input + " " + RED + "is not a number!");
                        return new ShearAmountsPrompt();
                    }

                }

                context.setSessionData(pref + "shearAmounts", shearAmounts);

            }

            return new ShearListPrompt();

        }
    }

    private class EventPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = DARKGREEN + "- Events -\n";
            if (questFactory.quests.events.isEmpty()) {
                text += RED + "- None";
            } else {
                for (Event e : questFactory.quests.events) {
                    text += GREEN + "- " + e.getName() + "\n";
                }
            }

            return text + YELLOW + "Enter an event name, or enter \"clear\" to clear the event, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                Event found = null;

                for (Event e : questFactory.quests.events) {

                    if (e.getName().equalsIgnoreCase(input)) {
                        found = e;
                        break;
                    }

                }

                if (found == null) {
                    player.sendMessage(RED + input + YELLOW + " is not a valid event name!");
                    return new EventPrompt();
                } else {
                    context.setSessionData(pref + "event", found.getName());
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                }

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(pref + "event", null);
                player.sendMessage(YELLOW + "Event cleared.");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }

        }
    }

    private class DelayPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter time (in milliseconds), or enter \"clear\" to clear the delay, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                long l;

                try {

                    l = Long.parseLong(input);

                } catch (Exception e) {
                    player.sendMessage(YELLOW + "Input was not a number!");
                    return new DelayPrompt();
                }


                if (l < 1000) {
                    player.sendMessage(YELLOW + "Delay must be at least one second!");
                    return new DelayPrompt();
                } else {
                    context.setSessionData(pref + "delay", l);
                    return new CreateStagePrompt(stageNum, questFactory, citizens);
                }

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(pref + "delay", null);
                player.sendMessage(YELLOW + "Delay cleared.");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }

        }
    }

    private class DelayMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            return YELLOW + "Enter delay message, or enter \"clear\" to clear the message, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                context.setSessionData(pref + "delayMessage", input);
                return new CreateStagePrompt(stageNum, questFactory, citizens);

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(pref + "delayMessage", null);
                player.sendMessage(YELLOW + "Delay message cleared.");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new DelayMessagePrompt();
            }

        }
    }

    private class DenizenPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = DARKAQUA + "- Denizen Scripts -";

            for (String s : ScriptRegistry._getScriptNames()) {
                text += AQUA + "- " + s + "\n";
            }

            return YELLOW + "Enter script name, or enter \"clear\" to clear the script, or \"cancel\" to return";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("cancel") == false && input.equalsIgnoreCase("clear") == false) {

                if (ScriptRegistry.containsScript(input)) {

                    context.setSessionData(pref + "denizen", ScriptRegistry.getScriptContainer(input).getName());
                    return new CreateStagePrompt(stageNum, questFactory, citizens);

                } else {

                    player.sendMessage(RED + "Denizen script not found!");
                    return new DenizenPrompt();

                }

            } else if (input.equalsIgnoreCase("clear")) {
                context.setSessionData(pref + "denizen", null);
                player.sendMessage(YELLOW + "Denizen script cleared.");
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                return new DenizenPrompt();
            }

        }
    }

    private class EpicBossListPrompt extends FixedSetPrompt {

        public EpicBossListPrompt() {
            super("1", "2", "3", "4");
        }

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = GOLD + "- " + DARKRED + "Kill Bosses" + GOLD + " -\n";

            if (cc.getSessionData(pref + "bossIds") == null) {
                text += BOLD + "" + RED + "1 " + RESET + RED + "- Set Bosses (None Set)\n";
            } else {
                text += BOLD + "" + RED + "1 " + RESET + RED + "- Set Bosses\n";
                LinkedList<String> ids = (LinkedList<String>) cc.getSessionData(pref + "bossIds");
                for (String id : ids) {

                    for (LoadBoss b : Quests.epicBoss.BossLoadList) {

                        if (b.getName().equalsIgnoreCase(id)) {
                            text += GOLD + "    - " + YELLOW + b.getName() + "\n";
                            break;
                        }

                    }


                }
            }

            if (cc.getSessionData(pref + "bossAmounts") == null) {
                text += BOLD + "" + RED + "2 " + RESET + RED + "- Set kill amounts (None Set)\n";
            } else {
                text += BOLD + "" + RED + "2 " + RESET + RED + "- Set kill amounts\n";
                LinkedList<Integer> amounts = (LinkedList<Integer>) cc.getSessionData(pref + "bossAmounts");
                for (int amount : amounts) {
                    text += GOLD + "    - " + YELLOW + amount + "\n";
                }
            }

            text += BOLD + "" + RED + "3 " + RESET + RED + "- Clear\n";
            text += BOLD + "" + RED + "4 " + RESET + GREEN + "- Done\n\n";

            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("1")) {
                return new EpicBossIDListPrompt();
            }else if(input.equalsIgnoreCase("2")) {
                return new EpicBossAmountListPrompt();
            }else if(input.equalsIgnoreCase("3")) {
                
                cc.setSessionData(pref + "bossIds", null);
                cc.setSessionData(pref + "bossAmounts", null);
                
                cc.getForWhom().sendRawMessage(YELLOW + "Kill Bosses objective cleared.");
                return new EpicBossListPrompt();
                
            }else if(input.equalsIgnoreCase("4")) {
                
                int idNum = 0;
                int amountNum = 0;
                
                if(cc.getSessionData(pref + "bossIds") != null)
                    idNum = ((LinkedList<String>) cc.getSessionData(pref + "bossIds")).size();
                
                if(cc.getSessionData(pref + "bossAmounts") != null)
                    amountNum = ((LinkedList<Integer>) cc.getSessionData(pref + "bossAmounts")).size();
                
                if(idNum != amountNum){
                    
                    cc.getForWhom().sendRawMessage(RED + "Error: the lists are not the same size!");
                    return new EpicBossListPrompt();
                }
                
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            }

            return new EpicBossListPrompt();

        }
    }

    private class EpicBossIDListPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = GOLD + "- " + DARKRED + "Bosses" + GOLD + " -\n";
            if (Quests.epicBoss.BossLoadList.isEmpty()) {
                text += RED + "- None\n";
            } else {

                for (LoadBoss b : Quests.epicBoss.BossLoadList) {
                    text += RED + "  - " + b.getName() + "\n";
                }

            }

            text += "\n";
            text += YELLOW + "Enter Boss names, separating each one by a space, or 'cancel' to return.";

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {

                String[] ids = input.split(" ");
                LinkedList<String> idList = new LinkedList<String>();
                for (String id : ids) {

                        LoadBoss found = null;

                        for (LoadBoss b : Quests.epicBoss.BossLoadList) {
                            if (b.getName().equalsIgnoreCase(input)) {
                                found = b;
                                break;
                            }
                        }

                        if (found == null) {
                            cc.getForWhom().sendRawMessage(RED + "Error: could not find Boss with name " + id + "!");
                            return new EpicBossIDListPrompt();
                        } else {
                            if (idList.contains(found.getName())) {
                                cc.getForWhom().sendRawMessage(RED + "Error: list contains duplicates!");
                                return new EpicBossIDListPrompt();
                            } else {
                                idList.add(found.getName());
                            }
                        }

                }

                cc.setSessionData(pref + "bossIds", idList);

            }

            return new EpicBossListPrompt();

        }
    }

    private class EpicBossAmountListPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = YELLOW + "Enter kill amounts (numbers), separating each one by a space, or 'cancel' to return.";

            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {

            if (input.equalsIgnoreCase("cancel") == false) {
                
                String[] amounts = input.split(",");
                LinkedList<Integer> amountList = new LinkedList<Integer>();
                for (String amount : amounts) {

                    try {

                        int i = Integer.parseInt(amount);
                        if(i < 1){
                            cc.getForWhom().sendRawMessage(RED + "Error: " + amount + " is not greater than zero!");
                            return new EpicBossAmountListPrompt();
                        }
                        
                        amountList.add(i);

                    } catch (Exception e) {
                        cc.getForWhom().sendRawMessage(RED + "Error: " + amount + " is not a number!");
                        return new EpicBossAmountListPrompt();
                    }

                }

                cc.setSessionData(pref + "bossAmounts", amountList);

            }

            return new EpicBossListPrompt();

        }
    }

    private class DeletePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = GREEN + "" + BOLD + "1" + RESET + "" + GREEN + " - Yes\n";
            text += GREEN + "" + BOLD + "2" + RESET + "" + GREEN + " - No";
            return RED + "Are you sure you want to delete " + YELLOW + "Stage " + stageNum + RED + " of " + GOLD + context.getSessionData("questName") + RED + "?\n(Any Stages after will be shifted back one spot)\n" + text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {

            Player player = (Player) context.getForWhom();

            if (input.equalsIgnoreCase("1") || input.equalsIgnoreCase("Yes")) {
                StagesPrompt.deleteStage(context, stageNum);
                player.sendMessage(YELLOW + "Stage deleted successfully.");
                return new StagesPrompt(questFactory);
            } else if (input.equalsIgnoreCase("2") || input.equalsIgnoreCase("No")) {
                return new CreateStagePrompt(stageNum, questFactory, citizens);
            } else {
                player.sendMessage(RED + "Invalid option!");
                return new DeletePrompt();
            }

        }
    }
}
