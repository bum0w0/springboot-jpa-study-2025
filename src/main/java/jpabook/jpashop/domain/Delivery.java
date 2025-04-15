package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
public class Delivery {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(mappedBy = "delivery", fetch = FetchType.LAZY)
    private Order order;

    @Embedded
    private Address address;

    // ORDINAL : 0, 1, 2… 같은 숫자로 DB에 저장
    // 기본값인 ORDINAL을 사용하면 enum 순서 변경이나 값 추가 시 기존 데이터 매핑이 틀어질 수 있으므로, 안정성을 위해 STRING을 사용하는 것이 좋다.
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; // 배송상태 [READY, COMP]

    protected Delivery() {

    }

}
