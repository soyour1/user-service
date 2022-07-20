package com.sparkminds.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparkminds.users.model.RefreshToken;
import com.sparkminds.users.model.UserDevice;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    @Override
    Optional<UserDevice> findById(Long id);

    Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken);

    Optional<UserDevice> findByUserId(Long userId);
}
