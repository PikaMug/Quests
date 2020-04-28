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

package me.blackvein.quests.convo.quests.options;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import me.blackvein.quests.Options;
import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;

public class OptionsPrompt extends QuestsEditorNumericPrompt {

    private final Quests plugin;
    private String tempKey;
    private Prompt tempPrompt;

    public OptionsPrompt(ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(ConversationContext context) {
        return Lang.get("optionsTitle").replace("<quest>", (String) context.getSessionData(CK.Q_NAME));
    }
    
    @Override
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
    
    @Override
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
    public String getAdditionalText(ConversationContext context, int number) {
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.DARK_GREEN + getTitle(context)
                .replace((String) context.getSessionData(CK.Q_NAME), ChatColor.AQUA 
                + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_GREEN) + "\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + "\n";
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        switch (input.intValue()) {
        case 1:
            return new OptionsGeneralPrompt(context);
        case 2:
            return new OptionsMultiplayerPrompt(context);
        case 3:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new OptionsPrompt(context);
        }
    }
    
    public class TrueFalsePrompt extends QuestsEditorStringPrompt {
        public TrueFalsePrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(ConversationContext context) {
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
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = Lang.get("optBooleanPrompt");
            text = text.replace("<true>", Lang.get("true"));
            text = text.replace("<false>", Lang.get("false"));
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                if (input.startsWith("t") || input.equalsIgnoreCase(Lang.get("true")) 
                        || input.equalsIgnoreCase(Lang.get("yesWord"))) {
                    context.setSessionData(tempKey, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(Lang.get("false")) 
                        || input.equalsIgnoreCase(Lang.get("noWord"))) {
                    context.setSessionData(tempKey, false);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateInvalidInput"));
                    return new TrueFalsePrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class LevelPrompt extends QuestsEditorStringPrompt {
        public LevelPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 6;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(ConversationContext context) {
            return "Pick level of progress sharing";
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
            QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = Lang.get("optNumberPrompt");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "1" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + Lang.get("everything");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "2" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + Lang.get("objectives");;
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "3" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + Lang.get("stageEditorStages");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "4" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + Lang.get("quests");
            return ChatColor.YELLOW + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    context.setSessionData(tempKey, i);
                } catch (Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class OptionsGeneralPrompt extends QuestsEditorNumericPrompt {
        public OptionsGeneralPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return ChatColor.DARK_GREEN + Lang.get("optGeneral");
        }
        
        @Override
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
        
        @Override
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("optAllowCommands");
            case 2:
                return ChatColor.YELLOW + Lang.get("optAllowQuitting");
            case 3:
                return ChatColor.YELLOW + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.OPT_ALLOW_COMMANDS) == null) {
                    boolean defaultOpt = new Options().getAllowCommands();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                        + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                        + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    boolean commandsOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_COMMANDS);
                    return ChatColor.GRAY + "(" + (commandsOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(commandsOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(commandsOpt))) + ChatColor.GRAY + ")";
                }
            case 2:
                if (context.getSessionData(CK.OPT_ALLOW_QUITTING) == null) {
                    boolean defaultOpt = new Options().getAllowQuitting();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                   boolean quittingOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_QUITTING);
                    return ChatColor.GRAY + "(" + (quittingOpt ? ChatColor.GREEN
                            + Lang.get(String.valueOf(quittingOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(quittingOpt))) + ChatColor.GRAY + ")";
                }
            case 3:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_GREEN + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - "
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
            case 1:
                tempKey = CK.OPT_ALLOW_COMMANDS;
                tempPrompt = new OptionsGeneralPrompt(context);
                return new TrueFalsePrompt(context);
            case 2:
                tempKey = CK.OPT_ALLOW_QUITTING;
                tempPrompt = new OptionsGeneralPrompt(context);
                return new TrueFalsePrompt(context);
            case 3:
                tempKey = null;
                tempPrompt = null;
                try {
                    return new OptionsPrompt(context);
                } catch (Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                    return Prompt.END_OF_CONVERSATION;
                }
            default:
                return null;
            }
        }
    }
    
    public class OptionsMultiplayerPrompt extends QuestsEditorNumericPrompt {
        public OptionsMultiplayerPrompt(ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(ConversationContext context) {
            return ChatColor.DARK_GREEN + Lang.get("optMultiplayer");
        }
        
        @Override
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
        
        @Override
        public String getSelectionText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("optUseDungeonsXLPlugin");
            case 2:
                return ChatColor.YELLOW + Lang.get("optUsePartiesPlugin");
            case 3:
                return ChatColor.YELLOW + Lang.get("optShareProgressLevel");
            case 4:
                return ChatColor.YELLOW + Lang.get("optRequireSameQuest");
            case 5:
                return ChatColor.YELLOW + Lang.get("done");
             default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(ConversationContext context, int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN) == null) {
                    boolean defaultOpt = new Options().getUseDungeonsXLPlugin();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    boolean dungeonsOpt = (Boolean) context.getSessionData(CK.OPT_USE_DUNGEONSXL_PLUGIN);
                    return ChatColor.GRAY + "(" + (dungeonsOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(dungeonsOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(dungeonsOpt))) + ChatColor.GRAY + ")";
                }
            case 2:
                if (context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN) == null) {
                    boolean defaultOpt = new Options().getUsePartiesPlugin();
                    return ChatColor.GRAY + "("+ (defaultOpt ? ChatColor.GREEN 
                           + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                           + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    boolean partiesOpt = (Boolean) context.getSessionData(CK.OPT_USE_PARTIES_PLUGIN);
                    return ChatColor.GRAY + "(" + (partiesOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(partiesOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(partiesOpt))) + ChatColor.GRAY +  ")";
                }
            case 3:
                if (context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL) == null) {
                    int defaultOpt = new Options().getShareProgressLevel();
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + String.valueOf(defaultOpt) + ChatColor.GRAY + ")";
                } else {
                    int shareOpt = (Integer) context.getSessionData(CK.OPT_SHARE_PROGRESS_LEVEL);
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + String.valueOf(shareOpt) + ChatColor.GRAY + ")";
                }
            case 4:
                if (context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST) == null) {
                    boolean defaultOpt = new Options().getRequireSameQuest();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    boolean requireOpt = (Boolean) context.getSessionData(CK.OPT_REQUIRE_SAME_QUEST);
                    return ChatColor.GRAY + "(" + (requireOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(requireOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(requireOpt))) + ChatColor.GRAY +  ")";
                }
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_GREEN + "- " + getTitle(context) + " -\n";
            for (int i = 1; i <= size; i++) {
                text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
            }
            return text;
        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
            case 1:
                tempKey = CK.OPT_USE_DUNGEONSXL_PLUGIN;
                tempPrompt = new OptionsMultiplayerPrompt(context);
                return new TrueFalsePrompt(context);
            case 2:
                tempKey = CK.OPT_USE_PARTIES_PLUGIN;
                tempPrompt = new OptionsMultiplayerPrompt(context);
                return new TrueFalsePrompt(context);
            case 3:
                tempKey = CK.OPT_SHARE_PROGRESS_LEVEL;
                tempPrompt = new OptionsMultiplayerPrompt(context);
                return new LevelPrompt(context);
            case 4:
                tempKey = CK.OPT_REQUIRE_SAME_QUEST;
                tempPrompt = new OptionsMultiplayerPrompt(context);
                return new TrueFalsePrompt(context);
            case 5:
                tempKey = null;
                tempPrompt = null;
                try {
                    return new OptionsPrompt(context);
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
