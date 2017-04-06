package vishal.chetan.splash;

import android.graphics.Bitmap;

public class UserIdentity {
    private int uid = -1;
    private String username = null;
    private String firstname = "";
    private String lastname = "";
    private String email = "";
    private Bitmap profpic = null;

    UserIdentity(String username, String firstname, String lastname, String email) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
    }

    public UserIdentity() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
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

    public Bitmap getProfpic() {
        return profpic;
    }

    public void setProfpic(Bitmap profpic) {
        this.profpic = profpic;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
