package net.serahill.floatdamage;

import org.bukkit.Bukkit;
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

    private final FloatDamage plugin;
    FileConfiguration config;
    ConfigurationSection enabledEntities;

    public DmgManager(FloatDamage plugin) {
        config = plugin.getConfig();
        enabledEntities = config.getConfigurationSection("enabled");
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();

        if ( !(attacker instanceof Player)) return;
        if ( !(defender instanceof Mob) && !(defender instanceof Player) ) return;

        if ( !isEnabled(defender)) return;

        String prefix = colorize(config.getString("prefix"));
        String suffix = colorize(config.getString("suffix"));
        boolean aboveHead = config.getBoolean("aboveHead");

        if (aboveHead) {
            showAboveHead(defender, prefix, suffix, event);
            return;
        }
        floatDamage(defender, prefix, suffix, event);
    }

    public void floatDamage(Entity defender, String prefix, String suffix, EntityDamageByEntityEvent event) {

        Random rand = new Random();
        Location loc = defender.getLocation().clone();
        loc.add(new Vector(.5 * (rand.nextBoolean() ? -1 : 0), 1.9f, .5 * (rand.nextBoolean() ? -1 : 0)));

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

    private ArmorStand spawnInvisibleArmorStand(Location location, String customName) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.add(0, 100, 0), EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(customName);
        armorStand.isSmall();
        armorStand.teleport(location.add(0, 0.2, 0));
        return armorStand;
    }


    public void showAboveHead(Entity defender, String prefix, String suffix, EntityDamageByEntityEvent event) {

        int taskId = 0;

        ArmorStand armorStand = spawnInvisibleArmorStand(
                defender.getLocation().add(0, 100, 0),
                prefix + String.valueOf(round(event.getFinalDamage(), 1)) + suffix
        );

        int finalTaskId = taskId;

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (defender.isValid()) {
                armorStand.teleport(defender.getLocation());
            } else {
                armorStand.remove();
                Bukkit.getScheduler().cancelTask(finalTaskId);
            }
        }, 0L, 1L); // 0L for immediate start, 1L for 1-tick interval


        new BukkitRunnable(){
            @Override
            public void run(){
                armorStand.remove();
                //check if finalTaskId is something else than 0, if it is, cancel the task
                if (finalTaskId != 0) Bukkit.getScheduler().cancelTask(finalTaskId);
            }
        }.runTaskLater(plugin, 20);

    }

    public boolean isEnabled(Entity defender) {

        boolean enabledMonsters = enabledEntities.getBoolean("monster");
        boolean enabledAnimal = enabledEntities.getBoolean("animal");
        boolean enabledPlayer = enabledEntities.getBoolean("player");

        // Check if defender is an animal and if damage is enabled, return if both are true
        if ( (defender instanceof Animals) && !(enabledAnimal) ) return false;

        // Check if defender is a monster and if damage is enabled, return if both are true
        if ( (defender instanceof Monster) && !(enabledMonsters) ) return false;

        // Check if defender is a player and if damage is enabled, return if both are true
        if ( (defender instanceof Player) && (!enabledPlayer) ) return false;

        // Check if defender is an armorstand and if damage is enabled, return if both are true
        if ( (defender instanceof ArmorStand) ) return false;

        return true;
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
