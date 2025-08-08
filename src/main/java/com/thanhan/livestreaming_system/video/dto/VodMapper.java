package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.user.dto.response.ChannelCacheResponse;
import com.thanhan.livestreaming_system.video.entity.Vod;

public class VodMapper {

    public static VodResponse toVodResponse(Vod vod, ChannelCacheResponse channel) {
        return new VodResponse(
                vod.getId(),
                vod.getTitle(),
                vod.getDescription(),
                vod.getImageUrl(),
                vod.getPublished(),
                vod.getOnlyMember(),
                vod.getChannel().getId(),
                channel
        );
    }

//
//    public static Vod toVod(VodRequest request){
//        return new Vod()
//    }
}
