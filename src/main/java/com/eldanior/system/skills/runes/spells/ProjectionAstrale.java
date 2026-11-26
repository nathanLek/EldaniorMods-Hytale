package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune de Projection Astrale (Espace, Rare, rune de predilection du Mystique) : l'esprit du lanceur quitte son corps et
 * file dans la direction du regard (10 b/s, 30 blocs du depart au plus, arrete par les murs) pendant 6 s. Un clic, ou la
 * fin du sort, teleporte le lanceur sur l'esprit (toujours dans une case libre) ; une onde astrale blesse et repousse a
 * l'arrivee. Protection de chute.
 * Fiche de conception : scripts/runes/specs/Rune_ProjectionAstrale.json
 */
public final class ProjectionAstrale implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 6_000, FX_EVERY_MS = 100, FALL_GUARD_MS = 3_000;
    private static final double SPEED = 10.0, MAX_DISTANCE = 30.0, EYE = 1.5, WAVE_RADIUS = 3.0, PUSH = 9.0, PUSH_LIFT = 4.0;
    private static final float DAMAGE = 120f;

    // ===== Assets =====
    private static final String FX_SPIRIT = "Astral_Esprit", FX_FLASH = "Bifrost_Divine";
    private static final String SFX_DEPART = "SFX_Portal_Neutral_Open", SFX_ARRIVE = "SFX_Portal_Neutral", SFX_WAVE = "SFX_Portal_Void";

    @Override public String id() { return "ProjectionAstrale"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        Vector3d spirit = new Vector3d(pos).add(0, EYE, 0);
        SpellFx.fx(FX_FLASH, spirit, 0.8f, store, cb);
        SpellFx.sound(SFX_DEPART, pos, cb);
        return new Projection(pos, spirit, world, System.currentTimeMillis());
    }

    /** Case ou un corps tient : pieds et tete hors d'un bloc solide (null = chunk non charge : refuse). */
    private static boolean free(World world, Vector3d feet) {
        int x = (int) Math.floor(feet.x), y = (int) Math.floor(feet.y), z = (int) Math.floor(feet.z);
        return passable(world, x, y, z) && passable(world, x, y + 1, z);
    }

    private static boolean passable(World world, int x, int y, int z) {
        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
        return t != null && t.getMaterial() != BlockMaterial.Solid;
    }

    private static final class Projection implements Instance {
        final Vector3d origin, spirit;
        final World world;
        final long startAt;
        Vector3d lastFree;                                               // derniere position libre des pieds de l'esprit
        long lastTick, lastFx;
        volatile boolean recall;

        Projection(Vector3d origin, Vector3d spirit, World world, long startAt) {
            this.origin = origin; this.spirit = spirit; this.world = world; this.startAt = startAt; this.lastTick = startAt;
            this.lastFree = new Vector3d(origin);
        }

        @Override public boolean listensClicks() { return true; }

        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            recall = true;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            double dt = Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;
            if (recall || now - startAt >= DURATION_MS) { arrive(caster, store, cb); return true; }

            Vector3d next = new Vector3d(spirit).add(SpellFx.lookDirection(caster, store).mul(SPEED * dt));
            if (next.distance(origin) > MAX_DISTANCE)                      // laisse : l'esprit reste sur la sphere de 30 blocs
                next = new Vector3d(origin).add(next.sub(origin).normalize().mul(MAX_DISTANCE));
            Vector3d feet = new Vector3d(next).sub(0, EYE, 0);
            int x = (int) Math.floor(next.x), y = (int) Math.floor(next.y), z = (int) Math.floor(next.z);
            if (passable(world, x, y, z)) {                                 // un mur arrete l'esprit
                spirit.set(next);
                if (free(world, feet)) lastFree = feet;
            }
            if (now - lastFx >= FX_EVERY_MS) {
                lastFx = now;
                SpellFx.fx(FX_SPIRIT, spirit, 1.0f, store, cb);
            }
            return false;
        }

        /** Teleportation sur l'esprit (case libre la plus proche), puis onde astrale. */
        private void arrive(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d feet = new Vector3d(spirit).sub(0, EYE, 0);
            Vector3d dest = free(world, feet) ? feet : lastFree;
            Vector3d from = SpellFx.position(caster, store);
            if (from != null) SpellFx.fx(FX_FLASH, new Vector3d(from).add(0, 1, 0), 0.8f, store, cb);
            SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
            SpellFx.teleport(caster, dest, store, cb);
            Vector3d center = new Vector3d(dest).add(0, 1, 0);
            SpellFx.fx(FX_FLASH, center, 1.6f, store, cb);
            SpellFx.fx(FX_SPIRIT, center, 2.5f, store, cb);
            SpellFx.sound(SFX_ARRIVE, dest, cb);
            SpellFx.sound(SFX_WAVE, dest, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, WAVE_RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d away = new Vector3d(p).sub(dest).setComponent(1, 0);
                if (away.lengthSquared() < 1e-6) away.set(1, 0, 0);
                SpellFx.damage(caster, e, "Elemental", DAMAGE, away.normalize().mul(PUSH).add(0, PUSH_LIFT, 0), "Impact_Light", cb);
            }
        }
    }
}
