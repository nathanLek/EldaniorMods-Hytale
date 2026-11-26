"""Animations de la Percee des katanas Luo (refaites le 2026-11-12, « ameliore l'animation de mouvement du skin lors de
l'attaque et du chargement »).

Le corps n'est plus repris de l'estoc vanilla avec un bassin decale : chaque image est composee (stance) — bassin, torsion du
buste (l'epaule droite recule a la charge, part en avant a l'estoc), inclinaison, tete qui garde la cible, et les deux jambes
placees par cinematique inverse sur des chevilles posees au sol (vraie fente : pied droit loin devant, jambe arriere tendue).
La lame est placee dans le repere du modele (devant = +Z, droite = -X, haut = +Y ; sol a y = 0, cheville a y = 7), donc
l'estoc part droit vers la cible quelle que soit la torsion du buste. Le bras droit suit par cinematique inverse
(tenryumaru_anims.ik), les images intermediaires par tenryumaru_anims.densify.

  TsukiPosture  maintien de l'attaque du Bokken : le corps s'enroule (lame retiree a la hanche, pointe vers la cible, main
                gauche tendue comme une mire), s'abaisse en chargeant, tremble un peu ; tenue jusqu'au relache
  Tsuki         estoc : anticipation, fente eclair (image 8), depassement, tenue, retour en garde (paliers 1, 4, 6)
  TsukiDouble   Tsuki du Ruisseau (palier 2) : deux estocs enchaines, le second plus profond (images 6 et 16)
  TsukiRuee     Estoc Fendant (palier 3) : fente rasante, buste couche, lame tenue pendant la ruee (image 5)
  TsukiRafale   Mille Aiguilles (palier 5) : sept estocs sur un pied avant qui pousse (images 3 + 5k), estoc final image 40
  TsukiCinq     Cinq Lances Celestes (palier 8) : cinq estocs eclair dans le vide (images 4 a 28), puis le bras designe la
                cible pendant que les lances partent une a une (jusqu'a l'image 186, 3,1 s)
  TsukiCharge   Percee Celeste, charge (palier 7, en boucle) : garde gatotsu, lame a hauteur d'oeil tiree en arriere, main
                gauche tendue vers la cible, posture de cavalier, tremblement
  TsukiCeleste  Percee Celeste, tir : depuis le gatotsu, fente maximale tenue pendant le faisceau

Les instants de frappe sont repris par Percee.java (STRIKE_MS etc.) : les changer ensemble.
"""
import math

import pose_kit as P
import tenryumaru_anims as A

K = P.K
Z3 = (0.0, 0.0, 0.0)
ANKLE_Y = 7.0
REST_ANKLE = {"R": (-7.3, ANKLE_Y, -3.0), "L": (7.3, ANKLE_Y, -3.0)}
EDGE_OUT = (-1.0, 0.0, 0.0)                 # hira-zuki : lame a plat, tranchant vers la droite


def _norm(v):
    n = math.sqrt(sum(c * c for c in v)) or 1.0
    return tuple(c / n for c in v)


# ---------------------------------------------------------------------------------------------------------------------
# Jambes : cinematique inverse (tangage + roulis de la cuisse, flexion du genou), pied a plat
# ---------------------------------------------------------------------------------------------------------------------
def leg_ik(body, side, ankle):
    names = (side + "-Thigh", side + "-Calf", side + "-Foot")

    def pose(v):
        a, r, b = v
        p = dict(body)
        p[names[0]] = (Z3, P.euler(a, 0, r))
        p[names[1]] = (Z3, P.euler(b, 0, 0))
        p[names[2]] = (Z3, P.euler(-(a + b), 0, -r))
        return p

    def cost(v):
        w = P.solve(pose(v))
        e = sum((w[names[2]][0][i] - ankle[i]) ** 2 for i in range(3))
        a, r, b = v
        return e + 0.02 * r * r + 4 * (max(0, -b) ** 2 + max(0, b - 140) ** 2 + max(0, abs(r) - 30) ** 2)

    x = [-10.0, 0.0, 20.0]
    best = cost(x)
    for step in (16, 8, 4, 2, 1, 0.5, 0.25):
        improved = True
        while improved:
            improved = False
            for i in range(3):
                for d in (step, -step):
                    y = list(x)
                    y[i] += d
                    c = cost(y)
                    if c < best - 1e-6:
                        x, best, improved = y, c, True
    p = pose(x)
    w = P.solve(p)
    err = math.sqrt(sum((w[names[2]][0][i] - ankle[i]) ** 2 for i in range(3)))
    return {n: p[n] for n in names}, err


def stance(pelvis=(0.0, 0.0, 0.0), yaw=0.0, twist=0.0, lean=0.0, rfoot=None, lfoot=None, head=(0.0, 0.0)):
    """Corps complet (hors bras). pelvis : decalage du bassin (repere du modele) ; yaw : rotation du bassin (> 0 : la hanche
    droite avance) ; twist : torsion du buste en plus ; lean : buste penche en avant (> 0) ; rfoot / lfoot : chevilles au sol
    (x, z) ; head : (tangage, lacet) en plus de la compensation qui garde le regard vers l'avant."""
    body = {"Pelvis": (tuple(pelvis), P.euler(0, yaw, 0)),
            "Belly": (Z3, P.euler(lean * 0.55, twist * 0.5, 0)),
            "Chest": (Z3, P.euler(lean * 0.45, twist * 0.5, 0)),
            "Head": (Z3, P.euler(-lean * 0.8 + head[0], -(yaw + twist) + head[1], 0))}
    errs = []
    for side, foot in (("R", rfoot), ("L", lfoot)):
        rest = REST_ANKLE[side]
        target = (foot[0], ANKLE_Y, foot[1]) if foot else rest
        legs, e = leg_ik(body, side, target)
        body.update(legs)
        errs.append(e)
    return body, max(errs)


# ---------------------------------------------------------------------------------------------------------------------
# Bras
# ---------------------------------------------------------------------------------------------------------------------
def blade_target(origin, blade_dir, edge_dir=EDGE_OUT):
    """Cible de R-Attachment : poignee en origin (repere du modele), lame vers blade_dir, tranchant vers edge_dir."""
    bd = _norm(blade_dir)
    ed = _norm(edge_dir)
    ed = _norm(tuple(ed[i] - bd[i] * sum(ed[j] * bd[j] for j in range(3)) for i in range(3)))
    h_rot = P.look_rot(bd, tuple(-c for c in ed))
    return A.attachment_for_handle(origin, h_rot)


def left_reach(body, point):
    """Bras gauche tendu vers point (repere du modele) : la main vise la cible, le fourreau suit."""
    arm, err, _ = A.ik("L", body, point, None, None)
    return arm, err


def frame(body, left, origin, blade_dir, init, edge=EDGE_OUT):
    """Pose complete : corps, bras gauche (dict de deltas ou point a atteindre), lame en origin."""
    errs = []
    if isinstance(left, tuple):                           # point a atteindre par la main gauche
        left, el = left_reach(body, left)
        errs.append(el)
    pose, er, init = A.solve_frame(body, left, blade_target(origin, blade_dir, edge), init)
    errs.append(er)
    return pose, max(errs), init


def guard_frame(vanilla, init):
    """Garde basse de fin de coup (celle du combo Luo), corps au repos."""
    return A.swing_frame(A.body_at(vanilla("Idle.blockyanim"), 0), *A.GUARD, A.HOLD, init)


def off(p, base):
    return (p[0] + base[0], p[1] + base[1], p[2] + base[2])


# Postures (decalage du bassin, lacet du bassin, torsion, inclinaison, cheville droite, cheville gauche)
COIL = dict(pelvis=(0.0, -4.0, -2.0), yaw=-12, twist=-22, lean=-2, rfoot=(-6.5, 7.0), lfoot=(8.5, -12.0))
COIL_DEEP = dict(pelvis=(0.0, -6.5, -3.5), yaw=-16, twist=-28, lean=-1, rfoot=(-6.5, 8.0), lfoot=(9.0, -14.0))
STRIKE = dict(pelvis=(0.0, -7.0, 12.0), yaw=6, twist=22, lean=10, rfoot=(-7.5, 27.0), lfoot=(8.5, -12.0))
STRIKE_DEEP = dict(pelvis=(0.0, -9.0, 15.0), yaw=8, twist=26, lean=13, rfoot=(-8.0, 31.0), lfoot=(9.0, -13.0))
RUEE = dict(pelvis=(0.0, -14.0, 16.0), yaw=8, twist=24, lean=30, rfoot=(-8.0, 34.0), lfoot=(9.5, -13.0))
GATOTSU = dict(pelvis=(0.0, -9.0, -4.0), yaw=-20, twist=-30, lean=2, rfoot=(-9.0, 9.0), lfoot=(11.0, -15.0))

CHAMBER = ((-13.0, 63.0, -3.0), (0.03, 0.14, 1.0))         # lame retiree contre la hanche droite, pointe vers la cible
CHAMBER_DEEP = ((-14.0, 62.0, -7.0), (0.04, 0.16, 1.0))
AIM = (4.0, 82.0, 30.0)                                      # main gauche tendue (mire), repere du modele, bassin compris


def _with(st, **kw):
    d = dict(st)
    d.update(kw)
    return d


def _pose(spec, left, blade, init):
    body, el = stance(**spec)
    origin = off(blade[0], spec["pelvis"])
    if isinstance(left, tuple):
        left = off(left, spec["pelvis"])
    pose, e, init = frame(body, left, origin, blade[1], init)
    return pose, max(e, el), init


def _finish(keys, duration, step=1, hold_last=True):
    frames = A.densify(keys, step)
    return A.add_visibility(A.keyed_from(frames, duration, hold_last).anim(), [(0, True)])


# ---------------------------------------------------------------------------------------------------------------------
# Animations
# ---------------------------------------------------------------------------------------------------------------------
def tsuki_posture(vanilla):
    keys, errs = [], []
    p, e, init = guard_frame(vanilla, None)
    keys.append((0, p)); errs.append(e)
    p, e, init = _pose(COIL, AIM, CHAMBER, init)
    keys.append((10, p)); errs.append(e)
    p, e, init = _pose(COIL_DEEP, off(AIM, (1, 1, 2)), CHAMBER_DEEP, init)
    keys.append((40, p)); errs.append(e)
    for t, j in ((52, 0.5), (64, -0.5), (76, 0.4), (90, 0.0)):    # le Ki s'accumule : la lame tremble
        p, e, init = _pose(_with(COIL_DEEP, pelvis=(0.0, -6.5 - abs(j), -3.5)), off(AIM, (1, 1 + j, 2)),
                           (off(CHAMBER_DEEP[0], (j, j * 0.6, 0)), CHAMBER_DEEP[1]), init)
        keys.append((t, p)); errs.append(e)
    return _finish(keys, 90), errs


def _strike_keys(vanilla, start, strike, reach, t_strike, t_hold, t_end, init=None):
    keys, errs = [], []
    p, e, init = _pose(start, AIM, CHAMBER, init)
    keys.append((0, p)); errs.append(e)
    p, e, init = _pose(_with(start, pelvis=off(start["pelvis"], (0, -1, -1.5)), twist=start["twist"] - 6), off(AIM, (0, 0, -3)),
                       (off(CHAMBER[0], (0, 0, -3)), CHAMBER[1]), init)
    keys.append((max(2, t_strike - 4), p)); errs.append(e)
    p, e, init = _pose(strike, A.SAYA_BACK, ((-7.0, 80.0, reach), (0.0, 0.03, 1.0)), init)
    keys.append((t_strike, p)); errs.append(e)
    over = _with(strike, pelvis=off(strike["pelvis"], (0, -0.5, 1.5)), twist=strike["twist"] + 4)
    p, e, init = _pose(over, A.SAYA_BACK, ((-7.0, 80.5, reach + 2.5), (0.0, 0.02, 1.0)), init)
    keys.append((t_strike + 2, p)); errs.append(e)
    p, e, init = _pose(strike, A.SAYA_BACK, ((-7.0, 80.0, reach - 0.5), (0.0, 0.04, 1.0)), init)
    keys.append((t_hold, p)); errs.append(e)
    p, e, init = guard_frame(vanilla, init)
    keys.append((t_end, p)); errs.append(e)
    return keys, errs, init


def tsuki(vanilla):
    keys, errs, _ = _strike_keys(vanilla, COIL, STRIKE, 39.0, 8, 18, 32)
    return _finish(keys, 32), errs


def tsuki_double(vanilla):
    keys, errs = [], []
    p, e, init = _pose(COIL, AIM, CHAMBER, None)
    keys.append((0, p)); errs.append(e)
    mid = _with(STRIKE, pelvis=(0.0, -6.0, 6.0), twist=14, lean=6, rfoot=(-7.0, 18.0))
    p, e, init = _pose(mid, A.SAYA_BACK, ((-7.0, 79.0, 36.0), (0.0, 0.04, 1.0)), init)      # 1er estoc, court
    keys.append((6, p)); errs.append(e)
    back = _with(mid, twist=4, pelvis=(0.0, -6.5, 5.0))
    p, e, init = _pose(back, A.SAYA_BACK, ((-11.0, 70.0, 16.0), (0.02, 0.1, 1.0)), init)    # retrait a mi-course, le courant reprend
    keys.append((11, p)); errs.append(e)
    p, e, init = _pose(STRIKE_DEEP, A.SAYA_BACK, ((-7.0, 80.0, 46.0), (0.0, 0.02, 1.0)), init)
    keys.append((16, p)); errs.append(e)
    p, e, init = _pose(_with(STRIKE_DEEP, pelvis=(0.0, -9.5, 16.5)), A.SAYA_BACK, ((-7.0, 80.5, 48.5), (0.0, 0.02, 1.0)), init)
    keys.append((18, p)); errs.append(e)
    p, e, init = _pose(STRIKE_DEEP, A.SAYA_BACK, ((-7.0, 80.0, 45.5), (0.0, 0.03, 1.0)), init)
    keys.append((26, p)); errs.append(e)
    p, e, init = guard_frame(vanilla, init)
    keys.append((40, p)); errs.append(e)
    return _finish(keys, 40), errs


def tsuki_ruee(vanilla):
    keys, errs = [], []
    low = _with(COIL_DEEP, lean=8)
    p, e, init = _pose(low, AIM, CHAMBER_DEEP, None)
    keys.append((0, p)); errs.append(e)
    p, e, init = _pose(RUEE, A.SAYA_BACK, ((-7.0, 70.0, 42.0), (0.0, -0.02, 1.0)), init)
    keys.append((5, p)); errs.append(e)
    p, e, init = _pose(_with(RUEE, pelvis=(0.0, -14.5, 17.5)), A.SAYA_BACK, ((-7.0, 70.0, 44.5), (0.0, -0.02, 1.0)), init)
    keys.append((8, p)); errs.append(e)
    p, e, init = _pose(_with(RUEE, lean=24, pelvis=(0.0, -13.0, 15.0)), A.SAYA_BACK, ((-7.0, 72.0, 42.0), (0.0, 0.0, 1.0)), init)
    keys.append((26, p)); errs.append(e)                                                     # tenue pendant la ruee
    p, e, init = guard_frame(vanilla, init)
    keys.append((40, p)); errs.append(e)
    return _finish(keys, 40), errs


RAFALE_POINTS = [(-7, 80, 42), (-3, 86, 40), (-11, 75, 41), (-6, 83, 43), (-9, 88, 39), (-2, 79, 42), (-8, 74, 42)]


def tsuki_rafale(vanilla):
    keys, errs = [], []
    stab = _with(STRIKE, pelvis=(0.0, -7.0, 9.0), twist=18, lean=8, rfoot=(-7.5, 24.0))
    back = _with(stab, pelvis=(0.0, -7.0, 7.0), twist=8, lean=6)
    p, e, init = _pose(COIL, AIM, CHAMBER, None)
    keys.append((0, p)); errs.append(e)
    t = 3
    for i, pt in enumerate(RAFALE_POINTS):
        d = _norm((pt[0] * 0.01 + 0.07, (pt[1] - 80) * 0.012 + 0.03, 1.0))
        p, e, init = _pose(stab, A.SAYA_BACK, ((pt[0], pt[1], pt[2]), d), init)
        keys.append((t, p)); errs.append(e)
        p, e, init = _pose(back, A.SAYA_BACK, ((-11.0, 72.0, 22.0), (0.03, 0.08, 1.0)), init)
        keys.append((t + 3, p)); errs.append(e)
        t += 5
    p, e, init = _pose(_with(COIL, pelvis=(0.0, -5.0, 2.0)), A.SAYA_BACK, CHAMBER, init)       # dernier retrait
    keys.append((36, p)); errs.append(e)
    p, e, init = _pose(STRIKE_DEEP, A.SAYA_BACK, ((-7.0, 80.0, 46.0), (0.0, 0.02, 1.0)), init)
    keys.append((40, p)); errs.append(e)
    p, e, init = _pose(_with(STRIKE_DEEP, pelvis=(0.0, -9.5, 16.5)), A.SAYA_BACK, ((-7.0, 80.5, 48.5), (0.0, 0.02, 1.0)), init)
    keys.append((42, p)); errs.append(e)
    p, e, init = _pose(STRIKE_DEEP, A.SAYA_BACK, ((-7.0, 80.0, 45.5), (0.0, 0.03, 1.0)), init)
    keys.append((50, p)); errs.append(e)
    p, e, init = guard_frame(vanilla, init)
    keys.append((64, p)); errs.append(e)
    return _finish(keys, 64), errs


GATOTSU_BLADE = ((-17.0, 87.0, -4.0), (0.0, 0.03, 1.0))   # lame a hauteur d'oeil, tiree en arriere, pointe vers la cible
GATOTSU_AIM = (2.0, 86.0, 20.0)


def tsuki_charge(vanilla):
    keys, errs, init = [], [], None
    for t, j in ((0, 0.0), (15, 0.7), (30, -0.5), (45, 0.6)):
        spec = _with(GATOTSU, pelvis=(0.0, -9.0 - abs(j) * 0.8, -4.0))
        p, e, init = _pose(spec, off(GATOTSU_AIM, (0, j * 0.6, 0)), (off(GATOTSU_BLADE[0], (j, -j, 0)), GATOTSU_BLADE[1]), init)
        keys.append((t, p)); errs.append(e)
    keys.append((60, keys[0][1]))                                  # boucle sans saut
    return A.add_visibility(A.keyed_from(keys, 60).anim(), [(0, True)]), errs


def tsuki_celeste(vanilla):
    keys, errs = [], []
    p, e, init = _pose(GATOTSU, GATOTSU_AIM, GATOTSU_BLADE, None)
    keys.append((0, p)); errs.append(e)
    p, e, init = _pose(STRIKE_DEEP, A.SAYA_BACK, ((-7.0, 84.0, 46.0), (0.0, 0.0, 1.0)), init)
    keys.append((6, p)); errs.append(e)
    p, e, init = _pose(_with(STRIKE_DEEP, pelvis=(0.0, -9.5, 17.0)), A.SAYA_BACK, ((-7.0, 84.0, 49.0), (0.0, 0.0, 1.0)), init)
    keys.append((8, p)); errs.append(e)
    recoil = _with(STRIKE_DEEP, pelvis=(0.0, -9.0, 12.0), lean=8)                               # le recul du canon
    p, e, init = _pose(recoil, A.SAYA_BACK, ((-7.0, 85.0, 42.0), (0.0, 0.03, 1.0)), init)
    keys.append((14, p)); errs.append(e)
    p, e, init = _pose(recoil, A.SAYA_BACK, ((-7.0, 85.0, 42.5), (0.0, 0.03, 1.0)), init)
    keys.append((60, p)); errs.append(e)
    p, e, init = guard_frame(vanilla, init)
    keys.append((78, p)); errs.append(e)
    return _finish(keys, 78, step=2), errs


# Cinq Lances Celestes (Dieu Martial, 2026-11-12) : cinq estocs eclair dans le vide, de plus en plus hauts et en eventail (chacun
# fait naitre une lance de Ki derriere le lanceur), puis le bras tendu designe la cible pendant que les lances partent.
CINQ_POINTS = [((-7.0, 86.0, 40.0), (0.0, 0.25, 1.0)), ((-2.0, 92.0, 38.0), (0.15, 0.4, 1.0)), ((-12.0, 90.0, 38.0), (-0.15, 0.4, 1.0)),
               ((0.0, 96.0, 35.0), (0.25, 0.55, 1.0)), ((-14.0, 96.0, 35.0), (-0.25, 0.55, 1.0))]
CINQ_STABS = (4, 10, 16, 22, 28)                                  # images des cinq estocs (Percee.java : CINQ_STAB_MS)


def tsuki_cinq(vanilla):
    keys, errs = [], []
    stab = _with(STRIKE, pelvis=(0.0, -7.0, 8.0), twist=16, lean=4, rfoot=(-7.5, 22.0))
    back = _with(stab, pelvis=(0.0, -7.0, 6.0), twist=6, lean=2)
    p, e, init = _pose(COIL, AIM, CHAMBER, None)
    keys.append((0, p)); errs.append(e)
    for t, (pt, d) in zip(CINQ_STABS, CINQ_POINTS):
        p, e, init = _pose(stab, A.SAYA_BACK, (pt, d), init)
        keys.append((t, p)); errs.append(e)
        p, e, init = _pose(back, A.SAYA_BACK, ((-11.0, 76.0, 20.0), (0.03, 0.2, 1.0)), init)
        keys.append((t + 3, p)); errs.append(e)
    p, e, init = _pose(STRIKE_DEEP, A.SAYA_BACK, ((-7.0, 84.0, 43.0), (0.0, 0.08, 1.0)), init)    # la lame designe la cible
    keys.append((36, p)); errs.append(e)
    # les lances partent une a une de 1,15 s a 2,83 s (Percee.Lances) : le bras reste tendu, le corps respire a peine
    for t, dz in ((90, 0.5), (140, -0.3), (186, 0.3)):
        p, e, init = _pose(_with(STRIKE_DEEP, pelvis=(0.0, -9.0, 14.5 + dz)), A.SAYA_BACK, ((-7.0, 84.5, 43.5 + dz), (0.0, 0.08, 1.0)), init)
        keys.append((t, p)); errs.append(e)
    p, e, init = guard_frame(vanilla, init)
    keys.append((204, p)); errs.append(e)
    return _finish(keys, 204, step=2), errs


REMOVED = ("TsukiProfond",)                                          # Lance de l'Ame retiree le 2026-11-12

# (nom, fonction, animation vanilla de la vue a la 1re personne, en boucle)
ANIMS = [("TsukiPosture", tsuki_posture, "Attacks/Stab_Dash_Charged/Stab_Dash_Charging_FPS.blockyanim", False),
         ("Tsuki", tsuki, "Attacks/Stab/Stab_FPS.blockyanim", False),
         ("TsukiDouble", tsuki_double, "Attacks/Stab/Stab_FPS.blockyanim", False),
         ("TsukiRuee", tsuki_ruee, "Attacks/Stab_Dash_Charged/Stab_Dash_Charged_FPS.blockyanim", False),
         ("TsukiRafale", tsuki_rafale, "Attacks/Stab/Stab_FPS.blockyanim", False),
         ("TsukiCinq", tsuki_cinq, "Attacks/Stab/Stab_FPS.blockyanim", False),
         ("TsukiCharge", tsuki_charge, "Attacks/Stab_Dash_Charged/Stab_Dash_Charging_FPS.blockyanim", True),
         ("TsukiCeleste", tsuki_celeste, "Attacks/Stab_Dash_Charged/Stab_Dash_Charged_FPS.blockyanim", False)]


# ---------------------------------------------------------------------------------------------------------------------
# Ecriture
# ---------------------------------------------------------------------------------------------------------------------
def add_to_sets(vanilla, put, sets, errors):
    """Ajoute les animations de la Percee aux jeux donnes (appele par tenryumaru_anims.build_sets et par main)."""
    import copy
    for st in sets:                                                     # animations retirees
        for gone in REMOVED:
            st["Animations"].pop(gone, None)
    for name, fn, fps, looping in ANIMS:
        anim, errors[name] = fn(vanilla)
        entry = {"ThirdPerson": put(name, anim),
                 "FirstPerson": put(name + "_FPS", A.add_visibility(copy.deepcopy(vanilla(fps)), [(0, True)])),
                 "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim", "KeepPreviousFirstPersonAnimation": False,
                 "Speed": 1.0, "ClipsGeometry": True, "BlendingDuration": 0.08 if name in ("TsukiPosture", "TsukiCharge") else 0.05,
                 "Looping": looping}
        for s in sets:
            s["Animations"][name] = copy.deepcopy(entry)


def main(res):
    """Reecrit seulement les animations de la Percee : .blockyanim (Common) et entrees des jeux Tenryumaru_Degainee,
    Tenryumaru_Degainee_Rapide (Hien) et Tenryumaru_Fourreau (Server/Item/Animations)."""
    import json, os, zipfile
    z = zipfile.ZipFile(P.ASSETS)
    cache = {}

    def vanilla(rel, base=A.SRC_SWORD):
        if (base, rel) not in cache:
            cache[(base, rel)] = json.loads(z.read("Common/" + base + rel))
        return cache[(base, rel)]

    files = {}

    def put(name, anim):
        files[A.DST + name + ".blockyanim"] = anim
        return A.DST + name + ".blockyanim"

    server = os.path.join(res, "Server", "Item", "Animations")
    names = (A.SET_DRAWN, A.SET_DRAWN + "_Rapide", A.SET_SHEATHED)
    sets = [json.load(open(os.path.join(server, n + ".json"), encoding="utf-8")) for n in names]
    errors = {}
    add_to_sets(vanilla, put, sets, errors)
    for rel, anim in files.items():
        path = os.path.join(res, "Common", rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(anim, f, separators=(",", ":"))
    for n, data in zip(names, sets):
        with open(os.path.join(server, n + ".json"), "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
            f.write("\n")
    for k, v in errors.items():
        print("IK %-14s %s" % (k, " ".join("%.1f" % (max(e) if isinstance(e, (tuple, list)) else e) for e in v)))
    print(len(files), "animations de la Percee ecrites")


if __name__ == "__main__":
    import os, sys
    main(sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "src", "main", "resources"))
