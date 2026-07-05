# BdTracker 📚

Application Android (Kotlin + Jetpack Compose) permettant à un parent de suivre,
tome par tome, les BD/livres déjà lus par son enfant — pratique pour éviter les
doublons d'emprunt en médiathèque.

## Architecture

```
MVVM :  UI (Compose)  →  ViewModel (StateFlow)  →  Repository  →  Room (local) + Retrofit (Open Library)
```

- **data/local** : entités Room (`SerieEntity`, `TomeEntity`) + DAO + base de données.
- **data/remote** : appel à l'API publique et gratuite [Open Library](https://openlibrary.org/dev/docs/api/search) (aucune clé requise).
- **data/repository** : `BdRepository`, source unique de vérité utilisée par tous les ViewModels.
- **di** : injection de dépendances "faite main" (`AppContainer` + `ViewModelFactory`), volontairement simple, sans Hilt, pour rester lisible.
- **ui** : un dossier par écran (`search`, `library`, `detail`), chacun avec son `ViewModel` (StateFlow) et son écran Compose.

## Écrans

1. **Recherche** : interroge Open Library, affiche titre + couverture, bouton "+" pour ajouter à la bibliothèque.
2. **Ma bibliothèque** : liste des séries ajoutées, recherche rapide locale, clic → détail.
3. **Détail d'une série** : liste des tomes triés par numéro, pastille verte/grise (lu/non lu), clic sur la carte ou sur l'icône pour basculer le statut, filtre "non lus uniquement", bouton "+" pour ajouter un tome.

## Pourquoi les tomes sont ajoutés manuellement dans le détail

Aucune API publique et gratuite ne fournit une liste fiable et complète des tomes
d'une série de BD (Open Library indexe des œuvres individuelles, pas des séries
structurées). Le compromis retenu, simple et robuste, est :
- la **recherche** sert à trouver et ajouter la **série** elle-même (avec couverture) ;
- l'écran de **détail** permet d'ajouter chaque **tome** en quelques secondes (numéro + titre),
  ce qui correspond exactement au geste réel en médiathèque ("tome 4 : pas encore lu, je l'ajoute").

## Compiler et installer l'APK

### Prérequis
- [Android Studio](https://developer.android.com/studio) (Koala ou plus récent)
- JDK 17 (fourni avec Android Studio)
- Un appareil Android (minSdk 24, soit Android 7.0+) ou un émulateur

### Étapes

1. Ouvrir le dossier `BdTracker/` avec **Android Studio** (`File > Open`).
2. Laisser Gradle synchroniser les dépendances (première fois : peut prendre quelques minutes).
3. Brancher un téléphone Android en mode débogage USB (ou lancer un émulateur).
4. Cliquer sur **Run ▶** (ou `Shift+F10`).

### Générer un APK installable manuellement

```bash
# Depuis la racine du projet BdTracker/
./gradlew assembleDebug
```

L'APK généré se trouve dans :
```
app/build/outputs/apk/debug/app-debug.apk
```

Il suffit de le transférer sur le téléphone (câble, email, cloud) puis de l'installer
en autorisant "Sources inconnues" si demandé par Android.

Pour une version signée destinée à être distribuée plus largement :
```bash
./gradlew assembleRelease
```
(nécessite de configurer une clé de signature dans `app/build.gradle.kts`).

## Conseils d'amélioration

- **Synchronisation multi-appareils** : ajouter Firebase Firestore en miroir de Room
  (écrire dans les deux à chaque modification, ou utiliser Firestore comme unique
  source avec cache local automatique). Pratique si les deux parents veulent consulter
  la bibliothèque depuis leurs téléphones respectifs.
- **Scan de code-barres ISBN** : utiliser CameraX + ML Kit pour scanner le dos d'une BD
  directement en rayon et retrouver la série instantanément.
- **Import/export** : bouton "exporter ma bibliothèque en JSON" pour sauvegarde manuelle
  ou partage entre appareils sans dépendre du cloud.
- **Tests** : ajouter des tests unitaires sur `BdRepository` (avec une base Room en mémoire)
  et sur les ViewModels (avec `kotlinx-coroutines-test`).
- **Mode hors-ligne pour la recherche** : mettre en cache les dernières recherches Open Library
  pour pouvoir consulter les résultats même sans réseau (WiFi faible en médiathèque).
