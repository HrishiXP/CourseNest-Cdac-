package com.example.CourseNest.service;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.CourseNest.exception.CourseNotFoundException;
import com.example.CourseNest.model.Course;
import com.example.CourseNest.model.Modules;
import com.example.CourseNest.repository.ModulesRepository;

import jakarta.validation.Valid;

@Service
public class ModuleService {

	@Autowired
	private CourseService courseService;
	@Autowired
	private ModulesRepository modulesRepository;

	private Modules validateModule(Long courseId, Long moduleId) {
		Modules module = modulesRepository.findById(moduleId).orElseThrow(
				() -> new CourseNotFoundException("Module with moduleId : " + moduleId + " does not exist"));

		if (module.getCourse().getCourseId() != courseId) {
			throw new CourseNotFoundException("ModuleId : " + moduleId + " does not belong to Course id " + courseId);
		}
		return module;
	}

	public List<Modules> findAllModulesByCourseId(Long courseId) {
		Course course = courseService.validateCourseById(courseId);
		return course.getModules();
	}

	public ResponseEntity<Modules> save(Long courseId, @Valid Modules module) {
		Course course = courseService.validateCourseById(courseId);
		module.setCourse(course);

		Modules savedModule = modulesRepository.save(module);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(savedModule.getModuleId()).toUri();
		return ResponseEntity.created(location).body(savedModule);
	}

	public Modules findModuleByIdForCourse(Long courseId, Long moduleId) {
		return validateModule(courseId, moduleId);
	}

	public ResponseEntity<Modules> deleteModuleByIdForCourse(Long courseId, Long moduleId) {
		validateModule(courseId, moduleId);
		modulesRepository.deleteById(moduleId);
		return ResponseEntity.noContent().build();
	}

	public Modules updateModuleByIdForCourse(Long courseId, Long moduleId, @Valid Modules module) {
		Modules existingModule = validateModule(courseId, moduleId);
		existingModule.setTitle(module.getTitle());
		existingModule.setContent(module.getContent());
		return modulesRepository.save(existingModule);
	}

}
