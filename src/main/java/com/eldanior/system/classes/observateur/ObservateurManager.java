package com.eldanior.system.classes.observateur;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.definitions.Observateur;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.ClassType;
import com.eldanior.system.skills.runes.RuneManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registre des Observateur (2026-11-16) : une place par trone (Roi d'Eldanior, Empereur Luo = Patriarche du Clan Luo),
 * donc deux joueurs au plus. La place appartient au trone, pas a la personne : un nouveau souverain herite de l'Observateur de
 * son predecesseur et peut le revoquer. Le changement de classe garde tout (niveau, caracteristiques, runes) ; la classe
 * d'avant est notee pour lui etre rendue a la revocation ou a la renonciation.
 *
 * Fichier : <dataDir>/classes/observateur_domini.json ({"holders": {ROI: {...}}, "pending": {uuid: classe a rendre},
 * "aliases": {uuid: nom d'emprunt}}).
 */
public final class ObservateurManager {

    private ObservateurManager() {}

    /** Les deux trones qui nomment un Observateur. */
    public enum Throne {
        ROI("Roi d'Eldanior", "l'assassin privé du Roi"),
        EMPEREUR("Empereur Luo", "l'Œil et les Oreilles de l'Empereur");

        public final String label, role;
        Throne(String label, String role) { this.label = label; this.role = role; }

        public static Throne parse(String s) {
            if (s == null) return null;
            String t = s.trim().toLowerCase();
            if (t.startsWith("roi") || t.startsWith("king")) return ROI;
            if (t.startsWith("emp") || t.startsWith("luo")) return EMPEREUR;
            return null;
        }
    }

    /** Titulaire d'une place : joueur, nom, classe d'avant (rendue a la revocation). */
    public record Holder(UUID uuid, String name, String previousClassId) {}

    private static final Map<Throne, Holder> HOLDERS = new EnumMap<>(Throne.class);
    /** Revocations prononcees hors ligne : joueur -> classe a lui rendre a sa prochaine connexion. */
    private static final Map<UUID, String> PENDING = new HashMap<>();
    /** Propositions en attente (joueur vise -> trone, heure d'expiration). */
    private static final Map<UUID, Offer> OFFERS = new ConcurrentHashMap<>();
    private static final long OFFER_MS = 120_000;

    record Offer(Throne throne, String sovereign, long expires) {}

    /** Noms d'emprunt (2026-11-16) : Observateur -> nom affiche au-dessus de sa tete a la place du sien (PlayerNameplateSystem). */
    private static final Map<UUID, String> ALIASES = new ConcurrentHashMap<>();
    public static final int ALIAS_MAX = 24;
    /** Executions prononcees hors ligne (2026-11-16) : le personnage sera efface a la prochaine connexion. */
    private static final java.util.Set<UUID> EXECUTIONS = ConcurrentHashMap.newKeySet();

    private static Path file;

    // ===================== NOM D'EMPRUNT =====================

    /** Nom d'emprunt du joueur s'il est Observateur, sinon null. */
    public static String alias(UUID uuid, PlayerLevelData data) {
        return uuid != null && isObservateur(data) ? ALIASES.get(uuid) : null;
    }

    /** Nom d'emprunt enregistre, sans verifier la classe (Cour : l'Observateur peut etre dans un autre monde). */
    public static String rawAlias(UUID uuid) { return uuid == null ? null : ALIASES.get(uuid); }

    /** true si ce joueur porte un nom d'emprunt (Sans Visage, nameplate). */
    public static boolean hasAlias(UUID uuid) { return uuid != null && ALIASES.containsKey(uuid); }

    /** Nettoie un nom propose : lettres, chiffres, espaces, apostrophes, tirets et points ; null s'il est vide ou trop long. */
    public static String cleanAlias(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", " ");
        if (s.isEmpty() || s.length() > ALIAS_MAX || !s.matches("[\\p{L}\\p{N} '\\-.]+")) return null;
        return s;
    }

    /** Pose (alias non null) ou retire (null) le nom d'emprunt. */
    public static synchronized void setAlias(UUID uuid, String alias) {
        if (uuid == null) return;
        if (alias == null) ALIASES.remove(uuid); else ALIASES.put(uuid, alias);
        save();
    }

    // ===================== REGLES =====================

    /** true si le joueur est Observateur : toutes les armes et toutes les runes du mod lui sont permises. */
    public static boolean isObservateur(PlayerLevelData data) {
        return data != null && Observateur.ID.equalsIgnoreCase(data.getPlayerClassId());
    }

    public static boolean isObservateur(ClassModel model) {
        return model != null && model.getType() == ClassType.OBSERVATEUR;
    }

    /** Trone du joueur : Roi (rang ROI) ou Empereur Luo (Patriarche du Clan Luo) ; null sinon. */
    public static Throne throneOf(PlayerLevelData data) {
        if (data == null) return null;
        if (NobilityRank.fromString(data.getNobilityRank()) == NobilityRank.ROI) return Throne.ROI;
        String fam = data.getNobleFamilyId();
        if (data.isPatriarch() && fam != null && "luo".equalsIgnoreCase(FamilyManager.migrateId(fam))) return Throne.EMPEREUR;
        return null;
    }

    public static synchronized Holder holder(Throne t) { return HOLDERS.get(t); }

    public static synchronized Throne throneHeldBy(UUID uuid) {
        for (Map.Entry<Throne, Holder> e : HOLDERS.entrySet()) if (e.getValue().uuid().equals(uuid)) return e.getKey();
        return null;
    }

    // ===================== PROPOSITION =====================

    static void offer(UUID target, Throne throne, String sovereign) {
        OFFERS.put(target, new Offer(throne, sovereign, System.currentTimeMillis() + OFFER_MS));
    }

    /** Proposition encore valable pour ce joueur (consommee), ou null. */
    static Offer takeOffer(UUID target) {
        Offer o = OFFERS.remove(target);
        return o != null && o.expires() >= System.currentTimeMillis() ? o : null;
    }

    static Offer peekOffer(UUID target) {
        Offer o = OFFERS.get(target);
        return o != null && o.expires() >= System.currentTimeMillis() ? o : null;
    }

    static void dropOffer(UUID target) { OFFERS.remove(target); }

    /**
     * Le joueur accepte : il devient Observateur (thread du monde du joueur). Retourne un message d'erreur, ou null si fait.
     */
    static String accept(Ref<EntityStore> ref, Store<EntityStore> store, UUID uuid, String name, Throne throne) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null) return "Données du joueur introuvables.";
        if (isObservateur(data)) return "Vous êtes déjà Observateur.";
        synchronized (ObservateurManager.class) {
            Holder h = HOLDERS.get(throne);
            if (h != null && !h.uuid().equals(uuid)) return "Le " + throne.label + " a déjà son Observateur (" + h.name() + ").";
            HOLDERS.put(throne, new Holder(uuid, name, data.getPlayerClassId()));
            PENDING.remove(uuid);
            save();
        }
        ClassModel model = ClassManager.get(Observateur.ID);
        if (model == null) return "Classe Observateur introuvable.";
        setClass(ref, store, model);
        ObservateurLames.give(ref, store);                         // ses lames (Mumei et Kagero), s'il ne les a pas deja
        EldaniorLogger.info("[Observateur] " + name + " devient " + throne.role + " (" + throne.label + ")");
        return null;
    }

    /** Change la classe (comme /es setclass) : niveau, caracteristiques et objets gardes. */
    static void setClass(Ref<EntityStore> ref, Store<EntityStore> store, ClassModel model) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        if (data == null || model == null) return;
        PlayerLevelData d = (PlayerLevelData) data.clone();
        if (d == null) return;
        d.setPlayerClass(model.getDisplayName());
        d.setPlayerClassId(model.getId());
        RuneManager.purgeDisallowedRunes(ref, store, d);
        store.putComponent(ref, type, d);
        StatCalculator.updatePlayerStats(ref, store, d);
        RuneManager.grantClassRunes(ref, store, d);
    }

    // ===================== REVOCATION =====================

    /**
     * Libere la place du trone. Retourne le titulaire libere (null si la place etait vide). La classe d'avant est rendue
     * par l'appelant si le joueur est en ligne (restore), sinon a sa prochaine connexion (onLogin).
     */
    static synchronized Holder release(Throne throne, boolean offline) {
        Holder h = HOLDERS.remove(throne);
        if (h != null) ALIASES.remove(h.uuid());
        if (h != null && offline) PENDING.put(h.uuid(), h.previousClassId() == null ? "novice" : h.previousClassId());
        if (h != null) save();
        return h;
    }

    /** Rend la classe d'avant a un ancien Observateur en ligne (thread de son monde). */
    static void restore(Ref<EntityStore> ref, Store<EntityStore> store, String previousClassId) {
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (!isObservateur(data)) return;                          // il a deja change de classe
        ClassModel back = ClassManager.get(previousClassId == null || previousClassId.isEmpty() ? "novice" : previousClassId);
        setClass(ref, store, back);
    }

    /**
     * Connexion : revocation prononcee pendant l'absence (classe d'avant rendue) ; Observateur qui n'en a plus la classe
     * (serment martial, /es setclass) : sa place est liberee. Modifie data directement (comme RoyalHouse.sync).
     * Retourne le message a afficher, ou null.
     */
    public static synchronized String onLogin(UUID uuid, PlayerLevelData data) {
        if (uuid == null || data == null) return null;
        String back = PENDING.remove(uuid);
        if (back != null) {
            save();
            if (isObservateur(data)) {
                ClassModel m = ClassManager.get(back);
                if (m != null) { data.setPlayerClass(m.getDisplayName()); data.setPlayerClassId(m.getId()); }
                return "Pendant votre absence, le souverain vous a retiré le titre d'Observateur : vous redevenez "
                        + (m != null ? m.getDisplayName() : "Novice") + ".";
            }
            return null;
        }
        Throne t = throneHeldBy(uuid);
        if (t != null && !isObservateur(data)) { HOLDERS.remove(t); ALIASES.remove(uuid); save(); }   // classe donnee hors registre (admin) : laissee telle quelle
        return null;
    }

    // ===================== EXECUTION =====================

    /**
     * Le souverain fait executer son Observateur (2026-11-16, « afin qu'il soit d'une loyaute sans faille ») : place liberee, puis
     * personnage efface (Leveling.utils.CharacterWipe : niveau 1, tout perdu, point d'apparition), tout de suite s'il est en
     * ligne (execute), sinon a sa prochaine connexion (consumeExecution).
     */
    static synchronized Holder sentence(Throne throne, boolean offline) {
        Holder h = HOLDERS.remove(throne);
        if (h == null) return null;
        ALIASES.remove(h.uuid());
        PENDING.remove(h.uuid());
        if (offline) EXECUTIONS.add(h.uuid());
        save();
        return h;
    }

    /** Efface le personnage (thread du monde du joueur) : CharacterWipe, poche et sac a dos remis a zero. */
    public static void execute(com.hypixel.hytale.server.core.universe.PlayerRef pr, Ref<EntityStore> ref, Store<EntityStore> store,
                               String sovereignLabel) {
        com.hypixel.hytale.server.core.universe.world.World world = store.getExternalData().getWorld();
        com.eldanior.system.poche.PocheDimensionnelle.resetPoche(ref, store);
        if (!com.eldanior.system.Leveling.utils.CharacterWipe.wipe(pr, ref, store, world)) return;
        com.eldanior.system.poche.PocheDimensionnelle.resetBackpack(ref, store);
        com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, "EXÉCUTÉ",
                "Le " + sovereignLabel + " ne pardonne pas la trahison", true);
        pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("Le " + sovereignLabel
                + " vous a fait exécuter : votre personnage est effacé, vous recommencez au niveau 1."));
        EldaniorLogger.info("[Observateur] " + pr.getUsername() + " execute par le " + sovereignLabel);
    }

    /** Connexion : true si une execution a ete prononcee pendant l'absence (l'appelant efface le personnage). */
    public static synchronized boolean consumeExecution(UUID uuid) {
        if (uuid == null || !EXECUTIONS.remove(uuid)) return false;
        save();
        return true;
    }

    // ===================== SAUVEGARDE =====================

    public static synchronized void init(Path dataDir) {
        file = dataDir.resolve("classes").resolve("observateur.json");
        HOLDERS.clear();
        PENDING.clear();
        ALIASES.clear();
        EXECUTIONS.clear();
        if (!Files.exists(file)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.has("holders")) {
                for (var e : root.getAsJsonObject("holders").entrySet()) {
                    Throne t = Throne.parse(e.getKey());
                    JsonObject o = e.getValue().getAsJsonObject();
                    if (t == null || !o.has("uuid")) continue;
                    HOLDERS.put(t, new Holder(UUID.fromString(o.get("uuid").getAsString()),
                            o.has("name") ? o.get("name").getAsString() : "?",
                            o.has("previousClassId") ? o.get("previousClassId").getAsString() : "novice"));
                }
            }
            if (root.has("pending")) {
                for (var e : root.getAsJsonObject("pending").entrySet())
                    PENDING.put(UUID.fromString(e.getKey()), e.getValue().getAsString());
            }
            if (root.has("aliases")) {
                for (var e : root.getAsJsonObject("aliases").entrySet())
                    ALIASES.put(UUID.fromString(e.getKey()), e.getValue().getAsString());
            }
            if (root.has("executions")) {
                for (var e : root.getAsJsonArray("executions")) EXECUTIONS.add(UUID.fromString(e.getAsString()));
            }
            EldaniorLogger.info("[Observateur] " + HOLDERS.size() + " titulaire(s)");
        } catch (Exception e) {
            EldaniorLogger.error("[Observateur] lecture de " + file, e);
        }
    }

    private static void save() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            JsonObject root = new JsonObject();
            JsonObject holders = new JsonObject();
            for (Map.Entry<Throne, Holder> e : HOLDERS.entrySet()) {
                JsonObject o = new JsonObject();
                o.addProperty("uuid", e.getValue().uuid().toString());
                o.addProperty("name", e.getValue().name());
                o.addProperty("previousClassId", e.getValue().previousClassId());
                holders.add(e.getKey().name(), o);
            }
            root.add("holders", holders);
            JsonObject pending = new JsonObject();
            for (Map.Entry<UUID, String> e : PENDING.entrySet()) pending.addProperty(e.getKey().toString(), e.getValue());
            root.add("pending", pending);
            JsonObject aliases = new JsonObject();
            for (Map.Entry<UUID, String> e : ALIASES.entrySet()) aliases.addProperty(e.getKey().toString(), e.getValue());
            root.add("aliases", aliases);
            com.google.gson.JsonArray executions = new com.google.gson.JsonArray();
            for (UUID u : EXECUTIONS) executions.add(u.toString());
            root.add("executions", executions);
            Files.writeString(file, root.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            EldaniorLogger.error("[Observateur] ecriture de " + file, e);
        }
    }
}
