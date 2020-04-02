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

package org.apache.hop.pipeline.transforms.mappingoutput;

import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.TransformDataInterface;
import org.apache.hop.pipeline.transform.TransformInterface;
import org.apache.hop.pipeline.transforms.mapping.MappingValueRename;

import java.util.List;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class MappingOutputData extends BaseTransformData implements TransformDataInterface {

  public TransformInterface[] targetTransforms;
  public String[] outputField;
  public String[] outputMapping;
  public RowMetaInterface outputRowMeta;
  public List<MappingValueRename> inputValueRenames;
  public List<MappingValueRename> outputValueRenames;

  public MappingOutputData() {
    super();
  }

}