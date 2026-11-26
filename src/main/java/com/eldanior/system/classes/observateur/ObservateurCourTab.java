package com.eldanior.system.classes.observateur;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Cour de l'Observateur, panneau #TabObservateur du menu Systeme (System/SystemPage.ui, bloc genere par
 * scripts/observateur/gen_ui.py). 2026-11-19 (demande de l'utilisateur) : l'onglet Observateur ouvre ce panneau a droite des
 * onglets, comme les autres, et plus une fenetre a part (ancienne ObservateurCourPage).
 *  - Souverain (Roi d'Eldanior, Empereur Luo ; un admin choisit le trone) : son Observateur (en ligne, nom d'emprunt,
 *    deguisement), Revoquer, Executer (deux clics) ; sans Observateur : joueurs en ligne a qui Proposer le titre.
 *  - Observateur : qui il sert, nom d'emprunt, Rendre son titre (deux clics). Il n'a pas le bouton (la barre des onglets
 *    deborderait avec son Marche noir) : « Ma charge » de Sans Visage et /es observateur menu ouvrent le menu sur ce panneau.
 * Les actions reprennent celles de ObservateurCommand (memes regles, memes messages dans le chat). Un objet par menu ouvert.
 */
public final class ObservateurCourTab {

    static final int ROWS = 6;
    /** Prefixe des actions de ce panneau dans les evenements du menu Systeme. */
    public static final String PREFIX = "oc_";
    /** Cle du champ du nom d'emprunt dans les evenements du menu Systeme. */
    public static final String ALIAS_KEY = "@OcAlias";

    private final PlayerRef playerRef;
    private ObservateurManager.Throne adminThrone = ObservateurManager.Throne.ROI;
    private int page;
    private boolean confirmExecute, confirmRenounce;
    private String status = "";
    /** Joueurs proposes sur la page affichee (uuid), dans l'ordre des lignes. */
    private final List<UUID> shown = new ArrayList<>();

    public ObservateurCourTab(PlayerRef playerRef) {
        this.playerRef = playerRef;
    }

    /**
     * Le joueur voit-il le bouton du menu Systeme ? Souverain ou admin. L'Observateur ouvre sa partie depuis la fenetre Sans
     * Visage (bouton « Ma charge ») : la barre des onglets (800 px) deborderait avec son bouton du Marche noir en plus.
     */
    public static boolean canOpen(PlayerRef pr, PlayerLevelData data) {
        return ObservateurManager.throneOf(data) != null || (pr != null && pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION));
    }

    /** Le panneau a-t-il quelque chose a montrer a ce joueur (bouton, ou Observateur lui-meme) ? */
    public static boolean allowed(PlayerRef pr, PlayerLevelData data) {
        return canOpen(pr, data) || ObservateurManager.isObservateur(data);
    }

    /** Ouvre le menu Systeme directement sur la Cour (Sans Visage « Ma charge », /es observateur menu). */
    public static void open(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player p = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (p != null && pr != null)
            p.getPageManager().openCustomPage(ref, store, new com.eldanior.system.gui.SystemScreen(pr).openOnObservateur());
    }

    private boolean admin() { return playerRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION); }

    /** Trone au nom duquel le joueur agit (le sien, sinon celui choisi par l'admin), ou null. */
    private ObservateurManager.Throne throne(PlayerLevelData data) {
        ObservateurManager.Throne own = ObservateurManager.throneOf(data);
        return own != null ? own : admin() ? adminThrone : null;
    }

    /** Liaisons des boutons du panneau (une fois, a la construction du menu). */
    public static void bind(UIEventBuilder events) {
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcThroneRoi", EventData.of("Action", PREFIX + "throne").append("Param", "roi"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcThroneEmp", EventData.of("Action", PREFIX + "throne").append("Param", "empereur"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcRevoke", EventData.of("Action", PREFIX + "revoke"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcExecute", EventData.of("Action", PREFIX + "execute"), false);
        for (int i = 0; i < ROWS; i++)
            events.addEventBinding(CustomUIEventBindingType.Activating, "#OcPB" + i, EventData.of("Action", PREFIX + "propose").append("Param", String.valueOf(i)), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcPrev", EventData.of("Action", PREFIX + "prev"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcNext", EventData.of("Action", PREFIX + "next"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcAliasOk", EventData.of("Action", PREFIX + "alias").append(ALIAS_KEY, "#OcAlias.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcAliasOff", EventData.of("Action", PREFIX + "alias_off"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#OcRenounce", EventData.of("Action", PREFIX + "renounce"), false);
    }

    /** Remplit le panneau (construction du menu ou mise a jour). */
    public void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        ui.set("#OcIcon.ItemId", "Rune_ArkiesDomini");
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        ObservateurManager.Throne own = ObservateurManager.throneOf(data);
        ObservateurManager.Throne t = throne(data);
        boolean self = ObservateurManager.isObservateur(data);

        ui.set("#OcSub.Text", t != null ? "Cour du " + t.label + (own == null ? " (admin)" : "")
                : self ? "Au service de la couronne" : "Réservé au Roi, à l'Empereur Luo et à leur Observateur");
        ui.set("#OcLead.Text", t != null ? "Votre Observateur est " + t.role + " : il ne répond qu'à vous."
                : self ? "L'Œil et les Oreilles de l'Empereur, l'assassin privé du Roi." : "");
        ui.set("#OcAdminRow.Visible", own == null && admin());
        if (own == null && admin()) {
            ui.set("#OcThroneRoi.Text", (adminThrone == ObservateurManager.Throne.ROI ? "▶ " : "") + "Roi");
            ui.set("#OcThroneEmp.Text", (adminThrone == ObservateurManager.Throne.EMPEREUR ? "▶ " : "") + "Empereur");
        }

        // ---- souverain
        ObservateurManager.Holder h = t != null ? ObservateurManager.holder(t) : null;
        ui.set("#OcHolderCard.Visible", t != null && h != null);
        ui.set("#OcPickCard.Visible", t != null && h == null);
        if (t != null && h != null) {
            PlayerRef online = Universe.get().getPlayer(h.uuid());
            String alias = ObservateurManager.rawAlias(h.uuid());
            var mask = com.eldanior.system.skills.runes.spells.SansVisage.mask(h.uuid());
            ClassModel before = ClassManager.get(h.previousClassId());
            ui.set("#OcHolderInitial.Text", h.name().isEmpty() ? "?" : h.name().substring(0, 1).toUpperCase());
            ui.set("#OcHolderName.Text", h.name());
            ui.set("#OcHolderState.Text", online != null ? "En ligne" : "Hors ligne");
            ui.set("#OcHolderState.Style.TextColor", online != null ? "#7ed47e" : "#878e9c");
            ui.set("#OcHolderBar.Background", online != null ? "#7ed47e" : "#c99436");
            ui.set("#OcHolderInfo0.Text", "Nom d'emprunt : " + (alias != null ? "« " + alias + " »" : "aucun"));
            ui.set("#OcHolderInfo1.Text", "Déguisement : " + (mask != null ? mask.label() : "aucun"));
            ui.set("#OcHolderInfo2.Text", "Ancienne classe (rendue s'il est révoqué) : " + (before != null ? before.getDisplayName() : "Novice"));
            ui.set("#OcExecute.Text", confirmExecute ? "Confirmer l'exécution" : "Exécuter");
            ui.set("#OcHolderWarn.Text", confirmExecute
                    ? "Irréversible : le personnage de " + h.name() + " sera effacé (niveau 1, tout perdu, point d'apparition)."
                    : "Révoquer lui rend son ancienne classe. Exécuter efface son personnage.");
            ui.set("#OcHolderWarn.Style.TextColor", confirmExecute ? "#e07070" : "#878e9c");
        }
        shown.clear();
        if (t != null && h == null) {
            List<PlayerRef> candidates = new ArrayList<>();
            for (PlayerRef pr : Universe.get().getPlayers()) {
                if (pr == null) continue;
                if (pr.getUuid().equals(playerRef.getUuid()) && !admin()) continue;
                if (ObservateurManager.throneHeldBy(pr.getUuid()) != null) continue;
                candidates.add(pr);
            }
            candidates.sort(Comparator.comparing(PlayerRef::getUsername, String.CASE_INSENSITIVE_ORDER));
            int pages = Math.max(1, (candidates.size() + ROWS - 1) / ROWS);
            page = Math.max(0, Math.min(page, pages - 1));
            ui.set("#OcPickInfo.Text", candidates.isEmpty() ? "Aucun joueur en ligne à nommer pour le moment."
                    : "Le joueur choisi reçoit une proposition (2 minutes pour accepter). Il garde niveau, caractéristiques, runes et objets.");
            for (int i = 0; i < ROWS; i++) {
                int k = page * ROWS + i;
                PlayerRef pr = k < candidates.size() ? candidates.get(k) : null;
                ui.set("#OcP" + i + ".Visible", pr != null);
                if (pr == null) continue;
                shown.add(pr.getUuid());
                ui.set("#OcPL" + i + ".Text", pr.getUsername());
                ui.set("#OcPS" + i + ".Text", describe(pr));
            }
            ui.set("#OcPage.Text", "Page " + (page + 1) + " / " + pages);
        }

        // ---- l'Observateur lui-meme
        ui.set("#OcSelfCard.Visible", self);
        if (self) {
            ObservateurManager.Throne served = ObservateurManager.throneHeldBy(playerRef.getUuid());
            ui.set("#OcSelfTitle.Text", served != null ? "Au service du " + served.label : "Observateur sans trône");
            ui.set("#OcSelfInfo.Text", served != null ? "Vous êtes " + served.role + ". Toutes les armes et tous les savoirs du royaume vous sont ouverts."
                    : "Vous portez la classe d'Observateur sans être inscrit à la Cour (classe donnée par un admin).");
            String alias = ObservateurManager.alias(playerRef.getUuid(), data);
            ui.set("#OcAlias.Value", alias != null ? alias : "");
            ui.set("#OcRenounce.Text", confirmRenounce ? "Confirmer" : "Rendre mon titre");
            ui.set("#OcRenounceWarn.Text", confirmRenounce ? "Vous retrouverez votre ancienne classe et quitterez la Cour."
                    : "Rendre votre titre vous rend votre ancienne classe.");
            ui.set("#OcRenounceWarn.Style.TextColor", confirmRenounce ? "#e07070" : "#878e9c");
        }

        // ---- l'autre trone, statut
        StringBuilder other = new StringBuilder();
        for (ObservateurManager.Throne x : ObservateurManager.Throne.values()) {
            if (x == t) continue;
            ObservateurManager.Holder oh = ObservateurManager.holder(x);
            if (other.length() > 0) other.append("   ·   ");
            other.append(x.label).append(" : ").append(oh != null ? oh.name() : "place libre");
        }
        ui.set("#OcOther.Text", other.toString());
        ui.set("#OcStatus.Text", status);
    }

    /** « Niveau 120 · Mage » si lisible depuis ce thread (meme monde), sinon vide. */
    private String describe(PlayerRef pr) {
        try {
            Ref<EntityStore> r = pr.getReference();
            Ref<EntityStore> mine = playerRef.getReference();
            if (r == null || mine == null || r.getStore() != mine.getStore()) return "autre monde";
            PlayerLevelData d = r.getStore().getComponent(r, EldaniorSystem.get().getPlayerLevelDataType());
            if (d == null) return "";
            ClassModel c = ClassManager.get(d.getPlayerClassId());
            return "Niveau " + d.getLevel() + "  ·  " + (c != null ? c.getDisplayName() : "sans classe");
        } catch (Exception e) {
            return "";
        }
    }

    /** Action du panneau (sans le prefixe oc_). Le menu repeuple ensuite le panneau. */
    public void handle(String a, String param, String aliasValue, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (data == null || !allowed(playerRef, data)) return;
        ObservateurManager.Throne t = throne(data);
        String tn = t == null ? null : t == ObservateurManager.Throne.ROI ? "roi" : "empereur";
        if (!"execute".equals(a)) confirmExecute = false;
        if (!"renounce".equals(a)) confirmRenounce = false;
        status = "";
        switch (a) {
            case "throne" -> {
                ObservateurManager.Throne x = ObservateurManager.Throne.parse(param);
                if (x != null && admin()) { adminThrone = x; page = 0; }
            }
            case "prev" -> page = Math.max(0, page - 1);
            case "next" -> page++;
            case "propose" -> {
                int i = parse(param);
                if (t == null || i < 0 || i >= shown.size()) break;
                PlayerRef target = Universe.get().getPlayer(shown.get(i));
                if (target == null) { status = "Ce joueur s'est déconnecté."; break; }
                ObservateurCommand.propose(playerRef, data, target.getUsername(), tn);
                status = "Proposition envoyée à " + target.getUsername() + " (voir le chat).";
            }
            case "revoke" -> {
                if (t == null) break;
                ObservateurCommand.revoke(playerRef, data, tn);
                status = "Titre retiré.";
            }
            case "execute" -> {
                ObservateurManager.Holder h = t != null ? ObservateurManager.holder(t) : null;
                if (h == null) break;
                if (!confirmExecute) { confirmExecute = true; status = "Cliquez de nouveau pour confirmer l'exécution de " + h.name() + "."; break; }
                confirmExecute = false;
                ObservateurCommand.execute(playerRef, data, h.name(), tn);
                status = h.name() + " est exécuté.";
            }
            case "alias" -> {
                ObservateurCommand.rename(playerRef, data, aliasValue);
                status = ObservateurManager.hasAlias(playerRef.getUuid()) ? "Nom d'emprunt pris." : "Nom refusé (voir le chat).";
            }
            case "alias_off" -> { ObservateurCommand.rename(playerRef, data, null); status = "Vous reprenez votre vrai nom."; }
            case "renounce" -> {
                if (!ObservateurManager.isObservateur(data)) break;
                if (!confirmRenounce) { confirmRenounce = true; status = "Cliquez de nouveau pour rendre votre titre."; break; }
                confirmRenounce = false;
                ObservateurCommand.renounce(ref, store, playerRef);
                status = "Titre rendu.";
            }
            default -> { }
        }
    }

    private static int parse(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
