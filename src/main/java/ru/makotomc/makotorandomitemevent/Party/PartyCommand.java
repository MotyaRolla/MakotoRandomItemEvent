package ru.makotomc.makotorandomitemevent.Party;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.makotomc.makotorandomitemevent.GameMode;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class Invite{
    public static List<Invite> invites = new LinkedList<>();
    public static Invite getInviteByPlayer(Player player){
        invites.removeIf(invite -> invite.expiredAt<System.currentTimeMillis());
        return invites.stream().filter(
                invite -> invite.invited.equals(player)
        ).findFirst().orElse(null);
    }
    public void removeInvite(){
        invites.remove(this);
    }
    Player invited;
    final Player inviter;
    private final long expiredAt = System.currentTimeMillis()+2*60*1000;
    public Invite(Player player, Player inviter){
        this.invited = player;
        this.inviter = inviter;
        invites.add(this);
    }
}
public class PartyCommand implements CommandExecutor {
    public static void sendMessage(Player player, String message){
        player.sendMessage(
                Component.text("P ").color(TextColor.color(Color.orange.getRGB()))
                        .append(Component.text(message).color(TextColor.color(Color.lightGray.getRGB())))
        );
    }
    public static String getInfo(Party party){
        return  "Ты в команде с " +
                party.getMembers().stream().map(Player::getName).collect(Collectors.joining(", "))+
                ". Администратор команды: "+party.getLeader().getName();
    }
    public static void sendInfo(Player p){
        Party party = PartyManager.getPartyByMember(p);
        if(party==null){
            sendMessage(p, "Ты не состоишь в группе! /party help");
        }else{
            sendMessage(p, getInfo(party));
        }
    }


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player p = (Player) commandSender;
        if(strings.length==0){
            sendInfo(p);
            return true;
        }
        switch (strings[0]){
            case "help" -> {
                sendMessage(p, "/party invite <игрок> - пригласить игрока в группу" +
                        "\n/party leave - покинуть группу" +
                        "\n/party kick <игрок> - выгнать игрока из группы" +
                        "\n/party info - информация о группе" +
                        "\n/party accept - принять приглашение" +
                        "\n/party decline - отклонить приглашение");
            }
            case "info" ->{
                sendInfo(p);
            }
            case "invite" ->{
                if(strings.length==1){
                    sendMessage(p, "Не указан игрок!");
                    return true;
                }
                Party party = PartyManager.getPartyByMember(p);
                if(party==null){
                    party = new Party(p);
                }else {
                    if (!party.getLeader().equals(p)) {
                        sendMessage(p, "Ты не администратор группы!");
                        return true;
                    }
                    if (party.getMembers().stream().map(Player::getName).collect(Collectors.toList()).contains(strings[1])) {
                        sendMessage(p, "Такой игрок уже в группе!");
                        return true;
                    }
                }
                if(party.getMembers().size()>=GameMode.CURRENT_GAME_MODE.playerCount){
                    sendMessage(p, "Группа уже полна!");
                    return true;
                }
                Player invited = Bukkit.getPlayer(strings[1]);
                if(invited==null){
                    sendMessage(p, "Такого игрока нет в сети!");
                    return true;
                }
                new Invite(invited, p);
                sendMessage(p, "Игрок "+invited.getName()+" приглашен в твою группу!");
                sendMessage(invited, "Игрок "+p.getName()+" пригласил тебя в группу!");
            }
            case "leave" ->{
                Party party = PartyManager.getPartyByMember(p);
                if(party==null){
                    sendMessage(p, "Ты не состоишь в группе! /party help");
                    return true;
                }
                String msg;
                if(party.getLeader().equals(p)){
                    msg = "Группа распущена!";
                    PartyManager.disbandParty(party);
                }else{
                    msg = "Игрок "+p.getName()+" покинул группу!";
                    sendMessage(party.getLeader(), msg);
                    party.removeMember(p);
                }
                for (Player member : party.getMembers())
                    if(member.isOnline())
                        sendMessage(member, msg);
            }
            case "kick" ->{
                if(strings.length==1){
                    sendMessage(p, "Не указан игрок!");
                    return true;
                }
                Party party = PartyManager.getPartyByLeader(p);
                if(party==null){
                    sendMessage(p, "Ты не состоишь в группе или не администратор группы! /party help");
                    return true;
                }
                if(!party.getMembers().stream().map(Player::getName).collect(Collectors.toList()).contains(strings[1])){
                    sendMessage(p, "Такого игрока нет в группе!");
                    return true;
                }
                party.removeMember(strings[1]);
                sendMessage(p, "Игрок "+strings[1]+" выгнан из группы!");
                Player kicked = Bukkit.getPlayer(strings[1]);
                if(kicked!=null)
                    sendMessage(kicked, "Ты был выгнан из группы!");
            }
            case "accept" ->{
                Invite invite = Invite.getInviteByPlayer(p);
                if(invite==null){
                    sendMessage(p, "Ты не приглашен в группу!");
                    return true;
                }
                Party invitedTo = PartyManager.getPartyByLeader(invite.inviter);
                if(invitedTo==null){
                    sendMessage(p, "Команды более не существует");
                    invite.removeInvite();
                    return true;
                }
                if(invitedTo.getMembers().size()>= GameMode.CURRENT_GAME_MODE.playerCount){
                    sendMessage(p, "Группа переполнена!");
                    invite.removeInvite();
                    return true;
                }
                Party party = PartyManager.getPartyByMember(p);
                if(party!=null&&party.getMembers().size()>1){
                    sendMessage(p, "Ты уже состоишь в группе");
                    invite.removeInvite();
                    return true;
                }else{
                    PartyManager.disbandParty(party);
                }
                invitedTo.addMember(p);
                invite.removeInvite();
                sendMessage(p, "Вы приняли приглашение в группу!");
                if (invite.inviter.isOnline()) {
                    sendMessage(invite.inviter, "Игрок " + p.getName() + " принял приглашение в группу!");
                }
            }
            case "decline" ->{
                Invite invite = Invite.getInviteByPlayer(p);
                if(invite==null){
                    sendMessage(p, "Ты не приглашен в группу!");
                    return true;
                }
                invite.removeInvite();
                sendMessage(p, "Ты отклонил приглашение в группу!");
            }
        }
        return true;
    }
}
