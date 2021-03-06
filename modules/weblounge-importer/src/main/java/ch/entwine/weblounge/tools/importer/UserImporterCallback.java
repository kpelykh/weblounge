/*
 * PageImporterCallback.java
 *
 * Copyright 2007 by Entwine
 * Zurich, Switzerland (CH)
 * All rights reserved.
 *
 * This software is confidential and proprietary information ("Confidential
 * Information").  You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license
 * agreement you entered into.
 */

package ch.entwine.weblounge.tools.importer;

import ch.entwine.weblounge.common.impl.util.xml.XPathHelper;

import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class UserImporterCallback extends AbstractImporterCallback {

  /** The users */
  Collection<String> users = new ArrayList<String>();

  /**
   * Creates a new callback for page imports.
   * 
   * @param src
   *          the source root directory
   * @param dest
   *          the destination root directory
   */
  UserImporterCallback(File src, File dest, Map<String, Collection<?>> clipboard) {
    super(src, dest);
    clipboard.put("users", users);
  }

  public boolean folderImported(File f) {
    String path = f.getAbsolutePath();
    path = path.substring(srcDir.getAbsolutePath().length()).toLowerCase();
    File dest = new File(destDir, path);
    if (!dest.exists())
      dest.mkdirs();
    return true;
  }

  /**
   * This method is called if a page file has been found.
   * 
   * @see ch.entwine.weblounge.tools.importer.AbstractImporterCallback#fileImported(java.io.File)
   */
  @Override
  public boolean fileImported(File f) throws Exception {
    try {
      String path = f.getAbsolutePath();
      path = path.substring(srcDir.getAbsolutePath().length()).toLowerCase();
      File dest = new File(destDir, path);
      if (!dest.exists())
        dest.createNewFile();
      if ("user.xml".equals(dest.getName())) {
        copy(f, dest);
        Node root = getXmlRoot(f);
        String id = XPathHelper.valueOf(root, "/user/@id");
        String firstname = XPathHelper.valueOf(root, "//firstname");
        String lastname = XPathHelper.valueOf(root, "//lastname");
        users.add(id);
        System.out.println("Imported " + firstname + " " + lastname);
      }
      return true;
    } catch (IOException e) {
      System.err.println("Error creating target file for " + f + ": " + e.getMessage());
      return false;
    }
  }

}