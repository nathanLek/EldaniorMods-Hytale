package com.eldanior.carte;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedEntityChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenLoadException;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongPredicate;
import java.util.zip.Inflater;

/**
 * Outil de conversion (pas un mod de jeu) : générateur de monde qui recopie le fichier .dat produit par
 * scripts/carte/mc_vers_hytale.py, puis fait générer toute la zone au démarrage du monde et arrête le serveur.
 * Le serveur Hytale écrit lui-même les chunks dans son format : la sauvegarde obtenue s'ouvre sans ce plugin.
 */
public class DownstonGen extends JavaPlugin {

    public DownstonGen(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        IWorldGenProvider.CODEC.register("Downston", Provider.class, Provider.CODEC);
        getEventRegistry().registerGlobal(StartWorldEvent.class, e -> {
            World world = e.getWorld();
            if (world == null) return;
            String verify = System.getProperty("downston.verify");
            if (verify != null) {
                world.execute(() -> verify(world, Path.of(verify)));
            } else if (world.getChunkStore().getGenerator() instanceof Gen gen) {
                world.execute(() -> pregenerate(world, gen));
            }
        });
    }

    /** Relit le monde enregistré (générateur Void) et le compare au .dat, bloc par bloc (1 colonne sur 4 x 4). */
    private static void verify(World world, Path dat) {
        Gen gen;
        try {
            gen = new Gen(dat, "Env_Zone1");
        } catch (Exception ex) {
            System.out.println("[Downston] VERIF_ERREUR " + ex);
            HytaleServer.get().shutdownServer();
            return;
        }
        ChunkStore store = world.getChunkStore();
        List<CompletableFuture<?>> all = new ArrayList<>();
        for (int z = 0; z < gen.ncz; z++)
            for (int x = 0; x < gen.ncx; x++)
                all.add(store.getChunkReferenceAsync(ChunkUtil.indexChunk(gen.cminX + x, gen.cminZ + z)));
        CompletableFuture.allOf(all.toArray(new CompletableFuture[0])).whenComplete((v, t) -> world.execute(() -> {
            long checked = 0, badBlock = 0, badRot = 0, badFluid = 0, nonAir = 0;
            var s = store.getStore();
            for (int cz = 0; cz < gen.ncz; cz++) {
                for (int cx = 0; cx < gen.ncx; cx++) {
                    byte[] blocks = Gen.inflate(gen.chunkData[cz * gen.ncx + cx], gen.height * 1024);
                    for (int y = 0; y < gen.height; y++) {
                        for (int lz = 0; lz < 32; lz += 4) {
                            for (int lx = 0; lx < 32; lx += 4) {
                                int wx = (gen.cminX + cx) * 32 + lx, wz = (gen.cminZ + cz) * 32 + lz;
                                int p = blocks[(y << 10) | (lz << 5) | lx] & 0xFF;
                                var ref = store.getChunkSectionReferenceAtBlock(wx, y, wz);
                                var bs = ref == null ? null : s.getComponent(ref,
                                        com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection.getComponentType());
                                var fs = ref == null ? null : s.getComponent(ref, FluidSection.getComponentType());
                                int block = bs == null ? 0 : bs.get(wx, y, wz);
                                int fluidId = fs == null ? 0 : fs.getFluidId(wx, y, wz);
                                checked++;
                                if (p != 0) nonAir++;
                                if (p != 0 && gen.fluid[p]) {
                                    if (fluidId != gen.blockId[p]) badFluid++;
                                } else {
                                    if (block != gen.blockId[p]) badBlock++;
                                    else if (p != 0 && bs.getRotationIndex(wx, y, wz) != gen.rotation[p]) badRot++;
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("[Downston] VERIF blocs=" + checked + " nonVides=" + nonAir + " erreursBloc=" + badBlock
                    + " erreursRotation=" + badRot + " erreursEau=" + badFluid);
            HytaleServer.get().shutdownServer();
        }));
    }

    private static void pregenerate(World world, Gen gen) {
        ChunkStore store = world.getChunkStore();
        List<CompletableFuture<?>> all = new ArrayList<>();
        AtomicInteger done = new AtomicInteger();
        int total = gen.ncx * gen.ncz;
        for (int z = 0; z < gen.ncz; z++) {
            for (int x = 0; x < gen.ncx; x++) {
                long idx = ChunkUtil.indexChunk(gen.cminX + x, gen.cminZ + z);
                all.add(store.getChunkReferenceAsync(idx).whenComplete((r, t) -> {
                    int n = done.incrementAndGet();
                    if (t != null) System.out.println("[Downston] ERREUR chunk : " + t);
                    if (n % 64 == 0 || n == total) System.out.println("[Downston] chunks " + n + "/" + total);
                }));
            }
        }
        CompletableFuture.allOf(all.toArray(new CompletableFuture[0])).whenComplete((v, t) -> {
            System.out.println("[Downston] GENERATION_TERMINEE " + done.get() + (t != null ? " avec erreur " + t : ""));
            // Laisser le temps à l'éclairage, puis arrêt propre (qui enregistre tous les chunks).
            new Thread(() -> {
                try { Thread.sleep(20_000); } catch (InterruptedException ignored) { }
                System.out.println("[Downston] ARRET");
                HytaleServer.get().shutdownServer();
            }, "downston-stop").start();
        });
    }

    // ------------------------------------------------------------------ fournisseur (config.json : WorldGen)
    public static class Provider implements IWorldGenProvider {
        public static final BuilderCodec<Provider> CODEC = BuilderCodec.builder(Provider.class, Provider::new)
                .append(new KeyedCodec<>("DataFile", Codec.STRING), (o, s) -> o.dataFile = s, o -> o.dataFile).add()
                .append(new KeyedCodec<>("Environment", Codec.STRING), (o, s) -> o.environment = s, o -> o.environment).add()
                .build();

        String dataFile;
        String environment = "Env_Zone1";

        @Override
        public IWorldGen getGenerator() throws WorldGenLoadException {
            try {
                return new Gen(Path.of(dataFile), environment);
            } catch (IOException e) {
                throw new WorldGenLoadException("Downston : lecture de " + dataFile + " impossible : " + e);
            }
        }
    }

    // ------------------------------------------------------------------ générateur
    static class Gen implements IWorldGen {
        final int cminX, cminZ, ncx, ncz, height;
        final int[] blockId, rotation;
        final boolean[] fluid;
        final byte[] fluidLevel;
        final byte[][] chunkData;
        final Transform spawn;
        final int envId;
        final int tint = 0xFF5B9E28;

        Gen(Path file, String environment) throws IOException, WorldGenLoadException {
            try (DataInputStream in = new DataInputStream(Files.newInputStream(file))) {
                byte[] magic = new byte[4];
                in.readFully(magic);
                if (!"DNST".equals(new String(magic)) || in.readInt() != 1) throw new IOException("fichier non reconnu");
                cminX = in.readInt(); cminZ = in.readInt(); ncx = in.readInt(); ncz = in.readInt(); height = in.readInt();
                int n = in.readInt();
                blockId = new int[n]; rotation = new int[n]; fluid = new boolean[n]; fluidLevel = new byte[n];
                for (int i = 0; i < n; i++) {
                    String name = in.readUTF();
                    rotation[i] = in.readByte();
                    fluid[i] = in.readByte() != 0;
                    if (i == 0) continue;
                    if (fluid[i]) {
                        int id = Fluid.getAssetMap().getIndex(name);
                        if (id == Integer.MIN_VALUE) throw new WorldGenLoadException("Fluide inconnu : " + name);
                        blockId[i] = id;
                        fluidLevel[i] = (byte) Fluid.getAssetMap().getAsset(id).getMaxFluidLevel();
                    } else {
                        int id = BlockType.getAssetMap().getIndex(name);
                        if (id == Integer.MIN_VALUE) throw new WorldGenLoadException("Bloc inconnu : " + name);
                        blockId[i] = id;
                    }
                }
                spawn = new Transform(in.readInt() + 0.5, in.readInt(), in.readInt() + 0.5);
                chunkData = new byte[ncx * ncz][];
                for (int i = 0; i < chunkData.length; i++) {
                    byte[] b = new byte[in.readInt()];
                    in.readFully(b);
                    chunkData[i] = b;
                }
            }
            int e = Environment.getAssetMap().getIndex(environment);
            envId = e == Integer.MIN_VALUE ? 0 : e;
        }

        @Override
        public WorldGenTimingsCollector getTimings() { return null; }

        @Override
        public Transform[] getSpawnPoints(int seed) { return new Transform[]{spawn}; }

        @Override
        public CompletableFuture<GeneratedChunk> generate(int seed, long index, int cx, int cz,
                                                          LongPredicate stillNeeded, Long2FloatFunction priority) {
            GeneratedBlockChunk bc = new GeneratedBlockChunk(index, cx, cz);
            Holder<ChunkStore>[] sections = GeneratedChunk.makeSections();
            int lx = cx - cminX, lz = cz - cminZ;
            if (lx >= 0 && lz >= 0 && lx < ncx && lz < ncz) {
                byte[] blocks = inflate(chunkData[lz * ncx + lx], height * 1024);
                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        bc.setTint(x, z, tint);
                        bc.setEnvironmentColumn(x, z, envId);
                    }
                }
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < 32; z++) {
                        for (int x = 0; x < 32; x++) {
                            int p = blocks[(y << 10) | (z << 5) | x] & 0xFF;
                            if (p == 0) continue;
                            if (fluid[p]) {
                                FluidSection fs = sections[y >> 5].ensureAndGetComponent(FluidSection.getComponentType());
                                fs.setFluid(x, y, z, blockId[p], fluidLevel[p]);
                            } else {
                                bc.setBlock(x, y, z, blockId[p], rotation[p], 0);
                            }
                        }
                    }
                }
            }
            return CompletableFuture.completedFuture(new GeneratedChunk(bc, new GeneratedEntityChunk(), sections));
        }

        private static byte[] inflate(byte[] data, int size) {
            Inflater inf = new Inflater();
            inf.setInput(data);
            byte[] out = new byte[size];
            try {
                int off = 0;
                while (off < size && !inf.finished()) off += inf.inflate(out, off, size - off);
            } catch (java.util.zip.DataFormatException e) {
                throw new IllegalStateException(e);
            } finally {
                inf.end();
            }
            return out;
        }
    }
}
