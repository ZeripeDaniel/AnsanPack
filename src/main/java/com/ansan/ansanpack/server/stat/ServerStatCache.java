package com.ansan.ansanpack.server.stat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerStatCache {
    private static final Map<UUID, PlayerStat> CACHE = new ConcurrentHashMap<>();

    public static void update(UUID uuid, PlayerStat stat) {
        CACHE.put(uuid, stat);
    }

    public static PlayerStat get(UUID uuid) {
        return CACHE.getOrDefault(uuid, new PlayerStat(0, 0, 0, 0, 0)); // 없는 경우 기본값
    }

    public static void remove(UUID uuid) {
        CACHE.remove(uuid);
    }
}
