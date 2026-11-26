package com.eldanior.system.guards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.gui.tabs.FamilleTab;
import com.eldanior.system.gui.tabs.TerritoiresTab;
import com.eldanior.system.quest.chapitres.ChapterQuests;
import com.eldanior.system.quest.chapitres.HouseQuests;
import com.eldanior.system.shop.CoinWallet;
import com.eldanior.system.territory.LandOffice;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.territory.laws.DomainLaws;
import com.eldanior.system.territory.teleport.ContinentalTeleporters;
import com.eldanior.system.territory.pancarte.Pancartes;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRegistry;
import com.eldanior.system.titles.nobility.family.FamilyRole;
import com.eldanior.system.titles.nobility.family.KnightOrder;
import com.eldanior.system.titles.nobility.family.KnightOrderManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Registre de la maison, tenu par l'intendant (refait le 2026-11-18 : « la meme fenetre que la fenetre de quetes mais
 * stylisee et adaptee pour l'intendant, avec des categories et des actions cliquables »). Intendant/Intendant.ui, genere par
 * scripts/intendant/gen_intendant_ui.py (CATS, CARDS, NOTES, ACTIONS doivent correspondre).
 *  - a droite : categories (Membres, Ordres, Tresor, Domaines, Biens, Reserve, Quetes, Titres pour un noble qui en confere,
 *    Royaume pour la maison Eldanior, Empire pour le Clan Luo), tuiles de chiffres, cartes cliquables ;
 *  - 3e passe (2026-11-18) : un seul intendant par maison, la vue change selon le role, la noblesse et la famille du joueur ;
 *    domaines en descente par niveaux avec population (IntendantLands), titres de noblesse personnels et cumulatifs,
 *    hierarchie a sept rangs dans les ordres, livre de comptes en cartes.
 *  - a gauche : la fiche de la carte choisie (ou l'aide de la categorie), champs et jusqu'a 6 actions.
 * Rangs (2026-11-18) : Patriarche > Doyen > Seigneur > Noble > Chevalier Royal > Chevalier > Recrue. Tout membre ouvre le
 * registre (gestes selon le rang) ; un etranger n'y voit que les quetes de la maison (et les terres a acquerir s'il est Comte).
 * Gestes irreversibles (rayer, dissoudre, retirer une quete) : second clic dans les 6 s.
 */
public final class IntendantPage extends InteractiveCustomUIPage<IntendantPage.Ev> {

    static final int CATS = 9, CARDS = 12, NOTES = 10, ACTIONS = 6, FACTS = 8;
    /** Gestes destructeurs : bouton rouge (2026-11-19). */
    private static final java.util.Set<String> DANGER = java.util.Set.of("kick", "disband", "dissolve", "orkick", "revoke", "qwithdraw",
            "ban", "curfewoff", "border:closed", "unsell", "estwithdraw", "leave", "odisband", "oleave", "bfire", "cleave");
    private static final String C_GREEN = "#00e7b6", C_RED = "#e0574a", C_GOLD = "#f2c45e", C_BLUE = "#6fa8e8", C_GREY = "#56677a",
            C_VIOLET = "#b47be8", C_ORANGE = "#e8a050";
    private static final long ARM_MS = 6_000, WEEK_MS = 7L * 24 * 3600 * 1000;
    /** Taxe Imperiale des clans vassaux vers le Clan Luo (2026-11-18, taux laisse a mon choix) : 10 % par semaine. */
    static final double IMPERIAL_RATE = 0.10;

    enum Cat {
        MEMBRES("Membres", "Eldanior_Banniere_Famille_"), ORDRES("Ordres", "Eldanior_Banniere_Famille_"), TRESOR("Trésor", "Elda_Gold_Coins"),
        DOMAINES("Domaines", "Eldanior_Borne_Territoire"), BIENS("Biens", "Eldanior_Affiche_Propriete"), RESERVE("Réserve", "Eldanior_Presentoir_Famille_"),
        QUETES("Quêtes", "Eldanior_Tome_01"), TITRES("Titres", "Decret_Chevalier"), ROYAUME("Royaume", "Armor_Eldanior_Roi_Chest"),
        EMPIRE("Empire", "Eldanior_Plaque_Luo_Patriarche");

        final String label, icon;
        Cat(String label, String icon) { this.label = label; this.icon = icon; }
    }

    record Card(String key, String icon, String mark, String name, String meta, String value, String bar) {}
    record Stat(String value, String label) {}
    record Btn(String label, String key) {}

    static final class Detail {
        String icon, mark, kicker, title, tagline, text = "";
        final List<String> notes = new ArrayList<>();
        String n0, n1, n2, t0, t1;             // libelles des champs (null : masque) ; n2 = #IntInput4
        Long v0, v1, v2;                       // valeurs a pousser dans les champs numeriques
        String s0, s1;                         // valeurs a pousser dans les champs texte
        final List<Btn> btns = new ArrayList<>();
        /** Faits en tete de fiche (2026-11-19) : {cle, valeur, couleur ou null}. */
        final List<String[]> facts = new ArrayList<>();
        Detail fact(String k, String v) { return fact(k, v, null); }
        Detail fact(String k, String v, String color) { if (v != null && facts.size() < FACTS) facts.add(new String[]{k, v, color}); return this; }
        Detail btn(String label, String key) { if (btns.size() < ACTIONS) btns.add(new Btn(label, key)); return this; }
        Detail note(String n) { if (n != null && notes.size() < NOTES) notes.add(n); return this; }
    }

    record View(String section, String footer, String empty, List<Card> cards, List<Stat> stats, Detail intro) {}

    private final String familyId, stewardName;
    private Cat cat;
    private int page;
    private String selected, status, statusColor = C_GREEN, armed;
    private long armedAt;
    private boolean pushFields;
    private long n0, n1, n2;
    /** Creation d'une quete (2026-11-18, 2e passe) : la grille montre les cibles a choisir, la fiche le formulaire. */
    private boolean draft;
    /** Action qui a ouvert une autre fenetre : ne plus envoyer de mise a jour a celle-ci (deconnexion « #IntCat0 not found »). */
    private boolean replaced;
    private String t0 = "", t1 = "";
    // brouillon de quete (Patriarche)
    private boolean draftHunt = true, draftMembers = false;
    private int draftHuntIndex = -1;
    private String draftItem, draftItemLabel;
    private List<Card> lastCards = List.of();
    /** Niveau ouvert dans la categorie : "d:<zone>" (domaines), "o:<ordre>" (membres d'un ordre), "book" (livre de comptes). */
    private String drill;
    /**
     * Regles du domaine ouvertes (2026-11-19, « ameliorer le design des actions pour les regles du domaine ») : id du domaine
     * dont la grille montre une carte par regle (combat, blocs, annonce, couvre-feu, armes, amende, frontieres, droit
     * d'entree, bannis, Portail Continental) ; la fiche de gauche montre la regle choisie et ses gestes. null : ferme.
     */
    private String rulesOf;
    /** Ordre en cours de fondation par le Patriarche (drill "ofound" : choix du Grand Maitre). */
    private String pendingOrderName, pendingOrderMotto;

    public IntendantPage(@Nonnull PlayerRef playerRef, String familyId, String stewardName) {
        super(playerRef, CustomPageLifetime.CanDismiss, Ev.CODEC);
        this.familyId = familyId != null ? FamilyManager.migrateId(familyId) : null;
        this.stewardName = stewardName != null ? stewardName : "L'intendant";
    }

    // ==================== REGISTRE D'UNE VILLE (Comte, 2026-11-19) ====================

    /** Ville dont ce registre est la fenetre (borne de la ville, Bailli) ; null : registre d'une maison. */
    private String cityId;

    /** Registre de la ville : son Comte (ou un admin) y gere tresor, biens, regles, lois et Bailli. */
    public static IntendantPage forCity(@Nonnull PlayerRef pr, ParcelData city, String speaker) {
        String fam = IntendantLands.house(city);
        if (fam.isEmpty()) fam = IntendantLands.house(com.eldanior.system.titles.nobility.family.HouseRoles.suzerain(city));
        // ville sans maison (ni son Duche) : maison vide, jamais nulle (2026-11-19 : NPE « this.familyId is null » a l'ouverture)
        IntendantPage page = new IntendantPage(pr, fam, speaker != null ? speaker : "Registre de " + city.getName());
        page.cityId = city.getId();
        page.selected = "d:" + city.getId();
        page.pushFields = true;
        return page;
    }

    private ParcelData city() { return cityId != null ? ParcelManager.get(cityId) : null; }

    /** Ligne au livre de comptes de la maison (rien pour une ville sans maison). */
    private void log(String text) {
        if (familyId != null && !familyId.isEmpty()) FamilyRegistry.log(familyId, text);
        else if (cityId != null) EldaniorLogger.info("[Ville] " + text);
    }

    /** Le Comte de la ville (son maitre) ou un admin. */
    public static boolean canOpenCity(ParcelData city, UUID u, PlayerRef pr) {
        return city != null && (pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION) || u != null && city.isOwner(u));
    }

    // ==================== CONTEXTE ====================

    private NobleFamilyModel family() { return familyId != null ? FamilyManager.get(familyId) : null; }

    private String familyName() {
        NobleFamilyModel f = family();
        return f != null ? f.getDisplayName() : "la maison";
    }

    private static PlayerLevelData data(Ref<EntityStore> ref, Store<EntityStore> store) {
        return store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
    }

    private static UUID me(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        try { return pr != null ? UUIDExtractor.getUUID(pr) : null; } catch (Exception e) { return null; }
    }

    private String username() { return playerRef.getUsername(); }

    private boolean admin() { return playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION); }

    private boolean member(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData d = data(ref, store);
        return d != null && familyId != null && familyId.equals(FamilyManager.migrateId(d.getNobleFamilyId()));
    }

    /** Rang avec lequel le joueur agit : le sien s'il est de la maison, Patriarche pour un admin exterieur, null sinon. */
    private FamilyRole role(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (cityId != null) {                                                          // registre de ville : le Comte agit en Seigneur
            if (admin()) return FamilyRole.PATRIARCH;
            ParcelData c = city();
            UUID m = me(ref, store);
            return c != null && m != null && c.isOwner(m) ? FamilyRole.VICE : null;
        }
        if (member(ref, store)) {
            PlayerLevelData d = data(ref, store);
            FamilyRole r = d.isPatriarch() ? FamilyRole.PATRIARCH : d.getFamilyRole() != null ? d.getFamilyRole() : FamilyRole.MEMBER;
            if (r.ordinal() < FamilyRole.ELDER.ordinal() && familyId != null      // membre qui tient une ville du Duche : Doyen d'office
                    && com.eldanior.system.titles.nobility.family.HouseRoles.isVassalDuke(me(ref, store), familyId)) return FamilyRole.ELDER;
            return r;
        }
        // seigneur d'une terre vassale (Marquis, Duc, Comte) : Doyen d'office de la maison suzeraine (2026-11-18)
        if (familyId != null && com.eldanior.system.titles.nobility.family.HouseRoles.isVassalDuke(me(ref, store), familyId)) return FamilyRole.ELDER;
        return admin() ? FamilyRole.PATRIARCH : null;
    }

    private boolean insider(Ref<EntityStore> ref, Store<EntityStore> store) { return role(ref, store) != null; }

    private boolean patriarch(Ref<EntityStore> ref, Store<EntityStore> store) { return role(ref, store) == FamilyRole.PATRIARCH; }

    private boolean comteOrMore(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData d = data(ref, store);
        NobilityRank r = d != null ? NobilityRank.fromString(d.getNobilityRank()) : null;
        return r != null && r.ordinal() >= NobilityRank.COMTE.ordinal();
    }

    /** Patriarche du Clan Luo : titres du Marquisat Luo en plus (NobilityManager.LUO_BONUS). */
    private boolean luoMarquis(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData d = data(ref, store);
        return d != null && d.isPatriarch() && com.eldanior.system.luo.LuoPlaques.FAMILY.equals(FamilyManager.migrateId(d.getNobleFamilyId()));
    }

    private NobilityRank nobility(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData d = data(ref, store);
        return d != null ? NobilityRank.fromString(d.getNobilityRank()) : null;
    }

    private String greet(Ref<EntityStore> ref, Store<EntityStore> store) {
        FamilyRole r = role(ref, store);
        if (!member(ref, store)) return admin() ? "Envoyé de la couronne" : "Voyageur";
        return (r == FamilyRole.MEMBER ? "" : r.label() + " ") + username();
    }

    private static String item(String id) { return id != null && Item.getAssetMap().getAsset(id) != null ? id : null; }

    private String famItem(String prefix) {
        if (familyId == null || familyId.isEmpty()) return null;
        return item(prefix + Character.toUpperCase(familyId.charAt(0)) + familyId.substring(1));
    }

    private String catIcon(Cat c) {
        String i = c.icon.endsWith("_") ? famItem(c.icon) : item(c.icon);
        return i != null ? i : item("Eldanior_Borne_Territoire");
    }

    private List<Cat> cats(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (cityId != null) return List.of(Cat.DOMAINES, Cat.TRESOR, Cat.BIENS);   // Tresor de la ville : 2026-11-19
        List<Cat> out = new ArrayList<>();
        FamilyRole r = role(ref, store);
        boolean titles = NobilityManager.grantsTitles(nobility(ref, store)) || luoMarquis(ref, store);   // chez tous les intendants
        if (r == null) {                                                               // etranger : quetes (et terres)
            if (familyId != null && LuoMerchants.claimable(familyId, data(ref, store))) out.add(Cat.MEMBRES);   // maison libre
            if (comteOrMore(ref, store)) out.add(Cat.DOMAINES);
            if (titles) out.add(Cat.TITRES);
            return out;
        }
        out.addAll(List.of(Cat.MEMBRES, Cat.ORDRES, Cat.TRESOR, Cat.DOMAINES, Cat.BIENS, Cat.RESERVE, Cat.QUETES));
        if (titles) out.add(Cat.TITRES);
        UUID mine = me(ref, store);
        boolean king = mine != null && mine.equals(NobilityManager.getCurrentKingUUID());
        if ("eldanior".equals(familyId) && (admin() || king || r.ordinal() >= FamilyRole.VICE.ordinal())) out.add(Cat.ROYAUME);
        if (com.eldanior.system.luo.LuoPlaques.FAMILY.equals(familyId) && r.ordinal() >= FamilyRole.VICE.ordinal()) out.add(Cat.EMPIRE);
        return out;
    }

    /** Le joueur a-t-il quelque chose a faire chez cet intendant (membre, admin, maison a prendre, terres pour un Comte) ? */
    static boolean hasAccess(String familyId, PlayerLevelData data, PlayerRef pr) {
        if (pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return true;
        String fam = familyId != null ? FamilyManager.migrateId(familyId) : null;
        if (fam != null && data != null && fam.equals(FamilyManager.migrateId(data.getNobleFamilyId()))) return true;
        if (fam != null && LuoMerchants.claimable(fam, data)) return true;
        NobilityRank r = data != null ? NobilityRank.fromString(data.getNobilityRank()) : null;
        return r != null && r.ordinal() >= NobilityRank.COMTE.ordinal();
    }

    /**
     * Rappel a la connexion du Patriarche et du Seigneur (2026-11-18) : impots a lever, rapatriements possibles, requetes d'ordre,
     * tribut des vassaux du. null : rien a signaler.
     */
    public static String reminder(String familyId, FamilyRole role) {
        if (familyId == null || role == null || !(role == FamilyRole.PATRIARCH || role == FamilyRole.VICE)) return null;
        String fam = FamilyManager.migrateId(familyId);
        int tax = 0, repatriate = 0, tribute = 0;
        for (ParcelData p : familyDomains(fam)) {
            if (!fam.equals(IntendantLands.house(p))) continue;
            if (TerritoiresTab.collectionRate(p) > 0 && !p.getChildIds().isEmpty() && p.canCollectTax()) tax++;
            if (p.getGuildId().isEmpty() && p.canTransferTreasury() && p.getTreasury() / 2 > 0) repatriate++;
        }
        for (ParcelData p : IntendantLands.vassals(fam)) if (p.canLevyImperialTax() && p.getTreasury() * IMPERIAL_RATE >= 1) tribute++;
        int requests = role == FamilyRole.PATRIARCH ? KnightOrderManager.getPendingRequestsForFamily(fam).size() : 0;
        List<String> parts = new ArrayList<>();
        if (tax > 0) parts.add(tax + (tax > 1 ? " impôts à lever" : " impôt à lever"));
        if (repatriate > 0) parts.add(repatriate + (repatriate > 1 ? " trésors à rapatrier" : " trésor à rapatrier"));
        if (tribute > 0) parts.add(IntendantLands.tributeName(fam) + " due sur " + tribute + " domaine(s)");
        if (requests > 0) parts.add(requests + (requests > 1 ? " requêtes d'ordre" : " requête d'ordre"));
        return parts.isEmpty() ? null : "L'intendant de votre maison vous attend : " + String.join(", ", parts) + ".";
    }

    // ==================== OUVERTURE ====================

    public static void open(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef pr, String familyId, String stewardName) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        player.getPageManager().openCustomPage(ref, store, new IntendantPage(pr, familyId, stewardName));
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Intendant/Intendant.ui");
        for (int i = 0; i < CATS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#IntCat" + i, EventData.of("Action", "cat").append("Index", String.valueOf(i)), false);
        for (int i = 0; i < CARDS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#IntCard" + i, EventData.of("Action", "card").append("Index", String.valueOf(i)), false);
        for (int i = 0; i < ACTIONS; i++)
            for (String id : new String[]{"#IntAct" + i, "#IntActD" + i})
                events.addEventBinding(CustomUIEventBindingType.Activating, id, EventData.of("Action", "act").append("Index", String.valueOf(i))
                        .append("@N0", "#IntInput0.Value").append("@N1", "#IntInput1.Value")
                        .append("@N2", "#IntInput4.Value").append("@T0", "#IntInput2.Value").append("@T1", "#IntInput3.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#IntInput0", EventData.of("Action", "n0").append("@N0", "#IntInput0.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#IntInput1", EventData.of("Action", "n1").append("@N1", "#IntInput1.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#IntInput4", EventData.of("Action", "n2").append("@N2", "#IntInput4.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#IntPrev", EventData.of("Action", "prev"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#IntNext", EventData.of("Action", "next"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#IntClose", EventData.of("Action", "close"), false);
        List<Cat> cs = cats(ref, store);
        cat = cs.isEmpty() ? Cat.QUETES : cs.get(0);
        fill(ui, ref, store);
    }

    // ==================== AFFICHAGE ====================

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Cat> cs = cats(ref, store);
        if (!cs.contains(cat)) cat = cs.isEmpty() ? Cat.QUETES : cs.get(0);
        for (int i = 0; i < CATS; i++) {
            boolean shown = i < cs.size();
            ui.set("#IntCat" + i + ".Visible", shown);
            if (!shown) continue;
            Cat c = cs.get(i);
            boolean on = c == cat;
            String icon = catIcon(c);
            if (icon != null) ui.set("#IntCatIcon" + i + ".ItemId", icon);
            ui.set("#IntCatLabel" + i + ".Text", c.label);
            ui.set("#IntCatLabel" + i + ".Style.TextColor", on ? C_GOLD : "#96a9be");
            ui.set("#IntCatGlow" + i + ".Visible", on);
            ui.set("#IntCatOn" + i + ".Visible", on);
        }

        View v;
        if ((familyId == null || family() == null) && cityId == null)
            v = new View("Domaine sans maison", null, "Ce domaine n'appartient à aucune maison : le registre est vierge.", List.of(), List.of(),
                    intro("Registre vierge", "Aucune maison ne possède ce domaine.", ""));
        else v = view(ref, store);
        lastCards = v.cards();

        ui.set("#IntQuote.Text", "« " + quote(ref, store) + " »");
        for (int i = 0; i < 4; i++) {
            boolean shown = i < v.stats().size();
            ui.set("#IntStat" + i + ".Visible", shown);
            if (!shown) continue;
            ui.set("#IntStatV" + i + ".Text", v.stats().get(i).value());
            ui.set("#IntStatL" + i + ".Text", v.stats().get(i).label());
        }
        ui.set("#IntSection.Text", v.section());
        ui.set("#IntFooter.Text", v.footer() != null ? v.footer() : "");

        int pages = Math.max(1, (v.cards().size() + CARDS - 1) / CARDS);
        page = Math.clamp(page, 0, pages - 1);
        for (int i = 0; i < CARDS; i++) {
            int n = page * CARDS + i;
            boolean shown = n < v.cards().size();
            ui.set("#IntCard" + i + ".Visible", shown);
            if (!shown) continue;
            Card c = v.cards().get(n);
            boolean hasIcon = c.icon() != null && item(c.icon()) != null;
            ui.set("#IntCardIcon" + i + ".Visible", hasIcon);
            if (hasIcon) ui.set("#IntCardIcon" + i + ".ItemId", c.icon());
            ui.set("#IntCardMark" + i + ".Visible", !hasIcon);
            ui.set("#IntCardMark" + i + ".Text", c.mark() != null ? c.mark() : "+");
            ui.set("#IntCardName" + i + ".Text", c.name());
            ui.set("#IntCardValue" + i + ".Text", c.value() != null ? c.value() : "");
            ui.set("#IntCardValue" + i + ".Style.TextColor", c.bar() != null && !C_GREY.equals(c.bar()) ? c.bar() : "#e8d9b0");
            ui.set("#IntCardMeta" + i + ".Text", c.meta() != null ? c.meta() : "");
            ui.set("#IntCardBar" + i + ".Background", c.bar() != null ? c.bar() : C_GREY);
            ui.set("#IntCardSel" + i + ".Visible", c.key().equals(draft ? draftKey() : selected));
        }
        ui.set("#IntEmpty.Visible", v.cards().isEmpty());
        ui.set("#IntEmpty.Text", v.empty() != null ? v.empty() : "");
        ui.set("#IntPager.Visible", pages > 1);
        ui.set("#IntPageLabel.Text", (page + 1) + " / " + pages);

        Detail d = null;
        if (selected == null && cat == Cat.DOMAINES && rulesOf == null && drill != null && drill.startsWith("d:")) selected = drill;   // fiche active du niveau ouvert
        if (draft && cat == Cat.QUETES) d = draftDetail(ref, store);
        else if (selected != null && (selected.equals(drill) || v.cards().stream().anyMatch(c -> c.key().equals(selected)))) {
            try { d = detail(selected, ref, store); } catch (Exception e) { EldaniorLogger.error("[Intendant] fiche " + selected, e); }
        } else selected = null;
        if (d == null) d = v.intro();
        showDetail(ui, d);

        PlayerLevelData pd = data(ref, store);
        Player player = store.getComponent(ref, Player.getComponentType());
        String purse = "Bourse : " + CoinWallet.format(player != null ? CoinWallet.value(player) : 0) + "   ·   Compte : "
                + gold(pd != null ? pd.getMoney() : 0) + " or";
        if (familyId != null && cityId == null && insider(ref, store)) purse += "   ·   Coffres de la maison : " + gold(FamilyManager.getRuntimeData(familyId).getTreasury()) + " or";
        ui.set("#IntPurse.Text", purse);
        ui.set("#IntStatus.Text", status != null ? status : "");
        ui.set("#IntStatus.Style.TextColor", statusColor);
    }

    private void showDetail(UICommandBuilder ui, Detail d) {
        boolean icon = d.icon != null && item(d.icon) != null;
        ui.set("#IntEmblem.Visible", icon);
        if (icon) ui.set("#IntEmblem.ItemId", d.icon);
        ui.set("#IntEmblemMark.Visible", !icon);
        ui.set("#IntEmblemMark.Text", d.mark != null ? d.mark : "");
        ui.set("#IntKicker.Text", d.kicker != null ? d.kicker : "");
        ui.set("#IntTitle.Text", d.title != null ? d.title : "");
        ui.set("#IntTagline.Text", d.tagline != null ? d.tagline : "");
        ui.set("#IntText.Text", d.text != null ? d.text : "");
        for (int i = 0; i < NOTES; i++) {
            boolean shown = i < d.notes.size();
            ui.set("#IntNote" + i + ".Visible", shown);
            if (shown) ui.set("#IntNoteText" + i + ".Text", d.notes.get(i));
        }
        String[] labels = {d.n0, d.n1, d.t0, d.t1, d.n2};
        for (int i = 0; i < 5; i++) {
            ui.set("#IntField" + i + ".Visible", labels[i] != null);
            if (labels[i] != null) ui.set("#IntFieldLabel" + i + ".Text", labels[i]);
        }
        if (pushFields) {                                                             // a l'ouverture d'une fiche seulement
            if (d.v0 != null) { ui.set("#IntInput0.Value", (int) Math.min(Integer.MAX_VALUE, d.v0)); n0 = d.v0; }
            if (d.v1 != null) { ui.set("#IntInput1.Value", (int) Math.min(Integer.MAX_VALUE, d.v1)); n1 = d.v1; }
            if (d.v2 != null) { ui.set("#IntInput4.Value", (int) Math.min(Integer.MAX_VALUE, d.v2)); n2 = d.v2; }
            if (d.t0 != null) { ui.set("#IntInput2.Value", d.s0 != null ? d.s0 : ""); t0 = d.s0 != null ? d.s0 : ""; }
            if (d.t1 != null) { ui.set("#IntInput3.Value", d.s1 != null ? d.s1 : ""); t1 = d.s1 != null ? d.s1 : ""; }
            pushFields = false;
        }
        for (int i = 0; i < ACTIONS; i++) {
            boolean shown = i < d.btns.size();
            boolean danger = shown && DANGER.contains(d.btns.get(i).key());
            ui.set("#IntAct" + i + ".Visible", shown && !danger);
            ui.set("#IntActD" + i + ".Visible", danger);
            if (shown) {
                ui.set("#IntAct" + i + ".Text", d.btns.get(i).label());
                ui.set("#IntActD" + i + ".Text", d.btns.get(i).label());
            }
        }
        ui.set("#IntActHead.Visible", !d.btns.isEmpty());
        for (int i = 0; i < FACTS; i++) {
            boolean shown = i < d.facts.size();
            ui.set("#IntFact" + i + ".Visible", shown);
            if (!shown) continue;
            String[] f = d.facts.get(i);
            ui.set("#IntFactK" + i + ".Text", f[0]);
            ui.set("#IntFactV" + i + ".Text", f[1]);
            ui.set("#IntFactV" + i + ".Style.TextColor", f[2] != null ? f[2] : "#e8d9b0");
        }
        for (int r = 0; r < FACTS / 2; r++) ui.set("#IntFactRow" + r + ".Visible", d.facts.size() > r * 2);
    }

    private Detail intro(String title, String tagline, String text) {
        Detail d = new Detail();
        d.icon = famItem("Eldanior_Banniere_Famille_");
        d.mark = "?";
        d.kicker = "Registre de la maison";
        d.title = title;
        d.tagline = tagline;
        d.text = text;
        return d;
    }

    private String quote(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (cityId != null) {
            ParcelData c = city();
            return c == null ? "Cette ville n'existe plus." : cat == Cat.BIENS ? "Les biens de " + c.getName() + " : maisons, échoppes et terrains à vendre ou à louer."
                    : cat == Cat.TRESOR ? "Le trésor de " + c.getName() + " contient " + gold(c.getTreasury()) + " or. Versez, retirez, vendez ou quittez la ville."
                    : "Le registre de " + c.getName() + ". Trésor, règles, lois et Bailli : tout se décide ici, Comte.";
        }
        if (familyId == null || family() == null) return "Ce domaine n'appartient à aucune maison. Je n'ai rien à consigner.";
        String g = greet(ref, store);
        return switch (cat) {
            case MEMBRES -> g + ". Voici le registre des nôtres. Désignez un nom : je vous dirai ce que je sais de lui.";
            case ORDRES -> "Nos ordres sont de petites compagnies dans la maison, " + g + ". Un Chevalier peut en demander un au Patriarche.";
            case TRESOR -> "Les coffres contiennent " + gold(FamilyManager.getRuntimeData(familyId).getTreasury()) + " or, " + g + ". Tout est au livre.";
            case DOMAINES -> insider(ref, store) ? "Nos terres, " + g + ". Leurs trésors, leurs impôts et leurs règles se décident ici."
                    : "Bienvenue, " + g + ". Si vous cherchez une terre, je vous montre celles que la maison cède.";
            case BIENS -> "Les biens de nos domaines, " + g + ". Une affiche suit chacun d'eux.";
            case RESERVE -> "La réserve est ouverte, " + g + ". Le Patriarche, les Doyens et le Seigneur peuvent la payer sur les coffres.";
            case QUETES -> insider(ref, store) ? "Les appels de la maison, " + g + ". La récompense est consignée chez moi avant d'être promise."
                    : "Bienvenue, " + g + ". La maison " + familyName() + " offre une récompense à qui l'aide.";
            case TITRES -> "Vos lettres de noblesse, " + g + ". Ce que vous avez déjà conféré reste compté, quel que soit votre rang.";
            case ROYAUME -> "Le Royaume d'Eldanior, " + g + ". Les Marquisats doivent la Taxe Royale à la couronne.";
            case EMPIRE -> "L'Empire, " + g + ". Les quatre clans vous doivent la Taxe Impériale, et le Comptoir attend vos agréments.";
        };
    }

    private View view(Ref<EntityStore> ref, Store<EntityStore> store) {
        return switch (cat) {
            case MEMBRES -> membres(ref, store);
            case ORDRES -> ordres(ref, store);
            case TRESOR -> tresor(ref, store);
            case DOMAINES -> domaines(ref, store);
            case BIENS -> biens(ref, store);
            case RESERVE -> reserve(ref, store);
            case QUETES -> quetes(ref, store);
            case TITRES -> titres(ref, store);
            case ROYAUME, EMPIRE -> vassalView(ref, store);
        };
    }

    private Detail detail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        return switch (cat) {
            case MEMBRES -> memberDetail(key, ref, store);
            case ORDRES -> orderDetail(key, ref, store);
            case TRESOR -> treasuryDetail(key, ref, store);
            case DOMAINES -> domainDetail(key, ref, store);
            case BIENS -> estateDetail(key, ref, store);
            case RESERVE -> articleDetail(key, ref, store);
            case QUETES -> questDetail(key, ref, store);
            case TITRES -> titleDetail(key, ref, store);
            case ROYAUME, EMPIRE -> empireDetail(key, ref, store);
        };
    }

    // ==================== MEMBRES ====================

    private List<FamilyRegistry.Entry> roster() {
        FamilleTab.onlineMembers(familyId);                                            // note les presents au registre
        List<FamilyRegistry.Entry> out = new ArrayList<>(FamilyRegistry.members(familyId));
        out.sort((a, b) -> {
            int c = Integer.compare(roleOf(b).ordinal(), roleOf(a).ordinal());
            if (c != 0) return c;
            boolean ha = online(a.uuid()), hb = online(b.uuid());
            if (ha != hb) return ha ? -1 : 1;
            return Integer.compare(b.level(), a.level());
        });
        return out;
    }

    private FamilyRole roleOf(FamilyRegistry.Entry e) {
        FamilyRole p = FamilyRegistry.pendingRole(familyId, e.uuid());
        if (p != null) return p;
        FamilyRole r = FamilyRole.of(e.role());
        return r != null ? r : FamilyRole.MEMBER;
    }

    private static boolean online(UUID u) { return u != null && Universe.get().getPlayer(u) != null; }

    private View membres(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<FamilyRegistry.Entry> roster = roster();
        List<Card> cards = new ArrayList<>();
        UUID mine = me(ref, store);
        if (!FamilyManager.isFamilyTaken(familyId) && LuoMerchants.claimable(familyId, data(ref, store)))
            cards.add(new Card("claim", famItem("Eldanior_Cle_Famille_"), "+", "Prendre la tête", "Cette maison n'a pas de Patriarche : prenez-en la tête.",
                    null, C_GOLD));
        for (FamilyRegistry.Entry e : roster) {
            FamilyRole r = roleOf(e);
            boolean here = online(e.uuid());
            String rank = martialRank(e.classId());
            boolean pending = FamilyRegistry.pendingRole(familyId, e.uuid()) != null;
            cards.add(new Card("m:" + e.uuid(), roleItem(r), r.label().substring(0, 1), e.name(),
                    r.label() + (pending ? " (à sa prochaine visite)" : "") + (rank != null ? "  ·  " + rank : "") + "\n"
                            + (here ? "Au domaine" : "Vu " + FamilyRegistry.ago(e.seen())),
                    "Niv. " + e.level(), e.uuid().equals(mine) ? C_GOLD : here ? C_GREEN : C_GREY));
        }
        var dukes = com.eldanior.system.titles.nobility.family.HouseRoles.vassalDukes(familyId);
        java.util.Set<UUID> listed = new java.util.HashSet<>();
        for (FamilyRegistry.Entry e : roster) listed.add(e.uuid());
        for (var dk : dukes) {
            if (listed.contains(dk.uuid())) continue;                                 // deja inscrit : sa carte de membre suffit
            cards.add(new Card("dk:" + dk.uuid(), roleItem(FamilyRole.ELDER), "D", dk.name(), "Doyen d'office  ·  " + dk.title() + " de " + dk.land() + "\n" + (online(dk.uuid()) ? "Au domaine" : dk.entry() != null ? "Vu " + FamilyRegistry.ago(dk.entry().seen()) : "Absent"),
                    dk.entry() != null ? "Niv. " + dk.entry().level() : dk.title(), dk.uuid().equals(mine) ? C_GOLD : C_ORANGE));
        }
        long online = roster.stream().filter(e -> online(e.uuid())).count();
        long knights = roster.stream().filter(e -> roleOf(e) == FamilyRole.CHEVALIER || roleOf(e) == FamilyRole.CHEVALIER_ROYAL).count();
        NobleFamilyModel f = family();
        StringBuilder help = new StringBuilder("« ").append(f.getMotto()).append(" »\n\n");
        String h = f.getHistory();
        if (h != null && !h.isEmpty()) help.append(h.length() > 420 ? h.substring(0, 420).trim() + "…" : h).append("\n\n");
        Detail intro = intro("Maison " + familyName(), "Maison " + f.getRarity().name().toLowerCase() + ", rang requis : "
                + f.getMinimumRank().getDisplayName(), help.toString());
        intro.note("Rangs : Patriarche, Doyen, Seigneur, Noble, Chevalier Royal, Chevalier, Recrue.");
        intro.note("D'office : Doyen = Duc (et Patriarche d'une maison ducale du Marquisat), Noble = Comte ou Baron, Chevalier Royal = Chevalier.");
        intro.note("Le Patriarche choisit Recrue, Chevalier et Seigneur ; les Doyens et le Seigneur, Recrue et Chevalier.");
        intro.note("Un membre absent change de rang à sa prochaine visite.");
        return new View("Les nôtres", roster.size() + " inscrit(s)", "Personne n'est inscrit : la maison attend son Patriarche.", cards,
                List.of(new Stat(String.valueOf(roster.size()), "Inscrits"), new Stat(String.valueOf(online), "Au domaine"),
                        new Stat(String.valueOf(roster.stream().filter(e -> roleOf(e) == FamilyRole.ELDER).count() + dukes.size()), "Doyens"),
                        new Stat(String.valueOf(knights), "Chevaliers")), intro);
    }

    private FamilyRegistry.Entry entry(String key) {
        if (key == null || !key.startsWith("m:")) return null;
        UUID u = UUID.fromString(key.substring(2));
        for (FamilyRegistry.Entry e : FamilyRegistry.members(familyId)) if (e.uuid().equals(u)) return e;
        return null;
    }

    private Detail memberDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        if ("claim".equals(key)) {
            long price = ParcelManager.getFamilyTerritoryPrice(familyId);
            Detail d = intro("Prendre la tête", "Maison " + familyName(), "Vous en devenez le Patriarche et recevez ses terres"
                    + (price > 0 ? " contre " + gold(price) + " or de votre compte." : "."));
            d.icon = famItem("Eldanior_Cle_Famille_");
            d.note("Le Patriarche reçoit la tenue de son rang.");
            return d.btn(armed("claim") ? "Confirmer" : "Prendre la tête", "claim");
        }
        if (key.startsWith("dk:")) {
            UUID u = UUID.fromString(key.substring(3));
            for (var dk : com.eldanior.system.titles.nobility.family.HouseRoles.vassalDukes(familyId)) {
                if (!dk.uuid().equals(u)) continue;
                var hf = dk.house().isEmpty() ? null : FamilyManager.get(dk.house());
                Detail d = intro(dk.name(), "Doyen d'office", "« " + dk.title() + " de " + dk.land() + (hf != null ? ", Patriarche de la maison "
                        + hf.getDisplayName() : "") + " : sa terre relève des nôtres, il siège parmi nos Doyens. »");
                d.icon = roleItem(FamilyRole.ELDER);
                d.kicker = "Registre de la maison " + familyName();
                d.note("Ce rang vient de sa terre (" + dk.land() + ") : il ne se donne ni ne se retire ici.");
                if (dk.entry() != null) d.note("Niveau " + dk.entry().level() + "  ·  " + (online(u) ? "présent." : "vu " + FamilyRegistry.ago(dk.entry().seen()) + "."));
                return d;
            }
            return null;
        }
        FamilyRegistry.Entry e = entry(key);
        if (e == null) return null;
        FamilyRole viewer = role(ref, store), r = roleOf(e);
        boolean self = e.uuid().equals(me(ref, store)), here = online(e.uuid());
        Detail d = new Detail();
        d.icon = roleItem(r);
        d.mark = r.label().substring(0, 1);
        d.kicker = "Registre de la maison " + familyName();
        d.title = e.name();
        d.tagline = r.label();
        d.text = "« " + remark(r) + " »";
        profile(d, e, r, here);
        if (FamilyRegistry.pendingRole(familyId, e.uuid()) != null) d.note("Nouveau rang pris à sa prochaine visite.");
        if (self) { d.note("C'est vous."); return d; }
        if (viewer == null) return d;
        if (r.automatic()) d.note("Rang donné par son titre de noblesse" + (r == FamilyRole.ELDER ? " (Duc)." : r == FamilyRole.NOBLE ? " (Comte ou Baron)." : " (Chevalier)."));
        FamilyRole up = r.promoted(), down = r.demoted();
        if (up != null && viewer.canChange(r, up)) d.btn(up == FamilyRole.VICE ? "Nommer Seigneur" : "Élever : " + up.label(), "up");
        if (down != null && viewer.canChange(r, down)) d.btn(r == FamilyRole.VICE ? "Retirer le titre de Seigneur" : "Abaisser : " + down.label(), "down");
        KnightOrder mineOrder = me(ref, store) != null ? KnightOrderManager.getPlayerOrder(me(ref, store)) : null;
        var myOrderRank = mineOrder != null ? mineOrder.getRoleOf(me(ref, store)) : null;
        if (mineOrder != null && myOrderRank != null && myOrderRank.officer() && KnightOrderManager.getPlayerOrder(e.uuid()) == null
                && mineOrder.getMemberCount() < KnightOrder.MAX_MEMBERS && familyId.equals(FamilyManager.migrateId(mineOrder.getFamilyId())))
            d.btn("Enrôler : Ordre " + mineOrder.getName(), "enrol");
        if (viewer == FamilyRole.PATRIARCH && r != FamilyRole.PATRIARCH) d.btn(armed("kick") ? "Confirmer : rayer" : "Rayer du registre", "kick");
        return d;
    }

    /**
     * Profil d'un membre (2026-11-19, « beaucoup plus d'informations quand je clique sur son profil ») : faits en tete (rang,
     * niveau, classe, titre, ordre, presence, terres, biens), puis notes : terres tenues, guilde, combats, exploration, prime,
     * dernieres lignes du livre de comptes qui le nomment. Joueur connecte : sa fiche complete ; absent : le registre.
     */
    private void profile(Detail d, FamilyRegistry.Entry e, FamilyRole r, boolean here) {
        PlayerLevelData pd = null;
        String where = null;
        PlayerRef pr = Universe.get().getPlayer(e.uuid());
        if (pr != null) {
            Ref<EntityStore> pref = pr.getReference();
            if (pref != null && pref.isValid()) {
                try { pd = pref.getStore().getComponent(pref, EldaniorSystem.get().getPlayerLevelDataType()); } catch (Exception ignored) { }
                var at = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(e.uuid());
                var w = pref.getStore().getExternalData().getWorld();
                if (at != null && w != null) {
                    ParcelData z = ParcelManager.getParcelAt(w.getName(), at.x, at.y, at.z);
                    where = z != null ? z.getName() : "en pleine nature";
                }
            }
        }
        String martial = martialRank(e.classId());
        d.fact("Rang", r.label(), r == FamilyRole.PATRIARCH ? C_GOLD : r.ordinal() >= FamilyRole.NOBLE.ordinal() ? C_VIOLET : null);
        d.fact("Niveau", String.valueOf(pd != null ? pd.getLevel() : e.level()));
        String cls = pd != null && pd.getPlayerClass() != null && !pd.getPlayerClass().isEmpty() ? pd.getPlayerClass() : martial != null ? martial
                : e.classId() != null && !e.classId().isEmpty() ? e.classId() : "—";
        d.fact("Classe", cls);
        NobilityRank nob = pd != null ? NobilityRank.fromString(pd.getNobilityRank()) : null;
        d.fact("Titre", nob != null ? nob.getDisplayName() : pd != null ? "Roturier" : "—");
        KnightOrder order = KnightOrderManager.getPlayerOrder(e.uuid());
        d.fact("Ordre", order != null ? order.getName() + " · " + order.getRoleOf(e.uuid()).title(familyId) : "aucun");
        d.fact("Présence", here ? (where != null ? where : "connecté") : "vu " + FamilyRegistry.ago(e.seen()), here ? C_GREEN : C_GREY);
        List<String> lands = new ArrayList<>();
        int estates = 0, rented = 0;
        for (ParcelData p : ParcelManager.getAll()) {
            boolean own = p.isOwner(e.uuid());
            if (IntendantLands.isDomain(p)) { if (own) lands.add(p.getType().getLabel() + " " + p.getName()); continue; }
            if (own) estates++;
            else if (e.uuid().equals(p.getRenterUUID())) rented++;
        }
        d.fact("Terres", lands.isEmpty() ? "aucune" : String.valueOf(lands.size()), lands.isEmpty() ? null : C_GOLD);
        d.fact("Biens", estates + " possédé(s)" + (rented > 0 ? ", " + rented + " loué(s)" : ""));
        if (!lands.isEmpty()) d.note("Terres tenues : " + String.join(", ", lands) + ".");
        if (pd != null) {
            var guild = com.eldanior.system.guild.GuildManager.getPlayerGuild(e.uuid());
            if (guild != null) d.note("Guilde : " + guild.getName() + (pd.getGuildRank() != null && !pd.getGuildRank().isEmpty() ? " (" + pd.getGuildRank() + ")" : "") + ".");
            d.note("Combats : " + pd.getTotalMobKills() + " monstres abattus, " + pd.getPlayerKills() + " joueur(s) vaincu(s), "
                    + pd.getPlayerDeaths() + " mort(s) ; duels " + pd.getDuelWins() + " victoire(s) pour " + pd.getDuelLosses() + " défaite(s).");
            d.note("Exploration : " + pd.getTerritoriesDiscovered() + " territoire(s), " + pd.getDungeonsDiscovered() + " donjon(s), "
                    + pd.getChestsDiscovered() + " coffre(s) au trésor découverts.");
            if (pd.getBounty() > 0 || pd.isPK())
                d.note((pd.isPK() ? "Recherché par la garde" : "Une prime pèse sur sa tête") + (pd.getBounty() > 0 ? " : " + gold(pd.getBounty()) + " or." : "."));
        } else d.note("Absent : je ne connais de lui que ce que dit le registre.");
        List<String> mentions = new ArrayList<>();
        List<FamilyRegistry.Line> book = FamilyRegistry.journal(familyId);
        for (int i = book.size() - 1; i >= 0 && mentions.size() < 3; i--)
            if (book.get(i).text().contains(e.name())) mentions.add(FamilyRegistry.ago(book.get(i).at()) + " : " + book.get(i).text());
        if (!mentions.isEmpty()) d.note("Au livre de comptes : " + String.join(" ; ", mentions) + ".");
    }

    private static String remark(FamilyRole r) {
        return switch (r) {
            case PATRIARCH -> "Le chef de notre maison. Sa parole fait loi, et je tiens ses comptes.";
            case ELDER -> "Un Doyen. Ses conseils ont le poids des années.";
            case VICE -> "Le Seigneur de nos terres : il veille sur le domaine quand le Patriarche est loin.";
            case NOBLE -> "Un Noble de sang. Il porte nos couleurs avec honneur.";
            case CHEVALIER_ROYAL -> "Un Chevalier Royal. Sa lame a déjà servi la maison plus d'une fois.";
            case CHEVALIER -> "Un Chevalier de la maison. Il peut demander à lever un ordre.";
            default -> "Une recrue. À vous de juger si elle mérite davantage.";
        };
    }

    private String roleItem(FamilyRole r) {
        if (com.eldanior.system.luo.LuoPlaques.FAMILY.equals(familyId)) {
            String p = switch (r) {
                case PATRIARCH -> "Patriarche";
                case ELDER -> "Doyen";
                case VICE -> "Seigneur";
                case NOBLE -> "Noble";
                default -> "Recrue";
            };
            return item("Eldanior_Plaque_Luo_" + p);
        }
        String clan = famItem("Eldanior_Plaque_Luo_Clan_");
        if (clan != null) return clan;
        return r == FamilyRole.PATRIARCH ? famItem("Eldanior_Cle_Famille_") : famItem("Eldanior_Banniere_Famille_");
    }

    private static String martialRank(String classId) {
        int t = com.eldanior.system.classes.definitions.luo.VoieMartiale.tier(classId);
        return t > 0 ? com.eldanior.system.classes.definitions.luo.VoieMartiale.className(t) : null;
    }

    // ==================== ORDRES ====================

    private View ordres(Ref<EntityStore> ref, Store<EntityStore> store) {
        KnightOrder open = openOrder();
        if (open != null) return orderMembers(open, ref, store);
        if ("ofound".equals(drill) && patriarch(ref, store) && pendingOrderName != null) return foundView(ref, store);
        drill = null;
        List<Card> cards = new ArrayList<>();
        UUID mine = me(ref, store);
        FamilyRole r = role(ref, store);
        boolean asked = KnightOrderManager.getPendingRequestsForFamily(familyId).stream().anyMatch(q -> q.getRequesterUUID().equals(mine));
        if (r == FamilyRole.PATRIARCH)
            cards.add(new Card("o+new", null, "+", "Lever un ordre", "Un nom, une devise et son Grand Maître ; "
                    + gold(KnightOrderManager.ORDER_PRICE) + " or pris aux coffres.", null, C_GOLD));
        else if (r != null && r.knightly() && KnightOrderManager.getPlayerOrder(mine) == null && !asked)
            cards.add(new Card("o+new", null, "+", "Demander un ordre", "Un nom et une devise ; vous en serez le Grand Maître.", null, C_GOLD));
        List<KnightOrder> orders = KnightOrderManager.getOrdersForFamily(familyId);
        for (KnightOrder k : orders)
            cards.add(new Card("o:" + k.getId(), famItem("Eldanior_Banniere_Famille_"), "O", "Ordre " + k.getName(),
                    GM(k) + " " + k.getCapitaineName() + "\n" + (k.isRecruiting() ? "Recrutement sur demande" : "Ordre d'élite : recrutement fermé")
                            + (k.getCandidates().isEmpty() ? "" : "  ·  " + k.getCandidates().size() + " demande(s)"), k.getMemberCount() + " / " + KnightOrder.MAX_MEMBERS,
                    mine != null && k.hasMember(mine) ? C_GOLD : C_BLUE));
        for (var q : KnightOrderManager.getPendingRequestsForFamily(familyId))
            cards.add(new Card("r:" + q.getRequesterUUID(), null, "?", "Requête : " + q.getOrderName(), "Présentée par " + q.getRequesterName()
                    + "\n« " + q.getOrderMotto() + " »", "En attente", C_VIOLET));
        Detail intro = intro("Ordres de chevalerie", "Une petite guilde dans la maison",
                "Un ordre a sept rangs, du Grand Maître (celui qui l'a levé) à l'Aspirant, et jusqu'à " + KnightOrder.MAX_MEMBERS + " membres de la maison.");
        intro.note("Les Chevaliers (ou plus) demandent à en lever un ; le Patriarche approuve.");
        intro.note("Autant d'ordres que la maison le veut : chacun coûte " + gold(KnightOrderManager.ORDER_PRICE) + " or aux coffres.");
        intro.note("Recrutement sur demande : un membre demande à entrer, un officier l'accepte ; il entre Aspirant.");
        intro.note("Recrutement fermé (ordre d'élite) : seuls les officiers enrôlent, depuis la fiche d'un membre.");
        return new View("Ordres de chevalerie", orders.size() + " ordre(s)", "Aucun ordre n'a encore prêté serment à la maison.",
                cards, List.of(new Stat(String.valueOf(orders.size()), "Ordres"),
                new Stat(String.valueOf(orders.stream().mapToInt(KnightOrder::getMemberCount).sum()), "Membres d'ordres"),
                new Stat(String.valueOf(KnightOrderManager.getPendingRequestsForFamily(familyId).size()), "Requêtes"),
                new Stat(gold(orders.stream().mapToLong(KnightOrder::getTotalKills).sum()), "Victoires")), intro);
    }

    private static String GM(KnightOrder k) {
        return com.eldanior.system.titles.nobility.family.KnightOrderRole.GRAND_MAITRE.title(k.getFamilyId());
    }

    private Detail orderDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (key.startsWith("om:")) return orderMemberDetail(key, ref, store);
        UUID mine = me(ref, store);
        boolean patriarch = patriarch(ref, store);
        if (key.startsWith("og:")) return founderDetail(key, ref, store);
        if (key.startsWith("oc:")) return candidateDetail(key, ref, store);
        if ("o+new".equals(key) && patriarch) {
            Detail d = intro("Lever un ordre", "Ordre de chevalerie de " + familyName(), "« Donnez-lui un nom de trois à vingt-quatre lettres et une "
                    + "devise. Vous choisirez ensuite son Grand Maître parmi les Chevaliers de la maison, ou vous-même. »");
            d.note("Prix : " + gold(KnightOrderManager.ORDER_PRICE) + " or pris aux coffres (" + gold(FamilyManager.getRuntimeData(familyId).getTreasury()) + " or).");
            d.t0 = "Nom de l'ordre";
            d.t1 = "Devise";
            return d.btn("Choisir le Grand Maître", "foundpick");
        }
        if ("o+new".equals(key)) {
            Detail d = intro(patriarch ? "Lever un ordre" : "Demander un ordre", "Ordre de chevalerie de " + familyName(),
                    "« Donnez-lui un nom de trois à vingt-quatre lettres, et une devise que vos chevaliers crieront en chargeant. »"
                            + (patriarch ? "" : "\n\nJe porterai votre requête au Patriarche.") + "\n\nVous en serez le Grand Maître.");
            d.t0 = "Nom de l'ordre";
            d.t1 = "Devise";
            return d.btn(patriarch ? "Fonder l'ordre" : "Présenter la requête", "found");
        }
        if (key.startsWith("r:")) {
            UUID req = UUID.fromString(key.substring(2));
            for (var q : KnightOrderManager.getPendingRequestsForFamily(familyId)) {
                if (!q.getRequesterUUID().equals(req)) continue;
                Detail d = intro("Requête : ordre " + q.getOrderName(), "présentée par " + q.getRequesterName(),
                        "Devise proposée : « " + q.getOrderMotto() + " »");
                d.note(patriarch ? "Si vous l'approuvez, " + q.getRequesterName() + " en sera le Grand Maître." : "Le Patriarche examinera la requête.");
                d.note("Prix : " + gold(KnightOrderManager.ORDER_PRICE) + " or pris aux coffres à l'approbation.");
                if (patriarch) d.btn("Approuver", "approve").btn("Refuser", "reject");
                return d;
            }
            return null;
        }
        KnightOrder k = KnightOrderManager.get(key.substring(2));
        if (k == null) return null;
        Detail d = new Detail();
        d.icon = famItem("Eldanior_Banniere_Famille_");
        d.mark = "O";
        d.kicker = "Ordre de chevalerie";
        d.title = "Ordre " + k.getName();
        d.tagline = "« " + k.getMotto() + " »";
        java.util.Map<String, Integer> byRank = new java.util.LinkedHashMap<>();
        for (UUID u : k.sortedMembers()) byRank.merge(k.getRoleOf(u).title(familyId), 1, Integer::sum);
        StringBuilder ranks = new StringBuilder();
        byRank.forEach((t, n) -> ranks.append(ranks.length() == 0 ? "" : ", ").append(n).append(" ").append(t));
        d.text = "Hiérarchie : " + ranks + ".";
        d.note(GM(k) + " : " + k.getCapitaineName());
        d.note("Vice-Maître : " + (k.getLieutenantName() != null ? k.getLieutenantName() : "aucun"));
        d.note("Membres : " + k.getMemberCount() + " / " + KnightOrder.MAX_MEMBERS + "  ·  victoires : " + gold(k.getTotalKills()));
        var myRank = mine != null ? k.getRoleOf(mine) : null;
        if (myRank != null) d.note("Votre rang : " + myRank.title(familyId) + " (" + myRank.numeral() + ").");
        d.btn("Voir les membres", "omembers");
        d.note(k.isRecruiting() ? "Recrutement sur demande : ouvert." : "Recrutement fermé : ordre d'élite, ses officiers enrôlent qui ils veulent.");
        boolean inHouse = member(ref, store);
        if (inHouse && mine != null && KnightOrderManager.getPlayerOrder(mine) == null && k.getMemberCount() < KnightOrder.MAX_MEMBERS) {
            if (k.getCandidates().containsKey(mine)) d.btn("Retirer ma demande", "unapply");
            else if (k.isRecruiting()) d.btn("Demander à rejoindre", "apply");
        }
        var actorRank = orderActor(k, ref, store);
        if (actorRank != null && actorRank.ordinal() <= com.eldanior.system.titles.nobility.family.KnightOrderRole.VICE_MAITRE.ordinal())
            d.btn(k.isRecruiting() ? "Fermer le recrutement" : "Ouvrir le recrutement", "recruit");
        if (mine != null && k.hasMember(mine) && !k.isCapitaine(mine)) d.btn("Quitter l'ordre", "leave");
        if (patriarch || (mine != null && k.isCapitaine(mine))) d.btn(armed("dissolve") ? "Confirmer : dissoudre" : "Dissoudre l'ordre", "dissolve");
        return d;
    }

    // ---- membres d'un ordre (hierarchie a sept rangs, 2026-11-18) ----

    private KnightOrder openOrder() {
        KnightOrder k = drill != null && drill.startsWith("o:") ? KnightOrderManager.get(drill.substring(2)) : null;
        if (k == null || !familyId.equals(FamilyManager.migrateId(k.getFamilyId()))) return null;
        return k;
    }

    private FamilyRegistry.Entry registryEntry(UUID u) {
        for (FamilyRegistry.Entry e : FamilyRegistry.members(familyId)) if (e.uuid().equals(u)) return e;
        return null;
    }

    private String orderName(KnightOrder k, UUID u) {
        String n = k.nameOf(u);
        if (n == null) n = nameOf(u);
        return n != null ? n : "Membre inconnu";
    }

    /** Rang de la maison du membre (null : hors du registre). */
    private FamilyRole houseRole(UUID u) {
        FamilyRegistry.Entry e = registryEntry(u);
        return e != null ? roleOf(e) : null;
    }

    /** Rang avec lequel le joueur commande dans cet ordre : le Patriarche (ou un admin) agit en Grand Maitre. */
    private com.eldanior.system.titles.nobility.family.KnightOrderRole orderActor(KnightOrder k, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (patriarch(ref, store)) return com.eldanior.system.titles.nobility.family.KnightOrderRole.GRAND_MAITRE;
        UUID mine = me(ref, store);
        return mine != null ? k.getRoleOf(mine) : null;
    }

    private View orderMembers(KnightOrder k, Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Card> cards = new ArrayList<>();
        UUID mine = me(ref, store);
        cards.add(new Card("nav:orders", null, "«", "Retour aux ordres", "Revenir à la liste des ordres de la maison.", null, C_VIOLET));
        var actorRank = orderActor(k, ref, store);
        for (var c : k.getCandidates().entrySet()) {
            FamilyRole fr = houseRole(c.getKey());
            cards.add(new Card("oc:" + c.getKey(), roleItem(fr != null ? fr : FamilyRole.MEMBER), "?", c.getValue(), "Demande à entrer dans l'ordre\n"
                    + (fr != null ? fr.label() + " de la maison" : "hors du registre"), "Demande", C_ORANGE));
        }
        int officers = 0, aspirants = 0;
        for (UUID u : k.sortedMembers()) {
            var kr = k.getRoleOf(u);
            if (kr.officer()) officers++;
            if (kr == com.eldanior.system.titles.nobility.family.KnightOrderRole.ASPIRANT) aspirants++;
            FamilyRegistry.Entry e = registryEntry(u);
            FamilyRole fr = houseRole(u);
            cards.add(new Card("om:" + u, roleItem(fr != null ? fr : FamilyRole.MEMBER), kr.numeral(), orderName(k, u),
                    kr.title(familyId) + "\n" + (fr != null ? fr.label() + " de la maison" : "hors du registre") + "  ·  "
                            + (online(u) ? "au domaine" : e != null ? "vu " + FamilyRegistry.ago(e.seen()) : "absent"),
                    "Rang " + kr.numeral(), u.equals(mine) ? C_GOLD : kr.ordinal() <= 1 ? C_ORANGE : kr.officer() ? C_BLUE : C_GREY));
        }
        Detail intro = intro("Ordre " + k.getName(), "« " + k.getMotto() + " »", "Sept rangs, du Grand Maître à l'Aspirant. Un officier ne "
                + "change que le rang de ceux sous lui, et jamais au-delà du rang juste sous le sien.");
        intro.icon = famItem("Eldanior_Banniere_Famille_");
        intro.kicker = "Hiérarchie de l'ordre";
        intro.note("I Grand Maître (1)  ·  II Vice-Maître (1)  ·  III Haut Commandant (2)  ·  IV Commandant (4).");
        intro.note("V Maître  ·  VI Disciple  ·  VII Aspirant (entrée par serment).");
        intro.note("Les rangs d'officier (Commandant et plus) sont réservés aux Chevaliers de la maison.");
        intro.note("Le Patriarche agit en Grand Maître dans tous les ordres de la maison.");
        return new View("Ordres  »  " + k.getName(), k.getMemberCount() + " / " + KnightOrder.MAX_MEMBERS + " membres", "L'ordre est vide.", cards,
                List.of(new Stat(k.getMemberCount() + " / " + KnightOrder.MAX_MEMBERS, "Membres"), new Stat(String.valueOf(officers), "Officiers"),
                        new Stat(String.valueOf(aspirants), "Aspirants"), new Stat(k.isRecruiting() ? String.valueOf(k.getCandidates().size()) : "Fermé", "Demandes")), intro);
    }

    /** Fondation par le Patriarche : la grille montre qui peut en etre le Grand Maitre (Chevaliers sans ordre, et lui-meme). */
    private View foundView(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("nav:orders", null, "X", "Annuler", "Revenir aux ordres sans rien fonder.", null, C_RED));
        UUID mine = me(ref, store);
        if (mine != null && KnightOrderManager.getPlayerOrder(mine) == null)
            cards.add(new Card("og:" + mine, roleItem(FamilyRole.PATRIARCH), "P", username() + " (vous)", "Patriarche", null, C_GOLD));
        for (FamilyRegistry.Entry e : roster()) {
            FamilyRole fr = roleOf(e);
            if (e.uuid().equals(mine) || !fr.knightly() || KnightOrderManager.getPlayerOrder(e.uuid()) != null) continue;
            cards.add(new Card("og:" + e.uuid(), roleItem(fr), fr.label().substring(0, 1), e.name(), fr.label() + "  ·  niveau " + e.level() + "\n"
                    + (online(e.uuid()) ? "Au domaine" : "Vu " + FamilyRegistry.ago(e.seen())), null, C_BLUE));
        }
        Detail intro = intro("Ordre " + pendingOrderName, "« " + pendingOrderMotto + " »", "Désignez à droite son Grand Maître : un Chevalier de la "
                + "maison (ou plus) qui ne sert encore aucun ordre, ou vous-même.");
        intro.note("Prix : " + gold(KnightOrderManager.ORDER_PRICE) + " or pris aux coffres.");
        return new View("Ordres  »  Fonder : " + pendingOrderName, "Choisissez le Grand Maître", "Aucun Chevalier libre dans la maison.", cards,
                List.of(new Stat(gold(FamilyManager.getRuntimeData(familyId).getTreasury()) + " or", "Coffres"),
                        new Stat(gold(KnightOrderManager.ORDER_PRICE) + " or", "Prix de l'ordre"),
                        new Stat(String.valueOf(cards.size() - 1), "Grands Maîtres possibles"),
                        new Stat(String.valueOf(KnightOrderManager.getOrdersForFamily(familyId).size()), "Ordres de la maison")), intro);
    }

    private Detail founderDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID u = UUID.fromString(key.substring(3));
        String name = u.equals(me(ref, store)) ? username() : nameOf(u);
        long coffers = FamilyManager.getRuntimeData(familyId).getTreasury();
        Detail d = intro("Ordre " + pendingOrderName, "Grand Maître : " + (name != null ? name : "?"), "« " + pendingOrderMotto + " »");
        d.icon = famItem("Eldanior_Banniere_Famille_");
        d.kicker = "Fonder un ordre";
        d.note("Prix : " + gold(KnightOrderManager.ORDER_PRICE) + " or ; coffres : " + gold(coffers) + " or"
                + (coffers < KnightOrderManager.ORDER_PRICE ? " : il en manque." : "."));
        return d.btn("Fonder  ·  " + gold(KnightOrderManager.ORDER_PRICE) + " or", "foundgo");
    }

    private Detail candidateDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        KnightOrder k = openOrder();
        if (k == null) return null;
        UUID u = UUID.fromString(key.substring(3));
        String name = k.getCandidates().get(u);
        if (name == null) return null;
        FamilyRole fr = houseRole(u);
        Detail d = intro(name, "Demande à entrer dans l'Ordre " + k.getName(), "Accepté, il entre Aspirant.");
        d.icon = roleItem(fr != null ? fr : FamilyRole.MEMBER);
        d.kicker = "Recrutement";
        d.note("Dans la maison : " + (fr != null ? fr.label() : "hors du registre") + ".");
        var actor = orderActor(k, ref, store);
        if (actor != null && actor.officer()) d.btn("Accepter", "caccept").btn("Refuser", "crefuse");
        else d.note("Un officier de l'ordre (Commandant ou plus) en décidera.");
        return d;
    }

    private Detail orderMemberDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        KnightOrder k = openOrder();
        if (k == null) return null;
        UUID u = UUID.fromString(key.substring(3));
        var kr = k.getRoleOf(u);
        if (kr == null) return null;
        UUID mine = me(ref, store);
        FamilyRole fr = houseRole(u);
        FamilyRegistry.Entry e = registryEntry(u);
        Detail d = new Detail();
        d.icon = roleItem(fr != null ? fr : FamilyRole.MEMBER);
        d.mark = kr.numeral();
        d.kicker = "Ordre " + k.getName() + "  ·  rang " + kr.numeral();
        d.title = orderName(k, u);
        d.tagline = kr.title(familyId);
        d.text = "« " + kr.duty() + " »";
        d.note("Dans la maison : " + (fr != null ? fr.label() : "hors du registre") + (e != null ? "  ·  niveau " + e.level() : "") + ".");
        d.note(kr.max() > 0 ? "Places à ce rang : " + k.count(kr) + " / " + kr.max() + "." : "Ce rang n'a pas de limite de places.");
        d.note(online(u) ? "Présent au domaine." : e != null ? "Absent ; dernière visite " + FamilyRegistry.ago(e.seen()) + "." : "Absent.");
        if (u.equals(mine)) { d.note("C'est vous."); return d; }
        var actor = orderActor(k, ref, store);
        if (actor == null) return d;
        boolean knightly = fr != null && fr.knightly();
        var up = kr.up();
        String refuseUp = up != null ? k.refuseRank(actor, u, up, knightly) : "";
        if (up != null && refuseUp == null) d.btn("Élever : " + up.title(familyId), "orup");
        else if (up != null && kr.ordinal() > actor.ordinal() + 1) d.note(refuseUp);
        var down = kr.down();
        if (down != null && k.refuseRank(actor, u, down, true) == null) d.btn("Abaisser : " + down.title(familyId), "ordown");
        if (actor.officer() && kr.ordinal() > actor.ordinal())
            d.btn(armed("orkick") ? "Confirmer : renvoyer" : "Renvoyer de l'ordre", "orkick");
        return d;
    }

    private String nameOf(UUID u) {
        PlayerRef pr = Universe.get().getPlayer(u);
        if (pr != null) return pr.getUsername();
        for (FamilyRegistry.Entry e : FamilyRegistry.members(familyId)) if (e.uuid().equals(u)) return e.name();
        return null;
    }

    // ==================== TRESOR ====================

    private long allowanceLeft(Ref<EntityStore> ref, Store<EntityStore> store) {
        FamilyRole r = role(ref, store);
        if (r == FamilyRole.PATRIARCH) return Long.MAX_VALUE;
        if (r == null || !r.stewardOfTreasury()) return 0;
        return Math.max(0, FamilyRegistry.allowance(familyId) - FamilyRegistry.spent(familyId, me(ref, store)));
    }

    /** Genre d'une ligne du livre de comptes (couleur et libelle des cartes), devine d'apres le texte. */
    private static String[] genre(String t) {
        String l = t.toLowerCase();
        if (l.contains(" verse") || l.contains("lève") || l.contains("rapatrie") || l.contains("taxe") || l.contains("vendu")) return new String[]{"Entrée", C_GREEN};
        if (l.contains("puise") || l.contains("réserve") || l.contains("coffres :") || l.contains("retire") && l.contains(" or")) return new String[]{"Sortie", C_RED};
        if (l.contains("ordre")) return new String[]{"Ordre", C_BLUE};
        if (l.contains("rang") || l.contains("élève") || l.contains("abaisse") || l.contains("patriarche") || l.contains("raye") || l.contains("accueil")
                || l.contains("décret")) return new String[]{"Rang", C_GOLD};
        if (l.contains("vente") || l.contains("location") || l.contains("combat") || l.contains("blocs") || l.contains("marché")) return new String[]{"Domaine", C_VIOLET};
        return new String[]{"Note", C_GREY};
    }

    /** Livre de comptes ouvert (2026-11-18) : les 40 dernieres lignes en cartes, colorees par genre. */
    private View book() {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("nav:treasury", null, "«", "Fermer le livre", "Revenir au trésor de la maison.", null, C_VIOLET));
        List<FamilyRegistry.Line> lines = FamilyRegistry.journal(familyId);
        int in = 0, out = 0;
        for (int i = 0; i < lines.size(); i++) {
            FamilyRegistry.Line l = lines.get(i);
            String[] g = genre(l.text());
            if (g[0].equals("Entrée")) in++;
            if (g[0].equals("Sortie")) out++;
            cards.add(new Card("l:" + i, null, g[0].substring(0, 1), g[0], l.text(), FamilyRegistry.ago(l.at()), g[1]));
        }
        Detail intro = intro("Livre de comptes", lines.size() + " ligne(s) sur " + FamilyRegistry.JOURNAL,
                "Je consigne chaque opération de la maison : entrées (vert), sorties (rouge), rangs (or), ordres (bleu), domaines (violet).");
        return new View("Trésor  »  Livre de comptes", "Les plus récentes d'abord", "Pas une ligne encore.", cards,
                List.of(new Stat(String.valueOf(lines.size()), "Lignes"), new Stat(String.valueOf(in), "Entrées"), new Stat(String.valueOf(out), "Sorties"),
                        new Stat(lines.isEmpty() ? "—" : FamilyRegistry.ago(lines.get(0).at()), "Dernière")), intro);
    }

    private View tresor(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (cityId != null) return cityTresor(ref, store);
        if ("book".equals(drill)) return book();
        drill = null;
        FamilyRole r = role(ref, store);
        long treasury = FamilyManager.getRuntimeData(familyId).getTreasury();
        List<Card> cards = new ArrayList<>();
        List<FamilyRegistry.Line> book = FamilyRegistry.journal(familyId);
        cards.add(new Card("nav:book", item("Elda_Silver_Coins"), "L", "Ouvrir le livre de comptes", book.isEmpty() ? "Aucune opération consignée."
                : book.get(0).text() + "\n" + FamilyRegistry.ago(book.get(0).at()), book.size() + " lignes", C_GREY));
        cards.add(new Card("t:deposit", item("Elda_Gold_Coins"), "+", "Verser aux coffres", "De votre compte vers la trésorerie de la maison.",
                gold(treasury), C_GREEN));
        if (r == FamilyRole.PATRIARCH) {
            cards.add(new Card("t:withdraw", item("Elda_Diamond_Coins"), "-", "Puiser dans les coffres", "Réservé au Patriarche.", gold(treasury), C_GOLD));
            cards.add(new Card("t:allow", item("Elda_Zenith_Coins"), "A", "Allocation des Doyens et du Seigneur",
                    "Ce qu'ils peuvent dépenser chaque semaine sur les coffres, à la réserve.", gold(FamilyRegistry.allowance(familyId)), C_BLUE));
        }
        if (r != null && r.canInvite())
            for (String name : FamilleTab.invitableNames())
                cards.add(new Card("t:invite:" + name, famItem("Eldanior_Cle_Famille_"), "+", "Accueillir " + name,
                        "Noble sans maison, présent dans le royaume.", "Inviter", C_GREEN));
        if (r == FamilyRole.PATRIARCH)
            cards.add(new Card("t:disband", famItem("Eldanior_Porte_Famille_"), "X", "Dissoudre la maison",
                    "Congédier les membres, dissoudre les ordres, rendre les terres.", "Irréversible", C_RED));
        long left = allowanceLeft(ref, store);
        long tax = familyDomains(familyId).stream().mapToLong(ParcelData::getTradeTaxWeek).sum();
        Detail intro = intro("Trésor de la maison", gold(treasury) + " or dans les coffres",
                "Les coffres appartiennent à la maison : chacun peut y verser, seul le Patriarche y puise.");
        intro.note("La réserve se paie sur les coffres : le Patriarche sans limite, les Doyens et le Seigneur dans leur allocation de la semaine.");
        intro.note("La revente d'un domaine et la Taxe Impériale rejoignent les coffres.");
        if (r != null && r != FamilyRole.PATRIARCH && r.stewardOfTreasury()) intro.note("Votre allocation restante : " + gold(left) + " or cette semaine.");
        return new View("Trésor", "Je consigne tout au livre de comptes.", null, cards,
                List.of(new Stat(gold(treasury) + " or", "Coffres"), new Stat(gold(domainsTreasury()) + " or", "Trésor des domaines"),
                        new Stat(gold(tax) + " or", "Taxe du commerce (7 j)"),
                        r == FamilyRole.PATRIARCH ? new Stat(gold(FamilyRegistry.allowance(familyId)) + " or", "Allocation / semaine")
                                : new Stat(r != null && r.stewardOfTreasury() ? gold(left) + " or" : "—", "Votre allocation")), intro);
    }

    // ---- tresor d'une ville (registre du Comte, 2026-11-19 : « categorie tresor pour verser, retirer, vendre la ville, quitter la ville ») ----

    private View cityTresor(Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData c = city();
        List<Card> cards = new ArrayList<>();
        if (c == null) return new View("Trésor", null, "Cette ville n'existe plus.", cards, List.of(), intro("Trésor", "", ""));
        PlayerLevelData pd = data(ref, store);
        long purse = pd != null ? pd.getMoney() : 0;
        cards.add(new Card("c:deposit", item("Elda_Gold_Coins"), "+", "Verser au trésor", "De votre compte vers le trésor de " + c.getName() + ".",
                gold(purse) + " or", C_GREEN));
        cards.add(new Card("c:withdraw", item("Elda_Diamond_Coins"), "-", "Retirer du trésor", "Du trésor de la ville vers votre compte.",
                gold(c.getTreasury()) + " or", C_GOLD));
        boolean onSale = LandOffice.ownerSale(c);
        cards.add(new Card("c:sell", item("Eldanior_Affiche_Propriete"), "$", onSale ? "Retirer la ville de la vente" : "Vendre la ville",
                onSale ? "Elle est aux Terres à acquérir, chez l'intendant de sa région." : "La proposer aux Comtes du royaume, au prix choisi.",
                onSale ? gold(c.getPrice()) + " or" : "Mettre en vente", C_BLUE));
        cards.add(new Card("c:leave", item("Eldanior_Borne_Ville"), "X", "Quitter la ville", "Renoncer à la ville : elle n'a plus de maître.",
                "Irréversible", C_RED));
        Detail intro = intro("Trésor de " + c.getName(), gold(c.getTreasury()) + " or dans le trésor",
                "Le trésor reçoit la taxe du commerce, les loyers des biens sans maître et sa part des ventes. Le Bailli y prend ses gages.");
        intro.note("Verser et retirer : de votre compte au trésor, et inversement. Tout est noté au registre.");
        intro.note("Vendre : la ville rejoint les Terres à acquérir ; le prix vous est versé à la vente (même absent). Le trésor reste à la ville.");
        intro.note("Quitter : la ville n'a plus de maître, sa maison (ou la couronne) la remet en vente. Le trésor reste à la ville.");
        return new View("Trésor de " + c.getName(), "Le registre consigne chaque geste.", null, cards,
                List.of(new Stat(gold(c.getTreasury()) + " or", "Trésor"), new Stat(gold(purse) + " or", "Votre compte"),
                        new Stat(gold(c.getTradeTaxWeek()) + " or", "Taxe du commerce (7 j)"),
                        new Stat(onSale ? gold(c.getPrice()) + " or" : "non", "En vente")), intro);
    }

    private Detail cityTreasuryDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData c = city();
        if (c == null) return null;
        PlayerLevelData pd = data(ref, store);
        long purse = pd != null ? pd.getMoney() : 0;
        Detail d = new Detail();
        d.kicker = "Trésor de " + c.getName();
        switch (key) {
            case "c:deposit" -> {
                d.icon = item("Elda_Gold_Coins");
                d.title = "Verser au trésor";
                d.tagline = "Votre compte : " + gold(purse) + " or";
                d.text = "L'or versé appartient à la ville : il paie le Bailli et vous pourrez le reprendre.";
                d.n0 = "Montant à verser (or)";
                d.v0 = Math.min(1_000L, Math.max(1L, purse));
                d.btn("Verser", "cdeposit");
            }
            case "c:withdraw" -> {
                d.icon = item("Elda_Diamond_Coins");
                d.title = "Retirer du trésor";
                d.tagline = "Trésor : " + gold(c.getTreasury()) + " or";
                d.text = "L'or rejoint votre compte. Pensez aux gages du Bailli (" + gold(CityStewards.WAGE) + " or par semaine).";
                d.n0 = "Montant à retirer (or)";
                d.v0 = Math.max(1L, Math.min(1_000L, c.getTreasury()));
                d.btn("Retirer", "cwithdraw");
            }
            case "c:sell" -> {
                d.icon = item("Eldanior_Affiche_Propriete");
                if (LandOffice.ownerSale(c)) {
                    d.title = "Retirer de la vente";
                    d.tagline = "En vente : " + gold(c.getPrice()) + " or";
                    d.text = "La ville quitte les Terres à acquérir. Vous la gardez.";
                    d.btn("Retirer de la vente", "cunsell");
                } else {
                    d.title = "Vendre la ville";
                    d.tagline = "Prix conseillé : " + gold(LandOffice.price(c)) + " or";
                    d.text = "Un Comte (ou plus), chef de guilde ou Capitaine d'un Ordre pourra l'acheter chez un intendant. Le prix vous est versé ; "
                            + "le trésor, les biens et le Bailli restent à la ville.";
                    d.n0 = "Prix de vente (or)";
                    d.v0 = c.getPrice() > 0 ? c.getPrice() : LandOffice.price(c);
                    d.btn("Mettre en vente", "csell");
                }
            }
            case "c:leave" -> {
                d.icon = item("Eldanior_Borne_Ville");
                d.title = "Quitter la ville";
                d.tagline = "Vous renoncez à " + c.getName();
                d.text = "Vous n'en serez plus le maître : plus de registre, plus de revenus. Le trésor (" + gold(c.getTreasury())
                        + " or) reste à la ville. Retirez-le d'abord si vous le voulez.";
                d.btn(armed("cleave") ? "Je le veux : quitter " + c.getName() : "Quitter la ville", "cleave");
            }
            default -> { return null; }
        }
        return d;
    }

    private Detail treasuryDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (cityId != null) return cityTreasuryDetail(key, ref, store);
        long treasury = FamilyManager.getRuntimeData(familyId).getTreasury();
        PlayerLevelData pd = data(ref, store);
        long money = pd != null ? pd.getMoney() : 0;
        Detail d = new Detail();
        d.kicker = "Trésor de la maison";
        if (key.startsWith("l:")) {
            int i = parse(key.substring(2));
            List<FamilyRegistry.Line> lines = FamilyRegistry.journal(familyId);
            if (i < 0 || i >= lines.size()) return null;
            String[] g = genre(lines.get(i).text());
            d.mark = g[0].substring(0, 1);
            d.title = g[0];
            d.tagline = FamilyRegistry.ago(lines.get(i).at());
            d.text = lines.get(i).text() + ".";
            return d;
        }
        switch (key) {
            case "t:book" -> {
                d.icon = item("Elda_Silver_Coins");
                d.title = "Livre de comptes";
                d.tagline = "Les dernières opérations";
                StringBuilder sb = new StringBuilder();
                for (FamilyRegistry.Line l : FamilyRegistry.journal(familyId))
                    sb.append(FamilyRegistry.ago(l.at())).append(" — ").append(l.text()).append("\n");
                d.text = sb.length() == 0 ? "« Pas une ligne encore. »" : sb.toString().trim();
            }
            case "t:deposit" -> {
                d.icon = item("Elda_Gold_Coins");
                d.title = "Verser aux coffres";
                d.tagline = "Votre compte : " + gold(money) + " or";
                d.text = "« L'or versé appartient à la maison. Seul le Patriarche pourra le reprendre. »";
                d.n0 = "Montant à verser (or)";
                d.v0 = 100L;
                d.btn("Verser", "deposit");
            }
            case "t:withdraw" -> {
                d.icon = item("Elda_Diamond_Coins");
                d.title = "Puiser dans les coffres";
                d.tagline = "Coffres : " + gold(treasury) + " or";
                d.text = "« Combien, Patriarche ? Je l'inscrirai au livre de comptes. » L'or rejoint votre compte.";
                d.n0 = "Montant à retirer (or)";
                d.v0 = 100L;
                d.btn("Retirer", "withdraw");
            }
            case "t:allow" -> {
                d.icon = item("Elda_Zenith_Coins");
                d.title = "Allocation hebdomadaire";
                d.tagline = "Actuelle : " + gold(FamilyRegistry.allowance(familyId)) + " or par semaine";
                d.text = "Chaque Doyen et le Seigneur peuvent payer la réserve avec les coffres, dans cette limite, sur sept jours.";
                d.n0 = "Nouvelle allocation (or par semaine)";
                d.v0 = FamilyRegistry.allowance(familyId);
                d.btn("Fixer l'allocation", "allow");
            }
            case "t:disband" -> {
                d.icon = famItem("Eldanior_Porte_Famille_");
                d.title = "Dissoudre la maison";
                d.tagline = "Réservé au Patriarche";
                d.text = "« Tous les membres seront congédiés, les ordres dissous et les terres rendues à la couronne. Il n'y aura pas de retour. »";
                d.btn(armed("disband") ? "Je le veux : dissoudre" : "Dissoudre", "disband");
            }
            default -> {
                if (!key.startsWith("t:invite:")) return null;
                String name = key.substring("t:invite:".length());
                d.icon = famItem("Eldanior_Cle_Famille_");
                d.title = "Accueillir " + name;
                d.tagline = "Future recrue de la maison";
                d.text = "« " + name + " entrera dans la maison " + familyName() + " aussitôt, comme Recrue. Vous pourrez ensuite l'élever. »";
                d.btn("Accueillir", "invite");
            }
        }
        return d;
    }

    // ==================== DOMAINES ====================

    /**
     * Zones de la maison qui ont un tresor : royaumes, grands territoires, territoires, villes. Clan Luo : aussi celles des
     * quatre clans vassaux (en lecture ici, Taxe Imperiale dans Empire).
     */
    static List<ParcelData> familyDomains(String familyId) {
        List<ParcelData> out = new ArrayList<>();
        if (familyId == null) return out;
        java.util.Set<String> houses = new java.util.HashSet<>(List.of(familyId));
        if ("luo".equals(familyId)) for (var c : com.eldanior.system.luo.LuoClans.Clan.values()) houses.add(c.id);
        for (ParcelType t : new ParcelType[]{ParcelType.KINGDOM, ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY, ParcelType.CITY}) {
            List<ParcelData> of = new ArrayList<>();
            for (ParcelData p : ParcelManager.getByType(t))
                if (!p.getFamilyId().isEmpty() && houses.contains(FamilyManager.migrateId(p.getFamilyId()))) of.add(p);
            of.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
            out.addAll(of);
        }
        return out;
    }

    private List<ParcelData> ownDomains() {
        if (cityId != null) { ParcelData c = city(); return c != null ? List.of(c) : List.of(); }
        List<ParcelData> out = new ArrayList<>();
        for (ParcelData p : familyDomains(familyId)) if (!vassal(p)) out.add(p);
        return out;
    }

    private long domainsTreasury() { return ownDomains().stream().mapToLong(ParcelData::getTreasury).sum(); }

    private boolean vassal(ParcelData p) { return p != null && !familyId.equals(FamilyManager.migrateId(p.getFamilyId())); }

    /** Patriarche, Seigneur, ou maitre de la zone ; pas les terres d'une autre maison (clans vassaux, Marquisats). */
    private boolean canRule(ParcelData p, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID mine = me(ref, store);
        if (p != null && mine != null && p.isOwner(mine)) return true;
        if (p == null) return false;
        String h = IntendantLands.house(p);
        if (!h.isEmpty() && !h.equals(familyId)) return false;
        if (h.isEmpty() && !IntendantLands.inTutelle(p, IntendantLands.roots(familyId))) return false;
        FamilyRole r = role(ref, store);
        return r == FamilyRole.PATRIARCH || r == FamilyRole.VICE;
    }

    private static String announceLabel(String a) {
        return switch (a == null ? "" : a) {
            case "on" -> "toujours";
            case "off" -> "jamais";
            default -> "habituelle";
        };
    }

    /** Zone ouverte dans les Domaines (null : sommet de nos terres). */
    private ParcelData openLand(List<ParcelData> roots) {
        ParcelData at = drill != null && drill.startsWith("d:") ? ParcelManager.get(drill.substring(2)) : null;
        if (at != null && !IntendantLands.inTutelle(at, roots)) { drill = null; return null; }
        return at;
    }

    private static String plural(int n, String one, String many) { return n + " " + (n > 1 ? many : one); }

    /** Carte d'une terre sous tutelle : domaine (population, tresor) ou bien (etat, prix). */
    private Card landCard(ParcelData p, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID mine = me(ref, store);
        if (!IntendantLands.isDomain(p)) {
            String state = estateState(p);
            String who = p.getOwnerUUID() != null ? Pancartes.name(p.getOwnerUUID(), p.getOwnerName()) : "la maison";
            String value = p.isForSale() ? gold(p.getPrice()) + " or" : p.isForRent() || p.isRented() ? gold(p.getRentPrice()) + " / 7 j" : "—";
            return new Card("b:" + p.getId(), item("Eldanior_Affiche_Propriete"), state.substring(0, 1).toUpperCase(),
                    p.getName() != null ? p.getName() : p.getId(), p.getType().getLabel() + "\n" + state + "  ·  " + who, value, switch (state) {
                case "à vendre" -> C_GREEN;
                case "à louer" -> C_BLUE;
                case "libre" -> C_GREY;
                default -> C_ORANGE;
            });
        }
        IntendantLands.Pop pop = IntendantLands.population(p, store);
        boolean ours = familyId.equals(IntendantLands.house(p));
        int kids = IntendantLands.domainChildren(p).size();
        String meta = p.getType().getLabel() + (ours ? "" : "  ·  maison " + IntendantLands.houseName(p)) + "\n"
                + plural(pop.residents(), "habitant", "habitants") + "  ·  " + pop.npcs() + " PNJ  ·  " + plural(kids, "terre", "terres")
                + (LandOffice.onMarket(p) ? "  ·  à vendre" : "");
        String bar = !ours ? C_VIOLET : LandOffice.onMarket(p) ? C_ORANGE : pop.taxReady() > 0 ? C_GREEN : mine != null && p.isOwner(mine) ? C_GOLD : C_BLUE;
        String icon = p.getType() == ParcelType.CITY ? (ours ? famItem("Eldanior_Banniere_Famille_") : item("Eldanior_Affiche_Propriete"))
                : item("Eldanior_Borne_Territoire");
        return new Card("d:" + p.getId(), icon, p.getType().getLabel().substring(0, 1), p.getName(), meta, gold(p.getTreasury()) + " or", bar);
    }

    private static IntendantLands.Pop sum(List<IntendantLands.Pop> pops) {
        int a = 0, b = 0, c = 0, d = 0, e = 0, f = 0, g = 0, h = 0, i = 0, j = 0, k = 0;
        long t = 0, w = 0;
        java.util.Map<String, Integer> kinds = new java.util.LinkedHashMap<>();
        for (IntendantLands.Pop p : pops) {
            a += p.residents(); b += p.owners(); c += p.renters(); d += p.npcs(); e += p.present(); f += p.domains(); g += p.estates();
            h += p.forSale(); i += p.forRent(); j += p.occupied(); k += p.taxReady(); t += p.treasury(); w += p.tradeWeek();
            p.npcByKind().forEach((key, v) -> kinds.merge(key, v, Integer::sum));
        }
        return new IntendantLands.Pop(a, b, c, d, kinds, e, f, g, h, i, j, t, w, k);
    }

    /**
     * Domaines (3e passe, 2026-11-18) : descente par niveaux dans les terres sous tutelle (Royaume > Grand Territoire >
     * Territoire > Ville > biens). Les tuiles resument tout le niveau affiche ; le titre donne le chemin.
     */
    private View domaines(Ref<EntityStore> ref, Store<EntityStore> store) {
        if ("lands".equals(drill)) return landsView(ref, store);
        if (rulesOf != null) {
            ParcelData rp = ParcelManager.get(rulesOf);
            if (rp != null && insider(ref, store)) return rulesView(rp, ref, store);
            rulesOf = null;
        }
        List<Card> cards = new ArrayList<>();
        if (!insider(ref, store)) {
            if (comteOrMore(ref, store))
                cards.add(new Card("nav:lands", item("Eldanior_Borne_Territoire"), "+", "Terres à acquérir",
                        "Villes et territoires à vendre sous notre tutelle, selon votre titre (Comte : ville, Duc : territoire).", null, C_GOLD));
            Detail intro = intro("Terres de la maison", "Maison " + familyName(), "La maison cède parfois une ville ou un territoire.");
            return new View("Terres", null, null, cards, List.of(), intro);
        }
        List<ParcelData> roots = cityId != null ? ownDomains() : IntendantLands.roots(familyId);
        ParcelData at = openLand(roots);
        List<ParcelData> level = at == null ? roots : IntendantLands.domainChildren(at);   // pas les biens (onglet Biens)
        if (at != null) {
            List<ParcelData> path = IntendantLands.path(at, roots);
            ParcelData parent = path.size() > 1 ? path.get(path.size() - 2) : null;
            cards.add(new Card("nav:dup", null, "«", "Remonter", parent != null ? "Vers " + parent.getName() + " (" + parent.getType().getLabel() + ")"
                    : "Vers le sommet de nos terres.", null, C_VIOLET));
        } else if (cityId == null && comteOrMore(ref, store))
            cards.add(new Card("nav:lands", item("Eldanior_Borne_Territoire"), "+", "Terres à acquérir",
                    "Villes et territoires à vendre sous notre tutelle, selon votre titre (Comte : ville, Duc : territoire).", null, C_GOLD));
        for (ParcelData p : level) cards.add(landCard(p, ref, store));
        IntendantLands.Pop pop = at != null ? IntendantLands.population(at, store)
                : sum(roots.stream().map(r -> IntendantLands.population(r, store)).toList());
        StringBuilder trail = new StringBuilder("Domaines");
        if (at != null) for (ParcelData p : IntendantLands.path(at, roots)) trail.append("  »  ").append(p.getName());
        Detail intro;
        if (at == null) {
            intro = intro("Terres sous notre tutelle", plural(roots.size(), "domaine au sommet", "domaines au sommet"),
                    "Choisissez une terre pour voir sa population, son trésor et ses règles ; « Entrer » montre les terres qui en dépendent.");
            intro.note("Population : " + pop.residents() + " habitant(s) (" + pop.owners() + " propriétaire(s), " + pop.renters() + " locataire(s)), "
                    + pop.npcs() + " PNJ, " + pop.present() + " présent(s).");
            intro.note(plural(pop.domains(), "domaine", "domaines") + " et " + plural(pop.estates(), "bien", "biens") + " sous tutelle : "
                    + pop.forSale() + " à vendre, " + pop.forRent() + " à louer, " + pop.occupied() + " occupé(s).");
            intro.note("Lever l'impôt : le domaine prélève sa part du trésor de ses zones vassales (une fois par semaine).");
            intro.note("Rapatrier : la moitié du trésor du domaine rejoint les coffres (une fois par semaine).");
            intro.note("Une terre en violet appartient à une autre maison : on la voit, on ne la gouverne pas.");
        } else {
            intro = domainDetail("d:" + at.getId(), ref, store);
            if (intro == null) intro = intro(at.getName(), at.getType().getLabel(), "");
        }
        String section = trail.toString();
        return new View(section, at == null ? "Désignez une terre, puis « Entrer » pour descendre" : "« Remonter » revient au niveau du dessus",
                at == null ? "Aucune terre ne porte encore nos couleurs." : "Aucune terre sous " + at.getName() + " : ses biens sont dans l'onglet Biens.", cards,
                List.of(new Stat(gold(pop.total()), "Population"), new Stat(gold(pop.treasury()) + " or", "Trésors"),
                        new Stat(gold(pop.tradeWeek()) + " or", "Taxe du commerce (7 j)"), new Stat(String.valueOf(pop.taxReady()), "Impôts à lever")), intro);
    }

    // ---- terres a acquerir (2026-11-18 : chez l'intendant, plus chez le commercant immobilier) ----

    private List<ParcelData> landsForSale() {
        List<ParcelData> roots = IntendantLands.roots(familyId);                       // meme arbre que les Domaines (lien par maison)
        if (roots.isEmpty()) return List.of();
        List<ParcelData> out = new ArrayList<>();
        for (ParcelData p : LandOffice.lands(null, null)) if (IntendantLands.inTutelle(p, roots)) out.add(p);
        return out;
    }

    /** Terres a vendre sous la tutelle de la maison : la carte est verte si le titre de noblesse du joueur suffit. */
    private View landsView(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("nav:domains", null, "«", "Retour aux domaines", "Revenir à nos terres.", null, C_VIOLET));
        PlayerLevelData pd = data(ref, store);
        UUID mine = me(ref, store);
        int open = 0;
        List<ParcelData> lands = landsForSale();
        for (ParcelData p : lands) {
            String refused = LandOffice.refusal(p, pd, mine);
            if (refused == null) open++;
            var house = LandOffice.freeHouse(p);
            ParcelData parent = IntendantLands.parentOf(p);
            cards.add(new Card("ld:" + p.getId(), item("Eldanior_Borne_Territoire"), p.getType().getLabel().substring(0, 1), LandOffice.name(p),
                    (house != null ? "Duché de la maison " + house.getDisplayName() : p.getType().getLabel()) + (parent != null ? "  ·  " + LandOffice.name(parent) : "")
                            + "\nTitre requis : " + LandOffice.required(p).getDisplayName() + (LandOffice.onMarket(p) ? "  ·  vendue par sa maison" : ""),
                    gold(LandOffice.price(p)) + " or", refused == null ? C_GREEN : C_GREY));
        }
        NobilityRank rank = nobility(ref, store);
        Detail intro = intro("Terres à acquérir", "Votre titre : " + (rank != null ? rank.getDisplayName() : "aucun"),
                "Les terres à vendre sous la tutelle de la maison " + familyName() + ". En vert : celles que votre titre et votre compte permettent.");
        intro.note("Ville : Comte (ou plus), chef de guilde ou Grand Maître d'un ordre.");
        intro.note("Territoire : Duc (ou plus). Un Duché libre se prend sans maison : vous en devenez le Patriarche, et Doyen d'office du Marquisat.");
        intro.note("Le prix va au trésor de la terre qui la contient, ou aux coffres de la maison qui la vend.");
        return new View("Domaines  »  Terres à acquérir", "Payées avec vos pièces, puis votre compte", "Aucune terre à vendre sous notre tutelle.", cards,
                List.of(new Stat(String.valueOf(lands.size()), "Terres à vendre"), new Stat(String.valueOf(open), "À votre portée"),
                        new Stat(rank != null ? rank.getDisplayName() : "—", "Votre titre"),
                        new Stat(gold(pd != null ? pd.getMoney() : 0) + " or", "Votre compte")), intro);
    }

    private Detail landDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = parcel(key, "ld:");
        if (p == null) return null;
        var house = LandOffice.freeHouse(p);
        ParcelData parent = IntendantLands.parentOf(p);
        IntendantLands.Pop pop = IntendantLands.population(p, store);
        Detail d = new Detail();
        d.icon = item("Eldanior_Borne_Territoire");
        d.kicker = p.getType().getLabel() + (parent != null ? " de " + LandOffice.name(parent) : "");
        d.title = LandOffice.name(p);
        d.tagline = "Prix : " + gold(LandOffice.price(p)) + " or  ·  titre requis : " + LandOffice.required(p).getDisplayName();
        d.text = house != null ? "Duché de la maison " + house.getDisplayName() + ", sans Patriarche : l'acheteur en prend la tête."
                : LandOffice.onMarket(p) ? "Vendue par la maison " + IntendantLands.houseName(p) + " : l'acheteur en devient le maître."
                : "Terre sans maître : l'acheteur en devient le maître.";
        d.note("Population : " + pop.residents() + " habitant(s), " + pop.npcs() + " PNJ ; " + plural(pop.domains() - 1, "terre vassale", "terres vassales")
                + ", " + plural(pop.estates(), "bien", "biens") + ".");
        if (p.getType() == ParcelType.CITY) d.note(LandOffice.CITY_RULE);
        String refused = LandOffice.refusal(p, data(ref, store), me(ref, store));
        d.fact("Prix", gold(LandOffice.price(p)) + " or", C_GOLD);
        d.fact("Titre requis", LandOffice.required(p).getDisplayName());
        if (refused != null) {                                                       // 2026-11-19 : la raison bien visible
            d.fact("Achat", "impossible", C_RED);
            error(refused);
            return d;
        }
        d.fact("Achat", "possible", C_GREEN);
        return d.btn(armed("landbuy") ? "Confirmer l'achat" : "Acheter : " + gold(LandOffice.price(p)) + " or", "landbuy");
    }

    private ParcelData parcel(String key, String prefix) {
        return key != null && key.startsWith(prefix) ? ParcelManager.get(key.substring(prefix.length())) : null;
    }

    private Detail domainDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        if ("d+lands".equals(key)) {
            Detail d = intro("Terres à acquérir", "Bureau des terres de la maison", "Je vous ouvre le registre des terres à vendre dans nos domaines.");
            d.icon = item("Eldanior_Borne_Territoire");
            return d.btn("Ouvrir les terres", "lands");
        }
        if (key.startsWith("b:")) return estateDetail(key, ref, store);
        if (key.startsWith("ld:")) return landDetail(key, ref, store);
        if (key.startsWith("rl:")) return ruleDetail(key, ref, store);
        ParcelData p = parcel(key, "d:");
        if (p == null) return null;
        boolean rule = canRule(p, ref, store);
        ParcelData parent = IntendantLands.parentOf(p);
        double rate = TerritoiresTab.collectionRate(p);
        List<String> kids = p.getChildIds();
        IntendantLands.Pop pop = IntendantLands.population(p, store);
        int below = IntendantLands.domainChildren(p).size();
        boolean opened = drill != null && drill.equals("d:" + p.getId());
        Detail d = new Detail();
        d.icon = p.getType() == ParcelType.CITY ? famItem("Eldanior_Banniere_Famille_") : item("Eldanior_Borne_Territoire");
        d.kicker = p.getType().getLabel() + (parent != null ? " de " + parent.getName() : "");
        d.title = p.getName();
        d.tagline = "Trésor : " + gold(p.getTreasury()) + " or  ·  population : " + gold(pop.total());
        StringBuilder text = new StringBuilder("Maître : " + (p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "la maison")
                + "  ·  maison " + IntendantLands.houseName(p));
        if (!pop.npcByKind().isEmpty()) {
            text.append("\nPNJ : ");
            boolean first = true;
            for (var e : pop.npcByKind().entrySet()) { text.append(first ? "" : ", ").append(e.getValue()).append(" ").append(e.getKey().toLowerCase()); first = false; }
        }
        d.text = text.toString();
        d.fact("Trésor", gold(p.getTreasury()) + " or", C_GOLD);
        d.fact("Population", pop.residents() + " hab. · " + pop.npcs() + " PNJ");
        d.fact("Taxe (7 j)", gold(p.getTradeTaxWeek()) + " or");
        d.fact("Présents", String.valueOf(pop.present()), pop.present() > 0 ? C_GREEN : null);
        var border = com.eldanior.system.territory.laws.DomainLaws.border(p);
        d.fact("Couvre-feu", com.eldanior.system.territory.laws.DomainLaws.curfewLabel(p));
        d.fact("Frontières", border.label, border == com.eldanior.system.territory.laws.DomainLaws.Border.CLOSED ? C_RED
                : border == com.eldanior.system.territory.laws.DomainLaws.Border.TOLL ? C_ORANGE : null);
        if (com.eldanior.system.territory.laws.Invasions.running(p.getId())) d.fact("Alerte", "invasion en cours", C_RED);
        d.note("Population : " + pop.residents() + " habitant(s) (" + pop.owners() + " propriétaire(s), " + pop.renters() + " locataire(s)), "
                + pop.npcs() + " PNJ, " + pop.present() + " présent(s).");
        d.note(plural(pop.domains() - 1, "terre vassale", "terres vassales") + ", " + plural(pop.estates(), "bien", "biens") + " : "
                + pop.forSale() + " à vendre, " + pop.forRent() + " à louer, " + pop.occupied() + " occupé(s).");
        d.note("Taxe du commerce : " + gold(p.getTradeTaxWeek()) + " or cette semaine, " + gold(p.getTradeTaxTotal()) + " or en tout.");
        if (rate > 0 && !kids.isEmpty())
            d.note("Impôt : " + Math.round(rate * 100) + " % des " + kids.size() + " zones vassales, " + (p.canCollectTax() ? "prêt."
                    : "levé " + FamilyRegistry.ago(p.getLastTaxCollection()) + " (" + gold(p.getLastTaxAmount()) + " or)."));
        d.note("Rapatriement : " + (p.canTransferTreasury() ? "possible (" + gold(p.getTreasury() / 2) + " or)." : "déjà fait cette semaine.")
                + (LandOffice.onMarket(p) ? "  À vendre : " + gold(LandOffice.price(p)) + " or." : ""));
        if (!rule) d.note("Seuls le Patriarche, le Seigneur et le maître du domaine en ont la charge.");
        if (below > 0 && !opened) d.btn("Entrer : " + plural(below, "terre", "terres"), "denter");
        if (!rule) return d;
        if (rate > 0 && !kids.isEmpty() && p.canCollectTax()) d.btn("Lever l'impôt", "tax");
        boolean comteCity = cityId != null && p.getId().equals(cityId) && p.getGuildId().isEmpty() && p.getFamilyId().isEmpty();
        if (comteCity) {                                                                 // ville d'un Comte sans maison : revenu au Comte
            if (p.canTransferTreasury() && p.getTreasury() / 2 > 0) d.btn("Percevoir " + gold(p.getTreasury() / 2) + " or", "cityincome");
        } else if (p.getGuildId().isEmpty() && p.canTransferTreasury() && p.getTreasury() / 2 > 0) d.btn("Rapatrier " + gold(p.getTreasury() / 2) + " or", "repatriate");
        if (cityId != null && p.getId().equals(cityId)) {
            // 2026-11-19 (demande de l'utilisateur) : le Comte verse l'or de sa bourse au tresor de sa ville, depuis la borne
            PlayerLevelData me = data(ref, store);
            d.n1 = "Verser au trésor de la ville (or, de votre bourse : " + gold(me != null ? me.getMoney() : 0) + ")";
            d.v1 = 1_000L;
            d.btn("Verser au trésor", "cityfund");
            boolean has = CityStewards.hasBailli(p);
            d.note("Bailli : " + (has ? "en poste, " + gold(CityStewards.WAGE) + " or par semaine sur le trésor." : "aucun. Il tiendrait ce registre en ville ("
                    + gold(CityStewards.WAGE) + " or par semaine)."));
            d.btn(has ? "Renvoyer le Bailli" : "Engager un Bailli ici", has ? "bfire" : "bhire");
            boolean borne = CityBornes.placed(p);
            d.note("Borne de ville : " + (borne ? "posée." : "pas encore posée. Posez-la dans la ville : elle ouvre ce registre."));
            if (!borne) d.btn("Recevoir la borne de ville", "bgive");
        }
        d.btn("Règles du domaine", "ruleson");
        if (patriarch(ref, store) && LandOffice.sellable(p)) {
            if (LandOffice.onMarket(p)) d.btn("Retirer de la vente", "unsell");
            else {
                d.n0 = "Prix de vente (or), versé aux coffres";
                d.v0 = p.getPrice() > 0 ? p.getPrice() : LandOffice.price(p);
                d.btn("Mettre en vente", "sell");
            }
        }
        return d;
    }

    // ==================== BIENS ====================

    private static final List<ParcelType> ESTATE_TYPES = List.of(ParcelType.PLOT, ParcelType.HOUSING, ParcelType.ROOM, ParcelType.FARM,
            ParcelType.FOREST, ParcelType.MINE);

    private static boolean under(ParcelData p, String domainId) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String id = p.getParentId(); id != null && seen.add(id); ) {
            if (id.equals(domainId)) return true;
            ParcelData parent = ParcelManager.get(id);
            id = parent != null ? parent.getParentId() : null;
        }
        return false;
    }

    private List<ParcelData> estates() {
        List<ParcelData> out = new ArrayList<>();
        List<ParcelData> domains = ownDomains();
        for (ParcelData p : ParcelManager.getAll()) {
            if (!ESTATE_TYPES.contains(p.getType())) continue;
            for (ParcelData d : domains) if (under(p, d.getId())) { out.add(p); break; }
        }
        out.sort(java.util.Comparator.comparing((ParcelData p) -> p.getParentId() != null ? p.getParentId() : "")
                .thenComparing(p -> p.getName() != null ? p.getName() : p.getId(), String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private ParcelData domainOf(ParcelData e) {
        List<ParcelData> domains = ownDomains();
        for (String id = e.getParentId(); id != null; ) {
            for (ParcelData d : domains) if (d.getId().equals(id)) return d;
            ParcelData parent = ParcelManager.get(id);
            id = parent != null ? parent.getParentId() : null;
        }
        return null;
    }

    private boolean canManageEstate(ParcelData e, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID mine = me(ref, store);
        if (mine != null && e.isOwner(mine)) return true;
        if (e.getOwnerUUID() != null) return admin() && Pancartes.manages(e, playerRef);
        ParcelData d = domainOf(e);
        return d != null && canRule(d, ref, store);
    }

    private static String estateState(ParcelData e) {
        boolean rented = e.isRented() && e.getRenterUUID() != null;
        if (e.isForSale() && !rented) return "à vendre";
        if (e.isForRent() && !rented) return "à louer";
        if (rented) return "loué";
        return e.getOwnerUUID() != null ? "occupé" : "libre";
    }

    private View biens(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<ParcelData> estates = estates();
        List<Card> cards = new ArrayList<>();
        for (ParcelData e : estates) {
            ParcelData parent = e.getParentId() != null ? ParcelManager.get(e.getParentId()) : null;
            String state = estateState(e);
            String who = e.getOwnerUUID() != null ? Pancartes.name(e.getOwnerUUID(), e.getOwnerName()) : "la maison";
            String value = e.isForSale() ? gold(e.getPrice()) + " or" : e.isForRent() || e.isRented() ? gold(e.getRentPrice()) + " / 7 j" : "—";
            cards.add(new Card("b:" + e.getId(), item("Eldanior_Affiche_Propriete"), state.substring(0, 1).toUpperCase(),
                    e.getName() != null ? e.getName() : e.getId(), e.getType().getLabel() + (parent != null ? " · " + parent.getName() : "") + "\n"
                    + state + "  ·  " + who, value, switch (state) {
                case "à vendre" -> C_GREEN;
                case "à louer" -> C_BLUE;
                case "libre" -> C_GREY;
                default -> C_ORANGE;
            }));
        }
        Detail intro = intro("Biens de nos domaines", estates.size() + " bien(s)",
                "Les parcelles, maisons, chambres, fermes, forêts et mines de nos domaines.");
        intro.note("Ceux qui ont la charge du domaine mettent les biens de la maison en vente ou en location.");
        intro.note("Un bien acheté par un joueur n'appartient qu'à lui.");
        return new View("Biens de nos domaines", "L'affiche d'un bien suit son état", "Aucun bien dans les domaines de la maison.", cards,
                List.of(new Stat(String.valueOf(estates.size()), "Biens"),
                        new Stat(String.valueOf(estates.stream().filter(e -> Pancartes.stateOf(e).startsWith("Vendre")).count()), "À vendre"),
                        new Stat(String.valueOf(estates.stream().filter(e -> Pancartes.stateOf(e).endsWith("Louer")).count()), "À louer"),
                        new Stat(String.valueOf(estates.stream().filter(e -> "Occupe".equals(Pancartes.stateOf(e))).count()), "Occupés")), intro);
    }

    private Detail estateDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData e = parcel(key, "b:");
        if (e == null) return null;
        boolean manage = canManageEstate(e, ref, store);
        boolean rented = e.isRented() && e.getRenterUUID() != null;
        int w = e.getMaxX() - e.getMinX() + 1, l = e.getMaxZ() - e.getMinZ() + 1;
        Detail d = new Detail();
        d.icon = item("Eldanior_Affiche_Propriete");
        ParcelData dom = domainOf(e);
        d.kicker = "Bien de " + (dom != null ? dom.getName() : familyName());
        d.title = e.getName() != null ? e.getName() : e.getId();
        d.tagline = e.getType().getLabel() + "  ·  " + (e.isCylinder() ? "Ø " + w : w + " × " + l);
        d.text = "État : " + estateState(e);
        d.note("Maître : " + (e.getOwnerUUID() != null ? Pancartes.name(e.getOwnerUUID(), e.getOwnerName()) : "la maison"));
        if (rented) d.note("Locataire : " + Pancartes.name(e.getRenterUUID(), "un locataire") + ", encore " + Pancartes.remaining(e.getRentEndTime()));
        d.note("Prix : " + (e.getPrice() > 0 ? gold(e.getPrice()) + " or" : "—") + "  ·  loyer : " + (e.getRentPrice() > 0 ? gold(e.getRentPrice()) + " or / 7 j" : "—"));
        int chests = Pancartes.chests(e, store.getExternalData().getWorld());
        d.note("Coffres : " + (chests < 0 ? "zone trop grande pour les compter" : chests) + "  ·  blocs : " + (e.isProtectedByDefault() ? "protégés" : "ouverts"));
        if (!manage) {
            d.note(e.getOwnerUUID() != null ? "Ce bien appartient à un particulier." : "Seuls le Patriarche, le Seigneur et le maître du domaine en disposent.");
            return d;
        }
        d.n0 = "Prix ou loyer (or)";
        d.v0 = e.getPrice() > 0 ? e.getPrice() : e.getRentPrice();
        if (!rented) d.btn("Vendre à ce prix", "estsell").btn("Louer à ce prix (7 j)", "estrent");
        if (e.isForSale() || e.isForRent()) d.btn("Retirer du marché", "estwithdraw");
        return d;
    }

    // ==================== RESERVE ====================

    private List<ShopPage.Article> articles(Store<EntityStore> store) {
        String shop = FamilleTab.reserveShop(familyId);
        if (shop == null) return List.of();
        List<ShopPage.Article> out = new ArrayList<>(ShopPage.articles(shop, playerRef, store));
        out.sort((a, b) -> {
            int c = a.category().compareTo(b.category());
            return c != 0 ? c : Integer.compare(a.level(), b.level());
        });
        return out;
    }

    private View reserve(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<ShopPage.Article> articles = articles(store);
        Player player = store.getComponent(ref, Player.getComponentType());
        long wallet = player != null ? CoinWallet.value(player) : 0;
        List<Card> cards = new ArrayList<>();
        for (ShopPage.Article a : articles)
            cards.add(new Card("a:" + a.index(), a.itemId(), null, a.name(), a.category() + (a.quality().isEmpty() ? "" : "  ·  " + a.quality())
                    + (a.lot() > 1 ? "  ·  lot de " + a.lot() : "") + "\n" + (a.stock() > 0 ? a.stock() + " en réserve" : "Épuisé"),
                    a.stock() > 0 ? a.price() : "Épuisé", a.stock() <= 0 ? C_RED : a.unit() > wallet ? C_GREY : C_GREEN));
        if (FamilleTab.reserveShop(familyId) != null)
            cards.add(new Card("a+all", famItem("Eldanior_Presentoir_Famille_"), "+", "Voir toute la réserve", "La fenêtre de boutique de la réserve.", null, C_GOLD));
        FamilyRole r = role(ref, store);
        long left = allowanceLeft(ref, store);
        Detail intro = intro("Réserve de la maison", "Réassort tous les deux jours",
                "Bannières, présentoirs, portes et tenues de la maison " + familyName() + ". Les tenues de chef ne se vendent pas : "
                        + "le Patriarche reçoit la sienne à sa promotion.");
        if (r != null && r.stewardOfTreasury())
            intro.note(r == FamilyRole.PATRIARCH ? "Vous pouvez payer avec les coffres, sans limite."
                    : "Vous pouvez payer avec les coffres : encore " + gold(left) + " or cette semaine.");
        return new View("Réserve de la maison", "En gris : au-delà de votre bourse", "Les rayonnages sont vides.", cards,
                List.of(new Stat(String.valueOf(articles.size()), "Articles"),
                        new Stat(gold(articles.stream().mapToLong(ShopPage.Article::stock).sum()), "En réserve"),
                        new Stat(gold(FamilyManager.getRuntimeData(familyId).getTreasury()) + " or", "Coffres"),
                        new Stat(r == FamilyRole.PATRIARCH ? "Illimitée" : r != null && r.stewardOfTreasury() ? gold(left) + " or" : "—", "Votre allocation")), intro);
    }

    private ShopPage.Article article(String key, Store<EntityStore> store) {
        if (key == null || !key.startsWith("a:")) return null;
        int idx = Integer.parseInt(key.substring(2));
        for (ShopPage.Article a : articles(store)) if (a.index() == idx) return a;
        return null;
    }

    private Detail articleDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        if ("a+all".equals(key)) {
            Detail d = intro("Toute la réserve", "Fenêtre de boutique", "Payée avec votre bourse.");
            d.icon = famItem("Eldanior_Presentoir_Famille_");
            return d.btn("Ouvrir la réserve", "reserveall");
        }
        ShopPage.Article a = article(key, store);
        if (a == null) return null;
        Detail d = new Detail();
        d.icon = a.itemId();
        d.kicker = a.category() + (a.level() > 0 ? "  ·  niveau " + a.level() : "");
        d.title = a.name();
        d.tagline = a.quality().isEmpty() ? "Réserve de la maison" : a.quality();
        d.text = "Prix : " + a.price() + (a.lot() > 1 ? " le lot de " + a.lot() : "") + "\nEn réserve : " + (a.stock() > 0 ? a.stock() : "plus rien");
        if (a.stock() <= 0) return d;
        d.n0 = "Quantité";
        d.v0 = 1L;
        if (a.unit() > 0) d.note("Total : " + CoinWallet.format(a.unit() * Math.max(1, Math.min(n0, a.stock()))));
        d.btn("Acheter (bourse)", "buy");
        FamilyRole r = role(ref, store);
        if (r != null && r.stewardOfTreasury()) d.btn("Payer avec les coffres", "buytreasury");
        return d;
    }

    // ==================== QUETES ====================

    /**
     * Quetes de la maison (2026-11-18, 2e passe) : elles s'affichent au tableau des quetes, pas ici. L'intendant sert au
     * Patriarche a les publier (grille = cibles a choisir, fiche = formulaire), a les suivre, a recuperer les objets livres et a
     * les retirer.
     */
    private View quetes(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (draft) return draftView(ref, store);
        List<Card> cards = new ArrayList<>();
        boolean patriarch = patriarch(ref, store);
        if (patriarch) cards.add(new Card("q+new", item("Eldanior_Tome_01"), "+", "Publier une quête",
                "Une chasse ou une collecte, affichée au tableau des quêtes. Vous payez la récompense.", null, C_GOLD));
        List<HouseQuests.HouseQuest> qs = HouseQuests.of(familyId);
        for (HouseQuests.HouseQuest q : qs)
            cards.add(new Card("q:" + q.id, questIcon(q), q.kind == ChapterQuests.Kind.KILL ? "C" : "R", q.title,
                    (q.kind == ChapterQuests.Kind.KILL ? "Chasse : " + q.amount + " " + q.targetLabel.toLowerCase() : "Collecte : " + q.amount + " × " + q.targetLabel)
                            + "\n" + (q.places - q.remaining) + " / " + q.places + " rendue(s)  ·  " + (q.membersOnly ? "la maison" : "tous")
                            + (q.delivered > 0 ? "  ·  " + q.delivered + " objet(s) à récupérer" : ""),
                    gold(q.reward) + " or", q.remaining > 0 ? C_ORANGE : C_GREY));
        long locked = qs.stream().mapToLong(q -> q.reward * q.remaining).sum();
        Detail intro = intro("Quêtes de la maison", "Affichées au tableau des quêtes",
                "Le Patriarche publie des chasses et des collectes. Les joueurs les prennent au tableau des quêtes, comme les autres.");
        intro.note("La récompense de chaque place est retenue sur le compte du Patriarche à la publication.");
        intro.note("Les objets d'une collecte reviennent au Patriarche : il les récupère ici.");
        intro.note("Retirer une quête rend la récompense des places non prises.");
        return new View("Quêtes de la maison", patriarch ? "Désignez une quête pour la suivre ou la retirer" : null,
                "La maison n'a publié aucune quête.", cards,
                List.of(new Stat(String.valueOf(qs.size()), "Quêtes publiées"), new Stat(String.valueOf(qs.stream().mapToInt(q -> q.remaining).sum()), "Places libres"),
                        new Stat(gold(locked) + " or", "Récompenses consignées"),
                        new Stat(String.valueOf(qs.stream().mapToInt(q -> q.delivered).sum()), "Objets à récupérer")), intro);
    }

    private String questIcon(HouseQuests.HouseQuest q) {
        if (q.kind == ChapterQuests.Kind.ITEM) return q.target;
        for (int i = 0; i < HouseQuests.HUNTS.size(); i++) if (HouseQuests.HUNTS.get(i)[1].equals(q.target)) return item(HouseQuests.HUNTS.get(i)[2]);
        return null;
    }

    /** Cle de la cible choisie (carte surlignee en mode creation). */
    private String draftKey() {
        if (draftHunt) return draftHuntIndex >= 0 ? "qt:" + draftHuntIndex : "";
        return draftItem != null ? "qi:" + draftItem : "";
    }

    /** Objets differents du sac du Patriarche (collecte), avec leur nombre. */
    private java.util.LinkedHashMap<String, Integer> bagItems(Store<EntityStore> store, Ref<EntityStore> ref) {
        java.util.LinkedHashMap<String, Integer> out = new java.util.LinkedHashMap<>();
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return out;
        var bag = player.getInventory().getCombinedBackpackStorageHotbar();
        for (short s = 0; s < bag.getCapacity(); s++) {
            ItemStack st = bag.getItemStack(s);
            if (st == null || st.isEmpty() || st.getItemId() == null) continue;
            out.merge(st.getItemId(), st.getQuantity(), Integer::sum);
        }
        return out;
    }

    /** Grille du mode creation : les cibles (monstres ou objets du sac), et « Annuler ». */
    private View draftView(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("q-cancel", null, "X", "Annuler", "Revenir aux quêtes de la maison sans rien publier.", null, C_RED));
        cards.add(new Card("q-kind", null, draftHunt ? "R" : "C", draftHunt ? "Passer à une collecte" : "Passer à une chasse",
                draftHunt ? "Demander des objets : choisissez-les dans votre sac." : "Demander d'abattre des monstres.", null, C_VIOLET));
        if (draftHunt) {
            for (int i = 0; i < HouseQuests.HUNTS.size(); i++) {
                String[] h = HouseQuests.HUNTS.get(i);
                cards.add(new Card("qt:" + i, item(h[2]), h[0].substring(0, 1), h[0], "Chasse : abattre des " + h[0].toLowerCase() + ".",
                        null, i == draftHuntIndex ? C_GOLD : C_BLUE));
            }
        } else {
            for (var e : bagItems(store, ref).entrySet()) {
                Item it = Item.getAssetMap().getAsset(e.getKey());
                cards.add(new Card("qi:" + e.getKey(), e.getKey(), "?", itemName(it, e.getKey()), "Dans votre sac : " + e.getValue(),
                        null, e.getKey().equals(draftItem) ? C_GOLD : C_BLUE));
            }
        }
        long reward = Math.max(0, n1), places = Math.max(1, n2);
        PlayerLevelData pd = data(ref, store);
        long account = pd != null ? pd.getMoney() : 0;
        return new View(draftHunt ? "Nouvelle chasse : choisissez la cible" : "Nouvelle collecte : choisissez l'objet",
                "Puis réglez la quête à gauche", draftHunt ? null : "Votre sac est vide : mettez-y l'objet à demander.", cards,
                List.of(new Stat(gold(account) + " or", "Votre compte"), new Stat(gold(reward * places) + " or", "Coût total"),
                        new Stat(String.valueOf(places), "Places"),
                        new Stat(HouseQuests.rankOf(reward).getDisplayName(), "Rang de la quête")),
                null);
    }

    private Detail questDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (!key.startsWith("q:")) return null;
        HouseQuests.HouseQuest q = HouseQuests.get(key.substring(2));
        if (q == null) return null;
        Detail d = new Detail();
        d.icon = questIcon(q);
        d.mark = q.kind == ChapterQuests.Kind.KILL ? "C" : "R";
        d.kicker = "Quête de la maison  ·  rang " + HouseQuests.rankOf(q.reward).getDisplayName();
        d.title = q.title;
        d.tagline = gold(q.reward) + " or par joueur";
        d.text = (q.kind == ChapterQuests.Kind.KILL ? "Abattre " + q.amount + " " + q.targetLabel.toLowerCase() + "." : "Rapporter " + q.amount + " × " + q.targetLabel + ".")
                + (q.message.isEmpty() ? "" : "\n\n« " + q.message + " »");
        d.note("Publiée par " + q.creatorName + " " + FamilyRegistry.ago(q.createdAt) + ", au tableau des quêtes.");
        d.note("Places : " + q.remaining + " libre(s) sur " + q.places + "  ·  " + (q.places - q.remaining) + " rendue(s).");
        d.note(q.membersOnly ? "Réservée aux membres de la maison." : "Ouverte à tous les joueurs.");
        d.note("Récompense encore consignée : " + gold(q.reward * q.remaining) + " or.");
        UUID mine = me(ref, store);
        boolean owner = admin() || (mine != null && mine.equals(q.creator)) || patriarch(ref, store);
        if (owner && q.delivered > 0) d.btn("Récupérer " + q.delivered + " objet(s)", "qtake");
        if (owner) d.btn(armed("qwithdraw") ? "Confirmer : retirer" : "Retirer la quête", "qwithdraw");
        return d;
    }

    /** Formulaire de creation (fiche de gauche), relu a chaque changement de champ. */
    private Detail draftDetail(Ref<EntityStore> ref, Store<EntityStore> store) {
        Detail d = new Detail();
        d.kicker = "Nouvelle quête de la maison";
        String target = draftHunt ? (draftHuntIndex >= 0 ? HouseQuests.HUNTS.get(draftHuntIndex)[0] : null) : draftItem != null ? draftItemLabel : null;
        d.icon = draftHunt ? (draftHuntIndex >= 0 ? item(HouseQuests.HUNTS.get(draftHuntIndex)[2]) : null) : draftItem;
        d.mark = draftHunt ? "C" : "R";
        d.title = draftHunt ? "Chasse" : "Collecte";
        d.tagline = target != null ? (draftHunt ? "Cible : " + target : "Objet : " + target) : (draftHunt ? "Choisissez la cible à droite" : "Choisissez l'objet à droite");
        long qty = Math.max(1, n0), reward = Math.max(0, n1), places = Math.max(1, n2);
        PlayerLevelData pd = data(ref, store);
        long account = pd != null ? pd.getMoney() : 0, total = reward * places;
        d.text = "La quête s'affichera au tableau des quêtes. Chaque joueur qui la rend touche la récompense ; vous la payez maintenant "
                + "pour toutes les places.";
        d.note("Coût total : " + gold(total) + " or (" + places + " place(s) × " + gold(reward) + " or)" + (total > account ? " : votre compte ne suffit pas." : "."));
        d.note("Rang affiché : " + HouseQuests.rankOf(reward).getDisplayName() + " (selon la récompense).");
        d.note(draftMembers ? "Réservée aux membres de la maison." : "Ouverte à tous les joueurs.");
        d.n0 = draftHunt ? "Nombre de monstres" : "Nombre d'objets";
        d.n1 = "Récompense par joueur (or)";
        d.n2 = "Nombre de places (joueurs)";
        d.t0 = "Titre (facultatif)";
        d.t1 = "Message du Patriarche (facultatif)";
        d.v0 = qty;
        d.v1 = reward;
        d.v2 = places;
        d.s0 = t0;
        d.s1 = t1;
        d.btn("Publier  ·  " + gold(total) + " or", "qpublish");
        d.btn(draftMembers ? "Pour : la maison" : "Pour : tous", "qopen");
        d.btn("Annuler", "qcancel");
        return d;
    }

    // ==================== TITRES (2026-11-18) ====================

    /** Titres qu'un noble peut conferer, du plus haut au plus bas. */
    private static final NobilityRank[] DECREES = {NobilityRank.MARQUIS, NobilityRank.DUC, NobilityRank.COMTE, NobilityRank.BARON, NobilityRank.CHEVALIER};

    private static String decreeItem(NobilityRank r) {
        return switch (r) {
            case MARQUIS -> "Decret_Marquis";
            case DUC -> "Decret_Duc";
            case COMTE -> "Decret_Comte";
            case BARON -> "Decret_Baron";
            default -> "Decret_Chevalier";
        };
    }

    /**
     * Lettres de noblesse du joueur, chez n'importe quel intendant : quota de son rang actuel moins ce qu'il a deja confere depuis
     * toujours (NobilityManager.remaining) ; devenir Duc apres avoir ete Comte ne rend pas les Chevaliers deja nommes.
     */
    private View titres(Ref<EntityStore> ref, Store<EntityStore> store) {
        NobilityRank mine = nobility(ref, store);
        UUID me = me(ref, store);
        boolean luo = luoMarquis(ref, store);
        List<Card> cards = new ArrayList<>();
        int left = 0, given = 0;
        for (NobilityRank t : DECREES) {
            int max = NobilityManager.quota(mine, t, luo), done = NobilityManager.given(me, t);
            if (max <= 0 && done == 0) continue;
            int rest = NobilityManager.remaining(me, mine, t, luo);
            left += rest;
            given += done;
            cards.add(new Card("ti:" + t.name(), decreeItem(t), t.getDisplayName().substring(0, 1), "Décret de " + t.getDisplayName(),
                    "Votre rang : " + max + "  ·  déjà conféré(s) : " + done + "\n" + (rest > 0 ? "Remettez-le au joueur à nommer." : "Plus aucun à délivrer."),
                    rest + " restant(s)", rest > 0 ? C_GOLD : C_GREY));
        }
        Detail intro = intro("Lettres de noblesse", mine != null ? "Vous êtes " + mine.getDisplayName() : "Sans titre",
                "Chaque noble confère un nombre limité de titres selon son rang. Ce que vous avez déjà conféré reste compté : "
                        + "un Comte qui a nommé ses deux Chevaliers et devient Duc n'en nomme plus qu'un.");
        intro.note("Roi : 1 Marquis, 2 Ducs, 3 Comtes, 4 Barons, 10 Chevaliers.");
        intro.note("Marquis : 2 Comtes, 1 Baron, 4 Chevaliers.  Duc : 1 Baron, 3 Chevaliers.");
        intro.note("Comte : 2 Chevaliers." + (luo ? "  Marquisat Luo, en plus : 4 Ducs, 3 Comtes, 8 Barons, 20 Chevaliers." : ""));
        intro.note("Le décret va dans votre sac ; le joueur à nommer l'utilise pour prendre son rang.");
        return new View("Titres à conférer", "Le même registre chez tous les intendants", "Votre rang ne confère aucun titre.", cards,
                List.of(new Stat(mine != null ? mine.getDisplayName() : "—", "Votre rang"), new Stat(String.valueOf(left), "Titres restants"),
                        new Stat(String.valueOf(given), "Titres conférés"),
                        new Stat(String.valueOf(NobilityManager.grantHistory(me).size()), "Décrets scellés")), intro);
    }

    private Detail titleDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        NobilityRank t = key.startsWith("ti:") ? NobilityRank.fromString(key.substring(3)) : null;
        if (t == null) return null;
        NobilityRank mine = nobility(ref, store);
        UUID me = me(ref, store);
        boolean luo = luoMarquis(ref, store);
        int max = NobilityManager.quota(mine, t, luo), done = NobilityManager.given(me, t), rest = NobilityManager.remaining(me, mine, t, luo);
        Detail d = intro("Décret de " + t.getDisplayName(), rest + " décret(s) restant(s)",
                "Le décret va dans votre sac. Remettez-le au joueur à nommer : il l'utilise pour devenir " + t.getDisplayName() + ".");
        d.icon = decreeItem(t);
        d.kicker = "Lettres de noblesse";
        d.note("Votre rang (" + (mine != null ? mine.getDisplayName() : "aucun") + ")" + (luo ? " et le Marquisat Luo" : "") + " en confèrent " + max + ".");
        d.note("Déjà conféré(s), tous rangs confondus : " + done + ".");
        int shown = 0;
        for (long[] l : NobilityManager.grantHistory(me)) {
            if (l[1] != t.ordinal() || shown >= 3) continue;
            d.note("Scellé " + FamilyRegistry.ago(l[0]) + ".");
            shown++;
        }
        if (rest > 0) d.btn("Délivrer le décret", "grant");
        else d.note("Un rang plus élevé vous en donnera peut-être davantage.");
        return d;
    }

    // ==================== ROYAUME (Eldanior) ET EMPIRE (Clan Luo) ====================

    /** Royaume d'Eldanior : Marquisats et Taxe Royale ; Empire Luo : clans, Taxe Imperiale et Comptoir. */
    private View vassalView(Ref<EntityStore> ref, Store<EntityStore> store) {
        boolean empire = cat == Cat.EMPIRE;
        String tribute = IntendantLands.tributeName(familyId);
        List<Card> cards = new ArrayList<>();
        long due = 0;
        int people = 0;
        List<ParcelData> vassals = IntendantLands.vassals(familyId);
        for (ParcelData p : vassals) {
            long tax = (long) Math.floor(p.getTreasury() * IMPERIAL_RATE);
            if (p.canLevyImperialTax()) due += tax;
            IntendantLands.Pop pop = IntendantLands.population(p, store);
            people += pop.total();
            cards.add(new Card("e:" + p.getId(), item("Eldanior_Borne_Territoire"), "T", p.getName(), (empire ? "Clan " : "Maison ")
                    + IntendantLands.houseName(p) + "  ·  " + p.getType().getLabel() + "  ·  " + pop.residents() + " hab., " + pop.npcs() + " PNJ\n"
                    + (p.canLevyImperialTax() ? tribute + " due : " + gold(tax) + " or" : "Levée " + FamilyRegistry.ago(p.getLastImperialTax())),
                    gold(p.getTreasury()) + " or", p.canLevyImperialTax() && tax > 0 ? C_GREEN : C_GREY));
        }
        FamilyRole r = role(ref, store);
        String fam = com.eldanior.system.luo.LuoPlaques.FAMILY;
        var merchants = FamilyManager.getMerchants(fam);
        boolean comptoir = empire && (r == FamilyRole.PATRIARCH || r == FamilyRole.ELDER);
        if (comptoir) {
            for (var m : merchants.entrySet())
                cards.add(new Card("e+m:" + m.getKey(), item("Eldanior_Plaque_Luo_Marchand"), "M", m.getValue(), "Marchand agréé du Comptoir de la Porte du Dragon.",
                        online(m.getKey()) ? "En ligne" : "Absent", C_BLUE));
            int shown = 0;
            for (PlayerRef pr : Universe.get().getPlayers()) {
                if (shown >= 6) break;
                UUID u;
                try { u = UUIDExtractor.getUUID(pr); } catch (Exception e) { continue; }
                if (u == null || merchants.containsKey(u)) continue;
                var pref = pr.getReference();
                PlayerLevelData d = pref != null ? pref.getStore().getComponent(pref, EldaniorSystem.get().getPlayerLevelDataType()) : null;
                if (d != null && fam.equals(FamilyManager.migrateId(d.getNobleFamilyId()))) continue;
                cards.add(new Card("e+a:" + u, item("Eldanior_Plaque_Luo_Marchand"), "+", "Agréer " + pr.getUsername(), "Étranger présent : marchand du Comptoir.",
                        null, C_GOLD));
                shown++;
            }
        }
        Detail intro;
        if (empire) {
            intro = intro("L'Empire Luo", "Quatre clans vassaux", "Sama, Hanazakura, Kage et Setsuka doivent chaque semaine la Taxe Impériale : "
                    + Math.round(IMPERIAL_RATE * 100) + " % du trésor de chacun de leurs domaines, versés aux coffres du Clan Luo.");
            if (comptoir) intro.note("Le Patriarche et les Doyens agréent les marchands du Comptoir de la Porte du Dragon.");
        } else {
            String king = NobilityManager.getCurrentKingName();
            intro = intro("Le Royaume d'Eldanior", king != null && !king.isEmpty() ? "Règne de " + king : "Trône vacant",
                    "Les Marquisats du royaume doivent chaque semaine la Taxe Royale : " + Math.round(IMPERIAL_RATE * 100)
                            + " % de leur trésor, versés aux coffres de la maison royale.");
            intro.note("Les décrets du Roi se délivrent dans Titres, comme ceux des autres nobles.");
        }
        intro.note("Le Patriarche et le Seigneur lèvent la " + tribute + ".");
        return new View(empire ? "Empire" : "Royaume", tribute + " : une fois par semaine et par domaine", empire ? "Aucun domaine de clan ni marchand."
                : "Aucun Marquisat dans le royaume.", cards,
                List.of(new Stat(gold(due) + " or", tribute + " due"), new Stat(String.valueOf(vassals.size()), empire ? "Domaines des clans" : "Marquisats"),
                        new Stat(gold(people), "Population vassale"),
                        empire ? new Stat(String.valueOf(merchants.size()), "Marchands agréés")
                                : new Stat(gold(vassals.stream().mapToLong(ParcelData::getTreasury).sum()) + " or", "Trésors vassaux")), intro);
    }

    private Detail empireDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        FamilyRole r = role(ref, store);
        if (key.startsWith("e+m:")) {
            UUID u = UUID.fromString(key.substring(4));
            String name = FamilyManager.getMerchants(com.eldanior.system.luo.LuoPlaques.FAMILY).get(u);
            Detail d = intro(name != null ? name : "Marchand", "Marchand agréé du Comptoir", "Sa plaque de marchand lui ouvre le Comptoir de la Porte du Dragon.");
            d.icon = item("Eldanior_Plaque_Luo_Marchand");
            return d.btn(armed("revoke") ? "Confirmer : révoquer" : "Révoquer l'agrément", "revoke");
        }
        if (key.startsWith("e+a:")) {
            UUID u = UUID.fromString(key.substring(4));
            PlayerRef pr = Universe.get().getPlayer(u);
            Detail d = intro(pr != null ? pr.getUsername() : "Étranger", "Futur marchand du Comptoir",
                    "Il recevra la plaque de Marchand agréé et pourra entrer au Comptoir de la Porte du Dragon.");
            d.icon = item("Eldanior_Plaque_Luo_Marchand");
            return d.btn("Agréer", "agree");
        }
        ParcelData p = parcel(key, "e:");
        if (p == null) return null;
        String tribute = IntendantLands.tributeName(familyId);
        long tax = (long) Math.floor(p.getTreasury() * IMPERIAL_RATE);
        IntendantLands.Pop pop = IntendantLands.population(p, store);
        Detail d = intro(p.getName(), p.getType().getLabel() + " de la maison " + IntendantLands.houseName(p), "Trésor : " + gold(p.getTreasury()) + " or.");
        d.icon = item("Eldanior_Borne_Territoire");
        d.kicker = tribute;
        d.note(p.canLevyImperialTax() ? tribute + " due : " + gold(tax) + " or (" + Math.round(IMPERIAL_RATE * 100) + " %)."
                : "Levée " + FamilyRegistry.ago(p.getLastImperialTax()) + " : " + gold(p.getLastImperialAmount()) + " or.");
        d.note("Population : " + pop.residents() + " habitant(s), " + pop.npcs() + " PNJ, " + pop.present() + " présent(s).");
        d.note(pop.domains() - 1 + " terre(s) vassale(s), " + pop.estates() + " bien(s).");
        if ((r == FamilyRole.PATRIARCH || r == FamilyRole.VICE) && p.canLevyImperialTax() && tax > 0) d.btn("Lever la " + tribute, "imptax");
        return d;
    }

    // ==================== EVENEMENTS ====================

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Ev ev) {
        if (ev.action == null) return;
        boolean keep = false;
        try {
            switch (ev.action) {
                case "close" -> { close(); return; }
                case "cat" -> {
                    List<Cat> cs = cats(ref, store);
                    int i = parse(ev.index);
                    if (i < 0 || i >= cs.size() || cs.get(i) == cat) return;
                    cat = cs.get(i);
                    page = 0;
                    selected = null;
                    armed = null;
                    draft = false;
                    drill = null;
                    rulesOf = null;
                }
                case "card" -> {
                    int i = parse(ev.index);
                    int n = page * CARDS + i;
                    if (i < 0 || n >= lastCards.size()) return;
                    String key = lastCards.get(n).key();
                    armed = null;
                    if (draft) { draftCard(key, store); pushFields = true; break; }
                    if ("q+new".equals(key)) {                                     // mode creation : cibles a droite, formulaire a gauche
                        draft = true;
                        draftHunt = true;
                        draftHuntIndex = -1;
                        draftItem = null;
                        draftMembers = false;
                        n0 = 10; n1 = 1_000; n2 = 1; t0 = ""; t1 = "";
                        selected = null;
                        page = 0;
                        pushFields = true;
                        break;
                    }
                    if (key.startsWith("nav:")) { navigate(key); break; }
                    selected = key.equals(selected) && !key.equals(drill) ? null : key;
                    if (!key.startsWith("rl:")) rulesOf = null;                   // une carte de regle garde la grille des regles
                    pushFields = selected != null;
                }
                case "prev" -> page--;
                case "next" -> page++;
                case "n0" -> { n0 = ev.n0 != null ? Math.max(0, ev.n0) : 0; keep = true; }
                case "n1" -> { n1 = ev.n1 != null ? Math.max(0, ev.n1) : 0; keep = true; }
                case "n2" -> { n2 = ev.n2 != null ? Math.max(0, ev.n2) : 0; keep = true; }
                case "act" -> {
                    if (ev.n0 != null) n0 = Math.max(0, ev.n0);
                    if (ev.n1 != null) n1 = Math.max(0, ev.n1);
                    if (ev.n2 != null) n2 = Math.max(0, ev.n2);
                    if (ev.t0 != null) t0 = ev.t0.trim();
                    if (ev.t1 != null) t1 = ev.t1.trim();
                    Detail d = draft && cat == Cat.QUETES ? draftDetail(ref, store) : selected != null ? detail(selected, ref, store)
                            : rulesOf != null && ParcelManager.get(rulesOf) != null ? rulesView(ParcelManager.get(rulesOf), ref, store).intro() : null;
                    int i = parse(ev.index);
                    if (d == null || i < 0 || i >= d.btns.size()) return;
                    act(d.btns.get(i).key(), ref, store);
                    keep = true;
                }
                default -> { return; }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Intendant] " + ev.action, e);
            error("Une erreur est survenue.");
            keep = true;
        }
        if (replaced) { replaced = false; return; }                                   // une autre fenetre a pris la place
        if (!keep) status = null;
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui, ref, store);
        sendUpdate(ui);
    }

    private void act(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID mine = me(ref, store);
        FamilyRole r = role(ref, store);
        Player player = store.getComponent(ref, Player.getComponentType());
        switch (key) {
            // ---- membres ----
            case "claim" -> {
                if (!LuoMerchants.claimable(familyId, data(ref, store))) return;
                if (!arm("claim")) { error("Cliquez encore pour prendre la tête de la maison " + familyName() + "."); return; }
                String refused = FamilleTab.claim(familyId, ref, store);
                if (refused != null) { error(refused); return; }
                log( username() + " prend la tête de la maison");
                ok("Vous êtes le Patriarche de la maison " + familyName() + ".");
                selected = null;
            }
            case "up", "down" -> {
                FamilyRegistry.Entry e = entry(selected);
                if (e == null || r == null) return;
                report(FamilleTab.changeRoleOf(familyId, r, mine, e.uuid(), "up".equals(key)));
            }
            case "kick" -> {
                FamilyRegistry.Entry e = entry(selected);
                if (e == null || r != FamilyRole.PATRIARCH) return;
                if (!arm("kick")) { error("Cliquez encore pour rayer " + e.name() + " du registre."); return; }
                report(FamilleTab.kickMember(familyId, FamilyRole.PATRIARCH, mine, e.uuid(), e.name()));
                selected = null;
            }
            // ---- ordres ----
            case "found" -> {
                if (r == null || !r.knightly() || mine == null) { error("Il faut être Chevalier de la maison, au moins."); return; }
                String name = t0, motto = t1.isEmpty() ? "Pour la maison." : t1;
                if (name.length() < 3 || name.length() > 24) { error("Le nom doit faire de 3 à 24 caractères."); return; }
                if (KnightOrderManager.getPlayerOrder(mine) != null) { error("Vous servez déjà un ordre."); return; }
                if (r == FamilyRole.PATRIARCH) {
                    KnightOrder k = KnightOrderManager.createOrder(name, motto, familyId, mine, username());
                    if (k == null) { error("Impossible de fonder cet ordre."); return; }
                    log( "L'ordre " + k.getName() + " est levé, Grand Maître " + username());
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                    ok("L'Ordre " + k.getName() + " est fondé ; vous en êtes le Grand Maître.");
                } else {
                    KnightOrderManager.requestCreation(mine, username(), name, motto, familyId);
                    log( username() + " demande à lever l'ordre " + name);
                    ok("Requête transmise au Patriarche.");
                }
                selected = null;
            }
            case "approve", "reject" -> {
                if (r != FamilyRole.PATRIARCH || selected == null || !selected.startsWith("r:")) return;
                UUID req = UUID.fromString(selected.substring(2));
                if ("reject".equals(key)) {
                    KnightOrderManager.rejectRequest(req);
                    notify(req, "Votre requête pour lever un ordre a été refusée par le Patriarche.");
                    log( "Une requête d'ordre est refusée");
                    ok("Requête refusée.");
                } else {
                    var runtime = FamilyManager.getRuntimeData(familyId);
                    if (runtime.getTreasury() < KnightOrderManager.ORDER_PRICE) {
                        error("Il faut " + gold(KnightOrderManager.ORDER_PRICE) + " or dans les coffres pour lever un ordre.");
                        return;
                    }
                    if (KnightOrderManager.getPlayerOrder(req) != null) { error("Le demandeur sert déjà un ordre."); return; }
                    KnightOrder k = KnightOrderManager.approveRequest(mine, req);
                    if (k == null) { error("Impossible de fonder cet ordre (nom de 3 à 24 caractères)."); return; }
                    runtime.withdrawTreasury(KnightOrderManager.ORDER_PRICE);
                    KnightOrderManager.clearCandidacies(req);
                    notify(req, "Le Patriarche a approuvé l'Ordre " + k.getName() + " : vous en êtes le Grand Maître.");
                    log( "L'ordre " + k.getName() + " est levé (coffres : " + gold(KnightOrderManager.ORDER_PRICE) + " or)");
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                    ok("L'Ordre " + k.getName() + " est fondé.");
                }
                selected = null;
            }
            case "foundpick" -> {
                if (r != FamilyRole.PATRIARCH) return;
                String name = t0, motto = t1.isEmpty() ? "Pour la maison." : t1;
                if (name.length() < 3 || name.length() > 24) { error("Le nom doit faire de 3 à 24 caractères."); return; }
                if (KnightOrderManager.getOrdersForFamily(familyId).stream().anyMatch(o -> o.getName().equalsIgnoreCase(name))) {
                    error("La maison a déjà un ordre de ce nom.");
                    return;
                }
                pendingOrderName = name;
                pendingOrderMotto = motto;
                drill = "ofound";
                selected = null;
                page = 0;
            }
            case "foundgo" -> {
                if (r != FamilyRole.PATRIARCH || pendingOrderName == null || selected == null || !selected.startsWith("og:")) return;
                UUID gm = UUID.fromString(selected.substring(3));
                if (KnightOrderManager.getPlayerOrder(gm) != null) { error("Il sert déjà un ordre."); return; }
                var runtime = FamilyManager.getRuntimeData(familyId);
                if (runtime.getTreasury() < KnightOrderManager.ORDER_PRICE) {
                    error("Il faut " + gold(KnightOrderManager.ORDER_PRICE) + " or dans les coffres (il y a " + gold(runtime.getTreasury()) + ").");
                    return;
                }
                String gmName = gm.equals(mine) ? username() : nameOf(gm);
                KnightOrder k = KnightOrderManager.createOrder(pendingOrderName, pendingOrderMotto, familyId, gm, gmName != null ? gmName : "?");
                if (k == null) { error("Impossible de fonder cet ordre."); return; }
                runtime.withdrawTreasury(KnightOrderManager.ORDER_PRICE);
                KnightOrderManager.clearCandidacies(gm);
                log( username() + " lève l'ordre " + k.getName() + ", Grand Maître " + gmName + " (coffres : "
                        + gold(KnightOrderManager.ORDER_PRICE) + " or)");
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                if (!gm.equals(mine)) notify(gm, "Le Patriarche lève l'Ordre " + k.getName() + " : vous en êtes le Grand Maître.");
                ok("L'Ordre " + k.getName() + " est fondé ; Grand Maître : " + gmName + ".");
                pendingOrderName = null;
                drill = null;
                selected = "o:" + k.getId();
            }
            case "apply", "unapply" -> {
                KnightOrder k = selected != null && selected.startsWith("o:") ? KnightOrderManager.get(selected.substring(2)) : null;
                if (k == null || mine == null || !member(ref, store)) return;
                if ("unapply".equals(key)) {
                    k.removeCandidate(mine);
                    com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                    ok("Votre demande à l'Ordre " + k.getName() + " est retirée.");
                    return;
                }
                if (!k.isRecruiting()) { error("L'Ordre " + k.getName() + " ne recrute pas sur demande."); return; }
                if (KnightOrderManager.getPlayerOrder(mine) != null) { error("Vous servez déjà un ordre."); return; }
                k.addCandidate(mine, username());
                for (UUID u : k.getMembers()) {
                    var kr = k.getRoleOf(u);
                    if (kr != null && kr.officer()) notify(u, username() + " demande à entrer dans l'Ordre " + k.getName() + " (intendant de la maison).");
                }
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                ok("Demande transmise aux officiers de l'Ordre " + k.getName() + ".");
            }
            case "recruit" -> {
                KnightOrder k = selected != null && selected.startsWith("o:") ? KnightOrderManager.get(selected.substring(2)) : null;
                if (k == null) return;
                var actor = orderActor(k, ref, store);
                if (actor == null || actor.ordinal() > com.eldanior.system.titles.nobility.family.KnightOrderRole.VICE_MAITRE.ordinal()) return;
                k.setRecruiting(!k.isRecruiting());
                log( username() + (k.isRecruiting() ? " ouvre" : " ferme") + " le recrutement de l'ordre " + k.getName());
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                ok(k.isRecruiting() ? "L'Ordre " + k.getName() + " accepte les demandes." : "L'Ordre " + k.getName() + " devient un ordre d'élite : recrutement fermé.");
            }
            case "caccept", "crefuse" -> {
                KnightOrder k = openOrder();
                if (k == null || selected == null || !selected.startsWith("oc:")) return;
                var actor = orderActor(k, ref, store);
                if (actor == null || !actor.officer()) return;
                UUID u = UUID.fromString(selected.substring(3));
                String name = k.removeCandidate(u);
                if (name == null) return;
                if ("crefuse".equals(key)) {
                    notify(u, "L'Ordre " + k.getName() + " n'a pas retenu votre demande.");
                    ok("Demande de " + name + " refusée.");
                } else {
                    if (KnightOrderManager.getPlayerOrder(u) != null) { error(name + " sert déjà un ordre."); return; }
                    if (!KnightOrderManager.joinOrder(u, k)) { error("L'ordre est plein (" + KnightOrder.MAX_MEMBERS + " membres)."); return; }
                    k.rememberName(u, name);
                    KnightOrderManager.clearCandidacies(u);
                    notify(u, "Vous entrez dans l'Ordre " + k.getName() + " comme " + k.getRoleOf(u).title(familyId) + ".");
                    log( username() + " accepte " + name + " dans l'ordre " + k.getName());
                    ok(name + " entre dans l'Ordre " + k.getName() + ".");
                }
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                selected = null;
            }
            case "enrol" -> {
                FamilyRegistry.Entry e = entry(selected);
                KnightOrder k = mine != null ? KnightOrderManager.getPlayerOrder(mine) : null;
                if (e == null || k == null || !familyId.equals(FamilyManager.migrateId(k.getFamilyId()))) return;
                var kr = k.getRoleOf(mine);
                if (kr == null || !kr.officer()) { error("Seuls les officiers (Commandant ou plus) enrôlent."); return; }
                if (KnightOrderManager.getPlayerOrder(e.uuid()) != null) { error(e.name() + " sert déjà un ordre."); return; }
                if (!KnightOrderManager.joinOrder(e.uuid(), k)) { error("L'ordre est plein (" + KnightOrder.MAX_MEMBERS + " membres)."); return; }
                k.rememberName(e.uuid(), e.name());
                KnightOrderManager.clearCandidacies(e.uuid());
                notify(e.uuid(), username() + " vous enrôle dans l'Ordre " + k.getName() + ".");
                log( username() + " enrôle " + e.name() + " dans l'ordre " + k.getName());
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                ok(e.name() + " entre dans l'Ordre " + k.getName() + " comme " + k.getRoleOf(e.uuid()).title(familyId) + ".");
            }
            case "join" -> {
                KnightOrder k = KnightOrderManager.get(selected.substring(2));
                if (k == null || mine == null || !member(ref, store)) return;
                if (KnightOrderManager.getPlayerOrder(mine) != null) { error("Vous servez déjà un ordre."); return; }
                if (!KnightOrderManager.joinOrder(mine, k)) { error("L'ordre est plein (" + KnightOrder.MAX_MEMBERS + " membres)."); return; }
                k.rememberName(mine, username());
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                notify(k.getCapitaineUUID(), username() + " a prêté serment à votre Ordre !");
                log( username() + " prête serment à l'ordre " + k.getName());
                ok("Vous avez prêté serment à l'Ordre " + k.getName() + " : vous y entrez " + k.getRoleOf(mine).title(familyId) + ".");
            }
            case "leave" -> {
                if (mine == null || !KnightOrderManager.leaveOrder(mine)) { error("Impossible de quitter l'ordre."); return; }
                log( username() + " quitte son ordre");
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                ok("Vous avez quitté l'ordre.");
            }
            case "dissolve" -> {
                KnightOrder k = KnightOrderManager.get(selected.substring(2));
                if (k == null || !(r == FamilyRole.PATRIARCH || (mine != null && k.isCapitaine(mine)))) return;
                if (!arm("dissolve")) { error("Cliquez encore pour dissoudre l'Ordre " + k.getName() + "."); return; }
                for (UUID u : k.getMembers()) notify(u, "L'Ordre " + k.getName() + " a été dissous.");
                KnightOrderManager.disbandOrder(k.getId());
                log( "L'ordre " + k.getName() + " est dissous");
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                ok("L'Ordre " + k.getName() + " est dissous.");
                selected = null;
            }
            // ---- tresor ----
            case "deposit" -> report(FamilleTab.treasury(true, n0, familyId, r, ref, store));
            case "withdraw" -> report(FamilleTab.treasury(false, n0, familyId, r, ref, store));
            case "allow" -> {
                if (r != FamilyRole.PATRIARCH) return;
                FamilyRegistry.setAllowance(familyId, n0);
                log( username() + " fixe l'allocation des Doyens et du Seigneur à " + gold(n0) + " or par semaine");
                ok("Allocation fixée : " + gold(n0) + " or par semaine.");
            }
            case "invite" -> {
                if (r == null || selected == null) return;
                report(FamilleTab.inviteByName(familyId, r, selected.substring("t:invite:".length())));
                selected = null;
            }
            case "disband" -> {
                if (r != FamilyRole.PATRIARCH) { error("Seul le Patriarche peut dissoudre la maison."); return; }
                if (!arm("disband")) { error("Cliquez encore pour dissoudre la maison. Ce geste est irréversible."); return; }
                FamilleTab.disbandFamily(familyId);
                ok("La maison est dissoute.");
                selected = null;
            }
            // ---- domaines ----
            case "lands" -> {
                List<String> scope = new ArrayList<>();
                for (ParcelData d : ownDomains()) scope.add(d.getId());
                var w = store.getExternalData().getWorld();
                if (player != null) {
                    player.getPageManager().openCustomPage(ref, store, new LandOfficeScreen(playerRef, stewardName, familyName(),
                            w != null ? w.getName() : null, scope, LandOfficeScreen.TAB_LANDS));
                    replaced = true;
                }
            }
            case "tax", "repatriate", "pvp", "blocks", "announce", "sell", "unsell" -> domainAct(key, ref, store);
            case "landbuy" -> {
                ParcelData p = parcel(selected, "ld:");
                if (p == null || landsForSale().stream().noneMatch(l -> l.getId().equals(p.getId()))) { error("Cette terre n'est plus à vendre."); return; }
                if (!arm("landbuy")) { error("Cliquez encore pour acheter " + LandOffice.name(p) + " : " + gold(LandOffice.price(p)) + " or."); return; }
                var house = LandOffice.freeHouse(p);
                long price = LandOffice.price(p);
                String err = LandOffice.buy(ref, store, p);
                if (err != null) { error(err); return; }
                log( username() + " acquiert " + LandOffice.name(p) + " : " + gold(price) + " or");
                ok(house != null ? "Vous êtes le Patriarche de la maison " + house.getDisplayName() + " et le maître de " + LandOffice.name(p) + "."
                        : LandOffice.name(p) + " est à vous.");
                selected = null;
            }
            case "denter" -> {
                if (selected == null || !selected.startsWith("d:")) return;
                drill = selected;
                page = 0;
                rulesOf = null;
            }
            case "ruleson" -> {
                ParcelData p = parcel(selected, "d:");
                if (p == null) return;
                rulesOf = p.getId();
                selected = null;
                page = 0;
            }
            case "cityincome", "cityfund", "bhire", "bfire", "bgive", "cdeposit", "cwithdraw", "csell", "cunsell", "cleave" -> {
                cityAct(key, ref, store);
                pushFields = true;
            }
            case "curfew", "curfewoff", "arms", "lawfine", "border", "border:open", "border:toll", "border:closed", "lawtoll", "ban", "unban",
                 "tptoggle", "tpprice" -> {
                lawAct(key, ref, store);
                pushFields = true;
            }
            case "annon", "annoff", "anndef" -> domainAct(key, ref, store);
            case "rulesoff" -> {
                if (rulesOf != null) selected = "d:" + rulesOf;
                rulesOf = null;
                page = 0;
            }
            // ---- hierarchie des ordres ----
            case "omembers" -> {
                if (selected == null || !selected.startsWith("o:")) return;
                drill = selected;
                selected = null;
                page = 0;
            }
            case "orup", "ordown", "orkick" -> orderRankAct(key, ref, store, mine);
            // ---- biens ----
            case "estsell", "estrent", "estwithdraw" -> {
                ParcelData e = parcel(selected, "b:");
                if (e == null || !canManageEstate(e, ref, store)) return;
                boolean sale = "estsell".equals(key), off = "estwithdraw".equals(key);
                String err = Pancartes.saveSettings(e, off ? e.getPrice() : sale ? n0 : e.getPrice(), off ? e.getRentPrice() : sale ? e.getRentPrice() : n0,
                        !off && sale, !off && !sale, e.isProtectedByDefault());
                if (err != null) { error(err); return; }
                Pancartes.refreshPosters(e);
                log( username() + (off ? " retire " + e.getName() + " du marché" : (sale ? " met en vente " : " met en location ")
                        + e.getName() + " : " + gold(n0) + " or"));
                ok(off ? e.getName() + " n'est plus proposé." : e.getName() + (sale ? " est à vendre : " : " est à louer : ") + gold(n0) + " or.");
            }
            // ---- reserve ----
            case "reserveall" -> {
                String shop = FamilleTab.reserveShop(familyId);
                if (shop != null && player != null) {
                    player.getPageManager().openCustomPage(ref, store, ShopPage.reserve(playerRef, shop));
                    replaced = true;
                }
            }
            case "buy", "buytreasury" -> {
                ShopPage.Article a = article(selected, store);
                String shop = FamilleTab.reserveShop(familyId);
                if (a == null || shop == null) { error("La réserve est vide."); return; }
                int qty = (int) Math.max(1, Math.min(n0, 999));
                ShopPage.Purchase p;
                if ("buy".equals(key)) p = ShopPage.purchase(shop, a.index(), qty, ref, store);
                else {
                    if (r == null || !r.stewardOfTreasury()) return;
                    var runtime = FamilyManager.getRuntimeData(familyId);
                    long funds = Math.min(runtime.getTreasury(), allowanceLeft(ref, store));
                    final boolean limited = r != FamilyRole.PATRIARCH;
                    p = ShopPage.purchaseWith(shop, a.index(), qty, ref, store, funds, amount -> {
                        runtime.withdrawTreasury(amount);
                        if (limited) FamilyRegistry.addSpent(familyId, mine, amount);
                    });
                }
                if (!p.ok()) { error(p.message()); return; }
                log( username() + " prend " + qty + " × " + a.name() + " à la réserve"
                        + ("buytreasury".equals(key) ? " (coffres : " + gold(p.paid()) + " or)" : ""));
                ok(p.message());
            }
            // ---- quetes ----
            case "qopen" -> { draftMembers = !draftMembers; pushFields = true; }
            case "qcancel" -> { draft = false; pushFields = false; }
            case "qpublish" -> publish(ref, store, mine);
            case "qtake", "qwithdraw" -> questAct(key, ref, store, mine, player);
            // ---- titres ----
            case "grant" -> {
                NobilityRank t = selected != null && selected.startsWith("ti:") ? NobilityRank.fromString(selected.substring(3)) : null;
                NobilityRank mineRank = nobility(ref, store);
                if (t == null || player == null || mine == null) return;
                if (NobilityManager.remaining(mine, mineRank, t, luoMarquis(ref, store)) <= 0) { error("Plus aucun décret de " + t.getDisplayName() + " à délivrer."); return; }
                if (!player.getInventory().getCombinedBackpackStorageHotbar().addItemStack(new ItemStack(decreeItem(t), 1)).succeeded()) {
                    error("Votre sac est plein.");
                    return;
                }
                NobilityManager.recordGrant(mine, t);
                com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
                PlayerLevelData pd = data(ref, store);
                String house = pd != null ? FamilyManager.migrateId(pd.getNobleFamilyId()) : null;
                if (house != null && !house.isEmpty()) FamilyRegistry.log(house, username() + " scelle un décret de " + t.getDisplayName());
                ok("Décret de " + t.getDisplayName() + " dans votre sac : remettez-le au joueur à nommer.");
            }
            // ---- royaume et empire ----
            case "imptax" -> {
                ParcelData p = parcel(selected, "e:");
                if (p == null || !(r == FamilyRole.PATRIARCH || r == FamilyRole.VICE) || !p.canLevyImperialTax()) return;
                if (IntendantLands.vassals(familyId).stream().noneMatch(v -> v.getId().equals(p.getId()))) return;
                String tribute = IntendantLands.tributeName(familyId);
                long tax = (long) Math.floor(p.getTreasury() * IMPERIAL_RATE);
                if (tax <= 0) { error("Le trésor de " + p.getName() + " est vide."); return; }
                p.addTreasury(-tax);
                p.setImperialTax(System.currentTimeMillis(), tax);
                ParcelManager.save();
                FamilyManager.getRuntimeData(familyId).addTreasury(tax);
                log( username() + " lève la " + tribute + " de " + p.getName() + " : " + gold(tax) + " or");
                FamilyRegistry.log(IntendantLands.house(p), ("eldanior".equals(familyId) ? "La couronne" : "L'Empire") + " lève la " + tribute
                        + " de " + p.getName() + " : " + gold(tax) + " or");
                ok(tribute + " levée sur " + p.getName() + " : " + gold(tax) + " or aux coffres.");
            }
            case "revoke" -> {
                if (!(r == FamilyRole.PATRIARCH || r == FamilyRole.ELDER)) return;
                UUID u = UUID.fromString(selected.substring(4));
                String fam = com.eldanior.system.luo.LuoPlaques.FAMILY;
                String name = FamilyManager.getMerchants(fam).get(u);
                if (name == null) return;
                if (!arm("revoke")) { error("Cliquez encore pour révoquer " + name + "."); return; }
                FamilyManager.removeMerchant(fam, name);
                notify(u, "Le Clan Luo vous retire votre agrément de marchand du Comptoir.");
                log( username() + " révoque le marchand " + name);
                ok("Agrément de " + name + " retiré : sa plaque lui sera reprise.");
                selected = null;
            }
            case "agree" -> {
                if (!(r == FamilyRole.PATRIARCH || r == FamilyRole.ELDER)) return;
                UUID u = UUID.fromString(selected.substring(4));
                PlayerRef target = Universe.get().getPlayer(u);
                if (target == null) { error("Ce joueur n'est plus là."); return; }
                FamilyManager.addMerchant(com.eldanior.system.luo.LuoPlaques.FAMILY, u, target.getUsername());
                target.sendMessage(Message.raw("Le Clan Luo vous agrée comme marchand du Comptoir de la Porte du Dragon. Portez votre plaque pour y entrer."));
                log( username() + " agrée " + target.getUsername() + " au Comptoir");
                ok(target.getUsername() + " est marchand agréé du Comptoir.");
                selected = null;
            }
            default -> { }
        }
    }

    /** Cartes de navigation : remonter d'un niveau de domaine, ouvrir ou fermer le livre, revenir aux ordres. */
    private void navigate(String key) {
        page = 0;
        if ("nav:rulesoff".equals(key)) {
            if (rulesOf != null) selected = "d:" + rulesOf;
            rulesOf = null;
            return;
        }
        rulesOf = null;
        switch (key) {
            case "nav:book" -> { drill = "book"; selected = null; }
            case "nav:lands" -> { drill = "lands"; selected = null; }
            case "nav:domains" -> { drill = null; selected = null; }
            case "nav:treasury", "nav:orders" -> { drill = null; selected = null; pendingOrderName = null; }
            case "nav:dup" -> {
                List<ParcelData> roots = IntendantLands.roots(familyId);
                ParcelData at = openLand(roots);
                if (at == null) { drill = null; selected = null; return; }
                List<ParcelData> path = IntendantLands.path(at, roots);
                drill = path.size() > 1 ? "d:" + path.get(path.size() - 2).getId() : null;
                selected = "d:" + at.getId();                                       // la terre d'ou l'on vient reste designee
            }
            default -> { }
        }
    }

    /** Clic sur une carte du mode creation : annuler, changer de type, choisir la cible. */
    private void draftCard(String key, Store<EntityStore> store) {
        switch (key) {
            case "q-cancel" -> draft = false;
            case "q-kind" -> { draftHunt = !draftHunt; page = 0; }
            default -> {
                if (key.startsWith("qt:")) draftHuntIndex = parse(key.substring(3));
                else if (key.startsWith("qi:")) {
                    draftItem = key.substring(3);
                    draftItemLabel = itemName(Item.getAssetMap().getAsset(draftItem), draftItem);
                }
            }
        }
    }

    /** Elever, abaisser ou renvoyer un membre de l'ordre ouvert (hierarchie a sept rangs). */
    private void orderRankAct(String key, Ref<EntityStore> ref, Store<EntityStore> store, UUID mine) {
        KnightOrder k = openOrder();
        if (k == null || selected == null || !selected.startsWith("om:")) return;
        UUID u = UUID.fromString(selected.substring(3));
        var actor = orderActor(k, ref, store);
        var kr = k.getRoleOf(u);
        if (actor == null || kr == null || u.equals(mine)) return;
        String name = orderName(k, u);
        if ("orkick".equals(key)) {
            if (!actor.officer() || kr.ordinal() <= actor.ordinal()) { error("Vous ne renvoyez que ceux qui sont sous vous."); return; }
            if (!arm("orkick")) { error("Cliquez encore pour renvoyer " + name + " de l'ordre."); return; }
            KnightOrderManager.leaveOrder(u);
            notify(u, "Vous avez été renvoyé de l'Ordre " + k.getName() + ".");
            log( username() + " renvoie " + name + " de l'ordre " + k.getName());
            com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
            ok(name + " ne fait plus partie de l'Ordre " + k.getName() + ".");
            selected = null;
            return;
        }
        var to = "orup".equals(key) ? kr.up() : kr.down();
        FamilyRole fr = houseRole(u);
        String refused = to == null ? "Rang inconnu." : k.refuseRank(actor, u, to, "ordown".equals(key) || (fr != null && fr.knightly()));
        if (refused != null) { error(refused); return; }
        k.setRank(u, to);
        notify(u, "Ordre " + k.getName() + " : vous êtes désormais " + to.title(familyId) + ".");
        log( username() + ("orup".equals(key) ? " élève " : " abaisse ") + name + " au rang de " + to.title(familyId)
                + " dans l'ordre " + k.getName());
        com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
        ok(name + " est désormais " + to.title(familyId) + ".");
    }

    // ==================== LOIS DU DOMAINE (2026-11-18) ====================

    // ==================== REGLES DU DOMAINE (refaites le 2026-11-19 : une carte par regle) ====================

    /** Domaine dont on regle les lois : celui des regles ouvertes, sinon la fiche designee. */
    private ParcelData ruled() {
        return rulesOf != null ? ParcelManager.get(rulesOf) : parcel(selected, "d:");
    }

    private static String capFirst(String s) {
        return s == null || s.isEmpty() ? "" : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String borderText(DomainLaws.Border b) {
        return switch (b) {
            case OPEN -> "Tout le monde entre librement.";
            case TOLL -> "Un étranger paie le droit d'entrée (valable 2 jours) ; sans le sou, les gardes le chassent.";
            case CLOSED -> "Les gardes attaquent les étrangers qui ne font pas demi-tour.";
        };
    }

    /** Grille des regles : une carte par regle, l'etat actuel en valeur, le liseret dit si la regle est severe. */
    private View rulesView(ParcelData p, Ref<EntityStore> ref, Store<EntityStore> store) {
        boolean rule = canRule(p, ref, store);
        int[] cf = DomainLaws.curfew(p);
        boolean arms = DomainLaws.armsBanned(p);
        DomainLaws.Border border = DomainLaws.border(p);
        int bans = DomainLaws.banned(p).size();
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("nav:rulesoff", null, "«", "Retour à " + p.getName(), "Revenir à la fiche du domaine : trésor, impôt, population.", null, C_VIOLET));
        cards.add(new Card("rl:pvp", item("Weapon_Sword_Iron"), "C", "Combat entre joueurs",
                p.isPvpEnabled() ? "Les joueurs peuvent s'affronter sur ces terres." : "Aucun joueur ne peut en blesser un autre ici.",
                p.isPvpEnabled() ? "Permis" : "Interdit", p.isPvpEnabled() ? C_RED : C_GREEN));
        cards.add(new Card("rl:blocks", item("Rock_Stone_Brick"), "B", "Blocs",
                p.isProtectedByDefault() ? "Seuls les ayants droit construisent et cassent." : "Tout le monde peut construire et casser.",
                p.isProtectedByDefault() ? "Protégés" : "Ouverts", p.isProtectedByDefault() ? C_GREEN : C_ORANGE));
        cards.add(new Card("rl:announce", item("Furniture_Human_Ruins_Banner"), "A", "Annonce à l'entrée",
                "Le nom du domaine s'affiche à qui y entre.", capFirst(announceLabel(p.getAnnounce())), C_BLUE));
        cards.add(new Card("rl:curfew", item("Furniture_Human_Ruins_Lantern"), "N", "Couvre-feu",
                cf != null ? "Dehors à ces heures sans être chez soi : amende ; les boutiques ferment." : "On circule à toute heure.",
                cf != null ? cf[0] + " h – " + cf[1] + " h" : "Aucun", cf != null ? C_ORANGE : C_GREY));
        cards.add(new Card("rl:arms", item("Weapon_Shield_Iron"), "R", "Port des armes",
                arms ? "Une arme en main est punie d'amende (katana au fourreau permis)." : "Chacun garde son arme en main.",
                arms ? "Rengainées" : "Libres", arms ? C_ORANGE : C_GREY));
        cards.add(new Card("rl:fine", item("Elda_Gold_Coins"), "$", "Amende",
                "Couvre-feu et armes : versée au trésor du domaine, puis toutes les 5 minutes.", gold(DomainLaws.fine(p)) + " or", C_GOLD));
        cards.add(new Card("rl:border", item("Furniture_Village_Sign"), "F", "Frontières", borderText(border), capFirst(border.label),
                border == DomainLaws.Border.CLOSED ? C_RED : border == DomainLaws.Border.TOLL ? C_ORANGE : C_GREEN));
        cards.add(new Card("rl:toll", item("Ingredient_Bar_Gold"), "$", "Droit d'entrée",
                border == DomainLaws.Border.TOLL ? "Prélevé aux étrangers 10 s après leur entrée." : "Sans effet tant que les frontières ne sont pas au droit d'entrée.",
                gold(DomainLaws.toll(p)) + " or", border == DomainLaws.Border.TOLL ? C_GOLD : C_GREY));
        cards.add(new Card("rl:ban", item("Deco_Scroll"), "X", "Bannis",
                bans > 0 ? "Les gardes les attaquent à vue." : "Personne n'est banni de ces terres.", String.valueOf(bans), bans > 0 ? C_RED : C_GREY));
        if (p.getType() == ParcelType.GRAND_TERRITORY) {
            boolean on = ContinentalTeleporters.active(p);
            cards.add(new Card("rl:tp", item(com.eldanior.system.territory.teleport.ContinentalGates.ITEM), "P", "Portail Continental",
                    ContinentalTeleporters.spot(p) != null ? "Voyage vers les autres Marquisats, payé au trésor du Marquisat." : "Pas encore bâti (un admin le bâtit).",
                    on ? "Ouvert · " + gold(ContinentalTeleporters.price(p)) + " or" : "Fermé", on ? C_GREEN : C_GREY));
        }

        Detail intro = new Detail();
        intro.icon = item("Eldanior_Borne_Territoire");
        intro.kicker = "Règles du domaine";
        intro.title = p.getName();
        intro.tagline = rule ? "Choisissez une règle à droite pour la changer." : "Seuls le Patriarche, le Seigneur et le maître du domaine en décident.";
        intro.fact("Combat", p.isPvpEnabled() ? "permis" : "interdit", p.isPvpEnabled() ? C_RED : C_GREEN);
        intro.fact("Blocs", p.isProtectedByDefault() ? "protégés" : "ouverts");
        intro.fact("Couvre-feu", DomainLaws.curfewLabel(p), cf != null ? C_ORANGE : null);
        intro.fact("Armes", arms ? "rengainées" : "libres", arms ? C_ORANGE : null);
        intro.fact("Frontières", border.label, border == DomainLaws.Border.CLOSED ? C_RED : border == DomainLaws.Border.TOLL ? C_ORANGE : null);
        intro.fact("Bannis", String.valueOf(bans), bans > 0 ? C_RED : null);
        intro.fact("Amende", gold(DomainLaws.fine(p)) + " or", C_GOLD);
        intro.fact("Droit d'entrée", gold(DomainLaws.toll(p)) + " or");
        intro.text = "Chaque carte montre l'état actuel d'une règle. Liseré rouge : règle sévère pour les visiteurs ; vert : règle douce.";
        intro.btn("Retour au domaine", "rulesoff");
        return new View("Domaines  »  " + p.getName() + "  »  Règles", "Désignez une règle pour la changer", null, cards,
                List.of(new Stat(p.isPvpEnabled() ? "Permis" : "Interdit", "Combat"), new Stat(capFirst(border.label), "Frontières"),
                        new Stat(cf != null ? cf[0] + " h – " + cf[1] + " h" : "Aucun", "Couvre-feu"), new Stat(String.valueOf(bans), "Bannis")), intro);
    }

    /** Fiche d'une regle : etat, explication, champs utiles et gestes (le principal en premier, pleine largeur). */
    private Detail ruleDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = ruled();
        if (p == null) return null;
        boolean rule = canRule(p, ref, store);
        Detail d = new Detail();
        d.kicker = "Règle de " + p.getName();
        switch (key.substring(3)) {
            case "pvp" -> {
                boolean on = p.isPvpEnabled();
                d.icon = item("Weapon_Sword_Iron"); d.mark = "C";
                d.title = "Combat entre joueurs";
                d.tagline = on ? "Permis : les joueurs peuvent s'affronter." : "Interdit : personne ne peut blesser un autre joueur.";
                d.fact("État", on ? "permis" : "interdit", on ? C_RED : C_GREEN);
                d.fact("Domaine", p.getType().getLabel());
                if (rule) d.btn(on ? "Interdire le combat" : "Permettre le combat", "pvp");
            }
            case "blocks" -> {
                boolean on = p.isProtectedByDefault();
                d.icon = item("Rock_Stone_Brick"); d.mark = "B";
                d.title = "Blocs";
                d.tagline = on ? "Protégés : seuls les ayants droit construisent." : "Ouverts : tout le monde construit et casse.";
                d.fact("État", on ? "protégés" : "ouverts", on ? C_GREEN : C_ORANGE);
                d.fact("Domaine", p.getType().getLabel());
                if (rule) d.btn(on ? "Ouvrir les blocs à tous" : "Protéger les blocs", "blocks");
            }
            case "announce" -> {
                String a = p.getAnnounce() == null ? "" : p.getAnnounce();
                d.icon = item("Furniture_Human_Ruins_Banner"); d.mark = "A";
                d.title = "Annonce à l'entrée";
                d.tagline = "Actuellement : " + announceLabel(a) + ".";
                d.fact("État", announceLabel(a), C_BLUE);
                d.note("Toujours : le nom du domaine s'affiche à chaque entrée.");
                d.note("Jamais : on entre sans annonce.");
                d.note("Habituelle : le réglage par défaut des zones.");
                if (rule) {
                    if (!"on".equals(a)) d.btn("Toujours annoncer", "annon");
                    if (!"off".equals(a)) d.btn("Ne jamais annoncer", "annoff");
                    if (!a.isEmpty()) d.btn("Annonce habituelle", "anndef");
                }
            }
            case "curfew" -> {
                int[] c = DomainLaws.curfew(p);
                d.icon = item("Furniture_Human_Ruins_Lantern"); d.mark = "N";
                d.title = "Couvre-feu";
                d.tagline = c != null ? "De " + c[0] + " h à " + c[1] + " h (heures du jeu)." : "Aucun couvre-feu.";
                d.fact("Heures", DomainLaws.curfewLabel(p), c != null ? C_ORANGE : null);
                d.fact("Amende", gold(DomainLaws.fine(p)) + " or", C_GOLD);
                d.note("Qui est dehors sans être chez lui paie l'amende, 60 s après le début, puis toutes les 5 minutes.");
                d.note("Les boutiques ferment. Les Chevaliers et plus de la maison n'y sont pas soumis.");
                if (rule) {
                    d.n0 = "Début (heure, 0 à 23)";
                    d.v0 = (long) (c != null ? c[0] : 22);
                    d.n1 = "Fin (heure, 0 à 23)";
                    d.v1 = (long) (c != null ? c[1] : 6);
                    d.btn(c != null ? "Changer les heures" : "Décréter le couvre-feu", "curfew");
                    if (c != null) d.btn("Lever le couvre-feu", "curfewoff");
                }
            }
            case "arms" -> {
                boolean on = DomainLaws.armsBanned(p);
                d.icon = item("Weapon_Shield_Iron"); d.mark = "R";
                d.title = "Port des armes";
                d.tagline = on ? "Rengainées : une arme en main est punie d'amende." : "Libres : chacun garde son arme en main.";
                d.fact("État", on ? "rengainées" : "libres", on ? C_ORANGE : null);
                d.fact("Amende", gold(DomainLaws.fine(p)) + " or", C_GOLD);
                d.note("Un katana au fourreau reste permis.");
                if (rule) d.btn(on ? "Libérer le port des armes" : "Imposer les armes rengainées", "arms");
            }
            case "fine" -> {
                long fine = DomainLaws.fine(p);
                d.icon = item("Elda_Gold_Coins"); d.mark = "$";
                d.title = "Amende";
                d.tagline = gold(fine) + " or par infraction, versés au trésor de " + p.getName() + ".";
                d.fact("Montant", gold(fine) + " or", C_GOLD);
                d.fact("Couvre-feu", DomainLaws.curfewLabel(p));
                d.note("Prélevée sur les pièces, puis sur le compte. Impayable : les gardes s'en chargent.");
                if (rule) {
                    d.n2 = "Amende (or)";
                    d.v2 = fine;
                    d.btn("Fixer l'amende", "lawfine");
                }
            }
            case "border" -> {
                DomainLaws.Border b = DomainLaws.border(p);
                d.icon = item("Furniture_Village_Sign"); d.mark = "F";
                d.title = "Frontières";
                d.tagline = capFirst(b.label) + " : " + borderText(b);
                d.fact("État", b.label, b == DomainLaws.Border.CLOSED ? C_RED : b == DomainLaws.Border.TOLL ? C_ORANGE : C_GREEN);
                d.fact("Droit d'entrée", gold(DomainLaws.toll(p)) + " or");
                d.note("Exemptés : gens de la maison, maisons vassales et suzeraines, habitants et leur groupe.");
                if (rule) {
                    if (b != DomainLaws.Border.OPEN) d.btn("Ouvrir les frontières", "border:open");
                    if (b != DomainLaws.Border.TOLL) d.btn("Instaurer le droit d'entrée", "border:toll");
                    if (b != DomainLaws.Border.CLOSED) d.btn("Fermer les frontières", "border:closed");
                }
            }
            case "toll" -> {
                DomainLaws.Border b = DomainLaws.border(p);
                long toll = DomainLaws.toll(p);
                d.icon = item("Ingredient_Bar_Gold"); d.mark = "$";
                d.title = "Droit d'entrée";
                d.tagline = gold(toll) + " or, valable 2 jours, versés au trésor de " + p.getName() + ".";
                d.fact("Montant", gold(toll) + " or", C_GOLD);
                d.fact("Frontières", b.label, b == DomainLaws.Border.TOLL ? C_ORANGE : null);
                if (b != DomainLaws.Border.TOLL) d.note("Sans effet tant que les frontières ne sont pas au droit d'entrée.");
                if (rule) {
                    d.n2 = "Droit d'entrée (or)";
                    d.v2 = toll;
                    d.btn("Fixer le droit d'entrée", "lawtoll");
                    if (b != DomainLaws.Border.TOLL) d.btn("Instaurer le droit d'entrée", "border:toll");
                }
            }
            case "ban" -> {
                var banned = DomainLaws.banned(p);
                d.icon = item("Deco_Scroll"); d.mark = "X";
                d.title = "Bannis";
                d.tagline = banned.isEmpty() ? "Personne n'est banni de ces terres." : plural(banned.size(), "banni", "bannis") + " : les gardes les attaquent à vue.";
                d.fact("Bannis", String.valueOf(banned.size()), banned.isEmpty() ? null : C_RED);
                d.fact("Frontières", DomainLaws.border(p).label);
                List<String> names = new ArrayList<>(banned.values());
                names.sort(String.CASE_INSENSITIVE_ORDER);
                for (int i = 0; i < Math.min(NOTES - 1, names.size()); i++) d.note(names.get(i));
                if (names.size() > NOTES - 1) d.note("… et " + (names.size() - NOTES + 1) + " autre(s).");
                if (rule) {
                    d.t0 = "Nom du joueur";
                    d.s0 = "";
                    d.btn("Bannir (joueur connecté)", "ban");
                    if (!banned.isEmpty()) d.btn("Gracier", "unban");
                }
            }
            case "tp" -> {
                if (p.getType() != ParcelType.GRAND_TERRITORY) return null;
                var spot = ContinentalTeleporters.spot(p);
                boolean on = ContinentalTeleporters.active(p);
                long price = ContinentalTeleporters.price(p);
                d.icon = item(com.eldanior.system.territory.teleport.ContinentalGates.ITEM); d.mark = "P";
                d.title = "Portail Continental";
                d.tagline = on ? "Ouvert : " + gold(price) + " or le voyage, versés au trésor du Marquisat." : "Fermé.";
                d.fact("État", on ? "ouvert" : "fermé", on ? C_GREEN : null);
                d.fact("Prix", gold(price) + " or", C_GOLD);
                d.fact("Emplacement", spot != null ? spot.x() + ", " + spot.y() + ", " + spot.z() : "pas bâti");
                List<String> names = new ArrayList<>();
                for (ParcelData m : ContinentalTeleporters.destinations(p)) names.add(m.getName());
                d.note("Destinations ouvertes : " + (names.isEmpty() ? "aucune" : String.join(", ", names)) + ".");
                if (spot == null) d.note("Un admin bâtit le Portail Continental (un seul par Marquisat) ; vous l'ouvrez ici.");
                if (rule) {
                    d.n2 = "Prix du voyage (or)";
                    d.v2 = price;
                    d.btn(on ? "Fermer le portail" : "Ouvrir le portail", "tptoggle");
                    d.btn("Fixer le prix", "tpprice");
                }
            }
            default -> { return null; }
        }
        if (!rule) d.note("Seuls le Patriarche, le Seigneur et le maître du domaine en décident.");
        return d.btn("Retour au domaine", "rulesoff");
    }

    private void lawAct(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = ruled();
        if (p == null || !canRule(p, ref, store)) { error("Seuls le Patriarche, le Seigneur et le maître du domaine en décident."); return; }
        String who = username();
        switch (key) {
            case "curfew" -> {
                if (n0 > 23 || n1 > 23 || n0 == n1) { error("Heures de 0 à 23, début et fin différents."); return; }
                p.setLaw("curfew", n0 + "-" + n1);
                ParcelManager.save();
                log( who + " décrète le couvre-feu à " + p.getName() + " de " + n0 + " h à " + n1 + " h");
                announceLaw(p, "Par ordre de la maison, couvre-feu à " + p.getName() + " de " + n0 + " h à " + n1 + " h.");
                ok("Couvre-feu à " + p.getName() + " de " + n0 + " h à " + n1 + " h.");
            }
            case "curfewoff" -> {
                p.setLaw("curfew", "");
                ParcelManager.save();
                log( who + " lève le couvre-feu à " + p.getName());
                announceLaw(p, "Le couvre-feu est levé à " + p.getName() + ".");
                ok("Couvre-feu levé à " + p.getName() + ".");
            }
            case "arms" -> {
                boolean on = !com.eldanior.system.territory.laws.DomainLaws.armsBanned(p);
                p.setLaw("arms", on ? "1" : "");
                ParcelManager.save();
                log( who + (on ? " impose les armes rengainées à " : " libère le port des armes à ") + p.getName());
                ok(on ? "À " + p.getName() + ", les armes se portent désormais rengainées." : "Port des armes libre à " + p.getName() + ".");
            }
            case "lawfine" -> {
                if (n2 < 1 || n2 > 10_000_000) { error("Amende : de 1 à 10 000 000 or."); return; }
                p.setLaw("fine", String.valueOf(n2));
                ParcelManager.save();
                ok("Amende de " + p.getName() + " : " + gold(n2) + " or.");
            }
            case "border", "border:open", "border:toll", "border:closed" -> {
                var next = switch (key) {
                    case "border:open" -> DomainLaws.Border.OPEN;
                    case "border:toll" -> DomainLaws.Border.TOLL;
                    case "border:closed" -> DomainLaws.Border.CLOSED;
                    default -> DomainLaws.border(p).next();
                };
                p.setLaw("border", next.code);
                ParcelManager.save();
                log( who + " déclare les frontières de " + p.getName() + " " + next.label);
                announceLaw(p, "Les frontières de " + p.getName() + " sont désormais " + next.label + ".");
                ok("Frontières de " + p.getName() + " : " + next.label + ".");
            }
            case "lawtoll" -> {
                if (n2 < 1 || n2 > 10_000_000) { error("Droit d'entrée : de 1 à 10 000 000 or."); return; }
                p.setLaw("toll", String.valueOf(n2));
                ParcelManager.save();
                ok("Droit d'entrée de " + p.getName() + " : " + gold(n2) + " or.");
            }
            case "ban", "unban" -> {
                if (t0.isEmpty()) { error("Écrivez le nom du joueur."); return; }
                var list = com.eldanior.system.territory.laws.DomainLaws.banned(p);
                if ("unban".equals(key)) {
                    UUID found = null;
                    for (var e : list.entrySet()) if (e.getValue().equalsIgnoreCase(t0)) found = e.getKey();
                    if (found == null) { error(t0 + " n'est pas banni de " + p.getName() + "."); return; }
                    String name = list.remove(found);
                    com.eldanior.system.territory.laws.DomainLaws.setBanned(p, list);
                    ParcelManager.save();
                    log( who + " gracie " + name + " (" + p.getName() + ")");
                    ok(name + " peut de nouveau entrer à " + p.getName() + ".");
                    return;
                }
                PlayerRef target = null;
                for (PlayerRef pr : Universe.get().getPlayers()) if (pr.getUsername().equalsIgnoreCase(t0)) target = pr;
                if (target == null) { error("Joueur introuvable : il doit être connecté pour être banni."); return; }
                if (target.getUuid().equals(me(ref, store))) { error("Vous ne pouvez pas vous bannir vous-même."); return; }
                list.put(target.getUuid(), target.getUsername());
                com.eldanior.system.territory.laws.DomainLaws.setBanned(p, list);
                ParcelManager.save();
                log( who + " bannit " + target.getUsername() + " de " + p.getName());
                target.sendMessage(Message.raw("Vous êtes banni de " + p.getName() + " : les gardes vous attaqueront à vue.").color(C_RED));
                ok(target.getUsername() + " est banni de " + p.getName() + ".");
            }
            case "tptoggle" -> {
                if (p.getType() != ParcelType.GRAND_TERRITORY) return;
                boolean on = !com.eldanior.system.territory.teleport.ContinentalTeleporters.active(p);
                com.eldanior.system.territory.teleport.ContinentalTeleporters.setActive(p, on);
                log( who + (on ? " active" : " éteint") + " le Téléporteur Continental de " + p.getName());
                ok("Téléporteur Continental de " + p.getName() + " " + (on ? "actif." : "éteint."));
            }
            case "tpprice" -> {
                if (n2 > 10_000_000) { error("Prix : de 0 à 10 000 000 or."); return; }
                p.setLaw("tpPrice", String.valueOf(n2));
                ParcelManager.save();
                ok("Prix du voyage depuis " + p.getName() + " : " + gold(n2) + " or.");
            }
            default -> { }
        }
    }

    /** Annonce aux joueurs presents dans la zone (cri du heraut). */
    private static void announceLaw(ParcelData p, String text) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            var at = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at != null && p.contains(at.x, at.y, at.z)) pr.sendMessage(Message.raw("Le héraut : « " + text + " »").color("#e8c060"));
        }
    }

    private void cityAct(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData c = city();
        if (c == null || !canRule(c, ref, store)) return;
        switch (key) {
            case "cityincome" -> {
                if (!c.canTransferTreasury() || c.getTreasury() / 2 <= 0) { error("Le revenu de " + c.getName() + " a déjà été perçu cette semaine."); return; }
                long got = c.getTreasury() / 2;
                var type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData d = store.getComponent(ref, type);
                if (d == null) return;
                PlayerLevelData copy = (PlayerLevelData) d.clone();
                if (copy == null) return;
                copy.addMoney(got);
                store.putComponent(ref, type, copy);
                c.addTreasury(-got);
                c.setLastTreasuryTransfer(System.currentTimeMillis());
                ParcelManager.save();
                ok(gold(got) + " or du trésor de " + c.getName() + " versés sur votre compte.");
            }
            case "cityfund" -> {                                                // bourse du Comte -> tresor de la ville
                long amount = n1;
                if (amount <= 0) { error("Indiquez un montant à verser (1 or au moins)."); return; }
                var type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData d = store.getComponent(ref, type);
                if (d == null) return;
                if (d.getMoney() < amount) { error("Votre bourse ne contient que " + gold(d.getMoney()) + " or."); return; }
                PlayerLevelData copy = (PlayerLevelData) d.clone();
                if (copy == null) return;
                copy.removeMoney(amount);
                store.putComponent(ref, type, copy);
                c.addTreasury(amount);
                ParcelManager.save();
                log(username() + " verse " + gold(amount) + " or de sa bourse au trésor de " + c.getName());
                ok(gold(amount) + " or versés au trésor de " + c.getName() + " (il contient " + gold(c.getTreasury()) + " or).");
                pushFields = true;
            }
            case "bhire" -> {
                var t = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
                var w = store.getExternalData().getWorld();
                if (t == null || w == null) return;
                if (!c.contains(t.getPosition().x, t.getPosition().y, t.getPosition().z)) { error("Le Bailli prend son poste dans la ville : tenez-vous-y."); return; }
                String err = CityStewards.hire(c, w, new org.joml.Vector3d(t.getPosition()), t.getRotation().y());
                if (err != null) { error(err); return; }
                ok("Un Bailli prend son poste à vos côtés. Ses gages : " + gold(CityStewards.WAGE) + " or par semaine.");
            }
            case "bgive" -> {
                Player pl = store.getComponent(ref, Player.getComponentType());
                if (CityBornes.placed(c)) { error(c.getName() + " a déjà sa borne."); return; }
                if (CityBornes.carries(pl)) { error("Vous avez déjà la borne de ville dans votre sac."); return; }
                if (!CityBornes.give(pl)) { error("Votre sac est plein."); return; }
                ok("Voici la borne de " + c.getName() + " : posez-la dans la ville.");
            }
            case "bfire" -> {
                if (!arm("bfire")) { error("Cliquez encore pour renvoyer le Bailli."); return; }
                CityStewards.dismiss(c);
                ok("Le Bailli a rendu sa charge.");
            }
            case "cdeposit", "cwithdraw" -> {                                   // tresor de la ville <-> compte du Comte
                boolean in = "cdeposit".equals(key);
                long amount = n0;
                if (amount <= 0) { error("Indiquez un montant (1 or au moins)."); return; }
                var type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData d = store.getComponent(ref, type);
                if (d == null) return;
                if (in && d.getMoney() < amount) { error("Votre compte ne contient que " + gold(d.getMoney()) + " or."); return; }
                if (!in && c.getTreasury() < amount) { error("Le trésor ne contient que " + gold(c.getTreasury()) + " or."); return; }
                PlayerLevelData copy = (PlayerLevelData) d.clone();
                if (copy == null) return;
                if (in) copy.removeMoney(amount); else copy.addMoney(amount);
                store.putComponent(ref, type, copy);
                c.addTreasury(in ? amount : -amount);
                ParcelManager.save();
                log(username() + (in ? " verse " : " retire ") + gold(amount) + " or " + (in ? "au" : "du") + " trésor de " + c.getName());
                ok(gold(amount) + " or " + (in ? "versés au" : "retirés du") + " trésor de " + c.getName() + " (il contient " + gold(c.getTreasury()) + " or).");
            }
            case "csell" -> {
                if (n0 <= 0) { error("Indiquez un prix de vente."); return; }
                c.setPrice(n0);
                c.setForSale(true);
                c.setReserved(false);
                ParcelManager.save();
                log(username() + " met en vente la ville " + c.getName() + " : " + gold(n0) + " or");
                ok(c.getName() + " est à vendre : " + gold(n0) + " or, versés sur votre compte à la vente.");
            }
            case "cunsell" -> {
                c.setForSale(false);
                ParcelManager.save();
                log(username() + " retire la ville " + c.getName() + " de la vente");
                ok(c.getName() + " n'est plus à vendre.");
            }
            case "cleave" -> {
                if (!arm("cleave")) { error("Cliquez encore pour quitter " + c.getName() + "."); return; }
                String name = c.getName();
                c.clearOwnership();
                c.setForSale(false);
                c.setGuildId("");
                ParcelManager.save();
                log(username() + " quitte la ville " + name);
                ok("Vous avez quitté " + name + ". Elle n'a plus de maître.");
                close();
                replaced = true;
            }
            default -> { }
        }
    }

    private void domainAct(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        ParcelData p = ruled();
        if (p == null || !canRule(p, ref, store)) return;
        switch (key) {
            case "tax" -> {
                long got = TerritoiresTab.collectTax(p);
                if (got < 0) { error("L'impôt de " + p.getName() + " a déjà été levé cette semaine."); return; }
                log( username() + " lève l'impôt de " + p.getName() + " : " + gold(got) + " or");
                ok("Impôt levé sur les zones vassales de " + p.getName() + " : " + gold(got) + " or.");
            }
            case "repatriate" -> {
                long got = TerritoiresTab.transferTreasury(p);
                if (got <= 0) { error("Le trésor de " + p.getName() + " a déjà été rapatrié cette semaine."); return; }
                log( username() + " rapatrie " + gold(got) + " or de " + p.getName() + " (taxe du commerce de la semaine : "
                        + gold(p.getTradeTaxWeek()) + " or)");
                ok(gold(got) + " or de " + p.getName() + " rejoignent les coffres.");
            }
            case "pvp" -> {
                p.setPvpEnabled(!p.isPvpEnabled());
                ParcelManager.save();
                log( username() + (p.isPvpEnabled() ? " permet" : " interdit") + " le combat à " + p.getName());
                ok("Combat entre joueurs " + (p.isPvpEnabled() ? "permis" : "interdit") + " à " + p.getName() + ".");
            }
            case "blocks" -> {
                p.setProtectedByDefault(!p.isProtectedByDefault());
                ParcelManager.save();
                log( username() + (p.isProtectedByDefault() ? " protège" : " ouvre") + " les blocs de " + p.getName());
                ok("Blocs de " + p.getName() + " " + (p.isProtectedByDefault() ? "protégés." : "ouverts à tous."));
            }
            case "announce", "annon", "annoff", "anndef" -> {
                String next = switch (key) {
                    case "annon" -> "on";
                    case "annoff" -> "off";
                    case "anndef" -> "";
                    default -> switch (p.getAnnounce()) {
                        case "" -> "on";
                        case "on" -> "off";
                        default -> "";
                    };
                };
                p.setAnnounce(next);
                ParcelManager.save();
                ok("Annonce à l'entrée de " + p.getName() + " : " + announceLabel(next) + ".");
            }
            case "sell" -> {
                if (!patriarch(ref, store) || !LandOffice.sellable(p)) return;
                if (n0 <= 0) { error("Indiquez un prix de vente."); return; }
                p.setPrice(n0);
                p.setForSale(true);
                p.setReserved(false);
                ParcelManager.save();
                log( username() + " met en vente " + p.getName() + " : " + gold(n0) + " or");
                ok(p.getName() + " est dans les Terres à acquérir : " + gold(n0) + " or, versés aux coffres.");
            }
            case "unsell" -> {
                if (!patriarch(ref, store)) return;
                p.setForSale(false);
                p.setReserved(true);                                                  // une ville sans maitre quitte aussi le marche
                ParcelManager.save();
                log( username() + " retire " + p.getName() + " de la vente");
                ok(p.getName() + " n'est plus à vendre.");
            }
            default -> { }
        }
    }

    private void publish(Ref<EntityStore> ref, Store<EntityStore> store, UUID mine) {
        if (!patriarch(ref, store) || mine == null) { error("Seul le Patriarche publie une quête de la maison."); return; }
        if (draftHunt && draftHuntIndex < 0) { error("Choisissez la cible de la chasse dans la grille, à droite."); return; }
        if (!draftHunt && draftItem == null) { error("Choisissez l'objet demandé dans la grille, à droite."); return; }
        if (n0 < 1 || n0 > 9_999) { error("Nombre demandé : de 1 à 9 999."); return; }
        if (n1 < 1) { error("Indiquez une récompense par joueur (1 or au moins)."); return; }
        if (n2 < 1 || n2 > 50) { error("Nombre de places : de 1 à 50."); return; }
        if (t0.length() > 40) { error("Titre : 40 caractères au plus."); return; }
        if (t1.length() > 160) { error("Message : 160 caractères au plus."); return; }
        int qty = (int) n0, places = (int) n2;
        long reward = n1, total = reward * places;
        var type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData d = data(ref, store);
        if (d == null || d.getMoney() < total) {
            error("Il faut " + gold(total) + " or sur votre compte (vous avez " + gold(d != null ? d.getMoney() : 0) + ").");
            return;
        }
        PlayerLevelData copy = (PlayerLevelData) d.clone();
        if (copy == null) return;
        copy.removeMoney(total);
        store.putComponent(ref, type, copy);
        String label = draftHunt ? HouseQuests.HUNTS.get(draftHuntIndex)[0] : draftItemLabel;
        String target = draftHunt ? HouseQuests.HUNTS.get(draftHuntIndex)[1] : draftItem;
        String title = !t0.isEmpty() ? t0 : draftHunt ? "Chasse aux " + label.toLowerCase() : "Collecte : " + label;
        HouseQuests.create(familyId, mine, username(), title, draftHunt ? ChapterQuests.Kind.KILL : ChapterQuests.Kind.ITEM,
                target, label, qty, reward, places, draftMembers, t1);
        ok("« " + title + " » est affichée au tableau des quêtes : " + gold(total) + " or retenus sur votre compte.");
        draft = false;
        selected = null;
    }

    private void questAct(String key, Ref<EntityStore> ref, Store<EntityStore> store, UUID mine, Player player) {
        HouseQuests.HouseQuest q = HouseQuests.get(selected.substring(2));
        if (q == null || player == null) return;
        if (!(admin() || patriarch(ref, store) || (mine != null && mine.equals(q.creator)))) return;
        if ("qtake".equals(key)) {
            int n = HouseQuests.takeDelivered(q.id);
            Item it = Item.getAssetMap().getAsset(q.target);
            int max = Math.max(1, it != null ? it.getMaxStack() : 64), left = n;
            while (left > 0) {
                int s = Math.min(left, max);
                if (!player.getInventory().getCombinedBackpackStorageHotbar().addItemStack(new ItemStack(q.target, s)).succeeded()) break;
                left -= s;
            }
            if (left > 0) HouseQuests.restoreDelivered(q.id, left);
            ok((n - left) + " objet(s) récupéré(s)" + (left > 0 ? " ; sac plein, " + left + " restent chez moi." : "."));
            return;
        }
        if (!arm("qwithdraw")) { error("Cliquez encore pour retirer « " + q.title + " »."); return; }
        long refund = HouseQuests.withdraw(q.id);
        if (refund > 0) {                                                               // rendu au Patriarche qui l'a publiee
            var type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerRef creator = Universe.get().getPlayer(q.creator);
            if (creator != null && creator.getReference() != null) {
                var cref = creator.getReference();
                PlayerLevelData cd = cref.getStore().getComponent(cref, type);
                if (cd != null) {
                    PlayerLevelData copy = (PlayerLevelData) cd.clone();
                    copy.addMoney(refund);
                    cref.getStore().putComponent(cref, type, copy);
                }
            } else FamilyManager.getRuntimeData(familyId).addTreasury(refund);       // absent : aux coffres de la maison
        }
        if (q.delivered > 0) {
            Item it = Item.getAssetMap().getAsset(q.target);
            int max = Math.max(1, it != null ? it.getMaxStack() : 64), left = q.delivered;
            while (left > 0) {
                int s = Math.min(left, max);
                if (!player.getInventory().getCombinedBackpackStorageHotbar().addItemStack(new ItemStack(q.target, s)).succeeded()) break;
                left -= s;
            }
        }
        ok("Quête retirée" + (refund > 0 ? " : " + gold(refund) + " or rendus." : "."));
        selected = null;
    }

    /** Nom traduit dans la langue du joueur (repli : anglais, puis identifiant lisible), comme l'editeur des prix. */
    private String itemName(Item it, String fallback) {
        try {
            if (it != null && it.getTranslationKey() != null) {
                String key = it.getTranslationKey();
                var i18n = com.hypixel.hytale.server.core.modules.i18n.I18nModule.get();
                String lang = playerRef.getLanguage();
                String n = lang != null ? i18n.getMessage(lang, key) : null;
                if (n == null || n.isBlank() || n.equals(key)) n = i18n.getMessage("en-US", key);
                if (n != null && !n.isBlank() && !n.equals(key)) return n;
            }
        } catch (Exception ignored) { }
        return fallback.replace('_', ' ').replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    // ==================== OUTILS ====================

    private void report(FamilleTab.Outcome o) { if (o.ok()) ok(o.message()); else error(o.message()); }

    private void ok(String m) { status = m; statusColor = C_GREEN; }

    private void error(String m) { status = m; statusColor = C_RED; }

    private boolean arm(String key) {
        long now = System.currentTimeMillis();
        if (key.equals(armed) && now - armedAt <= ARM_MS) { armed = null; return true; }
        armed = key;
        armedAt = now;
        return false;
    }

    private boolean armed(String key) { return key.equals(armed) && System.currentTimeMillis() - armedAt <= ARM_MS; }

    private static void notify(UUID uuid, String text) {
        PlayerRef pr = uuid != null ? Universe.get().getPlayer(uuid) : null;
        if (pr != null) pr.sendMessage(Message.raw(text));
    }

    static String gold(long v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ');
    }

    private static int parse(String s) {
        try { return s != null ? Integer.parseInt(s) : -1; } catch (NumberFormatException e) { return -1; }
    }

    public static class Ev {
        public static final BuilderCodec<Ev> CODEC = BuilderCodec.builder(Ev.class, Ev::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("Index", Codec.STRING), (d, v) -> d.index = v, d -> d.index).add()
                .append(new KeyedCodec<>("@N0", Codec.INTEGER), (d, v) -> d.n0 = v, d -> d.n0).add()
                .append(new KeyedCodec<>("@N1", Codec.INTEGER), (d, v) -> d.n1 = v, d -> d.n1).add()
                .append(new KeyedCodec<>("@N2", Codec.INTEGER), (d, v) -> d.n2 = v, d -> d.n2).add()
                .append(new KeyedCodec<>("@T0", Codec.STRING), (d, v) -> d.t0 = v, d -> d.t0).add()
                .append(new KeyedCodec<>("@T1", Codec.STRING), (d, v) -> d.t1 = v, d -> d.t1).add()
                .build();
        public String action, index, t0, t1;
        public Integer n0, n1, n2;
    }
}
