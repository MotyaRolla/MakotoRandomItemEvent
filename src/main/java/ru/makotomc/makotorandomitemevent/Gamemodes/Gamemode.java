package ru.makotomc.makotorandomitemevent.Gamemodes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.makotomc.makotorandomitemevent.Team.Team;

import java.util.Set;

public interface Gamemode {
    int getItemCooldown();
    int getMaximumHeight();
    int getDuration();
    String getName();
    int getCurrentHeightLimit();
    void registerJoin(Player p );
    void onStart();
    Set<Location> generateArena(Location loc);
    void teleportTeam(Team team, Location loc);
    void onGiveItem(ItemStack is, Player p);
    void onTeleportTeam(Team team, Location loc);
    boolean isEventRunning();

    Set<Location> createCircle(Location center, int radius, int blockCount, Material material);

    void startEvent(Location loc);
    void setBorder(Location center);
    void stopEvent(String winner);
    void registerDeath(Player p);
    Runnable startCycle();

    ItemStack getRandomItem();
}
