"""Armes a feu du mod (v2, 2026-11-15) : mousquet de base, trois mousquets legendaires, armes de leurs transformations,
munitions, lunettes de visee, animation de baionnette, interactions et objets. Mecanique : Java (Mousquet, ArmeTransfo,
MousquetTirInteraction, SeuilEnduranceInteraction ; Colts : AiglesDuDesert et gen_aigles_akimbo.py).

Demandes de l'utilisateur (2026-11-15) :
  - Force -> degats (70 -> 2 000 a 3 000 points) et vitesse de la balle ; Agilite -> rechargement (1 s mini a 3 000) ;
    Endurance -> portee (800 blocs a 3 000), precision, grossissement de la lunette (paliers) et vue des entites lointaines ;
  - viseur plus beau (lunette dessinee ici), detail du mousquet ameliore, baionnette RETIREE du modele : elle n'apparait
    que le temps de l'estoc (variante « _Baionnette » echangee par ArmeTransfo), animation d'estoc dediee ;
  - signature du mousquet = Concentration (balle a precision et degats x2) ; message de distance quand la balle tue ;
  - trois legendaires remplacent les anciens (Tonnerre, Dragon, Etoiles) ; leur signature transforme l'arme 5 min :
      Black Bess  « Arcane Sniper » (precision)  noir, runes violettes, lunette   -> SARS militaire (5 balles de sniper
                                                                                    divines, 2 000 blocs)
      Green Colt  « Le Rapide »     (rapidite)   laque verte et or, carabine      -> Colts de cavalerie akimbo
      Blue Tiger  « Le Perceur »    (force)      bleu tigre, frein de bouche      -> canon de char a main (Obus Divin,
                                                                                    5 s, 100 blocs, x5)
    les armes a feu ne s'obtiennent que par un mousquet.

  Weapon_Mousquet               Mousquet       Peu commun   niv. 40   Brown Bess : noyer, canon poli, laiton
  Artefact_Mousquet_BlackBess   Black Bess     Legendaire   niv. 560  (+ variantes _Baionnette, hors menu)
  Artefact_Mousquet_GreenColt   Green Colt     Legendaire   niv. 540
  Artefact_Mousquet_BlueTiger   Blue Tiger     Legendaire   niv. 560
  Artefact_Sniper_SARS          SARS militaire Legendaire   niv. 560  (transformation, hors menu de recette)
  Artefact_Canon_Main           Canon de char  Legendaire   niv. 560
  Munition_Balle_Sniper_Divine, Munition_Obus_Divin (Forge Divine)

Modele : chaine vanilla du tromblon gardee (R-Attachment > Origin_Projectile > Origin_Item > Handle : tenu comme lui,
animations « Rifle ») ; tout est construit dans le repere du noeud Handle3 vanilla (platine) : +z vers la bouche, +y vers
le haut, +x a droite (cote de la platine).

Usage : python3 scripts/artefacts/gen_mousquets.py <src/main/resources> [dossier_apercu|-] [cle ...]
        cles : Mousquet BlackBess GreenColt BlueTiger SARS Canon Munitions Lunettes Anims (aucune = tout)
"""
import copy, json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import gen_divines as G  # noqa: E402  (outils de modelisation : son main() ne s'execute pas a l'import)

K, png = G.K, G.png
RES = G.RES
PREVIEW = G.PREVIEW
MODEL_REL = G.MODEL_REL
VANILLA = "Items/Weapons/Gun/Blunderbuss.blockymodel"
VANILLA_ITEM = "Server/Item/Items/Weapon/Gun/Weapon_Gun_Blunderbuss.json"
mixc = G.mixc
OLD = ("Artefact_Mousquet_Tonnerre", "Artefact_Mousquet_Dragon", "Artefact_Mousquet_Etoiles")   # remplaces le 2026-11-15
BAYONET = "_Baionnette"
ANIM_SET = "Mousquet"


def want(key):
    return not G.ONLY or key in G.ONLY


# ---------------------------------------------------------------------------------------------------------------------
# Peintures
# ---------------------------------------------------------------------------------------------------------------------
def bore(face, w, h, seed):
    img = K.canvas(w, h, (14, 12, 14))
    cx, cy = w / 2.0, h / 2.0
    for y in range(h):
        for x in range(w):
            d = max(abs(x + 0.5 - cx) / cx, abs(y + 0.5 - cy) / cy)
            img[y][x][:3] = list(mixc((4, 4, 6), (40, 38, 42), d * d))
    return img


def walnut(base, grain=0.24):
    """Noyer huile : veines longues ondulees, flammes plus sombres, pores fins, aretes polies par l'usage."""
    dark = mixc(base, (20, 10, 4), 0.5)

    def paint(face, w, h, seed):
        img = K.canvas(w, h, base)
        for y in range(h):
            for x in range(w):
                vein = K.fbm(x * 0.025 + seed, y * 0.38 + 0.4 * math.sin(x * 0.04 + seed))
                line = 0.5 + 0.5 * math.sin(vein * 16)
                flame = max(0.0, K.fbm(x * 0.08 + 7 + seed, y * 0.08) - 0.55) * 1.6
                pore = 0.05 * (K.noise(x * 0.9 + seed, y * 0.9) - 0.5)
                k = 1 + grain * (line - 0.5) * 1.3 + pore
                c = [ch * k for ch in base]
                img[y][x][:3] = list(mixc(c, dark, min(0.6, flame)))
        if min(w, h) >= 6:                                               # aretes polies (plus claires)
            for y in range(h):
                for x in range(w):
                    if min(x, y, w - 1 - x, h - 1 - y) == 0:
                        img[y][x][:3] = [min(255, ch * 1.12) for ch in img[y][x][:3]]
        return K.finish(img, face, edges=False)
    return paint


def tiger(base, stripe, grain=0.12):
    """Bois ou metal a rayures de tigre : bandes sombres ondulees, effilees aux bouts, sur un fond legerement veine."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, base)
        for y in range(h):
            for x in range(w):
                vein = K.fbm(x * 0.03 + seed, y * 0.4)
                k = 1 + grain * (math.sin(vein * 12) * 0.5)
                img[y][x][:3] = [ch * k for ch in base]
        period = 11.0
        for y in range(h):
            for x in range(w):
                u = (x + 4 * math.sin(y * 0.35 + seed) + 2 * K.noise(x * 0.2, y * 0.2 + seed)) % period
                taper = abs((y + 0.5) / h - 0.5) * 2                         # bandes effilees vers les bords
                width = 3.2 * (1 - 0.6 * taper)
                if u < width:
                    img[y][x][:3] = list(stripe)
        return K.finish(img, face, edges=False)
    return paint


def engraved(base, line, border=True):
    """Metal grave : filet en creux le long des bords et rinceaux espaces (platines, garnitures)."""
    m = G.metal(base, 0.3, finish=False)
    light = mixc(base, (255, 255, 255), 0.4)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if border and min(w, h) >= 6:
            for y in range(h):
                for x in range(w):
                    if min(x, y, w - 1 - x, h - 1 - y) == 1:
                        img[y][x][:3] = list(line)
        if w >= 12 and h >= 5:
            for x0 in range(h, w - h + 1, max(6, int(h * 1.4))):
                pts = [(x0 + math.cos(t) * t * h * 0.05, h / 2.0 + math.sin(t) * t * h * 0.05) for t in [i * 0.35 for i in range(16)]]
                K.stroke(img, [(px, py + 0.6) for px, py in pts], light, 0.3)
                K.stroke(img, pts, line, 0.35)
        return G.bevel(K.finish(img, face, edges=False))
    return paint


def fluted(base, groove):
    """Canon cannele : rainures longitudinales regulieres."""
    m = G.metal(base, 0.2, finish=False)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if face in ("front", "back"):
            return K.finish(img, face, edges=False)
        for y in range(h):
            if y % 3 == 1:
                for x in range(w):
                    img[y][x][:3] = list(groove)
        return K.finish(img, face, edges=False)
    return paint


def slots(base, hole, every=7, length=4):
    """Garde-main a fentes M-LOK : fentes sombres alignees sur les grandes faces."""
    m = G.metal(base, 0.12, finish=False)

    def paint(face, w, h, seed):
        img = m(face, w, h, seed)
        if face in ("front", "back") or h < 4:
            return K.finish(img, face, edges=False)
        y0, y1 = int(h * 0.35), max(int(h * 0.35) + 1, int(h * 0.65))
        for x0 in range(3, w - length - 2, every):
            for y in range(y0, y1):
                for x in range(x0, x0 + length):
                    img[y][x][:3] = list(hole)
        return K.finish(img, face, edges=False)
    return paint


def rubber(c):
    def paint(face, w, h, seed):
        img = K.canvas(w, h, c)
        for y in range(h):
            for x in range(w):
                k = 1 + 0.08 * (K.noise(x * 0.8 + seed, y * 0.8) - 0.5) - (0.12 if (x + y) % 4 == 0 else 0)
                img[y][x][:3] = [ch * k for ch in c]
        return img
    return paint


def flint(face, w, h, seed):
    img = K.canvas(w, h, (70, 66, 62))
    for y in range(h):
        for x in range(w):
            k = 1 + 0.2 * (K.noise(x * 0.8 + seed, y * 0.8) - 0.5)
            img[y][x][:3] = [c * k for c in (70, 66, 62)]
    return img


def lacquer_gold(base, line):
    """Laque brillante a filets d'or le long des grandes faces et rinceaux espaces."""
    def paint(face, w, h, seed):
        img = K.canvas(w, h, base)
        hi = mixc(base, (255, 240, 220), 0.35)
        for y in range(h):
            for x in range(w):
                s = max(0.0, math.sin((x * 0.6 + y) * 4.0 / max(w, 8))) ** 8
                img[y][x][:3] = list(mixc(base, hi, s * 0.5))
        if face not in ("top", "bottom") and h >= 6:
            for y in (1, h - 2):
                for x in range(1, w - 1):
                    K.put(img, x, y, line)
            if w > 3 * h:
                for x0 in range(h, w - h, 2 * h):
                    curl = [(x0 + math.cos(t) * t * h * 0.05, h / 2.0 + math.sin(t) * t * h * 0.05) for t in [i * 0.3 for i in range(20)]]
                    K.stroke(img, curl, line, 0.35)
        return G.bevel(K.finish(img, face, edges=False))
    return paint


# ---------------------------------------------------------------------------------------------------------------------
# Construction
# ---------------------------------------------------------------------------------------------------------------------
def frame(parts, grip_paint):
    """Chaine vanilla du tromblon, poignee remplacee par une boite cachee ; parts dans le repere de la platine (Handle3)."""
    van = G.vanilla_handle_children(VANILLA)
    h2 = next(c for c in van["children"] if c["name"] == "Handle2")
    h3 = next(c for c in h2["children"] if c["name"] == "Handle3")
    pos = lambda n: (n["position"]["x"], n["position"]["y"], n["position"]["z"])   # noqa: E731
    meca = G.pivot("Platine_Repere", pos(h3), h3["orientation"], parts)
    corps = G.pivot("Corps", pos(h2), h2["orientation"], [meca])
    return G.mount(VANILLA, (1, 1, 1), grip_paint, [corps])


def tube(name, pos, d, length, paint, sides=12, lit=False):
    """Tube rond a l'oeil : boites tournees autour de z (12 pans : 0, 30, 60 degres ; 8 pans : 0, 45)."""
    make = G.lit if lit else G.box
    angles = (0, 30, 60) if sides == 12 else (0, 45)
    out = []
    for i, a in enumerate(angles):
        k = 1.0 - 0.02 * i                                                  # pas de faces confondues
        out.append(make(name if i == 0 else "%s_%d" % (name, a), pos, (d * k, d * k, length - 0.25 * i), paint,
                        q=G.qz(a) if a else G.ID_Q))
    return out


def strap(name, a, b, width, thick, paint):
    """Bande droite de a a b (points y, z dans le plan de l'arme), posee a plat (largeur en x)."""
    (ya, za), (yb, zb) = a, b
    L = math.hypot(yb - ya, zb - za)
    ang = math.degrees(math.atan2(yb - ya, zb - za))                     # pente : avant plus haut -> qx negatif
    return G.box(name, (0, (ya + yb) / 2.0, (za + zb) / 2.0), (width, thick, L + 0.3), paint, q=G.qx(-ang))


def sides(name, pos, size, paint, q=G.ID_Q, lit=False):
    """Paire symetrique (droite / gauche) d'une piece posee de part et d'autre de l'arme."""
    make = G.lit if lit else G.box
    x, y, z = pos
    return [make(name + "_D", (x, y, z), size, paint, q=q), make(name + "_G", (-x, y, z), size, paint, q=q)]


A = 2.2                                                                  # axe du canon (le bois l'enchasse)


def brown_bess(p, z1=78.0, bayonet=False, bore_d=1.9):
    """Mousquet facon Brown Bess (Land Pattern), dans le repere de la platine : +z bouche, +y haut, +x platine.
    v2 : canon a 12 pans en trois sections (tonnerre, renfort, volee) et renflement de bouche, poincons d'epreuve, tenon,
    fut arrondi, crosse adoucie (aretes chanfreinees), platine gravee a filet et rinceaux, ressort et vis visibles,
    pontet cintre en trois segments, porte-baguette en trompette, bouton de bretelle, plaque de couche a talon.
    bayonet : baionnette a douille fixee (variante tenue le temps de l'estoc).
    p : wood, barrel, breech, brass, steel, lock, blade, sling."""
    box = G.box
    z0 = 4.0
    L = z1 - z0
    zs = lambda z: z0 + (z - z0) * L / 74.0                              # noqa: E731  (positions avant a l'echelle du canon)
    parts = []
    # ----- canon -----
    parts += tube("Canon_Tonnerre", (0, A, (z0 + 18) / 2.0), 3.6, 18 - z0, p["breech"])
    parts += tube("Canon_Renfort", (0, A, (18 + zs(44)) / 2.0), 3.25, zs(44) - 18 + 0.25, p["barrel"])
    parts += tube("Canon_Volee", (0, A, (zs(44) + z1 - 1.5) / 2.0), 2.95, z1 - 1.5 - zs(44) + 0.25, p["barrel"])
    parts += tube("Renflement", (0, A, z1 - 0.75), 3.25, 1.5, p["barrel"])
    parts += tube("Moulure_A", (0, A, 18.0), 3.9, 0.75, p["breech"], sides=8)
    parts += tube("Moulure_B", (0, A, z0 + 0.4), 4.0, 0.8, p["breech"], sides=8)
    parts += tube("Moulure_C", (0, A, zs(44)), 3.5, 0.5, p["barrel"], sides=8)
    parts += [box("Ame", (0, A, z1 + 0.1), (bore_d, bore_d, 0.25), bore),
              box("Queue_Culasse", (0, 3.25, z0 - 2.6), (1.4, 0.4, 5.6), p["breech"], q=G.qx(-4)),
              box("Vis_Queue", (0, 3.5, z0 - 3.6), (0.6, 0.25, 0.6), p["steel"]),
              box("Tenon", (0, A + 1.6, z1 - 2.4), (0.6, 0.5, 1.0), p["steel"]),           # tenon de baionnette = guidon
              box("Poincon_A", (0.9, A + 1.55, 9.0), (0.5, 0.25, 0.5), p["lock"]),
              box("Poincon_B", (0.9, A + 1.55, 10.4), (0.5, 0.25, 0.5), p["lock"])]
    # ----- bois : flancs de platine, fut arrondi, poignee, crosse adoucie -----
    fut_z0, fut_z1 = 6.0, z1 - 6.5
    fl = fut_z1 - fut_z0
    parts += [
        box("Bois_Platine", (0, 0.2, 0.6), (5.0, 5.6, 11.6), p["wood"]),
        box("Fut", (0, 0.65, (fut_z0 + fut_z1) / 2.0), (4.4, 3.7, fl), p["wood"]),
        box("Fut_Ventre", (0, -0.9, (fut_z0 + fut_z1) / 2.0 - 0.5), (3.0, 3.0, fl - 1.5), p["wood"], q=G.qz(45)),
        box("Raccord_Fut", (0, -1.75, 7.3), (4.3, 1.6, 3.2), p["wood"], q=G.qx(-12)),
        box("Poignee", (0, -0.5, -8.5), (3.6, 5.0, 8.4), p["wood"], q=G.qx(-16)),
        box("Poignee_Arrondi", (0, -0.5, -8.5), (2.9, 5.6, 8.0), p["wood"], q=K.qmul(G.qx(-16), G.qz(45))),
        box("Crosse_Busc", (0, -1.25, -20.1), (4.4, 4.5, 16.4), p["wood"], q=G.qx(-3)),
        box("Crosse_Talon", (0, -3.95, -20.1), (4.4, 4.5, 16.8), p["wood"], q=G.qx(-15)),
        box("Crosse_Chanfrein_H", (0, 0.65, -20.4), (3.2, 1.2, 15.8), p["wood"], q=K.qmul(G.qx(-3), G.qz(45))),
        box("Crosse_Chanfrein_B", (0, -6.1, -21.2), (3.0, 1.2, 14.8), p["wood"], q=K.qmul(G.qx(-15), G.qz(45))),
        box("Queue_Castor", (0, 3.05, -2.6), (2.4, 0.5, 2.6), p["wood"]),                 # bois sculpte derriere la culasse
        box("Moulure_Platine", (2.55, 0.9, -5.6), (0.25, 2.4, 2.2), p["wood"], q=G.qx(-25)),  # ressaut sculpte derriere la platine
    ]
    for i, z in enumerate((22.0, 38.0, 54.0)):                                         # goupilles du canon
        parts.append(box("Goupille_%d" % i, (2.25, 1.7, zs(z)), (0.25, 0.4, 0.4), p["steel"]))
        parts.append(box("Goupille_G_%d" % i, (-2.25, 1.7, zs(z)), (0.25, 0.4, 0.4), p["steel"]))
    # ----- embouchoir, baguette, porte-baguettes -----
    parts += [box("Embouchoir", (0, 0.65, fut_z1 + 0.4), (4.8, 4.1, 1.0), p["brass"]),
              box("Embouchoir_Ventre", (0, -0.9, fut_z1 + 0.4), (3.3, 3.3, 1.0), p["brass"], q=G.qz(45)),
              box("Baguette", (0, -1.9, (9.0 + z1 - 2.2) / 2.0), (0.8, 0.8, z1 - 2.2 - 9.0), p["steel"]),
              box("Baguette_Tete", (0, -1.9, z1 - 2.0), (1.25, 1.25, 1.0), p["steel"]),
              box("Baguette_Bouton", (0, -1.9, z1 - 1.25), (0.9, 0.9, 0.5), p["steel"]),
              box("Porte_Baguette_Queue", (0, -1.95, 11.0), (1.5, 1.5, 3.6), p["brass"]),
              box("Porte_Baguette_Gland", (0, -1.7, 8.7), (1.0, 1.0, 1.0), p["brass"])]
    for i, z in enumerate((28.0, 44.0)):
        parts.append(box("Porte_Baguette_%d" % i, (0, -1.95, zs(z)), (1.5, 1.5, 2.0), p["brass"]))
    parts += [box("Trompette", (0, -1.95, zs(60.0)), (1.6, 1.6, 2.6), p["brass"]),       # porte-baguette en trompette
              box("Trompette_Evasee", (0, -1.95, zs(60.0) + 1.5), (1.9, 1.9, 0.5), p["brass"])]
    # ----- platine (cote droit) : corps en banane, bassinet, batterie, ressorts, chien en col de cygne -----
    xs, xo = 2.75, 3.2
    lock = p.get("lock_plate", p["lock"])
    parts += [
        box("Platine_Avant", (xs, 1.1, 3.0), (0.5, 2.2, 5.0), lock),
        box("Platine_Arriere", (xs, 0.7, -2.0), (0.5, 2.0, 5.2), lock, q=G.qx(-8)),
        box("Platine_Queue", (xs, 0.0, -5.0), (0.5, 1.2, 1.6), lock, q=G.qx(-25)),
        box("Bassinet", (xo, 2.3, 3.3), (1.0, 0.5, 2.0), p["lock"]),
        box("Garde_Feu", (xo + 0.3, 2.75, 2.6), (0.4, 0.5, 0.8), p["lock"]),
        box("Batterie", (xo, 3.7, 4.1), (0.6, 2.4, 0.8), p["steel"], q=G.qx(-12)),
        box("Batterie_Pied", (xo, 2.65, 3.6), (0.6, 0.4, 1.6), p["steel"]),
        box("Ressort_Batterie", (xs + 0.3, 1.0, 6.2), (0.45, 0.6, 2.4), p["steel"]),
        box("Ressort_Boucle", (xs + 0.3, 1.55, 7.2), (0.45, 0.8, 0.5), p["steel"], q=G.qx(30)),
        box("Vis_Batterie", (xs + 0.55, 2.0, 4.6), (0.25, 0.5, 0.5), p["steel"]),
        box("Noix", (xs + 0.35, 1.2, -1.0), (0.5, 1.4, 1.4), p["steel"]),
        box("Chien_Col", (xo, 2.1, -1.45), (0.5, 2.1, 0.8), p["steel"], q=G.qx(-25)),
        box("Chien_Cygne", (xo, 3.3, -1.25), (0.5, 0.8, 1.6), p["steel"], q=G.qx(-20)),
        box("Chien_Crete", (xo, 3.0, -2.4), (0.5, 0.5, 1.0), p["steel"], q=G.qx(-30)),
        box("Machoire_Basse", (xo, 3.55, 0.3), (0.7, 0.45, 1.6), p["steel"]),
        box("Silex", (xo, 3.95, 0.9), (0.55, 0.5, 1.0), flint),
        box("Cuir_Silex", (xo, 3.95, 0.3), (0.6, 0.45, 0.8), G.wood((70, 50, 34), 0.05)),
        box("Machoire_Haute", (xo, 4.35, 0.3), (0.7, 0.45, 1.6), p["steel"]),
        box("Vis_Machoire", (xo, 4.85, 0.3), (0.35, 1.0, 0.35), p["steel"]),
        box("Vis_Machoire_Tete", (xo, 5.4, 0.3), (0.6, 0.3, 0.6), p["steel"]),
    ]
    # contre-platine en S (cote gauche) et ses vis
    parts += [box("Contre_Platine", (-xs, 0.8, 0.5), (0.35, 1.0, 6.0), p["brass"], q=G.qx(-6)),
              box("Contre_Platine_Queue", (-xs, 0.1, -3.3), (0.35, 0.8, 2.2), p["brass"], q=G.qx(-25)),
              box("Contre_Platine_Tete", (-xs, 1.3, 4.2), (0.35, 0.8, 1.8), p["brass"], q=G.qx(15)),
              box("Vis_Contre_A", (-xs - 0.2, 0.85, -1.0), (0.2, 0.5, 0.5), p["steel"]),
              box("Vis_Contre_B", (-xs - 0.2, 0.95, 3.0), (0.2, 0.5, 0.5), p["steel"])]
    # ----- detente, pontet cintre a gland, battants, bretelle -----
    parts += [
        box("Detente", (0, -3.0, -0.7), (0.45, 1.6, 0.6), p["steel"], q=G.qx(18)),
        box("Pontet", (0, -3.85, -0.6), (0.9, 0.5, 4.0), p["brass"]),
        box("Pontet_Avant", (0, -3.35, 2.2), (0.9, 0.5, 2.2), p["brass"], q=G.qx(-40)),
        box("Pontet_Arriere", (0, -3.35, -3.4), (0.9, 0.5, 2.2), p["brass"], q=G.qx(40)),
        box("Pontet_Queue_Avant", (0, -2.65, 5.4), (0.8, 0.4, 4.6), p["brass"]),
        box("Pontet_Gland", (0, -2.5, 8.0), (1.0, 0.7, 1.0), p["brass"]),
        box("Pontet_Queue_Arriere", (0, -3.15, -8.0), (0.8, 0.4, 7.0), p["brass"], q=G.qx(-16)),
        box("Battant_Pontet", (0, -4.4, 1.8), (0.35, 1.0, 1.0), p["brass"]),
        box("Battant_Fut", (0, -2.7, zs(46.0)), (0.35, 1.2, 1.2), p["brass"]),
    ]
    sling = [(-3.2, zs(46.0)), (-5.4, zs(33.0)), (-5.8, 18.0), (-4.8, 1.8)]
    for i, (a, b) in enumerate(zip(sling, sling[1:])):
        parts.append(strap("Bretelle_%d" % i, a, b, 1.0, 0.3, p["sling"]))
    parts += [box("Boucle_Bretelle", (0, -5.65, zs(25.0)), (1.3, 0.5, 1.1), p["brass"])]
    # ----- garnitures de la poignee et de la crosse -----
    parts += [box("Ecusson", (0, 2.15, -8.0), (1.2, 0.3, 1.8), p["brass"], q=G.qx(-16)),            # plaque de pouce
              box("Plaque_Couche", (0, -3.9, -28.4), (4.8, 9.4, 0.8), p["brass"]),
              box("Plaque_Couche_Talon", (0, 0.75, -27.6), (4.4, 0.5, 1.6), p["brass"]),
              box("Plaque_Couche_Queue", (0, 0.8, -25.8), (1.4, 0.35, 5.0), p["brass"], q=G.qx(-3)),
              box("Plaque_Couche_Vis", (0, 1.0, -24.6), (0.55, 0.3, 0.55), p["steel"]),
              box("Plaque_Couche_Vis_B", (0, -6.5, -28.85), (0.55, 0.55, 0.25), p["steel"])]
    if bayonet:
        # ----- baionnette a douille : douille sur la bouche, coude, lame triangulaire a droite du canon -----
        parts += tube("Douille", (0, A, z1 - 2.25), 3.6, 4.8, p["steel"], sides=8)
        parts += [box("Douille_Bague", (0, A, z1 - 4.4), (4.0, 4.0, 0.6), p["steel"]),
                  box("Coude", (1.5, A - 0.9, z1 + 0.4), (1.4, 0.9, 1.4), p["steel"], q=G.qy(35)),
                  box("Talon_Lame", (2.2, A - 1.1, z1 + 1.7), (1.3, 1.3, 1.4), p["steel"])]
        bx, byl = 2.2, A - 1.1
        zb = z1 + 2.4
        for i, (Lb, w, h) in enumerate(((10.0, 1.6, 1.3), (9.0, 1.3, 1.05), (6.0, 1.0, 0.8), (2.4, 0.55, 0.45))):
            parts.append(box("Lame_%d" % i, (bx, byl, zb + Lb / 2.0), (w, h, Lb), p["blade"]))
            parts.append(box("Lame_Arete_%d" % i, (bx + w * 0.25, byl, zb + Lb / 2.0), (w * 0.6, h * 0.6, Lb - 0.25), p["blade"], q=G.qz(45)))
            zb += Lb - 0.25
    return parts


# ----- les quatre mousquets -----
def build_basic(bayonet=False):
    """Brown Bess : noyer huile, canon et baionnette polis (« bright »), garnitures de laiton, bretelle de buffle blanchi."""
    p = {"wood": walnut((116, 68, 36)),
         "barrel": G.metal((160, 164, 174), 0.32), "breech": G.metal((144, 148, 158), 0.3),
         "brass": G.metal((214, 168, 82), 0.35), "steel": G.metal((150, 154, 164), 0.3), "lock": G.metal((128, 132, 142), 0.28),
         "lock_plate": engraved((134, 138, 148), (70, 72, 80)),
         "blade": G.metal((188, 194, 206), 0.4), "sling": G.wood((228, 220, 198), 0.05)}
    return frame(brown_bess(p, bayonet=bayonet), p["wood"])


ARCANE, ARCANE_CORE = (150, 70, 255), (232, 210, 255)


def build_black_bess(bayonet=False):
    """Black Bess, « Arcane Sniper » : ebene noire, canon d'acier noirci grave de runes violettes, garnitures d'argent
    sombre, longue lunette d'arcane (lentilles violettes), anneaux de runes lumineux, amethystes, cercle d'arcane sur la
    crosse. Canon long (precision)."""
    silver = G.metal((176, 180, 196), 0.35)
    p = {"wood": walnut((34, 28, 36), 0.3), "barrel": G.runes((44, 44, 54), (120, 60, 200), 6),
         "breech": G.metal((40, 40, 50), 0.3, edge=(176, 180, 196)), "brass": silver, "steel": G.metal((96, 98, 112), 0.3),
         "lock": G.metal((58, 58, 70), 0.28), "lock_plate": engraved((60, 60, 74), (150, 90, 230)),
         "blade": G.metal((150, 150, 170), 0.4), "sling": G.wood((40, 34, 40), 0.06)}
    z1 = 86.0
    glow = G.glow(ARCANE, ARCANE_CORE)
    parts = brown_bess(p, z1=z1, bayonet=bayonet)
    sy = A + 4.1                                                                              # lunette au-dessus du canon
    tubep = G.metal((30, 30, 36), 0.3, edge=(176, 180, 196))
    parts += tube("Lunette", (0, sy, 18), 2.2, 26, tubep)
    parts += tube("Lunette_Objectif", (0, sy, 32.5), 3.1, 4.0, tubep)
    parts += tube("Lunette_Oculaire", (0, sy, 3.6), 2.8, 3.0, tubep)
    parts += [G.lit("Lentille_Avant", (0, sy, 34.6), (2.4, 2.4, 0.25), glow),
              G.lit("Lentille_Arriere", (0, sy, 2.0), (1.9, 1.9, 0.25), glow),
              G.box("Collier_A", (0, sy - 1.6, 9.0), (1.4, 1.6, 1.6), silver),
              G.box("Collier_B", (0, sy - 1.6, 26.0), (1.4, 1.6, 1.6), silver),
              G.box("Tourelle_Haut", (0, sy + 1.5, 17.0), (1.2, 0.9, 1.2), silver),
              G.box("Tourelle_Cote", (1.5, sy, 17.0), (0.9, 1.2, 1.2), silver)]
    for i, z in enumerate((14.0, 30.0, 50.0, 70.0)):                                         # anneaux de runes du canon
        parts += tube("Anneau_Arcane_%d" % i, (0, A, z), 3.45, 0.5, glow, sides=8, lit=True)
    parts += [G.lit("Bouche_Arcane", (0, A, z1 + 0.25), (1.6, 1.6, 0.25), glow)]
    parts += sides("Veine", (1.55, A, 46), (0.25, 0.5, 60), glow, lit=True)
    parts += sides("Amethyste", (2.05, -0.5, -8.2), (0.4, 1.6, 1.6), G.gem((140, 60, 230)), q=G.qx(-16))
    for i, a in enumerate((0, 45)):                                                          # cercle d'arcane sur la crosse
        parts += sides("Cercle_%d" % i, (2.3, -3.2, -19.5), (0.25, 3.6, 3.6), glow, q=K.qmul(G.qx(-8), G.qx(a)), lit=True)
    parts += sides("Cercle_Coeur", (2.42, -3.2, -19.5), (0.25, 1.4, 1.4), G.gem((140, 60, 230)), q=G.qx(-8))
    parts += [G.box("Busc_Joue", (-2.6, -0.6, -18.5), (0.8, 2.6, 9.0), p["wood"], q=G.qx(-3))]     # appui-joue
    return frame(parts, p["wood"])


JADE, JADE_CORE = (60, 230, 130), (210, 255, 225)


def build_green_colt(bayonet=False):
    """Green Colt, « Le Rapide » : carabine courte, laque verte a filets d'or, canon d'acier jaspe vert a bandes d'or,
    ailettes d'or « de vitesse » le long du fut, emeraudes, veines de jade lumineuses, cartouchiere a cinq tubes sur la
    crosse (rechargement rapide)."""
    gold = G.metal((232, 188, 92), 0.35)
    p = {"wood": lacquer_gold((26, 104, 62), (232, 188, 92)), "barrel": G.metal((92, 128, 108), 0.3, edge=(232, 188, 92)),
         "breech": G.metal((70, 104, 86), 0.3, edge=(232, 188, 92)), "brass": gold, "steel": G.metal((120, 132, 126), 0.3),
         "lock": G.metal((84, 110, 96), 0.3), "lock_plate": engraved((92, 120, 104), (232, 188, 92)),
         "blade": G.metal((180, 200, 190), 0.4), "sling": G.wood((40, 70, 50), 0.06)}
    z1 = 66.0
    glow = G.glow(JADE, JADE_CORE)
    parts = brown_bess(p, z1=z1, bayonet=bayonet)
    for i, z in enumerate((20.0, 34.0, 48.0)):                                               # bandes d'or du canon
        parts += tube("Bande_Or_%d" % i, (0, A, z), 3.5, 0.75, gold, sides=8)
    for i in range(4):                                                                       # ailettes de vitesse
        z = 16 + i * 9.5
        parts += sides("Ailette_%d" % i, (2.35, 0.2, z), (0.3, 1.6, 4.5), gold, q=G.qx(-28))
    parts += sides("Veine", (1.5, A, 38), (0.25, 0.5, 42), glow, lit=True)
    parts += [G.lit("Bouche_Jade", (0, A, z1 + 0.25), (1.6, 1.6, 0.25), glow)]
    parts += sides("Emeraude", (2.05, -0.5, -8.2), (0.4, 1.6, 1.6), G.gem((40, 200, 100)), q=G.qx(-16))
    # cartouchiere a cinq tubes sur le flanc gauche de la crosse
    parts += [G.box("Cartouchiere", (-2.75, -2.6, -19.0), (0.8, 4.0, 7.0), G.metal((40, 70, 50), 0.2, edge=(232, 188, 92)), q=G.qx(-8))]
    for i in range(5):
        parts.append(G.box("Cartouche_%d" % i, (-3.3, -1.0 - i * 0.75, -16.4 - i * 0.3), (0.5, 0.5, 2.0), gold, q=G.qx(-8)))
        parts.append(G.lit("Cartouche_Pointe_%d" % i, (-3.3, -1.0 - i * 0.75, -15.25 - i * 0.3), (0.4, 0.4, 0.4), glow, q=G.qx(-8)))
    parts += sides("Plume", (2.3, -4.3, -21.0), (0.25, 1.2, 7.0), gold, q=G.qx(-20))          # plumes d'or sur la crosse
    return frame(parts, p["wood"])


ICE, ICE_CORE = (90, 170, 255), (220, 240, 255)


def build_blue_tiger(bayonet=False):
    """Blue Tiger, « Le Perceur » : crosse bleu nuit a rayures de tigre, canon epais d'acier bleui a rayures, tonnerre
    renforce de frettes, frein de bouche a trois fentes et pointe perforante sous la bouche, saphirs, veines de glace."""
    white = G.metal((214, 222, 236), 0.35)
    p = {"wood": tiger((36, 70, 150), (12, 16, 30)), "barrel": tiger((58, 84, 136), (18, 24, 40), 0.06),
         "breech": G.metal((48, 70, 120), 0.3, edge=(214, 222, 236)), "brass": white, "steel": G.metal((120, 132, 156), 0.3),
         "lock": G.metal((52, 74, 120), 0.3), "lock_plate": engraved((56, 80, 128), (190, 220, 255)),
         "blade": G.metal((190, 205, 230), 0.4), "sling": G.wood((24, 30, 50), 0.06)}
    z1 = 80.0
    glow = G.glow(ICE, ICE_CORE)
    parts = brown_bess(p, z1=z1, bayonet=bayonet, bore_d=2.3)
    for i, z in enumerate((7.0, 11.0, 15.0)):                                                # frettes du tonnerre
        parts += tube("Frette_%d" % i, (0, A, z), 4.2, 1.0, p["breech"], sides=8)
    # frein de bouche (trois fentes) et pointe perforante
    parts += tube("Frein", (0, A, z1 - 3.0), 3.8, 6.0, p["breech"], sides=8)
    for i in range(3):
        parts += sides("Fente_%d" % i, (1.85, A, z1 - 5.0 + i * 1.8), (0.25, 1.6, 0.8), G.metal((14, 16, 24), 0.05))
    parts += [G.box("Pointe_Socle", (0, A - 2.4, z1 - 1.0), (1.6, 1.4, 2.6), white),
              G.box("Pointe", (0, A - 2.4, z1 + 1.6), (1.0, 1.0, 3.0), white, q=G.qz(45)),
              G.box("Pointe_Fine", (0, A - 2.4, z1 + 3.4), (0.6, 0.6, 1.2), white, q=G.qz(45)),
              G.lit("Bouche_Glace", (0, A, z1 + 0.25), (2.0, 2.0, 0.25), glow)]
    parts += sides("Veine", (1.6, A, 40), (0.25, 0.5, 46), glow, lit=True)
    parts += sides("Saphir", (2.05, -0.5, -8.2), (0.4, 1.6, 1.6), G.gem((50, 110, 255)), q=G.qx(-16))
    parts += sides("Croc", (2.3, -3.2, -19.0), (0.25, 3.0, 1.0), white, q=G.qx(-35))           # griffures de tigre sur la crosse
    parts += sides("Croc_B", (2.3, -3.0, -21.2), (0.25, 3.0, 1.0), white, q=G.qx(-35))
    return frame(parts, p["wood"])


# ----- armes des transformations -----
def build_sars():
    """SARS militaire (forme de Black Bess) : fusil de precision a verrou. Canon cannele et frein de bouche a events,
    garde-main flottant a fentes, rail Picatinny, lunette de combat (tourelles, pare-soleil, lentilles violettes),
    culasse et levier de verrou, chargeur de 5 cartouches, poignee pistolet, crosse squelette a busc reglable et plaque
    de couche en caoutchouc, bipied replie, monopode. Noir mat a accents violets d'arcane."""
    box = G.box
    black, black2 = G.metal((34, 34, 40), 0.12), G.metal((50, 50, 58), 0.15)
    violet = G.metal((80, 50, 120), 0.2)
    glow = G.glow(ARCANE, ARCANE_CORE)
    rail = G.metal((24, 24, 28), 0.1)
    parts = []
    parts += tube("Canon", (0, A + 0.6, 46), 2.4, 62, fluted((60, 60, 70), (24, 24, 30)))
    parts += [box("Frein", (0, A + 0.6, 80.0), (3.4, 3.0, 6.0), black2),
              box("Frein_Avant", (0, A + 0.6, 83.25), (2.8, 2.6, 0.5), black)]
    for i in range(3):
        parts += sides("Event_%d" % i, (1.6, A + 0.6, 78.2 + i * 1.6), (0.25, 1.8, 0.9), G.metal((8, 8, 10), 0.02))
    parts += [box("Ame", (0, A + 0.6, 83.6), (1.1, 1.1, 0.25), bore)]
    # garde-main flottant (octogonal a fentes) et rail
    parts += [box("Garde_Main", (0, A + 0.6, 30.0), (4.4, 4.4, 34.0), slots((40, 40, 46), (10, 10, 12))),
              box("Garde_Main_Pans", (0, A + 0.6, 30.0), (4.2, 4.2, 33.5), slots((40, 40, 46), (10, 10, 12)), q=G.qz(45))]
    # boitier, culasse, levier
    parts += [box("Boitier", (0, A + 0.4, 3.0), (4.0, 4.6, 22.0), black2),
              box("Boitier_Dessus", (0, A + 2.9, 3.0), (3.0, 0.6, 21.0), black),
              box("Fenetre", (2.05, A + 1.0, 4.5), (0.25, 1.4, 5.0), G.metal((12, 12, 14), 0.02)),
              box("Culasse", (2.1, A + 1.0, 4.5), (0.4, 1.0, 4.4), G.metal((160, 160, 172), 0.35)),
              box("Levier", (2.9, A + 0.2, -4.0), (1.8, 0.6, 0.6), G.metal((150, 150, 160), 0.3), q=G.qz(-30)),
              box("Levier_Boule", (3.9, A - 0.4, -4.0), (1.3, 1.3, 1.3), G.metal((30, 30, 34), 0.15)),
              box("Rail", (0, A + 3.45, 18.0), (1.8, 0.5, 52.0), rail)]
    for i in range(24):
        parts.append(box("Cran_%d" % i, (0, A + 3.85, -7.0 + i * 2.15), (2.0, 0.4, 0.9), rail))
    # lunette de combat
    sy = A + 7.4
    scope = G.metal((28, 28, 34), 0.2)
    parts += tube("Lunette", (0, sy, 12.0), 2.6, 22.0, scope)
    parts += tube("Lunette_Objectif", (0, sy, 26.0), 3.8, 7.0, scope)
    parts += tube("Pare_Soleil", (0, sy, 31.25), 3.6, 3.5, scope, sides=8)
    parts += tube("Lunette_Oculaire", (0, sy, -1.0), 3.3, 5.0, scope)
    parts += tube("Bonnette", (0, sy, -4.0), 3.5, 1.0, rubber((20, 20, 22)), sides=8)
    parts += [G.lit("Lentille_Avant", (0, sy, 33.1), (3.0, 3.0, 0.25), glow),
              G.lit("Lentille_Arriere", (0, sy, -4.6), (2.4, 2.4, 0.25), glow),
              box("Tourelle_Haut", (0, sy + 2.0, 11.0), (1.8, 1.4, 1.8), black2),
              box("Tourelle_Cote", (2.0, sy, 11.0), (1.4, 1.8, 1.8), black2),
              box("Tourelle_Gauche", (-1.8, sy, 11.0), (1.0, 1.4, 1.4), black2),
              box("Bague_A", (0, sy - 2.0, 3.0), (2.0, 2.0, 1.6), black),
              box("Bague_B", (0, sy - 2.0, 19.0), (2.0, 2.0, 1.6), black),
              box("Rune_Tourelle", (0, sy + 2.75, 11.0), (1.0, 0.25, 1.0), violet)]
    # chargeur, pontet, poignee pistolet
    parts += [box("Chargeur", (0, A - 3.6, 6.0), (2.6, 5.0, 5.4), black2, q=G.qx(-6)),
              box("Chargeur_Talon", (0, A - 6.2, 6.3), (3.0, 0.6, 5.8), black, q=G.qx(-6)),
              G.lit("Chargeur_Rune", (1.35, A - 3.6, 6.0), (0.25, 1.4, 1.4), glow, q=G.qx(-6)),
              box("Pontet", (0, -2.9, -1.0), (1.0, 0.5, 6.0), black),
              box("Pontet_Avant", (0, -1.6, 1.8), (1.0, 2.4, 0.5), black),
              box("Detente", (0, -1.6, -0.6), (0.4, 1.6, 0.6), G.metal((150, 150, 160), 0.3), q=G.qx(15)),
              box("Poignee", (0, -3.6, -6.4), (2.8, 7.0, 3.4), rubber((32, 32, 36)), q=G.qx(-22)),
              box("Poignee_Dos", (0, -2.6, -7.9), (2.4, 5.0, 0.8), black2, q=G.qx(-22))]
    # crosse squelette, busc reglable, plaque de couche, monopode
    parts += [box("Crosse_Haut", (0, A + 0.6, -17.0), (2.6, 2.4, 20.0), black2),
              box("Crosse_Bas", (0, -4.6, -21.0), (2.6, 1.8, 14.0), black2, q=G.qx(8)),
              box("Crosse_Montant", (0, -1.2, -12.0), (2.4, 6.0, 1.6), black2, q=G.qx(-20)),
              box("Crosse_Talon", (0, -1.0, -27.0), (2.8, 8.4, 1.6), black2),
              box("Plaque_Couche", (0, -1.0, -28.2), (3.2, 9.2, 0.8), rubber((22, 22, 24))),
              box("Busc", (0, A + 2.6, -18.0), (2.2, 1.0, 10.0), black),
              box("Busc_Montant_A", (0, A + 1.8, -15.0), (0.6, 1.4, 0.6), G.metal((150, 150, 160), 0.3)),
              box("Busc_Montant_B", (0, A + 1.8, -21.0), (0.6, 1.4, 0.6), G.metal((150, 150, 160), 0.3)),
              box("Monopode", (0, -6.4, -24.0), (0.8, 2.6, 0.8), black)]
    parts += sides("Rune_Crosse", (1.35, A + 0.6, -17.0), (0.25, 1.0, 8.0), violet)
    # bipied replie sous le garde-main
    for s in (1, -1):
        n = "D" if s > 0 else "G"
        parts += [box("Bipied_%s" % n, (0.9 * s, A - 2.1, 33.0), (0.6, 0.6, 14.0), black),
                  box("Bipied_Pied_%s" % n, (0.9 * s, A - 2.1, 26.2), (0.8, 0.8, 0.8), rubber((20, 20, 22)))]
    parts += [box("Bipied_Charniere", (0, A - 1.9, 40.5), (2.6, 1.2, 1.6), black2)]
    return frame(parts, black)


def build_canon():
    """Canon de char a main (forme de Blue Tiger) : tube lisse a gaine thermique, evacuateur de fumees renfle, frein de
    bouche a double chicane, culasse a coin et sa poignee, deux cylindres de recul, viseur periscopique, poignee pistolet
    a l'arriere et poignee avant verticale, appui d'epaule. Acier bleu a rayures de tigre, bandes d'or, runes de glace."""
    box = G.box
    blue = tiger((52, 80, 140), (14, 18, 32), 0.06)
    steel = G.metal((70, 96, 150), 0.3, edge=(214, 222, 236))
    dark = G.metal((30, 38, 56), 0.15)
    gold = G.metal((232, 188, 92), 0.35)
    glow = G.glow(ICE, ICE_CORE)
    Y = A + 1.2
    parts = []
    parts += tube("Tube", (0, Y, 40), 5.6, 64, blue)
    parts += tube("Evacuateur", (0, Y, 44), 7.0, 8.0, steel)
    parts += tube("Gaine_A", (0, Y, 26), 6.0, 2.0, dark, sides=8)
    parts += tube("Gaine_B", (0, Y, 58), 6.0, 2.0, dark, sides=8)
    for i, z in enumerate((14.0, 66.0)):
        parts += tube("Bande_Or_%d" % i, (0, Y, z), 6.2, 1.0, gold, sides=8)
    # frein de bouche a double chicane
    parts += [box("Frein", (0, Y, 76.0), (9.0, 6.4, 8.0), steel),
              box("Frein_Chicane_A", (0, Y, 74.5), (9.4, 6.8, 1.0), dark),
              box("Frein_Chicane_B", (0, Y, 77.5), (9.4, 6.8, 1.0), dark),
              box("Ame", (0, Y, 80.1), (3.4, 3.4, 0.25), bore)]
    parts += sides("Frein_Event", (4.6, Y, 76.0), (0.25, 4.0, 5.0), G.metal((8, 10, 14), 0.02))
    parts += [G.lit("Bouche_Glace", (0, Y, 80.0), (4.2, 4.2, 0.25), glow)]
    # culasse a coin, cylindres de recul, poignee de culasse
    parts += [box("Culasse", (0, Y, -4.0), (9.0, 9.0, 18.0), steel),
              box("Culasse_Arriere", (0, Y, -13.5), (7.6, 7.6, 1.5), dark),
              box("Coin", (0, Y, 2.0), (10.0, 4.0, 3.0), dark),
              box("Poignee_Culasse", (5.4, Y + 1.0, -6.0), (1.2, 1.2, 6.0), gold),
              box("Poignee_Culasse_Bras", (5.0, Y + 1.0, -2.8), (1.0, 1.0, 1.4), gold),
              box("Rivet_A", (4.6, Y + 3.5, -10.0), (0.4, 0.8, 0.8), gold),
              box("Rivet_B", (4.6, Y - 3.5, -10.0), (0.4, 0.8, 0.8), gold)]
    for s in (1, -1):
        n = "H" if s > 0 else "B"
        parts += tube("Recul_%s" % n, (0, Y + 4.4 * s, 10.0), 2.2, 24.0, steel, sides=8)
        parts += [box("Recul_Collier_%s" % n, (0, Y + 3.4 * s, 20.5), (2.4, 2.0, 1.6), dark)]
    # viseur periscopique (gauche, en haut)
    parts += [box("Viseur", (-4.2, Y + 6.2, -2.0), (2.6, 3.4, 5.0), dark),
              box("Viseur_Tete", (-4.2, Y + 8.4, -1.0), (2.8, 1.2, 6.0), dark),
              G.lit("Viseur_Lentille", (-4.2, Y + 8.4, 2.1), (2.0, 0.8, 0.25), glow)]
    # poignees, detente, appui d'epaule
    parts += [box("Poignee", (0, -3.4, -6.6), (2.8, 7.0, 3.4), rubber((26, 30, 40)), q=G.qx(-20)),
              box("Pontet", (0, -2.6, -1.4), (1.0, 0.5, 6.0), dark),
              box("Detente", (0, -1.4, -1.2), (0.5, 1.6, 0.6), gold, q=G.qx(15)),
              box("Poignee_Avant", (0, Y - 7.2, 22.0), (2.6, 7.0, 2.8), rubber((26, 30, 40)), q=G.qx(10)),
              box("Poignee_Avant_Collier", (0, Y - 3.6, 22.0), (3.6, 1.2, 4.0), dark),
              box("Appui", (0, Y - 1.0, -16.0), (3.2, 7.0, 5.5), dark),
              box("Appui_Coussin", (0, Y - 1.0, -19.25), (4.0, 8.4, 1.0), rubber((22, 22, 26)))]
    parts += sides("Rune_Culasse", (4.55, Y, -4.0), (0.25, 3.0, 8.0), glow, lit=True)
    parts += sides("Rune_Tube", (2.85, Y, 40.0), (0.25, 0.6, 40.0), glow, lit=True)
    return frame(parts, dark)


# ---------------------------------------------------------------------------------------------------------------------
# Projectiles et munitions
# ---------------------------------------------------------------------------------------------------------------------
HOLY, HOLY_CORE = (255, 206, 96), (255, 252, 230)
BRASS = (214, 168, 84)


def horn(face, w, h, seed):
    """Corne polie : creme a la base, brun sombre vers la pointe (le long de x), cernes de croissance."""
    img = K.canvas(w, h, (220, 200, 160))
    for y in range(h):
        for x in range(w):
            t = (x + 0.5) / w
            c = mixc((214, 200, 170), (58, 36, 22), t ** 1.1)
            ring = 0.08 * math.sin(x * 1.3 + seed) + 0.04 * (K.noise(x * 0.4 + seed, y * 0.6) - 0.5)
            img[y][x][:3] = [ch * (1 + ring) for ch in c]
    return K.finish(img, face, edges=False)


def round_ball(name, pos, d, paint):
    """Boule (trois boites croisees : rendu arrondi a l'oeil)."""
    x, y, z = pos
    return [G.box(name, pos, (d, d * 0.72, d * 0.72), paint),
            G.box(name + "_B", pos, (d * 0.72, d, d * 0.72), paint),
            G.box(name + "_C", pos, (d * 0.72, d * 0.72, d), paint),
            G.box(name + "_D", pos, (d * 0.86, d * 0.86, d * 0.86), paint, q=K.qmul(G.qx(45), G.qy(45)))]


def powder_horn_parts():
    """Corne a poudre (Poudre a canon) le long de x : corne courbe en sept troncons qui s'affinent, culot de laiton rivete,
    bec de laiton et son bouchon de bois, cordon de cuir, etiquette de papier « poudre fine »."""
    brass = G.metal(BRASS, 0.35)
    wood = G.wood((110, 70, 40), 0.15)
    leather = G.wood((92, 58, 34), 0.08)
    parts = []
    n = 7
    for i in range(n):
        t = i / (n - 1.0)
        x = -7.0 + 14.0 * t
        y = 4.2 * t * t
        d = 6.0 - 4.0 * t
        ang = math.degrees(math.atan2(8.4 * t, 14.0))                   # pente de la courbe
        parts.append(G.box("Corne_%d" % i, (x, y, 0), (2.6, d, d), horn, q=G.qz(ang)))
        parts.append(G.box("Corne_%d_P" % i, (x, y, 0), (2.4, d * 0.96, d * 0.96), horn, q=K.qmul(G.qz(ang), G.qx(45))))
    parts += [G.box("Culot", (-8.6, 0, 0), (0.75, 6.6, 6.6), brass),
              G.box("Culot_Pans", (-8.6, 0, 0), (0.6, 6.4, 6.4), brass, q=G.qx(45)),
              G.box("Culot_Rivet", (-9.05, 0, 0), (0.25, 1.5, 1.5), G.metal((120, 90, 50), 0.2)),
              G.box("Bague", (-6.4, 0.1, 0), (0.75, 6.4, 6.4), brass, q=G.qz(2)),
              G.box("Bec", (8.6, 4.5, 0), (2.4, 1.6, 1.6), brass, q=G.qz(18)),
              G.box("Bouchon", (9.9, 5.2, 0), (1.0, 1.25, 1.25), wood, q=G.qz(18)),
              G.box("Etiquette", (-1.5, 0.3, 2.65), (3.5, 2.5, 0.25), G.wood((228, 214, 180), 0.03))]
    for i, (a, b) in enumerate((((-8.4, 3.0), (-3.0, 5.4)), ((-3.0, 5.4), (3.0, 6.2)), ((3.0, 6.2), (8.2, 4.4)))):
        L = math.hypot(b[0] - a[0], b[1] - a[1])
        parts.append(G.box("Cordon_%d" % i, ((a[0] + b[0]) / 2, (a[1] + b[1]) / 2, 0), (L + 0.3, 0.5, 0.75), leather,
                           q=G.qz(math.degrees(math.atan2(b[1] - a[1], b[0] - a[0])))))
    return parts


def ball_pouch_parts():
    """Balles de fer : bourse de cuir a cordon serre, rabat ouvert, trois balles de fer qui en depassent."""
    leather, dark = G.wood((84, 52, 30), 0.12), G.wood((52, 32, 18), 0.08)
    iron = G.metal((92, 96, 108), 0.55)
    parts = [G.box("Bourse", (0, -1.5, 0), (5.0, 4.4, 5.0), leather),
             G.box("Bourse_Ventre", (0, -1.8, 0), (4.4, 4.0, 5.6), leather, q=G.qy(45)),
             G.box("Col", (0, 1.4, 0), (4.6, 1.0, 4.6), dark),
             G.box("Cordon", (0, 1.4, 0), (5.0, 0.5, 5.0), G.wood((200, 180, 140), 0.05)),
             G.box("Rabat", (-2.6, 2.1, 0), (0.5, 2.4, 4.0), leather, q=G.qz(-25)),
             G.box("Couture", (0, -1.5, 3.05), (5.0, 0.25, 0.25), dark)]
    for i, (x, y, z) in enumerate(((0.6, 2.9, 0.4), (-1.1, 2.6, -0.9), (1.2, 2.5, -1.4))):
        parts += round_ball("Balle_%d" % i, (x, y + 0.6, z), 2.8, iron)
    return parts


def shell_parts(scale=1.0, lit_tip=True):
    """Obus divin le long de x (pointe vers +x) : culot et gorge, douille de laiton conique, ceinture de cuivre double,
    corps d'acier bleu a bande de marquage blanche et chiffres, ogive d'or blanc, fusee d'acier a bague de reglage, coeur
    lumineux et anneau de runes."""
    s = scale
    brass, brass_dark = G.metal(BRASS, 0.32), G.metal(mixc(BRASS, (60, 40, 20), 0.35), 0.15)
    steel = G.metal((70, 96, 150), 0.3)
    copper = G.metal((196, 112, 70), 0.35)
    white = G.metal((226, 230, 236), 0.2)
    head = G.metal(mixc(G.GOLD_LIGHT, (255, 255, 255), 0.35), 0.35)
    fuze = G.metal((150, 156, 170), 0.35)
    glow = G.glow(ICE, ICE_CORE)
    kids = [G.box("Culot", (-10.4 * s, 0, 0), (0.75 * s, 6.8 * s, 6.8 * s), brass),
            G.box("Gorge", (-9.8 * s, 0, 0), (0.5 * s, 5.6 * s, 5.6 * s), brass_dark),
            G.box("Douille", (-5.5 * s, 0, 0), (8.5 * s, 6.0 * s, 6.0 * s), brass),
            G.box("Douille_Pans", (-5.5 * s, 0, 0), (8.25 * s, 5.8 * s, 5.8 * s), brass, q=G.qx(45)),
            G.box("Sertissage", (-1.1 * s, 0, 0), (0.5 * s, 5.6 * s, 5.6 * s), brass_dark),
            G.box("Corps", (3.0 * s, 0, 0), (6.5 * s, 5.4 * s, 5.4 * s), steel),
            G.box("Corps_Pans", (3.0 * s, 0, 0), (6.25 * s, 5.2 * s, 5.2 * s), steel, q=G.qx(45)),
            G.box("Ceinture_A", (0.2 * s, 0, 0), (0.75 * s, 6.0 * s, 6.0 * s), copper),
            G.box("Ceinture_B", (1.2 * s, 0, 0), (0.5 * s, 5.9 * s, 5.9 * s), copper, q=G.qx(45)),
            G.box("Marquage", (4.0 * s, 0, 0), (1.0 * s, 5.55 * s, 5.55 * s), white),
            G.box("Ogive", (7.4 * s, 0, 0), (2.6 * s, 4.4 * s, 4.4 * s), head),
            G.box("Ogive_Pans", (7.4 * s, 0, 0), (2.4 * s, 4.2 * s, 4.2 * s), head, q=G.qx(45)),
            G.box("Ogive_Fine", (9.2 * s, 0, 0), (1.2 * s, 3.0 * s, 3.0 * s), head),
            G.box("Fusee", (10.4 * s, 0, 0), (1.4 * s, 1.9 * s, 1.9 * s), fuze),
            G.box("Fusee_Bague", (10.0 * s, 0, 0), (0.4 * s, 2.3 * s, 2.3 * s), fuze, q=G.qx(45))]
    if lit_tip:
        kids += [G.lit("Coeur", (11.2 * s, 0, 0), (0.25 * s, 1.2 * s, 1.2 * s), glow),
                 G.lit("Runes", (5.6 * s, 0, 0), (0.5 * s, 5.5 * s, 5.5 * s), glow, q=G.qx(22.5))]
    return kids


def sniper_parts():
    """Cartouche de sniper divine le long de x : culot a gorge d'extraction et amorce, longue douille de laiton a epaule et
    goulot, ogive fine d'or blanc a chemise, pointe d'arcane lumineuse, rune gravee."""
    brass = G.metal(BRASS, 0.32)
    brass_dark = G.metal(mixc(BRASS, (60, 40, 20), 0.35), 0.15)
    head = G.metal(mixc(G.GOLD_LIGHT, (255, 255, 255), 0.35), 0.35)
    glow = G.glow(ARCANE, ARCANE_CORE)
    return [G.box("Culot", (-11.1, 0, 0), (0.75, 4.0, 4.0), brass),
            G.box("Amorce", (-11.55, 0, 0), (0.25, 1.4, 1.4), G.metal((200, 150, 120), 0.3)),
            G.box("Gorge", (-10.4, 0, 0), (0.6, 3.1, 3.1), brass_dark),
            G.box("Douille", (-3.6, 0, 0), (13.0, 3.6, 3.6), brass),
            G.box("Douille_Pans", (-3.6, 0, 0), (12.75, 3.45, 3.45), brass, q=G.qx(45)),
            G.box("Epaule", (3.6, 0, 0), (1.4, 2.9, 2.9), brass, q=G.qx(45)),
            G.box("Epaule_B", (3.6, 0, 0), (1.4, 2.6, 2.6), brass),
            G.box("Goulot", (5.3, 0, 0), (2.0, 2.0, 2.0), brass),
            G.box("Ogive", (8.0, 0, 0), (3.6, 1.8, 1.8), head),
            G.box("Ogive_Pans", (8.0, 0, 0), (3.4, 1.7, 1.7), head, q=G.qx(45)),
            G.box("Ogive_Fine", (10.4, 0, 0), (1.4, 1.2, 1.2), head),
            G.lit("Pointe", (11.4, 0, 0), (0.75, 0.75, 0.75), glow),
            G.lit("Rune", (-4.0, 0, 0), (0.75, 3.65, 3.65), glow, q=G.qx(45))]


def ammo_model(item_id, kids, grip, layout=((0, 0, 1.0),), view=None, margin=0.1, yaw=18, pitch=14):
    """Munition tenue et posee : hierarchie de la fleche vanilla ; icone (exemplaires decales selon layout)."""
    G.M = K.Model(item_id)
    G.M.reuse = True
    kids = kids() if callable(kids) else kids                         # pieces construites une fois le modele cree
    root, handle = G.mount("Items/Weapons/Arrow/Arrow.blockymodel", grip, G.metal(BRASS, 0.3), kids)
    G.renumber(root)
    G.fix_coplanar(handle, item_id)
    W, H, tex = G.M.texture()
    png.write(os.path.join(RES, "Common", MODEL_REL, item_id + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(RES, "Common", MODEL_REL, item_id + ".blockymodel"), "w"), indent=1)
    lifted = [[[min(255, c[0] * 1.12 + 10), min(255, c[1] * 1.12 + 10), min(255, c[2] * 1.12 + 12), c[3]] for c in row] for row in tex]
    S = 256
    one = K.draw(G.handle_quads(handle, lifted, view or G.qz(40)), S, yaw, pitch, margin=margin)
    big = [[[0, 0, 0, 0] for _ in range(S)] for _ in range(S)]
    for dx, dy, k in layout:
        for y in range(S):
            for x in range(S):
                p = one[y][x]
                X, Y = x + dx, y + dy
                if p[3] > 0 and 0 <= X < S and 0 <= Y < S:
                    big[Y][X] = [min(255, p[0] * k), min(255, p[1] * k), min(255, p[2] * k), p[3]]
    icon = G.outline(K.downscale(big, 4))
    png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", item_id + ".png"), 64, 64, icon)
    return icon


def projectile_model(name, kids, light, trail):
    """Modele en vol (le long de z) + asset de modele (traine, lumiere) + configuration de projectile neutre."""
    G.M = K.Model(name)
    G.M.reuse = True
    root = G.pivot("Root", (0, 0, 0), G.qy(-90), kids)                       # x du modele -> z de vol
    G.renumber(root)
    W, H, tex = G.M.texture()
    out = os.path.join(RES, "Common", "Items", "Projectiles")
    os.makedirs(out, exist_ok=True)
    png.write(os.path.join(out, name + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(out, name + ".blockymodel"), "w"), indent=1)
    trail_id = name + "_Trail"
    dump(os.path.join(RES, "Server", "Entity", "Trails", trail_id + ".json"), trail)
    dump(os.path.join(RES, "Server", "Models", "Projectiles", "Abilities", name + ".json"), {
        "$Comment": "%s, deplace par le code (Mousquet). Genere par scripts/artefacts/gen_mousquets.py." % name,
        "Model": "Items/Projectiles/%s.blockymodel" % name, "Texture": "Items/Projectiles/%s.png" % name,
        "MinScale": 1.0, "MaxScale": 1.0,
        "HitBox": {"Max": {"X": 0.3, "Y": 0.3, "Z": 0.6}, "Min": {"X": -0.3, "Y": -0.3, "Z": -0.6}},
        "Trails": [{"TrailId": trail_id, "TargetNodeName": "Root", "RotationOffset": {"Roll": r}} for r in (0, 90)],
        "Light": light})


def carrier(name, model):
    neutral = {"Interactions": [{"Type": "Simple", "RunTime": 0.01}]}
    dump(os.path.join(RES, "Server", "ProjectileConfigs", "Weapons", name + ".json"), {
        "$Comment": "Porteur deplace par le code (Mousquet). Interactions neutres obligatoires. scripts/artefacts/gen_mousquets.py.",
        "Model": model, "UseModelScale": True,
        "Physics": {"Type": "Standard", "Gravity": 0, "TerminalVelocityAir": 1, "TerminalVelocityWater": 1, "Bounciness": 0,
                    "SticksVertically": False},
        "LaunchForce": 0, "SpawnOffset": {"X": 0, "Y": 0, "Z": 0},
        "Interactions": {"ProjectileHit": neutral, "ProjectileMiss": neutral}})


def munitions():
    """Poudre a canon et balle de fer (mousquets), balle de sniper divine (SARS), obus divin (canon) : modeles, icones,
    objets, recettes ; porteurs des projectiles en vol."""
    icons = {POWDER: ammo_model(POWDER, powder_horn_parts, (1, 1, 1), margin=0.08),
             BALL: ammo_model(BALL, ball_pouch_parts, (1, 1, 1), view=G.qz(0), margin=0.1, yaw=30, pitch=34)}
    arrow = G.zjson("Server/Item/Items/Weapon/Arrow/Weapon_Arrow_Crude.json")
    d = os.path.join(RES, "Server", "Item", "Items", "Weapon", "Gun")
    dump(os.path.join(d, POWDER + ".json"), {
        "$Comment": "Munition des mousquets (1 par tir, avec 1 Balle de fer). scripts/artefacts/gen_mousquets.py.",
        "TranslationProperties": {"Name": "Poudre à canon",
                                  "Description": "Corne à poudre noire, charbon de bois et salpêtre broyés ensemble. Une charge par tir de "
                                                 "mousquet, avec une Balle de fer. Préparée par 4 à l'établi d'alchimie."},
        "Categories": ["Items.Weapons"], "Quality": "Common", "ItemLevel": 20,
        "Model": "%s/%s.blockymodel" % (MODEL_REL, POWDER), "Texture": "%s/%s.png" % (MODEL_REL, POWDER),
        "Icon": "Icons/ItemsGenerated/%s.png" % POWDER, "IconProperties": arrow["IconProperties"],
        "DroppedItemAnimation": arrow["DroppedItemAnimation"], "MaxStack": 100, "DropOnDeath": True,
        "Tags": {"Type": ["Weapon"], "Family": ["Munition"]},
        "Recipe": {"TimeSeconds": 2, "KnowledgeRequired": False,
                   "Input": [{"ItemId": "Ingredient_Charcoal", "Quantity": 2}, {"ItemId": "Ingredient_Salt", "Quantity": 1}],
                   "OutputQuantity": 4,
                   "BenchRequirement": [{"Type": "Crafting", "Id": "Alchemybench", "Categories": ["Alchemy_Bombs"]}]}})
    dump(os.path.join(d, BALL + ".json"), {
        "$Comment": "Munition des mousquets (1 par tir, avec 1 Poudre a canon). scripts/artefacts/gen_mousquets.py.",
        "TranslationProperties": {"Name": "Balle de fer",
                                  "Description": "Bourse de balles rondes de fer coulé, au calibre du mousquet. Une balle par tir, avec "
                                                 "une Poudre à canon. Coulées par 8 à l'établi d'armes."},
        "Categories": ["Items.Weapons"], "Quality": "Common", "ItemLevel": 20,
        "Model": "%s/%s.blockymodel" % (MODEL_REL, BALL), "Texture": "%s/%s.png" % (MODEL_REL, BALL),
        "Icon": "Icons/ItemsGenerated/%s.png" % BALL, "IconProperties": arrow["IconProperties"],
        "DroppedItemAnimation": arrow["DroppedItemAnimation"], "MaxStack": 100, "DropOnDeath": True,
        "Tags": {"Type": ["Weapon"], "Family": ["Munition"]},
        "Recipe": {"TimeSeconds": 2, "KnowledgeRequired": False,
                   "Input": [{"ItemId": "Ingredient_Bar_Iron", "Quantity": 1}],
                   "OutputQuantity": 8,
                   "BenchRequirement": [{"Type": "Crafting", "Id": "Weapon_Bench", "Categories": ["Weapon_Bow"]}]}})
    icons[SNIPER_AMMO] = ammo_model(SNIPER_AMMO, sniper_parts, (1, 1, 1), layout=((22, -10, 0.85), (-14, 14, 1.0)), margin=0.16)
    icons[SHELL] = ammo_model(SHELL, lambda: shell_parts(1.0), (1, 1, 1), margin=0.08)
    for iid, name, desc, lvl, inputs, out in (
            (SNIPER_AMMO, "Balle de sniper divine",
             "Longue cartouche de fusil de précision à ogive de minerai divin, pointe d'arcane violette. Munition du SARS militaire "
             "(forme de Black Bess) : chargeurs de 5. Forgée par 10 à la Forge Divine.", 320,
             [{"ItemId": "Ingredient_Bar_Divin", "Quantity": 1}, {"ItemId": "Ingredient_Bar_Iron", "Quantity": 2}], 10),
            (SHELL, "Obus Divin",
             "Obus de char à ogive d'or blanc, ceinture de cuivre et corps d'acier gravé de runes de glace. Munition du canon de char "
             "à main (forme de Blue Tiger) : un obus par coup. Forgé par 2 à la Forge Divine.", 340,
             [{"ItemId": "Ingredient_Bar_Divin", "Quantity": 2}, {"ItemId": "Ingredient_Bar_Iron", "Quantity": 4},
              {"ItemId": POWDER, "Quantity": 4}], 2)):
        dump(os.path.join(d, iid + ".json"), {
            "$Comment": "Munition d'arme transformee (Mousquet.java). Modele, icone et ce fichier : scripts/artefacts/gen_mousquets.py.",
            "TranslationProperties": {"Name": name, "Description": desc},
            "Categories": ["Items.Weapons"], "Quality": "Epic", "ItemLevel": lvl,
            "Model": "%s/%s.blockymodel" % (MODEL_REL, iid), "Texture": "%s/%s.png" % (MODEL_REL, iid),
            "Icon": "Icons/ItemsGenerated/%s.png" % iid, "IconProperties": arrow["IconProperties"],
            "MaxStack": 100 if iid == SNIPER_AMMO else 20, "DroppedItemAnimation": arrow["DroppedItemAnimation"],
            "Tags": {"Type": ["Weapon"], "Family": ["Munition"]},
            "Recipe": {"TimeSeconds": 3, "Input": inputs, "OutputQuantity": out,
                       "BenchRequirement": [{"Type": "Crafting", "Id": "Forge_Divine", "Categories": ["Forge_Arcs"]}]}})
    # projectiles en vol
    carrier("Projectile_Config_Balle_Mousquet", "Bullet_Blunderbuss")
    carrier("Projectile_Config_Balle_Sniper_Divine", "Balle_Divine")                       # traceur dore des balles divines
    projectile_model("Obus_Divin", shell_parts(1.6),
                     {"Color": "#9fd0ff", "Radius": 4},
                     {"TexturePath": "Trails/Gold.png", "LifeSpan": 6, "Roll": 0, "Start": {"Color": "rgba(#d8ecff, 0.9)", "Width": 0.25},
                      "End": {"Color": "rgba(#4080ff, 0)", "Width": 0}, "LightInfluence": 0, "Smooth": True, "RenderMode": "BlendLinear"})
    carrier("Projectile_Config_Obus_Divin", "Obus_Divin")
    return icons


# ---------------------------------------------------------------------------------------------------------------------
# Lunettes (surimpressions plein ecran de la visee)
# ---------------------------------------------------------------------------------------------------------------------
def scope_overlay(path, style):
    """1920 x 1080 : cercle de vision net, anneau de laiton ou de metal noir, ombre douce, vignettage, reticule.
    style "mousquet" : lunette de laiton ancienne, reticule fin a croisee et graduations ;
    style "sars" : lunette militaire, reticule mil-dot a poteaux epais, chiffres de distance, point d'arcane central."""
    W, H = 1920, 1080
    cx, cy, R = W / 2.0, H / 2.0, 470.0
    img = [[[0, 0, 0, 255] for _ in range(W)] for _ in range(H)]
    ring_out = R + 28
    brass, brass_dark = (176, 132, 62), (70, 48, 20)
    black, gray = (18, 18, 22), (60, 60, 68)
    for y in range(H):
        row = img[y]
        dy = y + 0.5 - cy
        for x in range(W):
            dx = x + 0.5 - cx
            r = math.sqrt(dx * dx + dy * dy)
            if r < R - 40:
                row[x] = [0, 0, 0, 0]
            elif r < R:                                                # bord de la lentille : ombre douce et frange
                t = (r - (R - 40)) / 40.0
                a = int(255 * (t ** 2.2) * 0.9)
                tint = (40, 30, 60) if style == "sars" else (60, 40, 16)
                row[x] = [tint[0], tint[1], tint[2], a]
            elif r < ring_out:                                         # anneau de la monture, eclaire en haut a gauche
                t = (r - R) / (ring_out - R)
                light = 0.5 + 0.5 * (-(dx + dy) / max(1.0, r)) * 0.7
                c1, c2 = (brass, brass_dark) if style == "mousquet" else (gray, black)
                k = math.sin(t * math.pi)
                c = mixc(c2, c1, max(0.0, min(1.0, k * light + 0.15)))
                row[x] = [int(c[0]), int(c[1]), int(c[2]), 255]
            else:
                v = min(1.0, (r - ring_out) / 600.0)
                row[x] = [int(8 * (1 - v)), int(8 * (1 - v)), int(10 * (1 - v)), 255]

    def px(x, y, c, a=255):
        if 0 <= x < W and 0 <= y < H and (x - cx) ** 2 + (y - cy) ** 2 < (R - 6) ** 2:
            img[y][x] = [c[0], c[1], c[2], a]

    def hline(x0, x1, y, th, c, a=255):
        for yy in range(int(y - th / 2), int(y + th / 2) + 1):
            for xx in range(int(x0), int(x1) + 1):
                px(xx, yy, c, a)

    def vline(y0, y1, x, th, c, a=255):
        for xx in range(int(x - th / 2), int(x + th / 2) + 1):
            for yy in range(int(y0), int(y1) + 1):
                px(xx, yy, c, a)

    def dot(x0, y0, r, c, a=255):
        for yy in range(int(y0 - r - 1), int(y0 + r + 2)):
            for xx in range(int(x0 - r - 1), int(x0 + r + 2)):
                if (xx + 0.5 - x0) ** 2 + (yy + 0.5 - y0) ** 2 <= r * r:
                    px(xx, yy, c, a)

    ink = (10, 10, 12)
    if style == "mousquet":
        hline(cx - R, cx - 14, cy, 2, ink)
        hline(cx + 14, cx + R, cy, 2, ink)
        vline(cy - R, cy - 14, cx, 2, ink)
        vline(cy + 14, cy + R, cx, 2, ink)
        dot(cx, cy, 3, (200, 40, 40))
        for i in range(1, 9):                                           # graduations de chute
            L = 22 if i % 2 == 0 else 12
            hline(cx - L, cx + L, cy + i * 34, 2, ink)
        for i in range(1, 6):
            vline(cy - 9, cy + 9, cx - i * 70, 2, ink)
            vline(cy - 9, cy + 9, cx + i * 70, 2, ink)
        for k in range(16):                                             # couronne de reperes d'or sur le pourtour
            a = k * math.pi / 8
            for t in range(10):
                rr = R - 18 - t
                dot(cx + math.cos(a) * rr, cy + math.sin(a) * rr, 1.6, (200, 160, 70))
    else:
        for s in (-1, 1):                                               # poteaux epais puis fil fin
            hline(cx + s * (R * 0.55), cx + s * R, cy, 10, ink) if s > 0 else hline(cx - R, cx - R * 0.55, cy, 10, ink)
            vline(cy + R * 0.55, cy + R, cx, 10, ink) if s > 0 else vline(cy - R, cy - R * 0.55, cx, 10, ink)
        hline(cx - R * 0.55, cx - 10, cy, 2, ink)
        hline(cx + 10, cx + R * 0.55, cy, 2, ink)
        vline(cy - R * 0.55, cy - 10, cx, 2, ink)
        vline(cy + 10, cy + R * 0.55, cx, 2, ink)
        for i in range(1, 9):                                           # mil-dots
            d = i * 28
            for sx, sy in ((d, 0), (-d, 0), (0, d), (0, -d)):
                if abs(sx) < R * 0.55 and abs(sy) < R * 0.55:
                    dot(cx + sx, cy + sy, 4, ink)
        for i in range(1, 5):                                           # chevrons de chute
            y0 = cy + 40 + i * 30
            hline(cx - 10 - i * 6, cx + 10 + i * 6, y0, 2, ink)
        dot(cx, cy, 3.5, ARCANE)
        dot(cx, cy, 1.5, (255, 255, 255))
        for k in range(24):
            a = k * math.pi / 12
            for t in range(14 if k % 3 == 0 else 7):
                rr = R - 14 - t
                dot(cx + math.cos(a) * rr, cy + math.sin(a) * rr, 1.5, (150, 100, 220))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    png.write(path, W, H, img)


# ---------------------------------------------------------------------------------------------------------------------
# Animation de baionnette (jeu d'animations « Mousquet » = « Rifle » vanilla + Baionnette)
# ---------------------------------------------------------------------------------------------------------------------
RIFLE_ANIMS = "Characters/Animations/Items/Dual_Handed/Rifle/"
MY_ANIMS = "Characters/Animations/Items/Dual_Handed/Mousquet/"


def _first(track):
    return copy.deepcopy(track[0]["delta"]) if track else None


UPPER = {"R-Arm", "R-Forearm", "R-Hand", "L-Arm", "L-Forearm", "L-Hand", "Chest", "Belly", "Head"}


def bayonet_anim(idle, fps):
    """Estoc a la baionnette depuis la pose de repos du fusil : on arme (recul du fusil et du buste, 6 images), on fend
    (bras et fusil projetes de 11 unites vers l'avant, buste penche, 12), on tient (20), on revient (34). Les deux bras
    se deplacent ensemble : le fusil reste dans les mains. FPS : bras seulement."""
    keys = [(0, 0.0, 0.0, 0.0), (6, -3.5, 1.0, -6.0), (12, 11.0, -1.5, 10.0), (20, 10.0, -1.2, 9.0), (34, 0.0, 0.0, 0.0)]
    out = {"formatVersion": 1, "duration": 36, "holdLastKeyframe": False, "nodeAnimations": {}}
    for name, tr in idle["nodeAnimations"].items():
        if name not in UPPER:                                           # jambes et vetements : la marche continue
            continue
        p0, q0 = _first(tr.get("position") or []), _first(tr.get("orientation") or [])
        node = {"position": [], "orientation": [], "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
        arm = name in ("R-Arm", "L-Arm")
        for t, fwd, up, lean in keys:
            if arm or p0 is not None:
                base = p0 or {"x": 0, "y": 0, "z": 0}
                d = dict(base)
                if arm:
                    d = {"x": base["x"], "y": base["y"] + up, "z": base["z"] + fwd}
                elif name == "Pelvis" and not fps:
                    d = {"x": base["x"], "y": base["y"] - abs(lean) * 0.12, "z": base["z"] + lean * 0.25}
                if arm or p0 is not None:
                    node["position"].append({"time": t, "delta": d, "interpolationType": "smooth"})
            if q0 is not None or (name in ("Chest", "Belly") and not fps):
                q = q0 or dict(G.ID_Q)
                if name in ("Chest", "Belly") and not fps:
                    q = K.qmul(q, G.qx(lean * (0.6 if name == "Chest" else 0.4)))
                node["orientation"].append({"time": t, "delta": {k: round(q[k], 6) for k in "xyzw"}, "interpolationType": "smooth"})
        out["nodeAnimations"][name] = node
    return out


def animations():
    rifle = G.zjson("Server/Item/Animations/Rifle.json")
    idle = G.zjson("Common/" + RIFLE_ANIMS + "Idle.blockyanim")
    idle_fps = G.zjson("Common/" + RIFLE_ANIMS + "Idle_FPS.blockyanim")
    d = os.path.join(RES, "Common", MY_ANIMS)
    os.makedirs(d, exist_ok=True)
    json.dump(bayonet_anim(idle, False), open(os.path.join(d, "Baionnette.blockyanim"), "w"), indent=1)
    json.dump(bayonet_anim(idle_fps, True), open(os.path.join(d, "Baionnette_FPS.blockyanim"), "w"), indent=1)
    s = copy.deepcopy(rifle)
    s["$Comment"] = ("Armes a feu : jeu « Rifle » vanilla + Baionnette (estoc) + Rechargement, RechargementRapide, RechargementEclair, "
                    "RechargementChargeur (mousquet_anims.py). Genere par scripts/artefacts/gen_mousquets.py.")
    import mousquet_anims as MA                                          # rechargements (cinematique inverse)
    for name in MA.write_all(RES, os.path.join(RES, "Common", MODEL_REL, "Weapon_Mousquet.blockymodel")):
        s["Animations"][name] = {"ThirdPerson": MY_ANIMS + name + ".blockyanim", "ThirdPersonMoving": MY_ANIMS + name + ".blockyanim",
                                 "FirstPerson": MY_ANIMS + name + "_FPS.blockyanim",
                                 "Looping": False, "Speed": 1.0, "BlendingDuration": 0.12}
    s["Animations"]["Baionnette"] = {"ThirdPerson": MY_ANIMS + "Baionnette.blockyanim", "ThirdPersonMoving": MY_ANIMS + "Baionnette.blockyanim",
                                     "FirstPerson": MY_ANIMS + "Baionnette_FPS.blockyanim",
                                     "Looping": False, "Speed": 1.0, "BlendingDuration": 0.08,
                                     "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim"}
    dump(os.path.join(RES, "Server", "Item", "Animations", ANIM_SET + ".json"), s)


# ---------------------------------------------------------------------------------------------------------------------
# Interactions
# ---------------------------------------------------------------------------------------------------------------------
POWDER, BALL = "Munition_Poudre_Canon", "Munition_Balle_Fer"
SNIPER_AMMO, SHELL = "Munition_Balle_Sniper_Divine", "Munition_Obus_Divin"
BAYONET_DAMAGE = 30
OVERLAY_MUSKET, OVERLAY_SARS = "ScreenEffects/Eldanior_Lunette_Mousquet.png", "ScreenEffects/Eldanior_Lunette_SARS.png"
# paliers de la lunette : (endurance requise, grossissement) ; Black Bess compte son endurance x1,5
MUSKET_ZOOM = [(3000, 12.0), (2100, 9.5), (1350, 7.0), (750, 5.0), (300, 3.5), (0, 2.5)]
SARS_ZOOM = [(3000, 20.0), (2250, 16.0), (1500, 12.0), (750, 9.0), (0, 6.0)]
BLOCKED = ["Ability1", "Ability2", "Ability3", "Use", "Pick", "SwapFrom", "SwapTo"]


def dump(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def zoom(mag, overlay, sens):
    return {"Type": "Charging", "AllowIndefiniteHold": True, "DisplayProgress": False, "CancelOnOtherClick": False,
            "HorizontalSpeedMultiplier": 0.45,
            "Effects": {"MovementEffects": {"DisableSprint": True}, "HideFirstPersonHeldItem": True,
                        "Zoom": {"MagnificationMultiplier": mag, "MouseSensitivityMultiplier": sens, "OverlayTexture": overlay,
                                 "DepthOfField": False, "ForcePerspective": "FirstPerson", "LodMultiplier": min(6, 1 + mag / 3),
                                 "InLerp": 0.1, "OutLerp": 0.15, "OverlayFade": True, "OverlayFadePerSecond": 12}}}


def tiers(levels, overlay):
    """Chaine de paliers : Eldanior_SeuilEndurance du plus haut au plus bas, chaque palier = sa visee."""
    chain = zoom(levels[-1][1], overlay, round(0.6 / levels[-1][1] ** 0.5, 3))
    for need, mag in reversed(levels[:-1]):
        chain = {"Type": "Eldanior_SeuilEndurance", "Endurance": need,
                 "Next": zoom(mag, overlay, round(0.6 / mag ** 0.5, 3)), "Failed": chain}
    return chain


def interactions():
    d_int = os.path.join(RES, "Server", "Item", "Interactions", "Weapons", "Mousquet")
    d_root = os.path.join(RES, "Server", "Item", "RootInteractions", "Weapons", "Mousquet")
    shoot = {"ItemAnimationId": "ShootFlintlock", "CameraEffect": "Handgun_Shoot",
             "WorldSoundEventId": "SFX_Blunderbuss_Fire", "LocalSoundEventId": "SFX_Blunderbuss_Fire_Local"}
    dump(os.path.join(d_int, "Mousquet_Tir.json"), {
        "$Comment": "Detente : le serveur verifie l'arme tenue et ses munitions (Mousquet.pullTrigger : mousquets, SARS, canon), "
                    "la balle part au tick suivant. Sinon cliquetis a vide.",
        "Type": "Eldanior_MousquetTir",
        "Next": {"Type": "Simple", "RunTime": 0.5, "Effects": shoot},
        "Failed": "Gun_Shoot_Fail"})
    dump(os.path.join(d_int, "Mousquet_Secondaire.json"), {
        "$Comment": "Clic droit : relache avant 0,2 s = coup de baionnette ; maintenu = visee (aucune barre de charge).",
        "Type": "Charging", "AllowIndefiniteHold": False, "DisplayProgress": False, "CancelOnOtherClick": False,
        "Next": {"0": "Mousquet_Baionnette", "0.2": "Mousquet_Visee"}})
    dump(os.path.join(d_int, "Mousquet_Visee.json"), {
        "$Comment": "Visee a la lunette : grossissement selon l'endurance (paliers %s), entites lointaines envoyees au tireur "
                    "(sort MousquetVisee). Relacher le clic droit finit la visee ; on tire sans la perdre." % [m for _, m in MUSKET_ZOOM],
        "Type": "Serial",
        "Interactions": [{"Type": "RuneSpell", "RunTime": 0.0, "Spell": "MousquetVisee"}, tiers(MUSKET_ZOOM, OVERLAY_MUSKET)]})
    damage = {"Type": "DamageEntity", "DamageCalculator": {"BaseDamage": {"Physical": BAYONET_DAMAGE}},
              "EntityStatsOnHit": [{"EntityStatId": "SignatureEnergy", "Amount": 3}],
              "DamageEffects": {"Knockback": {"Force": 1, "RelativeX": 0, "RelativeZ": -5, "VelocityY": 3},
                                "WorldSoundEventId": "SFX_Spear_Impact", "WorldParticles": [{"SystemId": "Impact_Blade_01"}]}}
    keep = {"OnItemChangeBehavior": "Ignore"}
    dump(os.path.join(d_int, "Mousquet_Baionnette.json"), {
        "$Comment": "Coup de baionnette : la baionnette est fixee (sort MousquetBaionnette : objet echange contre sa variante "
                    "_Baionnette pendant 0,65 s), estoc dedie (animation Baionnette du jeu Mousquet), selecteur Stab, 4 blocs.",
        "Type": "Serial",
        "Interactions": [
            {"Type": "RuneSpell", "RunTime": 0.0, "Spell": "MousquetBaionnette"},
            dict({"Type": "Simple", "RunTime": 0.18, "HorizontalSpeedMultiplier": 0.6,
                  "Effects": {"ItemAnimationId": "Baionnette", "WorldSoundEventId": "SFX_Spear_Lunge",
                              "LocalSoundEventId": "SFX_Spear_Lunge_Local"}}, **keep),
            dict({"Type": "Selector", "RunTime": 0.1,
                  "Selector": {"Id": "Stab", "TestLineOfSight": True, "ExtendTop": 0.3, "ExtendLeft": 0.3, "ExtendRight": 0.3,
                               "ExtendBottom": 0.3, "StartDistance": 0.1, "EndDistance": 4.0},
                  "HitEntity": {"Interactions": [damage]}}, **keep),
            dict({"Type": "Simple", "RunTime": 0.32}, **keep)]})
    dump(os.path.join(d_int, "Mousquet_Concentration.json"), {
        "$Comment": "Signature des mousquets, Concentration (energie pleine) : on epaule et retient son souffle, puis une balle a "
                    "precision et degats doubles part (Eldanior_MousquetTir Concentration ; munitions requises, pas le rechargement).",
        "Type": "StatsCondition", "Costs": {"SignatureEnergy": 100}, "ValueType": "Percent",
        "Next": {"Type": "Serial", "Interactions": [
            {"Type": "Simple", "RunTime": 0.6, "HorizontalSpeedMultiplier": 0.3,
             "Effects": {"ItemAnimationId": "ShootFlintlockCharging", "LocalSoundEventId": "SFX_Sword_T2_Lunge_Charge_Local"}},
            {"Type": "Eldanior_MousquetTir", "Concentration": True,
             "Next": {"Type": "Serial", "Interactions": [
                 {"Type": "ChangeStat", "StatModifiers": {"SignatureEnergy": -100}, "ValueType": "Percent"},
                 {"Type": "Simple", "RunTime": 0.6, "Effects": dict(shoot, ItemAnimationId="ShootFlintlockCharged")}]},
             "Failed": "Gun_Shoot_Fail"}]}})
    # ----- SARS et canon -----
    dump(os.path.join(d_int, "Sars_Tir.json"), {
        "$Comment": "SARS : tir (chargeur de 5 Balles de sniper divines, verrou 0,9 s, rechargement 2,5 s), Mousquet.sarsTrigger.",
        "Type": "Eldanior_MousquetTir",
        "Next": {"Type": "Simple", "RunTime": 0.35, "Effects": {"ItemAnimationId": "Shoot", "CameraEffect": "Handgun_Shoot",
                                                              "WorldSoundEventId": "SFX_Rifle_Fire", "LocalSoundEventId": "SFX_Rifle_Fire_Local"}},
        "Failed": "Gun_Shoot_Fail"})
    dump(os.path.join(d_int, "Sars_Visee.json"), {
        "$Comment": "SARS : lunette de combat, grossissement selon l'endurance (paliers %s)." % [m for _, m in SARS_ZOOM],
        "Type": "Serial",
        "Interactions": [{"Type": "RuneSpell", "RunTime": 0.0, "Spell": "MousquetVisee"}, tiers(SARS_ZOOM, OVERLAY_SARS)]})
    dump(os.path.join(d_int, "Canon_Tir.json"), {
        "$Comment": "Canon de char : un Obus Divin par coup, 5 s de rechargement (Mousquet.canonTrigger, sort CanonObus).",
        "Type": "Eldanior_MousquetTir",
        "Next": {"Type": "Simple", "RunTime": 0.8, "Effects": {"ItemAnimationId": "ShootFlintlockCharged", "CameraEffect": "Handgun_Shoot",
                                                             "WorldSoundEventId": "SFX_Weapon_Goblin_Cannon_Fire",
                                                             "LocalSoundEventId": "SFX_Weapon_Goblin_Cannon_Fire"}},
        "Failed": "Gun_Shoot_Fail"})
    dump(os.path.join(d_int, "Canon_Visee.json"), {
        "$Comment": "Canon de char : viseur periscopique (x2,5).",
        "Type": "Serial",
        "Interactions": [{"Type": "RuneSpell", "RunTime": 0.0, "Spell": "MousquetVisee"}, zoom(2.5, OVERLAY_MUSKET, 0.5)]})
    for name, inter, rules in (("Root_Mousquet_Secondaire", "Mousquet_Secondaire", ["Secondary"] + BLOCKED),
                               ("Root_Sars_Visee", "Sars_Visee", ["Secondary"] + BLOCKED),
                               ("Root_Canon_Visee", "Canon_Visee", ["Secondary"] + BLOCKED)):
        dump(os.path.join(d_root, name + ".json"), {"Rules": {"BlockedBy": rules}, "Interactions": [inter]})
    dump(os.path.join(d_root, "Root_Mousquet_Concentration.json"), {"RequireNewClick": True, "Interactions": ["Mousquet_Concentration"]})


# ---------------------------------------------------------------------------------------------------------------------
# Objets
# ---------------------------------------------------------------------------------------------------------------------
def primary(inter):
    return {"$Comment": "BlockedBy sans Secondary : on tire en visant.",
            "Rules": {"BlockedBy": ["Primary"] + BLOCKED}, "Interactions": [inter]}


def write_item(spec, variant=False):
    raw = G.zjson(VANILLA_ITEM)
    item = {k: v for k, v in raw.items() if k not in ("TranslationProperties", "Recipe", "InteractionVars", "Interactions")}
    iid = spec["id"] + (BAYONET if variant else "")
    item["TranslationProperties"] = {"Name": spec["name"], "Description": spec["desc"]}
    item.update({
        "$Comment": spec["comment"] + (" Variante a baionnette fixee (echangee le temps d'un estoc, ArmeTransfo)." if variant else "")
                    + " Genere par scripts/artefacts/gen_mousquets.py.",
        "Quality": spec["quality"], "ItemLevel": spec["level"],
        "Model": "%s/%s.blockymodel" % (MODEL_REL, iid), "Texture": "%s/%s.png" % (MODEL_REL, iid),
        "Icon": "Icons/ItemsGenerated/%s.png" % spec["id"],
        "IconProperties": {"Scale": 0.3, "Translation": [-12, -22], "Rotation": [320, 90, 0]},
        "Categories": [] if variant or spec.get("hidden") else (["Eldanior_System.Artefacts"] if spec["artefact"] else []) + ["Items.Weapons"],
        "MaxStack": 1,
        "PlayerAnimationsId": spec.get("anims", ANIM_SET),
        "Interactions": spec["interactions"],
        # pas d'EntityStatsToClear : l'echange baionnette viderait l'energie ; ArmeTransfo la remet a zero lui-meme
        "Weapon": {"StatModifiers": {"SignatureEnergy": [{"Amount": 20, "CalculationType": "Additive"}]}},
        "Tags": {"Type": ["Weapon"], "Family": ["Gun"]},
    })
    if spec.get("no_signature") or spec.get("ability"):                # capacite a recharge (legendaires), armes transformees
        item["Weapon"] = {}
    if spec["artefact"]:
        item.update({"MaxDurability": 0, "DurabilityLossOnHit": 0})
    path = os.path.join(RES, "Server", "Item", "Items", "Weapon", "Gun", iid + ".json")
    dump(path, item)


MUSKET_INTER = {"Primary": primary("Mousquet_Tir"), "Secondary": "Root_Mousquet_Secondaire"}
STATS_TEXT = ("Réservé aux Mousquetaires. Chaque tir demande 1 Poudre à canon et 1 Balle de fer. Force : dégâts (70 à 2 000) et vitesse de la balle ; "
              "Agilité : rechargement (50 s, 1 s à 3 000 points) ; Endurance : portée sûre (10 à 500 blocs, 500 au plus), précision au-delà, "
              "grossissement de la lunette et entités vues de loin. Une balle qui tue annonce la distance du tir. "
              "Clic droit maintenu : viser à la lunette. Clic droit bref : coup de baïonnette (fixée le temps de l'estoc).")

WEAPONS = {
    "Mousquet": {"id": "Weapon_Mousquet", "build": build_basic, "quality": "Uncommon", "level": 40, "artefact": False,
                 "name": "Mousquet", "bayonet": True,
                 "interactions": dict(MUSKET_INTER, Ability1="Root_Mousquet_Concentration"),
                 "comment": "Mousquet facon Brown Bess (v2, 2026-11-15) : tir Java (Mousquet, MousquetTirInteraction), visee et baionnette JSON.",
                 "desc": "Long mousquet à silex de l'infanterie : crosse de noyer, canon poli, garnitures de laiton. " + STATS_TEXT +
                         " Signature, Concentration : une balle à précision et dégâts doublés."},
    "BlackBess": {"id": "Artefact_Mousquet_BlackBess", "build": build_black_bess, "quality": "Legendary", "level": 560, "artefact": True,
                  "ability": True, "name": "Black Bess, l'Arcane Sniper", "bayonet": True,
                  "interactions": dict(MUSKET_INTER),
                  "comment": "Mousquet legendaire de precision (2026-11-15) : Mousquet.Arme.BLACK_BESS ; signature = SARS 5 min (ArmeTransfo).",
                  "desc": "Mousquet légendaire de précision, noir d'ébène, au long canon gravé de runes d'arcane et coiffé d'une lunette "
                          "aux lentilles violettes. Portée et précision ×1,5, lunette plus puissante, dégâts +15 %. " + STATS_TEXT +
                          " Rune de Transformation d'Arme (Mousquetaire) : Black Bess devient pendant 5 minutes un SARS militaire, fusil de précision à "
                          "chargeur de 5 Balles de sniper divines, portée 2 000 blocs ; puis elle redevient mousquet (la rune se recharge)."},
    "GreenColt": {"id": "Artefact_Mousquet_GreenColt", "build": build_green_colt, "quality": "Legendary", "level": 540, "artefact": True,
                  "ability": True, "name": "Green Colt, le Rapide", "bayonet": True,
                  "interactions": dict(MUSKET_INTER),
                  "comment": "Mousquet legendaire de rapidite (2026-11-15) : Mousquet.Arme.GREEN_COLT ; signature = Colts akimbo 5 min (ArmeTransfo).",
                  "desc": "Mousquet légendaire de rapidité, carabine courte laquée de vert et d'or, à ailettes de vitesse et cartouchière "
                          "sur la crosse. Rechargement deux fois plus rapide, dégâts +10 %. " + STATS_TEXT +
                          " Rune de Transformation d'Arme (Mousquetaire) : Green Colt devient pendant 5 minutes une paire de Colts de cavalerie (5 Balles "
                          "Divines par pistolet), qui tirent et rechargent à une vitesse folle, portée 40 blocs ; puis il redevient "
                          "mousquet (la rune se recharge)."},
    "BlueTiger": {"id": "Artefact_Mousquet_BlueTiger", "build": build_blue_tiger, "quality": "Legendary", "level": 560, "artefact": True,
                  "ability": True, "name": "Blue Tiger, le Perceur", "bayonet": True,
                  "interactions": dict(MUSKET_INTER),
                  "comment": "Mousquet legendaire de force (2026-11-15) : Mousquet.Arme.BLUE_TIGER ; signature = canon de char 5 min (ArmeTransfo).",
                  "desc": "Mousquet légendaire de force, crosse bleu nuit rayée comme un tigre, canon épais fretté à frein de bouche et "
                          "pointe perforante. Dégâts ×1,5, la balle traverse deux ennemis. " + STATS_TEXT +
                          " Rune de Transformation d'Arme (Mousquetaire) : Blue Tiger devient pendant 5 minutes un canon de char à main qui tire des Obus "
                          "Divins en ligne droite (un par chargement, 5 s, 100 blocs, dégâts ×5, traverse les ennemis et explose) ; "
                          "puis il redevient mousquet (la rune se recharge)."},
    "SARS": {"id": "Artefact_Sniper_SARS", "build": build_sars, "quality": "Legendary", "level": 560, "artefact": True, "hidden": True,
             "name": "SARS militaire (Black Bess)", "no_signature": True,
             "interactions": {"Primary": primary("Sars_Tir"), "Secondary": "Root_Sars_Visee"},
             "comment": "Forme transformee de Black Bess (5 min, ArmeTransfo) : Mousquet.sarsTrigger, sort SarsBalle.",
             "desc": "Forme transformée de Black Bess, pour 5 minutes : fusil de précision militaire à verrou. Chargeurs de 5 Balles de "
                     "sniper divines (2,5 s), portée 2 000 blocs, précision parfaite, dégâts ×1,5 de la balle de mousquet. "
                     "Clic droit maintenu : lunette de combat (jusqu'à ×20 avec l'Endurance)."},
    "Canon": {"id": "Artefact_Canon_Main", "build": build_canon, "quality": "Legendary", "level": 560, "artefact": True, "hidden": True,
              "name": "Canon de char à main (Blue Tiger)", "no_signature": True,
              "interactions": {"Primary": primary("Canon_Tir"), "Secondary": "Root_Canon_Visee"},
              "comment": "Forme transformee de Blue Tiger (5 min, ArmeTransfo) : Mousquet.canonTrigger, sort CanonObus.",
              "desc": "Forme transformée de Blue Tiger, pour 5 minutes : canon de char porté à deux mains. Un Obus Divin par coup, "
                      "5 s de rechargement, 100 blocs en ligne droite : l'obus traverse les ennemis (dégâts ×5 de la balle de mousquet) "
                      "puis explose et creuse le terrain (restauré après une minute)."},
}


# ---------------------------------------------------------------------------------------------------------------------
# Utilitaires d'images
# ---------------------------------------------------------------------------------------------------------------------
def recolor(img, dark, light):
    out = []
    for row in img:
        r2 = []
        for c in row:
            if c[3] == 0:
                r2.append([0, 0, 0, 0])
                continue
            t = (0.3 * c[0] + 0.59 * c[1] + 0.11 * c[2]) / 255.0
            t = max(0.0, min(1.0, (t - 0.5) * 1.25 + 0.5))
            r2.append(list(mixc(dark, light, t)) + [c[3]])
        out.append(r2)
    return out


def vanilla_png(path):
    import tempfile
    tmp = tempfile.NamedTemporaryFile(suffix=".png", delete=False)
    tmp.write(G.Z.read(path))
    tmp.close()
    try:
        return png.read(tmp.name)
    finally:
        os.unlink(tmp.name)


def ball_icon():
    S = 256
    big = [[[0, 0, 0, 0] for _ in range(S)] for _ in range(S)]
    base, hi, lo = (104, 106, 114), (226, 230, 238), (34, 34, 40)
    for cx, cy, r in ((132, 92, 50), (170, 160, 48), (96, 150, 52)):
        for y in range(S):
            for x in range(S):
                dx, dy = (x + 0.5 - cx) / r, (y + 0.5 - cy) / r
                d2 = dx * dx + dy * dy
                if d2 > 1:
                    continue
                nz = math.sqrt(1 - d2)
                light = max(0.0, -0.45 * dx - 0.55 * dy + 0.7 * nz)
                c = mixc(lo, base, min(1.0, 0.35 + light))
                spec = max(0.0, light - 0.82) / 0.18
                c = mixc(c, hi, min(1.0, spec))
                big[y][x] = list(c) + [255]
    return G.outline(K.downscale(big, 4))


def render(handle, tex):
    """Icone : l'arme en diagonale, bouche en haut a droite ; apercu : vue de profil (platine) et de trois quarts."""
    h2 = handle["children"][0]
    h3 = h2["children"][0]
    qm = K.qmul(h2["orientation"], h3["orientation"])
    inv = {"x": -qm["x"], "y": -qm["y"], "z": -qm["z"], "w": qm["w"]}
    profile = K.qmul(G.qy(-90), inv)                                       # +z de la platine vers +x de l'ecran
    diag = K.qmul(G.qz(35), profile)
    lifted = [[[min(255, c[0] * 1.15 + 14), min(255, c[1] * 1.15 + 14), min(255, c[2] * 1.15 + 16), c[3]] for c in row] for row in tex]
    big = K.draw(G.handle_quads(handle, lifted, diag), 256, 0, 0, margin=0.03)
    icon = G.outline(K.downscale(big, 4))
    side = K.draw(G.handle_quads(handle, tex, profile), 768, 0, 0, margin=0.03)
    three = K.draw(G.handle_quads(handle, tex, profile), 768, 30, 18, margin=0.03)
    return icon, side, three


def build_model(model_id, build, *args):
    G.M = K.Model(model_id)
    G.M.reuse = True
    root, handle = build(*args)
    G.renumber(root)
    G.fix_coplanar(handle, model_id)
    W, H, tex = G.M.texture()
    png.write(os.path.join(RES, "Common", MODEL_REL, model_id + ".png"), W, H, tex)
    json.dump({"nodes": [root], "lod": "auto"}, open(os.path.join(RES, "Common", MODEL_REL, model_id + ".blockymodel"), "w"), indent=1)
    return handle, tex, (W, H)


def remove_old():
    """Anciens legendaires (Tonnerre d'Ebene, Gueule du Dragon, Vigie des Etoiles) : objets, modeles, textures, icones."""
    for iid in OLD:
        for path in (os.path.join(RES, "Server", "Item", "Items", "Weapon", "Gun", iid + ".json"),
                     os.path.join(RES, "Common", MODEL_REL, iid + ".blockymodel"), os.path.join(RES, "Common", MODEL_REL, iid + ".png"),
                     os.path.join(RES, "Common", "Icons", "ItemsGenerated", iid + ".png")):
            if os.path.exists(path):
                os.remove(path)


def main():
    # armes longues : 2 px par unite et atlas de 1024
    K.set_density(2, 1024)
    os.makedirs(os.path.join(RES, "Common", MODEL_REL), exist_ok=True)
    remove_old()
    sheet = []
    for key, spec in WEAPONS.items():
        if not want(key):
            continue
        handle, tex, (W, H) = build_model(spec["id"], spec["build"])
        icon, side, three = render(handle, tex)
        png.write(os.path.join(RES, "Common", "Icons", "ItemsGenerated", spec["id"] + ".png"), 64, 64, icon)
        write_item(spec)
        if spec.get("bayonet"):
            build_model(spec["id"] + BAYONET, spec["build"], True)
            write_item(spec, variant=True)
        sheet.append((icon, side, three))
        print("%-9s %-30s texture %dx%d" % (key, spec["id"], W, H))
    if want("Munitions"):
        K.set_density(4, 512)
        icons = munitions()
        K.set_density(2, 1024)
        print("munitions : %s" % ", ".join(icons))
    if want("Mousquet") or want("Interactions"):
        interactions()
    if want("Anims"):
        animations()
        print("animations : %s (Baionnette)" % ANIM_SET)
    if want("Lunettes"):
        scope_overlay(os.path.join(RES, "Common", OVERLAY_MUSKET), "mousquet")
        scope_overlay(os.path.join(RES, "Common", OVERLAY_SARS), "sars")
        print("lunettes : ok")
    if PREVIEW and sheet:
        os.makedirs(PREVIEW, exist_ok=True)
        S = 768
        img = [[[44, 44, 52, 255] for _ in range(2 * S + 256)] for _ in range(len(sheet) * 384)]
        for i, (icon, side, three) in enumerate(sheet):
            oy = i * 384
            for ox, pic in ((0, side), (S, three)):
                for y in range(384):
                    for x in range(S):
                        p = pic[y + 192][x]
                        if p[3] > 0:
                            img[oy + y][ox + x] = list(p[:3]) + [255]
            for y in range(256):
                for x in range(256):
                    p = icon[y // 4][x // 4]
                    if p[3] > 0:
                        img[oy + 64 + y][2 * S + x] = list(p[:3]) + [255]
        png.write(os.path.join(PREVIEW, "armes_feu_planche.png"), 2 * S + 256, len(sheet) * 384, img)
    print("ok")


if __name__ == "__main__":
    main()
