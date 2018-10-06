package com.github.yeriomin.tokendispenser;

import spark.Request;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class StatsStorage {

    protected int rateLimitControlPeriod;
    protected int rateLimitRequests;
    private Map<Integer, Integer> tokenRetrievalResults = new ConcurrentHashMap<>();

    abstract public Map<Long, List<Long>> getIps();
    abstract public Map<Long, Integer> getRateLimitHits();

    abstract public boolean isSpam(Request request);

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

    public void clear() {
        tokenRetrievalResults.clear();
    }

    public Map<Integer, Integer> getTokenRetrievalResults() {
        return tokenRetrievalResults;
    }

    public void recordResult(int responseCode) {
        if (!tokenRetrievalResults.containsKey(responseCode)) {
            tokenRetrievalResults.put(responseCode, 0);
        }
        tokenRetrievalResults.put(responseCode, tokenRetrievalResults.get(responseCode) + 1);
    }
}
