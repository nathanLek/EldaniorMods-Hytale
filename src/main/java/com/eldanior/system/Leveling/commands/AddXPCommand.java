package com.eldanior.system.Leveling.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.Leveling.utils.StatCalculator;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AddXPCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<Integer> amountArg;

    public AddXPCommand() {
        super("addxp", "Ajouter de l'expérience à un joueur");
        this.playerArg = this.withRequiredArg("joueur", "Nom du joueur", ArgTypes.STRING);
        this.amountArg = this.withRequiredArg("nombre", "Quantité d'XP", ArgTypes.INTEGER);
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
        int amount = this.amountArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                if (!senderRef.hasPermission("eldanior.command.addxp")) {
                    senderRef.sendMessage(Message.raw("Erreur : Pas de permission."));
                    return;
                }

                if (amount <= 0) {
                    senderRef.sendMessage(Message.raw("Erreur : Le montant doit être positif."));
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
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();

                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                int oldLvl = data.getLevel();
                data.addExperience(amount);

                store.putComponent(ref, type, data);

                // Met à jour vie/mana/vitesse après changement de niveau
                StatCalculator.updatePlayerStats(ref, store, data);

                if (data.getLevel() > oldLvl) {
                    NotificationHelper.showLevelUpTitle(targetPlayer, data.getLevel());
                }

                // Verifier titres en temps reel apres ajout d'XP/niveau
                com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, data, targetPlayer);

                String msgTarget = "Reçu : <color:green>+" + amount + " XP</color> (Admin)";
                NotificationHelper.sendNotification(targetPlayer, msgTarget, NotificationStyle.Success);

                String msgSender = "Donné : <color:green>" + amount + " XP</color> à <color:yellow>" + targetPlayer.getUsername() + "</color>";
                sender.getPlayerRef().sendMessage(Message.raw(msgSender));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }
}
