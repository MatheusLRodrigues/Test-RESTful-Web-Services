package com.appsdeveloperblog.UsersService.ui;

import com.appsdeveloperblog.UsersService.service.UserService;

import com.appsdeveloperblog.UsersService.ui.model.ErrorResponse;
import com.appsdeveloperblog.UsersService.ui.model.User;
import com.appsdeveloperblog.UsersService.ui.model.UserRest;
import jakarta.validation.Valid;

import com.appsdeveloperblog.UsersService.exceptions.UserServiceException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/users")
@RestController
public class UsersController {

    UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody User user) {

        User createdUser;

        try {
            createdUser = userService.createUser(user);
        } catch(UserServiceException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ex.getMessage(), 500));
        }
        UserRest returnValue = new UserRest();
        BeanUtils.copyProperties(createdUser, returnValue);

        return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
    }


    @GetMapping("/{userId}")
    public UserRest getUser(@PathVariable String userId) {
        UserRest returnValue = new UserRest();
        BeanUtils.copyProperties(userService.getUser(userId), returnValue);
        return returnValue;
    }

    @GetMapping
    public List<UserRest> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                   @RequestParam(value = "limit", defaultValue = "2") int limit) {
        List<UserRest> returnValue  = new ArrayList<>();
        List<User> users =  userService.getUsers(page, limit);
        for (User user : users) {
            UserRest userRest = new UserRest();
            BeanUtils.copyProperties(user, userRest);
            returnValue.add(userRest);
        }
        return returnValue;
    }
}
