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

package ch.o2it.weblounge.common.security;

import ch.o2it.weblounge.common.user.User;

/**
 * Listener interface for those who are interested in changes of object owners
 * and permissions.
 */
public interface SecurityListener {

  /**
   * Notifies the listener about a new object owner.
   * 
   * @param source
   *          the the secured object
   * @param newOwner
   *          the new object owner
   * @param oldOwner
   *          the former object owner
   */
  void ownerChanged(Securable source, User newOwner, User oldOwner);

  /**
   * Notifies the listener about a permission change on object
   * <code>source</code>.
   * 
   * @param source
   *          the the secured object
   * @param p
   *          the new permission
   */
  void permissionChanged(Securable source, Permission p);

}