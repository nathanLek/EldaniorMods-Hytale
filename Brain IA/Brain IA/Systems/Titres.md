# Systeme de Titres

#titres #achievement #bonus

## Categories de titres
| Categorie | Description |
|-----------|-------------|
| Combat | Obtenus via les kills (mobs, PvP) |
| Exploration | Obtenus via les coffres decouverts |
| Social | Obtenus via les interactions (guilde, famille) |
| Craft | Obtenus via l'artisanat |
| Quest | Obtenus en completant des quetes |
| Special | Obtenus par des conditions speciales |

## Structure d'un titre
```java
TitleModel {
    id: "dragon_slayer"
    displayName: "Tueur de Dragons"
    description: "A terrasse 100 dragons"
    rarity: LEGENDARY
    category: COMBAT
    bonus: TitleBonus (stats bonus)
    effects: [TitleEffect] (effets vs type de mob)
}
```

## Bonus de titre
Les titres peuvent donner :
- Bonus de stats (force, vitalite, etc.)
- Bonus de degats vs un type de mob specifique
- Reduction de degats recus d'un type de mob

## Titre equipe
- Le joueur peut equiper UN titre a la fois
- Affiche dans le profil et le nameplate
- Le titre "novice" est le defaut

## Gestion dans le GUI
- Onglet **Titres** dans le SystemScreen
- **30 slots max** (`MAX_TITLE_SLOTS = 30`)
- Liste des titres debloques avec bouton **EQUIPE** (titre actif) / **EQUIPER** (titre inactif)
- Formatage des bonus : `"FOR +X | VIT +Y"`

## Deblocage
- Automatique : `TitleCheckSystem` verifie les conditions **toutes les 1 seconde** (polling, pas evenementiel — cause du delai de deblocage pouvant aller jusqu'a 1s)
- Manuel : via commande admin `/es titleadmin grant <joueur> <titleId>`
- Quete : recompense de quete

## Fichiers cles
- `titles/TitleManager.java` - Registre de tous les titres
- `titles/models/TitleModel.java` - Modele abstrait
- `titles/models/TitleBonus.java` - Bonus de stats
- `titles/models/TitleEffect.java` - Effets speciaux
- `titles/definitions/` - Definitions des titres
- `titles/enums/` - Categories et enums
- `gui/tabs/TitresTab.java` - Onglet GUI

## Note architecturale
Au niveau code, les systemes **Noblesse**, **Eglise** et **Familles** resident dans le package `titles/` (sous-packages `titles/nobility/`, `titles/church/`, `titles/nobility/family/`). Ce sont des "titres" au sens architectural, mais documentes comme systemes independants dans le Brain IA.

## Liens
- [[Systems/Noblesse]] - Rangs de noblesse (package titles/nobility/)
- [[Systems/Eglise]] - Rangs religieux (package titles/church/)
- [[Systems/Familles]] - Familles nobles (package titles/nobility/family/)
- [[Systems/Quetes]] - Titres en recompense
