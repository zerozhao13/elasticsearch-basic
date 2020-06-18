package com.phoenix.esdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "dalex")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
/**
 * The entity used for elastic search to save messages into index dalex;
 *
 */
public class Message {
    @Id
    private String id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String msg;
    @Field(type = FieldType.Keyword)
    private String sender;
    @Field(type = FieldType.Integer)
    private Integer type;
    @Field(type = FieldType.Date)
    private String sendDate;
    @Field(index = false)
    private String icon;
}
