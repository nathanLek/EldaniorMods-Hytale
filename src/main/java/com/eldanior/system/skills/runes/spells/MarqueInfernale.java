package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de la Marque Infernale (Feu, Rare) : marque un ennemi ; a sa mort il explose et enflamme ses voisins, et le plus
 * proche est marque a son tour (3 rebonds max). S'il survit 6 s, le sceau detone sur lui (sans rebond).
 * Fiche de conception : scripts/runes/specs/Rune_MarqueInfernale.json
 */
public final class MarqueInfernale implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long MARK_MS = 6_000;
    private static final double TARGET_RANGE = 25.0, BLAST_RADIUS = 5.0;
    private static final float MARK_DAMAGE = 30f, BLAST_DAMAGE = 120f;
    private static final int MAX_HOPS = 3;

    // ===== Assets =====
    private static final String MARK = "Elda_MarqueInfernale", BURNING = "Elda_Embrasement";
    private static final String FX_MARK = "Fire_Charge_Soak1", FX_BLAST = "Explosion_Medium", FX_FLAMES = "Effect_Death_Flames";
    private static final String SFX_MARK = "SFX_Flame_Ignite", SFX_BLAST = "SFX_Fireball_Death";

    @Override public String id() { return "MarqueInfernale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, TARGET_RANGE);
        if (target == null) return null;
        Mark m = new Mark();
        m.mark(caster, target, System.currentTimeMillis(), store, cb);
        return m.pos == null ? null : m;
    }

    private static final class Mark implements Instance {
        final Set<Ref<EntityStore>> burnt = new HashSet<>();
        Ref<EntityStore> target;
        Vector3d pos;
        long markedAt;
        int hops;

        void mark(Ref<EntityStore> caster, Ref<EntityStore> t, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            target = t; markedAt = now; pos = SpellFx.position(t, store);
            if (pos == null) return;
            burnt.add(t);
            SpellFx.damage(caster, t, "Fire", MARK_DAMAGE, null, null, cb);
            SpellFx.effect(t, MARK, cb);
            SpellFx.fx(FX_MARK, new Vector3d(pos).add(0, 1, 0), 0.6f, store, cb);
            SpellFx.sound(SFX_MARK, pos, cb);
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean dead = !target.isValid() || store.getComponent(target, DeathComponent.getComponentType()) != null;
            if (!dead) {
                Vector3d p = SpellFx.position(target, store);
                if (p != null) pos = p;
                if (now - markedAt < MARK_MS) return false;
                explode(caster, store, cb);                                   // a survecu : detonation finale, sans rebond
                return true;
            }
            explode(caster, store, cb);                                       // mort (ou retire) : explosion a sa derniere position
            if (++hops > MAX_HOPS) return true;
            Ref<EntityStore> next = nearest(caster, store);
            if (next == null) return true;
            mark(caster, next, now, store, cb);
            return pos == null;
        }

        private void explode(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = new Vector3d(pos).add(0, 1, 0);
            SpellFx.fx(FX_BLAST, at, 0.8f, store, cb);
            SpellFx.fx(FX_FLAMES, at, 0.8f, store, cb);
            SpellFx.sound(SFX_BLAST, pos, cb);
            SpellFx.shake(pos, 12, "Impact_Light", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, BLAST_RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (out.lengthSquared() > 0.25) out.normalize().mul(6);
                SpellFx.damage(caster, e, "Fire", BLAST_DAMAGE, out.add(0, 4, 0), "Impact_Light", cb);
                SpellFx.effect(e, BURNING, cb);
            }
        }

        /** Ennemi vivant le plus proche de l'explosion, jamais marque auparavant. */
        private Ref<EntityStore> nearest(Ref<EntityStore> caster, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestD = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, BLAST_RADIUS, store)) {
                if (burnt.contains(e) || store.getComponent(e, DeathComponent.getComponentType()) != null) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.distanceSquared(pos);
                if (d < bestD) { bestD = d; best = e; }
            }
            return best;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (target == null || !target.isValid()) return;
            try { EffectsManager.removeEffect(target, MARK, cb); } catch (Exception e) { EldaniorLogger.error("MarqueInfernale: effet", e); }
        }
    }
}
