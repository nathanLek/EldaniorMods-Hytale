---
name: ticket
description: "Lance plusieurs agents en parallele sur des tickets de domaines differents. Chaque agent travaille dans un worktree isole. Usage: /ticket [BUGS-XX] ou /ticket (multi-parallel)"
user_invocable: true
---

# Skill: Ticket Multi-Parallele

Lance **plusieurs agents simultanement**, chacun sur un ticket d'un domaine different.
Chaque agent travaille dans un **worktree git isole** → zero conflit.

## Parametres
- `/ticket` → mode parallele : pick 1 ticket par domaine different, lance tous les agents en meme temps
- `/ticket BUGS-XX` → traiter CE ticket specifique (mode solo)

## IDs des statuts Linear (TOUJOURS utiliser ces IDs, jamais les noms)
- **Todo** : `823f2630-1b0b-4825-983f-40c07b99d052`
- **In Progress** : `9832c40d-7386-4417-8acb-d934272c6798`
- **In Review** : `468e6805-5187-45db-badb-e5218297a4ff`
- **Done** : `838f9ae4-180d-44a6-ad7b-758eef8cc203`

## Teams Linear
- **BUGS** : `3e1552e4-4b55-4ffc-8ab9-90300909cf96`
- **Eldanior** : `a6f0f1f0-d63a-445d-ac0f-ced74383d453`

---

## ROUTING — Label → Agent

| Label | Agent (`subagent_type`) | Domaine fichiers |
|-------|------------------------|------------------|
| Combat | `pierre` | `duel/`, `Leveling/systems/` |
| Skills | `pierre` | `skills/` |
| Classes | `lea` | `classes/`, `definitions/` |
| GUI/HUD | `hugo` | `gui/`, `hud/` |
| Economie | `pierre` | `trade/`, `shop/`, `economy/` |
| Quetes | `theo` | `quest/`, `dialogue/` |
| Territoires | `pierre` | `territory/`, `parcel/` |
| Social | `pierre` | `social/`, `guild/`, `family/` |
| Persistence | `lucas` | `persistence/`, `config/Player/` |
| Effets/Particules | `nina` | `effects/`, particules |
| Commandes | `pierre` | `commands/` |
| Classements/Titres | `hugo` | `classement/`, `titles/` |
| Inventaire/Items | `pierre` | `inventory/`, `TreasureChest/` |
| Gameplay/Equilibrage | `manon` | `config/`, `Leveling/` |

---

## MODE PARALLELE (par defaut)

### Etape 1 — Lister les tickets Todo
```
mcp__linear__list_issues(team: "3e1552e4-4b55-4ffc-8ab9-90300909cf96", state: "823f2630-1b0b-4825-983f-40c07b99d052", limit: 50)
```

### Etape 2 — Selectionner 1 ticket par domaine
Parmi les tickets Todo, selectionner **le ticket le plus prioritaire de chaque domaine distinct** (basé sur le label non-"Bug").
Maximum 3-4 tickets en parallele pour ne pas surcharger.
Privilegier les domaines qui ne touchent PAS les memes fichiers.

Exemple de selection ideale :
- 1 ticket Combat (fichiers `duel/`, `Leveling/`)
- 1 ticket GUI/HUD (fichiers `gui/`, `hud/`)
- 1 ticket Quetes (fichiers `quest/`)
- 1 ticket Persistence (fichiers `persistence/`)

### Etape 3 — Passer tous les tickets en In Progress
Pour chaque ticket selectionne :
```
mcp__linear__save_issue(id: "BUGS-XX", state: "9832c40d-7386-4417-8acb-d934272c6798")
```

### Etape 4 — Lancer TOUS les agents en parallele
Envoyer UN SEUL message avec PLUSIEURS `Agent` tool calls.
Chaque agent utilise `isolation: "worktree"` et le bon `subagent_type`.

Le prompt de chaque agent doit contenir :
1. Le titre et la description COMPLETE du ticket
2. L'ID du ticket (BUGS-XX)
3. Les instructions :
   - **AVANT d'explorer** : consulter l'index `.claude/index/` (grep) pour localiser les classes/skills/cles au lieu de scanner le repo. Ex : `grep -i guildmanager .claude/index/classes.txt`
   - **Avant de CREER un skill/classe** : verifier qu'il n'existe pas deja (`grep -i "<nom>" .claude/index/skills-passives.txt`) pour eviter les doublons
   - Creer la branch `fix/bugs-XX-description-courte` depuis main
   - Lire CHAQUE fichier mentionne dans le ticket
   - Appliquer le fix minimal et correct
   - Verifier les imports
   - Ne toucher QUE les fichiers lies au ticket
   - Builder avec `./gradlew compileJava` pour verifier
   - Si le build echoue, corriger et re-builder
   - Lancer `./scripts/lint-eldanior.sh` : si un probleme HAUTE apparait a cause du fix, le corriger
   - **Definition of Done (obligatoire avant commit)** : (a) le code compile, (b) tout nouveau skill/classe est ENREGISTRE (enum PassiveSkill / ClassManager), (c) toute chaine visible a une cle de langue dans `server.lang`, (d) aucun stub/TODO laisse. Si un point hors-scope est decouvert, NE PAS le bricoler : le signaler dans le rapport pour creer un ticket de suivi.
   - `git add` uniquement les fichiers modifies (JAMAIS `git add .`)
   - Commit :
     ```
     fix: description courte

     Description detaillee.

     Fixes BUGS-XX

     Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>
     ```
   - `git push -u origin fix/bugs-XX-description-courte`

### Etape 5 — Attendre les resultats
Les agents tournent en parallele. Quand chacun finit :

### Etape 6 — Pour CHAQUE agent termine
1. **Revue qualite avant PR** : `/code-review low` sur le diff de la branche. Si une correction bloquante est trouvee, la faire appliquer par l'agent avant de continuer.
2. Linear → In Review
3. Creer la PR :
   ```
   gh pr create --title "fix: description (BUGS-XX)" --body "..." --base main --head fix/bugs-XX-description-courte
   ```
4. Verifier et merger :
   ```
   gh pr view <PR> --json mergeable
   gh pr merge <PR> --merge --delete-branch
   ```
5. Pull main :
   ```
   git pull origin main
   ```
6. **Doc** : si le fix change un comportement documente, noter le fichier `Brain IA/` a mettre a jour (traite par `/ticket-done` via l'agent camille).
7. **Tickets de suivi** : pour chaque point hors-scope signale par l'agent a l'Etape 4, creer un ticket Linear **en Backlog** (`a585414d-7b88-4052-8384-8bcd3e9dbb5d`) lie (team + milestone adaptes). La promotion en Todo se fait au grooming.
8. Linear → Done

### Etape 7 — Rapport final
Afficher un tableau recap :

| Ticket | Domaine | Agent | PR | Status |
|--------|---------|-------|----|--------|
| BUGS-XX | Combat | pierre | #N | DONE |
| BUGS-YY | GUI | hugo | #M | DONE |
| ... | ... | ... | ... | ... |

Puis : "Enchainement du prochain batch..."
Et **recommencer a l'Etape 1**.

---

## MODE SOLO (`/ticket BUGS-XX`)

Meme flow mais avec un seul ticket. Pas de selection multi-domaine.

---

## Regles strictes
- Maximum **4 agents en parallele** (au-dela, risque de conflits)
- Ne JAMAIS lancer 2 tickets du MEME domaine en parallele
- Toujours utiliser `isolation: "worktree"` pour les agents
- STOP si conflit de merge → resoudre avant de continuer
- Les PRs sont mergees **sequentiellement** (une a la fois) pour eviter les conflits
- Chaque PR doit etre mergee + main pulled avant la suivante
