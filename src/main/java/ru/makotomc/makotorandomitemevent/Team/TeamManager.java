package ru.makotomc.makotorandomitemevent.Team;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import ru.makotomc.makotorandomitemevent.GameMode;
import ru.makotomc.makotorandomitemevent.Party.Party;
import ru.makotomc.makotorandomitemevent.Party.PartyManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TeamManager implements Listener {
    public static List<Team> teams = new LinkedList<>();
    public static Team getTeamByPlayer(Player player){
        return teams.stream().filter(
                team -> team.members.contains(player)
        ).findFirst().orElse(null);
    }
    public static List<Player> getAllPlayers(){
        List<Player> allPlayers = new LinkedList<>();
        for (Team team : teams) {
            allPlayers.addAll(team.members);
        }
        return allPlayers;
    }

    public static List<Team> registerDeath(Player player){
        Team team = getTeamByPlayer(player);
        if(team!=null){
            teams.remove(team);
            if(team.members.size()>1) {
                team.removeMember(player);
                teams.add(team);
            }
        }
        applySymbols();
        return teams;
    }
    public static List<Team> generateTeams(){
        teams.clear();
        if(GameMode.CURRENT_GAME_MODE == GameMode.SOLO){
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                teams.add(new Team(List.of(onlinePlayer)));
            }
        }else{
            List<Party> parties = PartyManager.parties;
            parties.removeIf(party -> party.getMembers().size()>GameMode.CURRENT_GAME_MODE.playerCount||party.getMembers().size()<=1);
            List<Player> notInParty = new LinkedList<>(Bukkit.getOnlinePlayers());
            notInParty.removeAll(parties.stream().flatMap(party -> party.getMembers().stream()).collect(Collectors.toList()));
            Collections.shuffle(notInParty);
            for (Party party : parties) {
                List<Player> team = new LinkedList<>();
                for (Player player : party.getMembers()) {
                    team.add(player);
                    if(team.size()==GameMode.CURRENT_GAME_MODE.playerCount){
                        teams.add(new Team(team));
                        break;
                    }
                }
                if(!notInParty.isEmpty()&&team.size()<GameMode.CURRENT_GAME_MODE.playerCount){
                    for (Player player : notInParty) {
                        team.add(player);
                        if(team.size()==GameMode.CURRENT_GAME_MODE.playerCount){
                            teams.add(new Team(team));
                            break;
                        }
                    }
                    notInParty.removeAll(team);
                }
            }
            if(!notInParty.isEmpty()){
                List<Player> team = new LinkedList<>();
                for (Player player : notInParty) {
                    team.add(player);
                    if(team.size()==GameMode.CURRENT_GAME_MODE.playerCount){
                        teams.add(new Team(team));
                        team.clear();
                    }
                }
                if(!team.isEmpty())
                    teams.add(new Team(team));
            }
        }
        Team.reset();
        applySymbols();
        return teams;
    }
    public static void applySymbols(){
        List<Player> allPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        for (Team team : teams) {
            for (Player player : team.members) {
                allPlayers.remove(player);
                player.setPlayerListName(team.symbol+" "+player.getDisplayName());
            }
        }
        for (Player player : allPlayers) {
            player.setPlayerListName(player.getDisplayName());
        }
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Player))
            return;
        if(!(event.getEntity() instanceof Player))
            return;
        Player damager = (Player) event.getDamager();
        Player player = (Player) event.getEntity();
        Team team = getTeamByPlayer(player);
        if(team!=null){
            if(team.members.contains(damager)){
                event.setCancelled(true);
            }
        }
    }
}
