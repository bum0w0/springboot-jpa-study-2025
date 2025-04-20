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

