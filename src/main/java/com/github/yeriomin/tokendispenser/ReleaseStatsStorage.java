package com.github.yeriomin.tokendispenser;

import spark.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReleaseStatsStorage extends StatsStorage {

    private ExpiringIpList ips = new ExpiringIpList();

    @Override
    public void setRateLimitControlPeriod(int rateLimitControlPeriod) {
        ips.setKeepFor(rateLimitControlPeriod);
        super.setRateLimitControlPeriod(rateLimitControlPeriod);
    }

    @Override
    public void setRateLimitRequests(int rateLimitRequests) {
        ips.setLimit(rateLimitRequests);
        super.setRateLimitRequests(rateLimitRequests);
    }

    @Override
    public Map<Long, List<Long>> getIps() {
        return new HashMap<>();
    }

    @Override
    public Map<Long, Integer> getRateLimitHits() {
        return new HashMap<>();
    }

    @Override
    public boolean isSpam(Request request) {
        recordRequest(request);
        return ips.tooMany(Server.getIp(request));
    }

    @Override
    public void clear() {
        super.clear();
        ips.cleanup();
    }

    @Override
    protected void recordRequest(Request request) {
        ips.add(Server.getIp(request));
    }
}
