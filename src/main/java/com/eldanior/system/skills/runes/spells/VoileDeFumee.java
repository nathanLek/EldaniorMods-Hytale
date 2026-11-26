package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.skills.system.InvisibilityManager;
import com.eldanior.system.util.NpcAttitudes;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Rune du Voile de Fumée (Tenebres, Common, rune propre de l'Assassin) : bombe fumigene aux pieds, grand nuage (rayon ~5).
 * Le lanceur est invisible 3 s (InvisibilityManager, totale : le modele se dissout, ModelVFX Elda_Disparition) ; les
 * monstres a moins de 8 blocs perdent sa trace (attitude IGNORE forcee et cible verrouillee effacee, rafraichies toutes
 * les 0,5 s). Son premier coup porte pendant le voile inflige 50 % en plus (coup echo) et le revele.
 * Fiche de conception : scripts/runes/specs/Rune_VoileDeFumee.json
 */
public final class VoileDeFumee implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 3_000, REFRESH_MS = 500, SMOKE_EVERY_MS = 1_000;
    private static final double RADIUS = 8.0;
    private static final float BONUS = 0.5f;

    // ===== Assets =====
    private static final String FX_CLOUD = "Voile_Fumee", FX_SMOKE = "Divine_Smoke_Short", FX_AMBUSH = "Impact_Critical";
    private static final String SFX_BOMB = "SFX_Bomb_Fuse";

    @Override public String id() { return "VoileDeFumee"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        UUID uuid = SpellFx.uuid(caster, store);
        if (pos == null) return null;
        if (uuid != null) InvisibilityManager.applyInvisibility(uuid, caster, InvisibilityManager.InvisType.TOTAL, DURATION_MS / 1000f, cb);
        smoke(pos, store, cb);
        SpellFx.sound(SFX_BOMB, pos, cb);
        return new Veil(pos, uuid, System.currentTimeMillis());
    }

    /** Nuage epais (rayon ~5, 3 blocs de haut) + panaches noirs au centre et sur un anneau de 3 blocs. */
    private static void smoke(Vector3d at, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        SpellFx.fx(FX_CLOUD, at, 1.0f, store, cb);
        SpellFx.fx(FX_SMOKE, new Vector3d(at).add(0, 0.8, 0), 2.5f, store, cb);
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3;
            SpellFx.fx(FX_SMOKE, new Vector3d(at).add(Math.cos(a) * 3, 0.6, Math.sin(a) * 3), 2.0f, store, cb);
        }
    }

    private static final class Veil implements Instance {
        final Vector3d origin;
        final UUID uuid;
        final long startAt;
        long lastRefresh, lastSmoke;
        Ref<EntityStore> owner;                                           // pour cleanup, qui ne recoit pas le lanceur
        volatile Ref<EntityStore> ambushTarget;                          // premier coup (traitement des degats)
        volatile float ambushAmount;

        Veil(Vector3d origin, UUID uuid, long startAt) {
            this.origin = origin; this.uuid = uuid; this.startAt = startAt; this.lastSmoke = startAt; this.lastRefresh = startAt - REFRESH_MS;
        }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (ambushTarget == null) { ambushAmount = amount; ambushTarget = target; }
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            owner = caster;
            if (now - startAt >= DURATION_MS) return true;
            Ref<EntityStore> target = ambushTarget;
            if (target != null) {                                         // attaque surprise : bonus puis le voile tombe
                Vector3d p = SpellFx.position(target, store);
                float bonus = ambushAmount * BONUS;
                if (p != null && bonus >= 0.5f) {
                    RuneSpellRuntime.markEcho(target, bonus);
                    SpellFx.damage(caster, target, "Physical", bonus, null, "Impact_Strong", cb);
                    SpellFx.fx(FX_AMBUSH, new Vector3d(p).add(0, 1, 0), 1.0f, store, cb);
                }
                return true;
            }
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            if (now - lastRefresh >= REFRESH_MS) {                        // les monstres proches perdent sa trace
                lastRefresh = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RADIUS, store)) {
                    if (NpcAttitudes.force(e, caster, Attitude.IGNORE, (REFRESH_MS + 300) / 1000.0, store)) {
                        CloneFx.mark(e, "LockedTarget", null, store);
                    }
                }
            }
            if (now - lastSmoke >= SMOKE_EVERY_MS) { lastSmoke = now; smoke(origin, store, cb); }
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> caster = owner;
            if (uuid != null && caster != null && caster.isValid()) InvisibilityManager.removeInvisibility(uuid, caster, cb);
        }

    }
}
