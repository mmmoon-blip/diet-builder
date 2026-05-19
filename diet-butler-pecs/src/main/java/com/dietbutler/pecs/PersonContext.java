package com.dietbutler.pecs;

import java.util.ArrayList;
import java.util.List;

public class PersonContext {

    private String openid;
    private String nickname;
    private String gender;
    private Integer age;
    private Double height;

    private Double initialWeight;
    private Double targetWeight;
    private Double currentWeight;
    private Double bmr;
    private LocalDate targetWeightDate;

    private String userType;

    private List<String> constitutionTags = new ArrayList<>();

    private Integer exerciseFrequency;
    private String exercisePreference;
    private Boolean hasKneeIssue;
    private String fitnessLevel;

    private String dietPreference;
    private Boolean breakfastHabit;
    private String mealTimes;

    private Integer workPressure;
    private Integer waterIntake;
    private Integer standingHours;

    private Integer menstrualCycleLength;
    private LocalDate lastMenstrualDate;

    private String sleepStart;
    private String sleepEnd;

    public PersonContext() {}

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getInitialWeight() { return initialWeight; }
    public void setInitialWeight(Double initialWeight) { this.initialWeight = initialWeight; }

    public Double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(Double targetWeight) { this.targetWeight = targetWeight; }

    public Double getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(Double currentWeight) { this.currentWeight = currentWeight; }

    public Double getBmr() { return bmr; }
    public void setBmr(Double bmr) { this.bmr = bmr; }

    public LocalDate getTargetWeightDate() { return targetWeightDate; }
    public void setTargetWeightDate(LocalDate targetWeightDate) { this.targetWeightDate = targetWeightDate; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public List<String> getConstitutionTags() { return constitutionTags; }
    public void setConstitutionTags(List<String> constitutionTags) { this.constitutionTags = constitutionTags; }

    public Integer getExerciseFrequency() { return exerciseFrequency; }
    public void setExerciseFrequency(Integer exerciseFrequency) { this.exerciseFrequency = exerciseFrequency; }

    public String getExercisePreference() { return exercisePreference; }
    public void setExercisePreference(String exercisePreference) { this.exercisePreference = exercisePreference; }

    public Boolean getHasKneeIssue() { return hasKneeIssue; }
    public void setHasKneeIssue(Boolean hasKneeIssue) { this.hasKneeIssue = hasKneeIssue; }

    public String getFitnessLevel() { return fitnessLevel; }
    public void setFitnessLevel(String fitnessLevel) { this.fitnessLevel = fitnessLevel; }

    public String getDietPreference() { return dietPreference; }
    public void setDietPreference(String dietPreference) { this.dietPreference = dietPreference; }

    public Boolean getBreakfastHabit() { return breakfastHabit; }
    public void setBreakfastHabit(Boolean breakfastHabit) { this.breakfastHabit = breakfastHabit; }

    public String getMealTimes() { return mealTimes; }
    public void setMealTimes(String mealTimes) { this.mealTimes = mealTimes; }

    public Integer getWorkPressure() { return workPressure; }
    public void setWorkPressure(Integer workPressure) { this.workPressure = workPressure; }

    public Integer getWaterIntake() { return waterIntake; }
    public void setWaterIntake(Integer waterIntake) { this.waterIntake = waterIntake; }

    public Integer getStandingHours() { return standingHours; }
    public void setStandingHours(Integer standingHours) { this.standingHours = standingHours; }

    public Integer getMenstrualCycleLength() { return menstrualCycleLength; }
    public void setMenstrualCycleLength(Integer menstrualCycleLength) { this.menstrualCycleLength = menstrualCycleLength; }

    public LocalDate getLastMenstrualDate() { return lastMenstrualDate; }
    public void setLastMenstrualDate(LocalDate lastMenstrualDate) { this.lastMenstrualDate = lastMenstrualDate; }

    public String getSleepStart() { return sleepStart; }
    public void setSleepStart(String sleepStart) { this.sleepStart = sleepStart; }

    public String getSleepEnd() { return sleepEnd; }
    public void setSleepEnd(String sleepEnd) { this.sleepEnd = sleepEnd; }

    public static class LocalDate {
        private int year;
        private int month;
        private int day;

        public LocalDate() {}
        public LocalDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }
        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }
        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
    }
}
