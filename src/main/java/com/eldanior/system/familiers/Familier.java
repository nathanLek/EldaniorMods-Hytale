package com.eldanior.system.familiers;

import com.eldanior.system.familiers.Espece.Age;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

/**
 * Le familier d'un joueur (un seul par maitre). Sauvegarde dans eldanior_data/familiers.json par FamilierManager.
 * Cycle : oeuf pose (incubation) -> eclos (il attend son maitre au nid, puis le suit) -> grandit avec le temps (Jeune,
 * Adolescent, Adulte) et monte de niveau en combattant. Mort : l'oeuf est detruit et le lien rompu (FamilierManager.die).
 */
public final class Familier {

    public final UUID owner;
    String ownerName;
    final Espece espece;
    String name;
    int level = 1;
    long xp;
    /** Element tire a l'eclosion selon la rarete de l'espece (null : bete commune ou inhabituelle, ou pas encore eclos). */
    Element element;
    /** Competence recue a l'evolution adulte (null : pas encore evoluee ; aucune competence avant, 2026-11-13). */
    Competence skill;

    /** Fin de l'incubation (> 0 : l'oeuf pose est en incubation). */
    long incubationEnd;
    /** Incubation restante d'un oeuf ramasse avant d'eclore (reprise a la pose). */
    long incubationLeft;
    /** Eclosion (0 : pas encore eclos). L'age se compte depuis l'eclosion. */
    long hatchedAt;

    /** Oeuf pose (eggWorld null : l'oeuf est dans un inventaire, le familier y est retire). */
    String eggWorld;
    int eggX, eggY, eggZ;

    /** Suit son maitre ; sinon il reste a stay*. waiting : il attend son maitre au nid (eclosion, oeuf repose). */
    boolean following;
    boolean waiting = true;
    String stayWorld;
    double stayX, stayY, stayZ;
    float hpRatio = 1f;

    // ===== Session (non sauvegarde) =====
    volatile UUID entity;
    volatile String entityWorld;
    Age spawnedAge;
    int appliedLevel = -1;
    long nextSpawnTry, lastCombat, cdElement, cdGate, fedAt;
    volatile Ref<EntityStore> lastTarget;
    volatile boolean queued;
    /** Assis pres de son maitre (il ne le suit plus tant qu'il reste a moins de 5 blocs). */
    boolean sitting;

    Familier(UUID owner, String ownerName, Espece espece) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.espece = espece;
        this.name = espece.label;
    }

    public Espece espece() { return espece; }

    public String name() { return name; }

    public int level() { return level; }

    boolean hasEgg() { return eggWorld != null; }

    boolean hatched() { return hatchedAt > 0; }

    boolean incubating() { return !hatched(); }

    /** Age selon le temps ecoule depuis l'eclosion. */
    Age age(long now) {
        if (!hatched()) return Age.JEUNE;
        long lived = now - hatchedAt;
        if (lived >= FamilierManager.adulteAt()) return Age.ADULTE;
        if (lived >= FamilierManager.adolescentAt()) return Age.ADOLESCENT;
        return Age.JEUNE;
    }

    float maxHp(Age age) { return espece.baseHp[age.ordinal()] * (1f + 0.07f * (level - 1)); }

    float damage(Age age) { return espece.baseDamage[age.ordinal()] * (1f + 0.045f * (level - 1)); }

    /** Meme courbe que les joueurs (PlayerLevelData.getRequiredExperience). */
    long xpToNext() { return (long) (10 + 14.0 * (level - 1) + 0.5 * (level - 1) * (level - 1)); }

    int maxLevel() { return espece.maxLevel(); }

    String eggCoords() { return eggX + " " + eggY + " " + eggZ; }
}
