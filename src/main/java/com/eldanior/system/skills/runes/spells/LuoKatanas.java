package com.eldanior.system.skills.runes.spells;

import java.util.List;

/**
 * Katanas du Clan Luo : tous fonctionnent comme Tenryumaru (modeles, animations et objets generes par
 * scripts/artefacts/gen_luo_katanas.py et gen_tenryumaru.py). Chaque katana existe en quatre objets :
 *   - id          : lame au fourreau (rien dans la main droite) ;
 *   - drawn       : lame au clair dans la main droite ;
 *   - sheath      : fourreau plein (objet utilitaire, main gauche), pose avec la forme au fourreau ;
 *   - sheathEmpty : fourreau vide, pose avec la forme au clair.
 * TenryumaruSystem echange les formes et pose le bon fourreau ; Tenryumaru.Degaine / Rengaine passent d'une forme a l'autre.
 *
 * Reglages des katanas ordinaires (Tenryumaru a ses propres sorts : Flash Celeste, Dix Serments ; Kinryu-O aussi : Souffle du
 * Dragon d'Or, Eclipse du Dieu-Dragon) :
 *   - iai (attaque maintenue, sort LuoIai) : coupe devant le porteur, iaiDamage, et lame d'air de la couleur du katana
 *     (iaiBlade, cle de Tenryumaru_Lame_<cle>) a partir de Rare ;
 *   - iaigiri (signature, sort LuoIaigiri) : eclair sur iaigiriTargets ennemis a iaigiriRadius blocs (arrive degats a chaque
 *     passage), retour, rengainage, puis burstHits slashs de burstDamage sur chaque ennemi touche (effet burstEffect).
 */
public final class LuoKatanas {

    private LuoKatanas() {}

    public record Katana(String id, String drawn, String sheath, String sheathEmpty,
                         float iaiDamage, String iaiBlade, float iaiBladeDamage,
                         int iaigiriTargets, double iaigiriRadius, float arrive, int burstHits, float burstDamage, String burstEffect) {}

    private static Katana luo(String id, float iaiDamage, String iaiBlade, float iaiBladeDamage, int targets, double radius,
                              float arrive, int hits, float burst, String effect) {
        return new Katana(id, id + "_Degainee", id + "_Fourreau", id + "_Fourreau_Vide",
                iaiDamage, iaiBlade, iaiBladeDamage, targets, radius, arrive, hits, burst, effect);
    }

    public static final List<Katana> ALL = List.of(
            new Katana(Tenryumaru.ITEM, Tenryumaru.ITEM_DRAWN, TenryumaruSystem.SHEATH, TenryumaruSystem.SHEATH_EMPTY,
                    0, null, 0, 10, 14, 35, 10, 22, null),
            luo("Eldanior_Weapon_Sword_Katana_Bokken", 14, null, 0, 1, 6, 8, 3, 3, null),
            luo("Eldanior_Weapon_Sword_Katana_Fer", 26, null, 0, 2, 8, 14, 3, 6, null),
            luo("Eldanior_Weapon_Sword_Katana_Aonami", 36, "Aonami_H", 30, 3, 10, 22, 5, 8, null),
            luo("Eldanior_Weapon_Sword_Katana_Tsukikage", 52, "Tsukikage_H", 42, 4, 11, 32, 6, 10, "Arme_Givre"),
            luo("Artefact_Sword_Katana_Shirasagi", 70, "Shirasagi_H", 50, 5, 12, 42, 8, 12, "Apollo_Aveugle"),
            luo("Artefact_Sword_Katana_Hien", 73, "Hien_H", 60, 5, 12, 44, 8, 13, "Arme_Embrasement"),
            // katanas uniques des clans Sama et Kage (2026-11-10) ; Hanazakura = Hien, Setsuka = Shirasagi
            luo("Artefact_Sword_Katana_Gotetsu", 82, "Gotetsu_H", 66, 4, 11, 50, 6, 16, null),
            luo("Artefact_Sword_Katana_Yamikiri", 70, "Yamikiri_H", 54, 6, 13, 40, 9, 11, "Arme_Venin"),
            // Kinryu-O : maintien et signature propres (Kinryu.Souffle, Kinryu.Eclipse) ; reglages ci-dessous inutilises
            luo(Kinryu.ITEM, 170, "Kinryu_H", 120, 10, 16, 60, 10, 30, null));

    /** Katana dont id est la forme au fourreau ou au clair, sinon null. */
    public static Katana of(String id) {
        if (id == null) return null;
        for (Katana k : ALL) if (k.id.equals(id) || k.drawn.equals(id)) return k;
        return null;
    }

    public static boolean isSheathed(String id) {
        Katana k = of(id);
        return k != null && k.id.equals(id);
    }

    public static boolean isDrawn(String id) {
        Katana k = of(id);
        return k != null && k.drawn.equals(id);
    }

    /** true pour un fourreau (plein ou vide) de n'importe quel katana. */
    public static boolean isSheath(String id) {
        if (id == null) return false;
        for (Katana k : ALL) if (k.sheath.equals(id) || k.sheathEmpty.equals(id)) return true;
        return false;
    }

    /** Fourreau a porter avec l'objet tenu (plein avec la forme au fourreau, vide avec la forme au clair), sinon null. */
    public static String sheathFor(String heldId) {
        Katana k = of(heldId);
        if (k == null) return null;
        return k.id.equals(heldId) ? k.sheath : k.sheathEmpty;
    }

    /** Modele d'exposition (presentoir) : la forme au fourreau n'a rien en main, on montre le katana rengaine. Sinon null. */
    public static String displayModel(String itemId) {
        Katana k = of(itemId);
        return k == null ? null : "Items/Weapons/artefacts/" + k.id + "_Presentoir.blockymodel";
    }

    public static String displayTexture(String itemId) {
        Katana k = of(itemId);
        return k == null ? null : "Items/Weapons/artefacts/" + k.id + ".png";
    }

    /** Toutes les formes et fourreaux d'un katana Luo (reserves aux membres du clan). */
    public static boolean isClanItem(String id) {
        return of(id) != null || isSheath(id);
    }
}
