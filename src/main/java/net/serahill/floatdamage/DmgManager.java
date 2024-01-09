package net.serahill.floatdamage;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static net.serahill.floatdamage.FloatDamage.plugin;

public class DmgManager implements Listener {

    FileConfiguration config;
    ConfigurationSection enabledEntities;

    public DmgManager(FloatDamage plugin) {
        config = plugin.getConfig();
        enabledEntities = config.getConfigurationSection("enabled");
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();

        if ( !(attacker instanceof Player)) return;
        if ( !(defender instanceof Mob) && !(defender instanceof Player) ) return;

        boolean enabledMonsters = enabledEntities.getBoolean("monster");
        boolean enabledAnimal = enabledEntities.getBoolean("animal");
        boolean enabledPlayer = enabledEntities.getBoolean("player");

        // Check if defender is an animal and if animal damage is enabled, return if both are true
        if ( (defender instanceof Animals) && !(enabledAnimal) ) return;

        // Check if defender is a monster and if animal damage is enabled, return if both are true
        if ( (defender instanceof Monster) && !(enabledMonsters) ) return;

        // Check if defender is a player and if animal damage is enabled, return if both are true
        if ( (defender instanceof Player) && (!enabledPlayer) ) return;
        if ( (defender instanceof ArmorStand) ) return;

        Random rand = new Random();
        Location loc = defender.getLocation().clone();
        loc.add(new Vector(.5 * (rand.nextBoolean() ? -1 : 0), 1.9f, .5 * (rand.nextBoolean() ? -1 : 0)));

        String prefix = colorize(config.getString("prefix"));
        String suffix = colorize(config.getString("suffix"));

        TextDisplay floatDisplay = defender.getWorld().spawn(loc, TextDisplay.class);
        floatDisplay.setText(prefix + String.valueOf(round(event.getFinalDamage(), 1)) + suffix);
        floatDisplay.setBillboard(Display.Billboard.VERTICAL);
        floatDisplay.setSeeThrough(false);
        floatDisplay.setShadowed(false);

        new BukkitRunnable(){
            @Override
            public void run(){
                floatDisplay.remove();
            }
        }.runTaskLater(plugin, 10);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public static String colorize(String text) {
        if (text == null) {
            return null;
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
