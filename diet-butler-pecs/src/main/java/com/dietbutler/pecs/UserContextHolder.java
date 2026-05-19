package com.dietbutler.pecs;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserContextHolder {

    public PersonContext buildFromUser(Object user) {
        PersonContext ctx = new PersonContext();
        if (user == null) return ctx;

        try {
            Class<?> clazz = user.getClass();

            ctx.setOpenid(getStringField(clazz, user, "getOpenid"));
            ctx.setNickname(getStringField(clazz, user, "getNickname"));
            ctx.setGender(convertGender(getIntegerField(clazz, user, "getGender")));
            ctx.setAge(getIntegerField(clazz, user, "getAge"));
            ctx.setHeight(getDoubleField(clazz, user, "getHeight"));
            ctx.setInitialWeight(getDoubleField(clazz, user, "getInitialWeight"));
            ctx.setTargetWeight(getDoubleField(clazz, user, "getTargetWeight"));
            ctx.setBmr(getDoubleField(clazz, user, "getBasicMetabolism"));
            ctx.setUserType(getStringField(clazz, user, "getUserType"));

            String constitutionTags = getStringField(clazz, user, "getConstitutionTags");
            if (constitutionTags != null && !constitutionTags.isEmpty()) {
                ctx.setConstitutionTags(Arrays.asList(constitutionTags.split(",")));
            } else {
                ctx.setConstitutionTags(new ArrayList<>());
            }

            ctx.setExerciseFrequency(getIntegerField(clazz, user, "getExerciseFrequency"));
            ctx.setExercisePreference(getStringField(clazz, user, "getExercisePreference"));
            ctx.setHasKneeIssue(getBooleanField(clazz, user, "getHasKneeIssue"));
            ctx.setFitnessLevel(getStringField(clazz, user, "getFitnessLevel"));
            ctx.setDietPreference(getStringField(clazz, user, "getDietPreference"));
            ctx.setBreakfastHabit(getBooleanField(clazz, user, "getBreakfastHabit"));
            ctx.setMealTimes(getStringField(clazz, user, "getMealTimes"));
            ctx.setWorkPressure(getIntegerField(clazz, user, "getWorkPressure"));
            ctx.setWaterIntake(getIntegerField(clazz, user, "getWaterIntake"));
            ctx.setStandingHours(getIntegerField(clazz, user, "getStandingHours"));

            LocalTime sleepStart = getLocalTimeField(clazz, user, "getSleepStart");
            if (sleepStart != null) ctx.setSleepStart(sleepStart.toString());
            LocalTime sleepEnd = getLocalTimeField(clazz, user, "getSleepEnd");
            if (sleepEnd != null) ctx.setSleepEnd(sleepEnd.toString());

        } catch (Exception e) {
            // ignore - use defaults
        }
        return ctx;
    }

    private String getStringField(Class<?> clazz, Object obj, String method) {
        try {
            Object result = clazz.getMethod(method).invoke(obj);
            return result != null ? result.toString() : null;
        } catch (Exception e) { return null; }
    }

    private Integer getIntegerField(Class<?> clazz, Object obj, String method) {
        try {
            Object result = clazz.getMethod(method).invoke(obj);
            if (result instanceof Integer) return (Integer) result;
            if (result != null) return Integer.parseInt(result.toString());
            return null;
        } catch (Exception e) { return null; }
    }

    private Double getDoubleField(Class<?> clazz, Object obj, String method) {
        try {
            Object result = clazz.getMethod(method).invoke(obj);
            if (result instanceof Double) return (Double) result;
            if (result != null) return Double.parseDouble(result.toString());
            return null;
        } catch (Exception e) { return null; }
    }

    private Boolean getBooleanField(Class<?> clazz, Object obj, String method) {
        try {
            Object result = clazz.getMethod(method).invoke(obj);
            return result instanceof Boolean ? (Boolean) result : null;
        } catch (Exception e) { return null; }
    }

    private LocalTime getLocalTimeField(Class<?> clazz, Object obj, String method) {
        try {
            Object result = clazz.getMethod(method).invoke(obj);
            return result instanceof LocalTime ? (LocalTime) result : null;
        } catch (Exception e) { return null; }
    }

    private String convertGender(Integer genderVal) {
        if (genderVal == null) return null;
        if (genderVal == 1) return "男";
        if (genderVal == 2) return "女";
        return "未知";
    }
}
