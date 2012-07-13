/*
 * Copyright 2012 by swiss unihockey
 * Berne, Switzerland (CH)
 * All rights reserved.
 *
 * http://www.swissunihockey.ch
 * 
 * This software is confidential and proprietary information ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into.
 */

package ch.entwine.weblounge.contentrepository.impl.s3;

import ch.entwine.weblounge.common.content.Resource;
import ch.entwine.weblounge.common.content.ResourceContent;
import ch.entwine.weblounge.common.content.ResourceURI;
import ch.entwine.weblounge.common.content.ResourceUtils;
import ch.entwine.weblounge.common.content.repository.ContentRepositoryException;
import ch.entwine.weblounge.common.content.repository.IndexOperation;
import ch.entwine.weblounge.common.language.Language;
import ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository;
import ch.entwine.weblounge.contentrepository.impl.index.ContentRepositoryIndex;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.TransferManager;

import org.apache.commons.io.FilenameUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * TODO: Comment S3ContentRepository
 */
public class S3ContentRepository extends AbstractWritableContentRepository implements ManagedService {

  /** The logging facility */
  private static final Logger logger = LoggerFactory.getLogger(S3ContentRepository.class);

  /** The client for accessing the Amazon S3 web service */
  private AmazonS3Client s3Client;

  /** The transfer manager for accessing the Amazon S3 web service */
  private TransferManager tx;

  /** Name of the Amazon S3 bucket to use */
  private String bucketName;

  /** The repository type */
  public static final String TYPE = "ch.entwine.weblounge.contentrepository.s3";

  public static final String OBJ_KEY_SEPARATOR = "/";

  /**
   * Creates a new instance of the AWS S3 content repository.
   */
  public S3ContentRepository() {
    super(TYPE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  public void updated(Dictionary properties) throws ConfigurationException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#index()
   */
  public void index() throws ContentRepositoryException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.common.content.repository.WritableContentRepository#indexAsynchronously()
   */
  public IndexOperation indexAsynchronously() throws ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResource(ch.entwine.weblounge.common.content.Resource)
   */
  @Override
  protected <T extends ResourceContent, R extends Resource<T>> R storeResource(
      R resource) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#storeResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent,
   *      java.io.InputStream)
   */
  @Override
  protected <T extends ResourceContent> T storeResourceContent(ResourceURI uri,
      T content, InputStream is) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResource(ch.entwine.weblounge.common.content.ResourceURI,
   *      long[])
   */
  @Override
  protected void deleteResource(ResourceURI uri, long[] revisions)
      throws IOException {
    String prefix = uriToKeyPrefix(uri);

    // Collect all keys of objects to delete
    List<DeleteObjectsRequest.KeyVersion> keysToDelete = new ArrayList<DeleteObjectsRequest.KeyVersion>();
    for (long r : revisions) {
      String prefixWithVersion = prefix.concat(OBJ_KEY_SEPARATOR).concat(String.valueOf(r));
      List<S3ObjectSummary> objSummaries = s3Client.listObjects(bucketName, prefixWithVersion).getObjectSummaries();
      for (S3ObjectSummary summary : objSummaries) {
        keysToDelete.add(new DeleteObjectsRequest.KeyVersion(summary.getKey()));
      }
    }

    // Create delete request and send it to the S3 web service
    DeleteObjectsRequest delRequest = new DeleteObjectsRequest(bucketName);
    delRequest.setKeys(keysToDelete);
    try {
      s3Client.deleteObjects(delRequest);
    } catch (MultiObjectDeleteException e) {
      throw new IOException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractWritableContentRepository#deleteResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.content.ResourceContent)
   */
  @Override
  protected <T extends ResourceContent> void deleteResourceContent(
      ResourceURI uri, T content) throws IOException {
    try {
      s3Client.deleteObject(bucketName, uriToKey(uri, content));
    } catch (AmazonServiceException e) {
      throw new IOException(e);
    } catch (AmazonClientException e) {
      throw new IOException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#openStreamToResource(ch.entwine.weblounge.common.content.ResourceURI)
   */
  @Override
  protected InputStream openStreamToResource(ResourceURI uri)
      throws IOException {
    S3Object obj;
    try {
      obj = s3Client.getObject(bucketName, uriToKey(uri));
    } catch (AmazonServiceException e) {
      throw new IOException(e);
    } catch (AmazonClientException e) {
      throw new IOException(e);
    }
    return obj.getObjectContent();
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#openStreamToResourceContent(ch.entwine.weblounge.common.content.ResourceURI,
   *      ch.entwine.weblounge.common.language.Language)
   */
  @Override
  protected InputStream openStreamToResourceContent(ResourceURI uri,
      Language language) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see ch.entwine.weblounge.contentrepository.impl.AbstractContentRepository#loadIndex()
   */
  @Override
  protected ContentRepositoryIndex loadIndex() throws IOException,
      ContentRepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  protected String uriToKeyPrefix(ResourceURI uri)
      throws IllegalArgumentException, IOException {
    StringBuilder key = new StringBuilder();
    if (uri.getType() == null)
      throw new IllegalArgumentException("Resource uri has no type");
    key.append(uri.getType()).append("s").append(OBJ_KEY_SEPARATOR);
    String id = null;
    if (uri.getIdentifier() != null) {
      id = uri.getIdentifier();
    } else {
      id = index.getIdentifier(uri);
      if (id == null) {
        logger.debug("Uri '{}' is not part of the repository index", uri);
        return null;
      }
    }
    if (uri.getVersion() < 0) {
      logger.warn("Resource {} has no version");
    }

    // Append resource id
    key.append(id);

    return key.toString();
  }

  protected String uriToKeyPrefixWithVersion(ResourceURI uri)
      throws IllegalArgumentException, IOException {
    StringBuilder key = new StringBuilder(uriToKeyPrefix(uri));

    key.append(OBJ_KEY_SEPARATOR);
    key.append(uri.getVersion());

    return key.toString();
  }

  protected String uriToKey(ResourceURI uri) throws IllegalArgumentException,
      IOException {
    String key = uriToKeyPrefixWithVersion(uri);
    // Add the document name
    key.concat(OBJ_KEY_SEPARATOR).concat(ResourceUtils.getDocument(Resource.LIVE));
    return key;
  }

  protected String uriToKey(ResourceURI uri, ResourceContent content)
      throws IllegalArgumentException, IOException {
    // Construct the filename
    String fileName = content.getLanguage().getIdentifier();
    String fileExtension = FilenameUtils.getExtension(content.getFilename());
    if (!"".equals(fileExtension)) {
      fileName += "." + fileExtension;
    }

    return uriToKeyPrefixWithVersion(uri).concat(OBJ_KEY_SEPARATOR).concat(fileName);
  }

}
