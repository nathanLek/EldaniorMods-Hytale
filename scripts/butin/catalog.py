"""Catalogue des butins de monstres (liste validee le 2026-11-01).

F(section, slug, nom, regex, boss, niveau, couleurs, motif, items)
- regex : monstres couverts, sur l'identifiant de role sans prefixe Dungeon_/Coffer_ ni suffixe _Patrol/_Wander/_Surge...
- couleurs : trois teintes de la famille (le 1er item prend la 1re, etc. ; le trophee melange la famille et l'or)
- mineurs : regex des petites versions d'un boss : memes items aux taux normaux, sans trophee (table Drop_Butin_<slug>_Mineur)
- items : (nom, forme) ou (nom, forme, motif) ; ordre = commun, rare, precieux, puis trophee pour les boss.
  id="DragonHeart" reutilise un item existant au lieu d'en creer un.
"""

FAMILIES = []


def F(section, slug, nom, regex, boss, lvl, cols, pat, items, mineurs=None):
    FAMILIES.append(dict(section=section, slug=slug, nom=nom, regex=regex, boss=boss, lvl=lvl, cols=cols, pat=pat, items=items, mineurs=mineurs))


# ---------------- Dragons ----------------
F("Dragons", "Dragon_Feu", "Dragon de Feu", r"Dragon_Fire", True, 940, ("#b3261a", "#e8701e", "#ffc23a"), "lava", [
    ("Peau de Dragon de Feu", "hide", "scales"), ("Griffe de Dragon de Feu", "claw"), ("DragonHeart", "existing"),
    ("Corne du Dragon de Feu", "horn")])
F("Dragons", "Dragon_Givre", "Dragon de Givre", r"Dragon_Frost", True, 920, ("#2d7fb8", "#6fd0ee", "#c9f4ff"), "frost", [
    ("Peau de Dragon de Givre", "hide", "scales"), ("Griffe de Dragon de Givre", "claw"), ("Coeur de Dragon de Givre", "heart"),
    ("Corne du Dragon de Givre", "horn")])
F("Dragons", "Snapdragon", "Snapdragon", r"Snapdragon", True, 860, ("#3f9a4a", "#b7d13e", "#e8486c"), "scales", [
    ("Ecaille de Snapdragon", "scale"), ("Griffe de Snapdragon", "claw"), ("Coeur de Snapdragon", "heart"),
    ("Crete de Snapdragon", "crest")])

# ---------------- Dinosaures ----------------
F("Dinos", "Rex", "Rex des Cavernes", r"Rex_Cave", True, 805, ("#6e5a3a", "#a8703a", "#c8452e"), "scales", [
    ("Peau de Rex", "hide", "scales"), ("Griffe de Rex", "claw"), ("Coeur de Rex", "heart"), ("Machoire de Rex", "jaw")])
F("Dinos", "Raptor", "Raptor", r"Raptor_Cave", False, 770, ("#4f7a56", "#c9b06a", "#e8a020"), "stripes", [
    ("Peau de Raptor", "hide", "stripes"), ("Griffe de Raptor", "sickle"), ("Oeil de Raptor", "eye")])
F("Dinos", "Trillodon", "Trillodon", r"Trillodon", False, 785, ("#7a6a8c", "#c2b49a", "#c8506a"), "cracks", [
    ("Cuir de Trillodon", "hide", "cracks"), ("Corne de Trillodon", "horn"), ("Coeur de Trillodon", "heart")])
F("Dinos", "Pterodactyle", "Pterodactyle", r"Pterodactyl", False, 750, ("#a65f3c", "#6c7f8c", "#d8b23a"), "veins", [
    ("Membrane de Pterodactyle", "membrane"), ("Serre de Pterodactyle", "talon"), ("Bec de Pterodactyle", "beak")])
F("Dinos", "Archeopteryx", "Archeopteryx", r"Archaeopteryx", False, 745, ("#3a78a8", "#c28a3a", "#7fc26a"), "grain", [
    ("Plume d'Archeopteryx", "feather"), ("Serre d'Archeopteryx", "talon"), ("Oeuf d'Archeopteryx", "egg", "spots")])
F("Dinos", "Trilobite", "Trilobite", r"Trilobite(_Black)?", False, 740, ("#5a5068", "#3f8a8c", "#b08a52"), "rings", [
    ("Carapace de Trilobite", "carapace"), ("Antenne de Trilobite", "antenna"), ("Fossile de Trilobite", "fossil")])

# ---------------- Scaraks ----------------
F("Scaraks", "Scarak", "Scarak", r"Scarak_(Louse|Seeker|Fighter|Defender)", False, 800, ("#2f6e5c", "#7a9a38", "#d8c23a"), "metal", [
    ("Chitine de Scarak", "carapace"), ("Mandibule de Scarak", "pincer"), ("Glande de Scarak", "gland")])
F("Scaraks", "Garde_Royal", "Garde Royal Scarak", r"Scarak_Fighter_Royal_Guard", False, 845, ("#3a3f8c", "#6a4fc0", "#e0b040"), "metal", [
    ("Plaque Royale", "plate"), ("Lame de Chitine", "blade"), ("Insigne de la Ruche", "talisman")])
F("Scaraks", "Reine_Scarak", "Reine Scarak", r"Scarak_Broodmother(_Young)?", True, 865, ("#5a2a7a", "#b03a8c", "#58d0a0"), "metal", [
    ("Chitine de Reine", "carapace"), ("Dard de Reine", "stinger"), ("Oeuf de Reine", "egg", "veins"), ("Couronne de la Ruche", "crown")])

# ---------------- Golems ----------------
F("Golems", "Golem_Terre", "Golem de Terre", r"Golem_Crystal_Earth", False, 670, ("#7a5a3a", "#5f7a3a", "#c89a50"), "cracks", [
    ("Eclat de Terre", "shard"), ("Pierre Vive", "stone"), ("Noyau de Terre", "core")])
F("Golems", "Golem_Sable", "Golem de Sable", r"Golem_Crystal_Sand", False, 685, ("#d8b06a", "#e8d09a", "#c87a3a"), "grain", [
    ("Eclat de Sable", "shard"), ("Verre du Desert", "glass"), ("Noyau de Sable", "core")])
F("Golems", "Golem_Foudre", "Golem de Foudre", r"Golem_Crystal_Thunder", False, 695, ("#e0c83a", "#7a6ae0", "#c0e8ff"), "bolts", [
    ("Eclat de Foudre", "shard"), ("Cristal Charge", "cluster"), ("Noyau de Foudre", "core")])
F("Golems", "Golem_Givre", "Golem de Givre", r"Golem_Crystal_Frost", False, 710, ("#7ac8e8", "#a0e0f0", "#3a7fd0"), "frost", [
    ("Eclat de Givre", "shard"), ("Glace Eternelle", "cluster"), ("Noyau de Givre", "core")])
F("Golems", "Golem_Flamme", "Golem de Flamme", r"Golem_Crystal_Flame", False, 740, ("#e0552a", "#f09a2a", "#ffe066"), "lava", [
    ("Eclat de Flamme", "shard"), ("Cristal Ardent", "cluster"), ("Noyau de Flamme", "core")])
F("Golems", "Golem_Firesteel", "Golem de Firesteel", r"Golem_Firesteel", False, 725, ("#6a3a3a", "#b0522a", "#ff9a3a"), "metal", [
    ("Plaque de Firesteel", "plate"), ("Rouage Ardent", "gear"), ("Noyau de Firesteel", "core")])
F("Golems", "Gardien_Vide", "Gardien du Vide", r"Golem_Guardian_Void", True, 850, ("#3a2a5a", "#6a3ac0", "#d05ae0"), "void", [
    ("Plaque du Vide", "plate"), ("Rouage du Vide", "gear"), ("Noyau du Gardien", "core"), ("Sceau du Gardien", "seal")])

# ---------------- Esprits ----------------
F("Esprits", "Esprit_Racines", "Esprit des Racines", r"Spirit_Root", False, 600, ("#7a5a30", "#6ab03a", "#a8ffb0"), "grain", [
    ("Radicelle", "root"), ("Seve Spirituelle", "vial"), ("Ame des Racines", "soul")])
F("Esprits", "Esprit_Givre", "Esprit de Givre", r"Spirit_Frost", False, 615, ("#c8f0ff", "#6ab8f0", "#e8ffff"), "frost", [
    ("Flocon Spirituel", "snowflake"), ("Larme Gelee", "tear"), ("Ame de Givre", "soul")])
F("Esprits", "Esprit_Braise", "Esprit de Braise", r"Spirit_Ember", False, 635, ("#5a5050", "#f07a2a", "#ffd050"), "lava", [
    ("Cendre Spirituelle", "powder"), ("Braise Vive", "ember"), ("Ame de Braise", "soul")])
F("Esprits", "Esprit_Foudre", "Esprit de Foudre", r"Spirit_Thunder", False, 660, ("#f0e04a", "#8a7af0", "#ffffff"), "bolts", [
    ("Etincelle Spirituelle", "spark"), ("Arc Fige", "bolt"), ("Ame de Foudre", "soul")])

# ---------------- Vide ----------------
F("Vide", "Larve_Rampant", "Larve et Rampant du Vide", r"(Larva|Crawler)_Void", False, 530, ("#4a3a7a", "#2f2050", "#9a5ae0"), "void", [
    ("Mucus du Vide", "slime"), ("Griffe de Rampant", "claw"), ("Larve Cristallisee", "larva")])
F("Vide", "Oeil_Vide", "Oeil du Vide", r"Eye_Void", False, 540, ("#7a2a9a", "#c04a8a", "#e05050"), "void", [
    ("Pupille du Vide", "pupil"), ("Nerf Optique", "nerve"), ("Regard Petrifie", "eye", "cracks")])
F("Vide", "Spectre_Vide", "Spectre du Vide", r"Void_Spectre", False, 580, ("#3a3468", "#20182f", "#7ae0d0"), "void", [
    ("Voile du Vide", "veil"), ("Ectoplasme Noir", "slime"), ("Ame du Vide", "soul")])
F("Vide", "Rejeton_Vide", "Rejeton du Vide", r"Void_Spawn(_Entombed)?", True, 600, ("#5a2050", "#8a3ac0", "#e04ac0"), "void", [
    ("Chair du Vide", "meat"), ("Griffe de Rejeton", "claw"), ("Coeur de Rejeton", "heart"), ("Couronne du Rejeton", "crown")])
F("Vide", "Tentacule_Vide", "Tentacule du Vide", r"Void_Tentacle", False, 590, ("#6a4a8a", "#4a2a6a", "#3a2a5a"), "void", [
    ("Ventouse", "sucker"), ("Tentacule Tranche", "tentacle"), ("Sac d'Encre du Vide", "gland")])
F("Vide", "Cristal_Anomalie", "Cristal d'Anomalie", r"Void_Anomaly_Crystal_(Small|Medium|Large|Keystone)", False, 530, ("#b05ae0", "#5a7ae0", "#f0a0ff"), "facets", [
    ("Eclat d'Anomalie", "shard"), ("Prisme Instable", "prism"), ("Cle de Voute", "keystone")])
F("Vide", "Ours_Corrompu", "Ours Corrompu", r"Bear_Voidtaken", True, 600, ("#3a3048", "#5a3a8a", "#a050e0"), "fur", [
    ("Peau Corrompue", "hide", "fur"), ("Griffe Corrompue", "claw"), ("Coeur Corrompu", "heart"), ("Tentacule d'Ours", "tentacle")])
F("Vide", "Gobelin_Corrompu", "Gobelin Corrompu", r"Voidtaken_Goblin", False, 560, ("#5a7a4a", "#4a3a6a", "#b050d0"), "void", [
    ("Oreille Corrompue", "ear"), ("Dent Corrompue", "tooth"), ("Tumeur du Vide", "tumor")])

# ---------------- Outlanders ----------------
F("Outlanders", "Outlander", "Outlander", r"Outlander_(Peon|Stalker|Hunter|Marauder|Berserker)", False, 460, ("#6a5a48", "#8a8a8a", "#b0412e"), "fur", [
    ("Fourrure Outlander", "hide", "fur"), ("Lame Ebrechee", "blade"), ("Trophee de Raid", "totem")])
F("Outlanders", "Mystique", "Mystique Outlander", r"Outlander_(Cultist|Priest|Sorcerer)", False, 465, ("#4a2a5a", "#d8c8a8", "#a03a8a"), "weave", [
    ("Robe Rituelle", "robe"), ("Talisman d'Os", "talisman"), ("Grimoire Dechire", "book")])
F("Outlanders", "Loup_Outlander", "Loup Outlander", r"Wolf_Outlander_(Priest|Sorcerer)", False, 480, ("#3a3a50", "#8a6ac0", "#d05a50"), "fur", [
    ("Pelage Maudit", "hide", "fur"), ("Croc Maudit", "fang"), ("Sceau Lupin", "seal")])
F("Outlanders", "Brute", "Brute Outlander", r"Outlander_Brute", True, 530, ("#7a5238", "#c2a57a", "#b02a2a"), "cracks", [
    ("Peau de Brute", "hide", "cracks"), ("Machoire de Brute", "jaw"), ("Coeur de Brute", "heart"), ("Masse de Brute", "mace")])

# ---------------- Trorks ----------------
F("Trorks", "Trork", "Trork", r"Trork_(Unarmed|Sentry|Hunter|Brawler|Guard|Mauler|Warrior)", False, 400, ("#5f6a3a", "#d8c8a0", "#c04a2a"), "grain", [
    ("Cuir de Trork", "hide", "cracks"), ("Defense de Trork", "tusk"), ("Peinture de Guerre", "paint")])
F("Trorks", "Chaman_Trork", "Chaman Trork", r"Trork_(Shaman|Doctor_Witch)", False, 440, ("#3a8a6a", "#8a5a3a", "#a0d040"), "grain", [
    ("Plume Rituelle", "feather"), ("Fetiche Trork", "totem"), ("Philtre de Sorciere", "vial")])
F("Trorks", "Loup_Trork", "Loup Trork", r"Wolf_Trork_(Hunter|Shaman)", False, 410, ("#7a6a58", "#e0d0b0", "#a03a2a"), "fur", [
    ("Pelage de Loup Trork", "hide", "fur"), ("Croc de Loup Trork", "fang"), ("Collier de Meute", "collar")])
F("Trorks", "Chef_Trork", "Chef Trork", r"Trork_Chieftain", True, 460, ("#4a6a2a", "#e8d8b0", "#d0303a"), "cracks", [
    ("Peau de Chef", "hide", "cracks"), ("Defense de Chef", "tusk"), ("Coeur de Chef", "heart"), ("Couronne Trork", "crown")])

# ---------------- Zombies ----------------
F("Zombies", "Zombie", "Zombie", r"Zombie", False, 330, ("#7a8a5a", "#a89a6a", "#c08a9a"), "spots", [
    ("Chair Putride", "meat"), ("Dent Pourrie", "tooth"), ("Cerveau Fletri", "brain")])
F("Zombies", "Zombie_Sable", "Zombie des Sables", r"Zombie_Sand", False, 335, ("#b89a6a", "#e0d0a8", "#a0603a"), "cracks", [
    ("Chair Sechee", "meat"), ("Bandage Ensable", "bandage"), ("Coeur Desseche", "heart")])
F("Zombies", "Zombie_Givre", "Zombie de Givre", r"Zombie_Frost", False, 340, ("#8ab0c8", "#c0e0f0", "#5a8ac0"), "frost", [
    ("Chair Gelee", "meat"), ("Doigt Gele", "finger"), ("Coeur Gele", "heart")])
F("Zombies", "Zombie_Brule", "Zombie Brule", r"Zombie_Burnt", False, 345, ("#3f302a", "#5a4a44", "#e0602a"), "lava", [
    ("Chair Carbonisee", "meat"), ("Crane Fumant", "skull"), ("Coeur Calcine", "heart")])
F("Zombies", "Aberration", "Aberration", r"Zombie_Aberrant", True, 395, ("#8a4a6a", "#b0a050", "#d04a5a"), "veins", [
    ("Chair Mutee", "meat"), ("Griffe Mutante", "claw"), ("Tumeur Palpitante", "tumor"), ("Coeur Aberrant", "heart")],
  mineurs=r"Zombie_Aberrant_(Big|Small)")
F("Zombies", "Goule", "Goule", r"Ghoul", False, 385, ("#6a6a7a", "#c04a6a", "#9ab03a"), "grain", [
    ("Griffe de Goule", "claw"), ("Langue de Goule", "tongue"), ("Bile de Goule", "vial")])
F("Zombies", "Chien_Bleme", "Chien Bleme", r"Hound_Bleached", False, 355, ("#d8d4c8", "#c8c0b0", "#8a5ac0"), "fur", [
    ("Peau Bleme", "hide", "fur"), ("Croc Bleme", "fang"), ("Coeur Bleme", "heart")])

# ---------------- Morts-vivants ----------------
F("Morts", "Spectre", "Spectre", r"Wraith", True, 350, ("#8ab8c8", "#5a8aa8", "#c0f0ff"), "smoke", [
    ("Voile Spectral", "veil"), ("Larme de Spectre", "tear"), ("Ame Errante", "soul"), ("Couronne Spectrale", "crown")])
F("Morts", "Spectre_Lanterne", "Spectre a Lanterne", r"Wraith_Lantern", False, 280, ("#d8a03a", "#c0e0d0", "#60e080"), "smoke", [
    ("Meche Spectrale", "wick"), ("Verre de Lanterne", "lantern"), ("Flamme Eternelle", "flame")])
F("Morts", "Loup_Garou", "Loup-garou", r"Werewolf", True, 500, ("#4a3a30", "#c8b8a0", "#b02a3a"), "fur", [
    ("Fourrure de Loup-garou", "hide", "fur"), ("Griffe de Loup-garou", "claw"), ("Coeur de Loup-garou", "heart"), ("Crane de Loup-garou", "beastskull")])
F("Morts", "Chevalier_Ombre", "Chevalier de l'Ombre", r"Shadow_Knight", True, 750, ("#2a2a38", "#403060", "#8a4ae0"), "metal", [
    ("Eclat d'Armure Noire", "plate"), ("Cape d'Ombre", "cape"), ("Coeur d'Ombre", "heart"), ("Heaume de l'Ombre", "helm")])
F("Morts", "Cheval_Squelette", "Cheval Squelette", r"Horse_Skeleton(_Armored)?", False, 80, ("#d8d0b8", "#60708a", "#7a6a50"), "grain", [
    ("Os de Cheval", "bone"), ("Crin Spectral", "mane"), ("Fer Maudit", "horseshoe")])
F("Morts", "Betail_Mort", "Betail Mort-vivant", r"(Chicken|Cow|Pig)_Undead", False, 30, ("#8a6a50", "#c8c0a0", "#c0d0a0"), "spots", [
    ("Viande Avariee", "steak"), ("Os Pourri", "bone"), ("Oeil Laiteux", "eye")])

# ---------------- Squelettes ----------------
F("Squelettes", "Squelette", "Squelette",
  r"Skeleton(_(Scout|Archer|Fighter|Ranger|Mage|Knight|Soldier|Archmage|Elite(_Phase_2)?))?|Risen_(Gunner|Knight)", False, 15,
  ("#d8d0b0", "#b8ae90", "#7ad0e0"), "grain", [
    ("Os Ancien", "bone"), ("Crane Fele", "skull", "cracks"), ("Moelle Spectrale", "vial")])
F("Squelettes", "Squelette_Pirate", "Squelette Pirate", r"Skeleton_Pirate_(Gunner|Striker)", False, 25, ("#c8c0a0", "#e8c040", "#8a4a2a"), "grain", [
    ("Os Sale", "bone"), ("Dent en Or", "tooth", "metal"), ("Rhum Maudit", "bottle")])
F("Squelettes", "Capitaine_Pirate", "Capitaine Pirate", r"Skeleton_Pirate_Captain", True, 30, ("#2a3a5a", "#a08a60", "#e0c8a0"), "weave", [
    ("Tricorne Use", "tricorn"), ("Longue-vue Brisee", "spyglass"), ("Carte au Tresor", "map"), ("Crane du Capitaine", "skull")])
F("Squelettes", "Squelette_Givre", "Squelette de Givre", r"Skeleton_Frost_\w+", False, 150, ("#b0e0f0", "#88c8e8", "#58a0e0"), "frost", [
    ("Os Gele", "bone"), ("Crane de Givre", "skull"), ("Moelle Glacee", "vial")])
F("Squelettes", "Squelette_Brule", "Squelette Brule", r"Skeleton_Burnt_\w+", False, 210, ("#4a3a32", "#6a4a3a", "#909090"), "cracks", [
    ("Os Calcine", "bone"), ("Crane Noirci", "skull"), ("Cendres d'Alchimiste", "urn")])
F("Squelettes", "Squelette_Incandescent", "Squelette Incandescent", r"Skeleton_Incandescent_\w+", False, 215, ("#e08a3a", "#f0602a", "#ffb030"), "lava", [
    ("Os de Braise", "bone"), ("Crane Incandescent", "skull"), ("Noyau de Magma", "core")])
F("Squelettes", "Squelette_Sable", "Squelette des Sables", r"Skeleton_Sand_\w+", False, 270, ("#e0c890", "#e8dcc0", "#2a9a9a"), "grain", [
    ("Os Ensable", "bone"), ("Bandelette Antique", "bandage"), ("Scarabee Funeraire", "beetle", "metal")])

# ---------------- Gobelins ----------------
F("Gobelins", "Gobelin", "Gobelin", r"Goblin_(Burner|Glutton|Guardian|Lobber|Miner|Scrapper|Thief|Mace|Launcher)|Feastmaster_Scrapper", False, 40, ("#6a9a3a", "#d8c890", "#a07a30"), "grain", [
    ("Oreille de Gobelin", "ear"), ("Dent de Gobelin", "tooth"), ("Bourse Volee", "pouch")])
F("Gobelins", "Gobelin_Labo", "Gobelin de Labo", r"Goblin_(Scientist|Lab_Scrapper)", False, 60, ("#4ac0a0", "#8a8a98", "#e04ac0"), "metal", [
    ("Fiole Felee", "vial"), ("Rouage de Labo", "gear"), ("Formule Secrete", "scroll")])
F("Gobelins", "Maitre_Festin", "Maitre du Festin", r"Feastmaster_Boss", True, 95, ("#d8d0c0", "#a0a8b0", "#b08a4a"), "weave", [
    ("Tablier Graisseux", "apron"), ("Couteau de Boucher", "cleaver"), ("Recette Interdite", "scroll"), ("Toque du Festin", "toque")])
F("Gobelins", "Gobliterator", "Gobliterator", r"Goblin_Gobliterator_Phase3", True, 100, ("#7a6a58", "#c0c8d0", "#e0a020"), "metal", [
    ("Ferraille Lourde", "scrap"), ("Scie Tordue", "saw"), ("Moteur Gobelin", "engine"), ("Volant du Pilote", "wheel")])

# ---------------- Betes ----------------
F("Betes", "Loup", "Loup", r"Wolf_(Black|White)", False, 30, ("#6a6a78", "#e8e4d8", "#c03a3a"), "fur", [
    ("Peau de Loup", "hide", "fur"), ("Croc de Loup", "fang"), ("Coeur de Loup", "heart")])
F("Betes", "Hyene", "Hyene", r"Hyena", False, 30, ("#b09a6a", "#e0d4b0", "#d0a030"), "spots", [
    ("Peau de Hyene", "hide", "spots"), ("Croc de Hyene", "fang"), ("Rire Fige", "laugh")])
F("Betes", "Leopard", "Leopard des Neiges", r"Leopard_Snow", False, 40, ("#d0d4dc", "#e8ecf0", "#5ab0e0"), "spots", [
    ("Peau de Leopard", "hide", "spots"), ("Griffe de Leopard", "claw"), ("Oeil de Leopard", "eye")])
F("Betes", "Tigre", "Tigre a Dents de Sabre", r"Tiger_Sabertooth", False, 70, ("#e08a2a", "#e0d0b0", "#f0f0e0"), "stripes", [
    ("Peau de Tigre", "hide", "stripes"), ("Griffe de Tigre", "claw"), ("Canine de Sabre", "sabre")])
F("Betes", "Ours", "Ours", r"Bear_(Grizzly|Polar)", False, 60, ("#6a4a2a", "#3a2a1a", "#c04030"), "fur", [
    ("Peau d'Ours", "hide", "fur"), ("Griffe d'Ours", "claw"), ("Coeur d'Ours", "heart")])
F("Betes", "Yeti", "Yeti", r"Yeti", True, 550, ("#e0ecf4", "#7ab0d0", "#5a9ad8"), "fur", [
    ("Fourrure de Yeti", "hide", "fur"), ("Griffe de Yeti", "claw"), ("Coeur de Yeti", "heart"), ("Crane de Yeti", "beastskull")])
F("Betes", "Emberwulf", "Emberwulf", r"Emberwulf", False, 250, ("#4a2a22", "#f06a2a", "#ffc040"), "lava", [
    ("Pelage de Braise", "hide", "fur"), ("Croc de Braise", "fang"), ("Coeur de Braise", "heart")])
F("Betes", "Etincelle", "Etincelle Vivante", r"Spark_Living", False, 320, ("#505a7a", "#e8d84a", "#60e0ff"), "bolts", [
    ("Residu Electrique", "powder"), ("Bobine Vivante", "coil"), ("Coeur d'Etincelle", "heart")])
F("Betes", "Fen_Stalker", "Fen Stalker", r"Fen_Stalker", False, 100, ("#3a5a4a", "#7a8a3a", "#e0d050"), "spots", [
    ("Peau de Fen", "hide", "spots"), ("Griffe de Fen", "claw"), ("Oeil de Fen", "eye")])
F("Betes", "Cactee", "Cactee", r"Cactee", False, 20, ("#4a8a4a", "#8ac05a", "#e85a9a"), "grain", [
    ("Epine de Cactee", "spine"), ("Pulpe de Cactee", "pulp"), ("Fleur de Cactee", "flower")])
F("Betes", "Crocodile", "Crocodile", r"Crocodile", False, 50, ("#3a5a3a", "#e0d8b8", "#d0a020"), "scales", [
    ("Peau de Crocodile", "hide", "scales"), ("Dent de Crocodile", "tooth"), ("Oeil de Crocodile", "eye")])
F("Betes", "Lezard", "Lezard des Sables", r"Lizard_Sand", False, 30, ("#c8a060", "#a07a40", "#d06030"), "scales", [
    ("Ecaille de Lezard", "scale"), ("Griffe de Lezard", "claw"), ("Queue de Lezard", "tail")])
F("Betes", "Crapaud", "Crapaud Rhino", r"Toad_Rhino(_Magma)?", False, 60, ("#5a7a3a", "#a09a70", "#e05a3a"), "spots", [
    ("Peau de Crapaud", "hide", "spots"), ("Corne de Crapaud", "horn"), ("Glande de Crapaud", "gland")])
F("Betes", "Araignee", "Araignee", r"Spider(_Cave(_Ambush|_Spiderling)?)?", False, 25, ("#e0e0e8", "#3a3030", "#70d030"), "grain", [
    ("Soie d'Araignee", "silk"), ("Patte d'Araignee", "leg"), ("Sac de Venin Ancien", "gland")])
F("Betes", "Scorpion", "Scorpion", r"Scorpion", False, 40, ("#8a5a2a", "#a03a2a", "#d0d050"), "metal", [
    ("Carapace de Scorpion", "carapace"), ("Pince de Scorpion", "pincer"), ("Dard de Scorpion", "stinger")])
F("Betes", "Serpent", "Serpent", r"Snake_(Cobra|Marsh|Rattle)", False, 20, ("#6a8a3a", "#e0e0c8", "#70e060"), "scales", [
    ("Peau de Serpent", "snakeskin", "scales"), ("Croc de Serpent", "fang"), ("Glande a Venin", "gland")])
F("Betes", "Bete_Magma", "Bete de Magma", r"(Slug|Snail)_Magma", False, 25, ("#e0602a", "#6a4a40", "#ffb040"), "lava", [
    ("Bave de Magma", "slime"), ("Coquille de Magma", "shell"), ("Perle de Magma", "pearl")])

# ---------------- Plantes ----------------
F("Plantes", "Ronceux", "Ronceux", r"Bramblekin(_Shaman)?", False, 60, ("#4a6a2a", "#6a4a2a", "#a03a7a"), "grain", [
    ("Ronce Vive", "bramble"), ("Ecorce Ronceuse", "bark"), ("Graine Maudite", "seed")])
F("Plantes", "Hedera", "Hedera", r"Hedera", True, 650, ("#2f8a4a", "#a0d03a", "#e05a8a"), "veins", [
    ("Liane d'Hedera", "vine"), ("Epine d'Hedera", "spine"), ("Coeur d'Hedera", "heart"), ("Fleur d'Hedera", "flower")])

# ---------------- Mer ----------------
F("Mer", "Requin", "Requin-marteau", r"Shark_Hammerhead", True, 80, ("#5a7a98", "#e8e8e0", "#b03a3a"), "grain", [
    ("Peau de Requin", "hide", "grain"), ("Dent de Requin", "tooth"), ("Foie de Requin", "liver"), ("Tete de Requin", "sharkhead")])
F("Mer", "Baleine", "Baleine", r"Whale_Humpback", True, 700, ("#e0d0c0", "#3a4a5a", "#c0a050"), "grain", [
    ("Graisse de Baleine", "blubber"), ("Fanon de Baleine", "comb"), ("Ambre Gris", "amber"), ("Chant Fige", "song")])
F("Mer", "Piranha", "Piranha", r"Piranha(_Black)?", False, 20, ("#b03a3a", "#e0e0d0", "#d0a030"), "scales", [
    ("Ecaille de Piranha", "scale"), ("Dent de Piranha", "tooth"), ("Machoire de Piranha", "jaw")])
F("Mer", "Murene", "Murene", r"Eel_Moray", False, 40, ("#5a6a30", "#e0d8c0", "#e0c040"), "spots", [
    ("Peau de Murene", "snakeskin", "spots"), ("Croc de Murene", "fang"), ("Oeil de Murene", "eye")])
F("Mer", "Snapjaw", "Snapjaw", r"Snapjaw", False, 40, ("#3a6a7a", "#e0e4d0", "#8a4a2a"), "scales", [
    ("Ecaille de Snapjaw", "scale"), ("Croc de Snapjaw", "fang"), ("Machoire de Snapjaw", "jaw")])
F("Mer", "Frostgill", "Frostgill", r"Frostgill", False, 50, ("#7ab0d8", "#a0d8f0", "#e0f8ff"), "frost", [
    ("Ecaille Gelee", "scale"), ("Branchie Gelee", "gill"), ("Perle de Givre", "pearl")])
