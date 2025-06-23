package com.y0gant.springDemo.repository;

import com.y0gant.springDemo.entity.JournalConfig;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JournalConfigRepo extends MongoRepository<JournalConfig, ObjectId> {

}
