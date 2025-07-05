package com.example.demo.controller;

import com.example.demo.dto.GitHubSearchRequest;
import com.example.demo.entity.RepositoryEntity;
import com.example.demo.service.GitHubRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Slf4j
public class GitHubRepositoryController {
    private final GitHubRepositoryService gitHubRepositoryService;

    @PostMapping("/search")
    public ResponseEntity<?> searchRepositories(@RequestBody GitHubSearchRequest request) {
        log.info("Searching repositories with query: {}, language: {}, sort: {}", 
                request.getQuery(), request.getLanguage(), request.getSort());
        
        List<RepositoryEntity> repositories = gitHubRepositoryService.searchAndSaveRepositories(
                request.getQuery(), request.getLanguage(), request.getSort());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SearchResponse("Repositories fetched and saved successfully", repositories));
    }

    @GetMapping("/repositories")
    public ResponseEntity<?> getRepositories(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) Integer minForks,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "stars") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        
        log.info("Getting repositories with filters - language: {}, minStars: {}, minForks: {}, name: {}, sortBy: {}, sortOrder: {}", 
                language, minStars, minForks, name, sortBy, sortOrder);
        
        List<RepositoryEntity> repositories = gitHubRepositoryService.getRepositoriesWithFilters(
                language, minStars, minForks, name, sortBy, sortOrder);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RepositoriesResponse(repositories));
    }

    public static class SearchResponse {
        private final String message;
        private final List<RepositoryEntity> repositories;

        public SearchResponse(String message, List<RepositoryEntity> repositories) {
            this.message = message;
            this.repositories = repositories;
        }

        public String getMessage() { return message; }
        public List<RepositoryEntity> getRepositories() { return repositories; }
    }

    public static class RepositoriesResponse {
        private final List<RepositoryEntity> repositories;

        public RepositoriesResponse(List<RepositoryEntity> repositories) {
            this.repositories = repositories;
        }

        public List<RepositoryEntity> getRepositories() { return repositories; }
    }
} 