package com.eldanior.system.hologram;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class HologramCommand extends AbstractAsyncCommand {

    // === PRESETS ===

    private static final String[] WELCOME_LINES = {
            "=== Bienvenue sur Eldanior ===",
            "",
            "Il fut un temps ou les Anciens Dragons regnaient",
            "sur ces terres. Leur chute donna naissance",
            "aux Royaumes des Hommes, aux Familles Nobles",
            "et aux Ordres Sacres qui forgent ce monde.",
            "",
            "Guerriers, Mages, Archers, Assassins, Marchands, Dragons ou meme Dieu",
            "Choisissez votre voie et ecrivez votre legende.",
            "",
            "Tapez /es system pour commencer votre aventure !"
    };

    private static final String[] CLASSES_LINES = {
            "=== La Pierre du Destin ===",
            "",
            "Depuis la nuit des temps, cette statue",
            "guide les ames vers leur veritable voie.",
            "",
            "On raconte que ceux qui posent la main",
            "sur la pierre voient leur avenir se reveler.",
            "",
            "Approchez-vous et interagissez avec la statue",
            "pour decouvrir la classe qui sommeille en vous."
    };

    private static final String[] GUILDE_LINES = {
            "=== Hall des Guildes ===",
            "",
            "Les guildes rassemblent les aventuriers",
            "sous une meme banniere.",
            "",
            "Creez votre guilde ou rejoignez-en une",
            "pour partager tresorerie et gloire.",
            "",
            "/es guildcreate <nom> <tag> - Creer",
            "/es guild invite <joueur> - Inviter",
            "/es system > Guilde pour gerer"
    };

    private static final String[] NOBLESSE_LINES = {
            "=== Palais Royal ===",
            "",
            "La hierarchie du Royaume :",
            "Roi > Marquis > Duc > Comte > Baron > Chevalier",
            "",
            "Les nobles gouvernent les territoires,",
            "collectent les impots et emettent des decrets.",
            "",
            "Gagnez en dignite pour gravir les echelons.",
            "/es system > Territoires"
    };

    private static final String[] EGLISE_LINES = {
            "=== Cathedrale Sacree ===",
            "",
            "L'Ordre Sacre veille sur les ames d'Eldanior.",
            "Pape > Cardinal > Archeveque > Pretre > Laique",
            "",
            "Accumulez la Foi pour monter en rang",
            "et debloquer les benedictions divines.",
            "",
            "/es system > Titres pour voir votre rang"
    };

    private static final String[] MARCHE_LINES = {
            "=== Place du Marche ===",
            "",
            "Achetez et vendez des objets entre joueurs.",
            "Le Marche Noir accueille les criminels PK.",
            "",
            "/es sell <prix> - Vendre l'objet en main",
            "/es system > Shop pour parcourir",
            "/es system > Marche Noir (PK uniquement)",
            "",
            "Différents PNJ Marchant existe dans le monde.",
            "afin d'assurer un commerce minimal pour les joueurs!",
            "",
            "Les Marchands ont acces aux système d'Echanges directs."
    };

    private static final String[] COMBAT_LINES = {
            "=== Arene de Combat ===",
            "",
            "Defiez d'autres aventuriers en duel !",
            "Le perdant cede 10% de son XP au vainqueur.",
            "",
            "/es duel <joueur> - Defier un joueur",
            "/es duel accept - Accepter un defi",
            "",
            "Attention aux zones PvP : la mort y est reelle.",
            "Les criminels PK sont marques et traques."
    };

    private static final String[] QUETES_LINES = {
            "=== Panneau des Quetes ===",
            "",
            "Des missions vous attendent chaque jour.",
            "Completez-les pour gagner XP, Or et Titres.",
            "",
            "Quetes principales : suivez l'histoire d'Eldanior",
            "Quetes Secondaires : suivez différentes missions sur Eldanior",
            "Quetes journalieres : renouvellees chaque jour",
            "",
            "/es system > Quetes pour consulter",
            "Parlez aux PNJ pour decouvrir des missions."
    };

    private static final String[] COMPETENCES_LINES = {
            "=== Tour des Competences ===",
            "",
            "Apprenez des competences passives et actives",
            "en trouvant des Parchemins dans le monde.",
            "",
            "Passives : s'activent automatiquement au combat",
            "Actives : donnent un objet a utiliser",
            "",
            "Les competences evoluent en fonction de vous.",
            "/es system > Competences pour gerer"
    };

    private static final String[] DONJON_LINES = {
            "=== Portail de Donjon ===",
            "",
            "Au-dela de ce portail se cachent des creatures",
            "redoutables et des tresors inestimables.",
            "",
            "Formez un groupe avant d'entrer !",
            "/es system > Groupe",
            "",
            "Il existe 3 types de Donjons:",
            "- Donjon Naturel",
            "- Donjon Du Monde",
            "- Donjon Portail",
            "",
            "Les coffres decouverts contiennent des",
            "equipements, parchemins, artefacts et objets rares."
    };

    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> paramArg;

    public HologramCommand() {
        super("hologram", "Gestion des hologrammes (OP)");
        this.actionArg = this.withRequiredArg("action", "create|delete|list", ArgTypes.STRING);
        this.paramArg = this.withRequiredArg("param", "texte ou id", ArgTypes.STRING);
    }

    @Override
    protected boolean canGeneratePermission() { return false; }

    @Nonnull
    @Override
    public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> store = ref.getStore();
        World world = ((EntityStore) store.getExternalData()).getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);

        String action = this.actionArg.get(ctx);
        String param = this.paramArg.get(ctx);

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef senderRef = store.getComponent(ref, PlayerRef.getComponentType());
                Player sender = store.getComponent(ref, Player.getComponentType());
                if (senderRef == null || sender == null) return;

                if (!senderRef.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) {
                    senderRef.sendMessage(Message.raw("§cPermission refusee."));
                    return;
                }

                switch (action.toLowerCase()) {
                    case "create" -> handleCreate(sender, param);
                    case "delete", "remove" -> handleDelete(sender, param);
                    case "list" -> handleList(sender);
                    case "welcome" -> handlePresetMultiline(sender, WELCOME_LINES);
                    case "classes" -> handlePresetMultiline(sender, CLASSES_LINES);
                    case "guilde" -> handlePresetMultiline(sender, GUILDE_LINES);
                    case "noblesse" -> handlePresetMultiline(sender, NOBLESSE_LINES);
                    case "eglise" -> handlePresetMultiline(sender, EGLISE_LINES);
                    case "marche" -> handlePresetMultiline(sender, MARCHE_LINES);
                    case "combat" -> handlePresetMultiline(sender, COMBAT_LINES);
                    case "quetes" -> handlePresetMultiline(sender, QUETES_LINES);
                    case "competences" -> handlePresetMultiline(sender, COMPETENCES_LINES);
                    case "donjon" -> handlePresetMultiline(sender, DONJON_LINES);
                    case "arena" -> handleArenaHologram(sender);
                    default -> sendHelp(sender);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, world);
    }

    private void handleCreate(Player sender, String text) {
        if (text == null || text.isEmpty() || "_".equals(text)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage: /es hologram create <texte> (utilisez _ pour les espaces)"));
            return;
        }
        // _ = espace, | = nouvelle ligne
        text = text.replace("_", " ").replace("|", "\n");

        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();

        var transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            sender.getPlayerRef().sendMessage(Message.raw("§cPosition introuvable."));
            return;
        }

        double x = transform.getPosition().x;
        double y = transform.getPosition().y + 2.0;
        double z = transform.getPosition().z;

        String worldName = "default";
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef != null) {
                var w = Universe.get().getWorld(pRef.getWorldUuid());
                if (w != null) worldName = w.getName();
            }
        } catch (Exception e) { /* default */ }

        HologramData data = HologramManager.create(text, x, y, z, worldName);
        HologramManager.spawnHologram(data);

        sender.getPlayerRef().sendMessage(Message.raw("§aHologramme cree ! ID: §f" + data.getId() + " §7| " + data.getLocationString()));
    }

    private void handlePresetMultiline(Player sender, String[] lines) {
        var ref = sender.getReference();
        if (ref == null) return;
        var store = ref.getStore();

        var transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;

        double x = transform.getPosition().x;
        double y = transform.getPosition().y + 2.0;
        double z = transform.getPosition().z;

        String worldName = "default";
        try {
            PlayerRef pRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (pRef != null) {
                var w = Universe.get().getWorld(pRef.getWorldUuid());
                if (w != null) worldName = w.getName();
            }
        } catch (Exception e) { /* default */ }

        HologramData data = HologramManager.create(java.util.List.of(lines), x, y, z, worldName);
        HologramManager.spawnHologram(data);

        sender.getPlayerRef().sendMessage(Message.raw("§aHologramme cree ! ID: §f" + data.getId() + " §7(" + lines.length + " lignes)"));
    }

    private void handleDelete(Player sender, String id) {
        if (id == null || id.isEmpty() || "_".equals(id)) {
            sender.getPlayerRef().sendMessage(Message.raw("§cUsage: /es hologram delete <id>"));
            return;
        }

        if (HologramManager.delete(id)) {
            sender.getPlayerRef().sendMessage(Message.raw("§aHologramme §f" + id + " §asupprime ! (restart pour despawn)"));
        } else {
            sender.getPlayerRef().sendMessage(Message.raw("§cHologramme introuvable: " + id));
        }
    }

    private void handleList(Player sender) {
        var all = HologramManager.getAll();
        if (all.isEmpty()) {
            sender.getPlayerRef().sendMessage(Message.raw("§7Aucun hologramme."));
            return;
        }

        sender.getPlayerRef().sendMessage(Message.raw("§6=== Hologrammes (" + all.size() + ") ==="));
        for (HologramData h : all) {
            sender.getPlayerRef().sendMessage(Message.raw("§f" + h.getId() + " §7| §e" + h.getText() + " §7| " + h.getLocationString()));
        }
    }

    private void handleArenaHologram(Player sender) {
        // Trouver l'arene ou le joueur se trouve
        var ref = sender.getReference();
        if (ref == null) return;
        var transform = ref.getStore().getComponent(ref,
                com.hypixel.hytale.server.core.modules.entity.component.TransformComponent.getComponentType());
        if (transform == null) return;

        org.joml.Vector3d pos = transform.getPosition();
        String worldName = sender.getWorld().getName();

        com.eldanior.system.territory.ParcelData arena = com.eldanior.system.territory.ParcelManager.getParcelAt(
                worldName, pos.x, pos.y, pos.z);

        if (arena == null || arena.getType() != com.eldanior.system.territory.ParcelType.ARENA) {
            sender.getPlayerRef().sendMessage(Message.raw("§cVous devez etre dans une arene !"));
            return;
        }

        String holoId = DynamicHologramManager.createArenaHologram(
                arena.getId(), pos.x, pos.y + 2.0, pos.z, worldName);

        if (holoId != null) {
            sender.getPlayerRef().sendMessage(Message.raw("§a§lHologramme d'arene cree ! §7(" + holoId + ")"));
            sender.getPlayerRef().sendMessage(Message.raw("§7Classement top 5 de §f" + arena.getName().replace('_', ' ')));
            sender.getPlayerRef().sendMessage(Message.raw("§7Mise a jour automatique toutes les 60 secondes."));
        } else {
            sender.getPlayerRef().sendMessage(Message.raw("§cErreur lors de la creation de l'hologramme."));
        }
    }

    private void sendHelp(Player sender) {
        sender.getPlayerRef().sendMessage(Message.raw("§6=== Hologrammes ==="));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram create <texte> §7— Cree (_ = espace, | = ligne)"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram delete <id> §7— Supprime"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram list _ §7— Liste"));
        sender.getPlayerRef().sendMessage(Message.raw("§6--- Presets ---"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram welcome _ §7— Bienvenue"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram classes _ §7— Maitre des Classes"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram guilde _ §7— Hall des Guildes"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram noblesse _ §7— Palais Royal"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram eglise _ §7— Cathedrale Sacree"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram marche _ §7— Place du Marche"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram combat _ §7— Arene de Combat"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram quetes _ §7— Panneau des Quetes"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram competences _ §7— Tour des Competences"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram famille _ §7— Manoir des Familles"));
        sender.getPlayerRef().sendMessage(Message.raw("§f/es hologram donjon _ §7— Portail de Donjon"));
    }
}
