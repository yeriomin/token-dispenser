package com.github.yeriomin.tokendispenser;

import spark.Request;
import spark.Response;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatsResource {

    static private final String PARAM_MODE = "mode";
    static private final String PARAM_UNIQUE = "unique";

    static private final String MODE_IPS_BY_REQUEST_COUNT = "ipsByRequestCount";
    static private final String MODE_REQUESTS_BY_DAY = "requestsByDay";
    static private final String MODE_RATE_LIMIT_HITS = "rateLimitHits";
    static private final String MODE_TOTAL_RATE_LIMIT_HITS = "totalRateLimitHits";
    static private final String MODE_TOKEN_RETRIEVAL_RESULTS = "tokenRetrievalResults";
    static private final String MODE_TOTAL = "total";

    static private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public String get(Request request, Response response) {
        String mode = request.queryParams(PARAM_MODE);
        boolean unique = null != request.queryParams(PARAM_UNIQUE) && request.queryParams(PARAM_UNIQUE).equals("true");
        if (null == mode || mode.length() == 0) {
            mode = MODE_IPS_BY_REQUEST_COUNT;
        }
        if (MODE_TOKEN_RETRIEVAL_RESULTS.equals(mode)) {
            return getTokenRetrievalResults();
        } else if (Server.stats.getIps().isEmpty()) {
            return "No stats recorded yet";
        } else if (MODE_REQUESTS_BY_DAY.equals(mode)) {
            return unique ? getRequestsByDayUnique() : getRequestsByDay();
        } else if (MODE_RATE_LIMIT_HITS.equals(mode)) {
            return getRateLimitHits();
        } else if (MODE_TOTAL_RATE_LIMIT_HITS.equals(mode)) {
            return getTotalRateLimitHits();
        } else if (MODE_TOTAL.equals(mode)) {
            return unique ? getTotalRequestsUnique() : getTotalRequests();
        } else {
            return getRequestsByIp();
        }
    }

    public String delete(Request request, Response response) {
        Server.stats.clear();
        return "Stats cleared";
    }

    private String getRequestsByDay() {
        Map<String, Integer> requestsByDay = new TreeMap<>();
        for (List<Long> timestamps: Server.stats.getIps().values()) {
            for (Long timestamp: timestamps) {
                if (null == timestamp || timestamp == 0) {
                    continue;
                }
                String day = dateFormatter.format(new Date(timestamp));
                if (!requestsByDay.containsKey(day)) {
                    requestsByDay.put(day, 0);
                }
                requestsByDay.put(day, requestsByDay.get(day) + 1);
            }
        }
        return mapToString(requestsByDay);
    }

    private String getRequestsByDayUnique() {
        Map<String, Set<Long>> ipsByDay = new HashMap<>();
        for (long ip: Server.stats.getIps().keySet()) {
            for (Long timestamp: Server.stats.getIps().get(ip)) {
                if (null == timestamp || timestamp == 0) {
                    continue;
                }
                String day = dateFormatter.format(new Date(timestamp));
                if (!ipsByDay.containsKey(day)) {
                    ipsByDay.put(day, new HashSet<>());
                }
                ipsByDay.get(day).add(ip);
            }
        }
        Map<String, Integer> requestsByDay = new TreeMap<>();
        for (String day: ipsByDay.keySet()) {
            requestsByDay.put(day, ipsByDay.get(day).size());
        }
        return mapToString(requestsByDay);
    }

    private String getTotalRequests() {
        int total = 0;
        for (List<Long> timestamps: Server.stats.getIps().values()) {
            total += timestamps.size();
        }
        return Integer.toString(total);
    }

    private String getTotalRequestsUnique() {
        return Integer.toString(Server.stats.getIps().keySet().size());
    }

    private String getTotalRateLimitHits() {
        int total = 0;
        for (long ip: Server.stats.getRateLimitHits().keySet()) {
            total += Server.stats.getRateLimitHits().get(ip);
        }
        return Integer.toString(total);
    }

    private String getRequestsByIp() {
        Map<String, Integer> requestsByIp = new HashMap<>();
        for (long ip: Server.stats.getIps().keySet()) {
            requestsByIp.put(Server.longToIp(ip), Server.stats.getIps().get(ip).size());
        }
        try {
            return mapToString(sortByValue(requestsByIp, false));
        } catch (Throwable e) {
            Server.LOG.error(e.getMessage(), e);
            return "";
        }
    }

    private String getRateLimitHits() {
        Map<String, Integer> rateLimitHits = new HashMap<>();
        for (long ip: Server.stats.getRateLimitHits().keySet()) {
            rateLimitHits.put(Server.longToIp(ip), Server.stats.getRateLimitHits().get(ip));
        }
        return mapToString(sortByValue(rateLimitHits, false));
    }

    private String getTokenRetrievalResults() {
        return mapToString(Server.stats.getTokenRetrievalResults());
    }

    private String mapToString(Map map) {
        StringBuilder sb = new StringBuilder();
        for (Object key: map.keySet()) {
            sb.append(key).append("\t\t").append(map.get(key)).append("\n");
        }
        return sb.toString();
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean asc) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        if (!asc) {
            Collections.reverse(list);
        }
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
