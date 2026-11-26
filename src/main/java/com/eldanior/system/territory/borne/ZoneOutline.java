package com.eldanior.system.territory.borne;

import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelType;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.math.matrix.Matrix4dUtil;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.protocol.packets.player.DisplayDebug;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contour d'une zone bloc par bloc (2026-11-19, demande de l'utilisateur : « voir les aretes de chaque bloc qui delimite le
 * territoire »). Chaque bloc du bord, au niveau du sol, est entoure d'un cube en fil de fer (forme de debug du moteur, paquet
 * DisplayDebug envoye au seul admin qui regarde, sans face pleine : FLAG_NO_SOLID). Couleur = type de zone.
 *
 * Bord = case de la zone dont une voisine (nord, sud, est, ouest) est dehors : marche pour un rectangle comme pour un cylindre.
 * Hauteur : le sol de la colonne (premier bloc plein en descendant depuis la tete de l'admin) ; pour une zone basse (logement,
 * chambre : moins de 48 blocs de haut), ce sol est ramene dans ses hauteurs. Sols gardes 5 s en memoire.
 * Les cubes vivent {@link #LIFE_S} s et sont renvoyes chaque seconde par Bornes.markers.
 */
final class ZoneOutline {

    private ZoneOutline() {}

    private static final float LIFE_S = 1.25f, OPACITY = 0.9f;
    private static final byte FLAGS = 4;                                   // DebugUtils.FLAG_NO_SOLID : aretes seules
    private static final long SURFACE_TTL_MS = 5_000;
    private static final Map<String, long[]> SURFACE = new ConcurrentHashMap<>();

    static Vector3f color(ParcelType t) {
        return switch (t) {
            case KINGDOM -> new Vector3f(1.0f, 0.84f, 0.2f);
            case GRAND_TERRITORY -> new Vector3f(0.72f, 0.45f, 1.0f);
            case TERRITORY -> new Vector3f(0.35f, 0.7f, 1.0f);
            case CITY -> new Vector3f(0.4f, 1.0f, 0.55f);
            case FOREST -> new Vector3f(0.1f, 0.6f, 0.2f);
            case ARENA -> new Vector3f(1.0f, 0.25f, 0.25f);
            case PLOT -> new Vector3f(0.95f, 0.95f, 0.85f);
            case HOUSING -> new Vector3f(1.0f, 0.6f, 0.2f);
            case ROOM -> new Vector3f(1.0f, 0.5f, 0.8f);
            case FARM -> new Vector3f(0.75f, 0.95f, 0.3f);
            case MINE -> new Vector3f(0.6f, 0.6f, 0.65f);
            case DUNGEON -> new Vector3f(0.6f, 0.1f, 0.15f);
        };
    }

    /**
     * Envoie les cubes du bord de p vus depuis pos (rayon range), au plus max ; thread du monde. Renvoie le nombre envoye.
     */
    static int draw(PlayerRef viewer, World world, ParcelData p, Vector3d pos, int range, int max) {
        if (max <= 0) return 0;
        int px = (int) Math.floor(pos.x), pz = (int) Math.floor(pos.z);
        int x1 = Math.max(p.getMinX(), px - range), x2 = Math.min(p.getMaxX(), px + range);
        int z1 = Math.max(p.getMinZ(), pz - range), z2 = Math.min(p.getMaxZ(), pz + range);
        if (x1 > x2 || z1 > z2) return 0;
        int midY = Math.clamp((int) Math.floor(pos.y), p.getMinY(), p.getMaxY());
        boolean low = p.getMaxY() - p.getMinY() < 48;
        Vector3f color = color(p.getType());
        int sent = 0;
        long r2 = (long) range * range;
        for (int x = x1; x <= x2 && sent < max; x++) {
            for (int z = z1; z <= z2 && sent < max; z++) {
                long dx = x - px, dz = z - pz;
                if (dx * dx + dz * dz > r2) continue;
                if (!p.contains(x, midY, z)) continue;
                if (p.contains(x + 1, midY, z) && p.contains(x - 1, midY, z) && p.contains(x, midY, z + 1) && p.contains(x, midY, z - 1)) continue;
                int y = surface(world, x, z, (int) Math.floor(pos.y));
                if (low) y = Math.clamp(y, p.getMinY(), p.getMaxY());
                send(viewer, x, y, z, color);
                sent++;
            }
        }
        return sent;
    }

    private static void send(PlayerRef viewer, int x, int y, int z, Vector3f color) {
        Matrix4d m = new Matrix4d().translate(x + 0.5, y + 0.5, z + 0.5).scale(1.02, 1.02, 1.02);
        viewer.getPacketHandler().write(new DisplayDebug(DebugShape.Cube, Matrix4dUtil.asFloatData(m), color, LIFE_S, FLAGS, null, OPACITY));
    }

    /** Sol de la colonne : premier bloc plein en descendant de hint+16 a hint-24 (hint - 1 si rien, ou chunk non charge). */
    private static int surface(World world, int x, int z, int hint) {
        String key = world.getName() + "|" + x + "|" + z;
        long now = System.currentTimeMillis();
        long[] c = SURFACE.get(key);
        if (c != null && now - c[1] < SURFACE_TTL_MS && Math.abs(c[0] - hint) < 40) return (int) c[0];
        int found = hint - 1;
        for (int y = hint + 16; y > hint - 24; y--) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            if (t == null) break;                                                  // chunk non charge
            if (t == BlockType.EMPTY || "Empty".equals(t.getId())) continue;
            found = y;
            break;
        }
        if (SURFACE.size() > 50_000) SURFACE.clear();
        SURFACE.put(key, new long[]{found, now});
        return found;
    }
}
