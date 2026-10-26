package com.eldanior.system.guild;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ameliorations permanentes d'une guilde, achetees par son chef chez le Maitre de guilde avec le tresor de la guilde
 * (cout : base x niveau vise). Sauvegardees avec la guilde (PersistenceManager, cle "upgrades").
 *   - Sagesse : +{@link #XP_PER_LEVEL} % d'XP des monstres pour chaque membre (DeathXPSystem) ;
 *   - Fortune : +{@link #GOLD_PER_LEVEL} % d'or (pieces lachees par les monstres, recompenses des missions) ;
 *   - Effectifs : +{@link #SLOTS_PER_LEVEL} places de membres (Guild.getMaxMembers).
 */
public enum GuildUpgrade {
    SAGESSE("Sagesse", "d'XP pour tous les membres", 5, 50_000),
    FORTUNE("Fortune", "d'or pour tous les membres", 5, 50_000),
    EFFECTIFS("Effectifs", "places de membres", 4, 40_000);

    public static final int XP_PER_LEVEL = 3, GOLD_PER_LEVEL = 3, SLOTS_PER_LEVEL = 5, BASE_MEMBERS = 15;

    private final String label, effect;
    private final int maxLevel;
    private final long baseCost;

    GuildUpgrade(String label, String effect, int maxLevel, long baseCost) {
        this.label = label; this.effect = effect; this.maxLevel = maxLevel; this.baseCost = baseCost;
    }

    public String getLabel() { return label; }
    public int getMaxLevel() { return maxLevel; }

    /** Cout du niveau suivant (tresor de la guilde). */
    public long nextCost(int level) { return baseCost * (level + 1); }

    /** Effet a ce niveau : "+6 % d'XP pour tous les membres", "+10 places de membres". */
    public String describe(int level) {
        return switch (this) {
            case SAGESSE -> "+" + level * XP_PER_LEVEL + " % " + effect;
            case FORTUNE -> "+" + level * GOLD_PER_LEVEL + " % " + effect;
            case EFFECTIFS -> "+" + level * SLOTS_PER_LEVEL + " " + effect;
        };
    }

    /** XP gagnee par un joueur, bonus de sa guilde compris. */
    public static int applyXp(UUID player, int xp) {
        Guild g = GuildManager.getPlayerGuild(player);
        return g == null ? xp : (int) Math.round(xp * (1 + g.getUpgrade(SAGESSE) * XP_PER_LEVEL / 100.0));
    }

    /** Or gagne par un joueur, bonus de sa guilde compris (arrondi au hasard : les petites sommes en profitent aussi). */
    public static long applyGold(UUID player, long gold) {
        Guild g = GuildManager.getPlayerGuild(player);
        if (g == null || g.getUpgrade(FORTUNE) == 0) return gold;
        double v = gold * (1 + g.getUpgrade(FORTUNE) * GOLD_PER_LEVEL / 100.0);
        long base = (long) Math.floor(v);
        return base + (ThreadLocalRandom.current().nextDouble() < v - base ? 1 : 0);
    }
}
