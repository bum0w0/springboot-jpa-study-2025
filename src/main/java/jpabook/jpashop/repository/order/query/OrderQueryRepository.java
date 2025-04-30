package jpabook.jpashop.repository.order.query;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
// findOrderQueryDtos 메서드를 통해 DTO 기반 데이터 조회 구현 (OrderQueryDto와 OrderItemQueryDto를 조합)
// 필요한 데이터만 조회하여 성능을 최적화하고, DTO를 사용해 엔티티 노출을 방지함
public class OrderQueryRepository {

    private final EntityManager em;

    // OrderQueryDto와 OrderItemQueryDto를 조합하여 주문 데이터와 관련된 항목 데이터를 조회
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // 쿼리 1번 -> N개의 결과 (N+1 문제 발생)

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // 쿼리 N번 실행
            o.setOrderItems(orderItems);
        });

        return result;
    }

    // 특정 주문에 대한 주문 항목 데이터(OrderItemQueryDto)를 조회
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    // 주문 데이터(OrderQueryDto)를 조회
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

}
