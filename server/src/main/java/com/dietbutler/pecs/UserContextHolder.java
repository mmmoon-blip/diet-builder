package com.dietbutler.pecs;

import com.dietbutler.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserContextHolder {

    public PersonContext buildFromUser(User user) {
        if (user == null) {
            return new PersonContext();
        }

        PersonContext ctx = new PersonContext();

        // 基础信息
        ctx.setOpenid(user.getOpenid());
        ctx.setNickname(user.getNickname());
        // gender 是 Integer 类型 (0=未知, 1=男, 2=女)，转换为 String
        Integer genderVal = user.getGender();
        if (genderVal != null) {
            ctx.setGender(genderVal == 1 ? "男" : (genderVal == 2 ? "女" : "未知"));
        }
        ctx.setAge(user.getAge());
        ctx.setHeight(user.getHeight());

        // 体重信息
        ctx.setInitialWeight(user.getInitialWeight());
        ctx.setTargetWeight(user.getTargetWeight());
        ctx.setBmr(user.getBasicMetabolism());

        // 用户类型
        ctx.setUserType(user.getUserType() != null ? user.getUserType() : "weight_loss");

        // 体质标签
        List<String> tags = new ArrayList<>();
        if (user.getConstitutionTags() != null && !user.getConstitutionTags().isEmpty()) {
            tags = Arrays.asList(user.getConstitutionTags().split(","));
        }
        ctx.setConstitutionTags(tags);

        // 运动习惯
        ctx.setExerciseFrequency(user.getExerciseFrequency());
        ctx.setExercisePreference(user.getExercisePreference());
        ctx.setHasKneeIssue(user.getHasKneeIssue());
        ctx.setFitnessLevel(user.getFitnessLevel());

        // 饮食习惯
        ctx.setDietPreference(user.getDietPreference());
        ctx.setBreakfastHabit(user.getBreakfastHabit());
        ctx.setMealTimes(user.getMealTimes());

        // 生活状态
        ctx.setWorkPressure(user.getWorkPressure());
        // waterIntake 是 Integer 类型
        Integer waterIntake = user.getWaterIntake();
        ctx.setWaterIntake(waterIntake != null ? waterIntake : 2000);
        ctx.setStandingHours(user.getStandingHours());

        // 经期信息 - User实体中不存在这些字段，使用默认值
        // ctx.setMenstrualCycleLength(user.getMenstrualCycleLength());
        // ctx.setLastMenstrualDate(...);

        // 作息 - sleepStart 和 sleepEnd 是 LocalTime 类型
        LocalTime sleepStartLocal = user.getSleepStart();
        if (sleepStartLocal != null) {
            ctx.setSleepStart(sleepStartLocal.toString());
        }
        LocalTime sleepEndLocal = user.getSleepEnd();
        if (sleepEndLocal != null) {
            ctx.setSleepEnd(sleepEndLocal.toString());
        }

        return ctx;
    }
}
