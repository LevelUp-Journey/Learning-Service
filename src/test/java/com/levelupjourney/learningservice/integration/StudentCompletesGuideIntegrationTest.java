package com.levelupjourney.learningservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.CreateGuideResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.CreatePageResource;
import com.levelupjourney.learningservice.guides.interfaces.rest.resources.UpdateGuideStatusResource;
import com.levelupjourney.learningservice.learningprogress.domain.model.valueobjects.LearningEntityType;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.StartLearningResource;
import com.levelupjourney.learningservice.learningprogress.interfaces.rest.resources.UpdateProgressResource;
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

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Integration Test: Student completes a Guide (Flujo 1)")
public class StudentCompletesGuideIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TestJwtTokenProvider tokenProvider;
    
    private String studentToken;
    private String teacherToken;
    private String studentUserId = "student-123";
    private String teacherUserId = "teacher-456";
    
    @BeforeEach
    void setUp() {
        studentToken = tokenProvider.generateStudentToken(studentUserId);
        teacherToken = tokenProvider.generateTeacherToken(teacherUserId);
    }
    
    @Test
    @DisplayName("Complete flow: Student discovers, starts, tracks progress, and completes a guide")
    void testCompleteGuideFlow() throws Exception {
        // SETUP: Teacher creates a topic
        CreateTopicResource topicResource = new CreateTopicResource("Java Basics", "Learn Java fundamentals");
        MvcResult topicResult = mockMvc.perform(post("/api/v1/topics")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Java Basics"))
                .andReturn();
        
        String topicResponse = topicResult.getResponse().getContentAsString();
        UUID topicId = UUID.fromString(objectMapper.readTree(topicResponse).get("data").get("id").asText());
        
        // SETUP: Teacher creates a guide with pages
        CreateGuideResource guideResource = new CreateGuideResource(
                "Introduction to Java",
                "A comprehensive guide for beginners",
                "https://example.com/cover.jpg",
                Set.of(teacherUserId),
                Set.of(topicId)
        );
        
        MvcResult guideResult = mockMvc.perform(post("/api/v1/guides")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guideResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();
        
        String guideResponse = guideResult.getResponse().getContentAsString();
        UUID guideId = UUID.fromString(objectMapper.readTree(guideResponse).get("data").get("id").asText());
        
        // SETUP: Add 3 pages to the guide
        for (int i = 1; i <= 3; i++) {
            CreatePageResource pageResource = new CreatePageResource(
                    "Content of page " + i,
                    i
            );
            mockMvc.perform(post("/api/v1/guides/" + guideId + "/pages")
                            .header("Authorization", "Bearer " + teacherToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pageResource)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }
        
        // SETUP: Publish the guide
        UpdateGuideStatusResource statusResource = new UpdateGuideStatusResource(EntityStatus.PUBLISHED);
        mockMvc.perform(put("/api/v1/guides/" + guideId + "/status")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusResource)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        // STEP 1: Student gets all available guides (not enrolled in any)
        mockMvc.perform(get("/api/v1/guides")
                        .param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Introduction to Java"));
        
        // STEP 2: Student selects guide and gets all details
        mockMvc.perform(get("/api/v1/guides/" + guideId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Introduction to Java"))
                .andExpect(jsonPath("$.data.pagesCount").value(3));
        
        // STEP 3: System starts learning process for student
        StartLearningResource startLearning = new StartLearningResource(
                studentUserId,
                LearningEntityType.GUIDE,
                guideId
        );
        
        MvcResult progressResult = mockMvc.perform(post("/api/v1/progress")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startLearning)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(studentUserId))
                .andExpect(jsonPath("$.data.entityType").value("GUIDE"))
                .andExpect(jsonPath("$.data.totalItems").value(3))
                .andExpect(jsonPath("$.data.completedItems").value(0))
                .andExpect(jsonPath("$.data.progressPercentage").value(0))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andReturn();
        
        String progressResponse = progressResult.getResponse().getContentAsString();
        UUID progressId = UUID.fromString(objectMapper.readTree(progressResponse).get("data").get("id").asText());
        
        // STEP 4: Student completes page 1 (tracking progress)
        UpdateProgressResource updateProgress1 = new UpdateProgressResource(1, 120L); // 2 minutes
        mockMvc.perform(put("/api/v1/progress/" + progressId)
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProgress1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completedItems").value(1))
                .andExpect(jsonPath("$.data.progressPercentage").value(33))
                .andExpect(jsonPath("$.data.totalReadingTimeSeconds").value(120));
        
        // STEP 5: Student completes page 2
        UpdateProgressResource updateProgress2 = new UpdateProgressResource(2, 150L); // 2.5 minutes more
        mockMvc.perform(put("/api/v1/progress/" + progressId)
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProgress2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completedItems").value(2))
                .andExpect(jsonPath("$.data.progressPercentage").value(66))
                .andExpect(jsonPath("$.data.totalReadingTimeSeconds").value(270));
        
        // STEP 6: Student completes page 3 (final page - automatically completes guide)
        UpdateProgressResource updateProgress3 = new UpdateProgressResource(3, 180L); // 3 minutes more
        mockMvc.perform(put("/api/v1/progress/" + progressId)
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProgress3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.completedItems").value(3))
                .andExpect(jsonPath("$.data.progressPercentage").value(100))
                .andExpect(jsonPath("$.data.totalReadingTimeSeconds").value(450))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt").exists());
        
        // STEP 7: Verify guide is completed
        mockMvc.perform(get("/api/v1/progress")
                        .header("Authorization", "Bearer " + studentToken)
                        .param("userId", studentUserId)
                        .param("entityType", "GUIDE")
                        .param("entityId", guideId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
    
    @Test
    @DisplayName("Error case: Student tries to start learning twice for same guide")
    void testDuplicateStartLearning() throws Exception {
        // Setup guide
        CreateTopicResource topicResource = new CreateTopicResource("Test Topic", "Description");
        MvcResult topicResult = mockMvc.perform(post("/api/v1/topics")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicResource)))
                .andExpect(status().isCreated())
                .andReturn();
        
        UUID topicId = UUID.fromString(objectMapper.readTree(
                topicResult.getResponse().getContentAsString()).get("data").get("id").asText());
        
        CreateGuideResource guideResource = new CreateGuideResource(
                "Test Guide", "Description", null, Set.of(teacherUserId), Set.of(topicId)
        );
        
        MvcResult guideResult = mockMvc.perform(post("/api/v1/guides")
                        .header("Authorization", "Bearer " + teacherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guideResource)))
                .andExpect(status().isCreated())
                .andReturn();
        
        UUID guideId = UUID.fromString(objectMapper.readTree(
                guideResult.getResponse().getContentAsString()).get("data").get("id").asText());
        
        // Start learning first time
        StartLearningResource startLearning = new StartLearningResource(
                studentUserId, LearningEntityType.GUIDE, guideId
        );
        
        mockMvc.perform(post("/api/v1/progress")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startLearning)))
                .andExpect(status().isCreated());
        
        // Try to start again - should return 409 Conflict
        mockMvc.perform(post("/api/v1/progress")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startLearning)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    @DisplayName("Error case: Unauthenticated user cannot start learning")
    void testUnauthenticatedAccess() throws Exception {
        StartLearningResource startLearning = new StartLearningResource(
                studentUserId, LearningEntityType.GUIDE, UUID.randomUUID()
        );

        // Spring Security returns 403 Forbidden for anonymous users (not 401)
        // Note: 401 is returned when credentials are provided but invalid,
        // 403 is returned when no credentials are provided (anonymous access)
        mockMvc.perform(post("/api/v1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startLearning)))
                .andExpect(status().isForbidden()); // Spring Security default for anonymous users
    }
}
