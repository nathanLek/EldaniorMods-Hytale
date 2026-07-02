---
name: triage
description: "Triage quotidien des tickets Linear : deduplique, priorise selon les milestones, signale les tickets bloques/perimes. Usage: /triage"
user_invocable: true
---

# Skill: Triage des tickets Linear

Passe en revue les tickets ouverts, met de l'ordre, et signale ce qui coince. Deterministe et rapide — pas de modification de code.

## IDs des statuts Linear
- **Backlog** : `a585414d-7b88-4052-8384-8bcd3e9dbb5d` (Eldanior) / `f102b6d5-797d-4e53-820b-2568837e1797` (BUGS)
- **Todo** : `823f2630-1b0b-4825-983f-40c07b99d052`
- **In Progress** : `9832c40d-7386-4417-8acb-d934272c6798`
- **In Review** : `468e6805-5187-45db-badb-e5218297a4ff`
- **Done** : `838f9ae4-180d-44a6-ad7b-758eef8cc203`

## Teams
- **BUGS** : `3e1552e4-4b55-4ffc-8ab9-90300909cf96`
- **Eldanior** : `a6f0f1f0-d63a-445d-ac0f-ced74383d453`

## Milestones (ordre de priorite)
1. **M1** — Bugs critiques & Stabilite (crash, securite, corruption, exploits)
2. **M2** — Gameplay loops complets (features incompletes, UX, resources)
3. **M4** — Polish & Admin (ameliorations, optimisations)

---

## Etapes

### 1 — Collecter
Lister les tickets Todo + In Progress + In Review des deux teams via `mcp__linear__list_issues`.

### 2 — Deduplication
Reperer les tickets qui decrivent le meme probleme (meme fichier:ligne, meme titre a peu pres). Pour chaque groupe de doublons : garder le plus complet, marquer les autres en doublon (`mcp__linear__save_issue` avec `duplicateOf`).

### 3 — Priorisation & ajustement
Verifier que chaque ticket a : une priorite coherente avec sa milestone (M1 → Urgent/High), un label Systeme, une milestone. **Corriger activement** les manques via `save_issue` (ajuster priorite, milestone, label). Regle : un ticket M1 sans priorite Urgent/High est suspect → reevaluer.

### 3bis — Creation de tickets manquants
Si l'analyse revele un travail implicite non couvert par un ticket (ex : un ticket "ajouter le sort X" implique de creer sa page de skill + sa traduction, mais aucun ticket ne le couvre), **creer le ticket de suivi en Backlog** (`a585414d-7b88-4052-8384-8bcd3e9dbb5d`), lie au ticket parent (`relatedTo`), avec priorite/milestone adaptees. De meme, quand un nouveau ticket est cree, reevaluer la priorite des tickets voisins du meme domaine pour garder la file coherente. NB : la promotion Backlog -> Todo est faite par la routine de grooming de 12h, pas ici.

### 4 — Tickets bloques / perimes
- **Bloque** : In Progress depuis > 3 jours sans PR liee, ou avec une relation `blockedBy` non resolue → le signaler.
- **Perime** : Todo qui reference un fichier/ligne qui n'existe plus (verifier via `.claude/index/classes.txt`) → proposer de fermer.

### 5 — Rapport
Afficher un tableau : tickets dedupliques, champs corriges, tickets bloques, tickets perimes. Puis une recommandation : "Prochain batch conseille pour `/ticket` : ELD-XX, ELD-YY (domaines distincts, M1)."

## Regles
- Ne JAMAIS supprimer un ticket ; au pire le passer en doublon ou proposer sa fermeture.
- Ne pas toucher au code. Ce skill ne fait que du triage Linear.
