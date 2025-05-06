package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemberRepositoryOld {

    // 엔티티 매니저(EntityManager) 주입
    @PersistenceContext
    private EntityManager em;

    /* 영속성 컨텍스트에 member 객체 넣음 → 트랜잭션이 커밋되는 시점에 DB에 반영
       = 영속성 컨텍스트에 저장되어 있다가, 트랜잭션이 커밋되는 시점에 INSERT SQL이 실행되어 DB에 반영 */
    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        // 쿼리문의 'm'은 JPQL 문법에서 사용하는 엔티티의 별칭(alias)이다.
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }

}
