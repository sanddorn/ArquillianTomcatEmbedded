package de.bermuda.arquillian.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ContentTypeProxyServlet extends HttpServlet {
   private HttpClient client;
   private URL serviceURL;
   private Logger log = LoggerFactory.getLogger("ContentProxy");
   private Logger requestLogger = LoggerFactory.getLogger("RequestLog");

   @Override
   public void init() {
      client = new HttpClient();
      String url = "http://soap.bermuda.de/soapAction:8080";
      ServletConfig config = getServletConfig();
      if (config != null && config.getInitParameter("url") != null) {
         log.debug("Using Config Param");
         url = config.getInitParameter("url");
      }
      try {
         serviceURL = new URL(url);
      } catch (MalformedURLException e) {
         log.error("URL invalid", e);
      }
   }

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      String localPathInfo = req.getPathInfo() == null ? "" : req.getPathInfo();
      String queryString = req.getQueryString();
      log.info("Setting URL to: " + serviceURL.toExternalForm() + localPathInfo);
      PostMethod post = new PostMethod(serviceURL.toExternalForm() + localPathInfo);
      post.setQueryString(queryString);

      copyRequestHeaders(req, post);

      copyRequestBody(req, post);

      try {
         int statusCode = client.executeMethod(post);
         if (statusCode != HttpStatus.SC_OK) {
            log.warn("Could not post request to i.Serve: " + statusCode);
         }

         migrateResponseHeader(resp, post);

         copyResponseContent(resp, post);
      } catch (HttpException e) {
         log.error("Catched HTTPException: ", e);
      } catch (IOException e) {
         log.error("Catched IOException: ", e);
      }
   }

   private void copyRequestBody(HttpServletRequest req, PostMethod post) throws IOException {
      BufferedReader contentReader = req.getReader();
      StringBuilder content = new StringBuilder();
      String contentLine = "";
      while (contentLine != null) {
         content.append(contentLine);
         contentLine = contentReader.readLine();
      }
      StringRequestEntity requestEntity =
            new StringRequestEntity(content.toString(), req.getContentType(), req.getCharacterEncoding());
      post.setRequestEntity(requestEntity);
      requestLogger.info("RequestBody: " + content);
   }

   private void copyResponseContent(HttpServletResponse resp, HttpMethod post) throws IOException {
      OutputStream out = resp.getOutputStream();
      InputStream input = post.getResponseBodyAsStream();
      StringBuilder responseBody = new StringBuilder();
      byte[] buffer = new byte[1024];
      while (input.read(buffer) >= 0) {

         responseBody.append(new String(buffer));
         out.write(buffer);
      }
      requestLogger.info("ResponseBody: " + responseBody.toString());
   }

   private void migrateResponseHeader(HttpServletResponse resp, HttpMethod post) {
      for (Header header : post.getResponseHeaders()) {
         if (!(header.getName().equalsIgnoreCase("Content-Type")) &&
               !(header.getName().equalsIgnoreCase("Transfer-Encoding"))) {
            resp.addHeader(header.getName(), header.getValue());

            log.info("Copying response header: " + header.getName() + ": " + header.getValue());
         }
      }
      resp.setContentType("application/soap+xml");
   }

   private void copyRequestHeaders(HttpServletRequest req, HttpMethod post) {
      Enumeration<String> headerNames = req.getHeaderNames();
      while (headerNames.hasMoreElements()) {
         String headerName = headerNames.nextElement();
         post.addRequestHeader(headerName, req.getHeader(headerName));
         log.info("Copying request header: " + headerName + ": " + req.getHeader(headerName));
      }
   }
}
