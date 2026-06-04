package com.klaye.monolith.dossier.controller;

import com.klaye.monolith.dossier.dto.DossierCreateRequest;
import com.klaye.monolith.dossier.dto.DossierUpdateRequest;
import com.klaye.monolith.dossier.dto.DossierResponse;
import com.klaye.monolith.dossier.service.DossierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur pour la gestion des Dossiers clients.
 * Migré depuis Dossier-Service vers le Monolithe.
 */
@RestController
@RequestMapping("/api/v1/dossiers")
@RequiredArgsConstructor
public class DossierController {

    private final DossierService dossierService;

    /**
     * Crée un nouveau dossier.
     */
    @PostMapping
    @PreAuthorize("hasPermission(null, 'Dossier', 'WRITE')")
    public ResponseEntity<DossierResponse> create(@Valid @RequestBody DossierCreateRequest request) {
        return new ResponseEntity<>(dossierService.create(request), HttpStatus.CREATED);
    }

    /**
     * Liste tous les dossiers.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    //@PreAuthorize("hasPermission(null, 'Dossier', 'READ')")
    public ResponseEntity<List<DossierResponse>> getAll() {
        return ResponseEntity.ok(dossierService.findAll());
    }

    /**
     * Récupère un dossier par son code unique.
     */
    @GetMapping(path = "/{codeDossier}",produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission(#codeDossier, 'Dossier', 'READ')")
    public ResponseEntity<DossierResponse> getByCode(@PathVariable String codeDossier) {
        return ResponseEntity.ok(dossierService.findByCode(codeDossier));
    }

    /**
     * Met à jour un dossier.
     */
    @PutMapping("/{codeDossier}")
    @PreAuthorize("hasPermission(#codeDossier, 'Dossier', 'WRITE')")
    public ResponseEntity<DossierResponse> update(@PathVariable String codeDossier, @Valid @RequestBody DossierUpdateRequest request) {
        return ResponseEntity.ok(dossierService.update(codeDossier, request));
    }

    /**
     * Supprime un dossier.
     */
    @DeleteMapping("/{codeDossier}")
    @PreAuthorize("hasPermission(#codeDossier, 'Dossier', 'DELETE')")
    public ResponseEntity<Void> delete(@PathVariable String codeDossier) {
        dossierService.delete(codeDossier);
        return ResponseEntity.noContent().build();
    }
}
