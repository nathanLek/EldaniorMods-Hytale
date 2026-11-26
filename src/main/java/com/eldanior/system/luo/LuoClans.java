package com.eldanior.system.luo;

/**
 * Les quatre grands clans de l'Empire Luo (decision du 2026-11-10) : des familles a part entiere (FamilyManager), sans
 * territoire, de rang comte (elles peuvent acheter des villes de l'empire). Leur Patriarche est un des quatre Doyens de
 * l'Empire : il porte la plaque de Doyen et recoit le kimono de Doyen de son clan (FamilyKeySystem). Leurs membres portent
 * la plaque de leur clan et sont admis sur les terres de l'empire comme les gens du Clan Luo.
 *
 * Tenues : membres = tenue du clan (Sakura, Sama pugiliste, Ombre, Setsuka) ; Doyen = kimono de Doyen de sa couleur
 * (objets Armor_Kimono_Noble_* : violet, cramoisi, noir, blanc et jade).
 */
public final class LuoClans {

    private LuoClans() {}

    public enum Clan {
        SAMA("sama", "Sama", "Sama", "Noble_Cramoisi"),                  // tenue pugiliste (2026-11-10)
        HANAZAKURA("hanazakura", "Hanazakura", "", "Noble_Violet"),       // "" : kimono Sakura (Armor_Kimono_<piece>)
        KAGE("kage", "Kage", "Ombre", "Noble"),
        SETSUKA("setsuka", "Setsuka", "Setsuka", "Noble_Jade");

        public final String id, label, outfit, elderOutfit;

        Clan(String id, String label, String outfit, String elderOutfit) {
            this.id = id; this.label = label; this.outfit = outfit; this.elderOutfit = elderOutfit;
        }

        /** Les quatre pieces du kimono de Doyen du clan. */
        public java.util.List<String> elderPieces() {
            String p = "Armor_Kimono_" + elderOutfit + "_";
            return java.util.List.of(p + "Head", p + "Chest", p + "Hands", p + "Legs");
        }
    }

    /** Clan dont id est l'identifiant de famille, sinon null. */
    public static Clan of(String familyId) {
        if (familyId == null) return null;
        for (Clan c : Clan.values()) if (c.id.equalsIgnoreCase(familyId)) return c;
        return null;
    }

    /** Clan dont c'est le nom affiche (« Sama »...), sinon null (nom de famille des PNJ de poste). */
    public static Clan byName(String displayName) {
        if (displayName == null) return null;
        for (Clan c : Clan.values()) if (c.label.equalsIgnoreCase(displayName) || c.id.equalsIgnoreCase(displayName)) return c;
        return null;
    }

    public static boolean isClan(String familyId) {
        return of(familyId) != null;
    }

    /** Famille de l'Empire Luo : le Clan Luo ou un de ses quatre clans. */
    public static boolean isEmpire(String familyId) {
        return LuoPlaques.FAMILY.equalsIgnoreCase(familyId) || isClan(familyId);
    }

    // ===================== TECHNIQUES DES CLANS (2026-11-10) =====================

    /**
     * Technique d'un clan (ou de la famille Luo elle-meme : clan null, familyId "luo") : rune, clan qui l'enseigne, niveau du
     * personnage requis, famille dont il faut etre membre.
     */
    public record Technique(String rune, Clan clan, int level, String familyId) {
        public Technique(String rune, Clan clan, int level) { this(rune, clan, level, clan.id); }

        /** « clan Sama », « famille Luo ». */
        public String label() { return clan != null ? "clan " + clan.label : "famille Luo"; }
    }

    /**
     * Les 8 anciennes runes de palier de la voie martiale, devenues techniques des clans (decision du 2026-11-10,
     * docs/plan-clan-luo.md) : Sama (pugilistes, corps et Ki) : Paume de Ki, Frappe des Meridiens, Transformation Hwakyung ;
     * Hanazakura (epeistes floraux) : Domaine de l'Epee, Fleur de Prunier, Unite du Ciel et de la Terre ; Setsuka (maitres de
     * l'epee, contres, Ki) : Garde du Fleuve, Croissant de Ki ; Kage : techniques a creer. Seuls les membres du clan peuvent
     * les apprendre (manuel secret) et les utiliser, quelle que soit leur classe.
     */
    public static final java.util.List<Technique> TECHNIQUES = java.util.List.of(
            new Technique("Rune_PaumeKi", Clan.SAMA, 20),
            new Technique("Rune_FrappeMeridiens", Clan.SAMA, 100),
            new Technique("Rune_TransformationHwakyung", Clan.SAMA, 500),
            new Technique("Rune_DomaineEpee", Clan.HANAZAKURA, 300),
            new Technique("Rune_FleurPrunier", Clan.HANAZAKURA, 400),
            new Technique("Rune_UniteCielTerre", Clan.HANAZAKURA, 600),
            new Technique("Rune_GardeFleuve", Clan.SETSUKA, 150),
            new Technique("Rune_CroissantKi", Clan.SETSUKA, 220),
            // 1er Mouvement du Clan Luo (2026-11-12, titles.nobility.family.FamilyMovements) : manuel remis a l'entree dans la
            // famille, des l'Apprenti (niveau 1 : le serment de la voie martiale remet au niveau 1)
            new Technique("Rune_ExecutionCeleste", null, 1, "luo"));

    /** Technique de clan enseignee par cette rune, sinon null. */
    public static Technique technique(String runeId) {
        if (runeId == null) return null;
        for (Technique t : TECHNIQUES) if (t.rune().equals(runeId)) return t;
        return null;
    }

    /** true si ce joueur est membre du clan qui enseigne cette technique. */
    public static boolean knowsClan(Technique t, com.eldanior.system.config.Player.PlayerLevelData data) {
        return t != null && data != null && t.familyId().equalsIgnoreCase(
                com.eldanior.system.titles.nobility.family.FamilyManager.migrateId(data.getNobleFamilyId()));
    }

    /** Joueur -> techniques dont le manuel a deja ete remis cette session (evite de rescanner l'inventaire). */
    private static final java.util.Map<java.util.UUID, java.util.Set<String>> GIVEN = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Remet aux membres d'un clan le manuel secret de chaque technique de leur clan dont ils ont le niveau, s'ils ne la
     * connaissent pas deja (appele toutes les 5 s par locks.FamilyKeySystem). Retourne le nombre de manuels remis.
     */
    public static int grantTechniques(com.hypixel.hytale.component.Ref<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> ref,
                                      com.hypixel.hytale.component.Store<com.hypixel.hytale.server.core.universe.world.storage.EntityStore> store,
                                      com.eldanior.system.config.Player.PlayerLevelData data, java.util.UUID uuid) {
        Clan clan = data != null ? of(data.getNobleFamilyId()) : null;
        if (clan == null || uuid == null) return 0;
        java.util.Set<String> given = GIVEN.computeIfAbsent(uuid, k -> java.util.concurrent.ConcurrentHashMap.newKeySet());
        int count = 0;
        for (Technique t : TECHNIQUES) {
            if (t.clan() != clan || data.getLevel() < t.level() || given.contains(t.rune())) continue;
            String book = com.eldanior.system.skills.runes.spells.LuoManuels.bookFor(t.rune());
            if (book == null) continue;
            if (com.eldanior.system.skills.runes.RuneManager.ownsRune(ref, store, t.rune())
                    || com.eldanior.system.skills.runes.spells.LuoManuels.hasBook(ref, store, book)) { given.add(t.rune()); continue; }
            if (com.eldanior.system.skills.runes.spells.LuoManuels.giveBook(ref, store, book)) { given.add(t.rune()); count++; }
        }
        return count;
    }

    /** Deconnexion : oublie les remises de la session. */
    public static void forget(java.util.UUID uuid) { if (uuid != null) GIVEN.remove(uuid); }
}
