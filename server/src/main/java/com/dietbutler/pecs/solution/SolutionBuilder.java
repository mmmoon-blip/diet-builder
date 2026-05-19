package com.dietbutler.pecs.solution;

import com.dietbutler.pecs.PersonContext;
import com.dietbutler.pecs.scene.SceneResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SolutionBuilder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SolutionBuilder.class);

    public Solution buildDietSolution(PersonContext person, SceneResult scene) {
        Solution solution = new Solution();
        List<String> advices = new ArrayList<>();

        StringBuilder reasoning = new StringBuilder();
        reasoning.append("【PECS饮食方案推理】\n");
        reasoning.append("用户类型: ").append(person.getUserType()).append("\n");
        reasoning.append("体质标签: ").append(String.join(",", person.getConstitutionTags())).append("\n");
        reasoning.append("当前场景: ").append(scene.getScenes()).append("\n");

        // 根据用户类型设置基础热量目标
        double tdee = calculateTDEE(person);
        double targetCalories = applyUserTypeModifier(tdee, person.getUserType());
        advices.add("每日热量目标: " + (int) targetCalories + " kcal");

        // 根据场景调整
        if (scene.hasScene("MENSTRUAL")) {
            advices.add("经期建议: 增加温热食物摄入，如红糖姜茶、红枣");
            advices.add("建议补充铁元素: 瘦肉、动物肝脏、菠菜");
            reasoning.append("场景调整(经期): 增加温补食物\n");
        }

        if (scene.hasScene("LATE_NIGHT")) {
            advices.add("熬夜场景: 多摄入富含维生素B的食物，如全谷物、牛奶");
            advices.add("护肝建议: 枸杞菊花茶，避免油腻食物");
            reasoning.append("场景调整(熬夜): 护肝优先\n");
        }

        if (scene.hasScene("WORK_OVERTIME")) {
            advices.add("加班建议: 准备坚果、全麦饼干作为健康零食");
            advices.add("避免: 咖啡、浓茶、方便面");
            reasoning.append("场景调整(加班): 健康零食替代\n");
        }

        if (scene.hasScene("SEDENTARY")) {
            advices.add("久坐建议: 餐后站立15-20分钟促进消化");
            Integer waterIntake = person.getWaterIntake();
            advices.add("饮水量提升至: " + (waterIntake != null ? waterIntake : 2000) + "ml");
            reasoning.append("场景调整(久坐): 促进代谢\n");
        }

        // 根据体质标签调整
        if (person.getConstitutionTags().contains("易水肿")) {
            advices.add("体质调整(易水肿): 减少盐分摄入，晚餐清淡");
        }
        if (person.getConstitutionTags().contains("碳水敏感")) {
            advices.add("体质调整(碳水敏感): 碳水放在运动前后摄入");
        }
        if (person.getConstitutionTags().contains("代谢低")) {
            advices.add("体质调整(代谢低): 少食多餐，增加蛋白质摄入");
        }

        solution.setTitle("今日饮食方案");
        solution.setSummary("基于您的目标、体质和当前场景的综合饮食建议");
        solution.setAdvices(advices);
        solution.setReasoning(reasoning.toString());

        return solution;
    }

    public Solution buildExerciseSolution(PersonContext person, SceneResult scene) {
        Solution solution = new Solution();
        List<String> advices = new ArrayList<>();

        StringBuilder reasoning = new StringBuilder();
        reasoning.append("【PECS运动方案推理】\n");
        reasoning.append("用户类型: ").append(person.getUserType()).append("\n");
        reasoning.append("运动频率: 每周").append(person.getExerciseFrequency()).append("次\n");
        reasoning.append("当前场景: ").append(scene.getScenes()).append("\n");

        // 根据用户类型确定运动目标
        if ("weight_loss".equals(person.getUserType())) {
            advices.add("减脂优先: 有氧+力量结合");
            advices.add("推荐运动: 快走、慢跑、跳绳、游泳");
        } else if ("shaping".equals(person.getUserType())) {
            advices.add("塑形优先: 力量训练为主");
            advices.add("推荐运动: 哑铃、阻力带、自重训练");
        } else if ("muscle_gain".equals(person.getUserType())) {
            advices.add("增肌优先: 大重量少次数");
            advices.add("推荐运动: 深蹲、硬拉、卧推");
        }

        // 根据场景调整
        if (scene.hasScene("MENSTRUAL")) {
            advices.add("经期建议: 轻柔运动如瑜伽、散步");
            advices.add("避免: 剧烈运动、核心训练");
            reasoning.append("场景调整(经期): 轻柔为主\n");
        }

        if (scene.hasScene("LATE_NIGHT")) {
            advices.add("熬夜后建议: 避免高强度训练");
            advices.add("可选: 晨起轻度拉伸");
            reasoning.append("场景调整(熬夜): 低强度\n");
        }

        if (scene.hasScene("SEDENTARY")) {
            advices.add("久坐提醒: 每小时站起来活动5分钟");
            advices.add("建议: 站立办公、拉伸放松");
            reasoning.append("场景调整(久坐): 打破久坐\n");
        }

        if (scene.hasScene("POST_EXERCISE")) {
            advices.add("运动后: 补充优质蛋白质+碳水");
            advices.add("推荐: 牛奶+香蕉、鸡胸肉+米饭");
        }

        // 根据体质调整
        if (Boolean.TRUE.equals(person.getHasKneeIssue())) {
            advices.add("膝盖保护: 避免跳跃、深蹲，改用游泳、骑车");
        }
        if (person.getFitnessLevel() != null) {
            if ("新手".equals(person.getFitnessLevel())) {
                advices.add("新手建议: 从每周2-3次开始，循序渐进");
            }
        }

        solution.setTitle("今日运动方案");
        solution.setSummary("基于您的目标和当前身体状态推荐的运动计划");
        solution.setAdvices(advices);
        solution.setReasoning(reasoning.toString());

        return solution;
    }

    public Solution buildEmotionalSolution(PersonContext person, SceneResult scene) {
        Solution solution = new Solution();
        List<String> advices = new ArrayList<>();

        StringBuilder reasoning = new StringBuilder();
        reasoning.append("【PECS情绪支持推理】\n");
        reasoning.append("用户类型: ").append(person.getUserType()).append("\n");
        reasoning.append("当前场景: ").append(scene.getScenes()).append("\n");

        if (scene.hasScene("STRESS_EATING")) {
            advices.add("情绪化进食应对: 先喝一杯温水，等待10分钟");
            advices.add("分散注意力: 散步、听音乐、与朋友聊天");
            advices.add("自我对话: \"我现在不需要食物，我需要的是休息\"");
            reasoning.append("场景(情绪化进食): 正念干预\n");
        }

        if (scene.hasScene("WORK_OVERTIME")) {
            advices.add("工作压力调节: 4-7-8呼吸法（吸气4秒-屏息7秒-呼气8秒）");
            advices.add("起身活动: 离开工位，做颈部肩部拉伸");
            reasoning.append("场景(加班压力): 压力释放\n");
        }

        if (scene.hasScene("LATE_NIGHT")) {
            advices.add("熬夜情绪安抚: 告诉自己\"今晚休息是对明天的最好准备\"");
            advices.add("避免: 用食物填补熬夜的愧疚感");
            reasoning.append("场景(熬夜): 接纳情绪\n");
        }

        // 根据体质标签
        if (person.getConstitutionTags().contains("压力胖")) {
            advices.add("压力胖体质: 建议每日进行5-10分钟冥想");
        }

        advices.add("陪伴支持: 我在这里陪你，一起慢慢来");

        solution.setTitle("情绪支持方案");
        solution.setSummary("根据您当前的状态给予温暖的支持和实用建议");
        solution.setAdvices(advices);
        solution.setReasoning(reasoning.toString());

        return solution;
    }

    private double calculateTDEE(PersonContext person) {
        Double bmrValue = person.getBmr();
        double bmr = bmrValue != null ? bmrValue : 0;
        if (bmr <= 0) {
            double weight = person.getInitialWeight() != null ? person.getInitialWeight() : 60;
            double height = person.getHeight() != null ? person.getHeight() : 165;
            Integer age = person.getAge();
            int ageVal = age != null ? age : 25;
            String gender = person.getGender();

            if ("女性".equals(gender) || "女".equals(gender)) {
                bmr = 10 * weight + 6.25 * height - 5 * ageVal - 161;
            } else {
                bmr = 10 * weight + 6.25 * height - 5 * ageVal + 5;
            }
        }

        // 活动系数
        double activityFactor = 1.2;
        Integer exerciseFreq = person.getExerciseFrequency();
        if (exerciseFreq != null) {
            if (exerciseFreq >= 5) {
                activityFactor = 1.6;
            } else if (exerciseFreq >= 3) {
                activityFactor = 1.4;
            } else if (exerciseFreq >= 1) {
                activityFactor = 1.3;
            }
        }

        return bmr * activityFactor;
    }

    private double applyUserTypeModifier(double tdee, String userType) {
        if (userType == null) return tdee * 0.8;
        return switch (userType) {
            case "weight_loss" -> tdee * 0.8;
            case "shaping" -> tdee * 1.0;
            case "maintenance" -> tdee * 0.95;
            case "muscle_gain" -> tdee * 1.15;
            default -> tdee * 0.8;
        };
    }
}
