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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Victor Anuebunwa
 */
public class LibraryTest {

    public LibraryTest() {
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
     * Test of init method, of class Library.
     */
    @Test
    public void testInit() {
        System.out.println("init");
    }

    /**
     * Test of searchLibrary method, of class Library.
     */
    @Test
    public void testSearchLibrary() {
        System.out.println("searchLibrary");
        String query = "";
    }

    /**
     * Test of ready method, of class Library.
     */
    @Test
    public void testReady() {
        System.out.println("ready");
    }

    /**
     * Test of getInstance method, of class Library.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
    }

    /**
     * Test of getMaterials method, of class Library.
     */
    @Test
    public void testGetMaterials() {
        System.out.println("getMaterials");
    }

    /**
     * Test of getMaterial method, of class Library.
     */
    @Test
    public void testGetMaterial() {
        System.out.println("getMaterial");
        Library instance = Library.getInstance();
        Material expResult = instance.getMaterials().get(0);
        String path = expResult.getPath().toString();
        Material result;
        try {
            result = instance.getMaterial(new URI(path));
            assertEquals(expResult, result);
        } catch (URISyntaxException ex) {
            Logger.getLogger(LibraryTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of addMaterial method, of class Library.
     */
    @Test
    public void testAddMaterial() {
        System.out.println("addMaterial");
    }

}
