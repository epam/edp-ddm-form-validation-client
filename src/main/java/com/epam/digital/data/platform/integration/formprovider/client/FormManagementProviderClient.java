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
import com.epam.digital.data.platform.integration.formprovider.dto.FormDto;
import com.epam.digital.data.platform.storage.form.dto.FormDataDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * The interface represents a feign client and used to validate form data.
 */
@FeignClient(name = "validation-form-client", url = "${form-management-provider.url}", configuration = FeignConfig.class)
public interface FormManagementProviderClient {

  /**
   * Form data validation method.
   *
   * @param id       form identifier.
   * @param formData form data for validation.
   * @return form data.
   */
  @PostMapping("/{id}/submission?dryrun=1")
  FormDataDto validateFormData(@PathVariable("id") String id, @RequestBody FormDataDto formData);

  /**
   * Get form method
   * @param id form identifier
   * @return {@link FormDto} form object
   */
  @GetMapping("/{id}")
  FormDto getForm(@PathVariable("id") String id);
}
