package com.tkshards.plugin.zones;

import com.tkshards.plugin.Lang;
import com.tkshards.plugin.TKShardsPlugin;
import com.tkshards.plugin.util.MessageUtil;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ZoneManager implements Listener {
   private static final String WAND_NAME = "&#A303F9Wand vung TKShards";
   private final TKShardsPlugin plugin;
   private final Map<UUID, Location> pos1 = new ConcurrentHashMap();
   private final Map<UUID, Location> pos2 = new ConcurrentHashMap();
   private final Map<String, Zone> zones = new HashMap();
   private File file;
   private YamlConfiguration data;

   public ZoneManager(TKShardsPlugin plugin) {
      this.plugin = plugin;
      this.load();
   }

   private void load() {
      this.file = new File(this.plugin.getDataFolder(), "zones/zones.yml");
      this.data = YamlConfiguration.loadConfiguration(this.file);
      this.zones.clear();
      ConfigurationSection section = this.data.getConfigurationSection("zones");
      if (section != null) {
         for(String name : section.getKeys(false)) {
            ConfigurationSection z = section.getConfigurationSection(name);
            if (z != null) {
               this.zones.put(name.toLowerCase(), new Zone(name, z.getString("world", "world"), z.getInt("x1"), z.getInt("y1"), z.getInt("z1"), z.getInt("x2"), z.getInt("y2"), z.getInt("z2")));
            }
         }
      }

   }

   public void reload() {
      this.load();
   }

   private void save() {
      this.data.set("zones", (Object)null);

      for(Zone zone : this.zones.values()) {
         String path = "zones." + zone.getName();
         this.data.set(path + ".world", zone.getWorld());
         this.data.set(path + ".x1", zone.getMinX());
         this.data.set(path + ".y1", zone.getMinY());
         this.data.set(path + ".z1", zone.getMinZ());
         this.data.set(path + ".x2", zone.getMaxX());
         this.data.set(path + ".y2", zone.getMaxY());
         this.data.set(path + ".z2", zone.getMaxZ());
      }

      try {
         this.data.save(this.file);
      } catch (Exception e) {
         this.plugin.getLogger().warning("Không thể lưu zones.yml: " + e.getMessage());
      }

   }

   public ItemStack createWand() {
      ItemStack item = new ItemStack(Material.GOLDEN_AXE);
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(MessageUtil.color("&#A303F9Wand vung TKShards"));
         meta.setLore(List.of(MessageUtil.color("&7Chuot trai = diem 1"), MessageUtil.color("&7Chuot phai = diem 2")));
         item.setItemMeta(meta);
      }

      return item;
   }

   private boolean isWand(ItemStack item) {
      if (item != null && item.getType() == Material.GOLDEN_AXE && item.hasItemMeta()) {
         ItemMeta meta = item.getItemMeta();
         return meta != null && MessageUtil.color("&#A303F9Wand vung TKShards").equals(meta.getDisplayName());
      } else {
         return false;
      }
   }

   @EventHandler
   public void onInteract(PlayerInteractEvent event) {
      if (this.isWand(event.getItem()) && event.getClickedBlock() != null) {
         Player player = event.getPlayer();
         Location loc = event.getClickedBlock().getLocation();
         if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            this.pos1.put(player.getUniqueId(), loc);
            Lang var10001 = this.plugin.lang();
            String[] var10003 = new String[]{"pos", null};
            int var10006 = loc.getBlockX();
            var10003[1] = var10006 + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
            player.sendMessage(var10001.get("zones.pos1-set", var10003));
            event.setCancelled(true);
         } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            this.pos2.put(player.getUniqueId(), loc);
            Lang var4 = this.plugin.lang();
            String[] var5 = new String[]{"pos", null};
            int var6 = loc.getBlockX();
            var5[1] = var6 + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
            player.sendMessage(var4.get("zones.pos2-set", var5));
            event.setCancelled(true);
         }

      }
   }

   public boolean hasBothPoints(UUID uuid) {
      return this.pos1.containsKey(uuid) && this.pos2.containsKey(uuid);
   }

   public boolean createZone(UUID uuid, String name) {
      if (this.hasBothPoints(uuid) && !this.zones.containsKey(name.toLowerCase())) {
         Location a = (Location)this.pos1.get(uuid);
         Location b = (Location)this.pos2.get(uuid);
         Zone zone = new Zone(name, a.getWorld().getName(), a.getBlockX(), a.getBlockY(), a.getBlockZ(), b.getBlockX(), b.getBlockY(), b.getBlockZ());
         this.zones.put(name.toLowerCase(), zone);
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeZone(String name) {
      boolean removed = this.zones.remove(name.toLowerCase()) != null;
      if (removed) {
         this.save();
      }

      return removed;
   }

   public Map<String, Zone> getZones() {
      return this.zones;
   }

   public boolean zoneExists(String name) {
      return this.zones.containsKey(name.toLowerCase());
   }
}
