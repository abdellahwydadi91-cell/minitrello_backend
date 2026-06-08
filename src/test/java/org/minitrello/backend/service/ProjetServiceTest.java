package org.minitrello.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.minitrello.backend.dto.ProjetDTO;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.ProjetRepository;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetServiceTest {

    @Mock
    private ProjetRepository projetRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private ProjetService projetService;

    @Test
    void creerProjetAssocieLeProprietaire() {
        Utilisateur proprietaire = new Utilisateur();
        proprietaire.setId(1L);
        Projet projet = new Projet();
        projet.setTitre("Test Projet");

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(proprietaire));
        when(projetRepository.save(projet)).thenReturn(projet);

        ProjetDTO result = projetService.creerProjet(projet, 1L);

        assertThat(result.getProprietaireId()).isEqualTo(1L);
        assertThat(result.getTitre()).isEqualTo("Test Projet");
        verify(projetRepository).save(projet);
    }

    @Test
    void mettreAJourProjetModifieLesChampsEditables() {
        Utilisateur proprietaire = new Utilisateur();
        proprietaire.setId(1L);
        Projet existant = new Projet();
        existant.setId(5L);
        existant.setProprietaire(proprietaire);
        Projet miseAJour = new Projet();
        miseAJour.setTitre("Nouveau titre");
        miseAJour.setDescription("Nouvelle description");
        miseAJour.setDateDebut(LocalDate.of(2026, 1, 1));
        miseAJour.setDateFin(LocalDate.of(2026, 2, 1));

        when(projetRepository.findById(5L)).thenReturn(Optional.of(existant));
        when(projetRepository.save(existant)).thenReturn(existant);

        ProjetDTO result = projetService.mettreAJourProjet(5L, miseAJour);

        assertThat(result.getTitre()).isEqualTo("Nouveau titre");
        assertThat(result.getDescription()).isEqualTo("Nouvelle description");
        assertThat(result.getDateDebut()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.getDateFin()).isEqualTo(LocalDate.of(2026, 2, 1));
    }

    @Test
    void ajouterMembreAuProjetEviteLesDoublons() {
        Projet projet = new Projet();
        projet.setId(2L);
        Utilisateur proprietaire = new Utilisateur();
        proprietaire.setId(1L);
        projet.setProprietaire(proprietaire);
        Utilisateur membre = new Utilisateur();
        membre.setId(3L);
        projet.getMembres().add(membre);

        when(projetRepository.findById(2L)).thenReturn(Optional.of(projet));
        when(utilisateurRepository.findById(3L)).thenReturn(Optional.of(membre));
        when(projetRepository.save(projet)).thenReturn(projet);

        ProjetDTO result = projetService.ajouterMembreAuProjet(2L, 3L);

        assertThat(result.getMembreIds()).contains(3L);
        verify(projetRepository).save(projet);
    }

    @Test
    void verifierAccesRetourneVraiPourProprietaireOuMembre() {
        Utilisateur proprietaire = new Utilisateur();
        proprietaire.setId(8L);
        Utilisateur membre = new Utilisateur();
        membre.setId(9L);
        Projet projet = new Projet();
        projet.setProprietaire(proprietaire);
        projet.getMembres().add(membre);

        when(projetRepository.findById(8L)).thenReturn(Optional.of(projet));
        when(utilisateurRepository.findById(9L)).thenReturn(Optional.of(membre));

        assertThat(projetService.verifierAcces(8L, 9L)).isTrue();
    }

    @Test
    void creerProjetEchoueSiProprietaireIntrouvable() {
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.creerProjet(new Projet(), 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Utilisateur");
        verify(projetRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void obtenirProjetsProprietaireDelegueAuRepository() {
        Utilisateur proprietaire = new Utilisateur();
        proprietaire.setId(1L);
        Projet projet = new Projet();
        projet.setTitre("Projet Test");
        projet.setProprietaire(proprietaire);

        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(proprietaire));
        when(projetRepository.findByProprietaire(proprietaire)).thenReturn(List.of(projet));

        List<ProjetDTO> result = projetService.obtenirProjetsProprietaire(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitre()).isEqualTo("Projet Test");
    }
}
