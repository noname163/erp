// package com.dat.erp.customannotation.ownership;

// import java.io.Serializable;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationContext;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.repository.support.Repositories;
// import org.springframework.security.access.PermissionEvaluator;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Component;

// import jakarta.persistence.EntityManager;
// import jakarta.persistence.metamodel.EntityType;
// import jakarta.persistence.metamodel.Metamodel;
// import lombok.RequiredArgsConstructor;

// @Component
// public class CustomPermissionEvaluator implements PermissionEvaluator {

//     private final Repositories repositories;
//     private final OwnershipChecker ownershipChecker;
//     private final EntityManager entityManager;

//     @Autowired
//     public CustomPermissionEvaluator(
//             Repositories repositories,
//             OwnershipChecker ownershipChecker,
//             EntityManager entityManager) {
//         this.repositories = repositories;
//         this.ownershipChecker = ownershipChecker;
//         this.entityManager = entityManager;
//     }

//     @Override
//     public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
//         if (targetDomainObject == null)
//             return false;
//         return ownershipChecker.hasOwnership(targetDomainObject, auth);
//     }

//     @Override
//     public boolean hasPermission(Authentication auth,
//             Serializable targetId,
//             String targetType,
//             Object permission) {
//         // Get entity class from JPA Metamodel
//         EntityType<?> entityType = entityManager.getMetamodel()
//                 .getEntities().stream()
//                 .filter(e -> e.getName().equalsIgnoreCase(targetType))
//                 .findFirst()
//                 .orElseThrow(() -> new RuntimeException("Entity not found: " + targetType));

//         Class<?> clazz = entityType.getJavaType();

//         // Find repository dynamically
//         JpaRepository<?, ?> repository = (JpaRepository<?, ?>) repositories
//                 .getRepositoryFor(clazz)
//                 .orElseThrow(() -> new RuntimeException("No repository found for " + clazz.getSimpleName()));

//         // Load entity from repo
//         Object entity = repository.findById(targetId)
//                 .orElseThrow(() -> new RuntimeException("Entity not found with id: " + targetId));

//         // Delegate ownership check
//         return ownershipChecker.hasOwnership(entity, auth);
//     }

// }
