package com.eldanior.system.config;

import com.eldanior.system.territory.ParcelType;

/**
 * Source unique de verite pour tous les taux de taxation du systeme territorial.
 *
 * Distribution des taxes de transaction (12% preleve sur chaque transaction) :
 *   Ville 15%, Duc 20%, Marquis 30%, Royaume 35% (= 100%)
 *
 * Collecte periodique des impots (prelevement sur la tresorerie des enfants) :
 *   Duche preleve 35% des villes
 *   Marquisat preleve 30% du Duche
 *   Royaume preleve 25% du Marquisat
 */
public final class TaxConfig {

    private TaxConfig() {}

    // ==================== TAXE DE TRANSACTION ====================

    /** Taux de taxe preleve sur chaque transaction (12%) */
    public static final float TRANSACTION_TAX_RATE = 0.12f;

    // Parts de distribution de la taxe de transaction (total = 100%)
    /** Part de la ville sur la taxe de transaction */
    public static final double CITY_SHARE = 0.15;
    /** Part du duche sur la taxe de transaction */
    public static final double DUCHY_SHARE = 0.20;
    /** Part du marquisat sur la taxe de transaction */
    public static final double MARQUISATE_SHARE = 0.30;
    /** Part du royaume sur la taxe de transaction (recoit le reste) */
    public static final double KINGDOM_SHARE = 0.35;

    // Cas ou un niveau manque dans la hierarchie
    /** Part unique d'un territoire s'il est seul intermediaire */
    public static final double SINGLE_TERRITORY_SHARE = 0.25;

    // ==================== COLLECTE D'IMPOTS PERIODIQUE ====================

    /** Taux de prelevement du Duche sur les villes */
    public static final double DUCHY_COLLECTION_RATE = 0.35;
    /** Taux de prelevement du Marquisat sur le Duche */
    public static final double MARQUISATE_COLLECTION_RATE = 0.30;
    /** Taux de prelevement du Royaume sur le Marquisat */
    public static final double KINGDOM_COLLECTION_RATE = 0.25;

    /**
     * Retourne le taux de collecte periodique pour un type de parcelle.
     * @return le taux, ou 0.0 si ce type ne collecte pas d'impots
     */
    public static double getCollectionRate(ParcelType type) {
        return switch (type) {
            case KINGDOM -> KINGDOM_COLLECTION_RATE;
            case GRAND_TERRITORY -> MARQUISATE_COLLECTION_RATE;
            case TERRITORY -> DUCHY_COLLECTION_RATE;
            default -> 0.0;
        };
    }
}
