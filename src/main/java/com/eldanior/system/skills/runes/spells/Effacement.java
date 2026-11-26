package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.config.configs.Mobs.MobVirtualHPSystem;
import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de l'Effacement (Tenebres, Unique, rune de predilection du Mage Void Absolu) : apres 1 s d'incantation, un rayon de
 * vide perce tout sur 30 blocs (arrete par le premier bloc). Chaque ennemi touche perd 25 % de ses PV max (PV virtuels
 * pour les monstres : MobVirtualHPSystem, la reduction par niveau ramene ensuite au moins ce % reel), borne 150-2 000 ;
 * s'il devait tomber sous 10 %, ses PV restants sont effaces en plus (meme borne).
 * Fiche de conception : scripts/runes/specs/Rune_Effacement.json
 */
public final class Effacement implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 30.0, STEP = 0.5, WIDTH = 1.5;
    private static final float PERCENT = 0.25f, ERASE_BELOW = 0.10f, MIN_DAMAGE = 150f, MAX_DAMAGE = 2_000f;

    // ===== Assets =====
    private static final String FX_BEAM = "Nyx_Drain_Burst", FX_HIT = "Nyx_Void", FX_ERASE = "Chaos_Flash";
    private static final String SFX_BEAM = "SFX_Portal_Void", SFX_ERASE = "SFX_Void_Eviction";

    @Override public String id() { return "Effacement"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        World world = SpellFx.world(store);
        if (pos == null || world == null) return null;
        Vector3d dir = SpellFx.lookDirection(caster, store);
        Vector3d from = new Vector3d(pos).add(0, 1.5, 0);
        Vector3d to = new Vector3d(from);
        for (double d = 0; d < RANGE; d += STEP) {                                   // le rayon s'arrete au premier mur
            Vector3d next = new Vector3d(from).add(new Vector3d(dir).mul(d + STEP));
            int x = (int) Math.floor(next.x), y = (int) Math.floor(next.y), z = (int) Math.floor(next.z);
            if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) break;
            to = next;
        }
        for (double d = 1; d <= from.distance(to); d += 1.5)
            SpellFx.fx(FX_BEAM, new Vector3d(from).add(new Vector3d(dir).mul(d)), 0.6f, store, cb);
        SpellFx.sound(SFX_BEAM, pos, cb);

        Vector3d mid = new Vector3d(from).add(to).mul(0.5);
        double reach = from.distance(to) / 2 + WIDTH + 1;
        Set<Ref<EntityStore>> seen = new HashSet<>();
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, mid, reach, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || !seen.add(e) || RuneSpellRuntime.isSummon(e) || distanceToSegment(new Vector3d(p).add(0, 1, 0), from, to) > WIDTH) continue;
            erase(caster, e, p, store, cb);
        }
        return null;
    }

    /** Retire 25 % des PV max (virtuels) ; efface ce qui resterait sous 10 %. */
    private static void erase(Ref<EntityStore> caster, Ref<EntityStore> e, Vector3d p, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        EntityStatMap stats = store.getComponent(e, EntityStatsModule.get().getEntityStatMapComponentType());
        var health = stats != null ? stats.get(DefaultEntityStatTypes.getHealth()) : null;
        if (health == null || health.getMax() <= 0) return;
        float realMax = health.getMax(), fraction = health.get() / realMax;
        float max = realMax;
        MobLevelData mob = store.getComponent(e, EldaniorSystem.get().getMobLevelDataType());
        if (mob != null && mob.isStatsApplied()) max = MobVirtualHPSystem.getVirtualMaxHP(mob.getLevel(), realMax);
        float amount = clamp(max * PERCENT);
        boolean erased = fraction - amount / max <= ERASE_BELOW;
        if (erased) amount = clamp(amount + max * Math.max(0f, fraction - amount / max));
        SpellFx.damage(caster, e, "Elemental", amount, null, erased ? "Impact_Strong" : "Impact_Light", cb);
        SpellFx.fx(FX_HIT, new Vector3d(p).add(0, 0.5, 0), 1.0f, store, cb);
        if (erased) {
            SpellFx.fx(FX_ERASE, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
            SpellFx.sound2dNear(SFX_ERASE, p, 20, store);
        }
    }

    private static float clamp(float v) { return Math.max(MIN_DAMAGE, Math.min(MAX_DAMAGE, v)); }

    private static double distanceToSegment(Vector3d p, Vector3d a, Vector3d b) {
        Vector3d ab = new Vector3d(b).sub(a);
        double len2 = ab.lengthSquared();
        double t = len2 < 1e-9 ? 0 : Math.max(0, Math.min(1, new Vector3d(p).sub(a).dot(ab) / len2));
        return p.distance(new Vector3d(a).add(ab.mul(t)));
    }
}
