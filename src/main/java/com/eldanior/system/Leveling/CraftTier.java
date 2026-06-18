package com.eldanior.system.Leveling;

public enum CraftTier {
    NOVICE(0, 0.0),
    APPRENTI(500, 0.0),
    COMPAGNON(2000, 0.05),
    EXPERT(5000, 0.08),
    MAITRE(10000, 0.10);

    private final int requiredProcs;
    private final double doubleCraftChance;

    CraftTier(int requiredProcs, double doubleCraftChance) {
        this.requiredProcs = requiredProcs;
        this.doubleCraftChance = doubleCraftChance;
    }

    public int getRequiredProcs() { return requiredProcs; }
    public double getDoubleCraftChance() { return doubleCraftChance; }

    public static CraftTier fromProcs(int procs) {
        if (procs >= MAITRE.requiredProcs) return MAITRE;
        if (procs >= EXPERT.requiredProcs) return EXPERT;
        if (procs >= COMPAGNON.requiredProcs) return COMPAGNON;
        if (procs >= APPRENTI.requiredProcs) return APPRENTI;
        return NOVICE;
    }
}
