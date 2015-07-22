/*
 * Copyright (C) 2015 Victor Anuebunwa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Victor Anuebunwa
 */
public class SearcherTest {

    public SearcherTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of setSearchCategory method, of class Searcher.
     */
    @org.junit.Test
    public void testSetSearchCategory() {
        System.out.println("setSearchCategory");
        SearchCategory selectedSearchType = null;
        Searcher instance = new Searcher();
        instance.setSearchCategory(selectedSearchType);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of setSearchField method, of class Searcher.
     */
    @org.junit.Test
    public void testSetSearchField() {
        System.out.println("setSearchField");
        SearchField selectedSearchField = null;
        Searcher instance = new Searcher();
        instance.setSearchField(selectedSearchField);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of search method, of class Searcher.
     */
    @org.junit.Test
    public void testSearch() throws Exception {
        System.out.println("search");
        String q = "releva";
        Searcher instance = new Searcher();
        instance.setSearchCategory(SearchCategory.ALL);
        instance.setSearchField(SearchField.TITLE);
        List<Material> result = instance.search(q);
        result.stream().forEach((r) -> {
            System.out.println(r);
        });
        assertEquals(true, result.size() > 0);
    }

}
