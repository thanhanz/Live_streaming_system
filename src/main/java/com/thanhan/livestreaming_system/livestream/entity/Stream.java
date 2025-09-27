package com.thanhan.livestreaming_system.livestream.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thanhan.livestreaming_system.chat.entity.ChatMessage;
import com.thanhan.livestreaming_system.user.entity.Channel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @CreationTimestamp
    private Instant createdAt;


    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "viewer_count")
    private Integer viewerCount = 0;

    private Boolean active = true;

    @OneToMany(mappedBy = "stream", orphanRemoval = true, cascade = CascadeType.REMOVE)
    private List<ChatMessage> messageList = new ArrayList<>();
}
