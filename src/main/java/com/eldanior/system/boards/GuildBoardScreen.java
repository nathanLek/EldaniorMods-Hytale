package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.Leveling.utils.NotificationHelper;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.ToLongFunction;

/**
 * Tableau de guilde :
 *   - Classement : les 10 premieres guildes, triees au choix (bouton du bas) par contribution, monstres, joueurs vaincus ou
 *     tresor ; fiche de la guilde.
 *   - Contrats : contrats de guilde de la semaine (GuildBoard), avancement de sa guilde, recompenses.
 *   - Missions : missions individuelles du jour ; Prendre, Abandonner, Reclamer (or, XP, contribution pour sa guilde).
 */
final class GuildBoardScreen extends BoardScreen {

    private static final int TOP = 10;

    /** Criteres de tri du classement : libelle, valeur, unite. */
    private enum Sort {
        CONTRIBUTION("Contribution", Guild::getContribution, " pts"),
        MOBS("Monstres vaincus", g -> g.getTotalMobKills(), ""),
        PLAYERS("Joueurs vaincus", g -> g.getTotalPlayerKills(), ""),
        TREASURY("Trésor", Guild::getTreasury, " or");

        final String label, unit;
        final ToLongFunction<Guild> value;

        Sort(String label, ToLongFunction<Guild> value, String unit) { this.label = label; this.value = value; this.unit = unit; }
    }

    private Sort sort = Sort.CONTRIBUTION;
    private List<Guild> ranking = List.of();
    /** Missions affichees : celles du jour, puis celles prises un autre jour. */
    private List<GuildBoard.Mission> listed = List.of();
    private int selected = -1;
    private final UUID viewer;

    GuildBoardScreen(@Nonnull PlayerRef playerRef, String place) {
        super(playerRef, BoardType.GUILDE, place);
        this.viewer = playerRef.getUuid();
    }

    // ===== Contenu =====

    @Override
    protected List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID me = me(ref, store);
        Guild mine = me != null ? GuildManager.getPlayerGuild(me) : null;
        return List.of(new Stat(String.valueOf(GuildManager.getAll().size()), "Guildes"),
                new Stat(mine != null ? mine.getFormattedTag() : "Aucune", "Votre guilde"),
                new Stat(mine != null ? gold(mine.getTreasury()) : "—", "Trésor de la guilde"),
                new Stat(GuildBoard.takenBy(me).size() + " / " + GuildBoard.MAX_TAKEN, "Vos missions"));
    }

    @Override
    protected Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID me = me(ref, store);
        Guild mine = me != null ? GuildManager.getPlayerGuild(me) : null;
        return switch (index) {
            case 0 -> rankingTab(mine);
            case 1 -> contractsTab(mine);
            default -> missionsTab(me);
        };
    }

    private Tab rankingTab(Guild mine) {
        ranking = new ArrayList<>(GuildManager.getAll());
        ranking.sort(Comparator.comparingLong(sort.value).reversed());
        if (ranking.size() > TOP) ranking = ranking.subList(0, TOP);
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < ranking.size(); i++) {
            Guild g = ranking.get(i);
            rows.add(new Row(String.valueOf(i + 1), g.getFormattedTag() + " " + g.getName(),
                    g.getMemberCount() + " membres  ·  " + gold(g.getTotalMobKills()) + " monstres  ·  " + gold(g.getTotalPlayerKills()) + " joueurs",
                    gold(sort.value.applyAsLong(g)) + sort.unit, null, true, g == mine, i == selected));
        }
        return new Tab("Classement · " + sort.label, rows, "Aucune guilde n'a encore été fondée.",
                "Les guildes montent au classement par leurs exploits.", "Trier par : " + next(sort).label,
                "Les " + TOP + " meilleures guildes du royaume.\n\nTriez le classement avec le bouton du bas : contribution, "
                        + "monstres vaincus, joueurs vaincus ou trésor.\n\nOuvrez une fiche pour voir le détail d'une guilde.");
    }

    private Tab contractsTab(Guild mine) {
        List<GuildBoard.Contract> weekly = GuildBoard.weekly();
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < weekly.size(); i++) {
            GuildBoard.Contract c = weekly.get(i);
            int p = GuildBoard.progress(mine, i);
            String state = mine == null ? "rejoignez une guilde" : p < 0 ? "accompli !" : "votre guilde : " + gold(p) + " / " + gold(c.count());
            rows.add(new Row(String.valueOf(i + 1), c.title(), "Tuer " + gold(c.count()) + " " + c.family().label + "  ·  " + state,
                    p < 0 ? "Accompli" : gold(c.treasury()) + " or", p < 0 ? GREEN : GOLD, true, p < 0, i == selected));
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), GuildBoard.weekEnd());
        return new Tab("Contrats de la semaine", rows, "Aucun contrat de guilde cette semaine.",
                "Fin des contrats " + (days == 0 ? "ce soir à minuit." : "dans " + days + " jour" + (days > 1 ? "s" : "") + " (dimanche à minuit)."), null,
                "Missions communes de la semaine : chaque monstre tué par un membre fait avancer celles de sa guilde.\n\n"
                        + "Réussies, elles remplissent le trésor de la guilde et augmentent sa contribution.");
    }

    private Tab missionsTab(UUID me) {
        List<GuildBoard.Mission> list = new ArrayList<>(GuildBoard.missions());
        for (GuildBoard.Taken t : GuildBoard.takenBy(me)) if (!list.contains(t.mission)) list.add(t.mission);
        listed = list;
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < listed.size(); i++) {
            GuildBoard.Mission m = listed.get(i);
            GuildBoard.Taken t = GuildBoard.taken(me, m.id());
            String meta = "Tuer " + m.count() + " " + m.family().label + (t != null ? "  ·  " + Math.min(t.progress, m.count()) + " / " + m.count() : "");
            boolean done = t != null && t.done();
            rows.add(new Row(String.valueOf(i + 1), m.title(), meta, done ? "Terminée !" : gold(m.gold()) + " or",
                    done ? GREEN : GOLD, true, t != null, i == selected));
        }
        return new Tab("Missions", rows, "Aucune mission disponible pour le moment.",
                "Nouvelles missions chaque jour à minuit. Au plus " + GuildBoard.MAX_TAKEN + " en même temps.", null,
                "Missions individuelles : prenez-en une, accomplissez-la, puis revenez à un tableau de guilde toucher la récompense.\n\n"
                        + "Or et expérience pour vous, contribution pour votre guilde.");
    }

    @Override
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (selected < 0) return null;
        UUID me = me(ref, store);
        return switch (tab) {
            case 0 -> selected < ranking.size() ? guildSide(ranking.get(selected)) : null;
            case 1 -> contractSide(me != null ? GuildManager.getPlayerGuild(me) : null);
            default -> selected < listed.size() ? missionSide(me, listed.get(selected)) : null;
        };
    }

    private Side guildSide(Guild g) {
        String text = "Fondée par " + g.getFounderName() + "\nMembres : " + g.getMemberCount()
                + "\n\nContribution : " + gold(g.getContribution()) + " pts\nTrésor : " + gold(g.getTreasury()) + " or"
                + "\n\nMonstres vaincus : " + gold(g.getTotalMobKills()) + "\nJoueurs vaincus : " + gold(g.getTotalPlayerKills())
                + "\nMorts : " + gold(g.getTotalDeaths())
                + "\n\nRecrutement : " + (g.isOpenRecruitment() ? "ouvert" : "sur invitation");
        return Side.info(g.getFormattedTag() + " " + g.getName(), "Classement : " + (selected + 1) + "e", text);
    }

    private Side contractSide(Guild mine) {
        List<GuildBoard.Contract> weekly = GuildBoard.weekly();
        if (selected >= weekly.size()) return null;
        GuildBoard.Contract c = weekly.get(selected);
        int p = GuildBoard.progress(mine, selected);
        String state = mine == null ? "Rejoignez une guilde pour y participer."
                       : p < 0 ? "Accompli ! La récompense a été versée à votre guilde."
                       : "Votre guilde : " + gold(p) + " / " + gold(c.count()) + " (" + p * 100 / c.count() + " %)";
        String text = "Tuer " + gold(c.count()) + " " + c.family().label + " avant dimanche minuit.\n\nRécompense de la guilde :\n"
                + gold(c.treasury()) + " or au trésor\n+" + gold(c.contribution()) + " contribution\n\n" + state;
        return Side.info(c.title(), "Contrat de guilde", text);
    }

    private Side missionSide(UUID me, GuildBoard.Mission m) {
        GuildBoard.Taken t = GuildBoard.taken(me, m.id());
        String text = "Tuer " + m.count() + " " + m.family().label + ".\n\nRécompense :\n" + gold(m.gold()) + " or\n"
                + gold(m.xp()) + " XP\n+" + gold(m.contribution()) + " contribution pour votre guilde"
                + (t == null ? "" : "\n\nAvancement : " + Math.min(t.progress, m.count()) + " / " + m.count());
        String confirm = t == null ? "Prendre la mission" : t.done() ? "Réclamer la récompense" : null;
        String cancel = t != null && !t.done() ? "Abandonner" : "Fermer";
        return new Side(m.title(), t == null ? "Mission disponible" : t.done() ? "Mission accomplie" : "Mission en cours",
                text, null, null, null, confirm, cancel);
    }

    // ===== Actions =====

    @Override
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) { selected = index; }

    @Override
    protected void onTab() { selected = -1; }

    /** Bouton du bas (classement) : critere de tri suivant. */
    @Override
    protected void onAction(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab != 0) return;
        sort = next(sort);
        selected = -1;
    }

    /** Fermer ; sur une mission en cours : l'abandonner. */
    @Override
    protected void onCancel() {
        if (tab == 2 && selected >= 0 && selected < listed.size()) {
            GuildBoard.Taken t = GuildBoard.taken(viewer, listed.get(selected).id());
            if (t != null && !t.done()) {
                GuildBoard.abandon(viewer, t.mission.id());
                ok("Mission abandonnée.");
                return;
            }
        }
        selected = -1;
    }

    /** Prendre la mission, ou toucher sa recompense. */
    @Override
    protected void onConfirm(long amount, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab != 2 || selected < 0 || selected >= listed.size()) return;
        UUID me = me(ref, store);
        GuildBoard.Mission m = listed.get(selected);
        GuildBoard.Taken t = GuildBoard.taken(me, m.id());
        if (t == null) {
            String refused = GuildBoard.missions().contains(m) ? GuildBoard.take(me, m) : "Cette mission n'est plus proposée.";
            if (refused != null) error(refused); else ok("Mission prise : " + m.title() + ".");
            return;
        }
        if (!t.done() || GuildBoard.claim(me, m.id()) == null) return;
        PlayerLevelData data = data(ref, store);
        PlayerLevelData copy = data != null ? (PlayerLevelData) data.clone() : null;
        if (copy != null) {
            int before = copy.getLevel();
            copy.addMoney(com.eldanior.system.guild.GuildUpgrade.applyGold(me, m.gold()));      // Fortune, Sagesse de sa guilde
            copy.addExperience(com.eldanior.system.guild.GuildUpgrade.applyXp(me, m.xp()));
            store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
            PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
            if (pr != null && copy.getLevel() > before) NotificationHelper.showLevelUpTitle(pr, copy.getLevel());
        }
        Guild g = GuildManager.getPlayerGuild(me);
        if (g != null) g.addContribution(m.contribution());
        EldaniorLogger.info("[Boards] mission " + m.title() + " reclamee (" + m.gold() + " or, " + m.xp() + " XP)");
        ok("Récompense touchée : " + gold(m.gold()) + " or, " + gold(m.xp()) + " XP" + (g != null ? ", +" + gold(m.contribution()) + " contribution." : "."));
        selected = -1;
    }

    // ===== Outils =====

    private static Sort next(Sort s) {
        return Sort.values()[(s.ordinal() + 1) % Sort.values().length];
    }

    private static UUID me(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        return pr != null ? pr.getUuid() : null;
    }
}
