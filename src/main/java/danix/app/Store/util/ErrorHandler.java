package danix.app.Store.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

public class ErrorHandler {

    public static void handleException(BindingResult bindingResult, ExceptionType exceptionType) {
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            StringBuilder message = new StringBuilder();

            for (FieldError fieldError : fieldErrors) {
                message.append(fieldError.getField()).append("-").append(fieldError.getDefaultMessage())
                        .append(";");
            }

            switch (exceptionType) {
                case USER_EXCEPTION ->
                    throw new UserException(message.toString());
                case ITEM_EXCEPTION ->
                    throw new ItemException(message.toString());
                case ORDER_EXCEPTION ->
                        throw new OrderException(message.toString());
                case CART_EXCEPTION ->
                    throw new CartException(message.toString());
            }
        }
    }
}
