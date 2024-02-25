package ru.makotomc.makotorandomitemevent.Team;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class Team {
    private static final List<String> symbols = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
    private static int lastSymbol = 0;
    private static int loop = 0;
    public List<Player> members = new LinkedList<>();
    public String symbol;
    public static void reset(){
        lastSymbol = 0;
        loop=0;
    }
    public Team( List<Player> members){
        symbol = symbols.get(lastSymbol)+(loop==0?"":loop);
        lastSymbol++;
        if(lastSymbol==symbols.size()){
            lastSymbol = 0;
            loop++;
        }
        this.members.addAll(members);
    }
    public void addMember(Player player){
        members.add(player);
    }
    public void removeMember(Player player){
        members.remove(player);
    }
}
