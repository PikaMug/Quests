package me.blackvein.quests;

import java.util.LinkedList;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static Quests plugin = null;

    //Format -  id-id:amount-amount:data-data:enchantment-enchantment level:name-name:lore-lore:
    public static ItemStack parseItem(String s){

        String[] args = s.split(":");
        ItemStack stack = null;
        LinkedList<String> lore = new LinkedList<String>();

        for(String arg : args){

            if(arg.startsWith("id-"))
                stack = new ItemStack(Integer.parseInt(arg.substring(3)));
            else if(arg.startsWith("amount-"))
                stack.setAmount(Integer.parseInt(arg.substring(7)));
            else if(arg.startsWith("data-"))
                stack.setDurability(Short.parseShort(arg.substring(5)));
            else if(arg.startsWith("enchantment-")){
                String[] ench = arg.substring(12).split(" ");
                Enchantment e = Quests.getEnchantment(ench[0]);
                stack.addEnchantment(e, Integer.parseInt(ench[1]));
            }else if(arg.startsWith("name-"))
                stack.getItemMeta().setDisplayName(arg.substring(5));
            else if(arg.startsWith("lore-"))
                lore.add(arg.substring(5));


        }

        if(lore.isEmpty() == false)
            stack.getItemMeta().setLore(lore);

        return stack;

    }

}
