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

/**
 * @Author: zero
 * @Date: 20200627
 * The entity used for elastic search to save messages into index dalex;
 * 对应ES index dalex
 * ID 不可为空
 * 通过Lombok创建getter setter 以及无参数构造函数和全参数构造函数
 * 通过Field注解申明数据存入ES的类型以及解析器
 * icon字段因为是路径地址故申明不建立索引
 */
@Document(indexName = "dalex")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @NonNull
    private String id;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_max_word")
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
