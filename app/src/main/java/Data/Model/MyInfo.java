package Data.Model;

import java.util.List;

public class MyInfo {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String dob;
    private List<Role> roles;
    private String email;
    private String phoneNumber;
    private String userRank;
    private String gender;
    private boolean student;
    private String avatar;


    // Getters và setters

    public MyInfo() {
    }

    public MyInfo(String id, String username, String firstName, String lastName, String dob, List<Role> roles, String email, String phoneNumber, String userRank, String gender, boolean student, String avatarUrl) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.roles = roles;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userRank = userRank;
        this.gender = gender;
        this.student = student;
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserRank() {
        return userRank;
    }

    public void setUserRank(String userRank) {
        this.userRank = userRank;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isStudent() {
        return student;
    }

    public void setStudent(boolean student) {
        this.student = student;
    }

    public String getAvatarUrl() {
        return avatar;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatar = avatarUrl;
    }
}
