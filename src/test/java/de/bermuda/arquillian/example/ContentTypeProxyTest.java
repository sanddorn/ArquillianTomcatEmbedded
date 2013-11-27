package de.bermuda.arquillian.example;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Revision: $Revision$
 */

@RunWith(Arquillian.class)
public class ContentTypeProxyTest {

   private static final String BASE_URL = "http://localhost:8880/ContentTypeProxy";

   @Deployment
   public static WebArchive createDeployment() {
      return ShrinkWrap.create(WebArchive.class, "ContentTypeProxy.war").addClass(ContentTypeProxyServlet.class)
            .setWebXML("WEB-INF/web.xml");
   }

   @Test
   public void checkContentTypeTest() {
      PostMethod postMethod = postRequest();
      String contentType = postMethod.getResponseHeader("Content-Type").getValue();
      Assert.assertEquals("Content-Type not right", contentType, "application/soap+xml");
   }

   @Test
   public void checkBodyTest() {
      String content = "TestContent";
      PostMethod postMethod = postRequest(content);
      StringBuffer body = getHTTPBody(postMethod);

      Assert.assertTrue("HTTP Body is empty", body.length() > 0);
      String returnedContent = body.substring(body.indexOf("<content>") + 9, body.indexOf("</content>"));
      Assert.assertEquals("Body was not copied", content, returnedContent);
   }

   @Test
   public void checkURLTest() {
      String localPath = "/foo/bar";
      PostMethod postMethod = postRequest("TestContent", localPath);
      StringBuffer body = getHTTPBody(postMethod);

      Assert.assertTrue("HTTP Body is empty", body.length() > 0);
      String returnedPath = body.substring(body.indexOf("<path>") + 6, body.indexOf("</path>"));
      Assert.assertEquals("Path was not correctly mirrored", localPath, returnedPath);
   }

   private StringBuffer getHTTPBody(PostMethod postMethod) {
      StringBuffer body = new StringBuffer();
      byte[] buffer = new byte[1024];
      try {
         InputStream instream = postMethod.getResponseBodyAsStream();
         while (instream.read(buffer) >= 0) {
            body.append(new String(buffer));
         }
      } catch (IOException e) {
         Assert.assertTrue("Could not read HTTP body", false);
      }
      return body;
   }

   private PostMethod postRequest(String body, String localPath) {
      HttpClient client = new HttpClient();
      PostMethod postMethod = new PostMethod(BASE_URL + "/proxy" + localPath);
      try {
         StringRequestEntity entity = new StringRequestEntity(body, "application/soap+xml", "UTF-8");
         postMethod.setRequestEntity(entity);
      } catch (IOException e) {
         Assert.assertTrue("Catched IOException", false);
      }
      try {
         int status = client.executeMethod(postMethod);
         Assert.assertEquals("Expected OK", status, HttpStatus.SC_OK);
      } catch (HttpException e) {
         Assert.assertFalse("HttpException", true);
      } catch (IOException e) {
         Assert.assertFalse("IOException", true);
      }
      return postMethod;
   }

   private PostMethod postRequest(String body) {
      return postRequest(body, "");
   }

   private PostMethod postRequest() {
      return postRequest("MeinText");
   }
}
