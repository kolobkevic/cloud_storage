package ru.kolobkevic.cloud_storage.controllers;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@SpringBootTest()
@AutoConfigureMockMvc
@Transactional
class RegistrationControllerTest {
    @Autowired
    private MockMvc mockMvc;


    @Test
    void showRegistrationPage() throws Exception {
        this.mockMvc.perform(get("/auth/registration"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registration page")));
    }

    @Test
    void register() throws Exception {
        this.mockMvc.perform(post("/auth/registration")
                        .param("firstName", "First name")
                        .param("lastName", "Last name")
                        .param("email", "email@mail.ru")
                        .param("password", "password"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void showLoginPage() throws Exception {
        this.mockMvc.perform(get("/auth/login"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Login page")));
    }

    @Test
    void correctLogin() throws Exception {
        this.mockMvc.perform(formLogin()
                        .loginProcessingUrl("/perform-login")
                .user("kolobkevic@mail.ru")
                .password("123456"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}