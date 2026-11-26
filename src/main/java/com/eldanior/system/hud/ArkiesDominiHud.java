package com.eldanior.system.hud;

import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.quest.QuestDifficulty;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.Objects;

/**
 * Carte de l'entite visee pendant la rune Arkies Domini de l'Observateur (Hud/ArkiesDomini.ui, refaite le 2026-11-17 au
 * style des cartes de quete du HUD) : contour et type a la couleur du genre (joueur, PNJ, creature), blason du rang de
 * menace (F a EX, textures Quest/QHudRank*), nom, sous-titre, modele 3D (ItemPreviewComponent : portrait
 * Eldanior_Visage_<modele>, voir classes/observateur/Visages), vie, mana, classe, rang, 6 caracteristiques a icones,
 * ROWS lignes de details (masquees si vides), distance. Placee a droite SOUS le panneau de groupe.
 * HUD a part (cle KEY), pose a l'activation de la rune, retire a son arret. Ne renvoie que ce qui a change.
 */
public class ArkiesDominiHud extends CustomUIHud {

    public static final String KEY = "arkies_domini";
    public static final int ROWS = 6, STATS = 6;

    /** Genre de la presence : couleur du contour et du type (memes teintes que les types des cartes de quete). */
    public enum Kind {
        PLAYER("#f2c45e"), NPC("#6fb3ff"), BEAST("#d9573f");
        final String color;
        Kind(String color) { this.color = color; }
    }

    /**
     * Profil montre. kindLine : « Joueur · Niveau 120 ». rank : blason (null = « ? »). portrait : id d'objet (null = « ? »).
     * manaMax <= 0 : pas de barre de mana. stats : null ou STATS valeurs (Force, Vitalite, Intelligence, Endurance, Agilite,
     * Chance). rows : au plus ROWS lignes.
     */
    public record Profile(Kind kind, String kindLine, String title, String sub, QuestDifficulty rank, String portrait,
                          float hp, float hpMax, float mana, float manaMax, List<String> stats,
                          String classLine, String rankLine, List<String> rows, String distance, boolean behindWall) {}

    private Profile current;

    public ArkiesDominiHud(PlayerRef playerRef) {
        super(playerRef, KEY);
    }

    @Override
    protected void build(UICommandBuilder ui) {
        ui.append("Hud/ArkiesDomini.ui");
        if (current != null) render(ui, null, current);
    }

    /** Ecrit ce qui differe entre old (null = tout) et p (null = carte cachee). */
    private static void render(UICommandBuilder ui, Profile old, Profile p) {
        if (p == null) { ui.set("#AdPanel.Visible", false); return; }
        if (old == null) ui.set("#AdPanel.Visible", true);
        if (old == null || old.kind() != p.kind()) {
            ui.set("#AdPanel.Background", p.kind().color);
            ui.set("#AdRankLine.Background", p.kind().color);
            ui.set("#AdKind.Style.TextColor", p.kind().color);
            ui.set("#AdTitle.Style.TextColor", p.kind().color);
        }
        if (old == null || old.rank() != p.rank()) {
            for (QuestDifficulty r : QuestDifficulty.values()) ui.set("#AdBadge" + r.name() + ".Visible", r == p.rank());
            ui.set("#AdRankLetter.Text", p.rank() != null ? p.rank().getDisplayName() : "?");
            ui.set("#AdRankLetter.Style.TextColor", p.rank() != null ? light(p.rank().getColor()) : "#8a93a6");
        }
        text(ui, "#AdKind", old == null ? null : old.kindLine(), p.kindLine());
        text(ui, "#AdTitle", old == null ? null : old.title(), p.title());
        text(ui, "#AdSub", old == null ? null : old.sub(), p.sub());
        if (old == null || !Objects.equals(old.portrait(), p.portrait())) {
            boolean has = p.portrait() != null;
            ui.set("#AdModel.Visible", has);
            ui.set("#AdNoModel.Visible", !has);
            if (has) {
                ui.set("#AdModel.ItemId", p.portrait());
                ui.set("#AdModel.ItemScale", com.eldanior.system.classes.observateur.Visages.scaleOfPortrait(p.portrait()));   // gros modeles reduits
            }
        }
        if (old == null || old.hp() != p.hp() || old.hpMax() != p.hpMax()) {
            ui.set("#AdHp.Value", p.hpMax() > 0 ? Math.max(0f, Math.min(1f, p.hp() / p.hpMax())) : 0f);
            ui.set("#AdHpText.Text", p.hpMax() > 0 ? (int) p.hp() + " / " + (int) p.hpMax() : "?");
        }
        boolean mana = p.manaMax() > 0;
        if (old == null || (old.manaMax() > 0) != mana) ui.set("#AdManaRow.Visible", mana);
        if (mana && (old == null || old.mana() != p.mana() || old.manaMax() != p.manaMax())) {
            ui.set("#AdMana.Value", Math.max(0f, Math.min(1f, p.mana() / p.manaMax())));
            ui.set("#AdManaText.Text", (int) p.mana() + " / " + (int) p.manaMax());
        }
        boolean stats = p.stats() != null;
        if (old == null || (old.stats() != null) != stats) ui.set("#AdStats.Visible", stats);
        if (stats) for (int i = 0; i < STATS; i++)
            text(ui, "#AdS" + i, old == null || old.stats() == null ? null : at(old.stats(), i), at(p.stats(), i));
        text(ui, "#AdClass", old == null ? null : old.classLine(), p.classLine());
        text(ui, "#AdRank", old == null ? null : old.rankLine(), p.rankLine());
        for (int i = 0; i < ROWS; i++) {
            String before = old == null ? null : at(old.rows(), i), now = at(p.rows(), i);
            if (old == null || before.isEmpty() != now.isEmpty()) ui.set("#AdR" + i + ".Visible", !now.isEmpty());
            text(ui, "#AdL" + i, before, now);
        }
        text(ui, "#AdDist", old == null ? null : old.distance(), p.distance());
        if (old == null || old.behindWall() != p.behindWall()) ui.set("#AdWall.Text", p.behindWall() ? "Derrière un mur" : "");
    }

    private static void text(UICommandBuilder ui, String sel, String before, String now) {
        String v = now == null ? "" : now;
        if (before == null || !before.equals(v)) ui.set(sel + ".Text", v);
    }

    private static String at(List<String> l, int i) { return l != null && i < l.size() && l.get(i) != null ? l.get(i) : ""; }

    /** Couleur du rang eclaircie (60 % vers le blanc) pour la lettre du blason, comme le HUD des quetes. */
    private static String light(String hex) {
        int c = Integer.parseInt(hex.substring(1), 16);
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        return String.format("#%02x%02x%02x", r + (255 - r) * 6 / 10, g + (255 - g) * 6 / 10, b + (255 - b) * 6 / 10);
    }

    /** Montre le profil (null = cache la carte). N'envoie que ce qui a change. */
    public void show(Profile p) {
        if (p == null ? current == null : p.equals(current)) return;
        UICommandBuilder ui = new UICommandBuilder();
        render(ui, current, p);
        current = p;
        update(false, ui);
    }

    /** Pose le HUD sur le joueur (ou reprend celui deja pose). */
    public static ArkiesDominiHud attach(Ref<EntityStore> ref, Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || pr == null) return null;
        CustomUIHud current = player.getHudManager().getCustomHud(KEY);
        if (current instanceof ArkiesDominiHud hud) return hud;
        ArkiesDominiHud hud = new ArkiesDominiHud(pr);
        player.getHudManager().addCustomHud(pr, hud);
        return hud;
    }

    /** Retire le HUD du joueur. */
    public static void detach(Ref<EntityStore> ref, Store<EntityStore> store) {
        try {
            if (ref == null || !ref.isValid()) return;
            Player player = store.getComponent(ref, Player.getComponentType());
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            if (player != null && pr != null && player.getHudManager().getCustomHud(KEY) != null)
                player.getHudManager().removeCustomHud(pr, KEY);
        } catch (Exception e) {
            EldaniorLogger.error("[Arkies Domini] retrait du HUD", e);
        }
    }
}
