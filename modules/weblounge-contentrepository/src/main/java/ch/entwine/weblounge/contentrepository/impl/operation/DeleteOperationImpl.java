/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2012 The Weblounge Team
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

package ch.entwine.weblounge.contentrepository.impl.operation;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.repository.DeleteOperation;
import ch.entwine.weblounge.common.repository.ReferentialIntegrityException;
import ch.entwine.weblounge.common.repository.WritableContentRepository;

import java.io.IOException;

/**
 * This operation implements a delete of the given resource.
 */
public final class DeleteOperationImpl extends AbstractContentRepositoryOperation<Boolean> implements DeleteOperation {

  /** True if all versions of this resource should be removed */
  private boolean allVersions = false;

  /** The resource that is about to be deleted */
  private Resource<?> resource = null;

  /**
   * Creates a new delete operation for the given resource.
   * 
   * @param resource
   *          the resource
   * @param allVersions
   *          <code>true</code> if all versions should be removed
   */
  public DeleteOperationImpl(Resource<?> resource, boolean allVersions) {
    this.resource = resource;
    this.allVersions = allVersions;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.ContentRepositoryResourceOperation#getResourceURI()
   */
  public ResourceURI getResourceURI() {
    return resource.getURI();
  }

  /**
   * Returns the resource that is to be deleted.
   * 
   * @return the resource
   */
  public Resource<?> getResource() {
    return resource;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This implementation return <code>null</code> if the resource's uri equals
   * that of this operation.
   * 
   * @see ch.entwine.weblounge.common.repository.ContentRepositoryResourceOperation#apply(ResourceURI,
   *      Resource)
   */
  public <C extends ResourceContent, R extends Resource<C>> R apply(
      ResourceURI uri, R resource) {

    if (resource == null)
      return null;

    if (allVersions && ResourceUtils.equalsByIdOrPath(this.resource.getURI(), resource.getURI()))
      return null;
    else if (!allVersions && ResourceUtils.equalsByIdOrPathAndVersion(this.resource.getURI(), resource.getURI()))
      return null;

    return resource;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.DeleteOperation#allVersions()
   */
  public boolean allVersions() {
    return allVersions;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.repository.ContentRepositoryOperation#execute(ch.entwine.weblounge.common.repository.WritableContentRepository)
   */
  @Override
  protected Boolean run(WritableContentRepository repository)
      throws ContentRepositoryException, IOException,
      ReferentialIntegrityException {
    return repository.delete(resource.getURI(), allVersions);
  }

}
