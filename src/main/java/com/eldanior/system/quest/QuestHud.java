package com.eldanior.system.quest;

import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.quest.chapitres.ChapterQuests;
import com.eldanior.system.quest.dialogue.NpcDialogueQuest;
import com.eldanior.system.quest.dialogue.QuestCondition;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * HUD des quetes en cours (Quest/QuestHud.ui, genere par scripts/quetes/gen_quest_hud.py), aussi rendu par hud/CombinedHud.
 * 2026-11-13 (demande de l'utilisateur) : chaque quete est encadree comme une carte de la fenetre Chapitres (contour a la couleur
 * de son type), avec son rang de mission dans un bandeau a gauche (teinte du type), son type, son nom et ses attendus comme dans
 * la fenetre de quete (case, texte, avancement). Pas de recompense affichee (2026-11-13). Panneau du haut : la quete principale ; panneau du bas : la quete suivie (si ce n'est pas la principale).
 */
public class QuestHud extends CustomUIHud {

    /** = OBJECTIVES de gen_quest_hud.py. */
    static final int OBJECTIVES = 4;
    private static final String GOLD = "#f2c45e";

    private final UUID ownerUUID;
    private PlayerLevelData cachedData;
    private Player cachedPlayer;

    public QuestHud(PlayerRef playerRef, UUID ownerUUID) {
        super(playerRef, "quest_hud");
        this.ownerUUID = ownerUUID;
    }

    public void setCachedData(PlayerLevelData data, Player player) {
        this.cachedData = data;
        this.cachedPlayer = player;
    }

    @Override
    protected void build(UICommandBuilder ui) {
        ui.append("Quest/QuestHud.ui");
        render(ui);
    }

    @Override
    public void update(boolean firstUpdate, UICommandBuilder ui) {
        render(ui);
        super.update(firstUpdate, ui);
    }

    /** Accessible depuis CombinedHud */
    public void renderPublic(UICommandBuilder ui) { render(ui); }

    /** Une ligne d'attendu : texte, avancement, rempli. */
    private record Row(String text, String count, boolean ok) {}

    private void render(UICommandBuilder ui) {
        PlayerQuest main = findMainQuest();
        renderPanel(ui, "QHudMain", main);
        PlayerQuest active = QuestManager.getActiveQuest(ownerUUID);
        if (active != null && main != null && active.getQuestId().equals(main.getQuestId())) active = null;
        renderPanel(ui, "QHud", active);
        renderPanel(ui, "QHudAlt", other(main, active));                                    // 2 quetes en cours + la principale
    }

    private void renderPanel(UICommandBuilder ui, String p, PlayerQuest pq) {
        QuestModel model = pq != null ? QuestManager.getQuest(pq.getQuestId()) : null;
        if (model instanceof NpcDialogueQuest nq && nq.isInfoOnly()) model = null;           // talk_tavernier, etc.
        if (model == null) { ui.set("#" + p + "Panel.Visible", false); return; }
        ui.set("#" + p + "Panel.Visible", true);

        String[] type = typeOf(model);
        ui.set("#" + p + "Panel.Background", type[1]);                                       // contour de la carte
        ui.set("#" + p + "RankLine.Background", type[1]);
        ui.set("#" + p + "Type.Text", type[0]);
        ui.set("#" + p + "Type.Style.TextColor", type[1]);
        // blason du rang (2026-11-14) : losange a la couleur du rang (QHudRank<R>, gen_quest_hud.py), lettre claire dessus
        QuestDifficulty rank = model.getDifficulty();
        for (QuestDifficulty r : QuestDifficulty.values()) ui.set("#" + p + "Badge" + r.name() + ".Visible", r == rank);
        ui.set("#" + p + "Rank.Text", rank.getDisplayName());
        ui.set("#" + p + "Rank.Style.TextColor", light(rank.getColor()));
        ui.set("#" + p + "Name.Text", model instanceof ChapterQuests.ActQuest aq ? aq.act.name : model.getName());

        List<Row> rows = rows(model, pq);
        for (int i = 0; i < OBJECTIVES; i++) {
            boolean shown = i < rows.size();
            ui.set("#" + p + "Obj" + i + ".Visible", shown);
            if (!shown) continue;
            Row r = rows.get(i);
            ui.set("#" + p + "ObjOn" + i + ".Visible", r.ok());
            ui.set("#" + p + "ObjOff" + i + ".Visible", !r.ok());
            ui.set("#" + p + "ObjText" + i + ".Text", r.text());
            ui.set("#" + p + "ObjText" + i + ".Style.TextColor", r.ok() ? "#d9c58a" : "#b8c2d0");
            ui.set("#" + p + "ObjCount" + i + ".Text", r.count());
            ui.set("#" + p + "ObjCount" + i + ".Style.TextColor", r.ok() ? GOLD : "#77879d");
        }
    }

    /** Couleur du rang eclaircie (60 % vers le blanc) pour la lettre posee sur le blason, en #rrggbb. */
    private static String light(String hex) {
        int c = Integer.parseInt(hex.substring(1), 16);
        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
        return String.format("#%02x%02x%02x", r + (255 - r) * 6 / 10, g + (255 - g) * 6 / 10, b + (255 - b) * 6 / 10);
    }

    /** Libelle du type et couleur du contour (memes couleurs que les cartes de la fenetre Chapitres). */
    private static String[] typeOf(QuestModel m) {
        if (m instanceof ChapterQuests.ActQuest aq) {
            ChapterQuests.MissionType t = aq.act.chain.type;
            String color = t == ChapterQuests.MissionType.PRIME ? t.color
                    : t == ChapterQuests.MissionType.PRINCIPALE || aq.act.rank.atLeastS() ? GOLD : t.color;
            return new String[]{aq.act.chain.evolutive() ? t.label + " · " + aq.act.actLabel() : t.label, color};
        }
        return switch (m.getCategory()) {
            case PRINCIPAL -> new String[]{"Quête principale", GOLD};
            case JOURNALIERE -> new String[]{"Quête journalière", ChapterQuests.MissionType.JOURNALIERE.color};
            case PRIME -> new String[]{"Prime", ChapterQuests.MissionType.PRIME.color};
            default -> new String[]{"Quête secondaire", ChapterQuests.MissionType.SECONDAIRE.color};
        };
    }

    /** Attendus, comme dans la fenetre de quete. */
    private List<Row> rows(QuestModel model, PlayerQuest pq) {
        List<Row> out = new ArrayList<>();
        if (model instanceof ChapterQuests.ActQuest aq) {                                   // memes lignes que la fenetre (2026-11-14)
            ChapterQuests.State st = pq.isCompleted() ? ChapterQuests.State.DONE : ChapterQuests.State.IN_PROGRESS;
            for (var r : com.eldanior.system.quest.chapitres.QuestChaptersPage.rows(aq.act, st, pq, cachedData, cachedPlayer))
                out.add(new Row(r.text(), r.count(), r.ok()));
            return out;
        }
        if (model instanceof NpcDialogueQuest nq && nq.getCompletionCondition() != null) {
            QuestCondition cond = nq.getCompletionCondition();
            if (cond.getRequiredLevel() > 0) {
                int cur = cachedData != null ? cachedData.getLevel() : 0;
                boolean ok = cur >= cond.getRequiredLevel();
                out.add(new Row("Atteindre le niveau " + cond.getRequiredLevel(), ok ? "Fait" : "Niv. " + cur + " / " + cond.getRequiredLevel(), ok));
            }
            if (cond.getRequiredQuestCompleted() != null) {
                boolean ok = cachedData != null && cachedData.getQuestData() != null
                        && cachedData.getQuestData().contains(cond.getRequiredQuestCompleted() + ":")
                        && cachedData.getQuestData().contains(":COMPLETED");
                out.add(new Row("Trouver l'indice", ok ? "Fait" : "0 / 1", ok));
            }
            if (cond.getRequiredItemId() != null) {
                boolean ok = QuestManager.hasItemInFullInventory(cachedPlayer, cond.getRequiredItemId());
                String name = cond.getRequiredItemId().replaceAll("([a-z])([A-Z])", "$1 $2").replace("_", " ");
                if (name.contains(":")) name = name.substring(name.indexOf(':') + 1);
                out.add(new Row("Obtenir : " + name, ok ? "Fait" : "0 / 1", ok));
            }
            return out;
        }
        int have, need = Math.max(1, model.getTargetAmount());
        if ((model.getType() == QuestType.MINAGE || model.getType() == QuestType.RECOLTE) && cachedPlayer != null && model.getTargetId() != null)
            have = QuestManager.countItemInInventory(cachedPlayer, model.getTargetId());
        else
            have = pq.getProgress();
        boolean ok = pq.isCompleted() || have >= need;
        out.add(new Row(model.getObjectiveText(), ok ? "Fait" : Math.min(have, need) + " / " + need, ok));
        return out;
    }

    /** L'autre quete en cours (ni la principale, ni celle deja affichee), null si aucune. */
    private PlayerQuest other(PlayerQuest main, PlayerQuest shown) {
        for (PlayerQuest pq : QuestManager.getPlayerQuests(ownerUUID)) {
            if (pq.isCompleted() || pq == main || pq == shown) continue;
            QuestModel m = QuestManager.getQuest(pq.getQuestId());
            if (m == null || m.isMainStory() || m instanceof NpcDialogueQuest nq && nq.isInfoOnly()) continue;
            return pq;
        }
        return null;
    }

    private PlayerQuest findMainQuest() {
        for (PlayerQuest pq : QuestManager.getPlayerQuests(ownerUUID)) {
            QuestModel model = QuestManager.getQuest(pq.getQuestId());
            if (model != null && model.isMainStory() && !pq.isCompleted()) return pq;
        }
        return null;
    }
}
