package com.eldanior.system.titles.nobility.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.nobility.NobilityManager;
import com.eldanior.system.titles.nobility.NobilityRank;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.config.UUIDExtractor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class RankPromoteCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> rankArg;

    public RankPromoteCommand() {
        super("rankpromote", "Promouvoir un joueur (baron/comte/duc/marquis)");
        this.playerArg = this.withRequiredArg("joueur", "Joueur cible", ArgTypes.STRING);
        this.rankArg = this.withRequiredArg("rang", "baron|comte|duc|marquis", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return true; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderEntityStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderEntityStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String targetName = this.playerArg.get(ctx);
        String rankStr = this.rankArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                NobilityRank newRank = NobilityRank.fromString(rankStr);
                if (newRank == null || newRank == NobilityRank.ROI || newRank == NobilityRank.ROTURIER || newRank == NobilityRank.CHEVALIER) {
                    senderRef.sendMessage(Message.raw("§cRang invalide. Utilisez : baron, comte, duc, marquis"));
                    return;
                }

                UUID senderUUID = getSenderUUID(sender);
                if (senderUUID == null || !senderUUID.equals(NobilityManager.getCurrentKingUUID())) {
                    if (!sender.getPlayerRef().hasPermission("eldanior.command.rank.promote")) {
                        sender.getPlayerRef().sendMessage(Message.raw("§cSeul le Roi ou un Admin peut promouvoir."));
                        return;
                    }
                }

                if (!NobilityManager.canKingPromote(newRank)) {
                    sender.getPlayerRef().sendMessage(Message.raw("§cPlus de places pour " + newRank.getFormattedName()
                            + " §c(" + NobilityManager.getRemainingSlots(newRank) + "/" + newRank.getMaxPerKingdom() + ")"));
                    return;
                }

                PlayerRef targetRef = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
                if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("§cJoueur introuvable.")); return; }

                UUID targetUUID = extractUUID(targetRef);
                PlayerRef targetPlayer = Universe.get().getPlayer(targetUUID);
                if (targetPlayer == null) { sender.getPlayerRef().sendMessage(Message.raw("§cLe joueur doit etre connecte.")); return; }

                var ref = targetPlayer.getReference();
                if (ref == null) return;
                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);
                if (data == null) data = new PlayerLevelData();

                PlayerLevelData copy = (PlayerLevelData) data.clone();
                if (copy == null) return;
                copy.setNobilityRank(newRank.name());
                copy.setDignity(newRank.getBaseDignity());
                store.putComponent(ref, type, copy);

                NobilityManager.recordKingPromotion(newRank);

                sender.getPlayerRef().sendMessage(Message.raw("§a" + targetName + " promu au rang de " + newRank.getFormattedName()));
                com.eldanior.system.Leveling.utils.NotificationHelper.showEventTitle(targetPlayer,
                        "PROMOTION NOBLESSE", newRank.getFormattedName(), true);

                if (newRank == NobilityRank.MARQUIS || newRank == NobilityRank.DUC) {
                    String available = FamilyManager.getAvailableFamilyIdsForRank(newRank);
                    targetPlayer.sendMessage(Message.raw("§6Choisissez votre famille : §f/es family choose <familyId>"));
                    targetPlayer.sendMessage(Message.raw("§7Disponibles : §e" + available));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }, world);
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