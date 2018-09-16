package com.github.yeriomin.tokendispenser;

import java.util.*;

public class ExpiringList<T> {

    private long keepFor;
    protected Map<Long, T> timestampedValues = new TreeMap<>();

    public void setKeepFor(long keepFor) {
        this.keepFor = keepFor;
    }

    public void add(T t) {
        timestampedValues.put(System.currentTimeMillis(), t);
        cleanup();
    }

    synchronized protected void cleanup() {
        timestampedValues.keySet().removeIf(e -> e + keepFor < System.currentTimeMillis());
    }
}
