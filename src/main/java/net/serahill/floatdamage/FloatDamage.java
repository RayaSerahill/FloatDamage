package net.serahill.floatdamage;

import org.bukkit.plugin.java.JavaPlugin;

public final class FloatDamage extends JavaPlugin {

    public static FloatDamage plugin;
    @Override
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new DmgManager(), this);
    }
}
