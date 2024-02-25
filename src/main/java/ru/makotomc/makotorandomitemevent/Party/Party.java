package ru.makotomc.makotorandomitemevent.Party;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Party {
    private Player leader;
    private List<Player> members = new LinkedList<>();
    public Party(Player leader){
        this.leader = leader;
        members.add(leader);
        PartyManager.parties.add(this);
    }
    public void addMember(Player player){
        members.add(player);
    }
    public void removeMember(Player player){
        members.remove(player);
    }
    public void removeMember(String player){
        members.removeIf(member -> member.getName().equals(player));
    }
}
