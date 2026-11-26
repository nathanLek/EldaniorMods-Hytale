package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

/**
 * Rune du Miracle (Lumiere, Rare, rune de predilection du Thaumaturge), instantanee : soin de 40 % des PV max sur l'allie
 * vise (20 blocs) ou le lanceur. La part du soin qui depasse les PV max explose en lumiere sacree (rayon 4) :
 * 180 x (debordement / 40 %), double sur les morts-vivants ; ignoree sous 20 degats.
 * Fiche de conception : scripts/runes/specs/Rune_Miracle.json
 */
public final class Miracle implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final double RANGE = 20.0, BLAST_RADIUS = 4.0;
    private static final float HEAL = 0.40f, BLAST_MAX = 180f, BLAST_MIN = 20f, UNDEAD_MULTIPLIER = 2f;
    private static final String[] UNDEAD = {"Skeleton", "Zombie", "Undead", "Ghoul", "Wraith", "Mummy", "Lich", "Ghost"};

    // ===== Assets =====
    private static final String FX_HEAL = "Apollo_Heal_Pulse", FX_LIGHT = "Bifrost_Divine", FX_FLASH = "Chaos_Flash";
    private static final String SFX_HEAL = "SFX_Deployable_Totem_Heal_Spawn", SFX_BLAST = "SFX_Portal_Void";

    @Override public String id() { return "Miracle"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (SpellFx.position(caster, store) == null) return null;
        Ref<EntityStore> aimed = SpellFx.targetLiving(caster, store, RANGE);
        Ref<EntityStore> target = aimed != null && SpellFx.isAlly(caster, aimed, store) ? aimed : caster;
        Vector3d p = SpellFx.position(target, store);
        EntityStatMap map = store.getComponent(target, EntityStatMap.getComponentType());
        EntityStatValue hp = map != null ? map.get(DefaultEntityStatTypes.getHealth()) : null;
        if (p == null || hp == null || hp.getMax() <= 0) return null;
        float heal = hp.getMax() * HEAL, missing = Math.max(0, hp.getMax() - hp.get());
        SpellFx.heal(target, Math.min(heal, missing), store);
        SpellFx.fx(FX_HEAL, p, 1.4f, store, cb);
        SpellFx.fx(FX_LIGHT, new Vector3d(p).add(0, 2.2, 0), 1.2f, store, cb);
        SpellFx.sound(SFX_HEAL, p, cb);

        float blast = BLAST_MAX * Math.max(0, heal - missing) / heal;            // le soin qui deborde devient colere sacree
        if (blast < BLAST_MIN) return null;
        Vector3d center = new Vector3d(p).add(0, 1, 0);
        SpellFx.fx(FX_LIGHT, center, 2.5f, store, cb);
        SpellFx.fx(FX_FLASH, center, 0.8f, store, cb);
        SpellFx.sound(SFX_BLAST, p, cb);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, center, BLAST_RADIUS, store)) {
            Vector3d q = SpellFx.position(e, store);
            if (q == null) continue;
            Vector3d away = new Vector3d(q.x - p.x, 0, q.z - p.z);
            if (away.lengthSquared() < 1e-6) away.set(1, 0, 0);
            SpellFx.damage(caster, e, "Elemental", isUndead(e, store) ? blast * UNDEAD_MULTIPLIER : blast, away.normalize().mul(6).add(0, 3, 0), "Impact_Light", cb);
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
}
