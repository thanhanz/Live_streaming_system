package com.thanhan.livestreaming_system.video.scheduler;


import com.thanhan.livestreaming_system.video.service.VodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UpdateTotalViewVods {

    RedisTemplate<String, Long> redisTemplate;
    VodService vodService;

    @Scheduled(fixedRate = 5 * 60 * 1000) //Cứ 30' sẽ cập nhập lại views trong db
    public void updateTotalViewVods() {
        Set<String> pendingViews = redisTemplate.keys("view:*:views_pending");
        //Lấy tất cả các cache (pendingViews)
        if (pendingViews != null && pendingViews.size() > 0) {
            for (String key : pendingViews) {
                /**
                 * Warn: When you call APIs to get total view
                 * -> need to check cache pendingViews (if exist) and add to data total_views in DB
                 */
                Long addedView = redisTemplate.opsForValue().get(key);
                if (addedView != null) {
                    String vodId = key.split(":")[1];
                    vodService.updateViews(Long.valueOf(vodId), addedView);
                    redisTemplate.delete(key);
                }
            }
        }
    }

}
