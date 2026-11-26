package com.eldanior.system.familiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Especes de familiers (2026-11-13 : 18 especes, de Commun a Legendaire). Chaque espece a son oeuf (bloc Eldanior_Oeuf_<Cle> : meme nid
 * de paille et meme forme pour toutes, seules la texture de la coquille et la rarete changent) et un role par age
 * (Eldanior_Familier_<Cle>_<Age>), generes par scripts/familiers/gen_familiers.py (table ESPECES, meme ordre et memes cles).
 * PV et degats de base par age (Jeune, Adolescent, Adulte), multiplies par le niveau dans Familier.
 * skills : competences propres a l'espece, tirees au hasard (avec celle de son element) quand le maitre la fait evoluer a
 * l'age adulte (Competence, FamilierManager.evolve).
 */
public enum Espece {
    RENARD("Renard", "Renard roux", Element.Rarete.COMMUN, null, "#e8a060", null, "un renardeau",
            new float[]{40f, 90f, 170f}, new float[]{5f, 9f, 15f}, new float[]{0f, 0f, 0f}, Competence.MORSURE, Competence.BOND),
    CHAT("Chat", "Chat", Element.Rarete.COMMUN, null, "#d8b890", null, "un chaton",
            new float[]{35f, 80f, 150f}, new float[]{4f, 8f, 13f}, new float[]{0f, 0f, 0f}, Competence.MORSURE, Competence.BOND),
    CHIEN("Chien", "Chien", Element.Rarete.COMMUN, null, "#c89060", null, "un chiot",
            new float[]{45f, 100f, 190f}, new float[]{5f, 9f, 15f}, new float[]{0f, 0f, 0f}, Competence.MORSURE, Competence.HURLEMENT_MEUTE),
    CORBEAU("Corbeau", "Corbeau", Element.Rarete.COMMUN, null, "#606878", null, "un oisillon noir",
            new float[]{30f, 70f, 130f}, new float[]{4f, 8f, 13f}, new float[]{0f, 0f, 0f}, Competence.PIQUE, Competence.CRI),
    LOUP("Loup", "Loup sombre", Element.Rarete.INHABITUEL, null, "#9aa4b8", Age.ADULTE, "un louveteau sombre",
            new float[]{60f, 140f, 280f}, new float[]{6f, 12f, 20f}, new float[]{0f, 0f, 1.61f}, Competence.MORSURE, Competence.HURLEMENT_MEUTE),
    CHEVAL("Cheval", "Cheval", Element.Rarete.INHABITUEL, null, "#b08860", Age.ADOLESCENT, "un poulain",
            new float[]{60f, 150f, 300f}, new float[]{5f, 10f, 17f}, new float[]{0f, 1.16f, 1.45f}, Competence.RUADE, Competence.BOND),
    FAUCON("Faucon", "Faucon", Element.Rarete.INHABITUEL, null, "#c8a070", null, "un fauconneau",
            new float[]{45f, 110f, 220f}, new float[]{6f, 12f, 20f}, new float[]{0f, 0f, 0f}, Competence.PIQUE, Competence.CRI),
    OURS("Ours", "Ours brun", Element.Rarete.RARE, null, "#b07a4a", Age.ADULTE, "un ourson",
            new float[]{100f, 240f, 500f}, new float[]{8f, 16f, 28f}, new float[]{0f, 0f, 1.67f}, Competence.RUGISSEMENT, Competence.COUP_DE_PATTE),
    OURS_POLAIRE("OursPolaire", "Ours polaire", Element.Rarete.RARE, null, "#e8f0f8", Age.ADULTE, "un ourson blanc",
            new float[]{110f, 260f, 540f}, new float[]{8f, 16f, 28f}, new float[]{0f, 0f, 1.67f}, Competence.RUGISSEMENT, Competence.COUP_DE_PATTE),
    LEOPARD("Leopard", "Léopard des neiges", Element.Rarete.RARE, null, "#dfe6ee", Age.ADULTE, "un jeune léopard",
            new float[]{80f, 200f, 420f}, new float[]{9f, 18f, 31f}, new float[]{0f, 0f, 1.56f}, Competence.BOND, Competence.MORSURE),
    LOUP_BLANC("LoupBlanc", "Loup blanc", Element.Rarete.RARE, null, "#f0f4fa", Age.ADULTE, "un louveteau des neiges",
            new float[]{80f, 190f, 400f}, new float[]{8f, 16f, 28f}, new float[]{0f, 0f, 1.61f}, Competence.MORSURE, Competence.HURLEMENT_MEUTE),
    CHOUETTE("Chouette", "Chouette des neiges", Element.Rarete.RARE, null, "#f4f6fa", null, "une chouette duveteuse",
            new float[]{60f, 150f, 320f}, new float[]{8f, 16f, 27f}, new float[]{0f, 0f, 0f}, Competence.PIQUE, Competence.CRI),
    TIGRE("Tigre", "Tigre à dents de sabre", Element.Rarete.EPIQUE, null, "#f09a3a", Age.ADOLESCENT, "un petit tigre",
            new float[]{100f, 260f, 580f}, new float[]{9f, 20f, 35f}, new float[]{0f, 1.17f, 1.76f}, Competence.COUP_DE_PATTE, Competence.BOND, Competence.RUGISSEMENT),
    RAPTOR("Raptor", "Raptor des cavernes", Element.Rarete.EPIQUE, null, "#7aa060", Age.ADOLESCENT, "un petit raptor",
            new float[]{90f, 240f, 540f}, new float[]{10f, 21f, 36f}, new float[]{0f, 0.94f, 1.31f}, Competence.BOND, Competence.MORSURE, Competence.COUP_DE_PATTE),
    PTERODACTYLE("Pterodactyle", "Ptérodactyle", Element.Rarete.EPIQUE, null, "#a08870", null, "un bébé ptérodactyle",
            new float[]{80f, 220f, 500f}, new float[]{10f, 21f, 36f}, new float[]{0f, 0f, 0f}, Competence.PIQUE, Competence.CRI),
    FENRIR("Fenrir", "Fenrir", Element.Rarete.LEGENDAIRE, Element.GLACE, "#9fdcff", Age.ADOLESCENT, "un louveteau blanc",
            new float[]{120f, 320f, 700f}, new float[]{10f, 22f, 40f}, new float[]{0f, 1.35f, 2.3f}, Competence.GLACE, Competence.CROC),
    DRAGON_GIVRE("DragonGivre", "Dragon de givre", Element.Rarete.LEGENDAIRE, Element.GLACE, "#a8e0ff", Age.ADOLESCENT, "un dragonneau de givre",
            new float[]{130f, 340f, 760f}, new float[]{11f, 23f, 42f}, new float[]{0f, 1.3f, 2.0f}, Competence.GLACE, Competence.COUP_DE_PATTE, Competence.RUGISSEMENT),
    DRAGON_FEU("DragonFeu", "Dragon de feu", Element.Rarete.LEGENDAIRE, Element.DRAGONIC, "#ff7a4a", Age.ADOLESCENT, "un dragonneau de feu",
            new float[]{130f, 340f, 760f}, new float[]{12f, 25f, 45f}, new float[]{0f, 1.0f, 1.55f}, Competence.DRAGONIC, Competence.COUP_DE_PATTE, Competence.RUGISSEMENT);

    /** Cle des identifiants (Eldanior_Oeuf_<cle>, Eldanior_Familier_<cle>_<Age>) ; nom affiche ; couleur ; petit a l'eclosion. */
    public final String key, label, color, young, eggItem, rolePrefix;
    /** Rarete de la bete : niveau maximal, element (Element.roll) et oeufs proposes (boutique : Commun, Inhabituel ; quete : plus). */
    public final Element.Rarete rarete;
    /** Element fixe des betes legendaires et divines (null : tire au hasard a l'eclosion selon la rarete). */
    public final Element element;
    /** Age a partir duquel le maitre peut le monter (null : jamais). */
    public final Age mountFrom;
    final float[] baseHp, baseDamage;
    /** Hauteur de selle (blocs) par age : = role genere (gen_familiers.py, saddle x echelle) ; pose par FamilierManager.mount. */
    final float[] saddle;
    final Competence[] skills;

    Espece(String key, String label, Element.Rarete rarete, Element element, String color, Age mountFrom, String young,
           float[] baseHp, float[] baseDamage, float[] saddle, Competence... skills) {
        this.key = key; this.label = label; this.rarete = rarete; this.element = element; this.color = color;
        this.mountFrom = mountFrom; this.young = young; this.baseHp = baseHp; this.baseDamage = baseDamage; this.saddle = saddle;
        this.skills = skills;
        this.eggItem = "Eldanior_Oeuf_" + key;
        this.rolePrefix = "Eldanior_Familier_" + key + "_";
    }

    public String role(Age age) { return rolePrefix + age.label; }

    public boolean mountable(Age age) { return mountFrom != null && age.ordinal() >= mountFrom.ordinal(); }

    public int maxLevel() { return rarete.maxLevel; }

    /** Element a l'eclosion : celui de l'espece (legendaire, divine), sinon tire dans la liste de sa rarete. */
    public Element rollElement(Random rng) {
        return rarete.fixedElement() && element != null ? element : Element.roll(rarete, rng);
    }

    /** Competences possibles a l'evolution : celles de l'espece, plus celle de son element (betes rares et au-dela). */
    public List<Competence> skillPool(Element el) {
        List<Competence> out = new ArrayList<>(List.of(skills));
        Competence e = el != null ? Competence.of(el.name()) : null;
        if (e != null && !out.contains(e)) out.add(e);
        return out;
    }

    /**
     * Oeuf de la recompense de quete « Un Oeuf venu d'ailleurs » (2026-11-13) : bete rare (62 %), epique (30 %) ou
     * legendaire (8 %, demande de l'utilisateur), espece tiree au hasard dans sa rarete.
     */
    public static Espece rollHighRarity(Random rng) {
        int r = rng.nextInt(100);
        Element.Rarete want = r < 8 ? Element.Rarete.LEGENDAIRE : r < 38 ? Element.Rarete.EPIQUE : Element.Rarete.RARE;
        List<Espece> pool = new ArrayList<>();
        for (Espece e : values()) if (e.rarete == want) pool.add(e);
        return pool.get(rng.nextInt(pool.size()));
    }

    public static Espece ofEgg(String itemId) {
        if (itemId == null) return null;
        for (Espece e : values()) if (e.eggItem.equals(itemId)) return e;
        return null;
    }

    public static Espece of(String name) {
        if (name == null) return null;
        for (Espece e : values()) if (e.name().equalsIgnoreCase(name) || e.key.equalsIgnoreCase(name)) return e;
        return null;
    }

    /** Ages d'un familier. */
    public enum Age {
        JEUNE("Jeune"), ADOLESCENT("Adolescent"), ADULTE("Adulte");

        public final String label;

        Age(String label) { this.label = label; }

        public static Age of(String s) {
            if (s == null) return null;
            for (Age a : values()) if (a.name().equalsIgnoreCase(s) || a.label.equalsIgnoreCase(s)) return a;
            return null;
        }
    }
}
