package com.thanhan.livestreaming_system.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thanhan.livestreaming_system.livestream.entity.Stream;
import com.thanhan.livestreaming_system.video.entity.Vod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Table(name = "channels")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    private String displayName;
    private String description;
    private String avatarUrl;
    private String bannerUrl;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @CreationTimestamp
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @UpdateTimestamp
    private Instant updatedAt;


    private Integer followersCount = 0;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private List<Vod> vods;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    private List<Stream> streams;

}
