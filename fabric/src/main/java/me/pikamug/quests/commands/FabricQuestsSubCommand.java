/*
 * Copyright (c) PikaMug and contributors
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.pikamug.quests.commands;

import me.pikamug.quests.FabricQuestsPlugin;
import me.pikamug.quests.player.FabricQuester;
import me.pikamug.quests.player.Quester;
import me.pikamug.quests.quests.Quest;
import me.pikamug.quests.util.AnsiUtil;
import me.pikamug.quests.util.FabricLang;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class FabricQuestsSubCommand {

    public static final Logger SERVER_LOGGER = LoggerFactory.getLogger("net.minecraft.server.MinecraftServer");

    public abstract String getName();

    public abstract String getNameI18N();

    public abstract String getDescription();

    public abstract String getPermission();

    public abstract String getSyntax();

    public abstract int getMaxArguments();

    public abstract int execute(final CommandSourceStack source, final String[] args);

    public List<String> tabComplete(final CommandSourceStack source, final String[] args) {
        return Collections.emptyList();
    }

    public static String lang(final CommandSourceStack source, final String key) {
        return FabricLang.get(source.isPlayer() ? source.getPlayer() : null, key);
    }

    public static String lang(final ServerPlayer player, final String key) {
        return FabricLang.get(player, key);
    }

    /**
     * Sends command feedback. Players receive it through the normal success path; console feedback is
     * logged directly (with legacy color codes converted to ANSI) so that only Quests output is colorized.
     */
    public static void reply(final CommandSourceStack source, final Supplier<Component> message,
                             final boolean broadcast) {
        if (source.isPlayer()) {
            source.sendSuccess(() -> message.get(), broadcast);
        } else {
            SERVER_LOGGER.info("{}", AnsiUtil.toAnsi(message.get().getString()));
        }
    }

    public static boolean assertNonPlayer(final CommandSourceStack source) {
        if (!source.isPlayer()) {
            SERVER_LOGGER.info("{}", AnsiUtil.toAnsi(ChatFormatting.YELLOW + FabricLang.get("consoleError")));
            return true;
        }
        return false;
    }

    /**
     * Get an online Player by name
     *
     * @param name Name of the player
     * @return Player or null if not found
     */
    public static ServerPlayer getOnlinePlayer(final String name) {
        if (name == null) {
            return null;
        }
        final List<ServerPlayer> players = FabricQuestsPlugin.getInstance().getServer().getPlayerList().getPlayers()
                .stream().collect(java.util.stream.Collectors.toList());
        for (final ServerPlayer p : players) {
            if (p.getGameProfile().name().equalsIgnoreCase(name)) {
                return p;
            }
        }
        for (final ServerPlayer p : players) {
            if (p.getGameProfile().name().toLowerCase().startsWith(name.toLowerCase())) {
                return p;
            }
        }
        for (final ServerPlayer p : players) {
            if (p.getGameProfile().name().toLowerCase().contains(name.toLowerCase())) {
                return p;
            }
        }
        return null;
    }

    /**
     * Used to get quest names that contain spaces from command input
     *
     * @param args an array of Strings
     * @param startingIndex the index to start combining at
     * @param endingIndex the index to stop combining at
     * @param delimiter the character for which the array was split
     * @return a String or null
     */
    public static String concatArgArray(final String[] args, final int startingIndex, final int endingIndex,
                                        final char delimiter) {
        StringBuilder s = new StringBuilder();
        for (int i = startingIndex; i <= endingIndex; i++) {
            s.append(args[i]).append(delimiter);
        }
        s = new StringBuilder(s.substring(0, s.length()));
        return s.toString().trim().isEmpty() ? null : s.toString().trim();
    }

    public static Map<String, Integer> sort(final Map<String, Integer> unsortedMap) {
        final List<Map.Entry<String, Integer>> list = new LinkedList<>(unsortedMap.entrySet());
        list.sort((o1, o2) -> {
            final int i = o1.getValue();
            final int i2 = o2.getValue();
            return Integer.compare(i2, i);
        });
        final Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (final Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static void displayStats(final CommandSourceStack source, final FabricQuester quester) {
        final ServerPlayer player = source.isPlayer() ? source.getPlayer() : null;
        reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "questPoints") + " - "
                + ChatFormatting.DARK_PURPLE + quester.getQuestPoints()), false);
        if (quester.getCurrentQuests().isEmpty()) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "currentQuest")
                    + " " + ChatFormatting.DARK_PURPLE + lang(player, "none")), false);
        } else {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "currentQuest")), false);
            for (final Map.Entry<Quest, Integer> entry : quester.getCurrentQuests().entrySet()) {
                final String questName = entry.getKey().getName();
                final int stageNumber = entry.getValue() + 1;
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + "- " + questName
                        + ChatFormatting.LIGHT_PURPLE + " (" + lang(player, "stageEditorStage") + " " + stageNumber
                        + ")"), false);
            }
        }
        if (quester.getCompletedQuests().isEmpty()) {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "completedQuest")
                    + " " + ChatFormatting.DARK_PURPLE + lang(player, "none")), false);
        } else {
            reply(source,() -> Component.literal(ChatFormatting.YELLOW + lang(player, "completedQuest")), false);
            for (final Quest q : quester.getCompletedQuests()) {
                reply(source,() -> Component.literal(ChatFormatting.YELLOW + "- " + q.getName()), false);
            }
        }
    }

    public static Quest findQuest(final FabricQuestsPlugin plugin, final String name) {
        if (name == null) return null;
        final Quest exact = plugin.getQuest(name.toLowerCase());
        if (exact != null) return exact;
        final String lower = name.toLowerCase();
        for (final Quest quest : plugin.getLoadedQuests()) {
            if (quest.getId() != null && quest.getId().equalsIgnoreCase(name)) return quest;
            if (quest.getName() != null && quest.getName().toLowerCase().contains(lower)) return quest;
        }
        return null;
    }

    public static UUID resolveTargetUuid(final FabricQuestsPlugin plugin, final CommandSourceStack source,
                                         final String arg) {
        if (arg == null) return null;
        final ServerPlayer online = source.getServer().getPlayerList().getPlayerByName(arg);
        if (online != null) return online.getUUID();
        UUID uuid = null;
        try {
            uuid = UUID.fromString(arg);
        } catch (final IllegalArgumentException e) {
            // Do nothing
        }
        if (uuid != null) {
            for (final Quester q : plugin.getOfflineQuesters()) {
                if (q.getUUID().equals(uuid)) return uuid;
            }
            return null;
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            final String name = q.getLastKnownName();
            if (name != null && name.equalsIgnoreCase(arg)) return q.getUUID();
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            final String name = q.getLastKnownName();
            if (name != null && name.toLowerCase().startsWith(arg.toLowerCase())) return q.getUUID();
        }
        for (final Quester q : plugin.getOfflineQuesters()) {
            final String name = q.getLastKnownName();
            if (name != null && name.toLowerCase().contains(arg.toLowerCase())) return q.getUUID();
        }
        return null;
    }

    public static FabricQuester resolveTarget(final FabricQuestsPlugin plugin, final CommandSourceStack source,
                                              final String arg) {
        final UUID uuid = resolveTargetUuid(plugin, source, arg);
        if (uuid == null) return null;
        return plugin.getQuester(uuid);
    }
}