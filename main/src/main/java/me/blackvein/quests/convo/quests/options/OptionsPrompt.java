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

    public OptionsPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("optionsTitle").replace("<quest>", (String) context.getSessionData(CK.Q_NAME));
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
    public String getSelectionText(final ConversationContext context, final int number) {
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
    public String getAdditionalText(final ConversationContext context, final int number) {
        return null;
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.DARK_GREEN + getTitle(context)
                .replace((String) context.getSessionData(CK.Q_NAME), ChatColor.AQUA 
                + (String) context.getSessionData(CK.Q_NAME) + ChatColor.DARK_GREEN);
        for (int i = 1; i <= size; i++) {
            text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i);
        }
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
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
    
    public class OptionsTrueFalsePrompt extends QuestsEditorStringPrompt {
        public OptionsTrueFalsePrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            String text = "Select '<true>' or '<false>'";
            text = text.replace("<true>", Lang.get("true"));
            text = text.replace("<false>", Lang.get("false"));
            return text;
        }
        
        public String getSelectionText(final ConversationContext context, final int number) {
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
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = Lang.get("optBooleanPrompt");
            text = text.replace("<true>", Lang.get("true"));
            text = text.replace("<false>", Lang.get("false"));
            return ChatColor.YELLOW + text;
        }
        
        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
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
                    return new OptionsTrueFalsePrompt(context);
                }
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class OptionsDistancePrompt extends QuestsEditorStringPrompt {
        public OptionsDistancePrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return "Pick sharing distance";
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
                try {
                    final int i = Integer.parseInt(input);
                    context.setSessionData(tempKey, i);
                } catch (final Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                            .replace("<input>", input));
                }
            }
            return tempPrompt;
        }
    }
    
    public class OptionsGeneralPrompt extends QuestsEditorNumericPrompt {
        public OptionsGeneralPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return ChatColor.DARK_GREEN + Lang.get("optGeneral");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatColor.BLUE;
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
                return ChatColor.YELLOW + Lang.get("optAllowCommands");
            case 2:
                return ChatColor.YELLOW + Lang.get("optAllowQuitting");
            case 3:
                return ChatColor.YELLOW + Lang.get("optIgnoreSilkTouch");
            case 4:
                return ChatColor.YELLOW + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.OPT_ALLOW_COMMANDS) == null) {
                    final boolean defaultOpt = new Options().canAllowCommands();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                        + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                        + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    final boolean commandsOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_COMMANDS);
                    return ChatColor.GRAY + "(" + (commandsOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(commandsOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(commandsOpt))) + ChatColor.GRAY + ")";
                }
            case 2:
                if (context.getSessionData(CK.OPT_ALLOW_QUITTING) == null) {
                    final boolean defaultOpt = new Options().canAllowQuitting();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    final boolean quittingOpt = (Boolean) context.getSessionData(CK.OPT_ALLOW_QUITTING);
                    return ChatColor.GRAY + "(" + (quittingOpt ? ChatColor.GREEN
                            + Lang.get(String.valueOf(quittingOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(quittingOpt))) + ChatColor.GRAY + ")";
                }
            case 3:
                if (context.getSessionData(CK.OPT_IGNORE_SILK_TOUCH) == null) {
                    final boolean defaultOpt = new Options().canIgnoreSilkTouch();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    final boolean quittingOpt = (Boolean) context.getSessionData(CK.OPT_IGNORE_SILK_TOUCH);
                    return ChatColor.GRAY + "(" + (quittingOpt ? ChatColor.GREEN
                            + Lang.get(String.valueOf(quittingOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(quittingOpt))) + ChatColor.GRAY + ")";
                }
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_GREEN + "- " + getTitle(context) + " -";
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - "
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i);
            }
            return text;
        }

        @Override
        public Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                tempKey = CK.OPT_ALLOW_COMMANDS;
                tempPrompt = new OptionsGeneralPrompt(context);
                return new OptionsTrueFalsePrompt(context);
            case 2:
                tempKey = CK.OPT_ALLOW_QUITTING;
                tempPrompt = new OptionsGeneralPrompt(context);
                return new OptionsTrueFalsePrompt(context);
            case 3:
                tempKey = CK.OPT_IGNORE_SILK_TOUCH;
                tempPrompt = new OptionsGeneralPrompt(context);
                return new OptionsTrueFalsePrompt(context);
            case 4:
                tempKey = null;
                tempPrompt = null;
                try {
                    return new OptionsPrompt(context);
                } catch (final Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                    return Prompt.END_OF_CONVERSATION;
                }
            default:
                return null;
            }
        }
    }
    
    public class OptionsMultiplayerPrompt extends QuestsEditorNumericPrompt {
        public OptionsMultiplayerPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 4;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return ChatColor.DARK_GREEN + Lang.get("optMultiplayer");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatColor.BLUE;
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
                return ChatColor.YELLOW + Lang.get("optShareProgress");
            case 2:
                return ChatColor.YELLOW + Lang.get("optShareDistance");
            case 3:
                return ChatColor.YELLOW + Lang.get("optShareOnlySameQuest");
            case 4:
                return ChatColor.YELLOW + Lang.get("done");
             default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.OPT_SHARE_PROGRESS) == null) {
                    final boolean defaultOpt = new Options().canShareProgress();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN
                            + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                            + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    final boolean requireOpt = (Boolean) context.getSessionData(CK.OPT_SHARE_PROGRESS);
                    return ChatColor.GRAY + "(" + (requireOpt ? ChatColor.GREEN
                            + Lang.get(String.valueOf(requireOpt)) : ChatColor.RED
                            + Lang.get(String.valueOf(requireOpt))) + ChatColor.GRAY +  ")";
                }
            case 2:
                if (context.getSessionData(CK.OPT_SHARE_DISTANCE) == null) {
                    final long defaultOpt = new Options().getShareDistance();
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + String.valueOf(defaultOpt) + ChatColor.GRAY + ")";
                } else {
                    final long shareOpt = (Long) context.getSessionData(CK.OPT_SHARE_DISTANCE);
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + String.valueOf(shareOpt) + ChatColor.GRAY + ")";
                }
            case 3:
                if (context.getSessionData(CK.OPT_SHARE_SAME_QUEST_ONLY) == null) {
                    final boolean defaultOpt = new Options().canShareOnlySameQuest();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(defaultOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    final boolean requireOpt = (Boolean) context.getSessionData(CK.OPT_SHARE_SAME_QUEST_ONLY);
                    return ChatColor.GRAY + "(" + (requireOpt ? ChatColor.GREEN 
                            + Lang.get(String.valueOf(requireOpt)) : ChatColor.RED 
                            + Lang.get(String.valueOf(requireOpt))) + ChatColor.GRAY +  ")";
                }
            case 4:
                return "";
            default:
                return null;
            }
        }

        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String text = ChatColor.DARK_GREEN + "- " + getTitle(context) + " -";
            for (int i = 1; i <= size; i++) {
                text += "\n" + getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                        + getSelectionText(context, i) + " " + getAdditionalText(context, i);
            }
            return text;
        }

        @Override
        public Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                tempKey = CK.OPT_SHARE_PROGRESS;
                tempPrompt = new OptionsMultiplayerPrompt(context);
                return new OptionsTrueFalsePrompt(context);
            case 2:
                tempKey = CK.OPT_SHARE_DISTANCE;
                tempPrompt = new OptionsMultiplayerPrompt(context);
                return new OptionsDistancePrompt(context);
            case 3:
                tempKey = CK.OPT_SHARE_SAME_QUEST_ONLY;
                tempPrompt = new OptionsMultiplayerPrompt(context);
                return new OptionsTrueFalsePrompt(context);
            case 4:
                tempKey = null;
                tempPrompt = null;
                try {
                    return new OptionsPrompt(context);
                } catch (final Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                    return Prompt.END_OF_CONVERSATION;
                }
            default:
                return null;
            }
        }
    }
}
