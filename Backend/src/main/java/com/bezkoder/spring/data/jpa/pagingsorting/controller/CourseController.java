package com.bezkoder.spring.data.jpa.pagingsorting.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors; // Added for stream operations

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.spring.data.jpa.pagingsorting.model.Course; // Changed from Tutorial
import com.bezkoder.spring.data.jpa.pagingsorting.repository.CourseRepository; // Changed from TutorialRepository


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class CourseController { // Renamed from TutorialController

  @Autowired
  CourseRepository courseRepository; // Changed from tutorialRepository

  private Sort.Direction getSortDirection(String direction) {
    if (direction.equals("asc")) {
      return Sort.Direction.ASC;
    } else if (direction.equals("desc")) {
      return Sort.Direction.DESC;
    }
    return Sort.Direction.ASC;
  }

  @GetMapping("/sortedcourses") // Updated endpoint name
  public ResponseEntity<List<Course>> getAllCourses(@RequestParam(defaultValue = "id,desc") String[] sort) { // Changed to Course

    try {
      List<Order> orders = new ArrayList<Order>();

      if (sort[0].contains(",")) {
        for (String sortOrder : sort) {
          String[] _sort = sortOrder.split(",");
          orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
        }
      } else {
        orders.add(new Order(getSortDirection(sort[1]), sort[0]));
      }

      // Using the findByTitleContaining with an empty title to get all, then applying sort
      List<Course> courses = courseRepository.findByTitleContaining("", Sort.by(orders));

      if (courses.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<>(courses, HttpStatus.OK);
    } catch (Exception e) {
      // Log the exception for debugging
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/courses") // Updated endpoint name
  public ResponseEntity<Map<String, Object>> getAllCoursesPage( // Changed to Course
      @RequestParam(required = false) String title,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "3") int size,
      @RequestParam(defaultValue = "id,desc") String[] sort) {

    try {
      List<Order> orders = new ArrayList<Order>();

      if (sort[0].contains(",")) {
        for (String sortOrder : sort) {
          String[] _sort = sortOrder.split(",");
          orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
        }
      } else {
        orders.add(new Order(getSortDirection(sort[1]), sort[0]));
      }

      Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

      Page<Course> pageCourses; // Changed to Course
      if (title == null || title.isEmpty())
        pageCourses = courseRepository.findAll(pagingSort);
      else
        pageCourses = courseRepository.findByTitleContaining(title, pagingSort);

      List<Course> courses = pageCourses.getContent(); // Changed to Course

      Map<String, Object> response = new HashMap<>();
      response.put("courses", courses); // Changed key to 'courses'
      response.put("currentPage", pageCourses.getNumber());
      response.put("totalItems", pageCourses.getTotalElements());
      response.put("totalPages", pageCourses.getTotalPages());

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/courses/type/{type}") // New endpoint for filtering by type (similar to published)
  public ResponseEntity<Map<String, Object>> findByType(
      @PathVariable("type") String type, // Expect type as path variable
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "3") int size,
      @RequestParam(defaultValue = "id,desc") String[] sort) {

    try {
      List<Order> orders = new ArrayList<Order>();

      if (sort[0].contains(",")) {
        for (String sortOrder : sort) {
          String[] _sort = sortOrder.split(",");
          orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
        }
      } else {
        orders.add(new Order(getSortDirection(sort[1]), sort[0]));
      }

      Pageable pagingSort = PageRequest.of(page, size, Sort.by(orders));

      // In-memory filter for specific type
      List<Course> filteredCourses = courseRepository.findAll().stream()
              .filter(course -> course.getType().equalsIgnoreCase(type))
              .collect(Collectors.toList());

      // Manually paginate and sort the filtered list
      int start = (int) pagingSort.getOffset();
      int end = Math.min((start + pagingSort.getPageSize()), filteredCourses.size());
      List<Course> pagedCoursesContent = new ArrayList<>();
      if (start < end) {
          pagedCoursesContent = filteredCourses.subList(start, end);
      }

      // Apply sorting to the paged content if needed (though already sorted in CourseRepository)
      List<Course> sortedPagedContent = courseRepository.applySort(pagedCoursesContent, pagingSort.getSort());


      Map<String, Object> response = new HashMap<>();
      response.put("courses", sortedPagedContent);
      response.put("currentPage", pagingSort.getPageNumber());
      response.put("totalItems", (long) filteredCourses.size()); // Total items before pagination
      response.put("totalPages", (int) Math.ceil((double) filteredCourses.size() / pagingSort.getPageSize()));

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


  @GetMapping("/courses/{id}") // Updated endpoint name
  public ResponseEntity<Course> getCourseById(@PathVariable("id") String id) { // Changed to Course and String id
    Optional<Course> courseData = courseRepository.findById(id);

    if (courseData.isPresent()) {
      return new ResponseEntity<>(courseData.get(), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/courses") // Updated endpoint name
  public ResponseEntity<Course> createCourse(@RequestBody Course course) { // Changed to Course
    try {
      // Simulate ID generation and saving
      Course _course = courseRepository.save(new Course(
          null, // Let repository generate ID
          course.getTitle(),
          course.getDescription(),
          course.getCategory(),
          course.getType(),
          course.getGradeRange(),
          course.getMinAge(),
          course.getMaxAge(),
          course.getPrice(),
          course.getNextSessionDate() != null ? course.getNextSessionDate() : OffsetDateTime.now() // Default date if not provided
      ));
      return new ResponseEntity<>(_course, HttpStatus.CREATED);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/courses/{id}") // Updated endpoint name
  public ResponseEntity<Course> updateCourse(@PathVariable("id") String id, @RequestBody Course course) { // Changed to Course and String id
    Optional<Course> courseData = courseRepository.findById(id);

    if (courseData.isPresent()) {
      Course _course = courseData.get();
      _course.setTitle(course.getTitle());
      _course.setDescription(course.getDescription());
      _course.setCategory(course.getCategory());
      _course.setType(course.getType());
      _course.setGradeRange(course.getGradeRange());
      _course.setMinAge(course.getMinAge());
      _course.setMaxAge(course.getMaxAge());
      _course.setPrice(course.getPrice());
      _course.setNextSessionDate(course.getNextSessionDate());
      return new ResponseEntity<>(courseRepository.save(_course), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/courses/{id}") // Updated endpoint name
  public ResponseEntity<HttpStatus> deleteCourse(@PathVariable("id") String id) { // Changed to String id
    try {
      courseRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/courses") // Updated endpoint name
  public ResponseEntity<HttpStatus> deleteAllCourses() {
    try {
      courseRepository.deleteAll();
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}