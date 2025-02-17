package com.example.CourseNest.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.CourseNest.model.Video;
import com.example.CourseNest.service.VideoService;

@RestController
@RequestMapping("/api")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @GetMapping("/{moduleId}/videos")
    public List<Video> retrieveAllVideosForModule(@PathVariable Long moduleId) {
        return videoService.findAllVideosByModuleId(moduleId);
    }

    @PostMapping("/{moduleId}/videos")
    public ResponseEntity<Video> uploadVideo(@PathVariable Long moduleId, @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) throws IOException {
        return videoService.save(moduleId, title, file);
    }

    @GetMapping("/{moduleId}/videos/{videoId}")
    public Video retrieveVideoForModule(@PathVariable Long moduleId, @PathVariable Long videoId) {
        return videoService.findVideoByIdForModule(moduleId, videoId);
    }

    @DeleteMapping("/{moduleId}/videos/{videoId}")
    public ResponseEntity<Video> deleteVideoForModule(@PathVariable Long moduleId, @PathVariable Long videoId)
            throws IOException {
        return videoService.deleteVideoByIdForModule(moduleId, videoId);
    }

    @PutMapping("/{moduleId}/videos/{videoId}")
    public Video updateVideoForModule(@PathVariable Long moduleId, @PathVariable Long videoId,
            @RequestParam String title,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        return videoService.updateVideoByIdForModule(moduleId, videoId, title, file);
    }
}
