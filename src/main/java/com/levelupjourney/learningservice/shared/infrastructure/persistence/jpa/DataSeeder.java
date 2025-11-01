package com.levelupjourney.learningservice.shared.infrastructure.persistence.jpa;

import com.levelupjourney.learningservice.guides.domain.model.aggregates.Guide;
import com.levelupjourney.learningservice.guides.domain.model.aggregates.Page;
import com.levelupjourney.learningservice.guides.infrastructure.persistence.jpa.repositories.GuideRepository;
import com.levelupjourney.learningservice.shared.domain.model.EntityStatus;
import com.levelupjourney.learningservice.topics.domain.model.aggregates.Topic;
import com.levelupjourney.learningservice.topics.infrastructure.persistence.jpa.repositories.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final TopicRepository topicRepository;
    private final GuideRepository guideRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🌱 Starting data seeding...");

        // Seed topics
        seedTopics();

        // Seed guides
        seedGuides();

        log.info("✅ Data seeding completed!");
    }

    private void seedTopics() {
        if (topicRepository.count() > 0) {
            log.info("Topics already exist, skipping seeding");
            return;
        }

        List<String> topicNames = Arrays.asList(
            "Java", "Spring Boot", "Microservices", "Docker", "Kubernetes",
            "React", "Angular", "Vue.js", "Node.js", "Python",
            "Machine Learning", "Data Science", "DevOps", "AWS", "Azure",
            "JavaScript", "TypeScript", "SQL", "NoSQL", "Git"
        );

        for (String topicName : topicNames) {
            Topic topic = new Topic(topicName);
            topicRepository.save(topic);
        }

        log.info("Seeded {} topics", topicNames.size());
    }

    private void seedGuides() {
        if (guideRepository.count() > 0) {
            log.info("Guides already exist, skipping seeding");
            return;
        }

        // Get some topics
        List<Topic> topics = topicRepository.findAll();
        if (topics.isEmpty()) {
            log.warn("No topics found, skipping guide seeding");
            return;
        }

        // Sample guide data
        List<GuideData> guideDataList = Arrays.asList(
            new GuideData(
                "Introduction to Spring Boot",
                "Learn the basics of Spring Boot framework",
                Arrays.asList("Spring Boot", "Java"),
                "286ae104-a964-460c-ad5d-f1ab94426e86",
                EntityStatus.PUBLISHED,
                "Spring Boot is a powerful framework for building Java applications..."
            ),
            new GuideData(
                "Docker Fundamentals",
                "Master containerization with Docker",
                Arrays.asList("Docker", "DevOps"),
                "286ae104-a964-460c-ad5d-f1ab94426e86",
                EntityStatus.PUBLISHED,
                "Docker is a platform for developing, shipping, and running applications..."
            ),
            new GuideData(
                "React Best Practices",
                "Advanced React development techniques",
                Arrays.asList("React", "JavaScript"),
                "286ae104-a964-460c-ad5d-f1ab94426e86",
                EntityStatus.DRAFT,
                "React is a popular JavaScript library for building user interfaces..."
            ),
            new GuideData(
                "Python Data Analysis",
                "Analyze data with Python and pandas",
                Arrays.asList("Python", "Data Science"),
                "286ae104-a964-460c-ad5d-f1ab94426e86",
                EntityStatus.PUBLISHED,
                "Python is an excellent language for data analysis..."
            )
        );

        for (GuideData guideData : guideDataList) {
            Set<Topic> guideTopics = guideData.topicNames.stream()
                .map(topicName -> topics.stream()
                    .filter(t -> t.getName().equals(topicName))
                    .findFirst()
                    .orElse(null))
                .filter(t -> t != null)
                .collect(java.util.stream.Collectors.toSet());

            if (guideTopics.isEmpty()) {
                log.warn("No matching topics found for guide: {}", guideData.title);
                continue;
            }

            // Create guide using constructor
            Guide guide = new Guide(
                guideData.title,
                guideData.description,
                null, // coverImage
                Set.of(guideData.authorId), // authorIds
                guideTopics
            );

            // Set status
            guide.updateStatus(guideData.status);

            // Add a sample page
            Page page = new Page(guide, guideData.sampleContent, 1);
            guide.getPages().add(page);

            guideRepository.save(guide);
        }

        log.info("Seeded {} guides", guideDataList.size());
    }

    private static class GuideData {
        final String title;
        final String description;
        final List<String> topicNames;
        final String authorId;
        final EntityStatus status;
        final String sampleContent;

        GuideData(String title, String description, List<String> topicNames,
                 String authorId, EntityStatus status, String sampleContent) {
            this.title = title;
            this.description = description;
            this.topicNames = topicNames;
            this.authorId = authorId;
            this.status = status;
            this.sampleContent = sampleContent;
        }
    }
}