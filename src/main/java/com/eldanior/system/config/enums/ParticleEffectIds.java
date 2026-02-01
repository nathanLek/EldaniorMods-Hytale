package com.eldanior.system.config.enums;

public enum ParticleEffectIds {

    MAGIC_PARTICULES_SPHERES_ORBE_LIGHT_INF("E_Sphere_Old"),
    STATUS_EROSION_EFFECT_TEMP("Erosion_Status_Effect"),

    // ========================================

    PORTAL_ROUND_BLUE_INF("Portal_Round_Blue"),
    PORTAL_ROUND_BLUE2_INF("Portal_Round_Blue2");

    private final String id;

    ParticleEffectIds(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}