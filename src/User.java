
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;

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
/**
 *
 * @author Victor Anuebunwa
 */
public class User {

    private final Searcher cs;
    private static User thisClass;

    private User() {
        //Load index and searcher
        cs = new Searcher();
    }

    public static User getInstance() {
        if (thisClass == null) {
            thisClass = new User();
        }
        return thisClass;
    }

    public void openMaterial(Material material) {
        if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(new File(material.getPath()));
            } catch (IOException ex) {
                Utility.writeLog(ex);
            }
        }
    }

    public List<Material> query(String queryString, SearchCategory category, SearchField field) throws IOException, ParseException {
        if (cs != null) {

            //Set the selected category by user
            cs.setSearchCategory(category);
            //set the selected field by user
            cs.setSearchField(field);

            return cs.search(queryString);
        } else {
            throw new IllegalStateException("search engine not ready");
        }
    }

}
