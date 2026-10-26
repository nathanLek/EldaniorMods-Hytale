package com.eldanior.system.boards;

import com.eldanior.system.EldaniorSystem;
import com.eldanior.system.config.EldaniorLogger;
import com.eldanior.system.config.Player.PlayerLevelData;
import com.eldanior.system.guards.BountyHunter;
import com.eldanior.system.quest.QuestManager;
import com.eldanior.system.territory.ParcelData;
import com.eldanior.system.territory.ParcelManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tableau de prime :
 *   - Recherches : joueurs PK (meme sans prime) et criminels PNJ, de la plus forte prime a la plus faible. Fiche : niveau,
 *     derniere zone connue, prime ; ajouter de l'or a sa prime (montant + {@link BountyBoard#COMMISSION} de commission, au
 *     tresor de la ville du tableau). Bouton du bas : un joueur rachete sa prime ({@link BountyBoard#BUYBACK_FACTOR} fois son
 *     montant, au tresor de la ville), en deux clics.
 *   - Contrats : monstres d'elite du jour (MonsterContracts), zone, recompense, etat (en chasse / reclame par X).
 */
final class BountyBoardScreen extends BoardScreen {

    private final String world;
    private final Vector3d at;
    /** Lignes affichees (pour retrouver la cible d'un bouton). */
    private List<BountyBoard.Wanted> listed = List.of();
    private List<MonsterContracts.Contract> contracts = List.of();
    /** Fiche ouverte : cle du recherche, ou indice du contrat. */
    private String selected;
    private int selectedContract = -1;
    private long amount = 1_000;
    private boolean buyBackArmed;

    BountyBoardScreen(@Nonnull PlayerRef playerRef, String place, String world, Vector3d at) {
        super(playerRef, BoardType.PRIME, place);
        this.world = world;
        this.at = at;
    }

    // ===== Contenu =====

    @Override
    protected List<Stat> stats(Ref<EntityStore> ref, Store<EntityStore> store) {
        refresh(ref, store);
        long max = listed.stream().mapToLong(BountyBoard.Wanted::bounty).max().orElse(0);
        PlayerLevelData data = data(ref, store);
        long mine = data != null ? data.getBounty() : 0;
        long open = MonsterContracts.getContracts().stream().filter(c -> c.getClaimedBy() == null).count();
        return List.of(new Stat(String.valueOf(listed.size()), "Recherchés"),
                new Stat(max > 0 ? gold(max) : "—", "Plus forte prime"),
                new Stat(mine > 0 ? gold(mine) : data != null && data.isPK() ? "0" : "Aucune", "Votre prime"),
                new Stat(open + " / " + MonsterContracts.getContracts().size(), "Contrats en cours"));
    }

    /** Recherches a jour ; le joueur qui regarde y figure tout de suite s'il est PK (sans attendre le releve). */
    private void refresh(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        World w = store.getExternalData().getWorld();
        if (pr != null && w != null) BountyBoard.observe(pr, ref, store, w);
        listed = BountyBoard.list();
        contracts = new ArrayList<>(MonsterContracts.getContracts());
    }

    @Override
    protected Tab tab(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        return index == 1 ? contractsTab() : wantedTab(ref, store);
    }

    private Tab wantedTab(Ref<EntityStore> ref, Store<EntityStore> store) {
        UUID me = me(ref, store);
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < listed.size(); i++) {
            BountyBoard.Wanted w = listed.get(i);
            boolean mine = w.player() != null && w.player().equals(me);
            String meta = (w.isCriminal() ? "Criminel" : "Joueur") + "  ·  Niv. " + w.level() + "  ·  " + whereabouts(w);
            rows.add(new Row(String.valueOf(i + 1), w.name(), meta, gold(w.bounty()) + " or", bountyColor(w.bounty()),
                    !mine, mine, w.key().equals(selected)));
        }
        PlayerLevelData data = data(ref, store);
        long mine = data != null ? data.getBounty() : 0;
        String action = mine <= 0 ? null : buyBackArmed ? "Confirmer : payer " + gold(buyBack(mine)) + " or"
                                                        : "Racheter ma prime  ·  " + gold(buyBack(mine)) + " or";
        return new Tab("Avis de recherche", rows, "Aucun criminel n'est recherché. Le royaume dort tranquille.",
                "Abattez un recherché pour toucher sa prime. Les chasseurs de prime les traquent aussi.", action,
                "Joueurs PK et criminels du royaume, du plus recherché au moins recherché.\n\n"
                        + "Ouvrez une fiche pour voir où il a été aperçu et ajouter de l'or à sa prime (commission de "
                        + Math.round(BountyBoard.COMMISSION * 100) + " %, versée au trésor de la ville).\n\n"
                        + "Un PK peut racheter sa prime : " + BountyBoard.BUYBACK_FACTOR + " fois son montant.");
    }

    private Tab contractsTab() {
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < contracts.size(); i++) {
            MonsterContracts.Contract c = contracts.get(i);
            MonsterContracts.Zone z = c.zone();
            boolean claimed = c.getClaimedBy() != null;
            rows.add(new Row(String.valueOf(i + 1), c.getName(), c.getSpecies() + " d'élite  ·  Niv. " + c.getLevel() + "  ·  " + (z != null ? z.name() : "?"),
                    claimed ? "Réclamé" : gold(c.getReward()) + " or", claimed ? MUTED : GOLD, true, false, i == selectedContract));
        }
        return new Tab("Contrats du jour", rows, "Aucun contrat de monstre aujourd'hui.",
                "Nouveaux contrats chaque jour à minuit.", null,
                "Chaque jour, des monstres d'élite nommés sont mis à prix.\n\n"
                        + "Ils apparaissent dans leur zone quand un chasseur s'en approche, plus forts que les monstres du coin.\n\n"
                        + "Le premier qui en abat un touche la récompense.");
    }

    @Override
    protected Side side(Ref<EntityStore> ref, Store<EntityStore> store) {
        return tab == 1 ? contractSide() : wantedSide(ref, store);
    }

    private Side wantedSide(Ref<EntityStore> ref, Store<EntityStore> store) {
        BountyBoard.Wanted w = find(selected);
        if (w == null) { selected = null; return null; }
        long cost = cost(amount);
        PlayerLevelData data = data(ref, store);
        boolean enough = data != null && data.getMoney() >= cost;
        String text = "Prime : " + gold(w.bounty()) + " or\n" + where(w);
        if (w.isCriminal())
            text += "\n\nHors-la-loi. Il attaque les voyageurs (mais épargne les PK) ; gardes, chasseurs et aventuriers le combattent.";
        return new Side(w.name(), (w.isCriminal() ? "Criminel" : "Joueur PK") + "  ·  Niveau " + w.level(), text,
                "Ajouter à sa prime (minimum " + gold(BountyBoard.MIN_BOUNTY) + ")",
                "Coût : " + gold(amount) + " + " + Math.round(BountyBoard.COMMISSION * 100) + " % = " + gold(cost) + " or",
                enough ? GOLD : RED, "Placer " + gold(amount) + " or", "Fermer");
    }

    private Side contractSide() {
        if (selectedContract < 0 || selectedContract >= contracts.size()) { selectedContract = -1; return null; }
        MonsterContracts.Contract c = contracts.get(selectedContract);
        MonsterContracts.Zone z = c.zone();
        String state = c.getClaimedBy() == null ? "En chasse : le premier qui l'abat touche la prime."
                       : "Réclamé par " + c.getClaimedBy() + " " + ago(c.getClaimedAt()) + ".";
        String text = "Récompense : " + gold(c.getReward()) + " or\n"
                + (z != null ? "Zone : " + z.name() + "\nPosition : " + (int) z.pos().x + ", " + (int) z.pos().y + ", " + (int) z.pos().z + "\n" : "")
                + "\n" + state;
        return Side.info(c.getName(), c.getSpecies() + " d'élite  ·  Niveau " + c.getLevel(), text);
    }

    // ===== Actions =====

    @Override
    protected void onRow(int index, Ref<EntityStore> ref, Store<EntityStore> store) {
        buyBackArmed = false;
        if (tab == 1) {
            if (index < contracts.size()) selectedContract = index;
            return;
        }
        if (index >= listed.size()) return;
        BountyBoard.Wanted w = listed.get(index);
        if (w.player() != null && w.player().equals(me(ref, store))) return;
        selected = w.key();
        amount = 1_000;
        setAmount(amount);
    }

    @Override
    protected void onAmount(long value) { amount = Math.max(0, value); }

    @Override
    protected void onConfirm(long value, Ref<EntityStore> ref, Store<EntityStore> store) {
        amount = Math.max(0, value);
        BountyBoard.Wanted w = find(selected);
        if (w == null) { selected = null; error("Il n'est plus recherché."); return; }
        if (amount < BountyBoard.MIN_BOUNTY) { error("Une prime est d'au moins " + gold(BountyBoard.MIN_BOUNTY) + " or."); return; }
        long cost = cost(amount);
        if (!pay(cost, ref, store)) return;
        if (!BountyBoard.addBounty(w, amount)) { refund(cost, ref, store); selected = null; error("Il n'est plus recherché."); return; }
        toTreasury(cost - amount);
        EldaniorLogger.info("[Boards] prime de " + amount + " or sur " + w.name() + " (cout " + cost + ")");
        ok("Prime de " + gold(amount) + " or placée sur " + w.name() + " (total " + gold(w.bounty() + amount) + " or).");
    }

    @Override
    protected void onCancel() { selected = null; selectedContract = -1; }

    @Override
    protected void onTab() { selected = null; selectedContract = -1; buyBackArmed = false; }

    /** Rachat de sa propre prime : premier clic pour armer, second pour payer. */
    @Override
    protected void onAction(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = data(ref, store);
        long bounty = data != null ? data.getBounty() : 0;
        if (bounty <= 0) { buyBackArmed = false; return; }
        long cost = buyBack(bounty);
        if (!buyBackArmed) {
            buyBackArmed = true;
            ok("Racheter votre prime coûte " + gold(cost) + " or. Cliquez à nouveau pour confirmer.");
            return;
        }
        buyBackArmed = false;
        PlayerLevelData copy = (PlayerLevelData) data.clone();
        if (copy == null) return;
        if (!copy.removeMoney(cost)) { error("Il vous manque " + gold(cost - data.getMoney()) + " or pour racheter votre prime."); return; }
        copy.collectBounty();
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
        QuestManager.unregisterBounty(me(ref, store));
        toTreasury(cost);
        BountyBoard.save();
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        String name = pr != null ? pr.getUsername() : "Un criminel";
        BountyBoard.announce("[Prime] " + name + " a racheté sa prime pour " + gold(cost) + " or.");
        EldaniorLogger.info("[Boards] " + name + " rachete sa prime (" + cost + " or)");
        ok("Votre prime est effacée. Tant que vous êtes PK, les chasseurs de prime vous traquent toujours.");
    }

    // ===== Outils =====

    /** Retire l'or du joueur ; false (message d'erreur) s'il n'en a pas assez. */
    private boolean pay(long cost, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = data(ref, store);
        PlayerLevelData copy = data != null ? (PlayerLevelData) data.clone() : null;
        if (copy == null || !copy.removeMoney(cost)) {
            error("Il vous manque " + gold(cost - (data != null ? data.getMoney() : 0)) + " or.");
            return false;
        }
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
        return true;
    }

    private void refund(long gold, Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerLevelData data = data(ref, store);
        PlayerLevelData copy = data != null ? (PlayerLevelData) data.clone() : null;
        if (copy == null) return;
        copy.addMoney(gold);
        store.putComponent(ref, EldaniorSystem.get().getPlayerLevelDataType(), copy);
    }

    /** Or verse au tresor de la ville du tableau (perdu hors des villes). */
    private void toTreasury(long gold) {
        ParcelData town = world != null ? BountyHunter.treasuryAt(world, at) : null;
        if (town == null || gold <= 0) return;
        town.addTreasury(gold);
        ParcelManager.save();
    }

    private static long cost(long amount) {
        return amount + (long) Math.ceil(amount * BountyBoard.COMMISSION);
    }

    private static long buyBack(long bounty) {
        return bounty * BountyBoard.BUYBACK_FACTOR;
    }

    private BountyBoard.Wanted find(String key) {
        if (key == null) return null;
        for (BountyBoard.Wanted w : listed) if (w.key().equals(key)) return w;
        return null;
    }

    /** Ligne de la liste : en ligne, abattu, ou vu il y a... */
    private static String whereabouts(BountyBoard.Wanted w) {
        if (w.isCriminal()) return "en liberté";
        if (w.online()) return (w.zone() != null ? w.zone() : "position inconnue") + "  ·  en ligne";
        return w.zone() == null ? "jamais aperçu" : "vu " + ago(w.seenAt());
    }

    /** Fiche : derniere zone connue. */
    private static String where(BountyBoard.Wanted w) {
        if (w.isCriminal()) return w.zone();
        if (w.zone() == null) return "Jamais aperçu.";
        return (w.online() ? "En ce moment : " : "Vu pour la dernière fois " + ago(w.seenAt()) + " : ") + w.zone();
    }

    private static String ago(long at) {
        long minutes = (System.currentTimeMillis() - at) / 60_000;
        return minutes < 1 ? "à l'instant" : minutes < 60 ? "il y a " + minutes + " min"
               : minutes < 48 * 60 ? "il y a " + minutes / 60 + " h" : "il y a " + minutes / 1440 + " j";
    }

    /** Couleur du montant : or, orange au-dela de 10 000, rouge au-dela de 100 000. */
    private static String bountyColor(long bounty) {
        return bounty >= 100_000 ? "#ff5a4f" : bounty >= 10_000 ? "#ff9f43" : GOLD;
    }

    private static UUID me(Ref<EntityStore> ref, Store<EntityStore> store) {
        PlayerRef pr = store.getComponent(ref, PlayerRef.getComponentType());
        return pr != null ? pr.getUuid() : null;
    }
}
