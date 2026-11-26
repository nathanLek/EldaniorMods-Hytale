package com.eldanior.system.accueil;

import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Fenetre d'accueil d'un nouveau joueur (2026-11-25), sur la fenetre a cartes d'Eldanior (CardsPage) : 4 etapes —
 * Bienvenue (le monde du serveur en 6 cartes), Commandes, Classe (5 classes de base, passifs, runes, kit), Arrivee.
 * Pas de race : tout le monde est humain. Elle ne se ferme pas (CantClose) : on en sort par « Se connecter » (Welcome.finish).
 * Apercu admin (/es accueil) : refermable, rien n'est applique.
 */
public class WelcomePage extends CardsPage {

    static final String[] STEP_NAMES = {"Bienvenue", "Commandes", "Classe", "Arrivée"};
    private static final String[] STEP_TINT = {"Or", "Bleu", "Violet", "Or"};

    private final boolean preview;
    private int step = 0, reached = 0;
    private String classId;

    public WelcomePage(@Nonnull PlayerRef playerRef, boolean preview) {
        super(playerRef, preview ? CustomPageLifetime.CanDismiss : CustomPageLifetime.CantClose);
        this.preview = preview;
    }

    // ==================== CONTENU ====================

    private static Card info(String key, String art, String title, String mark, String heading, String tagline, String story,
                             String... notes) {
        return new Card(key, art, title).text(mark, "Bienvenue  ·  le monde d'Eldanior", heading, tagline, story).notes(notes);
    }

    private static final List<Card> WELCOME = List.of(
            info("royaume", "Royaume", "Le Royaume", "I", "Bienvenue à Eldanior", "Un royaume, un empire, mille destins.",
                    "Eldanior est un serveur de rôle et d'aventure. Vous gagnez des niveaux en combattant, vous apprenez des runes, "
                            + "vous rejoignez une guilde ou une maison noble, et vous vous taillez une place dans le royaume.",
                    "Le Roi règne sur le Royaume d'Eldanior ; ses nobles gouvernent villes et domaines.",
                    "Les villes ont leurs lois : couvre-feu, frontières, bannis.",
                    "Les PNJ proposent quêtes, boutiques, banque et enseignements."),
            info("luo", "Dojo", "L'Empire Luo", "II", "L'Empire Luo", "La voie du sabre, à l'est.",
                    "Au-delà des frontières du royaume s'étend l'Empire Luo et ses clans. Ses guerriers suivent la voie martiale : "
                            + "katanas, dojos, maîtres et techniques secrètes transmises de génération en génération.",
                    "Les Sensei mettent les disciples à l'épreuve au Bokken.",
                    "Le Rouleau de la voie martiale ouvre ses propres classes.",
                    "Il faut un laissez-passer pour circuler librement dans l'empire."),
            info("runes", "Runes", "Runes et magie", "III", "La magie des runes", "Vos sorts, vos armes secrètes.",
                    "Les runes sont vos compétences actives : boules de feu, ruées, voiles de fumée, invocations… "
                            + "Chaque classe a les siennes, et bien d'autres attendent d'être découvertes.",
                    "Une rune s'apprend en lisant son manuel de magie.",
                    "Douze écoles, de la rune Commune à la Divine.",
                    "Les manuels de votre classe vous sont remis d'office."),
            info("failles", "Faille", "Failles et donjons", "IV", "Failles et donjons", "Le danger paie.",
                    "Des failles s'ouvrent sur des donjons peuplés de monstres. Plus le rang est haut, plus le butin est rare. "
                            + "Des boss mondiaux menacent parfois tout le royaume.",
                    "Failles classées par rang, à explorer seul ou en groupe.",
                    "Coffres au trésor, butins de monstres, monstres étoilés.",
                    "Boss de donjon et boss mondiaux pour les plus braves."),
            info("guildes", "Guildes", "Guildes et noblesse", "V", "Guildes et noblesse", "On ne règne jamais seul.",
                    "Rejoignez une guilde pour ses quêtes et ses classements, servez une maison noble, gravissez les rangs de "
                            + "l'Église ou de la noblesse jusqu'à gouverner un domaine.",
                    "Guildes : quêtes, missions et classement des guildes.",
                    "Maisons nobles : Marquis, Comtes, Ducs… et le Roi.",
                    "Titres et rangs renforcent vos caractéristiques."),
            info("commerce", "Banque", "Commerce et banque", "VI", "Commerce et banque", "L'or fait tourner le royaume.",
                    "Gardez votre or à la banque, achetez et vendez aux boutiques des PNJ, louez ou achetez une maison, "
                            + "échangez avec les autres joueurs.",
                    "La banque garde votre or et prête aux plus audacieux.",
                    "Boutiques, marché noir, ventes entre joueurs.",
                    "Propriétés et parcelles à acheter ou à louer."));

    private static Card command(String key, String art, String title, String heading, String tagline, String story, String... notes) {
        return new Card(key, art, title).text("/", "Commandes  ·  à taper dans le chat", heading, tagline, story).notes(notes);
    }

    private static final List<Card> COMMANDS = List.of(
            command("system", "Grimoire", "/es system", "/es system", "La commande à retenir.",
                    "Ouvrez le chat (touche Entrée) et tapez /es system : le menu d'Eldanior s'ouvre. Tout y est rassemblé, "
                            + "à toute heure.",
                    "Profil : niveaux, caractéristiques, points à répartir.",
                    "Compétences, runes, titres et quêtes en cours.",
                    "Guilde, famille, groupe, duels, échanges, propriétés.",
                    "Boutique, classements et wiki du serveur."),
            command("social", "Arene", "Groupe et duels", "Jouer ensemble", "Groupe, duels et guilde.",
                    "La plupart de ces gestes se font aussi depuis /es system.",
                    "/es party : créer un groupe, inviter, accepter, quitter.",
                    "/es duel : défier un joueur, accepter ou refuser.",
                    "/es guild : votre guilde (inviter, quitter, recrutement)."),
            command("commerce", "Rumeurs", "Commerce et ville", "Commerce et ville", "Échanger, vendre, s'installer.",
                    "L'or se garde à la banque ; les boutiques des PNJ achètent et vendent.",
                    "/es trade : échanger des objets avec un joueur.",
                    "/es sell : mettre en vente l'objet en main.",
                    "/es parcel : vos parcelles (informations, liste, achat).",
                    "/es familier info : votre familier."));

    private static List<Card> classCards() {
        List<Card> out = new ArrayList<>();
        for (String id : Welcome.BASE_CLASSES) {
            Card c = ClassCards.of(id, true);
            if (c != null) { c.kicker = "Classe de départ  ·  " + c.kicker; out.add(c); }
        }
        return out;
    }

    /** Derniere etape : la classe choisie, son kit, et ce qui attend le joueur. */
    private List<Card> arrivalCards() {
        ClassModel m = classId != null ? ClassManager.get(classId) : null;
        if (m == null) return List.of();
        Card hero = ClassCards.of(m, true);
        hero.text(hero.mark, "Arrivée  ·  prêt à partir", "Prêt à partir", "Humain, " + m.getDisplayName().toLowerCase() + ".",
                "Vous allez apparaître au point de départ du monde avec votre kit et 1 000 or. Explorez, parlez aux habitants "
                        + "et suivez leurs quêtes pour faire vos premiers pas.");
        hero.notes("Niveau 1 : chaque niveau donne des points à répartir dans votre profil.",
                "Les manuels de vos runes de classe arrivent dans votre sac.",
                "La fabrication se fait aux établis : pas de fabrication rapide.",
                "Tapez /es system à tout moment pour ouvrir le menu.");
        hero.passives = List.of();
        hero.runes = List.of();
        Card world = new Card("arr-world", "Royaume", "Eldanior");
        world.text("E", hero.kicker, hero.heading, hero.tagline, hero.story);
        world.notes = hero.notes;
        world.kit = hero.kit;
        world.stats = hero.stats;
        return List.of(hero, world);
    }

    @Override
    protected List<Card> cards() {
        return switch (step) {
            case 0 -> WELCOME;
            case 1 -> COMMANDS;
            case 2 -> classCards();
            default -> arrivalCards();
        };
    }

    // ==================== ETAPES ====================

    @Override protected int step() { return step; }
    @Override protected String stepName(int k) { return STEP_NAMES[k]; }
    @Override protected boolean stepReachable(int k) { return k <= reached; }
    @Override protected boolean stepDone(int k) { return k < reached || (k == 2 && classId != null); }
    @Override protected String tint() { return STEP_TINT[step]; }
    @Override protected boolean chosen(Card c) { return step == 2 && c.key.equals(classId); }
    @Override protected String brandSub() {
        return preview ? "Aperçu de l'accueil  ·  admin" : "Bienvenue, voyageur  ·  Royaume d'Eldanior et Empire Luo";
    }

    @Override protected void onStep(int k) { go(k); }

    private void go(int k) {
        step = k;
        reached = Math.max(reached, k);
        resetFocus();
    }

    // ==================== BOUTONS ====================

    @Override
    protected String mainLabel(Card c) {
        return switch (step) {
            case 2 -> "Choisir : " + c.title;
            case 3 -> "Se connecter";
            default -> "Suivant";
        };
    }

    @Override protected String backLabel() { return step > 0 ? "Retour" : null; }

    @Override
    protected String defaultHint() {
        return switch (step) {
            case 0 -> "Cliquez sur une carte pour la lire ; les flèches montrent les suivantes.";
            case 1 -> "Retenez surtout /es system : tout le reste s'y trouve.";
            case 2 -> classId != null ? "Classe choisie : " + ClassManager.get(classId).getDisplayName() + ". Vous pouvez encore changer d'avis."
                    : "Survolez le kit pour voir chaque objet, puis « Choisir ».";
            default -> preview ? "Aperçu admin : « Se connecter » ferme la fenêtre sans rien appliquer."
                    : "« Se connecter » vous envoie au point d'apparition du monde.";
        };
    }

    @Override
    protected boolean onBack(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (step > 0) go(step - 1);
        return false;
    }

    @Override
    protected boolean onMain(Ref<EntityStore> ref, Store<EntityStore> store, Card c) {
        switch (step) {
            case 0, 1 -> go(step + 1);
            case 2 -> {
                if (!Welcome.BASE_CLASSES.contains(c.key)) return false;
                classId = c.key;
                go(3);
            }
            default -> {
                if (preview) {
                    PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
                    if (pr != null) pr.sendMessage(Message.raw("Aperçu de l'accueil : rien n'a été appliqué."));
                    close();
                    return true;
                }
                String err = Welcome.finish(ref, store, classId);
                if (err != null) { hint = err; return false; }
                close();
                return true;
            }
        }
        return false;
    }
}
