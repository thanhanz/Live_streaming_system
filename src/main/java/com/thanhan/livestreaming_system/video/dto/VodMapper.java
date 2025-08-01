package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.video.entity.Vod;

public class VodMapper {

    public static VodResponse toVodResponse(Vod vod){
        return new VodResponse(
                vod.getId(),
                vod.getTitle(),
                vod.getDescription(),
                vod.getImageUrl(),
                vod.getPublished(),
                vod.getOnlyMember(),
                vod.getChannel().getId()
        );
    }

//
//    public static Vod toVod(VodRequest request){
//        return new Vod()
//    }
}
