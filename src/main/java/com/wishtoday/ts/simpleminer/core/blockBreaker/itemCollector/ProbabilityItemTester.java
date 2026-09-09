package com.wishtoday.ts.simpleminer.core.blockBreaker.itemCollector;

import com.wishtoday.simpleservices.services.annotation.CreateConstruction;
import com.wishtoday.simpleservices.services.annotation.Service;
import com.wishtoday.ts.simpleminer.ItemStackKey;
import com.wishtoday.ts.simpleminer.Reloadable;
import com.wishtoday.ts.simpleminer.config.ServerConfig;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import net.minecraft.block.BlockState;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ProbabilityItemTester implements Reloadable {
    private final Map<BlockState, SampleEntry> samples;
    private int sampleCount = 45;

    @CreateConstruction
    public ProbabilityItemTester(ServerConfig config) {
        this.samples = new HashMap<>();
        this.reload(config);
    }

    @Override
    public boolean reload(ServerConfig config) {
        this.sampleCount = config.getSampleCollectSampleCount();
        return true;
    }

    public void initialize() {
        this.samples.clear();
    }

    public Object2IntOpenHashMap<ItemStackKey> end() {
        Object2IntOpenHashMap<ItemStackKey> temp = new Object2IntOpenHashMap<>();
        Set<Map.Entry<BlockState, SampleEntry>> set = this.samples.entrySet();
        for (Map.Entry<BlockState, SampleEntry> entry : set) {
            SampleEntry value = entry.getValue();
            if (!value.successful) continue;
            Set<Map.Entry<ItemStackKey, SampleEntry.Counter>> entries = value.counterMap.entrySet();
            int blockCount = value.blockCount;
            for (Map.Entry<ItemStackKey, SampleEntry.Counter> counterEntry : entries) {
                temp.addTo(counterEntry.getKey(), counterEntry.getValue().firstCount * blockCount);
            }
        }
        return temp;
    }

    public boolean shouldContinueGetDropped(BlockState state) {
        SampleEntry sampleEntry = this.samples.computeIfAbsent(state, s -> new SampleEntry(this.sampleCount));
        return !sampleEntry.successful;
    }

    public void incrementBlockCount(BlockState state, boolean toolFit) {
        if (!toolFit) return;
        SampleEntry sampleEntry = this.samples.computeIfAbsent(state, b -> new SampleEntry(this.sampleCount));
        sampleEntry.increaseBlockCount();
    }

    public void matchOneBlock(BlockState state, Object2IntOpenHashMap<ItemStackKey> map) {
        SampleEntry sampleEntry = this.samples.get(state);
        sampleEntry.matchOneBlockDropped(map);
    }

    private static class SampleEntry {
        @Getter
        private int blockCount;
        private int matchCount;
        private final int sampleCount;
        private final Map<ItemStackKey, Counter> counterMap;
        private boolean isStable;
        @Getter
        private boolean successful;
        private MatchState state;
        private int firstItemCount;

        private SampleEntry(int sampleCount) {
            this.sampleCount = sampleCount;
            this.blockCount = 0;
            this.matchCount = 0;
            this.counterMap = new HashMap<>();
            this.successful = false;
            this.isStable = true;
            this.firstItemCount = -1;
            this.state = MatchState.SAMPLE;
        }

        public void increaseBlockCount() {
            this.blockCount++;
        }

        private boolean isStable() {
            if (!isStable) return false;
            Collection<Counter> values = counterMap.values();
            for (Counter value : values) {
                if (value.admitted < this.sampleCount) return false;
            }
            return true;
        }

        public void matchOneBlockDropped(Object2IntOpenHashMap<ItemStackKey> map) {
            switch (this.state) {
                case SAMPLE -> {
                    this.matchCount++;
                    if (this.matchCount >= this.sampleCount) {
                        this.state = MatchState.MATCH;
                    }
                    if (!isStable) {
                        return;
                    }

                    Object2IntMap.FastEntrySet<ItemStackKey> entries = map.object2IntEntrySet();
                    if (this.firstItemCount == -1) {
                        this.firstItemCount = map.size();
                    }
                    if (map.size() != this.firstItemCount) {
                        this.isStable = false;
                        return;
                    }

                    for (Object2IntMap.Entry<ItemStackKey> entry : entries) {
                        ItemStackKey key = entry.getKey();
                        int intValue = entry.getIntValue();
                        boolean b = this.counterMap.computeIfAbsent(key, k -> new Counter(intValue))
                                .tryAdmit(intValue);
                        if (!b) {
                            this.isStable = false;
                        }
                    }
                }
                case MATCH -> {
                    this.successful = this.isStable();
                    this.state = MatchState.MATCH_END;
                }
                case MATCH_END -> {}
            }
        }

        @Getter
        private static class Counter {
            private final int firstCount;
            private int admitted;
            public Counter(int firstCount) {
                this.firstCount = firstCount;
                this.admitted = 1;
            }

            public boolean tryAdmit(int current) {
                if (this.firstCount == current) {
                    this.admitted++;
                    return true;
                }
                return false;
            }
        }

        private enum MatchState {
            SAMPLE,
            MATCH,
            MATCH_END
        }
    }
}
