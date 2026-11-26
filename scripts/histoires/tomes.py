"""Tomes Imperiaux du Royaume d'Eldanior (2026-11-14, histoire de l'utilisateur mise en forme) : 10 livres, de la Guerre Divine
a la mort du Roi Fou, signes par Shino Luo. Lus par gen_histoires.py (objets Eldanior_Tome_01 a _10).

Chaque tome : (numero romain, titre, [pages]). Une page = un ecran de la fenetre de lecture (LivrePage), qui defile si besoin.
Mise en forme du jeu : <i>italique</i>, <b>gras</b>, \\n = retour a la ligne.

Chronologie retenue (pour garder la suite logique) :
  - An 1 de l'Aube : fin de la Guerre Divine (le Dragon Primordial avale le soleil, premiere eclipse, Pacte de l'Aube).
  - An 7 : Epreuve du Choix au Mont de l'Aube (futur Tombeau des Epreuves Divines) ; Arald recoit les 3 reliques
    (Couronne d'Aube, Sceau d'Eldanior, Larme d'Or). Fondation d'Eldanior et de l'Empire Luo, Serment des Deux Rives,
    le Clan Luo fait Marquis et Doyen du Royaume.
  - La Longue Paix : Arald regne mille ans (benediction du roi elu) ; l'epreuve n'a jamais servi, elle est oubliee.
  - Mort d'Arald ; guerre de succession ; 9 mois plus tard, la Nuit Ecarlate ; Edward se couronne (Couronne de Fer).
    Le Doyen refuse de lui remettre la Couronne d'Aube et l'emporte chez les Luo.
  - 6e annee d'Edward : le Tombeau, sans 2 reliques sur 3 ; la Fureur du Dragon.
  - La reine cache le Sceau sous les ruines de l'aile de l'Est ; l'Empire Luo ferme ses portes (Comptoir seul ouvert).
  - 27e annee : Runkandel, Valmontis et Zippel tuent le Roi Fou (Ryukiri) ; personne ne prend le trone.

Secrets caches dans le texte :
  - Acrostiche : la premiere lettre de chaque tome donne DRAGON DORE.
  - Shino Luo est le Dragon d'Or Primordial : « je me souviens » (I), yeux d'or et age inconnu (IV), n'a pas change en mille ans
    (V), murmure les mots du Dragon a Arald mort (III / VI), « Moi, je le sais » (VIII), « comme la premiere fois » (X).
  - La Couronne d'Aube et la Larme d'Or sont chez Shino ; le Sceau dort sous l'aile de l'Est (suite de la Quete du Roi).
  - Version officielle : le roi est mort de sa folie ; la verite (regicide des trois maisons) n'est ecrite que dans le tome X.
"""

TOMES = [
    ("I", "La Guerre Divine", [
        "Du temps où le ciel brûlait encore, il n'y avait ni royaume ni empire. Il n'y avait que la guerre.\n\n"
        "Les <b>Anges</b> descendaient des hauteurs en colonnes de lumière, et les <b>Démons</b> montaient de l'Abîme en "
        "fleuves de cendre. Nul ne sait plus qui frappa le premier. Les anciens rouleaux disent seulement que la guerre dura "
        "si longtemps que les deux camps oublièrent pourquoi ils se battaient.\n\n"
        "Entre eux, il y avait les hommes.",

        "Les hommes n'avaient ni ailes ni cornes. Ils n'avaient pas de magie, pas d'arts, pas même de nom pour les étoiles. "
        "Ils servaient de bouclier aux uns et de pâture aux autres, et leurs villes brûlaient chaque fois que le ciel et "
        "l'Abîme se rencontraient au-dessus d'elles.\n\n"
        "Les Anges disaient : « Nous les protégeons. » Les Démons disaient : « Nous les libérons. » Les hommes, eux, "
        "enterraient leurs enfants.\n\n"
        "C'est alors que <b>Celui qui était avant toute chose</b> ouvrit les yeux.",

        "Les rouleaux le nomment le <b>Dragon Primordial</b>. Il a tissé les planètes et allumé le soleil. Il a donné son "
        "souffle à la magie, son rythme aux arts martiaux, sa forme à chaque compétence que l'on grave aujourd'hui dans les "
        "runes. Tout ce qui existe porte la trace de ses griffes.\n\n"
        "Il ne prit le parti de personne. Il ouvrit la gueule, et il <b>avala le soleil</b>.\n\n"
        "Ce fut la première éclipse. Le monde entier tomba dans la nuit, et dans cette nuit, plus aucune armée ne vit son "
        "ennemi. Les lances s'abaissèrent. Les ailes se replièrent. Pour la première fois depuis mille générations, le "
        "silence se fit.",

        "Quand la lumière revint, le Dragon parla, et toutes les créatures l'entendirent :\n\n"
        "<i>« Les Anges retourneront aux hauteurs, les Démons à l'Abîme. Aucun d'eux ne foulera plus la terre des hommes. "
        "Celui qui brisera cette loi me trouvera devant lui. »</i>\n\n"
        "Ainsi fut scellé le <b>Pacte de l'Aube</b>, et ainsi finit la Guerre Divine. Les sages comptent depuis ce jour les "
        "années de l'Aube.\n\n"
        "Je me souviens encore de la chaleur de ce premier matin… Du moins, c'est ce que diraient les anciens, s'il en "
        "restait un seul pour s'en souvenir.",
    ]),

    ("II", "Les Dons du Dragon", [
        "Rien ne repoussa sur les champs de bataille pendant sept ans. La terre avait bu trop de sang divin. Les hommes "
        "sortirent des cavernes où ils s'étaient cachés et découvrirent un monde vide, sans maîtres et sans protecteurs.\n\n"
        "Le Dragon ne les abandonna pas. Mais il ne voulut pas non plus régner sur eux.\n\n"
        "<i>« Un peuple que l'on porte n'apprend jamais à marcher », disait-il.</i>\n\n"
        "Alors il leur fit des dons.",

        "À ceux qui écoutaient le monde, il donna la <b>magie</b>, et les <b>Sources Arcanes</b> jaillirent sous les "
        "montagnes du nord-ouest.\n\n"
        "À ceux qui écoutaient leur propre corps, il donna les <b>arts martiaux</b> : le souffle, le Ki, la posture, la lame "
        "qui coupe avant même d'être dégainée.\n\n"
        "À tous, il donna les <b>compétences</b> : des éclats de sa propre force, que les hommes apprirent plus tard à graver "
        "dans la pierre des runes.\n\n"
        "Mais un don n'est rien sans quelqu'un pour le garder. Les seigneurs de guerre qui avaient survécu se disputaient "
        "déjà les ruines.",

        "La septième année, le Dragon lança un second appel. Il convoqua au sommet du <b>Mont de l'Aube</b> tous ceux qui "
        "prétendaient protéger les hommes.\n\n"
        "<i>« Je choisirai un roi. Pas le plus fort. Pas le plus riche. Celui qui saura porter une couronne sans qu'elle "
        "l'écrase. »</i>\n\n"
        "Sept répondirent. Il y avait parmi eux des généraux couverts de gloire, une reine des marchands, un sorcier qui "
        "avait volé le feu d'un Ange déchu…\n\n"
        "… et un capitaine sans terre, venu à pied parce qu'il avait donné son cheval à une famille de réfugiés. "
        "Il s'appelait <b>Arald</b>.",
    ]),

    ("III", "L'Épreuve du Choix", [
        "Au sommet du Mont de l'Aube, le Dragon creusa la montagne d'un seul souffle et y bâtit trois salles. Ce lieu, on "
        "l'appellerait un jour le <b>Tombeau des Épreuves Divines</b>.\n\n"
        "<b>La Salle de l'Épée</b> éprouvait le courage. Chaque candidat y affronta ce qu'il craignait le plus. Le sorcier y "
        "vit l'Ange qu'il avait trahi, et s'enfuit. Deux généraux s'y entretuèrent, chacun croyant voir en l'autre son pire "
        "cauchemar.\n\n"
        "Arald y vit son village en flammes. Il ne dégaina pas : il entra dans le feu pour en sortir les enfants, un par un, "
        "jusqu'à ce que les flammes s'éteignent d'elles-mêmes.",

        "<b>La Salle de la Couronne</b> éprouvait le cœur. Sur un autel reposait une couronne qui offrait à celui qui la "
        "porterait le monde entier, et l'éternité pour le gouverner.\n\n"
        "La reine des marchands la posa sur sa tête et ne put plus jamais l'enlever ; on dit qu'elle compte encore son or, "
        "quelque part sous la montagne. Le dernier général voulut la briser par orgueil, et la montagne le recracha.\n\n"
        "Arald la regarda longtemps. Puis il demanda : « Et mon peuple ? Que lui donne-t-elle, à lui ? »\n\n"
        "La couronne ne répondit pas. Alors il la laissa sur l'autel.",

        "<b>La Salle du Sang</b> éprouvait le sacrifice. Elle était vide, hormis le Dragon lui-même, immense, les yeux "
        "couleur d'or.\n\n"
        "<i>« Pour garder les hommes, il faut leur donner une chose que l'on ne reprendra jamais. Que me donnes-tu ? »</i>\n\n"
        "Arald s'agenouilla. « Ma vie. Tant que je régnerai, elle sera à eux, pas à moi. »\n\n"
        "Le Dragon resta longtemps silencieux. Puis il inclina sa tête immense et prononça ces mots, que les Eldanior "
        "gravèrent plus tard au-dessus de leur trône :\n\n"
        "<i>« Tu as choisi les tiens. Je ne l'oublierai pas. »</i>",

        "Le Dragon remit à Arald trois reliques, qui seraient pour toujours la preuve d'un roi élu :\n\n"
        "<b>La Couronne d'Aube</b>, forgée dans le premier rayon du soleil rendu au monde ;\n"
        "<b>Le Sceau d'Eldanior</b>, qui scelle le Pacte de l'Aube et donne force de loi à la parole du roi ;\n"
        "<b>La Larme d'Or</b>, une goutte du sang du Dragon changée en pierre.\n\n"
        "<i>« Quand tu ne seras plus, que ton héritier vienne ici avec les trois reliques. Je serai là. Je le regarderai, et "
        "je dirai s'il est digne. Sans elles, que nul n'entre. »</i>\n\n"
        "Arald descendit de la montagne. Il était parti capitaine. Il revint roi.",
    ]),

    ("IV", "La Fondation des Deux Rives", [
        "Grande fut la joie des peuples quand Arald descendit du Mont de l'Aube. Il prit le nom de la vallée qui l'avait vu "
        "naître, et l'on se mit à l'appeler <b>Arald Von Eldanior</b>, premier roi d'Eldanior.\n\n"
        "Il ne bâtit pas son royaume seul. Des maisons fidèles se tenaient à ses côtés, et il leur confia les marches du "
        "royaume :\n\n"
        "<b>Vorn Runkandel</b>, le général qui tint le col du nord-est contre les derniers Démons égarés, lors du Siège des "
        "Flammes ;\n"
        "<b>Mercurio Valmontis</b>, qui nourrit les réfugiés avec un simple comptoir et en fit le premier marché du royaume ;\n"
        "<b>Thalion Zippel</b>, qui trouva les Sources Arcanes et fonda la première Académie de Magie.",

        "La quatrième maison n'était pas tout à fait une maison du royaume.\n\n"
        "La même année, à la fin de la Guerre Divine, naquit de l'autre côté des montagnes du sud-ouest l'<b>Empire Luo</b>. "
        "Son empereur se nommait <b>Shino Luo</b>. D'où venait-il ? Les rouleaux de l'époque l'ignorent. Ils disent "
        "seulement qu'il était apparu au sortir de l'éclipse, qu'il avait les yeux couleur d'or, et que nul ne connaissait "
        "son âge.\n\n"
        "Les Luo disent descendre du Dieu-Dragon d'Or. Ils le disaient déjà en ce temps-là, et personne n'osait en rire.\n\n"
        "Le fils de l'Empereur, <b>Luo Tenryū</b>, franchit les cols et fonda dans la vallée des Mille Cerisiers le clan "
        "qui porte encore son nom.",

        "Arald et Shino se rencontrèrent à la frontière, sur un pont de pierre jeté au-dessus d'une rivière. Ils y restèrent "
        "trois jours. Personne ne sait ce qu'ils se dirent, mais ils en revinrent amis, et le restèrent toute leur vie.\n\n"
        "Sur ce pont, ils prêtèrent le <b>Serment des Deux Rives</b> :\n\n"
        "<i>« Tant que le royaume d'Eldanior se tiendra debout, l'Empire Luo sera à ses côtés. Ce qui menace l'un menace "
        "l'autre. Ensemble, ils garderont le continent. »</i>\n\n"
        "L'Empire promit d'être toujours là pour le royaume. En retour, Arald fit un don qu'aucun roi ne fit jamais après lui.",

        "Il éleva le Clan Luo au rang de <b>Marquis</b> du royaume, et donna à son chef le titre de <b>Doyen du Royaume</b>.\n\n"
        "<i>« Le Doyen siégera à ma droite, non comme un vassal mais comme un égal. Il me conseillera quand j'aurai raison, "
        "et plus encore quand j'aurai tort. »</i>\n\n"
        "C'est pourquoi, sur les registres d'Eldanior, le Clan Luo n'est qu'un simple marquisat, et pourquoi pourtant il est "
        "craint autant que la couronne elle-même.\n\n"
        "Luo Tenryū fut le premier Doyen. On dit que lorsqu'il entrait au conseil, le roi lui-même se levait pour le saluer.",
    ]),

    ("V", "La Longue Paix", [
        "On dit que mille ans passèrent ainsi, et c'est la vérité.\n\n"
        "La bénédiction du Dragon tenait le roi Arald hors du temps. Tant qu'il régnait, sa vie appartenait à son peuple, et "
        "le temps n'avait pas le droit de la reprendre. Ses cheveux blanchirent, mais sa main ne trembla jamais.\n\n"
        "Ce fut la <b>Longue Paix</b>. Les Runkandel gardaient les forteresses du nord-est, les Valmontis ouvraient des "
        "routes jusqu'au-delà des mers, les tours des Zippel s'élevaient au-dessus des nuages. Dans la vallée des Mille "
        "Cerisiers, les Luo forgeaient des lames et enseignaient les arts du souffle.",

        "La guerre ne disparut pas tout à fait. Il restait des bêtes dans les terres sauvages, des morts qui refusaient de "
        "dormir, et parfois une fissure de l'Abîme laissait passer ce qui aurait dû rester en bas. Mais sous le règne "
        "d'Arald, aucun homme ne leva plus l'épée contre un autre homme.\n\n"
        "Les ambassadeurs qui revenaient de l'Empire racontaient tous la même chose, génération après génération : "
        "l'Empereur Shino les avait reçus, il avait bu le thé avec eux, et il n'avait pas changé.\n\n"
        "« Les Luo vieillissent lentement », répondait-on à la cour. Et personne ne posait plus de questions.",

        "Arald eut des enfants, beaucoup, au fil des siècles. La plupart vécurent une vie d'homme et moururent avant lui. "
        "Il les pleura tous.\n\n"
        "Sentant venir la fin de son règne, il fit rédiger le <b>Testament d'Arald</b>, scellé du Sceau d'Eldanior et confié "
        "au Doyen :\n\n"
        "<i>« À ma mort, que mes héritiers se présentent devant le Doyen et les maisons du royaume, et qu'ils choisissent "
        "ensemble celui qui montera au Tombeau. La Couronne d'Aube restera auprès du Doyen jusqu'à ce jour. Le Sceau restera "
        "au palais, pour que la loi continue. Et que la Larme d'Or retourne à celui qui l'a versée. »</i>",

        "Mille ans, c'est long. Assez long pour que les promesses deviennent des coutumes, les coutumes des légendes, et les "
        "légendes des contes que l'on raconte aux enfants.\n\n"
        "Puisque le même roi régnait depuis toujours, personne n'avait jamais vu une épreuve de succession. Le chemin du Mont "
        "de l'Aube s'effaça sous les ronces. Le Tombeau des Épreuves Divines disparut des cartes. Seuls quelques vieux "
        "grimoires de la famille royale en parlaient encore, et plus personne ne les ouvrait.\n\n"
        "Ce fut une erreur. Nous l'avons tous payée.",
    ]),

    ("VI", "La Mort du Premier Roi", [
        "Nul ne sut l'heure exacte de la mort du roi Arald. On le trouva au matin, assis sur le balcon de sa chambre, tourné "
        "vers l'est, comme s'il avait voulu voir une dernière fois le soleil se lever.\n\n"
        "Ce fut le jour le plus triste que le royaume ait connu. Les cloches sonnèrent sept jours. Les marchés fermèrent. "
        "Dans les villages, les gens s'asseyaient devant leur porte sans rien dire, parce qu'aucun d'eux n'avait connu un "
        "monde sans lui.\n\n"
        "L'Empereur Shino vint en personne aux funérailles. Les témoins disent qu'il resta longtemps seul devant le corps, "
        "et qu'il murmura quelque chose. Un garde crut entendre :\n\n"
        "<i>« Tu as choisi les tiens. Je ne l'ai pas oublié. »</i>",

        "Ce jour-là fut aussi le premier jour de la fin.\n\n"
        "Arald laissait cinq héritiers, nés tard dans son règne, qui n'avaient jamais imaginé devoir le remplacer. Aucun ne "
        "voulut attendre. Chacun avait ses partisans, ses chevaliers, ses dettes. Le Testament d'Arald fut lu devant la "
        "cour ; on l'écouta poliment, puis on l'oublia.\n\n"
        "Le plus redouté des cinq était l'aîné, le Premier Prince <b>Edward Von Eldanior</b>. Il avait passé sa jeunesse à "
        "combattre ce qui sortait des fissures de l'Abîme, dans les marches du nord, et il n'y faisait jamais de "
        "prisonniers. Sur les champs de bataille, ses soldats l'appelaient le <b>Prince Sanglant</b>. Il aimait ce nom.",

        "La bataille de succession commença dans les salons et finit dans les rues. Les héritiers levèrent des armées. Des "
        "villes changèrent de maître trois fois en une saison. Les champs ne furent pas semés, et le peuple eut faim.\n\n"
        "L'Empire Luo tenta de calmer les choses. Le Doyen rappela le Serment des Deux Rives et le Testament d'Arald ; il "
        "proposa de réunir les maisons et de conduire le choix comme le roi l'avait voulu. L'Empereur Shino écrivit "
        "lui-même aux cinq héritiers.\n\n"
        "Personne ne voulut écouter. On répondit au Doyen que les affaires du royaume ne regardaient pas les étrangers. "
        "C'était la première fois en mille ans qu'on l'appelait ainsi.",
    ]),

    ("VII", "La Nuit Écarlate", [
        "Dans la nuit du solstice d'hiver, neuf lunes après la mort du roi, le Premier Prince invita ses quatre frères et "
        "sœurs au palais pour signer la paix.\n\n"
        "Ils vinrent. Ils étaient las de la guerre, et c'était leur frère.\n\n"
        "Aucun ne repartit. Edward les fit tuer à la table du festin, sous les yeux de leurs gardes, puis il fit tuer les "
        "gardes. L'aile de l'Est, où logeaient leurs familles, fut incendiée avant minuit. On dit que ce feu-là, d'une "
        "certaine manière, ne s'est jamais éteint.",

        "Cela ne lui suffit pas. Jusqu'à l'aube, ses chevaliers parcoururent la capitale. Ils frappèrent aux portes de tous "
        "ceux qui avaient servi un autre héritier, puis de ceux qui les avaient logés, puis de ceux qui avaient seulement "
        "regardé passer leurs bannières. On exécuta sur les places des boulangers, des enfants, des vieillards, sans procès "
        "et sans raison.\n\n"
        "Au matin, la neige des rues était rouge. Le peuple appela cette nuit la <b>Nuit Écarlate</b>, et il ne prononce "
        "plus ce nom qu'à voix basse.",

        "Edward ne se présenta pas devant le Doyen. Il ne réunit pas les maisons. Il ne monta pas au Tombeau. Il fit fondre "
        "les épées de ses frères, s'en fit forger une couronne, la <b>Couronne de Fer</b>, et se couronna lui-même dans la "
        "salle du trône encore tachée de sang.\n\n"
        "Puis il exigea que le Doyen lui remette la Couronne d'Aube. Le Doyen refusa :\n\n"
        "<i>« Le testament de votre père est clair. C'est le Dragon qui choisit, pas l'épée. »</i>\n\n"
        "Le soir même, le Doyen quitta le conseil royal et emporta la Couronne d'Aube derrière les montagnes. "
        "Elle n'en est jamais revenue.",
    ]),

    ("VIII", "Le Tombeau des Épreuves Divines", [
        "Oubliées des dieux, les terres du royaume s'enfoncèrent dans la terreur et la pauvreté. Les impôts du Roi de Fer "
        "prenaient la moitié des récoltes, puis les trois quarts. Ceux qui ne pouvaient pas payer partaient aux mines, et "
        "n'en revenaient pas. Les routes des Valmontis se vidèrent. Les académies des Zippel fermèrent une à une.\n\n"
        "Pourtant, Edward n'était pas satisfait. Il avait une couronne, mais le peuple ne s'agenouillait que par peur. Les "
        "chevaliers obéissaient, mais murmuraient. Et chaque nuit, il voyait en rêve un trône vide, et une couronne de "
        "lumière qui n'était pas la sienne.",

        "C'est dans la bibliothèque interdite du palais, au fond d'un coffre que personne n'avait ouvert depuis des siècles, "
        "qu'il trouva les vieux grimoires de la famille. L'un d'eux racontait l'<b>Épreuve Divine</b> : le Mont de l'Aube, "
        "les trois reliques, et le Dragon qui reconnaît le roi légitime et lui accorde les pouvoirs d'un roi élu.\n\n"
        "Edward lut la page cent fois. Il n'en retint qu'une chose : <i>les pouvoirs</i>.\n\n"
        "Il lança ses hommes sur les traces du Tombeau. Des mois de recherche suivirent. Ceux qui savaient quelque chose "
        "furent payés, puis tués pour qu'ils ne parlent à personne d'autre. Un vieil archiviste fut assassiné dans son lit. "
        "Un village entier, au pied de la montagne, disparut en une seule nuit.",

        "Au printemps de la sixième année de son règne, Edward trouva enfin l'entrée sous les ronces : le <b>donjon "
        "millénaire des Eldanior</b>, le Tombeau des Épreuves Divines.\n\n"
        "Mais il ne portait qu'une seule des trois reliques : le Sceau d'Eldanior, pris au palais. La Couronne d'Aube était "
        "derrière les montagnes des Luo. Quant à la Larme d'Or, personne ne savait ce qu'elle était devenue depuis la mort "
        "d'Arald.\n\n"
        "Le testament du Dragon était clair : <i>sans elles, que nul n'entre.</i>\n\n"
        "Edward entra.",

        "Il traversa les trois salles l'épée à la main, en tuant ce qu'il y trouvait, et ses chevaliers tombèrent un à un "
        "derrière lui. Dans la Salle du Sang, il n'y avait personne. Il cria. Il exigea. Il frappa les murs jusqu'à briser "
        "sa lame.\n\n"
        "Alors le Dragon vint, car le Dragon vient toujours à l'épreuve de sélection royale : il l'avait promis.\n\n"
        "Il regarda longtemps cet héritier couvert du sang de ses frères. Puis il ne prononça qu'un mot, et ce mot était une "
        "malédiction : la <b>Fureur du Dragon</b>.\n\n"
        "Edward sentit une force immense entrer en lui. Il éclata de rire, se crut béni, et redescendit seul de la "
        "montagne.\n\n"
        "<i>Les chroniques ne disent pas où se trouvait l'Empereur des Luo cette nuit-là. Moi, je le sais.</i>",
    ]),

    ("IX", "La Fureur du Dragon", [
        "Rongé de l'intérieur, le roi ne le comprit pas tout de suite.\n\n"
        "La Fureur du Dragon n'est pas une bénédiction. C'est un éclat de colère divine enfermé dans un corps trop petit "
        "pour la contenir. Elle donne une force immense, et la reprend chaque jour un peu plus, en dévorant celui qui la "
        "porte.\n\n"
        "La magie d'Edward se mit à s'entrechoquer en lui. Des flammes jaillissaient de ses mains quand il était en colère, "
        "et il était toujours en colère. Ses yeux brûlaient comme des braises sous la cendre. Il ne dormait plus. Il "
        "entendait respirer quelque chose derrière chaque porte.",

        "Il se retourna contre sa propre famille. La reine fut enfermée dans ses appartements. Ses serviteurs furent "
        "chassés, puis pendus, pour des complots qu'il était seul à voir. Il fit brûler les greniers des territoires vassaux "
        "qui avaient payé leur tribut en retard, puis de ceux qui l'avaient payé à l'heure, parce qu'il les soupçonnait.\n\n"
        "Le peuple l'appelait désormais le <b>Roi Fou</b>.\n\n"
        "La reine trouva le courage de lui voler le Sceau d'Eldanior pendant l'une de ses crises. Elle le cacha sous les "
        "ruines de l'aile de l'Est, là où le feu de la Nuit Écarlate couvait encore, et écrivit à l'encre invisible où il "
        "se trouvait. On ne la revit jamais.",

        "Cette même année, l'Empire Luo <b>ferma ses portes</b> au reste du monde.\n\n"
        "Les cols des montagnes du sud-ouest furent gardés par les Ombres, et nul ne put plus entrer sur les terres des Luo "
        "sans une autorisation temporaire de l'Empire. Seul le <b>Comptoir de la Porte du Dragon</b>, à son seuil, resta "
        "ouvert aux marchands agréés.\n\n"
        "On accusa l'Empire de lâcheté. Mais le Serment des Deux Rives liait les Luo au royaume, pas à l'homme qui l'avait "
        "trahi. Et l'Empereur avait juré de ne jamais lever la main contre le peuple d'Eldanior. Alors il ferma les portes, "
        "et il attendit.",

        "Dans les marches, trois maisons n'attendirent pas.\n\n"
        "Les <b>Runkandel</b> avaient vu leurs fils mourir pour les caprices du roi. Les <b>Valmontis</b> avaient vu leurs "
        "routes devenir des cimetières. Les <b>Zippel</b> avaient vu brûler leurs académies et compris, en étudiant les "
        "rapports, ce qui dévorait le roi : la Fureur du Dragon le rendait presque impossible à tuer.\n\n"
        "Ils se réunirent en secret dans une auberge du nord-est. Les Zippel trouvèrent comment percer la Fureur. Les "
        "Valmontis payèrent l'acier, les mercenaires et le silence. Et les forgerons des Runkandel forgèrent une lame "
        "capable de trancher un dragon : <b>Ryūkiri</b>.",
    ]),

    ("X", "La Mort du Roi Fou", [
        "Écoute, toi qui lis ces pages, car ce qui suit n'est écrit nulle part ailleurs.\n\n"
        "Officiellement, le Roi Fou mourut de sa folie, dévoré par le mal qui le rongeait, dans la vingt-septième année de "
        "son règne. C'est ce que disent les registres du royaume. C'est ce que les trois maisons ont voulu que l'on croie.\n\n"
        "La vérité est autre.",

        "Par une nuit sans lune, les Valmontis ouvrirent les portes du palais avec des clés achetées à des gardes qui n'en "
        "pouvaient plus d'avoir peur. Les mages Zippel brisèrent un à un les sceaux de magie qui protégeaient la salle du "
        "trône. Et les chevaliers Runkandel entrèrent.\n\n"
        "Le Roi Fou les attendait. La Fureur avait fait de lui quelque chose qui n'était plus tout à fait un homme. On dit "
        "que le combat dura jusqu'à l'aube, que le palais trembla, et que la moitié des chevaliers n'en ressortit jamais.\n\n"
        "Au lever du jour, Ryūkiri traversa le cœur d'Edward Von Eldanior, et la Fureur du Dragon s'éteignit avec lui.",

        "Les trois maisons auraient pu prendre la couronne. Elles avaient l'armée, l'or et la magie. Le peuple les aurait "
        "acclamées.\n\n"
        "Elles refusèrent.\n\n"
        "<i>« Aucun de nous n'est légitime, dit le chef des Runkandel. Un roi qui prend le trône par l'épée, c'est un second "
        "Edward. Et si l'un de nous s'y assoit, les deux autres lui déclareront la guerre avant l'hiver. »</i>\n\n"
        "Ils posèrent la Couronne de Fer sur le trône vide, firent distribuer au peuple les réserves du palais, et rentrèrent "
        "chacun dans sa marche. Depuis ce jour, le royaume n'a plus de roi. Il a trois gardiens qui ne s'aiment pas, et qui "
        "se surveillent.",

        "Les Runkandel ont gardé la lame. Quand ils apprirent d'où venait la Fureur, ils jurèrent qu'un jour Ryūkiri "
        "trancherait aussi le Dragon qui l'avait donnée. Depuis, le sang coule entre eux et les Luo, qui disent descendre de "
        "ce même Dragon. Ils ne savent pas à quel point ils ont raison, ni à quel point ils ont tort.\n\n"
        "Sur les anciennes terres du roi, les morts se relèvent encore, comme si la Fureur cherchait un nouveau corps.\n\n"
        "Le Sceau d'Eldanior dort toujours sous les cendres de l'aile de l'Est. La Couronne d'Aube est en lieu sûr. La Larme "
        "d'Or est retournée à celui qui l'avait versée, comme Arald l'avait voulu.\n\n"
        "Le Tombeau des Épreuves Divines attend. Si un jour un héritier digne y entre avec les trois reliques, je serai là "
        "pour le regarder. Comme la première fois.",

        "<i>Ces Tomes Impériaux ont été écrits de ma main, pour que le royaume se souvienne de ce qu'il a été, et de ce qu'il "
        "pourrait redevenir.</i>\n\n"
        "<i>Pardonne-moi, mon vieil ami. J'ai fermé les portes trop tard.</i>\n\n\n"
        "<b>Shino Luo</b>\n"
        "Empereur de l'Empire Luo\n"
        "Ami du Premier Roi, Arald Von Eldanior",
    ]),
]

# Acrostiche : premiere lettre de chaque tome (verifie par gen_histoires.py)
ACROSTICHE = "DRAGONDORE"
