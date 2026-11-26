package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.BlockSoundEvent;
import com.hypixel.hytale.server.core.asset.type.blockparticle.config.BlockParticleSet;
import com.hypixel.hytale.server.core.asset.type.blocksound.config.BlockSoundSet;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Colts de cavalerie akimbo (Artefact_Handgun_Aigles_Desert, ancien nom « Aigles du Desert ») : deux Colt Single Action Army
 * 1873 a canon cavalerie, obtenus SEULEMENT par la transformation du mousquet legendaire Green Colt (ArmeTransfo, 5 min).
 * Chargeurs de 5 + 5 Balles Divines (Mousquet.coltTrigger, interaction Eldanior_MousquetTir "Colt"), rechargement 0,6 s,
 * portee 40 blocs, degats x1,5 et augmentes par la Force (x1 + FOR / 1 000). Modele, balle, cartouche, animations et
 * interactions : scripts/artefacts/gen_aigles_akimbo.py.
 *
 * La balle est un traceur (modele Balle_Divine porte par Projectile_Config_Balle_Divine) deplace par le code : elle part de
 * la bouche du pistolet qui tire (flash, fumee et douille ejectee : RifleShooting vanilla, oriente sur le tir), file vers
 * le point vise, traverse des ennemis (impact vanilla de fusil, +50 % contre les morts-vivants et le Neant) et s'arrete au
 * premier bloc plein, qui vole en eclats (particules et son de casse de SA matiere) et revient une minute plus tard
 * (TerrainRestore : jamais en zone protegee, ni coffres, portes, socle, fluides).
 *   - AiglesBalleDroite / AiglesBalleGauche (clic, tirs alternes) : balle normale ; une sur sept est une Balle de
 *     Jugement : une colonne de lumiere tombe du ciel la ou elle frappe. Chaque balle au but charge la signature ;
 *   - AiglesDoubleTir (4e clic de la Cadence de l'Aigle) : une balle par pistolet, ensemble ;
 *   - AiglesPerforante (maintien) : balle lourde qui traverse tous les ennemis et jusqu'a 4 blocs (brises) ;
 *   - AiglesBoutPortant (3e temps du gun-kata, apres deux coups de crosse JSON) : double tir en cone, projette ;
 *   - Rythme du Pistolero (rhythmBonus / onRhythmHit, appeles par ArtefactWeapons) : +6 % par touche enchainee ;
 *   - AiglesRafaleDroite / AiglesRafaleGauche (signature) : balles qui cherchent l'ennemi le plus proche du regard ;
 *   - AiglesJugement (signature, fin) : balle geante ; elle creuse le terrain a l'impact, grande colonne de lumiere,
 *     degats de zone, aveuglement.
 */
public final class AiglesDuDesert {

    private AiglesDuDesert() {}

    private static final String CARRIER = "Projectile_Config_Balle_Divine", MODEL = "Balle_Divine", BEAM = "Ra_Rayon";
    // Salves courtes seulement (Apollo_Light_Rain et Apollo_Sacred_Ground durent 20 s : a eviter sur un impact de balle)
    private static final String FX_MUZZLE = "RifleShooting", FX_IMPACT = "RifleShooting_Impact", FX_DEBRIS = "Gun_Impact";
    private static final String FX_GLINT = "Gilgamesh_Sword_Burst", FX_AURA = "Gilgamesh_King_Aura", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String FX_BREAK_DEFAULT = "Block_Break_Stone";
    private static final String SFX_FLESH = "SFX_Blunderbuss_Hit", SFX_WALL = "SFX_Blunderbuss_Miss", SFX_THUNDER = "SFX_Global_Weather_Thunder";
    private static final String BLIND = "Elda_Aveuglement";
    private static final String[] NEVER = {"Bedrock", "Barrier"};
    private static final int JUDGEMENT_EVERY = 7;
    private static final long RESTORE_MS = 60_000, SAVE_EVERY_MS = 3_000;
    private static final float DARK_BONUS = 1.5f, SIGNATURE_PER_HIT = 1f;   // energie max de l'arme : 20 -> 20 balles au but

    /** Nombre de balles tirees par porteur (une sur sept est une Balle de Jugement). */
    private static final Map<Ref<EntityStore>, Integer> SHOTS = Collections.synchronizedMap(new WeakHashMap<>());
    private static volatile long lastSave;

    /**
     * Reglages d'une balle : vitesse et portee (blocs), rayon de touche, degats, ennemis traverses, echelle du modele,
     * recherche de cible, colonne de lumiere (rayon, degats ; 0 = aucune), aveuglement, charge de la signature, rayon du
     * trou creuse a l'impact (0 = seulement le bloc touche), blocs perces avant de s'arreter.
     */
    private record Shot(double speed, double range, double hitRadius, float damage, int pierce, float scale, boolean seek,
                        double pillarRadius, float pillarDamage, boolean blind, boolean charges, double craterRadius, int blockPierce) {}

    private static final Shot NORMAL = new Shot(110, 40, 0.7, 55f, 2, 1.0f, false, 0, 0, false, true, 0, 0);
    private static final Shot JUDGEMENT = new Shot(110, 40, 0.9, 55f, 2, 1.6f, false, 2.5, 150f, false, true, 0, 0);
    private static final Shot DOUBLE = new Shot(110, 40, 0.8, 75f, 2, 1.15f, false, 0, 0, false, true, 0, 0);
    private static final Shot PERFORANTE = new Shot(140, 40, 1.0, 180f, 99, 1.8f, false, 0, 0, false, true, 0, 4);
    private static final Shot RAFALE = new Shot(110, 40, 1.0, 80f, 1, 1.1f, true, 0, 0, false, false, 0, 0);
    private static final Shot FINALE = new Shot(70, 40, 1.6, 250f, 99, 3.0f, true, 5.5, 500f, true, false, 2.5, 0);

    // ===== Rythme du Pistolero : chaque touche dans la fenetre ajoute +6 % de degats (10 max) =====
    private static final long RHYTHM_WINDOW_MS = 2_500, RHYTHM_STEP_MS = 120;
    private static final int RHYTHM_MAX = 10;
    private static final float RHYTHM_PER_STACK = 0.06f;
    /** Par porteur : {cumuls, derniere touche, dernier cumul} (ms). */
    private static final Map<Ref<EntityStore>, long[]> RHYTHM = Collections.synchronizedMap(new WeakHashMap<>());

    /** Colts : degats x1,5, et la Force les augmente (x1 + FOR / 1 000, FOR plafonnee a 3 000). */
    static float coltBoost(Ref<EntityStore> caster, Store<EntityStore> store) {
        return 1.5f * (1f + Mousquet.strength(caster, store) / 1000f);
    }

    /** Explosion qui creuse le terrain (restaure apres une minute) : utilisee aussi par l'obus du canon de char. */
    static void blast(World world, Vector3d at, double radius, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Bullet.crater(world, at, radius, store, cb);
    }

    /** Multiplicateur de degats du rythme (ArtefactWeapons.modifyDamage, arme tenue). */
    public static float rhythmBonus(Ref<EntityStore> attacker) {
        long[] r = RHYTHM.get(attacker);
        return r == null || System.currentTimeMillis() - r[1] > RHYTHM_WINDOW_MS ? 1f : 1f + RHYTHM_PER_STACK * r[0];
    }

    /** Une touche (balle, crosse, bout portant, colonne) : le rythme monte ; eclat dore a 5 cumuls, aura au maximum. */
    public static void onRhythmHit(Ref<EntityStore> attacker, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        long now = System.currentTimeMillis();
        long[] r = RHYTHM.computeIfAbsent(attacker, k -> new long[3]);
        if (now - r[1] > RHYTHM_WINDOW_MS) r[0] = 0;
        r[1] = now;
        if (now - r[2] < RHYTHM_STEP_MS || r[0] >= RHYTHM_MAX) return;      // une colonne qui touche 8 ennemis ne compte qu'une fois
        r[2] = now;
        r[0]++;
        if (r[0] != 5 && r[0] != RHYTHM_MAX) return;
        Vector3d p = SpellFx.position(attacker, store);
        if (p == null) return;
        p.add(0, 1.1, 0);
        SpellFx.fx(FX_GLINT, p, r[0] == RHYTHM_MAX ? 0.9f : 0.5f, store, cb);
        if (r[0] == RHYTHM_MAX) SpellFx.fx(FX_AURA, p, 0.8f, store, cb);
    }

    /** 4e temps de la Cadence de l'Aigle : une balle par pistolet, en meme temps. */
    public static final class DoubleTir implements RuneSpell {
        @Override public String id() { return "AiglesDoubleTir"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            return Volley.of(Bullet.fire(caster, 1, DOUBLE, store, cb), Bullet.fire(caster, -1, DOUBLE, store, cb));
        }
    }

    /** Attaque maintenue : les deux canons tirent une balle lourde qui traverse tout (ennemis et 4 blocs). */
    public static final class Perforante implements RuneSpell {
        @Override public String id() { return "AiglesPerforante"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Bullet.muzzle(caster, -1, store, cb);
            Vector3d p = SpellFx.position(caster, store);
            if (p != null) SpellFx.shake(p, 3, "Impact_Strong", store);                 // recul lourd, pour le tireur
            return Bullet.fire(caster, 1, PERFORANTE, store, cb);
        }
    }

    /** 3e temps du gun-kata : double tir a bout portant dans un cone devant le tireur, qui projette les ennemis. */
    public static final class BoutPortant implements RuneSpell {
        private static final double RANGE = 4.5, COS = Math.cos(Math.toRadians(55));
        private static final float DAMAGE = 160f;

        @Override public String id() { return "AiglesBoutPortant"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = SpellFx.position(caster, store);
            if (at == null) return null;
            Vector3d look = SpellFx.lookDirection(caster, store).normalize();
            Vector3d eye = new Vector3d(at).add(0, 1.5, 0);
            Bullet.muzzle(caster, 1, store, cb);
            Bullet.muzzle(caster, -1, store, cb);
            SpellFx.sound(SFX_FLESH, eye, cb);
            Vector3d flat = new Vector3d(look.x, 0, look.z);
            if (flat.lengthSquared() < 1e-4) flat.set(1, 0, 0);
            flat.normalize();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, eye, RANGE + 1.5, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d to = new Vector3d(p).add(0, 1.0, 0).sub(eye);
                double d = to.length();
                if (d > RANGE || (d > 0.8 && to.dot(look) / d < COS)) continue;
                float damage = DAMAGE * coltBoost(caster, store) * (ArtefactWeapons.isDark(e, store) ? DARK_BONUS : 1f);
                SpellFx.damage(caster, e, "Physical", damage, new Vector3d(flat).mul(13).add(0, 6, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_IMPACT, new Vector3d(p).add(0, 1, 0), 1.4f, store, cb);
                SpellFx.fx(FX_GLINT, new Vector3d(p).add(0, 1, 0), 0.5f, store, cb);
            }
            return null;
        }
    }

    /** Plusieurs balles tirees ensemble, suivies comme un seul sort. */
    private static final class Volley implements RuneSpell.Instance {
        private final List<Bullet> bullets = new ArrayList<>();

        static Volley of(Bullet... list) {
            Volley v = new Volley();
            for (Bullet b : list) if (b != null) v.bullets.add(b);
            return v.bullets.isEmpty() ? null : v;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            bullets.removeIf(b -> {
                if (!b.tick(caster, now, store, cb)) return false;
                b.cleanup(cb);
                return true;
            });
            return bullets.isEmpty();
        }

        @Override public void cleanup(CommandBuffer<EntityStore> cb) { for (Bullet b : bullets) b.cleanup(cb); }
        @Override public void abort() { for (Bullet b : bullets) b.abort(); }
    }

    /** Tir d'un pistolet (side = 1 droite, -1 gauche). */
    public static final class Balle implements RuneSpell {
        private final String id;
        private final int side;
        private final boolean rafale;

        public Balle(String id, int side, boolean rafale) { this.id = id; this.side = side; this.rafale = rafale; }

        @Override public String id() { return id; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Shot shot = rafale ? RAFALE : SHOTS.merge(caster, 1, Integer::sum) % JUDGEMENT_EVERY == 0 ? JUDGEMENT : NORMAL;
            return Bullet.fire(caster, side, shot, store, cb);
        }
    }

    /** Fin de la signature : les deux pistolets tirent ensemble la balle de jugement. */
    public static final class Jugement implements RuneSpell {
        @Override public String id() { return "AiglesJugement"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Bullet.muzzle(caster, -1, store, cb);                                     // les deux canons crachent ensemble
            return Bullet.fire(caster, 1, FINALE, store, cb);
        }
    }

    private static final class Bullet implements RuneSpell.Instance {
        private static final double STEP = 0.4, SEEK_RANGE = 30, SEEK_COS = Math.cos(Math.toRadians(35));
        private static final long MAX_MS = 2_500;

        final Ref<EntityStore> owner;                                          // tireur (degats des colonnes)
        final Shot shot;
        final Vector3d pos, dir;
        final Ref<EntityStore> model;
        final long startAt;
        final Set<Ref<EntityStore>> hit = new HashSet<>();
        long last, lastCell = Long.MIN_VALUE;
        double travelled;
        int blocksBroken;
        boolean scaled, judged;

        private Bullet(Ref<EntityStore> owner, Shot shot, Vector3d pos, Vector3d dir, Ref<EntityStore> model, long startAt) {
            this.owner = owner; this.shot = shot; this.pos = pos; this.dir = dir; this.model = model; this.startAt = startAt; this.last = startAt;
        }

        /** Bouche du pistolet (side = 1 droite, -1 gauche), un peu devant et sous l'oeil. */
        private static Vector3d muzzlePos(Vector3d eye, Vector3d look, int side) {
            Vector3d right = new Vector3d(-look.z, 0, look.x);
            if (right.lengthSquared() < 1e-4) right.set(1, 0, 0);
            right.normalize();
            return new Vector3d(eye).add(new Vector3d(look).mul(0.9)).add(right.mul(0.38 * side)).add(0, -0.25, 0);
        }

        /** Flash, fumee et douille ejectee (RifleShooting vanilla : son axe +Y est tourne sur la direction du tir). */
        static void muzzle(Ref<EntityStore> caster, int side, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = SpellFx.position(caster, store);
            if (at == null) return;
            Vector3d look = SpellFx.lookDirection(caster, store).normalize();
            flash(muzzlePos(new Vector3d(at).add(0, 1.5, 0), look, side), look, 1f, store, cb);
        }

        private static void flash(Vector3d at, Vector3d dir, float scale, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            float yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
            float pitch = (float) Math.toDegrees(Math.asin(Math.max(-1, Math.min(1, dir.y))));
            SpellFx.fx(FX_MUZZLE, at, yaw, pitch - 90f, scale, store, cb);
            SpellFx.fx(FX_GLINT, at, 0.12f * scale, store, cb);                       // reflet dore de la balle divine
        }

        static Bullet fire(Ref<EntityStore> caster, int side, Shot shot, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = SpellFx.position(caster, store);
            if (at == null) return null;
            Vector3d look = SpellFx.lookDirection(caster, store).normalize();
            Vector3d eye = new Vector3d(at).add(0, 1.5, 0);
            Vector3d muzzle = muzzlePos(eye, look, side);
            Vector3d target = shot.seek() ? seek(caster, eye, look, store) : null;
            if (target == null) target = new Vector3d(eye).add(new Vector3d(look).mul(30));    // les deux canons convergent sur le viseur
            Vector3d dir = new Vector3d(target).sub(muzzle);
            dir = dir.lengthSquared() > 1e-4 ? dir.normalize() : look;
            Ref<EntityStore> model = SpellFx.spawnModel(caster, CARRIER, muzzle, dir, cb);
            SpellFx.notSaved(model, store, cb);
            flash(muzzle, dir, shot == FINALE ? 1.6f : 1f, store, cb);
            return new Bullet(caster, shot, muzzle, new Vector3d(dir), model, System.currentTimeMillis());
        }

        /** Poitrine de l'ennemi le plus proche du regard (cone de 35 degres, 30 blocs), ou null. */
        private static Vector3d seek(Ref<EntityStore> caster, Vector3d eye, Vector3d look, Store<EntityStore> store) {
            Vector3d best = null;
            double bestCos = SEEK_COS;
            Vector3d center = new Vector3d(eye).add(new Vector3d(look).mul(SEEK_RANGE / 2));
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, SEEK_RANGE / 2 + 2, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d to = p.add(0, 1.0, 0).sub(eye);
                double d = to.length();
                if (d < 1e-3 || d > SEEK_RANGE) continue;
                double cos = to.dot(look) / d;
                if (cos > bestCos) { bestCos = cos; best = new Vector3d(eye).add(to); }
            }
            return best;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (caster == null || !caster.isValid()) return true;
            if (now - startAt >= MAX_MS || travelled >= shot.range()) { end(caster, store, cb); return true; }
            if (!scaled && model != null && model.isValid()) {                    // le porteur n'existe qu'au tick suivant sa creation
                if (shot.scale() != 1f) SpellFx.setModelScale(model, MODEL, shot.scale(), 0.6 * shot.scale(), cb);
                scaled = true;
            }
            World world = SpellFx.world(store);
            if (world == null) return true;
            double move = Math.min(shot.range() - travelled, shot.speed() * Math.min(0.2, (now - last) / 1000.0));
            last = now;
            Vector3d mid = new Vector3d(pos).add(new Vector3d(dir).mul(move / 2));
            List<Ref<EntityStore>> near = SpellFx.enemiesAround(caster, mid, move / 2 + shot.hitRadius() + 2, store);
            for (double done = 0; done < move; done += STEP) {
                pos.add(new Vector3d(dir).mul(Math.min(STEP, move - done)));
                for (Ref<EntityStore> e : near) {
                    if (hit.contains(e)) continue;
                    Vector3d p = SpellFx.position(e, store);
                    if (p == null) continue;
                    double cy = Math.max(p.y + 0.2, Math.min(p.y + 1.8, pos.y));          // axe vertical du corps
                    if (new Vector3d(p.x, cy, p.z).distance(pos) > shot.hitRadius()) continue;
                    hit.add(e);
                    strike(caster, e, store, cb);
                    if (hit.size() > shot.pierce()) { end(caster, store, cb); return true; }
                }
                int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
                BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                if (t == null) { end(caster, store, cb); return true; }                     // chunk non charge
                if (t.getMaterial() == BlockMaterial.Solid && !TerrainRestore.isEmpty(world, x, y, z)) {   // herbes et fleurs : traversees
                    long cell = ((long) x & 0x3FFFFFF) << 38 | ((long) y & 0xFFF) << 26 | ((long) z & 0x3FFFFFF);
                    if (cell == lastCell) continue;                                        // bloc deja brise (cassure differee)
                    lastCell = cell;
                    boolean broken = hitBlock(world, x, y, z, t, store, cb);
                    if (!broken || ++blocksBroken > shot.blockPierce()) { end(caster, store, cb); return true; }   // bloc incassable : arret
                }
            }
            travelled += move;
            SpellFx.hold(model, pos, SpellFx.facing(dir, true), store);
            return false;
        }

        private void strike(Ref<EntityStore> caster, Ref<EntityStore> e, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            float damage = shot.damage() * coltBoost(caster, store) * (ArtefactWeapons.isDark(e, store) ? DARK_BONUS : 1f);
            SpellFx.damage(caster, e, "Physical", damage, new Vector3d(dir).mul(2.5).add(0, 1, 0), "Impact_Light", cb);
            SpellFx.fx(FX_IMPACT, pos, shot.scale(), store, cb);
            SpellFx.fx(FX_GLINT, pos, 0.3f * shot.scale(), store, cb);
            SpellFx.sound(SFX_FLESH, pos, cb);
            if (shot.charges() && hit.size() == 1) charge(caster, store);
            if (shot.pillarRadius() > 0 && shot != FINALE && !judged) { judged = true; pillar(pos, store, cb); }
        }

        /** Bloc plein touche : il vole en eclats (particules et son de sa matiere) et la Balle de Jugement y appelle sa colonne. */
        private boolean hitBlock(World world, int x, int y, int z, BlockType t, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d center = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
            SpellFx.fx(FX_IMPACT, pos, shot.scale(), store, cb);
            SpellFx.fx(FX_DEBRIS, pos, 0.5f * shot.scale(), store, cb);
            SpellFx.sound(SFX_WALL, pos, cb);
            boolean broken = breakable(world, x, y, z, t);
            if (broken) {
                SpellFx.fx(breakFx(t), center, 1f, store, cb);
                String sound = breakSound(t);
                if (sound != null) SpellFx.sound(sound, center, cb);
                shatter(world, List.of(new int[]{x, y, z}));
            }
            if (shot.pillarRadius() > 0 && shot != FINALE && !judged) { judged = true; pillar(pos, store, cb); }
            return broken;
        }

        /** Fin de course : la balle de jugement geante creuse le terrain et appelle toujours sa colonne. */
        private void end(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (shot != FINALE || judged || caster == null || !caster.isValid()) return;
            judged = true;
            World world = SpellFx.world(store);
            if (world != null && shot.craterRadius() > 0) crater(world, pos, shot.craterRadius(), store, cb);
            pillar(pos, store, cb);
        }

        /** Trou spherique (restaure apres une minute), avec les eclats de chaque matiere. */
        private static void crater(World world, Vector3d at, double r, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<int[]> cells = new ArrayList<>();
            int R = (int) Math.ceil(r), fx = 0;
            for (int dx = -R; dx <= R; dx++)
                for (int dy = -R; dy <= R; dy++)
                    for (int dz = -R; dz <= R; dz++) {
                        if (dx * dx + dy * dy + dz * dz > r * r) continue;
                        int x = (int) Math.floor(at.x) + dx, y = (int) Math.floor(at.y) + dy, z = (int) Math.floor(at.z) + dz;
                        BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
                        if (t == null || !breakable(world, x, y, z, t)) continue;
                        cells.add(new int[]{x, y, z});
                        if (fx++ % 4 == 0) SpellFx.fx(breakFx(t), new Vector3d(x + 0.5, y + 0.5, z + 0.5), 1f, store, cb);
                    }
            SpellFx.fx("Explosion_Big", at, 0.8f, store, cb);
            shatter(world, cells);
        }

        /** Colonne de lumiere tombee du ciel : faisceau dore, eclat de lumiere, degats autour du point d'impact. */
        private void pillar(Vector3d at, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> caster = owner;
            World world = SpellFx.world(store);
            Vector3d g = world != null ? SpellFx.ground(world, at) : new Vector3d(at);
            if (at.y - g.y > 6 || g.y - at.y > 3) g = new Vector3d(at);              // impact en hauteur : la colonne descend jusqu'a la balle
            boolean big = shot == FINALE;
            float k = big ? 2.2f : 1.0f;
            SpellFx.spawnBeam(BEAM, new Vector3d(g).add(0, 24, 0), g, big ? 3.0f : 1.2f, big ? 1_100 : 500, store, cb);
            SpellFx.fx(FX_AURA, new Vector3d(g).add(0, 0.5, 0), 1.1f * k, store, cb);
            SpellFx.fx(FX_HEAL, new Vector3d(g).add(0, 0.3, 0), 0.9f * k, store, cb);
            SpellFx.fx(FX_GLINT, new Vector3d(g).add(0, 1, 0), 0.8f * k, store, cb);
            if (big) {
                SpellFx.sound2dNear(SFX_THUNDER, g, 60, store);
                SpellFx.shake(g, 25, "Impact_Strong", store);
            }
            if (caster == null || !caster.isValid()) return;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, g, shot.pillarRadius() + 2, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, g) > shot.pillarRadius() || Math.abs(p.y - g.y) > 4) continue;
                Vector3d out = new Vector3d(p.x - g.x, 0, p.z - g.z);
                out = out.lengthSquared() > 0.04 ? out.normalize().mul(big ? 6 : 3) : new Vector3d(dir).mul(big ? 6 : 3);
                float damage = shot.pillarDamage() * coltBoost(caster, store) * (ArtefactWeapons.isDark(e, store) ? DARK_BONUS : 1f);
                SpellFx.damage(caster, e, "Physical", damage, out.add(0, big ? 5 : 2, 0), big ? "Impact_Strong" : "Impact_Light", cb);
                if (shot.blind()) SpellFx.effect(e, BLIND, cb);
            }
        }

        /** Une balle au but charge l'energie de signature de l'arme. */
        private static void charge(Ref<EntityStore> caster, Store<EntityStore> store) {
            EntityStatMap stats = store.getComponent(caster, EntityStatsModule.get().getEntityStatMapComponentType());
            int signature = DefaultEntityStatTypes.getSignatureEnergy();
            if (stats != null && stats.get(signature) != null) stats.addStatValue(signature, SIGNATURE_PER_HIT);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(model, cb);
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(model);
        }
    }

    // =====================================================================================================================
    // Blocs detruits par les balles (restaures apres une minute)
    // =====================================================================================================================

    /** Bloc plein qu'une balle peut briser : hors zone protegee, ni socle, ni bloc conserve (coffres, portes...), ni fluide. */
    private static boolean breakable(World world, int x, int y, int z, BlockType t) {
        if (t == null || t.getMaterial() != BlockMaterial.Solid || TerrainRestore.isEmpty(world, x, y, z)) return false;
        String id = t.getId();
        return !TerrainRestore.isProtectedArea(world.getName(), x, y, z) && !TerrainRestore.contains(id, NEVER)
                && !TerrainRestore.isKept(world, id, x, y, z) && WorldBlockUtil.getFluidIndex(world, x, y, z) <= 0;
    }

    /** Particules de casse de la matiere du bloc (jeu de particules du bloc), sinon celles de la pierre. */
    private static String breakFx(BlockType t) {
        try {
            String set = t.getBlockParticleSetId();
            BlockParticleSet bps = set != null ? BlockParticleSet.getAssetMap().getAsset(set) : null;
            String id = bps != null && bps.getParticleSystemIds() != null ? bps.getParticleSystemIds().get(BlockParticleEvent.Break) : null;
            return id != null ? id : FX_BREAK_DEFAULT;
        } catch (Exception e) {
            return FX_BREAK_DEFAULT;
        }
    }

    /** Son de casse du bloc (jeu de sons du bloc), ou null. */
    private static String breakSound(BlockType t) {
        try {
            BlockSoundSet set = BlockSoundSet.getAssetMap().getAsset(t.getBlockSoundSetIndex());
            return set != null && set.getSoundEventIds() != null ? set.getSoundEventIds().get(BlockSoundEvent.Break) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** Vide les cases (hors du tick ECS) et memorise leurs blocs, remis en place par TerrainRestore apres RESTORE_MS. */
    private static void shatter(World world, List<int[]> cells) {
        if (cells.isEmpty()) return;
        world.execute(() -> {
            TerrainRestore.Restore batch = null;
            for (int[] c : cells) {
                try {
                    BlockType t = WorldBlockUtil.getBlockType(world, c[0], c[1], c[2]);
                    if (!breakable(world, c[0], c[1], c[2], t)) continue;
                    if (batch == null) batch = TerrainRestore.open(world, System.currentTimeMillis() + RESTORE_MS);
                    TerrainRestore.place(world, batch, c[0], c[1], c[2], t.getId(), TerrainRestore.EMPTY, 0);
                } catch (Exception ignored) { }
            }
            long now = System.currentTimeMillis();
            if (batch != null && (cells.size() > 1 || now - lastSave >= SAVE_EVERY_MS)) {   // pas d'ecriture disque a chaque balle
                lastSave = now;
                TerrainRestore.save();
            }
        });
    }
}
