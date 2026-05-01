# Familles Nobles

#familles #noblesse #passifs #blasons

## 9 Familles organisees par tier

### Famille Royale (Rang requis : ROI)
| Famille | Rarete | Devise | Passif |
|---------|--------|--------|--------|
| **Eldanior** | Divine | "Par le sang et la couronne, nous regnons" | FAMILY_ROYAL_AUTHORITY |

### Familles Ducales (Rang requis : DUC)
| Famille | Rarete | Devise | Passif |
|---------|--------|--------|--------|
| **Frostguard** | Epic | "L'hiver ne nous brise pas, il nous forge" | FAMILY_FROST_RESILIENCE |
| **Ironveil** | Epic | "Notre volonte est d'acier, notre honneur indestructible" | FAMILY_IRON_WILL |
| **Shadowmere** | Epic | "L'ombre nous protege, les tenebres nous guide" | FAMILY_SHADOWMERE |
| **Stormcrest** | Epic | "La tempete obeit a notre commandement" | FAMILY_STORM_MASTERY |

### Familles de Marquis (Rang requis : MARQUIS)
| Famille | Rarete | Devise | Passif |
|---------|--------|--------|--------|
| **Ashford** | Legendary | "De la cendre, nous renaissons plus forts" | FAMILY_PHOENIX_BLOOD |
| **Drakenhart** | Legendary | "Le feu du dragon coule dans nos veines" | FAMILY_DRAGON_FURY |
| **Luminara** | Legendary | "Lumiere eternelle, sagesse infinie" | FAMILY_LUMINOUS_BLESSING |
| **Valmontis** | Legendary | "Par la magie et la science, nous conquerons" | FAMILY_ARCANE_SUPREMACY |

## Mecaniques
- Chaque famille a un **rang minimum** pour y entrer
- Le **blason** est affiche dans le nameplate : `Nom §7Von §6FamilyName`
- Les **passifs de famille** sont des skills automatiques pour les membres
- Un joueur avec une famille noble **ne peut PAS** rejoindre de guilde
- Chaque famille a une **tresorerie** partagee

## Tresorerie de famille
- Tresorerie de depart selon le rang de la famille
- Alimentee par les impots des territoires geres par la famille
- Accessible aux membres via le GUI Famille

## Fichiers cles
- `titles/nobility/family/definitions/` — definitions par tier
- `titles/nobility/family/FamilyManager.java` — gestion des familles
- `titles/nobility/family/NobleFamilyModel.java` — modele de base

## Liens
- [[../Classes/Arbre des classes]] - Classes avec skills de famille
- [[../Systems/Noblesse]] - Hierarchie feodale
- [[../Systems/Familles]] - Systeme complet