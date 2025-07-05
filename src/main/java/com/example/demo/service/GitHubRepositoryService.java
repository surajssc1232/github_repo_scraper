package com.example.demo.service;

import com.example.demo.entity.RepositoryEntity;
import com.example.demo.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubRepositoryService {
    private final RepositoryRepository repositoryRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${github.api.token:}")
    private String githubApiToken;

    private static final String GITHUB_SEARCH_URL = "https://api.github.com/search/repositories";

    public List<RepositoryEntity> searchAndSaveRepositories(String query, String language, String sort) {
        validateSearchParameters(query, language, sort);
        
        String builtQuery = buildQuery(query, language).replace(" ", "+");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GITHUB_SEARCH_URL)
                .queryParam("q", builtQuery)
                .queryParam("sort", sort != null ? sort : "stars")
                .queryParam("order", "desc")
                .queryParam("per_page", 30);

        log.info("GitHub API URL: {}", builder.toUriString());
        log.info("Query param: {}", builtQuery);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        if (githubApiToken != null && !githubApiToken.isEmpty()) {
            headers.set("Authorization", "Bearer " + githubApiToken);
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            log.info("GitHub API response status: {}", response.getStatusCode());
            
            if (response.getBody() == null) {
                log.warn("Empty response from GitHub API");
                return Collections.emptyList();
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
            if (items == null || items.isEmpty()) {
                log.info("No repositories found for query: {}", builtQuery);
                return Collections.emptyList();
            }

            List<RepositoryEntity> repos = items.stream()
                    .map(this::mapToEntity)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            repos.forEach(repo -> repositoryRepository.save(repo));
            log.info("Successfully saved {} repositories", repos.size());
            return repos;
            
        } catch (Exception e) {
            log.error("Error fetching repositories from GitHub API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch repositories from GitHub API", e);
        }
    }

    public List<RepositoryEntity> getRepositoriesWithFilters(String language, Integer minStars, Integer minForks, String name, String sortBy, String sortOrder) {
        validateFilterParameters(minStars, minForks, sortBy, sortOrder);
        
        List<RepositoryEntity> repositories = repositoryRepository.findByLanguageAndMinStarsAndMinForks(language, minStars, minForks);
        
        if (name != null && !name.isEmpty()) {
            repositories = repositories.stream()
                    .filter(repo -> repo.getName() != null && repo.getName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        Comparator<RepositoryEntity> comparator = getComparator(sortBy, sortOrder);
        repositories.sort(comparator);
        
        log.info("Retrieved {} repositories with filters", repositories.size());
        return repositories;
    }

    private void validateSearchParameters(String query, String language, String sort) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query parameter is required and cannot be empty");
        }
        
        if (sort != null && !isValidSortOption(sort)) {
            throw new IllegalArgumentException("Invalid sort option. Must be one of: stars, forks, updated");
        }
    }

    private void validateFilterParameters(Integer minStars, Integer minForks, String sortBy, String sortOrder) {
        if (minStars != null && minStars < 0) {
            throw new IllegalArgumentException("Minimum stars count cannot be negative");
        }
        
        if (minForks != null && minForks < 0) {
            throw new IllegalArgumentException("Minimum forks count cannot be negative");
        }
        
        if (sortBy != null && !isValidSortOption(sortBy)) {
            throw new IllegalArgumentException("Invalid sort option. Must be one of: stars, forks, updated, name");
        }
        
        if (sortOrder != null && !sortOrder.equalsIgnoreCase("asc") && !sortOrder.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Invalid sort order. Must be 'asc' or 'desc'");
        }
    }

    private boolean isValidSortOption(String sort) {
        return Arrays.asList("stars", "forks", "updated", "name").contains(sort.toLowerCase());
    }

    private Comparator<RepositoryEntity> getComparator(String sortBy, String sortOrder) {
        Comparator<RepositoryEntity> comparator;
        
        switch (sortBy.toLowerCase()) {
            case "forks":
                comparator = Comparator.comparing(RepositoryEntity::getForks, Comparator.nullsLast(Integer::compareTo));
                break;
            case "updated":
                comparator = Comparator.comparing(RepositoryEntity::getLastUpdated, Comparator.nullsLast(ZonedDateTime::compareTo));
                break;
            case "name":
                comparator = Comparator.comparing(RepositoryEntity::getName, Comparator.nullsLast(String::compareTo));
                break;
            default:
                comparator = Comparator.comparing(RepositoryEntity::getStars, Comparator.nullsLast(Integer::compareTo));
                break;
        }
        
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        
        return comparator;
    }

    private String buildQuery(String query, String language) {
        StringBuilder sb = new StringBuilder();
        if (query != null && !query.isEmpty()) {
            sb.append(query);
        }
        if (language != null && !language.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("language:").append(language);
        }
        return sb.toString();
    }

    private RepositoryEntity mapToEntity(Map<String, Object> item) {
        try {
            Map<String, Object> owner = (Map<String, Object>) item.get("owner");
            return RepositoryEntity.builder()
                    .id(((Number) item.get("id")).longValue())
                    .name((String) item.get("name"))
                    .description((String) item.get("description"))
                    .owner(owner != null ? (String) owner.get("login") : null)
                    .language((String) item.get("language"))
                    .stars(item.get("stargazers_count") != null ? ((Number) item.get("stargazers_count")).intValue() : 0)
                    .forks(item.get("forks_count") != null ? ((Number) item.get("forks_count")).intValue() : 0)
                    .lastUpdated(item.get("updated_at") != null ? ZonedDateTime.parse((String) item.get("updated_at")) : null)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping repository item: {}", e.getMessage());
            return null;
        }
    }
} 