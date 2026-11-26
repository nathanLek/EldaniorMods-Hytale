package com.eldanior.system.classes.definitions;

import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.configs.WeaponMastery;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;

import java.util.List;

/**
 * Observateur (2026-11-16, demande de l'utilisateur) : « l'Oeil et les Oreilles de l'Empereur, l'assassin prive du Roi ».
 * Classe unique : jamais tiree ni proposee en evolution (adminAccess), seulement remise par le Roi ou l'Empereur Luo
 * (Patriarche du Clan Luo), un joueur chacun (classes.observateur.ObservateurManager). Simple changement de classe : niveau,
 * caracteristiques, runes et objets sont gardes. Elle manie toutes les armes et toutes les runes du mod
 * (ObservateurManager.isObservateur : MasterySystem, RuneManager.canUseRune, parchemins, manuels Luo). Espion du continent : il voit
 * le marche noir (SystemScreen) et entre sur le territoire Luo sans plaque (LuoPlaques.admitted).
 */
public class Observateur extends ClassModel {

    public static final String ID = "observateur";

    public Observateur() {
        super(
                ID,
                "Observateur",
                "L'Oeil et les Oreilles de l'Empereur, l'assassin prive du Roi. Nomme par le souverain lui-meme, il manie toutes les armes et tous les savoirs du royaume.",
                Rarity.LEGENDARY,
                ClassType.OBSERVATEUR,
                // espion et assassin : l'Oeil (Vue Absolue), les Oreilles (Sens du Danger), la lame (precision, dague, esquive)
                List.of(PassiveSkill.SURVIVAL_INSTINCT.r(2), PassiveSkill.LUCKY_STRIKE.r(5), PassiveSkill.MIRACLE_DODGE.r(3), PassiveSkill.DAGGER_MASTERY.r(3)),
                List.of(WeaponMastery.values()),
                List.of(),
                999,
                true,
                // 2026-11-16 : « equivalent d'une classe legendaire, presque l'une des plus fortes » : total 3520, sous le
                // Dragon Ancestral (4500), le Demi-Dieu (3876) et le Demi-Dragon (3604), au-dessus de toute classe d'assassin
                500, 380, 520, 420, 1000, 700
        );
        // 2026-11-17 : detection (Arkies Domini), deguisement (Sans Visage), technique ultime d'assassinat (Sentence Silencieuse)
        runes("Rune_ArkiesDomini", "Rune_SansVisage", "Rune_SentenceSilencieuse");
    }
}
