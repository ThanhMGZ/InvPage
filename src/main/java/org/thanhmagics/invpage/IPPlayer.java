package org.thanhmagics.invpage;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class IPPlayer implements Serializable {

    public String uuid;

    public Map<Integer,Map<Integer, String>> inv = new HashMap<>();

    private int current = 0;

    public int max = 2;

    public IPPlayer(String uuid) {
        this.uuid = uuid;
    }


    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }
}
