package io.github.mido.kenning.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String googleSub = oidcUser.getAttribute("sub");
        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");

        try {
            userRepository.findByGoogleSub(googleSub)
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .googleSub(googleSub)
                                    .email(email)
                                    .displayName(name)
                                    .build()
                    ));
        } catch (DataIntegrityViolationException ignored) {
            // concurrent first-login: another request already inserted this user
        }

        return oidcUser;
    }
}