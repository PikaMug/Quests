package me.blackvein.quests;

public class StageTimer implements Runnable{
    
    Quester quester;
    
    public StageTimer(Quester q){
        
        quester = q;
        
    }
    
    @Override
    public void run(){
        
        if(quester.delayOver){
            quester.currentQuest.nextStage(quester);
        }
                    
        quester.delayOver = true;
        
    }
    
}
