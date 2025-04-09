package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
// JPA에서는 상속 관계도 매핑할 수 있음. (자식 클래스별로 구분되는 dtype 컬럼이 생기고, 한 테이블에 다 저장)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype") // 상속 구조의 타입 구분용 컬럼을 설정 (책, 영화, 앨범을 구분하는 컬럼이 생김)
@Getter @Setter
// Book, Album, Movie 같은 구체 클래스들을 만들어 사용하기 위해 추상 클래스로 선언
// List<Item>으로 모든 상품들을 다룰 수도 있음. 다형성을 이용해 공통 로직을 처리
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String name;

    private int price;

    private int stockQuantity;

}
