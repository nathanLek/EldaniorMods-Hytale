package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Assassin extends ClassModel {

    public Assassin() {
        super(
                "assassin",
                "Assassin",
                "Predateur de l'ombre, l'Assassin frappe avec une precision mortelle. Sa vitesse et son agilite en font un fantome insaisissable sur le champ de bataille.",
                Rarity.COMMON,
                ClassType.ASSASSIN,
                List.of(PassiveSkill.WIND_STEP, PassiveSkill.KEEN_SENSES),
                List.of(WeaponMastery.DAGGER, WeaponMastery.SWORD),
                List.of("voleur", "rodeur", "sicaire", "eclaireur", "empoisonneur", "saboteur", "acrobate", "maitre_lame", "ombre_furtive", "chasseur", "ninja", "espion", "corsaire", "traqueur", "lame_noire", "maitre_ombre", "faucheur", "phantom_blade", "grand_maitre_poison", "ange_dechu", "spectre_assassin", "seigneur_poison", "lame_eternelle", "ombre_supreme", "empereur_ombres", "avatar_neant", "dieu_ombres"),
                180,
                false,
                6, 4, 2, 2, 14, 8
        );
    }
}
