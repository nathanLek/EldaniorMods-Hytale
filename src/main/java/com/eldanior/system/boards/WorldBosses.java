package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.PersistenceUtils;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.TintComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.npc.NPCPlugin;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Boss Mondial (2026-11-19, demande de l'utilisateur). Un monstre geant couronne d'or, lance par un admin depuis le panneau
 * d'administration (Tableau › Boss Mondial), jamais par commande ni au hasard.
 *  - Annonce dans le chat a tous : « un Boss Mondial apparaitra dans X min », rappels a 10, 5 et 1 min, 30 et 10 s.
 *  - Lieu tire au hasard dans le royaume, JAMAIS dans une ville (ni a moins de {@link #CITY_MARGIN} blocs de ses murs).
 *  - A l'heure dite : alerte « Boss Mondial apparu » + coordonnees dans le chat, titre a l'ecran, et marqueur sur la carte (comme
 *    les failles des gobelins, ParcelMapSystem). Le monstre lui-meme se materialise quand un joueur arrive a moins de
 *    {@link #SPAWN_RANGE} blocs (chunk charge : pas d'apparition dans le vide, piege des apparitions hors chunk).
 *  - Abattu : annonce du vainqueur, marqueur retire. Pas abattu a la fin de sa duree : il se retire.
 * Un seul Boss Mondial a la fois. Fichier : eldanior_data/world_boss.properties (reprise apres redemarrage).
 *
 * 2026-11-20 (demandes de l'utilisateur) : plusieurs Boss Mondiaux au choix (ou au hasard) ; niveau tire entre
 * {@link #MIN_LEVEL} et {@link #MAX_BASE_LEVEL} ; 1 a 3 COURONNES tirees au hasard, avec le meme bonus que les etoiles des
 * monstres etoiles (niveau x1,2 / x1,5 / x2, MonsterContracts.eliteFor) ; peut aussi etre pose a un bloc de poste (categorie
 * Monstres de NpcPostPage) au lieu d'un lieu tire au hasard. Marqueur avec alerte de boussole (CompassAlertComponent).
 */
public final class WorldBosses {

    private WorldBosses() {}

    /**
     * Sorte de Boss Mondial : role, nom, espece, taille, version des couronnes (Couronnes_N_crown, gen_emblemes_etoiles.py, meme
     * cle que CROWN_BOSSES : hauteur et echelle propres a chaque boss).
     */
    public record Kind(String id, String role, String name, String species, float scale, String crown) {
        /** Taille affichee : « 3 » ou « 2,5 » (l'ancien (int) affichait ×1 pour Vel'Kazhar x1,5). */
        public String scaleLabel() {
            return scale == Math.rint(scale) ? String.valueOf((int) scale) : String.valueOf(scale).replace('.', ',');
        }
    }

    /** Roi de la Mort : Wraith blanc x5 a flammes violettes (gen_roi_mort.py) ; Ursaroth : Ours du Vide noir x3 a flammes rouges (gen_ours_braises.py) ; puis gen_boss_mondiaux.py. */
    public static final List<Kind> KINDS = List.of(
            new Kind("roi_mort", "Eldanior_Roi_De_La_Mort", "Le Roi de la Mort", "Spectre", 5.0f, "Roi"),
            new Kind("ursaroth", "Eldanior_Ursaroth", "Ursaroth, l'Ours des Braises Noires", "Ours", 3.0f, "Ours"),
            // 2026-11-20 (gen_boss_mondiaux.py) : dragon noir et or (Dragon_Frost), tyran Rex_Cave charbon et lave, golem de fer ardent
            new Kind("vel_kazhar", "Eldanior_Vel_Kazhar", "Vel'Kazhar, le Dragon d'Ébène et d'Or", "Dragon", 1.5f, "Dragon"),
            new Kind("gorgoth", "Eldanior_Gorgoth", "Gorgoth, le Tyran des Cendres", "Rex", 2.0f, "Rex"),
            new Kind("ignarok", "Eldanior_Ignarok", "Ignarok, le Titan de Fer Ardent", "Golem", 3.0f, "Golem"),
            // 2026-11-22 (gen_ryu_celeste.py) : Ryu japonais noir et or, modele propre (cornes d'or, criniere, moustaches), VOLANT
            // (gabarit volant du jeu), ~60 blocs de long a x6 ; manoeuvres animees par RyuFlight
            new Kind("kokuo_ryu", RyuFlight.ROLE, "Kokuō-Ryū, le Dragon Céleste Impérial", "Dragon", 6.0f, "Ryu"));

    /** Niveau de base tire au hasard (avant le bonus des couronnes). */
    public static final int MIN_LEVEL = 500, MAX_BASE_LEVEL = 999;
    /** Valeur « au hasard » pour le boss. */
    public static final String RANDOM = "hasard";

    static final double SPAWN_RANGE = 72.0, CITY_MARGIN = 48.0;
    private static final long[] REMINDERS = {10 * 60_000L, 5 * 60_000L, 60_000L, 30_000L, 10_000L};
    private static final String C_ALERT = "#f2c45e", C_RED = "#e0574a";
    static final String MARKER_ICON = "Eldanior_BossMondial.png", MARKER_COMPASS = "Eldanior_BossMondial_Boussole.png";

    /** Boss Mondial en cours (annonce, apparu, ou en vie). */
    public static final class Event {
        final Kind kind;
        final String world, place;
        final double x, z;
        final int level, crowns;
        final long spawnAt, endAt;
        volatile double y = Double.NaN;
        volatile UUID mob;
        volatile boolean appeared, dressed, spawning;
        volatile int reminder, misses;
        transient volatile Ref<EntityStore> ref;
        transient final BossBarView bar = new BossBarView();

        Event(Kind kind, String world, String place, double x, double z, int level, int crowns, long spawnAt, long endAt) {
            this.kind = kind; this.world = world; this.place = place; this.x = x; this.z = z; this.level = level;
            this.crowns = Math.clamp(crowns, 1, 3); this.spawnAt = spawnAt; this.endAt = endAt;
        }

        public Kind getKind() { return kind; }
        public boolean isAppeared() { return appeared; }
        public long getSpawnAt() { return spawnAt; }
        public long getEndAt() { return endAt; }
        public int getLevel() { return level; }
        public int getCrowns() { return crowns; }
        public String coords() { return "X " + (int) Math.floor(x) + ", Z " + (int) Math.floor(z) + (Double.isNaN(y) ? "" : ", Y " + (int) y); }
        public String getPlace() { return place; }
        public boolean isAlive() { return mob != null; }

        String title() {
            return kind.name + " (" + crowns(crowns) + ") · Boss Mondial · Lv." + level + MonsterContracts.rankSuffix(level).replace("  ·  ", " · ");
        }

        /** 1 a 3 couronnes d'or au-dessus de la tete, a la hauteur propre a ce boss. */
        MonsterContracts.Emblem emblem() {
            return new MonsterContracts.Emblem("NPC/Eldanior/Emblemes/Couronnes_" + crowns + "_" + kind.crown + ".blockymodel",
                    "NPC/Eldanior/Emblemes/Couronne_Texture.png", new int[]{1});
        }
    }

    /** « 2 couronnes » (le jeu n'affiche pas les symboles, comme pour les etoiles). */
    public static String crowns(int n) {
        return n + (n > 1 ? " couronnes" : " couronne");
    }

    private static volatile Event current;
    private static Path file;
    private static final Random RNG = new Random();

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("world_boss.properties");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { tick(); } catch (Exception e) { EldaniorLogger.error("[Boss Mondial] boucle", e); }
        }, 15, 1, TimeUnit.SECONDS);
        // Manoeuvres de vol de Kokuō-Ryū (virages, montee, pique, tonneau, approche du sol, clignement) : 4 fois par seconde
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { flightTick(); } catch (Exception e) { EldaniorLogger.error("[Boss Mondial] vol", e); }
        }, 16_000, 250, TimeUnit.MILLISECONDS);
    }

    private static void flightTick() {
        Event e = current;
        if (e == null || !RyuFlight.ROLE.equals(e.kind.role)) return;
        Ref<EntityStore> ref = e.ref;
        if (ref == null || !ref.isValid()) return;
        World world = Universe.get().getWorld(e.world);
        if (world == null) return;
        world.execute(() -> {
            try { RyuFlight.tick(ref, world.getEntityStore().getStore(), world); }
            catch (Exception ex) { EldaniorLogger.error("[Boss Mondial] vol de " + e.kind.name, ex); }
        });
    }

    public static Event current() { return current; }

    public static Kind kind(String id) {
        if (id == null || RANDOM.equals(id) || id.isBlank()) return KINDS.get(RNG.nextInt(KINDS.size()));
        for (Kind k : KINDS) if (k.id.equals(id)) return k;
        return null;
    }

    public static boolean isWorldBoss(UUID uuid) {
        Event e = current;
        return uuid != null && e != null && uuid.equals(e.mob);
    }

    // ==================== LANCEMENT (panneau d'administration) ====================

    /** Lance un Boss Mondial dans le royaume du monde world (lieu au hasard) ; renvoie le message pour l'admin. */
    public static String start(String kindId, World world, int delayMin, int level, int durationMin) {
        return start(kindId, world, delayMin, level, durationMin, 0, null);
    }

    /**
     * Lance un Boss Mondial ; kindId « hasard » ou null : boss tire au hasard ; level 0 : niveau de base tire entre MIN_LEVEL et
     * MAX_BASE_LEVEL ; crowns 0 : 1 a 3 couronnes au hasard (bonus de niveau des etoiles) ; at non null : a ce point (bloc de
     * poste), sinon un lieu au hasard du royaume hors des villes.
     */
    public static synchronized String start(String kindId, World world, int delayMin, int level, int durationMin, int crowns, Vector3d at) {
        if (current != null) return "Un Boss Mondial est déjà en cours : " + current.kind.name + ". Retirez-le d'abord.";
        Kind kind = kind(kindId);
        if (kind == null) return "Boss Mondial inconnu.";
        ParcelData realm = kingdom(world.getName());
        if (realm == null && at == null) return "Aucun royaume dans ce monde : posez d'abord une borne de royaume.";
        double[] spot = at != null ? new double[]{Math.floor(at.x) + 0.5, Math.floor(at.z) + 0.5} : spot(realm);
        if (spot == null) return "Aucun lieu libre trouvé dans " + realm.getName() + " (hors des villes). Réessayez.";
        delayMin = Math.clamp(delayMin, 0, 120);
        durationMin = Math.clamp(durationMin <= 0 ? 60 : durationMin, 10, 24 * 60);
        crowns = crowns >= 1 && crowns <= 3 ? crowns : 1 + RNG.nextInt(3);
        int base = level > 0 ? level : MIN_LEVEL + RNG.nextInt(MAX_BASE_LEVEL - MIN_LEVEL + 1);
        level = Math.clamp(Math.round(base * MonsterContracts.eliteFor(crowns)), 1, MonsterContracts.MAX_LEVEL);
        long now = System.currentTimeMillis();
        long spawnAt = now + delayMin * 60_000L;
        String place = realm != null ? placeName(world.getName(), spot[0], spot[1], realm) : "poste de boss";
        Event e = new Event(kind, world.getName(), place, spot[0], spot[1], level, crowns, spawnAt, spawnAt + durationMin * 60_000L);
        for (int i = 0; i < REMINDERS.length; i++) if (REMINDERS[i] >= delayMin * 60_000L) e.reminder = i + 1;   // rappels deja passes
        current = e;
        save();
        String realmName = realm != null ? realm.getName() : world.getName();
        broadcast(delayMin > 0 ? "Un Boss Mondial approche : " + kind.name + " (" + crowns(crowns) + ") apparaîtra quelque part dans le royaume de " + realmName
                + " dans " + duration(delayMin * 60_000L) + " ! Préparez-vous." : kind.name + " s'éveille dans le royaume !", C_ALERT);
        for (PlayerRef pr : Universe.get().getPlayers())
            com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, "Boss Mondial", kind.name + (delayMin > 0 ? " dans " + duration(delayMin * 60_000L) : ""), true);
        EldaniorLogger.info("[Boss Mondial] " + kind.name + " annonce, " + e.coords() + " (" + e.place + "), dans " + delayMin + " min");
        return kind.name + " annoncé : il apparaîtra dans " + duration(delayMin * 60_000L) + " vers " + e.coords() + " (" + e.place
                + "), " + crowns(crowns) + ", niveau " + level + " (base " + base + " × " + MonsterContracts.eliteFor(crowns) + "), "
                + durationMin + " min pour l'abattre.";
    }

    /** Retire le Boss Mondial en cours (panneau d'administration). */
    public static synchronized String stop() {
        Event e = current;
        if (e == null) return "Aucun Boss Mondial en cours.";
        end(e, null);
        broadcast(e.kind.name + " a disparu : le Boss Mondial est annulé.", C_ALERT);
        return e.kind.name + " retiré.";
    }

    /** Boss Mondial abattu par un joueur (DeathXPSystem). */
    public static void killed(UUID victim, String killer) {
        Event e = current;
        if (e == null || victim == null || !victim.equals(e.mob)) return;
        end(e, null);
        broadcast(killer + " a vaincu " + e.kind.name + ", Boss Mondial de niveau " + e.level + " ! Gloire au vainqueur.", "#00e7b6");
        for (PlayerRef pr : Universe.get().getPlayers())
            com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, e.kind.name + " est tombé", "Vainqueur : " + killer, true);
    }

    private static synchronized void end(Event e, Boolean ignored) {
        if (current != e) return;
        current = null;
        e.bar.hide();
        UUID mob = e.mob;
        Ref<EntityStore> known = e.ref;
        e.mob = null;
        World world = Universe.get().getWorld(e.world);
        if (world != null && (mob != null || known != null)) world.execute(() -> {
            Ref<EntityStore> ref = known != null && known.isValid() ? known : mob != null ? world.getEntityStore().getRefFromUUID(mob) : null;
            if (ref != null && ref.isValid()) world.getEntityStore().getStore().removeEntity(ref, RemoveReason.REMOVE);
        });
        save();
    }

    // ==================== LIEU ====================

    /** Le plus grand royaume du monde. */
    static ParcelData kingdom(String world) {
        ParcelData best = null;
        long v = -1;
        for (ParcelData p : ParcelManager.getByType(ParcelType.KINGDOM)) {
            if (!world.equals(p.getWorld())) continue;
            long size = (long) (p.getMaxX() - p.getMinX() + 1) * (p.getMaxZ() - p.getMinZ() + 1);
            if (size > v) { v = size; best = p; }
        }
        return best;
    }

    /** Point au hasard dans le royaume, hors de toute ville (et de ses abords). */
    private static double[] spot(ParcelData realm) {
        int margin = 32;
        for (int tries = 0; tries < 400; tries++) {
            double x = realm.getMinX() + margin + RNG.nextDouble() * Math.max(1, realm.getMaxX() - realm.getMinX() - 2 * margin);
            double z = realm.getMinZ() + margin + RNG.nextDouble() * Math.max(1, realm.getMaxZ() - realm.getMinZ() - 2 * margin);
            if (!realm.contains(x, (realm.getMinY() + realm.getMaxY()) / 2.0, z)) continue;
            if (nearCity(realm.getWorld(), x, z)) continue;
            return new double[]{Math.floor(x) + 0.5, Math.floor(z) + 0.5};
        }
        return null;
    }

    static boolean nearCity(String world, double x, double z) {
        for (ParcelData c : ParcelManager.getByType(ParcelType.CITY)) {
            if (!world.equals(c.getWorld())) continue;
            double dx = Math.max(Math.max(c.getMinX() - x, 0), x - (c.getMaxX() + 1)), dz = Math.max(Math.max(c.getMinZ() - z, 0), z - (c.getMaxZ() + 1));
            if (Math.hypot(dx, dz) <= CITY_MARGIN) return true;
        }
        return false;
    }

    /** « territoire de X, Marquisat Y » : la plus petite zone nommee qui contient le point (hors royaume). */
    private static String placeName(String world, double x, double z, ParcelData realm) {
        ParcelData best = null;
        long v = Long.MAX_VALUE;
        for (ParcelData p : ParcelManager.getAll()) {
            if (!world.equals(p.getWorld()) || p.getType() == ParcelType.KINGDOM || !p.contains(x, (p.getMinY() + p.getMaxY()) / 2.0, z)) continue;
            if (p.getType() != ParcelType.GRAND_TERRITORY && p.getType() != ParcelType.TERRITORY && p.getType() != ParcelType.FOREST) continue;
            long size = (long) (p.getMaxX() - p.getMinX() + 1) * (p.getMaxZ() - p.getMinZ() + 1);
            if (size < v) { v = size; best = p; }
        }
        return best != null ? best.getType().getLabel().toLowerCase() + " " + best.getName().replace('_', ' ') : "terres sauvages de " + realm.getName();
    }

    // ==================== DEROULEMENT ====================

    private static void tick() {
        Event e = current;
        if (e == null) return;
        long now = System.currentTimeMillis();
        if (!e.appeared) {
            long left = e.spawnAt - now;
            if (left > 0) {
                if (e.reminder < REMINDERS.length && left <= REMINDERS[e.reminder]) {
                    broadcast(e.kind.name + " apparaîtra dans " + duration(REMINDERS[e.reminder]) + " quelque part dans le royaume !", C_ALERT);
                    e.reminder++;
                    while (e.reminder < REMINDERS.length && left <= REMINDERS[e.reminder]) e.reminder++;
                }
                return;
            }
            e.appeared = true;
            save();
            broadcast("ALERTE : Boss Mondial apparu ! " + e.kind.name + " (niveau " + e.level + ") se dresse en " + e.coords() + ", "
                    + e.place + ". Il est marqué sur la carte. Il reste " + duration(e.endAt - now) + " pour l'abattre.", C_RED);
            for (PlayerRef pr : Universe.get().getPlayers())
                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(pr, "Boss Mondial apparu", e.kind.name + " · " + e.coords(), true);
            EldaniorLogger.info("[Boss Mondial] " + e.kind.name + " apparu en " + e.coords());
        }
        if (now >= e.endAt) {
            end(e, null);
            broadcast(e.kind.name + " n'a pas été vaincu : il se retire dans les ombres.", C_ALERT);
            return;
        }
        World world = Universe.get().getWorld(e.world);
        if (world == null) return;
        world.execute(() -> {
            try { keep(e, world); } catch (Exception ex) { EldaniorLogger.error("[Boss Mondial] " + e.kind.name, ex); }
        });
    }

    /** Thread du monde : nom, couronne, barre de boss ; apparition quand un joueur approche. */
    private static void keep(Event e, World world) {
        if (current != e) return;
        Store<EntityStore> store = world.getEntityStore().getStore();
        Ref<EntityStore> ref = e.ref;
        if ((ref == null || !ref.isValid()) && e.mob != null) ref = world.getEntityStore().getRefFromUUID(e.mob);
        if (ref != null && ref.isValid()) {
            e.ref = ref;
            e.misses = 0;
            store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(e.title()));
            if (!e.dressed) { MonsterContracts.dress(ref, store, e.kind.scale, e.emblem()); e.dressed = true; }
            e.bar.update(ref, store, world, e.title(), true);
            return;
        }
        e.bar.hide();
        e.dressed = false;
        if (e.spawning || !playerNear(e, world)) return;                    // personne : il attend (chunk peut-etre decharge)
        if (WorldBlockUtil.getBlockType(world, (int) Math.floor(e.x), 64, (int) Math.floor(e.z)) == null) return;   // chunk pas encore charge
        if (e.mob != null) {                                                  // perdu (chunk recharge sans lui) : on le refait
            if (++e.misses < 5) return;                                       // le temps que ses entites se chargent
            e.misses = 0;
            EldaniorLogger.warn("[Boss Mondial] " + e.kind.name + " introuvable, nouvelle apparition");
            e.mob = null;
        }
        spawn(e, world, store);
    }

    private static boolean playerNear(Event e, World world) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r == null || !r.isValid() || r.getStore().getExternalData().getWorld() != world) continue;
            Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at != null && Math.hypot(at.x - e.x, at.z - e.z) <= SPAWN_RANGE) return true;
        }
        return false;
    }

    private static void spawn(Event e, World world, Store<EntityStore> store) {
        e.spawning = true;
        try {
            int bx = (int) Math.floor(e.x), bz = (int) Math.floor(e.z);
            Integer ground = null;
            for (int y = 318; y > 1; y--) {
                BlockType t = WorldBlockUtil.getBlockType(world, bx, y, bz);
                if (t == null) return;
                if (t == BlockType.EMPTY || "Empty".equals(t.getId())) continue;
                String id = t.getId();
                if (id.contains("Leaves") || id.startsWith("Plant_")) continue;     // au sol, pas dans les arbres
                ground = y;
                break;
            }
            if (ground == null) return;
            e.y = ground + 1.0;
            var pair = NPCPlugin.get().spawnNPC(store, e.kind.role, null, new Vector3d(e.x, e.y, e.z), new Rotation3f(0, RNG.nextFloat() * 360f, 0));
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Boss Mondial] apparition impossible : " + e.kind.role); return; }
            store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(e.level, e.kind.role, true));
            store.putComponent(ref, Nameplate.getComponentType(), new Nameplate(e.title()));
            MonsterContracts.dress(ref, store, e.kind.scale, e.emblem());
            e.dressed = true;
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            e.mob = u != null ? u.getUuid() : null;
            e.ref = ref;
            save();
            EldaniorLogger.info("[Boss Mondial] " + e.kind.name + " materialise en " + e.coords());
        } finally {
            e.spawning = false;
        }
    }

    // ==================== CARTE ====================

    /** Marqueur du Boss Mondial apparu (null sinon), comme celui des failles des gobelins. */
    public static MapMarker marker(String world) {
        Event e = current;
        if (e == null || !e.appeared || !e.world.equals(world)) return null;
        try {
            MapMarkerBuilder b = new MapMarkerBuilder("eldanior_boss_mondial", MARKER_ICON,
                    new Transform(new Vector3d(e.x, Double.isNaN(e.y) ? 100 : e.y, e.z), new Rotation3f(0, 0, 0)));
            b.withCustomName("Boss Mondial : " + e.kind.name + " (" + crowns(e.crowns) + ", niv. " + e.level + ")");
            b.withCompassImage(MARKER_COMPASS);
            b.withIconSize(com.hypixel.hytale.protocol.packets.worldmap.MapMarkerIconSize.Major);
            b.withComponent(new TintComponent(new Color((byte) 0xFF, (byte) 0xD7, (byte) 0x40)));
            // 2026-11-20 (« on ne voit pas le marqueur sur la boussole ») : alerte de boussole du jeu (MapMarkerAlert), qui met le
            // marqueur en avant des qu'il passe dans le champ de la boussole (30 s, attente 30 s) ; renvoyee a chaque nouveau joueur.
            b.withComponent(new com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerAlert(MARKER_COMPASS, 30f, 30f, null).toComponent());
            return b.build();
        } catch (Exception ex) {
            EldaniorLogger.error("[Boss Mondial] marqueur", ex);
            return null;
        }
    }

    // ==================== OUTILS ====================

    private static void broadcast(String text, String color) {
        for (PlayerRef pr : Universe.get().getPlayers()) pr.sendMessage(Message.raw("[Boss Mondial] " + text).color(color));
    }

    public static String duration(long ms) {
        long s = Math.max(0, ms / 1000);
        if (s < 60) return s + " s";
        long m = s / 60;
        if (m < 60) return m + " min" + (s % 60 > 0 && m < 5 ? " " + s % 60 + " s" : "");
        return m / 60 + " h" + (m % 60 > 0 ? " " + m % 60 + " min" : "");
    }

    private static synchronized void save() {
        if (file == null) return;
        try {
            if (!Files.exists(file.getParent())) Files.createDirectories(file.getParent());
            Properties props = new Properties();
            props.setProperty("_version", "1");
            Event e = current;
            if (e != null) {
                props.setProperty("kind", e.kind.id);
                props.setProperty("world", e.world);
                props.setProperty("place", e.place);
                props.setProperty("x", String.valueOf(e.x));
                props.setProperty("z", String.valueOf(e.z));
                props.setProperty("level", String.valueOf(e.level));
                props.setProperty("crowns", String.valueOf(e.crowns));
                props.setProperty("spawnAt", String.valueOf(e.spawnAt));
                props.setProperty("endAt", String.valueOf(e.endAt));
                props.setProperty("appeared", String.valueOf(e.appeared));
                if (e.mob != null) props.setProperty("mob", e.mob.toString());
            }
            PersistenceUtils.writeAtomicWithBackup(file, props, "Eldanior - Boss Mondial");
        } catch (Exception ex) {
            EldaniorLogger.error("[Boss Mondial] sauvegarde", ex);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(file)) { props.load(in); }
            String kid = props.getProperty("kind", "");
            Kind kind = kid.isBlank() ? null : kind(kid);
            if (kind == null) return;
            long endAt = Long.parseLong(props.getProperty("endAt", "0"));
            if (endAt <= System.currentTimeMillis()) return;                  // fini pendant l'arret
            Event e = new Event(kind, props.getProperty("world", "default"), props.getProperty("place", "?"),
                    Double.parseDouble(props.getProperty("x", "0")), Double.parseDouble(props.getProperty("z", "0")),
                    Integer.parseInt(props.getProperty("level", String.valueOf(MIN_LEVEL))),
                    Integer.parseInt(props.getProperty("crowns", "1")),
                    Long.parseLong(props.getProperty("spawnAt", "0")), endAt);
            e.appeared = Boolean.parseBoolean(props.getProperty("appeared", "false"));
            e.reminder = REMINDERS.length;
            String mob = props.getProperty("mob");
            if (mob != null) e.mob = UUID.fromString(mob);
            current = e;
            EldaniorLogger.info("[Boss Mondial] repris : " + kind.name + " (" + e.coords() + ")");
        } catch (Exception ex) {
            EldaniorLogger.error("[Boss Mondial] chargement", ex);
        }
    }
}
