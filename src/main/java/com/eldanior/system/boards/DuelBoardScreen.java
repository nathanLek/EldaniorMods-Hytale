package com.eldanior.system.boards;

import com.eldanior.system.classement.ClassementManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.duel.DuelManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tableau des duels (bloc Eldanior_Tableau_Duel) : informations sur les duels d'honneur (DuelManager) :
 *   - En cours : duels du moment, vie des deux duellistes, duree ;
 *   - Champions : classement des victoires ; fiche d'un joueur en ligne : le defier (meme defi que l'onglet Duel du menu,
 *     accepte par /es duel accept) ;
 *   - Mes duels : les 10 derniers duels du joueur (vainqueur, vie restante de chacun).
 * Tuiles : victoires, defaites, serie en cours, record.
 */
final class DuelBoardScreen extends BoardScreen {

    private static final int TOP = 50;

    private List<ClassementManager.RankEntry> champions = List.of();
    private int selected = -1;

    DuelBoardScreen(@Nonnull PlayerRef playerRef, String place) {
        super(playerRef, BoardType.DUEL, place);
    }

    @Override
    protected List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData d = data(ref, store);
        if (d == null) return List.of();
        return List.of(new Stat(gold(d.getDuelWins()), "Victoires"), new Stat(gold(d.getDuelLosses()), "Défaites"),
                new Stat(String.valueOf(d.getDuelStreak()), "Série en cours"), new Stat(String.valueOf(d.getDuelBestStreak()), "Record de série"));
    }

    @Override
    protected Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        return switch (index) {
            case 0 -> running(ref, store);
            case 1 -> championsTab(ref, store);
            default -> history(ref, store);
        };
    }

    private Tab running(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID me = me(ref, store);
        List<Row> rows = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (DuelManager.ActiveDuel duel : DuelManager.getActiveDuels()) {
            PlayerRef a = Universe.get().getPlayer(duel.getPlayer1()), b = Universe.get().getPlayer(duel.getPlayer2());
            String na = a != null ? a.getUsername() : "?", nb = b != null ? b.getUsername() : "?";
            long sec = Math.max(0, now - duel.getStartTime()) / 1000;
            rows.add(new Row(null, na + "  contre  " + nb, "depuis " + (sec >= 60 ? sec / 60 + " min " + sec % 60 + " s" : sec + " s"),
                    hp(a) + "  /  " + hp(b), "#c9d1da", false,
                    me != null && (me.equals(duel.getPlayer1()) || me.equals(duel.getPlayer2())), false));
        }
        return new Tab("Duels en cours", rows, "Aucun duel en ce moment.", "Vie des deux duellistes, en direct.", null,
                "Les duels d'honneur du moment, avec la vie restante de chaque duelliste.\n\nUn duel s'arrête quand l'un "
                        + "des deux tombe à 1 point de vie : il n'y a pas de mort. Le perdant cède 10 % de son expérience "
                        + "au vainqueur.\n\nDéfiez un joueur depuis l'onglet Champions ou le menu (onglet Duel).");
    }

    private Tab championsTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        String me = name(ref, store);
        champions = ClassementManager.getDuelRanking(TOP);
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < champions.size(); i++) {
            ClassementManager.RankEntry e = champions.get(i);
            PlayerRef pr = online(e.name());
            String meta = pr == null ? "Hors ligne" : DuelManager.isInDuel(uuid(pr)) ? "En duel" : "En ligne";
            rows.add(new Row(String.valueOf(i + 1), e.name(), meta, gold(e.value()) + " victoires", null, true,
                    e.name().equalsIgnoreCase(me), i == selected));
        }
        return new Tab("Champions du royaume", rows, "Aucun duel n'a encore été gagné.", "Les " + TOP + " meilleurs duellistes.", null,
                "Le classement des duels gagnés.\n\nOuvrez la fiche d'un joueur en ligne pour le défier : il a 60 secondes "
                        + "pour accepter (/es duel accept).");
    }

    private Tab history(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData d = data(ref, store);
        List<Row> rows = new ArrayList<>();
        if (d != null) {
            for (String h : d.getDuelHistory()) {
                String[] p = h.split("\\|");
                if (p.length < 4) continue;
                boolean won = "V".equals(p[0]);
                rows.add(new Row(null, (won ? "Victoire contre " : "Défaite contre ") + p[1],
                        "votre vie : " + p[2] + "  ·  la sienne : " + p[3], won ? "Victoire" : "Défaite", won ? GREEN : RED,
                        false, won, false));
            }
        }
        return new Tab("Vos derniers duels", rows, "Vous n'avez encore livré aucun duel.", "Les 10 derniers duels.", null,
                "Vos 10 derniers duels, du plus récent au plus ancien, avec la vie restante de chacun à la fin.");
    }

    @Override
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab != 1 || selected < 0 || selected >= champions.size()) return null;
        ClassementManager.RankEntry e = champions.get(selected);
        PlayerRef target = online(e.name());
        String me = name(ref, store);
        UUID mine = me(ref, store);
        boolean self = e.name().equalsIgnoreCase(me);
        boolean can = target != null && !self && mine != null && !DuelManager.isInDuel(mine) && !DuelManager.isInDuel(uuid(target));
        String text = "Duels gagnés : " + gold(e.value()) + "\nMonstres vaincus : " + gold(ClassementManager.mobKillsOf(e.name()))
                + "\nJoueurs vaincus : " + gold(ClassementManager.pvpKillsOf(e.name()))
                + (self ? "\n\nC'est vous." : target == null ? "\n\nHors ligne : impossible de le défier." :
                can ? "\n\nLe défier lui envoie une invitation (60 secondes)." : "\n\nUn duel est déjà en cours.");
        return new Side(e.name(), "#" + (selected + 1) + " des duellistes", text, null, null, null, can ? "Défier" : null, "Fermer");
    }

    @Override
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab != 1 || index < 0 || index >= champions.size()) return;
        selected = selected == index ? -1 : index;
    }

    @Override
    protected void onConfirm(long amount, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab != 1 || selected < 0 || selected >= champions.size()) return;
        PlayerRef target = online(champions.get(selected).name());
        PlayerRef self = store.getComponent(ref, PlayerRef.getComponentType());
        UUID mine = me(ref, store), theirs = target != null ? uuid(target) : null;
        if (self == null || mine == null || theirs == null || mine.equals(theirs)) { error("Ce joueur n'est pas en ligne."); return; }
        if (DuelManager.isInDuel(mine) || DuelManager.isInDuel(theirs)) { error("Un duel est déjà en cours."); return; }
        if (!DuelManager.sendChallenge(mine, theirs)) { error("Vous avez déjà un défi en attente."); return; }
        target.sendMessage(Message.raw(self.getUsername() + " vous défie en duel !"));
        target.sendMessage(Message.raw("Tapez /es duel accept ou /es duel decline (expire dans 60 s)"));
        ok("Défi envoyé à " + target.getUsername() + " (expire dans 60 s).");
    }

    @Override
    protected void onCancel() { selected = -1; }

    @Override
    protected void onTab() { selected = -1; }

    private static String hp(PlayerRef pr) {
        return pr == null ? "—" : Math.round(DuelManager.getPlayerHPRatio(pr) * 100) + " %";
    }

    private static PlayerRef online(String name) {
        for (PlayerRef pr : Universe.get().getPlayers()) if (pr.getUsername().equalsIgnoreCase(name)) return pr;
        return null;
    }

    private static UUID uuid(PlayerRef pr) {
        try { return UUIDExtractor.getUUID(pr); } catch (Exception e) { return null; }
    }

    private static UUID me(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        return pr != null ? uuid(pr) : null;
    }

    private static String name(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        return pr != null ? pr.getUsername() : "";
    }
}
