package com.eldanior.system.skills.models;

import java.util.List;

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
        float duration,
        List<String> levelUp,
        List<String> levelDown
) {
    public SkillModel(String skillId, String catalystId, String displayName, String requiredClass,
                      int manaCost, float cooldown, float castTime, float damage, float range, float duration) {
        this(skillId, catalystId, displayName, requiredClass, manaCost, cooldown, castTime, damage, range, duration,
                List.of(), List.of());
    }

    public boolean isBuff() {
        return duration > 0 && damage == 0;
    }

    public boolean isInstant() {
        return castTime == 0;
    }
}