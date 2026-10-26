package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Rune du Domaine Elementaire (Feu, Unique) : domaine fixe de 15 blocs, 15 s, dont la regle change toutes les 5 s :
 * feu (brulure), glace (ralentissement puis gel general a la fin de la phase), foudre (eclairs sur 4 ennemis par seconde).
 * Fiche de conception : scripts/runes/specs/Rune_DomaineElementaire.json
 */
public final class DomaineElementaire implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, PHASE_MS = 5_000, TICK_EVERY_MS = 1_000, BORDER_EVERY_MS = 600;
    private static final double RADIUS = 15.0, HEIGHT = 8.0;
    private static final float FIRE_DAMAGE = 16f, ICE_DAMAGE = 12f, BOLT_DAMAGE = 70f;
    private static final long FREEZE_MS = 1_500, STUN_MS = 500;
    private static final int BOLTS_PER_SECOND = 4, BORDER_POINTS = 24;

    // ===== Assets =====
    private static final String BURN = "Elda_Brulure", SLOW = "Elda_Givre", FREEZE = "Elda_Gel", STUN = "Taranis_Foudroye";
    private static final String[] FX_PHASE = {"Impact_Fire", "Effect_Snow_Impact", "Taranis_Electric_Burst"};
    private static final String[] SFX_PHASE = {"SFX_Flame_Ignite", "SFX_Ice_Build", "SFX_Global_Weather_Thunder"};
    private static final String FX_BOLT = "Taranis_Bolt", FX_FREEZE = "Ability_Ice_Impact";
    private static final String SFX_ZAP = "SFX_Emit_Goblin_Electric_Fence_Zap";

    @Override public String id() { return "DomaineElementaire"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        return new Domain(pos, System.currentTimeMillis());
    }

    private static final class Domain implements Instance {
        final Vector3d center;
        final long startAt;
        int phase = -1;
        long lastTick, lastBorder;

        Domain(Vector3d center, long startAt) { this.center = center; this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            long t = now - startAt;
            if (t >= DURATION_MS) { endPhase(caster, store, cb); return true; }
            int p = (int) (t / PHASE_MS);
            if (p != phase) {
                if (phase >= 0) endPhase(caster, store, cb);
                phase = p;
                lastTick = now - TICK_EVERY_MS;                                      // la nouvelle regle frappe tout de suite
                if (phase == 2) SpellFx.sound2dNear(SFX_PHASE[phase], center, RADIUS * 2, store);
                else SpellFx.sound(SFX_PHASE[phase], center, cb);
            }
            if (now - lastBorder >= BORDER_EVERY_MS) { lastBorder = now; border(store, cb); }
            if (now - lastTick >= TICK_EVERY_MS) { lastTick = now; apply(caster, store, cb); }
            return false;
        }

        /** Bordure et coeur du domaine aux couleurs de la phase. */
        private void border(Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            String fx = FX_PHASE[phase];
            for (int i = 0; i < BORDER_POINTS; i++) {
                double a = Math.PI * 2 * i / BORDER_POINTS;
                SpellFx.fx(fx, new Vector3d(center).add(Math.cos(a) * RADIUS, 0.4, Math.sin(a) * RADIUS), 0.9f, store, cb);
            }
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * 2 * i / 8 + phase;
                SpellFx.fx(fx, new Vector3d(center).add(Math.cos(a) * RADIUS * 0.5, 0.4, Math.sin(a) * RADIUS * 0.5), 0.7f, store, cb);
            }
        }

        private List<Ref<EntityStore>> inside(Ref<EntityStore> caster, Store<EntityStore> store) {
            List<Ref<EntityStore>> out = new ArrayList<>();
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, RADIUS + 1, store)) {
                Vector3d p = SpellFx.position(e, store);
                if (p != null && SpellFx.horizontalDistance(p, center) <= RADIUS && p.y > center.y - 4 && p.y < center.y + HEIGHT) out.add(e);
            }
            return out;
        }

        private void apply(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            List<Ref<EntityStore>> targets = inside(caster, store);
            switch (phase) {
                case 0 -> targets.forEach(e -> { SpellFx.damage(caster, e, "Fire", FIRE_DAMAGE, null, null, cb); SpellFx.effect(e, BURN, cb); });
                case 1 -> targets.forEach(e -> { SpellFx.damage(caster, e, "Ice", ICE_DAMAGE, null, null, cb); SpellFx.effect(e, SLOW, cb); });
                default -> {
                    Collections.shuffle(targets);
                    for (Ref<EntityStore> e : targets.subList(0, Math.min(BOLTS_PER_SECOND, targets.size()))) {
                        Vector3d p = SpellFx.position(e, store);
                        if (p == null) continue;
                        SpellFx.fx(FX_BOLT, p, 0.8f, store, cb);
                        SpellFx.sound(SFX_ZAP, p, cb);
                        SpellFx.damage(caster, e, "Lightning", BOLT_DAMAGE, null, "Impact_Light", cb);
                        SpellFx.stun(e, STUN, STUN_MS, store, cb);
                    }
                }
            }
        }

        /** Fin de la phase de glace : tout le domaine gele d'un coup. */
        private void endPhase(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (phase != 1) return;
            for (Ref<EntityStore> e : inside(caster, store)) {
                SpellFx.stun(e, FREEZE, FREEZE_MS, store, cb);
                Vector3d p = SpellFx.position(e, store);
                if (p != null) SpellFx.fx(FX_FREEZE, p.add(0, 1, 0), 1.0f, store, cb);
            }
        }
    }
}
