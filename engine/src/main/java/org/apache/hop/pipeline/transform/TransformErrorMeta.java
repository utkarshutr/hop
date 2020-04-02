/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.pipeline.transform;

import org.apache.hop.core.Const;
import org.apache.hop.core.changed.ChangedFlag;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.core.xml.XMLInterface;
import org.w3c.dom.Node;

import java.util.List;

/**
 * This class contains the metadata to handle proper error handling on a transform level.
 *
 * @author Matt
 */
public class TransformErrorMeta extends ChangedFlag implements XMLInterface, Cloneable {
  public static final String XML_ERROR_TAG = "error";
  public static final String XML_SOURCE_TRANSFORM_TAG = "source_transform";
  public static final String XML_TARGET_TRANSFORM_TAG = "target_transform";

  /**
   * The source transform that can send the error rows
   */
  private TransformMeta sourceTransform;

  /**
   * The target transform to send the error rows to
   */
  private TransformMeta targetTransform;

  /**
   * Is the error handling enabled?
   */
  private boolean enabled;

  /**
   * the name of the field value to contain the number of errors (null or empty means it's not needed)
   */
  private String nrErrorsValuename;

  /**
   * the name of the field value to contain the error description(s) (null or empty means it's not needed)
   */
  private String errorDescriptionsValuename;

  /**
   * the name of the field value to contain the fields for which the error(s) occured (null or empty means it's not
   * needed)
   */
  private String errorFieldsValuename;

  /**
   * the name of the field value to contain the error code(s) (null or empty means it's not needed)
   */
  private String errorCodesValuename;

  /**
   * The maximum number of errors allowed before we stop processing with a hard error
   */
  private String maxErrors = "";

  /**
   * The maximum percent of errors allowed before we stop processing with a hard error
   */
  private String maxPercentErrors = "";

  /**
   * The minimum number of rows to read before the percentage evaluation takes place
   */
  private String minPercentRows = "";

  private VariableSpace variables;

  /**
   * Create a new transform error handling metadata object
   *
   * @param sourceTransform The source transform that can send the error rows
   */
  public TransformErrorMeta( VariableSpace space, TransformMeta sourceTransform ) {
    this.sourceTransform = sourceTransform;
    this.enabled = false;
    this.variables = space;
  }

  /**
   * Create a new transform error handling metadata object
   *
   * @param sourceTransform The source transform that can send the error rows
   * @param targetTransform The target transform to send the error rows to
   */
  public TransformErrorMeta( VariableSpace space, TransformMeta sourceTransform, TransformMeta targetTransform ) {
    this.sourceTransform = sourceTransform;
    this.targetTransform = targetTransform;
    this.enabled = false;
    this.variables = space;
  }

  /**
   * Create a new transform error handling metadata object
   *
   * @param sourceTransform                 The source transform that can send the error rows
   * @param targetTransform                 The target transform to send the error rows to
   * @param nrErrorsValuename          the name of the field value to contain the number of errors (null or empty means it's not needed)
   * @param errorDescriptionsValuename the name of the field value to contain the error description(s) (null or empty means it's not needed)
   * @param errorFieldsValuename       the name of the field value to contain the fields for which the error(s) occured (null or empty means it's
   *                                   not needed)
   * @param errorCodesValuename        the name of the field value to contain the error code(s) (null or empty means it's not needed)
   */
  public TransformErrorMeta( VariableSpace space, TransformMeta sourceTransform, TransformMeta targetTransform, String nrErrorsValuename,
                             String errorDescriptionsValuename, String errorFieldsValuename, String errorCodesValuename ) {
    this.sourceTransform = sourceTransform;
    this.targetTransform = targetTransform;
    this.enabled = false;
    this.nrErrorsValuename = nrErrorsValuename;
    this.errorDescriptionsValuename = errorDescriptionsValuename;
    this.errorFieldsValuename = errorFieldsValuename;
    this.errorCodesValuename = errorCodesValuename;
    this.variables = space;
  }

  @Override
  public TransformErrorMeta clone() {
    try {
      return (TransformErrorMeta) super.clone();
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  @Override
  public String getXML() {
    StringBuilder xml = new StringBuilder( 300 );

    xml.append( "      " ).append( XMLHandler.openTag( TransformErrorMeta.XML_ERROR_TAG ) ).append( Const.CR );
    xml.append( "        " ).append(
      XMLHandler.addTagValue( TransformErrorMeta.XML_SOURCE_TRANSFORM_TAG, sourceTransform != null ? sourceTransform.getName() : "" ) );
    xml.append( "        " ).append(
      XMLHandler.addTagValue( TransformErrorMeta.XML_TARGET_TRANSFORM_TAG, targetTransform != null ? targetTransform.getName() : "" ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "is_enabled", enabled ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "nr_valuename", nrErrorsValuename ) );
    xml
      .append( "        " ).append(
      XMLHandler.addTagValue( "descriptions_valuename", errorDescriptionsValuename ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "fields_valuename", errorFieldsValuename ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "codes_valuename", errorCodesValuename ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "max_errors", maxErrors ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "max_pct_errors", maxPercentErrors ) );
    xml.append( "        " ).append( XMLHandler.addTagValue( "min_pct_rows", minPercentRows ) );
    xml.append( "      " ).append( XMLHandler.closeTag( TransformErrorMeta.XML_ERROR_TAG ) ).append( Const.CR );

    return xml.toString();
  }

  public TransformErrorMeta( VariableSpace variables, Node node, List<TransformMeta> transforms ) {
    this.variables = variables;

    sourceTransform = TransformMeta.findTransform( transforms, XMLHandler.getTagValue( node, TransformErrorMeta.XML_SOURCE_TRANSFORM_TAG ) );
    targetTransform = TransformMeta.findTransform( transforms, XMLHandler.getTagValue( node, TransformErrorMeta.XML_TARGET_TRANSFORM_TAG ) );
    enabled = "Y".equals( XMLHandler.getTagValue( node, "is_enabled" ) );
    nrErrorsValuename = XMLHandler.getTagValue( node, "nr_valuename" );
    errorDescriptionsValuename = XMLHandler.getTagValue( node, "descriptions_valuename" );
    errorFieldsValuename = XMLHandler.getTagValue( node, "fields_valuename" );
    errorCodesValuename = XMLHandler.getTagValue( node, "codes_valuename" );
    maxErrors = XMLHandler.getTagValue( node, "max_errors" );
    maxPercentErrors = XMLHandler.getTagValue( node, "max_pct_errors" );
    minPercentRows = XMLHandler.getTagValue( node, "min_pct_rows" );
  }

  /**
   * @return the error codes valuename
   */
  public String getErrorCodesValuename() {
    return errorCodesValuename;
  }

  /**
   * @param errorCodesValuename the error codes valuename to set
   */
  public void setErrorCodesValuename( String errorCodesValuename ) {
    this.errorCodesValuename = errorCodesValuename;
  }

  /**
   * @return the error descriptions valuename
   */
  public String getErrorDescriptionsValuename() {
    return errorDescriptionsValuename;
  }

  /**
   * @param errorDescriptionsValuename the error descriptions valuename to set
   */
  public void setErrorDescriptionsValuename( String errorDescriptionsValuename ) {
    this.errorDescriptionsValuename = errorDescriptionsValuename;
  }

  /**
   * @return the error fields valuename
   */
  public String getErrorFieldsValuename() {
    return errorFieldsValuename;
  }

  /**
   * @param errorFieldsValuename the error fields valuename to set
   */
  public void setErrorFieldsValuename( String errorFieldsValuename ) {
    this.errorFieldsValuename = errorFieldsValuename;
  }

  /**
   * @return the nr errors valuename
   */
  public String getNrErrorsValuename() {
    return nrErrorsValuename;
  }

  /**
   * @param nrErrorsValuename the nr errors valuename to set
   */
  public void setNrErrorsValuename( String nrErrorsValuename ) {
    this.nrErrorsValuename = nrErrorsValuename;
  }

  /**
   * @return the target transform
   */
  public TransformMeta getTargetTransform() {
    return targetTransform;
  }

  /**
   * @param targetTransform the target transform to set
   */
  public void setTargetTransform( TransformMeta targetTransform ) {
    this.targetTransform = targetTransform;
  }

  /**
   * @return The source transform can send the error rows
   */
  public TransformMeta getSourceTransform() {
    return sourceTransform;
  }

  /**
   * @param sourceTransform The source transform can send the error rows
   */
  public void setSourceTransform( TransformMeta sourceTransform ) {
    this.sourceTransform = sourceTransform;
  }

  /**
   * @return the enabled flag: Is the error handling enabled?
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * @param enabled the enabled flag to set: Is the error handling enabled?
   */
  public void setEnabled( boolean enabled ) {
    this.enabled = enabled;
  }

  public RowMetaInterface getErrorFields() {
    return getErrorRowMeta( 0L, null, null, null );
  }

  public RowMetaInterface getErrorRowMeta( long nrErrors, String errorDescriptions, String fieldNames,
                                           String errorCodes ) {
    RowMetaInterface row = new RowMeta();

    String nrErr = variables.environmentSubstitute( getNrErrorsValuename() );
    if ( !Utils.isEmpty( nrErr ) ) {
      ValueMetaInterface v = new ValueMetaInteger( nrErr );
      v.setLength( 3 );
      row.addValueMeta( v );
    }
    String errDesc = variables.environmentSubstitute( getErrorDescriptionsValuename() );
    if ( !Utils.isEmpty( errDesc ) ) {
      ValueMetaInterface v = new ValueMetaString( errDesc );
      row.addValueMeta( v );
    }
    String errFields = variables.environmentSubstitute( getErrorFieldsValuename() );
    if ( !Utils.isEmpty( errFields ) ) {
      ValueMetaInterface v = new ValueMetaString( errFields );
      row.addValueMeta( v );
    }
    String errCodes = variables.environmentSubstitute( getErrorCodesValuename() );
    if ( !Utils.isEmpty( errCodes ) ) {
      ValueMetaInterface v = new ValueMetaString( errCodes );
      row.addValueMeta( v );
    }

    return row;
  }

  public void addErrorRowData( Object[] row, int startIndex, long nrErrors, String errorDescriptions,
                               String fieldNames, String errorCodes ) {
    int index = startIndex;

    String nrErr = variables.environmentSubstitute( getNrErrorsValuename() );
    if ( !Utils.isEmpty( nrErr ) ) {
      row[ index ] = new Long( nrErrors );
      index++;
    }
    String errDesc = variables.environmentSubstitute( getErrorDescriptionsValuename() );
    if ( !Utils.isEmpty( errDesc ) ) {
      row[ index ] = errorDescriptions;
      index++;
    }
    String errFields = variables.environmentSubstitute( getErrorFieldsValuename() );
    if ( !Utils.isEmpty( errFields ) ) {
      row[ index ] = fieldNames;
      index++;
    }
    String errCodes = variables.environmentSubstitute( getErrorCodesValuename() );
    if ( !Utils.isEmpty( errCodes ) ) {
      row[ index ] = errorCodes;
      index++;
    }
  }

  /**
   * @return the maxErrors
   */
  public String getMaxErrors() {
    return maxErrors;
  }

  /**
   * @param maxErrors the maxErrors to set
   */
  public void setMaxErrors( String maxErrors ) {
    this.maxErrors = maxErrors;
  }

  /**
   * @return the maxPercentErrors
   */
  public String getMaxPercentErrors() {
    return maxPercentErrors;
  }

  /**
   * @param maxPercentErrors the maxPercentErrors to set
   */
  public void setMaxPercentErrors( String maxPercentErrors ) {
    this.maxPercentErrors = maxPercentErrors;
  }

  /**
   * @return the minRowsForPercent
   */
  public String getMinPercentRows() {
    return minPercentRows;
  }

  /**
   * @param minRowsForPercent the minRowsForPercent to set
   */
  public void setMinPercentRows( String minRowsForPercent ) {
    this.minPercentRows = minRowsForPercent;
  }
}