package com.tkshards.plugin.zones;

import org.bukkit.Location;
import org.bukkit.World;

public class Zone {
   private final String name;
   private final String world;
   private final int minX;
   private final int minY;
   private final int minZ;
   private final int maxX;
   private final int maxY;
   private final int maxZ;

   public Zone(String name, String world, int x1, int y1, int z1, int x2, int y2, int z2) {
      this.name = name;
      this.world = world;
      this.minX = Math.min(x1, x2);
      this.minY = Math.min(y1, y2);
      this.minZ = Math.min(z1, z2);
      this.maxX = Math.max(x1, x2);
      this.maxY = Math.max(y1, y2);
      this.maxZ = Math.max(z1, z2);
   }

   public boolean contains(Location loc) {
      World w = loc.getWorld();
      if (w != null && w.getName().equals(this.world)) {
         double x = loc.getX();
         double y = loc.getY();
         double z = loc.getZ();
         return x >= (double)this.minX && x <= (double)(this.maxX + 1) && y >= (double)this.minY && y <= (double)(this.maxY + 1) && z >= (double)this.minZ && z <= (double)(this.maxZ + 1);
      } else {
         return false;
      }
   }

   public String getName() {
      return this.name;
   }

   public String getWorld() {
      return this.world;
   }

   public int getMinX() {
      return this.minX;
   }

   public int getMinY() {
      return this.minY;
   }

   public int getMinZ() {
      return this.minZ;
   }

   public int getMaxX() {
      return this.maxX;
   }

   public int getMaxY() {
      return this.maxY;
   }

   public int getMaxZ() {
      return this.maxZ;
   }
}
