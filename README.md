# 200ok-BE
홍익대학교 졸업프로젝트 팀 '200ok'의 백엔드 레포지토리입니다.

## 카카오 로그인

실행 환경에 다음 값을 설정합니다.

- `KAKAO_REST_API_KEY`: 카카오 앱의 REST API 키
- `KAKAO_REST_API_SECRET`: 카카오 앱의 Client Secret
- `KAKAO_REDIRECT_URI`: 선택 사항. 기본값은 `{baseUrl}/login/oauth2/code/kakao`
- `JWT_SECRET_KEY`: 32바이트 이상의 JWT 서명 키

카카오 개발자 콘솔의 Redirect URI에도 실제 서버 주소의
`/login/oauth2/code/kakao`를 등록해야 합니다.

1. 브라우저에서 `GET /oauth2/authorization/kakao`로 로그인합니다.
2. 콜백이 성공하면 응답 본문으로 `accessToken`, `refreshToken`, `tokenType`이 반환됩니다.
3. 인증 API는 `Authorization: Bearer {accessToken}` 헤더로 호출합니다.

기본 로그인에서는 닉네임만 동의를 요청합니다. 프로필 사진은 카카오 앱에서
동의항목을 활성화한 뒤 `profile_image` scope를 추가했을 때만 저장됩니다.
