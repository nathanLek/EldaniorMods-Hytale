package com.eldanior.system.territory.systems;

import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.worldmap.ClearWorldMap;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.TintComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.universe.world.map.WorldMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Affiche les zones (Royaume, Territoire, Ville, Arene) sur la map native Hytale.
 *
 * Feature A : Marqueurs colores au centre de chaque zone
 * Feature B : Overlay colore applique lors de la generation des tuiles (wrapper IWorldMap)
 *
 * L'overlay est applique UNIQUEMENT lors de la generation des chunks (pas de post-processing).
 * Pour forcer un refresh apres modification de parcelles, utiliser refreshMap().
 */
public class ParcelMapSystem {

    private static final Set<ParcelType> MAP_TYPES = EnumSet.of(
            ParcelType.KINGDOM, ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY, ParcelType.CITY, ParcelType.ARENA, ParcelType.FOREST
    );

    private static final int BLOCKS_PER_CHUNK = 32;

    // ==================== REGISTRATION ====================

    public static void onWorldStart(StartWorldEvent event) {
        World world = event.getWorld();
        if (world == null) return;

        try {
            WorldMapManager mapManager = world.getWorldMapManager();
            if (mapManager == null) return;

            // Feature A : marqueurs colores
            mapManager.addMarkerProvider("eldanior_zones", new ZoneMarkerProvider());
            // 2026-11-19 : Boss Mondial et failles d'invasion, marques comme les failles des gobelins du jeu
            mapManager.addMarkerProvider("eldanior_events", new EventMarkerProvider());

            // Feature B : overlay colore DESACTIVE pour le moment
            // L'overlay modifie les MapImage et cause des IndexOutOfRangeException cote client.
            // Les marqueurs (Feature A) fonctionnent correctement.
            // TODO: investiguer le format MapImage plus en detail avant de reactiver

            EldaniorLogger.info("[ParcelMap] Systeme de map installe pour monde: " + world.getName());
        } catch (Exception e) {
            EldaniorLogger.error("ParcelMapSystem.onWorldStart", e);
        }
    }

    // ==================== FEATURE A : MARKER PROVIDER ====================

    private static class ZoneMarkerProvider implements WorldMapManager.MarkerProvider {

        private long lastVersion = -1;
        private List<MapMarker> cachedMarkers = new ArrayList<>();

        @Override
        public void update(World world, Player player, MarkersCollector collector) {
            try {
                long currentVersion = ParcelManager.getVersion();
                if (currentVersion != lastVersion) {
                    rebuildMarkers();
                    lastVersion = currentVersion;
                }

                for (MapMarker marker : cachedMarkers) {
                    collector.addIgnoreViewDistance(marker);
                }

            } catch (Exception e) {
                EldaniorLogger.error("ZoneMarkerProvider.update", e);
            }
        }

        private void rebuildMarkers() {
            List<MapMarker> markers = new ArrayList<>();

            for (ParcelData parcel : ParcelManager.getAll()) {
                if (!MAP_TYPES.contains(parcel.getType())) continue;
                // territoires de l'empire Luo de nouveau marques sur la carte (retour du 2026-11-10 nuit : poses par la borne)

                double centerX = (parcel.getMinX() + parcel.getMaxX() + 1) / 2.0;
                double centerY = 100;
                double centerZ = (parcel.getMinZ() + parcel.getMaxZ() + 1) / 2.0;
                String borne = parcel.getBorne();                      // 2026-11-20 : le marqueur sur la borne (vrai centre de la zone)
                if (!borne.isEmpty()) {
                    String[] k = borne.split("\\|");
                    if (k.length == 4) try {
                        centerX = Integer.parseInt(k[1]) + 0.5; centerY = Integer.parseInt(k[2]); centerZ = Integer.parseInt(k[3]) + 0.5;
                    } catch (NumberFormatException ignored) { }
                }

                Color color = getMarkerColor(parcel.getType());
                boolean realm = parcel == com.eldanior.system.luo.LuoClanSystem.luoRealm(parcel);
                String label = parcel.getName() + " [" + (realm ? "Empire du Clan Luo" : parcel.getType().getLabel()) + "]";

                try {
                    Transform transform = new Transform(
                            new Vector3d(centerX, centerY, centerZ),
                            new com.hypixel.hytale.math.vector.Rotation3f(0, 0, 0)
                    );

                    MapMarkerBuilder builder = new MapMarkerBuilder(
                            "zone_" + parcel.getId(),
                            "Spawn.png",
                            transform
                    );

                    builder.withCustomName(label);
                    builder.withComponent(new TintComponent(color));
                    // 2026-11-19 (demande de l'utilisateur) : plus de marqueurs de zone dans la boussole, seulement sur la carte
                    builder.withCompassImage(EMPTY_COMPASS);

                    markers.add(builder.build());
                } catch (Exception e) {
                    EldaniorLogger.error("ZoneMarkerProvider.build " + parcel.getId(), e);
                }
            }

            cachedMarkers = markers;
            EldaniorLogger.info("[ParcelMap] Marqueurs reconstruits: " + markers.size() + " zones");
        }
    }

    /** Image transparente (scripts/territoire/gen_evenements.py) : le marqueur n'apparait pas dans la boussole. */
    static final String EMPTY_COMPASS = "Eldanior_Vide.png";

    /** Boss Mondial apparu et failles d'invasion ouvertes (icones de la faille des gobelins du jeu pour les failles). */
    private static class EventMarkerProvider implements WorldMapManager.MarkerProvider {
        @Override
        public void update(World world, Player player, MarkersCollector collector) {
            try {
                MapMarker boss = com.eldanior.system.boards.WorldBosses.marker(world.getName());
                if (boss != null) collector.addIgnoreViewDistance(boss);
                for (Object[] r : com.eldanior.system.failles.FailleManager.markers(world.getName())) {   // failles-donjons
                    MapMarkerBuilder b = new MapMarkerBuilder("eldanior_fd_" + r[0], (String) r[4],
                            new Transform(new Vector3d((int) r[1] + 0.5, (int) r[2], (int) r[3] + 0.5), new com.hypixel.hytale.math.vector.Rotation3f(0, 0, 0)));
                    b.withCustomName((String) r[6]);
                    b.withCompassImage((String) r[5]);
                    collector.addIgnoreViewDistance(b.build());
                }
                for (Object[] r : com.eldanior.system.territory.laws.Invasions.rifts()) {
                    if (!world.getName().equals(r[0])) continue;
                    MapMarkerBuilder b = new MapMarkerBuilder("eldanior_faille_" + r[2] + "_" + r[4], "GoblinBreach.png",
                            new Transform(new Vector3d((int) r[2] + 0.5, (int) r[3], (int) r[4] + 0.5), new com.hypixel.hytale.math.vector.Rotation3f(0, 0, 0)));
                    b.withCustomName("Faille d'invasion (" + r[1] + ")");
                    b.withCompassImage("GoblinBreachCompass.png");
                    collector.addIgnoreViewDistance(b.build());
                }
            } catch (Exception e) {
                EldaniorLogger.error("EventMarkerProvider.update", e);
            }
        }
    }

    // ==================== FEATURE B : ZONE OVERLAY WORLD MAP ====================

    /**
     * Wrapper qui applique l'overlay colore lors de la generation de chaque chunk.
     * C'est le SEUL endroit ou les images sont modifiees - pas de post-processing.
     */
    private static class ZoneOverlayWorldMap implements IWorldMap {

        private final IWorldMap delegate;

        ZoneOverlayWorldMap(IWorldMap delegate) {
            this.delegate = delegate;
        }

        @Override
        public WorldMapSettings getWorldMapSettings() {
            return delegate.getWorldMapSettings();
        }

        @Override
        public CompletableFuture<WorldMap> generate(World world, int minChunkX, int minChunkZ, LongSet chunkIndices) {
            return delegate.generate(world, minChunkX, minChunkZ, chunkIndices)
                    .thenApply(worldMap -> {
                        try {
                            Long2ObjectMap<MapImage> chunks = worldMap.getChunks();
                            if (chunks != null) {
                                for (Long2ObjectMap.Entry<MapImage> entry : chunks.long2ObjectEntrySet()) {
                                    MapImage image = entry.getValue();
                                    if (image == null) continue;

                                    long idx = entry.getLongKey();
                                    int cx = (int) (idx >> 32);
                                    int cz = (int) idx;

                                    paintChunkOverlay(image, cx, cz);
                                }
                            }
                        } catch (Exception e) {
                            EldaniorLogger.error("ZoneOverlay.paint", e);
                        }
                        return worldMap;
                    });
        }

        @Override
        public CompletableFuture<Map<String, MapMarker>> generatePointsOfInterest(World world) {
            return delegate.generatePointsOfInterest(world);
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }
    }

    /**
     * Applique l'overlay de toutes les parcelles qui intersectent ce chunk.
     */
    private static void paintChunkOverlay(MapImage image, int chunkX, int chunkZ) {
        if (image.palette == null || image.packedIndices == null) return;

        int chunkBlockMinX = chunkX * BLOCKS_PER_CHUNK;
        int chunkBlockMinZ = chunkZ * BLOCKS_PER_CHUNK;
        int chunkBlockMaxX = chunkBlockMinX + BLOCKS_PER_CHUNK - 1;
        int chunkBlockMaxZ = chunkBlockMinZ + BLOCKS_PER_CHUNK - 1;

        float scaleX = (float) image.width / BLOCKS_PER_CHUNK;
        float scaleZ = (float) image.height / BLOCKS_PER_CHUNK;

        for (ParcelData parcel : ParcelManager.getAll()) {
            if (!MAP_TYPES.contains(parcel.getType())) continue;
            if (com.eldanior.system.luo.LuoClanSystem.hiddenInLuoRealm(parcel)) continue;   // empire Luo : pas de bordure de duche

            if (parcel.getMaxX() < chunkBlockMinX || parcel.getMinX() > chunkBlockMaxX) continue;
            if (parcel.getMaxZ() < chunkBlockMinZ || parcel.getMinZ() > chunkBlockMaxZ) continue;

            paintParcel(image, parcel, chunkBlockMinX, chunkBlockMinZ, scaleX, scaleZ);
        }
    }

    /**
     * Peint une zone sur l'image d'un chunk, pixel par pixel (2026-11-10, bornes de territoire) : bordure seulement sur le vrai
     * bord de la zone (avant : chaque chunk recevait son propre cadre), cercle pour une zone cylindrique. Plus de teinte a
     * l'interieur (les hachures genaient, demande de l'utilisateur).
     */
    private static void paintParcel(MapImage image, ParcelData parcel, int chunkMinX, int chunkMinZ, float scaleX, float scaleZ) {
        if (image.bitsPerIndex <= 0 || image.bitsPerIndex > 8) return;
        int borderIdx = findClosestPaletteEntry(image.palette, getBorderColorRGBA(parcel.getType()));
        double border = 2.0 / Math.max(0.01, scaleX);                         // 2 pixels, en blocs
        double x1 = parcel.getMinX(), x2 = parcel.getMaxX() + 1, z1 = parcel.getMinZ(), z2 = parcel.getMaxZ() + 1;
        double cx = (x1 + x2) / 2, cz = (z1 + z2) / 2, r = (x2 - x1) / 2;
        for (int pz = 0; pz < image.height; pz++) {
            double wz = chunkMinZ + (pz + 0.5) / scaleZ;
            for (int px = 0; px < image.width; px++) {
                double wx = chunkMinX + (px + 0.5) / scaleX;
                double edge;                                                   // distance au bord (negative dehors)
                if (parcel.isCylinder()) edge = r - Math.hypot(wx - cx, wz - cz);
                else edge = Math.min(Math.min(wx - x1, x2 - wx), Math.min(wz - z1, z2 - wz));
                if (edge < 0) continue;
                int i = pz * image.width + px;
                if (i >= image.packedIndices.length) continue;
                if (edge < border) image.packedIndices[i] = (byte) (borderIdx & 0xFF);   // bordure seule : plus de hachures (2026-11-10)
            }
        }
    }

    // ==================== PIXEL OPERATIONS ====================

    /**
     * Trouve l'entree palette la plus proche (distance RGB euclidienne).
     * Ne modifie PAS la palette — utilise seulement les couleurs existantes.
     */
    private static int findClosestPaletteEntry(int[] palette, int targetRGBA) {
        int tR = (targetRGBA >> 24) & 0xFF;
        int tG = (targetRGBA >> 16) & 0xFF;
        int tB = (targetRGBA >> 8) & 0xFF;

        int bestIdx = 0;
        int bestDist = Integer.MAX_VALUE;

        for (int i = 0; i < palette.length; i++) {
            int c = palette[i];
            int r = (c >> 24) & 0xFF;
            int g = (c >> 16) & 0xFF;
            int b = (c >> 8) & 0xFF;

            int dist = (r - tR) * (r - tR) + (g - tG) * (g - tG) + (b - tB) * (b - tB);
            if (dist == 0) return i;
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx = i;
            }
        }

        return bestIdx;
    }

    // ==================== REFRESH MAP ====================

    /**
     * Rafraichit la map : purge les images serveur + envoie ClearWorldMap aux clients.
     * A appeler UNIQUEMENT depuis l'admin apres reset/modification de parcelles.
     */
    public static void refreshMap(World world) {
        // Retour du 2026-11-10 (nuit) : vider les images (clearImages + ClearWorldMap) gelait la carte du client a chaque
        // creation de territoire (il fallait se reconnecter) ; le dessin des zones sur les images est desactive (Feature B),
        // il n'y a donc rien a redessiner. Les marqueurs se reconstruisent seuls (ParcelManager.getVersion change a la sauvegarde).
        if (world == null) return;
        EldaniorLogger.info("[ParcelMap] Zones modifiees : marqueurs reconstruits a la prochaine mise a jour (" + world.getName() + ")");
    }

    // ==================== COULEURS ====================

    private static Color getMarkerColor(ParcelType type) {
        return switch (type) {
            case KINGDOM -> new Color((byte) 0xFF, (byte) 0xD7, (byte) 0x00);       // Or
            case GRAND_TERRITORY -> new Color((byte) 0x1E, (byte) 0x90, (byte) 0xFF); // Bleu vif
            case TERRITORY -> new Color((byte) 0x34, (byte) 0x98, (byte) 0xDB);     // Bleu clair
            case CITY -> new Color((byte) 0x2E, (byte) 0xCC, (byte) 0x71);          // Vert
            case ARENA -> new Color((byte) 0xE7, (byte) 0x4C, (byte) 0x3C);         // Rouge
            case FOREST -> new Color((byte) 0x1E, (byte) 0x8A, (byte) 0x3A);        // Vert sombre (2026-11-19, forets posees par borne)
            default -> new Color((byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
        };
    }

    private static int getOverlayColorRGBA(ParcelType type) {
        return switch (type) {
            case KINGDOM -> 0xFFD700A0;
            case GRAND_TERRITORY -> 0x9B59FF90;
            case TERRITORY -> 0x3498DBA0;
            case CITY -> 0x2ECC71A0;
            case ARENA -> 0xE74C3CA0;
            default -> 0xFFFFFF60;
        };
    }

    private static int getBorderColorRGBA(ParcelType type) {
        return switch (type) {
            case KINGDOM -> 0xFFD700FF;
            case GRAND_TERRITORY -> 0x9B59FFFF;
            case TERRITORY -> 0x3498DBFF;
            case CITY -> 0x2ECC71FF;
            case ARENA -> 0xE74C3CFF;
            default -> 0xFFFFFFFF;
        };
    }
}
