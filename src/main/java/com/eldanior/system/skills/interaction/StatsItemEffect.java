package com.eldanior.system.skills.interaction;

import java.util.ArrayList;
import java.util.List;

public class StatsItemEffect {

    private final String displayName;
    private final List<StatEntry> entries;

    public StatsItemEffect(String displayName, StatType statType, int value) {
        this.displayName = displayName;
        this.entries = List.of(new StatEntry(statType, value, null));
    }

    private StatsItemEffect(String displayName, List<StatEntry> entries) {
        this.displayName = displayName;
        this.entries = List.copyOf(entries);
    }

    public String getDisplayName() { return displayName; }
    public List<StatEntry> getEntries() { return entries; }

    // Builder pour les effets multi-stats
    public static Builder builder(String displayName) { return new Builder(displayName); }

    // Builder pour les effets rank (string value)
    public static StatsItemEffect rank(String displayName, StatType rankType, String rankValue) {
        return new StatsItemEffect(displayName, List.of(new StatEntry(rankType, 0, rankValue)));
    }

    public static class StatEntry {
        private final StatType statType;
        private final int value;
        private final String stringValue; // Pour les rangs

        public StatEntry(StatType statType, int value, String stringValue) {
            this.statType = statType;
            this.value = value;
            this.stringValue = stringValue;
        }

        public StatType getStatType() { return statType; }
        public int getValue() { return value; }
        public String getStringValue() { return stringValue; }
    }

    public static class Builder {
        private final String displayName;
        private final List<StatEntry> entries = new ArrayList<>();

        private Builder(String displayName) { this.displayName = displayName; }

        public Builder add(StatType stat, int value) {
            entries.add(new StatEntry(stat, value, null));
            return this;
        }

        public StatsItemEffect build() {
            return new StatsItemEffect(displayName, entries);
        }
    }
}
