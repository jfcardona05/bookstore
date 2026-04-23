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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    void roleRestrictionsAndQueryEndpointsWork() throws Exception {
        String adminToken = loginAndExtractToken("admin@bookstore.com", "Admin123*");

        Long authorId = createResource("""
                {
                  "name": "Julio Verne",
                  "biography": "Autor de aventuras"
                }
                """, "/authors", adminToken);

        Long categoryId = createResource("""
                {
                  "name": "Adventure",
                  "description": "Adventure books"
                }
                """, "/categories", adminToken);

        Long bookId = createResource("""
                {
                  "title": "Viaje al centro de la tierra",
                  "isbn": "ISBN-VERNE-001",
                  "description": "Classic adventure",
                  "price": 39.90,
                  "stock": 8,
                  "authorId": %d,
                  "categoryId": %d
                }
                """.formatted(authorId, categoryId), "/books", adminToken);

        mockMvc.perform(get("/books")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/books")
                        .param("title", "centro")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Viaje al centro de la tierra"));

        mockMvc.perform(get("/authors/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        String userEmail = "reader2@test.com";
        registerUser("Reader Two", userEmail, "Reader1234");
        String userToken = loginAndExtractToken(userEmail, "Reader1234");

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Forbidden book",
                                  "isbn": "ISBN-FORBIDDEN-001",
                                  "description": "Should fail",
                                  "price": 10.0,
                                  "stock": 1,
                                  "authorId": %d,
                                  "categoryId": %d
                                }
                                """.formatted(authorId, categoryId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/books/" + bookId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(bookId));
    }

    @Test
    void duplicateAndValidationErrorsAreHandled() throws Exception {
        String adminToken = loginAndExtractToken("admin@bookstore.com", "Admin123*");

        registerUser("Duplicate User", "duplicate@test.com", "Password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Duplicate User",
                                  "email": "duplicate@test.com",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El correo ya est\u00e1 registrado"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "",
                                  "email": "badmail",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        Long authorId = createResource("""
                {
                  "name": "Isaac Asimov",
                  "biography": "Sci-fi"
                }
                """, "/authors", adminToken);

        Long categoryId = createResource("""
                {
                  "name": "Sci-Fi",
                  "description": "Science fiction"
                }
                """, "/categories", adminToken);

        createResource("""
                {
                  "title": "Foundation",
                  "isbn": "ISBN-FOUNDATION-001",
                  "description": "Classic sci-fi",
                  "price": 50.00,
                  "stock": 4,
                  "authorId": %d,
                  "categoryId": %d
                }
                """.formatted(authorId, categoryId), "/books", adminToken);

        mockMvc.perform(post("/books")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Foundation 2",
                                  "isbn": "ISBN-FOUNDATION-001",
                                  "description": "Duplicated isbn",
                                  "price": 60.00,
                                  "stock": 5,
                                  "authorId": %d,
                                  "categoryId": %d
                                }
                                """.formatted(authorId, categoryId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ya existe un libro con ese ISBN"));
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

    private void registerUser(String fullName, String email, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "%s",
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(fullName, email, password)))
                .andExpect(status().isCreated());
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
