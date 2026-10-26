package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de la Chaine d'Eclairs (Foudre, Rare) : eclair qui frappe l'ennemi vise puis saute sur l'ennemi le plus proche,
 * jusqu'a 5 cibles (chacune une fois), en perdant 15 % a chaque saut. Un saut toutes les 120 ms (visible).
 * Fiche de conception : scripts/runes/specs/Rune_ChaineDEclairs.json
 */
public final class ChaineDEclairs implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 25.0, JUMP_RANGE = 8.0;
    private static final float DAMAGE = 140f, FALLOFF = 0.85f;
    private static final int MAX_TARGETS = 5;
    private static final long HOP_EVERY_MS = 120, STUN_MS = 300;

    // ===== Assets =====
    private static final String STUN = "Taranis_Foudroye";
    private static final String FX_SPARK = "Taranis_Electric_Burst";
    private static final String SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap", SFX_THUNDER = "SFX_Global_Weather_Thunder";

    @Override public String id() { return "ChaineDEclairs"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d hand = new Vector3d(pos).add(0, 1.4, 0).add(SpellFx.forward(caster, store).mul(0.6));
        Ref<EntityStore> first = SpellFx.targetEntity(caster, store, RANGE);
        if (first == null) {
            SpellFx.arc(FX_SPARK, hand, new Vector3d(hand).add(SpellFx.lookDirection(caster, store).mul(10)), 0.4f, store, cb);
            return null;
        }
        Chain c = new Chain(hand);
        SpellFx.sound2dNear(SFX_THUNDER, pos, 30, store);
        c.strike(caster, first, store, cb);
        c.lastHop = System.currentTimeMillis();
        return c.from == null ? null : c;
    }

    private static final class Chain implements Instance {
        final Set<Ref<EntityStore>> struck = new HashSet<>();
        Vector3d from;
        float damage = DAMAGE;
        long lastHop;

        Chain(Vector3d hand) { this.from = hand; }

        void strike(Ref<EntityStore> caster, Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d p = SpellFx.position(target, store);
            if (p == null) { from = null; return; }
            Vector3d to = p.add(0, 1, 0);
            struck.add(target);
            SpellFx.arc(FX_SPARK, from, to, 0.45f, store, cb);
            SpellFx.fx(FX_SPARK, to, 1.0f, store, cb);
            SpellFx.sound(SFX_ZAP, to, cb);
            SpellFx.damage(caster, target, "Lightning", damage, null, "Impact_Light", cb);
            SpellFx.stun(target, STUN, STUN_MS, store, cb);
            damage *= FALLOFF;
            from = to;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - lastHop < HOP_EVERY_MS) return false;
            lastHop = now;
            if (struck.size() >= MAX_TARGETS) return true;
            Ref<EntityStore> next = null;
            double best = JUMP_RANGE * JUMP_RANGE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, from, JUMP_RANGE, store)) {
                if (struck.contains(e) || store.getComponent(e, DeathComponent.getComponentType()) != null) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                double d = p.add(0, 1, 0).distanceSquared(from);
                if (d < best) { best = d; next = e; }
            }
            if (next == null) return true;
            strike(caster, next, store, cb);
            return from == null;
        }
    }
}
