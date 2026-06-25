package com.eldanior.system.territory.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.*;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.Message;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ParcelCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;

    public ParcelCommand() {
        super("parcel", "Gestion des parcelles (pos1|pos2|info|list|buy)");
        this.actionArg = this.withRequiredArg("action", "pos1|pos2|info|list|buy", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> senderEntityRef = ctx.senderAsPlayerRef();
        if (senderEntityRef == null || !senderEntityRef.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> senderStore = senderEntityRef.getStore();
        World world = ((EntityStore) senderStore.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                UUID senderUUID = UUIDExtractor.getUUID(senderRef);
                if (senderUUID == null) return;
                boolean isAdmin = senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

                switch (action.toLowerCase()) {
                    case "pos1" -> handlePos1(sender, senderUUID);
                    case "pos2" -> handlePos2(sender, senderUUID);
                    case "info" -> handleInfo(sender, senderUUID);
                    case "list" -> handleList(sender, senderUUID, isAdmin);
                    case "buy" -> handleBuy(sender, senderUUID);
                    default -> sender.getPlayerRef().sendMessage(Message.raw("§cUsage: /es parcel <pos1|pos2|info|list|buy>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    // ==================== POS1 / POS2 ====================

    private void handlePos1(Player sender, UUID uuid) {
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3d pos = transform.getPosition();
        int x = (int) Math.floor(pos.x);
        int y = (int) Math.floor(pos.y);
        int z = (int) Math.floor(pos.z);

        ParcelManager.setPos1(uuid, x, y, z);
        sender.getPlayerRef().sendMessage(Message.raw("§aPosition 1 definie : §f" + x + ", " + y + ", " + z));
    }

    private void handlePos2(Player sender, UUID uuid) {
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3d pos = transform.getPosition();
        int x = (int) Math.floor(pos.x);
        int y = (int) Math.floor(pos.y);
        int z = (int) Math.floor(pos.z);

        ParcelManager.setPos2(uuid, x, y, z);
        sender.getPlayerRef().sendMessage(Message.raw("§aPosition 2 definie : §f" + x + ", " + y + ", " + z));
    }

    // ==================== INFO ====================

    private void handleInfo(Player sender, UUID uuid) {
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3d pos = transform.getPosition();
        String worldStr = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(worldStr, pos.x, pos.y, pos.z);

        if (parcel == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§7Vous etes en zone sauvage (aucune parcelle)."));
            return;
        }

        sender.getPlayerRef().sendMessage(Message.raw("§6=== " + parcel.getType().getLabel() + " : " + parcel.getName() + " ==="));
        sender.getPlayerRef().sendMessage(Message.raw("§7ID: §f" + parcel.getId()));
        sender.getPlayerRef().sendMessage(Message.raw("§7Proprietaire: §e" + parcel.getOwnerName()));
        sender.getPlayerRef().sendMessage(Message.raw("§7Protection: " + (parcel.isProtectedByDefault() ? "§aActivee" : "§cDesactivee")));
        sender.getPlayerRef().sendMessage(Message.raw("§7Membres: §f" + parcel.getMembers().size()));

        int sizeX = parcel.getMaxX() - parcel.getMinX() + 1;
        int sizeY = parcel.getMaxY() - parcel.getMinY() + 1;
        int sizeZ = parcel.getMaxZ() - parcel.getMinZ() + 1;
        sender.getPlayerRef().sendMessage(Message.raw("§7Taille: §f" + sizeX + "x" + sizeY + "x" + sizeZ));

        if (parcel.isForSale()) sender.getPlayerRef().sendMessage(Message.raw("§eEn vente : " + parcel.getPrice() + " Or"));
        if (parcel.isForRent()) sender.getPlayerRef().sendMessage(Message.raw("§eEn location : " + parcel.getRentPrice() + " Or/7j"));

        ParcelRole myRole = parcel.getRole(uuid);
        sender.getPlayerRef().sendMessage(Message.raw("§7Votre role: §f" + (myRole != null ? myRole.name() : "Aucun")));
    }

    // ==================== LIST ====================

    private void handleList(Player sender, UUID uuid, boolean isAdmin) {
        List<ParcelData> myParcels = ParcelManager.getParcelsOwnedBy(uuid);

        if (isAdmin) {
            sender.getPlayerRef().sendMessage(Message.raw("§6=== Toutes les parcelles (" + ParcelManager.getAll().size() + ") ==="));
            for (ParcelData p : ParcelManager.getAll()) {
                sender.getPlayerRef().sendMessage(Message.raw("§7[" + p.getType().getLabel() + "] §f" + p.getName() + " §7par §e" + p.getOwnerName() + " §7(ID: " + p.getId() + ")"));
            }
        } else {
            sender.getPlayerRef().sendMessage(Message.raw("§6=== Mes parcelles (" + myParcels.size() + ") ==="));
            for (ParcelData p : myParcels) {
                sender.getPlayerRef().sendMessage(Message.raw("§7[" + p.getType().getLabel() + "] §f" + p.getName() + " §7(ID: " + p.getId() + ")"));
            }
        }
    }

    // ==================== BUY ====================

    private void handleBuy(Player sender, UUID uuid) {
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String worldStr = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(worldStr, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.getPlayerRef().sendMessage(Message.raw("§cAucune parcelle ici.")); return; }

        // Synchronized on the parcel object to prevent double-purchase race condition (ELD-82)
        synchronized (parcel) {
            if (!parcel.isForSale()) { sender.getPlayerRef().sendMessage(Message.raw("§cCette parcelle n'est pas en vente.")); return; }
            if (parcel.isOwner(uuid)) { sender.getPlayerRef().sendMessage(Message.raw("§cVous etes deja proprietaire.")); return; }

            PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
            if (data == null) return;

            // Calcul taxe (meme logique que ProprietesTab)
            long totalPrice = parcel.getPrice();
            long[] taxResult = ParcelEconomyManager.calculateTax(totalPrice);
            long netAmount = taxResult[0];
            long taxAmount = taxResult[1];

            // Transfert — prelever le montant total au joueur (atomique, refuse si fonds insuffisants)
            if (!data.removeMoney(totalPrice)) {
                sender.getPlayerRef().sendMessage(Message.raw("§cPas assez d'or ! Il faut " + totalPrice + " Or."));
                return;
            }
            store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);

            // Marquer immediatement comme vendue pour empecher tout double achat
            parcel.setForSale(false);

            // Ancien proprietaire recoit le montant NET (apres taxe)
            UUID oldOwner = parcel.getOwnerUUID();
            if (oldOwner != null) {
                PlayerRef oldRef = Universe.get().getPlayer(oldOwner);
                if (oldRef != null) {
                    try {
                        var oldEntRef = oldRef.getReference();
                        if (oldEntRef != null) {
                            var oldStore = oldEntRef.getStore();
                            PlayerLevelData oldData = oldStore.getComponent(oldEntRef, EldaniorSystem.get().getPlayerLevelDataType());
                            if (oldData != null) {
                                oldData.addMoney(netAmount);
                                oldStore.putComponent(oldEntRef, EldaniorSystem.get().getPlayerLevelDataType(), oldData);
                            }
                        }
                        oldRef.sendMessage(Message.raw("§a" + sender.getPlayerRef().getUsername() + " a achete votre parcelle " + parcel.getName() + " pour " + totalPrice + " Or ! (net: " + netAmount + " Or, taxe: " + taxAmount + " Or)"));
                    } catch (Exception e) { EldaniorLogger.error("ParcelCommand", e); }
                } else {
                    // Proprietaire offline — stocker les gains NET en attente
                    ParcelManager.addPendingEarnings(oldOwner, netAmount);
                }
            }

            // Distribuer la taxe dans la hierarchie territoriale
            if (taxAmount > 0) {
                ParcelEconomyManager.distributeTax(parcel.getId(), taxAmount);
            }

            parcel.setOwnerUUID(uuid);
            parcel.setOwnerName(sender.getPlayerRef().getUsername());
            parcel.getMembers().clear();
            parcel.addMember(uuid, ParcelRole.OWNER);
            ParcelManager.save();

            sender.getPlayerRef().sendMessage(Message.raw("§a§lParcelle achetee : §f" + parcel.getName() + " §apour §f" + totalPrice + " Or §7(taxe: " + taxAmount + " Or)§a !"));
        }
    }
}
