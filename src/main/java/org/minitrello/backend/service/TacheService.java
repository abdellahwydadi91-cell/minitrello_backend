package org.minitrello.backend.service;

import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.ColonneRepository;
import org.minitrello.backend.repository.TacheRepository;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TacheService {

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private ColonneRepository colonneRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Value("${file.upload-dir:./uploads/taches/}")
    private String uploadDir;

    // ========== MÉTHODES EXISTANTES ==========

    public Tache creerTache(Tache tache, Long colonneId) {
        Colonne colonne = colonneRepository.findById(colonneId)
                .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
        return creerTacheDansColonne(tache, colonne);
    }

    public Tache creerTache(Tache tache, Long colonneId, Long utilisateurId) {
        Colonne colonne = colonneRepository.findByIdWithProjet(colonneId)
                .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!peutAccederAuProjet(utilisateur, colonne.getProjet())) {
            throw new AccessDeniedException("Vous n'etes pas membre de ce projet");
        }

        return creerTacheDansColonne(tache, colonne);
    }

    private Tache creerTacheDansColonne(Tache tache, Colonne colonne) {
        tache.setColonne(colonne);
        List<Tache> tachesExistantes = tacheRepository.findByColonneOrderByPositionAsc(colonne);
        tache.setPosition(tachesExistantes.size());
        return tacheRepository.save(tache);
    }

    private boolean peutAccederAuProjet(Utilisateur utilisateur, Projet projet) {
        if (projet == null || utilisateur == null || utilisateur.getId() == null) {
            return false;
        }
        if (utilisateur.equals(projet.getProprietaire())) {
            return true;
        }
        return projet.getMembres().contains(utilisateur);
    }

    @Transactional(readOnly = true)
    public Optional<Tache> obtenirTacheParId(Long id) {
        return tacheRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Tache> obtenirTacheParIdAvecRelations(Long id) {
        return tacheRepository.findByIdWithColonneAndProjet(id);
    }

    public void sauvegarderStatut(Tache tache) {
        tacheRepository.save(tache);
    }

    public Tache mettreAJourTache(Long id, Tache tacheMiseAJour) {
        return tacheRepository.findById(id).map(tache -> {
            tache.setTitre(tacheMiseAJour.getTitre());
            tache.setDescription(tacheMiseAJour.getDescription());
            tache.setPriorite(tacheMiseAJour.getPriorite());
            tache.setStatut(tacheMiseAJour.getStatut());
            tache.setDateEcheance(tacheMiseAJour.getDateEcheance());
            return tacheRepository.save(tache);
        }).orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
    }

    public void supprimerTache(Long id) {
        tacheRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Tache> obtenirTachesParColonne(Long colonneId) {
        Colonne colonne = colonneRepository.findByIdWithProjet(colonneId)
                .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
        return tacheRepository.findByColonneWithAssignesOrderByPositionAsc(colonne);
    }

    public Tache deplacerTache(Long tacheId, Long nouvelleColonneId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        Colonne nouvelleColonne = colonneRepository.findById(nouvelleColonneId)
                .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
        tache.setColonne(nouvelleColonne);
        tacheRepository.save(tache);
        // Recharger avec toutes les relations (colonne + projet + assignés)
        return tacheRepository.findByIdWithColonneAndProjet(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée après save"));
    }

    public void reorganiserTaches(Long colonneId, List<Long> tacheIds) {
        Colonne colonne = colonneRepository.findById(colonneId)
                .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
        for (int i = 0; i < tacheIds.size(); i++) {
            Tache tache = tacheRepository.findById(tacheIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
            if (!tache.getColonne().equals(colonne)) {
                throw new RuntimeException("La tâche n'appartient pas à cette colonne");
            }
            tache.setPosition(i);
            tacheRepository.save(tache);
        }
    }

    public Tache ajouterAssigne(Long tacheId, Long assigneId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        Utilisateur assigne = utilisateurRepository.findById(assigneId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (!tache.getAssignes().contains(assigne)) {
            tache.getAssignes().add(assigne);
        }
        return tacheRepository.save(tache);
    }

    public Tache retirerAssigne(Long tacheId, Long assigneId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        Utilisateur assigne = utilisateurRepository.findById(assigneId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        tache.getAssignes().remove(assigne);
        return tacheRepository.save(tache);
    }

    @Transactional(readOnly = true)
    public List<Tache> obtenirTachesAssignees(Long utilisateurId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return tacheRepository.findTachesAssigneesAvecRelations(utilisateur);
    }

    @Transactional(readOnly = true)
    public List<Tache> obtenirTachesEnRetard() {
        return tacheRepository.findByDateEcheanceBeforeAndDateEcheanceIsNotNull(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Tache> rechercherTacheParTitre(String titre) {
        return tacheRepository.findByTitreContainingIgnoreCase(titre);
    }

    @Transactional(readOnly = true)
    public boolean verifierAppartenance(Long tacheId, Long colonneId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));
        Colonne colonne = colonneRepository.findById(colonneId)
                .orElseThrow(() -> new RuntimeException("Colonne non trouvée"));
        return tache.getColonne().equals(colonne);
    }

    // ========== NOUVELLES MÉTHODES POUR LES IMAGES ==========

    public Tache uploadImage(Long tacheId, MultipartFile file) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id: " + tacheId));

        try {
            // Créer le dossier si nécessaire
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Valider le fichier
            if (file.isEmpty()) {
                throw new RuntimeException("Le fichier est vide");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Seules les images sont acceptées");
            }

            // Générer un nom unique pour l'image
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            // Sauvegarder l'image
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // URL accessible pour le frontend
            String imageUrl = "/uploads/taches/" + fileName;
            tache.getImageUrls().add(imageUrl);

            // Sauvegarder la tâche
            tacheRepository.save(tache);

            // ✅ CORRECTION IMPORTANTE : Recharger la tâche avec toutes ses relations
            return tacheRepository.findByIdWithColonneAndProjet(tacheId)
                    .orElseThrow(() -> new RuntimeException("Tâche non trouvée après upload"));

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload de l'image: " + e.getMessage(), e);
        }
    }

    public Tache deleteImage(Long tacheId, int imageIndex) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id: " + tacheId));

        if (imageIndex >= 0 && imageIndex < tache.getImageUrls().size()) {
            String imageUrl = tache.getImageUrls().remove(imageIndex);

            // Supprimer physiquement le fichier
            try {
                String fileName = Paths.get(imageUrl).getFileName().toString();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression du fichier: " + e.getMessage());
            }
        }

        Tache savedTache = tacheRepository.save(tache);

        // ✅ Recharger la tâche avec toutes ses relations
        return tacheRepository.findByIdWithColonneAndProjet(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée après suppression"));
    }

    public List<String> getTacheImages(Long tacheId) {
        Tache tache = tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée avec l'id: " + tacheId));
        return tache.getImageUrls();
    }
}