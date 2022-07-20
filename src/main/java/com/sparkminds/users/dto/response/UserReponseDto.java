package com.sparkminds.users.dto.response;

import java.util.List;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserReponseDto {
    private Long id;

    private String email;
    
    private String name;

    private Boolean active;

    private String picture;
}
