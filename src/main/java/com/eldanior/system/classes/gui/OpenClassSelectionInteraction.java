package com.eldanior.system.classes.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.Rarity;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OpenClassSelectionInteraction extends SimpleInteraction {

    private static final int REQUIRED_LEVEL = 20;
    private static final Random RANDOM = new Random();

    public OpenClassSelectionInteraction() { super(); }

    public static final BuilderCodec<OpenClassSelectionInteraction> CODEC =
            BuilderCodec.builder(OpenClassSelectionInteraction.class, OpenClassSelectionInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {

        if (!firstRun || type != InteractionType.Use) return;

        var entityRef = context.getOwningEntity();
        var store = entityRef.getStore();

        Player player = store.getComponent(entityRef, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        if (player == null || playerRef == null) return;

        ComponentType<EntityStore, PlayerLevelData> dataType = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData playerData = store.getComponent(entityRef, dataType);
        if (playerData == null) return;

        int level = playerData.getLevel();
        String currentClassId = playerData.getPlayerClassId();
        boolean isNovice = currentClassId == null || currentClassId.equalsIgnoreCase("novice");

        if (level < REQUIRED_LEVEL) {
            NotificationHelper.sendNotification(playerRef,
                    "<color:red>Niveau insuffisant !</color> <color:gray>Niveau " + REQUIRED_LEVEL + " requis (vous êtes niveau " + level + ").</color>",
                    NotificationStyle.Warning);
            return;
        }

        // --- 1. CHOIX DE LA CLASSE DE BASE (Niveau 20) ---
        if (isNovice) {
            List<String> baseClassIds = new ArrayList<>();
            for (ClassModel m : ClassManager.getAll()) {
                if (!m.isAdminAccess()
                        && !m.getId().equalsIgnoreCase("novice")
                        && !ClassManager.isEvolution(m.getId())) {
                    baseClassIds.add(m.getId());
                }
            }
            player.getPageManager().openCustomPage(entityRef, store,
                    new ClassIntroScreen(playerRef, baseClassIds, false));
            return;
        }

        // --- 2. ÉVOLUTION DE CLASSE (Niveau 120, etc.) ---
        ClassModel currentClass = ClassManager.get(currentClassId);
        if (currentClass == null) return;

        if (level < currentClass.getPromotionLevel()) {
            int remaining = currentClass.getPromotionLevel() - level;
            NotificationHelper.sendNotification(playerRef,
                    "<color:gold>Prochaine évolution au niveau " + currentClass.getPromotionLevel() + "</color> <color:gray>(" + remaining + " niveaux restants)</color>",
                    NotificationStyle.Warning);
            return;
        }

        List<String> possibleEvolutions = currentClass.getNextClassId();

        if (possibleEvolutions == null || possibleEvolutions.isEmpty()) {
            NotificationHelper.sendNotification(playerRef,
                    "<color:gray>Vous avez atteint le sommet. Aucune évolution supplémentaire disponible.</color>",
                    NotificationStyle.Warning);
            return;
        }

        // --- 3. SYSTÈME DE GACHA AVEC SAUVEGARDE ---
        // Tier 2 (promotionLevel >= 400) : 1 seul choix proposé
        // Tier 1 (promotionLevel < 400) : 3 choix proposés
        int numChoices = currentClass.getPromotionLevel() >= 400 ? 1 : 3;

        // Si le joueur a déjà des choix sauvegardés, on les repropose
        List<String> savedChoices = playerData.getSavedEvolutionChoices();
        List<String> proposedIds;

        if (!savedChoices.isEmpty()) {
            // Vérifier que les choix sauvegardés sont toujours valides
            boolean allValid = true;
            for (String id : savedChoices) {
                if (ClassManager.get(id) == null) { allValid = false; break; }
            }
            proposedIds = allValid ? savedChoices : performGachaRoll(possibleEvolutions, numChoices);
        } else {
            proposedIds = performGachaRoll(possibleEvolutions, numChoices);
        }

        // La sauvegarde se fait dans ClassEvolutionIntroScreen.build()
        boolean isAdmin = player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        player.getPageManager().openCustomPage(entityRef, store,
                new ClassEvolutionIntroScreen(playerRef, proposedIds, playerData.getEvolutionRerolls(), isAdmin));
    }

    // ==========================================================
    // MÉTHODES DU GACHA
    // ==========================================================

    /**
     * Effectue un tirage au sort pondéré pour choisir 'amount' classes uniques.
     */
    public static List<String> performGachaRoll(List<String> pool, int amount) {
        List<String> result = new ArrayList<>();
        List<String> remainingPool = new ArrayList<>(pool);

        int maxChoices = Math.min(amount, remainingPool.size());

        for (int i = 0; i < maxChoices; i++) {
            int totalWeight = 0;

            // Étape A : Calcul du poids total de l'urne restante
            for (String id : remainingPool) {
                ClassModel model = ClassManager.get(id);
                if (model != null) {
                    totalWeight += getRarityWeight(model.getRarity());
                }
            }

            if (totalWeight == 0) break; // Sécurité

            // Étape B : On lance un dé de 0 à totalWeight
            int roll = RANDOM.nextInt(totalWeight);
            int currentWeightSum = 0;

            // Étape C : On cherche sur quelle classe le dé est tombé
            for (int j = 0; j < remainingPool.size(); j++) {
                String id = remainingPool.get(j);
                ClassModel model = ClassManager.get(id);

                if (model != null) {
                    currentWeightSum += getRarityWeight(model.getRarity());

                    // Si on dépasse le lancer de dé, c'est que c'est cette classe qui a été piochée !
                    if (currentWeightSum > roll) {
                        result.add(id);
                        remainingPool.remove(j); // On la retire de l'urne pour ne pas la piocher en double
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static int getRarityWeight(Rarity rarity) {
        return switch (rarity) {
            case COMMON    -> 50000; // 1 chance sur 2 (50%)
            case RARE      -> 33333; // 1 chance sur 3 (~33.3%)
            case EPIC      -> 6666;  // 1 chance sur 15 (~6.6%)
            case UNIQUE    -> 500;   // 1 chance sur 200 (0.5%)
            case LEGENDARY -> 125;   // 1 chance sur 800 (0.125%)
            case DIVINE    -> 20;    // 1 chance sur 5000 (0.02%)
            default        -> 50000;
        };
    }
}