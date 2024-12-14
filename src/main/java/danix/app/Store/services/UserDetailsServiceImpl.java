package danix.app.Store.services;

import danix.app.Store.models.User;
import danix.app.Store.security.PersonDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> person = userService.getUserByEmail(username);

        if(person.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        return new PersonDetails(person.get());
    }
}
