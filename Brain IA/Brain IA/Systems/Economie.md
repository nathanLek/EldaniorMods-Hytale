# Systeme Economique

#economie #taxe #impot #tresorerie

## Taxe de transaction (12%)
Chaque achat ou location preleve 12% de taxe, distribue dans la hierarchie :

### Repartition fixe sur le total de la taxe
| Niveau | Part |
|--------|------|
| Ville | 15% |
| Duc (1er territoire) | 20% |
| Marquis (2eme territoire) | 30% |
| Royaume | tout le reste (~35%) |

### Exemple sur une vente a 4000 Or
```
Acheteur paie 4000 Or
├── Vendeur recoit : 3520 Or (88%)
└── Taxe 480 Or (12%) :
    ├── Ville : 72 Or (15%)
    ├── Duc : 96 Or (20%)
    ├── Marquis : 144 Or (30%)
    └── Royaume : 168 Or (35%)
```

### Part du vendeur (88%)
- Si proprio **joueur** → va dans son compte
- Si proprio **ville** (pas de joueur) → va dans tresorerie de la ville

## Impots (bouton, cooldown 7j)
Chaque niveau prend un % de la tresorerie de ses enfants directs :

| Qui reclame | Taux | Sur |
|-------------|------|-----|
| Royaume | 57% | Tresorerie des Marquis |
| Marquis | 80% | Tresorerie des Ducs |
| Duc | 87.5% | Tresorerie des Villes |
| Ville | - | Pas d'impots |

### Exemple : Ville a 20 000 Or
```
Duc reclame 87.5% : 17 500 → Ville garde 2 500
Marquis reclame 80% du Duc : 14 000 → Duc garde 3 500
Royaume reclame 57% du Marquis : ~8 000 → Marquis garde ~6 000
```
**Resultat** : Ville 2 500 < Duc 3 500 < Marquis 6 000 < Royaume 8 000

## Transfert de tresorerie
- Bouton dans l'onglet Territoires (cooldown 7j)
- Transfere 50% de la tresorerie vers la **famille** ou la **guilde** associee
- La famille/guilde recoit l'argent dans sa propre tresorerie

## Monnaie
5 types de pieces (items consommables) :
- Elda Copper Coins
- Elda Silver Coins
- Elda Gold Coins
- Elda Diamond Coins
- Elda Zenith Coins

## Fichiers cles
- `territory/ParcelEconomyManager.java` - Distribution taxes + impots
- `gui/tabs/ProprietesTab.java` - Achat/vente/location GUI
- `gui/tabs/TerritoiresTab.java` - Impots + transfert GUI
- `config/configs/CoinItemRegistry.java` - Registre des pieces

## Liens
- [[Systems/Territoires]] - Parcelles et proprietes
- [[Systems/Consommables]] - Items economiques
- [[Systems/Noblesse]] - Qui collecte les impots
