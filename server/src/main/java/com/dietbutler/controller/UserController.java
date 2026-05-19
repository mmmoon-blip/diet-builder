package com.dietbutler.controller;

import com.dietbutler.dto.ApiResponse;
import com.dietbutler.dto.UpdateUserRequest;
import com.dietbutler.entity.User;
import com.dietbutler.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/getOrCreate")
    public ApiResponse<User> getOrCreate(@RequestParam String openid) {
        User user = userService.getOrCreateUser(openid);
        return ApiResponse.success(user);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ApiResponse.error("用户不存在");
        }
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getHeight() != null) user.setHeight(request.getHeight());
        if (request.getTargetWeight() != null) user.setTargetWeight(request.getTargetWeight());
        if (request.getTargetFat() != null) user.setTargetFat(request.getTargetFat());
        if (request.getBasicMetabolism() != null) user.setBasicMetabolism(request.getBasicMetabolism());
        if (request.getDietaryTaboo() != null) user.setDietaryTaboo(request.getDietaryTaboo());
        if (request.getSleepStart() != null) user.setSleepStart(request.getSleepStart());
        if (request.getSleepEnd() != null) user.setSleepEnd(request.getSleepEnd());
        if (request.getStartWeightDate() != null) user.setStartWeightDate(request.getStartWeightDate());
        if (request.getWeightLossPeriod() != null) user.setWeightLossPeriod(request.getWeightLossPeriod());
        if (request.getReminderIntervalHours() != null) user.setReminderIntervalHours(request.getReminderIntervalHours());
        // 新增字段
        if (request.getUserType() != null) user.setUserType(request.getUserType());
        if (request.getConstitutionTags() != null) user.setConstitutionTags(request.getConstitutionTags());
        if (request.getExerciseFrequency() != null) user.setExerciseFrequency(request.getExerciseFrequency());
        if (request.getExercisePreference() != null) user.setExercisePreference(request.getExercisePreference());
        if (request.getHasKneeIssue() != null) user.setHasKneeIssue(request.getHasKneeIssue());
        if (request.getFitnessLevel() != null) user.setFitnessLevel(request.getFitnessLevel());
        if (request.getDietPreference() != null) user.setDietPreference(request.getDietPreference());
        if (request.getBreakfastHabit() != null) user.setBreakfastHabit(request.getBreakfastHabit());
        if (request.getMealTimes() != null) user.setMealTimes(request.getMealTimes());
        if (request.getWorkPressure() != null) user.setWorkPressure(request.getWorkPressure());
        if (request.getWaterIntake() != null) user.setWaterIntake(request.getWaterIntake());
        if (request.getStandingHours() != null) user.setStandingHours(request.getStandingHours());
        if (request.getTargetAreas() != null) user.setTargetAreas(request.getTargetAreas());
        user.setUpdatedAt(java.time.LocalDateTime.now());
        User saved = userService.updateUser(user);
        return ApiResponse.success("更新成功", saved);
    }
}