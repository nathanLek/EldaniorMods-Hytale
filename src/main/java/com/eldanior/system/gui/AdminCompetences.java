package com.eldanior.system.gui;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.classes.ClassManager;
import com.eldanior.system.classes.models.ClassModel;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Effects.EffectsManager;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.config.configs.StatConfig;
import com.eldanior.system.skills.SkillManager;
import com.eldanior.system.skills.runes.RuneManager;
import com.eldanior.system.skills.runes.RuneStats;
import com.eldanior.system.skills.skillsInteraction.Metier;
import com.eldanior.system.skills.skillsInteraction.PassiveSkill;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.NameMatching;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

/**
 * Categorie « Competences » du panneau admin (2026-11-24, demande de l'utilisateur : « ameliorer l'interface admin par
 * rapport aux competences, aux effets de statut »). Trois vues :
 *  - Passifs : une carte par passif (serie a rangs, Ange, Peche) ; la fiche donne chaque rang, les classes qui le donnent,
 *    et les gestes (donner a un joueur a un rang, donner la page, retirer, maitriser) ;
 *  - Metiers : donner / retirer un metier, donner son parchemin ;
 *  - Effets : les effets de statut du mod, par dossier (eldanior/effets.json) ; appliquer a soi ou a un joueur, retirer,
 *    tout purger, voir ceux qui sont actifs.
 * Et, depuis Joueurs > Gerer, la carte « Passifs » : les passifs d'un joueur, le detail de ses stats, et les memes gestes.
 *
 * Le joueur vise : le nom ecrit dans le champ « Joueur » ; vide = le joueur gere, sinon l'admin lui-meme.
 */
final class AdminCompetences {

    private AdminCompetences() {}

    static final String C_GREEN = "#00e7b6", C_RED = "#e0574a", C_GOLD = "#f2c45e", C_BLUE = "#6fa8e8", C_GREY = "#56677a",
            C_VIOLET = "#b47be8", C_ORANGE = "#e8a050";

    enum Mode { PASSIFS, METIERS, EFFETS, RUNES }

    // ================================================================================================= effets (index)

    record Effet(String id, String dossier, float duree, boolean negatif, boolean infini, String note) {}

    private static Map<String, List<Effet>> effets;

    /** Effets du mod par dossier (lu une fois dans eldanior/effets.json). */
    static synchronized Map<String, List<Effet>> effets() {
        if (effets != null) return effets;
        Map<String, List<Effet>> out = new LinkedHashMap<>();
        try (InputStream in = AdminCompetences.class.getResourceAsStream("/eldanior/effets.json")) {
            if (in != null) {
                var root = com.google.gson.JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                for (var e : root.getAsJsonObject("dossiers").entrySet()) {
                    List<Effet> list = new ArrayList<>();
                    for (var el : e.getValue().getAsJsonArray()) {
                        var o = el.getAsJsonObject();
                        list.add(new Effet(o.get("id").getAsString(), e.getKey(), o.get("duree").getAsFloat(), o.get("negatif").getAsBoolean(),
                                o.get("infini").getAsBoolean(), o.get("note").getAsString()));
                    }
                    out.put(e.getKey(), list);
                }
            }
        } catch (Exception e) {
            EldaniorLogger.error("[Admin] lecture de eldanior/effets.json", e);
        }
        effets = out;
        return out;
    }

    // ================================================================================================= vues

    static AdminScreen.View view(Mode mode) {
        List<AdminScreen.Card> cards = new ArrayList<>();
        cards.add(nav(Mode.PASSIFS, mode, "P", "Passifs", PassiveSkill.values().length + " passifs (séries à rangs, Anges, Péchés)"));
        cards.add(nav(Mode.METIERS, mode, "M", "Métiers", Metier.values().length + " métiers (accès aux établis)"));
        int nbEffets = 0;
        for (List<Effet> l : effets().values()) nbEffets += l.size();
        cards.add(nav(Mode.EFFETS, mode, "FX", "Effets de statut", nbEffets + " effets du mod, par dossier"));
        cards.add(nav(Mode.RUNES, mode, "R", "Runes actives", RuneStats.catalogue().size() + " runes : lancers, possession, par type"));

        int learned = 0, online = 0;
        for (PlayerRef pr : Universe.get().getPlayers()) {
            PlayerLevelData d = data(pr);
            if (d == null) continue;
            online++;
            learned += d.countLearnedPassives();
        }
        List<AdminScreen.Stat> stats;
        stats = List.of(new AdminScreen.Stat(String.valueOf(PassiveSkill.values().length), "Passifs"),
                new AdminScreen.Stat(String.valueOf(SkillManager.getAllPages().stream().filter(SkillManager.Page::main).count()), "Pages"),
                new AdminScreen.Stat(String.valueOf(nbEffets), "Effets"),
                new AdminScreen.Stat(online > 0 ? String.valueOf(learned / online) : "—", "Passifs appris (moy.)"));

        String section;
        switch (mode) {
            case PASSIFS -> {
                section = "Compétences  »  Passifs";
                for (PassiveSkill p : PassiveSkill.values()) {
                    String ranks = p.isDivine() ? "Divin" : p.getMinRank() == p.getMaxRank() ? PassiveSkill.rankLabel(p.getMinRank())
                            : PassiveSkill.rankLabel(p.getMinRank()) + " → " + PassiveSkill.rankLabel(p.getMaxRank());
                    int owners = 0;
                    for (PlayerRef pr : Universe.get().getPlayers()) { PlayerLevelData d = data(pr); if (d != null && d.hasActivePassive(p)) owners++; }
                    cards.add(new AdminScreen.Card("ps:" + p.name(), SkillManager.pageFor(p.name(), p.isDivine() ? 7 : p.getMaxRank()),
                            p.getDisplayName().substring(0, 1), p.getDisplayName(), p.getTheme().label + "  ·  " + ranks + "\n"
                            + shorten(p.getDescription(p.getMaxRank()), 70), owners > 0 ? owners + " actif(s)" : null, themeColor(p)));
                }
            }
            case METIERS -> {
                section = "Compétences  »  Métiers";
                for (Metier m : Metier.values()) {
                    int owners = 0;
                    for (PlayerRef pr : Universe.get().getPlayers()) { PlayerLevelData d = data(pr); if (d != null && d.hasMetier(m)) owners++; }
                    cards.add(new AdminScreen.Card("mt:" + m.name(), m.getScrollItem(), "M", m.getDisplayName(), m.getDescription(),
                            owners > 0 ? owners + " joueur(s)" : null, C_ORANGE));
                }
            }
            case RUNES -> {
                section = "Compétences  »  Runes actives";
                Census c = census();
                Map<String, List<RuneStats.Info>> byType = new TreeMap<>();
                for (RuneStats.Info i : RuneStats.catalogue()) byType.computeIfAbsent(i.type(), t -> new ArrayList<>()).add(i);
                for (var e : byType.entrySet()) {
                    long casts = 0; int owned = 0, equipped = 0;
                    for (RuneStats.Info i : e.getValue()) {
                        casts += RuneStats.casts(i);
                        owned += c.owned.getOrDefault(i.id(), 0);
                        equipped += c.equipped.getOrDefault(i.id(), 0);
                    }
                    cards.add(new AdminScreen.Card("rt:" + e.getKey(), null, e.getKey().substring(0, 1), e.getKey(),
                            e.getValue().size() + " rune(s)  ·  " + owned + " possédée(s), " + equipped + " équipée(s) par les connectés",
                            casts + " lancer(s)", typeColor(e.getKey())));
                }
                stats = List.of(new AdminScreen.Stat(String.valueOf(RuneStats.catalogue().size()), "Runes"),
                        new AdminScreen.Stat(String.valueOf(totalRuneCasts()), "Lancers comptés"),
                        new AdminScreen.Stat(String.valueOf(c.owned.size()), "Runes possédées (connectés)"),
                        new AdminScreen.Stat(String.valueOf(c.equipped.size()), "Runes équipées (connectés)"));
            }
            default -> {
                section = "Compétences  »  Effets de statut";
                for (var e : effets().entrySet()) {
                    long neg = e.getValue().stream().filter(Effet::negatif).count();
                    cards.add(new AdminScreen.Card("fx:" + e.getKey(), null, "FX", e.getKey(),
                            e.getValue().size() + " effet(s)" + (neg > 0 ? "  ·  " + neg + " négatif(s)" : ""), String.valueOf(e.getValue().size()),
                            neg > 0 ? C_RED : C_VIOLET));
                }
            }
        }
        AdminScreen.Detail intro = new AdminScreen.Detail();
        intro.mark = "C";
        intro.kicker = "Administration";
        intro.title = "Compétences";
        intro.tagline = "Passifs, métiers et effets de statut.";
        intro.text = "Choisissez une vue dans les trois premières cartes, puis une carte pour sa fiche. Les gestes visent le joueur écrit dans le "
                + "champ « Joueur » (vide : vous-même). Pour un joueur précis : Joueurs › Gérer › Passifs.";
        intro.note("Passifs : " + PassiveSkill.values().length + " (" + countSeries() + " séries de rang Commun à Légendaire, 7 Anges, 7 Péchés).");
        intro.note("Limite : " + PlayerLevelData.MAX_LEARNED_PASSIVES + " passifs appris par joueur (les métiers et les passifs de classe n'y comptent pas).");
        return new AdminScreen.View(section, "Choisissez une carte", "Rien à afficher.", cards, stats, intro);
    }

    private static AdminScreen.Card nav(Mode m, Mode current, String mark, String name, String meta) {
        return new AdminScreen.Card("nav:sk:" + m.name(), null, mark, (m == current ? "» " : "") + name, meta, null, m == current ? C_GOLD : C_GREY);
    }

    private static int countSeries() {
        int n = 0;
        for (PassiveSkill p : PassiveSkill.values()) if (!p.isDivine()) n++;
        return n;
    }

    // ================================================================================================= fiches

    static AdminScreen.Detail detail(String key) {
        if (key.startsWith("ps:")) return passiveDetail(PassiveSkill.fromName(key.substring(3)));
        if (key.startsWith("mt:")) return metierDetail(Metier.fromName(key.substring(3)));
        if (key.startsWith("fx:")) return effectDetail(key.substring(3));
        if (key.startsWith("rt:")) return runeTypeDetail(key.substring(3));
        return null;
    }

    private static AdminScreen.Detail passiveDetail(PassiveSkill p) {
        if (p == null) return null;
        AdminScreen.Detail d = new AdminScreen.Detail();
        d.icon = SkillManager.pageFor(p.name(), p.isDivine() ? 7 : p.getMaxRank());
        d.mark = p.getDisplayName().substring(0, 1);
        d.kicker = "Passif  ·  " + p.getTheme().label;
        d.title = p.getDisplayName();
        if (p.isDivine()) {
            d.tagline = "Divin  ·  " + (p.isPeche() ? "réservé aux PK" : "réservé aux Religieux de l'Église et au-dessus") + "  ·  un seul à la fois";
            d.text = p.getDescription(7);
            if (p.getCooldownSeconds() > 0) d.fact("Recharge", (int) (p.getCooldownSeconds() / 3600) + " h", C_ORANGE);
        } else {
            d.tagline = "Rangs " + PassiveSkill.rankLabel(p.getMinRank()) + " à " + PassiveSkill.rankLabel(p.getMaxRank()) + "  ·  maîtrisé : effet +10 %";
            for (int r = p.getMinRank(); r <= p.getMaxRank(); r++) {
                if (!p.hasRank(r)) continue;
                d.note(PassiveSkill.rankLabel(r) + (p.getRankName(r).equals(p.getDisplayName()) ? "" : " (" + p.getRankName(r) + ")") + " : " + p.getDescription(r));
            }
        }
        List<String> classes = new ArrayList<>();
        for (ClassModel c : ClassManager.getAll()) if (c.passiveRank(p) > 0) classes.add(c.getDisplayName() + " " + PassiveSkill.rankLabel(c.passiveRank(p)).charAt(0));
        d.fact("Identifiant", p.name());
        d.fact("Classes", String.valueOf(classes.size()), classes.isEmpty() ? null : C_BLUE);
        long pages = SkillManager.getAllPages().stream().filter(pg -> pg.main() && pg.skillId().equals(p.name())).count();
        d.fact("Pages", String.valueOf(pages));
        int owners = 0;
        for (PlayerRef pr : Universe.get().getPlayers()) { PlayerLevelData pd = data(pr); if (pd != null && pd.hasActivePassive(p)) owners++; }
        d.fact("Actif chez", owners + " joueur(s)");
        if (!classes.isEmpty()) d.note("Donné par : " + shorten(String.join(", ", classes), 300));
        if (!p.isDivine()) { d.n0 = "Rang (1 Commun à 6 Légendaire)"; d.v0 = (long) p.getMaxRank(); }
        d.t1 = "Joueur (vide : vous)";
        d.s1 = "";
        d.btn(p.isDivine() ? "Donner le passif" : "Donner le passif au rang du champ", "skgive:" + p.name());
        d.btn("Donner la page (inventaire)", "skpage:" + p.name());
        d.btn("Maîtriser (100 %)", "skmax:" + p.name());
        d.btn("Remettre la maîtrise à zéro", "skzero:" + p.name());
        d.danger("Retirer le passif", "skdel:" + p.name());
        return d;
    }

    private static AdminScreen.Detail metierDetail(Metier m) {
        if (m == null) return null;
        AdminScreen.Detail d = new AdminScreen.Detail();
        d.icon = m.getScrollItem();
        d.mark = "M";
        d.kicker = "Métier";
        d.title = m.getDisplayName();
        d.tagline = m.getDescription();
        d.text = "Un métier ne compte pas dans la limite de passifs. Il progresse en fabriquant (paliers d'artisanat).";
        List<String> classes = new ArrayList<>();
        for (ClassModel c : ClassManager.getAll()) if (c.getMetierIds().contains(m.name())) classes.add(c.getDisplayName());
        d.fact("Identifiant", m.name());
        d.fact("Parchemin", m.getScrollItem());
        d.fact("Classes", String.valueOf(classes.size()));
        if (!classes.isEmpty()) d.note("Donné par : " + shorten(String.join(", ", classes), 300));
        d.t1 = "Joueur (vide : vous)";
        d.s1 = "";
        d.btn("Donner le métier", "mtgive:" + m.name());
        d.btn("Donner le parchemin", "mtpage:" + m.name());
        d.danger("Retirer le métier", "mtdel:" + m.name());
        return d;
    }

    private static AdminScreen.Detail effectDetail(String dossier) {
        List<Effet> list = effets().get(dossier);
        if (list == null) return null;
        AdminScreen.Detail d = new AdminScreen.Detail();
        d.mark = "FX";
        d.kicker = "Effets de statut";
        d.title = dossier;
        d.tagline = list.size() + " effet(s). Écrivez l'identifiant dans le champ « Effet ».";
        StringBuilder sb = new StringBuilder();
        for (Effet e : list) {
            if (sb.length() > 0) sb.append(",  ");
            sb.append(e.id()).append(e.infini() ? " (∞)" : e.duree() > 0 ? " (" + fmt(e.duree()) + " s)" : "").append(e.negatif() ? " −" : "");
        }
        d.text = sb.toString();
        for (Effet e : list) if (!e.note().isBlank()) d.note(e.id() + " : " + e.note());
        d.n0 = "Durée en secondes (0 : celle de l'effet)";
        d.v0 = 0L;
        d.t0 = "Effet (identifiant)";
        d.s0 = list.get(0).id();
        d.t1 = "Joueur (vide : vous)";
        d.s1 = "";
        d.btn("Appliquer", "fxapply");
        d.btn("Retirer cet effet", "fxremove");
        d.btn("Voir ses effets actifs", "fxlist");
        d.btn("Retirer les effets négatifs", "fxcure");
        d.danger("Retirer tous ses effets", "fxclear");
        return d;
    }

    // ================================================================================================= runes actives

    /** Runes possedees / equipees par les joueurs connectes (rune -> nombre de joueurs). */
    private static final class Census {
        final Map<String, Integer> owned = new HashMap<>(), equipped = new HashMap<>();
    }

    private static Census census() {
        Census c = new Census();
        for (PlayerRef pr : Universe.get().getPlayers()) {
            Ref<EntityStore> ref = pr.getReference();
            if (ref == null || !ref.isValid()) continue;
            try {
                for (String id : RuneManager.ownedRunes(ref, ref.getStore())) c.owned.merge(id, 1, Integer::sum);
                for (String id : new HashSet<>(RuneManager.equippedRunes(ref, ref.getStore()))) c.equipped.merge(id, 1, Integer::sum);
            } catch (Exception e) { EldaniorLogger.error("[Admin] runes de " + pr.getUsername(), e); }
        }
        return c;
    }

    private static long totalRuneCasts() {
        long n = 0;
        for (RuneStats.Info i : RuneStats.catalogue()) n += RuneStats.casts(i);
        return n;
    }

    private static AdminScreen.Detail runeTypeDetail(String type) {
        List<RuneStats.Info> list = new ArrayList<>();
        for (RuneStats.Info i : RuneStats.catalogue()) if (i.type().equals(type)) list.add(i);
        if (list.isEmpty()) return null;
        Census c = census();
        list.sort(Comparator.comparingLong((RuneStats.Info i) -> -RuneStats.casts(i)).thenComparing(RuneStats.Info::name));
        AdminScreen.Detail d = new AdminScreen.Detail();
        d.mark = type.substring(0, 1);
        d.kicker = "Runes actives";
        d.title = type;
        long casts = 0;
        for (RuneStats.Info i : list) casts += RuneStats.casts(i);
        d.tagline = list.size() + " rune(s)  ·  " + casts + " lancer(s) depuis le " + date(RuneStats.since());
        Map<String, Integer> rar = new TreeMap<>();
        for (RuneStats.Info i : list) rar.merge(i.rarity(), 1, Integer::sum);
        int k = 0;
        for (var e : rar.entrySet()) if (k++ < 6) d.fact(e.getKey(), String.valueOf(e.getValue()));
        int shown = 0;
        for (RuneStats.Info i : list) {
            if (shown++ >= 10) break;
            d.note(i.name() + " (" + i.rarity() + ") : " + RuneStats.casts(i) + " lancer(s), possédée par " + c.owned.getOrDefault(i.id(), 0)
                    + ", équipée par " + c.equipped.getOrDefault(i.id(), 0) + (RuneStats.lastCast(i.spell()) > 0 ? ", dernier " + date(RuneStats.lastCast(i.spell())) : ""));
        }
        List<String> never = new ArrayList<>();
        for (RuneStats.Info i : list) if (RuneStats.casts(i) == 0) never.add(i.name());
        d.text = never.isEmpty() ? "Toutes les runes de ce type ont été lancées au moins une fois."
                : "Jamais lancées : " + shorten(String.join(", ", never), 600) + ".";
        d.t0 = "Rune (identifiant ou nom)";
        d.s0 = list.get(0).id();
        d.t1 = "Joueur (vide : vous)";
        d.s1 = "";
        d.btn("Donner la rune", "rngive");
        d.btn("Donner son manuel", "rnbook");
        d.danger("Remettre tous les compteurs de lancers à zéro", "rnreset");
        return d;
    }

    /** Carte « Runes » de Joueurs > Gerer : ses runes, ses lancers, sa rune favorite. */
    static AdminScreen.Detail playerRunes(String name, PlayerRef pr) {
        AdminScreen.Detail out = new AdminScreen.Detail();
        out.mark = "R";
        out.kicker = "Gérer " + name;
        out.title = "Runes actives";
        Ref<EntityStore> ref = pr != null ? pr.getReference() : null;
        if (ref == null || !ref.isValid()) { out.tagline = "Hors ligne."; return out; }
        Set<String> owned = RuneManager.ownedRunes(ref, ref.getStore());
        List<String> equipped = RuneManager.equippedRunes(ref, ref.getStore());
        Map<String, Long> casts = RuneStats.playerCasts(pr.getUuid());
        long total = 0;
        String fav = null; long favN = 0;
        for (var e : casts.entrySet()) {
            RuneStats.Info i = RuneStats.bySpell(e.getKey());
            if (i == null) continue;
            total += e.getValue();
            if (e.getValue() > favN) { favN = e.getValue(); fav = i.name(); }
        }
        out.tagline = owned.size() + " rune(s) possédée(s)  ·  " + equipped.size() + " équipée(s)  ·  " + total + " lancer(s)";
        out.fact("Possédées", String.valueOf(owned.size()), C_BLUE);
        out.fact("Équipées", String.valueOf(equipped.size()), C_GREEN);
        out.fact("Lancers", String.valueOf(total), C_GOLD);
        out.fact("Favorite", fav != null ? fav + " (" + favN + ")" : "—");
        Map<String, Integer> byType = new TreeMap<>();
        for (String id : owned) { RuneStats.Info i = RuneStats.byId(id); byType.merge(i != null ? i.type() : "?", 1, Integer::sum); }
        List<String> types = new ArrayList<>();
        for (var e : byType.entrySet()) types.add(e.getKey() + " " + e.getValue());
        out.text = "Par type : " + (types.isEmpty() ? "aucune" : String.join(", ", types)) + ".";
        for (String id : equipped) {
            RuneStats.Info i = RuneStats.byId(id);
            out.note("Équipée : " + (i != null ? i.name() + " (" + i.type() + ", " + i.rarity() + ") : " + RuneStats.casts(pr.getUuid(), i) + " lancer(s)" : id));
        }
        out.t0 = "Rune (identifiant ou nom)";
        out.s0 = "";
        out.btn("Donner la rune", "prngive");
        out.btn("Donner son manuel", "prnbook");
        return out;
    }

    static RuneStats.Info findRune(String text) {
        if (text == null || text.isBlank()) return null;
        RuneStats.Info i = RuneStats.byId(text.trim());
        if (i != null) return i;
        String n = norm(text);
        for (RuneStats.Info x : RuneStats.catalogue()) if (norm(x.name()).equals(n) || norm(x.id()).equals(n)) return x;
        for (RuneStats.Info x : RuneStats.catalogue()) if (norm(x.name()).contains(n)) return x;
        return null;
    }

    private static String typeColor(String type) {
        return switch (type) {
            case "Feu" -> "#e8603c";
            case "Glace" -> "#8fd0f0";
            case "Foudre" -> "#f2d65e";
            case "Terre" -> "#b08a4a";
            case "Air" -> "#b8e8d0";
            case "Lumiere" -> "#f6eaa0";
            case "Tenebres" -> "#8a5ab8";
            case "Temps" -> "#d0b070";
            case "Espace", "Spatial" -> "#6f8ee8";
            case "Luo" -> "#e05050";
            default -> C_GREY;
        };
    }

    private static String date(long ms) {
        return new java.text.SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(new Date(ms));
    }

    /** Carte « Passifs » de Joueurs > Gerer : ses passifs, le detail de ses stats, les gestes. */
    static AdminScreen.Detail playerPassives(String name, PlayerLevelData d) {
        AdminScreen.Detail out = new AdminScreen.Detail();
        out.mark = "P";
        out.kicker = "Gérer " + name;
        out.title = "Passifs et métiers";
        List<PlayerLevelData.ActivePassive> act = d.getActivePassiveEntries();
        out.tagline = d.countLearnedPassives() + " / " + PlayerLevelData.MAX_LEARNED_PASSIVES + " appris  ·  " + act.size() + " actif(s)"
                + (d.hasDivineSkill() ? "  ·  divin : " + d.getDivineSkill() : "");
        ClassModel cls = ClassManager.get(d.getPlayerClassId());
        try {
            out.fact("Critique", fmt(StatConfig.LUCK_CRITICAL.getFinalValue(d, cls)) + " %", C_GOLD);
            out.fact("Esquive", fmt(StatConfig.DODGE_CHANCE.getFinalValue(d, cls)) + " %");
            out.fact("Défense", fmt(StatConfig.ENDURANCE_DEFENSE.getFinalValue(d, cls)) + " %");
            out.fact("Force", "+" + fmt(StatConfig.STRENGTH_DAMAGE.getFinalValue(d, cls)) + " %");
            out.fact("Butin", fmt(StatConfig.LUCK_LOOT.getFinalValue(d, cls)) + " %");
            out.fact("Vitesse", "×" + fmt(StatConfig.AGILITY_SPEED.getFinalValue(d, cls)));
            out.fact("Menaces", fmt(StatConfig.THREAT_AWARENESS.getFinalValue(d, cls)) + " blocs");
        } catch (Exception e) { EldaniorLogger.error("[Admin] stats de " + name, e); }
        List<PlayerLevelData.ActivePassive> sorted = new ArrayList<>(act);
        sorted.sort(Comparator.comparingInt(a -> -a.rank()));
        int shown = 0;
        for (PlayerLevelData.ActivePassive a : sorted) {
            if (shown == 9 && sorted.size() > 10) { out.note("… et " + (sorted.size() - 9) + " autre(s)."); break; }
            out.note(a.skill().getDisplayName() + " · " + PassiveSkill.rankLabel(a.rank())
                    + (d.getLearnedRank(a.skill().name()) == 0 ? " (classe)" : "") + " · maîtrise "
                    + (d.isSkillMastered(a.skill().name()) ? "MAX" : fmt(d.getSkillProgression(a.skill().name())) + " %"));
            shown++;
        }
        List<String> metiers = new ArrayList<>();
        for (Metier m : Metier.values()) if (d.hasMetier(m)) metiers.add(m.getDisplayName());
        out.text = "Métiers : " + (metiers.isEmpty() ? "aucun" : String.join(", ", metiers)) + ".";
        out.t0 = "Passif ou métier (identifiant ou nom)";
        out.s0 = "";
        out.n0 = "Rang (1 à 6)";
        out.v0 = 1L;
        out.btn("Donner (au rang du champ)", "pgive");
        out.btn("Maîtriser (100 %)", "pmax");
        out.danger("Retirer", "pdel");
        out.danger("Oublier tous ses passifs appris", "pforget");
        return out;
    }

    // ================================================================================================= gestes

    /** Resultat d'un geste : message, et s'il a reussi. */
    record Result(boolean ok, String message) {
        static Result ok(String m) { return new Result(true, m); }
        static Result err(String m) { return new Result(false, m); }
    }

    /**
     * Execute un geste de la categorie. target = joueur vise (deja resolu : champ, joueur gere ou admin).
     * k = cle du geste, arg = argument de la cle, n0 = rang ou duree, t0 = identifiant ecrit.
     */
    static Result act(String k, String arg, PlayerRef target, long n0, String t0) {
        if (target == null) return Result.err("Joueur introuvable (déconnecté ?).");
        Ref<EntityStore> ref = target.getReference();
        if (ref == null || !ref.isValid()) return Result.err("Joueur introuvable (déconnecté ?).");
        Store<EntityStore> store = ref.getStore();
        PlayerLevelData d = store.getComponent(ref, EldaniorSystem.get().getPlayerLevelDataType());
        if (d == null) return Result.err("Données du joueur indisponibles.");
        String who = target.getUsername();

        switch (k) {
            case "pgive", "pmax", "pdel" -> {
                Metier m = findMetier(t0);
                PassiveSkill p = m == null ? findPassive(t0) : null;
                if (m == null && p == null) return Result.err("Passif ou métier inconnu : « " + t0 + " ».");
                if (m != null) return act("pgive".equals(k) ? "mtgive" : "pdel".equals(k) ? "mtdel" : "mtnone", m.name(), target, n0, t0);
                return act("pgive".equals(k) ? "skgive" : "pmax".equals(k) ? "skmax" : "skdel", p.name(), target, n0, t0);
            }
            case "pforget" -> {
                List<String> ids = new ArrayList<>();
                for (String id : d.getUnlockedSkills()) if (PassiveSkill.fromName(id) != null) ids.add(id);
                for (String id : ids) d.forgetPassive(id);
                save(ref, store, d);
                return Result.ok(who + " a oublié " + ids.size() + " passif(s) appris.");
            }
            case "skgive" -> {
                PassiveSkill p = PassiveSkill.fromName(arg);
                if (p == null) return Result.err("Passif inconnu.");
                if (p.isDivine() && d.hasDivineSkill() && !d.getDivineSkill().equals(p.name())) {
                    d.forgetPassive(d.getDivineSkill());                                    // un seul pouvoir divin
                }
                int rank = p.isDivine() ? PassiveSkill.DIVINE_RANK : (int) Math.max(1, Math.min(6, n0));
                var res = d.learnPassive(p, rank);
                if (res == PlayerLevelData.LearnResult.LIMIT) return Result.err(who + " a déjà " + PlayerLevelData.MAX_LEARNED_PASSIVES + " passifs appris.");
                if (p.isDivine()) d.setDivineSkill(p.name());
                save(ref, store, d);
                return Result.ok(switch (res) {
                    case ALREADY -> who + " a déjà " + p.getDisplayName() + " à ce rang ou plus haut.";
                    case UPGRADED -> p.getDisplayName() + " passe au rang " + PassiveSkill.rankLabel(p.clamp(rank)) + " pour " + who + ".";
                    default -> p.getDisplayName() + (p.isDivine() ? "" : " (" + PassiveSkill.rankLabel(p.clamp(rank)) + ")") + " donné à " + who + ".";
                });
            }
            case "skpage" -> {
                PassiveSkill p = PassiveSkill.fromName(arg);
                if (p == null) return Result.err("Passif inconnu.");
                int rank = p.isDivine() ? 7 : p.clamp((int) Math.max(1, Math.min(6, n0)));
                String page = SkillManager.pageFor(p.name(), rank);
                if (page == null) return Result.err("Pas de page pour ce rang.");
                return give(store, ref, page, who);
            }
            case "skmax", "skzero" -> {
                PassiveSkill p = PassiveSkill.fromName(arg);
                if (p == null) return Result.err("Passif inconnu.");
                d.setSkillProcs(p.name(), "skmax".equals(k) ? Integer.MAX_VALUE : 0);
                save(ref, store, d);
                return Result.ok(p.getDisplayName() + " : maîtrise " + ("skmax".equals(k) ? "complète" : "remise à zéro") + " pour " + who + ".");
            }
            case "skdel" -> {
                PassiveSkill p = PassiveSkill.fromName(arg);
                if (p == null) return Result.err("Passif inconnu.");
                if (!d.forgetPassive(p.name())) return Result.err(who + " n'a pas appris " + p.getDisplayName() + " (un passif de classe ne se retire pas).");
                save(ref, store, d);
                return Result.ok(p.getDisplayName() + " retiré à " + who + ".");
            }
            case "mtgive" -> {
                Metier m = Metier.fromName(arg);
                if (m == null) return Result.err("Métier inconnu.");
                if (d.hasMetier(m)) return Result.err(who + " connaît déjà ce métier.");
                d.learnSkill(m.name());
                save(ref, store, d);
                return Result.ok("Métier " + m.getDisplayName() + " donné à " + who + ".");
            }
            case "mtdel" -> {
                Metier m = Metier.fromName(arg);
                if (m == null) return Result.err("Métier inconnu.");
                if (!d.getUnlockedSkills().contains(m.name())) return Result.err(who + " n'a pas appris ce métier (ou sa classe le donne).");
                d.removeSkill(m.name());
                save(ref, store, d);
                return Result.ok("Métier " + m.getDisplayName() + " retiré à " + who + ".");
            }
            case "mtpage" -> {
                String scroll = SkillManager.scrollFor(arg);
                return scroll != null ? give(store, ref, scroll, who) : Result.err("Métier inconnu.");
            }
            case "mtnone" -> { return Result.err("Un métier n'a pas de maîtrise à régler ici."); }
            case "rngive", "prngive" -> {
                RuneStats.Info i = findRune(t0);
                if (i == null) return Result.err("Rune inconnue : « " + t0 + " ».");
                return RuneManager.giveRune(ref, store, i.id()) ? Result.ok(i.name() + " ajoutée au sac de runes de " + who + ".")
                        : Result.err("Sac de runes de " + who + " plein.");
            }
            case "rnbook", "prnbook" -> {
                RuneStats.Info i = findRune(t0);
                if (i == null) return Result.err("Rune inconnue : « " + t0 + " ».");
                String book = com.eldanior.system.skills.runes.spells.MagicManuals.bookFor(i.id());
                return book != null ? give(store, ref, book, who) : Result.err(i.name() + " n'a pas de manuel.");
            }
            case "fxapply" -> {
                String id = t0 == null ? "" : t0.trim();
                if (EffectsManager.getEffectAsset(id) == null) return Result.err("Effet inconnu : « " + id + " ».");
                if (n0 > 0) EffectsManager.applyCustomEffect(ref, id, n0, OverlapBehavior.OVERWRITE, store);
                else if (!EffectsManager.applyEffect(ref, id, store)) return Result.err("L'effet n'a pas pu être appliqué.");
                return Result.ok(id + " appliqué à " + who + (n0 > 0 ? " pour " + n0 + " s." : "."));
            }
            case "fxremove" -> {
                String id = t0 == null ? "" : t0.trim();
                if (!EffectsManager.hasEffect(ref, id, store)) return Result.err(who + " n'a pas l'effet « " + id + " ».");
                EffectsManager.removeEffect(ref, id, store);
                return Result.ok(id + " retiré à " + who + ".");
            }
            case "fxcure" -> { EffectsManager.cureAllDebuffs(ref, store); return Result.ok("Effets négatifs retirés à " + who + "."); }
            case "fxclear" -> { EffectsManager.clearAllEffects(ref, store); return Result.ok("Tous les effets de " + who + " sont retirés."); }
            case "fxlist" -> {
                int[] total = {0};
                EffectsManager.forEachActiveEffect(ref, store, (i, a) -> total[0]++);
                List<String> known = new ArrayList<>();
                for (List<Effet> l : effets().values()) for (Effet e : l) if (EffectsManager.hasEffect(ref, e.id(), store)) known.add(e.id());
                return Result.ok(who + " : " + total[0] + " effet(s) actif(s)" + (known.isEmpty() ? "" : " dont " + String.join(", ", known))
                        + (total[0] > known.size() ? " (et " + (total[0] - known.size()) + " du jeu de base)" : "") + ".");
            }
            default -> { return Result.err("Geste inconnu."); }
        }
    }

    // ================================================================================================= outils

    /** Joueur vise : le nom du champ, sinon le joueur gere, sinon l'admin. */
    static PlayerRef target(String typed, String managed, Ref<EntityStore> adminRef, Store<EntityStore> adminStore) {
        String name = typed != null && !typed.isBlank() ? typed.trim() : managed;
        if (name != null) return Universe.get().getPlayerByUsername(name, NameMatching.EXACT_IGNORE_CASE);
        return adminStore.getComponent(adminRef, PlayerRef.getComponentType());
    }

    private static void save(Ref<EntityStore> ref, Store<EntityStore> store, PlayerLevelData d) {
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), d);
        try { com.eldanior.system.Leveling.utils.StatCalculator.updatePlayerStats(ref, store, d); } catch (Exception e) { EldaniorLogger.error("[Admin] stats", e); }
    }

    private static Result give(Store<EntityStore> store, Ref<EntityStore> ref, String itemId, String who) {
        Player p = store.getComponent(ref, Player.getComponentType());
        if (p == null) return Result.err("Joueur introuvable.");
        var res = p.getInventory().getCombinedHotbarFirst().addItemStack(new ItemStack(itemId, 1));
        return res.succeeded() ? Result.ok(itemId + " donné à " + who + ".") : Result.err("Inventaire de " + who + " plein.");
    }

    private static PlayerLevelData data(PlayerRef pr) {
        try {
            var r = pr != null ? pr.getReference() : null;
            return r != null && r.isValid() ? r.getStore().getComponent(r, EldaniorSystem.get().getPlayerLevelDataType()) : null;
        } catch (Exception e) { return null; }
    }

    static PassiveSkill findPassive(String text) {
        if (text == null || text.isBlank()) return null;
        PassiveSkill p = PassiveSkill.fromName(text.trim().replace(' ', '_'));
        if (p != null) return p;
        String n = norm(text);
        for (PassiveSkill s : PassiveSkill.values()) if (norm(s.getDisplayName()).equals(n)) return s;
        for (PassiveSkill s : PassiveSkill.values()) if (norm(s.getDisplayName()).contains(n)) return s;
        var old = com.eldanior.system.skills.skillsInteraction.PassiveMigration.of(text.trim());
        return old != null ? PassiveSkill.fromName(old.skillId()) : null;
    }

    static Metier findMetier(String text) {
        if (text == null || text.isBlank()) return null;
        Metier m = Metier.fromName(text.trim());
        if (m != null) return m;
        String n = norm(text);
        for (Metier x : Metier.values()) if (norm(x.getDisplayName()).equals(n)) return x;
        return null;
    }

    private static String norm(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    private static String themeColor(PassiveSkill p) {
        return switch (p.getTheme()) {
            case ATTAQUE -> C_RED;
            case MAGIE -> C_VIOLET;
            case DEFENSE -> C_BLUE;
            case VIE, ENDURANCE -> C_GREEN;
            case AGILITE -> "#7fd0d0";
            case CHANCE -> C_GOLD;
            case PECHE -> "#a0304a";
            case ANGE -> "#e8e0b0";
        };
    }

    private static String fmt(float v) {
        return v == Math.floor(v) ? String.valueOf((int) v) : String.format(Locale.FRANCE, "%.1f", v);
    }

    private static String shorten(String s, int max) { return s == null || s.length() <= max ? s : s.substring(0, max - 1) + "…"; }
}
