package com.y0gant.springDemo.repository;

import com.y0gant.springDemo.entity.User;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepoImpl {
    private MongoTemplate template;

    public UserRepoImpl(MongoTemplate template) {
        this.template = template;
    }

    public List<User> getUsersWithSA() {
        Query query = new Query();
        query.addCriteria(Criteria.where("email").regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\\.[A-Za-z]{2,}$"));
        query.addCriteria(Criteria.where("sentiment_analysis").is(true));
        List<User> userList = template.find(query, User.class);
        return userList;
    }


}
