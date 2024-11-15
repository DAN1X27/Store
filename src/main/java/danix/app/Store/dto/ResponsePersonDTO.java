package danix.app.Store.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ResponsePersonDTO {
    private String username;

    private String email;

    private List<AdminOrderDTO> orders;

    private LocalDateTime createdAt;
    private String role;

    private boolean banned;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<AdminOrderDTO> getOrders() {
        return orders;
    }

    public void setOrders(List<AdminOrderDTO> orders) {
        this.orders = orders;
    }

}
