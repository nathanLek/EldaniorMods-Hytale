package com.eldanior.system.gui.tabs;

import com.eldanior.system.config.UUIDExtractor;
import com.eldanior.system.quest.PlayerQuest;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Onglet « Quetes » du menu systeme (2026-11-12) : simple liste des quetes en cours et a rendre du joueur, en lecture seule
 * (on les prend et on les rend aux tableaux et aux panneaux). Elements #TabQuetes de System/SystemPage.ui.
 */
public final class QuetesEnCoursTab {

    private QuetesEnCoursTab() {}

    public static final int ROWS = 10;

    public static void populate(UICommandBuilder ui, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        UUID u;
        try { u = pr != null ? UUIDExtractor.getUUID(pr) : null; } catch (Exception e) { u = null; }
        List<PlayerQuest> list = new ArrayList<>();
        if (u != null) {
            PlayerQuest active = QuestManager.getActiveQuest(u);
            if (active != null && !active.isCompleted()) list.add(active);                 // la quete suivie d'abord
            for (PlayerQuest q : QuestManager.getInProgressQuests(u)) if (!list.contains(q)) list.add(q);
            list.addAll(QuestManager.getClaimableQuests(u));
        }
        int done = 0;
        for (PlayerQuest q : list) if (q.isCompleted()) done++;
        ui.set("#QuestHeader.Text", list.isEmpty() ? "" : (list.size() - done) + " en cours" + (done > 0 ? "  ·  " + done + " à rendre" : "")
                + (list.size() > ROWS ? "  ·  " + ROWS + " affichées" : ""));
        ui.set("#QuestEmpty.Visible", list.isEmpty());
        for (int n = 0; n < ROWS; n++) {
            PlayerQuest q = n < list.size() ? list.get(n) : null;
            QuestModel m = q != null ? QuestManager.getQuest(q.getQuestId()) : null;
            ui.set("#QuestRow" + n + ".Visible", m != null);
            if (m == null) continue;
            ui.set("#QuestName" + n + ".Text", m.getName() != null ? m.getName() : q.getQuestId());
            String obj = m.getObjectiveText();
            ui.set("#QuestObj" + n + ".Text", obj != null ? obj : "");
            int target = Math.max(1, m.getTargetAmount());
            ui.set("#QuestProg" + n + ".Text", q.isCompleted() ? "Terminée" : Math.min(q.getProgress(), target) + " / " + target);
            String state, color;
            if (q.isCompleted()) { state = "À rendre"; color = "#f0c868"; }
            else if (q.isActive()) { state = "Suivie"; color = "#7ed47e"; }
            else { state = "En cours"; color = "#96a9be"; }
            ui.set("#QuestState" + n + ".Text", state);
            ui.set("#QuestState" + n + ".Style.TextColor", color);
            ui.set("#QuestBar" + n + ".Background", color);
        }
    }
}
