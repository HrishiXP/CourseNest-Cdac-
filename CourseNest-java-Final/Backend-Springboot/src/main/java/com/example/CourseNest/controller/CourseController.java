package com.example.CourseNest.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CourseNest.model.Course;
import com.example.CourseNest.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

	@Autowired
	private CourseService courseService;

	@GetMapping("/courses")
	public List<Course> retrieveAllCoursesForUser() {
		return this.courseService.findAll();
	}

	@GetMapping("/{creatorId}/courses")
	public List<Course> retrieveAllCourses(@PathVariable Long creatorId) {
		return this.courseService.findCoursesForCreator(creatorId);
	}

	@PostMapping("/{creatorId}/courses")
	public ResponseEntity<Course> createCourses(
			@PathVariable Long creatorId,
			@Valid @RequestParam("data") String courseData,
			@RequestPart("thumbnailImage") MultipartFile thumbnailImage) throws IOException {

		// Convert the JSON string into a Course object
		ObjectMapper objectMapper = new ObjectMapper();
		Course course = objectMapper.readValue(courseData, Course.class);

		return this.courseService.save(creatorId, course, thumbnailImage);
	}

	@GetMapping("/{creatorId}/courses/{courseId}")
	public ResponseEntity<Course> retrieveCourseForCreator(@PathVariable Long creatorId, @PathVariable Long courseId) {
		Course course = this.courseService.findCourseByCourseId(creatorId, courseId);
		if (course == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(course);
	}

	@DeleteMapping("/{creatorId}/courses/{courseId}")
	public ResponseEntity<Course> deleteCourseForCreator(@PathVariable Long creatorId, @PathVariable Long courseId) {
		return this.courseService.deleteCourseByCourseId(creatorId, courseId);
	}

	@PutMapping("/{creatorId}/courses/{courseId}")
	public Course updateCourseForCreator(
			@PathVariable Long creatorId,
			@PathVariable Long courseId,
			@RequestParam("data") String courseData,
			@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) throws IOException {

		// Convert the JSON string into a Course object
		ObjectMapper objectMapper = new ObjectMapper();
		Course course = objectMapper.readValue(courseData, Course.class);

		return this.courseService.updateCourseByCourseId(creatorId, courseId, course, thumbnailImage);
	}

}
