package org.example.iam_service.service;

import lombok.RequiredArgsConstructor;
import org.example.iam_service.model.User;
import org.example.iam_service.model.UserPrincipal;
import org.example.iam_service.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("user not found: "+username));
        return new UserPrincipal(user);
    }

}
