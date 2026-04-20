package com.eldanior.system.titles.commands;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.titles.TitleManager;
import com.eldanior.system.titles.models.TitleEffect;
import com.eldanior.system.titles.models.TitleModel;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
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
        if (!(ctx.sender() instanceof Player sender)) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);

        switch (action.toLowerCase()) {
            case "equip" -> handleEquip(sender, ctx);
            case "info" -> handleInfo(sender, ctx);
            default -> sender.sendMessage(Message.raw("§cUsage : /es title <equip|info> <titleId>"));
        }

        return CompletableFuture.completedFuture(null);
    }

    private void handleEquip(Player sender, CommandContext ctx) {
        String titleId = this.titleIdArg.get(ctx);

        assert sender.getWorld() != null;
        CompletableFuture.runAsync(() -> {
            try {
                var ref = sender.getReference();
                if (ref == null) return;

                Store<EntityStore> store = ref.getStore();
                ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
                PlayerLevelData data = store.getComponent(ref, type);

                if (data == null) {
                    sender.sendMessage(Message.raw("§cAucune donnee trouvee."));
                    return;
                }

                if (!data.getUnlockedTitles().contains(titleId.toLowerCase())) {
                    sender.sendMessage(Message.raw("§cVous ne possedez pas ce titre."));
                    return;
                }

                TitleModel title = TitleManager.get(titleId.toLowerCase());
                if (title == null) {
                    sender.sendMessage(Message.raw("§cTitre '" + titleId + "' inconnu dans le registre."));
                    return;
                }

                data.setCurrentTitle(title.getId());
                store.putComponent(ref, type, data);

                sender.sendMessage(Message.raw("§aTitre equipe : " + title.getFormattedName()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, sender.getWorld());
    }

    private void handleInfo(Player sender, CommandContext ctx) {
        String titleId = this.titleIdArg.get(ctx);

        TitleModel title = TitleManager.get(titleId.toLowerCase());
        if (title == null) {
            sender.sendMessage(Message.raw("§cTitre '" + titleId + "' inconnu."));
            return;
        }

        sender.sendMessage(Message.raw("§6=== " + title.getFormattedName() + " §6==="));
        sender.sendMessage(Message.raw("§7" + title.getDescription()));
        sender.sendMessage(Message.raw("§7Rarete : " + title.getRarity().getDisplayName()));
        sender.sendMessage(Message.raw("§7Categorie : " + title.getCategory().getDisplayName()));

        var bonus = title.getBonus();
        if (bonus.strength() != 0 || bonus.vitality() != 0 || bonus.intelligence() != 0
                || bonus.endurance() != 0 || bonus.agility() != 0 || bonus.luck() != 0) {
            sender.sendMessage(Message.raw("§eBonus de stats :"));
            if (bonus.strength() != 0) sender.sendMessage(Message.raw("§7  STR +" + bonus.strength()));
            if (bonus.vitality() != 0) sender.sendMessage(Message.raw("§7  VIT +" + bonus.vitality()));
            if (bonus.intelligence() != 0) sender.sendMessage(Message.raw("§7  INT +" + bonus.intelligence()));
            if (bonus.endurance() != 0) sender.sendMessage(Message.raw("§7  END +" + bonus.endurance()));
            if (bonus.agility() != 0) sender.sendMessage(Message.raw("§7  AGL +" + bonus.agility()));
            if (bonus.luck() != 0) sender.sendMessage(Message.raw("§7  LCK +" + bonus.luck()));
        }

        if (!title.getEffects().isEmpty()) {
            sender.sendMessage(Message.raw("§eEffets speciaux :"));
            for (TitleEffect effect : title.getEffects()) {
                String desc = switch (effect.type()) {
                    case DAMAGE_BONUS_VS_MOB -> "+" + (int)(effect.value() * 100) + "% degats vs " + effect.target();
                    case DAMAGE_REDUCTION_FROM_MOB -> "-" + (int)(effect.value() * 100) + "% degats recus de " + effect.target();
                    case XP_BONUS_PERCENT -> "+" + (int)(effect.value() * 100) + "% XP";
                    case MONEY_BONUS_PERCENT -> "+" + (int)(effect.value() * 100) + "% argent";
                    case HEALTH_BONUS_FLAT -> "+" + (int) effect.value() + " PV";
                    case MANA_BONUS_FLAT -> "+" + (int) effect.value() + " Mana";
                };
                sender.sendMessage(Message.raw("§7  " + desc));
            }
        }
    }
}