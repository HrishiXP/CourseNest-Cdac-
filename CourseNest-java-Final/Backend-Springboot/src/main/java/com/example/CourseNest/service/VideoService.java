package com.example.CourseNest.service;

import com.example.CourseNest.exception.ModuleNotFoundException;
import com.example.CourseNest.exception.VideoNotFoundException;
import com.example.CourseNest.model.Modules;
import com.example.CourseNest.model.Video;
import com.example.CourseNest.repository.ModulesRepository;
import com.example.CourseNest.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {

    private final String uploadDir = "C:\\A Java CourseNest\\CourseNest\\src\\main\\resources\\static\\videos\\";

    @Autowired
    private ModulesRepository modulesRepository;

    @Autowired
    private VideoRepository videoRepository;

    public List<Video> findAllVideosByModuleId(Long moduleId) {
        Modules module = modulesRepository.findById(moduleId)
                .orElseThrow(
                        () -> new ModuleNotFoundException("Module with moduleId: " + moduleId + " does not exist"));
        return module.getVideos();
    }

    public ResponseEntity<Video> save(Long moduleId, String title, MultipartFile file) throws IOException {
        Modules module = modulesRepository.findById(moduleId)
                .orElseThrow(
                        () -> new ModuleNotFoundException("Module with moduleId: " + moduleId + " does not exist"));

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.createDirectories(filePath.getParent()); // Ensures the videos directory exists
        Files.copy(file.getInputStream(), filePath);

        Video video = new Video();
        video.setTitle(title);
        video.setFilePath(filePath.toString());
        video.setModule(module);

        Video savedVideo = videoRepository.save(video);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedVideo.getVideoId()).toUri();
        return ResponseEntity.created(location).build();
    }

    public Video findVideoByIdForModule(Long moduleId, Long videoId) {
        return videoRepository.findById(videoId)
                .filter(video -> video.getModule().getModuleId().equals(moduleId))
                .orElseThrow(() -> new VideoNotFoundException("Video with videoId: " + videoId
                        + " does not exist or does not belong to the specified module"));
    }

    public ResponseEntity<Video> deleteVideoByIdForModule(Long moduleId, Long videoId) throws IOException {
        Video video = findVideoByIdForModule(moduleId, videoId);

        // Delete the file from the file system
        Path filePath = Paths.get(video.getFilePath());
        Files.deleteIfExists(filePath);

        // Delete the video record from the database
        videoRepository.delete(video);
        return ResponseEntity.noContent().build();
    }

    public Video updateVideoByIdForModule(Long moduleId, Long videoId, String title, MultipartFile file)
            throws IOException {
        Video existingVideo = findVideoByIdForModule(moduleId, videoId);

        // If a new file is uploaded, delete the old file
        if (file != null && !file.isEmpty()) {
            Path oldFilePath = Paths.get(existingVideo.getFilePath());
            Files.deleteIfExists(oldFilePath);

            // Save the new file
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path newFilePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(newFilePath.getParent()); // Ensures the videos directory exists
            Files.copy(file.getInputStream(), newFilePath);

            existingVideo.setFilePath(newFilePath.toString());
        }

        if (title != null && !title.isEmpty()) {
            existingVideo.setTitle(title);
        }

        return videoRepository.save(existingVideo);
    }
}
