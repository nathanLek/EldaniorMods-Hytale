package com.eldanior.system.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.gui.tabs.AdminTab;
import com.eldanior.system.gui.tabs.TerritoiresTab;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.hologram.HologramManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.titles.church.ChurchRank;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Fenetre d'administration (/es admin, admins seulement), refaite le 2026-11-19 a la demande de l'utilisateur : « le meme
 * design que la fenetre de l'intendant ». Elle reprend la coquille Intendant/Intendant.ui (habillage Chapitres) : categories
 * en haut a droite, replique, 4 tuiles, grille de 12 cartes cliquables, et a gauche la fiche de la carte choisie (faits,
 * texte, champs, gestes ; les gestes destructeurs en rouge, a confirmer d'un second clic dans les 6 s).
 *
 * Categories : Tableau (etat du serveur), Joueurs (profil, puis « Gerer » : une carte par geste), Monde (domaines et biens),
 * Economie (argent en circulation, guildes, familles), Hologrammes, Donnees (remises a zero).
 * Les gestes sur un joueur passent par AdminTab (memes regles que l'ancienne fenetre).
 */
public class AdminScreen extends InteractiveCustomUIPage<AdminScreen.Ev> {

    static final int CATS = 9, CARDS = 12, NOTES = 10, ACTIONS = 6, FACTS = 8;
    private static final String C_GREEN = "#00e7b6", C_RED = "#e0574a", C_GOLD = "#f2c45e", C_BLUE = "#6fa8e8", C_GREY = "#56677a",
            C_VIOLET = "#b47be8", C_ORANGE = "#e8a050";
    private static final long ARM_MS = 6_000, WEEK_MS = 7L * 24 * 3600 * 1000;
    private static final String[][] CLASSES = {{"warrior", "Guerrier"}, {"mage", "Mage"}, {"archer", "Archer"}, {"assassin", "Assassin"}, {"dragon", "Dragon"}};
    private static final String[] NOBILITY = {"CHEVALIER", "BARON", "COMTE", "DUC", "MARQUIS", "ROI"};
    private static final String[] CHURCH = {"PRETRE", "ARCHEVEQUE", "CARDINAL", "PAPE"};

    enum Cat {
        TABLEAU("Tableau", "Eldanior_Tableau_Classement"), JOUEURS("Joueurs", "Eldanior_Mannequin_Dojo"), MONDE("Monde", "Eldanior_Borne_Territoire"),
        ECONOMIE("Économie", "Elda_Gold_Coins"), HOLOGRAMMES("Hologrammes", "Deco_Scroll"), FAILLES("Failles", "Eldanior_Faille_Scellee_E"),
        COMPETENCES("Compétences", "skill_page_deep_slash"), DONNEES("Données", "Eldanior_Tome_01");

        final String label, icon;
        Cat(String label, String icon) { this.label = label; this.icon = icon; }
    }

    record Card(String key, String icon, String mark, String name, String meta, String value, String bar) {}
    record Stat(String value, String label) {}
    record Btn(String label, String key, boolean danger) {}

    static final class Detail {
        String icon, mark, kicker, title, tagline, text = "";
        final List<String> notes = new ArrayList<>();
        String n0, n1, n2, t0, t1;
        Long v0, v1, v2;
        String s0, s1;
        final List<Btn> btns = new ArrayList<>();
        final List<String[]> facts = new ArrayList<>();
        Detail fact(String k, String v) { return fact(k, v, null); }
        Detail fact(String k, String v, String color) { if (v != null && facts.size() < FACTS) facts.add(new String[]{k, v, color}); return this; }
        Detail btn(String label, String key) { if (btns.size() < ACTIONS) btns.add(new Btn(label, key, false)); return this; }
        Detail danger(String label, String key) { if (btns.size() < ACTIONS) btns.add(new Btn(label, key, true)); return this; }
        Detail note(String n) { if (n != null && notes.size() < NOTES) notes.add(n); return this; }
    }

    record View(String section, String footer, String empty, List<Card> cards, List<Stat> stats, Detail intro) {}

    private Cat cat = Cat.TABLEAU;
    private int page;
    private String selected, status, statusColor = C_GREEN, armed;
    /** Un geste a ferme l'ecran (premiere connexion sur soi-meme) : ne plus lui envoyer de mise a jour. */
    private boolean closedByAct;
    private long armedAt;
    private boolean pushFields;
    private long n0, n1, n2;
    private String t0 = "", t1 = "";
    /** Vue de la categorie Competences (passifs, metiers, effets de statut : AdminCompetences). */
    private AdminCompetences.Mode skMode = AdminCompetences.Mode.PASSIFS;
    private List<Card> lastCards = List.of();
    /** Joueur gere (Joueurs : la grille montre ses gestes) ; null : liste des joueurs. */
    private String managed;
    /** Monde : biens (parcelles, logements, fermes...) au lieu des domaines. */
    private boolean estates;
    /** Fiche d'une zone : gestes avances (tresor, famille, impot, proprietaire). */
    private boolean zoneMore;

    public AdminScreen(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Ev.CODEC);
    }

    // ==================== OUVERTURE ====================

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Intendant/Intendant.ui");
        if (!admin(ref, store)) return;
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
        fill(ui, ref, store);
    }

    private static boolean admin(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        return pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION);
    }

    private static UUID adminUuid(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null) return new UUID(0, 0);
        try { return UUIDExtractor.getUUID(pr); } catch (Exception e) { return new UUID(0, 0); }
    }

    // ==================== AFFICHAGE ====================

    private void fill(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        Cat[] cs = Cat.values();
        for (int i = 0; i < CATS; i++) {
            boolean shown = i < cs.length;
            ui.set("#IntCat" + i + ".Visible", shown);
            if (!shown) continue;
            boolean on = cs[i] == cat;
            String icon = item(cs[i].icon);
            if (icon != null) ui.set("#IntCatIcon" + i + ".ItemId", icon);
            ui.set("#IntCatLabel" + i + ".Text", cs[i].label);
            ui.set("#IntCatLabel" + i + ".Style.TextColor", on ? C_GOLD : "#96a9be");
            ui.set("#IntCatGlow" + i + ".Visible", on);
            ui.set("#IntCatOn" + i + ".Visible", on);
        }

        View v = switch (cat) {
            case TABLEAU -> tableau(ref, store);
            case JOUEURS -> managed != null ? manageView(managed) : joueurs();
            case MONDE -> monde();
            case ECONOMIE -> economie();
            case HOLOGRAMMES -> hologrammes();
            case FAILLES -> failles();
            case COMPETENCES -> AdminCompetences.view(skMode);
            case DONNEES -> donnees();
        };
        lastCards = v.cards();

        ui.set("#IntQuote.Text", "« " + quote() + " »");
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
            ui.set("#IntCardSel" + i + ".Visible", c.key().equals(selected));
        }
        ui.set("#IntEmpty.Visible", v.cards().isEmpty());
        ui.set("#IntEmpty.Text", v.empty() != null ? v.empty() : "");
        ui.set("#IntPager.Visible", pages > 1);
        ui.set("#IntPageLabel.Text", (page + 1) + " / " + pages);

        Detail d = null;
        if (selected != null && v.cards().stream().anyMatch(c -> c.key().equals(selected))) {
            try { d = detail(selected, ref, store); } catch (Exception e) { EldaniorLogger.error("[Admin] fiche " + selected, e); }
        } else selected = null;
        if (d == null) d = v.intro();
        showDetail(ui, d);

        PlayerRef me = store.getComponent(ref, PlayerRef.getComponentType());
        ui.set("#IntPurse.Text", "Administration  ·  " + (me != null ? me.getUsername() : "?") + "   ·   "
                + plural(Universe.get().getPlayers().size(), "joueur connecté", "joueurs connectés") + "   ·   "
                + plural(ParcelManager.getAll().size(), "zone", "zones"));
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
        if (pushFields) {
            if (d.v0 != null) { ui.set("#IntInput0.Value", (int) Math.min(Integer.MAX_VALUE, d.v0)); n0 = d.v0; }
            if (d.v1 != null) { ui.set("#IntInput1.Value", (int) Math.min(Integer.MAX_VALUE, d.v1)); n1 = d.v1; }
            if (d.v2 != null) { ui.set("#IntInput4.Value", (int) Math.min(Integer.MAX_VALUE, d.v2)); n2 = d.v2; }
            if (d.t0 != null) { ui.set("#IntInput2.Value", d.s0 != null ? d.s0 : ""); t0 = d.s0 != null ? d.s0 : ""; }
            if (d.t1 != null) { ui.set("#IntInput3.Value", d.s1 != null ? d.s1 : ""); t1 = d.s1 != null ? d.s1 : ""; }
            pushFields = false;
        }
        for (int i = 0; i < ACTIONS; i++) {
            boolean shown = i < d.btns.size();
            boolean danger = shown && d.btns.get(i).danger();
            ui.set("#IntAct" + i + ".Visible", shown && !danger);
            ui.set("#IntActD" + i + ".Visible", danger);
            if (shown) {
                String label = armed(d.btns.get(i).key()) ? "Confirmer : " + d.btns.get(i).label() : d.btns.get(i).label();
                ui.set("#IntAct" + i + ".Text", label);
                ui.set("#IntActD" + i + ".Text", label);
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

    private String quote() {
        return switch (cat) {
            case TABLEAU -> "L'état du royaume, d'un coup d'œil. Choisissez une carte pour le détail.";
            case JOUEURS -> managed != null ? "Chaque carte est un geste sur " + managed + ". Les gestes rouges se confirment d'un second clic."
                    : "Les joueurs connectés. Désignez-en un, puis « Gérer » pour tous les gestes.";
            case MONDE -> estates ? "Les biens : parcelles, logements, fermes, forêts et mines." : "Les domaines : royaumes, Marquisats, territoires et villes.";
            case ECONOMIE -> "L'argent en circulation, les guildes et les maisons.";
            case HOLOGRAMMES -> "Les hologrammes posés dans le monde.";
            case FAILLES -> "Les failles-donjons : un donjon par rang (F, E et D), une carte par faille ouverte.";
            case COMPETENCES -> "Les passifs, les métiers et les effets de statut : les donner, les retirer, les essayer.";
            case DONNEES -> "Remises à zéro : irréversibles. Chaque geste demande un second clic.";
        };
    }

    private Detail intro(String kicker, String title, String tagline, String text) {
        Detail d = new Detail();
        d.icon = item(cat.icon);
        d.kicker = kicker;
        d.title = title;
        d.tagline = tagline;
        d.text = text;
        return d;
    }

    private Detail detail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (key.startsWith("t:")) return tableauDetail(key, ref, store);
        if (key.startsWith("p:")) return playerDetail(key.substring(2), true);
        if (key.startsWith("a:")) return gestureDetail(key.substring(2));
        if (key.startsWith("z:")) return zoneDetail(ParcelManager.get(key.substring(2)));
        if (key.startsWith("g:")) return guildDetail(GuildManager.get(key.substring(2)));
        if (key.startsWith("f:")) return familyDetail(key.substring(2));
        if (key.startsWith("e:")) return economyDetail(key);
        if (key.startsWith("h:")) return hologramDetail(key.substring(2));
        if (key.startsWith("w:")) return wipeDetail(key.substring(2));
        if (key.startsWith("y:") || key.startsWith("x:")) return failleDetail(key);
        if (key.startsWith("ps:") || key.startsWith("mt:") || key.startsWith("fx:") || key.startsWith("rt:")) return AdminCompetences.detail(key);
        return null;
    }

    // ==================== TABLEAU ====================

    private record Census(int online, int pk, int parties, long playerMoney, int[] nobility, int[] church, int levelSum) {}

    private static Census census() {
        int online = 0, pk = 0, parties = 0, levelSum = 0;
        long money = 0;
        int[] nob = new int[NOBILITY.length], ch = new int[CHURCH.length];
        for (PlayerRef pr : Universe.get().getPlayers()) {
            online++;
            try { if (com.eldanior.system.party.PartyManager.hasParty(UUIDExtractor.getUUID(pr))) parties++; } catch (Exception ignored) { }
            PlayerLevelData d = data(pr);
            if (d == null) continue;
            money += d.getMoney();
            levelSum += d.getLevel();
            if (d.isPK()) pk++;
            for (int i = 0; i < NOBILITY.length; i++) if (NOBILITY[i].equals(d.getNobilityRank())) nob[i]++;
            for (int i = 0; i < CHURCH.length; i++) if (CHURCH[i].equals(d.getChurchRank())) ch[i]++;
        }
        return new Census(online, pk, parties / 2, money, nob, ch, levelSum);
    }

    private static long guildMoney() { long s = 0; for (Guild g : GuildManager.getAll()) s += g.getTreasury(); return s; }

    private static long familyMoney() {
        long s = 0;
        for (var f : FamilyManager.getAll()) if (FamilyManager.isFamilyTaken(f.getId())) s += FamilyManager.getRuntimeData(f.getId()).getTreasury();
        return s;
    }

    private static long parcelMoney() { long s = 0; for (ParcelData p : ParcelManager.getAll()) s += p.getTreasury(); return s; }

    private static int[] zoneCounts() {
        int[] c = new int[ParcelType.values().length];
        for (ParcelData p : ParcelManager.getAll()) c[p.getType().ordinal()]++;
        return c;
    }

    private static int takenFamilies() {
        int n = 0;
        for (var f : FamilyManager.getAll()) if (FamilyManager.isFamilyTaken(f.getId())) n++;
        return n;
    }

    private View tableau(Ref<EntityStore> ref, Store<EntityStore> store) {
        Census c = census();
        int[] z = zoneCounts();
        long total = c.playerMoney() + guildMoney() + familyMoney() + parcelMoney();
        String king = com.eldanior.system.titles.nobility.NobilityManager.getCurrentKingName();
        String pope = com.eldanior.system.titles.church.ChurchManager.getCurrentPopeName();
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("t:players", "Eldanior_Mannequin_Dojo", "J", "Joueurs",
                c.pk() + " PK  ·  " + c.parties() + " groupe(s)  ·  " + com.eldanior.system.duel.DuelManager.getActiveDuelCount() + " duel(s) en cours",
                String.valueOf(c.online()), C_BLUE));
        cards.add(new Card("t:zones", "Eldanior_Borne_Territoire", "Z", "Territoires",
                z[ParcelType.KINGDOM.ordinal()] + " royaume(s)  ·  " + (z[ParcelType.GRAND_TERRITORY.ordinal()] + z[ParcelType.TERRITORY.ordinal()])
                        + " territoire(s)  ·  " + z[ParcelType.CITY.ordinal()] + " ville(s)", String.valueOf(ParcelManager.getAll().size()), C_GREEN));
        cards.add(new Card("t:nobility", "Decret_Chevalier", "N", "Noblesse",
                c.nobility()[4] + " Marquis  ·  " + c.nobility()[3] + " Ducs  ·  " + c.nobility()[2] + " Comtes  ·  " + c.nobility()[1] + " Barons",
                king != null && !king.isEmpty() ? king : "Pas de roi", C_GOLD));
        cards.add(new Card("t:church", null, "É", "Église",
                c.church()[2] + " Cardinaux  ·  " + c.church()[1] + " Archevêques  ·  " + c.church()[0] + " Prêtres",
                pope != null && !pope.isEmpty() ? pope : "Pas de pape", C_VIOLET));
        cards.add(new Card("t:money", "Elda_Gold_Coins", "$", "Argent en circulation",
                "Joueurs, guildes, maisons et trésors des zones.", money(total) + " or", C_GOLD));
        cards.add(new Card("t:families", "Eldanior_Banniere_Famille_Eldanior", "F", "Maisons",
                "Maisons qui ont un Patriarche.", takenFamilies() + " / " + FamilyManager.getAll().size(), C_BLUE));
        cards.add(new Card("t:shop", "Eldanior_Tableau_Prime", "M", "Commerce",
                com.eldanior.system.shop.ShopManager.getListings().size() + " offre(s) au marché  ·  "
                        + com.eldanior.system.shop.ShopManager.getBlackMarketListings().size() + " au marché noir",
                (int) (com.eldanior.system.territory.ParcelEconomyManager.TAX_RATE * 100) + " % de taxe", C_ORANGE));
        int[] chests = chests(ref, store);
        cards.add(new Card("t:chests", null, "C", "Coffres au trésor",
                chests[1] + " donjon  ·  " + chests[2] + " normal  ·  " + chests[3] + " légendaire  ·  " + chests[4] + " or", String.valueOf(chests[0]), C_GREY));
        cards.add(new Card("t:content", "Eldanior_Tome_01", "+", "Contenu du mod",
                com.eldanior.system.classes.ClassManager.getAll().size() + " classes  ·  " + com.eldanior.system.skills.skillsInteraction.PassiveSkill.values().length
                        + " compétences  ·  " + com.eldanior.system.quest.QuestManager.getAllQuests().size() + " quêtes", null, C_GREY));
        int inv = com.eldanior.system.territory.laws.Invasions.list().size();
        cards.add(new Card("t:invasion", null, "!", "Invasion", "Une faille s'ouvre près du domaine où vous êtes, les monstres en sortent un par un.",
                inv > 0 ? inv + " en cours" : null, C_RED));
        var wb = com.eldanior.system.boards.WorldBosses.current();
        cards.add(new Card("t:worldboss", "Eldanior_Borne_Territoire", "B", "Boss Mondial",
                wb == null ? "Annoncer un Boss Mondial : il apparaît au hasard dans le royaume, hors des villes."
                        : wb.getKind().name() + (wb.isAppeared() ? " · apparu en " + wb.coords() : " · dans " + com.eldanior.system.boards.WorldBosses.duration(wb.getSpawnAt() - System.currentTimeMillis())),
                wb == null ? null : wb.isAppeared() ? "apparu" : "annoncé", C_GOLD));
        cards.add(new Card("t:map", null, "C", "Carte du royaume",
                "Tout le royaume chargé sur la carte des joueurs, sans rester près de son personnage.",
                com.eldanior.system.territory.systems.KingdomMap.enabled() ? "active" : "coupée",
                com.eldanior.system.territory.systems.KingdomMap.enabled() ? C_GREEN : C_GREY));
        Detail intro = intro("Administration", "Tableau du serveur", "Choisissez une carte pour le détail.", "");
        intro.fact("Joueurs", String.valueOf(c.online()), C_BLUE);
        intro.fact("PK", String.valueOf(c.pk()), c.pk() > 0 ? C_RED : null);
        intro.fact("Zones", String.valueOf(ParcelManager.getAll().size()));
        intro.fact("Guildes", String.valueOf(GuildManager.getAll().size()));
        intro.fact("Roi", king != null && !king.isEmpty() ? king : "aucun", C_GOLD);
        intro.fact("Pape", pope != null && !pope.isEmpty() ? pope : "aucun");
        intro.fact("Argent", money(total) + " or", C_GOLD);
        intro.fact("Niveau max", String.valueOf(PlayerLevelData.MAX_LEVEL));
        return new View("Tableau du serveur", "Les chiffres sont relevés à l'ouverture de chaque carte", null, cards,
                List.of(new Stat(String.valueOf(c.online()), "Joueurs connectés"), new Stat(String.valueOf(ParcelManager.getAll().size()), "Zones"),
                        new Stat(String.valueOf(GuildManager.getAll().size()), "Guildes"), new Stat(money(total) + " or", "Argent en circulation")), intro);
    }

    private static int[] chests(Ref<EntityStore> ref, Store<EntityStore> store) {
        int[] c = new int[5];
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null || player.getWorld() == null) return c;
            var template = player.getWorld().getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
            if (template == null) return c;
            for (String key : template.getTemplateKeys()) {
                c[0]++;
                String[] k = key.split(",");
                if (k.length != 3) continue;
                String drop = template.getDropList(Integer.parseInt(k[0]), Integer.parseInt(k[1]), Integer.parseInt(k[2]));
                if (drop == null) continue;
                switch (drop) {
                    case "donjon", "donjon_common" -> c[1]++;
                    case "default" -> c[2]++;
                    case "legendary" -> c[3]++;
                    case "gold" -> c[4]++;
                    default -> { }
                }
            }
        } catch (Exception e) { EldaniorLogger.error("[Admin] coffres", e); }
        return c;
    }

    private Detail tableauDetail(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        Census c = census();
        Detail d = new Detail();
        d.kicker = "Tableau du serveur";
        switch (key) {
            case "t:players" -> {
                d.icon = item("Eldanior_Mannequin_Dojo"); d.title = "Joueurs";
                d.tagline = plural(c.online(), "joueur connecté", "joueurs connectés") + ".";
                d.fact("Connectés", String.valueOf(c.online()), C_BLUE);
                d.fact("PK", String.valueOf(c.pk()), c.pk() > 0 ? C_RED : null);
                d.fact("Groupes", String.valueOf(c.parties()));
                d.fact("Duels en cours", String.valueOf(com.eldanior.system.duel.DuelManager.getActiveDuelCount()));
                d.fact("Niveau moyen", c.online() > 0 ? String.valueOf(c.levelSum() / c.online()) : "—");
                d.fact("Or des joueurs", money(c.playerMoney()) + " or", C_GOLD);
                d.btn("Voir les joueurs", "go:players");
            }
            case "t:zones" -> {
                int[] z = zoneCounts();
                int pvp = 0;
                for (ParcelData p : ParcelManager.getAll()) if (p.getType() == ParcelType.CITY && p.isPvpEnabled()) pvp++;
                d.icon = item("Eldanior_Borne_Territoire"); d.title = "Territoires";
                d.tagline = plural(ParcelManager.getAll().size(), "zone", "zones") + " en tout.";
                d.fact("Royaumes", String.valueOf(z[ParcelType.KINGDOM.ordinal()]), C_GOLD);
                d.fact("Marquisats", String.valueOf(z[ParcelType.GRAND_TERRITORY.ordinal()]));
                d.fact("Territoires", String.valueOf(z[ParcelType.TERRITORY.ordinal()]));
                d.fact("Villes", String.valueOf(z[ParcelType.CITY.ordinal()]), C_GREEN);
                d.fact("Parcelles", String.valueOf(z[ParcelType.PLOT.ordinal()]));
                d.fact("Logements", String.valueOf(z[ParcelType.HOUSING.ordinal()]));
                d.fact("Fermes", String.valueOf(z[ParcelType.FARM.ordinal()]));
                d.fact("Villes en PvP", String.valueOf(pvp), pvp > 0 ? C_RED : null);
                d.btn("Voir le monde", "go:world");
            }
            case "t:nobility" -> {
                String king = com.eldanior.system.titles.nobility.NobilityManager.getCurrentKingName();
                d.icon = item("Decret_Chevalier"); d.title = "Noblesse";
                d.tagline = "Roi : " + (king != null && !king.isEmpty() ? king : "aucun") + ". Comptés parmi les joueurs connectés.";
                for (int i = NOBILITY.length - 2; i >= 0; i--) d.fact(rankName(NOBILITY[i]), String.valueOf(c.nobility()[i]));
            }
            case "t:church" -> {
                String pope = com.eldanior.system.titles.church.ChurchManager.getCurrentPopeName();
                d.mark = "É"; d.title = "Église";
                d.tagline = "Pape : " + (pope != null && !pope.isEmpty() ? pope : "aucun") + ". Comptés parmi les joueurs connectés.";
                for (int i = CHURCH.length - 2; i >= 0; i--) d.fact(churchName(CHURCH[i]), String.valueOf(c.church()[i]));
            }
            case "t:money" -> {
                long g = guildMoney(), f = familyMoney(), p = parcelMoney();
                d.icon = item("Elda_Gold_Coins"); d.title = "Argent en circulation";
                d.tagline = money(c.playerMoney() + g + f + p) + " or en tout.";
                d.fact("Joueurs connectés", money(c.playerMoney()) + " or", C_GOLD);
                d.fact("Guildes", money(g) + " or");
                d.fact("Maisons", money(f) + " or");
                d.fact("Trésors des zones", money(p) + " or");
                d.btn("Voir l'économie", "go:economy");
            }
            case "t:families" -> {
                d.icon = item("Eldanior_Banniere_Famille_Eldanior"); d.title = "Maisons";
                d.tagline = takenFamilies() + " maison(s) sur " + FamilyManager.getAll().size() + " ont un Patriarche.";
                d.btn("Voir les maisons", "go:economy");
            }
            case "t:shop" -> {
                d.icon = item("Eldanior_Tableau_Prime"); d.title = "Commerce";
                d.fact("Marché", String.valueOf(com.eldanior.system.shop.ShopManager.getListings().size()));
                d.fact("Marché noir", String.valueOf(com.eldanior.system.shop.ShopManager.getBlackMarketListings().size()));
                d.fact("Taxe", (int) (com.eldanior.system.territory.ParcelEconomyManager.TAX_RATE * 100) + " %", C_GOLD);
                d.note("Prix du jeu : /es prix.");
            }
            case "t:chests" -> {
                int[] ch = chests(ref, store);
                d.mark = "C"; d.title = "Coffres au trésor";
                d.tagline = "Dans votre monde.";
                d.fact("Total", String.valueOf(ch[0]), C_GOLD);
                d.fact("Donjon", String.valueOf(ch[1]));
                d.fact("Normaux", String.valueOf(ch[2]));
                d.fact("Légendaires", String.valueOf(ch[3]));
                d.fact("Or", String.valueOf(ch[4]));
                d.text = "Tirage (2026-11-25) : 1 objet, un 2e selon le coffre et la Chance du joueur (stat Butin, %), un 3e avec beaucoup "
                        + "de chance. Famille, puis rang, puis objet du catalogue. Réglages : scripts/coffres/conception.py puis gen_coffres.py.";
                for (var pr : com.eldanior.system.TreasureChest.ChestLoot.profiles()) {
                    var br = com.eldanior.system.TreasureChest.ChestLoot.breakdown(pr.id());
                    d.note(pr.name() + " (" + pr.id() + ") : 2e objet " + pr.second() + " %, recharge " + (pr.cooldownSeconds() / 60) + " min, "
                            + pr.items().size() + " objets possibles (" + String.join(", ", br.keySet()) + "), rangs " + pr.ranks().keySet());
                }
            }
            case "t:content" -> {
                d.icon = item("Eldanior_Tome_01"); d.title = "Contenu du mod";
                d.fact("Monstres (XP)", String.valueOf(com.eldanior.system.config.configs.MobXP.getTotalMobCount()));
                d.fact("PNJ (XP)", String.valueOf(com.eldanior.system.config.configs.MobXP.getTotalNpcCount()));
                d.fact("Classes", String.valueOf(com.eldanior.system.classes.ClassManager.getAll().size()));
                d.fact("Titres", String.valueOf(com.eldanior.system.titles.TitleManager.getAll().size()));
                d.fact("Compétences", String.valueOf(com.eldanior.system.skills.skillsInteraction.PassiveSkill.values().length));
                d.fact("Quêtes", String.valueOf(com.eldanior.system.quest.QuestManager.getAllQuests().size()));
                d.fact("Niveau max", String.valueOf(PlayerLevelData.MAX_LEVEL));
                d.note("Relique : /es getrelic.");
            }
            case "t:invasion" -> {
                d.mark = "!"; d.title = "Invasion";
                var here = adminDomain(ref, store);
                d.tagline = here != null ? "Domaine où vous êtes : " + here.getName() + " (" + here.getType().getLabel() + ")"
                        : "Placez-vous dans une ville, un territoire ou un Marquisat.";
                d.text = "Une faille s'ouvre au sol à 24-40 blocs d'un défenseur (jamais dans une ville), marquée sur la carte ; chaque vague "
                        + "en sort monstre par monstre. Victoire : prime partagée entre les défenseurs. Défaite : le domaine est pillé (10 % du trésor).";
                d.n0 = "Vagues (1 à 10)"; d.v0 = 3L;
                d.n1 = "Niveau des monstres"; d.v1 = 50L;
                d.n2 = "Prime aux défenseurs (or, 0 : aucune)"; d.v2 = 0L;
                for (String line : com.eldanior.system.territory.laws.Invasions.list()) d.note("En cours : " + line);
                d.btn("Lancer l'invasion ici", "invstart");
                if (here != null && com.eldanior.system.territory.laws.Invasions.running(here.getId())) d.danger("Arrêter l'invasion ici", "invstop");
                d.btn("Annoncer une invasion à tous (message seul)", "invasion");
            }
            case "t:worldboss" -> {
                var e = com.eldanior.system.boards.WorldBosses.current();
                d.mark = "B"; d.icon = item("Eldanior_Borne_Territoire"); d.title = "Boss Mondial";
                if (e == null) {
                    d.tagline = "Aucun Boss Mondial en cours.";
                    d.text = "Le chat prévient tout le monde (rappels à 10, 5 et 1 min, 30 et 10 s). À l'heure dite, l'alerte donne ses coordonnées "
                            + "et un marqueur apparaît sur la carte. Lieu tiré au hasard dans le royaume, jamais dans une ville. Il se matérialise "
                            + "quand un joueur approche, avec 1 à 3 couronnes d'or tirées au hasard : niveau ×1,2, ×1,5 ou ×2, comme les "
                            + "étoiles des monstres étoilés. Pour le poser à un endroit précis : bloc de poste › catégorie Monstres › Boss Mondial.";
                    d.n0 = "Délai avant l'apparition (minutes)"; d.v0 = 5L;
                    d.n1 = "Niveau de base (0 : au hasard, " + com.eldanior.system.boards.WorldBosses.MIN_LEVEL + " à "
                            + com.eldanior.system.boards.WorldBosses.MAX_BASE_LEVEL + ")"; d.v1 = 0L;
                    d.n2 = "Durée pour l'abattre (minutes)"; d.v2 = 60L;
                    d.btn("Annoncer un Boss Mondial au hasard", "wbstart:" + com.eldanior.system.boards.WorldBosses.RANDOM);
                    for (var k : com.eldanior.system.boards.WorldBosses.KINDS)
                        d.btn("Annoncer : " + k.name() + " (" + k.species() + ", ×" + k.scaleLabel() + ")", "wbstart:" + k.id());
                } else {
                    long now = System.currentTimeMillis();
                    d.tagline = e.getKind().name() + (e.isAppeared() ? " est apparu" : " est annoncé");
                    d.fact("Lieu", e.coords(), C_GOLD);
                    d.fact("Région", e.getPlace());
                    d.fact("Niveau", e.getLevel() + "  ·  " + com.eldanior.system.boards.WorldBosses.crowns(e.getCrowns()));
                    d.fact(e.isAppeared() ? "Reste" : "Apparaît dans", com.eldanior.system.boards.WorldBosses.duration((e.isAppeared() ? e.getEndAt() : e.getSpawnAt()) - now));
                    d.fact("Monstre", e.isAlive() ? "matérialisé" : "pas encore (aucun joueur près)", e.isAlive() ? C_RED : null);
                    d.danger("Retirer le Boss Mondial", "wbstop");
                }
            }
            case "t:map" -> {
                boolean on = com.eldanior.system.territory.systems.KingdomMap.enabled();
                Player admin = store.getComponent(ref, Player.getComponentType());
                long chunks = com.eldanior.system.territory.systems.KingdomMap.chunks(admin != null ? admin.getWorld() : null);
                d.mark = "C"; d.title = "Carte du royaume";
                d.tagline = on ? "Active : " + com.eldanior.system.territory.systems.KingdomMap.scale() + " pixel par bloc" : "Coupée : la carte suit le joueur (jeu de base).";
                long[] prog = com.eldanior.system.territory.systems.KingdomMap.progress();
                if (on && prog[1] > 0) {
                    d.fact("Royaume dessiné", money(prog[0]) + " / " + money(prog[1]) + " chunks", prog[0] >= prog[1] ? C_GREEN : C_ORANGE);
                    d.fact("Préparation", prog[2] == 1 ? "en cours" : prog[0] >= prog[1] ? "terminée" : "en attente", prog[2] == 1 ? C_ORANGE : null);
                }
                d.text = "Active, la carte de chaque joueur couvre TOUT le royaume (" + money(chunks) + " chunks), se remplit en spirale depuis "
                        + "lui, ne s'efface jamais quand il s'éloigne, et se dézoome assez pour voir le royaume entier. Chaque image est gardée "
                        + "sur le disque du serveur : reconnexion et retour quasi instantanés.";
                d.note("Le serveur prépare tout le royaume en tâche de fond (une seule fois : chunks jamais visités générés et sauvegardés). "
                        + "Une fois prêt, chaque joueur reçoit à son arrivée la carte entière d'un bloc, du plus proche au plus loin.");
                d.note("Hors du royaume, seuls les lieux visités sont gardés.");
                d.note("Les lieux jamais visités sont d'abord une ESQUISSE tirée du générateur de monde (relief, couleurs des biomes, eau), "
                        + "calculée en quelques minutes pour tout le royaume sans créer de chunk ; la vraie image (arbres, constructions) "
                        + "la remplace dès qu'un joueur passe à proximité.");
                int rec = com.eldanior.system.territory.systems.KingdomMap.recommended(chunks);
                d.note("Finesse : 3 px/bloc = carte du jeu. Pour ce royaume (" + money(chunks) + " chunks), conseillé : "
                        + com.eldanior.system.territory.systems.KingdomMap.SCALES[rec] + " px/bloc (à 3 px/bloc, 100 000 chunks pèsent près d'1 Go d'images).");
                String[] labels = {"Activer (carte du jeu, 3 px/bloc)", "Activer (1 px par bloc, plus léger)", "Activer (0,5 px par bloc, très léger)"};
                for (int i = 0; i < labels.length; i++) d.btn(labels[i] + (i == rec ? " — conseillé" : ""), "map:on:" + i);
                if (on) d.danger("Couper la carte du royaume", "map:off");
            }
            default -> { return null; }
        }
        return d;
    }

    // ==================== JOUEURS ====================

    /** Domaine ou se tient l'admin (ville, territoire, Marquisat), pour l'invasion. */
    private static ParcelData adminDomain(Ref<EntityStore> ref, Store<EntityStore> store) {
        var t = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        var w = store.getExternalData().getWorld();
        return t == null || w == null ? null : com.eldanior.system.territory.laws.Invasions.domainAt(w.getName(), new org.joml.Vector3d(t.getPosition()));
    }

    private static PlayerRef online(String name) {
        return name == null ? null : Universe.get().getPlayerByUsername(name, NameMatching.EXACT_IGNORE_CASE);
    }

    private static PlayerLevelData data(PlayerRef pr) {
        try {
            var r = pr != null ? pr.getReference() : null;
            return r != null ? r.getStore().getComponent(r, EldaniorSystem.get().getPlayerLevelDataType()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String rankName(String id) {
        NobilityRank r = NobilityRank.fromString(id);
        return r != null ? r.getDisplayName() : id;
    }

    private static String churchName(String id) {
        ChurchRank r = ChurchRank.fromString(id);
        return r != null ? r.getDisplayName() : id;
    }

    private View joueurs() {
        List<PlayerRef> players = new ArrayList<>(Universe.get().getPlayers());
        players.sort(Comparator.comparing(PlayerRef::getUsername, String.CASE_INSENSITIVE_ORDER));
        List<Card> cards = new ArrayList<>();
        Census c = census();
        for (PlayerRef pr : players) {
            PlayerLevelData d = data(pr);
            String name = pr.getUsername();
            String meta = d == null ? "Données indisponibles." : "Niveau " + d.getLevel() + "  ·  " + d.getPlayerClass() + "\n" + rankName(d.getNobilityRank())
                    + "  ·  " + churchName(d.getChurchRank()) + "  ·  K/D " + d.getPlayerKills() + "/" + d.getPlayerDeaths();
            cards.add(new Card("p:" + name, null, name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(), name + (d != null && d.isPK() ? "  (PK)" : ""), meta,
                    d != null ? money(d.getMoney()) + " or" : null, d != null && d.isPK() ? C_RED : C_BLUE));
        }
        Detail intro = intro("Administration", "Joueurs", plural(players.size(), "joueur connecté", "joueurs connectés") + ".",
                "Désignez un joueur : son profil s'affiche ici. « Gérer » ouvre tous les gestes (expérience, niveau, or, classe, noblesse, Église, PK, "
                        + "titres, remise à zéro).");
        return new View("Joueurs connectés", "Désignez un joueur, puis « Gérer »", "Personne n'est connecté.", cards,
                List.of(new Stat(String.valueOf(c.online()), "Connectés"), new Stat(String.valueOf(c.pk()), "PK"),
                        new Stat(c.online() > 0 ? String.valueOf(c.levelSum() / c.online()) : "—", "Niveau moyen"),
                        new Stat(money(c.playerMoney()) + " or", "Or des joueurs")), intro);
    }

    /** Profil d'un joueur ; withManage : gestes rapides et « Gerer » (fiche de la liste). */
    private Detail playerDetail(String name, boolean withManage) {
        PlayerRef pr = online(name);
        PlayerLevelData d = data(pr);
        Detail out = new Detail();
        out.mark = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
        out.kicker = "Joueur";
        out.title = name;
        if (pr == null || d == null) {
            out.tagline = "Hors ligne.";
            return out;
        }
        out.tagline = "Niveau " + d.getLevel() + "  ·  " + d.getPlayerClass() + (d.isPK() ? "  ·  PK" : "");
        out.fact("Niveau", String.valueOf(d.getLevel()), C_GOLD);
        out.fact("Classe", d.getPlayerClass());
        out.fact("Or", money(d.getMoney()) + " or", C_GOLD);
        out.fact("Statut", d.isPK() ? "PK" + (d.isForcePK() ? " (forcé)" : "") : "honnête", d.isPK() ? C_RED : C_GREEN);
        out.fact("Noblesse", rankName(d.getNobilityRank()));
        out.fact("Église", churchName(d.getChurchRank()));
        String fam = d.getNobleFamilyId();
        out.fact("Maison", fam != null && !fam.isEmpty() ? fam : "—");
        String guild = d.getGuildId();
        Guild g = guild != null && !guild.isEmpty() ? GuildManager.get(guild) : null;
        out.fact("Guilde", g != null ? g.getName() : guild != null && !guild.isEmpty() ? guild : "—");
        out.note("Combats : " + d.getPlayerKills() + " victoire(s), " + d.getPlayerDeaths() + " mort(s).");
        out.note("Duels : " + d.getDuelWins() + " gagné(s), " + d.getDuelLosses() + " perdu(s).");
        if (d.getBounty() > 0) out.note("Prime : " + money(d.getBounty()) + " or.");
        if (withManage) {
            out.btn("Gérer " + name, "manage");
            out.btn("+10 000 XP", "qxp");
            out.btn("+10 000 or", "qgold");
            out.btn(d.isForcePK() ? "Retirer le PK" : "Rendre PK", "pk");
        }
        return out;
    }

    private View manageView(String name) {
        PlayerRef pr = online(name);
        PlayerLevelData d = data(pr);
        if (pr == null || d == null) {
            managed = null;
            error(name + " s'est déconnecté.");
            return joueurs();
        }
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("nav:players", null, "«", "Retour aux joueurs", "Revenir à la liste des joueurs connectés.", null, C_VIOLET));
        cards.add(new Card("a:xp", null, "XP", "Expérience", "Donner de l'expérience (les niveaux suivent).", money(d.getExperience()) + " XP", C_GREEN));
        cards.add(new Card("a:level", null, "N", "Niveau", "Fixer le niveau ; les points d'attribut gagnés sont ajoutés.", String.valueOf(d.getLevel()), C_GOLD));
        cards.add(new Card("a:gold", "Elda_Gold_Coins", "$", "Or", "Verser de l'or sur son compte.", money(d.getMoney()) + " or", C_GOLD));
        cards.add(new Card("a:class", null, "C", "Classe", "Changer de classe (compétences oubliées, runes interdites retirées).", d.getPlayerClass(), C_BLUE));
        cards.add(new Card("a:nob", "Decret_Chevalier", "T", "Noblesse", "Conférer un rang de noblesse.", rankName(d.getNobilityRank()), C_GOLD));
        cards.add(new Card("a:church", null, "É", "Église", "Conférer un rang dans l'Église.", churchName(d.getChurchRank()), C_VIOLET));
        cards.add(new Card("a:pk", null, "!", "Statut PK", "PK forcé : 1 000 or de prime, inscrit au tableau des primes.",
                d.isPK() ? "PK" : "Honnête", d.isPK() ? C_RED : C_GREEN));
        cards.add(new Card("a:passifs", null, "P", "Passifs", "Ses passifs, ses métiers, le détail de ses stats ; donner, retirer, maîtriser.",
                d.countLearnedPassives() + " / " + PlayerLevelData.MAX_LEARNED_PASSIVES, C_VIOLET));
        cards.add(new Card("a:runes", null, "R", "Runes", "Ses runes possédées et équipées, ses lancers, sa favorite ; donner une rune ou un manuel.",
                null, C_BLUE));
        cards.add(new Card("a:titles", null, "T", "Titres", "Remettre les titres à zéro.", null, C_ORANGE));
        cards.add(new Card("a:reset", null, "0", "Remise à zéro", "Niveau 1, classe, rangs, guilde, quêtes, barre d'action : tout repart de zéro.", null, C_RED));
        cards.add(new Card("a:welcome", null, "1", "Première connexion", "Tout effacer, retour au point d'apparition et fenêtre d'accueil, comme un nouveau joueur.",
                d.isOnboardingDone() ? null : "À l'accueil", C_RED));
        cards.add(new Card("a:cmds", null, "/", "Commandes", "Les commandes d'admin préremplies avec son nom.", null, C_GREY));
        Detail intro = playerDetail(name, false);
        intro.kicker = "Gestion du joueur";
        intro.text = "Choisissez un geste à droite.";
        return new View("Joueurs  »  " + name, "Désignez un geste", null, cards,
                List.of(new Stat(String.valueOf(d.getLevel()), "Niveau"), new Stat(d.getPlayerClass(), "Classe"),
                        new Stat(money(d.getMoney()) + " or", "Or"), new Stat(rankName(d.getNobilityRank()), "Noblesse")), intro);
    }

    private Detail gestureDetail(String g) {
        PlayerLevelData d = data(online(managed));
        if (d == null) return null;
        Detail out = new Detail();
        out.kicker = "Gérer " + managed;
        switch (g) {
            case "xp" -> {
                out.mark = "XP"; out.title = "Expérience";
                out.tagline = "Niveau " + d.getLevel() + ", " + money(d.getExperience()) + " XP.";
                out.n0 = "Expérience à donner";
                out.v0 = 10_000L;
                out.btn("Donner l'expérience du champ", "xp");
                out.btn("+1 000 XP", "xp:1000");
                out.btn("+10 000 XP", "xp:10000");
                out.btn("+100 000 XP", "xp:100000");
            }
            case "level" -> {
                out.mark = "N"; out.title = "Niveau";
                out.tagline = "Niveau actuel : " + d.getLevel() + " (maximum " + PlayerLevelData.MAX_LEVEL + ").";
                out.n0 = "Niveau";
                out.v0 = (long) d.getLevel();
                out.btn("Fixer le niveau du champ", "level");
                out.btn("Niveau 50", "level:50");
                out.btn("Niveau 100", "level:100");
                out.btn("Niveau 200", "level:200");
            }
            case "gold" -> {
                out.icon = item("Elda_Gold_Coins"); out.mark = "$"; out.title = "Or";
                out.tagline = "Compte : " + money(d.getMoney()) + " or.";
                out.n0 = "Or à verser";
                out.v0 = 10_000L;
                out.btn("Verser l'or du champ", "gold");
                out.btn("+10 000 or", "gold:10000");
                out.btn("+100 000 or", "gold:100000");
                out.btn("+1 000 000 or", "gold:1000000");
            }
            case "class" -> {
                out.mark = "C"; out.title = "Classe";
                out.tagline = "Classe actuelle : " + d.getPlayerClass() + ".";
                out.note("Ses compétences sont oubliées et les runes que la nouvelle classe n'autorise pas sont retirées.");
                out.note("Dieu Martial : /es setclass " + managed + " dieu_martial.");
                for (String[] c : CLASSES) {
                    var model = com.eldanior.system.classes.ClassManager.get(c[0]);
                    if (model != null) out.btn(model.getDisplayName(), "class:" + c[0]);
                }
            }
            case "nob" -> {
                out.icon = item("Decret_Chevalier"); out.mark = "T"; out.title = "Noblesse";
                out.tagline = "Rang actuel : " + rankName(d.getNobilityRank()) + ".";
                out.note("Le Roi devient Patriarche de la maison Eldanior.");
                for (String r : NOBILITY) out.btn(rankName(r), "nob:" + r);
            }
            case "church" -> {
                out.mark = "É"; out.title = "Église";
                out.tagline = "Rang actuel : " + churchName(d.getChurchRank()) + ".";
                for (String r : CHURCH) out.btn(churchName(r), "church:" + r);
            }
            case "pk" -> {
                out.mark = "!"; out.title = "Statut PK";
                out.tagline = d.isPK() ? "PK" + (d.isForcePK() ? " (forcé par un admin)." : " (par ses meurtres).") : "Honnête.";
                out.fact("Prime", money(d.getBounty()) + " or", d.getBounty() > 0 ? C_RED : null);
                out.note("PK forcé : 1 000 or de prime, inscrit au tableau des primes. Retirer le PK forcé efface la prime s'il n'est plus PK.");
                out.btn(d.isForcePK() ? "Retirer le PK forcé" : "Rendre PK", "pk");
            }
            case "titles" -> {
                out.mark = "T"; out.title = "Titres";
                out.tagline = "Retire tous les titres du joueur.";
                out.danger("Remettre les titres à zéro", "titles");
            }
            case "welcome" -> {
                out.mark = "1"; out.title = "Première connexion";
                out.tagline = "Irréversible.";
                out.note("Comme une perte totale : niveau 1, classe, compétences, runes, titres, noblesse, maison, guilde, quêtes oubliés.");
                out.note("Tout l'inventaire, la poche dimensionnelle et le sac à dos sont vidés ; 1 000 or.");
                out.note("Le joueur est renvoyé au point d'apparition, invisible et invulnérable, et la fenêtre d'accueil s'ouvre : "
                        + "présentation, commandes, choix de l'une des 5 classes de base, puis « Se connecter ».");
                out.danger("Remettre " + managed + " en première connexion", "welcome");
            }
            case "reset" -> {
                out.mark = "0"; out.title = "Remise à zéro";
                out.tagline = "Irréversible.";
                out.note("Niveau 1, attributs à 1, classe Novice, compétences, runes et titres oubliés.");
                out.note("Noblesse, maison, Église, guilde, combats, primes, quêtes et temps de recharge effacés ; 1 000 or.");
                out.note("La barre d'action est vidée.");
                out.danger("Remettre " + managed + " à zéro", "reset");
            }
            case "passifs" -> { return AdminCompetences.playerPassives(managed, d); }
            case "runes" -> { return AdminCompetences.playerRunes(managed, online(managed)); }
            case "cmds" -> {
                out.mark = "/"; out.title = "Commandes";
                out.tagline = "Préremplies avec le nom du joueur.";
                for (String c : commands()) out.note("/es " + c);
                out.btn("Les écrire dans le chat", "cmds");
            }
            default -> { return null; }
        }
        return out;
    }

    private List<String> commands() {
        String n = managed;
        return List.of("titleadmin grant " + n + " <titre>", "titleadmin remove " + n + " <titre>", "familyset " + n + " <maison>",
                "setclass " + n + " <classe>", "rank demote " + n, "church demote " + n, "nstatus setvice " + n, "classinfo " + n,
                "guildcreate <nom> <tag>", "kingdom");
    }

    // ==================== MONDE ====================

    private static boolean isDomain(ParcelData p) {
        return switch (p.getType()) {
            case KINGDOM, GRAND_TERRITORY, TERRITORY, CITY -> true;
            default -> false;
        };
    }

    private static String typeColor(ParcelType t) {
        return switch (t) {
            case KINGDOM -> C_GOLD;
            case GRAND_TERRITORY -> C_VIOLET;
            case TERRITORY -> C_BLUE;
            case CITY -> C_GREEN;
            case HOUSING -> C_ORANGE;
            default -> C_GREY;
        };
    }

    private static String typeIcon(ParcelData p) {
        return p.getType() == ParcelType.CITY ? "Eldanior_Borne_Ville" : isDomain(p) ? "Eldanior_Borne_Territoire" : "Eldanior_Affiche_Propriete";
    }

    private static String state(ParcelData p) {
        if (isDomain(p)) {
            boolean set = p.getType() == ParcelType.CITY ? !p.getGuildId().isEmpty() || !p.getFamilyId().isEmpty() : !p.getFamilyId().isEmpty();
            return set ? "configuré" : "sans maison ni guilde";
        }
        if (p.isRented()) return p.isRentExpired() ? "location expirée" : "en location";
        if (p.isBought()) return "acheté";
        if (p.isForSale() && p.isForRent()) return "à vendre et à louer";
        if (p.isForSale()) return "à vendre";
        if (p.isForRent()) return "à louer";
        return p.getPrice() <= 0 && p.getRentPrice() <= 0 && (p.getOwnerName() == null || p.getOwnerName().isEmpty()) ? "à configurer" : "libre";
    }

    private View monde() {
        List<ParcelData> all = new ArrayList<>(ParcelManager.getAll());
        List<ParcelData> shown = new ArrayList<>();
        int domains = 0, others = 0, cities = 0, pvp = 0;
        for (ParcelData p : all) {
            if (isDomain(p)) domains++; else others++;
            if (p.getType() == ParcelType.CITY) cities++;
            if (p.isPvpEnabled()) pvp++;
            if (isDomain(p) != estates) shown.add(p);
        }
        shown.sort(Comparator.comparingInt((ParcelData p) -> p.getType().ordinal())
                .thenComparing(p -> p.getName() != null ? p.getName() : p.getId(), String.CASE_INSENSITIVE_ORDER));
        List<Card> cards = new ArrayList<>();
        cards.add(estates ? new Card("nav:domains", "Eldanior_Borne_Territoire", "«", "Voir les domaines", plural(domains, "domaine", "domaines")
                + " : royaumes, Marquisats, territoires, villes.", null, C_VIOLET)
                : new Card("nav:estates", "Eldanior_Affiche_Propriete", "»", "Voir les biens", plural(others, "bien", "biens")
                + " : parcelles, logements, fermes, forêts, mines.", null, C_VIOLET));
        for (ParcelData p : shown) {
            ParcelData parent = p.getParentId() != null && !p.getParentId().isEmpty() ? ParcelManager.get(p.getParentId()) : null;
            String owner = p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "sans maître";
            String meta = p.getType().getLabel() + (parent != null ? " de " + parent.getName() : "") + "\n" + owner + "  ·  " + state(p)
                    + (p.isPvpEnabled() ? "  ·  PvP" : "");
            cards.add(new Card("z:" + p.getId(), typeIcon(p), p.getType().getLabel().substring(0, 1), p.getName() != null ? p.getName() : p.getId(), meta,
                    isDomain(p) ? money(p.getTreasury()) + " or" : p.getPrice() > 0 ? money(p.getPrice()) + " or" : null, typeColor(p.getType())));
        }
        Detail intro = intro("Administration", estates ? "Biens" : "Domaines", plural(shown.size(), "zone affichée", "zones affichées") + ".",
                "Désignez une zone pour la régler : combat, protection, vente, prix, trésor, maison, impôt, suppression. "
                        + "Pour en créer une : la borne de territoire, ou /es pcreate.");
        return new View(estates ? "Monde  »  Biens" : "Monde  »  Domaines", "Désignez une zone", "Aucune zone.", cards,
                List.of(new Stat(String.valueOf(domains), "Domaines"), new Stat(String.valueOf(cities), "Villes"),
                        new Stat(String.valueOf(others), "Biens"), new Stat(String.valueOf(pvp), "Zones en PvP")), intro);
    }

    private Detail zoneDetail(ParcelData p) {
        if (p == null) return null;
        boolean dom = isDomain(p);
        Detail d = new Detail();
        d.icon = item(typeIcon(p));
        ParcelData parent = p.getParentId() != null && !p.getParentId().isEmpty() ? ParcelManager.get(p.getParentId()) : null;
        d.kicker = p.getType().getLabel() + (parent != null ? " de " + parent.getName() : "") + "  ·  " + p.getId();
        d.title = p.getName() != null ? p.getName() : p.getId();
        int sx = Math.abs(p.getMaxX() - p.getMinX()) + 1, sy = Math.abs(p.getMaxY() - p.getMinY()) + 1, sz = Math.abs(p.getMaxZ() - p.getMinZ()) + 1;
        d.tagline = sx + " x " + sz + " blocs" + (p.isCylinder() ? " (cylindre)" : "") + ", " + sy + " de haut  ·  " + state(p);
        String fam = p.getFamilyId();
        var family = fam != null && !fam.isEmpty() ? FamilyManager.get(fam) : null;
        d.fact("Maître", p.getOwnerName() != null && !p.getOwnerName().isEmpty() ? p.getOwnerName() : "aucun");
        d.fact("Trésor", money(p.getTreasury()) + " or", C_GOLD);
        d.fact("Combat", p.isPvpEnabled() ? "permis" : "interdit", p.isPvpEnabled() ? C_RED : C_GREEN);
        d.fact("Blocs", p.isProtectedByDefault() ? "protégés" : "ouverts", p.isProtectedByDefault() ? null : C_ORANGE);
        d.fact("Maison", family != null ? family.getDisplayName() : fam != null && !fam.isEmpty() ? fam : "—");
        d.fact("Guilde", p.getGuildId() != null && !p.getGuildId().isEmpty() ? p.getGuildId() : "—");
        d.fact("Vente", p.isForSale() ? money(p.getPrice()) + " or" : "non", p.isForSale() ? C_GREEN : null);
        d.fact("Loyer (7 j)", p.isForRent() ? money(p.getRentPrice()) + " or" : "non", p.isForRent() ? C_BLUE : null);
        d.text = "De (" + p.getMinX() + ", " + p.getMinY() + ", " + p.getMinZ() + ") à (" + p.getMaxX() + ", " + p.getMaxY() + ", " + p.getMaxZ() + ").";
        if (p.getRenterUUID() != null) {
            PlayerRef r = Universe.get().getPlayer(p.getRenterUUID());
            long left = p.getRentEndTime() - System.currentTimeMillis();
            d.note("Locataire : " + (r != null ? r.getUsername() : "hors ligne") + (p.getRentEndTime() > 0 ? left > 0 ? ", fin dans " + duration(left) + "." : ", bail expiré." : "."));
        }
        if (dom) {
            d.note("Taxe : " + (int) (p.getTaxRate() * 100) + " %" + (p.canCollectTax() ? ", impôt prêt à lever." : "."));
            if (p.getLastTaxCollection() > 0) {
                long next = p.getLastTaxCollection() + WEEK_MS - System.currentTimeMillis();
                d.note("Impôt : " + (next > 0 ? "prochain dans " + duration(next) : "réclamable") + (p.getLastTaxAmount() > 0 ? ", dernier : " + money(p.getLastTaxAmount()) + " or." : "."));
            }
        }
        List<String> kids = ParcelManager.getChildrenOf(p.getId());
        if (!kids.isEmpty()) {
            StringBuilder sb = new StringBuilder(plural(kids.size(), "sous-zone", "sous-zones") + " : ");
            for (int i = 0; i < Math.min(6, kids.size()); i++) {
                ParcelData k = ParcelManager.get(kids.get(i));
                if (k != null) sb.append(i > 0 ? ", " : "").append(k.getName());
            }
            d.note(sb.append(kids.size() > 6 ? "…" : ".").toString());
        }
        if (!zoneMore) {
            d.n0 = "Prix de vente (or)";
            d.v0 = p.getPrice();
            d.n1 = "Loyer pour 7 jours (or)";
            d.v1 = p.getRentPrice();
            d.btn("Fixer le prix et le loyer", "zprices");
            d.btn(p.isPvpEnabled() ? "Interdire le combat" : "Permettre le combat", "zpvp");
            d.btn(p.isProtectedByDefault() ? "Ouvrir les blocs" : "Protéger les blocs", "zprotect");
            d.btn(p.isForSale() ? "Retirer de la vente" : "Mettre en vente", "zsale");
            d.btn("Gestes avancés…", "zmore");
            d.danger("Supprimer la zone", "zdelete");
            return d;
        }
        d.kicker = "Gestes avancés  ·  " + d.kicker;
        d.note("Guilde : /es parcel assignguild " + p.getId() + " <guilde>.");
        d.n2 = "Or à ajouter au trésor";
        d.v2 = 10_000L;
        d.t0 = "Maison (identifiant) : " + FamilyManager.getAvailableIds();
        d.s0 = fam != null ? fam : "";
        d.btn("Ajouter au trésor", "ztreasury");
        d.btn("Attribuer à la maison", "zfamily");
        if (dom && p.canCollectTax() && !p.getChildIds().isEmpty()) d.btn("Lever l'impôt", "ztax");
        if (p.canTransferTreasury() && p.getTreasury() > 0) d.btn("Rapatrier la moitié", "ztransfer");
        d.danger("Retirer le maître", "zclearowner");
        d.btn("« Gestes courants", "zless");
        return d;
    }

    // ==================== ECONOMIE ====================

    private View economie() {
        long pm = census().playerMoney(), gm = guildMoney(), fm = familyMoney(), zm = parcelMoney();
        List<Card> cards = new ArrayList<>();
        cards.add(new Card("e:shop", "Eldanior_Tableau_Prime", "M", "Commerce",
                com.eldanior.system.shop.ShopManager.getListings().size() + " offre(s)  ·  " + com.eldanior.system.shop.ShopManager.getBlackMarketListings().size()
                        + " au marché noir", (int) (com.eldanior.system.territory.ParcelEconomyManager.TAX_RATE * 100) + " % de taxe", C_ORANGE));
        cards.add(new Card("e:zones", "Eldanior_Borne_Territoire", "Z", "Trésors des zones", "La somme des trésors de toutes les zones.", money(zm) + " or", C_GOLD));
        List<Guild> guilds = new ArrayList<>(GuildManager.getAll());
        guilds.sort((a, b) -> Long.compare(b.getTreasury(), a.getTreasury()));
        for (Guild g : guilds)
            cards.add(new Card("g:" + g.getId(), null, g.getTag() != null && !g.getTag().isEmpty() ? g.getTag().substring(0, 1) : "G", g.getName(),
                    "Guilde [" + g.getTag() + "]  ·  " + plural(g.getMemberCount(), "membre", "membres"), money(g.getTreasury()) + " or", C_BLUE));
        for (var f : FamilyManager.getAll()) {
            if (!FamilyManager.isFamilyTaken(f.getId())) continue;
            var rd = FamilyManager.getRuntimeData(f.getId());
            String id = f.getId();
            cards.add(new Card("f:" + id, "Eldanior_Banniere_Famille_" + Character.toUpperCase(id.charAt(0)) + id.substring(1), "F", "Maison " + f.getDisplayName(),
                    "Contribution : " + money(rd.getContribution()) + " or", money(rd.getTreasury()) + " or", C_GOLD));
        }
        Detail intro = intro("Administration", "Économie", money(pm + gm + fm + zm) + " or en circulation.", "");
        intro.fact("Joueurs connectés", money(pm) + " or", C_GOLD);
        intro.fact("Guildes", money(gm) + " or");
        intro.fact("Maisons", money(fm) + " or");
        intro.fact("Trésors des zones", money(zm) + " or");
        return new View("Économie", "Guildes par trésor, puis les maisons", null, cards,
                List.of(new Stat(money(pm + gm + fm + zm) + " or", "En circulation"), new Stat(money(pm) + " or", "Joueurs"),
                        new Stat(money(gm) + " or", "Guildes"), new Stat(money(fm) + " or", "Maisons")), intro);
    }

    private Detail guildDetail(Guild g) {
        if (g == null) return null;
        Detail d = new Detail();
        d.mark = g.getTag() != null && !g.getTag().isEmpty() ? g.getTag().substring(0, 1) : "G";
        d.kicker = "Guilde [" + g.getTag() + "]";
        d.title = g.getName();
        d.fact("Trésor", money(g.getTreasury()) + " or", C_GOLD);
        d.fact("Membres", String.valueOf(g.getMemberCount()));
        d.fact("Identifiant", g.getId());
        return d;
    }

    private Detail familyDetail(String id) {
        var f = FamilyManager.get(id);
        if (f == null) return null;
        var rd = FamilyManager.getRuntimeData(id);
        Detail d = new Detail();
        d.icon = item("Eldanior_Banniere_Famille_" + Character.toUpperCase(id.charAt(0)) + id.substring(1));
        d.mark = "F";
        d.kicker = "Maison";
        d.title = f.getDisplayName();
        d.fact("Coffres", money(rd.getTreasury()) + " or", C_GOLD);
        d.fact("Contribution", money(rd.getContribution()) + " or");
        d.fact("Identifiant", id);
        d.note("Le registre de la maison se tient chez son intendant.");
        return d;
    }

    private Detail economyDetail(String key) {
        Detail d = new Detail();
        d.kicker = "Économie";
        if ("e:shop".equals(key)) {
            d.icon = item("Eldanior_Tableau_Prime"); d.title = "Commerce";
            d.fact("Marché", String.valueOf(com.eldanior.system.shop.ShopManager.getListings().size()));
            d.fact("Marché noir", String.valueOf(com.eldanior.system.shop.ShopManager.getBlackMarketListings().size()));
            d.fact("Taxe", (int) (com.eldanior.system.territory.ParcelEconomyManager.TAX_RATE * 100) + " %", C_GOLD);
            d.note("Prix du jeu : /es prix.");
            return d;
        }
        d.icon = item("Eldanior_Borne_Territoire"); d.title = "Trésors des zones";
        List<ParcelData> rich = new ArrayList<>(ParcelManager.getAll());
        rich.sort((a, b) -> Long.compare(b.getTreasury(), a.getTreasury()));
        d.tagline = money(parcelMoney()) + " or en tout. Les plus riches :";
        for (int i = 0; i < Math.min(NOTES, rich.size()); i++)
            if (rich.get(i).getTreasury() > 0) d.note(rich.get(i).getName() + " (" + rich.get(i).getType().getLabel() + ") : " + money(rich.get(i).getTreasury()) + " or");
        return d;
    }

    // ==================== HOLOGRAMMES ====================

    // ==================== FAILLES ====================

    private static String rankColor(com.eldanior.system.failles.FailleRank r) { return r == com.eldanior.system.failles.FailleRank.F ? "#e8e8e8" : r.color; }

    private View failles() {
        List<Card> cards = new ArrayList<>();
        for (var d : com.eldanior.system.failles.FailleDungeon.all().values()) {
            boolean fixed = com.eldanior.system.failles.FailleManager.isFixed(d), edit = com.eldanior.system.failles.FailleManager.isEditing(d);
            cards.add(new Card("y:" + d.id, "Eldanior_Faille_Scellee_" + d.rank.label, d.rank.label, d.name,
                    "Donjon de rang " + d.rank.label + "\n" + (edit ? "EN ÉDITION" : fixed ? "instance fixe (modifiable)" : "plan d'origine, jamais édité"),
                    "Rang " + d.rank.label, rankColor(d.rank)));
        }
        long now = System.currentTimeMillis();
        int open = 0, running = 0;
        for (var f : com.eldanior.system.failles.FailleManager.all()) {
            if (f.getState() == com.eldanior.system.failles.Faille.State.FERMEE || f.getState() == com.eldanior.system.failles.Faille.State.RATEE) continue;
            if (f.getState() == com.eldanior.system.failles.Faille.State.EN_COURS) running++; else open++;
            cards.add(new Card("x:" + f.getId(), "Eldanior_Faille_Scellee_" + f.getRank().label, "!", "Faille " + f.getRank().label + " · " + f.coordsLabel(),
                    f.statusLabel(now) + "\n" + f.entriesLabel() + "  ·  " + f.getWorld(), f.getState() == com.eldanior.system.failles.Faille.State.EN_COURS ? "en cours" : "ouverte",
                    rankColor(f.getRank())));
        }
        cards.add(new Card("x:near", null, "×", "Supprimer la faille la plus proche", "La faille à moins de 64 blocs de vous : bloc, structure, donjon en cours.", null, C_RED));
        boolean nat = com.eldanior.system.failles.FailleManager.naturalEnabled();
        cards.add(new Card("x:nat", null, "N", "Apparitions naturelles", nat ? "Actives : 1 faille pour 3 joueurs connectés, 8 au plus." : "Coupées.",
                nat ? "actives" : "coupées", nat ? C_GREEN : C_GREY));
        Detail intro = intro("Administration", "Failles-donjons", "Trois donjons : le Cimetière des Oubliés (F), le Terrier du Duc (E) et la Nécropole Calcinée (D).",
                "Choisissez un DONJON pour ouvrir sa faille autour de vous (dans " + com.eldanior.system.failles.FailleManager.ADMIN_DELAY_S
                        + " s) ou éditer son instance. Choisissez une FAILLE ouverte pour la supprimer.");
        return new View("Failles", "Désignez un donjon ou une faille", "Aucun donjon.", cards,
                List.of(new Stat(String.valueOf(open), "Ouvertes"), new Stat(String.valueOf(running), "En cours"),
                        new Stat(String.valueOf(com.eldanior.system.failles.FailleDungeon.all().size()), "Donjons"),
                        new Stat(nat ? "oui" : "non", "Naturelles")), intro);
    }

    private Detail failleDetail(String key) {
        Detail d = new Detail();
        if (key.equals("x:near")) {
            d.mark = "×"; d.kicker = "Failles"; d.title = "Supprimer la faille la plus proche";
            d.text = "Retire la faille à moins de 64 blocs de vous : son portail, sa structure de roche (le terrain d'origine revient), son "
                    + "compteur et, si un groupe est dedans, son donjon (les joueurs sont renvoyés dehors). Pas de débordement.";
            return d.danger("Supprimer la faille la plus proche", "fnear");
        }
        if (key.equals("x:nat")) {
            boolean nat = com.eldanior.system.failles.FailleManager.naturalEnabled();
            d.mark = "N"; d.kicker = "Failles"; d.title = "Apparitions naturelles";
            d.tagline = nat ? "Actives" : "Coupées";
            d.text = "Toutes les 10 min, une chance sur deux : une faille s'annonce (étincelles, " + com.eldanior.system.failles.FailleManager.ADMIN_DELAY_S
                    + " s) à 64-200 blocs d'un joueur, hors des villes. Au plus 1 faille pour 3 joueurs (8 au plus), jamais au-dessus du rang "
                    + "du joueur, et le même donjon ne revient pas avant 1 h.";
            d.btn(nat ? "Couper les apparitions naturelles" : "Activer les apparitions naturelles", "fauto");
            d.btn("Forcer une apparition maintenant (près d'un joueur)", "fnat");
            if (!com.eldanior.system.failles.FailleManager.all().isEmpty()) d.danger("Supprimer TOUTES les failles", "fclose");
            return d;
        }
        if (key.startsWith("y:")) {
            var dg = com.eldanior.system.failles.FailleDungeon.get(key.substring(2));
            if (dg == null) return null;
            boolean fixed = com.eldanior.system.failles.FailleManager.isFixed(dg), edit = com.eldanior.system.failles.FailleManager.isEditing(dg);
            d.icon = item("Eldanior_Faille_Scellee_" + dg.rank.label); d.mark = dg.rank.label; d.kicker = "Donjon de rang " + dg.rank.label; d.title = dg.name;
            d.tagline = edit ? "En cours d'édition" : fixed ? "Instance fixe : vos retouches servent à chaque faille" : "Plan d'origine (jamais édité)";
            d.fact("Rang", dg.rank.label, rankColor(dg.rank));
            d.fact("Instance", edit ? "en édition" : fixed ? "fixe" : "plan d'origine", edit ? C_ORANGE : fixed ? C_GREEN : C_GREY);
            d.fact("Boss reconnu", dg.bossRoom().boss.role() + " (et ses phases)");
            d.fact("Porte du boss", dg.door != null ? dg.door.block().replace("Eldanior_Porte_Boss_", "porte de boss ") : "aucune");
            d.fact("Clé", dg.keyName);
            d.fact("Durée", dg.minutes + " min");
            d.fact("Arrivée", com.eldanior.system.failles.FailleManager.hasArrival(dg) ? "placée par vous" : "celle du plan");
            d.text = "1) « Ouvrir » : une faille de ce donjon s'annonce autour de vous (étincelles) et s'ouvre " + com.eldanior.system.failles.FailleManager.ADMIN_DELAY_S
                    + " s après le clic.\n2) « Éditer » : vous entrez dans le MODÈLE de l'instance, en créatif et sans monstre. Posez vos balises "
                    + "NPC_Spawn_Marker (elles feront apparaître les monstres dans chaque faille), vos coffres au trésor et vos sorties "
                    + "(Eldanior_Faille_Sortie). Le boss est reconnu à son rôle : " + dg.bossRoom().boss.role() + ".\n3) « Terminer » : le modèle est "
                    + "sauvegardé (Mods/EldaniorFailles) ; chaque faille ouverte ensuite en est une copie.";
            d.n0 = "Taille du groupe (1 à 10)"; d.v0 = 3L;
            d.btn("Ouvrir cette faille autour de moi (dans " + com.eldanior.system.failles.FailleManager.ADMIN_DELAY_S + " s)", "fopen");
            d.btn(edit ? "Retourner dans l'édition" : "Éditer l'instance (créatif)", "fedit");
            d.btn("Terminer l'édition et sauvegarder", "fdone");
            d.btn("Placer l'arrivée des joueurs ici (en édition)", "farr");
            d.danger("Remettre le donjon d'origine (efface vos retouches)", "freset");
            return d;
        }
        var f = com.eldanior.system.failles.FailleManager.get(key.substring(2));
        if (f == null) return null;
        long now = System.currentTimeMillis();
        d.icon = item("Eldanior_Faille_Scellee_" + f.getRank().label); d.mark = "!"; d.kicker = "Faille ouverte"; d.title = f.title();
        d.tagline = f.statusLabel(now);
        d.fact("Lieu", f.coordsLabel(), rankColor(f.getRank()));
        d.fact("Monde", f.getWorld());
        d.fact("Entrées", f.entriesLabel());
        d.fact("Origine", f.getSource().name().toLowerCase());
        d.fact("Joueurs entrés", f.enteredNames());
        d.text = "Supprimer : le portail et sa structure disparaissent, le terrain d'origine revient ; un groupe dans le donjon est renvoyé dehors. "
                + "Aucun débordement.";
        return d.danger("Supprimer cette faille", "fdel");
    }

    private View hologrammes() {
        List<Card> cards = new ArrayList<>();
        for (var h : HologramManager.getAll())
            cards.add(new Card("h:" + h.getId(), null, "H", h.getId(), h.getText() + "\n" + h.getLocationString(), h.getWorldName(), C_BLUE));
        Detail intro = intro("Administration", "Hologrammes", plural(cards.size(), "hologramme", "hologrammes") + ".",
                "Désignez un hologramme pour le supprimer. Pour en poser un : /es holo.");
        return new View("Hologrammes", null, "Aucun hologramme.", cards,
                List.of(new Stat(String.valueOf(cards.size()), "Hologrammes")), intro);
    }

    private Detail hologramDetail(String id) {
        var h = HologramManager.get(id);
        if (h == null) return null;
        Detail d = new Detail();
        d.mark = "H";
        d.kicker = "Hologramme";
        d.title = h.getId();
        d.tagline = h.getText();
        d.fact("Monde", h.getWorldName());
        d.fact("Position", h.getLocationString());
        return d.danger("Supprimer l'hologramme", "holodel");
    }

    // ==================== DONNEES ====================

    private static final String[][] WIPES = {
            {"guilds", "Guildes", "Toutes les guildes et leurs trésors (guilds.properties)."},
            {"families", "Maisons et noblesse", "Maisons, hiérarchies et rangs de noblesse (families.properties, hierarchies.properties)."},
            {"parcels", "Zones", "Toutes les zones : royaumes, territoires, villes et biens (parcels.properties)."},
            {"shop", "Commerce", "Le marché et le marché noir (shop.properties, blackmarket.properties)."},
            {"classements", "Classements", "Tous les classements (classements.properties)."},
            {"all", "Tout", "Guildes, maisons, zones et tout le dossier eldanior_data. Redémarrer le serveur ensuite."}};

    private View donnees() {
        List<Card> cards = new ArrayList<>();
        for (String[] w : WIPES) cards.add(new Card("w:" + w[0], null, "0", w[1], w[2], "Effacer", C_RED));
        Detail intro = intro("Administration", "Données", "Remises à zéro du serveur.",
                "Chaque remise à zéro est irréversible : désignez-la, puis cliquez deux fois sur le bouton rouge.");
        return new View("Données", "Irréversible", null, cards, List.of(), intro);
    }

    private Detail wipeDetail(String id) {
        for (String[] w : WIPES) {
            if (!w[0].equals(id)) continue;
            Detail d = new Detail();
            d.mark = "0";
            d.kicker = "Remise à zéro";
            d.title = w[1];
            d.tagline = "Irréversible.";
            d.text = w[2];
            return d.danger("Effacer : " + w[1].toLowerCase(), "wipe");
        }
        return null;
    }

    // ==================== EVENEMENTS ====================

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Ev ev) {
        if (ev.action == null) return;
        if (!admin(ref, store)) { close(); return; }
        AdminTab.setAdminContext(adminUuid(ref, store));
        boolean keep = false;
        try {
            switch (ev.action) {
                case "close" -> { close(); return; }
                case "cat" -> {
                    int i = parse(ev.index);
                    if (i < 0 || i >= Cat.values().length || Cat.values()[i] == cat) return;
                    cat = Cat.values()[i];
                    page = 0;
                    selected = null;
                    armed = null;
                    managed = null;
                    zoneMore = false;
                }
                case "card" -> {
                    int i = parse(ev.index);
                    int n = page * CARDS + i;
                    if (i < 0 || n >= lastCards.size()) return;
                    String key = lastCards.get(n).key();
                    armed = null;
                    zoneMore = false;
                    if (key.startsWith("nav:")) { navigate(key); break; }
                    selected = key.equals(selected) ? null : key;
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
                    Detail d = selected != null ? detail(selected, ref, store) : currentIntro(ref, store);
                    int i = parse(ev.index);
                    if (d == null || i < 0 || i >= d.btns.size()) return;
                    Btn b = d.btns.get(i);
                    if (b.danger() && !arm(b.key())) { error("Cliquez encore pour confirmer : " + b.label() + "."); keep = true; break; }
                    act(b.key(), ref, store);
                    if (closedByAct) return;                                  // ecran ferme par le geste : rien a mettre a jour
                    keep = true;
                }
                default -> { return; }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Admin] " + ev.action, e);
            error("Erreur : " + e.getMessage());
            keep = true;
        }
        if (!keep) status = null;
        UICommandBuilder ui = new UICommandBuilder();
        fill(ui, ref, store);
        sendUpdate(ui);
    }

    /** Fiche d'accueil de la vue courante (pour ses boutons). */
    private Detail currentIntro(Ref<EntityStore> ref, Store<EntityStore> store) {
        return switch (cat) {
            case TABLEAU -> tableau(ref, store).intro();
            case JOUEURS -> managed != null ? manageView(managed).intro() : joueurs().intro();
            case MONDE -> monde().intro();
            case ECONOMIE -> economie().intro();
            case HOLOGRAMMES -> hologrammes().intro();
            case FAILLES -> failles().intro();
            case COMPETENCES -> AdminCompetences.view(skMode).intro();
            case DONNEES -> donnees().intro();
        };
    }

    private void navigate(String key) {
        page = 0;
        selected = null;
        switch (key) {
            case "nav:players" -> {
                String back = managed;
                managed = null;
                if (back != null) selected = "p:" + back;
            }
            case "nav:estates" -> estates = true;
            case "nav:domains" -> estates = false;
            case "nav:sk:PASSIFS" -> skMode = AdminCompetences.Mode.PASSIFS;
            case "nav:sk:METIERS" -> skMode = AdminCompetences.Mode.METIERS;
            case "nav:sk:EFFETS" -> skMode = AdminCompetences.Mode.EFFETS;
            case "nav:sk:RUNES" -> skMode = AdminCompetences.Mode.RUNES;
            default -> { }
        }
    }

    private void act(String key, Ref<EntityStore> ref, Store<EntityStore> store) {
        Player admin = store.getComponent(ref, Player.getComponentType());
        String target = managed != null ? managed : selected != null && selected.startsWith("p:") ? selected.substring(2) : null;
        String[] kv = key.split(":", 2);
        String k = kv[0], arg = kv.length > 1 ? kv[1] : null;
        switch (k) {
            case "go" -> {
                cat = switch (arg) { case "players" -> Cat.JOUEURS; case "world" -> Cat.MONDE; default -> Cat.ECONOMIE; };
                selected = null; page = 0; managed = null;
            }
            case "invstart", "invstop" -> {
                var here = adminDomain(ref, store);
                var world = store.getExternalData().getWorld();
                String msg = "invstart".equals(k)
                        ? com.eldanior.system.territory.laws.Invasions.start(here, world, (int) n0, (int) n1, n2)
                        : com.eldanior.system.territory.laws.Invasions.stop(here);
                if (msg.startsWith("Invasion lancée") || msg.startsWith("Invasion de")) ok(msg); else error(msg);
            }
            case "wbstart" -> {
                String msg = com.eldanior.system.boards.WorldBosses.start(arg, store.getExternalData().getWorld(), (int) n0, (int) n1, (int) n2);
                if (com.eldanior.system.boards.WorldBosses.current() != null && msg.contains("annoncé")) ok(msg); else error(msg);
            }
            case "wbstop" -> ok(com.eldanior.system.boards.WorldBosses.stop());
            case "fopen", "fedit", "fdone", "farr", "freset" -> {
                String id = selected != null && selected.startsWith("y:") ? selected.substring(2) : null;
                if (id == null && !"fdone".equals(k)) { error("Choisissez d'abord un donjon."); break; }
                String msg = switch (k) {
                    case "fopen" -> com.eldanior.system.failles.FailleManager.openNearDelayed(ref, id, (int) Math.max(1, Math.min(10, n0)));
                    case "fedit" -> com.eldanior.system.failles.FailleManager.edit(ref, id);
                    case "fdone" -> com.eldanior.system.failles.FailleManager.finishEdit(ref);
                    case "farr" -> com.eldanior.system.failles.FailleManager.setArrival(ref);
                    default -> com.eldanior.system.failles.FailleManager.resetDungeon(id);
                };
                boolean good = !msg.startsWith("Sortez") && !msg.startsWith("Placez") && !msg.startsWith("Pas de") && !msg.startsWith("Impossible")
                        && !msg.startsWith("Donjon") && !msg.startsWith("Terminez") && !msg.startsWith("Les failles") && !msg.startsWith("Une faille est")
                        && !msg.startsWith("Aucune") && !msg.startsWith("Vous êtes");
                if (good) ok(msg); else error(msg);
            }
            case "fdel" -> {
                String id = selected != null && selected.startsWith("x:") ? selected.substring(2) : null;
                String msg = id == null ? "Choisissez d'abord une faille." : com.eldanior.system.failles.FailleManager.remove(id);
                if (msg.contains("supprimée")) { ok(msg); selected = null; } else error(msg);
            }
            case "fnear" -> {
                String msg = com.eldanior.system.failles.FailleManager.removeNearest(ref);
                if (msg.contains("supprimée")) ok(msg); else error(msg);
            }
            case "fclose" -> { ok(com.eldanior.system.failles.FailleManager.closeAll()); selected = null; }
            case "fnat" -> ok(com.eldanior.system.failles.FailleManager.naturalSpawn(true));
            case "fauto" -> {
                boolean on = !com.eldanior.system.failles.FailleManager.naturalEnabled();
                com.eldanior.system.failles.FailleManager.setNatural(on);
                ok(on ? "Apparitions naturelles activées." : "Apparitions naturelles coupées.");
            }
            case "map" -> {
                if ("off".equals(arg)) {
                    com.eldanior.system.territory.systems.KingdomMap.set(false, com.eldanior.system.territory.systems.KingdomMap.scale());
                    ok("Carte du royaume coupée : la carte suit de nouveau le joueur.");
                } else {
                    int i = arg != null && arg.startsWith("on:") ? parse(arg.substring(3)) : 0;
                    float sc = com.eldanior.system.territory.systems.KingdomMap.SCALES[Math.clamp(i, 0, 2)];
                    com.eldanior.system.territory.systems.KingdomMap.set(true, sc);
                    ok("Carte du royaume activée (" + sc + " px par bloc) : elle s'étend autour des joueurs dans les 5 s, du plus proche au plus loin.");
                }
            }
            case "invasion" -> {
                for (PlayerRef pr : Universe.get().getPlayers())
                    pr.sendMessage(Message.raw("Des créatures hostiles envahissent le monde ! Préparez-vous au combat !").color(C_RED));
                ok("Invasion annoncée à tous les joueurs.");
            }
            // ---- joueurs ----
            case "welcome" -> {                                                  // premiere connexion (accueil, 2026-11-25)
                PlayerRef pr = target != null ? online(target) : null;
                Ref<EntityStore> tRef = pr != null ? pr.getReference() : null;
                if (tRef == null || !tRef.isValid()) { error("Joueur introuvable (déconnecté ?)."); return; }
                Store<EntityStore> tStore = tRef.getStore();
                // Sur soi-meme : l'accueil remplace cet ecran ; le fermer d'abord, sinon sa mise a jour vise des elements
                // (#IntCat0...) absents de la fenetre d'accueil et le client se deconnecte (2026-11-25)
                PlayerRef me = store.getComponent(ref, PlayerRef.getComponentType());
                if (me != null && me.getUuid().equals(pr.getUuid())) { closedByAct = true; close(); }
                tStore.getExternalData().getWorld().execute(() -> {             // thread du monde du joueur
                    try {
                        if (tRef.isValid()) com.eldanior.system.accueil.Welcome.resetToFirstConnection(pr, tRef, tStore);
                    } catch (Exception e) { EldaniorLogger.error("[Admin] premiere connexion", e); }
                });
                ok(target + " est remis en première connexion : la fenêtre d'accueil s'ouvre chez lui.");
            }
            case "manage" -> {
                if (target == null || online(target) == null) { error("Joueur introuvable."); return; }
                managed = target;
                selected = null;
                page = 0;
            }
            case "qxp", "qgold", "pk", "xp", "level", "gold", "class", "nob", "church", "titles", "reset" -> {
                if (target == null || online(target) == null) { error("Joueur introuvable (déconnecté ?)."); return; }
                AdminTab.selectPlayerByName(target);
                boolean done = switch (k) {
                    case "qxp" -> AdminTab.handleAddXP(10_000, ref, store);
                    case "qgold" -> AdminTab.handleGiveGold(10_000, ref, store);
                    case "pk" -> AdminTab.handleSetPK(ref, store);
                    case "xp" -> AdminTab.handleAddXP((int) Math.min(Integer.MAX_VALUE, arg != null ? parseLong(arg) : n0), ref, store);
                    case "level" -> {
                        long lv = arg != null ? parseLong(arg) : n0;
                        if (lv < 1 || lv > PlayerLevelData.MAX_LEVEL) { error("Niveau de 1 à " + PlayerLevelData.MAX_LEVEL + "."); yield false; }
                        yield AdminTab.handleSetLevel((int) lv, ref, store);
                    }
                    case "gold" -> AdminTab.handleGiveGold(arg != null ? parseLong(arg) : n0, ref, store);
                    case "class" -> AdminTab.handleSetClass(arg, ref, store);
                    case "nob" -> AdminTab.handleNobilityPromote(arg, ref, store);
                    case "church" -> AdminTab.handleChurchPromote(arg, ref, store);
                    case "titles" -> AdminTab.handleResetTitles(ref, store);
                    case "reset" -> AdminTab.handleResetLevel(ref, store);
                    default -> false;
                };
                if (!done) { if (status == null || C_GREEN.equals(statusColor)) error("Le geste n'a pas pu être fait."); return; }
                ok(switch (k) {
                    case "qxp" -> "+10 000 XP pour " + target + ".";
                    case "qgold" -> "+10 000 or pour " + target + ".";
                    case "pk" -> "Statut PK de " + target + " changé.";
                    case "xp" -> "+" + money(arg != null ? parseLong(arg) : n0) + " XP pour " + target + ".";
                    case "level" -> target + " est niveau " + (arg != null ? arg : n0) + ".";
                    case "gold" -> "+" + money(arg != null ? parseLong(arg) : n0) + " or pour " + target + ".";
                    case "class" -> target + " change de classe.";
                    case "nob" -> target + " : " + rankName(arg) + ".";
                    case "church" -> target + " : " + churchName(arg) + ".";
                    case "titles" -> "Titres de " + target + " remis à zéro.";
                    default -> target + " est remis à zéro.";
                });
            }
            case "cmds" -> {
                if (admin == null || managed == null) return;
                admin.getPlayerRef().sendMessage(Message.raw("Commandes pour " + managed + " :").color(C_GOLD));
                for (String c : commands()) admin.getPlayerRef().sendMessage(Message.raw("/es " + c));
                ok("Commandes écrites dans le chat.");
            }
            // ---- monde ----
            case "zmore" -> { zoneMore = true; pushFields = true; }
            case "zless" -> { zoneMore = false; pushFields = true; }
            case "zprices", "zpvp", "zprotect", "zsale", "zdelete", "ztreasury", "zfamily", "ztax", "ztransfer", "zclearowner" -> zoneAct(k, admin);
            // ---- competences : passifs, metiers, effets de statut ----
            case "skgive", "skpage", "skmax", "skzero", "skdel", "mtgive", "mtdel", "mtpage", "fxapply", "fxremove", "fxcure", "fxclear", "fxlist",
                 "pgive", "pmax", "pdel", "pforget", "rngive", "rnbook", "prngive", "prnbook" -> {
                PlayerRef who = AdminCompetences.target(k.startsWith("p") ? null : t1, managed, ref, store);
                AdminCompetences.Result res = AdminCompetences.act(k, arg, who, n0, t0);
                if (res.ok()) ok(res.message()); else error(res.message());
            }
            case "rnreset" -> { com.eldanior.system.skills.runes.RuneStats.reset(); ok("Compteurs de lancers remis à zéro."); }
            // ---- hologrammes ----
            case "holodel" -> {
                if (selected == null || !selected.startsWith("h:")) return;
                String id = selected.substring(2);
                if (!HologramManager.delete(id)) { error("Hologramme introuvable."); return; }
                selected = null;
                ok("Hologramme " + id + " supprimé.");
            }
            // ---- donnees ----
            case "wipe" -> {
                if (selected == null || !selected.startsWith("w:")) return;
                wipe(selected.substring(2), admin);
            }
            default -> { }
        }
    }

    private void zoneAct(String k, Player admin) {
        ParcelData p = selected != null && selected.startsWith("z:") ? ParcelManager.get(selected.substring(2)) : null;
        if (p == null) { error("Zone introuvable."); return; }
        switch (k) {
            case "zprices" -> {
                p.setPrice(n0);
                p.setRentPrice(n1);
                ok(p.getName() + " : vente " + money(n0) + " or, loyer " + money(n1) + " or pour 7 jours.");
            }
            case "zpvp" -> { p.setPvpEnabled(!p.isPvpEnabled()); ok("Combat " + (p.isPvpEnabled() ? "permis" : "interdit") + " à " + p.getName() + "."); }
            case "zprotect" -> { p.setProtectedByDefault(!p.isProtectedByDefault()); ok("Blocs de " + p.getName() + (p.isProtectedByDefault() ? " protégés." : " ouverts.")); }
            case "zsale" -> { p.setForSale(!p.isForSale()); ok(p.getName() + (p.isForSale() ? " est à vendre." : " n'est plus à vendre.")); }
            case "ztreasury" -> {
                if (n2 <= 0) { error("Indiquez un montant."); return; }
                p.addTreasury(n2);
                ok("+" + money(n2) + " or au trésor de " + p.getName() + ".");
            }
            case "zfamily" -> {
                String id = t0.trim().toLowerCase();
                if (id.isEmpty()) { p.setFamilyId(""); ok(p.getName() + " n'appartient plus à aucune maison."); break; }
                if (FamilyManager.get(id) == null) { error("Maison inconnue : " + id + "."); return; }
                ParcelManager.assignToFamily(p.getId(), id);
                ok(p.getName() + " appartient à la maison " + FamilyManager.get(id).getDisplayName() + ".");
            }
            case "ztax" -> {
                long got = TerritoiresTab.collectTax(p);
                if (got < 0) { error("L'impôt a déjà été levé cette semaine."); return; }
                ok(money(got) + " or d'impôt levés pour " + p.getName() + ".");
            }
            case "ztransfer" -> {
                long got = TerritoiresTab.transferTreasury(p);
                if (got <= 0) { error("Rien à rapatrier (déjà fait cette semaine, ou ni guilde ni maison)."); return; }
                ok(money(got) + " or rapatriés depuis " + p.getName() + ".");
            }
            case "zclearowner" -> { p.clearOwnership(); ok(p.getName() + " n'a plus de maître."); }
            case "zdelete" -> {
                String name = p.getName();
                ParcelManager.deleteParcel(p.getId());
                selected = null;
                if (admin != null && admin.getWorld() != null) com.eldanior.system.territory.systems.ParcelMapSystem.refreshMap(admin.getWorld());
                ok(name + " est supprimée ; ses sous-zones remontent à la zone parente.");
                return;
            }
            default -> { return; }
        }
        ParcelManager.save();
        pushFields = true;
    }

    private void wipe(String id, Player admin) {
        switch (id) {
            case "guilds" -> { GuildManager.init(); deleteDataFile("guilds.properties"); }
            case "families" -> {
                FamilyManager.init();
                com.eldanior.system.titles.nobility.NobilityManager.init();
                deleteDataFile("families.properties");
                deleteDataFile("hierarchies.properties");
            }
            case "parcels" -> resetParcels(admin);
            case "shop" -> { com.eldanior.system.shop.ShopManager.init(); deleteDataFile("shop.properties"); deleteDataFile("blackmarket.properties"); }
            case "classements" -> { com.eldanior.system.classement.ClassementManager.init(); deleteDataFile("classements.properties"); }
            case "all" -> {
                GuildManager.init();
                FamilyManager.init();
                resetParcels(admin);
                try {
                    java.nio.file.Path dir = EldaniorSystem.get().getDataDirectory().resolve("eldanior_data");
                    java.io.File[] files = java.nio.file.Files.exists(dir) ? dir.toFile().listFiles() : null;
                    if (files != null) for (java.io.File f : files) f.delete();
                } catch (Exception e) { EldaniorLogger.error("[Admin] remise a zero", e); }
                ok("Tout est remis à zéro. Redémarrez le serveur pour finir.");
                selected = null;
                return;
            }
            default -> { return; }
        }
        selected = null;
        ok("Remise à zéro faite.");
    }

    private static void resetParcels(Player admin) {
        try {
            java.io.File f = EldaniorSystem.get().getDataDirectory().resolve("parcels.properties").toFile();
            if (f.exists()) f.delete();
        } catch (Exception e) { EldaniorLogger.error("[Admin] zones", e); }
        ParcelManager.init(EldaniorSystem.get().getDataDirectory());
        if (admin != null && admin.getWorld() != null) com.eldanior.system.territory.systems.ParcelMapSystem.refreshMap(admin.getWorld());
    }

    private static void deleteDataFile(String filename) {
        try {
            java.io.File f = EldaniorSystem.get().getDataDirectory().resolve("eldanior_data").resolve(filename).toFile();
            if (f.exists()) f.delete();
        } catch (Exception e) { EldaniorLogger.error("[Admin] " + filename, e); }
    }

    // ==================== OUTILS ====================

    private void ok(String m) { status = m; statusColor = C_GREEN; }

    private void error(String m) { status = m; statusColor = C_RED; }

    /** Geste destructeur : premier clic = arme (6 s), second = fait. */
    private boolean arm(String key) {
        if (armed(key)) { armed = null; return true; }
        armed = key;
        armedAt = System.currentTimeMillis();
        return false;
    }

    private boolean armed(String key) { return key.equals(armed) && System.currentTimeMillis() - armedAt <= ARM_MS; }

    private static String item(String id) { return id != null && Item.getAssetMap().getAsset(id) != null ? id : null; }

    private static String money(long v) {
        return String.format(java.util.Locale.FRANCE, "%,d", v).replace(' ', ' ').replace(' ', ' ');
    }

    private static String plural(int n, String one, String many) { return n + " " + (n > 1 ? many : one); }

    private static String duration(long ms) {
        long h = ms / 3_600_000;
        return h >= 24 ? h / 24 + " j " + h % 24 + " h" : h + " h " + ms % 3_600_000 / 60_000 + " min";
    }

    private static int parse(String s) {
        try { return s != null ? Integer.parseInt(s) : -1; } catch (NumberFormatException e) { return -1; }
    }

    private static long parseLong(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return 0; }
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
