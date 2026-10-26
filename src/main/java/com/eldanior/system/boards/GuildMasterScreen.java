package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guild.Guild;
import com.eldanior.system.guild.GuildManager;
import com.eldanior.system.guild.GuildUpgrade;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Fenetre du Maitre de guilde (PNJ de poste, touche d'interaction : GuildMasterSystem), au design des tableaux :
 *   - Fonder : guildes en recrutement ouvert (fiche, Rejoindre) ; sans selection, fonder sa guilde (nom, tag) pour
 *     {@link #FOUND_COST} or a partir du niveau {@link #FOUND_LEVEL} (memes regles que /es guildcreate, reservee aux admins).
 *   - Ameliorations : Sagesse, Fortune, Effectifs (GuildUpgrade), achetees par le chef avec le tresor de la guilde.
 */
public final class GuildMasterScreen extends BoardScreen {

    public static final long FOUND_COST = 150_000;
    public static final int FOUND_LEVEL = 120;

    private List<Guild> open = List.of();
    private int selected = -1;

    public GuildMasterScreen(@Nonnull PlayerRef playerRef, String place) {
        super(playerRef, BoardType.MAITRE, place);
    }

    // ===== Contenu =====

    @Override
    protected List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        Guild g = guild(ref, store);
        return List.of(new Stat(g != null ? g.getFormattedTag() : "Aucune", "Votre guilde"),
                new Stat(g != null ? gold(g.getTreasury()) : "—", "Trésor"),
                new Stat(g != null ? g.getMemberCount() + " / " + g.getMaxMembers() : "—", "Membres"),
                new Stat(g != null ? "+" + g.getUpgrade(GuildUpgrade.SAGESSE) * GuildUpgrade.XP_PER_LEVEL + " % · +"
                        + g.getUpgrade(GuildUpgrade.FORTUNE) * GuildUpgrade.GOLD_PER_LEVEL + " %" : "—", "Bonus XP · Or"));
    }

    @Override
    protected Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        Guild g = guild(ref, store);
        if (index == 1) {
            List<Row> rows = new ArrayList<>();
            if (g != null) {
                GuildUpgrade[] all = GuildUpgrade.values();
                for (int i = 0; i < all.length; i++) {
                    GuildUpgrade u = all[i];
                    int level = g.getUpgrade(u);
                    boolean max = level >= u.getMaxLevel();
                    rows.add(new Row(String.valueOf(level), u.getLabel(), u.describe(level) + "  ·  niveau " + level + " / " + u.getMaxLevel(),
                            max ? "Maximum" : gold(u.nextCost(level)) + " or", max ? GREEN : GOLD, true, false, i == selected));
                }
            }
            return new Tab("Améliorations de la guilde", rows, "Rejoignez ou fondez une guilde pour l'améliorer.",
                    "Les améliorations sont payées avec le trésor de la guilde, par son chef.", null,
                    "Bonus permanents pour tous les membres : expérience, or, places de membres.\n\n"
                            + "Le trésor se remplit par les contrats de guilde, les impôts et les dons.");
        }
        open = new ArrayList<>(GuildManager.getOpenGuilds());
        open.sort(Comparator.comparingLong(Guild::getContribution).reversed());
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < open.size(); i++) {
            Guild o = open.get(i);
            rows.add(new Row(String.valueOf(i + 1), o.getFormattedTag() + " " + o.getName(),
                    o.getMemberCount() + " / " + o.getMaxMembers() + " membres  ·  fondée par " + o.getFounderName(),
                    gold(o.getContribution()) + " pts", null, true, o == g, i == selected));
        }
        return new Tab("Guildes qui recrutent", rows, "Aucune guilde ne recrute ouvertement pour le moment.",
                "Fondez votre guilde, ou rejoignez une guilde en recrutement ouvert.", null, null);
    }

    @Override
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) {
        Guild g = guild(ref, store);
        if (tab == 1) return selected >= 0 && g != null && selected < GuildUpgrade.values().length ? upgradeSide(g, GuildUpgrade.values()[selected], ref, store) : null;
        if (selected >= 0 && selected < open.size()) return joinSide(open.get(selected), g);
        if (g != null)
            return Side.info(g.getFormattedTag() + " " + g.getName(), "Votre guilde",
                    "Vous êtes déjà membre d'une guilde.\n\nMembres : " + g.getMemberCount() + " / " + g.getMaxMembers()
                            + "\nTrésor : " + gold(g.getTreasury()) + " or\nContribution : " + gold(g.getContribution()) + " pts"
                            + "\n\nAméliorez-la dans l'onglet Améliorations.");
        PlayerLevelData data = data(ref, store);
        boolean ready = data != null && data.getLevel() >= FOUND_LEVEL && data.getMoney() >= FOUND_COST;
        return new Side("Fonder une guilde", "Coût : " + gold(FOUND_COST) + " or  ·  niveau " + FOUND_LEVEL,
                "Choisissez un nom (3 à 24 caractères) et un tag (2 à 5 lettres). Vous en serez le chef."
                        + (ready ? "" : "\n\nIl vous faut le niveau " + FOUND_LEVEL + " et " + gold(FOUND_COST) + " or."),
                null, null, null, "Fonder  ·  " + gold(FOUND_COST) + " or", null, true);
    }

    private Side joinSide(Guild o, Guild mine) {
        String text = "Fondée par " + o.getFounderName() + "\nMembres : " + o.getMemberCount() + " / " + o.getMaxMembers()
                + "\nContribution : " + gold(o.getContribution()) + " pts\nTrésor : " + gold(o.getTreasury()) + " or"
                + "\n\nAméliorations :\n" + GuildUpgrade.SAGESSE.describe(o.getUpgrade(GuildUpgrade.SAGESSE))
                + "\n" + GuildUpgrade.FORTUNE.describe(o.getUpgrade(GuildUpgrade.FORTUNE));
        boolean canJoin = mine == null && !o.isFull();
        return new Side(o.getFormattedTag() + " " + o.getName(), o.isFull() ? "Complète" : "Recrutement ouvert", text,
                null, null, null, canJoin ? "Rejoindre la guilde" : null, "Fermer");
    }

    private Side upgradeSide(Guild g, GuildUpgrade u, Ref<EntityStore> ref, Store<EntityStore> store) {
        int level = g.getUpgrade(u);
        boolean max = level >= u.getMaxLevel();
        boolean chief = isChief(g, ref, store);
        long cost = u.nextCost(level);
        String text = "Actuellement : " + (level == 0 ? "aucun bonus" : u.describe(level))
                + (max ? "\n\nNiveau maximum atteint." : "\nNiveau suivant : " + u.describe(level + 1)
                        + "\n\nCoût : " + gold(cost) + " or du trésor\nTrésor : " + gold(g.getTreasury()) + " or")
                + (chief || max ? "" : "\n\nSeul le chef de la guilde peut l'améliorer.");
        return new Side(u.getLabel(), "Niveau " + level + " / " + u.getMaxLevel(), text, null, null, null,
                !max && chief ? "Améliorer  ·  " + gold(cost) + " or" : null, "Fermer");
    }

    // ===== Actions =====

    @Override
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) { selected = index; }

    @Override
    protected void onTab() { selected = -1; }

    @Override
    protected void onCancel() { selected = -1; }

    @Override
    protected void onConfirm(long amount, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tab == 1) { upgrade(ref, store); return; }
        if (selected >= 0 && selected < open.size()) join(open.get(selected), ref, store);
        else found(ref, store);
    }

    /** Fonder une guilde : memes verifications que /es guildcreate. */
    private void found(Ref<EntityStore> ref, Store<EntityStore> store) {
        String name = inputName, tag = inputTag.toUpperCase();
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (data == null || pr == null) return;
        if (name.length() < 3 || name.length() > 24) { error("Le nom doit faire entre 3 et 24 caractères."); return; }
        if (tag.length() < 2 || tag.length() > 5 || !tag.chars().allMatch(Character::isLetterOrDigit)) { error("Le tag doit faire entre 2 et 5 lettres."); return; }
        if (GuildManager.guildExists(name)) { error("Une guilde porte déjà ce nom."); return; }
        if (GuildManager.tagExists(tag)) { error("Ce tag est déjà utilisé."); return; }
        if (data.getLevel() < FOUND_LEVEL) { error("Niveau " + FOUND_LEVEL + " requis (vous êtes niveau " + data.getLevel() + ")."); return; }
        if (data.getMoney() < FOUND_COST) { error("Il vous manque " + gold(FOUND_COST - data.getMoney()) + " or."); return; }
        if (!data.canJoinGuild()) { error(data.hasGuild() ? "Vous êtes déjà dans une guilde." : "Les nobles ne peuvent pas fonder de guilde."); return; }
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return;
        Guild guild = GuildManager.createGuild(name, tag, pr.getUuid(), pr.getUsername());
        if (guild == null) { error("Nom ou tag invalide."); return; }
        copy.removeMoney(FOUND_COST);
        copy.setGuildRole("CHEF");
        copy.setGuildId(guild.getId());
        store.putComponent(ref, type, copy);
        com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, pr);
        BountyBoard.announce("[Guilde] " + pr.getUsername() + " fonde la guilde " + guild.getFormattedTag() + " " + guild.getName() + " !");
        EldaniorLogger.info("[Boards] guilde " + guild.getTag() + " fondee par " + pr.getUsername());
        ok("Guilde " + guild.getFormattedTag() + " " + guild.getName() + " fondée ! Vous en êtes le chef.");
    }

    /** Rejoindre une guilde en recrutement ouvert (comme /es guild join). */
    private void join(Guild guild, Ref<EntityStore> ref, Store<EntityStore> store) {
        ComponentType<EntityStore, PlayerLevelData> type = EldaniorSystem.get().getPlayerLevelDataType();
        PlayerLevelData data = store.getComponent(ref, type);
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (data == null || pr == null) return;
        if (!guild.isOpenRecruitment()) { error("Cette guilde ne recrute plus ouvertement."); return; }
        if (guild.isFull()) { error("Cette guilde est complète."); return; }
        if (!data.canJoinGuild()) { error(data.hasGuild() ? "Vous êtes déjà dans une guilde." : "Les nobles ne peuvent pas rejoindre de guilde."); return; }
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return;
        copy.setGuildId(guild.getId());
        copy.setGuildRole("MEMBER");
        store.putComponent(ref, type, copy);
        GuildManager.joinGuild(pr.getUuid(), guild);
        com.eldanior.system.titles.TitleManager.checkAndUnlockTitles(ref, store, copy, pr);
        PlayerRef founder = Universe.get().getPlayer(guild.getFounderUUID());
        if (founder != null) founder.sendMessage(com.hypixel.hytale.server.core.Message.raw(pr.getUsername() + " a rejoint votre guilde via le Maître de guilde."));
        ok("Vous avez rejoint la guilde " + guild.getFormattedTag() + " " + guild.getName() + " !");
        selected = -1;
    }

    /** Amelioration choisie, payee par le tresor de la guilde (chef seulement). */
    private void upgrade(Ref<EntityStore> ref, Store<EntityStore> store) {
        Guild g = guild(ref, store);
        if (g == null || selected < 0 || selected >= GuildUpgrade.values().length) return;
        GuildUpgrade u = GuildUpgrade.values()[selected];
        int level = g.getUpgrade(u);
        if (!isChief(g, ref, store)) { error("Seul le chef de la guilde peut l'améliorer."); return; }
        if (level >= u.getMaxLevel()) { error("Niveau maximum atteint."); return; }
        long cost = u.nextCost(level);
        if (!g.withdrawTreasury(cost)) { error("Il manque " + gold(cost - g.getTreasury()) + " or au trésor de la guilde."); return; }
        g.setUpgrade(u, level + 1);
        EldaniorLogger.info("[Boards] guilde " + g.getTag() + " : " + u + " niveau " + (level + 1));
        for (UUID member : g.getMembers()) {
            PlayerRef m = Universe.get().getPlayer(member);
            if (m != null) m.sendMessage(com.hypixel.hytale.server.core.Message.raw("[Guilde] " + u.getLabel() + " niveau " + (level + 1)
                    + " : " + u.describe(level + 1) + "."));
        }
        ok(u.getLabel() + " passe au niveau " + (level + 1) + " : " + u.describe(level + 1) + ".");
    }

    // ===== Outils =====

    private static Guild guild(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        return pr != null ? GuildManager.getPlayerGuild(pr.getUuid()) : null;
    }

    /** Chef : fondateur de la guilde, ou role CHEF. */
    private static boolean isChief(Guild g, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        PlayerLevelData data = data(ref, store);
        return pr != null && (g.getFounderUUID().equals(pr.getUuid()) || (data != null && "CHEF".equals(data.getGuildRole())));
    }
}
