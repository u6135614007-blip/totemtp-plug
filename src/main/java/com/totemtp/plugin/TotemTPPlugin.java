package com.totemtp.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class TotemTPPlugin extends JavaPlugin {

    private DataManager dataManager;

    @Override
    public void onEnable() {
        this.dataManager = new DataManager(this);
        this.dataManager.load();

        getServer().getPluginManager().registerEvents(new TotemListener(this), this);

        TotemTPCommand command = new TotemTPCommand(this);
        getCommand("totemtp").setExecutor(command);
        getCommand("totemtp").setTabCompleter(command);

        getLogger().info("TotemTP active - " + dataManager.getAllConfigs().size() + " configuration(s) chargee(s).");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.save();
        }
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
