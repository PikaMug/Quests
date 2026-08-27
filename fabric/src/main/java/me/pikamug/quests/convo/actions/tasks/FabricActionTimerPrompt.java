/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.convo.actions.tasks;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.convo.actions.FabricActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.FabricActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.FabricActionMainPrompt;
import me.pikamug.quests.util.FabricLang;
import me.pikamug.quests.util.FabricMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.SessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FabricActionTimerPrompt extends FabricActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final FabricQuestsPlugin plugin;

    public FabricActionTimerPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = FabricQuestsPlugin.getInstance();
    }

    private final int size = 3;

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return FabricLang.get("eventEditorTimer");
    }

    @Override
    public ChatFormatting getNumberColor(final int number) {
        switch (number) {
        case 1:
        case 2:
            return ChatFormatting.BLUE;
        case 3:
            return ChatFormatting.GREEN;
        default:
            return null;
        }
    }

    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorSetTimer");
        case 2:
            return ChatFormatting.YELLOW + FabricLang.get("eventEditorCancelTimer");
        case 3:
            return ChatFormatting.GREEN + FabricLang.get("done");
        default:
            return null;
        }
    }

    @Override
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (SessionData.get(uuid, Key.A_TIMER) == null) {
                return ChatFormatting.GRAY + "(" + FabricLang.get("noneSet") + ")";
            } else {
                final Integer timer = (Integer) SessionData.get(uuid, Key.A_TIMER);
                if (timer != null) {
                    return ChatFormatting.GRAY + "(" + ChatFormatting.AQUA + FabricMiscUtil.formatTime(timer * 1000L) + ChatFormatting.GRAY
                            + ")";
                }
                break;
            }
        case 2:
            if (SessionData.get(uuid, Key.A_CANCEL_TIMER) == null) {
                return ChatFormatting.GRAY + "(" + ChatFormatting.RED + FabricLang.get("false") + ChatFormatting.GRAY + ")";
            } else {
                final Boolean timerOpt = (Boolean) SessionData.get(uuid, Key.A_CANCEL_TIMER);
                return ChatFormatting.GRAY + "(" + (Boolean.TRUE.equals(timerOpt) ? ChatFormatting.GREEN + FabricLang.get("true")
                        : ChatFormatting.RED + FabricLang.get("false")) + ChatFormatting.GRAY + ")";
            }
        case 3:
            return "";
        default:
            return null;
        }
        return "";
    }

    @Override
    public @NotNull String getPromptText() {
        if (SessionData.get(uuid, Key.A_CANCEL_TIMER) == null) {
            SessionData.set(uuid, Key.A_CANCEL_TIMER, false);
        }

        final StringBuilder text = new StringBuilder(ChatFormatting.GOLD + "- " + getTitle() + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(i)).append(ChatFormatting.BOLD).append(i)
                    .append(ChatFormatting.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                    .append(getAdditionalText(i));
        }
        return sendClickableSelection(text.toString(), plugin.getQuester(uuid));
    }

    @Override
    public void acceptInput(final Number input) {
        switch (input.intValue()) {
        case 1:
            new ActionTimerFailPrompt(uuid).start();
            break;
        case 2:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_CANCEL_TIMER);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.A_CANCEL_TIMER, false);
            } else {
                SessionData.set(uuid, Key.A_CANCEL_TIMER, true);
            }
            new FabricActionTimerPrompt(uuid).start();
            break;
        case 3:
            new FabricActionMainPrompt(uuid).start();
            break;
        default:
            new FabricActionTimerPrompt(uuid).start();
            break;
        }
    }

    public class ActionTimerFailPrompt extends FabricActionsEditorStringPrompt {

        public ActionTimerFailPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return FabricLang.get("eventEditorEnterTimerSeconds");
        }

        @Override
        public @NotNull String getPromptText() {
            return ChatFormatting.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            final ServerPlayer sender = FabricMiscUtil.getPlayer(uuid, plugin);
            if (sender == null) {
                return;
            }
            try {
                final int i = Integer.parseInt(input);
                if (i < 1) {
                    sender.sendSystemMessage(Component.literal(ChatFormatting.RED + FabricLang.get("questEditorPositiveAmount")));
                } else {
                    SessionData.set(uuid, Key.A_TIMER, i);
                }
            } catch (final NumberFormatException e) {
                sender.sendSystemMessage(Component.literal(ChatFormatting.RED
                        + FabricLang.get("reqNotANumber").replace("<input>", input)));
                new ActionTimerFailPrompt(uuid).start();
                return;
            }
            new FabricActionTimerPrompt(uuid).start();
        }
    }
}
