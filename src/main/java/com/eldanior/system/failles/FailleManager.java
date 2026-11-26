package com.eldanior.system.failles;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.boards.MonsterContracts;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.shop.CoinWallet;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.territory.laws.DomainLaws;
import com.eldanior.system.util.NpcAttitudes;
import com.eldanior.system.util.WorldBlockUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.ProjectileComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;
import org.joml.Vector3i;

import it.unimi.dsi.fastutil.Pair;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Failles-donjons (2026-11-22/23, docs/plan-failles-donjons.md). Une faille s'ouvre dans le monde (bloc Eldanior_Faille_<Rang>,
 * couleur et taille selon le rang, compteur d'entrees au-dessus, marqueur sur la carte) ; on y entre en marchant dedans
 * (Eldanior_EntrerFaille). Le premier entre cree le donjon : un monde d'instance vide ou l'on colle les pieces vanilla et la
 * couche « sceau » du plan (FailleDungeon). Le groupe suit par le meme bloc (une entree par joueur, groupSize au plus, membres
 * du groupe du chef s'il en a un). Mort = ejecte (gameplay Eldanior_Faille). Les salles s'eveillent a l'approche ; le mini-boss
 * lache la cle du sceau de la salle du boss ; le boss abattu : coffres au tresor, sortie, recompense, faille refermee.
 * Faille non fermee a temps ou groupe entierement ejecte : DEBORDEMENT (le portail explose, un groupe de monstres en sort).
 *
 * Apparitions naturelles proportionnelles aux joueurs connectes : au plus ceil(joueurs / 3) failles (8 max), du rang d'un joueur
 * present (jamais au-dessus du rang le plus haut des joueurs), et un meme donjon ne revient pas avant 1 h (sauf admin).
 */
public final class FailleManager {

    private FailleManager() {}

    /** Origine du donjon dans le monde de la faille. */
    static final int BASE_X = 0, BASE_Y = 120, BASE_Z = 0;
    static final long LIFETIME_MS = 30 * 60_000L, CLOSE_DELAY_MS = 3 * 60_000L, SAME_DUNGEON_COOLDOWN_MS = 60 * 60_000L;
    static final long NATURAL_CHECK_MS = 10 * 60_000L;
    static final String EXIT_BLOCK = "Eldanior_Faille_Sortie", SEALED_PREFIX = "Eldanior_Faille_Scellee_";
    static final double ROOM_MARGIN = 6, HOLO_RANGE = 64;
    static final int DEFAULT_GROUP = 3;

    private static final Map<String, Faille> FAILLES = new ConcurrentHashMap<>();
    /** Monde d'instance -> faille. */
    private static final Map<String, Faille> BY_INSTANCE = new ConcurrentHashMap<>();
    /** Donjon -> fin de la derniere faille naturelle (delai d'1 h avant qu'il revienne). */
    private static final Map<String, Long> LAST_CLOSED = new ConcurrentHashMap<>();
    private static final Random RNG = new Random();
    private static Path dataFile;
    private static volatile boolean natural = true;
    private static volatile long nextNaturalAt;

    // =====================================================================================================================
    // Demarrage, persistance
    // =====================================================================================================================

    public static void init(com.hypixel.hytale.server.core.plugin.JavaPlugin plugin, Path pluginDataDir) {
        FaillePack.install(plugin, pluginDataDir);                                              // modeles d'instance modifiables
        dataFile = pluginDataDir.resolve("eldanior_data").resolve("failles.json");
        load();
        FailleDungeon.all();
        nextNaturalAt = System.currentTimeMillis() + 5 * 60_000L;
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { tick(); } catch (Exception e) { EldaniorLogger.error("[Failles] tick", e); }
        }, 5, 1, TimeUnit.SECONDS);
        EldaniorLogger.info("[Failles] " + FailleDungeon.all().size() + " donjon(s), " + FAILLES.size() + " faille(s) ouverte(s)");
    }

    private record Saved(String id, String world, int x, int y, int z, String rank, String dungeon, int group, String source,
                         long openedAt, long expireAt, int[] anchor, List<String> snapshot, long openAt, boolean revealed) {}

    private record SavedState(List<Saved> failles, Map<String, Long> lastClosed, boolean natural, Map<String, int[]> arrivals,
                              Map<String, List<String>> restores) {}

    /** Arrivee choisie par l'admin dans le monde d'edition (donjon -> x, y, z relatifs a l'origine). */
    private static final Map<String, int[]> ARRIVALS = new ConcurrentHashMap<>();
    /** Donjon -> monde d'edition ouvert. */
    private static final Map<String, String> EDITING = new ConcurrentHashMap<>();

    private static synchronized void save() {
        if (dataFile == null) return;
        try {
            List<Saved> out = new ArrayList<>();
            for (Faille f : FAILLES.values())
                if (f.state == Faille.State.OUVERTE)
                    out.add(new Saved(f.id, f.world, f.x, f.y, f.z, f.rank.label, f.dungeonId, f.groupSize, f.source.name(), f.openedAt, f.expireAt,
                            f.anchor, f.snapshot, f.openAt, f.revealed));
            Files.createDirectories(dataFile.getParent());
            Files.writeString(dataFile, new GsonBuilder().setPrettyPrinting().create().toJson(new SavedState(out, LAST_CLOSED, natural, ARRIVALS, PENDING_RESTORE)),
                    StandardCharsets.UTF_8);
        } catch (Exception e) { EldaniorLogger.error("[Failles] sauvegarde", e); }
    }

    private static void load() {
        try {
            if (dataFile == null || !Files.exists(dataFile)) return;
            SavedState s = new Gson().fromJson(Files.readString(dataFile, StandardCharsets.UTF_8), new TypeToken<SavedState>() {}.getType());
            if (s == null) return;
            if (s.lastClosed() != null) LAST_CLOSED.putAll(s.lastClosed());
            natural = s.natural();
            if (s.arrivals() != null) ARRIVALS.putAll(s.arrivals());
            if (s.restores() != null) PENDING_RESTORE.putAll(s.restores());
            if (s.failles() != null) for (Saved v : s.failles()) {
                FailleRank r = FailleRank.of(v.rank());
                if (r == null || FailleDungeon.get(v.dungeon()) == null) continue;
                Faille f = new Faille(v.id(), v.world(), v.x(), v.y(), v.z(), r, v.dungeon(), v.group(),
                        Faille.Source.valueOf(v.source()), v.openedAt(), v.expireAt());
                f.anchor = v.anchor(); f.snapshot = v.snapshot(); f.openAt = v.openAt(); f.revealed = v.revealed() || v.anchor() == null;
                FAILLES.put(f.id, f);
            }
        } catch (Exception e) { EldaniorLogger.error("[Failles] lecture", e); }
    }

    // =====================================================================================================================
    // Consultation (carte, panneau admin)
    // =====================================================================================================================

    public static Collection<Faille> all() { return FAILLES.values(); }

    public static boolean naturalEnabled() { return natural; }

    public static void setNatural(boolean on) { natural = on; save(); }

    public static boolean isRiftWorld(String worldName) { return worldName != null && BY_INSTANCE.containsKey(worldName); }

    /** Marqueurs : {id, x, y, z, image, boussole, nom}. */
    public static List<Object[]> markers(String world) {
        List<Object[]> out = new ArrayList<>();
        for (Faille f : FAILLES.values()) {
            if (!f.world.equals(world) || f.state == Faille.State.FERMEE || f.state == Faille.State.RATEE) continue;
            out.add(new Object[]{f.id, f.x, f.y, f.z, f.rank.markerImage(), f.rank.compassImage(),
                    f.title() + " · " + f.entriesLeft() + "/" + f.groupSize + " entrées"});
        }
        return out;
    }

    public static List<String> list() {
        List<String> out = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Faille f : FAILLES.values())
            out.add(f.title() + " · " + f.coords() + " · " + f.state.name().toLowerCase().replace('_', ' ') + " · "
                    + f.entered.size() + "/" + f.groupSize + " entrés · " + Math.max(0, (f.expireAt - now) / 60_000) + " min");
        return out;
    }

    // =====================================================================================================================
    // Ouverture
    // =====================================================================================================================

    /** Ouvre une faille au bloc (x, y, z) (thread du monde, chunk charge). Renvoie le message pour celui qui l'a demandee. */
    public static String open(World world, int x, int y, int z, FailleRank rank, int groupSize, Faille.Source source) {
        return open(world, x, y, z, rank, groupSize, source, null, 0);
    }

    /** Structure d'apparition d'un rang (scripts/failles/gen_failles.py : copie de Goblin_World_Event_Portal). */
    record Site(String prefab, int[] main, int rotation, List<int[]> cells) {}

    private static final Map<FailleRank, Site> SITES = new ConcurrentHashMap<>();

    static Site site(FailleRank r) {
        return SITES.computeIfAbsent(r, k -> {
            try (var in = FailleManager.class.getResourceAsStream("/eldanior/failles/site_" + k.label + ".json")) {
                if (in == null) return null;
                var o = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                List<int[]> cells = new ArrayList<>();
                for (var c : o.getAsJsonArray("cells")) { var a = c.getAsJsonArray(); cells.add(new int[]{a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()}); }
                var m = o.getAsJsonArray("main");
                return new Site(o.get("prefab").getAsString(), new int[]{m.get(0).getAsInt(), m.get(1).getAsInt(), m.get(2).getAsInt()},
                        o.get("rotation").getAsInt(), cells);
            } catch (Exception e) { EldaniorLogger.error("[Failles] structure " + k.label, e); return null; }
        });
    }

    /**
     * Annonce une faille au sol (x, y-1, z : bloc de sol ; thread du monde, chunk charge) : l'etat des blocs est sauvegarde, les
     * etincelles du rang apparaissent avec un compte a rebours ; au bout de delayS secondes la faille s'ouvre comme la faille
     * gobeline du jeu (explosion en anneau, eclat, arche de roche collee avec le portail). Les blocs d'origine reviennent a la
     * fermeture. Renvoie le message pour celui qui l'a demandee.
     */
    public static String open(World world, int x, int y, int z, FailleRank rank, int groupSize, Faille.Source source, String dungeonId, int delayS) {
        if (isRiftWorld(world.getName()) || isEditWorld(world.getName())) return "Impossible d'ouvrir une faille ici.";
        if (inCity(world.getName(), x + 0.5, y + 0.5, z + 0.5)) return "Les failles ne s'ouvrent jamais dans une ville.";
        FailleDungeon d = dungeonId != null ? FailleDungeon.get(dungeonId) : FailleDungeon.forRank(rank);
        if (d == null) return "Aucun donjon de faille n'est disponible.";
        FailleRank r = d.rank;
        Site site = site(r);
        if (site == null) return "Structure d'apparition introuvable pour le rang " + r.label + ".";
        for (Faille o : FAILLES.values())
            if (o.world.equals(world.getName()) && Math.abs(o.x - x) < 16 && Math.abs(o.z - z) < 16) return "Une faille est déjà ouverte ici.";
        int ax = x, ay = y - 1, az = z;                                                         // ancre de la structure : le sol
        List<String> snap = new ArrayList<>();
        for (int[] c : site.cells()) {
            BlockType t = WorldBlockUtil.getBlockType(world, ax + c[0], ay + c[1], az + c[2]);
            if (t == null) return "Le terrain n'est pas chargé ici.";
            snap.add(c[0] + "," + c[1] + "," + c[2] + "," + t.getId() + "," + WorldBlockUtil.getRotationIndex(world, ax + c[0], ay + c[1], az + c[2]));
        }
        long now = System.currentTimeMillis();
        int[] m = site.main();
        Faille f = new Faille(UUID.randomUUID().toString().substring(0, 8), world.getName(), ax + m[0], ay + m[1], az + m[2], r, d.id,
                groupSize, source, now, now + delayS * 1000L + LIFETIME_MS);
        f.anchor = new int[]{ax, ay, az};
        f.snapshot = snap;
        f.openAt = now + delayS * 1000L;
        FAILLES.put(f.id, f);
        WorldBlockUtil.setBlock(world, ax, ay + 1, az, "Eldanior_Faille_" + r.label + "_Etincelle");   // etincelles d'annonce
        save();
        if (delayS <= 0) reveal(f, world);
        else broadcastNear(world.getName(), new Vector3d(ax, ay, az), 300, "Une déchirure crépite en " + f.coords() + " : une faille de rang "
                + r.label + " s'ouvre dans " + delayS + " s !", r.color);
        EldaniorLogger.info("[Failles] annoncee : " + f.title() + " en " + f.coords() + " (" + world.getName() + ", " + source + ", groupe "
                + f.groupSize + ", dans " + delayS + " s)");
        return f.title() + (delayS > 0 ? " : elle s'ouvre en " + f.coords() + " dans " + delayS + " s" : " ouverte en " + f.coords())
                + " (" + f.groupSize + " entrées).";
    }

    /** Ouverture : explosion en anneau, eclat du rang, arche de roche et portail colles (comme la faille gobeline). */
    private static void reveal(Faille f, World world) {
        if (f.revealed) return;
        f.revealed = true;
        f.expireAt = System.currentTimeMillis() + LIFETIME_MS;
        Site site = site(f.rank);
        int[] a = f.anchor;
        Store<EntityStore> store = world.getEntityStore().getStore();
        try {
            paste(site.prefab(), world, new Vector3i(a[0], a[1], a[2]), store);
        } catch (Exception e) { EldaniorLogger.error("[Failles] structure d'apparition", e); WorldBlockUtil.setBlock(world, f.x, f.y, f.z, f.rank.blockId()); }
        Vector3d c = new Vector3d(f.x + 1.5, f.y + 2, f.z + 0.5);
        try {
            for (int i = 0; i < 8; i++) {
                double ang = i * Math.PI / 4;
                com.eldanior.system.skills.runes.spells.SpellFx.fx("Explosion_Big", new Vector3d(a[0] + 0.5 + Math.cos(ang) * 10, a[1] + 1, a[2] + 0.5 + Math.sin(ang) * 10), 4f, store, store);
            }
            com.eldanior.system.skills.runes.spells.SpellFx.fx("Eldanior_Faille_" + f.rank.label + "_Ouverture", new Vector3d(f.x + 0.5, f.y + 1.05, f.z + 0.5), 1.8f, store, store);
            com.eldanior.system.skills.runes.spells.SpellFx.sound2dNear("SFX_Goblin_World_Breach_Open", c, 100, store);
        } catch (Exception ignored) { }
        save();
        broadcastNear(world.getName(), c, 400, "Une " + f.title().toLowerCase() + " s'est ouverte en " + f.coords()
                + " ! " + f.groupSize + " entrée(s) possible(s). Elle est marquée sur la carte.", f.rank.color);
        EldaniorLogger.info("[Failles] ouverte : " + f.title() + " en " + f.coords());
    }

    /** Remet les blocs d'origine sous la structure (thread du monde). false : chunk pas charge, a refaire plus tard. */
    private static boolean restoreSite(String worldName, int[] a, List<String> snap) {
        World world = Universe.get().getWorld(worldName);
        if (world == null || a == null || snap == null) return true;
        if (WorldBlockUtil.getBlockType(world, a[0], a[1], a[2]) == null) return false;
        for (String line : snap) {
            String[] p = line.split(",");
            int x = a[0] + Integer.parseInt(p[0]), y = a[1] + Integer.parseInt(p[1]), z = a[2] + Integer.parseInt(p[2]);
            int rot = p.length > 4 ? Integer.parseInt(p[4]) : 0;
            try { WorldBlockUtil.setBlock(world, x, y, z, p[3], Math.max(0, rot)); } catch (Exception e) { WorldBlockUtil.setBlock(world, x, y, z, "Empty"); }
        }
        // etincelles (case au-dessus du sol, peut-etre hors de la structure)
        BlockType t = WorldBlockUtil.getBlockType(world, a[0], a[1] + 1, a[2]);
        if (t != null && t.getId().endsWith("_Etincelle")) WorldBlockUtil.setBlock(world, a[0], a[1] + 1, a[2], "Empty");
        return true;
    }

    /** Sites a remettre en etat quand leur chunk sera charge : monde|x|y|z -> instantane. */
    private static final Map<String, List<String>> PENDING_RESTORE = new ConcurrentHashMap<>();

    private static void scheduleRestore(Faille f) {
        if ((f.anchor == null || f.snapshot == null) && !f.restored) {                           // faille d'avant les structures : son bloc seul
            f.restored = true;
            World w = Universe.get().getWorld(f.world);
            if (w != null) w.execute(() -> {
                BlockType t = WorldBlockUtil.getBlockType(w, f.x, f.y, f.z);
                if (t != null && t.getId().startsWith("Eldanior_Faille_")) WorldBlockUtil.setBlock(w, f.x, f.y, f.z, "Empty");
            });
            return;
        }
        if (f.anchor == null || f.snapshot == null || f.restored) return;
        f.restored = true;
        String key = f.world + "|" + f.anchor[0] + "|" + f.anchor[1] + "|" + f.anchor[2];
        PENDING_RESTORE.put(key, f.snapshot);
        save();
        tryRestores();
    }

    private static void tryRestores() {
        for (Map.Entry<String, List<String>> e : new ArrayList<>(PENDING_RESTORE.entrySet())) {
            String[] k = e.getKey().split("\\|");
            World w = Universe.get().getWorld(k[0]);
            if (w == null) continue;
            int[] a = {Integer.parseInt(k[1]), Integer.parseInt(k[2]), Integer.parseInt(k[3])};
            w.execute(() -> { if (restoreSite(k[0], a, e.getValue())) { PENDING_RESTORE.remove(e.getKey()); save(); } });
        }
    }

    /** Delai entre le clic de l'admin et l'ouverture (demande de l'utilisateur, 2026-11-23). */
    public static final int ADMIN_DELAY_S = 15;

    /** Panneau admin : la faille du donjon s'ouvre AUTOUR de l'admin 15 s apres le clic (compte a rebours dans le chat). */
    public static String openNearDelayed(Ref<EntityStore> admin, String dungeonId, int groupSize) {
        FailleDungeon d = FailleDungeon.get(dungeonId);
        if (d == null) return "Donjon inconnu.";
        String here = admin.getStore().getExternalData().getWorld().getName();
        if (isRiftWorld(here) || isEditWorld(here)) return "Sortez d'abord de la faille ou de l'édition.";
        return openNear(admin, d.rank, groupSize, dungeonId, ADMIN_DELAY_S);
    }

    /** Faille autour de l'admin (12 a 20 blocs, au sol), ouverte apres delayS secondes d'etincelles. */
    static String openNear(Ref<EntityStore> admin, FailleRank rank, int groupSize, String dungeonId, int delayS) {
        Store<EntityStore> store = admin.getStore();
        World world = store.getExternalData().getWorld();
        TransformComponent t = store.getComponent(admin, TransformComponent.getComponentType());
        if (t == null) return "Position inconnue.";
        Vector3d p = t.getPosition();
        for (int i = 0; i < 24; i++) {
            double a = RNG.nextDouble() * Math.PI * 2, dist = 12 + RNG.nextDouble() * 8;
            int x = (int) Math.floor(p.x + Math.cos(a) * dist), z = (int) Math.floor(p.z + Math.sin(a) * dist);
            Integer y = surface(world, x, z, (int) Math.floor(p.y));
            if (y != null) return open(world, x, y + 1, z, rank, groupSize, Faille.Source.ADMIN, dungeonId, delayS);
        }
        return "Pas de sol libre autour de vous pour ouvrir la faille.";
    }

    /** Supprime une faille (sans debordement) : bloc, compteur, donjon en cours (joueurs rejetes), monstres du debordement. */
    public static String remove(String id) {
        Faille f = FAILLES.get(id);
        if (f == null) return "Faille introuvable (déjà refermée ?).";
        finish(f, "supprimée par un admin");
        return f.title() + " en " + f.coords() + " supprimée.";
    }

    /** Supprime la faille la plus proche de l'admin (meme monde, 64 blocs). */
    public static String removeNearest(Ref<EntityStore> admin) {
        Faille f = nearest(admin, 64);
        return f == null ? "Aucune faille à moins de 64 blocs de vous." : remove(f.id);
    }

    public static Faille nearest(Ref<EntityStore> admin, double range) {
        World w = admin.getStore().getExternalData().getWorld();
        TransformComponent t = admin.getStore().getComponent(admin, TransformComponent.getComponentType());
        if (t == null) return null;
        Faille best = null;
        double bd = range * range;
        for (Faille f : FAILLES.values()) {
            if (!f.world.equals(w.getName())) continue;
            double d = t.getPosition().distanceSquared(f.x + 0.5, f.y, f.z + 0.5);
            if (d < bd) { bd = d; best = f; }
        }
        return best;
    }

    public static Faille get(String id) { return FAILLES.get(id); }

    // =====================================================================================================================
    // Edition des instances fixes (demande de l'utilisateur, 2026-11-23)
    // =====================================================================================================================

    public static boolean isEditWorld(String worldName) { return worldName != null && EDITING.containsValue(worldName); }

    /** Modele (nom d'instance) ouvert dans ce monde d'edition, null sinon. */
    public static String editInstanceOf(String worldName) {
        if (worldName == null) return null;
        for (Map.Entry<String, String> e : EDITING.entrySet())
            if (e.getValue().equals(worldName)) { FailleDungeon d = FailleDungeon.get(e.getKey()); return d != null ? d.instance : null; }
        return null;
    }

    public static boolean isFixed(FailleDungeon d) { return FaillePack.prebuilt(d.instance); }

    public static boolean isEditing(FailleDungeon d) { return EDITING.containsKey(d.id); }

    public static boolean hasArrival(FailleDungeon d) { return ARRIVALS.containsKey(d.id); }

    static int[] arrival(FailleDungeon d) {
        int[] a = ARRIVALS.get(d.id);
        return a != null ? a : d.arrival;
    }

    /**
     * Ouvre le MODELE de l'instance en edition (creatif, sans monstre) et y emmene l'admin. Premiere fois : le plan du donjon y
     * est colle. Les retouches (blocs, balises NPC_Spawn_Marker, coffres au tresor, sorties) sont sauvegardees dans
     * Mods/EldaniorFailles et servent a chaque faille ouverte ensuite.
     */
    public static String edit(Ref<EntityStore> admin, String dungeonId) {
        FailleDungeon d = FailleDungeon.get(dungeonId);
        if (d == null) return "Donjon inconnu.";
        Store<EntityStore> store = admin.getStore();
        World here = store.getExternalData().getWorld();
        if (isRiftWorld(here.getName())) return "Sortez d'abord de la faille.";
        if (isEditWorld(here.getName())) return "Vous êtes déjà dans une édition : « Terminer l'édition » d'abord.";
        TransformComponent t = store.getComponent(admin, TransformComponent.getComponentType());
        if (t == null) return "Position inconnue.";
        Transform back = new Transform(new Vector3d(t.getPosition()), new Rotation3f(0, 0, 0));
        int[] a = arrival(d);
        Transform spawn = new Transform(new Vector3d(BASE_X + a[0] + 0.5, BASE_Y + a[1] + 0.1, BASE_Z + a[2] + 0.5), new Rotation3f(0, 0, 0));
        String open = EDITING.get(d.id);
        World already = open != null ? Universe.get().getWorld(open) : null;
        CompletableFuture<World> ready;
        boolean fresh = !FaillePack.prebuilt(d.instance);
        if (already != null) ready = CompletableFuture.completedFuture(already);
        else {
            CompletableFuture<World> done = new CompletableFuture<>();
            ready = done;
            InstancesPlugin.loadInstanceAssetForEdit(d.instance).whenComplete((w, err) -> {
                if (w == null) {
                    EldaniorLogger.error("[Failles] edition de " + d.instance, err != null ? err : new IllegalStateException("monde null"));
                    done.completeExceptionally(err != null ? err : new IllegalStateException("edition impossible"));
                    return;
                }
                EDITING.put(d.id, w.getName());
                if (!fresh) { done.complete(w); return; }
                Faille tmp = new Faille("edit-" + d.id, here.getName(), 0, 0, 0, d.rank, d.id, 1, Faille.Source.ADMIN, 0, Long.MAX_VALUE);
                build(tmp, w, d).whenComplete((v, e2) -> {
                    if (e2 != null) { done.completeExceptionally(e2); return; }
                    Generateurs.seedTemplate(d.instance, d.generateurs);                             // generateurs du plan (modele neuf)
                    FaillePack.markMarkers(d.instance);                                              // pas de balises automatiques
                    done.complete(w);
                });
            });
        }
        InstancesPlugin.teleportPlayerToLoadingInstance(admin, store, ready, back, spawn);
        PlayerRef apr = store.getComponent(admin, PlayerRef.getComponentType());
        return "Édition de « " + d.name + " »" + (fresh ? " : le donjon d'origine y est collé (première fois)." : ".")
                + " Créatif, aucun monstre pendant l'édition. Posez des Générateurs de monstres (rubrique Donjons, F dessus pour choisir le monstre "
                + "et le nombre), coffres au trésor et sorties, puis « Terminer l'édition ».";
    }

    /** L'admin quitte l'edition : le monde d'edition se ferme des qu'il est vide, ce qui sauvegarde le modele. */
    public static String finishEdit(Ref<EntityStore> admin) {
        World w = admin.getStore().getExternalData().getWorld();
        String id = null;
        for (Map.Entry<String, String> e : EDITING.entrySet()) if (e.getValue().equals(w.getName())) id = e.getKey();
        if (id == null) {
            if (EDITING.isEmpty()) return "Aucune édition ouverte.";
            for (String k : new ArrayList<>(EDITING.keySet())) closeEditWorld(k);
            return "Éditions refermées et sauvegardées.";
        }
        String did = id;
        FaillePack.markEdited(FailleDungeon.get(id).instance);                                     // jamais remplace par une mise a jour
        InstancesPlugin.exitInstance(admin, admin.getStore());
        EldaniorLogger.SCHEDULER.schedule(() -> closeEditWorld(did), 3, TimeUnit.SECONDS);
        return "Édition terminée : modèle sauvegardé. Les prochaines failles de ce donjon en seront une copie.";
    }

    private static void closeEditWorld(String dungeonId) {
        String name = EDITING.get(dungeonId);
        World w = name != null ? Universe.get().getWorld(name) : null;
        if (w == null) { EDITING.remove(dungeonId); return; }
        if (!playersIn(w).isEmpty()) {                                                          // quelqu'un y est encore : plus tard
            EldaniorLogger.SCHEDULER.schedule(() -> closeEditWorld(dungeonId), 10, TimeUnit.SECONDS);
            return;
        }
        EDITING.remove(dungeonId);
        boolean ok = Universe.get().removeWorld(name);
        EldaniorLogger.info("[Failles] edition de " + dungeonId + " fermee (" + (ok ? "sauvegardee" : "deja fermee") + ")");
    }

    /** Dans le monde d'edition : l'arrivee des joueurs devient la position de l'admin. */
    public static String setArrival(Ref<EntityStore> admin) {
        World w = admin.getStore().getExternalData().getWorld();
        String id = null;
        for (Map.Entry<String, String> e : EDITING.entrySet()) if (e.getValue().equals(w.getName())) id = e.getKey();
        if (id == null) return "Placez-vous d'abord dans le donjon en cours d'édition (« Éditer l'instance »).";
        TransformComponent t = admin.getStore().getComponent(admin, TransformComponent.getComponentType());
        if (t == null) return "Position inconnue.";
        Vector3d p = t.getPosition();
        ARRIVALS.put(id, new int[]{(int) Math.floor(p.x) - BASE_X, (int) Math.floor(p.y) - BASE_Y, (int) Math.floor(p.z) - BASE_Z});
        save();
        return "Arrivée des joueurs placée ici pour « " + FailleDungeon.get(id).name + " ».";
    }

    /** Efface le modele construit : la prochaine edition (ou faille) recolle le donjon d'origine. */
    public static String resetDungeon(String dungeonId) {
        FailleDungeon d = FailleDungeon.get(dungeonId);
        if (d == null) return "Donjon inconnu.";
        if (EDITING.containsKey(d.id)) return "Terminez d'abord l'édition de ce donjon.";
        if (!FaillePack.reset(d.instance)) return "Impossible d'effacer le modèle (voir le journal).";
        ARRIVALS.remove(d.id);
        save();
        return "« " + d.name + " » remis à l'origine : il sera recollé à la prochaine édition ou faille.";
    }

    public static String closeAll() {
        int n = 0;
        for (Faille f : new ArrayList<>(FAILLES.values())) { finish(f, null); n++; }
        return n + " faille(s) refermée(s).";
    }

    /** Faille scellee posee par un joueur (thread du monde). false : pose refusee (en ville, dans une faille...). */
    public static boolean placeSealed(Player player, PlayerRef pr, World world, Vector3i pos, String itemId) {
        FailleRank rank = FailleRank.of(itemId.substring(SEALED_PREFIX.length()));
        if (rank == null) return true;
        if (isRiftWorld(world.getName())) { NotificationHelper.sendWarning(pr, "Impossible d'ouvrir une faille dans une faille."); return false; }
        if (inCity(world.getName(), pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5)) {
            NotificationHelper.sendWarning(pr, "Les failles ne s'ouvrent jamais dans une ville.");
            return false;
        }
        Party party = PartyManager.getParty(pr.getUuid());
        int size = party != null ? Math.max(1, Math.min(10, party.getSize())) : DEFAULT_GROUP;
        world.execute(() -> {
            WorldBlockUtil.setBlock(world, pos.x(), pos.y(), pos.z(), "Empty");                 // la faille scellee se brise
            String msg = open(world, pos.x(), pos.y(), pos.z(), rank, size, Faille.Source.SCELLEE, null, 5);
            pr.sendMessage(Message.raw("[Faille] " + msg).color(rank.color));
        });
        return true;
    }

    // =====================================================================================================================
    // Entree (marcher dans la faille)
    // =====================================================================================================================

    /** Le joueur touche une faille (interaction CollisionEnter). Thread du monde. */
    public static void touch(Ref<EntityStore> ref, Vector3i block) {
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null) return;
        Faille f = null;
        double best = Double.MAX_VALUE;
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        Vector3d at = block != null ? new Vector3d(block.x() + 0.5, block.y(), block.z() + 0.5) : t != null ? t.getPosition() : null;
        if (at == null) return;
        for (Faille o : FAILLES.values()) {
            if (!o.world.equals(world.getName())) continue;
            double d = at.distanceSquared(o.x + 0.5, o.y, o.z + 0.5);
            if (d < best && d < 100) { best = d; f = o; }
        }
        if (f == null) {                                                                        // bloc d'une faille disparue
            if (block != null) {
                BlockType bt = WorldBlockUtil.getBlockType(world, block.x(), block.y(), block.z());
                if (bt != null && bt.getId().startsWith("Eldanior_Faille_") && !bt.getId().startsWith(SEALED_PREFIX)
                        && !bt.getId().equals("Eldanior_Faille_Barriere") && !bt.getId().equals(EXIT_BLOCK))
                    WorldBlockUtil.setBlock(world, block.x(), block.y(), block.z(), "Empty");
            }
            return;
        }
        Faille rift = f;
        world.execute(() -> enter(ref, pr, rift));
    }

    private static final Map<UUID, Long> LAST_REFUSAL = new ConcurrentHashMap<>();
    /** Vrai pendant que la faille fait apparaitre un de SES monstres (FailleNpcSystem ne le touche pas). */
    private static final ThreadLocal<Boolean> OWN_SPAWN = ThreadLocal.withInitial(() -> false);

    private static void refuse(PlayerRef pr, String why) {
        long now = System.currentTimeMillis();
        Long last = LAST_REFUSAL.get(pr.getUuid());
        if (last != null && now - last < 4_000) return;                                  // on reste dans la faille : un message, pas cent
        LAST_REFUSAL.put(pr.getUuid(), now);
        NotificationHelper.sendWarning(pr, why);
    }

    private static void enter(Ref<EntityStore> ref, PlayerRef pr, Faille f) {
        if (!ref.isValid()) return;
        UUID u = pr.getUuid();
        if (f.state != Faille.State.OUVERTE && f.state != Faille.State.EN_COURS) { refuse(pr, "Cette faille se referme."); return; }
        if (!f.revealed) { refuse(pr, "La faille n'est pas encore ouverte."); return; }
        if (f.entered.contains(u)) { refuse(pr, "Vous êtes déjà entré dans cette faille : on n'y entre qu'une fois."); return; }
        if (f.entered.size() >= f.groupSize) { refuse(pr, "Cette faille n'accepte plus personne (" + f.groupSize + "/" + f.groupSize + ")."); return; }
        if (f.leader != null) {
            Party party = PartyManager.getParty(f.leader);
            if (party != null && !party.isMember(u)) {
                refuse(pr, "Cette faille est réservée au groupe de " + f.names.getOrDefault(f.leader, "son chef") + ".");
                return;
            }
        }
        FailleDungeon d = f.dungeon();
        if (d == null) { refuse(pr, "Le donjon de cette faille est introuvable."); return; }
        Store<EntityStore> store = ref.getStore();
        World here = store.getExternalData().getWorld();
        if (f.ready == null) startDungeon(f, here, d);
        f.entered.add(u);
        f.names.put(u, pr.getUsername());
        if (f.leader == null) f.leader = u;
        f.state = Faille.State.EN_COURS;
        save();
        // point de retour : a cote de la faille, pas dedans (sinon on y re-entre aussitot)
        double ang = RNG.nextDouble() * Math.PI * 2, r = 3.5 + 1.5 * scaleOf(f.rank);
        Transform back = new Transform(new Vector3d(f.x + 0.5 + Math.cos(ang) * r, f.y + 0.2, f.z + 0.5 + Math.sin(ang) * r), new Rotation3f(0, 0, 0));
        int[] a = arrival(d);
        Transform spawn = new Transform(new Vector3d(BASE_X + a[0] + 0.5, BASE_Y + a[1] + 0.1, BASE_Z + a[2] + 0.5), new Rotation3f(0, 0, 0));
        // ordre du moteur (parametres lus par javap, 2026-11-23) : (… , retour, arrivee)
        InstancesPlugin.teleportPlayerToLoadingInstance(ref, store, f.ready, back, spawn);
        NotificationHelper.showEventTitle(pr, f.title(), "Entrées : " + f.entered.size() + "/" + f.groupSize, true);
        EldaniorLogger.info("[Failles] " + pr.getUsername() + " entre dans " + f.title() + " (" + f.entered.size() + "/" + f.groupSize + ")");
    }

    static float scaleOf(FailleRank r) {
        return switch (r) { case F, E -> 1.0f; case D -> 1.2f; case C -> 1.4f; case B -> 1.6f; case A -> 1.9f; case S -> 2.2f; };
    }

    /** Cree le monde de la faille et y colle le donjon ; f.ready est complete quand tout est en place. */
    private static void startDungeon(Faille f, World from, FailleDungeon d) {
        CompletableFuture<World> done = new CompletableFuture<>();
        f.ready = done;
        Transform ret = new Transform(new Vector3d(f.x + 0.5, f.y + 1, f.z + 0.5), new Rotation3f(0, 0, 0));
        boolean fixed = FaillePack.prebuilt(d.instance);
        f.fixed = fixed;
        // copie du modele, passee en aventure, PNJ et balises d'apparition actifs (le modele est en creatif, sans monstre)
        InstancesPlugin.get().spawnInstance(d.instance, null, from, ret, cfg -> {
            cfg.setGameMode(com.hypixel.hytale.protocol.GameMode.Adventure);
            cfg.setSpawningNPC(true);
            cfg.setIsSpawnMarkersEnabled(true);
        }).whenComplete((w, err) -> {
            if (err != null || w == null) {
                EldaniorLogger.error("[Failles] creation du monde " + d.instance, err != null ? err : new IllegalStateException("monde null"));
                done.completeExceptionally(err != null ? err : new IllegalStateException("monde de faille non cree"));
                return;
            }
            f.instanceName = w.getName();
            BY_INSTANCE.put(w.getName(), f);
            if (!fixed) Generateurs.seedTemplate(d.instance, d.generateurs);                       // modele pas encore construit : ceux du plan
            Generateurs.registerRift(w.getName(), d.instance);                                    // generateurs poses dans le modele
            // boss : celui choisi au poste du modele (boss.json) sinon celui du plan ; il apparait a l'approche (generateur d'un
            // monstre), sauf si le modele a deja un generateur pour lui (l'ancien bloc de boss est vide depuis la v5)
            FaillePack.TemplateBoss tb = FaillePack.boss(d.instance);
            f.bossRole = tb != null && tb.role() != null ? tb.role() : d.bossRoom().boss.role();
            f.bossName = tb != null && tb.name() != null ? tb.name() : d.bossRoom().boss.name();
            if (tb != null && tb.level() > 0) f.bossLevel = tb.level();
            Generateurs.registerBoss(w.getName(), d.instance, tb != null ? new int[]{tb.x(), tb.y(), tb.z()} : d.boss, f.bossRole);
            (fixed ? CompletableFuture.<Void>completedFuture(null) : build(f, w, d)).whenComplete((v, e2) -> {
                if (e2 != null) { EldaniorLogger.error("[Failles] construction du donjon " + d.id, e2); done.completeExceptionally(e2); return; }
                long now = System.currentTimeMillis();
                f.readyAt = now;
                f.endAt = now + d.minutes * 60_000L;
                done.complete(w);
                EldaniorLogger.info("[Failles] donjon " + d.name + " pret dans " + w.getName() + (fixed ? " (instance fixe)" : " (plan colle)"));
            });
        });
    }

    /** Charge les chunks du donjon, colle les pieces puis la couche sceau, pose la plateforme de sortie de l'arrivee. */
    static CompletableFuture<Void> build(Faille f, World w, FailleDungeon d) {
        return buildBlocks(f, w, d);
    }

    /**
     * Balises d'apparition du plan (NPC_Spawn_Marker). Un monde d'instance ou aucun joueur n'est encore entre REFUSE les
     * entites (vu au serveur isole, 2026-11-23) : on les colle donc quand un joueur est la. Modele : a la premiere edition
     * (memorise par FaillePack.markMarkers, ensuite ce sont les balises retouchees qui comptent). Faille dont le modele n'a pas
     * encore ses balises : a l'arrivee du premier joueur.
     */
    static int pasteMarkers(World w, FailleDungeon d) {
        if (d.markersPrefab == null) return 0;
        Store<EntityStore> store = w.getEntityStore().getStore();
        int before = store.getEntityCount();
        try { paste(d.markersPrefab, w, new Vector3i(BASE_X, BASE_Y, BASE_Z), store, true); }
        catch (Exception e) { EldaniorLogger.error("[Failles] balises de " + d.id, e); }
        int n = store.getEntityCount() - before;
        EldaniorLogger.info("[Failles] balises d'apparition de " + d.id + " posees dans " + w.getName() + " : " + n);
        return n;
    }

    private static CompletableFuture<Void> buildBlocks(Faille f, World w, FailleDungeon d) {
        List<CompletableFuture<?>> chunks = new ArrayList<>();
        for (int cx = (BASE_X + d.min[0]) >> 5; cx <= (BASE_X + d.max[0]) >> 5; cx++)
            for (int cz = (BASE_Z + d.min[2]) >> 5; cz <= (BASE_Z + d.max[2]) >> 5; cz++)
                chunks.add(w.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunkFromBlock(cx << 5, cz << 5),
                        com.hypixel.hytale.server.core.universe.world.storage.GetChunkFlags.SET_TICKING));   // actifs : sinon PNJ refuses
        CompletableFuture<Void> out = new CompletableFuture<>();
        CompletableFuture.allOf(chunks.toArray(new CompletableFuture[0])).whenComplete((v, err) -> w.execute(() -> {
            try {
                f.pastingUntil = Long.MAX_VALUE;
                Store<EntityStore> store = w.getEntityStore().getStore();
                for (FailleDungeon.Piece p : d.pieces) paste(p.prefab(), w, new Vector3i(BASE_X + p.x(), BASE_Y + p.y(), BASE_Z + p.z()), store);
                paste(d.overlay, w, new Vector3i(BASE_X, BASE_Y, BASE_Z), store);
                if (d.door != null)                                                                  // porte de boss (cases de remplissage par le moteur)
                    WorldBlockUtil.setBlock(w, BASE_X + d.door.x(), BASE_Y + d.door.y(), BASE_Z + d.door.z(), d.door.block(), d.door.rotation());
                var template = w.getChunkStore().getStore().getResource(EldaniorSystem.CHEST_TEMPLATE_TYPE);
                for (FailleDungeon.Chest c : d.chests) {                                             // coffres au tresor enregistres
                    int x = BASE_X + c.x(), y = BASE_Y + c.y(), z = BASE_Z + c.z();
                    var tc = WorldBlockUtil.getBlockType(w, x, y, z);
                    if (tc == null || !c.block().equals(tc.getId())) WorldBlockUtil.setBlock(w, x, y, z, c.block());
                    try {
                        var table = com.eldanior.system.config.configs.LootTableConfig.getById(c.table());
                        String drop = com.eldanior.system.TreasureChest.ChestLoot.profile(c.table()) != null ? c.table().toLowerCase() : table.getTableId();
                        if (template != null) template.saveTemplate(x, y, z, table.generateLoot(RNG.nextLong()), drop);   // donjon_tier1.. gardes (2026-11-25)
                    } catch (Exception ex) { EldaniorLogger.error("[Failles] coffre " + c.block(), ex); }
                }
                int[] e = d.exitStart;
                WorldBlockUtil.setBlock(w, BASE_X + e[0], BASE_Y + e[1], BASE_Z + e[2], EXIT_BLOCK);
                f.pastingUntil = System.currentTimeMillis() + 1_500;
                out.complete(null);
            } catch (Exception ex) {
                out.completeExceptionally(ex);
            }
        }));
        return out;
    }

    /**
     * Colle un prefab (vanilla ou du mod) SANS ses entites : les PNJ et balises de patrouille enregistres dans les pieces
     * plantent l'outil de chemins du jeu au collage (« Key is EMPTY », vu le 2026-11-23) et nos monstres sont places a part.
     */
    private static void paste(String prefab, World w, Vector3i at, Store<EntityStore> store) { paste(prefab, w, at, store, false); }

    private static void paste(String prefab, World w, Vector3i at, Store<EntityStore> store, boolean entities) {
        Path path = PrefabStore.get().findAssetPrefabPath(prefab);
        if (path == null) throw new IllegalStateException("prefab introuvable : " + prefab);
        var buffer = com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil.getCached(path);
        com.hypixel.hytale.server.core.util.PrefabUtil.paste(buffer, w, at, com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation.None,
                true, RNG, 0, false, false, entities, store);
    }

    // =====================================================================================================================
    // Deroulement (chaque seconde)
    // =====================================================================================================================

    private static void tick() {
        long now = System.currentTimeMillis();
        for (Faille f : new ArrayList<>(FAILLES.values())) {
            World world = Universe.get().getWorld(f.world);
            if (world == null) continue;
            world.execute(() -> {
                try { outside(f, world, now); } catch (Exception e) { EldaniorLogger.error("[Failles] dehors " + f.id, e); }
            });
            World inst = f.instanceName != null ? Universe.get().getWorld(f.instanceName) : null;
            if (inst != null && f.readyAt > 0) inst.execute(() -> {
                try { inside(f, inst, now); } catch (Exception e) { EldaniorLogger.error("[Failles] donjon " + f.id, e); }
            });
            else if (f.readyAt > 0 && f.instanceName != null && f.state == Faille.State.EN_COURS && !f.bossDead) fail(f, "le donjon s'est refermé");
            else if (inst == null && f.bossDead && f.overflowMobs.isEmpty()) { BY_INSTANCE.values().remove(f); FAILLES.remove(f.id); }
            if (f.state == Faille.State.RATEE && f.overflowAt > 0 && now - f.overflowAt > LIFETIME_MS) FAILLES.remove(f.id);   // debordement oublie
        }
        if (natural && now >= nextNaturalAt) {
            nextNaturalAt = now + NATURAL_CHECK_MS;
            try { naturalSpawn(false); } catch (Exception e) { EldaniorLogger.error("[Failles] apparition naturelle", e); }
        }
        if (now % 10_000 < 1_000 && !PENDING_RESTORE.isEmpty()) tryRestores();
    }

    /** Dehors : compteur au-dessus de la faille, delai de vie, debordement en cours. */
    private static void outside(Faille f, World world, long now) {
        if (f.state == Faille.State.FERMEE || f.state == Faille.State.RATEE) return;
        BlockType t = WorldBlockUtil.getBlockType(world, f.x, f.y, f.z);
        if (!f.revealed) {                                                                       // etincelles : compte a rebours
            if (now >= f.openAt) { if (t != null) reveal(f, world); return; }
        } else if (f.state == Faille.State.OUVERTE && now >= f.expireAt) { fail(f, "personne ne l'a fermée à temps"); return; }
        Vector3d top = new Vector3d(f.x + 1.5, f.y + 5.2, f.z + 0.5);
        boolean near = MonsterContracts.playerNear(f.world, top, HOLO_RANGE);
        if (!near || t == null) return;                                                          // personne, ou chunk pas charge
        if (f.revealed && !f.rank.blockId().equals(t.getId()) && f.anchor != null && site(f.rank) != null) {             // structure perdue (outil, redemarrage...)
            try { paste(site(f.rank).prefab(), world, new Vector3i(f.anchor[0], f.anchor[1], f.anchor[2]), world.getEntityStore().getStore()); }
            catch (Exception e) { EldaniorLogger.error("[Failles] structure", e); }
        }
        Store<EntityStore> store = world.getEntityStore().getStore();
        String text = !f.revealed ? f.title() + "  ·  s'ouvre dans " + Math.max(0, (f.openAt - now + 999) / 1000) + " s"
                : f.title() + "  ·  Entrées : " + f.entriesLeft() + "/" + f.groupSize + "  ·  " + Math.max(0, (f.expireAt - now) / 60_000) + " min";
        if (f.state == Faille.State.EN_COURS && f.readyAt > 0) text = f.title() + "  ·  Entrées : " + f.entriesLeft() + "/" + f.groupSize + "  ·  donjon en cours";
        Ref<EntityStore> h = f.hologram;
        if (h == null || !h.isValid()) f.hologram = hologram(world, store, top, text);
        else store.putComponent(h, Nameplate.getComponentType(), new Nameplate(text));
    }

    /** Ligne de texte flottante (entite sans modele, jamais sauvegardee), comme les hologrammes du mod. */
    private static Ref<EntityStore> hologram(World world, Store<EntityStore> store, Vector3d at, String text) {
        try {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
            ProjectileComponent pc = new ProjectileComponent("Projectile");
            holder.putComponent(ProjectileComponent.getComponentType(), pc);
            holder.putComponent(TransformComponent.getComponentType(), new TransformComponent(at, new Rotation3f(0, 0, 0)));
            holder.ensureComponent(UUIDComponent.getComponentType());
            if (pc.getProjectile() == null) { pc.initialize(); if (pc.getProjectile() == null) return null; }
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
            holder.addComponent(Nameplate.getComponentType(), new Nameplate(text));
            holder.ensureComponent(store.getRegistry().getNonSerializedComponentType());
            return store.addEntity(holder, AddReason.SPAWN);
        } catch (Exception e) {
            EldaniorLogger.error("[Failles] compteur", e);
            return null;
        }
    }

    private static void removeHologram(Faille f) {
        Ref<EntityStore> h = f.hologram;
        f.hologram = null;
        if (h == null) return;
        World world = Universe.get().getWorld(f.world);
        if (world != null) world.execute(() -> { if (h.isValid()) h.getStore().removeEntity(h, RemoveReason.REMOVE); });
    }

    /** Dans le donjon : salles qui s'eveillent, boss, minuteur, depart des joueurs. */
    private static void inside(Faille f, World w, long now) {
        FailleDungeon d = f.dungeon();
        if (d == null) return;
        Store<EntityStore> store = w.getEntityStore().getStore();
        List<PlayerRef> inside = playersIn(w);
        // fin apres la victoire : on laisse le temps de piller les coffres
        if (f.bossDead) {
            if (now >= f.closeInstanceAt) {
                for (PlayerRef pr : inside) { var r = pr.getReference(); if (r != null && r.isValid()) InstancesPlugin.exitInstance(r, r.getStore()); }
                if (inside.isEmpty()) dropInstance(f, w);
            }
            return;
        }
        if (inside.isEmpty()) {
            if (now - f.readyAt > 20_000 && f.state == Faille.State.EN_COURS) fail(f, "tout le groupe a été éjecté");
            return;
        }
        // monstres : balises NPC_Spawn_Marker du modele (posees par gen_donjons.py, retouchables en edition)
        // monstres speciaux : nom, barres de boss
        for (Map.Entry<UUID, String> e : f.titles.entrySet()) {
            Ref<EntityStore> r = w.getEntityStore().getRefFromUUID(e.getKey());
            if (r == null || !r.isValid() || store.getComponent(r, DeathComponent.getComponentType()) != null) continue;
            store.putComponent(r, Nameplate.getComponentType(), new Nameplate(e.getValue()));
            if (e.getKey().equals(f.boss)) {
                f.bossBar.update(r, store, w, e.getValue(), true);
                TransformComponent bt = store.getComponent(r, TransformComponent.getComponentType());
                if (bt != null) f.bossLastPos = new Vector3d(bt.getPosition());
            }
        }
        // reveil du boss (gele et invulnerable quelques secondes), renforts
        Ref<EntityStore> b = f.boss != null ? w.getEntityStore().getRefFromUUID(f.boss) : null;
        if (b != null && b.isValid() && f.bossAwakeAt > 0 && now >= f.bossAwakeAt) {
            f.bossAwakeAt = 0;
            freeze(b, store, false);
            f.bossFightAt = now;
            for (PlayerRef pr : inside) NotificationHelper.showEventTitle(pr, bossName(f, d), "Boss de donjon", true);
        }
        // phase de boss adoptee : niveau, taille, crane
        UUID db = f.dressBoss;
        if (db != null) {
            Ref<EntityStore> r = w.getEntityStore().getRefFromUUID(db);
            if (r != null && r.isValid()) {
                f.dressBoss = null;
                if (f.bossLevel > 0) store.putComponent(r, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(f.bossLevel, bossRole(f, d), true));
                MonsterContracts.dress(r, store, f.bossScale, MonsterContracts.SKULL);
            }
        }
        // mort du boss (une VRAIE mort : composant de mort vu, ou tue par un joueur ; une entite dechargee ou retiree ne compte
        // pas) : victoire si aucune nouvelle phase n'est venue en 3 s
        if (f.boss != null && f.bossDeathAt == 0 && dead(w, store, f.boss)) { f.bossDeathAt = now; f.bossDeadUuid = f.boss; }
        if (f.bossDeathAt > 0 && now - f.bossDeathAt > 3_000) {
            if (f.boss != null && f.boss.equals(f.bossDeadUuid)) { f.bossBar.hide(); bossFallen(f, w, f.bossKiller); }
            else f.bossDeathAt = 0;                                                              // nouvelle phase adoptee
        }
    }

    private static boolean dead(World w, Store<EntityStore> store, UUID u) {
        if (u == null) return false;
        Ref<EntityStore> r = w.getEntityStore().getRefFromUUID(u);
        return r != null && r.isValid() && store.getComponent(r, DeathComponent.getComponentType()) != null;
    }

    private static boolean alive(World w, Store<EntityStore> store, UUID u) {
        if (u == null) return false;
        Ref<EntityStore> r = w.getEntityStore().getRefFromUUID(u);
        return r != null && r.isValid() && store.getComponent(r, DeathComponent.getComponentType()) == null;
    }

    private static List<PlayerRef> playersIn(World w) {
        List<PlayerRef> out = new ArrayList<>();
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r != null && r.isValid() && r.getStore().getExternalData().getWorld() == w) out.add(pr);
        }
        return out;
    }

    private static int playersEntered(Faille f) { return Math.max(1, f.entered.size()); }

    /** Une salle s'eveille : ses monstres apparaissent (plus nombreux selon le groupe), etoiles au hasard, mini-boss. */
    static UUID spawn(Faille f, World w, Store<EntityStore> store, String role, int[] p, int level, String room) {
        try {
            Vector3d at = new Vector3d(BASE_X + p[0] + 0.5, BASE_Y + p[1] + 0.05, BASE_Z + p[2] + 0.5);
            OWN_SPAWN.set(true);
            Pair<Ref<EntityStore>, ?> pair;
            try { pair = NPCPlugin.get().spawnNPC(store, role, null, at, new Rotation3f(0, RNG.nextFloat() * 360f, 0)); }
            finally { OWN_SPAWN.set(false); }
            Ref<EntityStore> ref = pair != null ? pair.first() : null;
            if (ref == null || !ref.isValid()) { EldaniorLogger.warn("[Failles] monstre non cree : " + role); return null; }
            store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(Math.max(1, level), role, true));
            UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
            if (u == null) return null;
            f.mobs.put(u.getUuid(), room);
            return u.getUuid();
        } catch (Exception e) {
            EldaniorLogger.error("[Failles] apparition " + role, e);
            return null;
        }
    }

    /** Monstre etoile : niveau x eliteFor, etoiles au-dessus de la tete, prime au tueur (comme un contrat). */
    private static void star(Faille f, World w, Store<EntityStore> store, UUID mob) {
        Ref<EntityStore> r = w.getEntityStore().getRefFromUUID(mob);
        if (r == null || !r.isValid()) return;
        double roll = RNG.nextDouble();
        int stars = roll < 0.70 ? 1 : roll < 0.95 ? 2 : 3;
        MobLevelData data = store.getComponent(r, EldaniorSystem.get().getMobLevelDataType());
        int lvl = data != null ? (int) Math.round(data.getLevel() * MonsterContracts.eliteFor(stars)) : 1;
        store.putComponent(r, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(lvl, data != null ? data.getMobTypeId() : "", true));
        MonsterContracts.dress(r, store, 1.15f, MonsterContracts.starsEmblem(stars));
        f.starred.put(mob, stars);
        NPCEntity npc = store.getComponent(r, NPCEntity.getComponentType());
        String species = npc != null ? npc.getNPCTypeId().replace("Dungeon_", "").replace('_', ' ') : "Monstre";
        f.titles.put(mob, species + " étoilé, " + MonsterContracts.stars(stars) + " · Lv." + lvl);
    }

    private static void freeze(Ref<EntityStore> ref, Store<EntityStore> store, boolean on) {
        var frozen = com.hypixel.hytale.server.core.entity.Frozen.getComponentType();
        var inv = com.hypixel.hytale.server.core.modules.entity.component.Invulnerable.getComponentType();
        if (on) {
            store.putComponent(ref, frozen, com.hypixel.hytale.server.core.entity.Frozen.get());
            store.putComponent(ref, inv, com.hypixel.hytale.server.core.modules.entity.component.Invulnerable.INSTANCE);
        } else {
            if (store.getComponent(ref, frozen) != null) store.removeComponent(ref, frozen);
            if (store.getComponent(ref, inv) != null) store.removeComponent(ref, inv);
        }
    }

    // =====================================================================================================================
    // Morts (DeathXPSystem) et monstres du jeu
    // =====================================================================================================================

    /** Monstre abattu par un joueur : prime des etoiles, cle du mini-boss, victoire au boss. */
    public static void killed(UUID victim, UUID killer, String killerName) {
        if (victim == null) return;
        Generateurs.killed(victim);
        for (Faille f : FAILLES.values()) {
            if (f.overflowMobs.remove(victim)) {
                if (f.overflowMobs.isEmpty()) {
                    broadcastNear(f.world, new Vector3d(f.x, f.y, f.z), 400, "Le débordement de la " + f.title().toLowerCase() + " est repoussé.", "#00e7b6");
                    FAILLES.remove(f.id);
                }
                return;
            }
            if (!f.mobs.containsKey(victim)) continue;
            f.mobs.remove(victim);
            f.titles.remove(victim);
            World w = f.instanceName != null ? Universe.get().getWorld(f.instanceName) : null;
            Integer stars = f.starred.remove(victim);
            if (stars != null && killer != null) {
                long prime = Math.round(f.rank.maxLevel * 30 / 10.0) * 10L * stars / 3;
                pay(killer, prime, 0, "Prime du monstre étoilé : " + CoinWallet.format(prime) + ".");
            }
            if (victim.equals(f.boss)) {                                                  // la phase suivante a 3 s pour venir
                f.bossKiller = killerName != null ? killerName : "?";
                f.bossDeathAt = System.currentTimeMillis();
                f.bossDeadUuid = victim;
            }
            return;
        }
    }

    static String bossRole(Faille f, FailleDungeon d) { return f.bossRole != null ? f.bossRole : d.bossRoom().boss.role(); }

    static String bossName(Faille f, FailleDungeon d) { return f.bossName != null ? f.bossName : d.bossRoom().boss.name(); }

    /** Boss du modele choisi au poste de PNJ pendant l'edition (role null = celui du plan). Renvoie le boss enregistre. */
    public static FaillePack.TemplateBoss setTemplateBoss(String instance, String role, String name, int level, Vector3d at) {
        FailleDungeon d = null;
        for (FailleDungeon x : FailleDungeon.all().values()) if (instance.equals(x.instance)) d = x;
        if (d == null) return null;
        FailleDungeon.Champion plan = d.bossRoom().boss;
        FaillePack.TemplateBoss b = new FaillePack.TemplateBoss(role != null ? role : plan.role(), name != null && !name.isBlank() ? name : plan.name(),
                level, (int) Math.floor(at.x) - BASE_X, (int) Math.floor(at.y) - BASE_Y, (int) Math.floor(at.z) - BASE_Z);
        return FaillePack.setBoss(instance, b) ? b : null;
    }

    /** Un PNJ apparait dans le monde d'une faille : ceux colles avec les pieces (prefabs) sont retires ; les phases de boss
     *  (nouvelle entite) et les invocations sont adoptees. Appele par FailleNpcSystem (thread du monde). */
    static boolean onNpcAdded(World w, Ref<EntityStore> ref, Store<EntityStore> store) {
        Faille f = BY_INSTANCE.get(w.getName());
        if (f == null || OWN_SPAWN.get()) return false;                                        // nos monstres : on garde
        if (System.currentTimeMillis() < f.pastingUntil) return true;                         // PNJ arrive avec les pieces : retire
        UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (u == null || npc == null || f.mobs.containsKey(u.getUuid())) return false;
        FailleDungeon d = f.dungeon();
        String bossRole = d != null ? bossRole(f, d) : null;
        String type = npc.getNPCTypeId();
        if (bossRole != null && f.boss == null && !f.bossDead && type != null && type.startsWith(bossRole)) {
            // instance fixe : le boss apparait par une balise NPC_Spawn_Marker ; on le reconnait a son role
            f.boss = u.getUuid();
            f.mobs.put(u.getUuid(), d.bossRoom().id);
            f.titles.put(u.getUuid(), "Boss de donjon · " + bossName(f, d));
            f.bossScale = d.bossRoom().boss.scale();
            f.dressBoss = u.getUuid();
            f.bossFightAt = System.currentTimeMillis();
        } else if (bossRole != null && f.boss != null && !f.bossDead && type != null && type.startsWith(bossRole)) {
            // nouvelle phase du boss (le jeu remplace l'entite) : elle reprend son titre et sa barre
            String title = f.titles.remove(f.boss);
            f.mobs.remove(f.boss);
            f.boss = u.getUuid();
            f.mobs.put(u.getUuid(), d.bossRoom().id);
            if (title != null) f.titles.put(u.getUuid(), title);
            f.dressBoss = u.getUuid();
            f.bossDeathAt = 0;
            if (!f.reinforced) f.bossFightAt = 1;                                                // phase 2 : renforts
        } else f.mobs.put(u.getUuid(), "invocation");
        return false;
    }

    /** Boss abattu : coffres au tresor, sortie, recompense, faille refermee dehors. Thread du monde de la faille. */
    private static void bossFallen(Faille f, World w, String killerName) {
        if (f.bossDead) return;
        FailleDungeon d = f.dungeon();
        if (d == null) return;
        f.bossDead = true;
        f.closeInstanceAt = System.currentTimeMillis() + CLOSE_DELAY_MS;
        // coffres au tresor : deja dans le donjon (plan ou modele retouche), butin propre a chaque joueur
        Vector3d bp = f.bossLastPos;
        if (bp != null) {                                                                        // sortie la ou le boss est tombe
            int ex = (int) Math.floor(bp.x), ey = (int) Math.floor(bp.y), ez = (int) Math.floor(bp.z);
            BlockType at = WorldBlockUtil.getBlockType(w, ex, ey, ez);
            if (at == null || at == BlockType.EMPTY || "Empty".equals(at.getId())) WorldBlockUtil.setBlock(w, ex, ey, ez, EXIT_BLOCK);
            else WorldBlockUtil.setBlock(w, ex, ey + 1, ez, EXIT_BLOCK);
        } else if (!f.fixed) {
            int[] e = d.exitEnd;
            WorldBlockUtil.setBlock(w, BASE_X + e[0], BASE_Y + e[1], BASE_Z + e[2], EXIT_BLOCK);
        }
        for (PlayerRef pr : playersIn(w)) {
            NotificationHelper.showEventTitle(pr, "Faille refermée", bossName(f, d) + " est vaincu", true);
            pr.sendMessage(Message.raw("[Faille] Pillez les coffres au trésor et prenez la sortie : la faille vous rejette dans "
                    + CLOSE_DELAY_MS / 60_000 + " min.").color("#00e7b6"));
        }
        for (UUID u : f.entered) pay(u, f.rank.money, f.rank.xp, "Faille de rang " + f.rank.label + " refermée : +" + f.rank.xp + " XP, "
                + CoinWallet.format(f.rank.money) + ".");
        f.state = Faille.State.FERMEE;
        closeOutside(f);
        broadcastNear(f.world, new Vector3d(f.x, f.y, f.z), 100_000, f.title() + " a été refermée par " + String.join(", ", f.names.values())
                + " (coup final : " + killerName + ").", "#00e7b6");
        EldaniorLogger.info("[Failles] victoire : " + f.title() + " par " + f.names.values());
    }

    // =====================================================================================================================
    // Fin : fermeture, echec, debordement
    // =====================================================================================================================

    /** Retire la faille du monde (bloc, compteur) ; le donjon vit encore le temps de sortir. */
    private static void closeOutside(Faille f) {
        removeHologram(f);
        scheduleRestore(f);                                                                      // terrain d'origine
        if (f.source == Faille.Source.NATURELLE) LAST_CLOSED.put(f.dungeonId, System.currentTimeMillis());
        save();
    }

    private static void dropInstance(Faille f, World w) {
        BY_INSTANCE.remove(w.getName());
        Generateurs.unregisterWorld(w.getName());
        f.instanceName = null;
        if (f.overflowMobs.isEmpty()) FAILLES.remove(f.id);
        try { InstancesPlugin.safeRemoveInstance(w); } catch (Exception e) { EldaniorLogger.error("[Failles] suppression du monde", e); }
    }

    /** Echec : la faille deborde. */
    private static void fail(Faille f, String why) {
        if (f.state == Faille.State.FERMEE || f.state == Faille.State.RATEE) return;
        f.state = Faille.State.RATEE;
        f.bossBar.hide(); f.miniBar.hide();
        World inst = f.instanceName != null ? Universe.get().getWorld(f.instanceName) : null;
        if (inst != null) inst.execute(() -> {
            for (PlayerRef pr : playersIn(inst)) { var r = pr.getReference(); if (r != null && r.isValid()) InstancesPlugin.exitInstance(r, r.getStore()); }
            dropInstance(f, inst);
        });
        else BY_INSTANCE.values().remove(f);
        removeHologram(f);
        World world = Universe.get().getWorld(f.world);
        if (world == null) { FAILLES.remove(f.id); save(); return; }
        world.execute(() -> overflow(f, world, why));
    }

    /** Debordement (version simple, decision du 2026-11-22) : le portail explose, un groupe de monstres du theme en sort. */
    private static void overflow(Faille f, World world, String why) {
        FailleDungeon d = f.dungeon();
        BlockType t = WorldBlockUtil.getBlockType(world, f.x, f.y, f.z);
        scheduleRestore(f);                                                                      // le portail explose : terrain d'origine
        Store<EntityStore> store = world.getEntityStore().getStore();
        Vector3d c = new Vector3d(f.x + 0.5, f.y + 0.5, f.z + 0.5);
        if (t == null || d == null) {                                                            // chunk endormi : rien ne sort
            FAILLES.remove(f.id); save();
            return;
        }
        try {
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                com.eldanior.system.skills.runes.spells.SpellFx.fx("Explosion_Big", new Vector3d(c.x + Math.cos(a) * 3, c.y, c.z + Math.sin(a) * 3), 2f, store, store);
            }
            com.eldanior.system.skills.runes.spells.SpellFx.sound2dNear("SFX_Goblin_World_Breach_Open", c, 120, store);
        } catch (Exception ignored) { }
        int lvl = (f.rank.minLevel + f.rank.maxLevel) / 2;
        for (FailleDungeon.Mob m : d.overflow)
            for (int i = 0; i < m.count(); i++) {
                double a = RNG.nextDouble() * Math.PI * 2, r = 1.5 + RNG.nextDouble() * 3;
                Vector3d p = new Vector3d(c.x + Math.cos(a) * r, f.y + 0.1, c.z + Math.sin(a) * r);
                try {
                    var pair = NPCPlugin.get().spawnNPC(store, m.role(), null, p, new Rotation3f(0, RNG.nextFloat() * 360f, 0));
                    Ref<EntityStore> ref = pair != null ? pair.first() : null;
                    if (ref == null || !ref.isValid()) continue;
                    store.putComponent(ref, EldaniorSystem.get().getMobLevelDataType(), new MobLevelData(lvl + RNG.nextInt(11) - 5, m.role(), true));
                    UUIDComponent u = store.getComponent(ref, UUIDComponent.getComponentType());
                    if (u != null) f.overflowMobs.add(u.getUuid());
                    Ref<EntityStore> target = nearestPlayer(world, p);
                    if (target != null) NpcAttitudes.hostile(ref, target, 10, store);
                } catch (Exception e) { EldaniorLogger.error("[Failles] debordement " + m.role(), e); }
            }
        broadcastNear(f.world, c, 400, "DÉBORDEMENT ! La " + f.title().toLowerCase() + " (" + f.coords() + ") a explosé : " + why
                + ". Des monstres en sortent !", "#e0574a");
        if (f.source == Faille.Source.NATURELLE) LAST_CLOSED.put(f.dungeonId, System.currentTimeMillis());
        f.overflowAt = System.currentTimeMillis();
        if (f.overflowMobs.isEmpty()) FAILLES.remove(f.id);
        save();
        EldaniorLogger.info("[Failles] debordement : " + f.title() + " (" + why + ")");
    }

    /** Fin forcee par l'admin (sans debordement). */
    private static void finish(Faille f, String why) {
        f.state = Faille.State.FERMEE;
        f.bossBar.hide(); f.miniBar.hide();
        closeOutside(f);
        World inst = f.instanceName != null ? Universe.get().getWorld(f.instanceName) : null;
        if (inst != null) inst.execute(() -> {
            for (PlayerRef pr : playersIn(inst)) { var r = pr.getReference(); if (r != null && r.isValid()) InstancesPlugin.exitInstance(r, r.getStore()); }
            dropInstance(f, inst);
        });
        World world = Universe.get().getWorld(f.world);
        if (world != null && !f.overflowMobs.isEmpty()) {
            List<UUID> mobs = new ArrayList<>(f.overflowMobs);
            world.execute(() -> {
                for (UUID u : mobs) { Ref<EntityStore> r = world.getEntityStore().getRefFromUUID(u); if (r != null && r.isValid()) world.getEntityStore().getStore().removeEntity(r, RemoveReason.REMOVE); }
            });
        }
        FAILLES.remove(f.id);
        save();
    }

    // =====================================================================================================================
    // Apparitions naturelles
    // =====================================================================================================================

    /** Ratio : au plus ceil(joueurs / 3) failles ouvertes (8 max). force : panneau admin (ignore le delai d'1 h et le hasard). */
    public static String naturalSpawn(boolean force) {
        List<PlayerRef> candidates = new ArrayList<>();
        int online = 0;
        FailleRank top = FailleRank.F;
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r == null || !r.isValid()) continue;
            World w = r.getStore().getExternalData().getWorld();
            if (w == null || isRiftWorld(w.getName()) || w.getName().startsWith(InstancesPlugin.INSTANCE_PREFIX)) continue;
            online++;
            candidates.add(pr);
        }
        if (candidates.isEmpty()) return "Aucun joueur dans le monde.";
        int cap = Math.min(8, (int) Math.ceil(online / 3.0));
        long open = FAILLES.values().stream().filter(f -> f.source == Faille.Source.NATURELLE
                && (f.state == Faille.State.OUVERTE || f.state == Faille.State.EN_COURS)).count();
        if (!force && open >= cap) return "Déjà " + open + " faille(s) naturelle(s) pour " + online + " joueur(s).";
        if (!force && RNG.nextDouble() < 0.4) return "Pas de faille cette fois (hasard).";
        PlayerRef who = candidates.get(RNG.nextInt(candidates.size()));
        Ref<EntityStore> ref = who.getReference();
        if (ref == null) return "Joueur introuvable.";
        PlayerLevelData data = ref.getStore().getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        FailleRank playerRank = FailleRank.forPlayerLevel(data != null ? data.getLevel() : 1);
        // donjon du rang du joueur (jamais au-dessus), pas revenu depuis moins d'1 h
        long now = System.currentTimeMillis();
        FailleDungeon pick = null;
        for (FailleDungeon d : FailleDungeon.all().values()) {
            if (d.rank.ordinal() > playerRank.ordinal()) continue;
            Long last = LAST_CLOSED.get(d.id);
            if (!force && last != null && now - last < SAME_DUNGEON_COOLDOWN_MS) continue;
            boolean busy = FAILLES.values().stream().anyMatch(f -> f.dungeonId.equals(d.id)
                    && (f.state == Faille.State.OUVERTE || f.state == Faille.State.EN_COURS));
            if (busy && !force) continue;
            if (pick == null || d.rank.ordinal() > pick.rank.ordinal() || d.rank == pick.rank && RNG.nextBoolean()) pick = d;
        }
        if (pick == null) return "Aucun donjon disponible pour le rang " + playerRank.label + " (délai d'une heure entre deux apparitions du même donjon).";
        FailleDungeon dungeon = pick;
        World world = ref.getStore().getExternalData().getWorld();
        Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(who.getUuid());
        if (at == null) return "Position du joueur inconnue.";
        CompletableFuture<String> res = new CompletableFuture<>();
        world.execute(() -> {
            for (int i = 0; i < 30; i++) {
                double a = RNG.nextDouble() * Math.PI * 2, dist = 64 + RNG.nextDouble() * 136;
                int x = (int) Math.floor(at.x + Math.cos(a) * dist), z = (int) Math.floor(at.z + Math.sin(a) * dist);
                Integer y = surface(world, x, z, (int) Math.floor(at.y));
                if (y == null || inCity(world.getName(), x + 0.5, y + 1.0, z + 0.5)) continue;
                res.complete(open(world, x, y + 1, z, dungeon.rank, DEFAULT_GROUP, Faille.Source.NATURELLE, dungeon.id, ADMIN_DELAY_S));
                return;
            }
            res.complete("Pas de lieu sauvage chargé autour de " + who.getUsername() + ".");
        });
        return "Faille de rang " + dungeon.rank.label + " (" + dungeon.name + ") demandée près de " + who.getUsername() + ".";
    }

    // =====================================================================================================================
    // Outils
    // =====================================================================================================================

    static boolean inCity(String world, double x, double y, double z) {
        for (ParcelData p : DomainLaws.chain(world, x, y, z)) if (p.getType() == ParcelType.CITY) return true;
        for (ParcelData c : ParcelManager.getByType(ParcelType.CITY)) {
            if (!world.equals(c.getWorld())) continue;
            double dx = Math.max(Math.max(c.getMinX() - x, 0), x - c.getMaxX()), dz = Math.max(Math.max(c.getMinZ() - z, 0), z - c.getMaxZ());
            if (Math.hypot(dx, dz) <= 16 && y >= c.getMinY() - 8 && y <= c.getMaxY() + 8) return true;
        }
        return false;
    }

    /** Sol libre (2 cases d'air au-dessus, pas d'eau, pas dans un arbre) ; null si le chunk n'est pas charge. */
    static Integer surface(World world, int x, int z, int hintY) {
        if (WorldBlockUtil.getBlockType(world, x, Math.max(1, hintY), z) == null) return null;
        for (int y = Math.min(318, hintY + 30); y > Math.max(1, hintY - 40); y--) {
            BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
            if (type == null) return null;
            if (type == BlockType.EMPTY || "Empty".equals(type.getId())) continue;
            if (WorldBlockUtil.getFluidIndex(world, x, y + 1, z) > 0) return null;
            String id = type.getId();
            if (id.contains("Leaves") || id.startsWith("Plant_") || id.startsWith("Wood_")) return null;
            BlockType a1 = WorldBlockUtil.getBlockType(world, x, y + 1, z), a2 = WorldBlockUtil.getBlockType(world, x, y + 2, z);
            boolean free = (a1 == null || a1 == BlockType.EMPTY || "Empty".equals(a1.getId())) && (a2 == null || a2 == BlockType.EMPTY || "Empty".equals(a2.getId()));
            return free ? y : null;
        }
        return null;
    }

    private static Ref<EntityStore> nearestPlayer(World world, Vector3d from) {
        Ref<EntityStore> best = null;
        double bd = 60 * 60;
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            if (r == null || !r.isValid() || r.getStore().getExternalData().getWorld() != world) continue;
            Vector3d at = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (at != null && at.distanceSquared(from) < bd) { bd = at.distanceSquared(from); best = r; }
        }
        return best;
    }

    /** XP et argent (compte en banque) d'un joueur en ligne. */
    private static void pay(UUID u, long money, int xp, String message) {
        PlayerRef pr = Universe.get().getPlayer(u);
        Ref<EntityStore> r = pr != null ? pr.getReference() : null;
        if (r == null || !r.isValid()) return;
        World w = r.getStore().getExternalData().getWorld();
        w.execute(() -> {
            if (!r.isValid()) return;
            var type = EldaniorSystem.get().getPlayerLevelDataType();
            var data = r.getStore().getComponent(r, type);
            if (data == null) return;
            var copy = (PlayerLevelData) data.clone();
            if (copy == null) return;
            if (money > 0) copy.addMoney(money);
            if (xp > 0) copy.addExperience(xp);
            r.getStore().putComponent(r, type, copy);
            pr.sendMessage(Message.raw("[Faille] " + message).color("#00e7b6"));
        });
    }

    private static void broadcastNear(String world, Vector3d at, double range, String text, String color) {
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> r = pr.getReference();
            World w = r != null && r.isValid() ? r.getStore().getExternalData().getWorld() : null;
            Vector3d p = PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
            if (w == null || !w.getName().equals(world) || p == null || p.distanceSquared(at) > range * range) continue;
            pr.sendMessage(Message.raw("[Faille] " + text).color(color));
        }
    }

    /** Pour la carte : la faille d'un monde d'instance (null : pas une faille). */
    static Faille ofInstance(String worldName) { return BY_INSTANCE.get(worldName); }

    static Map<String, Faille> byInstance() { return BY_INSTANCE; }

    @SuppressWarnings("unused")
    private static Map<String, Object> debug(Faille f) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", f.id); m.put("etat", f.state); m.put("entres", f.entered.size());
        return m;
    }
}
