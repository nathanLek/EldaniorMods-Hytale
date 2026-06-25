package com.eldanior.system.titles.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleEffect;
import com.eldanior.system.titles.models.TitleModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class TitleOneArgCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> titleIdArg;

    public TitleOneArgCommand() {
        super("title", "Titres (equip/info)");
        this.actionArg = this.withRequiredArg("action", "equip | info", ArgTypes.STRING);
        this.titleIdArg = this.withRequiredArg("titleId", "ID du titre", ArgTypes.STRING);
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

        String action = this.actionArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = senderEntityStore.getComponent(senderEntityRef, PlayerRef.getComponentType());
                Player sender = senderEntityStore.getComponent(senderEntityRef, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                switch (action.toLowerCase()) {
                    case "equip" -> handleEquip(sender, ctx);
                    case "info" -> handleInfo(sender, ctx);
                    default -> senderRef.sendMessage(Message.raw("Usage : /es title <equip|info> <titleId>"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    private void handleEquip(Player sender, CommandContext ctx) {
        String titleId = this.titleIdArg.get(ctx);

        try {
            var ref = sender.getReference();
            if (ref == null) return;

            Store<EntityStore> store = ref.getStore();
            ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
            PlayerLevelData data = store.getComponent(ref, type);

            if (data == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Aucune donnee trouvee."));
                return;
            }

            if (!data.getUnlockedTitles().contains(titleId.toLowerCase())) {
                sender.getPlayerRef().sendMessage(Message.raw("Vous ne possedez pas ce titre."));
                return;
            }

            TitleModel title = TitleManager.get(titleId.toLowerCase());
            if (title == null) {
                sender.getPlayerRef().sendMessage(Message.raw("Titre '" + titleId + "' inconnu dans le registre."));
                return;
            }

            data.setCurrentTitle(title.getId());
            store.putComponent(ref, type, data);

            sender.getPlayerRef().sendMessage(Message.raw("Titre affiche : " + title.getFormattedName()));
            sender.getPlayerRef().sendMessage(Message.raw("Les bonus de tous vos titres sont actifs automatiquement."));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleInfo(Player sender, CommandContext ctx) {
        String titleId = this.titleIdArg.get(ctx);

        TitleModel title = TitleManager.get(titleId.toLowerCase());
        if (title == null) {
            sender.getPlayerRef().sendMessage(Message.raw("Titre '" + titleId + "' inconnu."));
            return;
        }

        sender.getPlayerRef().sendMessage(Message.raw("=== " + title.getFormattedName() + " ==="));
        sender.getPlayerRef().sendMessage(Message.raw("" + title.getDescription()));
        sender.getPlayerRef().sendMessage(Message.raw("Rarete : " + title.getRarity().getDisplayName()));
        sender.getPlayerRef().sendMessage(Message.raw("Categorie : " + title.getCategory().getDisplayName()));

        var bonus = title.getBonus();
        if (bonus.strength() != 0 || bonus.vitality() != 0 || bonus.intelligence() != 0
                || bonus.endurance() != 0 || bonus.agility() != 0 || bonus.luck() != 0) {
            sender.getPlayerRef().sendMessage(Message.raw("Bonus de stats :"));
            if (bonus.strength() != 0) sender.getPlayerRef().sendMessage(Message.raw("  STR +" + bonus.strength()));
            if (bonus.vitality() != 0) sender.getPlayerRef().sendMessage(Message.raw("  VIT +" + bonus.vitality()));
            if (bonus.intelligence() != 0) sender.getPlayerRef().sendMessage(Message.raw("  INT +" + bonus.intelligence()));
            if (bonus.endurance() != 0) sender.getPlayerRef().sendMessage(Message.raw("  END +" + bonus.endurance()));
            if (bonus.agility() != 0) sender.getPlayerRef().sendMessage(Message.raw("  AGL +" + bonus.agility()));
            if (bonus.luck() != 0) sender.getPlayerRef().sendMessage(Message.raw("  LCK +" + bonus.luck()));
        }

        if (!title.getEffects().isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("Effets speciaux :"));
            for (TitleEffect effect : title.getEffects()) {
                String desc = switch (effect.type()) {
                    case DAMAGE_BONUS_VS_MOB -> "+" + (int)(effect.value() * 100) + "% degats vs " + effect.target();
                    case DAMAGE_REDUCTION_FROM_MOB -> "-" + (int)(effect.value() * 100) + "% degats recus de " + effect.target();
                    case XP_BONUS_PERCENT -> "+" + (int)(effect.value() * 100) + "% XP";
                    case MONEY_BONUS_PERCENT -> "+" + (int)(effect.value() * 100) + "% argent";
                    case HEALTH_BONUS_FLAT -> "+" + (int) effect.value() + " PV";
                    case MANA_BONUS_FLAT -> "+" + (int) effect.value() + " Mana";
                };
                sender.getPlayerRef().sendMessage(Message.raw("  " + desc));
            }
        }
    }
}
