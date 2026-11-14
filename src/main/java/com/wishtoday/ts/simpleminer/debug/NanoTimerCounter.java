package com.wishtoday.ts.simpleminer.debug;

public class NanoTimerCounter {
    private long startTime;
    private long endTime;

    public NanoTimerCounter() {
    }

    public void startTimer() {
        this.startTime = System.nanoTime();
    }

    public void endTimer() {
        this.endTime = System.nanoTime();
    }
    public long getElapsedTime() {
        return this.endTime - this.startTime;
    }
}
