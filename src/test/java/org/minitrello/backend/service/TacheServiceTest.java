package org.minitrello.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.ColonneRepository;
import org.minitrello.backend.repository.TacheRepository;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TacheServiceTest {

    @Mock
    private TacheRepository tacheRepository;

    @Mock
    private ColonneRepository colonneRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private TacheService tacheService;

    @Test
    void creerTacheAssigneColonneEtPositionSuivante() {
        Colonne colonne = new Colonne();
        Tache existante = new Tache();
        Tache nouvelle = new Tache();

        when(colonneRepository.findById(1L)).thenReturn(Optional.of(colonne));
        when(tacheRepository.findByColonneOrderByPositionAsc(colonne)).thenReturn(List.of(existante));
        when(tacheRepository.save(nouvelle)).thenReturn(nouvelle);

        Tache result = tacheService.creerTache(nouvelle, 1L);

        assertThat(result.getColonne()).isSameAs(colonne);
        assertThat(result.getPosition()).isEqualTo(1);
    }

    @Test
    void mettreAJourTacheModifieLesChampsEditables() {
        Tache existante = new Tache();
        Tache miseAJour = new Tache();
        miseAJour.setTitre("Nouvelle tache");
        miseAJour.setDescription("Details");
        miseAJour.setPriorite(Tache.Priorite.URGENTE);
        miseAJour.setDateEcheance(LocalDate.of(2026, 6, 1));

        when(tacheRepository.findById(5L)).thenReturn(Optional.of(existante));
        when(tacheRepository.save(existante)).thenReturn(existante);

        Tache result = tacheService.mettreAJourTache(5L, miseAJour);

        assertThat(result.getTitre()).isEqualTo("Nouvelle tache");
        assertThat(result.getDescription()).isEqualTo("Details");
        assertThat(result.getPriorite()).isEqualTo(Tache.Priorite.URGENTE);
        assertThat(result.getDateEcheance()).isEqualTo(LocalDate.of(2026, 6, 1));
    }

    @Test
    void reorganiserTachesMetAJourLesPositions() {
        Colonne colonne = new Colonne();
        Tache premiere = new Tache();
        Tache seconde = new Tache();
        premiere.setColonne(colonne);
        seconde.setColonne(colonne);

        when(colonneRepository.findById(1L)).thenReturn(Optional.of(colonne));
        when(tacheRepository.findById(10L)).thenReturn(Optional.of(premiere));
        when(tacheRepository.findById(20L)).thenReturn(Optional.of(seconde));

        tacheService.reorganiserTaches(1L, List.of(10L, 20L));

        assertThat(premiere.getPosition()).isZero();
        assertThat(seconde.getPosition()).isEqualTo(1);
        verify(tacheRepository).save(premiere);
        verify(tacheRepository).save(seconde);
    }

    @Test
    void reorganiserTachesRefuseUneTacheDUneAutreColonne() {
        Colonne colonne = new Colonne();
        Colonne autreColonne = new Colonne();
        colonne.setId(1L);
        autreColonne.setId(2L);
        Tache tache = new Tache();
        tache.setColonne(autreColonne);

        when(colonneRepository.findById(1L)).thenReturn(Optional.of(colonne));
        when(tacheRepository.findById(10L)).thenReturn(Optional.of(tache));

        assertThatThrownBy(() -> tacheService.reorganiserTaches(1L, List.of(10L)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("colonne");
    }

    @Test
    void ajouterAssigneEviteLesDoublons() {
        Tache tache = new Tache();
        Utilisateur assigne = new Utilisateur();
        tache.getAssignes().add(assigne);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(assigne));
        when(tacheRepository.save(tache)).thenReturn(tache);

        Tache result = tacheService.ajouterAssigne(1L, 2L);

        assertThat(result.getAssignes()).containsExactly(assigne);
        verify(tacheRepository).save(tache);
    }

    @Test
    void obtenirTachesEnRetardUtiliseLaDateDuJour() {
        Tache tache = new Tache();
        when(tacheRepository.findByDateEcheanceBeforeAndDateEcheanceIsNotNull(any(LocalDate.class)))
                .thenReturn(List.of(tache));

        assertThat(tacheService.obtenirTachesEnRetard()).containsExactly(tache);
    }
}
