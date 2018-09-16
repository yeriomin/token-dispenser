package com.github.yeriomin.tokendispenser;

public class ExpiringIpList extends ExpiringList<Long> {

    private int limit;

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean tooMany(long ipInQuestion) {
        cleanup();
        int count = 0;
        for (long ip: timestampedValues.values()) {
            if (ip == ipInQuestion) {
                count++;
            }
            if (count > limit) {
                return true;
            }
        }
        return false;
    }
}
