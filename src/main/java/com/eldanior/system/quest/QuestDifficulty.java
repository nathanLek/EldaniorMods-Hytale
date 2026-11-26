package com.eldanior.system.quest;

public enum QuestDifficulty {
    F("F", "#8899aa", 1.0),
    E("E", "#4CAF50", 1.2),
    D("D", "#2196F3", 1.5),
    C("C", "#9C27B0", 2.0),
    B("B", "#ff9800", 2.5),
    A("A", "#cc4444", 3.5),
    S("S", "#FFD700", 5.0),
    SS("SS", "#ff7a3d", 7.0),                                   // monstres au-dela du niveau 999 (2026-11-16)
    EX("EX", "#c46bff", 10.0);                                  // monstres au-dela du niveau 1500 (ex-SSS, renomme le 2026-11-19)

    private final String displayName;
    private final String color;
    private final double rewardMultiplier;

    QuestDifficulty(String displayName, String color, double rewardMultiplier) {
        this.displayName = displayName;
        this.color = color;
        this.rewardMultiplier = rewardMultiplier;
    }

    /** Seuil des rangs de monstre au-dela du niveau maximum des joueurs (999) : SS jusqu'a SS_MAX, EX au-dessus. */
    public static final int SS_MIN = 1000, SS_MAX = 1500;

    /**
     * Rang d'un monstre trop fort pour l'echelle F-S (2026-11-16, monstres de contrat et boss de donjon) : SS du niveau 1000 au
     * niveau 1500, EX au-dessus ; null en dessous (le rang ordinaire s'applique).
     */
    public static QuestDifficulty forMonsterLevel(int level) {
        return level > SS_MAX ? EX : level >= SS_MIN ? SS : null;
    }

    /** Le plus haut des deux rangs (null ignore). */
    public static QuestDifficulty max(QuestDifficulty a, QuestDifficulty b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    /** S, SS ou EX : rangs dores (contour or des cartes). */
    public boolean atLeastS() { return ordinal() >= S.ordinal(); }

    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public double getRewardMultiplier() { return rewardMultiplier; }
}
