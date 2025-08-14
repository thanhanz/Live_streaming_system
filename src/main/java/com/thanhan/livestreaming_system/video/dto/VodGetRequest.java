package com.thanhan.livestreaming_system.video.dto;

import com.thanhan.livestreaming_system.common.paginate.PaginateParams;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
public class VodGetRequest extends PaginateParams {

    public static VodGetRequest of(int page, int limit, String sortBy, String order) {
        VodGetRequest req = new VodGetRequest();
        req.setPage(page);
        req.setLimit(limit);
        req.setSortBy(sortBy);
        req.setOrder(order);
        return req;
    }
}
