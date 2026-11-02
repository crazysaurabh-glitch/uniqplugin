
package com.lazermc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.block.Action;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LazerMCAbility extends JavaPlugin implements Listener {

    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private NamespacedKey abilityKey;

    @Override
    public void onEnable() {
        abilityKey = new NamespacedKey(this, "ability");
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("ability") != null) {
            getCommand("ability").setExecutor((sender, command, label, args) -> {
                if (sender instanceof Player player) {
                    player.sendMessage(ChatColor.GOLD + "Your kills: " + kills.getOrDefault(player.getUniqueId(), 0));
                    player.sendMessage(ChatColor.YELLOW + "3 Kills: Thunder | 8: Frost | 15: Fire | 25: Air");
                }
                return true;
            });
        }

        getLogger().info("LazerMC Ability enabled!");
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();

        if (killer == null) return;

        UUID id = killer.getUniqueId();
        int count = kills.getOrDefault(id, 0) + 1;
        kills.put(id, count);

        killer.sendMessage(ChatColor.GREEN + "Kills: " + count);

        if (count == 3 || count == 8 || count == 15 || count == 25) {
            String ability = count == 3 ? "Thunder" :
                    count == 8 ? "Frost" :
                    count == 15 ? "Fire" : "Air";

            giveAbility(killer, ability);
        }
    }

    private void giveAbility(Player player, String ability) {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + ability + " Ability");
        meta.getPersistentDataContainer().set(
                abilityKey, PersistentDataType.STRING, ability
        );

        item.setItemMeta(meta);
        player.getInventory().addItem(item);

        player.sendMessage(ChatColor.GREEN + ability + " Ability unlocked!");
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR &&
                action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        String ability = item.getItemMeta()
                .getPersistentDataContainer()
                .get(abilityKey, PersistentDataType.STRING);

        if (ability == null) return;

        Player player = event.getPlayer();
        UUID id = player.getUniqueId();

        long now = System.currentTimeMillis();
        if (cooldowns.getOrDefault(id, 0L) > now) {
            player.sendMessage(ChatColor.RED + "Ability is on cooldown!");
            return;
        }

        switch (ability) {
            case "Thunder":
                player.getWorld().strikeLightning(player.getTargetBlockExact(20) != null
                        ? player.getTargetBlockExact(20).getLocation()
                        : player.getLocation());
                break;

            case "Frost":
                player.sendMessage(ChatColor.AQUA + "Frost activated!");
                break;

            case "Fire":
                player.setFireTicks(100);
                player.sendMessage(ChatColor.RED + "Fire activated!");
                break;

            case "Air":
                player.setVelocity(player.getLocation().getDirection().multiply(2).setY(1));
                player.sendMessage(ChatColor.GREEN + "Air activated!");
                break;
        }

        cooldowns.put(id, now + 30000);
    }
}
