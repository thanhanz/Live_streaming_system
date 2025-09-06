package com.thanhan.livestreaming_system.video.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanhan.livestreaming_system.category.entity.Category;
import com.thanhan.livestreaming_system.comment.entity.Comment;
import com.thanhan.livestreaming_system.tag.entity.Tag;
import com.thanhan.livestreaming_system.user.entity.Channel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "Vods")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Vod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnail;

    @Column
    private String videoUrl;

    //An? video
    @Column
    private Boolean published = true;

    @Column
    private Boolean onlyMember = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(name = "views")
    private Long totalView;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @CreationTimestamp
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @UpdateTimestamp
    private Instant updatedAt;

    @OneToMany(mappedBy = "vod", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany
    @JoinTable(name = "vods_tags",
            joinColumns = @JoinColumn(name = "vod_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}
