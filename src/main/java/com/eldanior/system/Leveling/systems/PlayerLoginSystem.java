package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.classement.ClassementManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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

        PlayerRef pRef = store.getComponent(playerRef, PlayerRef.getComponentType());
        if (pRef == null) return;
        UUID uuid;
        try { uuid = UUIDExtractor.getUUID(pRef); }
        catch (Exception e) { return; }
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

            // Synchroniser le mana interne avec le vrai max mana du système de stats Hytale
            float realMaxMana = statMap.get(DefaultEntityStatTypes.getMana()).getMax();
            int realMax = (int) realMaxMana;
            LOGGER.atInfo().log("[PlayerLogin] Mana sync: interne=" + finalData.getCurrentMana() + " | realMax=" + realMax + " | getMaxMana()=" + finalData.getMaxMana());
            finalData.setCurrentMana(realMax);
            deferredStore.putComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType(), finalData);

            LOGGER.atInfo().log("[PlayerLogin] Mana après update : " +
                    statMap.get(DefaultEntityStatTypes.getMana()).get() +
                    " / " + statMap.get(DefaultEntityStatTypes.getMana()).getMax() +
                    " | Mana interne: " + finalData.getCurrentMana());
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

        // Nettoyer les doublons de catalyst de competence active
        commandBuffer.run(deferredStore -> {
            try {
                com.hypixel.hytale.server.core.entity.entities.Player loginPlayer =
                        deferredStore.getComponent(playerRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
                if (loginPlayer != null) {
                    for (var skill : com.eldanior.system.skills.SkillManager.getAllSkills()) {
                        if (skill.catalystId() == null) continue;
                        removeDuplicateCatalyst(loginPlayer, skill.catalystId());
                    }
                }
            } catch (Exception e) { /* skip */ }
        });

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

        // Collecter les gains des ventes de parcelles en attente
        long pendingParcelGold = com.eldanior.system.territory.ParcelManager.collectPendingEarnings(uuid);
        if (pendingParcelGold > 0) {
            final long parcelGold = pendingParcelGold;
            final PlayerLevelData parcelData = data;
            commandBuffer.run(deferredStore -> {
                parcelData.addMoney(parcelGold);
                deferredStore.putComponent(playerRef, type, parcelData);
            });
            com.hypixel.hytale.server.core.universe.PlayerRef pRefParcel = store.getComponent(playerRef, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            if (pRefParcel != null) {
                pRefParcel.sendMessage(com.hypixel.hytale.server.core.Message.raw("§a+" + parcelGold + " Or recu de ventes de parcelles !"));
            }
        }

        // Charger les quetes du joueur
        String questData = data.getQuestData();
        String cooldownData = data.getCooldownData();
        boolean hasEcsData = (questData != null && !questData.isEmpty())
                || (cooldownData != null && !cooldownData.isEmpty());

        if (hasEcsData) {
            // Source primaire : EntityStore (donnees ECS)
            if (questData != null && !questData.isEmpty()) {
                com.eldanior.system.quest.QuestManager.deserializePlayerQuests(uuid, questData);
            }
            if (cooldownData != null && !cooldownData.isEmpty()) {
                com.eldanior.system.quest.QuestManager.deserializeCooldowns(uuid, cooldownData);
            }
        } else {
            // Fallback: fichier de sauvegarde (filet de securite si shutdown sans serialisation ECS)
            com.eldanior.system.quest.QuestManager.loadFromFileForPlayer(uuid);
        }
        com.eldanior.system.quest.QuestManager.checkDailyReset();

        // Charger les scores dans le classement persistant
        String playerName = player.getPlayerRef().getUsername();
        ClassementManager.updateMobKills(playerName, data.getTotalMobKills());
        ClassementManager.updatePvPKills(playerName, data.getPlayerKills());
        ClassementManager.updateDuelWins(playerName, data.getDuelWins());

        initializedPlayers.add(uuid);
    }

    public void invalidate(UUID uuid) {
        initializedPlayers.remove(uuid);
    }

    @Nonnull
    private static void removeDuplicateCatalyst(com.hypixel.hytale.server.core.entity.entities.Player player, String catalystId) {
        try {
            var inv = player.getInventory();
            int count = 0;

            // Compter dans la hotbar
            for (short i = 0; i < 9; i++) {
                var item = inv.getHotbar().getItemStack(i);
                if (item != null && !item.isEmpty() && catalystId.equals(item.getItemId())) {
                    count++;
                    if (count > 1) inv.getHotbar().removeItemStackFromSlot(i);
                }
            }
            // Compter dans le storage
            for (short i = 0; i < 27; i++) {
                var item = inv.getStorage().getItemStack(i);
                if (item != null && !item.isEmpty() && catalystId.equals(item.getItemId())) {
                    count++;
                    if (count > 1) inv.getStorage().removeItemStackFromSlot(i);
                }
            }
            // Compter dans le backpack
            for (short i = 0; i < 8; i++) {
                var item = inv.getBackpack().getItemStack(i);
                if (item != null && !item.isEmpty() && catalystId.equals(item.getItemId())) {
                    count++;
                    if (count > 1) inv.getBackpack().removeItemStackFromSlot(i);
                }
            }
        } catch (Exception e) { /* skip */ }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}