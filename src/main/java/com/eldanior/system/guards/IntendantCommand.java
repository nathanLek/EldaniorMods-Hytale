package com.eldanior.system.guards;

import com.eldanior.system.classes.definitions.luo.VoieMartiale;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.titles.nobility.family.FamilyManager;
import com.eldanior.system.titles.nobility.family.FamilyRegistry;
import com.eldanior.system.titles.nobility.family.FamilyRole;
import com.eldanior.system.titles.nobility.family.KnightOrder;
import com.eldanior.system.titles.nobility.family.KnightOrderManager;
import com.eldanior.system.titles.nobility.family.KnightOrderRole;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /es intendant fictifs ajouter|retirer (admin, 2026-11-18) : cinq joueurs fictifs dans le Clan Luo pour essayer le registre de
 * l'intendant (rangs, niveaux, classes martiales et dernieres visites varies) et un ordre de demonstration a plusieurs rangs.
 * Ils ne se connectent jamais : un changement de rang reste « a sa prochaine visite ». Identifiants fixes : la commande peut
 * etre relancee sans doublon.
 */
public class IntendantCommand extends AbstractCommandCollection {

    static final String FAMILY = "luo";
    static final String ORDER = "Lames du Dragon d'Or";

    /** nom, rang de la maison, niveau, palier martial (1 Apprenti ... 7 Maitre Supreme), heures depuis la derniere visite. */
    private record Fake(String name, FamilyRole role, int level, int tier, double hoursAgo) {
        UUID uuid() { return UUID.nameUUIDFromBytes(("eldanior-fictif-" + FAMILY + "-" + name).getBytes(StandardCharsets.UTF_8)); }
    }

    private static final List<Fake> FAKES = List.of(
            new Fake("Luo Mingyu", FamilyRole.ELDER, 640, 5, 3),
            new Fake("Luo Shuang", FamilyRole.NOBLE, 430, 4, 26),
            new Fake("Tian Wei", FamilyRole.CHEVALIER_ROYAL, 355, 3, 0.7),
            new Fake("Jian Hao", FamilyRole.CHEVALIER, 220, 2, 75),
            new Fake("Yun Xiao", FamilyRole.MEMBER, 48, 1, 150));

    public IntendantCommand() {
        super("intendant", "Intendant (admin) : fictifs ajouter|retirer");
        this.addSubCommand(new Fakes());
    }

    private static final class Fakes extends AbstractAsyncCommand {
        private final RequiredArg<String> actionArg;

        Fakes() {
            super("fictifs", "Ajoute ou retire 5 joueurs fictifs du Clan Luo (essai de l'intendant)");
            this.actionArg = this.withRequiredArg("action", "ajouter ou retirer", ArgTypes.STRING);
        }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            var ref = ctx.senderAsPlayerRef();
            if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
            var store = ref.getStore();
            var world = store.getExternalData().getWorld();
            if (world == null) return CompletableFuture.completedFuture(null);
            String action = this.actionArg.get(ctx).toLowerCase();
            world.execute(() -> {
                try {
                    PlayerRef sender = store.getComponent(ref, PlayerRef.getComponentType());
                    if (sender == null) return;
                    if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) { sender.sendMessage(Message.raw("Permission refusée.")); return; }
                    switch (action) {
                        case "ajouter" -> sender.sendMessage(Message.raw(add()));
                        case "retirer" -> sender.sendMessage(Message.raw(remove()));
                        default -> sender.sendMessage(Message.raw("Usage : /es intendant fictifs ajouter|retirer"));
                    }
                } catch (Exception e) {
                    EldaniorLogger.error("IntendantCommand", e);
                }
            });
            return CompletableFuture.completedFuture(null);
        }
    }

    static String add() {
        long now = System.currentTimeMillis();
        String[] classes = VoieMartiale.classIds();
        for (Fake f : FAKES) {
            String classId = classes[Math.min(f.tier(), classes.length) - 1];
            FamilyRegistry.inscribe(FAMILY, f.uuid(), f.name(), f.role().code(), f.level(), classId, now - (long) (f.hoursAgo() * 3_600_000));
            if (f.role() == FamilyRole.ELDER) FamilyManager.addElder(FAMILY, f.uuid());
        }
        String orderNote;
        KnightOrder existing = KnightOrderManager.getPlayerOrder(FAKES.get(2).uuid());
        if (existing != null) orderNote = "l'Ordre " + existing.getName() + " existe déjà";
        else if (!KnightOrderManager.canCreateOrder(FAMILY)) orderNote = "pas d'ordre de démonstration (la maison a déjà 3 ordres)";
        else {
            Fake gm = FAKES.get(2);
            KnightOrder k = KnightOrderManager.createOrder(ORDER, "L'acier avant la parole.", FAMILY, gm.uuid(), gm.name());
            if (k == null) orderNote = "ordre de démonstration impossible";
            else {
                join(k, FAKES.get(1), KnightOrderRole.VICE_MAITRE);
                join(k, FAKES.get(3), KnightOrderRole.COMMANDANT);
                join(k, FAKES.get(4), KnightOrderRole.DISCIPLE);
                orderNote = "Ordre " + ORDER + " levé (Grand Maître " + gm.name() + ", 4 membres)";
            }
        }
        FamilyRegistry.log(FAMILY, "Cinq membres fictifs sont inscrits au registre (essai)");
        com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
        return "Cinq joueurs fictifs inscrits au Clan Luo (Doyen, Noble, Chevalier Royal, Chevalier, Recrue) ; " + orderNote
                + ". Ouvrez l'intendant Luo.";
    }

    private static void join(KnightOrder k, Fake f, KnightOrderRole r) {
        if (KnightOrderManager.getPlayerOrder(f.uuid()) != null) return;
        if (!KnightOrderManager.joinOrder(f.uuid(), k)) return;
        k.rememberName(f.uuid(), f.name());
        k.setRank(f.uuid(), r);
    }

    static String remove() {
        int n = 0;
        for (Fake f : FAKES) {
            UUID u = f.uuid();
            KnightOrder k = KnightOrderManager.getPlayerOrder(u);
            if (k != null) {
                if (k.isCapitaine(u)) KnightOrderManager.disbandOrder(k.getId());
                else KnightOrderManager.leaveOrder(u);
            }
            FamilyRegistry.consumeRole(FAMILY, u);
            FamilyManager.removeElder(FAMILY, u);
            if (FamilyRegistry.members(FAMILY).stream().anyMatch(e -> e.uuid().equals(u))) n++;
            FamilyRegistry.remove(FAMILY, u);
        }
        FamilyRegistry.log(FAMILY, "Les membres fictifs sont rayés du registre");
        com.eldanior.system.persistence.PersistenceManager.saveOrdersAndTitles();
        return n + " joueur(s) fictif(s) retiré(s) du Clan Luo (et leur ordre de démonstration).";
    }
}
