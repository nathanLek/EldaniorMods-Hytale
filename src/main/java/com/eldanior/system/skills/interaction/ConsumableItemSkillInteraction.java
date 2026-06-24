package com.eldanior.system.skills.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.SkillManager;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.titles.church.ChurchRank;
import com.eldanior.system.config.EldaniorLogger;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.entity.InteractionContext;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.awt.Color;
import java.util.List;

public class ConsumableItemSkillInteraction extends SimpleInteraction {

    public ConsumableItemSkillInteraction() { super(); }

    public static final BuilderCodec<ConsumableItemSkillInteraction> CODEC =
            BuilderCodec.builder(ConsumableItemSkillInteraction.class, ConsumableItemSkillInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type, @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun || type != InteractionType.Use) return;

        var playerRef = context.getOwningEntity();
        if (playerRef == null || !playerRef.isValid()) return;
        Player player = playerRef.getStore().getComponent(playerRef, Player.getComponentType());
        PlayerLevelData data = playerRef.getStore().getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());

        if (player == null || data == null) return;

        ItemStack heldItem = player.getInventory().getHotbar().getItemStack(context.getHeldItemSlot());
        if (heldItem == null) return;

        SkillManager.getSkillFromItem(heldItem.getItemId()).ifPresent(skill -> {

            String playerClass = data.getPlayerClassId().toLowerCase();
            ClassModel classModel = ClassManager.get(data.getPlayerClassId());
            String parentClass = classModel != null
                    ? classModel.getType().name().toLowerCase()
                    : "";

            String requiredClass = skill.requiredClass();

            // Vérification de la classe
            if (requiredClass != null && !requiredClass.equalsIgnoreCase("all")) {
                String reqLower = requiredClass.toLowerCase();
                if (!reqLower.contains(playerClass) && !reqLower.contains(parentClass)) {
                    player.getPlayerRef().sendMessage(Message.raw("Votre classe n'est pas apte à déchiffrer ce savoir !")
                            .color(Color.ORANGE));
                    return;
                }
            }

            List<String> playerSkills = data.getUnlockedSkills();
            String newSkillId = skill.skillId();

            // 0. Validation des skills divins
            if (PassiveSkill.isDivineSkill(newSkillId)) {
                // Vérifier prérequis PK pour péchés
                if (PassiveSkill.isPecheSkill(newSkillId) && !data.isPK()) {
                    player.getPlayerRef().sendMessage(Message.raw("§cSeuls les PK peuvent maîtriser un Péché Capital !")
                            .color(Color.RED));
                    return;
                }
                // Vérifier prérequis Église pour anges
                if (PassiveSkill.isAngeSkill(newSkillId)) {
                    ChurchRank rank = ChurchRank.fromString(data.getChurchRank());
                    if (rank == null || rank.ordinal() < ChurchRank.RELIGIEUX.ordinal()) {
                        player.getPlayerRef().sendMessage(Message.raw("§cSeuls les Religieux et au-dessus peuvent recevoir la bénédiction d'un Ange !")
                                .color(Color.RED));
                        return;
                    }
                }
                // Vérifier max 1 skill divin
                if (data.hasDivineSkill() && !data.getDivineSkill().equals(newSkillId)) {
                    player.getPlayerRef().sendMessage(Message.raw("§cVous possédez déjà un pouvoir divin (" + data.getDivineSkill() + "). Vous ne pouvez en avoir qu'un seul !")
                            .color(Color.RED));
                    return;
                }
            }

            // 1. Possède-t-il DÉJÀ exactement cette compétence ?
            if (playerSkills.contains(newSkillId)) {
                player.getPlayerRef().sendMessage(Message.raw("Vous maîtrisez déjà ce savoir !").color(Color.RED));
                return;
            }

            // 2. Possède-t-il une compétence SUPÉRIEURE (LevelUp) ? -> On bloque !
            // Remplace .levelUp() par le vrai nom de ta méthode dans SkillModel
            if (skill.levelUp() != null) {
                for (String higherSkill : skill.levelUp()) {
                    if (playerSkills.contains(higherSkill)) {
                        player.getPlayerRef().sendMessage(Message.raw("Vous maîtrisez déjà une version supérieure de ce savoir !")
                                .color(Color.RED));
                        return;
                    }
                }
            }

            // 3. Possède-t-il une compétence INFÉRIEURE (LevelDown) ? -> On prépare l'évolution
            String skillToEvolve = null;
            // Remplace .levelDown() par le vrai nom de ta méthode dans SkillModel
            if (skill.levelDown() != null) {
                for (String lowerSkill : skill.levelDown()) {
                    if (playerSkills.contains(lowerSkill)) {
                        skillToEvolve = lowerSkill;
                        break; // On a trouvé l'ancienne compétence, on s'arrête
                    }
                }
            }

            // --- APPLICATION DU SAVOIR ---

            if (skillToEvolve != null) {
                // C'EST UNE ÉVOLUTION !
                // (Assure-toi d'avoir créé une méthode removeSkill() dans ton PlayerLevelData)
                data.removeSkill(skillToEvolve);
                data.learnSkill(newSkillId);

                player.getPlayerRef().sendMessage(Message.raw("Évolution de " + skillToEvolve + " ! Le savoir a muté en : " + skill.displayName())
                        .color(Color.MAGENTA).bold(true));
            } else {
                // C'EST UN NOUVEL APPRENTISSAGE (Classique)
                data.learnSkill(newSkillId);

                player.getPlayerRef().sendMessage(Message.raw("Savoir acquis : " + skill.displayName())
                        .color(Color.CYAN).bold(true));
            }

            // Track divine skill
            if (PassiveSkill.isDivineSkill(newSkillId)) {
                data.setDivineSkill(newSkillId);
            }

            // Suppression du parchemin
            int slot = context.getHeldItemSlot();
            EldaniorLogger.SCHEDULER.schedule(() -> {
                    try {
                        player.getInventory().getHotbar().removeItemStackFromSlot((short) slot, 1, true, false);
                    } catch (Exception e) { EldaniorLogger.error("ConsumableItemSkillInteraction", e); }
                }, 50, java.util.concurrent.TimeUnit.MILLISECONDS);

        });
    }
}