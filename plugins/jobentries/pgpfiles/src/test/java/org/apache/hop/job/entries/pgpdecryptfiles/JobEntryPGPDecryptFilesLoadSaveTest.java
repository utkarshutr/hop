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

package org.apache.hop.job.entries.pgpdecryptfiles;

import org.apache.hop.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.apache.hop.pipeline.transforms.loadsave.validator.ArrayLoadSaveValidator;
import org.apache.hop.pipeline.transforms.loadsave.validator.FieldLoadSaveValidator;
import org.apache.hop.pipeline.transforms.loadsave.validator.StringLoadSaveValidator;
import org.junit.ClassRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JobEntryPGPDecryptFilesLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryPGPDecryptFiles> {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Override
  protected Class<JobEntryPGPDecryptFiles> getJobEntryClass() {
    return JobEntryPGPDecryptFiles.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "GPGLocation",
      "arg_from_previous",
      "include_subfolders",
      "add_result_filesname",
      "destination_is_a_file",
      "create_destination_folder",
      "addDate",
      "addTime",
      "SpecifyFormat",
      "dateTimeFormat",
      "nrErrorsLessThan",
      "success_condition",
      "AddDateBeforeExtension",
      "DoNotKeepFolderStructure",
      "ifFileExists",
      "destinationFolder",
      "ifMovedFileExists",
      "movedDateTimeFormat",
      "create_move_to_folder",
      "addMovedDate",
      "addMovedTime",
      "SpecifyMoveFormat",
      "AddMovedDateBeforeExtension",
      "source_filefolder",
      "passphrase",
      "destination_filefolder",
      "wildcard" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    int count = new Random().nextInt( 50 ) + 1;
    validators.put( "source_filefolder", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "passphrase", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "destination_filefolder", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );
    validators.put( "wildcard", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), count ) );

    return validators;
  }

}