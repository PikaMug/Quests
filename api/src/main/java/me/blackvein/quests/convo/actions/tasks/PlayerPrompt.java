/*
 * Copyright (c) 2014 PikaMug and contributors. All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package me.blackvein.quests.convo.actions.tasks;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.ActionsEditorNumericPrompt;
import me.blackvein.quests.convo.actions.ActionsEditorStringPrompt;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.actions.ActionsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerPrompt extends ActionsEditorNumericPrompt {
    
    private final Quests plugin;
    
    public PlayerPrompt(final ConversationContext context) {
        super(context);
        this.plugin = (Quests)context.getPlugin();
    }
    
    private final int size = 10;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("eventEditorPlayer");
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
    public String getSelectionText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("eventEditorSetMessage");
        case 2:
            return ChatColor.YELLOW + Lang.get("eventEditorClearInv");
        case 3:
            return ChatColor.YELLOW + Lang.get("eventEditorSetItems");
        case 4:
            return ChatColor.YELLOW + Lang.get("eventEditorSetPotionEffects");
        case 5:
            return ChatColor.YELLOW + Lang.get("eventEditorSetHunger");
        case 6:
            return ChatColor.YELLOW + Lang.get("eventEditorSetSaturation");
        case 7:
            return ChatColor.YELLOW + Lang.get("eventEditorSetHealth");
        case 8:
            return ChatColor.YELLOW + Lang.get("eventEditorSetTeleport");
        case 9:
            return ChatColor.YELLOW + Lang.get("eventEditorSetCommands");
        case 10:
            return ChatColor.GREEN + Lang.get("done");
        default:
            return null;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public String getAdditionalText(final ConversationContext context, final int number) {
        switch (number) {
        case 1:
            if (context.getSessionData(CK.E_MESSAGE) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_MESSAGE) + ChatColor.GRAY + ")";
            }
        case 2:
            return ChatColor.AQUA + "" + context.getSessionData(CK.E_CLEAR_INVENTORY);
        case 3:
            if (context.getSessionData(CK.E_ITEMS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.E_ITEMS);
                if (items != null) {
                    for (final ItemStack is : items) {
                        if (is != null) {
                            text.append(ChatColor.GRAY).append("     - ").append(ItemUtil.getString(is)).append("\n");
                        }
                    }
                    return text.toString();
                }
            }
        case 4:
            if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                final LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES);
                final LinkedList<Long> durations = (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS);
                final LinkedList<Integer> mags = (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGTH);
                int index = -1;
                if (types != null && durations != null && mags != null) {
                    for (final String type : types) {
                        index++;
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(type)
                                .append(ChatColor.DARK_PURPLE).append(" ").append(RomanNumeral.getNumeral(mags
                                .get(index))).append(ChatColor.GRAY).append(" -> ").append(ChatColor.DARK_AQUA)
                                .append(MiscUtil.getTime(durations.get(index) * 50L)).append("\n");
                    }
                }
                return text.toString();
            }
        case 5:
            if (context.getSessionData(CK.E_HUNGER) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_HUNGER) + ChatColor.GRAY 
                        + ")";
            }
        case 6:
            if (context.getSessionData(CK.E_SATURATION) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_SATURATION) + ChatColor.GRAY 
                        + ")";
            }
        case 7:
            if (context.getSessionData(CK.E_HEALTH) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_HEALTH) + ChatColor.GRAY 
                        + ")";
            }
        case 8:
            if (context.getSessionData(CK.E_TELEPORT) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                return ChatColor.GRAY + "(" + ChatColor.AQUA + context.getSessionData(CK.E_TELEPORT) + ChatColor.GRAY 
                        + ")";
            }
        case 9:
            if (context.getSessionData(CK.E_COMMANDS) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                final StringBuilder text = new StringBuilder("\n");
                for (final String s : (LinkedList<String>) Objects.requireNonNull(context
                        .getSessionData(CK.E_COMMANDS))) {
                    text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s).append("\n");
                }
                return text.toString();
            }
        case 10:
            return "";
        default:
            return null;
        }
    }

    @Override
    public @NotNull String getBasicPromptText(final ConversationContext context) {
        if (context.getSessionData(CK.E_CLEAR_INVENTORY) == null) {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
        }
        
        final ActionsEditorPostOpenNumericPromptEvent event
                = new ActionsEditorPostOpenNumericPromptEvent(context, this);
        plugin.getServer().getPluginManager().callEvent(event);

        final StringBuilder text = new StringBuilder(ChatColor.GOLD + "- " + getTitle(context) + " -");
        for (int i = 1; i <= size; i++) {
            text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                    .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                    .append(getAdditionalText(context, i));
        }
        return text.toString();
    }

    @Override
    protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
        switch (input.intValue()) {
        case 1:
            return new PlayerMessagePrompt(context);
        case 2:
            final String s = (String) context.getSessionData(CK.E_CLEAR_INVENTORY);
            if (s != null) {
                if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                    context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
                } else {
                    context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
                }
            }
            return new ActionMainPrompt(context);
        case 3:
            return new PlayerItemListPrompt(context);
        case 4:
            return new PlayerPotionEffectPrompt(context);
        case 5:
            return new PlayerHungerPrompt(context);
        case 6:
            return new PlayerSaturationPrompt(context);
        case 7:
            return new PlayerHealthPrompt(context);
        case 8:
            if (context.getForWhom() instanceof Player) {
                final Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
                selectedTeleportLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                return new PlayerTeleportPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("consoleError"));
                return new PlayerPrompt(context);
            }
        case 9:
            if (!plugin.hasLimitedAccess(context.getForWhom())) {
                return new PlayerCommandsPrompt(context);
            } else {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("noPermission"));
                return new PlayerPrompt(context);
            }
        case 10:
            return new ActionMainPrompt(context);
        default:
            return new PlayerPrompt(context);
        }
    }
    
    public class PlayerMessagePrompt extends ActionsEditorStringPrompt {

        public PlayerMessagePrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetMessagePrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_MESSAGE, input);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_MESSAGE, null);
            }
            return new ActionMainPrompt(context);
        }
    }

    public class PlayerItemListPrompt extends ActionsEditorNumericPrompt {

        public PlayerItemListPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 3;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorGiveItemsTitle");
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
                return ChatColor.YELLOW + Lang.get("stageEditorDeliveryAddItem");
            case 2:
                return ChatColor.RED + Lang.get("clear");
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
                if (context.getSessionData(CK.E_ITEMS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    for (final ItemStack is : (List<ItemStack>) Objects.requireNonNull(context
                            .getSessionData(CK.E_ITEMS))) {
                        text.append(ChatColor.GRAY).append("     - ").append(ItemUtil.getDisplayString(is))
                                .append("\n");
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

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull String getBasicPromptText(final ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("tempStack") != null) {
                if (context.getSessionData(CK.E_ITEMS) != null) {
                    final List<ItemStack> items = (List<ItemStack>) context.getSessionData(CK.E_ITEMS);
                    if (items != null) {
                        items.add((ItemStack) context.getSessionData("tempStack"));
                        context.setSessionData(CK.E_ITEMS, items);
                    }
                } else {
                    final LinkedList<ItemStack> itemRewards = new LinkedList<>();
                    itemRewards.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.E_ITEMS, itemRewards);
                }
                ItemStackPrompt.clearSessionData(context);
            }
            
            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch(input.intValue()) {
            case 1:
                return new ItemStackPrompt(context, PlayerItemListPrompt.this);
            case 2:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorItemsCleared"));
                context.setSessionData(CK.E_ITEMS, null);
                return new PlayerItemListPrompt(context);
            case 3:
                return new ActionMainPrompt(context);
            default:
                return new PlayerItemListPrompt(context);
            }
        }
    }
    
    public class PlayerPotionEffectPrompt extends ActionsEditorNumericPrompt {

        public PlayerPotionEffectPrompt(final ConversationContext context) {
            super(context);
        }
        
        private final int size = 5;
        
        @Override
        public int getSize() {
            return size;
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorPotionEffectsTitle");
        }
        
        @Override
        public ChatColor getNumberColor(final ConversationContext context, final int number) {
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
        public String getSelectionText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                return ChatColor.YELLOW + Lang.get("eventEditorSetPotionEffectTypes");
            case 2:
                return ChatColor.YELLOW + Lang.get("eventEditorSetPotionDurations");
            case 3:
                return ChatColor.YELLOW + Lang.get("eventEditorSetPotionMagnitudes");
            case 4:
                return ChatColor.RED + Lang.get("clear");
            case 5:
                return ChatColor.GREEN + Lang.get("done");
            default:
                return null;
            }
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public String getAdditionalText(final ConversationContext context, final int number) {
            switch (number) {
            case 1:
                if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    for (final String s : (LinkedList<String>) Objects.requireNonNull(context
                            .getSessionData(CK.E_POTION_TYPES))) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.AQUA).append(s).append("\n");
                    }
                    return text.toString();
                }
            case 2:
                if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    for (final Long l : (LinkedList<Long>) Objects.requireNonNull(context
                            .getSessionData(CK.E_POTION_DURATIONS))) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_AQUA)
                                .append(MiscUtil.getTime(l * 50L)).append("\n");
                    }
                    return text.toString();
                }
            case 3:
                if (context.getSessionData(CK.E_POTION_STRENGTH) == null) {
                    return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
                } else {
                    final StringBuilder text = new StringBuilder("\n");
                    for (final int i : (LinkedList<Integer>) Objects.requireNonNull(context
                            .getSessionData(CK.E_POTION_STRENGTH))) {
                        text.append(ChatColor.GRAY).append("     - ").append(ChatColor.DARK_PURPLE).append(i)
                                .append("\n");
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
        public @NotNull String getBasicPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenNumericPromptEvent event
                    = new ActionsEditorPostOpenNumericPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder text = new StringBuilder(ChatColor.GOLD + getTitle(context));
            for (int i = 1; i <= size; i++) {
                text.append("\n").append(getNumberColor(context, i)).append(ChatColor.BOLD).append(i)
                        .append(ChatColor.RESET).append(" - ").append(getSelectionText(context, i)).append(" ")
                        .append(getAdditionalText(context, i));
            }
            return text.toString();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(final @NotNull ConversationContext context, final Number input) {
            switch (input.intValue()) {
            case 1:
                return new PlayerPotionTypesPrompt(context);
            case 2:
                if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetPotionTypesFirst"));
                    return new PlayerPotionEffectPrompt(context);
                } else {
                    return new PlayerPotionDurationsPrompt(context);
                }
            case 3:
                if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("eventEditorMustSetPotionTypesAndDurationsFirst"));
                    return new PlayerPotionEffectPrompt(context);
                } else if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("eventEditorMustSetPotionDurationsFirst"));
                    return new PlayerPotionEffectPrompt(context);
                } else {
                    return new PlayerPotionMagnitudesPrompt(context);
                }
            case 4:
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorPotionsCleared"));
                context.setSessionData(CK.E_POTION_TYPES, null);
                context.setSessionData(CK.E_POTION_DURATIONS, null);
                context.setSessionData(CK.E_POTION_STRENGTH, null);
                return new PlayerPotionEffectPrompt(context);
            case 5:
                final int one;
                final int two;
                final int three;
                final List<String> types = (List<String>) context.getSessionData(CK.E_POTION_TYPES);
                final List<Long> durations = (List<Long>) context.getSessionData(CK.E_POTION_DURATIONS);
                final List<Integer> strength = (List<Integer>) context.getSessionData(CK.E_POTION_STRENGTH);
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
                    return new ActionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorListSizeMismatch"));
                    return new PlayerPotionEffectPrompt(context);
                }
            default:
                return new PlayerPotionEffectPrompt(context);
            }
        }
    }

    public class PlayerPotionTypesPrompt extends ActionsEditorStringPrompt {

        public PlayerPotionTypesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("eventEditorPotionTypesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetPotionEffectsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            final StringBuilder effs = new StringBuilder(ChatColor.LIGHT_PURPLE + getTitle(context) + "\n");
            for (final PotionEffectType pet : PotionEffectType.values()) {
                effs.append(pet != null ? ChatColor.DARK_PURPLE + pet.getName() + "\n" : "");
            }
            return effs.toString() + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<String> effTypes = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    if (s != null && PotionEffectType.getByName(s.toUpperCase()) != null) {
                        effTypes.add(Objects.requireNonNull(PotionEffectType.getByName(s.toUpperCase())).getName());
                        context.setSessionData(CK.E_POTION_TYPES, effTypes);
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                               + Lang.get("eventEditorInvalidPotionType"));
                        return new PlayerPotionTypesPrompt(context);
                    }
                }
            }
            return new PlayerPotionEffectPrompt(context);
        }
    }

    public class PlayerPotionDurationsPrompt extends ActionsEditorStringPrompt {

        public PlayerPotionDurationsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetPotionDurationsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<Long> effDurations = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        final long l = i * 1000L;
                        if (l < 1000) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new PlayerPotionDurationsPrompt(context);
                        }
                        effDurations.add(l / 50L);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                                .replace("<input>", s));
                        return new PlayerPotionDurationsPrompt(context);
                    }
                }
                context.setSessionData(CK.E_POTION_DURATIONS, effDurations);
            }
            return new PlayerPotionEffectPrompt(context);
        }
    }

    public class PlayerPotionMagnitudesPrompt extends ActionsEditorStringPrompt {

        public PlayerPotionMagnitudesPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetPotionMagnitudesPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final LinkedList<Integer> magAmounts = new LinkedList<>();
                for (final String s : input.split(" ")) {
                    try {
                        final int i = Integer.parseInt(s);
                        if (i < 1) {
                            context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("invalidMinimum")
                                    .replace("<number>", "1"));
                            return new PlayerPotionMagnitudesPrompt(context);
                        }
                        magAmounts.add(i);
                    } catch (final NumberFormatException e) {
                        context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("reqNotANumber")
                                .replace("<input>", s));
                        return new PlayerPotionMagnitudesPrompt(context);
                    }
                }
                context.setSessionData(CK.E_POTION_STRENGTH, magAmounts);
            }
            return new PlayerPotionEffectPrompt(context);
        }
    }
    
    public class PlayerHungerPrompt extends ActionsEditorStringPrompt {

        public PlayerHungerPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetHungerPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED
                                + Lang.get("invalidMinimum").replace("<number>", "0"));
                        return new PlayerHungerPrompt(context);
                    } else {
                        context.setSessionData(CK.E_HUNGER, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new PlayerHungerPrompt(context);
                }
            } else {
                context.setSessionData(CK.E_HUNGER, null);
            }
            return new ActionMainPrompt(context);
        }
    }

    public class PlayerSaturationPrompt extends ActionsEditorStringPrompt {

        public PlayerSaturationPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetSaturationPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED
                                + Lang.get("invalidMinimum").replace("<number>", "0"));
                        return new PlayerSaturationPrompt(context);
                    } else {
                        context.setSessionData(CK.E_SATURATION, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new PlayerSaturationPrompt(context);
                }
            } else {
                context.setSessionData(CK.E_SATURATION, null);
            }
            return new ActionMainPrompt(context);
        }
    }

    public class PlayerHealthPrompt extends ActionsEditorStringPrompt {

        public PlayerHealthPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetHealthPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                try {
                    final int i = Integer.parseInt(input);
                    if (i < 0) {
                        context.getForWhom().sendRawMessage(ChatColor.RED
                                + Lang.get("invalidMinimum").replace("<number>", "0"));
                        return new PlayerHealthPrompt(context);
                    } else {
                        context.setSessionData(CK.E_HEALTH, i);
                    }
                } catch (final NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new PlayerHealthPrompt(context);
                }
            } else {
                context.setSessionData(CK.E_HEALTH, null);
            }
            return new ActionMainPrompt(context);
        }
    }
    
    public class PlayerTeleportPrompt extends ActionsEditorStringPrompt {

        public PlayerTeleportPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetTeleportPrompt");
        }
        
        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdDone"))) {
                final Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
                final Block block = selectedTeleportLocations.get(player.getUniqueId());
                if (block != null) {
                    final Location loc = block.getLocation();
                    context.setSessionData(CK.E_TELEPORT, ConfigUtil.getLocationInfo(loc));
                    selectedTeleportLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new PlayerTeleportPrompt(context);
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_TELEPORT, null);
                final Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
                selectedTeleportLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                final Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
                selectedTeleportLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                return new ActionMainPrompt(context);
            } else {
                return new PlayerTeleportPrompt(context);
            }
        }
    }

    public class PlayerCommandsPrompt extends ActionsEditorStringPrompt {

        public PlayerCommandsPrompt(final ConversationContext context) {
            super(context);
        }
        
        @Override
        public String getTitle(final ConversationContext context) {
            return null;
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("eventEditorSetCommandsPrompt");
        }

        @Override
        public @NotNull String getPromptText(final @NotNull ConversationContext context) {
            final ActionsEditorPostOpenStringPromptEvent event
                    = new ActionsEditorPostOpenStringPromptEvent(context, this);
            plugin.getServer().getPluginManager().callEvent(event);
            
            return ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final @NotNull ConversationContext context, final String input) {
            if (input == null) {
                return null;
            }
            if (!input.equalsIgnoreCase(Lang.get("cmdCancel")) && !input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                final String[] commands = input.split(Lang.get("charSemi"));
                final LinkedList<String> cmdList = new LinkedList<>(Arrays.asList(commands));
                context.setSessionData(CK.E_COMMANDS, cmdList);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_COMMANDS, null);
            }
            return new ActionMainPrompt(context);
        }
    }
}
