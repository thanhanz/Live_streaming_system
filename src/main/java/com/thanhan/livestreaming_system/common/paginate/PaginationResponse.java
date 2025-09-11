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
    public Object nextCursor;
    public boolean hasNext;
    public List<T> items;
}
