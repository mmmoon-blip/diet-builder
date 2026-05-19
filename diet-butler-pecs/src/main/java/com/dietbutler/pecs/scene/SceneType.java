package com.dietbutler.pecs.scene;

public enum SceneType {
    MORNING_FASTING("早晨空腹", "早餐前的空腹状态"),
    LATE_NIGHT("熬夜", "夜间活动或睡眠不足"),
    MENSTRUAL("经期", "女性生理期"),
    OVULATION("排卵期", "月经中期"),
    POST_EXERCISE("运动后", "刚完成运动不久"),
    WORK_OVERTIME("加班", "工作压力大或加班"),
    SEDENTARY("久坐", "长时间坐着"),
    STRESS_EATING("情绪化进食", "因压力或情绪进食"),
    FEELING_DOWN("情绪低落", "心情不好"),
    FEAST_DAY("聚餐", "有饭局或聚会"),
    TRAVEL("出差旅行", "环境变化");

    private final String label;
    private final String description;

    SceneType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getDescription() { return description; }
}
