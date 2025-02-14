package com.example.CourseNest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CourseNest.model.Creator;
import com.example.CourseNest.service.AuthService;
import com.example.CourseNest.service.CreatorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CreatorController {

	@Autowired
	private CreatorService creatorService;

	@Autowired
	private AuthService authService;

	@GetMapping("/creators")
	@PreAuthorize("hasRole('ROLE_CREATOR')")
	public List<Creator> retrieveAllCreators() {
		return this.creatorService.findAll();
	}

	@PostMapping("/creators")
	public ResponseEntity<Creator> createCreator(@Valid @RequestBody Creator creator) {
		return this.creatorService.save(creator);
	}

	@GetMapping("/creators/{id}")
	@PreAuthorize("hasRole('ROLE_CREATOR')")
	public EntityModel<Creator> findCreator(@PathVariable long id) {
		authService.checkCreatorOwnership(id);
		return this.creatorService.findById(id);
	}

	@DeleteMapping("/creators/{id}")
	@PreAuthorize("hasRole('ROLE_CREATOR')")
	public ResponseEntity<Creator> deleteCreator(@PathVariable long id) {
		authService.checkCreatorOwnership(id);
		return this.creatorService.deleteCreatorById(id);
	}

	@PutMapping("/creators/{id}")
	@PreAuthorize("hasRole('ROLE_CREATOR')")
	public Creator updateCreator(@PathVariable long id, @Valid @RequestBody Creator creator) {
		authService.checkCreatorOwnership(id);
		return this.creatorService.updateCreator(id, creator);
	}
}
