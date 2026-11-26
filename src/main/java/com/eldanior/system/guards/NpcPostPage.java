package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Fenetre d'un poste de PNJ (bloc Eldanior_Poste_PNJ, Npc/NpcPost.ui) : type de PNJ, specialite (classe d'aventurier, de
 * chasseur ou de criminel ; boutique du marchand Luo), statut ou rang, niveau, regard. « Poser » remplace le PNJ du poste.
 */
public class NpcPostPage extends InteractiveCustomUIPage<NpcPostPage.PostEventData> {

    private static final String RANDOM = "hasard";
    private final String key;

    public NpcPostPage(@Nonnull PlayerRef playerRef, String key) {
        super(playerRef, CustomPageLifetime.CanDismiss, PostEventData.CODEC);
        this.key = key;
    }

    private static DropdownEntryInfo entry(String label, String value) {
        return new DropdownEntryInfo(LocalizableString.fromString(label), value);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Npc/NpcPost.ui");
        GuardManager.Post p = NpcPostBlocks.postOf(key);

        String cat = p != null ? category(p.type) : "garde";
        List<DropdownEntryInfo> cats = new ArrayList<>();
        for (String[] c : CATEGORIES) cats.add(entry(c[1], c[0]));
        ui.set("#PostCategory.Entries", cats);
        ui.set("#PostCategory.Value", cat);
        List<DropdownEntryInfo> types = types(cat);
        // Statut commun (2026-11-16) : Apprenti, Expert, Maitre pour tous les PNJ (NpcStatut le traduit pour chaque type)
        List<DropdownEntryInfo> tiers = new ArrayList<>(List.of(entry("Au hasard", RANDOM)));
        tiers.add(entry("Apprenti  (aventurier ordinaire · 1 étoile)", NpcStatut.APPRENTI.arg));
        tiers.add(entry("Expert  (exceptionnel, négociant, compagnon · 2 étoiles)", NpcStatut.EXPERT.arg));
        tiers.add(entry("Maître  (Héros, Grand Marchand, maître · 3 étoiles)", NpcStatut.MAITRE.arg));
        List<DropdownEntryInfo> facings = List.of(entry("Vers moi", "moi"), entry("Nord", "nord"), entry("Est", "est"),
                entry("Sud", "sud"), entry("Ouest", "ouest"));

        List<DropdownEntryInfo> shops = new ArrayList<>(List.of(entry("Aucun commerce", ""), entry("Un peu de tout (assortiment ambulant)", LuoMerchants.AMBULANT)));
        for (NpcTrades.Metier m : NpcTrades.Metier.values()) shops.add(entry(m.getLabel() + " · " + m.pitch, NpcTrades.value(m)));
        for (LuoMerchants.Shop s : LuoMerchants.Shop.values())
            for (LuoMerchants.Rank r : LuoMerchants.Rank.values())
                shops.add(entry(s.getLabel() + " du Clan Luo · " + r.getLabel(), s.name() + ":" + r.name()));
        // Quetes (2026-11-13) : active les quetes en chapitres sur ce PNJ (le Sensei les a toujours)
        ui.set("#PostQuest.Entries", List.of(entry("Désactivées", ""), entry("Activées (fenêtre des quêtes, carte « + » pour les admins)", NpcServices.CHAPTERS)));
        ui.set("#PostQuest.Value", p != null && (p.quest != null && p.quest.startsWith(NpcServices.CHAPTERS) || p.type == GuardType.SENSEI) ? NpcServices.CHAPTERS : "");
        ui.set("#PostShop.Entries", shops);
        ui.set("#PostShop.Value", p != null && p.shop != null && p.type != GuardType.MARCHAND && p.type != GuardType.COMMERCANT
                && p.type != GuardType.LEGENDAIRE ? p.shop : "");
        ui.set("#PostLines.Value", p != null ? String.join(" | ", NpcServices.lines(p)) : "");
        ui.set("#PostType.Entries", types);
        ui.set("#PostSpec.Entries", specs(p != null ? p.type.getArg() : GuardType.VILLE.getArg()));   // triee selon le PNJ
        ui.set("#PostTier.Entries", tiers);
        ui.set("#PostFacing.Entries", facings);
        ui.set("#PostType.Value", p != null ? p.type.getArg() : GuardType.VILLE.getArg());
        ui.set("#PostSpec.Value", p == null ? RANDOM : p.type == GuardType.LEGENDAIRE ? "legende_" + LegendarySmiths.of(p).arg()
                : p.type == GuardType.COMMERCANT && NpcTrades.metier(p) != null ? NpcTrades.value(NpcTrades.metier(p))
                : p.type == GuardType.MARCHAND ? LuoMerchants.shopOf(p).arg
                : usesClass(p.type) ? AdventurerType.of(p.skinSeed).name().toLowerCase() : RANDOM);
        NpcStatut statut = NpcStatut.current(p);
        ui.set("#PostTier.Value", statut != null ? statut.arg : RANDOM);
        ui.set("#PostFacing.Value", "moi");
        List<DropdownEntryInfo> families = new ArrayList<>(List.of(entry("Celle du territoire", ""), entry("Neutre (aucune famille)", GuardManager.NEUTRAL)));
        for (var f : com.eldanior.system.titles.nobility.family.FamilyManager.getAll())   // clans : tires au hasard dans l'empire
            if (!com.eldanior.system.luo.LuoClans.isClan(f.getId())) families.add(entry("Famille " + f.getDisplayName(), f.getId()));
        ui.set("#PostFamily.Entries", families);
        ui.set("#PostFamily.Value", p != null && p.allegiance != null ? p.allegiance : "");
        ui.set("#PostLevel.Value", p != null ? p.level : 0);
        var star = p == null ? starNear(key) : null;                                   // monstre etoile pose ici (2026-11-16)
        var boss = p == null && star == null ? bossNear(key) : null;                   // boss de donjon pose ici
        if (star != null) {
            ui.set("#PostCategory.Value", MONSTERS);
            ui.set("#PostType.Entries", types(MONSTERS));
            ui.set("#PostType.Value", CONTRACT);
            ui.set("#PostSpec.Entries", specs(CONTRACT));
            ui.set("#PostSpec.Value", star.role() != null ? STAR_PREFIX + star.role() : RANDOM);
            ui.set("#PostTier.Value", star.stars() >= 3 ? NpcStatut.MAITRE.arg : star.stars() == 2 ? NpcStatut.EXPERT.arg : NpcStatut.APPRENTI.arg);
            ui.set("#PostLevel.Value", star.level());
            ui.set("#PostLines.Value", star.name());
        }
        if (boss != null) {
            String bossType = boss.isMini() ? MINI : boss.gated() ? BOSS : BOSS_ZONE;
            ui.set("#PostCategory.Value", MONSTERS);
            ui.set("#PostType.Entries", types(MONSTERS));
            ui.set("#PostType.Value", bossType);
            ui.set("#PostSpec.Entries", specs(bossType));
            ui.set("#PostSpec.Value", STAR_PREFIX + bossRole(boss));
            ui.set("#PostLevel.Value", boss.getLevel());
            ui.set("#PostLines.Value", boss.getName());
        }
        ui.set("#PostState.Text", boss != null ? boss.kind() + " « " + boss.getName() + " » (" + boss.getSpecies() + ", niveau " + boss.getLevel()
                + com.eldanior.system.boards.MonsterContracts.rankSuffix(boss.getLevel()) + ")"
                + (boss.getDeadAt() > 0 ? " : abattu, revient dans " + Math.max(0, (boss.getDeadAt() + com.eldanior.system.boards.DungeonBosses.RESPAWN_MS - System.currentTimeMillis()) / 60_000 + 1) + " min." : ".")
                : star != null ? "Monstre étoilé (" + com.eldanior.system.boards.MonsterContracts.stars(star.stars()) + ") « " + star.name()
                + " » (niveau de zone " + star.level() + ") : son contrat est au tableau de prime chaque jour."
                : p == null ? "Poste vide : choisissez le PNJ à poser ici."
                : GuardManager.nameplate(p) + "  ·  " + p.id + (p.isAlive() ? "" : "  ·  en attente de réapparition")
                + (p.isLuo() ? "  ·  territoire du Clan Luo" : ""));
        services(ui, p != null ? p.type : GuardType.VILLE);
        if (star != null) contractHelp(ui);
        if (boss != null) { if (boss.isMini()) miniHelp(ui); else if (boss.gated()) bossHelp(ui); else zoneBossHelp(ui); }

        events.addEventBinding(CustomUIEventBindingType.Activating, "#PostPlace", EventData.of("Action", "place")
                .append("@Type", "#PostType.Value").append("@Spec", "#PostSpec.Value").append("@Tier", "#PostTier.Value")
                .append("@Facing", "#PostFacing.Value").append("@Level", "#PostLevel.Value")
                .append("@Shop", "#PostShop.Value").append("@Lines", "#PostLines.Value").append("@Family", "#PostFamily.Value")
                .append("@Quest", "#PostQuest.Value"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PostSave", EventData.of("Action", "save")
                .append("@Shop", "#PostShop.Value").append("@Lines", "#PostLines.Value").append("@Family", "#PostFamily.Value")
                .append("@Quest", "#PostQuest.Value"));
        events.addEventBinding(CustomUIEventBindingType.Activating, "#PostRemove", EventData.of("Action", "remove"));
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PostType", EventData.of("Action", "type").append("@Type", "#PostType.Value"), false);
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#PostCategory", EventData.of("Action", "category").append("@Type", "#PostCategory.Value"), false);
    }

    /** Categories de la liste des PNJ : valeur, libelle. */
    private static final String[][] CATEGORIES = {{"garde", "Gardes et combattants"}, {"peuple", "Peuple (habitants)"},
            {"commerce", "Commerce (commerçants, marchands)"}, {"exclusif", "Exclusifs (Sensei, maître de guilde, forgeron légendaire)"},
            {"aventurier", "Aventuriers"}, {"horslaloi", "Hors-la-loi (criminels)"},
            {"monstres", "Monstres (étoilés, mini-boss, boss de donjon, boss de zone, Boss Mondial)"}, {"tous", "Tous les PNJ"}};

    /** Categorie Monstres (2026-11-20, demande de l'utilisateur) : monstre etoile, boss de donjon, boss de zone, Boss Mondial. */
    static final String MONSTERS = "monstres";

    /**
     * PNJ exclusifs (2026-11-16) : uniques ou presque dans le monde, chacun avec un service a lui (techniques et duels du
     * Sensei, guildes, forge du fer divin). Ajouter ici les prochains.
     */
    static boolean exclusive(GuardType t) {
        return t == GuardType.SENSEI || t == GuardType.MAITRE_GUILDE || t == GuardType.LEGENDAIRE;
    }

    /** Pseudo-type « monstre etoile » de la categorie Hors-la-loi : cree une zone de contrat (boards.MonsterContracts). */
    static final String CONTRACT = "contrat_etoile";
    /** Valeur de la Specialite pour l'espece d'un monstre etoile : « etoile:Trork_Warrior ». */
    static final String STAR_PREFIX = "etoile:";

    /** Pseudo-type « Boss de donjon » (categorie Monstres) : boss garde par la porte de boss, poste DANS une zone Donjon (boards.DungeonBosses). */
    static final String BOSS = "boss_donjon";
    /** Pseudo-type « Boss de zone » (2026-11-20, separe du boss de donjon) : meme boss, poste HORS d'un donjon, apparait a l'approche. */
    static final String BOSS_ZONE = "boss_zone";
    /** Pseudo-type « Mini-Boss » (2026-11-24) : plus petit qu'un boss, apparait a l'approche partout (dans un donjon aussi). */
    static final String MINI = "mini_boss";
    /** Pseudo-type « Boss Mondial » : annonce un Boss Mondial a ce poste (boards.WorldBosses), au lieu d'un lieu tire au hasard. */
    static final String WORLD_BOSS = "boss_mondial";
    /** Delai avant l'apparition d'un Boss Mondial pose a un poste (minutes) ; le panneau d'administration laisse le choisir. */
    static final int WORLD_BOSS_DELAY = 5;

    /**
     * Specialites proposees pour ce type de PNJ (2026-11-16 : la liste suit le PNJ choisi, plus un melange de tout) : classes
     * d'aventurier (sans mage pour chasseurs et criminels), metiers du commercant, boutiques du marchand Luo, noms du forgeron
     * legendaire, especes du monstre etoile et du boss ; « Aucune » pour les autres.
     */
    static List<DropdownEntryInfo> specs(String typeArg) {
        List<DropdownEntryInfo> out = new ArrayList<>();
        if (WORLD_BOSS.equals(typeArg)) {
            out.add(entry("Boss Mondial au hasard", RANDOM));
            for (var k : com.eldanior.system.boards.WorldBosses.KINDS) out.add(entry(k.name() + "  (" + k.species() + ", ×" + k.scaleLabel() + ")", k.id()));
            return out;
        }
        if (CONTRACT.equals(typeArg) || BOSS.equals(typeArg) || BOSS_ZONE.equals(typeArg) || MINI.equals(typeArg)) {
            out.add(entry("Espèce au hasard" + (CONTRACT.equals(typeArg) ? " (tirée chaque jour)" : ""), RANDOM));
            for (com.eldanior.system.boards.MonsterContracts.Species sp : !CONTRACT.equals(typeArg)
                    ? com.eldanior.system.boards.MonsterContracts.bossSpecies() : com.eldanior.system.boards.MonsterContracts.species())
                out.add(entry(sp.label() + "  (" + sp.role() + ")", STAR_PREFIX + sp.role()));
            return out;
        }
        GuardType t = GuardType.fromArg(typeArg);
        if (t == GuardType.AVENTURIER || t == GuardType.CHASSEUR_PRIME || t == GuardType.CRIMINEL) {
            out.add(entry("Classe au hasard", RANDOM));
            for (AdventurerType a : AdventurerType.values())
                if (t == GuardType.AVENTURIER || BountyHunter.isHunterType(a)) out.add(entry("Classe · " + a.getLabel(), a.name().toLowerCase()));
        } else if (t == GuardType.COMMERCANT) {
            out.add(entry("Métier au hasard", RANDOM));
            for (NpcTrades.Metier m : NpcTrades.Metier.values()) out.add(entry("Métier · " + m.getLabel(), NpcTrades.value(m)));
        } else if (t == GuardType.MARCHAND) {
            out.add(entry("Boutique au hasard", RANDOM));
            for (LuoMerchants.Shop sh : LuoMerchants.Shop.values()) out.add(entry("Boutique Luo · " + sh.getLabel(), sh.arg));
        } else if (t == GuardType.LEGENDAIRE) {
            out.add(entry("Nom au hasard", RANDOM));
            for (LegendarySmiths.Smith sm : LegendarySmiths.Smith.values()) out.add(entry("Nom : " + sm.getLabel(), "legende_" + sm.arg()));
        } else {
            out.add(entry("Aucune (ce PNJ n'a pas de spécialité)", RANDOM));
        }
        return out;
    }

    /** Boss de donjon a ce poste (10 blocs), null sinon. */
    private static com.eldanior.system.boards.DungeonBosses.Boss bossNear(String key) {
        Vector3d at = NpcPostBlocks.center(key);
        String world = NpcPostBlocks.world(key);
        return at == null || world == null ? null : com.eldanior.system.boards.DungeonBosses.near(world, at);
    }

    private static String bossRole(com.eldanior.system.boards.DungeonBosses.Boss b) {
        for (var sp : com.eldanior.system.boards.MonsterContracts.bossSpecies()) if (sp.label().equals(b.getSpecies())) return sp.role();
        return "";
    }

    /** Zone de monstre etoile a ce poste (10 blocs), null sinon. */
    private static com.eldanior.system.boards.MonsterContracts.Zone starNear(String key) {
        Vector3d at = NpcPostBlocks.center(key);
        String world = NpcPostBlocks.world(key);
        if (at == null || world == null) return null;
        for (var z : com.eldanior.system.boards.MonsterContracts.getZones())
            if (z.stars() > 0 && z.world().equals(world) && z.pos().distanceSquared(at) <= 100) return z;
        return null;
    }

    private static String category(GuardType t) {
        if (exclusive(t)) return "exclusif";
        return switch (t.getKind()) {
            case GUARD -> "garde";
            case VILLAGER -> "peuple";
            case SERVICE -> "commerce";
            case ADVENTURER, HUNTER, ROAMER -> "aventurier";
            case CRIMINAL -> "horslaloi";
        };
    }

    private static List<DropdownEntryInfo> types(String cat) {
        List<DropdownEntryInfo> out = new ArrayList<>();
        for (GuardType t : TYPE_ORDER) if ("tous".equals(cat) || category(t).equals(cat)) out.add(entry(typeLabel(t), t.getArg()));
        if (MONSTERS.equals(cat) || "tous".equals(cat)) {                               // categorie Monstres (2026-11-20)
            out.add(entry("Monstre · Monstre étoilé (contrat du tableau de prime, 1 à 3 étoiles)", CONTRACT));
            out.add(entry("Monstre · Mini-Boss (apparaît à l'approche, partout, réapparaît)", MINI));
            out.add(entry("Monstre · Boss de donjon (dans une zone Donjon, derrière la porte de boss)", BOSS));
            out.add(entry("Monstre · Boss de zone (hors donjon, apparaît à l'approche, réapparaît)", BOSS_ZONE));
            out.add(entry("Monstre · Boss Mondial (annoncé à tous, apparaît ici dans " + WORLD_BOSS_DELAY + " min, 1 à 3 couronnes)", WORLD_BOSS));
        }
        return out;
    }

    /** Ordre de la liste : gardes, civils, marchands, aventuriers, hors-la-loi. */
    private static final List<GuardType> TYPE_ORDER = List.of(GuardType.VILLE, GuardType.ROYAL, GuardType.CHEVALIER, GuardType.PATROUILLEUR,
            GuardType.ASSASSIN, GuardType.DISCIPLE, GuardType.MAGE_ROYAL, GuardType.VILLAGEOIS, GuardType.COMMERCANT, GuardType.MARCHAND,
            GuardType.AMBULANT, GuardType.LEGENDAIRE, GuardType.SENSEI, GuardType.MAITRE_GUILDE, GuardType.AVENTURIER, GuardType.CHASSEUR_PRIME, GuardType.CHEVALIER_ERRANT,
            GuardType.CRIMINEL);

    /** « Garde · Garde de ville », « Civil · Pèlerin (se parle) »... */
    private static String typeLabel(GuardType t) {
        String group = switch (t.getKind()) {
            case GUARD -> "Garde";
            case VILLAGER -> "Civil";
            case SERVICE -> t == GuardType.MAITRE_GUILDE ? "Service" : "Marchand";
            case ADVENTURER, HUNTER, ROAMER -> "Aventurier";
            case CRIMINAL -> "Hors-la-loi";
        };
        if (t == GuardType.DISCIPLE) return "Dojo · Disciple (s'entraîne, duel des quêtes du Sensei)";
        if (exclusive(t)) return "Exclusif · " + t.getLabel() + (NpcServices.talks(t) ? "  (se parle)" : "");
        return group + " · " + t.getLabel() + (NpcServices.talks(t) ? "  (se parle)" : "");
    }

    /** « Rodrik le Garde · 3 quêtes (Chasse) » : nom du donneur (NpcData), nombre et categorie de ses quetes. */
    private static String giverLabel(String giver, List<com.eldanior.system.quest.QuestModel> quests) {
        String name = null;
        for (var d : com.eldanior.system.config.configs.Mobs.Mobs.NpcData.values())
            if (giver.equalsIgnoreCase(d.getKeyword()) && d.getCustomTitle() != null) name = d.getCustomTitle();
        if (name == null) name = giver.replace("Quest_Npc_", "").replace('_', ' ');
        String cat = "";
        try { cat = quests.get(0).getCategory() != null ? " (" + quests.get(0).getCategory().getDisplayName() + ")" : ""; } catch (Exception ignored) { }
        return name + " · " + quests.size() + (quests.size() > 1 ? " quêtes" : " quête") + cat;
    }

    /** Section Services : visible pour un PNJ qui se parle, sinon une explication. */
    private static void services(UICommandBuilder ui, GuardType type) {
        boolean talks = type != null && NpcServices.talks(type);
        ui.set("#PostServices.Visible", talks);
        ui.set("#PostServicesOff.Visible", !talks);
        ui.set("#PostServicesOff.Text", "« " + (type != null ? type.getLabel() : "Ce PNJ") + " » ne se parle pas : les gardes et les combattants n'ont pas de services. "
                + "Pour proposer commerce, quêtes ou discussion, choisissez un villageois, un pèlerin, un marchand, un aventurier ou le maître de guilde.");
    }

    private static boolean usesClass(GuardType t) {
        return t == GuardType.AVENTURIER || t == GuardType.CHASSEUR_PRIME || t == GuardType.CRIMINEL;
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PostEventData data) {
        if ("category".equals(data.action)) {                                         // categorie : liste des PNJ filtree
            UICommandBuilder ui = new UICommandBuilder();
            List<DropdownEntryInfo> list = types(data.type == null ? "tous" : data.type);
            ui.set("#PostType.Entries", list);
            if (!list.isEmpty()) {
                String first = list.get(0).value();
                ui.set("#PostType.Value", first);
                typeChanged(ui, first);
            }
            sendUpdate(ui);
            return;
        }
        if ("type".equals(data.action)) {                                             // type change : section Services
            UICommandBuilder ui = new UICommandBuilder();
            typeChanged(ui, data.type);
            sendUpdate(ui);
            return;
        }
        this.close();
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        if (pr == null || !pr.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) return;
        World world = Universe.get().getWorld(NpcPostBlocks.world(key));
        if (world == null || !NpcPostBlocks.isBlock(key)) { pr.sendMessage(Message.raw("Ce poste n'existe plus.")); return; }
        GuardManager.Post old = NpcPostBlocks.postOf(key);
        if ("remove".equals(data.action)) {
            if (old == null && bossNear(key) != null) {                                  // boss de donjon
                var gone = com.eldanior.system.boards.DungeonBosses.removeNear(world.getName(), NpcPostBlocks.center(key));
                pr.sendMessage(Message.raw((gone.isEmpty() ? "Boss « ?" : gone.get(0).kind() + " « " + gone.get(0).getName()) + " » retiré."));
                return;
            }
            if (old == null && starNear(key) != null) {                                 // monstre etoile : zone et contrat du jour
                var gone = com.eldanior.system.boards.MonsterContracts.removeStarredNear(world.getName(), NpcPostBlocks.center(key));
                pr.sendMessage(Message.raw("Monstre étoilé « " + (gone.isEmpty() ? "?" : gone.get(0).name()) + " » retiré, avec son contrat."));
                return;
            }
            if (old != null) GuardManager.removeById(old.id, world);
            NpcPostBlocks.link(key, null);
            pr.sendMessage(Message.raw(old != null ? "PNJ retiré ; le poste reste en place (cassez le bloc pour le supprimer)." : "Ce poste n'a pas de PNJ."));
            return;
        }
        if ("save".equals(data.action)) {
            if (old == null) { pr.sendMessage(Message.raw("Posez d'abord un PNJ.")); return; }
            applyServices(old, data);
            String family = data.family == null || data.family.isBlank() ? null : data.family;
            if (clanRefused(old.type, family)) { pr.sendMessage(Message.raw(CLAN_REFUSED)); family = old.allegiance; }
            if (!java.util.Objects.equals(family, old.allegiance)) {                       // famille changee : il reapparait habille
                old.allegiance = family;
                world.execute(() -> GuardManager.respawn(old, world));
                pr.sendMessage(Message.raw("Famille de " + GuardManager.nameplate(old) + " : "
                        + (family == null ? "celle du territoire" : family.equals(GuardManager.NEUTRAL) ? "neutre" : family) + "."));
            }
            GuardManager.saveLater();
            if (!NpcServices.talks(old.type)) {
                pr.sendMessage(Message.raw(GuardManager.nameplate(old) + " ne se parle pas : choisissez un PNJ civil pour lui donner des services."));
                return;
            }
            pr.sendMessage(Message.raw("Services de " + GuardManager.nameplate(old) + " : "
                    + String.join(", ", NpcServices.services(old).stream().map(sv -> sv.label).toList())));
            return;
        }
        if (!"place".equals(data.action)) return;
        NpcStatut statut = NpcStatut.of(data.tier);
        if (CONTRACT.equals(data.type)) { placeStar(pr, world, old, data, statut); return; }
        if (MINI.equals(data.type)) { placeMini(pr, world, old, data); return; }
        if (BOSS.equals(data.type) || BOSS_ZONE.equals(data.type)) { placeBoss(pr, world, old, data, BOSS.equals(data.type)); return; }
        if (WORLD_BOSS.equals(data.type)) { placeWorldBoss(pr, world, data, statut); return; }
        GuardType type = GuardType.fromArg(data.type);
        if (type == null) { pr.sendMessage(Message.raw("Type de PNJ inconnu.")); return; }
        AdventurerType advType = usesClass(type) ? AdventurerType.fromArg(data.spec) : null;
        if (advType != null && type != GuardType.AVENTURIER && !BountyHunter.isHunterType(advType)) advType = null;   // jamais mage
        AdventurerTier advTier = type == GuardType.AVENTURIER ? (statut != null ? statut.adventurer() : AdventurerTier.fromArg(data.tier)) : null;
        LuoMerchants.Shop shop = type == GuardType.MARCHAND ? LuoMerchants.shopArg(data.spec) : null;
        LuoMerchants.Rank rank = type == GuardType.MARCHAND ? (statut != null ? statut.luo() : LuoMerchants.rankArg(data.tier)) : null;
        // Le champ Niveau l'emporte... sauf s'il n'a pas ete touche : la fenetre le pre-remplit avec le niveau du PNJ en place, qui
        // ecrasait le nouveau statut (2026-11-20 : un garde Apprenti passe en Maitre restait au niveau d'Apprenti).
        boolean levelUntouched = old != null && old.type == type && data.level == old.level && statut != NpcStatut.current(old);
        int level = statut != null && (data.level <= 0 || levelUntouched) ? statut.levelFor(type) : data.level;
        NpcTrades.Metier metier = type == GuardType.COMMERCANT ? NpcTrades.ofArg(data.spec) : null;
        Vector3d at = NpcPostBlocks.center(key);
        TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
        float yaw = NpcPostBlocks.yaw(data.facing, at, t != null ? new Vector3d(t.getPosition()) : at);
        if (metier == NpcTrades.Metier.INTENDANT) {                                             // un seul intendant par territoire
            GuardManager.Post other = NpcTrades.stewardOn(world.getName(), at.x, at.y, at.z);
            if (other != null && (old == null || !other.id.equals(old.id))) {
                pr.sendMessage(Message.raw("Ce territoire a déjà son intendant (" + other.id + ")."));
                return;
            }
        }
        if (old != null) GuardManager.removeById(old.id, world);
        com.eldanior.system.boards.MonsterContracts.removeStarredNear(world.getName(), at);  // un seul occupant par poste :
        com.eldanior.system.boards.DungeonBosses.removeNear(world.getName(), at);            // monstre etoile ou boss retire
        NpcTrades.Rang tradeRang = type == GuardType.COMMERCANT ? (statut != null ? statut.trade() : NpcTrades.rangArg(data.tier)) : null;
        // exclusifs sans systeme de rang (maitre de guilde, forgeron legendaire) : statut garde et affiche
        // 2026-11-20 : garde pour TOUS (la fenetre le reaffiche) ; il n'apparait dans le nom que pour les PNJ sans rang (NpcStatut.suffix)
        String keptStatut = statut != null ? statut.arg : null;
        LegendarySmiths.Smith smith = type == GuardType.LEGENDAIRE ? LegendarySmiths.ofArg(data.spec) : null;
        String preset = smith != null ? LegendarySmiths.value(smith) : tradeRang != null && metier != null ? NpcTrades.value(metier, tradeRang) : null;
        String family = data.family == null || data.family.isBlank() ? null : data.family;
        if (clanRefused(type, family)) { pr.sendMessage(Message.raw(CLAN_REFUSED)); family = null; }
        GuardManager.Post p = GuardManager.place(type, world, at, yaw, advType, advTier,
                usesClass(type) || MartialRanks.applies(type) || type == GuardType.SENSEI ? Math.max(0, level) : 0, shop, rank, metier, preset, keptStatut);
        if (family != null) {                                                              // famille choisie : il reapparait avec elle
            p.allegiance = family;
            world.execute(() -> GuardManager.respawn(p, world));
        }
        if (tradeRang != null && metier == null && NpcTrades.metier(p) != null) p.shop = NpcTrades.value(NpcTrades.metier(p), tradeRang);
        NpcPostBlocks.link(key, p.id);
        if (NpcServices.talks(type)) applyServices(p, data);
        GuardManager.saveLater();
        pr.sendMessage(Message.raw(GuardManager.nameplate(p) + " posté" + (old != null ? ", à la place de " + GuardManager.nameplate(old) : "") + "."));
    }

    /** Type choisi : specialites de ce type (premiere selectionnee) et section Services. */
    private static void typeChanged(UICommandBuilder ui, String typeArg) {
        ui.set("#PostSpec.Entries", specs(typeArg));
        ui.set("#PostSpec.Value", RANDOM);
        if (CONTRACT.equals(typeArg)) contractHelp(ui);
        else if (BOSS.equals(typeArg)) bossHelp(ui);
        else if (BOSS_ZONE.equals(typeArg)) zoneBossHelp(ui);
        else if (MINI.equals(typeArg)) miniHelp(ui);
        else if (WORLD_BOSS.equals(typeArg)) worldBossHelp(ui);
        else services(ui, GuardType.fromArg(typeArg));
    }

    /** Section Services : explication du boss de donjon. */
    private static void bossHelp(UICommandBuilder ui) {
        ui.set("#PostServices.Visible", false);
        ui.set("#PostServicesOff.Visible", true);
        ui.set("#PostServicesOff.Text", "Boss de donjon : à poser DANS une zone Donjon. Il n'apparaît que lorsqu'une Clé de Boss ouvre "
                + "la porte de boss du même donjon, attend immobile que la porte se referme (15 s), et le groupe qui ouvre est prévenu. "
                + "Hors d'un donjon, choisissez « Boss de zone ». Le monstre choisi est agrandi, avec une tête de mort "
                + "au-dessus de la tête et une barre de boss. Il n'est dans aucun tableau (ni prime, ni quête). Abattu, il revient "
                + "après " + com.eldanior.system.boards.DungeonBosses.RESPAWN_MS / 60_000 + " min (essais ; 2 h prévues). Spécialité = espèce, "
                + "Niveau = niveau du boss (100 par défaut ; au-delà de 999 : rang SS, au-delà de 1500 : rang EX), Répliques = nom du boss. "
                + "Le Statut ne change rien.");
    }

    /** Section Services : explication du boss de zone (2026-11-20, separe du boss de donjon). */
    private static void zoneBossHelp(UICommandBuilder ui) {
        ui.set("#PostServices.Visible", false);
        ui.set("#PostServicesOff.Visible", true);
        ui.set("#PostServicesOff.Text", "Boss de zone : à poser HORS d'une zone Donjon (dans un donjon, choisissez « Boss de donjon »). Il apparaît "
                + "quand un joueur approche, agrandi, avec une tête de mort au-dessus de la tête et une barre de boss. Il n'est dans aucun "
                + "tableau. Abattu, il revient après " + com.eldanior.system.boards.DungeonBosses.RESPAWN_MS / 60_000 + " min (essais ; 2 h prévues). "
                + "Spécialité = espèce, Niveau = niveau du boss (100 par défaut), Répliques = nom du boss. Le Statut ne change rien.");
    }

    /** Section Services : explication du mini-boss (2026-11-24). */
    private static void miniHelp(UICommandBuilder ui) {
        ui.set("#PostServices.Visible", false);
        ui.set("#PostServicesOff.Visible", true);
        ui.set("#PostServicesOff.Text", "Mini-Boss : à poser partout, dans un donjon aussi (il n'attend pas la porte de boss). Il apparaît "
                + "quand un joueur approche, plus grand qu'un monstre ordinaire mais moins qu'un boss, avec une tête de mort et une barre de boss. "
                + "Il n'est dans aucun tableau. Abattu, il revient après " + com.eldanior.system.boards.DungeonBosses.RESPAWN_MS / 60_000
                + " min. Spécialité = espèce, Niveau = niveau du mini-boss (60 par défaut), Répliques = nom. Le Statut ne change rien.");
    }

    /** Pose d'un mini-boss : remplace le PNJ, le monstre etoile ou le boss du poste. */
    private void placeMini(PlayerRef pr, World world, GuardManager.Post old, PostEventData data) {
        Vector3d at = NpcPostBlocks.center(key);
        if (com.eldanior.system.failles.FailleManager.editInstanceOf(world.getName()) != null) {
            pr.sendMessage(Message.raw("Dans un modèle de faille, les mini-boss viennent du plan du donjon (salles) : rien n'a été posé.").color("#e0574a"));
            return;
        }
        if (old != null) {
            GuardManager.removeById(old.id, world);
            NpcPostBlocks.link(key, null);
        }
        boolean hadStar = !com.eldanior.system.boards.MonsterContracts.removeStarredNear(world.getName(), at).isEmpty();
        String role = data.spec != null && data.spec.startsWith(STAR_PREFIX) ? data.spec.substring(STAR_PREFIX.length()) : null;
        String name = data.lines != null && !data.lines.isBlank() ? data.lines.split("\\|")[0].trim() : "Mini-Boss";
        int level = data.level > 0 ? data.level : 60;
        var b = com.eldanior.system.boards.DungeonBosses.add(name, world.getName(), at, level, role, true);
        pr.sendMessage(Message.raw("Mini-Boss posé : " + b.getName() + " (" + b.getSpecies() + " niveau " + b.getLevel()
                + com.eldanior.system.boards.MonsterContracts.rankSuffix(b.getLevel()) + "). Il apparaît quand un joueur approche et revient "
                + com.eldanior.system.boards.DungeonBosses.RESPAWN_MS / 60_000 + " min après sa mort."
                + (old != null ? " Le PNJ du poste a été retiré." : "") + (hadStar ? " Le monstre étoilé du poste a été retiré." : "")));
    }

    /** Section Services : explication du Boss Mondial pose a un poste. */
    private static void worldBossHelp(UICommandBuilder ui) {
        ui.set("#PostServices.Visible", false);
        ui.set("#PostServicesOff.Visible", true);
        ui.set("#PostServicesOff.Text", "Boss Mondial : annoncé à tous les joueurs, il apparaît À CE POSTE dans " + WORLD_BOSS_DELAY + " min (rappels dans "
                + "le chat, marqueur sur la carte et la boussole) et reste 1 h. Spécialité = boss (au hasard ou choisi), Statut = couronnes "
                + "(Apprenti 1 couronne niveau ×1,2, Expert 2 ×1,5, Maître 3 ×2 ; Au hasard : 1 à 3), Niveau = niveau de base (0 : au hasard entre "
                + com.eldanior.system.boards.WorldBosses.MIN_LEVEL + " et " + com.eldanior.system.boards.WorldBosses.MAX_BASE_LEVEL
                + "). Un seul Boss Mondial à la fois ; le poste garde son PNJ. Pour un lieu au hasard : panneau d'administration › Boss Mondial.");
    }

    /** Annonce d'un Boss Mondial a ce poste (le PNJ du poste reste en place). */
    private void placeWorldBoss(PlayerRef pr, World world, PostEventData data, NpcStatut statut) {
        int crowns = statut == NpcStatut.MAITRE ? 3 : statut == NpcStatut.EXPERT ? 2 : statut == NpcStatut.APPRENTI ? 1 : 0;
        String msg = com.eldanior.system.boards.WorldBosses.start(data.spec, world, WORLD_BOSS_DELAY, Math.max(0, data.level), 60, crowns,
                NpcPostBlocks.center(key));
        pr.sendMessage(Message.raw(msg));
    }

    /** Section Services : explication du monstre etoile (il ne se parle pas). */
    private static void contractHelp(UICommandBuilder ui) {
        ui.set("#PostServices.Visible", false);
        ui.set("#PostServicesOff.Visible", true);
        ui.set("#PostServicesOff.Text", "Monstre étoilé : un monstre de contrat géant apparaît ici quand un joueur approche, chaque jour, "
                + "et son contrat est au tableau de prime. Spécialité = espèce (« Au hasard » : tirée chaque jour), Statut = étoiles "
                + "(Apprenti 1 étoile niveau x1,2, Expert 2 étoiles x1,5, Maître 3 étoiles x2 ; récompense x1, x2, x3), Niveau = niveau de la zone (50 par défaut), "
                + "Répliques = nom du lieu.");
    }

    /** Pose d'un monstre etoile : zone de contrat au poste et contrat du jour (remplace le PNJ du poste s'il y en a un). */
    private void placeStar(PlayerRef pr, World world, GuardManager.Post old, PostEventData data, NpcStatut statut) {
        Vector3d at = NpcPostBlocks.center(key);
        if (old != null) {
            GuardManager.removeById(old.id, world);
            NpcPostBlocks.link(key, null);
        }
        com.eldanior.system.boards.DungeonBosses.removeNear(world.getName(), at);           // un seul monstre par poste
        int stars = statut == NpcStatut.MAITRE ? 3 : statut == NpcStatut.EXPERT ? 2 : 1;
        String role = data.spec != null && data.spec.startsWith(STAR_PREFIX) ? data.spec.substring(STAR_PREFIX.length()) : null;
        String name = data.lines != null && !data.lines.isBlank() ? data.lines.split("\\|")[0].trim() : "Repaire étoilé";
        int level = data.level > 0 ? data.level : 50;
        var c = com.eldanior.system.boards.MonsterContracts.addStarred(name, world.getName(), at, level, stars, role);
        pr.sendMessage(Message.raw("Monstre étoilé posé : " + c.title() + " (" + c.getSpecies() + " niveau " + c.getLevel() + ", "
                + com.eldanior.system.shop.CoinWallet.format(c.getReward()) + ") à « " + name + " ». Son contrat est au tableau de prime ; "
                + "il revient chaque jour." + (old != null ? " Le PNJ du poste a été retiré." : "")));
    }

    /** Pose d'un boss de donjon (dungeon) ou de zone : remplace le PNJ, le monstre etoile ou le boss du poste. */
    private void placeBoss(PlayerRef pr, World world, GuardManager.Post old, PostEventData data, boolean dungeon) {
        Vector3d at = NpcPostBlocks.center(key);
        String rift = com.eldanior.system.failles.FailleManager.editInstanceOf(world.getName());
        if (rift != null) { placeRiftBoss(pr, rift, old, data, at, world); return; }
        boolean inDungeon = com.eldanior.system.boards.DungeonBosses.dungeonAt(world.getName(), at) != null;
        if (dungeon && !inDungeon) {
            pr.sendMessage(Message.raw("Ce poste n'est dans aucune zone Donjon : choisissez « Boss de zone », ou posez d'abord la zone du donjon."));
            return;
        }
        if (!dungeon && inDungeon) {
            pr.sendMessage(Message.raw("Ce poste est dans une zone Donjon : choisissez « Boss de donjon » (il attend la porte de boss)."));
            return;
        }
        if (old != null) {
            GuardManager.removeById(old.id, world);
            NpcPostBlocks.link(key, null);
        }
        boolean hadStar = !com.eldanior.system.boards.MonsterContracts.removeStarredNear(world.getName(), at).isEmpty();
        String role = data.spec != null && data.spec.startsWith(STAR_PREFIX) ? data.spec.substring(STAR_PREFIX.length()) : null;
        String name = data.lines != null && !data.lines.isBlank() ? data.lines.split("\\|")[0].trim() : "Seigneur du donjon";
        int level = data.level > 0 ? data.level : 100;
        var b = com.eldanior.system.boards.DungeonBosses.add(name, world.getName(), at, level, role);
        pr.sendMessage(Message.raw(b.kind() + " posé : " + b.getName() + " (" + b.getSpecies() + " niveau " + b.getLevel()
                + com.eldanior.system.boards.MonsterContracts.rankSuffix(b.getLevel()) + "). Il apparaît quand un joueur approche et revient "
                + com.eldanior.system.boards.DungeonBosses.RESPAWN_MS / 60_000 + " min après sa mort."
                + (old != null ? " Le PNJ du poste a été retiré." : "") + (hadStar ? " Le monstre étoilé du poste a été retiré." : "")));
    }

    /** Boss pose sur un poste DANS un modele de faille (monde d'edition, 2026-11-23) : il devient le boss de ce donjon
     *  (boss.json du modele) ; chaque faille ouverte le fait apparaitre ici a l'approche d'un joueur. Pas de zone Donjon requise. */
    private void placeRiftBoss(PlayerRef pr, String instance, GuardManager.Post old, PostEventData data, Vector3d at, World world) {
        if (old != null) {
            GuardManager.removeById(old.id, world);
            NpcPostBlocks.link(key, null);
        }
        String role = data.spec != null && data.spec.startsWith(STAR_PREFIX) ? data.spec.substring(STAR_PREFIX.length()) : null;
        String name = data.lines != null && !data.lines.isBlank() ? data.lines.split("\\|")[0].trim() : null;
        int level = data.level > 0 ? Math.clamp(data.level, 1, com.eldanior.system.boards.MonsterContracts.MAX_LEVEL) : 0;
        var b = com.eldanior.system.failles.FailleManager.setTemplateBoss(instance, role, name, level, at);
        if (b == null) { pr.sendMessage(Message.raw("Boss de faille non enregistré (modèle introuvable).").color("#e0574a")); return; }
        pr.sendMessage(Message.raw("Boss de cette faille : " + b.name() + " (" + b.role().replace('_', ' ')
                + (b.level() > 0 ? ", niveau " + b.level() : ", niveau du plan") + "). Dans chaque faille ouverte de ce donjon, il apparaît ici "
                + "quand un joueur approche (24 blocs). Rien n'apparaît pendant l'édition.").color("#f2c45e"));
    }

    static final String CLAN_REFUSED ="Les gardes et les chevaliers servent la famille du territoire : ils ne portent pas le nom d'un clan.";

    /** Gardes et chevaliers ne portent pas le nom d'un des quatre clans (decision du 2026-11-10) ; les autres PNJ oui. */
    private static boolean clanRefused(GuardType type, String family) {
        boolean guard = type == GuardType.VILLE || type == GuardType.ROYAL || type == GuardType.PATROUILLEUR || type == GuardType.CHEVALIER;
        return guard && com.eldanior.system.luo.LuoClans.isClan(family);
    }

    /** Commerce (sauf marchand Luo, qui garde sa boutique), repliques. */
    private static void applyServices(GuardManager.Post p, PostEventData data) {
        if (p.type != GuardType.MARCHAND && p.type != GuardType.COMMERCANT && p.type != GuardType.LEGENDAIRE) p.shop = data.shop == null || data.shop.isBlank() ? null : data.shop;
        // quetes en chapitres (2026-11-13) ; un ancien donneur (ancien systeme, desactive) est conserve s'il n'est pas remplace
        boolean on = p.quest != null && p.quest.startsWith(NpcServices.CHAPTERS);    // « chapitres[:quetes ajoutees] »
        if (NpcServices.CHAPTERS.equals(data.quest)) { if (!on) p.quest = NpcServices.CHAPTERS; }
        else if (on) p.quest = null;
        StringBuilder sb = new StringBuilder();
        if (data.lines != null) for (String l : data.lines.split("\\|")) if (!l.isBlank()) sb.append(sb.length() > 0 ? "\n" : "").append(l.trim());
        p.lines = sb.length() > 0 ? sb.toString() : null;
    }

    public static class PostEventData {
        public static final BuilderCodec<PostEventData> CODEC = BuilderCodec.builder(PostEventData.class, PostEventData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d, v) -> d.action = v, d -> d.action).add()
                .append(new KeyedCodec<>("@Type", Codec.STRING), (d, v) -> d.type = v, d -> d.type).add()
                .append(new KeyedCodec<>("@Spec", Codec.STRING), (d, v) -> d.spec = v, d -> d.spec).add()
                .append(new KeyedCodec<>("@Tier", Codec.STRING), (d, v) -> d.tier = v, d -> d.tier).add()
                .append(new KeyedCodec<>("@Facing", Codec.STRING), (d, v) -> d.facing = v, d -> d.facing).add()
                .append(new KeyedCodec<>("@Level", Codec.INTEGER), (d, v) -> d.level = v, d -> d.level).add()
                .append(new KeyedCodec<>("@Shop", Codec.STRING), (d, v) -> d.shop = v, d -> d.shop).add()
                .append(new KeyedCodec<>("@Quest", Codec.STRING), (d, v) -> d.quest = v, d -> d.quest).add()
                .append(new KeyedCodec<>("@Lines", Codec.STRING), (d, v) -> d.lines = v, d -> d.lines).add()
                .append(new KeyedCodec<>("@Family", Codec.STRING), (d, v) -> d.family = v, d -> d.family).add()
                .build();
        public String action, type, spec, tier, facing, shop, quest, lines, family;
        public int level;
    }
}
