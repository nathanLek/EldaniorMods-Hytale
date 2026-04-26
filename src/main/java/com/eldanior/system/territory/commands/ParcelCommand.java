package com.eldanior.system.territory.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.*;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.prefab.selection.SelectionManager;
import com.hypixel.hytale.server.core.prefab.selection.SelectionProvider;
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
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ParcelCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> arg1;
    private final RequiredArg<String> arg2;

    public ParcelCommand() {
        super("parcel", "Gestion des parcelles (pos1|pos2|create|delete|info|invite|kick|setperm|list|sell|buy)");
        this.actionArg = this.withRequiredArg("action", "pos1|pos2|create|delete|info|invite|kick|setperm|list|sell|buy", ArgTypes.STRING);
        this.arg1 = this.withRequiredArg("arg1", "type/nom/joueur", ArgTypes.STRING);
        this.arg2 = this.withRequiredArg("arg2", "nom/valeur", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        assert sender.getWorld() != null;

        return CompletableFuture.runAsync(() -> {
            try {
                UUID senderUUID = getSenderUUID(sender);
                if (senderUUID == null) return;
                boolean isAdmin = sender.hasPermission("eldanior.command.setlevel");

                switch (action.toLowerCase()) {
                    case "pos1" -> handlePos1(sender, senderUUID);
                    case "pos2" -> handlePos2(sender, senderUUID);
                    case "create" -> {
                        // Capture la selection EditorTool puis cree la parcelle
                        captureEditorSelectionAsync(sender, senderUUID, () ->
                                handleCreate(sender, senderUUID, ctx, isAdmin));
                        return; // Le handleCreate sera appele dans le callback
                    }
                    case "delete" -> handleDelete(sender, senderUUID, ctx, isAdmin);
                    case "info" -> handleInfo(sender, senderUUID);
                    case "invite" -> handleInvite(sender, senderUUID, ctx, isAdmin);
                    case "kick" -> handleKick(sender, senderUUID, ctx, isAdmin);
                    case "setperm" -> handleSetPerm(sender, senderUUID, ctx, isAdmin);
                    case "list" -> handleList(sender, senderUUID, isAdmin);
                    case "sell" -> handleSell(sender, senderUUID, ctx, isAdmin);
                    case "buy" -> handleBuy(sender, senderUUID);
                    case "setprice" -> handleSetPrice(sender, senderUUID, ctx, isAdmin);
                    case "setrent" -> handleSetRent(sender, senderUUID, ctx, isAdmin);
                    case "assign" -> handleAssignFamily(sender, senderUUID, ctx, isAdmin);
                    case "assignguild" -> handleAssignGuild(sender, senderUUID, ctx, isAdmin);
                    default -> sender.sendMessage(Message.raw("§cUsage: /es parcel <pos1|pos2|create|delete|info|invite|kick|setperm|list|sell|buy>"));
                }
            } catch (Exception e) {
                sender.sendMessage(Message.raw("§cErreur: " + e.getMessage()));
                e.printStackTrace();
            }
        }, sender.getWorld());
    }

    // ==================== POS1 / POS2 ====================

    /**
     * Lance la capture async de la selection EditorTool.
     * Le callback s'execute au prochain tick du WorldThread.
     * On stocke le resultat dans ParcelManager pour le joueur.
     */
    private void captureEditorSelectionAsync(Player sender, UUID uuid, Runnable onComplete) {
        try {
            SelectionProvider provider = SelectionManager.getSelectionProvider();
            if (provider == null) { onComplete.run(); return; }

            var ref = sender.getReference();
            if (ref == null) { onComplete.run(); return; }

            provider.computeSelectionCopy(ref, sender, selection -> {
                if (selection != null && selection.hasSelectionBounds()) {
                    Vector3i min = selection.getSelectionMin();
                    Vector3i max = selection.getSelectionMax();
                    if (min != null && max != null) {
                        ParcelManager.setPos1(uuid, min.getX(), min.getY(), min.getZ());
                        ParcelManager.setPos2(uuid, max.getX(), max.getY(), max.getZ());
                        System.out.println("[Parcel] Selection EditorTool stockee: " + min + " -> " + max);
                    }
                }
                onComplete.run();
            }, ref.getStore());
        } catch (Exception e) {
            System.out.println("[Parcel] EditorTool: " + e.getMessage());
            onComplete.run();
        }
    }

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
        sender.sendMessage(Message.raw("§aPosition 1 definie : §f" + x + ", " + y + ", " + z));
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
        sender.sendMessage(Message.raw("§aPosition 2 definie : §f" + x + ", " + y + ", " + z));
    }

    // ==================== CREATE ====================

    private void handleCreate(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String typeStr = this.arg1.get(ctx);
        String name = this.arg2.get(ctx);

        ParcelType type;
        try { type = ParcelType.valueOf(typeStr.toUpperCase()); }
        catch (Exception e) {
            sender.sendMessage(Message.raw("§cType invalide. Valides: KINGDOM, TERRITORY, CITY, PLOT, FARM"));
            return;
        }

        // Verifier les droits
        if (!isAdmin) {
            var ref = sender.getReference();
            PlayerLevelData data = ref != null ? ref.getStore().getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType()) : null;
            String rank = data != null ? data.getNobilityRank() : "";

            boolean allowed = switch (type) {
                case KINGDOM -> "ROI".equals(rank);
                case TERRITORY -> "MARQUIS".equals(rank) || "DUC".equals(rank);
                case CITY -> "COMTE".equals(rank);
                case PLOT, HOUSING, ROOM, FARM -> false; // Seulement admin
            };

            if (!allowed) {
                sender.sendMessage(Message.raw("§cVous n'avez pas le rang requis pour creer un " + type.getLabel() + "."));
                return;
            }
        }

        // La selection EditorTool a ete stockee dans pos1/pos2 par le callback async
        if (!ParcelManager.hasFullSelection(uuid)) {
            sender.sendMessage(Message.raw("§cAucune selection detectee !"));
            sender.sendMessage(Message.raw("§7Utilisez le §fSelection Tool §7dans le jeu, ou :"));
            sender.sendMessage(Message.raw("§f/es parcel pos1 _ §7puis §f/es parcel pos2 _"));
            return;
        }

        int[] sel = ParcelManager.getSelection(uuid);

        String world = sender.getWorld().getName();

        // Chercher le parent automatiquement (teste coin + centre de la selection)
        String parentId = null;
        if (type != ParcelType.KINGDOM) {
            // Essayer au coin, puis au centre
            int centerX = (sel[0] + sel[3]) / 2;
            int centerY = (sel[1] + sel[4]) / 2;
            int centerZ = (sel[2] + sel[5]) / 2;

            ParcelData parentParcel = ParcelManager.getParcelAt(world, sel[0], sel[1], sel[2]);
            if (parentParcel == null) parentParcel = ParcelManager.getParcelAt(world, centerX, centerY, centerZ);
            if (parentParcel == null) parentParcel = ParcelManager.getParcelAt(world, sel[3], sel[4], sel[5]);

            if (parentParcel != null) {
                if (ParcelManager.isValidParent(parentParcel.getId(), type)) {
                    parentId = parentParcel.getId();
                } else {
                    sender.sendMessage(Message.raw("§cUn " + type.getLabel() + " ne peut pas etre cree dans un " + parentParcel.getType().getLabel() + "."));
                    return;
                }
            } else if (type != ParcelType.TERRITORY) {
                sender.sendMessage(Message.raw("§cUn " + type.getLabel() + " doit etre cree dans une zone existante."));
                return;
            }
        }

        // Creation — la ville/territoire parent est proprio par defaut pour PLOT/HOUSING
        String id = ParcelManager.createParcel(name, type, null, "", world,
                sel[0], sel[1], sel[2], sel[3], sel[4], sel[5], parentId);

        // Assigner la ville parente comme proprio si PLOT ou HOUSING
        ParcelData created = ParcelManager.get(id);
        if (created != null && (type == ParcelType.PLOT || type == ParcelType.HOUSING)) {
            ParcelManager.assignCityAsOwnerPublic(created);
            ParcelManager.save();
        }

        ParcelManager.clearSelection(uuid);

        int sizeX = Math.abs(sel[3] - sel[0]) + 1;
        int sizeY = Math.abs(sel[4] - sel[1]) + 1;
        int sizeZ = Math.abs(sel[5] - sel[2]) + 1;

        sender.sendMessage(Message.raw("§a§l" + type.getLabel() + " cree ! §f" + name));
        sender.sendMessage(Message.raw("§7ID: " + id + " | Taille: " + sizeX + "x" + sizeY + "x" + sizeZ));
        if (parentId != null) {
            ParcelData parent = ParcelManager.get(parentId);
            sender.sendMessage(Message.raw("§7Parent: " + (parent != null ? parent.getName() : parentId)));
        }
    }

    // ==================== DELETE ====================

    private void handleDelete(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String idOrName = this.arg1.get(ctx);
        ParcelData parcel = findParcel(idOrName);
        if (parcel == null) {
            sender.sendMessage(Message.raw("§cParcelle introuvable: " + idOrName));
            return;
        }

        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.sendMessage(Message.raw("§cVous n'etes pas proprietaire de cette parcelle."));
            return;
        }

        String name = parcel.getName();
        ParcelManager.deleteParcel(parcel.getId());
        sender.sendMessage(Message.raw("§cParcelle supprimee : §f" + name));
    }

    // ==================== INFO ====================

    private void handleInfo(Player sender, UUID uuid) {
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        Vector3d pos = transform.getPosition();
        String world = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(world, pos.x, pos.y, pos.z);

        if (parcel == null) {
            sender.sendMessage(Message.raw("§7Vous etes en zone sauvage (aucune parcelle)."));
            return;
        }

        sender.sendMessage(Message.raw("§6=== " + parcel.getType().getLabel() + " : " + parcel.getName() + " ==="));
        sender.sendMessage(Message.raw("§7ID: §f" + parcel.getId()));
        sender.sendMessage(Message.raw("§7Proprietaire: §e" + parcel.getOwnerName()));
        sender.sendMessage(Message.raw("§7Protection: " + (parcel.isProtectedByDefault() ? "§aActivee" : "§cDesactivee")));
        sender.sendMessage(Message.raw("§7Membres: §f" + parcel.getMembers().size()));

        int sizeX = parcel.getMaxX() - parcel.getMinX() + 1;
        int sizeY = parcel.getMaxY() - parcel.getMinY() + 1;
        int sizeZ = parcel.getMaxZ() - parcel.getMinZ() + 1;
        sender.sendMessage(Message.raw("§7Taille: §f" + sizeX + "x" + sizeY + "x" + sizeZ));

        if (parcel.isForSale()) sender.sendMessage(Message.raw("§eEn vente : " + parcel.getPrice() + " Or"));
        if (parcel.isForRent()) sender.sendMessage(Message.raw("§eEn location : " + parcel.getTaxRate() + " Or/jour"));

        ParcelRole myRole = parcel.getRole(uuid);
        sender.sendMessage(Message.raw("§7Votre role: §f" + (myRole != null ? myRole.name() : "Aucun")));
    }

    // ==================== INVITE ====================

    private void handleInvite(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String playerName = this.arg1.get(ctx);

        // Trouver la parcelle ou se trouve le sender
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String world = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(world, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid) && parcel.getRole(uuid) != ParcelRole.OFFICER) {
            sender.sendMessage(Message.raw("§cVous n'avez pas les droits pour inviter.")); return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) { sender.sendMessage(Message.raw("§cJoueur introuvable.")); return; }

        try {
            Field f = PlayerRef.class.getDeclaredField("uuid");
            f.setAccessible(true);
            UUID targetUUID = (UUID) f.get(targetRef);

            parcel.addMember(targetUUID, ParcelRole.MEMBER);
            ParcelManager.save();

            sender.sendMessage(Message.raw("§a" + playerName + " ajoute a la parcelle " + parcel.getName()));
            targetRef.sendMessage(Message.raw("§eVous avez ete ajoute a la parcelle §f" + parcel.getName() + " §epar §f" + sender.getDisplayName()));
        } catch (Exception e) {
            sender.sendMessage(Message.raw("§cErreur: " + e.getMessage()));
        }
    }

    // ==================== KICK ====================

    private void handleKick(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String playerName = this.arg1.get(ctx);

        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String world = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(world, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.sendMessage(Message.raw("§cSeul le proprietaire peut exclure.")); return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) { sender.sendMessage(Message.raw("§cJoueur introuvable.")); return; }

        try {
            Field f = PlayerRef.class.getDeclaredField("uuid");
            f.setAccessible(true);
            UUID targetUUID = (UUID) f.get(targetRef);

            parcel.removeMember(targetUUID);
            ParcelManager.save();

            sender.sendMessage(Message.raw("§a" + playerName + " retire de la parcelle."));
        } catch (Exception e) {
            sender.sendMessage(Message.raw("§cErreur: " + e.getMessage()));
        }
    }

    // ==================== SETPERM ====================

    private void handleSetPerm(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        // /es parcel setperm <role:permission> <true|false>
        String rolePermStr = this.arg1.get(ctx);
        String valueStr = this.arg2.get(ctx);

        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String world = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(world, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.sendMessage(Message.raw("§cSeul le proprietaire peut changer les permissions.")); return;
        }

        String[] parts = rolePermStr.split(":");
        if (parts.length != 2) {
            sender.sendMessage(Message.raw("§cFormat: /es parcel setperm <ROLE:PERMISSION> <true|false>"));
            sender.sendMessage(Message.raw("§7Roles: OWNER, OFFICER, MEMBER, VISITOR"));
            sender.sendMessage(Message.raw("§7Permissions: BUILD, BREAK, INTERACT, PVP, ENTER"));
            return;
        }

        try {
            ParcelRole role = ParcelRole.valueOf(parts[0].toUpperCase());
            ParcelPermission perm = ParcelPermission.valueOf(parts[1].toUpperCase());
            boolean allowed = Boolean.parseBoolean(valueStr);

            parcel.setRolePermission(role, perm, allowed);
            ParcelManager.save();

            sender.sendMessage(Message.raw("§aPermission " + perm.name() + " pour " + role.name() + " = " + (allowed ? "§aOUI" : "§cNON")));
        } catch (Exception e) {
            sender.sendMessage(Message.raw("§cRole ou permission invalide."));
        }
    }

    // ==================== LIST ====================

    private void handleList(Player sender, UUID uuid, boolean isAdmin) {
        List<ParcelData> myParcels = ParcelManager.getParcelsOwnedBy(uuid);

        if (isAdmin) {
            sender.sendMessage(Message.raw("§6=== Toutes les parcelles (" + ParcelManager.getAll().size() + ") ==="));
            for (ParcelData p : ParcelManager.getAll()) {
                sender.sendMessage(Message.raw("§7[" + p.getType().getLabel() + "] §f" + p.getName() + " §7par §e" + p.getOwnerName() + " §7(ID: " + p.getId() + ")"));
            }
        } else {
            sender.sendMessage(Message.raw("§6=== Mes parcelles (" + myParcels.size() + ") ==="));
            for (ParcelData p : myParcels) {
                sender.sendMessage(Message.raw("§7[" + p.getType().getLabel() + "] §f" + p.getName() + " §7(ID: " + p.getId() + ")"));
            }
        }
    }

    // ==================== SELL ====================

    private void handleSell(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String priceStr = this.arg1.get(ctx);
        long price;
        try { price = Long.parseLong(priceStr); }
        catch (Exception e) { sender.sendMessage(Message.raw("§cPrix invalide.")); return; }

        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String world = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(world, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.sendMessage(Message.raw("§cSeul le proprietaire peut vendre.")); return;
        }

        parcel.setForSale(true);
        parcel.setPrice(price);
        ParcelManager.save();

        sender.sendMessage(Message.raw("§aParcelle mise en vente pour §f" + price + " Or§a !"));
    }

    // ==================== BUY ====================

    private void handleBuy(Player sender, UUID uuid) {
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String world = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(world, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.sendMessage(Message.raw("§cAucune parcelle ici.")); return; }
        if (!parcel.isForSale()) { sender.sendMessage(Message.raw("§cCette parcelle n'est pas en vente.")); return; }
        if (parcel.isOwner(uuid)) { sender.sendMessage(Message.raw("§cVous etes deja proprietaire.")); return; }

        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null) return;

        if (data.getMoney() < parcel.getPrice()) {
            sender.sendMessage(Message.raw("§cPas assez d'or ! Il faut " + parcel.getPrice() + " Or."));
            return;
        }

        // Transfert
        data.addMoney(-parcel.getPrice());
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), data);

        // Ancien proprietaire recoit l'or
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
                            oldData.addMoney(parcel.getPrice());
                            oldStore.putComponent(oldEntRef, EldaniorSystem.get().getPlayerLevelDataType(), oldData);
                        }
                    }
                    oldRef.sendMessage(Message.raw("§a" + sender.getDisplayName() + " a achete votre parcelle " + parcel.getName() + " pour " + parcel.getPrice() + " Or !"));
                } catch (Exception ignored) {}
            }
        }

        parcel.setOwnerUUID(uuid);
        parcel.setOwnerName(sender.getDisplayName());
        parcel.setForSale(false);
        parcel.getMembers().clear();
        parcel.addMember(uuid, ParcelRole.OWNER);
        ParcelManager.save();

        sender.sendMessage(Message.raw("§a§lParcelle achetee : §f" + parcel.getName() + " §apour §f" + parcel.getPrice() + " Or§a !"));
    }

    // ==================== SET PRICE / RENT ====================

    private void handleSetPrice(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String priceStr = this.arg1.get(ctx);
        long price;
        try { price = Long.parseLong(priceStr); } catch (Exception e) { sender.sendMessage(Message.raw("§cPrix invalide.")); return; }

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) { sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) { sender.sendMessage(Message.raw("§cPas proprietaire.")); return; }

        parcel.setPrice(price);
        parcel.setForSale(price > 0);
        ParcelManager.save();
        sender.sendMessage(Message.raw("§aPrix de vente defini : §f" + price + " Or" + (price == 0 ? " (retire de la vente)" : "")));
    }

    private void handleSetRent(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String priceStr = this.arg1.get(ctx);
        long rentPrice;
        try { rentPrice = Long.parseLong(priceStr); } catch (Exception e) { sender.sendMessage(Message.raw("§cPrix invalide.")); return; }

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) { sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) { sender.sendMessage(Message.raw("§cPas proprietaire.")); return; }

        parcel.setRentPrice(rentPrice);
        parcel.setForRent(rentPrice > 0);
        ParcelManager.save();
        sender.sendMessage(Message.raw("§aPrix de location defini : §f" + rentPrice + " Or/7j" + (rentPrice == 0 ? " (retire de la location)" : "")));
    }

    // ==================== ASSIGN FAMILY ====================

    private void handleAssignFamily(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        if (!isAdmin) {
            sender.sendMessage(Message.raw("§cCommande admin uniquement."));
            return;
        }

        String familyId = this.arg1.get(ctx);

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) {
            sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle."));
            return;
        }

        // Verifier que c'est un territoire/royaume/ville (pas un plot)
        if (parcel.getType() == ParcelType.PLOT) {
            sender.sendMessage(Message.raw("§cLes plots ne peuvent pas etre assignes a une famille."));
            return;
        }

        ParcelManager.assignToFamily(parcel.getId(), familyId);
        sender.sendMessage(Message.raw("§a" + parcel.getType().getLabel() + " §f" + parcel.getName() + " §aassigne a la famille §f" + familyId));
    }

    private void handleAssignGuild(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        if (!isAdmin) {
            sender.sendMessage(Message.raw("§cCommande admin uniquement."));
            return;
        }

        String guildIdOrName = this.arg1.get(ctx);

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) {
            sender.sendMessage(Message.raw("§cVous n'etes dans aucune parcelle."));
            return;
        }

        if (parcel.getType() != ParcelType.CITY) {
            sender.sendMessage(Message.raw("§cSeules les villes peuvent etre assignees a une guilde."));
            return;
        }

        parcel.setGuildId(guildIdOrName);
        ParcelManager.save();
        sender.sendMessage(Message.raw("§aVille §f" + parcel.getName() + " §aassignee a la guilde §f" + guildIdOrName));
    }

    private ParcelData getParcelAtPlayer(Player sender) {
        try {
            var ref = sender.getReference();
            if (ref == null) return null;
            var store = ref.getStore();
            var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            if (transform == null || sender.getWorld() == null) return null;
            return ParcelManager.getParcelAt(sender.getWorld().getName(), transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        } catch (Exception e) { return null; }
    }

    // ==================== UTILS ====================

    private ParcelData findParcel(String idOrName) {
        ParcelData p = ParcelManager.get(idOrName);
        if (p != null) return p;
        for (ParcelData parcel : ParcelManager.getAll()) {
            if (parcel.getName().equalsIgnoreCase(idOrName)) return parcel;
        }
        return null;
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        Field f = PlayerRef.class.getDeclaredField("uuid");
        f.setAccessible(true);
        return (UUID) f.get(pRef);
    }
}
