package com.tkshards.plugin.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {
   private static final Pattern PATTERN = Pattern.compile("(\\d+)([smhd])");

   private TimeUtil() {
   }

   public static Long parseDurationSeconds(String input) {
      if (input != null && !input.isEmpty()) {
         Matcher matcher = PATTERN.matcher(input.toLowerCase());
         long totalSeconds = 0L;
         boolean matched = false;

         while(matcher.find()) {
            matched = true;
            long value = Long.parseLong(matcher.group(1));
            switch (matcher.group(2)) {
               case "s":
                  totalSeconds += value;
                  break;
               case "m":
                  totalSeconds += value * 60L;
                  break;
               case "h":
                  totalSeconds += value * 3600L;
                  break;
               case "d":
                  totalSeconds += value * 86400L;
            }
         }

         return matched ? totalSeconds : null;
      } else {
         return null;
      }
   }

   public static String formatDuration(long totalSeconds) {
      long days = totalSeconds / 86400L;
      long seconds = totalSeconds % 86400L;
      long hours = seconds / 3600L;
      seconds %= 3600L;
      long minutes = seconds / 60L;
      seconds %= 60L;
      StringBuilder builder = new StringBuilder();
      if (days > 0L) {
         builder.append(days).append("d ");
      }

      if (hours > 0L) {
         builder.append(hours).append("h ");
      }

      if (minutes > 0L) {
         builder.append(minutes).append("m ");
      }

      if (seconds > 0L || builder.length() == 0) {
         builder.append(seconds).append("s");
      }

      return builder.toString().trim();
   }
}
