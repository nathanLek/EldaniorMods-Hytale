# Familles Nobles

#famille #noblesse #blason #passif

## Fonctionnement
- Les nobles de rang **Duc+** peuvent fonder ou rejoindre une famille
- Chaque famille a un **blason**, un **passif unique** et une **tresorerie**
- Le Patriarch gere la famille

## Roles
| Role | Description |
|------|-------------|
| PATRIARCH | Chef de famille |
| VICE | Second |
| MEMBER | Membre |

## Tresorerie de depart
| Rang du fondateur | Tresorerie initiale |
|-------------------|-------------------|
| Roi | 1 500 000 Or |
| Marquis | 1 000 000 Or |
| Duc | 500 000 Or |

## Passifs de famille
Chaque famille a un passif unique herite par tous les membres :

| Famille | Passif | Effet |
|---------|--------|-------|
| Eldanior | Autorite Royale | Bonus degats evolutif |
| Drakenhart | Fureur Draconique | Bonus degats evolutif |
| Ashford | Sang du Phenix | Bonus PV max |
| Luminara | Lumiere Divine | Bonus mana |
| Valmontis | Fortune Doree | Bonus argent |
| Frostguard | Resilience du Givre | Reduction degats |
| Shadowmere | Frappe de l'Ombre | Bonus critiques |
| Stormcrest | Vigueur de la Tempete | Bonus dignite |
| Ironveil | - | - |

## Nameplate
Les membres d'une famille ont le suffixe : `§7Von §6NomDeFamille`

## Lien avec les Territoires
- Un territoire peut etre assigne a une famille
- La tresorerie du territoire peut etre transferee a la famille (50%, cooldown 7j)

## Fichiers cles
- `titles/nobility/family/FamilyManager.java` - Registre
- `titles/nobility/family/NobleFamilyModel.java` - Modele
- `titles/nobility/family/definitions/` - Definitions par rang
- `gui/tabs/FamilleTab.java` - Onglet GUI

## Liens
- [[Systems/Noblesse]] - Hierarchie des rangs
- [[Systems/Territoires]] - Territoires assignes aux familles
- [[Systems/Economie]] - Tresorerie et transferts