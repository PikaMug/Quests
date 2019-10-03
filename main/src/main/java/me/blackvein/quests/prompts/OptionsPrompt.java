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

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;

import me.blackvein.quests.Options;
import me.blackvein.quests.QuestFactory;
import me.blackvein.quests.Quests;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenOptionsGeneralPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenOptionsLevelPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenOptionsMultiplayerPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenOptionsPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenOptionsTrueFalsePromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class OptionsPrompt extends NumericPrompt {
    
    private final Quests plugin;
    private final QuestFactory factory;
    private String tempKey;
    private Prompt tempPrompt;

    public OptionsPrompt(Quests plugin, QuestFactory qf) {
        this.plugin = plugin;
        factory = qf;
    }
    
    private final int size = 3;
    
    public int getSize() {
        return size;
    }
    
    public String getTitle(ConversationContext context) {
        return Lang.get("optionsTitle").replace("<quest>", (String) context.getSessionData(CK.Q_NAME));
    }
    
    public ChatColor getNumberColor(ConversationContext context, int number) {
        switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                return ChatColor.BLUE;
            case 3:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    public String getSelectionText(ConversationContext context, int number) {
        switch (number) {
            case 1:
                return ChatColor.GOLD + Lang.get("optGeneral");
            case 2:
                return ChatColor.GOLD + Lang.get("optMultiplayer");
            case 3:
                return ChatColor.YELLOW + Lang.get("done");
            default:
                return null;
        }
    }

    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenOptionsPromptEvent event = new QuestsEditorPostOpenOptionsPromptEvent(factory, context);
        plugin.getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.DARK_GREEN + getTitle(context).replace((String) context.getSessionData(CK.Q_NAME), ChatColor.AQUA + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_GREEN) + "\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
            case 1:
                return new GeneralPrompt();
            case 2:
                return new MultiplayerPrompt();
            case 3:
                return factory.returnToMenu();
            default:
                return null;
        }
    }
    
    public class TrueFalsePrompt extends StringPrompt {

        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        public String getQueryText() {
            String text = "Select '<true>' or '<false>'";
            text = text.replace("<true>", Lang.get("true"));
            text = text.replace("<false>", Lang.get("false"));
            return text;
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.YELLOW + Lang.get("true");
                case 2:
                    return ChatColor.YELLOW + Lang.get("false");
                case 3:
                    return ChatColor.RED + Lang.get("cmdClear");
                case 4:
                    return ChatColor.RED + Lang.get("cmdCancel");
                default:
                    return null;
            }
        }
        
        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenOptionsTrueFalsePromptEvent event = new QuestsEditorPostOpenOptionsTrueFalsePromptEvent(factory, context);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = Lang.get("optBooleanPrompt");
            text = text.replace("<true>", Lang.get("true"));
            text = text.replace("<false>", Lang.get("false"));
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    boolean b = Boolean.parseBoolean(input);
                    if (input.equalsIgnoreCase("t") || input.equalsIgnoreCase(Lang.get("true")) || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                        b = true;
                    }
                    context.setSessionData(tempKey, b);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new TrueFalsePrompt();
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class LevelPrompt extends StringPrompt {
        
        private final int size = 6;
        
        public int getSize() {
            return size;
        }
        
        public String getQueryText() {
            return "Select level of progress sharing";
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.GOLD + "1";
                case 2:
                    return ChatColor.GOLD + "2";
                case 3:
                    return ChatColor.GOLD + "3";
                case 4:
                    return ChatColor.GOLD + "4";
                case 5:
                    return ChatColor.RED + Lang.get("cmdClear");
                case 6:
                    return ChatColor.RED + Lang.get("cmdCancel");
                default:
                    return null;
            }
        }
        
        public String getAdditionalText(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.GRAY + Lang.get("everything");
                case 2:
                    return ChatColor.GRAY + Lang.get("objectives");
                case 3:
                    return ChatColor.GRAY + Lang.get("stageEditorStages");
                case 4:
                    return ChatColor.GRAY + Lang.get("quests");
                case 5:
                    return "";
                case 6:
                    return "";
                default:
                    return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenOptionsLevelPromptEvent event = new QuestsEditorPostOpenOptionsLevelPromptEvent(factory, context);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = Lang.get("optNumberPrompt");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "1" + ChatColor.RESET + " = " + ChatColor.GRAY + Lang.get("everything");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "2" + ChatColor.RESET + " = " + ChatColor.GRAY + Lang.get("objectives");;
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "3" + ChatColor.RESET + " = " + ChatColor.GRAY + Lang.get("stageEditorStages");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "4" + ChatColor.RESET + " = " + ChatColor.GRAY + Lang.get("quests");
            return ChatColor.YELLOW + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    context.setSessionData(tempKey, i);
                } catch (Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", input));
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class GeneralPrompt extends NumericPrompt {
        
        private final int size = 3;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle() {
            return ChatColor.DARK_GREEN + Lang.get("optGeneral");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.BLUE;
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    if (context.getSessionData(CK.OPT_ALLOW_COMMANDS) == null) {
                        boolean defaultOpt = new Options().getAllowCommands();
                        return ChatColor.YELLOW + Lang.get("optAllowCommands") + " (" 
                                + (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")";
                    } else {
                        boolean commandsOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_COMMANDS);
                        return ChatColor.YELLOW + Lang.get("optAllowCommands") + " (" 
                                + (commandsOpt ? ChatColor.GREEN + String.valueOf(commandsOpt) : ChatColor.RED + String.valueOf(commandsOpt)) + ChatColor.YELLOW + ")";
                    }
                case 2:
                    if (context.getSessionData(CK.OPT_ALLOW_QUITTING) == null) {
                        boolean defaultOpt = new Options().getAllowQuitting();
                        return ChatColor.YELLOW + Lang.get("optAllowQuitting") + " (" 
                                + (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")";
                    } else {
                        boolean quittingOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_QUITTING);
                        return ChatColor.YELLOW + Lang.get("optAllowQuitting") + " (" 
                                + (quittingOpt ? ChatColor.GREEN + String.valueOf(quittingOpt) : ChatColor.RED + String.valueOf(quittingOpt)) + ChatColor.YELLOW + ")";
                    }
                case 3:
                    return ChatColor.YELLOW + Lang.get("done");
                default:
                    return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenOptionsGeneralPromptEvent event = new QuestsEditorPostOpenOptionsGeneralPromptEvent(factory, context);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_GREEN + "- " + getTitle() + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
                case 1:
                    tempKey = CK.OPT_ALLOW_COMMANDS;
                    tempPrompt = new GeneralPrompt();
                    return new TrueFalsePrompt();
                case 2:
                    tempKey = CK.OPT_ALLOW_QUITTING;
                    tempPrompt = new GeneralPrompt();
                    return new TrueFalsePrompt();
                case 3:
                    tempKey = null;
                    tempPrompt = null;
                    try {
                        return new OptionsPrompt(plugin, factory);
                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                        return Prompt.END_OF_CONVERSATION;
                    }
                default:
                    return null;
            }
        }
    }
    
    public class MultiplayerPrompt extends NumericPrompt {
        
        private final int size = 5;
        
        public int getSize() {
            return size;
        }
        
        public String getTitle() {
            return ChatColor.DARK_GREEN + Lang.get("optMultiplayer");
        }
        
        public ChatColor getNumberColor(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    return ChatColor.BLUE;
                case 2:
                    return ChatColor.BLUE;
                case 3:
                    return ChatColor.BLUE;
                case 4:
                    return ChatColor.BLUE;
                case 5:
                    return ChatColor.GREEN;
                default:
                    return null;
            }
        }
        
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
                case 1:
                    if (context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) == null) {
                        boolean defaultOpt = new Options().getUseDungeonsXLPlugin();
                        return ChatColor.YELLOW + Lang.get("optUseDungeonsXLPlugin") + " (" 
                                + (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")";
                    } else {
                        boolean dungeonsOpt = (Boolean) context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN);
                        return ChatColor.YELLOW + Lang.get("optUseDungeonsXLPlugin") + " (" 
                                + (dungeonsOpt ? ChatColor.GREEN + String.valueOf(dungeonsOpt) : ChatColor.RED + String.valueOf(dungeonsOpt)) + ChatColor.YELLOW + ")";
                    }
                case 2:
                    if (context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) == null) {
                        boolean defaultOpt = new Options().getUsePartiesPlugin();
                        return ChatColor.YELLOW + Lang.get("optUsePartiesPlugin") + " ("
                                + (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")";
                    } else {
                        boolean partiesOpt = (Boolean) context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN);
                        return ChatColor.YELLOW + Lang.get("optUsePartiesPlugin") + " (" 
                                + (partiesOpt ? ChatColor.GREEN + String.valueOf(partiesOpt) : ChatColor.RED + String.valueOf(partiesOpt)) + ChatColor.YELLOW +  ")";
                    }
                case 3:
                    if (context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) == null) {
                        int defaultOpt = new Options().getShareProgressLevel();
                        return ChatColor.YELLOW + Lang.get("optShareProgressLevel") + " (" 
                                + ChatColor.AQUA + String.valueOf(defaultOpt) + ChatColor.YELLOW + ")";
                    } else {
                        int shareOpt = (Integer) context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL);
                        return ChatColor.YELLOW + Lang.get("optShareProgressLevel") + " (" 
                                + ChatColor.AQUA + String.valueOf(shareOpt) + ChatColor.YELLOW + ")";
                    }
                case 4:
                    if (context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST) == null) {
                        boolean defaultOpt = new Options().getRequireSameQuest();
                        return ChatColor.YELLOW + Lang.get("optRequireSameQuest") + " (" 
                        + (defaultOpt ? ChatColor.GREEN + String.valueOf(defaultOpt) : ChatColor.RED + String.valueOf(defaultOpt)) + ChatColor.YELLOW + ")";
                    } else {
                        boolean requireOpt = (Boolean) context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST);
                        return ChatColor.YELLOW + Lang.get("optRequireSameQuest") + " (" 
                        + (requireOpt ? ChatColor.GREEN + String.valueOf(requireOpt) : ChatColor.RED + String.valueOf(requireOpt)) + ChatColor.YELLOW +  ")";
                    }
                case 5:
                    return ChatColor.YELLOW + Lang.get("done");
                default:
                    return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenOptionsMultiplayerPromptEvent event = new QuestsEditorPostOpenOptionsMultiplayerPromptEvent(factory, context);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_GREEN + "- " + getTitle() + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " + getSelectionText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
                case 1:
                    tempKey = CK.OPT_USE_DUNGEONSXL_PLUGIN;
                    tempPrompt = new MultiplayerPrompt();
                    return new TrueFalsePrompt();
                case 2:
                    tempKey = CK.OPT_USE_PARTIES_PLUGIN;
                    tempPrompt = new MultiplayerPrompt();
                    return new TrueFalsePrompt();
                case 3:
                    tempKey = CK.OPT_SHARE_PROGRESS_LEVEL;
                    tempPrompt = new MultiplayerPrompt();
                    return new LevelPrompt();
                case 4:
                    tempKey = CK.OPT_REQUIRE_SAME_QUEST;
                    tempPrompt = new MultiplayerPrompt();
                    return new TrueFalsePrompt();
                case 5:
                    tempKey = null;
                    tempPrompt = null;
                    try {
                        return new OptionsPrompt(plugin, factory);
                    } catch (Exception e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                        return Prompt.END_OF_CONVERSATION;
                    }
                default:
                    return null;
            }
        }
    }
}
