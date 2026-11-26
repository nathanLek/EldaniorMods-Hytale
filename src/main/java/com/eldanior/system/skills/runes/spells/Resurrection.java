package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Rune de la Resurrection (Lumiere, Unique, rune de predilection du Genese Mage) : le lanceur et ses allies a 15 blocs
 * sont benis 15 s (protection RuneSpellRuntime.ward). Le premier coup mortel d'un beni est annule : PV = max(actuels,
 * 50 %), malus purges, 2 s de coups annules ; une onde sacree frappe les ennemis a 5 blocs (150, x2 morts-vivants).
 * Chaque beni n'est releve qu'une fois. Ranimer un joueur deja mort n'a pas d'API verifiee : le sort agit avant la mort.
 * Fiche de conception : scripts/runes/specs/Rune_Resurrection.json
 */
public final class Resurrection implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 15_000, GRACE_MS = 2_000, GLOW_EVERY_MS = 1_000;
    private static final double RADIUS = 15.0, WAVE_RADIUS = 5.0, WAVE_PUSH = 8.0;
    private static final float REVIVE_HEALTH = 0.5f, WAVE = 150f, UNDEAD_MULTIPLIER = 2f;
    private static final String[] UNDEAD = {"Skeleton", "Zombie", "Undead", "Ghoul", "Wraith", "Mummy", "Lich", "Ghost"};

    // ===== Assets =====
    private static final String FX_BLESS = "Bifrost_Divine", FX_PILLAR = "Bifrost_Pillar", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_REVIVE = "SFX_Divine_Respawn";

    @Override public String id() { return "Resurrection"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        Genesis g = new Genesis(System.currentTimeMillis());
        for (Ref<EntityStore> a : SpellFx.alliesAround(caster, pos, RADIUS, store)) {
            Blessing b = new Blessing(g);
            g.blessed.put(a, b);
            RuneSpellRuntime.ward(a, b);
            Vector3d p = SpellFx.position(a, store);
            if (p != null) SpellFx.fx(FX_BLESS, new Vector3d(p).add(0, 1, 0), 1.2f, store, cb);
        }
        SpellFx.sound(SFX_REVIVE, pos, cb);
        return g;
    }

    /** Benediction d'un allie : un seul relevement, puis 2 s de grace. */
    private static final class Blessing implements RuneSpellRuntime.Ward {
        final Genesis owner;
        volatile boolean used;
        volatile long graceUntil;

        Blessing(Genesis owner) { this.owner = owner; }

        @Override
        public float onHurt(Ref<EntityStore> target, Ref<EntityStore> attacker, float amount) {
            return System.currentTimeMillis() < graceUntil ? 0 : amount;
        }

        @Override
        public boolean preventDeath(Ref<EntityStore> target, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (used) return false;
            used = true;
            graceUntil = System.currentTimeMillis() + GRACE_MS;
            try {
                EntityStatMap map = store.getComponent(target, EntityStatMap.getComponentType());
                EntityStatValue hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
                if (hp != null) map.setStatValue(DefaultEntityStatTypes.getHealth(), Math.max(hp.get(), hp.getMax() * REVIVE_HEALTH));
                EffectsManager.cureAllDebuffs(target, cb);
            } catch (Exception e) {
                EldaniorLogger.error("Resurrection: relevement", e);
            }
            owner.revived.add(target);                                            // colonne de lumiere et onde : au tick du lanceur
            return true;
        }
    }

    private static final class Genesis implements Instance {
        final long startAt;
        final Map<Ref<EntityStore>, Blessing> blessed = new HashMap<>();
        final Queue<Ref<EntityStore>> revived = new ConcurrentLinkedQueue<>();
        long lastGlow;

        Genesis(long startAt) { this.startAt = startAt; }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Ref<EntityStore> r;
            while ((r = revived.poll()) != null) rise(caster, r, store, cb);
            if (now - startAt >= DURATION_MS && blessed.values().stream().noneMatch(b -> now < b.graceUntil)) return true;
            if (now - lastGlow >= GLOW_EVERY_MS && now - startAt < DURATION_MS) {
                lastGlow = now;
                for (Map.Entry<Ref<EntityStore>, Blessing> en : blessed.entrySet()) {
                    if (en.getValue().used) continue;
                    Vector3d p = SpellFx.position(en.getKey(), store);
                    if (p != null) SpellFx.fx(FX_BLESS, new Vector3d(p).add(0, 2.2, 0), 0.5f, store, cb);
                }
            }
            if (now - startAt >= DURATION_MS)                                     // fin : seules les graces en cours restent
                blessed.forEach((a, b) -> { if (now >= b.graceUntil) RuneSpellRuntime.unward(a, b); });
            return false;
        }

        /** Le beni se releve dans une colonne de lumiere ; l'onde sacree repousse ses ennemis. */
        private void rise(Ref<EntityStore> caster, Ref<EntityStore> ally, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Vector3d p = SpellFx.position(ally, store);
            if (p == null) return;
            SpellFx.fx(FX_PILLAR, p, 0.6f, store, cb);
            SpellFx.fx(FX_HEAL, p, 1.2f, store, cb);
            SpellFx.sound(SFX_REVIVE, p, cb);
            for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, p, WAVE_RADIUS, store)) {
                Vector3d q = SpellFx.position(e, store);
                if (q == null) continue;
                Vector3d out = new Vector3d(q.x - p.x, 0, q.z - p.z);
                if (out.lengthSquared() < 1e-4) out.set(1, 0, 0);
                SpellFx.damage(caster, e, "Elemental", isUndead(e, store) ? WAVE * UNDEAD_MULTIPLIER : WAVE, out.normalize().mul(WAVE_PUSH).add(0, 3, 0), "Impact_Strong", cb);
                SpellFx.fx(FX_BLESS, new Vector3d(q).add(0, 1, 0), 0.6f, store, cb);
            }
        }

        private static boolean isUndead(Ref<EntityStore> e, Store<EntityStore> store) {
            NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
            String type = npc != null ? npc.getNPCTypeId() : null;
            if (type == null) return false;
            for (String k : UNDEAD) if (type.contains(k)) return true;
            return false;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) { blessed.forEach(RuneSpellRuntime::unward); }

        @Override
        public void abort() { blessed.forEach(RuneSpellRuntime::unward); }
    }
}
