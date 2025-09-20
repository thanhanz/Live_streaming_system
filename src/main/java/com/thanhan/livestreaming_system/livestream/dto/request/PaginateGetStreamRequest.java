package com.thanhan.livestreaming_system.livestream.dto.request;

import com.thanhan.livestreaming_system.common.paginate.PaginateParams;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginateGetStreamRequest extends PaginateParams {
    public static PaginateGetStreamRequest of(int limit, String sortBy, String order, Object nextCursor) {
        PaginateGetStreamRequest req = new PaginateGetStreamRequest();
        req.setLimit(limit);
        req.setSortBy(sortBy);
        req.setOrder(order);
        req.setNextCursor(nextCursor);
        return req;
    }
}
