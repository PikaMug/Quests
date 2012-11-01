package me.blackvein.quests;

import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Quest {

    String name;
    String description;
    String finished;
    int redoDelay = -1;
    LinkedList<Stage> stages = new LinkedList<Stage>();
    NPC npcStart;
    Location blockStart;
    Quests plugin;
    Map<Material, Integer> questItems = new EnumMap<Material, Integer>(Material.class);

    //Requirements
    int moneyReq = 0;
    int questPointsReq = 0;

    List<Integer> itemIds = new LinkedList<Integer>();
    List<Integer> itemAmounts = new LinkedList<Integer>();
    List<Boolean> removeItems = new LinkedList<Boolean>();

    List<String> neededQuests = new LinkedList<String>();

    List<String> permissionReqs = new LinkedList<String>();

    String failRequirements;
    //

    //Rewards
    int moneyReward = 0;
    int questPoints = 0;
    int exp = 0;
    List<String> commands = new LinkedList<String>();
    List<String> permissions = new LinkedList<String>();
    LinkedList<ItemStack> itemRewards = new LinkedList<ItemStack>();
    LinkedList<Integer> itemRewardAmounts = new LinkedList<Integer>();
      //Heroes
      int heroesExp = 0;
      String heroesClass = null;
      String heroesSecClass = null;
      //

      //mcMMO
      List<String> mcmmoSkills = new LinkedList<String>();
      List<Integer> mcmmoAmounts = new LinkedList<Integer>();
      //
    //
    public void nextStage(Quester q){

        if(q.currentStage.delay < 0){

            Player player = plugin.getServer().getPlayerExact(q.name);

            if(stages.indexOf(q.currentStage) == (stages.size() - 1)){

                if(q.currentStage.script != null)
                    plugin.trigger.parseQuestTaskTrigger(q.currentStage.script, player);
                if(q.currentStage.event != null)
                    q.currentStage.event.happen(player);

                completeQuest(q);

            }else {

                q.reset();
                player.sendMessage(plugin.parseString(q.currentStage.finished, q.currentQuest));
                if(q.currentStage.script != null)
                    plugin.trigger.parseQuestTaskTrigger(q.currentStage.script, player);
                if(q.currentStage.event != null)
                    q.currentStage.event.happen(player);
                q.currentStage = stages.get(stages.indexOf(q.currentStage) + 1);
                q.addEmpties();

                for (Entry e : q.currentStage.itemsToCollect.entrySet()) {

                    if ((Boolean) e.getValue() == true) {

                        Map<Material, Integer> tempMap = (Map<Material, Integer>) e.getKey();
                        for (Entry e2 : tempMap.entrySet()) {

                            questItems.put((Material) e2.getKey(), (Integer) e2.getValue());

                        }

                    }

                }

                for (Entry e : q.currentStage.itemsToCraft.entrySet()) {

                    if ((Boolean) e.getValue() == true) {

                        Map<Material, Integer> tempMap = (Map<Material, Integer>) e.getKey();
                        for (Entry e2 : tempMap.entrySet()) {

                            questItems.put((Material) e2.getKey(), (Integer) e2.getValue());

                        }

                    }

                }

                player.sendMessage(ChatColor.GOLD + "---(Objectives)---");
                for(String s : q.getObjectives()){

                    player.sendMessage(s);

                }

            }

            q.delayStartTime = 0;
            q.delayTimeLeft = -1;

        }else{

            q.startStageTimer();

        }

    }

    public String getName(){
        return name;
    }

    public boolean testRequirements(Player player){

        Quester quester = plugin.getQuester(player.getName());

        if(moneyReq != 0 && Quests.economy.getBalance(player.getName()) < moneyReq)
            return false;

        PlayerInventory inventory = player.getInventory();
        int num = 0;

        for(int i : itemIds){

            for(ItemStack stack : inventory.getContents()){

                if(stack != null){
                    if(i == stack.getTypeId())
                        num += stack.getAmount();
                }

            }

            if(num < itemAmounts.get(itemIds.indexOf(i)))
                return false;

            num = 0;

        }

        for(String s : permissionReqs){

            if(player.hasPermission(s) == false)
                return false;

        }

        if(quester.questPoints < questPointsReq)
            return false;

        if(quester.completedQuests.containsAll(neededQuests) == false)
            return false;

        return true;

    }

    public void completeQuest(Quester q){

        Player player = plugin.getServer().getPlayerExact(q.name);
        q.reset();
        q.completedQuests.add(name);
        String none = ChatColor.GRAY + "- (None)";
        player.sendMessage(plugin.parseString(finished, q.currentQuest));
        if(moneyReward > 0 && Quests.economy != null){
            Quests.economy.depositPlayer(q.name, moneyReward);
            none = null;
        }
        if(redoDelay > -1)
            q.completedTimes.put(this.name, System.currentTimeMillis());

        for(ItemStack i : itemRewards){
            Quests.addItem(player, i);
            none = null;
        }

        for(Entry entry : questItems.entrySet()){

            Material material = (Material) entry.getKey();
            int amount = (Integer) entry.getValue();
            for(ItemStack stack : player.getInventory().getContents()){

                if(stack != null){

                    if(stack.getType().equals(material)){

                        if(stack.getAmount() > amount){

                            stack.setAmount(stack.getAmount() - amount);
                            break;

                        }else{

                            amount -= stack.getAmount();
                            stack.setAmount(0);
                            if(amount == 0)
                                break;

                        }

                    }

                }

            }

        }

        for(String s : commands){

            s = s.replaceAll("<player>", player.getName());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s);
            none = null;

        }

        for(String s : permissions){

            Quests.permission.playerAdd(player, s);
            none = null;

        }

        for(String s : mcmmoSkills){

            Quests.mcmmo.getPlayerProfile(player.getName()).skillUp(Quests.getMcMMOSkill(s), mcmmoAmounts.get(mcmmoSkills.indexOf(s)));
            none = null;

        }

        if(exp > 0){
            player.giveExp(exp);
            none = null;
        }

        if(heroesExp > 0){
            plugin.heroes.getCharacterManager().getHero(player).gainExp(heroesExp, ExperienceType.QUESTING, player.getLocation());
            none = null;
        }

        if(heroesClass != null){
            plugin.heroes.getCharacterManager().getHero(player).changeHeroClass(plugin.heroes.getClassManager().getClass(heroesClass), false);
            none = null;
        }

        if(heroesSecClass != null){
            plugin.heroes.getCharacterManager().getHero(player).changeHeroClass(plugin.heroes.getClassManager().getClass(heroesSecClass), true);
            none = null;
        }
        player.sendMessage(ChatColor.GOLD + "**QUEST COMPLETE: " + ChatColor.YELLOW + q.currentQuest.name + ChatColor.GOLD + "**");
        player.sendMessage(ChatColor.GREEN + "Rewards:");

        if(questPoints > 0){
            player.sendMessage("- " + ChatColor.DARK_GREEN + questPoints + " Quest Points");
            q.questPoints += questPoints;
            none = null;
        }

        for(ItemStack i : itemRewards){
            player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ChatColor.GRAY + " x " + itemRewardAmounts.get(itemRewards.indexOf(i)));
            none = null;
        }

        if(moneyReward > 1){
            player.sendMessage("- " + ChatColor.DARK_GREEN + moneyReward + " " + ChatColor.DARK_PURPLE + Quests.getCurrency(true));
            none = null;
        }else if(moneyReward == 1){
            player.sendMessage("- " + ChatColor.DARK_GREEN + moneyReward + " " + ChatColor.DARK_PURPLE + Quests.getCurrency(false));
            none = null;
        }

        if(exp > 0){
            player.sendMessage("- " + ChatColor.DARK_GREEN + exp + ChatColor.DARK_PURPLE + " Experience");
            none = null;
        }

        if(heroesExp > 0){
            player.sendMessage("- " + ChatColor.DARK_GREEN + heroesExp + ChatColor.DARK_PURPLE + " Heroes Exp");
            none = null;
        }

        if(heroesClass != null){
            player.sendMessage("- " + ChatColor.DARK_PURPLE + heroesClass + ChatColor.AQUA + " Heroes Class");
            none = null;
        }

        if(heroesSecClass != null){
            player.sendMessage("- " + ChatColor.DARK_PURPLE + heroesSecClass + ChatColor.AQUA + " Heroes Secondary Class");
            none = null;
        }

        if(none != null){
            player.sendMessage(none);
        }
        q.currentQuest = null;
        q.currentStage = null;

        q.saveData();
        player.updateInventory();

    }

    @Override
    public boolean equals(Object o){

        if(o instanceof Quest){

            Quest other = (Quest) o;

            if(other.blockStart != null && blockStart != null){
                if(other.blockStart.equals(blockStart) == false)
                    return false;
            }else if(other.blockStart != null && blockStart == null){
                return false;
            }else if(other.blockStart == null && blockStart != null)
                return false;

            for(String s : other.commands){

                if(commands.get(other.commands.indexOf(s)).equals(s) == false)
                    return false;

            }

            if(other.description.equals(description) == false)
                return false;

            if(other.exp != exp)
                return false;

            if(other.failRequirements != null && failRequirements != null){
                if(other.failRequirements.equals(failRequirements) == false)
                    return false;
            }else if(other.failRequirements != null && failRequirements == null){
                return false;
            }else if(other.failRequirements == null && failRequirements != null)
                return false;

            if(other.finished.equals(finished) == false)
                return false;

            if(other.heroesClass != null && heroesClass != null){
                if(other.heroesClass.equals(heroesClass) == false)
                    return false;
            }else if(other.heroesClass != null && heroesClass == null){
                return false;
            }else if(other.heroesClass == null && heroesClass != null)
                return false;

            if(other.heroesExp != heroesExp)
                return false;

            if(other.heroesSecClass != null && heroesSecClass != null){
                if(other.heroesSecClass.equals(heroesSecClass) == false)
                    return false;
            }else if(other.heroesSecClass != null && heroesSecClass == null){
                return false;
            }else if(other.heroesSecClass == null && heroesSecClass != null)
                return false;

            if(other.itemAmounts.equals(itemAmounts) == false)
                return false;

            if(other.itemIds.equals(itemIds) == false)
                return false;

            if(other.itemRewards.equals(itemRewards) == false)
                return false;

            if(other.mcmmoAmounts.equals(mcmmoAmounts) == false)
                return false;

            if(other.mcmmoSkills.equals(mcmmoSkills) == false)
                return false;

            if(other.moneyReq != moneyReq)
                return false;

            if(other.moneyReward != moneyReward)
                return false;

            if(other.name.equals(name) == false)
                return false;

            if(other.neededQuests.equals(neededQuests) == false)
                return false;

            if(other.npcStart != null && npcStart != null){
                if(other.npcStart.equals(npcStart) == false)
                    return false;
            }else if(other.npcStart != null && npcStart == null){
                return false;
            }else if(other.npcStart == null && npcStart != null)
                return false;

            if(other.permissionReqs.equals(permissionReqs) == false)
                return false;

            if(other.permissions.equals(permissions) == false)
                return false;

            if(other.questItems.equals(questItems) == false)
                return false;

            if(other.questPoints != questPoints)
                return false;

            if(other.questPointsReq != questPointsReq)

            if(other.redoDelay != redoDelay)
                return false;


            if(other.stages.equals(stages) == false)
                return false;

        }

        return true;

    }

}
