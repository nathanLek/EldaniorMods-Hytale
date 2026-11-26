package com.eldanior.system.failles;

/**
 * Rangs des failles (docs/plan-failles-donjons.md, decisions du 2026-11-22/23). Bandes de niveau et couleurs : celles des quetes
 * (quest/QuestDifficulty, skill quest-creation § 3), sauf F en BLANC (demande de l'utilisateur). La faille grandit avec le rang
 * (blocs Eldanior_Faille_<Rang>, scripts/failles/gen_failles.py). Recompense de fermeture : reperes des quetes du meme rang.
 */
public enum FailleRank {
    F("F", "#FFFFFF", 1, 60, 300, 100, "donjon_common"),
    E("E", "#4CAF50", 60, 150, 800, 300, "donjon_tier1"),
    D("D", "#2196F3", 150, 300, 1_500, 800, "donjon_tier1"),
    C("C", "#9C27B0", 300, 450, 3_000, 2_000, "donjon_tier2"),
    B("B", "#ff9800", 450, 600, 6_000, 5_000, "donjon_tier2"),
    A("A", "#cc4444", 600, 800, 12_000, 12_000, "donjon_tier3"),
    S("S", "#FFD700", 800, 999, 25_000, 30_000, "donjon_tier4");

    public final String label, color, chestTable;
    public final int minLevel, maxLevel, xp;
    public final long money;

    FailleRank(String label, String color, int minLevel, int maxLevel, int xp, long money, String chestTable) {
        this.label = label; this.color = color; this.minLevel = minLevel; this.maxLevel = maxLevel; this.xp = xp; this.money = money;
        this.chestTable = chestTable;
    }

    public String blockId() { return "Eldanior_Faille_" + label; }

    public String markerImage() { return "Eldanior_Faille_" + label + ".png"; }

    public String compassImage() { return "Eldanior_Faille_" + label + "_Boussole.png"; }

    /** Rang d'un joueur selon son niveau (memes bandes que les quetes). */
    public static FailleRank forPlayerLevel(int level) {
        FailleRank r = F;
        for (FailleRank k : values()) if (level >= k.minLevel) r = k;
        return r;
    }

    public static FailleRank of(String s) {
        if (s == null) return null;
        for (FailleRank k : values()) if (k.label.equalsIgnoreCase(s.trim())) return k;
        return null;
    }

    /** Niveau d'un monstre prevu pour le rang `from`, ramene dans la bande de ce rang (faille ouverte a un autre rang). */
    public int remap(int level, FailleRank from) {
        if (from == this) return level;
        double t = (level - from.minLevel) / (double) Math.max(1, from.maxLevel - from.minLevel);
        return (int) Math.round(minLevel + Math.max(0, Math.min(1.2, t)) * (maxLevel - minLevel));
    }
}
