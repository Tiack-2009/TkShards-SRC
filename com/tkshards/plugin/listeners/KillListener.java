package com.tkshards.plugin.listeners;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.MessageUtil;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

public class KillListener implements Listener {
   private static final long LAST_DAMAGER_WINDOW_MS = 15000L;
   private final TKShardsPlugin plugin;
   private final Map<UUID, Map<UUID, Long>> lastRewardTime = new ConcurrentHashMap();
   private final Map<UUID, DamageRecord> lastDamager = new ConcurrentHashMap();

   public KillListener(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onDamage(EntityDamageByEntityEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof Player victim) {
         Player attacker = this.resolveAttackingPlayer(event);
         if (attacker != null && !attacker.equals(victim)) {
            this.lastDamager.put(victim.getUniqueId(), new DamageRecord(attacker.getUniqueId(), System.currentTimeMillis()));
         }
      }
   }

   private Player resolveAttackingPlayer(EntityDamageByEntityEvent event) {
      Entity var3 = event.getDamager();
      if (var3 instanceof Player player) {
         return player;
      } else {
         var3 = event.getDamager();
         if (var3 instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
               return player;
            }
         }

         return null;
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onDeath(PlayerDeathEvent event) {
      Player victim = event.getEntity();
      Player killer = victim.getKiller();
      if (killer == null) {
         DamageRecord record = (DamageRecord)this.lastDamager.get(victim.getUniqueId());
         if (record != null && System.currentTimeMillis() - record.timestamp() <= 15000L) {
            killer = this.plugin.getServer().getPlayer(record.attackerUuid());
         }
      }

      this.lastDamager.remove(victim.getUniqueId());
      this.plugin.currency().incrementDeaths(victim.getUniqueId());
      if (killer != null && !killer.equals(victim)) {
         this.plugin.currency().incrementKills(killer.getUniqueId());
         String requiredPermission = this.plugin.getConfig().getString("required-permission", "");
         if (requiredPermission != null && !requiredPermission.isEmpty() && !killer.hasPermission(requiredPermission)) {
            Logger var24 = this.plugin.getLogger();
            String var26 = killer.getName();
            var24.info("[Kill] " + var26 + " không có quyền '" + requiredPermission + "' - không cộng Shard.");
         } else {
            long cooldownMinutes = this.plugin.getConfig().getLong("kill.cooldown-minutes", 20L);
            long cooldownMs = cooldownMinutes * 60L * 1000L;
            Map<UUID, Long> perVictim = (Map)this.lastRewardTime.computeIfAbsent(killer.getUniqueId(), (k) -> new ConcurrentHashMap());
            Long last = (Long)perVictim.get(victim.getUniqueId());
            long now = System.currentTimeMillis();
            if (last != null && now - last < cooldownMs) {
               long remainingMin = (cooldownMs - (now - last)) / 60000L + 1L;
               Logger var23 = this.plugin.getLogger();
               String var25 = killer.getName();
               var23.info("[Kill] " + var25 + " giết " + victim.getName() + " nhưng còn đang trong thời gian chờ (~" + remainingMin + " phút nữa) - không cộng Shard.");
            } else {
               perVictim.put(victim.getUniqueId(), now);
               long reward = this.plugin.getConfig().getLong("kill.shards-per-kill", 10L);
               double multiplier = this.plugin.booster().getMultiplier(killer.getUniqueId()) * this.plugin.getRankMultiplier(killer);
               long finalReward = Math.round((double)reward * multiplier);
               this.plugin.currency().addBalance(killer.getUniqueId(), finalReward);
               Logger var10000 = this.plugin.getLogger();
               Level var10001 = Level.INFO;
               String var10002 = killer.getName();
               var10000.log(var10001, "[Kill] " + var10002 + " giết " + victim.getName() + " -> +" + finalReward + " " + this.plugin.lang().currency());
               String template = this.plugin.getConfig().getString("kill.kill-message", "");
               if (template != null && !template.isEmpty()) {
                  String msg = template.replace("{shards}", String.valueOf(finalReward)).replace("{currency}", this.plugin.lang().currency());
                  killer.sendMessage(MessageUtil.color(msg));
               }

            }
         }
      } else {
         this.plugin.getLogger().info("[Kill] " + victim.getName() + " chết nhưng không xác định được người giết - không cộng Shard.");
      }
   }

   private static record DamageRecord(UUID attackerUuid, long timestamp) {
   }
}
