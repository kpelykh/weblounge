/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2010 The Weblounge Team
 *  http://weblounge.o2it.ch
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

package ch.o2it.weblounge.common.site;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.o2it.weblounge.common.Times;
import ch.o2it.weblounge.common.impl.image.ImageStyleImpl;
import ch.o2it.weblounge.common.impl.language.LanguageImpl;
import ch.o2it.weblounge.common.impl.page.PageTemplateImpl;
import ch.o2it.weblounge.common.impl.site.SiteImpl;
import ch.o2it.weblounge.common.impl.user.WebloungeUserImpl;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.security.DigestType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Test case for {@link SiteImpl}.
 */
public class SiteImplTest {

  /** The site instance under test */
  protected SiteImpl site = null;

  /** Site identifier */
  protected final String identifier = "dev";

  /** Sets the site enabled state */
  protected final boolean enabled = true;

  /** Site description */
  protected final String description = "Main Site";

  /** Site administrator */
  protected WebloungeUserImpl administrator = null;

  /** Site administrator login */
  protected final String administratorLogin = "blogadmin";

  /** Site administrator name */
  protected final String administratorName = "Web Office";

  /** Site administrator email */
  protected final String administratorEmail = "weboffice@nowhere.com";

  /** Site administrator password */
  protected final String administratorPassword = "secret";

  /** Default page template */
  protected PageTemplate defaultTemplate = null;

  /** Default template id */
  protected final String defaultTemplateId = "default";
  
  /** Default template recheck time */
  protected final long defaultTemplateRecheckTime = Times.MS_PER_DAY + 10*Times.MS_PER_MIN;

  /** Default template valid time */
  protected final long defaultTemplateValidTime = Times.MS_PER_WEEK + Times.MS_PER_DAY + Times.MS_PER_HOUR + Times.MS_PER_MIN;;

  /** Default template url */
  protected final String defaultTemplateUrl = "file://template/default.jsp";

  /** Default template stage */
  protected final String defaultTemplateStage = "boxes";

  /** Default template German name */
  protected final String defaultTemplateNameGerman = "Standard Vorlage";

  /** Default template English name */
  protected final String defaultTemplateNameEnglish = "Default template";

  /** Mobile page template */
  protected PageTemplate mobileTemplate = null;

  /** Mobile template id */
  protected final String mobileTemplateId = "mobile";

  /** Mobile template url */
  protected final String mobileTemplateUrl = "file://template/mobile.jsp";

  /** Mobile template class name */
  protected final String mobileTemplateClass = "ch.o2it.weblounge.common.impl.site.JSPTemplate";
  
  /** Mobile template German name */
  protected final String mobileTemplateNameGerman = "Mobile";

  /** Mobile template English name */
  protected final String mobileTemplateNameEnglish = "Mobile";
  
  /** The site hostnames */
  protected List<String> hostnames = new ArrayList<String>();
  
  /** Default hostname */
  protected String defaultHostname = "www.weblounge.org";

  /** Default hostname */
  protected String fallbackHostname = "*.nowhere.com";

  /** Default hostname */
  protected String localhost = "localhost:8080";
  
  /** Portrait image style */
  protected ImageStyle portraitImageStyle = null;

  /** High resolution image style */
  protected ImageStyle highresImageStyle = null;
  
  /** The English language */
  protected final Language English = new LanguageImpl(new Locale("en"));

  /** The German language */
  protected final Language German = new LanguageImpl(new Locale("de"));

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    setupPrerequisites();
    site = new SiteImpl();
    site.setIdentifier(identifier);
    site.setAutoStart(enabled);
    site.setDescription(description);
    site.setAdministrator(administrator);
    site.setDefaultTemplate(defaultTemplate);
    site.addTemplate(mobileTemplate);
    site.setDefaultLanguage(German);
    site.addLanguage(English);
    site.addHostName(defaultHostname);
    site.addHostName(fallbackHostname);
    site.addHostName(localhost);
    site.addImageStyle(portraitImageStyle);
    site.addImageStyle(highresImageStyle);
  }

  /**
   * Sets up preliminary data structures.
   * 
   * @throws Exception
   */
  protected void setupPrerequisites() throws Exception {
    // Administrator
    administrator = new WebloungeUserImpl(administratorLogin);
    administrator.setName(administratorName);
    administrator.setEmail(administratorEmail);
    administrator.setPassword(administratorPassword.getBytes(), DigestType.md5);
    // Default template
    defaultTemplate = new PageTemplateImpl(defaultTemplateId, new URL(defaultTemplateUrl));
    defaultTemplate.setRecheckTime(defaultTemplateRecheckTime);
    defaultTemplate.setValidTime(defaultTemplateValidTime);
    defaultTemplate.setComposeable(true);
    defaultTemplate.setStage(defaultTemplateStage);
    defaultTemplate.setName(defaultTemplateNameEnglish, English);
    defaultTemplate.setName(defaultTemplateNameGerman, German);
    // Mobile template
    mobileTemplate = new PageTemplateImpl(mobileTemplateId, new URL(mobileTemplateUrl));
    mobileTemplate.setComposeable(true);
    mobileTemplate.setName(mobileTemplateNameEnglish, English);
    mobileTemplate.setName(mobileTemplateNameGerman, German);
    // Portrait image style
    portraitImageStyle = new ImageStyleImpl("portrait", 150, 200, ScalingMode.Box, true);
    portraitImageStyle.setName("Portraitbild", German);
    portraitImageStyle.setName("Portrait image", English);
    // Highresolution image style
    highresImageStyle = new ImageStyleImpl("highresolution", 800, 600, ScalingMode.Box, true);
    highresImageStyle.setName("Hohe Auflösung", German);
    highresImageStyle.setName("High resolution", English);
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.SiteImpl#setIdentifier(java.lang.String)}
   * .
   */
  @Test
  public void testSetIdentifier() {
    site.setIdentifier("1ab_2ABC3-.0");
    try {
      site.setIdentifier("Test id with spaces and,strange/characters");
    } catch (IllegalArgumentException e) {
      // This is expected
    }
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getIdentifier()}.
   */
  @Test
  public void testGetIdentifier() {
    assertEquals(identifier, site.getIdentifier());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.SiteImpl#isStartedAutomatically()}.
   */
  @Test
  public void testIsStartedAutomatically() {
    assertEquals(enabled, site.isStartedAutomatically());
  }

  /**
   * Test method for
   * {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getDescription(ch.o2it.weblounge.common.language.Language)}
   * .
   */
  @Test
  public void testGetDescription() {
    assertEquals(description, site.getDescription());
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#addTemplate(ch.o2it.weblounge.common.site.PageTemplate)}.
   */
  @Test
  public void testAddTemplate() throws Exception {
    site.addTemplate(defaultTemplate);
    assertEquals(2, site.getTemplates().length);
    site.addTemplate(new PageTemplateImpl("test", new URL(defaultTemplateUrl)));
    assertEquals(3, site.getTemplates().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#removeTemplate(ch.o2it.weblounge.common.site.PageTemplate)}.
   */
  @Test
  public void testRemoveTemplate() {
    site.removeTemplate(defaultTemplate);
    assertEquals(1, site.getTemplates().length);
    assertTrue(site.getDefaultTemplate() == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getTemplate(java.lang.String)}.
   */
  @Test
  public void testGetTemplate() throws Exception {
    PageTemplate d = site.getTemplate(defaultTemplateId);
    assertNotNull(d);
    assertEquals(defaultTemplateId, d.getIdentifier());
    assertEquals(new URL(defaultTemplateUrl), d.getRenderer());
    assertEquals(defaultTemplateRecheckTime, d.getRecheckTime());
    assertEquals(defaultTemplateValidTime, d.getValidTime());
    assertEquals(defaultTemplateStage, d.getStage());
    assertEquals(defaultTemplateNameEnglish, d.getName(English));
    assertEquals(defaultTemplateNameGerman, d.getName(German));
    
    PageTemplate m = site.getTemplate(mobileTemplateId);
    assertNotNull(m);
    assertEquals(mobileTemplateId, m.getIdentifier());
    assertEquals(new URL(mobileTemplateUrl), m.getRenderer());
    assertEquals(Renderer.DEFAULT_RECHECK_TIME, m.getRecheckTime());
    assertEquals(Renderer.DEFAULT_VALID_TIME, m.getValidTime());
    assertEquals(PageTemplate.DEFAULT_STAGE, m.getStage());
    assertEquals(mobileTemplateNameEnglish, m.getName(English));
    assertEquals(mobileTemplateNameGerman, m.getName(German));

    assertTrue(site.getTemplate("test") == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getTemplates()}.
   */
  @Test
  public void testGetTemplates() {
    assertNotNull(site.getTemplates());
    assertEquals(2, site.getTemplates().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getDefaultTemplate()}.
   */
  @Test
  public void testGetDefaultTemplate() {
    assertEquals(defaultTemplate, site.getDefaultTemplate());
  }
  
  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#addLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testAddLanguage() {
    site.addLanguage(English);
    assertEquals(2, site.getLanguages().length);
    site.addLanguage(new LanguageImpl(new Locale("fr")));
    assertEquals(3, site.getLanguages().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#removeLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testRemoveLanguage() {
    site.removeLanguage(German);
    assertEquals(1, site.getLanguages().length);
    assertTrue(site.getDefaultLanguage() == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getLanguage(java.lang.String)}.
   */
  @Test
  public void testGetLanguage() {
    assertEquals(German, site.getLanguage(German.getIdentifier()));
    assertTrue(site.getLanguage("fr") == null);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getLanguages()}.
   */
  @Test
  public void testGetLanguages() {
    assertEquals(2, site.getLanguages().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#supportsLanguage(ch.o2it.weblounge.common.language.Language)}.
   */
  @Test
  public void testSupportsLanguage() {
    assertTrue(site.supportsLanguage(German));
    assertTrue(site.supportsLanguage(English));
    assertFalse(site.supportsLanguage(new LanguageImpl(new Locale("fr"))));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getDefaultLanguage()}.
   */
  @Test
  public void testGetDefaultLanguage() {
    assertEquals(German, site.getDefaultLanguage());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#removeHostname(java.lang.String)}.
   */
  @Test
  public void testRemoveHostname() {
    site.removeHostname("test");
    assertEquals(3, site.getHostNames().length);
    site.removeHostname(defaultHostname);
    assertEquals(2, site.getHostNames().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getHostNames()}.
   */
  @Test
  public void testGetHostNames() {
    assertEquals(3, site.getHostNames().length);
    assertEquals(defaultHostname, site.getHostNames()[0]);
    assertEquals(fallbackHostname, site.getHostNames()[1]);
    assertEquals(localhost, site.getHostNames()[2]);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getHostName()}.
   */
  @Test
  public void testGetHostName() {
    assertEquals(defaultHostname, site.getHostName());
    site.removeHostname(defaultHostname);
    assertEquals(2, site.getHostNames().length);
    assertEquals(fallbackHostname, site.getHostName());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getUrl()}.
   */
  @Test
  public void testGetUrl() {
    assertEquals(defaultHostname, site.getUrl().getPath());
    site.removeHostname(defaultHostname);
    site.removeHostname(fallbackHostname);
    site.removeHostname(localhost);
    assertEquals("localhost/", site.getUrl().getPath());
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#removeImageStyle(java.lang.String)}.
   */
  @Test
  public void testRemoveImageStyle() {
    site.removeImageStyle(portraitImageStyle.getIdentifier());
    assertEquals(1, site.getImageStyles().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getImageStyle(java.lang.String)}.
   */
  @Test
  public void testGetImageStyle() {
    assertEquals(highresImageStyle, site.getImageStyle(highresImageStyle.getIdentifier()));
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getImageStyles()}.
   */
  @Test
  public void testGetImageStyles() {
    assertEquals(2, site.getImageStyles().length);
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#removeLayout(java.lang.String)}.
   */
  @Test @Ignore
  public void testRemoveLayout() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getLayout(java.lang.String)}.
   */
  @Test @Ignore
  public void testGetLayout() {
    fail("Not yet implemented"); // TODO
  }

  /**
   * Test method for {@link ch.o2it.weblounge.common.impl.site.SiteImpl#getLayouts()}.
   */
  @Test @Ignore
  public void testGetLayouts() {
    assertEquals(0, site.getLayouts().length);
  }

}
