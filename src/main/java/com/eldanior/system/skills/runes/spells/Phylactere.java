package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * Rune du Phylactere (Tenebres, Epic) : pose un phylactere de cristal (vrai bloc, restaure) aux pieds du lanceur pour
 * 60 s. Un coup mortel pendant ce temps est annule (RuneSpell.Instance.preventOwnerDeath) : le lanceur renait sur le
 * phylactere avec 30 % de PV et 1,5 s d'invulnerabilite, et le phylactere se brise.
 * Fiche de conception : scripts/runes/specs/Rune_Phylactere.json
 */
public final class Phylactere implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 60_000, GRACE_MS = 1_500, GLOW_EVERY_MS = 2_000, FALL_GUARD_MS = 3_000;
    private static final float REVIVE_HEALTH = 0.30f;

    // ===== Assets =====
    private static final String CRYSTAL = "Rock_Crystal_Purple_Large";
    private static final String FX_GLOW = "Nyx_Void", FX_REVIVE = "Nyx_Chain_Burst";
    private static final String SFX_PLACE = "SFX_Bear_Voidtaken_Tentacle", SFX_REVIVE = "SFX_Bear_Voidtaken_Roar";

    @Override public String id() { return "Phylactere"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        World world = SpellFx.world(store);
        long now = System.currentTimeMillis();
        int x = (int) Math.floor(pos.x), y = (int) Math.floor(pos.y), z = (int) Math.floor(pos.z);
        TerrainRestore.Restore stone = TerrainRestore.open(world, now + DURATION_MS);
        world.execute(() -> {                                                          // visuel seulement : case vide requise
            try {
                if (TerrainRestore.isEmpty(world, x, y, z) && !TerrainRestore.isProtectedArea(world.getName(), x, y, z))
                    TerrainRestore.place(world, stone, x, y, z, TerrainRestore.EMPTY, CRYSTAL, 0);
                TerrainRestore.save();
            } catch (Exception e) {
                EldaniorLogger.error("Phylactere: cristal", e);
            }
        });
        SpellFx.fx(FX_GLOW, pos, 1.0f, store, cb);
        SpellFx.sound(SFX_PLACE, pos, cb);
        return new Soul(new Vector3d(x + 0.5, y, z + 0.5), world, stone, now);
    }

    private static final class Soul implements Instance {
        final Vector3d anchor;
        final World world;
        final TerrainRestore.Restore stone;
        final long startAt;
        long lastGlow, graceUntil;
        boolean used;

        Soul(Vector3d anchor, World world, TerrainRestore.Restore stone, long startAt) {
            this.anchor = anchor; this.world = world; this.stone = stone; this.startAt = startAt;
        }

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (used) return now >= graceUntil;
            if (now - startAt >= DURATION_MS) return true;
            if (now - lastGlow >= GLOW_EVERY_MS) { lastGlow = now; SpellFx.fx(FX_GLOW, new Vector3d(anchor).add(0, 0.5, 0), 0.6f, store, cb); }
            return false;
        }

        @Override
        public float onOwnerHurt(Ref<EntityStore> attacker, float amount, String causeId) {
            return used && System.currentTimeMillis() < graceUntil ? 0 : amount;
        }

        /** Le coup mortel est annule : le lanceur renait sur le phylactere, qui se brise. */
        @Override
        public boolean preventOwnerDeath(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
            if (used) return false;
            used = true;
            graceUntil = System.currentTimeMillis() + GRACE_MS;
            try {
                EntityStatMap map = store.getComponent(caster, EntityStatMap.getComponentType());
                EntityStatValue hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
                if (hp != null) map.setStatValue(DefaultEntityStatTypes.getHealth(), Math.max(hp.get(), hp.getMax() * REVIVE_HEALTH));
            } catch (Exception e) {
                EldaniorLogger.error("Phylactere: renaissance", e);
            }
            Vector3d from = SpellFx.position(caster, store);
            if (from != null) SpellFx.fx(FX_GLOW, new Vector3d(from).add(0, 1, 0), 1.2f, store, cb);
            SpellFx.teleport(caster, new Vector3d(anchor).add(0, 1.05, 0), store, cb);     // au-dessus du cristal (retire juste apres)
            SpellFx.protectFromFall(caster, FALL_GUARD_MS, store);
            SpellFx.fx(FX_REVIVE, anchor, 1.2f, store, cb);
            SpellFx.sound(SFX_REVIVE, anchor, cb);
            world.execute(() -> TerrainRestore.restoreNow(world, stone));               // le phylactere se brise
            return true;
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            world.execute(() -> TerrainRestore.restoreNow(world, stone));
        }

        @Override
        public void abort() {
            TerrainRestore.save();
        }
    }
}
