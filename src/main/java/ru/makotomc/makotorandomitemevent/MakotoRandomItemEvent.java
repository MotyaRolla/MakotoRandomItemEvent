package ru.makotomc.makotorandomitemevent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class MakotoRandomItemEvent extends JavaPlugin implements Listener, CommandExecutor {
    private static Location wbCenter;
    private static double wbSize;

    public static Set<Player> participants = new HashSet<>();
    public static Set<Location> createCircle(Location center, int radius, int blockCount, Material material) {
        double angle = 2 * Math.PI / blockCount;

        Location[] blocks = new Location[blockCount];
        for (int i = 0; i < blockCount; i++) {
            double x = center.getX() + radius * Math.cos(angle * i);
            double z = center.getZ() + radius * Math.sin(angle * i);
            Location blockLocation = new Location(center.getWorld(), x, center.getY(), z);
            blocks[i] = blockLocation;
        }

        Set<Location> blockSet = new HashSet<>();
        for (Location block : blocks) {
            block.getBlock().setType(material);
            blockSet.add(block);
        }
        return blockSet;
    }
    public static void startEvent(Location loc){
        int playerCount = Bukkit.getOnlinePlayers().size();
        Set<Location> locs = createCircle(loc, (int) (2.44 * playerCount), playerCount, Material.OBSIDIAN);
        int size = playerCount;
        while (size > 9) {
            size -= 6;
            createCircle(loc, (int) (size * 2.44), size * (size < 10 ? 4 : 2), Material.OBSIDIAN);
        }
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        Collections.shuffle(players);

        int i = 0;
        for (Location location : locs){
            try {
                Player participant = players.get(i);
                if (participant.isValid()) {
                    Location tmp = location.add(0, 2, 0);
                    participant.setGameMode(GameMode.SURVIVAL);
                    participant.getInventory().clear();
                    participant.setHealth(20);
                    participant.setSaturation(20);
                    participant.teleport(tmp);
                    participants.add(participant);
                }
            } catch (Exception ignored){
                // ignored
            }
            i++;
        }
        WorldBorder wb = loc.getWorld().getWorldBorder();
        wbCenter = wb.getCenter();
        wbSize = wb.getSize();
        wb.setCenter(loc.clone());
        wb.setSize(playerCount * 4 >= 200 ? playerCount * 6 : 200);
        wb.setSize(4, TimeUnit.MINUTES, 30);
    }



    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!participants.contains(e.getPlayer()))
            return;
        participants.remove(e.getPlayer());
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
        e.getPlayer().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
        if(participants.size()==1){
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showTitle(
                        Title.title(
                                Component.text("Ивент окончен").color(TextColor.color(java.awt.Color.pink.getRGB())),
                                Component.text("Победитель: "+participants.iterator().next().getName()).color(TextColor.color(java.awt.Color.lightGray.getRGB())),
                                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofSeconds(2))
                        )
                );
            }
            participants.clear();
            WorldBorder wb = wbCenter.getWorld().getWorldBorder();
            wb.setCenter(wbCenter);
            wb.setSize(wbSize);
        }
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
    }
    @EventHandler
    public void onDisconnect(PlayerQuitEvent e){
        participants.remove(e.getPlayer());
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginCommand("revent").setExecutor(new ReventCommand());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(participants.isEmpty())
                return;

            for (Player player : participants) {
                player.getInventory().addItem(getRandomItem());
                player.setLevel(14);
            }
        },13*20,13*20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(participants.isEmpty())
                return;
            for (Player player : participants) {
                player.setLevel(player.getLevel()==0?0:player.getLevel()-1);
            }
        },13*20,20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static MakotoRandomItemEvent getInstance(){
        return getPlugin(MakotoRandomItemEvent.class);
    }
    public static ItemStack getRandomItem(){
        ItemStack is = null;
        for(int i = 0; i < 3;i++) {
            is = new ItemStack(
                    Material.values()[(int) (Math.random() * (Material.values().length-1))]
            ,1);
            if(!(is.getType().name().contains("POTTERY_SHERD")||is.getType().name().contains("ARMOR_TRIM")|| is.getType().name().contains("_CORAL")))
                return is;
        }
        return is;
    }
}
