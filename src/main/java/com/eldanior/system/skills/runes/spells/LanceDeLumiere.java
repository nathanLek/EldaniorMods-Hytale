package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.util.WorldBlockUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.Set;

/**
 * Rune de la Lance de Lumiere (Lumiere, Rare) : rayon sacre instantane de 30 blocs (arrete par le premier mur). Les
 * ennemis traverses sont blesses (double sur les morts-vivants, reconnus au nom de leur role), les allies traverses soignes.
 * Fiche de conception : scripts/runes/specs/Rune_LanceDeLumiere.json
 */
public final class LanceDeLumiere implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 30.0, WIDTH = 1.4, STEP = 0.5;
    private static final float DAMAGE = 110f, UNDEAD_MULTIPLIER = 2f, HEAL = 0.10f;
    private static final String[] UNDEAD = {"Skeleton", "Zombie", "Undead", "Ghoul", "Wraith", "Mummy", "Lich", "Ghost"};

    // ===== Assets =====
    private static final String FX_BEAM = "Bifrost_Divine", FX_HEAL = "Apollo_Heal_Pulse";
    private static final String SFX_BEAM = "SFX_Portal_Void";

    @Override public String id() { return "LanceDeLumiere"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        World world = SpellFx.world(store);
        Vector3d dir = SpellFx.lookDirection(caster, store);
        Vector3d from = new Vector3d(pos).add(0, 1.5, 0);
        Vector3d to = new Vector3d(from);
        for (double d = 0; d < RANGE; d += STEP) {                                   // le rayon s'arrete au premier mur
            Vector3d next = new Vector3d(from).add(new Vector3d(dir).mul(d + STEP));
            int x = (int) Math.floor(next.x), y = (int) Math.floor(next.y), z = (int) Math.floor(next.z);
            if (WorldBlockUtil.getBlockType(world, x, y, z) == null || !TerrainRestore.isEmpty(world, x, y, z)) break;
            to = next;
        }
        for (double d = 1; d <= from.distance(to); d += 1.0)
            SpellFx.fx(FX_BEAM, new Vector3d(from).add(new Vector3d(dir).mul(d)), 0.3f, store, cb);
        SpellFx.sound(SFX_BEAM, pos, cb);

        Vector3d mid = new Vector3d(from).add(to).mul(0.5);
        double reach = from.distance(to) / 2 + WIDTH + 1;
        Set<Ref<EntityStore>> seen = new HashSet<>();
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, mid, reach, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || !seen.add(e) || distanceToSegment(p.add(0, 1, 0), from, to) > WIDTH) continue;
            SpellFx.damage(caster, e, "Elemental", isUndead(e, store) ? DAMAGE * UNDEAD_MULTIPLIER : DAMAGE, new Vector3d(dir).mul(3), "Impact_Light", cb);
            SpellFx.fx(FX_BEAM, p, 0.6f, store, cb);
        }
        for (Ref<EntityStore> a : SpellFx.alliesAround(caster, mid, reach, store)) {
            Vector3d p = SpellFx.position(a, store);
            if (p == null || !seen.add(a) || distanceToSegment(p.add(0, 1, 0), from, to) > WIDTH) continue;
            SpellFx.restore(a, DefaultEntityStatTypes.getHealth(), HEAL, cb);
            SpellFx.fx(FX_HEAL, p, 1.0f, store, cb);
        }
        return null;
    }

    private static boolean isUndead(Ref<EntityStore> e, Store<EntityStore> store) {
        NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
        String type = npc != null ? npc.getNPCTypeId() : null;
        if (type == null) return false;
        for (String k : UNDEAD) if (type.contains(k)) return true;
        return false;
    }

    private static double distanceToSegment(Vector3d p, Vector3d a, Vector3d b) {
        Vector3d ab = new Vector3d(b).sub(a);
        double len2 = ab.lengthSquared();
        double t = len2 < 1e-9 ? 0 : Math.max(0, Math.min(1, new Vector3d(p).sub(a).dot(ab) / len2));
        return p.distance(new Vector3d(a).add(ab.mul(t)));
    }
}
