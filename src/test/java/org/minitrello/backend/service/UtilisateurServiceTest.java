package org.minitrello.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurService utilisateurService;

    @Test
    void creerUtilisateurEncodeLeMotDePasseEtSauvegarde() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("user@example.com");
        utilisateur.setMotDePasse("secret");

        when(utilisateurRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(utilisateurRepository.save(utilisateur)).thenReturn(utilisateur);

        Utilisateur result = utilisateurService.creerUtilisateur(utilisateur);

        assertThat(result.getMotDePasse()).isEqualTo("encoded-secret");
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void creerUtilisateurRefuseUnEmailDejaUtilise() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("user@example.com");

        when(utilisateurRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.creerUtilisateur(utilisateur))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void changerMotDePasseValideAncienMotDePassePuisSauvegarde() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setMotDePasse("old-hash");

        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        utilisateurService.changerMotDePasse(10L, "old-password", "new-password");

        assertThat(utilisateur.getMotDePasse()).isEqualTo("new-hash");
        verify(utilisateurRepository).save(utilisateur);
    }

    @Test
    void changerMotDePasseRefuseUnAncienMotDePasseIncorrect() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setMotDePasse("old-hash");

        when(utilisateurRepository.findById(10L)).thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("bad-password", "old-hash")).thenReturn(false);

        assertThatThrownBy(() -> utilisateurService.changerMotDePasse(10L, "bad-password", "new-password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Ancien mot de passe incorrect");
    }
}
