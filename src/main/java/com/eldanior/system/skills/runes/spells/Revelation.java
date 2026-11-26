package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.skills.system.InvisibilityManager;
import com.eldanior.system.util.NpcAttitudes;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import org.joml.Vector3d;

import java.util.List;
import java.util.UUID;

/**
 * Rune de la Revelation (Lumiere, Unique, rune de predilection du Prophete) : onde de verite instantanee sur 20 blocs.
 * Ennemis : 100 (x2 morts-vivants), Elda_Revele 12 s et vulnerabilite x1,3 de toutes sources (RuneSpellRuntime.expose,
 * lue par DivineDamageSystem). Joueurs ennemis invisibles : devoiles. Monstres : eblouis 2 s (attitude IGNORE envers les
 * joueurs proches, cible verrouillee effacee).
 * Fiche de conception : scripts/runes/specs/Rune_Revelation.json
 */
public final class Revelation implements RuneSpell {

    // ===== Reglages (equilibrage) =====
    private static final long REVEAL_MS = 12_000;
    private static final double RADIUS = 20.0, DAZZLE_S = 2.0;
    private static final float DAMAGE = 100f, UNDEAD_MULTIPLIER = 2f, VULNERABILITY = 1.3f;
    private static final String[] UNDEAD = {"Skeleton", "Zombie", "Undead", "Ghoul", "Wraith", "Mummy", "Lich", "Ghost"};

    // ===== Assets =====
    private static final String REVEALED = "Elda_Revele";
    private static final String FX_LIGHT = "Bifrost_Divine", FX_PILLAR = "Bifrost_Pillar";
    private static final String SFX_WAVE = "SFX_Divine_Respawn";

    @Override public String id() { return "Revelation"; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        long until = System.currentTimeMillis() + REVEAL_MS;
        List<Ref<EntityStore>> witnesses = SpellFx.playersNear(pos, RADIUS * 2, store);
        for (Ref<EntityStore> e : SpellFx.enemiesAround(caster, pos, RADIUS, store)) {
            Vector3d p = SpellFx.position(e, store);
            if (p == null || RuneSpellRuntime.isSummon(e)) continue;
            NPCEntity npc = store.getComponent(e, NPCEntity.getComponentType());
            SpellFx.damage(caster, e, "Elemental", isUndead(npc) ? DAMAGE * UNDEAD_MULTIPLIER : DAMAGE, null, "Impact_Light", cb);
            SpellFx.effect(e, REVEALED, cb);
            RuneSpellRuntime.expose(e, VULNERABILITY, until);
            SpellFx.fx(FX_LIGHT, new Vector3d(p).add(0, 1, 0), 0.8f, store, cb);
            if (store.getComponent(e, Player.getComponentType()) != null) {   // rien ne se cache devant la verite
                UUID uuid = SpellFx.uuid(e, store);
                if (uuid != null && InvisibilityManager.isInvisible(uuid)) InvisibilityManager.removeInvisibility(uuid, e, cb);
            } else if (npc != null) {                                          // monstres eblouis : ils perdent leur cible
                for (Ref<EntityStore> w : witnesses) NpcAttitudes.force(e, w, Attitude.IGNORE, DAZZLE_S, store);
                CloneFx.mark(e, "LockedTarget", null, store);
            }
        }
        SpellFx.fx(FX_PILLAR, pos, 0.7f, store, cb);
        for (double r = 5; r <= RADIUS; r += 5)
            for (int i = 0; i < 16; i++) {
                double a = i * Math.PI / 8 + r * 0.1;
                SpellFx.fx(FX_LIGHT, new Vector3d(pos).add(Math.cos(a) * r, 0.8, Math.sin(a) * r), 0.6f, store, cb);
            }
        SpellFx.sound(SFX_WAVE, pos, cb);
        SpellFx.shake(pos, RADIUS + 5, "Impact_Light", store);
        return null;
    }

    private static boolean isUndead(NPCEntity npc) {
        String type = npc != null ? npc.getNPCTypeId() : null;
        if (type == null) return false;
        for (String k : UNDEAD) if (type.contains(k)) return true;
        return false;
    }
}
