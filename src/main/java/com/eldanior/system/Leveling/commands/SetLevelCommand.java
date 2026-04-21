package com.eldanior.system.Leveling.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.StatCalculator; // <-- L'import vital pour appliquer les stats
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import java.lang.reflect.Field;
import java.util.ArrayList;
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

        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        if (!sender.hasPermission("eldanior.command.setlevel")) {
            sender.sendMessage(Message.raw("Erreur : Pas de permission."));
            return CompletableFuture.completedFuture(null);
        }

        String playerName = this.playerArg.get(ctx);
        int level = this.levelArg.get(ctx);

        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) {
            sender.sendMessage(Message.raw("Erreur : Joueur introuvable."));
            return CompletableFuture.completedFuture(null);
        }

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                Field uuidField = PlayerRef.class.getDeclaredField("uuid");
                uuidField.setAccessible(true);
                UUID targetUUID = (UUID) uuidField.get(targetRef);

                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) {
                    sender.sendMessage(Message.raw("Erreur : Le joueur doit être connecté."));
                    return;
                }

                var ref = targetPlayer.getReference();
                assert ref != null;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                int oldLevel = data.getLevel();
                data.setLevel(level);
                data.setExperience(0);

                if (level == 1) {
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
                    if (oldFamilyId != null && !oldFamilyId.isEmpty() && data.isPatriarch()) {
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

                } else {
                    // --- GESTION DES POINTS SI ON LE MONTÉ DE NIVEAU ---
                    int gained = Math.max(0, level - oldLevel) * 3;
                    if (gained > 0) {
                        data.setAttributePoints(data.getAttributePoints() + gained);
                    }
                }

                // 1. Sauvegarde les données dans le composant
                store.putComponent(ref, type, data);

                // 2. CRUCIAL : Met à jour la barre de vie et la vitesse en jeu !
                StatCalculator.updatePlayerStats(ref, store, data);

                sender.sendMessage(Message.raw("Niveau défini sur " + level + " pour " + playerName + "."));
                targetPlayer.sendMessage(Message.raw("Votre niveau a été changé à : " + level + "."));

            } catch (Exception e) {
                sender.sendMessage(Message.raw("Erreur technique : " + e.getMessage()));
                e.printStackTrace();
            }
        }, sender.getWorld());
    }
}