package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de l'Autorite Gravitationnelle (Spatial, Legendaire) : le lanceur impose sa gravite a tout ce qui l'entoure.
 *
 * Zone invisible de {@link #RADIUS} blocs de rayon centree sur le lanceur, pendant {@link #DURATION_MS}. Chaque ennemi qui s'y
 * trouve ou y entre recoit l'effet du Puits Gravitationnel : le sol s'effondre sous ses pieds (cratere de 7 blocs, fissures),
 * une colonne de gravite noir-violet tombe sur lui, et il reste cloue au sol jusqu'a la fin (2 degats par seconde).
 * La gravite est invisible : seuls le terrain sous les cibles et les colonnes la trahissent (pas d'explosion au sol).
 * Fiche de conception : scripts/runes/specs/Rune_AutoriteGravitationnelle.json
 */
public final class AutoriteGravitationnelle implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RADIUS = 20.0, HEIGHT = 40.0;
    private static final long DURATION_MS = 30_000, SCAN_EVERY_MS = 400, PIN_DELAY_MS = 350, DAMAGE_EVERY_MS = 1_000, SLAM_MAX_MS = 3_000;
    private static final float DAMAGE_PER_SECOND = 2f;
    private static final double SLAM_SPEED = 24.0;
    private static final int WELLS_MAX = 10;                       // colonnes simultanees (budget particules)
    private static final long RESTORE_AFTER_MS = 180_000;
    // ===== Ambiance =====
    private static final long COLUMN_EVERY_MS = 500, FISSURE_EVERY_MS = 400, HUM_EVERY_MS = 2_500;
    private static final int FISSURE_FX_PER_WELL = 4;

    // ===== Assets =====
    private static final String COLUMN_PROJECTILE = "Projectile_Config_Gravite_Colonne";
    private static final String FX_COLUMN = "Gravite_Colonne_Legere", FX_FISSURE = "Gravite_Fissure";
    private static final String CRUSH_EFFECT = "Gravite_Ecrasement";
    private static final String SFX_IMPACT = "SFX_Golem_Frost_Slam_Impact", SFX_HUM = "SFX_Emit_Dungeon_Goblin_Void_Strike", SFX_CAST = "SFX_Portal_Void", SFX_END = "SFX_Void_Eviction";

    @Override public String id() { return "AutoriteGravitationnelle"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        World world = SpellFx.world(store);
        SpellFx.sound(SFX_CAST, pos, cb);
        return new Domain(caster, SpellFx.ground(world, pos), System.currentTimeMillis(), world);
    }

    /** Puits sous un ennemi : cratere, fissures, colonne ; l'ennemi y est cloue. */
    private static final class Well {
        final Vector3d at;
        final List<double[]> cracks;
        final long caughtAt;
        Ref<EntityStore> column;
        boolean pinned;
        Well(Vector3d at, List<double[]> cracks, long caughtAt) { this.at = at; this.cracks = cracks; this.caughtAt = caughtAt; }
    }

    private static final class Domain implements Instance {
        final Ref<EntityStore> caster;
        final Vector3d center;
        final long startAt;
        final World world;
        final TerrainRestore.Restore terrain;
        final Map<Ref<EntityStore>, Well> wells = new LinkedHashMap<>();
        long lastScan, lastDamage, lastColumn, lastFissure, lastHum;

        Domain(Ref<EntityStore> caster, Vector3d center, long startAt, World world) {
            this.caster = caster; this.center = center; this.startAt = startAt; this.world = world; this.lastDamage = startAt;
            this.terrain = TerrainRestore.open(world, startAt + RESTORE_AFTER_MS);
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (now - startAt >= DURATION_MS) { SpellFx.sound(SFX_END, center, cb); return true; }
            if (now - lastScan >= SCAN_EVERY_MS) { lastScan = now; capture(now, store, cb); }
            boolean columns = now - lastColumn >= COLUMN_EVERY_MS, fissures = now - lastFissure >= FISSURE_EVERY_MS;
            if (columns) lastColumn = now;
            if (fissures) lastFissure = now;
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            for (Map.Entry<Ref<EntityStore>, Well> en : wells.entrySet()) {
                Ref<EntityStore> e = en.getKey();
                Well w = en.getValue();
                if (!e.isValid()) {                                                             // cible morte : sa colonne s'eteint, le cratere reste
                    if (w.column != null) { SpellFx.remove(w.column, cb); w.column = null; }
                    continue;
                }
                SpellFx.hold(w.column, w.at, null, store);
                if (columns) SpellFx.fx(FX_COLUMN, w.at, 1.0f, store, cb);
                if (fissures && !w.cracks.isEmpty())
                    for (int i = 0; i < FISSURE_FX_PER_WELL; i++) {
                        double[] c = w.cracks.get(rng.nextInt(w.cracks.size()));
                        SpellFx.fx(FX_FISSURE, new Vector3d(c[0] + 0.5, w.at.y + 0.3 - c[2], c[1] + 0.5), 1.0f, store, cb);
                    }
                if (!w.pinned) pin(e, w, now, store, cb);
            }
            if (now - lastHum >= HUM_EVERY_MS && !wells.isEmpty()) { lastHum = now; SpellFx.sound(SFX_HUM, center, cb); }
            if (now - lastDamage >= DAMAGE_EVERY_MS) {
                lastDamage = now;
                for (Map.Entry<Ref<EntityStore>, Well> en : wells.entrySet())
                    if (en.getValue().pinned && en.getKey().isValid()) SpellFx.damage(caster, en.getKey(), "Crush", DAMAGE_PER_SECOND, null, null, cb);
            }
            return false;
        }

        /** Chaque nouvel ennemi de la zone recoit son puits (sol effondre sous ses pieds + colonne). */
        private void capture(long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            long active = wells.keySet().stream().filter(Ref::isValid).count();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 1, store)) {
                if (active >= WELLS_MAX) return;
                if (wells.containsKey(e)) continue;
                Vector3d p = SpellFx.position(e, store);
                if (p == null || SpellFx.horizontalDistance(p, center) > RADIUS || Math.abs(p.y - center.y) > HEIGHT) continue;
                Vector3d at = SpellFx.ground(world, p);
                Well w = new Well(at, PuitsGravitationnel.planCracks(at), now);
                PuitsGravitationnel.crater(world, terrain, at, w.cracks);
                w.column = SpellFx.spawnModel(caster, COLUMN_PROJECTILE, at, new Vector3d(1, 0, 0), cb);
                SpellFx.fx(FX_COLUMN, at, 1.0f, store, cb);
                SpellFx.sound(SFX_IMPACT, at, cb);
                SpellFx.push(e, new Vector3d(0, -SLAM_SPEED, 0), false, cb);
                wells.put(e, w);
                active++;
            }
        }

        /** Apres la chute, l'ennemi est cloue jusqu'a la fin du sort (joueurs : effet ; monstres : verrou). */
        private void pin(Ref<EntityStore> e, Well w, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d p = SpellFx.position(e, store);
            boolean grounded = p != null && p.y - w.at.y < 1.5;
            if (now - w.caughtAt < PIN_DELAY_MS || (!grounded && now - w.caughtAt < SLAM_MAX_MS)) {
                SpellFx.push(e, new Vector3d(0, -SLAM_SPEED, 0), false, cb);
                return;
            }
            w.pinned = true;
            long left = Math.max(0, DURATION_MS - (now - startAt));
            EffectsManager.applyCustomEffect(e, CRUSH_EFFECT, left / 1000f, OverlapBehavior.OVERWRITE, cb);
            SpellFx.lockInPlace(e, left, store);
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            for (Map.Entry<Ref<EntityStore>, Well> en : wells.entrySet()) {
                SpellFx.remove(en.getValue().column, cb);
                Ref<EntityStore> e = en.getKey();
                if (!en.getValue().pinned || !e.isValid()) continue;
                SpellFx.unlock(e);
                try { EffectsManager.removeEffect(e, CRUSH_EFFECT, cb); } catch (Exception ex) { EldaniorLogger.error("AutoriteGravitationnelle: effet", ex); }
            }
            wells.clear();
            TerrainRestore.save();
        }

        @Override
        public void abort() {
            List<Ref<EntityStore>> targets = new ArrayList<>(wells.keySet());
            for (Well w : wells.values()) RuneSpellRuntime.removeLater(w.column);
            for (Ref<EntityStore> e : targets) SpellFx.unlock(e);                               // l'effet expire seul (borne au temps restant)
            TerrainRestore.save();
        }
    }
}
