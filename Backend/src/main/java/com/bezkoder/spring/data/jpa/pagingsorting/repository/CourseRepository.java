package com.bezkoder.spring.data.jpa.pagingsorting.repository;

import com.bezkoder.spring.data.jpa.pagingsorting.model.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger; // For generating new IDs
import java.util.stream.Collectors;

// Spring Data classes for pagination and sorting (not tied to a specific database)
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@Repository
public class CourseRepository {

    private List<Course> courses;
    private final ObjectMapper objectMapper;
    private AtomicInteger idCounter = new AtomicInteger(0); // For generating new IDs

    public CourseRepository() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("sample-courses.json");
            try (InputStream inputStream = resource.getInputStream()) {
                this.courses = objectMapper.readValue(inputStream, new TypeReference<List<Course>>() {});
                System.out.println("Loaded " + courses.size() + " courses from sample-courses.json");

                // Initialize ID counter to be greater than max existing ID for new courses
                this.courses.stream().mapToInt(Course::getId).max().ifPresent(maxId -> idCounter.set(maxId + 1));

            }
        } catch (IOException e) {
            System.err.println("Failed to load courses from sample-courses.json: " + e.getMessage());
            this.courses = new ArrayList<>();
        }
    }

    public List<Course> findAll() {
        return new ArrayList<>(courses); // Return a copy
    }

    public Optional<Course> findById(int id) { // ID is int now
        return courses.stream()
                      .filter(course -> course.getId() == id)
                      .findFirst();
    }

    // Used by /sortedcourses endpoint
    public List<Course> findByTitleContaining(String title, Sort sort) {
        List<Course> filteredCourses = courses.stream()
                .filter(course -> course.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
        return applySort(filteredCourses, sort);
    }

    // Used by /courses/type/{type} endpoint
    public Page<Course> findByType(String type, Pageable pageable) {
        List<Course> filteredCourses = courses.stream()
                .filter(course -> course.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        filteredCourses = applySort(filteredCourses, pageable.getSort()); // Apply sorting

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCourses.size());

        List<Course> pagedCourses = new ArrayList<>();
        if (start < end) {
            pagedCourses = filteredCourses.subList(start, end);
        }
        return new PageImpl<>(pagedCourses, pageable, filteredCourses.size());
    }

    public Course save(Course course) {
        if (course.getId() == 0) { // If ID is 0 (default int value), it's a new course
            course.setId(idCounter.getAndIncrement()); // Assign a new unique ID
            this.courses.add(course);
        } else {
            // Find and update existing course
            Optional<Course> existingCourseOpt = findById(course.getId());
            if (existingCourseOpt.isPresent()) {
                Course existingCourse = existingCourseOpt.get();
                existingCourse.setTitle(course.getTitle());
                existingCourse.setDescription(course.getDescription());
                existingCourse.setCategory(course.getCategory());
                existingCourse.setType(course.getType());
                existingCourse.setGradeRange(course.getGradeRange());
                existingCourse.setMinAge(course.getMinAge());
                existingCourse.setMaxAge(course.getMaxAge());
                existingCourse.setPrice(course.getPrice());
                existingCourse.setNextSessionDate(course.getNextSessionDate());
            } else {
                this.courses.add(course); // Add if ID provided but not found
            }
        }
        return course;
    }

    public void deleteById(int id) { // ID is int now
        courses.removeIf(course -> course.getId() == id);
    }

    public void deleteAll() {
        this.courses.clear();
        this.idCounter.set(1); // Reset ID counter
    }

    // Helper method to apply sorting based on Spring Data Sort object
    public List<Course> applySort(List<Course> list, Sort sort) {
        if (sort == null || !sort.iterator().hasNext()) {
            return list;
        }

        Comparator<Course> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Course> currentComparator = null;
            switch (order.getProperty()) {
                case "id":
                    currentComparator = Comparator.comparingInt(Course::getId); // Use comparingInt for int
                    break;
                case "title":
                    currentComparator = Comparator.comparing(Course::getTitle);
                    break;
                case "category":
                    currentComparator = Comparator.comparing(Course::getCategory);
                    break;
                case "type":
                    currentComparator = Comparator.comparing(Course::getType);
                    break;
                case "minAge":
                    currentComparator = Comparator.comparingInt(Course::getMinAge);
                    break;
                case "maxAge":
                    currentComparator = Comparator.comparingInt(Course::getMaxAge);
                    break;
                case "price":
                    currentComparator = Comparator.comparingDouble(Course::getPrice); // Use comparingDouble for double
                    break;
                case "nextSessionDate":
                    currentComparator = Comparator.comparing(Course::getNextSessionDate);
                    break;
                default:
                    continue;
            }

            if (currentComparator != null) {
                if (order.isDescending()) {
                    currentComparator = currentComparator.reversed();
                }
                if (comparator == null) {
                    comparator = currentComparator;
                } else {
                    comparator = comparator.thenComparing(currentComparator);
                }
            }
        }

        if (comparator != null) {
            list.sort(comparator);
        }
        return list;
    }

    // --- Pagination helpers for controller (simplified) ---
    public Page<Course> findAll(Pageable pageable) {
        List<Course> allCourses = new ArrayList<>(courses);
        allCourses = applySort(allCourses, pageable.getSort());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allCourses.size());

        List<Course> pagedCourses = new ArrayList<>();
        if (start < end) {
            pagedCourses = allCourses.subList(start, end);
        }
        return new PageImpl<>(pagedCourses, pageable, allCourses.size());
    }

    public Page<Course> findByTitleContaining(String title, Pageable pageable) {
        List<Course> filteredCourses = courses.stream()
                .filter(course -> course.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());

        filteredCourses = applySort(filteredCourses, pageable.getSort());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCourses.size());

        List<Course> pagedCourses = new ArrayList<>();
        if (start < end) {
            pagedCourses = filteredCourses.subList(start, end);
        }
        return new PageImpl<>(pagedCourses, pageable, filteredCourses.size());
    }
}