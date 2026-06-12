# 🎬 Cinémastre

> Mon carnet de films personnel — façon Letterboxd, version **Kotlin**. J'enregistre les films que j'ai vus, je les note à ma façon, et l'application calcule mes statistiques.

Réécriture en Kotlin du projet CinéStats : une seule application **Ktor** qui sert l'API REST, les statistiques et le front, avec une base **SQLite** modélisée selon Merise (MCD/MLD/MPD).

📐 **La conception de la base (MCD, MLD, MPD) est dans [`docs/CONCEPTION.md`](docs/CONCEPTION.md).**

---

## 📦 Architecture

```
┌──────────────────────────────────────────────┐
│              Navigateur (front)               │
│         http://localhost:8080/index.html      │
└──────────────┬────────────────┬───────────────┘
               │ /api/logs      │ /stats/*
               ▼                ▼
   ┌──────────────────────────────────────────┐
   │           cinemastre  (:8080)             │
   │   Kotlin + Ktor + Exposed + SQLite        │
   │   CRUD films • statistiques • front       │
   └──────────────────────────────────────────┘
```

- **API films** : CRUD complet sur le journal (`/api/logs`)
- **Statistiques** : moyenne, top 5, répartition par genre (`/stats/*`)
- **Modèle** : 2 tables `FILM` et `GENRE` reliées par une clé étrangère (voir la conception)

---

## ✅ Prérequis

| Outil  | Pour quoi faire |
|--------|-----------------|
| Git    | Cloner le projet |
| Docker | Lancer l'application (recommandé — rien d'autre à installer) |

Installation sur Ubuntu/Debian :

```bash
sudo apt update && sudo apt install -y git
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER   # puis se déconnecter/reconnecter
```

Pour développer **en local sans Docker**, il faut en plus **JDK 21** et **Gradle** :

```bash
sudo apt install -y openjdk-21-jdk
# Gradle récent via SDKMAN (la version apt est trop ancienne) :
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle
```

---

## 🚀 Lancer le projet

### Option A — Docker (recommandé)

```bash
docker compose up --build -d
```

| Ressource | URL |
|-----------|-----|
| 🌐 Front | http://localhost:8080/index.html |
| 🎬 API films | http://localhost:8080/api/logs |
| 📊 Stats | http://localhost:8080/stats/summary |

La base SQLite est stockée dans un **volume Docker** : les films survivent aux redémarrages.

```bash
docker compose logs -f     # suivre les logs
docker compose down        # arrêter
```

### Option B — En local (développement)

```bash
gradle run
# puis ouvrir http://localhost:8080/index.html
```

---

## 🔌 Endpoints

| Méthode | Route | Description |
|---------|-------|-------------|
| `GET`    | `/api/logs`      | Liste tous les films |
| `GET`    | `/api/logs/{id}` | Détail d'un film |
| `POST`   | `/api/logs`      | Ajoute un film |
| `PUT`    | `/api/logs/{id}` | Modifie un film |
| `DELETE` | `/api/logs/{id}` | Supprime un film |
| `GET`    | `/stats/summary`  | Total, moyenne, coups de cœur |
| `GET`    | `/stats/by-genre` | Répartition par genre |
| `GET`    | `/stats/top`      | Top 5 des mieux notés |
| `GET`    | `/health`         | État du serveur |

Exemple :

```bash
curl -X POST http://localhost:8080/api/logs \
  -H "Content-Type: application/json" \
  -d '{"title":"Dune","year":2021,"genre":"Sci-Fi","rating":4.5,"review":"Magnifique"}'
```

Les erreurs de validation renvoient un statut `422` :

```json
{"errors": ["La note doit être comprise entre 0,5 et 5"]}
```

---

## 🧪 Tests unitaires

```bash
gradle test
```

Les tests (JUnit / kotlin.test) couvrent la logique métier pure : calculs de statistiques, règles de validation, `isHighlyRated`.

---

## ⚙️ CI/CD — GitHub Actions

À chaque `push` sur `main`, le workflow `.github/workflows/ci.yml` :

1. lance les **tests unitaires** ;
2. si tout est vert, construit et **pousse l'image sur Docker Hub** (`<utilisateur>/cinemastre:latest`).

Secrets requis dans **Settings → Secrets and variables → Actions** :

| Secret | Valeur |
|--------|--------|
| `DOCKER_USERNAME` | Identifiant Docker Hub |
| `DOCKER_PASSWORD` | Jeton d'accès Docker Hub (Account Settings → Personal access tokens) |

---

## 📁 Structure du projet

```
cinemastre/
├── build.gradle.kts            # dépendances (Ktor, Exposed, SQLite…)
├── settings.gradle.kts
├── Dockerfile                  # build multi-étapes Gradle → JRE
├── docker-compose.yml          # + volume pour la persistance SQLite
├── .github/workflows/ci.yml    # tests + push Docker Hub
└── src/
    ├── main/
    │   ├── kotlin/fr/cinemastre/
    │   │   ├── Application.kt  # point d'entrée Ktor
    │   │   ├── Db.kt           # tables Exposed (= le MPD)
    │   │   ├── Models.kt       # DTO + validation
    │   │   ├── Routes.kt       # routes API + stats
    │   │   └── Stats.kt        # calculs statistiques (purs)
    │   └── resources/static/
    │       └── index.html      # le front
    └── test/kotlin/fr/cinemastre/
        └── CinemastreTest.kt   # tests unitaires
```

---

## 🛠️ Stack technique

- **Langage** : Kotlin 2.1 (JVM 21)
- **Serveur** : Ktor 3 (Netty)
- **ORM** : Exposed + SQLite
- **Sérialisation** : kotlinx.serialization
- **Tests** : JUnit 5 / kotlin.test
- **Conteneurs** : Docker, Docker Compose
- **CI/CD** : GitHub Actions → Docker Hub
