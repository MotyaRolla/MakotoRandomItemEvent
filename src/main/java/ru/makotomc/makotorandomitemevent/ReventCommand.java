package ru.makotomc.makotorandomitemevent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.Color;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.showTitle(
                            Title.title(
                                    Component.text("Ивент начинается").color(TextColor.color(Color.PINK.getRGB())),
                                    Component.text("Арена подготавливается!").color(TextColor.color(Color.gray.getRGB())),
                                    Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(4), Duration.ofSeconds(1))
                            )
                    );
                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,6*20,1,true,false));
                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,6*20,1,true,false));
                }
                participants.clear();
                Bukkit.getScheduler().runTaskLater(MakotoRandomItemEvent.getInstance(), () -> {
                    startEvent(p.getLocation().clone());
                },100);
                break;
            case "stop":
                participants.clear();
                break;
            case "test":
                createCircle(p.getLocation().clone(), Integer.parseInt(strings[1]),Integer.valueOf(strings[2]), Material.OBSIDIAN);
                break;
        }
        return true;
    }
}
