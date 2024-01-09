package net.serahill.floatdamage;

import org.bukkit.plugin.java.JavaPlugin;

public final class FloatDamage extends JavaPlugin {

    public static FloatDamage plugin;
    private DmgManager DmgManager;

    @Override
    public void onEnable() {
        plugin = this;

        this.saveDefaultConfig();

        this.DmgManager = new DmgManager(this);

        getServer().getPluginManager().registerEvents(DmgManager, this);

    }
}
