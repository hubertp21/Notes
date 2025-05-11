package pl.edu.uj.notes.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.edu.uj.notes.authentication.SecurityConfig;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;
import pl.edu.uj.notes.user.exceptions.UserNotFoundException;
import pl.edu.uj.notes.user.exceptions.UsersNotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(SecurityConfig.class)
public class UserServiceTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String ENCODED_PASSWORD = "encoded";

  @Autowired private UserRepository userRepository;
  @Autowired private UserService userService;
  @MockitoBean private PasswordEncoder passwordEncoder;

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Nested
  class CreateUser {

    @Test
    void whenCreateUser_thenReturnsUserId() {
      // Given
      CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);

      // When
      String id = userService.createUser(createUserRequest);

      // Then
      assertTrue(userRepository.existsById(id));
      assertTrue(userRepository.existsByUsername(createUserRequest.getUsername()));
    }

    @Test
    void whenUsernameAlreadyExists_thenThrowException() {
      // Given
      CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);

      // When & Then
      assertDoesNotThrow(() -> userService.createUser(createUserRequest));
      assertThrows(
          UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
    }

    @Test
    void createsUserWithEncodedPassword() {
      var request = new CreateUserRequest(USERNAME, PASSWORD);

      when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

      userService.createUser(request);
      UserEntity user = userRepository.getUserEntityByUsername(USERNAME).orElseThrow();

      assertEquals(ENCODED_PASSWORD, user.getPassword());
    }
  }

  @Test
  void whenUserExists_thenDeleteUserSuccessfully() {
    // Given
    UserEntity user = new UserEntity(USERNAME, PASSWORD);
    userRepository.save(user);
    String userId = user.getId();

    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(userId);

    // When
    userService.deleteUser(deleteUserRequest);

    // Then
    assertFalse(userRepository.existsById(userId));
  }

  @Test
  void whenUserDoesNotExist_thenThrowUserNotFoundException() {
    // Given
    String userId = "1e931558-2ef8-42ae-8642-3e72778de9c5";
    DeleteUserRequest deleteUserRequest = new DeleteUserRequest(userId);

    // When & Then
    UserNotFoundException exception =
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(deleteUserRequest));

    assertEquals("User with ID " + userId + " does not exist", exception.getMessage());
    assertFalse(userRepository.existsById(userId));
  }

  @Test
  void whenUserPresentInDatabase_thenReturnUsername() {
    // Given
    UserEntity user = new UserEntity(USERNAME, PASSWORD);
    userRepository.save(user);
    String userId = user.getId();
    ViewUsersRequest viewUsersRequest = ViewUsersRequest.builder().idList(List.of(userId)).build();

    // When
    List<String> usernames = userService.viewUsers(viewUsersRequest);

    // Then
    assertEquals(1, usernames.size());
    assertEquals(USERNAME, usernames.getFirst());
  }

  @Test
  void whenUserIsNotPresentInDatabase_thenThrowException() {
    // Given
    String fakeId = "fakeId";
    ViewUsersRequest viewUsersRequest = ViewUsersRequest.builder().idList(List.of(fakeId)).build();

    // When & Then
    assertThrows(UsersNotFoundException.class, () -> userService.viewUsers(viewUsersRequest));
  }
}
