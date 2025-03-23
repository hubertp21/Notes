package pl.edu.uj.notes.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public int createUser(CreateUserRequest request) {
    UserEntity user = new UserEntity(request.getUsername(), request.getPassword());

    if (userRepository.existsByUsername(request.getUsername())) {
      String message = String.format("User '%s' already exists", request.getUsername());
      throw new UserAlreadyExistsException(message);
    }

    userRepository.save(user);
    return user.getId();
  }
}
