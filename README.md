# Delivery (order)

고객이 사장님의 메뉴를 주문하고 결제하는 배달 주문 서비스 백엔드입니다.
회원가입/로그인(JWT), 메뉴 관리, 주문, 결제를 제공합니다.

## 기술 스택

- Java 17, Spring Boot 4.1.1, Gradle
- Spring Web, Spring Security(JWT, 무상태), Bean Validation
- Spring Data JPA(Hibernate), QueryDSL 5.1.0
- PostgreSQL (운영/개발), H2 (테스트)
- JJWT 0.12.6

## 실행 방법

1. 환경변수 파일을 만듭니다. DB 접속 정보와 JWT 서명 키가 들어 있고 git 에는 올라가지 않습니다.

   ```bash
   cp .env.example .env     # 값을 채웁니다 (JWT_SECRET 은 32바이트 이상, 예: openssl rand -hex 32)
   ```

   Spring Boot 는 프로젝트 루트의 `.env` 를 읽고(`spring.config.import`), docker compose 도 같은 파일을 사용합니다.
   같은 이름의 OS 환경변수가 있으면 그 값이 우선합니다.

2. PostgreSQL 을 Docker 로 띄웁니다.

   ```bash
   docker compose up -d      # 종료: docker compose down (데이터 유지), docker compose down -v (데이터 삭제)
   ```

3. 실행합니다. `ddl-auto: update`라서 처음 실행하면 테이블이 만들어집니다.

   ```bash
   ./gradlew bootRun
   ```

4. (선택) 더미 데이터를 넣습니다. 테이블이 만들어진 뒤 `sql/dummy_data.sql`을 실행합니다.
   고객 `customer1~3`, 사장님 `owner1~3`(사장님당 메뉴 4개)가 들어가며 비밀번호는 모두 `password1234`입니다.
   같은 파일을 두 번 실행하면 회원이 중복으로 들어갑니다.

## 빌드와 테스트

```bash
./gradlew build                                   # 컴파일 + 테스트
./gradlew test                                    # 테스트만
./gradlew test --tests "com.example.delivery.order.entity.OrderTest"   # 단일 테스트
```

## 인증

로그인으로 받은 토큰을 `Authorization: Bearer {토큰}` 헤더에 담아 보냅니다.
토큰에는 로그인 아이디와 역할(`CUSTOMER` / `OWNER`), 만료 시간이 들어 있고 기본 유효시간은 1시간입니다.

## API

공통 오류 응답은 `{"message": "..."}` 형태입니다.

### 회원

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| POST | `/user/signup` | 누구나 | 회원가입. 아이디 4~20자, 비밀번호 8자 이상, 역할 `CUSTOMER` / `OWNER`. 중복 아이디 409 |
| POST | `/user/login` | 누구나 | 로그인. `{"accessToken": "..."}` 반환. 실패 401 |

### 메뉴

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| POST | `/menu` | OWNER | 메뉴 등록(201). 이름 필수, 가격 1원 이상 |
| PUT | `/menu/{id}` | OWNER | 메뉴 수정. 보낸 값만 바뀜. 본인 메뉴만(403), 없거나 삭제된 메뉴 404 |
| DELETE | `/menu/{id}` | OWNER | 메뉴 삭제(소프트 삭제). 본인 메뉴만 |
| GET | `/menu` | 누구나 | 목록 조회. `keyword`, `minPrice`, `maxPrice`, `page`, `size`. 삭제된 메뉴 제외 |
| GET | `/menu/{id}` | 누구나 | 단건 조회. 없거나 삭제된 메뉴 404 |

### 주문

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| POST | `/order` | CUSTOMER | 주문 생성 |
| GET | `/order` | 로그인 | 주문 목록. 고객은 본인 주문, 사장님은 받은 주문 |
| PATCH | `/order/{id}/cancel` | CUSTOMER, OWNER | 주문 취소. 고객은 결제 전·결제 완료, 사장님은 결제 완료·주문 수락 상태만. 결제된 주문은 결제도 취소 |
| POST | `/order/{id}/reorder` | CUSTOMER | 재주문(201). 본인의 취소된 주문을 같은 메뉴·수량·배송지로 새로 주문. 주문 1건당 한 번 |
| PATCH | `/order/{id}/state` | OWNER | 주문 상태 변경. `PAID → ACCEPTED`, `ACCEPTED → DELIVERED`만 허용 |

**주문 생성** 요청과 응답:

```json
// 요청
{
  "items": [
    { "menuId": 1, "quantity": 2 },
    { "menuId": 5, "quantity": 1 }
  ],
  "shippingAddress": "서울시 ..."
}

// 응답 (항상 201, 성공/실패는 본문으로 구분)
{
  "successes": [ { "orderId": 10, "ownerId": 4, "totalPrice": 22000 } ],
  "failures":  [ { "ownerId": 5, "reason": "주문 처리 중 오류가 발생했습니다." } ],
  "totalOrderPrice": 22000
}
```

- 서로 다른 사장님의 메뉴는 사장님별로 **독립된 주문**으로 저장됩니다. 한 사장님 주문이 실패해도 다른 사장님 주문은 유지됩니다.
- 총액은 서버가 메뉴 가격 × 수량으로 계산합니다. 요청으로 금액을 받지 않습니다.
- 없거나 삭제된 메뉴가 하나라도 있으면 전체 404입니다.

**주문 목록**은 상태 조건과 페이징을 쿼리 파라미터로 받습니다.

```
GET /order?states=PAID&states=ACCEPTED&page=0&size=20
```

`states`를 생략하면 전체입니다. 기본 페이지 크기는 20, 최대 100입니다.

| 보고 싶은 것 | 조건 |
|---|---|
| (고객) 결제 전 | `states=PENDING_PAYMENT` |
| (고객) 결제 후 ~ 배송 완료 전 | `states=PAID&states=ACCEPTED` |
| (고객) 배송 완료 | `states=DELIVERED` |
| (사장님) 접수해야 할 주문 | `states=PAID` |
| (사장님) 배달 완료 처리할 주문 | `states=ACCEPTED` |

**주문 상태**: `PENDING_PAYMENT`(결제 전) → `PAID`(결제 완료) → `ACCEPTED`(주문 수락) → `DELIVERED`(배달 완료).

**주문 취소와 재주문**

| 상태 | 고객 취소 | 사장님 취소 |
|---|---|---|
| `PENDING_PAYMENT` | O | X |
| `PAID` | O (결제 취소) | O (결제 취소) |
| `ACCEPTED` | X | O (결제 취소) |
| `DELIVERED`, `CANCELED` | X | X |

- 결제된 주문을 취소하면 같은 트랜잭션에서 결제도 `CANCELED`로 바뀝니다. 둘 중 하나만 바뀌는 경우는 없습니다.
- 결제·취소·상태 변경·재주문은 주문 행을 잠그고(`SELECT ... FOR UPDATE`) 처리합니다. 결제와 취소, 고객 취소와 사장님 수락이 동시에 들어와도 하나씩 순서대로 처리되고, 뒤 요청은 바뀐 상태로 다시 검증됩니다.
- 재주문은 취소된 주문을 되살리지 않고 **새 주문(결제 전)** 을 만듭니다. 취소된 주문과 결제 기록은 그대로 남고, 금액은 현재 메뉴 가격으로 다시 계산합니다. 그 사이 삭제된 메뉴가 있으면 404, 이미 재주문한 주문이면 409입니다.

### 결제

| 메서드 | 경로 | 권한 | 설명 |
|---|---|---|---|
| POST | `/payment` | CUSTOMER | 결제(201). 본인 주문만(403), 결제 전 주문만 |

```json
// 요청 (금액은 받지 않고 서버가 주문 총액을 사용)
{ "orderId": 10, "paymentMethod": "CARD" }

// 응답
{ "paymentId": 3, "orderId": 10, "amount": 22000 }
```

결제 수단은 `CARD`만 받습니다.

## 오류 처리

`GlobalException`의 하위 클래스(`BadRequest` 400, `Unauthorized` 401, `Forbidden` 403, `NotFound` 404, `Conflict` 409)를 던지면
`GlobalExceptionHandler`가 해당 상태코드로 응답합니다.
`@Valid` 검증 실패는 400, 예상하지 못한 예외는 500(`서버 오류가 발생했습니다.`)으로 응답하고 원인은 서버 로그에만 남깁니다.

## 프로젝트 구조

```
src/main/java/com/example/delivery
├── global        공통 (config, entity(BaseEntity), exception, security(JWT))
├── user          회원: controller / service / repository / entity / dto
├── menu          메뉴 (기준 구현)
├── order         주문 (OrderService, OrderProcessor)
└── payment       결제
sql/              테이블 DDL, 더미 데이터
CLAUDE.md         작업 규칙 (.claude/rules 에 코드 컨벤션)
TODO.md           요구사항 체크리스트
```