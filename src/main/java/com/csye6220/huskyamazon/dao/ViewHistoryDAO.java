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

    // 查找某用户的最近浏览记录 (按时间倒序)
    // 使用 Pageable 来限制返回条数 (例如只取前 6 条)
    @Query("SELECT v FROM ViewHistory v WHERE v.user = :user ORDER BY v.viewedAt DESC")
    List<ViewHistory> findByUserOrderByViewedAtDesc(User user, Pageable pageable);

    // 检查是否已经存在该记录 (为了更新时间而不是重复插入)
    ViewHistory findByUserAndProductId(User user, Long productId);

    // 按时间清理：删除早于指定时间的所有浏览记录
    @Modifying
    @Query("DELETE FROM ViewHistory v WHERE v.viewedAt < :cutoff")
    int deleteOlderThan(LocalDateTime cutoff);
}