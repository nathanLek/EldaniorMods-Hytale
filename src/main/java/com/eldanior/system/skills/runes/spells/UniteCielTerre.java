package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.List;

/**
 * Rune de l'Unite du Ciel et de la Terre (Combat, Legendaire, Dieu Martial, voie martiale du Clan Luo) : 10 s, le lanceur se
 * fond dans la nature et devient intouchable (onOwnerHurt -> 0) ; toutes les 0,6 s, le vent tranche chaque ennemi a 25 blocs
 * (90, 16 ennemis au plus par vague, effet Unite_Vent : ralenti). A la fin, une coupe horizontale unique frappe tous les
 * ennemis de la zone (700). Epargne groupe, guilde et famille.
 * Fiche de conception : scripts/runes/specs/Rune_UniteCielTerre.json
 */
public final class UniteCielTerre implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, WAVE_MS = 600, AURA_MS = 1_000;
    private static final double RADIUS = 25.0;
    private static final float WIND = 90f, FINAL_CUT = 700f;
    private static final int MAX_PER_WAVE = 16, RING_POINTS = 24;

    // ===== Assets =====
    private static final String SLOW = "Unite_Vent", AURA = "Ki_Visual_8";
    private static final String FX_CUT = "Impact_Sword_Charged", FX_WIND = "Fujin_Gust", FX_RING = "Eldanior_Pas_Celeste", FX_GOLD = "Gilgamesh_Sword_Burst";
    private static final String SFX_WIND = "SFX_Ability_Wind_Strike_Slash", SFX_FINAL = "SFX_Global_Weather_Thunder", SFX_START = "SFX_Longsword_Special_Impact";

    @Override public String id() { return "UniteCielTerre"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, AURA, cb);
        SpellFx.fx(FX_RING, new Vector3d(pos).add(0, 0.1, 0), 4.0f, store, cb);
        SpellFx.sound(SFX_START, pos, cb);
        return new Unity(System.currentTimeMillis());
    }

    private static final class Unity implements Instance {
        final long startAt;
        long lastWave, lastAura;

        Unity(long startAt) { this.startAt = startAt; this.lastWave = startAt; this.lastAura = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) { return 0f; }   // fondu dans la nature

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d me = SpellFx.position(caster, store);
            if (me == null) return true;
            if (now - startAt >= DURATION_MS) { finalCut(caster, me, store, cb); return true; }
            if (now - lastAura >= AURA_MS) { lastAura = now; SpellFx.effect(caster, AURA, cb); }
            if (now - lastWave < WAVE_MS) return false;
            lastWave = now;
            List<Ref<EntityStore>> foes = SpellFx.enemiesAround(caster, me, RADIUS, store);
            int n = 0;
            for (Ref<EntityStore> e : foes) {
                if (n++ >= MAX_PER_WAVE) break;
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                Vector3d chest = new Vector3d(p).add(0, 1.1, 0);
                SpellFx.fx(FX_WIND, chest, 0.6f, store, cb);
                SpellFx.fx(FX_CUT, chest, 0.8f, store, cb);
                if (n % 4 == 1) SpellFx.sound(SFX_WIND, chest, cb);
                SpellFx.damage(caster, e, "Physical", WIND, null, null, cb);
                SpellFx.effect(e, SLOW, cb);
            }
            return false;
        }

        /** La coupe qui traverse le monde : anneau d'or a hauteur de poitrine, puis tous les ennemis de la zone. */
        private void finalCut(Ref<EntityStore> caster, Vector3d me, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            for (int i = 0; i < RING_POINTS; i++) {
                double a = i * 2 * Math.PI / RING_POINTS;
                for (double r : new double[]{RADIUS * 0.35, RADIUS * 0.7, RADIUS})
                    SpellFx.fx(FX_GOLD, new Vector3d(me).add(Math.cos(a) * r, 1.2, Math.sin(a) * r), 0.7f, store, cb);
            }
            SpellFx.fx(FX_RING, new Vector3d(me).add(0, 0.1, 0), 5.0f, store, cb);
            SpellFx.sound2dNear(SFX_FINAL, me, RADIUS + 10, store);
            SpellFx.shake(me, RADIUS + 5, "Impact_Strong", store);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, me, RADIUS, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p == null) continue;
                SpellFx.fx(FX_CUT, new Vector3d(p).add(0, 1.1, 0), 1.8f, store, cb);
                Vector3d away = new Vector3d(p.x - me.x, 0, p.z - me.z);
                if (away.lengthSquared() < 1e-4) away.set(0, 0, 1);
                SpellFx.damage(caster, e, "Physical", FINAL_CUT, away.normalize().mul(8).add(0, 3, 0), "Impact_Strong", cb);
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { }
    }
}
