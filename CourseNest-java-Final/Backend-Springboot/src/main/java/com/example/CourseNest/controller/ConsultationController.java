package com.example.CourseNest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CourseNest.model.Consultation;
import com.example.CourseNest.service.ConsultationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @GetMapping("/consultations")
    public List<Consultation> retrieveAllConsultations() {
        return consultationService.findAll();
    }

    @GetMapping("/{creatorId}/consultations")
    public List<Consultation> retrieveConsultationForCreator(@PathVariable Long creatorId) {
        return consultationService.findConsultationForCreator(creatorId);
    }

    @PostMapping("/{creatorId}/consultations")
    public ResponseEntity<Consultation> createConsultationForCreator(@PathVariable Long creatorId,
            @Valid @RequestBody Consultation consultation) {
        return consultationService.save(creatorId, consultation);
    }

    @GetMapping("/{creatorId}/consultations/{consultationId}")
    public Consultation retrieveConsultationForCreator(@PathVariable Long creatorId,
            @PathVariable Long consultationId) {
        return consultationService.findConsultationByCreatorId(creatorId, consultationId);
    }

    @DeleteMapping("/{creatorId}/consultations/{consultationId}")
    public ResponseEntity<Consultation> deleteConsultationForCreator(@PathVariable Long creatorId,
            @PathVariable Long consultationId) {
        return consultationService.deleteConsultationForCreator(creatorId, consultationId);
    }

    @PutMapping("/{creatorId}/consultations/{consultationId}")
    public Consultation updateConsultationForCreator(@PathVariable Long creatorId,
            @PathVariable Long consultationId, @Valid @RequestBody Consultation consultation) {
        return consultationService.updateConsultationForCreator(creatorId, consultationId, consultation);
    }
}
