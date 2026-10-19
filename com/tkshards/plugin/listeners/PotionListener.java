package com.tkshards.plugin.listeners;

import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.MessageUtil;
import com.tkshards.plugin.util.SchedulerUtil;
import com.tkshards.plugin.util.TimeUtil;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

public class PotionListener implements Listener {
   private static final String ITEM_KEY_NAME = "TKSHARDS_POTION";
   private static final long EXPIRE_CHECK_INTERVAL_TICKS = 6000L;
   private final TKShardsPlugin plugin;
   private final NamespacedKey markerKey;
   private final NamespacedKey createdAtKey;
   private Object cleanupTask;

   public PotionListener(TKShardsPlugin plugin) {
      this.plugin = plugin;
      this.markerKey = new NamespacedKey(plugin, "potion_marker");
      this.createdAtKey = new NamespacedKey(plugin, "potion_created_at");
   }

   public void startExpiryCleanup() {
      this.stopExpiryCleanup();
      this.cleanupTask = SchedulerUtil.runTimer(this.plugin, this::cleanupExpiredPotions, 6000L, 6000L);
   }

   public void stopExpiryCleanup() {
      SchedulerUtil.cancel(this.cleanupTask);
      this.cleanupTask = null;
   }

   private long expireMillis() {
      long hours = this.plugin.getConfig().getLong("potion.expire-hours", 24L);
      return hours * 60L * 60L * 1000L;
   }

   public ItemStack createPotionItem() {
      ItemStack item = new ItemStack(Material.POTION);
      PotionMeta meta = (PotionMeta)item.getItemMeta();
      if (meta != null) {
         meta.setBasePotionType(PotionType.WATER);
         String name = this.plugin.getConfig().getString("potion.display-name", "&bThuốc Nhân Đôi Shard");
         List<String> lore = this.plugin.getConfig().getStringList("potion.lore");
         meta.setDisplayName(MessageUtil.color(name));
         meta.setLore(lore.stream().map(MessageUtil::color).toList());
         meta.getPersistentDataContainer().set(this.markerKey, PersistentDataType.STRING, "TKSHARDS_POTION");
         meta.getPersistentDataContainer().set(this.createdAtKey, PersistentDataType.LONG, System.currentTimeMillis());
         item.setItemMeta(meta);
      }

      return item;
   }

   private boolean isPotionItem(ItemStack item) {
      if (item != null && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && "TKSHARDS_POTION".equals(meta.getPersistentDataContainer().get(this.markerKey, PersistentDataType.STRING));
      } else {
         return false;
      }
   }

   private boolean isExpired(ItemStack item) {
      ItemMeta meta = item.getItemMeta();
      if (meta == null) {
         return false;
      } else {
         Long createdAt = (Long)meta.getPersistentDataContainer().get(this.createdAtKey, PersistentDataType.LONG);
         if (createdAt == null) {
            return false;
         } else {
            return System.currentTimeMillis() - createdAt > this.expireMillis();
         }
      }
   }

   @EventHandler
   public void onConsume(PlayerItemConsumeEvent event) {
      ItemStack item = event.getItem();
      if (this.isPotionItem(item)) {
         Player player = event.getPlayer();
         event.setItem(new ItemStack(Material.AIR));
         if (this.isExpired(item)) {
            player.sendMessage(this.plugin.lang().get("potion.expired-on-drink"));
         } else {
            long durationSeconds = this.plugin.getConfig().getLong("potion.duration-seconds", 1800L);
            double multiplier = this.plugin.getConfig().getDouble("potion.multiplier", (double)2.0F);
            this.plugin.booster().activate(player.getUniqueId(), durationSeconds, multiplier);
            player.sendMessage(this.plugin.lang().get("potion.received", "duration", TimeUtil.formatDuration(durationSeconds), "multiplier", String.valueOf((long)multiplier)));
         }
      }
   }

   private void cleanupExpiredPotions() {
      for(Player player : Bukkit.getOnlinePlayers()) {
         SchedulerUtil.runForEntity(this.plugin, player, () -> {
            Inventory inv = player.getInventory();
            boolean removedAny = false;
            ItemStack[] contents = inv.getStorageContents();

            for(int i = 0; i < contents.length; ++i) {
               ItemStack stack = contents[i];
               if (stack != null && this.isPotionItem(stack) && this.isExpired(stack)) {
                  inv.setItem(i, (ItemStack)null);
                  removedAny = true;
               }
            }

            if (removedAny) {
               player.sendMessage(this.plugin.lang().get("potion.expired-removed"));
            }

         });
      }

   }

   public void give(Player target) {
      target.getInventory().addItem(new ItemStack[]{this.createPotionItem()});
   }
}
