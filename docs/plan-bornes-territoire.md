# Borne de territoire — validée et codée le 2026-11-10 (compilé, assets chargés sur serveur isolé, non vu en jeu)

But : créer et régler à la main les zones du royaume (Royaume, Grand territoire, Territoire, Ville) avec un bloc, au lieu des
commandes. Parcelles, maisons, appartements restent sur commande.

- **Types** (rappel du 2026-11-10) : royaume, territoire, ville. Un royaume posé par borne revient à la famille Eldanior et crée
  ses 4 marquisats (grands territoires Zippel, Runkandel, Luo, Valmontis, dans les coins comme /es pcreate KINGDOM).
- **Carte** : bordures seules (plus de hachures), cercle pour un cylindre, redessinée après chaque création, modification ou suppression.
- **Annonce** à l'entrée réglable par zone (Oui / Non, passe avant le silence des territoires de l'Empire Luo).
- **Bloc** `Eldanior_Borne_Territoire` (admins, invisible et traversable, `scripts/territoire/gen_borne.py`) : le poser ou F dessus
  ouvre la fenêtre `Territoire/Borne.ui` (`territory/borne/BornePage`). La borne est le **centre** de la zone.
- **Fenêtre** : type, nom, famille ; forme **rectangle** (demi-largeurs est-ouest / nord-sud) ou **cylindre** (rayon), hauteurs min/max ;
  PvP, protection par défaut, **régénération** (secondes avant qu'un bloc cassé revienne, 0 = non ; `FarmRegenSystem`). Boutons :
  Créer / mettre à jour, Lier la zone existante, Supprimer la zone.
- **La borne doit rester posée** : la casser supprime la zone (sous-zones remontées au parent) ; si elle disparaît autrement, la zone
  est supprimée après deux constats à 10 s, chunk chargé (`Bornes.checkStanding`). Les zones créées par commande ne changent pas,
  sauf si on les lie à une borne.
- **Contrôles** : la zone tient dans son parent (plus petite zone qui contient la borne et peut l'accueillir, `ParcelManager.canContain`),
  ne chevauche pas une sœur du même type, garde ses sous-zones dedans ; le type d'une zone qui a des sous-zones ne change pas.
  Cylindre : contrôles sur son carré englobant ; la carte du monde le dessine encore comme un carré.
- **Marques** : en tenant une borne, les admins voient une colonne de points sur chaque borne et les bords des zones proches (48 blocs).
- Données : `ParcelData.cylinder`, `regen`, `borne` (« monde|x|y|z »), sauvegardés dans parcels.properties.

## 2026-11-19 — zones larges et contour

- **Types posés par borne** : royaume, grand territoire, territoire, ville, forêt, arène, parcelle (`Bornes.TYPES`). Les zones
  précises (logement, chambre, ferme, mine, donjon) restent à la main, bloc par bloc.
- **Contour** : en tenant une borne, chaque bloc du bord de toutes les zones à 40 blocs est entouré d'un cube en fil de fer
  (`ZoneOutline`, paquet `DisplayDebug`, sans face pleine), couleur du type ; 900 cubes au plus, les petites zones d'abord.
- Frontières qui bougent (zones qui se recouvrent) : `docs/plan-frontieres-dynamiques.md`.
