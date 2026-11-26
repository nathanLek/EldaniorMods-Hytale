package com.eldanior.system.boards;

import com.eldanior.system.classement.ClassementManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * Tableau des classements (bloc Eldanior_Tableau_Classement) : les classements du royaume (ClassementManager), les memes que
 * l'onglet Classements du menu, sur TOP lignes paginees :
 *   - Chasseurs : monstres vaincus ;
 *   - Combattants : joueurs vaincus ;
 *   - Guildes (2026-11-19, repris du tableau de guilde) : triees au choix (bouton du bas) par contribution, monstres, joueurs
 *     vaincus ou tresor ; fiche de la guilde ;
 *   - Familles : familles nobles, contribution + tresor / 100.
 * Les duels ont leur propre tableau (DuelBoardScreen). Fiche d'un joueur : ses trois scores et s'il est en ligne.
 */
final class RankingBoardScreen extends BoardScreen {

    private static final int TOP = 50;
    private static final int GUILDS = 2, FAMILIES = 3;

    /** Criteres de tri du classement des guildes : libelle, valeur, unite. */
    private enum Sort {
        CONTRIBUTION("Contribution", Guild::getContribution, " pts"),
        MOBS("Monstres vaincus", g -> g.getTotalMobKills(), ""),
        PLAYERS("Joueurs vaincus", g -> g.getTotalPlayerKills(), ""),
        TREASURY("Trésor", Guild::getTreasury, " or");

        final String label, unit;
        final ToLongFunction<Guild> value;

        Sort(String label, ToLongFunction<Guild> value, String unit) { this.label = label; this.value = value; this.unit = unit; }

        Sort next() { return values()[(ordinal() + 1) % values().length]; }
    }

    private Sort sort = Sort.CONTRIBUTION;
    private List<Guild> guilds = List.of();
    private List<ClassementManager.RankEntry> listed = List.of();
    private int selected = -1;

    RankingBoardScreen(@Nonnull PlayerRef playerRef, String place) {
        super(playerRef, BoardType.CLASSEMENT, place);
    }

    @Override
    protected List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        String me = name(ref, store);
        PlayerLevelData d = data(ref, store);
        return List.of(new Stat(rankOf(ClassementManager.getMobRanking(TOP * 4), me), "Votre rang de chasseur"),
                new Stat(gold(d != null ? d.getTotalMobKills() : 0), "Monstres vaincus"),
                new Stat(rankOf(ClassementManager.getPvPRanking(TOP * 4), me), "Votre rang de combattant"),
                new Stat(gold(d != null ? d.getPlayerKills() : 0), "Joueurs vaincus"));
    }

    @Override
    protected Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (index == GUILDS) return guildTab(ref, store);
        String me = name(ref, store);
        listed = switch (index) {
            case 0 -> ClassementManager.getMobRanking(TOP);
            case 1 -> ClassementManager.getPvPRanking(TOP);
            default -> ClassementManager.getGuildFamilyRanking(TOP * 4).stream()
                    .filter(e -> e.name().startsWith("[F] ")).limit(TOP).toList();       // familles seules
        };
        String unit = switch (index) { case 0 -> " monstres"; case 1 -> " joueurs"; default -> " pts"; };
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < listed.size(); i++) {
            ClassementManager.RankEntry e = listed.get(i);
            boolean guild = index == FAMILIES;
            String shown = guild ? e.name().replace("[F] ", "") : e.name();
            String meta = guild ? "Famille noble" : online(e.name()) ? "En ligne" : "Hors ligne";
            rows.add(new Row(String.valueOf(i + 1), shown, meta, gold(e.value()) + unit, null, !guild,
                    !guild && e.name().equalsIgnoreCase(me), i == selected));
        }
        return switch (index) {
            case 0 -> new Tab("Les plus grands chasseurs", rows, "Personne n'a encore vaincu de monstre.",
                    "Les " + TOP + " premiers du royaume.", null,
                    "Le classement des chasseurs : monstres vaincus depuis le début de l'aventure.\n\nOuvrez une ligne pour "
                            + "voir la fiche d'un joueur.");
            case 1 -> new Tab("Les combattants les plus redoutés", rows, "Aucun joueur n'a encore été vaincu.",
                    "Les " + TOP + " premiers du royaume.", null,
                    "Le classement des combattants : joueurs vaincus hors des duels.\n\nLes duels d'honneur ont leur propre "
                            + "tableau.");
            default -> new Tab("Familles nobles", rows, "Aucune famille classée.",
                    "Score : contribution + trésor / 100.", null,
                    "Les familles nobles du royaume, classées par leur contribution et leur trésor.");
        };
    }

    /** Classement des guildes (ex-onglet du tableau de guilde) : tri au choix, fiche de la guilde. */
    private Tab guildTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        Guild mine = pr != null ? GuildManager.getPlayerGuild(pr.getUuid()) : null;
        List<Guild> all = new ArrayList<>(GuildManager.getAll());
        all.sort(Comparator.comparingLong(sort.value).reversed());
        guilds = all.size() > TOP ? all.subList(0, TOP) : all;
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < guilds.size(); i++) {
            Guild g = guilds.get(i);
            rows.add(new Row(String.valueOf(i + 1), g.getFormattedTag() + " " + g.getName(),
                    g.getMemberCount() + " membres  ·  " + gold(g.getTotalMobKills()) + " monstres  ·  " + gold(g.getTotalPlayerKills()) + " joueurs",
                    gold(sort.value.applyAsLong(g)) + sort.unit, null, true, g == mine, i == selected));
        }
        return new Tab("Guildes · " + sort.label, rows, "Aucune guilde n'a encore été fondée.",
                "Les guildes montent au classement par leurs exploits.", "Trier par : " + sort.next().label,
                "Les " + TOP + " meilleures guildes du royaume.\n\nTriez le classement avec le bouton du bas : contribution, "
                        + "monstres vaincus, joueurs vaincus ou trésor.\n\nOuvrez une fiche pour voir le détail d'une guilde.");
    }

    private Side guildSide(Guild g) {
        String text = "Fondée par " + g.getFounderName() + "\nMembres : " + g.getMemberCount()
                + "\n\nContribution : " + gold(g.getContribution()) + " pts\nTrésor : " + gold(g.getTreasury()) + " or"
                + "\n\nMonstres vaincus : " + gold(g.getTotalMobKills()) + "\nJoueurs vaincus : " + gold(g.getTotalPlayerKills())
                + "\nMorts : " + gold(g.getTotalDeaths())
                + "\n\nRecrutement : " + (g.isOpenRecruitment() ? "ouvert" : "sur invitation");
        return Side.info(g.getFormattedTag() + " " + g.getName(), "Classement : " + (selected + 1) + "e", text);
    }

    /** Bouton du bas (guildes) : critere de tri suivant. */
    @Override
    protected void onAction(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab != GUILDS) return;
        sort = sort.next();
        selected = -1;
    }

    @Override
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab == GUILDS) return selected >= 0 && selected < guilds.size() ? guildSide(guilds.get(selected)) : null;
        if (selected < 0 || selected >= listed.size() || tab == FAMILIES) return null;
        String who = listed.get(selected).name();
        return Side.info(who, "#" + (selected + 1) + (tab == 0 ? " des chasseurs" : " des combattants")
                        + (online(who) ? "  ·  en ligne" : ""),
                "Monstres vaincus : " + gold(ClassementManager.mobKillsOf(who))
                        + "\nJoueurs vaincus : " + gold(ClassementManager.pvpKillsOf(who))
                        + "\nDuels gagnés : " + gold(ClassementManager.duelWinsOf(who)));
    }

    @Override
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab == GUILDS) { if (index >= 0 && index < guilds.size()) selected = selected == index ? -1 : index; return; }
        if (index < 0 || index >= listed.size() || tab == FAMILIES) return;
        selected = selected == index ? -1 : index;
    }

    @Override
    protected void onCancel() { selected = -1; }

    @Override
    protected void onTab() { selected = -1; }

    private static String name(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        return pr != null ? pr.getUsername() : "";
    }

    private static boolean online(String name) {
        for (PlayerRef pr : Universe.get().getPlayers()) if (pr.getUsername().equalsIgnoreCase(name)) return true;
        return false;
    }

    private static String rankOf(List<ClassementManager.RankEntry> board, String name) {
        for (int i = 0; i < board.size(); i++) if (board.get(i).name().equalsIgnoreCase(name)) return "#" + (i + 1);
        return "—";
    }
}
