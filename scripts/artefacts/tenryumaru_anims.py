"""Animations de Tenryumaru : katana tenu au fourreau dans la main gauche, degaine de la main droite.

Deux jeux d'animations (Server/Item/Animations) :
  - Tenryumaru_Fourreau (objet Artefact_Sword_Tenryumaru) : lame au fourreau, main gauche a la hanche qui tient le saya, lame
    vers l'arriere ; la main droite est libre (deplacements du corps vanilla) ;
  - Tenryumaru_Degainee (objet Artefact_Sword_Tenryumaru_Degainee) : lame en main droite (animations de l'epee vanilla), le
    fourreau reste dans la main gauche.
La lame (objet tenu, racine R-Attachment) et le fourreau (objet utilitaire Artefact_Tenryumaru_Fourreau*, racine
L-Attachment, pose dans la main gauche par TenryumaruSystem) sont deux modeles : le moteur accroche tout l'objet tenu a la
main droite, quelles que soient ses racines (verifie en jeu le 2026-11-05). Chaque animation ecrit shapeVisible pour les deux : la lame passe d'une main a l'autre a
l'image exacte ou la main droite saisit (ou lache) la poignee, au meme endroit (cinematique inverse sur la poignee rengainee).

Animations propres (main droite placee par cinematique inverse, corps et jambes repris des coups vanilla) :
  Degainer      : nukitsuke, la main droite saisit la poignee, degaine en tirant le fourreau en arriere, coupe horizontale ;
  IaiCharging   : posture iai basse, main droite sur la poignee, lame au fourreau (maintien de l'attaque) ;
  IaiCharged    : degaine foudroyant, fente en avant, grande coupe horizontale (le slash divin, Java PremierSerment) ;
  Rengainer     : noto, la lame revient au fourreau (joue par Java apres un temps sans attaquer) ;
  GardeFourreau : parade au fourreau, lame rengainee tenue a l'horizontale des deux mains.
"""
import copy, json, math, os, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import pose_kit as P  # noqa: E402
K = P.K

Z3 = (0.0, 0.0, 0.0)
SRC_SWORD = "Characters/Animations/Items/Main_Handed/Sword/"
SRC_DEFAULT = "Characters/Animations/Default/"
DST = "Characters/Animations/Items/Main_Handed/Tenryumaru/"
SET_SHEATHED, SET_DRAWN = "Tenryumaru_Fourreau", "Tenryumaru_Degainee"

# ---------------------------------------------------------------------------------------------------------------------
# Main gauche au fourreau
# ---------------------------------------------------------------------------------------------------------------------
HOLD = {"L-Arm": P.euler(22, 0, -2), "L-Forearm": P.euler(-55, -15, 0), "L-Hand": P.euler(0, 0, 0)}   # poing sur le cote de la hanche
SAYA_AXIS = (0.18, -0.42, -0.89)        # sens poignee -> pointe du fourreau, au repos : nettement vers l'arriere et le bas
GRIP_X = 21.0                           # le poing gauche serre le fourreau a 21 unites du centre de la poignee (embouchure a 16)

# Repere du Handle de la lame degainee sous l'os R-Attachment (chaine Origin_Projectile -> Origin_Item -> Handle du modele)
H_POS = (0.0, -4.0, 0.0)
H_Q = {"x": -0.5, "y": -0.5, "z": 0.5, "w": 0.5}


def pose_of(d):
    return {k: (Z3, v) if isinstance(v, dict) else v for k, v in d.items()}


def norm(v):
    n = math.sqrt(sum(c * c for c in v)) or 1.0
    return tuple(c / n for c in v)


def saya_pivot():
    """Position et orientation du noeud Saya-Pivot (repere de la poignee rengainee) dans le repere de l'os L-Attachment."""
    world = P.solve(pose_of(HOLD))
    la, lq = world["L-Attachment"][0], world["L-Attachment"][1]
    axis = norm(SAYA_AXIS)
    rot_w = P.look_rot(axis, (0.0, -1.0, 0.0))                  # +y (dos de la lame) vers le bas : tranchant en haut
    origin_w = tuple(la[i] - axis[i] * GRIP_X for i in range(3))
    inv = P.qconj(lq)
    pos = K.qrot(inv, tuple(origin_w[i] - la[i] for i in range(3)))
    return pos, P.qn(K.qmul(inv, rot_w))


SAYA_POS, SAYA_Q = saya_pivot()


def sheathed_handle(world):
    """Repere monde (origine, rotation) de la poignee rengainee pour une pose resolue."""
    la, lq = world["L-Attachment"][0], world["L-Attachment"][1]
    return P.add(la, K.qrot(lq, SAYA_POS)), P.qn(K.qmul(lq, SAYA_Q))


def attachment_for_handle(h_origin, h_rot):
    """Repere que doit prendre l'os R-Attachment pour que la poignee de la lame degainee soit en (h_origin, h_rot)."""
    att_rot = P.qn(K.qmul(h_rot, P.qconj(H_Q)))
    return tuple(h_origin[i] - K.qrot(att_rot, H_POS)[i] for i in range(3)), att_rot


# ---------------------------------------------------------------------------------------------------------------------
# Cinematique inverse d'un bras (descente par coordonnees sur les angles d'Euler)
# ---------------------------------------------------------------------------------------------------------------------
LIMITS = {"Arm": [(-190, 80), (-120, 120), (-70, 140)], "Forearm": [(-150, 0), (-100, 100), (-30, 30)],
          "Hand": [(-90, 90), (-110, 110), (-80, 80)]}


IK_STARTS = {"R": [[-40, 40, 0, -70, 0, 0, -40, 80, -70], [-60, 30, 10, -90, -30, 0, 0, 0, 0], [-90, 0, 0, -40, 0, 0, 0, 0, 0]],
             "L": [[-40, -40, 0, -70, 0, 0, -40, -80, 70], [-60, -30, -10, -90, 30, 0, 0, 0, 0], [0, 0, 0, -70, -25, 0, 0, 0, 0]]}


def ik(side, body, target_pos, target_rot, init=None, rot_weight=40.0, axis=None):
    """Plusieurs departs (init, puis des poses types) : garde la meilleure solution.
    axis = (vecteur local de l'os, direction monde) : seule cette direction est imposee (target_rot ignore)."""
    best = None
    for start in ([init] if init else []) + IK_STARTS[side] + [None]:
        r = ik_once(side, body, target_pos, target_rot, start, rot_weight, axis)
        if best is None or r[3] < best[3]:
            best = r
        if init and start is init and r[3] < 2.0:                # suite d'une image voisine deja bonne : continuite
            break
    return best[:3]


def ik_once(side, body, target_pos, target_rot, init=None, rot_weight=40.0, axis=None):
    """Angles (Arm, Forearm, Hand) du bras side ('R' ou 'L') qui amenent l'os <side>-Attachment sur la cible.
    body : pose (deltas) du reste du corps. Retourne ({os: delta}, erreur de position)."""
    names = [side + "-Arm", side + "-Forearm", side + "-Hand"]
    s = -1 if side == "R" else 1                                # limites en miroir pour le bras droit (y et z opposes)
    lim = []
    for part in ("Arm", "Forearm", "Hand"):
        for j, (lo, hi) in enumerate(LIMITS[part]):
            lim.append((lo, hi) if j == 0 or s > 0 else (-hi, -lo))
    x = list(init) if init else [0.0] * 9

    def pose(v):
        p = dict(body)
        for i, n in enumerate(names):
            p[n] = (Z3, P.euler(v[3 * i], v[3 * i + 1], v[3 * i + 2]))
        return p

    def cost(v):
        w = P.solve(pose(v))
        piv, rot, _ = w[side + "-Attachment"]
        e = sum((piv[i] - target_pos[i]) ** 2 for i in range(3))
        if axis is not None:
            a = K.qrot(rot, axis[0])
            d = sum(a[i] * axis[1][i] for i in range(3))
            e += rot_weight * (math.degrees(math.acos(max(-1.0, min(1.0, d)))) / 10.0) ** 2
        elif target_rot is not None:
            d = abs(sum(rot[k] * target_rot[k] for k in "xyzw"))
            e += rot_weight * (2 * math.degrees(math.acos(min(1.0, d))) / 10.0) ** 2
        pen = sum(max(0, lo - v[i]) ** 2 + max(0, v[i] - hi) ** 2 for i, (lo, hi) in enumerate(lim))
        return e + 4 * pen

    best = cost(x)
    for step in (24, 12, 6, 3, 1.5, 0.75, 0.3):
        improved = True
        while improved:
            improved = False
            for i in range(9):
                for d in (step, -step):
                    y = list(x)
                    y[i] += d
                    c = cost(y)
                    if c < best - 1e-6:
                        x, best, improved = y, c, True
    w = P.solve(pose(x))
    err = math.sqrt(sum((w[side + "-Attachment"][0][i] - target_pos[i]) ** 2 for i in range(3)))
    return {n: P.euler(x[3 * i], x[3 * i + 1], x[3 * i + 2]) for i, n in enumerate(names)}, err, x, best


# ---------------------------------------------------------------------------------------------------------------------
# Ecriture des pistes
# ---------------------------------------------------------------------------------------------------------------------
def rq(q):
    return {k: round(q[k], 6) for k in "xyzw"}


def track(keys_q=(), keys_p=(), interp="smooth"):
    return {"position": [{"time": t, "delta": {"x": round(p[0], 4), "y": round(p[1], 4), "z": round(p[2], 4)},
                          "interpolationType": interp} for t, p in keys_p],
            "orientation": [{"time": t, "delta": rq(q), "interpolationType": interp} for t, q in keys_q],
            "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}


def vis_track(keys):
    return {"position": [], "orientation": [], "shapeStretch": [], "shapeVisible": [{"time": t, "delta": v} for t, v in keys],
            "shapeUvOffset": []}


class Names:
    drawn = []       # formes de la lame degainee (racine R-Attachment)
    hilt = []        # formes de la monture rengainee (racine L-Attachment, sauf le fourreau)


def add_visibility(anim, keys_drawn):
    """keys_drawn : [(image, lame degainee visible ?)] ; la monture rengainee prend l'etat inverse."""
    nodes = anim.setdefault("nodeAnimations", {})
    for n in Names.drawn:
        nodes.setdefault(n, vis_track([]))["shapeVisible"] = [{"time": t, "delta": v} for t, v in keys_drawn]
    for n in Names.hilt:
        nodes.setdefault(n, vis_track([]))["shapeVisible"] = [{"time": t, "delta": not v} for t, v in keys_drawn]
    return anim


def with_hold(anim, keep_left=False):
    """Main gauche au fourreau sur toute l'animation (remplace les pistes du bras gauche)."""
    out = copy.deepcopy(anim)
    nodes = out.setdefault("nodeAnimations", {})
    if not keep_left:
        for n, q in HOLD.items():
            nodes[n] = track([(0, q)])
    return out


def empty_anim(duration, hold_last=False):
    return {"formatVersion": 1, "duration": duration, "holdLastKeyframe": hold_last, "nodeAnimations": {}}


# ---------------------------------------------------------------------------------------------------------------------
# Animations composees (corps vanilla + bras par cinematique inverse)
# ---------------------------------------------------------------------------------------------------------------------
ARM_NODES = ("R-Arm", "R-Forearm", "R-Hand", "L-Arm", "L-Forearm", "L-Hand")


def body_at(anim, t):
    """Pose du corps (hors bras) d'une animation vanilla au temps t."""
    return {k: v for k, v in P.sample(anim, t).items() if k not in ARM_NODES}


def sword_target(world, origin, blade_dir, edge_dir):
    """Cible de R-Attachment pour une lame dont la poignee est en origin (relatif au centre du torse, repere du torse),
    la lame pointant vers blade_dir et le tranchant vers edge_dir (reperes du torse)."""
    _, crot, cc = world["Chest"]
    o = P.add(cc, K.qrot(crot, origin))
    bd = K.qrot(crot, norm(blade_dir))
    ed = K.qrot(crot, norm(edge_dir))
    h_rot = P.look_rot(bd, tuple(-c for c in ed))               # +y du Handle = dos de la lame, oppose au tranchant
    return attachment_for_handle(o, h_rot)


class Keyed:
    """Accumule des images clefs (pose complete par image) puis ecrit les pistes."""

    def __init__(self, duration, hold_last=True):
        self.duration, self.hold_last = duration, hold_last
        self.frames = []                                       # (t, {os: (dp, dq)})

    def key(self, t, pose):
        self.frames.append((t, pose))

    def anim(self, interp="smooth"):
        out = empty_anim(self.duration, self.hold_last)
        names = sorted({n for _, p in self.frames for n in p})
        for n in names:
            qs = [(t, p[n][1]) for t, p in self.frames if n in p]
            ps = [(t, p[n][0]) for t, p in self.frames if n in p and any(abs(c) > 1e-4 for c in p[n][0])]
            if ps and len(ps) != len(qs):
                ps = [(t, p[n][0]) for t, p in self.frames if n in p]
            out["nodeAnimations"][n] = track(qs, ps, interp)
        return out


def solve_frame(body, left, right_target, init):
    """Pose complete : corps, bras gauche (deltas), bras droit par cinematique inverse sur right_target (pos, rot)."""
    pose = dict(body)
    pose.update(pose_of(left))
    arm, err, x = ik("R", pose, right_target[0], right_target[1], init)
    pose.update(pose_of(arm))
    return pose, err, x


GRAB_CENTER = (-4.0, -15.0, 18.0)      # centre de la poignee rengainee au moment de la saisie (repere du torse) : devant le nombril
GRAB_AXIS = (0.6, -0.18, -0.78)        # fourreau presente vers le centre : la poignee vise l'axe du corps


def handle_frame(world, center, axis):
    """Repere monde d'une poignee de centre center et d'axe axis (reperes du torse), tranchant vers le haut."""
    _, crot, cc = world["Chest"]
    ax = K.qrot(crot, norm(axis))
    up = K.qrot(crot, (0.0, 1.0, 0.0))
    return P.add(cc, K.qrot(crot, center)), P.look_rot(ax, tuple(-c for c in up)), ax


def left_for_handle(body, h_org, h_rot, init=None):
    """Bras gauche qui tient le fourreau de sorte que la poignee rengainee soit en (h_org, h_rot)."""
    la_rot = P.qn(K.qmul(h_rot, P.qconj(SAYA_Q)))
    la_pos = tuple(h_org[i] - K.qrot(la_rot, SAYA_POS)[i] for i in range(3))
    left, err, _ = ik("L", body, la_pos, la_rot, init)
    return left, err


BLADE_IN_ATT = K.qrot(H_Q, (1.0, 0.0, 0.0))                     # axe de la lame dans le repere de R-Attachment


def grab(body, slide=0.0, pull=0.0, center=GRAB_CENTER, axis=GRAB_AXIS, init=None):
    """Saisie : la main droite tient la poignee (sortie de slide unites le long du fourreau), avec le roulis naturel du poignet ;
    la main gauche presente le fourreau exactement autour (reculee de pull). Retourne (pose complete, erreurs G/D, init droite)."""
    w = P.solve(body)
    h_org, h_rot, ax = handle_frame(w, center, axis)
    o = tuple(h_org[i] - ax[i] * slide for i in range(3))
    att_pos = tuple(o[i] - K.qrot(P.qn(K.qmul(h_rot, P.qconj(H_Q))), H_POS)[i] for i in range(3))
    best = None
    for start in ([init] if init else []) + [None]:            # suite de l'image d'avant, ou depart neuf : on garde la saisie
        pose = dict(body)                                       # ou les deux mains coincident le mieux
        arm, eR, x = ik("R", pose, att_pos, None, start, axis=(BLADE_IN_ATT, ax))
        pose.update(pose_of(arm))
        wr = P.solve(pose)
        pv, rr, _ = wr["R-Attachment"]
        real_rot = P.qn(K.qmul(rr, H_Q))                          # repere reel de la poignee tenue
        real_org = P.add(pv, K.qrot(rr, H_POS))
        sheath_org = tuple(real_org[i] + ax[i] * (slide + pull) for i in range(3))
        left, eL = left_for_handle(pose, sheath_org, real_rot)
        pose.update(pose_of(left))
        if best is None or max(eL, eR) < max(best[1]):
            best = (pose, (eL, eR), x)
    pose, (eL, eR), x = best
    return pose, (eL, eR), x


def grip_target(body, left, slide=0.0):
    """Cible de la main droite sur la poignee rengainee, sortie de slide unites le long du fourreau."""
    w = P.solve(dict(body, **pose_of(left)))
    o, r = sheathed_handle(w)
    ax = K.qrot(r, (1.0, 0.0, 0.0))
    o = tuple(o[i] - ax[i] * slide for i in range(3))
    return attachment_for_handle(o, r)


SAYA_BACK = {"L-Arm": P.euler(32, 0, -2), "L-Forearm": P.euler(-45, -15, 0), "L-Hand": P.euler(0, 0, 0)}   # saya-biki
SAYA_FWD = {"L-Arm": P.euler(-4, 0, -4), "L-Forearm": P.euler(-70, -26, 0), "L-Hand": P.euler(0, 0, 0)}    # pousse la tsuba


# ---------------------------------------------------------------------------------------------------------------------
# Fluidite : images intermediaires calculees dans le repere du torse (trajectoire de la lame), pas par melange d'angles
# ---------------------------------------------------------------------------------------------------------------------
def _lerp_pose(a, b, u):
    out = {}
    for n in set(a) | set(b):
        pa, qa = a.get(n, (Z3, P.ID_Q))
        pb, qb = b.get(n, (Z3, P.ID_Q))
        out[n] = (tuple(pa[i] + (pb[i] - pa[i]) * u for i in range(3)), P.slerp(qa, qb, u))
    return out


def _rel_target(pose):
    """Repere de R-Attachment exprime dans le repere du torse."""
    w = P.solve(pose)
    pv, rr, _ = w["R-Attachment"]
    _, crot, cc = w["Chest"]
    inv = P.qconj(crot)
    return K.qrot(inv, tuple(pv[i] - cc[i] for i in range(3))), P.qn(K.qmul(inv, rr))


def ease(u):
    return u * u * (3 - 2 * u)


def densify(keys, step=2, x0=None):
    """keys : [(image, pose complete)] ; renvoie [(image, pose)] avec une image toutes les step images. Entre deux clefs, le corps
    et le bras gauche sont interpoles, la lame suit une trajectoire continue dans le repere du torse (position lineaire,
    orientation spherique, avec acceleration et freinage), et le bras droit est recalcule par cinematique inverse."""
    out = [keys[0]]
    init = x0
    for (ta, pa), (tb, pb) in zip(keys, keys[1:]):
        ra, rb = _rel_target(pa), _rel_target(pb)
        t = ta + step
        while t < tb - 0.5:
            u = ease((t - ta) / float(tb - ta))
            body = {n: v for n, v in _lerp_pose(pa, pb, u).items() if not n.startswith("R-") or n == "R-Thigh" or n == "R-Calf" or n == "R-Foot"}
            w = P.solve(body)
            _, crot, cc = w["Chest"]
            rel_p = tuple(ra[0][i] + (rb[0][i] - ra[0][i]) * u for i in range(3))
            rel_q = P.slerp(ra[1], rb[1], u)
            tgt = (P.add(cc, K.qrot(crot, rel_p)), P.qn(K.qmul(crot, rel_q)))
            arm, _, init = ik("R", body, tgt[0], tgt[1], init)
            pose = dict(body, **pose_of(arm))
            out.append((t, pose))
            t += step
        out.append((tb, pb))
    return out


def keyed_from(frames, duration, hold_last=True):
    k = Keyed(duration, hold_last)
    for t, p in frames:
        k.key(t, p)
    return k


def swing_frame(body, origin, blade_dir, edge_dir, left, init):
    """Image d'une coupe : lame placee dans le repere du torse, bras gauche donne."""
    w = P.solve(dict(body, **pose_of(left)))
    return solve_frame(body, left, sword_target(w, origin, blade_dir, edge_dir), init)


def degainer(vanilla):
    """Nukitsuke : la main droite saisit la poignee (0-3), degaine en tirant le fourreau (3-8), la lame jaillit vers l'avant
    (11), coupe horizontale vers la droite (15), fin de geste (22) puis garde basse (30). Images intermediaires : densify."""
    sr = vanilla("Attacks/Swing_Right/Swing_Right.blockyanim")
    keys = []
    p, e0, init = grab(body_at(sr, 0))
    keys += [(0, p), (3, p)]
    p, e1, init = grab(body_at(sr, 6), slide=24.0, pull=6.0, init=init)
    keys.append((8, p))
    p, e2, init = swing_frame(body_at(sr, 10), (10, -8, 24), (0.55, 0.05, 0.85), (-0.6, 0.2, 0.3), SAYA_BACK, init)
    keys.append((11, p))
    p, e3, init = swing_frame(body_at(sr, 14), (-18, -6, 24), (-0.75, 0.05, 0.65), (-0.3, 0.1, -0.9), SAYA_BACK, init)
    keys.append((15, p))
    p, e4, init = swing_frame(body_at(sr, 22), (-24, -4, 12), (-0.85, 0.15, -0.3), (0.2, 0.0, -0.95), HOLD, init)
    keys.append((22, p))
    idle = vanilla("Idle.blockyanim")
    p5, e5, _ = swing_frame(body_at(idle, 0), (-8, -14, 18), (0.1, 0.35, 0.93), (0.0, -1.0, 0.2), HOLD, init)
    keys.append((30, p5))
    frames = densify(keys[1:], 2)
    anim = add_visibility(keyed_from([keys[0]] + frames, 30).anim(), [(0, False), (3, True)])
    return anim, [max(e0), max(e1), e2, e3, e4, e5]


def iai_body(vanilla, lean=14.0):
    """Corps de la posture iai : accroupi vanilla (de face, genoux flechis), buste penche, epaule gauche un peu en avant."""
    crouch = vanilla("Crouch.blockyanim", SRC_DEFAULT)
    body = body_at(crouch, 0)
    dp, dq = body.get("Belly", (Z3, P.ID_Q))
    body["Belly"] = (dp, P.qn(K.qmul(dq, P.euler(lean, -12, 0))))
    hp, hq = body.get("Head", (Z3, P.ID_Q))
    body["Head"] = (hp, P.qn(K.qmul(hq, P.euler(-lean * 0.8, 12, 0))))
    return body


def lunge_body(vanilla, t, forward=7.0, down=4.0):
    """Corps du degaine charge : revers droit vanilla (torsion de la coupe horizontale), bassin projete en avant et plus bas."""
    sr = vanilla("Attacks/Swing_Right/Swing_Right.blockyanim")
    body = body_at(sr, t)
    dp, dq = body.get("Pelvis", (Z3, P.ID_Q))
    body["Pelvis"] = ((dp[0], dp[1] - down, dp[2] + forward), dq)
    return body


def iai_charging(vanilla, from_drawn):
    """Posture iai : accroupi, la main gauche presente le fourreau, la droite sur la poignee ; le buste s'abaisse en chargeant."""
    k = Keyed(60)
    p, e0, init = grab(iai_body(vanilla, 6.0))
    k.key(0, p)
    p2, e1, _ = grab(iai_body(vanilla, 18.0), init=init)
    k.key(24, p2)
    k.key(60, p2)
    anim = add_visibility(k.anim(), [(0, True), (6, False)] if from_drawn else [(0, False)])
    return anim, [max(e0), max(e1)]


def iai_charged(vanilla):
    """Degaine foudroyant : fente en avant, la lame sort en tirant le fourreau et balaie tout l'horizon vers la droite."""
    keys = []
    p, e0, init = grab(iai_body(vanilla, 18.0))
    keys.append((0, p))
    p, e1, init = grab(lunge_body(vanilla, 6, 4.0, 3.0), slide=26.0, pull=8.0, init=init)
    keys.append((3, p))
    p, e2, init = swing_frame(lunge_body(vanilla, 10), (10, -8, 26), (0.5, 0.0, 0.86), (-0.86, 0.0, 0.5), SAYA_BACK, init)
    keys.append((6, p))
    p, e3, init = swing_frame(lunge_body(vanilla, 14), (-20, -6, 26), (-0.75, 0.0, 0.66), (-0.66, 0.0, -0.75), SAYA_BACK, init)
    keys.append((10, p))
    p, e4, init = swing_frame(lunge_body(vanilla, 20, 6.0, 4.0), (-28, -2, 8), (-0.95, 0.1, -0.3), (0.3, 0.0, -0.95), SAYA_BACK, init)
    keys.append((18, p))
    keys.append((26, p))
    anim = add_visibility(keyed_from(densify(keys, 2), 26).anim(), [(0, False), (2, True)])
    return anim, [max(e0), max(e1), e2, e3, e4]


def rengainer(vanilla):
    """Noto : la lame passe devant, pointe vers la hanche gauche, entre dans le fourreau que la main gauche presente,
    s'enfonce ; la main droite lache la poignee et retombe."""
    idle = vanilla("Idle.blockyanim")
    body = body_at(idle, 0)
    start = dict(P.sample(idle, 0), **pose_of(HOLD))
    p, e1, init = swing_frame(body, (-14, -4, 34), (0.85, -0.2, -0.5), (0.0, 1.0, 0.0), SAYA_FWD, None)
    keys = [(0, start), (8, p)]
    p, e2, init = grab(body, slide=36.0, init=init)
    keys.append((14, p))
    p, e3, init = grab(body, slide=10.0, init=init)
    keys.append((20, p))
    p, e4, init = grab(body, init=init)
    keys.append((25, p))
    frames = densify(keys[1:], 2, init)
    rest = dict(body, **pose_of(HOLD))
    rest.update({"R-Arm": (Z3, P.euler(0, 0, -4)), "R-Forearm": (Z3, P.euler(-12, 0, 0)), "R-Hand": (Z3, P.euler(0, 0, 0))})
    anim = add_visibility(keyed_from([keys[0]] + frames + [(34, rest), (40, rest)], 40).anim(), [(0, True), (25, False)])
    return anim, [e1, max(e2), max(e3), max(e4)]


# ----- Execution Celeste (rune de famille du Clan Luo, 2026-11-11) -----
KOIGUCHI_CENTER, KOIGUCHI_AXIS = (-6.0, 5.0, 24.0), (0.96, 0.14, 0.24)   # katana presente devant la poitrine, poignee a droite
KOIGUCHI_SLIDE = 7.0                                                        # la lame sort de quelques centimetres


def iai_court(vanilla):
    """Apprenti et Disciple : depuis la posture iai, degaine rapide, coupe horizontale de gauche a droite arretee au milieu,
    la lame pointee droit devant."""
    keys = []
    p, e0, init = grab(iai_body(vanilla, 18.0))
    keys.append((0, p))
    p, e1, init = grab(lunge_body(vanilla, 6, 4.0, 3.0), slide=26.0, pull=8.0, init=init)
    keys.append((3, p))
    p, e2, init = swing_frame(lunge_body(vanilla, 10), (10, -8, 26), (0.5, 0.0, 0.86), (-0.86, 0.0, 0.5), SAYA_BACK, init)
    keys.append((6, p))
    p, e3, init = swing_frame(lunge_body(vanilla, 12), (0, -6, 28), (0.0, 0.0, 1.0), (-1.0, 0.0, 0.0), SAYA_BACK, init)
    keys.append((9, p))
    keys.append((24, p))
    anim = add_visibility(keyed_from(densify(keys, 2), 24).anim(), [(0, False), (2, True)])
    return anim, [max(e0), max(e1), e2, e3]


def koiguchi(vanilla):
    """Saint et au-dela : le katana rengaine est presente devant soi, la main droite se pose sur la poignee et tire la lame de
    quelques centimetres (koiguchi o kiru), puis reste immobile pendant que les coupes tombent."""
    body = body_at(vanilla("Idle.blockyanim"), 0)
    p1, e1, init = grab(body, center=KOIGUCHI_CENTER, axis=KOIGUCHI_AXIS)
    p2, e2, _ = grab(body, slide=KOIGUCHI_SLIDE, center=KOIGUCHI_CENTER, axis=KOIGUCHI_AXIS, init=init)
    anim = add_visibility(keyed_from([(0, p1), (12, p1), (20, p2), (60, p2)], 60).anim(), [(0, True)])
    return anim, [max(e1), max(e2)]


def koiguchi_fin(vanilla):
    """Fin de l'Execution Celeste : la lame claque dans le fourreau (3 images), puis le bras gauche ramene le fourreau a la hanche."""
    idle = vanilla("Idle.blockyanim")
    body = body_at(idle, 0)
    p1, e1, init = grab(body, center=KOIGUCHI_CENTER, axis=KOIGUCHI_AXIS)
    p2, e2, _ = grab(body, slide=KOIGUCHI_SLIDE, center=KOIGUCHI_CENTER, axis=KOIGUCHI_AXIS, init=init)
    rest = dict(body, **pose_of(HOLD))
    rest.update({"R-Arm": (Z3, P.euler(0, 0, -4)), "R-Forearm": (Z3, P.euler(-12, 0, 0)), "R-Hand": (Z3, P.euler(0, 0, 0))})
    anim = add_visibility(keyed_from([(0, p2), (3, p1), (14, p1), (28, rest), (32, rest)], 32).anim(), [(0, True), (14, False)])
    return anim, [max(e2), max(e1)]



# Percee (estoc) : percee_anims.py


def garde_fourreau(vanilla):
    """Parade au fourreau : le katana rengaine tenu a l'horizontale devant le torse, poignee a droite, fourreau a gauche."""
    guard = vanilla("Attacks/Guard/Guard.blockyanim")
    p, e, _ = grab(body_at(guard, 10), center=(-12.0, 2.0, 22.0), axis=(1.0, 0.08, 0.05))
    k = Keyed(10, hold_last=False)
    k.key(0, p)
    k.key(10, p)
    anim = add_visibility(k.anim(), [(0, False)])
    return anim, list(e)


# ---------------------------------------------------------------------------------------------------------------------
# Combo Luo : 4 coups (yoko-giri, kesa-giri, kiri-age, karatake-wari), lame placee dans le repere du torse
# ---------------------------------------------------------------------------------------------------------------------
GUARD = ((-8, -14, 18), (0.1, 0.35, 0.93), (0.0, -1.0, 0.2))          # garde basse de fin de coup (= fin du degaine)

# coup : (animation vanilla du corps, duree, [(image, image du corps, origine, lame, tranchant)], decalage du bassin au pas)
LUO_COUPS = {
    "LuoCoup1": ("Attacks/Swing_Left/Swing_Left.blockyanim", 24, [        # yoko-giri : coupe horizontale de droite a gauche
        (0, 4, (-22, -2, 10), (-0.85, 0.15, -0.5), (-0.5, 0.0, 0.85)),
        (6, 10, (-4, -6, 24), (0.0, 0.05, 1.0), (1.0, 0.0, 0.0)),
        (10, 14, (16, -6, 18), (0.85, 0.05, 0.5), (0.5, 0.0, -0.85)),
    ], None),
    "LuoCoup2": ("Attacks/Swing_Left/Swing_Left.blockyanim", 24, [        # kesa-giri : diagonale de l'epaule droite vers la hanche gauche
        (0, 4, (-16, 12, 12), (-0.4, 0.8, -0.3), (0.6, 0.2, 0.75)),
        (6, 10, (-2, 0, 24), (0.3, 0.4, 0.85), (0.7, -0.7, 0.0)),
        (10, 14, (14, -14, 16), (0.6, -0.5, 0.6), (0.2, -0.75, -0.6)),
    ], None),
    "LuoCoup3": ("Attacks/Swing_Right/Swing_Right.blockyanim", 24, [      # kiri-age : diagonale remontante vers l'epaule droite
        (0, 9, (14, -16, 12), (0.6, -0.6, 0.5), (-0.6, -0.3, 0.7)),
        (6, 12, (0, -6, 24), (-0.2, 0.2, 0.95), (-0.7, 0.7, 0.0)),
        (10, 16, (-16, 10, 14), (-0.6, 0.7, 0.3), (-0.3, 0.4, -0.85)),
    ], None),
    "LuoCoup4": ("Attacks/Swing_Down_Strong/Swing_Down_Strong.blockyanim", 30, [   # karatake-wari : coupe verticale, pas en avant
        (0, 10, (-4, 18, 6), (0.0, 0.5, -0.85), (0.0, 0.85, 0.5)),
        (8, 24, (-2, -4, 26), (0.0, 0.2, 0.98), (0.0, -1.0, 0.2)),
        (12, 30, (-2, -14, 20), (0.0, -0.5, 0.85), (0.0, -0.85, -0.5)),
    ], 6.0),
}


def luo_coup(vanilla, name):
    src, duration, keys, step = LUO_COUPS[name]
    body_anim = vanilla(src)
    idle = vanilla("Idle.blockyanim")
    frames, init, errs = [], None, []
    for t, bt, origin, blade, edge in keys:
        body = body_at(body_anim, bt)
        if step:                                                         # pas en avant : bassin projete
            dp, dq = body.get("Pelvis", (Z3, P.ID_Q))
            body["Pelvis"] = ((dp[0], dp[1] - 2.0, dp[2] + step * min(1.0, t / 8.0)), dq)
        p, e, init = swing_frame(body, origin, blade, edge, HOLD, init)
        frames.append((t, p))
        errs.append(e)
    p, e, init = swing_frame(body_at(idle, 0), *GUARD, HOLD, init)
    frames.append((duration, p))
    anim = add_visibility(keyed_from(densify(frames, 2), duration).anim(), [(0, True)])
    return anim, errs


# ---------------------------------------------------------------------------------------------------------------------
# Jeux d'animations
# ---------------------------------------------------------------------------------------------------------------------
LOCOMOTION = ["Idle", "Walk", "WalkBackward", "Run", "RunBackward", "Sprint", "Crouch", "CrouchWalk", "CrouchWalkBackward",
              "Jump", "JumpWalk", "JumpRun", "Fall", "FallFar", "FluidIdle", "FluidWalk", "FluidWalkBackward", "FluidRun"]
SWINGS = ["SwingLeft", "SwingRight", "SwingDown", "SwingDownStrong", "Stab", "Guard", "GuardBash"]


# Kata du Dragon d'Or (gen_luo_katanas.py) : mouvements empruntes a d'autres armes, ajoutes au jeu de la lame au clair.
# Ils vivent ici depuis le 2026-11-12 : build_sets (appele par gen_tenryumaru.py) reconstruisait le jeu sans eux et les coups
# 7, 9, 10 et 11 du kata (Kaiten, Tourbillon, Salto, Tornade) n'avaient plus d'animation.
KATA_EXTRA_ANIMS = {"LuoSalto": ("Battleaxe", "SwingDownFrontflipChop"), "LuoTornade": ("Daggers", "KickTornadoRight"),
                    "LuoKaiten": ("Spear", "SpinSwingLeft", 1.4), "LuoTourbillon": ("Battleaxe", "Whirlwind", 1.3)}


def add_kata_extras(drawn, z):
    for name, spec in KATA_EXTRA_ANIMS.items():
        e = dict(json.loads(z.read("Server/Item/Animations/%s.json" % spec[0]))["Animations"][spec[1]])
        e.pop("ThirdPersonFace", None)
        if len(spec) > 2:
            e["Speed"] = spec[2]                                          # vitesse propre au kata
        drawn["Animations"][name] = e


def write_rapide(drawn, server):
    """Jeu rapide de Hien : celui de la lame au clair, combo et degaine 25 % plus vifs."""
    fast = copy.deepcopy(drawn)
    for name, e in fast["Animations"].items():
        if name.startswith("LuoCoup") or name == "Degainer":
            e["Speed"] = round(e.get("Speed", 1.0) * 1.25, 3)
    with open(os.path.join(server, SET_DRAWN + "_Rapide.json"), "w", encoding="utf-8") as f:
        json.dump(fast, f, indent=2)
        f.write("\n")


def build_sets(res, preview=None):
    """Ecrit les .blockyanim (Common) et les deux jeux d'animations (Server). Retourne les erreurs de cinematique inverse."""
    z = zipfile.ZipFile(P.ASSETS)
    cache = {}

    def vanilla(rel, base=SRC_SWORD):
        if (base, rel) not in cache:
            cache[(base, rel)] = json.loads(z.read("Common/" + base + rel))
        return cache[(base, rel)]

    files = {}

    def put(name, anim):
        files[DST + name + ".blockyanim"] = anim
        return DST + name + ".blockyanim"

    sword = json.loads(z.read("Server/Item/Animations/Sword.json"))
    default = json.loads(z.read("Server/Item/Animations/Default.json"))
    errors = {}

    # ----- lame degainee : vanilla + main gauche au fourreau + lame visible -----
    drawn = copy.deepcopy(sword)
    drawn["Animations"] = {}
    for key, entry in sword["Animations"].items():
        e = copy.deepcopy(entry)
        hold = key in LOCOMOTION or key in SWINGS
        for field in ("ThirdPerson", "ThirdPersonMoving", "FirstPerson"):
            path = e.get(field)
            if not (isinstance(path, str) and path.startswith(SRC_SWORD)):
                continue
            src = vanilla(path[len(SRC_SWORD):])
            anim = with_hold(src) if (hold and field != "FirstPerson") else copy.deepcopy(src)
            add_visibility(anim, [(0, True)])
            e[field] = put("Degainee/" + path[len(SRC_SWORD):-len(".blockyanim")], anim)
        drawn["Animations"][key] = e

    # ----- lame au fourreau : main gauche au fourreau, main droite libre -----
    sheathed = copy.deepcopy(default)
    sheathed["Camera"] = copy.deepcopy(default.get("Camera", {}))
    garde = empty_anim(60)                                      # respiration : le poing gauche bouge a peine sur la hanche
    for n, (a0, a1) in {"L-Arm": (P.euler(22, 0, -2), P.euler(24, 1, -3)),
                        "L-Forearm": (P.euler(-55, -15, 0), P.euler(-57, -14, 0))}.items():
        garde["nodeAnimations"][n] = track([(0, a0), (30, a1), (60, a0)])
    garde["nodeAnimations"]["L-Hand"] = track([(0, P.euler(0, 0, 0)), (30, P.euler(2, 0, 1)), (60, P.euler(0, 0, 0))])
    add_visibility(garde, [(0, False)])
    garde_path = put("Fourreau/Garde_Fourreau", garde)
    for key in LOCOMOTION:
        e = copy.deepcopy(default["Animations"].get(key) or sword["Animations"][key])
        e["ThirdPerson"] = garde_path
        e.pop("ThirdPersonMoving", None)
        fp = e.get("FirstPerson")
        if isinstance(fp, str) and fp.startswith(SRC_DEFAULT):
            fanim = add_visibility(copy.deepcopy(vanilla(fp[len(SRC_DEFAULT):], SRC_DEFAULT)), [(0, False)])
            e["FirstPerson"] = put("Fourreau/" + fp[len(SRC_DEFAULT):-len(".blockyanim")], fanim)
        sheathed["Animations"][key] = e
    hide = put("Fourreau/Lame_Cachee", add_visibility(empty_anim(60), [(0, False)]))
    for key, e in sheathed["Animations"].items():               # nage, escalade, vol... : lame au fourreau, corps vanilla
        if key not in LOCOMOTION and not e.get("ThirdPerson"):
            e["ThirdPerson"] = hide
    for e in list(sheathed["Animations"].values()) + list(drawn["Animations"].values()):
        e.setdefault("BlendingDuration", 0.2)                  # transitions adoucies entre deplacements
    # les coups de la lame degainee existent aussi dans le jeu du fourreau (signature lancee lame au fourreau)
    for key in SWINGS:
        sheathed["Animations"][key] = copy.deepcopy(drawn["Animations"][key])

    # ----- animations propres -----
    fps_hidden = put("Fourreau/Idle_FPS_Rengainee", add_visibility(copy.deepcopy(vanilla("Idle_FPS.blockyanim", SRC_DEFAULT)), [(0, False)]))
    a, errors["Degainer"] = degainer(vanilla)
    path = put("Degainer", a)
    fpa = add_visibility(copy.deepcopy(vanilla("Attacks/Swing_Right/Swing_Right_FPS.blockyanim")), [(0, False), (4, True)])
    entry = {"ThirdPerson": path, "FirstPerson": put("Degainer_FPS", fpa), "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim",
             "KeepPreviousFirstPersonAnimation": False, "Speed": 1.0, "ClipsGeometry": True, "BlendingDuration": 0.08}
    sheathed["Animations"]["Degainer"] = entry
    drawn["Animations"]["Degainer"] = copy.deepcopy(entry)

    for flag, target, suffix in ((False, sheathed, "Fourreau"), (True, drawn, "Degainee")):
        a, errors["IaiCharging_" + suffix] = iai_charging(vanilla, flag)
        target["Animations"]["IaiCharging"] = {
            "ThirdPerson": put("Iai_Charging_" + suffix, a), "FirstPerson": fps_hidden, "Looping": False, "Speed": 1.0,
            "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim", "KeepPreviousFirstPersonAnimation": False,
            "ClipsGeometry": True, "BlendingDuration": 0.12}
    a, errors["IaiCharged"] = iai_charged(vanilla)
    fpa = add_visibility(copy.deepcopy(vanilla("Attacks/Stab_Dash_Charged/Stab_Dash_Charged_FPS.blockyanim")), [(0, False), (2, True)])
    entry = {"ThirdPerson": put("Iai_Charged", a), "FirstPerson": put("Iai_Charged_FPS", fpa), "Speed": 1.0,
             "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim", "KeepPreviousFirstPersonAnimation": False,
             "ClipsGeometry": True, "BlendingDuration": 0.05}
    sheathed["Animations"]["IaiCharged"] = entry
    drawn["Animations"]["IaiCharged"] = copy.deepcopy(entry)

    a, errors["Rengainer"] = rengainer(vanilla)
    drawn["Animations"]["Rengainer"] = {"ThirdPerson": put("Rengainer", a), "FirstPerson": fps_hidden, "Speed": 1.0,
                                       "KeepPreviousFirstPersonAnimation": False, "BlendingDuration": 0.15}
    sheathed["Animations"]["Rengainer"] = copy.deepcopy(drawn["Animations"]["Rengainer"])

    for name in LUO_COUPS:                                          # combo Luo, commun a tous les katanas du clan
        a, errors[name] = luo_coup(vanilla, name)
        fpa = add_visibility(copy.deepcopy(vanilla("Attacks/Swing_Left/Swing_Left_FPS.blockyanim" if name != "LuoCoup4"
                                                   else "Attacks/Swing_Down_Strong/Swing_Down_Strong_FPS.blockyanim")), [(0, True)])
        entry = {"ThirdPerson": put(name, a), "FirstPerson": put(name + "_FPS", fpa),
                 "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim", "KeepPreviousFirstPersonAnimation": False,
                 "Speed": 1.0, "ClipsGeometry": True, "BlendingDuration": 0.08}
        drawn["Animations"][name] = entry
        sheathed["Animations"][name] = copy.deepcopy(entry)

    # Execution Celeste (2026-11-11) : degaine court, koiguchi et claquement final, dans les deux jeux
    for name, fn, fps in (("IaiCourt", iai_court, "Attacks/Swing_Right/Swing_Right_FPS.blockyanim"),
                          ("Koiguchi", koiguchi, None), ("KoiguchiFin", koiguchi_fin, None)):
        a, errors[name] = fn(vanilla)
        entry = {"ThirdPerson": put(name, a),
                 "FirstPerson": put(name + "_FPS", add_visibility(copy.deepcopy(vanilla(fps)), [(0, True)])) if fps else fps_hidden,
                 "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim", "KeepPreviousFirstPersonAnimation": False,
                 "Speed": 1.0, "ClipsGeometry": True, "BlendingDuration": 0.1}
        drawn["Animations"][name] = entry
        sheathed["Animations"][name] = copy.deepcopy(entry)

    # Percee (refaite le 2026-11-12) : animations composees dans percee_anims.py (fente, posture, gatotsu...)
    import percee_anims
    percee_anims.add_to_sets(vanilla, put, (drawn, sheathed), errors)

    a, errors["GardeFourreau"] = garde_fourreau(vanilla)
    sheathed["Animations"]["Guard"] = {"ThirdPerson": put("Garde_Fourreau_Parade", a), "FirstPerson": fps_hidden,
                                      "ThirdPersonFace": "Characters/Animations/Expressions/Frown.blockyanim",
                                      "KeepPreviousFirstPersonAnimation": False, "BlendingDuration": 0.1, "Looping": True}

    for rel, anim in files.items():
        path = os.path.join(res, "Common", rel)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(anim, f, separators=(",", ":"))
    server = os.path.join(res, "Server", "Item", "Animations")
    os.makedirs(server, exist_ok=True)
    add_kata_extras(drawn, z)
    write_rapide(drawn, server)
    for name, data in ((SET_SHEATHED, sheathed), (SET_DRAWN, drawn)):
        with open(os.path.join(server, name + ".json"), "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
            f.write("\n")
    return files, errors
