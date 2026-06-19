package com.eldanior.system.territory.commands;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.TaxConfig;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.eldanior.system.territory.ParcelType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * Commande admin : /es taxaudit
 * Affiche un audit des taxes collectees par parcelle (dernier montant, date, tresorerie).
 */
public class TaxAuditCommand extends AbstractAsyncCommand {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public TaxAuditCommand() {
        super("taxaudit", "Audit des taxes collectees par parcelle (admin)");
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef == null) return;

                if (!playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    playerRef.sendMessage(Message.raw("§cVous n'avez pas la permission."));
                    return;
                }

                playerRef.sendMessage(Message.raw("§6=== Audit des taxes ==="));

                int count = 0;
                for (ParcelData parcel : ParcelManager.getAll()) {
                    // N'afficher que les parcelles qui collectent des taxes
                    double rate = TaxConfig.getCollectionRate(parcel.getType());
                    if (rate <= 0.0 && parcel.getLastTaxAmount() == 0) continue;

                    long lastAmount = parcel.getLastTaxAmount();
                    long lastTime = parcel.getLastTaxCollection();
                    long treasury = parcel.getTreasury();
                    String typeLabel = parcel.getType().getLabel();

                    String dateStr = lastTime > 0 ? DATE_FMT.format(new Date(lastTime)) : "jamais";

                    playerRef.sendMessage(Message.raw(
                            "§e" + parcel.getName() + " §7(" + typeLabel + ")"
                            + " §fTresor: §a" + treasury + " Or"
                            + " §fDerniere collecte: §b" + lastAmount + " Or"
                            + " §7(" + dateStr + ")"
                            + (rate > 0 ? " §7[taux: " + (int)(rate * 100) + "%]" : "")
                    ));
                    count++;
                }

                if (count == 0) {
                    playerRef.sendMessage(Message.raw("§7Aucune parcelle avec historique de taxe."));
                }
                playerRef.sendMessage(Message.raw("§6=== " + count + " parcelle(s) ==="));
            } catch (Exception e) {
                EldaniorLogger.error("TaxAuditCommand", e);
            }
        }, world);
    }
}
