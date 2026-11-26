package com.eldanior.system.guards;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.luo.LuoClans;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Rang martial des PNJ de poste qui combattent (decision du 2026-11-10, docs/plan-clan-luo.md, C) : gardes de ville, gardes
 * royaux, patrouilleurs, chevaliers, chevaliers errants, assassins et disciples. Les civils (habitants, commercants, marchands,
 * forgerons), les mages royaux et les aventuriers, chasseurs de prime et criminels (qui ont deja leur rang d'aventure) n'en ont pas.
 *
 * Niveau martial : celui du poste s'il est regle (Post.level, champ Niveau de la fenetre du poste), sinon tire de la graine du
 * poste dans la fourchette de son type (disciple 20-60, garde et patrouilleur 60-180, garde royal 180-300, chevalier errant
 * 200-400, chevalier 280-450, assassin 300-500). Rang = classe de la voie martiale atteinte a ce niveau (Apprenti 40,
 * Disciple 100, Expert 180, Maitre 280, Grand Maitre 400, Saint 550, Maitre Supreme 750, Dieu Martial 999) ; il remplace le
 * role dans le nom affiche (« Haruki Luo - Expert Martial »). En dessous de 40 : pas de rang, le role reste affiche.
 * Le combat (aura de Ki, techniques du rang et du clan) est dans skills.runes.spells.MartialNpcs.
 */
public final class MartialRanks {

    private MartialRanks() {}

    /** Niveau minimal de chaque palier (1 Apprenti ... 8 Dieu Martial). */
    private static final int[] THRESHOLDS = {40, 100, 180, 280, 400, 550, 750, 999};

    /** true si ce type de PNJ a un rang martial. */
    public static boolean applies(GuardType t) {
        if (t == null) return false;
        return switch (t) {
            case VILLE, ROYAL, CHEVALIER, PATROUILLEUR, CHEVALIER_ERRANT, ASSASSIN, DISCIPLE -> true;
            default -> false;
        };
    }

    /**
     * Fourchette de niveau martial tiree pour un type (quand le poste n'a pas de niveau regle), decidee le 2026-11-10 :
     * patrouilleur Apprenti ou Disciple, garde Disciple ou Expert, chevalier (et chevalier errant) Expert ou Maitre, garde royal
     * Grand Maitre ou Saint ; assassin Maitre ou Grand Maitre ; disciple du dojo debutant ou Apprenti.
     */
    private static int[] range(GuardType t) {
        return switch (t) {
            case DISCIPLE -> new int[]{40, DISCIPLE_MAX};                  // 2026-11-13 : Apprenti a Grand Maitre
            case PATROUILLEUR -> new int[]{40, 179};
            case CHEVALIER, CHEVALIER_ERRANT -> new int[]{180, 399};
            case ASSASSIN -> new int[]{280, 549};
            case ROYAL -> new int[]{400, 749};
            default -> new int[]{100, 279};                                   // gardes de ville
        };
    }

    /**
     * Sensei (decision du 2026-11-10) : au minimum Grand Maitre Martial, au plus Maitre Supreme (niveau 400 a 998) ; seule
     * exception au plafond du Saint Martial. Niveau du poste s'il est regle (ramene dans la fourchette), sinon tire de la graine.
     */
    public static final int SENSEI_MIN = 400, SENSEI_MAX = 998;

    /** Niveau martial du Sensei de ce poste. */
    public static int senseiLevel(GuardManager.Post p) {
        if (p == null) return SENSEI_MIN;
        if (p.level > 0) return Math.clamp(p.level, SENSEI_MIN, SENSEI_MAX);
        return SENSEI_MIN + (int) Math.floorMod(p.skinSeed >> 7, (long) (SENSEI_MAX - SENSEI_MIN + 1));
    }

    /** Niveau martial du PNJ de ce poste (0 si le type n'a pas de rang), plafonne au Saint Martial (749), sauf le Sensei. */
    public static int level(GuardManager.Post p) {
        if (p != null && p.type == GuardType.SENSEI) return senseiLevel(p);
        if (p == null || !applies(p.type)) return 0;
        if (p.level > 0) return Math.min(p.level, p.type == GuardType.DISCIPLE ? DISCIPLE_MAX : NPC_MAX_LEVEL);
        int[] r = range(p.type);
        return r[0] + (int) Math.floorMod(p.skinSeed >> 7, (long) (r[1] - r[0] + 1));
    }

    /** Un disciple va jusqu'au Grand Maitre Martial (demande du 2026-11-13). */
    public static final int DISCIPLE_MAX = 549;

    /** Aucun PNJ au-dela du Saint Martial (decision du 2026-11-10). */
    public static final int NPC_MAX_LEVEL = 749;

    /** Palier de la voie martiale atteint a ce niveau (0 sous 40). */
    public static int tier(int level) {
        int t = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) if (level >= THRESHOLDS[i]) t = i + 1;
        return t;
    }

    public static int tier(GuardManager.Post p) { return tier(level(p)); }

    /** Nom du rang martial du PNJ (« Expert Martial »), null s'il n'en a pas. */
    public static String rankName(GuardManager.Post p) {
        int t = tier(p);
        return t > 0 ? VoieMartiale.className(t) : null;
    }

    /** Clan de l'Empire Luo du PNJ (nom de famille affiche), null sinon. */
    public static LuoClans.Clan clan(GuardManager.Post p) {
        return p != null ? LuoClans.byName(p.family) : null;
    }

    /** Poste du PNJ s'il a un rang martial (palier 1 ou plus), sinon null. */
    public static GuardManager.Post martialPost(Ref<EntityStore> ref, ComponentAccessor<EntityStore> store) {
        GuardManager.Post p = GuardManager.postOf(ref, store);
        return p != null && tier(p) > 0 ? p : null;
    }
}
