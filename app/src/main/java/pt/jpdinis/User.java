package pt.jpdinis;


import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("firstName")
    String FirstName;
    @SerializedName("lastName")
    String LastName;
    @SerializedName("username")
    String Username;
    @SerializedName("password")
    String Password;
    @SerializedName("email")
    String Email;
    @SerializedName("profilepic")
    String ProfilePic;
    @SerializedName("admin")
    boolean Admin;

    public User(String firstName, String lastName, String username, String password, String email, String profilepic, Boolean admin) {
        this.Username = username;
        this.FirstName = firstName;
        this.LastName = lastName;
        this.Email = email;
        this.ProfilePic = profilepic;
        this.Password = password;
        this.Admin = admin;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
    }

    public String getProfilePic() {
        return ProfilePic;
    }

    public void setProfilePic(String profilePic) {
        this.ProfilePic = profilePic;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        this.Password = password;
    }

    public boolean isAdmin() {
        return Admin;
    }

    public void setAdmin(boolean admin) {
        this.Admin = admin;
    }
}
