package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de la Frappe des Meridiens (Combat, Uncommon, Disciple Martial, voie martiale du Clan Luo) : trois frappes eclair sur
 * l'ennemi vise a 4 blocs (3 x 40, une toutes les 0,15 s) ; ses meridiens sont ensuite scelles 4 s : ralenti
 * (effet Meridiens_Scelles, joueurs) et +15 % de degats subis de toutes sources (RuneSpellRuntime.expose).
 * Fiche de conception : scripts/runes/specs/Rune_FrappeMeridiens.json
 */
public final class FrappeMeridiens implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 4.5;
    private static final float DAMAGE = 40f, VULNERABILITY = 1.15f;
    private static final int STRIKES = 3;
    private static final long STRIKE_MS = 150, SEAL_MS = 4_000;

    // ===== Assets =====
    private static final String FX_HIT = "Impact_Sword_Charged", FX_SEAL = "Nyx_Void", SEAL = "Meridiens_Scelles";
    private static final String SFX_HIT = "SFX_Light_Melee_T2_Lunge", SFX_SEAL = "SFX_Sword_T2_Impact";

    @Override public String id() { return "FrappeMeridiens"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
        if (target == null) return null;
        return new Strikes(target, System.currentTimeMillis());
    }

    private static final class Strikes implements Instance {
        final Ref<EntityStore> target;
        long next;
        int done;

        Strikes(Ref<EntityStore> target, long now) { this.target = target; this.next = now; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (target == null || !target.isValid()) return true;
            if (now < next) return false;
            Vector3d p = SpellFx.position(target, store);
            if (p == null) return true;
            Vector3d point = new Vector3d(p).add(0, 0.7 + 0.4 * done, 0);       // trois points vitaux, de bas en haut
            SpellFx.fx(FX_HIT, point, 0.8f, store, cb);
            SpellFx.sound(SFX_HIT, point, cb);
            SpellFx.damage(caster, target, "Physical", DAMAGE, null, "Impact_Light", cb);
            done++;
            next = now + STRIKE_MS;
            if (done < STRIKES) return false;
            SpellFx.effect(target, SEAL, cb);                                   // meridiens scelles
            RuneSpellRuntime.expose(target, VULNERABILITY, now + SEAL_MS);
            SpellFx.fx(FX_SEAL, new Vector3d(p).add(0, 1.1, 0), 0.6f, store, cb);
            SpellFx.sound(SFX_SEAL, p, cb);
            return true;
        }
    }
}
