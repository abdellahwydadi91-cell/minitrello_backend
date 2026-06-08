package org.minitrello.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.minitrello.backend.dto.ColonneDTO;
import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.repository.ColonneRepository;
import org.minitrello.backend.repository.ProjetRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ColonneServiceTest {

    @Mock
    private ColonneRepository colonneRepository;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private ColonneService colonneService;

    @Test
    void creerColonneAssigneProjetEtPositionSuivante() {
        Projet projet = new Projet();
        projet.setId(1L);
        Colonne existante = new Colonne();
        Colonne nouvelle = new Colonne();
        nouvelle.setNom("Done");

        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(colonneRepository.existsByNomAndProjet("Done", projet)).thenReturn(false);
        when(colonneRepository.findByProjetOrderByPositionAsc(projet)).thenReturn(List.of(existante));
        when(colonneRepository.save(nouvelle)).thenReturn(nouvelle);

        ColonneDTO result = colonneService.creerColonne(nouvelle, 1L);

        assertThat(result.getProjetId()).isEqualTo(1L);
        assertThat(result.getPosition()).isEqualTo(1);
        assertThat(result.getNom()).isEqualTo("Done");
        verify(colonneRepository).save(nouvelle);
    }

    @Test
    void creerColonneRefuseUnNomDejaPresentDansLeProjet() {
        Projet projet = new Projet();
        Colonne colonne = new Colonne();
        colonne.setNom("Todo");

        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(colonneRepository.existsByNomAndProjet("Todo", projet)).thenReturn(true);

        assertThatThrownBy(() -> colonneService.creerColonne(colonne, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("colonne");
    }

    @Test
    void reorganiserColonnesMetAJourLesPositions() {
        Projet projet = new Projet();
        Colonne premiere = new Colonne();
        Colonne seconde = new Colonne();
        premiere.setProjet(projet);
        seconde.setProjet(projet);

        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(colonneRepository.findById(10L)).thenReturn(Optional.of(premiere));
        when(colonneRepository.findById(20L)).thenReturn(Optional.of(seconde));

        colonneService.reorganiserColonnes(1L, List.of(10L, 20L));

        assertThat(premiere.getPosition()).isZero();
        assertThat(seconde.getPosition()).isEqualTo(1);
        verify(colonneRepository).save(premiere);
        verify(colonneRepository).save(seconde);
    }

    @Test
    void reorganiserColonnesRefuseUneColonneDUnAutreProjet() {
        Projet projet = new Projet();
        Projet autreProjet = new Projet();
        projet.setId(1L);
        autreProjet.setId(2L);
        Colonne colonne = new Colonne();
        colonne.setProjet(autreProjet);

        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(colonneRepository.findById(10L)).thenReturn(Optional.of(colonne));

        assertThatThrownBy(() -> colonneService.reorganiserColonnes(1L, List.of(10L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("colonne");
    }
}
