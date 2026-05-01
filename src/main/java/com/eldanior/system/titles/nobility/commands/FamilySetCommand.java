package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.NobleFamilyModel;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FamilySetCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> familyArg;

    public FamilySetCommand() {
        super("familyset", "Forcer une famille (Admin)");
        this.playerArg = this.withRequiredArg("joueur", "Joueur cible", ArgTypes.STRING);
        this.familyArg = this.withRequiredArg("familyId", "ID de la famille", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        if (!sender.hasPermission("eldanior.command.family.set")) {
            UUID senderUUID;
            try { senderUUID = getSenderUUID(sender); } catch (Exception e) { return CompletableFuture.completedFuture(null); }
            if (senderUUID == null || !senderUUID.equals(NobilityManager.getCurrentKingUUID())) {
                sender.sendMessage(Message.raw("§cSeul le Roi ou un Admin peut forcer une famille."));
                return CompletableFuture.completedFuture(null);
            }
        }

        String targetName = this.playerArg.get(ctx);
        String familyId = this.familyArg.get(ctx).toLowerCase();

        NobleFamilyModel family = FamilyManager.get(familyId);
        if (family == null) {
            sender.sendMessage(Message.raw("§cFamille '" + familyId + "' inconnue."));
            sender.sendMessage(Message.raw("§7Disponibles : " + FamilyManager.getAvailableIds()));
            return CompletableFuture.completedFuture(null);
        }

        if (FamilyManager.isFamilyTaken(familyId)) {
            sender.sendMessage(Message.raw("§eAttention : Cette famille est deja prise. Attribution forcee (Admin)."));
        }

        assert sender.getWorld() != null;
        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) { sender.sendMessage(Message.raw("§cJoueur introuvable.")); return; }

                UUID targetUUID = extractUUID(targetRef);
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) { sender.sendMessage(Message.raw("§cLe joueur doit etre connecte.")); return; }

                var ref = targetPlayer.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) return;

                NobilityRank rank = NobilityRank.fromString(data.getNobilityRank());
                if (rank == null || !rank.isNoble()) {
                    sender.sendMessage(Message.raw("§cCe joueur n'est pas noble."));
                    return;
                }

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setNobleFamilyId(family.getId());
                copy.setStatus("PATRIARCH");
                store.putComponent(ref, type, copy);

                FamilyManager.claimFamily(family.getId());

                sender.sendMessage(Message.raw("§a" + targetName + " est Patriarche de " + family.getFormattedName()));
                targetPlayer.sendMessage(Message.raw("§eVous etes Patriarche de " + family.getFormattedName() + " §7- §o" + family.getMotto()));
            } catch (Exception e) { e.printStackTrace(); }
        }, sender.getWorld());
    }

    private UUID extractUUID(PlayerRef playerRef) throws Exception {
        return UUIDExtractor.getUUID(playerRef);
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return extractUUID(pRef);
    }
}
