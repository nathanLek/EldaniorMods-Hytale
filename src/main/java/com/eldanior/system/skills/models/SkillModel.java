package com.eldanior.system.skills.models;

public record SkillModel(
        String skillId,
        String catalystId,
        String displayName,
        String requiredClass,
        int manaCost,
        float cooldown,
        float castTime,
        float damage,
        float range,
        float duration
) {
    public boolean isBuff() {
        return duration > 0 && damage == 0;
    }

    public boolean isInstant() {
        return castTime == 0;
    }
}