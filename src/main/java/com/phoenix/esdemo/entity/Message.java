package com.phoenix.esdemo.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.lang.NonNull;

import java.time.Instant;

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
    @NonNull
    private String id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String msg;
    @Field(type = FieldType.Keyword)
    private String sender;
    @Field(type = FieldType.Integer)
    private Integer type;
    @Field(type = FieldType.Date, format = DateFormat.basic_date_time)
    private Instant sendDate;
    @Field(index = false)
    private String icon;
}
