package com.csye6220.huskyamazon.dao;

import com.csye6220.huskyamazon.entity.User;
import com.csye6220.huskyamazon.entity.ViewHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface ViewHistoryDAO extends JpaRepository<ViewHistory, Long> {

    // Find某user的最近浏览record (按time倒序)
    // 使用 Pageable 来限制returnitem数 (例如只取前 6 item)
    @Query("SELECT v FROM ViewHistory v WHERE v.user = :user ORDER BY v.viewedAt DESC")
    List<ViewHistory> findByUserOrderByViewedAtDesc(User user, Pageable pageable);

    // Check是否已经存在该record (为了Updatetime而不是重复insert)
    ViewHistory findByUserAndProductId(User user, Long productId);

    // 按time清理：Delete早于指定time的all浏览record
    @Modifying
    @Query("DELETE FROM ViewHistory v WHERE v.viewedAt < :cutoff")
    int deleteOlderThan(LocalDateTime cutoff);
}