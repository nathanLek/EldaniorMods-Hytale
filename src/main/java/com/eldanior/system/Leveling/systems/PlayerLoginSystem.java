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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerLoginSystem extends EntityTickingSystem<EntityStore> {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static PlayerLoginSystem instance;

    public static PlayerLoginSystem getInstance() { return instance; }

    private final Set<UUID> initializedPlayers = ConcurrentHashMap.newKeySet();

    public PlayerLoginSystem() {
        instance = this;
    }

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
            data.setOnboardingDone(false); // fenetre d'accueil a la premiere connexion (accueil.Welcome, 2026-11-25)
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
                if (!com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(loginFamId)) {
                    // Famille non prise : seul le Patriarche peut la restaurer
                    if ("PATRIARCH".equals(data.getStatus())) {
                        com.eldanior.system.titles.nobility.family.FamilyManager.claimFamily(loginFamId);
                        System.out.println("[PlayerLogin] Famille " + loginFamId + " restauree pour " + uuid);
                    } else {
                        // Famille dissoute (plus de Patriarche online) -> nettoyer le membre offline
                        final PlayerLevelData famCleanData = data;
                        commandBuffer.run(deferredStore -> {
                            famCleanData.setNobleFamilyId("");
                            famCleanData.setStatus("");
                            deferredStore.putComponent(playerRef, type, famCleanData);
                        });
                        System.out.println("[PlayerLogin] Famille " + loginFamId + " dissoute, membre nettoye: " + uuid);
                    }
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

        // Maison royale (2026-11-13) : le Roi est Patriarche d'Eldanior ; un ancien Roi redevient Noble de la maison
        final PlayerLevelData royalData = data;
        commandBuffer.run(deferredStore -> {
            if (com.eldanior.system.titles.nobility.family.RoyalHouse.sync(playerRef, deferredStore, royalData))
                deferredStore.putComponent(playerRef, type, royalData);
        });

        // Accueil (2026-11-25) : nouveau joueur, ou remis en premiere connexion par un admin et deconnecte avant la fin
        if (!data.isOnboardingDone())
            com.eldanior.system.accueil.Welcome.scheduleOnLogin(playerRef, store.getExternalData().getWorld());

        // Observateur execute pendant son absence : personnage efface (niveau 1, tout perdu, point d'apparition)
        if (com.eldanior.system.classes.observateur.ObservateurManager.consumeExecution(uuid)) {
            // 3 s apres : les autres etapes de la connexion ecrivent encore la fiche du joueur dans ce tick
            com.hypixel.hytale.server.core.universe.world.World execWorld = store.getExternalData().getWorld();
            com.eldanior.system.config.EldaniorLogger.SCHEDULER.schedule(() -> execWorld.execute(() -> {
                try {
                    if (playerRef.isValid())
                        com.eldanior.system.classes.observateur.ObservateurManager.execute(pRef, playerRef, playerRef.getStore(), "souverain");
                } catch (Exception e) {
                    com.eldanior.system.config.EldaniorLogger.error("[Observateur] execution a la connexion", e);
                }
            }), 3, java.util.concurrent.TimeUnit.SECONDS);
        }

        // Observateur (2026-11-16) : revocation prononcee pendant l'absence, place liberee si la classe a change
        String observateurMsg = com.eldanior.system.classes.observateur.ObservateurManager.onLogin(uuid, data);
        if (observateurMsg != null) {
            final PlayerLevelData observateurData = data;
            commandBuffer.run(deferredStore -> {
                deferredStore.putComponent(playerRef, type, observateurData);
                StatCalculator.updatePlayerStats(playerRef, deferredStore, observateurData);
            });
            pRef.sendMessage(com.hypixel.hytale.server.core.Message.raw(observateurMsg));
        }

        // Rang change par l'intendant pendant l'absence (2026-11-18) : applique maintenant
        String effectiveStatus = data.getStatus();
        String pendFam = data.getNobleFamilyId();
        if (pendFam != null && !pendFam.isEmpty() && !data.isPatriarch()) {
            var pending = com.eldanior.system.titles.nobility.family.FamilyRegistry.consumeRole(
                    com.eldanior.system.titles.nobility.family.FamilyManager.migrateId(pendFam), uuid);
            if (pending != null) {
                effectiveStatus = pending.code();
                final PlayerLevelData promoted = data;
                final String code = pending.code();
                commandBuffer.run(deferredStore -> {
                    promoted.setStatus(code);
                    deferredStore.putComponent(playerRef, type, promoted);
                });
                pRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("Pendant votre absence, votre rang dans la maison a changé : "
                        + pending.label() + "."));
            }
        }

        // Registre des Doyens : il suit le rang enregistre sur le joueur (un admin a pu le changer pendant son absence)
        String elderFam = data.getNobleFamilyId();
        com.eldanior.system.titles.nobility.family.FamilyManager.removeElderEverywhere(uuid);
        if (com.eldanior.system.titles.nobility.family.FamilyRole.of(effectiveStatus) == com.eldanior.system.titles.nobility.family.FamilyRole.ELDER
                && !data.isPatriarch() && elderFam != null && !elderFam.isEmpty()
                && com.eldanior.system.titles.nobility.family.FamilyManager.isFamilyTaken(elderFam)) {
            com.eldanior.system.titles.nobility.family.FamilyManager.restoreElder(elderFam, uuid);
        }

        // Registre de la maison (intendant, 2026-11-11) : presence notee ; exclusion prononcee pendant l'absence appliquee
        String regFam = data.getNobleFamilyId();
        if (regFam != null && !regFam.isEmpty()) {
            String fam = com.eldanior.system.titles.nobility.family.FamilyManager.migrateId(regFam);
            if (com.eldanior.system.titles.nobility.family.FamilyRegistry.consumeKick(fam, uuid)) {
                final PlayerLevelData kicked = data;
                commandBuffer.run(deferredStore -> {
                    kicked.setNobleFamilyId("");
                    kicked.setStatus("");
                    deferredStore.putComponent(playerRef, type, kicked);
                });
                pRef.sendMessage(com.hypixel.hytale.server.core.Message.raw("Pendant votre absence, le Patriarche vous a exclu de la famille."));
            } else {
                com.eldanior.system.titles.nobility.family.FamilyRegistry.touch(fam, uuid, pRef.getUsername(), effectiveStatus,
                        data.getLevel(), data.getPlayerClassId());
                try {                                                                            // rappels de l'intendant (2026-11-18)
                    var role = data.isPatriarch() ? com.eldanior.system.titles.nobility.family.FamilyRole.PATRIARCH
                            : com.eldanior.system.titles.nobility.family.FamilyRole.of(effectiveStatus);
                    String note = com.eldanior.system.guards.IntendantPage.reminder(fam, role);
                    if (note != null) pRef.sendMessage(com.hypixel.hytale.server.core.Message.raw(note));
                } catch (Exception e) {
                    com.eldanior.system.config.EldaniorLogger.error("[Intendant] rappel", e);
                }
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
                pRefShop.sendMessage(com.hypixel.hytale.server.core.Message.raw(com.eldanior.system.config.MessageConfig.SHOP_EARNINGS.format(pendingGold)));
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
                pRefParcel.sendMessage(com.hypixel.hytale.server.core.Message.raw(com.eldanior.system.config.MessageConfig.PARCEL_EARNINGS.format(parcelGold)));
            }
        }

        // Charger les quetes du joueur (ECS d'abord, puis fichier shutdown si plus recent)
        String questData = data.getQuestData();
        if (questData != null && !questData.isEmpty()) {
            com.eldanior.system.quest.QuestManager.deserializePlayerQuests(uuid, questData);
        }
        // Charger les cooldowns de quetes
        String cooldownData = data.getCooldownData();
        if (cooldownData != null && !cooldownData.isEmpty()) {
            com.eldanior.system.quest.QuestManager.deserializeCooldowns(uuid, cooldownData);
        }
        // Restaurer depuis le fichier shutdown (ecrase les donnees ECS potentiellement obsoletes)
        com.eldanior.system.quest.QuestManager.loadPlayerFromFile(uuid);
        // Roi elu pendant son absence : la quete principale quitte ses quetes en cours (2026-11-13)
        if (com.eldanior.system.quest.chapitres.ChapterQuests.purgeMain(uuid) > 0) {
            final PlayerLevelData mainData = data;
            commandBuffer.run(deferredStore -> {
                mainData.setQuestData(com.eldanior.system.quest.QuestManager.serializePlayerQuests(uuid));
                deferredStore.putComponent(playerRef, type, mainData);
            });
        }
        com.eldanior.system.quest.QuestManager.checkDailyReset();

        // Charger les scores dans le classement persistant
        String playerName = player.getPlayerRef().getUsername();
        ClassementManager.updateMobKills(playerName, data.getTotalMobKills());
        ClassementManager.updatePvPKills(playerName, data.getPlayerKills());
        ClassementManager.updateDuelWins(playerName, data.getDuelWins());

        // Poche dimensionnelle et sac a dos niveau 1 a l'arrivee (2026-11-16)
        commandBuffer.run(deferredStore -> com.eldanior.system.poche.PocheDimensionnelle.onLogin(playerRef, deferredStore));

        initializedPlayers.add(uuid);
        com.eldanior.system.skills.runes.spells.ArmeTransfo.watch(playerRef);   // armes a feu transformees : fin pendant l'absence

        // Primes placees sur lui pendant son absence (tableau de prime)
        long pendingBounty = com.eldanior.system.boards.BountyBoard.collectPending(uuid);
        if (pendingBounty > 0) {
            final PlayerLevelData bountyData = data;
            commandBuffer.run(deferredStore -> {
                bountyData.addBounty(pendingBounty);
                deferredStore.putComponent(playerRef, type, bountyData);
            });
            com.hypixel.hytale.server.core.universe.PlayerRef pRefBounty = store.getComponent(playerRef, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            if (pRefBounty != null) {
                pRefBounty.sendMessage(com.hypixel.hytale.server.core.Message.raw("Pendant votre absence, " + pendingBounty
                        + " or ont été ajoutés à la prime sur votre tête."));
            }
        }

        // Enregistrer la prime si le joueur est PK avec bounty > 0
        if (data.isPK() && data.getBounty() + pendingBounty > 0) {
            com.eldanior.system.quest.QuestManager.registerBounty(uuid, playerName, data.getLevel(), data.getBounty() + pendingBounty);
        }

        // Message de bienvenue
        try {
            com.hypixel.hytale.server.core.universe.PlayerRef pRefWelcome = store.getComponent(playerRef, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
            if (pRefWelcome != null) {
                String welcomeMsg = com.eldanior.system.config.MessageConfig.LOGIN_WELCOME.format(
                        playerName, data.getLevel(), data.getPlayerClass());
                com.eldanior.system.Leveling.utils.NotificationHelper.sendNotification(
                        pRefWelcome, "<color:gold>" + welcomeMsg + "</color>",
                        com.hypixel.hytale.protocol.packets.interface_.NotificationStyle.Success);
                com.eldanior.system.guards.BankLoans.remind(pRefWelcome);           // pret en cours : somme et delai
            }
        } catch (Exception e) { /* skip */ }
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