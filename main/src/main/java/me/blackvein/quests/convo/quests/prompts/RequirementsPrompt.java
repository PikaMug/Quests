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

package me.blackvein.quests.convo.quests.prompts;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;

import me.blackvein.quests.CustomRequirement;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class RequirementsPrompt extends QuestsEditorNumericPrompt {

    private final Quests plugin;
    private final String classPrefix;
    private boolean hasRequirement = false;
    private final int size = 11;
    
    public RequirementsPrompt(ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
        this.classPrefix = getClass().getSimpleName();
    }
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("requirementsTitle").replace("<quest>", (String) context.getSessionData(CK.Q_NAME));
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
            return ChatColor.BLUE;
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.BLUE;
            } else {
                return ChatColor.GRAY;
            }
        case 9:
            return ChatColor.BLUE;
        case 10:
            if (context.getSessionData(CK.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            } else {
                return ChatColor.BLUE;
            }
        case 11:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return ChatColor.YELLOW + Lang.get("reqSetMoney");
            } else {
                return ChatColor.GRAY + Lang.get("reqSetMoney");
            }
        case 2:
            return ChatColor.YELLOW + Lang.get("reqSetQuestPoints").replace("<points>", Lang.get("questPoints"));
        case 3:
            return ChatColor.YELLOW + Lang.get("reqSetItem");
        case 4:
            return ChatColor.YELLOW + Lang.get("reqSetPerms");
        case 5:
            return ChatColor.YELLOW + Lang.get("reqSetQuest");
        case 6:
            return ChatColor.YELLOW + Lang.get("reqSetQuestBlocks");
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return ChatColor.YELLOW + Lang.get("reqSetMcMMO");
            } else {
                return ChatColor.GRAY + Lang.get("reqSetMcMMO");
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return ChatColor.YELLOW + Lang.get("reqSetHeroes");
            } else {
                return ChatColor.GRAY + Lang.get("reqSetHeroes");
            }
        case 9:
            return ChatColor.DARK_PURPLE + Lang.get("reqSetCustom");
        case 10:
            if (!hasRequirement) {
                return ChatColor.GRAY + Lang.get("overrideCreateSet");
            } else {
                return ChatColor.YELLOW + Lang.get("overrideCreateSet");
            }
        case 11:
            return ChatColor.YELLOW + Lang.get("done");
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getAdditionalText(ConversationContext context, int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(CK.REQ_MONEY) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                int moneyReq = (Integer) context.getSessionData(CK.REQ_MONEY);
                return ChatColor.GRAY + "(" + ChatColor.AQUA + moneyReq + " " 
                        + (moneyReq > 1 ? plugin.getDependencies().getCurrency(true) 
                        : plugin.getDependencies().getCurrency(false)) + ChatColor.GRAY + ")";
            }
        case 2:
            if (context.getSessionData(CK.REQ_QUEST_POINTS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.REQ_QUEST_POINTS) + " " 
                        + Lang.get("questPoints") + ChatColor.GRAY + ")";
            }
        case 3:
            if (context.getSessionData(CK.REQ_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
                for (int i = 0; i < items.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + ItemUtil.getName(items.get(i)) 
                            + ChatColor.GRAY + " x " + ChatColor.AQUA + items.get(i).getAmount() + "\n";
                }
                return text;
            }
        case 4:
            if (context.getSessionData(CK.REQ_PERMISSION) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                List<String> perms = (List<String>) context.getSessionData(CK.REQ_PERMISSION);
                for (String s : perms) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                return text;
            }
        case 5:
            if (context.getSessionData(CK.REQ_QUEST) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                List<String> qs = (List<String>) context.getSessionData(CK.REQ_QUEST);
                for (String s : qs) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                return text;
                }
        case 6:
            if (context.getSessionData(CK.REQ_QUEST_BLOCK) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                List<String> qs = (List<String>) context.getSessionData(CK.REQ_QUEST_BLOCK);
                for (String s : qs) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                return text;
            }
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                if (context.getSessionData(CK.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    List<String> skills = (List<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS);
                    List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS);
                    for (String s : skills) {
                        text += ChatColor.GRAY + "     - " + ChatColor.DARK_GREEN + s + ChatColor.RESET 
                                + ChatColor.YELLOW + " " + Lang.get("mcMMOLevel") + " " + ChatColor.GREEN 
                                + amounts.get(skills.indexOf(s)) + "\n";
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null 
                        && context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")\n";
                } else {
                    String text = "\n";
                    if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + Lang.get("reqHeroesPrimaryDisplay") + " " 
                                + ChatColor.BLUE + (String) context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS);
                    }
                    if (context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null) {
                        text += ChatColor.AQUA + "    " + Lang.get("reqHeroesSecondaryDisplay") + " " 
                                + ChatColor.BLUE + (String) context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS);
                    }
                    return text;
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
            }
        case 9:
           if (context.getSessionData(CK.REQ_CUSTOM) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                LinkedList<String> customReqs = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
                for (String s : customReqs) {
                    text += ChatColor.RESET + "" + ChatColor.DARK_PURPLE + "  - " + ChatColor.LIGHT_PURPLE + s + "\n";
                 }
                return text;
            }
        case 10:
            if (context.getSessionData(CK.REQ_FAIL_MESSAGE) == null) {
                if (!hasRequirement) {
                    return ChatColor.GRAY + "(" + Lang.get("stageEditorOptional") + ")";
                } else {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                }
            } else {
                String text = "\n";
                LinkedList<String> overrides = new LinkedList<String>();
                overrides.addAll((List<String>) context.getSessionData(CK.REQ_FAIL_MESSAGE));
                for (int i = 0; i < overrides.size(); i++) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + overrides.get(i) + "\n";
                }
                return text;
            }
        case 11:
            return "";
        default:
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        // Checkadd newly made override
        if (context.getSessionData(classPrefix + "-override") != null) {
            LinkedList<String> overrides = new LinkedList<String>();
            if (context.getSessionData(CK.REQ_FAIL_MESSAGE) != null) {
                overrides.addAll((List<String>) context.getSessionData(CK.REQ_FAIL_MESSAGE));
            }
            overrides.add((String) context.getSessionData(classPrefix + "-override"));
            context.setSessionData(CK.REQ_FAIL_MESSAGE, overrides);
            context.setSessionData(classPrefix + "-override", null);
        }  
        checkRequirement(context);
        
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.DARK_AQUA + getTitle(context).replace((String) context
                .getSessionData(CK.Q_NAME), ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) 
                + ChatColor.DARK_AQUA) + "\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return new MoneyPrompt();
            } else {
                return new RequirementsPrompt(context);
            }
        case 2:
            return new QuestPointsPrompt();
        case 3:
            return new ItemListPrompt(context);
        case 4:
            return new PermissionsPrompt();
        case 5:
            return new QuestListPrompt(true);
        case 6:
            return new QuestListPrompt(false);
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return new mcMMOPrompt();
            } else {
                return new RequirementsPrompt(context);
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return new HeroesPrompt();
            } else {
                return new RequirementsPrompt(context);
            }
        case 9:
            return new CustomRequirementsPrompt();
        case 10:
            if (hasRequirement) {
                return new OverridePrompt.Builder()
                        .context(context)
                        .source(this)
                        .promptText(Lang.get("overrideCreateEnter"))
                        .build();
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidOption"));
                return new RequirementsPrompt(context);
            }
        case 11:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return null;
        }
    }
    
    public boolean checkRequirement(ConversationContext context) {
        if (context.getSessionData(CK.REQ_MONEY) != null 
                || context.getSessionData(CK.REQ_QUEST_POINTS) != null
                || context.getSessionData(CK.REQ_ITEMS) != null
                || context.getSessionData(CK.REQ_PERMISSION) != null
                || context.getSessionData(CK.REQ_QUEST) != null
                || context.getSessionData(CK.REQ_QUEST_BLOCK) != null
                || context.getSessionData(CK.REQ_MCMMO_SKILLS) != null 
                || context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) != null
                || context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) != null
                || context.getSessionData(CK.REQ_CUSTOM) != null) {
            hasRequirement = true;
            return true;
        }
        return false;
    }

    private class MoneyPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = Lang.get("reqMoneyPrompt");
            if (plugin.getDependencies().getVaultEconomy() != null) {
                text = text.replace("<money>", ChatColor.DARK_PURPLE+ ((plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural().isEmpty() ? Lang.get("money") : plugin.getDependencies().getVaultEconomy()
                        .currencyNamePlural())) + ChatColor.YELLOW);
            } else {
                text = text.replace("<money>", ChatColor.DARK_PURPLE + Lang.get("money") + ChatColor.YELLOW);
            }
            return ChatColor.YELLOW + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REQ_MONEY, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new MoneyPrompt();
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new MoneyPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_MONEY, null);
                return new RequirementsPrompt(context);
            }
            return new RequirementsPrompt(context);
        }
    }

    private class QuestPointsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("reqQuestPointsPrompt").replace("<points>", Lang.get("questPoints"));
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REQ_QUEST_POINTS, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new QuestPointsPrompt();
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new QuestPointsPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_QUEST_POINTS, null);
                return new RequirementsPrompt(context);
            }
            return new RequirementsPrompt(context);
        }
    }

    private class QuestListPrompt extends StringPrompt {

        private final boolean isRequiredQuest;

        public QuestListPrompt(boolean isRequired) {
            this.isRequiredQuest = isRequired;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.LIGHT_PURPLE + Lang.get("reqQuestListTitle") + "\n" + ChatColor.DARK_PURPLE;
            boolean none = true;
            for (Quest q : plugin.getQuests()) {
                text += q.getName() + ", ";
                none = false;
            }
            if (none) {
                text += "(" + Lang.get("none") + ")\n";
            } else {
                text = text.substring(0, (text.length() - 2));
                text += "\n";
            }
            text += ChatColor.YELLOW + Lang.get("reqQuestPrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] args = input.split(Lang.get("charSemi"));
                LinkedList<String> questNames = new LinkedList<String>();
                for (String s : args) {
                    if (plugin.getQuest(s) == null) {
                        String text = Lang.get("reqNotAQuestName");
                        text = text.replace("<quest>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(text);
                        return new QuestListPrompt(isRequiredQuest);
                    }
                    if (questNames.contains(s)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listDuplicate"));
                        return new QuestListPrompt(isRequiredQuest);
                    }
                    questNames.add(plugin.getQuest(s).getName());
                }
                Collections.sort(questNames, new Comparator<String>() {

                    @Override
                    public int compare(String one, String two) {
                        return one.compareTo(two);
                    }
                });
                if (isRequiredQuest) {
                    context.setSessionData(CK.REQ_QUEST, questNames);
                } else {
                    context.setSessionData(CK.REQ_QUEST_BLOCK, questNames);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                if (isRequiredQuest) {
                    context.setSessionData(CK.REQ_QUEST, null);
                } else {
                    context.setSessionData(CK.REQ_QUEST_BLOCK, null);
                }
            }
            return new RequirementsPrompt(context);
        }
    }

    public class ItemListPrompt extends QuestsEditorNumericPrompt {
        public ItemListPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 4;

        public int getSize() {
            return size;
        }
        
        public String getTitle(ConversationContext context) {
            return Lang.get("itemRequirementsTitle");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY;
                } else {
                    return ChatColor.BLUE;
                }
            case 3:
                return ChatColor.RED;
            case 4:
                return ChatColor.GREEN;
            default:
                return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + Lang.get("reqSetRemoveItems");
                } else {
                    return ChatColor.YELLOW + Lang.get("reqSetRemoveItems");
                }
            case 3:
                return ChatColor.RED + Lang.get("clear");
            case 4:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        public String getAdditionalText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.REQ_ITEMS) != null) {
                    String text = "\n";
                    for (ItemStack is : getItems(context)) {
                        text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                    }
                    return text;
                }
                return "";
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("reqNoItemsSet") + ")";
                } else {
                    if (context.getSessionData(CK.REQ_ITEMS_REMOVE) == null) {
                        return ChatColor.YELLOW + "(" + Lang.get("reqNoValuesSet") + ")";
                    } else {
                        String text = "\n";
                        for (Boolean b : getRemoveItems(context)) {
                            text += ChatColor.GRAY + "     - " + ChatColor.AQUA
                                    + (b.equals(Boolean.TRUE) ? Lang.get("yesWord") : Lang.get("noWord")) + "\n";
                        }
                        return text;
                    }
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
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(CK.REQ_ITEMS) != null) {
                    List<ItemStack> itemReqs = getItems(context);
                    itemReqs.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REQ_ITEMS, itemReqs);
                } else {
                    LinkedList<ItemStack> itemReqs = new LinkedList<ItemStack>();
                    itemReqs.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.REQ_ITEMS, itemReqs);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            
            QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.GOLD + getTitle(context) + "\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
            case 1:
                return new ItemStackPrompt(ItemListPrompt.this);
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqMustAddItem"));
                    return new ItemListPrompt(context);
                } else {
                    return new RemoveItemsPrompt();
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqItemCleared"));
                context.setSessionData(CK.REQ_ITEMS, null);
                context.setSessionData(CK.REQ_ITEMS_REMOVE, null);
                return new ItemListPrompt(context);
            case 4:
                int one;
                int two;
                if (context.getSessionData(CK.REQ_ITEMS) != null) {
                    one = ((List<ItemStack>) context.getSessionData(CK.REQ_ITEMS)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(CK.REQ_ITEMS_REMOVE) != null) {
                    two = ((List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE)).size();
                } else {
                    two = 0;
                }
                if (one == two) {
                    return new RequirementsPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listsNotSameSize"));
                    return new ItemListPrompt(context);
                }
            default:
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
        }

        @SuppressWarnings("unchecked")
        private List<Boolean> getRemoveItems(ConversationContext context) {
            return (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE);
        }
    }

    private class RemoveItemsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("reqRemoveItemsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                String[] args = input.split(" ");
                LinkedList<Boolean> booleans = new LinkedList<Boolean>();
                for (String s : args) {
                    if (input.startsWith("t") || s.equalsIgnoreCase(Lang.get("true")) 
                            || s.equalsIgnoreCase(Lang.get("yesWord"))) {
                        booleans.add(true);
                    } else if (input.startsWith("f") || s.equalsIgnoreCase(Lang.get("false")) 
                            || s.equalsIgnoreCase(Lang.get("noWord"))) {
                        booleans.add(false);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                        return new RemoveItemsPrompt();
                    }
                }
                context.setSessionData(CK.REQ_ITEMS_REMOVE, booleans);
            }
            return new ItemListPrompt(context);
        }
    }

    private class PermissionsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("reqPermissionsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] args = input.split(" ");
                LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));
                context.setSessionData(CK.REQ_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_PERMISSION, null);
            }
            return new RequirementsPrompt(context);
        }
    }

    private class CustomRequirementsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.LIGHT_PURPLE + Lang.get("customRequirementsTitle") + "\n";
            if (plugin.getCustomRequirements().isEmpty()) {
                text += ChatColor.DARK_PURPLE + "(" + Lang.get("stageEditorNoModules") + ") ";
            } else {
                for (CustomRequirement cr : plugin.getCustomRequirements()) {
                    text += ChatColor.DARK_PURPLE + "  - " + cr.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + Lang.get("reqCustomPrompt");
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                CustomRequirement found = null;
                // Check if we have a custom requirement with the specified name
                for (CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getName().equalsIgnoreCase(input)) {
                        found = cr;
                        break;
                    }
                }
                if (found == null) {
                    // No? Check again, but with locale sensitivity
                    for (CustomRequirement cr : plugin.getCustomRequirements()) {
                        if (cr.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr;
                            break;
                        }
                    }
                }
                if (found != null) {
                    if (context.getSessionData(CK.REQ_CUSTOM) != null) {
                        // The custom requirement may already have been added, so let's check that
                        LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
                        LinkedList<Map<String, Object>> datamapList 
                                = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
                        if (list.contains(found.getName()) == false) {
                            // Hasn't been added yet, so let's do it
                            list.add(found.getName());
                            datamapList.add(found.getData());
                            context.setSessionData(CK.REQ_CUSTOM, list);
                            context.setSessionData(CK.REQ_CUSTOM_DATA, datamapList);
                        } else {
                            // Already added, so inform user
                            context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqCustomAlreadyAdded"));
                            return new CustomRequirementsPrompt();
                        }
                    } else {
                        // The custom requirement hasn't been added yet, so let's do it
                        LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
                        datamapList.add(found.getData());
                        LinkedList<String> list = new LinkedList<String>();
                        list.add(found.getName());
                        context.setSessionData(CK.REQ_CUSTOM, list);
                        context.setSessionData(CK.REQ_CUSTOM_DATA, datamapList);
                    }
                    // Send user to the custom data prompt if there is any needed
                    if (found.getData().isEmpty() == false) {
                        context.setSessionData(CK.REQ_CUSTOM_DATA_DESCRIPTIONS, found.getDescriptions());
                        return new RequirementCustomDataListPrompt();
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqCustomNotFound"));
                    return new CustomRequirementsPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_CUSTOM, null);
                context.setSessionData(CK.REQ_CUSTOM_DATA, null);
                context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqCustomCleared"));
            }
            return new RequirementsPrompt(context);
        }
    }

    private class RequirementCustomDataListPrompt extends StringPrompt {

        @SuppressWarnings("unchecked")
        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.AQUA + "- ";
            LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
            LinkedList<Map<String, Object>> datamapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            String reqName = list.getLast();
            Map<String, Object> datamap = datamapList.getLast();
            text += reqName + " -\n";
            int index = 1;
            LinkedList<String> datamapKeys = new LinkedList<String>();
            for (String key : datamap.keySet()) {
                datamapKeys.add(key);
            }
            Collections.sort(datamapKeys);
            for (String dataKey : datamapKeys) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + index + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + dataKey;
                if (datamap.get(dataKey) != null) {
                    text += ChatColor.GREEN + " (" + datamap.get(dataKey).toString() + ")\n";
                } else {
                    text += ChatColor.RED + " (" + Lang.get("valRequired") + ")\n";
                }
                index++;
            }
            text += ChatColor.GREEN + "" + ChatColor.BOLD + index + ChatColor.YELLOW + " - " + Lang.get("done");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            @SuppressWarnings("unchecked")
            LinkedList<Map<String, Object>> datamapList 
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            int numInput;
            try {
                numInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                return new RequirementCustomDataListPrompt();
            }
            if (numInput < 1 || numInput > datamap.size() + 1) {
                return new RequirementCustomDataListPrompt();
            }
            if (numInput < datamap.size() + 1) {
                LinkedList<String> datamapKeys = new LinkedList<String>();
                for (String key : datamap.keySet()) {
                    datamapKeys.add(key);
                }
                Collections.sort(datamapKeys);
                String selectedKey = datamapKeys.get(numInput - 1);
                context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, selectedKey);
                return new RequirementCustomDataPrompt();
            } else {
                if (datamap.containsValue(null)) {
                    return new RequirementCustomDataListPrompt();
                } else {
                    context.setSessionData(CK.REQ_CUSTOM_DATA_DESCRIPTIONS, null);
                    return new RequirementsPrompt(context);
                }
            }
        }
    }

    private class RequirementCustomDataPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = "";
            String temp = (String) context.getSessionData(CK.REQ_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            Map<String, String> descriptions 
                    = (Map<String, String>) context.getSessionData(CK.REQ_CUSTOM_DATA_DESCRIPTIONS);
            if (descriptions.get(temp) != null) {
                text += ChatColor.GOLD + descriptions.get(temp) + "\n";
            }
            String lang = Lang.get("stageEditorCustomDataPrompt");
            lang = lang.replace("<data>", temp);
            text += ChatColor.YELLOW + lang;
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            @SuppressWarnings("unchecked")
            LinkedList<Map<String, Object>> datamapList 
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            Map<String, Object> datamap = datamapList.getLast();
            datamap.put((String) context.getSessionData(CK.REQ_CUSTOM_DATA_TEMP), input);
            context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, null);
            return new RequirementCustomDataListPrompt();
        }
    }

    private class mcMMOPrompt extends FixedSetPrompt {

        public mcMMOPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.DARK_GREEN + Lang.get("mcMMORequirementsTitle") + "\n";
            if (cc.getSessionData(CK.REQ_MCMMO_SKILLS) == null) {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "1" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqSetSkills") + "(" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "1" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqSetSkills") + "\n";
                @SuppressWarnings("unchecked")
                LinkedList<String> skills = (LinkedList<String>) cc.getSessionData(CK.REQ_MCMMO_SKILLS);
                for (String skill : skills) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + skill + "\n";
                }
            }
            if (cc.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) == null) {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "2" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqSetSkillAmounts") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "2" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqSetSkillAmounts") + "\n";
                @SuppressWarnings("unchecked")
                LinkedList<Integer> amounts = (LinkedList<Integer>) cc.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS);
                for (int i : amounts) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                }
            }
            text += ChatColor.BOLD + "" + ChatColor.GREEN + "3" + ChatColor.RESET + ChatColor.GREEN + " - " 
                    + Lang.get("done");
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new mcMMOSkillsPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                return new mcMMOAmountsPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new RequirementsPrompt(context);
            }
            return null;
        }
    }

    private class mcMMOSkillsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String skillList = ChatColor.DARK_GREEN + Lang.get("skillListTitle") + "\n";
            SkillType[] skills = SkillType.values();
            for (int i = 0; i < skills.length; i++) {
                if (i == (skills.length - 1)) {
                    skillList += ChatColor.GREEN + skills[i].getName() + "\n";
                } else {
                    skillList += ChatColor.GREEN + skills[i].getName() + "\n\n";
                }
            }
            return skillList + ChatColor.YELLOW + Lang.get("reqMcMMOPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                LinkedList<String> skills = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    String formatted = MiscUtil.getCapitalized(s);
                    if (Quests.getMcMMOSkill(formatted) != null) {
                        skills.add(formatted);
                    } else if (skills.contains(formatted)) {
                        cc.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("listDuplicate"));
                        return new mcMMOSkillsPrompt();
                    } else {
                        String text = Lang.get("reqMcMMOError");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        cc.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new mcMMOSkillsPrompt();
                    }
                }
                cc.setSessionData(CK.REQ_MCMMO_SKILLS, skills);
                return new mcMMOPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                cc.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqMcMMOCleared"));
                cc.setSessionData(CK.REQ_MCMMO_SKILLS, null);
                return new mcMMOPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new mcMMOPrompt();
            }
            return new mcMMOSkillsPrompt();
        }
    }

    private class mcMMOAmountsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("reqMcMMOAmountsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {
                    try {
                        int i = Integer.parseInt(s);
                        amounts.add(i);
                    } catch (NumberFormatException nfe) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        cc.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new mcMMOAmountsPrompt();
                    }
                }
                cc.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, amounts);
                return new mcMMOPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                cc.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqMcMMOAmountsCleared"));
                cc.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, null);
                return new mcMMOPrompt();
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new mcMMOPrompt();
            }
            return new mcMMOAmountsPrompt();
        }
    }

    private class HeroesPrompt extends FixedSetPrompt {

        public HeroesPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.DARK_GREEN + Lang.get("heroesRequirementsTitle") + "\n";
            if (cc.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null) {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "1" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqHeroesSetPrimary") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "1" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqHeroesSetPrimary") + " (" + ChatColor.AQUA 
                        + (String) cc.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) + ChatColor.GREEN + ")\n";
            }
            if (cc.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "2" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqHeroesSetSecondary") + " (" + Lang.get("noneSet") + ")\n";
            } else {
                text += ChatColor.BOLD + "" + ChatColor.GREEN + "2" + ChatColor.RESET + ChatColor.GREEN + " - " 
                        + Lang.get("reqHeroesSetSecondary") + " (" + ChatColor.AQUA 
                        + (String) cc.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) + ChatColor.GREEN + ")\n";
            }
            text += ChatColor.BOLD + "" + ChatColor.GREEN + "3" + ChatColor.RESET + ChatColor.GREEN + " - " 
                    + Lang.get("done");
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new HeroesPrimaryPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                return new HeroesSecondaryPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new RequirementsPrompt(context);
            }
            return null;
        }
    }

    private class HeroesPrimaryPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.DARK_PURPLE + Lang.get("heroesPrimaryTitle") + "\n";
            LinkedList<String> list = new LinkedList<String>();
            for (HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isPrimary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text += ChatColor.GRAY + "(" + Lang.get("none") + ")\n";
            } else {
                Collections.sort(list);
                for (String s : list) {
                    text += ChatColor.DARK_PURPLE + "- " + ChatColor.LIGHT_PURPLE + s + "\n";
                }
            }
            text += ChatColor.YELLOW + Lang.get("reqHeroesPrimaryPrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isPrimary()) {
                        cc.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, hc.getName());
                        return new HeroesPrompt();
                    } else {
                        String text = Lang.get("reqHeroesNotPrimary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        cc.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesPrimaryPrompt();
                    }
                } else {
                    cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqHeroesClassNotFound"));
                    return new HeroesPrimaryPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                cc.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, null);
                cc.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqHeroesPrimaryCleared"));
                return new HeroesPrompt();
            } else {
                return new HeroesPrompt();
            }
        }
    }

    private class HeroesSecondaryPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext cc) {
            String text = ChatColor.DARK_PURPLE + Lang.get("heroesSecondaryTitle") + "\n";
            LinkedList<String> list = new LinkedList<String>();
            for (HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isSecondary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text += ChatColor.GRAY + "(" + Lang.get("none") + ")\n";
            } else {
                Collections.sort(list);
                for (String s : list) {
                    text += ChatColor.DARK_PURPLE + "- " + ChatColor.LIGHT_PURPLE + s + "\n";
                }
            }
            text += ChatColor.YELLOW + Lang.get("reqHeroesSecondaryPrompt");
            return text;
        }

        @Override
        public Prompt acceptInput(ConversationContext cc, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isSecondary()) {
                        cc.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, hc.getName());
                        return new HeroesPrompt();
                    } else {
                        String text = Lang.get("reqHeroesNotSecondary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        cc.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesSecondaryPrompt();
                    }
                } else {
                    cc.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqHeroesClassNotFound"));
                    return new HeroesSecondaryPrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("clear"))) {
                cc.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, null);
                cc.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqHeroesSecondaryCleared"));
                return new HeroesPrompt();
            } else {
                return new HeroesPrompt();
            }
        }
    }
}
