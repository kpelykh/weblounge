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

package ch.o2it.weblounge.common.impl.scheduler;

import ch.o2it.weblounge.common.impl.util.xml.XPathHelper;
import ch.o2it.weblounge.common.scheduler.Job;
import ch.o2it.weblounge.common.scheduler.JobTrigger;
import ch.o2it.weblounge.common.scheduler.JobWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

/**
 * Base implementation for jobs.
 */
public final class QuartzJob implements Job {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(QuartzJob.class);
  
  /** The job identifier */
  protected String identifier = null;

  /** The job name */
  protected String name = null;

  /** The actual job implementation */
  protected Class<? extends JobWorker> worker = null;

  /** Job trigger */
  protected JobTrigger trigger = null;

  /** Job context map */
  protected Dictionary<String, Serializable> ctx = null;

  /**
   * Creates a new job.
   * 
   * @param identifier
   *          job identifier
   * @param worker
   *          the job implementation
   * @param trigger
   *          the job trigger
   */
  public QuartzJob(String identifier, Class<? extends JobWorker> worker,
      JobTrigger trigger) {
    this(identifier, worker, null, trigger);
  }

  /**
   * Creates a new job with an initial job context. That context will be passed
   * every time the {@link #execute(Dictionary)} method is triggered.
   * 
   * @param identifier
   *          job identifier
   * @param worker
   *          the job implementation
   * @param context
   *          the job context
   * @param trigger
   *          the job trigger
   */
  public QuartzJob(String identifier, Class<? extends JobWorker> worker,
      Dictionary<String, Serializable> context, JobTrigger trigger) {
    if (identifier == null)
      throw new IllegalArgumentException("Job identifier must not be null");
    if (worker == null)
      throw new IllegalArgumentException("Worker must not be null");
    if (trigger == null)
      throw new IllegalArgumentException("Trigger must not be null");
    this.identifier = identifier;
    this.worker = worker;
    this.trigger = trigger;
    this.ctx = context;
    if (this.ctx == null)
      ctx = new Hashtable<String, Serializable>();
  }

  /**
   * Sets the job identifier.
   * 
   * @param identifier
   *          the job identifier
   * @throws IllegalArgumentException
   *           if <code>identifier</code> is <code>null</code>
   */
  public void setIdentifier(String identifier) {
    if (identifier == null)
      throw new IllegalArgumentException("Job identifier must not be null");
    this.identifier = identifier;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#getIdentifier()
   */
  public String getIdentifier() {
    return identifier;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#setName(java.lang.String)
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#setWorker(java.lang.Class)
   */
  public void setWorker(Class<JobWorker> job) {
    if (worker == null)
      throw new IllegalArgumentException("Job implementation must not be null");
    this.worker = job;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#getWorker()
   */
  public Class<? extends JobWorker> getWorker() {
    return worker;
  }

  /**
   * Returns the job context.
   * 
   * @return the context
   */
  public Dictionary<String, Serializable> getContext() {
    return ctx;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#setTrigger(ch.o2it.weblounge.common.scheduler.JobTrigger)
   */
  public void setTrigger(JobTrigger trigger) {
    if (trigger == null)
      throw new IllegalArgumentException("Job trigger must not be null");
    this.trigger = trigger;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#getTrigger()
   */
  public JobTrigger getTrigger() {
    return trigger;
  }

  /**
   * Returns the string representation of this job, which is equal to the value
   * returned by <code>getName()</code>.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(identifier);
    buf.append(" [schedule=");
    buf.append(trigger);
    buf.append("; class=");
    buf.append(worker.getClass().getName());
    buf.append("]");
    return buf.toString();
  }

  /**
   * Initializes this job from an XML node that was generated using
   * {@link #toXml()}.
   * <p>
   * To speed things up, you might consider using the second signature that uses
   * an existing <code>XPath</code> instance instead of creating a new one.
   * 
   * @param context
   *          the job node
   * @throws IllegalStateException
   *           if the job cannot be parsed
   * @see #fromXml(Node, XPath)
   * @see #toXml()
   */
  public static Job fromXml(Node context) throws IllegalStateException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return fromXml(context, xpath);
  }

  /**
   * Initializes this job from an XML node that was generated using
   * {@link #toXml()}.
   * 
   * @param context
   *          the job node
   * @param xpathProcessor
   *          xpath processor to use
   * @throws IllegalStateException
   *           if the job cannot be parsed
   * @see #toXml()
   */
  @SuppressWarnings("unchecked")
  public static Job fromXml(Node config, XPath xPathProcessor)
      throws IllegalStateException {

    CronJobTrigger jobTrigger = null;
    Dictionary<String, Serializable> ctx = new Hashtable<String, Serializable>();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    
    // Main attributes
    String identifier = XPathHelper.valueOf(config, "@id", xPathProcessor);
    String name = XPathHelper.valueOf(config, "name", xPathProcessor);

    // Implementation class
    String className = XPathHelper.valueOf(config, "class", xPathProcessor);
    Class<JobWorker> clazz;
    try {
      clazz = (Class<JobWorker>) classLoader.loadClass(className);
    } catch (ClassNotFoundException e) {
      logger.error("Error the implementation for job '{}'", identifier);
      throw new IllegalStateException();
    }

    // Read execution schedule
    String schedule = XPathHelper.valueOf(config, "schedule", xPathProcessor);
    if (schedule == null)
      throw new IllegalStateException("No schedule has been defined for job '" + identifier + "'");
    jobTrigger = new CronJobTrigger(schedule);

    // Read options
    NodeList nodes = XPathHelper.selectList(config, "options/option", xPathProcessor);
    for (int i = 0; i < nodes.getLength(); i++) {
      Node option = nodes.item(i);
      String optionName = XPathHelper.valueOf(option, "name", xPathProcessor);
      String value = XPathHelper.valueOf(option, "value", xPathProcessor);
      ctx.put(optionName, value);
    }

    // Did we find something?

    QuartzJob job = new QuartzJob(identifier, clazz, ctx, jobTrigger);
    job.setName(name);
    return job;
  }

  /**
   * {@inheritDoc}
   *
   * @see ch.o2it.weblounge.common.scheduler.Job#toXml()
   */
  public String toXml() {
    StringBuffer b = new StringBuffer();
    b.append("<job id=\"");
    b.append(identifier);
    b.append("\">");

    // Name
    if (name != null) {
      b.append("<name>");
      b.append(name);
      b.append("</name>");
    }

    // Class
    b.append("<class>");
    b.append(worker.getName());
    b.append("</class>");

    // Schedule
    if (!(trigger instanceof CronJobTrigger))
      throw new IllegalStateException("Cannot serialize job trigger of type " + trigger.getClass().getName());
    b.append("<schedule>");
    b.append(((CronJobTrigger) trigger).getCronExpression());
    b.append("</schedule>");

    // Options
    Enumeration<String> e = ctx.keys();
    if (e.hasMoreElements()) {
      b.append("<options>");
      while (e.hasMoreElements()) {
        String key = e.nextElement();
        b.append("<option>");
        b.append("<name>");
        b.append(key);
        b.append("</name>");
        b.append("<value>");
        b.append(ctx.get(key));
        b.append("</value>");
        b.append("</option>");
      }
      b.append("</options>");
    }

    b.append("</job>");
    return b.toString();
  }

}
