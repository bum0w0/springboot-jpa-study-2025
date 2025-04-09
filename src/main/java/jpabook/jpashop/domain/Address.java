package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable // 내장 타입 (다른 엔티티에 내장되어 사용됨)
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

}
