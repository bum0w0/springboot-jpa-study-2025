package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@Builder
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded // 해당 엔티티에서 @Embeddable 클래스를 사용함
    private Address address;

    @OneToMany(mappedBy = "member") // mappedBy로 연관관계의 주인이 아님을 명시, 실제 외래 키(FK)는 반대쪽(Order.member)에서 관리된다
    private List<Order> orders = new ArrayList<>();

    // @Builder는 내부적으로 전체 필드를 받는 생성자를 만들어 쓰기 때문에, JPA가 사용하는 기본 생성자도 필요
    protected Member() {

    }

}
