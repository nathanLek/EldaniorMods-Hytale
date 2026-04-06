package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.configs.MobXP;
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
            String victimName = "Inconnu";
            boolean isPvP = false;
            boolean isMob = false;

            if (store.getComponent(victimRef, Player.getComponentType()) != null) {
                isPvP = true;
                PlayerRef victimRefObj = Universe.get().getPlayer(victimUUID);
                victimName = (victimRefObj != null) ? victimRefObj.getUsername() : "Joueur";

                PlayerLevelData victimData = store.getComponent(victimRef, lvlType);
                int victimLevel = (victimData != null) ? victimData.getLevel() : 1;
                xpAmount = calculatePvPXP(killerLevel, victimLevel);
            } else {
                NPCEntity npc = store.getComponent(victimRef, Objects.requireNonNull(NPCEntity.getComponentType()));
                if (npc != null) {
                    isMob = true;
                    String typeId = npc.getNPCTypeId().toLowerCase();
                    victimName = typeId.replace("_", " ");
                    xpAmount = calculatePvEXP(typeId);
                }
            }

            if (xpAmount <= 0) return;

            PlayerLevelData dataToWrite = (killerDataRead != null)
                    ? (PlayerLevelData) killerDataRead.clone()
                    : new PlayerLevelData();

            assert dataToWrite != null;
            int oldLvl = dataToWrite.getLevel();
            dataToWrite.addExperience(xpAmount);
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

                            int mobLevel = MobXP.getMinLevelForId(npcComp.getNPCTypeId());
                            int levelDiff = mobLevel - killerLevel;

                            int maxAmount = Math.max(1, Math.min(10, 5 + levelDiff));
                            int quantity = 1 + random.nextInt(maxAmount);

                            ItemStack coinStack = new ItemStack("Elda_Copper_Coins", quantity);

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

    private int calculatePvEXP(String typeId) {
        return MobXP.getXpForId(typeId);
    }

    private int calculatePvPXP(int killerLvl, int victimLvl) {
        double ratio = (double) Math.max(1, victimLvl) / (double) Math.max(1, killerLvl);
        return (int) Math.min(2000, Math.max(1, 100 * ratio));
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return DeathComponent.getComponentType();
    }
}