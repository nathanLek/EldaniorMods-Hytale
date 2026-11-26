package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

/**
 * Rune du Rayon Celeste (Lumiere, Epic, rune de predilection du Devin Cosmique) : un pilier de lumiere tombe du ciel sur
 * le point vise et glisse vers le curseur (7 b/s, 30 blocs) pendant 6 s. Les ennemis dedans (rayon 2,5) sont brules
 * toutes les 0,25 s (double sur les morts-vivants), les allies soignes de 1 % toutes les 0,5 s.
 * Fiche de conception : scripts/runes/specs/Rune_RayonCeleste.json
 */
public final class RayonCeleste implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, HIT_EVERY_MS = 250, HEAL_EVERY_MS = 500, PILLAR_EVERY_MS = 200, SOUND_EVERY_MS = 2_000;
    private static final double RANGE = 30.0, RADIUS = 2.5, FOLLOW_SPEED = 7.0, HEIGHT = 3.0;
    private static final float DAMAGE = 14f, UNDEAD_MULTIPLIER = 2f, HEAL = 0.01f;
    private static final String[] UNDEAD = {"Skeleton", "Zombie", "Undead", "Ghoul", "Wraith", "Mummy", "Lich", "Ghost"};

    // ===== Assets =====
    private static final String FX_PILLAR = "Bifrost_Pillar", FX_GROUND = "Bifrost_Divine", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_BEAM = "SFX_Portal_Neutral";

    @Override public String id() { return "RayonCeleste"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d at = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        SpellFx.fx(FX_GROUND, new Vector3d(at).add(0, 0.5, 0), 2.0f, store, cb);
        return new Beam(at, System.currentTimeMillis());
    }

    private static boolean isUndead(Ref<EntityStore> e, Store<EntityStore> store) {
        NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
        String type = npc != null ? npc.getNPCTypeId() : null;
        if (type == null) return false;
        for (String k : UNDEAD) if (type.contains(k)) return true;
        return false;
    }

    private static final class Beam implements Instance {
        final Vector3d at;
        final long startAt;
        long lastTick, lastHit, lastHeal, lastPillar, lastSound;

        Beam(Vector3d at, long startAt) { this.at = at; this.startAt = startAt; this.lastTick = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS || SpellFx.position(caster, store) == null) return true;
            double dt = Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;

            Vector3d aim = SpellFx.targetPoint(caster, store, RANGE, RANGE);   // le pilier glisse vers le curseur
            Vector3d delta = new Vector3d(aim).sub(at);
            double step = FOLLOW_SPEED * dt;
            if (delta.length() > step) delta.normalize().mul(step);
            at.add(delta);

            if (now - lastPillar >= PILLAR_EVERY_MS) {
                lastPillar = now;
                SpellFx.fx(FX_PILLAR, at, 0.6f, store, cb);
                SpellFx.fx(FX_GROUND, new Vector3d(at).add(0, 0.3, 0), 1.2f, store, cb);
            }
            if (now - lastSound >= SOUND_EVERY_MS) { lastSound = now; SpellFx.sound(SFX_BEAM, at, cb); }

            Vector3d center = new Vector3d(at).add(0, HEIGHT / 2, 0);
            double reach = RADIUS + HEIGHT;
            if (now - lastHit >= HIT_EVERY_MS) {
                lastHit = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, reach, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || SpellFx.horizontalDistance(p, at) > RADIUS || Math.abs(p.y - at.y) > HEIGHT) continue;
                    SpellFx.damage(caster, e, "Elemental", isUndead(e, store) ? DAMAGE * UNDEAD_MULTIPLIER : DAMAGE, null, null, cb);
                }
            }
            if (now - lastHeal >= HEAL_EVERY_MS) {
                lastHeal = now;
                for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, reach, store)) {
                    Vector3d p = SpellFx.position(a, store);
                    if (p == null || SpellFx.horizontalDistance(p, at) > RADIUS || Math.abs(p.y - at.y) > HEIGHT) continue;
                    SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), HEAL, cb);
                    SpellFx.fx(FX_HEAL, p, 0.5f, store, cb);
                }
            }
            return false;
        }
    }
}
