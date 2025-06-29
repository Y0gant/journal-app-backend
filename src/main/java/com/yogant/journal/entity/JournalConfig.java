package com.yogant.journal.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalConfig {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String key;
    private String value;
}
