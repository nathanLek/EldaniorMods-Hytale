package com.eldanior.system.classes.observateur;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * /es observateur <proposer|revoquer|renoncer|liste|nom> [--joueur nom] [--trone roi|empereur] [--nom nom d'emprunt]
 *  - proposer --joueur X : le Roi ou l'Empereur Luo propose a X de devenir son Observateur (fenetre chez X).
 *  - revoquer : le souverain retire le titre a son Observateur (classe d'avant rendue, tout de suite ou a la connexion).
 *  - renoncer : l'Observateur rend son titre lui-meme.
 *  - liste : qui sert quel trone.
 *  - executer --joueur <nom de l'Observateur> : le souverain le fait executer (personnage efface, niveau 1, point d'apparition).
 *  - nom --nom X : l'Observateur se fait appeler X (nameplate) ; sans --nom, il reprend son vrai nom.
 * Les admins choisissent le trone avec --trone (pour tester sans etre Roi ni Empereur).
 */
public class ObservateurCommand extends AbstractAsyncCommand {

    private final RequiredArg<String> actionArg;
    private final OptionalArg<String> playerArg;
    private final OptionalArg<String> throneArg;
    private final OptionalArg<String> nameArg;

    public ObservateurCommand() {
        super("observateur", "Observateur : proposer, revoquer, renoncer, liste");
        requireNoPermission();                                   // commande joueur : le Roi et l'Empereur Luo
        this.actionArg = this.withRequiredArg("action", "proposer | revoquer | renoncer | liste", ArgTypes.STRING);
        this.playerArg = this.withOptionalArg("joueur", "Joueur a qui proposer le titre", ArgTypes.STRING);
        this.throneArg = this.withOptionalArg("trone", "roi | empereur (admins)", ArgTypes.STRING);
        this.nameArg = this.withOptionalArg("nom", "Nom d'emprunt de l'Observateur (sans : retour a son vrai nom)", ArgTypes.STRING);
    }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        String action = actionArg.get(ctx).trim().toLowerCase();
        String targetName = playerArg.get(ctx);
        String throneName = throneArg.get(ctx);
        String alias = nameArg.get(ctx);
        world.execute(() -> {
            try {
                PlayerRef me = store.getComponent(ref, PlayerRef.getComponentType());
                PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
                if (me == null || data == null) return;
                switch (action) {
                    case "liste", "list" -> list(me);
                    case "menu", "cour" -> ObservateurCourTab.open(ref, store);   // menu Systeme ouvert sur la Cour (2026-11-19)
                    case "renoncer" -> renounce(ref, store, me);
                    case "nom", "nameplate", "deguisement" -> rename(me, data, alias);
                    case "proposer", "nommer" -> propose(me, data, targetName, throneName);
                    case "revoquer", "retirer" -> revoke(me, data, throneName);
                    case "executer", "tuer" -> execute(me, data, targetName, throneName);
                    default -> me.sendMessage(Message.raw("Usage : /es observateur proposer --joueur <nom> | revoquer | renoncer | liste | nom --nom <nom d'emprunt> | executer --joueur <nom de l'Observateur>"));
                }
            } catch (Exception e) {
                EldaniorLogger.error("[Observateur] /es observateur " + action, e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    /** Trone au nom duquel le joueur agit : le sien, ou celui choisi par un admin. */
    static ObservateurManager.Throne throne(PlayerRef me, PlayerLevelData data, String throneName) {
        ObservateurManager.Throne own = ObservateurManager.throneOf(data);
        if (own != null) return own;
        if (me.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
            ObservateurManager.Throne t = ObservateurManager.Throne.parse(throneName);
            if (t != null) return t;
            me.sendMessage(Message.raw("Admin : précisez le trône avec --trone roi ou --trone empereur."));
            return null;
        }
        me.sendMessage(Message.raw("Seuls le Roi d'Eldanior et l'Empereur Luo nomment un Observateur."));
        return null;
    }

    private static void list(PlayerRef me) {
        me.sendMessage(Message.raw("=== Observateur ==="));
        for (ObservateurManager.Throne t : ObservateurManager.Throne.values()) {
            ObservateurManager.Holder h = ObservateurManager.holder(t);
            me.sendMessage(Message.raw(t.label + " : " + (h != null ? h.name() : "place libre")));
        }
    }

    static void propose(PlayerRef me, PlayerLevelData data, String targetName, String throneName) {
        ObservateurManager.Throne t = throne(me, data, throneName);
        if (t == null) return;
        if (targetName == null || targetName.isBlank()) {
            me.sendMessage(Message.raw("Usage : /es observateur proposer --joueur <nom>"));
            return;
        }
        PlayerRef target = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
        if (target == null) {
            me.sendMessage(Message.raw("Joueur « " + targetName + " » introuvable ou hors ligne."));
            return;
        }
        UUID targetId = target.getUuid();
        if (targetId.equals(me.getUuid()) && !me.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
            me.sendMessage(Message.raw("Un souverain ne peut pas être son propre Observateur."));
            return;
        }
        ObservateurManager.Holder h = ObservateurManager.holder(t);
        if (h != null && !h.uuid().equals(targetId)) {
            me.sendMessage(Message.raw("Votre trône a déjà son Observateur : " + h.name() + ". Révoquez-le d'abord (/es observateur revoquer)."));
            return;
        }
        ObservateurManager.Throne other = ObservateurManager.throneHeldBy(targetId);
        if (other != null) {
            me.sendMessage(Message.raw(target.getUsername() + " sert déjà le " + other.label + "."));
            return;
        }
        Ref<EntityStore> tRef = target.getReference();
        if (tRef == null || !tRef.isValid()) return;
        Store<EntityStore> tStore = tRef.getStore();
        World tWorld = tStore.getExternalData().getWorld();
        if (tWorld == null) return;
        String sovereign = me.getUsername();
        ObservateurManager.offer(targetId, t, sovereign);
        tWorld.execute(() -> {
            try {
                if (!tRef.isValid()) return;
                PlayerLevelData td = tStore.getComponent(tRef, EldaniorSystem.get().getPlayerLevelDataType());
                if (ObservateurManager.isObservateur(td)) {
                    ObservateurManager.dropOffer(targetId);
                    tell(sovereign, target.getUsername() + " est déjà Observateur.");
                    return;
                }
                Player p = tStore.getComponent(tRef, Player.getComponentType());
                if (p != null) p.getPageManager().openCustomPage(tRef, tStore, new ObservateurOfferPage(target, t, sovereign));
            } catch (Exception e) {
                EldaniorLogger.error("[Observateur] ouverture de la proposition", e);
            }
        });
        me.sendMessage(Message.raw("Proposition envoyée à " + target.getUsername() + " (valable 2 minutes)."));
    }

    static void revoke(PlayerRef me, PlayerLevelData data, String throneName) {
        ObservateurManager.Throne t = throne(me, data, throneName);
        if (t == null) return;
        ObservateurManager.Holder h = ObservateurManager.holder(t);
        if (h == null) {
            me.sendMessage(Message.raw("Votre trône n'a pas d'Observateur."));
            return;
        }
        PlayerRef target = Universe.get().getPlayer(h.uuid());
        Ref<EntityStore> tRef = target != null ? target.getReference() : null;
        boolean online = tRef != null && tRef.isValid();
        ObservateurManager.release(t, !online);
        if (online) {
            Store<EntityStore> tStore = tRef.getStore();
            World tWorld = tStore.getExternalData().getWorld();
            if (tWorld != null) tWorld.execute(() -> {
                try {
                    if (!tRef.isValid()) return;
                    ObservateurManager.restore(tRef, tStore, h.previousClassId());
                    target.sendMessage(Message.raw("Le " + t.label + " vous a retiré le titre d'Observateur : vous retrouvez votre ancienne classe."));
                } catch (Exception e) {
                    EldaniorLogger.error("[Observateur] revocation", e);
                }
            });
        }
        me.sendMessage(Message.raw(h.name() + " n'est plus votre Observateur" + (online ? "." : " (appliqué à sa prochaine connexion).")));
    }

    /**
     * Le souverain fait executer son Observateur : personnage efface (niveau 1, tout perdu, point d'apparition). Irreversible :
     * il faut redonner le nom exact de l'Observateur en confirmation.
     */
    static void execute(PlayerRef me, PlayerLevelData data, String targetName, String throneName) {
        ObservateurManager.Throne t = throne(me, data, throneName);
        if (t == null) return;
        ObservateurManager.Holder h = ObservateurManager.holder(t);
        if (h == null) {
            me.sendMessage(Message.raw("Votre trône n'a pas d'Observateur à exécuter."));
            return;
        }
        if (targetName == null || !targetName.trim().equalsIgnoreCase(h.name())) {
            me.sendMessage(Message.raw("L'exécution efface tout le personnage de " + h.name() + " (niveau 1, tout perdu, retour au point "
                    + "d'apparition). Pour confirmer : /es observateur executer --joueur " + h.name()));
            return;
        }
        PlayerRef target = Universe.get().getPlayer(h.uuid());
        Ref<EntityStore> tRef = target != null ? target.getReference() : null;
        boolean online = tRef != null && tRef.isValid();
        ObservateurManager.sentence(t, !online);
        if (online) {
            Store<EntityStore> tStore = tRef.getStore();
            World tWorld = tStore.getExternalData().getWorld();
            if (tWorld != null) tWorld.execute(() -> {
                try {
                    if (tRef.isValid()) ObservateurManager.execute(target, tRef, tStore, t.label);
                } catch (Exception e) {
                    EldaniorLogger.error("[Observateur] execution", e);
                }
            });
        }
        me.sendMessage(Message.raw(h.name() + " est exécuté" + (online ? " : son personnage est effacé." : " : son personnage sera effacé à sa prochaine connexion.")
                + " Votre place d'Observateur est libre."));
    }

    static void renounce(Ref<EntityStore> ref, Store<EntityStore> store, PlayerRef me) {
        ObservateurManager.Throne t = ObservateurManager.throneHeldBy(me.getUuid());
        if (t == null) {
            me.sendMessage(Message.raw("Vous n'êtes l'Observateur d'aucun souverain."));
            return;
        }
        ObservateurManager.Holder h = ObservateurManager.release(t, false);
        if (h != null) ObservateurManager.restore(ref, store, h.previousClassId());
        me.sendMessage(Message.raw("Vous rendez votre titre d'Observateur au " + t.label + "."));
    }

    /** Nom d'emprunt de l'Observateur : affiche au-dessus de sa tete a la place du sien (sans --nom : retour au vrai nom). */
    static void rename(PlayerRef me, PlayerLevelData data, String raw) {
        if (!ObservateurManager.isObservateur(data)) {
            me.sendMessage(Message.raw("Seul un Observateur peut prendre un nom d'emprunt."));
            return;
        }
        if (raw == null || raw.isBlank()) {
            ObservateurManager.setAlias(me.getUuid(), null);
            me.sendMessage(Message.raw("Vous reprenez votre vrai nom."));
            return;
        }
        String alias = ObservateurManager.cleanAlias(raw);
        if (alias == null) {
            me.sendMessage(Message.raw("Nom refusé : " + ObservateurManager.ALIAS_MAX
                    + " caractères au plus (lettres, chiffres, espaces, ' - .). Utilisez des guillemets pour un nom en plusieurs mots."));
            return;
        }
        ObservateurManager.setAlias(me.getUuid(), alias);
        me.sendMessage(Message.raw("Vous vous faites désormais appeler « " + alias + " ». /es observateur nom (sans --nom) pour reprendre votre vrai nom."));
    }

    private static void tell(String name, String text) {
        if (name == null || text == null) return;
        for (PlayerRef pr : Universe.get().getPlayers())
            if (pr != null && pr.getUsername().equalsIgnoreCase(name)) pr.sendMessage(Message.raw(text));
    }
}
