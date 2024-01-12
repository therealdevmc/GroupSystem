package de.therealdev.groupsystem.manager.message;

import de.therealdev.groupsystem.GroupSystem;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class MessageManager {

    private File file;
    private FileConfiguration fileConfiguration;

    /**
     * MessageManager
     * @param groupSystem
     */
    public MessageManager(GroupSystem groupSystem) {
        String language = groupSystem.getConfig().getString("messages.language");
        if(language == null) {
            System.out.println("Language is null");
            Bukkit.getPluginManager().disablePlugin(groupSystem);
            return;
        }
        file = new File("plugins/GroupSystem", "messages_" + language + ".yml");
        if(!file.exists()) {
            loadFile(groupSystem, "messages_" + language + ".yml");
        }
    }


    /**
     * setFileConfiguration
     */
    public void setFileConfiguration() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }


    /**
     * getMessage
     * @param path
     * @return
     */
    public String getMessage(String path) {
        return fileConfiguration.getString(path).replace("&", "§");
    }


    /**
     * loadFile
     * @param groupSystem
     * @param file
     */
    @SneakyThrows
    public void loadFile(GroupSystem groupSystem, String file) {
        File t = new File(groupSystem.getDataFolder(), file);

        t.createNewFile();
        FileWriter out = new FileWriter(t);
        InputStream is = getClass().getResourceAsStream("/" + file);

        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            out.write(line + "\n");
        }
        out.flush();
        is.close();
        isr.close();
        br.close();
        out.close();

    }

}
