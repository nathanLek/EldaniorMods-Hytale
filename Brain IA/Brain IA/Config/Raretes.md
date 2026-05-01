# Systeme de Rarete

#rarete #items #classes #couleurs

## 6 Niveaux de rarete

| Rarete | Code couleur | Hex | Poids gacha | Utilisation |
|--------|-------------|-----|-------------|-------------|
| **COMMON** | §f Blanc | #E0E0E0 | 50000 (50%) | Classes T1, skills basiques |
| **RARE** | §9 Bleu | #3498DB | 33333 (33%) | Classes evoluees, skills intermediaires |
| **EPIC** | §5 Violet | #9B59B6 | 6666 (6.6%) | Classes puissantes, items speciaux |
| **UNIQUE** | §e Jaune | #E74C3C | 500 (0.5%) | Classes rares, items uniques |
| **LEGENDARY** | §6 Orange | #F1C40F | 125 (0.125%) | Classes mythiques, items legendaires |
| **DIVINE** | §c Rouge/Cyan | #00FFFF | 20 (0.02%) | Classes divines, items ultimes |

## Utilisation par systeme
- **Classes** : determine la rarete de l'evolution (gacha pondere)
- **Skills** : determine la difficulte a trouver les parchemins
- **Items** : determine la couleur dans l'inventaire (Quality dans le JSON)
- **Titres** : determine la rarete d'obtention
- **Loot** : determine le poids dans les tables de drop

## Fichier cle
- `config/configs/Rarity.java` - Enum

## Liens
- [[Systems/Classes]] - Gacha d'evolution
- [[Config/LootTables]] - Probabilites de drop