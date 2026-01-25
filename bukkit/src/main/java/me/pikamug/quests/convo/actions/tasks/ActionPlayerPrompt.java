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

import me.pikamug.quests.BukkitQuestsPlugin;
import me.pikamug.quests.convo.actions.ActionsEditorIntegerPrompt;
import me.pikamug.quests.convo.actions.ActionsEditorStringPrompt;
import me.pikamug.quests.convo.actions.main.ActionMainPrompt;
import me.pikamug.quests.convo.generic.ItemStackPrompt;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenNumericPromptEvent;
import me.pikamug.quests.events.editor.actions.BukkitActionsEditorPostOpenStringPromptEvent;
import me.pikamug.quests.util.BukkitConfigUtil;
import me.pikamug.quests.util.BukkitItemUtil;
import me.pikamug.quests.util.BukkitLang;
import me.pikamug.quests.util.BukkitMiscUtil;
import me.pikamug.quests.util.Key;
import me.pikamug.quests.util.RomanNumeral;
import me.pikamug.quests.util.SessionData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionPlayerPrompt extends ActionsEditorIntegerPrompt {

    private final @NotNull UUID uuid;
    private final BukkitQuestsPlugin plugin;
    
    public ActionPlayerPrompt(final @NotNull UUID uuid) {
        super(uuid);
        this.uuid = uuid;
        this.plugin = BukkitQuestsPlugin.getInstance();
    }
    
    private final int size = 10;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle() {
        return BukkitLang.get("eventEditorPlayer");
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
        case 7:
        case 8:
        case 9:
            return ChatColor.BLUE;
        case 10:
            return ChatColor.GREEN;
        default:
            return null;
        }
    }
    
    @Override
    public String getSelectionText(final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetMessage");
        case 2:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetItems");
        case 3:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetPotionEffects");
        case 4:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetHunger");
        case 5:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetSaturation");
        case 6:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetHealth");
        case 7:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetTeleport");
        case 8:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorSetCommands");
        case 9:
            return ChatColor.YELLOW + BukkitLang.get("eventEditorClearInv");
        case 10:
            return ChatColor.GREEN + BukkitLang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final int number) {
        switch (number) {
        case 1:
            if (SessionData.get(uuid, Key.A_MESSAGE) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_MESSAGE) + ChatColor.GRAY + ")";
            }
        case 2:
            if (SessionData.get(uuid, Key.A_ITEMS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) SessionData.get(uuid, Key.A_ITEMS);
                if (items != null) {
                    for (final ItemStack is : items) {
                        if (is != null) {
                            text.append("\n").append(ChatColor.GRAY).append("     - ").append(BukkitItemUtil.getString(is));
                        }
                    }
                    return text.toString();
                }
            }
        case 3:
            if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                final LinkedList<String> types = (LinkedList<String>) SessionData.get(uuid, Key.A_POTION_TYPES);
                final LinkedList<Long> durations = (LinkedList<Long>) SessionData.get(uuid, Key.A_POTION_DURATIONS);
                final LinkedList<Integer> mags = (LinkedList<Integer>) SessionData.get(uuid, Key.A_POTION_STRENGTH);
                int index = -1;
                if (types != null && durations != null && mags != null) {
                    for (final String type : types) {
                        index++;
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(type)
                                .append(ChatColor.DARK_PURPLE).append(" ").append(RomanNumeral.getNumeral(mags
                                .get(index))).append(ChatColor.GRAY).append(" -> ").append(ChatColor.DARK_AQUA)
                                .append(BukkitMiscUtil.getTime(durations.get(index) * 50L));
                    }
                }
                return text.toString();
            }
        case 4:
            if (SessionData.get(uuid, Key.A_HUNGER) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_HUNGER) + ChatColor.GRAY
                        + ")";
            }
        case 5:
            if (SessionData.get(uuid, Key.A_SATURATION) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_SATURATION) + ChatColor.GRAY
                        + ")";
            }
        case 6:
            if (SessionData.get(uuid, Key.A_HEALTH) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_HEALTH) + ChatColor.GRAY
                        + ")";
            }
        case 7:
            if (SessionData.get(uuid, Key.A_TELEPORT) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + SessionData.get(uuid, Key.A_TELEPORT) + ChatColor.GRAY
                        + ")";
            }
        case 8:
            if (SessionData.get(uuid, Key.A_COMMANDS) == null) {
                return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder();
                for (final String s : (LinkedList<String>) Objects.requireNonNull(SessionData
                        .get(uuid, Key.A_COMMANDS))) {
                    text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                }
                return text.toString();
            }
        case 9:
            if (SessionData.get(uuid, Key.A_CLEAR_INVENTORY) == null) {
                return ChatColor.GRAY + "(" + ChatColor.RED + BukkitLang.get("false") + ChatColor.GRAY + ")";
            } else {
                final Boolean clearOpt = (Boolean) SessionData.get(uuid, Key.A_CLEAR_INVENTORY);
                return ChatColor.GRAY + "(" + (Boolean.TRUE.equals(clearOpt) ? ChatColor.GREEN + BukkitLang.get("true")
                        : ChatColor.RED + BukkitLang.get("false")) + ChatColor.GRAY + ")";
            }
        case 10:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getPromptText() {
        if (SessionData.get(uuid, Key.A_CLEAR_INVENTORY) == null) {
            SessionData.set(uuid, Key.A_CLEAR_INVENTORY, false);
        }
        
        final BukkitActionsEditorPostOpenNumericPromptEvent event
                = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle() + " -");
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
            new ActionPlayerMessagePrompt(uuid).start();
        case 2:
            new ActionPlayerItemListPrompt(uuid).start();
        case 3:
            new ActionPlayerPotionListPrompt(uuid).start();
        case 4:
            new ActionPlayerHungerPrompt(uuid).start();
        case 5:
            new ActionPlayerSaturationPrompt(uuid).start();
        case 6:
            new ActionPlayerHealthPrompt(uuid).start();
        case 7:
            if (sender instanceof Player) {
                final ConcurrentHashMap<UUID, Block> selectedTeleportLocations
                        = plugin.getActionFactory().getSelectedTeleportLocations();
                if (BukkitMiscUtil.getWorlds().isEmpty()) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("unknownError"));
                    new ActionPlayerPrompt(uuid).start();
                }
                selectedTeleportLocations.put(((Player) sender).getUniqueId(),
                        Bukkit.getWorlds().get(0).getBlockAt(0,0,0));
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                new ActionPlayerTeleportPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("consoleError"));
                new ActionPlayerPrompt(uuid).start();
            }
        case 8:
            if (!plugin.hasLimitedAccess(uuid)) {
                new ActionPlayerCommandsPrompt(uuid).start();
            } else {
                sender.sendMessage(ChatColor.RED + BukkitLang.get("noPermission"));
                new ActionPlayerPrompt(uuid).start();
            }
        case 9:
            final Boolean b = (Boolean) SessionData.get(uuid, Key.A_CLEAR_INVENTORY);
            if (Boolean.TRUE.equals(b)) {
                SessionData.set(uuid, Key.A_CLEAR_INVENTORY, false);
            } else {
                SessionData.set(uuid, Key.A_CLEAR_INVENTORY, true);
            }
            new ActionPlayerPrompt(uuid).start();
        case 10:
            new ActionMainPrompt(uuid).start();
        default:
            new ActionPlayerPrompt(uuid).start();
        }
    }
    
    public class ActionPlayerMessagePrompt extends ActionsEditorStringPrompt {

        public ActionPlayerMessagePrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_MESSAGE, input);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_MESSAGE, null);
            }
            new ActionMainPrompt(uuid).start();
        }
    }

    public class ActionPlayerItemListPrompt extends ActionsEditorIntegerPrompt {

        public ActionPlayerItemListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorGiveItemsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
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
        public String getSelectionText(final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + BukkitLang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + BukkitLang.get("clear");
            case 3:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch(number) {
            case 1:
                if (SessionData.get(uuid, Key.A_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final ItemStack is : (List<ItemStack>) Objects.requireNonNull(SessionData
                            .get(uuid, Key.A_ITEMS))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ")
                                .append(BukkitItemUtil.getDisplayString(is));
                    }
                    return text.toString();
                }
            case 2:
            case 3:
                return "";
            default:
                return null;
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull String getPromptText() {
            // Check/add newly made item
            if (SessionData.get(uuid, "tempStack") != null) {
                if (SessionData.get(uuid, Key.A_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) SessionData.get(uuid, Key.A_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) SessionData.get(uuid, "tempStack"));
                        SessionData.set(uuid, Key.A_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> itemRewards = new LinkedList<>();
                    itemRewards.add((ItemStack) SessionData.get(uuid, "tempStack"));
                    SessionData.set(uuid, Key.A_ITEMS, itemRewards);
                }
                ItemStackPrompt.clearSessionData(uuid);
            }
            
            final BukkitActionsEditorPostOpenNumericPromptEvent event
                    = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
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
            switch(input.intValue()) {
            case 1:
                new ItemStackPrompt(uuid, ActionPlayerItemListPrompt.this);
            case 2:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorItemsCleared"));
                SessionData.set(uuid, Key.A_ITEMS, null);
                new ActionPlayerItemListPrompt(uuid).start();
            case 3:
                new ActionMainPrompt(uuid).start();
            default:
                new ActionPlayerItemListPrompt(uuid).start();
            }
        }
    }
    
    public class ActionPlayerPotionListPrompt extends ActionsEditorIntegerPrompt {

        public ActionPlayerPotionListPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorPotionEffectsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final int number) {
            switch (number) {
            case 1:
            case 2:
            case 3:
                return ChatColor.BLUE;
            case 4:
                return ChatColor.RED;
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
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetPotionEffectTypes");
            case 2:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetPotionDurations");
            case 3:
                return ChatColor.YELLOW + BukkitLang.get("eventEditorSetPotionMagnitudes");
            case 4:
                return ChatColor.RED + BukkitLang.get("clear");
            case 5:
                return ChatColor.GREEN + BukkitLang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final int number) {
            switch (number) {
            case 1:
                if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final String s : (LinkedList<String>) Objects.requireNonNull(SessionData
                            .get(uuid, Key.A_POTION_TYPES))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s);
                    }
                    return text.toString();
                }
            case 2:
                if (SessionData.get(uuid, Key.A_POTION_DURATIONS) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final Long l : (LinkedList<Long>) Objects.requireNonNull(SessionData.get(uuid, 
                            Key.A_POTION_DURATIONS))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_AQUA)
                                .append(BukkitMiscUtil.getTime(l * 50L));
                    }
                    return text.toString();
                }
            case 3:
                if (SessionData.get(uuid, Key.A_POTION_STRENGTH) == null) {
                    return ChatColor.GRAY + "(" + BukkitLang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder();
                    for (final int i : (LinkedList<Integer>) Objects.requireNonNull(SessionData.get(uuid, 
                            Key.A_POTION_STRENGTH))) {
                        text.append("\n").append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_PURPLE)
                                .append(i);
                    }
                    return text.toString();
                }
            case 4:
            case 5:
                return "";
            default:
                return null;
            }
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenNumericPromptEvent event
                    = new BukkitActionsEditorPostOpenNumericPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle());
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(i)).append(" ")
                        .append(getAdditionalText(i));
            }
            return text.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void acceptInput(final Number input) {
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            switch (input.intValue()) {
            case 1:
                new ActionPlayerPotionTypesPrompt(uuid).start();
            case 2:
                if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorMustSetPotionTypesFirst"));
                    new ActionPlayerPotionListPrompt(uuid).start();
                } else {
                    new ActionPlayerPotionDurationsPrompt(uuid).start();
                }
            case 3:
                if (SessionData.get(uuid, Key.A_POTION_TYPES) == null) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("eventEditorMustSetPotionTypesAndDurationsFirst"));
                    new ActionPlayerPotionListPrompt(uuid).start();
                } else if (SessionData.get(uuid, Key.A_POTION_DURATIONS) == null) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("eventEditorMustSetPotionDurationsFirst"));
                    new ActionPlayerPotionListPrompt(uuid).start();
                } else {
                    new ActionPlayerPotionMagnitudesPrompt(uuid).start();
                }
            case 4:
                sender.sendMessage(ChatColor.YELLOW + BukkitLang.get("eventEditorPotionsCleared"));
                SessionData.set(uuid, Key.A_POTION_TYPES, null);
                SessionData.set(uuid, Key.A_POTION_DURATIONS, null);
                SessionData.set(uuid, Key.A_POTION_STRENGTH, null);
                new ActionPlayerPotionListPrompt(uuid).start();
            case 5:
                final int one;
                final int two;
                final int three;
                final List<String> types = (List<String>) SessionData.get(uuid, Key.A_POTION_TYPES);
                final List<Long> durations = (List<Long>) SessionData.get(uuid, Key.A_POTION_DURATIONS);
                final List<Integer> strength = (List<Integer>) SessionData.get(uuid, Key.A_POTION_STRENGTH);
                if (types != null) {
                    one = types.size();
                } else {
                    one = 0;
                }
                if (durations != null) {
                    two = durations.size();
                } else {
                    two = 0;
                }
                if (strength != null) {
                    three = strength.size();
                } else {
                    three = 0;
                }
                if (one == two && two == three) {
                    new ActionMainPrompt(uuid).start();
                } else {
                    sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorListSizeMismatch"));
                    new ActionPlayerPotionListPrompt(uuid).start();
                }
            default:
                new ActionPlayerPotionListPrompt(uuid).start();
            }
        }
    }

    public class ActionPlayerPotionTypesPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerPotionTypesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return BukkitLang.get("eventEditorPotionTypesTitle");
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetPotionEffectsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);

            final StringBuilder potions = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle() + "\n");
            final List<PotionEffectType> effArr = new LinkedList<>(Arrays.asList(PotionEffectType.values()));
            effArr.sort(Comparator.comparing(PotionEffectType::getName));
            for (int i = 0; i < effArr.size(); i++) {
                final String name = effArr.get(i).getName().replaceFirst("minecraft:", "").toUpperCase();
                potions.append(ChatColor.AQUA).append(name);
                if (i < (effArr.size() - 1)) {
                    potions.append(ChatColor.GRAY).append(", ");
                }
            }
            potions.append("\n").append(ChatColor.YELLOW).append(getQueryText());
            return potions.toString();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<String> effTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (PotionEffectType.getByName(s.toUpperCase()) != null) {
                        effTypes.add(Objects.requireNonNull(PotionEffectType.getByName(s.toUpperCase())).getName());
                        SessionData.set(uuid, Key.A_POTION_TYPES, effTypes);
                    } else {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorInvalidPotionType")
                                .replace("<input>", s));
                        new ActionPlayerPotionTypesPrompt(uuid).start();
                    }
                }
            }
            new ActionPlayerPotionListPrompt(uuid).start();
        }
    }

    public class ActionPlayerPotionDurationsPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerPotionDurationsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetPotionDurationsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<Long> effDurations = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        final long l = i * 1000L;
                        if (l < 1000) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new ActionPlayerPotionDurationsPrompt(uuid).start();
                        }
                        effDurations.add(l / 50L);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", s));
                        new ActionPlayerPotionDurationsPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.A_POTION_DURATIONS, effDurations);
            }
            new ActionPlayerPotionListPrompt(uuid).start();
        }
    }

    public class ActionPlayerPotionMagnitudesPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerPotionMagnitudesPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetPotionMagnitudesPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final LinkedList<Integer> magAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            sender.sendMessage(ChatColor.RED + BukkitLang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            new ActionPlayerPotionMagnitudesPrompt(uuid).start();
                        }
                        magAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + BukkitLang.get("reqNotANumber")
                                .replace("<input>", s));
                        new ActionPlayerPotionMagnitudesPrompt(uuid).start();
                    }
                }
                SessionData.set(uuid, Key.A_POTION_STRENGTH, magAmounts);
            }
            new ActionPlayerPotionListPrompt(uuid).start();
        }
    }
    
    public class ActionPlayerHungerPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerHungerPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetHungerPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("invalidMinimum").replace("<number>", "0"));
                        new ActionPlayerHungerPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.A_HUNGER, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("reqNotANumber").replace("<input>", input));
                    new ActionPlayerHungerPrompt(uuid).start();
                }
            } else {
                SessionData.set(uuid, Key.A_HUNGER, null);
            }
            new ActionMainPrompt(uuid).start();
        }
    }

    public class ActionPlayerSaturationPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerSaturationPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetSaturationPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("invalidMinimum").replace("<number>", "0"));
                        new ActionPlayerSaturationPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.A_SATURATION, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("reqNotANumber").replace("<input>", input));
                    new ActionPlayerSaturationPrompt(uuid).start();
                }
            } else {
                SessionData.set(uuid, Key.A_SATURATION, null);
            }
            new ActionMainPrompt(uuid).start();
        }
    }

    public class ActionPlayerHealthPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerHealthPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetHealthPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        sender.sendMessage(ChatColor.RED
                                + BukkitLang.get("invalidMinimum").replace("<number>", "0"));
                        new ActionPlayerHealthPrompt(uuid).start();
                    } else {
                        SessionData.set(uuid, Key.A_HEALTH, i);
                    }
                } catch (final NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED
                            + BukkitLang.get("reqNotANumber").replace("<input>", input));
                    new ActionPlayerHealthPrompt(uuid).start();
                }
            } else {
                SessionData.set(uuid, Key.A_HEALTH, null);
            }
            new ActionMainPrompt(uuid).start();
        }
    }
    
    public class ActionPlayerTeleportPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerTeleportPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetTeleportPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            final CommandSender sender = BukkitMiscUtil.getEntity(uuid);
            final Player player = (Player) sender;
            if (input.equalsIgnoreCase(BukkitLang.get("cmdDone"))) {
                final ConcurrentHashMap<UUID, Block> selectedTeleportLocations
                        = plugin.getActionFactory().getSelectedTeleportLocations();
                final Block block = selectedTeleportLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    SessionData.set(uuid, Key.A_TELEPORT, BukkitConfigUtil.getLocationInfo(loc));
                    selectedTeleportLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                } else {
                    player.sendMessage(ChatColor.RED + BukkitLang.get("eventEditorSelectBlockFirst"));
                    new ActionPlayerTeleportPrompt(uuid).start();
                }
                new ActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_TELEPORT, null);
                final ConcurrentHashMap<UUID, Block> selectedTeleportLocations
                        = plugin.getActionFactory().getSelectedTeleportLocations();
                selectedTeleportLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                new ActionMainPrompt(uuid).start();
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdCancel"))) {
                final ConcurrentHashMap<UUID, Block> selectedTeleportLocations
                        = plugin.getActionFactory().getSelectedTeleportLocations();
                selectedTeleportLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                new ActionMainPrompt(uuid).start();
            } else {
                new ActionPlayerTeleportPrompt(uuid).start();
            }
        }
    }

    public class ActionPlayerCommandsPrompt extends ActionsEditorStringPrompt {

        public ActionPlayerCommandsPrompt(final @NotNull UUID uuid) {
            super(uuid);
        }
        
        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public String getQueryText() {
            return BukkitLang.get("eventEditorSetCommandsPrompt");
        }

        @Override
        public @NotNull String getPromptText() {
            final BukkitActionsEditorPostOpenStringPromptEvent event
                    = new BukkitActionsEditorPostOpenStringPromptEvent(uuid, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText();
        }

        @Override
        public void acceptInput(final String input) {
            if (input == null) {
                return;
            }
            if (!input.equalsIgnoreCase(BukkitLang.get("cmdCancel")) && !input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                final String[] commands = input.split(BukkitLang.get("charSemi"));
                final LinkedList<String> cmdList = new LinkedList<>(Arrays.asList(commands));
                SessionData.set(uuid, Key.A_COMMANDS, cmdList);
            } else if (input.equalsIgnoreCase(BukkitLang.get("cmdClear"))) {
                SessionData.set(uuid, Key.A_COMMANDS, null);
            }
            new ActionMainPrompt(uuid).start();
        }
    }
}
