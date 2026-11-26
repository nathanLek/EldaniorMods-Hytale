package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Rune de Prescience (Temps, Epic, rune de predilection du Voyant Arcane) : pendant 10 s, les 3 prochains coups portes par
 * une creature ou un joueur sont esquives (onOwnerHurt -> 0) : pas de cote de 2 blocs (case libre), image remanente,
 * attaquant fige 1 s. Le coup du lanceur qui suit une esquive inflige 50 % en plus (riposte, markEcho).
 * Fiche de conception : scripts/runes/specs/Rune_Prescience.json
 */
public final class Prescience implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000, FREEZE_MS = 1_000, AURA_EVERY_MS = 1_000;
    private static final int DODGES = 3;
    private static final double STEP = 2.0;
    private static final float RIPOSTE = 0.5f;

    // ===== Assets =====
    private static final String VEIL = "Elda_Prescience", FREEZE = "Chronos_Fige";
    private static final String FX_AURA = "Chronos_Gold", FX_GHOST = "Chronos_Afterimage", FX_DODGE = "Chronos_Burst", FX_RIPOSTE = "Impact_Critical";
    private static final String SFX_CAST = "SFX_Arcane_Workbench_Craft", SFX_DODGE = "SFX_Goblin_Feastmaster_Whoosh_Quick";

    @Override public String id() { return "Prescience"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        SpellFx.effect(caster, VEIL, cb);
        SpellFx.fx(FX_DODGE, new Vector3d(pos).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_CAST, pos, cb);
        return new Foresight(caster, SpellFx.world(store), System.currentTimeMillis());
    }

    private static final class Foresight implements Instance {
        final Ref<EntityStore> caster;
        final World world;
        final long startAt;
        final Queue<Ref<EntityStore>> dodged = new ConcurrentLinkedQueue<>();   // attaquants esquives, traites dans tick
        volatile int dodgesLeft = DODGES;
        volatile boolean riposteReady;
        volatile Ref<EntityStore> riposteTarget;
        volatile float riposteAmount;
        long lastAura;

        Foresight(Ref<EntityStore> caster, World world, long startAt) { this.caster = caster; this.world = world; this.startAt = startAt; }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            if (attacker == null || attacker.equals(caster) || dodgesLeft <= 0) return amount;   // chute, feu, poison : pas d'esquive
            dodgesLeft--;
            dodged.add(attacker);
            return 0;
        }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (riposteReady && riposteTarget == null) { riposteAmount = amount; riposteTarget = target; }
        }

        @Override
        public boolean tick(Ref<EntityStore> casterRef, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d pos = SpellFx.position(caster, store);
            if (pos == null) return true;
            Ref<EntityStore> attacker;
            while ((attacker = dodged.poll()) != null) dodge(pos, attacker, store, cb);

            Ref<EntityStore> target = riposteTarget;
            if (target != null) {                                         // riposte : +50 % sur le coup qui suit une esquive
                riposteTarget = null;
                riposteReady = false;
                Vector3d p = SpellFx.position(target, store);
                float bonus = riposteAmount * RIPOSTE;
                if (p != null && bonus >= 0.5f) {
                    RuneSpellRuntime.markEcho(target, bonus);
                    SpellFx.damage(caster, target, "Elemental", bonus, null, "Impact_Strong", cb);
                    SpellFx.fx(FX_RIPOSTE, new Vector3d(p).add(0, 1, 0), 1.0f, store, cb);
                }
            }
            if (now - lastAura >= AURA_EVERY_MS) { lastAura = now; SpellFx.fx(FX_AURA, new Vector3d(pos).add(0, 2.1, 0), 0.6f, store, cb); }
            return now - startAt >= DURATION_MS || (dodgesLeft <= 0 && dodged.isEmpty() && !riposteReady);
        }

        /** Pas de cote perpendiculaire a l'attaquant (gauche ou droite, la premiere case libre), remanence, attaquant fige. */
        private void dodge(Vector3d pos, Ref<EntityStore> attacker, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d ap = SpellFx.position(attacker, store);
            Vector3d toward = ap != null ? new Vector3d(ap).sub(pos).setComponent(1, 0) : SpellFx.forward(caster, store);
            if (toward.lengthSquared() < 1e-6) toward.set(0, 0, 1);
            toward.normalize();
            Vector3d side = new Vector3d(-toward.z, 0, toward.x);
            SpellFx.fx(FX_GHOST, new Vector3d(pos).add(0, 1, 0), 1.2f, store, cb);
            if (ThreadLocalRandom.current().nextBoolean()) side.negate();
            for (int i = 0; i < 2; i++, side.negate()) {                     // un cote au hasard, sinon l'autre ; sinon sur place
                Vector3d dest = new Vector3d(pos).add(new Vector3d(side).mul(STEP));
                if (free(dest)) { SpellFx.teleport(caster, dest, store, cb); break; }
            }
            SpellFx.fx(FX_DODGE, new Vector3d(pos).add(0, 1, 0), 0.9f, store, cb);
            SpellFx.sound(SFX_DODGE, pos, cb);
            if (ap != null && !RuneSpellRuntime.isSummon(attacker)) SpellFx.stun(attacker, FREEZE, FREEZE_MS, store, cb);
            riposteReady = true;
        }

        private boolean free(Vector3d feet) {
            if (world == null) return false;
            int x = (int) Math.floor(feet.x), y = (int) Math.floor(feet.y), z = (int) Math.floor(feet.z);
            return passable(x, y, z) && passable(x, y + 1, z) && !passable(x, y - 1, z);   // pas de pas dans le vide
        }

        private boolean passable(int x, int y, int z) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            return t != null && t.getMaterial() != BlockMaterial.Solid;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (!caster.isValid()) return;
            try { EffectsManager.removeEffect(caster, VEIL, cb); } catch (Exception e) { EldaniorLogger.error("Prescience: effet", e); }
        }
    }
}
