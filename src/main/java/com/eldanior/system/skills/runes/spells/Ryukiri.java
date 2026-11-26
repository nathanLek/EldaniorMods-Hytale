package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Ryukiri, Tranche-Dragon des Runkandel (artefact divin de famille, Artefact_Sword_Ryukiri ; modele, item, interactions et
 * lame d'energie generes par scripts/artefacts/gen_ryukiri.py). Son passif (+100 % contre les dragons, feu subi reduit) est
 * dans ArtefactWeapons et la reservation a la famille dans MasterySystem ; ce fichier porte sa signature, Tranche-Montagne,
 * lancee par l'interaction Ryukiri_Signature en deux temps :
 *   - TrancheMontagneEveil (debut de la charge) : des flammes convergent en spirale vers le porteur, le sol brule et gronde ;
 *   - TrancheMontagne (au coup) : une lame d'energie verticale geante (modele Ryukiri_Lame, ~12 blocs de haut, deplacee par
 *     le code) file droit devant a l'horizontale sur 60 blocs. Rien ne l'arrete : elle tranche le terrain (saignee de 3-4
 *     blocs de large sur 17 de haut, au fond une cicatrice de roche en fusion), projette des eclats de roche, frappe une
 *     fois chaque ennemi traverse (le passif s'applique : double contre les dragons) ; puis la saignee explose en chaine,
 *     du porteur vers le bout de la ligne, jusqu'a une explosion finale. Terrain restaure apres RESTORE_MS, jamais en zone
 *     protegee.
 */
public final class Ryukiri {

    private Ryukiri() {}

    private static final String FX_FIRE = "Impact_Fire", FX_BURST = "Gilgamesh_Sword_Burst", FX_PILLAR = "Muspelheim_Fire_Pillar";
    private static final String FX_EXPLOSION = "Explosion_Big", FX_GROUND = "Muspelheim_Burning_Ground";
    private static final String SFX_RUMBLE = "SFX_Golem_Earth_Swing_Impact", SFX_THUNDER = "SFX_Global_Weather_Thunder";

    // ===================== Charge =====================

    public static final class TrancheMontagneEveil implements RuneSpell {
        @Override public String id() { return "TrancheMontagneEveil"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d feet = SpellFx.position(caster, store);
            if (feet == null) return null;
            SpellFx.fx(FX_GROUND, feet, 1.2f, store, cb);
            SpellFx.sound(SFX_RUMBLE, feet, cb);
            return new Charge(caster);
        }
    }

    /** Flammes en spirale qui se resserrent sur le porteur pendant la charge (CHARGE_MS, calee sur l'interaction). */
    private static final class Charge implements RuneSpell.Instance {
        private static final long CHARGE_MS = 900, EVERY_MS = 90;
        private static final int ARMS = 6;
        private static final double START_RADIUS = 4.5;

        final Ref<EntityStore> caster;
        final long startAt;
        long next;
        boolean half;

        Charge(Ref<EntityStore> caster) { this.caster = caster; this.startAt = System.currentTimeMillis(); }

        @Override
        public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            long t = now - startAt;
            if (t >= CHARGE_MS || !caster.isValid()) return true;
            if (now < next) return false;
            next = now + EVERY_MS;
            Vector3d feet = SpellFx.position(caster, store);
            if (feet == null) return true;
            double k = t / (double) CHARGE_MS;
            double r = START_RADIUS * (1 - k) + 0.6, turn = k * Math.PI * 3;
            for (int i = 0; i < ARMS; i++) {
                double a = turn + i * 2 * Math.PI / ARMS;
                Vector3d p = new Vector3d(feet).add(Math.cos(a) * r, 0.3 + 1.6 * k, Math.sin(a) * r);
                SpellFx.fx(FX_FIRE, p, 0.45f + 0.35f * (float) k, store, cb);
            }
            if (!half && k >= 0.5) {
                half = true;
                SpellFx.sound(SFX_RUMBLE, feet, cb);
                SpellFx.shake(feet, 15, "Impact_Light", store);
                SpellFx.fx(FX_GROUND, feet, 1.6f, store, cb);
            }
            return false;
        }
    }

    // ===================== Tranche-Montagne =====================

    public static final class TrancheMontagne implements RuneSpell {
        @Override public String id() { return "TrancheMontagne"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            return Slash.launch(caster, store, cb);
        }
    }

    private static final class Slash implements RuneSpell.Instance {

        // ===== Reglages (equilibrage) =====
        private static final double RANGE = 60.0, SPEED = 55.0, STEP = 0.5;
        private static final double SAFE_START = 2.5;                       // pas de saignee sous les pieds du porteur
        private static final double HIT_HALF_WIDTH = 3.0;
        private static final int CUT_BELOW = 2, CUT_ABOVE = 14;              // saignee : des pieds -2 a +14 (17 blocs)
        private static final double[] CUT_LATERAL = {-1.2, -0.6, 0.0, 0.6, 1.2};   // 3 a 4 blocs de large
        private static final float DAMAGE = 320f, ERUPTION_DAMAGE = 90f, FINAL_DAMAGE = 180f;
        private static final double PUSH = 6.0, LIFT = 4.0, ERUPTION_RADIUS = 3.5, FINAL_RADIUS = 6.0;
        private static final float BLADE_SCALE = 7.0f;                       // ~12 blocs de haut
        private static final double BLADE_HEIGHT = 6.0;                      // centre de la lame au-dessus des pieds
        private static final double ERUPTION_EVERY = 4.0;                    // une explosion tous les 4 blocs de saignee
        private static final long ERUPTION_DELAY_MS = 450, ERUPTION_GAP_MS = 70;
        private static final double DEBRIS_EVERY = 2.0, GRAVITY = 22.0;
        private static final int DEBRIS_MAX = 40;
        private static final long DEBRIS_LIFE_MS = 1_800, RESTORE_MS = 120_000, MAX_MS = 8_000;
        private static final String SCAR = "Rock_Volcanic_Cracked_Lava";    // cicatrice ardente au fond de la saignee
        private static final String[] NEVER_CUT = {"Bedrock", "Barrier"};

        // ===== Assets =====
        private static final String CARRIER = "Projectile_Config_Ryukiri_Lame", MODEL = "Ryukiri_Lame";
        private static final String SFX_SLASH = "SFX_Ability_Wind_Strike_Slash", SFX_HIT = "SFX_Sword_T2_Impact";
        private static final String SFX_BLAST = "SFX_Fireball_Death";
        private static final String BURN = "Elda_Brulure";

        private record Eruption(Vector3d at, long dueAt) {}
        private static final class Debris {
            final Ref<EntityStore> ref; final Vector3d pos, vel; final long born; final float spin;
            Debris(Ref<EntityStore> ref, Vector3d pos, Vector3d vel, long born, float spin) {
                this.ref = ref; this.pos = pos; this.vel = vel; this.born = born; this.spin = spin;
            }
        }

        final Ref<EntityStore> caster;
        Ref<EntityStore> model;
        final Vector3d pos, dir, right;
        final int baseY;
        final World world;
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>(), burned = new HashSet<>();
        final Set<Long> cut = new HashSet<>();
        final List<Eruption> eruptions = new ArrayList<>();
        final List<Debris> debris = new ArrayList<>();
        TerrainRestore.Restore terrain;
        long last, flightEnd, nextEruptionAt;
        double travelled, nextFx, nextRumble, nextEruption = SAFE_START, nextDebris = SAFE_START;
        int debrisCount;
        boolean scaled, flying = true, finale;

        private Slash(Ref<EntityStore> caster, Ref<EntityStore> model, Vector3d pos, Vector3d dir, int baseY, World world) {
            this.caster = caster; this.model = model; this.pos = pos; this.dir = dir; this.baseY = baseY; this.world = world;
            this.right = new Vector3d(-dir.z, 0, dir.x);
            this.startAt = this.last = System.currentTimeMillis();
        }

        static Slash launch(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d feet = SpellFx.position(caster, store);
            if (feet == null) return null;
            Vector3d dir = SpellFx.lookDirection(caster, store);
            dir.y = 0;                                                       // ligne droite a l'horizontale
            if (dir.lengthSquared() < 1e-4) dir = SpellFx.forward(caster, store);
            dir.y = 0;
            if (dir.lengthSquared() < 1e-4) return null;
            dir.normalize();
            Vector3d from = new Vector3d(feet).add(0, BLADE_HEIGHT, 0).add(new Vector3d(dir).mul(1.5));
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, from, dir, cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            Vector3d front = new Vector3d(feet).add(0, 1.5, 0).add(new Vector3d(dir).mul(2));
            SpellFx.fx(FX_BURST, front, 2.6f, store, cb);
            SpellFx.fx(FX_EXPLOSION, front, 1.0f, store, cb);
            SpellFx.sound(SFX_SLASH, front, cb);
            SpellFx.sound(SFX_RUMBLE, front, cb);
            SpellFx.sound2dNear(SFX_THUNDER, feet, 80, store);              // la montagne se fend
            SpellFx.shake(feet, 40, "Impact_Strong", store);
            return new Slash(caster, model, from, dir, (int) Math.floor(feet.y), SpellFx.world(store));
        }

        @Override
        public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!caster.isValid() || now - startAt > MAX_MS) return true;
            double dt = Math.min(0.2, (now - last) / 1000.0);
            last = now;
            if (flying) fly(now, dt, store, cb);
            erupt(now, store, cb);
            moveDebris(now, dt, store, cb);
            return !flying && eruptions.isEmpty() && finale && debris.isEmpty();
        }

        // ----- La lame -----

        private void fly(long now, double dt, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!scaled && model != null && model.isValid()) {             // le porteur n'existe qu'au tick suivant sa creation
                SpellFx.setModelScale(model, MODEL, BLADE_SCALE, 1.2 * BLADE_SCALE, cb);
                scaled = true;
            }
            double move = Math.min(RANGE - travelled, SPEED * dt);
            List<int[]> cells = new ArrayList<>(), scar = new ArrayList<>();
            for (double done = 0; done < move; done += STEP) {
                double step = Math.min(STEP, move - done);
                pos.add(new Vector3d(dir).mul(step));
                travelled += step;
                if (travelled >= SAFE_START) collectCut(cells, scar);
                strike(store, cb);
                Vector3d floor = new Vector3d(pos.x, baseY - CUT_BELOW + 0.5, pos.z);
                if (travelled >= nextFx) {                                   // gerbes de feu la ou la lame entre dans le sol
                    nextFx = travelled + 2.0;
                    SpellFx.fx(FX_FIRE, floor, 1.4f, store, cb);
                    SpellFx.fx(FX_BURST, new Vector3d(pos), 0.8f, store, cb);
                }
                if (travelled >= nextRumble) {
                    nextRumble = travelled + 8.0;
                    SpellFx.sound(SFX_RUMBLE, pos, cb);
                    SpellFx.shake(pos, 18, "Impact_Light", store);
                }
                if (travelled >= nextEruption) {                             // la saignee explosera ici, un peu apres
                    nextEruption = travelled + ERUPTION_EVERY;
                    long due = Math.max(now + ERUPTION_DELAY_MS, nextEruptionAt + ERUPTION_GAP_MS);
                    nextEruptionAt = due;
                    eruptions.add(new Eruption(floor, due));
                }
                if (travelled >= nextDebris && debrisCount < DEBRIS_MAX) {
                    nextDebris = travelled + DEBRIS_EVERY;
                    throwDebris(now, store, cb);
                }
            }
            carve(cells, scar);
            if (model != null && model.isValid()) SpellFx.hold(model, pos, SpellFx.facing(dir, false), store);
            if (travelled >= RANGE) {                                        // bout de la ligne : la lame se brise
                flying = false;
                flightEnd = now;
                SpellFx.remove(model, cb);
                model = null;
                SpellFx.fx(FX_BURST, new Vector3d(pos), 2.0f, store, cb);
            }
        }

        /** Cases de la saignee au droit de la lame (une seule fois chacune) ; le fond de l'axe devient une cicatrice ardente. */
        private void collectCut(List<int[]> cells, List<int[]> scar) {
            for (double k : CUT_LATERAL) {
                int bx = (int) Math.floor(pos.x + right.x * k), bz = (int) Math.floor(pos.z + right.z * k);
                for (int by = baseY - CUT_BELOW - (k == 0.0 ? 1 : 0); by <= baseY + CUT_ABOVE; by++) {
                    long key = (((long) bx & 0x3FFFFFFL) << 38) | (((long) by & 0xFFFL) << 26) | ((long) bz & 0x3FFFFFFL);
                    if (!cut.add(key)) continue;
                    (by < baseY - CUT_BELOW ? scar : cells).add(new int[]{bx, by, bz});
                }
            }
        }

        /** La saignee : les blocs tranches disparaissent, le fond rougeoie (restaures apres RESTORE_MS). Hors du tick ECS. */
        private void carve(List<int[]> cells, List<int[]> scar) {
            if ((cells.isEmpty() && scar.isEmpty()) || world == null) return;
            if (terrain == null) terrain = TerrainRestore.open(world, System.currentTimeMillis() + RESTORE_MS);
            TerrainRestore.Restore batch = terrain;
            World w = world;
            w.execute(() -> {
                for (int[] c : cells) replace(w, batch, c, TerrainRestore.EMPTY);
                for (int[] c : scar) replace(w, batch, c, SCAR);
            });
        }

        private static void replace(World w, TerrainRestore.Restore batch, int[] c, String placed) {
            try {
                BlockType type = WorldBlockUtil.getBlockType(w, c[0], c[1], c[2]);
                if (type == null || TerrainRestore.isEmpty(w, c[0], c[1], c[2])) return;
                String id = type.getId();
                if (TerrainRestore.contains(id, NEVER_CUT) || TerrainRestore.isProtectedArea(w.getName(), c[0], c[1], c[2])
                        || TerrainRestore.isKept(w, id, c[0], c[1], c[2]) || WorldBlockUtil.getFluidIndex(w, c[0], c[1], c[2]) > 0) return;
                TerrainRestore.place(w, batch, c[0], c[1], c[2], id, placed, 0);
            } catch (Exception ignored) { }
        }

        /** Chaque ennemi traverse par la lame est frappe une fois. */
        private void strike(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, HIT_HALF_WIDTH + CUT_ABOVE, store)) {
                if (hit.contains(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || p.y < baseY - CUT_BELOW - 1 || p.y > baseY + CUT_ABOVE) continue;
                if (SpellFx.horizontalDistance(p, pos) > HIT_HALF_WIDTH) continue;
                hit.add(e);
                Vector3d side = new Vector3d(p.x - pos.x, 0, p.z - pos.z);       // projete hors de la saignee, vers l'avant
                side = side.lengthSquared() > 0.04 ? side.normalize().mul(PUSH * 0.6) : new Vector3d();
                Vector3d push = new Vector3d(dir).mul(PUSH).add(side).add(0, LIFT, 0);
                SpellFx.damage(caster, e, "Physical", DAMAGE, push, "Impact_Strong", cb);
                SpellFx.effect(e, BURN, cb);
                Vector3d at = new Vector3d(p).add(0, 1, 0);
                SpellFx.fx(FX_FIRE, at, 1.4f, store, cb);
                SpellFx.fx(FX_BURST, at, 0.9f, store, cb);
                SpellFx.sound(SFX_HIT, at, cb);
            }
        }

        // ----- Eclats de roche -----

        /** Un bloc de la saignee, arrache et projete en l'air de part et d'autre (entite-bloc deplacee par le code). */
        private void throwDebris(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (world == null) return;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (int tries = 0; tries < 6; tries++) {
                int bx = (int) Math.floor(pos.x + right.x * rng.nextDouble(-1.2, 1.2)), bz = (int) Math.floor(pos.z + right.z * rng.nextDouble(-1.2, 1.2));
                int by = baseY - CUT_BELOW + rng.nextInt(0, 6);
                BlockType t = WorldBlockUtil.getBlockType(world, bx, by, bz);
                if (t == null || TerrainRestore.isEmpty(world, bx, by, bz) || TerrainRestore.isKept(world, t.getId(), bx, by, bz)) continue;
                Vector3d at = new Vector3d(bx + 0.5, by + 0.5, bz + 0.5);
                Ref<EntityStore> ref = SpellFx.spawnBlock(t.getId(), at, store, cb);
                if (ref == null) return;
                SpellFx.notSaved(ref, store, cb);
                double side = rng.nextBoolean() ? 1 : -1;
                Vector3d vel = new Vector3d(right).mul(side * rng.nextDouble(4, 9)).add(new Vector3d(dir).mul(rng.nextDouble(1, 5)))
                        .add(0, rng.nextDouble(9, 15), 0);
                debris.add(new Debris(ref, at, vel, now, (float) rng.nextDouble(4, 9) * (float) side));
                debrisCount++;
                return;
            }
        }

        private void moveDebris(long now, double dt, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (Iterator<Debris> it = debris.iterator(); it.hasNext(); ) {
                Debris d = it.next();
                long age = now - d.born;
                if (age > DEBRIS_LIFE_MS || d.ref == null || !d.ref.isValid()) {
                    if (d.ref != null && d.ref.isValid()) SpellFx.fx(FX_FIRE, d.pos, 0.5f, store, cb);
                    SpellFx.remove(d.ref, cb);
                    it.remove();
                    continue;
                }
                d.vel.y -= GRAVITY * dt;
                d.pos.add(new Vector3d(d.vel).mul(dt));
                Rotation3f rot = new Rotation3f();
                rot.setYaw(d.spin * age / 1000f);
                rot.setPitch(d.spin * 0.7f * age / 1000f);
                SpellFx.hold(d.ref, d.pos, rot, store);
            }
        }

        // ----- Explosions en chaine -----

        private void erupt(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean blasted = false;
            for (Iterator<Eruption> it = eruptions.iterator(); it.hasNext(); ) {
                Eruption e = it.next();
                if (now < e.dueAt()) continue;
                it.remove();
                SpellFx.fx(FX_EXPLOSION, e.at(), 1.1f, store, cb);
                SpellFx.fx(FX_PILLAR, e.at(), 0.9f, store, cb);
                SpellFx.fx(FX_FIRE, new Vector3d(e.at()).add(0, 2, 0), 1.6f, store, cb);
                if (!blasted) { SpellFx.sound(SFX_BLAST, e.at(), cb); SpellFx.shake(e.at(), 20, "Impact_Light", store); blasted = true; }
                blast(e.at(), ERUPTION_RADIUS, ERUPTION_DAMAGE, 6.0, store, cb);
            }
            if (!finale && !flying && eruptions.isEmpty() && now - flightEnd > ERUPTION_DELAY_MS) {
                finale = true;                                               // explosion finale au bout de la ligne
                Vector3d end = new Vector3d(pos.x, baseY - CUT_BELOW + 0.5, pos.z);
                SpellFx.fx(FX_EXPLOSION, end, 2.6f, store, cb);
                SpellFx.fx(FX_PILLAR, end, 2.0f, store, cb);
                SpellFx.fx(FX_BURST, new Vector3d(end).add(0, 3, 0), 3.0f, store, cb);
                SpellFx.sound(SFX_BLAST, end, cb);
                SpellFx.sound2dNear(SFX_THUNDER, end, 80, store);
                SpellFx.shake(end, 40, "Impact_Strong", store);
                blast(end, FINAL_RADIUS, FINAL_DAMAGE, 10.0, store, cb);
            }
        }

        /** Degats de feu autour d'un point, une fois par ennemi pour toutes les explosions de la chaine (sauf la finale). */
        private void blast(Vector3d at, double radius, float damage, double lift, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            boolean last = damage == FINAL_DAMAGE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, radius + CUT_ABOVE, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, at) > radius || p.y < at.y - 2 || p.y > at.y + CUT_ABOVE) continue;
                if (!last && !burned.add(e)) continue;
                SpellFx.damage(caster, e, "Fire", damage, new Vector3d(0, lift, 0), last ? "Impact_Strong" : "Impact_Light", cb);
                SpellFx.effect(e, BURN, cb);
            }
        }

        // ----- Fin -----

        /** Saignee ecrite sur disque (apres les blocs : meme file world.execute), sinon un arret du serveur la laisserait. */
        private void saveTerrain() {
            if (world == null || terrain == null) return;
            try { world.execute(TerrainRestore::save); }
            catch (Exception e) { EldaniorLogger.error("Ryukiri: sauvegarde du terrain", e); }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(model, cb);
            for (Debris d : debris) SpellFx.remove(d.ref, cb);
            debris.clear();
            saveTerrain();
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(model);
            for (Debris d : debris) RuneSpellRuntime.removeLater(d.ref);
            debris.clear();
            saveTerrain();
        }
    }
}
