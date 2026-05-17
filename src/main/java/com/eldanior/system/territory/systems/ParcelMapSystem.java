package com.eldanior.system.territory.systems;

import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
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
            ParcelType.KINGDOM, ParcelType.GRAND_TERRITORY, ParcelType.TERRITORY, ParcelType.CITY, ParcelType.ARENA
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

                double centerX = (parcel.getMinX() + parcel.getMaxX()) / 2.0;
                double centerY = (parcel.getMinY() + parcel.getMaxY()) / 2.0;
                double centerZ = (parcel.getMinZ() + parcel.getMaxZ()) / 2.0;

                Color color = getMarkerColor(parcel.getType());
                String label = parcel.getName() + " [" + parcel.getType().getLabel() + "]";

                try {
                    Transform transform = new Transform(
                            new Vector3d(centerX, centerY, centerZ),
                            new Vector3f(0, 0, 0)
                    );

                    MapMarkerBuilder builder = new MapMarkerBuilder(
                            "zone_" + parcel.getId(),
                            "Spawn.png",
                            transform
                    );

                    builder.withCustomName(label);
                    builder.withComponent(new TintComponent(color));

                    markers.add(builder.build());
                } catch (Exception e) {
                    EldaniorLogger.error("ZoneMarkerProvider.build " + parcel.getId(), e);
                }
            }

            cachedMarkers = markers;
            EldaniorLogger.info("[ParcelMap] Marqueurs reconstruits: " + markers.size() + " zones");
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

            if (parcel.getMaxX() < chunkBlockMinX || parcel.getMinX() > chunkBlockMaxX) continue;
            if (parcel.getMaxZ() < chunkBlockMinZ || parcel.getMinZ() > chunkBlockMaxZ) continue;

            int relMinX = Math.max(0, parcel.getMinX() - chunkBlockMinX);
            int relMinZ = Math.max(0, parcel.getMinZ() - chunkBlockMinZ);
            int relMaxX = Math.min(BLOCKS_PER_CHUNK - 1, parcel.getMaxX() - chunkBlockMinX);
            int relMaxZ = Math.min(BLOCKS_PER_CHUNK - 1, parcel.getMaxZ() - chunkBlockMinZ);

            int pixMinX = Math.max(0, (int) (relMinX * scaleX));
            int pixMinZ = Math.max(0, (int) (relMinZ * scaleZ));
            int pixMaxX = Math.min(image.width - 1, (int) ((relMaxX + 1) * scaleX) - 1);
            int pixMaxZ = Math.min(image.height - 1, (int) ((relMaxZ + 1) * scaleZ) - 1);

            int overlayRGBA = getOverlayColorRGBA(parcel.getType());
            int borderRGBA = getBorderColorRGBA(parcel.getType());
            tintImageRegion(image, pixMinX, pixMinZ, pixMaxX, pixMaxZ, overlayRGBA);
            drawBorder(image, pixMinX, pixMinZ, pixMaxX, pixMaxZ, borderRGBA, 2);
        }
    }

    // ==================== PIXEL OPERATIONS ====================

    private static void tintImageRegion(MapImage image, int minX, int minZ, int maxX, int maxZ, int tintRGBA) {
        int bpi = image.bitsPerIndex;
        if (bpi <= 0 || bpi > 8) return;

        int width = image.width;
        int mask = (1 << bpi) - 1;
        int maxPaletteIdx = (1 << bpi) - 1;

        int tintR = (tintRGBA >> 24) & 0xFF;
        int tintG = (tintRGBA >> 16) & 0xFF;
        int tintB = (tintRGBA >> 8) & 0xFF;
        float alpha = (tintRGBA & 0xFF) / 255.0f;

        // Pre-calculer le mapping de palette : ancien index -> nouvel index blende
        int[] paletteMapping = new int[image.palette.length];
        for (int i = 0; i < image.palette.length; i++) {
            int orig = image.palette[i];
            int origR = (orig >> 24) & 0xFF;
            int origG = (orig >> 16) & 0xFF;
            int origB = (orig >> 8) & 0xFF;
            int origA = orig & 0xFF;

            int blendR = (int) (origR * (1 - alpha) + tintR * alpha);
            int blendG = (int) (origG * (1 - alpha) + tintG * alpha);
            int blendB = (int) (origB * (1 - alpha) + tintB * alpha);

            int blended = (blendR << 24) | (blendG << 16) | (blendB << 8) | origA;
            paletteMapping[i] = findClosestPaletteEntry(image.palette, blended);
        }

        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                int pixelIndex = z * width + x;
                int byteIndex = pixelIndex;  // bpi=8 -> 1 pixel per byte

                if (byteIndex >= image.packedIndices.length) continue;

                int curIdx = image.packedIndices[byteIndex] & 0xFF;
                if (curIdx >= image.palette.length) continue;

                image.packedIndices[byteIndex] = (byte) (paletteMapping[curIdx] & 0xFF);
            }
        }
    }

    private static void drawBorder(MapImage image, int minX, int minZ, int maxX, int maxZ,
                                   int borderRGBA, int thickness) {
        if (image.palette == null || image.packedIndices == null) return;

        int width = image.width;
        int height = image.height;

        int borderIdx = findClosestPaletteEntry(image.palette, borderRGBA);

        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                boolean onBorder = (x < minX + thickness || x > maxX - thickness
                        || z < minZ + thickness || z > maxZ - thickness);
                if (!onBorder) continue;
                if (x < 0 || x >= width || z < 0 || z >= height) continue;

                int byteIndex = z * width + x;
                if (byteIndex >= image.packedIndices.length) continue;

                image.packedIndices[byteIndex] = (byte) (borderIdx & 0xFF);
            }
        }
    }

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
        if (world == null) return;
        try {
            WorldMapManager mapManager = world.getWorldMapManager();
            if (mapManager != null) {
                mapManager.clearImages();
            }

            ClearWorldMap clearPacket = new ClearWorldMap();
            for (java.util.UUID uuid :
                    com.eldanior.system.config.Player.PlayerPositionTracker.PLAYER_POSITIONS.keySet()) {
                try {
                    PlayerRef pRef = Universe.get().getPlayer(uuid);
                    if (pRef != null) pRef.getPacketHandler().writeNoCache(clearPacket);
                } catch (Exception ignored) {}
            }

            EldaniorLogger.info("[ParcelMap] Map rafraichie (serveur + clients)");
        } catch (Exception e) {
            EldaniorLogger.error("ParcelMapSystem.refreshMap", e);
        }
    }

    // ==================== COULEURS ====================

    private static Color getMarkerColor(ParcelType type) {
        return switch (type) {
            case KINGDOM -> new Color((byte) 0xFF, (byte) 0xD7, (byte) 0x00);       // Or
            case GRAND_TERRITORY -> new Color((byte) 0x1E, (byte) 0x90, (byte) 0xFF); // Bleu vif
            case TERRITORY -> new Color((byte) 0x34, (byte) 0x98, (byte) 0xDB);     // Bleu clair
            case CITY -> new Color((byte) 0x2E, (byte) 0xCC, (byte) 0x71);          // Vert
            case ARENA -> new Color((byte) 0xE7, (byte) 0x4C, (byte) 0x3C);         // Rouge
            default -> new Color((byte) 0xFF, (byte) 0xFF, (byte) 0xFF);
        };
    }

    private static int getOverlayColorRGBA(ParcelType type) {
        return switch (type) {
            case KINGDOM -> 0xFFD700A0;
            case TERRITORY -> 0x3498DBA0;
            case CITY -> 0x2ECC71A0;
            case ARENA -> 0xE74C3CA0;
            default -> 0xFFFFFF60;
        };
    }

    private static int getBorderColorRGBA(ParcelType type) {
        return switch (type) {
            case KINGDOM -> 0xFFD700FF;
            case TERRITORY -> 0x3498DBFF;
            case CITY -> 0x2ECC71FF;
            case ARENA -> 0xE74C3CFF;
            default -> 0xFFFFFFFF;
        };
    }
}
