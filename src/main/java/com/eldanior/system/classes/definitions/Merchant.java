package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

public class Merchant extends ClassModel {

    public Merchant() {
        super(
                "merchant",
                "Marchand",
                "Le Marchand est un maitre du commerce et de la negociation. Sa chance legendaire attire fortune et opportunites partout ou il passe.",
                Rarity.COMMON,
                ClassType.MERCHANT,
                List.of(PassiveSkill.ARTISANAT, PassiveSkill.FORTUNE_COINS),
                List.of(WeaponMastery.ANY, WeaponMastery.SHIELD, WeaponMastery.SPEAR, WeaponMastery.STAFF, WeaponMastery.SPELLBOOK, WeaponMastery.SWORD, WeaponMastery.BOW, WeaponMastery.CLUB, WeaponMastery.AXE, WeaponMastery.MACE, WeaponMastery.DAGGER, WeaponMastery.RIFLE, WeaponMastery.GUN),
                List.of("master_artisan", "relic_hunter", "smuggler", "black_market_prince", "gold_baron", "guild_master", "prosperity_avatar", "underworld_king", "world_forger", "negociant", "caravanier", "prospecteur", "banquier", "marchand_divin", "roi_commerce"),
                180,
                false,
                4, 2, 4, 4, 4, 20
        );
    }
}
