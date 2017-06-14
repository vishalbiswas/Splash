package vishal.chetan.splash;

import android.support.annotation.Nullable;

public class UserIdentity {
    private long uid = -1;
    @Nullable
    private String username = null;
    private String firstname = "";
    private String lastname = "";
    private String email = "";
    private long profpic = -1;
    private boolean revoked = false;
    private boolean banned = false;
    private String sessionid = null;

    public static final int MODERATOR_NONE = 0;
    public static final int MODERATOR_NORMAL = 1;
    public static final int MODERATOR_ABSOLUTE = 2;

    private int mod = MODERATOR_NONE;

    UserIdentity(long uid, @Nullable String username, String firstname, String lastname, String email, long profpic) {
        this.uid = uid;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.profpic = profpic;
    }

    UserIdentity(long uid, @Nullable String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    UserIdentity(@Nullable String username, String firstname, String lastname, String email) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public UserIdentity() {
    }

    public UserIdentity(long uid, @Nullable String username, String firstname, String lastname, String email) {
        this.uid = uid;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }


    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getProfpic() {
        return profpic;
    }

    public void setProfpic(long profpic) {
        this.profpic = profpic;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getMod() {
        return mod;
    }

    public void setMod(int mod) {
        if (mod > MODERATOR_NONE && mod <= MODERATOR_ABSOLUTE) {
            this.mod = mod;
        } else {
            this.mod = MODERATOR_NONE;
        }
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
}
