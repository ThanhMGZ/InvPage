package org.thanhmagics.invpage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class FileData {

    private FileConfiguration config;

    private File file;


    public FileData(String name) {
        file = new File(InvPage.get().getDataFolder() + "/" + name);
        if (!file.exists()) {
            try {
                file.createNewFile();
                InvPage.get().saveResource(name,true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
