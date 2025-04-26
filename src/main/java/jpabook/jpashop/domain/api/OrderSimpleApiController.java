package jpabook.jpashop.domain.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2() {
        // 엔티티의 연관된 데이터를 가져오기 위해 추가로 N개의 쿼리가 실행 (ORDER 가 늘어날수록 쿼리가 많이 실행되는 문제 발생)
        // 1 + N + N = 1 + 2N
        // 즉, 각 주문의 member 조회 쿼리와 각 주문의 delivery 조회 쿼리가 각각 N명, N건 실행된다.
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .toList();

        return result;
    }

    // 엔티티로 조회한 뒤 DTO로 변환 (DB에서는 엔티티 전체를 가져오고, 그걸 애플리케이션 레이어에서 필요한 값만 추리는 방식)
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(SimpleOrderDto::new)
                .toList();

        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // LAZY 초기화
        }
    }

}
