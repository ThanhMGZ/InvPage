package org.thanhmagics.invpage;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class Listeners implements Listener {


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerJoinActions(player);
    }

    public static void playerJoinActions(Player player) {
        if (InvPage.get().playerMap.containsKey(player.getUniqueId().toString())) {
            return;
        }
        IPPlayer p = new IPPlayer(player.getUniqueId().toString());
        InvPage.get().playerMap.put(player.getUniqueId().toString(),p);
        p.inv.put(0,InvPage.get().invToMap(player.getInventory()));
        InvPage.get().actionItem(p);
    }

    static int previous = InvPage.get().configFile.getConfig().getInt("previous.inv"),next = InvPage.get().configFile.getConfig().getInt("next.inv");

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (Objects.equals(event.getClickedInventory(), player.getInventory())) {
            IPPlayer p = InvPage.get().playerMap.get(player.getUniqueId().toString());
            int cpage = p.getCurrent();
            if (Integer.valueOf(previous).equals(event.getSlot())) {
                event.setCancelled(true);
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    player.sendMessage(ChatColor.RED + "để sử dụng InvPage phải ở trong gamemode S (vì thiếu kinh phí)");
                    return;
                }
                if (cpage <= 0)
                    return;
                InvPage.get().setInventory(p,p.inv.get(p.getCurrent()-1));
                p.setCurrent(p.getCurrent() - 1);
            } else if (Integer.valueOf(next).equals(event.getSlot())) {
                event.setCancelled(true);
                if (player.getGameMode().equals(GameMode.CREATIVE)) {
                    player.sendMessage(ChatColor.RED + "để sử dụng InvPage phải ở trong gamemode S (vì thiếu kinh phí)");
                    return;
                }
                if (cpage >= p.max - 1)
                    return;
                InvPage.get().setInventory(p,p.inv.get(p.getCurrent()+1));
                p.setCurrent(p.getCurrent() + 1);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getKeepInventory())
            return;
        Player player = event.getPlayer();
        IPPlayer p = InvPage.get().playerMap.get(event.getPlayer().getUniqueId().toString());
        player.getInventory().remove(InvPage.get().getItemStackFromConfig("next",InvPage.get().configFile.getConfig(),p));
        player.getInventory().remove(InvPage.get().getItemStackFromConfig("previous",InvPage.get().configFile.getConfig(),p));
    }

    @EventHandler
    public void onRS(PlayerRespawnEvent event) {
        InvPage.get().actionItem(InvPage.get().playerMap.get(event.getPlayer().getUniqueId().toString()));
    }

    @EventHandler
    public void onTP(PlayerTeleportEvent event) {
        InvPage.get().actionItem(InvPage.get().playerMap.get(event.getPlayer().getUniqueId().toString()));
    }


}
