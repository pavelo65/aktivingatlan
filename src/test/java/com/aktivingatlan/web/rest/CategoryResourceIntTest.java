package com.aktivingatlan.web.rest;

import com.aktivingatlan.Application;
import com.aktivingatlan.domain.Category;
import com.aktivingatlan.repository.CategoryRepository;
import com.aktivingatlan.web.rest.dto.CategoryDTO;
import com.aktivingatlan.web.rest.mapper.CategoryMapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Test class for the CategoryResource REST controller.
 *
 * @see CategoryResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class CategoryResourceIntTest {

    private static final String DEFAULT_NAME_HU = "AAAAA";
    private static final String UPDATED_NAME_HU = "BBBBB";
    private static final String DEFAULT_NAME_EN = "AAAAA";
    private static final String UPDATED_NAME_EN = "BBBBB";
    private static final String DEFAULT_NAME_DE = "AAAAA";
    private static final String UPDATED_NAME_DE = "BBBBB";

    @Inject
    private CategoryRepository categoryRepository;

    @Inject
    private CategoryMapper categoryMapper;

    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    private MockMvc restCategoryMockMvc;

    private Category category;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        CategoryResource categoryResource = new CategoryResource();
        ReflectionTestUtils.setField(categoryResource, "categoryRepository", categoryRepository);
        ReflectionTestUtils.setField(categoryResource, "categoryMapper", categoryMapper);
        this.restCategoryMockMvc = MockMvcBuilders.standaloneSetup(categoryResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Before
    public void initTest() {
        category = new Category();
        category.setNameHu(DEFAULT_NAME_HU);
        category.setNameEn(DEFAULT_NAME_EN);
        category.setNameDe(DEFAULT_NAME_DE);
    }

    @Test
    @Transactional
    public void createCategory() throws Exception {
        int databaseSizeBeforeCreate = categoryRepository.findAll().size();

        // Create the Category
        CategoryDTO categoryDTO = categoryMapper.categoryToCategoryDTO(category);

        restCategoryMockMvc.perform(post("/api/categorys")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
                .andExpect(status().isCreated());

        // Validate the Category in the database
        List<Category> categorys = categoryRepository.findAll();
        assertThat(categorys).hasSize(databaseSizeBeforeCreate + 1);
        Category testCategory = categorys.get(categorys.size() - 1);
        assertThat(testCategory.getNameHu()).isEqualTo(DEFAULT_NAME_HU);
        assertThat(testCategory.getNameEn()).isEqualTo(DEFAULT_NAME_EN);
        assertThat(testCategory.getNameDe()).isEqualTo(DEFAULT_NAME_DE);
    }

    @Test
    @Transactional
    public void getAllCategorys() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get all the categorys
        restCategoryMockMvc.perform(get("/api/categorys?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(category.getId().intValue())))
                .andExpect(jsonPath("$.[*].nameHu").value(hasItem(DEFAULT_NAME_HU.toString())))
                .andExpect(jsonPath("$.[*].nameEn").value(hasItem(DEFAULT_NAME_EN.toString())))
                .andExpect(jsonPath("$.[*].nameDe").value(hasItem(DEFAULT_NAME_DE.toString())));
    }

    @Test
    @Transactional
    public void getCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

        // Get the category
        restCategoryMockMvc.perform(get("/api/categorys/{id}", category.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(category.getId().intValue()))
            .andExpect(jsonPath("$.nameHu").value(DEFAULT_NAME_HU.toString()))
            .andExpect(jsonPath("$.nameEn").value(DEFAULT_NAME_EN.toString()))
            .andExpect(jsonPath("$.nameDe").value(DEFAULT_NAME_DE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingCategory() throws Exception {
        // Get the category
        restCategoryMockMvc.perform(get("/api/categorys/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

		int databaseSizeBeforeUpdate = categoryRepository.findAll().size();

        // Update the category
        category.setNameHu(UPDATED_NAME_HU);
        category.setNameEn(UPDATED_NAME_EN);
        category.setNameDe(UPDATED_NAME_DE);
        CategoryDTO categoryDTO = categoryMapper.categoryToCategoryDTO(category);

        restCategoryMockMvc.perform(put("/api/categorys")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(categoryDTO)))
                .andExpect(status().isOk());

        // Validate the Category in the database
        List<Category> categorys = categoryRepository.findAll();
        assertThat(categorys).hasSize(databaseSizeBeforeUpdate);
        Category testCategory = categorys.get(categorys.size() - 1);
        assertThat(testCategory.getNameHu()).isEqualTo(UPDATED_NAME_HU);
        assertThat(testCategory.getNameEn()).isEqualTo(UPDATED_NAME_EN);
        assertThat(testCategory.getNameDe()).isEqualTo(UPDATED_NAME_DE);
    }

    @Test
    @Transactional
    public void deleteCategory() throws Exception {
        // Initialize the database
        categoryRepository.saveAndFlush(category);

		int databaseSizeBeforeDelete = categoryRepository.findAll().size();

        // Get the category
        restCategoryMockMvc.perform(delete("/api/categorys/{id}", category.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Category> categorys = categoryRepository.findAll();
        assertThat(categorys).hasSize(databaseSizeBeforeDelete - 1);
    }
}
