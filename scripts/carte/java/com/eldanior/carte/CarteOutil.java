package com.eldanior.carte;

import com.eldanior.system.carte.EldaniorWorldGen;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Outil d'essai de la carte (pas un mod de jeu) : au démarrage du monde, fait générer la zone
 * -Dcarte.box=x0,z0,x1,z1 (blocs), écrit une vue du dessus dans -Dcarte.dump=<fichier>, puis arrête le serveur.
 * Format du fichier : "CRTE" | x0 z0 w h | nbNoms | noms (UTF) | puis w*h colonnes : short nom, short hauteur, byte eau.
 * Rendu : scripts/carte/vue_dessus.py.
 */
public class CarteOutil extends JavaPlugin {

    public CarteOutil(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider.CODEC.register(
                EldaniorWorldGen.ID, EldaniorWorldGen.Provider.class, EldaniorWorldGen.Provider.CODEC);
        getEventRegistry().registerGlobal(StartWorldEvent.class, e -> {
            World world = e.getWorld();
            String box = System.getProperty("carte.box");
            if (world == null || box == null) return;
            String[] p = box.split(",");
            int x0 = Integer.parseInt(p[0].trim()), z0 = Integer.parseInt(p[1].trim());
            int x1 = Integer.parseInt(p[2].trim()), z1 = Integer.parseInt(p[3].trim());
            world.execute(() -> run(world, x0, z0, x1, z1, System.getProperty("carte.dump")));
        });
    }

    private static final int BATCH = 2048;

    /**
     * Génère les chunks par lots (la mémoire reste stable : les lots finis sont déchargés et enregistrés).
     * Avec -Dcarte.apercu=<fichier>, relève une colonne sur 8 de chaque lot avant son déchargement (vue d'ensemble).
     */
    private static void run(World world, int x0, int z0, int x1, int z1, String dump) {
        int cx0 = Math.floorDiv(x0, 32), cz0 = Math.floorDiv(z0, 32), cx1 = Math.floorDiv(x1 - 1, 32), cz1 = Math.floorDiv(z1 - 1, 32);
        List<long[]> chunks = new ArrayList<>();
        for (int cz = cz0; cz <= cz1; cz++)
            for (int cx = cx0; cx <= cx1; cx++) chunks.add(new long[]{cx, cz});
        String apercu = System.getProperty("carte.apercu");
        Apercu ap = apercu == null ? null : new Apercu(cx0 * 32, cz0 * 32, (cx1 - cx0 + 1) * 32, (cz1 - cz0 + 1) * 32);
        long t0 = System.currentTimeMillis();
        nextBatch(world, chunks, 0, t0, ap, () -> {
            System.out.println("[Carte] GENERATION_TERMINEE " + chunks.size() + " chunks en " + (System.currentTimeMillis() - t0) / 1000 + " s");
            try {
                if (dump != null) dump(world, x0, z0, x1, z1, dump);
                if (ap != null) ap.write(apercu);
            } catch (Exception ex) {
                System.out.println("[Carte] ERREUR dump " + ex);
            }
            new Thread(() -> {
                try { Thread.sleep(20_000); } catch (InterruptedException ignored) { }
                System.out.println("[Carte] ARRET");
                HytaleServer.get().shutdownServer();
            }, "carte-stop").start();
        });
    }

    private static void nextBatch(World world, List<long[]> chunks, int from, long t0, Apercu ap, Runnable end) {
        if (from >= chunks.size()) {
            end.run();
            return;
        }
        ChunkStore store = world.getChunkStore();
        // Frein : on attend que le jeu ait déchargé les lots précédents (sinon la mémoire déborde à la relecture).
        if (store.getLoadedChunksCount() > 6000) {
            HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> world.execute(() -> nextBatch(world, chunks, from, t0, ap, end)),
                    500, java.util.concurrent.TimeUnit.MILLISECONDS);
            return;
        }
        int to = Math.min(chunks.size(), from + BATCH);
        List<CompletableFuture<?>> batch = new ArrayList<>();
        for (int i = from; i < to; i++) {
            long[] c = chunks.get(i);
            // Relevé immédiat, sur le fil du monde, avant que le chunk ne soit déchargé.
            batch.add(store.getChunkReferenceAsync(ChunkUtil.indexChunk((int) c[0], (int) c[1]))
                    .thenCompose(r -> CompletableFuture.runAsync(() -> {
                        if (ap != null) ap.sample(world, (int) c[0], (int) c[1]);
                    }, world))
                    .whenComplete((r, t) -> {
                        if (t != null) System.out.println("[Carte] ERREUR chunk : " + t);
                    }));
        }
        CompletableFuture.allOf(batch.toArray(new CompletableFuture[0])).whenComplete((v, t) -> world.execute(() -> {
            System.out.println("[Carte] chunks " + to + "/" + chunks.size() + " en " + (System.currentTimeMillis() - t0) / 1000 + " s");
            nextBatch(world, chunks, to, t0, ap, end);
        }));
    }

    /** Vue d'ensemble : une colonne sur 8 (même format que le relevé, coordonnées divisées par 8). */
    static final class Apercu {
        static final int STEP = 8;
        final int x0, z0, w, h;
        final short[] top, height;
        final byte[] water;
        final Map<Integer, Short> names = new HashMap<>();
        final List<String> nameList = new ArrayList<>();
        int missing;

        Apercu(int x0, int z0, int wBlocks, int hBlocks) {
            this.x0 = x0; this.z0 = z0; this.w = wBlocks / STEP; this.h = hBlocks / STEP;
            top = new short[w * h]; height = new short[w * h]; water = new byte[w * h];
        }

        void sample(World world, int cx, int cz) {
            ChunkStore cs = world.getChunkStore();
            Store<ChunkStore> s = cs.getStore();
            BlockSection[] bss = new BlockSection[10];
            FluidSection[] fss = new FluidSection[10];
            for (int sy = 0; sy < 10; sy++) {
                Ref<ChunkStore> ref = cs.getChunkSectionReferenceAtBlock(cx * 32, sy * 32, cz * 32);
                if (ref == null) missing++;
                if (ref != null && ref.isValid()) {
                    bss[sy] = s.getComponent(ref, BlockSection.getComponentType());
                    fss[sy] = s.getComponent(ref, FluidSection.getComponentType());
                }
            }
            for (int lz = 0; lz < 32; lz += STEP) {
                for (int lx = 0; lx < 32; lx += STEP) {
                    int x = cx * 32 + lx, z = cz * 32 + lz;
                    int o = ((z - z0) / STEP) * w + (x - x0) / STEP;
                    if (o < 0 || o >= top.length) continue;
                    for (int y = 319; y >= 0; y--) {
                        BlockSection bs = bss[y >> 5];
                        FluidSection fs = fss[y >> 5];
                        int fl = fs == null ? 0 : fs.getFluidId(x, y, z);
                        int id = bs == null ? 0 : bs.get(x, y, z);
                        if (fl != 0 && water[o] == 0) { water[o] = 1; height[o] = (short) y; }
                        if (id != 0) {
                            Short n = names.get(id);
                            if (n == null) {
                                BlockType bt = BlockType.getAssetMap().getAsset(id);
                                n = (short) nameList.size();
                                nameList.add(bt == null ? ("#" + id) : bt.getId());
                                names.put(id, n);
                            }
                            top[o] = n;
                            if (water[o] == 0) height[o] = (short) y;
                            break;
                        }
                    }
                }
            }
        }

        void write(String path) throws Exception {
            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
                out.writeBytes("CRTE");
                out.writeInt(x0 / STEP); out.writeInt(z0 / STEP); out.writeInt(w); out.writeInt(h);
                out.writeInt(nameList.size());
                for (String n : nameList) out.writeUTF(n);
                for (int i = 0; i < w * h; i++) {
                    out.writeShort(top[i]);
                    out.writeShort(height[i]);
                    out.writeByte(water[i]);
                }
            }
            System.out.println("[Carte] APERCU_ECRIT " + path + " (" + w + "x" + h + ", sections absentes : " + missing + ")");
        }
    }

    private static void dump(World world, int x0, int z0, int x1, int z1, String path) throws Exception {
        ChunkStore cs = world.getChunkStore();
        Store<ChunkStore> s = cs.getStore();
        int w = x1 - x0, h = z1 - z0;
        short[] top = new short[w * h], height = new short[w * h];
        byte[] water = new byte[w * h];
        Map<Integer, Short> names = new HashMap<>();
        List<String> nameList = new ArrayList<>();
        Map<Long, BlockSection[]> bsCache = new HashMap<>();
        Map<Long, FluidSection[]> fsCache = new HashMap<>();
        for (int z = z0; z < z1; z++) {
            for (int x = x0; x < x1; x++) {
                long key = ChunkUtil.indexChunk(Math.floorDiv(x, 32), Math.floorDiv(z, 32));
                BlockSection[] bss = bsCache.get(key);
                FluidSection[] fss = fsCache.get(key);
                if (bss == null) {
                    bss = new BlockSection[10];
                    fss = new FluidSection[10];
                    for (int sy = 0; sy < 10; sy++) {
                        Ref<ChunkStore> ref = cs.getChunkSectionReferenceAtBlock(x, sy * 32, z);
                        if (ref != null && ref.isValid()) {
                            bss[sy] = s.getComponent(ref, BlockSection.getComponentType());
                            fss[sy] = s.getComponent(ref, FluidSection.getComponentType());
                        }
                    }
                    bsCache.put(key, bss);
                    fsCache.put(key, fss);
                }
                int o = (z - z0) * w + (x - x0);
                for (int y = 319; y >= 0; y--) {
                    BlockSection bs = bss[y >> 5];
                    FluidSection fs = fss[y >> 5];
                    int fl = fs == null ? 0 : fs.getFluidId(x, y, z);
                    int id = bs == null ? 0 : bs.get(x, y, z);
                    if (fl != 0 && water[o] == 0) {
                        water[o] = 1;
                        height[o] = (short) y;   // surface de l'eau ; on continue pour trouver le fond
                    }
                    if (id != 0) {
                        Short n = names.get(id);
                        if (n == null) {
                            BlockType bt = BlockType.getAssetMap().getAsset(id);
                            n = (short) nameList.size();
                            nameList.add(bt == null ? ("#" + id) : bt.getId());
                            names.put(id, n);
                        }
                        top[o] = n;
                        if (water[o] == 0) height[o] = (short) y;
                        break;
                    }
                }
            }
        }
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            out.writeBytes("CRTE");
            out.writeInt(x0); out.writeInt(z0); out.writeInt(w); out.writeInt(h);
            out.writeInt(nameList.size());
            for (String n : nameList) out.writeUTF(n);
            for (int i = 0; i < w * h; i++) {
                out.writeShort(top[i]);
                out.writeShort(height[i]);
                out.writeByte(water[i]);
            }
        }
        System.out.println("[Carte] VUE_ECRITE " + path + " (" + w + "x" + h + ", " + nameList.size() + " blocs différents)");
    }
}
