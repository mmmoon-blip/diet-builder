package com.dietbutler.service;

import com.dietbutler.entity.User;
import com.dietbutler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreateUser(String openid) {
        Optional<User> existing = userRepository.findByOpenid(openid);
        if (existing.isPresent()) {
            return existing.get();
        }

        User newUser = new User();
        newUser.setOpenid(openid);
        return userRepository.save(newUser);
    }

    public User updateUser(User user) {
        fillBasalMetabolism(user);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean isProfileComplete(User user) {
        // 判断用户是否已完成建档：需要身高和目标体重
        return user.getHeight() != null && user.getTargetWeight() != null;
    }

    public String getProfileIncompleteReason(User user) {
        if (user.getHeight() == null && user.getTargetWeight() == null) {
            return "身高和目标体重";
        } else if (user.getHeight() == null) {
            return "身高";
        } else {
            return "目标体重";
        }
    }

    /**
     * 根据身高、体重、年龄、性别计算基础代谢率（BMR）
     * 使用 Mifflin-St Jeor 公式:
     *   男性: BMR = 10*体重(kg) + 6.25*身高(cm) - 5*年龄 + 5
     *   女性: BMR = 10*体重(kg) + 6.25*身高(cm) - 5*年龄 - 161
     * 优先使用 initialWeight（初始体重），无则用 targetWeight
     */
    public double calculateBasalMetabolism(User user) {
        Double weight = user.getInitialWeight() != null ? user.getInitialWeight()
                     : user.getTargetWeight();
        if (weight == null || user.getHeight() == null || user.getAge() == null) {
            return 0;
        }

        double bmr = 10 * weight + 6.25 * user.getHeight() - 5 * user.getAge();
        if (user.getGender() != null && user.getGender() == 1) {
            bmr += 5;   // 男性
        } else {
            bmr -= 161; // 女性
        }
        return Math.round(bmr);
    }

    /**
     * 当档案更新时，如果具备计算BMR的条件（身高+体重+年龄+性别），自动填充基础代谢率
     */
    public void fillBasalMetabolism(User user) {
        Double weight = user.getInitialWeight() != null ? user.getInitialWeight()
                     : user.getTargetWeight();
        if (user.getHeight() != null && weight != null && user.getAge() != null && user.getGender() != null) {
            user.setBasicMetabolism(calculateBasalMetabolism(user));
        }
    }

    /**
     * 计算每日总消耗（TDEE）
     * TDEE = BMR × 活动系数
     * 活动系数：
     * 1.2 = 久坐（无运动）
     * 1.375 = 轻度活跃（每周1-3次运动）
     * 1.55 = 中度活跃（每周3-5次运动）
     * 1.725 = 高度活跃（每周6-7次运动）
     * 1.9 = 极度活跃（运动员/体力工作者）
     */
    public double calculateTDEE(User user) {
        double bmr = calculateBasalMetabolism(user);
        if (bmr == 0) {
            // 如果无法计算BMR，返回默认值
            return 1800; // 默认日消耗
        }

        double activityFactor = getActivityFactor(user);
        return Math.round(bmr * activityFactor);
    }

    /**
     * 获取活动系数
     */
    private double getActivityFactor(User user) {
        Integer freq = user.getExerciseFrequency();
        Integer standingHours = user.getStandingHours();

        // 基础系数
        double factor = 1.2; // 久坐

        if (freq != null) {
            if (freq >= 6) {
                factor = 1.725; // 高度活跃
            } else if (freq >= 4) {
                factor = 1.55; // 中度活跃
            } else if (freq >= 2) {
                factor = 1.375; // 轻度活跃
            } else if (freq >= 1) {
                factor = 1.3;
            }
        }

        // 根据久坐时间微调
        if (standingHours != null) {
            if (standingHours < 4) {
                factor += 0.05; // 久坐少，活动多
            } else if (standingHours > 8) {
                factor -= 0.05; // 久坐多
            }
        }

        return factor;
    }

    /**
     * 根据用户类型和目标，计算每日建议摄入热量
     */
    public int calculateRecommendedCalorie(User user) {
        double tdee = calculateTDEE(user);
        String userType = user.getUserType() != null ? user.getUserType() : "weight_loss";

        return switch (userType) {
            case "weight_loss" -> (int) (tdee * 0.8); // 减脂：80% TDEE
            case "shaping" -> (int) (tdee * 0.95); // 塑形：95% TDEE
            case "maintenance" -> (int) tdee; // 维持：100% TDEE
            case "muscle_gain" -> (int) (tdee * 1.15); // 增肌：115% TDEE
            default -> (int) (tdee * 0.8);
        };
    }

    /**
     * 自动判断用户类型
     * 根据体重数据和用户特征自动推荐类型
     */
    public String autoDetectUserType(User user) {
        Double initialWeight = user.getInitialWeight();
        Double targetWeight = user.getTargetWeight();
        Double height = user.getHeight();

        if (initialWeight == null || targetWeight == null || height == null) {
            return "weight_loss"; // 默认
        }

        // 计算BMI
        double heightM = height / 100;
        double bmi = initialWeight / (heightM * heightM);

        // 计算需要减的体重
        double weightToLose = initialWeight - targetWeight;
        double weightToLosePercent = weightToLose / initialWeight * 100;

        // 根据BMI判断
        if (bmi < 18.5) {
            // BMI偏低，可能是塑形或增肌
            return "shaping";
        } else if (bmi >= 18.5 && bmi < 24) {
            // BMI正常
            if (weightToLosePercent < 3) {
                // 减重幅度小于3%，可能是塑形
                return "shaping";
            } else if (weightToLosePercent < 8) {
                // 减重3-8%，轻度减重
                return "weight_loss";
            } else {
                // 减重超过8%，中度减重
                return "weight_loss";
            }
        } else if (bmi >= 24 && bmi < 28) {
            // BMI偏重
            if (weightToLosePercent < 5) {
                return "shaping"; // 轻度塑形
            }
            return "weight_loss";
        } else {
            // BMI肥胖
            if (weightToLosePercent >= 10) {
                return "weight_loss";
            } else {
                return "shaping"; // 大基数先塑形再减重
            }
        }
    }
}
