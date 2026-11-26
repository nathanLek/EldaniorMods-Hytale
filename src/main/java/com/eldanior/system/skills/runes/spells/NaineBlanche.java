package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

/**
 * Rune de la Naine Blanche (Lumiere, Unique, rune de predilection du Createur Cosmique) : etoile de 12 s a 4 blocs
 * au-dessus du point vise (25 blocs) : modele Apollo_Sun reduit + Bifrost_Divine. Ennemis a 10 blocs : attires (2,5 b/s,
 * horizontal) toutes les 0,25 s, 20 degats / 0,5 s (x2 morts-vivants) ; allies a 10 blocs : +1 % PV / 0,5 s.
 * Effondrement final : 250 a 8 blocs.
 * Fiche de conception : scripts/runes/specs/Rune_NaineBlanche.json
 */
public final class NaineBlanche implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 12_000, PULL_EVERY_MS = 250, PULSE_EVERY_MS = 500, GLOW_EVERY_MS = 300;
    private static final double RANGE = 25.0, HEIGHT = 4.0, RADIUS = 10.0, COLLAPSE_RADIUS = 8.0, PULL = 2.5;
    private static final float BURN = 20f, HEAL = 0.01f, COLLAPSE = 250f, UNDEAD_MULTIPLIER = 2f, SUN_SCALE = 1.6f;
    private static final String[] UNDEAD = {"Skeleton", "Zombie", "Undead", "Ghoul", "Wraith", "Mummy", "Lich", "Ghost"};

    // ===== Assets =====
    private static final String SUN = "Projectile_Config_Apollo_Sun", SUN_MODEL = "Apollo_Sun";
    private static final String FX_GLOW = "Bifrost_Divine", FX_FLASH = "Chaos_Flash", FX_PILLAR = "Bifrost_Pillar";
    private static final String SFX_IGNITE = "SFX_Divine_Respawn", SFX_COLLAPSE = "SFX_Portal_Void";

    @Override public String id() { return "NaineBlanche"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Vector3d ground = SpellFx.targetPoint(caster, store, RANGE, RANGE);
        Vector3d star = new Vector3d(ground).add(0, HEIGHT, 0);
        Ref<EntityStore> sun = SpellFx.spawnModel(caster, SUN, star, new Vector3d(1, 0, 0), cb);
        SpellFx.notSaved(sun, store, cb);
        SpellFx.fx(FX_GLOW, star, 3.0f, store, cb);
        SpellFx.sound(SFX_IGNITE, star, cb);
        return new Star(ground, star, sun, System.currentTimeMillis());
    }

    private static final class Star implements Instance {
        final Vector3d ground, star;
        final Ref<EntityStore> sun;
        final long startAt;
        boolean scaled;
        long lastPull, lastPulse, lastGlow;

        Star(Vector3d ground, Vector3d star, Ref<EntityStore> sun, long startAt) { this.ground = ground; this.star = star; this.sun = sun; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (now - startAt >= DURATION_MS) { collapse(caster, store, cb); return true; }
            if (sun != null && sun.isValid()) {
                if (!scaled) { scaled = true; SpellFx.setModelScale(sun, SUN_MODEL, SUN_SCALE, 1.5, cb); }
                SpellFx.hold(sun, star, null, store);
            }
            if (now - lastGlow >= GLOW_EVERY_MS) { lastGlow = now; SpellFx.fx(FX_GLOW, star, 2.5f, store, cb); }
            boolean pulse = now - lastPulse >= PULSE_EVERY_MS;
            if (pulse) lastPulse = now;
            if (now - lastPull >= PULL_EVERY_MS) {
                lastPull = now;
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, ground, RADIUS + HEIGHT, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || RuneSpellRuntime.isSummon(e) || SpellFx.horizontalDistance(p, ground) > RADIUS) continue;
                    Vector3d in = new Vector3d(ground.x - p.x, 0, ground.z - p.z);
                    if (in.lengthSquared() > 1.0) SpellFx.push(e, in.normalize().mul(PULL), true, cb);     // la masse de l'etoile attire
                    if (pulse) SpellFx.damage(caster, e, "Elemental", isUndead(e, store) ? BURN * UNDEAD_MULTIPLIER : BURN, null, null, cb);
                }
            }
            if (pulse) for (Ref<EntityStore> a : SpellFx.alliesAround(caster, ground, RADIUS + HEIGHT, store)) {
                Vector3d p = SpellFx.position(a, store);
                if (p != null && SpellFx.horizontalDistance(p, ground) <= RADIUS) SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), HEAL, cb);
            }
            return false;
        }

        /** L'etoile s'effondre dans un eclair aveuglant. */
        private void collapse(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            SpellFx.remove(sun, cb);
            SpellFx.fx(FX_FLASH, star, 2.0f, store, cb);
            SpellFx.fx(FX_PILLAR, ground, 0.8f, store, cb);
            SpellFx.fx(FX_GLOW, star, 5.0f, store, cb);
            SpellFx.sound(SFX_COLLAPSE, star, cb);
            SpellFx.shake(star, 25, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, star, COLLAPSE_RADIUS + HEIGHT, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || RuneSpellRuntime.isSummon(e) || SpellFx.horizontalDistance(p, ground) > COLLAPSE_RADIUS) continue;
                SpellFx.damage(caster, e, "Elemental", COLLAPSE, null, "Impact_Strong", cb);
            }
        }

        private static boolean isUndead(Ref<EntityStore> e, Store<EntityStore> store) {
            NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
            String type = npc != null ? npc.getNPCTypeId() : null;
            if (type == null) return false;
            for (String k : UNDEAD) if (type.contains(k)) return true;
            return false;
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { SpellFx.remove(sun, cb); }

        @Override public void abort() { RuneSpellRuntime.removeLater(sun); }
    }
}
