# Systeme de Coffres au Tresor

#coffres #loot #tresor #exploration

## Fonctionnement
1. Admin place un coffre et le configure avec `/es treasureconfig`
2. Le coffre est associe a une **table de loot** (donjon, default, gold, legendary)
3. Quand un joueur ouvre le coffre → loot genere aleatoirement
4. Cooldown par joueur (300s par defaut)
5. Le nombre d'items depend de la **Chance** du joueur

## Configuration par coffre
| Parametre | Description |
|-----------|-------------|
| canPlayerBreakLootChests | Les joueurs peuvent casser le coffre |
| isLootRandom | Loot aleatoire ou fixe |
| isMessageAppear | Notification a l'ouverture |
| isParticlesAppear | Particules visuelles |
| particlesColor | Couleur des particules (hex) |
| nextLootResetInterval | Intervalle de reset global |
| dropList | ID de la table de loot |

## Systeme de decouverte
- Chaque joueur a un `PlayerChestData` qui track les coffres visites
- `isDiscovered(x, y, z, world)` → deja ouvert ?
- `lastLootTime(x, y, z, world)` → cooldown
- La stat `chestsDiscovered` est incrementee dans le profil

## Detection de proximite
- `TreasureChestRangeSystem` detecte les joueurs proches des coffres
- Affiche des particules quand un coffre est a portee

## Protection
- Les coffres sont proteges contre la casse (BreakBlockEvent annule)
- Protection des blocs adjacents (empeche de casser le support)

## Commandes
```
/es treasureconfig   → Configure un coffre existant
/es deletetreasure   → Supprime la config d'un coffre
```

## Fichiers cles
- `TreasureChest/resources/TreasureChestTemplate.java` - Templates des coffres
- `TreasureChest/resources/TreasureChestConfig.java` - Config globale
- `TreasureChest/components/PlayerChestData.java` - Donnees par joueur
- `TreasureChest/events/*.java` - 4 event listeners (Interact, Break, Place, Damage)
- `TreasureChest/systems/TreasureChestRangeSystem.java` - Detection proximite
- `TreasureChest/systems/TreasureResetManager.java` - Reset periodique
- `TreasureChest/pages/TreasureChestConfigPage.java` - Page de configuration

## Liens
- [[Config/LootTables]] - Tables de drop
- [[Systems/Quetes]] - Quetes d'exploration
- [[Config/StatConfig]] - Stat Chance pour le loot
