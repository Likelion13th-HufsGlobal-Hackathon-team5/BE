엔티티명 + 행위(CRUD) + 형태(Req/Res) + Dto

단, Read는 Get으로 대체하며 CRUD로 표현할 수 없는 경우는 행위만 표현!

ex) 회원가입

- UserCreateReqDto (프론트 -> 백엔드 요청)
- UserCreateResDto (백엔드 -> 프론트 응답)

ex) 달력 (Get 요청)

- CalendarGetReqDto (프론트 -> 백엔드 요청)
- CalendarGetResDto (백엔드 -> 프론트 응답)

ex) 로그인 (CRUD 표현 불가)

- AuthLoginReqDto (프론트 -> 백엔드 요청)
- AuthLoginResDto (백엔드 -> 프론트 응답)
