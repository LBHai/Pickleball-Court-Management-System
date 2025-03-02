package Model;

import java.util.List;

public class Role {
    private String name;
    private String description;
    // Sửa từ List<String> sang List<Permission>
    private List<Permission> permissions;

    // Getters và setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<Permission> getPermissions() {
        return permissions;
    }
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}
