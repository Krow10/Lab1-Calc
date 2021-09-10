# [GTI785 Systèmes d'applications mobiles] Lab 1 : Git, Android Studio et Calculatrice
## Présentation
L'objectif de ce travail de laboratoire est de réaliser une application Android de calculatrice aux fonctionnalités basiques à l'aide d'Android Studio.
Les opérations supportées sont les suivantes :
* **[+]** Addition
* **[−]** Soustraction
* **[×]** Multiplication
* **[÷]** Division

L'interface se compose d'un champ éditable pour entrer les nombres, d'un affichage du résultat ainsi que d'un bouton de suppression.

Voici une capture du prototype demandé (à gauche) et du résultat final (à droite) :

![image](https://user-images.githubusercontent.com/23462475/132776344-36f2e728-31e1-43dc-8cef-ee42ed38a537.png)

Les nombres flottants (à virgule) ainsi que les nombres négatifs sont supportés mais ne peuvent pas être rentrés directement.

Une limite de 200 caractères est fixée pour le premier champ et les erreurs telles que les divisons par zéro ou expressions incorrectes sont signalées à l'utilisateur par une notification.

![image](https://user-images.githubusercontent.com/23462475/132776913-1b566fe5-7c5c-4046-b420-b3c72346f2d6.png)

Certaines constantes telles que la couleur des opérateurs, le nombre maximum de caractères ou encore la taille du texte peuvent être modifiées simplement dans le fichier [main_activity_layout_constants.xml](https://github.com/Krowten11/Lab1-Calc/blob/master/app/src/main/res/values/main_activity_layout_constants.xml).

## Installation

Récupérer la dernière version de l'apk depuis la [page de publication]() ou compilez là vous même à l'aide de Gradle !

## License

Distribué sous la license MIT. Voir le fichier [LICENSE.md](https://github.com/Krowten11/Lab1-Calc/blob/master/LICENSE.md) pour plus d'informations.

## Contact

Etienne Donneger - etienne.donneger.1@ens.etsmtl.ca
