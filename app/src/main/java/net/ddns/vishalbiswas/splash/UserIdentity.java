package net.ddns.vishalbiswas.splash;

import android.graphics.Bitmap;

class UserIdentity {
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

    UserIdentity() {
    }

    String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    String getFirstname() {
        return firstname;
    }

    void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    String getLastname() {
        return lastname;
    }

    void setLastname(String lastname) {
        this.lastname = lastname;
    }

    String getEmail() {
        return email;
    }

    void setEmail(String email) {
        this.email = email;
    }

    Bitmap getProfpic() {
        return profpic;
    }

    void setProfpic(Bitmap profpic) {
        this.profpic = profpic;
    }

    int getUid() {
        return uid;
    }

    void setUid(int uid) {
        this.uid = uid;
    }
}
