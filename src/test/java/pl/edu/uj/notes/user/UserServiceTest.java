package pl.edu.uj.notes.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.edu.uj.notes.user.exceptions.UserAlreadyExistsException;

public class UserServiceTest {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";

  private AutoCloseable closeable;

  @Mock private UserRepository userRepository;
  @InjectMocks private UserService userService;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void close() throws Exception {
    closeable.close();
  }

  @Test
  void whenCreateUser_thenReturnsUserId() {
    // Given
    CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);
    UserEntity mockUser = new UserEntity(USERNAME, PASSWORD);

    when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
    when(userRepository.save(any(UserEntity.class))).thenReturn(mockUser);

    // When
    int id = userService.createUser(createUserRequest);

    // Then
    assertEquals(0, id);
    verify(userRepository, times(1)).save(any(UserEntity.class));
  }

  @Test
  void whenUsernameAlreadyExists_thenThrowException() {
    // Given
    CreateUserRequest createUserRequest = new CreateUserRequest(USERNAME, PASSWORD);

    when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

    // When & Then
    assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
    verify(userRepository, never()).save(any(UserEntity.class));
  }
}
