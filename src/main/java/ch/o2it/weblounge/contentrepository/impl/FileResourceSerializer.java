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

package ch.o2it.weblounge.contentrepository.impl;

import ch.o2it.weblounge.common.content.Resource;
import ch.o2it.weblounge.common.content.file.FileContent;
import ch.o2it.weblounge.common.content.file.FileResource;
import ch.o2it.weblounge.common.impl.content.file.FileResourceReader;
import ch.o2it.weblounge.contentrepository.impl.index.solr.FileResourceInputDocument;

import org.apache.solr.common.SolrInputDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Implementation of a serializer for file resources.
 */
public class FileResourceSerializer extends AbstractResourceSerializer<FileContent, FileResource> {

  /**
   * Creates a new file resource serializer.
   */
  public FileResourceSerializer() {
    super(FileResource.TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.o2it.weblounge.contentrepository.impl.AbstractResourceSerializer#createNewReader()
   */
  @Override
  protected FileResourceReader createNewReader()
      throws ParserConfigurationException, SAXException {
    return new FileResourceReader();
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.contentrepository.ResourceSerializer#getInputDocument(ch.o2it.weblounge.common.content.Resource)
   */
  public SolrInputDocument getInputDocument(Resource<?> resource) {
    return new FileResourceInputDocument((FileResource)resource);
  }

}
