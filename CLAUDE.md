# delivery (order) 프로젝트 가이드

Spring Boot 4 / Java 17 / Gradle / JPA / PostgreSQL(운영), H2(테스트).
패키지 루트: `com.example.delivery`

## 구조
- `global/` : 공통 (config, entity, exception, security)
- 도메인(`user`, `menu`, `order`, `payment`)마다 `presentation → application → domain ← infrastructure`, 그리고 `dto(request, response)`
  - `domain`: 엔티티, enum, 저장소 인터페이스(`MenuRepository`). JPA 구현을 모른다
  - `infrastructure`: 저장소 구현(`MenuRepositoryImpl`), Spring Data(`MenuJpaRepository`), 조회용 QueryDSL(`MenuQueryRepository`)
- 모든 엔티티는 `global.entity.BaseEntity`를 상속한다 (createdAt, updatedAt 자동 기록, JPA Auditing).

## 코드 컨벤션
`menu` 패키지가 기준 구현이다. 세부 규칙은 `.claude/rules/`의 파일을 따른다 (`naming`, `layers`, `test`). 새 도메인은 `menu`와 같은 구조와 이름 규칙으로 만든다.

## 작업 규칙
- 코드를 수정하면 반드시 `./gradlew build`(컴파일 + 테스트)로 검증하고, 실패하면 원인을 고친 뒤 다시 돌린다.
- 새 기능에는 테스트를 같이 작성한다 (`src/test/java` 아래 같은 패키지 구조).
- 엔티티 필드/컬럼을 바꾸면 DB DDL(`ALTER TABLE ...`)도 함께 안내한다. `ddl-auto: update`는 컬럼 이름 변경/삭제를 반영하지 못한다.
- DDL 문서는 sql/ 폴더 밑으로 해당 테이블 명으로 작성한다.
- **저장소는 `domain`의 인터페이스(`MenuRepository`)와 `infrastructure`의 구현(`MenuRepositoryImpl`)으로 나눈다.** 서비스는 인터페이스만 쓴다 (`.claude/rules/layers.md`).
- **조회는 반드시 QueryDSL + `@QueryProjection` DTO로 한다.** 쿼리 메서드(`findBy...`)로 엔티티를 조회해 DTO로 변환하지 않는다. 쿼리 메서드는 수정/삭제처럼 엔티티가 필요한 경우에만 쓴다.
- **질문에는 코드를 수정하지 않는다.** "어떻게 해?", "왜 그래?", "~는 어때?" 같은 질문에는 설명과 예시만 답하고, "구현해줘", "수정해줘", "만들어줘"처럼 명시적으로 요청했을 때만 파일을 변경한다. 애매하면 구현 여부를 먼저 묻는다.
- 요청에 없는 리팩터링이나 파일 추가는 하지 않는다. 한 일은 짧게 보고한다.
- **최소한으로, 단순하게 구현한다.** 요청한 기능을 충족하는 가장 단순한 코드만 작성한다.
  - 요구되지 않은 추상화, 인터페이스, 범용 유틸, 설정 옵션, 방어 코드는 만들지 않는다.
  - 이미 있는 코드와 어노테이션으로 해결되면 새 클래스나 의존성을 추가하지 않는다.
  - 어노테이션은 실제로 필요한 것만 붙인다 (예: 기본 생성자가 이미 있으면 `@NoArgsConstructor` 불필요).
  - 선택지가 여러 개면 가장 단순한 방법을 고르고, 복잡한 대안은 필요할 때 제안만 한다.
- 답변은 한국어로 한다.

## 검증 명령
- 전체: `./gradlew build`
- 테스트만: `./gradlew test`
- 단일 테스트: `./gradlew test --tests "com.example.delivery.order.application.OrderServiceTest"`
