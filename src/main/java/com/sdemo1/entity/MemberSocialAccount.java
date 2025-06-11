package com.sdemo1.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "member_social_account")
public class MemberSocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "msa_id")
    private int msaId;

    @Column(name = "member_id")
    private int memberId;

    @Column(name = "email")
    private String email;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "user_login_id")
    private String userLoginId;

    @Column(name = "user_login_pw")
    private String userLoginPw;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "modified_at", insertable = false, updatable = false)
    private Timestamp modifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    public enum Provider {
        GOOGLE("google"),
        NAVER("naver"),
        KAKAO("kakao");

        private final String value;

        Provider(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Provider fromString(String provider) {
            try {
                return valueOf(provider.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + provider);
            }
        }

        public static Provider fromValue(String value) {
            for (Provider provider : values()) {
                if (provider.value.equals(value)) {
                    return provider;
                }
            }
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다: " + value);
        }
    }
} 