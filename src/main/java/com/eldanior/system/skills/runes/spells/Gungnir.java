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
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gungnir, la Lance d'Odin (artefact divin, Artefact_Gungnir) : elle ne manque jamais sa cible et revient toujours dans
 * la main de son porteur. Deux sorts du moteur RuneSpell, lances par les interactions de l'arme :
 *   - GungnirLancer (clic maintenu) : la lance quitte la main du porteur et file, pointe en avant, vers l'ennemi vise (ou
 *     le plus proche devant lui). Elle transperce tout sur son passage (creatures et blocs : tunnel restaure apres
 *     RESTORE_MS), frappe sa cible, puis la foudre d'Odin s'abat sur elle et laisse un cratere ; la lance revient ensuite
 *     dans la main. Sans ennemi, elle file droit devant et revient ;
 *   - OeilOdin (signature) : marque jusqu'a 8 ennemis proches, les transperce l'un apres l'autre (un eclair sur chacun),
 *     puis la grande foudre et un cratere sur le dernier, et elle revient.
 * La lance en vol est un modele (Projectile_Config_Gungnir) deplace par le code a chaque tick : trajectoire exacte, aucune
 * physique. Une seule lance en vol par porteur. Terrain : TerrainRestore (jamais en ville, sur une parcelle, en donjon...).
 */
public final class Gungnir {

    private Gungnir() {}

    public static final String ITEM_ID = "Artefact_Gungnir";
    private static final String FLYING = "Projectile_Config_Gungnir";
    private static final String FX_HIT = "Taranis_Electric_Burst", FX_MARK = "Bifrost_Divine", FX_RETURN = "Gilgamesh_Sword_Burst";
    private static final String FX_BOLT = "Taranis_Bolt", FX_SKY_FLASH = "Taranis_Sky_Flash";
    private static final String SFX_HIT = "SFX_Spear_Impact", SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";
    private static final String SFX_THUNDER = "SFX_Global_Weather_Thunder";

    /**
     * Orientation du modele en vol, ajoutee a la direction du vol (degres). Zero : le modele est monte comme une lance
     * vanilla lancee (son Origin_Projectile est une piece fixee a celui du projectile : la pointe suit l'axe du vol).
     */
    private static final float PITCH_OFFSET = 0f, YAW_OFFSET = 0f;

    /** Terrain detruit par la lance (tunnel, cratere) : restaure apres ce delai. */
    private static final long RESTORE_MS = 120_000;
    private static final double CARVE_RADIUS = 1.0;                                    // tunnel de 3 blocs de large
    private static final String[] NEVER_CARVED = {"Bedrock", "Barrier"};

    /** Porteurs dont la lance est en vol. */
    private static final Set<Ref<EntityStore>> inFlight = ConcurrentHashMap.newKeySet();

    private static Vector3d eye(Ref<EntityStore> caster, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(caster, store);
        return p != null ? p.add(0, 1.5, 0) : null;
    }

    private static Vector3d chest(Ref<EntityStore> e, Store<EntityStore> store) {
        Vector3d p = SpellFx.position(e, store);
        return p != null ? p.add(0, 1.0, 0) : null;
    }

    /** Orientation de la lance en vol : direction du vol + correction du modele (PITCH_OFFSET, YAW_OFFSET). */
    private static Rotation3f orient(Vector3d dir) {
        Rotation3f rot = new Rotation3f();
        rot.setYaw(PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(dir.x, dir.z) + (float) Math.toRadians(YAW_OFFSET)));
        rot.setPitch(PhysicsMath.pitchFromDirection(dir.x, dir.y, dir.z) + (float) Math.toRadians(PITCH_OFFSET));
        return rot;
    }

    // ===================== Lancer =====================

    public static final class Lancer implements RuneSpell {
        private static final double RANGE = 80, ASSIST_ANGLE = Math.toRadians(35), FRONT_ANGLE = Math.toRadians(70), FREE_FLIGHT = 70;
        static final float DAMAGE = 150f, PIERCE = 60f, BOLT = 90f;
        static final double BOLT_RADIUS = 4.0;

        @Override public String id() { return "GungnirLancer"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!inFlight.add(caster)) return null;                                     // deja en vol
            Vector3d from = eye(caster, store);
            if (from == null) { inFlight.remove(caster); return null; }
            Vector3d look = SpellFx.lookDirection(caster, store);
            Ref<EntityStore> target = SpellFx.targetEntity(caster, store, RANGE);
            if (target == null) target = guided(caster, from, look, ASSIST_ANGLE, store);   // visee assistee
            if (target == null) target = guided(caster, from, look, FRONT_ANGLE, store);    // sinon le plus proche devant
            List<Ref<EntityStore>> targets = target != null ? List.of(target) : List.of();
            Vector3d free = new Vector3d(from).add(new Vector3d(look).mul(FREE_FLIGHT));
            return Flight.launch(caster, from, look, targets, free, 55, 60, 10_000, false, store, cb);
        }

        /** L'ennemi le plus proche de la ligne de visee, dans un cone de l'angle donne (le plus proche en distance a egalite). */
        private static Ref<EntityStore> guided(Ref<EntityStore> caster, Vector3d from, Vector3d look, double maxAngle, Store<EntityStore> store) {
            Ref<EntityStore> best = null;
            double bestScore = Double.MAX_VALUE;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, from, RANGE, store)) {
                Vector3d c = chest(e, store);
                if (c == null) continue;
                Vector3d to = new Vector3d(c).sub(from);
                double angle = to.angle(look);
                if (angle > maxAngle) continue;
                double score = angle * 40 + to.length();                              // l'angle compte plus que la distance
                if (score < bestScore) { bestScore = score; best = e; }
            }
            return best;
        }
    }

    // ===================== Oeil d'Odin =====================

    public static final class OeilOdin implements RuneSpell {
        private static final double RADIUS = 30;
        private static final int MAX_TARGETS = 8;
        static final float DAMAGE = 200f, BOLT = 50f, FINAL_BOLT = 140f;
        static final double BOLT_RADIUS = 2.5, FINAL_BOLT_RADIUS = 5.0;

        @Override public String id() { return "OeilOdin"; }

        @Override
        public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d from = eye(caster, store);
            if (from == null || !inFlight.add(caster)) return null;
            List<Ref<EntityStore>> chain = chain(caster, from, store);
            if (chain.isEmpty()) { inFlight.remove(caster); return null; }
            for (Ref<EntityStore> e : chain) {                                           // l'oeil d'Odin les marque
                Vector3d c = chest(e, store);
                if (c != null) {
                    SpellFx.fx(FX_MARK, new Vector3d(c).add(0, 1.6, 0), 0.8f, store, cb);
                    SpellFx.fx(FX_HIT, new Vector3d(c), 0.5f, store, cb);
                }
            }
            SpellFx.fx(FX_SKY_FLASH, new Vector3d(from).add(0, 20, 0), 10f, store, cb);   // le ciel s'ouvre
            SpellFx.sound(SFX_ZAP, from, cb);
            SpellFx.sound2dNear(SFX_THUNDER, from, 60, store);
            Vector3d look = SpellFx.lookDirection(caster, store);
            return Flight.launch(caster, from, look, chain, null, 65, 60, 14_000, true, store, cb);
        }

        /** Ennemis du rayon, dans l'ordre du trajet : chaque fois le plus proche du precedent. */
        private static List<Ref<EntityStore>> chain(Ref<EntityStore> caster, Vector3d from, Store<EntityStore> store) {
            List<Ref<EntityStore>> left = new ArrayList<>(SpellFx.enemiesAround(caster, from, RADIUS, store));
            List<Ref<EntityStore>> out = new ArrayList<>();
            Vector3d at = new Vector3d(from);
            while (!left.isEmpty() && out.size() < MAX_TARGETS) {
                final Vector3d here = at;
                Ref<EntityStore> next = left.stream().filter(e -> chest(e, store) != null)
                        .min(Comparator.comparingDouble(e -> chest(e, store).distanceSquared(here))).orElse(null);
                if (next == null) break;
                left.remove(next);
                out.add(next);
                at = chest(next, store);
            }
            return out;
        }
    }

    // ===================== Lance en vol =====================

    /**
     * Trajet de la lance : chaque cible a son tour (la poursuit jusqu'au contact), sinon le point libre, puis retour vers
     * la main du porteur. Elle transperce tout ce qu'elle croise (creatures, blocs), et la foudre suit ses coups.
     */
    private static final class Flight implements RuneSpell.Instance {
        private static final double HIT_RADIUS = 1.3, CATCH_RADIUS = 1.6;
        private static final long MARK_DELAY_MS = 500, SPAWN_WAIT_MS = 1_000, BOLT_DELAY_MS = 250;
        private static final double NEAR_CASTER = 3.0;                                  // pas de tunnel a moins de 3 blocs du porteur

        final Ref<EntityStore> caster, spear;
        final List<Ref<EntityStore>> targets;
        final Vector3d free;
        final double outSpeed, backSpeed;
        final long startedAt, timeoutMs;
        final boolean signature;
        final World world;
        final Set<Ref<EntityStore>> pierced = new HashSet<>();                          // deja transperces en vol
        final Set<Long> carved = new HashSet<>();                                       // cases deja creusees
        TerrainRestore.Restore terrain;
        Vector3d pos, dir;
        long last, boltAt;
        Vector3d boltPos;
        boolean boltFinal;
        int index, hiddenSlot = -1;
        boolean returning, spawned, caught;

        private Flight(Ref<EntityStore> caster, Ref<EntityStore> spear, Vector3d pos, Vector3d dir, List<Ref<EntityStore>> targets,
                       Vector3d free, double outSpeed, double backSpeed, long timeoutMs, boolean signature, World world) {
            this.caster = caster; this.spear = spear; this.pos = pos; this.dir = dir; this.targets = targets; this.free = free;
            this.outSpeed = outSpeed; this.backSpeed = backSpeed; this.timeoutMs = timeoutMs; this.signature = signature;
            this.world = world;
            this.startedAt = this.last = System.currentTimeMillis();
        }

        static Flight launch(Ref<EntityStore> caster, Vector3d from, Vector3d look, List<Ref<EntityStore>> targets, Vector3d free,
                             double outSpeed, double backSpeed, long timeoutMs, boolean signature,
                             Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d start = new Vector3d(from).add(new Vector3d(look).mul(1.0));
            Ref<EntityStore> spear = SpellFx.spawnModel(caster, FLYING, start, look, cb);
            if (spear == null) { inFlight.remove(caster); return null; }
            SpellFx.notSaved(spear, store, cb);                                          // deconnexion en plein vol : pas de lance fantome
            Flight f = new Flight(caster, spear, start, new Vector3d(look), targets, free, outSpeed, backSpeed, timeoutMs, signature,
                    SpellFx.world(store));
            f.hideInHand(store);
            return f;
        }

        @Override
        public boolean tick(Ref<EntityStore> owner, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (!caster.isValid() || now - startedAt > timeoutMs) { showInHand(store); return true; }
            if (boltAt > 0 && now >= boltAt) { lightning(boltPos, boltFinal, store, cb); boltAt = 0; }
            if (caught) { if (boltAt == 0) { showInHand(store); return true; } return false; }  // attend la foudre
            if (!spawned) {                                                              // creee par le CommandBuffer : au tick suivant
                if (SpellFx.position(spear, store) == null) return now - startedAt > SPAWN_WAIT_MS;
                spawned = true;
            }
            if (!spear.isValid()) { showInHand(store); return true; }
            double dt = Math.min(0.1, (now - last) / 1000.0);
            last = now;
            if (signature && now - startedAt < MARK_DELAY_MS) { SpellFx.hold(spear, pos, orient(dir), store); return false; }
            Vector3d goal = goal(store);
            if (goal == null) return false;                                              // cible disparue : passe a la suivante
            Vector3d to = new Vector3d(goal).sub(pos);
            double dist = to.length(), step = (returning ? backSpeed : outSpeed) * dt;
            if (dist > 1e-4) dir = to.div(dist);
            Vector3d before = new Vector3d(pos);
            if (dist <= step) pos = new Vector3d(goal); else pos.add(new Vector3d(dir).mul(step));
            SpellFx.hold(spear, pos, orient(dir), store);
            Vector3d hand = eye(caster, store);
            if (hand == null || hand.distance(pos) > NEAR_CASTER) carve(before, pos);    // elle traverse tout (pas pres du porteur)
            pierce(store, cb);
            if (returning) {
                if (dist <= Math.max(step, CATCH_RADIUS)) { SpellFx.fx(FX_RETURN, pos, 0.5f, store, cb); SpellFx.remove(spear, cb); caught = true; }
                return false;
            }
            if (index < targets.size()) {
                if (dist <= Math.max(step, HIT_RADIUS)) strike(targets.get(index++), store, cb);
            } else if (!returning && dist <= step) {
                returning = true;                                                        // vol libre termine
            }
            if (index >= targets.size() && !targets.isEmpty()) returning = true;
            return false;
        }

        /** Prochain point : la cible en cours (sautee si elle a disparu), le point libre, ou la main du porteur. */
        private Vector3d goal(Store<EntityStore> store) {
            if (returning) return eye(caster, store);
            while (index < targets.size()) {
                Ref<EntityStore> t = targets.get(index);
                Vector3d c = t.isValid() ? chest(t, store) : null;
                if (c != null) return c;
                index++;
            }
            if (!targets.isEmpty()) { returning = true; return eye(caster, store); }
            return free;
        }

        /** Coup sur une cible : degats, foudre runique au contact, puis l'eclair d'Odin tombe sur elle (BOLT_DELAY_MS). */
        private void strike(Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d at = chest(target, store);
            if (at == null) return;
            pierced.add(target);
            Vector3d push = new Vector3d(dir.x, 0, dir.z);
            if (push.lengthSquared() > 1e-4) push.normalize().mul(signature ? 5 : 8).add(0, 3.5, 0); else push = null;
            SpellFx.damage(caster, target, "Physical", signature ? OeilOdin.DAMAGE : Lancer.DAMAGE, push, "Impact_Strong", cb);
            SpellFx.fx(FX_HIT, at, signature ? 1.4f : 1.2f, store, cb);
            SpellFx.sound(SFX_HIT, at, cb);
            SpellFx.sound(SFX_ZAP, at, cb);
            boolean finalHit = !signature || index >= targets.size();
            if (signature && !finalHit) { lightning(at, false, store, cb); return; }       // eclair sur chaque transperce
            boltPos = new Vector3d(at);                                                  // grande foudre + cratere
            boltFinal = true;
            boltAt = System.currentTimeMillis() + BOLT_DELAY_MS;
            returning = true;
        }

        /** Les creatures croisees en vol sont transpercees (une fois chacune). */
        private void pierce(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> current = !returning && index < targets.size() ? targets.get(index) : null;
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, HIT_RADIUS + 0.4, store)) {
                if (e.equals(current) || !pierced.add(e)) continue;
                SpellFx.damage(caster, e, "Physical", Lancer.PIERCE, null, "Impact_Light", cb);
                Vector3d c = chest(e, store);
                if (c != null) SpellFx.fx(FX_HIT, c, 0.6f, store, cb);
            }
        }

        /**
         * Foudre d'Odin : eclair du ciel sur le sol sous le point, degats de foudre autour ; la grande foudre (final) frappe
         * plus fort, plus large, et creuse un cratere (PuitsGravitationnel.crater).
         */
        private void lightning(Vector3d at, boolean big, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (at == null) return;
            Vector3d ground = world != null ? SpellFx.ground(world, at) : new Vector3d(at);
            float damage = signature ? (big ? OeilOdin.FINAL_BOLT : OeilOdin.BOLT) : Lancer.BOLT;
            double radius = signature ? (big ? OeilOdin.FINAL_BOLT_RADIUS : OeilOdin.BOLT_RADIUS) : Lancer.BOLT_RADIUS;
            SpellFx.fx(FX_SKY_FLASH, new Vector3d(ground).add(0, 25, 0), big ? 12f : 6f, store, cb);
            SpellFx.fx(FX_BOLT, ground, big ? 2.5f : 1.2f, store, cb);
            SpellFx.fx(FX_HIT, new Vector3d(ground).add(0, 0.5, 0), big ? 2.0f : 1.0f, store, cb);
            SpellFx.sound2dNear(SFX_THUNDER, ground, big ? 80 : 40, store);
            if (big) SpellFx.shake(ground, 25, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, ground, radius, store))
                SpellFx.damage(caster, e, "Lightning", damage, null, big ? "Impact_Strong" : "Impact_Light", cb);
            if (big && world != null) {
                try {
                    PuitsGravitationnel.crater(world, terrain(), ground, PuitsGravitationnel.planCracks(ground));
                } catch (Exception e) {
                    EldaniorLogger.error("Gungnir: cratere", e);
                }
            }
        }

        private TerrainRestore.Restore terrain() {
            if (terrain == null) terrain = TerrainRestore.open(world, System.currentTimeMillis() + RESTORE_MS);
            return terrain;
        }

        /** Tunnel le long du trajet [from, to] : les blocs traverses disparaissent (restaures apres RESTORE_MS). */
        private void carve(Vector3d from, Vector3d to) {
            if (world == null) return;
            List<int[]> cells = new ArrayList<>();
            int steps = Math.max(1, (int) Math.ceil(from.distance(to) / 0.5));
            int r = (int) Math.ceil(CARVE_RADIUS);
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double x = from.x + (to.x - from.x) * t, y = from.y + (to.y - from.y) * t, z = from.z + (to.z - from.z) * t;
                for (int dx = -r; dx <= r; dx++)
                    for (int dy = -r; dy <= r; dy++)
                        for (int dz = -r; dz <= r; dz++) {
                            if (dx * dx + dy * dy + dz * dz > CARVE_RADIUS * CARVE_RADIUS * 2) continue;
                            int bx = (int) Math.floor(x) + dx, by = (int) Math.floor(y) + dy, bz = (int) Math.floor(z) + dz;
                            long key = (((long) bx & 0x3FFFFFFL) << 38) | (((long) by & 0xFFFL) << 26) | ((long) bz & 0x3FFFFFFL);
                            if (carved.add(key)) cells.add(new int[]{bx, by, bz});
                        }
            }
            if (cells.isEmpty()) return;
            World w = world;
            TerrainRestore.Restore batch = terrain();
            w.execute(() -> {                                                           // blocs : hors du tick ECS
                for (int[] c : cells) {
                    try {
                        BlockType type = WorldBlockUtil.getBlockType(w, c[0], c[1], c[2]);
                        if (type == null || TerrainRestore.isEmpty(w, c[0], c[1], c[2])) continue;
                        String id = type.getId();
                        if (TerrainRestore.contains(id, NEVER_CARVED) || TerrainRestore.isProtectedArea(w.getName(), c[0], c[1], c[2])
                                || TerrainRestore.isKept(w, id, c[0], c[1], c[2]) || WorldBlockUtil.getFluidIndex(w, c[0], c[1], c[2]) > 0) continue;
                        TerrainRestore.place(w, batch, c[0], c[1], c[2], id, TerrainRestore.EMPTY, 0);
                    } catch (Exception ignored) { }
                }
            });
        }

        /** Au lancer, la lance quitte la main : le porteur passe sur un emplacement vide de sa barre (s'il en a un). */
        private void hideInHand(Store<EntityStore> store) {
            try {
                Player p = store.getComponent(caster, Player.getComponentType());
                var inv = p != null ? p.getInventory() : null;
                if (inv == null || inv.getHotbar() == null) return;
                ItemStack held = inv.getActiveHotbarItem();
                if (held == null || held.isEmpty() || !ITEM_ID.equals(held.getItemId())) return;
                short current = inv.getActiveHotbarSlot();
                for (short i = 0; i < 9; i++) {
                    ItemStack it = inv.getHotbar().getItemStack(i);
                    if (i != current && (it == null || it.isEmpty())) {
                        hiddenSlot = current;
                        inv.setActiveHotbarSlot(caster, (byte) i, store);
                        return;
                    }
                }
            } catch (Exception e) {
                EldaniorLogger.error("Gungnir: main", e);
            }
        }

        /** Au retour, la lance revient dans la main (si elle est toujours a son emplacement). */
        private void showInHand(Store<EntityStore> store) {
            if (hiddenSlot < 0) return;
            int slot = hiddenSlot;
            hiddenSlot = -1;
            try {
                Player p = caster.isValid() ? store.getComponent(caster, Player.getComponentType()) : null;
                var inv = p != null ? p.getInventory() : null;
                if (inv == null || inv.getHotbar() == null) return;
                ItemStack it = inv.getHotbar().getItemStack((short) slot);
                if (it != null && !it.isEmpty() && ITEM_ID.equals(it.getItemId())) inv.setActiveHotbarSlot(caster, (byte) slot, store);
            } catch (Exception e) {
                EldaniorLogger.error("Gungnir: main", e);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            SpellFx.remove(spear, cb);
            inFlight.remove(caster);
        }

        @Override
        public void abort() {
            RuneSpellRuntime.removeLater(spear);
            inFlight.remove(caster);
        }
    }
}
