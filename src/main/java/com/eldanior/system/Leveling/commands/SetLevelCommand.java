package com.eldanior.system.Leveling.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator; // <-- L'import vital pour appliquer les stats
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class SetLevelCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<Integer> levelArg;

    public SetLevelCommand() {
        super("setlevel", "Définir le niveau d'un joueur");
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.levelArg  = this.withRequiredArg("niveau", "Niveau", ArgTypes.INTEGER);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {

        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderEntityStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderEntityStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String playerName = this.playerArg.get(ctx);
        int level = this.levelArg.get(ctx);

        int clampedLevel = Math.max(1, Math.min(PlayerLevelData.MAX_LEVEL, level));

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                if (!senderRef.hasPermission("eldanior.command.setlevel")) {
                    senderRef.sendMessage(Message.raw("Erreur : Pas de permission."));
                    return;
                }

                PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) {
                    senderRef.sendMessage(Message.raw("Erreur : Joueur introuvable."));
                    return;
                }

                UUID targetUUID = UUIDExtractor.getUUID(targetRef);

                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) {
                    sender.getPlayerRef().sendMessage(Message.raw("Erreur : Le joueur doit être connecté."));
                    return;
                }

                var ref = targetPlayer.getReference();
                assert ref != null;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                int oldLevel = data.getLevel();
                data.setLevel(clampedLevel);
                data.setExperience(0);

                if (clampedLevel == 1) {
                    // --- RESET COMPLET DU JOUEUR (Hard Reset) ---
                    data.setStrength(1);
                    data.setVitality(1);
                    data.setIntelligence(1);
                    data.setEndurance(1);
                    data.setAgility(1);
                    data.setLuck(1);

                    data.setAttributePoints(0); // Avant, tu oubliais de remettre ça à 0 !

                    data.setPlayerClass("Novice");
                    data.setPlayerClassId("novice");
                    data.forgetAllSkills();

                    // Reset titres + kills + stats PvP + coffres
                    data.resetTitles();
                    data.setPlayerKills(0);
                    data.setPlayerDeaths(0);
                    data.setKillStreak(0);
                    data.setBestKillStreak(0);
                    data.setChestsDiscovered(0);

                    // Reset noblesse + famille
                    String oldFamilyId = data.getNobleFamilyId();
                    if (oldFamilyId != null && !oldFamilyId.isEmpty()) {
                        // Release la famille (reset tresorerie + contribution)
                        com.eldanior.system.titles.nobility.family.FamilyManager.releaseFamily(oldFamilyId);
                    }
                    data.setNobilityRank("ROTURIER");
                    data.setNobleFamilyId("");
                    data.setStatus("");
                    data.setDignity(0);

                    // Reset eglise
                    data.setChurchRank("LAIQUE");
                    data.setFaith(0);

                    // Reset guilde
                    data.setGuildId("");
                    data.setGuildRole("");

                    // Reset argent
                    data.setMoney(1000);

                    // Reset quetes + cooldowns + evolution
                    data.setQuestData("");
                    data.setCooldownData("");
                    data.clearSavedEvolutionChoices();
                    data.setEvolutionRerolls(0);
                    com.eldanior.system.quest.QuestManager.getPlayerQuests(targetUUID).clear();

                    System.out.println("[RESET] Money=" + data.getMoney() + " QuestData='" + data.getQuestData() + "' Cooldowns='" + data.getCooldownData() + "'");

                } else {
                    // --- GESTION DES POINTS SI ON LE MONTÉ DE NIVEAU ---
                    int gained = Math.max(0, clampedLevel - oldLevel) * 3;
                    if (gained > 0) {
                        data.setAttributePoints(data.getAttributePoints() + gained);
                    }
                }

                // 1. Sauvegarde les données dans le composant
                store.putComponent(ref, type, data);

                // 2. CRUCIAL : Met à jour la barre de vie et la vitesse en jeu !
                StatCalculator.updatePlayerStats(ref, store, data);

                sender.getPlayerRef().sendMessage(Message.raw("Niveau défini sur " + clampedLevel + " pour " + playerName + "."));
                targetPlayer.sendMessage(Message.raw("Votre niveau a été changé à : " + clampedLevel + "."));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }
}