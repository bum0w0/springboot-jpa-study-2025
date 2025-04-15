package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String name;

    @Embedded // 해당 엔티티에서 @Embeddable 클래스를 사용함
    private Address address;

    @OneToMany(mappedBy = "member") // mappedBy로 연관관계의 주인이 아님을 명시, 실제 외래 키(FK)는 반대쪽(Order.member)에서 관리된다
    private List<Order> orders = new ArrayList<>();

}
