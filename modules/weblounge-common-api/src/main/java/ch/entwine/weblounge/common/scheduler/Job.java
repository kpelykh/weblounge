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

package ch.entwine.weblounge.common.scheduler;

import ch.entwine.weblounge.common.site.Environment;

import java.util.Dictionary;

/**
 * A job object contains a worker implementation containing the logic to get
 * work done as well as information on when to trigger the job.
 */
public interface Job {

  /**
   * Sets the job identifier.
   * 
   * @param identifier
   *          the job identifier
   * @throws IllegalArgumentException
   *           if <code>identifier</code> is <code>null</code>
   */
  void setIdentifier(String identifier);

  /**
   * Returns the job identifier.
   * 
   * @return the job identifier
   */
  String getIdentifier();

  /**
   * Sets the current environment.
   * 
   * @param environment
   *          the environment
   */
  void setEnvironment(Environment environment);

  /**
   * Sets the job name.
   * 
   * @param name
   *          the name
   */
  void setName(String name);

  /**
   * Returns the job name.
   * 
   * @return the job name
   */
  String getName();

  /**
   * Sets the job implementation.
   * 
   * @param job
   *          the job
   */
  void setWorker(Class<JobWorker> job);

  /**
   * Returns the job implementation.
   * 
   * @return the job
   */
  Class<? extends JobWorker> getWorker();

  /**
   * Adds a new trigger to the list of triggers.
   * 
   * @param trigger
   *          the trigger to add
   */
  void setTrigger(JobTrigger trigger);

  /**
   * Returns the job trigger.
   * 
   * @return the trigger
   */
  JobTrigger getTrigger();

  /**
   * Returns the job context which in the beginning only contains the
   * configuration options found in the job definition.
   * 
   * @return the context
   */
  Dictionary<String, Object> getContext();

  /**
   * Returns an <code>XML</code> representation of the job, which will look
   * similar to the following example:
   * 
   * <pre>
   * &lt;job id=&quot;test&quot;&gt;
   *     &lt;name&gt;Job title&lt;/name&gt;
   *     &lt;description&gt;Job title&lt;/description&gt;
   *     &lt;class&gt;ch.entwine.weblounge.module.test.SampleCronJob&lt;/class&gt;
   *     &lt;schedule&gt;0 0 * * 1&lt;/schedule&gt;
   *     &lt;option&gt;
   *         &lt;name&gt;opt&lt;/name&gt;
   *         &lt;value&gt;optvalue&lt;/value&gt;
   *     &lt;/option&gt;
   * &lt;/job&gt;
   * </pre>
   * 
   * Use {@link #fromXml(org.w3c.dom.Node))} or
   * {@link #fromXml(org.w3c.dom.Node, javax.xml.xpath.XPath)} to create a
   * <code>QuartzJob</code> from the serialized output of this method.
   * 
   * @return the <code>XML</code> representation of the context
   * @see #fromXml(org.w3c.dom.Node)
   * @see #fromXml(org.w3c.dom.Node, javax.xml.xpath.XPath)
   */
  String toXml();

}
