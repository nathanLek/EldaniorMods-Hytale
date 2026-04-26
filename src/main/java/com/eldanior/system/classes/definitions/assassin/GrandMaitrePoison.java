package com.eldanior.system.classes.definitions.assassin;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class GrandMaitrePoison extends ClassModel {
    public GrandMaitrePoison() {
        super("grand_maitre_poison", "Grand Maitre du Poison", "Le Grand Maitre du Poison connait chaque toxine existante. Une goutte de son elixir peut terrasser un dragon.",
                Rarity.EPIC, ClassType.ASSASSIN,
                List.of(PassiveSkill.HYDRA_BLOOD, PassiveSkill.SPIRIT_DRAIN, PassiveSkill.DEADLY_PRECISION),
                List.of(WeaponMastery.DAGGER),
                List.of("seigneur_du_venin", "hydre_empoisonnee", "venin_absolu"), 400, false,
                40, 40, 30, 20, 80, 90);
    }
}
