---
name: ticket-done
description: "PR mergee : delete branch, Linear Done, pull main, enchaine le prochain ticket automatiquement. Usage: /ticket-done"
user_invocable: true
---

# Skill: Ticket Done + Enchainer

Le user a merge la PR. Tout enchainer automatiquement sans pause.

## IDs des statuts Linear (TOUJOURS utiliser ces IDs, jamais les noms)
- **Todo** : `823f2630-1b0b-4825-983f-40c07b99d052`
- **In Progress** : `9832c40d-7386-4417-8acb-d934272c6798`
- **In Review** : `468e6805-5187-45db-badb-e5218297a4ff`
- **Done** : `838f9ae4-180d-44a6-ad7b-758eef8cc203`

## Teams Linear
- **BUGS** : `3e1552e4-4b55-4ffc-8ab9-90300909cf96`
- **Eldanior** : `a6f0f1f0-d63a-445d-ac0f-ced74383d453`

## Etapes (executer dans l'ordre, sans pause)

### Etape 1 — Identifier le ticket en cours
- Branch actuelle : `git branch --show-current`
- Extraire BUGS-XX ou ELD-XX du nom de branch
- Si deja sur main : regarder le dernier merge commit

### Etape 2 — Checkout main & pull
```
git checkout main && git pull origin main
```

### Etape 3 — Supprimer la branch
```
git branch -d <branch>
git push origin --delete <branch>
```
Si la branch remote est deja supprimee (par GitHub), ignorer l'erreur.

### Etape 4 — Linear → Done
```
mcp__linear__save_issue(id: "BUGS-XX", state: "838f9ae4-180d-44a6-ad7b-758eef8cc203")
```

### Etape 5 — Confirmer et enchainer
- Afficher : "BUGS-XX termine. Enchainement du prochain ticket..."
- Executer immediatement la procedure complete du skill `/ticket` (auto-pick)

## Regles
- Ne pas attendre de confirmation entre les etapes
