package com.example.bookstore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookstoreApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndLoginFlowWorks() throws Exception {
        String registerPayload = """
                {
                  "fullName": "Jane Reader",
                  "email": "jane.reader@test.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("jane.reader@test.com"));

        String loginPayload = """
                {
                  "email": "jane.reader@test.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void adminCanCreateCatalogAndUserCanCreateOrder() throws Exception {
        String adminToken = loginAndExtractToken("admin@bookstore.com", "Admin123*");

        Long authorId = createResource("""
                {
                  "name": "George Orwell",
                  "biography": "Autor de ficci\u00f3n pol\u00edtica"
                }
                """, "/authors", adminToken);

        Long categoryId = createResource("""
                {
                  "name": "Dystopian",
                  "description": "Dystopian novels"
                }
                """, "/categories", adminToken);

        Long bookId = createResource("""
                {
                  "title": "1984",
                  "isbn": "ISBN-1984-001",
                  "description": "Classic novel",
                  "price": 45.50,
                  "stock": 10,
                  "authorId": %d,
                  "categoryId": %d
                }
                """.formatted(authorId, categoryId), "/books", adminToken);

        String userEmail = "buyer@test.com";
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Buyer User",
                                  "email": "%s",
                                  "password": "Buyer1234"
                                }
                                """.formatted(userEmail)))
                .andExpect(status().isCreated());

        String userToken = loginAndExtractToken(userEmail, "Buyer1234");

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    {
                                      "bookId": %d,
                                      "quantity": 2
                                    }
                                  ]
                                }
                                """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.total").value(91.0))
                .andExpect(jsonPath("$.data.items[0].bookId").value(bookId));

        mockMvc.perform(get("/orders/my-orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerEmail").value(userEmail));
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("token").asText();
    }

    private Long createResource(String payload, String path, String token) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.path("data").path("id").asLong();
    }
}
