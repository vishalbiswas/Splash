package vishal.chetan.splash;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

public class UserIdentity {
    private long uid = -1;
    @Nullable
    private String username = null;
    private String firstname = "";
    private String lastname = "";
    private String email = "";
    @Nullable
    private long profpic = -1;

    UserIdentity(long uid, @Nullable String username, String firstname, String lastname, String email, long profpic) {
        this.uid = uid;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.profpic = profpic;
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

    @Nullable
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

}
