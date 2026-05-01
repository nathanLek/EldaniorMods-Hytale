package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classement.ClassementManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerLoginSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private final Set<UUID> initializedPlayers = new HashSet<>();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {

        Ref<EntityStore> playerRef = archetypeChunk.getReferenceTo(index);
        if (!playerRef.isValid()) return;

        Player player = store.getComponent(playerRef, Player.getComponentType());
        if (player == null) return;

        UUID uuid = getPlayerUUID(store, playerRef);
        if (uuid == null) return;

        if (initializedPlayers.contains(uuid)) return;

        EntityStatMap statMap = store.getComponent(playerRef,
                EntityStatsModule.get().getEntityStatMapComponentType());

        if (statMap == null) return;

        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(playerRef, type);

        if (data == null) {
            data = new PlayerLevelData();
            data.setMoney(1000); // Or de depart pour les nouveaux joueurs
            commandBuffer.putComponent(playerRef, type, data);
            LOGGER.atInfo().log("[PlayerLogin] Nouveau joueur initialisé : " + uuid + " (+1000 Or)");
        }

        final PlayerLevelData finalData = data;

        commandBuffer.run(deferredStore -> {
            LOGGER.atInfo().log("[PlayerLogin] Mana avant update : " +
                    statMap.get(DefaultEntityStatTypes.getMana()).get() +
                    " / " + statMap.get(DefaultEntityStatTypes.getMana()).getMax());

            StatCalculator.updatePlayerStats(playerRef, deferredStore, finalData);
            LOGGER.atInfo().log("[PlayerLogin] Mana après update : " +
                    statMap.get(DefaultEntityStatTypes.getMana()).get() +
                    " / " + statMap.get(DefaultEntityStatTypes.getMana()).getMax());
            LOGGER.atInfo().log("[PlayerLogin] Stats appliquées pour : " + uuid + " Lv." + finalData.getLevel());
        });

        // Nettoyer guilde dissoute : si le joueur a une guilde qui n'existe plus
        if (data.hasGuild()) {
            com.eldanior.system.guild.Guild loginGuild = com.eldanior.system.guild.GuildManager.get(data.getGuildId());
            if (loginGuild == null) {
                final PlayerLevelData guildCleanData = data;
                commandBuffer.run(deferredStore -> {
                    guildCleanData.setGuildId("");
                    guildCleanData.setGuildRole("");
                    deferredStore.putComponent(playerRef, type, guildCleanData);
                });
                System.out.println("[PlayerLogin] Guilde dissoute, joueur nettoye: " + uuid);
            }
        }

        // Restaurer famille : si le joueur a une famille, la marquer comme prise
        String loginFamId = data.getNobleFamilyId();
        if (loginFamId != null && !loginFamId.isEmpty()) {
            com.eldanior.system.titles.nobility.family.NobleFamilyModel famModel =
                    com.eldanior.system.titles.nobility.family.FamilyManager.get(loginFamId);
            if (famModel != null) {
                // Reclaim la famille si pas encore prise
                if (!com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(loginFamId)) {
                    com.eldanior.system.titles.nobility.family.FamilyManager.claimFamily(loginFamId);
                    System.out.println("[PlayerLogin] Famille " + loginFamId + " restauree pour " + uuid);
                }
            } else {
                // Famille inconnue -> nettoyer
                final PlayerLevelData cleanData = data;
                commandBuffer.run(deferredStore -> {
                    cleanData.setNobleFamilyId("");
                    cleanData.setStatus("");
                    deferredStore.putComponent(playerRef, type, cleanData);
                });
                System.out.println("[PlayerLogin] Famille inconnue " + loginFamId + ", joueur nettoye: " + uuid);
            }
        }

        // Collecter les gains du shop en attente
        long pendingGold = com.eldanior.system.shop.ShopManager.collectPendingEarnings(uuid);
        if (pendingGold > 0) {
            final long gold = pendingGold;
            final PlayerLevelData shopData = data;
            commandBuffer.run(deferredStore -> {
                shopData.addMoney(gold);
                deferredStore.putComponent(playerRef, type, shopData);
            });
            com.hypixel.hytale.server.core.universe.PlayerRef pRefShop = store.getComponent(playerRef, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            if (pRefShop != null) {
                pRefShop.sendMessage(com.hypixel.hytale.server.core.Message.raw("§a+" + pendingGold + " Or recu de ventes au marche !"));
            }
        }

        // Charger les quetes du joueur
        String questData = data.getQuestData();
        if (questData != null && !questData.isEmpty()) {
            com.eldanior.system.quest.QuestManager.deserializePlayerQuests(uuid, questData);
        }
        // Charger les cooldowns de quetes
        String cooldownData = data.getCooldownData();
        if (cooldownData != null && !cooldownData.isEmpty()) {
            com.eldanior.system.quest.QuestManager.deserializeCooldowns(uuid, cooldownData);
        }
        com.eldanior.system.quest.QuestManager.checkDailyReset();

        // Charger les scores dans le classement persistant
        String playerName = player.getDisplayName();
        ClassementManager.updateMobKills(playerName, data.getTotalMobKills());
        ClassementManager.updatePvPKills(playerName, data.getPlayerKills());
        ClassementManager.updateDuelWins(playerName, data.getDuelWins());

        initializedPlayers.add(uuid);
    }

    private UUID getPlayerUUID(Store<EntityStore> store, Ref<EntityStore> playerRef) {
        try {
            Player player = store.getComponent(playerRef, Player.getComponentType());
            if (player == null) return null;

            for (java.lang.reflect.Method m : player.getClass().getMethods()) {
                if (m.getReturnType().equals(UUID.class) && m.getParameterCount() == 0) {
                    return (UUID) m.invoke(player);
                }
            }
        } catch (Exception e) { EldaniorLogger.error("PlayerLoginSystem", e); }
        return null;
    }

    public void invalidate(UUID uuid) {
        initializedPlayers.remove(uuid);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}