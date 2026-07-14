# Pac-Man

Un jeu Pac-Man en Java (Swing) : menu de démarrage, IA des fantômes, sons, animation de la bouche, meilleur score sauvegardé.

## Jouer sans rien installer (recommandé)

1. Va sur la page des [Releases](https://github.com/POUKONE/pacman/releases/latest) et télécharge `Pac-Man-portable.zip`.
2. Dézippe le fichier où tu veux.
3. Double-clique sur `Jouer.bat`.

Ce package embarque son propre mini-runtime Java : **aucune installation de Java n'est nécessaire**.

## Jouer depuis ce dépôt (Java déjà installé)

Si tu as un JDK installé sur ta machine :

1. Clone ou télécharge ce dépôt.
2. Double-clique sur [`Jouer.bat`](Jouer.bat) à la racine du projet.

Si le jeu ne se lance pas (par exemple parce que le code source a changé), recompile-le d'abord :

1. Double-clique sur [`build.bat`](build.bat) — il recompile les sources et régénère `PacMan.jar`.
2. Puis double-clique sur `Jouer.bat`.

## Commandes

| Touche | Action |
|---|---|
| Flèches directionnelles | Déplacer Pac-Man |
| Espace | Pause / reprise |

Au démarrage, un menu permet de choisir la difficulté (Débutant / Intermédiaire / Avancé). À la fin d'une partie, tu peux rejouer directement ou retourner au menu.

## Développement

- `src/` : sources Java et images (`.png`)
- `bin/` : classes compilées (généré par `build.bat`, pas versionné)
- `build.bat` : recompile les sources et régénère `PacMan.jar`
- `Jouer.bat` : lance le jeu (`javaw -jar PacMan.jar`, sans fenêtre de console)

Ouvrir le dossier dans VS Code avec l'extension Java suffit aussi pour compiler/lancer directement depuis `src/App.java`.
