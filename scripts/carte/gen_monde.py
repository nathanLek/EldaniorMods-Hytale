#!/usr/bin/env python3
"""Fabrique le générateur Hytale du continent d'Eldanior (couche 2 du plan docs/plan-carte-eldanior.md).

Entrées :
  - le masque produit par continent.py --masque <dossier> (Mask.png + zones.json) ;
  - les zones de Hytale, lues directement dans Assets.zip du jeu installé (même version que le serveur).
Sorties, sous <racine> (racine d'un pack d'assets : src/main/resources pour le mod, ou un dossier d'essai) :
  Server/World/Eldanior/World.json, Zones.json, Mask.png
  Server/World/Default/Zones/Eldanior_<Zone>/Zone.json + biomes (Tile.* / Custom.*)
  Server/Languages/en-US/server.lang (bloc « carte Eldanior » : noms affichés à l'entrée des territoires)

Règles : pas de dossier Cave (donc aucune grotte) ; dans les biomes, on ne garde que la nature
(Trees, Plants, Rock_Formations) : plus de monuments, camps, PNJ, donjons ; pas de UniquePrefabs.

Usage :
    python3 -I scripts/carte/gen_monde.py <dossier masque> <racine>
"""
import copy, json, os, re, shutil, sys, zipfile

ASSETS_ZIP = os.path.expanduser('~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip')
ZONES = 'Server/World/Default/Zones/'
NATURE = ('Trees.', 'Plants.', 'Rock_Formations.')
SEA = 114          # niveau de la mer des biomes de Hytale

# ------------------------------------------------------------------ recettes des zones
# tiles / customs : (zone source, nom du fichier sans .json). « as_tile » : biome Custom transformé en Tile.
# special : biomes fabriqués ici (bambous, glycines, lac, plateau).
RECETTES = {
    'Royaume': dict(nom="Royaume d'Eldanior", cle='Royaume_Eldanior', zone='Zone1_Tier1', affiche=True,
                    tiles=[('Zone1_Tier1', 'Tile.Plains_Smooth'), ('Zone1_Tier1', 'Tile.Plains_Birch'),
                           ('Zone1_Tier1', 'Tile.Forest_Flower'), ('Zone1_Tier2', 'Tile.Plains_Tallgrass')],
                    customs=[('Zone1_Tier1', 'Custom.River_Plains_Smooth'), ('Zone1_Tier1', 'Custom.River_Plains_Birch'),
                             ('Zone1_Tier1', 'Custom.River_Forest_Flower')]),
    'Plateau': dict(nom="Royaume d'Eldanior", cle='Royaume_Eldanior', zone='Zone1_Tier1', affiche=False,
                    special=['plateau']),
    'Marquisat_Nord': dict(nom='Marquisat du Nord', cle='Marquisat_Nord', zone='Zone3_Tier1', affiche=True,
                           tiles=[('Zone3_Tier1', 'Tile.Forest_Fir'), ('Zone3_Tier1', 'Tile.Forest_Tundra'),
                                  ('Zone3_Tier1', 'Tile.Forest_Redwood'), ('Zone3_Tier2', 'Tile.Plains_Tundra'),
                                  ('Zone3_Tier2', 'Tile.Forest_Cedar')],
                           customs=[('Zone3_Tier1', 'Custom.Lake_Forest_Fir'), ('Zone3_Tier1', 'Custom.Valley_Forest_Fir')]),
    'Marquisat_Ouest': dict(nom="Marquisat de l'Ouest", cle='Marquisat_Ouest', zone='Zone2_Tier1', affiche=True,
                            tiles=[('Zone2_Tier1', 'Tile.Savannah_Plains'), ('Zone2_Tier1', 'Tile.Savannah_Rock'),
                                   ('Zone2_Tier1', 'Tile.Scrub_Bushland'), ('Zone2_Tier1', 'Tile.Savannah_Boab'),
                                   ('Zone2_Tier2', 'Tile.Desert_Red'), ('Zone2_Tier2', 'Tile.Desert_Oasis')],
                            customs=[('Zone2_Tier1', 'Custom.River')]),
    'Marquisat_Est': dict(nom="Marquisat de l'Est", cle='Marquisat_Est', zone='Zone1_Tier2', affiche=True,
                          tiles=[('Zone1_Tier2', 'Tile.Forest_Aspen'), ('Zone1_Tier2', 'Tile.Plains_Tallgrass'),
                                 ('Zone1_Tier2', 'Tile.Forest_Gully'), ('Zone1_Tier1', 'Tile.Forest_Birch'),
                                 ('Zone1_Tier1', 'Tile.Plains_Birch')],
                          customs=[('Zone1_Tier2', 'Custom.River_Forest_Aspen'),
                                   ('Zone1_Tier2', 'Custom.River_Plains_Tallgrass')]),
    'Luo': dict(nom='Empire Luo', cle='Empire_Luo', zone='Zone1_Tier3', affiche=True,
                tiles=[('Zone1_Tier3', 'Tile.Forest_Moss'), ('Zone1_Tier2', 'Tile.Plains_Tallgrass')],
                special=['bambous', 'glycines']),
    'Montagnes': dict(nom="Monts d'Eldanior", cle='Monts_Eldanior', zone='Zone1_Tier2', affiche=False,
                      as_tile=[('Zone1_Tier2', 'Custom.Mountain_Tier2')]),
    'Montagnes_Neige': dict(nom='Monts du Nord', cle='Monts_Nord', zone='Zone3_Tier1', affiche=False,
                            as_tile=[('Zone3_Tier1', 'Custom.Mountain_Zone3_Tier1')]),
    'Montagnes_Luo': dict(nom='Monts de Luo', cle='Monts_Luo', zone='Zone1_Tier3', affiche=False,
                          as_tile=[('Zone1_Tier3', 'Custom.Mountain_Tier3')]),
    'Foret_Frontiere': dict(nom='Forêts frontalières', cle='Forets_Frontiere', zone='Zone1_Tier3', affiche=False,
                            tiles=[('Zone1_Tier3', 'Tile.Forest_Moss'), ('Zone1_Tier1', 'Tile.Forest_Birch')]),
    'Lacs': dict(nom='Lacs', cle='Lacs_Eldanior', zone='Zone1_Tier3', affiche=False, special=['lac']),
    'Cote': dict(nom='Côtes', cle='Cotes_Eldanior', zone='Zone1_Shore', affiche=False,
                 tiles=[('Zone1_Shore', 'Tile.Beach')], customs=[('Zone1_Shore', 'Custom.Cliffs')]),
    'Cote_Froide': dict(nom='Côtes du Nord', cle='Cotes_Nord', zone='Zone3_Shore_Tier1', affiche=False,
                        tiles=[('Zone3_Shore_Tier1', 'Tile.Beach_Rocky'), ('Zone3_Shore_Tier1', 'Tile.Beach_Tundra')]),
    'Ocean_Cote': dict(nom='Mer', cle='Mer_Eldanior', zone='Zone1_Shallow_Ocean', affiche=False,
                       tiles=[('Zone1_Shallow_Ocean', 'Tile.Shore')], customs=[('Zone1_Shallow_Ocean', 'Custom.Rocks')]),
    'Ocean': dict(nom='Mer', cle='Mer_Eldanior', zone='Oceans', affiche=False,
                  tiles=[('Oceans', 'Tile.Temperate_Sandy'), ('Oceans', 'Tile.Temperate_Kelp'),
                         ('Oceans', 'Tile.Temperate_Reef'), ('Oceans', 'Tile.Temperate_Barren')]),
}

# Carte tirée d'une image (carte_image.py) : aucune frontière ni nom affiché, les biomes ne donnent que les sols, les
# plantes et les arbres sur un sol plat (Y ~120, fonds marins ~100) ; tout le relief vient du générateur (Terrain.bin).
def _r(z, nom, tiles=(), as_tile=(), special=(), ocean=False):
    return dict(nom=nom, cle='Eldanior_' + z, zone=(tiles or as_tile or [('Zone1_Tier1', '')])[0][0], affiche=False,
                tiles=list(tiles), as_tile=list(as_tile), special=list(special), ocean=ocean)

RECETTES_IMAGE = {
    'Ocean': _r('Ocean', 'Mer', [('Oceans', 'Tile.Temperate_Sandy'), ('Oceans', 'Tile.Temperate_Kelp'),
                                 ('Oceans', 'Tile.Temperate_Reef'), ('Oceans', 'Tile.Temperate_Barren')], ocean=True),
    'Ocean_Cote': _r('Ocean_Cote', 'Mer', [('Zone1_Shallow_Ocean', 'Tile.Shore')], ocean=True),
    'Cote': _r('Cote', 'Côtes', [('Zone1_Shore', 'Tile.Beach')]),
    'Cote_Froide': _r('Cote_Froide', 'Côtes du Nord', [('Zone3_Shore_Tier1', 'Tile.Beach_Rocky'),
                                                       ('Zone3_Shore_Tier1', 'Tile.Beach_Tundra')]),
    'Plaines': _r('Plaines', 'Plaines', [('Zone1_Tier1', 'Tile.Plains_Smooth'), ('Zone1_Tier1', 'Tile.Plains_Birch'),
                                         ('Zone1_Tier1', 'Tile.Forest_Flower'), ('Zone1_Tier2', 'Tile.Plains_Tallgrass')]),
    'Collines': _r('Collines', 'Collines', [('Zone1_Tier2', 'Tile.Plains_Tallgrass'), ('Zone1_Tier2', 'Tile.Forest_Aspen'),
                                            ('Zone1_Tier1', 'Tile.Forest_Birch')]),
    'Automne': _r('Automne', "Forêts d'automne", [('Zone1_Tier3', 'Tile.Forest_Autumn'),
                                                  ('Zone1_Tier2', 'Tile.Plains_Tallgrass')]),
    'Foret': _r('Foret', 'Forêts profondes', [('Zone1_Tier3', 'Tile.Forest_Moss'), ('Zone1_Tier1', 'Tile.Forest_Flower')]),
    # Pas de biome enneigé par nature : la neige est posée par le générateur au-dessus de la limite des neiges.
    'Nord': _r('Nord', 'Taïga', [('Zone3_Tier1', 'Tile.Forest_Fir'), ('Zone3_Tier1', 'Tile.Forest_Redwood'),
                                 ('Zone3_Tier2', 'Tile.Forest_Cedar')]),
    'Nord_Plaines': _r('Nord_Plaines', 'Prairies du Nord', [('Zone3_Tier1', 'Tile.Plains_Shire'),
                                                            ('Zone3_Tier1', 'Tile.Forest_Fir')]),
    'Montagnes': _r('Montagnes', 'Montagnes', as_tile=[('Zone1_Tier2', 'Custom.Mountain_Tier2')]),
    'Montagnes_Nord': _r('Montagnes_Nord', 'Montagnes du Nord', as_tile=[('Zone1_Tier3', 'Custom.Mountain_Tier3')]),
    'Montagnes_Neige': _r('Montagnes_Neige', 'Sommets', as_tile=[('Zone1_Tier2', 'Custom.Mountain_Tier2')]),
    'Montagnes_Foret': _r('Montagnes_Foret', 'Forêts de montagne', [('Zone3_Tier2', 'Tile.Forest_Cedar_Mixed'),
                                                                    ('Zone3_Tier1', 'Tile.Forest_Fir')]),
    'Luo': _r('Luo', 'Bambouseraies', [('Zone1_Tier3', 'Tile.Forest_Moss'), ('Zone1_Tier2', 'Tile.Plains_Tallgrass')],
              special=['bambous', 'glycines']),
}
SOL_TERRE = 120     # sol plat des biomes de terre (carte image)
SOL_MER = 100       # fond plat des biomes marins (carte image)

def aplatir(biome, sol):
    biome['TerrainHeightThreshold'] = {'Positions': [sol - 3, sol - 1, sol + 1, sol + 3], 'Values': [1, 0.75, 0.25, 0]}
    biome.pop('CustomBiomeGenerator', None)

# ------------------------------------------------------------------ outils
class Assets:
    def __init__(self, path):
        self.z = zipfile.ZipFile(path)
        self.names = set(self.z.namelist())

    def json(self, zone, name):
        p = ZONES + zone + '/' + name + '.json'
        if p not in self.names:
            raise SystemExit('introuvable dans Assets.zip : ' + p)
        return json.loads(self.z.read(p).decode('utf-8-sig'))

def nature_only(biome):
    """Retire de Prefabs.Entries tout ce qui n'est pas de la nature (monuments, camps, PNJ, donjons, grottes)."""
    P = biome.get('Prefabs')
    if not isinstance(P, dict):
        return 0
    kept, removed = [], 0
    for e in P.get('Entries', []):
        prefabs = e.get('Prefab', [])
        weights = e.get('Weight')
        keep_idx = [i for i, p in enumerate(prefabs) if p.startswith(NATURE)]
        if not keep_idx:
            removed += 1
            continue
        if len(keep_idx) != len(prefabs):
            e = copy.deepcopy(e)
            e['Prefab'] = [prefabs[i] for i in keep_idx]
            if isinstance(weights, list) and len(weights) == len(prefabs):
                e['Weight'] = [weights[i] for i in keep_idx]
            removed += 1
        kept.append(e)
    P['Entries'] = kept
    return removed

def nature_covers(biome):
    """Retire des couvertures les objets fabriqués ou macabres (tonneaux, caisses, coffres, pots, os, feux)."""
    C = biome.get('Covers')
    if not isinstance(C, list):
        return 0
    bad = ('Furniture_', 'Deco_Bone', 'Deco_Fire', 'Deco_Pot')
    kept = [c for c in C if not any(bad_ in json.dumps(c.get('Type', '')) for bad_ in bad)]
    biome['Covers'] = kept
    return len(C) - len(kept)

def as_tile(biome, weight=30.0, size=1.0, base=None):
    """Biome Custom -> Tile. Avec base, on reprend son relief (hauteur des plaines) : c'est le générateur
    Eldanior qui soulève ensuite le terrain (Relief.bin), sans falaise aux limites des zones."""
    b = copy.deepcopy(biome)
    b.pop('CustomBiomeGenerator', None)
    if base is not None:
        b['HeightmapNoise'] = copy.deepcopy(base['HeightmapNoise'])
        b['TerrainHeightThreshold'] = copy.deepcopy(base['TerrainHeightThreshold'])
    b = {'Weight': weight, 'SizeModifier': size, **b}
    return b

def swap_trees(biome, prefabs, weights, pattern):
    """Remplace les entrées d'arbres d'un biome par une seule entrée (bambous, glycines)."""
    b = copy.deepcopy(biome)
    entries = [e for e in b['Prefabs']['Entries'] if not any(p.startswith('Trees.') for p in e.get('Prefab', []))]
    entries.append({'Weight': weights, 'Prefab': prefabs, 'Pattern': {'File': pattern}})
    b['Prefabs']['Entries'] = entries
    return b

def flat_biome(base, positions, values, covers=True, prefabs=True):
    b = copy.deepcopy(base)
    b['TerrainHeightThreshold'] = {'Positions': positions, 'Values': values}
    if not covers:
        b['Covers'] = {'Entries': []} if isinstance(b.get('Covers'), dict) else b.get('Covers')
    if not prefabs and isinstance(b.get('Prefabs'), dict):
        b['Prefabs']['Entries'] = []
    return b

def specials(a, kind):
    out = {}
    if kind == 'plateau':
        base = a.json('Zone1_Tier1', 'Tile.Plains_Smooth')
        # Plateau dégagé (herbe et quelques rochers, pas d'arbres) ; il est surélevé par Relief.bin.
        b = copy.deepcopy(base)   # hauteur normale : le soulèvement vient de Relief.bin
        b['Prefabs']['Entries'] = [e for e in b['Prefabs']['Entries']
                                   if all(p.startswith('Rock_Formations.Rocks') for p in e.get('Prefab', []))]
        out['Tile.Plateau'] = b
    elif kind == 'lac':
        base = a.json('Zone1_Tier3', 'Tile.Forest_Moss')
        # Fond à ~106 : 8 blocs d'eau ; pas de couverture ni d'arbres sous l'eau.
        out['Tile.Lac'] = flat_biome(base, [100, 104, 106, 108], [1, 0.9, 0.5, 0], covers=False, prefabs=False)
    elif kind == 'bambous':
        base = a.json('Zone1_Tier3', 'Tile.Forest_Moss')
        out['Tile.Bambouseraie'] = swap_trees(base, ['Trees.Bamboo.Stage_2.*', 'Trees.Bamboo.Stage_3.*'], [3, 1],
                                              'Zones.Zone1_Tier1.PrefabPatterns.Forest_Mixed_Oak')
    elif kind == 'glycines':
        base = a.json('Zone1_Tier1', 'Tile.Plains_Smooth')
        out['Tile.Glycines'] = swap_trees(base, ['Trees.Wisteria.Stage_2.*', 'Trees.Wisteria.Stage_3.*'], [3, 1],
                                          'Zones.Zone1_Tier1.PrefabPatterns.Plains_Smooth_Oak')
    return out

# ------------------------------------------------------------------ génération
def write_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

def main():
    if len(sys.argv) != 3:
        sys.exit(__doc__)
    masque, root = sys.argv[1], sys.argv[2]
    info = json.load(open(os.path.join(masque, 'zones.json'), encoding='utf-8'))
    a = Assets(ASSETS_ZIP)
    world_dir = os.path.join(root, 'Server/World/Eldanior')
    zones_dir = os.path.join(root, 'Server/World/Default/Zones')
    os.makedirs(world_dir, exist_ok=True)

    # Monde : masque de SPAN x SPAN blocs centré sur 0,0.
    span = info['span_blocs']
    image = info.get('recettes') == 'image'
    recettes = RECETTES_IMAGE if image else RECETTES
    for old in ('Relief.bin', 'Terrain.bin'):
        if os.path.exists(os.path.join(world_dir, old)):
            os.remove(os.path.join(world_dir, old))
    if image and os.path.isdir(zones_dir):
        for d in os.listdir(zones_dir):
            if d.startswith('Eldanior_') and d[9:] not in recettes:
                shutil.rmtree(os.path.join(zones_dir, d))
    write_json(os.path.join(world_dir, 'World.json'), {
        'OverrideDataFolder': '../Default/', 'PrefabStore': 'ASSETS',
        'Width': span, 'Height': span, 'OffsetX': span // 2, 'OffsetY': span // 2,
        'Randomizer': {'Generators': [{'NoiseType': 'OLD_SIMPLEX', 'Octaves': 3, 'Scale': 0.01,
                                       'Amplitude': 16 if image else 40}]},
    })
    write_json(os.path.join(world_dir, 'Zones.json'), {
        'GridGenerator': {'Scale': 0.0008, 'Jitter': 0.9, 'Randomizer': {'Generators': [
            {'NoiseType': 'OLD_SIMPLEX', 'Octaves': 3, 'Persistence': 0.5, 'Scale': 0.008, 'Amplitude': 100}]}},
        'MaskMapping': {color: ['Eldanior_' + z] for z, color in info['zones'].items()},
    })
    shutil.copyfile(os.path.join(masque, 'Mask.png'), os.path.join(world_dir, 'Mask.png'))
    for f in ('Relief.bin', 'Terrain.bin'):
        if os.path.exists(os.path.join(masque, f)):
            shutil.copyfile(os.path.join(masque, f), os.path.join(world_dir, f))

    lang = {}
    for z in info['zones']:
        r = recettes[z]
        zdir = os.path.join(zones_dir, 'Eldanior_' + z)
        if os.path.isdir(zdir):
            shutil.rmtree(zdir)
        zone = a.json(r['zone'], 'Zone')
        zone.pop('UniquePrefabs', None)
        disc = zone.setdefault('Discovery', {})
        disc['ZoneName'] = r['cle']
        disc['Display'] = r['affiche']
        write_json(os.path.join(zdir, 'Zone.json'), zone)
        lang['map.zone.' + r['cle']] = r['nom']
        biomes = {}
        for src, name in r.get('tiles', []):
            biomes[name] = a.json(src, name)
        for src, name in r.get('customs', []):
            biomes[name] = a.json(src, name)
        plaine = a.json('Zone1_Tier1', 'Tile.Plains_Smooth')
        for src, name in r.get('as_tile', []):
            biomes['Tile.' + name.split('.', 1)[1]] = as_tile(a.json(src, name), base=plaine)
        for kind in r.get('special', []):
            biomes.update(specials(a, kind))
        removed = 0
        for name, b in biomes.items():
            removed += nature_only(b) + nature_covers(b)
            if image:
                aplatir(b, SOL_MER if r.get('ocean') else SOL_TERRE)
            write_json(os.path.join(zdir, name + '.json'), b)
        tiles = [n for n in biomes if n.startswith('Tile.')]
        # Les biomes Custom ne s'appliquent qu'aux Tiles nommés dans BiomeMask : on retire les noms absents.
        for name, b in biomes.items():
            g = b.get('CustomBiomeGenerator')
            if g and 'BiomeMask' in g:
                g['BiomeMask'] = [m for m in g['BiomeMask'] if 'Tile.' + m in biomes]
                write_json(os.path.join(zdir, name + '.json'), b)
        print('zone Eldanior_%-16s %d biomes (%d Tile), %d entrées non naturelles retirées'
              % (z, len(biomes), len(tiles), removed))

    # Noms affichés : bloc dédié dans server.lang (remplacé à chaque génération).
    lang_path = os.path.join(root, 'Server/Languages/en-US/server.lang')
    os.makedirs(os.path.dirname(lang_path), exist_ok=True)
    old = open(lang_path, encoding='utf-8').read() if os.path.exists(lang_path) else ''
    old = re.sub(r'\n?# --- carte Eldanior \(gen_monde\.py\) ---\n.*?# --- fin carte Eldanior ---\n', '\n', old, flags=re.S)
    block = '# --- carte Eldanior (gen_monde.py) ---\n' + ''.join(
        '%s = %s\n' % (k, v) for k, v in sorted(lang.items())) + '# --- fin carte Eldanior ---\n'
    with open(lang_path, 'w', encoding='utf-8') as f:
        f.write(old.rstrip('\n') + ('\n\n' if old.strip() else '') + block)
    print('monde écrit sous', root)

if __name__ == '__main__':
    main()
