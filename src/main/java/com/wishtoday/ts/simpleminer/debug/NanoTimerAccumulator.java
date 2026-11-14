package com.wishtoday.ts.simpleminer.debug;

import lombok.Getter;

public class NanoTimerAccumulator {
    @Getter
    private long accumulated = 0;
    private long _t = 0;

    public void consume(long _t) {accumulated += _t;}
    public void visitStart() {
        this._t = System.nanoTime();
    }
    public void visitEnd() {
        long l = System.nanoTime() - this._t;
        this._t = 0;
        this.consume(l);
    }

    @Override
    public String toString() {
        return this.getAccumulated() + "";
    }
}
