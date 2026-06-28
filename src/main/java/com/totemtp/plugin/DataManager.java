package com.totemtp.plugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gere le chargement / sauvegarde des "stations" (une station = un joueur declencheur
 * avec son compteur de totems et sa destination de TP).
 */
public class DataManager {

    private final TotemTPPlugin plugin;
    private final File file;
    private final Map<UUID, TotemStation> stations = new LinkedHashMap<>();

    public DataManager(TotemTPPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        stations.clear();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            return;
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        var section = cfg.getConfigurationSection("stations");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            try {
                UUID triggerUUID = UUID.fromString(key);
                String path = "stations." + key + ".";

                String triggerName = cfg.getString(path + "trigger-name", "inconnu");
                TotemStation station = new TotemStation(triggerUUID, triggerName);

                station.setCounterRaw(cfg.getInt(path + "counter", 0));

                String targetUUIDStr = cfg.getString(path + "target-uuid", null);
                if (targetUUIDStr != null) {
                    String targetName = cfg.getString(path + "target-name", "inconnu");
                    station.setTarget(UUID.fromString(targetUUIDStr), targetName);
                }

                String world = cfg.getString(path + "location.world", null);
                if (world != null) {
                    double x = cfg.getDouble(path + "location.x");
                    double y = cfg.getDouble(path + "location.y");
                    double z = cfg.getDouble(path + "location.z");
                    float yaw = (float) cfg.getDouble(path + "location.yaw");
                    float pitch = (float) cfg.getDouble(path + "location.pitch");
                    station.setRaw(world, x, y, z, yaw, pitch);
                }

                stations.put(triggerUUID, station);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Entree invalide dans data.yml: " + key);
            }
        }
    }

    public void save() {
        FileConfiguration cfg = new YamlConfiguration();

        for (Map.Entry<UUID, TotemStation> entry : stations.entrySet()) {
            UUID uuid = entry.getKey();
            TotemStation station = entry.getValue();
            String path = "stations." + uuid + ".";

            cfg.set(path + "trigger-name", station.getTriggerName());
            cfg.set(path + "counter", station.getCounter());

            if (station.getTargetUUID() != null) {
                cfg.set(path + "target-uuid", station.getTargetUUID().toString());
                cfg.set(path + "target-name", station.getTargetName());
            }

            if (station.hasLocation()) {
                cfg.set(path + "location.world", station.getWorldName());
                cfg.set(path + "location.x", station.getX());
                cfg.set(path + "location.y", station.getY());
                cfg.set(path + "location.z", station.getZ());
                cfg.set(path + "location.yaw", (double) station.getYaw());
                cfg.set(path + "location.pitch", (double) station.getPitch());
            }
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder data.yml: " + e.getMessage());
        }
    }

    public TotemStation getOrCreate(UUID triggerUUID, String triggerName) {
        TotemStation station = stations.get(triggerUUID);
        if (station == null) {
            station = new TotemStation(triggerUUID, triggerName);
            stations.put(triggerUUID, station);
        } else {
            station.setTriggerName(triggerName);
        }
        return station;
    }

    public TotemStation get(UUID triggerUUID) {
        return stations.get(triggerUUID);
    }

    public void remove(UUID triggerUUID) {
        stations.remove(triggerUUID);
    }

    public Map<UUID, TotemStation> getAllConfigs() {
        return stations;
    }
}
