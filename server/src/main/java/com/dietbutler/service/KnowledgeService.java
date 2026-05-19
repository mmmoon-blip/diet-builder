package com.dietbutler.service;

import com.dietbutler.entity.KnowledgeArticle;
import com.dietbutler.repository.KnowledgeArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeArticleRepository articleRepository;

    @Value("${knowledge.enabled:true}")
    private boolean knowledgeEnabled;

    @Value("${knowledge.max-results:3}")
    private int maxResults;

    /**
     * 搜索知识库文章
     */
    public List<KnowledgeArticle> search(String query, String category) {
        if (!knowledgeEnabled) {
            return List.of();
        }

        List<KnowledgeArticle> results;
        if (category != null && !category.isEmpty()) {
            results = articleRepository.searchByKeywordAndCategory(query, category);
        } else {
            results = articleRepository.searchByKeyword(query);
        }

        // 限制返回数量
        return results.stream().limit(maxResults).collect(Collectors.toList());
    }

    /**
     * 获取分类下的热门文章
     */
    public List<KnowledgeArticle> getPopularByCategory(String category, int limit) {
        if (!knowledgeEnabled) {
            return List.of();
        }

        List<KnowledgeArticle> articles = articleRepository.findByCategoryAndStatusOrderByViewCountDesc(category, 1);
        return articles.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * 获取所有启用的分类
     */
    public List<String> getAllCategories() {
        if (!knowledgeEnabled) {
            return List.of();
        }
        return articleRepository.findAllActiveCategories();
    }

    /**
     * 增加浏览次数
     */
    @Transactional
    public void incrementViewCount(Long articleId) {
        articleRepository.findById(articleId).ifPresent(article -> {
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);
        });
    }

    /**
     * 构建知识上下文字符串（用于 Prompt 增强）
     */
    public String buildKnowledgeContext(String query, String category) {
        List<KnowledgeArticle> articles = search(query, category);
        if (articles.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n【相关健康知识】\n");
        for (int i = 0; i < articles.size(); i++) {
            KnowledgeArticle article = articles.get(i);
            sb.append(String.format("%d. 【%s】%s\n%s\n",
                i + 1,
                article.getTitle(),
                article.getTags() != null ? "(" + article.getTags() + ")" : "",
                article.getSummary() != null ? article.getSummary() : article.getContent().substring(0, Math.min(200, article.getContent().length())) + "..."
            ));
        }
        return sb.toString();
    }

    /**
     * 解析用户消息，提取可能涉及的知识分类
     */
    public String detectKnowledgeCategory(String message) {
        String lowerMsg = message.toLowerCase();

        // 营养学关键词
        if (containsAny(lowerMsg, "热量", "蛋白质", "碳水", "脂肪", "代谢", "bmr", "基础代谢",
                         "gi", "升糖", "食物", "饮食", "宏量", "节食", "缺口", "营养")) {
            return "营养学";
        }

        // 运动塑形关键词
        if (containsAny(lowerMsg, "运动", "跑步", "训练", "力量", "有氧", "肌肉", "减脂",
                         "塑形", "线条", "体态", "深蹲", "拉伸", "热身", "健身房")) {
            return "运动塑形";
        }

        // 医学代谢关键词
        if (containsAny(lowerMsg, "水肿", "平台期", "激素", "皮质醇", "经期", "姨妈", "月经",
                         "排卵", "假性肥胖", "便秘", "代谢", "甲状腺")) {
            return "医学代谢";
        }

        // 心理行为关键词
        if (containsAny(lowerMsg, "暴食", "焦虑", "压力", "情绪", "沮丧", "动力", "习惯",
                         "打卡", "坚持", "放弃", "自责", "安慰")) {
            return "心理行为";
        }

        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}