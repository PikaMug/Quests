/*******************************************************************************************************
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/

package me.blackvein.quests.convo.quests.requirements;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.herocraftonline.heroes.characters.classes.HeroClass;

import me.blackvein.quests.CustomRequirement;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.convo.generic.OverridePrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class RequirementsPrompt extends QuestsEditorNumericPrompt {

    private final Quests plugin;
    private final String classPrefix;
    private boolean hasRequirement = false;
    private final int size = 11;
    
    public RequirementsPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
        this.classPrefix = getClass().getSimpleName();
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("requirementsTitle").replace("<quest>", (String) context.getSessionData(CK.Q_NAME));
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
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
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                if (context.getSessionData(CK.REQ_MONEY) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final int moneyReq = (Integer) context.getSessionData(CK.REQ_MONEY);
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + moneyReq + " " 
                            + (moneyReq > 1 ? plugin.getDependencies().getCurrency(true) 
                            : plugin.getDependencies().getCurrency(false)) + ChatColor.GRAY + ")";
                }
            } else {
                return ChatColor.GRAY + "(" + Lang.get("notInstalled") + ")";
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
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
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
                final List<String> perms = (List<String>) context.getSessionData(CK.REQ_PERMISSION);
                for (final String s : perms) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                return text;
            }
        case 5:
            if (context.getSessionData(CK.REQ_QUEST) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                final List<String> qs = (List<String>) context.getSessionData(CK.REQ_QUEST);
                for (final String s : qs) {
                    text += ChatColor.GRAY + "     - " + ChatColor.AQUA + s + "\n";
                }
                return text;
                }
        case 6:
            if (context.getSessionData(CK.REQ_QUEST_BLOCK) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                final List<String> qs = (List<String>) context.getSessionData(CK.REQ_QUEST_BLOCK);
                for (final String s : qs) {
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
                    final List<String> skills = (List<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS);
                    final List<Integer> amounts = (List<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS);
                    for (final String s : skills) {
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
                final LinkedList<String> customReqs = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
                for (final String s : customReqs) {
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
                final LinkedList<String> overrides = new LinkedList<String>();
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
    public String getPromptText(final ConversationContext context) {
        final String input = (String) context.getSessionData(classPrefix + "-override");
        if (input != null && !input.equalsIgnoreCase(Lang.get("cancel"))) {
            if (input.equalsIgnoreCase(Lang.get("clear"))) {
                context.setSessionData(CK.REQ_FAIL_MESSAGE, null);
            } else {
                final LinkedList<String> overrides = new LinkedList<String>();
                if (context.getSessionData(CK.REQ_FAIL_MESSAGE) != null) {
                    overrides.addAll((List<String>) context.getSessionData(CK.REQ_FAIL_MESSAGE));
                }
                overrides.add(input);
                context.setSessionData(CK.REQ_FAIL_MESSAGE, overrides);
                context.setSessionData(classPrefix + "-override", null);
            }
        }
        checkRequirement(context);
        
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.DARK_AQUA + getTitle(context).replace((String) context
                .getSessionData(CK.Q_NAME), ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) 
                + ChatColor.DARK_AQUA);
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i);
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            if (plugin.getDependencies().getVaultEconomy() != null) {
                return new RequirementsMoneyPrompt(context);
            } else {
                return new RequirementsPrompt(context);
            }
        case 2:
            return new RequirementsQuestPointsPrompt(context);
        case 3:
            return new RequirementsItemListPrompt(context);
        case 4:
            return new RequirementsPermissionsPrompt(context);
        case 5:
            return new RequirementsQuestListPrompt(context, true);
        case 6:
            return new RequirementsQuestListPrompt(context, false);
        case 7:
            if (plugin.getDependencies().getMcmmoClassic() != null) {
                return new RequirementsMcMMOListPrompt(context);
            } else {
                return new RequirementsPrompt(context);
            }
        case 8:
            if (plugin.getDependencies().getHeroes() != null) {
                return new RequirementsHeroesListPrompt(context);
            } else {
                return new RequirementsPrompt(context);
            }
        case 9:
            return new CustomRequirementsPrompt(context);
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
            return new RequirementsPrompt(context);
        }
    }
    
    public boolean checkRequirement(final ConversationContext context) {
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

    public class RequirementsMoneyPrompt extends QuestsEditorStringPrompt {
        
        public RequirementsMoneyPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewMoneyPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = getQueryText(context);
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
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REQ_MONEY, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RequirementsMoneyPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RequirementsMoneyPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_MONEY, null);
                return new RequirementsPrompt(context);
            }
            return new RequirementsPrompt(context);
        }
    }

    public class RequirementsQuestPointsPrompt extends QuestsEditorStringPrompt {
        
        public RequirementsQuestPointsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewQuestPointsPrompt").replace("<points>", Lang.get("questPoints"));
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }
        
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i > 0) {
                        context.setSessionData(CK.REQ_QUEST_POINTS, i);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("inputPosNum"));
                        return new RequirementsQuestPointsPrompt(context);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                    return new RequirementsQuestPointsPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_QUEST_POINTS, null);
                return new RequirementsPrompt(context);
            }
            return new RequirementsPrompt(context);
        }
    }

    public class RequirementsQuestListPrompt extends QuestsEditorStringPrompt {
        
        private final boolean isRequiredQuest;

        public RequirementsQuestListPrompt(final ConversationContext context, final boolean isRequired) {
            super(context);
            this.isRequiredQuest = isRequired;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("reqQuestListTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqQuestPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n" + ChatColor.DARK_PURPLE;
            boolean none = true;
            for (final Quest q : plugin.getQuests()) {
                text += q.getName() + ", ";
                none = false;
            }
            if (none) {
                text += "(" + Lang.get("none") + ")\n";
            } else {
                text = text.substring(0, (text.length() - 2));
                text += "\n";
            }
            text += ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                final String[] args = input.split(Lang.get("charSemi"));
                final LinkedList<String> questNames = new LinkedList<String>();
                for (final String s : args) {
                    if (plugin.getQuest(s) == null) {
                        String text = Lang.get("reqNotAQuestName");
                        text = text.replace("<quest>", ChatColor.LIGHT_PURPLE + s + ChatColor.RED);
                        context.getForWhom().sendRawMessage(text);
                        return new RequirementsQuestListPrompt(context, isRequiredQuest);
                    }
                    if (questNames.contains(s)) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("listDuplicate"));
                        return new RequirementsQuestListPrompt(context, isRequiredQuest);
                    }
                    questNames.add(plugin.getQuest(s).getName());
                }
                Collections.sort(questNames, new Comparator<String>() {

                    @Override
                    public int compare(final String one, final String two) {
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

    public class RequirementsItemListPrompt extends QuestsEditorNumericPrompt {
        
        public RequirementsItemListPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;

        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("itemRequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
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
        
        @SuppressWarnings("unchecked")
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final ItemStack is : (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS)) {
                        text += ChatColor.GRAY + "     - " + ItemUtil.getDisplayString(is) + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    if (context.getSessionData(CK.REQ_ITEMS_REMOVE) == null) {
                        return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                    } else {
                        String text = "\n";
                        for (final Boolean b : (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE)) {
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
        
        @SuppressWarnings("unchecked")
        @Override
        public String getPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                if (context.getSessionData(CK.REQ_ITEMS) != null) {
                    final List<ItemStack> itemReqs = (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
                    final ItemStack i = (ItemStack) context.getSessionData("tempStack");
                    if (i != null) {
                        itemReqs.add((ItemStack) context.getSessionData("tempStack"));
                    }
                    context.setSessionData(CK.REQ_ITEMS, itemReqs);
                } else {
                    final LinkedList<ItemStack> itemReqs = new LinkedList<ItemStack>();
                    final ItemStack i = (ItemStack) context.getSessionData("tempStack");
                    if (i != null) {
                        itemReqs.add((ItemStack) context.getSessionData("tempStack"));
                    }
                    context.setSessionData(CK.REQ_ITEMS, itemReqs);
                }
                ItemStackPrompt.clearSessionData(context);
            }
            
            final QuestsEditorPostOpenNumericPromptEvent event
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
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, RequirementsItemListPrompt.this);
            case 2:
                if (context.getSessionData(CK.REQ_ITEMS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqMustAddItem"));
                    return new RequirementsItemListPrompt(context);
                } else {
                    return new RemoveItemsPrompt(context);
                }
            case 3:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqItemCleared"));
                context.setSessionData(CK.REQ_ITEMS, null);
                context.setSessionData(CK.REQ_ITEMS_REMOVE, null);
                return new RequirementsItemListPrompt(context);
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
                    return new RequirementsItemListPrompt(context);
                }
            default:
                return null;
            }
        }

        /*@SuppressWarnings("unchecked")
        private List<ItemStack> getItems(final ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(CK.REQ_ITEMS);
        }*/

        /*@SuppressWarnings("unchecked")
        private List<Boolean> getRemoveItems(final ConversationContext context) {
            return (List<Boolean>) context.getSessionData(CK.REQ_ITEMS_REMOVE);
        }*/
    }

    public class RemoveItemsPrompt extends QuestsEditorStringPrompt {
        
        public RemoveItemsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqRemoveItemsPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final String[] args = input.split(" ");
                final LinkedList<Boolean> booleans = new LinkedList<Boolean>();
                for (final String s : args) {
                    if (input.startsWith("t") || s.equalsIgnoreCase(Lang.get("true")) 
                            || s.equalsIgnoreCase(Lang.get("yesWord"))) {
                        booleans.add(true);
                    } else if (input.startsWith("f") || s.equalsIgnoreCase(Lang.get("false")) 
                            || s.equalsIgnoreCase(Lang.get("noWord"))) {
                        booleans.add(false);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                        return new RemoveItemsPrompt(context);
                    }
                }
                context.setSessionData(CK.REQ_ITEMS_REMOVE, booleans);
            }
            return new RequirementsItemListPrompt(context);
        }
    }

    public class RequirementsPermissionsPrompt extends QuestsEditorStringPrompt {
        
        public RequirementsPermissionsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqPermissionsPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                final String[] args = input.split(" ");
                final LinkedList<String> permissions = new LinkedList<String>();
                permissions.addAll(Arrays.asList(args));
                context.setSessionData(CK.REQ_PERMISSION, permissions);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_PERMISSION, null);
            }
            return new RequirementsPrompt(context);
        }
    }

    public class CustomRequirementsPrompt extends QuestsEditorStringPrompt {
        
        public CustomRequirementsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("customRequirementsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqCustomPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n";
            if (plugin.getCustomRequirements().isEmpty()) {
                text += ChatColor.DARK_PURPLE + "(" + Lang.get("stageEditorNoModules") + ") ";
            } else {
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    text += ChatColor.DARK_PURPLE + "  - " + cr.getName() + "\n";
                }
            }
            return text + ChatColor.YELLOW + getQueryText(context);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                CustomRequirement found = null;
                // Check if we have a custom requirement with the specified name
                for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                    if (cr.getName().equalsIgnoreCase(input)) {
                        found = cr;
                        break;
                    }
                }
                if (found == null) {
                    // No? Check again, but with locale sensitivity
                    for (final CustomRequirement cr : plugin.getCustomRequirements()) {
                        if (cr.getName().toLowerCase().contains(input.toLowerCase())) {
                            found = cr;
                            break;
                        }
                    }
                }
                if (found != null) {
                    if (context.getSessionData(CK.REQ_CUSTOM) != null) {
                        // The custom requirement may already have been added, so let's check that
                        final LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
                        final LinkedList<Map<String, Object>> datamapList 
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
                            return new CustomRequirementsPrompt(context);
                        }
                    } else {
                        // The custom requirement hasn't been added yet, so let's do it
                        final LinkedList<Map<String, Object>> datamapList = new LinkedList<Map<String, Object>>();
                        datamapList.add(found.getData());
                        final LinkedList<String> list = new LinkedList<String>();
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
                    return new CustomRequirementsPrompt(context);
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
        public String getPromptText(final ConversationContext context) {
            String text = ChatColor.GOLD + "- ";
            final LinkedList<String> list = (LinkedList<String>) context.getSessionData(CK.REQ_CUSTOM);
            final LinkedList<Map<String, Object>> datamapList
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            final String reqName = list.getLast();
            final Map<String, Object> datamap = datamapList.getLast();
            text += reqName + " -\n";
            int index = 1;
            final LinkedList<String> datamapKeys = new LinkedList<String>();
            for (final String key : datamap.keySet()) {
                datamapKeys.add(key);
            }
            Collections.sort(datamapKeys);
            for (final String dataKey : datamapKeys) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + index + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + dataKey;
                if (datamap.get(dataKey) != null) {
                    text += ChatColor.GRAY + " (" + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&',
                            datamap.get(dataKey).toString()) + ChatColor.GRAY + ")\n";
                } else {
                    text += ChatColor.GRAY + " (" + Lang.get("noneSet") + ChatColor.GRAY + ")\n";
                }
                index++;
            }
            text += ChatColor.GREEN + "" + ChatColor.BOLD + index + ChatColor.YELLOW + " - " + Lang.get("done");
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final
            LinkedList<Map<String, Object>> datamapList 
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            final Map<String, Object> datamap = datamapList.getLast();
            int numInput;
            try {
                numInput = Integer.parseInt(input);
            } catch (final NumberFormatException nfe) {
                return new RequirementCustomDataListPrompt();
            }
            if (numInput < 1 || numInput > datamap.size() + 1) {
                return new RequirementCustomDataListPrompt();
            }
            if (numInput < datamap.size() + 1) {
                final LinkedList<String> datamapKeys = new LinkedList<String>();
                for (final String key : datamap.keySet()) {
                    datamapKeys.add(key);
                }
                Collections.sort(datamapKeys);
                final String selectedKey = datamapKeys.get(numInput - 1);
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
        public String getPromptText(final ConversationContext context) {
            String text = "";
            final String temp = (String) context.getSessionData(CK.REQ_CUSTOM_DATA_TEMP);
            @SuppressWarnings("unchecked")
            final
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
        public Prompt acceptInput(final ConversationContext context, final String input) {
            @SuppressWarnings("unchecked")
            final
            LinkedList<Map<String, Object>> datamapList 
                    = (LinkedList<Map<String, Object>>) context.getSessionData(CK.REQ_CUSTOM_DATA);
            final Map<String, Object> datamap = datamapList.getLast();
            datamap.put((String) context.getSessionData(CK.REQ_CUSTOM_DATA_TEMP), input);
            context.setSessionData(CK.REQ_CUSTOM_DATA_TEMP, null);
            return new RequirementCustomDataListPrompt();
        }
    }

    public class RequirementsMcMMOListPrompt extends QuestsEditorNumericPrompt {

        public RequirementsMcMMOListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("mcMMORequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("reqSetSkills");
            case 2:
                return ChatColor.YELLOW + Lang.get("reqSetSkillAmounts");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(CK.REQ_MCMMO_SKILLS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final String skill : (LinkedList<String>) context.getSessionData(CK.REQ_MCMMO_SKILLS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + skill + "\n";
                    }
                    return text;
                }
            case 2:
                if (context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    String text = "\n";
                    for (final int i : (LinkedList<Integer>) context.getSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS)) {
                        text += ChatColor.GRAY + "     - " + ChatColor.AQUA + i + "\n";
                    }
                    return text;
                }
            case 3:
                return "";
            default:
                return null;
            }
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new McMMOSkillsPrompt(context);
            case 2:
                return new McMMOAmountsPrompt(context);
            case 3:
                return new RequirementsPrompt(context);
            default:
                return new RequirementsMcMMOListPrompt(context);
            }
        }
    }

    public class McMMOSkillsPrompt extends QuestsEditorStringPrompt {
        
        public McMMOSkillsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("skillListTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("rewMcMMOPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String skillList = ChatColor.DARK_GREEN + getTitle(context) + "\n";
            final SkillType[] skills = SkillType.values();
            for (int i = 0; i < skills.length; i++) {
                if (i == (skills.length - 1)) {
                    skillList += ChatColor.GREEN + skills[i].getName() + "\n";
                } else {
                    skillList += ChatColor.GREEN + skills[i].getName() + "\n\n";
                }
            }
            return skillList + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                final LinkedList<String> skills = new LinkedList<String>();
                for (final String s : input.split(" ")) {
                    final String formatted = MiscUtil.getCapitalized(s);
                    if (Quests.getMcMMOSkill(formatted) != null) {
                        skills.add(formatted);
                    } else if (skills.contains(formatted)) {
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("listDuplicate"));
                        return new McMMOSkillsPrompt(context);
                    } else {
                        String text = Lang.get("reqMcMMOError");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new McMMOSkillsPrompt(context);
                    }
                }
                context.setSessionData(CK.REQ_MCMMO_SKILLS, skills);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqMcMMOCleared"));
                context.setSessionData(CK.REQ_MCMMO_SKILLS, null);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new RequirementsMcMMOListPrompt(context);
            }
            return new McMMOSkillsPrompt(context);
        }
    }

    public class McMMOAmountsPrompt extends QuestsEditorStringPrompt {
        
        public McMMOAmountsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqMcMMOAmountsPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                final LinkedList<Integer> amounts = new LinkedList<Integer>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        amounts.add(i);
                    } catch (final NumberFormatException nfe) {
                        String text = Lang.get("reqNotANumber");
                        text = text.replace("<input>", ChatColor.RED + s + ChatColor.YELLOW);
                        context.getForWhom().sendRawMessage(ChatColor.YELLOW + text);
                        return new McMMOAmountsPrompt(context);
                    }
                }
                context.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, amounts);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqMcMMOAmountsCleared"));
                context.setSessionData(CK.REQ_MCMMO_SKILL_AMOUNTS, null);
                return new RequirementsMcMMOListPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                return new RequirementsMcMMOListPrompt(context);
            }
            return new McMMOAmountsPrompt(context);
        }
    }

    public class RequirementsHeroesListPrompt extends QuestsEditorNumericPrompt {

        public RequirementsHeroesListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesRequirementsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
                case 1:
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("reqHeroesSetPrimary");
            case 2:
                return ChatColor.YELLOW + Lang.get("reqHeroesSetSecondary");
            case 3:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch(number) {
            case 1:
                if (context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + (String) context.getSessionData(CK.REQ_HEROES_PRIMARY_CLASS) 
                            + ChatColor.GREEN + ")\n";
                }
            case 2:
                if (context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) == null) {
                    return ChatColor.GRAY + " (" + Lang.get("noneSet") + ")";
                } else {
                    return "(" + ChatColor.AQUA + (String) context.getSessionData(CK.REQ_HEROES_SECONDARY_CLASS) 
                            + ChatColor.GREEN + ")\n";
                }
            case 3:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);

            String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }
        @Override
        protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new HeroesPrimaryPrompt(context);
            case 2:
                return new HeroesSecondaryPrompt(context);
            case 3:
                return new RequirementsPrompt(context);
            default:
                return new RequirementsHeroesListPrompt(context);
            }
        }
    }

    public class HeroesPrimaryPrompt extends QuestsEditorStringPrompt {
        
        public HeroesPrimaryPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesPrimaryTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqHeroesPrimaryPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_PURPLE + getTitle(context) + "\n";
            final LinkedList<String> list = new LinkedList<String>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isPrimary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text += ChatColor.GRAY + "(" + Lang.get("none") + ")\n";
            } else {
                Collections.sort(list);
                for (final String s : list) {
                    text += ChatColor.DARK_PURPLE + "- " + ChatColor.LIGHT_PURPLE + s + "\n";
                }
            }
            text += ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isPrimary()) {
                        context.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, hc.getName());
                        return new RequirementsHeroesListPrompt(context);
                    } else {
                        String text = Lang.get("reqHeroesNotPrimary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesPrimaryPrompt(context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqHeroesClassNotFound"));
                    return new HeroesPrimaryPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.REQ_HEROES_PRIMARY_CLASS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqHeroesPrimaryCleared"));
                return new RequirementsHeroesListPrompt(context);
            } else {
                return new RequirementsHeroesListPrompt(context);
            }
        }
    }

    public class HeroesSecondaryPrompt extends QuestsEditorStringPrompt {
        
        public HeroesSecondaryPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("heroesSecondaryTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("reqHeroesSecondaryPrompt");
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_PURPLE + getTitle(context) + "\n";
            final LinkedList<String> list = new LinkedList<String>();
            for (final HeroClass hc : plugin.getDependencies().getHeroes().getClassManager().getClasses()) {
                if (hc.isSecondary()) {
                    list.add(hc.getName());
                }
            }
            if (list.isEmpty()) {
                text += ChatColor.GRAY + "(" + Lang.get("none") + ")\n";
            } else {
                Collections.sort(list);
                for (final String s : list) {
                    text += ChatColor.DARK_PURPLE + "- " + ChatColor.LIGHT_PURPLE + s + "\n";
                }
            }
            text += ChatColor.YELLOW + getQueryText(context);
            return text;
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final HeroClass hc = plugin.getDependencies().getHeroes().getClassManager().getClass(input);
                if (hc != null) {
                    if (hc.isSecondary()) {
                        context.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, hc.getName());
                        return new RequirementsHeroesListPrompt(context);
                    } else {
                        String text = Lang.get("reqHeroesNotSecondary");
                        text = text.replace("<class>", ChatColor.LIGHT_PURPLE + hc.getName() + ChatColor.RED);
                        context.getForWhom().sendRawMessage(ChatColor.RED + text);
                        return new HeroesSecondaryPrompt(context);
                    }
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqHeroesClassNotFound"));
                    return new HeroesSecondaryPrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("clear"))) {
                context.setSessionData(CK.REQ_HEROES_SECONDARY_CLASS, null);
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("reqHeroesSecondaryCleared"));
                return new RequirementsHeroesListPrompt(context);
            } else {
                return new RequirementsHeroesListPrompt(context);
            }
        }
    }
}
