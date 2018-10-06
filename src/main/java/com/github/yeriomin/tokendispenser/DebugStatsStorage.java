package com.github.yeriomin.tokendispenser;

import spark.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DebugStatsStorage extends StatsStorage {

    private Map<Long, List<Long>> ips = new ConcurrentHashMap<>();
    private Map<Long, Integer> rateLimitHits = new ConcurrentHashMap<>();

    @Override
    public Map<Long, List<Long>> getIps() {
        return ips;
    }

    @Override
    public Map<Long, Integer> getRateLimitHits() {
        return rateLimitHits;
    }

    @Override
    public boolean isSpam(Request request) {
        recordRequest(request);
        long ip = Server.getIp(request);
        if (ips.containsKey(ip)) {
            int recentRequestCount = 0;
            for (Long timestamp: ips.get(ip)) {
                if (timestamp > System.currentTimeMillis() - rateLimitControlPeriod) {
                    recentRequestCount++;
                }
                if (recentRequestCount > rateLimitRequests) {
                    if (!rateLimitHits.containsKey(ip)) {
                        rateLimitHits.put(ip, 0);
                    }
                    rateLimitHits.put(ip, rateLimitHits.get(ip) + 1);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void clear() {
        super.clear();
        ips.clear();
        rateLimitHits.clear();
    }

    @Override
    protected void recordRequest(Request request) {
        long ip = Server.getIp(request);
        if (!ips.containsKey(ip)) {
            ips.put(ip, new ArrayList<>());
        }
        ips.get(ip).add(System.currentTimeMillis());
    }
}
