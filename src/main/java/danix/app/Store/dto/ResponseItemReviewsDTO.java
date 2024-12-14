package danix.app.Store.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ResponseItemReviewsDTO {

    private String comment;

    private Integer likes;

    private Date createdAt;

    private String ownerUsername;

    private String itemName;

    private boolean isLiked;

    private Integer grade;
}
