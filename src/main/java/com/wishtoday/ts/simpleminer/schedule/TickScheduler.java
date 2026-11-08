package com.wishtoday.ts.simpleminer.schedule;

import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.lifecycle.Lifecycle;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class TickScheduler implements Lifecycle {
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

    public void scheduleNextTick(Task t) {
        this.nextTick.add(t);
    }

    public void scheduleCurrentTick(Task task) {
        this.currentTick.add(task);
    }

    @Override
    public void onTick(MinecraftServer server) {
        this.tick();
    }

    @Override
    public void onServerEnd(MinecraftServer server) {
        this.currentTick.clear();
        this.nextTick.clear();
    }
}
