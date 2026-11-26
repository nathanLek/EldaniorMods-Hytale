"""Animations de rechargement des armes a feu (jeu d'animations « Mousquet », 2026-11-15) : calculees par cinematique
inverse (pose_kit) a partir de la pose de repos du fusil vanilla, en troisieme personne et en vue FPS.

  - Rechargement (2,5 s) / RechargementRapide (1,25 s) / RechargementEclair (0,7 s) : le mousquet passe en travers du corps,
    bouche haute a gauche (« port d'armes ») ; la main gauche saisit la baguette, bourre le long du canon (deux coups, un
    pour la version rapide, un bref pour l'eclair), puis amorce le bassinet (petites tapes pres de la platine) et le
    mousquet revient a l'epaule. Java (Mousquet) choisit la version d'apres la duree du rechargement.
  - RechargementChargeur (1,6 s) : l'arme penche sur la droite, la main gauche descend chercher le chargeur sous le boitier,
    le remonte et le claque, puis manoeuvre le verrou a droite (SARS, canon de char : culasse).
La crosse est collee a la main droite (os R-Attachment) : la main gauche n'atteint pas la bouche (86 unites), d'ou le
bourrage le long du canon. Repere de l'arme : noeud Platine_Repere du modele (+z bouche, +y dessus, +x platine).

Usage interne : import mousquet_anims as MA ; MA.write_all(res_dir, model_path, preview_dir)
"""
import json, math, os, sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import pose_kit as P  # noqa: E402

K = P.K
Z3 = (0.0, 0.0, 0.0)
RIFLE = "Characters/Animations/Items/Dual_Handed/Rifle/"
MY = "Characters/Animations/Items/Dual_Handed/Mousquet/"
A = 2.2                                                                    # axe du canon dans le repere de la platine


def norm(v):
    n = math.sqrt(sum(c * c for c in v)) or 1.0
    return tuple(c / n for c in v)


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def dot(a, b):
    return sum(a[i] * b[i] for i in range(3))


def quat_from_axes(X, Y, Z):
    """Rotation dont les colonnes sont X, Y, Z (base orthonormee directe)."""
    m00, m01, m02 = X[0], Y[0], Z[0]
    m10, m11, m12 = X[1], Y[1], Z[1]
    m20, m21, m22 = X[2], Y[2], Z[2]
    tr = m00 + m11 + m22
    if tr > 0:
        s = math.sqrt(tr + 1.0) * 2
        q = {"w": 0.25 * s, "x": (m21 - m12) / s, "y": (m02 - m20) / s, "z": (m10 - m01) / s}
    elif m00 > m11 and m00 > m22:
        s = math.sqrt(1.0 + m00 - m11 - m22) * 2
        q = {"w": (m21 - m12) / s, "x": 0.25 * s, "y": (m01 + m10) / s, "z": (m02 + m20) / s}
    elif m11 > m22:
        s = math.sqrt(1.0 + m11 - m00 - m22) * 2
        q = {"w": (m02 - m20) / s, "x": (m01 + m10) / s, "y": 0.25 * s, "z": (m12 + m21) / s}
    else:
        s = math.sqrt(1.0 + m22 - m00 - m11) * 2
        q = {"w": (m10 - m01) / s, "x": (m02 + m20) / s, "y": (m12 + m21) / s, "z": 0.25 * s}
    return P.qn(q)


def gun_frame(model):
    """Repere (position, rotation) du noeud Platine_Repere dans le repere de l'os R-Attachment (memes regles que
    pose_kit.item_quads : un enfant se place par rapport au centre de forme de son parent)."""
    found = []

    def walk(n, origin, q):
        if n["name"] == "Platine_Repere":
            found.append((origin, q))
            return
        s = n.get("shape", {})
        off = s.get("offset") or {"x": 0, "y": 0, "z": 0}
        center = P.add(origin, K.qrot(q, (off["x"], off["y"], off["z"])))
        for c in n.get("children", []):
            p = K.qrot(q, (c["position"]["x"], c["position"]["y"], c["position"]["z"]))
            walk(c, P.add(center, p), P.qn(K.qmul(q, c.get("orientation") or P.ID_Q)))
    root = model["nodes"][0]
    for c in root.get("children", []):
        walk(c, (c["position"]["x"], c["position"]["y"], c["position"]["z"]), c.get("orientation") or P.ID_Q)
    return found[0]


def ik_keep(side, body, target_pos, target_rot=None, init=None, rot_weight=30.0):
    """IK d'un bras (Arm, Forearm, Hand) qui GARDE les deltas de position du corps (vue FPS : bras deplaces)."""
    names = [side + "-Arm", side + "-Forearm", side + "-Hand"]
    s = -1 if side == "R" else 1
    lim = []
    for part in ("Arm", "Forearm", "Hand"):
        for j, (lo, hi) in enumerate(_LIMITS[part]):
            lim.append((lo, hi) if j == 0 or s > 0 else (-hi, -lo))
    base_pos = {n: (body.get(n, (Z3, P.ID_Q))[0]) for n in names}

    def pose(v):
        p = dict(body)
        for i, n in enumerate(names):
            p[n] = (base_pos[n], P.euler(v[3 * i], v[3 * i + 1], v[3 * i + 2]))
        return p

    def cost(v):
        w = P.solve(pose(v))
        piv, rot, _ = w[side + "-Attachment"]
        e = sum((piv[i] - target_pos[i]) ** 2 for i in range(3))
        if target_rot is not None:
            d = abs(sum(rot[k] * target_rot[k] for k in "xyzw"))
            e += rot_weight * (2 * math.degrees(math.acos(min(1.0, d))) / 10.0) ** 2
        pen = sum(max(0, lo - v[i]) ** 2 + max(0, v[i] - hi) ** 2 for i, (lo, hi) in enumerate(lim))
        return e + 4 * pen

    best_x, best_c = None, None
    starts = ([init] if init else []) + _STARTS[side]
    for st in starts:
        x = list(st)
        c = cost(x)
        for step in (24, 12, 6, 3, 1.5, 0.75):
            improved = True
            while improved:
                improved = False
                for i in range(9):
                    for d in (step, -step):
                        y = list(x)
                        y[i] += d
                        cy = cost(y)
                        if cy < c - 1e-6:
                            x, c, improved = y, cy, True
        if best_c is None or c < best_c:
            best_x, best_c = x, c
        if init and st is init and c < 4.0:
            break
    return {n: (base_pos[n], P.euler(best_x[3 * i], best_x[3 * i + 1], best_x[3 * i + 2])) for i, n in enumerate(names)}, best_x


_LIMITS = {"Arm": [(-190, 80), (-120, 120), (-70, 140)], "Forearm": [(-150, 0), (-100, 100), (-30, 30)],
           "Hand": [(-90, 90), (-110, 110), (-80, 80)]}
_STARTS = {"R": [[-40, 40, 0, -70, 0, 0, -40, 80, -70], [-60, 30, 10, -90, -30, 0, 0, 0, 0], [-90, 0, 0, -40, 0, 0, 0, 0, 0],
                 [-20, 0, 0, -60, 0, 0, 0, 0, 0]],
           "L": [[-40, -40, 0, -70, 0, 0, -40, -80, 70], [-60, -30, -10, -90, 30, 0, 0, 0, 0], [-90, 0, 0, -40, 0, 0, 0, 0, 0],
                 [-20, 0, 0, -60, 0, 0, 0, 0, 0]]}


# ---------------------------------------------------------------------------------------------------------------------
# Poses de l'arme
# ---------------------------------------------------------------------------------------------------------------------
def gun_rot(barrel, top_hint):
    Zb = norm(barrel)
    Y = norm(tuple(top_hint[i] - dot(top_hint, Zb) * Zb[i] for i in range(3)))
    X = cross(Y, Zb)
    return quat_from_axes(X, Y, Zb)


class Rig:
    """Pose de repos (3P ou FPS) et repere de l'arme : convertit « ou est l'arme » en cibles des deux mains."""
    def __init__(self, idle, frame, fps):
        self.fps = fps
        self.idle = idle
        self.body0 = P.sample(idle, 0)
        self.world0 = P.solve(self.body0)
        self.fpos, self.fq = frame                                        # Platine_Repere dans R-Attachment
        att_p, att_q, _ = self.world0["R-Attachment"]
        self.grip0 = att_p
        g = P.qn(K.qmul(att_q, self.fq))
        self.barrel0 = K.qrot(g, (0, 0, 1))
        self.top0 = K.qrot(g, (0, 1, 0))
        # main gauche au repos, dans le repere de la platine (elle suit l'arme quand celle-ci bouge)
        org = P.add(att_p, K.qrot(att_q, self.fpos))
        rel = tuple(self.world0["L-Attachment"][0][i] - org[i] for i in range(3))
        self.left0 = K.qrot(P.qconj(g), rel)

    def attachment(self, grip, barrel, top):
        """Os R-Attachment (position, rotation) pour une arme dont le repere de platine a cette orientation, le pivot de
        l'attache etant place en grip."""
        gq = gun_rot(barrel, top)
        att_q = P.qn(K.qmul(gq, P.qconj(self.fq)))
        return grip, att_q

    def point(self, grip, att_q, local):
        """Point du repere de la platine -> monde."""
        return P.add(P.add(grip, K.qrot(att_q, self.fpos)), K.qrot(P.qn(K.qmul(att_q, self.fq)), local))


def lerp3(a, b, u):
    return tuple(a[i] + (b[i] - a[i]) * u for i in range(3))


def ease(u):
    return u * u * (3 - 2 * u)


def build(rig, keys, duration, step=3):
    """keys : [(image, etat)] ; etat = {"grip": decalage de la poignee, "barrel": direction, "top": dessus, "left": point de la
    platine ou va la main gauche (None = pose de repos), "head": inclinaison de la tete}. Images intermediaires interpolees
    puis resolues (une toutes les `step` images)."""
    times = list(range(0, duration + 1, step))
    if times[-1] != duration:
        times.append(duration)
    frames = []
    initR = initL = None
    for t in times:
        for (ta, sa), (tb, sb) in zip(keys, keys[1:]):
            if ta <= t <= tb:
                u = ease((t - ta) / float(tb - ta)) if tb > ta else 0.0
                break
        else:
            (ta, sa), (tb, sb), u = keys[-1], keys[-1], 0.0
        if sa is None and sb is None:
            frames.append((t, None))
            continue
        a = sa or REST
        b = sb or REST
        grip = P.add(rig.grip0, lerp3(a["grip"], b["grip"], u))
        barrel = norm(lerp3(a["barrel"] or rig.barrel0, b["barrel"] or rig.barrel0, u))
        top = norm(lerp3(a["top"] or rig.top0, b["top"] or rig.top0, u))
        grip_p, att_q = rig.attachment(grip, barrel, top)
        body = dict(rig.body0)
        head = a.get("head", 0) + (b.get("head", 0) - a.get("head", 0)) * u
        if not rig.fps and head and "Head" in body:
            hp, hq = body["Head"]
            body["Head"] = (hp, P.qn(K.qmul(hq, P.euler(head, 0, 0))))
        right, initR = ik_keep("R", body, grip_p, att_q, initR, rot_weight=40.0)
        body.update(right)
        la = a.get("left") if a.get("left") is not None else rig.left0
        lb = b.get("left") if b.get("left") is not None else rig.left0
        lt = rig.point(grip_p, att_q, lerp3(la, lb, u))
        left, initL = ik_keep("L", body, lt, None, initL)
        body.update(left)
        frames.append((t, body))
    return frames


REST = {"grip": Z3, "barrel": None, "top": None, "left": None, "head": 0}


def to_anim(rig, frames, duration):
    """Pistes : bras, avant-bras, mains (resolus) ; le reste reprend la pose de repos (deltas de la premiere image)."""
    out = {"formatVersion": 1, "duration": duration, "holdLastKeyframe": False, "nodeAnimations": {}}
    # haut du corps seulement : jambes, bassin et vetements suivent la marche ou la course (sinon elles se figent)
    names = {"R-Arm", "R-Forearm", "R-Hand", "L-Arm", "L-Forearm", "L-Hand"} | (set() if rig.fps else {"Head"})
    for name in names:
        node = {"position": [], "orientation": [], "shapeStretch": [], "shapeVisible": [], "shapeUvOffset": []}
        for t, body in frames:
            p, q = (body or rig.body0).get(name, (Z3, P.ID_Q))
            node["position"].append({"time": t, "delta": {"x": round(p[0], 3), "y": round(p[1], 3), "z": round(p[2], 3)},
                                     "interpolationType": "smooth"})
            node["orientation"].append({"time": t, "delta": {k: round(q[k], 6) for k in "xyzw"}, "interpolationType": "smooth"})
        if all(k["delta"] == node["position"][0]["delta"] for k in node["position"]):
            node["position"] = node["position"][:1] if any(abs(v) > 1e-6 for v in node["position"][0]["delta"].values()) else []
        if all(k["delta"] == node["orientation"][0]["delta"] for k in node["orientation"]):
            node["orientation"] = node["orientation"][:1]
        out["nodeAnimations"][name] = node
    return out


# ---------------------------------------------------------------------------------------------------------------------
# Scenarios
# ---------------------------------------------------------------------------------------------------------------------
# directions dans le repere du joueur : avant +Z, gauche +X, haut +Y
PORT = norm((0.55, 0.72, 0.42))                 # bouche haute, a gauche, un peu en avant
PORT_TOP = norm((-0.25, 0.2, -1.0))             # dessus du canon (baguette) vers le tireur
PRIME = norm((0.35, 0.2, 0.92))                 # mousquet bas et presque a plat pour amorcer
PRIME_TOP = norm((0.0, 1.0, -0.1))


def musket_keys(kind, fps):
    lo = (-3.0, -10.0, -4.0) if not fps else (6.0, -8.0, -10.0)      # poignee descendue devant la hanche droite
    pr = (-2.0, -6.0, 0.0) if not fps else (4.0, -6.0, -4.0)
    port = lambda left, head=-12: {"grip": lo, "barrel": PORT, "top": PORT_TOP, "left": left, "head": head}   # noqa: E731
    prime = lambda left: {"grip": pr, "barrel": PRIME, "top": PRIME_TOP, "left": left, "head": -18}           # noqa: E731
    up, down = (0, A + 2.6, 46.0), (0, A + 2.6, 30.0)                 # le long du dessus du canon (baguette)
    grab = (0, A - 2.6, 26.0)                                         # sous le fut
    pan = (3.4, 4.4, 3.5)                                             # au-dessus du bassinet
    tap = (3.4, 6.2, 3.5)
    if kind == "Rechargement":
        return 150, [(0, None), (14, port(grab)), (28, port(up)), (40, port(down)), (52, port(up)), (64, port(down)),
                     (76, port(up)), (90, port(grab)), (104, prime(tap)), (111, prime(pan)), (118, prime(tap)), (125, prime(pan)),
                     (150, None)]
    if kind == "RechargementRapide":
        return 76, [(0, None), (8, port(grab)), (16, port(up)), (26, port(down)), (36, port(up)), (46, prime(tap)),
                    (53, prime(pan)), (76, None)]
    return 42, [(0, None), (7, port(up, -8)), (15, port(down, -8)), (23, prime(pan)), (42, None)]


TILT_TOP = norm((-0.9, 0.45, 0.0))              # arme penchee sur la droite


def magazine_keys(fps):
    lo = (0.0, -8.0, -2.0) if not fps else (4.0, -8.0, -6.0)
    tilt = lambda left, head=-14: {"grip": lo, "barrel": None, "top": TILT_TOP, "left": left, "head": head}  # noqa: E731
    mag, mag_out = (0, -4.5, 6.0), (2.0, -14.0, 4.0)
    bolt = (6.0, 2.5, -2.5)
    return 96, [(0, None), (10, tilt((0, A - 2.6, 24.0))), (22, tilt(mag)), (34, tilt(mag_out, -22)), (46, tilt(mag_out, -22)),
                (58, tilt(mag)), (64, tilt((0, -2.5, 6.0))), (74, tilt(bolt, -10)), (80, tilt((6.0, 2.5, -6.0), -10)),
                (86, tilt(bolt, -10)), (96, None)]


ANIMS = {"Rechargement": "musket", "RechargementRapide": "musket", "RechargementEclair": "musket", "RechargementChargeur": "mag"}


def write_all(res, model_path, preview=None):
    model = json.load(open(model_path))
    frame = gun_frame(model)
    idle = P.load_json("Common/" + RIFLE + "Idle.blockyanim")
    idle_fps = P.load_json("Common/" + RIFLE + "Idle_FPS.blockyanim")
    d = os.path.join(res, "Common", MY)
    os.makedirs(d, exist_ok=True)
    shots = []
    for name, kind in ANIMS.items():
        for fps in (False, True):
            rig = Rig(idle_fps if fps else idle, frame, fps)
            duration, keys = magazine_keys(fps) if kind == "mag" else musket_keys(name, fps)
            frames = build(rig, keys, duration)
            anim = to_anim(rig, frames, duration)
            json.dump(anim, open(os.path.join(d, name + ("_FPS" if fps else "") + ".blockyanim"), "w"), separators=(",", ":"))
            if preview and not fps:
                tex = png_read(model_path[:-len(".blockymodel")] + ".png")
                for t in [k[0] for k in keys][:8]:
                    shots.append(P.render(P.sample(anim, t), [(model, tex)], 256, yaw=35, pitch=10))
        print("  animation %s" % name)
    if preview and shots:
        img = P.sheet(shots, 4)
        sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
        import png
        png.write(os.path.join(preview, "mousquet_rechargement.png"), len(img[0]), len(img), img)
    return list(ANIMS)


def png_read(path):
    sys.path.insert(0, os.path.join(HERE, "..", "rune_aura_icons"))
    import png
    w, h, img = png.read(path)
    return img
