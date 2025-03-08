package com.example.earthtalk.domain.debate.entity;

import com.example.earthtalk.domain.user.entity.User;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

@Getter
public class VoteStatus {
    private Long pro;
    private Long con;
    private Long neutral;
    private final Set<Long> users;

    public VoteStatus() {
        this.pro = 0L;
        this.con = 0L;
        this.neutral = 0L;
        this.users = new HashSet<>();
    }

    public void incrementPro() {
        this.pro++;
    }
    public void incrementCon() {
        this.con++;
    }
    public void incrementNeutral() {
        this.neutral++;
    }
}
