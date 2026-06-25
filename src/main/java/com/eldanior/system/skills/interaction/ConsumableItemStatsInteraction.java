package com.eldanior.system.skills.interaction;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;


public class ConsumableItemStatsInteraction extends SimpleInteraction {

    public ConsumableItemStatsInteraction() { super(); }

    public static final BuilderCodec<ConsumableItemStatsInteraction> CODEC =
            BuilderCodec.builder(ConsumableItemStatsInteraction.class, ConsumableItemStatsInteraction::new, SimpleInteraction.CODEC).build();

    @Override
    protected void tick0(boolean firstRun, float time, @NonNullDecl InteractionType type,
                         @NonNullDecl InteractionContext context, @NonNullDecl CooldownHandler cooldownHandler) {
        if (!firstRun) return;

        try {
            var playerRef = context.getOwningEntity();
            if (playerRef == null || !playerRef.isValid()) return;
            Player player = playerRef.getStore().getComponent(playerRef, Player.getComponentType());
            PlayerLevelData data = playerRef.getStore().getComponent(playerRef, EldaniorSystem.get().getPlayerLevelDataType());

            if (player == null || data == null) return;

            ItemStack heldItem = player.getInventory().getHotbar().getItemStack(context.getHeldItemSlot());
            if (heldItem == null) return;

            String itemId = heldItem.getItemId();

            // Bloquer l'utilisation si l'item est un catalyst de competence active non apprise
            var skillOpt = com.eldanior.system.skills.SkillManager.getAllSkills().stream()
                    .filter(s -> itemId.equals(s.catalystId()))
                    .findFirst();
            if (skillOpt.isPresent()) {
                boolean hasSkill = data.getUnlockedSkills().contains(skillOpt.get().skillId());
                if (!hasSkill) {
                    var classModel = com.eldanior.system.classes.ClassManager.get(data.getPlayerClassId());
                    hasSkill = classModel != null && classModel.getActiveSkillIds().contains(skillOpt.get().skillId());
                }
                if (!hasSkill) {
                    player.getPlayerRef().sendMessage(com.hypixel.hytale.server.core.Message.raw("Vous n'avez pas appris cette competence !"));
                    return;
                }
            }

            StatsItemEffect effect = StatsItemRegistry.getEffect(itemId).orElse(null);
            if (effect == null) return;

            // Appliquer tous les effets
            StringBuilder msgParts = new StringBuilder();
            for (StatsItemEffect.StatEntry entry : effect.getEntries()) {
                boolean applied = applyEntry(data, entry);
                if (applied && msgParts.length() > 0) msgParts.append(", ");
                if (applied) msgParts.append(formatEntry(entry));
            }

            if (msgParts.length() == 0) return;

            // Recalculer les stats
            try {
                StatCalculator.updatePlayerStats(playerRef, playerRef.getStore(), data);
            } catch (Exception e) { EldaniorLogger.error("ConsumableItemStatsInteraction", e); }

            // Notification
            try {
                var pRefComp = playerRef.getStore().getComponent(playerRef, PlayerRef.getComponentType());
                if (pRefComp != null) {
                    String msg = "<color:gold>" + effect.getDisplayName() + "</color> <color:gray>| " + msgParts + "</color>";
                    NotificationHelper.sendNotification(pRefComp, msg, NotificationStyle.Success);
                }
            } catch (Exception e) { EldaniorLogger.error("ConsumableItemStatsInteraction", e); }

            // Consommer l'item
            int slot = context.getHeldItemSlot();
            EldaniorLogger.SCHEDULER.schedule(() -> {
                    try {
                        player.getInventory().getHotbar().removeItemStackFromSlot((short) slot, 1, true, false);
                    } catch (Exception e) { EldaniorLogger.error("ConsumableItemStatsInteraction", e); }
                }, 50, java.util.concurrent.TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            System.err.println("[StatsItem] ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean applyEntry(PlayerLevelData data, StatsItemEffect.StatEntry entry) {
        int value = entry.getValue();

        switch (entry.getStatType()) {
            case STRENGTH -> data.setStrength(data.getStrength() + value);
            case VITALITY -> data.setVitality(data.getVitality() + value);
            case INTELLIGENCE -> data.setIntelligence(data.getIntelligence() + value);
            case ENDURANCE -> data.setEndurance(data.getEndurance() + value);
            case AGILITY -> data.setAgility(data.getAgility() + value);
            case LUCK -> data.setLuck(data.getLuck() + value);
            case LEVEL -> {
                int newLevel = data.getLevel() + value;
                if (newLevel < 1) return false;
                data.setLevel(newLevel);
                data.setAttributePoints(data.getAttributePoints() + (value * 3));
            }
            case XP -> {
                if (value <= 0) return false;
                data.addExperience(value);
            }
            case MONEY -> data.addMoney(value);
            case REROLL -> data.setEvolutionRerolls(data.getEvolutionRerolls() + value);
            case DIGNITY -> data.setDignity(data.getDignity() + value);
            case NOBILITY_RANK -> {
                String rank = entry.getStringValue();
                if (rank == null) return false;
                data.setNobilityRank(rank);
            }
            case CHURCH_RANK -> {
                String rank = entry.getStringValue();
                if (rank == null) return false;
                data.setChurchRank(rank);
            }
            default -> { return false; }
        }
        return true;
    }

    private String formatEntry(StatsItemEffect.StatEntry entry) {
        if (entry.getStringValue() != null) {
            return switch (entry.getStatType()) {
                case NOBILITY_RANK -> "Rang: " + entry.getStringValue();
                case CHURCH_RANK -> "Eglise: " + entry.getStringValue();
                default -> entry.getStringValue();
            };
        }
        // Reroll : afficher le nombre de relances gagnées (pas le delta interne)
        if (entry.getStatType() == StatType.REROLL) {
            int gained = Math.abs(entry.getValue());
            return "+" + gained + " Relance" + (gained > 1 ? "s" : "");
        }
        String sign = entry.getValue() >= 0 ? "+" : "";
        String label = switch (entry.getStatType()) {
            case STRENGTH -> "Force";
            case VITALITY -> "Vitalite";
            case INTELLIGENCE -> "Intelligence";
            case ENDURANCE -> "Endurance";
            case AGILITY -> "Agilite";
            case LUCK -> "Chance";
            case LEVEL -> "Niveau";
            case XP -> "XP";
            case MONEY -> "Or";
            case REROLL -> "Relance";
            case DIGNITY -> "Dignite";
            default -> "?";
        };
        return label + " " + sign + entry.getValue();
    }
}
