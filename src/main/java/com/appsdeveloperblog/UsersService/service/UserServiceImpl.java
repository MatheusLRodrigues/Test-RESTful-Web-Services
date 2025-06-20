package com.appsdeveloperblog.UsersService.service;

import com.appsdeveloperblog.UsersService.exceptions.UserServiceException;
import com.appsdeveloperblog.UsersService.io.UserEntity;
import com.appsdeveloperblog.UsersService.io.UsersRepository;
import com.appsdeveloperblog.UsersService.ui.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class UserServiceImpl implements UserService {

    UsersRepository usersRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.usersRepository = usersRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User createUser(User user) {
        user.setId(UUID.randomUUID().toString());

        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(user, userEntity);

        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        User returnValue;
        try {
            UserEntity storedUserEntity = usersRepository.save(userEntity);
            returnValue = new User();
            BeanUtils.copyProperties(storedUserEntity, returnValue);
        } catch (Exception ex) {
            throw new UserServiceException(ex.getMessage());
        }

        return returnValue;
    }

    @Override
    public User getUser(String userId) {
        User returnValue;

        UserEntity storedUserEntity = usersRepository.findById(userId)
                .orElseThrow(() -> new UserServiceException("User not found"));
        returnValue = new User();
        BeanUtils.copyProperties(storedUserEntity, returnValue);
        return returnValue;
    }

    @Override
    public List<User> getUsers(int page, int limit) {
        if (page > 0)
            page = page - 1;

        Pageable pageableRequest = PageRequest.of(page, limit);

        Page<UserEntity> usersPage = usersRepository.findAll(pageableRequest);
        List<UserEntity> userEntities = usersPage.getContent();

        List<User> users = new ArrayList<>();
        Iterator<UserEntity> iterator = userEntities.iterator();

        while (iterator.hasNext()) {
            UserEntity userEntity = (UserEntity) iterator.next();
            User user = new User();
            BeanUtils.copyProperties(userEntity, user);
            users.add(user);
        }
        return users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = usersRepository.findByEmail(username);
        if (userEntity == null)
            throw new UsernameNotFoundException(username);

        return new org.springframework.security.core.userdetails.User(userEntity.getId(),
                userEntity.getEncryptedPassword(),
                true, true, true, true,
                new ArrayList<>());

    }

}
