package ru.makotomc.makotorandomitemevent.Party;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.LinkedList;
import java.util.List;

public class PartyManager implements Listener {
    public static List<Party> parties = new LinkedList<>();
    public static Party getPartyByLeader(Player player) {
        return parties.stream().filter(
                party -> party.getLeader().equals(player)
        ).findFirst().orElse(null);
    }
    public static Party getPartyByMember(Player player) {
        return parties.stream().filter(
                party -> party.getMembers().contains(player)
        ).findFirst().orElse(null);
    }
    public static void disbandParty(Party party) {
        parties.remove(party);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Party party = getPartyByMember(player);
        if (party != null) {
            String msg;
            if(party.getLeader().equals(player)){
                msg = "Ваша команда была расформирована после выхода администратора!";
                disbandParty(party);
            }else {
                msg = "Вашу команду покинул игрок " + player.getName();
                party.removeMember(player);
            }
            for (Player member : party.getMembers()) {
                if(member.isOnline())
                    PartyCommand.sendMessage(member, msg);
            }
        }
    }



}
