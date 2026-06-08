# ⚡ AUTHENTICATIONMANAGER - RÉPONSE RAPIDE

> **Où trouver AuthenticationManager en 30 secondes**

---

## 🎯 RÉPONSE DIRECTE

### AuthenticationManager est À DEUX ENDROITS:

#### 1️⃣ **CRÉÉ dans**: `SecurityConfig.java`

```
Fichier: src/main/java/org/minitrello/backend/security/SecurityConfig.java
Ligne  : 49-51

Code:
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
```

#### 2️⃣ **UTILISÉ dans**: `AuthController.java`

```
Fichier: src/main/java/org/minitrello/backend/controller/AuthController.java
Ligne  : 26
Usage  : Dans la méthode authenticateUser()

Code:
    @Autowired
    AuthenticationManager authenticationManager;
    
    // Utilisé au login (ligne ~34):
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(), 
            loginRequest.getMotDePasse()
        )
    );
```

---

## 💡 SIMPLE RÉSUMÉ

| | Localisation |
|---|---|
| **Créé** | `SecurityConfig.java` ligne 49 |
| **Utilisé** | `AuthController.java` ligne 26 |
| **Fonction** | Valider email + mot de passe |
| **Retourne** | Objet `Authentication` |

---

## 📂 ARBORESCENCE RAPIDE

```
src/main/java/org/minitrello/backend/
├── controller/
│   └── AuthController.java          ← UTILISÉ ICI (ligne 26)
│
└── security/
    └── SecurityConfig.java          ← CRÉÉ ICI (ligne 49-51)
```

---

## 🔍 POUR LE TROUVER VIA IDE

```
IntelliJ: Ctrl+Shift+F
Taper   : "AuthenticationManager"
Entrée  : Voir les 2 fichiers
```

---

**Voilà! AuthenticationManager trouvé! ✅**


