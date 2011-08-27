/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2011 The Weblounge Team
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

package ch.entwine.weblounge.common.security;

/**
 * Utility methods around security.
 */
public final class SecurityUtils {

  /**
   * Private construtor to prevent instantiation.
   */
  private SecurityUtils() {
    // Nothing to do
  }

  /**
   * Returns <code>true</code> if <code>user</code> has role <code>role</code>
   * amongst its public credentials.
   * 
   * @param user
   *          the user
   * @param role
   *          the role
   * @return <code>true</code> if the user has the given role
   */
  public static boolean userHasRole(User user, Role role) {
    if (user == null)
      throw new IllegalArgumentException("User cannot be null");
    if (role == null)
      throw new IllegalArgumentException("Role cannot be null");
    for (Object o : user.getPublicCredentials(Role.class)) {
      Role masterRole = (Role) o;
      for (Role r : masterRole.getClosure()) {
        if (role.equals(r))
          return true;
      }
    }
    return false;
  }

  /**
   * Returns <code>true</code> if <code>user</code> has the role with identifier
   * <code>roleId</code> from the weblounge context
   * {@link SecurityConstants#SYSTEM_CONTEXT} amongst its public credentials.
   * 
   * @param user
   *          the user
   * @param role
   *          the weblounge role identifier
   * @return <code>true</code> if the user has the given role
   */
  public static boolean userHasRole(User user, String roleId) {
    if (user == null)
      throw new IllegalArgumentException("User cannot be null");
    if (roleId == null)
      throw new IllegalArgumentException("Role identifier cannot be null");
    for (Object o : user.getPublicCredentials(Role.class)) {
      Role masterRole = (Role) o;
      for (Role r : masterRole.getClosure()) {
        String ctx = r.getContext();
        String id = r.getIdentifier();
        if (ctx.equals(Security.SYSTEM_CONTEXT) && id.equals(roleId))
          return true;
      }
    }
    return false;
  }

}
