# BdTracker 📚

Application Android (Kotlin + Jetpack Compose) permettant à un parent de suivre,
tome par tome, les BD/livres déjà lus par son enfant — pratique pour éviter les
doublons d'emprunt en médiathèque.

## Architecture

```
MVVM :  UI (Compose)  →  ViewModel (StateFlow)  →  Repository  →  Room (local) + Retrofit (Open Library)
```

- **data/local** : entités Room (`SerieEntity`, `TomeEntity`) + DAO + base de données.
- **data/remote** : appel à l'API publique et gratuite **Google Books** (aucune clé requise), utilisée pour retrouver automatiquement tous les tomes d'une série.
- **data/repository** : `BdRepository`, source unique de vérité utilisée par tous les ViewModels.
- **di** : injection de dépendances "faite main" (`AppContainer` + `ViewModelFactory`), volontairement simple, sans Hilt, pour rester lisible.
- **ui** : un dossier par écran (`search`, `library`, `detail`), chacun avec son `ViewModel` (StateFlow) et son écran Compose.

## Écrans

1. **Recherche** : le parent tape juste le **nom de la série** (ex: "Les Légendaires"). L'application interroge automatiquement l'API Google Books, détecte le numéro de chaque tome dans son titre, et affiche la liste complète avec les jaquettes. Un seul bouton ajoute tous les tomes cochés à la bibliothèque, sans aucune saisie manuelle de numéro.
2. **Ma bibliothèque** : liste des séries ajoutées, recherche rapide locale, clic → détail.
3. **Détail d'une série** : liste des tomes triés par numéro, pastille verte/grise (lu/non lu), clic sur la carte ou sur l'icône pour basculer le statut, filtre "non lus uniquement". Un bouton "+" reste disponible pour ajouter manuellement un tome que Google Books n'aurait pas trouvé.

## Comment les tomes sont détectés automatiquement

L'application utilise l'API publique et gratuite **Google Books** (`intitle:{nom de la série}`), qui indexe très bien les séries de BD francophones. Pour chaque résultat, `TomeNumeroExtractor` analyse le titre (ex: "Les Légendaires - Tome 12 - Le crépuscule des dieux") avec une série d'expressions régulières ("Tome X", "T.X", "Vol. X", "#X") afin d'en extraire le numéro. Les résultats sont ensuite triés par numéro et dédoublonnés (plusieurs éditions du même tome ne comptent qu'une fois). Si un titre ne contient aucun numéro détectable, un numéro de secours lui est attribué automatiquement à la suite des autres.

Cette détection n'est pas garantie à 100 % (catalogue Google Books incomplet ou titres atypiques) : l'utilisateur peut décocher un résultat non pertinent avant l'ajout, et l'ajout manuel dans l'écran de détail reste disponible en solution de secours.

## Erreur "code 429" (quota dépassé)

Google Books limite le nombre de requêtes anonymes par adresse IP. Sur un WiFi partagé
(médiathèque, bibliothèque), cette IP est utilisée par de nombreux appareils à la fois :
le quota se sature vite et n'a **rien à voir avec ta connexion personnelle**. L'application
réessaie automatiquement 2 fois en cas de 429 passager, mais si le quota du réseau est
vraiment épuisé, il faut une clé API personnelle (gratuite) :

1. Va sur [console.cloud.google.com](https://console.cloud.google.com/), crée un projet (gratuit, aucune carte bancaire nécessaire pour cette API).
2. Menu **APIs & Services > Library** → recherche **"Books API"** → **Enable**.
3. Menu **APIs & Services > Credentials** → **Create credentials > API key**. Copie la clé générée.
4. (Recommandé) Restreins la clé à l'API "Books API" uniquement, via "Restrict key".
5. À la racine du projet (le dossier `BdTracker/`, au même niveau que `settings.gradle.kts`), crée un fichier `local.properties` avec cette ligne :
   ```
   GOOGLE_BOOKS_API_KEY=ta_cle_ici
   ```
   Ce fichier est automatiquement ignoré par Git (`.gitignore`) : **il ne sera jamais visible sur GitHub**, contrairement au code du reste du projet qui est public.
6. Recompile (`./gradlew assembleDebug` ou relance le workflow GitHub Actions après avoir ajouté ce fichier **en local uniquement**, ou en secret GitHub Actions — voir plus bas).

### Utiliser la clé avec GitHub Actions (sans jamais l'exposer publiquement)

Le fichier `local.properties` n'étant pas commité, le build GitHub Actions ne le voit pas.
Pour lui fournir la clé de façon sécurisée :
1. Sur GitHub : Settings du dépôt → **Secrets and variables > Actions** → **New repository secret**.
2. Nom : `GOOGLE_BOOKS_API_KEY`, valeur : ta clé.
3. Dans `.github/workflows/build-apk.yml`, juste avant l'étape "Compiler l'APK de debug", ajoute :
   ```yaml
   - name: Créer local.properties avec la clé API
     run: echo "GOOGLE_BOOKS_API_KEY=${{ secrets.GOOGLE_BOOKS_API_KEY }}" >> local.properties
   ```
Ainsi la clé reste un secret GitHub, jamais visible dans le code source public.

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
