package com.myproject.myweb.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class CommonExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class}) // request redirect session
    public String argOrStatusException(HttpServletRequest request,
                                       IllegalArgumentException exception,
                                       Model model){

        String msg = messageSource.getMessage(exception.getMessage(), null, Locale.getDefault());
        model.addAttribute("msg", msg); // alert로 전달
        log.error("error msg = " + msg + " 이전 url = " + request.getHeader("REFERER"));

        return "redirect:/";
    }

    /* 각 controller에 또는 @ControllerAdvice(@RestControllerAdvice)
    @ExceptionHandler(IllegalStateException.class)
    public @ResponseBody or ResponseEntity statusException(){}
     */

}
