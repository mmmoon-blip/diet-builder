package com.dietbutler.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "knowledge_article")
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String category;  // 分类：营养学/运动塑形/医学代谢/心理行为

    @Column(nullable = false, length = 200)
    private String title;    // 标题

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // 正文内容

    @Column(length = 500)
    private String summary;  // 摘要

    @Column(length = 200)
    private String tags;      // 标签

    @Column(nullable = false)
    private Integer viewCount = 0;  // 查看次数

    @Column(nullable = false)
    private Integer status = 1;     // 状态：0-禁用 1-启用

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}