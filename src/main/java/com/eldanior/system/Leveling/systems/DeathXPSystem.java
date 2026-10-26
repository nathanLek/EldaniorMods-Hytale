package com.eldanior.system.Leveling.systems;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.configs.MobXP;
import com.eldanior.system.config.configs.Mobs.MobLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.party.Party;
import com.eldanior.system.party.PartyManager;
import com.eldanior.system.classement.ClassementManager;
import com.eldanior.system.duel.DuelManager;
import com.eldanior.system.dungeon.DungeonScaling;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.titles.nobility.family.FamilyManager;
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
import org.joml.Vector3d;
import org.joml.Vector3f;
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
    /**
     * Donnees de joueur ecrites dans le tampon de commandes mais pas encore appliquees : plusieurs morts traitees dans le meme
     * tick (un chef et ses complices, un sort de zone) partent de la derniere version. Sans cela, chacune repartait de la
     * copie du store, perimee jusqu'au vidage du tampon : gains perdus (prime, XP) et montee de niveau affichee en boucle.
     */
    private final Map<UUID, PlayerLevelData> pending = new ConcurrentHashMap<>();
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

            if (processedDeaths.putIfAbsent(victimUUID, true) != null) return;

            // Skip tout si la victime ou le tueur est en duel (gere par DuelManager)
            if (DuelManager.isInDuel(victimUUID)) return;

            UUID killerUUID = EldaniorSystem.get().getLastAttackers().remove(victimUUID);
            if (killerUUID != null && DuelManager.isInDuel(killerUUID)) return;

            // --- PÉNALITÉ DE MORT : -10% XP si la victime est un joueur ---
            if (store.getComponent(entityRef, Player.getComponentType()) != null) {
                applyDeathPenalty(victimUUID, entityRef, store, commandBuffer);
            }

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
            // Chasseur de prime : un PK tue perd sa prime (part au tresor de la ville), pas d'autre recompense
            if (com.eldanior.system.guards.BountyHunter.onKill(killerUUID, victimUUID, victimRef, store, commandBuffer)) return;
            UUID companionOwner = com.eldanior.system.guards.GuardManager.ownerOfCompanion(killerUUID);
            if (companionOwner != null) {
                rewardCompanionKill(companionOwner, victimRef, store, commandBuffer);
                return;
            }
            PlayerRef killerRefObj = Universe.get().getPlayer(killerUUID);
            if (killerRefObj == null) return;

            var killerEntityRef = killerRefObj.getReference();
            if (killerEntityRef == null) return;

            Store<EntityStore> killerStore = killerEntityRef.getStore();
            ComponentType<EntityStore, PlayerLevelData> lvlType = EldaniorSystem.get().getPlayerLevelDataType();

            PlayerLevelData killerDataRead = latest(killerUUID, killerEntityRef, killerStore, lvlType);
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
            // Cap a 2.0x max (100% bonus) pour eviter le farm de hauts niveaux
            double multiplier = Math.min(2.0, Math.max(0.2, 1.0 + (levelGap * 0.02)));

            // On applique le multiplicateur et on empêche l'XP de tomber sous 1 point
            xpAmount = (int) Math.max(1, Math.round(xpAmount * multiplier));

            // --- DUNGEON SCALING : bonus XP en fonction du nombre de joueurs dans le groupe ---
            if (isMob) {
                xpAmount = DungeonScaling.scaleXP(xpAmount, killerUUID);
            }

            // --- PK MODIFIERS ---
            boolean killerIsPK = killerDataRead != null && killerDataRead.isPK();
            if (killerIsPK) {
                if (isMob) {
                    xpAmount = (int) (xpAmount * 0.70); // -30% XP mobs
                } else if (isPvP) {
                    xpAmount = (int) (xpAmount * 1.20); // +20% XP PvP
                }
            }
            // ----------------------------------------

            PlayerLevelData dataToWrite = (killerDataRead != null)
                    ? (PlayerLevelData) killerDataRead.clone()
                    : new PlayerLevelData();

            assert dataToWrite != null;
            int oldLvl = dataToWrite.getLevel();

            // Criminel PNJ (guards/Criminals) ou monstre de contrat du jour (boards/MonsterContracts) : sa prime au tueur
            if (isMob) {
                long reward = com.eldanior.system.guards.Criminals.claim(victimRef, store, killerRefObj.getUsername())
                        + com.eldanior.system.boards.MonsterContracts.claim(victimUUID, killerRefObj.getUsername());
                if (reward > 0) {
                    dataToWrite.addMoney(reward);
                    QuestManager.onGoldGained(killerUUID, reward);
                    NotificationHelper.sendNotification(killerRefObj,
                            "<color:gold>PRIME TOUCHEE !</color> <color:green>+" + reward + " Or</color>", NotificationStyle.Success);
                }
            }

            // Bonus XP Diligence (+20%)
            if (dataToWrite.getActivePassives().contains(com.eldanior.system.skills.skillsInteraction.PassiveSkill.ANGE_DILIGENCE)) {
                xpAmount = (int)(xpAmount * 1.20f);
            }
            xpAmount = com.eldanior.system.guild.GuildUpgrade.applyXp(killerUUID, xpAmount);   // Sagesse de sa guilde
            dataToWrite.addExperience(xpAmount);

            // --- GUILD STATS TRACKING ---
            Guild killerGuild = GuildManager.getPlayerGuild(killerUUID);

            // --- PVP KILL TRACKING ---
            if (isPvP) {
                dataToWrite.addPlayerKill();
                // Hook quete PK (EXECUTION = kill joueurs)
                QuestManager.onPKKilled(killerUUID, null);
                int pvpContrib = Math.max(1, xpAmount / 5); // PvP = XP/5 (double du PvE)
                if (killerGuild != null) {
                    killerGuild.addPlayerKill();
                    killerGuild.addContribution(pvpContrib);
                }
                String pvpFamilyId = dataToWrite.getNobleFamilyId();
                if (pvpFamilyId != null && !pvpFamilyId.isEmpty()) {
                    FamilyManager.getRuntimeData(pvpFamilyId).addContribution(pvpContrib);
                }

                // Classement persistant
                ClassementManager.updatePvPKills(killerRefObj.getUsername(), dataToWrite.getPlayerKills());

                // --- BOUNTY : si le tueur est PK, augmenter sa prime ---
                if (killerIsPK) {
                    long kills = Math.max(0, (long) dataToWrite.getPlayerKills());
                    long bountyIncrease = 500 + Math.min(kills * 100L, 1_000_000L);
                    dataToWrite.addBounty(bountyIncrease);
                    dataToWrite.setLastPvPKillTime(System.currentTimeMillis());
                    // Mettre a jour le registre des primes
                    com.eldanior.system.quest.QuestManager.registerBounty(
                            killerUUID, killerRefObj.getUsername(), dataToWrite.getLevel(), dataToWrite.getBounty());
                }

                // --- RECOMPENSE : si la victime etait PK, le tueur recoit la prime ---
                PlayerLevelData victimDataPK = store.getComponent(victimRef, lvlType);
                if (victimDataPK != null && victimDataPK.isPK()) {
                    long bountyReward = victimDataPK.getBounty();
                    long bonusXP = xpAmount * 2; // Double XP pour tuer un PK
                    long bonusGold = Math.max(1000, bountyReward);

                    dataToWrite.addExperience((int) bonusXP);
                    dataToWrite.addMoney(bonusGold);
                    QuestManager.onGoldGained(killerUUID, bonusGold);

                    // Reset bounty via commandBuffer.run pour ne pas ecraser la penalite de mort
                    final Ref<EntityStore> victimRefFinal = victimRef;
                    commandBuffer.run(deferredStore -> {
                        PlayerLevelData penalizedData = deferredStore.getComponent(victimRefFinal, lvlType);
                        if (penalizedData != null) {
                            PlayerLevelData bountyCopy = (PlayerLevelData) penalizedData.clone();
                            if (bountyCopy != null) {
                                bountyCopy.collectBounty();
                                deferredStore.putComponent(victimRefFinal, lvlType, bountyCopy);
                            }
                        }
                    });
                    QuestManager.unregisterBounty(victimUUID);

                    NotificationHelper.sendNotification(killerRefObj,
                            "<color:gold>PRIME RECLAMEE !</color> <color:green>+" + bonusGold + " Or +" + bonusXP + " XP</color>",
                            NotificationStyle.Success);
                }

                // Verifier titres PvP
                java.util.List<TitleModel> pvpTitles = TitleManager.checkTitleUnlocks(dataToWrite);
                for (TitleModel title : pvpTitles) {
                    dataToWrite.addTitle(title.getId());
                    NotificationHelper.showEventTitle(killerRefObj,
                            "TITRE DEBLOQUE", title.getDisplayName(), true);
                }
            }

            // --- KILL TRACKING + TITLE CHECK ---
            if (isMob) {
                NPCEntity npcForKill = store.getComponent(victimRef, NPCEntity.getComponentType());
                if (npcForKill != null) {
                    String mobTypeId = npcForKill.getNPCTypeId().toLowerCase();
                    dataToWrite.addMobKill(mobTypeId);
                    int contribPoints = Math.max(1, xpAmount / 10);
                    if (killerGuild != null) {
                        killerGuild.addMobKill();
                        killerGuild.addContribution(contribPoints);
                    }

                    // Contribution famille
                    String killerFamilyId = dataToWrite.getNobleFamilyId();
                    if (killerFamilyId != null && !killerFamilyId.isEmpty()) {
                        FamilyManager.getRuntimeData(killerFamilyId).addContribution(contribPoints);
                    }

                    // Classement persistant
                    ClassementManager.updateMobKills(killerRefObj.getUsername(), dataToWrite.getTotalMobKills());

                    // Progression quete
                    QuestManager.onMobKill(killerUUID, mobTypeId);
                    // Tableau de guilde : contrats de sa guilde, ses missions
                    com.eldanior.system.boards.GuildBoard.onMobKill(killerUUID, mobTypeId);

                    // Verifier si de nouveaux titres sont debloques
                    java.util.List<TitleModel> newTitles = TitleManager.checkTitleUnlocks(dataToWrite);
                    for (TitleModel title : newTitles) {
                        dataToWrite.addTitle(title.getId());
                        NotificationHelper.showEventTitle(killerRefObj,
                                "TITRE DEBLOQUE", title.getDisplayName(), true);
                    }
                }

                // Progression des skills de loot/chance à chaque kill de mob
                for (var passive : dataToWrite.getActivePassives()) {
                    String name = passive.name();
                    if ("FORTUNE_COINS".equals(name)) {
                        dataToWrite.addSkillProc(name);
                    }
                }
            }

            write(killerUUID, killerEntityRef, dataToWrite, lvlType, commandBuffer);

            String xpMsg = isPvP
                    ? "<color:gold>⚔ PvP : +" + xpAmount + " XP</color> <color:gray>(vs " + victimName + ")</color>"
                    : "<color:green>+" + xpAmount + " XP</color> <color:gray>(" + victimName + ")</color>";
            NotificationHelper.sendNotification(killerRefObj, xpMsg, NotificationStyle.Success);

            if (dataToWrite.getLevel() > oldLvl) {
                NotificationHelper.showLevelUpTitle(killerRefObj, dataToWrite.getLevel());
            }

            // --- PARTAGE XP GROUPE (10% aux autres membres) ---
            Party killerParty = PartyManager.getParty(killerUUID);
            if (killerParty != null && xpAmount > 0) {
                int sharedXP = Math.max(1, (int)(xpAmount * 0.10));
                for (UUID memberUUID : killerParty.getMemberUUIDs()) {
                    if (memberUUID.equals(killerUUID)) continue;
                    PlayerRef memberRef = Universe.get().getPlayer(memberUUID);
                    if (memberRef == null) continue;
                    var memberEntityRef = memberRef.getReference();
                    if (memberEntityRef == null) continue;
                    Store<EntityStore> memberStore = memberEntityRef.getStore();
                    PlayerLevelData memberData = latest(memberUUID, memberEntityRef, memberStore, lvlType);
                    if (memberData == null) continue;
                    PlayerLevelData memberCopy = (PlayerLevelData) memberData.clone();
                    if (memberCopy == null) continue;
                    int memberOldLvl = memberCopy.getLevel();
                    memberCopy.addExperience(sharedXP);
                    write(memberUUID, memberEntityRef, memberCopy, lvlType, commandBuffer);
                    NotificationHelper.sendNotification(memberRef,
                            "<color:aqua>+" + sharedXP + " XP (groupe)</color>",
                            NotificationStyle.Success);
                    if (memberCopy.getLevel() > memberOldLvl) {
                        NotificationHelper.showLevelUpTitle(memberRef, memberCopy.getLevel());
                    }
                }
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
                            int quantity = (int) com.eldanior.system.guild.GuildUpgrade.applyGold(killerUUID, 1 + random.nextInt(maxAmount));   // Fortune de sa guilde

                            // Type de coin scalé selon le niveau du mob tué
                            String coinType = com.eldanior.system.config.configs.CoinItemRegistry.getCoinTypeForLevel(victimLevel);
                            ItemStack coinStack = new ItemStack(coinType, quantity);

                            Holder<EntityStore> itemEntityHolder = ItemComponent.generateItemDrop(store, coinStack, spawnPos, com.hypixel.hytale.math.vector.Rotation3f.ZERO, 0.0F, 0.5F, 0.0F);
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
            PlayerLevelData dataRead = latest(playerUUID, playerEntityRef, playerStore, lvlType);
            if (dataRead == null) return;

            PlayerLevelData dataToWrite = (PlayerLevelData) dataRead.clone();
            if (dataToWrite == null) return;

            dataToWrite.addPlayerDeath();
            Guild victimGuild = GuildManager.getPlayerGuild(playerUUID);
            if (victimGuild != null) victimGuild.addDeath();
            // PK perd 20% d'XP a la mort, non-PK perd 10%
            double deathPenalty = dataToWrite.isPK() ? 0.20 : 0.10;
            int xpLost = dataToWrite.removeExperiencePercent(deathPenalty);
            write(playerUUID, playerEntityRef, dataToWrite, lvlType, commandBuffer);

            String penaltyText = dataToWrite.isPK()
                    ? "<color:red>☠ Mort criminelle : -" + xpLost + " XP (-20%)</color>"
                    : "<color:red>☠ Mort : -" + xpLost + " XP (-10%)</color>";
            if (xpLost > 0) {
                NotificationHelper.sendNotification(playerRefObj, penaltyText, NotificationStyle.Danger);
            }
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("ERREUR lors de la pénalité de mort");
        }
    }

    /** Derniere version des donnees d'un joueur : celle en attente d'ecriture, sinon celle du store. */
    private PlayerLevelData latest(UUID player, Ref<EntityStore> ref, Store<EntityStore> store,
                                   ComponentType<EntityStore, PlayerLevelData> lvlType) {
        PlayerLevelData waiting = pending.get(player);
        return waiting != null ? waiting : store.getComponent(ref, lvlType);
    }

    /** Ecrit les donnees d'un joueur (tampon de commandes) ; elles restent la derniere version jusqu'au vidage du tampon. */
    private void write(UUID player, Ref<EntityStore> ref, PlayerLevelData data, ComponentType<EntityStore, PlayerLevelData> lvlType,
                       CommandBuffer<EntityStore> commandBuffer) {
        pending.put(player, data);
        commandBuffer.putComponent(ref, lvlType, data);
        commandBuffer.run(s -> pending.remove(player, data));
    }

    private int calculatePvEXP(String typeId) {
        return MobXP.getXpForId(typeId);
    }

    /**
     * Monstre tue par un aventurier recrute : son joueur recoit 10 % de l'XP qu'il aurait gagnee en le tuant lui-meme
     * (meme calcul : XP du monstre, ecart de niveau), comme le partage d'XP d'un groupe.
     */
    /**
     * Monstre tue par un aventurier recrute : son joueur et les autres joueurs de son groupe recoivent chacun 10 % de l'XP
     * qu'ils auraient gagnee en le tuant eux-memes (XP du monstre, ecart avec leur niveau), comme le partage d'un groupe.
     */
    private void rewardCompanionKill(UUID ownerUUID, Ref<EntityStore> victimRef, Store<EntityStore> store,
                                     CommandBuffer<EntityStore> commandBuffer) {
        NPCEntity npc = store.getComponent(victimRef, NPCEntity.getComponentType());
        if (npc == null) return;                                                    // les aventuriers ne donnent que l'XP des monstres
        int xpAmount = calculatePvEXP(npc.getNPCTypeId().toLowerCase());
        if (xpAmount <= 0) return;
        MobLevelData mobData = store.getComponent(victimRef, EldaniorSystem.get().getMobLevelDataType());
        int victimLevel = mobData != null ? mobData.getLevel() : MobXP.getMinLevelForId(npc.getNPCTypeId());

        java.util.Set<UUID> receivers = new java.util.LinkedHashSet<>();
        receivers.add(ownerUUID);
        Party party = PartyManager.getParty(ownerUUID);
        if (party != null) receivers.addAll(party.getMemberUUIDs());               // les autres aventuriers du groupe sont ignores plus bas
        for (UUID uuid : receivers) giveCompanionShare(uuid, xpAmount, victimLevel, commandBuffer);
    }

    /** Part d'XP (10 %) d'un joueur pour le kill d'un aventurier ; rien si ce n'est pas un joueur connecte. */
    private void giveCompanionShare(UUID playerUUID, int xpAmount, int victimLevel, CommandBuffer<EntityStore> commandBuffer) {
        PlayerRef playerRef = Universe.get().getPlayer(playerUUID);
        var entityRef = playerRef != null ? playerRef.getReference() : null;
        if (entityRef == null || !entityRef.isValid()) return;
        ComponentType<EntityStore, PlayerLevelData> lvlType = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = latest(playerUUID, entityRef, entityRef.getStore(), lvlType);
        if (data == null) return;
        double multiplier = Math.clamp(1.0 + ((victimLevel - data.getLevel()) * 0.02), 0.2, 2.0);
        int sharedXP = Math.max(1, (int) Math.round(xpAmount * multiplier * 0.10));
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return;
        int oldLvl = copy.getLevel();
        copy.addExperience(sharedXP);
        write(playerUUID, entityRef, copy, lvlType, commandBuffer);
        NotificationHelper.sendNotification(playerRef, "<color:aqua>+" + sharedXP + " XP (aventurier)</color>", NotificationStyle.Success);
        if (copy.getLevel() > oldLvl) NotificationHelper.showLevelUpTitle(playerRef, copy.getLevel());
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