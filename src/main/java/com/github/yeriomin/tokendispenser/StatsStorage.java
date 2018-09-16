package com.github.yeriomin.tokendispenser;

import spark.Request;

import java.util.List;
import java.util.Map;

abstract public class StatsStorage {

    protected int rateLimitControlPeriod;
    protected int rateLimitRequests;

    abstract public Map<Long, List<Long>> getIps();
    abstract public Map<Long, Integer> getRateLimitHits();
    abstract public Map<Integer, Integer> getTokenRetrievalResults();

    abstract public boolean isSpam(Request request);
    abstract public void recordResult(int responseCode);
    abstract public void clear();

    abstract protected void recordRequest(Request request);

    public int getRateLimitControlPeriod() {
        return rateLimitControlPeriod;
    }

    public void setRateLimitControlPeriod(int rateLimitControlPeriod) {
        this.rateLimitControlPeriod = rateLimitControlPeriod;
    }

    public void setRateLimitRequests(int rateLimitRequests) {
        this.rateLimitRequests = rateLimitRequests;
    }
}
