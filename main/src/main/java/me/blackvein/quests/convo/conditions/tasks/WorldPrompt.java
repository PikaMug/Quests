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

package me.blackvein.quests.convo.conditions.tasks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class WorldPrompt extends QuestsEditorNumericPrompt {
    
    public WorldPrompt(final ConversationContext context) {
        super(context);
    }
    
    private final int size = 3;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("conditionEditorWorld");
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
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("conditionEditorStayWithinWorld");
        case 2:
            return ChatColor.YELLOW + Lang.get("conditionEditorStayWithinBiome");
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
            if (context.getSessionData(CK.C_WHILE_WITHIN_WORLD) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                for (final String s: (List<String>) context.getSessionData(CK.C_WHILE_WITHIN_WORLD)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + s + "\n";
                }
                return text;
            }
        case 2:
            if (context.getSessionData(CK.C_WHILE_WITHIN_BIOME) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                for (final String s: (List<String>) context.getSessionData(CK.C_WHILE_WITHIN_BIOME)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + s + "\n";
                }
                return text;
            }
        case 3:
            return "";
        default:
            return null;
        }
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final QuestsEditorPostOpenNumericPromptEvent event = new QuestsEditorPostOpenNumericPromptEvent(context, this);
        context.getPlugin().getServer().getPluginManager().callEvent(event);
        
        String text = ChatColor.AQUA + "- " + getTitle(context) + " -\n";
        for (int i = 1; i <= size; i++) {
            text += getNumberColor(context, i) + "" + ChatColor.BOLD + i + ChatColor.RESET + " - " 
                    + getSelectionText(context, i) + " " + getAdditionalText(context, i) + "\n";
        }
        return text;
    }
    
    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        switch(input.intValue()) {
        case 1:
            return new WorldsPrompt(context);
        case 2:
            return new BiomesPrompt(context);
        case 3:
            try {
                return new ConditionMainPrompt(context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new WorldPrompt(context);
        }
    }
    
    public class WorldsPrompt extends QuestsEditorStringPrompt {
        
        public WorldsPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorWorldsTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorWorldsPrompt");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String worlds = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n";
            final List<World> worldArr = Bukkit.getWorlds();
            for (int i = 0; i < worldArr.size(); i++) {
                if (i < (worldArr.size() - 1)) {
                    worlds += MiscUtil.snakeCaseToUpperCamelCase(worldArr.get(i).getName()) + ", ";
                } else {
                    worlds += MiscUtil.snakeCaseToUpperCamelCase(worldArr.get(i).getName()) + "\n";
                }
            }
            return worlds + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final LinkedList<String> worlds = new LinkedList<String>();
                for (final String s : input.split(" ")) {
                    if (Bukkit.getWorld(s) != null) {
                        worlds.add(s);
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("conditionEditorInvalidWorld"));
                        return new WorldsPrompt(context);
                    }
                }
                context.setSessionData(CK.C_WHILE_WITHIN_WORLD, worlds);
            }
            return new WorldPrompt(context);
        }
    }
    
    public class BiomesPrompt extends QuestsEditorStringPrompt {
        
        public BiomesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorBiomesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorBiomesPrompt");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String biomes = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n";
            final LinkedList<Biome> biomeArr = new LinkedList<Biome>(Arrays.asList(Biome.values()));
            for (int i = 0; i < biomeArr.size(); i++) {
                if (i < (biomeArr.size() - 1)) {
                    biomes += MiscUtil.snakeCaseToUpperCamelCase(biomeArr.get(i).name()) + ", ";
                } else {
                    biomes += MiscUtil.snakeCaseToUpperCamelCase(biomeArr.get(i).name()) + "\n";
                }
            }
            return biomes + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            final Player player = (Player) context.getForWhom();
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final LinkedList<String> biomes = new LinkedList<String>();
                for (final String s : input.split(" ")) {
                    if (MiscUtil.getProperBiome(s) != null) {
                        biomes.add(s);
                    } else {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("conditionEditorInvalidBiome"));
                        return new BiomesPrompt(context);
                    }
                }
                context.setSessionData(CK.C_WHILE_WITHIN_BIOME, biomes);
            }
            return new WorldPrompt(context);
        }
    }
}
