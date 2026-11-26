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
            // 2026-11-25 : meme fenetre a cartes que l'accueil (ClassChoicePage)
            player.getPageManager().openCustomPage(entityRef, store,
                    new ClassChoicePage(playerRef, baseClassIds, false, false, 0,
                            player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION)));
            return;
        }

        // --- 2. ÉVOLUTION DE CLASSE (niveaux 180, 400, puis 650) ---
        ClassModel currentClass = ClassManager.get(currentClassId);
        if (currentClass == null) return;

        int promotionLevel = currentClass.getPromotionLevel();
        if (level < promotionLevel) {
            int remaining = promotionLevel - level;
            NotificationHelper.sendNotification(playerRef,
                    "<color:gold>Prochaine évolution au niveau " + promotionLevel + "</color> <color:gray>(" + remaining + " niveaux restants)</color>",
                    NotificationStyle.Warning);
            return;
        }

        List<String> possibleEvolutions = com.eldanior.system.classes.definitions.luo.VoieMartiale.filter(currentClass.getNextClassId(), playerData);

        if (possibleEvolutions == null || possibleEvolutions.isEmpty()) {
            NotificationHelper.sendNotification(playerRef,
                    "<color:gray>Vous avez atteint le sommet. Aucune évolution supplémentaire disponible.</color>",
                    NotificationStyle.Warning);
            return;
        }

        // --- 3. VOIES PROPOSEES (2026-11-26) ---
        // Palier 2 et au-dela : choix libre parmi toutes les evolutions (meme rarete : le tirage n'apportait rien).
        // Classe de base : 3 voies tirees selon leur rarete, avec la pitie ; tirage sauvegarde, 2 relances.
        boolean free = isFreeChoice(currentClass);
        List<String> proposedIds;
        if (free) {
            proposedIds = new ArrayList<>(possibleEvolutions);
        } else {
            List<String> savedChoices = playerData.getSavedEvolutionChoices();
            boolean allValid = !savedChoices.isEmpty();
            for (String id : savedChoices) {
                if (ClassManager.get(id) == null || !possibleEvolutions.contains(id)) { allValid = false; break; }
            }
            proposedIds = allValid ? savedChoices : roll(possibleEvolutions, playerData);
        }

        proposedIds = com.eldanior.system.classes.definitions.luo.VoieMartiale.ensure(proposedIds, possibleEvolutions, playerData);   // Clan Luo

        // Sauvegarde du tirage et de la pitie (modifiee par roll) : ClassChoicePage.build(), pas ici (jamais de
        // putComponent pendant le traitement du store)
        boolean isAdmin = player.getPlayerRef().hasPermission(EldaniorLogger.ADMIN_PERMISSION);
        player.getPageManager().openCustomPage(entityRef, store,
                new ClassChoicePage(playerRef, proposedIds, true, free, playerData.getEvolutionRerolls(), isAdmin));
    }

    /** Choix libre (pas de tirage, pas de relance) : a partir du palier 2. */
    public static boolean isFreeChoice(ClassModel current) {
        return current != null && current.getTier() >= 2;
    }

    // ==========================================================
    // PITIE (2026-11-26)
    // ==========================================================

    /** Pitie maximale prise en compte : x11 sur les chances d'Unique, Legendaire et Divine. */
    public static final int MAX_PITY = 20;

    /**
     * Tire 3 voies selon leur rarete et la pitie du joueur, puis met la pitie a jour : remise a zero si une voie Unique ou
     * mieux sort, +1 sinon. La pitie n'est pas effacee par la remise a zero du personnage (CharacterWipe).
     */
    public static List<String> roll(List<String> pool, PlayerLevelData data) {
        int pity = Math.min(MAX_PITY, data.getEvolutionPity());
        List<String> result = performGachaRoll(pool, 3, pity);
        boolean jackpot = false;
        for (String id : result) {
            ClassModel m = ClassManager.get(id);
            if (m != null && m.getRarity().ordinal() >= Rarity.UNIQUE.ordinal()) jackpot = true;
        }
        data.setEvolutionPity(jackpot ? 0 : data.getEvolutionPity() + 1);
        return result;
    }

    /** Multiplicateur de pitie d'une rarete : +50 % par point pour Unique, Legendaire et Divine. */
    static double pityFactor(Rarity rarity, int pity) {
        return rarity.ordinal() >= Rarity.UNIQUE.ordinal() ? 1.0 + 0.5 * pity : 1.0;
    }

    // ==========================================================
    // MÉTHODES DU GACHA
    // ==========================================================

    /**
     * Effectue un tirage au sort pondéré pour choisir 'amount' classes uniques.
     */
    public static List<String> performGachaRoll(List<String> pool, int amount) { return performGachaRoll(pool, amount, 0); }

    public static List<String> performGachaRoll(List<String> pool, int amount, int pity) {
        List<String> result = new ArrayList<>();
        List<String> remainingPool = new ArrayList<>(pool);

        int maxChoices = Math.min(amount, remainingPool.size());

        for (int i = 0; i < maxChoices; i++) {
            double totalWeight = 0;

            // Étape A : Calcul du poids total de l'urne restante
            for (String id : remainingPool) {
                ClassModel model = ClassManager.get(id);
                if (model != null) {
                    totalWeight += weight(model.getRarity(), pity);
                }
            }

            if (totalWeight <= 0) break; // Sécurité

            // Étape B : On lance un dé de 0 à totalWeight
            double roll = RANDOM.nextDouble() * totalWeight;
            double currentWeightSum = 0;

            // Étape C : On cherche sur quelle classe le dé est tombé
            for (int j = 0; j < remainingPool.size(); j++) {
                String id = remainingPool.get(j);
                ClassModel model = ClassManager.get(id);

                if (model != null) {
                    currentWeightSum += weight(model.getRarity(), pity);

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

    private static double weight(Rarity rarity, int pity) { return getRarityWeight(rarity) * pityFactor(rarity, pity); }

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