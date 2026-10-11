package pactoria.horseCombatPaper.Listeners;

import fr.nocsy.mcpets.api.MCPetsAPI;
import fr.nocsy.mcpets.data.Pet;
import io.lumine.mythic.bukkit.MythicBukkit;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Objects;

import static pactoria.horseCombatPaper.extraClasses.HorseDataManagement.playerHorseMovementMap;

public class OnLanceSwing implements Listener{
    private FileConfiguration config;
    public OnLanceSwing(FileConfiguration config) {
        this.config = config;
    }
    @EventHandler
    public void onLanceSwing(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (p.getInventory().getItemInMainHand().getType() != Material.DIAMOND_SWORD) return;
        if (!(e.getEntity() instanceof Player target)) return;
        if (!(p.getVehicle() instanceof Horse h)) return;

        if (MythicBukkit.inst().getMobManager().isMythicMob(h)) return;

        Pet pet = MCPetsAPI.getActivePet(p.getUniqueId());

        if (pet != null && pet.getInstance() != null) {
            Object instance = pet.getInstance();

            if (instance instanceof org.bukkit.entity.Entity entity) {
                if (entity.equals(h)) {
                    return;
                }
            }
        }



        double momentum;

        if (playerHorseMovementMap.get(p.getUniqueId()) != null) {
            momentum = playerHorseMovementMap.get(p.getUniqueId()).getMomentum();
        }
        else{
            return;
        }
        double horseSpeed = Objects.requireNonNull(
                h.getAttribute(Attribute.MOVEMENT_SPEED)).getValue() * config.getDouble("horseMovementSpeedMultiplier");
        double maxDamage;


        if (horseSpeed > config.getDouble("slowFastHorseMiddle")) {
            maxDamage = config.getDouble("fastHorseMaxDamage");
        } else {
            maxDamage = config.getDouble("slowHorseMaxDamage");
        }

        double desiredFinalDamage = (momentum * horseSpeed) / 25;
        if (desiredFinalDamage > maxDamage) {
            desiredFinalDamage = maxDamage;
        }

        double armor = Objects.requireNonNull(target.getAttribute(Attribute.ARMOR)).getValue();
        double toughness = Objects.requireNonNull(target.getAttribute(Attribute.ARMOR_TOUGHNESS)).getValue();

        double resistanceReduction = 0;
        if (target.hasPotionEffect(PotionEffectType.RESISTANCE)) {
            int level = Objects.requireNonNull(target.getPotionEffect(PotionEffectType.RESISTANCE)).getAmplifier() + 1;
            resistanceReduction = level * 0.20;
        }

        double enchantReduction = 0;
        for (ItemStack earmor : target.getInventory().getArmorContents()) {
            if (earmor == null) continue;
            int protLevel = earmor.getEnchantmentLevel(Enchantment.PROTECTION);
            enchantReduction += protLevel * 0.04;
        }
        enchantReduction = Math.min(enchantReduction, 0.80);

        double rawDamage = desiredFinalDamage;

        for (int i = 0; i < 10; i++) {
            double armorReduction = Math.clamp(
                    Math.max(
                            armor / 5.0,
                            armor - (4.0 * rawDamage) / (toughness + 8.0)
                    ),
                    0.0,
                    20.0
            ) / 25.0;
            armorReduction = Math.min(armorReduction, 0.99);

            double totalReduction = 1.0 - ((1.0 - armorReduction) * (1.0 - resistanceReduction) * (1.0 - enchantReduction));
            totalReduction = Math.min(totalReduction, 0.99);

            rawDamage = desiredFinalDamage / (1.0 - totalReduction);
        }

        e.setDamage(rawDamage);

        if (target.getVehicle() instanceof Horse th) {

            int guaranteedKnockOff = this.config.getInt("guaranteedKnockOff");

            double chance = Math.min(momentum / guaranteedKnockOff, 1.0);

            if (Math.random() < chance) {
                th.eject();
            }

        }

        Vector dir = p.getLocation().toVector()
                .subtract(target.getLocation().toVector());

        dir.setY(0);

        if (dir.lengthSquared() == 0) return;

        dir.normalize();

        double knockbackStrength =
                config.getDouble("knockbackStrengthMultiplier")
                        * (1 + (momentum / 10.0))
                        * (1 + (horseSpeed / 10));

        double maxKnockbackStrength =
                Math.max(0.1, config.getDouble("maxKnockBackStrength"));

        knockbackStrength =
                Math.min(knockbackStrength, maxKnockbackStrength);

        target.knockback(knockbackStrength, dir.getX(), dir.getZ());



        playerHorseMovementMap.remove(p.getUniqueId());
    }
}