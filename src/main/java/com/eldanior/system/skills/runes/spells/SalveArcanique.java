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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rune de la Salve Arcanique (Lumiere, Rare, rune de predilection de l'Archimage) : 5 orbes de lumiere (particules
 * deplacees par le code) jaillissent en eventail puis se dirigent chacun vers un ennemi devant le lanceur (25 blocs),
 * repartis entre les cibles : 45 degats par orbe. Un orbe sans cible vole droit et se dissipe apres 3 s ou contre un mur.
 * Fiche de conception : scripts/runes/specs/Rune_SalveArcanique.json
 */
public final class SalveArcanique implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long LIFE_MS = 3_000, LAUNCH_GAP_MS = 80, FX_EVERY_MS = 50;
    private static final int ORBS = 5;
    private static final double RANGE = 25.0, SPEED = 18.0, TURN = 6.0, HIT_RADIUS = 1.1, SPREAD_DEG = 22.0;
    private static final float DAMAGE = 45f;

    // ===== Assets =====
    private static final String FX_ORB = "Bifrost_Divine", FX_HIT = "Apollo_Heal_Pulse";
    private static final String SFX_HIT = "SFX_Portal_Void";

    @Override public String id() { return "SalveArcanique"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Vector3d look = SpellFx.lookDirection(caster, store);
        Vector3d fwd = SpellFx.forward(caster, store);
        List<Ref<EntityStore>> targets = new ArrayList<>();                 // ennemis devant, les plus proches du regard d'abord
        Vector3d eye = new Vector3d(pos).add(0, 1.5, 0);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(pos).add(new Vector3d(fwd).mul(RANGE / 2)), RANGE / 2 + 1, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || RuneSpellRuntime.isSummon(e) || p.distance(pos) > RANGE) continue;
            if (new Vector3d(p).sub(pos).setComponent(1, 0).normalize().dot(fwd) < 0.3) continue;
            targets.add(e);
        }
        targets.sort(Comparator.comparingDouble(e -> {
            Vector3d to = SpellFx.position(e, store).add(0, 1, 0).sub(eye);
            return to.length() * (1.5 - to.normalize().dot(look));
        }));
        long now = System.currentTimeMillis();
        List<Orb> orbs = new ArrayList<>();
        for (int i = 0; i < ORBS; i++) {
            double yaw = Math.toRadians((i - (ORBS - 1) / 2.0) * SPREAD_DEG);
            Vector3d dir = new Vector3d(look.x * Math.cos(yaw) - look.z * Math.sin(yaw), look.y + 0.35, look.x * Math.sin(yaw) + look.z * Math.cos(yaw)).normalize();
            orbs.add(new Orb(new Vector3d(eye).add(new Vector3d(dir).mul(0.8)), dir, targets.isEmpty() ? null : targets.get(i % targets.size()), now + i * LAUNCH_GAP_MS));
        }
        return new Salvo(orbs, targets, SpellFx.world(store), now);
    }

    private static final class Orb {
        final Vector3d pos, dir;
        final long launchAt;
        Ref<EntityStore> target;
        boolean done;
        Orb(Vector3d pos, Vector3d dir, Ref<EntityStore> target, long launchAt) { this.pos = pos; this.dir = dir; this.target = target; this.launchAt = launchAt; }
    }

    private static final class Salvo implements Instance {
        final List<Orb> orbs;
        final List<Ref<EntityStore>> targets;
        final World world;
        final long startAt;
        long lastTick, lastFx;

        Salvo(List<Orb> orbs, List<Ref<EntityStore>> targets, World world, long startAt) {
            this.orbs = orbs; this.targets = targets; this.world = world; this.startAt = startAt; this.lastTick = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            double dt = Math.min(0.1, (now - lastTick) / 1000.0);
            lastTick = now;
            boolean fx = now - lastFx >= FX_EVERY_MS;
            if (fx) lastFx = now;
            boolean alive = false;
            for (Orb o : orbs) {
                if (o.done) continue;
                alive = true;
                if (now < o.launchAt) continue;
                if (now - o.launchAt >= LIFE_MS) { o.done = true; continue; }
                Vector3d tp = o.target != null ? SpellFx.position(o.target, store) : null;
                if (tp == null && o.target != null) { o.target = retarget(store); tp = o.target != null ? SpellFx.position(o.target, store) : null; }
                if (tp != null) {                                               // tete chercheuse : le cap tourne vers la cible
                    Vector3d want = new Vector3d(tp).add(0, 1, 0).sub(o.pos).normalize();
                    o.dir.add(want.sub(o.dir).mul(Math.min(1.0, TURN * dt))).normalize();
                }
                Vector3d next = new Vector3d(o.pos).add(new Vector3d(o.dir).mul(SPEED * dt));
                if (!passable(next)) { o.done = true; SpellFx.fx(FX_ORB, o.pos, 0.6f, store, cb); continue; }
                o.pos.set(next);
                if (fx) SpellFx.fx(FX_ORB, o.pos, 0.35f, store, cb);
                for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, o.pos, HIT_RADIUS + 1.5, store)) {
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null || new Vector3d(p).add(0, 1, 0).distance(o.pos) > HIT_RADIUS + 0.4) continue;
                    SpellFx.damage(caster, e, "Elemental", DAMAGE, new Vector3d(o.dir).mul(3), "Impact_Light", cb);
                    SpellFx.fx(FX_ORB, o.pos, 1.0f, store, cb);
                    SpellFx.fx(FX_HIT, p, 0.6f, store, cb);
                    SpellFx.sound(SFX_HIT, p, cb);
                    o.done = true;
                    break;
                }
            }
            return !alive;
        }

        private Ref<EntityStore> retarget(Store<EntityStore> store) {
            for (Ref<EntityStore> e : targets) if (SpellFx.position(e, store) != null) return e;
            return null;
        }

        private boolean passable(Vector3d p) {
            if (world == null) return false;
            BlockType t = WorldBlockUtil.getBlockType(world, (int) Math.floor(p.x), (int) Math.floor(p.y), (int) Math.floor(p.z));
            return t != null && t.getMaterial() != BlockMaterial.Solid;
        }
    }
}
