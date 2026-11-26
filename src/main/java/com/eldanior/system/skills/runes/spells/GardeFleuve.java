package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Garde du Fleuve (Combat, Rare, Expert Martial, voie martiale du Clan Luo) : garde de 1,5 s ; le premier coup
 * recu est annule (onOwnerHurt -> 0) et le Ki le renvoie a l'attaquant (250 degats, repousse) s'il est a moins de 8 blocs.
 * Un coup sans attaquant (chute, feu) est seulement annule.
 * Fiche de conception : scripts/runes/specs/Rune_GardeFleuve.json
 */
public final class GardeFleuve implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long GUARD_MS = 1_500, PULSE_MS = 300;
    private static final float RIPOSTE = 250f;
    private static final double RIPOSTE_RANGE = 8.0, PUSH = 9.0, LIFT = 3.0;

    // ===== Assets =====
    private static final String FX_GUARD = "Ability_Ice_Impact", FX_HIT = "Impact_Sword_Charged", FX_RING = "Eldanior_Pas_Celeste";
    private static final String SFX_PARRY = "SFX_Longsword_Special_Impact", SFX_SLASH = "SFX_Ability_Wind_Strike_Slash";

    @Override public String id() { return "GardeFleuve"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos != null) SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 1.0f, store, cb);
        return new Guard(System.currentTimeMillis());
    }

    private static final class Guard implements Instance {
        final long startAt;
        volatile boolean parried;
        volatile Ref<EntityStore> attacker;
        long lastPulse;

        Guard(long startAt) { this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (parried || amount <= 0) return amount;
            parried = true;
            this.attacker = attacker;
            return 0f;                                                          // le fleuve contourne le coup
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d me = SpellFx.position(caster, store);
            if (me == null) return true;
            if (parried) {
                SpellFx.fx(FX_RING, new Vector3d(me).add(0, 0.1, 0), 1.6f, store, cb);
                SpellFx.sound(SFX_PARRY, me, cb);
                Ref<EntityStore> foe = attacker;
                Vector3d p = foe != null && foe.isValid() ? SpellFx.position(foe, store) : null;
                if (p != null && p.distance(me) <= RIPOSTE_RANGE && SpellFx.isEnemy(caster, foe, store)) {
                    Vector3d dir = new Vector3d(p).sub(me);
                    dir.y = 0;
                    if (dir.lengthSquared() < 1e-4) dir.set(0, 0, 1);
                    dir.normalize();
                    Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                    SpellFx.fx(FX_HIT, chest, 1.5f, store, cb);
                    SpellFx.sound(SFX_SLASH, chest, cb);
                    SpellFx.damage(caster, foe, "Physical", RIPOSTE, new Vector3d(dir).mul(PUSH).add(0, LIFT, 0), "Impact_Strong", cb);
                }
                return true;
            }
            if (now - lastPulse >= PULSE_MS) {                                  // eau qui tourne autour de la garde
                lastPulse = now;
                double a = (now - startAt) / 250.0;
                SpellFx.fx(FX_GUARD, new Vector3d(me).add(Math.cos(a) * 1.1, 1.0, Math.sin(a) * 1.1), 0.5f, store, cb);
            }
            return now - startAt >= GUARD_MS;
        }
    }
}
