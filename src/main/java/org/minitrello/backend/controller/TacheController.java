package org.minitrello.backend.controller;

import org.minitrello.backend.entity.StatutTache;
import lombok.RequiredArgsConstructor;
import org.minitrello.backend.dto.TacheDTO;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.minitrello.backend.service.TacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TacheController {

    private final TacheService tacheService;
    private final UtilisateurRepository utilisateurRepository;

    private TacheDTO toDTO(Tache tache) {
        TacheDTO dto = new TacheDTO();

        // Champs de base
        dto.setId(tache.getId());
        dto.setTitre(tache.getTitre());
        dto.setDescription(tache.getDescription());
        dto.setPriorite(tache.getPriorite());
        dto.setStatut(tache.getStatut());
        dto.setDateEcheance(tache.getDateEcheance());
        dto.setPosition(tache.getPosition());

        // Colonne → titre
        if (tache.getColonne() != null) {
            dto.setColonneId(tache.getColonne().getId());
            dto.setColonneTitre(tache.getColonne().getNom());

            // Projet → via colonne
            if (tache.getColonne().getProjet() != null) {
                dto.setProjetId(tache.getColonne().getProjet().getId());
                dto.setProjetTitre(tache.getColonne().getProjet().getTitre());
            }
        }

        // Assignés (ManyToMany)
        if (tache.getAssignes() != null && !tache.getAssignes().isEmpty()) {
            dto.setAssigneIds(
                    tache.getAssignes().stream()
                            .map(Utilisateur::getId)
                            .collect(Collectors.toList())
            );
            dto.setAssigneNoms(
                    tache.getAssignes().stream()
                            .map(u -> u.getPrenom() + " " + u.getNom())
                            .collect(Collectors.toList())
            );
        }

        // NOUVEAU : Images
        if (tache.getImageUrls() != null) {
            dto.setImageUrls(tache.getImageUrls());
        }

        return dto;
    }

    // GET /api/taches/colonne/{colonneId}
    @GetMapping("/colonne/{colonneId}")
    public ResponseEntity<List<TacheDTO>> getByColonne(@PathVariable Long colonneId) {
        return ResponseEntity.ok(
                tacheService.obtenirTachesParColonne(colonneId)
                        .stream().map(this::toDTO).collect(Collectors.toList())
        );
    }

    // GET /api/taches/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TacheDTO> getById(@PathVariable Long id) {
        return tacheService.obtenirTacheParId(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/taches/mes-taches
    @GetMapping("/mes-taches")
    public ResponseEntity<List<TacheDTO>> getMesTaches() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow();
        return ResponseEntity.ok(
                tacheService.obtenirTachesAssignees(u.getId())
                        .stream().map(this::toDTO).collect(Collectors.toList())
        );
    }

    // GET /api/taches/assignees/{utilisateurId}
    @GetMapping("/assignees/{utilisateurId}")
    public ResponseEntity<List<TacheDTO>> getTachesAssignees(@PathVariable Long utilisateurId) {
        return ResponseEntity.ok(
                tacheService.obtenirTachesAssignees(utilisateurId)
                        .stream().map(this::toDTO).collect(Collectors.toList())
        );
    }

    // POST /api/taches/{colonneId}
    @PostMapping("/{colonneId}")
    public ResponseEntity<TacheDTO> creer(@PathVariable Long colonneId,
                                          @RequestBody Tache tache) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow();
        return ResponseEntity.ok(toDTO(tacheService.creerTache(tache, colonneId, u.getId())));
    }

    // PUT /api/taches/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TacheDTO> modifier(@PathVariable Long id,
                                             @RequestBody Tache tache) {
        return ResponseEntity.ok(toDTO(tacheService.mettreAJourTache(id, tache)));
    }

    // DELETE /api/taches/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        tacheService.supprimerTache(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/taches/{id}/deplacer
    @PatchMapping("/{id}/deplacer")
    public ResponseEntity<TacheDTO> deplacer(@PathVariable Long id,
                                             @RequestParam Long colonneId) {
        return ResponseEntity.ok(toDTO(tacheService.deplacerTache(id, colonneId)));
    }

    // POST /api/taches/{colonneId}/reorganiser
    @PostMapping("/{colonneId}/reorganiser")
    public ResponseEntity<Void> reorganiser(@PathVariable Long colonneId,
                                            @RequestBody List<Long> tacheIds) {
        tacheService.reorganiserTaches(colonneId, tacheIds);
        return ResponseEntity.ok().build();
    }

    // POST /api/taches/{id}/assignes/{assigneId}
    @PostMapping("/{id}/assignes/{assigneId}")
    public ResponseEntity<TacheDTO> ajouterAssigne(@PathVariable Long id,
                                                   @PathVariable Long assigneId) {
        return ResponseEntity.ok(toDTO(tacheService.ajouterAssigne(id, assigneId)));
    }

    // DELETE /api/taches/{id}/assignes/{assigneId}
    @DeleteMapping("/{id}/assignes/{assigneId}")
    public ResponseEntity<TacheDTO> retirerAssigne(@PathVariable Long id,
                                                   @PathVariable Long assigneId) {
        return ResponseEntity.ok(toDTO(tacheService.retirerAssigne(id, assigneId)));
    }

    // PATCH /api/taches/{id}/statut
    @PatchMapping("/{id}/statut")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<TacheDTO> changerStatut(@PathVariable Long id,
                                                  @RequestParam String statut) {
        Tache tache = tacheService.obtenirTacheParIdAvecRelations(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        tache.setStatut(StatutTache.valueOf(statut));
        tacheService.sauvegarderStatut(tache);
        // Recharger avec relations pour le DTO
        Tache tacheComplete = tacheService.obtenirTacheParIdAvecRelations(id)
                .orElseThrow();
        return ResponseEntity.ok(toDTO(tacheComplete));
    }

    // ========== ENDPOINTS POUR LES IMAGES ==========

    // Upload d'une image pour une tâche (Version multipart - pour Postman/cURL)
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<TacheDTO> uploadImage(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Aucune image fournie");
        }

        Tache updatedTache = tacheService.uploadImage(id, file);
        return ResponseEntity.ok(toDTO(updatedTache));
    }

    // Upload d'une image en base64 (pour Swagger)
    @PostMapping("/{id}/upload-image-base64")
    public ResponseEntity<TacheDTO> uploadImageBase64(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        String base64Image = request.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            throw new RuntimeException("Aucune image fournie");
        }

        try {
            // Supprimer le préfixe data:image/png;base64, si présent
            if (base64Image.contains(",")) {
                base64Image = base64Image.split(",")[1];
            }

            // Décoder le base64
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);

            // Créer un MultipartFile à partir du base64
            MultipartFile file = new MockMultipartFile(
                    "image",
                    "image.png",
                    "image/png",
                    imageBytes
            );

            Tache updatedTache = tacheService.uploadImage(id, file);
            return ResponseEntity.ok(toDTO(updatedTache));

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Format base64 invalide", e);
        }
    }

    // Supprimer une image d'une tâche
    @DeleteMapping("/{id}/images/{imageIndex}")
    public ResponseEntity<TacheDTO> deleteImage(
            @PathVariable Long id,
            @PathVariable int imageIndex) {
        Tache updatedTache = tacheService.deleteImage(id, imageIndex);
        return ResponseEntity.ok(toDTO(updatedTache));
    }

    // Récupérer toutes les images d'une tâche
    @GetMapping("/{id}/images")
    public ResponseEntity<List<String>> getTacheImages(@PathVariable Long id) {
        List<String> images = tacheService.getTacheImages(id);
        return ResponseEntity.ok(images);
    }
}