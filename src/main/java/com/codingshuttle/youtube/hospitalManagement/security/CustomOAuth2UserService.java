package com.codingshuttle.youtube.hospitalManagement.security;
// package com.clinic.doctorappointment.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // The DefaultOAuth2UserService does the heavy lifting of fetching user info from the provider.
        // We can add custom logic here later if needed, e.g., to immediately persist or update user info.
        return super.loadUser(userRequest);
    }
}
