package danix.app.Store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;

@Getter
@Setter
@AllArgsConstructor
public class ResponseImageDTO {
    private byte[] imageData;
    private MediaType mediaType;
}
