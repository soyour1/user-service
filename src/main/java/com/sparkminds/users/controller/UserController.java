package com.sparkminds.users.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparkminds.users.dto.request.LogOutRequest;
import com.sparkminds.users.dto.response.UserReponseDto;
import com.sparkminds.users.event.OnUserLogoutSuccessEvent;
import com.sparkminds.users.exception.ResourceNotFoundException;
import com.sparkminds.users.exception.UserLogoutException;
import com.sparkminds.users.model.User;
import com.sparkminds.users.model.UserDevice;
import com.sparkminds.users.repository.UserRepository;
import com.sparkminds.users.response.ApiResponse;
import com.sparkminds.users.response.UserProfile;
import com.sparkminds.users.service.CurrentUser;
import com.sparkminds.users.service.RefreshTokenService;
import com.sparkminds.users.service.UserDeviceService;
import com.sparkminds.users.service.UserPrincipal;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDeviceService userDeviceService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @GetMapping
    public UserReponseDto getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
       return UserReponseDto.builder()
        .id(userPrincipal.getId())
        .email(userPrincipal.getEmail())
        .name(userPrincipal.getName())
        .active(userPrincipal.getActive())
        .picture(userPrincipal.getPicture()).build();
    }

    @PutMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(@CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody LogOutRequest logOutRequest) {
        String deviceId = logOutRequest.getDeviceInfo().getDeviceId();
        UserDevice userDevice = userDeviceService.findByUserId(currentUser.getId())
                .filter(device -> device.getDeviceId().equals(deviceId))
                .orElseThrow(() -> new UserLogoutException(logOutRequest.getDeviceInfo().getDeviceId(),
                        "Invalid device Id supplied. No matching device found for the given user "));
        refreshTokenService.deleteById(userDevice.getRefreshToken().getId());

        OnUserLogoutSuccessEvent logoutSuccessEvent = new OnUserLogoutSuccessEvent(currentUser.getEmail(),
                logOutRequest.getToken(), logOutRequest);
        applicationEventPublisher.publishEvent(logoutSuccessEvent);
        return ResponseEntity.ok(new ApiResponse(true, "User has successfully logged out from the system!"));
    }

}
