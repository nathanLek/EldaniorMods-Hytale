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

        // 1. GESTION DE L'ESQUIVE (Maintenant gérée par le StatConfig !)
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

        ClassModel victimClass = ClassManager.get(victimData.getPlayerClassId());
        float dodgeChance = StatConfig.DODGE_CHANCE.getFinalValue(victimData, victimClass);

        // --- 1. RECHERCHE DE L'ATTAQUANT ET DE SON NIVEAU ---
        int attackerLevel = -1;

        if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> attackerRef = entitySource.getRef();
            if (attackerRef.isValid()) {

                // Est-ce un Mob ?
                var mobLevelType = EldaniorSystem.get().getMobLevelDataType();
                if (mobLevelType != null) {
                    var mobData = store.getComponent(attackerRef, mobLevelType);
                    if (mobData != null) {
                        attackerLevel = mobData.getLevel();
                    }
                }

                // Si ce n'est pas un Mob, est-ce un Joueur ?
                if (attackerLevel == -1) {
                    PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());
                    if (attackerData != null) {
                        attackerLevel = attackerData.getLevel();
                    }
                }
            }
        }

        // --- 2. CALCUL DU MALUS DE NIVEAU ---
        if (attackerLevel != -1) {
            int victimLevel = victimData.getLevel();
            int levelGap = attackerLevel - victimLevel;

            // Si l'ennemi a plus de 5 niveaux de plus que le joueur
            if (levelGap > 5) {
                // Exemple : On retire 5% de la chance d'esquive pour chaque niveau de différence au-dessus de 5.
                // Si l'ennemi a 10 niveaux de plus (levelGap = 10) : (10 - 5) * 0.05 = 0.25 (25% de malus)
                float penaltyMultiplier = 1.0f - ((levelGap - 5) * 0.05f);

                // On s'assure que le joueur a toujours au moins une infime chance d'esquiver (ex: max 90% de malus)
                if (penaltyMultiplier < 0.1f) {
                    penaltyMultiplier = 0.1f;
                }

                // On applique la réduction
                dodgeChance *= penaltyMultiplier;
            }
        }

        // --- 3. LANCEMENT DU DÉ ---
        if (Math.random() < (dodgeChance / 100.0f)) {
            damage.setCancelled(true);
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

        ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();
        if (mobLevelType != null) {
            var mobData = store.getComponent(attackerRef, mobLevelType);
            if (mobData != null && mobData.isStatsApplied()) {
                float damageBonus = mobData.getLevel() * com.eldanior.system.config.configs.MobsWorldConfig.DAMAGE_PER_LEVEL;
                damage.setAmount(damage.getAmount() + damageBonus);
                return;
            }
        }

        PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (attackerData == null) return;

        ClassModel attackerClass = ClassManager.get(attackerData.getPlayerClassId());

        float strengthBonus = StatConfig.STRENGTH_DAMAGE.getFinalValue(attackerData, attackerClass);
        float currentDamage = damage.getAmount() + strengthBonus;

        if (LuckSystem.isCriticalHit(attackerData)) {
            currentDamage *= CRIT_MULTIPLIER;
            LOGGER.atSevere().log("Coup Critique Donné ===> " + currentDamage);
        }

        damage.setAmount(currentDamage);

        for (PassiveSkill skill : attackerData.getActivePassives()) {
            if (skill.getLogic() != null) {
                skill.getLogic().onAttack(damage, attackerData, store, attackerRef, victimRef);
            }
        }
    }

    private void applyEnduranceDefense(Ref<EntityStore> victimRef, Store<EntityStore> store, Damage damage) {
        PlayerLevelData victimData = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (victimData == null) return;

        ClassModel victimClass = ClassManager.get(victimData.getPlayerClassId());

        float defense = StatConfig.ENDURANCE_DEFENSE.getFinalValue(victimData, victimClass);
        float currentDamage = damage.getAmount() - defense;

        if (currentDamage < 1) currentDamage = 1;
        damage.setAmount(currentDamage);

        Ref<EntityStore> attackerRef = null;
        if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            attackerRef = entitySource.getRef();
        }

        for (PassiveSkill skill : victimData.getActivePassives()) {
            if (skill.getLogic() != null) {
                skill.getLogic().onDefend(damage, victimData, store, attackerRef, victimRef);
            }
        }

        if (!damage.isCancelled() && damage.getAmount() < 1) {
            damage.setAmount(1);
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
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