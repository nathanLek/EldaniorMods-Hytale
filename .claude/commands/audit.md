---
name: audit
description: "Audit complet du codebase : analyse tous les systemes, trouve les bugs/failles/perf, cree les tickets Linear automatiquement. Usage: /audit [systeme] ou /audit (tout)"
user_invocable: true
---

# Skill: Audit Codebase + Creation Tickets

Analyse le codebase en profondeur, identifie tous les problemes, et cree des tickets Linear automatiquement.

## Parametres
- `/audit` → audit complet de TOUS les systemes (lance les agents en parallele)
- `/audit trade` → audit d'un systeme specifique (trade, duel, guild, quest, territory, skills, economy, gui, persistence, perf)

## IDs des statuts Linear (TOUJOURS utiliser ces IDs)
- **Backlog** : `a585414d-7b88-4052-8384-8bcd3e9dbb5d`  ← les tickets crees par l'audit vont ICI
- **Todo** : `823f2630-1b0b-4825-983f-40c07b99d052`
- **In Progress** : `9832c40d-7386-4417-8acb-d934272c6798`
- **In Review** : `468e6805-5187-45db-badb-e5218297a4ff`
- **Done** : `838f9ae4-180d-44a6-ad7b-758eef8cc203`

> **Regle** : tout nouveau ticket est cree en **Backlog**, jamais directement en Todo. Le passage Backlog -> Todo se fait au grooming (routine de 12h ou `/triage`).

## Team et Projet Linear
- Team UUID : `a6f0f1f0-d63a-445d-ac0f-ced74383d453`
- Projet : `Eldanior RPG Mod`
- Milestones :
  - M1 — Bugs critiques & Stabilite (bugs bloquants, securite, crash)
  - M2 — Gameplay loops complets (features incompletes, UX, resources)
  - M4 — Polish & Admin (ameliorations, optimisations, admin)

---

## PHASE 1 — Lancer les audits (en parallele)

Lancer des agents specialises en parallele selon le scope demande. Chaque agent doit :
- Lire CHAQUE fichier du systeme concerne
- Identifier les bugs avec fichier:ligne
- Classer par severite (CRITIQUE / HAUTE / MOYENNE / BASSE)
- Proposer un fix pour chaque probleme

### Si `/audit` (complet) — lancer TOUS ces agents en parallele :

1. **Agent securite** (agent pierre) : race conditions, permissions manquantes, exploits de duplication, injections, overflow/underflow
2. **Agent persistence** (agent lucas) : donnees non persistees, ecritures non atomiques, fuites memoire, corruption, backup
3. **Agent performance** (agent pierre) : boucles O(n2), allocations dans tick loops, reflection, timers, collections non bornees
4. **Agent economie** (agent manon) : exploits monnaie infinie, arbitrage prix, formules cassees, caps non respectes
5. **Agent GUI/commandes** (agent hugo) : etat static partage, boutons morts, parseInt sans try-catch, permissions, refresh manquants
6. **Agent resources** (agent Explore) : JSON invalides, references cassees, particules manquantes, traductions manquantes

### Si `/audit <systeme>` — lancer UN agent cible sur le systeme demande

Pour chaque systeme, l'agent doit lire tous les fichiers concernes :
- `trade` : TradeCommand, TradeManager, TradeSession, TradeScreen, EchangesTab
- `duel` : DuelCommand, DuelManager, DuelProtectionSystem, DuelTab
- `guild` : GuildManager, GuildCommand, GuildeTab, Guild
- `quest` : QuestManager, QuestTab, QuestHudUpdateSystem, NpcQuestDetectionSystem
- `territory` : ParcelManager, ParcelCommand, ParcelRangeSystem, TerritoiresTab, ProprietesTab
- `skills` : SkillManager, PassiveSkill, CombatStatsSystem, CompetencesTab
- `economy` : tous les Shop JSON, ShopTab, BlackMarketTab, ParcelEconomyManager, CoinItemRegistry
- `gui` : SystemScreen, AdminScreen, tous les *Tab, CombinedHud
- `persistence` : PersistenceManager, PlayerLevelData (CODEC), tous les *Manager qui save
- `perf` : tous les *System ECS, PlayerPositionTracker, timers

---

## PHASE 2 — Collecter et dedupliquer les resultats

Une fois TOUS les agents termines :
1. Collecter tous les bugs trouves
2. Dedupliquer (meme fichier:ligne signale par plusieurs agents)
3. Verifier que le bug n'est pas deja couvert par un ticket existant :
   - Lister les issues existantes du projet via `mcp__linear__list_issues`
   - Comparer les titres et descriptions
   - Ne pas creer de doublon

---

## PHASE 3 — Creer les tickets Linear

Pour chaque nouveau bug identifie, creer un ticket Linear :

### Format du titre
```
[CATEGORIE] Description courte du probleme
```
Categories : SECU, PERSIST, PERF, ECO, GUI, CMD, TRADE, DUEL, GUILD, QUEST, SKILLS, TERRITORY, RESOURCES

### Priorite
- CRITIQUE → priority: 1 (Urgent)
- HAUTE → priority: 2 (High)
- MOYENNE → priority: 3 (Medium)
- BASSE → priority: 4 (Low)

### Milestone
- Bugs securite, crash, corruption, exploits → M1
- Features incompletes, UX, resources manquantes → M2
- Ameliorations, optimisations, polish → M4

### Labels (un seul label "Systeme" par ticket + Bug/Feature/Improvement)
Utiliser les labels existants : Combat, Skills, Territoires, Economie, Quetes, Social, GUI/HUD, Persistence, Effets/Particules

### Format de la description
```
**Fichier** : chemin/vers/fichier.java:ligne

**Probleme** : Description claire du bug, avec le code concerne si utile.

**Impact** : Ce qui se passe en jeu a cause de ce bug.

**Fix propose** : Description du correctif a appliquer.
```

### Appel Linear
```
mcp__linear__save_issue(
  title: "[CAT] description",
  team: "a6f0f1f0-d63a-445d-ac0f-ced74383d453",
  project: "Eldanior RPG Mod",
  milestone: "M1 — Bugs critiques & Stabilité",
  priority: 1,
  labels: ["Bug", "Combat"],
  state: "a585414d-7b88-4052-8384-8bcd3e9dbb5d",
  description: "..."
)
```

---

## PHASE 4 — Rapport final

Afficher un tableau recapitulatif :

```
## Audit termine

| # | Ticket | Titre | Priorite | Milestone |
|---|--------|-------|----------|-----------|
| 1 | ELD-XX | [SECU] ... | Urgent | M1 |
| 2 | ELD-XX | [PERF] ... | High | M1 |
| ... |

Total : X nouveaux tickets crees, Y doublons ignores
Par severite : X Urgent, X High, X Medium, X Low
```

---

## Regles strictes
- NE PAS creer de tickets en doublon (toujours verifier les existants)
- Chaque bug doit avoir un fichier:ligne precis
- Un ticket = un probleme (pas de mega-tickets fourre-tout)
- Grouper les problemes tres similaires dans le meme fichier (ex: 3 race conditions dans TradeSession = 1 ticket)
- NE PAS creer de tickets pour du code mort ou du style — uniquement bugs, securite, perf, features cassees
- Limiter a 1 label "Systeme" par ticket (contrainte Linear: un seul label par groupe)
