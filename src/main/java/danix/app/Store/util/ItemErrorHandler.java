package danix.app.Store.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class ItemErrorHandler {
    public static void handleException(BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            StringBuilder message = new StringBuilder();

            for (FieldError fieldError : errors) {
                message.append(fieldError.getField()).append("-").append(fieldError.getDefaultMessage())
                        .append(";");
            }

            throw new ItemException(message.toString());
        }
    }
}
