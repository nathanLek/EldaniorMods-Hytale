package com.eldanior.system.territory;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Remise en etat d'un bien (demande du 2026-11-17) : « remettre la zone dans l'etat d'avant la location ou l'achat ; si le
 * joueur est expulse, ne loue plus ou quitte le bien, supprimer les blocs et vider les coffres ajoutes pendant ce temps ».
 *
 * capture() photographie la zone (id de bloc, rotation, case de remplissage) au debut d'une location (RENT) ou d'un achat (BUY),
 * dans eldanior_data/zone_snapshots/<id>.<kind>.snap (gzip). restore() la remet : les blocs qui different sont retires (un
 * contenant retire est vide avant, rien ne tombe au sol), puis ceux de la photo sont reposes. Les contenants deja la au debut
 * gardent leur contenu (le reposer permettrait de dupliquer ce que le locataire en a sorti) ; un contenant d'origine casse est
 * repose vide. Un bloc dans un autre etat (porte ouverte, coffre ouvert) compte comme identique. Liquides et entites : non suivis.
 *
 * Les sections non chargees sont chargees (getChunkSectionReferenceAtBlockAsync) avant le travail, fait en une fois sur le thread
 * du monde. Une remise en cours est renommee en .restore et reprise au demarrage si le serveur s'arrete avant (resumePending).
 * Seulement parcelles, maisons et chambres, et au plus MAX_SECTIONS sections de 32 x 32 x 32.
 */
public final class ZoneSnapshots {

    private ZoneSnapshots() {}

    public static final String BUY = "buy", RENT = "rent";
    private static final List<ParcelType> TYPES = List.of(ParcelType.PLOT, ParcelType.HOUSING, ParcelType.ROOM);
    private static final int MAX_SECTIONS = 160, MAGIC = 0x5A4E5331, VERSION = 1;
    private static final byte S_MISSING = 0, S_AIR = 1, S_DATA = 2;
    private static final String EMPTY = "Empty";
    /** Index de type -> id sans etat (« *Porte_State_Definitions_Open » -> « Porte »). */
    private static final Map<Integer, String> BASE = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean eligible(ParcelData p) {
        return p != null && TYPES.contains(p.getType()) && p.getWorld() != null && !p.getWorld().isEmpty();
    }

    private static Path dir() {
        Path d = ParcelManager.getDataDir();
        return d != null ? d.resolve("zone_snapshots") : null;
    }

    private static Path file(String parcelId, String kind, String ext) {
        Path d = dir();
        return d != null ? d.resolve(parcelId.replaceAll("[^A-Za-z0-9_.-]", "_") + "." + kind + ext) : null;
    }

    public static boolean has(ParcelData p, String kind) {
        Path f = p != null ? file(p.getId(), kind, ".snap") : null;
        return f != null && Files.exists(f);
    }

    public static void discard(ParcelData p, String kind) {
        Path f = p != null ? file(p.getId(), kind, ".snap") : null;
        try { if (f != null) Files.deleteIfExists(f); } catch (Exception e) { EldaniorLogger.error("[Remise en etat] suppression", e); }
    }

    // ============================================ geometrie ============================================

    /** Boite de la zone (Y borne a 0..319) et sections couvertes, dans l'ordre sx, sz, sy. */
    private record Geo(int x0, int y0, int z0, int x1, int y1, int z1, boolean cyl) {
        int sx0() { return x0 >> 5; } int sx1() { return x1 >> 5; }
        int sy0() { return y0 >> 5; } int sy1() { return y1 >> 5; }
        int sz0() { return z0 >> 5; } int sz1() { return z1 >> 5; }
        long sections() { return (long) (sx1() - sx0() + 1) * (sy1() - sy0() + 1) * (sz1() - sz0() + 1); }
        boolean inside(int x, int z) {
            if (!cyl) return true;
            double cx = (x0 + x1) / 2.0, cz = (z0 + z1) / 2.0, r = (x1 - x0) / 2.0 + 0.5;
            return (x - cx) * (x - cx) + (z - cz) * (z - cz) <= r * r;
        }
    }

    private static Geo geo(ParcelData p) {
        int y0 = Math.max(0, p.getMinY()), y1 = Math.min(319, p.getMaxY());
        if (y1 < y0) return null;
        return new Geo(p.getMinX(), y0, p.getMinZ(), p.getMaxX(), y1, p.getMaxZ(), p.isCylinder());
    }

    /** Charge toutes les sections de la boite, puis lance work sur le thread du monde. */
    private static void withSections(World world, Geo g, Runnable work) {
        world.execute(() -> {
            ChunkStore chunks = world.getChunkStore();
            List<CompletableFuture<?>> loads = new ArrayList<>();
            for (int sx = g.sx0(); sx <= g.sx1(); sx++)
                for (int sz = g.sz0(); sz <= g.sz1(); sz++)
                    for (int sy = g.sy0(); sy <= g.sy1(); sy++) {
                        int x = sx << 5, y = sy << 5, z = sz << 5;
                        Ref<ChunkStore> r = chunks.getChunkSectionReferenceAtBlock(x, y, z);
                        if (r == null || !r.isValid()) loads.add(chunks.getChunkSectionReferenceAtBlockAsync(x, y, z));
                    }
            if (loads.isEmpty()) { run(work); return; }
            CompletableFuture.allOf(loads.toArray(new CompletableFuture[0])).orTimeout(60, TimeUnit.SECONDS)
                    .whenComplete((v, err) -> world.execute(() -> run(work)));
        });
    }

    private static void run(Runnable work) {
        try { work.run(); } catch (Exception e) { EldaniorLogger.error("[Remise en etat]", e); }
    }

    private static BlockSection section(World world, int x, int y, int z) {
        ChunkStore chunks = world.getChunkStore();
        Ref<ChunkStore> r = chunks.getChunkSectionReferenceAtBlock(x, y, z);
        return r != null && r.isValid() ? chunks.getStore().getComponent(r, BlockSection.getComponentType()) : null;
    }

    // ============================================ photo ============================================

    /** Photographie la zone (remplace la photo precedente du meme genre). */
    public static void capture(ParcelData p, String kind) {
        if (!eligible(p)) return;
        World world = Universe.get().getWorld(p.getWorld());
        Geo g = geo(p);
        if (world == null || g == null) return;
        if (g.sections() > MAX_SECTIONS) {
            EldaniorLogger.info("[Remise en etat] " + p.getName() + " : zone trop grande, pas de photo (" + g.sections() + " sections)");
            return;
        }
        String id = p.getId(), name = p.getName();
        withSections(world, g, () -> {
            byte[] data = encode(world, g);
            Path f = file(id, kind, ".snap");
            if (f == null) return;
            EldaniorLogger.SCHEDULER.execute(() -> {
                try {
                    Files.createDirectories(f.getParent());
                    Path tmp = f.resolveSibling(f.getFileName() + ".tmp");
                    Files.write(tmp, data);
                    Files.move(tmp, f, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    EldaniorLogger.info("[Remise en etat] photo de " + name + " (" + kind + ", " + data.length / 1024 + " Ko)");
                } catch (Exception e) {
                    EldaniorLogger.error("[Remise en etat] ecriture " + f, e);
                }
            });
        });
    }

    private static byte[] encode(World world, Geo g) {
        try {
            Map<Integer, Short> palette = new HashMap<>();
            List<String> names = new ArrayList<>();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(body);
            for (int sx = g.sx0(); sx <= g.sx1(); sx++)
                for (int sz = g.sz0(); sz <= g.sz1(); sz++)
                    for (int sy = g.sy0(); sy <= g.sy1(); sy++) {
                        BlockSection bs = section(world, sx << 5, sy << 5, sz << 5);
                        if (bs == null) { out.writeByte(S_MISSING); continue; }
                        if (bs.isSolidAir()) { out.writeByte(S_AIR); continue; }
                        out.writeByte(S_DATA);
                        int ax = Math.max(g.x0(), sx << 5), bx = Math.min(g.x1(), (sx << 5) + 31);
                        int ay = Math.max(g.y0(), sy << 5), by = Math.min(g.y1(), (sy << 5) + 31);
                        int az = Math.max(g.z0(), sz << 5), bz = Math.min(g.z1(), (sz << 5) + 31);
                        for (int x = ax; x <= bx; x++)
                            for (int z = az; z <= bz; z++)
                                for (int y = ay; y <= by; y++) {
                                    int id = bs.get(x, y, z);
                                    Short pal = palette.get(id);
                                    if (pal == null) {
                                        BlockType t = BlockType.getAssetMap().getAsset(id);
                                        pal = (short) names.size();
                                        names.add(t != null && t.getId() != null ? t.getId() : EMPTY);
                                        palette.put(id, pal);
                                    }
                                    out.writeShort(pal);
                                    out.writeByte(bs.getRotationIndex(x, y, z));
                                    out.writeInt(bs.getFiller(x, y, z));
                                }
                    }
            out.flush();
            ByteArrayOutputStream all = new ByteArrayOutputStream();
            try (DataOutputStream o = new DataOutputStream(new GZIPOutputStream(all))) {
                o.writeInt(MAGIC);
                o.writeInt(VERSION);
                for (int v : new int[]{g.x0(), g.y0(), g.z0(), g.x1(), g.y1(), g.z1()}) o.writeInt(v);
                o.writeBoolean(g.cyl());
                o.writeInt(names.size());
                for (String n : names) o.writeUTF(n);
                body.writeTo(o);
            }
            return all.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("photo de zone", e);
        }
    }

    // ============================================ remise en etat ============================================

    /**
     * Remet la zone comme sur sa photo de ce genre, puis efface la photo. Sans photo (bien acquis avant le 2026-11-17, ou zone
     * trop grande) : rien. Peut etre appele depuis n'importe quel thread.
     */
    public static void restore(ParcelData p, String kind) {
        if (p == null) return;
        Path snap = file(p.getId(), kind, ".snap"), pending = file(p.getId(), kind, ".restore");
        if (snap == null || !Files.exists(snap)) return;
        try {
            Files.move(snap, pending, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            EldaniorLogger.error("[Remise en etat] " + snap, e);
            return;
        }
        apply(p.getWorld(), p.getName(), pending);
    }

    /** Remises interrompues par un arret du serveur : reprises (appele une fois, apres le chargement des mondes). */
    public static void resumePending() {
        Path d = dir();
        if (d == null || !Files.isDirectory(d)) return;
        try (var files = Files.list(d)) {
            for (Path f : files.filter(x -> x.getFileName().toString().endsWith(".restore")).toList()) {
                String n = f.getFileName().toString();
                String id = n.substring(0, n.indexOf('.'));
                ParcelData p = ParcelManager.get(id);
                if (p == null) { Files.deleteIfExists(f); continue; }
                apply(p.getWorld(), p.getName(), f);
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Remise en etat] reprise", e);
        }
    }

    private record Snap(Geo geo, String[] names, byte[] flags, List<short[]> pal, List<byte[]> rot, List<int[]> fill) {}

    private static void apply(String worldName, String name, Path pending) {
        World world = worldName != null ? Universe.get().getWorld(worldName) : null;
        if (world == null) return;                                            // monde absent : reprise au prochain demarrage
        Snap s;
        try { s = decode(Files.readAllBytes(pending)); }
        catch (Exception e) {
            EldaniorLogger.error("[Remise en etat] lecture " + pending, e);
            try { Files.deleteIfExists(pending); } catch (Exception ignored) {}
            return;
        }
        withSections(world, s.geo(), () -> {
            int changed = write(world, s);
            if (changed < 0) return;                                          // sections absentes : on garde le fichier
            try { Files.deleteIfExists(pending); } catch (Exception ignored) {}
            EldaniorLogger.info("[Remise en etat] " + name + " : " + changed + " bloc(s) remis");
        });
    }

    private static Snap decode(byte[] raw) throws Exception {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(raw)))) {
            if (in.readInt() != MAGIC || in.readInt() != VERSION) throw new IllegalStateException("format inconnu");
            Geo g = new Geo(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readBoolean());
            String[] names = new String[in.readInt()];
            for (int i = 0; i < names.length; i++) names[i] = in.readUTF();
            int count = (int) g.sections();
            byte[] flags = new byte[count];
            List<short[]> pal = new ArrayList<>(count);
            List<byte[]> rot = new ArrayList<>(count);
            List<int[]> fill = new ArrayList<>(count);
            int i = 0;
            for (int sx = g.sx0(); sx <= g.sx1(); sx++)
                for (int sz = g.sz0(); sz <= g.sz1(); sz++)
                    for (int sy = g.sy0(); sy <= g.sy1(); sy++, i++) {
                        flags[i] = in.readByte();
                        if (flags[i] != S_DATA) { pal.add(null); rot.add(null); fill.add(null); continue; }
                        int n = cells(g, sx, sy, sz);
                        short[] p = new short[n]; byte[] r = new byte[n]; int[] f = new int[n];
                        for (int c = 0; c < n; c++) { p[c] = in.readShort(); r[c] = in.readByte(); f[c] = in.readInt(); }
                        pal.add(p); rot.add(r); fill.add(f);
                    }
            return new Snap(g, names, flags, pal, rot, fill);
        }
    }

    private static int cells(Geo g, int sx, int sy, int sz) {
        int ax = Math.max(g.x0(), sx << 5), bx = Math.min(g.x1(), (sx << 5) + 31);
        int ay = Math.max(g.y0(), sy << 5), by = Math.min(g.y1(), (sy << 5) + 31);
        int az = Math.max(g.z0(), sz << 5), bz = Math.min(g.z1(), (sz << 5) + 31);
        return (bx - ax + 1) * (by - ay + 1) * (bz - az + 1);
    }

    /** Applique la photo (thread du monde). Nombre de blocs changes, -1 si une section photographiee n'est pas chargee. */
    private static int write(World world, Snap s) {
        Geo g = s.geo();
        ChunkStore chunks = world.getChunkStore();
        int airIdx = BlockType.getAssetMap().getIndexOrDefault(EMPTY, -1);
        if (airIdx < 0) return -1;
        BlockType airType = BlockType.getAssetMap().getAsset(airIdx);
        if (airType == null) return -1;
        int[] idx = new int[s.names().length];
        for (int i = 0; i < idx.length; i++) {
            int k = BlockType.getAssetMap().getIndexOrDefault(s.names()[i], -1);
            idx[i] = k >= 0 && BlockType.getAssetMap().getAsset(k) != null ? k : -1;      // type disparu : case laissee telle quelle
        }
        int changed = 0;
        for (int pass = 0; pass < 2; pass++) {                                 // 0 : retirer ce qui differe ; 1 : reposer la photo
            int i = 0;
            for (int sx = g.sx0(); sx <= g.sx1(); sx++)
                for (int sz = g.sz0(); sz <= g.sz1(); sz++)
                    for (int sy = g.sy0(); sy <= g.sy1(); sy++, i++) {
                        byte flag = s.flags()[i];
                        if (flag == S_MISSING) continue;
                        Ref<ChunkStore> ref = chunks.getChunkSectionReferenceAtBlock(sx << 5, sy << 5, sz << 5);
                        BlockSection bs = ref != null && ref.isValid() ? chunks.getStore().getComponent(ref, BlockSection.getComponentType()) : null;
                        if (bs == null) return -1;
                        if (flag == S_AIR && (pass == 1 || bs.isSolidAir())) continue;
                        short[] pal = s.pal().get(i);
                        byte[] rot = s.rot().get(i);
                        int[] fill = s.fill().get(i);
                        int ax = Math.max(g.x0(), sx << 5), bx = Math.min(g.x1(), (sx << 5) + 31);
                        int ay = Math.max(g.y0(), sy << 5), by = Math.min(g.y1(), (sy << 5) + 31);
                        int az = Math.max(g.z0(), sz << 5), bz = Math.min(g.z1(), (sz << 5) + 31);
                        int c = 0;
                        for (int x = ax; x <= bx; x++)
                            for (int z = az; z <= bz; z++)
                                for (int y = ay; y <= by; y++, c++) {
                                    if (!g.inside(x, z)) continue;
                                    int sid = flag == S_AIR ? airIdx : idx[pal[c]];
                                    if (sid < 0) continue;
                                    int srot = flag == S_AIR ? 0 : rot[c] & 0xFF;
                                    boolean sFiller = flag != S_AIR && fill[c] != 0;
                                    int cur = bs.get(x, y, z), crot = bs.getRotationIndex(x, y, z);
                                    boolean cFiller = bs.getFiller(x, y, z) != 0;
                                    boolean same = same(cur, sid) && crot == srot && cFiller == sFiller;
                                    if (same) continue;
                                    if (pass == 0) {
                                        if (cFiller || cur == airIdx) continue;              // case de remplissage : partira avec son bloc
                                        if (same(cur, sid) && crot == srot) continue;
                                        empty(world, x, y, z, cur);
                                        BlockOperations.setBlock(chunks, ref, x, y, z, airIdx, airType, 0, 0, 0);
                                        changed++;
                                    } else {
                                        if (sFiller || sid == airIdx) continue;
                                        if (!cFiller && same(cur, sid) && crot == srot) continue;
                                        BlockType t = BlockType.getAssetMap().getAsset(sid);
                                        if (t == null) continue;
                                        if (cur != airIdx && !cFiller) empty(world, x, y, z, cur);
                                        BlockOperations.setBlock(chunks, ref, x, y, z, sid, t, srot, 0, 0);
                                        changed++;
                                    }
                                }
                    }
        }
        return changed;
    }

    /** Vide le contenant (s'il y en a un) avant de retirer son bloc : rien ne tombe au sol. */
    private static void empty(World world, int x, int y, int z, int typeIdx) {
        try {
            BlockType t = BlockType.getAssetMap().getAsset(typeIdx);
            if (t == null || t.getBlockEntity() == null) return;
            ItemContainerBlock box = BlockModule.getComponent(ItemContainerBlock.getComponentType(), world, x, y, z);
            if (box != null && box.getItemContainer() != null) box.getItemContainer().clear();
        } catch (Exception e) {
            EldaniorLogger.error("[Remise en etat] contenant", e);
        }
    }

    /** Meme bloc, quel que soit son etat (porte ouverte, coffre ouvert). */
    private static boolean same(int a, int b) {
        return a == b || base(a).equals(base(b));
    }

    private static String base(int idx) {
        return BASE.computeIfAbsent(idx, i -> {
            BlockType t = BlockType.getAssetMap().getAsset(i);
            String id = t != null && t.getId() != null ? t.getId() : "#" + i;
            if (id.startsWith("*")) id = id.substring(1);
            int st = id.indexOf("_State_");
            return st > 0 ? id.substring(0, st) : id;
        });
    }
}
