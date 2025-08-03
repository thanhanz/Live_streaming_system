package com.thanhan.livestreaming_system.common.paginate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class PaginationResponse<T> {
    public int page;
    public int limit;
    public int totalItems;
    public long totalPage;
    public List<T> items;
}
