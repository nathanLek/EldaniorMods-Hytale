package com.eldanior.system.skills.runes.spells;

import com.eldanior.system.config.Effects.EffectsManager;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.UUID;

/**
 * Posture de la Percee (maintien de l'attaque du Bokken, 2026-11-12 : « ameliore l'animation du chargement, associe la couleur
 * de Ki du joueur »). Lancee au debut du maintien (Luo_Bokken_Percee, gen_luo_katanas.py), avant la charge de 0,5 s :
 *  - lame au clair : effet Percee_Posture_T<palier> sur la lame (Ki de la couleur de la classe aspire vers le fil) ;
 *  - au fourreau : anneau de Ki au sol (la lame n'est pas encore en main) ;
 *  - charge pleine (READY_MS) : eclat en etoile Percee_Pret_T<palier> a la pointe, et un souffle.
 * La posture prend fin quand le joueur relache (l'interaction de charge cede la place a la suivante, ou la chaine se ferme),
 * quand la Percee part (Percee.start appelle {@link #release}) ou apres MAX_MS. Les PNJ n'ont pas de posture.
 */
public final class PerceePosture implements RuneSpell {

    static final long READY_MS = 470, MAX_MS = 6_000, SETTLE_MS = 60;
    static final String EFFECT = "Percee_Posture_T", FX_READY = "Percee_Pret_T", FX_RING = "Ki_Anneau_T";

    private final String id;
    private final boolean blade;

    /** blade : lame au clair (effet sur la lame) ; sinon au fourreau. */
    public PerceePosture(String id, boolean blade) {
        this.id = id;
        this.blade = blade;
    }

    @Override public String id() { return id; }

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (MartialKi.NPC_TIER.containsKey(caster)) return null;
        Vector3d feet = SpellFx.position(caster, store);
        if (feet == null) return null;
        UUID uuid = SpellFx.uuid(caster, store);
        Posture old = uuid == null ? null : RuneSpellRuntime.find(uuid, Posture.class);
        if (old != null) old.done = true;
        int tier = BokkenSignature.tier(caster, store);
        Posture p = new Posture(caster, tier, blade, System.currentTimeMillis());
        if (blade) SpellFx.effect(caster, EFFECT + tier, cb);
        SpellFx.fx(FX_RING + tier, new Vector3d(feet).add(0, 0.05, 0), 0.7f, store, cb);
        return p;
    }

    /** La Percee part : la posture s'efface (son effet de lame est retire au tick suivant). */
    static void release(Ref<EntityStore> caster, Store<EntityStore> store) {
        UUID uuid = SpellFx.uuid(caster, store);
        Posture p = uuid == null ? null : RuneSpellRuntime.find(uuid, Posture.class);
        if (p != null) p.done = true;
    }

    static final class Posture implements Instance {
        final Ref<EntityStore> caster;
        final int tier;
        final boolean blade;
        final long startAt;
        boolean done, ready;
        int chainId = Integer.MIN_VALUE, opIndex = Integer.MIN_VALUE;

        Posture(Ref<EntityStore> caster, int tier, boolean blade, long now) {
            this.caster = caster; this.tier = tier; this.blade = blade; this.startAt = now;
        }

        @Override
        public boolean tick(Ref<EntityStore> c, long now, Store<EntityStore> s, CommandBuffer<EntityStore> b) {
            long age = now - startAt;
            if (done || age > MAX_MS || released(c, s, age)) return true;
            if (!ready && age >= READY_MS) {
                ready = true;
                Vector3d feet = SpellFx.position(c, s);
                if (feet != null) {
                    Vector3d look = SpellFx.lookDirection(c, s);
                    Vector3d flat = new Vector3d(look.x, 0, look.z);
                    if (flat.lengthSquared() < 1e-6) flat.set(0, 0, 1);
                    flat.normalize();
                    Vector3d tip = blade ? Percee.at(feet, flat, Percee.TIP_POSTURE)     // pointe de la lame en posture
                            : Percee.at(feet, flat, new double[]{0.2, 0.9, 0.5});             // au fourreau : la main sur la poignee
                    SpellFx.fx(FX_READY + tier, tip, 0.8f + 0.06f * tier, s, b);
                    SpellFx.sound("SFX_Ability_Wind_Strike_Imbue", tip, b);
                }
            }
            return false;
        }

        /**
         * Le joueur a-t-il relache ? La chaine de l'attaque (Primary) passe de la charge a l'interaction suivante (combo ou
         * Percee) : son indice d'operation change ; ou la chaine se ferme. L'indice est lu une fois la charge commencee.
         */
        private boolean released(Ref<EntityStore> c, Store<EntityStore> s, long age) {
            try {
                InteractionManager manager = s.getComponent(c, InteractionModule.get().getInteractionManagerComponent());
                if (manager == null) return false;
                InteractionChain primary = null;
                for (InteractionChain ch : manager.getChains().values())
                    if (ch != null && ch.getType() == InteractionType.Primary) { primary = ch; break; }
                if (primary == null) return age > SETTLE_MS;
                if (age < SETTLE_MS) return false;
                if (chainId == Integer.MIN_VALUE) {
                    chainId = primary.getChainId();
                    opIndex = primary.getOperationIndex();
                    return false;
                }
                return primary.getChainId() != chainId || primary.getOperationIndex() != opIndex;
            } catch (Exception e) {
                return age > READY_MS + 200;                                   // lecture impossible : posture breve
            }
        }

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {
            if (blade && caster != null && caster.isValid()) EffectsManager.removeEffect(caster, EFFECT + tier, cb);
        }
    }
}
