package org.minitrello.backend.controller;

import lombok.RequiredArgsConstructor;
import org.minitrello.backend.dto.ProjetDTO;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.minitrello.backend.service.ProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProjetController {

    private final ProjetService projetService;
    private final UtilisateurRepository utilisateurRepository;

    private Utilisateur getUtilisateurConnecte() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));
    }

    @GetMapping
    public ResponseEntity<List<ProjetDTO>> getMesProjets() {
        Utilisateur u = getUtilisateurConnecte();
        return ResponseEntity.ok(projetService.obtenirProjetsByUtilisateur(u.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetDTO> getById(@PathVariable Long id) {
        return projetService.obtenirProjetParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjetDTO> creer(@RequestBody Projet projet) {
        Utilisateur u = getUtilisateurConnecte();
        return ResponseEntity.ok(projetService.creerProjet(projet, u.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjetDTO> modifier(@PathVariable Long id,
                                              @RequestBody Projet projet) {
        return ResponseEntity.ok(projetService.mettreAJourProjet(id, projet));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        projetService.supprimerProjet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tous")
    public ResponseEntity<List<ProjetDTO>> getTousMesProjets() {
        Utilisateur u = getUtilisateurConnecte();

        // chef -> tous les projets
        // mewmbre -> seulement ses projets

        if (u.getRole() == Utilisateur.Role.CHEF_PROJET){
            return ResponseEntity.ok(projetService.obtenirTousLesProjets());
        }
        return ResponseEntity.ok(projetService.obtenirProjetsByUtilisateur(u.getId()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProjetDTO>> getAllProjets() {
        return ResponseEntity.ok(projetService.obtenirTousLesProjets());
    }

    @PostMapping("/{id}/membres/{membreId}")
    public ResponseEntity<ProjetDTO> ajouterMembre(@PathVariable Long id,
                                                   @PathVariable Long membreId) {
        return ResponseEntity.ok(projetService.ajouterMembreAuProjet(id, membreId));
    }

    @DeleteMapping("/{id}/membres/{membreId}")
    public ResponseEntity<ProjetDTO> retirerMembre(@PathVariable Long id,
                                                   @PathVariable Long membreId) {
        return ResponseEntity.ok(projetService.retirerMembreDuProjet(id, membreId));
    }
}