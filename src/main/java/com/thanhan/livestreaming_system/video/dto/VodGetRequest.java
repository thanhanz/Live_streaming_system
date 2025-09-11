package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.common.paginate.PaginateParams;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
public class VodGetRequest extends PaginateParams {
    public static VodGetRequest of(int limit, String sortBy, String order, Object nextCursor) {
        VodGetRequest req = new VodGetRequest();
        req.setLimit(limit);
        req.setSortBy(sortBy);
        req.setOrder(order);
        req.setNextCursor(nextCursor);
        return req;
    }
}
