package com.eldanior.system.locks;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

/**
 * Serrure d'une porte, lue dans l'identifiant du bloc : Eldanior_Porte_Famille_X s'ouvre avec Eldanior_Cle_Famille_X,
 * Eldanior_Porte_Boss_X avec Eldanior_Cle_Boss_X. Aucun registre de positions : une porte fonctionne donc dans un prefab ou une
 * instance de donjon, dont le monde porte un nom genere. Les objets viennent de scripts/portes/gen_portes.py.
 *
 * @param family  porte de salle au tresor ; sinon porte de boss
 * @param code    identifiant de la famille (minuscules) ou palier de rang (ED, CB, A, S)
 * @param keyItem objet qui ouvre la porte
 */
public record DoorLock(boolean family, String code, String keyItem) {

    private static final String FAMILY_DOOR = "Eldanior_Porte_Famille_";
    private static final String BOSS_DOOR = "Eldanior_Porte_Boss_";
    static final String FAMILY_KEY = "Eldanior_Cle_Famille_";
    private static final String BOSS_KEY = "Eldanior_Cle_Boss_";
    /** Une porte ouverte est une variante d'etat : *Eldanior_Porte_Boss_A_State_Definitions_OpenDoorIn. */
    private static final String STATE_MARK = "_State_Definitions_";

    /** Serrure du bloc, null si ce n'est pas une porte verrouillee. */
    public static DoorLock of(BlockType type) {
        if (type == null || type.getId() == null || !type.getId().contains("Eldanior_Porte_")) return null;
        String id = baseId(type.getId());
        if (id.startsWith(FAMILY_DOOR)) {
            String family = id.substring(FAMILY_DOOR.length());
            return new DoorLock(true, family.toLowerCase(), FAMILY_KEY + family);
        }
        if (id.startsWith(BOSS_DOOR)) {
            String tier = id.substring(BOSS_DOOR.length());
            return new DoorLock(false, tier, BOSS_KEY + tier);
        }
        return null;
    }

    /** Cle d'une famille, a partir de son identifiant (runkandel -> Eldanior_Cle_Famille_Runkandel). */
    static String familyKey(String familyId) {
        return FAMILY_KEY + Character.toUpperCase(familyId.charAt(0)) + familyId.substring(1);
    }

    /** Libelle du rang d'une porte de boss, tel qu'affiche sur l'objet (ED -> E-D). */
    String tierLabel() {
        return code.length() == 2 ? code.charAt(0) + "-" + code.charAt(1) : code;
    }

    static String baseId(String id) {
        int mark = id.indexOf(STATE_MARK);
        String base = mark < 0 ? id : id.substring(0, mark);
        return base.startsWith("*") ? base.substring(1) : base;
    }
}
