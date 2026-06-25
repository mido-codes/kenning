package io.github.mido.kenning.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OidcUser oidcUser)) {
            throw new IllegalStateException("No authenticated OIDC user found in security context");
        }

        String googleSub = oidcUser.getAttribute("sub");

        return userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new UserNotFoundException(   "Authenticated user not found in database"));
    }
}
