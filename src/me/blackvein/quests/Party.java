package me.blackvein.quests;

import java.util.LinkedList;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class Party implements ConversationAbandonedListener, ColorUtil{
    
    public static final String partyPrefix = PURPLE + "[" + PINK + "Party" + PURPLE + "] " + RESET;
    public static ConversationFactory factory;
    private final LinkedList<Quester> members;
    private Quester leader;
    private Quest currentQuest = null;
    private Quests quests = null;
    
    public Party(Quests plugin, Quester q){
        
        quests = plugin;
        members = new LinkedList<Quester>();
        members.add(q);
        leader = q;
        
    }
    
    public void initFactory(){
        
        factory = new ConversationFactory(quests)
                .withModality(false)
                .withPrefix(new PartyPrefix())
                .withFirstPrompt(new InvitePrompt())
                .withTimeout(Quests.inviteTimeout)
                .thatExcludesNonPlayersWithMessage("Console may not perform this conversation!")
                .addConversationAbandonedListener(this);
        
    }
    
    public void addMember(Quester q){
        members.add(q);
    }
    
    public void removeMember(Quester q){
        members.remove(q);
    }
    
    public void setLeader(Quester q){
        leader = q;
    }
    
    public void disband(){
        leader.reset();
        leader.currentStage = null;
        leader.currentQuest = null;
        for(Quester quester : members){
            quester.reset();
            quester.currentStage = null;
            quester.currentQuest = null;
        }
        
        members.clear();
        leader = null;
        currentQuest = null;
    }
    
    public void setQuest(Quest q){
        currentQuest = q;
    }
    
    public void sendMessage(String msg){
        for(Quester q : members){
            q.getPlayer().sendMessage(partyPrefix + msg);
        }
    }
    
    public void sendMessageEx(String msg, Quester exclude){
        for(Quester q : members){
            if(q != exclude)
                q.getPlayer().sendMessage(partyPrefix + msg);
        }
    }
    
    
    public LinkedList<Quester> getAllMembers(){
        return members;
    }
    
    public LinkedList<Quester> getMembers(){
        LinkedList<Quester> mems = new LinkedList<Quester>();
        mems.addAll(members);
        mems.remove(leader);
        return mems;
    }
    
    public Quester getLeader(){
        return leader;
    }
    
    public Quester getMember(Player p){
        return getMember(p.getName());
    }
    
    public Quester getMember(String s){
        for(Quester q : members){
            if(q.name.equalsIgnoreCase(s))
                return q;
        }
        return null;
    }
    
    public Quest getQuest(){
        return currentQuest;
    }
    
    public void cancelQuest(){
        
        for(Quester quester : members){
            quester.reset();
            quester.currentStage = null;
            quester.currentQuest = null;
        }
        
        currentQuest = null;
        
    }
    
    public boolean isLeader(Quester quester){
        return leader == quester;
    }
    
    public void sendInvite(Quester target){
        
        Player player = target.getPlayer();
        
        
    }
    
    public void checkSize(){
        
        int size = members.size();
        if(currentQuest != null){
            
            if(currentQuest.parties > size){
                
                sendMessage(RED + "Your party size is not large enough to continue " + PINK + currentQuest.name + RED + ". The Quest has been cancelled.");
                cancelQuest();
                
            }
            
        }
        
    }
    
    public int getSize(){
        return members.size();
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent cae) {
        throw new UnsupportedOperationException("Not supported yet.");a
    }
    
    private static class PartyPrefix implements ConversationPrefix {

        @Override
        public String getPrefix(ConversationContext cc) {
            return "" + GRAY;
        }
        
        
        
    }
    
    private class InvitePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {

            String text = partyPrefix + PINK + "You have been invited to " + PURPLE + ((String) context.getSessionData("inviter")) + PINK + "'s party.\n";
            
            return text + YELLOW + "Accept Invite?  " + GREEN + "Yes / No";

        }

        @Override
        public Prompt acceptInput(ConversationContext context, String s) {

            Player player = (Player) context.getForWhom();

            if (s.equalsIgnoreCase("Yes")) {

                String inviterName = (String) context.getSessionData("inviter");
                
                Quester quester =
                
                return Prompt.END_OF_CONVERSATION;

            } else if (s.equalsIgnoreCase("No")) {

                player.sendMessage(partyPrefix + YELLOW + "Declined invite.");
                return Prompt.END_OF_CONVERSATION;

            } else {

                player.sendMessage(RED + "Invalid choice. Type \'Yes\' or \'No\'");
                return new InvitePrompt();

            }


        }
    }
    
}
