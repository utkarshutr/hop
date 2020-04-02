/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.pipeline.transforms.switchcase;

import org.apache.hop.core.RowSet;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaFactory;
import org.apache.hop.core.util.Utils;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformDataInterface;
import org.apache.hop.pipeline.transform.TransformIOMetaInterface;
import org.apache.hop.pipeline.transform.TransformInterface;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.TransformMetaInterface;
import org.apache.hop.pipeline.transform.errorhandling.StreamInterface;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Filters input rows base on conditions.
 *
 * @author Matt
 * @since 16-apr-2003, 07-nov-2004 (rewrite)
 */
public class SwitchCase extends BaseTransform implements TransformInterface {
  private static Class<?> PKG = SwitchCaseMeta.class; // for i18n purposes, needed by Translator!!

  private SwitchCaseMeta meta;
  private SwitchCaseData data;

  public SwitchCase( TransformMeta transformMeta, TransformDataInterface transformDataInterface, int copyNr, PipelineMeta pipelineMeta,
                     Pipeline pipeline ) {
    super( transformMeta, transformDataInterface, copyNr, pipelineMeta, pipeline );
  }

  public boolean processRow( TransformMetaInterface smi, TransformDataInterface sdi ) throws HopException {
    meta = (SwitchCaseMeta) smi;
    data = (SwitchCaseData) sdi;

    Object[] r = getRow(); // Get next usable row from input rowset(s)!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      // map input to output streams
      createOutputValueMapping();
    }

    // We already know the target values, but we need to make sure that the input data type is the same as the specified
    // one.
    // Perhaps there is some conversion needed.
    //
    Object lookupData = data.valueMeta.convertData( data.inputValueMeta, r[ data.fieldIndex ] );

    // could not use byte[] as key in Maps, so we need to convert it to his specific hashCode for comparisons
    lookupData = prepareObjectType( lookupData );

    // Determine the output set of rowset to use...
    Set<RowSet> rowSetSet = ( data.valueMeta.isNull( lookupData ) ) ? data.nullRowSetSet : data.outputMap.get( lookupData );

    // If the rowset is still not found (unspecified key value, we drop down to the default option
    // For now: send it to the default transform...
    if ( rowSetSet == null ) {
      rowSetSet = data.defaultRowSetSet;
    }

    for ( RowSet rowSet : rowSetSet ) {
      putRowTo( data.outputRowMeta, r, rowSet );
    }

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "SwitchCase.Log.LineNumber" ) + getLinesRead() );
      }
    }

    return true;
  }

  /**
   * @see TransformInterface#init(org.apache.hop.pipeline.transform.TransformMetaInterface, org.apache.hop.pipeline.transform.TransformDataInterface)
   */
  public boolean init( TransformMetaInterface smi, TransformDataInterface sdi ) {
    meta = (SwitchCaseMeta) smi;
    data = (SwitchCaseData) sdi;

    if ( !super.init( smi, sdi ) ) {
      return false;
    }
    data.outputMap = meta.isContains() ? new ContainsKeyToRowSetMap() : new KeyToRowSetMap();

    if ( Utils.isEmpty( meta.getFieldname() ) ) {
      logError( BaseMessages.getString( PKG, "SwitchCase.Log.NoFieldSpecifiedToSwitchWith" ) );
      return false;
    }

    try {
      data.valueMeta = ValueMetaFactory.createValueMeta( meta.getFieldname(), meta.getCaseValueType() );
      data.valueMeta.setConversionMask( meta.getCaseValueFormat() );
      data.valueMeta.setGroupingSymbol( meta.getCaseValueGroup() );
      data.valueMeta.setDecimalSymbol( meta.getCaseValueDecimal() );
      data.stringValueMeta = ValueMetaFactory.cloneValueMeta( data.valueMeta, ValueMetaInterface.TYPE_STRING );
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "SwitchCase.Log.UnexpectedError", e ) );
    }

    return true;
  }

  /**
   * This will prepare transform for execution:
   * <ol>
   * <li>will copy input row meta info, fields info, etc. transform related info
   * <li>will get transform IO meta info and discover target streams for target output transforms
   * <li>for every target output find output rowset and expected value.
   * <li>for every discovered output rowset put it as a key-value: 'expected value'-'output rowSet'. If expected value
   * is null - put output rowset to special 'null set' (avoid usage of null as a map keys)
   * <li>Discover default row set. We expect only one default rowset, even if technically can have many. *
   * </ol>
   *
   * @throws HopException if something goes wrong during transform preparation.
   */
  void createOutputValueMapping() throws HopException {
    data.outputRowMeta = getInputRowMeta().clone();
    meta.getFields( getInputRowMeta(), getTransformName(), null, null, this, metaStore );

    data.fieldIndex = getInputRowMeta().indexOfValue( meta.getFieldname() );
    if ( data.fieldIndex < 0 ) {
      throw new HopException( BaseMessages.getString( PKG, "SwitchCase.Exception.UnableToFindFieldName", meta
        .getFieldname() ) );
    }

    data.inputValueMeta = getInputRowMeta().getValueMeta( data.fieldIndex );

    try {
      TransformIOMetaInterface ioMeta = meta.getTransformIOMeta();

      // There is one or many case target for each target stream.
      // The ioMeta object has one more target stream for the default target though.
      //
      List<StreamInterface> targetStreams = ioMeta.getTargetStreams();
      for ( int i = 0; i < targetStreams.size(); i++ ) {
        SwitchCaseTarget target = (SwitchCaseTarget) targetStreams.get( i ).getSubject();
        if ( target == null ) {
          break; // Skip over default option
        }
        if ( target.caseTargetTransform == null ) {
          throw new HopException( BaseMessages.getString(
            PKG, "SwitchCase.Log.NoTargetTransformSpecifiedForValue", target.caseValue ) );
        }

        RowSet rowSet = findOutputRowSet( target.caseTargetTransform.getName() );
        if ( rowSet == null ) {
          throw new HopException( BaseMessages.getString(
            PKG, "SwitchCase.Log.UnableToFindTargetRowSetForTransform", target.caseTargetTransform ) );
        }

        try {
          Object value =
            data.valueMeta.convertDataFromString(
              target.caseValue, data.stringValueMeta, null, null, ValueMetaInterface.TRIM_TYPE_NONE );

          // If we have a value and a rowset, we can store the combination in the map
          //
          if ( data.valueMeta.isNull( value ) ) {
            data.nullRowSetSet.add( rowSet );
          } else {
            // could not use byte[] as key in Maps, so we need to convert it to his specific hashCode for future
            // comparisons
            value = prepareObjectType( value );
            data.outputMap.put( value, rowSet );
          }
        } catch ( Exception e ) {
          throw new HopException( BaseMessages.getString(
            PKG, "SwitchCase.Log.UnableToConvertValue", target.caseValue ), e );
        }
      }

      if ( meta.getDefaultTargetTransform() != null ) {
        RowSet rowSet = findOutputRowSet( meta.getDefaultTargetTransform().getName() );
        if ( rowSet != null ) {
          data.defaultRowSetSet.add( rowSet );
          if ( data.nullRowSetSet.isEmpty() ) {
            data.nullRowSetSet.add( rowSet );
          }
        }
      }
    } catch ( Exception e ) {
      throw new HopException( e );
    }
  }

  protected static Object prepareObjectType( Object o ) {
    return ( o instanceof byte[] ) ? Arrays.hashCode( (byte[]) o ) : o;
  }

}