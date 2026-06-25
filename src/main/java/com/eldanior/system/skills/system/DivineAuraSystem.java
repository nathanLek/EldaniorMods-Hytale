package com.eldanior.system.skills.system;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.Player.PlayerPositionTracker;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * DivineAuraSystem — Système tick pour les compétences divines à effet de groupe.
 *
 * - Patience (Raphaël) : +20% HP et force aux alliés du groupe dans un rayon de 15 blocs
 * - Charité (Gabriel) : Quand un allié perd de la vie, le porteur donne sa propre vie (si > 10% HP)
 * - Bienveillance (Chamuel) : 10% de l'argent du porteur redistribué aux alliés (géré ailleurs)
 * - Paresse (Péché) : Aura de ralentissement 15 blocs (PVP only)
 *
 * Tick toutes les 2 secondes.
 */
public class DivineAuraSystem extends EntityTickingSystem<EntityStore> {

    private float updateTimer = 0;
    private boolean shouldUpdate = false;

    private static final float AURA_RADIUS = 15.0f;
    private static final float AURA_RADIUS_SQ = AURA_RADIUS * AURA_RADIUS;

    // Track les HP précédents des alliés pour détecter les pertes (Charité)
    private static final Map<UUID, Float> previousHP = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> chunk,
                     @Nonnull Store<EntityStore> store,
                     @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        if (index == 0) {
            updateTimer += dt;
            if (updateTimer >= 2.0f) { // Tick toutes les 2 secondes
                shouldUpdate = true;
                updateTimer = 0;
            } else {
                shouldUpdate = false;
            }
        }
        if (!shouldUpdate) return;

        Ref<EntityStore> playerRef = chunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        PlayerRef selfPRef = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (selfPRef == null) return;

        UUID selfUUID;
        try { selfUUID = UUIDExtractor.getUUID(selfPRef); }
        catch (Exception e) { return; }
        if (selfUUID == null) return;

        PlayerLevelData selfData = store.getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());
        if (selfData == null) return;

        List<PassiveSkill> activePassives = selfData.getActivePassives();

        boolean hasPatience = activePassives.contains(PassiveSkill.ANGE_PATIENCE);
        boolean hasCharite = activePassives.contains(PassiveSkill.ANGE_CHARITE);

        if (!hasPatience && !hasCharite) return;

        // Récupérer le groupe du joueur
        Party party = PartyManager.getParty(selfUUID);
        if (party == null) return;

        List<UUID> members = party.getMemberUUIDs();
        if (members.size() <= 1) return; // Seul dans le groupe

        TransformComponent selfTransform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (selfTransform == null) return;
        Vector3d selfPos = selfTransform.getPosition();

        // Itérer les alliés du groupe dans le rayon
        for (UUID allyUUID : members) {
            if (allyUUID.equals(selfUUID)) continue;

            Vector3d allyPos = PlayerPositionTracker.PLAYER_POSITIONS.get(allyUUID);
            if (allyPos == null) continue;

            double distSq = distanceSquared(selfPos, allyPos);
            if (distSq > AURA_RADIUS_SQ) continue;

            // L'allié est dans le rayon de 15 blocs
            PlayerRef allyPRef = Universe.get().getPlayer(allyUUID);
            if (allyPRef == null) continue;

            // === PATIENCE : +20% HP et force aux alliés ===
            if (hasPatience) {
                applyPatienceBuff(allyUUID, allyPRef, store);
            }

            // === CHARITÉ : Soigner l'allié avec sa propre vie ===
            if (hasCharite) {
                applyChariteHeal(selfUUID, selfData, playerRef, allyUUID, allyPRef, store, commandBuffer);
            }
        }
    }

    /**
     * Patience : applique +20% HP et force via un heal proportionnel.
     * On régénère 2% du max HP de l'allié toutes les 2 secondes (simule le buff).
     */
    private void applyPatienceBuff(UUID allyUUID, PlayerRef allyPRef, Store<EntityStore> store) {
        try {
            Ref<EntityStore> allyRef = allyPRef.getReference();
            if (allyRef == null || !allyRef.isValid()) return;

            EntityStatMap allyStats = store.getComponent(allyRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (allyStats == null) return;

            EntityStatValue healthStat = allyStats.get(DefaultEntityStatTypes.getHealth());
            if (healthStat == null) return;

            // Regen de 2% HP max toutes les 2 secondes (simule le +20% HP continu)
            float healAmount = healthStat.getMax() * 0.02f;
            float newHP = Math.min(healthStat.getMax(), healthStat.get() + healAmount);
            if (newHP > healthStat.get()) {
                allyStats.setStatValue(DefaultEntityStatTypes.getHealth(), newHP);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Charité : si un allié a perdu de la vie, le porteur donne sa propre vie pour le soigner.
     * Désactivé si le porteur < 10% HP.
     */
    private void applyChariteHeal(UUID selfUUID, PlayerLevelData selfData,
                                   Ref<EntityStore> selfRef, UUID allyUUID,
                                   PlayerRef allyPRef, Store<EntityStore> store,
                                   CommandBuffer<EntityStore> commandBuffer) {
        try {
            // Vérifier HP du porteur (> 10% pour activer)
            EntityStatMap selfStats = store.getComponent(selfRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (selfStats == null) return;
            EntityStatValue selfHealth = selfStats.get(DefaultEntityStatTypes.getHealth());
            if (selfHealth == null) return;

            float selfHPPercent = selfHealth.get() / selfHealth.getMax();
            if (selfHPPercent < 0.10f) return; // Désactivé si < 10% HP

            // Vérifier HP de l'allié
            Ref<EntityStore> allyRef = allyPRef.getReference();
            if (allyRef == null || !allyRef.isValid()) return;

            EntityStatMap allyStats = store.getComponent(allyRef, EntityStatsModule.get().getEntityStatMapComponentType());
            if (allyStats == null) return;
            EntityStatValue allyHealth = allyStats.get(DefaultEntityStatTypes.getHealth());
            if (allyHealth == null) return;

            // Comparer avec les HP précédents pour détecter une perte
            float currentAllyHP = allyHealth.get();
            float prevAllyHP = previousHP.getOrDefault(allyUUID, currentAllyHP);

            if (currentAllyHP < prevAllyHP && currentAllyHP < allyHealth.getMax()) {
                // L'allié a perdu de la vie ! Transférer nos HP
                float hpLost = prevAllyHP - currentAllyHP;
                float healAmount = Math.min(hpLost, selfHealth.get() - (selfHealth.getMax() * 0.10f)); // Garder au moins 10% HP

                if (healAmount > 0) {
                    // Retirer HP au porteur
                    selfStats.setStatValue(DefaultEntityStatTypes.getHealth(), selfHealth.get() - healAmount);
                    // Soigner l'allié
                    allyStats.setStatValue(DefaultEntityStatTypes.getHealth(),
                            Math.min(allyHealth.getMax(), currentAllyHP + healAmount));
                }
            }

            // Sauvegarder le HP actuel pour le prochain tick
            previousHP.put(allyUUID, allyHealth.get());

        } catch (Exception ignored) {}
    }

    private double distanceSquared(Vector3d a, Vector3d b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        double dz = a.z - b.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static void handleDisconnect(UUID uuid) {
        if (uuid != null) previousHP.remove(uuid);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
