package danix.app.Store.dto;

import lombok.*;

import java.util.Date;

@Data
public class ResponseItemReviewDTO {

    private String comment;

    private Integer likes;

    private Date createdAt;

    private String ownerUsername;

    private String itemName;

    private boolean isLiked;

    private Integer grade;
}
