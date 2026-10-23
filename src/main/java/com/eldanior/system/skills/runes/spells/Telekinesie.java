package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de Telekinesie (Spatial, Epic) : pendant {@link #DURATION_MS}, une main spectrale obeit au lanceur.
 *
 *  - rien en main : un clic (gauche ou droit) saisit tout ce que contient une sphere invisible de {@link #SPHERE_RADIUS} blocs
 *    de rayon au point vise (creatures, joueurs, blocs) ; la main se referme dessus et tout s'eleve ;
 *  - en main : la prise suit le curseur ; clic gauche = ECRASER (tout est plaque violemment au sol sous la prise),
 *    clic droit = PROJETER (tout part dans la direction visee).
 * Les blocs saisis quittent le monde, volent, puis se reposent la ou ils atterrissent (tout est restaure apres 3 min).
 * Fiche de conception : scripts/runes/specs/Rune_Telekinesie.json
 */
public final class Telekinesie implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 20_000, THROWN_MAX_MS = 5_000, SPAWN_GRACE_MS = 1_000;
    private static final double RANGE = 30.0, SPHERE_RADIUS = 2.5, HOLD_MIN = 4.0, HOLD_MAX = 25.0, LIFT = 1.8, LIFT_TIME = 0.5;
    private static final int BLOCKS_MAX = 60;
    private static final double FOLLOW = 8.0, FOLLOW_MAX = 30.0, BLOCK_FOLLOW = 10.0, BOB = 0.25, TUMBLE = 25.0;   // prise souple
    private static final double CRUSH_SPEED = 45.0, THROW_SPEED = 55.0, THROW_LIFT = 6.0, GRAVITY = 25.0, THROWN_GRAVITY = 16.0, THROW_AIM_RANGE = 90.0;
    private static final float CRUSH_DAMAGE = 120f, THROW_DAMAGE = 80f, BLOCK_DAMAGE = 50f, SPLASH_DAMAGE = 40f;
    // Animation : la main descend sur la prise, prend de l'elan avant d'ecraser, s'arme en reculant avant de projeter
    private static final long GRAB_DESCENT_MS = 300, CRUSH_WINDUP_MS = 260, THROW_WINDUP_MS = 200;
    private static final double GRAB_DESCENT = 3.5, CRUSH_RAISE = 1.8, THROW_PULLBACK = 2.2;
    // Impact de l'ecrasement : cuvette, fissures, eclats, souffle
    private static final double IMPACT_RADIUS = 2.5, SPLASH_RADIUS = 4.5, SPLASH_PUSH = 14.0;
    private static final int IMPACT_CRACKS = 5, EJECTA = 10;
    private static final long RESTORE_AFTER_MS = 180_000, AURA_EVERY_MS = 300, TETHER_EVERY_MS = 150, DUST_EVERY_MS = 350, HAND_GESTURE_MS = 400;
    /** Orientation de la main : false si le modele (doigts vers -Z) pointe bien vers l'avant en jeu, true s'il apparait a l'envers. */
    private static final boolean HAND_REVERSED = false;

    // ===== Assets =====
    private static final String HAND_PROJECTILE = "Projectile_Config_Telekinesie_Main";
    private static final String FX_AURA = "Telekinesie_Aura", FX_DUST = "Gaia_Dust_Burst", FX_CRUMBS = "Block_Break_Dust", FX_SHOCKWAVE = "Chaos_Shockwave", FX_GUST = "Fujin_Gust";
    private static final String DENT_HALF = "Rock_Stone_Cobble_Half", DENT_FULL = "Rock_Stone_Cobble";
    private static final String HELD_EFFECT = "Telekinesie_Saisi";
    private static final String SFX_GRAB = "SFX_Portal_Void", SFX_THROW = "SFX_Ability_Wind_Strike_Slash", SFX_CRUSH = "SFX_Golem_Frost_Slam_Impact";

    @Override public String id() { return "Telekinesie"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        EldaniorLogger.info("[Telekinesie] lancee : 20 s (clic = saisir ; en main : gauche = ecraser, droit = projeter)");
        return new Control(caster, System.currentTimeMillis(), SpellFx.world(store));
    }

    /** Ce qui est tenu : une creature / un joueur, ou un bloc devenu entite. */
    private static final class Held {
        final Ref<EntityStore> ref;
        final String blockId;                                           // null pour une creature
        final Vector3d offset;                                          // par rapport au centre de la prise
        final long born;
        final double phase;
        Vector3d pos;
        Held(Ref<EntityStore> ref, String blockId, Vector3d offset, Vector3d pos, long born, double phase) {
            this.ref = ref; this.blockId = blockId; this.offset = offset; this.pos = pos; this.born = born; this.phase = phase;
        }
    }

    /** Bloc lache, ecrase ou projete : vole avec la gravite, percute les ennemis, se repose la ou il atterrit. */
    private static final class Thrown {
        final Ref<EntityStore> ref;
        final String blockId;
        final long at;
        final boolean place;                                            // false : eclat de l'impact, disparait a l'atterrissage
        Vector3d pos, vel;
        boolean hit;
        Thrown(Ref<EntityStore> ref, String blockId, Vector3d pos, Vector3d vel, long at, boolean place) {
            this.ref = ref; this.blockId = blockId; this.pos = pos; this.vel = vel; this.at = at; this.place = place;
        }
    }

    private static final class Control implements Instance {
        final Ref<EntityStore> caster;
        final long startAt;
        final World world;
        final TerrainRestore.Restore terrain;
        final List<Held> held = new ArrayList<>();
        final List<Thrown> thrown = new ArrayList<>();
        Ref<EntityStore> hand;
        double holdDistance, top;                                       // top : hauteur de la prise au-dessus de son centre
        long grabAt, lastTick, lastAura, lastTether, lastDust, gestureAt, windupAt, impactAt, lastTrail;
        Vector3d lastAnchor, gestureFrom, gestureDir, impactPoint;
        double gestureLength;
        int windup;                                                     // 0 : aucun ; 1 : elan d'ecrasement ; 2 : armer la projection
        final Set<Ref<EntityStore>> crushed = new HashSet<>();
        final java.util.Map<Ref<EntityStore>, Long> flying = new java.util.HashMap<>();   // creatures projetees -> fin de la trainee
        boolean released;

        Control(Ref<EntityStore> caster, long startAt, World world) {
            this.caster = caster; this.startAt = startAt; this.world = world; this.lastTick = startAt;
            this.terrain = TerrainRestore.open(world, startAt + RESTORE_AFTER_MS);
        }

        @Override public boolean listensClicks() { return !released; }

        @Override
        public void onClick(boolean left, Ref<EntityStore> target, Ref<EntityStore> casterRef, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (held.isEmpty()) {
                grab(target, store, cb);
                long creatures = held.stream().filter(h -> h.blockId == null).count();
                EldaniorLogger.info("[Telekinesie] saisie : " + creatures + " creature(s), " + (held.size() - creatures) + " bloc(s)");
            } else if (windup == 0) {                                                          // la main prend son elan, le geste part ensuite
                windup = left ? 1 : 2;
                windupAt = System.currentTimeMillis();
                EldaniorLogger.info("[Telekinesie] " + (left ? "ecrasement" : "projection"));
            }
        }

        // ----- Saisie : tout le contenu de la sphere au curseur -----
        private Vector3d eye(Store<EntityStore> store) {
            Vector3d p = SpellFx.position(caster, store);
            return p == null ? null : p.add(0, 1.6, 0);
        }

        private Vector3d anchor(Store<EntityStore> store, long now) {
            Vector3d eye = eye(store);
            if (eye == null) return lastAnchor;
            double lift = LIFT * Math.min(1.0, (now - grabAt) / 1000.0 / LIFT_TIME);           // la prise s'eleve doucement
            Vector3d look = SpellFx.lookDirection(caster, store);
            Vector3d a = eye.add(new Vector3d(look).mul(holdDistance)).add(0, lift, 0);
            if (windup != 0) {
                double k = ease(Math.min(1.0, (now - windupAt) / (double) (windup == 1 ? CRUSH_WINDUP_MS : THROW_WINDUP_MS)));
                if (windup == 1) a.add(0, CRUSH_RAISE * k, 0);                                  // elan vers le haut
                else a.sub(new Vector3d(look).mul(THROW_PULLBACK * k));                         // armer : la prise recule vers le lanceur
            }
            return a;
        }

        private void grab(Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d eye = eye(store);
            if (eye == null) return;
            long now = System.currentTimeMillis();
            Vector3d center;
            Vector3d tp = target != null && target.isValid() && !target.equals(caster) ? SpellFx.position(target, store) : null;
            if (tp != null) center = tp.add(0, 1, 0);
            else {
                Vector3i b = TargetUtil.getTargetBlock(caster, RANGE, store);
                center = b != null ? new Vector3d(b.x + 0.5, b.y + 0.5, b.z + 0.5) : new Vector3d(eye).add(SpellFx.lookDirection(caster, store).mul(RANGE));
            }
            holdDistance = Math.max(HOLD_MIN, Math.min(HOLD_MAX, center.distance(eye)));
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (Ref<EntityStore> e : TargetUtil.getAllEntitiesInSphere(center, SPHERE_RADIUS + 1, store)) {
                if (e == null || !e.isValid() || e.equals(caster) || !living(e, store)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || new Vector3d(p).add(0, 1, 0).distance(center) > SPHERE_RADIUS + 0.5) continue;
                held.add(new Held(e, null, new Vector3d(p).sub(center), p, now, rng.nextDouble(Math.PI * 2)));
                long left = Math.max(0, DURATION_MS - (now - startAt));
                if (store.getComponent(e, Player.getComponentType()) != null) EffectsManager.applyCustomEffect(e, HELD_EFFECT, left / 1000f, OverlapBehavior.OVERWRITE, cb);
            }
            List<int[]> cells = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            int r = (int) Math.ceil(SPHERE_RADIUS);
            int cx = (int) Math.floor(center.x), cy = (int) Math.floor(center.y), cz = (int) Math.floor(center.z);
            for (int dy = r; dy >= -r && cells.size() < BLOCKS_MAX; dy--) for (int dx = -r; dx <= r && cells.size() < BLOCKS_MAX; dx++) for (int dz = -r; dz <= r && cells.size() < BLOCKS_MAX; dz++) {
                int x = cx + dx, y = cy + dy, z = cz + dz;
                Vector3d at = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
                if (at.distance(center) > SPHERE_RADIUS) continue;
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                if (t == null || TerrainRestore.isEmpty(world, x, y, z)) continue;
                String id = t.getId();
                if (TerrainRestore.isKept(world, id, x, y, z) || TerrainRestore.isProtectedArea(world.getName(), x, y, z) || WorldBlockUtil.getFluidIndex(world, x, y, z) > 0) continue;
                Ref<EntityStore> ref = SpellFx.spawnBlock(id, at, store, cb);
                if (ref == null) continue;
                held.add(new Held(ref, id, new Vector3d(at).sub(center), at, now, rng.nextDouble(Math.PI * 2)));
                cells.add(new int[]{x, y, z});
                ids.add(id);
            }
            if (held.isEmpty()) return;
            removeBlocks(cells, ids);
            top = held.stream().mapToDouble(h -> h.offset.y + (h.blockId == null ? 2.0 : 0.5)).max().orElse(1.0);
            grabAt = now;
            lastAnchor = anchor(store, now);
            hand = SpellFx.spawnModel(caster, HAND_PROJECTILE, handPos(lastAnchor), new Vector3d(1, 0, 0), cb);
            if (!cells.isEmpty()) {                                                             // le sol se dechire sous la prise
                SpellFx.fx(FX_DUST, new Vector3d(center).add(0, -SPHERE_RADIUS + 0.5, 0), 2.0f, store, cb);
                SpellFx.fx(FX_CRUMBS, center, 2.0f, store, cb);
            }
            SpellFx.fx(FX_AURA, center, 2.5f, store, cb);
            SpellFx.sound(SFX_GRAB, center, cb);
        }

        private Vector3d handPos(Vector3d anchor) {
            double descent = GRAB_DESCENT * (1 - ease(Math.min(1.0, (System.currentTimeMillis() - grabAt) / (double) GRAB_DESCENT_MS)));
            return new Vector3d(anchor).add(0, top + 0.6 + descent, 0);                         // la main descend du ciel sur la prise
        }

        private static double ease(double k) { return k * k * (3 - 2 * k); }

        // ----- Ecraser / projeter -----
        private void crush(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d a = lastAnchor;
            if (a == null) return;
            Vector3d ground = SpellFx.ground(world, new Vector3d(a).add(0, -1, 0));
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (Held h : held) if (h.blockId == null) crushed.add(h.ref);
            letGo(new Vector3d(0, -CRUSH_SPEED, 0), CRUSH_DAMAGE, () -> new Vector3d(rng.nextDouble(-1.5, 1.5), -CRUSH_SPEED, rng.nextDouble(-1.5, 1.5)), store, cb);
            double fall = Math.max(0.5, a.y - ground.y);
            gesture(new Vector3d(0, -1, 0), fall + top);
            impactPoint = ground;
            impactAt = System.currentTimeMillis() + Math.max(80, (long) (fall / CRUSH_SPEED * 1000));   // quand la prise touche le sol
            SpellFx.fx(FX_GUST, a, 2.0f, store, cb);
        }

        /** L'ecrasement touche le sol : cuvette en demi-dalles, fissures, eclats de terre, souffle sur les ennemis proches. */
        private void impact(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d g = impactPoint;
            impactPoint = null;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            SpellFx.fx(FX_SHOCKWAVE, new Vector3d(g).add(0, 0.3, 0), 3.5f, store, cb);
            SpellFx.fx(FX_SHOCKWAVE, new Vector3d(g).add(0, 0.5, 0), 2.0f, store, cb);
            SpellFx.fx(FX_DUST, new Vector3d(g).add(0, 0.5, 0), 3.5f, store, cb);
            for (int i = 0; i < 8; i++) {
                double an = Math.PI * i / 4;
                SpellFx.fx(FX_DUST, new Vector3d(g).add(Math.cos(an) * 3, 0.3, Math.sin(an) * 3), 1.6f, store, cb);
            }
            SpellFx.fx(FX_CRUMBS, new Vector3d(g).add(0, 0.5, 0), 3.0f, store, cb);
            SpellFx.sound(SFX_CRUSH, g, cb);
            SpellFx.sound(SFX_GRAB, g, cb);
            SpellFx.shake(g, 35, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g, SPLASH_RADIUS, store)) {   // souffle de l'impact
                if (crushed.contains(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d out = new Vector3d(p.x - g.x, 0, p.z - g.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Bludgeoning", SPLASH_DAMAGE, out.normalize().mul(SPLASH_PUSH).add(0, 6, 0), "Impact_Strong", cb);
            }
            crushed.clear();
            int gx = (int) Math.floor(g.x), gz = (int) Math.floor(g.z), baseY = (int) Math.floor(g.y) - 1;
            String groundId = null;
            for (int y = baseY + 2; y >= baseY - 3 && groundId == null; y--) {
                BlockType t = WorldBlockUtil.getBlockType(world, gx, y, gz);
                if (t != null && !TerrainRestore.isEmpty(world, gx, y, gz) && !t.getId().startsWith("Plant_")) groundId = t.getId();
            }
            if (groundId != null) {                                                             // eclats de terre projetes autour
                long now = System.currentTimeMillis();
                for (int i = 0; i < EJECTA; i++) {
                    double an = rng.nextDouble(Math.PI * 2), sp = rng.nextDouble(7, 13);
                    Vector3d at = new Vector3d(g).add(Math.cos(an) * 0.8, 0.6, Math.sin(an) * 0.8);
                    Ref<EntityStore> ref = SpellFx.spawnBlock(groundId, at, store, cb);
                    if (ref != null) thrown.add(new Thrown(ref, groundId, at, new Vector3d(Math.cos(an) * sp, rng.nextDouble(6, 10), Math.sin(an) * sp), now, false));
                }
            }
            List<double[]> cracks = new ArrayList<>();
            java.util.Random r = new java.util.Random(rng.nextLong());
            Set<Long> seen = new HashSet<>();
            for (int k = 0; k < IMPACT_CRACKS; k++)
                GraviteSol.crackPath(g, Math.PI * 2 * k / IMPACT_CRACKS + r.nextDouble() * 0.8, IMPACT_RADIUS + 0.2, 2 + r.nextDouble() * 3, 1.0, 0.5, 0.1, cracks, seen, r, true);
            world.execute(() -> {                                                               // la cuvette de l'impact
                try {
                    int rr = (int) Math.ceil(IMPACT_RADIUS);
                    for (int dx = -rr; dx <= rr; dx++) for (int dz = -rr; dz <= rr; dz++) {
                        double d = Math.hypot(gx + dx + 0.5 - g.x, gz + dz + 0.5 - g.z);
                        if (d > IMPACT_RADIUS) continue;
                        int x = gx + dx, z = gz + dz;
                        if (TerrainRestore.isProtectedArea(world.getName(), x, baseY, z)) continue;
                        int topY = GraviteSol.surface(world, x, baseY, z);
                        if (topY == Integer.MIN_VALUE || WorldBlockUtil.getFluidIndex(world, x, topY + 1, z) > 0) continue;
                        double depth = Math.max(0.5, Math.round(1.5 * (1 - (d / IMPACT_RADIUS) * (d / IMPACT_RADIUS)) * 2) / 2.0);
                        GraviteSol.dig(world, terrain, x, topY, z, depth, DENT_FULL, DENT_HALF);
                    }
                    for (double[] c : cracks) {
                        int x = (int) c[0], z = (int) c[1];
                        int topY = GraviteSol.surface(world, x, baseY, z);
                        if (topY == Integer.MIN_VALUE || TerrainRestore.isProtectedArea(world.getName(), x, topY, z) || WorldBlockUtil.getFluidIndex(world, x, topY + 1, z) > 0) continue;
                        GraviteSol.dig(world, terrain, x, topY, z, c[2], DENT_FULL, DENT_HALF);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("Telekinesie: impact", e);
                }
            });
        }

        private void project(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d from = lastAnchor != null ? lastAnchor : eye(store);
            if (from == null) return;
            Vector3d aim = SpellFx.targetPoint(caster, store, THROW_AIM_RANGE, THROW_AIM_RANGE);           // vers le point vise
            Vector3d dir = new Vector3d(aim).sub(from);
            if (dir.lengthSquared() < 1) dir = SpellFx.lookDirection(caster, store);
            dir.normalize();
            Vector3d v = new Vector3d(dir).mul(THROW_SPEED).add(0, THROW_LIFT, 0);
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            letGo(v, THROW_DAMAGE, () -> new Vector3d(v).add(rng.nextDouble(-2, 2), rng.nextDouble(-1, 2), rng.nextDouble(-2, 2)), store, cb);
            gesture(dir, 6.0);
            SpellFx.fx(FX_GUST, from, 3.0f, store, cb);
            SpellFx.fx(FX_SHOCKWAVE, from, 1.2f, store, cb);
            SpellFx.sound(SFX_THROW, from, cb);
            SpellFx.sound(SFX_GRAB, from, cb);
        }

        /** Tout ce qui est tenu part avec la vitesse donnee ; les ennemis tenus prennent les degats du geste. */
        private void letGo(Vector3d entityVel, float damage, java.util.function.Supplier<Vector3d> blockVel, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            long now = System.currentTimeMillis();
            for (Held h : held) {
                if (h.blockId == null) {
                    if (!h.ref.isValid()) continue;
                    try { EffectsManager.removeEffect(h.ref, HELD_EFFECT, cb); } catch (Exception ex) { EldaniorLogger.error("Telekinesie: effet", ex); }
                    if (entityVel.length() > 20) flying.put(h.ref, now + 1_200);
                    if (SpellFx.isEnemy(caster, h.ref, store)) {
                        if (damage > 0) SpellFx.damage(caster, h.ref, "Bludgeoning", damage, new Vector3d(entityVel), "Impact_Strong", cb);
                        else SpellFx.push(h.ref, new Vector3d(entityVel), false, cb);
                    } else {
                        SpellFx.push(h.ref, new Vector3d(entityVel).mul(0.5), false, cb);
                        SpellFx.protectFromFall(h.ref, 4_000, store);                           // allies et neutres : pas de degats de chute
                    }
                } else {
                    thrown.add(new Thrown(h.ref, h.blockId, new Vector3d(h.pos), blockVel.get(), now, true));
                }
            }
            held.clear();
        }

        /** La main accompagne le geste (vers le bas ou vers l'avant) puis disparait. */
        private void gesture(Vector3d dir, double length) {
            gestureAt = System.currentTimeMillis();
            gestureFrom = lastAnchor != null ? handPos(lastAnchor) : null;
            gestureDir = new Vector3d(dir).normalize();
            gestureLength = length;
        }

        // ----- Tick -----
        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            double dt = Math.min(0.2, Math.max(0.001, (now - lastTick) / 1000.0));
            lastTick = now;
            if (!released && now - startAt >= DURATION_MS) {                                   // fin : tout est lache doucement
                windup = 0;
                letGo(new Vector3d(0, -1, 0), 0f, Vector3d::new, store, cb);
                released = true;
            }
            if (!held.isEmpty()) {
                Vector3d a = anchor(store, now);
                if (a != null) { lastAnchor = a; carry(a, now, dt, store, cb); }
                if (windup != 0 && now - windupAt >= (windup == 1 ? CRUSH_WINDUP_MS : THROW_WINDUP_MS)) {
                    if (windup == 1) crush(store, cb); else project(store, cb);
                    windup = 0;
                }
            } else windup = 0;
            if (impactPoint != null && now >= impactAt) impact(store, cb);
            if (!flying.isEmpty() && now - lastTrail >= 100) {                                  // trainee des creatures projetees
                flying.entrySet().removeIf(en -> {
                    Vector3d p = SpellFx.position(en.getKey(), store);
                    if (p == null || now >= en.getValue()) return true;
                    SpellFx.fx(FX_AURA, new Vector3d(p).add(0, 1, 0), 0.9f, store, cb);
                    return false;
                });
            }
            moveHand(now, store, cb);
            List<int[]> cells = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            fly(now, dt, store, cb, cells, ids);
            placeBlocks(cells, ids);
            return released && held.isEmpty() && thrown.isEmpty() && hand == null && impactPoint == null && flying.isEmpty();
        }

        /** La main plane au-dessus de la prise, tournee dans la direction du regard ; apres un geste, elle le suit puis s'efface. */
        private void moveHand(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (hand == null) return;
            Vector3d look = SpellFx.lookDirection(caster, store);
            Vector3d facingDir = new Vector3d(look.x, 0, look.z);
            if (HAND_REVERSED) facingDir.negate();
            Rotation3f rot = SpellFx.facing(facingDir, false);
            if (gestureFrom != null) {
                double k = (now - gestureAt) / (double) HAND_GESTURE_MS;
                if (k >= 1) { SpellFx.remove(hand, cb); hand = null; gestureFrom = null; return; }
                SpellFx.hold(hand, new Vector3d(gestureFrom).add(new Vector3d(gestureDir).mul(gestureLength * ease(k))), rot, store);
            } else if (!held.isEmpty() && lastAnchor != null) {
                SpellFx.hold(hand, handPos(lastAnchor), rot, store);
            } else if (held.isEmpty()) {
                SpellFx.remove(hand, cb);
                hand = null;
            }
        }

        /** La prise suit le point de tenue, avec du jeu : balancement, blocs qui tournoient, poussiere qui s'en echappe. */
        private void carry(Vector3d anchor, long now, double dt, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean aura = now - lastAura >= AURA_EVERY_MS, dust = now - lastDust >= DUST_EVERY_MS;
            if (aura) lastAura = now;
            if (dust) lastDust = now;
            double t = (now - grabAt) / 1000.0;
            int n = 0;
            for (var it = held.iterator(); it.hasNext(); ) {
                Held h = it.next();
                Vector3d target = new Vector3d(anchor).add(h.offset).add(0, Math.sin(t * 2.2 + h.phase) * BOB, 0);
                if (h.blockId == null) {
                    Vector3d p = SpellFx.position(h.ref, store);
                    if (p == null) { it.remove(); continue; }                                  // creature morte ou retiree
                    Vector3d vel = new Vector3d(target).sub(p).mul(FOLLOW);
                    if (vel.length() > FOLLOW_MAX) vel.normalize().mul(FOLLOW_MAX);
                    SpellFx.push(h.ref, vel, false, cb);
                    h.pos = p;
                    if (aura) SpellFx.fx(FX_AURA, new Vector3d(p).add(0, 1, 0), 1.2f, store, cb);
                } else {
                    if (!h.ref.isValid()) { if (now - h.born >= SPAWN_GRACE_MS) it.remove(); continue; }
                    h.pos = new Vector3d(h.pos).add(new Vector3d(target).sub(h.pos).mul(Math.min(1.0, BLOCK_FOLLOW * dt)));
                    Rotation3f spin = new Rotation3f();
                    spin.setYaw((float) Math.toRadians((t * TUMBLE + h.phase * 57) % 360));
                    spin.setPitch((float) Math.toRadians(Math.sin(t + h.phase) * 15));
                    SpellFx.hold(h.ref, h.pos, spin, store);
                    if (aura && n % 5 == 0) SpellFx.fx(FX_AURA, h.pos, 0.8f, store, cb);
                    if (dust && n % 7 == 0) SpellFx.fx(FX_CRUMBS, h.pos, 0.6f, store, cb);   // terre qui s'effrite
                    n++;
                }
            }
            if (now - lastTether >= TETHER_EVERY_MS) {                                         // fil d'energie du lanceur a la prise
                lastTether = now;
                Vector3d from = eye(store);
                if (from != null) {
                    from.add(0, -0.4, 0);
                    Vector3d to = handPos(anchor);
                    for (int i = 1; i <= 5; i++) {
                        double k = i / 6.0;
                        Vector3d p = new Vector3d(from).lerp(to, k).add(0, Math.sin(k * Math.PI) * 0.6, 0);
                        SpellFx.fx(FX_AURA, p, 0.35f, store, cb);
                    }
                }
            }
        }

        /** Blocs lances : gravite, percussion des ennemis, atterrissage (le bloc se repose dans le monde). */
        private void fly(long now, double dt, Store<EntityStore> store, CommandBuffer<EntityStore> cb, List<int[]> cells, List<String> ids) {
            boolean trail = now - lastTrail >= 100;
            if (trail) lastTrail = now;
            int[] trailed = {0};
            thrown.removeIf(t -> {
                if (!t.ref.isValid() && now - t.at >= SPAWN_GRACE_MS) return true;
                t.vel.y -= (t.place ? THROWN_GRAVITY : GRAVITY) * dt;
                if (trail && t.place && t.vel.length() > 15 && trailed[0]++ < 6) SpellFx.fx(FX_AURA, t.pos, 0.5f, store, cb);   // trainee violette
                Vector3d next = new Vector3d(t.pos).add(new Vector3d(t.vel).mul(dt));
                if (!t.hit && t.vel.length() > 6) {
                    for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, next, 1.3, store)) {
                        SpellFx.damage(caster, e, "Bludgeoning", BLOCK_DAMAGE, new Vector3d(t.vel).mul(0.5).add(0, 3, 0), "Impact_Light", cb);
                        t.hit = true;
                    }
                    if (t.hit) t.vel.mul(0.3);
                }
                int nx = (int) Math.floor(next.x), ny = (int) Math.floor(next.y), nz = (int) Math.floor(next.z);
                BlockType at = WorldBlockUtil.getBlockType(world, nx, ny, nz);
                boolean solid = at == null || !TerrainRestore.isEmpty(world, nx, ny, nz);
                if (solid || now - t.at >= THROWN_MAX_MS) {
                    int x = (int) Math.floor(t.pos.x), y = (int) Math.floor(t.pos.y), z = (int) Math.floor(t.pos.z);
                    if (t.place && TerrainRestore.isEmpty(world, x, y, z) && !TerrainRestore.isProtectedArea(world.getName(), x, y, z)) {
                        cells.add(new int[]{x, y, z});
                        ids.add(t.blockId);
                    }
                    if (t.vel.length() > 10) {
                        SpellFx.fx(FX_DUST, t.pos, 0.9f, store, cb);
                        SpellFx.fx(FX_CRUMBS, t.pos, 1.0f, store, cb);
                    }
                    SpellFx.remove(t.ref, cb);
                    return true;
                }
                t.pos = next;
                SpellFx.hold(t.ref, t.pos, null, store);
                return false;
            });
        }

        private boolean living(Ref<EntityStore> e, Store<EntityStore> store) {
            return store.getComponent(e, Player.getComponentType()) != null || store.getComponent(e, NPCEntity.getComponentType()) != null;
        }

        private void removeBlocks(List<int[]> cells, List<String> ids) {
            if (cells.isEmpty()) return;
            world.execute(() -> {
                try {
                    for (int i = 0; i < cells.size(); i++) {
                        int[] c = cells.get(i);
                        BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                        if (t != null && ids.get(i).equals(t.getId())) TerrainRestore.place(world, terrain, c[0], c[1], c[2], ids.get(i), TerrainRestore.EMPTY, 0);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("Telekinesie: saisie de blocs", e);
                }
            });
        }

        private void placeBlocks(List<int[]> cells, List<String> ids) {
            if (cells.isEmpty()) return;
            world.execute(() -> {
                try {
                    Set<Long> used = new HashSet<>();
                    for (int i = 0; i < cells.size(); i++) {
                        int[] c = cells.get(i);
                        long k = (((long) c[0] & 0x1FFFFF) << 42) | (((long) c[1] & 0x1FFFFF) << 21) | ((long) c[2] & 0x1FFFFF);
                        if (used.add(k) && TerrainRestore.isEmpty(world, c[0], c[1], c[2]))
                            TerrainRestore.place(world, terrain, c[0], c[1], c[2], TerrainRestore.EMPTY, ids.get(i), 0);
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("Telekinesie: atterrissage", e);
                }
            });
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(hand, cb);
            for (Held h : held) {
                if (h.blockId != null) SpellFx.remove(h.ref, cb);
                else if (h.ref.isValid()) try { EffectsManager.removeEffect(h.ref, HELD_EFFECT, cb); } catch (Exception ex) { EldaniorLogger.error("Telekinesie: effet", ex); }
            }
            for (Thrown t : thrown) SpellFx.remove(t.ref, cb);
            held.clear();
            thrown.clear();
            TerrainRestore.save();
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(hand);
            for (Held h : held) if (h.blockId != null) RuneSpellRuntime.removeLater(h.ref);
            for (Thrown t : thrown) RuneSpellRuntime.removeLater(t.ref);
            TerrainRestore.save();
        }
    }
}
