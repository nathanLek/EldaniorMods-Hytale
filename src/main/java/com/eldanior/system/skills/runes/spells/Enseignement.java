package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de l'Enseignement (Lumiere, Epic, rune de predilection du Mentorant Supreme), instantanee : le lanceur et ses allies
 * a 15 blocs recoivent Elda_Enseignement (+30 % de degats infliges 10 s, OutgoingDamage) et 10 % de mana. Un trait de
 * lumiere relie le lanceur a chacun.
 * Fiche de conception : scripts/runes/specs/Rune_Enseignement.json
 */
public final class Enseignement implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RADIUS = 15.0, TRAIL_STEP = 1.5;
    private static final float MANA = 0.10f;

    // ===== Assets =====
    private static final String TAUGHT = "Elda_Enseignement";
    private static final String FX_TRAIL = "Bifrost_Divine", FX_ALLY = "Apollo_Heal_Pulse";
    private static final String SFX_CAST = "SFX_Deployable_Totem_Heal_Spawn";

    @Override public String id() { return "Enseignement"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d from = new Vector3d(pos).add(0, 1.4, 0);
        for (Ref<EntityStore> a : SpellFx.alliesAround(caster, pos, RADIUS, store)) {     // lanceur compris
            Vector3d p = SpellFx.position(a, store);
            if (p == null) continue;
            SpellFx.effect(a, TAUGHT, cb);
            SpellFx.restore(a, DefaultEntityStatTypes.getMana(), MANA, cb);
            SpellFx.fx(FX_ALLY, p, 1.0f, store, cb);
            Vector3d to = new Vector3d(p).add(0, 1.2, 0);
            double len = from.distance(to);
            for (double d = TRAIL_STEP; d < len; d += TRAIL_STEP) SpellFx.fx(FX_TRAIL, new Vector3d(from).lerp(to, d / len), 0.3f, store, cb);
        }
        SpellFx.fx(FX_TRAIL, from, 1.5f, store, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        return null;
    }
}
