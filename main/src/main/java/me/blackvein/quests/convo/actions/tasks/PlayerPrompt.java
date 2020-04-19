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

package me.blackvein.quests.convo.actions.tasks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import me.blackvein.quests.Quests;
import me.blackvein.quests.convo.actions.main.ActionMainPrompt;
import me.blackvein.quests.convo.generic.ItemStackPrompt;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.ConfigUtil;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;
import me.blackvein.quests.util.RomanNumeral;

public class PlayerPrompt extends FixedSetPrompt {
    
    private final Quests plugin;
    
    public PlayerPrompt(ConversationContext context) {
        super("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
        this.plugin = (Quests)context.getPlugin();
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPromptText(ConversationContext context) {
        String text = ChatColor.GOLD + "- " + Lang.get("eventEditorPlayer") + " -\n";
        if (context.getSessionData(CK.E_MESSAGE) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMessage") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetMessage") + " (" + ChatColor.AQUA 
                    + context.getSessionData(CK.E_MESSAGE) + ChatColor.RESET + ChatColor.YELLOW + ")\n";
        }
        if (context.getSessionData(CK.E_CLEAR_INVENTORY) == null) {
            context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
        }
        text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                + Lang.get("eventEditorClearInv") + ": " + ChatColor.AQUA 
                + context.getSessionData(CK.E_CLEAR_INVENTORY) + "\n";
        if (context.getSessionData(CK.E_ITEMS) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetItems") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetItems") + "\n";
            LinkedList<ItemStack> items = (LinkedList<ItemStack>) context.getSessionData(CK.E_ITEMS);
            for (ItemStack is : items) {
                if (is != null) {
                    text += ChatColor.GRAY + "    - " + ItemUtil.getString(is) + "\n";
                }
            }
        }
        if (context.getSessionData(CK.E_POTION_TYPES) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetPotionEffects") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetPotionEffects") + "\n";
            LinkedList<String> types = (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES);
            LinkedList<Long> durations = (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS);
            LinkedList<Integer> mags = (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGHT);
            int index = -1;
            for (String type : types) {
                index++;
                text += ChatColor.GRAY + "    - " + ChatColor.AQUA + type + ChatColor.DARK_PURPLE + " " 
                        + RomanNumeral.getNumeral(mags.get(index)) + ChatColor.GRAY + " -> " + ChatColor.DARK_AQUA 
                        + MiscUtil.getTime(durations.get(index) * 50L) + "\n";
            }
        }
        if (context.getSessionData(CK.E_HUNGER) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetHunger") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetHunger") + ChatColor.AQUA + " (" 
                    + (Integer) context.getSessionData(CK.E_HUNGER) + ")\n";
        }
        if (context.getSessionData(CK.E_SATURATION) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetSaturation") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "6" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetSaturation") + ChatColor.AQUA + " (" 
                    + (Integer) context.getSessionData(CK.E_SATURATION) + ")\n";
        }
        if (context.getSessionData(CK.E_HEALTH) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetHealth") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "7" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetHealth") + ChatColor.AQUA + " (" 
                    + (Integer) context.getSessionData(CK.E_HEALTH) + ")\n";
        }
        if (context.getSessionData(CK.E_TELEPORT) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "8" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetTeleport") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "8" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetTeleport") + ChatColor.AQUA + " (" 
                    + (String) context.getSessionData(CK.E_TELEPORT) + ")\n";
        }
        if (context.getSessionData(CK.E_COMMANDS) == null) {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "9" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetCommands") + ChatColor.GRAY + " (" + Lang.get("noneSet") + ")\n";
        } else {
            text += ChatColor.BLUE + "" + ChatColor.BOLD + "9" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                    + Lang.get("eventEditorSetCommands") + "\n";
            for (String s : (LinkedList<String>) context.getSessionData(CK.E_COMMANDS)) {
                text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
            }
        }
        text += ChatColor.GREEN + "" + ChatColor.BOLD + "10 " + ChatColor.RESET + ChatColor.YELLOW + "- " 
                + Lang.get("done") + "\n";
        return text;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("1")) {
            return new MessagePrompt();
        } else if (input.equalsIgnoreCase("2")) {
            String s = (String) context.getSessionData(CK.E_CLEAR_INVENTORY);
            if (s.equalsIgnoreCase(Lang.get("yesWord"))) {
                context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("noWord"));
            } else {
                context.setSessionData(CK.E_CLEAR_INVENTORY, Lang.get("yesWord"));
            }
            return new ActionMainPrompt(context);
        } else if (input.equalsIgnoreCase("3")) {
            return new ItemListPrompt();
        } else if (input.equalsIgnoreCase("4")) {
            return new PotionEffectPrompt();
        } else if (input.equalsIgnoreCase("5")) {
            return new HungerPrompt();
        } else if (input.equalsIgnoreCase("6")) {
            return new SaturationPrompt();
        } else if (input.equalsIgnoreCase("7")) {
            return new HealthPrompt();
        } else if (input.equalsIgnoreCase("8")) {
            Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
            selectedTeleportLocations.put(((Player) context.getForWhom()).getUniqueId(), null);
            plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
            return new TeleportPrompt();
        } else if (input.equalsIgnoreCase("9")) {
            return new CommandsPrompt();
        }
        return new ActionMainPrompt(context);
    }
    
    public class MessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetMessagePrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                context.setSessionData(CK.E_MESSAGE, input);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_MESSAGE, null);
            }
            return new ActionMainPrompt(context);
        }
    }

    public class ItemListPrompt extends FixedSetPrompt {

        public ItemListPrompt() {
            super("1", "2", "3");
        }

        @Override
        public String getPromptText(ConversationContext context) {
            // Check/add newly made item
            if (context.getSessionData("newItem") != null) {
                if (context.getSessionData(CK.E_ITEMS) != null) {
                    List<ItemStack> items = getItems(context);
                    items.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.E_ITEMS, items);
                } else {
                    LinkedList<ItemStack> itemRews = new LinkedList<ItemStack>();
                    itemRews.add((ItemStack) context.getSessionData("tempStack"));
                    context.setSessionData(CK.E_ITEMS, itemRews);
                }
                context.setSessionData("newItem", null);
                context.setSessionData("tempStack", null);
            }
            String text = ChatColor.GOLD + Lang.get("eventEditorGiveItemsTitle") + "\n";
            if (context.getSessionData(CK.E_ITEMS) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            } else {
                for (ItemStack is : getItems(context)) {
                    text += ChatColor.GRAY + "    - " + ItemUtil.getDisplayString(is) + "\n";
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("stageEditorDeliveryAddItem") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("clear") + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("done");
            }
            return text;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new ItemStackPrompt(ItemListPrompt.this);
            } else if (input.equalsIgnoreCase("2")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorItemsCleared"));
                context.setSessionData(CK.E_ITEMS, null);
                return new ItemListPrompt();
            } else if (input.equalsIgnoreCase("3")) {
                return new ActionMainPrompt(context);
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<ItemStack> getItems(ConversationContext context) {
            return (List<ItemStack>) context.getSessionData(CK.E_ITEMS);
        }
    }
    
    private class PotionEffectPrompt extends FixedSetPrompt {

        public PotionEffectPrompt() {
            super("1", "2", "3", "4", "5");
        }

        @SuppressWarnings("unchecked")
        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + Lang.get("eventEditorPotionEffectsTitle") + "\n";
            if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - " 
                        + Lang.get("eventEditorSetPotionEffectTypes") + " (" + Lang.get("noneSet") + ")\n";
                text += ChatColor.GRAY + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("eventEditorSetPotionDurations") + " " + Lang.get("noneSet") + "\n";
                text += ChatColor.GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " " + Lang.get("noneSet")
                        + "\n";
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("done");
            } else {
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "1" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("eventEditorSetPotionEffectTypes") + "\n";
                for (String s : (LinkedList<String>) context.getSessionData(CK.E_POTION_TYPES)) {
                    text += ChatColor.GRAY + "    - " + ChatColor.AQUA + s + "\n";
                }
                if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - "
                            + Lang.get("eventEditorSetPotionDurations") + " (" + Lang.get("noneSet") + ")\n";
                    text += ChatColor.GRAY + "3 - " + Lang.get("eventEditorSetPotionMagnitudes") + " "
                            + Lang.get("noneSet") + "\n";
                } else {
                    text += ChatColor.BLUE + "" + ChatColor.BOLD + "2" + ChatColor.RESET + ChatColor.YELLOW + " - "
                            + Lang.get("noneSet") + "\n";
                    for (Long l : (LinkedList<Long>) context.getSessionData(CK.E_POTION_DURATIONS)) {
                        text += ChatColor.GRAY + "    - " + ChatColor.DARK_AQUA + MiscUtil.getTime(l * 50L) + "\n";
                    }
                    if (context.getSessionData(CK.E_POTION_STRENGHT) == null) {
                        text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - "
                                + Lang.get("eventEditorSetPotionMagnitudes") + " (" + Lang.get("noneSet") + ")\n";
                    } else {
                        text += ChatColor.BLUE + "" + ChatColor.BOLD + "3" + ChatColor.RESET + ChatColor.YELLOW + " - "
                                + Lang.get("eventEditorSetPotionMagnitudes") + "\n";
                        for (int i : (LinkedList<Integer>) context.getSessionData(CK.E_POTION_STRENGHT)) {
                            text += ChatColor.GRAY + "    - " + ChatColor.DARK_PURPLE + i + "\n";
                        }
                    }
                }
                text += ChatColor.BLUE + "" + ChatColor.BOLD + "4" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("clear") + "\n";
                text += ChatColor.GREEN + "" + ChatColor.BOLD + "5" + ChatColor.RESET + ChatColor.YELLOW + " - "
                        + Lang.get("done");
            }
            return text;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("1")) {
                return new PotionTypesPrompt();
            } else if (input.equalsIgnoreCase("2")) {
                if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorMustSetPotionTypesFirst"));
                    return new PotionEffectPrompt();
                } else {
                    return new PotionDurationsPrompt();
                }
            } else if (input.equalsIgnoreCase("3")) {
                if (context.getSessionData(CK.E_POTION_TYPES) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("eventEditorMustSetPotionTypesAndDurationsFirst"));
                    return new PotionEffectPrompt();
                } else if (context.getSessionData(CK.E_POTION_DURATIONS) == null) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("eventEditorMustSetPotionDurationsFirst"));
                    return new PotionEffectPrompt();
                } else {
                    return new PotionMagnitudesPrompt();
                }
            } else if (input.equalsIgnoreCase("4")) {
                context.getForWhom().sendRawMessage(ChatColor.YELLOW + Lang.get("eventEditorPotionsCleared"));
                context.setSessionData(CK.E_POTION_TYPES, null);
                context.setSessionData(CK.E_POTION_DURATIONS, null);
                context.setSessionData(CK.E_POTION_STRENGHT, null);
                return new PotionEffectPrompt();
            } else if (input.equalsIgnoreCase("5")) {
                int one;
                int two;
                int three;
                if (context.getSessionData(CK.E_POTION_TYPES) != null) {
                    one = ((List<String>) context.getSessionData(CK.E_POTION_TYPES)).size();
                } else {
                    one = 0;
                }
                if (context.getSessionData(CK.E_POTION_DURATIONS) != null) {
                    two = ((List<Long>) context.getSessionData(CK.E_POTION_DURATIONS)).size();
                } else {
                    two = 0;
                }
                if (context.getSessionData(CK.E_POTION_STRENGHT) != null) {
                    three = ((List<Integer>) context.getSessionData(CK.E_POTION_STRENGHT)).size();
                } else {
                    three = 0;
                }
                if (one == two && two == three) {
                    return new ActionMainPrompt(context);
                } else {
                    context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("eventEditorListSizeMismatch"));
                    return new PotionEffectPrompt();
                }
            }
            return null;
        }
    }

    private class PotionTypesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String effs = ChatColor.LIGHT_PURPLE + Lang.get("eventEditorPotionTypesTitle") + "\n";
            for (PotionEffectType pet : PotionEffectType.values()) {
                effs += (pet != null && pet.getName() != null) ? (ChatColor.DARK_PURPLE + pet.getName() + "\n") : "";
            }
            return effs + ChatColor.YELLOW + Lang.get("eventEditorSetPotionEffectsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<String> effTypes = new LinkedList<String>();
                for (String s : input.split(" ")) {
                    if (PotionEffectType.getByName(s.toUpperCase()) != null) {
                        effTypes.add(PotionEffectType.getByName(s.toUpperCase()).getName());
                        context.setSessionData(CK.E_POTION_TYPES, effTypes);
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                               + Lang.get("eventEditorInvalidPotionType"));
                        return new PotionTypesPrompt();
                    }
                }
            }
            return new PotionEffectPrompt();
        }
    }

    private class PotionDurationsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetPotionDurationsPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<Long> effDurations = new LinkedList<Long>();
                for (String s : input.split(" ")) {
                    try {
                        int i = Integer.parseInt(s);
                        long l = i * 1000;
                        if (l < 1000) {
                            player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new PotionDurationsPrompt();
                        }
                        effDurations.add(l / 50L);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", s));
                        return new PotionDurationsPrompt();
                    }
                }
                context.setSessionData(CK.E_POTION_DURATIONS, effDurations);
            }
            return new PotionEffectPrompt();
        }
    }

    private class PotionMagnitudesPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetPotionMagnitudesPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                LinkedList<Integer> magAmounts = new LinkedList<Integer>();
                for (String s : input.split(" ")) {
                    try {
                        int i = Integer.parseInt(s);
                        if (i < 1) {
                            player.sendMessage(ChatColor.RED + Lang.get("invalidMinimum").replace("<number>", "1"));
                            return new PotionMagnitudesPrompt();
                        }
                        magAmounts.add(i);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + Lang.get("reqNotANumber").replace("<input>", s));
                        return new PotionMagnitudesPrompt();
                    }
                }
                context.setSessionData(CK.E_POTION_STRENGHT, magAmounts);
            }
            return new PotionEffectPrompt();
        }
    }
    
    public class HungerPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetHungerPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 0) {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED
                                + Lang.get("invalidMinimum").replace("<number>", "0"));
                        return new HungerPrompt();
                    } else {
                        context.setSessionData(CK.E_HUNGER, (Integer) i);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new HungerPrompt();
                }
            } else {
                context.setSessionData(CK.E_HUNGER, null);
            }
            return new ActionMainPrompt(context);
        }
    }

    public class SaturationPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetSaturationPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 0) {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED
                                + Lang.get("invalidMinimum").replace("<number>", "0"));
                        return new SaturationPrompt();
                    } else {
                        context.setSessionData(CK.E_SATURATION, (Integer) i);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new SaturationPrompt();
                }
            } else {
                context.setSessionData(CK.E_SATURATION, null);
            }
            return new ActionMainPrompt(context);
        }
    }

    public class HealthPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetHealthPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                try {
                    int i = Integer.parseInt(input);
                    if (i < 0) {
                        ((Player) context.getForWhom()).sendMessage(ChatColor.RED
                                + Lang.get("invalidMinimum").replace("<number>", "0"));
                        return new HealthPrompt();
                    } else {
                        context.setSessionData(CK.E_HEALTH, (Integer) i);
                    }
                } catch (NumberFormatException e) {
                    context.getForWhom().sendRawMessage(ChatColor.RED
                            + Lang.get("reqNotANumber").replace("<input>", input));
                    return new HealthPrompt();
                }
            } else {
                context.setSessionData(CK.E_HEALTH, null);
            }
            return new ActionMainPrompt(context);
        }
    }
    
    public class TeleportPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.YELLOW + Lang.get("eventEditorSetTeleportPrompt");
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdDone"))) {
                Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
                Block block = selectedTeleportLocations.get(player.getUniqueId());
                if (block != null) {
                    Location loc = block.getLocation();
                    context.setSessionData(CK.E_TELEPORT, ConfigUtil.getLocationInfo(loc));
                    selectedTeleportLocations.remove(player.getUniqueId());
                    plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                } else {
                    player.sendMessage(ChatColor.RED + Lang.get("eventEditorSelectBlockFirst"));
                    return new TeleportPrompt();
                }
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_TELEPORT, null);
                Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
                selectedTeleportLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                return new ActionMainPrompt(context);
            } else if (input.equalsIgnoreCase(Lang.get("cmdCancel"))) {
                Map<UUID, Block> selectedTeleportLocations = plugin.getActionFactory().getSelectedTeleportLocations();
                selectedTeleportLocations.remove(player.getUniqueId());
                plugin.getActionFactory().setSelectedTeleportLocations(selectedTeleportLocations);
                return new ActionMainPrompt(context);
            } else {
                return new TeleportPrompt();
            }
        }
    }

    public class CommandsPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            String text = ChatColor.GOLD + "" + ChatColor.ITALIC + Lang.get("eventEditorCommandsNote");
            return ChatColor.YELLOW + Lang.get("eventEditorSetCommandsPrompt") + "\n" + text;
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false 
                    && input.equalsIgnoreCase(Lang.get("cmdClear")) == false) {
                String[] commands = input.split(Lang.get("charSemi"));
                LinkedList<String> cmdList = new LinkedList<String>();
                cmdList.addAll(Arrays.asList(commands));
                context.setSessionData(CK.E_COMMANDS, cmdList);
            } else if (input.equalsIgnoreCase(Lang.get("cmdClear"))) {
                context.setSessionData(CK.E_COMMANDS, null);
            }
            return new ActionMainPrompt(context);
        }
    }
}
