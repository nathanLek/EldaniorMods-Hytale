package com.eldanior.system.territory.systems;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.hypixel.hytale.math.shape.Box2D;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMapSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.map.WorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Carte du royaume (panneau d'administration › Tableau › Carte du royaume). Demandes de l'utilisateur :
 *  - 2026-11-19 : « la carte chargee a 100 % (le royaume entier), se deplacer librement sans chunk vide » ;
 *  - 2026-11-20 : « la map visitee doit rester chargee », « deplacer mon curseur sans etre limite », « quand tu refresh tu oublies
 *    ce que tu as deja enregistre », « ameliore le systeme de map ».
 *
 * Historique : la zone fixe du moteur (WorldMapSettings.worldMapArea) rendait la carte NOIRE (chargement ligne par ligne depuis le
 * coin nord-ouest du royaume, la region du joueur en dernier) : ne plus l'utiliser. Puis un rayon recalcule toutes les 5 s :
 * en retrecissant il effacait la carte du client, et le serveur refaisait tout a chaque reconnexion.
 *
 * Fonctionnement actuel :
 *  1. MEMOIRE SUR DISQUE (MapCache) : chaque image produite est gardee. Une image deja vue est resservie sans recharger ni generer
 *     le chunk : reconnexion et retour sur la carte quasi instantanes, rien n'est oublie.
 *  2. Les images sont REFAITES seulement pres d'un joueur ({@link #LIVE_RADIUS} chunks), la ou le terrain peut changer ; ailleurs
 *     la memoire suffit. Chunks jamais vus : generes s'ils sont dans le royaume (du plus proche au plus loin), laisses vides
 *     ailleurs, puis remplis quand un joueur y passe (WorldMapTracker.clearChunks).
 *  3. RAYON de carte par joueur (WorldMapTracker.setViewRadiusOverride) qui couvre tout le royaume et NE RETRECIT JAMAIS tant que
 *     la carte est active : le client ne recoit jamais d'ordre d'effacer ce qu'il a deja.
 *  4. DEZOOM plus large (minScale {@link #MIN_ZOOM} au lieu de 32) pour voir tout le royaume d'un coup.
 *
 * 2026-11-20 (3e retour : « a l'arrivee dans le serveur la map doit etre chargee entierement, sans zone noire ») :
 *  5. PREPARATION en tache de fond (prepare) : tant que la carte est active, chaque chunk du royaume qui n'a pas encore d'image
 *     est dessine (et genere s'il ne l'a jamais ete) puis garde sur disque, par paquets, region par region,
 *     en partant des joueurs. Une seule fois : ensuite tout le royaume est pret. Avancement dans le panneau d'administration.
 *  6. ENVOI EN BLOC a l'arrivee (push) : des qu'un joueur entre dans le monde (ou a l'activation), toutes les images gardees du
 *     royaume lui sont envoyees directement (paquets UpdateWorldMap d'environ 1,5 Mo, les regions les plus proches d'abord),
 *     sans attendre le suivi du moteur, qui les note comme deja envoyees (champ prive « loaded » du WorldMapTracker, par
 *     reflexion : sinon il les renverrait une 2e fois).
 *
 * 2026-11-20 (4e retour : « quand je me teleporte depuis la carte les marqueurs bougent avec moi, la carte n'est chargee
 * qu'autour de moi, des zones ne chargent pas ») : dans le moteur, WorldMapTracker.setViewRadiusOverride appelle clear()
 * (ClearWorldMap au client, chunks et marqueurs oublies). Le rayon etait recalcule depuis la position du joueur et grandissait
 * apres une teleportation : la carte etait videe, puis seule la spirale du moteur la remplissait, autour du joueur. Desormais :
 *  7. RAYON FIXE par royaume (sa diagonale), le meme ou que soit le joueur : pose une seule fois, jamais apres un deplacement ;
 *     et si le moteur doit quand meme l'appliquer (royaume agrandi), tout le royaume est renvoye en bloc juste apres.
 *  8. TROUS : un chunk du royaume demande avant d'avoir son image part vide au client (le moteur le note comme envoye) ; des que
 *     son image existe (preparation ou passage d'un joueur), il est redemande pour tous les joueurs du monde (clearChunks).
 *
 * 2026-11-20 (5e retour : « c'est toujours pas ca ») : journal du serveur : royaume de 100 806 chunks, ~17 000 images seulement,
 * preparation bloquee (« paquet trop lent » toutes les 2 min : le moteur doit GENERER le terrain de chaque chunk et de ses 8
 * voisins pour le dessiner). Desormais :
 *  9. ESQUISSE (MapSketch) : tout chunk du royaume sans vraie image est dessine directement par le generateur de monde (relief,
 *     couleur de carte des biomes, eau, ombres), sans creer de chunk : quelques millisecondes au lieu de plusieurs secondes. La
 *     preparation dessine ainsi tout le royaume ; le moteur en redemande aussi a la volee. Gardees a part (dossier « -esquisse »).
 * 10. VRAIE IMAGE : des qu'un joueur passe a moins de {@link #UPGRADE_RADIUS} chunks d'une esquisse, le chunk est redemande et la
 *     vraie image (arbres, constructions, chemins) la remplace, pour de bon (memoire sur disque).
 * 11. FINESSE conseillee selon la taille du royaume ({@link #recommended}) : a 3 px/bloc, 100 000 chunks pesent pres d'1 Go
 *     d'images pour le client et le serveur ; 0,5 px/bloc reste lisible et 20 fois plus leger.
 */
public final class KingdomMap {

    private KingdomMap() {}

    /** Finesse proposee : pixels par bloc. BASE = carte du jeu. */
    public static final float BASE = 3.0f;
    public static final float[] SCALES = {BASE, 1.0f, 0.5f};
    private static final int MARGIN_CHUNKS = 2, MIN_RADIUS = 32, MAX_RADIUS = 512, LIVE_RADIUS = 32;
    private static final float MIN_ZOOM = 2f;
    private static final int SKETCH_BATCH = 64, UPGRADE_RADIUS = 10, PUSH_BYTES = 1_500_000, PUSH_PACKETS = 3;

    /** Preparation : monde, chunks du royaume, deja prets ; un seul travail a la fois. */
    private static volatile String prepWorld;
    private static volatile long prepTotal, prepReady;
    private static volatile boolean prepRunning;
    /** Derniere passe complete (monde|finesse|zone) : on ne repasse pas sur un royaume deja prepare. */
    private static volatile String prepDoneKey = "";
    private static final java.util.concurrent.ExecutorService PREP = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Eldanior-Carte");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        return t;
    });
    /** Envois en cours : joueur -> regions restantes (les plus proches d'abord). */
    private static final Map<UUID, Push> PUSHES = new ConcurrentHashMap<>();
    /** Suivi de carte deja servi pour chaque joueur (un nouveau suivi = arrivee, changement de monde, carte remise a zero). */
    private static final Map<UUID, Object> SERVED = new ConcurrentHashMap<>();

    private static volatile boolean enabled;
    private static volatile float scale = BASE;
    private static Path file, cacheDir;
    private static final Map<String, MapCache> CACHES = new ConcurrentHashMap<>(), SKETCHES = new ConcurrentHashMap<>();
    /** Esquisses deja redemandees pour leur vraie image pendant cette session (une seule tentative chacune). */
    private static final Map<String, LongSet> UPGRADED = new ConcurrentHashMap<>();
    /** Chunks laisses vides (hors royaume, jamais vus) par monde : a remplir quand un joueur y passe. */
    private static final Map<String, LongSet> EMPTY = new ConcurrentHashMap<>();
    /** Chunks du royaume envoyes vides (pas encore d'image) par monde, et ceux dont l'image vient d'arriver : a renvoyer. */
    private static final Map<String, LongSet> MISSING = new ConcurrentHashMap<>(), RELOAD = new ConcurrentHashMap<>();

    public static boolean enabled() { return enabled; }
    public static float scale() { return scale; }

    public static void init(Path pluginDataDir) {
        file = pluginDataDir.resolve("eldanior_data").resolve("carte_royaume.properties");
        cacheDir = pluginDataDir.resolve("eldanior_data").resolve("carte");
        load();
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { applyAll(); } catch (Exception e) { EldaniorLogger.error("[Carte] royaume", e); }
        }, 20, 3, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { for (MapCache c : CACHES.values()) c.flush(); for (MapCache c : SKETCHES.values()) c.flush(); }
            catch (Exception e) { EldaniorLogger.error("[Carte] memoire", e); }
        }, 60, 30, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { pushTick(); } catch (Exception e) { EldaniorLogger.error("[Carte] envoi", e); }
        }, 20, 200, TimeUnit.MILLISECONDS);
    }

    /** Panneau d'administration : active ou coupe la carte du royaume, avec cette finesse. */
    public static void set(boolean on, float pxPerBlock) {
        enabled = on;
        scale = pxPerBlock;
        if (!on) PUSHES.clear();
        SERVED.clear();
        save();
        applyAll();
    }

    /**
     * Vraies images du monde, a la finesse en cours : un dossier par finesse (celui de 3 px/bloc garde son ancien nom), pour que
     * « deja dessine » veuille dire « deja dessine a cette finesse » quand l'admin en change.
     */
    static MapCache cache(String world) {
        int w = width();
        String dir = safe(world) + (w == (int) Math.floor(32 * BASE) ? "" : "-w" + w);
        return CACHES.computeIfAbsent(dir, d -> new MapCache(cacheDir.resolve(d)));
    }

    /** Esquisses du monde (MapSketch) a la finesse en cours, a part des vraies images. */
    static MapCache sketches(String world) {
        String dir = safe(world) + "-esquisse-w" + width();
        return SKETCHES.computeIfAbsent(dir, d -> new MapCache(cacheDir.resolve(d)));
    }

    private static String safe(String world) { return world.replaceAll("[^A-Za-z0-9_.-]", "_"); }

    /** Image a servir : la vraie si elle existe, sinon l'esquisse, sinon null. */
    static MapImage best(String world, int cx, int cz, int width) {
        MapImage img = cache(world).get(cx, cz, width);
        return img != null ? img : sketches(world).get(cx, cz, width);
    }

    /** Finesse conseillee pour ce nombre de chunks (indice dans SCALES) : 3 px/bloc jusqu'a 20 000, 1 jusqu'a 50 000, puis 0,5. */
    public static int recommended(long chunks) {
        return chunks <= 20_000 ? 0 : chunks <= 50_000 ? 1 : 2;
    }

    /** Rectangle (en chunks) des royaumes du monde, avec une marge ; null s'il n'y a pas de royaume. */
    static Box2D area(String world) {
        double x1 = Double.MAX_VALUE, z1 = Double.MAX_VALUE, x2 = -Double.MAX_VALUE, z2 = -Double.MAX_VALUE;
        boolean any = false;
        for (ParcelData p : ParcelManager.getByType(ParcelType.KINGDOM)) {
            if (!world.equals(p.getWorld())) continue;
            any = true;
            x1 = Math.min(x1, Math.floorDiv(p.getMinX(), 32));
            z1 = Math.min(z1, Math.floorDiv(p.getMinZ(), 32));
            x2 = Math.max(x2, Math.floorDiv(p.getMaxX(), 32) + 1);
            z2 = Math.max(z2, Math.floorDiv(p.getMaxZ(), 32) + 1);
        }
        return any ? new Box2D(x1 - MARGIN_CHUNKS, z1 - MARGIN_CHUNKS, x2 + MARGIN_CHUNKS, z2 + MARGIN_CHUNKS) : null;
    }

    /** Nombre de chunks de la carte du royaume (affiche dans le panneau). */
    public static long chunks(World world) {
        Box2D a = world != null ? area(world.getName()) : null;
        return a == null ? 0 : (long) ((a.max.x - a.min.x) * (a.max.y - a.min.y));
    }

    private static void applyAll() {
        for (World world : Universe.get().getWorlds().values()) world.execute(() -> {
            try { apply(world); } catch (Exception e) { EldaniorLogger.error("[Carte] " + world.getName(), e); }
        });
    }

    /** Thread du monde : generateur enveloppe (memoire, finesse, dezoom), rayon des joueurs, chunks vides a remplir. */
    private static void apply(World world) {
        WorldMapManager m = world.getWorldMapManager();
        IWorldMap gen = m != null ? m.getGenerator() : null;
        if (gen == null) return;
        Box2D area = enabled ? area(world.getName()) : null;
        if (gen instanceof Realm r) {
            if (area == null) {
                m.setGenerator(r.delegate);
                for (Player p : world.getPlayers()) if (p != null && p.getWorldMapTracker() != null) p.getWorldMapTracker().setViewRadiusOverride(null);
                EldaniorLogger.info("[Carte] " + world.getName() + " : carte du royaume coupee");
                return;
            }
            if (r.scale != scale) { m.setGenerator(new Realm(r.delegate, world.getName(), scale)); SERVED.clear(); }
        } else {
            if (area == null) return;
            WorldMapSettings base = gen.getWorldMapSettings();
            if (base == null || base == WorldMapSettings.DISABLED || base.getWorldMapArea() != null) return;
            m.setGenerator(new Realm(gen, world.getName(), scale));
            SERVED.clear();
            EldaniorLogger.info("[Carte] " + world.getName() + " : carte du royaume (" + chunks(world) + " chunks, memoire sur disque)");
        }
        if (!prepRunning && !prepKey(world.getName(), area).equals(prepDoneKey)) startPrepare(world);
        LongSet empty = EMPTY.get(world.getName());
        LongSet arrived = takeReload(world.getName());                       // images arrivees pour des chunks envoyes vides
        int want = radius(area);                                              // meme rayon partout : rien a refaire apres une teleportation
        for (Player player : world.getPlayers()) {
            var tracker = player != null ? player.getWorldMapTracker() : null;
            PlayerRef pr = player != null ? player.getPlayerRef() : null;
            Vector3d at = pr != null ? PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid()) : null;
            if (tracker == null || at == null) continue;
            int cx = (int) Math.floor(at.x / 32), cz = (int) Math.floor(at.z / 32);
            Integer cur = tracker.getViewRadiusOverride();
            if (cur == null || cur < want) {                                   // le moteur vide la carte du client (clear) :
                tracker.setViewRadiusOverride(want);                           // tout le royaume est renvoye juste apres
                SERVED.remove(pr.getUuid());
            }
            if (!arrived.isEmpty()) tracker.clearChunks(arrived);
            LongSet upgrade = upgrades(world.getName(), cx, cz);               // esquisses pres du joueur : vraie image
            if (!upgrade.isEmpty()) for (Player p : world.getPlayers()) if (p != null && p.getWorldMapTracker() != null) p.getWorldMapTracker().clearChunks(upgrade);
            if (SERVED.get(pr.getUuid()) != tracker) {                         // arrivee (ou carte remise a zero) : tout le royaume
                SERVED.put(pr.getUuid(), tracker);
                PUSHES.put(pr.getUuid(), new Push(world.getName(), pr, tracker, cx, cz, area, width()));
            }
            if (empty == null || empty.isEmpty()) continue;
            LongSet reload = new LongOpenHashSet();                          // chunks vides que le joueur approche : a remplir
            synchronized (empty) {
                for (int x = cx - LIVE_RADIUS; x <= cx + LIVE_RADIUS; x++)
                    for (int z = cz - LIVE_RADIUS; z <= cz + LIVE_RADIUS; z++) {
                        long idx = ChunkUtil.indexChunk(x, z);
                        if (empty.remove(idx)) reload.add(idx);
                    }
            }
            if (!reload.isEmpty()) for (Player p : world.getPlayers()) if (p != null && p.getWorldMapTracker() != null) p.getWorldMapTracker().clearChunks(reload);
        }
    }

    static int width() { return (int) Math.floor(32 * scale); }

    /** Esquisses a moins de UPGRADE_RADIUS chunks du joueur, sans vraie image, pas encore redemandees. */
    private static LongSet upgrades(String world, int cx, int cz) {
        LongSet out = new LongOpenHashSet();
        MapCache real = cache(world), sk = sketches(world);
        LongSet done = UPGRADED.computeIfAbsent(world, k -> new LongOpenHashSet());
        synchronized (done) {
            for (int x = cx - UPGRADE_RADIUS; x <= cx + UPGRADE_RADIUS; x++)
                for (int z = cz - UPGRADE_RADIUS; z <= cz + UPGRADE_RADIUS; z++) {
                    long idx = ChunkUtil.indexChunk(x, z);
                    if (done.contains(idx) || !sk.has(x, z) || real.has(x, z)) continue;
                    done.add(idx);
                    out.add(idx);
                }
        }
        return out;
    }

    private static String prepKey(String world, Box2D a) {
        return world + "|" + scale + "|" + (int) a.min.x + "," + (int) a.min.y + "," + (int) a.max.x + "," + (int) a.max.y;
    }

    // ==================== PREPARATION DU ROYAUME (tache de fond) ====================

    /** Avancement pour le panneau : {prets, total, en cours (1/0)}. */
    public static long[] progress() { return new long[]{prepReady, prepTotal, prepRunning ? 1 : 0}; }

    private static void startPrepare(World world) {
        Box2D area = area(world.getName());
        if (area == null || !(world.getWorldMapManager().getGenerator() instanceof Realm)) return;
        prepRunning = true;
        prepWorld = world.getName();
        String key = prepKey(world.getName(), area);
        PREP.submit(() -> {
            try { if (prepare(world, area)) prepDoneKey = key; } catch (Exception e) { EldaniorLogger.error("[Carte] preparation", e); }
            finally { prepRunning = false; }
        });
    }

    /**
     * Esquisse chaque chunk du royaume qui n'a ni vraie image ni esquisse, region par region, les plus proches des joueurs
     * d'abord (MapSketch : generateur de monde, aucun chunk cree). Le moteur redemande seul les chunks qu'il n'a pas encore.
     */
    private static boolean prepare(World world, Box2D area) throws Exception {
        String name = world.getName();
        MapCache cache = cache(name), sk = sketches(name);
        int w = width();
        float startScale = scale;
        if (!(world.getWorldMapManager().getGenerator() instanceof Realm realm)) return false;
        com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator gen = MapSketch.generator(realm.delegate);
        if (gen == null) { EldaniorLogger.warn("[Carte] pas de generateur de monde : pas d'esquisse du royaume"); return true; }
        int x1 = (int) area.min.x, z1 = (int) area.min.y, x2 = (int) area.max.x, z2 = (int) area.max.y;
        prepTotal = (long) (x2 - x1) * (z2 - z1);
        java.util.List<int[]> regions = new java.util.ArrayList<>();
        for (int rx = x1 >> 5; rx <= (x2 - 1) >> 5; rx++) for (int rz = z1 >> 5; rz <= (z2 - 1) >> 5; rz++) regions.add(new int[]{rx, rz});
        int[] from = firstPlayerChunk(name, (x1 + x2) / 2, (z1 + z2) / 2);
        regions.sort(java.util.Comparator.comparingDouble(r -> Math.hypot(r[0] * 32 + 16 - from[0], r[1] * 32 + 16 - from[1])));
        long ready = 0;
        for (int cx = x1; cx < x2; cx++) for (int cz = z1; cz < z2; cz++) if (cache.has(cx, cz) || sk.has(cx, cz)) ready++;
        prepReady = ready;
        if (ready >= prepTotal) return true;
        long t0 = System.currentTimeMillis();
        EldaniorLogger.info("[Carte] esquisse du royaume : " + ready + " / " + prepTotal + " chunks deja prets");
        java.util.List<int[]> batch = new java.util.ArrayList<>();
        for (int[] r : regions) {
            for (int cx = Math.max(x1, r[0] * 32); cx < Math.min(x2, r[0] * 32 + 32); cx++)
                for (int cz = Math.max(z1, r[1] * 32); cz < Math.min(z2, r[1] * 32 + 32); cz++) {
                    if (!enabled || scale != startScale || !(world.getWorldMapManager().getGenerator() instanceof Realm)) return false;
                    if (cache.has(cx, cz) || sk.has(cx, cz)) continue;
                    batch.add(new int[]{cx, cz});
                    if (batch.size() >= SKETCH_BATCH) { sketchBatch(world, gen, batch, w); batch = new java.util.ArrayList<>(); }
                }
        }
        if (!batch.isEmpty()) sketchBatch(world, gen, batch, w);
        sk.flush();
        EldaniorLogger.info("[Carte] royaume esquisse : " + prepReady + " / " + prepTotal + " chunks en " + (System.currentTimeMillis() - t0) / 1000 + " s");
        return true;
    }

    private static void sketchBatch(World world, com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator gen, java.util.List<int[]> batch, int w) throws Exception {
        int[][] cells = batch.toArray(new int[0][]);
        MapImage[] imgs;
        try {
            imgs = MapSketch.draw(world, gen, cells, w).get(60, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            EldaniorLogger.warn("[Carte] esquisse trop lente, on continue");
            return;
        }
        MapCache sk = sketches(world.getName());
        for (int i = 0; i < cells.length; i++) {
            if (imgs[i] == null) continue;
            sk.put(cells[i][0], cells[i][1], imgs[i]);
            filled(world.getName(), ChunkUtil.indexChunk(cells[i][0], cells[i][1]));
            prepReady++;
        }
        Thread.sleep(2);                                                      // laisse respirer le generateur de monde
    }

    private static int[] firstPlayerChunk(String world, int dx, int dz) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            var r = pr.getReference();
            if (r == null || !r.isValid() || !world.equals(r.getStore().getExternalData().getWorld().getName())) continue;
            Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at != null) return new int[]{(int) Math.floor(at.x / 32), (int) Math.floor(at.z / 32)};
        }
        return new int[]{dx, dz};
    }

    // ==================== ENVOI EN BLOC A L'ARRIVEE ====================

    /** Envoi de toutes les images gardees du royaume a un joueur, region par region (les plus proches d'abord). */
    private static final class Push {
        final String world;
        final PlayerRef pr;
        final Object tracker;
        final int width;
        final java.util.ArrayDeque<int[]> regions = new java.util.ArrayDeque<>();
        final Box2D area;
        java.util.ArrayDeque<int[]> cells = new java.util.ArrayDeque<>();
        long sent;

        Push(String world, PlayerRef pr, Object tracker, int cx, int cz, Box2D area, int width) {
            this.world = world; this.pr = pr; this.tracker = tracker; this.area = area; this.width = width;
            java.util.List<int[]> all = new java.util.ArrayList<>();
            for (int rx = ((int) area.min.x) >> 5; rx <= ((int) area.max.x - 1) >> 5; rx++)
                for (int rz = ((int) area.min.y) >> 5; rz <= ((int) area.max.y - 1) >> 5; rz++) all.add(new int[]{rx, rz});
            all.sort(java.util.Comparator.comparingDouble(r -> Math.hypot(r[0] * 32 + 16 - cx, r[1] * 32 + 16 - cz)));
            regions.addAll(all);
        }
    }

    private static java.lang.reflect.Field loadedField, lockField;
    private static volatile boolean reflectFailed;

    /** Note les chunks envoyes dans le suivi de carte du moteur (sinon il les renverrait). */
    private static void markLoaded(Object tracker, LongSet sent) {
        if (reflectFailed || sent.isEmpty()) return;
        try {
            if (loadedField == null) {
                loadedField = com.hypixel.hytale.server.core.universe.world.WorldMapTracker.class.getDeclaredField("loaded");
                lockField = com.hypixel.hytale.server.core.universe.world.WorldMapTracker.class.getDeclaredField("loadedLock");
                loadedField.setAccessible(true);
                lockField.setAccessible(true);
            }
            var lock = (java.util.concurrent.locks.ReentrantReadWriteLock) lockField.get(tracker);
            var loaded = (LongSet) loadedField.get(tracker);
            lock.writeLock().lock();
            try { loaded.addAll(sent); } finally { lock.writeLock().unlock(); }
        } catch (Exception e) {
            reflectFailed = true;
            EldaniorLogger.warn("[Carte] suivi du moteur non modifiable (" + e + ") : images envoyees deux fois");
        }
    }

    /** Toutes les 200 ms : jusqu'a PUSH_PACKETS paquets d'environ 1,5 Mo par joueur en cours d'envoi. */
    private static void pushTick() {
        if (!enabled || PUSHES.isEmpty()) return;
        for (var entry : PUSHES.entrySet()) {
            Push p = entry.getValue();
            var ref = p.pr.getReference();
            if (ref == null || !ref.isValid() || SERVED.get(entry.getKey()) != p.tracker) { PUSHES.remove(entry.getKey(), p); continue; }
            MapCache cache = cache(p.world);
            for (int packet = 0; packet < PUSH_PACKETS; packet++) {
                java.util.List<com.hypixel.hytale.protocol.packets.worldmap.MapChunk> chunks = new java.util.ArrayList<>();
                LongSet ids = new LongOpenHashSet();
                int bytes = 0;
                while (bytes < PUSH_BYTES) {
                    if (p.cells.isEmpty()) {
                        int[] r = p.regions.poll();
                        if (r == null) break;
                        java.util.Set<Long> seen = new java.util.HashSet<>();     // vraies images et esquisses de la region
                        java.util.List<int[]> list = new java.util.ArrayList<>();
                        for (int[] c : cache.cells(r[0], r[1])) if (seen.add(ChunkUtil.indexChunk(c[0], c[1]))) list.add(c);
                        for (int[] c : sketches(p.world).cells(r[0], r[1])) if (seen.add(ChunkUtil.indexChunk(c[0], c[1]))) list.add(c);
                        list.removeIf(c -> c[0] < p.area.min.x || c[0] >= p.area.max.x || c[1] < p.area.min.y || c[1] >= p.area.max.y);
                        p.cells = new java.util.ArrayDeque<>(list);
                        continue;
                    }
                    int[] c = p.cells.poll();
                    MapImage img = best(p.world, c[0], c[1], p.width);
                    if (img == null) continue;
                    chunks.add(new com.hypixel.hytale.protocol.packets.worldmap.MapChunk(c[0], c[1], img));
                    ids.add(ChunkUtil.indexChunk(c[0], c[1]));
                    bytes += 32 + img.packedIndices.length + (img.palette != null ? img.palette.length * 4 : 0);
                }
                if (chunks.isEmpty()) {
                    PUSHES.remove(entry.getKey(), p);
                    EldaniorLogger.info("[Carte] " + p.pr.getUsername() + " : " + p.sent + " images du royaume envoyees a l'arrivee");
                    break;
                }
                markLoaded(p.tracker, ids);
                p.pr.getPacketHandler().write(new com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMap(
                        chunks.toArray(new com.hypixel.hytale.protocol.packets.worldmap.MapChunk[0]), null, null));
                p.sent += chunks.size();
            }
        }
    }

    /**
     * Rayon (en chunks) : la diagonale du royaume, arrondie a 16 au-dessus. D'ou que soit le joueur dans le royaume, tout le royaume
     * reste dans son rayon : le moteur n'efface rien quand il se teleporte, et le rayon ne change jamais (le changer vide la carte).
     */
    private static int radius(Box2D area) {
        double diag = Math.hypot(area.max.x - area.min.x, area.max.y - area.min.y);
        return Math.clamp((int) Math.ceil(diag / 16.0) * 16 + 16, MIN_RADIUS, MAX_RADIUS);
    }

    /** Chunk du royaume parti vide chez les clients (pas encore d'image). */
    private static void missing(String world, long idx) {
        LongSet set = MISSING.computeIfAbsent(world, k -> new LongOpenHashSet());
        synchronized (set) { set.add(idx); }
    }

    /** Image d'un chunk enfin prete : s'il etait parti vide, il sera redemande pour tous les joueurs du monde. */
    private static void filled(String world, long idx) {
        LongSet set = MISSING.get(world);
        if (set == null) return;
        boolean was;
        synchronized (set) { was = set.remove(idx); }
        if (!was) return;
        LongSet reload = RELOAD.computeIfAbsent(world, k -> new LongOpenHashSet());
        synchronized (reload) { reload.add(idx); }
    }

    private static LongSet takeReload(String world) {
        LongSet reload = RELOAD.get(world);
        if (reload == null) return new LongOpenHashSet();
        synchronized (reload) {
            LongSet out = new LongOpenHashSet(reload);
            reload.clear();
            return out;
        }
    }

    /** Un joueur du monde a moins de LIVE_RADIUS chunks de ce chunk. */
    private static boolean nearPlayer(String world, int cx, int cz) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            var r = pr.getReference();
            if (r == null || !r.isValid() || !world.equals(r.getStore().getExternalData().getWorld().getName())) continue;
            Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at != null && Math.abs(Math.floor(at.x / 32) - cx) <= LIVE_RADIUS && Math.abs(Math.floor(at.z / 32) - cz) <= LIVE_RADIUS) return true;
        }
        return false;
    }

    /** Generateur de carte du monde, avec la memoire sur disque, la finesse choisie et un dezoom plus large. Jamais de zone fixe. */
    static final class Realm implements IWorldMap {
        final IWorldMap delegate;
        final String world;
        final float scale;
        private final WorldMapSettings settings;

        Realm(IWorldMap delegate, String world, float scale) {
            this.delegate = delegate;
            this.world = world;
            this.scale = scale;
            WorldMapSettings b = delegate.getWorldMapSettings();
            UpdateWorldMapSettings packet = b.getSettingsPacket() != null ? new UpdateWorldMapSettings(b.getSettingsPacket()) : null;
            if (packet != null) packet.minScale = Math.min(packet.minScale, MIN_ZOOM);   // on peut reculer jusqu'a voir tout le royaume
            this.settings = new WorldMapSettings(null, scale, 2.0f, b.getViewRadiusMin(), b.getViewRadiusMax(), packet);
        }

        @Override public WorldMapSettings getWorldMapSettings() { return settings; }

        /**
         * Pres d'un joueur : vraie image (le terrain peut changer), l'esquisse en secours. Ailleurs : la vraie image gardee, sinon
         * l'esquisse gardee, sinon une esquisse calculee tout de suite (royaume) ; hors royaume, rien tant que personne n'y passe.
         */
        @Override
        public CompletableFuture<WorldMap> generate(World w, int width, int height, LongSet chunks) {
            MapCache cache = cache(world), sk = sketches(world);
            Box2D area = area(world);
            WorldMap out = new WorldMap(chunks.size());
            LongSet live = new LongOpenHashSet();
            java.util.List<int[]> draft = new java.util.ArrayList<>();
            LongSet empty = EMPTY.computeIfAbsent(world, k -> new LongOpenHashSet());
            for (long idx : chunks) {
                int cx = ChunkUtil.xOfChunkIndex(idx), cz = ChunkUtil.zOfChunkIndex(idx);
                boolean realm = area != null && cx >= area.min.x && cx < area.max.x && cz >= area.min.y && cz < area.max.y;
                if (nearPlayer(world, cx, cz)) { live.add(idx); continue; }
                MapImage kept = cache.get(cx, cz, width);
                if (kept == null) kept = sk.get(cx, cz, width);
                if (kept != null) { out.getChunks().put(idx, kept); continue; }
                if (realm) draft.add(new int[]{cx, cz});
                else synchronized (empty) { empty.add(idx); }                   // hors royaume, jamais vu, personne : vide pour l'instant
            }
            com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator gen = draft.isEmpty() ? null : MapSketch.generator(delegate);
            if (!draft.isEmpty() && gen == null) for (int[] c : draft) live.add(ChunkUtil.indexChunk(c[0], c[1]));   // pas d'esquisse possible
            CompletableFuture<Void> liveDone = live.isEmpty() ? CompletableFuture.completedFuture(null)
                    : delegate.generate(w, width, height, live).thenAccept(made -> {
                        if (made != null && made.getChunks() != null)
                            for (var e : made.getChunks().long2ObjectEntrySet()) {
                                MapImage img = e.getValue();
                                if (img == null) continue;
                                synchronized (out) { out.getChunks().put(e.getLongKey(), img); }
                                cache.put(ChunkUtil.xOfChunkIndex(e.getLongKey()), ChunkUtil.zOfChunkIndex(e.getLongKey()), img);
                                filled(world, e.getLongKey());
                            }
                        for (long idx : live) {                                    // pas d'image : l'esquisse, ou a renvoyer plus tard
                            synchronized (out) { if (out.getChunks().containsKey(idx)) continue; }
                            int cx = ChunkUtil.xOfChunkIndex(idx), cz = ChunkUtil.zOfChunkIndex(idx);
                            MapImage s = sk.get(cx, cz, width);
                            if (s != null) synchronized (out) { out.getChunks().put(idx, s); }
                            else if (area != null && cx >= area.min.x && cx < area.max.x && cz >= area.min.y && cz < area.max.y) missing(world, idx);
                        }
                    });
            CompletableFuture<Void> draftDone = gen == null ? CompletableFuture.completedFuture(null)
                    : MapSketch.draw(w, gen, draft.toArray(new int[0][]), width).thenAccept(imgs -> {
                        for (int i = 0; i < imgs.length; i++) {
                            long idx = ChunkUtil.indexChunk(draft.get(i)[0], draft.get(i)[1]);
                            if (imgs[i] == null) { missing(world, idx); continue; }
                            sk.put(draft.get(i)[0], draft.get(i)[1], imgs[i]);
                            synchronized (out) { out.getChunks().put(idx, imgs[i]); }
                        }
                    });
            return CompletableFuture.allOf(liveDone, draftDone).handle((v, err) -> {
                if (err != null) EldaniorLogger.error("[Carte] images", err);
                return out;
            });
        }

        @Override
        public CompletableFuture<Map<String, MapMarker>> generatePointsOfInterest(World w) {
            return delegate.generatePointsOfInterest(w);
        }

        @Override public void shutdown() { for (MapCache c : CACHES.values()) c.flush(); for (MapCache c : SKETCHES.values()) c.flush(); }   // le generateur d'origine resert apres nous
    }

    private static void save() {
        if (file == null) return;
        try {
            Files.createDirectories(file.getParent());
            Properties p = new Properties();
            p.setProperty("enabled", String.valueOf(enabled));
            p.setProperty("scale", String.valueOf(scale));
            com.eldanior.system.config.PersistenceUtils.writeAtomicWithBackup(file, p, "Eldanior - carte du royaume");
        } catch (Exception e) {
            EldaniorLogger.error("[Carte] sauvegarde", e);
        }
    }

    private static void load() {
        if (file == null || !Files.exists(file)) return;
        try (InputStream in = Files.newInputStream(file)) {
            Properties p = new Properties();
            p.load(in);
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "false"));
            scale = Float.parseFloat(p.getProperty("scale", String.valueOf(BASE)));
            boolean known = false;
            for (float s : SCALES) known |= s == scale;
            if (!known) scale = BASE;
        } catch (Exception e) {
            EldaniorLogger.error("[Carte] chargement", e);
        }
    }
}
