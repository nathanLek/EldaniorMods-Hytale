package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guards.BountyHunter;
import com.eldanior.system.guards.Criminals;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.awt.Color;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Recherches du tableau de prime : joueurs PK (registre QuestManager.registerBounty / BountyInfo, plus les PK connectes sans
 * prime, que le registre ignore) et criminels PNJ (guards/Criminals).
 * Un criminel abattu quitte la liste jusqu'a sa reapparition.
 *   - le registre est sauvegarde (eldanior_data/bounty_board.properties) et recharge au demarrage : les PK hors ligne restent
 *     affiches apres un redemarrage ;
 *   - toutes les {@link #TRACK_SECONDS} s, chaque joueur connecte : PK (ou avec une prime) ou non, prime a jour, derniere
 *     zone connue (ville, territoire, royaume, sinon terres sauvages et position arrondie) ;
 *   - prime placee sur un joueur hors ligne : en attente, versee a sa prochaine connexion (PlayerLoginSystem).
 */
public final class BountyBoard {

    private BountyBoard() {}

    /** Prime minimale placee, commission (au tresor de la ville du tableau), rachat (multiple de la prime). */
    static final long MIN_BOUNTY = 100;
    static final double COMMISSION = 0.10;
    static final long BUYBACK_FACTOR = 2;
    /** Prime placee a partir de laquelle tout le serveur est prevenu. */
    private static final long ANNOUNCE_FROM = 10_000;
    private static final int TRACK_SECONDS = 10, SAVE_SECONDS = 60;

    /** Recherche du tableau : joueur (player) ou criminel PNJ (criminal : son poste). */
    public record Wanted(UUID player, String criminal, String name, int level, long bounty, boolean online, String zone,
                         long seenAt, long respawnAt) {
        public boolean isCriminal() { return criminal != null; }
        /** Cle unique (selection dans la fenetre). */
        public String key() { return criminal != null ? criminal : player.toString(); }
    }

    /** Joueurs PK (ou avec une prime) connectes, prime 0 comprise. */
    private static final Map<UUID, QuestManager.BountyInfo> online = new ConcurrentHashMap<>();
    private static final Map<UUID, String> zones = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> seen = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> pending = new ConcurrentHashMap<>();
    private static Path file;

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("bounty_board.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { track(); } catch (Exception e) { EldaniorLogger.error("[Boards] suivi des recherches", e); }
        }, TRACK_SECONDS, TRACK_SECONDS, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(BountyBoard::save, SAVE_SECONDS, SAVE_SECONDS, TimeUnit.SECONDS);
        EldaniorLogger.info("[Boards] " + QuestManager.getActiveBounties().size() + " prime(s) au tableau.");
    }

    // ===== Zones =====

    /** Derniere zone connue, null si jamais vu. */
    static String zone(UUID player) { return zones.get(player); }

    /** Dernier releve de sa position (ms), 0 si jamais vu. */
    static long seenAt(UUID player) { return seen.getOrDefault(player, 0L); }

    static boolean isOnline(UUID player) {
        PlayerRef pr = Universe.get().getPlayer(player);
        return pr != null && pr.getReference() != null && pr.getReference().isValid();
    }

    /** Releve chaque joueur connecte (dans le thread de son monde) : PK ou non, prime, zone. */
    private static void track() {
        online.keySet().removeIf(u -> !isOnline(u));
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            if (ref == null || !ref.isValid()) continue;
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            if (world != null) world.execute(() -> observe(pr, ref, store, world));
        }
    }

    /** Un joueur vu (thread de son monde) : recherche s'il est PK ou a une prime ; registre a jour ; zone. */
    static void observe(PlayerRef pr, Ref<EntityStore> ref, Store<EntityStore> store, World world) {
        if (!ref.isValid()) return;
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        UUID uuid = pr.getUuid();
        if (data == null || (!data.isPK() && data.getBounty() <= 0)) { online.remove(uuid); return; }
        online.put(uuid, new QuestManager.BountyInfo(uuid, pr.getUsername(), data.getLevel(), data.getBounty()));
        if (data.getBounty() > 0) QuestManager.registerBounty(uuid, pr.getUsername(), data.getLevel(), data.getBounty());
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        if (t == null) return;
        zones.put(uuid, zoneName(world.getName(), new Vector3d(t.getPosition())));
        seen.put(uuid, System.currentTimeMillis());
    }

    /** Tous les recherches : PK connectes (a jour), PK hors ligne du registre, criminels. Du plus recherche au moins. */
    static List<Wanted> list() {
        Map<UUID, QuestManager.BountyInfo> players = new HashMap<>();
        for (QuestManager.BountyInfo b : QuestManager.getActiveBounties()) players.put(b.uuid, b);
        for (QuestManager.BountyInfo b : online.values()) if (isOnline(b.uuid)) players.put(b.uuid, b);
        List<Wanted> out = new ArrayList<>();
        for (QuestManager.BountyInfo b : players.values()) {
            boolean on = isOnline(b.uuid);
            if (on && !online.containsKey(b.uuid)) continue;                                  // connecte, plus PK ni prime
            out.add(new Wanted(b.uuid, null, b.name, b.level, b.bounty, on, zones.get(b.uuid), seenAt(b.uuid), 0));
        }
        for (Criminals.Wanted c : Criminals.list()) {
            if (!c.alive()) continue;                                                         // abattu : hors liste jusqu'a son retour
            out.add(new Wanted(null, c.postId(), c.name(), c.level(), c.bounty(), c.alive(),
                    "Repaire : " + zoneName(c.world(), c.lair()), 0, 0));
        }
        out.sort(Comparator.comparingLong(Wanted::bounty).reversed());
        return out;
    }

    /** Ville, territoire ou royaume ; sinon la parcelle (donjon, mine...) ; sinon terres sauvages, position a 50 blocs pres. */
    static String zoneName(String world, Vector3d pos) {
        ParcelData town = BountyHunter.treasuryAt(world, pos);
        if (town != null) return town.getName();
        ParcelData parcel = ParcelManager.getParcelAt(world, pos.x, pos.y, pos.z);
        if (parcel != null) return parcel.getName();
        return "Terres sauvages (" + Math.round(pos.x / 50) * 50 + ", " + Math.round(pos.z / 50) * 50 + ")";
    }

    // ===== Primes =====

    /** Ajoute add a la prime d'un recherche (joueur ou criminel) ; false s'il n'est plus recherche. */
    static boolean addBounty(Wanted w, long add) {
        if (w.isCriminal()) return Criminals.addBounty(w.criminal(), add);
        addBounty(new QuestManager.BountyInfo(w.player(), w.name(), w.level(), w.bounty()), add);
        return true;
    }

    /**
     * Ajoute add a la prime d'un joueur : tout de suite s'il est connecte (thread de son monde), sinon a sa prochaine
     * connexion. Le registre est mis a jour sans attendre. Prevenu (lui, et tout le serveur pour une grosse prime).
     */
    static void addBounty(QuestManager.BountyInfo info, long add) {
        QuestManager.registerBounty(info.uuid, info.name, info.level, info.bounty + add);
        PlayerRef pr = Universe.get().getPlayer(info.uuid);
        Ref<EntityStore> ref = pr != null ? pr.getReference() : null;
        World world = ref != null && ref.isValid() ? ref.getStore().getExternalData().getWorld() : null;
        if (world == null) {
            pending.merge(info.uuid, add, Long::sum);
        } else {
            world.execute(() -> {
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> lvlType = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = ref.isValid() ? store.getComponent(ref, lvlType) : null;
                PlayerLevelData copy = data != null ? (PlayerLevelData) data.clone() : null;
                if (copy == null) { pending.merge(info.uuid, add, Long::sum); return; }
                copy.addBounty(add);
                store.putComponent(ref, lvlType, copy);
                online.put(info.uuid, new QuestManager.BountyInfo(info.uuid, pr.getUsername(), copy.getLevel(), copy.getBounty()));
                QuestManager.registerBounty(info.uuid, pr.getUsername(), copy.getLevel(), copy.getBounty());
                NotificationHelper.sendNotification(pr, "<color:red>Une prime de " + BoardScreen.gold(add)
                        + " or a été placée sur votre tête !</color> <color:gray>(total " + BoardScreen.gold(copy.getBounty()) + " or)</color>",
                        NotificationStyle.Danger);
            });
        }
        if (add >= ANNOUNCE_FROM)
            announce("[Prime] Une prime de " + BoardScreen.gold(add) + " or a été placée sur la tête de " + info.name
                    + " (total " + BoardScreen.gold(info.bounty + add) + " or).");
        save();
    }

    /** Primes placees pendant son absence (a ajouter a sa prime a la connexion), retirees de l'attente. */
    public static long collectPending(UUID player) {
        Long amount = pending.remove(player);
        if (amount != null) save();
        return amount != null ? amount : 0;
    }

    static void announce(String text) {
        for (PlayerRef pr : Universe.get().getPlayers()) pr.sendMessage(Message.raw(text).color(Color.ORANGE));
    }

    // ===== Persistance =====

    public static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            for (QuestManager.BountyInfo b : QuestManager.getActiveBounties()) {
                String k = "bounty." + b.uuid + ".";
                props.setProperty(k + "name", b.name);
                props.setProperty(k + "level", String.valueOf(b.level));
                props.setProperty(k + "amount", String.valueOf(b.bounty));
            }
            zones.forEach((u, z) -> props.setProperty("zone." + u, z));
            seen.forEach((u, t) -> props.setProperty("seen." + u, String.valueOf(t)));
            pending.forEach((u, a) -> props.setProperty("pending." + u, String.valueOf(a)));
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - tableau de prime");
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] sauvegarde des primes", e);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            for (String key : props.stringPropertyNames()) {
                String[] parts = key.split("\\.", 3);
                if (parts.length < 2) continue;
                UUID uuid;
                try { uuid = UUID.fromString(parts[1]); } catch (IllegalArgumentException e) { continue; }
                switch (parts[0]) {
                    case "bounty" -> {
                        if (!"amount".equals(parts.length > 2 ? parts[2] : "")) continue;
                        String k = "bounty." + uuid + ".";
                        QuestManager.registerBounty(uuid, props.getProperty(k + "name", "?"),
                                Integer.parseInt(props.getProperty(k + "level", "1")), Long.parseLong(props.getProperty(key)));
                    }
                    case "zone" -> zones.put(uuid, props.getProperty(key));
                    case "seen" -> seen.put(uuid, Long.parseLong(props.getProperty(key)));
                    case "pending" -> pending.put(uuid, Long.parseLong(props.getProperty(key)));
                    default -> { }
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] chargement des primes", e);
        }
    }
}
