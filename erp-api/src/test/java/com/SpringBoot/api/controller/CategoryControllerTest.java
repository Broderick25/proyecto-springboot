package com.SpringBoot.api.controller;

import com.SpringBoot.application.command.category.CreateCategoryCommandHandler;
import com.SpringBoot.application.command.category.DeleteCategoryCommandHandler;
import com.SpringBoot.application.command.category.UpdateCategoryCommandHandler;
import com.SpringBoot.application.query.category.GetCategoryByCodeQueryHandler;
import com.SpringBoot.application.query.category.GetCategoryByIdQueryHandler;
import com.SpringBoot.application.query.category.ListCategoriesQueryHandler;
import com.SpringBoot.application.query.category.view.CatalogView;
import com.SpringBoot.application.query.category.view.ItemView;
import com.SpringBoot.api.exception.GlobalExceptionHandler;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.document.DuplicateCategoryException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @ContextConfiguration(classes = ...) explícito: ErpApiApplication tiene
// @ComponentScan(basePackages = "com.SpringBoot") + @EnableMongoRepositories/@EnableJpaRepositories
// directos, y @WebMvcTest no logra excluirlos (intenta construir repositorios Mongo/JPA reales,
// que necesitan mongoTemplate/datasource) si se lo deja autodetectar ErpApiApplication. Acotando
// el contexto a solo el controller + el @RestControllerAdvice se evita esa cascada.
@WebMvcTest(CategoryController.class)
@ContextConfiguration(classes = CategoryController.class)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateCategoryCommandHandler createCategoryCommandHandler;

    @MockitoBean
    private UpdateCategoryCommandHandler updateCategoryCommandHandler;

    @MockitoBean
    private DeleteCategoryCommandHandler deleteCategoryCommandHandler;

    @MockitoBean
    private ListCategoriesQueryHandler listCategoriesQueryHandler;

    @MockitoBean
    private GetCategoryByCodeQueryHandler getCategoryByCodeQueryHandler;

    @MockitoBean
    private GetCategoryByIdQueryHandler getCategoryByIdQueryHandler;

    @Test
    void create_devuelve201ConElId() throws Exception {
        when(createCategoryCommandHandler.handle(any())).thenReturn("cat-sports");

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"cat-sports","code":"SPORTS","value":"Sports","description":"Sporting goods"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("cat-sports"));
    }

    @Test
    void create_devuelve409_cuandoElIdYaExiste() throws Exception {
        when(createCategoryCommandHandler.handle(any()))
                .thenThrow(new DuplicateCategoryException("cat-sports"));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"cat-sports","code":"SPORTS","value":"Sports","description":null}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void list_devuelveElCatalogo() throws Exception {
        CatalogView view = new CatalogView("catalog-1", "PRODUCT_CATEGORIES", "Product Categories", null, true,
                List.of(new ItemView("cat-electronics", "ELECTRONICS", "Electronics", null, 1)), null, null);
        when(listCategoriesQueryHandler.handle(any())).thenReturn(view);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("catalog-1"))
                .andExpect(jsonPath("$.items[0].code").value("ELECTRONICS"));
    }

    @Test
    void getByCode_devuelve404_cuandoNoExiste() throws Exception {
        when(getCategoryByCodeQueryHandler.handle(any()))
                .thenThrow(CategoryNotFoundException.byCode("NO_EXISTE"));

        mockMvc.perform(get("/categories/code/NO_EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_devuelveElItem() throws Exception {
        when(getCategoryByIdQueryHandler.handle(any()))
                .thenReturn(new ItemView("cat-electronics", "ELECTRONICS", "Electronics", null, 1));

        mockMvc.perform(get("/categories/cat-electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ELECTRONICS"));
    }

    @Test
    void getById_devuelve404_cuandoNoExiste() throws Exception {
        when(getCategoryByIdQueryHandler.handle(any()))
                .thenThrow(CategoryNotFoundException.byId("cat-no-existe"));

        mockMvc.perform(get("/categories/cat-no-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_devuelve200() throws Exception {
        mockMvc.perform(put("/categories/cat-electronics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ELEC","value":"Electronics & Tech","description":null}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void delete_devuelve204() throws Exception {
        mockMvc.perform(delete("/categories/cat-electronics"))
                .andExpect(status().isNoContent());
    }
}
