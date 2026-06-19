package com.eldanior.system.territory.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.territory.*;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3d;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.prefab.selection.SelectionManager;
import com.hypixel.hytale.server.core.prefab.selection.SelectionProvider;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
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
    private final OptionalArg<String> arg1;
    private final OptionalArg<String> arg2;

    public ParcelCommand() {
        super("parcel", "Gestion des parcelles (pos1|pos2|create|delete|info|invite|kick|setperm|list|sell|buy)");
        this.actionArg = this.withRequiredArg("action", "pos1|pos2|create|delete|info|invite|kick|setperm|list|sell|buy", ArgTypes.STRING);
        this.arg1 = this.withOptionalArg("arg1", "type/nom/joueur", ArgTypes.STRING);
        this.arg2 = this.withOptionalArg("arg2", "nom/valeur", ArgTypes.STRING);
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
                    case "setrank" -> handleSetRank(sender, senderUUID, ctx, isAdmin);
                    case "setregen" -> handleSetRegen(sender, senderUUID, ctx, isAdmin);
                    default -> sender.getPlayerRef().sendMessage(Message.raw("§cUsage: /es parcel <pos1|pos2|create|delete|info|invite|kick|setperm|list|sell|buy|setrank|setregen>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
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
                        ParcelManager.setPos1(uuid, min.x(), min.y(), min.z());
                        ParcelManager.setPos2(uuid, max.x(), max.y(), max.z());
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

    // ==================== CREATE ====================

    private void handleCreate(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String typeStr = this.arg1.get(ctx);
        String name = this.arg2.get(ctx);
        if (typeStr == null || typeStr.isEmpty() || name == null || name.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel create <type> <nom>"));
            return;
        }

        ParcelType type;
        try { type = ParcelType.valueOf(typeStr.toUpperCase()); }
        catch (Exception e) {
            sender.getPlayerRef().sendMessage(Message.raw("§cType invalide. Valides: KINGDOM, TERRITORY, CITY, PLOT, HOUSING, ROOM, FARM"));
            return;
        }

        // Verifier les droits
        if (!isAdmin) {
            var ref = sender.getReference();
            PlayerLevelData data = ref != null ? ref.getStore().getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType()) : null;
            String rank = data != null ? data.getNobilityRank() : "";

            boolean allowed = switch (type) {
                case KINGDOM -> "ROI".equals(rank);
                case GRAND_TERRITORY -> "MARQUIS".equals(rank);
                case TERRITORY -> "MARQUIS".equals(rank) || "DUC".equals(rank);
                case CITY -> "COMTE".equals(rank);
                case PLOT, HOUSING, ROOM, FARM, FOREST -> false;
                case ARENA, DUNGEON, MINE -> false;
            };

            if (!allowed) {
                sender.getPlayerRef().sendMessage(Message.raw("§cVous n'avez pas le rang requis pour creer un " + type.getLabel() + "."));
                return;
            }
        }

        String worldStr = sender.getWorld().getName();

        // KINGDOM : auto-generation complete (royaume + domaine royal + 4 marquisats + 8 duches)
        if (type == ParcelType.KINGDOM) {
            var ref2 = sender.getReference();
            if (ref2 == null) return;
            var transform2 = ref2.getStore().getComponent(ref2,
                    com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
            if (transform2 == null) return;

            Vector3d playerPos = transform2.getPosition();
            int cx = (int) Math.floor(playerPos.x);
            int cz = (int) Math.floor(playerPos.z);

            generateKingdom(sender, name, worldStr, cx, cz);

            ParcelManager.clearSelection(uuid);
            return;
        }

        // Autres types : selection manuelle requise
        if (!ParcelManager.hasFullSelection(uuid)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cAucune selection detectee !"));
            sender.getPlayerRef().sendMessage(Message.raw("§7Utilisez le §fSelection Tool §7dans le jeu, ou :"));
            sender.getPlayerRef().sendMessage(Message.raw("§f/es parcel pos1 §7puis §f/es parcel pos2"));
            return;
        }

        int[] sel = ParcelManager.getSelection(uuid);

        // Chercher le parent automatiquement (teste coin + centre de la selection)
        String parentId = null;
        if (type != ParcelType.KINGDOM) {
            // Essayer au coin, puis au centre
            int centerX = (sel[0] + sel[3]) / 2;
            int centerY = (sel[1] + sel[4]) / 2;
            int centerZ = (sel[2] + sel[5]) / 2;

            ParcelData parentParcel = ParcelManager.getParcelAt(worldStr, sel[0], sel[1], sel[2]);
            if (parentParcel == null) parentParcel = ParcelManager.getParcelAt(worldStr, centerX, centerY, centerZ);
            if (parentParcel == null) parentParcel = ParcelManager.getParcelAt(worldStr, sel[3], sel[4], sel[5]);

            if (parentParcel != null) {
                if (ParcelManager.isValidParent(parentParcel.getId(), type)) {
                    parentId = parentParcel.getId();
                } else {
                    sender.getPlayerRef().sendMessage(Message.raw("§cUn " + type.getLabel() + " ne peut pas etre cree dans un " + parentParcel.getType().getLabel() + "."));
                    return;
                }
            } else if (type != ParcelType.TERRITORY && type != ParcelType.CITY) {
                sender.getPlayerRef().sendMessage(Message.raw("§cUn " + type.getLabel() + " doit etre cree dans une zone existante."));
                return;
            }
        }

        // Creation — la ville/territoire parent est proprio par defaut pour PLOT/HOUSING
        String id = ParcelManager.createParcel(name, type, null, "", worldStr,
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

        sender.getPlayerRef().sendMessage(Message.raw("§a§l" + type.getLabel() + " cree ! §f" + name));
        sender.getPlayerRef().sendMessage(Message.raw("§7ID: " + id + " | Taille: " + sizeX + "x" + sizeY + "x" + sizeZ));
        if (parentId != null) {
            ParcelData parent = ParcelManager.get(parentId);
            sender.getPlayerRef().sendMessage(Message.raw("§7Parent: " + (parent != null ? parent.getName() : parentId)));
        }
    }

    // ==================== DELETE ====================

    private void handleDelete(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String idOrName = this.arg1.get(ctx);
        if (idOrName == null || idOrName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel delete <id_ou_nom>"));
            return;
        }
        ParcelData parcel = findParcel(idOrName);
        if (parcel == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cParcelle introuvable: " + idOrName));
            return;
        }

        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes pas proprietaire de cette parcelle."));
            return;
        }

        String name = parcel.getName();
        ParcelManager.deleteParcel(parcel.getId());
        sender.getPlayerRef().sendMessage(Message.raw("§cParcelle supprimee : §f" + name));
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

    // ==================== INVITE ====================

    private void handleInvite(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String playerName = this.arg1.get(ctx);
        if (playerName == null || playerName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel invite <joueur>"));
            return;
        }

        // Trouver la parcelle ou se trouve le sender
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String worldStr = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(worldStr, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid) && parcel.getRole(uuid) != ParcelRole.OFFICER) {
            sender.getPlayerRef().sendMessage(Message.raw("§cVous n'avez pas les droits pour inviter.")); return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("§cJoueur introuvable.")); return; }

        try {
                        UUID targetUUID = UUIDExtractor.getUUID(targetRef);

            parcel.addMember(targetUUID, ParcelRole.MEMBER);
            ParcelManager.save();

            sender.getPlayerRef().sendMessage(Message.raw("§a" + playerName + " ajoute a la parcelle " + parcel.getName()));
            targetRef.sendMessage(Message.raw("§eVous avez ete ajoute a la parcelle §f" + parcel.getName() + " §epar §f" + sender.getPlayerRef().getUsername()));
        } catch (Exception e) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur: " + e.getMessage()));
        }
    }

    // ==================== KICK ====================

    private void handleKick(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String playerName = this.arg1.get(ctx);
        if (playerName == null || playerName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel kick <joueur>"));
            return;
        }

        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String worldStr = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(worldStr, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cSeul le proprietaire peut exclure.")); return;
        }

        PlayerRef targetRef = Universe.get().getPlayerByUsername(playerName, NameMatching.EXACT_IGNORE_CASE);
        if (targetRef == null) { sender.getPlayerRef().sendMessage(Message.raw("§cJoueur introuvable.")); return; }

        try {
                        UUID targetUUID = UUIDExtractor.getUUID(targetRef);

            parcel.removeMember(targetUUID);
            ParcelManager.save();

            sender.getPlayerRef().sendMessage(Message.raw("§a" + playerName + " retire de la parcelle."));
        } catch (Exception e) {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur: " + e.getMessage()));
        }
    }

    // ==================== SETPERM ====================

    private void handleSetPerm(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        // /es parcel setperm <role:permission> <true|false>
        String rolePermStr = this.arg1.get(ctx);
        String valueStr = this.arg2.get(ctx);
        if (rolePermStr == null || rolePermStr.isEmpty() || valueStr == null || valueStr.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel setperm <ROLE:PERMISSION> <true|false>"));
            return;
        }

        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String worldStr = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(worldStr, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cSeul le proprietaire peut changer les permissions.")); return;
        }

        String[] parts = rolePermStr.split(":");
        if (parts.length != 2) {
            sender.getPlayerRef().sendMessage(Message.raw("§cFormat: /es parcel setperm <ROLE:PERMISSION> <true|false>"));
            sender.getPlayerRef().sendMessage(Message.raw("§7Roles: OWNER, OFFICER, MEMBER, VISITOR"));
            sender.getPlayerRef().sendMessage(Message.raw("§7Permissions: BUILD, BREAK, INTERACT, PVP, ENTER"));
            return;
        }

        try {
            ParcelRole role = ParcelRole.valueOf(parts[0].toUpperCase());
            ParcelPermission perm = ParcelPermission.valueOf(parts[1].toUpperCase());
            boolean allowed = Boolean.parseBoolean(valueStr);

            parcel.setRolePermission(role, perm, allowed);
            ParcelManager.save();

            sender.getPlayerRef().sendMessage(Message.raw("§aPermission " + perm.name() + " pour " + role.name() + " = " + (allowed ? "§aOUI" : "§cNON")));
        } catch (Exception e) {
            sender.getPlayerRef().sendMessage(Message.raw("§cRole ou permission invalide."));
        }
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

    // ==================== SELL ====================

    private void handleSell(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String priceStr = this.arg1.get(ctx);
        if (priceStr == null || priceStr.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel sell <prix>"));
            return;
        }
        long price;
        try { price = Long.parseLong(priceStr); }
        catch (Exception e) { sender.getPlayerRef().sendMessage(Message.raw("§cPrix invalide.")); return; }

        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();
        var transform = store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        String worldStr = sender.getWorld().getName();
        ParcelData parcel = ParcelManager.getParcelAt(worldStr, transform.getPosition().x, transform.getPosition().y, transform.getPosition().z);
        if (parcel == null) { sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cSeul le proprietaire peut vendre.")); return;
        }

        parcel.setForSale(true);
        parcel.setPrice(price);
        ParcelManager.save();

        sender.getPlayerRef().sendMessage(Message.raw("§aParcelle mise en vente pour §f" + price + " Or§a !"));
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

    // ==================== SET PRICE / RENT ====================

    private void handleSetPrice(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String priceStr = this.arg1.get(ctx);
        if (priceStr == null || priceStr.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel setprice <prix>"));
            return;
        }
        long price;
        try { price = Long.parseLong(priceStr); } catch (Exception e) { sender.getPlayerRef().sendMessage(Message.raw("§cPrix invalide.")); return; }

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) { sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) { sender.getPlayerRef().sendMessage(Message.raw("§cPas proprietaire.")); return; }

        parcel.setPrice(price);
        parcel.setForSale(price > 0);
        ParcelManager.save();
        sender.getPlayerRef().sendMessage(Message.raw("§aPrix de vente defini : §f" + price + " Or" + (price == 0 ? " (retire de la vente)" : "")));
    }

    private void handleSetRent(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        String priceStr = this.arg1.get(ctx);
        if (priceStr == null || priceStr.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel setrent <prix>"));
            return;
        }
        long rentPrice;
        try { rentPrice = Long.parseLong(priceStr); } catch (Exception e) { sender.getPlayerRef().sendMessage(Message.raw("§cPrix invalide.")); return; }

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) { sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle.")); return; }
        if (!isAdmin && !parcel.isOwner(uuid)) { sender.getPlayerRef().sendMessage(Message.raw("§cPas proprietaire.")); return; }

        parcel.setRentPrice(rentPrice);
        parcel.setForRent(rentPrice > 0);
        ParcelManager.save();
        sender.getPlayerRef().sendMessage(Message.raw("§aPrix de location defini : §f" + rentPrice + " Or/7j" + (rentPrice == 0 ? " (retire de la location)" : "")));
    }

    // ==================== ASSIGN FAMILY ====================

    private void handleAssignFamily(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        if (!isAdmin) {
            sender.getPlayerRef().sendMessage(Message.raw("§cCommande admin uniquement."));
            return;
        }

        String familyId = this.arg1.get(ctx);
        if (familyId == null || familyId.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel assign <familyId>"));
            return;
        }

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle."));
            return;
        }

        // Verifier que c'est un territoire/royaume/ville (pas un plot)
        if (parcel.getType() == ParcelType.PLOT) {
            sender.getPlayerRef().sendMessage(Message.raw("§cLes plots ne peuvent pas etre assignes a une famille."));
            return;
        }

        ParcelManager.assignToFamily(parcel.getId(), familyId);
        sender.getPlayerRef().sendMessage(Message.raw("§a" + parcel.getType().getLabel() + " §f" + parcel.getName() + " §aassigne a la famille §f" + familyId));
    }

    private void handleAssignGuild(Player sender, UUID uuid, CommandContext ctx, boolean isAdmin) {
        if (!isAdmin) {
            sender.getPlayerRef().sendMessage(Message.raw("§cCommande admin uniquement."));
            return;
        }

        String guildIdOrName = this.arg1.get(ctx);
        if (guildIdOrName == null || guildIdOrName.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage : /es parcel assignguild <guilde>"));
            return;
        }

        ParcelData parcel = getParcelAtPlayer(sender);
        if (parcel == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cVous n'etes dans aucune parcelle."));
            return;
        }

        if (parcel.getType() != ParcelType.CITY) {
            sender.getPlayerRef().sendMessage(Message.raw("§cSeules les villes peuvent etre assignees a une guilde."));
            return;
        }

        parcel.setGuildId(guildIdOrName);
        ParcelManager.save();
        sender.getPlayerRef().sendMessage(Message.raw("§aVille §f" + parcel.getName() + " §aassignee a la guilde §f" + guildIdOrName));
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

    // ==================== SETRANK (donjon) ====================

    private void handleSetRank(Player sender, UUID senderUUID, CommandContext ctx, boolean isAdmin) {
        if (!isAdmin) {
            sender.getPlayerRef().sendMessage(Message.raw("§cCommande admin uniquement."));
            return;
        }
        String rankStr = this.arg1.get(ctx);
        if (rankStr == null || rankStr.isEmpty() || !java.util.Set.of("E", "D", "C", "B", "A", "S").contains(rankStr.toUpperCase())) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage: /es parcel setrank <E|D|C|B|A|S>"));
            return;
        }
        var ref = sender.getReference();
        if (ref == null) return;
        var transform = ref.getStore().getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;
        Vector3d pos = transform.getPosition();
        ParcelData parcel = ParcelManager.getParcelAt(sender.getWorld().getName(), pos.x, pos.y, pos.z);
        if (parcel == null || parcel.getType() != ParcelType.DUNGEON) {
            sender.getPlayerRef().sendMessage(Message.raw("§cVous devez etre dans un donjon."));
            return;
        }
        parcel.setDungeonRank(rankStr.toUpperCase());
        ParcelManager.save();
        sender.getPlayerRef().sendMessage(Message.raw("§aRank du donjon §e" + parcel.getName() + "§a defini a §6" + rankStr.toUpperCase()));
    }

    // ==================== SETREGEN (mine/farm/forest) ====================

    private void handleSetRegen(Player sender, UUID senderUUID, CommandContext ctx, boolean isAdmin) {
        if (!isAdmin) {
            sender.getPlayerRef().sendMessage(Message.raw("§cCommande admin uniquement."));
            return;
        }
        String delayStr = this.arg1.get(ctx);
        if (delayStr == null || delayStr.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage: /es parcel setregen <secondes>"));
            return;
        }
        int delaySec;
        try { delaySec = Integer.parseInt(delayStr); }
        catch (NumberFormatException e) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage: /es parcel setregen <secondes>"));
            return;
        }
        var ref = sender.getReference();
        if (ref == null) return;
        var transform = ref.getStore().getComponent(ref, com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;
        Vector3d pos = transform.getPosition();
        ParcelData parcel = ParcelManager.getParcelAt(sender.getWorld().getName(), pos.x, pos.y, pos.z);
        if (parcel == null || (parcel.getType() != ParcelType.FARM && parcel.getType() != ParcelType.MINE && parcel.getType() != ParcelType.FOREST)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cVous devez etre dans une mine, farm ou foret."));
            return;
        }
        parcel.setRegenDelaySec(delaySec);
        ParcelManager.save();
        sender.getPlayerRef().sendMessage(Message.raw("§aDelai de regeneration de §e" + parcel.getName() + "§a defini a §6" + delaySec + "s"));
    }

    // ==================== GENERATION AUTO ROYAUME ====================

    /**
     * Genere un Royaume complet avec :
     * - 1 Royaume (10000x10000)
     * - 1 Domaine Royal au centre (2000x2000)
     * - 4 Marquisats dans les coins (4000x4000)
     * - 8 Duches (2 par Marquisat, 2000x4000 chacun)
     * - 4 zones neutres (bras de la croix) rattachees au Royaume
     *
     * Layout (vue du dessus, 10000x10000) :
     *  +----------+------+----------+
     *  | Marq NO  |Neutre| Marq NE  |
     *  | (2 ducs) | Nord | (2 ducs) |
     *  +----------+------+----------+
     *  |Neutre Ou.|Royal |Neutre Est|
     *  +----------+------+----------+
     *  | Marq SO  |Neutre| Marq SE  |
     *  | (2 ducs) | Sud  | (2 ducs) |
     *  +----------+------+----------+
     */
    private void generateKingdom(Player sender, String kingdomName, String worldStr, int cx, int cz) {
        int H = 5000;   // demi-taille royaume (10000/2)
        int M = 4000;    // taille marquisat
        int N = 1000;    // demi-taille zone neutre/royale (2000/2)
        int yMin = 0;
        int yMax = 319;

        // Bornes du Royaume
        int kx1 = cx - H, kz1 = cz - H;
        int kx2 = cx + H, kz2 = cz + H;

        // 1. Creer le Royaume + assigner famille royale
        String kingdomId = ParcelManager.createParcel(kingdomName, ParcelType.KINGDOM,
                null, "", worldStr, kx1, yMin, kz1, kx2, yMax, kz2, null);
        ParcelManager.assignToFamily(kingdomId, "eldanior");

        sender.getPlayerRef().sendMessage(Message.raw("§6§l=== CREATION DU ROYAUME ==="));
        sender.getPlayerRef().sendMessage(Message.raw("§a§lRoyaume §f" + kingdomName + " §a§lcree ! §7(Famille Eldanior)"));
        sender.getPlayerRef().sendMessage(Message.raw("§7Centre: " + cx + ", " + cz + " | 10000x10000"));

        // 2. Domaine Royal au centre (2000x2000) — famille royale
        String royalId = ParcelManager.createParcel("Domaine_Royal", ParcelType.TERRITORY,
                null, "", worldStr,
                cx - N, yMin, cz - N, cx + N, yMax, cz + N, kingdomId);
        ParcelManager.assignToFamily(royalId, "eldanior");
        sender.getPlayerRef().sendMessage(Message.raw("§e  Domaine Royal §7(Famille Eldanior)"));

        // 3. Quatre Marquisats (coins) — chacun 4000x4000
        String marqNO = ParcelManager.createParcel("Marquisat_Zippel", ParcelType.GRAND_TERRITORY,
                null, "", worldStr,
                kx1, yMin, kz1, kx1 + M, yMax, kz1 + M, kingdomId);
        ParcelManager.assignToFamily(marqNO, "zippel");

        String marqNE = ParcelManager.createParcel("Marquisat_Runkandel", ParcelType.GRAND_TERRITORY,
                null, "", worldStr,
                kx2 - M, yMin, kz1, kx2, yMax, kz1 + M, kingdomId);
        ParcelManager.assignToFamily(marqNE, "runkandel");

        String marqSO = ParcelManager.createParcel("Marquisat_Luminara", ParcelType.GRAND_TERRITORY,
                null, "", worldStr,
                kx1, yMin, kz2 - M, kx1 + M, yMax, kz2, kingdomId);
        ParcelManager.assignToFamily(marqSO, "luminara");

        String marqSE = ParcelManager.createParcel("Marquisat_Valmontis", ParcelType.GRAND_TERRITORY,
                null, "", worldStr,
                kx2 - M, yMin, kz2 - M, kx2, yMax, kz2, kingdomId);
        ParcelManager.assignToFamily(marqSE, "valmontis");

        sender.getPlayerRef().sendMessage(Message.raw("§e  4 Marquisats §7(Zippel, Runkandel, Luminara, Valmontis)"));

        // 4. Duches (2 par Marquisat, avec espace central pour le Marquisat)
        int duchW = 1500;
        int gap = 1000;

        String dNO1 = ParcelManager.createParcel("Duche_Frostguard", ParcelType.TERRITORY,
                null, "", worldStr,
                kx1, yMin, kz1, kx1 + duchW, yMax, kz1 + M, marqNO);
        ParcelManager.assignToFamily(dNO1, "frostguard");

        String dNO2 = ParcelManager.createParcel("Duche_Spellweave", ParcelType.TERRITORY,
                null, "", worldStr,
                kx1 + duchW + gap, yMin, kz1, kx1 + M, yMax, kz1 + M, marqNO);
        ParcelManager.assignToFamily(dNO2, "spellweave");

        String dNE1 = ParcelManager.createParcel("Duche_Ironveil", ParcelType.TERRITORY,
                null, "", worldStr,
                kx2 - M, yMin, kz1, kx2 - M + duchW, yMax, kz1 + M, marqNE);
        ParcelManager.assignToFamily(dNE1, "ironveil");

        String dNE2 = ParcelManager.createParcel("Duche_Warbane", ParcelType.TERRITORY,
                null, "", worldStr,
                kx2 - duchW, yMin, kz1, kx2, yMax, kz1 + M, marqNE);
        ParcelManager.assignToFamily(dNE2, "warbane");

        String dSO1 = ParcelManager.createParcel("Duche_Nighthollow", ParcelType.TERRITORY,
                null, "", worldStr,
                kx1, yMin, kz2 - M, kx1 + duchW, yMax, kz2, marqSO);
        ParcelManager.assignToFamily(dSO1, "nighthollow");

        String dSO2 = ParcelManager.createParcel("Duche_Swiftquiver", ParcelType.TERRITORY,
                null, "", worldStr,
                kx1 + duchW + gap, yMin, kz2 - M, kx1 + M, yMax, kz2, marqSO);
        ParcelManager.assignToFamily(dSO2, "swiftquiver");

        String dSE1 = ParcelManager.createParcel("Duche_Goldcrest", ParcelType.TERRITORY,
                null, "", worldStr,
                kx2 - M, yMin, kz2 - M, kx2 - M + duchW, yMax, kz2, marqSE);
        ParcelManager.assignToFamily(dSE1, "goldcrest");

        String dSE2 = ParcelManager.createParcel("Duche_Silkroad", ParcelType.TERRITORY,
                null, "", worldStr,
                kx2 - duchW, yMin, kz2 - M, kx2, yMax, kz2, marqSE);
        ParcelManager.assignToFamily(dSE2, "silkroad");

        sender.getPlayerRef().sendMessage(Message.raw("§e  8 Duches §7(familles assignees automatiquement)"));
        sender.getPlayerRef().sendMessage(Message.raw("§e  4 Zones Neutres §7(bras de la croix, terres de la couronne)"));
        sender.getPlayerRef().sendMessage(Message.raw("§e  1 Domaine Royal §7(Famille Eldanior)"));
        sender.getPlayerRef().sendMessage(Message.raw("§6§l=== " + (1 + 1 + 4 + 8) + " parcelles creees ! ==="));

        ParcelManager.optimizeHierarchy();
    }

    private UUID getSenderUUID(Player sender) throws Exception {
        var ref = sender.getReference();
        if (ref == null) return null;
        Store<EntityStore> store = ref.getStore();
        PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (pRef == null) return null;
        return UUIDExtractor.getUUID(pRef);
    }
}