package org.thanhmagics.invpage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;

public class Command implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender.equals(Bukkit.getConsoleSender()) || (commandSender.hasPermission("op"))) {
            if (strings.length == 2) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(strings[0]);
                int nmax = Integer.parseInt(strings[1]);
                if (nmax < 1)
                    return true;
                IPPlayer p = InvPage.get().playerMap.get(player.getUniqueId().toString());
                p.max = nmax;
            }
        } else {
            ((Player) commandSender).sendMessage("Lệnh chỉ đc xài bởi console hoặc player có op");
        }
        return true;
    }
}
