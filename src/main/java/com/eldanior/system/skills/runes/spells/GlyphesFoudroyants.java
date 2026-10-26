package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Rune des Glyphes Foudroyants (Foudre, Rare) : trois glyphes-pieges en triangle au point vise. Un ennemi qui marche
 * sur un glyphe est foudroye (le glyphe s'eteint) ; au bout de 6 s, les glyphes restants detonent tous.
 * Fiche de conception : scripts/runes/specs/Rune_GlyphesFoudroyants.json
 */
public final class GlyphesFoudroyants implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, GLOW_EVERY_MS = 400, STUN_MS = 500;
    private static final double TARGET_RANGE = 20.0, FALLBACK = 8.0, SPREAD = 2.5, TRIGGER = 1.6, SPLASH = 2.5;
    private static final float DAMAGE = 110f, SPLASH_DAMAGE = 40f;

    // ===== Assets =====
    private static final String STUN = "Taranis_Foudroye";
    private static final String FX_GLYPH = "Taranis_Electric_Burst", FX_BOLT = "Taranis_Bolt";
    private static final String SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap", SFX_THUNDER = "SFX_Global_Weather_Thunder";
    private static final String SFX_DRAW = "SFX_Emit_Goblin_Electric_Sparks";

    @Override public String id() { return "GlyphesFoudroyants"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        World world = SpellFx.world(store);
        Vector3d center = SpellFx.targetPoint(caster, store, TARGET_RANGE, FALLBACK);
        Vector3d fwd = SpellFx.forward(caster, store);
        double base = Math.atan2(fwd.z, fwd.x);
        List<Vector3d> glyphs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double a = base + i * Math.PI * 2 / 3;
            glyphs.add(SpellFx.ground(world, new Vector3d(center).add(Math.cos(a) * SPREAD, 0, Math.sin(a) * SPREAD)));
        }
        for (Vector3d g : glyphs) SpellFx.fx(FX_GLYPH, new Vector3d(g).add(0, 0.2, 0), 0.8f, store, cb);
        SpellFx.sound(SFX_DRAW, center, cb);
        return new Traps(glyphs, System.currentTimeMillis());
    }

    private static final class Traps implements Instance {
        final List<Vector3d> glyphs;
        final long startAt;
        long lastGlow;

        Traps(List<Vector3d> glyphs, long startAt) { this.glyphs = glyphs; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) {                                       // detonation des glyphes restants
                for (Vector3d g : glyphs) strike(caster, g, null, store, cb);
                return true;
            }
            if (now - lastGlow >= GLOW_EVERY_MS) {
                lastGlow = now;
                for (Vector3d g : glyphs) SpellFx.fx(FX_GLYPH, new Vector3d(g).add(0, 0.2, 0), 0.5f, store, cb);
            }
            glyphs.removeIf(g -> {
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g, TRIGGER + 1, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || SpellFx.horizontalDistance(p, g) > TRIGGER || Math.abs(p.y - g.y) > 2) continue;
                    strike(caster, g, e, store, cb);
                    return true;
                }
                return false;
            });
            return glyphs.isEmpty();
        }

        /** La foudre tombe sur le glyphe : la victime (s'il y en a une) prend le coup plein, les voisins l'eclat. */
        private void strike(Ref<EntityStore> caster, Vector3d g, Ref<EntityStore> victim, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.fx(FX_BOLT, g, 0.7f, store, cb);
            SpellFx.fx(FX_GLYPH, new Vector3d(g).add(0, 1, 0), 1.2f, store, cb);
            SpellFx.sound(SFX_ZAP, g, cb);
            SpellFx.sound2dNear(SFX_THUNDER, g, 25, store);
            if (victim != null) {
                SpellFx.damage(caster, victim, "Lightning", DAMAGE, null, "Impact_Light", cb);
                SpellFx.stun(victim, STUN, STUN_MS, store, cb);
            }
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g, SPLASH, store)) {
                if (e.equals(victim)) continue;
                SpellFx.damage(caster, e, "Lightning", victim == null ? DAMAGE * 0.6f : SPLASH_DAMAGE, null, null, cb);
            }
        }
    }
}
