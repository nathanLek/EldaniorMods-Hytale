package com.eldanior.system.classes.models;

import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.Collections;
import java.util.List;

public abstract class ClassModel {

    private final String id;
    private final String displayName;
    private final String description;
    private final Rarity rarity;
    private final ClassType type;
    /** Passifs innes de la classe, chacun a son rang (refonte des passifs du 2026-11-24). */
    private final List<PassiveSkill.Ranked> passiveSkills;
    private final List<String> activeSkillIds;
    private final List<WeaponMastery> allowedMasteries;

    private final List<String> nextClassId;
    private final int promotionLevel;
    private final boolean adminAccess;

    private final int bonusStr;
    private final int bonusVit;
    private final int bonusInt;
    private final int bonusEnd;
    private final int bonusAgl;
    private final int bonusLck;

    /** Runes de la classe (competences actives, objets Rune_*), declarees dans son constructeur comme ses passifs. */
    private List<String> runeIds = Collections.emptyList();
    /** Metiers (etablis) que la classe connait d'office : ARTISANAT, CRAFT_... (sortis des passifs le 2026-11-24). */
    private List<String> metierIds = Collections.emptyList();
    /** Palier de la classe : 1 base, 2 et 3 evolutions (catalogue), 0 classe speciale (Novice, Dragon, Observateur, voie martiale). */
    private int tier = 0;

    public ClassModel(
            String id,
            String displayName,
            String description,
            Rarity rarity,
            ClassType type,
            List<PassiveSkill.Ranked> passiveSkills,
            List<WeaponMastery> allowedMasteries,
            List<String> nextClassId,
            int promotionLevel,
            boolean adminAccess,
            int str,
            int vit,
            int intel,
            int end,
            int agl,
            int lck
    ) {
        this(id, displayName, description, rarity, type, passiveSkills, Collections.emptyList(),
                allowedMasteries, nextClassId, promotionLevel, adminAccess, str, vit, intel, end, agl, lck);
    }

    public ClassModel(
            String id,
            String displayName,
            String description,
            Rarity rarity,
            ClassType type,
            List<PassiveSkill.Ranked> passiveSkills,
            List<String> activeSkillIds,
            List<WeaponMastery> allowedMasteries,
            List<String> nextClassId,
            int promotionLevel,
            boolean adminAccess,
            int str,
            int vit,
            int intel,
            int end,
            int agl,
            int lck
    ) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.rarity = rarity;
        this.type = type;
        this.passiveSkills = (passiveSkills != null) ? List.copyOf(passiveSkills) : Collections.emptyList();
        this.activeSkillIds = (activeSkillIds != null) ? List.copyOf(activeSkillIds) : Collections.emptyList();
        this.allowedMasteries = (allowedMasteries != null) ? List.copyOf(allowedMasteries) : Collections.emptyList();
        this.nextClassId = (nextClassId != null) ? List.copyOf(nextClassId) : Collections.emptyList();
        this.promotionLevel = promotionLevel;
        this.adminAccess = adminAccess;
        this.bonusStr = str;
        this.bonusVit = vit;
        this.bonusInt = intel;
        this.bonusEnd = end;
        this.bonusAgl = agl;
        this.bonusLck = lck;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
    public ClassType getType() { return type; }

    /** Passifs innes de la classe et leur rang. */
    public List<PassiveSkill.Ranked> getClassPassives() { return passiveSkills; }
    public List<String> getActiveSkillIds() { return activeSkillIds; }
    public List<String> getRuneIds() { return runeIds; }

    /** Runes de la classe, dans son constructeur apres super(...) (ecrit par scripts/classes/class_runes.py). */
    protected final void runes(String... ids) { this.runeIds = List.of(ids); }

    /** Metiers de la classe, dans son constructeur apres super(...) (ecrit par scripts/passifs/gen_passifs.py). */
    protected final void metiers(String... ids) { this.metierIds = List.of(ids); }
    public List<String> getMetierIds() { return metierIds; }

    /** Palier, dans le constructeur apres super(...) (catalogue : scripts/classes/class_catalog.py). */
    protected final void tier(int tier) { this.tier = tier; }
    public int getTier() { return tier; }
    public List<WeaponMastery> getAllowedMasteries() { return allowedMasteries; }

    public List<String> getNextClassId() { return nextClassId; }
    public int getPromotionLevel() { return promotionLevel; }
    public boolean isAdminAccess() { return adminAccess; }

    public int getBonusStr() { return bonusStr; }
    public int getBonusVit() { return bonusVit; }
    public int getBonusInt() { return bonusInt; }
    public int getBonusEnd() { return bonusEnd; }
    public int getBonusAgl() { return bonusAgl; }
    public int getBonusLck() { return bonusLck; }

    /** Rang du passif donne par la classe (0 si elle ne le donne pas). */
    public int passiveRank(PassiveSkill skill) {
        for (PassiveSkill.Ranked r : passiveSkills) if (r.skill() == skill) return r.rank();
        return 0;
    }

    public boolean canEquip(WeaponMastery mastery) {
        if (mastery == WeaponMastery.ANY) return true;
        return allowedMasteries.contains(mastery);
    }
}