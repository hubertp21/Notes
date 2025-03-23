package pl.edu.uj.notes.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UserNotFoundException;
import pl.edu.uj.notes.user.exceptions.UsersNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public String createUser(CreateUserRequest request) {
    UserEntity user =
        new UserEntity(request.getUsername(), passwordEncoder.encode(request.getPassword()));

    if (userRepository.existsByUsername(request.getUsername())) {
      String message = String.format("User '%s' already exists", request.getUsername());
      throw new UserAlreadyExistsException(message);
    }

    return userRepository.save(user).getId();
  }

  public void deleteUser(DeleteUserRequest request) {
    String id = request.getId();

    UserEntity user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " does not exist"));

    userRepository.delete(user);
  }

  public Optional<UserEntity> getUserByUsername(String username) {
    if (StringUtils.isBlank(username)) {
      throw new IllegalArgumentException("Username should not be null or empty");
    }

    return userRepository.getUserEntityByUsername(username);
  }

  public List<String> viewUsers(ViewUsersRequest request) {
    List<UserEntity> users = userRepository.findAllById(request.getIdList());
    if (users.isEmpty()) {
      String message = String.format("Users '%s' not found", request.getIdList());
      throw new UsersNotFoundException(message);
    }
    return getUsernames(users);
  }

  private static List<String> getUsernames(List<UserEntity> users) {
    return users.stream().map(UserEntity::getUsername).collect(Collectors.toList());
  }
}
