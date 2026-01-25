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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.QuestsIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.QuestsEditorStringPrompt;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.quests.BukkitQuestsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.quests.components.BukkitOptions;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import me.pikamug.unite.api.objects.PartyProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class QuestOptionsPrompt extends QuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    private String tempKey;
    private QuestsIntegerPrompt tempPrompt;

    public QuestOptionsPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 4;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("optionsTitle").replace("<quest>", (String) Objects
                .requireNonNull(SessionData.get(uuid, Key.Q_NAME)));
    }
    
    @Override
    public ChatColor getNumberColor(final int number) {
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
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.GOLD + BukkitLang.get("optGeneral");
        case 2:
            return ChatColor.GOLD + BukkitLang.get("optMultiplayer");
        case 3:
            return ChatColor.GOLD + "Server (Global)";
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    public String getAdditionalText(final int number) {
        return null;
    }

    @Override
    public @NotNull String getPromptText() {
        final BukkitQuestsEditorPostOpenNumericPromptEvent event
                = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);
        
        final StringBuilder text = new StringBuilder(ChatColor.DARK_GREEN + "- "  + getTitle()
                .replace((String) Objects.requireNonNull(SessionData.get(uuid, Key.Q_NAME)), ChatColor.AQUA
                + (String) SessionData.get(uuid, Key.Q_NAME) + ChatColor.DARK_GREEN) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(i));
        }
        return text.toString();
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new QuestOptionsGeneralPrompt(uuid).start();
        case 2:
            new QuestOptionsMultiplayerPrompt(uuid).start();
        case 3:
            new QuestOptionsGlobalPrompt(uuid).start();
        case 4:
            plugin.getQuestFactory().returnToMenu(uuid);
        default:
            new QuestOptionsPrompt(uuid).start();
        }
    }

    public class QuestOptionsPluginPrompt extends QuestsEditorStringPrompt {

        public QuestOptionsPluginPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return BukkitLang.get("optPluginListTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("optExternalPartyPluginPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            StringBuilder text = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n"
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
            text.append(ChatColor.YELLOW).append(getQueryText());
            return text.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (input.equalsIgnoreCase("Quests")) {
                    sender.sendMessage(" " + ChatColor.AQUA + ChatColor.UNDERLINE
                            + "https://www.youtube.com/watch?v=gvdf5n-zI14");
                    new QuestOptionsPluginPrompt(uuid).start();
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
                    sender.sendMessage(text);
                    new QuestOptionsPluginPrompt(uuid).start();
                }
                SessionData.set(uuid, Key.OPT_EXTERNAL_PARTY_PLUGIN, properCase);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, tempKey, null);
            }
            tempPrompt.start();
        }
    }
    
    public class QuestOptionsTrueFalsePrompt extends QuestsEditorStringPrompt {

        public QuestOptionsTrueFalsePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 4;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return null;
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("optBooleanQuery").replace("<true>", BukkitLang.get("true"))
                    .replace("<false>", BukkitLang.get("false"));
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + BukkitLang.get("optBooleanPrompt").replace("<true>", BukkitLang.get("true"))
                    .replace("<false>", BukkitLang.get("false"));
        }
        
        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                if (input.startsWith("t") || input.equalsIgnoreCase(BukkitLang.get("true"))
                        || input.equalsIgnoreCase(BukkitLang.get("yesWord"))) {
                    SessionData.set(uuid, tempKey, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(BukkitLang.get("false"))
                        || input.equalsIgnoreCase(BukkitLang.get("noWord"))) {
                    SessionData.set(uuid, tempKey, false);
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateInvalidInput"));
                    new QuestOptionsTrueFalsePrompt(uuid).start();
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, tempKey, null);
            }
            tempPrompt.start();
        }
    }
    
    public class QuestOptionsLevelPrompt extends QuestsEditorStringPrompt {

        public QuestOptionsLevelPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 6;
        
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return null;
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("optNumberQuery");
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
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
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    SessionData.set(uuid, tempKey, i);
                } catch (final Exception e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, tempKey, null);
            }
            tempPrompt.start();
        }
    }
    
    public class QuestOptionsDistancePrompt extends QuestsEditorStringPrompt {

        public QuestOptionsDistancePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }
        
        @Override
        public String getQueryText() {
            return BukkitLang.get("optDistancePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenStringPromptEvent event
                    = new BukkitQuestsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final double d = Double.parseDouble(input);
                    SessionData.set(uuid, tempKey, d);
                } catch (final Exception e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                            .replace("<input>", input));
                }
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, tempKey, null);
            }
            tempPrompt.start();
        }
    }
    
    public class QuestOptionsGeneralPrompt extends QuestsEditorIntegerPrompt {

        public QuestOptionsGeneralPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return ChatColor.DARK_GREEN + BukkitLang.get("optGeneral");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                final Boolean commandsOpt = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_COMMANDS);
                if (commandsOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canAllowCommands();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (commandsOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                }
            case 2:
                final Boolean quittingOpt = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_QUITTING);
                if (quittingOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canAllowQuitting();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (quittingOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                }
            case 3:
                final Boolean ignoreOpt = (Boolean) SessionData.get(uuid, Key.OPT_IGNORE_SILK_TOUCH);
                if (ignoreOpt == null) {
                    final boolean defaultOpt = new BukkitOptions().canIgnoreSilkTouch();
                    return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + (ignoreOpt ? ChatColor.GREEN + BukkitLang.get("true")
                            : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
                }
            case 4:
                final Boolean ignoreBlockReplaceOpt = (Boolean) SessionData.get(uuid, Key.OPT_IGNORE_BLOCK_REPLACE);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_GREEN + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                tempKey = Key.OPT_ALLOW_COMMANDS;
                tempPrompt = new QuestOptionsGeneralPrompt(uuid);
                new QuestOptionsTrueFalsePrompt(uuid).start();
            case 2:
                tempKey = Key.OPT_ALLOW_QUITTING;
                tempPrompt = new QuestOptionsGeneralPrompt(uuid);
                new QuestOptionsTrueFalsePrompt(uuid).start();
            case 3:
                tempKey = Key.OPT_IGNORE_SILK_TOUCH;
                tempPrompt = new QuestOptionsGeneralPrompt(uuid);
                new QuestOptionsTrueFalsePrompt(uuid).start();
            case 4:
                tempKey = Key.OPT_IGNORE_BLOCK_REPLACE;
                tempPrompt = new QuestOptionsGeneralPrompt(uuid);
                new QuestOptionsTrueFalsePrompt(uuid).start();
            case 5:
                tempKey = null;
                tempPrompt = null;
                try {
                    new QuestOptionsPrompt(uuid).start();
                } catch (final Exception e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                    return;
                }
            default:
                return;
            }
        }
    }
    
    public class QuestOptionsMultiplayerPrompt extends QuestsEditorIntegerPrompt {

        public QuestOptionsMultiplayerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 7;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return ChatColor.DARK_GREEN + BukkitLang.get("optMultiplayer");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
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
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                final String externalOpt = (String) SessionData.get(uuid, Key.OPT_EXTERNAL_PARTY_PLUGIN);
                if (plugin.getDependencies().getPartyProvider() == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("notInstalled") + ")";
                } else if (externalOpt != null){
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + externalOpt + ChatColor.GRAY + ")";
                } else {
                    return "";
                }
            case 2:
                final Boolean partiesOpt = (Boolean) SessionData.get(uuid, Key.OPT_USE_PARTIES_PLUGIN);
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
                final Integer shareOpt = (Integer) SessionData.get(uuid, Key.OPT_SHARE_PROGRESS_LEVEL);
                if (shareOpt == null) {
                    final int defaultOpt = new BukkitOptions().getShareProgressLevel();
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + defaultOpt + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + shareOpt + ChatColor.GRAY + ")";
                }
            case 4:
                final Boolean requireOpt = (Boolean) SessionData.get(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY);
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
                final Double distanceOpt = (Double) SessionData.get(uuid, Key.OPT_SHARE_DISTANCE);
                if (distanceOpt == null) {
                    final double defaultOpt = new BukkitOptions().getShareDistance();
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + defaultOpt + ChatColor.GRAY + ")";
                } else {
                    return ChatColor.GRAY + "(" + ChatColor.AQUA + distanceOpt + ChatColor.GRAY + ")";
                }
            case 6:
                final Boolean handleOpt = (Boolean) SessionData.get(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS);
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
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.DARK_GREEN + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                tempKey = Key.OPT_EXTERNAL_PARTY_PLUGIN;
                tempPrompt = new QuestOptionsMultiplayerPrompt(uuid);
                new QuestOptionsPluginPrompt(uuid).start();
            case 2:
                tempKey = Key.OPT_USE_PARTIES_PLUGIN;
                tempPrompt = new QuestOptionsMultiplayerPrompt(uuid);
                new QuestOptionsTrueFalsePrompt(uuid).start();
            case 3:
                tempKey = Key.OPT_SHARE_PROGRESS_LEVEL;
                tempPrompt = new QuestOptionsMultiplayerPrompt(uuid);
                new QuestOptionsLevelPrompt(uuid).start();
            case 4:
                tempKey = Key.OPT_SHARE_SAME_QUEST_ONLY;
                tempPrompt = new QuestOptionsMultiplayerPrompt(uuid);
                new QuestOptionsTrueFalsePrompt(uuid).start();
            case 5:
                tempKey = Key.OPT_SHARE_DISTANCE;
                tempPrompt = new QuestOptionsMultiplayerPrompt(uuid);
                new QuestOptionsDistancePrompt(uuid).start();
            case 6:
                tempKey = Key.OPT_HANDLE_OFFLINE_PLAYERS;
                tempPrompt = new QuestOptionsMultiplayerPrompt(uuid);
                new QuestOptionsTrueFalsePrompt(uuid).start();
            case 7:
                tempKey = null;
                tempPrompt = null;
                try {
                    new QuestOptionsPrompt(uuid).start();
                } catch (final Exception e) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                    return;
                }
            default:
                return;
            }
        }
    }

    public class QuestOptionsGlobalPrompt extends QuestsEditorIntegerPrompt {

        public QuestOptionsGlobalPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return ChatColor.DARK_GREEN + BukkitLang.get("optServer");
        }

        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
            switch (number) {
                case 1:
                    return ChatColor.YELLOW + BukkitLang.get("optGiveLoginGlobal");
                case 2:
                    return ChatColor.YELLOW + BukkitLang.get("optAllowStackingGlobal");
                case 3:
                    return ChatColor.YELLOW + BukkitLang.get("optInformStartGlobal");
                case 4:
                    return ChatColor.YELLOW + BukkitLang.get("optOverrideSettingGlobal");
                case 5:
                    return ChatColor.YELLOW + BukkitLang.get("done");
                default:
                    return null;
            }
        }

        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
                case 1:
                    final Boolean globalOpt = (Boolean) SessionData.get(uuid, Key.OPT_GIVE_GLOBALLY_AT_LOGIN);
                    if (globalOpt == null) {
                        final boolean defaultOpt = new BukkitOptions().canGiveGloballyAtLogin();
                        return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                    } else {
                        return ChatColor.GRAY + "(" + (globalOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(globalOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(globalOpt))) + ChatColor.GRAY + ")";
                    }
                case 2:
                    final Boolean stackOpt = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_STACKING_GLOBAL);
                    if (stackOpt == null) {
                        final boolean defaultOpt = new BukkitOptions().canAllowStackingGlobal();
                        return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                    } else {
                        return ChatColor.GRAY + "(" + (stackOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(stackOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(stackOpt))) + ChatColor.GRAY + ")";
                    }
                case 3:
                    final Boolean informOpt = (Boolean) SessionData.get(uuid, Key.OPT_INFORM_QUEST_START);
                    if (informOpt == null) {
                        final boolean defaultOpt = new BukkitOptions().canInformOnStart();
                        return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                    } else {
                        return ChatColor.GRAY + "(" + (informOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(informOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(informOpt))) + ChatColor.GRAY + ")";
                    }
                case 4:
                    final Boolean overrideOpt = (Boolean) SessionData.get(uuid, Key.OPT_OVERRIDE_MAX_QUESTS);
                    if (overrideOpt == null) {
                        final boolean defaultOpt = new BukkitOptions().canInformOnStart();
                        return ChatColor.GRAY + "(" + (defaultOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(defaultOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(defaultOpt))) + ChatColor.GRAY + ")";
                    } else {
                        return ChatColor.GRAY + "(" + (overrideOpt ? ChatColor.GREEN
                                + BukkitLang.get(String.valueOf(overrideOpt)) : ChatColor.RED
                                + BukkitLang.get(String.valueOf(overrideOpt))) + ChatColor.GRAY + ")";
                    }
                case 5:
                    return "";
                default:
                    return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitQuestsEditorPostOpenNumericPromptEvent event
                    = new BukkitQuestsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder text = new StringBuilder(ChatColor.DARK_GREEN + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
                case 1:
                    tempKey = Key.OPT_GIVE_GLOBALLY_AT_LOGIN;
                    tempPrompt = new QuestOptionsGlobalPrompt(uuid);
                    new QuestOptionsTrueFalsePrompt(uuid).start();
                case 2:
                    tempKey = Key.OPT_ALLOW_STACKING_GLOBAL;
                    tempPrompt = new QuestOptionsGlobalPrompt(uuid);
                    new QuestOptionsTrueFalsePrompt(uuid).start();
                case 3:
                    tempKey = Key.OPT_INFORM_QUEST_START;
                    tempPrompt = new QuestOptionsGlobalPrompt(uuid);
                    new QuestOptionsTrueFalsePrompt(uuid).start();
                case 4:
                    tempKey = Key.OPT_OVERRIDE_MAX_QUESTS;
                    tempPrompt = new QuestOptionsGlobalPrompt(uuid);
                    new QuestOptionsTrueFalsePrompt(uuid).start();
                case 5:
                    tempKey = null;
                    tempPrompt = null;
                    try {
                        new QuestOptionsPrompt(uuid).start();
                    } catch (final Exception e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("itemCreateCriticalError"));
                        return;
                    }
                default:
                    return;
            }
        }
    }
}
