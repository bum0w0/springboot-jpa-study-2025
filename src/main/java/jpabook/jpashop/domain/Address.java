package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable // 내장 타입 (다른 엔티티에 내장되어 사용됨)
@Getter
// JPA 스펙에 따라 엔티티나 임베디드 타입에는 기본 생성자가 반드시 있어야 하며, 해당 생성자는 public 또는 protected로 선언해야 함
// 일반적으로는 protected로 설정하여 외부에서의 무분별한 객체 생성을 방지하는 것이 좋다. (필요한 경우에는 public 생성자를 사용해야 한다는 인지 또한 가능하게 함)
// JPA에서 이러한 제약을 두는 이유는, 내부적으로 객체를 생성할 때 리플렉션(reflection)과 같은 기술을 사용하기 때문
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    private String city;
    private String street;
    private String zipcode;

}
