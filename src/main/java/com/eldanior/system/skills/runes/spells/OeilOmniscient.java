package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de l'Oeil Omniscient (Espace, Unique, rune de predilection du Visionnaire Supreme) : un oeil s'ouvre au-dessus du
 * lanceur pendant 15 s. Clic droit sur un ennemi visible (40 blocs, sans traverser de bloc) : les deux echangent leurs
 * places, l'ennemi subit 150 degats et reste etourdi 1,5 s. 4 echanges au plus, 1 s d'ecart. Le clic gauche reste une
 * attaque normale.
 * Fiche de conception : scripts/runes/specs/Rune_OeilOmniscient.json
 */
public final class OeilOmniscient implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, SWAP_GAP_MS = 1_000, STUN_MS = 1_500, EYE_EVERY_MS = 150, FALL_GUARD_MS = 2_000;
    private static final double RANGE = 40.0, EYE_HEIGHT = 2.8;
    private static final int SWAPS = 4;
    private static final float DAMAGE = 150f;

    // ===== Assets =====
    private static final String STUN = "Gaia_Etourdi";
    private static final String FX_EYE = "Astral_Esprit", FX_FLASH = "Bifrost_Divine";
    private static final String SFX_OPEN = "SFX_Portal_Neutral_Open", SFX_SWAP = "SFX_Portal_Neutral";

    @Override public String id() { return "OeilOmniscient"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.fx(FX_FLASH, new Vector3d(pos).add(0, EYE_HEIGHT, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_OPEN, pos, cb);
        return new Eye(System.currentTimeMillis());
    }

    private static final class Eye implements Instance {
        final long startAt;
        int swapsLeft = SWAPS;
        long lastSwap, lastEye, stunAt;
        boolean swapWanted;
        Ref<EntityStore> stunned;                                            // etourdi au tick suivant : la teleportation doit d'abord s'appliquer

        Eye(long startAt) { this.startAt = startAt; }

        @Override public boolean listensClicks() { return swapsLeft > 0; }

        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!left) swapWanted = true;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            if (stunned != null && now >= stunAt) { SpellFx.stun(stunned, STUN, STUN_MS, store, cb); stunned = null; }
            if (stunned == null && (now - startAt >= DURATION_MS || swapsLeft <= 0)) return true;
            if (swapWanted) {
                swapWanted = false;
                if (now - lastSwap >= SWAP_GAP_MS && swap(caster, pos, store, cb)) { lastSwap = now; swapsLeft--; }
            }
            if (now - lastEye >= EYE_EVERY_MS) {                                  // l'oeil flotte et oscille au-dessus de la tete
                lastEye = now;
                double t = (now - startAt) / 1000.0;
                SpellFx.fx(FX_EYE, new Vector3d(pos).add(Math.cos(t) * 0.3, EYE_HEIGHT + Math.sin(t * 2) * 0.15, Math.sin(t) * 0.3), 1.4f, store, cb);
            }
            return false;
        }

        /** Echange de places avec l'ennemi sous le curseur. */
        private boolean swap(Ref<EntityStore> caster, Vector3d pos, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
            Vector3d tp = target != null && !RuneSpellRuntime.isSummon(target) ? SpellFx.position(target, store) : null;
            if (tp == null) return false;
            Vector3d mine = new Vector3d(pos);
            SpellFx.fx(FX_FLASH, new Vector3d(mine).add(0, 1, 0), 1.2f, store, cb);
            SpellFx.fx(FX_FLASH, new Vector3d(tp).add(0, 1, 0), 1.2f, store, cb);
            SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
            SpellFx.teleport(caster, tp, store, cb);
            SpellFx.teleport(target, mine, store, cb);
            SpellFx.damage(caster, target, "Elemental", DAMAGE, null, "Impact_Light", cb);
            stunned = target;
            stunAt = System.currentTimeMillis() + 60;
            SpellFx.sound(SFX_SWAP, tp, cb);
            SpellFx.sound(SFX_SWAP, mine, cb);
            return true;
        }
    }
}
