package pactoria.horseCombatPaper.Listeners;

import fr.nocsy.mcpets.api.MCPetsAPI;
import fr.nocsy.mcpets.data.Pet;
import pactoria.horseCombatPaper.extraClasses.HorseDataManagement;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.kyori.adventure.text.Component;

import java.io.File;

import static pactoria.horseCombatPaper.extraClasses.HorseDataManagement.playerHorseMovementMap;

public class OnHorseMove implements Listener {

    FileConfiguration config;

    public OnHorseMove(FileConfiguration config){
        this.config = config;
    }



    @EventHandler
    public void onHorseMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!(p.getVehicle() instanceof Horse horse)) return;

        if (MythicBukkit.inst().getMobManager().isMythicMob(horse)) return;


        Pet pet = MCPetsAPI.getActivePet(p.getUniqueId());

        if (pet != null && pet.getInstance() != null) {
            Object instance = pet.getInstance();

            if (instance instanceof org.bukkit.entity.Entity entity) {
                if (entity.equals(horse)) {
                    return;
                }
            }
        }


        if (e.getFrom().getX() == e.getTo().getX() && e.getFrom().getZ() == e.getTo().getZ()) {
            playerHorseMovementMap.remove(p.getUniqueId());
            return;
        }

        long thisEventFire = System.currentTimeMillis();
        HorseDataManagement management = playerHorseMovementMap.get(p.getUniqueId());

        if (management != null) {
            if (thisEventFire - management.getLastEventFireTime() > 150) {
                playerHorseMovementMap.remove(p.getUniqueId());
                return;
            }
        }

        if (!isMovingForward(e, horse)) {
            playerHorseMovementMap.remove(p.getUniqueId());
            return;
        }

        if (management == null) {
            management = new HorseDataManagement(thisEventFire);
            playerHorseMovementMap.put(p.getUniqueId(), management);
        } else {
            management.setLastEventFireTime(thisEventFire);
        }

        if (management.getMomentum() < this.config.getDouble("maxMomentum")){
            management.increaseMomentum(this.config.getDouble("momentumIncrement"));

        }
        int momentum = (int) management.getMomentum();
        p.sendActionBar(Component.text("momentum: " + momentum).color(NamedTextColor.BLUE));
    }

    @EventHandler
    public void onHorseDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDismounted() instanceof Horse)) return;
        playerHorseMovementMap.remove(player.getUniqueId());
    }

    private boolean isMovingForward(PlayerMoveEvent e, Horse horse) {
        assert e.getTo() != null;
        Vector movement = e.getTo().toVector().subtract(e.getFrom().toVector());
        movement.setY(0);
        if (movement.lengthSquared() == 0) return false;

        Vector facing = horse.getLocation().getDirection();
        facing.setY(0).normalize();
        movement.normalize();

        return facing.dot(movement) > 0.5;
    }
}

