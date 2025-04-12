## 도메인 분석 설계

### 연관관계 매핑 
- 객체 지향 프로그래밍의 객체 간 관계를, 관계형 데이터베이스의 테이블 간 외래 키(FK) 관계로 매핑하는 것

  JPA에서는 이를 통해 엔티티 간의 관계를 정의하고, SQL 없이 객체 지향 방식으로 데이터를 조작

### 연관관계 매핑이 필요한 이유
- 자바는 참조(reference)를 통해 객체 간 관계를 맺고, 데이터베이스는 외래 키(foreign key)를 통해 테이블 간 관계를 맺음

    → 이 둘 사이의 패러다임 불일치(Paradigm Mismatch) 를 해결하기 위해 연관관계 매핑이 필요

### 엔티티와 테이블 매핑
- 자바에서는 객체(Entity)로 데이터를 다루고, 데이터베이스에서는 테이블로 데이터를 저장함.
- JPA는 @Entity 애너테이션을 사용해 자바 클래스와 테이블 간 매핑을 제공하며, 이를 통해 개발자는 SQL을 직접 작성하지 않고 객체 중심의 프로그래밍이 가능함.

    ```java
    @Entity
    @Table(name = "member")
    public class Member {
        @Id @GeneratedValue
        private Long id;
        
        private String name;
    }
  
### 연관관계 매핑의 종류
- 관계형 데이터베이스에서는 외래 키(FK)를 통해 테이블 간 관계를 설정하지만, 객체는 참조(Reference)를 통해 관계를 맺음.
- JPA에서는 다양한 애너테이션을 제공하여 테이블 간 관계를 객체 간 관계로 매핑할 수 있음.
> - @OneToOne: 일대일 관계
> - @OneToMany: 일대다 관계
> - @ManyToOne: 다대일 관계 (가장 일반적)
> - @ManyToMany: 다대다 관계 (중간 테이블이 필요함) 

> [참고] @ManyToMany는 사용을 지양하고, 중간 테이블을 위한 엔티티를 직접 생성해 @ManyToOne, @OneToMany로 구성하는 것이 바람직함.
자동 생성되는 중간 테이블은 외래 키 외에 추가 필드를 포함할 수 없기 때문

### 단방향 & 양방향 매핑
#### 단방향 매핑: 한쪽 엔티티만 관계를 알고 있는 구조
> - 자바 코드 상에서는 단방향이지만, 외래 키(FK)는 생성됨. 
> - 예: Member → Team 참조 시 member 테이블에 team_id FK 생성

#### 양방향 매핑: 양쪽 엔티티가 서로를 참조하는 구조
> - 연관관계의 주인을 명시해야 하며, 주인 쪽에서 외래 키를 관리

### 연관관계 주인과 mappedBy
- 양방향 연관관계에서 mappedBy는 연관관계의 주인이 아닌 쪽에 사용됨.
- 연관관계의 주인은 외래 키가 있는 쪽이며, JPA는 주인 엔티티를 통해 연관관계를 관리
> [참고] 외래 키가 있는 곳을 연관관계의 주인으로 정해라.
> - 연관관계의 주인은 단순히 외래 키를 누가 관리하냐의 문제이지 비즈니스상 우위에 있다고 정하면 안된다.
> - 자동차를 연관 관계의 주인으로 정하면 자동차가 관리하지 않는 바퀴 테이블의 외래 키 값이 업데이트 되므로 관리와 유지보수가 어려울 수 있다는 점을 생각해보면 된다.
```java
// 연관관계의 주인
@ManyToOne
@JoinColumn(name = "team_id")
private Team team;

// 주인이 아님 (읽기 전용)
// 주인이 아니기 때문에 이 컬렉션을 수정해도 DB의 외래 키에는 변화가 없음
@OneToMany(mappedBy = "team")
private List<Member> members = new ArrayList<>();
```
- mappedBy가 설정된 비주인 쪽을 통해 연관관계를 설정했을 때의 예시
```java
Team teamA = new Team();
Member member = new Member();

// 아래처럼 설정하면 DB에 반영됨 (주인 쪽에서 설정했기 때문)
member.setTeam(teamA); // 외래 키 값이 실제로 설정됨

// 반면 이렇게만 해서는 DB에 아무 영향 없음
teamA.getMembers().add(member); // 연관관계 설정 X
```
### 연관관계에서 외래 키(FK) 위치 원칙
#### 다대일(N:1) 관계
- 외래 키는 항상 "다(N)" 쪽에 위치함.
- 여러 개의 `Order`는 하나의 `User`에 속하므로, `Order` 테이블에 `user_id` 외래 키가 존재.
- JPA에서도 `@ManyToOne`이 있는 쪽이 연관관계의 주인이 되며, 외래 키를 소유하게 됨.

#### 일대일(1:1) 관계
- 외래 키를 어느 테이블에 둘 것인지는 자유롭지만, 일반적으로 다음 기준에 따라 결정
  - **더 자주 조회되는 테이블에 FK를 둔다.**
  - **주 테이블이 명확하면 보조 테이블에 FK를 둔다.**
- FK에는 `UNIQUE` 제약 조건을 걸어 1:1 관계를 보장해야 함.

#### 일대다(1:N)
- “일대다”는 단지 조회 편의를 위한 양방향 매핑의 반대편일 뿐
- 실질적인 관계 관리는 @ManyToOne에서만 일어남
  ```java
  // 양방향 매핑의 반대편 예시
  @OneToMany(mappedBy = "parent")
  private List<Category> child = new ArrayList<>();
  ```
  
#### 엔티티에서 Setter를 지양해야 하는 이유
- 단순하게 모든 필드에 setter를 열어두면, 객체 상태가 아무 때나 외부에서 바뀔 수 있어서 추적이 어려움
- JPA에서는 엔티티의 변경사항을 추적하고 변경 감지를 해야 하는데, setter를 남용하면 어떤 시점에 어떤 값이 바뀐 건지 파악이 안 됨
- 불필요하게 엔티티의 상태가 계속 바뀌게 되면, 영속성 컨텍스트가 감지하는 변경이 많아지고 성능에도 영향이 있음
- 비즈니스 로직과 관련된 상태 변경은 명확한 의미를 가진 메서드로 제공하는 게 좋음


#### 모든 연관관계는 지연 로딩(Lazy Loading)으로 설정
- 즉시 로딩(Eager Loading)은 SQL 실행 시 연관된 엔티티를 함께 가져오게 되는데, 이로 인해 실행되는 SQL을 예측하기 어렵고, 어떤 시점에 어떤 쿼리가 나갈지 추적하기 힘들다.
- JPQL을 사용할 때 N+1 문제가 자주 발생할 수 있으며, 연관된 엔티티를 함께 조회해야 하는 경우엔 fetch join 또는 @EntityGraph 같은 기능을 명시적으로 사용하는 것이 좋다.
- 또한, JPA에서는 @ManyToOne, @OneToOne 관계의 기본 fetch 전략이 즉시 로딩(EAGER)이므로, 지연 로딩(LAZY)으로 변경해주는 것이 권장된다.
> - N+1 문제 : 각 엔티티의 연관된 데이터를 가져오기 위해 추가로 N개의 쿼리가 실행되는 문제
> - Fetch Join : 연관된 엔티티를 한 번에 조회하기 위해 사용하는 JPQL의 JOIN 문법
> - @EntityGraph : JPQL 없이도 특정 엔티티를 조회할 때, 연관된 엔티티를 함께 로딩할 수 있게 도와주는 기능

#### [참고] open-in-view 설정과 Lazy Loading
- `spring.jpa.open-in-view=true`는 컨트롤러나 뷰 렌더링까지 영속성 컨텍스트(엔티티 매니저)를 열어두는 설정
- 개발 초기, 혹은 뷰에서 Lazy Loading이 편하게 필요할 때는 true로 설정
- 실무에서는 계층 간 책임 분리와 성능 최적화를 위해 false로 설정하고, 필요한 데이터는 서비스에서 전부 로딩해서 넘기는 구조를 사용
> #### `open-in-view=true`로 설정해도 문제는 생기지 않지만 유지보수 측면에서 권장되지 않음
> - Controller, View에서도 DB에 접근할 수 있다는 점에서 계층 분리 원칙이 깨짐
> - 서비스 로직이 어디서 끝났는지 명확하지 않아 트랜잭션 범위가 애매함

#### 엔티티 설계 시 컬렉션은 필드에서 초기화
- null 문제에서 안전하고, 코드도 간결하다.
- 하이버네이트는 엔티티를 영속화 할 때, 컬렉션을 감싸사 하이버네이트가 제공하는 내장 컬렉션으로 변경한다. 따라서 임의의 메소드에서 컬렉션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다.

> [복습] 영속화(Persistence)
> - 엔티티 객체를 JPA가 관리하는 상태(영속 상태)로 만드는 것.
> - EntityManager.persist(entity)를 호출하면, 해당 객체는 영속성 컨텍스트에 저장됨.
> - 영속화된 엔티티는 트랜잭션 커밋 시점에 DB에 자동 반영(플러시)됨.
> - 자바 객체 → JPA 관리 → DB 반영 의 흐름에서 “JPA 관리” 단계가 바로 영속화임.

> [참고] 하이버네이트(Hibernate)
> - JPA의 구현체 중 하나로, JPA의 표준 인터페이스를 실제로 동작하게 만드는 라이브러리.
> - JPA를 사용하면 하이버네이트가 내부에서 작동하여 SQL 생성, DB 연결, 트랜잭션 처리 등을 해줌.

#### 영속성 전이
- 한 엔티티의 생명주기 변화(저장, 삭제 등)가 연관된 다른 엔티티에도 전파되는 것
- 부모 엔티티를 persist, remove 등 할 때 연관된 자식 엔티티도 자동으로 함께 처리되는 기능
- 부모를 저장하거나 삭제할 때 자식을 항상 같이 처리해야 할 때 사용함.

#### 연관관계 편의 메소드
- 양방향 연관관계에서 두 객체 간의 관계를 한쪽에서만 설정하는 것이 아니라 양쪽 모두에 자동으로 설정되도록 도와주는 메서드
- 사용하는 이유 :  ORM은 객체와 DB를 동기화해주는 도구이기 때문에, 자바 객체 관계도 맞아야 하고, DB 외래 키도 맞아야 정상 동작
> - DB에서는 외래 키(FK) 하나만 설정하면 연관관계가 끝이지만, 자바에서는 객체 간 관계를 양쪽에서 따로 유지해야 함.
```java
// 양방향 관계에서는 객체의 양쪽에서 모두 관계를 설정해줘야 자바 객체 관점에서 올바른 연관관계가 성립

// Member.java
@ManyToOne
@JoinColumn(name = "team_id")
private Team team;

// Team.java
@OneToMany(mappedBy = "team")
private List<Member> members = new ArrayList<>();
```
아래 처럼 연관관계 편의 메소드를 정의할 수 있음. (객체 세계에서도 양쪽 방향을 모두 관리 해야 함)
```java
// Member.java
public void changeTeam(Team team) {
    this.team = team;
    team.getMembers().add(this);
}
```
편의 메소드 위치는 연관관계의 주인 이거나 비즈니스 로직상 책임을 지는 쪽에 두는 것이 좋은 방식

## 회원 도메인 개발
#### 트랜잭션
- 트랜잭션은 하나의 작업 단위로, 여러 작업을 하나처럼 묶어 모두 성공하거나, 모두 실패하게 만드는 기능
#### Entity Manager (엔티티 매니저)
- JPA에서 데이터베이스와 상호작용하는 핵심 객체
> JPA는 인터페이스만 제공하고, 실제로는 하이버네이트 같은 구현체가 내부적으로 EntityManager를 동작시켜줌.
즉, 우리가 em.persist() 이런 걸 쓰면, JPA가 하이버네이트를 통해 SQL로 바꿔서 DB에 날려주는 구조

- 엔티티 매니저가 하는 일

  | 메서드            | 설명                                                  |
  |-------------------|-----------------------------------------------------|
  | `persist()`       | 엔티티를 영속성 컨텍스트에 저장하고 DB에 INSERT 함                    |
  | `find()`          | PK로 엔티티를 조회함                                        |
  | `remove()`        | 엔티티를 삭제함 (DELETE)                                   |
  | `merge()`         | 분리(detached) 상태의 엔티티를 영속 상태로 변경                     |
  | `createQuery()`   | JPQL을 사용한 쿼리를 생성하고 실행함 (Ex. 단건 조회가 아닌 전체 조회가 필요할 때) |

- JPA를 직접 사용하는 레포지토리 클래스에서 사용함.
  ```java
  @Repository
  public class MemberRepository {
  
      //스프링이 현재 트랜잭션 범위에서 관리되는 EntityManager를 주입
      @PersistenceContext
      private EntityManager em;
  
      public void save(Member member) {
          em.persist(member);
      }
  
      public Member find(Long id) {
          return em.find(Member.class, id);
      }
  
  }
  ```

#### @PersistenceContext
- JPA에서 엔티티 매니저(EntityManager)를 주입받을 때 사용하는 어노테이션
- Spring 환경에서 JPA를 사용할 때, 영속성 컨텍스트를 자동으로 연결해주는 역할
> [참고] @Autowired를 사용하여 주입 받는 것과 차이가 있는가?
> @Autowired EntityManager도 가능하긴 한데, JPA에서는 트랜잭션 연계 등 더 정교하게 관리되기 때문에 @PersistenceContext 사용

#### 트랜잭션 범위에서의 EntityManager 관리

- 스프링은 트랜잭션마다 새로운 EntityManager를 생성하고, 해당 트랜잭션 내에서만 사용하는 EntityManager를 자동으로 주입해준다.  
- 동일 트랜잭션 내에서는 동일한 영속성 컨텍스트가 보장되며, 이를 통해 1차 캐시, 변경 감지 등의 기능이 일관되게 동작한다.
#### [복습] 영속성 컨텍스트
- 엔티티를 저장해두는 JPA 내부의 일종의 메모리(1차 캐시)
- JPA는 이 엔티티 저장소를 통해 객체들을 관리

> 1. JPA는 엔티티 객체를 데이터베이스에 바로 저장하지 않고 영속성 컨텍스트에 먼저 저장해 둠.
> 2. 트랜잭션이 커밋되는 순간에, 영속성 컨텍스트에 저장된 변경사항들을 모아서 한번에 DB에 반영

- 위 과정을 거치는 이유
1. 성능 최적화 : 쿼리를 즉시 보내지 않고 모아서 보냄
2. 변경 감지(Dirty Checking) : 객체의 필드 값이 바뀌면, JPA가 이를 감지해서 자동으로 update 쿼리를 만들어 줌
3. 1차 캐시 역할 : 같은 객체를 다시 조회하면 DB에 가는 것이 아니라, 영속성 컨텍스트에 있는 객체를 찾아서 반환
   (동일한 트랜잭션 내에 있다면, 동일한 객체가 반환된다)
  ```java
    @Transactional
    public void example() {
        Member m1 = em.find(Member.class, 1L); // 첫 번째 조회 → DB에 다녀옴 + 영속성 컨텍스트에 저장
        Member m2 = em.find(Member.class, 1L); // 두 번째 조회 → DB에 안 감 캐시에서 바로 가져옴

       System.out.println(m1 == m2); // true
    }
  ```

#### @Transactional
트랜잭션 처리를 자동으로 관리해주는 기능. 작업 도중 오류가 발생하면 자동으로 롤백하고, 성공 시 커밋

여러 DB 작업이 하나의 작업처럼 처리되어야 할 때 사용
- 예: 게시글 작성 시, 게시글 테이블과 첨부파일 테이블에 동시에 insert 해야 하는 경우
- 둘 중 하나라도 실패하면 전체를 롤백해야 함

읽기 전용이면 readOnly = true 설정 → 성능 향상 가능
> - 트랜잭션이 필요한 메서드는 Service 계층에 작성하는 것이 일반적 (비즈니스 로직 중심)
> - 테스트에서도 @Transactional 사용 가능 → 테스트 종료 시 자동 롤백

#### [참고] @Builder
- Lombok에서 제공하는 애노테이션으로, 객체 생성 시 가독성과 유연성을 높여주는 빌더 패턴을 자동으로 생성
>  테스트 코드에서 사용하는 이유
> 1. 객체 생성 시 매개변수 순서를 헷갈릴 필요가 없음
> 2. 원하는 필드만 선택적으로 설정 가능
> 3. 테스트 코드 유지보수 용이
> ```java
> @Getter
> @Builder
> public class User {
>     private String name;
>     private int age;
> }
> 
>----------------------------
> 
> User user = User.builder()
>       .name("홍길동")
>       .age(30)
>       .build();
>```

#### [참고] assertThrows (jUnit5)
- 예외가 발생하는지 검증하는 JUnit5의 메서드
  
1. 람다식으로 예외 발생 지점을 명확히 지정 가능
2. 예외 메시지나 예외 객체 내부 상태까지도 검증 가능
```java
assertThrows(예외타입.class, () -> 예외가 발생할 코드를 람다로 작성);
```

#### [참고] assertThat (AssertJ)
- 객체나 값이 예상과 일치하는지 검증할 수 있는 메서드
- JUnit의 `assertEquals`, `assertTrue`보다 더 직관적이고 다양한 조건 비교가 가능함
- 메서드 체이닝 방식으로 다양한 조건을 표현할 수 있어 복잡한 테스트에서도 명확한 의도를 전달할 수 있음

```java
assertThat(actual).isEqualTo(expected);        // 값 일치
assertThat(list).hasSize(3);                   // 리스트 크기 검증
assertThat(string).startsWith("prefix");       // 문자열 시작 검사
assertThat(object).isInstanceOf(User.class);   // 객체 타입 확인