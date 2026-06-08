package org.minitrello.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.minitrello.backend.entity.Projet;
import org.minitrello.backend.entity.Utilisateur;
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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

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
                                "role", "CHEF_PROJET"
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

    private Projet projetPour(Utilisateur proprietaire, String titre) {
        Projet projet = new Projet();
        projet.setTitre(titre);
        projet.setDescription("Description test");
        projet.setDateDebut(LocalDate.of(2026, 1, 1));
        projet.setProprietaire(proprietaire);
        return projetRepository.save(projet);
    }

    @Test
    void testCreerProjet() throws Exception {
        AuthContext auth = authContext("create-project");

        Projet projet = new Projet();
        projet.setTitre("Nouveau Projet");
        projet.setDescription("Description test");
        projet.setDateDebut(LocalDate.of(2026, 1, 1));

        mockMvc.perform(post("/api/projets")
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Nouveau Projet")))
                .andExpect(jsonPath("$.proprietaireId", equalTo(auth.utilisateur().getId().intValue())));
    }

    @Test
    void testObtenirProjetParId() throws Exception {
        AuthContext auth = authContext("get-project");
        Projet projet = projetPour(auth.utilisateur(), "Test Projet");

        mockMvc.perform(get("/api/projets/" + projet.getId())
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Test Projet")));
    }

    @Test
    void testMettreAJourProjet() throws Exception {
        AuthContext auth = authContext("update-project");
        Projet projet = projetPour(auth.utilisateur(), "Ancien titre");

        Projet update = new Projet();
        update.setTitre("Nouveau titre");
        update.setDescription("Nouvelle description");

        mockMvc.perform(put("/api/projets/" + projet.getId())
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Nouveau titre")));
    }

    @Test
    void testSupprimerProjet() throws Exception {
        AuthContext auth = authContext("delete-project");
        Projet projet = projetPour(auth.utilisateur(), "Projet a supprimer");

        mockMvc.perform(delete("/api/projets/" + projet.getId())
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testObtenirMesProjets() throws Exception {
        AuthContext auth = authContext("my-projects");
        projetPour(auth.utilisateur(), "Projet 1");

        mockMvc.perform(get("/api/projets")
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testAjouterMembreAuProjet() throws Exception {
        AuthContext auth = authContext("member-owner");
        AuthContext membre = authContext("member-user");
        Projet projet = projetPour(auth.utilisateur(), "Projet membres");

        mockMvc.perform(post("/api/projets/" + projet.getId() + "/membres/" + membre.utilisateur().getId())
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.membreIds", hasSize(1)));
    }

    @Test
    void testObtenirTousLesProjets() throws Exception {
        AuthContext auth = authContext("all-projects");
        projetPour(auth.utilisateur(), "Projet visible");

        mockMvc.perform(get("/api/projets/all")
                        .header(HttpHeaders.AUTHORIZATION, auth.authorization()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    private record AuthContext(String authorization, Utilisateur utilisateur) {
    }
}
