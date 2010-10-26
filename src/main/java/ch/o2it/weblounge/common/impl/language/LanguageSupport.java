/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2009 The Weblounge Team
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

package ch.o2it.weblounge.common.impl.language;

import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.language.UnknownLanguageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * <code>LanguageSupport</code> is a helper class the facilitates the handling
 * of languages in numerous ways.
 */
public final class LanguageSupport {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(LanguageSupport.class);

  /** Globally available languages */
  private static final Map<String, Language> systemLanguages = new HashMap<String, Language>();

  /**
   * This class is not meant to be instantiated.
   */
  private LanguageSupport() {
    // Nothing to be done here
  }

  /**
   * Returns the language object that represents the given locale.
   * 
   * @param locale
   *          the locale
   * @return the language
   * @throws UnknownLanguageException
   *           if there is no language for the given locale
   */
  public static Language getLanguage(Locale locale)
      throws UnknownLanguageException {
    Language language = systemLanguages.get(locale.getLanguage());
    if (language == null) {
      language = new LanguageImpl(locale);
      systemLanguages.put(locale.getLanguage(), language);
    }
    return language;
  }

  /**
   * Returns the language object identified by the language identifier.
   * 
   * @param languageCode
   *          the language identifier
   * @return the language
   * @throws UnknownLanguageException
   *           if there is no language for the given locale
   */
  public static Language getLanguage(String languageCode)
      throws UnknownLanguageException {
    Language language = systemLanguages.get(languageCode);
    if (language != null)
      return language;
    for (Locale locale : Locale.getAvailableLocales()) {
      if (locale.getLanguage().equals(languageCode)) {
        language = new LanguageImpl(new Locale(languageCode, "", ""));
        systemLanguages.put(languageCode, language);
      }
    }
    if (language == null)
      throw new UnknownLanguageException(languageCode);
    return language;
  }

  /**
   * Reads the names of an object described in a weblounge configuration file in
   * various languages and applies them to the multilingual object
   * <code>o</code>.
   * <p>
   * The localized content is looked up in the tags specified by parameter
   * <code>tagName</code>, language identifier are expected in the
   * <code>language</code> attribute of these tags.
   * <p>
   * If the description is found in the default language, then
   * {@link LocalizableContent#setDefaultLanguage(Language)} is called.
   * <p>
   * The required format of the input node is as follows:
   * 
   * <pre>
   *     &lt;role&gt;
   *         &lt;id&gt;editor&lt;/id&gt;
   *         &lt;name language=&quot;de&quot;&gt;Editor&lt;/name&gt;
   *         &lt;name language=&quot;fr&quot;&gt;Editeur&lt;/name&gt;
   *         &lt;name language=&quot;it&quot;&gt;Editore&lt;/name&gt;
   * &lt;/role&gt;
   * </pre>
   * <p>
   * The method throws a &lt;code&gt;ConfigurationException&lt;/code&gt; if no
   * name is provided the site default language.
   * 
   * @param configuration
   *          the XML configuration node containing the descriptions
   * @param tagName
   *          the tag name containing the localized content
   * @param defaultLanguage
   *          the default language
   * @param o
   *          the localizable object
   * @param escape
   *          &lt;code&gt;true&lt;/code&gt; to filter out &quot; and '
   * @return the localized content
   */
  public static LocalizableContent<String> addDescriptions(Node configuration,
      String tagName, Language defaultLanguage, LocalizableContent<String> o,
      boolean escape) {

    XPath xpath = XPathFactory.newInstance().newXPath();
    return addDescriptions(configuration, tagName, defaultLanguage, o, escape, xpath);
  }

  /**
   * Reads the names of an object described in a weblounge configuration file in
   * various languages and applies them to the multilingual object
   * <code>o</code>.
   * <p>
   * The localized content is looked up in the tags specified by parameter
   * <code>tagName</code>, language identifier are expected in the
   * <code>language</code> attribute of these tags.
   * <p>
   * If the description is found in the default language, then
   * {@link LocalizableContent#setDefaultLanguage(Language)} is called.
   * <p>
   * The required format of the input node is as follows:
   * 
   * <pre>
   *     &lt;role&gt;
   *         &lt;id&gt;editor&lt;/id&gt;
   *         &lt;name language=&quot;de&quot;&gt;Editor&lt;/name&gt;
   *         &lt;name language=&quot;fr&quot;&gt;Editeur&lt;/name&gt;
   *         &lt;name language=&quot;it&quot;&gt;Editore&lt;/name&gt;
   * &lt;/role&gt;
   * </pre>
   * <p>
   * The method throws a &lt;code&gt;ConfigurationException&lt;/code&gt; if no
   * name is provided the site default language.
   * 
   * @param configuration
   *          the XML configuration node containing the descriptions
   * @param tagName
   *          the tag name containing the localized content
   * @param defaultLanguage
   *          the default language
   * @param o
   *          the localizable object
   * @param escape
   *          &lt;code&gt;true&lt;/code&gt; to filter out &quot; and '
   * @param xpath
   *          the xpath processor
   * @return the localized content
   */
  public static LocalizableContent<String> addDescriptions(Node configuration,
      String tagName, Language defaultLanguage, LocalizableContent<String> o,
      boolean escape, XPath xpath) {

    if (configuration == null)
      throw new IllegalArgumentException("Cannot extract from empty configuration");
    if (tagName == null)
      throw new IllegalArgumentException("Tagname must be specified");
    if (o == null)
      o = new LocalizableContent<String>();

    NodeList nodes = XPathHelper.selectList(configuration, tagName, xpath);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node name = nodes.item(i);
      String description = XPathHelper.valueOf(name, "text()", xpath);
      String lAttrib = XPathHelper.valueOf(name, "@language", xpath);
      Language language = LanguageSupport.getLanguage(lAttrib);
      if (language == null) {
        logger.debug("Found name in unsupported language {}", lAttrib);
        continue;
      }

      // Escape?
      if (escape) {
        description = description.replaceAll("\"", "");
        description = description.replaceAll("'", "");
      }

      // Add the entry
      logger.debug("Found description {}", description);
      o.put(description, language);
      if (language.equals(defaultLanguage)) {
        o.setDefaultLanguage(defaultLanguage);
      }
    }
    return o;
  }

  /**
   * Returns the localized variant for the given language.
   * <p>
   * For example, if <code>s</code> is <tt>file.jsp</tt> and
   * <code>language</code> is <tt>German</tt>, then this method returns
   * <tt>file_de.jsp</tt>.
   * 
   * @param s
   *          the file name
   * @param language
   *          the language variant to obtain
   * @return the localized variant of the text
   */
  public static String getLanguageVariant(String s, Language language) {
    int suffixPosition = s.lastIndexOf(".");
    String suffix = "";
    if (suffixPosition > -1) {
      suffix = s.substring(suffixPosition);
      s = s.substring(0, suffixPosition);
    }
    s += "_" + language.getIdentifier() + suffix;
    return s;
  }

  /**
   * Returns a String array containing the language variants of <code>s</code>
   * for the given language and site, ordered by priority, meaning most
   * specialized version first, with the originally passed in string last.
   * <p>
   * The priorities are evaluated as follows:
   * <ul>
   * <li>The requested language <code>l</code></li>
   * <li>The site default language of <code>site</code></li>
   * <li>The international version, equal to the original <code>s</code></li>
   * </ul>
   * 
   * @param s
   *          the file name
   * @param l
   *          the preferred language
   * @param site
   *          the site
   * @return the localized variant of the text
   */
  public static String[] getLanguageVariantsByPriority(String s, Language l,
      Language defaultLanguage) {
    if (s == null)
      throw new IllegalArgumentException("String must not be null");
    if (defaultLanguage == null)
      throw new IllegalArgumentException("Default language must not be null");
    String[] variants = new String[3 - ((l == null) ? 1 : 0)];
    if (l != null) {
      variants[0] = getLanguageVariant(s, l);
    }
    variants[1 - ((l == null) ? 1 : 0)] = getLanguageVariant(s, defaultLanguage);
    variants[2 - ((l == null) ? 1 : 0)] = s;
    return variants;
  }

  /**
   * Returns all language variants of the filename <code>s</code> that are
   * supported by the given site or, more precise, by the associated
   * <code>LanguageRegistry</code>.
   * <p>
   * The last entry of the returned variants is the original string
   * <code>s</code>.
   * 
   * @param s
   *          the filename
   * @param languages
   *          the languages used to build the variants
   */
  public static String[] getLanguageVariants(String s, Language[] languages) {
    String[] result = new String[languages.length + 1];
    result[languages.length] = s;
    for (int i = 0; i < languages.length; i++) {
      Language language = languages[i];
      result[i] = getLanguageVariant(s, language);
    }
    return result;
  }

  /**
   * Returns the original version string of text <code>s</code>.
   * <p>
   * For example, if <tt>s</tt> equals <tt>file_de.jsp</tt> then this method
   * returns <tt>file.jsp</tt>.
   * 
   * @param s
   *          the language filename
   * @param languages
   *          the languages
   * @return the original filename
   */
  public static String getBaseVersion(String s) {
    Language l = extractLanguage(s);
    if (l == null) {
      return s;
    }
    int languagePos = s.indexOf("_" + l.getIdentifier());
    String original = s.substring(0, languagePos);
    if (s.length() > languagePos + 3) {
      String suffix = s.substring(languagePos + 3);
      original += suffix;
    }
    return original;
  }

  /**
   * Returns the language of this file. For example, if <tt>s</tt> is
   * <tt>file_de.jsp</tt> then this method returns the German language object.
   * <p>
   * <b>Note:</b> This method returns <code>null</code> if the string contains
   * an unknown language identifier or no language identifier at all.
   * 
   * @param s
   *          the filename
   * @return the language object or <code>null</code>
   */
  public static Language extractLanguage(String s) {
    int languagePosition = s.lastIndexOf("_");
    if ((languagePosition < 0) || (languagePosition + 1 > s.length()))
      return null;

    Language l = null;
    String languageId = s.substring(languagePosition + 1, languagePosition + 3);
    l = getLanguage(languageId);
    return l;
  }

  /**
   * Returns the language out of <code>choices</code> that matches the client's
   * requirements as indicated through the <code>Accept-Language</code> header.
   * If no match is possible, <code>null</code> is returned.
   * 
   * @param choices
   *          the available locales
   * @param request
   *          the http request
   */
  public static Language getPreferredLanguage(Set<Language> choices,
      HttpServletRequest request) {
    if (request.getHeader("Accept-Language") != null) {
      Enumeration<?> locales = request.getLocales();
      while (locales.hasMoreElements()) {
        Language l = getLanguage((Locale) locales.nextElement());
        if (choices.contains(l))
          return l;
      }
    }
    return null;
  }

  /**
   * Returns the language out of <code>choices</code> that matches the client's
   * requirements as indicated through the <code>Accept-Language</code> header.
   * If no match is possible, it is determined if <code>choices</code> contains
   * <code>defaultLanguage</code> in which case that language is returned. If
   * neither of this is the case, <code>null</code> is returned.
   * 
   * @param choices
   *          the available locales
   * @param defaultLanguage
   *          the default language fallback
   * @param request
   *          the http request
   */
  public static Language getPreferredLanguage(Set<Language> choices,
      HttpServletRequest request, Language defaultLanguage) {
    Language preferred = getPreferredLanguage(choices, request);
    if (preferred == null && choices.contains(defaultLanguage)) {
      return defaultLanguage;
    }
    return preferred;
  }

}