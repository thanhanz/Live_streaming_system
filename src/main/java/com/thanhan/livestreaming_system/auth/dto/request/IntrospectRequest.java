package com.thanhan.livestreaming_system.auth.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class IntrospectRequest {
    String token;
}
