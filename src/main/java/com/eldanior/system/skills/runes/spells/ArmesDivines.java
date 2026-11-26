package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Armes divines, une par famille d'arme (modeles, items, interactions et assets : scripts/artefacts/gen_divines.py ;
 * passifs : ArtefactWeapons ; classes : MasterySystem). Chaque interaction Divin_<Arme>_Signature (ou _Hold pour une
 * attaque maintenue) lance le sort du meme nom. Les effets s'appuient sur des choses concretes : vrais blocs (failles,
 * glace, pierre, sol brule, fleurs : TerrainRestore, jamais en zone protegee, restaures et sauvegardes), vrais rochers
 * projetes (entites-blocs), vraies fleches et vraies armes en vol (modeles deplaces par le code, comme Gungnir).
 *   - OeilDeRa, PhiltreDeCirce : soleil qui foudroie de rayons ; malefice qui bondit d'ennemi en ennemi ;
 *   - RegardDeMeduse (Egide, recharge 25 s) : le regard avance en cone ; ennemis petrifies, gangue de pierre ;
 *   - BrecheDeRoland (Durandal) : une faille court sur 22 blocs, projette la roche, souleve les ennemis ;
 *   - HiverDeSkadi : le sol gele, des pics de glace poussent un a un, ennemis pris dans la glace, puis tout eclate ;
 *   - ChargeDuMinotaure (Labrys) : ruee qui laboure le sol et encorne, puis coup de hache qui fend la terre ;
 *   - MoissonDesAmes (Thanatos) : une faux spectrale geante tourne trois fois, les ames soignent le porteur ;
 *   - MjollnirLancer (attaque maintenue) : le marteau tournoie sur 100 blocs, traverse tout, foudroie, revient en main ;
 *   - ColereDeThor : la foudre s'abat ennemi apres ennemi et brule le sol, puis frappe le marteau leve : onde de tonnerre ;
 *   - EcraseurDeMilliers (Sharur) : tourbillon qui attire, puis impact : cratere, ondes, rochers projetes ;
 *   - PluieLunaire (Artemis) : de vraies fleches d'argent tombent du ciel et se plantent ;
 *   - RuyiExtension (attaque maintenue) et BatonCeleste : le baton s'allonge droit devant ; le baton geant s'abat et fend le sol ;
 *   - CoupDHeracles : failles en eventail, roche arrachee, ennemis projetes ;
 *   - ChaudronDuDagda : onde qui blesse, fleurs qui eclosent, regeneration des allies ;
 *   - VoileDeLOmbre (Carnwennan) : cinq apparitions dans le dos des ennemis, puis retour au point de depart ;
 *   - GuandaoLancer (attaque maintenue) et CroissantDuDragon : le guandao tournoie et revient ; croissant de jade qui
 *     fauche l'herbe et entaille le sol ;
 *   - FlecheDuSoleil (Arc d'Apollon) : fleche d'or geante qui transperce, brule son sillage et explose en soleil.
 */
public final class ArmesDivines {

    private ArmesDivines() {}

    // ===== Assets communs =====
    private static final String FX_FIRE = "Impact_Fire", FX_BURST = "Gilgamesh_Sword_Burst", FX_EXPLOSION = "Explosion_Big";
    private static final String FX_SMOKE = "Divine_Smoke_Short", FX_SLASH = "Daggers_Signature_Slash", FX_ICE = "Niflheim_Freeze_Burst";
    private static final String FX_BOLT = "Taranis_Bolt", FX_SKY_FLASH = "Taranis_Sky_Flash", FX_SPARK = "Taranis_Electric_Burst";
    private static final String FX_HEAL = "Apollo_Heal_Pulse", FX_SUN = "Gilgamesh_King_Aura", FX_HEX = "Divine_Smoke_Short";
    private static final String FX_MOON_HIT = "Ability_Ice_Impact", FX_DUST = "Block_Land_Hard_Dust", FX_ROCK = "Block_Break_Stone";
    private static final String FX_DIRT = "Block_Break_Dirt", FX_ICE_SHARDS = "Block_Break_Ice", FX_ICE_BLAST = "Ability_Ice_Explosion";
    private static final String FX_PILLAR = "Muspelheim_Fire_Pillar", FX_STORM = "Taranis_Fog";
    private static final String SFX_THUNDER = "SFX_Global_Weather_Thunder", SFX_RUMBLE = "SFX_Golem_Earth_Swing_Impact";
    private static final String SFX_SLASH = "SFX_Ability_Wind_Strike_Slash", SFX_HIT = "SFX_Sword_T2_Impact", SFX_BLAST = "SFX_Fireball_Death";
    private static final String SFX_FIRE = "SFX_Staff_Flame_Fireball_Launch", SFX_STONE = "SFX_Stone_Break", SFX_STONE_BUILD = "SFX_Stone_Build";
    private static final String SFX_ICE_BUILD = "SFX_Ice_Build", SFX_ICE_BREAK = "SFX_Ice_Break";
    private static final String STUN = "Taranis_Foudroye", BURN = "Arme_Embrasement", FREEZE = "Elda_Gel", WEAK = "Elda_Faiblesse";
    private static final String STASIS = "Chronos_Fige", STONE = "Divin_Petrification", AEGIS = "Elda_Egide", BLIND = "Elda_Aveuglement";
    private static final String[] NEVER = {"Bedrock", "Barrier"};
    private static final String[] FLOWERS = {"Plant_Flower_Bushy_Blue", "Plant_Flower_Bushy_Cyan", "Plant_Flower_Bushy_Orange",
            "Plant_Flower_Bushy_Purple", "Plant_Flower_Bushy_Red"};

    // =====================================================================================================================
    // Socle commun
    // =====================================================================================================================

    /** Rocher arrache au sol : entite-bloc deplacee par le code (balistique), retiree a la retombee. */
    private static final class Rock {
        final Ref<EntityStore> ref; final Vector3d pos, vel; final long born; final float spin;
        Rock(Ref<EntityStore> ref, Vector3d pos, Vector3d vel, long born, float spin) { this.ref = ref; this.pos = pos; this.vel = vel; this.born = born; this.spin = spin; }
    }

    /** Effet persistant (fumee qui monte d'un sol brule, poussiere qui retombe...). */
    private static final class Linger {
        final String fx; final Vector3d pos; final float scale; final long until, every; long next;
        Linger(String fx, Vector3d pos, float scale, long until, long every) { this.fx = fx; this.pos = pos; this.scale = scale; this.until = until; this.every = every; }
    }

    /** Sort qui se deroule dans le temps : step(t) a chaque tick ; rochers, blocs et lot de terrain geres ici. */
    private abstract static class Timed implements RuneSpell.Instance {
        final Ref<EntityStore> caster;
        final long startAt = System.currentTimeMillis(), durationMs;
        long restoreMs = 60_000, lastTick = startAt;
        World world;
        TerrainRestore.Restore terrain;
        final List<Rock> rocks = new ArrayList<>();
        final List<Linger> lingers = new ArrayList<>();

        Timed(Ref<EntityStore> caster, long durationMs) { this.caster = caster; this.durationMs = durationMs; }

        /** Effet qui persiste apres l'impact (fumee, poussiere, braises) : repete tous les everyMs pendant forMs. */
        void linger(String fx, Vector3d pos, float scale, long forMs, long everyMs) {
            if (lingers.size() < 60) lingers.add(new Linger(fx, new Vector3d(pos), scale, System.currentTimeMillis() + forMs, everyMs));
        }

        /** Sol brule sous un point (terre, herbe). */
        void scorch(Vector3d at) {
            if (world == null) return;
            Vector3d g = SpellFx.ground(world, at);
            int x = (int) Math.floor(g.x), y = (int) Math.floor(g.y) - 1, z = (int) Math.floor(g.z);
            BlockType bt = WorldBlockUtil.getBlockType(world, x, y, z);
            if (bt != null && (bt.getId().startsWith("Soil_") || bt.getId().contains("Grass"))) setCells(List.of(new int[]{x, y, z}), "Soil_Dirt_Burnt", true, terrain());
        }

        @Override
        public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            if (world == null) world = SpellFx.world(store);
            long t = now - startAt;
            double dt = Math.min(0.2, (now - lastTick) / 1000.0);
            lastTick = now;
            step(t, store, cb);
            moveRocks(now, dt, store, cb);
            for (Iterator<Linger> it = lingers.iterator(); it.hasNext(); ) {
                Linger l = it.next();
                if (now >= l.until) { it.remove(); continue; }
                if (now >= l.next) { l.next = now + l.every; SpellFx.fx(l.fx, l.pos, l.scale, store, cb); }
            }
            return t >= durationMs && rocks.isEmpty() && lingers.isEmpty();
        }

        abstract void step(long t, Store<EntityStore> store, CommandBuffer<EntityStore> cb);

        void onEnd(CommandBuffer<EntityStore> cb) {}

        @Override
        public final void cleanup(CommandBuffer<EntityStore> cb) {
            for (Rock r : rocks) SpellFx.remove(r.ref, cb);
            rocks.clear();
            onEnd(cb);
            save();
        }

        @Override
        public final void abort() {
            for (Rock r : rocks) RuneSpellRuntime.removeLater(r.ref);
            rocks.clear();
            onAbort();
            save();
        }

        void onAbort() {}

        void save() { if (world != null && terrain != null) world.execute(TerrainRestore::save); }

        TerrainRestore.Restore terrain() {
            if (terrain == null && world != null) terrain = TerrainRestore.open(world, System.currentTimeMillis() + restoreMs);
            return terrain;
        }

        /** Rocher arrache a la case (ou du type de bloc donne) et projete. */
        void throwRock(String blockId, Vector3d at, Vector3d vel, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
            if (blockId == null || rocks.size() > 40) return;
            Ref<EntityStore> ref = SpellFx.spawnBlock(blockId, at, s, c);
            if (ref == null) return;
            SpellFx.notSaved(ref, s, c);
            rocks.add(new Rock(ref, new Vector3d(at), vel, System.currentTimeMillis(), (float) ThreadLocalRandom.current().nextDouble(-9, 9)));
        }

        /** Bloc solide de surface sous un point (identifiant), ou null. */
        String surfaceBlock(Vector3d at) {
            if (world == null) return null;
            Vector3d g = SpellFx.ground(world, at);
            BlockType t = WorldBlockUtil.getBlockType(world, (int) Math.floor(g.x), (int) Math.floor(g.y) - 1, (int) Math.floor(g.z));
            return t == null || t == BlockType.EMPTY || "Empty".equals(t.getId()) || TerrainRestore.isKept(world, t.getId(), (int) Math.floor(g.x), (int) Math.floor(g.y) - 1, (int) Math.floor(g.z)) ? null : t.getId();
        }

        private void moveRocks(long now, double dt, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
            for (Iterator<Rock> it = rocks.iterator(); it.hasNext(); ) {
                Rock r = it.next();
                long age = now - r.born;
                if (age > 2_000 || r.ref == null || !r.ref.isValid()) {
                    if (r.ref != null && r.ref.isValid()) SpellFx.fx(FX_DUST, r.pos, 0.6f, s, c);
                    SpellFx.remove(r.ref, c);
                    it.remove();
                    continue;
                }
                r.vel.y -= 24 * dt;
                r.pos.add(new Vector3d(r.vel).mul(dt));
                Rotation3f rot = new Rotation3f();
                rot.setYaw(r.spin * age / 1000f);
                rot.setPitch(r.spin * 0.7f * age / 1000f);
                SpellFx.hold(r.ref, r.pos, rot, s);
            }
        }

        /**
         * Blocs : EMPTY = la case est videe (faille), sinon le bloc donne remplace une case solide (replace) ou remplit une
         * case vide. Hors du tick ECS, jamais en zone protegee, ni sur un bloc conserve, ni dans un fluide.
         */
        void setCells(List<int[]> cells, String placed, boolean replaceSolid, TerrainRestore.Restore batch) {
            if (cells.isEmpty() || world == null || batch == null) return;
            World w = world;
            w.execute(() -> {
                for (int[] c : cells) {
                    try {
                        BlockType type = WorldBlockUtil.getBlockType(w, c[0], c[1], c[2]);
                        if (type == null || TerrainRestore.isProtectedArea(w.getName(), c[0], c[1], c[2]) || WorldBlockUtil.getFluidIndex(w, c[0], c[1], c[2]) > 0) continue;
                        boolean empty = TerrainRestore.isEmpty(w, c[0], c[1], c[2]);
                        String id = type.getId();
                        if (!empty && (TerrainRestore.contains(id, NEVER) || TerrainRestore.isKept(w, id, c[0], c[1], c[2]))) continue;
                        if (TerrainRestore.EMPTY.equals(placed)) {
                            if (!empty) TerrainRestore.place(w, batch, c[0], c[1], c[2], id, TerrainRestore.EMPTY, 0);
                        } else if (empty ? !replaceSolid : replaceSolid) {
                            TerrainRestore.place(w, batch, c[0], c[1], c[2], empty ? TerrainRestore.EMPTY : id, placed, 0);
                        }
                    } catch (Exception ignored) { }
                }
            });
        }

        /** Faille : cases sous la surface au droit d'un point (largeur 1 ou 3), videes. */
        void crack(Vector3d at, int depth, boolean wide, Vector3d side) {
            if (world == null) return;
            Vector3d g = SpellFx.ground(world, at);
            int top = (int) Math.floor(g.y);
            List<int[]> cells = new ArrayList<>();
            for (int k = wide ? -1 : 0; k <= (wide ? 1 : 0); k++) {
                int x = (int) Math.floor(g.x + side.x * k * 0.8), z = (int) Math.floor(g.z + side.z * k * 0.8);
                int d = k == 0 ? depth : Math.max(1, depth - 1);
                for (int y = top; y >= top - d; y--) cells.add(new int[]{x, y, z});
            }
            setCells(cells, TerrainRestore.EMPTY, false, terrain());
        }

        /** Gangue (cage de 8 cases sur 2 de haut, couvercle optionnel) autour d'une cible, restauree apres ms. */
        void cage(Vector3d at, String block, boolean top, long ms) {
            if (world == null) return;
            TerrainRestore.Restore batch = TerrainRestore.open(world, System.currentTimeMillis() + ms);
            int cx = (int) Math.floor(at.x), cy = (int) Math.floor(at.y), cz = (int) Math.floor(at.z);
            List<int[]> cells = new ArrayList<>();
            for (int dy = 0; dy <= 2; dy++)
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++) {
                        boolean wall = dx != 0 || dz != 0;
                        if (dy < 2 ? !wall : (wall || !top)) continue;
                        cells.add(new int[]{cx + dx, cy + dy, cz + dz});
                    }
            setCells(cells, block, false, batch);
        }
    }

    private static Vector3d flatForward(Ref<EntityStore> ref, Store<EntityStore> store) {
        Vector3d f = SpellFx.lookDirection(ref, store);
        f.y = 0;
        if (f.lengthSquared() < 1e-4) f = SpellFx.forward(ref, store);
        f.y = 0;
        if (f.lengthSquared() < 1e-4) f.set(1, 0, 0);
        return f.normalize();
    }

    private static Vector3d chest(Ref<EntityStore> e, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        return p != null ? p.add(0, 1.0, 0) : null;
    }

    private static List<Ref<EntityStore>> nearest(Ref<EntityStore> caster, Vector3d at, double radius, Store<EntityStore> store) {
        List<Ref<EntityStore>> list = new ArrayList<>(SpellFx.enemiesAround(caster, at, radius, store));
        list.removeIf(e -> SpellFx.position(e, store) == null);
        list.sort(Comparator.comparingDouble(e -> SpellFx.position(e, store).distanceSquared(at)));
        return list;
    }

    private static Vector3d outward(Vector3d center, Vector3d p, Vector3d fallback, double force, double lift) {
        Vector3d out = new Vector3d(p.x - center.x, 0, p.z - center.z);
        out = out.lengthSquared() > 0.04 ? out.normalize().mul(force) : new Vector3d(fallback).mul(force);
        return out.add(0, lift, 0);
    }

    private static void ring(String fx, Vector3d c, double r, int n, float scale, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        for (int i = 0; i < n; i++) {
            double a = i * 2 * Math.PI / n;
            SpellFx.fx(fx, new Vector3d(c).add(Math.cos(a) * r, 0, Math.sin(a) * r), scale, store, cb);
        }
    }

    private static Vector3d rotateY(Vector3d v, double rad) {
        double c = Math.cos(rad), s = Math.sin(rad);
        return new Vector3d(v.x * c - v.z * s, v.y, v.x * s + v.z * c);
    }

    /** Cratere (comme le Puits Gravitationnel), restaure apres 90 s. */
    private static void crater(Vector3d at, Store<EntityStore> store) {
        World world = SpellFx.world(store);
        if (world == null) return;
        try {
            Vector3d ground = SpellFx.ground(world, at);
            TerrainRestore.Restore batch = TerrainRestore.open(world, System.currentTimeMillis() + 90_000);
            PuitsGravitationnel.crater(world, batch, ground, PuitsGravitationnel.planCracks(ground));
            world.execute(TerrainRestore::save);
        } catch (Exception e) {
            EldaniorLogger.error("ArmesDivines: cratere", e);
        }
    }

    // =====================================================================================================================
    // Ouas de Ra : Oeil de Ra
    // =====================================================================================================================

    public static final class OeilDeRa implements RuneSpell {
        private static final String CARRIER = "Projectile_Config_Ra_Soleil", MODEL = "Ra_Soleil", BEAM = "Ra_Rayon";
        private static final long DURATION = 6_000, RISE_MS = 500, RAY_EVERY = 320;
        private static final float SUN_SCALE = 5f;
        private static final double HEIGHT = 5.5, RANGE = 18;

        @Override public String id() { return "OeilDeRa"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d p = SpellFx.position(caster, store);
            if (p == null) return null;
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, new Vector3d(p).add(0, 1.5, 0), new Vector3d(1, 0, 0), cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            SpellFx.fx(FX_BURST, new Vector3d(p).add(0, 1.6, 0), 1.6f, store, cb);
            SpellFx.sound(SFX_FIRE, p, cb);
            return new Timed(caster, DURATION + 500) {
                long nextRay, nextScale;
                float scale = 1f;
                boolean set;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    Vector3d pos = SpellFx.position(caster, s);
                    if (pos == null || model == null || !model.isValid()) return;
                    double rise = Math.min(1.0, t / (double) RISE_MS);
                    if (t < RISE_MS + 80 && t >= nextScale) {                      // le soleil se leve du sceptre et grandit
                        nextScale = t + 70;
                        scale = 1f + (SUN_SCALE - 1f) * (float) rise;
                        SpellFx.setModelScale(model, MODEL, scale, 1.0, c);
                    }
                    if (t > DURATION && t >= nextScale) {                          // puis il se couche et s'eteint
                        nextScale = t + 60;
                        scale = Math.max(0.3f, scale * 0.65f);
                        SpellFx.setModelScale(model, MODEL, scale, 1.0, c);
                        if (!set) { set = true; SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, HEIGHT, 0), 2.5f, s, c); }
                    }
                    Vector3d sun = new Vector3d(pos).add(0, 1.5 + (HEIGHT - 1.5) * rise, 0);
                    Rotation3f rot = new Rotation3f();
                    rot.setYaw((float) (t / 1000.0 * 1.4));
                    SpellFx.hold(model, sun, rot, s);
                    if (t < RISE_MS || t > DURATION || t < nextRay) return;
                    nextRay = t + RAY_EVERY;
                    List<Ref<EntityStore>> foes = nearest(caster, pos, RANGE, s);
                    int n = Math.min(3, foes.size());
                    for (int i = 0; i < n; i++) {                                   // rayons de soleil sur les ennemis
                        Ref<EntityStore> e = foes.get(i);
                        Vector3d at = chest(e, s);
                        if (at == null) continue;
                        SpellFx.spawnBeam(BEAM, sun, at, 0.9f, 300, s, c);
                        SpellFx.fx(FX_BURST, at, 0.8f, s, c);
                        Vector3d feet = SpellFx.position(e, s);
                        if (feet != null) { SpellFx.fx(FX_FIRE, feet, 0.8f, s, c); scorch(feet); linger("Smoke_Black", feet, 0.5f, 2_000, 500); }
                        SpellFx.damage(caster, e, "Fire", 110f, null, "Impact_Light", c);
                        SpellFx.effect(e, BURN, c);
                    }
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    if (n < 3 && rng.nextFloat() < 0.6f) {                          // et la terre brule sous lui
                        double a = rng.nextDouble(Math.PI * 2), r = rng.nextDouble(2, 9);
                        Vector3d g = SpellFx.ground(world, new Vector3d(pos).add(Math.cos(a) * r, 0, Math.sin(a) * r));
                        SpellFx.spawnBeam(BEAM, sun, g, 0.7f, 250, s, c);
                        SpellFx.fx(FX_FIRE, g, 0.9f, s, c);
                        scorch(g);
                        linger("Smoke_Black", g, 0.5f, 1_800, 500);
                    }
                    SpellFx.sound(SFX_FIRE, sun, c);
                }

                @Override void onEnd(CommandBuffer<EntityStore> c) { SpellFx.remove(model, c); }
                @Override void onAbort() { RuneSpellRuntime.removeLater(model); }
            };
        }
    }

    // =====================================================================================================================
    // Baguette de Circe : Philtre de Circe
    // =====================================================================================================================

    public static final class PhiltreDeCirce implements RuneSpell {
        @Override public String id() { return "PhiltreDeCirce"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d from = chest(caster, store);
            if (from == null) return null;
            Ref<EntityStore> first = SpellFx.targetEntity(caster, store, 25);
            if (first == null || !SpellFx.isEnemy(caster, first, store)) {
                List<Ref<EntityStore>> foes = nearest(caster, from, 15, store);
                first = foes.isEmpty() ? null : foes.get(0);
            }
            if (first == null) return null;
            final Ref<EntityStore> start = first;
            return new Timed(caster, 8 * 180) {
                final Set<Ref<EntityStore>> hit = new HashSet<>();
                Ref<EntityStore> current = start;
                Vector3d lastPos = from;
                long next;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    if (t < next || current == null || hit.size() >= 8) return;
                    next = t + 180;
                    Vector3d at = chest(current, s);
                    if (at == null) { current = null; return; }
                    hit.add(current);
                    SpellFx.arc(FX_HEX, lastPos, at, 0.7f, s, c);
                    SpellFx.fx(FX_HEX, at, 1.2f, s, c);
                    SpellFx.sound(SFX_HIT, at, c);
                    SpellFx.damage(caster, current, "Physical", 140f, null, "Impact_Light", c);
                    SpellFx.effect(current, WEAK, c);
                    SpellFx.stun(current, STASIS, 1_500, s, c);
                    lastPos = at;
                    current = null;
                    for (Ref<EntityStore> e : nearest(caster, at, 10, s)) if (!hit.contains(e)) { current = e; break; }
                }
            };
        }
    }

    // =====================================================================================================================
    // Egide : Regard de Meduse (recharge, pas d'energie)
    // =====================================================================================================================

    public static final class RegardDeMeduse implements RuneSpell {
        private static final double RANGE = 12, HALF_ANGLE = Math.toRadians(40);
        private static final long STONE_MS = 4_000;

        @Override public String id() { return "RegardDeMeduse"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d origin = new Vector3d(pos);
            SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1.4, 0).add(new Vector3d(fwd).mul(1.2)), 1.6f, store, cb);
            SpellFx.effect(caster, AEGIS, cb);
            SpellFx.sound2dNear(SFX_THUNDER, pos, 30, store);
            return new Timed(caster, 900) {
                final Set<Ref<EntityStore>> hit = new HashSet<>();
                int k;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    while (k < 10 && t >= k * 70L) {                          // le regard avance d'1,2 bloc toutes les 70 ms
                        k++;
                        double d = k * RANGE / 10;
                        double half = Math.tan(HALF_ANGLE) * d;
                        Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
                        for (int i = -2; i <= 2; i++) {
                            Vector3d p = new Vector3d(origin).add(new Vector3d(fwd).mul(d)).add(new Vector3d(side).mul(half * i / 2.0)).add(0, 1, 0);
                            SpellFx.fx(FX_SMOKE, p, 1.0f, s, c);
                        }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, origin, d + 1.5, s)) {
                            Vector3d p = SpellFx.position(e, s);
                            if (p == null || hit.contains(e)) continue;
                            Vector3d to = new Vector3d(p.x - origin.x, 0, p.z - origin.z);
                            if (to.length() > d || (to.length() > 1 && to.angle(fwd) > HALF_ANGLE)) continue;
                            hit.add(e);
                            SpellFx.damage(caster, e, "Physical", 150f, null, "Impact_Strong", c);
                            SpellFx.stun(e, STONE, STONE_MS, s, c);
                            cage(p, "Rock_Stone", false, STONE_MS + 300);
                            SpellFx.fx(FX_ROCK, new Vector3d(p).add(0, 1, 0), 1.4f, s, c);
                            SpellFx.sound(SFX_STONE_BUILD, p, c);
                        }
                    }
                }
            };
        }
    }

    // =====================================================================================================================
    // Durandal : Breche de Roland
    // =====================================================================================================================

    public static final class BrecheDeRoland implements RuneSpell {
        private static final int LENGTH = 22;

        @Override public String id() { return "BrecheDeRoland"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
            Vector3d impact = new Vector3d(pos).add(new Vector3d(fwd).mul(1.5));
            SpellFx.fx(FX_EXPLOSION, impact, 0.9f, store, cb);
            SpellFx.fx(FX_DUST, impact, 1.6f, store, cb);
            SpellFx.sound(SFX_STONE, impact, cb);
            SpellFx.sound2dNear(SFX_THUNDER, impact, 50, store);
            SpellFx.shake(impact, 25, "Impact_Strong", store);
            return new Timed(caster, LENGTH * 55L + 200) {
                final Set<Ref<EntityStore>> hit = new HashSet<>();
                final Vector3d tip = new Vector3d(impact);
                double drift;
                int k;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    while (k < LENGTH && t >= k * 55L) {                     // la faille court en zigzag
                        k++;
                        drift = Math.max(-1.5, Math.min(1.5, drift + rng.nextDouble(-0.6, 0.6)));
                        tip.set(impact).add(new Vector3d(fwd).mul(k)).add(new Vector3d(side).mul(drift));
                        String rock = surfaceBlock(tip);
                        crack(tip, 3 + (k % 3 == 0 ? 1 : 0), k % 4 == 0, side);
                        SpellFx.fx(FX_DUST, tip, 1.1f, s, c);
                        SpellFx.fx(FX_ROCK, new Vector3d(tip).add(0, 0.5, 0), 1.0f, s, c);
                        if (k % 3 == 0) linger(FX_DUST, tip, 0.8f, 2_200, 450);              // la poussiere retombe
                        if (k % 2 == 0) {
                            double sgn = rng.nextBoolean() ? 1 : -1;
                            throwRock(rock, new Vector3d(tip).add(0, 0.6, 0), new Vector3d(side).mul(sgn * rng.nextDouble(3, 7)).add(new Vector3d(fwd).mul(rng.nextDouble(0, 3))).add(0, rng.nextDouble(8, 12), 0), s, c);
                        }
                        if (k % 5 == 0) { SpellFx.sound(SFX_RUMBLE, tip, c); SpellFx.shake(tip, 15, "Impact_Light", s); }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, tip, 3, s)) {
                            Vector3d p = SpellFx.position(e, s);
                            if (p == null || !hit.add(e) || SpellFx.horizontalDistance(p, tip) > 2.2) continue;
                            SpellFx.damage(caster, e, "Physical", 220f, new Vector3d(0, 9, 0).add(new Vector3d(fwd).mul(2)), "Impact_Strong", c);
                            SpellFx.stun(e, STUN, 1_200, s, c);
                        }
                    }
                }
            };
        }
    }

    // =====================================================================================================================
    // Hache de Skadi : Hiver de Skadi
    // =====================================================================================================================

    public static final class HiverDeSkadi implements RuneSpell {
        private static final int LENGTH = 16;
        private static final long SHATTER_AT = 5_000;

        @Override public String id() { return "HiverDeSkadi"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
            SpellFx.sound(SFX_ICE_BUILD, pos, cb);
            Timed winter = new Timed(caster, SHATTER_AT + 300) {
                final Set<Ref<EntityStore>> hit = new HashSet<>();
                final List<long[]> growth = new ArrayList<>();             // {date, x, y, z, hauteur atteinte, hauteur finale, dx, dz}
                final List<Vector3d> line = new ArrayList<>();
                int k;
                boolean shattered;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    while (k < LENGTH && t >= k * 60L) {                      // le sol gele, un pic tous les 2 blocs
                        k++;
                        Vector3d at = new Vector3d(pos).add(new Vector3d(fwd).mul(k + 0.5));
                        Vector3d g = SpellFx.ground(world, at);
                        line.add(g);
                        int top = (int) Math.floor(g.y) - 1;
                        List<int[]> floor = new ArrayList<>();
                        for (int j = -1; j <= 1; j++)
                            floor.add(new int[]{(int) Math.floor(g.x + side.x * j), top, (int) Math.floor(g.z + side.z * j)});
                        setCells(floor, "Rock_Ice", true, terrain());
                        SpellFx.fx(FX_ICE, new Vector3d(g).add(0, 0.5, 0), 0.9f, s, c);
                        if (k % 2 == 1) linger("Niflheim_Flurry", new Vector3d(g).add(0, 0.6, 0), 0.5f, SHATTER_AT - t, 700);   // brume de givre
                        if (k % 2 == 0) {
                            double off = rng.nextDouble(-1.2, 1.2);
                            int x = (int) Math.floor(g.x + side.x * off), z = (int) Math.floor(g.z + side.z * off);
                            int lean = rng.nextInt(-1, 2);
                            growth.add(new long[]{t, x, top + 1, z, 0, 2 + rng.nextInt(3), Math.round(side.x * lean), Math.round(side.z * lean)});
                        }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g, 3, s)) {
                            Vector3d p = SpellFx.position(e, s);
                            if (p == null || !hit.add(e) || SpellFx.horizontalDistance(p, g) > 2.2) continue;
                            SpellFx.damage(caster, e, "Physical", 150f, null, "Impact_Strong", c);
                            SpellFx.effect(e, FREEZE, c);
                            SpellFx.lockInPlace(e, 3_000, s);
                            cage(p, "Rock_Ice", true, 3_200);                  // prisonnier de la glace
                            SpellFx.fx(FX_ICE_SHARDS, new Vector3d(p).add(0, 1, 0), 1.2f, s, c);
                        }
                    }
                    for (long[] g : growth) {                                 // chaque pic pousse d'un bloc toutes les 60 ms
                        if (g[4] >= g[5] || t < g[0] + g[4] * 60) continue;
                        int h = (int) g[4];
                        int x = (int) (g[1] + (h >= 2 ? g[6] : 0)), y = (int) (g[2] + h), z = (int) (g[3] + (h >= 2 ? g[7] : 0));
                        setCells(List.of(new int[]{x, y, z}), "Rock_Ice", false, terrain());
                        if (h == 0) SpellFx.sound(SFX_ICE_BUILD, new Vector3d(x + 0.5, y, z + 0.5), c);
                        g[4]++;
                    }
                    if (!shattered && t >= SHATTER_AT) {                      // tout vole en eclats
                        shattered = true;
                        TerrainRestore.Restore batch = terrain;
                        if (batch != null) world.execute(() -> TerrainRestore.restoreNow(world, batch));
                        for (int i = 0; i < line.size(); i += 2) SpellFx.fx(FX_ICE_BLAST, new Vector3d(line.get(i)).add(0, 1, 0), 0.8f, s, c);
                        if (!line.isEmpty()) SpellFx.sound(SFX_ICE_BREAK, line.get(line.size() / 2), c);
                    }
                }
            };
            winter.restoreMs = SHATTER_AT + 5_000;                            // securite si le sort est interrompu
            return winter;
        }
    }

    // =====================================================================================================================
    // Labrys : Charge du Minotaure
    // =====================================================================================================================

    public static final class ChargeDuMinotaure implements RuneSpell {
        private static final long CHARGE_MS = 650;

        @Override public String id() { return "ChargeDuMinotaure"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
            SpellFx.protectFromFall(caster, 2_500, store);
            SpellFx.sound(SFX_RUMBLE, pos, cb);
            SpellFx.sound2dNear("SFX_Bear_Voidtaken_Charge_Roar", pos, 30, store);
            return new Timed(caster, 1_000) {
                final Set<Ref<EntityStore>> gored = new HashSet<>();
                long nextFurrow;
                boolean chopped;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    Vector3d p = SpellFx.position(caster, s);
                    if (p == null) return;
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    if (t < CHARGE_MS) {
                        SpellFx.push(caster, new Vector3d(fwd).mul(20).add(0, -1, 0), false, c);   // ruee soutenue
                        if (t >= nextFurrow) {                                  // le sol est laboure derriere lui
                            nextFurrow = t + 70;
                            SpellFx.fx(FX_DUST, p, 1.0f, s, c);
                            SpellFx.fx(FX_DIRT, p, 0.8f, s, c);
                            Vector3d g = SpellFx.ground(world, p);
                            int y = (int) Math.floor(g.y) - 1;
                            List<int[]> soil = List.of(new int[]{(int) Math.floor(g.x), y, (int) Math.floor(g.z)});
                            BlockType bt = WorldBlockUtil.getBlockType(world, (int) Math.floor(g.x), y, (int) Math.floor(g.z));
                            if (bt != null && bt.getId().contains("Grass")) setCells(soil, "Soil_Dirt", true, terrain());
                        }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, 2.8, s)) {
                            Vector3d ep = SpellFx.position(e, s);
                            if (ep == null || !gored.add(e)) continue;
                            double sgn = new Vector3d(ep).sub(p).dot(side) >= 0 ? 1 : -1;      // encorne et rejette de cote
                            SpellFx.damage(caster, e, "Physical", 200f, new Vector3d(side).mul(8 * sgn).add(new Vector3d(fwd).mul(6)).add(0, 10, 0), "Impact_Strong", c);
                            SpellFx.fx(FX_BURST, new Vector3d(ep).add(0, 1, 0), 0.9f, s, c);
                            SpellFx.sound(SFX_HIT, ep, c);
                        }
                    } else if (!chopped) {                                      // la hache s'abat et fend la terre
                        chopped = true;
                        SpellFx.push(caster, new Vector3d(0, 0, 0), false, c);
                        Vector3d impact = new Vector3d(p).add(new Vector3d(fwd).mul(2));
                        SpellFx.fx(FX_EXPLOSION, impact, 1.3f, s, c);
                        SpellFx.fx(FX_DUST, impact, 2.0f, s, c);
                        SpellFx.sound(SFX_STONE, impact, c);
                        SpellFx.sound2dNear(SFX_THUNDER, impact, 45, s);
                        SpellFx.shake(impact, 30, "Impact_Strong", s);
                        crater(impact, s);
                        linger(FX_DUST, impact, 1.2f, 2_000, 400);
                        for (int i = 1; i <= 8; i++) crack(new Vector3d(impact).add(new Vector3d(fwd).mul(i + 1)), 2, i < 4, side);
                        String rock = surfaceBlock(impact);
                        for (int i = 0; i < 6; i++) {
                            double a = rng.nextDouble(Math.PI * 2);
                            throwRock(rock, new Vector3d(impact).add(0, 0.8, 0), new Vector3d(Math.cos(a) * 5, rng.nextDouble(9, 13), Math.sin(a) * 5), s, c);
                        }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, impact, 5.5, s)) {
                            Vector3d ep = SpellFx.position(e, s);
                            if (ep != null) SpellFx.damage(caster, e, "Physical", 140f, outward(impact, ep, fwd, 8, 8), "Impact_Strong", c);
                        }
                    }
                }
            };
        }
    }

    // =====================================================================================================================
    // Faux de Thanatos : Moisson des Ames (faux spectrale geante)
    // =====================================================================================================================

    public static final class MoissonDesAmes implements RuneSpell {
        private static final String CARRIER = "Projectile_Config_Thanatos_Faux_Spectrale", MODEL = "Thanatos_Faux_Spectrale";
        private static final long SPIN_MS = 450;
        private static final int SPINS = 3;
        private static final double REACH = 7.0;

        @Override public String id() { return "MoissonDesAmes"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final double a0 = Math.atan2(fwd.z, fwd.x);
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, new Vector3d(pos).add(0, 1.2, 0), fwd, cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            SpellFx.sound2dNear(SFX_THUNDER, pos, 35, store);
            return new Timed(caster, SPIN_MS * SPINS + 100) {
                final List<Set<Ref<EntityStore>>> reaped = List.of(new HashSet<>(), new HashSet<>(), new HashSet<>());
                boolean scaled;
                int lastSpin = -1;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    Vector3d p = SpellFx.position(caster, s);
                    if (p == null || model == null || !model.isValid()) return;
                    if (!scaled) { SpellFx.setModelScale(model, MODEL, 2.6f, 3.0, c); scaled = true; }
                    int spin = (int) Math.min(SPINS - 1, t / SPIN_MS);
                    if (spin != lastSpin) { lastSpin = spin; SpellFx.sound(SFX_SLASH, p, c); }
                    double angle = a0 + 2 * Math.PI * Math.min(1.0, t / (double) (SPIN_MS * SPINS)) * SPINS;
                    Vector3d radial = new Vector3d(Math.cos(angle), 0, Math.sin(angle));
                    Vector3d center = new Vector3d(p).add(0, 1.2, 0);
                    SpellFx.hold(model, center, SpellFx.facing(radial, false), s);
                    SpellFx.fx(FX_SMOKE, new Vector3d(center).add(new Vector3d(radial).mul(REACH - 1)), 0.7f, s, c);
                    if (t > SPIN_MS * SPINS) return;
                    for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, REACH + 1, s)) {
                        Vector3d ep = SpellFx.position(e, s);
                        if (ep == null || reaped.get(spin).contains(e) || SpellFx.horizontalDistance(ep, p) > REACH) continue;
                        double ea = Math.atan2(ep.z - p.z, ep.x - p.x);
                        double diff = Math.abs(Math.atan2(Math.sin(ea - angle), Math.cos(ea - angle)));
                        if (diff > Math.toRadians(28)) continue;                   // la lame passe sur lui
                        reaped.get(spin).add(e);
                        float dmg = ArtefactWeapons.healthRatio(e, s) < 0.25f ? 99_999f : 130f;
                        SpellFx.damage(caster, e, "Physical", dmg, new Vector3d(radial).mul(3).add(0, 2, 0), "Impact_Light", c);
                        Vector3d soul = new Vector3d(ep).add(0, 1.2, 0);
                        SpellFx.arc(FX_SMOKE, soul, new Vector3d(p).add(0, 1.4, 0), 0.6f, s, c);   // l'ame rejoint le faucheur
                        SpellFx.restore(caster, DefaultEntityStatTypes.getHealth(), 0.04f, c);
                    }
                }

                @Override void onEnd(CommandBuffer<EntityStore> c) { SpellFx.remove(model, c); }
                @Override void onAbort() { RuneSpellRuntime.removeLater(model); }
            };
        }
    }

    // =====================================================================================================================
    // Armes lancees qui reviennent en main (Mjollnir, Guandao)
    // =====================================================================================================================

    private abstract static class Thrown implements RuneSpell.Instance {
        final Ref<EntityStore> caster;
        final String itemId;
        Ref<EntityStore> model;
        final Vector3d pos, dir;
        final double range, outSpeed, backSpeed, hitRadius, carveRadius;
        final World world;
        final long startAt = System.currentTimeMillis();
        final Set<Ref<EntityStore>> pierced = new HashSet<>();
        final Set<Long> carved = new HashSet<>();
        TerrainRestore.Restore terrain;
        long last = startAt;
        double travelled, spin;
        boolean returning, caught;
        int hiddenSlot = -1;

        Thrown(Ref<EntityStore> caster, String itemId, String carrier, Vector3d from, Vector3d dir, double range, double outSpeed, double backSpeed,
               double hitRadius, double carveRadius, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            this.caster = caster; this.itemId = itemId; this.pos = new Vector3d(from); this.dir = new Vector3d(dir).normalize();
            this.range = range; this.outSpeed = outSpeed; this.backSpeed = backSpeed; this.hitRadius = hitRadius; this.carveRadius = carveRadius;
            this.world = SpellFx.world(store);
            this.model = SpellFx.spawnModel(caster, carrier, from, dir, cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            hideInHand(store);
        }

        abstract Rotation3f rotation(Vector3d heading);

        abstract void hit(Ref<EntityStore> e, Vector3d at, Store<EntityStore> s, CommandBuffer<EntityStore> c);

        @Override
        public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!caster.isValid() || now - startAt > 10_000 || caught) { showInHand(store); return true; }
            double dt = Math.min(0.1, (now - last) / 1000.0);
            last = now;
            Vector3d hand = chest(caster, store);
            Vector3d goal;
            if (!returning) goal = null;
            else if (hand != null) goal = new Vector3d(hand).add(0, 0.4, 0);
            else { showInHand(store); return true; }
            double step = (returning ? backSpeed : outSpeed) * dt;
            Vector3d before = new Vector3d(pos);
            Vector3d heading = returning ? new Vector3d(goal).sub(pos) : new Vector3d(dir);
            double dist = heading.length();
            if (dist > 1e-4) heading.div(dist);
            if (returning && dist <= Math.max(step, 1.6)) {                    // de retour dans la main
                SpellFx.fx(FX_BURST, pos, 0.5f, store, cb);
                SpellFx.remove(model, cb);
                caught = true;
                return false;
            }
            pos.add(new Vector3d(heading).mul(step));
            if (!returning) { travelled += step; if (travelled >= range) returning = true; }
            spin += dt;
            if (model != null && model.isValid()) SpellFx.hold(model, pos, rotation(heading), store);
            if (carveRadius > 0 && (hand == null || hand.distance(pos) > 3)) carve(before, pos);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, hitRadius + 1, store)) {
                Vector3d c = chest(e, store);
                if (c == null || c.distance(pos) > hitRadius + 0.6 || !pierced.add(e)) continue;
                hit(e, c, store, cb);
            }
            return false;
        }

        /** Tunnel le long du trajet (restaure apres 2 minutes), comme Gungnir. */
        private void carve(Vector3d from, Vector3d to) {
            if (world == null) return;
            List<int[]> cells = new ArrayList<>();
            int steps = Math.max(1, (int) Math.ceil(from.distance(to) / 0.5));
            int r = (int) Math.ceil(carveRadius);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double x = from.x + (to.x - from.x) * t, y = from.y + (to.y - from.y) * t, z = from.z + (to.z - from.z) * t;
                for (int dx = -r; dx <= r; dx++)
                    for (int dy = -r; dy <= r; dy++)
                        for (int dz = -r; dz <= r; dz++) {
                            if (dx * dx + dy * dy + dz * dz > carveRadius * carveRadius * 2) continue;
                            int bx = (int) Math.floor(x) + dx, by = (int) Math.floor(y) + dy, bz = (int) Math.floor(z) + dz;
                            long key = (((long) bx & 0x3FFFFFFL) << 38) | (((long) by & 0xFFFL) << 26) | ((long) bz & 0x3FFFFFFL);
                            if (carved.add(key)) cells.add(new int[]{bx, by, bz});
                        }
            }
            if (cells.isEmpty()) return;
            if (terrain == null) terrain = TerrainRestore.open(world, System.currentTimeMillis() + 120_000);
            TerrainRestore.Restore batch = terrain;
            World w = world;
            w.execute(() -> {
                for (int[] c : cells) {
                    try {
                        BlockType type = WorldBlockUtil.getBlockType(w, c[0], c[1], c[2]);
                        if (type == null || TerrainRestore.isEmpty(w, c[0], c[1], c[2])) continue;
                        String id = type.getId();
                        if (TerrainRestore.contains(id, NEVER) || TerrainRestore.isProtectedArea(w.getName(), c[0], c[1], c[2])
                                || TerrainRestore.isKept(w, id, c[0], c[1], c[2]) || WorldBlockUtil.getFluidIndex(w, c[0], c[1], c[2]) > 0) continue;
                        TerrainRestore.place(w, batch, c[0], c[1], c[2], id, TerrainRestore.EMPTY, 0);
                    } catch (Exception ignored) { }
                }
            });
        }

        /** L'arme quitte la main : le porteur passe sur un emplacement vide de sa barre (s'il en a un). */
        private void hideInHand(Store<EntityStore> store) {
            try {
                Player p = store.getComponent(caster, Player.getComponentType());
                var inv = p != null ? p.getInventory() : null;
                if (inv == null || inv.getHotbar() == null) return;
                ItemStack held = inv.getActiveHotbarItem();
                if (held == null || held.isEmpty() || !itemId.equals(held.getItemId())) return;
                short current = inv.getActiveHotbarSlot();
                for (short i = 0; i < 9; i++) {
                    ItemStack it = inv.getHotbar().getItemStack(i);
                    if (i != current && (it == null || it.isEmpty())) { hiddenSlot = current; inv.setActiveHotbarSlot(caster, (byte) i, store); return; }
                }
            } catch (Exception e) {
                EldaniorLogger.error("ArmesDivines: main", e);
            }
        }

        private void showInHand(Store<EntityStore> store) {
            if (hiddenSlot < 0) return;
            int slot = hiddenSlot;
            hiddenSlot = -1;
            try {
                Player p = caster.isValid() ? store.getComponent(caster, Player.getComponentType()) : null;
                var inv = p != null ? p.getInventory() : null;
                if (inv == null || inv.getHotbar() == null) return;
                ItemStack it = inv.getHotbar().getItemStack((short) slot);
                if (it != null && !it.isEmpty() && itemId.equals(it.getItemId())) inv.setActiveHotbarSlot(caster, (byte) slot, store);
            } catch (Exception e) {
                EldaniorLogger.error("ArmesDivines: main", e);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(model, cb);
            if (world != null && terrain != null) world.execute(TerrainRestore::save);
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(model);
            if (world != null && terrain != null) world.execute(TerrainRestore::save);
        }
    }

    private static Rotation3f tumble(Vector3d heading, double angle) {
        Rotation3f rot = new Rotation3f();
        rot.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(heading.x, heading.z)));
        rot.setPitch((float) angle);
        return rot;
    }

    private static Rotation3f flatSpin(Vector3d heading, double angle) {
        Rotation3f rot = new Rotation3f();
        rot.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(heading.x, heading.z) + (float) angle));
        return rot;
    }

    // ===================== Mjollnir : lancer (attaque maintenue) =====================

    public static final class MjollnirLancer implements RuneSpell {
        @Override public String id() { return "MjollnirLancer"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d eye = SpellFx.position(caster, store);
            if (eye == null) return null;
            eye.add(0, 1.5, 0);
            Vector3d look = SpellFx.lookDirection(caster, store);
            SpellFx.sound(SFX_SLASH, eye, cb);
            SpellFx.fx(FX_SPARK, eye, 0.8f, store, cb);
            return new Thrown(caster, ArtefactWeapons.MJOLLNIR, "Projectile_Config_Mjollnir_Vol", new Vector3d(eye).add(new Vector3d(look).mul(1.2)), look,
                    100, 70, 80, 1.6, 1.0, store, cb) {
                @Override Rotation3f rotation(Vector3d heading) { return tumble(heading, spin * 18); }

                @Override
                void hit(Ref<EntityStore> e, Vector3d at, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    SpellFx.damage(caster, e, "Physical", 220f, new Vector3d(dir).mul(8).add(0, 4, 0), "Impact_Strong", c);
                    SpellFx.fx(FX_BOLT, new Vector3d(at).add(0, -1, 0), 1.2f, s, c);
                    SpellFx.fx(FX_SPARK, at, 1.0f, s, c);
                    SpellFx.sound2dNear(SFX_THUNDER, at, 35, s);
                    SpellFx.stun(e, STUN, 800, s, c);
                }
            };
        }
    }

    // ===================== Guandao : lancer (attaque maintenue) =====================

    public static final class GuandaoLancer implements RuneSpell {
        @Override public String id() { return "GuandaoLancer"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d eye = SpellFx.position(caster, store);
            if (eye == null) return null;
            eye.add(0, 1.4, 0);
            Vector3d look = SpellFx.lookDirection(caster, store);
            SpellFx.sound(SFX_SLASH, eye, cb);
            return new Thrown(caster, ArtefactWeapons.GUANDAO, "Projectile_Config_Guandao_Vol", new Vector3d(eye).add(new Vector3d(look).mul(1.2)), look,
                    30, 40, 45, 2.2, 0, store, cb) {
                long nextCut;

                @Override Rotation3f rotation(Vector3d heading) { return flatSpin(heading, spin * 20); }

                @Override
                void hit(Ref<EntityStore> e, Vector3d at, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    SpellFx.damage(caster, e, "Physical", 180f, new Vector3d(dir).mul(5).add(0, 3, 0), "Impact_Strong", c);
                    SpellFx.fx(FX_SLASH, at, 1.2f, s, c);
                    SpellFx.sound(SFX_HIT, at, c);
                }

                @Override
                public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
                    if (now >= nextCut && world != null) {                    // la lame fauche l'herbe sous elle
                        nextCut = now + 80;
                        cutPlants(world, pos, 1.5);
                    }
                    return super.tick(owner, now, store, cb);
                }
            };
        }
    }

    /** Fauche les plantes (herbes, fleurs) autour d'un point : elles repoussent a la restauration. */
    private static void cutPlants(World world, Vector3d at, double radius) {
        int r = (int) Math.ceil(radius);
        int cx = (int) Math.floor(at.x), cy = (int) Math.floor(at.y), cz = (int) Math.floor(at.z);
        TerrainRestore.Restore batch = TerrainRestore.open(world, System.currentTimeMillis() + 60_000);
        world.execute(() -> {
            for (int dx = -r; dx <= r; dx++)
                for (int dz = -r; dz <= r; dz++)
                    for (int dy = -2; dy <= 1; dy++) {
                        int x = cx + dx, y = cy + dy, z = cz + dz;
                        try {
                            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                            if (t == null || !t.getId().startsWith("Plant_") || TerrainRestore.isProtectedArea(world.getName(), x, y, z)
                                    || TerrainRestore.isKept(world, t.getId(), x, y, z)) continue;
                            TerrainRestore.place(world, batch, x, y, z, t.getId(), TerrainRestore.EMPTY, 0);
                        } catch (Exception ignored) { }
                    }
        });
    }

    // =====================================================================================================================
    // Mjollnir : Colere de Thor
    // =====================================================================================================================

    public static final class ColereDeThor implements RuneSpell {
        private static final int BOLTS = 8;
        private static final long FIRST_MS = 450, EVERY_MS = 170;

        @Override public String id() { return "ColereDeThor"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            SpellFx.fx(FX_STORM, new Vector3d(pos).add(0, 16, 0), 4f, store, cb);        // le ciel s'assombrit
            SpellFx.fx(FX_SKY_FLASH, new Vector3d(pos).add(0, 25, 0), 8f, store, cb);
            SpellFx.sound2dNear(SFX_THUNDER, pos, 80, store);
            List<Ref<EntityStore>> targets = nearest(caster, pos, 20, store);
            if (targets.size() > BOLTS) targets = new ArrayList<>(targets.subList(0, BOLTS));
            final List<Ref<EntityStore>> bolts = targets;
            final long finale = FIRST_MS + bolts.size() * EVERY_MS + 200;
            return new Timed(caster, finale + 300) {
                int i;
                boolean done;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    while (i < bolts.size() && t >= FIRST_MS + i * EVERY_MS) {   // la foudre tombe, ennemi apres ennemi
                        Ref<EntityStore> e = bolts.get(i++);
                        Vector3d p = SpellFx.position(e, s);
                        if (p == null) continue;
                        strike(p, s, c);
                        SpellFx.damage(caster, e, "Lightning", 180f, null, "Impact_Strong", c);
                        SpellFx.stun(e, STUN, 1_000, s, c);
                    }
                    if (!done && t >= finale) {                                 // un eclair frappe le marteau leve
                        done = true;
                        Vector3d p = SpellFx.position(caster, s);
                        if (p == null) return;
                        SpellFx.fx(FX_SKY_FLASH, new Vector3d(p).add(0, 25, 0), 10f, s, c);
                        SpellFx.fx(FX_BOLT, p, 2.2f, s, c);
                        SpellFx.fx(FX_SPARK, new Vector3d(p).add(0, 2, 0), 2.0f, s, c);
                        SpellFx.fx(FX_EXPLOSION, p, 1.2f, s, c);
                        ring(FX_SPARK, new Vector3d(p).add(0, 0.5, 0), 4, 12, 0.9f, s, c);
                        ring(FX_DUST, p, 6, 14, 1.0f, s, c);
                        SpellFx.sound2dNear(SFX_THUNDER, p, 80, s);
                        SpellFx.shake(p, 40, "Impact_Strong", s);
                        String rock = surfaceBlock(p);
                        ThreadLocalRandom rng = ThreadLocalRandom.current();
                        for (int k = 0; k < 8; k++) {
                            double a = k * Math.PI / 4 + rng.nextDouble(-0.3, 0.3);
                            throwRock(rock, new Vector3d(p).add(Math.cos(a) * 2, 0.6, Math.sin(a) * 2), new Vector3d(Math.cos(a) * 7, rng.nextDouble(8, 12), Math.sin(a) * 7), s, c);
                        }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, 7.5, s)) {
                            Vector3d ep = SpellFx.position(e, s);
                            if (ep != null) SpellFx.damage(caster, e, "Lightning", 150f, outward(p, ep, fwd, 12, 7), "Impact_Strong", c);
                        }
                    }
                }

                /** Eclair sur un point : flash dans le ciel, eclair, etincelles, sol brule. */
                private void strike(Vector3d p, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    Vector3d g = SpellFx.ground(world, p);
                    SpellFx.fx(FX_SKY_FLASH, new Vector3d(g).add(0, 25, 0), 6f, s, c);
                    SpellFx.fx(FX_BOLT, g, 1.6f, s, c);
                    SpellFx.fx(FX_SPARK, new Vector3d(g).add(0, 1, 0), 1.3f, s, c);
                    SpellFx.fx(FX_FIRE, new Vector3d(g).add(0, 0.3, 0), 0.8f, s, c);
                    SpellFx.sound2dNear(SFX_THUNDER, g, 50, s);
                    int y = (int) Math.floor(g.y) - 1;
                    List<int[]> burnt = new ArrayList<>();
                    for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) if (Math.abs(dx) + Math.abs(dz) < 2)
                        burnt.add(new int[]{(int) Math.floor(g.x) + dx, y, (int) Math.floor(g.z) + dz});
                    setCells(burnt, "Soil_Dirt_Burnt", true, terrain());
                    linger("Smoke_Black", new Vector3d(g).add(0, 0.2, 0), 0.6f, 3_000, 450);        // le sol foudroye fume
                    linger(FX_FIRE, new Vector3d(g).add(0, 0.2, 0), 0.4f, 1_200, 400);
                }
            };
        }
    }

    // =====================================================================================================================
    // Sharur : Ecraseur de Milliers
    // =====================================================================================================================

    public static final class EcraseurDeMilliers implements RuneSpell {
        private static final long PULL_MS = 650;

        @Override public String id() { return "EcraseurDeMilliers"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            SpellFx.sound(SFX_RUMBLE, pos, cb);
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d impact = new Vector3d(pos).add(new Vector3d(fwd).mul(2));
            return new Timed(caster, PULL_MS + 450) {
                long nextPull;
                int wave = -1;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    if (t < PULL_MS) {                                             // tourbillon de poussiere qui aspire
                        if (t < nextPull) return;
                        nextPull = t + 90;
                        double r = 9 - t / 90.0;
                        double turn = t / 120.0;
                        for (int i = 0; i < 8; i++) {
                            double a = turn + i * Math.PI / 4;
                            SpellFx.fx(FX_DUST, new Vector3d(impact).add(Math.cos(a) * r, 0.3 + i % 3 * 0.6, Math.sin(a) * r), 0.8f, s, c);
                        }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, impact, 11, s)) {
                            Vector3d ep = SpellFx.position(e, s);
                            if (ep == null) continue;
                            Vector3d to = new Vector3d(impact.x - ep.x, 0, impact.z - ep.z);
                            if (to.lengthSquared() > 1) SpellFx.push(e, rotateY(to.normalize(), 0.5).mul(9).add(0, 1, 0), false, c);
                        }
                        return;
                    }
                    int w = (int) ((t - PULL_MS) / 90);
                    if (wave < 0) {                                                // impact
                        SpellFx.fx(FX_EXPLOSION, impact, 2.0f, s, c);
                        SpellFx.fx(FX_BURST, new Vector3d(impact).add(0, 1, 0), 2.5f, s, c);
                        SpellFx.sound(SFX_BLAST, impact, c);
                        SpellFx.sound(SFX_STONE, impact, c);
                        SpellFx.sound2dNear(SFX_THUNDER, impact, 60, s);
                        SpellFx.shake(impact, 40, "Impact_Strong", s);
                        crater(impact, s);
                        linger(FX_DUST, impact, 1.4f, 2_500, 400);                         // nuage de poussiere sur le cratere
                        String rock = surfaceBlock(impact);
                        ThreadLocalRandom rng = ThreadLocalRandom.current();
                        for (int k = 0; k < 12; k++) {
                            double a = k * Math.PI / 6 + rng.nextDouble(-0.2, 0.2);
                            throwRock(rock, new Vector3d(impact).add(Math.cos(a) * 2.5, 0.8, Math.sin(a) * 2.5),
                                    new Vector3d(Math.cos(a) * rng.nextDouble(6, 10), rng.nextDouble(9, 14), Math.sin(a) * rng.nextDouble(6, 10)), s, c);
                        }
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, impact, 6, s)) {
                            Vector3d ep = SpellFx.position(e, s);
                            if (ep != null) SpellFx.damage(caster, e, "Physical", 260f, outward(impact, ep, fwd, 8, 10), "Impact_Strong", c);
                        }
                        wave = 0;
                    }
                    while (wave < Math.min(4, w)) {                               // ondes de choc qui s'elargissent
                        wave++;
                        ring(FX_DUST, impact, 2 + wave * 2.2, 10 + wave * 4, 1.0f, s, c);
                    }
                }
            };
        }
    }

    // =====================================================================================================================
    // Arc d'Artemis : Pluie Lunaire (vraies fleches)
    // =====================================================================================================================

    public static final class PluieLunaire implements RuneSpell {
        private static final String CARRIER = "Projectile_Config_Artemis_Fleche";
        private static final int ARROWS = 32;
        private static final double FALL_SPEED = 38, RADIUS = 6;

        @Override public String id() { return "PluieLunaire"; }

        private static final class Arrow {
            final Ref<EntityStore> ref; final Vector3d pos, target, dir; long landedAt;
            Arrow(Ref<EntityStore> ref, Vector3d pos, Vector3d target) {
                this.ref = ref; this.pos = pos; this.target = target; this.dir = new Vector3d(target).sub(pos).normalize();
            }
        }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            World world = SpellFx.world(store);
            Vector3d aim = SpellFx.targetPoint(caster, store, 40, 20);
            if (aim == null || world == null) return null;
            final Vector3d center = SpellFx.ground(world, aim);
            SpellFx.fx(FX_MOON_HIT, new Vector3d(center).add(0, 0.5, 0), 2.0f, store, cb);
            SpellFx.sound(SFX_SLASH, center, cb);
            return new Timed(caster, 3_400) {
                final List<Arrow> arrows = new ArrayList<>();
                int spawned;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    while (spawned < ARROWS && t >= spawned * 60L) {          // volee de fleches qui tombent du ciel
                        spawned++;
                        double a = rng.nextDouble(Math.PI * 2), r = Math.sqrt(rng.nextDouble()) * RADIUS;
                        Vector3d hit = SpellFx.ground(world, new Vector3d(center).add(Math.cos(a) * r, 0, Math.sin(a) * r)).add(0, 0.3, 0);
                        Vector3d sky = new Vector3d(hit).add(rng.nextDouble(-4, 4), 24, rng.nextDouble(-4, 4));
                        Ref<EntityStore> ref = SpellFx.spawnModel(caster, CARRIER, sky, new Vector3d(hit).sub(sky), c);
                        if (ref == null) continue;
                        SpellFx.notSaved(ref, s, c);
                        arrows.add(new Arrow(ref, sky, hit));
                    }
                    long now = startAt + t;
                    double step = FALL_SPEED * 0.05;
                    for (Iterator<Arrow> it = arrows.iterator(); it.hasNext(); ) {
                        Arrow ar = it.next();
                        if (ar.landedAt > 0) {                                   // plantee dans le sol un instant
                            if (now - ar.landedAt > 1_200) { SpellFx.remove(ar.ref, c); it.remove(); }
                            continue;
                        }
                        if (!ar.ref.isValid()) continue;
                        double left = ar.pos.distance(ar.target);
                        if (left <= step * 1.5) {
                            ar.pos.set(ar.target);
                            ar.landedAt = now;
                            SpellFx.fx(FX_MOON_HIT, ar.target, 0.8f, s, c);
                            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, ar.target, 2.5, s)) {
                                Vector3d ep = SpellFx.position(e, s);
                                if (ep != null && SpellFx.horizontalDistance(ep, ar.target) <= 1.6) SpellFx.damage(caster, e, "Physical", 60f, null, "Impact_Light", c);   // arc legendaire : x3,8 au lieu de x5
                            }
                        } else {
                            ar.pos.add(new Vector3d(ar.dir).mul(step * 1.5));
                        }
                        SpellFx.hold(ar.ref, ar.pos, SpellFx.facing(ar.dir, true), s);
                    }
                }

                @Override void onEnd(CommandBuffer<EntityStore> c) { for (Arrow a : arrows) SpellFx.remove(a.ref, c); }
                @Override void onAbort() { for (Arrow a : arrows) RuneSpellRuntime.removeLater(a.ref); }
            };
        }
    }

    // =====================================================================================================================
    // Arc d'Apollon : Fleche du Soleil
    // =====================================================================================================================

    public static final class FlecheDuSoleil implements RuneSpell {
        private static final String CARRIER = "Projectile_Config_Apollon_Fleche", MODEL = "Apollon_Fleche";
        private static final double RANGE = 80, SPEED = 60;

        @Override public String id() { return "FlecheDuSoleil"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d eye = SpellFx.position(caster, store);
            if (eye == null) return null;
            eye.add(0, 1.5, 0);
            final Vector3d dir = SpellFx.lookDirection(caster, store).normalize();
            final Vector3d pos = new Vector3d(eye).add(new Vector3d(dir).mul(1.5));
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, pos, dir, cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            SpellFx.fx(FX_BURST, pos, 1.8f, store, cb);
            SpellFx.sound(SFX_FIRE, pos, cb);
            return new Timed(caster, (long) (RANGE / SPEED * 1000) + 700) {
                final Set<Ref<EntityStore>> hit = new HashSet<>();
                double travelled, nextScorch = 2;
                boolean scaled, exploded;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    if (exploded) return;
                    if (!scaled && model != null && model.isValid()) { SpellFx.setModelScale(model, MODEL, 3.5f, 2.0, c); scaled = true; }
                    double move = Math.min(RANGE - travelled, SPEED * 0.05 * 1.2);
                    pos.add(new Vector3d(dir).mul(move));
                    travelled += move;
                    if (model != null && model.isValid()) SpellFx.hold(model, pos, SpellFx.facing(dir, true), s);
                    if (travelled >= nextScorch) {                                 // sillage de feu, sol brule
                        nextScorch += 2;
                        SpellFx.fx(FX_FIRE, pos, 1.0f, s, c);
                        Vector3d g = SpellFx.ground(world, pos);
                        if (pos.y - g.y < 5) {
                            int y = (int) Math.floor(g.y) - 1;
                            setCells(List.of(new int[]{(int) Math.floor(g.x), y, (int) Math.floor(g.z)}), "Soil_Dirt_Burnt", true, terrain());
                            SpellFx.fx(FX_FIRE, new Vector3d(g).add(0, 0.3, 0), 0.7f, s, c);
                            linger(FX_FIRE, new Vector3d(g).add(0, 0.3, 0), 0.5f, 2_500, 450);   // le sillage brule encore
                        }
                    }
                    for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, 3, s)) {
                        Vector3d cp = chest(e, s);
                        if (cp == null || cp.distance(pos) > 2.2 || !hit.add(e)) continue;
                        SpellFx.damage(caster, e, "Fire", 260f, new Vector3d(dir).mul(6).add(0, 3, 0), "Impact_Strong", c);
                        SpellFx.effect(e, BURN, c);
                        SpellFx.fx(FX_BURST, cp, 1.0f, s, c);
                    }
                    if (travelled >= RANGE) {                                       // un soleil eclate au bout de la course
                        exploded = true;
                        SpellFx.remove(model, c);
                        SpellFx.fx(FX_EXPLOSION, pos, 2.2f, s, c);
                        SpellFx.fx(FX_PILLAR, SpellFx.ground(world, pos), 1.8f, s, c);
                        SpellFx.fx(FX_SUN, pos, 3.0f, s, c);
                        SpellFx.sound(SFX_BLAST, pos, c);
                        SpellFx.sound2dNear(SFX_THUNDER, pos, 70, s);
                        SpellFx.shake(pos, 35, "Impact_Strong", s);
                        crater(pos, s);
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, 6, s)) {
                            Vector3d ep = SpellFx.position(e, s);
                            if (ep == null) continue;
                            SpellFx.damage(caster, e, "Fire", 200f, outward(pos, ep, dir, 9, 8), "Impact_Strong", c);
                            SpellFx.effect(e, BURN, c);
                        }
                    }
                }

                @Override void onEnd(CommandBuffer<EntityStore> c) { SpellFx.remove(model, c); }
                @Override void onAbort() { RuneSpellRuntime.removeLater(model); }
            };
        }
    }

    // =====================================================================================================================
    // Ruyi Jingu Bang : Extension (attaque maintenue) et Baton Celeste
    // =====================================================================================================================

    private static final String RUYI_CARRIER = "Projectile_Config_Ruyi_Geant", RUYI_MODEL = "Ruyi_Geant";
    private static final double RUYI_LENGTH = 2.06;                            // blocs a l'echelle 1

    public static final class RuyiExtension implements RuneSpell {
        private static final float MAX_SCALE = 7f;                             // ~14 blocs
        private static final long GROW_MS = 140, HOLD_MS = 180, BACK_MS = 160;

        @Override public String id() { return "RuyiExtension"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d dir = SpellFx.lookDirection(caster, store).normalize();
            final Vector3d base = new Vector3d(pos).add(0, 1.3, 0).add(new Vector3d(dir).mul(0.6));
            Ref<EntityStore> model = SpellFx.spawnModel(caster, RUYI_CARRIER, new Vector3d(base).add(new Vector3d(dir).mul(1)), dir, cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            SpellFx.sound(SFX_SLASH, base, cb);
            return new Timed(caster, GROW_MS + HOLD_MS + BACK_MS) {
                final Set<Ref<EntityStore>> hit = new HashSet<>();
                float lastScale = -1;
                boolean impact;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    if (model == null || !model.isValid()) return;
                    float k = t < GROW_MS ? t / (float) GROW_MS : t < GROW_MS + HOLD_MS ? 1f : Math.max(0f, 1f - (t - GROW_MS - HOLD_MS) / (float) BACK_MS);
                    float scale = 1f + (MAX_SCALE - 1f) * k;
                    if (Math.abs(scale - lastScale) > 0.35f) { SpellFx.setModelScale(model, RUYI_MODEL, scale, 1.0, c); lastScale = scale; }
                    double length = RUYI_LENGTH * scale;
                    SpellFx.hold(model, new Vector3d(base).add(new Vector3d(dir).mul(length / 2)), SpellFx.facing(dir, true), s);
                    Vector3d tip = new Vector3d(base).add(new Vector3d(dir).mul(length));
                    if (k >= 1f && !impact) {                                      // le bout frappe : poussiere, roche, onde
                        impact = true;
                        SpellFx.fx(FX_DUST, tip, 1.5f, s, c);
                        for (int i = 1; i <= 3; i++) SpellFx.fx(FX_BURST, new Vector3d(base).add(new Vector3d(dir).mul(length * i / 4)), 0.5f, s, c);
                        SpellFx.shake(tip, 15, "Impact_Light", s);
                        BlockType at = WorldBlockUtil.getBlockType(world, (int) Math.floor(tip.x), (int) Math.floor(tip.y), (int) Math.floor(tip.z));
                        Vector3d g = SpellFx.ground(world, tip);
                        if ((at != null && !TerrainRestore.isEmpty(world, (int) Math.floor(tip.x), (int) Math.floor(tip.y), (int) Math.floor(tip.z))) || tip.y - g.y < 1.2) {
                            SpellFx.fx(FX_ROCK, tip, 1.3f, s, c);
                            SpellFx.sound(SFX_STONE, tip, c);
                            crack(g, 1, true, new Vector3d(-dir.z, 0, dir.x));
                            String rock = surfaceBlock(tip);
                            ThreadLocalRandom rng = ThreadLocalRandom.current();
                            for (int i = 0; i < 3; i++)
                                throwRock(rock, new Vector3d(tip).add(0, 0.5, 0), new Vector3d(dir).mul(-rng.nextDouble(2, 5)).add(rng.nextDouble(-3, 3), rng.nextDouble(6, 10), rng.nextDouble(-3, 3)), s, c);
                            linger(FX_DUST, g, 0.8f, 1_500, 400);
                        } else {
                            SpellFx.sound(SFX_HIT, tip, c);
                        }
                    }
                    for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, new Vector3d(base).add(new Vector3d(dir).mul(length / 2)), length / 2 + 2, s)) {
                        Vector3d cp = chest(e, s);
                        if (cp == null || hit.contains(e)) continue;
                        Vector3d rel = new Vector3d(cp).sub(base);
                        double along = rel.dot(dir);
                        if (along < 0 || along > length || new Vector3d(rel).sub(new Vector3d(dir).mul(along)).length() > 1.8) continue;
                        hit.add(e);
                        SpellFx.damage(caster, e, "Physical", 200f, new Vector3d(dir).mul(14).add(0, 5, 0), "Impact_Strong", c);
                        SpellFx.fx(FX_BURST, cp, 0.8f, s, c);
                        SpellFx.sound(SFX_HIT, cp, c);
                    }
                }

                @Override void onEnd(CommandBuffer<EntityStore> c) { SpellFx.remove(model, c); }
                @Override void onAbort() { RuneSpellRuntime.removeLater(model); }
            };
        }
    }

    public static final class BatonCeleste implements RuneSpell {
        private static final float MAX_SCALE = 12f;                            // ~24 blocs
        private static final long GROW_MS = 450, FALL_MS = 260, REST_MS = 300, SHRINK_MS = 400;
        private static final long END_MS = GROW_MS + FALL_MS + REST_MS + SHRINK_MS;

        @Override public String id() { return "BatonCeleste"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d side = new Vector3d(-fwd.z, 0, fwd.x);
            final Vector3d base = new Vector3d(pos).add(new Vector3d(fwd).mul(1.0)).add(0, 0.5, 0);
            Ref<EntityStore> model = SpellFx.spawnModel(caster, RUYI_CARRIER, new Vector3d(base).add(0, 2, 0), new Vector3d(0, 1, 0), cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            SpellFx.sound(SFX_RUMBLE, base, cb);
            SpellFx.fx(FX_DUST, base, 1.5f, store, cb);
            return new Timed(caster, END_MS) {
                long nextScale, nextCloud;
                float scale = 1f;
                boolean slammed;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    if (model == null || !model.isValid()) return;
                    long shrinkAt = GROW_MS + FALL_MS + REST_MS;
                    if (t >= nextScale && (t <= GROW_MS || t >= shrinkAt)) {         // grandit vers le ciel, puis reprend sa taille
                        nextScale = t + 70;
                        scale = t <= GROW_MS ? 1f + (MAX_SCALE - 1f) * Math.min(1f, t / (float) GROW_MS)
                                : Math.max(1f, MAX_SCALE - (MAX_SCALE - 1f) * (t - shrinkAt) / (float) SHRINK_MS);
                        SpellFx.setModelScale(model, RUYI_MODEL, scale, 1.0, c);
                    }
                    double angle = t <= GROW_MS ? 0 : Math.min(1.0, (t - GROW_MS) / (double) FALL_MS);
                    angle = Math.pow(angle, 1.6) * Math.PI / 2;                     // la chute accelere
                    Vector3d axis = new Vector3d(0, 1, 0).mul(Math.cos(angle)).add(new Vector3d(fwd).mul(Math.sin(angle))).normalize();
                    double half = RUYI_LENGTH * scale / 2;
                    SpellFx.hold(model, new Vector3d(base).add(new Vector3d(axis).mul(half)), SpellFx.facing(axis, true), s);
                    if (t <= GROW_MS && t >= nextCloud) {                            // il perce les nuages
                        nextCloud = t + 120;
                        Vector3d top = new Vector3d(base).add(new Vector3d(axis).mul(half * 2));
                        SpellFx.fx(FX_SMOKE, top, 2.2f, s, c);
                        SpellFx.fx(FX_SMOKE, new Vector3d(top).add(1.5, -0.5, 1.0), 1.6f, s, c);
                    }
                    if (!slammed && t >= GROW_MS + FALL_MS) {
                        slammed = true;
                        slam(s, c, half * 2);
                    }
                }

                private void slam(Store<EntityStore> s, CommandBuffer<EntityStore> c, double length) {
                    Set<Ref<EntityStore>> hit = new HashSet<>();
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    for (double d = 1.5; d <= length; d += 1.0) {                 // le sol se fend sur toute la longueur
                        Vector3d at = new Vector3d(base).add(new Vector3d(fwd).mul(d));
                        crack(at, 2, ((int) d) % 3 == 0, side);
                        if (((int) d) % 2 == 0) {
                            SpellFx.fx(FX_DUST, new Vector3d(at).add(new Vector3d(side).mul(2.5)), 1.0f, s, c);   // onde de part et d'autre
                            SpellFx.fx(FX_DUST, new Vector3d(at).add(new Vector3d(side).mul(-2.5)), 1.0f, s, c);
                            double sgn = rng.nextBoolean() ? 1 : -1;
                            throwRock(surfaceBlock(at), new Vector3d(at).add(0, 0.6, 0), new Vector3d(side).mul(sgn * rng.nextDouble(4, 8)).add(0, rng.nextDouble(8, 12), 0), s, c);
                        }
                        if (((int) d) % 4 == 0) linger(FX_DUST, at, 0.9f, 2_000, 450);
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, at, 3.5, s)) {
                            Vector3d ep = SpellFx.position(e, s);
                            if (ep == null || !hit.add(e) || SpellFx.horizontalDistance(ep, at) > 2.8) continue;
                            SpellFx.damage(caster, e, "Physical", 280f, new Vector3d(ep).sub(at).mul(0).add(new Vector3d(side).mul(new Vector3d(ep).sub(at).dot(side) >= 0 ? 6 : -6)).add(0, 9, 0), "Impact_Strong", c);
                            SpellFx.stun(e, STUN, 1_200, s, c);
                        }
                    }
                    Vector3d end = new Vector3d(base).add(new Vector3d(fwd).mul(length));
                    SpellFx.fx(FX_EXPLOSION, end, 1.5f, s, c);
                    SpellFx.shake(base, 45, "Impact_Strong", s);
                    SpellFx.sound2dNear(SFX_THUNDER, base, 70, s);
                    SpellFx.sound(SFX_STONE, end, c);
                    crater(end, s);
                }

                @Override void onEnd(CommandBuffer<EntityStore> c) { SpellFx.remove(model, c); }
                @Override void onAbort() { RuneSpellRuntime.removeLater(model); }
            };
        }
    }

    // =====================================================================================================================
    // Gourdin d'Heracles : Coup d'Heracles
    // =====================================================================================================================

    public static final class CoupDHeracles implements RuneSpell {
        private static final int CRACKS = 5, LENGTH = 9;

        @Override public String id() { return "CoupDHeracles"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d impact = new Vector3d(pos).add(new Vector3d(fwd).mul(2.5));
            SpellFx.fx(FX_EXPLOSION, impact, 1.8f, store, cb);
            SpellFx.fx(FX_DUST, impact, 2.2f, store, cb);
            SpellFx.sound(SFX_BLAST, impact, cb);
            SpellFx.sound(SFX_STONE, impact, cb);
            SpellFx.sound2dNear(SFX_THUNDER, impact, 60, store);
            SpellFx.shake(impact, 40, "Impact_Strong", store);
            crater(impact, store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, 11, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d to = new Vector3d(p.x - pos.x, 0, p.z - pos.z);
                if (to.length() > 10 || (to.length() > 1 && to.angle(fwd) > Math.toRadians(40))) continue;
                SpellFx.damage(caster, e, "Physical", 300f, new Vector3d(to).normalize().mul(20).add(0, 12, 0), "Impact_Strong", cb);
                SpellFx.stun(e, STUN, 1_000, store, cb);
            }
            return new Timed(caster, LENGTH * 40L + 200) {
                int k;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    while (k < LENGTH && t >= k * 40L) {                           // failles en eventail
                        k++;
                        for (int i = 0; i < CRACKS; i++) {
                            double a = Math.toRadians(-36 + 18 * i);
                            Vector3d d = rotateY(fwd, a);
                            Vector3d at = new Vector3d(impact).add(new Vector3d(d).mul(k + 1));
                            crack(at, Math.max(1, 3 - k / 4), false, new Vector3d(-d.z, 0, d.x));
                            if (k % 3 == 0) { SpellFx.fx(FX_DUST, at, 0.9f, s, c); linger(FX_DUST, at, 0.7f, 1_800, 450); }
                            if (k == 2 || k == 5) {
                                throwRock(surfaceBlock(at), new Vector3d(at).add(0, 0.6, 0),
                                        new Vector3d(d).mul(rng.nextDouble(7, 12)).add(0, rng.nextDouble(7, 11), 0), s, c);
                            }
                        }
                    }
                }
            };
        }
    }

    // =====================================================================================================================
    // Massue du Dagda : Chaudron du Dagda
    // =====================================================================================================================

    public static final class ChaudronDuDagda implements RuneSpell {
        private static final long REGEN_MS = 6_000, REGEN_EVERY = 500;

        @Override public String id() { return "ChaudronDuDagda"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            final Vector3d center = new Vector3d(pos);
            SpellFx.fx(FX_EXPLOSION, pos, 1.2f, store, cb);
            SpellFx.fx(FX_DUST, pos, 2.0f, store, cb);
            SpellFx.sound(SFX_BLAST, pos, cb);
            SpellFx.shake(pos, 25, "Impact_Strong", store);
            Timed cauldron = new Timed(caster, REGEN_MS) {
                final Set<Ref<EntityStore>> struck = new HashSet<>();
                int wave;
                long nextRegen;
                boolean bloomed;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    while (wave < 5 && t >= wave * 80L) {                          // l'onde du bout qui tue
                        wave++;
                        double r = wave * 1.8;
                        ring(FX_DUST, center, r, 8 + wave * 3, 0.9f, s, c);
                        ring(FX_HEAL, new Vector3d(center).add(0, 0.2, 0), r, 6 + wave * 2, 0.6f, s, c);
                        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, r + 1, s)) {
                            Vector3d p = SpellFx.position(e, s);
                            if (p == null || SpellFx.horizontalDistance(p, center) > r || !struck.add(e)) continue;
                            SpellFx.damage(caster, e, "Physical", 200f, outward(center, p, fwd, 8, 5), "Impact_Strong", c);
                        }
                    }
                    if (!bloomed) {                                                // la terre fleurit (le bout qui ranime)
                        bloomed = true;
                        bloom(s);
                    }
                    if (t >= nextRegen) {                                          // regeneration des allies
                        nextRegen = t + REGEN_EVERY;
                        for (Ref<EntityStore> a : SpellFx.alliesAround(caster, center, 10, s)) {
                            SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), t == 0 ? 0.10f : 0.03f, c);
                            Vector3d p = SpellFx.position(a, s);
                            if (p != null) SpellFx.fx(FX_HEAL, p, 0.6f, s, c);
                        }
                    }
                }

                private void bloom(Store<EntityStore> s) {
                    ThreadLocalRandom rng = ThreadLocalRandom.current();
                    List<int[]> cells = new ArrayList<>();
                    for (int i = 0; i < 40; i++) {
                        double a = rng.nextDouble(Math.PI * 2), r = 1.5 + Math.sqrt(rng.nextDouble()) * 6;
                        Vector3d g = SpellFx.ground(world, new Vector3d(center).add(Math.cos(a) * r, 0, Math.sin(a) * r));
                        int x = (int) Math.floor(g.x), y = (int) Math.floor(g.y), z = (int) Math.floor(g.z);
                        BlockType below = WorldBlockUtil.getBlockType(world, x, y - 1, z);
                        if (below == null || !below.getId().startsWith("Soil_Grass")) continue;
                        cells.add(new int[]{x, y, z});
                    }
                    if (cells.isEmpty()) return;
                    TerrainRestore.Restore batch = terrain();
                    World w = world;
                    w.execute(() -> {
                        for (int[] c : cells) {
                            try {
                                if (!TerrainRestore.isEmpty(w, c[0], c[1], c[2]) || TerrainRestore.isProtectedArea(w.getName(), c[0], c[1], c[2])) continue;
                                TerrainRestore.place(w, batch, c[0], c[1], c[2], TerrainRestore.EMPTY, FLOWERS[ThreadLocalRandom.current().nextInt(FLOWERS.length)], 0);
                            } catch (Exception ignored) { }
                        }
                    });
                }
            };
            cauldron.restoreMs = 30_000;                                           // les fleurs fanent apres 30 s
            return cauldron;
        }
    }

    // =====================================================================================================================
    // Carnwennan : Voile de l'Ombre
    // =====================================================================================================================

    public static final class VoileDeLOmbre implements RuneSpell {
        private static final long HOP_MS = 220;

        @Override public String id() { return "VoileDeLOmbre"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            List<Ref<EntityStore>> foes = nearest(caster, pos, 16, store);
            if (foes.isEmpty()) return null;
            final List<Ref<EntityStore>> chain = foes.size() > 5 ? new ArrayList<>(foes.subList(0, 5)) : foes;
            final Vector3d origin = new Vector3d(pos);
            final Vector3d originLook = flatForward(caster, store);
            SpellFx.fx(FX_SMOKE, new Vector3d(pos).add(0, 1, 0), 2.0f, store, cb);   // il se fond dans l'ombre
            SpellFx.fx("Smoke_Black", new Vector3d(pos).add(0, 0.5, 0), 1.5f, store, cb);
            SpellFx.protectFromFall(caster, 4_000, store);
            return new Timed(caster, chain.size() * HOP_MS + 300) {
                int i;
                boolean back;
                Vector3d at = new Vector3d(origin);

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    if (i < chain.size() && t >= i * HOP_MS) {
                        Ref<EntityStore> e = chain.get(i++);
                        Vector3d p = SpellFx.position(e, s);
                        if (p == null) return;
                        Vector3d look = SpellFx.forward(e, s);
                        look.y = 0;
                        if (look.lengthSquared() < 1e-4) look.set(new Vector3d(p).sub(at).normalize());
                        look.y = 0;
                        look.normalize();
                        Vector3d behind = new Vector3d(p).sub(new Vector3d(look).mul(1.3));
                        SpellFx.arc(FX_SMOKE, new Vector3d(at).add(0, 1, 0), new Vector3d(behind).add(0, 1, 0), 0.6f, s, c);   // trainee d'ombre
                        SpellFx.teleport(caster, behind, SpellFx.facing(new Vector3d(look), false), s, c);
                        SpellFx.fx("Smoke_Black", new Vector3d(behind).add(0, 0.5, 0), 1.0f, s, c);
                        SpellFx.fx("Impact_Daggers_Signature_Slash", new Vector3d(p).add(0, 1, 0), 1.3f, s, c);
                        SpellFx.sound(SFX_HIT, p, c);
                        SpellFx.damage(caster, e, "Physical", 170f, new Vector3d(look).mul(3).add(0, 2, 0), "Impact_Strong", c);
                        SpellFx.effect(e, BLIND, c);
                        at = behind;
                    } else if (!back && i >= chain.size() && t >= chain.size() * HOP_MS + 150) {
                        back = true;                                               // il reparait d'ou il est parti
                        SpellFx.arc(FX_SMOKE, new Vector3d(at).add(0, 1, 0), new Vector3d(origin).add(0, 1, 0), 0.6f, s, c);
                        SpellFx.teleport(caster, origin, SpellFx.facing(originLook, false), s, c);
                        SpellFx.fx(FX_SMOKE, new Vector3d(origin).add(0, 1, 0), 1.6f, s, c);
                    }
                }
            };
        }
    }

    // =====================================================================================================================
    // Guandao : Croissant du Dragon
    // =====================================================================================================================

    public static final class CroissantDuDragon implements RuneSpell {
        private static final String CARRIER = "Projectile_Config_Guandao_Croissant", MODEL = "Guandao_Croissant";
        private static final double SPEED = 30, RANGE = 26, HIT = 2.6;

        @Override public String id() { return "CroissantDuDragon"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return null;
            final Vector3d fwd = flatForward(caster, store);
            Vector3d mid = new Vector3d(pos).add(0, 1, 0);
            for (int i = 0; i < 10; i++) {                                         // grand moulinet
                double a = i * Math.PI / 5;
                SpellFx.fx(FX_SLASH, new Vector3d(mid).add(Math.cos(a) * 3.5, 0, Math.sin(a) * 3.5), (float) Math.toDegrees(a) + 90f, 0f, 1.5f, store, cb);
            }
            SpellFx.sound(SFX_SLASH, mid, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, 6.5, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p != null && SpellFx.horizontalDistance(p, pos) <= 6) SpellFx.damage(caster, e, "Physical", 180f, outward(pos, p, fwd, 10, 4), "Impact_Strong", cb);
            }
            World world = SpellFx.world(store);
            if (world != null) cutPlants(world, pos, 5);
            final Vector3d wave = new Vector3d(pos).add(0, 0.9, 0).add(new Vector3d(fwd).mul(1.5));
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, wave, fwd, cb);
            if (model != null) SpellFx.notSaved(model, store, cb);
            return new Timed(caster, (long) (RANGE / SPEED * 1000) + 250) {
                final Set<Ref<EntityStore>> hit = new HashSet<>();
                double travelled, nextScar;
                boolean scaled;

                @Override
                void step(long t, Store<EntityStore> s, CommandBuffer<EntityStore> c) {
                    if (model == null || !model.isValid() || travelled >= RANGE) return;
                    if (!scaled) { SpellFx.setModelScale(model, MODEL, 3.0f, 3.6, c); scaled = true; }
                    double move = Math.min(RANGE - travelled, SPEED * 0.06);
                    wave.add(new Vector3d(fwd).mul(move));
                    travelled += move;
                    SpellFx.hold(model, wave, SpellFx.facing(fwd, false), s);
                    if (travelled >= nextScar) {                                    // il fauche l'herbe et entaille le sol
                        nextScar = travelled + 1.0;
                        cutPlants(world, wave, 1.6);
                        Vector3d g = SpellFx.ground(world, wave);
                        if (wave.y - g.y < 3) {
                            int y = (int) Math.floor(g.y) - 1;
                            BlockType bt = WorldBlockUtil.getBlockType(world, (int) Math.floor(g.x), y, (int) Math.floor(g.z));
                            if (bt != null && bt.getId().contains("Grass"))
                                setCells(List.of(new int[]{(int) Math.floor(g.x), y, (int) Math.floor(g.z)}), "Soil_Dirt", true, terrain());
                            SpellFx.fx(FX_DIRT, new Vector3d(g).add(0, 0.3, 0), 0.7f, s, c);
                        }
                    }
                    for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, wave, HIT + 1, s)) {
                        Vector3d p = SpellFx.position(e, s);
                        if (p == null || !hit.add(e)) continue;
                        SpellFx.damage(caster, e, "Physical", 200f, new Vector3d(fwd).mul(8).add(0, 4, 0), "Impact_Strong", c);
                        SpellFx.fx(FX_BURST, new Vector3d(p).add(0, 1, 0), 1.0f, s, c);
                    }
                }

                @Override void onEnd(CommandBuffer<EntityStore> c) { SpellFx.remove(model, c); }
                @Override void onAbort() { RuneSpellRuntime.removeLater(model); }
            };
        }
    }
}
