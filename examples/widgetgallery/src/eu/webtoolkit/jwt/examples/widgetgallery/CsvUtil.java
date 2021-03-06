/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WObject;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;

public class CsvUtil {
    public static void readFromCsv(BufferedReader reader, WAbstractItemModel model) {
        try {
            Pattern pat = Pattern.compile(",\"(.+?)\"");
            String line = null;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pat.matcher(line);
                StringBuffer sb = new StringBuffer();
                while (matcher.find()) {
                    matcher.appendReplacement(sb, ","
                            + matcher.group(1).replaceAll(",", "&comm"));
                }
                matcher.appendTail(sb);
                String[] fields = sb.toString().split(",");
                String text;

                if (row != 0)
                    model.insertRow(model.getRowCount());

                for (int col = 0; col < fields.length; col++) {
                    text = (fields[col] != null ? fields[col].replaceAll(
                            "&comm", ",") : "").replaceAll("\"", "");

                    if (col >= model.getColumnCount())
                        model.insertColumn(col);

                    if (row == 0) {
                        model.setHeaderData(col, text);
                    } else {
                        try {
                            Integer i = Integer.valueOf(text);
                            model.setData(row - 1, col, i);
                        } catch (NumberFormatException e) {
                            model.setData(row - 1, col, text);
                        }
                    }
                }
                row++;
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

 	public static WStandardItemModel csvToModel(String resourceName, WObject parent) {
        InputStream is = CsvUtil.class.getResourceAsStream("/eu/webtoolkit/jwt/examples/widgetgallery/data/" + resourceName);

        WStandardItemModel model = new WStandardItemModel();
        CsvUtil.readFromCsv(new BufferedReader(new InputStreamReader(is)), model);
        return model;
	}
}
