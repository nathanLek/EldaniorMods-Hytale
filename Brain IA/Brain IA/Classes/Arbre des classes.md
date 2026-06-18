# Arbre des Classes

#classes #evolution #arbre

## Classe speciale hors-famille

**DragonAncestral** — classe unique enregistree en dehors de toutes les familles. Initialisee en premier dans `ClassManager.init()` avant les 5 bases. Fichier : `definitions/DragonAncestral.java`. Ne suit pas le systeme de gacha.

## Guerrier (Warrior)
```
Guerrier (Lvl 180)
├── Berserker (Common) → BerserkerEnrage, FureurSanglante, Devastateur
├── Duelliste (Common) → LameVirtuose, MaitreDArmes, Fleurettiste
├── Protecteur (Common) → GardienSacre, RempartVivant, Sentinelle
├── Brute (Common) → Ecraseur, BoucherDeGuerre, ColosseDeFer
├── Epeiste (Common) → LameCeleste, Trancheur, EscrimeurRoyal
├── Fantassin (Common) → Veteran, Centurion, Legionnaire
├── Mercenaire (Common) → CapitaineMercenaire, ChasseurDePrimes, LoupDeGuerre
├── AvantGarde (Rare) → Marechal, AvantGardeSupreme, FerDeLance
├── Bretteur (Rare) → LameFantome, VirtuoseDuSabre, DanseurDeLames
├── Gladiateur (Rare) → ChampionDeLArene, RoiGladiateur, Spartiate
├── LameMage (Rare) → EnchanteurDeGuerre, LameArcanique, ChevalierMystique
├── Paladin (Rare) → PaladinSacre, CroiseDivin, Justicier
├── Ravageur (Rare) → Destructeur, FleauDeGuerre, Annihilateur
├── Samourai (Rare) → Kensei, Shogun, RoninLegendaire
├── Templier (Rare) → GrandTemplier, Commandeur, Inquisiteur
├── Champion (Epic) → EluDesBatailles, Conquerant, Invaincu
├── ChevalierNoir (Epic) → SeigneurDesTenebres, CavalierDeLOmbre, TyranNoir
├── Colosse (Epic) → TitanDePierre, GolemVivant, Forteresse
├── Croise (Epic) → CroiseEternel, SaintGuerrier, MarteauDivin
├── Warlord (Epic) → EmpereurDeGuerre, StrategeSupreme, ConquerantUltime
├── Executeur (Unique) → BourrauSupreme, FaucheseDeGuerre, JugementFinal
├── GardienRunique (Unique) → ArchonRunique, GardienPrimordial, SageDeGuerre
├── Titan (Unique) → TitanOriginel, ColosseEternel, Ascendant
├── Fleau (Legendary) → Apocalypse, Cataclysme, Extinction
├── Heros (Legendary) → HerosMythique, LegendeVivante, Paragone
├── DivineApotre (Divine) → DemiDieu
└── SangDragon (Divine) → DemiDragon
```

## Mage
```
Mage (Lvl 180)
├── 7 Common (Elementaliste, Enchanteur, Necromancien, Invocateur, Guerisseur, Pyromancien, [[Cryomancien]])
├── 8 Rare (Archimage, Sorcier, Druide, Illusionniste, Mystique, Thaumaturge, Alchimiste, Sage)
├── 5 Epic (Magus, Liche, Oracle, MaitreElementaire, Chronoturge)
├── 3 Unique (Archonte, MageVoid, Primordial)
├── 2 Legendary (Demiurge, Prophete)
└── 2 Divine (AvatarArcanique → DemiArcane, DieuDesArcanes → DemiMage)
```
Chaque Tier 1 a 3 evolutions Tier 2 (sauf Divine → 1).

## Assassin
```
Assassin (Lvl 180)
├── 7 Common (Voleur, Rodeur, Sicaire, Eclaireur, Empoisonneur, Saboteur, Acrobate)
├── 8 Rare (MaitreLame, OmbreFurtive, Chasseur, Ninja, Espion, Corsaire, Traqueur, LameNoire)
├── 5 Epic (MaitreOmbre, Faucheur, GrandMaitrePoison, AngeDechu, PhantomBlade)
├── 3 Unique (LameEternelle, SeigneurPoison, Spectre)
├── 2 Legendary (EmpereurDesOmbres, OmbreSupreme)
└── 2 Divine (AvatarDuNeant → DemiNeant, DieuDesOmbres → DemiOmbre)
```

## Archer
```
Archer (Lvl 180)
├── 4 Common (Tireur, Chasseur, Arbaletrier, Eclaireur)
├── 4 Rare (Franc_Tireur, ArcMystique, MaitreChasse, RangerElite)
├── 3 Epic (GeneralArcher, SniperDivin, TireurElementaire)
├── 2 Unique (OeilDeFaucon, ArcAncien)
├── 1 Legendary (AvatarDeLArc)
└── 1 Divine (DieuDeLArc → DemiDieuArcher)
```

## Marchand
```
Marchand (Lvl 180)
├── 3 Common (Caravanier, Negociant, Prospecteur) → 1 evol. chacun
├── 4 Rare (Banquier, MasterArtisan, RelicHunter, Smuggler) → 1 evol. chacun
├── 3 Epic (BlackMarketPrince, GoldBaron, GuildMaster) → 1 evol. chacun
├── 3 Legendary (UnderworldKing, ProsperityAvatar, WorldForger) → 1 evol. chacun
└── 2 Divine (MarchandDivin → DemiMarchand, RoiDuCommerce → DemiCommercant)
```
Les marchands n'ont qu'**1 seule evolution** par classe (pas 3).

## Total
| Famille | Tier 1 | Tier 2 | Total |
|---------|--------|--------|-------|
| Guerrier | 27 | 77 | 104 |
| Mage | 27 | 76 | 103 |
| Assassin | 27 | 77 | 104 |
| Archer | 15 | 43 | 58 |
| Marchand | 15 | 15 | 30 |
| **Total** | **111** | **288** | **399 familles + 5 bases + Novice + DragonAncestral = 406** |

## Liens
- [[Systems/Classes]] - Details du systeme
- [[Systems/Skills]] - Competences par classe
