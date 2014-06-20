/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.jbws2074.usecase1.client;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.test.ws.jaxws.jbws2074.usecase1.service.POJOIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2074] Resource injection in jaxws endpoints and handlers
 *
 * @author ropalka@redhat.com
 */
public final class JBWS2074TestCase extends JBossWSTest
{
   static {
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws2074-usecase1.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.logging\n"))
               .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.DescriptorResourcesHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.JavaResourcesHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.ManualResourcesHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase1.service.POJOIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase1.service.POJOImpl.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws2074/usecase1/service/jaxws-service-handlers.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase1/WEB-INF/web.xml"));
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws2074-usecase1.ear") { {
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2074-usecase1.war"))
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase1-ear/META-INF/application.xml"), "application.xml");
         }
      });
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2074TestCase.class, "");
   }

   public void executeTest() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-jbws2074-usecase1/Service";
      QName serviceName = new QName("http://ws.jboss.org/jbws2074", "POJOService");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      POJOIface port = (POJOIface)service.getPort(POJOIface.class);

      String retStr = port.echo("hello");

      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":Inbound:ManualResourcesHandler");
      expStr.append(":Inbound:JavaResourcesHandler");
      expStr.append(":Inbound:DescriptorResourcesHandler");
      expStr.append(":POJOImpl");
      expStr.append(":Outbound:DescriptorResourcesHandler");
      expStr.append(":Outbound:JavaResourcesHandler");
      expStr.append(":Outbound:ManualResourcesHandler");
      assertEquals(expStr.toString(), retStr);
   }

   public void testUsecase1WithoutEar() throws Exception
   {
      try
      {
         deploy("jaxws-jbws2074-usecase1.war");
         executeTest();
      }
      finally
      {
         undeploy("jaxws-jbws2074-usecase1.war");
      }
   }

   public void testUsecase1WithEar() throws Exception
   {
      try
      {
         deploy("jaxws-jbws2074-usecase1.ear");
         executeTest();
      }
      finally
      {
         undeploy("jaxws-jbws2074-usecase1.ear");
      }
   }

}
