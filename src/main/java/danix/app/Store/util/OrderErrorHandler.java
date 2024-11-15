package danix.app.Store.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class OrderErrorHandler {
    public static void exceptionHandle(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<FieldError> errors = bindingResult.getFieldErrors();
            StringBuilder message = new StringBuilder();

            errors.forEach(e -> message.append(e.getField()).append("-").append(e.getDefaultMessage()).append(";"));

            throw new OrderException(message.toString());
        }
    }
}
