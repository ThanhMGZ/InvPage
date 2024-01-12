package org.thanhmagics.invpage;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.world.item.ItemMapEmpty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class InvPage extends JavaPlugin {

    private static InvPage instance;

    public FileData configFile;

    public Map<String,IPPlayer> playerMap = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            Files.createDirectories(Path.of(getDataFolder().getPath()));
            Files.createDirectories(Path.of(getDataFolder() + "/data"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        instance = this;
        configFile = new FileData("config.yml");
        getServer().getPluginManager().registerEvents(new Listeners(),this);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Listeners.playerJoinActions(player);
        }
        File[] files = new File(InvPage.get().getDataFolder(),"data").listFiles();
        for (File file : files) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                IPPlayer p = (IPPlayer) ois.readObject();
                InvPage.get().playerMap.put(p.uuid, p);
                InvPage.get().actionItem(p);
                return;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        getCommand("invpage").setExecutor(new Command());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (IPPlayer player : playerMap.values()) {
            try {
                FileOutputStream fos = new FileOutputStream(new File(getDataFolder(), "data/" + player.uuid + ".data"));
                ObjectOutputStream oop = new ObjectOutputStream(fos);
                oop.writeObject(player);
                oop.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public ItemStack getItemStackFromConfig(String path, FileConfiguration cf,IPPlayer player) {
        ItemStack rs = new ItemStack(Material.STONE);
        if (cf.contains(path + ".material"))
            rs = new ItemStack(Material.valueOf(cf.getString(path + ".material").toUpperCase()));
        if (cf.contains(path + ".skull")) {
            GameProfile gameProfile = new GameProfile(UUID.randomUUID(),null);
            gameProfile.getProperties().put("textures",new Property("textures",cf.getString(path + ".skull")));
            Field profileField;
            rs = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) rs.getItemMeta();
            try {
                profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(skullMeta, gameProfile);
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            rs.setItemMeta(skullMeta);
        }
        ItemMeta meta = rs.getItemMeta();
        if (cf.contains(path + ".name"))
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(cf.getString(path + ".name"))));
        List<String> lore = new LinkedList<>();
        if (cf.contains(path + ".lore"))
            for (String value : cf.getStringList(path + ".lore"))
                lore.add(ChatColor.translateAlternateColorCodes('&',value));
        meta.setLore(lore);
        rs.setItemMeta(meta);
        return rs;
    }


    public Map<@Nullable Integer,@org.jetbrains.annotations.Nullable String> invToMap(PlayerInventory inventory) {
        Map<Integer,String> rs = new HashMap<>();
        for (int i = 9; i <= 35; i++) {
            rs.put(i,isToString(inventory.getStorageContents()[i]));
        }
        return rs;
    }

    public void setInventory(IPPlayer player,Map<Integer,String> n) {
        if (n == null)
            n = new HashMap<>();
        Player p = Bukkit.getPlayer(UUID.fromString(player.uuid));
        Map<Integer,String> old = invToMap((p.getInventory()));
        if (player.inv.containsKey(player.getCurrent()))
            player.inv.replace(player.getCurrent(),old);
        else
            player.inv.put(player.getCurrent(),old);
        ItemStack[] newContents = new ItemStack[36];
        for (int i = 9; i <= 35; i++) {
            if (n.containsKey(i)) {
                newContents[i] = stringToIs(n.get(i));
            } else {
                newContents[i] = new ItemStack(Material.AIR);
            }
        }
        for (int i = 0; i < 9; i++) {
            newContents[i] = p.getInventory().getStorageContents()[i];
        }
        p.getInventory().setStorageContents(newContents);
        actionItem(player);
    }

    public String isToString(ItemStack itemStack) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(itemStack);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ItemStack stringToIs(String str) {
        ItemStack item;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(str));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            item = (ItemStack) dataInput.readObject();
            dataInput.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return item;
    }

    public void actionItem(IPPlayer player) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(player.uuid));
        if (!p.isOnline())
            return;
        Player plr = (Player) p;
        plr.getInventory().setItem(Listeners.next,getItemStackFromConfig("next",configFile.getConfig(),player));
        plr.getInventory().setItem(Listeners.previous,getItemStackFromConfig("previous",configFile.getConfig(),player));
    }
    public static InvPage get() {
        return instance;
    }

}
