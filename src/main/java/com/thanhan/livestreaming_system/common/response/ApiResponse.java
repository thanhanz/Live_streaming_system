package com.thanhan.livestreaming_system.common.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class ApiResponse<T> {
    int status;
    String message;
    T data;

    public static <T> ApiResponse<T> success(int status, String message) {
        return ApiResponse.success(status, message);
    }
}
