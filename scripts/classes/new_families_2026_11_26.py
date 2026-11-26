"""Nouvelles voies de classes (2026-11-26) : Clerc, Dompteur, Barde. Ajoute (ou remplace) leurs classes dans
scripts/classes/classes.json, puis relancer class_catalog.py.

Decision de l'utilisateur (2026-11-26) : ce ne sont PAS des classes de base. Leurs lignees rejoignent une famille existante :
Clerc et Barde -> Mage, Dompteur -> Marchand. Chaque voie : 12 lignees au palier 2 (3 Communes, 3 Rares, 2 Epiques,
2 Uniques, 1 Legendaire, 1 Divine), ajoutees aux evolutions de la classe de base de la famille (tirage a 180), et leurs
evolutions au palier 3 (la rarete monte d'un cran jusqu'a Unique ; Divine : une seule suite).
Runes : runes existantes du theme en attendant les runes propres de chaque classe (chantier « identite », famille par famille).

Usage : python3 scripts/classes/new_families_2026_11_26.py
"""
import os, sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import class_catalog as cc  # noqa: E402

RANK = {"COMMON": 1, "RARE": 2, "EPIC": 3, "UNIQUE": 4, "LEGENDARY": 5, "DIVINE": 6}
UP = {"COMMON": "RARE", "RARE": "EPIC", "EPIC": "UNIQUE", "UNIQUE": "UNIQUE", "LEGENDARY": "LEGENDARY", "DIVINE": "DIVINE"}
NRUNES = {"COMMON": 1, "RARE": 1, "EPIC": 2, "UNIQUE": 2, "LEGENDARY": 3, "DIVINE": 3}
STAT = {"FOR": 0, "VIT": 1, "INT": 2, "END": 3, "AGI": 4, "CHA": 5}
MAXR = cc.passives()


def ranked(names, rarity, bonus=0):
    return [[p, max(2 if p.endswith("_MASTERY") else 1, min(MAXR.get(p, 6), RANK[rarity] + bonus))] for p in names]


def focus(profil, stat, k=1.35):
    p = list(profil)
    p[STAT[stat]] = round(p[STAT[stat]] * k)
    return p


def family(fam, base, lineages):
    """fam : famille d'accueil (mage, marchand) ; base : ancienne classe de base de la voie, gardee seulement comme note
    (plus creee depuis le 2026-11-26) ; lineages : [(id, nom, rarete, description, profil, passifs, runes, armes, enfants)],
    enfants : [(id, nom, description, stat favorisee, passif ajoute, rune ajoutee)]."""
    out = []
    kids_out = []
    for lid, nom, rar, desc, prof, pas, runes, armes, kids in lineages:
        out.append(dict(id=lid, nom=nom, famille=fam, palier=2, rarete=rar, description=desc, passifs=ranked(pas, rar),
                        runes=runes[:NRUNES[rar]], armes=armes, metiers=[], profil=prof, evolutions=[k[0] for k in kids]))
        kr = UP[rar]
        for kid, knom, kdesc, kstat, kpas, krune in kids:
            kp = list(dict.fromkeys(pas + ([kpas] if kpas else [])))
            kru = list(dict.fromkeys(([krune] if krune else []) + runes))[:NRUNES[kr]]
            kids_out.append(dict(id=kid, nom=knom, famille=fam, palier=3, rarete=kr, description=kdesc,
                                 passifs=ranked(kp, kr, 1 if kr == rar else 0), runes=kru, armes=armes, metiers=[],
                                 profil=focus(prof, kstat), evolutions=[]))
    return out + kids_out


# ===================================================================================================================== CLERC
CL_ARMES = ["MACE", "STAFF", "SHIELD", "SPELLBOOK"]
CLERC = family("mage", dict(
    id="clerc", nom="Clerc", passifs=["CELLULAR_REGENERATION", "MANA_FONT"], runes=["Rune_SoinMineur", "Rune_HaloProtecteur"],
    armes=CL_ARMES, profil=[6, 10, 10, 6, 2, 2],
    description="Serviteur de la Lumière, le Clerc soigne ses alliés et châtie les créatures impies. Sa foi lui tient lieu "
                "de bouclier et sa masse rappelle aux morts qu'ils doivent le rester."), [
    ("acolyte", "Acolyte", "COMMON", "L'Acolyte apprend les prières de soin au service d'un temple. Sa magie est douce mais ne s'épuise jamais.",
     [3, 10, 12, 6, 3, 4], ["CELLULAR_REGENERATION", "MANA_FONT"], ["Rune_SoinMineur"], ["STAFF", "SPELLBOOK", "MACE"], [
        ("pretre", "Prêtre", "Le Prêtre guide les fidèles et referme les plaies d'un mot. Ses prières soignent tout un groupe.", "INT", "LIFE_FORCE", "Rune_FontaineDeVie"),
        ("chapelain", "Chapelain", "Le Chapelain accompagne les armées. Ses bénédictions renforcent les soldats avant la bataille.", "VIT", "COMBAT_VIGOR", "Rune_ArmeBenie"),
        ("diacre", "Diacre", "Le Diacre veille sur les sanctuaires. Sa présence calme les blessés et repousse les ombres.", "INT", "MANA_BARRIER", "Rune_SanctuaireMineur")]),
    ("exorciste", "Exorciste", "COMMON", "L'Exorciste chasse les esprits et les morts-vivants. Ses incantations brûlent tout ce qui n'a plus d'âme.",
     [6, 6, 12, 4, 4, 4], ["ARCANE_STRIKE", "AWAKENED_MIND"], ["Rune_LanceDeLumiere"], ["MACE", "SPELLBOOK"], [
        ("chasseur_de_demons", "Chasseur de Démons", "Le Chasseur de Démons traque les créatures des abîmes jusque dans leurs repaires.", "FOR", "HAUNTING_THRUST", "Rune_LanceDeLumiere"),
        ("purificateur", "Purificateur", "Le Purificateur consume la corruption par le feu sacré. Rien d'impur ne lui résiste.", "INT", "OVERFLOWING_POWER", "Rune_RayonCeleste"),
        ("conjureur_d_ames", "Conjureur d'Âmes", "Le Conjureur d'Âmes délivre les esprits prisonniers et retourne leur colère contre leurs geôliers.", "INT", "SPIRITUAL_SIPHON", "Rune_Revelation")]),
    ("hospitalier", "Frère Hospitalier", "COMMON", "Le Frère Hospitalier soigne les voyageurs sur les routes dangereuses. Robuste, il encaisse autant qu'il guérit.",
     [6, 12, 8, 8, 2, 2], ["ROBUST_CONSTITUTION", "NATURAL_RECOVERY"], ["Rune_SoinMineur"], ["MACE", "SHIELD", "STAFF"], [
        ("infirmier_de_guerre", "Infirmier de Guerre", "L'Infirmier de Guerre traverse le champ de bataille pour relever les blessés sous les flèches.", "VIT", "SECOND_WIND", "Rune_FontaineDeVie"),
        ("gardien_des_routes", "Gardien des Routes", "Le Gardien des Routes protège les pèlerins. Son bouclier béni a sauvé des caravanes entières.", "END", "ADAPTIVE_SHIELD", "Rune_EgideRoyale"),
        ("frere_errant", "Frère Errant", "Le Frère Errant parcourt le continent sans repos. Il se relève de tout et soigne en marchant.", "VIT", "TIRELESS_BREATH", "Rune_Meditation")]),
    ("pretre_de_guerre", "Prêtre de Guerre", "RARE", "Le Prêtre de Guerre prie la masse à la main. Chacun de ses coups est une bénédiction pour ses alliés.",
     [12, 10, 8, 8, 2, 2], ["INSTINCTIVE_STRIKE", "CELLULAR_REGENERATION"], ["Rune_ArmeBenie", "Rune_EgideRoyale"], ["MACE", "SHIELD", "FLAIL"], [
        ("marteau_de_la_foi", "Marteau de la Foi", "Le Marteau de la Foi frappe comme la colère des cieux. Ses coups étourdissent les hérétiques.", "FOR", "PRESSURE_POINT", "Rune_ArmeBenie"),
        ("champion_du_temple", "Champion du Temple", "Le Champion du Temple défend l'autel jusqu'à son dernier souffle. Aucun ennemi ne franchit sa garde.", "END", "SOLID_STANCE", "Rune_EgideRoyale"),
        ("aumonier_des_legions", "Aumônier des Légions", "L'Aumônier des Légions marche en tête des troupes. Ses chants de guerre soignent et galvanisent.", "VIT", "COMBAT_VIGOR", "Rune_SanctuaireMineur")]),
    ("gardien_du_sanctuaire", "Gardien du Sanctuaire", "RARE", "Le Gardien du Sanctuaire érige des zones sacrées où ses alliés guérissent et où les ennemis faiblissent.",
     [4, 10, 12, 8, 2, 2], ["MANA_BARRIER", "HEART_OF_OAK"], ["Rune_SanctuaireMineur", "Rune_HaloProtecteur"], ["STAFF", "SHIELD"], [
        ("veilleur_sacre", "Veilleur Sacré", "Le Veilleur Sacré ne dort jamais. Ses sanctuaires brillent toute la nuit et repoussent les morts.", "END", "STEEL_NERVES", "Rune_SanctuaireMineur"),
        ("batisseur_de_temples", "Bâtisseur de Temples", "Le Bâtisseur de Temples fait jaillir des murs de lumière. Ses sanctuaires deviennent des forteresses.", "INT", "STONE_SKIN", "Rune_Rempart"),
        ("egide_vivante", "Égide Vivante", "L'Égide Vivante détourne les coups vers elle. Sa foi fait d'elle le bouclier de tout un groupe.", "VIT", "ADAPTIVE_SHIELD", "Rune_EgideRoyale")]),
    ("augure", "Augure", "RARE", "L'Augure lit la volonté des cieux dans le vol des oiseaux. Ses présages protègent ses alliés avant même le danger.",
     [2, 8, 14, 6, 4, 6], ["AWAKENED_MIND", "GOOD_OMEN"], ["Rune_Meditation", "Rune_LienDAme"], ["STAFF", "SPELLBOOK"], [
        ("voix_divine", "Voix Divine", "La Voix Divine porte la parole des dieux. Ses mots ferment les blessures et brisent la volonté ennemie.", "INT", "SPIRITUAL_SIPHON", "Rune_Revelation"),
        ("lecteur_d_etoiles", "Lecteur d'Étoiles", "Le Lecteur d'Étoiles prédit les coups avant qu'ils ne tombent. Ses alliés esquivent ce qu'il a vu.", "CHA", "GOOD_OMEN", "Rune_Prescience"),
        ("lien_des_ames", "Tisseur de Liens", "Le Tisseur de Liens unit les âmes de ses compagnons : la douleur de l'un est partagée par tous.", "VIT", "LIFE_FORCE", "Rune_LienDAme")]),
    ("hierophante", "Hiérophante", "EPIC", "Le Hiérophante est le gardien des mystères sacrés. Sa lumière soigne les justes et aveugle les impies.",
     [4, 10, 16, 6, 2, 4], ["OVERFLOWING_POWER", "CELLULAR_REGENERATION"], ["Rune_RayonCeleste", "Rune_FontaineDeVie"], ["STAFF", "SPELLBOOK", "MACE"], [
        ("grand_hierophante", "Grand Hiérophante", "Le Grand Hiérophante dirige les temples du royaume. Sa parole fait trembler les rois.", "INT", "MANA_FONT", "Rune_Reflexion"),
        ("porte_lumiere", "Porte-Lumière", "Le Porte-Lumière transporte un fragment de soleil. Les ténèbres reculent sur son passage.", "INT", "ARCANE_STRIKE", "Rune_NaineBlanche"),
        ("mystagogue", "Mystagogue", "Le Mystagogue initie les élus aux secrets divins. Ses rituels doublent la force de ses alliés.", "CHA", "AWAKENED_MIND", "Rune_Enseignement")]),
    ("martyr", "Martyr", "EPIC", "Le Martyr offre sa propre vie pour sauver les autres. Plus il souffre, plus sa lumière grandit.",
     [6, 16, 8, 8, 2, 2], ["VITAL_BLOOD", "PERSEVERANCE"], ["Rune_LienDAme", "Rune_FontaineDeVie"], ["MACE", "SHIELD"], [
        ("saint_sacrifie", "Saint Sacrifié", "Le Saint Sacrifié a déjà donné sa vie une fois. Revenu, il ne craint plus aucune blessure.", "VIT", "SECOND_WIND", "Rune_Resurrection"),
        ("coeur_ardent", "Cœur Ardent", "Le Cœur Ardent transforme sa douleur en flammes sacrées qui consument ses ennemis.", "FOR", "OVERFLOWING_POWER", "Rune_AuraDeBraise"),
        ("rempart_de_chair", "Rempart de Chair", "Le Rempart de Chair se tient devant les siens. Les lames se brisent sur sa foi.", "END", "STONE_SKIN", "Rune_EgideRoyale")]),
    ("heraut_celeste", "Héraut Céleste", "UNIQUE", "Le Héraut Céleste annonce la venue de la Lumière. Ses ailes de clarté rendent espoir aux armées vaincues.",
     [6, 10, 16, 6, 4, 4], ["OVERFLOWING_POWER", "MANA_FONT", "LIFE_FORCE"], ["Rune_RayonCeleste", "Rune_Miracle"], ["STAFF", "SPELLBOOK", "MACE"], [
        ("trompette_du_jugement", "Trompette du Jugement", "La Trompette du Jugement sonne la fin des impies. Son appel abat les murailles.", "INT", "ARCANE_STRIKE", "Rune_LanceDeLumiere"),
        ("aile_de_l_aube", "Aile de l'Aube", "L'Aile de l'Aube vole au secours des mourants. Là où elle se pose, la mort recule.", "VIT", "CELLULAR_REGENERATION", "Rune_Resurrection"),
        ("messager_des_cieux", "Messager des Cieux", "Le Messager des Cieux voyage plus vite que la lumière pour porter les miracles des dieux.", "AGI", "WIND_STEP", "Rune_Miracle")]),
    ("ange_gardien", "Ange Gardien", "UNIQUE", "L'Ange Gardien veille sur un seul allié à la fois et le protège de la mort elle-même.",
     [4, 14, 12, 10, 2, 2], ["ADAPTIVE_SHIELD", "HEART_OF_OAK", "MANA_BARRIER"], ["Rune_HaloProtecteur", "Rune_LienDAme"], ["SHIELD", "STAFF", "MACE"], [
        ("protecteur_des_ames", "Protecteur des Âmes", "Le Protecteur des Âmes garde les esprits de ses alliés : aucun ne tombe tant qu'il veille.", "VIT", "PERSEVERANCE", "Rune_Resurrection"),
        ("bouclier_celeste", "Bouclier Céleste", "Le Bouclier Céleste dresse un dôme de lumière que rien ne traverse.", "END", "STONE_SKIN", "Rune_Reflexion"),
        ("sentinelle_d_argent", "Sentinelle d'Argent", "La Sentinelle d'Argent voit le danger avant tout le monde et s'interpose sans hésiter.", "AGI", "SURVIVAL_INSTINCT", "Rune_EgideRoyale")]),
    ("grand_pontife", "Grand Pontife", "LEGENDARY", "Le Grand Pontife est la voix de la foi sur le continent. Ses miracles relèvent les morts et renversent les batailles.",
     [6, 14, 18, 8, 2, 6], ["OVERFLOWING_POWER", "CELLULAR_REGENERATION", "LIFE_FORCE"], ["Rune_Resurrection", "Rune_Miracle", "Rune_RayonCeleste"], ["STAFF", "SPELLBOOK", "MACE"], [
        ("pontife_eternel", "Pontife Éternel", "Le Pontife Éternel ne meurt pas tant que la foi demeure. Ses prières traversent les siècles.", "INT", "PERSEVERANCE", "Rune_Resurrection"),
        ("main_des_dieux", "Main des Dieux", "La Main des Dieux accomplit leurs volontés sur terre. Ses jugements sont sans appel.", "FOR", "ARCANE_STRIKE", "Rune_NaineBlanche"),
        ("lumiere_du_monde", "Lumière du Monde", "La Lumière du Monde éclaire tout le continent. Les ténèbres n'osent plus s'approcher.", "VIT", "HEART_OF_OAK", "Rune_Miracle")]),
    ("avatar_lumiere", "Avatar de la Lumière", "DIVINE", "L'Avatar de la Lumière est la foi incarnée. Ses mains guérissent toute blessure et son regard consume les démons.",
     [8, 16, 20, 8, 4, 8], ["OVERFLOWING_POWER", "CELLULAR_REGENERATION", "MANA_FONT"], ["Rune_Resurrection", "Rune_NaineBlanche", "Rune_Miracle"], ["STAFF", "SPELLBOOK", "MACE", "SHIELD"], [
        ("demi_seraphin", "Demi-Séraphin", "Le Demi-Séraphin a reçu le feu des plus hauts anges. Six ailes de lumière l'élèvent au-dessus des mortels.", "INT", "LIFE_FORCE", "Rune_Resurrection")]),
])

# ===================================================================================================================== DOMPTEUR
DO_ARMES = ["SPEAR", "BOW", "DAGGER", "CLUB"]
DOMPTEUR = family("marchand", dict(
    id="dompteur", nom="Dompteur", passifs=["HUNTER_GUARD", "SURVIVAL_INSTINCT"], runes=["Rune_EspritFollet", "Rune_Racines"],
    armes=DO_ARMES, profil=[8, 8, 4, 8, 8, 4],
    description="Le Dompteur se bat aux côtés des bêtes qu'il a apprivoisées. Il comprend leur langage, partage leur instinct "
                "et fait de son compagnon une arme redoutable."), [
    ("maitre_chien", "Maître-Chien", "COMMON", "Le Maître-Chien commande une meute fidèle. Ses chiens encerclent la proie pendant qu'il porte le coup final.",
     [8, 8, 2, 8, 10, 4], ["HAUNTING_THRUST", "TIRELESS_BREATH"], ["Rune_MeuteSpectrale"], ["SPEAR", "DAGGER"], [
        ("chef_de_meute", "Chef de Meute", "Le Chef de Meute est reconnu par tous les loups. La meute obéit à un seul de ses sifflements.", "AGI", "COMBAT_VIGOR", "Rune_MeuteSpectrale"),
        ("veneur", "Veneur", "Le Veneur dirige les grandes chasses royales. Aucune bête ne survit à sa traque.", "FOR", "PRESSURE_POINT", "Rune_PluieDeFleches"),
        ("molosse_humain", "Molossier", "Le Molossier élève des chiens de guerre cuirassés. Ensemble, ils brisent les lignes ennemies.", "VIT", "STURDY_BODY", "Rune_Ruee")]),
    ("fauconnier", "Fauconnier", "COMMON", "Le Fauconnier chasse avec ses rapaces. L'oiseau aveugle la proie, la flèche l'achève.",
     [4, 6, 4, 6, 12, 8], ["BOW_MASTERY", "NIGHT_VISION"], ["Rune_PluieDeFleches"], ["BOW", "DAGGER"], [
        ("maitre_des_rapaces", "Maître des Rapaces", "Le Maître des Rapaces lâche des aigles qui fondent du ciel comme la foudre.", "AGI", "WIND_STEP", "Rune_Bourrasque"),
        ("oeil_du_ciel", "Œil du Ciel", "L'Œil du Ciel voit par les yeux de ses oiseaux. Rien ne lui échappe sur des lieues.", "CHA", "GOOD_OMEN", "Rune_ProjectionAstrale"),
        ("chasseur_aile", "Chasseur Ailé", "Le Chasseur Ailé et son faucon frappent ensemble : un coup du ciel, un coup de la terre.", "FOR", "LUCKY_STRIKE", "Rune_ChargedShot")]),
    ("chevaucheur", "Chevaucheur", "COMMON", "Le Chevaucheur ne fait qu'un avec sa monture. Ses charges renversent tout sur leur passage.",
     [10, 8, 2, 8, 8, 4], ["SPEAR_MASTERY", "COMBAT_VIGOR"], ["Rune_Ruee"], ["SPEAR", "CLUB"], [
        ("cavalier_des_steppes", "Cavalier des Steppes", "Le Cavalier des Steppes traverse les plaines au galop et frappe avant d'être vu.", "AGI", "WIND_STEP", "Rune_Ruee"),
        ("lancier_monte", "Lancier Monté", "Le Lancier Monté transperce l'ennemi au bout de sa charge. Rien n'arrête sa lance.", "FOR", "SPEAR_MASTERY", "Rune_GroundSlam"),
        ("dresseur_de_destriers", "Dresseur de Destriers", "Le Dresseur de Destriers forme les montures des chevaliers. Les siennes sont infatigables.", "END", "TIRELESS_BREATH", "Rune_Racines")]),
    ("charmeur_de_serpents", "Charmeur de Serpents", "RARE", "Le Charmeur de Serpents envoûte les reptiles de sa flûte. Leur venin devient le sien.",
     [4, 6, 8, 6, 12, 6], ["DAGGER_MASTERY", "LUCKY_STRIKE"], ["Rune_PoisonImbue", "Rune_SablesMouvants"], ["DAGGER", "CLUB"], [
        ("maitre_des_reptiles", "Maître des Reptiles", "Le Maître des Reptiles commande aux vipères et aux basilics. Leur morsure ne pardonne pas.", "AGI", "HAUNTING_THRUST", "Rune_PoisonImbue"),
        ("envouteur_de_basilics", "Envoûteur de Basilics", "L'Envoûteur de Basilics pétrifie ses ennemis du regard de ses bêtes.", "INT", "MYSTIC_VEIL", "Rune_Petrification"),
        ("fils_du_desert", "Fils du Désert", "Le Fils du Désert dompte scorpions et serpents des sables. Les dunes l'obéissent.", "END", "SURVIVAL_INSTINCT", "Rune_SablesMouvants")]),
    ("dresseur_d_ours", "Dresseur d'Ours", "RARE", "Le Dresseur d'Ours combat épaule contre épaule avec un ours colossal. Ensemble, ils écrasent tout.",
     [14, 12, 2, 10, 4, 2], ["STURDY_BODY", "INSTINCTIVE_STRIKE"], ["Rune_GroundSlam", "Rune_Enrage"], ["CLUB", "SPEAR"], [
        ("frere_de_l_ours", "Frère de l'Ours", "Le Frère de l'Ours partage la force de son compagnon. Ses coups font trembler la terre.", "FOR", "DEEP_SLASH", "Rune_GroundSlam"),
        ("gardien_des_cavernes", "Gardien des Cavernes", "Le Gardien des Cavernes vit parmi les ours des montagnes. Sa peau est aussi dure que la leur.", "END", "STONE_SKIN", "Rune_Rempart"),
        ("furie_sauvage", "Furie Sauvage", "La Furie Sauvage entre en rage avec son ours. Leur colère commune ne connaît pas de limite.", "FOR", "OVERFLOWING_POWER", "Rune_Enrage")]),
    ("belluaire", "Belluaire", "RARE", "Le Belluaire a appris son art dans les arènes, face aux fauves. Il les fait combattre pour lui sous les vivats.",
     [10, 8, 2, 8, 10, 6], ["MINOR_PARRY", "COMBAT_VIGOR"], ["Rune_ChainHook", "Rune_Ruee"], ["SPEAR", "DAGGER", "CLUB"], [
        ("maitre_des_fauves", "Maître des Fauves", "Le Maître des Fauves dirige lions et tigres d'un claquement de fouet.", "FOR", "PRESSURE_POINT", "Rune_ChainHook"),
        ("roi_de_l_arene_sauvage", "Roi de l'Arène Sauvage", "Le Roi de l'Arène Sauvage n'a jamais perdu un combat de bêtes. La foule scande son nom.", "CHA", "LUCKY_STRIKE", "Rune_Enrage"),
        ("gladiateur_des_betes", "Gladiateur des Bêtes", "Le Gladiateur des Bêtes affronte lui-même les monstres qu'il ne peut dompter.", "VIT", "TENACITY", "Rune_GroundSlam")]),
    ("seigneur_des_loups", "Seigneur des Loups", "EPIC", "Le Seigneur des Loups règne sur les meutes du nord. Sa voix fait hurler les forêts entières.",
     [10, 10, 4, 8, 12, 4], ["HAUNTING_THRUST", "WIND_STEP", "NIGHT_VISION"], ["Rune_MeuteSpectrale", "Rune_ChainHook"], ["SPEAR", "DAGGER"], [
        ("loup_alpha", "Alpha Éternel", "L'Alpha Éternel n'est plus tout à fait humain : il court avec la meute et chasse sous la lune.", "AGI", "WIND_STEP", "Rune_MeuteSpectrale"),
        ("roi_de_l_hiver", "Roi de l'Hiver", "Le Roi de l'Hiver mène des loups blancs nés des tempêtes de neige.", "END", "HEART_OF_OAK", "Rune_SentierDHiver"),
        ("pere_des_meutes", "Père des Meutes", "Le Père des Meutes compte cent loups dans sa famille. Ils accourent au moindre appel.", "CHA", "GOOD_OMEN", "Rune_AmesErrantes")]),
    ("metamorphe", "Métamorphe", "EPIC", "Le Métamorphe emprunte la forme de ses bêtes. Tour à tour ours, loup ou faucon, il est imprévisible.",
     [10, 10, 6, 8, 10, 4], ["ELDANIOR_SUPPLENESS", "CELLULAR_REGENERATION", "INSTINCTIVE_STRIKE"], ["Rune_EspritForet", "Rune_Enrage"], ["CLUB", "DAGGER"], [
        ("change_peau", "Change-Peau", "Le Change-Peau passe d'une forme à l'autre en un battement de cœur.", "AGI", "MIRACLE_DODGE", "Rune_Doppelganger"),
        ("esprit_totem", "Esprit-Totem", "L'Esprit-Totem porte en lui l'âme de toutes les bêtes de son clan.", "INT", "SPIRITUAL_SIPHON", "Rune_EspritForet"),
        ("bete_primale", "Bête Primale", "La Bête Primale a oublié sa forme humaine. Elle ne connaît plus que l'instinct et la chasse.", "FOR", "DEEP_SLASH", "Rune_Enrage")]),
    ("chevaucheur_de_wyverne", "Chevaucheur de Wyverne", "UNIQUE", "Le Chevaucheur de Wyverne a dompté une bête ailée. Il fond du ciel sur ses ennemis.",
     [12, 10, 4, 10, 12, 4], ["SPEAR_MASTERY", "WIND_STEP", "COMBAT_VIGOR"], ["Rune_Bourrasque", "Rune_Ruee"], ["SPEAR", "BOW"], [
        ("dragonnier", "Dragonnier", "Le Dragonnier monte les jeunes dragons des montagnes. Leur souffle embrase les champs de bataille.", "FOR", "DEEP_SLASH", "Rune_Fireball"),
        ("seigneur_des_vents", "Seigneur des Vents", "Le Seigneur des Vents chevauche les tempêtes. Sa wyverne disperse les armées d'un battement d'ailes.", "AGI", "WIND_STEP", "Rune_DanseDesVents"),
        ("lance_celeste", "Lance Céleste", "La Lance Céleste plonge des nuages comme une étoile filante. Personne ne survit à sa charge.", "FOR", "PRESSURE_POINT", "Rune_PilierAscendant")]),
    ("gardien_des_betes_sacrees", "Gardien des Bêtes Sacrées", "UNIQUE", "Le Gardien des Bêtes Sacrées protège les créatures légendaires du continent. Elles le protègent en retour.",
     [6, 12, 10, 10, 6, 6], ["HEART_OF_OAK", "HUNTER_GUARD", "LIFE_FORCE"], ["Rune_EspritForet", "Rune_Bosquet"], ["SPEAR", "BOW", "CLUB"], [
        ("berger_des_licornes", "Berger des Licornes", "Le Berger des Licornes guérit par la corne de ses protégées. Leur lumière purifie tout poison.", "VIT", "CELLULAR_REGENERATION", "Rune_FontaineDeVie"),
        ("ami_des_phenix", "Ami des Phénix", "L'Ami des Phénix renaît de ses cendres aux côtés de son oiseau de feu.", "INT", "PERSEVERANCE", "Rune_Resurrection"),
        ("voix_de_la_foret", "Voix de la Forêt", "La Voix de la Forêt parle aux cerfs blancs et aux esprits des bois. La forêt entière se lève pour elle.", "INT", "NATURAL_RECOVERY", "Rune_Bosquet")]),
    ("roi_des_betes", "Roi des Bêtes", "LEGENDARY", "Le Roi des Bêtes est obéi par toutes les créatures. D'un rugissement, il lève une armée sauvage.",
     [14, 12, 6, 10, 12, 6], ["INSTINCTIVE_STRIKE", "HAUNTING_THRUST", "HEART_OF_OAK"], ["Rune_MeuteSpectrale", "Rune_EspritForet", "Rune_Enrage"], ["SPEAR", "CLUB", "BOW", "DAGGER"], [
        ("empereur_sauvage", "Empereur Sauvage", "L'Empereur Sauvage règne sur les forêts, les cieux et les mers. Chaque bête est son soldat.", "CHA", "GOOD_OMEN", "Rune_ColossePierre"),
        ("lion_de_l_aube", "Lion de l'Aube", "Le Lion de l'Aube porte une crinière de lumière. Son rugissement brise le courage des ennemis.", "FOR", "DEEP_SLASH", "Rune_Enrage"),
        ("coeur_de_la_nature", "Cœur de la Nature", "Le Cœur de la Nature bat au rythme du monde vivant. Les bêtes guérissent à son contact.", "VIT", "LIFE_FORCE", "Rune_Bosquet")]),
    ("lien_primordial", "Lien Primordial", "DIVINE", "Le Lien Primordial a dompté une bête née avant le monde. Leur union fait trembler les dieux eux-mêmes.",
     [16, 16, 8, 12, 16, 6], ["INSTINCTIVE_STRIKE", "HEART_OF_OAK", "WIND_STEP"], ["Rune_ColossePierre", "Rune_MeuteSpectrale", "Rune_EspritForet"], ["SPEAR", "CLUB", "BOW", "DAGGER"], [
        ("avatar_sauvage", "Avatar Sauvage", "L'Avatar Sauvage est la nature déchaînée. Les bêtes primordiales s'éveillent à son appel.", "FOR", "LIFE_FORCE", "Rune_ColossePierre")]),
])

# ===================================================================================================================== BARDE
BA_ARMES = ["SWORD", "DAGGER", "BOW", "SPELLBOOK"]
BARDE = family("mage", dict(
    id="barde", nom="Barde", passifs=["GOOD_OMEN", "TIRELESS_BREATH"], runes=["Rune_Meditation", "Rune_EspritFollet"],
    armes=BA_ARMES, profil=[4, 6, 8, 4, 8, 10],
    description="Le Barde combat en chantant. Ses mélodies donnent du cœur à ses alliés, ses satires brisent le moral ennemi "
                "et sa légende grandit à chaque taverne."), [
    ("menestrel", "Ménestrel", "COMMON", "Le Ménestrel chante les exploits des héros. Ses chansons redonnent souffle et courage à ses compagnons.",
     [2, 6, 8, 6, 6, 12], ["GOOD_OMEN", "SECOND_WIND"], ["Rune_Meditation"], ["SWORD", "DAGGER", "SPELLBOOK"], [
        ("troubadour", "Troubadour", "Le Troubadour voyage de cour en cour. Ses ballades apaisent les blessures et les cœurs.", "CHA", "LIFE_FORCE", "Rune_SoinMineur"),
        ("chantre_des_tavernes", "Chantre des Tavernes", "Le Chantre des Tavernes connaît mille chansons à boire. Ses alliés se battent comme dix.", "VIT", "COMBAT_VIGOR", "Rune_Enseignement"),
        ("poete_errant", "Poète Errant", "Le Poète Errant improvise des vers qui changent le cours des combats.", "INT", "AWAKENED_MIND", "Rune_SavoirAccelere")]),
    ("jongleur", "Jongleur", "COMMON", "Le Jongleur lance ses dagues comme des balles de foire. Il amuse la foule avant de la dépouiller.",
     [6, 4, 2, 4, 14, 10], ["ELDANIOR_SUPPLENESS", "LUCKY_STRIKE"], ["Rune_WindStrike"], ["DAGGER", "SWORD"], [
        ("bateleur", "Bateleur", "Le Bateleur fait de chaque combat un spectacle. Ses tours de passe-passe désarment les ennemis.", "AGI", "MIRACLE_DODGE", "Rune_MiroirTrompeur"),
        ("lanceur_de_couteaux", "Lanceur de Couteaux", "Le Lanceur de Couteaux ne rate jamais une pomme posée sur une tête... ni un œil.", "FOR", "DAGGER_MASTERY", "Rune_WindStrike"),
        ("fou_du_roi", "Fou du Roi", "Le Fou du Roi dit la vérité en riant. Sous les grelots se cache le plus dangereux des bretteurs.", "CHA", "GOOD_OMEN", "Rune_Hallucination")]),
    ("conteur", "Conteur", "COMMON", "Le Conteur fait revivre les légendes. Quand il raconte une bataille, ses auditeurs la gagnent.",
     [2, 6, 12, 4, 4, 10], ["AWAKENED_MIND", "MANA_FONT"], ["Rune_Enseignement"], ["SPELLBOOK", "DAGGER"], [
        ("chroniqueur", "Chroniqueur", "Le Chroniqueur écrit l'histoire pendant qu'elle se fait. Ses écrits protègent ceux qu'ils nomment.", "INT", "MANA_BARRIER", "Rune_EchosDuPasse"),
        ("gardien_des_legendes", "Gardien des Légendes", "Le Gardien des Légendes connaît le nom de chaque héros. Il invoque leur force dans ses récits.", "INT", "SPIRITUAL_SIPHON", "Rune_AmesErrantes"),
        ("faiseur_de_mythes", "Faiseur de Mythes", "Le Faiseur de Mythes invente des héros... qui finissent par exister.", "CHA", "OVERFLOWING_POWER", "Rune_Clonage")]),
    ("skalde", "Skalde", "RARE", "Le Skalde chante les sagas du nord au milieu de la mêlée. Ses chants de guerre déchaînent la fureur de ses alliés.",
     [12, 10, 4, 8, 6, 8], ["COMBAT_VIGOR", "INSTINCTIVE_STRIKE"], ["Rune_Enrage", "Rune_Enseignement"], ["SWORD", "SPELLBOOK"], [
        ("chantre_de_guerre", "Chantre de Guerre", "Le Chantre de Guerre rythme les charges. Ses alliés frappent au tempo de ses tambours.", "FOR", "DEEP_SLASH", "Rune_Enrage"),
        ("voix_des_ancetres", "Voix des Ancêtres", "La Voix des Ancêtres réveille les guerriers d'autrefois. Leurs ombres combattent à ses côtés.", "INT", "SPIRITUAL_SIPHON", "Rune_AmesErrantes"),
        ("barde_de_bataille", "Barde de Bataille", "Le Barde de Bataille chante sans jamais reculer. Tant qu'il chante, nul ne fuit.", "VIT", "STEEL_NERVES", "Rune_SanctuaireMineur")]),
    ("virtuose", "Virtuose", "RARE", "Le Virtuose joue si bien que le temps lui-même s'arrête pour l'écouter. Ses alliés en profitent.",
     [4, 6, 12, 4, 10, 12], ["AWAKENED_MIND", "WIND_STEP"], ["Rune_Surregime", "Rune_SavoirAccelere"], ["SPELLBOOK", "SWORD"], [
        ("maestro", "Maestro", "Le Maestro dirige ses alliés comme un orchestre : chacun frappe au moment parfait.", "CHA", "GOOD_OMEN", "Rune_Surregime"),
        ("harpiste_celeste", "Harpiste Céleste", "La Harpiste Céleste joue la musique des sphères. Ses notes soignent et protègent.", "INT", "LIFE_FORCE", "Rune_FontaineDeVie"),
        ("compositeur_du_temps", "Compositeur du Temps", "Le Compositeur du Temps écrit des partitions qui ralentissent les ennemis et pressent les alliés.", "INT", "MANA_FONT", "Rune_BulleTemporelle")]),
    ("charmeur", "Charmeur", "RARE", "Le Charmeur séduit d'un sourire. Les ennemis baissent leur garde, parfois leurs armes.",
     [4, 6, 10, 4, 8, 14], ["MYSTIC_VEIL", "LUCKY_STRIKE"], ["Rune_Hallucination", "Rune_MiroirTrompeur"], ["DAGGER", "SPELLBOOK"], [
        ("seducteur", "Séducteur", "Le Séducteur retourne les ennemis les uns contre les autres d'un simple regard.", "CHA", "GOOD_OMEN", "Rune_Marionnette"),
        ("voix_de_sirene", "Voix de Sirène", "La Voix de Sirène attire les ennemis vers leur perte. Nul ne résiste à son chant.", "INT", "SPIRITUAL_SIPHON", "Rune_Hallucination"),
        ("masque_d_argent", "Masque d'Argent", "Le Masque d'Argent change de visage à chaque couplet. Personne ne connaît le vrai.", "AGI", "MIRACLE_DODGE", "Rune_MiroirTrompeur")]),
    ("maitre_chanteur", "Maître Chanteur", "EPIC", "Le Maître Chanteur maîtrise les chants de pouvoir. Une seule note peut briser l'acier.",
     [6, 8, 14, 6, 6, 14], ["OVERFLOWING_POWER", "GOOD_OMEN", "MANA_FONT"], ["Rune_OndeRepulsion", "Rune_Enseignement"], ["SPELLBOOK", "SWORD"], [
        ("voix_de_cristal", "Voix de Cristal", "La Voix de Cristal atteint des notes si pures qu'elles font éclater les pierres.", "INT", "ARCANE_STRIKE", "Rune_GalerieDesGlaces"),
        ("chant_de_l_aube", "Chant de l'Aube", "Le Chant de l'Aube réveille les mourants et chasse les cauchemars.", "VIT", "CELLULAR_REGENERATION", "Rune_Miracle"),
        ("cri_du_tonnerre", "Cri du Tonnerre", "Le Cri du Tonnerre pousse un chant si puissant qu'il appelle l'orage.", "FOR", "OVERFLOWING_POWER", "Rune_NuageOrage")]),
    ("danseur_de_guerre", "Danseur de Guerre", "EPIC", "Le Danseur de Guerre combat en dansant. Chaque pas est une esquive, chaque tour une estocade.",
     [8, 6, 4, 6, 16, 10], ["MIRACLE_DODGE", "WIND_STEP", "ELDANIOR_SUPPLENESS"], ["Rune_WindStrike", "Rune_DanseDesVents"], ["SWORD", "DAGGER"], [
        ("derviche", "Derviche", "Le Derviche tourne sans fin, lames tendues. Approcher, c'est mourir.", "AGI", "WIND_STEP", "Rune_TempeteFeuilles"),
        ("danseuse_des_lames", "Danseuse des Lames", "La Danseuse des Lames trace des arabesques mortelles. Son public ne survit pas au final.", "FOR", "DAGGER_MASTERY", "Rune_WindStrike"),
        ("ballet_mortel", "Ballet Mortel", "Le Ballet Mortel fait danser ses alliés avec lui : ensemble, ils deviennent insaisissables.", "CHA", "GOOD_OMEN", "Rune_DanseDesVents")]),
    ("aede", "Aède", "UNIQUE", "L'Aède chante sous l'inspiration des muses. Ses épopées accordent aux héros une gloire immortelle.",
     [6, 8, 14, 6, 8, 16], ["AWAKENED_MIND", "GOOD_OMEN", "LIFE_FORCE"], ["Rune_Enseignement", "Rune_Revelation"], ["SPELLBOOK", "SWORD", "BOW"], [
        ("elu_des_muses", "Élu des Muses", "L'Élu des Muses reçoit chaque nuit un chant nouveau. Chacun est un sortilège.", "INT", "OVERFLOWING_POWER", "Rune_Revelation"),
        ("chantre_eternel", "Chantre Éternel", "Le Chantre Éternel chante depuis des siècles. Sa voix ne se tait jamais, même dans la mort.", "VIT", "PERSEVERANCE", "Rune_EchosDuPasse"),
        ("lyre_d_or", "Lyre d'Or", "La Lyre d'Or joue l'air qui apaisa les dieux. Ses alliés deviennent intouchables.", "CHA", "MANA_BARRIER", "Rune_Reflexion")]),
    ("voix_des_enfers", "Voix des Enfers", "UNIQUE", "La Voix des Enfers a chanté pour les morts et en est revenue. Ses complaintes arrachent les âmes.",
     [4, 8, 16, 6, 6, 12], ["SPIRITUAL_SIPHON", "MYSTIC_VEIL", "AWAKENED_MIND"], ["Rune_AmesErrantes", "Rune_ChainesNoires"], ["SPELLBOOK", "DAGGER"], [
        ("chantre_des_morts", "Chantre des Morts", "Le Chantre des Morts fait danser les squelettes. Son chœur est une armée.", "INT", "SPIRITUAL_SIPHON", "Rune_ReleveDesMorts"),
        ("complainte_noire", "Complainte Noire", "La Complainte Noire chante la tristesse du monde. Ses ennemis perdent toute volonté de combattre.", "CHA", "MYSTIC_VEIL", "Rune_SilenceNeant"),
        ("passeur_d_ames", "Passeur d'Âmes", "Le Passeur d'Âmes guide les défunts vers l'au-delà, et parfois les en ramène.", "VIT", "LIFE_FORCE", "Rune_Resurrection")]),
    ("barde_legendaire", "Barde Légendaire", "LEGENDARY", "Le Barde Légendaire est connu sur tout le continent. Son nom seul suffit à faire fuir une armée.",
     [8, 10, 16, 8, 10, 18], ["GOOD_OMEN", "OVERFLOWING_POWER", "LUCKY_STRIKE"], ["Rune_Enseignement", "Rune_Surregime", "Rune_Miracle"], ["SPELLBOOK", "SWORD", "BOW", "DAGGER"], [
        ("chantre_des_rois", "Chantre des Rois", "Le Chantre des Rois est réclamé par tous les souverains. Ses chants font et défont les couronnes.", "CHA", "GOOD_OMEN", "Rune_CouronneAutorite"),
        ("hymne_vivant", "Hymne Vivant", "L'Hymne Vivant est devenu chanson. Partout où on le chante, il apparaît.", "INT", "AWAKENED_MIND", "Rune_Clonage"),
        ("voix_du_continent", "Voix du Continent", "La Voix du Continent est entendue d'un bout à l'autre du royaume. Ses alliés combattent comme des héros.", "VIT", "LIFE_FORCE", "Rune_Miracle")]),
    ("chant_primordial", "Chant Primordial", "DIVINE", "Le Chant Primordial chante la mélodie qui créa le monde. Chaque note réécrit la réalité.",
     [8, 12, 20, 8, 12, 20], ["OVERFLOWING_POWER", "GOOD_OMEN", "AWAKENED_MIND"], ["Rune_Reecriture", "Rune_Surregime", "Rune_Miracle"], ["SPELLBOOK", "SWORD", "BOW", "DAGGER"], [
        ("demi_muse", "Demi-Muse", "La Demi-Muse est née de la première chanson. Les dieux eux-mêmes viennent l'écouter.", "CHA", "LIFE_FORCE", "Rune_Reecriture")]),
])


HOST = {"mage": "mage", "marchand": "merchant"}         # famille -> classe de base qui recoit les lignees
OLD_BASES = {"clerc", "dompteur", "barde"}              # anciennes classes de base de ces voies (retirees le 2026-11-26)


def main():
    data = cc.load()
    new = CLERC + DOMPTEUR + BARDE
    ids = {c["id"] for c in new}
    data = [c for c in data if c["id"] not in ids and c["id"] not in OLD_BASES]
    for c in data:
        if c["palier"] == 1 and c["id"] in HOST.values():
            fam = next(f for f, b in HOST.items() if b == c["id"])
            lin = [x["id"] for x in new if x["palier"] == 2 and x["famille"] == fam]
            c["evolutions"] = [e for e in c["evolutions"] if e not in ids] + lin
    by_fam = {}
    for c in data:
        by_fam.setdefault(c["famille"], []).append(c)
    data = [c for f in by_fam for c in by_fam[f]]
    # lignees apres celles de leur famille (palier 2 puis 3), pour garder classes.json lisible
    out = []
    for f in by_fam:
        fam_new = [x for x in new if x["famille"] == f]
        for pal in (1, 2, 3):
            out += [c for c in by_fam[f] if c["palier"] == pal] + [x for x in fam_new if x["palier"] == pal]
    cc.save(out)
    print(len(new), "classes : Clerc et Barde dans le Mage, Dompteur dans le Marchand")


if __name__ == "__main__":
    main()
