package pl.edu.uj.notes.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.edu.uj.notes.authentication.SecurityConfig;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

  static final String USER_URI = "/api/v1/user";

  @MockitoBean UserService userService;
  @Autowired MockMvc mockMvc;

  @Test
  void whenBlankFields_thenBadRequest() throws Exception {
    mockMvc
        .perform(post(USER_URI).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void whenCorrectRequest_thenCreated() throws Exception {
    mockMvc
        .perform(
            post(USER_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"username\",\"password\":\"password\"}"))
        .andExpect(status().isCreated());
  }
}
