package com.bezkoder.spring.data.jpa.pagingsorting.controller;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.bezkoder.spring.data.jpa.pagingsorting.model.Course;
import com.bezkoder.spring.data.jpa.pagingsorting.repository.CourseRepository;

@CrossOrigin(origins = "*") 
@RestController
@RequestMapping("/api")
public class CourseController {

  @Autowired
  CourseRepository courseRepository;

  private Sort.Direction getSortDirection(String direction) {
    if (direction.equals("asc")) {
      return Sort.Direction.ASC;
    } else if (direction.equals("desc")) {
      return Sort.Direction.DESC;
    }
    return Sort.Direction.ASC;
  }

  @GetMapping("/sortedcourses")
  public ResponseEntity<List<Course>> getAllCourses(@RequestParam(defaultValue = "id,desc") String[] sort) {

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

  
      List<Course> courses = courseRepository.findByTitleContaining("", Sort.by(orders));

      if (courses.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }

      return new ResponseEntity<>(courses, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/courses")
  public ResponseEntity<Map<String, Object>> getAllCoursesPage(
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

      Page<Course> pageCourses;
      if (title == null || title.isEmpty()) {
        pageCourses = courseRepository.findAll(pagingSort);
      } else {
        pageCourses = courseRepository.findByTitleContaining(title, pagingSort);
      }

      List<Course> courses = pageCourses.getContent();

      Map<String, Object> response = new HashMap<>();
      response.put("courses", courses);
      response.put("currentPage", pageCourses.getNumber());
      response.put("totalItems", pageCourses.getTotalElements());
      response.put("totalPages", pageCourses.getTotalPages());

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/courses/type/{type}")
  public ResponseEntity<Map<String, Object>> findByType(
      @PathVariable("type") String type,
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


      Page<Course> pageCourses = courseRepository.findByType(type, pagingSort);

      List<Course> courses = pageCourses.getContent();

      Map<String, Object> response = new HashMap<>();
      response.put("courses", courses);
      response.put("currentPage", pageCourses.getNumber());
      response.put("totalItems", pageCourses.getTotalElements());
      response.put("totalPages", pageCourses.getTotalPages());

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/courses/{id}")
  public ResponseEntity<Course> getCourseById(@PathVariable("id") int id) { 
    Optional<Course> courseData = courseRepository.findById(id);

    if (courseData.isPresent()) {
      return new ResponseEntity<>(courseData.get(), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @PostMapping("/courses")
  public ResponseEntity<Course> createCourse(@RequestBody Course course) {
    try {
      
      Course _course = courseRepository.save(course);
      return new ResponseEntity<>(_course, HttpStatus.CREATED);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/courses/{id}")
  public ResponseEntity<Course> updateCourse(@PathVariable("id") int id, @RequestBody Course course) { 
    Optional<Course> courseData = courseRepository.findById(id);

    if (courseData.isPresent()) {
      Course _course = courseData.get();
      _course.setId(id);
      _course.setTitle(course.getTitle());
      _course.setDescription(course.getDescription());
      _course.setCategory(course.getCategory());
      _course.setType(course.getType());
      _course.setGradeRange(course.getGradeRange());
      _course.setMinAge(course.getMinAge());
      _course.setMaxAge(course.getMaxAge());
      _course.setPrice(course.getPrice());
      _course.setNextSessionDate(course.getNextSessionDate());
      
      Course updatedCourse = courseRepository.save(_course);
      return new ResponseEntity<>(updatedCourse, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/courses/{id}")
  public ResponseEntity<HttpStatus> deleteCourse(@PathVariable("id") int id) {
    try {
      courseRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/courses")
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