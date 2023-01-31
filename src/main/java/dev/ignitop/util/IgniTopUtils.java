/*
 * Copyright 2023 Ilya Shishkov (https://github.com/shishkovilja)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ignitop.util;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public final class IgniTopUtils {
    /**
     * @param uptimeMillis Uptime millis.
     */
    public static String formattedUptime(long uptimeMillis) {
        long totalSeconds = uptimeMillis / 1000;

        long days = totalSeconds / (24 * 3600);
        long hours = (totalSeconds / 3600)  % 24;
        long minutes = (totalSeconds / 60) % 60;
        long seconds = totalSeconds % 60;

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    /**
     * @param map Map.
     * @param k Key.
     * @param v Value.
     */
    public static <K, V> Map<K, V> append(@Nullable Map<K, V> map, K k, V v) {
        if (map == null) {
            Map<K, V> map0 = new HashMap<>();
            map0.put(k, v);

            return map0;
        }
        else {
            map.put(k, v);

            return map;
        }
    }
}
