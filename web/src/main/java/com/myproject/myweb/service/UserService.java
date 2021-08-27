package com.myproject.myweb.service;

import com.myproject.myweb.domain.user.User;
import com.myproject.myweb.dto.user.UserRequestDto;
import com.myproject.myweb.dto.user.UserResponseDto;
import com.myproject.myweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto login(UserRequestDto userRequestDto) throws IllegalArgumentException, IllegalStateException {

        User user = userRepository.findByEmail(userRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("UserNotFoundException"));

        Boolean isUser = user.checkPassword(userRequestDto.getPassword());
        if(!isUser){
            throw new IllegalStateException("UserNotMatchedException");
        }

        return new UserResponseDto(user);
    }

    public void join(){}
}
