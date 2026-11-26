#!/usr/bin/env python3
"""
Visages de l'Observateur (2026-11-17) : un objet « portrait » cache par modele d'entite (vanilla + mod) et le catalogue
classe que lisent le HUD d'Arkies Domini et la fenetre de la rune Sans Visage.

Pourquoi des objets : un .ui de mod ne sait pas dessiner le modele d'une autre entite, mais l'element ItemPreviewComponent
(celui des fenetres d'inventaire du jeu) affiche en 3D le modele d'un OBJET (propriete ItemId, reglable par le serveur).
Chaque portrait est donc un objet sans categorie (absent de l'inventaire creatif) dont le Model/Texture sont ceux du
modele d'entite. Limite : les attachements (cheveux, armures, accessoires) ne sont pas dessines.

Sorties :
  src/main/resources/Server/Item/Items/Visages/Eldanior_Visage_<ModelId>.json
  src/main/resources/eldanior/visages.json   {"pnj": [[id, nom], ...], "animal": [...], "monstre": [...]}

Usage : python3 scripts/observateur/gen_visages.py [--dry-run]
"""
import json
import os
import shutil
import sys
import zipfile

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
RES = os.path.join(ROOT, 'src', 'main', 'resources')
OUT_ITEMS = os.path.join(RES, 'Server', 'Item', 'Items', 'Visages')
OUT_CATALOG = os.path.join(RES, 'eldanior', 'visages.json')
ZIP = os.environ.get('HYTALE_ASSETS_ZIP', os.path.expanduser(
    '~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip'))
PREFIX = 'Eldanior_Visage_'

# dossier (premier niveau sous Server/Models) -> categorie de la fenetre Sans Visage
CATEGORY = {
    'Intelligent': 'pnj', 'Human': 'pnj',
    'Beast': 'animal', 'Critter': 'animal', 'Flying_Beast': 'animal', 'Flying_Critter': 'animal',
    'Flying_Wildlife': 'animal', 'Livestock': 'animal', 'Pets': 'animal', 'Swimming_Beast': 'animal',
    'Swimming_Critter': 'animal', 'Swimming_Wildlife': 'animal', 'Wildlife': 'animal',
    'Boss': 'monstre', 'Elemental': 'monstre', 'Undead': 'monstre', 'Void': 'monstre', 'Christmas': 'monstre',
}
ROOT_FILES = {'NPC_Elf': 'pnj', 'NPC_Santa': 'pnj', 'Warrior_Quest': 'pnj'}
# sous-dossiers du mod (Server/Models/Eldanior/<x>)
MOD_CATEGORY = {'Guards': 'pnj', 'Dojo': 'pnj', 'Familiers': 'animal', 'Summons': 'monstre'}
MOD_FILES = {'Eldanior_Dragon_Avatar': 'monstre'}
SKIP_WORDS = ('Test', 'Marker', 'Transition')
# dossiers ou un OBJET a le droit de prendre son modele et sa texture (validation du serveur, vue le 2026-11-17 :
# « Common Asset 'Characters/Player.blockymodel' must be within the root » = mod refuse au chargement)
ITEM_MODEL_ROOTS = ('Blocks/', 'Items/', 'Resources/', 'NPC/', 'VFX/', 'Consumable/')
ITEM_TEXTURE_ROOTS = ('Blocks/', 'BlockTextures/', 'Items/', 'NPC/', 'Resources/', 'VFX/')
# portrait sans place dans la fenetre (le HUD en a besoin : joueurs)
ALWAYS = {'Player'}


# Taille dans l'apercu 3D (2026-11-17 : « certains animaux ou monstres sont coupes ») : l'element ItemPreviewComponent
# n'ajuste pas la taille du modele a son cadre. On mesure le modele (boites du .blockymodel, 64 unites = 1 bloc, enfants
# places au centre de la forme du parent ; rotations ignorees) et le serveur regle ItemScale = FIT_BLOCKS / plus grande
# dimension, borne entre FIT_MIN et 1 (les petits modeles ne sont jamais grossis).
FIT_BLOCKS, FIT_MIN = 0.85, 0.08   # 2026-11-17 : 1.4 coupait encore la tete du squelette (cadre de 110 px)
_zip = None


def read_common(path):
    """Fichier de Common/ : celui du mod d'abord, sinon celui du jeu."""
    global _zip
    mod = os.path.join(RES, 'Common', path)
    if os.path.isfile(mod):
        with open(mod, 'rb') as fh:
            return fh.read()
    if _zip is None:
        _zip = zipfile.ZipFile(ZIP)
    try:
        return _zip.read('Common/' + path)
    except KeyError:
        return None


def model_size(path):
    """Plus grande dimension du modele, en blocs (None si illisible)."""
    raw = read_common(path)
    if raw is None:
        return None
    try:
        d = json.loads(raw.decode('utf-8-sig'))
    except Exception:
        return None
    mn, mx = [1e9] * 3, [-1e9] * 3

    def walk(n, origin):
        p = n.get('position') or {}
        pos = [origin[0] + p.get('x', 0), origin[1] + p.get('y', 0), origin[2] + p.get('z', 0)]
        sh = n.get('shape') or {}
        off = sh.get('offset') or {}
        c = [pos[0] + off.get('x', 0), pos[1] + off.get('y', 0), pos[2] + off.get('z', 0)]
        size = (sh.get('settings') or {}).get('size') or sh.get('size') or {}
        st = sh.get('stretch') or {}
        if size and sh.get('type', 'box') != 'none' and sh.get('visible', True) is not False:
            for i, k in enumerate('xyz'):
                h = abs(size.get(k, 0) * st.get(k, 1)) / 2
                mn[i], mx[i] = min(mn[i], c[i] - h), max(mx[i], c[i] + h)
        for ch in n.get('children') or []:
            walk(ch, c)

    for n in d.get('nodes') or []:
        walk(n, [0, 0, 0])
    if mx[0] < mn[0]:
        return None
    return max(mx[i] - mn[i] for i in range(3)) / 64


def fit(path):
    size = model_size(path)
    if not size or size <= 0:
        return 1.0
    return round(max(FIT_MIN, min(1.0, FIT_BLOCKS / size)), 3)


def pretty(model_id):
    s = model_id
    for p in ('Eldanior_', 'NPC_'):
        if s.startswith(p):
            s = s[len(p):]
    return s.replace('_', ' ')


def load_models():
    """{id: (data, categorie|None, source)} pour les modeles vanilla et du mod."""
    models = {}
    with zipfile.ZipFile(ZIP) as z:
        for n in z.namelist():
            if not (n.startswith('Server/Models/') and n.endswith('.json')):
                continue
            rel = n[len('Server/Models/'):]
            mid = os.path.splitext(os.path.basename(rel))[0]
            parts = rel.split('/')
            cat = CATEGORY.get(parts[0]) if len(parts) > 1 else ROOT_FILES.get(mid)
            try:
                models[mid] = (json.loads(z.read(n).decode('utf-8-sig')), cat, 'vanilla')
            except Exception as e:
                print('  ! lecture', n, e)
    base = os.path.join(RES, 'Server', 'Models')
    for dirpath, _, files in os.walk(base):
        for f in files:
            if not f.endswith('.json'):
                continue
            rel = os.path.relpath(os.path.join(dirpath, f), base).replace(os.sep, '/')
            mid = os.path.splitext(f)[0]
            parts = rel.split('/')
            cat = None
            if parts[0] == 'Eldanior':
                cat = MOD_CATEGORY.get(parts[1]) if len(parts) > 2 else MOD_FILES.get(mid)
            try:
                with open(os.path.join(dirpath, f), encoding='utf-8-sig') as fh:
                    models[mid] = (json.load(fh), cat, 'mod')
            except Exception as e:
                print('  ! lecture', rel, e)
    return models


def resolve(mid, models, key, depth=0):
    """Valeur d'un champ, en remontant les Parent."""
    if mid not in models or depth > 8:
        return None
    d = models[mid][0]
    if d.get(key):
        return d[key]
    p = d.get('Parent')
    return resolve(p, models, key, depth + 1) if p else None


def main():
    dry = '--dry-run' in sys.argv
    models = load_models()
    catalog = {'pnj': [], 'animal': [], 'monstre': []}
    items = {}
    skipped = []
    scales = {}
    for mid, (d, cat, src) in sorted(models.items()):
        if cat is None and mid not in ALWAYS:
            continue
        if any(w in mid for w in SKIP_WORDS):
            continue
        model, texture = resolve(mid, models, 'Model'), resolve(mid, models, 'Texture')
        if not model or not texture or not isinstance(texture, str):
            continue
        if cat:                                      # le deguisement n'a pas besoin d'objet : modele dans le catalogue
            catalog[cat].append([mid, pretty(mid)]) if mid not in ALWAYS else None
        if not model.startswith(ITEM_MODEL_ROOTS) or not texture.startswith(ITEM_TEXTURE_ROOTS):
            skipped.append(mid)                      # pas de portrait 3D (modele sous Characters/ ...)
            continue
        f = fit(model)
        if f < 1.0:
            scales[mid] = f
        items[mid] = {
            '$Comment': 'Genere par scripts/observateur/gen_visages.py : portrait 3D du modele ' + mid
                        + ' (HUD Arkies Domini, fenetre Sans Visage). Objet cache, ne pas donner.',
            'TranslationProperties': {'Name': pretty(mid)},
            'Model': model,
            'Texture': texture,
            'PlayerAnimationsId': 'Item',
            'Quality': 'Developer',
            'MaxStack': 1,
            'Scale': 1.0,
            'DropOnDeath': False,
        }
    for c in catalog.values():
        c.sort(key=lambda e: e[1].lower())
    catalog['scales'] = dict(sorted(scales.items()))     # ItemScale des apercus (absent = 1)
    print('sans portrait (dossier interdit a un objet) :', len(skipped), ', '.join(skipped))
    print('apercus reduits :', len(scales), '| exemples :', ', '.join(f'{k} {v}' for k, v in list(sorted(scales.items(), key=lambda e: e[1]))[:6]))
    print('portraits :', len(items), '|', ', '.join(f'{k} {len(v)}' for k, v in catalog.items() if k != 'scales'))
    if dry:
        return
    if os.path.isdir(OUT_ITEMS):
        shutil.rmtree(OUT_ITEMS)
    os.makedirs(OUT_ITEMS)
    for mid, item in items.items():
        with open(os.path.join(OUT_ITEMS, PREFIX + mid + '.json'), 'w', encoding='utf-8') as fh:
            json.dump(item, fh, ensure_ascii=False, indent=2)
    os.makedirs(os.path.dirname(OUT_CATALOG), exist_ok=True)
    with open(OUT_CATALOG, 'w', encoding='utf-8') as fh:
        json.dump(catalog, fh, ensure_ascii=False, indent=1)
    print('ecrit :', os.path.relpath(OUT_ITEMS, ROOT), '+', os.path.relpath(OUT_CATALOG, ROOT))


if __name__ == '__main__':
    main()
