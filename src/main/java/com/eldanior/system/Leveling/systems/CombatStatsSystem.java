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

    // Multiplicateur de dégâts critique (2 = +100 % de dégâts)
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
                            // Check ParcelPermission.PVP — meme si la zone a PvP active,
                            // verifier que l'attaquant a la permission PVP dans cette parcelle
                            if (parcel != null && parcel.isPvpEnabled()) {
                                PlayerRef attackerPRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
                                if (attackerPRef != null) {
                                    java.util.UUID attackerUUID = com.eldanior.system.config.UUIDExtractor.getUUID(attackerPRef);
                                    if (attackerUUID != null && !parcel.hasPermission(attackerUUID, com.eldanior.system.territory.ParcelPermission.PVP)) {
                                        damage.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) { EldaniorLogger.error("CombatStatsSystem", e); }
                }
            }
        }

        // Journal de combat (/es combat log) : victime et/ou attaquant qui l'ont active
        Ref<EntityStore> logAttacker = damage.getSource() instanceof Damage.EntitySource es0 ? es0.getRef() : null;
        boolean logVictim = CombatLog.isOn(victimRef, store);
        boolean logHitter = logAttacker != null && CombatLog.isOn(logAttacker, store);
        StringBuilder dodgeLog = logVictim ? new StringBuilder() : null;
        float rawAmount = damage.getAmount();

        // 1. ESQUIVE AUTOMATIQUE (refonte 2026-11-25 : AutoDodge, dash a l'oppose de l'ennemi)
        if (AutoDodge.tryDodge(victimRef, store, damage, commandBuffer, dodgeLog)) {
            logHit(logVictim, logHitter, victimRef, logAttacker, store, damage, rawAmount, -1f, dodgeLog);
            return; // coup esquive : plus aucun calcul
        }

        // 2. Gestion de l'Attaquant (Force + Critique + Passifs)
        applyOffensiveStats(damage, store, victimRef, commandBuffer);
        float afterAttack = damage.getAmount();

        if (damage.isCancelled()) { logHit(logVictim, logHitter, victimRef, logAttacker, store, damage, rawAmount, afterAttack, dodgeLog); return; }

        // 3. Gestion de la Victime (Endurance + Passifs)
        applyEnduranceDefense(victimRef, store, damage, commandBuffer);

        // 4. Bokken du Dojo (sabre d'entrainement) : le coup ne tue jamais, il laisse au moins 1 PV
        if (damage.getSource() instanceof Damage.EntitySource hitter)
            com.eldanior.system.skills.runes.spells.LuoPassives.trainingCap(damage, hitter.getRef(), victimRef, store);

        logHit(logVictim, logHitter, victimRef, logAttacker, store, damage, rawAmount, afterAttack, dodgeLog);
    }

    /** Ecrit le coup dans le journal de combat de la victime et/ou de l'attaquant (avant armure et PV virtuels). */
    private static void logHit(boolean logVictim, boolean logHitter, Ref<EntityStore> victim, Ref<EntityStore> attacker,
                               Store<EntityStore> store, Damage damage, float raw, float afterAttack, StringBuilder dodgeLog) {
        if (!logVictim && !logHitter) return;
        try {
            String cause = damage.getCause() != null ? damage.getCause().getId() : "?";
            String result = damage.isCancelled() ? "ANNULÉ" : CombatLog.num(damage.getAmount());
            String steps = CombatLog.num(raw) + (afterAttack >= 0 ? " → attaque " + CombatLog.num(afterAttack) : "") + " → " + result;
            PlayerLevelData hitterData = attacker != null ? store.getComponent(attacker, EldaniorSystem.get().getPlayerLevelDataType()) : null;
            String crit = hitterData != null && hitterData.wasLastAttackCrit() && afterAttack >= 0 ? " (critique)" : "";
            if (logVictim)
                CombatLog.send(victim, store, "[Combat] reçu de " + CombatLog.name(attacker, store) + " (" + cause + ") : " + steps + crit
                        + (dodgeLog != null && dodgeLog.length() > 0 ? " | " + dodgeLog : ""));
            if (logHitter && attacker != null)
                CombatLog.send(attacker, store, "[Combat] donné à " + CombatLog.name(victim, store) + " (" + cause + ") : " + steps + crit);
        } catch (Exception e) { EldaniorLogger.error("CombatStatsSystem:journal", e); }
    }

    /**
     * Notifie l'attaquant que le PvP est bloque dans cette zone.
     * Utilise un cooldown interne pour eviter le spam de notifications.
     */
    private static final java.util.Map<java.util.UUID, Long> pvpBlockedNotifCooldown = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long PVP_NOTIF_COOLDOWN_MS = 3000L; // 3 secondes entre chaque notification

    private void notifyPvpBlocked(Ref<EntityStore> attackerRef, Store<EntityStore> store) {
        try {
            PlayerRef pRef = store.getComponent(attackerRef, PlayerRef.getComponentType());
            if (pRef == null) return;
            java.util.UUID uuid = com.eldanior.system.config.UUIDExtractor.getUUID(pRef);
            if (uuid == null) return;

            long now = System.currentTimeMillis();
            Long lastNotif = pvpBlockedNotifCooldown.get(uuid);
            if (lastNotif != null && now - lastNotif < PVP_NOTIF_COOLDOWN_MS) return;
            pvpBlockedNotifCooldown.put(uuid, now);

            NotificationHelper.sendNotification(pRef,
                    "<color:red>PvP desactive dans cette zone !</color>",
                    NotificationStyle.Warning);
        } catch (Exception e) { EldaniorLogger.error("CombatStatsSystem:pvpNotif", e); }
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
                // PNJ de poste (gardes, mage, aventuriers...) : bonus de niveau reduit, leurs degats viennent surtout de leur arme
                NPCEntity attackerNpc = store.getComponent(attackerRef, NPCEntity.getComponentType());
                boolean postNpc = attackerNpc != null && com.eldanior.system.guards.GuardType.fromRole(attackerNpc.getNPCTypeId()) != null;
                float damageBonus = mobData.getLevel() * (postNpc ? com.eldanior.system.config.configs.MobsWorldConfig.POST_NPC_DAMAGE_PER_LEVEL
                        : com.eldanior.system.config.configs.MobsWorldConfig.DAMAGE_PER_LEVEL);
                damage.setAmount(damage.getAmount() + damageBonus);
                if (postNpc) com.eldanior.system.guards.AdventurerPassives.onAttack(damage, attackerRef, victimRef, store);  // passifs de l'aventurier
                return;
            }
        }

        PlayerLevelData attackerData = store.getComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (attackerData == null) return;

        ClassModel attackerClass = ClassManager.get(attackerData.getPlayerClassId());

        // Force : +0,5 % de degats par point (2026-11-25 ; avant, +0,5 degat fixe par point plafonne a +50 des le niveau ~35)
        float strengthPct = StatConfig.STRENGTH_DAMAGE.getFinalValue(attackerData, attackerClass);
        float currentDamage = damage.getAmount() * (1f + strengthPct / 100f);

        currentDamage = com.eldanior.system.skills.runes.spells.ArtefactWeapons.modifyDamage(currentDamage, attackerRef, victimRef, store);  // Excalibur, Gram, Roi Dechu, Ryukiri
        currentDamage *= com.eldanior.system.skills.runes.spells.KiEpee.damageMultiplier(attackerRef, store);   // voie martiale : Ki de l'Epee
        currentDamage *= com.eldanior.system.skills.runes.spells.TransformationHwakyung.damageMultiplier(attackerRef, store);   // Transformation Hwakyung

        boolean isCrit = LuckSystem.isCriticalHit(attackerData)
                || com.eldanior.system.skills.runes.spells.GungnirRavens.huginCrit(attackerRef, store)    // Gungnir : Hugin et Munin
                || com.eldanior.system.skills.runes.spells.ArtefactWeapons.bonusCrit(attackerRef, store)  // Masamune
                || (com.eldanior.system.config.Effects.EffectsManager.hasEffect(attackerRef, "Eld_Effet_Precision", store)
                    && java.util.concurrent.ThreadLocalRandom.current().nextFloat() < 0.20f);             // Potion de Precision
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
        com.eldanior.system.skills.runes.spells.ArtefactWeapons.onHit(currentDamage, attackerRef, victimRef, store, commandBuffer);  // armes divines

        // Passifs d'attaque (refonte du 2026-11-24) : chaque passif agit a son rang ; plus de cout en mana ni de recharge,
        // sauf certains Peches (recharge propre). Un declenchement fait progresser la maitrise (x5 avec Diligence).
        boolean dataChanged = false;
        int procWeight = attackerData.hasActivePassive(PassiveSkill.ANGE_DILIGENCE) ? 5 : 1;
        for (PlayerLevelData.ActivePassive a : attackerData.getActivePassiveEntries()) {
            PassiveSkill skill = a.skill();
            if (skill.getCooldownSeconds() > 0 && !attackerData.canCast(skill.name())) continue;
            boolean procced = skill.getLogic().onAttack(damage, attackerData, store, attackerRef, victimRef, a.rank(),
                    attackerData.isSkillMastered(skill.name()));
            if (!procced) continue;
            for (int p = 0; p < procWeight; p++) attackerData.addSkillProc(skill.name());
            if (skill.getCooldownSeconds() > 0) attackerData.applyCooldown(skill.name(), skill.getCooldownSeconds());
            dataChanged = true;
        }

        // --- Rune d'Aura active : bonus de degats scale sur la dignite (remplace l'ancien passif) ---
        if (!damage.isCancelled()
                && com.eldanior.system.config.Effects.EffectsManager.hasEffect(attackerRef,
                        com.eldanior.system.titles.nobility.systems.RuneAuraSystem.BUFF_ID, store)) {
            PlayerLevelData auraVictim = store.getComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType());
            boolean resists = com.eldanior.system.titles.nobility.systems.RuneAuraSystem.isMartial(auraVictim);   // artiste martial
            float auraBonus = resists ? 0f : com.eldanior.system.titles.nobility.systems.RuneAuraSystem.getDamageBonus(attackerData.getDignity());
            if (auraBonus > 0f) damage.setAmount(damage.getAmount() * (1.0f + auraBonus));
        }

        // Persister les changements (procs, cooldowns, stacks)
        if (dataChanged) {
            commandBuffer.putComponent(attackerRef, EldaniorSystem.get().getPlayerLevelDataType(), attackerData);
        }

        // --- TITLE EFFECTS (bonus degats vs mob type) — cumul de TOUS les titres debloqués ---
        if (!damage.isCancelled()) {
            String victimMobType = null;
            if (victimRef != null) {
                NPCEntity npc = store.getComponent(victimRef, NPCEntity.getComponentType());
                if (npc != null) {
                    victimMobType = npc.getNPCTypeId().toLowerCase();
                }
            }

            for (String titleId : attackerData.getUnlockedTitles()) {
                TitleModel title = TitleManager.get(titleId);
                if (title != null && !title.getEffects().isEmpty()) {
                    for (TitleEffect effect : title.getEffects()) {
                        if (effect.type() == TitleEffect.TitleEffectType.DAMAGE_BONUS_VS_MOB) {
                            String target = effect.target();
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
        if (victimData == null) {                                        // pas un joueur : peut-etre un aventurier (ses passifs)
            com.eldanior.system.guards.AdventurerPassives.onDefend(damage, victimRef, store);
            return;
        }

        ClassModel victimClass = ClassManager.get(victimData.getPlayerClassId());

        // Defense : -% de degats recus (2026-11-25 ; avant, -0,1 degat fixe par point plafonne a -35)
        float defensePct = StatConfig.ENDURANCE_DEFENSE.getFinalValue(victimData, victimClass);
        float currentDamage = damage.getAmount() * (1f - defensePct / 100f);

        if (currentDamage < 1) currentDamage = 1;
        damage.setAmount(currentDamage);

        Ref<EntityStore> attackerRef = null;
        if (damage.getSource() instanceof Damage.EntitySource entitySource) {
            attackerRef = entitySource.getRef();
        }

        // Passifs de defense : meme regle que l'attaque (rang, maitrise, recharge des seuls Peches qui en ont une)
        victimData.setLastDamageTakenTime(System.currentTimeMillis());   // Frappe Opportuniste, Retablissement Naturel
        boolean defenseDataChanged = false;
        int defWeight = victimData.hasActivePassive(PassiveSkill.ANGE_DILIGENCE) ? 5 : 1;
        for (PlayerLevelData.ActivePassive a : victimData.getActivePassiveEntries()) {
            PassiveSkill skill = a.skill();
            if (skill.getCooldownSeconds() > 0 && !victimData.canCast(skill.name())) continue;
            boolean procced = skill.getLogic().onDefend(damage, victimData, store, attackerRef, victimRef, a.rank(),
                    victimData.isSkillMastered(skill.name()));
            if (!procced) continue;
            for (int p = 0; p < defWeight; p++) victimData.addSkillProc(skill.name());
            if (skill.getCooldownSeconds() > 0) victimData.applyCooldown(skill.name(), skill.getCooldownSeconds());
            defenseDataChanged = true;
        }

        // --- Rune d'Aura active : reduction de degats scale sur la dignite (remplace l'ancien passif) ---
        if (!damage.isCancelled()
                && com.eldanior.system.config.Effects.EffectsManager.hasEffect(victimRef,
                        com.eldanior.system.titles.nobility.systems.RuneAuraSystem.BUFF_ID, store)) {
            float auraRed = com.eldanior.system.titles.nobility.systems.RuneAuraSystem.getDamageReduction(victimData.getDignity());
            if (auraRed > 0f) damage.setAmount(damage.getAmount() * (1.0f - auraRed));
        }

        // --- Artefact tenu : protection elementaire (Ryukiri : feu reduit) ---
        if (!damage.isCancelled()) {
            String causeId = damage.getCause() != null ? damage.getCause().getId() : null;
            damage.setAmount(com.eldanior.system.skills.runes.spells.ArtefactWeapons.modifyTaken(damage.getAmount(), causeId, victimRef, store));
        }

        // Persister les procs/cooldowns défensifs
        if (defenseDataChanged) {
            commandBuffer.putComponent(victimRef, EldaniorSystem.get().getPlayerLevelDataType(), victimData);
        }

        // --- TITLE EFFECTS (reduction degats from mob type) — cumul de TOUS les titres debloqués ---
        if (!damage.isCancelled()) {
            String attackerMobType = null;
            if (attackerRef != null) {
                NPCEntity npc = store.getComponent(attackerRef, NPCEntity.getComponentType());
                if (npc != null) {
                    attackerMobType = npc.getNPCTypeId().toLowerCase();
                }
            }

            for (String titleId : victimData.getUnlockedTitles()) {
                TitleModel title = TitleManager.get(titleId);
                if (title != null && !title.getEffects().isEmpty()) {
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

    private static SystemGroup<EntityStore> DAMAGE_GROUP_CACHE = null;

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public SystemGroup<EntityStore> getGroup() {
        if (DAMAGE_GROUP_CACHE != null) return DAMAGE_GROUP_CACHE;
        try {
            Class<?> mod = Class.forName("com.hypixel.hytale.server.core.modules.entity.damage.DamageModule");
            Object inst = mod.getMethod("get").invoke(null);
            DAMAGE_GROUP_CACHE = (SystemGroup<EntityStore>) mod.getMethod("getFilterDamageGroup").invoke(inst);
            return DAMAGE_GROUP_CACHE;
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