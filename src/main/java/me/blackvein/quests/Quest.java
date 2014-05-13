package me.blackvein.quests;

import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.codisimus.plugins.phatloots.loot.CommandLoot;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.util.player.UserManager;
import com.herocraftonline.heroes.characters.Hero;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.blackvein.quests.exceptions.InvalidStageException;
import me.blackvein.quests.util.ItemUtil;
import me.blackvein.quests.util.Lang;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

public class Quest {

    public String name;
    public String description;
    public String finished;
    public long redoDelay = -1;
    public String region = null;
    public ItemStack guiDisplay = null;
    public int parties = 0;
    LinkedList<Stage> orderedStages = new LinkedList<Stage>();
    NPC npcStart;
    Location blockStart;
    Quests plugin;
    Event initialEvent;
    
    //Requirements
    int moneyReq = 0;
    int questPointsReq = 0;
    List<ItemStack> items = new LinkedList<ItemStack>();
    List<Boolean> removeItems = new LinkedList<Boolean>();
    List<String> neededQuests = new LinkedList<String>();
    List<String> blockQuests = new LinkedList<String>();
    List<String> permissionReqs = new LinkedList<String>();
    List<String> mcMMOSkillReqs = new LinkedList<String>();
    List<Integer> mcMMOAmountReqs = new LinkedList<Integer>();
    String heroesPrimaryClassReq = null;
    String heroesSecondaryClassReq = null;
    Map<String, Map<String, Object>> customRequirements = new HashMap<String, Map<String, Object>>();
    Map<String, Map<String, Object>> customRewards = new HashMap<String, Map<String, Object>>();
    
    public String failRequirements = null;
    //
    
    //Rewards
    int moneyReward = 0;
    int questPoints = 0;
    int exp = 0;
    List<String> commands = new LinkedList<String>();
    List<String> permissions = new LinkedList<String>();
    LinkedList<ItemStack> itemRewards = new LinkedList<ItemStack>();
    LinkedList<Integer> rpgItemRewardIDs = new LinkedList<Integer>();
    LinkedList<Integer> rpgItemRewardAmounts = new LinkedList<Integer>();
    List<String> mcmmoSkills = new LinkedList<String>();
    List<Integer> mcmmoAmounts = new LinkedList<Integer>();
    List<String> heroesClasses = new LinkedList<String>();
    List<Double> heroesAmounts = new LinkedList<Double>();
    List<String> phatLootRewards = new LinkedList<String>();
    //
    
    public void nextStage(Quester q) {

        String stageCompleteMessage = q.currentStage.completeMessage;
        if (stageCompleteMessage != null) {
            q.getPlayer().sendMessage(Quests.parseString(stageCompleteMessage, q.currentQuest));
        }

        if (q.currentStage.delay < 0) {

            Player player = q.getPlayer();
            if (q.currentStageIndex == (q.currentQuest.orderedStages.size() - 1)) {

                if (q.currentStage.script != null) {
                    plugin.trigger.parseQuestTaskTrigger(q.currentStage.script, player);
                }
                if (q.currentStage.finishEvent != null) {
                    q.currentStage.finishEvent.fire(q);
                }

                completeQuest(q);

            } else {

                q.currentStageIndex++;
                try {
                    setStage(q, q.currentStageIndex);
                } catch (InvalidStageException e) {
                    e.printStackTrace();
                }

            }

            q.delayStartTime = 0;
            q.delayTimeLeft = -1;

        } else {
            q.startStageTimer();

        }

    }

    public void setStage(Quester quester, int stage) throws InvalidStageException {

    	quester.currentStageIndex = stage;
    	
        if (orderedStages.size() - 1 < stage) {
        	throw new InvalidStageException(this, stage);
        }

        quester.resetObjectives();

        if (quester.currentStage.script != null) {
            plugin.trigger.parseQuestTaskTrigger(quester.currentStage.script, quester.getPlayer());
        }

        /*if (quester.currentStage.finishEvent != null) {
            quester.currentStage.finishEvent.fire(quester);
        }*/

        quester.currentStage = orderedStages.get(stage);

        if (quester.currentStage.startEvent != null) {
            quester.currentStage.startEvent.fire(quester);
        }

        quester.addEmpties();

        quester.getPlayer().sendMessage(ChatColor.GOLD + "---(Objectives)---");
        for (String s : quester.getObjectivesReal()) {

            quester.getPlayer().sendMessage(s);

        }

        String stageStartMessage = quester.currentStage.startMessage;
        if (stageStartMessage != null) {
            quester.getPlayer().sendMessage(Quests.parseString(stageStartMessage, quester.currentQuest));
        }

    }

    public String getName() {
        return name;
    }

    public boolean testRequirements(Quester quester) {
        return testRequirements(quester.getPlayer());
    }

    public boolean testRequirements(Player player) {

        Quester quester = plugin.getQuester(player.getName());

        if (moneyReq != 0 && Quests.economy.getBalance(player.getName()) < moneyReq) {
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        int num = 0;

        for (ItemStack is : items) {

            for (ItemStack stack : inventory.getContents()) {

                if (stack != null) {
                    if (ItemUtil.compareItems(is, stack, true) == 0) {
                        num += stack.getAmount();
                    }
                }

            }

            if (num < is.getAmount()) {
                return false;
            }

            num = 0;

        }

        for (String s : permissionReqs) {

            if (player.hasPermission(s) == false) {
                return false;
            }

        }

        for (String s : mcMMOSkillReqs) {

            final SkillType st = Quests.getMcMMOSkill(s);
            final int lvl = mcMMOAmountReqs.get(mcMMOSkillReqs.indexOf(s));
            if (UserManager.getPlayer(player).getProfile().getSkillLevel(st) < lvl) {
                return false;
            }

        }

        if (heroesPrimaryClassReq != null) {

            if (plugin.testPrimaryHeroesClass(heroesPrimaryClassReq, player.getName()) == false) {
                return false;
            }

        }

        if (heroesSecondaryClassReq != null) {

            if (plugin.testSecondaryHeroesClass(heroesSecondaryClassReq, player.getName()) == false) {
                return false;
            }

        }
        
        for (String s : customRequirements.keySet()){
            
            CustomRequirement found = null;
            for(CustomRequirement cr : plugin.customRequirements){
                if(cr.getName().equalsIgnoreCase(s)){
                    found = cr;
                    break;
                }
            }
            
            if(found != null){
                if(found.testRequirement(player, customRequirements.get(s)) == false)
                    return false;
            }else{
                Quests.printWarning("[Quests] Quester \"" + player.getName() + "\" attempted to take Quest \"" + name + "\", but the Custom Requirement \"" + s + "\" could not be found. Does it still exist?");
            }
            
        }

        if (quester.questPoints < questPointsReq) {
            return false;
        }

        if (quester.completedQuests.containsAll(neededQuests) == false) {
            return false;
        }

        for (String q : blockQuests) {
            if (quester.completedQuests.contains(q)) {
                return false;
            }
        }

        return true;

    }

    public void completeQuest(Quester q) {

        Player player = plugin.getServer().getPlayerExact(q.name);
        q.resetObjectives();
        q.completedQuests.add(name);
        String none = ChatColor.GRAY + "- (None)";

        String ps = Quests.parseString(finished, q.currentQuest);

        for (String msg : ps.split("<br>")) {
            player.sendMessage(msg);
        }

        if (moneyReward > 0 && Quests.economy != null) {
            Quests.economy.depositPlayer(q.name, moneyReward);
            none = null;
        }
        if (redoDelay > -1) {
            q.completedTimes.put(this.name, System.currentTimeMillis());
        }

        for (ItemStack i : itemRewards) {
            Quests.addItem(player, i);
            none = null;
        }

        for (Integer i : rpgItemRewardIDs) {
            ItemStack is = ItemManager.getItemById(i).toItemStack(null);
            is.setAmount(rpgItemRewardAmounts.get(rpgItemRewardIDs.indexOf(i)));
            Quests.addItem(player, is);
            none = null;
        }

        for (String s : commands) {

            s = s.replaceAll("<player>", player.getName());

            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), s);
            none = null;

        }

        for (String s : permissions) {

            Quests.permission.playerAdd(player, s);
            none = null;

        }

        for (String s : mcmmoSkills) {

            UserManager.getPlayer(player).getProfile().addLevels(Quests.getMcMMOSkill(s), mcmmoAmounts.get(mcmmoSkills.indexOf(s)));
            none = null;

        }

        for (String s : heroesClasses) {

            Hero hero = plugin.getHero(player.getName());
            hero.addExp(heroesAmounts.get(heroesClasses.indexOf(s)), Quests.heroes.getClassManager().getClass(s), player.getLocation());
            none = null;

        }

        LinkedList<ItemStack> phatLootItems = new LinkedList<ItemStack>();
        int phatLootExp = 0;
        int phatLootMoney = 0;

        LinkedList<String> phatLootMessages = new LinkedList<String>();

        for(String s : phatLootRewards) {

            LootBundle lb = PhatLootsAPI.getPhatLoot(s).rollForLoot();

            if(lb.getExp() > 0){
                phatLootExp += lb.getExp();
                player.giveExp(lb.getExp());
            }

            if(lb.getMoney() > 0){
                phatLootMoney += lb.getMoney();
                Quests.economy.depositPlayer(player.getName(), lb.getMoney());
            }

            if(lb.getItemList().isEmpty() == false){
                phatLootItems.addAll(lb.getItemList());
                for(ItemStack is : lb.getItemList())
                    Quests.addItem(player, is);
            }

            if(lb.getCommandList().isEmpty() == false){
                for(CommandLoot cl : lb.getCommandList())
                    cl.execute(player);
            }

            if(lb.getMessageList().isEmpty() == false)
                phatLootMessages.addAll(lb.getMessageList());

        }

        if (exp > 0) {
            player.giveExp(exp);
            none = null;
        }

        player.sendMessage(ChatColor.GOLD + "**QUEST COMPLETE: " + ChatColor.YELLOW + q.currentQuest.name + ChatColor.GOLD + "**");
        player.sendMessage(ChatColor.GREEN + "Rewards:");

        if (questPoints > 0) {
            player.sendMessage("- " + ChatColor.DARK_GREEN + questPoints + " Quest Points");
            q.questPoints += questPoints;
            none = null;
        }

        for (Integer i : rpgItemRewardIDs) {

            RPGItem item = ItemManager.getItemById(i);
            player.sendMessage("- " + ChatColor.LIGHT_PURPLE + "- " + ChatColor.ITALIC + item.getName() + ChatColor.GRAY + " x " + rpgItemRewardAmounts.get(rpgItemRewardIDs.indexOf(i)));

        }

        for (ItemStack i : itemRewards) {

            if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
                
                if(i.getEnchantments().isEmpty())
                    player.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount());
                else
                    player.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " *Enchanted*");
            
            } else if (i.getDurability() != 0) {
                
                if(i.getEnchantments().isEmpty())
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ":" + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount());
                else
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ":" + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " *Enchanted*");
            
            } else {
                
                if(i.getEnchantments().isEmpty())
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ChatColor.GRAY + " x " + i.getAmount());
                else
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " *Enchanted*");
                
            }

            none = null;
        }

        for (ItemStack i : phatLootItems) {

            if (i.hasItemMeta() && i.getItemMeta().hasDisplayName()) {
                
                if(i.getEnchantments().isEmpty())
                    player.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount());
                else
                    player.sendMessage("- " + ChatColor.DARK_AQUA + ChatColor.ITALIC + i.getItemMeta().getDisplayName() + ChatColor.RESET + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " *Enchanted*");
            
            } else if (i.getDurability() != 0) {
                
                if(i.getEnchantments().isEmpty())
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ":" + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount());
                else
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ":" + i.getDurability() + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " *Enchanted*");
            
            } else {
                
                if(i.getEnchantments().isEmpty())
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ChatColor.GRAY + " x " + i.getAmount());
                else
                    player.sendMessage("- " + ChatColor.DARK_GREEN + Quester.prettyItemString(i.getTypeId()) + ChatColor.GRAY + " x " + i.getAmount() + ChatColor.DARK_PURPLE + " *Enchanted*");
                
            }

            none = null;
        }

        if (moneyReward > 1) {
            player.sendMessage("- " + ChatColor.DARK_GREEN + moneyReward + " " + ChatColor.DARK_PURPLE + Quests.getCurrency(true));
            none = null;
        } else if (moneyReward == 1) {
            player.sendMessage("- " + ChatColor.DARK_GREEN + moneyReward + " " + ChatColor.DARK_PURPLE + Quests.getCurrency(false));
            none = null;
        }

        if (exp > 0 || phatLootExp > 0) {

            int tot = exp + phatLootExp;
            player.sendMessage("- " + ChatColor.DARK_GREEN + tot + ChatColor.DARK_PURPLE + " Experience");
            none = null;
        }

        if (mcmmoSkills.isEmpty() == false) {
            for (String s : mcmmoSkills) {
                player.sendMessage("- " + ChatColor.DARK_GREEN + mcmmoAmounts.get(mcmmoSkills.indexOf(s)) + " " + ChatColor.DARK_PURPLE + s + " Experience");
            }
            none = null;
        }

        if (heroesClasses.isEmpty() == false) {
            for (String s : heroesClasses) {
                player.sendMessage("- " + ChatColor.AQUA + heroesAmounts.get(heroesClasses.indexOf(s)) + " " + ChatColor.BLUE + s + " Experience");
            }
            none = null;
        }

        if(phatLootMessages.isEmpty() == false) {
            for (String s : phatLootMessages){
                player.sendMessage("- " + s);
            }
            none = null;
        }
        
        for (String s : customRewards.keySet()){
            
            CustomReward found = null;
            for(CustomReward cr : plugin.customRewards){
                if(cr.getName().equalsIgnoreCase(s)){
                    found = cr;
                    break;
                }
            }
            
            if(found != null){
                Map<String, Object> datamap = customRewards.get(found.getName());
                String message = found.getRewardName();
                
                for(String key : datamap.keySet()){
                    message = message.replaceAll("%" + ((String) key) + "%", ((String) datamap.get(key)));
                }
                player.sendMessage("- " + ChatColor.GOLD + found.getRewardName());
                found.giveReward(player, customRewards.get(s));
            }else{
                Quests.printWarning("[Quests] Quester \"" + player.getName() + "\" completed the Quest \"" + name + "\", but the Custom Reward \"" + s + "\" could not be found. Does it still exist?");
            }
            
            none = null;
            
        }

        if (none != null) {
            player.sendMessage(none);
        }

        q.currentQuest = null;

        q.currentStage = null;
        q.currentStageIndex = 0;

        q.saveData();
        player.updateInventory();

    }

    public void failQuest(Quester q) {

        Player player = plugin.getServer().getPlayerExact(q.name);
        q.resetObjectives();

        player.sendMessage(ChatColor.AQUA + "-- " + ChatColor.DARK_PURPLE + q.currentQuest.name + ChatColor.AQUA + " -- ");
        player.sendMessage(ChatColor.RED + Lang.get("questFailed"));

        q.currentQuest = null;

        q.currentStage = null;
        q.currentStageIndex = 0;

        q.saveData();
        player.updateInventory();

    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof Quest) {

            Quest other = (Quest) o;

            if (other.blockStart != null && blockStart != null) {
                if (other.blockStart.equals(blockStart) == false) {
                    return false;
                }
            } else if (other.blockStart != null && blockStart == null) {
                return false;
            } else if (other.blockStart == null && blockStart != null) {
                return false;
            }

            if (commands.size() == other.commands.size()) {

                for (int i = 0; i < commands.size(); i++) {
                    if (commands.get(i).equals(other.commands.get(i)) == false) {
                        return false;
                    }
                }

            } else {
                return false;
            }

            if (other.description.equals(description) == false) {
                return false;
            }

            if (other.initialEvent != null && initialEvent != null) {
                if (other.initialEvent.equals(initialEvent) == false) {
                    return false;
                }
            } else if (other.initialEvent != null && initialEvent == null) {
                return false;
            } else if (other.initialEvent == null && initialEvent != null) {
                return false;
            }

            if (other.exp != exp) {
                return false;
            }

            if (other.failRequirements != null && failRequirements != null) {
                if (other.failRequirements.equals(failRequirements) == false) {
                    return false;
                }
            } else if (other.failRequirements != null && failRequirements == null) {
                return false;
            } else if (other.failRequirements == null && failRequirements != null) {
                return false;
            }

            if (other.finished.equals(finished) == false) {
                return false;
            }

            if (other.items.equals(items) == false) {
                return false;
            }

            if (other.itemRewards.equals(itemRewards) == false) {
                return false;
            }

            if (other.rpgItemRewardIDs.equals(rpgItemRewardIDs) == false) {
                return false;
            }

            if (other.rpgItemRewardAmounts.equals(rpgItemRewardAmounts) == false) {
                return false;
            }

            if (other.mcmmoAmounts.equals(mcmmoAmounts) == false) {
                return false;
            }

            if (other.mcmmoSkills.equals(mcmmoSkills) == false) {
                return false;
            }

            if (other.heroesClasses.equals(heroesClasses) == false) {
                return false;
            }

            if (other.heroesAmounts.equals(heroesAmounts) == false) {
                return false;
            }

            if(other.phatLootRewards.equals(phatLootRewards) == false) {
                return false;
            }

            if (other.moneyReq != moneyReq) {
                return false;
            }

            if (other.moneyReward != moneyReward) {
                return false;
            }

            if (other.name.equals(name) == false) {
                return false;
            }

            if (other.neededQuests.equals(neededQuests) == false) {
                return false;
            }

            if (other.blockQuests.equals(blockQuests) == false) {
                return false;
            }

            if (other.npcStart != null && npcStart != null) {
                if (other.npcStart.equals(npcStart) == false) {
                    return false;
                }
            } else if (other.npcStart != null && npcStart == null) {
                return false;
            } else if (other.npcStart == null && npcStart != null) {
                return false;
            }

            if (other.permissionReqs.equals(permissionReqs) == false) {
                return false;
            }

            if (other.heroesPrimaryClassReq != null && heroesPrimaryClassReq != null) {
                if (other.heroesPrimaryClassReq.equals(heroesPrimaryClassReq) == false) {
                    return false;
                }
            } else if (other.heroesPrimaryClassReq != null && heroesPrimaryClassReq == null) {
                return false;
            } else if (other.heroesPrimaryClassReq == null && heroesPrimaryClassReq != null) {
                return false;
            }

            if (other.heroesSecondaryClassReq != null && heroesSecondaryClassReq != null) {
                if (other.heroesSecondaryClassReq.equals(heroesSecondaryClassReq) == false) {
                    return false;
                }
            } else if (other.heroesSecondaryClassReq != null && heroesSecondaryClassReq == null) {
                return false;
            } else if (other.heroesSecondaryClassReq == null && heroesSecondaryClassReq != null) {
                return false;
            }
            
            if (other.customRequirements.equals(customRequirements) == false){
                return false;
            }
            
            if (other.customRewards.equals(customRewards) == false){
                return false;
            }

            if (other.permissions.equals(permissions) == false) {
                return false;
            }

            if (other.mcMMOSkillReqs.equals(mcMMOSkillReqs) == false) {
                return false;
            }

            if (other.mcMMOAmountReqs.equals(mcMMOAmountReqs) == false) {
                return false;
            }

            if (other.questPoints != questPoints) {
                return false;
            }

            if (other.questPointsReq != questPointsReq) {
                return false;
            }

            if (other.redoDelay != redoDelay) {
                return false;
            }

            if (other.orderedStages.equals(orderedStages) == false) {
                return false;
            }

        }

        return true;

    }

    public boolean isInRegion(Player player){

        if(region == null){
            return true;
        }else{
            ApplicableRegionSet ars = Quests.worldGuard.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
            Iterator<ProtectedRegion> i = ars.iterator();
            while(i.hasNext()){

                ProtectedRegion pr = i.next();
                if(pr.getId().equalsIgnoreCase(region))
                    return true;

            }
            return false;
        }

    }

}
