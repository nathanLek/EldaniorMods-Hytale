package com.eldanior.system.skills.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.skills.SkillManager;
import com.eldanior.system.skills.runes.RuneManager;
import com.eldanior.system.skills.skillsInteraction.Metier;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.titles.church.ChurchRank;
import com.eldanior.system.config.EldaniorLogger;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.awt.Color;

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

        // Parchemin de rune : meme restriction de classe, la rune va directement dans le sac de runes
        var runeId = RuneManager.getRuneFromPage(heldItem.getItemId());
        if (runeId.isPresent()) {
            learnRune(runeId.get(), player, data, playerRef, context.getHeldItemSlot());
            return;
        }

        String itemId = heldItem.getItemId();
        int slot = context.getHeldItemSlot();

        // Parchemin de Metier (ou ancienne page de metier) : acces a un etabli, hors limite de passifs
        var metierId = SkillManager.getMetier(itemId);
        if (metierId.isPresent()) {
            Metier metier = Metier.fromName(metierId.get());
            if (metier == null) return;
            if (data.hasMetier(metier)) {
                player.getPlayerRef().sendMessage(Message.raw("Vous connaissez déjà le métier : " + metier.getDisplayName() + ".").color(Color.RED));
                return;
            }
            data.learnSkill(metier.name());
            player.getPlayerRef().sendMessage(Message.raw("Métier appris : " + metier.getDisplayName() + " (" + metier.getDescription() + ")")
                    .color(Color.CYAN).bold(true));
            consume(player, slot);
            return;
        }

        // Page d'un passif supprime le 2026-11-24 (ne faisait rien, faux actif...) : elle ne s'apprend plus
        if (SkillManager.isLost(itemId)) {
            player.getPlayerRef().sendMessage(Message.raw("Ce savoir s'est perdu : la page ne s'apprend plus.").color(Color.ORANGE));
            return;
        }

        SkillManager.getPage(itemId).ifPresent(page -> {
            PassiveSkill skill = PassiveSkill.fromName(page.skillId());
            if (skill == null) return;

            // Classe : la page indique les classes qui peuvent la lire (l'Observateur lit tout)
            String requiredClass = page.requiredClass();
            ClassModel classModel = ClassManager.get(data.getPlayerClassId());
            String playerClass = data.getPlayerClassId().toLowerCase();
            String parentClass = classModel != null ? classModel.getType().name().toLowerCase() : "";
            if (requiredClass != null && !requiredClass.isBlank() && !requiredClass.equalsIgnoreCase("all")
                    && !com.eldanior.system.classes.observateur.ObservateurManager.isObservateur(data)) {
                String reqLower = requiredClass.toLowerCase();
                if (!reqLower.contains(playerClass) && !reqLower.contains(parentClass)) {
                    player.getPlayerRef().sendMessage(Message.raw("Votre classe n'est pas apte à déchiffrer ce savoir !").color(Color.ORANGE));
                    return;
                }
            }

            // Anges et Peches : conditions et un seul pouvoir divin
            if (skill.isDivine()) {
                if (skill.isPeche() && !data.isPK()) {
                    player.getPlayerRef().sendMessage(Message.raw("Seuls les PK peuvent maîtriser un Péché Capital !").color(Color.RED));
                    return;
                }
                if (skill.isAnge()) {
                    ChurchRank rank = ChurchRank.fromString(data.getChurchRank());
                    if (rank == null || rank.ordinal() < ChurchRank.RELIGIEUX.ordinal()) {
                        player.getPlayerRef().sendMessage(Message.raw("Seuls les Religieux et au-dessus peuvent recevoir la bénédiction d'un Ange !")
                                .color(Color.RED));
                        return;
                    }
                }
                if (data.hasDivineSkill() && !data.getDivineSkill().equals(skill.name())) {
                    PassiveSkill current = PassiveSkill.fromName(data.getDivineSkill());
                    player.getPlayerRef().sendMessage(Message.raw("Vous possédez déjà un pouvoir divin ("
                            + (current != null ? current.getDisplayName() : data.getDivineSkill()) + "). Oubliez-le d'abord dans l'onglet Compétences.")
                            .color(Color.RED));
                    return;
                }
            }

            int before = Math.max(data.getLearnedRank(skill.name()), classModel != null ? classModel.passiveRank(skill) : 0);
            switch (data.learnPassive(skill, page.rank())) {
                case ALREADY -> {
                    player.getPlayerRef().sendMessage(Message.raw("Vous maîtrisez déjà ce savoir"
                            + (skill.isDivine() ? "" : " (rang " + PassiveSkill.rankLabel(before)
                            + (data.getLearnedRank(skill.name()) < before ? ", donné par votre classe" : "") + ")") + " !").color(Color.RED));
                    return;
                }
                case LIMIT -> {
                    player.getPlayerRef().sendMessage(Message.raw("Vous connaissez déjà " + PlayerLevelData.MAX_LEARNED_PASSIVES
                            + " passifs : oubliez-en un dans l'onglet Compétences avant d'en apprendre un autre.").color(Color.RED));
                    return;
                }
                case UPGRADED -> player.getPlayerRef().sendMessage(Message.raw(skill.getDisplayName() + " passe au rang "
                        + PassiveSkill.rankLabel(page.rank()) + " : " + skill.getRankName(page.rank()) + " !").color(Color.MAGENTA).bold(true));
                case NEW -> player.getPlayerRef().sendMessage(Message.raw("Savoir acquis : " + skill.getDisplayName()
                        + (skill.isDivine() ? "" : " (" + PassiveSkill.rankLabel(page.rank()) + ")")).color(Color.CYAN).bold(true));
            }
            if (skill.isDivine()) data.setDivineSkill(skill.name());
            consume(player, slot);
        });
    }

    /** Retire l'objet lu de la barre d'action (un peu plus tard : on est dans l'interaction). */
    private static void consume(Player player, int slot) {
        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                player.getInventory().getHotbar().removeItemStackFromSlot((short) slot, 1, true, false);
            } catch (Exception e) { EldaniorLogger.error("ConsumableItemSkillInteraction", e); }
        }, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private static void learnRune(String runeId, Player player, PlayerLevelData data,
                                  Ref<EntityStore> playerRef, int slot) {
        // 2026-11-24 (demande de l'utilisateur) : une rune ne s'apprend plus que dans un manuel. Un ancien parchemin se relie
        // en manuel (meme rune) ; c'est le manuel qu'on lit pour apprendre la rune.
        String book = com.eldanior.system.skills.runes.spells.MagicManuals.bookFor(runeId);
        if (book == null) {
            player.getPlayerRef().sendMessage(Message.raw("Ce parchemin est trop abîmé pour être déchiffré.").color(Color.ORANGE));
            return;
        }
        EldaniorLogger.SCHEDULER.schedule(() -> {
            try {
                player.getInventory().getHotbar().removeItemStackFromSlot((short) slot, 1, true, false);
                var inv = player.getInventory().getCombinedHotbarFirst();
                ItemStack manual = new ItemStack(book, 1);
                if (inv.canAddItemStack(manual)) inv.addItemStack(manual);
                else player.getInventory().getHotbar().setItemStackForSlot((short) slot, manual);
                player.getPlayerRef().sendMessage(Message.raw("Le vieux parchemin s'est relié en manuel. Maintenez le clic droit sur "
                        + "le manuel pour le lire et apprendre la rune.").color(Color.CYAN));
            } catch (Exception e) { EldaniorLogger.error("ConsumableItemSkillInteraction: parchemin -> manuel", e); }
        }, 50, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

}