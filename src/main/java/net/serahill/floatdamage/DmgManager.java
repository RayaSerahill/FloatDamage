package net.serahill.floatdamage;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.serahill.floatdamage.FloatDamage.plugin;

public class DmgManager implements Listener {
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity attacker = event.getDamager();
        Entity defender = event.getEntity();
        if ( !(attacker instanceof Player)) return;
        if ( (defender instanceof ArmorStand) ) {
            if (((ArmorStand) defender).isInvisible() && defender.isInvulnerable()) {
                event.setCancelled(true);
            }
            return;
        }

        org.bukkit.util.Vector vector = new org.bukkit.util.Vector(0, 0.35, 0);
        double x = defender.getLocation().getX();
        double y = defender.getLocation().getY();
        double z = defender.getLocation().getZ();
        Location loc = new Location(defender.getWorld(), x, y+0.5 ,z);
        ArmorStand as = (ArmorStand) defender.getWorld().spawn(loc, ArmorStand.class, armorStand -> {
            armorStand.setInvulnerable(true);
            armorStand.setInvisible(true);
            armorStand.setVelocity(vector);
            armorStand.setSmall(true);
            armorStand.setCustomName(ChatColor.RED + " " + String.valueOf(round(event.getFinalDamage(), 1)));
            armorStand.setCustomNameVisible(true);
            armorStand.setNoDamageTicks(40);
            armorStand.setRemoveWhenFarAway(true);
            armorStand.setCollidable(false);
        });

        new BukkitRunnable(){
            @Override
            public void run(){
                as.remove();
            }
        }.runTaskLater(plugin, 10);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
