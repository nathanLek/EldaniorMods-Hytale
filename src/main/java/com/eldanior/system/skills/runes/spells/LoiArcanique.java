package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rune de la Loi Arcanique (Espace, Unique, rune de predilection du Seigneur Arcanique) : domaine fixe de 15 blocs autour
 * du lanceur pendant 12 s, sans sortie. Tout ennemi entre dans le domaine y est inscrit ; s'il franchit la frontiere, il
 * reapparait au point oppose (a 13 blocs du centre, case libre au sol) : 60 degats, Gaia_Etourdi 0,5 s (au tick suivant,
 * apres la teleportation), pour les 4 premiers renvois. A la fin, chaque inscrit subit 60 degats par renvoi (240 max). Lanceur et allies passent librement.
 * Fiche de conception : scripts/runes/specs/Rune_LoiArcanique.json
 */
public final class LoiArcanique implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, SCAN_EVERY_MS = 250, BORDER_FX_EVERY_MS = 1_000, STUN_MS = 500;
    private static final double RADIUS = 15.0, RETURN_RADIUS = 13.0, FORGET_RADIUS = 25.0, HEIGHT = 8.0;
    private static final int BORDER_POINTS = 24, MAX_VERDICT_COUNT = 4;
    private static final float RETURN_DAMAGE = 60f, VERDICT_PER_ESCAPE = 60f;

    // ===== Assets =====
    private static final String STUN = "Gaia_Etourdi";
    private static final String FX_LAW = "Bifrost_Divine";
    private static final String SFX_OPEN = "SFX_Portal_Neutral_Open", SFX_RETURN = "SFX_Portal_Neutral";

    @Override public String id() { return "LoiArcanique"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        SpellFx.fx(FX_LAW, new Vector3d(pos).add(0, 1, 0), 2.0f, store, cb);
        SpellFx.sound(SFX_OPEN, pos, cb);
        Domain d = new Domain(world, new Vector3d(pos), System.currentTimeMillis());
        d.border(0, store, cb);
        return d;
    }

    /** Pieds d'un corps sur la colonne (x, z) pres de la hauteur y0 : sol solide dessous, 2 cases libres. Null si aucun. */
    static Vector3d feet(World world, double x, double y0, double z) {
        int bx = (int) Math.floor(x), bz = (int) Math.floor(z), by = (int) Math.floor(y0);
        for (int dy = 0; dy <= 8; dy++) {
            for (int y : new int[]{by + dy, by - dy}) {
                if (solid(world, bx, y - 1, bz) == Boolean.TRUE && solid(world, bx, y, bz) == Boolean.FALSE && solid(world, bx, y + 1, bz) == Boolean.FALSE)
                    return new Vector3d(bx + 0.5, y, bz + 0.5);
            }
        }
        return null;
    }

    /** null = chunk non charge. */
    private static Boolean solid(World world, int x, int y, int z) {
        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
        return t == null ? null : t.getMaterial() == BlockMaterial.Solid;
    }

    private static final class Domain implements Instance {
        final World world;
        final Vector3d center;
        final long startAt;
        final Map<Ref<EntityStore>, Integer> escapes = new HashMap<>();       // inscrits -> nombre de renvois
        final List<Ref<EntityStore>> stunNext = new ArrayList<>();
        long lastScan, lastBorder;

        Domain(World world, Vector3d center, long startAt) { this.world = world; this.center = center; this.startAt = startAt; this.lastBorder = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            for (Ref<EntityStore> e : stunNext) if (e.isValid()) SpellFx.stun(e, STUN, STUN_MS, store, cb);
            stunNext.clear();
            if (now - startAt >= DURATION_MS) { verdict(caster, store, cb); return true; }
            if (now - lastBorder >= BORDER_FX_EVERY_MS) { lastBorder = now; border(now - startAt, store, cb); }
            if (now - lastScan < SCAN_EVERY_MS) return false;
            lastScan = now;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS, store)) {   // inscription
                Vector3d p = SpellFx.position(e, store);
                if (p != null && Math.abs(p.y - center.y) <= HEIGHT && !RuneSpellRuntime.isSummon(e)) escapes.putIfAbsent(e, 0);
            }
            escapes.entrySet().removeIf(en -> {
                Ref<EntityStore> e = en.getKey();
                Vector3d p = SpellFx.position(e, store);
                if (p == null) return true;
                double d = SpellFx.horizontalDistance(p, center);
                if (d > FORGET_RADIUS) return true;                               // emporte au loin par autre chose
                if (d > RADIUS) { sendBack(caster, e, p, en.getValue() < MAX_VERDICT_COUNT, store, cb); en.setValue(en.getValue() + 1); }
                return false;
            });
            return false;
        }

        /** Le fuyard reapparait du cote oppose du domaine. */
        private void sendBack(Ref<EntityStore> caster, Ref<EntityStore> e, Vector3d p, boolean punish, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d dir = new Vector3d(center.x - p.x, 0, center.z - p.z);
            if (dir.lengthSquared() < 1e-4) dir.set(1, 0, 0);
            dir.normalize();
            Vector3d dest = feet(world, center.x + dir.x * RETURN_RADIUS, center.y, center.z + dir.z * RETURN_RADIUS);
            if (dest == null) dest = feet(world, center.x, center.y, center.z);
            if (dest == null) dest = new Vector3d(center);
            SpellFx.fx(FX_LAW, new Vector3d(p).add(0, 1, 0), 1.0f, store, cb);
            SpellFx.fx(FX_LAW, new Vector3d(dest).add(0, 1, 0), 1.0f, store, cb);
            SpellFx.sound(SFX_RETURN, p, cb);
            SpellFx.sound(SFX_RETURN, dest, cb);
            SpellFx.teleport(e, dest, store, cb);
            if (!punish) return;                                                  // au-dela de 4 renvois : seulement ramene
            SpellFx.damage(caster, e, "Elemental", RETURN_DAMAGE, null, null, cb);
            stunNext.add(e);
        }

        /** Frontiere visible : eclats qui tournent lentement sur le cercle. */
        void border(long elapsed, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double turn = elapsed / 6000.0;
            for (int i = 0; i < BORDER_POINTS; i++) {
                double a = turn + i * 2 * Math.PI / BORDER_POINTS;
                SpellFx.fx(FX_LAW, new Vector3d(center).add(Math.cos(a) * RADIUS, 1.0, Math.sin(a) * RADIUS), 0.7f, store, cb);
            }
        }

        /** Fin de la loi : chaque fuite est punie. */
        private void verdict(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Map.Entry<Ref<EntityStore>, Integer> en : escapes.entrySet()) {
                int n = Math.min(MAX_VERDICT_COUNT, en.getValue());
                Vector3d p = SpellFx.position(en.getKey(), store);
                if (n == 0 || p == null) continue;
                SpellFx.damage(caster, en.getKey(), "Elemental", VERDICT_PER_ESCAPE * n, null, "Impact_Strong", cb);
                SpellFx.fx(FX_LAW, new Vector3d(p).add(0, 1, 0), 1.5f, store, cb);
            }
            SpellFx.sound(SFX_RETURN, center, cb);
        }
    }
}
