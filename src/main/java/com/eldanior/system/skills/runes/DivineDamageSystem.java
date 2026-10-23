package com.eldanior.system.skills.runes;

import com.eldanior.system.skills.runes.spells.NiflheimGate;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Modificateurs de degats des sorts divins :
 *  - Porte de Niflheim, « Briser la glace » : un ennemi emprisonne subit +50 % (hors glace) et sa prison explose ;
 *  - Voile de Nyx : le lanceur dans son dome de nuit inflige +50 % ;
 *  - Sablier de Chronos : les degats sur une cible figee sont mis en reserve ;
 *  - Sanctuaire d'Apollon : les allies dans le sanctuaire subissent -20 % et infligent +15 % ; Grace d'Apollon
 *    (un coup mortel est annule une fois) et invulnerabilite qui suit ;
 *  - Eveil d'Yggdrasil : les allies benis dans le jardin de l'arbre-monde subissent -20 %.
 *  - Sorts de rune du moteur commun : chaque coup inflige est signale (RuneSpellRuntime.onDamageDealt), ex. Doppelganger.
 */
public class DivineDamageSystem extends DamageEventSystem {

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Damage damage) {
        if (damage.isCancelled() || damage.getAmount() <= 0) return;
        Ref<EntityStore> target = chunk.getReferenceTo(index);
        if (target == null || !target.isValid()) return;
        String cause = damage.getCause() != null ? damage.getCause().getId() : null;
        // Sanctuaire d'Apollon : invulnerabilite de la Grace, puis Grace si le coup est mortel
        if (com.eldanior.system.skills.runes.spells.ApolloSanctuary.isInvulnerable(target)) { damage.setCancelled(true); return; }
        // Sablier de Chronos : un coup sur une cible figee est mis en reserve (il s'abattra a la reprise du temps)
        if (com.eldanior.system.skills.runes.spells.ChronosHourglass.absorb(target, damage.getAmount())) { damage.setCancelled(true); return; }
        // Invocations des sorts de rune (clones, double) : pas de tir ami avec leur lanceur, double intouchable
        Ref<EntityStore> attacker = damage.getSource() instanceof Damage.EntitySource es ? es.getRef() : null;
        if (com.eldanior.system.skills.runes.spells.RuneSpellRuntime.blocksDamage(attacker, target, damage.getAmount(), store)) { damage.setCancelled(true); return; }
        if (com.eldanior.system.guards.GuardManager.blocksFriendlyFire(attacker, target, store)) { damage.setCancelled(true); return; }   // joueur <-> son aventurier
        if (damage.getSource() instanceof Damage.EntitySource hitter && hitter.getRef() != null)          // sorts de rune qui copient les coups du lanceur
            com.eldanior.system.skills.runes.spells.RuneSpellRuntime.onDamageDealt(hitter.getRef(), target, damage.getAmount());
        float multiplier = NiflheimGate.onDamage(target, cause);
        if (damage.getSource() instanceof Damage.EntitySource source && source.getRef() != null) {
            multiplier *= com.eldanior.system.skills.runes.spells.NyxVeil.damageMultiplier(source.getRef());
            multiplier *= com.eldanior.system.skills.runes.spells.ApolloSanctuary.damageDealtMultiplier(source.getRef());
            multiplier *= com.eldanior.system.skills.runes.spells.DragonAvatar.damageDealtMultiplier(source.getRef());
        }
        multiplier *= com.eldanior.system.skills.runes.spells.ApolloSanctuary.damageTakenMultiplier(target);
        multiplier *= com.eldanior.system.skills.runes.spells.DragonAvatar.damageTakenMultiplier(target);
        multiplier *= com.eldanior.system.skills.runes.spells.YggdrasilAwakening.damageTakenMultiplier(target);
        multiplier *= com.eldanior.system.skills.runes.spells.NyxVeil.damageTakenMultiplier(target);
        if (multiplier != 1f) damage.setAmount(damage.getAmount() * multiplier);
        if (com.eldanior.system.skills.runes.spells.ApolloSanctuary.tryGrace(target, damage.getAmount(), store, commandBuffer)) damage.setCancelled(true);
    }

    // Meme groupe que les autres filtres de degats du mod (FallDamageSystem)
    private static SystemGroup<EntityStore> damageGroup = null;

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        if (damageGroup != null) return damageGroup;
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
            damageGroup = (SystemGroup<EntityStore>) mod.getMethod("getFilterDamageGroup").invoke(inst);
            return damageGroup;
        } catch (Throwable e) {
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}
