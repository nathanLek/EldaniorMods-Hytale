"""Faces coplanaires (z-fighting) des modeles d'armes : detection et correction automatique.

Deux boites dont une face de meme sens tombe dans le meme plan et recouvre l'autre scintillent en jeu. fix_coplanar
amincit la seconde boite d'un pixel (ou d'un quart d'unite a 1 px par unite) sur l'axe concerne, jusqu'a ce qu'il n'en
reste plus. Les positions des enfants suivent le CENTRE de forme du parent (regle des blockymodels).
Utilise par gen_divines.py, gen_ryukiri.py et gen_gungnir.py.
"""
import os, sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "armures"))
import armor_kit as K  # noqa: E402

ID_Q = {"x": 0, "y": 0, "z": 0, "w": 1}


def boxes_world(node, origin=(0, 0, 0), q=ID_Q, out=None):
    """Boites du sous-arbre dans le repere de la racine : (noeud, centre, rotation, demi-tailles en unites)."""
    out = [] if out is None else out
    s = node["shape"]
    st = s.get("stretch", {"x": 1, "y": 1, "z": 1})
    off = s.get("offset", {"x": 0, "y": 0, "z": 0})
    center = tuple(origin[i] + K.qrot(q, (off["x"], off["y"], off["z"]))[i] for i in range(3))
    if s.get("type") == "box":
        sz = s["settings"]["size"]
        out.append((node, center, q, (abs(sz["x"] * st["x"]) / 2, abs(sz["y"] * st["y"]) / 2, abs(sz["z"] * st["z"]) / 2)))
    for c in node.get("children", []):
        p = K.qrot(q, (c["position"]["x"], c["position"]["y"], c["position"]["z"]))
        boxes_world(c, tuple(center[i] + p[i] for i in range(3)), K.qmul(q, c["orientation"]), out)
    return out


def _faces(b):
    node, c, q, h = b
    axes = [K.qrot(q, v) for v in ((1, 0, 0), (0, 1, 0), (0, 0, 1))]
    faces = []
    for i in range(3):
        for sgn in (1, -1):
            n = tuple(axes[i][k] * sgn for k in range(3))
            fc = tuple(c[k] + n[k] * h[i] for k in range(3))
            j, l = [a for a in range(3) if a != i]
            faces.append((i, n, fc, (axes[j], h[j]), (axes[l], h[l])))
    return faces


def _dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def _overlap(fa, fb):
    """Deux rectangles du meme plan se recouvrent-ils (axes separateurs) ?"""
    _, _, ca, ua, va = fa
    _, _, cb, ub, vb = fb
    d = tuple(cb[k] - ca[k] for k in range(3))
    for axis in (ua[0], va[0], ub[0], vb[0]):
        ra = abs(_dot(ua[0], axis)) * ua[1] + abs(_dot(va[0], axis)) * va[1]
        rb = abs(_dot(ub[0], axis)) * ub[1] + abs(_dot(vb[0], axis)) * vb[1]
        if abs(_dot(d, axis)) >= ra + rb - 0.05:
            return False
    return True


def fix_coplanar(root, label):
    """Faces de meme sens dans le meme plan qui se recouvrent = scintillement : on amincit la seconde boite d'un pixel
    sur l'axe concerne (sa face recule d'un huitieme d'unite). Repete jusqu'a ce qu'il n'en reste plus."""
    fixed = 0
    for _ in range(10):
        bs = boxes_world(root)
        faces = [(bi, f) for bi, b in enumerate(bs) for f in _faces(b)]
        changed = set()
        for x in range(len(faces)):
            bi, fa = faces[x]
            for y in range(x + 1, len(faces)):
                bj, fb = faces[y]
                if bi == bj or bj in changed or bi in changed:
                    continue
                if _dot(fa[1], fb[1]) < 0.995 or abs(_dot(tuple(fb[2][k] - fa[2][k] for k in range(3)), fa[1])) > 0.06:
                    continue
                if not _overlap(fa, fb):
                    continue
                for bk, fk in ((bj, fb), (bi, fa)):                         # la plus fine qui peut encore l'etre
                    node = bs[bk][0]
                    sz = node["shape"]["settings"]["size"]
                    key = "xyz"[fk[0]]
                    stretch = abs(node["shape"].get("stretch", {}).get(key, 1))
                    step = 1 if stretch < 1 else 0.25                      # un pixel, ou un quart d'unite a 1 px/unite
                    if sz[key] > 2 * step:
                        sz[key] -= step
                        changed.add(bk)
                        fixed += 1
                        break
        if not changed:
            break
    if fixed:
        print("  %-10s %d face(s) coplanaire(s) corrigee(s)" % (label, fixed))
    return fixed
