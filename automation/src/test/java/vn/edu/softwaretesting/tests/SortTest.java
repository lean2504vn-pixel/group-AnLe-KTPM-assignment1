package vn.edu.softwaretesting.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import vn.edu.softwaretesting.core.WebTestBase;
import vn.edu.softwaretesting.pages.InventoryPage;
import vn.edu.softwaretesting.pages.LoginPage;
import vn.edu.softwaretesting.pages.LoginPage.LoginResult;

/**
 * FR-04: TC-SORT-01..04 chạy với standard_user, sau đó chạy lại cùng các bước
 * với problem_user (cột "TC-SORT-01..04 (problem)" trong test-cases.xlsx).
 * Kết quả mong đợi luôn tính từ đặc tả: sắp xếp bản sao của danh sách thực tế.
 */
@Tag("web")
@DisplayName("FR-04: Product sorting")
class SortTest extends WebTestBase {

    private static final int PRODUCT_COUNT = 6;

    // ---------- standard_user ----------

    @ParameterizedTest(name = "{0}: sort by name, option={1}")
    @CsvSource({
            "TC-SORT-01, az, true",
            "TC-SORT-02, za, false"
    })
    void sortByName(String id, String option, boolean ascending) {
        assertNamesSorted(id, loginAs("standard_user"), option, ascending);
    }

    @ParameterizedTest(name = "{0}: sort by price, option={1}")
    @CsvSource({
            "TC-SORT-03, lohi, true",
            "TC-SORT-04, hilo, false"
    })
    void sortByPrice(String id, String option, boolean ascending) {
        assertPricesSorted(id, loginAs("standard_user"), option, ascending);
    }

    // ---------- problem_user: cùng các bước như standard_user ----------

    // Lỗi đã biết BUG-04: problem_user chọn kiểu sắp xếp nhưng danh sách không đổi.
    @ParameterizedTest(name = "{0} (problem_user) [BUG-04]: sort by {1}, option={2}")
    @Tag("known-bug")
    @CsvSource({
            "TC-SORT-01, name,  az,   true",
            "TC-SORT-02, name,  za,   false",
            "TC-SORT-03, price, lohi, true",
            "TC-SORT-04, price, hilo, false"
    })
    void problemUserSortIsNotApplied(String id, String field, String option, boolean ascending) {
        InventoryPage inventoryPage = loginAs("problem_user");
        String label = id + " (problem_user) [BUG-04]";
        if (field.equals("name")) {
            assertNamesSorted(label, inventoryPage, option, ascending);
        } else {
            assertPricesSorted(label, inventoryPage, option, ascending);
        }
    }

    // ---------- helpers ----------

    private InventoryPage loginAs(String username) {
        LoginResult result = new LoginPage(driver, baseUrl)
                .open()
                .login(username, "secret_sauce");
        assertTrue(result.accepted(),
                () -> "Precondition failed for " + username + ": " + result.message());
        return new InventoryPage(driver);
    }

    private static void assertNamesSorted(String id, InventoryPage page,
            String option, boolean ascending) {
        if (option.equals("az")) {
            // Trang mặc định đã là A->Z: chọn Z->A trước để danh sách rời khỏi
            // thứ tự A->Z, nếu không thì bước chọn A->Z không chứng minh được gì.
            List<String> initial = page.getProductNames();
            List<String> afterZa = page.sortBy("za").getProductNames();
            assertNotEquals(initial, afterZa,
                    () -> id + ": precondition step Z->A did not change the list");
        }
        List<String> actual = page.sortBy(option).getProductNames();
        List<String> expected = new ArrayList<>(actual);
        expected.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

        assertEquals(PRODUCT_COUNT, actual.size(),
                () -> id + ": unexpected number of products");
        assertEquals(expected, actual,
                () -> id + ": names are not sorted for option " + option);
    }

    private static void assertPricesSorted(String id, InventoryPage page,
            String option, boolean ascending) {
        List<BigDecimal> actual = page.sortBy(option).getProductPrices();
        List<BigDecimal> expected = new ArrayList<>(actual);
        expected.sort(ascending ? Comparator.naturalOrder() : Comparator.reverseOrder());

        assertEquals(PRODUCT_COUNT, actual.size(),
                () -> id + ": unexpected number of products");
        assertEquals(expected, actual,
                () -> id + ": prices are not sorted for option " + option);
    }
}
