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

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.quests.FabricQuestsEditorIntegerPrompt;
import me.pikamug.quests.convo.quests.FabricQuestsEditorStringPrompt;
import me.pikamug.quests.quests.components.FabricOptions;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class FabricQuestOptionsPrompt extends FabricQuestsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;
    private String tempKey;
    private FabricQuestsEditorIntegerPrompt tempPrompt;

    public FabricQuestOptionsPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 4;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("optionsTitle").replace("<quest>", (String) Objects
                .requireNonNull(SessionData.get(uuid, Key.Q_NAME)));
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
        case 3:
            return ChatFormatting.BLUE;
        case 4:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.GOLD + FabricLang.get("optGeneral");
        case 2:
            return ChatFormatting.GOLD + FabricLang.get("optMultiplayer");
        case 3:
            return ChatFormatting.GOLD + "Server (Global)";
        case 4:
            return ChatFormatting.YELLOW + FabricLang.get("done");
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
        final StringBuilder text = new StringBuilder(ChatFormatting.DARK_GREEN + "- " + getTitle()
                .replace((String) Objects.requireNonNull(SessionData.get(uuid, Key.Q_NAME)), ChatFormatting.AQUA
                + (String) SessionData.get(uuid, Key.Q_NAME) + ChatFormatting.DARK_GREEN) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new FabricQuestOptionsGeneralPrompt(uuid).start();
            break;
        case 2:
            new FabricQuestOptionsMultiplayerPrompt(uuid).start();
            break;
        case 3:
            new FabricQuestOptionsGlobalPrompt(uuid).start();
            break;
        case 4:
            plugin.getQuestFactory().returnToMenu(uuid);
            break;
        default:
            new FabricQuestOptionsPrompt(uuid).start();
            break;
        }
    }

    public class FabricQuestOptionsTrueFalsePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestOptionsTrueFalsePrompt(final @NotNull UUID uuid) {
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
            return FabricLang.get("optBooleanQuery").replace("<true>", FabricLang.get("true"))
                    .replace("<false>", FabricLang.get("false"));
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("true");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("false");
            case 3:
                return ChatFormatting.RED + FabricLang.get("cmdClear");
            case 4:
                return ChatFormatting.RED + FabricLang.get("cmdCancel");
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + FabricLang.get("optBooleanPrompt").replace("<true>", FabricLang.get("true"))
                    .replace("<false>", FabricLang.get("false"));
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                if (input.startsWith("t") || input.equalsIgnoreCase(FabricLang.get("true"))
                        || input.equalsIgnoreCase(FabricLang.get("yesWord"))) {
                    SessionData.set(uuid, tempKey, true);
                } else if (input.startsWith("f") || input.equalsIgnoreCase(FabricLang.get("false"))
                        || input.equalsIgnoreCase(FabricLang.get("noWord"))) {
                    SessionData.set(uuid, tempKey, false);
                } else {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateInvalidInput")));
                    new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                    return;
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, tempKey, null);
            }
            tempPrompt.start();
        }
    }

    public class FabricQuestOptionsLevelPrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestOptionsLevelPrompt(final @NotNull UUID uuid) {
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
            return FabricLang.get("optNumberQuery");
        }

        @SuppressWarnings("unused")
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GOLD + "1";
            case 2:
                return ChatFormatting.GOLD + "2";
            case 3:
                return ChatFormatting.GOLD + "3";
            case 4:
                return ChatFormatting.GOLD + "4";
            case 5:
                return ChatFormatting.RED + FabricLang.get("cmdClear");
            case 6:
                return ChatFormatting.RED + FabricLang.get("cmdCancel");
            default:
                return null;
            }
        }

        @SuppressWarnings("unused")
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.GRAY + FabricLang.get("everything");
            case 2:
                return ChatFormatting.GRAY + FabricLang.get("objectives");
            case 3:
                return ChatFormatting.GRAY + FabricLang.get("stageEditorStages");
            case 4:
                return ChatFormatting.GRAY + FabricLang.get("quests");
            case 5:
            case 6:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            String text = FabricLang.get("optNumberPrompt");
            text += "\n" + ChatFormatting.GRAY + "\u2515 " + ChatFormatting.GOLD + "1" + ChatFormatting.RESET + " = " + ChatFormatting.GRAY
                    + FabricLang.get("everything");
            text += "\n" + ChatFormatting.GRAY + "\u2515 " + ChatFormatting.GOLD + "2" + ChatFormatting.RESET + " = " + ChatFormatting.GRAY
                    + FabricLang.get("objectives");
            text += "\n" + ChatFormatting.GRAY + "\u2515 " + ChatFormatting.GOLD + "3" + ChatFormatting.RESET + " = " + ChatFormatting.GRAY
                    + FabricLang.get("stageEditorStages");
            text += "\n" + ChatFormatting.GRAY + "\u2515 " + ChatFormatting.GOLD + "4" + ChatFormatting.RESET + " = " + ChatFormatting.GRAY
                    + FabricLang.get("quests");
            return ChatFormatting.YELLOW + text;
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    SessionData.set(uuid, tempKey, i);
                } catch (final Exception e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, tempKey, null);
            }
            tempPrompt.start();
        }
    }

    public class FabricQuestOptionsDistancePrompt extends FabricQuestsEditorStringPrompt {

        public FabricQuestOptionsDistancePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("optDistancePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (!input.equalsIgnoreCase(FabricLang.get("cmdCancel")) && !input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                try {
                    final double d = Double.parseDouble(input);
                    SessionData.set(uuid, tempKey, d);
                } catch (final Exception e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("reqNotANumber")
                            .replace("<input>", input)));
                }
            } else if (input.equalsIgnoreCase(FabricLang.get("cmdClear"))) {
                SessionData.set(uuid, tempKey, null);
            }
            tempPrompt.start();
        }
    }

    public class FabricQuestOptionsGeneralPrompt extends FabricQuestsEditorIntegerPrompt {

        public FabricQuestOptionsGeneralPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return ChatFormatting.DARK_GREEN + FabricLang.get("optGeneral");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatFormatting.BLUE;
            case 5:
                return ChatFormatting.GREEN;
            default:
                return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("optAllowCommands");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("optAllowQuitting");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("optIgnoreSilkTouch");
            case 4:
                return ChatFormatting.YELLOW + FabricLang.get("optIgnoreBlockReplace");
            case 5:
                return ChatFormatting.YELLOW + FabricLang.get("done");
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
                    final boolean defaultOpt = new FabricOptions().canAllowCommands();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (commandsOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                }
            case 2:
                final Boolean quittingOpt = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_QUITTING);
                if (quittingOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canAllowQuitting();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (quittingOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                }
            case 3:
                final Boolean ignoreOpt = (Boolean) SessionData.get(uuid, Key.OPT_IGNORE_SILK_TOUCH);
                if (ignoreOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canIgnoreSilkTouch();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (ignoreOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                }
            case 4:
                final Boolean ignoreBlockReplaceOpt = (Boolean) SessionData.get(uuid, Key.OPT_IGNORE_BLOCK_REPLACE);
                if (ignoreBlockReplaceOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canIgnoreBlockReplace();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (ignoreBlockReplaceOpt ? ChatFormatting.GREEN + FabricLang.get("true")
                            : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
                }
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.DARK_GREEN + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch (input.intValue()) {
            case 1:
                tempKey = Key.OPT_ALLOW_COMMANDS;
                tempPrompt = new FabricQuestOptionsGeneralPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 2:
                tempKey = Key.OPT_ALLOW_QUITTING;
                tempPrompt = new FabricQuestOptionsGeneralPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 3:
                tempKey = Key.OPT_IGNORE_SILK_TOUCH;
                tempPrompt = new FabricQuestOptionsGeneralPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 4:
                tempKey = Key.OPT_IGNORE_BLOCK_REPLACE;
                tempPrompt = new FabricQuestOptionsGeneralPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 5:
                tempKey = null;
                tempPrompt = null;
                try {
                    new FabricQuestOptionsPrompt(uuid).start();
                } catch (final Exception e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                }
                break;
            default:
                new FabricQuestOptionsPrompt(uuid).start();
                break;
            }
        }
    }

    public class FabricQuestOptionsMultiplayerPrompt extends FabricQuestsEditorIntegerPrompt {

        public FabricQuestOptionsMultiplayerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 7;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return ChatFormatting.DARK_GREEN + FabricLang.get("optMultiplayer");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return ChatFormatting.BLUE;
            case 7:
                return ChatFormatting.GREEN;
            default:
                return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("optExternalPartyPlugin");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("optUsePartiesPlugin");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("optShareProgressLevel");
            case 4:
                return ChatFormatting.YELLOW + FabricLang.get("optShareOnlySameQuest");
            case 5:
                return ChatFormatting.YELLOW + FabricLang.get("optShareDistance");
            case 6:
                return ChatFormatting.YELLOW + FabricLang.get("optHandleOfflinePlayer");
            case 7:
                return ChatFormatting.YELLOW + FabricLang.get("done");
            default:
                return null;
            }
        }

        @Override
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                final String externalOpt = (String) SessionData.get(uuid, Key.OPT_EXTERNAL_PARTY_PLUGIN);
                if (externalOpt != null) {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + externalOpt + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + FabricLang.get("notInstalled") + ")";
                }
            case 2:
                final Boolean partiesOpt = (Boolean) SessionData.get(uuid, Key.OPT_USE_PARTIES_PLUGIN);
                if (partiesOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canUsePartiesPlugin();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (partiesOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(partiesOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(partiesOpt))) + ChatFormatting.GRAY + ")";
                }
            case 3:
                final Integer shareOpt = (Integer) SessionData.get(uuid, Key.OPT_SHARE_PROGRESS_LEVEL);
                if (shareOpt == null) {
                    final int defaultOpt = new FabricOptions().getShareProgressLevel();
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + defaultOpt + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + shareOpt + ChatFormatting.GRAY + ")";
                }
            case 4:
                final Boolean requireOpt = (Boolean) SessionData.get(uuid, Key.OPT_SHARE_SAME_QUEST_ONLY);
                if (requireOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canShareSameQuestOnly();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (requireOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(requireOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(requireOpt))) + ChatFormatting.GRAY + ")";
                }
            case 5:
                final Double distanceOpt = (Double) SessionData.get(uuid, Key.OPT_SHARE_DISTANCE);
                if (distanceOpt == null) {
                    final double defaultOpt = new FabricOptions().getShareDistance();
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + defaultOpt + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + distanceOpt + ChatFormatting.GRAY + ")";
                }
            case 6:
                final Boolean handleOpt = (Boolean) SessionData.get(uuid, Key.OPT_HANDLE_OFFLINE_PLAYERS);
                if (handleOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canHandleOfflinePlayers();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (handleOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(handleOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(handleOpt))) + ChatFormatting.GRAY + ")";
                }
            case 7:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.DARK_GREEN + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch (input.intValue()) {
            case 1:
                tempKey = Key.OPT_EXTERNAL_PARTY_PLUGIN;
                tempPrompt = new FabricQuestOptionsMultiplayerPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 2:
                tempKey = Key.OPT_USE_PARTIES_PLUGIN;
                tempPrompt = new FabricQuestOptionsMultiplayerPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 3:
                tempKey = Key.OPT_SHARE_PROGRESS_LEVEL;
                tempPrompt = new FabricQuestOptionsMultiplayerPrompt(uuid);
                new FabricQuestOptionsLevelPrompt(uuid).start();
                break;
            case 4:
                tempKey = Key.OPT_SHARE_SAME_QUEST_ONLY;
                tempPrompt = new FabricQuestOptionsMultiplayerPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 5:
                tempKey = Key.OPT_SHARE_DISTANCE;
                tempPrompt = new FabricQuestOptionsMultiplayerPrompt(uuid);
                new FabricQuestOptionsDistancePrompt(uuid).start();
                break;
            case 6:
                tempKey = Key.OPT_HANDLE_OFFLINE_PLAYERS;
                tempPrompt = new FabricQuestOptionsMultiplayerPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 7:
                tempKey = null;
                tempPrompt = null;
                try {
                    new FabricQuestOptionsPrompt(uuid).start();
                } catch (final Exception e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                    return;
                }
                break;
            default:
                new FabricQuestOptionsPrompt(uuid).start();
                break;
            }
        }
    }

    public class FabricQuestOptionsGlobalPrompt extends FabricQuestsEditorIntegerPrompt {

        public FabricQuestOptionsGlobalPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        private final int size = 5;

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public String getTitle() {
            return ChatFormatting.DARK_GREEN + FabricLang.get("optServer");
        }

        @Override
        public ChatFormatting getNumberColor(final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
            case 4:
                return ChatFormatting.BLUE;
            case 5:
                return ChatFormatting.GREEN;
            default:
                return null;
            }
        }

        @Override
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatFormatting.YELLOW + FabricLang.get("optGiveLoginGlobal");
            case 2:
                return ChatFormatting.YELLOW + FabricLang.get("optAllowStackingGlobal");
            case 3:
                return ChatFormatting.YELLOW + FabricLang.get("optInformStartGlobal");
            case 4:
                return ChatFormatting.YELLOW + FabricLang.get("optOverrideSettingGlobal");
            case 5:
                return ChatFormatting.YELLOW + FabricLang.get("done");
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
                    final boolean defaultOpt = new FabricOptions().canGiveGloballyAtLogin();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (globalOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(globalOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(globalOpt))) + ChatFormatting.GRAY + ")";
                }
            case 2:
                final Boolean stackOpt = (Boolean) SessionData.get(uuid, Key.OPT_ALLOW_STACKING_GLOBAL);
                if (stackOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canAllowStackingGlobal();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (stackOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(stackOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(stackOpt))) + ChatFormatting.GRAY + ")";
                }
            case 3:
                final Boolean informOpt = (Boolean) SessionData.get(uuid, Key.OPT_INFORM_QUEST_START);
                if (informOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canInformOnStart();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (informOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(informOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(informOpt))) + ChatFormatting.GRAY + ")";
                }
            case 4:
                final Boolean overrideOpt = (Boolean) SessionData.get(uuid, Key.OPT_OVERRIDE_MAX_QUESTS);
                if (overrideOpt == null) {
                    final boolean defaultOpt = new FabricOptions().canOverrideMaxQuests();
                    return ChatFormatting.GRAY + "(" + (defaultOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(defaultOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(defaultOpt))) + ChatFormatting.GRAY + ")";
                } else {
                    return ChatFormatting.GRAY + "(" + (overrideOpt ? ChatFormatting.GREEN
                            + FabricLang.get(String.valueOf(overrideOpt)) : ChatFormatting.RED
                            + FabricLang.get(String.valueOf(overrideOpt))) + ChatFormatting.GRAY + ")";
                }
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final StringBuilder text = new StringBuilder(ChatFormatting.DARK_GREEN + "- " + getTitle() + " -");
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                        .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
        }

        @Override
        public void acceptInput(final Number input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            switch (input.intValue()) {
            case 1:
                tempKey = Key.OPT_GIVE_GLOBALLY_AT_LOGIN;
                tempPrompt = new FabricQuestOptionsGlobalPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 2:
                tempKey = Key.OPT_ALLOW_STACKING_GLOBAL;
                tempPrompt = new FabricQuestOptionsGlobalPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 3:
                tempKey = Key.OPT_INFORM_QUEST_START;
                tempPrompt = new FabricQuestOptionsGlobalPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 4:
                tempKey = Key.OPT_OVERRIDE_MAX_QUESTS;
                tempPrompt = new FabricQuestOptionsGlobalPrompt(uuid);
                new FabricQuestOptionsTrueFalsePrompt(uuid).start();
                break;
            case 5:
                tempKey = null;
                tempPrompt = null;
                try {
                    new FabricQuestOptionsPrompt(uuid).start();
                } catch (final Exception e) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("itemCreateCriticalError")));
                    return;
                }
                break;
            default:
                new FabricQuestOptionsPrompt(uuid).start();
                break;
            }
        }
    }
}
