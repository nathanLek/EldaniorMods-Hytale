package com.eldanior.system.accueil;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.gui.ClassInfo;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.accueil.CardsPage.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Carte d'une classe pour les fenetres a cartes (accueil et statue des classes, 2026-11-25) : illustration selon son type,
 * titre a la couleur de sa rarete, description, armes, evolution, passifs avec leur effet, runes avec leur icone, bonus de
 * caracteristiques et, a l'accueil seulement, le kit de depart.
 */
public final class ClassCards {

    private ClassCards() {}

    /** Illustration d'une classe : celle de sa famille (type), le dojo pour la voie martiale du Clan Luo. */
    public static String art(ClassModel m) {
        if (m == null) return "Royaume";
        for (ClassModel luo : com.eldanior.system.classes.definitions.luo.VoieMartiale.classes())
            if (luo.getId().equals(m.getId())) return "Dojo";
        return switch (m.getType()) {
            case WARRIOR -> "Guerrier";
            case MAGE -> "Mage";
            case ARCHER, MOUSQUETAIRE -> "Archer";
            case ASSASSIN -> "Assassin";
            case MERCHANT -> "Marchand";
            default -> "Royaume";
        };
    }

    /** Dossier des illustrations propres aux classes (scripts/classes/gen_class_art.py), relatif a Common/UI/Custom. */
    private static final String ART_DIR = "Accueil/Classes/";
    private static final java.util.Map<String, Boolean> ART_EXISTS = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Illustration propre de la classe (2026-11-26) : « Accueil/Classes/Art_<id> » si ses documents existent dans le jar,
     * sinon celle de l'ancetre le plus proche qui en a une, sinon null (l'illustration de sa famille, {@link #art}).
     */
    public static String ownArt(ClassModel m) {
        if (m == null) return null;
        // 2026-11-26 : une classe sans illustration (paliers 3 a 5) prend celle de sa lignee (ancetre du palier 2) :
        // l'atlas des textures d'interface du client n'a pas la place d'une image par classe
        String id = m.getId();
        for (int depth = 0; id != null && depth < 6; depth++, id = ClassManager.parentOf(id)) {
            String base = ART_DIR + "Art_" + id;
            boolean ok = ART_EXISTS.computeIfAbsent(id, k ->
                    ClassCards.class.getResource("/Common/UI/Custom/" + base + "C.ui") != null
                            && ClassCards.class.getResource("/Common/UI/Custom/" + base + "G.ui") != null);
            if (ok) return base;
        }
        return null;
    }

    /** Contour de carte selon la rarete (2026-11-25) : memes couleurs que les titres de rarete du mod. */
    public static String tint(com.eldanior.system.config.configs.Rarity r) {
        return switch (r) {
            case RARE -> "Bleu";
            case EPIC -> "Violet";
            case UNIQUE -> "Rouge";
            case LEGENDARY -> "Or";
            case DIVINE -> "Cyan";
            default -> "Blanc";                                            // Commune : blanc (2026-11-25)
        };
    }

    public static Card of(String classId, boolean withKit) {
        ClassModel m = ClassManager.get(classId);
        return m != null ? of(m, withKit) : null;
    }

    public static Card of(ClassModel m, boolean withKit) {
        String rarity = ClassInfo.rarityLabel(m.getRarity());
        Card c = new Card(m.getId(), art(m), m.getDisplayName());
        c.tint = tint(m.getRarity());
        c.artUi = ownArt(m);
        c.titleColor = m.getRarity() == com.eldanior.system.config.configs.Rarity.COMMON ? CardsPage.GOLD
                : ClassInfo.getRarityHexColor(m.getRarity());
        c.text(m.getDisplayName().substring(0, 1), m.getType().getLabel() + "  ·  classe " + rarity, m.getDisplayName(),
                withKit ? "Votre première classe." : "Classe " + rarity + ".", description(m));
        List<String> notes = new ArrayList<>();
        notes.add("Armes : " + ClassInfo.weapons(m.getAllowedMasteries()) + ".");
        if (m.getNextClassId() != null && !m.getNextClassId().isEmpty() && m.getPromotionLevel() > 0)
            notes.add(nextNote(m));
        else notes.add("Sommet de sa lignée : plus d'évolution.");
        c.notes = notes;
        List<CardsPage.Passive> passives = new ArrayList<>();
        for (PassiveSkill.Ranked p : m.getClassPassives())
            passives.add(new CardsPage.Passive(p.skill().getDisplayName(), PassiveSkill.rankLabel(p.rank()),
                    p.skill().getDescription(p.rank())));
        c.passives = passives;
        c.runes = ClassInfo.runes(m, 170);
        c.stats = new int[]{m.getBonusStr(), m.getBonusVit(), m.getBonusInt(), m.getBonusEnd(), m.getBonusAgl(), m.getBonusLck()};
        if (withKit) c.kit = StarterKits.kit(m.getId());
        return c;
    }

    /** Ligne « evolution » de la carte : Eveil (palier 3), Transcendance (palier 4), sinon nombre de voies. */
    static String nextNote(ClassModel m) {
        int n = m.getNextClassId().size();
        return switch (m.getTier()) {
            case 3 -> "Éveil au niveau " + m.getPromotionLevel() + ".";
            case 4 -> "Transcendance au niveau " + m.getPromotionLevel() + ".";
            default -> "Évolution au niveau " + m.getPromotionLevel() + " : " + n + (n > 1 ? " voies possibles." : " voie.");
        };
    }

    /** Descriptions des classes de base avec leurs accents (celles des definitions sont ecrites sans). */
    public static String description(ClassModel m) {
        return switch (m.getId()) {
            case "warrior" -> "Né pour le champ de bataille, le Guerrier excelle au corps à corps. Sa force brute et sa résilience "
                    + "en font un adversaire redoutable que peu osent défier.";
            case "mage" -> "Maître des arcanes, le Mage canalise une puissance magique dévastatrice. Son intelligence supérieure "
                    + "lui permet de manipuler les éléments à sa guise.";
            case "archer" -> "L'Archer maîtrise le combat à distance avec une précision chirurgicale. Son œil de faucon et sa "
                    + "dextérité lui permettent d'atteindre n'importe quelle cible.";
            case "assassin" -> "Prédateur de l'ombre, l'Assassin frappe avec une précision mortelle. Sa vitesse et son agilité en "
                    + "font un fantôme insaisissable sur le champ de bataille.";
            case "merchant" -> "Le Marchand est un maître du commerce et de la négociation. Sa chance légendaire attire fortune et "
                    + "opportunités partout où il passe.";
            default -> m.getDescription() != null ? m.getDescription() : "";
        };
    }
}
