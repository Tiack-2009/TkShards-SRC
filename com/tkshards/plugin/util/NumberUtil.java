package com.tkshards.plugin.util;

import java.util.List;

public final class NumberUtil {
   private NumberUtil() {
   }

   public static String format(long value, boolean enabled, int decimals, List<String> suffixes) {
      if (enabled && suffixes != null && !suffixes.isEmpty()) {
         double number = (double)value;

         int index;
         for(index = 0; Math.abs(number) >= (double)1000.0F && index < suffixes.size() - 1; ++index) {
            number /= (double)1000.0F;
         }

         String pattern = "%." + Math.max(decimals, 0) + "f%s";
         String formatted = String.format(pattern, number, suffixes.get(index));
         if (decimals > 0) {
            formatted = formatted.replaceAll("\\.0+([A-Za-z]*)$", "$1").replaceAll("(\\.\\d*?)0+([A-Za-z]*)$", "$1$2").replaceAll("\\.([A-Za-z]*)$", "$1");
         }

         return formatted;
      } else {
         return String.valueOf(value);
      }
   }
}
