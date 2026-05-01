# Tables de Loot

#loot #coffres #drop #probabilites

## 5 Tables de loot

| Table | ID | Cooldown | Utilisation |
|-------|-----|----------|-------------|
| **Donjon** | donjon | 300s | Coffres de donjon (haut tier) |
| **Donjon Common** | donjon_common | 300s | Coffres de donjon (bas tier) |
| **Legendaire** | legendary | 300s | Coffres legendaires (toutes raretes) |
| **Or** | gold | 300s | Coffres monnaie uniquement |
| **Default** | default | 300s | Coffres du monde ouvert |

## Contenu par table

### Donjon (haut tier)
- Armures : Cobalt, Thorium, Mithril, Onyxium, Adamantite, Prisma
- Armes : tous les tiers
- Skills : Common (1%) → Divine (0.01%)
- Consommables : +1 (2%), +5 (0.3%), +10 (0.05%), Elixirs (0.2%)
- Cle de donjon (5%)

### Default (monde ouvert)
- Nourriture, plantes, ores de base
- Skills : Common (2%), Uncommon (1%)
- Consommables : +1 (1%), Tome XP (0.5%)
- Cle de donjon (0.02%)

### Legendaire
- Tous les skills de toutes les raretes
- Consommables : +1 (5%), +5 (1%), +10 (0.2%), Elixirs (0.5%)
- Tome Niveau (0.1%), Essence Dignite (0.02%)

### Or
- Uniquement des pieces : Copper → Zenith

## Systeme de loot
1. Le coffre a un `dropList` qui reference une table
2. `LootTableConfig.generateLoot(seed)` genere les items
3. Le nombre d'items depend de la **Chance** du joueur (1-2 base, +4 max a 3000 LCK)
4. Les items sont melanges aleatoirement dans les slots du coffre

## Fichier cle
- `config/configs/LootTableConfig.java` - Toutes les tables (~1600 lignes)

## Liens
- [[Systems/Consommables]] - Items dans les loot
- [[Config/Raretes]] - Probabilites par rarete
