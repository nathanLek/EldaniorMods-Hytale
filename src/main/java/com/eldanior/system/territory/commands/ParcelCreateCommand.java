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
import com.hypixel.hytale.server.core.prefab.selection.SelectionManager;
import com.hypixel.hytale.server.core.prefab.selection.SelectionProvider;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /es pcreate <TYPE> <NOM> — Creer une parcelle
 */
public class ParcelCreateCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> typeArg;
    private final RequiredArg<String> nameArg;

    public ParcelCreateCommand() {
        super("pcreate", "Creer une parcelle (KINGDOM, TERRITORY, CITY, PLOT, etc.)");
        this.typeArg = this.withRequiredArg("type", "KINGDOM|TERRITORY|CITY|PLOT|HOUSING|ROOM|FARM", ArgTypes.STRING);
        this.nameArg = this.withRequiredArg("nom", "Nom de la parcelle", ArgTypes.STRING);
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

        String typeStr = this.typeArg.get(ctx);
        String name = this.nameArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                UUID senderUUID = UUIDExtractor.getUUID(senderRef);
                if (senderUUID == null) return;
                boolean isAdmin = senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION);

                // KINGDOM : capture la selection EditorTool d'abord pour les non-KINGDOM
                if ("KINGDOM".equalsIgnoreCase(typeStr)) {
                    handleCreate(sender, senderUUID, typeStr, name, isAdmin);
                } else {
                    captureEditorSelectionAsync(sender, senderUUID, () ->
                            handleCreate(sender, senderUUID, typeStr, name, isAdmin));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

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
                    }
                }
                onComplete.run();
            }, ref.getStore());
        } catch (Exception e) {
            onComplete.run();
        }
    }

    private void handleCreate(Player sender, UUID uuid, String typeStr, String name, boolean isAdmin) {
        if (typeStr == null || typeStr.isEmpty() || name == null || name.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Usage : /es pcreate <type> <nom>"));
            return;
        }

        ParcelType type;
        try { type = ParcelType.valueOf(typeStr.toUpperCase()); }
        catch (Exception e) {
            sender.getPlayerRef().sendMessage(Message.raw("Type invalide. Valides: KINGDOM, TERRITORY, CITY, PLOT, HOUSING, ROOM, FARM"));
            return;
        }

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
                sender.getPlayerRef().sendMessage(Message.raw("Vous n'avez pas le rang requis pour creer un " + type.getLabel() + "."));
                return;
            }
        }

        String worldStr = sender.getWorld().getName();

        // KINGDOM : auto-generation complete
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
            sender.getPlayerRef().sendMessage(Message.raw("Aucune selection detectee !"));
            sender.getPlayerRef().sendMessage(Message.raw("Utilisez le Selection Tool dans le jeu, ou :"));
            sender.getPlayerRef().sendMessage(Message.raw("/es parcel pos1 puis /es parcel pos2"));
            return;
        }

        int[] sel = ParcelManager.getSelection(uuid);

        String parentId = null;
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
                sender.getPlayerRef().sendMessage(Message.raw("Un " + type.getLabel() + " ne peut pas etre cree dans un " + parentParcel.getType().getLabel() + "."));
                return;
            }
        } else if (type != ParcelType.TERRITORY && type != ParcelType.CITY) {
            sender.getPlayerRef().sendMessage(Message.raw("Un " + type.getLabel() + " doit etre cree dans une zone existante."));
            return;
        }

        String id = ParcelManager.createParcel(name, type, null, "", worldStr,
                sel[0], sel[1], sel[2], sel[3], sel[4], sel[5], parentId);

        ParcelData created = ParcelManager.get(id);
        if (created != null && (type == ParcelType.PLOT || type == ParcelType.HOUSING)) {
            ParcelManager.assignCityAsOwnerPublic(created);
            ParcelManager.save();
        }

        ParcelManager.clearSelection(uuid);

        int sizeX = Math.abs(sel[3] - sel[0]) + 1;
        int sizeY = Math.abs(sel[4] - sel[1]) + 1;
        int sizeZ = Math.abs(sel[5] - sel[2]) + 1;

        sender.getPlayerRef().sendMessage(Message.raw("" + type.getLabel() + " cree ! " + name));
        sender.getPlayerRef().sendMessage(Message.raw("ID: " + id + " | Taille: " + sizeX + "x" + sizeY + "x" + sizeZ));

        if (parentId != null) {
            ParcelData parent = ParcelManager.get(parentId);
            sender.getPlayerRef().sendMessage(Message.raw("Parent: " + (parent != null ? parent.getName() : parentId)));
        }
    }

    private void generateKingdom(Player sender, String kingdomName, String worldStr, int cx, int cz) {
        int H = 5000;
        int M = 4000;
        int N = 1000;
        int yMin = 0;
        int yMax = 319;

        int kx1 = cx - H, kz1 = cz - H;
        int kx2 = cx + H, kz2 = cz + H;

        String kingdomId = ParcelManager.createParcel(kingdomName, ParcelType.KINGDOM,
                null, "", worldStr, kx1, yMin, kz1, kx2, yMax, kz2, null);
        ParcelManager.assignToFamily(kingdomId, "eldanior");

        sender.getPlayerRef().sendMessage(Message.raw("=== CREATION DU ROYAUME ==="));
        sender.getPlayerRef().sendMessage(Message.raw("Royaume " + kingdomName + " cree ! (Famille Eldanior)"));
        sender.getPlayerRef().sendMessage(Message.raw("Centre: " + cx + ", " + cz + " | 10000x10000"));

        String royalId = ParcelManager.createParcel("Domaine_Royal", ParcelType.TERRITORY,
                null, "", worldStr, cx - N, yMin, cz - N, cx + N, yMax, cz + N, kingdomId);
        ParcelManager.assignToFamily(royalId, "eldanior");
        sender.getPlayerRef().sendMessage(Message.raw("  Domaine Royal (Famille Eldanior)"));

        String marqNO = ParcelManager.createParcel("Marquisat_Zippel", ParcelType.GRAND_TERRITORY,
                null, "", worldStr, kx1, yMin, kz1, kx1 + M, yMax, kz1 + M, kingdomId);
        ParcelManager.assignToFamily(marqNO, "zippel");

        String marqNE = ParcelManager.createParcel("Marquisat_Runkandel", ParcelType.GRAND_TERRITORY,
                null, "", worldStr, kx2 - M, yMin, kz1, kx2, yMax, kz1 + M, kingdomId);
        ParcelManager.assignToFamily(marqNE, "runkandel");

        String marqSO = ParcelManager.createParcel("Marquisat_Luminara", ParcelType.GRAND_TERRITORY,
                null, "", worldStr, kx1, yMin, kz2 - M, kx1 + M, yMax, kz2, kingdomId);
        ParcelManager.assignToFamily(marqSO, "luminara");

        String marqSE = ParcelManager.createParcel("Marquisat_Valmontis", ParcelType.GRAND_TERRITORY,
                null, "", worldStr, kx2 - M, yMin, kz2 - M, kx2, yMax, kz2, kingdomId);
        ParcelManager.assignToFamily(marqSE, "valmontis");

        sender.getPlayerRef().sendMessage(Message.raw("  4 Marquisats (Zippel, Runkandel, Luminara, Valmontis)"));

        int duchW = 1500;
        int gap = 1000;

        String dNO1 = ParcelManager.createParcel("Duche_Frostguard", ParcelType.TERRITORY, null, "", worldStr,
                kx1, yMin, kz1, kx1 + duchW, yMax, kz1 + M, marqNO);
        ParcelManager.assignToFamily(dNO1, "frostguard");

        String dNO2 = ParcelManager.createParcel("Duche_Spellweave", ParcelType.TERRITORY, null, "", worldStr,
                kx1 + duchW + gap, yMin, kz1, kx1 + M, yMax, kz1 + M, marqNO);
        ParcelManager.assignToFamily(dNO2, "spellweave");

        String dNE1 = ParcelManager.createParcel("Duche_Ironveil", ParcelType.TERRITORY, null, "", worldStr,
                kx2 - M, yMin, kz1, kx2 - M + duchW, yMax, kz1 + M, marqNE);
        ParcelManager.assignToFamily(dNE1, "ironveil");

        String dNE2 = ParcelManager.createParcel("Duche_Warbane", ParcelType.TERRITORY, null, "", worldStr,
                kx2 - duchW, yMin, kz1, kx2, yMax, kz1 + M, marqNE);
        ParcelManager.assignToFamily(dNE2, "warbane");

        String dSO1 = ParcelManager.createParcel("Duche_Nighthollow", ParcelType.TERRITORY, null, "", worldStr,
                kx1, yMin, kz2 - M, kx1 + duchW, yMax, kz2, marqSO);
        ParcelManager.assignToFamily(dSO1, "nighthollow");

        String dSO2 = ParcelManager.createParcel("Duche_Swiftquiver", ParcelType.TERRITORY, null, "", worldStr,
                kx1 + duchW + gap, yMin, kz2 - M, kx1 + M, yMax, kz2, marqSO);
        ParcelManager.assignToFamily(dSO2, "swiftquiver");

        String dSE1 = ParcelManager.createParcel("Duche_Goldcrest", ParcelType.TERRITORY, null, "", worldStr,
                kx2 - M, yMin, kz2 - M, kx2 - M + duchW, yMax, kz2, marqSE);
        ParcelManager.assignToFamily(dSE1, "goldcrest");

        String dSE2 = ParcelManager.createParcel("Duche_Silkroad", ParcelType.TERRITORY, null, "", worldStr,
                kx2 - duchW, yMin, kz2 - M, kx2, yMax, kz2, marqSE);
        ParcelManager.assignToFamily(dSE2, "silkroad");

        sender.getPlayerRef().sendMessage(Message.raw("  8 Duches (familles assignees automatiquement)"));
        sender.getPlayerRef().sendMessage(Message.raw("  4 Zones Neutres (bras de la croix, terres de la couronne)"));
        sender.getPlayerRef().sendMessage(Message.raw("  1 Domaine Royal (Famille Eldanior)"));
        sender.getPlayerRef().sendMessage(Message.raw("=== 14 parcelles creees ! ==="));

        ParcelManager.optimizeHierarchy();
    }
}
