package jpabook.jpashop.domain.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 이 컨트롤러는 xToOne(연관관계: ManyToOne, OneToOne) 관계에서 성능 최적화를 어떻게 해야 하는지를 다룬다.
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화 (원래 지연 로딩은 필요할 때까지 DB 접근을 미루지만, 지연 로딩으로 설정된 연관 객체에 접근해서 미리 로딩시킴.)
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }


}
