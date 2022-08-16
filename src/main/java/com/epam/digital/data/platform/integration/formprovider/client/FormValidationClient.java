/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.integration.formprovider.client;

import com.epam.digital.data.platform.integration.formprovider.config.FeignConfig;
import com.epam.digital.data.platform.integration.formprovider.dto.FileDataValidationDto;
import com.epam.digital.data.platform.integration.formprovider.dto.FormDataValidationDto;
import com.epam.digital.data.platform.integration.formprovider.dto.FormFieldListValidationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * The interface represents a feign client and used to validate form data.
 */
@FeignClient(name = "validation-form-client", url = "${form-submission-validation.url}", configuration = FeignConfig.class)
public interface FormValidationClient {

  /**
   * Form data validation method.
   *
   * @param formKey  form identifier.
   * @param formData form data for validation.
   * @return form data.
   */
  @PostMapping("/api/form-submissions/{form-key}/validate")
  FormDataValidationDto validateFormData(@PathVariable("form-key") String formKey,
      @RequestBody FormDataValidationDto formData);

  /**
   * Data validation of a separate field.
   *
   * @param formKey     form identifier
   * @param fieldKey    form field identifier
   * @param fileDataDto file data for validation
   */
  @PostMapping("/api/form-submissions/{form-key}/fields/{field-key}/validate")
  void validateFileField(@PathVariable("form-key") String formKey,
      @PathVariable("field-key") String fieldKey, @RequestBody FileDataValidationDto fileDataDto);

  /**
   * Data validation field names.
   *
   * @param formKey form identifier
   * @param fields  list of form fields
   */
  @PostMapping("/api/form-submissions/{form-key}/fields/check")
  void checkFieldNames(@PathVariable("form-key") String formKey,
      @RequestBody FormFieldListValidationDto fields);
}
