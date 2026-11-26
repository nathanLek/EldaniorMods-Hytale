package com.eldanior.system.carte;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.procedurallib.file.FileIO;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunkSection;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import com.hypixel.hytale.server.worldgen.HytaleWorldGenProvider;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.WorldGenConfig;
import com.hypixel.hytale.server.worldgen.loader.AssetFileSystem;
import com.hypixel.hytale.server.worldgen.loader.ChunkGeneratorJsonLoader;
import com.hypixel.hytale.server.worldgen.prefab.PrefabStoreRoot;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;
import java.util.zip.GZIPInputStream;

/**
 * Générateur « Eldanior » : la carte du continent tirée d'une image (scripts/carte/carte_image.py), puis le monde normal
 * de Hytale au-delà.
 *
 * <p>Le continent est fait en deux temps :
 * <ol>
 *   <li>Le générateur V1 de Hytale lit Server/World/Eldanior (masque des zones) et produit, pour chaque zone, les sols,
 *   les plantes et les arbres sur un sol plat (Y ~120 sur terre, ~100 sous la mer) ;</li>
 *   <li>chaque colonne est ensuite déplacée à la hauteur voulue par Terrain.bin (relief, vallées, rivières, lacs, fonds
 *   marins), avec un bruit de détail (crêtes et pics en montagne), puis on pose l'eau, les plages, la neige et la roche
 *   des pentes raides. Les arbres suivent leur tronc, pour ne pas être déformés sur les pentes.</li>
 * </ol>
 * Au-delà de « Rayon » (distance en carré arrondi), c'est le générateur normal de Hytale ; dans la bande de
 * « Transition », ses terres sont abaissées sous la mer pour éviter un mur.
 */
public class EldaniorWorldGen {

    public static final String ID = "Eldanior";
    public static final int SEA = 114;
    static final int SOL_TERRE = 120, SOL_MER = 100;

    public static class Provider implements IWorldGenProvider {
        public static final BuilderCodec<Provider> CODEC = BuilderCodec.builder(Provider.class, Provider::new)
                .append(new KeyedCodec<>("Name", Codec.STRING), (o, s) -> o.name = s, o -> o.name).add()
                .append(new KeyedCodec<>("Rayon", Codec.INTEGER), (o, v) -> o.rayon = v, o -> o.rayon).add()
                .append(new KeyedCodec<>("Transition", Codec.INTEGER), (o, v) -> o.transition = v, o -> o.transition).add()
                .append(new KeyedCodec<>("Fond", Codec.INTEGER), (o, v) -> o.fond = v, o -> o.fond).add()
                .build();

        String name = "Eldanior";
        int rayon = 6100;
        int transition = 500;
        int fond = 78;

        @Override
        public IWorldGen getGenerator() throws WorldGenLoadException {
            Path dir = null;
            for (var pack : AssetModule.get().getAssetPacks()) {
                Path p = pack.getRoot().resolve("Server").resolve("World").resolve(name);
                if (Files.exists(p.resolve("World.json"))) {
                    dir = p;
                    break;
                }
            }
            if (dir == null) throw new WorldGenLoadException("Aucun pack ne contient Server/World/" + name + "/World.json");
            IWorldGen inner;
            try {
                WorldGenConfig config = new WorldGenConfig(dir, name, new Semver(0L, 0L, 0L));
                // World.json est lu avant que le chargeur n'ouvre son système de fichiers fusionné (tous les packs) :
                // on l'ouvre nous-mêmes pour que le fichier du pack du mod soit trouvé.
                AssetFileSystem fs = FileIO.openFileIOSystem(new AssetFileSystem(config));
                try {
                    inner = new ChunkGeneratorJsonLoader(new SeedString<>("ChunkGenerator",
                            new SeedStringResource(PrefabStoreRoot.DEFAULT, config)), config).load();
                } finally {
                    FileIO.closeFileIOSystem(fs);
                }
            } catch (Error e) {
                throw new WorldGenLoadException("Eldanior : chargement du continent impossible", e);
            }
            IWorldGen outer = new HytaleWorldGenProvider().getGenerator();
            Terrain terrain;
            try {
                terrain = Terrain.read(dir.resolve("Terrain.bin"));
            } catch (IOException e) {
                throw new WorldGenLoadException("Eldanior : Terrain.bin illisible : " + e);
            }
            return new Gen(inner, outer, rayon, transition, fond, terrain);
        }
    }

    // ------------------------------------------------------------------ catégories de blocs
    static final byte AIR = 0, SOL = 1, ARBRE = 2, PLANTE = 3;

    public static class Gen implements IWorldGen, IWorldMapProvider {
        final IWorldGen inner, outer;
        final int r, t, fond;
        final Terrain ter;
        final int water, stone, snow, sand, gravel, clay, pebbles, waterMax;
        final ConcurrentHashMap<Integer, Byte> cat = new ConcurrentHashMap<>();

        Gen(IWorldGen inner, IWorldGen outer, int r, int t, int fond, Terrain ter) {
            this.inner = inner; this.outer = outer; this.r = r; this.t = t; this.fond = fond; this.ter = ter;
            this.water = Fluid.getAssetMap().getIndex("Water_Source");
            this.waterMax = Fluid.getAssetMap().getAsset(water).getMaxFluidLevel();
            this.stone = BlockType.getAssetMap().getIndex("Rock_Stone");
            this.snow = BlockType.getAssetMap().getIndex("Soil_Snow");
            this.sand = BlockType.getAssetMap().getIndex("Soil_Sand");
            this.gravel = BlockType.getAssetMap().getIndex("Soil_Gravel");
            this.clay = BlockType.getAssetMap().getIndex("Soil_Clay");
            this.pebbles = BlockType.getAssetMap().getIndex("Soil_Pebbles");
        }

        byte cat(int id) {
            if (id == 0) return AIR;
            return cat.computeIfAbsent(id, k -> {
                BlockType bt = BlockType.getAssetMap().getAsset(k);
                String n = bt == null ? "" : bt.getId();
                if (n.startsWith("Soil_") || n.startsWith("Rock_") || n.startsWith("Ore_")) return SOL;
                if (n.startsWith("Wood_") || (n.startsWith("Plant_Leaves_") && !n.endsWith("_Floor"))) return ARBRE;
                return PLANTE;
            });
        }

        // ------------------------------------------------------------------ distances (carré arrondi)
        static double norm(double x, double z) {
            double ax = Math.abs(x), az = Math.abs(z);
            double m = Math.max(ax, az);
            if (m == 0) return 0;
            double a = ax / m, b = az / m;
            return m * Math.pow(a * a * a * a * a * a + b * b * b * b * b * b, 1.0 / 6);
        }

        // ------------------------------------------------------------------ relief voulu d'une colonne
        /** Hauteur du sol (Y) d'une colonne de terre, bruit de détail compris. */
        double landY(int x, int z, Terrain.Sample s) {
            double rug = s.rug;
            double h = s.h;
            // Crêtes : bruit « ridged » aiguisé, plus fort là où le terrain est déjà haut (pics).
            double ridge = 0, amp = 1, norm = 0, f = 1 / 110.0;
            for (int o = 0; o < 4; o++) {
                double n = 1 - Math.abs(Noise.perlin(x * f, z * f, 31 + o));
                ridge += amp * n * n * n;
                norm += amp; amp *= 0.5; f *= 2.05;
            }
            ridge /= norm;
            double hills = Noise.perlin(x / 140.0, z / 140.0, 41) * 0.7 + Noise.perlin(x / 47.0, z / 47.0, 42) * 0.3;
            double fine = Noise.perlin(x / 13.0, z / 13.0, 43);
            double peak = rug * (6 + 0.2 * Math.max(0, h)) * (ridge * 2.2 - 0.55);
            double d = peak + (2.5 + 4 * rug) * hills + (0.6 + 2.2 * rug) * fine;
            double atten = smooth(0, 45, s.sdf);
            return SEA + h + d * atten;
        }

        /** Échantillon de la carte avec une légère ondulation (rives et côtes moins rectilignes). */
        void sample(int x, int z, Terrain.Sample s) {
            double wx = 5.5 * Noise.perlin(x / 37.0, z / 37.0, 71) + 2.0 * Noise.perlin(x / 11.0, z / 11.0, 73);
            double wz = 5.5 * Noise.perlin(x / 37.0, z / 37.0, 72) + 2.0 * Noise.perlin(x / 11.0, z / 11.0, 74);
            ter.sample(x + wx, z + wz, s);
        }

        static double smooth(double a, double b, double v) {
            double k = Math.max(0, Math.min(1, (v - a) / (b - a)));
            return k * k * (3 - 2 * k);
        }

        /** Résultat pour une colonne : sol, niveau d'eau, mouillée, type de bord. */
        static final class Col {
            int g;          // Y du dernier bloc de sol
            int wl;         // Y de la surface de l'eau
            boolean wet;    // eau au-dessus du sol
            boolean sea;    // eau de mer (sinon rivière / lac)
            int beach;      // 0 aucun, 1 sable, 2 gravier
            double sdf;
        }

        Col column(int x, int z, Terrain.Sample s, Col c) {
            c.sdf = s.sdf;
            c.sea = s.sea;
            c.wl = (int) Math.floor(SEA + (s.sea ? 0 : s.wl) + 0.5);
            if (s.sdf < 0) {
                if (s.sea) {
                    double bed = SEA + s.h + 1.5 * Noise.perlin(x / 23.0, z / 23.0, 51);
                    c.g = (int) Math.floor(Math.min(bed, c.wl - 1));
                } else {
                    double depth = 1.5 + Math.min(5.5, -s.sdf * 0.32) + 0.8 * Noise.perlin(x / 9.0, z / 9.0, 52);
                    c.g = (int) Math.floor(Math.min(SEA + s.h, c.wl - depth));
                    c.g = Math.min(c.g, c.wl - 1);
                }
                c.wet = true;
            } else {
                double y = landY(x, z, s);
                if (s.sdf < 40) y = Math.max(y, c.wl + 1);
                c.g = (int) Math.floor(y);
                c.wet = false;
            }
            c.g = Math.max(8, Math.min(318, c.g));
            // Plages : sable au bord de la mer, sable ou gravier au bord des rivières et des lacs.
            c.beach = 0;
            double n = Noise.perlin(x / 31.0, z / 31.0, 53);
            if (s.sea) {
                if (s.sdf < 16 + 10 * n && c.g <= c.wl + 3) c.beach = (z < -3000 && n > 0.2) ? 2 : 1;
            } else if (s.sdf < 4 + 3 * n && c.g <= c.wl + 2) {
                c.beach = n > 0.25 ? 2 : 1;
            }
            return c;
        }

        /** Façonne un chunk du continent (sortie du générateur V1) selon Terrain.bin. */
        private GeneratedChunk shape(GeneratedChunk g, int cx, int cz) {
            GeneratedBlockChunk b = g.getBlockChunk();
            Holder<ChunkStore>[] sections = g.getSections();
            FluidSection[] fsec = new FluidSection[10];
            for (int i = 0; i < 10; i++) fsec[i] = sections[i].getComponent(FluidSection.getComponentType());
            int x0 = cx * 32, z0 = cz * 32;

            // 1) Relief voulu sur 34 x 34 (une colonne de marge pour les pentes).
            Terrain.Sample s = new Terrain.Sample();
            int[] gy = new int[34 * 34];
            Col tmp = new Col();
            for (int dz = -1; dz <= 32; dz++)
                for (int dx = -1; dx <= 32; dx++) {
                    sample(x0 + dx, z0 + dz, s);
                    gy[(dz + 1) * 34 + dx + 1] = column(x0 + dx, z0 + dz, s, tmp).g;
                }

            // 2) Sol d'origine (plat) de chaque colonne et troncs d'arbres.
            int[] src = new int[1024];          // Y du sol d'origine
            int[] off = new int[1024];          // décalage du sol
            boolean[] trunk = new boolean[1024];
            boolean[] oceanCol = new boolean[1024];
            for (int z = 0; z < 32; z++)
                for (int x = 0; x < 32; x++) {
                    int i = z * 32 + x;
                    boolean oceanSrc = fluidAt(fsec, x, SOL_MER + 6, z) != 0;
                    int base = oceanSrc ? SOL_MER : SOL_TERRE;
                    int top = base;
                    for (int y = base + 3; y >= base - 6; y--) {
                        if (cat(b.getBlock(x, y, z)) == SOL) { top = y; break; }
                    }
                    src[i] = top;
                    oceanCol[i] = oceanSrc;
                    off[i] = gy[(z + 1) * 34 + x + 1] - top;
                    trunk[i] = !oceanSrc && cat(b.getBlock(x, top + 1, z)) == ARBRE;
                }
            // Décalage des arbres : celui du tronc le plus proche (rayon 7), sinon celui de la colonne.
            int[] offTree = new int[1024];
            for (int z = 0; z < 32; z++)
                for (int x = 0; x < 32; x++) {
                    int best = Integer.MAX_VALUE, bo = off[z * 32 + x];
                    for (int dz = -7; dz <= 7; dz++) {
                        int zz = z + dz;
                        if (zz < 0 || zz >= 32) continue;
                        for (int dx = -7; dx <= 7; dx++) {
                            int xx = x + dx;
                            if (xx < 0 || xx >= 32 || !trunk[zz * 32 + xx]) continue;
                            int d2 = dx * dx + dz * dz;
                            if (d2 < best) { best = d2; bo = off[zz * 32 + xx]; }
                        }
                    }
                    offTree[z * 32 + x] = bo;
                }

            // 3) Réécriture des colonnes.
            int[] blk = new int[320], rot = new int[320], fil = new int[320], env = new int[320];
            int[] nb = new int[320], nr = new int[320], nf = new int[320];
            Col c = new Col();
            for (int z = 0; z < 32; z++) {
                for (int x = 0; x < 32; x++) {
                    int i = z * 32 + x;
                    int wx = x0 + x, wz = z0 + z;
                    sample(wx, wz, s);
                    column(wx, wz, s, c);
                    int G = c.g, S = src[i], d = G - S, dt = offTree[i];
                    for (int y = 0; y < 320; y++) {
                        blk[y] = b.getBlock(x, y, z);
                        rot[y] = b.getRotationIndex(x, y, z);
                        GeneratedChunkSection sec = b.getSection(y >> 5);
                        fil[y] = sec == null ? 0 : sec.getFiller(x, y, z);
                        env[y] = b.getEnvironment(x, y, z);
                        nb[y] = 0; nr[y] = 0; nf[y] = 0;
                    }
                    // Sol
                    for (int y = 0; y <= G; y++) {
                        int sy = y - d;
                        if (y == 0) { nb[0] = blk[0]; continue; }
                        if (sy < 1) { nb[y] = stone; continue; }
                        if (sy > S) { nb[y] = stone; continue; }
                        nb[y] = blk[sy]; nr[y] = rot[sy]; nf[y] = fil[sy];
                    }
                    // Plantes et arbres (au-dessus du sol d'origine)
                    double sl = slope(gy, x, z);
                    double snowY = ter.snowY + 12 * Math.max(-1, Math.min(1, wz / 5000.0)) + 9 * Noise.perlin(wx / 60.0, wz / 60.0, 61);
                    boolean bare = c.beach != 0 || (!c.wet && sl > 1.7);
                    boolean noTree = !c.wet && G > snowY - 10;                 // limite des arbres
                    for (int sy = S + 1; sy < 320; sy++) {
                        int id = blk[sy];
                        byte k = cat(id);
                        if (k == AIR) continue;
                        int ty = sy + (k == ARBRE ? dt : d);
                        if (ty <= G || ty >= 320) continue;
                        if (c.wet && !c.sea) continue;                     // rien dans les rivières et les lacs
                        if (c.wet != oceanCol[i]) continue;                // biome marin sur terre, ou l'inverse
                        if (c.wet && ty > c.wl) continue;                  // rien qui dépasse de la mer
                        if (!c.wet && k == PLANTE && (bare || G >= snowY)) continue;
                        if (!c.wet && k == ARBRE && ((c.beach != 0 && dt == d) || noTree)) continue;
                        nb[ty] = id; nr[ty] = rot[sy]; nf[ty] = fil[sy];
                    }
                    // Surface : plage, lit des rivières, roche des pentes raides, neige.
                    if (c.wet && !c.sea) {
                        double n = Noise.perlin(wx / 17.0, wz / 17.0, 62);
                        int bed = n > 0.3 ? clay : (n < -0.2 ? sand : gravel);
                        for (int y = Math.max(1, G - 2); y <= G; y++) nb[y] = bed;
                    } else if (c.beach != 0) {
                        int m = c.beach == 1 ? sand : gravel;
                        for (int y = Math.max(1, G - 3); y <= G; y++) nb[y] = m;
                    } else if (!c.wet) {
                        // Roche : pentes raides partout, pentes moyennes en altitude (plus bas que la neige).
                        double rockSlope = G > snowY - 40 ? 1.0 : (G > 170 ? 1.3 : 1.7);
                        boolean rock = sl > rockSlope;
                        if (rock) for (int y = Math.max(1, G - 3); y <= G; y++) nb[y] = stone;
                        if (G >= snowY && sl <= 1.6) {
                            nb[G] = snow;
                            if (G - 1 > 0 && cat(nb[G - 1]) == SOL) nb[G - 1] = snow;
                        }
                    }
                    // Écriture
                    for (int y = 0; y < 320; y++) {
                        b.setBlock(x, y, z, nb[y], nr[y], nf[y]);
                        int sy = Math.max(0, Math.min(319, y - d));
                        b.setEnvironment(x, y, z, env[sy]);
                    }
                    // Eau
                    for (int y = 0; y < 320; y++) {
                        FluidSection fs = fsec[y >> 5];
                        boolean wetY = c.wet && y > G && y <= c.wl;
                        if (wetY) {
                            if (fs == null) fs = fsec[y >> 5] = sections[y >> 5].ensureAndGetComponent(FluidSection.getComponentType());
                            fs.setFluid(x, y, z, water, (byte) waterMax);
                        } else if (fs != null && fs.getFluidId(x, y, z) != 0) {
                            fs.setFluid(x, y, z, 0, (byte) 0);
                        }
                    }
                }
            }
            return g;
        }

        private static int fluidAt(FluidSection[] fs, int x, int y, int z) {
            FluidSection f = fs[y >> 5];
            return f == null ? 0 : f.getFluidId(x, y, z);
        }

        /** Pente (blocs de dénivelé par bloc) au centre de la colonne. */
        private static double slope(int[] gy, int x, int z) {
            int i = (z + 1) * 34 + x + 1;
            double gx = (gy[i + 1] - gy[i - 1]) * 0.5, gz = (gy[i + 34] - gy[i - 34]) * 0.5;
            return Math.sqrt(gx * gx + gz * gz);
        }

        @Override
        public WorldGenTimingsCollector getTimings() { return inner.getTimings(); }

        @Override
        public Transform[] getSpawnPoints(int seed) { return inner.getSpawnPoints(seed); }

        @Override
        public IWorldMap getGenerator(World world) throws WorldMapLoadException {
            if (inner instanceof IWorldMapProvider p) return p.getGenerator(world);
            throw new WorldMapLoadException("pas de carte");
        }

        @Override
        public void shutdown() {
            inner.shutdown();
            outer.shutdown();
        }

        @Override
        public CompletableFuture<GeneratedChunk> generate(int seed, long index, int cx, int cz,
                                                          LongPredicate stillNeeded, Long2FloatFunction priority) {
            double nx = clamp(0, cx * 32, cx * 32 + 31), nz = clamp(0, cz * 32, cz * 32 + 31);
            double minD = norm(nx, nz), maxD = 0;
            for (int dx = 0; dx <= 1; dx++)
                for (int dz = 0; dz <= 1; dz++)
                    maxD = Math.max(maxD, norm(cx * 32 + dx * 31, cz * 32 + dz * 31));
            if (maxD < r) return inner.generate(seed, index, cx, cz, stillNeeded, priority).thenApply(g -> shape(g, cx, cz));
            if (minD >= r + t) return outer.generate(seed, index, cx, cz, stillNeeded, priority);
            CompletableFuture<GeneratedChunk> o = outer.generate(seed, index, cx, cz, stillNeeded, priority)
                    .thenApply(g -> { blend(g, cx, cz); return g; });
            if (minD >= r) return o;
            return inner.generate(seed, index, cx, cz, stillNeeded, priority).thenApply(g -> shape(g, cx, cz)).thenCombine(o, (in, out) -> {
                for (int x = 0; x < 32; x++)
                    for (int z = 0; z < 32; z++)
                        if (norm(cx * 32 + x, cz * 32 + z) >= r) copyColumn(out, in, x, z);
                return in;
            });
        }

        private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

        /** Abaisse les terres de Hytale dans la bande de transition, puis remet l'eau jusqu'au niveau de la mer. */
        private void blend(GeneratedChunk g, int cx, int cz) {
            GeneratedBlockChunk b = g.getBlockChunk();
            Holder<ChunkStore>[] sections = g.getSections();
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    int wx = cx * 32 + x, wz = cz * 32 + z;
                    double d = norm(wx, wz) + 0.45 * t * Noise.perlin(wx / 380.0, wz / 380.0, 5)
                            + 0.15 * t * Noise.perlin(wx / 90.0, wz / 90.0, 6);
                    if (d >= r + t) continue;
                    double k = Math.max(0, (d - r) / t);
                    k = k * k * (3 - 2 * k);
                    int top = -1;
                    for (int y = 319; y >= 0; y--) if (b.getBlock(x, y, z) != 0) { top = y; break; }
                    if (top < 0) continue;
                    int newTop = (int) Math.round(Math.min(top, fond + (top - fond) * k));
                    int delta = top - newTop;
                    if (delta > 0) {
                        for (int y = 0; y < 320; y++) {
                            int src = y + delta;
                            if (y < newTop - 40) continue;
                            if (src < 320) {
                                GeneratedChunkSection s = b.getSection(src >> 5);
                                int filler = s == null ? 0 : s.getFiller(x, src, z);
                                b.setBlock(x, y, z, b.getBlock(x, src, z), b.getRotationIndex(x, src, z), filler);
                            } else {
                                b.setBlock(x, y, z, 0, 0, 0);
                            }
                        }
                    }
                    int surface = -1;
                    for (int y = 319; y >= 0; y--) if (b.getBlock(x, y, z) != 0) { surface = y; break; }
                    for (int y = 0; y < 320; y++) {
                        FluidSection fs = sections[y >> 5].getComponent(FluidSection.getComponentType());
                        boolean wet = y > surface && y <= SEA;
                        if (wet) {
                            if (fs == null) fs = sections[y >> 5].ensureAndGetComponent(FluidSection.getComponentType());
                            fs.setFluid(x, y, z, water, (byte) waterMax);
                        } else if (fs != null && fs.getFluidId(x, y, z) != 0 && y > SEA) {
                            fs.setFluid(x, y, z, 0, (byte) 0);
                        }
                    }
                }
            }
        }

        /** Recopie une colonne (blocs, rotation, fluides, teinte, environnement) de src vers dst. */
        private static void copyColumn(GeneratedChunk src, GeneratedChunk dst, int x, int z) {
            GeneratedBlockChunk s = src.getBlockChunk(), d = dst.getBlockChunk();
            for (int y = 0; y < 320; y++) {
                GeneratedChunkSection sec = s.getSection(y >> 5);
                int filler = sec == null ? 0 : sec.getFiller(x, y, z);
                d.setBlock(x, y, z, s.getBlock(x, y, z), s.getRotationIndex(x, y, z), filler);
                d.setEnvironment(x, y, z, s.getEnvironment(x, y, z));
                FluidSection fsS = src.getSections()[y >> 5].getComponent(FluidSection.getComponentType());
                FluidSection fsD = dst.getSections()[y >> 5].getComponent(FluidSection.getComponentType());
                int fl = fsS == null ? 0 : fsS.getFluidId(x, y, z);
                if (fl != 0) {
                    if (fsD == null) fsD = dst.getSections()[y >> 5].ensureAndGetComponent(FluidSection.getComponentType());
                    fsD.setFluid(x, y, z, fl, fsS.getFluidLevel(x, y, z));
                } else if (fsD != null && fsD.getFluidId(x, y, z) != 0) {
                    fsD.setFluid(x, y, z, 0, (byte) 0);
                }
            }
            d.setTint(x, z, s.getTint(x, z));
        }
    }

    /** Données de la carte (Terrain.bin de carte_image.py), lues en interpolation bilinéaire. */
    public static class Terrain {
        final int w, h, span;
        final float[] hgt, wl, sdf, rug;
        final byte[] flags;
        int snowY = 240;

        Terrain(int w, int h, int span) {
            this.w = w; this.h = h; this.span = span;
            int n = w * h;
            hgt = new float[n]; wl = new float[n]; sdf = new float[n]; rug = new float[n]; flags = new byte[n];
        }

        static Terrain read(Path file) throws IOException {
            try (var in = new DataInputStream(new java.io.BufferedInputStream(new GZIPInputStream(Files.newInputStream(file), 1 << 16)))) {
                byte[] magic = new byte[4];
                in.readFully(magic);
                if (!"TER2".equals(new String(magic))) throw new IOException("Terrain.bin non reconnu");
                int w = in.readInt(), h = in.readInt(), span = in.readInt();
                Terrain t = new Terrain(w, h, span);
                t.snowY = in.readInt();
                int n = w * h;
                for (int i = 0; i < n; i++) t.hgt[i] = in.readShort() / 8f;
                for (int i = 0; i < n; i++) t.wl[i] = in.readShort() / 8f;
                for (int i = 0; i < n; i++) t.sdf[i] = in.readShort() / 4f;
                for (int i = 0; i < n; i++) t.rug[i] = (in.readByte() & 0xFF) / 255f;
                in.readFully(t.flags);
                return t;
            }
        }

        static final class Sample {
            double h, wl, sdf, rug;
            boolean sea;
        }

        void sample(double x, double z, Sample s) {
            double fx = (x + 0.5 + span / 2.0) * w / span - 0.5, fz = (z + 0.5 + span / 2.0) * h / span - 0.5;
            int i = (int) Math.floor(fx), j = (int) Math.floor(fz);
            double tx = fx - i, tz = fz - j;
            int i0 = cl(i, w), i1 = cl(i + 1, w), j0 = cl(j, h), j1 = cl(j + 1, h);
            int a = j0 * w + i0, b = j0 * w + i1, c = j1 * w + i0, d = j1 * w + i1;
            s.h = bil(hgt, a, b, c, d, tx, tz);
            s.wl = bil(wl, a, b, c, d, tx, tz);
            s.sdf = bil(sdf, a, b, c, d, tx, tz);
            s.rug = bil(rug, a, b, c, d, tx, tz);
            // Mer ou eau intérieure : le pixel d'eau le plus proche parmi les quatre.
            double seaW = 0, inW = 0;
            double[] wt = {(1 - tx) * (1 - tz), tx * (1 - tz), (1 - tx) * tz, tx * tz};
            int[] id = {a, b, c, d};
            for (int k = 0; k < 4; k++) {
                if ((flags[id[k]] & 1) != 0) seaW += wt[k];
                if ((flags[id[k]] & 2) != 0) inW += wt[k];
            }
            s.sea = seaW > 0 && seaW >= inW;
            if (seaW == 0 && inW == 0) s.sea = s.wl <= 0.01;   // terre : l'eau voisine décide
        }

        private static int cl(int v, int n) { return v < 0 ? 0 : (v >= n ? n - 1 : v); }

        private static double bil(float[] a, int i00, int i10, int i01, int i11, double tx, double tz) {
            double u = a[i00] + (a[i10] - a[i00]) * tx;
            double v = a[i01] + (a[i11] - a[i01]) * tx;
            return u + (v - u) * tz;
        }
    }

    /** Bruit de Perlin 2D classique (déterministe, sans état). */
    static final class Noise {
        private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }

        private static double grad(int hash, double x, double y) {
            switch (hash & 7) {
                case 0: return x + y;   case 1: return -x + y;
                case 2: return x - y;   case 3: return -x - y;
                case 4: return x;       case 5: return -x;
                case 6: return y;       default: return -y;
            }
        }

        private static int hash(int x, int y, int seed) {
            int h = x * 374761393 + y * 668265263 + seed * 2147483647;
            h = (h ^ (h >>> 13)) * 1274126177;
            return h ^ (h >>> 16);
        }

        static double perlin(double x, double y, int seed) {
            int xi = (int) Math.floor(x), yi = (int) Math.floor(y);
            double xf = x - xi, yf = y - yi, u = fade(xf), v = fade(yf);
            double a = grad(hash(xi, yi, seed), xf, yf), b = grad(hash(xi + 1, yi, seed), xf - 1, yf);
            double c = grad(hash(xi, yi + 1, seed), xf, yf - 1), d = grad(hash(xi + 1, yi + 1, seed), xf - 1, yf - 1);
            double r = (a + (b - a) * u) + ((c + (d - c) * u) - (a + (b - a) * u)) * v;
            return Math.max(-1, Math.min(1, r * 0.75));
        }
    }
}
