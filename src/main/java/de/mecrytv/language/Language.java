package de.mecrytv.language;

import de.mecrytv.DatabaseAPI;
import de.mecrytv.language.manager.ConfigManager;
import de.mecrytv.language.model.LanguageModel;
import de.mecrytv.languageapi.LanguageAPI;
import de.mecrytv.utils.DatabaseConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;

public final class Language extends JavaPlugin {

    private static Language instance;
    private LanguageAPI languageAPI;
    private DatabaseAPI databaseAPI;
    private ConfigManager config;

    @Override
    public void onEnable() {
        instance = this;
        this.languageAPI = new LanguageAPI(Paths.get("/home/minecraft/languages/"));
        this.config = new ConfigManager(getDataFolder().toPath(), "config.json");

        DatabaseConfig dbConfig = new DatabaseConfig(
                config.getString("mariadb.host"),
                config.getInt("mariadb.port"),
                config.getString("mariadb.database"),
                config.getString("mariadb.username"),
                config.getString("mariadb.password"),
                config.getString("redis.host"),
                config.getInt("redis.port"),
                config.getString("redis.password")
        );
        this.databaseAPI = new DatabaseAPI(dbConfig);

        databaseAPI.registerModel("language", LanguageModel::new);

        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            getLogger().info("✅ HeadDatabase-API erfolgreich gefunden!");
        } else {
            getLogger().warning("❌ HeadDatabase wurde nicht gefunden! GUI-Flaggen funktionieren nicht.");
        }
    }

    @Override
    public void onDisable() {
        if (this.databaseAPI != null) this.databaseAPI.shutdown();
    }

    public Component getPrefix() {
        String prefixRaw = this.config.getString("prefix");
        if (prefixRaw.isEmpty() || prefixRaw == null) {
            prefixRaw = "<darK_grey>[<gold>Language<dark_grey>] ";
        }
        return MiniMessage.miniMessage().deserialize(prefixRaw);
    }
    public static Language getInstance() {
        return instance;
    }
    public LanguageAPI getLanguageAPI() {
        return languageAPI;
    }
    public DatabaseAPI getDatabaseAPI() {
        return databaseAPI;
    }
    public ConfigManager getConfiguration() {
        return config;
    }
}
