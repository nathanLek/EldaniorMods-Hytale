package com.eldanior.system.territory.systems;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.palette.BitFieldArr;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.worldgen.biome.Biome;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.chunk.ZoneBiomeResult;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.concurrent.CompletableFuture;

/**
 * Esquisse de carte (2026-11-20, 5e retour : « ameliore encore le systeme pour la map, c'est toujours pas ca »).
 *
 * Constat (journal du serveur) : le royaume fait 100 806 chunks et seuls ~17 000 avaient une image. Le moteur dessine une image a
 * partir du chunk CHARGE et de ses 8 voisins (ImageBuilder) : pour un chunk jamais visite, il faut d'abord GENERER le terrain, ce
 * qui prenait plus de 2 min par paquet de 16 chunks (« paquet trop lent ») : le royaume n'aurait jamais ete pret.
 *
 * Ici l'image est calculee directement par le generateur de monde, sans creer aucun chunk : hauteur du terrain
 * (ChunkGenerator.getHeight), couleur de carte du biome (Biome.getMapColor, champ « MapColor » des biomes du jeu), eau (hauteur
 * d'eau du biome, plus sombre en profondeur) et relief ombre comme la carte du jeu (lumiere du nord-ouest). Un echantillon tous
 * les {@link #STEP} blocs. C'est une esquisse fidele aux reliefs et aux biomes (ni arbres ni constructions) : la vraie image la
 * remplace des qu'un joueur passe a proximite (KingdomMap).
 */
final class MapSketch {

    private MapSketch() {}

    /** Un echantillon de terrain tous les STEP blocs. */
    static final int STEP = 2;
    private static final int N = 32 / STEP;                 // echantillons par cote de chunk

    private static java.lang.reflect.Field generatorField;
    private static volatile boolean reflectFailed;

    /** Generateur de monde derriere le generateur de carte du jeu (GeneratorChunkWorldMap.generator), null si absent. */
    static ChunkGenerator generator(IWorldMap map) {
        if (map == null || reflectFailed) return null;
        try {
            for (Class<?> c = map.getClass(); c != null; c = c.getSuperclass()) {
                if (!c.getName().endsWith("GeneratorChunkWorldMap")) continue;
                if (generatorField == null) {
                    generatorField = c.getDeclaredField("generator");
                    generatorField.setAccessible(true);
                }
                return generatorField.get(map) instanceof ChunkGenerator g ? g : null;
            }
        } catch (Exception e) {
            reflectFailed = true;
            EldaniorLogger.warn("[Carte] generateur de monde introuvable (" + e + ") : pas d'esquisse");
        }
        return null;
    }

    /** Esquisses de ces chunks {cx, cz}, calculees sur les fils du generateur de monde (largeur d'image width). */
    static CompletableFuture<MapImage[]> draw(World world, ChunkGenerator gen, int[][] chunks, int width) {
        int seed = (int) world.getWorldConfig().getSeed();
        return gen.execute(seed, (s, g) -> {
            MapImage[] out = new MapImage[chunks.length];
            for (int i = 0; i < chunks.length; i++) {
                try { out[i] = sketch((ChunkGenerator) g, s, chunks[i][0], chunks[i][1], width); }
                catch (Exception e) { out[i] = null; }
            }
            return out;
        });
    }

    private static MapImage sketch(ChunkGenerator g, int seed, int cx, int cz, int width) {
        int bx = cx * 32, bz = cz * 32;
        int[] h = new int[(N + 2) * (N + 2)];                // hauteurs avec une bordure (ombre au bord du chunk)
        int[] color = new int[N * N];
        for (int j = -1; j <= N; j++)
            for (int i = -1; i <= N; i++) {
                int x = bx + i * STEP + STEP / 2, z = bz + j * STEP + STEP / 2;
                int height = g.getHeight(seed, x, z);
                h[(j + 1) * (N + 2) + (i + 1)] = height;
                if (i < 0 || j < 0 || i >= N || j >= N) continue;
                ZoneBiomeResult r = g.getZoneBiomeResultAt(seed, x, z);
                Biome b = r != null ? r.getBiome() : null;
                int rgb = b != null ? b.getMapColor() : 0x7a9a4a;
                int water = Integer.MIN_VALUE;
                if (b != null && b.getWaterContainer() != null && b.getWaterContainer().hasEntries())
                    water = b.getWaterContainer().getMaxHeight(seed, x, z);
                color[j * N + i] = water > height ? waterColor(water - height) : landColor(rgb, height);
            }
        int[] px = new int[width * width];
        for (int y = 0; y < width; y++) {
            int j = Math.min(N - 1, y * N / width);
            for (int x = 0; x < width; x++) {
                int i = Math.min(N - 1, x * N / width);
                int c = color[j * N + i];
                int k = (j + 1) * (N + 2) + (i + 1);
                boolean wet = (c & 1) == 1;                      // marque de l'eau (bit de poids faible du bleu)
                float shade = wet ? 1f : shade(h[k], h[k - 1], h[k + 1], h[k - (N + 2)], h[k + (N + 2)]);
                px[y * width + x] = pack(c, shade);
            }
        }
        return encode(width, px);
    }

    /** Couleur du biome, un peu plus claire en altitude. Bit 0 du bleu a 0 : terre. */
    private static int landColor(int rgb, int height) {
        float k = 0.86f + 0.28f * Math.clamp((height - 64) / 180f, 0f, 1f);
        int r = Math.min(255, Math.round(((rgb >> 16) & 255) * k)), gg = Math.min(255, Math.round(((rgb >> 8) & 255) * k)),
                b = Math.min(255, Math.round((rgb & 255) * k));
        return (r << 16) | (gg << 8) | (b & ~1);
    }

    /** Eau : claire sur les berges, sombre au large. Bit 0 du bleu a 1 : eau (pas d'ombre de relief). */
    private static int waterColor(int depth) {
        float t = Math.clamp(depth / 24f, 0f, 1f);
        int r = Math.round(70 + (22 - 70) * t), gg = Math.round(140 + (62 - 140) * t), b = Math.round(214 + (128 - 214) * t);
        return (r << 16) | (gg << 8) | (b | 1);
    }

    /** Ombre du relief (lumiere du nord-ouest), comme la carte du jeu. */
    private static float shade(int c, int west, int east, int north, int south) {
        float d = ((east - west) + (south - north)) / (2f * STEP);
        return Math.clamp(1f + d * 0.22f, 0.62f, 1.3f);
    }

    /** RGBA de la carte (r << 24 | g << 16 | b << 8 | a), couleurs arrondies a 8 (palette courte, comme le jeu). */
    private static int pack(int rgb, float shade) {
        int r = q(((rgb >> 16) & 255) * shade), g = q(((rgb >> 8) & 255) * shade), b = q((rgb & 255) * shade);
        return (r << 24) | (g << 16) | (b << 8) | 255;
    }

    private static int q(float v) { return Math.min(248, Math.max(0, Math.round(v / 8f) * 8)); }

    private static MapImage encode(int width, int[] px) {
        Int2IntOpenHashMap index = new Int2IntOpenHashMap();
        IntArrayList palette = new IntArrayList();
        for (int c : px) if (!index.containsKey(c)) { index.put(c, palette.size()); palette.add(c); }
        int n = palette.size(), bits = n <= 16 ? 4 : n <= 256 ? 8 : n <= 4096 ? 12 : 16;
        BitFieldArr arr = new BitFieldArr(bits, px.length);
        for (int i = 0; i < px.length; i++) arr.set(i, index.get(px[i]));
        return new MapImage(width, width, palette.toIntArray(), (byte) bits, arr.get());
    }
}
