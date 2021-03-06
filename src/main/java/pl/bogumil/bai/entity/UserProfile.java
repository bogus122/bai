package pl.bogumil.bai.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import pl.bogumil.bai.entity.common.EntityBase;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by bbierc on 2016-03-31.
 */
@Table(name = "USER_PROFILE")
@Entity
@Getter
@Setter
public class UserProfile extends EntityBase {
    @Column(name = "LOGIN", unique = true)
    private String login;
    @Column(name = "PASSWORD")
    private String password;
    @Column(name = "LAST_LOGIN_DATE", columnDefinition = "DATETIME")
    private LocalDateTime lastLoginDate;
    @Column(name = "LAST_FAILED_LOGIN_DATE", columnDefinition = "DATETIME")
    private LocalDateTime lastFailedLoginDate;
    @Column(name = "IS_ACTIVE")
    private Boolean isActive;
    @Column(name = "NO_OF_FAILED_LOGINS_SINCE_LAST")
    private Integer numberOfFailedLoginsSinceLast;
    @Column(name = "NO_OF_ATTEMPTS_BEFORE_BLOCKADE")
    private Integer numberOfAttempsBeforeBlockade;
    @Column(name = "DELAY_IN_SECONDS")
    private Integer delayInSeconds;
    @Column(name = "BLOCKADE_DEADLINE", columnDefinition = "DATETIME")
    private LocalDateTime blockadeDeadline;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> ownMessages;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "ALLOWED_MESSAGE", joinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "MESSAGE_ID", referencedColumnName = "ID"))
    private List<Message> allowedMessages;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<PasswordFragment> passwordFragments;

    @Column(name = "CURRENT_PASSWORD_FRAGMENT_ID")
    private Integer currentPasswordFragmentId;

    @Column(name = "PASSWORD_LENGTH")
    private Integer passwordLength;

    @PrePersist
    public void prePersist() {
        if (delayInSeconds == null) {
            delayInSeconds = ThreadLocalRandom.current().nextInt(1, 5);
        }
        if (numberOfAttempsBeforeBlockade == null) {
            numberOfAttempsBeforeBlockade = ThreadLocalRandom.current().nextInt(1, 5);
        }
    }

    public List<Integer> getCurrentPasswordMask() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PasswordFragment passwordFragment = passwordFragments
                .stream()
                .filter(passwordFragment1 ->
                        passwordFragment1.getId().equals(currentPasswordFragmentId))
                .findFirst().get();
        return passwordFragment.getPasswordMask();
    }

    public PasswordFragment getCurrentPasswordFragment() {
        return passwordFragments
                .stream()
                .filter(passwordFragment1 ->
                        passwordFragment1.getId().equals(currentPasswordFragmentId))
                .findFirst().get();
    }

}
