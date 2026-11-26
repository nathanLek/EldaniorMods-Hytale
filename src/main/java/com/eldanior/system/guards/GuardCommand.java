package com.eldanior.system.guards;

import com.eldanior.system.config.EldaniorLogger;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * /es garde poser [ville|royal|chevalier|patrouilleur|mage|errant|villageois|aventurier|chasseur|criminel|maitre_guilde|assassin|disciple|pelerin|marchand|ambulant|commercant] : PNJ de poste a votre position.
 *   Aventurier : --type guerrier|mage|archer|assassin, --statut ordinaire|exceptionnel|heros, --niveau 20..600 (sinon au hasard).
 *   Chasseur de prime, criminel : --type guerrier|archer|assassin, --niveau 20..600 (sinon au hasard).
 *   Marchand Luo : --type armurerie|tailleur|herboriste|maison_the, --statut apprenti|negociant|grand (sinon au hasard).
 * /es garde retirer : retire le poste le plus proche (6 blocs) et son garde.
 * /es garde liste : postes et etat des gardes.
 * (Sous-commandes : un argument optionnel de commande Hytale s'ecrit --nom valeur, pas en position.)
 */
public class GuardCommand extends AbstractCommandCollection {

    public GuardCommand() {
        super("garde", "Gardes de zone (OP) : poser|retirer|liste");
        this.addSubCommand(new Place());
        this.addSubCommand(new Remove());
        this.addSubCommand(new ListPosts());
    }

    protected boolean canGeneratePermission() { return false; }

    /** Contexte d'une sous-commande, execute dans le thread du monde du joueur (admin seulement). */
    private interface Action {
        void run(PlayerRef sender, World world, Vector3d pos, float yaw);
    }

    private static CompletableFuture<Void> onWorld(CommandContext ctx, Action action) {
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);
        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        if (world == null) return CompletableFuture.completedFuture(null);
        world.execute(() -> {
            try {
                PlayerRef sender = store.getComponent(ref, PlayerRef.getComponentType());
                if (sender == null) return;
                if (!sender.hasPermission(EldaniorLogger.ADMIN_PERMISSION)) { sender.sendMessage(Message.raw("Permission refusee.")); return; }
                TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
                if (t == null) return;
                action.run(sender, world, new Vector3d(t.getPosition()), t.getRotation().yaw());
            } catch (Exception e) {
                EldaniorLogger.error("GuardCommand", e);
            }
        });
        return CompletableFuture.completedFuture(null);
    }

    private static final class Place extends AbstractAsyncCommand {
        private final RequiredArg<String> rankArg;
        private final OptionalArg<String> typeArg, tierArg;
        private final OptionalArg<Integer> levelArg;

        Place() {
            super("poser", "Poste un PNJ ici : " + GuardType.args());
            this.rankArg = this.withRequiredArg("rang", GuardType.args(), ArgTypes.STRING);
            this.typeArg = this.withOptionalArg("type", "Aventurier : guerrier|mage|archer|assassin ; chasseur, criminel : guerrier|archer|assassin ; marchand : armurerie|tailleur|herboriste|maison_the ; commercant : metier ; legendaire : humain|nain|elfe", ArgTypes.STRING);
            this.tierArg = this.withOptionalArg("statut", "Aventurier : ordinaire|exceptionnel|heros ; marchand : apprenti|negociant|grand ; commercant : apprenti|compagnon|maitre", ArgTypes.STRING);
            this.levelArg = this.withOptionalArg("niveau", "Aventurier, chasseur, criminel : 20 a 600", ArgTypes.INTEGER);
        }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            String rank = this.rankArg.get(ctx);
            String typeName = this.typeArg.get(ctx), tierName = this.tierArg.get(ctx);
            Integer level = this.levelArg.get(ctx);
            return onWorld(ctx, (sender, world, pos, yaw) -> {
                GuardType type = GuardType.fromArg(rank);
                if (type == null) { sender.sendMessage(Message.raw("Rang inconnu : " + rank + " (" + GuardType.args().replace("|", ", ") + ")")); return; }
                AdventurerType advType = AdventurerType.fromArg(typeName);
                AdventurerTier advTier = AdventurerTier.fromArg(tierName);
                if ((type == GuardType.CHASSEUR_PRIME || type == GuardType.CRIMINEL) && advType != null && !BountyHunter.isHunterType(advType)) {
                    sender.sendMessage(Message.raw(type.getLabel() + " : guerrier, archer ou assassin.")); return;
                }
                NpcTrades.Metier metier = null;
                NpcTrades.Rang tradeRang = null;
                LegendarySmiths.Smith smith = null;
                LuoMerchants.Shop shop = null;
                LuoMerchants.Rank merchantRank = null;
                if (type == GuardType.COMMERCANT) {                                              // --type metier
                    metier = NpcTrades.ofArg(typeName);
                    if (typeName != null && metier == null) { sender.sendMessage(Message.raw("Métier inconnu : " + typeName + " (" + java.util.Arrays.stream(NpcTrades.Metier.values()).map(m -> m.name().toLowerCase()).toList() + ")")); return; }
                    tradeRang = NpcTrades.rangArg(tierName);
                    if (tierName != null && tradeRang == null) { sender.sendMessage(Message.raw("Rang inconnu : " + tierName + " (apprenti, compagnon, maitre)")); return; }
                    advType = null;
                    advTier = null;
                } else if (type == GuardType.LEGENDAIRE) {                                       // --type humain|nain|elfe
                    smith = LegendarySmiths.ofArg(typeName);
                    if (typeName != null && smith == null) { sender.sendMessage(Message.raw("Forgeron inconnu : " + typeName + " (humain, nain, elfe)")); return; }
                    advType = null;
                    advTier = null;
                } else if (type == GuardType.MARCHAND) {                                         // --type boutique, --statut rang
                    shop = LuoMerchants.shopArg(typeName);
                    merchantRank = LuoMerchants.rankArg(tierName);
                    if (typeName != null && shop == null) { sender.sendMessage(Message.raw("Boutique inconnue : " + typeName + " (armurerie, tailleur, herboriste, maison_the)")); return; }
                    if (tierName != null && merchantRank == null) { sender.sendMessage(Message.raw("Rang inconnu : " + tierName + " (apprenti, negociant, grand)")); return; }
                    advType = null;
                    advTier = null;
                } else {
                    if (typeName != null && advType == null) { sender.sendMessage(Message.raw("Type inconnu : " + typeName + " (guerrier, mage, archer, assassin)")); return; }
                    if (tierName != null && advTier == null) { sender.sendMessage(Message.raw("Statut inconnu : " + tierName + " (ordinaire, exceptionnel, heros)")); return; }
                }
                if (metier == NpcTrades.Metier.INTENDANT) {                                      // un seul intendant par territoire
                    GuardManager.Post other = NpcTrades.stewardOn(world.getName(), pos.x, pos.y, pos.z);
                    if (other != null) { sender.sendMessage(Message.raw("Ce territoire a déjà son intendant (" + other.getId() + ").")); return; }
                }
                String preset = smith != null ? LegendarySmiths.value(smith)
                        : tradeRang != null && metier != null ? NpcTrades.value(metier, tradeRang) : null;
                GuardManager.Post p = GuardManager.place(type, world, pos, yaw, advType, advTier, level != null ? level : 0, shop, merchantRank, metier, preset);
                if (tradeRang != null && metier == null && NpcTrades.metier(p) != null) { p.shop = NpcTrades.value(NpcTrades.metier(p), tradeRang); GuardManager.saveLater(); }
                sender.sendMessage(Message.raw(type.getLabel() + " poste ici (" + p.getId() + ")."
                        + switch (type) {
                    case PATROUILLEUR -> " Il fait sa ronde autour de ce point.";
                    case CHEVALIER_ERRANT -> " Il erre dans 200 blocs autour de ce point.";
                    case CHASSEUR_PRIME -> " " + GuardManager.nameplate(p) + " (" + AdventurerType.of(p.getSkinSeed()).getLabel()
                            + ", niveau " + p.level + ") erre dans 200 blocs autour de ce point et traque les PK.";
                    case MAITRE_GUILDE -> " Parlez-lui (touche d'interaction) pour fonder, rejoindre ou améliorer une guilde.";
                    case CRIMINEL -> " " + GuardManager.nameplate(p) + " (" + AdventurerType.of(p.getSkinSeed()).getLabel()
                            + ", niveau " + p.level + ") rôde autour de ce repaire. Prime : " + Criminals.bounty(p) + " or.";
                    case VILLAGEOIS -> " " + GuardManager.nameplate(p) + " se promene dans 30 blocs autour de ce point.";
                    case ASSASSIN -> " Il garde ce poste, dagues en main.";
                    case DISCIPLE -> " Il s'exerce au kata a ce poste.";
                    case COMMERCANT -> " " + GuardManager.nameplate(p) + " tient boutique ici.";
                    case AMBULANT -> " " + GuardManager.nameplate(p) + " vend un peu de tout, a tout le monde (assortiment renouvele chaque jour).";
                    case MARCHAND -> " " + GuardManager.nameplate(p) + " tient boutique ici (" + LuoMerchants.rankOf(p).getLabel()
                            + ") : membres du clan selon leur rang, Comptoir pour les marchands agrees.";
                    case AVENTURIER -> " " + GuardManager.nameplate(p) + " (niveau " + p.level + ", "
                            + AdventurerTier.of(p).getLabel()
                            + (AdventurerTier.classOf(p) != null ? ", " + AdventurerTier.classOf(p).getDisplayName() : "")
                            + "), recrutable pour " + GuardManager.priceOf(p) + " or.";
                    default -> " Il revient toujours a ce poste.";
                } + (p.isLuo() ? " Territoire du Clan Luo : il en porte la tenue et attaque les étrangers sans plaque." : "")));
            });
        }
    }

    private static final class Remove extends AbstractAsyncCommand {
        Remove() { super("retirer", "Retire le poste de garde le plus proche (6 blocs)"); }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            return onWorld(ctx, (sender, world, pos, yaw) -> {
                GuardManager.Post p = GuardManager.removeNearest(world, pos);
                sender.sendMessage(Message.raw(p != null ? p.getType().getLabel() + " retire (" + p.getId() + ")."
                        : "Aucun poste de garde a moins de 6 blocs."));
            });
        }
    }

    private static final class ListPosts extends AbstractAsyncCommand {
        ListPosts() { super("liste", "Postes de garde et etat des gardes"); }

        protected boolean canGeneratePermission() { return false; }

        @Nonnull
        @Override
        public CompletableFuture<Void> executeAsync(@Nonnull CommandContext ctx) {
            return onWorld(ctx, (sender, world, pos, yaw) -> {
                if (GuardManager.getPosts().isEmpty()) { sender.sendMessage(Message.raw("Aucun poste de garde.")); return; }
                long now = System.currentTimeMillis();
                for (GuardManager.Post p : GuardManager.getPosts()) {
                    Vector3d c = p.position();
                    String state = p.isAlive() ? "en poste" : p.getRespawnAt() > 0
                                                              ? "tombe, retour dans " + Math.max(0, (p.getRespawnAt() - now) / 1000) + " s" : "a faire apparaitre";
                    String label = p.getType() == GuardType.AVENTURIER || p.getType() == GuardType.CHASSEUR_PRIME || p.getType() == GuardType.CRIMINEL
                                   ? GuardManager.nameplate(p) : p.getType().getLabel();
                    sender.sendMessage(Message.raw(p.getId() + " - " + label + " - " + p.getWorld() + " "
                            + (int) c.x + " " + (int) c.y + " " + (int) c.z + " - " + state));
                }
            });
        }
    }
}