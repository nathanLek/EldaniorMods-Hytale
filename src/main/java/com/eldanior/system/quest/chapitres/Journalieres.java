package com.eldanior.system.quest.chapitres;

import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.quest.QuestModel;
import com.eldanior.system.quest.chapitres.ChapterQuests.Act;
import com.eldanior.system.quest.chapitres.ChapterQuests.Art;
import com.eldanior.system.quest.chapitres.ChapterQuests.Chain;
import com.eldanior.system.quest.chapitres.ChapterQuests.Kind;
import com.eldanior.system.quest.chapitres.ChapterQuests.MissionType;
import com.eldanior.system.quest.chapitres.ChapterQuests.Objective;
import com.eldanior.system.quest.chapitres.ChapterQuests.Reward;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Affiches du jour au tableau des quetes (2026-11-13, rappel de l'utilisateur : le tableau se gere tout seul) : les quetes
 * journalieres du tirage du jour de QuestManager (les memes pour tous ; celles des PK pour un PK), presentees en cartes
 * « Quete journaliere » dans la fenetre Chapitres. L'acte porte l'identifiant de la quete : acceptation, avancement (quete
 * suivie), recompense (QuestTab.claimQuest) et delai restent ceux de QuestManager. Les affiches changent a minuit.
 */
public final class Journalieres {

    private Journalieres() {}

    /** Cartes deja construites, par identifiant de quete (le tirage change chaque jour). */
    private static final Map<String, Act> CACHE = new ConcurrentHashMap<>();

    /** Affiches du jour pour ce joueur (PK : contrats des PK). */
    public static List<Act> acts(boolean pk) {
        QuestManager.checkDailyReset();
        List<Act> out = new ArrayList<>();
        for (String id : QuestManager.getTodaysDailyIds()) {
            QuestModel m = QuestManager.getQuest(id);
            if (m == null || !m.isDaily() || m.isPKOnly() != pk) continue;
            out.add(CACHE.computeIfAbsent(id, k -> build(m)));
        }
        return out;
    }

    private static Act build(QuestModel m) {
        Chain c = new Chain("jour_" + m.getId(), ChapterQuests.TABLEAU, "Affiche du jour", "Elda_Gold_Coins", MissionType.JOURNALIERE);
        c.daily = m;
        String story = m.getDescription() != null && !m.getDescription().isBlank() ? m.getDescription()
                : "« " + m.getObjectiveText() + ". Le royaume paie ceux qui s'en chargent. »";
        List<Reward> rewards = new ArrayList<>();
        if (m.getRewardXP() > 0) rewards.add(Reward.xp(m.getRewardXP()));
        if (m.getRewardGold() > 0) rewards.add(Reward.money(m.getRewardGold()));
        c.act(m.getName(), Art.AUBE, m.getDifficulty(), 0, story,
                List.of("« Les affiches changent chaque jour à minuit. »",
                        "« Une quête prise et non terminée disparaît à minuit. »",
                        "« Deux quêtes en cours au plus, en plus de la quête principale. »"),
                List.of(new Objective(Kind.DAILY, m.getId(), Math.max(1, m.getTargetAmount()), m.getObjectiveText())),
                rewards);
        return c.acts.get(0);
    }
}
