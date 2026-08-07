package com.team202ok.demo.global;

import com.team202ok.demo.domain.user.entity.User;
import com.team202ok.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User kakaoUser = super.loadUser(userRequest);
        Object kakaoIdAttribute = kakaoUser.getAttribute("id");
        if (kakaoIdAttribute == null) {
            throw new OAuth2AuthenticationException("Kakao user response does not contain an id.");
        }
        String kakaoId = kakaoIdAttribute.toString();
        Map<String, Object> account = valueAsMap(kakaoUser.getAttribute("kakao_account"));
        Map<String, Object> profile = valueAsMap(account.get("profile"));
        String nickname = valueAsString(profile.get("nickname"));
        String profileImageUrl = valueAsString(profile.get("profile_image_url"));

        User user = userRepository.findByKakaoId(kakaoId)
                .map(existing -> {
                    existing.updateProfile(nickname, profileImageUrl);
                    return existing;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(kakaoId)
                        .name(nickname)
                        .profileImageUrl(profileImageUrl)
                        .isNotificationEnabled(true)
                        .build()));

        return new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of("userId", user.getId(), "kakaoId", kakaoId),
                "userId"
        );
    }

    private Map<String, Object> valueAsMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Collections.emptyMap();
    }

    private String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
