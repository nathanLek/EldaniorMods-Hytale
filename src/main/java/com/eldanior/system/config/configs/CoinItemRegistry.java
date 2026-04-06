package com.eldanior.system.config.configs;

import java.util.Map;
import java.util.Optional;

public final class CoinItemRegistry {

    private static final Map<String, Long> COIN_VALUES = Map.of(
            "Elda_Copper_Coins",      1L,
            "Elda_Silver_Coins",      10L,
            "Elda_Gold_Coins",  100L,
            "Elda_Diamond_Coins", 1000L,
            "Elda_Zenith_Coins", 10000L
    );

    private CoinItemRegistry() {}

    public static Optional<Long> getValueFor(String itemId) {
        return Optional.ofNullable(COIN_VALUES.get(itemId));
    }

    public static boolean isCoinItem(String itemId) {
        return COIN_VALUES.containsKey(itemId);
    }
}