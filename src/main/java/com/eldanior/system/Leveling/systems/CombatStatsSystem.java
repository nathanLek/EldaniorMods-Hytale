package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombatStatsSystem extends DamageEventSystem {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    // Configurable ici : Multiplicateur de dégâts critique (1.8 = +80% dégâts)
    private static final float CRIT_MULTIPLIER = 2f;

    @Override
    public void handle(int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull Damage damage) {

        if (damage.isCancelled()) return;

        Ref<EntityStore> victimRef = archetypeChunk.getReferenceTo(index);
        if (!victimRef.isValid()) return;

        // 1. GESTION DE L'ESQUIVE (Basée sur l'Agilité Totale)
        if (tryDodge(victimRef, store, damage)) {
            return; // Si l'attaque est esquivée, on arrête tout calcul !
        }

        // 2. Gestion de l'Attaquant (Force + Critique + Passifs)
        applyOffensiveStats(damage, store, victimRef);

        if (damage.isCancelled()) return;

        // 3. Gestion de la Victime (Endurance + Passifs)
        applyEnduranceDefense(victimRef, store, damage);
    }

    private boolean tryDodge(Ref<EntityStore> victimRef, Store<EntityStore> store, Damage damage) {
        PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (victimData == null) return false;

        // Calcul de la chance d'esquive : 0.1% par point d'Agilité
        float dodgeChance = victimData.getTotalAgility() * 0.001f;
        if (dodgeChance > 0.60f) dodgeChance = 0.60f; // Cap à 60% d'esquive max

        if (Math.random() < dodgeChance) {
            damage.setCancelled(true);

            // Notification optionnelle pour le joueur
            UUIDComponent uuidComp = store.getComponent(victimRef, UUIDComponent.getComponentType());
            if (uuidComp != null) {
                PlayerRef playerRef = Universe.get().getPlayer(uuidComp.getUuid());
                if (playerRef != null) {
                    NotificationHelper.sendNotification(playerRef, "<color:aqua>Esquive !</color>", NotificationStyle.Success);
                }
            }
            return true;
        }
        return false;
    }

    private void applyOffensiveStats(Damage damage, Store<EntityStore> store, Ref<EntityStore> victimRef) {
        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource entitySource)) return;

        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (!attackerRef.isValid()) return;

        // --- 🌟 NOUVEAU : SI L'ATTAQUANT EST UN MOB ---
        // On lui donne son bonus de dégâts ici, AVANT de calculer la défense du joueur !
        ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();
        if (mobLevelType != null) {
            var mobData = store.getComponent(attackerRef, mobLevelType);
            if (mobData != null && mobData.isStatsApplied()) {
                float damageBonus = mobData.getLevel() * com.eldanior.system.config.configs.MobsWorldConfig.DAMAGE_PER_LEVEL;
                damage.setAmount(damage.getAmount() + damageBonus);
                return; // Le mob n'a pas de force ou de crit de joueur, on s'arrête là !
            }
        }

        // --- SI L'ATTAQUANT EST UN JOUEUR ---
        PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (attackerData == null) return;

        ClassModel attackerClass = ClassManager.get(attackerData.getPlayerClassId());

        // Force
        float strengthBonus = StatConfig.STRENGTH_DAMAGE.getFinalValue(attackerData, attackerClass);
        float currentDamage = damage.getAmount() + strengthBonus;

        // Coup Critique
        if (LuckSystem.isCriticalHit(attackerData)) {
            currentDamage *= CRIT_MULTIPLIER;
            LOGGER.atSevere().log("Coup Critique Donné ===> " + currentDamage);
        }

        damage.setAmount(currentDamage);

        // Passifs offensifs (Sword Mastery, etc.)
        for (PassiveSkill skill : attackerData.getActivePassives()) {
            if (skill.getLogic() != null) {
                // Ajoute "attackerRef" ici entre store et victimRef !
                skill.getLogic().onAttack(damage, attackerData, store, attackerRef, victimRef);
            }
        }
    }

    private void applyEnduranceDefense(Ref<EntityStore> victimRef, Store<EntityStore> store, Damage damage) {
        PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (victimData == null) return;

        ClassModel victimClass = ClassManager.get(victimData.getPlayerClassId());

        // --- C. DEFENSE ENDURANCE ---
        float defense = StatConfig.ENDURANCE_DEFENSE.getFinalValue(victimData, victimClass);
        float currentDamage = damage.getAmount() - defense;

        if (currentDamage < 1) currentDamage = 1;
        damage.setAmount(currentDamage);

        // Récupération de l'attaquant pour l'envoyer au passif défensif (ex: renvoi de dégâts)
        Ref<EntityStore> attackerRef = null;
        if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            attackerRef = entitySource.getRef();
        }

        // --- D. PASSIFS DÉFENSIFS DE CLASSE ---
        for (PassiveSkill skill : victimData.getActivePassives()) {
            if (skill.getLogic() != null) {
                // ✅ On ajoute victimRef à la fin de l'appel !
                // (Si ta variable s'appelle "ref" ou "entityRef" dans cette méthode, mets ça à la place de "victimRef")
                skill.getLogic().onDefend(damage, victimData, store, attackerRef, victimRef);
            }
        }

        // Sécurité finale : un passif défensif n'a pas le droit de soigner l'ennemi (dégâts < 0)
        if (!damage.isCancelled() && damage.getAmount() < 1) {
            damage.setAmount(1);
        }
    }

    // --- Partie Technique Hytale ---
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);

            // 🌟 CORRECTION DE PRIORITÉ ICI : On utilise ApplyDamageGroup
            // pour que tes stats et passifs soient appliqués en TOUT DERNIER !
            return (SystemGroup<EntityStore>) mod.getMethod("getFilterDamageGroup").invoke(inst);

        } catch (Throwable e) {
            LOGGER.atSevere().log("Erreur critique dans CombatStatsSystem : " + e.getMessage());
            return null;
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.any();
    }
}