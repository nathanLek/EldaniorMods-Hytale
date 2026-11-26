package com.eldanior.system.presentoir;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

/**
 * Types de presentoir, lus sur le bloc : rien de plus a sauvegarder. Mesures en blocs au-dessus du bloc de base, sauf les tailles
 * d'objet, en unites de modele (64 = 1 bloc). Les mesures de bloc viennent de gen_presentoir.py (FLOOR, SMALL_FLOOR, FAMILY_FLOOR).
 *
 * @param roomHeight     hauteur disponible pour un objet
 * @param roomRadius     rayon de rotation libre
 * @param minSize        taille minimale a l'affichage : un objet plus petit est grossi
 * @param maxZoom        grossissement maximal
 * @param mannequinScale taille du mannequin, 0 si le presentoir ne porte pas d'armure
 */
enum StandKind {
    TEMPLE(6 / 32.0, 1.28, 2.3, 128, 36, 44, 3, 1),
    PETIT(20 / 32.0, 1.09, 1.52, 54, 28, 40, 3, 0),
    FAMILLE(8 / 32.0, 1.6, 2.7, 150, 56, 124, 3, 1.15f);

    private static final String SMALL_ID = PresentoirManager.BLOCK_ID + "_Petit";
    private static final String FAMILY_ID = PresentoirManager.BLOCK_ID + "_Famille_";

    final double floor, center, top;
    final float roomHeight, roomRadius, minSize, maxZoom, mannequinScale;

    StandKind(double floor, double center, double top, float roomHeight, float roomRadius, float minSize, float maxZoom, float mannequinScale) {
        this.floor = floor;
        this.center = center;
        this.top = top;
        this.roomHeight = roomHeight;
        this.roomRadius = roomRadius;
        this.minSize = minSize;
        this.maxZoom = maxZoom;
        this.mannequinScale = mannequinScale;
    }

    boolean carriesArmor() { return mannequinScale > 0; }

    static StandKind of(BlockType type) {
        String id = type != null ? type.getId() : null;
        if (id == null) return TEMPLE;
        return id.startsWith(FAMILY_ID) ? FAMILLE : id.startsWith(SMALL_ID) ? PETIT : TEMPLE;
    }
}
