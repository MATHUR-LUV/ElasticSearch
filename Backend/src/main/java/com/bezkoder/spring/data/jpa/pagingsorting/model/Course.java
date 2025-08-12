package com.bezkoder.spring.data.jpa.pagingsorting.model;

import java.time.OffsetDateTime; // Use OffsetDateTime for ISO-8601 with timezone

// Remove JPA annotations as we are not using a database
// import jakarta.persistence.*;

public class Course {

  private String id; // Changed to String to match JSON IDs
  private String title;
  private String description;
  private String category;
  private String type; // ONE_TIME, COURSE, or CLUB
  private String gradeRange;
  private int minAge;
  private int maxAge;
  private double price; // Changed to double for decimal prices
  private OffsetDateTime nextSessionDate; // For ISO-8601 date-time string

  public Course() {
    // Default constructor for Jackson deserialization
  }

  public Course(String id, String title, String description, String category, String type, String gradeRange,
                int minAge, int maxAge, double price, OffsetDateTime nextSessionDate) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.category = category;
    this.type = type;
    this.gradeRange = gradeRange;
    this.minAge = minAge;
    this.maxAge = maxAge;
    this.price = price;
    this.nextSessionDate = nextSessionDate;
  }

  // Getters and Setters

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getGradeRange() {
    return gradeRange;
  }

  public void setGradeRange(String gradeRange) {
    this.gradeRange = gradeRange;
  }

  public int getMinAge() {
    return minAge;
  }

  public void setMinAge(int minAge) {
    this.minAge = minAge;
  }

  public int getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(int maxAge) {
    this.maxAge = maxAge;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public OffsetDateTime getNextSessionDate() {
    return nextSessionDate;
  }

  public void setNextSessionDate(OffsetDateTime nextSessionDate) {
    this.nextSessionDate = nextSessionDate;
  }

  @Override
  public String toString() {
    return "Course [id=" + id + ", title=" + title + ", category=" + category + ", type=" + type + ", gradeRange="
        + gradeRange + ", minAge=" + minAge + ", maxAge=" + maxAge + ", price=" + price + ", nextSessionDate="
        + nextSessionDate + "]";
  }
}