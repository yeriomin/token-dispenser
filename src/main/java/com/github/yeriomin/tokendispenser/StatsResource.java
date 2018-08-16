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
    static private final String MODE_TOTAL = "total";

    public String get(Request request, Response response) {
        String mode = request.queryParams(PARAM_MODE);
        boolean unique = null != request.queryParams(PARAM_UNIQUE) && request.queryParams(PARAM_UNIQUE).equals("true");
        if (null == mode || mode.length() == 0) {
            mode = MODE_IPS_BY_REQUEST_COUNT;
        }
        try {
            if (Server.ips.isEmpty()) {
                return "No stats recorded yet";
            } else if (MODE_REQUESTS_BY_DAY.equals(mode)) {
                return unique ? getRequestsByDayUnique() : getRequestsByDay();
            } else if (MODE_RATE_LIMIT_HITS.equals(mode)) {
                return getRateLimitHits();
            } else if (MODE_TOTAL.equals(mode)) {
                return unique ? getTotalRequestsUnique() : getTotalRequests();
            } else {
                return getRequestsByIp();
            }
        } catch (Throwable e) {
            Server.LOG.error(e.getMessage(), e);
            return e.getMessage();
        }
    }

    public String delete(Request request, Response response) {
        Server.ips.clear();
        Server.rateLimitHits.clear();
        return "Stats cleared";
    }

    private String getRequestsByDay() {
        Map<String, Integer> requestsByDay = new LinkedHashMap<>();
        for (List<Long> timestamps: Server.ips.values()) {
            for (Long timestamp: timestamps) {
                String day = new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp));
                if (!requestsByDay.containsKey(day)) {
                    requestsByDay.put(day, 0);
                }
                requestsByDay.put(day, requestsByDay.get(day) + 1);
            }
        }
        return mapToString(requestsByDay);
    }

    private String getRequestsByDayUnique() {
        Map<String, Set<String>> ipsByDay = new HashMap<>();
        for (String ip: Server.ips.keySet()) {
            for (Long timestamp: Server.ips.get(ip)) {
                String day = new SimpleDateFormat("yyyy-MM-dd").format(new Date(timestamp));
                if (!ipsByDay.containsKey(day)) {
                    ipsByDay.put(day, new HashSet<>());
                }
                ipsByDay.get(day).add(ip);
            }
        }
        Map<String, Integer> requestsByDay = new LinkedHashMap<>();
        for (String day: ipsByDay.keySet()) {
            requestsByDay.put(day, ipsByDay.get(day).size());
        }
        return mapToString(requestsByDay);
    }

    private String getTotalRequests() {
        int total = 0;
        for (List<Long> timestamps: Server.ips.values()) {
            total += timestamps.size();
        }
        return Integer.toString(total);
    }

    private String getTotalRequestsUnique() {
        return Integer.toString(Server.ips.keySet().size());
    }

    private String getRequestsByIp() {
        Map<String, Integer> requestsByIp = new HashMap<>();
        for (String ip: Server.ips.keySet()) {
            requestsByIp.put(ip, Server.ips.get(ip).size());
        }
        try {
            return mapToString(sortByValue(requestsByIp, false));
        } catch (Throwable e) {
            Server.LOG.error(e.getMessage(), e);
            return "";
        }
    }

    private String getRateLimitHits() {
        return mapToString(sortByValue(Server.rateLimitHits, false));
    }

    private String mapToString(Map map) {
        StringBuilder sb = new StringBuilder();
        for (Object key: map.keySet()) {
            sb.append(key).append("\t\t").append(map.get(key)).append("\n");
        }
        return sb.toString();
    }

    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return sortByValue(map, true);
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
