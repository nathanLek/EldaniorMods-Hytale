package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.configs.MobXP;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleModel;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathXPSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Map<UUID, Boolean> processedDeaths = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Override
    public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                     @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        try {
            Ref<EntityStore> entityRef = archetypeChunk.getReferenceTo(index);
            if (!entityRef.isValid()) return;

            UUIDComponent uuidComp = store.getComponent(entityRef, UUIDComponent.getComponentType());
            if (uuidComp == null) return;
            UUID victimUUID = uuidComp.getUuid();

            if (processedDeaths.containsKey(victimUUID)) return;
            processedDeaths.put(victimUUID, true);

            // --- PÉNALITÉ DE MORT : -10% XP si la victime est un joueur ---
            if (store.getComponent(entityRef, Player.getComponentType()) != null) {
                applyDeathPenalty(victimUUID, entityRef, store, commandBuffer);
            }

            UUID killerUUID = EldaniorSystem.get().getLastAttackers().remove(victimUUID);
            if (killerUUID != null) {
                processKillRewards(killerUUID, victimUUID, entityRef, store, commandBuffer);
            }

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR CRITIQUE dans DeathXPSystem");
        }
    }

    private void processKillRewards(UUID killerUUID, UUID victimUUID, Ref<EntityStore> victimRef,
                                    Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        try {
            PlayerRef killerRefObj = Universe.get().getPlayer(killerUUID);
            if (killerRefObj == null) return;

            var killerEntityRef = killerRefObj.getReference();
            if (killerEntityRef == null) return;

            Store<EntityStore> killerStore = killerEntityRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> lvlType = EldaniorSystem.get().getPlayerLevelDataType();

            PlayerLevelData killerDataRead = killerStore.getComponent(killerEntityRef, lvlType);
            int killerLevel = (killerDataRead != null) ? killerDataRead.getLevel() : 1;

            int xpAmount = 0;
            int victimLevel = 1; // NOUVEAU : On déclare le niveau de la victime ici pour qu'il soit accessible partout
            String victimName = "Inconnu";
            boolean isPvP = false;
            boolean isMob = false;

            if (store.getComponent(victimRef, Player.getComponentType()) != null) {
                isPvP = true;
                PlayerRef victimRefObj = Universe.get().getPlayer(victimUUID);
                victimName = (victimRefObj != null) ? victimRefObj.getUsername() : "Joueur";

                PlayerLevelData victimData = store.getComponent(victimRef, lvlType);
                victimLevel = (victimData != null) ? victimData.getLevel() : 1;
                xpAmount = calculatePvPXP(killerLevel, victimLevel);
            } else {
                NPCEntity npc = store.getComponent(victimRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                if (npc != null) {
                    isMob = true;
                    String typeId = npc.getNPCTypeId().toLowerCase();
                    victimName = typeId.replace("_", " ");
                    xpAmount = calculatePvEXP(typeId);

                    // NOUVEAU : On récupère le niveau exact du monstre
                    ComponentType<EntityStore, MobLevelData> mobLevelType = EldaniorSystem.get().getMobLevelDataType();
                    MobLevelData mobData = store.getComponent(victimRef, mobLevelType);

                    // S'il n'a pas de niveau dynamique, on prend son niveau minimum par défaut
                    victimLevel = (mobData != null) ? mobData.getLevel() : MobXP.getMinLevelForId(npc.getNPCTypeId());
                }
            }

            // Si le monstre/joueur donne 0 XP de base, on arrête tout
            if (xpAmount <= 0) return;

            // --- NOUVEAU : CALCUL DU SCALING D'XP ---
            // On calcule l'écart (Victime - Tueur)
            int levelGap = victimLevel - killerLevel;

            // 1 niveau d'écart = 0.02 (2%) | Ex: +10 niveaux = 1.20 | -10 niveaux = 0.80
            double multiplier = 1.0 + (levelGap * 0.02);

            // On applique le multiplicateur et on empêche l'XP de tomber sous 1 point
            xpAmount = (int) Math.max(1, Math.round(xpAmount * multiplier));
            // ----------------------------------------

            PlayerLevelData dataToWrite = (killerDataRead != null)
                    ? (PlayerLevelData) killerDataRead.clone()
                    : new PlayerLevelData();

            assert dataToWrite != null;
            int oldLvl = dataToWrite.getLevel();
            dataToWrite.addExperience(xpAmount);

            // --- PVP KILL TRACKING ---
            if (isPvP) {
                dataToWrite.addPlayerKill();

                // Verifier titres PvP
                java.util.List<TitleModel> pvpTitles = TitleManager.checkTitleUnlocks(dataToWrite);
                for (TitleModel title : pvpTitles) {
                    dataToWrite.addTitle(title.getId());
                    NotificationHelper.sendNotification(killerRefObj,
                            "<color:gold>★ Titre debloque : " + title.getDisplayName() + " !</color>",
                            NotificationStyle.Success);
                }
            }

            // --- KILL TRACKING + TITLE CHECK ---
            if (isMob) {
                NPCEntity npcForKill = store.getComponent(victimRef, NPCEntity.getComponentType());
                if (npcForKill != null) {
                    String mobTypeId = npcForKill.getNPCTypeId().toLowerCase();
                    dataToWrite.addMobKill(mobTypeId);

                    // Verifier si de nouveaux titres sont debloques
                    java.util.List<TitleModel> newTitles = TitleManager.checkTitleUnlocks(dataToWrite);
                    for (TitleModel title : newTitles) {
                        dataToWrite.addTitle(title.getId());
                        NotificationHelper.sendNotification(killerRefObj,
                                "<color:gold>★ Titre debloque : " + title.getDisplayName() + " !</color>",
                                NotificationStyle.Success);
                    }
                }
            }

            commandBuffer.putComponent(killerEntityRef, lvlType, dataToWrite);

            String xpMsg = isPvP
                    ? "<color:gold>⚔ PvP : +" + xpAmount + " XP</color> <color:gray>(vs " + victimName + ")</color>"
                    : "<color:green>+" + xpAmount + " XP</color> <color:gray>(" + victimName + ")</color>";
            NotificationHelper.sendNotification(killerRefObj, xpMsg, NotificationStyle.Success);

            if (dataToWrite.getLevel() > oldLvl) {
                NotificationHelper.showLevelUpTitle(killerRefObj, dataToWrite.getLevel());
            }

            if (isMob) {
                NPCEntity npcComp = store.getComponent(victimRef, NPCEntity.getComponentType());
                if (npcComp != null) {
                    double chanceStat = (killerDataRead != null) ? killerDataRead.getLuck() : 0.0;
                    double finalChance = Math.min(0.80, 0.30 + (chanceStat * 0.01));

                    if (random.nextDouble() <= finalChance) {
                        TransformComponent victimTransform = store.getComponent(victimRef, TransformComponent.getComponentType());
                        if (victimTransform != null) {
                            Vector3d spawnPos = victimTransform.getPosition();

                            // Quantité basée sur le level gap
                            int maxAmount = Math.max(1, Math.min(10, 5 + levelGap));
                            int quantity = 1 + random.nextInt(maxAmount);

                            // Type de coin scalé selon le niveau du mob tué
                            String coinType = com.eldanior.system.config.configs.CoinItemRegistry.getCoinTypeForLevel(victimLevel);
                            ItemStack coinStack = new ItemStack(coinType, quantity);

                            Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(store, coinStack, spawnPos, Vector3f.ZERO, 0.0F, 0.5F, 0.0F);
                            if (itemEntityHolder != null) {
                                commandBuffer.addEntity(itemEntityHolder, AddReason.SPAWN);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR lors de l'attribution des récompenses/drops");
        }
    }

    private void applyDeathPenalty(UUID playerUUID, Ref<EntityStore> playerRef,
                                    Store<EntityStore> store, CommandBuffer<EntityStore> commandBuffer) {
        try {
            PlayerRef playerRefObj = Universe.get().getPlayer(playerUUID);
            if (playerRefObj == null) return;

            var playerEntityRef = playerRefObj.getReference();
            if (playerEntityRef == null) return;

            Store<EntityStore> playerStore = playerEntityRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> lvlType = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData dataRead = playerStore.getComponent(playerEntityRef, lvlType);
            if (dataRead == null) return;

            PlayerLevelData dataToWrite = (PlayerLevelData) dataRead.clone();
            if (dataToWrite == null) return;

            dataToWrite.addPlayerDeath();
            int xpLost = dataToWrite.removeExperiencePercent(0.10);
            commandBuffer.putComponent(playerEntityRef, lvlType, dataToWrite);

            if (xpLost > 0) {
                NotificationHelper.sendNotification(playerRefObj,
                        "<color:red>☠ Mort : -" + xpLost + " XP (-10%)</color>",
                        NotificationStyle.Danger);
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR lors de la pénalité de mort");
        }
    }

    private int calculatePvEXP(String typeId) {
        return MobXP.getXpForId(typeId);
    }

    private int calculatePvPXP(int killerLvl, int victimLvl) {
        double ratio = (double) Math.max(1, victimLvl) / (double) Math.max(1, killerLvl);
        int baseXP = (int) (100 * ratio);
        // Cap dynamique : 500 + 5 * niveau victime (Lv100 = cap 1000, Lv500 = cap 3000)
        int dynamicCap = 500 + (5 * Math.max(1, victimLvl));
        return Math.max(1, Math.min(dynamicCap, baseXP));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DeathComponent.getComponentType();
    }
}