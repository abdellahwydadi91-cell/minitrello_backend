package org.minitrello.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.minitrello.backend.entity.Commentaire;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.CommentaireRepository;
import org.minitrello.backend.repository.TacheRepository;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentaireServiceTest {

    @Mock
    private CommentaireRepository commentaireRepository;

    @Mock
    private TacheRepository tacheRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @InjectMocks
    private CommentaireService commentaireService;

    @Test
    void creerCommentaireAssocieTacheAuteurEtDateCreation() {
        Tache tache = new Tache();
        Utilisateur auteur = new Utilisateur();
        Commentaire commentaire = new Commentaire();

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(auteur));
        when(commentaireRepository.save(commentaire)).thenReturn(commentaire);

        Commentaire result = commentaireService.creerCommentaire(commentaire, 1L, 2L);

        assertThat(result.getTache()).isSameAs(tache);
        assertThat(result.getAuteur()).isSameAs(auteur);
        assertThat(result.getDateCreation()).isNotNull();
        verify(commentaireRepository).save(commentaire);
    }

    @Test
    void mettreAJourCommentaireModifieContenuEtDateModification() {
        Commentaire existant = new Commentaire();
        Commentaire miseAJour = new Commentaire();
        miseAJour.setContenu("Nouveau contenu");

        when(commentaireRepository.findById(1L)).thenReturn(Optional.of(existant));
        when(commentaireRepository.save(existant)).thenReturn(existant);

        LocalDateTime avant = LocalDateTime.now();
        Commentaire result = commentaireService.mettreAJourCommentaire(1L, miseAJour);

        assertThat(result.getContenu()).isEqualTo("Nouveau contenu");
        assertThat(result.getDateModification()).isAfterOrEqualTo(avant);
    }

    @Test
    void verifierProprietaireRetourneVraiPourAuteur() {
        Utilisateur auteur = new Utilisateur();
        auteur.setId(12L);
        Commentaire commentaire = new Commentaire();
        commentaire.setAuteur(auteur);

        when(commentaireRepository.findById(1L)).thenReturn(Optional.of(commentaire));

        assertThat(commentaireService.verifierProprietaire(1L, 12L)).isTrue();
    }

    @Test
    void compterCommentairesDeTacheEchoueSiTacheIntrouvable() {
        when(tacheRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentaireService.compterCommentairesDeTache(9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("T");
    }
}
