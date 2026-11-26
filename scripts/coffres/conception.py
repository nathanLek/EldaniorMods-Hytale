"""Conception des coffres au tresor (refonte du 2026-11-25, deuxieme version le meme jour).

Demandes de l'utilisateur (2026-11-25) :
  - tous les coffres dans la categorie creative « Donjons & Keys » ;
  - 1 objet minimum, 3 au maximum avec beaucoup de chance ; pourcentages reduits ;
  - Coffre au tresor (defaut) : ressources, minerais, plantes, nourriture ;
  - Coffre de donjon commun : minerais, argent, butins de monstres, competences actives et passives communes ;
  - Coffre de donjon : armures, armes, potions, pilules, competences Inhabituel a Epique ;
  - Coffre legendaire : artefacts, competences Unique a Divin, potions, pilules, consommables tres rares ;
  - le butin qui manquait : reparti selon sa rarete et sa difficulte (armures et armes Legendaires/Divines, minerai Divin,
    nourritures et metiers rares : coffre de donjon ou legendaire, en petite part).

Les objets de chaque coffre sont tires du CATALOGUE du mod (economie.json + qualite des objets), par famille et par rang ;
plus de LootTableConfig pour ces coffres (coffres d'or et a cle de boss : inchanges). Tirage (ChestLoot.java) :
  1 objet ; un 2e avec la chance « second » du coffre + la moitie de la Chance du joueur (stat Butin, %) ; un 3e avec le
  quart de la Chance du joueur. Pour chaque objet : une famille (poids), un rang (poids, parmi ceux que la famille a), un
  objet de ce rang. Chance du joueur / 200 : le rang monte d'un cran.

Rang = qualite : Common 1, Uncommon 2, Rare 3, Epic 4, Unique 5, Legendary 6, Divin 7 ; page de passif = son rang.
"""

# Familles (voir gen_coffres.py, famille_de) : ressource, minerai, nourriture, butin, pieces, page, manuel, metier, arme,
# armure, artefact, potion, pilule, rare (consommables tres rares : elixirs, tomes, essence).

# bornes = rangs permis par famille dans ce coffre (absent : tous les rangs)
COFFRES = {
    "default": dict(
        nom="Coffre au trésor", recharge=900, second=30,
        familles=dict(ressource=40, nourriture=35, minerai=25),
        rangs={1: 75, 2: 20, 3: 5}),
    "donjon_common": dict(
        nom="Coffre de donjon (commun)", recharge=2880, second=30,
        familles=dict(butin=30, minerai=24, pieces=24, page=10, manuel=8, metier=4),
        rangs={1: 70, 2: 25, 3: 5},
        bornes=dict(page=(1, 1), manuel=(1, 1), metier=(1, 2), pieces=(1, 3))),
    "donjon": dict(
        nom="Coffre de donjon", recharge=3600, second=35,
        familles=dict(arme=24, armure=24, potion=15, pilule=9, page=12, manuel=10, nourriture=3, metier=3),
        rangs={1: 12, 2: 40, 3: 32, 4: 14, 5: 2},
        bornes=dict(page=(2, 4), manuel=(2, 4), arme=(1, 5), armure=(1, 5), nourriture=(3, 5), metier=(3, 4))),
    "donjon_boss": dict(
        nom="Coffre de boss", recharge=7200, second=50,
        familles=dict(arme=22, armure=22, page=15, manuel=15, potion=10, pilule=8, butin=8),
        rangs={2: 10, 3: 35, 4: 35, 5: 15, 6: 5},
        bornes=dict(page=(3, 5), manuel=(3, 5), arme=(2, 6), armure=(2, 6))),
    "legendary": dict(
        nom="Coffre légendaire", recharge=21600, second=25,
        familles=dict(page=24, manuel=24, artefact=16, potion=9, pilule=9, rare=8, armure=4, arme=2, nourriture=2, minerai=1, metier=1),
        rangs={3: 10, 4: 30, 5: 38, 6: 20, 7: 2},
        bornes=dict(page=(5, 7), manuel=(5, 7), potion=(3, 7), pilule=(3, 7), armure=(6, 7), arme=(6, 7), nourriture=(5, 7),
                    minerai=(6, 7), metier=(5, 7))),
    # coffres des failles-donjons (rangs F, E, D, C)
    "donjon_tier1": dict(
        nom="Coffre de faille (F)", recharge=2880, second=30,
        familles=dict(butin=25, minerai=20, pieces=20, arme=10, armure=10, page=8, manuel=5, metier=2),
        rangs={1: 72, 2: 24, 3: 4},
        bornes=dict(page=(1, 1), manuel=(1, 1), pieces=(1, 3))),
    "donjon_tier2": dict(
        nom="Coffre de faille (E)", recharge=2880, second=30,
        familles=dict(arme=18, armure=18, butin=18, pieces=14, potion=10, page=10, manuel=8, metier=4),
        rangs={1: 48, 2: 38, 3: 12, 4: 2},
        bornes=dict(page=(1, 2), manuel=(1, 2), pieces=(1, 4))),
    "donjon_tier3": dict(
        nom="Coffre de faille (D)", recharge=3600, second=35,
        familles=dict(arme=22, armure=22, potion=14, pilule=8, butin=10, page=13, manuel=11),
        rangs={2: 40, 3: 40, 4: 17, 5: 3},
        bornes=dict(page=(2, 3), manuel=(2, 3))),
    "donjon_tier4": dict(
        nom="Coffre de faille (C)", recharge=3600, second=40,
        familles=dict(arme=22, armure=22, potion=12, pilule=10, butin=8, page=14, manuel=12),
        rangs={3: 40, 4: 40, 5: 17, 6: 3},
        bornes=dict(page=(3, 4), manuel=(3, 4))),
}

# Pieces par rang : (piece, min, max)
PIECES = {
    1: ("Elda_Copper_Coins", 10, 40),
    2: ("Elda_Silver_Coins", 3, 12),
    3: ("Elda_Gold_Coins", 2, 6),
    4: ("Elda_Gold_Coins", 6, 15),
}

# Quantites par famille : (min, max) ; absent = 1
QUANTITES = {"ressource": (1, 4), "minerai": (1, 3), "nourriture": (1, 3), "butin": (1, 2), "potion": (1, 2)}

# Qualites jamais tirees (outils, debug, gabarits, dechets)
QUALITES_EXCLUES = {"Developer", "Debug", "Technical", "Template", "Tool", "Junk"}

# Objets du mod reserves (categorie creative) : jamais dans un coffre
CATEGORIES_EXCLUES = ("Eldanior_Familles", "Eldanior_Empire_Luo")
# Armes a feu (Mousquetaire seulement), formes temporaires d'armes, fourreaux
MOTS_EXCLUS = ("Mousquet", "Colt", "Handgun", "Rifle", "Sniper", "Canon", "Fourreau", "Degainee", "_Baionnette", "Black_Bess",
               "Green_Colt", "Blue_Tiger", "Halloween")
