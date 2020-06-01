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

package me.blackvein.quests.convo.quests.objectives;

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
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.convo.quests.stages.StageMainPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class MobsPrompt extends QuestsEditorNumericPrompt {
    private final Quests plugin;
    private final int stageNum;
    private final String pref;

    public MobsPrompt(int stageNum, ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
        this.stageNum = stageNum;
        this.pref = "stage" + stageNum;
    }
    
    private final int size = 6;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("stageEditorMobs");
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return ChatColor.BLUE;
            case 6:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("stageEditorKillMobs");
        case 2:
            return ChatColor.YELLOW + Lang.get("stageEditorTameMobs"); 
        case 3:
            return ChatColor.YELLOW + Lang.get("stageEditorCatchFish");
        case 4:
            return ChatColor.YELLOW + Lang.get("stageEditorMilkCows");
        case 5:
            return ChatColor.YELLOW + Lang.get("stageEditorShearSheep");
        case 6:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch(number) {
        case 1:
            if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_TYPES);
                LinkedList<Integer> amnts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS);
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    for (int i = 0; i < mobs.size(); i++) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA 
                                + MiscUtil.getPrettyMobName(MiscUtil.getProperMobType(mobs.get(i))) + ChatColor.GRAY 
                                + " x " + ChatColor.DARK_AQUA + amnts.get(i) + "\n";
                    }
                } else {
                    LinkedList<String> locs 
                            = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS);
                    LinkedList<Integer> radii 
                            = (LinkedList<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS);
                    LinkedList<String> names 
                            = (LinkedList<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES);
                    for (int i = 0; i < mobs.size(); i++) {
                        String msg = Lang.get("blocksWithin");
                        msg = msg.replace("<amount>", ChatColor.DARK_PURPLE + "" + radii.get(i) + ChatColor.GRAY);
                        text += ChatColor.GRAY + "     - " + ChatColor.BLUE 
                                + MiscUtil.getPrettyMobName(MiscUtil.getProperMobType(mobs.get(i))) + ChatColor.GRAY 
                                + " x " + ChatColor.DARK_AQUA + amnts.get(i) + ChatColor.GRAY + msg + ChatColor.YELLOW 
                                + names.get(i) + " (" + locs.get(i) + ")\n";
                    }
                }
                return text;
            }
        case 2:
            if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> mobs = (LinkedList<String>) context.getSessionData(pref + CK.S_TAME_TYPES);
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS);
                for (int i = 0; i < mobs.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + mobs.get(i) + ChatColor.GRAY + " x " 
                            + ChatColor.AQUA + amounts.get(i) + "\n";
                }
                return text;
            }
        case 3:
            if (context.getSessionData(pref + CK.S_FISH) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                Integer fish = (Integer) context.getSessionData(pref + CK.S_FISH);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + fish + " " + Lang.get("stageEditorFish") 
                        + ChatColor.GRAY + ")\n";
            }
        case 4:
            if (context.getSessionData(pref + CK.S_COW_MILK) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                Integer cows = (Integer) context.getSessionData(pref + CK.S_COW_MILK);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + cows + " " + Lang.get("stageEditorCows") 
                        + ChatColor.GRAY + ")\n";
            }
        case 5:
            if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> colors = (LinkedList<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS);
                LinkedList<Integer> amounts = (LinkedList<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS);
                for (int i = 0; i < colors.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + colors.get(i) + ChatColor.GRAY + " x " 
                            + ChatColor.AQUA + amounts.get(i) + "\n";
                }
                return text;
            }
        case 6:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        context.setSessionData(pref, Boolean.TRUE);
        
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);

        String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch(input.intValue()) {
        case 1:
            return new MobsKillListPrompt(context);
        case 2:
            return new MobsTameListPrompt(context); 
        case 3:
            return new MobsFishPrompt(context);
        case 4:
            return new MobsCowsPrompt(context);
        case 5:
            return new MobsShearListPrompt(context);
        case 6:
            try {
                return new StageMainPrompt(stageNum, context);
            } catch (Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new MobsPrompt(stageNum, context);
        }
    }

    public class MobsKillListPrompt extends QuestsEditorNumericPrompt {
        
        public MobsKillListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 7;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorKillMobs");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    return ChatColor.BLUE;
                case 6:
                    return ChatColor.RED;
                case 7:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorSetMobTypes");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetMobAmounts"); 
            case 3:
                return ChatColor.YELLOW + Lang.get("stageEditorSetKillLocations");
            case 4:
                return ChatColor.YELLOW + Lang.get("stageEditorSetKillLocationRadii");
            case 5:
                return ChatColor.YELLOW + Lang.get("stageEditorSetKillLocationNames");
            case 6:
                return ChatColor.RED + Lang.get("clear");
            case 7:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_MOB_TYPES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_MOB_TYPES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(pref + CK.S_MOB_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_MOB_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                }
            case 3:
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 4:
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (int i : (List<Integer>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                }
            case 5:
                if (context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 6:
            case 7:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch(input.intValue()) {
            case 1:
                return new MobsTypesPrompt(context);
            case 2:
                return new MobsAmountsPrompt(context);
            case 3:
                Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                temp.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getQuestFactory().setSelectedKillLocations(temp);
                return new MobsLocationPrompt(context);
            case 4:
                return new MobsRadiiPrompt(context);
            case 5:
                return new MobsLocationNamesPrompt(context);
            case 6:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_MOB_TYPES, null);
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, null);
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, null);
                return new MobsKillListPrompt(context);
            case 7:
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
                            return new MobsPrompt(stageNum, context);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                            return new MobsKillListPrompt(context);
                        }
                    } else {
                        return new MobsPrompt(stageNum, context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new MobsKillListPrompt(context);
                }
            default:
                return new MobsPrompt(stageNum, context);
            }
        }
    }

    public class MobsTypesPrompt extends QuestsEditorStringPrompt {
        
        public MobsTypesPrompt(ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(ConversationContext context) {
            return Lang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorMobsPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String mobs = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n";
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
            return mobs + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> mobTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    if (MiscUtil.getProperMobType(s) != null) {
                        mobTypes.add(s);
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidMob"));
                        return new MobsTypesPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_MOB_TYPES, mobTypes);
            }
            return new MobsKillListPrompt(context);
        }
    }

    public class MobsAmountsPrompt extends QuestsEditorStringPrompt {
        
        public MobsAmountsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorMobAmountsPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new MobsAmountsPrompt(context);
                        }
                        mobAmounts.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                        return new MobsAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_MOB_AMOUNTS, mobAmounts);
            }
            return new MobsKillListPrompt(context);
        }
    }

    public class MobsLocationPrompt extends QuestsEditorStringPrompt {

        public MobsLocationPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorMobLocationPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdAdd"))) {
                Block block = plugin.getQuestFactory().getSelectedKillLocations().get(player.getUniqueId());
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
                    Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                    temp.remove(player.getUniqueId());
                    plugin.getQuestFactory().setSelectedKillLocations(temp);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("stageEditorNoBlock"));
                    return new MobsLocationPrompt(context);
                }
                return new MobsKillListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                Map<UUID, Block> temp = plugin.getQuestFactory().getSelectedKillLocations();
                temp.remove(player.getUniqueId());
                plugin.getQuestFactory().setSelectedKillLocations(temp);
                return new MobsKillListPrompt(context);
            } else {
                return new MobsLocationPrompt(context);
            }
        }
    }

    public class MobsRadiiPrompt extends QuestsEditorStringPrompt {
        
        public MobsRadiiPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorMobLocationRadiiPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new MobsRadiiPrompt(context);
                        }
                        radii.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + input + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidItemName"));
                        return new MobsRadiiPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_RADIUS, radii);
            }
            return new MobsKillListPrompt(context);
        }
    }

    public class MobsLocationNamesPrompt extends QuestsEditorStringPrompt {

        public MobsLocationNamesPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorMobLocationNamesPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> locNames = new LinkedList<String>();
                locNames.addAll(Arrays.asList(input.split(Lang.get("charSemi"))));
                context.setSessionData(pref + CK.S_MOB_KILL_LOCATIONS_NAMES, locNames);
            }
            return new MobsKillListPrompt(context);
        }
    }
    
    public class MobsFishPrompt extends QuestsEditorStringPrompt {

        public MobsFishPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorCatchFishPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
                        return new MobsFishPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(pref + CK.S_FISH, i);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new MobsFishPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_FISH, null);
            }
            return new MobsPrompt(stageNum, context);
        }
    }
    
    public class MobsCowsPrompt extends QuestsEditorStringPrompt {

        public MobsCowsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorMilkCowsPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("stageEditorPositiveAmount"));
                        return new MobsCowsPrompt(context);
                    } else if (i > 0) {
                        context.setSessionData(pref + CK.S_COW_MILK, i);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new MobsCowsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(pref + CK.S_COW_MILK, null);
            }
            return new MobsPrompt(stageNum, context);
        }
    }
    
    public class MobsTameListPrompt extends QuestsEditorNumericPrompt {

        public MobsTameListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorTameMobs");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorSetMobTypes");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetTameAmounts"); 
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_TAME_TYPES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_TAME_TYPES)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(pref + CK.S_TAME_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_TAME_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch(input.intValue()) {
            case 1:
                return new MobsTameTypesPrompt(context);
            case 2:
                return new MobsTameAmountsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_TAME_TYPES, null);
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, null);
                return new MobsTameListPrompt(context);
            case 4:
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
                    return new MobsPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new MobsTameListPrompt(context);
                }
            default:
                return new MobsPrompt(stageNum, context);
            }
        }
    }

    public class MobsTameTypesPrompt extends QuestsEditorStringPrompt {

        public MobsTameTypesPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return Lang.get("eventEditorMobsTitle");
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorMobsPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String mobs = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n";
            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {
                final EntityType type = mobArr[i];
                if (type.isAlive() == false || Tameable.class.isAssignableFrom(type.getEntityClass()) == false) {
                    continue;
                }
                mobs += MiscUtil.snakeCaseToUpperCamelCase(mobArr[i].name()) + ", ";
            }
            mobs = mobs.substring(0, mobs.length() - 2) + "\n";
            return mobs + ChatColor.YELLOW + getQueryText(context);
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
                            mobTypes.add(s);
                            context.setSessionData(pref + CK.S_TAME_TYPES, mobTypes);
                        } else {
                            player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                    + Lang.get("stageEditorInvalidMob"));
                            return new MobsTameTypesPrompt(context);
                        }
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidMob"));
                        return new MobsTameTypesPrompt(context);
                    }
                }
            }
            return new MobsTameListPrompt(context);
        }
    }

    public class MobsTameAmountsPrompt extends QuestsEditorStringPrompt {
        
        public MobsTameAmountsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorTameAmountsPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new MobsTameAmountsPrompt(context);
                        }
                        mobAmounts.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber")
                                .replace("<input>", input));
                        return new MobsTameAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_TAME_AMOUNTS, mobAmounts);
            }
            return new MobsTameListPrompt(context);
        }
    }

    public class MobsShearListPrompt extends QuestsEditorNumericPrompt {

        public MobsShearListPrompt(ConversationContext context) {
            super(context);
        }
        
        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorShearSheep");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.RED;
                case 4:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorSetShearColors");
            case 2:
                return ChatColor.YELLOW + Lang.get("stageEditorSetShearAmounts"); 
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @SuppressWarnings("unchecked")
        public String getAdditionalText(ConversationContext context, int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(pref + CK.S_SHEAR_COLORS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (String s : (List<String>) context.getSessionData(pref + CK.S_SHEAR_COLORS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(pref + CK.S_SHEAR_AMOUNTS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (Integer i : (List<Integer>) context.getSessionData(pref + CK.S_SHEAR_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                }
            case 3:
            case 4:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch(input.intValue()) {
            case 1:
                return new MobsShearColorsPrompt(context);
            case 2:
                return new MobsShearAmountsPrompt(context);
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("stageEditorObjectiveCleared"));
                context.setSessionData(pref + CK.S_SHEAR_COLORS, null);
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, null);
                return new MobsShearListPrompt(context);
            case 4:
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
                    return new MobsPrompt(stageNum, context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new MobsShearListPrompt(context);
                }
            default:
                return new MobsPrompt(stageNum, context);
            }
        }
    }

    public class MobsShearColorsPrompt extends QuestsEditorStringPrompt {
        
        public MobsShearColorsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return Lang.get("stageEditorColors");
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorShearColorsPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String cols = ChatColor.LIGHT_PURPLE + "- " + getTitle(context) + " - \n";
            final DyeColor[] colArr = DyeColor.values();
            for (int i = 0; i < colArr.length; i++) {
                if (i < (colArr.length - 1)) {
                    cols += MiscUtil.snakeCaseToUpperCamelCase(colArr[i].name()) + ", ";
                } else {
                    cols += MiscUtil.snakeCaseToUpperCamelCase(colArr[i].name()) + "\n";
                }
            }
            return cols + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> colors = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    if (MiscUtil.getProperDyeColor(s) != null) {
                        colors.add(s);
                        context.setSessionData(pref + CK.S_SHEAR_COLORS, colors);
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidDye"));
                        return new MobsShearColorsPrompt(context);
                    }
                }
            }
            return new MobsShearListPrompt(context);
        }
    }

    public class MobsShearAmountsPrompt extends QuestsEditorStringPrompt {
        
        public MobsShearAmountsPrompt(ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(ConversationContext context) {
            return Lang.get("stageEditorShearAmountsPrompt");
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
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
                            return new MobsShearAmountsPrompt(context);
                        }
                        shearAmounts.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                        return new MobsShearAmountsPrompt(context);
                    }
                }
                context.setSessionData(pref + CK.S_SHEAR_AMOUNTS, shearAmounts);
            }
            return new MobsShearListPrompt(context);
        }
    }
}
