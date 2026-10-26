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
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Contrats de monstres du tableau de prime : chaque jour, {@link #PER_DAY} zones de contrat (posees par un admin : /es contrat)
 * recoivent un monstre d'elite nomme ("Grok le Devoreur", espece tiree dans SPECIES), au niveau de la zone x {@link #ELITE}
 * (MobLevelData), deux fois plus grand ({@link #SCALE}, une seule fois a l'apparition : changer le modele en plein combat le
 * fait sauter), avec une aura (effet Eldanior_Contrat_Aura reapplique). Son point de laisse le suit chaque seconde : sans cela
 * le moteur le ramene d'un coup a son point d'apparition quand le combat l'en eloigne (laisse dure). Il apparait
 * quand un joueur arrive a moins de {@link #SPAWN_RANGE} blocs de sa zone. Disparu sans etre reclame (mort sans tueur, retire
 * par un autre systeme) : il ne revient qu'apres {@link #RESPAWN_DELAY_MS}, et jamais sous les yeux d'un joueur (personne a
 * moins de {@link #CLEAR_RANGE} blocs du point). Le premier joueur qui l'abat touche la recompense (DeathXPSystem -> claim),
 * annonce a tous. Contrats et zones : eldanior_data/monster_contracts.properties.
 */
public final class MonsterContracts {

    private MonsterContracts() {}

    static final int PER_DAY = 3;
    static final double ELITE = 1.2, SPAWN_RANGE = 64.0, CLEAR_RANGE = 16.0;
    static final float SCALE = 2.0f;
    static final long REWARD_PER_LEVEL = 30, RESPAWN_DELAY_MS = 60_000;
    private static final String AURA = "Eldanior_Contrat_Aura";

    /** Espece : role vanilla (verifie dans Assets.zip) et nom affiche. */
    private record Species(String role, String label) {}

    private static final List<Species> SPECIES = List.of(
            new Species("Trork_Warrior", "Trork"), new Species("Trork_Mauler", "Trork"), new Species("Trork_Chieftain", "Chef trork"),
            new Species("Goblin_Scrapper", "Gobelin"), new Species("Outlander_Berserker", "Outlander"),
            new Species("Outlander_Brute", "Outlander"), new Species("Skeleton_Burnt_Knight", "Squelette"),
            new Species("Skeleton_Frost_Knight", "Squelette de givre"), new Species("Scarak_Fighter", "Scarak"),
            new Species("Werewolf", "Loup-garou"), new Species("Yeti", "Yéti"), new Species("Bear_Grizzly", "Grizzly"),
            new Species("Golem_Firesteel", "Golem"), new Species("Toad_Rhino", "Crapaud-rhino"));
    private static final String[] NAMES = {"Grok", "Morgash", "Skarn", "Vhul", "Brakka", "Zogg", "Ulgrim", "Kraal", "Thrak",
            "Mordax", "Sliss", "Vorag", "Drogul", "Karnak", "Gorza", "Ruk"};
    private static final String[] EPITHETS = {"le Dévoreur", "l'Éventreur", "le Maudit", "Brise-Crânes", "le Sanguinaire",
            "Croc-de-Fer", "le Rôdeur", "l'Insatiable", "le Fléau", "Mange-Os", "Œil-Rouge", "le Colosse"};

    /** Zone de contrat : point d'apparition et niveau de base. */
    public record Zone(String id, String name, String world, Vector3d pos, int level) {}

    /** Contrat du jour. mob : UUID du monstre apparu (null : pas encore) ; claimedBy : joueur qui l'a abattu. */
    public static final class Contract {
        final String zoneId, role, species, name;
        final int level;
        final long reward;
        volatile UUID mob;
        volatile String claimedBy;
        volatile long claimedAt;
        transient volatile boolean spawning;
        transient volatile Ref<EntityStore> ref;                             // monstre en vie (thread du monde)
        transient volatile long goneAt;                                     // disparu sans etre reclame (0 : non)
        transient volatile boolean enlarged;                                // modele deja agrandi

        Contract(String zoneId, String role, String species, String name, int level, long reward) {
            this.zoneId = zoneId; this.role = role; this.species = species; this.name = name; this.level = level; this.reward = reward;
        }

        public String getName() { return name; }
        public String getSpecies() { return species; }
        public int getLevel() { return level; }
        public long getReward() { return reward; }
        public String getClaimedBy() { return claimedBy; }
        public long getClaimedAt() { return claimedAt; }
        public Zone zone() { return zones.get(zoneId); }
    }

    private static final Map<String, Zone> zones = new ConcurrentHashMap<>();
    private static final List<Contract> today = new CopyOnWriteArrayList<>();
    private static volatile LocalDate day;
    private static int nextZone = 1;
    private static Path file;
    private static final Random RNG = new Random();

    // ===== Cycle de vie =====

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("monster_contracts.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                if (!LocalDate.now().equals(day)) roll();
                watch();
            } catch (Exception e) {
                EldaniorLogger.error("[Boards] contrats de monstres", e);
            }
        }, 10, 5, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { followLeash(); } catch (Exception e) { EldaniorLogger.error("[Boards] laisse des contrats", e); }
        }, 10, 1, TimeUnit.SECONDS);
        EldaniorLogger.info("[Boards] " + zones.size() + " zone(s) de contrat, " + today.size() + " contrat(s) du jour.");
    }

    public static Collection<Zone> getZones() { return zones.values(); }

    public static List<Contract> getContracts() { return today; }

    // ===== Zones (commande admin) =====

    public static Zone addZone(String name, String world, Vector3d pos, int level) {
        Zone z = new Zone("zone_" + nextZone++, name, world, new Vector3d(pos), Math.clamp(level, 1, 1000));
        zones.put(z.id(), z);
        save();
        return z;
    }

    /** Retire la zone la plus proche (10 blocs) ; ses contrats du jour aussi. */
    public static Zone removeNearest(String world, Vector3d pos) {
        Zone best = null;
        double bestD = 100;
        for (Zone z : zones.values())
            if (z.world().equals(world) && z.pos().distanceSquared(pos) <= bestD) { bestD = z.pos().distanceSquared(pos); best = z; }
        if (best == null) return null;
        zones.remove(best.id());
        String id = best.id();
        for (Contract c : today) if (c.zoneId.equals(id)) { despawn(c); today.remove(c); }
        save();
        return best;
    }

    // ===== Contrats du jour =====

    /** Nouveaux contrats : PER_DAY zones au hasard, une espece et un nom au hasard pour chacune. */
    public static synchronized void roll() {
        for (Contract c : today) if (c.claimedBy == null) despawn(c);
        today.clear();
        day = LocalDate.now();
        List<Zone> pool = new ArrayList<>(zones.values());
        Collections.shuffle(pool, RNG);
        for (Zone z : pool.subList(0, Math.min(PER_DAY, pool.size()))) {
            Species s = SPECIES.get(RNG.nextInt(SPECIES.size()));
            int level = (int) Math.round(z.level() * ELITE);
            String name = NAMES[RNG.nextInt(NAMES.length)] + " " + EPITHETS[RNG.nextInt(EPITHETS.length)];
            today.add(new Contract(z.id(), s.role(), s.label(), name, level, Math.round(level * REWARD_PER_LEVEL / 10.0) * 10));
        }
        save();
        EldaniorLogger.info("[Boards] " + today.size() + " contrat(s) de monstre pour le " + day);
    }

    /** Monstre de contrat en vie (systemes de plaques : son nom est gere ici). */
    public static boolean isContractMob(Ref<EntityStore> ref, com.hypixel.hytale.component.ComponentAccessor<EntityStore> store) {
        if (today.isEmpty() || ref == null || !ref.isValid()) return false;
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        return u != null && isContractMob(u.getUuid());
    }

    public static boolean isContractMob(UUID uuid) {
        if (uuid == null) return false;
        for (Contract c : today) if (uuid.equals(c.mob) && c.claimedBy == null) return true;
        return false;
    }

    /**
     * Toutes les 5 s, pour chaque contrat en cours pres duquel se trouve un joueur : le monstre en vie garde son aura, son nom
     * et sa taille ; disparu, il reapparait apres RESPAWN_DELAY_MS, si personne n'est pres du point d'apparition.
     */
    private static void watch() {
        long now = System.currentTimeMillis();
        for (Contract c : today) {
            Zone z = c.zone();
            if (c.claimedBy != null || z == null || c.spawning) continue;
            World world = Universe.get().getWorld(z.world());
            if (world == null || !playerNear(z, SPAWN_RANGE)) continue;
            world.execute(() -> {
                try {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    Ref<EntityStore> ref = c.ref;
                    if ((ref == null || !ref.isValid()) && c.mob != null) ref = world.getEntityStore().getRefFromUUID(c.mob);   // apres un redemarrage
                    if (ref != null && ref.isValid()) { c.ref = ref; c.goneAt = 0; keep(c, ref, store); return; }
                    if (c.mob != null && c.goneAt == 0) {                                                  // vient de disparaitre
                        c.goneAt = now;
                        c.enlarged = false;
                        EldaniorLogger.info("[Boards] contrat : " + c.name + " a disparu sans etre reclame, retour dans " + RESPAWN_DELAY_MS / 1000 + " s");
                        return;
                    }
                    if (c.mob != null && now - c.goneAt < RESPAWN_DELAY_MS) return;
                    if (playerNear(z, CLEAR_RANGE)) return;                                                // pas sous les yeux d'un joueur
                    spawn(c, z, store);
                } catch (Exception e) {
                    EldaniorLogger.error("[Boards] contrat " + c.name, e);
                }
            });
        }
    }

    /** Monstre en vie : aura (l'effet dure 8 s), nom ; taille x2 une seule fois (a l'apparition, ou au rechargement). */
    private static void keep(Contract c, Ref<EntityStore> ref, Store<EntityStore> store) {
        com.eldanior.system.config.Effects.EffectsManager.applyEffect(ref, AURA, store);
        Nameplate np = store.getComponent(ref, Nameplate.getComponentType());
        if (np == null || !nameplate(c).equals(np.getText())) store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(nameplate(c)));
        if (!c.enlarged) { enlarge(ref, store); c.enlarged = true; }
    }

    /** Chaque seconde : le point de laisse de chaque monstre en vie suit sa position (jamais ramene a son point d'apparition). */
    private static void followLeash() {
        for (Contract c : today) {
            Ref<EntityStore> ref = c.ref;
            Zone z = c.zone();
            if (c.claimedBy != null || ref == null || !ref.isValid() || z == null) continue;
            World world = Universe.get().getWorld(z.world());
            if (world == null) continue;
            world.execute(() -> {
                if (!ref.isValid()) return;
                Store<EntityStore> store = ref.getStore();
                NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                if (npc != null && t != null) npc.setLeashPoint(new Vector3d(t.getPosition()));
            });
        }
    }

    /** Modele du monstre a l'echelle SCALE (par rapport a son modele d'origine). */
    private static void enlarge(Ref<EntityStore> ref, Store<EntityStore> store) {
        ModelComponent mc = store.getComponent(ref, ModelComponent.getComponentType());
        if (mc == null || mc.getModel() == null || mc.getModel().getScale() >= SCALE * 0.95f) return;
        ModelAsset asset = ModelAsset.getAssetMap().getAsset(mc.getModel().getModelAssetId());
        if (asset != null) store.putComponent(ref, ModelComponent.getComponentType(), new ModelComponent(Model.createScaledModel(asset, SCALE)));
    }

    /** Un joueur dans le monde de la zone a moins de range blocs du point (positions relevees par PlayerPositionTracker). */
    private static boolean playerNear(Zone z, double range) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            World w = ref != null && ref.isValid() ? ref.getStore().getExternalData().getWorld() : null;
            Vector3d at = w != null && w.getName().equals(z.world())
                          ? com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            if (at != null && at.distanceSquared(z.pos()) <= range * range) return true;
        }
        return false;
    }

    private static void spawn(Contract c, Zone z, Store<EntityStore> store) {
        c.spawning = true;
        try {
            var pair = NPCPlugin.get().spawnNPC(store, c.role, null, new Vector3d(z.pos()), new Rotation3f(0, RNG.nextFloat() * 360f, 0));
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Boards] monstre de contrat non cree : " + c.role); return; }
            store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(c.level, c.role, true));
            keep(c, ref, store);
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            c.mob = u != null ? u.getUuid() : null;
            c.ref = ref;
            c.goneAt = 0;
            save();
            EldaniorLogger.info("[Boards] contrat : " + c.name + " (" + c.role + " niv. " + c.level + ") apparait a " + z.name());
        } finally {
            c.spawning = false;
        }
    }

    private static void despawn(Contract c) {
        Zone z = c.zone();
        World world = z != null && c.mob != null ? Universe.get().getWorld(z.world()) : null;
        if (world == null) return;
        UUID mob = c.mob;
        world.execute(() -> {
            Ref<EntityStore> ref = world.getEntityStore().getRefFromUUID(mob);
            if (ref != null && ref.isValid()) world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
        });
    }

    static String nameplate(Contract c) {
        return c.name + " · " + c.species + " d'élite Lv." + c.level;
    }

    /** Monstre abattu par un joueur (DeathXPSystem) : la recompense si c'est un contrat en cours (0 sinon), annonce. */
    public static long claim(UUID victim, String killer) {
        for (Contract c : today) {
            if (victim == null || !victim.equals(c.mob) || c.claimedBy != null) continue;
            c.claimedBy = killer;
            c.claimedAt = System.currentTimeMillis();
            save();
            Zone z = c.zone();
            BountyBoard.announce("[Contrat] " + killer + " a abattu " + c.name + " (" + c.species + " niv. " + c.level + ")"
                    + (z != null ? " à " + z.name() : "") + " et touche " + BoardScreen.gold(c.reward) + " or.");
            return c.reward;
        }
        return 0;
    }

    // ===== Persistance =====

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            props.setProperty("nextZone", String.valueOf(nextZone));
            if (day != null) props.setProperty("day", day.toString());
            for (Zone z : zones.values()) {
                String k = "zone." + z.id() + ".";
                props.setProperty(k + "name", z.name());
                props.setProperty(k + "world", z.world());
                props.setProperty(k + "pos", z.pos().x + "," + z.pos().y + "," + z.pos().z);
                props.setProperty(k + "level", String.valueOf(z.level()));
            }
            for (int i = 0; i < today.size(); i++) {
                Contract c = today.get(i);
                String k = "contract." + i + ".";
                props.setProperty(k + "zone", c.zoneId);
                props.setProperty(k + "role", c.role);
                props.setProperty(k + "species", c.species);
                props.setProperty(k + "name", c.name);
                props.setProperty(k + "level", String.valueOf(c.level));
                props.setProperty(k + "reward", String.valueOf(c.reward));
                if (c.mob != null) props.setProperty(k + "mob", c.mob.toString());
                if (c.claimedBy != null) {
                    props.setProperty(k + "claimedBy", c.claimedBy);
                    props.setProperty(k + "claimedAt", String.valueOf(c.claimedAt));
                }
            }
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - contrats de monstres");
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] sauvegarde des contrats", e);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            nextZone = Integer.parseInt(props.getProperty("nextZone", "1"));
            String d = props.getProperty("day");
            day = d != null ? LocalDate.parse(d) : null;
            for (String key : props.stringPropertyNames()) {
                if (!key.startsWith("zone.") || !key.endsWith(".name")) continue;
                String id = key.substring(5, key.length() - 5), k = "zone." + id + ".";
                String[] c = props.getProperty(k + "pos", "0,0,0").split(",");
                zones.put(id, new Zone(id, props.getProperty(key), props.getProperty(k + "world", "default"),
                        new Vector3d(Double.parseDouble(c[0]), Double.parseDouble(c[1]), Double.parseDouble(c[2])),
                        Integer.parseInt(props.getProperty(k + "level", "50"))));
            }
            for (int i = 0; props.getProperty("contract." + i + ".zone") != null; i++) {
                String k = "contract." + i + ".";
                Contract c = new Contract(props.getProperty(k + "zone"), props.getProperty(k + "role"), props.getProperty(k + "species"),
                        props.getProperty(k + "name"), Integer.parseInt(props.getProperty(k + "level")), Long.parseLong(props.getProperty(k + "reward")));
                String mob = props.getProperty(k + "mob");
                if (mob != null) c.mob = UUID.fromString(mob);
                c.claimedBy = props.getProperty(k + "claimedBy");
                c.claimedAt = Long.parseLong(props.getProperty(k + "claimedAt", "0"));
                today.add(c);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Boards] chargement des contrats", e);
        }
    }
}
