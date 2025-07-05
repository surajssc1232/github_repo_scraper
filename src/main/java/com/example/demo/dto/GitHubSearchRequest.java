package com.example.demo.dto;

import lombok.Data;

@Data
public class GitHubSearchRequest {
    private String query;
    private String language;
    private String sort;
} 