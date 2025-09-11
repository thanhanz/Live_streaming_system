package com.thanhan.livestreaming_system.common.paginate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class PaginateParams {
    @NotNull
    public int limit;
    public String sortBy = "createdAt";
    public String order = "desc";
    public Object nextCursor;
}
