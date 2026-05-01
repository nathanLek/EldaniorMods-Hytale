# Economie - Inconsistance des Taxes

#balance #haute #economie #taxes

## Probleme principal
**Deux systemes differents** utilisent des pourcentages differents pour la meme distribution de taxes.

### Systeme 1 : ParcelEconomyManager.distributeTax()
**Fichier** : `territory/ParcelEconomyManager.java` lignes 36-102
| Destinataire | Pourcentage |
|-------------|------------|
| Ville | 15% |
| Duc | 20% |
| Marquis | 30% |
| Royaume | 35% |
| **Total** | **100%** |

### Systeme 2 : TerritoiresTab (Impots)
**Fichier** : `gui/tabs/TerritoiresTab.java` lignes 339-347
| Niveau | Pourcentage des enfants |
|--------|------------------------|
| Duc | 87.5% des enfants |
| Marquis | 80% des enfants |
| Royaume | 57% des enfants |

### Pourquoi c'est un probleme
- Un joueur paie une taxe → elle est distribuee avec les % du systeme 1
- Un noble collecte ses impots → il voit les % du systeme 2
- Les montants ne correspondent **jamais**

## Correction proposee
Unifier en un seul systeme de distribution. Garder le systeme 1 (plus clair) et adapter l'affichage GUI.

```java
// ParcelEconomyManager — source unique de verite
public static final float CITY_SHARE = 0.15f;
public static final float DUKE_SHARE = 0.20f;
public static final float MARQUIS_SHARE = 0.30f;
public static final float KINGDOM_SHARE = 0.35f;

// TerritoiresTab — utiliser les memes constantes
int taxes = (int)(totalChildTreasury * ParcelEconomyManager.DUKE_SHARE);
```

## Priorite
**HAUTE** — Confusion economique pour les joueurs

## Liens
- [[Systems/Economie]] - Systeme economique
- [[Features/Taxes Hebdomadaires]] - Taxes periodiques manquantes
- [[Systems/Territoires]] - Hierarchie des parcelles
