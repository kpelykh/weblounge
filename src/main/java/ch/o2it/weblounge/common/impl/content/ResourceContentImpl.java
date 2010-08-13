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

package ch.o2it.weblounge.common.impl.content;

import ch.o2it.weblounge.common.content.ResourceContent;
import ch.o2it.weblounge.common.language.Language;
import ch.o2it.weblounge.common.user.User;

import java.util.Date;

/**
 * Default implementation of a resource content.
 */
public class ResourceContentImpl implements ResourceContent {

  /** The content's language */
  protected Language language = null;

  /** Creation information */
  protected CreationContext creationCtx = new CreationContext();
  
  /** True if this is the original content */
  protected boolean isOriginal = false;

  /**
   * Creates a new resource content representation.
   * 
   * @param language
   *          the content language
   */
  protected ResourceContentImpl(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language cannot be null");
    this.language = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ResourceContent#setLanguage(ch.o2it.weblounge.common.language.Language)
   */
  public void setLanguage(Language language) {
    if (language == null)
      throw new IllegalArgumentException("Language must not be null");
    this.language = language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ResourceContent#getLanguage()
   */
  public Language getLanguage() {
    return language;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreationDate()
   */
  public Date getCreationDate() {
    return creationCtx.getCreationDate();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#isCreatedAfter(java.util.Date)
   */
  public boolean isCreatedAfter(Date date) {
    return creationCtx.isCreatedAfter(date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.Creatable#getCreator()
   */
  public User getCreator() {
    return creationCtx.getCreator();
  }

  /**
   * Sets the creation date and the user who created the content.
   * 
   * @param date
   *          the creation date
   * @param user
   *          the creator
   */
  public void setCreated(Date date, User user) {
    creationCtx.setCreated(user, date);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ResourceContent) {
      return language.equals(((ResourceContent) obj).getLanguage());
    }
    return false;
  }

  /**
   * Callback for subclasses that need to add additional information to the file
   * content representation. Implementations should append their data to the
   * <code>StringBuffer</code> and return it once they're done.
   * 
   * @param xml
   *          the string buffer
   * @return the modified string buffer
   */
  protected StringBuffer addXml(StringBuffer xml) {
    return xml;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.common.content.ResourceContent#toXml()
   */
  public String toXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<content language=\"").append(getLanguage().getIdentifier()).append("\">");
    buf.append(creationCtx.toXml());
    addXml(buf);
    buf.append("</content>");
    return buf.toString();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(language.toString().toLowerCase());
    buf.append(" content");
    return buf.toString();
  }

}
