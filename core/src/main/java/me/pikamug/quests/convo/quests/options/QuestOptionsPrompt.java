/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.quests.options;

import me.pikamug.quests.quests.components.BukkitOptions;
import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.quests.QuestsEditorNumericPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.unite.api.objects.PartyProvider;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class QuestOptionsPrompt extends QuestsEditorNumericPrompt {

    private final BukkitQuestsPlugin plugin;
    private String tempKey;
    private Prompt tempPrompt;

    public QuestOptionsPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (BukkitQuestsPlugin)context.getPlugin();
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return BukkitLang.get("optionsTitle").replace("<quest>", (String) Objects
                .requireNonNull(context.getSessionData(Key.Q_NAME)));
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
        switch (number) {
        case 1:
            return ChatColor.GOLD + BukkitLang.get("optGeneral");
        case 2:
            return ChatColor.GOLD + BukkitLang.get("optMultiplayer");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final ConversationContext context, final int number) {
        return null;
    }

    @Override
    public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event
                = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.DARK_GREEN + "- "  + getTitle(context)
                .replace((String) Objects.requireNonNull(context.getSessionData(Key.Q_NAME)), ChatColor.AQUA
                + (String) context.getSessionData(Key.Q_NAME) + ChatColor.DARK_GREEN) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new QuestOptionsGeneralPrompt(context);
        case 2:
            return new QuestOptionsMultiplayerPrompt(context);
        case 3:
            return plugin.getQuestFactory().returnToMenu(context);
        default:
            return new QuestOptionsPrompt(context);
        }
    }

    public class QuestOptionsPluginPrompt extends QuestsEditorStringPrompt {

        public QuestOptionsPluginPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return BukkitLang.get("optPluginListTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("optExternalPartyPluginPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);

            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n"
                    + ChatColor.DARK_PURPLE);
            boolean none = true;
            for (final PartyProvider q : plugin.getDependencies().getPartyProviders()) {
                text.append(q.getPluginName()).append(", ");
                none = false;
            }
            if (none) {
                text.append("(").append(BukkitLang.get("none")).append(")\n");
            } else {
                text = new StringBuilder(text.substring(0, (text.length() - 2)));
                text.append("\n");
            }
            text.append(ChatColor.YELLOW).append(getQueryText(context));
            return text.toString();
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (input.equalsIgnoreCase("Quests")) {
                    context.getForWhom().sendRawMessage(" " + ChatColor.AQUA + ChatColor.UNDERLINE
                            + "https://www.youtube.com/watch?v=gvdf5n-zI14");
                    return new QuestOptionsPluginPrompt(context);
                }
                String properCase = null;
                for (final PartyProvider partyProvider : plugin.getDependencies().getPartyProviders()) {
                    if (input.equalsIgnoreCase(partyProvider.getPluginName())) {
                        properCase = partyProvider.getPluginName();
                    }
                }
                if (properCase == null) {
                    String text = BukkitLang.get("optNotAPluginName");
                    text = text.replace("<plugin>", ChatColor.LIGHT_PURPLE + input + ChatColor.RED);
                    context.getForWhom().sendRawMessage(text);
                    return new QuestOptionsPluginPrompt(context);
                }
                context.setSessionData(Key.OPT_EXTERNAL_PARTY_PLUGIN, properCase);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class QuestOptionsTrueFalsePrompt extends QuestsEditorStringPrompt {

        public QuestOptionsTrueFalsePrompt(final ConversationContext context) {
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
            return BukkitLang.get("optBooleanQuery").replace("<true>", BukkitLang.get("true"))
                    .replace("<false>", BukkitLang.get("false"));
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("true");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("false");
            case 3:
                return ChatColor.RED + BukkitLang.get("cmdClear");
            case 4:
                return ChatColor.RED + BukkitLang.get("cmdCancel");
            default:
                return null;
            }
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + BukkitLang.get("optBooleanPrompt").replace("<true>", BukkitLang.get("true"))
                    .replace("<false>", BukkitLang.get("false"));
        }
        
        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (input.startsWith("t") || input.equalsIgnoreCase(BukkitLang.get("true"))
                        || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                    context.setSessionData(tempKey, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(BukkitLang.get("false"))
                        || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                    context.setSessionData(tempKey, false);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    return new QuestOptionsTrueFalsePrompt(context);
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class QuestOptionsLevelPrompt extends QuestsEditorStringPrompt {

        public QuestOptionsLevelPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 6;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("optNumberQuery");
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final ConversationContext context, final int number) {
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
                return ChatColor.RED + BukkitLang.get("cmdClear");
            case 6:
                return ChatColor.RED + BukkitLang.get("cmdCancel");
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.GRAY + BukkitLang.get("everything");
            case 2:
                return ChatColor.GRAY + BukkitLang.get("objectives");
            case 3:
                return ChatColor.GRAY + BukkitLang.get("stageEditorStages");
            case 4:
                return ChatColor.GRAY + BukkitLang.get("quests");
            case 5:
            case 6:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            String text = BukkitLang.get("optNumberPrompt");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "1" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + BukkitLang.get("everything");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "2" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + BukkitLang.get("objectives");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "3" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + BukkitLang.get("stageEditorStages");
            text += "\n" + ChatColor.GRAY + "\u2515 " + ChatColor.GOLD + "4" + ChatColor.RESET + " = " + ChatColor.GRAY
                    + BukkitLang.get("quests");
            return ChatColor.YELLOW + text;
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    context.setSessionData(tempKey, i);
                } catch (final Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class QuestOptionsDistancePrompt extends QuestsEditorStringPrompt {

        public QuestOptionsDistancePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }
        
        @Override
        public String getQueryText(final ConversationContext context) {
            return BukkitLang.get("optDistancePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event
                    = new QuestsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final double d = Double.parseDouble(input);
                    context.setSessionData(tempKey, d);
                } catch (final Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                context.setSessionData(tempKey, null);
                return tempPrompt;
            }
            return tempPrompt;
        }
    }
    
    public class QuestOptionsGeneralPrompt extends QuestsEditorNumericPrompt {

        public QuestOptionsGeneralPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return ChatColor.DARK_GREEN + BukkitLang.get("optGeneral");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatColor.BLUE;
            case 5:
                return ChatColor.GREEN;
            default:
                return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("optAllowCommands");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("optAllowQuitting");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("optIgnoreSilkTouch");
            case 4:
                return ChatColor.YELLOW + BukkitLang.get("optIgnoreBlockReplace");
            case 5:
                return ChatColor.YELLOW + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                final Boolean commandsOpt = (Boolean) context.getSessionData(Key.OPT_ALLOW_COMMANDS);
                if (commandsOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canAllowCommands();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (commandsOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                }
            case 2:
                final Boolean quittingOpt = (Boolean) context.getSessionData(Key.OPT_ALLOW_QUITTING);
                if (quittingOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canAllowQuitting();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (quittingOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                }
            case 3:
                final Boolean ignoreOpt = (Boolean) context.getSessionData(Key.OPT_IGNORE_SILK_TOUCH);
                if (ignoreOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canIgnoreSilkTouch();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (ignoreOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                }
            case 4:
                final Boolean ignoreBlockReplaceOpt = (Boolean) context.getSessionData(Key.OPT_IGNORE_BLOCK_REPLACE);
                if (ignoreBlockReplaceOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canIgnoreBlockReplace();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (ignoreBlockReplaceOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                }
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_GREEN + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @Override
        public Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                tempKey = Key.OPT_ALLOW_COMMANDS;
                tempPrompt = new QuestOptionsGeneralPrompt(context);
                return new QuestOptionsTrueFalsePrompt(context);
            case 2:
                tempKey = Key.OPT_ALLOW_QUITTING;
                tempPrompt = new QuestOptionsGeneralPrompt(context);
                return new QuestOptionsTrueFalsePrompt(context);
            case 3:
                tempKey = Key.OPT_IGNORE_SILK_TOUCH;
                tempPrompt = new QuestOptionsGeneralPrompt(context);
                return new QuestOptionsTrueFalsePrompt(context);
            case 4:
                tempKey = Key.OPT_IGNORE_BLOCK_REPLACE;
                tempPrompt = new QuestOptionsGeneralPrompt(context);
                return new QuestOptionsTrueFalsePrompt(context);
            case 5:
                tempKey = null;
                tempPrompt = null;
                try {
                    return new QuestOptionsPrompt(context);
                } catch (final Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                    return Prompt.END_OF_CONVERSATION;
                }
            default:
                return null;
            }
        }
    }
    
    public class QuestOptionsMultiplayerPrompt extends QuestsEditorNumericPrompt {

        public QuestOptionsMultiplayerPrompt(final ConversationContext context) {
            super(context);
        }

        private final int size = 7;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return ChatColor.DARK_GREEN + BukkitLang.get("optMultiplayer");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return ChatColor.BLUE;
            case 7:
                return ChatColor.GREEN;
             default:
                return null;
            }
        }
        
        @Override
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("optExternalPartyPlugin");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("optUsePartiesPlugin");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("optShareProgressLevel");
            case 4:
                return ChatColor.YELLOW + BukkitLang.get("optShareOnlySameQuest");
            case 5:
                return ChatColor.YELLOW + BukkitLang.get("optShareDistance");
            case 6:
                return ChatColor.YELLOW + BukkitLang.get("optHandleOfflinePlayer");
            case 7:
                return ChatColor.YELLOW + BukkitLang.get("done");
             default:
                return null;
            }
        }
        
        @Override
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                final String externalOpt = (String) context.getSessionData(Key.OPT_EXTERNAL_PARTY_PLUGIN);
                if (plugin.getDependencies().getPartyProvider() == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
                } else if (externalOpt != null){
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + externalOpt + ChatColor.GRAY + ")";
                } else {
                    return "";
                }
            case 2:
                final Boolean partiesOpt = (Boolean) context.getSessionData(Key.OPT_USE_PARTIES_PLUGIN);
                if (partiesOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canUsePartiesPlugin();
                    return ChatColor.GRAY + "("+ (defaultOpt ? ChatColor.GREEN 
                           + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                           + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (partiesOpt ? ChatColor.GREEN 
                            + BukkitLang.get(String.valueOf(partiesOpt)) : ChatColor.RED
                            + BukkitLang.get(String.valueOf(partiesOpt))) + ChatColor.GRAY +  ")";
                }
            case 3:
                final Integer shareOpt = (Integer) context.getSessionData(Key.OPT_SHARE_PROGRESS_LEVEL);
                if (shareOpt == null) {
                    final int defaultOpt = new BukkitOptions().getShareProgressLevel();
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + defaultOpt + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + shareOpt + ChatColor.GRAY + ")";
                }
            case 4:
                final Boolean requireOpt = (Boolean) context.getSessionData(Key.OPT_SHARE_SAME_QUEST_ONLY);
                if (requireOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canShareSameQuestOnly();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN 
                            + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                            + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (requireOpt ? ChatColor.GREEN 
                            + BukkitLang.get(String.valueOf(requireOpt)) : ChatColor.RED
                            + BukkitLang.get(String.valueOf(requireOpt))) + ChatColor.GRAY +  ")";
                }
            case 5:
                final Double distanceOpt = (Double) context.getSessionData(Key.OPT_SHARE_DISTANCE);
                if (distanceOpt == null) {
                    final double defaultOpt = new BukkitOptions().getShareDistance();
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + defaultOpt + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + distanceOpt + ChatColor.GRAY + ")";
                }
            case 6:
                final Boolean handleOpt = (Boolean) context.getSessionData(Key.OPT_HANDLE_OFFLINE_PLAYERS);
                if (handleOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canHandleOfflinePlayers();
                    return ChatColor.GRAY + "("+ (defaultOpt ? ChatColor.GREEN 
                           + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                           + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (handleOpt ? ChatColor.GREEN 
                            + BukkitLang.get(String.valueOf(handleOpt)) : ChatColor.RED
                            + BukkitLang.get(String.valueOf(handleOpt))) + ChatColor.GRAY +  ")";
                }
            case 7:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final QuestsEditorPostOpenNumericPromptEvent event
                    = new QuestsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_GREEN + "- " + getTitle(context) + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @Override
        public Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                tempKey = Key.OPT_EXTERNAL_PARTY_PLUGIN;
                tempPrompt = new QuestOptionsMultiplayerPrompt(context);
                return new QuestOptionsPluginPrompt(context);
            case 2:
                tempKey = Key.OPT_USE_PARTIES_PLUGIN;
                tempPrompt = new QuestOptionsMultiplayerPrompt(context);
                return new QuestOptionsTrueFalsePrompt(context);
            case 3:
                tempKey = Key.OPT_SHARE_PROGRESS_LEVEL;
                tempPrompt = new QuestOptionsMultiplayerPrompt(context);
                return new QuestOptionsLevelPrompt(context);
            case 4:
                tempKey = Key.OPT_SHARE_SAME_QUEST_ONLY;
                tempPrompt = new QuestOptionsMultiplayerPrompt(context);
                return new QuestOptionsTrueFalsePrompt(context);
            case 5:
                tempKey = Key.OPT_SHARE_DISTANCE;
                tempPrompt = new QuestOptionsMultiplayerPrompt(context);
                return new QuestOptionsDistancePrompt(context);
            case 6:
                tempKey = Key.OPT_HANDLE_OFFLINE_PLAYERS;
                tempPrompt = new QuestOptionsMultiplayerPrompt(context);
                return new QuestOptionsTrueFalsePrompt(context);
            case 7:
                tempKey = null;
                tempPrompt = null;
                try {
                    return new QuestOptionsPrompt(context);
                } catch (final Exception e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                    return Prompt.END_OF_CONVERSATION;
                }
            default:
                return null;
            }
        }
    }
}
