#!/usr/bin/env python3
"""Outil des runes Eldanior : creer une rune a partir d'une fiche, verifier toutes les runes, chercher des assets.

    python3 scripts/runes/rune_tool.py new   scripts/runes/specs/Rune_X.json [--force] [--dry-run]
    python3 scripts/runes/rune_tool.py check [Rune_X ...]
    python3 scripts/runes/rune_tool.py assets particles|sounds|models|trails|effects|anims|interactions <regex|set>
    python3 scripts/runes/rune_tool.py rules
    python3 scripts/runes/rune_tool.py icons Rune_X [--emblem Puits] [--type Air --rarity Divin]

Regles (couts, recharges, loot, types) : scripts/runes/rune_rules.json. Fiches : scripts/runes/specs/.
Assets vanilla lus dans Assets.zip (HYTALE_ASSETS_ZIP pour changer le chemin).
"""
import argparse, json, os, re, sys, zipfile

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
RES = os.path.join(ROOT, 'src/main/resources')
JAVA = os.path.join(ROOT, 'src/main/java/com/eldanior/system')
SPELLS = os.path.join(JAVA, 'skills/runes/spells')
RUNE_MANAGER = os.path.join(JAVA, 'skills/runes/RuneManager.java')
LOOT = os.path.join(JAVA, 'config/configs/LootTableConfig.java')
INTERACTION_MANAGER = os.path.join(JAVA, 'skills/InteractionManager.java')
REGISTRY = os.path.join(SPELLS, 'RuneSpells.java')
ZIP = os.environ.get('HYTALE_ASSETS_ZIP', os.path.expanduser('~/Library/Application Support/Hytale/install/release/package/game/latest/Assets.zip'))
RULES = json.load(open(os.path.join(HERE, 'rune_rules.json'), encoding='utf-8'))

sys.path.insert(0, HERE)

# ============================================================ assets (mod + vanilla) ============================================================
_zip_names = None
def zip_names():
    global _zip_names
    if _zip_names is None:
        _zip_names = zipfile.ZipFile(ZIP).namelist() if os.path.exists(ZIP) else []
    return _zip_names

def mod_files():
    for d, _, fs in os.walk(RES):
        for f in fs: yield os.path.relpath(os.path.join(d, f), RES).replace(os.sep, '/')

_index = None
def index():
    """{(dossier racine, nom de base sans extension, extension): [chemins]} pour mod et vanilla."""
    global _index
    if _index is None:
        _index = {}
        for src, names in (('mod', list(mod_files())), ('vanilla', zip_names())):
            for n in names:
                if n.endswith('/'): continue
                base, ext = os.path.splitext(os.path.basename(n))
                _index.setdefault((ext, base), []).append((src, n))
    return _index

def find(ext, base, under=''):
    return [(s, p) for s, p in index().get((ext, base), []) if p.startswith(under)]

def exists_path(rel):
    """Chemin relatif a Common/ ou Server/ (ex. 'Icons/ItemsGenerated/X.png') : mod ou vanilla."""
    for prefix in ('Common/', 'Server/', ''):
        p = prefix + rel
        if os.path.exists(os.path.join(RES, p)) or p in set_zip(): return True
    return False

_zset = None
def set_zip():
    global _zset
    if _zset is None: _zset = set(zip_names())
    return _zset

def read_asset(src, path):
    if src == 'mod': return open(os.path.join(RES, path), encoding='utf-8-sig').read()
    return zipfile.ZipFile(ZIP).read(path).decode('utf-8-sig')

def load_json(src, path):
    try: return json.loads(read_asset(src, path))
    except Exception: return None

# ============================================================ noms ============================================================
def short(rune_id): return rune_id[5:] if rune_id.startswith('Rune_') else rune_id
def snake(name): return re.sub(r'(?<!^)(?=[A-Z])', '_', name).lower()
def rarity_of_quality(q):
    for k, v in RULES['rarities'].items():
        if v['quality'] == q: return k
    return None

# ============================================================ NEW ============================================================
def cost_of(spec):
    r = RULES['rarities'][spec['rarity']]
    ov = spec.get('overrides') or {}
    mana = ov.get('mana') if ov.get('mana') is not None else r['mana']
    cd = ov.get('cooldown') if ov.get('cooldown') is not None else r['cooldown']
    if (ov.get('mana') is not None or ov.get('cooldown') is not None) and not ov.get('reason'):
        sys.exit("ERREUR : overrides mana/cooldown sans 'reason' (toute exception doit etre justifiee).")
    return mana, cd

def build_item(spec, mana, cd):
    r = RULES['rarities'][spec['rarity']]; s = short(spec['id'])
    keywords = list(dict.fromkeys(RULES['types'][spec['type']]['keywords'] + spec.get('keywords', [])))
    desc = spec['description'].rstrip() + f" Coût : {mana} mana. Recharge : {cd} s."
    return {
        "TranslationProperties": {"Name": spec['name'], "Description": desc},
        "Icon": f"Icons/ItemsGenerated/{spec['id']}.png",
        "Quality": r['quality'], "ItemLevel": r['itemLevel'],
        "Categories": ["Items.Ingredients", "Eldanior_System.Competences_Actives"],
        "Model": "Items/Runes/Rune_Ability.blockymodel",
        "Texture": f"Items/Runes/Rune_Textures/Rune_Ability_{s}.png",
        "PlayerAnimationsId": "Item",
        "IconProperties": {"Scale": 0.75, "Rotation": [0, 30, 15], "Translation": [4, -18]},
        "MaxStack": 1, "DropOnDeath": False, "Scale": 0.8, "ItemSoundSetId": "ISS_Items_Gems",
        "Ability": {"Slot": "Core", "Cooldown": cd, "Cost": mana, "CostType": "Mana", "AbilityKeywords": keywords, "Cast": f"Root_Ability_{s}"},
    }

def page_title(name):
    """'Rune de l'Autorite X' -> 'Autorite X' ; 'Rune du Puits X' -> 'Puits X' ; 'Rune de Bourrasque' -> 'Bourrasque'."""
    for prefix in ("Rune de l'", "Rune de la ", "Rune du ", "Rune des ", "Rune de ", "Rune d'", "Rune "):
        if name.startswith(prefix): return name[len(prefix):]
    return name

def build_page(spec):
    r = RULES['rarities'][spec['rarity']]
    return {
        "Id": spec['page'],
        "TranslationProperties": {"Name": f"Parchemin de Rune : {page_title(spec['name'])}",
                                  "Description": spec.get('pageDescription') or (spec['description'] + " Utilisez-le pour ajouter la rune à votre sac de runes.")},
        "Icon": "Icons/ItemsGenerated/Recipe_Page.png", "Quality": r['quality'],
        "Categories": ["Eldanior_System.Competences_Actives"], "Tags": {"Type": ["Item", "Consumable"]},
        "MaxStack": 1, "PlayerAnimationsId": "Item",
        "Model": "Items/Consumables/Recipes/Recipe.blockymodel", "Texture": "Items/Consumables/Recipes/Recipe_Texture.png",
        "IconProperties": {"Scale": 0.76, "Rotation": [135, 135, 0], "Translation": [-1, 5]},
        "Interactions": {"Use": "Root_ConsumableItemSkillInteraction"},
    }

def build_cast(spec):
    c = {"animationSet": "Staff", "charging": "CastSummonCharging", "release": "CastSummon", "chargeTime": 0.4, "releaseTime": 0.4,
         "moveSpeed": 0.5, "cameraEffect": "Ability_Cast_Generic", "sound": None, "releaseSound": None, "handParticles": None,
         "handParticlesScale": 0.6, "costMoment": "start"}
    c.update(spec.get('cast') or {})
    charge = {"Type": "Simple", "RunTime": c['chargeTime'], "HorizontalSpeedMultiplier": c['moveSpeed'],
              "Effects": {"ItemPlayerAnimationsId": c['animationSet'], "ItemAnimationId": c['charging'], "WaitForAnimationToFinish": False}}
    if c['cameraEffect']: charge['Effects']['CameraEffect'] = c['cameraEffect']
    if c['sound']: charge['Effects']['WorldSoundEventId'] = c['sound']
    if c['handParticles']:
        charge['Effects']['Particles'] = [{"SystemId": c['handParticles'], "TargetEntityPart": "Entity", "TargetNodeName": hand, "Scale": c['handParticlesScale']}
                                          for hand in ("R-Hand", "L-Hand")]
    spell = {"Type": "RuneSpell", "RunTime": 0.05, "Spell": spec['spell']}
    release = {"Type": "Simple", "RunTime": c['releaseTime'], "HorizontalSpeedMultiplier": min(1.0, c['moveSpeed'] + 0.2),
               "Effects": {"ItemPlayerAnimationsId": c['animationSet'], "ItemAnimationId": c['release'], "ClearAnimationOnFinish": True}}
    if c['releaseSound']: release['Effects']['WorldSoundEventId'] = c['releaseSound']
    cooldown = {"Type": "TriggerCooldown", "UseAbilityCooldown": True}
    stat = {"Type": "ChangeStat", "UseAbilityCost": True}
    if c['costMoment'] == 'release':      # mana et recharge seulement si l'incantation va au bout
        branches = [{"Interactions": [charge, stat, cooldown, spell, release]}]
    else:                                  # mana et recharge des le debut (comportement des sorts divins actuels)
        branches = [{"Interactions": [charge, spell, release]}, {"Interactions": [cooldown]}, {"Interactions": [stat]}]
    return {"$Comment": f"{spec['name']} : incantation puis sort Java '{spec['spell']}' (RuneSpell). Genere par rune_tool.py, cout : {c['costMoment']}.",
            "Type": "StatsCondition", "UseAbilityCost": True, "Failed": "Ability_Cast_Fail",
            "Next": {"Type": "Parallel", "Interactions": branches}}

JAVA_INSTANT = '''package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * {name} ({type}, {rarity}) : {summary}
 * Fiche de conception : scripts/runes/specs/{id}.json
 */
public final class {cls} implements RuneSpell {{

    // ===== Reglages (equilibrage) =====

    // ===== Assets =====

    @Override public String id() {{ return "{spell}"; }}

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {{
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        // TODO : effet principal (SpellFx.enemiesAround, damage, push, fx, sound...)
        return null;
    }}
}}
'''

JAVA_PERSISTENT = '''package com.eldanior.system.skills.runes.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/**
 * {name} ({type}, {rarity}) : {summary}
 * Fiche de conception : scripts/runes/specs/{id}.json
 */
public final class {cls} implements RuneSpell {{

    // ===== Reglages (equilibrage) =====
    private static final long DURATION_MS = 10_000;

    // ===== Assets =====

    @Override public String id() {{ return "{spell}"; }}

    @Override
    public Instance start(Ref<EntityStore> caster, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {{
        Vector3d pos = SpellFx.position(caster, store);
        if (pos == null) return null;
        return new Active(pos, System.currentTimeMillis());
    }}

    private static final class Active implements Instance {{
        final Vector3d origin;
        final long startAt;
        Active(Vector3d origin, long startAt) {{ this.origin = origin; this.startAt = startAt; }}

        @Override
        public boolean tick(Ref<EntityStore> caster, long now, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {{
            if (now - startAt >= DURATION_MS) return true;
            // TODO : effet a chaque tick (limiter la frequence : un champ lastX et un intervalle)
            return false;
        }}

        @Override
        public void cleanup(CommandBuffer<EntityStore> cb) {{
            // TODO : retirer les entites temporaires (SpellFx.remove), restaurer le terrain si besoin
        }}
    }}
}}
'''

REGISTRY_SRC = '''package com.eldanior.system.skills.runes.spells;

/** Sorts de rune branches sur le moteur commun. Une ligne par sort (rune_tool.py new l'ajoute). */
public final class RuneSpells {

    private RuneSpells() {}

    public static void registerAll() {
        // <rune-tool:register>
    }
}
'''

def write_json(path, data, force, dry, created):
    full = os.path.join(RES, path)
    if os.path.exists(full) and not force:
        print(f"  = existe deja : {path}"); return
    created.append(path)
    if dry: return
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w', encoding='utf-8') as f: json.dump(data, f, ensure_ascii=False, indent=2); f.write('\n')

def insert_map_entry(src, map_name, entry_line, key):
    if f'"{key}"' in src[src.index(map_name):src.index('\n    );', src.index(map_name))]: return src, False
    start = src.index(map_name); end = src.index('\n    );', start)
    return src[:end] + ',\n            ' + entry_line + src[end:], True

def insert_loot(src, table, page, chance):
    m = re.search(r'\n    ' + table + r'\("', src)
    if not m: print(f"  ! table de loot introuvable : {table}"); return src, False
    nxt = re.search(r'\n    [A-Z_]+\("', src[m.end():])
    block_end = m.end() + (nxt.start() if nxt else len(src))
    if f'"{page}"' in src[m.start():block_end]: return src, False
    line_end = src.index('\n', m.end())
    return src[:line_end + 1] + f'            new LootEntry("{page}", 1, 1, 1, {chance}),\n' + src[line_end + 1:], True

def cmd_new(a):
    spec = json.load(open(a.spec, encoding='utf-8'))
    for k in ('id', 'spell', 'javaClass', 'name', 'type', 'rarity', 'classes', 'description'):
        if not spec.get(k): sys.exit(f"ERREUR : champ obligatoire manquant dans la fiche : {k}")
    if spec['type'] not in RULES['types']: sys.exit(f"ERREUR : type inconnu {spec['type']} (types : {', '.join(RULES['types'])})")
    if spec['rarity'] not in RULES['rarities']: sys.exit(f"ERREUR : rarete inconnue {spec['rarity']} (raretes : {', '.join(RULES['rarities'])})")
    s = short(spec['id']); spec.setdefault('page', 'rune_page_' + snake(s))
    mana, cd = cost_of(spec)
    created = []
    print(f"== {spec['id']} ({spec['type']}, {spec['rarity']}) : {mana} mana, {cd} s ==")
    write_json(f"Server/Item/Items/Rune/Ability/{spec['id']}.json", build_item(spec, mana, cd), a.force, a.dry_run, created)
    write_json(f"Server/Item/Items/Rune/Parchemin/{spec['page']}.json", build_page(spec), a.force, a.dry_run, created)
    write_json(f"Server/Item/RootInteractions/Abilities/Root_Ability_{s}.json", {"Interactions": [f"Ability_{s}_Cast"], "RequireNewClick": True}, a.force, a.dry_run, created)
    write_json(f"Server/Item/Interactions/Abilities/{s}/Ability_{s}_Cast.json", build_cast(spec), a.force, a.dry_run, created)

    icons = [f"Common/Icons/Abilities/{spec['id']}.png", f"Common/Icons/ItemsGenerated/{spec['id']}.png", f"Common/Items/Runes/Rune_Textures/Rune_Ability_{s}.png"]
    if a.force or not all(os.path.exists(os.path.join(RES, p)) for p in icons):
        created += icons
        if not a.dry_run:
            import rune_icons
            rune_icons.generate(spec['type'], RULES['rarities'][spec['rarity']], RULES['types'][spec['type']], ZIP,
                                *(os.path.join(RES, p) for p in icons), seed=len(spec['id']), emblem=spec.get('emblem'))
    else: print("  = icones deja presentes")

    java = os.path.join(SPELLS, spec['javaClass'] + '.java')
    if not os.path.exists(java) or a.force:
        tpl = JAVA_PERSISTENT if spec.get('persistent') else JAVA_INSTANT
        src = tpl.format(name=spec['name'], type=spec['type'], rarity=spec['rarity'], summary=spec.get('summary', spec['description'][:120]),
                         id=spec['id'], cls=spec['javaClass'], spell=spec['spell'])
        created.append(os.path.relpath(java, ROOT))
        if not a.dry_run: open(java, 'w', encoding='utf-8').write(src)
    else: print(f"  = classe Java deja presente : {spec['javaClass']}.java")

    edits = []
    if not os.path.exists(REGISTRY) and not a.dry_run: open(REGISTRY, 'w', encoding='utf-8').write(REGISTRY_SRC)
    reg = open(REGISTRY, encoding='utf-8').read() if os.path.exists(REGISTRY) else REGISTRY_SRC
    if f'new {spec["javaClass"]}()' not in reg:
        reg = reg.replace('        // <rune-tool:register>', f'        RuneSpellRuntime.register(new {spec["javaClass"]}());\n        // <rune-tool:register>')
        edits.append('RuneSpells.registerAll')
        if not a.dry_run: open(REGISTRY, 'w', encoding='utf-8').write(reg)
    rm = open(RUNE_MANAGER, encoding='utf-8').read()
    rm, e1 = insert_map_entry(rm, 'RUNE_CLASSES = Map.ofEntries(', f'Map.entry("{spec["id"]}", "{spec["classes"]}")', spec['id'])
    rm, e2 = insert_map_entry(rm, 'PAGE_TO_RUNE = Map.ofEntries(', f'Map.entry("{spec["page"]}", "{spec["id"]}")', spec['page'])
    if e1: edits.append('RuneManager.RUNE_CLASSES')
    if e2: edits.append('RuneManager.PAGE_TO_RUNE')
    if (e1 or e2) and not a.dry_run: open(RUNE_MANAGER, 'w', encoding='utf-8').write(rm)
    loot = open(LOOT, encoding='utf-8').read()
    loot_rules = spec.get('loot') or RULES['rarities'][spec['rarity']]['loot']
    for table, chance in loot_rules.items():
        loot, e = insert_loot(loot, table, spec['page'], chance)
        if e: edits.append(f'LootTableConfig.{table} ({chance} %)')
    if not a.dry_run: open(LOOT, 'w', encoding='utf-8').write(loot)

    print("  fichiers crees :" if created else "  aucun fichier cree")
    for p in created: print("   +", p)
    for e in edits: print("   ~", e)
    print("Suite : coder l'effet dans", spec['javaClass'] + '.java', "puis `./gradlew compileJava` et `rune_tool.py check", spec['id'] + '`.')

# ============================================================ ICONS ============================================================
def cmd_icons(a):
    """Regenere seulement les 3 images d'une rune (fiche, ou --type/--rarity pour une rune sans fiche)."""
    import rune_icons
    spec_path = a.target if a.target.endswith('.json') else os.path.join(HERE, 'specs', a.target + '.json')
    spec = json.load(open(spec_path, encoding='utf-8')) if os.path.exists(spec_path) else {"id": a.target}
    rid = spec['id']; t = a.type or spec.get('type'); r = a.rarity or spec.get('rarity'); emblem = a.emblem or spec.get('emblem')
    if not t or not r: sys.exit("ERREUR : type et rarete requis (fiche, ou --type et --rarity)")
    s = short(rid)
    paths = [f"Common/Icons/Abilities/{rid}.png", f"Common/Icons/ItemsGenerated/{rid}.png", f"Common/Items/Runes/Rune_Textures/Rune_Ability_{s}.png"]
    rune_icons.generate(t, RULES['rarities'][r], RULES['types'][t], ZIP, *(os.path.join(RES, p) for p in paths), seed=len(rid), emblem=emblem)
    for p in paths: print("  ~", p)

# ============================================================ CHECK ============================================================
class Report:
    def __init__(self): self.errors = self.warns = 0
    def err(self, who, msg): self.errors += 1; print(f"  [ERREUR] {who} : {msg}")
    def warn(self, who, msg): self.warns += 1; print(f"  [ATTENTION] {who} : {msg}")
    def info(self, who, msg): print(f"  [info] {who} : {msg}")

_vanilla_types = None
def vanilla_interaction_types():
    global _vanilla_types
    if _vanilla_types is None:
        _vanilla_types = set()
        z = zipfile.ZipFile(ZIP) if os.path.exists(ZIP) else None
        if z:
            for n in z.namelist():
                if n.startswith('Server/Item/Interactions/') and n.endswith('.json'):
                    _vanilla_types.update(re.findall(r'"Type"\s*:\s*"([A-Za-z_]+)"', z.read(n).decode('utf-8', 'ignore')))
    return _vanilla_types

def walk(o, key):
    if isinstance(o, dict):
        for k, v in o.items():
            if k == key and isinstance(v, str): yield v, o
            yield from walk(v, key)
    elif isinstance(o, list):
        for v in o: yield from walk(v, key)

def particle_has_lifespan(src, path):
    d = load_json(src, path)
    return d is not None and 'LifeSpan' in d

def particle_is_finite(src, path):
    """Un systeme se termine seul s'il a un LifeSpan, ou si tous ses spawners emettent en rafale (TotalParticles / SpawnBurst)."""
    d = load_json(src, path)
    if d is None: return True
    if 'LifeSpan' in d: return True
    for sp in d.get('Spawners', []):
        hits = find('.particlespawner', sp.get('SpawnerId', '-'))
        if not hits: continue
        sd = load_json(*hits[0]) or {}
        total = (sd.get('TotalParticles') or {}).get('Max', 0) if isinstance(sd.get('TotalParticles'), dict) else sd.get('TotalParticles')
        if not ((total and total > 0) or sd.get('SpawnBurst')): return False          # TotalParticles -1 = infini
    return True

def check_interaction_file(rep, who, src, path, java_src, registered_types, seen):
    if path in seen: return
    seen.add(path)
    d = load_json(src, path)
    if d is None: rep.err(who, f"JSON invalide : {path}"); return
    for t, node in walk(d, 'Type'):
        if t in vanilla_interaction_types() or t in registered_types: pass
        else: rep.err(who, f"type d'interaction inconnu '{t}' ({path}) : ni vanilla ni enregistre dans InteractionManager")
        if t == 'RuneSpell':
            spell = node.get('Spell')
            m = re.search(r'class\s+(\w+)\s+implements\s+RuneSpell\b[\s\S]*?return\s+"' + re.escape(str(spell)) + '"', java_src)
            if not m: rep.err(who, f"RuneSpell '{spell}' : aucune classe RuneSpell ne renvoie cet id")
            elif f'new {m.group(1)}()' not in (open(REGISTRY, encoding='utf-8').read() if os.path.exists(REGISTRY) else ''):
                rep.err(who, f"RuneSpell '{spell}' : {m.group(1)} n'est pas enregistre dans RuneSpells.registerAll")
    for sid, _ in walk(d, 'SystemId'):
        if not find('.particlesystem', sid): rep.err(who, f"systeme de particules introuvable : {sid}")
    for key in ('WorldSoundEventId', 'SoundEventId', 'LocalSoundEventId'):
        for sid, _ in walk(d, key):
            if not find('.json', sid, 'Server/Audio/SoundEvents/'): rep.err(who, f"son introuvable : {sid}")
    for aid, node in walk(d, 'ItemAnimationId'):
        aset = node.get('ItemPlayerAnimationsId')
        if aset:
            hits = find('.json', aset, 'Server/Item/Animations/')
            anims = set()
            for s, p in hits: anims |= set(((load_json(s, p) or {}).get('Animations') or {}).keys())
            if hits and aid not in anims: rep.warn(who, f"animation '{aid}' absente du jeu d'animations '{aset}'")
    for ref, _ in walk(d, 'Next'):
        for s, p in find('.json', ref, 'Server/Item/Interactions/'): check_interaction_file(rep, who, s, p, java_src, registered_types, seen)
    for lst in (v for v, _ in walk(d, 'Interactions') if isinstance(v, str)):
        for s, p in find('.json', lst, 'Server/Item/Interactions/'): check_interaction_file(rep, who, s, p, java_src, registered_types, seen)

def cmd_check(a):
    rep = Report()
    java_src = ''
    for d, _, fs in os.walk(SPELLS):
        for f in fs:
            if f.endswith('.java'): java_src += open(os.path.join(d, f), encoding='utf-8').read() + '\n'
    registered_types = set(re.findall(r'\.register\(\s*"(\w+)"', open(INTERACTION_MANAGER, encoding='utf-8').read()))
    rm = open(RUNE_MANAGER, encoding='utf-8').read()
    classes = dict(re.findall(r'Map\.entry\("(\w+)",\s*"([^"]*)"\)', rm[rm.index('RUNE_CLASSES'):rm.index('PAGE_TO_RUNE')]))
    pages = dict(re.findall(r'Map\.entry\("(\w+)",\s*"(\w+)"\)', rm[rm.index('PAGE_TO_RUNE'):rm.index('private static final Set')]))
    loot = open(LOOT, encoding='utf-8').read()
    qualities = {os.path.splitext(os.path.basename(p))[0] for _, p in sum((v for k, v in index().items() if k[0] == '.json'), []) if '/Item/Qualities/' in p}

    rune_dir = os.path.join(RES, 'Server/Item/Items/Rune/Ability')
    runes = sorted(f[:-5] for f in os.listdir(rune_dir) if f.endswith('.json'))
    if a.runes: runes = [r for r in runes if r in a.runes]
    for rid in runes:
        print(f"-- {rid}")
        item = load_json('mod', f'Server/Item/Items/Rune/Ability/{rid}.json')
        if item is None: rep.err(rid, "JSON invalide"); continue
        ab = item.get('Ability') or {}
        if item.get('Quality') not in qualities: rep.err(rid, f"qualite inconnue {item.get('Quality')}")
        for k in ('Icon', 'Texture', 'Model'):
            if item.get(k) and not exists_path(item[k]): rep.err(rid, f"{k} introuvable : {item[k]}")
        if not exists_path(f"Icons/Abilities/{rid}.png"): rep.warn(rid, "icone de competence (Icons/Abilities) absente")
        cast = ab.get('Cast')
        roots = find('.json', cast or '-', 'Server/Item/RootInteractions/')
        if not roots: rep.err(rid, f"RootInteraction introuvable : {cast}")
        for s, p in roots:
            root = load_json(s, p) or {}
            for ref in root.get('Interactions', []):
                hits = find('.json', ref, 'Server/Item/Interactions/') if isinstance(ref, str) else []
                if isinstance(ref, str) and not hits: rep.err(rid, f"interaction introuvable : {ref}")
                for hs, hp in hits: check_interaction_file(rep, rid, hs, hp, java_src, registered_types, set())
        if rid not in classes and rid != 'Rune_Aura': rep.warn(rid, "absente de RuneManager.RUNE_CLASSES (utilisable par toutes les classes)")
        my_pages = [p for p, r in pages.items() if r == rid]
        if not my_pages and rid != 'Rune_Aura': rep.warn(rid, "aucun parchemin ne l'enseigne (PAGE_TO_RUNE)")
        for p in my_pages:
            if not os.path.exists(os.path.join(RES, f'Server/Item/Items/Rune/Parchemin/{p}.json')): rep.err(rid, f"parchemin {p} reference mais fichier absent")
            elif f'"{p}"' not in loot: rep.warn(rid, f"parchemin {p} absent des tables de loot")
        rarity = rarity_of_quality(item.get('Quality'))
        if rarity:
            r = RULES['rarities'][rarity]
            if (ab.get('Cost'), ab.get('Cooldown')) != (r['mana'], r['cooldown']):
                why = RULES['exceptions'].get(rid)
                spec_path = os.path.join(HERE, 'specs', rid + '.json')
                if not why and os.path.exists(spec_path): why = ((json.load(open(spec_path, encoding='utf-8')).get('overrides') or {}).get('reason'))
                msg = f"cout/recharge {ab.get('Cost')} / {ab.get('Cooldown')} s au lieu de {r['mana']} / {r['cooldown']} s ({rarity})"
                (rep.info if why else rep.warn)(rid, msg + (f" - exception : {why}" if why else " - exception non documentee (rune_rules.json > exceptions)"))

    print("-- Effets emis par le code (SpellFx.fx) : systemes sans LifeSpan = boucle infinie")
    for sid in sorted(set(re.findall(r'(?<![A-Z])FX_[A-Z0-9_]+\s*=\s*"([^"]+)"', java_src))):
        hits = find('.particlesystem', sid)
        if not hits: rep.err('Java', f"systeme de particules introuvable : {sid}")
        elif not any(particle_is_finite(s, p) for s, p in hits): rep.warn('Java', f"{sid} : ni LifeSpan ni spawners en rafale, emis par SpellFx.fx il boucle a l'infini (faire une copie du mod avec LifeSpan)")
    for sid in sorted(set(re.findall(r'SFX_[A-Z0-9_]+\s*=\s*"([^"]+)"', java_src))):
        if not find('.json', sid, 'Server/Audio/SoundEvents/'): rep.err('Java', f"son introuvable : {sid}")

    print("-- Modeles du mod (Server/Models)")
    for p in [p for p in mod_files() if p.startswith('Server/Models/') and p.endswith('.json')]:
        d = load_json('mod', p) or {}
        mp = d.get('Model')
        if mp and not re.match(r'(Characters|NPC|Items|VFX)/', mp): rep.err(p, f"chemin de modele interdit '{mp}' (doit etre sous Characters/, NPC/, Items/ ou VFX/ : sinon echec des assets)")
        for k in ('Model', 'Texture'):
            if d.get(k) and not exists_path(d[k]): rep.err(p, f"{k} introuvable : {d[k]}")
        for tid, _ in walk(d, 'TrailId'):
            if not find('.json', tid, 'Server/Entity/Trails/'): rep.err(p, f"trail introuvable : {tid}")
        for sid, _ in walk(d, 'SystemId'):
            if not find('.particlesystem', sid): rep.err(p, f"systeme de particules introuvable : {sid}")

    print(f"\n== {rep.errors} erreur(s), {rep.warns} avertissement(s) ==")
    sys.exit(1 if rep.errors else 0)

# ============================================================ ASSETS ============================================================
KINDS = {'particles': ('.particlesystem', ''), 'sounds': ('.json', 'Server/Audio/SoundEvents/'), 'models': ('.json', 'Server/Models/'),
         'trails': ('.json', 'Server/Entity/Trails/'), 'effects': ('.json', 'Server/Entity/Effects/'),
         'interactions': ('.json', 'Server/Item/Interactions/'), 'camera': ('.json', 'Server/Camera/'),
         'damage': ('.json', 'Server/Entity/Damage/'), 'blocks': ('.json', 'Server/Item/Items/'), 'beams': ('.json', 'Server/Entity/Beams/')}

def cmd_assets(a):
    if a.kind == 'anims':
        for s, p in find('.json', a.pattern, 'Server/Item/Animations/'):
            print(f"[{s}] {a.pattern} :", ', '.join(sorted(((load_json(s, p) or {}).get('Animations') or {}).keys())))
        return
    ext, under = KINDS[a.kind]
    rx = re.compile(a.pattern, re.I); n = 0
    for (e, base), hits in sorted(index().items()):
        if e != ext or not rx.search(base): continue
        for s, p in hits:
            if p.startswith(under):
                extra = ''
                if a.kind == 'particles': extra = '' if particle_is_finite(s, p) else '  (boucle : ne pas emettre par SpellFx.fx)'
                print(f"[{s}] {base}  ->  {p}{extra}"); n += 1
    print(f"{n} resultat(s)")

def cmd_rules(_):
    print("Rarete      Qualite    Mana  Recharge  Loot")
    for k, v in RULES['rarities'].items(): print(f"{k:<11} {v['quality']:<10} {v['mana']:>5} {v['cooldown']:>6} s  {v['loot']}")
    print("Types :", ', '.join(RULES['types']))

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = ap.add_subparsers(dest='cmd', required=True)
    n = sub.add_parser('new'); n.add_argument('spec'); n.add_argument('--force', action='store_true'); n.add_argument('--dry-run', action='store_true'); n.set_defaults(fn=cmd_new)
    c = sub.add_parser('check'); c.add_argument('runes', nargs='*'); c.set_defaults(fn=cmd_check)
    s = sub.add_parser('assets'); s.add_argument('kind', choices=list(KINDS) + ['anims']); s.add_argument('pattern'); s.set_defaults(fn=cmd_assets)
    r = sub.add_parser('rules'); r.set_defaults(fn=cmd_rules)
    i = sub.add_parser('icons'); i.add_argument('target', help='fiche .json ou id de rune'); i.add_argument('--type'); i.add_argument('--rarity'); i.add_argument('--emblem'); i.set_defaults(fn=cmd_icons)
    a = ap.parse_args(); a.fn(a)

if __name__ == '__main__':
    main()
