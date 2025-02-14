package com.example.CourseNest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Modules {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long moduleId;

	@Size(min = 5, message = "Title should at least have 5 characters")
	private String title;

	@Size(min = 20, message = "Description should at least have 20 characters")
	private String content;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	private Course course;

	@OneToMany(mappedBy = "module", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<Video> videos;
}
