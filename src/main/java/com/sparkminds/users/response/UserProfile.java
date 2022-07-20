package com.sparkminds.users.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
	
    private Long id;
    
    private String email;
    
    private String name;
    
    private Boolean active;
}
