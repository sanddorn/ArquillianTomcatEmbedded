package de.bermuda.arquillian.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Revision: $Revision$
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
