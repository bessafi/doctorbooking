package com.codingshuttle.youtube.hospitalManagement.repository;

import com.codingshuttle.youtube.hospitalManagement.entity.User;
import com.codingshuttle.youtube.hospitalManagement.entity.type.AuthProviderType;
//import com.clinic.doctorappointment.entity.User;
//import com.clinic.doctorappointment.entity.type.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username (which is their email).
     *
     * @param username The email of the user.
     * @return An Optional containing the user if found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their OAuth2 provider ID and provider type.
     * This is used to link social logins to a user account.
     *
     * @param providerId   The unique ID from the provider (e.g., Google's 'sub' claim).
     * @param providerType The authentication provider (e.g., GOOGLE).
     * @return An Optional containing the user if found.
     */
    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType providerType);
}
