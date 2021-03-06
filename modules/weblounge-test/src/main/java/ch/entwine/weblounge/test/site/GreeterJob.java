/*
 *  Weblounge: Web Content Management System
 *  Copyright (c) 2003 - 2013 The Weblounge Team
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

package ch.entwine.weblounge.test.site;

import ch.entwine.weblounge.test.util.TestSiteUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Test job that will print a friendly greeting to <code>System.out</code>.
 */
@SuppressWarnings("unchecked")
public class GreeterJob implements Runnable {

  /** Logging facility */
  protected static final Logger logger = LoggerFactory.getLogger(GreeterJob.class);

  /** Hello world in many languages */
  protected static Map.Entry<String, String>[] greetings = null;

  static {
    Map<String, String> hellos = TestSiteUtils.loadGreetings();
    greetings = hellos.entrySet().toArray(new Map.Entry[hellos.size()]);
  }

  @Override
  public void run() {
    int index = (int) ((greetings.length - 1) * Math.random());
    Map.Entry<String, String> entry = greetings[index];
    try {
      logger.info(new String(entry.getValue().getBytes("utf-8"), "utf-8") + " (" + entry.getKey() + ")");
    } catch (UnsupportedEncodingException e) {
      logger.warn("Error decoding greeting for {}, :", entry.getKey(), e.getMessage());
    }
  }

}
