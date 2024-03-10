package ru.makotomc.makotorandomitemevent;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.makotomc.makotorandomitemevent.Gamemodes.BasicGamemode;
import ru.makotomc.makotorandomitemevent.Gamemodes.Gamemode;
import ru.makotomc.makotorandomitemevent.Party.PartyCommand;
import ru.makotomc.makotorandomitemevent.Party.PartyManager;
import ru.makotomc.makotorandomitemevent.Team.TeamManager;

public final class MakotoRandomItemEvent extends JavaPlugin implements Listener, CommandExecutor {
    public static Gamemode gamemode = new BasicGamemode();

//    public static Set<Player> participants = new HashSet<>();
    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        if(e.getBlock().getY()>= gamemode.getCurrentHeightLimit()) {
            e.getPlayer().sendMessage(ChatColor.GRAY+"Строительство выше запрещено!");
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        gamemode.registerDeath(e.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        gamemode.registerJoin(e.getPlayer());
    }
    @EventHandler
    public void onDisconnect(PlayerQuitEvent e){
        gamemode.registerDeath(e.getPlayer());
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(new PartyManager(), this);
        Bukkit.getPluginManager().registerEvents(new TeamManager(), this);
        Bukkit.getPluginCommand("revent").setExecutor(new ReventCommand());
        Bukkit.getPluginCommand("party").setExecutor(new PartyCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public static MakotoRandomItemEvent getInstance(){
        return getPlugin(MakotoRandomItemEvent.class);
    }

}
