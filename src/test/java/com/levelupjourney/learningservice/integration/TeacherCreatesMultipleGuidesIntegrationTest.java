package com.levelupjourney.learningservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.CreateGuideResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.CreatePageResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.UpdateGuideStatusResource;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.shared.infrastructure.security.TestJwtTokenProvider;
import com.levelupjourney.learningservice.topics.interfaces.rest.resources.CreateTopicResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Test: Teacher creates multiple guides for course preparation")
public class TeacherCreatesMultipleGuidesIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TestJwtTokenProvider tokenProvider;
    
    private String teacherToken;
    private String teacher2Token;
    private String studentToken;
    private String teacherUserId = "teacher-001";
    private String teacher2UserId = "teacher-002";
    private String studentUserId = "student-001";
    
    @BeforeEach
    void setUp() {
        teacherToken = tokenProvider.generateTeacherToken(teacherUserId);
        teacher2Token = tokenProvider.generateTeacherToken(teacher2UserId);
        studentToken = tokenProvider.generateStudentToken(studentUserId);
    }
    
    @Test
    @DisplayName("Teacher creates multiple topics and guides for different subjects")
    void testTeacherCreatesMultipleGuides() throws Exception {
        // Step 1: Create multiple topics
        List<UUID> topicIds = new ArrayList<>();
        String[] topicNames = {"Java Fundamentals", "Spring Framework", "Database Design", "Web Development"};
        
        for (String topicName : topicNames) {
            CreateTopicResource topicResource = new CreateTopicResource(topicName);
            
            MvcResult result = mockMvc.perform(post("/api/v1/topics")
                            .header("Authorization", "Bearer " + teacherToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(topicResource)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value(topicName))
                    .andReturn();
            
            String response = result.getResponse().getContentAsString();
            UUID topicId = UUID.fromString(objectMapper.readTree(response).get("data").get("id").asText());
            topicIds.add(topicId);
        }
        
        // Step 2: Create Java Fundamentals Guide with 5 pages
        UUID javaTopicId = topicIds.get(0);
        UUID javaGuideId = createGuideWithPages(
                "Java Basics - Variables and Data Types",
                "Master Java variables, primitive types, and reference types",
                "https://example.com/java-basics.jpg",
                Set.of(teacherUserId),
                Set.of(javaTopicId),
                5,
                teacherToken
        );
        
        // Publish the Java guide
        publishGuide(javaGuideId, teacherToken);
        
        // Step 3: Create Spring Framework Guide with 7 pages (multi-author)
        UUID springTopicId = topicIds.get(1);
        UUID springGuideId = createGuideWithPages(
                "Spring Boot Essentials",
                "Learn Spring Boot fundamentals, dependency injection, and REST APIs",
                "https://example.com/spring-boot.jpg",
                Set.of(teacherUserId, teacher2UserId),
                Set.of(springTopicId),
                7,
                teacherToken
        );
        
        // Publish the Spring guide
        publishGuide(springGuideId, teacherToken);
        
        // Step 4: Create Database Guide with 4 pages
        UUID dbTopicId = topicIds.get(2);
        UUID dbGuideId = createGuideWithPages(
                "SQL and Relational Databases",
                "Understanding tables, relationships, queries, and transactions",
                "https://example.com/database.jpg",
                Set.of(teacher2UserId),
                Set.of(dbTopicId),
                4,
                teacher2Token
        );
        
        // Publish the Database guide
        publishGuide(dbGuideId, teacher2Token);
        
        // Step 5: Create Web Development Guide (cross-topic)
        Set<UUID> webTopics = Set.of(topicIds.get(1), topicIds.get(3));
        UUID webGuideId = createGuideWithPages(
                "Building Web Applications with Spring MVC",
                "Create dynamic web applications using Spring MVC, Thymeleaf, and REST",
                "https://example.com/web-dev.jpg",
                Set.of(teacherUserId, teacher2UserId),
                webTopics,
                6,
                teacherToken
        );
        
        // Publish the Web guide
        publishGuide(webGuideId, teacherToken);
        
        // Step 6: Create an Advanced Guide (keep as DRAFT for course association)
        UUID advancedGuideId = createGuideWithPages(
                "Advanced Java - Concurrency and Streams",
                "Deep dive into Java concurrency, parallel streams, and functional programming",
                "https://example.com/advanced-java.jpg",
                Set.of(teacherUserId),
                Set.of(javaTopicId),
                8,
                teacherToken
        );
        // Leave as DRAFT for course association
        
        // Step 7: Verify published guides are visible to students
        mockMvc.perform(get("/api/v1/guides")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[?(@.status == 'PUBLISHED')]").isNotEmpty());
        
        // Step 8: Verify student can access published guide details
        mockMvc.perform(get("/api/v1/guides/" + javaGuideId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(javaGuideId.toString()))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.pagesCount").value(5));
        
        // Step 9: Verify student can see pages of published guide
        mockMvc.perform(get("/api/v1/guides/" + javaGuideId + "/pages")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(5));
        
        // Step 10: Verify student CANNOT access DRAFT guide (returns 404 to avoid information disclosure)
        mockMvc.perform(get("/api/v1/guides/" + advancedGuideId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
        
        // Step 11: Verify teacher can access their DRAFT guide
        mockMvc.perform(get("/api/v1/guides/" + advancedGuideId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
        
        // Step 12: Search guides by topic
        mockMvc.perform(get("/api/v1/guides")
                        .param("topicIds", javaTopicId.toString())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
        
        // Step 13: Get guides by author
        mockMvc.perform(get("/api/v1/guides")
                        .param("authorIds", teacherUserId)
                        .header("Authorization", "Bearer " + teacherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
    
    @Test
    @DisplayName("Verify guide with maximum authors (5)")
    void testGuideWithMaxAuthors() throws Exception {
        // Create topic
        CreateTopicResource topicResource = new CreateTopicResource("Team Project");
        
        MvcResult topicResult = mockMvc.perform(post("/api/v1/topics")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicResource)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String topicResponse = topicResult.getResponse().getContentAsString();
        UUID topicId = UUID.fromString(objectMapper.readTree(topicResponse).get("data").get("id").asText());
        
        // Create guide with 5 authors (max allowed)
        Set<String> authorIds = Set.of("teacher-001", "teacher-002", "teacher-003", "teacher-004", "teacher-005");
        CreateGuideResource guideResource = new CreateGuideResource(
                "Team Collaboration Guide",
                "A guide created by multiple authors",
                "https://example.com/team.jpg",
                authorIds,
                Set.of(topicId)
        );
        
        mockMvc.perform(post("/api/v1/guides")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guideResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.authorIds").isArray())
                .andExpect(jsonPath("$.data.authorIds.length()").value(5));
    }
    
    @Test
    @DisplayName("Verify student cannot create guides")
    void testStudentCannotCreateGuide() throws Exception {
        // Try to create topic as student (should fail)
        CreateTopicResource topicResource = new CreateTopicResource("Unauthorized Topic");
        
        mockMvc.perform(post("/api/v1/topics")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicResource)))
                .andExpect(status().isForbidden());
    }
    
    // Helper method to create a guide with pages
    private UUID createGuideWithPages(
            String title, 
            String description, 
            String coverImage,
            Set<String> authorIds,
            Set<UUID> topicIds,
            int pageCount,
            String authToken
    ) throws Exception {
        // Create guide
        CreateGuideResource guideResource = new CreateGuideResource(
                title,
                description,
                coverImage,
                authorIds,
                topicIds
        );
        
        MvcResult guideResult = mockMvc.perform(post("/api/v1/guides")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guideResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        
        String guideResponse = guideResult.getResponse().getContentAsString();
        UUID guideId = UUID.fromString(objectMapper.readTree(guideResponse).get("data").get("id").asText());
        
        // Add pages
        for (int i = 1; i <= pageCount; i++) {
            CreatePageResource pageResource = new CreatePageResource(
                    String.format("# Page %d\n\nThis is the content of page %d. It covers important topics related to %s.", 
                            i, i, title),
                    i
            );
            
            mockMvc.perform(post("/api/v1/guides/" + guideId + "/pages")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pageResource)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
        
        return guideId;
    }
    
    // Helper method to publish a guide
    private void publishGuide(UUID guideId, String authToken) throws Exception {
        UpdateGuideStatusResource statusResource = new UpdateGuideStatusResource(EntityStatus.PUBLISHED);
        
        mockMvc.perform(put("/api/v1/guides/" + guideId + "/status")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }
}
