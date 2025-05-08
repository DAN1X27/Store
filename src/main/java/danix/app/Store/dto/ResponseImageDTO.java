package danix.app.Store.dto;

import lombok.*;
import org.springframework.http.MediaType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseImageDTO {
    private byte[] imageData;
    private MediaType mediaType;
}
