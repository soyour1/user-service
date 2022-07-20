package com.stackabuse.springSecurity.controller;

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

import com.stackabuse.springSecurity.dto.LogOutRequest;
import com.stackabuse.springSecurity.dto.UserReponseDto;
import com.stackabuse.springSecurity.event.OnUserLogoutSuccessEvent;
import com.stackabuse.springSecurity.exception.ResourceNotFoundException;
import com.stackabuse.springSecurity.exception.UserLogoutException;
import com.stackabuse.springSecurity.model.User;
import com.stackabuse.springSecurity.model.UserDevice;
import com.stackabuse.springSecurity.repository.UserRepository;
import com.stackabuse.springSecurity.response.ApiResponse;
import com.stackabuse.springSecurity.response.UserProfile;
import com.stackabuse.springSecurity.service.CurrentUser;
import com.stackabuse.springSecurity.service.RefreshTokenService;
import com.stackabuse.springSecurity.service.UserDeviceService;
import com.stackabuse.springSecurity.service.UserPrincipal;

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
//        return null;
//        return userRepository.findById(userPrincipal.getId())
//                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
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
