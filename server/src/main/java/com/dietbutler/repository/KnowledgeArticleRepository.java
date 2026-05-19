package com.dietbutler.repository;

import com.dietbutler.entity.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByStatusOrderByViewCountDesc(Integer status);

    List<KnowledgeArticle> findByCategoryAndStatusOrderByViewCountDesc(String category, Integer status);

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.status = 1 AND " +
           "(a.title LIKE %:keyword% OR a.summary LIKE %:keyword% OR a.tags LIKE %:keyword%) " +
           "ORDER BY a.viewCount DESC")
    List<KnowledgeArticle> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT a FROM KnowledgeArticle a WHERE a.status = 1 AND a.category = :category AND " +
           "(a.title LIKE %:keyword% OR a.summary LIKE %:keyword% OR a.tags LIKE %:keyword%) " +
           "ORDER BY a.viewCount DESC")
    List<KnowledgeArticle> searchByKeywordAndCategory(@Param("keyword") String keyword,
                                                       @Param("category") String category);

    @Query("SELECT DISTINCT a.category FROM KnowledgeArticle a WHERE a.status = 1")
    List<String> findAllActiveCategories();
}