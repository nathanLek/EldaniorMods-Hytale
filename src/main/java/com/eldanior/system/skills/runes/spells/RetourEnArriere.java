package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rune du Retour en Arriere (Temps, Unique, rune de predilection de l'Oracle Absolu) : pose une ancre temporelle (position
 * et PV). 5 s plus tard le temps se rembobine : le lanceur revient a l'ancre avec max(PV actuels, PV de l'ancre), et chaque
 * coup porte entre-temps frappe une seconde fois a 50 % (markEcho). Un coup mortel pendant les 5 s declenche le retour
 * immediatement (preventOwnerDeath), avec au moins 20 % de PV et 1 s d'invulnerabilite.
 * Fiche de conception : scripts/runes/specs/Rune_RetourEnArriere.json
 */
public final class RetourEnArriere implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 5_000, GRACE_MS = 1_000, GHOST_EVERY_MS = 200, TICK_SOUND_EVERY_MS = 1_000, FALL_GUARD_MS = 2_000;
    private static final float ECHO = 0.5f, DEATH_FLOOR = 0.20f;
    private static final int MAX_ECHOES = 20;

    // ===== Assets =====
    private static final String FX_GHOST = "Chronos_Afterimage", FX_GOLD = "Chronos_Gold", FX_BURST = "Chronos_Burst", FX_DASH = "Chronos_Dash";
    private static final String SFX_TICK = "SFX_Arcane_Workbench_Craft", SFX_REWIND = "SFX_Crystal_Break";

    @Override public String id() { return "RetourEnArriere"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        float hp = 0;
        EntityStatMap map = store.getComponent(caster, EntityStatMap.getComponentType());
        EntityStatValue v = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
        if (v != null) hp = v.get();
        SpellFx.fx(FX_BURST, new Vector3d(pos).add(0, 1, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_TICK, pos, cb);
        return new Anchor(pos, hp, SpellFx.world(store), System.currentTimeMillis());
    }

    private record Hit(Ref<EntityStore> target, float amount) {}

    private static final class Anchor implements Instance {
        final Vector3d anchor;
        final float anchorHp;
        final World world;
        final long startAt;
        final Queue<Hit> hits = new ConcurrentLinkedQueue<>();
        final AtomicInteger hitCount = new AtomicInteger();
        volatile boolean rewound;
        volatile long graceUntil;
        long lastGhost, lastSound;

        Anchor(Vector3d anchor, float anchorHp, World world, long startAt) {
            this.anchor = anchor; this.anchorHp = anchorHp; this.world = world; this.startAt = startAt;
        }

        @Override
        public void onOwnerHit(Ref<EntityStore> target, float amount) {
            if (!rewound && amount > 0 && hitCount.getAndIncrement() < MAX_ECHOES) hits.add(new Hit(target, amount));
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return rewound && System.currentTimeMillis() < graceUntil ? 0 : amount;
        }

        @Override
        public boolean preventOwnerDeath(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (rewound) return false;
            rewind(caster, DEATH_FLOOR, store, cb);
            return true;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (SpellFx.position(caster, store) == null) return true;
            if (rewound) { replay(caster, store, cb); return now >= graceUntil; }
            if (now - startAt >= DURATION_MS) { rewind(caster, 0f, store, cb); replay(caster, store, cb); return false; }
            if (now - lastGhost >= GHOST_EVERY_MS) {                        // silhouette a l'ancre : on voit ou l'on reviendra
                lastGhost = now;
                SpellFx.fx(FX_GHOST, new Vector3d(anchor).add(0, 1, 0), 1.2f, store, cb);
            }
            if (now - lastSound >= TICK_SOUND_EVERY_MS) {
                lastSound = now;
                SpellFx.fx(FX_GOLD, new Vector3d(anchor).add(0, 2.3, 0), 0.6f, store, cb);
                SpellFx.sound(SFX_TICK, anchor, cb);
            }
            return false;
        }

        /** Retour a l'ancre (case libre requise, sinon sur place) et PV de l'ancre si plus eleves (au moins floor x max). */
        private void rewind(Ref<EntityStore> caster, float floor, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            rewound = true;
            graceUntil = System.currentTimeMillis() + GRACE_MS;
            Vector3d from = SpellFx.position(caster, store);
            if (from != null) {                                             // trainee du present vers l'ancre
                SpellFx.fx(FX_BURST, new Vector3d(from).add(0, 1, 0), 1.0f, store, cb);
                for (int i = 1; i <= 6; i++) SpellFx.fx(FX_DASH, new Vector3d(from).lerp(anchor, i / 7.0).add(0, 1, 0), 0.8f, store, cb);
            }
            if (free(anchor)) {
                SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
                SpellFx.teleport(caster, anchor, store, cb);
            }
            try {
                EntityStatMap map = store.getComponent(caster, EntityStatMap.getComponentType());
                EntityStatValue hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
                if (hp != null) map.setStatValue(DefaultEntityStatTypes.getHealth(), Math.max(Math.max(hp.get(), Math.min(anchorHp, hp.getMax())), hp.getMax() * floor));
            } catch (Exception e) {
                EldaniorLogger.error("RetourEnArriere: PV", e);
            }
            SpellFx.fx(FX_BURST, new Vector3d(anchor).add(0, 1, 0), 1.8f, store, cb);
            SpellFx.sound(SFX_REWIND, anchor, cb);
        }

        /** Les coups portes pendant les 5 s frappent une seconde fois. */
        private void replay(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            Hit h;
            while ((h = hits.poll()) != null) {
                Vector3d p = SpellFx.position(h.target(), store);
                float echo = h.amount() * ECHO;
                if (p == null || echo < 0.5f) continue;
                RuneSpellRuntime.markEcho(h.target(), echo);
                SpellFx.damage(caster, h.target(), "Elemental", echo, null, "Impact_Light", cb);
                SpellFx.fx(FX_GHOST, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
            }
        }

        private boolean free(Vector3d feet) {
            if (world == null) return false;
            int x = (int) Math.floor(feet.x), y = (int) Math.floor(feet.y), z = (int) Math.floor(feet.z);
            return passable(x, y, z) && passable(x, y + 1, z);
        }

        private boolean passable(int x, int y, int z) {
            BlockType t = WorldBlockUtil.getBlockType(world, x, y, z);
            return t != null && t.getMaterial() != BlockMaterial.Solid;
        }
    }
}
