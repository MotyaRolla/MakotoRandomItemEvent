package ru.makotomc.makotorandomitemevent.Gamemodes;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.makotomc.makotorandomitemevent.MakotoRandomItemEvent;
import ru.makotomc.makotorandomitemevent.Team.Team;
import ru.makotomc.makotorandomitemevent.Team.TeamManager;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ru.makotomc.makotorandomitemevent.Team.TeamManager.teams;

@Getter
public class BasicGamemode implements Gamemode {
    private int heightLimit = 256;
    private Location wbCenter;
    private int wbSize;

    public int getItemCooldown() {
        return 14;
    }
    public int getMaximumHeight() {
        return 10;
    }
    public int getCurrentHeightLimit() {
        return heightLimit;
    }

    @Override
    public int getDuration() {
        return 28;
    }


    @Override
    public String getName() {
        return "Обычный";
    }

    public Set<Location> createCircle(Location center, int radius, int blockCount, Material material) {
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

    @Override
    public boolean isEventRunning() {
        return stopCycle != null;
    }

    public void stopCycle(){
        stopCycle.run();
        stopCycle = null;
    }
    public Set<Location> generateArena(Location loc){
        List<Player> players = TeamManager.getAllPlayers();
        int playerCount = players.size();
        Set<Location> locs = createCircle(loc, (int) (2.44 * playerCount), teams.size(), Material.OBSIDIAN);
        int size = playerCount;
        while (size > 9) {
            size -= 6;
            createCircle(loc, (int) (size * 2.44), size * (size < 10 ? 4 : 2), Material.OBSIDIAN);
        }
        return locs;
    }

    @Override
    public void onTeleportTeam(Team team, Location loc) {

    }

    public void startEvent(Location loc) {
        onStart();
        List<Team> teams = TeamManager.generateTeams();
        List<Player> players = TeamManager.getAllPlayers();
        stopCycle = startCycle();

        Set<Location> locs = generateArena(loc);

        Collections.shuffle(players);
        int i = 0;
        for (Location location : locs) {
            try {
                Team team = teams.get(i);
                Location standOn = location.clone();
                teleportTeam(team, standOn);
            } catch (Exception ignored) {
                // ignored
            }
            i++;
        }
        setBorder(loc);
    }


    public void teleportTeam(Team team, Location standOn) {
        onTeleportTeam(team, standOn);
        for (Player participant : team.members) {
            standOn.getBlock().setType(Material.OBSIDIAN);
            if (participant.isValid()) {
                Location tmp = standOn.clone().add(0, 2, 0);
                participant.setGameMode(GameMode.SURVIVAL);
                participant.getInventory().clear();
                participant.setHealth(20);
                participant.setSaturation(20);
                participant.teleport(tmp);
            }
            standOn.add(1, 0, 0);
        }
    }

    @Override
    public void onGiveItem(ItemStack is, Player p) {

    }

    public void setBorder(Location center){
        WorldBorder wb = center.getWorld().getWorldBorder();
        wbCenter = wb.getCenter();
        wbSize = (int) wb.getSize();
        wb.setCenter(center.clone());
        int size = TeamManager.getAllPlayers().size();
        wb.setSize(size * 4 >= 200 ? size * 6 : 200);
        wb.setSize(2, TimeUnit.MINUTES, getDuration());
        heightLimit = center.getBlockY() +getMaximumHeight();
    }
    public void registerJoin(Player p){
        p.setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void onStart() {

    }

    public void stopEvent(String winner){
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showTitle(
                    Title.title(
                            Component.text("Ивент окончен").color(TextColor.color(java.awt.Color.pink.getRGB())),
                            Component.text("Победитель: "+winner).color(TextColor.color(java.awt.Color.lightGray.getRGB())),
                            Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofSeconds(2))
                    )
            );
        }
        stopCycle();
        for (Player allPlayer : TeamManager.getAllPlayers()) {
            allPlayer.setGameMode(org.bukkit.GameMode.SPECTATOR);
        }
        teams.clear();
        TeamManager.applySymbols();
        heightLimit=256;
        if(wbCenter!=null) {
            WorldBorder wb = wbCenter.getWorld().getWorldBorder();
            wb.setCenter(wbCenter);
            wb.setSize(wbSize);
        }
    }
    public void registerDeath(Player p){
        if (!TeamManager.getAllPlayers().contains(p))
            return;
        p.getWorld().strikeLightningEffect(p.getLocation());
        if(TeamManager.registerDeath(p).size()<2){
            stopEvent("Команда "+ teams.get(0).symbol);
        }
        p.setGameMode(org.bukkit.GameMode.SPECTATOR);
    }
    private Runnable stopCycle;
    public Runnable startCycle(){
        int x = Bukkit.getScheduler().scheduleSyncRepeatingTask(MakotoRandomItemEvent.getInstance(), () -> {
            if(teams.isEmpty())
                return;

            for (Player player : TeamManager.getAllPlayers()) {
                ItemStack is = getRandomItem();
                player.getInventory().addItem(getRandomItem());
                onGiveItem(is,player);
                player.setLevel(getItemCooldown()+1);
            }
        },getItemCooldown()* 20L,getItemCooldown()* 20L);
        int z = Bukkit.getScheduler().scheduleSyncRepeatingTask(MakotoRandomItemEvent.getInstance(), () -> {
            if(teams.isEmpty())
                return;
            for (Player player : TeamManager.getAllPlayers()) {
                player.setLevel(player.getLevel()==0?0:player.getLevel()-1);
            }
        },getItemCooldown()* 20L,20);
        return () -> {
            Bukkit.getScheduler().cancelTask(x);
            Bukkit.getScheduler().cancelTask(z);
        };
    }

    public ItemStack getRandomItem(){
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
