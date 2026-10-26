package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de l'Orbe Statique (Foudre, Rare) : orbe electrique lent qui avance 6 s droit devant en suivant le relief et
 * foudroie les ennemis proches, puis explose (fin de duree ou mur). L'orbe est fait de particules seules.
 * Fiche de conception : scripts/runes/specs/Rune_OrbeStatique.json
 */
public final class OrbeStatique implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, FX_EVERY_MS = 100, PULSE_EVERY_MS = 500, GROUND_EVERY_MS = 250, HUM_EVERY_MS = 1_500;
    private static final double SPEED = 5.0, HOVER = 1.3, RADIUS = 4.0;
    private static final float PULSE_DAMAGE = 12f, BLAST_DAMAGE = 90f;
    private static final long STUN_MS = 500;

    // ===== Assets =====
    private static final String STUN = "Taranis_Foudroye";
    private static final String FX_ORB = "Taranis_Electric_Burst", FX_BOLT = "Taranis_Bolt";
    private static final String SFX_HUM = "SFX_Emit_Goblin_Electric_Sparks", SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";
    private static final String SFX_THUNDER = "SFX_Global_Weather_Thunder";

    @Override public String id() { return "OrbeStatique"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d dir = SpellFx.forward(caster, store);
        dir.y = 0;
        if (dir.lengthSquared() < 1e-6) dir.set(0, 0, 1);
        dir.normalize();
        Vector3d at = new Vector3d(pos).add(new Vector3d(dir).mul(1.5)).add(0, HOVER, 0);
        SpellFx.sound(SFX_HUM, at, cb);
        long now = System.currentTimeMillis();
        return new Orb(at, dir, now);
    }

    private static final class Orb implements Instance {
        final Vector3d pos, dir;
        final long startAt;
        long last, lastFx, lastPulse, lastGround, lastHum;
        double floorY;

        Orb(Vector3d pos, Vector3d dir, long startAt) {
            this.pos = pos; this.dir = dir; this.startAt = startAt; this.last = startAt; this.lastHum = startAt;
            this.floorY = pos.y - HOVER;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            World world = SpellFx.world(store);
            if (now - startAt >= DURATION_MS) { explode(caster, store, cb); return true; }
            pos.add(new Vector3d(dir).mul(SPEED * Math.min(0.2, (now - last) / 1000.0)));
            last = now;
            if (now - lastGround >= GROUND_EVERY_MS) {                                // suit le relief en douceur
                lastGround = now;
                floorY = SpellFx.ground(world, new Vector3d(pos.x, floorY + 2, pos.z)).y;
            }
            pos.y += (floorY + HOVER - pos.y) * 0.3;
            int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
            if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) {
                explode(caster, store, cb);
                return true;
            }
            if (now - lastFx >= FX_EVERY_MS) { lastFx = now; SpellFx.fx(FX_ORB, pos, 0.9f, store, cb); }
            if (now - lastHum >= HUM_EVERY_MS) { lastHum = now; SpellFx.sound(SFX_HUM, pos, cb); }
            if (now - lastPulse >= PULSE_EVERY_MS) {
                lastPulse = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RADIUS, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    SpellFx.arc(FX_ORB, pos, p.add(0, 1, 0), 0.35f, store, cb);
                    SpellFx.damage(caster, e, "Lightning", PULSE_DAMAGE, null, null, cb);
                }
            }
            return false;
        }

        private void explode(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_BOLT, new Vector3d(pos.x, floorY, pos.z), 1.0f, store, cb);
            for (int i = 0; i < 3; i++) SpellFx.fx(FX_ORB, new Vector3d(pos).add(0, i * 0.6 - 0.6, 0), 1.6f, store, cb);
            SpellFx.sound(SFX_ZAP, pos, cb);
            SpellFx.sound2dNear(SFX_THUNDER, pos, 30, store);
            SpellFx.shake(pos, 12, "Impact_Light", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (out.lengthSquared() > 0.25) out.normalize().mul(5);
                SpellFx.damage(caster, e, "Lightning", BLAST_DAMAGE, out.add(0, 3, 0), "Impact_Light", cb);
                SpellFx.stun(e, STUN, STUN_MS, store, cb);
            }
        }
    }
}
