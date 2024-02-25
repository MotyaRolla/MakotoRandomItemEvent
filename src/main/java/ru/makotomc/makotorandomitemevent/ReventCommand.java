package ru.makotomc.makotorandomitemevent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import ru.makotomc.makotorandomitemevent.Party.Party;
import ru.makotomc.makotorandomitemevent.Party.PartyCommand;
import ru.makotomc.makotorandomitemevent.Party.PartyManager;
import ru.makotomc.makotorandomitemevent.Team.TeamManager;

import java.awt.Color;
import java.time.Duration;
import java.util.Arrays;

import static ru.makotomc.makotorandomitemevent.MakotoRandomItemEvent.*;

public class ReventCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!commandSender.isOp())
            return false;
        if(strings.length==0) {
            commandSender.sendMessage("usage: /revent start/stop");
            return true;
        }
        Player p = (Player) commandSender;
        switch (strings[0]){
            case "start":
                if(Bukkit.getOnlinePlayers().size()<=GameMode.CURRENT_GAME_MODE.playerCount){
                    p.sendMessage("Недостаточно игроков");
                    return true;
                }
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.showTitle(
                            Title.title(
                                    Component.text("Ивент начинается").color(TextColor.color(Color.PINK.getRGB())),
                                    Component.text("Арена генерируется!").color(TextColor.color(Color.gray.getRGB())),
                                    Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(4), Duration.ofSeconds(1))
                            )
                    );
                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,6*20,1,true,false));
                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,6*20,1,true,false));
                }
                TeamManager.teams.clear();
                Bukkit.getScheduler().runTaskLater(MakotoRandomItemEvent.getInstance(), () -> {
                    startEvent(p.getLocation().clone());
                },100);
                break;
            case "type":
                if(strings.length==1){
                    p.sendMessage("Тип игры: "+GameMode.CURRENT_GAME_MODE);
                    return true;
                }
                try {
                    GameMode gm= GameMode.valueOf(strings[1]);
                    GameMode.CURRENT_GAME_MODE = gm;
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.showTitle(
                                Title.title(
                                        Component.text("Изменение ивента").color(TextColor.color(Color.PINK.getRGB())),
                                        Component.text("Тип игры: "+GameMode.CURRENT_GAME_MODE.name()).color(TextColor.color(Color.gray.getRGB())),
                                        Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(7), Duration.ofSeconds(1))
                                )
                        );
                    }
                    for(Party party : PartyManager.parties){
                        if(party.getMembers().size()>gm.playerCount){
                            for (Player member : party.getMembers()){
                                PartyCommand.sendMessage(member,"Ваша команда больше чем требуется. Она будет удалена");
                            }
                        }
                    }
                    PartyManager.parties.removeIf(party -> party.getMembers().size()>gm.playerCount);
                }catch (Exception e){
                    p.sendMessage("Возможные типы игры: "+ Arrays.toString(GameMode.values()));
                }
                break;
            case "stop":
                MakotoRandomItemEvent.stopEvent("Ивент прерван");
                break;
            case "test":
                if(strings.length==1){
                    p.getInventory().addItem(MakotoRandomItemEvent.getRandomItem());
                }else {
                    createCircle(p.getLocation().clone(), (int) (2.44 * Integer.parseInt(strings[1])), Integer.parseInt(strings[1]), Material.OBSIDIAN);
                }
                break;
        }
        return true;
    }
}
