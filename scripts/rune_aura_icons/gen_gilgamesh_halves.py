"""Armes de Gilgamesh Babylon en attente : copie des modeles vanilla dont les pieces cote poignee (HIDDEN_FRACTION de
la longueur) sont masquees -> l'arme semble sortir de son portail. Affiche aussi les decalages a reporter dans le code.
Usage : python3 gen_gilgamesh_halves.py <dossier_resources_Common> <dossier_contenant_Common/...extrait d'Assets.zip>"""
import json, os, sys
out, src = sys.argv[1], sys.argv[2]
UNITS_PER_BLOCK = 64.0          # modeles d'entites (joueur : 121 unites pour 1.85 bloc) ; 32 pour les modeles de blocs
HIDDEN_FRACTION = 0.35          # part de l'arme masquee (poignee, pommeau)
WEAPONS = [("Sword", "Sword/Adamantite", 1.6), ("Katana", "Longsword/Katana", 1.4),
           ("Ornate", "Sword/Frost", 1.5), ("Longsword", "Longsword/Adamantite", 1.4)]
def qrot(q, v):
    x, y, z, w = q['x'], q['y'], q['z'], q['w']; vx, vy, vz = v
    ix = w*vx + y*vz - z*vy; iy = w*vy + z*vx - x*vz; iz = w*vz + x*vy - y*vx; iw = -x*vx - y*vy - z*vz
    return (ix*w - iw*x - iy*z + iz*y, iy*w - iw*y - iz*x + ix*z, iz*w - iw*z - ix*y + iy*x)
def qmul(a, b):
    return {'x': a['w']*b['x']+a['x']*b['w']+a['y']*b['z']-a['z']*b['y'], 'y': a['w']*b['y']-a['x']*b['z']+a['y']*b['w']+a['z']*b['x'],
            'z': a['w']*b['z']+a['x']*b['y']-a['y']*b['x']+a['z']*b['w'], 'w': a['w']*b['w']-a['x']*b['x']-a['y']*b['y']-a['z']*b['z']}
ID = {'x': 0, 'y': 0, 'z': 0, 'w': 1}
def shapes(d):
    """(noeud, centre_y, min_y, max_y) de chaque forme, et y de Origin_Projectile."""
    res, tip = [], [None]
    def walk(n, pos, rot):
        p = n.get('position', {}); o = n.get('orientation') or ID
        rp = qrot(rot, (p.get('x', 0), p.get('y', 0), p.get('z', 0))); np_ = tuple(pos[i] + rp[i] for i in range(3)); nr = qmul(rot, o)
        if n.get('name') == 'Origin_Projectile': tip[0] = np_[1]
        s = n.get('shape') or {}; sz = (s.get('settings') or {}).get('size')
        if s.get('type') in ('box', 'quad') and sz:
            off = s.get('offset', {}); c = qrot(nr, (off.get('x', 0), off.get('y', 0), off.get('z', 0)))
            ys = [np_[1] + c[1] + qrot(nr, (a*sz.get('x', 0)/2, b*sz.get('y', 0)/2, e*sz.get('z', 0)/2))[1]
                  for a in (-1, 1) for b in (-1, 1) for e in (-1, 1)]
            res.append((n, np_[1] + c[1], min(ys), max(ys)))
        for ch in n.get('children', []): walk(ch, np_, nr)
    for n in d['nodes']: walk(n, (0, 0, 0), ID)
    return res, tip[0]
G = os.path.join(out, 'Items/Projectiles/Gilgamesh'); os.makedirs(G, exist_ok=True)
out_final, out_start = [], []
for name, path, scale in WEAPONS:
    d = json.load(open(os.path.join(src, 'Common/Items/Weapons', path + '.blockymodel')))
    sh, tip = shapes(d)
    lo, hi = min(s[2] for s in sh), max(s[3] for s in sh); cut = lo + (hi - lo) * HIDDEN_FRACTION
    hidden = 0
    for n, cy, _, _ in sh:
        if cy < cut: n['shape']['visible'] = False; hidden += 1
    mid = min(s[2] for s in sh if s[0]['shape'].get('visible', True) is not False)   # debut de la partie visible = portail
    json.dump(d, open(os.path.join(G, 'Half_%s.blockymodel' % name), 'w'), indent=2)
    k = scale / UNITS_PER_BLOCK
    out_final.append((tip - mid) * k); out_start.append((tip - hi) * k)
    print('%-9s longueur %.2f blocs, %d piece(s) masquee(s) sur %d' % (name, (hi - lo) * k, hidden, len(sh)))
print('EMERGE_FINAL =', ', '.join('%.2f' % v for v in out_final))
print('EMERGE_START =', ', '.join('%.2f' % v for v in out_start))
