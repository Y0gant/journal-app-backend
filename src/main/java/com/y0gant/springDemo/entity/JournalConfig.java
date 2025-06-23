package com.y0gant.springDemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalConfig {
    private String key;
    private String value;
}
