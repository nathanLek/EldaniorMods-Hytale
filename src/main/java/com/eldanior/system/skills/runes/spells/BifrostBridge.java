package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Pont du Bifrost — sort divin de teleportation (Rune_PontBifrost), le pont arc-en-ciel des dieux.
 *
 *  1. Visee : un cercle runique suit le curseur jusqu'a {@link #RANGE} blocs (en visant le ciel, la destination est a
 *     portee maximale droit devant) ; un arc-en-ciel en pointilles dessine deja le trajet. Un clic confirme ; sans clic,
 *     le sort part seul apres {@link #TARGET_MS}.
 *  2. Convocation ({@link #CHANNEL_MS}) : une colonne de lumiere runique s'abat sur le lanceur et le pont arc-en-ciel
 *     (7 bandes de couleur) se deploie dans le ciel du depart jusqu'a la destination.
 *  3. Voyage : le lanceur et ses allies proches deviennent lumiere et filent sur le pont ; ils reapparaissent au-dessus
 *     de la destination (le temps que la zone se charge si elle est lointaine) puis descendent le faisceau.
 *  4. Impact : le Bifrost frappe le sol comme un eclair divin — onde de choc, degats et projection sur
 *     {@link #ARRIVAL_RADIUS} blocs — et grave dans le sol le sceau runique brule du Bifrost (restaure apres 3 min).
 */
public final class BifrostBridge {

    private BifrostBridge() {}

    // ===== Reglages =====
    private static final double RANGE = 600.0, START_DISTANCE = 20.0;
    private static final long TARGET_MS = 6_000, CHANNEL_MS = 1_500, COMET_MS = 700, ARRIVE_TIMEOUT_MS = 10_000, FADE_MS = 4_500, PILLAR_MS = 4_000, PILLAR_EVERY_MS = 150;
    private static final double ESCORT_RADIUS = 8.0;           // allies emportes avec le lanceur
    private static final double SKY_ABOVE = 22.0, DESCENT_SPEED = 32.0, LANDED = 1.5;
    private static final float ARRIVAL_DAMAGE = 900f;
    private static final double ARRIVAL_RADIUS = 12.0, ARRIVAL_PUSH = 20.0, ARRIVAL_LIFT = 10.0;
    private static final double RIBBON_STEP = 3.0;
    private static final int PREVIEW_POINTS = 18;
    private static final int SEAL_RADIUS = 10;
    private static final long RESTORE_AFTER_MS = 180_000;

    // ===== Assets =====
    private static final String COLUMN_PROJECTILE = "Projectile_Config_Bifrost_Column";
    private static final String MARKER_PROJECTILE = "Projectile_Config_Bifrost_Marker";
    private static final String FX_DIVINE = "Bifrost_Divine", FX_RIBBON = "Bifrost_Ribbon", FX_PILLAR = "Bifrost_Pillar";
    private static final String FX_SHOCKWAVE = "Chaos_Shockwave", FX_DUST = "Gaia_Dust_Burst";
    private static final String SEAL_LINE = "Rock_Basalt", SEAL_SCORCH = "Soil_Dirt_Burnt";
    private static final String SFX_SUMMON = "SFX_Portal_Neutral_Open";
    private static final String SFX_TRAVEL = "SFX_Dungeon_Goblin_Portal_Void_Teleport_Cinematic";
    private static final String SFX_IMPACT = "SFX_Golem_Frost_Slam_Impact";

    enum Phase { TARGETING, CHANNEL, ARRIVING, LANDED }

    record Traveller(Ref<EntityStore> ref, Vector3d offset) {}

    static final class Bridge {
        final long castAt;
        Phase phase = Phase.TARGETING;
        Vector3d from, to;
        double peak, built;
        long phaseAt, lastSpark, lastPillar;
        Integer groundY;                                     // sol de la destination, connu une fois la zone chargee
        Set<Integer> seenPrimary, seenSecondary;
        Ref<EntityStore> marker;
        final List<Ref<EntityStore>> columns = new ArrayList<>();
        final List<Traveller> travellers = new ArrayList<>();
        Bridge(long castAt) { this.castAt = castAt; }
    }

    private static final Map<UUID, List<Bridge>> bridges = new ConcurrentHashMap<>();

    public static void cast(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        if (uuid == null) return;
        Bridge b = new Bridge(System.currentTimeMillis());
        b.seenPrimary = SpellFx.chains(caster, store, InteractionType.Primary);
        b.seenSecondary = SpellFx.chains(caster, store, InteractionType.Secondary);
        bridges.computeIfAbsent(uuid, k -> new CopyOnWriteArrayList<>()).add(b);
    }

    static void tick(UUID uuid, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Bridge> list = bridges.get(uuid);
        if (list == null) return;
        long now = System.currentTimeMillis();
        for (Bridge b : list) if (tickBridge(caster, b, now, store, cb)) list.remove(b);
        if (list.isEmpty()) bridges.remove(uuid);
    }

    private static boolean tickBridge(Ref<EntityStore> caster, Bridge b, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d casterPos = SpellFx.position(caster, store);
        if (casterPos == null) return true;
        World world = SpellFx.world(store);
        switch (b.phase) {
            case TARGETING -> {
                aim(caster, b, casterPos, world, store);
                SpellFx.hold(b.marker, new Vector3d(b.to).add(0, 0.1, 0), null, store);
                if (b.marker == null) b.marker = SpellFx.spawnModel(caster, MARKER_PROJECTILE, new Vector3d(b.to).add(0, 0.1, 0), new Vector3d(1, 0, 0), cb);
                if (now - b.lastSpark >= 300) {                                           // trajet en pointilles arc-en-ciel
                    b.lastSpark = now;
                    b.from = new Vector3d(casterPos);
                    b.peak = peak(b);
                    for (int i = 1; i < PREVIEW_POINTS; i++) SpellFx.fx(FX_RIBBON, arc(b, i / (double) PREVIEW_POINTS), 0.45f, store, cb);
                    SpellFx.fx(FX_DIVINE, new Vector3d(b.to).add(0, 0.6, 0), 0.8f, store, cb);
                }
                if (clicked(caster, b, store) || now - b.castAt >= TARGET_MS) {
                    b.phase = Phase.CHANNEL;
                    b.phaseAt = now;
                    b.from = new Vector3d(casterPos);
                    b.peak = peak(b);
                    SpellFx.remove(b.marker, cb);
                    b.columns.add(SpellFx.spawnModel(caster, COLUMN_PROJECTILE, b.from, new Vector3d(1, 0, 0), cb));
                    for (int h = 0; h <= 40; h += 5) SpellFx.fx(FX_DIVINE, new Vector3d(b.from).add(0, h, 0), 3.0f, store, cb);
                    SpellFx.sound(SFX_SUMMON, b.from, cb);
                    SpellFx.shake(b.from, 30, "Impact_Light", store);
                }
            }
            case CHANNEL -> {
                // le pont se deploie du depart vers la destination
                double length = Math.max(1, arcLength(b)), target = length * Math.min(1.0, (now - b.phaseAt) / (double) CHANNEL_MS);
                for (; b.built < target; b.built += RIBBON_STEP) SpellFx.fx(FX_RIBBON, arc(b, b.built / length), 1.0f, store, cb);
                if (now - b.lastSpark >= 250) {
                    b.lastSpark = now;
                    double rise = (now - b.phaseAt) / (double) CHANNEL_MS * 14;
                    SpellFx.fx(FX_DIVINE, new Vector3d(b.from).add(0, rise, 0), 1.6f, store, cb);
                }
                if (now - b.phaseAt >= CHANNEL_MS) {
                    depart(caster, b, world, store, cb);
                    b.phase = Phase.ARRIVING;
                    b.phaseAt = now;
                }
            }
            case ARRIVING -> {
                long t = now - b.phaseAt;
                if (t < COMET_MS) {                                                        // la lumiere file sur le pont
                    double u = t / (double) COMET_MS;
                    SpellFx.fx(FX_DIVINE, arc(b, u), 2.5f, store, cb);
                    SpellFx.fx(FX_RIBBON, arc(b, u), 1.6f, store, cb);
                }
                if (b.groundY == null) {
                    b.groundY = surface(world, (int) Math.floor(b.to.x), (int) Math.floor(b.to.z), (int) Math.floor(b.to.y));
                    if (b.groundY != null) b.to = new Vector3d(b.to.x, b.groundY + 1.0, b.to.z);
                }
                if (b.groundY != null) pillar(b, now, store, cb);                              // ils descendent dans le pilier
                boolean down = true;
                for (Traveller tr : b.travellers) {
                    Vector3d p = SpellFx.position(tr.ref(), store);
                    if (p == null) continue;
                    if (b.groundY == null) { SpellFx.push(tr.ref(), new Vector3d(0, 0.5, 0), false, cb); down = false; continue; }   // la zone se charge : il plane
                    double above = p.y - (b.groundY + 1.0);
                    if (above > LANDED) { SpellFx.push(tr.ref(), new Vector3d(0, -DESCENT_SPEED, 0), false, cb); down = false; }
                }
                boolean timeout = t >= ARRIVE_TIMEOUT_MS;
                if (down && t >= COMET_MS || timeout) {
                    if (timeout && b.groundY != null) for (Traveller tr : b.travellers) SpellFx.teleport(tr.ref(), new Vector3d(b.to).add(tr.offset()), store, cb);
                    impact(caster, b, world, store, cb);
                    b.phase = Phase.LANDED;
                    b.phaseAt = now;
                }
            }
            case LANDED -> {
                if (now - b.phaseAt < PILLAR_MS) pillar(b, now, store, cb);
                if (now - b.phaseAt >= FADE_MS) {
                    for (Ref<EntityStore> c : b.columns) {
                        Vector3d p = SpellFx.position(c, store);
                        if (p != null) SpellFx.fx(FX_DIVINE, new Vector3d(p).add(0, 2, 0), 2.5f, store, cb);
                        SpellFx.remove(c, cb);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /** Pilier de lumiere de 7 blocs de diametre qui s'eleve a 200 blocs au-dessus de la destination. */
    private static void pillar(Bridge b, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (now - b.lastPillar < PILLAR_EVERY_MS) return;
        b.lastPillar = now;
        SpellFx.fx(FX_PILLAR, new Vector3d(b.to).add(0, -0.5, 0), 1.0f, store, cb);
    }

    /** Destination : le bloc vise, ou a portee maximale droit devant (ciel, zone pas encore chargee). */
    private static void aim(Ref<EntityStore> caster, Bridge b, Vector3d casterPos, World world, Store<EntityStore> store) {
        Vector3i block = TargetUtil.getTargetBlock(caster, RANGE, store);
        if (block != null) { b.to = new Vector3d(block.x + 0.5, block.y + 1.0, block.z + 0.5); return; }
        Vector3d look = SpellFx.lookDirection(caster, store);
        if (look.y < -0.2 && b.to != null) return;                                     // vise le vide sous lui : garde la derniere cible
        Vector3d far = new Vector3d(casterPos).add(SpellFx.forward(caster, store).mul(look.y > 0.2 || b.to == null ? RANGE : START_DISTANCE));
        Integer g = surface(world, (int) Math.floor(far.x), (int) Math.floor(far.z), (int) Math.floor(casterPos.y));
        b.to = g != null ? new Vector3d(far.x, g + 1.0, far.z) : far;
    }

    /** Le lanceur et ses allies proches deviennent lumiere et reapparaissent au-dessus de la destination. */
    private static void depart(Ref<EntityStore> caster, Bridge b, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        List<Ref<EntityStore>> refs = SpellFx.alliesAround(caster, b.from, ESCORT_RADIUS, store);
        if (!refs.contains(caster)) refs.add(caster);
        b.groundY = surface(world, (int) Math.floor(b.to.x), (int) Math.floor(b.to.z), (int) Math.floor(b.to.y));
        if (b.groundY != null) b.to = new Vector3d(b.to.x, b.groundY + 1.0, b.to.z);
        Vector3d sky = new Vector3d(b.to).add(0, SKY_ABOVE, 0);
        for (Ref<EntityStore> r : refs) {
            Vector3d p = SpellFx.position(r, store);
            if (p == null) continue;
            Vector3d offset = new Vector3d(p).sub(b.from);
            offset.y = 0;
            b.travellers.add(new Traveller(r, offset));
            SpellFx.fx(FX_DIVINE, new Vector3d(p).add(0, 1, 0), 3.0f, store, cb);
            SpellFx.teleport(r, new Vector3d(sky).add(offset), store, cb);
            SpellFx.protectFromFall(r, ARRIVE_TIMEOUT_MS + 5_000, store);
        }
        for (int h = 0; h <= 30; h += 6) SpellFx.fx(FX_RIBBON, new Vector3d(b.from).add(0, h, 0), 1.8f, store, cb);
        SpellFx.sound(SFX_TRAVEL, b.from, cb);
    }

    /** Le Bifrost frappe : faisceau, onde de choc, degats et projection, sceau runique grave dans le sol. */
    private static void impact(Ref<EntityStore> caster, Bridge b, World world, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d at = b.to;
        b.lastPillar = 0;
        pillar(b, System.currentTimeMillis(), store, cb);
        b.columns.add(SpellFx.spawnModel(caster, COLUMN_PROJECTILE, at, new Vector3d(1, 0, 0), cb));
        for (int h = 0; h <= 48; h += 4) SpellFx.fx(h % 8 == 0 ? FX_DIVINE : FX_RIBBON, new Vector3d(at).add(0, h, 0), h == 0 ? 7.0f : 2.6f, store, cb);
        for (int i = 0; i < 28; i++) {                                                        // anneau arc-en-ciel qui s'etend
            double a = Math.PI * 2 * i / 28;
            SpellFx.fx(FX_RIBBON, new Vector3d(at).add(Math.cos(a) * 7, 0.8, Math.sin(a) * 7), 1.2f, store, cb);
        }
        SpellFx.fx(FX_SHOCKWAVE, new Vector3d(at).add(0, 0.3, 0), 7.0f, store, cb);
        SpellFx.fx(FX_DUST, new Vector3d(at).add(0, 0.5, 0), 5.0f, store, cb);
        SpellFx.sound(SFX_IMPACT, at, cb);
        SpellFx.sound(SFX_TRAVEL, at, cb);
        SpellFx.shake(at, 60, "Impact_Strong", store);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, ARRIVAL_RADIUS, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null) continue;
            Vector3d out = new Vector3d(p.x - at.x, 0, p.z - at.z);
            double d = out.length();
            if (d < 1e-2) out.set(1, 0, 0);
            double k = 1 - Math.min(1, d / ARRIVAL_RADIUS) * 0.5;
            out.normalize().mul(ARRIVAL_PUSH * k).add(0, ARRIVAL_LIFT * k, 0);
            SpellFx.damage(caster, e, "Physical", (float) (ARRIVAL_DAMAGE * k), out, "Impact_Strong", cb);
        }
        if (b.groundY != null) seal(world, (int) Math.floor(at.x), b.groundY, (int) Math.floor(at.z));
    }

    /**
     * Sceau du Bifrost brule dans le sol : cercles concentriques, rayons et entrelacs (noeuds nordiques) en pierre
     * sombre, sol calcine entre eux.
     */
    private static void seal(World world, int cx, int cy, int cz) {
        TerrainRestore.Restore batch = TerrainRestore.open(world, System.currentTimeMillis() + RESTORE_AFTER_MS);
        world.execute(() -> {
            try {
                for (int dx = -SEAL_RADIUS; dx <= SEAL_RADIUS; dx++) {
                    for (int dz = -SEAL_RADIUS; dz <= SEAL_RADIUS; dz++) {
                        double r = Math.hypot(dx, dz);
                        if (r > SEAL_RADIUS + 0.5) continue;
                        String mark = sealCell(dx, dz, r);
                        if (mark == null) continue;
                        int x = cx + dx, z = cz + dz;
                        if (TerrainRestore.isProtectedArea(world.getName(), x, cy, z)) continue;
                        Integer y = surface(world, x, z, cy);
                        if (y == null || Math.abs(y - cy) > 3) continue;
                        BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
                        if (type == null || TerrainRestore.isKept(world, type.getId(), x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) continue;
                        TerrainRestore.place(world, batch, x, y, z, type.getId(), mark, 0);
                    }
                }
            } catch (Exception e) {
                EldaniorLogger.error("BifrostBridge: sceau", e);
            }
        });
    }

    /** Motif du sceau a la cellule (dx, dz) : trait, sol brule, ou rien. */
    private static String sealCell(int dx, int dz, double r) {
        double a = Math.atan2(dz, dx);
        if (Math.abs(r - 3) < 0.6 || Math.abs(r - 6.5) < 0.6 || Math.abs(r - SEAL_RADIUS) < 0.6) return SEAL_LINE;   // cercles
        if (r > 3 && r < SEAL_RADIUS) {
            double spoke = Math.abs(Math.IEEEremainder(a, Math.PI / 4)) * r;              // 8 rayons
            if (spoke < 0.55) return SEAL_LINE;
            for (int i = 0; i < 8; i++) {                                                     // entrelacs entre les rayons
                double b = Math.PI / 8 + i * Math.PI / 4;
                if (Math.abs(Math.hypot(dx - Math.cos(b) * 8.2, dz - Math.sin(b) * 8.2) - 1.6) < 0.5) return SEAL_LINE;
            }
        }
        if (r <= 1.5) return SEAL_LINE;
        return r < SEAL_RADIUS ? SEAL_SCORCH : null;
    }

    /** Sol (bloc plein le plus haut, hors plantes et feuillages) a la colonne x, z ; null si la zone n'est pas chargee. */
    private static Integer surface(World world, int x, int z, int hintY) {
        if (WorldBlockUtil.getBlockType(world, x, Math.max(1, hintY), z) == null) return null;
        for (int y = Math.min(318, hintY + 160); y > Math.max(0, hintY - 200); y--) {
            BlockType type = WorldBlockUtil.getBlockType(world, x, y, z);
            if (type == null) return null;
            if (TerrainRestore.isEmpty(world, x, y, z)) { if (WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) return y; continue; }
            String id = type.getId();
            if (id.contains("Leaves") || id.startsWith("Plant_") || id.startsWith("Wood_")) continue;
            return y;
        }
        return null;
    }

    /** Point du pont a la fraction u (0 : depart, 1 : destination) : arc qui monte haut dans le ciel. */
    private static Vector3d arc(Bridge b, double u) {
        return new Vector3d(b.from.x + (b.to.x - b.from.x) * u, b.from.y + 1.5 + (b.to.y - b.from.y) * u + b.peak * Math.sin(Math.PI * u),
                b.from.z + (b.to.z - b.from.z) * u);
    }

    private static double peak(Bridge b) {
        return Math.max(15, Math.min(90, SpellFx.horizontalDistance(b.from, b.to) * 0.2));
    }

    private static double arcLength(Bridge b) {
        double len = 0;
        Vector3d prev = arc(b, 0);
        for (int i = 1; i <= 24; i++) { Vector3d p = arc(b, i / 24.0); len += p.distance(prev); prev = p; }
        return len;
    }

    /** Nouveau clic (gauche ou droit) depuis le lancement. */
    private static boolean clicked(Ref<EntityStore> caster, Bridge b, Store<EntityStore> store) {
        Set<Integer> primary = SpellFx.chains(caster, store, InteractionType.Primary);
        Set<Integer> secondary = SpellFx.chains(caster, store, InteractionType.Secondary);
        boolean click = false;
        for (Integer id : primary) if (!b.seenPrimary.contains(id)) click = true;
        for (Integer id : secondary) if (!b.seenSecondary.contains(id)) click = true;
        b.seenPrimary = primary;
        b.seenSecondary = secondary;
        return click;
    }

    public static void handleDisconnect(UUID uuid) { bridges.remove(uuid); }
}
