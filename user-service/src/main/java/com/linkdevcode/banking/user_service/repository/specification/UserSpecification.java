package com.linkdevcode.banking.user_service.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import com.linkdevcode.banking.user_service.entity.User;
import com.linkdevcode.banking.user_service.model.request.UserSearchRequest;

import jakarta.persistence.criteria.Predicate;

public class UserSpecification {
    public static Specification<User> search(UserSearchRequest request){
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            if(request.keyword() != null && !request.keyword().isBlank()){
                String keywordPattern = "%" + request.keyword().toLowerCase() + "%";
                
                Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), keywordPattern);
                Predicate fullNamePredicate = cb.like(cb.lower(root.get("fullName")), keywordPattern);
                Predicate phonePredicate = cb.like(cb.lower(root.get("phoneNumber")), keywordPattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), keywordPattern);

                predicates.add(cb.or(usernamePredicate, fullNamePredicate, phonePredicate, emailPredicate));
            }

            if(request.status() != null){
                predicates.add(cb.equal(root.get("status"), request.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
