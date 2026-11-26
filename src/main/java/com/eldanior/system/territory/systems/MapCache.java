package com.eldanior.system.territory.systems;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.protocol.packets.worldmap.MapImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Memoire de la carte (2026-11-20, retour de l'utilisateur : « la map visitee doit rester chargee », « quand tu refresh tu oublies
 * ce que tu as deja enregistre »). Le moteur ne garde pas les images de carte : il les refait a partir des chunks des qu'un joueur
 * revient ou se reconnecte, et les oublie quand plus personne n'est pres. Ici chaque image produite est gardee sur disque
 * (eldanior_data/carte/<monde>/r.<rx>.<rz>.bin, une region = 32 x 32 chunks), compressee ; KingdomMap la ressert sans recharger
 * ni generer le chunk. Les regions lues restent en memoire (au plus {@link #MAX_REGIONS}, les plus anciennes repartent sur disque).
 */
final class MapCache {

    private static final int MAX_REGIONS = 128;

    private final Path dir;
    /** Region -> (case locale 0..1023 -> image compressee). Ordre d'acces : la plus ancienne sort en premier. */
    private final LinkedHashMap<Long, Region> regions = new LinkedHashMap<>(64, 0.75f, true);

    private static final class Region {
        final Map<Integer, byte[]> cells = new HashMap<>();
        boolean dirty;
    }

    MapCache(Path dir) { this.dir = dir; }

    private static long regionKey(int cx, int cz) { return ((long) (cx >> 5) << 32) | ((cz >> 5) & 0xffffffffL); }

    private static int local(int cx, int cz) { return (cx & 31) * 32 + (cz & 31); }

    private Path file(long key) { return dir.resolve("r." + (int) (key >> 32) + "." + (int) key + ".bin"); }

    /** Image gardee pour ce chunk, a cette largeur (finesse), null sinon. */
    synchronized MapImage get(int cx, int cz, int width) {
        byte[] data = region(regionKey(cx, cz)).cells.get(local(cx, cz));
        if (data == null) return null;
        try {
            MapImage img = decode(data);
            return img.width == width ? img : null;
        } catch (IOException e) {
            return null;
        }
    }

    synchronized boolean has(int cx, int cz) { return region(regionKey(cx, cz)).cells.containsKey(local(cx, cz)); }

    /** Chunks gardes dans la region (rx, rz) (coordonnees de region = chunk >> 5) : {cx, cz} par case. */
    synchronized int[][] cells(int rx, int rz) {
        Region r = region(((long) rx << 32) | (rz & 0xffffffffL));
        int[][] out = new int[r.cells.size()][];
        int i = 0;
        for (int cell : r.cells.keySet()) out[i++] = new int[]{rx * 32 + cell / 32, rz * 32 + cell % 32};
        return out;
    }

    synchronized void put(int cx, int cz, MapImage img) {
        if (img == null || img.packedIndices == null) return;
        try {
            Region r = region(regionKey(cx, cz));
            r.cells.put(local(cx, cz), encode(img));
            r.dirty = true;
        } catch (IOException e) {
            EldaniorLogger.error("[Carte] memoire", e);
        }
    }

    /** Ecrit les regions modifiees (tache de fond). */
    void flush() {
        Map<Long, byte[]> out = new HashMap<>();
        synchronized (this) {
            for (var e : regions.entrySet()) {
                if (!e.getValue().dirty) continue;
                try { out.put(e.getKey(), write(e.getValue())); e.getValue().dirty = false; }
                catch (IOException ex) { EldaniorLogger.error("[Carte] memoire", ex); }
            }
        }
        for (var e : out.entrySet()) save(e.getKey(), e.getValue());
    }

    private void save(long key, byte[] bytes) {
        try {
            Files.createDirectories(dir);
            Path f = file(key), tmp = f.resolveSibling(f.getFileName() + ".tmp");
            Files.write(tmp, bytes);
            Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            EldaniorLogger.error("[Carte] ecriture " + key, e);
        }
    }

    private Region region(long key) {
        Region r = regions.get(key);
        if (r != null) return r;
        r = new Region();
        Path f = file(key);
        if (Files.exists(f)) {
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(Files.readAllBytes(f)))) {
                int n = in.readInt();
                for (int i = 0; i < n; i++) {
                    int cell = in.readInt(), len = in.readInt();
                    byte[] b = new byte[len];
                    in.readFully(b);
                    r.cells.put(cell, b);
                }
            } catch (IOException e) {
                EldaniorLogger.error("[Carte] lecture " + f.getFileName(), e);
            }
        }
        regions.put(key, r);
        while (regions.size() > MAX_REGIONS) {                               // la plus ancienne repart sur disque
            var it = regions.entrySet().iterator();
            var old = it.next();
            if (old.getKey() == key) break;
            if (old.getValue().dirty) {
                try { save(old.getKey(), write(old.getValue())); } catch (IOException e) { EldaniorLogger.error("[Carte] memoire", e); }
            }
            it.remove();
        }
        return r;
    }

    private static byte[] write(Region r) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bo)) {
            out.writeInt(r.cells.size());
            for (var e : r.cells.entrySet()) {
                out.writeInt(e.getKey());
                out.writeInt(e.getValue().length);
                out.write(e.getValue());
            }
        }
        return bo.toByteArray();
    }

    private static byte[] encode(MapImage img) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
        try (DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(bo, new Deflater(Deflater.BEST_SPEED)))) {
            out.writeInt(img.width);
            out.writeInt(img.height);
            out.writeByte(img.bitsPerIndex);
            int[] pal = img.palette != null ? img.palette : new int[0];
            out.writeInt(pal.length);
            for (int c : pal) out.writeInt(c);
            out.writeInt(img.packedIndices.length);
            out.write(img.packedIndices);
        }
        return bo.toByteArray();
    }

    private static MapImage decode(byte[] data) throws IOException {
        try (DataInputStream in = new DataInputStream(new InflaterInputStream(new ByteArrayInputStream(data)))) {
            int w = in.readInt(), h = in.readInt();
            byte bits = in.readByte();
            int[] pal = new int[in.readInt()];
            for (int i = 0; i < pal.length; i++) pal[i] = in.readInt();
            byte[] packed = new byte[in.readInt()];
            in.readFully(packed);
            return new MapImage(w, h, pal, bits, packed);
        }
    }
}
