package com.thanhan.livestreaming_system.livestream.entity;

import com.thanhan.livestreaming_system.user.entity.Channel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Table(name = "StreamSessions")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Stream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String streamKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private StreamStatus status;

    @Column(name = "rtmp_url")
    private String rtmpUrl;


    @Column(name = "created_at")
    @CreatedDate
    private Instant createdAt;


    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "viewer_count")
    private Integer viewerCount = 0;

    // Constructors, getters, setters

}
