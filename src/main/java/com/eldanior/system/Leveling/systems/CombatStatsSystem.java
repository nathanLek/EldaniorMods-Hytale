package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleEffect;
import com.eldanior.system.titles.models.TitleModel;
import com.eldanior.system.titles.nobility.systems.DignityAuraSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
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

        // 0. CHECK PVP ZONE — bloquer les degats entre joueurs si PvP desactive dans la zone
        if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> attackerRef = entitySource.getRef();
            if (attackerRef.isValid()) {
                Player attackerPlayer = store.getComponent(attackerRef, Player.getComponentType());
                Player victimPlayer = store.getComponent(victimRef, Player.getComponentType());
                // Si les deux sont des joueurs -> check PvP zone
                if (attackerPlayer != null && victimPlayer != null && victimPlayer.getWorld() != null) {
                    try {
                        var transform = store.getComponent(victimRef,
                                com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
                        if (transform != null) {
                            String world = victimPlayer.getWorld().getName();
                            com.eldanior.system.territory.ParcelData parcel = com.eldanior.system.territory.ParcelManager.getParcelAt(
                                    world, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
                            if (parcel != null && !parcel.isPvpEnabled()) {
                                damage.setCancelled(true);
                                return;
                            }
                        }
                    } catch (Exception e) { EldaniorLogger.error("CombatStatsSystem", e); }
                }
            }
        }

        // 1. GESTION DE L'ESQUIVE (Maintenant gérée par le StatConfig !)
        if (tryDodge(victimRef, store, damage)) {
            return; // Si l'attaque est esquivée, on arrête tout calcul !
        }

        // 2. Gestion de l'Attaquant (Force + Critique + Passifs)
        applyOffensiveStats(damage, store, victimRef, commandBuffer);

        if (damage.isCancelled()) return;

        // 3. Gestion de la Victime (Endurance + Passifs)
        applyEnduranceDefense(victimRef, store, damage, commandBuffer);
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

        // Plancher minimum : 2% d'esquive quoi qu'il arrive
        if (dodgeChance < 2.0f) {
            dodgeChance = 2.0f;
        }

        // --- 3. LANCEMENT DU DÉ ---
        if (Math.random() < (dodgeChance / 100.0f)) {
            damage.setCancelled(true);

            // Appliquer l'effet visuel Dodge_Left ou Dodge_Right aleatoirement
            try {
                String dodgeEffect = Math.random() < 0.5 ? "Dodge_Left" : "Dodge_Right";
                com.eldanior.system.config.Effects.EffectsManager.applyEffect(victimRef, dodgeEffect, store);
            } catch (Exception e) { EldaniorLogger.error("CombatStatsSystem", e); }

            return true;
        }
        return false;
    }

    private void applyOffensiveStats(Damage damage, Store<EntityStore> store, Ref<EntityStore> victimRef,
                                      CommandBuffer<EntityStore> commandBuffer) {
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

        boolean isCrit = LuckSystem.isCriticalHit(attackerData);
        attackerData.setLastAttackWasCrit(isCrit);
        if (isCrit) {
            currentDamage *= CRIT_MULTIPLIER;
            LOGGER.atFine().log("Coup Critique Donné ===> " + currentDamage);
            // Effet visuel Red_Flash sur la victime lors d'un coup critique
            try {
                com.eldanior.system.config.Effects.EffectsManager.applyEffect(victimRef, "Red_Flash", store);
            } catch (Exception e) { /* skip */ }
        }

        damage.setAmount(currentDamage);

        // Lire le vrai mana depuis le système de stats Hytale (celui qui se régénère)
        EntityStatMap attackerStatMap = store.getComponent(attackerRef, EntityStatsModule.get().getEntityStatMapComponentType());
        float realMana = attackerStatMap != null ? attackerStatMap.get(DefaultEntityStatTypes.getMana()).get() : 0;

        // Travailler directement sur attackerData (pas de clone — même pattern que applyEnduranceDefense)
        boolean dataChanged = false;

        for (PassiveSkill skill : attackerData.getActivePassives()) {
            if (skill.getLogic() != null) {
                // Vérifier le cooldown du skill passif
                if (skill.getCooldownSeconds() > 0 && !attackerData.canCast(skill.getId())) {
                    LOGGER.atInfo().log("[Passive] " + skill.getId() + " en COOLDOWN — skip (reste " + attackerData.getRemainingCooldown(skill.getId()) / 1000 + "s)");
                    continue;
                }

                int manaCost = skill.getManaCost();
                if (manaCost > 0 && realMana < manaCost) {
                    LOGGER.atInfo().log("[Passive] " + skill.getId() + " pas assez de MANA (" + (int)realMana + "/" + manaCost + ") — skip");
                    continue;
                }

                boolean mastered = attackerData.isSkillMastered(skill.getId());
                boolean procced = skill.getLogic().onAttack(damage, attackerData, store, attackerRef, victimRef, mastered);

                // Le mana n'est consommé QUE si le skill a effectivement proc
                if (procced) {
                    if (manaCost > 0) {
                        realMana -= manaCost;
                        attackerStatMap.setStatValue(DefaultEntityStatTypes.getMana(), realMana);
                    }
                    attackerData.addSkillProc(skill.getId());
                    LOGGER.atInfo().log("[Passive] " + skill.getId() + " PROC! Mana: " + (int)realMana + " (-" + manaCost + ") | Progression: " + String.format("%.2f", attackerData.getSkillProgression(skill.getId())) + "% | Mastered: " + mastered);
                    if (skill.getCooldownSeconds() > 0) {
                        attackerData.applyCooldown(skill.getId(), skill.getCooldownSeconds());
                        LOGGER.atInfo().log("[Passive] " + skill.getId() + " COOLDOWN activé: " + skill.getCooldownSeconds() + "s");
                    }
                    dataChanged = true;
                }

                // Effet visuel du skill
                com.eldanior.system.config.Effects.SkillEffectConfig.applySkillEffects(skill, attackerRef, victimRef, store);
            }
        }

        // Persister les changements (procs, cooldowns, stacks)
        if (dataChanged) {
            commandBuffer.putComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType(), attackerData);
        }

        // --- TITLE EFFECTS (bonus degats vs mob type) ---
        if (!damage.isCancelled()) {
            String currentTitleId = attackerData.getCurrentTitle();
            if (currentTitleId != null && !currentTitleId.isEmpty()) {
                TitleModel title = TitleManager.get(currentTitleId);
                if (title != null && !title.getEffects().isEmpty()) {
                    // Identifier le type de mob victime
                    String victimMobType = null;
                    if (victimRef != null) {
                        NPCEntity npc = store.getComponent(victimRef, NPCEntity.getComponentType());
                        if (npc != null) {
                            victimMobType = npc.getNPCTypeId().toLowerCase();
                        }
                    }

                    for (TitleEffect effect : title.getEffects()) {
                        if (effect.type() == TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB) {
                            String target = effect.target();
                            // "all" = bonus contre tout, sinon on verifie si le mob match
                            if ("all".equals(target) || (victimMobType != null && victimMobType.contains(target))) {
                                damage.setAmount(damage.getAmount() * (1.0f + (float) effect.value()));
                            }
                        }
                    }
                }
            }
        }

        // Bonus de degats de l'aura retire — l'aura ralentit/paralyse les mobs via DignityAuraSystem
    }

    private void applyEnduranceDefense(Ref<EntityStore> victimRef, Store<EntityStore> store, Damage damage, CommandBuffer<EntityStore> commandBuffer) {
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

        boolean defenseDataChanged = false;
        for (PassiveSkill skill : victimData.getActivePassives()) {
            if (skill.getLogic() != null) {
                // Vérifier le cooldown du skill passif
                if (skill.getCooldownSeconds() > 0 && !victimData.canCast(skill.getId())) continue;

                boolean mastered = victimData.isSkillMastered(skill.getId());
                boolean procced = skill.getLogic().onDefend(damage, victimData, store, attackerRef, victimRef, mastered);

                // Si le skill a proc : incrémenter progression + appliquer cooldown
                if (procced) {
                    victimData.addSkillProc(skill.getId());
                    if (skill.getCooldownSeconds() > 0) {
                        victimData.applyCooldown(skill.getId(), skill.getCooldownSeconds());
                    }
                    defenseDataChanged = true;
                }

                // Effet visuel du skill (pour la defense, l'attaquant et victime sont inverses)
                com.eldanior.system.config.Effects.SkillEffectConfig.applySkillEffects(skill, victimRef, attackerRef, store);
            }
        }

        // Persister les procs/cooldowns défensifs
        if (defenseDataChanged) {
            commandBuffer.putComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType(), victimData);
        }

        // --- TITLE EFFECTS (reduction degats from mob type) ---
        if (!damage.isCancelled()) {
            String currentTitleId = victimData.getCurrentTitle();
            if (currentTitleId != null && !currentTitleId.isEmpty()) {
                TitleModel title = TitleManager.get(currentTitleId);
                if (title != null && !title.getEffects().isEmpty()) {
                    String attackerMobType = null;
                    if (attackerRef != null) {
                        NPCEntity npc = store.getComponent(attackerRef, NPCEntity.getComponentType());
                        if (npc != null) {
                            attackerMobType = npc.getNPCTypeId().toLowerCase();
                        }
                    }

                    for (TitleEffect effect : title.getEffects()) {
                        if (effect.type() == TitleEffect.TitleEffectType.DAMAGE_REDUCTION_FROM_MOB) {
                            String target = effect.target();
                            if ("all".equals(target) || (attackerMobType != null && attackerMobType.contains(target))) {
                                damage.setAmount(damage.getAmount() * (1.0f - (float) effect.value()));
                            }
                        }
                    }
                }
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