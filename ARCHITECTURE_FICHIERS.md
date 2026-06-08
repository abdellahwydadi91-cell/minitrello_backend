# 📁 ARCHITECTURE DES FICHIERS - MINI TRELLO BACKEND

> **Structure complète du projet avec explication détaillée de chaque dossier et fichier**

---

## 🌳 ARBORESCENCE COMPLÈTE

```
minitrello/
│
├── 📄 Fichiers de configuration racine
│   ├── pom.xml                          ← Dépendances Maven
│   ├── mvnw & mvnw.cmd                  ← Wrapper Maven (exécution sans Maven installé)
│   ├── .gitignore                       ← Fichiers ignorés par Git
│   ├── .gitattributes                   ← Attributs Git
│   ├── HELP.md                          ← Aide Spring Boot
│   ├── server.out.log                   ← Logs de sortie du serveur
│   └── server.err.log                   ← Logs d'erreur du serveur
│
├── 📂 .mvn/
│   └── wrapper/                         ← Configuration du wrapper Maven
│
├── 📂 .idea/
│   └── (Configuration IDE IntelliJ)
│
├── 📂 src/ ⭐ (Dossier source principal)
│   │
│   ├── 📂 main/
│   │   │
│   │   ├── 📂 java/org/minitrello/backend/ ⭐ (Code métier)
│   │   │   │
│   │   │   ├── 📄 MinitrelloApplication.java (Point d'entrée Spring Boot)
│   │   │   │
│   │   │   ├── 📂 controller/ ⭐ (Endpoints REST - Couche présentation)
│   │   │   │   ├── AuthController.java                  (Login/Register)
│   │   │   │   ├── ProjetController.java                (CRUD Projets)
│   │   │   │   ├── ColonneController.java               (Gestion colonnes)
│   │   │   │   ├── TacheController.java                 (Tâches + Images)
│   │   │   │   └── UtilisateurController.java           (Utilisateurs)
│   │   │   │
│   │   │   ├── 📂 service/ ⭐ (Logique métier - Couche business)
│   │   │   │   ├── ProjetService.java                   (Logique projets)
│   │   │   │   ├── ColonneService.java                  (Logique colonnes)
│   │   │   │   ├── TacheService.java                    (Logique tâches + upload)
│   │   │   │   └── UtilisateurService.java              (Logique utilisateurs)
│   │   │   │
│   │   │   ├── 📂 repository/ ⭐ (Accès base de données - Data layer)
│   │   │   │   ├── UtilisateurRepository.java           (JPA interface)
│   │   │   │   ├── ProjetRepository.java                (JPA interface)
│   │   │   │   ├── ColonneRepository.java               (JPA interface)
│   │   │   │   └── TacheRepository.java                 (JPA interface)
│   │   │   │
│   │   │   ├── 📂 entity/ ⭐ (Modèles de données - Entités JPA)
│   │   │   │   ├── Utilisateur.java                     (Entité + Enum Role)
│   │   │   │   ├── Projet.java                          (Entité)
│   │   │   │   ├── Colonne.java                         (Entité)
│   │   │   │   ├── Tache.java                           (Entité + Enum Priorité)
│   │   │   │   ├── Commentaire.java                     (Entité)
│   │   │   │   └── StatutTache.java                     (Enum: A_FAIRE, EN_COURS, TERMINEE)
│   │   │   │
│   │   │   ├── 📂 dto/ ⭐ (Data Transfer Objects - Transfert données API)
│   │   │   │   ├── ProjetDTO.java                       (DTO pour projets)
│   │   │   │   ├── ColonneDTO.java                      (DTO pour colonnes)
│   │   │   │   ├── TacheDTO.java                        (DTO pour tâches)
│   │   │   │   ├── JwtResponse.java                     (Réponse login)
│   │   │   │   └── LoginRequest.java                    (Requête login)
│   │   │   │
│   │   │   ├── 📂 security/ ⭐ (Gestion authentification & sécurité)
│   │   │   │   ├── SecurityConfig.java                  (Config Spring Security)
│   │   │   │   ├── JwtUtils.java                        (Génération/validation JWT)
│   │   │   │   ├── JwtAuthenticationFilter.java         (Filtre JWT pour chaque requête)
│   │   │   │   └── UserDetailsServiceImpl.java           (Chargement utilisateur)
│   │   │   │
│   │   │   ├── 📂 config/
│   │   │   │   └── (Configurations optionnelles)
│   │   │   │
│   │   │   └── 📂 exception/
│   │   │       └── (Gestion des exceptions - optionnel)
│   │   │
│   │   └── 📂 resources/ ⭐ (Fichiers de configuration)
│   │       ├── application.properties        (Configuration Spring Boot)
│   │       ├── 📂 static/                    (Fichiers statiques - CSS, JS)
│   │       └── 📂 templates/                 (Templates HTML - Thymeleaf)
│   │
│   └── 📂 test/ ⭐ (Tests unitaires)
│       └── 📂 java/org/minitrello/backend/
│           ├── MinitrelloApplicationTests.java
│           ├── controller/
│           │   ├── ProjetControllerIntegrationTest.java
│           │   └── TacheControllerIntegrationTest.java
│           ├── service/
│           │   ├── ProjetServiceTest.java
│           │   ├── ColonneServiceTest.java
│           │   ├── TacheServiceTest.java
│           │   ├── UtilisateurServiceTest.java
│           │   └── CommentaireServiceTest.java
│           └── security/
│               └── JwtUtilsTest.java
│
├── 📂 target/ (Généré - Compilation)
│   ├── classes/                          (Classes Java compilées)
│   ├── test-classes/                     (Classes de test compilées)
│   ├── generated-sources/                (Code généré par Maven)
│   ├── surefire-reports/                 (Rapports de tests)
│   ├── maven-status/                     (État Maven)
│   └── backend-0.0.1-SNAPSHOT.jar        (JAR packagé)
│
├── 📂 uploads/ ⭐ (Fichiers uploadés)
│   └── taches/                           (Images des tâches)
│       ├── 52ab442e-6c77-4698-9648-66c3100da22a.png
│       └── ... (autres images)
│
├── 📂 Documentation (Fichiers créés pour soutenance)
│   ├── GUIDE_SOUTENANCE_BACKEND.md       (Documentation complète)
│   ├── FEUILLE_TRICHE_SOUTENANCE.md      (Aide-mémoire)
│   ├── SCRIPT_DEMO.md                    (Commandes prêtes à exécuter)
│   ├── RESUME_1PAGE.md                   (Diaporama condensé)
│   ├── QUICK_START.md                    (Démarrage rapide)
│   ├── INDEX_DOCUMENTS.md                (Guide de navigation)
│   ├── CAHIER_DES_CHARGES_API.md         (Spécifications API)
│   └── ARCHITECTURE_FICHIERS.md          (Ce fichier!)
│
└── 📂 postman_collection.json            (Collection d'APIs Postman)
```

---

## 📊 STRUCTURE PAR COUCHES (Architecture MVC)

```
┌─────────────────────────────────────┐
│      PRÉSENTATION (Couche 1)        │
├─────────────────────────────────────┤
│   📂 controller/                    │
│   • AuthController.java             │
│   • ProjetController.java           │
│   • TacheController.java            │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│      MÉTIER (Couche 2)              │
├─────────────────────────────────────┤
│   📂 service/                       │
│   • ProjetService.java              │
│   • TacheService.java               │
│   • UtilisateurService.java         │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│      DONNÉES (Couche 3)             │
├─────────────────────────────────────┤
│   📂 repository/ (JPA)              │
│   • ProjetRepository.java           │
│   • TacheRepository.java            │
│   ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓                    │
│   PostgreSQL Database               │
└─────────────────────────────────────┘
```

---

## 🔑 FICHIERS CRITIQUES EXPLIQUÉS

### ⭐ Point d'entrée

**MinitrelloApplication.java**
```
Purpose    : Lancer l'application Spring Boot
Annotation : @SpringBootApplication
Method     : static void main(String[] args)
Action     : SpringApplication.run(MinitrelloApplication.class, args)
```

---

### 📡 CONTRÔLEURS (Endpoints REST)

| Fichier | Endpoints | Routes |
|---------|-----------|--------|
| **AuthController.java** | Login, Register | `/api/auth/login`, `/api/auth/register` |
| **ProjetController.java** | CRUD Projets | `/api/projets` |
| **ColonneController.java** | CRUD Colonnes | `/api/colonnes` |
| **TacheController.java** | CRUD Tâches + Images | `/api/taches` |
| **UtilisateurController.java** | CRUD Utilisateurs | `/api/utilisateurs` |

---

### 🔧 SERVICES (Logique métier)

| Fichier | Responsabilités |
|---------|-----------------|
| **ProjetService.java** | Création, modification, gestion membres de projets |
| **ColonneService.java** | Gestion colonnes, réorganisation (drag & drop) |
| **TacheService.java** | Création tâches, déplacement, upload images, changement statut |
| **UtilisateurService.java** | Gestion utilisateurs, modification rôles |

---

### 💾 REPOSITORY (Accès BD - JPA)

| Fichier | Méthodes |
|---------|----------|
| **UtilisateurRepository.java** | findByEmail(), findById(), findAll() |
| **ProjetRepository.java** | findByProprietaire(), findProjetsByUtilisateur(), findAll() |
| **ColonneRepository.java** | findByProjet(), findByIdAndProjet(), custom queries |
| **TacheRepository.java** | findByColonne(), findTachesByAssigne(), findByIdWithRelations() |

---

### 📦 ENTITÉS (Modèles JPA)

| Fichier | BD Table | Relations |
|---------|----------|-----------|
| **Utilisateur.java** | utilisateurs | 1 propriétaire de N projets |
| **Projet.java** | projets | N colonnes, N membres (M:M) |
| **Colonne.java** | colonnes | N tâches |
| **Tache.java** | taches | N assignés (M:M), N images, N commentaires |
| **Commentaire.java** | commentaires | 1 tâche, 1 auteur |
| **StatutTache.java** | (Enum) | A_FAIRE, EN_COURS, TERMINEE |

---

### 🔐 SÉCURITÉ

| Fichier | Rôle |
|---------|------|
| **SecurityConfig.java** | Configuration Spring Security, CORS, autorisation |
| **JwtUtils.java** | Génération et validation des tokens JWT |
| **JwtAuthenticationFilter.java** | Filtre exécuté pour CHAQUE requête (vérifie JWT) |
| **UserDetailsServiceImpl.java** | Charge l'utilisateur depuis la BD pour Spring |

---

### 📨 DTO (Data Transfer Objects)

```
Purpose : Séparer les entités (BD) de l'API REST
Avantage : Flexibilité, sécurité (ne pas exposer certains champs)

Exemple :
┌─────────────────┐         ┌──────────────┐
│ Entité Tache    │  →      │   TacheDTO   │
│ (BD)            │  (Conversion)         │
├─────────────────┤         ├──────────────┤
│ id              │         │ id           │
│ titre           │         │ titre        │
│ description     │         │ description  │
│ colonne (objet) │  X      │ colonneId    │
│ assignes (list) │  →      │ assigneIds   │
└─────────────────┘         └──────────────┘
```

---

## 🏗️ DÉPENDANCES MAVEN (pom.xml)

```xml
<!-- Core Spring Boot -->
spring-boot-starter-web              → Serveur REST HTTP
spring-boot-starter-data-jpa         → ORM Hibernate
spring-boot-starter-security         → Spring Security
spring-boot-starter-validation       → Validation données

<!-- Base de données -->
postgresql                            → Driver PostgreSQL

<!-- JWT Tokens -->
jjwt-api, jjwt-impl, jjwt-jackson    → Génération/validation JWT

<!-- Utils -->
lombok                               → Réduction boilerplate
springdoc-openapi                    → Swagger/OpenAPI
commons-fileupload                   → Upload fichiers
spring-test                          → Framework tests

<!-- Tests -->
spring-boot-starter-test            → JUnit 5, Mockito
```

---

## 🗄️ BASE DE DONNÉES (PostgreSQL)

```sql
TABLES PRINCIPALES :
├── utilisateurs (id, nom, prenom, email, mot_de_passe, role, avatar)
├── projets (id, titre, description, proprietaire_id, date_debut, date_fin)
├── colonnes (id, nom, couleur, position, projet_id)
├── taches (id, titre, description, priorite, statut, position, colonne_id)
├── commentaires (id, contenu, tache_id, auteur_id, date_creation)
├── membres_projet (projet_id, utilisateur_id) [Many-to-Many]
├── tache_assignees (tache_id, utilisateur_id) [Many-to-Many]
└── tache_images (tache_id, image_url) [ElementCollection]
```

---

## 📂 DOSSIERS SPÉCIALISÉS

### uploads/taches/
```
Purpose    : Stockage physique des images uploadées
Structure  : ./uploads/taches/<UUID>.png
Exemple    : ./uploads/taches/52ab442e-6c77-4698-9648-66c3100da22a.png
Accès      : http://localhost:8080/uploads/taches/<UUID>.png
Permissions: lecture publique (configuré dans SecurityConfig)
```

### target/
```
Purpose    : Artefacts compilés (généré par Maven)
├── classes/                 Bytecode Java compilé
├── test-classes/            Tests compilés
├── generated-sources/       Code généré
├── surefire-reports/        Résultats des tests
└── backend-0.0.1-SNAPSHOT.jar   JAR exécutable
```

### src/test/
```
Purpose    : Tests unitaires et d'intégration
├── ProjetControllerIntegrationTest.java    Test API Projets
├── TacheControllerIntegrationTest.java     Test API Tâches
├── ProjetServiceTest.java                  Test logique métier
├── ColonneServiceTest.java                 Test logique colonnes
├── TacheServiceTest.java                   Test logique tâches
├── UtilisateurServiceTest.java             Test logique utilisateurs
├── CommentaireServiceTest.java             Test logique commentaires
└── JwtUtilsTest.java                       Test authentification JWT
```

---

## 🔄 FLUX DE DONNÉES

```
CLIENT (React)
    │
    ├─ Requête HTTP
    │  (avec JWT en header)
    │
    ↓
CONTRÔLEUR (ProjetController.java)
    │
    ├─ Désérialise JSON → DTO
    ├─ Valide les données
    │
    ↓
SERVICE (ProjetService.java)
    │
    ├─ Logique métier
    ├─ Vérifie règles business
    │
    ↓
REPOSITORY (ProjetRepository.java)
    │
    ├─ Query BD (JPA)
    │
    ↓
DATABASE (PostgreSQL)
    │
    ├─ SELECT, INSERT, UPDATE, DELETE
    │
    ↓
REPOSITORY
    │
    ├─ Retourne Entity
    │
    ↓
SERVICE
    │
    ├─ Traite données (si besoin)
    │
    ↓
CONTRÔLEUR
    │
    ├─ Convertit Entity → DTO
    ├─ Sérialise JSON
    │
    ↓
CLIENT
    │
    └─ Reçoit réponse JSON
```

---

## 🎯 PATTERNS ET ARCHITECTURE

### Design Patterns utilisés

1. **MVC (Model-View-Controller)**
   - Model = Entity + DTO
   - View = JSON retourné
   - Controller = Endpoints REST

2. **DAO (Data Access Object)**
   - Repository pattern
   - JpaRepository (Spring Data)

3. **Service Layer**
   - Logique métier centralisée
   - Réutilisable pour multiple contrôleurs

4. **DTO (Data Transfer Object)**
   - Sépare persistance et API
   - Sécurité + flexibilité

5. **Singleton**
   - Services injectés par Spring (@Service)
   - Une instance partagée

6. **Dependency Injection**
   - @Autowired
   - Constructor injection

---

## 📈 DÉPENDANCES ENTRE FICHIERS

```
AuthController.java
    ↓ dépend →
AuthenticationManager (SecurityConfig)
UserDetailsService (UserDetailsServiceImpl)
UtilisateurRepository
JwtUtils

ProjetController.java
    ↓ dépend →
ProjetService
UtilisateurRepository
SecurityContextHolder (Spring Security)

ProjetService.java
    ↓ dépend →
ProjetRepository
UtilisateurRepository
Projet (Entity)
Utilisateur (Entity)

TacheService.java
    ↓ dépend →
TacheRepository
ColonneRepository
UtilisateurRepository
(+ File I/O pour images)
```

---

## 🔐 Couche Sécurité

```
Request HTTP
    ↓
JwtAuthenticationFilter
    ├─ Extraire JWT du header
    ├─ Valider signature (JwtUtils)
    ├─ Charger utilisateur (UserDetailsServiceImpl)
    ├─ Charger autorités (rôles)
    ↓
SecurityContextHolder (contexte Spring)
    ↓
SecurityConfig
    ├─ Vérifier autorisation
    ├─ Appliquer règles CORS
    ├─ CSRF disabled (API REST)
    ↓
Contrôleur (si autorisé)
```

---

## 📊 Statistiques du projet

| Métrique | Nombre |
|----------|--------|
| **Fichiers Java** | ~25 |
| **Lignes de code** | ~2500 |
| **Entités JPA** | 6 |
| **DTOs** | 5 |
| **Contrôleurs** | 5 |
| **Services** | 4 |
| **Repositories** | 4 |
| **Endpoints API** | ~40 |
| **Tables BD** | 8 |
| **Relations M:M** | 3 |
| **Tests** | 8+ |

---

## 🚀 Startup Sequence

```
1. MinitrelloApplication.main() lancé
                ↓
2. Spring Boot initialise le contexte
                ↓
3. Charge application.properties
                ↓
4. Crée Connection Pool PostgreSQL
                ↓
5. Charge les Beans (@Bean, @Service, @Repository)
                ↓
6. JPA Hibernate crée les tables (ddl-auto=update)
                ↓
7. Enregistre les Contrôleurs REST
                ↓
8. Configure SecurityFilterChain + JwtAuthenticationFilter
                ↓
9. Lance le serveur Tomcat sur le port 8080
                ↓
✅ Application prête à recevoir des requêtes
```

---

## 📝 Fichiers de configuration

### application.properties
```properties
# Serveur
server.port=8080

# Database PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/mini_trello
spring.datasource.username=postgres
spring.datasource.password=nano

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
app.jwt.expiration=86400000  (24 heures)

# Upload
file.upload-dir=./uploads/taches/
spring.servlet.multipart.max-file-size=10MB
```

---

## 🎯 Guide de modification

### Ajouter une nouvelle entité
```
1. Créer Entity dans src/main/java/.../entity/MaNouvelle.java
2. Créer Repository : src/main/java/.../repository/MaNouvelleRepository.java
3. Créer Service : src/main/java/.../service/MaNouvelleService.java
4. Créer Controller : src/main/java/.../controller/MaNouvelleController.java
5. Créer DTO : src/main/java/.../dto/MaNouvelleDTO.java
6. Créer Tests : src/test/java/.../service/MaNouvelleServiceTest.java
```

### Ajouter un nouvel endpoint
```
1. Ajouter @PostMapping/@GetMapping dans le Controller
2. Implémenter la logique dans le Service
3. Tester dans Swagger UI
```

### Deployer en production
```
1. Compiler : mvn clean package -DskipTests
2. JAR généré : target/backend-0.0.1-SNAPSHOT.jar
3. Run : java -jar target/backend-0.0.1-SNAPSHOT.jar
```

---

## 🔗 Connexions inter-fichiers

```
Scenario : Créer une tâche

1. Frontend → AuthController.login()
2. Récoit JWT
3. TacheController.creer(colonneId, Tache)
   ├─ Vérifie JWT (JwtAuthenticationFilter + SecurityConfig)
   ├─ Appelle TacheService.creerTache()
   │   ├─ Valide Colonne (ColonneRepository.findById())
   │   ├─ Valide Utilisateur (UtilisateurRepository.findById())
   │   ├─ Vérifie accès projet (BusinessLogic)
   │   ├─ Calcule position automatiquement
   │   └─ Sauvegarde (TacheRepository.save())
   ├─ Convertit Tache → TacheDTO
   └─ Retourne 200 OK + DTO JSON
4. Frontend reçoit la nouvelle tâche
```

---

## ✨ Points clés de l'architecture

| Point | Explication |
|-------|-------------|
| **Séparation des responsabilités** | Constructor → Service → Repository |
| **Inversion de contrôle** | Spring gère les instances (@Autowired) |
| **Stateless** | JWT au lieu de sessions (scalable) |
| **DTOs** | Sépare BD de l'API (sécurité) |
| **Transactions** | @Transactional pour cohérence |
| **Lazy Loading** | Relations en FetchType.LAZY (perf) |
| **Cascade Delete** | Suppression en cascade (intégrité) |
| **Many-to-Many** | Tables de jonction pour flexibilité |

---

**Architecture claire et maintenable ! 🏗️**


