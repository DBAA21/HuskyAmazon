package com.csye6220.huskyamazon.service.impl;

import com.csye6220.huskyamazon.dao.ViewHistoryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Keepstrategy：仅Keep最近3天的view history。
 * 每天定时清理一次早于3天的record。
 */
@Service
public class ViewHistoryRetentionService {

    private final ViewHistoryDAO viewHistoryDAO;

    @Autowired
    public ViewHistoryRetentionService(ViewHistoryDAO viewHistoryDAO) {
        this.viewHistoryDAO = viewHistoryDAO;
    }

    // 每天凌晨3点清理一次
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeOldHistories() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(3);
        viewHistoryDAO.deleteOlderThan(cutoff);
    }
}
