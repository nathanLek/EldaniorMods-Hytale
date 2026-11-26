package com.eldanior.system.boards;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.gui.tabs.QuestTab;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.QuestType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tableau des quetes (bloc Eldanior_Tableau_Quetes) : les quetes journalieres, celles du systeme de quetes (QuestManager,
 * meme tirage du jour pour tous, quetes PK pour les PK), prises et rendues sans ouvrir le menu du systeme :
 *   - Du jour : les affiches du jour ; fiche : Accepter, Suivre (quete affichee au HUD), Abandonner, Reclamer ;
 *   - En cours : journalieres prises et pas encore terminees, avec leur avancement ;
 *   - A reclamer : journalieres terminees ; bouton du bas : tout reclamer.
 * La recompense passe par QuestTab.claimQuest, comme dans le menu (objets apportes, XP, or, titre).
 */
final class QuestBoardScreen extends BoardScreen {

    /** Etat d'une quete pour le joueur. */
    private enum State { AVAILABLE, IN_PROGRESS, CLAIMABLE, COOLDOWN }

    /** Quetes de l'onglet affiche (identifiants, dans l'ordre des lignes). */
    private List<String> listed = List.of();
    private String selected;

    QuestBoardScreen(@Nonnull PlayerRef playerRef, String place) {
        super(playerRef, BoardType.QUETES, place);
    }

    // ===== Contenu =====

    @Override
    protected List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        QuestManager.checkDailyReset();
        UUID me = me(ref, store);
        boolean pk = pk(ref, store);
        int today = todays(pk).size(), running = 0, done = 0;
        for (PlayerQuest pq : dailies(me)) {
            if (pq.isCompleted()) done++;
            else running++;
        }
        return List.of(new Stat(String.valueOf(today), pk ? "Contrats de PK du jour" : "Affiches du jour"),
                new Stat(String.valueOf(running), "En cours"),
                new Stat(String.valueOf(done), "À réclamer"),
                new Stat(untilMidnight(), "Nouvelles affiches dans"));
    }

    @Override
    protected Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID me = me(ref, store);
        Player player = store.getComponent(ref, Player.getComponentType());
        List<String> ids = new ArrayList<>();
        switch (index) {
            case 0 -> { for (QuestModel m : todays(pk(ref, store))) ids.add(m.getId()); }
            case 1 -> { for (PlayerQuest pq : dailies(me)) if (!pq.isCompleted()) ids.add(pq.getQuestId()); }
            default -> { for (PlayerQuest pq : dailies(me)) if (pq.isCompleted()) ids.add(pq.getQuestId()); }
        }
        listed = ids;
        List<Row> rows = new ArrayList<>();
        for (String id : ids) {
            QuestModel m = QuestManager.getQuest(id);
            if (m == null) continue;
            PlayerQuest pq = owned(me, id);
            State st = state(me, m, pq);
            String value, color;
            switch (st) {
                case CLAIMABLE -> { value = "À réclamer"; color = GREEN; }
                case IN_PROGRESS -> { value = progress(m, pq, player); color = "#7a9cc6"; }
                case COOLDOWN -> { value = "Faite"; color = MUTED; }
                default -> { value = "+" + gold(m.getRewardGold()) + " or"; color = null; }
            }
            String meta = m.getType().getDisplayName() + "  ·  " + m.getDifficulty().getDisplayName() + "  ·  " + m.getObjectiveText()
                    + (pq != null && pq.isActive() ? "  ·  suivie" : "");
            rows.add(new Row(null, m.getName(), meta, value, color, true, pq != null, id.equals(selected)));
        }
        return switch (index) {
            case 0 -> new Tab("Affiches du jour", rows, "Aucune affiche aujourd'hui.", "Les affiches changent chaque jour à minuit.",
                    null, "Les quêtes journalières du royaume : les mêmes pour tous aujourd'hui (les PK ont leurs propres contrats).\n\n"
                    + "Ouvrez une affiche pour l'accepter. Une quête suivie s'affiche à l'écran ; seule la quête suivie avance.\n\n"
                    + "Une quête prise et non terminée disparaît à minuit.");
            case 1 -> new Tab("Quêtes en cours", rows, "Aucune quête journalière en cours.", "Seule la quête suivie avance.", null,
                    "Vos quêtes journalières en cours et leur avancement.\n\nOuvrez-en une pour la suivre (elle s'affiche à "
                            + "l'écran et avance) ou l'abandonner.");
            default -> new Tab("Quêtes à réclamer", rows, "Aucune récompense en attente.", "Les objets demandés sont retirés du sac.",
                    rows.isEmpty() ? null : "Tout réclamer",
                    "Vos quêtes journalières terminées : réclamez la récompense (XP, or, titre).\n\nPour une quête de minage ou de "
                            + "récolte, les objets doivent être dans votre sac au moment de réclamer.");
        };
    }

    @Override
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (selected == null) return null;
        QuestModel m = QuestManager.getQuest(selected);
        if (m == null) { selected = null; return null; }
        UUID me = me(ref, store);
        PlayerQuest pq = owned(me, selected);
        State st = state(me, m, pq);
        Player player = store.getComponent(ref, Player.getComponentType());
        StringBuilder text = new StringBuilder(m.getDescription() != null ? m.getDescription() : "");
        text.append("\n\nObjectif : ").append(m.getObjectiveText());
        if (st == State.IN_PROGRESS) text.append("  (").append(progress(m, pq, player)).append(")");
        text.append("\nRécompense : ").append(gold(m.getRewardXP())).append(" XP, ").append(gold(m.getRewardGold())).append(" or");
        if (m.getRewardTitleId() != null) text.append(", un titre");
        String sub = m.getType().getDisplayName() + "  ·  " + m.getDifficulty().getDisplayName();
        return switch (st) {
            case AVAILABLE -> new Side(m.getName(), sub, text.toString(), null, null, null, "Accepter", "Fermer");
            case IN_PROGRESS -> new Side(m.getName(), sub + (pq.isActive() ? "  ·  suivie" : ""), text.toString(), null, null, null,
                    pq.isActive() ? null : "Suivre", "Abandonner");
            case CLAIMABLE -> new Side(m.getName(), sub + "  ·  terminée", text.toString(), null, null, null, "Réclamer", "Fermer");
            case COOLDOWN -> Side.info(m.getName(), sub, text + "\n\nDéjà faite : de retour dans "
                    + left(QuestManager.getCooldownEnd(me, m.getId()) - System.currentTimeMillis()) + ".");
        };
    }

    // ===== Evenements =====

    @Override
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (index < 0 || index >= listed.size()) return;
        String id = listed.get(index);
        selected = id.equals(selected) ? null : id;
    }

    @Override
    protected void onTab() { selected = null; }

    @Override
    protected void onConfirm(long amount, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (selected == null) return;
        QuestModel m = QuestManager.getQuest(selected);
        UUID me = me(ref, store);
        if (m == null || me == null) return;
        PlayerQuest pq = owned(me, selected);
        switch (state(me, m, pq)) {
            case AVAILABLE -> {
                String refused = QuestManager.refusal(me, m.getId());          // une seule quete a la fois (hors principale)
                if (refused != null) { error(refused); return; }
                if (!todays(pk(ref, store)).contains(m) || !QuestManager.acceptQuest(me, m.getId())) {
                    error("Cette affiche n'est plus disponible.");
                    return;
                }
                if (QuestManager.getActiveQuest(me) == null) QuestManager.activateQuest(me, m.getId());   // premiere quete : suivie
                QuestTab.saveQuestData(me, ref, store);
                ok("Quête acceptée : " + m.getName() + (QuestManager.getActiveQuest(me) != null
                        && m.getId().equals(QuestManager.getActiveQuest(me).getQuestId()) ? " (suivie)." : "."));
            }
            case IN_PROGRESS -> {
                QuestManager.activateQuest(me, m.getId());
                QuestTab.saveQuestData(me, ref, store);
                ok("Quête suivie : " + m.getName() + ".");
            }
            case CLAIMABLE -> {
                if (QuestTab.claimQuest(m.getId(), ref, store)) {
                    ok("Récompense reçue : " + m.getName() + ".");
                    selected = null;
                } else error(m.getType() == QuestType.MINAGE || m.getType() == QuestType.RECOLTE
                        ? "Il vous manque des objets dans le sac." : "Impossible de réclamer cette quête.");
            }
            default -> {}
        }
    }

    @Override
    protected void onCancel(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID me = me(ref, store);
        if (selected != null && me != null) {
            PlayerQuest pq = owned(me, selected);
            QuestModel m = QuestManager.getQuest(selected);
            if (pq != null && !pq.isCompleted() && m != null) {
                QuestManager.abandonQuest(me, selected);
                QuestTab.saveQuestData(me, ref, store);
                ok("Quête abandonnée : " + m.getName() + ".");
            }
        }
        selected = null;
    }

    @Override
    protected void onAction(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab != 2) return;
        UUID me = me(ref, store);
        int got = 0, missing = 0;
        for (PlayerQuest pq : dailies(me)) {
            if (!pq.isCompleted()) continue;
            if (QuestTab.claimQuest(pq.getQuestId(), ref, store)) got++;
            else missing++;
        }
        selected = null;
        if (got > 0) ok(got + " récompense" + (got > 1 ? "s reçues" : " reçue") + (missing > 0 ? " ; " + missing + " en attente d'objets." : "."));
        else error(missing > 0 ? "Il vous manque des objets dans le sac." : "Aucune récompense en attente.");
    }

    // ===== Outils =====

    private static UUID me(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null) return null;
        try { return UUIDExtractor.getUUID(pr); } catch (Exception e) { return null; }
    }

    private static boolean pk(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData d = data(ref, store);
        return d != null && d.isPK();
    }

    /** Affiches du jour pour ce joueur : journalieres du tirage, celles des PK pour un PK, les autres sinon. */
    private static List<QuestModel> todays(boolean pk) {
        List<QuestModel> out = new ArrayList<>();
        for (String id : QuestManager.getTodaysDailyIds()) {
            QuestModel m = QuestManager.getQuest(id);
            if (m != null && m.isDaily() && m.isPKOnly() == pk) out.add(m);
        }
        return out;
    }

    /** Journalieres prises par le joueur (en cours et terminees). */
    private static List<PlayerQuest> dailies(UUID me) {
        List<PlayerQuest> out = new ArrayList<>();
        if (me == null) return out;
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me)) {
            QuestModel m = QuestManager.getQuest(pq.getQuestId());
            if (m != null && m.isDaily()) out.add(pq);
        }
        return out;
    }

    private static PlayerQuest owned(UUID me, String id) {
        if (me == null) return null;
        for (PlayerQuest pq : QuestManager.getPlayerQuests(me)) if (pq.getQuestId().equals(id)) return pq;
        return null;
    }

    private static State state(UUID me, QuestModel m, PlayerQuest pq) {
        if (pq != null) return pq.isCompleted() ? State.CLAIMABLE : State.IN_PROGRESS;
        if (me != null && QuestManager.getCooldownEnd(me, m.getId()) > System.currentTimeMillis()) return State.COOLDOWN;
        return State.AVAILABLE;
    }

    /** Avancement : objets dans le sac pour le minage et la recolte, compteur de la quete sinon. */
    private static String progress(QuestModel m, PlayerQuest pq, Player player) {
        int have = pq != null ? pq.getProgress() : 0;
        if ((m.getType() == QuestType.MINAGE || m.getType() == QuestType.RECOLTE) && player != null && m.getTargetId() != null)
            have = QuestManager.countItemInInventory(player, m.getTargetId());
        return gold(Math.min(have, m.getTargetAmount())) + " / " + gold(m.getTargetAmount());
    }

    private static String untilMidnight() {
        return left(Duration.between(LocalDateTime.now(), LocalDate.now().plusDays(1).atStartOfDay()).toMillis());
    }

    private static String left(long ms) {
        long min = Math.max(0, ms) / 60_000;
        return min >= 60 ? (min / 60) + " h " + String.format("%02d", min % 60) : Math.max(1, min) + " min";
    }
}
