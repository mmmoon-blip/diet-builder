package com.dietbutler.pecs.solution;

import com.dietbutler.pecs.PersonContext;
import com.dietbutler.pecs.scene.SceneEngine;
import com.dietbutler.pecs.scene.SceneResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Set;

@Component
public class PecsReasoner {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PecsReasoner.class);

    @Autowired
    private SceneEngine sceneEngine;

    @Autowired
    private SolutionBuilder solutionBuilder;

    public String reasonDiet(PersonContext person, String userMessage) {
        log.debug("PECS饮食推理开始, 用户消息: {}", userMessage);
        SceneResult scene = sceneEngine.recognize(person, LocalTime.now(), userMessage);
        log.debug("场景识别结果: {}", scene.getScenes());
        Solution solution = solutionBuilder.buildDietSolution(person, scene);
        return formatSolutionResponse(solution);
    }

    public String reasonExercise(PersonContext person, String userMessage) {
        log.debug("PECS运动推理开始, 用户消息: {}", userMessage);
        SceneResult scene = sceneEngine.recognize(person, LocalTime.now(), userMessage);
        log.debug("场景识别结果: {}", scene.getScenes());
        Solution solution = solutionBuilder.buildExerciseSolution(person, scene);
        return formatSolutionResponse(solution);
    }

    public String reasonEmotion(PersonContext person, String userMessage) {
        log.debug("PECS情绪支持推理开始, 用户消息: {}", userMessage);
        SceneResult scene = sceneEngine.recognize(person, LocalTime.now(), userMessage);
        log.debug("场景识别结果: {}", scene.getScenes());
        Solution solution = solutionBuilder.buildEmotionalSolution(person, scene);
        return formatSolutionResponse(solution);
    }

    public String reasonGeneral(PersonContext person, String userMessage) {
        log.debug("PECS通用推理开始, 用户消息: {}", userMessage);
        SceneResult scene = sceneEngine.recognize(person, LocalTime.now(), userMessage);
        Set<String> scenes = scene.getScenes();
        if (scenes.contains("MENSTRUAL")) {
            return reasonDiet(person, userMessage);
        } else if (scenes.contains("STRESS_EATING") || scenes.contains("WORK_OVERTIME")) {
            return reasonEmotion(person, userMessage);
        } else if (scenes.contains("POST_EXERCISE")) {
            return reasonExercise(person, userMessage);
        }
        StringBuilder response = new StringBuilder();
        response.append(reasonDiet(person, userMessage)).append("\n\n");
        response.append(reasonExercise(person, userMessage));
        return response.toString();
    }

    private String formatSolutionResponse(Solution solution) {
        StringBuilder sb = new StringBuilder();
        sb.append("🍽️ ").append(solution.getTitle()).append("\n\n");
        sb.append(solution.getSummary()).append("\n\n");
        var advices = solution.getAdvices();
        if (advices != null) {
            for (int i = 0; i < advices.size(); i++) {
                sb.append(i + 1).append(". ").append(advices.get(i)).append("\n");
            }
        }
        return sb.toString();
    }
}
