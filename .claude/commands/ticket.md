---
name: ticket
description: "Boucle automatisee : pick ticket, branch, fix, PR, merge, Linear Done, puis enchaine le suivant. Usage: /ticket [ELD-XX] ou /ticket (auto-pick)"
user_invocable: true
---

# Skill: Ticket Automatise (boucle complete)

Boucle complete automatisee : fix → PR → merge → cleanup → prochain ticket.
Executer TOUTES les etapes sans pause ni confirmation (sauf si fix > 5 fichiers).

## Parametres
- `/ticket` → auto-pick le prochain ticket Urgent/High en Todo (M1 d'abord, puis M2)
- `/ticket ELD-XX` → traiter ce ticket specifique

## IDs des statuts Linear (TOUJOURS utiliser ces IDs, jamais les noms)
- **Todo** : `823f2630-1b0b-4825-983f-40c07b99d052`
- **In Progress** : `9832c40d-7386-4417-8acb-d934272c6798`
- **In Review** : `468e6805-5187-45db-badb-e5218297a4ff`
- **Done** : `838f9ae4-180d-44a6-ad7b-758eef8cc203`

---

## PHASE 1 — Preparer

### Etape 1 — Checkout main & pull
```
git checkout main && git pull origin main
```

### Etape 2 — Choisir le ticket
- Si un ID est fourni : recuperer ce ticket via Linear
- Sinon : lister les issues Todo du projet "Eldanior RPG Mod", trier par priorite (Urgent > High > Medium), prendre la premiere
- Afficher le titre du ticket choisi

### Etape 3 — Creer la branch depuis main
```
git checkout -b fix/eld-XX-description-courte
```
Nom de branch : court, kebab-case, sans accents ni caracteres speciaux.

### Etape 4 — Linear → In Progress
```
mcp__linear__save_issue(id: "ELD-XX", state: "9832c40d-7386-4417-8acb-d934272c6798")
```

---

## PHASE 2 — Corriger

### Etape 5 — Analyser le ticket
- Lire la description du ticket pour identifier les fichiers et lignes concernes
- Lire CHAQUE fichier mentionne dans le ticket
- Comprendre le pattern existant dans le codebase
- Planifier le fix avant de coder

### Etape 6 — Corriger le code
- Appliquer le fix minimal et correct
- Verifier les imports manquants
- Ne toucher QUE les fichiers lies au ticket
- Suivre les patterns existants du codebase

---

## PHASE 3 — Publier

### Etape 7 — Linear → In Review
```
mcp__linear__save_issue(id: "ELD-XX", state: "468e6805-5187-45db-badb-e5218297a4ff")
```

### Etape 8 — Commit et push
- `git add` uniquement les fichiers modifies (JAMAIS `git add .`)
- Commit :
```
fix: description courte

Description detaillee.

Fixes ELD-XX

Co-Authored-By: Claude Opus 4.6 (1M context) <noreply@anthropic.com>
```
- `git push -u origin fix/eld-XX-description-courte`

### Etape 9 — Creer la PR
```
gh pr create --title "fix: description (ELD-XX)" --body "..." --base main
```
Body : Summary (bullet points), Test plan (checklist), Fixes ELD-XX

---

## PHASE 4 — Valider et merger

### Etape 10 — Verifier la PR
- Executer `gh pr checks <PR_NUMBER>` pour verifier les CI checks
- Executer `gh pr view <PR_NUMBER> --json mergeable` pour verifier les conflits
- Si **pas de conflit ET pas d'echec CI** → passer a l'etape 11
- Si **conflit ou CI fail** → STOP, afficher l'erreur au user et attendre ses instructions

### Etape 11 — Merger la PR
```
gh pr merge <PR_NUMBER> --merge --delete-branch
```

---

## PHASE 5 — Cleanup et enchainer

### Etape 12 — Checkout main & pull
```
git checkout main && git pull origin main
```

### Etape 13 — Supprimer la branch locale (si encore presente)
```
git branch -d fix/eld-XX-description-courte 2>/dev/null; true
```

### Etape 14 — Linear → Done
```
mcp__linear__save_issue(id: "ELD-XX", state: "838f9ae4-180d-44a6-ad7b-758eef8cc203")
```

### Etape 15 — Rapport et enchainement
Afficher un resume compact :
- Ticket : ELD-XX — titre — **DONE**
- PR : URL (merged)
- Fichiers modifies

Puis afficher : "Enchainement du prochain ticket..."
Et **recommencer immediatement a l'Etape 1** (auto-pick le prochain ticket Todo).

---

## Regles strictes
- NE PAS demander confirmation entre les etapes (sauf si > 5 fichiers a modifier)
- NE PAS modifier de fichiers hors scope du ticket
- Toujours verifier les imports
- STOP uniquement si : conflit de merge, CI fail, ou fix > 5 fichiers
- La boucle continue indefiniment tant qu'il y a des tickets Todo
- Team UUID Linear : `a6f0f1f0-d63a-445d-ac0f-ced74383d453`
- Projet Linear : `Eldanior RPG Mod`