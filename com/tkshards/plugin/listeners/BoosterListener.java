package com.tkshards.plugin.listeners;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.MessageUtil;
import com.tkshards.plugin.util.TimeUtil;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class BoosterListener implements Listener {
   private static final String ITEM_KEY_NAME = "TKSHARDS_BOOSTER";
   private final TKShardsPlugin plugin;
   private final Map<UUID, Long> activeUntil = new ConcurrentHashMap();
   private final Map<UUID, Double> activeMultiplier = new ConcurrentHashMap();

   public BoosterListener(TKShardsPlugin plugin) {
      this.plugin = plugin;
   }

   public ItemStack createBoosterItem() {
      ItemStack item = new ItemStack(Material.NETHER_STAR);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         String name = this.plugin.getConfig().getString("booster.display-name", "&#A303F9Booster TKShards");
         List<String> lore = this.plugin.getConfig().getStringList("booster.lore");
         meta.setDisplayName(MessageUtil.color(name));
         meta.setLore(lore.stream().map(MessageUtil::color).toList());
         meta.getPersistentDataContainer().set(new NamespacedKey(this.plugin, "booster_marker"), PersistentDataType.STRING, "TKSHARDS_BOOSTER");
         item.setItemMeta(meta);
      }

      return item;
   }

   private boolean isBoosterItem(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && "TKSHARDS_BOOSTER".equals(meta.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "booster_marker"), PersistentDataType.STRING));
      } else {
         return false;
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
         ItemStack item = event.getItem();
         if (this.isBoosterItem(item)) {
            Player player = event.getPlayer();
            event.setCancelled(true);
            long durationSeconds = 3600L;
            this.activate(player.getUniqueId(), durationSeconds);
            item.setAmount(item.getAmount() - 1);
            player.sendMessage(this.plugin.lang().get("booster.received", "duration", TimeUtil.formatDuration(durationSeconds)));
         }
      }
   }

   public void activate(UUID uuid, long durationSeconds) {
      this.activate(uuid, durationSeconds, this.plugin.getConfig().getDouble("booster.multiplier", (double)1.0F));
   }

   public void activate(UUID uuid, long durationSeconds, double multiplier) {
      long until = System.currentTimeMillis() + durationSeconds * 1000L;
      Long existing = (Long)this.activeUntil.get(uuid);
      if (existing == null || until > existing) {
         this.activeUntil.put(uuid, until);
         this.activeMultiplier.put(uuid, multiplier);
      }

   }

   public boolean isActive(UUID uuid) {
      Long until = (Long)this.activeUntil.get(uuid);
      return until != null && until > System.currentTimeMillis();
   }

   public long getRemainingSeconds(UUID uuid) {
      if (!this.isActive(uuid)) {
         return 0L;
      } else {
         Long until = (Long)this.activeUntil.get(uuid);
         return Math.max(0L, (until - System.currentTimeMillis()) / 1000L);
      }
   }

   public double getMultiplier(UUID uuid) {
      return !this.isActive(uuid) ? (double)1.0F : (Double)this.activeMultiplier.getOrDefault(uuid, (double)1.0F);
   }

   public void give(Player target, long durationSeconds) {
      target.getInventory().addItem(new ItemStack[]{this.createBoosterItem()});
      this.activate(target.getUniqueId(), durationSeconds);
   }
}
