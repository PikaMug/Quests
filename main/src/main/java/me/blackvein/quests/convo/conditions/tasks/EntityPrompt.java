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

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;

import me.blackvein.quests.convo.conditions.main.ConditionMainPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorNumericPrompt;
import me.blackvein.quests.convo.quests.QuestsEditorStringPrompt;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenNumericPromptEvent;
import me.blackvein.quests.events.editor.quests.QuestsEditorPostOpenStringPromptEvent;
import me.blackvein.quests.util.CK;
import me.blackvein.quests.util.Lang;
import me.blackvein.quests.util.MiscUtil;

public class EntityPrompt extends QuestsEditorNumericPrompt {
    
    public EntityPrompt(final ConversationContext context) {
        super(context);
    }
    
    private final int size = 2;
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public String getTitle(final ConversationContext context) {
        return Lang.get("conditionEditorEntity");
    }
    
    @Override
    public ChatColor getNumberColor(final ConversationContext context, final int number) {
        switch (number) {
            case 1:
                return ChatColor.BLUE;
            case 2:
                return ChatColor.GREEN;
            default:
                return null;
        }
    }
    
    @Override
    public String getSelectionText(final ConversationContext context, final int number) {
        switch(number) {
        case 1:
            return ChatColor.YELLOW + Lang.get("conditionEditorRideEntity");
        case 2:
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
            if (context.getSessionData(CK.C_WHILE_RIDING_ENTITY) == null) {
                return ChatColor.GRAY + "(" + Lang.get("noneSet") + ")";
            } else {
                String text = "\n";
                for (final String s: (List<String>) context.getSessionData(CK.C_WHILE_RIDING_ENTITY)) {
                    text += ChatColor.GRAY + "     - " + ChatColor.BLUE + MiscUtil.getProperMobType(s) + "\n";
                }
                return text;
            }
        case 2:
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
            return new EntitiesPrompt(context);
        case 2:
            try {
                return new ConditionMainPrompt(context);
            } catch (final Exception e) {
                context.getForWhom().sendRawMessage(ChatColor.RED + Lang.get("itemCreateCriticalError"));
                return Prompt.END_OF_CONVERSATION;
            }
        default:
            return new EntityPrompt(context);
        }
    }
    
    public class EntitiesPrompt extends QuestsEditorStringPrompt {
        
        public EntitiesPrompt(final ConversationContext context) {
            super(context);
        }

        @Override
        public String getTitle(final ConversationContext context) {
            return Lang.get("conditionEditorEntitiesTitle");
        }

        @Override
        public String getQueryText(final ConversationContext context) {
            return Lang.get("conditionEditorEntitiesPrompt");
        }
        
        @Override
        public String getPromptText(final ConversationContext context) {
            final QuestsEditorPostOpenStringPromptEvent event = new QuestsEditorPostOpenStringPromptEvent(context, this);
            context.getPlugin().getServer().getPluginManager().callEvent(event);
            
            String entities = ChatColor.LIGHT_PURPLE + getTitle(context) + "\n";
            final EntityType[] mobArr = EntityType.values();
            for (int i = 0; i < mobArr.length; i++) {
                final EntityType type = mobArr[i];
                if (type.getEntityClass() == null || !Vehicle.class.isAssignableFrom(type.getEntityClass())) {
                    continue;
                }
                entities += MiscUtil.snakeCaseToUpperCamelCase(mobArr[i].name()) + ", ";
            }
            entities = entities.substring(0, entities.length() - 2) + "\n";
            return entities + ChatColor.YELLOW + getQueryText(context);
        }

        @Override
        public Prompt acceptInput(final ConversationContext context, final String input) {
            if (input.equalsIgnoreCase(Lang.get("cmdCancel")) == false) {
                final LinkedList<String> mobTypes = new LinkedList<String>();
                for (final String s : input.split(" ")) {
                    if (MiscUtil.getProperMobType(s) != null) {
                        final EntityType type = MiscUtil.getProperMobType(s);
                        if (type.getEntityClass() != null && Vehicle.class.isAssignableFrom(type.getEntityClass())) {
                            mobTypes.add(s);
                            context.setSessionData(CK.C_WHILE_RIDING_ENTITY, mobTypes);
                        } else {
                            context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                    + Lang.get("stageEditorInvalidMob"));
                            return new EntitiesPrompt(context);
                        }
                    } else {
                        context.getForWhom().sendRawMessage(ChatColor.LIGHT_PURPLE + s + " " + ChatColor.RED 
                                + Lang.get("stageEditorInvalidMob"));
                        return new EntitiesPrompt(context);
                    }
                }
            }
            return new EntityPrompt(context);
        }
    }
}
