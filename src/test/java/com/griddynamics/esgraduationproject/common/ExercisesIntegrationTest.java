package com.griddynamics.esgraduationproject.common;

import com.griddynamics.esgraduationproject.service.TypeaheadService;
import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ExercisesIntegrationTest extends BaseTest {
    private final APIClient client = new APIClient();

    private final TypeaheadService typeaheadService;

    @BeforeAll
    public void init() throws InterruptedException {
        typeaheadService.recreateIndex();
        Thread.sleep(1100); // TASK 6: Why if we change 1100 to 500, then some tests fail? How to fix it, so that all tests pass with 500?
    }

    // TASK 1: Fix 2 bugs in config and recreation/filling of the index.
    @Test
    public void testGetAllWorks() {
        client
//                .logResponse() // Use this method to log the response to debug tests
                .typeaheadRequest()
                .body("{}")
                .post()
                .then()
                .statusCode(200)
                .body("totalHits", greaterThan(0));
    }

    // TASK 2: Fix bulk data file
    @Test
    public void testGetAllReturns36documents() {
        client
//            .logResponse() // Use this method to log the response to debug tests
            .typeaheadRequest()
            .body("{}")
            .post()
            .then()
            .statusCode(200)
            .body("totalHits", is(36))
            .body("typeaheads", hasSize(36));
    }

    // TASK 3: Fix bug in search by text
    @Test
    public void testSearchByTextWorks() {
        client
//            .logResponse() // Use this method to log the response to debug tests
            .typeaheadRequest()
            .body("{\"size\": 3, \"textQuery\": \"sho\"}")
            .post()
            .then()
            .statusCode(200)
            // TotalHits
            .body("totalHits", is(21))
            // Typeaheads
            .body("typeaheads", hasSize(3))
            .body("typeaheads[0].name", is("Sneakers and shoes"))
            .body("typeaheads[0].rank", is(51))
            .body("typeaheads[1].name", is("Women's sneakers & shoes"))
            .body("typeaheads[1].rank", is(50))
            .body("typeaheads[2].name", is("Men's sneakers & shoes"))
            .body("typeaheads[2].rank", is(48))
            // Facets
            .body("facets", notNullValue())
            .body("facets[\"Item Counts\"].empty.count", is(0))
            .body("facets[\"Item Counts\"].small.count", greaterThanOrEqualTo(0))
            .body("facets[\"Item Counts\"].medium.count", greaterThan(0))
            .body("facets[\"Item Counts\"].large.count", greaterThan(0))
        ;
    }

    // TASK 4: Change facet bucket definition so that the small bucket contain count > 0
    @Test
    public void testSearchByTextReturnsMoreThan0InSmallFacetBucket() {
        client
//            .logResponse() // Use this method to log the response to debug tests
            .typeaheadRequest()
            .body("{\"size\": 3, \"textQuery\": \"sho\"}")
            .post()
            .then()
            .statusCode(200)
            // Facets
            .body("facets", notNullValue())
            .body("facets[\"Item Counts\"][\"small\"].count", greaterThan(0))
        ;
    }

    // TASK 5: Add a new parameter "considerItemCountInSorting" to the request that change sorting (when it's true)
    // from: _score DESC, rank DESC, _id DESC
    // to: _score DESC, itemCount DESC, _id DESC
    @Test
    public void testSortingByItemCountWorks() {

        // considerItemCountInSorting = true
        client
//            .logResponse() // Use this method to log the response to debug tests
            .typeaheadRequest()
            .body("{\"size\": 3, \"textQuery\": \"sho\", \"considerItemCountInSorting\": true}")
            .post()
            .then()
            .statusCode(200)
            // Facets
            // TotalHits
            .body("totalHits", is(21))
            // Typeaheads
            .body("typeaheads", hasSize(3))
            .body("typeaheads[0].name", is("Sneakers and shoes"))
            .body("typeaheads[0].rank", is(51))
            .body("typeaheads[0].itemCount", is(448))
            .body("typeaheads[1].name", is("Shorts"))
            .body("typeaheads[1].rank", is(20))
            .body("typeaheads[1].itemCount", is(285))
            .body("typeaheads[2].name", is("Women's sneakers & shoes"))
            .body("typeaheads[2].rank", is(50))
            .body("typeaheads[2].itemCount", is(247))
        ;

        // considerItemCountInSorting = false
        client
//            .logResponse() // Use this method to log the response to debug tests
            .typeaheadRequest()
            .body("{\"size\": 3, \"textQuery\": \"sho\", \"considerItemCountInSorting\": false}")
            .post()
            .then()
            .statusCode(200)
            // TotalHits
            .body("totalHits", is(21))
            // Typeaheads
            .body("typeaheads", hasSize(3))
            .body("typeaheads[0].name", is("Sneakers and shoes"))
            .body("typeaheads[0].rank", is(51))
            .body("typeaheads[0].itemCount", is(448))
            .body("typeaheads[1].name", is("Women's sneakers & shoes"))
            .body("typeaheads[1].rank", is(50))
            .body("typeaheads[1].itemCount", is(247))
            .body("typeaheads[2].name", is("Men's sneakers & shoes"))
            .body("typeaheads[2].rank", is(48))
            .body("typeaheads[2].itemCount", is(201))
        ;

        // considerItemCountInSorting isn't specified (default = false)
        client
//            .logResponse() // Use this method to log the response to debug tests
            .typeaheadRequest()
            .body("{\"size\": 3, \"textQuery\": \"sho\"}")
            .post()
            .then()
            .statusCode(200)
            // TotalHits
            .body("totalHits", is(21))
            // Typeaheads
            .body("typeaheads", hasSize(3))
            .body("typeaheads[0].name", is("Sneakers and shoes"))
            .body("typeaheads[0].rank", is(51))
            .body("typeaheads[0].itemCount", is(448))
            .body("typeaheads[1].name", is("Women's sneakers & shoes"))
            .body("typeaheads[1].rank", is(50))
            .body("typeaheads[1].itemCount", is(247))
            .body("typeaheads[2].name", is("Men's sneakers & shoes"))
            .body("typeaheads[2].rank", is(48))
            .body("typeaheads[2].itemCount", is(201))
        ;
    }
}
