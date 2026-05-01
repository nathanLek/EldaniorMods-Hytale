# Rarites et Distribution des Skills

#skills #rarete #distribution #loot

## 8 Niveaux de rarete

| Rarete | Nombre | Couleur | Cout mana typique |
|--------|--------|---------|-------------------|
| Common | ~50 | §f Blanc | 0-5 |
| Uncommon | ~50 | §a Vert | 5-10 |
| Rare | ~50 | §9 Bleu | 10-15 |
| Epic | ~50 | §5 Violet | 15-25 |
| Unique | ~40 | §c Rouge | 25-30 |
| Legendary | ~30 | §6 Or | 25-35 |
| Divine | ~25 | §b Cyan | 35-40 |
| Family | 9 | Special | Variable |

## Family Skills (exclusifs aux familles nobles)
| Famille | Skill | Rarete famille |
|---------|-------|---------------|
| Eldanior | FAMILY_ROYAL_AUTHORITY | Divine |
| Drakenhart | FAMILY_DRAGON_FURY | Legendary |
| Luminara | FAMILY_LUMINOUS_BLESSING | Legendary |
| Ashford | FAMILY_PHOENIX_BLOOD | Legendary |
| Valmontis | FAMILY_ARCANE_SUPREMACY | Legendary |
| Frostguard | FAMILY_FROST_RESILIENCE | Epic |
| Ironveil | FAMILY_IRON_WILL | Epic |
| Shadowmere | FAMILY_SHADOWMERE | Epic |
| Stormcrest | FAMILY_STORM_MASTERY | Epic |

## Distribution dans les coffres
- Common/Uncommon → Coffres Default et Or
- Rare/Epic → Coffres Or et Legendaire
- Unique/Legendary → Coffres Legendaire uniquement
- Divine → Coffres Legendaire (0.02% chance)
- Family → Non lootables (automatiques a la famille)

## Fichiers cles
- `skills/skillsInteraction/PassiveSkill.java` — enum avec rarete
- `config/LootTables/` — tables de drop

## Liens
- [[Categories de Skills]] - Les 11 categories
- [[Apprentissage de Skills]] - Parchemins
- [[../Skills]] - Vue d'ensemble
