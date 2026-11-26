package com.eldanior.system.territory.borne;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Bornes de territoire (decision du 2026-11-10, docs/plan-bornes-territoire.md) : un bloc invisible (Eldanior_Borne_Territoire,
 * admins) marque le centre d'un royaume, grand territoire, territoire ou ville et porte ses reglages (BornePage). Le lien est
 * garde dans la zone elle-meme (ParcelData.borne = "monde|x|y|z", sauvegarde avec les parcelles).
 *
 * Regle validee : la borne doit rester posee pour que la zone existe. La casser supprime la zone (BorneEvents) ; si elle
 * disparait autrement (explosion, edition du monde), la zone est supprimee apres deux constats a 10 s d'intervalle, chunk charge.
 * Les sous-zones remontent au parent (ParcelManager.deleteParcel). Les zones creees par commande (sans borne) ne changent pas.
 * Marques : chaque seconde, pour les admins qui tiennent une borne en main, point sur chaque borne et pointilles le long des
 * bords des zones proches (rectangle ou cercle).
 */
public final class Bornes {

    private Bornes() {}

    public static final String ITEM = "Eldanior_Borne_Territoire";
    /** Particules par type de zone (gen_fx_martiaux.py, retour du 2026-11-10) : colonne lumineuse sur la borne, piquets de bord.
     *  Royaume or, grand territoire violet, territoire bleu, ville vert. */
    private static final String COLUMN = "Borne_Colonne_", POST = "Borne_Bord_";
    private static final double VIEW_RANGE = 48.0, EDGE_STEP = 8.0;
    private static final int MAX_POINTS = 240;
    /** Contour bloc par bloc (2026-11-19, « voir les aretes de chaque bloc qui delimite le territoire ») : cubes en fil de fer. */
    private static final int OUTLINE_RANGE = 40, MAX_CUBES = 900;
    /**
     * Types de zone poses par borne. 2026-11-19 (demande de l'utilisateur) : toutes les zones LARGES — royaume, grand territoire,
     * territoire, ville, foret, arene, parcelle. Les zones PRECISES (logement, chambre, ferme, mine, donjon) restent a la main,
     * bloc par bloc (/es pcreate et la selection). Un royaume pose cree encore ses 4 marquisats (BornePage.marquisates).
     */
    public static final List<ParcelType> TYPES = List.of(ParcelType.KINGDOM, ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY,
            ParcelType.CITY, ParcelType.FOREST, ParcelType.ARENA, ParcelType.PLOT);
    /** Zones qu'une borne peut lier (zones existantes creees par commande). */
    static final List<ParcelType> LINKABLE = TYPES;

    /** Zones dont la borne manquait au dernier controle. */
    private static final Set<String> missing = ConcurrentHashMap.newKeySet();

    public static void init() {
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { markers(); } catch (Exception e) { EldaniorLogger.error("[Borne] marques", e); }
        }, 6, 1, TimeUnit.SECONDS);
        EldaniorLogger.SCHEDULER.scheduleAtFixedRate(() -> {
            try { checkStanding(); } catch (Exception e) { EldaniorLogger.error("[Borne] controle", e); }
        }, 30, 10, TimeUnit.SECONDS);
    }

    public static String key(String world, int x, int y, int z) { return world + "|" + x + "|" + y + "|" + z; }

    static String world(String key) { return key.substring(0, key.indexOf('|')); }

    static int[] coords(String key) {
        String[] k = key.split("\\|");
        return new int[]{Integer.parseInt(k[1]), Integer.parseInt(k[2]), Integer.parseInt(k[3])};
    }

    static boolean isBorne(BlockType type) {
        String id = type != null ? type.getId() : null;
        return id != null && (id.equals(ITEM) || id.endsWith("*" + ITEM) || id.startsWith(ITEM + "_State"));
    }

    /** Zone reglee par cette borne, null sinon. */
    public static ParcelData zoneOf(String key) {
        if (key == null) return null;
        for (ParcelData p : ParcelManager.getAll()) if (key.equals(p.getBorne())) return p;
        return null;
    }

    /** Zone de type reglable qui contient la borne et n'a pas encore de borne (proposee au lien), null sinon. */
    static ParcelData linkable(String key) {
        int[] c = coords(key);
        String world = world(key);
        ParcelData best = null;
        long bestVolume = Long.MAX_VALUE;
        for (ParcelData p : ParcelManager.getAll()) {
            if (!world.equals(p.getWorld()) || !LINKABLE.contains(p.getType()) || !p.getBorne().isEmpty() || !p.contains(c[0], c[1], c[2])) continue;
            long v = (long) (p.getMaxX() - p.getMinX() + 1) * (p.getMaxZ() - p.getMinZ() + 1);
            if (v < bestVolume) { bestVolume = v; best = p; }
        }
        return best;
    }

    /** Carte du monde : les images des chunks sont peintes a leur generation ; on les efface pour qu'elles soient refaites. */
    static void refreshMap(String worldName) {
        World world = worldName != null ? Universe.get().getWorld(worldName) : null;
        if (world != null) world.execute(() -> com.eldanior.system.territory.systems.ParcelMapSystem.refreshMap(world));
    }

    /** La borne a ete cassee (ou a disparu) : sa zone est supprimee, les sous-zones remontent au parent. */
    static ParcelData remove(String key) {
        ParcelData p = zoneOf(key);
        if (p == null) return null;
        ParcelManager.deleteParcel(p.getId());
        refreshMap(world(key));
        EldaniorLogger.info("[Borne] zone supprimee avec sa borne : " + p.getName() + " (" + p.getId() + ")");
        return p;
    }

    /** Toutes les 10 s : une borne absente deux fois de suite (chunk charge) emporte sa zone. */
    private static void checkStanding() {
        Map<String, List<ParcelData>> byWorld = new ConcurrentHashMap<>();
        for (ParcelData p : ParcelManager.getAll())
            if (!p.getBorne().isEmpty()) byWorld.computeIfAbsent(world(p.getBorne()), w -> new ArrayList<>()).add(p);
        for (var e : byWorld.entrySet()) {
            World world = Universe.get().getWorld(e.getKey());
            if (world == null) continue;
            world.execute(() -> {
                for (ParcelData p : e.getValue()) {
                    int[] c = coords(p.getBorne());
                    BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                    if (t == null) continue;                                       // chunk non charge : on ne sait pas
                    if (isBorne(t)) { missing.remove(p.getId()); continue; }
                    if (missing.add(p.getId())) continue;                          // premier constat : on attend le suivant
                    missing.remove(p.getId());
                    ParcelManager.deleteParcel(p.getId());
                    com.eldanior.system.territory.systems.ParcelMapSystem.refreshMap(world);
                    EldaniorLogger.warn("[Borne] borne absente : zone " + p.getName() + " (" + p.getId() + ") supprimee");
                }
            });
        }
    }

    /** Points du bord d'une zone (au niveau y), dans le rayon de vue de pos. */
    private static List<Vector3d> edge(ParcelData p, double y, Vector3d pos) {
        List<Vector3d> out = new ArrayList<>();
        double x1 = p.getMinX(), x2 = p.getMaxX() + 1, z1 = p.getMinZ(), z2 = p.getMaxZ() + 1;
        double r2 = VIEW_RANGE * VIEW_RANGE;
        if (p.isCylinder()) {
            double cx = (x1 + x2) / 2, cz = (z1 + z2) / 2, r = (x2 - x1) / 2;
            int n = (int) Math.min(4000, Math.max(16, 2 * Math.PI * r / EDGE_STEP));
            for (int i = 0; i < n && out.size() < MAX_POINTS; i++) {
                double a = i * 2 * Math.PI / n;
                Vector3d v = new Vector3d(cx + Math.cos(a) * r, y, cz + Math.sin(a) * r);
                if (v.distanceSquared(pos) <= r2) out.add(v);
            }
            return out;
        }
        for (double x = x1; x <= x2 && out.size() < MAX_POINTS; x += EDGE_STEP) {
            for (double z : new double[]{z1, z2}) { Vector3d v = new Vector3d(x, y, z); if (v.distanceSquared(pos) <= r2) out.add(v); }
        }
        for (double z = z1; z <= z2 && out.size() < MAX_POINTS; z += EDGE_STEP) {
            for (double x : new double[]{x1, x2}) { Vector3d v = new Vector3d(x, y, z); if (v.distanceSquared(pos) <= r2) out.add(v); }
        }
        return out;
    }

    /**
     * Chaque seconde : bornes et bords des zones, vus des seuls admins qui tiennent une borne en main. Colonne de lumiere sur
     * chaque borne, piquets tous les 8 blocs (repere de loin), et contour bloc par bloc en fil de fer (ZoneOutline) de TOUTES
     * les zones proches, bornees ou non (logements, chambres, fermes, mines comprises : utile pour les regler a la main).
     */
    private static void markers() {
        List<ParcelData> zones = new ArrayList<>(ParcelManager.getAll());
        if (zones.isEmpty()) return;
        for (PlayerRef pr : Universe.get().getPlayers()) {
            if (!pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) continue;
            Ref<EntityStore> r = pr.getReference();
            if (r == null || !r.isValid()) continue;
            World world = r.getStore().getExternalData().getWorld();
            if (world == null) continue;
            world.execute(() -> {
                if (!r.isValid()) return;
                Store<EntityStore> store = r.getStore();
                var player = store.getComponent(r, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                var hand = player != null ? player.getInventory().getActiveHotbarItem() : null;
                if (hand == null || hand.isEmpty() || !ITEM.equals(hand.getItemId())) return;
                Vector3d pos = com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.get(pr.getUuid());
                if (pos == null) return;
                List<Ref<EntityStore>> viewer = List.of(r);
                int cubes = 0;
                // les plus petites zones d'abord : leur contour passe avant celui du royaume si la limite de cubes est atteinte
                zones.sort(java.util.Comparator.comparingLong(z -> (long) (z.getMaxX() - z.getMinX() + 1) * (z.getMaxZ() - z.getMinZ() + 1)));
                for (ParcelData p : zones) {
                    if (!world.getName().equals(p.getWorld())) continue;
                    try {
                        String type = p.getType().name();
                        if (!p.getBorne().isEmpty()) {
                            int[] c = coords(p.getBorne());
                            Vector3d at = new Vector3d(c[0] + 0.5, c[1] + 0.05, c[2] + 0.5);
                            if (at.distanceSquared(pos) <= VIEW_RANGE * VIEW_RANGE * 4)     // colonne lumineuse sur la borne
                                ParticleUtil.spawnParticleEffect(COLUMN + type, at.x, at.y, at.z, 0f, 0f, 0f, 1f, null, null, viewer, store, 1.6f);
                            for (Vector3d v : edge(p, pos.y - 0.4, pos))              // piquets le long du bord (repere de loin)
                                ParticleUtil.spawnParticleEffect(POST + type, v.x, v.y, v.z, 0f, 0f, 0f, 1f, null, null, viewer, store, 1.6f);
                        }
                        if (cubes < MAX_CUBES) cubes += ZoneOutline.draw(pr, world, p, pos, OUTLINE_RANGE, MAX_CUBES - cubes);
                    } catch (Exception ex) {
                        EldaniorLogger.error("[Borne] marque", ex);
                    }
                }
            });
        }
    }
}
