package com.example.earthtalk.domain.debate.entity;

import com.example.earthtalk.domain.user.entity.User;
import com.example.earthtalk.global.baseTime.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "debate_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class DebateUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "debate_id", nullable = false)
    private Debate debate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagType position;// 찬/반 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlagType afterPosition;

    @PrePersist
    public void setDefaultPosition() {
        if (this.position == null) {
            this.position = FlagType.NO_POSITION;
        }

        if (this.afterPosition == null) {
            this.afterPosition = FlagType.NO_POSITION;
        }
    }


}
