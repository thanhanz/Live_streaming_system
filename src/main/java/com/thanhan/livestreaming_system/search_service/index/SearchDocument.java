package com.thanhan.livestreaming_system.search_service.index;

import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "search_index")
@Getter
@Setter
@Data
public class SearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String entityId;

    @Field(type = FieldType.Keyword)
    private String type; // "channel", "video", "live"

    // --- common fields ---
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword, index = false)
    private String thumbnailUrl;

    @Field(type = FieldType.Boolean)
    private Boolean isOnlyMember;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Date)
    private String createdAt;

    // --- channel info ---
    @Field(type = FieldType.Keyword)
    private String channelId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String channelName;

    @Field(type = FieldType.Keyword, index = false)
    private String channelAvatar;

    @Field(type = FieldType.Long)
    private Long channelFollowers;

    @Field(type = FieldType.Keyword, index = false)
    private String avatarUrl;

}
