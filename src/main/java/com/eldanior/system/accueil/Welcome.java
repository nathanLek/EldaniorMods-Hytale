package com.eldanior.system.accueil;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.Invulnerable;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Accueil des nouveaux joueurs (2026-11-25) : a la premiere connexion (fiche neuve, ou remise a zero par le bouton admin
 * « Premiere connexion »), la fenetre WelcomePage s'ouvre et ne se ferme pas avant la fin : presentation du serveur, commandes,
 * choix d'une des 5 classes de base (pas de race : tout le monde est humain), et « Se connecter » qui envoie le joueur au point
 * d'apparition du monde.
 * Pendant ce temps il reste sur place, invisible (effet Elda_InvisTotal) et invulnerable (composant Invulnerable).
 *
 * Toutes les methodes tournent sur le thread du monde du joueur, hors du traitement d'un tick.
 */
public final class Welcome {

    private Welcome() {}

    /** Les 5 classes de base proposees a l'arrivee (plus de Novice), dans l'ordre des cartes. */
    public static final List<String> BASE_CLASSES = List.of("warrior", "mage", "archer", "assassin", "merchant");

    private static final String HIDE_EFFECT = "Elda_InvisTotal";
    /** Delai avant d'ouvrir la fenetre a la connexion : le client finit de charger, la fiche est ecrite. */
    private static final long OPEN_DELAY_S = 3;

    private static ComponentType<EntityStore, PlayerLevelData> type() { return EldaniorSystem.get().getPlayerLevelDataType(); }

    // ==================== OUVERTURE ====================

    /** Connexion (PlayerLoginSystem) : ouvre l'accueil quelques secondes plus tard si le joueur ne l'a pas encore passe. */
    public static void scheduleOnLogin(Ref<EntityStore> ref, World world) {
        EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
            try {
                if (!ref.isValid()) return;
                Store<EntityStore> store = ref.getStore();
                PlayerLevelData d = store.getComponent(ref, type());
                if (d == null || d.isOnboardingDone()) return;
                open(ref, store);
            } catch (Exception e) {
                EldaniorLogger.error("[Accueil] ouverture a la connexion", e);
            }
        }), OPEN_DELAY_S, TimeUnit.SECONDS);
    }

    /** Apercu pour un admin (/es accueil) : fenetre refermable, rien n'est applique, le joueur n'est pas cache. */
    public static void openPreview(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        player.getPageManager().openCustomPage(ref, store, new WelcomePage(pr, true));
    }

    /** Cache le joueur et ouvre la fenetre d'accueil (thread du monde du joueur). */
    public static void open(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return;
        hide(ref, store, true);
        player.getPageManager().openCustomPage(ref, store, new WelcomePage(pr, false));
    }

    /** Invisible et invulnerable pendant l'accueil (on), ou de nouveau visible et vulnerable (off). */
    static void hide(Ref<EntityStore> ref, Store<EntityStore> store, boolean on) {
        var inv = Invulnerable.getComponentType();
        try {
            if (on) {
                store.putComponent(ref, inv, Invulnerable.INSTANCE);
                EffectsManager.applyInfiniteEffect(ref, HIDE_EFFECT, store);
            } else {
                if (store.getComponent(ref, inv) != null) store.removeComponent(ref, inv);
                EffectsManager.removeEffect(ref, HIDE_EFFECT, store);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Accueil] " + (on ? "masquer" : "montrer") + " le joueur", e);
        }
    }

    // ==================== FIN : « SE CONNECTER » ====================

    /**
     * Applique la classe choisie, donne son kit de depart (StarterKits, seulement ici), marque l'accueil comme passe, rend le joueur visible et vulnerable, puis l'envoie au
     * point d'apparition du monde par defaut. Renvoie un message d'erreur, ou null si tout est fait.
     */
    static String finish(Ref<EntityStore> ref, Store<EntityStore> store, String classId) {
        ClassModel model = classId != null && BASE_CLASSES.contains(classId) ? ClassManager.get(classId) : null;
        if (model == null) return "Choisissez d'abord votre classe.";
        PlayerLevelData d = store.getComponent(ref, type());
        if (d == null) return "Votre fiche n'est pas encore chargée, réessayez dans un instant.";

        d.setPlayerClass(model.getDisplayName());
        d.setPlayerClassId(model.getId());
        com.eldanior.system.skills.runes.RuneManager.purgeDisallowedRunes(ref, store, d);
        d.clearSavedEvolutionChoices();
        for (String skill : model.getActiveSkillIds()) {                     // comme ClassChoicePage.apply
            d.learnSkill(skill);
            d.enableSkill(skill);
        }
        d.setOnboardingDone(true);
        StatCalculator.updatePlayerStats(ref, store, d);
        store.putComponent(ref, type(), d);

        hide(ref, store, false);
        StarterKits.give(ref, store, model.getId());                          // kit de la classe : seulement ici (1re connexion)
        toWorldSpawn(ref, store);

        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr != null) {
            NotificationHelper.showEventTitle(pr, "BIENVENUE À ELDANIOR", model.getDisplayName(), true);
            pr.sendMessage(Message.raw("Bienvenue, " + model.getDisplayName().toLowerCase() + " ! Tapez /es system pour ouvrir le menu d'Eldanior.").color("#f2c45e"));
        }
        EldaniorLogger.info("[Accueil] " + (pr != null ? pr.getUsername() : "?") + " : " + model.getId());
        return null;
    }

    /** Point d'apparition du monde par defaut (autre monde : teleportation d'un monde a l'autre). */
    static void toWorldSpawn(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            World here = store.getExternalData().getWorld();
            World main = Universe.get().getDefaultWorld();
            World target = main != null ? main : here;
            Transform[] spawns = target.getWorldConfig().getSpawnProvider().getSpawnPoints();
            if (spawns == null || spawns.length == 0 || spawns[0] == null) return;
            Teleport tp = target == here ? Teleport.createForPlayer(spawns[0]) : Teleport.createForPlayer(target, spawns[0]);
            store.putComponent(ref, Teleport.getComponentType(), tp);
        } catch (Exception e) {
            EldaniorLogger.error("[Accueil] retour au point d'apparition", e);
        }
    }

    // ==================== ADMIN : « PREMIERE CONNEXION » ====================

    /**
     * Bouton admin (2026-11-25) : efface le personnage comme une perte totale (Leveling.utils.CharacterWipe : niveau 1, tout
     * l'inventaire, runes, famille, guilde, titres, quetes ; point d'apparition), vide la poche et le sac a dos, remet 1 000 or,
     * et rouvre l'accueil comme a une toute premiere connexion. Thread du monde du joueur.
     */
    public static boolean resetToFirstConnection(PlayerRef pr, Ref<EntityStore> ref, Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();
        com.eldanior.system.poche.PocheDimensionnelle.resetPoche(ref, store);
        if (!com.eldanior.system.Leveling.utils.CharacterWipe.wipe(pr, ref, store, world)) return false;
        com.eldanior.system.poche.PocheDimensionnelle.resetBackpack(ref, store);
        PlayerLevelData d = store.getComponent(ref, type());
        if (d == null) return false;
        d.setMoney(1000);                                                    // or de depart d'un nouveau joueur
        d.setOnboardingDone(false);
        StatCalculator.updatePlayerStats(ref, store, d);
        store.putComponent(ref, type(), d);
        pr.sendMessage(Message.raw("Un administrateur a remis votre personnage à zéro : vous recommencez comme à votre première connexion."));
        // Ouverture differee : la fenetre qui a declenche le geste (ecran admin) finit son traitement avant d'etre remplacee
        EldaniorLogger.SCHEDULER.schedule(() -> world.execute(() -> {
            try {
                if (!ref.isValid()) return;
                PlayerLevelData now = ref.getStore().getComponent(ref, type());
                if (now != null && !now.isOnboardingDone()) open(ref, ref.getStore());
            } catch (Exception e) {
                EldaniorLogger.error("[Accueil] ouverture apres remise a zero", e);
            }
        }), 1, TimeUnit.SECONDS);
        EldaniorLogger.info("[Accueil] " + pr.getUsername() + " remis en premiere connexion");
        return true;
    }
}
