package com.wishtoday.ts.simpleminer.schedule;

import com.wishtoday.simpleservices.services.annotation.Service;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class TickScheduler {
    private final Deque<Task> currentTick;
    private final Deque<Task> nextTick;

    public TickScheduler() {
        this.currentTick = new ArrayDeque<>();
        this.nextTick = new ArrayDeque<>();
    }

    public void tick() {
        while (!this.currentTick.isEmpty()) {
            Task task = this.currentTick.poll();
            task.run();
        }
        //currentTick.clear();
        this.currentTick.addAll(nextTick);
        nextTick.clear();
    }

    public void scheduleNextTick() {
        this.nextTick.add(this.currentTick.poll());
    }

    public void scheduleCurrentTick() {
        this.currentTick.add(this.currentTick.poll());
    }
}
