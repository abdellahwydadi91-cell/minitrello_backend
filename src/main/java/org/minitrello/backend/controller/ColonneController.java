package org.minitrello.backend.controller;

import lombok.RequiredArgsConstructor;
import org.minitrello.backend.dto.ColonneDTO;
import org.minitrello.backend.entity.Colonne;
import org.minitrello.backend.service.ColonneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/colonnes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ColonneController {

    private final ColonneService colonneService;

    // GET /api/colonnes/projet/{projetId}
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<ColonneDTO>> getByProjet(@PathVariable Long projetId) {
        return ResponseEntity.ok(
                colonneService.obtenirColonnesParProjet(projetId)
        );
    }

    // GET /api/colonnes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ColonneDTO> getById(@PathVariable Long id) {
        return colonneService.obtenirColonneParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/colonnes/{projetId}
    @PostMapping("/{projetId}")
    public ResponseEntity<ColonneDTO> creer(@PathVariable Long projetId,
                                            @RequestBody Colonne colonne) {
        return ResponseEntity.ok(
                colonneService.creerColonne(colonne, projetId)
        );
    }

    // PUT /api/colonnes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ColonneDTO> modifier(@PathVariable Long id,
                                               @RequestBody Colonne colonne) {
        return ResponseEntity.ok(
                colonneService.mettreAJourColonne(id, colonne)
        );
    }

    // DELETE /api/colonnes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        colonneService.supprimerColonne(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/colonnes/{projetId}/reorganiser
    @PostMapping("/{projetId}/reorganiser")
    public ResponseEntity<Void> reorganiser(@PathVariable Long projetId,
                                            @RequestBody List<Long> colonneIds) {
        colonneService.reorganiserColonnes(projetId, colonneIds);
        return ResponseEntity.ok().build();
    }
}
