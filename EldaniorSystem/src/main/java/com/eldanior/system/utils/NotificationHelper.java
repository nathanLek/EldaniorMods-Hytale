package com.eldanior.system.utils;
import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.rpg.classes.skills.Skills.SkillModel;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import org.bson.BsonDocument;
import org.bson.BsonString;

import java.util.UUID;
import javax.annotation.Nonnull;

public class NotificationHelper {

    // Envoie une notification "Toast" (en haut à droite)
    public static void sendNotification(@Nonnull PlayerRef playerRef, @Nonnull String message, @Nonnull NotificationStyle style) {
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), TinyMsg.parse(message), style);
    }

    public static void sendNotification(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Default);
    }

    public static void sendSuccess(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Success);
    }

    public static void sendWarning(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Warning);
    }

    public static void sendDanger(@Nonnull PlayerRef playerRef, @Nonnull String message) {
        sendNotification(playerRef, message, NotificationStyle.Danger);
    }

    // Affiche un gros titre au milieu de l'écran
    public static void showEventTitle(@Nonnull PlayerRef playerRef, @Nonnull String primaryTitle, @Nonnull String secondaryTitle, boolean isMajor) {
        UUID worldUuid = playerRef.getWorldUuid();
        if (worldUuid != null) {
            World world = Universe.get().getWorld(worldUuid);
            if (world != null && world.isAlive()) {
                world.execute(() -> safeShowTitle(playerRef, primaryTitle, secondaryTitle, isMajor));
                return;
            }
        }
        safeShowTitle(playerRef, primaryTitle, secondaryTitle, isMajor);
    }

    private static void safeShowTitle(PlayerRef playerRef, String primary, String secondary, boolean isMajor) {
        try {
            EventTitleUtil.showEventTitleToPlayer(playerRef, TinyMsg.parse(primary), TinyMsg.parse(secondary), isMajor);
        } catch (Exception e) {
            // Fallback si le titre échoue
            sendNotification(playerRef, primary + " - " + secondary, NotificationStyle.Success);
        }
    }

    public static void showLevelUpTitle(@Nonnull PlayerRef playerRef, int newLevel) {
        showEventTitle(playerRef, "LEVEL UP!", "Niveau" + newLevel, true);
    }

    public static void giveSkillItem(PlayerRef playerRef, SkillModel skill) {
        BsonDocument metadata = new BsonDocument();
        metadata.put("displayName", new BsonString("§dSkill Test : " + skill.getName()));
        metadata.put(EldaniorSystem.getSkillIdKey().getKey(), new BsonString(skill.getId()));

        // ESSAI : ID "Food_Apple". Si ça fait un point d'interrogation, remets "hytale:apple"
        // Le but est d'avoir un item consommable.
        ItemStack stack = new ItemStack("skill_activator", 1, metadata);

        var ref = playerRef.getReference();
        if (ref != null) {
            Player p = ref.getStore().getComponent(ref, Player.getComponentType());
            if (p != null) {
                p.getInventory().getHotbar().setItemStackForSlot((short) 0, stack); // Slot 1 (0 en index)
            }
        }
    }

    public static void sendSuccess(Player player, String message) {
        if (player != null) {
            // La méthode standard pour envoyer un message simple à un joueur
            player.sendMessage(Message.parse(message));
            // Si tu veux aussi jouer un son ou autre, ajoute-le ici
        }
    }

    public static void sendWarning(Player player, String message) {
        if (player != null) {
            player.sendMessage(Message.parse(message));
        }
    }
}