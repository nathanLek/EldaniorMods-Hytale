package com.eldanior.system.skills.models;

/**
 * @param requiredClass "mage", "warrior", "scout", etc.
 */
public record SkillModel(String skillId, String catalystId, String displayName, String requiredClass) {
}