package ru.makotomc.makotorandomitemevent.Gamemodes;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CrazyRandom extends BasicGamemode {
    @Override
    public int getItemCooldown() {
        return 5;
    }

    @Override
    public String getName() {
        return "Безумный";
    }

    @Override
    public void onGiveItem(ItemStack is, Player p) {
        if(Math.random()>0.92){
            p.addPotionEffect(new PotionEffect(PotionEffectType.values()[
                    (int) (Math.random() * (PotionEffectType.values().length-1))], (int) (Math.random()*40+1), 1));
        }
        if(Math.random()>0.97){
            p.getLocation().getWorld().spawnEntity(p.getLocation().add(0,4,0), EntityType.values()[
                    (int) (Math.random() * (EntityType.values().length-1))
            ]);
        }
    }

    @Override
    public int getDuration() {
        return 9;
    }

    @Override
    public ItemStack getRandomItem() {
        ItemStack is = null;
        for(int i = 0; i < 3;i++) {
            is = new ItemStack(
                    Material.values()[(int) (Math.random() * (Material.values().length-1))]
                    ,1);
            if(!(is.getType().name().contains("POTTERY_SHERD")||
                    is.getType().name().contains("ARMOR_TRIM")||
                    is.getType().name().contains("_CORAL")||
                    is.getType().name().contains("CANDLE"))){

                while (Math.random()>0.63){
                    is.addUnsafeEnchantment(
                            Enchantment.values()[(int) (Math.random() * (Enchantment.values().length-1))],
                            (int) (Math.random()*10+1)
                    );
                }
                return is;
            }
        }
        return is;
    }
}
