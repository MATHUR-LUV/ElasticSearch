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
import java.util.stream.Collectors;

// ✨ THESE ARE THE CRUCIAL IMPORTS FOR SPRING DATA INTERFACES ✨
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


@Repository
public class CourseRepository {

    private List<Course> courses;
    private final ObjectMapper objectMapper; // To read JSON

    public CourseRepository() {
        this.objectMapper = new ObjectMapper();
        // Register JavaTimeModule to handle Java 8 Date/Time types (like OffsetDateTime)
        // when deserializing JSON.
        objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("sample-courses.json");
            try (InputStream inputStream = resource.getInputStream()) {
                this.courses = objectMapper.readValue(inputStream, new TypeReference<List<Course>>() {});
                System.out.println("Loaded " + courses.size() + " courses from sample-courses.json");
            }
        } catch (IOException e) {
            System.err.println("Failed to load courses from sample-courses.json: " + e.getMessage());
            this.courses = new ArrayList<>();
        }
    }

    public List<Course> findAll() {
        return new ArrayList<>(courses);
    }

    public Optional<Course> findById(String id) {
        return courses.stream()
                      .filter(course -> course.getId().equals(id))
                      .findFirst();
    }

    public List<Course> findByTitleContaining(String title, Sort sort) {
        List<Course> filteredCourses = courses.stream()
                .filter(course -> course.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
        return applySort(filteredCourses, sort);
    }

    public List<Course> findByPublished(boolean published) {
        return courses.stream()
                .filter(course -> published ? "COURSE".equals(course.getType()) : !"COURSE".equals(course.getType()))
                .collect(Collectors.toList());
    }

    public Course save(Course course) {
        if (course.getId() == null || course.getId().isEmpty()) {
            course.setId(String.valueOf(System.currentTimeMillis()));
            this.courses.add(course);
        } else {
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
                this.courses.add(course);
            }
        }
        return course;
    }

    public void deleteById(String id) {
        courses.removeIf(course -> course.getId().equals(id));
    }

    public void deleteAll() {
        this.courses.clear();
    }

    public List<Course> applySort(List<Course> list, Sort sort) {
        if (sort == null || !sort.iterator().hasNext()) {
            return list;
        }

        Comparator<Course> comparator = null;
        for (Sort.Order order : sort) {
            Comparator<Course> currentComparator = null;
            switch (order.getProperty()) {
                case "id":
                    currentComparator = Comparator.comparing(Course::getId);
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
                    currentComparator = Comparator.comparing(Course::getMinAge);
                    break;
                case "maxAge":
                    currentComparator = Comparator.comparing(Course::getMaxAge);
                    break;
                case "price":
                    currentComparator = Comparator.comparing(Course::getPrice);
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

    public Page<Course> findByPublished(boolean published, Pageable pageable) {
        List<Course> filteredCourses = courses.stream()
                .filter(course -> published ? "COURSE".equals(course.getType()) : !"COURSE".equals(course.getType()))
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