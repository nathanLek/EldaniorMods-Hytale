package com.eldanior.system.skills.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import java.awt.Color;

public class GiveRelicCommand extends CommandBase {

    public GiveRelicCommand() {
        // Le constructeur prend le nom de la commande et sa description
        super("getrelic", "Se donner la Relique de test");
    }

    @Override
    protected void executeSync(@NonNullDecl CommandContext context) {
        // On vérifie que celui qui tape la commande est bien un joueur
        if (!(context.sender() instanceof Player player)) return;

        // L'ID complet : namespace + id défini dans le JSON
        String fullId = "skill_page_fireball";

        try {
            // Création de l'item
            ItemStack relic = new ItemStack(fullId, 1);

            // Ajout à l'inventaire
            if (player.getInventory().getHotbar().addItemStack(relic).succeeded()) {
                player.sendMessage(Message.raw("Relique obtenue ! Testez le clic droit.").color(Color.GREEN));
            } else {
                player.sendMessage(Message.raw("Votre inventaire est plein !").color(Color.RED));
            }
        } catch (Exception e) {
            // Message d'erreur si le JSON est mal chargé ou l'ID incorrect
            player.sendMessage(Message.raw("ERREUR : Impossible de trouver l'item '" + fullId + "'").color(Color.RED));
            // Affiche l'erreur dans la console pour debug
            e.printStackTrace();
        }
    }
}