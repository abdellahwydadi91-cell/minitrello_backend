package org.minitrello.backend.service;

import org.minitrello.backend.dto.ProjetDTO;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.ProjetRepository;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjetService {

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // ✅ Méthode utilitaire : Projet → ProjetDTO
    private ProjetDTO toDTO(Projet projet) {
        ProjetDTO dto = new ProjetDTO();
        dto.setId(projet.getId());
        dto.setTitre(projet.getTitre());
        dto.setDescription(projet.getDescription());
        dto.setDateDebut(projet.getDateDebut());
        dto.setDateFin(projet.getDateFin());
        dto.setDateCreation(projet.getDateCreation());
        dto.setProprietaireId(projet.getProprietaire().getId());
        dto.setProprietaireNom(projet.getProprietaire().getNom());
        dto.setProprietairePrenom(projet.getProprietaire().getPrenom());
        dto.setMembreIds(
                projet.getMembres().stream()
                        .map(Utilisateur::getId)
                        .collect(Collectors.toList())
        );
        // ✅ AJOUTER — remplir la liste membres complète
        dto.setMembres(
                projet.getMembres().stream()
                        .map(m -> {
                            ProjetDTO.MembreDTO membreDTO = new ProjetDTO.MembreDTO();
                            membreDTO.setId(m.getId());
                            membreDTO.setNom(m.getNom());
                            membreDTO.setPrenom(m.getPrenom());
                            membreDTO.setEmail(m.getEmail());
                            return membreDTO;
                        })
                        .collect(Collectors.toList())
        );
        String emailConnecte = org.springframework.security.core.context
                .SecurityContextHolder.getContext()
                .getAuthentication().getName();
        utilisateurRepository.findByEmail(emailConnecte).ifPresent(u -> {
            if (projet.getProprietaire().getId().equals(u.getId())) {
                dto.setRole("PROPRIETAIRE");
            } else {
                dto.setRole("Membre");
            }
        });
        return dto;
    }

    private Utilisateur findUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Utilisateur non trouvé: " + id));
    }

    private Projet findProjet(Long id) {
        return projetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Projet non trouvé: " + id));
    }

    public ProjetDTO creerProjet(Projet projet, Long proprietaireId) {
        Utilisateur proprietaire = findUtilisateur(proprietaireId);
        projet.setProprietaire(proprietaire);

        if (projet.getColonnes() != null) {
            for (int i = 0; i < projet.getColonnes().size(); i++) {
                projet.getColonnes().get(i).setPosition(i + 1);
                projet.getColonnes().get(i).setProjet(projet);
            }
        }

        return toDTO(projetRepository.save(projet));
    }

    @Transactional(readOnly = true)
    public Optional<ProjetDTO> obtenirProjetParId(Long id) {
        return projetRepository.findByIdWithDetails(id).map(this::toDTO);
    }

    public ProjetDTO mettreAJourProjet(Long id, Projet projetMiseAJour) {
        return projetRepository.findById(id).map(projet -> {
            projet.setTitre(projetMiseAJour.getTitre());
            projet.setDescription(projetMiseAJour.getDescription());
            projet.setDateDebut(projetMiseAJour.getDateDebut());
            projet.setDateFin(projetMiseAJour.getDateFin());
            return toDTO(projetRepository.save(projet));
        }).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Projet non trouvé: " + id));
    }

    public void supprimerProjet(Long id) {
        if (!projetRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Projet non trouvé: " + id);
        }
        projetRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProjetDTO> obtenirProjetsProprietaire(Long proprietaireId) {
        Utilisateur proprietaire = findUtilisateur(proprietaireId);
        return projetRepository.findByProprietaire(proprietaire)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjetDTO> obtenirProjetsByUtilisateur(Long utilisateurId) {
        Utilisateur utilisateur = findUtilisateur(utilisateurId);
        return projetRepository.findProjetsByUtilisateur(utilisateur)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjetDTO> rechercherprojetParTitre(String titre) {
        return projetRepository.findByTitreContainingIgnoreCase(titre)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ProjetDTO ajouterMembreAuProjet(Long projetId, Long membreId) {
        Projet projet = findProjet(projetId);
        Utilisateur membre = findUtilisateur(membreId);
        if (!projet.getMembres().contains(membre)) {
            projet.getMembres().add(membre);
        }
        return toDTO(projetRepository.save(projet));
    }

    public ProjetDTO retirerMembreDuProjet(Long projetId, Long membreId) {
        Projet projet = findProjet(projetId);
        Utilisateur membre = findUtilisateur(membreId);
        projet.getMembres().remove(membre);
        return toDTO(projetRepository.save(projet));
    }

    @Transactional(readOnly = true)
    public boolean verifierAcces(Long projetId, Long utilisateurId) {
        Projet projet = findProjet(projetId);
        Utilisateur utilisateur = findUtilisateur(utilisateurId);
        return projet.getProprietaire().equals(utilisateur)
                || projet.getMembres().contains(utilisateur);
    }

    @Transactional(readOnly = true)
    public List<ProjetDTO> obtenirTousLesProjets() {
        return projetRepository.findAllWithDetails()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
}