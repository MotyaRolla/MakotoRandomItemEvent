package ru.makotomc.makotorandomitemevent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.makotomc.makotorandomitemevent.Party.PartyCommand;
import ru.makotomc.makotorandomitemevent.Party.PartyManager;
import ru.makotomc.makotorandomitemevent.Team.Team;
import ru.makotomc.makotorandomitemevent.Team.TeamManager;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class MakotoRandomItemEvent extends JavaPlugin implements Listener, CommandExecutor {
    private static Location wbCenter;
    private static double wbSize;
    private static int heightLimit = 256;

//    public static Set<Player> participants = new HashSet<>();
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
        List<Team> teams = TeamManager.generateTeams();
        List<Player> players =TeamManager.getAllPlayers();

        int playerCount = players.size();
        Set<Location> locs = createCircle(loc, (int) (2.44 * playerCount), teams.size(), Material.OBSIDIAN);
        int size = playerCount;
        while (size > 9) {
            size -= 6;
            createCircle(loc, (int) (size * 2.44), size * (size < 10 ? 4 : 2), Material.OBSIDIAN);
        }

        Collections.shuffle(players);
        int i = 0;
        for (Location location : locs){
            try {
                Team team = teams.get(i);
                Location standOn = location.clone();
                for(Player participant : team.members) {
                    standOn.getBlock().setType(Material.OBSIDIAN);
                    if (participant.isValid()) {
                        Location tmp = standOn.clone().add(0, 2, 0);
                        participant.setGameMode(org.bukkit.GameMode.SURVIVAL);
                        participant.getInventory().clear();
                        participant.setHealth(20);
                        participant.setSaturation(20);
                        participant.teleport(tmp);
                    }
                    standOn.add(1,0, 0);
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
        heightLimit = loc.getBlockY()+10;
    }
    public static void stopEvent(String winner){
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showTitle(
                    Title.title(
                            Component.text("Ивент окончен").color(TextColor.color(java.awt.Color.pink.getRGB())),
                            Component.text("Победитель: "+winner).color(TextColor.color(java.awt.Color.lightGray.getRGB())),
                            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofSeconds(2))
                    )
            );

        }
        TeamManager.teams.clear();
        TeamManager.applySymbols();
        WorldBorder wb = wbCenter.getWorld().getWorldBorder();
        wb.setCenter(wbCenter);
        wb.setSize(wbSize);
        heightLimit=256;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        if(e.getBlock().getY()>=heightLimit) {
            e.getPlayer().sendMessage(ChatColor.GRAY+"Строительство выше запрещено!");
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!TeamManager.getAllPlayers().contains(e.getPlayer()))
            return;
        e.getPlayer().setGameMode(org.bukkit.GameMode.SPECTATOR);
        e.getPlayer().getWorld().strikeLightningEffect(e.getPlayer().getLocation());
        if(TeamManager.registerDeath(e.getPlayer()).size()==1){
            stopEvent(TeamManager.getAllPlayers().get(0).getName());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        e.getPlayer().setGameMode(GameMode.SPECTATOR);
    }
    @EventHandler
    public void onDisconnect(PlayerQuitEvent e){
        if(TeamManager.registerDeath(e.getPlayer()).size()==1){
            stopEvent(TeamManager.getAllPlayers().get(0).getName());
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new PartyManager(), this);
        Bukkit.getPluginManager().registerEvents(new TeamManager(), this);
        Bukkit.getPluginCommand("revent").setExecutor(new ReventCommand());
        Bukkit.getPluginCommand("party").setExecutor(new PartyCommand());

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(TeamManager.teams.isEmpty())
                return;

            for (Player player : TeamManager.getAllPlayers()) {
                player.getInventory().addItem(getRandomItem());
                player.setLevel(15);
            }
        },14*20,14*20);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(TeamManager.teams.isEmpty())
                return;
            for (Player player : TeamManager.getAllPlayers()) {
                player.setLevel(player.getLevel()==0?0:player.getLevel()-1);
            }
        },14*20,20);
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
            if(!(is.getType().name().contains("POTTERY_SHERD")||
                    is.getType().name().contains("ARMOR_TRIM")||
                    is.getType().name().contains("_CORAL")||
                    is.getType().name().contains("CANDLE")||
                    is.getType().name().contains("WITHER"))){

                while (Math.random()>0.94){
                    is.addUnsafeEnchantment(
                            Enchantment.values()[(int) (Math.random() * (Enchantment.values().length-1))],
                            (int) (Math.random()*4+1)
                    );
                }
                return is;
            }
        }
        return is;
    }
}
