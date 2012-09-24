/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2011 The Weblounge Team
 *  http://entwinemedia.com/weblounge
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software Foundation
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package ch.entwine.weblounge.common.impl.site;

import ch.entwine.weblounge.common.impl.testing.IntegrationTestGroup;
import ch.entwine.weblounge.common.impl.testing.IntegrationTestParser;
import ch.entwine.weblounge.common.impl.util.classloader.BundleClassLoader;
import ch.entwine.weblounge.common.impl.util.classloader.ContextClassLoaderUtils;
import ch.entwine.weblounge.common.impl.util.config.ConfigurationUtils;
import ch.entwine.weblounge.common.impl.util.xml.ValidationErrorHandler;
import ch.entwine.weblounge.common.site.Site;
import ch.entwine.weblounge.testing.IntegrationTest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.Callable;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * The <code>SiteActivator</code> is used to load a site from the enclosing
 * bundle and is meant to be used with the
 * <code>OSGi Declarative Services</code> facility.
 * <p>
 * It will scan the bundle resources for a file called <code>site.xml</code>
 * located at <code>site</code> directory relative to the bundle root.
 */
public class SiteActivator {

  /** The logging facility */
  static final Logger logger = LoggerFactory.getLogger(SiteActivator.class);

  /** Tests activation/deactivation option */
  public static final String OPT_TESTS = "tests";

  /** The site service registration */
  protected ServiceRegistration siteService = null;

  /** The site */
  protected Site site = null;

  /**
   * Callback from the OSGi environment to activate a site. Subclasses should
   * make sure to call this super implementation as it will assist in correctly
   * setting up the site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if the site activation fails
   */
  void activate(final ComponentContext context) throws Exception {
    BundleContext bundleContext = context.getBundleContext();

    logger.debug("Scanning bundle '{}' for site.xml", bundleContext.getBundle().getSymbolicName());

    // Prepare schema validator
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaUrl = SiteImpl.class.getResource("/xsd/site.xsd");
    Schema siteSchema = schemaFactory.newSchema(schemaUrl);

    // Set up the document builder
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setSchema(siteSchema);
    docBuilderFactory.setNamespaceAware(true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    // Load the site
    Enumeration<URL> e = bundleContext.getBundle().findEntries("site", "site.xml", false);
    if (e != null && e.hasMoreElements()) {
      String bundleName = bundleContext.getBundle().getSymbolicName();
      URL siteUrl = e.nextElement();

      logger.info("Loading site from bundle '{}'", bundleName);

      // Load and validate the site descriptor
      ValidationErrorHandler errorHandler = new ValidationErrorHandler(siteUrl);
      docBuilder.setErrorHandler(errorHandler);
      final Document siteXml = docBuilder.parse(siteUrl.openStream());
      if (errorHandler.hasErrors()) {
        logger.error("Errors found while validating site descriptor in bundle '{}'. Site is not loaded", bundleName);
        return;
      }

      final BundleClassLoader bundleClassLoader = new BundleClassLoader(bundleContext.getBundle());
      ContextClassLoaderUtils.doWithClassLoader(bundleClassLoader, new Callable<Void>() {
        public Void call() throws Exception {
          site = SiteImpl.fromXml(siteXml.getFirstChild());
          if (site instanceof SiteImpl) {
            ((SiteImpl) site).activate(context);
          }
          return null;
        }
      });

      // Register it as a service
      logger.debug("Registering site '{}' in the service registry", site);
      siteService = bundleContext.registerService(Site.class.getName(), site, context.getProperties());

      logger.debug("Site '{}' loaded", site);
    } else {
      logger.warn("Site activator was unable to locate site.xml");
    }

    // If integration tests are switched off explicitly, don't even bother
    String testActivation = (String) context.getProperties().get(OPT_TESTS);
    int testCount = 0;
    if (ConfigurationUtils.isDisabled(testActivation)) {
      logger.info("Integration tests are switched off for site '{}'", site);
      return;
    }

    // Find and register site-wide integration tests
    Enumeration<URL> siteDirectories = bundleContext.getBundle().findEntries("site", "*", false);
    while (siteDirectories != null && siteDirectories.hasMoreElements()) {
      URL entry = siteDirectories.nextElement();
      if (entry.getPath().endsWith("/tests/")) {
        testCount += loadIntegrationTests(bundleContext, entry.getPath());
        break;
      }
    }

    // Find and register module integration tests
    Enumeration<URL> modules = bundleContext.getBundle().findEntries("site/modules", "*", false);
    while (modules != null && modules.hasMoreElements()) {
      URL module = modules.nextElement();
      Enumeration<URL> moduleDirectories = bundleContext.getBundle().findEntries(module.getPath(), "*", false);
      while (moduleDirectories != null && moduleDirectories.hasMoreElements()) {
        URL entry = moduleDirectories.nextElement();
        if (entry.getPath().endsWith("/tests/")) {
          testCount += loadIntegrationTests(bundleContext, entry.getPath());
          break;
        }
      }
    }

    if (testCount > 0)
      logger.info("Registered {} integration tests for site '{}'", testCount, site);
  }

  /**
   * Callback from the OSGi environment to deactivate a site. Subclasses should
   * make sure to call this super implementation as it will assist in correctly
   * shutting down the site.
   * <p>
   * This method should be configured in the <tt>Dynamic Services</tt> section
   * of your bundle.
   * 
   * @param context
   *          the component context
   * @throws Exception
   *           if the site deactivation fails
   */
  void deactivate(ComponentContext context) throws Exception {
    if (site != null && site instanceof SiteImpl) {
      ((SiteImpl) site).deactivate(context);
    }

    if (siteService != null) {
      try {
        siteService.unregister();
      } catch (IllegalStateException e) {
        // Never mind, the service has been unregistered already
      } catch (Throwable t) {
        logger.error("Unregistering site failed: {}", t.getMessage());
      }
    }
  }

  /**
   * Loads and registers the integration tests that are found in the bundle at
   * the given location.
   * 
   * @param bundleContext
   *          the bundle context
   * @param dir
   *          the directory containing the test files
   */
  int loadIntegrationTests(BundleContext bundleContext, String dir) {
    Enumeration<?> entries = bundleContext.getBundle().findEntries(dir, "*.xml", true);

    // Schema validator setup
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    URL schemaUrl = SiteImpl.class.getResource("/xsd/test.xsd");
    Schema testSchema = null;
    try {
      testSchema = schemaFactory.newSchema(schemaUrl);
    } catch (SAXException e) {
      logger.error("Error loading XML schema for test definitions: {}", e.getMessage());
      return 0;
    }

    // Module.xml document builder setup
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setSchema(testSchema);
    docBuilderFactory.setNamespaceAware(true);

    int testCount = 0;
    while (entries != null && entries.hasMoreElements()) {
      URL entry = (URL) entries.nextElement();

      // Validate and read the module descriptor
      ValidationErrorHandler errorHandler = new ValidationErrorHandler(entry);
      DocumentBuilder docBuilder;

      try {
        docBuilder = docBuilderFactory.newDocumentBuilder();
        docBuilder.setErrorHandler(errorHandler);
        Document doc = docBuilder.parse(entry.openStream());
        if (errorHandler.hasErrors()) {
          logger.warn("Error parsing integration test {}: XML validation failed", entry);
          continue;
        }
        IntegrationTestGroup test = IntegrationTestParser.fromXml(doc.getFirstChild());
        test.setSite(site);
        test.setGroup(site.getName());
        logger.debug("Registering integration test " + test.getClass());
        bundleContext.registerService(IntegrationTest.class.getName(), test, null);
        testCount++;
      } catch (SAXException e) {
        throw new IllegalStateException(e);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      } catch (ParserConfigurationException e) {
        throw new IllegalStateException(e);
      }
    }

    return testCount;
  }

}
