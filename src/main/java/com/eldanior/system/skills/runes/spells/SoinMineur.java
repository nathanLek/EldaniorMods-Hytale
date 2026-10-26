package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de Soin Mineur (Lumiere, Common) : soin instantane de 15 % sur l'allie vise (ou soi sans allie sous le curseur),
 * puis 1 % par seconde pendant 5 s.
 * Fiche de conception : scripts/runes/specs/Rune_SoinMineur.json
 */
public final class SoinMineur implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 20.0;
    private static final float HEAL = 0.15f, REGEN = 0.01f;
    private static final int REGEN_TICKS = 5;
    private static final long REGEN_EVERY_MS = 1_000;

    // ===== Assets =====
    private static final String FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_HEAL = "SFX_Portal_Void";

    @Override public String id() { return "SoinMineur"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> aimed = SpellFx.targetLiving(caster, store, RANGE);
        Ref<EntityStore> target = aimed != null && SpellFx.isAlly(caster, aimed, store) ? aimed : caster;
        Vector3d p = SpellFx.position(target, store);
        if (p == null) return null;
        SpellFx.restore(target, DefaultEntityStatTypes.getHealth(), HEAL, cb);
        SpellFx.fx(FX_HEAL, p.add(0, 1, 0), 1.0f, store, cb);
        SpellFx.sound(SFX_HEAL, p, cb);
        return new Regen(target, System.currentTimeMillis());
    }

    private static final class Regen implements Instance {
        final Ref<EntityStore> target;
        long last;
        int done;

        Regen(Ref<EntityStore> target, long startAt) { this.target = target; this.last = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!target.isValid()) return true;
            if (now - last < REGEN_EVERY_MS) return false;
            last = now;
            SpellFx.restore(target, DefaultEntityStatTypes.getHealth(), REGEN, cb);
            return ++done >= REGEN_TICKS;
        }
    }
}
