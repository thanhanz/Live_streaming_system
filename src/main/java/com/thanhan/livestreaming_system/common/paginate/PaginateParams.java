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
    @Min(1)
    public int page;

    @NotNull
    @Min(5)
    public int limit;
    public String sortBy = "createdAt";
    public String order = "desc";
}
