package com.griddynamics.pss.common;
import com.griddynamics.pss.services.ProductService;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest extends  BaseTest{
    private static final String CALVIN_KLEIN = "Calvin Klein";
    private static final String LEVIS = "Levi's";
    private static final String CHEAP = "Cheap";
    private static final String AVERAGE = "Average";
    private static final String EXPENSIVE = "Expensive";
    private final APIClient client = new APIClient();
    private final ProductService productService;

    @BeforeAll
    public void init() throws InterruptedException {
        productService.recreateIndex();
        Thread.sleep(3000); // TASK 6: Why if we change 1100 to 500, then some tests fail? How to fix it, so that all tests pass with 500?
    }

    // Empty request test
    @Test
    public void emptyRequestTest() {
        client
                .productRequest()
                .body("{}")
                .post()
                .then()
                .statusCode(400);
                //.body("totalHits", equalTo(0));
    }

    @Test
    public void wrongWordTest() {
        client
                .productRequest()
                .body("{\"queryText\": \"Calvin klein L blue ankle skinny jeans wrongword\"}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", equalTo(0));
    }

    @Test
    public void notExistingDocTest() {
        client
                .productRequest()
                .body("{\"queryText\": \"Calvin klein L red ankle skinny jeans\"}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", equalTo(0));
    }

    // Happy path test
    @Test
    public void happyPathTest() {
        client
                .logResponse() // Use this method to log the response to debug tests
                .productRequest()
                .body("{\"queryText\": \"Calvin klein L blue ankle skinny jeans\"}")
                .post()
                .then()
                .statusCode(200)
                // TotalHits
                .body("totalHits", is(1))
                // Typeaheads
                .body("products", hasSize(1))
                .body("products[0].id", is(2))
                .body("products[0].brand", is(CALVIN_KLEIN))
                .body("products[0].name", is("Women ankle skinny jeans, model 1282"))
                .body("products[0].skus", hasSize(9));
    }

    @Test
    public void facetJeansTest() {
        client
                .logResponse() // Use this method to log the response to debug tests
                .productRequest()
                .body("{\"queryText\": \"jeans\"}")
                .post()
                .then()
                .statusCode(200)
                // facets count
                .body("facets", aMapWithSize(4))
                // Facets brand
                .body("facets.brand", hasSize(2))
                .body("facets.brand[0].value", is(CALVIN_KLEIN))
                .body("facets.brand[0].count", is(4))
                .body("facets.brand[1].value", is(LEVIS))
                .body("facets.brand[1].count", is(4))
                // Facets price
                .body("facets.price", hasSize(3))
                .body("facets.price[0].value", is(CHEAP))
                .body("facets.price[0].count", is(2))
                .body("facets.price[1].value", is(AVERAGE))
                .body("facets.price[1].count", is(6))
                .body("facets.price[2].value", is(EXPENSIVE))
                .body("facets.price[2].count", is(0))
                // Facets color
                .body("facets.color", hasSize(4))
                .body("facets.color[0].value", is("Blue"))
                .body("facets.color[0].count", is(8))
                .body("facets.color[1].value", is("Black"))
                .body("facets.color[1].count", is(7))
                .body("facets.color[2].value", is("Red"))
                .body("facets.color[2].count", is(1))
                .body("facets.color[3].value", is("White"))
                .body("facets.color[3].count", is(1))
                // Facets Sie
                .body("facets.size", hasSize(6))
                .body("facets.size[0].value", is("L"))
                .body("facets.size[0].count", is(8))
                .body("facets.size[1].value", is("M"))
                .body("facets.size[1].count", is(8))
                .body("facets.size[2].value", is("S"))
                .body("facets.size[2].count", is(6))
                .body("facets.size[3].value", is("XL"))
                .body("facets.size[3].count", is(5))
                .body("facets.size[4].value", is("XXL"))
                .body("facets.size[4].count", is(3))
                .body("facets.size[5].value", is("XS"))
                .body("facets.size[5].count", is(2));
    }

    @Test
    public void facetWomenAnkleBlueJeansTest() {
        client
                .logResponse() // Use this method to log the response to debug tests
                .productRequest()
                .body("{\"queryText\": \"women ankle blue jeans\"}")
                .post()
                .then()
                .statusCode(200)
                // facets count
                .body("facets", aMapWithSize(4))
                // Facets brand
                .body("facets.brand", hasSize(2))
                .body("facets.brand[0].value", is(CALVIN_KLEIN))
                .body("facets.brand[0].count", is(2))
                .body("facets.brand[1].value", is(LEVIS))
                .body("facets.brand[1].count", is(1))
                // Facets price
                .body("facets.price", hasSize(3))
                .body("facets.price[0].value", is(CHEAP))
                .body("facets.price[0].count", is(0))
                .body("facets.price[1].value", is(AVERAGE))
                .body("facets.price[1].count", is(3))
                .body("facets.price[2].value", is(EXPENSIVE))
                .body("facets.price[2].count", is(0))
                // Facets color
                .body("facets.color", hasSize(4))
                .body("facets.color[0].value", is("Black"))
                .body("facets.color[0].count", is(3))
                .body("facets.color[1].value", is("Blue"))
                .body("facets.color[1].count", is(3))
                .body("facets.color[2].value", is("Red"))
                .body("facets.color[2].count", is(1))
                .body("facets.color[3].value", is("White"))
                .body("facets.color[3].count", is(1))
                // Facets Sie
                .body("facets.size", hasSize(4))
                .body("facets.size[0].value", is("L"))
                .body("facets.size[0].count", is(3))
                .body("facets.size[1].value", is("M"))
                .body("facets.size[1].count", is(3))
                .body("facets.size[2].value", is("S"))
                .body("facets.size[2].count", is(3))
                .body("facets.size[3].value", is("XS"))
                .body("facets.size[3].count", is(1));
    }

    // Pagination Test
    @Test
    public void paginationTest() {
        client
                .logResponse()
                .productRequest()
                .body("{" +
                            "\"queryText\": \"jeans\",\n" +
                            "\"size\": \"2\",\n" +
                            "\"page\": \"1\"\n" +
                        "}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", is(8))
                .body("products", hasSize(2))
                .body("products[0].id", is(6))
                .body("products[1].id", is(5));
    }

    @Test
    public void boostJeansTest() {
        client
                .logResponse()
                .productRequest()
                .body("{" +
                        "\"queryText\": \"jeans\"\n" +
                        "}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", is(8))
                .body("products[0].id", is(8))
                .body("products[1].id", is(7))
                .body("products[2].id", is(6))
                .body("products[3].id", is(5))
                .body("products[4].id", is(4))
                .body("products[5].id", is(3))
                .body("products[6].id", is(2))
                .body("products[7].id", is(1));
    }

    @Test
    public void boostWomenAnkleBlueJeans() {
        client
                .productRequest()
                .body("{" +
                        "\"queryText\": \"women ankle blue jeans\"\n" +
                        "}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", is(3))
                .body("products[0].id", is(6))
                .body("products[1].id", is(2))
                .body("products[2].id", is(1));
    }

    @Test
    public void boostBlueWomenJeansTest() {
        client
                .productRequest()
                .body("{" +
                        "\"queryText\": \"blue WOMEN jeans\"\n" +
                        "}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", is(5))
                .body("products[0].id", is(5))
                .body("products[1].id", is(3))
                .body("products[2].id", is(6))
                .body("products[3].id", is(2))
                .body("products[4].id", is(1));
    }

    @Test
    public void boostWomenBlueJeansTest() {
        client
                .logResponse()
                .productRequest()
                .body("{" +
                        "\"queryText\": \"WOMEN blue jeans\"\n" +
                        "}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", is(5))
                .body("products[0].id", is(6))
                .body("products[1].id", is(5))
                .body("products[2].id", is(3))
                .body("products[3].id", is(2))
                .body("products[4].id", is(1));
    }
}
