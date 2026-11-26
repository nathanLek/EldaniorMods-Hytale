package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Boss de donjon (2026-11-16, fenetre du poste de PNJ, categorie Hors-la-loi → « Dungeon Boss ») : un monstre pose a un bloc de
 * poste, dans un donjon, qui en devient le boss. Meme principe que les monstres etoiles (MonsterContracts) : agrandi, barre de
 * boss du jeu pour les joueurs proches, embleme flottant au-dessus de la tete (une tete de mort au lieu des etoiles). Mais il
 * n'a pas de contrat : il n'apparait dans AUCUN tableau (prime, quetes), on l'abat pour l'XP et le butin de son espece.
 * Abattu, il revient apres {@link #RESPAWN_MS} (5 min pour les tests ; 2 h prevues). Il apparait quand un joueur arrive a moins de
 * {@link #SPAWN_RANGE} blocs (chunk charge). Son point de laisse reste au poste : il garde sa salle.
 * Fichier : eldanior_data/dungeon_bosses.properties.
 *
 * 2026-11-19 (demande de l'utilisateur) — deux sortes de boss selon l'endroit du poste :
 *  - DANS un donjon (zone DUNGEON) : boss de donjon garde par la porte de boss du meme donjon. Il n'apparait PAS a l'approche :
 *    il apparait quand la porte s'ouvre (cle de boss), reste immobile et invulnerable tant que la porte ne s'est pas refermee seule
 *    (15 s, DoorLockManager), puis combat. Le groupe de celui qui ouvre (ou lui seul) est prevenu. Abattu, la porte s'ouvre sans
 *    cle jusqu'a son retour (sortie du donjon) ; laisse seul 3 min, il se rendort (il reviendra a la prochaine ouverture).
 *  - HORS d'un donjon : boss de zone, comme avant (apparait a l'approche d'un joueur).
 *
 * 2026-11-24 (demande de l'utilisateur) — Mini-Boss : pose au bloc de poste (categorie Monstres), partout (dans un donjon aussi).
 * Il apparait toujours a l'approche, jamais par la porte de boss ; moins grand qu'un boss ({@link #MINI_SCALE}), barre et tete
 * de mort comme un boss, niveau 60 par defaut. Sa force vient de son niveau.
 */
public final class DungeonBosses {

    private DungeonBosses() {}

    /** Delai de reapparition apres sa mort. TEST : 5 min ; a passer a 2 h (2 * 60 * 60_000L) apres les essais. */
    public static final long RESPAWN_MS = 5 * 60_000L;
    static final double SPAWN_RANGE = 64.0;
    /** Porte de boss d'un donjon : refermee seule apres ce delai ; le boss attend immobile jusque-la. */
    public static final long GATE_MS = 15_000L;
    /** Boss de donjon laisse sans joueur proche : il se rendort (retire sans compter comme mort). */
    static final long ASLEEP_MS = 3 * 60_000L;
    /** Taille d'un mini-boss par rapport a celle d'un boss de la meme espece (jamais sous la taille normale). */
    static final float MINI_SCALE = 0.7f;

    /** Boss pose a un poste. mob : UUID du monstre en vie (null : pas encore ou mort) ; deadAt : heure de sa mort (0 : vivant). */
    public static final class Boss {
        final String id, name, world, role, species;
        final Vector3d pos;
        final int level;
        /** Mini-Boss (2026-11-24) : jamais garde par la porte de boss, plus petit. */
        final boolean mini;
        volatile UUID mob;
        volatile long deadAt;
        transient volatile Ref<EntityStore> ref;
        transient volatile boolean spawning, dressed;
        transient final BossBarView bar = new BossBarView();
        /** Boss de donjon : immobile jusqu'a cette heure (porte encore ouverte) ; 0 : libre. */
        transient volatile long frozenUntil;
        /** Joueurs prevenus a l'ouverture (groupe de l'ouvreur) : prevenus aussi du debut du combat. */
        transient volatile List<UUID> party = List.of();
        /** Dernier passage avec un joueur proche (boss de donjon en vie). */
        transient volatile long seenAt;
        /** Porte ouverte pendant que son chunk n'etait pas charge : apparition des que possible. */
        transient volatile boolean wanted;

        Boss(String id, String name, String world, Vector3d pos, String role, String species, int level, boolean mini) {
            this.id = id; this.name = name; this.world = world; this.pos = pos; this.role = role; this.species = species; this.level = level;
            this.mini = mini;
        }

        public boolean isMini() { return mini; }

        public String getName() { return name; }
        public String getSpecies() { return species; }
        public int getLevel() { return level; }
        public Vector3d getPos() { return pos; }
        public String getWorld() { return world; }
        public long getDeadAt() { return deadAt; }

        /** Zone DUNGEON qui contient le poste (la plus petite), null : boss de zone. */
        public com.eldanior.system.territory.ParcelData dungeon() { return dungeonAt(world, pos); }

        public boolean gated() { return !mini && dungeon() != null; }

        public String kind() { return mini ? "Mini-Boss" : gated() ? "Boss de donjon" : "Boss de zone"; }

        /** Taille du modele : celle du boss de l'espece, reduite pour un mini-boss. */
        float scale() {
            float s = MonsterContracts.scaleOf(role);
            return mini ? Math.max(1.0f, s * MINI_SCALE) : s;
        }

        /** « Seigneur Vorag · Trork · Boss de donjon · Lv.1200 · Rang SS » (« Boss de zone » hors d'un donjon). */
        String title() {
            return name + " · " + species + " · " + kind() + " · Lv." + level + MonsterContracts.rankSuffix(level).replace("  ·  ", " · ");
        }

        boolean resting() { return deadAt > 0 && System.currentTimeMillis() - deadAt < RESPAWN_MS; }

        long restLeftMs() { return deadAt > 0 ? Math.max(0, deadAt + RESPAWN_MS - System.currentTimeMillis()) : 0; }
    }

    private static final Map<String, Boss> bosses = new ConcurrentHashMap<>();
    private static int nextId = 1;
    private static Path file;
    private static final Random RNG = new Random();

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("dungeon_bosses.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { watch(); } catch (Exception e) { EldaniorLogger.error("[Boss] surveillance", e); }
        }, 12, 5, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { tick(); } catch (Exception e) { EldaniorLogger.error("[Boss] barre", e); }
        }, 12, 1, TimeUnit.SECONDS);
        EldaniorLogger.info("[Boss] " + bosses.size() + " boss de donjon.");
    }

    public static Collection<Boss> all() { return bosses.values(); }

    /** Boss pose a moins de 10 blocs de ce point, null sinon. */
    public static Boss near(String world, Vector3d pos) {
        for (Boss b : bosses.values()) if (b.world.equals(world) && b.pos.distanceSquared(pos) <= 100) return b;
        return null;
    }

    /** Pose un boss au poste (remplace celui qui y etait). role null : espece au hasard. */
    /** Zone DUNGEON (la plus petite) qui contient ce point, null : hors donjon (boss de zone). */
    public static com.eldanior.system.territory.ParcelData dungeonAt(String world, Vector3d pos) {
        com.eldanior.system.territory.ParcelData best = null;
        long v = Long.MAX_VALUE;
        for (var p : com.eldanior.system.territory.ParcelManager.getByType(com.eldanior.system.territory.ParcelType.DUNGEON)) {
            if (!world.equals(p.getWorld()) || !p.contains(pos.x, pos.y, pos.z)) continue;
            long size = (long) (p.getMaxX() - p.getMinX() + 1) * (p.getMaxZ() - p.getMinZ() + 1) * (p.getMaxY() - p.getMinY() + 1);
            if (size < v) { v = size; best = p; }
        }
        return best;
    }

    public static synchronized Boss add(String name, String world, Vector3d pos, int level, String role) {
        return add(name, world, pos, level, role, false);
    }

    /** mini : Mini-Boss (apparait a l'approche, meme dans un donjon). */
    public static synchronized Boss add(String name, String world, Vector3d pos, int level, String role, boolean mini) {
        removeNear(world, pos);
        List<MonsterContracts.Species> pool = MonsterContracts.bossSpecies();
        MonsterContracts.Species sp = null;
        if (role != null) for (MonsterContracts.Species s : pool) if (s.role().equals(role)) sp = s;
        if (sp == null) sp = pool.get(RNG.nextInt(pool.size()));
        Boss b = new Boss("boss_" + nextId++, name, world, new Vector3d(pos), sp.role(), sp.label(), Math.clamp(level, 1, MonsterContracts.MAX_LEVEL), mini);
        bosses.put(b.id, b);
        save();
        EldaniorLogger.info("[Boss] " + b.name + " (" + b.role + " niv. " + b.level + ") pose a " + (int) pos.x + " " + (int) pos.y + " " + (int) pos.z);
        return b;
    }

    /** Retire les boss a moins de 10 blocs (bloc de poste casse, « Retirer ») : monstre retire, barre cachee. */
    public static synchronized List<Boss> removeNear(String world, Vector3d pos) {
        List<Boss> out = new ArrayList<>();
        for (Boss b : new ArrayList<>(bosses.values())) {
            if (!b.world.equals(world) || b.pos.distanceSquared(pos) > 100) continue;
            bosses.remove(b.id);
            b.bar.hide();
            despawn(b);
            out.add(b);
            EldaniorLogger.info("[Boss] " + b.name + " retire (" + b.id + ")");
        }
        if (!out.isEmpty()) save();
        return out;
    }

    // ===== Porte de boss d'un donjon (2026-11-19) =====

    /** Boss du donjon (zone DUNGEON) qui contient cette porte ; vide si la porte n'est pas dans un donjon a boss. */
    public static List<Boss> gatedBy(String world, org.joml.Vector3i door) {
        List<Boss> out = new ArrayList<>();
        com.eldanior.system.territory.ParcelData dj = null;
        long v = Long.MAX_VALUE;
        for (var p : com.eldanior.system.territory.ParcelManager.getByType(com.eldanior.system.territory.ParcelType.DUNGEON)) {
            if (!world.equals(p.getWorld()) || !p.contains(door.x(), door.y(), door.z())) continue;
            long size = (long) (p.getMaxX() - p.getMinX() + 1) * (p.getMaxZ() - p.getMinZ() + 1) * (p.getMaxY() - p.getMinY() + 1);
            if (size < v) { v = size; dj = p; }
        }
        if (dj == null) return out;
        for (Boss b : bosses.values()) {
            var bd = b.world.equals(world) && !b.mini ? b.dungeon() : null;
            if (bd != null && bd.getId().equals(dj.getId())) out.add(b);
        }
        return out;
    }

    /** Tous ces boss ont ete abattus et ne sont pas encore revenus. */
    public static boolean allResting(List<Boss> gate) {
        if (gate.isEmpty()) return false;
        for (Boss b : gate) if (!b.resting()) return false;
        return true;
    }

    /** Temps avant le retour du premier de ces boss. */
    public static long restLeftMs(List<Boss> gate) {
        long m = Long.MAX_VALUE;
        for (Boss b : gate) m = Math.min(m, b.restLeftMs());
        return m == Long.MAX_VALUE ? 0 : m;
    }

    /**
     * Une cle a ouvert la porte du boss (DoorLockUseEvent, en plein traitement du monde : tout est differe). Les boss du donjon
     * apparaissent a leur poste, immobiles et invulnerables jusqu'a la fermeture de la porte (GATE_MS) ; le groupe de l'ouvreur est
     * prevenu.
     */
    public static void doorOpened(World world, List<Boss> gate, UUID opener, String openerName) {
        List<UUID> group = new ArrayList<>();
        var party = opener != null ? com.eldanior.system.party.PartyManager.getParty(opener) : null;
        if (party != null) group.addAll(party.getMemberUUIDs());
        if (opener != null && !group.contains(opener)) group.add(opener);
        long until = System.currentTimeMillis() + GATE_MS;
        StringBuilder names = new StringBuilder();
        for (Boss b : gate) {
            if (b.resting()) continue;
            if (names.length() > 0) names.append(", ");
            names.append(b.name);
            b.party = List.copyOf(group);
            b.seenAt = System.currentTimeMillis();
            world.execute(() -> {
                try {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    Ref<EntityStore> ref = b.ref != null && b.ref.isValid() ? b.ref : b.mob != null ? world.getEntityStore().getRefFromUUID(b.mob) : null;
                    if (ref != null && ref.isValid()) { b.ref = ref; return; }                // deja reveille : on ne le fige pas en plein combat
                    if (com.eldanior.system.util.WorldBlockUtil.getBlockType(world, (int) Math.floor(b.pos.x), (int) Math.floor(b.pos.y),
                            (int) Math.floor(b.pos.z)) == null) { b.wanted = true; b.frozenUntil = until; return; }   // chunk pas charge
                    b.frozenUntil = until;
                    spawn(b, store);
                } catch (Exception e) {
                    EldaniorLogger.error("[Boss] porte " + b.name, e);
                }
            });
        }
        if (names.length() == 0) return;
        String who = openerName != null ? openerName : "Quelqu'un";
        for (UUID u : group) {
            var pr = Universe.get().getPlayer(u);
            if (pr == null) continue;
            pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("[Donjon] " + who + " a ouvert la porte du boss : " + names
                    + " s'éveille. La porte se referme dans " + GATE_MS / 1000 + " s : le combat commencera alors.").color("#e0574a"));
            com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, "Porte du boss", names + " s'éveille", true);
        }
    }

    /** Fige (ou libere) le boss : immobile et invulnerable tant que la porte est ouverte. */
    private static void freeze(Ref<EntityStore> ref, Store<EntityStore> store, boolean on) {
        if (ref == null || !ref.isValid()) return;
        if (on) {
            store.putComponent(ref, com.hypixel.hytale.server.core.entity.Frozen.getComponentType(), com.hypixel.hytale.server.core.entity.Frozen.get());
            store.putComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.Invulnerable.getComponentType(),
                    com.hypixel.hytale.server.core.modules.entity.component.Invulnerable.INSTANCE);
        } else {
            if (store.getComponent(ref, com.hypixel.hytale.server.core.entity.Frozen.getComponentType()) != null)
                store.removeComponent(ref, com.hypixel.hytale.server.core.entity.Frozen.getComponentType());
            if (store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.Invulnerable.getComponentType()) != null)
                store.removeComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.Invulnerable.getComponentType());
        }
    }

    public static boolean isBoss(UUID uuid) {
        if (uuid == null) return false;
        for (Boss b : bosses.values()) if (uuid.equals(b.mob)) return true;
        return false;
    }

    /** Boss abattu par un joueur (DeathXPSystem) : annonce, compte a rebours de son retour. */
    public static void killed(UUID victim, String killer) {
        if (victim == null) return;
        for (Boss b : bosses.values()) {
            if (!victim.equals(b.mob)) continue;
            b.mob = null;
            b.deadAt = System.currentTimeMillis();
            b.dressed = false;
            b.bar.hide();
            save();
            b.frozenUntil = 0;
            if (b.mini) {                                                              // mini-boss : pas d'annonce a tout le serveur
                var pr = Universe.get().getPlayerByUsername(killer, com.hypixel.hytale.server.core.NameMatching.EXACT_IGNORE_CASE);
                if (pr != null) pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("[Mini-Boss] " + b.name + " vaincu. Il reviendra dans "
                        + RESPAWN_MS / 60_000 + " min.").color("#f2c45e"));
                return;
            }
            BountyBoard.announce("[Boss] " + killer + " a vaincu " + b.name + ", " + b.kind().toLowerCase() + " (" + b.species + " niv. " + b.level + ")."
                    + (b.gated() ? " La porte du boss s'ouvre sans clé jusqu'à son retour." : ""));
            return;
        }
    }

    /**
     * Toutes les 5 s, pour chaque boss pres duquel se trouve un joueur : vivant, il garde son nom et son embleme ; mort, il revient
     * apres RESPAWN_MS. Un boss introuvable (chunk pas encore charge apres un redemarrage) n'est pas declare mort : il est
     * recherche a chaque passage, et ne reapparait que s'il est mort (deadAt) ou n'a jamais apparu.
     */
    private static void watch() {
        long now = System.currentTimeMillis();
        for (Boss b : bosses.values()) {
            if (!b.spawning && b.mob != null && b.deadAt == 0 && b.seenAt > 0 && now - b.seenAt > ASLEEP_MS
                    && !MonsterContracts.playerNear(b.world, b.pos, SPAWN_RANGE) && b.gated()) {   // donjon vide : il se rendort
                b.bar.hide();
                despawn(b);
                b.mob = null; b.ref = null; b.dressed = false; b.frozenUntil = 0;
                save();
                EldaniorLogger.info("[Boss] " + b.name + " se rendort (personne dans le donjon)");
                continue;
            }
            if (b.spawning || !MonsterContracts.playerNear(b.world, b.pos, SPAWN_RANGE)) continue;
            World world = Universe.get().getWorld(b.world);
            if (world == null) continue;
            world.execute(() -> {
                try {
                    Boolean post = com.eldanior.system.guards.NpcPostBlocks.postBlockAt(world, b.pos);
                    if (post != null && !post) {                                                 // bloc de poste disparu
                        EldaniorLogger.info("[Boss] " + b.name + " : bloc de poste disparu, boss retire");
                        removeNear(b.world, b.pos);
                        com.eldanior.system.guards.NpcPostBlocks.forget(world, b.pos);
                        return;
                    }
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    Ref<EntityStore> ref = b.ref;
                    if ((ref == null || !ref.isValid()) && b.mob != null) ref = world.getEntityStore().getRefFromUUID(b.mob);
                    boolean gated = b.gated();
                    if (ref != null && ref.isValid()) {
                        b.ref = ref;
                        keep(b, ref, store);
                        b.seenAt = now;                                                         // un joueur est la
                        return;
                    }
                    if (gated) {                                                                // boss de donjon : seulement par la porte
                        if (b.mob != null && b.deadAt == 0) { b.mob = null; b.dressed = false; save(); }   // disparu : endormi
                        if (b.wanted && !b.resting()) { b.wanted = false; spawn(b, store); }
                        return;
                    }
                    if (b.mob != null && b.deadAt == 0) {                                       // disparu sans tueur connu
                        b.deadAt = now;
                        b.dressed = false;
                        save();
                        EldaniorLogger.info("[Boss] " + b.name + " a disparu, retour dans " + RESPAWN_MS / 1000 + " s");
                        return;
                    }
                    if (b.deadAt > 0 && now - b.deadAt < RESPAWN_MS) return;
                    spawn(b, store);
                } catch (Exception e) {
                    EldaniorLogger.error("[Boss] " + b.name, e);
                }
            });
        }
    }

    /** Chaque seconde : nom (garde la barre de vie du jeu) et barre de boss pour les joueurs proches. */
    private static void tick() {
        for (Boss b : bosses.values()) {
            Ref<EntityStore> ref = b.ref;
            if (ref == null || !ref.isValid() || b.mob == null) { b.bar.hide(); continue; }
            World world = Universe.get().getWorld(b.world);
            if (world == null) continue;
            world.execute(() -> {
                if (!ref.isValid()) { b.bar.hide(); return; }
                Store<EntityStore> store = ref.getStore();
                store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(b.title()));
                b.bar.update(ref, store, world, b.title(), true);
                if (b.frozenUntil > 0 && System.currentTimeMillis() >= b.frozenUntil) {       // la porte s'est refermee : combat
                    b.frozenUntil = 0;
                    freeze(ref, store, false);
                    for (UUID u : b.party) {
                        var pr = Universe.get().getPlayer(u);
                        if (pr == null) continue;
                        pr.sendMessage(com.hypixel.hytale.server.core.Message.raw("[Donjon] La porte s'est refermée : " + b.name + " attaque !").color("#e0574a"));
                        com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, b.name, "Le combat commence", true);
                    }
                }
            });
        }
    }

    private static void keep(Boss b, Ref<EntityStore> ref, Store<EntityStore> store) {
        store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(b.title()));
        if (!b.dressed) { MonsterContracts.dress(ref, store, b.scale(), MonsterContracts.SKULL); b.dressed = true; }
    }

    private static void spawn(Boss b, Store<EntityStore> store) {
        b.spawning = true;
        try {
            var pair = NPCPlugin.get().spawnNPC(store, b.role, null, new Vector3d(b.pos), new Rotation3f(0, RNG.nextFloat() * 360f, 0));
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Boss] boss non cree : " + b.role); return; }
            store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(b.level, b.role, true));
            b.dressed = false;
            keep(b, ref, store);
            if (b.frozenUntil > System.currentTimeMillis()) freeze(ref, store, true);   // boss de donjon : attend la fermeture de la porte
            b.seenAt = System.currentTimeMillis();
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            b.mob = u != null ? u.getUuid() : null;
            b.ref = ref;
            b.deadAt = 0;
            save();
            EldaniorLogger.info("[Boss] " + b.name + " (" + b.role + " niv. " + b.level + ") apparait");
        } finally {
            b.spawning = false;
        }
    }

    private static void despawn(Boss b) {
        World world = b.mob != null || b.ref != null ? Universe.get().getWorld(b.world) : null;
        if (world == null) return;
        UUID mob = b.mob;
        Ref<EntityStore> known = b.ref;
        world.execute(() -> {
            Ref<EntityStore> ref = known != null && known.isValid() ? known : mob != null ? world.getEntityStore().getRefFromUUID(mob) : null;
            if (ref != null && ref.isValid()) world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
        });
    }

    // ===== Persistance =====

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            props.setProperty("nextId", String.valueOf(nextId));
            for (Boss b : bosses.values()) {
                String k = "boss." + b.id + ".";
                props.setProperty(k + "name", b.name);
                props.setProperty(k + "world", b.world);
                props.setProperty(k + "pos", b.pos.x + "," + b.pos.y + "," + b.pos.z);
                props.setProperty(k + "role", b.role);
                props.setProperty(k + "species", b.species);
                props.setProperty(k + "level", String.valueOf(b.level));
                if (b.mini) props.setProperty(k + "mini", "true");
                if (b.mob != null) props.setProperty(k + "mob", b.mob.toString());
                if (b.deadAt > 0) props.setProperty(k + "deadAt", String.valueOf(b.deadAt));
            }
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - boss de donjon");
        } catch (Exception e) {
            EldaniorLogger.error("[Boss] sauvegarde", e);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            nextId = Integer.parseInt(props.getProperty("nextId", "1"));
            for (String key : props.stringPropertyNames()) {
                if (!key.startsWith("boss.") || !key.endsWith(".name")) continue;
                String id = key.substring(5, key.length() - 5), k = "boss." + id + ".";
                String[] c = props.getProperty(k + "pos", "0,0,0").split(",");
                Boss b = new Boss(id, props.getProperty(key), props.getProperty(k + "world", "default"),
                        new Vector3d(Double.parseDouble(c[0]), Double.parseDouble(c[1]), Double.parseDouble(c[2])),
                        MonsterContracts.migrateRole(props.getProperty(k + "role")), props.getProperty(k + "species", "?"),
                        Integer.parseInt(props.getProperty(k + "level", "100")), Boolean.parseBoolean(props.getProperty(k + "mini", "false")));
                String mob = props.getProperty(k + "mob");
                if (mob != null) b.mob = UUID.fromString(mob);
                b.deadAt = Long.parseLong(props.getProperty(k + "deadAt", "0"));
                bosses.put(id, b);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Boss] chargement", e);
        }
    }
}
