package me.blackvein.quests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class QuestFactory implements ConversationAbandonedListener {

    Quests quests;
    Map<Player, Quest> editSessions = new HashMap<Player, Quest>();
    ConversationFactory convoCreator;
    static final ChatColor BOLD = ChatColor.BOLD;
    static final ChatColor AQUA = ChatColor.AQUA;
    static final ChatColor BLUE = ChatColor.BLUE;
    static final ChatColor GOLD = ChatColor.GOLD;
    static final ChatColor RED = ChatColor.RED;
    static final ChatColor DARKRED = ChatColor.DARK_RED;
    static final ChatColor YELLOW = ChatColor.YELLOW;
    static final ChatColor RESET = ChatColor.RESET;

    File questsFile;

    @SuppressWarnings("LeakingThisInConstructor")
    public QuestFactory(Quests plugin){

        quests = plugin;
        questsFile = new File(plugin.getDataFolder(), "quests.yml");

        //Ensure to initialize convoCreator last, to ensure that 'this' is fully initialized before it is passed
        this.convoCreator = new ConversationFactory(plugin)
                .withModality(false)
                .withLocalEcho(false)
                .withPrefix(new QuestCreatorPrefix())
                .withFirstPrompt(new MenuPrompt())
                .withTimeout(3600)
                .thatExcludesNonPlayersWithMessage("Console may not perform this operation!")
                .addConversationAbandonedListener(this);

    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {



    }

    private class QuestCreatorPrefix implements ConversationPrefix {

        @Override
        public String getPrefix(ConversationContext context){

            return "";

        }

    }

    private class MenuPrompt extends FixedSetPrompt {

        public MenuPrompt(){

            super ("1", "2", "3");

        }

        @Override
        public String getPromptText(ConversationContext context){

            String text =
            GOLD + "- Quest Editor -\n" +
            BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Create a Quest\n" +
            BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Edit a Quest\n" +
            BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Delete a Quest"
            ;

            return text;

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("1"))
                return new QuestNamePrompt();

            return null;

        }

    }

    private class CreateMenuPrompt extends FixedSetPrompt {

        public CreateMenuPrompt(){

            super("1", "2", "3", "4", "5", "6");

        }

        @Override
        public String getPromptText(ConversationContext context){

            String text =
            GOLD + "- Quest: " + AQUA + context.getSessionData("questName") + GOLD + " -\n";

            text += BLUE + "" + BOLD + "1" + RESET + YELLOW + " - Set name\n";

            if(context.getSessionData("askMessage") == null)
                text += BLUE + "" + BOLD + "2" + RESET + RED + " - Set ask message " + DARKRED + "(Required, none set)\n";
            else
                text += BLUE + "" + BOLD + "2" + RESET + YELLOW + " - Set ask message (\"" + context.getSessionData("askMessage") + "\")\n";

            if(context.getSessionData("finishMessage") == null)
                text += BLUE + "" + BOLD + "3" + RESET + RED + " - Set finish message " + DARKRED + "(Required, none set)\n";
            else
                text += BLUE + "" + BOLD + "3" + RESET + YELLOW + " - Set finish message (\"" + context.getSessionData("finishMessage") + "\")\n";

            if(context.getSessionData("redoDelay") == null)
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set redo delay (None set)";
            else
                text += BLUE + "" + BOLD + "4" + RESET + YELLOW + " - Set redo delay (" + Quests.getTime((Long)context.getSessionData("redoDelay")) + ")";

            if(context.getSessionData("npcStart") == null && quests.citizens != null)
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set NPC start (None set)";
            else if(quests.citizens != null)
                text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set NPC start (" + quests.citizens.getNPCRegistry().getById((Integer)context.getSessionData("npcStart")).getName() + ")";

            if(context.getSessionData("blockStart") == null){

                if(quests.citizens != null)
                    text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Block start (None set)";
                else
                    text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Block start (None set)";

            }else{

                if(quests.citizens != null){
                    Location l = (Location) context.getSessionData("blockStart");
                    text += BLUE + "" + BOLD + "6" + RESET + YELLOW + " - Set Block start (" + l.getWorld().getName() + ", " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")";
                }else{
                    Location l = (Location) context.getSessionData("blockStart");
                    text += BLUE + "" + BOLD + "5" + RESET + YELLOW + " - Set Block start (" + l.getWorld().getName() + ", " + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ() + ")";
                }

            }

            return text;

        }

        @Override
        public Prompt acceptValidatedInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("1")){

                return new SetNamePrompt();

            }else if(input.equalsIgnoreCase("2")){

                return new AskMessagePrompt();

            }else if(input.equalsIgnoreCase("3")){

                return new FinishMessagePrompt();

            }else if(input.equalsIgnoreCase("4")){

                return new RedoDelayPrompt();

            }

            return null;

        }

    }

    private class QuestNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){

            String text =
            AQUA + "Create new Quest " + GOLD + "- Enter a name for the Quest (Or enter \'cancel\' to return to the main menu)"
            ;

            return text;

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false){

                for(Quest q : quests.quests){

                    if(q.name.equalsIgnoreCase(input)){

                        context.getForWhom().sendRawMessage(ChatColor.RED + "Quest already exists!");
                        return new QuestNamePrompt();

                    }

                }

                context.setSessionData("questName", input);
                return new CreateMenuPrompt();

            }else{

                return new MenuPrompt();

            }

        }

    }

    private class SetNpcStartPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context){

            return ChatColor.YELLOW + "Enter NPC ID (or -1 to return)";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){

            if(input.intValue() != -1){

                if(quests.citizens.getNPCRegistry().getById(input.intValue()) == null){
                    context.getForWhom().sendRawMessage(ChatColor.RED + "No NPC exists with that id!");
                    return new SetNpcStartPrompt();
                }else{
                    context.setSessionData("")
                }

            }else{
                return new CreateMenuPrompt();
            }
                context.setSessionData("questName", input);

            return new CreateMenuPrompt();

        }

    }

    private class SetNamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){

            return ChatColor.YELLOW + "Enter Quest name (or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false)
                context.setSessionData("questName", input);

            return new CreateMenuPrompt();

        }

    }

    private class AskMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){

            return ChatColor.YELLOW + "Enter ask message (or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false)
                context.setSessionData("askMessage", input);

            return new CreateMenuPrompt();

        }

    }

    private class FinishMessagePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context){

            return ChatColor.YELLOW + "Enter finish message (or \'cancel\' to return)";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input){

            if(input.equalsIgnoreCase("cancel") == false)
                context.setSessionData("finishMessage", input);

            return new CreateMenuPrompt();

        }

    }

    private class RedoDelayPrompt extends NumericPrompt{

        @Override
        public String getPromptText(ConversationContext context){

            return ChatColor.YELLOW + "Enter amount of time (in milliseconds)";

        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input){

            if(input.longValue() < 0)
                context.getForWhom().sendRawMessage(ChatColor.RED + "Amount must be a positive number.");
            else
                context.setSessionData("redoDelay", input.longValue());

            return new CreateMenuPrompt();

        }

    }

}
