package com.codingshuttle.youtube.hospitalManagement.security;

import com.codingshuttle.youtube.hospitalManagement.dto.LoginRequestDto;
import com.codingshuttle.youtube.hospitalManagement.dto.LoginResponseDto;
import com.codingshuttle.youtube.hospitalManagement.dto.SignUpRequestDto;
import com.codingshuttle.youtube.hospitalManagement.dto.SignupResponseDto;
import com.codingshuttle.youtube.hospitalManagement.entity.Doctor;
import com.codingshuttle.youtube.hospitalManagement.entity.Patient;
import com.codingshuttle.youtube.hospitalManagement.entity.Role;
import com.codingshuttle.youtube.hospitalManagement.entity.User;
import com.codingshuttle.youtube.hospitalManagement.entity.type.AuthProviderType;
import com.codingshuttle.youtube.hospitalManagement.repository.DoctorRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.PatientRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.RoleRepository;
import com.codingshuttle.youtube.hospitalManagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(@Lazy AuthenticationManager authenticationManager, UserRepository userRepository,
                           DoctorRepository doctorRepository, PatientRepository patientRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);
        User user = (User) authentication.getPrincipal();
        return new LoginResponseDto(jwt, user.getId());
    }

    @Transactional
    public SignupResponseDto signup(SignUpRequestDto signUpRequestDto) {
        if (userRepository.findByUsername(signUpRequestDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        User user = new User();
        user.setUsername(signUpRequestDto.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        user.setProviderType(AuthProviderType.EMAIL);

        Set<Role> roles = new HashSet<>();
        
        // THE FIX IS HERE: We now default to creating a DOCTOR for email signup.
        Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_DOCTOR")));
        roles.add(doctorRole);
        user.setRoles(roles);

        Doctor doctor = Doctor.builder()
                .user(user)
                .name(signUpRequestDto.getName())
                .email(signUpRequestDto.getUsername())
                .build();
        doctorRepository.save(doctor);
        return new SignupResponseDto(doctor.getId(), doctor.getEmail());

        /*
        // The old patient logic is commented out for future reference
        if (signUpRequestDto.getRole().equalsIgnoreCase("DOCTOR")) {
            Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_DOCTOR")));
            roles.add(doctorRole);
            user.setRoles(roles);

            Doctor doctor = Doctor.builder()
                    .user(user)
                    .name(signUpRequestDto.getName())
                    .email(signUpRequestDto.getUsername())
                    .build();
            doctorRepository.save(doctor);
            return new SignupResponseDto(doctor.getId(), doctor.getEmail());
        } else {
            Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_PATIENT")));
            roles.add(patientRole);
            user.setRoles(roles);

            Patient patient = Patient.builder()
                    .user(user)
                    .name(signUpRequestDto.getName())
                    .email(signUpRequestDto.getUsername())
                    .build();
            patientRepository.save(patient);
            return new SignupResponseDto(patient.getId(), patient.getEmail());
        }
        */
    }

    @Transactional
    public LoginResponseDto handleOAuth2Login(OAuth2User oAuth2User) {
        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByProviderIdAndProviderType(providerId, AuthProviderType.GOOGLE)
                .orElseGet(() -> {
                    if (userRepository.findByUsername(email).isPresent()) {
                        throw new IllegalArgumentException("Email is already registered with a different provider.");
                    }
                    User newUser = new User();
                    newUser.setUsername(email);
                    newUser.setProviderId(providerId);
                    newUser.setProviderType(AuthProviderType.GOOGLE);

                    Set<Role> roles = new HashSet<>();
                    
                    // Defaulting to creating a DOCTOR for Google sign-in
                    Role doctorRole = roleRepository.findByName("ROLE_DOCTOR")
                            .orElseGet(() -> roleRepository.save(new Role("ROLE_DOCTOR")));
                    roles.add(doctorRole);
                    newUser.setRoles(roles);

                    Doctor doctor = Doctor.builder()
                            .user(newUser)
                            .name(name)
                            .email(email)
                            .build();
                    doctorRepository.save(doctor);
                    
                    /* // Patient logic commented out for future use
                    Role patientRole = roleRepository.findByName("ROLE_PATIENT")
                           .orElseGet(() -> roleRepository.save(new Role("ROLE_PATIENT")));
                    roles.add(patientRole);
                    newUser.setRoles(roles);

                    Patient patient = Patient.builder()
                            .user(newUser)
                            .name(name)
                            .email(email)
                            .build();
                    patientRepository.save(patient);
                    */

                    return newUser;
                });

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return new LoginResponseDto(jwt, user.getId());
    }
}

