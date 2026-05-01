# Localisation et Langue

#langue #traduction #i18n

## Fichier de langue
`Server/Languages/en-US/server.lang`

## Format
```
items.<itemId>.name=Nom de l'item
items.<itemId>.description=Description detaillee
```

## Contenu
- 300+ noms de parchemins de skills avec rarete et description
- Noms des pieces de monnaie
- Noms des consommables

## Formatage dans les descriptions
```
<b>Gras</b>
<i>Italique</i>
\n → Retour a la ligne
```

## Acces dans le code
```java
I18nModule.get().getMessage("en_us", key);
```

## Fichier cle
- `Server/Languages/en-US/server.lang`

## Liens
- [[Items/Consommables]] - Noms traduits
- [[Systems/Skills]] - 300+ descriptions de skills
