package org.minitrello.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.entity.Tache;
import org.minitrello.backend.entity.Utilisateur;
import org.minitrello.backend.repository.ColonneRepository;
import org.minitrello.backend.repository.ProjetRepository;
import org.minitrello.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TacheControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private ColonneRepository colonneRepository;

    private AuthContext authContext(String prefix) throws Exception {
        String email = prefix + "." + System.nanoTime() + "@example.com";
        String password = "password123";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nom", "Test",
                                "prenom", "User",
                                "email", email,
                                "motDePasse", password,
                                "role", "MEMBRE"
                        ))))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "motDePasse", password
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(loginResponse);
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElseThrow();
        return new AuthContext("Bearer " + json.get("token").asText(), utilisateur);
    }

    private Colonne colonnePour(Utilisateur proprietaire) {
        Projet projet = new Projet();
        projet.setTitre("Projet taches");
        projet.setDescription("Projet pour tester les taches");
        projet.setDateDebut(LocalDate.of(2026, 1, 1));
        projet.setProprietaire(proprietaire);
        projet = projetRepository.save(projet);

        Colonne colonne = new Colonne();
        colonne.setNom("A faire");
        colonne.setCouleur("#0052CC");
        colonne.setPosition(0);
        colonne.setProjet(projet);
        return colonneRepository.save(colonne);
    }

    private void ajouterMembre(Colonne colonne, Utilisateur membre) {
        Projet projet = colonne.getProjet();
        projet.getMembres().add(membre);
        projetRepository.save(projet);
    }

    @Test
    void creerTacheAvecTokenValideRetourneOk() throws Exception {
        AuthContext auth = authContext("create-task");
        Colonne colonne = colonnePour(auth.utilisateur());

        Tache tache = new Tache();
        tache.setTitre("Nouvelle tache");
        tache.setDescription("Details");
        tache.setPriorite(Tache.Priorite.HAUTE);

        mockMvc.perform(post("/api/taches/" + colonne.getId())
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tache)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Nouvelle tache")))
                .andExpect(jsonPath("$.colonneId", equalTo(colonne.getId().intValue())));
    }

    @Test
    void membreDuProjetPeutCreerTache() throws Exception {
        AuthContext proprietaire = authContext("task-owner");
        AuthContext membre = authContext("task-member");
        Colonne colonne = colonnePour(proprietaire.utilisateur());
        ajouterMembre(colonne, membre.utilisateur());

        Tache tache = new Tache();
        tache.setTitre("Tache membre");
        tache.setDescription("Creee par un membre");

        mockMvc.perform(post("/api/taches/" + colonne.getId())
                        .header(HttpHeaders.AUTHORIZATION, membre.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tache)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Tache membre")));
    }

    @Test
    void utilisateurHorsProjetNePeutPasCreerTache() throws Exception {
        AuthContext proprietaire = authContext("task-owner-forbidden");
        AuthContext outsider = authContext("task-outsider");
        Colonne colonne = colonnePour(proprietaire.utilisateur());

        Tache tache = new Tache();
        tache.setTitre("Tache interdite");
        tache.setDescription("Creee par un utilisateur hors projet");

        mockMvc.perform(post("/api/taches/" + colonne.getId())
                        .header(HttpHeaders.AUTHORIZATION, outsider.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tache)))
                .andExpect(status().isForbidden());
    }

    private record AuthContext(String authorization, Utilisateur utilisateur) {
    }
}
