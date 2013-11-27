package de.bermuda.arquillian.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Copyright 2013 Nils Bokermann
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
public class DummyServlet extends HttpServlet {
   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      BufferedReader contentReader = req.getReader();
      String contentLine = "";
      resp.setContentType("text/xml");
      String contextPath = req.getPathInfo() == null ? "" : req.getPathInfo();
      Writer writer = resp.getWriter();
      writer.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
      writer.write("<path>" + contextPath + "</path>");
      writer.write("<content>");
      do {
         writer.write(contentLine);
         contentLine = contentReader.readLine();
      } while (contentLine != null);
      writer.write("</content>");
   }
}
