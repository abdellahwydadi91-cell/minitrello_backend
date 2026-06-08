package org.minitrello.backend.controller;

import lombok.RequiredArgsConstructor;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.service.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // GET /api/utilisateurs — liste tous les utilisateurs
    @GetMapping
    public ResponseEntity<List<Utilisateur>> getAll() {
        return ResponseEntity.ok(
                utilisateurService.obtenirTousLesUtilisateurs()
        );
    }

    // GET /api/utilisateurs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getById(@PathVariable Long id) {
        return utilisateurService.obtenirUtilisateurParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/utilisateurs/moi — profil de l'utilisateur connecté
    @GetMapping("/moi")
    public ResponseEntity<Utilisateur> getMoi() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return utilisateurService.obtenirUtilisateurParEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/utilisateurs/{id} — modifier profil
    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> modifier(@PathVariable Long id,
                                                @RequestBody Utilisateur utilisateur) {
        return ResponseEntity.ok(
                utilisateurService.mettreAJourUtilisateur(id, utilisateur)
        );
    }

    // DELETE /api/utilisateurs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        utilisateurService.supprimerUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/utilisateurs/{id}/role — changer le rôle (Admin)
    @PatchMapping("/{id}/role")
    public ResponseEntity<Utilisateur> changerRole(@PathVariable Long id,
                                                   @RequestParam Utilisateur.Role role) {
        return ResponseEntity.ok(
                utilisateurService.changerRole(id, role)
        );
    }

    // PATCH /api/utilisateurs/{id}/mot-de-passe — changer mot de passe
    @PatchMapping("/{id}/mot-de-passe")
    public ResponseEntity<Void> changerMotDePasse(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwords) {
        utilisateurService.changerMotDePasse(
                id,
                passwords.get("ancienMotDePasse"),
                passwords.get("nouveauMotDePasse")
        );
        return ResponseEntity.ok().build();
    }
}