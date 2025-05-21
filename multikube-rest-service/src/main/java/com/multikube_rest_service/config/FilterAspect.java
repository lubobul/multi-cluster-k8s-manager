package com.multikube_rest_service.config;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class FilterAspect {
    private final EntityManager entityManager;

    public FilterAspect(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Before("execution(* com.chat_mat_rest_service.repositories.*.*(..))")
    public void enableFilter() {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("nonDeletedEntityFilter").setParameter("isDeleted", false);
    }
}
