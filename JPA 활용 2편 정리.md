## API 개발 기본
### DTO의 필요성 (Entity 변경과 API 스펙 분리 관점)
> DTO : 계층 간 데이터 전달을 위해 사용하는 객체로, 비즈니스 로직과는 무관한 데이터를 담는 그릇
> <br><br> Ex. 요청 및 응답 DTO 설계
> - 클라이언트 → 서버에 요청할 때 
> - 서버 → 클라이언트에 응답할 때

**1. 엔티티는 내부 도메인 모델**
- DB와 비즈니스 로직 중심으로 설계되어 있으며, 내부 시스템의 구현에 초점을 맞춤
- 이를 API에 직접 노출하면 내부 구조가 외부에 드러나 보안 및 유연성 측면에서 문제가 발생할 수 있음

**2. 엔티티 변경 시 API 스펙도 함께 변경되는 문제**
- 엔티티에 필드가 추가되거나 삭제되면, 이를 그대로 사용하는 API 응답 형식도 함께 바뀔 수 있음

**DTO를 사용해 API 스펙을 고정해야 한다.**
- 엔티티 구조가 변경되더라도 DTO를 통해 API 응답 형식을 일정하게 유지 가능
- 필요한 데이터만 선택적으로 전달 가능
---
### 배열을 사용한 JSON 응답의 확장성과 유연성 문제
- 스펙 고정화: 배열 내부 구조에 의존하게 되어 응답 형식이 고정됨. 이후 구조 확장이나 수정이 어려움.
- 구조 변경의 어려움: 새로운 필드를 추가하거나 구조를 변경할 경우, 구조가 배열로 고정되어 있어 큰 리팩토링이 필요할 수 있음
```java
    // 배열을 사용한 응답 (스펙 고정화 문제 발생)
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }
    
    // Result 객체로 래핑하여 응답. 추후에 count, status, metadata 등 다른 필드를 유연하게 추가할 수 있음
    @GetMapping("/api/v2/members")
    public Result membersV2() {
            List<Member> findMembers = memberService.findMembers();
            List<MemberDto> collect = findMembers.stream()
            .map(member -> new MemberDto(member.getName()))
            .toList();

        return new Result(collect);
    }
```
---
## API 개발 고급
### 지연 로딩과 조회 성능 최적화

---
> **'엔티티를 외부에 노출 했을 경우'** 를 가정하고 발생하는 문제점 및 해결 방안이기 때문에 간단히 보고 넘어가기
### 양방향 연관관계에서 순환참조 문제
- JPA에서 양방향 연관관계를 그대로 JSON으로 변환하면 무한 루프가 발생할 수 있음  
  (예: Member → orders → member → orders ...)
- 이 문제를 막기 위해 한쪽 필드에 `@JsonIgnore`를 사용하여 직렬화 대상에서 제외해야 함
- 대안: DTO로 필요한 데이터만 반환
```java
class Order {
// 양방향 연관관계에서는 직렬화 시 무한 재귀 문제가 발생할 수 있음.
// 이를 방지하기 위해 한쪽에 @JsonIgnore를 반드시 붙여야 함
    @ManyToOne
    @JsonIgnore
    private Member member;
}
```
---
### @JsonIgnore를 써도 무한 순환이 발생하는 이유

- JPA는 연관관계에 LAZY 로딩을 사용하는데, 이때 Hibernate는 프록시 객체를 생성함 (ByteBuddy, CGLIB 등 사용)
- 이 프록시 객체는 실제 엔티티가 아니라 Hibernate가 만든 가짜 객체
- Jackson(ObjectMapper)이 이 프록시를 직렬화할 때 내부를 건드리며 다시 연관된 객체를 불러오고, 그 과정에서 순환 참조가 발생할 수 있음

> ObjectMapper란?
> - Jackson 라이브러리에서 제공하는 객체로, 자바 객체와 JSON 간의 변환(직렬화/역직렬화)을 담당함
> - 기본적으로 Spring Boot에서는 ObjectMapper가 자동으로 등록

해결 방법

1. Hibernate 모듈을 ObjectMapper에 등록해 프록시 직렬화 문제 방지
   1. Hibernate의 프록시 객체나 지연 로딩 구조를 Jackson이 인식하여,
        JSON 변환 시 오류 없이 안전하게 직렬화 하도록 함
2. DTO 사용 (엔티티 직접 노출 X, 필요한 데이터만 반환)
---

### 프록시(Proxy)란?

- JPA에서 `LAZY` 로딩을 사용할 때, 엔티티 대신 가짜 객체(프록시 객체)를 반환함
- 이 프록시는 실제 데이터를 가지고 있지 않고, **필요할 때 진짜 객체를 로딩**함 (지연 로딩)

왜 프록시를 쓰는가?

- 연관된 객체를 **무조건 즉시 조회하지 않고**, 실제로 필요할 때만 DB에서 불러오기 위해 사용
- Ex. `order.getMember()`를 호출하기 전까지는 `member` 엔티티가 DB에서 조회되지 않음

프록시의 동작 방식

- Hibernate는 **ByteBuddy** 또는 **CGLIB** 등의 라이브러리를 이용해 프록시 클래스를 동적으로 생성
- 실제 클래스의 하위 클래스를 만들고, 메서드를 오버라이딩해서 데이터 접근 시점에 실제 객체를 불러오게 만듦

```java
Member member = em.find(Member.class, 1L); // 실제 객체
Member proxy = em.getReference(Member.class, 1L); // 프록시 객체
```
---
### 엔티티 노출과 Lazy Loading 최적화 정리

#### 1. 엔티티 직접 노출은 피해야 한다
- 엔티티를 직접 반환하면 **Lazy Loading**으로 인해 **N+1 문제**와 같은 성능 문제가 발생할 수 있음.
- 연관된 엔티티가 불필요하게 조회되는 것을 피해야 함.

#### 2. Lazy Loading과 DTO 사용
- **Lazy Loading**된 엔티티까지 조회할 필요가 없다면, **DTO**(Data Transfer Object)를 사용하여 필요한 데이터만 선택적으로 전달해야 함.
- **DTO**를 사용하면 연관된 엔티티 조회를 방지하고 성능을 최적화할 수 있음.

---

### JPA의 N + 1 문제
- JPA에서 지연 로딩(LAZY)을 사용할 때 발생하는 성능 이슈
- 예를 들어 `Order` 엔티티를 조회하고, 각 `Order`에서 `Member`와 `Delivery` 정보를 접근하면 발생
#### 예시
>회원 N + 배송 N = N개의 주문에 대해 각 회원/배송을 지연 로딩하면서 총 2N개의 추가 쿼리가 발생하는 문제

```java
// 주문이 많아질수록 member와 delivery 쿼리 수도 기하급수적으로 증가
// 해당 예시에서는 쿼리가 총 1 + N + N 번 실행
List<Order> orders = orderRepository.findAllByString(new OrderSearch());
for (Order order : orders) {
    order.getMember().getName();      // 회원 정보 조회 (N번 발생)
    order.getDelivery().getAddress(); // 배송 정보 조회 (N번 발생)
}
```
- Order의 결과가 2건이라면, 각 주문마다 다른 회원 또는 다른 배송 정보일 경우 최악의 경우 1 + 2 + 2번의 쿼리가 실행
- 단, JPA의 지연 로딩은 영속성 컨텍스트를 통해 이미 조회된 엔티티는 재조회하지 않기 때문에,
같은 회원 또는 배송 정보가 중복된다면 1 + 1 + 1로 줄어듬

---

### 페치 조인 (Fetch Join)

#### 페치 조인이란?
- 연관된 엔티티를 즉시 함께 조회 (지연 로딩 무시)
- 한 번의 SQL로 필요한 데이터 모두 조회 (N+1 문제 해결)
- 조회한 엔티티는 모두 영속성 컨텍스트에 저장됨
- JPQL에서만 사용 가능 (`fetch` 키워드)

```java
// 페치 조인 (연관 엔티티를 즉시 함께 가져옴)
SELECT o FROM Order o JOIN FETCH o.member
public List<Order> findAllWithMemberDelivery() {
    return em.createQuery(
            "select o from Order o" +
            "join fetch o.member m" +
            "join fetch o.delivery d", Order.class
        ).getResultList();
}
// 페치 조인으로 order -> member, order -> delivery는 이미 조회된 상태이므로 지연로딩 자체가 일어나지 않음
```
Fetch Join 주의사항

- `@OneToMany`(1:N) 관계에서 페치 조인 시 **중복 결과** 발생 가능 → **`distinct`** 사용 필요
- 너무 많은 테이블을 동시에 페치 조인하면 **쿼리 성능 저하**
- 필요한 연관관계에 한해서만 선택적으로 사용해야 함

#### 기본은 지연로딩, 필요한 경우에만 페치조인 → 90% 이상의 성능 문제 해결
> - Fetch Join은 주 엔티티(A)와 연관된 데이터(b, c)를 JOIN으로 묶어 한 번에 조회하는 방법이다.
> - 비유하자면, 마트에 여러 번 가지 말고, 큰 카트에 필요한 걸 한 번에 담아 오는 것

#### 실무에서 정말 자주 사용하는 기법 중 하나 (Fetch Join)

- 서비스를 만들 때는 '이 데이터와 저 데이터는 항상 같이 쓰인다'는 **업무 전제**가 깔려 있는 경우가 많음
- 예를 들어, **배달앱**에서는 '주문 내역'을 조회할 때 **회원 정보**와 **배달 기사 정보**를 함께 보여줘야 함
- 이런 경우 **연관된 데이터를 한 번에 가져오는 Fetch Join**을 사용하면 성능을 크게 최적화할 수 있음
- 즉, "같이 쓰일 게 뻔한" 데이터는 페치 조인으로 미리 묶어 가져오는 것이 실무에서는 매우 일반적

---
### DTO 직접 조회를 통한 성능 최적화

- 페치 조인은 연관된 엔티티 전체를 통으로 조회하기 때문에, 필요한 데이터만 뽑아오는 게 불가능
- 따라서 실제로 필요한 데이터보다 더 많은 양을 조회하지 않도록 최적화를 고려해야 함

#### 최적화 방안
- DTO 직접 조회 최적화
- QueryDSL 사용

#### 판단 기준
- 자주 함께 사용하는 데이터라면 ➔ **Fetch Join**
- 필요한 일부 데이터만 조회해야 하면 ➔ **DTO 조회**
- 재사용성의 차이도 존재 (범용적이지 못한 쿼리가 될 수 있다는 의미)

#### 예시

- **Fetch Join**
  : 배달 앱에서 주문(Order) + 회원(Member) + 배송(Delivery)을 항상 함께 조회할 때

- **DTO 조회**
  : 관리자 페이지에서 회원 이름, 이메일, 가입일만 조회할 때
  : Member 테이블에는 실제로는 수십 개 컬럼이 있지만(주소, 생년월일 등) 일부 컬럼만 필요

> - JPQL에서 new 명령어를 사용해 조회 결과를 DTO로 즉시 변환
> - 필요한 필드만 선택해서 조회하므로 **네트워크 전송량을 줄일 수 있음**
> - 그러나 네트워크 최적화 효과는 생각보다 미미한 경우가 많음
> - API 스펙에 맞춘 조회가 되다 보니, 해당 레포지토리 메서드는 **재사용성이 떨어질 수 있음**
---
### 엔티티를 DTO로 변환 vs DTO로 바로 조회

#### 1. 사실 필드 개수는 성능에 큰 영향이 없다
- 조회 쿼리가 실행될 때, 필드 몇 개 더 가져와도 성능 차이는 거의 없음
- RDB는 블록 단위로 읽기 때문에 미세한 데이터 증가는 무시 가능

#### 2. 진짜 성능을 잡아먹는 것들
- **JOIN** : 불필요한 조인이 많으면 디스크 I/O, 메모리 사용량 증가
- **WHERE 조건** : 인덱스 없이 조회하면 풀 스캔 발생

#### 3. 결론
- 엔티티를 DTO로 변환(페치 조인)과 DTO 직접 조회는 각각 장단점이 있음
- API 호출 빈도나 사용 패턴에 따라 적절히 섞어 사용하는 것이 가장 좋다
- 요구사항과 사용 빈도에 맞춰 유연하게 선택하는 것이 핵심
> 도메인 패키지에는 엔티티랑 순수한 기본 Repository만, API 최적화 쿼리는 별도 패키지로 관리하는 구조가 실무에서도 매우 좋은 전략 (도메인 모델은 건드리지 말고, API 최적화는 따로 관리)

---

